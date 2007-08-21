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

//KMLiPlugIn - creates a timeseries of network links to images, for dynamic display in Google
//             Earth beta4 and newer clients
// Eric Friets
// 9/21/06
// for SecureScan

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.RBNBProcess;

import java.util.Hashtable;

public class KMLiPlugIn {

//RBNB connections
private String address = "localhost:3333";
private String sinkName = "kmliSink";
private String pluginName = "KMLi";
private Sink sink = null;
private PlugIn plugin = null;

private double baseinterval=1;
private int basecount=20; //100;
private String urlBase="http://localhost/RBNB/";

public static void main(String[] args) {
    (new KMLiPlugIn(args)).exec();
}

//constructor
public KMLiPlugIn(String[] args) {
    
    //parse args
    try {
	ArgHandler ah = new ArgHandler(args);
	
	// 'h' Help
	if (ah.checkFlag('h')) {
	    System.err.println("KMLiPlugIn command line options");
	    System.err.println("   -a <RBNB address>");
	    System.err.println("       default: localhost:3333");
	    System.err.println("   -c <count> : max number of images to return");
	    System.err.println("      default: 100");
	    System.err.println("   -h (display this help message)");
	    System.err.println("   -i <interval> : min interval between images");
	    System.err.println("      default: 1 second");
	    System.err.println("   -n <PlugIn name>");
	    System.err.println("       default: KMLi");
	    System.err.println("   -u <URL base> : beginning of image URLs");
	    System.err.println("       default: http://localhost/RBNB/");
	}
	
	// 'a' - RBNB server address
	if (ah.checkFlag('a')) {
	    String addressL = ah.getOption('a');
	    if (addressL != null) {
		address=addressL;
	    } else {
		System.err.println(
		    "WARNING: Null argument to the \"-a\"" +
		    " command line option.");
	    }
	}
	
	// 'c' - count
	if (ah.checkFlag('c')) {
	    String countS = ah.getOption('c');
	    if (countS!=null) {
		basecount = Integer.parseInt(countS);
	    } else {
		System.err.println("WARNING: Null argument to the -c command line option");
	    }
	}
	
	// 'i' - interval
	if (ah.checkFlag('i')) {
	    String intervalS = ah.getOption('i');
	    if (intervalS!=null) {
		baseinterval = Double.parseDouble(intervalS);
	    } else {
		System.err.println("WARNING: Null argument to the -i command line option");
	    }
	}
	
	// 'n' PlugIn name
	if (ah.checkFlag('n')) {
	    String name = ah.getOption('n');
	    if (name != null) {
		pluginName = name;
	    } else {
		System.err.println(
		    "WARNING: Null argument to the \"-n\"" +
		    " command line option.");
	    }
	}
	
	// 'u' URL base
	if (ah.checkFlag('u')) {
	    String urlL=ah.getOption('u');
	    if (urlL!=null) {
		urlBase=urlL;
	    } else {
		System.err.println("WARNING: Null argument to the \"-u\" command line option");
	    }
	}
    } catch (Exception e) {
	System.err.println("AppendPlugIn argument exception "+e.getMessage());
	e.printStackTrace();
	RBNBProcess.exit(0);
    }
} //end constructor

//main execution loop
// creates plugin and sink connections, handles picm requests
public void exec() {
    
    //open connections
    try {
	sink = new Sink();
	sink.OpenRBNBConnection(address,sinkName);
	plugin = new PlugIn();
	plugin.OpenRBNBConnection(address,pluginName);
    } catch (Exception e) {
	System.err.println("Exception opening RBNB connections, aborting.");
	e.printStackTrace();
	RBNBProcess.exit(0);
    }
    
    //warn user if plugin name is different
    // (probably means another version of AppendPlugIn is running...)
    if (!plugin.GetClientName().equals(pluginName)) {
	pluginName = plugin.GetClientName();
	System.err.println(
	    "WARNING: The actual PlugIn name is " +
	    pluginName);
    }
    System.err.println("connections open; awaiting requests");
    
    //loop handling requests
    while (true) {
	PlugInChannelMap picm = null;
	try {
	picm = plugin.Fetch(3000);

	if ((picm.GetIfFetchTimedOut())||
	    (picm.NumberOfChannels()==0)) continue;
	
	//if generic registration request, just return
//	if (picm.GetRequestReference().equals("registration") &&
//	(picm.GetName(0).equals("*")||picm.GetName(0).equals("..."))) {
//	    plugin.Flush(picm);
//	    continue;
//	}
System.err.println("\npicm "+picm);
System.err.println("start "+picm.GetRequestStart()+", dur "+picm.GetRequestDuration()+", ref "+picm.GetRequestReference());
//byte[] foo=picm.GetDataAsInt8(0);
//System.err.println("foo length "+foo.length);
//for (int i=0;i<foo.length;i++) System.err.println(foo[i]);

	String[] message=null;
//System.err.println("picm.GetType(0) "+picm.GetType(0));
	if (picm.GetType(0)==ChannelMap.TYPE_STRING) {
	    message = picm.GetDataAsString(0);
	} else if (picm.GetType(0)==ChannelMap.TYPE_INT8) {
	    message=new String[1];
	    message[0]=new String(picm.GetDataAsInt8(0));
	}
System.err.println("picm(0) contained message "+message[0]);
	int count=basecount;
	double interval=baseinterval;
	if (message!=null && message[0].trim().length()>0) {
	    message[0]=message[0].trim();
	    char[] term = {'&'};
	    KeyValueHash kvh=new KeyValueHash(message[0],term);
	    String countS=kvh.get("kmli_count");
	    String intervalS=kvh.get("kmli_interval");
	    if (countS!=null) try {
		count=Integer.parseInt(countS);
	    } catch (Exception e) { e.printStackTrace(); }
	    if (intervalS!=null) try {
		interval=Double.parseDouble(intervalS);
	    } catch (Exception e) { e.printStackTrace(); }
	    
System.err.println("message contained count="+countS+", interval="+intervalS);
System.err.println("  new count="+count+", interval="+interval);
	}

	
	//make registration request, get data limits
	ChannelMap cm = new ChannelMap();
	for (int i=0;i<picm.NumberOfChannels();i++) cm.Add(picm.GetName(i));
System.err.println("created cm "+cm);
	if (message!=null && message[0].length()>0) for (int i=0;i<cm.NumberOfChannels();i++) cm.PutDataAsString(i,message[0]);
// System.err.println("making request with cm "+cm);
	sink.RequestRegistration(cm);
	cm=sink.Fetch(60000);
System.err.println("return ChannelMap from fetch:\n" + cm);
	if (cm.GetIfFetchTimedOut()) {
	    System.err.println("timed out making request, returning no data");
	    plugin.Flush(picm);
	    continue;
	}
	if (cm.NumberOfChannels()<1) {
	    System.err.println("no data on requested channel, returning no data");
	    plugin.Flush(picm);
	    continue;
	}
System.err.println("cm(0) range "+cm.GetTimeStart(0)+" plus "+cm.GetTimeDuration(0));
	
	//find appropriate begin and end of images
	double begin=0;
	double end=0;
	double duration=cm.GetTimeDuration(0);
	if (duration>picm.GetRequestDuration()) duration=picm.GetRequestDuration();
	if (picm.GetRequestReference().equals("registration")) {
	    picm.PutTime(cm.GetTimeStart(0),cm.GetTimeDuration(0));
	    picm.PutDataAsInt8(0,new byte[1]);
	    plugin.Flush(picm);
	    continue;
	} else if (picm.GetRequestReference().equals("newest")) {
	    end=cm.GetTimeStart(0) + cm.GetTimeDuration(0);
	    begin=end-duration;
System.err.println("newest req, begin="+begin+", dur="+duration+", end="+end);
	} else if (picm.GetRequestReference().equals("oldest")) {
	    begin=cm.GetTimeStart(0);
	    end=begin+duration;
System.err.println("oldest req, begin="+begin+", dur="+duration+", end="+end);
	} else { //must be absolute request
	    begin=cm.GetTimeStart(0);
	    if (begin<picm.GetRequestStart()) begin=picm.GetRequestStart();
	    end=begin+duration;
	    double dataend=cm.GetTimeStart(0)+cm.GetTimeDuration(0);
	    double reqend=picm.GetRequestStart()+picm.GetRequestDuration();
	    if (end>dataend) end=dataend;
	    if (end>reqend) end=dataend;
	    duration=begin-end;
System.err.println("absolute req, begin="+begin+", dur="+duration+", end="+end);
	}
	
	//find times of images to request
System.err.println("count="+count+", interval="+interval);
	if (duration<=0) count=1;
	else if (duration/count < interval)  count=(int)Math.round(duration/interval);
	interval=duration/count;
System.err.println("count="+count+", interval="+interval);
	
	//create KML string
	String kmlString = buildKML(urlBase+picm.GetName(0),begin,end,interval,count);
//System.err.println("kmlString "+kmlString);
	picm.PutTime(begin,duration);
	picm.PutDataAsString(0,kmlString);
	plugin.Flush(picm);
	} catch (Exception e) {
	    System.err.println("RBNB exception; returning no data; restarting plugin and sink");
	    e.printStackTrace();
	    try {
		if (picm!=null) {
		    picm.Clear();
		    plugin.Flush(picm);
		}
		sink.CloseRBNBConnection();
		sink.OpenRBNBConnection(address,sinkName);
		plugin.CloseRBNBConnection();
		plugin.OpenRBNBConnection(address,pluginName);
	    } catch (Exception e2) {
		System.err.println("RBNB exception; unable to establish connections; aborting");
		e2.printStackTrace();
		break;
	    }
	}
    } //end while
} //end method exec

private String buildKML(String url, double begin, double end, double intv, int cnt) {
    StringBuffer sb=new StringBuffer();
    
    //start kml document
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<kml xmlns=\"http://earth.google.com/kml/2.1\">");
    sb.append("<Folder>");
    sb.append("<name>foo</name>");
    sb.append("<visibility>0</visibility>");
    sb.append("<open>1</open>");
    sb.append("<LookAt id=\"khLookAt727_copy0\">");
    sb.append("<longitude>-72.2336291</longitude>");
    sb.append("<latitude>43.6839681</latitude>");
    sb.append("<altitude>0</altitude>");
    sb.append("<range>461</range>");
    sb.append("<tilt>0</tilt>");
    sb.append("<heading>0</heading>");
    sb.append("</LookAt>");

