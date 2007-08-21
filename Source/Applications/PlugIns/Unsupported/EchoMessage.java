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

// EchoMessage - receives messages, and prints them to the console
//             - example code illustrating messaging; no real use
//             - not robust: needs error checking, reality checks, etc
// EMF
// December 2003
// Copyright 2003 Creare Incorporated

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;

public class EchoMessage {

	public EchoMessage() {}

	public static void main(String[] args) {
		(new EchoMessage()).exec();
	} //end method main

	// loop handling messages (requests), echoing them to the console
	public void exec() {
		String rbnbServer="localhost:3333";
		String plugInName="EchoMessage";
		PlugIn plugin=null; //plugin connection
		try {
			//make rbnb connection
			plugin=new PlugIn();
			plugin.OpenRBNBConnection(rbnbServer,plugInName);

			//register channel, so will never receive registration requests
			ChannelMap cm=new ChannelMap();
			cm.Add("text");
			plugin.Register(cm);
			
			//loop handling requests  - note ignores start, duration, reference, using only
			//                          contents of request (message)
			while (true) {
				PlugInChannelMap picm=plugin.Fetch(-1); //block until request arrives
				picm.PutTime((double)System.currentTimeMillis(),0);
				//extract message, silently ignore nontext messages
				if (picm.GetType(0)==ChannelMap.TYPE_STRING) {
					String message=picm.GetDataAsString(0)[0];
					System.err.println("EchoMessage received message:");
					System.err.println(message);
					System.err.println();
					String response="Message Received.";
					picm.PutMime(0,"text/plain");
					picm.PutDataAsString(0,response);
				}
				plugin.Flush(picm);
			}

		} catch (Exception e) {
			System.err.println(plugInName+" exception.  Aborting.");
			e.printStackTrace();
		}
  }//end exec method
  
}//end EchoMessage class

