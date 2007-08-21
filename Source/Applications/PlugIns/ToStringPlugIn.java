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

// ToStringPlugIn - converts binary and primitive data to strings
// EMF
// May 2003
// Copyright 2003 Creare Incorporated

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler; //for argument parsing
import com.rbnb.utility.RBNBProcess; //alternative to System.exit, so
                                       //don't bring down servlet engine


public class ToStringPlugIn {
  private String address=new String("localhost:3333"); //server to connect to
  private String sinkName=new String("TSsink"); //sink connection name
  private String pluginName=new String("ToString"); //plugin connection name
  private PlugIn plugin=null; //plugin connection
  private Sink sink=null; //sink connection
  private int debug=0;

  public ToStringPlugIn(String[] args) {
    //parse args
    try {
      ArgHandler ah=new ArgHandler(args);
      if (ah.checkFlag('a')) {
        String addressL=ah.getOption('a');
        if (addressL!=null) address=addressL;
      }
      if (ah.checkFlag('n')) {
        String name=ah.getOption('n');
        if (name!=null) pluginName=name;
      }
    } catch (Exception e) {
      System.err.println("ToStringPlugIn argument exception "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(0);
    }
  }

  public static void main(String[] args) {
    (new ToStringPlugIn(args)).exec();
  }

  public void exec() {
    try {
      //make connections
      sink=new Sink();
      sink.OpenRBNBConnection(address,sinkName);
      plugin=new PlugIn();
      plugin.OpenRBNBConnection(address,pluginName);

      //loop handling requests - note multithreaded would be better
      //                       - does not support monitor/subscribe yet

	//process is to wait for request, get data from sink, convert data, send response, repeat
      while (true) {
        PlugInChannelMap picm=plugin.Fetch(-1); //block until request arrives

		//do not answer general registration requests, only registration requests for particular channels
		if (!(picm.GetRequestReference().equals("registration")
				&&picm.NumberOfChannels()==1
				&&(picm.GetName(0).equals("..."))||picm.GetName(0).equals("*"))) {
			//map channels to remote source 
			ChannelMap cm=new ChannelMap();
			for (int i=0;i<picm.NumberOfChannels();i++) {
			  cm.Add(picm.GetName(i));
			}
	
			//request data from remote source
			sink.Request(cm,
						 picm.GetRequestStart(),
						 picm.GetRequestDuration(),
						 picm.GetRequestReference());
			cm=sink.Fetch(-1,cm);

			//copy data,times,types to answer original request,
			//converting to strings
			String[] folder=cm.GetFolderList();
			for (int i=0;i<folder.length;i++) {
				int idx=picm.GetIndex(folder[i]);
				if (idx==-1) picm.AddFolder(folder[i]);
			}
			for (int i=0;i<cm.NumberOfChannels();i++) {
				String chan=cm.GetName(i);
				int idx=picm.GetIndex(chan);
				if (idx==-1) idx=picm.Add(chan);
				// if registration, may need to make new xml metadata for RBNBdav
//	if (picm.GetRequestReference().equals("registration")) System.err.println("ToStringPlugIn, got registration request, Mime: "+cm.GetMime(i)+", Type: "+cm.GetType(i)+", Data: "+new String(cm.GetData(i)));		// MJM DEBUG

				if (picm.GetRequestReference().equals("registration")
					) {		// just send generic header to avoid problems MJM 8/9/07
//					&& cm.GetMime(i).equals("text/xml") &&
//					&& "text/xml".equals(cm.GetMime(i)) &&     	// MJM reverse to avoid potential null-ptr
//					&& cm.GetType(i)==ChannelMap.TYPE_STRING) {
					String result=
							"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
							+"<!DOCTYPE rbnb>\n"
							+"<rbnb>\n"
							+"\t\t<size>"+1+"</size>\n"
  							+"\t\t<mime>text/plain</mime>\n"
							+"</rbnb>\n";
					picm.PutDataAsString(idx,result);
					picm.PutMime(idx,"text/xml");
				} else {
					switch (cm.GetType(i)) {
						case (ChannelMap.TYPE_STRING):
							//already string, just copy it
							picm.PutTimeRef(cm,i);
							picm.PutDataRef(idx,cm,i);
							break;
						case (ChannelMap.TYPE_BYTEARRAY):
							//just note MIME type in data...
							picm.PutTime(cm.GetTimeStart(i),cm.GetTimeDuration(i));
							picm.PutDataAsString(idx,cm.GetMime(i)+"\n");
							picm.PutMime(idx,"text/plain");
							break;
						case (ChannelMap.TYPE_INT8):
							double[] timeb=cm.GetTimes(i);
							byte[] datab=cm.GetDataAsInt8(i);
							for (int j=0;j<datab.length;j++) {
								picm.PutTime(timeb[j],0);
								picm.PutDataAsString(idx,Byte.toString(datab[j])+"\n");
							}
							picm.PutMime(idx,"text/plain");
							break;
						case (ChannelMap.TYPE_INT16):
							double[] times=cm.GetTimes(i);
							short[] datas=cm.GetDataAsInt16(i);
							for (int j=0;j<datas.length;j++) {
								picm.PutTime(times[j],0);
								picm.PutDataAsString(idx,Short.toString(datas[j])+"\n");
							}
							picm.PutMime(idx,"text/plain");
							break;
						case (ChannelMap.TYPE_INT32):
							double[] timei=cm.GetTimes(i);
							int[] datai=cm.GetDataAsInt32(i);
							for (int j=0;j<datai.length;j++) {
								picm.PutTime(timei[j],0);
								picm.PutDataAsString(idx,Integer.toString(datai[j])+"\n");
							}
							picm.PutMime(idx,"text/plain");
							break;
						case (ChannelMap.TYPE_INT64):
							double[] timel=cm.GetTimes(i);
							long[] datal=cm.GetDataAsInt64(i);
							for (int j=0;j<datal.length;j++) {
								picm.PutTime(timel[j],0);
								picm.PutDataAsString(idx,Long.toString(datal[j])+"\n");
							}
							picm.PutMime(idx,"text/plain");
							break;
						case (ChannelMap.TYPE_FLOAT32):
							double[] timef=cm.GetTimes(i);
							float[] dataf=cm.GetDataAsFloat32(i);
							for (int j=0;j<dataf.length;j++) {
								picm.PutTime(timef[j],0);
								picm.PutDataAsString(idx,Float.toString(dataf[j])+"\n");
							}
							picm.PutMime(idx,"text/plain");
							break;
						case (ChannelMap.TYPE_FLOAT64):
							double[] timed=cm.GetTimes(i);
							double[] datad=cm.GetDataAsFloat64(i);
							for (int j=0;j<datad.length;j++) {
								picm.PutTime(timed[j],0);
								picm.PutDataAsString(idx,Double.toString(datad[j])+"\n");
							}
							picm.PutMime(idx,"text/plain");
							break;
						default:
							picm.PutTime(cm.GetTimeStart(i),cm.GetTimeDuration(i));
							picm.PutDataAsString(idx,"unknown type\n");
							picm.PutMime(idx,"text/plain");
							break;
					} //end switch(type)
				}
			}
		}
        //send response
        plugin.Flush(picm);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//end exec method

}//end ToString class

