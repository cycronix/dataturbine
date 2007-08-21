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

// AverageApp - computes average of input data, streams input
// EMF
// August 2004
// Copyright 2004 Creare Incorporated

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.ChannelTree.Node;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.utility.ArgHandler; //for argument parsing
import com.rbnb.utility.RBNBProcess; //alternative to System.exit, so
                                       //don't bring down servlet engine

public class AverageApp {
  private String address=new String("localhost:3333"); //server to connect to
  private String sinkName=new String("AveSink"); //sink connection name
  private String sourceName=new String("AverageApp"); //plugin connection name
  private Source source=null; //plugin connection
  private Sink sink=null; //sink connection
  private String remoteSource=null; //name of remote source
  private int debug=0;
  private long timeout=60000; //timeout on requests, default 1 minute

  public AverageApp(String[] args) {
    //parse args
    try {
      ArgHandler ah=new ArgHandler(args);
      if (ah.checkFlag('a')) {
        String addressL=ah.getOption('a');
        if (addressL!=null) address=addressL;
      }
      if (ah.checkFlag('s')) {
        String source=ah.getOption('s');
        if (source!=null) remoteSource=source;
        //channel name completion easier if always ends in /
        if (!remoteSource.endsWith("/")) remoteSource=remoteSource+"/";
      }
      if (ah.checkFlag('n')) {
        String name=ah.getOption('n');
        if (name!=null) sourceName=name;
      }
      if (ah.checkFlag('t')) {
        long to=Long.parseLong(ah.getOption('t'));
        if (to>=-1) timeout=to;
      }
        
    } catch (Exception e) {
      System.err.println("AverageApp argument exception "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(0);
    }
    //source must be specified
    if (remoteSource==null) {
      System.err.println("AverageApp: source must be specified with -s flag");
      RBNBProcess.exit(0);
    }
  }

  public static void main(String[] args) {
    (new AverageApp(args)).exec();
  }

  public void exec() {
    try {
      //make connections
      sink=new Sink();
      sink.OpenRBNBConnection(address,sinkName);
      source=new Source();
      source.OpenRBNBConnection(address,sourceName);

      //set up stream
      ChannelMap reqMap=new ChannelMap();
      reqMap.Add(remoteSource+"*");
      sink.Monitor(reqMap,0);
      //sink.Subscribe(reqMap,"newest");//,1.0);
      //loop handling data, reading stream and writing average
      while (true) {
        ChannelMap inMap=sink.Fetch(-1);
if (inMap.NumberOfChannels()==0) System.err.println(
"fetched empty channel map, probably end of stream");

        ChannelMap outMap=new ChannelMap();

        //map channels to source 
        for (int i=0;i<inMap.NumberOfChannels();i++) {
          String chan=inMap.GetName(i);
//System.err.println("VSource inMap chan "+chan);
          if (chan.startsWith(remoteSource)) {

            chan=chan.substring(remoteSource.length());
            int idx=outMap.Add(chan);
    
// average data, to demonstrate data reduction for OHM across ratty satellite link
            if (inMap.GetType(i)==ChannelMap.TYPE_FLOAT64) {
              double[] data=inMap.GetDataAsFloat64(i);
//System.err.println("data length "+data+" "+data.length);
              double[] sum=new double[1];
              for (int j=0;j<data.length;j++) sum[0]+=data[j];
              sum[0]/=data.length;
//System.err.println("average is "+sum[0]);
              outMap.PutTime(inMap.GetTimeStart(i),inMap.GetTimeDuration(i));
              outMap.PutDataAsFloat64(idx,sum);
            } else { //just copy data
              outMap.PutTimeRef(inMap,i);
              outMap.PutDataRef(idx,inMap,i);
            }
          } else {
            System.err.println("AverageApp: unexpected remote channel "+inMap.GetName(i));
          }
          //send response
System.err.println("AverageApp sent response "+outMap);
          source.Flush(outMap);
          outMap.Clear();
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//end exec method

}//end AveragePlugIn class

