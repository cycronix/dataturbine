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

import com.rbnb.sapi.*;

public class HelloWorld
{
    static String inStr="Hello World!";
    public static void main(String[] args)
    {
	try {
	    // Create both a source and a sink, connect both:
	    Source source=new Source();
	    source.OpenRBNBConnection("localhost:3333","HelloWorld");
	    Sink sink=new Sink();
	    sink.OpenRBNBConnection();
		
	    // Push data onto the server:
	    System.out.println("Placing string \""+inStr+"\" into server.");
	    ChannelMap sMap = new ChannelMap();
	    sMap.Add("HelloWorld");
	    sMap.PutTimeAuto("timeofday");
	    sMap.PutDataAsString(0,inStr);
	    source.Flush(sMap);

	    // Pull data from the server:
	    ChannelMap rMap = new ChannelMap();
	    rMap.Add("HelloWorld/HelloWorld");
	    sink.Request(rMap,-10.0,20.0,"newest");

	    ChannelMap aMap;
	    if ((aMap = sink.Fetch(-1)) == null) 
		{
		    System.err.println("Data not received!");
		    return;
		}
	    System.out.println("Retrieved \""
			       +aMap.GetDataAsString(0)[0]
				   +"\" from server.");
	    source.CloseRBNBConnection();
	    sink.CloseRBNBConnection();
	} catch (SAPIException se) { se.printStackTrace(); }
    }
}


	
