
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
	
	private static int iterCount=0;
	private static Timer myTimer;
	public static Source source=null;
	private static long lastMod = 0;
	private static final String TAG = "rbnbFolder";
	private static Pattern pattern;
	
//---------------------------------------------------------------------------------	
    public rbnbFolder() {
    	myTimer = new Timer();
    	myTimer.schedule(new TimeTask(), 1000,(int)(updateInc*1000)); 	// try a delay
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
        	startSource();
        } catch(Exception e) {
        	e.printStackTrace();
        	System.exit(0);
        };

        pattern = Pattern.compile(replaceWildcards(fileFilter));	// do this once in advance
        
        try {
        	new rbnbFolder();
        } finally {
//        	if(source != null) source.CloseRBNBConnection();
        }

    }
    
//---------------------------------------------------------------------------------	
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
	private void FileWatch() {
		java.io.File file=null;
		
		try {
//			if(!Connected) startSource();	// startup may take a while to reload archives
			
			// get updated list of files
//	    	System.err.println("fileFilter: "+fileFilter);
	    	File folder;
	    	try {
	    		folder = new File(folderName);
	    	} catch (Exception e) {
	    		System.err.println("Error on opening folder: "+folderName);
	    		return;
	    	}

			File[] listOfFiles = folder.listFiles();
			if(listOfFiles == null) return;
			
			String fileName;
			Matcher matcher;
//			System.err.println("folder: "+folderName+", nfile: "+listOfFiles.length);
			boolean initMod = (lastMod==0);		// initialization pass
			
			for(int i=0; i<listOfFiles.length; i++) {
				if(!listOfFiles[i].isFile()) continue;
				fileName = listOfFiles[i].getName();
				
				matcher = pattern.matcher(fileName);
				if(negateFilter) { if(matcher.matches()) continue;  }
				else 			 { if(!matcher.matches()) continue; }  
//		        else				   System.err.println("match!, "+fileName);

				// define RBNB channels
				ChannelMap cMap = new ChannelMap();
				String rName = fileName;	// rbnb channel name
				if(commonName != null) rName = commonName;	// common name
				cMap.Add(rName);
				
				file = new java.io.File(folderName+File.separator+fileName); 
				
				long fileLength = file.length();
//				System.err.println("file: "+file+", lastMod: "+lastMod+", fileMod: "+file.lastModified());
				if(fileLength <= 0) continue;
	
				if(initMod) {	// avoid resend at startup
					if(lastMod < file.lastModified()) lastMod = file.lastModified();	
//					System.err.println("File: "+file);
				}
				// do real work after first-pass established mod-times
				if((initMod && sendInitial) || (file.lastModified() > lastMod)) {
					byte[] data;
					data = new byte[(int)(fileLength)];	// read file update
					java.io.FileInputStream fis = new java.io.FileInputStream(file);
					int nread = fis.read(data);
					if(nread > 0) {
						System.err.println("Put file: "+fileName+", rbnbChan: "+rName+", size: "+fileLength);
						cMap.PutDataAsByteArray(0, data);
						source.Flush(cMap);	
					}
					if(file.lastModified() > lastMod) lastMod = file.lastModified();	
					if(!retainFile) {
						boolean status = file.delete();
						if(status) System.err.println("Deleted source file: "+fileName);
						else	System.err.println("Failed to delete source file: "+fileName);
					}
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
			startSource();		// presume it needs to reconnect?
		}
		
//		if(file != null) lastMod = file.lastModified();	// catch lastMod time after full sweep
	}
  
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




