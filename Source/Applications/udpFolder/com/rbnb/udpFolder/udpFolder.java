
/*
Copyright 2011 Erigo Technologies LLC

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

package com.rbnb.udpFolder;

import java.io.File;
import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;

//import com.rbnb.sapi.*;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.UDPOutputStream;

/******************************************************************************
 * Watch a file folder and if any files in it update, send the new content to
 * UDP port.  Modelled after rbnbFolder
 * <p>
 *
 * @author Matthew J. Miller
 *
 * @since V3.2
 * @version 07/01/2011
 */

/*
 * Copyright 2011 Erigo Technologies LLC
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/01/2011  MJM	Created.
 *
 */

public class udpFolder extends Thread {

	private static int updateInc = 2;		// update interval (sec)
	
	private static String folderName = ".";
	private static String fileFilter="*";
	private static boolean retainFile=true;
	private static boolean sendInitial=false;
	private static boolean negateFilter=false;
	
	private static long iterCount=0;
	private static Timer myTimer;
	private static long lastMod = 0;
	private static final String TAG = "udpFolder";
	private static Pattern pattern;
	private static File folder=null;
	
	private static Hashtable fileHash = new Hashtable();
	private static Hashtable fileHash2 = new Hashtable();
	final static String FileNames = "_FileNames";
	
    private static String recipientHost = "localhost";
    private static int recipientPort = 5555;
    private static UDPOutputStream udpOut = null;
    
//---------------------------------------------------------------------------------	
    public udpFolder() {
    	
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
		    if ((value = ah.getOption('a')) != null) recipientHost = value;
		    if ((value = ah.getOption('p')) != null) recipientPort = Integer.parseInt(value);
		    if ((value = ah.getOption('F')) != null) fileFilter = value;
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
	    		"udpFolder"+
	    		"\n -f<folder>      \tdefault="+folderName+ 
	    		"\n -a<host>        \tdefault="+recipientHost+
	    		"\n -p<port>        \tdefault="+recipientPort+
	    		"\n -F<filter>      \tdefault="+fileFilter+
	    		"\n -T<timeInterval>\tdefault="+updateInc+
	    		"\n -D<deleteFile>  \tdefault=false"+
	    		"\n -I<sendInitial> \tdefault=false"+
	    		"\n -G<negateFilter>\tdefault=false");
    	
	    if(printHelp) System.exit(0);
	    else	System.out.println("Running...");
	    
        try {
        	startSource(InetAddress.getByName(recipientHost), recipientPort);	// connect to UDP port
        } catch(Exception e) {
        	e.printStackTrace();
        	System.exit(0);
        };

        pattern = Pattern.compile
        			(replaceWildcards(fileFilter));	// do this once in advance
    
    	initializeFolder();			// initialize file hashtable
        new udpFolder();			// start the timer check 
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
	  // open UDP socket/port
	  
	  private static void startSource(InetAddress iAdd, int port) throws Exception  {
			udpOut = new UDPOutputStream(iAdd, port); 
	  }
	  
	  private static void stopSource() {
			if (udpOut != null) {
			    try {
				udpOut.close();
			    } catch (Exception e) {}
			    udpOut = null;
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
		
//		System.err.println("udpFolder initialized, matches found: "+nfound);
	}

//---------------------------------------------------------------------------------	
// check if a file is legal and passes file-filter
	
	private static boolean fileMatch(File file) {
		if(!file.isFile()) return(false);
		
		String fName = file.getName();
		
		Matcher matcher = pattern.matcher(fName);
		if(negateFilter) { if(matcher.matches()) return(false);  }
		else 			 { if(!matcher.matches()) return(false); }  

		if(file.length() <= 0) return(false);

		return(true);	
	}
	
//---------------------------------------------------------------------------------	
// do the main file checking and send to UDP if legal update
	
	private synchronized void FileWatch() {
		try {
			// get updated list of files

			File[] listOfFiles = folder.listFiles();
			if(listOfFiles == null) return;

			for(int i=0; i<listOfFiles.length; i++) {
				File file = listOfFiles[i];
				String fileName =  file.getName();
				
				// fis.methods catch already-open file on Windows, not Unix
//				if(!canOpen(fileName))continue;		// this method doesn't seem to help on any OS
				
				if(fileMatch(file)) {
//					String fileName =  file.getName();
					long newMod = file.lastModified();
					long oldMod = 0;
					if(fileHash.containsKey(fileName))
						oldMod = ((Long)fileHash.get(fileName)).longValue();
					
//					fileHash.put(fileName, new Long(newMod));	// new/update entry
					if(newMod > oldMod) {	// it's a go
//						System.err.println("folder update: "+fileName+", newMod: "+newMod+", oldMod: "+oldMod);
				
						byte[] data;
						long fileLength = file.length();
						data = new byte[(int)(fileLength)];	// read file update
						java.io.FileInputStream fis = new java.io.FileInputStream(file);
						int nread = fis.read(data);
						fis.close();
						if(nread > 0) {
							writeData(data);		// write to UDP
							System.err.println("Put file: "+fileName+", size: "+fileLength);
							fileHash.put(fileName, new Long(newMod));	// new/update entry on success
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
		}
	}

//---------------------------------------------------------------------------------
	// utility to test if file is available (i.e. not currently being written)
	// doesnt seem to work bleh
	
	private boolean canOpen(String fname) {
		
		try {
			File wfile = new File(fname);
			java.io.FileOutputStream fos = new java.io.FileOutputStream(wfile);
			fos.close();
		}
		catch(Exception e) {
			System.err.println("file NG: "+fname);
			return(false);
		}
//		System.err.println("file OK: "+fname);
		return(true);
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

//---------------------------------------------------------------------------------
   // Write data out as a UDP packet
  
    private void writeData(byte[] data) throws Exception {
	
    	udpOut.setBufferSize(data.length);
    	udpOut.write(data, 0, data.length);
    	udpOut.flush();
	
    }
}



