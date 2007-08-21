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

// BRPlugIn - receives requests and provides responses to server
// Eric Friets
// January 2002
// Copyright 2002, 2004 Creare Incorporated

package com.rbnb.bufferedRoute;

import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.RBNBProcess;

public class BRPlugIn {
  private int debug=0;
  private String plugInName=null;
  private PlugIn plugin=null;

  public BRPlugIn(String serverAddress,String piName,String[] inputs,int debugI) {
    debug=debugI;
    plugInName=piName;
    //connect to server as PlugIn
    try {
      plugin=new PlugIn();
      plugin.OpenRBNBConnection(serverAddress,plugInName);
      if (debug>2) System.err.println("BRPlugIn plugin: "+plugin);
      //if inputs, get channels and register them
      if (inputs.length>0) {
        PlugInChannelMap picm=new PlugInChannelMap();
        for (int i=0;i<inputs.length;i++) {
          if (!inputs[i].endsWith("/...")) inputs[i]=inputs[i]+"/...";

	  /* INB 06/29/2004 - plugin.GetChannelList is deprecated.
          String[] chans=plugin.GetChannelList(inputs[i]);
	  */

	  // To get the channel list, we need a sink.
	  Sink sink = new Sink();
	  sink.OpenRBNBConnection(serverAddress,"_Sink." + plugInName);
	  ChannelMap reqMap = new ChannelMap();
	  reqMap.Add(inputs[i]);
	  sink.RequestRegistration(reqMap);
	  ChannelMap ansMap = plugin.Fetch(-1);
	  sink.CloseRBNBConnection();

          String[] chans = ansMap.GetChannelList();
          for (int j=0;j<chans.length;j++) {
            System.err.println("BRPlugIn adding "+chans[j]);
            picm.Add(chans[j]);
          }
        }
        plugin.Register(picm); //register channels

        if (debug>2) System.err.println("BRPlugIn: registering "+picm);
      }
    } catch (Exception e) {
      System.err.println("Exception creating plugin: "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(0);
    }
    if (debug>0) System.err.println("Started BRPlugIn...");
  }

  public PlugInChannelMap getRequest() {
    //get request
    try {
      // below just for testing
//System.err.print("waiting for request...");
      PlugInChannelMap picm=plugin.Fetch(-1);
//System.err.println("received.");
      if (debug>1) System.err.println("BRPlugIn.getRequest picm "+picm);
      return picm;
    } catch (Exception e) {
      System.err.println("Exception creating DataRequest: "+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  public void sendResponse(PlugInChannelMap picm) {
    try {
      //write response
      if (debug>1) System.err.println("BRPlugIn.sendResponse picm "+picm);
      plugin.Flush(picm);
    } catch (Exception e) {
      System.err.println("BRPlugIn.sendResponse exception: "+e.getMessage());
      e.printStackTrace();
    }
  }

  public void close() {
    //close plugin connection
    plugin.CloseRBNBConnection();
  }

}//end class BRPlugIn

