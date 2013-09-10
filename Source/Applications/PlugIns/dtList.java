
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
	static private boolean debug=false;
	
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
	    		if(debug) System.err.println("\n-----------about to fetch");
		        picm=plugin.Fetch(-1); //block until request arrives
		        if(picm.NumberOfChannels()==0) {
		        	System.err.println("oops, no channels in request");
		        	continue;
		        }
		        
				if (picm.GetRequestReference().equals("registration")) {
					if(debug) System.err.println("registration request (generic response)");
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
					plugin.Flush(picm);
					continue;
				} else {
			        if(debug) System.err.println("request: "+picm+", nchan: "+picm.NumberOfChannels());

					ChannelMap cget = new ChannelMap();
					String rchan = picm.GetName(0);
					String sname = new String(rchan);
					if(debug) System.err.println("requesting chan: "+sname);
					String resp="";
					
					// special case for _oldest, _newest time request
					if(rchan.endsWith("_limits")) {
						sname = sname.split("/",2)[0] + "/...";		// full-depth channel list this source
						cget.Add(sname);
//						System.err.println("sname: "+sname);
						sink.RequestRegistration(cget);
//						System.err.println("fetch: "+cget);
						cget = sink.Fetch(60000);
//						System.err.println("got: "+cget);
						double told = 0., tnew = 0.;
						for(int i=0; i<cget.NumberOfChannels(); i++) {
							double tstart = cget.GetTimeStart(i);
							double tend = tstart + cget.GetTimeDuration(i);
//							System.err.println("tstart: "+tstart+", tend: "+tend);
							if(i==0) { told = tstart;  tnew = tend; }
							else {
								if(tstart < told) told = tstart;
								if(tend > tnew) tnew = tend;
							}
						}
						String Told = new java.util.Date((long)(told*1000)).toString();
						String Tnew = new java.util.Date((long)(tnew*1000)).toString();

						resp = Told+"  -  "+Tnew;
//						System.out.println("resp: "+resp);
					}
					// special case for source-list request (/*)
					else if(rchan.equals("*"))  {
						sink.RequestRegistration();		// get what was asked for
						cget = sink.Fetch(60000);	
						ChannelTree ctree = ChannelTree.createFromChannelMap(cget);
//						System.err.println("itree: "+ctree);

						@SuppressWarnings("unchecked")
						Iterator<ChannelTree.Node> itree = ctree.rootIterator();
						while(itree.hasNext()) {
							ChannelTree.Node node = itree.next();
//							System.err.println("node: "+node);
							String nodeName = node.getName();
							if(nodeName.equals(pluginName)) continue;		// don't list yourself
							if(node.getType() == ChannelTree.SOURCE) {
								resp += (nodeName + "\n");							
							}
							else if(node.getType() == ChannelTree.PLUGIN) {	// recurse one level into plugins
								ChannelMap pget = new ChannelMap();
								sink.RequestRegistration(pget);		// get plugin channels
								pget = sink.Fetch(60000);	
								ChannelTree ptree = ChannelTree.createFromChannelMap(pget);
								@SuppressWarnings("unchecked")
								Iterator<ChannelTree.Node> iptree = ptree.rootIterator();
								while(iptree.hasNext()) {
									ChannelTree.Node pnode = iptree.next();
//									System.err.println("pnode: "+pnode);
									String pnodeName = pnode.getName();
									if(pnode.getType() == ChannelTree.SOURCE) {
										resp += (nodeName + "/" + pnodeName + "\n");							
									}
								}
							}
						}
					}
					else {			// channel list request	
						if(debug) System.err.println("rchan: '"+rchan+"'");
						if(!sname.endsWith("/")) sname += "/";
						if(rchan.equals("*")) rchan = "/...";
						//					if(rchan.equals("*")) rchan = "*";		// let /* mean get sources
						if(!rchan.endsWith("*") && !rchan.endsWith("...")) rchan += "/...";
						cget.Add(rchan);
						if(debug) System.err.println("request: "+cget);
						sink.RequestRegistration(cget);		// get what was asked for
						cget = sink.Fetch(60000);					
						if(debug) System.err.println("got: "+cget);					
						if(cget.NumberOfChannels()==0) {
							System.err.println("no channels!");
							picm.Add("reply");
							resp="<No Channels>";
//							continue;
						} else {
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
					}
					
					picm.PutDataAsString(0, resp);
				}

				if(picm.NumberOfChannels()==0) {			// another try to prevent long delays
					picm.Add("reply");
					picm.PutDataAsString(0,"<No Data>");
				}
				if(debug) System.err.println("response: "+picm);
				plugin.Flush(picm);
	    	} catch(Exception e) {
				System.err.println("oops, exception: "+e);
				try{
					picm.PutDataAsString(0,"error: "+e);  plugin.Flush(picm);
					Thread.sleep(1000);
				} catch(Exception ee){};	// no busy loop
//				System.exit(0);		// no infinite loops
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
			if ((opt=ah.getOption('x')) != null) debug=true;
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
