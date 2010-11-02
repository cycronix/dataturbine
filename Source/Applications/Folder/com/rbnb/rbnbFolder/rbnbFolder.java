
/*
Copyright 2010 Erigo Technologies LLC

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

package com.rbnb.rbnbFolder;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ArgHandler;

/******************************************************************************
 * Watch a file folder and if any files in it update, send the new content to
 * the RBNB server.
 * <p>
 *
 * @author Matthew J. Miller
 *
 * @since V3.2
 * @version 11/02/2010
 */

/*
 * Copyright 2010 Erigo Technologies LLC
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/02/2010  MJM	Created.
 *
 */

public class rbnbFolder extends Thread {

	private static int updateInc = 2;		// update interval (sec)
	
	private static String folderName = ".";
	private static String rbnbServer = "localhost";  // 127.0.0.1 is more DTN than "localhost"?
	private static String fileFilter="*";
	private static String sName="Folder";
	private static String commonName=null;
	private static boolean retainFile=true;
	private static boolean sendInitial=false;
	private static boolean negateFilter=false;
	private static int Ncache=1, Narchive=10000;
	private static String Amode="append";
	
	private static long iterCount=0;
	private static Timer myTimer;
	public static Source source=null;
	private static long lastMod = 0;
	private static final String TAG = "rbnbFolder";
	private static Pattern pattern;
	private static File folder=null;
	
	private static Hashtable fileHash = new Hashtable();
	
//---------------------------------------------------------------------------------	
    public rbnbFolder() {
    	
    	myTimer = new Timer();
    	myTimer.schedule(new TimeTask(), 1000,(int)(updateInc*1000)); 	// initial delay
    }

//---------------------------------------------------------------------------------	   
    public static void main (String[] args) {

    	boolean printHelp=false;
    	
	    try { 
		    ArgHandler ah = new ArgHandler(args);
		    String value;
		    if ((value = ah.getOption('f')) != null) folderName = value;
		    if ((value = ah.getOption('a')) != null) rbnbServer = value;
		    if ((value = ah.getOption('s')) != null) sName = value;
		    if ((value = ah.getOption('c')) != null) Ncache = Integer.parseInt(value);
		    if ((value = ah.getOption('n')) != null) Narchive = Integer.parseInt(value);
		    if ((value = ah.getOption('m')) != null) Amode = value;
		    if ((value = ah.getOption('F')) != null) fileFilter = value;
		    if ((value = ah.getOption('C')) != null) commonName=value;
		    if ((value = ah.getOption('T')) != null) updateInc=Integer.parseInt(value);
		    if (ah.checkFlag('D')) retainFile=false;
		    if (ah.checkFlag('I')) sendInitial=true;
		    if (ah.checkFlag('G')) negateFilter=true;
		    if (ah.checkFlag('h')) printHelp=true;
    	} catch (Exception e) {
    	    System.err.println("Error on argument parsing");
    	    e.printStackTrace();
    	    System.exit(-1);
    	};
 
    	if((args.length == 0) || printHelp) System.out.println(
	    		"rbnbFolder"+
	    		"\n -f<folder>      \tdefault="+folderName+ 
	    		"\n -a<host>        \tdefault="+rbnbServer+
	    		"\n -s<source>      \tdefault="+sName+
	    		"\n -c<cache>       \tdefault="+Ncache+
	    		"\n -n<archive>     \tdefault="+Narchive+
	    		"\n -m<amode>       \tdefault="+Amode+
	    		"\n -F<filter>      \tdefault="+fileFilter+
	    		"\n -C<commonName>  \tdefault="+commonName+
	    		"\n -T<timeInterval>\tdefault="+updateInc+
	    		"\n -D<deleteFile>  \tdefault=false"+
	    		"\n -I<sendInitial> \tdefault=false"+
	    		"\n -G<negateFilter>\tdefault=false");
    	
	    if(printHelp) System.exit(0);
	    else	System.out.println("Running...");
	    
        try {
        	startSource();			// connect to RBNB server
        } catch(Exception e) {
        	e.printStackTrace();
        	System.exit(0);
        };

        pattern = Pattern.compile
        			(replaceWildcards(fileFilter));	// do this once in advance
    
    	initializeFolder();			// initialize file hashtable
        new rbnbFolder();			// start the timer check 
    }
    
//---------------------------------------------------------------------------------	
// main activity class - check for updates on timer thread
    
