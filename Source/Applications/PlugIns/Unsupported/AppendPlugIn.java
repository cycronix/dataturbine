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

//AppendPlugIn - appends the channel(s) specified in the message to the regular channels
//               Initial use: derived channels (pseudo-altitude) for Google Earth via KMLPlugIn
// Eric Friets
// 9/12/06
// for INDScan

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.RBNBProcess;

import java.util.Hashtable;

public class AppendPlugIn {

//RBNB connections
private String address = "localhost:3333";
private String sinkName = "appendSink";
private String pluginName = "Append";
private Sink sink = null;
private PlugIn plugin = null;

public static void main(String[] args) {
    (new AppendPlugIn(args)).exec();
}

//constructor
public AppendPlugIn(String[] args) {
    
    //parse args
    try {
	ArgHandler ah = new ArgHandler(args);
	
	// 'h' Help
	if (ah.checkFlag('h')) {
	    System.err.println("AppendPlugIn command line options");
	    System.err.println("   -a <RBNB address>");
	    System.err.println("       default: localhost:3333");
	    System.err.println("   -h (display this help message)");
	    System.err.println("   -n <PlugIn name>");
	    System.err.println("       default: Append");
	}
	
	// 'a' - RBNB server address
	if (ah.checkFlag('a')) {
	    String addressL = ah.getOption('a');
	    if (addressL != null) {
		address=addressL;
	    } else {
		System.err.println(
		    "WARNING: Null argument to the \"-a\"" +
		    " command line option.");
	    }
	}
	
	// 'n' PlugIn name
	if (ah.checkFlag('n')) {
	    String name = ah.getOption('n');
	    if (name != null) {
		pluginName = name;
	    } else {
		System.err.println(
		    "WARNING: Null argument to the \"-n\"" +
		    " command line option.");
	    }
	}
    } catch (Exception e) {
	System.err.println("AppendPlugIn argument exception "+e.getMessage());
	e.printStackTrace();
	RBNBProcess.exit(0);
    }
} //end constructor

//main execution loop
// creates plugin and sink connections, handles picm requests
public void exec() {
    
    //open connections
    try {
	sink = new Sink();
	sink.OpenRBNBConnection(address,sinkName);
	plugin = new PlugIn();
	plugin.OpenRBNBConnection(address,pluginName);
    } catch (Exception e) {
	System.err.println("Exception opening RBNB connections, aborting.");
	e.printStackTrace();
	RBNBProcess.exit(0);
    }
    
    //warn user if plugin name is different
    // (probably means another version of AppendPlugIn is running...)
    if (!plugin.GetClientName().equals(pluginName)) {
	pluginName = plugin.GetClientName();
	System.err.println(
	    "WARNING: The actual PlugIn name is " +
	    pluginName);
    }
    
    //loop handling requests
    while (true) {
	PlugInChannelMap picm = null;
	try {
	picm = plugin.Fetch(3000);

	if ((picm.GetIfFetchTimedOut())||
	    (picm.NumberOfChannels()==0)) continue;
	
	//if generic registration request, just return
	if (picm.GetRequestReference().equals("registration") &&
	(picm.GetName(0).equals("*")||picm.GetName(0).equals("..."))) {
	    plugin.Flush(picm);
	    continue;
	}
// System.err.println("\npicm "+picm);	
	//pull out channel to append, if any
	String achan = null;
	String[] message = null;

	if (picm.GetType(0)==ChannelMap.TYPE_STRING) {
	    message = picm.GetDataAsString(0);
// System.err.println("picm(0) contained message "+message[0]);
	    if (message!=null && message[0].trim().length()>0) {
		message[0]=message[0].trim();
		char[] term = {'&'};
		KeyValueHash kvh=new KeyValueHash(message[0],term);
		achan=kvh.get("append");
// System.err.println("message contained append="+achan);
	    }
	}
	
	//make request
	ChannelMap cm = new ChannelMap();
	for (int i=0;i<picm.NumberOfChannels();i++) cm.Add(picm.GetName(i));
	if (achan!=null) cm.Add(achan);
// System.err.println("created cm "+cm);
	if (message!=null && message[0].length()>0) for (int i=0;i<cm.NumberOfChannels();i++) cm.PutDataAsString(i,message[0]);
// System.err.println("making request with cm "+cm);
	sink.Request(cm,picm.GetRequestStart(),picm.GetRequestDuration(),picm.GetRequestReference());
	picm.Clear();
	cm=sink.Fetch(60000);
// System.err.println("return ChannelMap from fetch:\n" + cm);
	if (cm.GetIfFetchTimedOut()) {
	    System.err.println("timed out making request, returning no data");
	    plugin.Flush(picm);
	    continue;
	}
	for (int i=0;i<cm.NumberOfChannels();i++) {
	    picm.Add(cm.GetName(i));
	    picm.PutTimeRef(cm,i);
	    picm.PutDataRef(i,cm,i);
	}
	plugin.Flush(picm);
	} catch (Exception e) {
	    System.err.println("RBNB exception; returning no data; restarting plugin and sink");
	    e.printStackTrace();
	    try {
		if (picm!=null) {
		    picm.Clear();
		    plugin.Flush(picm);
		}
		sink.CloseRBNBConnection();
		sink.OpenRBNBConnection(address,sinkName);
		plugin.CloseRBNBConnection();
		plugin.OpenRBNBConnection(address,pluginName);
	    } catch (Exception e2) {
		System.err.println("RBNB exception; unable to establish connections; aborting");
		e2.printStackTrace();
		break;
	    }
	}
    } //end while
} //end method exec

} //end class AppendPlugIn
