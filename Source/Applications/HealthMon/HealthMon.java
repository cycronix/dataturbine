/*
Copyright 2013 Cycronix

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

// Monitor DT Server for operational health
// Matt Miller, Cycronix 6/2013

import com.rbnb.sapi.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

//---------------------------------------------------------------------------------------------
@SuppressWarnings("deprecation")
public class HealthMon
{
    private String delim="\n----------------------------------------------------";
    private String myName="HealthMon";
    private String server="localhost";
    private double interval = 3600.;		// 60 minutes
    double Tnow=0.;
	FileWriter Fsummary, Fdetails;

	// constructor
	public HealthMon(String[] args) {
	    if(args.length > 0) server = args[0];
	    if(args.length > 1) interval = Double.parseDouble(args[1]);
	    if(args.length == 0) System.out.println("HealthMon <server> <interval>");
	    try {
	    	Fsummary = new FileWriter(myName+".txt");
	    	Fdetails = new FileWriter(myName+"_details.txt");
	    } catch(Exception e) {
	    	System.err.println("Exception on opening Report file: "+e);
	    }
	}
	
	//---------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		try{
			(new HealthMon(args)).exec();
		} catch(Exception e) {
			System.err.println("OOPS, exception: "+e);
		}
	}
	
	//---------------------------------------------------------------------------------------------
    public void exec() {
//    	System.out.println("HealthMon <server> <interval>");

    	long Tmillis = System.currentTimeMillis();
    	Tnow = (double)Tmillis / 1000.;

    	try {
    		// report header
    		report(delim);
    		details("Exception/Error Details:\n");
    		report("DataTurbine HealthMonitor Summary Report");
    		report("Date:  "+new Date(Tmillis));
    		report("Server: "+server);

    		// Create a sink:
    		Sink sink=new Sink();
    		sink.OpenRBNBConnection(server,myName);

    		ParseRegistration(sink);		// parse Registration for info
    		ParseLog(sink);					// parse Log-file for problems
    		ParseMetrics(sink);				// analyze metrics for issues

    		// clean up
    		sink.CloseRBNBConnection();
    		report(delim);
    		Fsummary.close();
    	} catch (Exception e) { e.printStackTrace(); }
    }
  
    //----------------------------------------------------------------------------------
	// Parse Registration for info
    void ParseRegistration(Sink sink) throws IOException, SAPIException {
    	sink.RequestRegistration();
    	ChannelMap aMap = sink.Fetch(-1);
    	String[] SrcList = aMap.GetSourceList();
    	String[] SnkList = aMap.GetSinkList();
    	String[] PiList = aMap.GetPlugInList();
    	String[] ChanList = aMap.GetChannelList();
    	String[] ServList = aMap.GetServerList();
    	report(delim);
    	report("Connections:");
    	report("Sources["+SrcList.length+"]: "+Arr2List(SrcList));
    	report("Sinks["+SnkList.length+"]: "+Arr2List(SnkList));
    	report("PlugIns["+PiList.length+"]: "+Arr2List(PiList));
    	report("Routed Servers["+ServList.length+"]: "+Arr2List(ServList));

    	int nchan=0;
    	for(int i=0; i<ChanList.length; i++) if(!ChanList[i].startsWith("_")) nchan++;		// don't count built-in sources
    	report("Data Channels: "+nchan);
    }
    
    //----------------------------------------------------------------------------------
	// parse Log-file for problems
    void ParseLog(Sink sink) throws IOException, SAPIException {
		ChannelMap rMap = new ChannelMap();
		ChannelMap aMap;

		rMap.Add("_Log/*");
		sink.RequestRegistration(rMap);		// have to use non-wildcard chans for absolute times
		rMap = sink.Fetch(-1);

		sink.Request(rMap,Tnow-interval,interval,"absolute");
		//	    sink.Request(rMap,0.,interval,"aligned");
		//	    sink.Request(rMap,0.,interval,"newest");

		if ((aMap = sink.Fetch(-1)) == null) {
			System.err.println("Data not received!");
			return;
		}
		
		int Exceptions=0;		// exception counter
		int Errors=0;			// error counter
		int SinkStarts=0;
		int SinkStops=0;
		int SourceStarts=0;
		int SourceStops=0;
		int ClientStarts=0;
		for(int i=0; i<aMap.NumberOfChannels(); i++) {
			String[] sdata = aMap.GetDataAsString(i);
			for(int j=0; j<sdata.length; j++) {
				//	    		report(sdata[j]);
				if(sdata[j].contains("Exception")) {
					Exceptions++;
					details("Exception: "+sdata[j]);
				}
				if(sdata[j].contains("server error")) {
					Errors++;
					details("Error: "+sdata[j]);
				}
				if(sdata[j].contains("Started for client")) ClientStarts++;
				if(sdata[j].contains("Started for sink")) SinkStarts++;
				if(sdata[j].contains("shutting down.")) SinkStops++;		// this gets client stops too
				if(sdata[j].contains("Started for source")) SourceStarts++;
				if(sdata[j].contains("shutting down with")) SourceStops++;
			}
		}
		report(delim);
		report("Log Summary (Interval: "+sInterval(interval)+")");
		report("Sink Start/Stop:   "+(ClientStarts+SinkStarts)+"/"+SinkStops);
		report("Source Start/Stop: "+SourceStarts+"/"+SourceStops);
		report("Exception Count:   "+Exceptions);
		report("Server Errors:     "+Errors);
    }
    
    //----------------------------------------------------------------------------------
	// analyze Metrics channels for issues
    void ParseMetrics(Sink sink) throws IOException, SAPIException {
		ChannelMap rMap = new ChannelMap();
		ChannelMap aMap;
		
		rMap.Add("_Metrics/MemoryUsed");
		rMap.Add("_Metrics/SocketBytes");
		rMap.Add("_Metrics/TotalMemory");

		sink.Request(rMap,Tnow-interval,interval,"absolute");

		if ((aMap = sink.Fetch(-1)) == null) {
			System.err.println("Data not received!");
			return;
		}
		//	    report("aMap: "+aMap);
		report(delim);
		double times[] = aMap.GetTimes(0);		// see what interval we got
		double mInterval = Math.round(times[times.length-1]-times[0]);
		if(Math.abs((interval-mInterval)/interval) <= 0.1)  mInterval = interval;		// close enough (10%)
		report("Metrics Summary (Interval: "+sInterval(mInterval)+")");

		long SockBytes[] = aMap.GetDataAsInt64(aMap.GetIndex("_Metrics/SocketBytes"));
		long deltaSB = SockBytes[SockBytes.length-1] - SockBytes[0];			// net this interval
		if(deltaSB < 10000000) 	report("SocketBytes:          "+deltaSB/1024+" (KB)");		
		else					report("SocketBytes:          "+deltaSB/1048576+" (MB)");
		long MemUsed[] = aMap.GetDataAsInt64(aMap.GetIndex("_Metrics/MemoryUsed"));
//		report("MemoryUsed (Min/Max/Latest): "+minMem(MemUsed)+"/"+maxMem(MemUsed)+"/"+MemUsed[MemUsed.length-1]/1048576+" (MB)");
		report("MemoryUsed (Min/Max): "+minMem(MemUsed)+"/"+maxMem(MemUsed)+" (MB)");
		report("MemoryUsed Trend:     "+Math.round(linreg(MemUsed,mInterval)/(1048576./3600.))+" (MB/Hour)");

		long TotMem[] = aMap.GetDataAsInt64(aMap.GetIndex("_Metrics/TotalMemory"));
		report("Java Heap:            "+TotMem[TotMem.length-1]/1048576+" (MB)");
    }
    
    //----------------------------------------------------------------------------------
    // build up comma-separated string from string-array
    static String Arr2List(String[] s) {
        StringBuilder sb = new StringBuilder();
        for (String n : s) { 
//        	if(n.startsWith("_")) continue;			// skip _Hidden fields
            if (sb.length() > 0) sb.append(',');
            sb.append("'").append(n).append("'");
        }
        return sb.toString();
    } 

    //----------------------------------------------------------------------------------
    // find max, convert to MB
    static long maxMem(long[] vals) {
    	long max=vals[0];
    	for(int i=1; i<vals.length; i++) if(max < vals[i]) max=vals[i];
    	return max / 1048576;
    }
    
    // find min, convert to MB
    static long minMem(long[] vals) {
    	long min=vals[0];
    	for(int i=1; i<vals.length; i++) if(min > vals[i]) min=vals[i];
    	return min / 1048576;
    }
    
    //----------------------------------------------------------------------------------
    static double linreg(long[] y, double interval) {
    	// first pass: read in data, compute xbar and ybar
    	double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
    	double dx = interval / (y.length-1);
    	int n = y.length;
    	for(int i=0; i<n; i++) {
    		double xi = i*dx;
    		sumx  += xi;
    		sumx2 += xi * xi;
    		sumy  += y[i];
    	}
    	double xbar = sumx / n;
    	double ybar = sumy / n;

    	// second pass: compute summary statistics
    	double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
    	for (int i = 0; i < n; i++) {
    		double xi = i*dx;
    		xxbar += (xi - xbar) * (xi - xbar);
    		yybar += (y[i] - ybar) * (y[i] - ybar);
    		xybar += (xi - xbar) * (y[i] - ybar);
    	}
    	double beta1 = xybar / xxbar;
    	double beta0 = ybar - beta1 * xbar;

//    	System.out.println("y   = " + beta1 + " * x + " + beta0);
    	return(beta1);
    }
    
    //----------------------------------------------------------------------------------
    // make pretty time interval string
    static String sInterval(double interval) {
    	if(interval < 300.) 			return(""+Math.round(interval)+" Sec");
    	else if(interval < 7200.) 		return(""+Math.round(interval/60.)+" Min");
    	else if(interval < 2.*86400.) 	return(""+Math.round(interval/3600.)+" Hour");
    	else 							return(""+Math.round(interval/86400.)+" Day");
    	
    }

    //----------------------------------------------------------------------------------
    // generate report
    void report(String line) throws IOException {
		System.out.println(line);
		Fsummary.write(line+"\n");
		Fsummary.flush();	
    }
    
    //----------------------------------------------------------------------------------
    // document details
    void details(String line) throws IOException {
//		System.err.println(line);
		Fdetails.write(line+"\n");
		Fdetails.flush();	
    }

}  // end class HealthMon    
