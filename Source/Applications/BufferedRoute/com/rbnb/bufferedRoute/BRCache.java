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

// BRCache - stores full frames in RBNB, supports queries and additions
// Eric Friets
// January 2002
// Copyright 2002 Creare Incorporated

package com.rbnb.bufferedRoute;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.utility.RBNBProcess;

//import java.util.Hashtable;
import java.util.StringTokenizer;

public class BRCache {
  private int debug=0;
  private static String cacheName=new String("brCache");
  private Source source=null;
  //private double lastTime=0;
  //private Hashtable endTimes=new Hashtable();
  private BRSink sink=null;

  public BRCache(String serverAddress,boolean useArchive,int numCacheFrames,int numArchiveFrames,boolean newArchive,int debugI) {
    debug=debugI;
    //create source connection
    try {
      String mode="none";
      if (useArchive) {
        if (newArchive) mode="create";
        else mode="append";
      }
      source=new Source(numCacheFrames,mode,numArchiveFrames);
      source.OpenRBNBConnection(serverAddress,cacheName);
	  cacheName=source.GetClientName();
      if (debug>2) System.err.println("BRCache source: "+source);
    } catch (Exception e) {
      System.err.println("Exception creating cache: "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(0);
    }
    if (debug>0) System.err.println("Started BRCache...");
  }
  
  public void setSink(BRSink sinkI) {
	  sink=sinkI;
  }
	  

  public String getName() {
    try {
      return source.GetServerName()+"/"+source.GetClientName()+"/";
    } catch (Exception e) {
      System.err.println("BRCache.getName exception: "+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  //EMF 5/30/03: function moved to BRSink, reimplemented using
  //             registration requests instead of local Hashtable
  /*public Double[] getEndTimes(ChannelMap cm) {
    Double[] times=new Double[cm.NumberOfChannels()];
    for (int i=0;i<cm.NumberOfChannels();i++) {
      StringTokenizer st=new StringTokenizer(chanName,"/");
      if (st.countTokens()==2) { //must be local src/chan
        times[i]=(Double)endTimes.get(chanName);
      } else { //fully qualified name, prepend /
        times[i]=(Double)endTimes.get("/"+chanName);
      }
      if (debug>4) System.err.println("cache endtime "+times[i]);
    }
    return times;
  }
  */

  public void register(ChannelMap cm) {
    try {
      source.Register(cm);
    } catch (Exception e) {
      System.err.println("BRCache exception registering channels.");
      e.printStackTrace();
    }
  }

  //EMF 5/30/03: never clear cache, just don't write if older than
  //             what's there...
  public void put(ChannelMap cm) {
    try {
      if (debug>3) System.err.println("BRCache.put: cm "+cm);
      if (debug>3) for (int i=0;i<cm.NumberOfChannels();i++) {
        System.err.println(i+": "+cm.GetTimeStart(i)+" "+cm.GetData(i));
      }
	  
	  //check must be on per channel basis
	  ChannelMap cmCache=new ChannelMap();
	  Double[] lastTimeD=sink.getEndTimes(cm);
	  for (int i=0;i<cm.NumberOfChannels();i++) {
		  double thisTime=cm.GetTimeStart(i);
		  if (lastTimeD[i]==null || lastTimeD[i].doubleValue()<thisTime) {
			  int idx=cmCache.Add(cm.GetName(i));
			  cmCache.PutTimeRef(cm,i);
			  cmCache.PutDataRef(idx,cm,i);
		  }
	  }
	  source.Flush(cmCache);

	  /*
      //if time goes backwards, reset ring buffer
      double thisTime=Double.MAX_VALUE;
      for (int i=0;i<cm.NumberOfChannels();i++) {
        double time=cm.GetTimeStart(i)+cm.GetTimeDuration(i);
        if (time<thisTime) thisTime=time;
      }
      if (thisTime<lastTime) { //reset ring buffer and clear endTimes
        if (debug>3) System.err.println("BRCache.put: reset ring buffer");
        source.SetRingBuffer(1000,"none",0);
        endTimes.clear();
      }
      if (thisTime<Double.MAX_VALUE) lastTime=thisTime;

      //put end times into cache
      for (int i=0;i<cm.NumberOfChannels();i++) {
        endTimes.put(cm.GetName(i),new Double(cm.GetTimeStart(i)+cm.GetTimeDuration(i)));
      }

      //put data into server
      source.Flush(cm);
	  */

    } catch (Exception e) {
      System.err.println("BRCache.put exception: "+e.getMessage());
      e.printStackTrace();
    }
  }

  public void close() {
    //close connection, save archive
    try {
      source.CloseRBNBConnection();
    } catch (Exception e) {
      System.err.println("Exception closing cache: "+e.getMessage());
      e.printStackTrace();
    }
  }

}//end class BRCache