    //add images to kml document
    java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    sdf.setTimeZone(new java.util.SimpleTimeZone(0,"UTC"));
    for (int i=0;i<cnt;i++) {
	double btime=begin+i*intv;
	double etime=begin+(i+1)*intv;
	String bdate=sdf.format(new java.util.Date((long)btime*1000));
	String edate=sdf.format(new java.util.Date((long)etime*1000));
	sb.append("<GroundOverlay id=\""+i+"\">");
	sb.append("<name>Image"+i+"</name>");
	sb.append("<TimeSpan>");
	sb.append("<begin>"+bdate+"</begin>");
	sb.append("<end>"+edate+"</end>");
	sb.append("</TimeSpan>");
	sb.append("<Icon>");
	sb.append("<href>"+url+"?t="+btime+"</href>");
	sb.append("</Icon>");
	sb.append("<LatLonBox id=\"khLatLonBox727_copy0\">");
	sb.append("<north>43.68446809718569</north>");
	sb.append("<south>43.68346809718571</south>");
	sb.append("<east>-72.23202763228679</east>");
	sb.append("<west>-72.23360590315268</west>");
	sb.append("</LatLonBox>");
	sb.append("</GroundOverlay>");
	
    }
    
    //close kml document
    sb.append("</Folder>");
    sb.append("</kml>");

    return sb.toString();
}

} //end class KMLiPlugIn
