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

// SignaturePlugIn - adds signature (password) to data.  Based on VSourcePlugIn
// MJM Nov 2004
// Copyright 2004 Creare Incorporated

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.ChannelTree.Node;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler; //for argument parsing
import com.rbnb.utility.RBNBProcess; //alt to System.exit: don't bring down servlet engine

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.security.*;
import java.util.Stack;

public class SignaturePlugIn implements Runnable {
    private Stack threadStack;
    private String signature=new String(""); // signature
    private String piAddress=new String("localhost:3333"); //server to connect to
    private String sAddress=new String("localhost:3333"); //server to connect to
    private String sinkName=new String("SigSink"); //sink connection name
    private String pluginName=new String("Signature"); //plugin connection name
    private PlugIn plugin=null; //plugin connection
    private Sink sink=null; //sink connection
    private int debug=0;
    private long timeout=60000; //timeout on requests, default 1 minute

    public static void main(String[] args) {

	SignaturePlugIn spi = new SignaturePlugIn();

	//parse args
	try {
	    ArgHandler ah=new ArgHandler(args);
	    
	    if (ah.checkFlag('a')) {
		String addressL=ah.getOption('a');
		if (addressL!=null) spi.setPiAddress(addressL);
	    }

	    if (ah.checkFlag('b')) {
		String addressL=ah.getOption('b');
		if (addressL!=null) spi.setSAddress(addressL);
	    } 

	    if (ah.checkFlag('t')) {
		long to=Long.parseLong(ah.getOption('t'));
		if (to>=-1) spi.setTimeout(to);
	    }        

	    if (ah.checkFlag('n')) {
		String name=ah.getOption('n');
		if (name!=null) spi.setPiName(name);
	    }
	    
	    if (ah.checkFlag('s')) {
		String name=ah.getOption('s');
		if (name!=null) spi.setSignature(name);
	    }
	    
	} catch (Exception e) {
	    System.err.println("SignaturePlugIn argument exception "+e.getMessage());
	    e.printStackTrace();
	    RBNBProcess.exit(0);
	}
	
	spi.run();
    }
    
//------------------------------------------------------------------------------------------------
    public SignaturePlugIn() {
	plugin=new PlugIn();
	threadStack = new Stack();
    }
    
//------------------------------------------------------------------------------------------------
// Access methods

    public void setPiAddress(String address) { this.piAddress=address; }    
    public void setSAddress(String address)  { this.sAddress=address; }    
    public void setTimeout(long to)          { this.timeout=to; }    
    public void setPiName(String name)       { this.pluginName=name; }    
    public void setSignature(String name)    { this.signature=name; }    

//------------------------------------------------------------------------------------------------
  public void run() {

      try {
	  plugin.OpenRBNBConnection(piAddress,pluginName);
	  
	  System.err.println("Signature pluginName: "+pluginName);
	  
	  //loop handling requests - does not support monitor/subscribe yet
	  while (true) {
	      PlugInChannelMap picm=plugin.Fetch(-1); //block until request arrives
	      //System.err.println(pluginName+": got picm: "+picm);
	      // pop an open sink thread from stack, see RoutingPI	      
	      AnswerRequest a;
	      if (threadStack.empty()) a = new AnswerRequest();
	      else                     a = (AnswerRequest) threadStack.pop();
	      a.setRequestMap(picm);
	      new Thread(a).start();
	  }
      } catch (Exception e) {
	  e.printStackTrace();
      }
  }

//------------------------------------------------------------------------------------------------
    private class AnswerRequest implements Runnable
//------------------------------------------------------------------------------------------------ 
    {
	private PlugInChannelMap picm;
	private Sink sink;
	private long lastConnect=System.currentTimeMillis();

	AnswerRequest()
	{
	    // open and maintain sink object/connection in threadStack
	    sink = new Sink();
	    try {
		//System.err.println("New Sink!");
		sink.OpenRBNBConnection(sAddress,sinkName);
	    } catch (Exception e) {	e.printStackTrace(); }		

	}
    
//------------------------------------------------------------------------------------------------
    // Access methods
    public void setRequestMap(PlugInChannelMap requestMap) { this.picm=requestMap; }

//------------------------------------------------------------------------------------------------
	public void run()
	{
	    try {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		
		//map channels to remote source 
		ChannelMap cm=new ChannelMap();
		for (int i=0; i<picm.NumberOfChannels(); i++) {
		    cm.Add(picm.GetName(i));
		}

		//map empty folders to remote source (for registration requests)
		String[] folder = picm.GetFolderList();
		for (int i=0; i<folder.length; i++) {
		    cm.AddFolder(folder[i]);
		}    
		
		//request data from remote source
		sink.Request(cm,
			     picm.GetRequestStart(),
			     picm.GetRequestDuration(),
			     picm.GetRequestReference());
		
		//cm=sink.Fetch(-1,cm);
		cm=sink.Fetch(timeout);
		if(cm.GetIfFetchTimedOut()) {
		    System.err.println("Signature Data Fetch Timed Out!");
		    picm.Clear();
		} 
		else {
		    md.reset();
		    
		    // copy data,times,types to answer original request		    
		    // folders aren't part of signature digest
		    folder=cm.GetFolderList();
		    for (int i=0; i<folder.length; i++) picm.AddFolder(folder[i]);

		    int sigIdx = -1;
		    for (int i=0;i<cm.NumberOfChannels();i++) {
			
			String chan=cm.GetName(i);
			//System.err.println(pluginName+": chan "+chan);
			
			if(chan.endsWith("/_signature")) {
			    //System.err.println(pluginName+": got sigchan "+i);
			    sigIdx = i;
			    continue;
			}
			
			int idx=picm.GetIndex(chan);   // if channel was not in request map, add it here
			if (idx==-1) idx=picm.Add(chan);
			
			picm.PutTimeRef(cm,i);
			picm.PutDataRef(idx,cm,i);
		    
			//System.err.println(pluginName+": adding chan "+i+" to md");
			md.update(cm.GetData(i));    // update message digest
			md.update((new Double(cm.GetTimeStart(i))).toString().getBytes());  // toss in timestamp
		    }
		
		    if(cm.NumberOfChannels() > 0) {  // no signature if no channels 
			byte[] amd = md.digest(signature.getBytes());    // finally, add in signature
			
			// implied checkMode if signature channel is present
			if(sigIdx >= 0) {
			    if(MessageDigest.isEqual(amd,cm.GetDataAsByteArray(sigIdx)[0])) {
				System.err.println(pluginName+": signature matched for: "+cm.GetName(0));
			    } 
			    else {
				System.err.println(pluginName+": failed signature test, sending null response");
				picm.Clear();
			    }
			}
			else {
			    System.err.println(pluginName+": _signature attached for: "+cm.GetName(0));
			    int idx = picm.Add("_signature");
			    picm.PutTime(0.,0.);   // null time [don't inherit last channel time(s)]
			    picm.PutDataAsByteArray(idx, amd);
			}
		    }
		}
		//send response
		plugin.Flush(picm);
	    } catch (Exception e) {
		e.printStackTrace();
	    }


	    if(threadStack.size() < 4) threadStack.push(this);      // push thread - includes open sink connection
	    else             	       sink.CloseRBNBConnection();

	} //end run method
	
    } // end inner class runnable
} //end SignaturePlugIn class
