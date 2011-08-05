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

/* XMLDemux - demux binary data
*  runs as plugin, subscribing to UDPCapture/UDP channel
*
* EMF
* 4/12/05
* for IOScan
*
* JPB 
*
*
*   Modified Version for header version 8,4,4,8
*
* JPW 12/14/2006 Add "-z" flag (recovery mode)
*
*
*/
   
   package com.rbnb.xmldemux;
   
   import java.io.ByteArrayInputStream;
   import java.io.DataInputStream;
   import java.io.*;
   import java.lang.String;
   import java.text.SimpleDateFormat;
   import java.util.Date;
   import java.util.TimerTask;
   import java.util.TimeZone;
   import java.util.Timer;
   import java.util.Vector;
   
   import com.rbnb.sapi.ChannelMap;
   import com.rbnb.sapi.Sink;
   import com.rbnb.sapi.Source;
   import com.rbnb.utility.ArgHandler;
   import com.rbnb.utility.ByteConvert;
   import com.rbnb.utility.RBNBProcess;
   
public class XMLDemux{
    
    String serverAddress="localhost:3333";
    String serverAddressOut="localhost:3333";
    int archive=0;  // default archive is off
    int cache=1000;  // was 100, larger helps build larger framesets = more efficient
    double start = 0.;
    String reference = null;
    
    // Specify which embedded channel is the time channel
    int tstampField=1;
    
    // JPW 07/24/2006
    // Use the RBNB arrival time as the output timestamp?
    // NOTE: This is actually the UDP packet's arrival time at UDPCapture
    // To use the embedded time in the UDP packet as the output time: use
    // "-t" on the command line; that will set this flag false
    boolean bUseArrivalTime = true;
    
    // JPW 07/24/2006: Change Nmarker from 8 to 9999; this way, by default the
    //                 entire marker string will be checked
    int Nmarker=9999;
    String mode=new String("none");
    String In=null;
    String out=null;
    boolean multichan= true;  // multiple channels, i.e. one RBO per chan
    boolean roundtime= false;  // round times to nearest second (to enhance time-compression)
    boolean swapBytes=true;    // word order swap
    long duration = 600000;  //write to disk every x, default 10 minutes
    
    // JPW 07/18/2006: Use the "id" attribute as the channel name?
    //                 When this is false, the channel name is taken from the
    //                 content of the "label" field.
    // This is set to true by using the "-I" flag
    boolean bUseIDForChanName = false;
    
    Sink snk=null;
    Source src=null;
    ChannelMap cmin=new ChannelMap(); //channels received
    ChannelMap csub=new ChannelMap(); //channel to subscribe to
    ChannelMap regmap = null;
    
    // Vector cmv=new Vector();
    
    XMLParser xml;
    String XMLFile = null;
    
    // JPW 07/24/2006: Change variable name from "s" to "bSilent"
    boolean bSilent = false;  //silent, default is debugging on
    private static final int printPeriod = 1000;
    // num packets read; print debug once every printPeriod number of packets
    int count = 0;
    
    // JPW 07/24/2006: Number of params to write to the IWG1 string
    // JPW 08/22/2006: Change default from 31 to 9999 - this is a simple way to
    //                 make sure ALL the demux channels are included in the
    //                 IWG1 string.
    int numIWGParams = 9999;
    
    // Name of the IWG1 channel
    String iwgChanName = "_IWG1";
     
    // Marker used at the head of the IWG1 string
    String iwgMarker = "IWG1";
    
    // JPW 12/14/2006: Add recover mode; a single request is made for data
    //                 from recoverStartTime to recoverStopTime
    boolean bRecoverMode = false;
    double recoverStartTime = 0;
    double recoverStopTime = 0;
    
    // JPW 08/07/2007: Add filter channel; only demux those UDP packets where
    //                 the value of filterChan is above filterValue
    // The channel to filter on
    String filterChan = null;
    // Only save data from those UDP packets whose values of filterChan are above filterValue
    double filterValue = 0;
    // The channel index into the UDP packet of where the filter chan is found
    int filterChanIdx = -1;
    // Index of filterChan's start byte in the UDP packet
    int filterChanStartByte = -1;
    // Number of bytes which make up filterChan's value
    int filterChanLength = -1;
    
    //-------------------------------------------------------------------------
    //Main
    public static void main(String[] arg) throws Exception {
	new XMLDemux(arg).start();
    }
   
