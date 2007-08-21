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


// VChansPlugIn2 - fulfills requests for data by querying the remote channels
//    JPW 03/17/2006: A break from VChansPlugIn; see my notes below

// EMF
// February 2002
// Copyright 2002, 2004 Creare Incorporated
// Modified: INB 04/07/2004 - replaced the Request method with one that doesn't
//                            take the byFrame flag (it is no longer supported)
// Modified: JPW 03/17/2006 -
//                 1. In order to be able to receive registration requests,
//                    don't register channels.  Alternatively, could at some
//                    point add code to register the chans but also have a
//                    sleepy loop to update the chan's registration info.
//                 2. Support requests for "*" and "..." by making a request
//                    for all channels that have been mapped.

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;

import java.util.Enumeration;
import java.util.Hashtable;

public class VChansPlugIn2 {
  private String address=new String("localhost:3333"); //server to connect to
  private String sinkName=new String("VCsink"); //sink connection name
  private String pluginName=new String("VChans"); //plugin connection name
  private Hashtable toLocal=new Hashtable(); //for mapping virtual/real channels
  private Hashtable toRemote=new Hashtable();
  private PlugIn plugin=null; //plugin connection
  private Sink sink=null; //sink connection

  public VChansPlugIn2(String[] args) {
    //parse args (expecting optional -a address, followed by remote/local
    //            channel name pairs)
    for (int i=0;i<args.length;i++) {
      if (args[i].startsWith("-a")) {
        if (args[i].length()==2) address=args[++i];
        else address=args[i].substring(2);
      } else {
        String remote=args[i];
        String local=args[++i];
        System.err.println("mapping "+remote+" to "+local);
        // fill up Hashtables, so can easily map virtual to/from real channels
        toLocal.put(remote,local);
        toRemote.put(local,remote);
      }
    }
  }

  public static void main(String[] args) {
    (new VChansPlugIn2(args)).exec();
  }

  public void exec() {
    try {
      //make connections
      sink=new Sink();
      sink.OpenRBNBConnection(address,sinkName);
      plugin=new PlugIn();
      plugin.OpenRBNBConnection(address,pluginName);
      
      ChannelMap cm=new ChannelMap();
      
      // JPW 03/17/2006: Don't register channels; if we register, then
      //                 VChansPlugIn will never receive registration requests.
      //                 Could register these chans and then have a thread
      //                 with a sleepy loop to update the registration
      //                 information periodically.
      /*
      //register virtual channels
      for (Enumeration e=toLocal.elements();e.hasMoreElements();) {
        cm.Add((String)e.nextElement());
      }
      plugin.Register(cm);
      */
      
      //loop handling requests - note multithreaded would be better.
      //                       - does not support monitor/subscribe yet
      while (true) {
        PlugInChannelMap picm=plugin.Fetch(-1); //block until request arrives
	
        //map virtual to real channels
        cm=new ChannelMap();
        for (int i=0;i<picm.NumberOfChannels();i++) {
	    // JPW 03/17/2006: Support requests for "*" and "..." by making
	    //                 a request for all channels that have been mapped
	    if (picm.GetName(i).equals("*") || picm.GetName(i).equals("...")) {
	        for (Enumeration e = toRemote.elements(); e.hasMoreElements();) {
		    cm.Add((String)e.nextElement());
		}
	    } else {
		cm.Add((String)toRemote.get(picm.GetName(i)));
	    }
        }

        //request data from real channels
	/* INB - this method is no longer supported.  04/07/2004
        sink.Request(cm,
                     picm.GetRequestStart(),
                     picm.GetRequestDuration(),
                     picm.GetRequestReference(),
                     picm.IsRequestFrames());
	*/
        sink.Request(cm,
                     picm.GetRequestStart(),
                     picm.GetRequestDuration(),
                     picm.GetRequestReference());
        cm=sink.Fetch(20000,cm);   // JPW 03/17/2006: Put in 20 sec timeout
	if (cm.GetIfFetchTimedOut()) {
	    System.err.println(
	        "++ VChansPlugIn Time out on" + cm.GetName(0) );
	}
	
        //copy data,times,types from real to virtual channels
        for (int i=0;i<cm.NumberOfChannels();i++) {
	  String localChan = (String)toLocal.get(cm.GetName(i));
	  if ( (localChan == null) || (localChan.equals("")) ) {
	      // Just go to the next channel
	      continue;
	  }
          int idx = picm.GetIndex(localChan);
          if (idx == -1) {
	      // Channel does not exist in request, add it.
	      // This channel may not have been in the original request PICM
	      // because it is fulfilling a wildcard request
	      idx = picm.Add(localChan);
	  }
	  picm.PutTimeRef(cm,i);
	  //EMF 1/24/03: switched to new PutDataRef call to avoid
	  //             byte order ambiguity
	  //picm.PutData(idx,cm.GetData(i),cm.GetType(i));
	  picm.PutDataRef(idx,cm,i);
	  picm.PutMime(idx,cm.GetMime(i));
        }
        //send response
        plugin.Flush(picm);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//end exec method

}//end VChansPlugIn class

