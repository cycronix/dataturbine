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

// BRSink - handles sink connection to server; fetches data not found in cache
// Eric Friets
// January 2002
// Copyright 2002 Creare Incorporated

package com.rbnb.bufferedRoute;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.RBNBProcess;

import java.util.StringTokenizer;

public class BRSink {
  private String cacheName=null;
  private int debug=0;
  private static String sinkName=new String("brSink");
  private Sink sink=null;

  public BRSink(String serverAddress,String cacheNameI,int debugI) {
    cacheName=cacheNameI;
    debug=debugI;
    try {
      //create sink connection
      sink=new Sink();
      sink.OpenRBNBConnection(serverAddress,sinkName);
    } catch (Exception e) {
      System.err.println("Exception creating sink: "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(0);
    }
    if (debug>0) System.err.println("Started BRSink...");
  }
  
  //EMF 5/30/03: moved getEndTimes from cache to sink, no longer
  //             using Hashtable, instead make registration requests
  //             Change to ensure correct working with reloaded archives,
  //             which may be out of sync with Hashtable.
  public Double[] getEndTimes(ChannelMap cm) {
	  Double[] times=new Double[cm.NumberOfChannels()];
	  ChannelMap cmReg=new ChannelMap();
	  try {
		  for (int i=0;i<cm.NumberOfChannels();i++) {
			  cmReg.Add(cacheName+cm.GetName(i));
		  }
		  sink.RequestRegistration(cmReg);
		  cmReg=sink.Fetch(60000);
		  for (int i=0;i<cm.NumberOfChannels();i++) {
			  int idx=cmReg.GetIndex(cacheName+cm.GetName(i));
			  if (idx>=0) {
				  times[i]=new Double(cmReg.GetTimeStart(idx)+cmReg.GetTimeDuration(idx));
			  }
			  if (debug>2) System.err.println("time "+times[i]);
		  }
	  } catch (Exception e) {
		  System.err.println("BRSink exception: "+e.getMessage());
		  e.printStackTrace();
	  }
	  return times;
  }

  public ChannelMap checkCache(PlugInChannelMap picm,double start,double duration,String reference) {
    try {
      ChannelMap cm=new ChannelMap();
      for (int i=0;i<picm.NumberOfChannels();i++) {
        String chanName=picm.GetName(i);
        if (!(chanName.startsWith("...")||chanName.startsWith("*"))) {
          if (chanName.startsWith("/")) {
            chanName=chanName.substring(1);
          }
          cm.Add(cacheName+chanName);
        }
      }
      if (cm.NumberOfChannels()==0) return cm;
      sink.Request(cm,start,duration,reference);
      sink.Fetch(-1,cm); //probably should timeout, rather than block forever
      if (debug>2) System.err.println("BRSink.checkCache: cm "+cm);
      return cm;
    } catch (Exception e) {
      System.err.println("BRSink.checkCache exception: "+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  public ChannelMap checkSources(PlugInChannelMap picm,double start,double duration,String reference) {
    try {
      ChannelMap cm=new ChannelMap();
	  if (!(picm.GetRequestReference().equals("registration")
		&&picm.NumberOfChannels()==1
		&&(picm.GetName(0).equals("..."))||picm.GetName(0).equals("*"))) {

		  for (int i=0;i<picm.NumberOfChannels();i++) {
			String chanName=picm.GetName(i);
			if (!(chanName.startsWith("...")||chanName.startsWith("*"))) {
			  StringTokenizer st=new StringTokenizer(chanName,"/");
			  //if (st.countTokens()==2) { //must be local src/chan
				//cm.Add(chanName);
				//if (debug>3) System.err.println("BRSink.checkSources: added "+chanName);
			  //} else { //assume fully qualified name
				cm.Add("/"+chanName);
				if (debug>3) System.err.println("BRSink.checkSources: added "+"/"+chanName);
			  //}
			}
		  }
      }
      if (debug>3) System.err.println("BRSink.checkSources: making request:"+start+" "+duration+" "+reference); 
      if (cm.NumberOfChannels()==0) return cm;
      sink.Request(cm,start,duration,reference);
      cm=sink.Fetch(-1,cm);
      if (debug>2) System.err.println("BRSink.checkSources: cm "+cm);
	  if (debug>2) {
		  String[] folder = cm.GetFolderList();
		  System.err.println("BRSink.checkSources: cm folders "+folder);
		  for (int i=0;i<folder.length;i++) { 
			  System.err.println("\t"+folder[i]);
		  }
	  }
      return cm;
    } catch (Exception e) {
      System.err.println("BRSink.checkSources exception: "+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }
    
  public void close() {
    //close connection to server
    try {
      sink.CloseRBNBConnection();
    } catch (Exception e) {
      System.err.println("Exception closing sink: "+e.getMessage());
      e.printStackTrace();
    }
  }

}//end class BRSink
    