    //-------------------------------------------------------------------------
    //Constructor
    public XMLDemux(String[] arg) throws Exception {
       
         try {
            ArgHandler ah=new ArgHandler(arg);
            if (ah.checkFlag('h')) {
               System.err.println("XMLDemux");
               System.err.println(" -a <server address> : address of RBNB server to read data from");
               System.err.println("             default : localhost:3333");
	       System.err.println(" -A <server address> : address of RBNB server to write data to");
               System.err.println("             default : localhost:3333");
	       System.err.println(" -b                  : if set, use native bytes (default = swap)");
               System.err.println(" -c <num>            : cache frames");
	       System.err.println(" -f <chan>=<value>   : only demux those UDP packets where the value of the given channel is above the specified value");
	       System.err.println(" -h                  : print this usage info");
	       System.err.println(" -i <name>           : name of input");
	       System.err.println("             no default; required option");
	       System.err.println(" -I                  : obtain channel name from the \"id\" attribute");
	       System.err.println("             default : Without the -I flag, XMLDemux uses the label field for the channel name");
               System.err.println(" -k <num>            : archive frames (append)");
               System.err.println(" -K <num>            : archive frames (create)");
               System.err.println("             default : 0 (no archiving)");
	       System.err.println(" -m                  : turn on multiple channels (default on)");
               System.err.println(" -M                  : turn OFF multiple channels");
	       System.err.println(" -n                  : the number of marker characters to compare");
	       System.err.println(" -o <name>           : name of output Source");
	       System.err.println("             default : use the marker string");
	       System.err.println(" -r                  : reference (default newest)");
	       System.err.println(" -R                  : turn on rounded-times");
               System.err.println(" -s                  : start (default 0)");
	       System.err.println(" -S                  : silent mode");
	       System.err.println(" -t                  : Use the embedded timestamp param (rather than arrival time)");
               System.err.println(" -T                  : embedded timestamp param position (NOTE: must be greater than 0)");
	       System.err.println("             default : 1");
	       System.err.println(" -v <IWG1 marker>    : Marker used at the head of the IWG1 string");
	       System.err.println("                     : NOTE: This string cannot contain any spaces!");
	       System.err.println("             default : " + iwgMarker);
	       System.err.println(" -w <num>            : number of consecutive params to write to the IWG1 string (including timestamp);");
	       System.err.println("                     : set this to 0 to not produce an IWG1 channel");
	       System.err.println("             default : " + numIWGParams);
	       System.err.println(" -x <name>           : name of XML file");
	       System.err.println("             no default; required option");
	       System.err.println(" -z <start>,<stop>   : Recover mode; read from input channel from time <start> to time <stop>");
	       System.err.println("                     : <start> and <stop> must be in seconds since epoch");
               RBNBProcess.exit(0);
            }
	    
	    //server
            if (ah.checkFlag('a')) {
               String serverAddressL=ah.getOption('a');
               if (serverAddress!=null) serverAddress=serverAddressL;
            }
            if (ah.checkFlag('A')) {
               String serverAddressL=ah.getOption('A');
               if (serverAddressOut!=null) serverAddressOut=serverAddressL;
            }
            else serverAddressOut = serverAddress;
	    
	    // native bytes flag 
            if (ah.checkFlag('b')){
               swapBytes = false;
            } else swapBytes=true;
	    
	    // cache size
	    if(ah.checkFlag('c')) {
		String tc = ah.getOption('c');
		cache = Integer.parseInt(tc);
	    }
	    
	    // filter on given channel
	    if (ah.checkFlag('f')) {
		String filterStr = ah.getOption('f');
		if ( (filterStr == null) || (filterStr.trim().equals("")) ) {
		    System.err.println("Missing arguments to the \"-f\" flag.  Must be \"-f <filter chan>=<filter value>\".");
		    System.exit(0);
		}
		// Parse filterChan and filterValue
		String[] filterStrArray = filterStr.split("=");
		if ( (filterStrArray == null)              ||
		     (filterStrArray.length != 2)          ||
	             (filterStrArray[0] == null)           ||
		     (filterStrArray[0].trim().equals("")) ||
		     (filterStrArray[1] == null)           ||
		     (filterStrArray[1].trim().equals("")) )
		{
		    System.err.println("Missing or illegal arguments to the \"-f\" flag.  Must be \"-f <filter chan>=<filter value>\".");
		    System.exit(0);
		}
		try {
		    filterChan = filterStrArray[0];
		    filterValue = Double.parseDouble(filterStrArray[1]);
		} catch (NumberFormatException nfe) {
		    System.err.println("Missing or illegal arguments to the \"-f\" flag.  Must be \"-f <filter chan>=<filter value>\".");
		    System.exit(0);
		}
	    }
	    
	    //input
            if (ah.checkFlag('i')) {
               In = ah.getOption('i');
            } else {
		System.err.println("Input name must be specified with the -i option.");
		System.exit(0);
	    }
	    
	    // Use "id" attribute for channel name
	    if (ah.checkFlag('I')) {
		bUseIDForChanName = true;
	    }
	    
	    //archive
            if (ah.checkFlag('k')) {
               String naf=ah.getOption('k');
               if (naf!=null) archive=Integer.parseInt(naf);
               if (archive>0) {
                  mode=new String("append");  // was create
                  if (archive<cache) cache=archive;
               }
            }
	    
	    //archive
            if (ah.checkFlag('K')) {
               String naf=ah.getOption('K');
               if (naf!=null) archive=Integer.parseInt(naf);
               if (archive>0) {
                  mode=new String("create");  // was create
                  if (archive<cache) cache=archive;
               }
            }
	    
	    // single/multiple channels 
            if (ah.checkFlag('m')){
               multichan = true;
            }
	    
	    // single/multiple channels 
            if (ah.checkFlag('M')){
               multichan = false;
            }
	    
	    if (ah.checkFlag('n')) {
               String nm = ah.getOption('n');
               if(nm!=null) Nmarker = Integer.parseInt(nm);
            }
	    
	    //output
            if (ah.checkFlag('o')) {
               out = ah.getOption('o');
            }
	    
	    if(ah.checkFlag('r')) {
                reference = ah.getOption('r');
            }
	    
	    // single/multiple channels
            if (ah.checkFlag('R')){
               roundtime = true;
            } else roundtime = false;
	    
	    if(ah.checkFlag('s')) {
                String strt = ah.getOption('s');
                if(strt != null) start = Double.parseDouble(strt);
            }
	    
	    //silent
            if (ah.checkFlag('S')){
               bSilent = true;
            } else bSilent = false;
	    
	    /*
	     * NO LONGER USED
	    //flushes the cache every -t miliseconds
            if (ah.checkFlag('t')) {
               String dur = ah.getOption('t');
               if(dur!=null) duration = Integer.parseInt(dur);
            }
	    */
	    
	    // Use embedded timestamp param as the output timestamp
	    if (ah.checkFlag('t')) {
		bUseArrivalTime = false;
	    }
	    
	    // timestamp parameter field
	    // NOTE: This software is currently developed assuming that
	    //       tstampField = 1.  If this is not the case, the software
	    //       will need to be reworked
            if (ah.checkFlag('T')){
		String tsf = ah.getOption('T');
		tstampField = Integer.parseInt(tsf);
		// JPW 07/24/2006: Must be greater than 0
		if (tstampField < 1) {
		    tstampField = 1;
		}
		if (tstampField != 1) {
		    System.err.println(
			"\n\n\nNOTE: XMLDemux currently requires the\n" +
			"timestamp parameter to be the first\n" +
			"parameter in the UDP packet.\n\n" +
			"You have specified that the timestamp\n" +
			"parameter is number " +
			tstampField +
			".\n\n" +
			"XMLDemux will not work correctly.\n\n");
		    try { Thread.sleep(10000); } catch (Exception e) {}
		}
            } else tstampField = 1;
	    
	    // JPW 08/29/2006: Add user-configurable IWG1 marker field; used
	    //                 at the head of the IWG1 string
	    if (ah.checkFlag('v')) {
		String tempMarkerStr = ah.getOption('v');
		if ( (tempMarkerStr != null) &&
		     (!tempMarkerStr.trim().equals("")) )
		{
		    iwgMarker = tempMarkerStr.trim();
		}
	    }
	    
	    // How many consecutive parameters to write to the IWG1 string
	    if (ah.checkFlag('w')) {
		String numStr = ah.getOption('w');
		numIWGParams = Integer.parseInt(numStr);
            }
	    
	    //XML file
            if (ah.checkFlag('x')){
               XMLFile = ah.getOption('x');
            } else {
		System.err.println("XML file must be specified with the -x option");
		System.exit(0);
	    }
	    
	    // JPW 12/14/2006: Recover mode
	    if (ah.checkFlag('z')){
		bRecoverMode = true;
		// The argument must be <start time>,<stop time>
		String timesStr = ah.getOption('z');
		if ( (timesStr == null) || (timesStr.trim().equals("")) ) {
		    System.err.println("Missing arguments to the \"-z\" flag.  Must be \"-z <start time>,<stop time>\".");
		    System.exit(0);
		}
		String[] timesStrArray = timesStr.split(",");
		if ( (timesStrArray == null)              ||
		     (timesStrArray.length != 2)          ||
	             (timesStrArray[0] == null)           ||
		     (timesStrArray[0].trim().equals("")) ||
		     (timesStrArray[1] == null)           ||
		     (timesStrArray[1].trim().equals("")) )
		{
		    System.err.println("Missing or illegal arguments to the \"-z\" flag.  Must be \"-z <start time>,<stop time>\".");
		    System.exit(0);
		}
		try {
		    recoverStartTime = Double.parseDouble(timesStrArray[0]);
		    recoverStopTime = Double.parseDouble(timesStrArray[1]);
		    if (recoverStopTime < recoverStartTime) {
			System.err.println("Illegal argument to the \"-z\" flag: Stop time must be greater than or equal to start time.");
			System.exit(0);
		    }
		} catch (NumberFormatException nfe) {
		    System.err.println("Missing or illegal arguments to the \"-z\" flag.  Must be \"-z <start time>,<stop time>\".");
		    System.exit(0);
		}
            }
	 }
         catch (Exception e) {
             System.err.println("Exception parsing arguments");
             e.printStackTrace();
             RBNBProcess.exit(0);
	 }
	 
	 //check for xml file
         if(!(new File(XMLFile)).exists())
         {
            System.err.println("!!! Could not open XML file: "+XMLFile);
            System.exit(0);
         }
         xml = new XMLParser(XMLFile);
	 
	 //add new channel map
         csub.Add(In);
	 
	 //open connection
         String []info = xml.getInfo();      
	 
         System.err.println("marker: "+xml.getMarker());
      	 if(out == null) {
	     String suffix = "";  // was "Demux"
	     out = xml.getMarker();
	     // JPW 07/18/2006: Setting the Source name by taking the last
	     //                 8 characters was done when the marker field
	     //                 contained multiple pieces of info; this is
	     //                 no longer the case.
	     // if(out.length() >= 16) out = out.substring(8,16);
	     out = out + suffix;
	 }
	 
      	//registering channel map with channel info 
         regmap = new ChannelMap();
         String []label = xml.getLabel();
	 String []id = xml.getID();
         String []type = xml.getType();
         String cname = "";
	 
         for(int i=1; i<=xml.getNParam(); i++) {
	    
	    // JPW 07/18/2006: Add bUseIDForChanName; this is set true
	    //                 using the -I flag
	    //                 Also check for empty id or label string
	    if (bUseIDForChanName) {
		if ( (id[i] != null) && (!id[i].trim().equals("")) ) {
		    cname = id[i].trim();
		} else {
		    cname = label[i];
		}
	    } else {
		if ( (label[i] != null) && (!label[i].trim().equals("")) ) {
		    cname = label[i].trim();
		} else {
		    cname = id[i];
		}
	    }
	    //remove '/' from chan names (causes RBNB problems)
	    cname=cname.replace('/', ' ');
	    
            System.out.println("Adding Channel "+i+" "+cname);
            int idx  = regmap.Add(cname);
            if(info[i] != null) regmap.PutUserInfo(idx,info[i]);
	    else                regmap.PutUserInfo(idx,"<null userinfo>");    // need to put userInfo on all or none
	    
	    // JPW 08/07/2007: See if this is our filterChan
	    if ( (filterChan != null) && (cname.equals(filterChan)) ) {
		filterChanIdx = i;
	    }
	    
         }
	 
	 // JPW 08/07/2007: If we were expecting a filter channel, and none was
	 //                 found, quit
	 if ( (filterChan != null) && (filterChanIdx <= 0) ) {
	     System.err.println("The filter channel, " + filterChan + ", was not found in this demux specification; quitting.");
	     System.exit(0);
	 }
	 // Determine where to find filterChan in the UDP packet
	 if (filterChanIdx > 0) {
	    filterChanStartByte = xml.getMarker().length();
	    for (int chanIdx = 1; chanIdx < filterChanIdx; ++chanIdx) {
		if(type[chanIdx].equals("double")) {
		    filterChanStartByte += 8;
		} else {
		    filterChanStartByte += 4;
		}
	    }
	    filterChanLength = 8;
	    if (!type[filterChanIdx].equals("double")) {
		filterChanLength = 4;
	    }
	 }
	 
	 /* add latency channel */
         int idx = regmap.Add("_Latency");
         regmap.PutUserInfo(idx,"Creare_recieved - Data_timestamp");
	 
	 /* add IWG1 channel */
	 // JPW 08/22/2006: Only add this channel if numIWGParams > 0
	 if (numIWGParams > 0) {
             idx = regmap.Add(iwgChanName);
	     regmap.PutUserInfo(idx,"IWG1 data string");
	 }
       }
       
