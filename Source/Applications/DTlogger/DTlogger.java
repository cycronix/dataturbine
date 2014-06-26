/*
Copyright 2014 Cycronix

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

//---------------------------------------------------------------------------------	
// DTlogger:  parse data logger file to DataTurbine 
// Matt Miller, Cycronix
// 06/09/2014

// sample file format:
// RBNB,DT
// ID,Time,seqno,data_len,conductivity,temp_C,pressure,dissolvedO2
// DT, 2014/05/08T18:58:38, 0, 36, 45.7467, 18.8747, 1.12, 5.832

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;

public class DTlogger {
	static boolean debug=false;				// debug prints
	static boolean appendMode=false;		// append to end of existing DT data
	static long autoflush = 3600;			// default 1-hour (3600 second) autoflush
	static long skipLines = 0;				// # lines to skip between first chanNames line to data
	static boolean repeatFetch = false;		// auto-fetch data loop 
	static boolean storeTime = false;		// store time string as channel
	static String nanVal = "0";				// replace NAN with this
	static String loggerFileName=null;		// name of input data file to parse
	static String leadingID=null;			// leading ID string (IWG1 compliant)
	static String address="localhost";		// RBNB address
	static int ncache = 10;					// RBNB cache frames
	static int narchive = 1000;				// RBNB archive frames
	
	static BufferedReader br=null;			// re-useable call-to-call
	static String SourceName="DTlogger";
	static ArrayList<String>chanNames=null;
	static Source source=null;
	static ChannelMap cput=null;
	
	public static void main(String args[]) {
	 	boolean printHelp=false;

	 	int dirArg = 0;
	 	while((dirArg<args.length) && args[dirArg].startsWith("-")) {		// arg parsing
	 		if(args[dirArg].equals("-x")) 	{ debug = true;   }
	 		if(args[dirArg].equals("-h")) 	{ printHelp = true; }
	 		if(args[dirArg].equals("-a")) 	{ address = args[++dirArg];  }		
	 		if(args[dirArg].equals("-f"))   { autoflush = Long.parseLong(args[++dirArg]); }	
	 		if(args[dirArg].equals("-k"))   { skipLines = Long.parseLong(args[++dirArg]); }	
	 		if(args[dirArg].equals("-n"))   { narchive = Integer.parseInt(args[++dirArg]); }	
	 		if(args[dirArg].equals("-r"))   { repeatFetch = true; }	
	 		if(args[dirArg].equals("-t"))   { storeTime = true; }	
	 		if(args[dirArg].equals("-A"))   { appendMode = true; }	
	 		if(args[dirArg].equals("-v"))	{ nanVal = args[++dirArg]; }
	 		if(args[dirArg].equals("-s"))	{ SourceName = args[++dirArg]; }
	 		dirArg++;
	 	}

	 	if(printHelp || args.length < (dirArg+1)) {
	 		System.out.println(
	 				"DTlogger"+
	 						"\n -h(help)        \tdefault=false          \tPrint this usage message."+
	 						"\n -x(debug)       \tdefault=false          \tOptionally print debug messages."+
	 						"\n -a<host>        \tdefault="+address+"      \tDNS or IP address of RBNB server to send data."+
	 						"\n -f<autoFlush>   \tdefault="+autoflush+"    \tInterval (sec) at which to poll logfile."+
	 						"\n -k<skipLines>   \tdefault="+skipLines+"        \tLines to skip (between 2 header & 1st data lines)."+
	 						"\n -n<narchive>    \tdefault="+narchive+"     \tNumber of archive frames to store in RBNB."+
	 						"\n -r(repeatFetch) \tdefault="+repeatFetch+"   \tRepeat-fetch data (true) or single pass (false)."+
	 						"\n -t(storeTime)   \tdefault="+storeTime+"    \tStore time as a parameter channel (true)."+
	 						"\n -A(appendMode)  \tdefault="+appendMode+"   \tAppend archive data (true) or create new (false)."+
	 						"\n -v<nanVal>      \tdefault="+nanVal+"       \tReplace \"NAN\" string with this numeric value."+
	 						"\n -s<sourceName>  \tdefault="+SourceName+"   \tName of RBNB data source."+
	 						"\n  logFileName     \t<required>             \tFile name of log file from which to read data."
	 				);
	 		System.exit(0);
	 	}
		loggerFileName = args[dirArg++];		// args[0]:  logger.dat file

		// DT setup
		try {
			String mode = "create";
			if(appendMode) mode = "append";
			source = new Source(ncache,mode,narchive);
			source.OpenRBNBConnection(address,SourceName);	
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		if(!repeatFetch) getData(true);		// run once
		else {
			Timer timer = new Timer();
			TimerTask fetchTask = new TimerTask() {
				@Override public void run() {
					if(getData(false)) {		// pick up from old data if you can
						System.err.println("Failed to pick up from old data, refetch from start of file...");
						boolean status = getData(true);	
						System.err.println("refetch status: "+status);
					}
//					System.err.println("Waiting for data ("+autoflush+" sec)...");
				};
			};
			// repeatFetch@autoflush interval, convert to msec
			timer.scheduleAtFixedRate(fetchTask,  0,  autoflush*1000);		
		}
	}
	
//---------------------------------------------------------------------------------	

	private static boolean getData(boolean newfile) {
		System.err.println("DTlogger fetch: "+loggerFileName);
		int idxTime=0, idxFirstChan=0;
		try {
			File loggerFile = new File(loggerFileName);	

			// logger file header parse
			if(newfile || (br==null)) {		// new file
				br = new BufferedReader(new FileReader(loggerFile));
				String line = br.readLine();
				String[] headerItems = line.replace("\"","").split(",");
				if(!headerItems[0].equals("TOA5") && !headerItems[0].equals("IWG1") && !headerItems[0].equals("RBNB")) {
					br.close();
					throw new Exception("unrecognized file format (need RBNB or TOA5 or IWG1)");
				}
				if(headerItems.length > 1) leadingID = headerItems[1];

				// Channel names
				line = br.readLine();
				chanNames = new ArrayList<String>(Arrays.asList(line.replace("\"","").split(",")));
				cput = new ChannelMap();
				
				if(leadingID == null) 	idxTime = 0;
				else					idxTime = 1;		// skootch
				idxFirstChan = idxTime+1;
				if(storeTime) cput.Add(chanNames.get(idxTime));
				for(int i=idxFirstChan; i<chanNames.size(); i++) cput.Add(chanNames.get(i));

				// Sample types (unused lines)
				for(int i=0; i<skipLines; i++) line = br.readLine();
				
				System.err.println("DTlogger started, line ID: "+leadingID+", nchan: "+cput.NumberOfChannels());
			}
		
			// time formats (try lots of variants)
			ArrayList <String>data;
			ArrayList <SimpleDateFormat>sdf = new ArrayList<SimpleDateFormat>();
			sdf.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz"));
			sdf.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			sdf.add(new SimpleDateFormat("MM/dd/yyyy HH:mmz"));
			sdf.add(new SimpleDateFormat("MM/dd/yyyy HH:mm"));
			sdf.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ssz"));
			sdf.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss"));
			sdf.add(new SimpleDateFormat("yyyyMMdd'T'HHmmssz"));
			sdf.add(new SimpleDateFormat("yyyyMMdd'T'HHmmss"));
			
			// parse data lines
			long otime = 0;
			String line;
			int lineCount = 0;
			
			while ((line = br.readLine()) != null) {
				if(debug) System.err.println("DTlogger line: "+line);
				line = line.replace("\"","");			// remove extra quote chars
				line = line.replace("NAN", nanVal);		// replace "NAN" with legal number
				data = new ArrayList<String>(Arrays.asList(line.split(",")));
				if(data.size()<2) {
//					System.err.println("Bad line parse (skipping): "+line);
					continue;
				}
				long time = 0;
				
				for(SimpleDateFormat s:sdf) {
					try {
						time = s.parse(data.get(idxTime)).getTime();
						break;			// got it
					} 
					catch(ParseException e) {
						continue;
					}
				}

				if(time <= 0) {
					System.err.println("illegal timestamp, ignored: "+data.get(idxTime));
					continue;
				}
				if(time <= otime) {
					System.err.println("backwards-going time, ignored: "+data.get(idxTime));			// warning
					continue;
				}
				
				lineCount++;
				otime = time;
				if(debug) System.err.println("time,data1,data2: "+new Date(time)+", "+data.get(idxFirstChan)+", "+data.get(idxFirstChan+1));

				if(leadingID != null) {
					if(!data.get(0).equals(leadingID)) {
						System.err.println("Error, leading ID mismatch.  Expected: "+leadingID+", got: "+data.get(0));
						continue;
					}
				}
				
				// write DT data as float32 (skip ID & optionally Time)
				double dtime = (double)time/1000.;			// RBNB time
//				if(debug) System.err.println("time: "+time+", dtime: "+dtime);
				cput.PutTime(dtime, 0.);
				int idx=0;
				if(storeTime) cput.PutDataAsFloat64(idx++, new double[]{dtime});	// time as double seconds
				for(int i=idxFirstChan; i<data.size(); i++) 
					cput.PutDataAsFloat32(idx++, new float[]{Float.parseFloat(data.get(i))} ) ;
			}
			source.Flush(cput);				// clean up after last autoflush
			if(newfile) br.close();			// close if newfile mode
			if(lineCount > 0) System.err.println("DTlogger processed lines: "+lineCount);
		} catch(Exception e) {
			System.err.println("DTlogger exception: "+e);
			e.printStackTrace();
			return true;		// something went wrong
		}
		return false;			// OK
	}
}
