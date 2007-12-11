/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

//------------------------------------------------------------------------------------------------
// RoutingPlugIn - creates a local source, but fulfills requests by
//                 querying the remote source
//
// September 2005
// Copyright 2005 Creare Incorporated

// EMF 2005-10-20 added metrics reporting in separate source
// EMF 2005-10-12 for monitor requests, use UDP
// MJM 2004-09-10 modified VSource to become Routing plugin
// EMF 2004-07-19 added timeouts on requests, with crude reconnect logic
// EMF 2004-07-15 added second server connection, so PlugIn can do simple
//                routing-like data forwarding

// TO DO:
// further reconnect refinements

import com.rbnb.sapi.*;
import com.rbnb.utility.ArgHandler;  //for argument parsing
import com.rbnb.utility.MetricsHandler;
import com.rbnb.utility.RBNBProcess; //alternative to System.exit: don't bring down servlet engine

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

//------------------------------------------------------------------------------------------------
public class RoutingPlugIn extends Thread
//------------------------------------------------------------------------------------------------
{
    private boolean doneFlush = false;
    private boolean autoMirror = false;           //respond via mirror RBO

    private String piAddress=new String("localhost");    //server to connect to
    private String sAddress=new String("localhost");     //server to connect to
    private long timeout=60000;                          //timeout on requests, default 1 minute

    private Stack threadStack;
    private int maxThreads = 100;                          // max pushed threads (dormant open sinks)
    private String sinkName=new String();  		 // sink connection name
    private String pluginName=null;   			 // plugin connection name
    private PlugIn plugin=null;               		 // plugin connection
    private String remoteSource=null;         		 // name of remote source
    private Hashtable activeRequests=new Hashtable();
    private String loopback=new String("_loopback");

    private String myID=new String();			 // for debug out
    private long maxConnect=10000;			 // max failed reconnnects before quit
    private int udpPort=4444;
    
    private int metricsCache=3600;
    private int metricsArchive=0;
    private int metricsInterval=10;
    
    private boolean debug = false;		
//------------------------------------------------------------------------------------------------
    public static void main(String[] args) {

	RoutingPlugIn rpi = new RoutingPlugIn();

	try {	    //parse args

	    ArgHandler ah=new ArgHandler(args);
		
	    if (ah.checkFlag('h')) throw new Exception();

	    if (ah.checkFlag('d')) rpi.setDebug(true);
	    
	    if (ah.checkFlag('a')) {			// "gateway" or parent of route
		String addressL=ah.getOption('a');
		if (addressL!=null) rpi.setPiAddress(addressL);
	    }

	    if (ah.checkFlag('b')) {			// "sink" or child of route
		String addressL=ah.getOption('b');
		if (addressL!=null) rpi.setSAddress(addressL);
	    } 

	    if (ah.checkFlag('t')) {
		long to=Long.parseLong(ah.getOption('t'));
		if (to>=-1) rpi.setTimeout(to);
	    }        

	    if (ah.checkFlag('m')) {
			rpi.setMetrics(ah.getOption('m'));
	    }        
		
	    if (ah.checkFlag('n')) {
		String nameL=ah.getOption('n');
		if (nameL!=null) rpi.setPluginName(nameL);
	    }
	    
	} catch (Exception e) {
	    System.err.println("RoutingPlugIn argument exception "+e.getMessage());
	    System.err.println("Usage:  RoutingPlugIn");
	    System.err.println(" -h                 : print this help message");
	    System.err.println(" -d                 : debug mode");		
	    System.err.println(" -a host:port		: gateway or parent of route");
	    System.err.println("            default : localhost:3333");
	    System.err.println(" -b host:port		: sink or child of route");
	    System.err.println("            default : localhost:3333");
	    System.err.println(" -t timeout         : timeout on fetches (in ms)");
	    System.err.println("            default : 60000 (60 seconds)");
	    System.err.println(" -n name            : PlugIn client name");
	    System.err.println("            default : gateway server name");
	    System.err.println(" -m interval,archive,cache : metrics configuration");
	    System.err.println("            default : 10,3600,0");
	    e.printStackTrace();
	    RBNBProcess.exit(0);
	}
	
	rpi.start();
    }

//------------------------------------------------------------------------------------------------
    // Constructor
    public RoutingPlugIn() {
	plugin=new PlugIn();	
	threadStack = new Stack();
    }

//------------------------------------------------------------------------------------------------
// Access methods

    public void setPiAddress(String address) { this.piAddress=address; } 
    public void setDebug(boolean dbg) 		 { this.debug = dbg; }
    public void setSAddress(String address)  { this.sAddress=address; }    
    public void setTimeout(long to)          { this.timeout=to; }   
    public void setPluginName(String name)   { pluginName=name; }
    public void setMetrics(String params) {
	StringTokenizer st=new StringTokenizer(params,",");
	metricsInterval=Integer.parseInt(st.nextToken());
	metricsCache=Integer.parseInt(st.nextToken());
	metricsArchive=Integer.parseInt(st.nextToken());
    }

//------------------------------------------------------------------------------------------------
    public void run() {

	PlugInChannelMap picm=null;
	MetricsHandler mh=null;
	String gwName="X";
	int connectCount=0;
	long lastBytes=0; //metrics reference
	
	while(true) {

	    try {  //  master restart
		if(connectCount >= maxConnect) {	// avoid infinite loop reconnect
		    System.err.println(myID+": exceeded max consecutive connect failures ("+maxConnect+"), aborting!");
		    RBNBProcess.exit(0);
		}
		System.err.println("RoutingPI Startup, connectCount: "+connectCount);
		int sleepTime = connectCount * 1000 + 10;
		if(sleepTime > 60000) sleepTime = 60000;	// settle in at once/minute retry
		Thread.currentThread().sleep(sleepTime);   	// take a break and try again 
		connectCount += 1;
		
		// establish named connections
		try {
		    //loop through active requests, terminating them
		    for (Enumeration e=activeRequests.elements(); e.hasMoreElements();) {
			((AnswerRequest)e.nextElement()).interrupt(false);
		    }
		    activeRequests=new Hashtable();
		    
		    // initial sink connection just to get name
		    Sink sink=new Sink();
		    sink.OpenRBNBConnection(sAddress,"Route->X");
		    remoteSource = sink.GetServerName();
		    sink.CloseRBNBConnection();
		    
		    if (pluginName==null) pluginName = remoteSource.substring(1+remoteSource.lastIndexOf('/'));
		    if (!remoteSource.endsWith("/")) remoteSource=remoteSource+"/";
		    
		    plugin.OpenRBNBConnection(piAddress,pluginName);
		    pluginName=plugin.GetClientName();
		    
		    // construct sinkname for future use
		    gwName = plugin.GetServerName();	    
		    gwName = gwName.substring(1+gwName.lastIndexOf('/'));	    
		    sinkName = "Route->"+gwName;
		    
		    if (mh==null) {
			mh=new MetricsHandler(piAddress,"_Route."+pluginName,metricsCache,metricsArchive,metricsInterval);
			mh.start();
			}
			mh.updateInfo("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
				      "<!DOCTYPE metrics>\n"+
				      "<metrics>\n"+
				      "<pluginaddress>"+piAddress+"</pluginaddress>\n"+
				      "<sinkaddress>"+sAddress+"</sinkaddress>\n"+
				      "<sinkname>"+remoteSource+"</sinkname>\n"+
				      "<interval>"+metricsInterval+"</interval>\n"+
				      "</metrics>");
		    
		    
		    connectCount = 0;  // reset on success
		} catch (Exception e) {	
		    throw(e);
		}
		
		myID = gwName + "/" + pluginName;
		System.err.println("RoutingPlugin ID: "+myID);
		
		//loop handling requests
		try {
		    boolean reconnect=false;
		    
		    while (true) {
			if(reconnect) {
			    if (plugin.VerifyConnection()) { //looks ok, so leave alone
				if(debug) System.err.println(myID+": "+new Date()+": PlugIn connection verified, continuing...");  // MJM DEBUG
				reconnect=false;
			    } else { //something's bad, restart everything
				//loop through active requests, terminating them
				for (Enumeration e=activeRequests.elements(); e.hasMoreElements();) {
				    ((AnswerRequest)e.nextElement()).interrupt(false);
				}
				activeRequests=new Hashtable();
				//mh.close(); //stop metrics
				System.err.println(myID+": "+new Date()+": Reconnecting (streams will be lost)");
				plugin.CloseRBNBConnection();
				plugin.OpenRBNBConnection(piAddress,pluginName);
				pluginName=plugin.GetClientName();
				mh.updateInfo("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
				      "<!DOCTYPE metrics>\n"+
				      "<metrics>\n"+
				      "<pluginaddress>"+piAddress+"</pluginaddress>\n"+
				      "<sinkaddress>"+sAddress+"</sinkaddress>\n"+
				      "<sinkname>"+remoteSource+"</sinkname>\n"+
				      "<interval>"+metricsInterval+"</interval>\n"+
				      "</metrics>");
				reconnect=false;
			    }
			}
			
			doneFlush = false;
			picm=plugin.Fetch(timeout); // fetch request for routed data
			if(debug) System.err.println("Routing Plugin got picm: "+picm);	// MJM DEBUG
			
			long totalBytes=plugin.BytesTransferred();
			if (mh.update(totalBytes-lastBytes)==false) { //metrics failed; abort
				System.err.println("mh.update failed!");
			    //loop through active requests, terminating them
			    for (Enumeration e=activeRequests.elements(); e.hasMoreElements();) {
				((AnswerRequest)e.nextElement()).interrupt(false);
			    }
			    //throw new Exception("Metrics died; closing RoutingPlugIn");
			}
			lastBytes=totalBytes;
			//EMF 1/5/06: remove stale AnswerRequests on stack
			Iterator it=threadStack.iterator();
			long now=System.currentTimeMillis();
			while (it.hasNext()) {
				AnswerRequest ar=(AnswerRequest)it.next();
				if (now-ar.lastConnect > 2*timeout) {		// was hard-coded 60000 msec, give it plenty of time to avoid exception MJM 12/2007
					//System.err.println("removing stale AnswerRequest "+ar);
					ar.sink.CloseRBNBConnection();
					it.remove();		// this can cause java.util.ConcurrentModification exception ?! Now caught and ignored if happens MJM 12/2007
				}
			}
			
			switch (picm.GetRequestType()) {
			    
			case (PlugInChannelMap.RT_TIMEOUT): 	// "ping" to avoid connection timeouts
			    //System.err.println(gwName+": TimeOut, doneFlush: "+doneFlush+", Date: "+new Date());
			    if(!doneFlush) reconnect=true; 	// streaming flushes keep connection active
			    break;
			    
			case (PlugInChannelMap.RT_ENDOFSTREAM):
			    String eosID = picm.GetRequestId();
			    AnswerRequest ar = (AnswerRequest)activeRequests.get(eosID);
			    if (ar!=null) {
				ar.interrupt(true);
			    } else System.err.println("EOS with no matching thread: "+eosID);
			    break;
			    
			default: //must be request of some sort
			    // check if message
			    if (picm.NumberOfChannels()>0 && picm.GetName(0).equals(pluginName)) {
				boolean terminate=false;
				try {
				    //handle registration requests, else RBNB servlet won't send message
				    if (picm.GetRequestReference().equals("registration")) {
					picm.PutDataAsByteArray(0,new byte[1]);
					plugin.Flush(picm);
				    } else {
					String[] message=picm.GetDataAsString(0);
					com.rbnb.utility.XmlHandler xh=new com.rbnb.utility.XmlHandler(message[0]);
					String id=xh.getValueOf("id");
					String command=xh.getValueOf("command");
					//System.err.println("received command "+command+" for id "+id);
					if (id==null || command==null) {
					    System.err.println("Message ignored, needs id and command: "+message[0]);
					    throw new Exception();
					}
					if (id.equals(pluginName)&&command.equals("terminate")) {
					    System.err.println("Received shutdown message with ID "+picm.GetRequestId());
					    terminate=true;
					    picm.PutDataAsString(0,"RoutingPlugIn terminated");
					    plugin.Flush(picm);
					} else { //find existing request
					    AnswerRequest arold=(AnswerRequest)activeRequests.get(id);
					    if (arold!=null && command.equals("terminate")) arold.interrupt(true);
					    else System.err.println("Unknown request or command, ignored.");
					}
				    }
				} catch (Exception e) {
				    picm.Clear();
				    plugin.Flush(picm);
				}

				if (terminate) {		//loop through active requests, terminating them
				    for (Enumeration e=activeRequests.elements(); e.hasMoreElements();) {
					((AnswerRequest)e.nextElement()).interrupt(false);
				    }
					throw new Exception("shutting down");
				}
			    } else { 
				// launch thread for each request (threadStack pushed on exit from AnswerRequest)
				AnswerRequest a;
				if (threadStack.empty()) a = new AnswerRequest();
				else                     a = (AnswerRequest) threadStack.pop();
				a.setRequestMap(picm,mh);
				activeRequests.put(picm.GetRequestId(),a);
				(new Thread(a)).start();
			    }
			    break;
			}
			
			connectCount = 0;  // reset on success
		    }
		} catch (Exception e) { 
		    throw (e);
		}
	    }
	    catch (com.rbnb.sapi.SAPIException e) {
		e.printStackTrace(); 
		System.err.println(myID+": Connect Failure, Retrying...");
		continue;
	    }
	    catch (java.io.EOFException e) {
		System.err.println(myID+": Unexpected EOF, Retrying...");
		continue;
	    }
	    catch (Exception e) {
			e.printStackTrace(); 
			//System.err.println(myID+": Unknown Exception, aborting!");
			//EMF 1/19/06: don't close down, attempt restart
			//mh.close();
			if (plugin!=null) plugin.CloseRBNBConnection();
			if ((e.getMessage() != null) && e.getMessage().equals("shutting down")) {
				mh.close();
				break;
			}
			//break;
	    }
	}  // end while(true)
	//EMF 1/19/06 this now unreachable, so must be commented out
	RBNBProcess.exit(0);
    } //end run method

//------------------------------------------------------------------------------------------------
    private class AnswerRequest implements Runnable
//------------------------------------------------------------------------------------------------ 
    {
	private PlugInChannelMap requestMap;
	private String requestID=new String("");
	private Sink sink;
	private ChannelMap sinkMap;
	private long lastConnect=System.currentTimeMillis();
	private Thread myThread=null;
	private boolean EOS=false;
	private MetricsHandler mh=null;
	private long lastBytes=0; //metrics reference
	private boolean expectingEOS=false;

	AnswerRequest()
	{
	    // open and maintain sink object/connection in threadStack
	    sink = new Sink();
	    try {
		//System.err.println(myID+": New Sink!");
		sink.OpenRBNBConnection(sAddress,sinkName);
	    } catch (Exception e) {	e.printStackTrace(); }	
        }
	
	//terminate this AnswerRequest
	public void interrupt(boolean atEOS) {
            EOS=atEOS;
	    if (expectingEOS && EOS) expectingEOS=false;
            else if (myThread!=null) myThread.interrupt();
	}
	
	private void clear() {
            requestMap=null;
            requestID=new String("");
            sinkMap=null;
            myThread=null;
            EOS=false;
	}
	

//------------------------------------------------------------------------------------------------
	// Access methods
	public void setRequestMap(PlugInChannelMap requestMap, MetricsHandler mh) 	{
            this.requestMap=requestMap;
			this.mh=mh;
            requestID=requestMap.GetRequestId(); //requestMap is cleared on Flush,
                                                 // but ID needed later...
	}
	
	public PlugInChannelMap getRequestMap() { 
            return(this.requestMap); 
	}

//------------------------------------------------------------------------------------------------
	public void run()			//AnswerRequest run method
	{
        myThread=Thread.currentThread();
	    boolean dostream=false;
	    boolean verifyC=false;
	    long dT = 0;
	    boolean doUDPsend=false;
	    boolean doUDPreceive=false;
	    DatagramSocket ds=null;
	    DatagramPacket dp=null;
	    long now = System.currentTimeMillis();
	    Source pisrc = null;
	    long nFetch=0, nFlush=0;
	    int minperiod=0; //delay between fetches on paced monitor
	    String origReqID=null;
            
	    try {
//		if(((dT=(now - lastConnect)) > timeout) || !(verifyC=sink.VerifyConnection())) {
       		if(((dT=(now - lastConnect)) > timeout)) {
				//System.err.println("Stale connection, dt>timeout: "+dT+" > "+timeout);
				if(!(verifyC=sink.VerifyConnection())) {  // mjm 12/23/05:  only verify if stale
					sink.CloseRBNBConnection();
					sink.OpenRBNBConnection(sAddress,sinkName);
					System.err.println(myID+": Bad ping, re-opening sink! "+sinkName);
					lastConnect = System.currentTimeMillis();
				} //else               
					//System.err.println("Ping verified: "+verifyC);
		}

		//map channels to remote source 
		sinkMap=new ChannelMap();
		for (int i=0;i<requestMap.NumberOfChannels();i++) {
		    int j=sinkMap.Add(remoteSource+requestMap.GetName(i));
                    //EMF 1/5/07: send messages as well (so munges get through)
		    sinkMap.PutTimeRef(requestMap,i);
		    sinkMap.PutDataRef(j,requestMap,i);
		}

		//map empty folders to remote source (for registration requests)
		String[] folder=requestMap.GetFolderList();
		for (int i=0;i<folder.length;i++) {
		    sinkMap.AddFolder(remoteSource+folder[i]);
		}    
		
		//request data from remote source
		int reqtype = requestMap.GetRequestType();
		String reqref = requestMap.GetRequestReference();
		//System.err.println(myID + ": RequestType: "+reqtype+", ref "+reqref+", "+sinkMap);
                
                // re-enable RequestRegistration mjm 10/4/05    
//		if(reqref.equals("registration")) {
//		    sink.RequestRegistration(sinkMap);   // over-ridden by following Request with ref="registration"
//		} else

		// initial stab at auto-mirror */
		if( (autoMirror==true) &&
			(reqtype==PlugInChannelMap.RT_MONITOR || reqtype==PlugInChannelMap.RT_SUBSCRIBE) ) {
			doAutoMirror(pisrc);
		}

		if(reqtype==PlugInChannelMap.RT_MONITOR) {
		    dostream = true;
		    //check for message on first channel, which indicates if UDP sender or listener
		    String message=null;
		    if (requestMap.GetType(0)==ChannelMap.TYPE_STRING) message=requestMap.GetDataAsString(0)[0];
		    if (message==null || message.length()==0) {
				//set up UDP receiver
			doUDPreceive=true;
			setupUDPReceive(ds,dp,message,origReqID);
		    } else { //set up UDP sender
			doUDPsend=true;
			minperiod= setupUDPSend(ds, dp, message, minperiod);
		    }
		} 
		else if(reqtype==PlugInChannelMap.RT_SUBSCRIBE) {           // subscribe
		    dostream = true;
// System.err.println("Subscribe, duration: "+requestMap.GetRequestDuration()+", Start: "+requestMap.GetRequestStart());
		    if(requestMap.GetRequestDuration() == 0.) // cluge, currently no way to determine by-frame subscribe
			sink.Subscribe(sinkMap);
		    else
			sink.Subscribe(sinkMap,                   
				   requestMap.GetRequestStart(),
				   requestMap.GetRequestDuration(),
				   reqref);
		}
		else {
		    dostream=false;
		    sink.Request(sinkMap,                                   // request
				 requestMap.GetRequestStart(), 
				 requestMap.GetRequestDuration(), 
				 reqref);
		}

                if(dostream) System.err.println(myID+": Streaming!");
                
		// How to pace monitor mode?
		// Just let it pile up?  Not sure how much it can queue.  
		// Do see some lag accumulate, but not indefinitely (?)
 //               java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("HH:mm:ss.SSS");
 //               java.util.Date date=new java.util.Date();

		while(true) {
		    requestMap.Clear();
		    if (doUDPreceive) { //pick up incoming UDP packets
			sinkMap = UDPReceive(ds,dp);
		    } else { //use normal sink connection
			if (minperiod!=0) try {
			    Thread.currentThread().sleep(minperiod);
			} catch (Exception e) {}
			sinkMap=sink.Fetch(timeout);
			//System.err.println("sink.Fetch: "+sinkMap);
		    }
		    if (Thread.currentThread().isInterrupted()) throw new Exception("interrupt received");
		    
		    if(sinkMap.GetIfFetchTimedOut()) {		// Fetch timed-out, just try again
			//System.err.println(myID+": "+new Date()+": Routed data fetch timed out!");
			//dostream=false;
		    } else if (doUDPsend) {
			UDPSend(ds,dp,sinkMap);
		    } else {  // Got some data!
			//            date.setTime((long)(sinkMap.GetTimeStart(0)*1000.0));
			//             System.err.println("fetched data at time "+sdf.format(date));
			++nFetch;
			//copy data,times,types to answer original request
			folder=sinkMap.GetFolderList();
                        //System.err.println("Got folders: "+folder.length);
			for (int i=0;i<folder.length;i++) {
			    if (folder[i].startsWith(remoteSource)) {
				folder[i]=folder[i].substring(remoteSource.length());
				if (folder[i].length()>0) {
				    int idx=requestMap.GetIndex(folder[i]);
				    if (idx==-1) {
                                        requestMap.AddFolder(folder[i]);
                                        //System.err.println("Folder["+i+"]: "+folder[i]);
                                    }
				}
			    } // else NP
			}
			
                        //System.err.println("Got channels: "+sinkMap.NumberOfChannels());
			
			for (int i=0;i<sinkMap.NumberOfChannels();i++) {
			    String chan=sinkMap.GetName(i);
			    if (chan.startsWith(remoteSource)) {
				chan=chan.substring(remoteSource.length());
				int idx=requestMap.GetIndex(chan);
				if (idx==-1) idx=requestMap.Add(chan);
				//EMF 1/24/03: switch to PutDataRef to avoid byte order ambiguity
				requestMap.PutTimeRef(sinkMap,i);
				requestMap.PutDataRef(idx,sinkMap,i);
			    } else {
				System.err.println("RoutingPI: unexpected chan "+sinkMap.GetName(i));
			    }
			}
			
			//if(requestMap.NumberOfChannels() > 0) 
			//if((folder.length==0) && (sinkMap.NumberOfChannels()==0)) {
			//    requestMap.Add("ThumbNail/foo");  // grope !!!
			//    requestMap.PutDataAsString(0,"foobar");
			//}
			doneFlush = true;
			
			//EMF 10/24/05: update metrics (plugin handled by main class)
			long totalBytes=sink.BytesTransferred();
			mh.update(totalBytes-lastBytes);
			lastBytes=totalBytes;
			
			if (doUDPsend) {
			    UDPSend(ds,dp,requestMap);
			} else {
			    if(pisrc != null) pisrc.Flush(requestMap);           	// stream to mirror
			    else              plugin.Flush(requestMap,dostream); 	// or answer via plugin
			}

                        ++nFlush;
			//System.err.println(myID+": "+"Flush: "+nFlush+", "+requestMap);
                                                
			lastConnect = System.currentTimeMillis();	 	// update connect timer on success
			if (!dostream)	break;                                	// all done if not streaming
		    }
		}
	    } catch (Exception e) {
//e.printStackTrace();
		//clean up both ends as needed
		if (!EOS) {		// this case has a failure other than request terminating
                                        // we could try to re-establish the upstream connection?
		    try {
			plugin.Flush(requestMap); 	//tell server we're done
		    } catch (Exception ex) {
			System.err.println("Exception terminating output stream:");
			ex.printStackTrace();
		    }
		}
		sink.CloseRBNBConnection();
		if (doUDPreceive && origReqID!=null) { //message remote side to stop
		    System.err.println("sending message to remote sender to terminate");
		    try {
			sink.OpenRBNBConnection(sAddress,sinkName);
			ChannelMap cm=new ChannelMap();
			cm.Add(remoteSource+loopback+"/"+loopback); //channel name must be same as plugin name
			cm.PutDataAsString(0,"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+
					   "<!DOCTYPE route>"+
					   "<route>"+
					   "<id>"+origReqID+"</id>"+
					   "<command>terminate</command>"+
					   "</route>");
			sink.Request(cm,0,0,"absolute");
			sink.Fetch(100); //don't care about answer, just give request time to propagate
			sink.CloseRBNBConnection();
		    } catch (Exception ex) {
			System.err.println("Exception sending kill message.");
			ex.printStackTrace();
		    }
		}
		if (ds!=null) ds.close();
		
		System.err.println(myID+": "+new Date()+": Request Aborted!, EOS: "+EOS);
		e.printStackTrace();  
		// mjm 12/23/05:  try returning:
		return;    // return without pushing bad connection back on stack
	    }
	    //sink.CloseRBNBConnection();                // dump sink connection every time?
	    activeRequests.remove(requestID);
	    clear();	      				 // don't carry state along, except sink

	    // MJM 12/23/05:
	    // limit number of pushed threads
	    // could exit without push on stale threads that haven't done anything in a long while
	    if(threadStack.size() < maxThreads) {
		// System.err.println("pushing AnswerRequest thread: "+threadStack.size());
		threadStack.push(this);                      // push thread - includes open sink connection
	    } else
		System.err.println("Thread limit hit (running with existing stack): "+maxThreads);

	} // end AnswerRequest.run()
	
	private void doAutoMirror(Source pisrc) throws Exception {
		    pisrc = new Source();  
		    pisrc.OpenRBNBConnection(piAddress,"_"+pluginName);
		    ChannelMap regmap = new ChannelMap();   	 // re-register or lose user-data
		    sink.RequestRegistration(sinkMap);
		    regmap = sink.Fetch(-1);
		    requestMap = new PlugInChannelMap();
		    // copy over registration data (strange error if just register regmap?)
		    for (int i=0;i<regmap.NumberOfChannels();i++) {
			String chan=regmap.GetName(i);
			if (chan.startsWith(remoteSource)) {   // strip remote source name
			    chan=chan.substring(remoteSource.length());
			    requestMap.Add(chan);
			    requestMap.PutDataRef(i,regmap,i);
			    //System.err.println("auto-mirror: "+chan);
			}
		    }
		    pisrc.Register(requestMap);
	} //end method doAutoMirror
	
	private void setupUDPReceive(DatagramSocket ds,
				     DatagramPacket dp,
				     String message,
				     String origReqID) throws Exception {
	    int myport = ++udpPort;
	    String myhost=(java.net.InetAddress.getLocalHost()).getHostAddress();
	    
	    boolean gotSocket=false;
	    do {
		try {
		    ds=new java.net.DatagramSocket(myport);
		    gotSocket=true;
		} catch (Exception e) {
		    myport=++udpPort;
		}
	    } while (!gotSocket);

	    System.err.println("opened UDP listener on "+myhost+":"+myport);
	    dp=new java.net.DatagramPacket(new byte[65536],65536);
	    // redo sinkmap, to add remote RoutingPlugIn
	    sinkMap.Clear();
	    for (int i=0;i<requestMap.NumberOfChannels();i++) {
		//System.err.println("requesting remote chan "+remoteSource+loopback+"/"+requestMap.GetName(i));
		int j=sinkMap.Add(remoteSource+loopback+"/"+requestMap.GetName(i));
		sinkMap.PutDataAsString(j,"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+
					"<!DOCTYPE route>"+
					"<route>"+
					"<protocol>udp</protocol>"+
					"<host>"+myhost+"</host>"+
					"<port>"+myport+"</port>"+
					"<minperiod>0</minperiod>"+
					"<requestid>"+requestMap.GetRequestId()+"</requestid>"+
					"</route>");
	    }

	    //System.err.println("monitoring for "+sinkMap);
	    sink.Monitor(sinkMap,0);    // monitor
	    for (int i=0;i<10;i++) { //wait for response
		sinkMap=sink.Fetch(1000);
		if (!sinkMap.GetIfFetchTimedOut()) {
		    //System.err.println("received "+sinkMap);
		    if (sinkMap.GetType(0)==ChannelMap.TYPE_STRING) message=sinkMap.GetDataAsString(0)[0];
		    com.rbnb.utility.XmlHandler xh=new com.rbnb.utility.XmlHandler(message);
		    //pull out sender's host:port for filtering - TODO
		    //pull out requestID so can terminate later
		    origReqID=xh.getValueOf("requestid");
		    //System.err.println("    origReqID="+origReqID);
		    break;
		}
	    } //add code to deal with no response - kill stream
	} //end method setupUDPReceive
	
	private int setupUDPSend(DatagramSocket ds,
				  DatagramPacket dp,
				  String message,
				  int minperiod) throws Exception {
	    int myport = ++udpPort;
	    com.rbnb.utility.XmlHandler xh=new com.rbnb.utility.XmlHandler(message);
	    String protocol=xh.getValueOf("protocol");
	    if (!protocol.equals("udp")) {
		System.err.println("Unknown protocol "+protocol+", terminating request.");
		throw new Exception("Unknown protocol.");
	    }
	    String remoteHost=xh.getValueOf("host");
	    int remotePort=Integer.parseInt(xh.getValueOf("port"));

	    if (xh.getValueOf("minperiod")!=null)
		minperiod=Integer.parseInt(xh.getValueOf("minperiod"));
System.err.println("will send data via UDP to "+remoteHost+":"+remotePort+" with minperiod "+minperiod+" ms");
	    //System.err.println("original requestID was "+xh.getValueOf("requestID"));
	    boolean gotSocket=false;

	    do {
		try {
		    ds=new java.net.DatagramSocket(myport);
		    gotSocket=true;
		} catch (Exception e) {
		    myport=++udpPort;
		}
	    } while (!gotSocket) ;

	    String localhost=java.net.InetAddress.getLocalHost().getHostAddress();
System.err.println("from "+localhost+":"+myport);
	    java.net.InetSocketAddress isa=new java.net.InetSocketAddress(remoteHost,remotePort);
	    dp=new java.net.DatagramPacket(new byte[65536],65536);
	    dp.setSocketAddress(isa);
	    requestMap.PutTime(0,1);
	    requestMap.PutDataAsString(0,"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+
				       "<!DOCTYPE route>"+
				       "<route>"+
				       "<protocol>udp</protocol>"+
				       "<host>"+localhost+"</host>"+
				       "<port>"+myport+"</port>"+
				       "<minperiod>"+minperiod+"</minperiod>"+
				       "<requestid>"+requestMap.GetRequestId()+"</requestid>"+
				       "</route>");
	    expectingEOS=true;
	    plugin.Flush(requestMap,false);
	    //System.err.println("monitoring for "+sinkMap);
	    sink.Monitor(sinkMap,0); 
	    return minperiod;
	} //end method setupUDPSend

	private ChannelMap UDPReceive(DatagramSocket ds,
				      DatagramPacket dp) throws Exception {
	    ChannelMap cm=null;
	    ds.receive(dp);
	    if (dp.getLength()>0) {
		byte[] data=new byte[dp.getLength()];
		System.arraycopy(dp.getData(),dp.getOffset(),data,0,dp.getLength());
		java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(data);
		java.io.ObjectInputStream ois=new java.io.ObjectInputStream(bais);
		cm=(ChannelMap)ois.readObject();
		//System.err.println("UDP received ChannelMap "+sinkMap);
	    }
	    return cm;
	} //end method UDPReceive
	
	private void UDPSend(DatagramSocket ds,
			     DatagramPacket dp,
			     ChannelMap cm) throws Exception {
				 
	    java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
	    java.io.ObjectOutputStream oos=new java.io.ObjectOutputStream(baos);
	    //System.err.println("writing sinkMap "+sinkMap);
	    oos.writeObject(cm);
	    if (baos.size()<65536) {
		mh.update(baos.size());
		dp.setData(baos.toByteArray());
		ds.send(dp);
		//System.err.println("wrote UDP");
	    } else {
		System.err.println("UDP packet too large; not send");
	    }		 
	} //end method UDPSend
	    
	    
    } // end inner class AnswerRequest
} //end RoutingPlugIn class