       private void makeSource() {
	   do {
	       try {
		   if (src!=null) { //a restart; do not overwrite earlier data
		       if (!mode.equals("none")) mode="append";
		       System.err.println("restarting source connection...");
		       if (src.VerifyConnection()) src.CloseRBNBConnection();
		   }
		   src=new Source();
		   src.SetRingBuffer(cache,mode,archive);
		   System.err.println("Output to: " + out);
		   //System.err.println("Connecting to source: "+serverAddressOut);
		   src.OpenRBNBConnection(serverAddressOut,out);
		   System.err.println("Connected to Source: "+serverAddressOut);
		   // JPW/EMF 09/22/2006: If it takes a while for the Source to come up,
		   //                     this Register() call can throw an exception.
		   //                     Therefore, try it in a loop.
		   boolean bRegistered = false;
		   while (!bRegistered) {
			   try {
				   src.Register(regmap);
				   bRegistered = true;
			   } catch (Exception ex) {
				   System.err.println("Can't yet register chans...try again...");
			   }
		   }
		   break;
	       } catch (Exception e) {
		   System.err.println("exception creating source; will try again after a 100 sec sleep");
		   e.printStackTrace();
		   // JPW/MJM 08/01/2007: Sleep longer in case archive recovery is happening
		   try {Thread.currentThread().sleep(100000);} catch (Exception e2) {}
	       }
	   } while (true);
       } //end method makeSource

	       
       private void makeSink() {
	   do {
	       try {
		   //open RBNB connections
		   if (snk!=null) { //a restart; must be from newest
		       if (reference!=null && reference.equals("oldest")) reference="newest";
		       System.err.println("restarting sink connection...");
		       if (snk.VerifyConnection()) snk.CloseRBNBConnection();
		   }
		   snk=new Sink();
		   snk.OpenRBNBConnection(serverAddress,"XMLDemuxSink");
		   System.err.println("Connected to Sink: "+serverAddress);
		   // JPW 12/14/2006: Add recover mode; we'll make one request
		   //                 for all the data
		   if (bRecoverMode) {
		       System.err.println(
		           "\nRecover mode: make one request for data from time " +
			   recoverStartTime +
			   " to time " +
			   recoverStopTime);
		       double duration = recoverStopTime - recoverStartTime;
		       snk.Request(csub,recoverStartTime,duration,"absolute");
		   } else {
		       if(reference == null || reference.equals("")) 	{
		           System.err.println("Running subscribe-by-frame newest mode");
			   snk.Subscribe(csub);
		       } else {
		           System.err.println("Running subscribe-by-time mode: "+start+", "+reference);
			   snk.Subscribe(csub,start,0,reference);
		       }
		   }
		   break;
	       } catch (Exception e) {
		   System.err.println("exception creating sink; will try again");
		   e.printStackTrace();
		   try {Thread.currentThread().sleep(10000);} catch (Exception e2) {}
	       }
	   } while (true);
       } //end method makeSink
       
