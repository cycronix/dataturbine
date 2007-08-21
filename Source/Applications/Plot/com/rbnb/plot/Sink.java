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

// Sink - wrapper for com.rbnb.sapi.Sink that implements some methods
//        in COM.Creare.RBNB.API.Connection

// 10/07/2002  INB  Save the server address.

// 04/16/2002  WHF  Removed infinite timeout in call to 
//						com.rbnb.sapi.Sink.Fetch().
// 02/07/2005  EMF  Added local variable lastMap to store
//                      most recent data fetch.  It is used
//                      by ExportData to avoid recreating one
//                      from the V2/V1 converted structures.  Yuch.

package com.rbnb.plot;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelMap;

public class Sink {

private com.rbnb.sapi.Sink sink=null;
private boolean connected=false;
private String saveaddress=null;
private ChannelMap lastMap=null;

public Sink() {
  sink=new com.rbnb.sapi.Sink();
  //System.err.println("Sink: "+sink);
}

public Map getTimeLimits(Map inMap) {
  if (!connected) return inMap;
  //EMF 12/23/03 - if limits are not returned, ask for oldest/newest
  // nonlimits are -maxdbl start, zero duration
  try {
    //create channelmap, copy channels into it
    ChannelMap cm=new ChannelMap();
    Channel[] chan=inMap.channelList();
    for (int i=0;i<chan.length;i++) {
      cm.Add("/"+chan[i].getChannelName());
      chan[i].clear(); //clear data from channel
    }

    sink.RequestRegistration(cm);
    ChannelMap regMap=sink.Fetch(2000); //wait at most two seconds for an answer
    ChannelMap cm2=null;
    for (int i=0;i<regMap.NumberOfChannels();i++) {
//System.err.println(regMap.GetName(i)+" times: "+regMap.GetTimeStart(i)+" "+regMap.GetTimeDuration(i));
      if (regMap.GetTimeDuration(i)==0) { //no time limits; add to map
//System.err.println("no time limits on "+regMap.GetName(i));
        if (cm2==null) cm2=new ChannelMap();
        cm2.Add(regMap.GetName(i));
      } else { //copy data to inMap
        double[] times=new double[2];
        times[0]=regMap.GetTimeStart(i);
        times[1]=times[0]+regMap.GetTimeDuration(i);
        DataTimeStamps dt=new DataTimeStamps(times);
        Channel ch=inMap.findChannel(regMap.GetName(i).substring(1));
//System.err.println("Sink found channel "+ch+": "+times[0]+" "+times[1]);
        if (ch!=null) ch.setTimeStamp(dt);
      }
    }

    //if cm2 is nonempty, go find newest and oldest times
    if (cm2!=null) {
      sink.Request(cm2,0,0,"newest");
      ChannelMap newmap=sink.Fetch(2000);
      sink.Request(cm2,0,0,"oldest");
      ChannelMap oldmap=sink.Fetch(2000);
      for (int i=0;i<oldmap.NumberOfChannels();i++) {
        Channel ch=inMap.findChannel(oldmap.GetName(i).substring(1));
        double[] times=new double[2];
        times[0]=oldmap.GetTimeStart(i);
        int chnum=newmap.GetIndex(oldmap.GetName(i));
        if (chnum>=0) {
          times[1]=newmap.GetTimeStart(chnum)+newmap.GetTimeDuration(chnum);
          if (ch!=null) ch.setTimeStamp(new DataTimeStamps(times));
//System.err.println("fetched limits for "+oldmap.GetName(i)+": "+times[0]+" "+times[1]);
        }
      }
    }

    
  } catch (Exception e) {
    System.err.println("Exception in Sink.getTimeLimits");
    e.printStackTrace();
  }
  return inMap;
}

//EMF 9/7/05: throw exception, so connection failure propagates
public void OpenRBNBConnection() throws Exception {
  //try {
    saveaddress = "localhost:3333";
    sink.OpenRBNBConnection();
    connected=true;
  //} catch (SAPIException se) {
    //se.printStackTrace();
  //}
}

//EMF 9/7/05: throw exception, so connection failure propagates
public void OpenRBNBConnection(String address, String client) throws Exception {
  //try {
    saveaddress = address;
    sink.OpenRBNBConnection(address,client);
    connected=true;
  //} catch (SAPIException se) {
    //se.printStackTrace();
  //}
}

public void OpenRBNBConnection(String address, String client, String user, String pw) {
  try {
    saveaddress = address;
    sink.OpenRBNBConnection(address,client,user,pw);
    connected=true;
  } catch (SAPIException se) {
    se.printStackTrace();
  }
}

public Object isActive() {
  if (connected) return this;
  else return null;
}

public void getInformation(Map mapIO) {
  //put info into channels in map
}

public void terminateRBNB() {
  if (sink != null) {
    try {
      com.rbnb.api.Server server = com.rbnb.api.Server.newServerHandle
	(null,
	 saveaddress);
      server.stop();
    } catch (Exception e) {
    }
  }
}

public void disconnect(boolean that, boolean otherThing) {
    // INB 12/13/2001 - eliminated SAPIException handling.
    sink.CloseRBNBConnection();
}

//EMF 8/8/05: make 3d return array, adding mime
//EMF 3/25/05: make 2d return array, adding userdata
public String[][] getChannelList(String match) {
  try {
    // EMF 5/16/02: use new RequestRegistration method
    ChannelMap cm=new ChannelMap();
//    cm.Add(sink.GetServerName()+"/...");
    cm.Add(sink.GetServerName()+"/*/...");	// mjm 7/16/04. get plugin/route down a level
    sink.RequestRegistration(cm);
    cm=sink.Fetch(-1);
	String[][] chans=new String[3][];
	chans[0]=cm.GetChannelList();
	chans[1]=new String[chans[0].length];
	for (int i=0;i<chans[0].length;i++) {
		chans[1][i]=cm.GetUserInfo(i);
	}
        //EMF 8/8/05: add mime
        chans[2]=new String[chans[0].length];
        for (int i=0;i<chans[0].length;i++) {
            if (cm.GetType(i)==ChannelMap.TYPE_STRING) {
		String xmldat=cm.GetDataAsString(i)[0];
                if (xmldat!=null && xmldat.length()>0) {
			//parse mime field out of xml
			int i1=xmldat.indexOf("<mime>");
			int i2=xmldat.indexOf("</mime>");
                        if (i1>-1 && i2>-1) chans[2][i]=xmldat.substring(i1+6,i2);
		}
            }
	}
	return chans;
    //return cm.GetChannelList();
    // INB 11/05/2001 - use absolute path names and then eliminate the extra slash.
    //String[] chanList=sink.GetChannelList("/...");
/*
//EMF test code only
    String[] chanListPlus=null;
    if (chanList!=null) {
      chanListPlus=new String[chanList.length+1];
      chanListPlus[0]="/parent/RmapSource/c0";
      System.err.println("added /parent/RmapSource/c0");
      for (int i=0;i<chanList.length;i++) {
        chanListPlus[i+1]=chanList[i];
      }
    } else {
      chanListPlus=new String[1];
      chanListPlus[0]="/parent/RmapSource/c0";
      System.err.println("added /parent/RmapSource/c0");
    }
    chanList=chanListPlus;
//end EMF test code
*/
    //System.err.println("Sink.getChannelList: "+chanList.length+" channels");
    //return chanList;
  } catch (SAPIException se) {
    se.printStackTrace();
    return null;
  }
}

public void setSinkMode(String mode) {
}

public void setReadTimeOut(Time timeOut) {
}

public void streamSetMap(Map map,Time start,Time duration,int flags) {
}

public Map streamGetMap() {
  return null;
}

public void synchronizeSink() {
}

public Map getData(Map m,Time s, Time d, int f) {
  return getMap(m,s,d,f);
}

// 10/18/2004  EMF  Added byte array support.
// 04/19/2002  WHF  Added int8 support.
public Map getMap(Map map,Time startT,Time durationT,int flags) {
  ChannelMap cm=null;
  int retVal=0;
//  long begin=System.currentTimeMillis();
  int numChan=0;
  String timeRef=new String("absolute");
  double start=0;
  double duration=0;
  if (startT!=null) start=startT.getDoubleValue();
  if (durationT!=null) duration=durationT.getDoubleValue();
  
  //make new channel map
  cm=new ChannelMap();

  //add new channels
  Channel[] chan = map.channelList();
  for (int i=0;i<chan.length;i++) {
    try {
      cm.Add("/" + chan[i].getChannelName());
      //clear any existing channel data from map
      chan[i].clear();
    } catch (Exception e) {
      System.err.println("com.rbnb.plot.Sink.getMap: exception ");
      e.printStackTrace();
    }
  }

  //determine appropriate timeRef
  if ((flags&DataRequest.newest) == DataRequest.newest) {
    timeRef=new String("newest");
    //start=-1*duration;
    start=0;
  } else if ((flags&DataRequest.oldest) == DataRequest.oldest) {
    timeRef=new String("oldest");
    start=0;
  }

  //get data from server
//long requestBegin=System.currentTimeMillis();
  try {
//System.err.println("request start "+start+", duration "+duration+", timeRef"+timeRef);
    sink.Request(cm,start,duration,timeRef);
	
	// 04/16/2002  WHF  Should use timeout instead of blocking thread forever:
    // 10/02/2002  INB	1 second is not long enough.  MJM suggests a 60 second
    // timeout until we can think about this.
//    cm=sink.Fetch(-1);
//    cm=sink.Fetch(1000);
    //cm=sink.Fetch(60000);
	//EMF 7/22/04:  need to handle the timeout, or things get bluey
	  cm=sink.Fetch(60000);
	  //EMF 2/7/05: save copy for possible export
	  lastMap=cm;
	  if (cm.GetIfFetchTimedOut()) 
		  System.err.println("***********rbnbPlot fetch timed out*************");

    numChan=cm.NumberOfChannels();
  } catch (SAPIException se) {
    se.printStackTrace();
  }
//long requestEnd=System.currentTimeMillis();
  //put the data into Channels and Map
  for (int i=0;i<=numChan-1;i++) {
    boolean getTimeStamp=true;
    Channel ch=map.findChannel(cm.GetName(i));
    if (ch!=null) {
      switch (cm.GetType(i)) {
	  case ChannelMap.TYPE_BYTEARRAY:
	    // 2004/12/23  WHF  Also include mime type, which may be useful.
		ch.setDataByteArray(cm.GetDataAsByteArray(i), cm.GetMime(i));
		break;
	  // EMF 6/29/05: add support for string data
	  case ChannelMap.TYPE_STRING:
	  	ch.setDataString(cm.GetDataAsString(i),cm.GetMime(i));
		break;
	  case ChannelMap.TYPE_INT8:
		ch.setDataInt8(cm.GetDataAsInt8(i));
		break;
        case ChannelMap.TYPE_INT16:
          ch.setDataInt16(cm.GetDataAsInt16(i));
          break;
        case ChannelMap.TYPE_INT32:
          ch.setDataInt32(cm.GetDataAsInt32(i));
          break;
        case ChannelMap.TYPE_INT64:
          ch.setDataInt64(cm.GetDataAsInt64(i));
          break;
        case ChannelMap.TYPE_FLOAT32:
          ch.setDataFloat32(cm.GetDataAsFloat32(i));
          break;
        case ChannelMap.TYPE_FLOAT64:
          ch.setDataFloat64(cm.GetDataAsFloat64(i));
          break;
        default:
          //System.err.println("Sink.getMap: no data in channel");
          //System.err.println("Sink.getMap: unknown data type: "+sink.ChannelType(i));
          //map.removeChannel(ch);
          for (int j=0;j<chan.length;j++) { //clear the channels
            chan[j].clear();
          }
          getTimeStamp=false;
          break;
      }
      if (getTimeStamp) {
        DataTimeStamps ts=new DataTimeStamps(cm.GetTimes(i));
        ch.setTimeStamp(ts);
      }
    }
  }

//long end=System.currentTimeMillis();
//System.err.println("Sink.getMap: setup   "+(requestBegin-begin));
//System.err.println("             request "+(requestEnd-requestBegin));
//System.err.println("             parse   "+(end-requestEnd));
//System.err.println("             total   "+(end-begin));
  //return the map
  return map;
}

//EMF 2/7/05: new method to access most recently fetched data without
//            requiring a reconversion from V1 to V2 data structures
public ChannelMap getLastMap() {
	return lastMap;
}

}

