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

// CacheInterface.java - handles DataTurbine server, source, and sink
// Eric Friets
// September 12, 2001
// Copyright 2001 Creare Incorporated

//modified 3/4/02 to use simple API instead of full API

// Starts a source and sink.  Provides simple get and put
// methods for threads handling individual HTTP requests.

package com.rbnb.webCache;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.utility.RBNBProcess;

public class CacheInterface {
  private static String headerSuffix=new String("header");

  private WebCache wc=null;
  private String serverName=new String("WebCache");
  private Source source=null;
  private String sourceName=new String("webSource");
  private Object sourceLock=new Object();
  private Sink sink=null;
  private String sinkName=new String("webSink");
  private Object sinkLock=new Object();
  private int debug=0;
  private boolean shutDown=false;

  //constructor
  public CacheInterface(WebCache wcI,String rbnbServer,boolean useArchive,int numCframes,int numAframes,boolean newArchive,int debugI) {

    wc=wcI;
    debug=debugI;
    //make DataTurbine connections
    try {
      //start sink
      sink=new Sink();
      sink.OpenRBNBConnection(rbnbServer,sinkName);
      sinkName=sink.GetClientName();
      serverName=sink.GetServerName();
      if (debug>0) System.err.println("Started sink "+sink);
      //start source
      String mode="append";
      if (newArchive) {
        mode="create";
      }
      if (numAframes==-1) numAframes=numCframes;
      source=new Source(numCframes,mode,numAframes);
      source.OpenRBNBConnection(rbnbServer,sourceName);
      sourceName=source.GetClientName();
      if (debug>0) System.err.println("Started source "+source);
    } catch (Exception e) {
      System.err.println("Exception creating DataTurbine connections: "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(1);
    }
  } //end constructor

  private void doShutdown() {
    shutDown=true;
    try {
      sink.CloseRBNBConnection();
    } catch (Exception e) {
      System.err.println("Exception shutting down RBNB sink: "+e.getMessage());
      e.printStackTrace();
    }
    try {
      source.CloseRBNBConnection();
    } catch (Exception e) {
      System.err.println("Exception shutting down RBNB source: "+e.getMessage());
      e.printStackTrace();
    }
  }

  public byte[] getHeader(String channel) {
    //return getData(channel+"/"+headerSuffix);
    //EMF 5/13/02: get header from metadata, not separate channel
    synchronized(sinkLock) {
      if (channel.endsWith("/")) channel=channel.substring(0,channel.length()-1);
      if (shutDown) return null;
      try {
        ChannelMap cm=new ChannelMap();
        cm.Add(sourceName+"/"+channel);
        sink.RequestRegistration(cm);
        cm=sink.Fetch(-1);
        if (cm.NumberOfChannels()==0) return null;
        else return cm.GetDataAsString(0)[0].getBytes();
      } catch (Exception e) {
        System.err.println();
        System.err.println("Exception reading from DataTurbine: "+e.getMessage());
        e.printStackTrace();
        wc.doShutdown(); //kill the webcache
        doShutdown();
        return null;
      }
    } //end synchronized block
  }
  
  public byte[] getData(String channel) {
      return getData(channel,0,0,"newest");
  }

  public byte[] getData(String channel, double start, double duration, String reference) {
    synchronized(sinkLock) {
      if (channel.endsWith("/")) channel=channel.substring(0,channel.length()-1);
      if (shutDown) return null;
      try {
        ChannelMap cm=new ChannelMap();
        cm.Add(sourceName+"/"+channel);
        //EMF 10/8/03: stop using deprecated method
	//sink.Request(cm,0,0,"newest",true);
        sink.Request(cm,start,duration,reference);
        cm=sink.Fetch(-1);
        if (cm.NumberOfChannels()==0) return null;
        else return cm.GetData(0);
      } catch (Exception e) {
        System.err.println();
        System.err.println("Exception reading from DataTurbine: "+e.getMessage());
        e.printStackTrace();
        wc.doShutdown(); //kill the webcache
        doShutdown();
        return null;
      }
    } //end synchronized block
  } //end getData method

  public void putData(String channel, byte[] header, byte[] data) {
    if ((data.length<1)||(header.length<1)) return;
    synchronized(sourceLock) {
      if (channel.endsWith("/")) channel=channel.substring(0,channel.length()-1);
      if (shutDown) return;
      try {
        ChannelMap cm=new ChannelMap();
        cm.PutTimeAuto("timeofday");
        cm.Add("/"+channel);
        cm.PutData(0,data,ChannelMap.TYPE_INT8);
        source.Flush(cm);
        //EMF 5/13/02: put header into metadata, not separate channel
        //cm.Add("/"+channel+"/"+headerSuffix);
        //cm.PutData(1,header,ChannelMap.TYPE_INT8);
        cm.PutData(0,header,ChannelMap.TYPE_STRING);
        cm.PutMime(0,"text/plain");
        source.Register(cm);
      } catch (Exception e) {
        System.err.println("Exception writing to DataTurbine: "+e.getMessage());
        e.printStackTrace();
        wc.doShutdown(); //kill the webcache
        doShutdown();
      }
    } //end synchronized block
  } //end getData method
        
} //end CacheInterface class