   	//-------------------------------------------------------------------------
   	//loop listening for incoming data
       public void start() throws Exception {
	 
	 int index=0;
         count = 0;
	 
	 //create RBNB connections
	 makeSource();
	 makeSink();
	 
	 // JPW 08/07/2007: Add filter mode
	 if (filterChanIdx > 0) {
	     System.err.println(
	         "\nFilter on channel " +
		 filterChan +
		 ": values must be greater than " +
		 filterValue +
		 " to save data from the UDP packet");
	 }
	 
	 // JPW 12/14/2006: Add recovery mode
	 if (bRecoverMode) {
	     processRecoverRequest();
	     return;
	 }
	 
         while (true)
         {
	    //print debug when count == printPeriod
            count++;
            
            do {
		try {
		    cmin=snk.Fetch(600000);
		    break;
		} catch (Exception e) {
		    System.err.println("exception fetching from sink, restarting");
		    e.printStackTrace();
		    makeSink();
		}
	    } while (true);
	    
	    // MJM 07/14/2005: Add check on number of channels
            if ((cmin.NumberOfChannels() > 0) && !cmin.GetIfFetchTimedOut()) 
            {
               index++;
               if(!bSilent) System.out.println(index+" -Packet Recieved-, size: "+cmin.GetData(0).length);          	
               process(cmin);
	       System.err.print(".");
	       
	       /*  flush inside process? mjm 9/05
               if (cmv!=null && cmv.size()>0) {
                   for (int i=0;i<cmv.size();i++) {
                       src.Flush((ChannelMap)cmv.get(i));
                  }
               }
	       */
	       
            } else {
		System.err.print("x");
		// MJM 07/21/2011: for network disruption tolerance,
		//                 reset connection on any timeout
		makeSink();
	    }
	    
	    // Reset count if needed
	    if (count == printPeriod) {
               System.err.println();
               count = 0;
            }
	    
         }
      }
      
