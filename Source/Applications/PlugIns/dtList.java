
/*
Copyright 2012 Cycronix

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

/**
 * Plugin that returns plaintext list of RBNB channels
 *
 * @author Matt Miller
 *
 * @since V3.2
 * @version 12/10/2012
 */

/*
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/10/2012  MJM	Created.
 *
 */

import java.util.Iterator;
import java.util.Vector;

import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;


public class dtList {
	private String address=new String("localhost:3333"); //server to connect to
	private String sinkName=new String("dtListSink"); 	//sink connection name
	private String pluginName=new String("dtList"); 	//plugin connection name
	private PlugIn plugin=null; 						//plugin connection
	private Sink sink=null; 							//sink connection
	private boolean debug=false;
	
	public dtList() {}
	
	public void run(boolean getInfo) {
		PlugInChannelMap picm=new PlugInChannelMap();
		plugin=new PlugIn();
		try {
		   	plugin.OpenRBNBConnection(address,pluginName);	
			sink = new Sink();
			sink.OpenRBNBConnection(address,sinkName);
		} catch(Exception e) {
			System.err.println("Error on connect: "+e);
		    RBNBProcess.exit(0);
		}
		System.err.println("dtList, getInfo: "+getInfo);
		
	//process is to wait for request, get data from sink, convert data, send response, repeat
	    while (true) {
	    	try {
	    		if(debug) System.err.println("about to fetch");
		        picm=plugin.Fetch(-1); //block until request arrives
		        if(debug) System.err.println("got: "+picm);
		        
				if (picm.GetRequestReference().equals("registration")) {
//		        if(false) {
					if(debug) System.err.println("got registration request");
					// send generic header to avoid problems
					String result=
						"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
						+"<!DOCTYPE rbnb>\n"
						+"<rbnb>\n"
						+"\t\t<size>"+1+"</size>\n"
							+"\t\t<mime>text/plain</mime>\n"
						+"</rbnb>\n";
					picm.PutDataAsString(0,result);
					picm.PutMime(0,"text/xml");
				} else {

					
					ChannelMap cget = new ChannelMap();
					String rchan = picm.GetName(0);
					String sname = new String(rchan);
					String resp="";
					
					// special case for source-list request (/*)
					if(rchan.equals("*"))  {
						sink.RequestRegistration(cget);		// get what was asked for
						cget = sink.Fetch(60000);	
						ChannelTree ctree = ChannelTree.createFromChannelMap(cget);
						@SuppressWarnings("unchecked")
						Iterator<ChannelTree.Node> itree = ctree.iterator();
						while(itree.hasNext()) {
							ChannelTree.Node node = itree.next();
							if((node.getType() == ChannelTree.SOURCE) || (node.getType() == ChannelTree.PLUGIN)) {
								resp += node.getName();
								resp += "\n";
							}
						}
					}
					else {					
						if(!sname.endsWith("/")) sname += "/";
						if(rchan.equals("*")) rchan = "/...";
						//					if(rchan.equals("*")) rchan = "*";		// let /* mean get sources
						if(!rchan.endsWith("*") && !rchan.endsWith("...")) rchan += "/...";
						if(debug) System.err.println("rchan: '"+rchan+"'");

						cget.Add(rchan);
						if(debug) System.err.println("request: "+cget);
						sink.RequestRegistration(cget);		// get what was asked for
						cget = sink.Fetch(60000);					
						if(debug) System.err.println("got: "+cget);					

						int ngot = cget.NumberOfChannels();

						for(int i=0; i<ngot; i++) {
							resp += cget.GetName(i);
							resp = resp.replace(sname, "");			// strip leading source name
							//						if(getInfo) resp += ", ["+cget.GetUserInfo(i) +"]";
							String ui = cget.GetUserInfo(i);
							//						if(debug) System.err.println("cget("+i+").GetName: "+cget.GetName(i)+", GetUserInfo: "+ui);
							if(getInfo && !ui.equals("")) resp += ","+ui;
							resp += "\n";
						}
					}
					
					picm.PutDataAsString(0, resp);
				}
		        plugin.Flush(picm);
	    	} catch(Exception e) {
				System.err.println("oops, exception: "+e);
			}
	    }
	}
	
	public static void main(String[] args) {
		boolean getInfo=true;		// get user info by default
		try {
			ArgHandler ah = new ArgHandler(args);
			String opt="";
			if ((opt=ah.getOption('i')) != null) {
				if(opt.startsWith("n")) getInfo = false;
			}
		} catch(Exception e) {
			System.err.println("dtList Exception: "+e);
		}
		
		dtList dtl = new dtList();
		System.err.println("dtList running...");
		try {
			dtl.run(getInfo);
		} catch(Exception e) {
			System.err.println("Oops, exception: "+e);
		}
	}
}
