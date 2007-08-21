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

// FileDrop - drops text message into local file
//             modelled after SQLPlugIn
// MJM
// FileDrop:   Subscribe to a remote source, write its data to files
// Matt Miller, Creare Inc, July 29, 2004
// revised Sept 23, 2004 for request "next" mode
// Copyright 2004 Creare Incorporated

import com.rbnb.sapi.*;
import com.rbnb.utility.*;

public class FileDrop {
    private String rbnbServer="localhost:3333";  // RBNB server to connect
    private String sinkName="FileDrop";          // name of this sink
    private Sink sink=null;                      // sink connection
    private RequestOptions reqop;                // request options
    private String pickup="Fdrop/*";             // pickup subscription
    private String dropFolder=".";               // folder to write output
    private boolean subscribe=false;             // subscribe or request mode 
    private long waitTime=5000;                  // msec to sleep between requests

    public FileDrop(String[] args) {
	//parse args
	try {
	    ArgHandler ah=new ArgHandler(args);
	    if (ah.checkFlag('h')) {
		throw new Exception("");
	    }
	    if (ah.checkFlag('a')) {
		rbnbServer=ah.getOption('a');
		if (rbnbServer==null) throw new Exception("Must specify rbnb server with -a");
	    }
	    if (ah.checkFlag('n')) {
		sinkName=ah.getOption('n');
	    }
	    if (ah.checkFlag('p')) {
		pickup=ah.getOption('p');
	    }
	    if (ah.checkFlag('d')) {
       		dropFolder=ah.getOption('d');
	    }
	    if (ah.checkFlag('w')) {
       		waitTime=Long.parseLong(ah.getOption('w'));
	    }
	    if (ah.checkFlag('s')) {
       		subscribe=true;
	    }

	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.err.println("Usage:  java FileDrop");
	    System.err.println("\t-h\tprint this usage guide");
	    System.err.println("\t-a\t<rbnb server host:port>\tdefault: "+rbnbServer);
	    System.err.println("\t-n\t<sink name>            \tdefault: "+sinkName);
	    System.err.println("\t-p\t<pickup subscription>  \tdefault: "+pickup);
	    System.err.println("\t-d\t<drop folder>          \tdefault: "+dropFolder);
	    System.err.println("\t-s\t<subscribe>            \tdefault: false");
	    RBNBProcess.exit(0);
	}

    } //end constructor
    
    public static void main(String[] args) {
	(new FileDrop(args)).exec();
    } //end method main
    
    // loop handling file drops
    public void exec() {
	int maxtry=0;
	double rtime = 0.;
	boolean newest = false;

	if(waitTime <= 0) subscribe = true;

	while(true) {   // recovery from exception
	    try {
		//make rbnb connection
		sink=new Sink();
		sink.OpenRBNBConnection(rbnbServer,sinkName);
		
		reqop = new RequestOptions();
		reqop.setMaxWait(10000);   // this doesn't actually seem to work for fetch "next"

		// subscribe to pickup
		ChannelMap cmr=new ChannelMap();
		ChannelMap cm=new ChannelMap();
		cmr.Add(pickup);
		if(subscribe) {
		    sink.Subscribe(cmr, 0., 0., "newest");  // only time-based subs work w wildcards
		    //sink.Subscribe(cm); 
		}

		//loop handling items that show up 
		while (true) {
		    // try sleepy poll for next
		    if(!subscribe) {
			//System.err.println("request at rtime: "+rtime);
			//if(rtime > 0.) sink.Request(cmr,rtime+.0001,0.,"after");
			// NOTE:  "next" via RoutingPI requires RoutingPI run with >V2.4.4 SAPI
			if(rtime > 0.) sink.Request(cmr,rtime,0.,"next",reqop);
			else {
			    //sink.Request(cmr,0.,0.,"newest");
			    sink.RequestRegistration(cmr);  // initial request just to get rtime
			    newest = true;
			}
		    }

		    // could time-out and verify connection to be more robust
		    cm=sink.Fetch(-1); //block until request arrives
		    //System.err.println("FileDrop got nchan: "+cm.NumberOfChannels());

		    //extract file(s) from fetch
		    for (int i=0;i<cm.NumberOfChannels();i++) {
			byte fdat[]= cm.GetData(i);
			String cname = cm.GetName(i);
			double ttime = cm.GetTimeStart(i);
			//System.err.println("FileDrop got Chan: "+cm.GetName(i)+", "+fdat.length+" bytes");

			// if(subscribe || (ttime > rtime)) {  // only if newer (shouldn't happen with "next")
			if(true) { 	// always write what we get 
			    if(ttime>rtime) rtime = ttime;    // keep rtime up to date with very latest
			    if(!newest) {   // skip initial newest fetch
				cname = cname.substring(cname.lastIndexOf('/')+1);  // relative name
				String fname = dropFolder + "/" + cname;
				System.err.println("FileDrop wrote: "+fname+"; "+fdat.length+" bytes");
				try {  // write output to file
				    java.io.File file = new java.io.File(fname);
				    java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
				    fos.write(fdat);
				    fos.close();
				} catch (Exception e) {
				    System.err.println("Oops, FileDrop File Write Failed!");
				    break;
				}
			    }
			}    		
		    }

		    newest = false;
		    if(!subscribe && (cm.NumberOfChannels() == 0)) 
			Thread.currentThread().sleep(waitTime);  // take a break;
		}
	    } catch (Exception e) {
		sink.CloseRBNBConnection();
		e.printStackTrace();
		if(++maxtry < 10) System.err.println(sinkName+" exception. Restart "+maxtry+"/10");
		else              System.exit(-1);
	    }
	} // loop back to exception recovery point
    }//end exec method
    
}//end FileDrop class