	  class TimeTask extends TimerTask {
		    public void run() {
				FileWatch();
				iterCount++;
//				System.err.println("iterCount: "+iterCount);
		    }
	  }

//---------------------------------------------------------------------------------	
	  private static void startSource() {
	        try {
	        	source = new Source(Ncache,Amode,Narchive);	
	        	source.OpenRBNBConnection(rbnbServer,sName);
	        } catch(Exception e) {
	        	e.printStackTrace();
	        	System.exit(0);
	        };  
	  }
	  
	  private static void stopSource() {
		    if(source != null) {
		    	System.err.println("Detaching source");
		    	source.Detach();
		    }
	  }

//---------------------------------------------------------------------------------	
// initialize folder by building a hash table of file names and modified times
	  
	private static void initializeFolder() {
		int nfound=0;
		try {
			// get updated list of files
	    	folder = new File(folderName);
			File[] listOfFiles = folder.listFiles();
			if(listOfFiles == null) return;
//			System.err.println("folder: "+folderName+", nfile: "+listOfFiles.length);
			
			for(int i=0; i<listOfFiles.length; i++) {
				File file = listOfFiles[i];
				if(fileMatch(file))	{		// add to initial file hash
					nfound++;
					String fileName = new String(file.getName());
					Long lastMod; 
					if(sendInitial) lastMod = new Long(0);	// force initial update
					else			lastMod = new Long(file.lastModified());
					
					fileHash.put(fileName, lastMod);
//					System.err.println("fileHash("+fileName+","+lastMod+")");
				}
			}
		} catch (Exception e) {
    		System.err.println("Error initializing folder: "+folderName);
			e.printStackTrace();
		}
		
//		System.err.println("rbnbFolder initialized, matches found: "+nfound);
	}

//---------------------------------------------------------------------------------	
// check if a file is legal and passes file-filter
	
	private static boolean fileMatch(File file) {
		if(!file.isFile()) return(false);
		
		String fileName = file.getName();
		
		Matcher matcher = pattern.matcher(fileName);
		if(negateFilter) { if(matcher.matches()) return(false);  }
		else 			 { if(!matcher.matches()) return(false); }  
		
		if(file.length() <= 0) return(false);

		return(true);	
	}
	
//---------------------------------------------------------------------------------	
// do the main file checking and send to RBNB if legal update
	
	private void FileWatch() {
		try {
			// get updated list of files

			File[] listOfFiles = folder.listFiles();
			if(listOfFiles == null) return;

			for(int i=0; i<listOfFiles.length; i++) {
				File file = listOfFiles[i];
				if(fileMatch(file)) {
					String fileName =  file.getName();
					long newMod = file.lastModified();
					long oldMod = 0;
					if(fileHash.containsKey(fileName))
						oldMod = ((Long)fileHash.get(fileName)).longValue();
					
					fileHash.put(fileName, new Long(newMod));	// new/update entry
					
					if(newMod > oldMod) {	// it's a go
//						System.err.println("folder update: "+fileName+", newMod: "+newMod+", oldMod: "+oldMod);
				
					// define RBNB channels
						ChannelMap cMap = new ChannelMap();
						String rName = fileName;					// rbnb channel name
						if(commonName != null) rName = commonName;	// common name
						cMap.Add(rName);

						byte[] data;
						long fileLength = file.length();
						data = new byte[(int)(fileLength)];	// read file update
						java.io.FileInputStream fis = new java.io.FileInputStream(file);
						int nread = fis.read(data);
						fis.close();
						if(nread > 0) {
							System.err.println("Put file: "+fileName+", rbnbChan: "+rName+", size: "+fileLength);
							cMap.PutDataAsByteArray(0, data);
							source.Flush(cMap);	
						}
	
						if(!retainFile) {
							boolean status = file.delete();
							fileHash.remove(fileName);	// toss known dead files (proactively sweep/clean?)
							if(status) System.err.println("Deleted source file: "+fileName);
							else	System.err.println("Failed to delete source file: "+fileName);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			startSource();		// presume it needs to reconnect?
		}
	}
 
//---------------------------------------------------------------------------------
// utility to convert file "glob" to java wildcard logic
	
	private static String replaceWildcards(String wild)
	{
	    StringBuffer buffer = new StringBuffer();
	 
	    char [] chars = wild.toCharArray();
	 
	    for (int i = 0; i < chars.length; ++i)
	    {
	        if (chars[i] == '*')
	            buffer.append(".*");
	        else if (chars[i] == '?')
	            buffer.append(".");
	        else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
	            buffer.append('\\').append(chars[i]);
	        else
	            buffer.append(chars[i]);
	    }
	 
	    return buffer.toString();
	 
	}// end replaceWildcards method
}