	//---------------------------------------------------------------------
	// processRecoverRequest
	//
	// JPW 12/14/2006
	//
	// Make one request for all data from recoverStartTime to
	// recoverStopTime.  Then loop through the fetched data and process it.
	// Detach from the Source when complete.
	private void processRecoverRequest() {
	    
	    try {
		System.err.println("Fetching data to recover...");
		cmin=snk.Fetch(-1);
		System.err.println("Got the data.");
	    } catch (Exception e) {
		System.err.println("Exception fetching from sink; closing connections");
		e.printStackTrace();
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    if ( (cmin.NumberOfChannels() != 1) || (cmin.GetIfFetchTimedOut()) ) {
		System.err.println("Didn't fetch any data; closing connections");
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    if (cmin.GetIndex(In)!=0) {
		System.err.println("Wrong name for input channel; closing connections");
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    try {
		process(cmin);
	    } catch (Exception e) {
		System.err.println("Exception processing data; closing connections");
		e.printStackTrace();
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    snk.CloseRBNBConnection();
	    src.Detach();
	    System.err.println("Recovered data.  Source detached.");
	    
	}
      
      
      //-------------------------------------------------------------------------
      // process data channels
      //
      // JPW 08/07/2007: Add filtering capability; if filterChan's value is not
      //                 above filterValue, then don't flush the data for this
      //                 UDP packet to the RBNB.
      //
      public void process(ChannelMap in) throws Exception {
	
	ChannelMap out = new ChannelMap();
	
	try {
	    if (in.GetIndex(In)!=0) return;
	    
            byte[][] data=in.GetDataAsByteArray(0);
	    
	    //retrieving channel names&descriptions and marker (again, ugh)
            String []type = xml.getType();
            String []label = xml.getLabel();
	    String []id = xml.getID();
            String []info = xml.getInfo();
            String marker = xml.getMarker(); 
            int    lmarker = marker.length();
	    String cname = "";
	    
	    // JPW 07/24/2006: Check the size of marker against Nmarker
	    if (lmarker < Nmarker) {
		Nmarker = lmarker;
	    }
	    // Check the first Nmarker characters of the marker field
            String cmarker = marker.substring(0,Nmarker);
            
            //size of packet to be read
            int size = xml.getSize();
	    
	    int data_length = data.length;
	    if (bRecoverMode) {
		System.err.println("Recover " + data_length + " packets");
	    }
            
            for (int i=0; i<data.length; ++i) {
		
		if ( (bRecoverMode) && ((i % 10) == 0) ) {
		    System.err.println("Packet " + i + " out of " + data_length);
		}
		
		if(data[i].length < lmarker) {
		    // We know this can't be good - the entire data packet
		    // size is less than the size of the expected marker!
		    continue;
		}
                String udpmarker = new String(data[i],0,Nmarker);
		
                if(!bSilent) {
		    System.err.println(
			"Want marker,size: " +
			cmarker +
			"," +
			size +
			", Got: " +
			udpmarker +
			"," +
			data[i].length);
		}
            	// NOTE: "udpmarker" is the marker we got from the UDP packet
		//       "cmarker" is the expected marker (read from XML config file)
		if (data[i].length==size && udpmarker.equals(cmarker)) {
		    // got a good UDP packet!
		    
		    // JPW 08/07/2007: If we are filtering, determine the value
		    //                 of the filter channel to see if we want
		    //                 to store data from this UDP packet
		    if (filterChanIdx > 0) {
			double currentFilterChanValue = 0;
			if (filterChanLength == 4) {
			    byte[] doubleArray = new byte[8];
			    doubleArray[0] = data[i][filterChanStartByte + 0];
			    doubleArray[1] = data[i][filterChanStartByte + 1];
			    doubleArray[2] = data[i][filterChanStartByte + 2];
			    doubleArray[3] = data[i][filterChanStartByte + 3];
			    float[] valArray = ByteConvert.byte2Float(doubleArray,swapBytes);
			    currentFilterChanValue = (double)valArray[0];
			} else {
			    byte[] doubleArray = new byte[8];
			    doubleArray[0] = data[i][filterChanStartByte + 0];
			    doubleArray[1] = data[i][filterChanStartByte + 1];
			    doubleArray[2] = data[i][filterChanStartByte + 2];
			    doubleArray[3] = data[i][filterChanStartByte + 3];
			    doubleArray[4] = data[i][filterChanStartByte + 4];
			    doubleArray[5] = data[i][filterChanStartByte + 5];
			    doubleArray[6] = data[i][filterChanStartByte + 6];
			    doubleArray[7] = data[i][filterChanStartByte + 7];
			    double[] valArray = ByteConvert.byte2Double(doubleArray,swapBytes);
			    currentFilterChanValue = valArray[0];
			}
			if (!bSilent) System.err.println("Current value of the filter channel = " + currentFilterChanValue);
			if ( (currentFilterChanValue == Double.NaN) ||
			     (currentFilterChanValue <= filterValue) )
			{
			    // Go on to the next data point
			    if (!bSilent) System.err.println("    UDP packet didn't pass filter; go on to next packet");
			    continue;
			}
		    }
		    
		    //Starts reading after marker size
		    ByteArrayInputStream bais = new ByteArrayInputStream(data[i],lmarker,data[i].length);
		    
		    // Time the UDP packet arrived at UDPCapture
		    double arrivalTime = in.GetTimes(0)[i];
		    // Timestamp embedded in the UDP packet
		    double embeddedTime = 0.0;
		    // Timestamp put on the output data
		    // This can either be arrivalTime or embeddedTime;
		    // initialize it to arrivalTime for now
		    double rbnbOutTime = arrivalTime;
		    
		    out.Clear();
		    
		    // JPW 07/24/2006: String data for the new IWG1 channel
		    // JPW 08/29/2006: Allow for user-defined marker
		    // StringBuffer iwgSB = new StringBuffer("IWG1");
		    StringBuffer iwgSB = new StringBuffer(iwgMarker);
		    
		    // NOTE: If "multichan" is true, we flush each channel individually
		    //       That is, each channel goes in its own RingBuffer
		    
		    // processing of data channels
		    if(!multichan && (bUseArrivalTime)) {
			// Just one put of arrival time takes care of the entire ChannelMap
			out.PutTime(arrivalTime,0);
		    }
		    
		    for(int q=1; q<=xml.getNParam(); q++) {
			//create a new channel each time when in multiple mode              
			if(multichan)out.Clear();  // try clearing vs creating new (less GC?)
			
			// JPW 07/18/2006: Add bUseIDForChanName; this is set true
	    		//                 using the -I flag
	    		//                 Also check for empty id or label string
			// if(label[q] == null) cname = id[q];
			// else                 cname = label[q];
	    		if (bUseIDForChanName) {
			    if ( (id[q] != null) && (!id[q].trim().equals("")) ) {
				cname = id[q].trim();
			    } else {
				cname = label[q];
			    }
	    		} else {
			    if ( (label[q] != null) && (!label[q].trim().equals("")) ) {
				cname = label[q].trim();
			    } else {
				cname = id[q];
			    }
			}
			//remove '/' from chan names (causes RBNB problems)
			cname=cname.replace('/', ' ');
			
			if(type[q].equals("double")) {
			    int idx = out.Add(cname);
			    byte[] eight=new byte[8];
			    bais.read(eight,0,8);     
			    double[] z=ByteConvert.byte2Double(eight,swapBytes);
			    // We presume the timestamp param will be a double
			    if(q == tstampField) {
				// use arrival time if embedded time<=0
				if (z[0] > 0.) {
				    embeddedTime = z[0];
				} else {
				    embeddedTime = arrivalTime;
				    if (!bSilent || (count == printPeriod)) {
					System.err.println(
					    "Warning: embedded time is zero or negative; using arrival time");
				    }
				}
				if(roundtime==true) embeddedTime = Math.round(embeddedTime);
				if(!bSilent || (count == printPeriod)) System.err.println("TIME: " + embeddedTime);
				// NOTE: This is why XMLDemux is currently designed only for
				//       tstampField = 1.  If we are using the timestamp
				//       which is embedded in the UDP packet, then this *must*
				//       be the first channel we parse (because time needs to
				//       be set first before data is added to the ChannelMap).
				if (!bUseArrivalTime) {
				    rbnbOutTime = embeddedTime;
				}
				if(!multichan && (!bUseArrivalTime)) out.PutTime(embeddedTime,0);
			    }
			    if (!bSilent || (count == printPeriod)) System.err.println("DATA["+q+"], "+cname+" "+z[0]);
			    if (multichan) {
				out.PutTime(rbnbOutTime,0);
			    }
			    out.PutDataAsFloat64(idx,z);
			    if(q == tstampField) {
				appendTimeToIWG(iwgSB,z[0],q);
			    } else {
				appendDataToIWG(iwgSB,z[0],q);
			    }
			}
			
			if(type[q].equals("float")) {
			    int idx=out.Add(cname);
			    byte[] four=new byte[4];
			    bais.read(four,0,4);
			    float[] z=ByteConvert.byte2Float(four,swapBytes);
			    if (!bSilent || (count == printPeriod)) System.err.println("DATA["+q+"], "+cname+" "+z[0]);
			    if (multichan) {
				out.PutTime(rbnbOutTime,0);
			    }
			    out.PutDataAsFloat32(idx,z);
			    appendDataToIWG(iwgSB,z[0],q);
			}
			if(multichan) {
			    do {
				try {
				    src.Flush(out);
				    break;
				} catch (Exception e) {
				    System.err.println("exception flushing to source, restarting");
				    e.printStackTrace();
				    makeSource();
				}
			    } while (true);
			}
		    }
		    
		    /* Latency channel */
		    if(multichan) out.Clear();
		    int idx = out.Add("_Latency");
		    double lat[] = new double[1];
		    lat[0] = arrivalTime - embeddedTime;
		    if (!bSilent || (count == printPeriod)) {
			System.err.println("Latency "+lat[0]);
		    }
		    if (multichan) {
			out.PutTime(rbnbOutTime,0);
		    }
		    out.PutDataAsFloat64(idx,lat);
		    if(multichan) {
			do {
			    try {
				src.Flush(out);
				break;
			    } catch (Exception e) {
				System.err.println("exception flushing to source, restarting");
				e.printStackTrace();
				makeSource();
			    }
			} while (true);
		    }
		    
		    /* IWG1 channel */
		    // JPW 08/22/2006: Add this channel if numIWGParams > 0
		    if (numIWGParams > 0) {
			if(multichan) out.Clear();
			idx = out.Add(iwgChanName);
			iwgSB.append("\r\n");
			String iwgStr = iwgSB.toString();
			if (!bSilent || (count == printPeriod)) {
			    System.err.println(
				iwgChanName + " channel: " + iwgStr);
			}
			if (multichan) {
			    out.PutTime(rbnbOutTime,0);
			}
			out.PutDataAsString(idx,iwgStr);
			if(multichan) {
			    do {
				try {
				    src.Flush(out);
				    break;
				} catch (Exception e) {
				    System.err.println("exception flushing to source, restarting");
				    e.printStackTrace();
				    makeSource();
				}
			    } while (true);
			}
		    }
		}
            }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	if(!multichan) {
	    do {
		try {
		    src.Flush(out);// flush multiplexed map once at end
		    break;
		} catch (Exception e) {
		    System.err.println("exception flushing to source, restarting");
		    e.printStackTrace();
		    makeSource();
		}
	    } while (true);
	}
	if(!bSilent) System.err.println("WROTE DATA");
	
      } //end method process
      
      // NOTE: timeI is in seconds since epoch
      private void appendTimeToIWG(
	StringBuffer iwgSBI, double timeI, int paramIdxI)
      {
	if (paramIdxI > numIWGParams) {
	    return;
	}
	// JPW 08/28/2006: At Larry's request, add millisecond output to the
	//                 time. Accomplished by adding ".SSS" to format str.
	String format = new String("yyyy-MM-dd'T'HH:mm:ss.SSS");
	SimpleDateFormat sdf = new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	// Convert timeI from seconds since epoch to milliseconds since epoch
	Date date = new Date( (long)(timeI * 1000.0) );
	String dateString = sdf.format(date);
	iwgSBI.append(",");
	iwgSBI.append(dateString);
      }
      
      private void appendDataToIWG(
	StringBuffer iwgSBI, double dataI, int paramIdxI)
      {
	if (paramIdxI > numIWGParams) {
	    return;
	}
	iwgSBI.append(",");
	if ( (dataI != Double.NaN)        &&
	     (dataI >= -Double.MAX_VALUE) &&
	     (dataI <= Double.MAX_VALUE)  &&
	     (dataI != Double.NEGATIVE_INFINITY) &&
	     (dataI != Double.POSITIVE_INFINITY) )
	{
	    iwgSBI.append(dataI);
	}
	else if ( (dataI == Double.NEGATIVE_INFINITY) ||
		  (dataI == Double.POSITIVE_INFINITY) )
	{
	    iwgSBI.append("inf");
	}
	else
	{
	    iwgSBI.append("nan");
	}
      }
      
      private void appendDataToIWG(
	StringBuffer iwgSBI, float dataI, int paramIdxI)
      {
	if (paramIdxI > numIWGParams) {
	    return;
	}
	iwgSBI.append(",");
	if ( (dataI != Float.NaN)        &&
	     (dataI >= -Float.MAX_VALUE) &&
	     (dataI <= Float.MAX_VALUE)  &&
	     (dataI != Float.NEGATIVE_INFINITY) &&
	     (dataI != Float.POSITIVE_INFINITY) )
	{
	    iwgSBI.append(dataI);
	}
	else if ( (dataI == Float.NEGATIVE_INFINITY) ||
		  (dataI == Float.POSITIVE_INFINITY) )
	{
	    iwgSBI.append("inf");
	}
	else
	{
	    iwgSBI.append("nan");
	}
      }
      
   } //end class XMLDemux
   
