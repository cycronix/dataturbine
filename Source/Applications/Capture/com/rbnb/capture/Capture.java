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


/*
  *****************************************************************
  ***								***
  ***	Name :	Capture		(Capture from UDP/multicast)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998 - May, 2005			***
  ***								***
  ***	Copyright 1998, 1999, 2005 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class captures data from a UDP or multicast	***
  ***	socket and sends it to the RBNB.			***
  ***								***
  ***	Modification History :					***
  ***	   05/04/1999 - INB					***
  ***		Compute an exponential average for the bytes/	***
  ***		second display rather than an average of the	***
  ***		entire period.					***
  ***	   05/27/1999 - INB					***
  ***		Modified to use new API access methods.		***
  ***      December 1999/January 2000 - UCB                     ***
  ***           Made various changes to Capture.  Added menus   ***
  ***           and dialog boxes to allow the user to make      ***
  ***           changes to the command-line options at run time:***
  ***           setUpFrame() (some of the code here was         ***
  ***           originally in main()), createMenus(),           ***
  ***           createFileMenu(), createDataMenu(),             ***
  ***           actionPerformed().  Moved most code from run()  ***
  ***           to runCapture() and added stop/start buttons so ***
  ***           capture can be stopped and restarted.           ***
  ***      January 2000 - UCB                                   ***
  ***           Changed command line flags.  -a causes Capture  ***
  ***           to begin running as soon as it is opened. -m    ***
  ***           has been eliminated; IP is parsed to determine  ***
  ***           value.                                          ***
  ***	   December 2000 - JPW					***
  ***		Got rid of hardExit variable.			***
  ***		Added new variable: processID.			***
  ***		Added new constructor which accepts processID	***
  ***		    as an argument.				***
  ***      June 2001 - UCB                                      ***
  ***           In place of the single Channel object used to   ***
  ***           pass data frames between the Datagram receive   ***
  ***           and RBNB send portions of the code, there is    ***
  ***           now a ChannelBuffer object.                     ***
  ***	   02/22/02 - UCB                                       ***
  ***           Capture no longer has fields or methods to      ***
  ***           handle setting the desired frames/second data   ***
  ***           output rate.  Capture now runs as fast as it    ***
  ***           can, and places each whole received packet into ***
  ***           one out-going RBNB frame.                       ***
  ***	   02/26/02 - UCB                                       ***
  ***           Pressing the Stop button now causes activity to ***
  ***           suspend but does not disconnect the Connection  ***
  ***           or socket.  If the user alters any settings,    ***
  ***           then Capture will disconnect and reconnect.     ***
  ***	   04/05/02 - UCB                                       ***
  ***           Capture has been modified to store its received ***
  ***           UDP data in multiple DataTurbines.              ***
  ***      04/08/02 - UCB                                       ***
  ***           Capture has been modified to capture its        ***
  ***           current time in millis from COM.Creare.Utility.-***
  ***           RBNBClock.currentTimeMillis().                  ***
  ***	   04/25/02 - UCB                                       ***
  ***           The multiple output DataTurbines are now        ***
  ***           restricted to having the same output datapath   ***
  ***           and channel names.                              ***
  ***	   07/23/04 - JPW					***
  ***		Convert Capture to a standalone application	***
  ***		which is RBNB V2.4 compliant.			***
  ***	   05/04/05 - JPW					***
  ***		Duration is no longer stored in ChanInfo	***
  ***		receiveFirst() is no longer called (previously,	***
  ***		    it had been used to initialize start time	***
  ***		    so that duration could be calculated;	***
  ***		    however, it also resulted that the first	***
  ***		    frame wan't sent to the RBNB)		***
  ***		In CaptureUDP.open() where the (non-multicast)	***
  ***		    DatagramSocket is created, if the user has	***
  ***		    entered "0" for a host then this is a	***
  ***		    special case indicating that they don't	***
  ***		    want to bind to a particular address.	***
  ***		In CaptureDatagramIP.receive() I set a timeout	***
  ***		    on the DatagramSocket.receive() call.	***
  ***		Got rid of using the extended classes		***
  ***		    CaptureUDP and CaptureMulticast; directly	***
  ***		    open the socket connections in Capture-	***
  ***		    DatagramIP.open()				***
  ***	   05/06/05 - JPW					***
  ***		    Avoid duplicate timestamps - force each	***
  ***		    frame sent to the RBNB to have a unique	***
  ***		    timestamp.  This change was made in PostRBNB***
  ***								***
  *****************************************************************
*/

package com.rbnb.capture;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.RTPCounter;
import com.rbnb.utility.HostAndPortDialog;
import com.rbnb.utility.RBNBClock;
import com.rbnb.utility.RBNBProcessInterface;
import com.rbnb.utility.SettingsReader;
import com.rbnb.utility.SettingsWriter;
import com.rbnb.utility.Utility;

import java.applet.Applet;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Button;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Capture extends Applet implements ActionListener {

    public static final String[] DEFAULT_RBNB = new String[]{"localhost:3333"};
    public static final String DEFAULT_PATH = "Source";
    public static final String DEFAULT_CHANNEL = "capData";

    public static final boolean DEFAULT_ARCHIVE = false;
    public static final int DEFAULT_CACHE_SIZE = 10000;
    public static final int DEFAULT_ARCHIVE_SIZE = 0;

    public static final boolean DEFAULT_AUTORUN = false;
    public static final boolean DEFAULT_USE_SETS_FILE = false;

    public static final String SETTINGS_FILE = "CaptureSettings.dat";

  /* Fields: */
					    // Display packets received + sent.
  TextField		      tPacketsProcessed = null,
					    // Display bytes transferred.
			      tBytesTransferred = null,
					    // Display start time.
			      tStartTime = null,
					    // Display run time.
			      tRunTime = null,
					    // Display bytes/second.
			      tBytesSecond = null;


  /* Private fields: */
					    // Capture from multicast?
  private boolean	      multicast = false,
					    // Are we running as an app?
                              applicationRun = false;
					    // Archive the data?
  private boolean             archive = DEFAULT_ARCHIVE;
					    // Frames to put in the cache.
  private int		      cacheFrames = DEFAULT_CACHE_SIZE,
					    // Frames to put in the archive.
			      archiveFrames = DEFAULT_ARCHIVE_SIZE;
					    // Name of the RBNB.
  private String[]	      rbnbName = DEFAULT_RBNB;
					    // Name to give the channel.
  private String              channelName = DEFAULT_CHANNEL,
					    // The data path.
                              dataPath = DEFAULT_PATH,
					    // Capture from socket.
                              socketName = null; 

  private Font                guiFont = new Font("Dialog", Font.PLAIN, 12);
  //EMF 11/23/00: made frame public so can be accessed by plugin wrapper
  //              when starting Capture
  public Frame                frame = null;

  private volatile boolean                  // stop the Capture
                              stopRequested = false,
                                            // begin running upon start-up
                              autorun = DEFAULT_AUTORUN;

  private boolean             useSettingsFile = DEFAULT_USE_SETS_FILE;

  // JPW 07/23/2004: convert from Connection to Source
  Source[]                    connection = null;
  CaptureDatagramIP           cdIP = null;
  PostRBNB                    poster = null;
  DisplayStats                statDisplay = null;

  Button                      startB,  
                              stopB;

    // 02/22/02 UCB - changesMade is set true whenever the user changes
    // any input parameters.
  private boolean             changesMade = true;

  private Thread              captureThread = null;

  public static final int     OK = 1,
                              CANCEL = 2;

  // JPW 12/18/2000: Add processID variable; used in call to RBNBProcess.exit()
  public RBNBProcessInterface processID = null;

/*
  *****************************************************************
  ***								***
  ***	Name :	Capture		(Constructor: defaults)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a new Capture from the	***
  ***	default values. It is used when the Capture is run	***
  ***	as an applet.						***
  ***								***
  *****************************************************************
*/
  public Capture() {
      // NOTE: DO NOT CALL THIS METHOD EXCEPT TO RUN
      // CAPTURE AS AN APPLET.

      // 02/22/02 UCB - try to initialize the socket name.
      initSocketName(null);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	Capture		(Constructor: from values)	***
  ***	By   :	U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	E-Scan II					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds the Capture from the		***
  ***	input values.						***
  ***								***
  ***	Input :							***
  ***	   rbnbNameI[]		The names of the RBNBs.		***
  ***	   channelNameI 	The name of the channels.	***
  ***	   socketNameI		The name of the capture		***
  ***				socket.				***
  ***	   dataPathI		The desired data path.		***
  ***	   cacheFramesI 	# frames in caches.		***
  ***	   archiveI		Archive data?			***
  ***	   archiveFramesI	# frames in archives.		***
  ***								***
  ***								***
  *****************************************************************
*/
  public Capture
    (String[] rbnbNameI,
     String channelNameI,
     String socketNameI,
     String dataPathI,
     int cacheFramesI,
     boolean archiveI,
     int archiveFramesI,
     boolean autorunI,
     boolean useSettingsFileI,
     RBNBProcessInterface processIDI) {

      this(rbnbNameI, 
	   channelNameI, 
	   socketNameI, 
	   dataPathI,
	   cacheFramesI, 
	   archiveI, 
	   archiveFramesI, 
	   autorunI,
	   useSettingsFileI);
      processID = processIDI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	Capture		(Constructor: from values)	***
  ***	By   :	U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	E-Scan II					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds the Capture from the		***
  ***	input values.						***
  ***								***
  ***	Input :							***
  ***	   rbnbNameI[]		The names of the RBNBs.		***
  ***	   channelNameI 	The name of the channels.	***
  ***	   socketNameI		The name of the capture		***
  ***				socket.				***
  ***	   dataPathI		The desired data path.		***
  ***	   cacheFramesI 	# frames in caches.		***
  ***	   archiveI		Archive data?			***
  ***	   archiveFramesI	# frames in archives.		***
  ***								***
  ***								***
  *****************************************************************
*/
  public Capture
    (String[] rbnbNameI,
     String channelNameI,
     String socketNameI,
     String dataPathI,
     int cacheFramesI,
     boolean archiveI,
     int archiveFramesI,
     boolean autorunI,
     boolean useSettingsFileI) {

    applicationRun = true;
    rbnbName = rbnbNameI;
    channelName = channelNameI;

    // 02/22/02 UCB - initialize the socket name.
    initSocketName(socketNameI);

    dataPath = dataPathI;
    cacheFrames = cacheFramesI;
    archive = archiveI;
    archiveFrames = archiveFramesI;
    autorun = autorunI;

    if (useSettingsFileI &&
	SettingsReader.settingsExist(SETTINGS_FILE)) {

	useSettingsFile = true;
	loadSettings();
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	loadSettings                                    ***
  ***	By   :	U. C. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method loads all possible settings from the     ***
  ***   SETTINGS_FILE.                                          ***
  ***								***
  *****************************************************************
*/
    private void loadSettings() {
	// Create a settings hashtable to populate.
	Hashtable hash = createSettingsHash();
	
	// Use SettingsReader to populate the Hashtable with
	// the saved settings.
	// NOTE THAT SettingsReader.settingsExist(SETTINGS_FILE)
	// SHOULD BE TRUE BEFORE loadSettings() IS CALLED.
	SettingsReader sr = new SettingsReader();
	sr.setMatchHash(hash);
	try {
	    sr.readSettingsFrom(SETTINGS_FILE);
	} catch (IOException ioe) {
	    // do not report the error?
	    return;
	}
	hash = sr.getMatch();

	// reset all values according to those in the Hashtable.
	// It is OK to ignore any exceptions, as all values were already
	// initialized; if any bad values are in the file,
	// they will not be applied.
	rbnbName = Utility.unpack((String) hash.get("RBNB"), "\n");
	dataPath = (String) hash.get("DataPath");
	channelName = (String) hash.get("Channel");
	initSocketName((String) hash.get("SocketName"));
	try {
	    cacheFrames = Integer.parseInt((String) hash.get("CacheFrames"));
	} catch (NumberFormatException nfe) {}
	try {
	   archiveFrames = Integer.parseInt((String)hash.get("ArchiveFrames"));
	} catch (NumberFormatException nfe2) {}
	archive = Boolean.valueOf((String) hash.get("Archive")).booleanValue();
	autorun = Boolean.valueOf((String) hash.get("AutoRun")).booleanValue();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	saveSettings                                    ***
  ***	By   :	U. C. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method saves all settings to the SETTING_FILE.  ***
  ***								***
  *****************************************************************
*/
    private void saveSettings() {
	// Create a settings hashtable.
	Hashtable hash = createSettingsHash();
	
	// Use SettingsWriter to save the settings Hashtable.
	// NOTE THAT SettingsWriter.settingsExist(SETTINGS_FILE)
	// SHOULD BE TRUE BEFORE saveSettings() IS CALLED.
	SettingsWriter sw = new SettingsWriter();
	sw.setSettingsHash(hash);
	try {
	    sw.writeSettingsTo(SETTINGS_FILE);
	} catch (IOException ioe) {
	    // do not report the error?
	    return;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createSettingsHash                              ***
  ***	By   :	U. C. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates a settings Hashtable, for use    ***
  ***   in loadSettings() and saveSettings().                   ***
  ***								***
  *****************************************************************
*/
    private Hashtable createSettingsHash() {
	Hashtable setHash = new Hashtable();

	StringBuffer rbnbNames = new StringBuffer();
	for (int i = 0; i < rbnbName.length; i++) {
	    rbnbNames.append(rbnbName[i]);
	    rbnbNames.append("\n");
	}
	setHash.put("RBNB", rbnbNames.toString());
	setHash.put("DataPath", dataPath);
	setHash.put("Channel", channelName);
	setHash.put("SocketName", socketName);
	setHash.put("CacheFrames", String.valueOf(cacheFrames));
	setHash.put("ArchiveFrames", String.valueOf(archiveFrames));
	setHash.put("Archive", String.valueOf(archive));
	setHash.put("AutoRun", String.valueOf(autorun));

	return setHash;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	main		(Application main)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This static routine provides the main program for	***
  ***	the Capture application. It accepts the command-	***
  ***	line arguments and creates an Capture object.		***
  ***								***
  ***	Input :							***
  ***	   argv			The command line arguments.	***
  ***								***
  *****************************************************************
*/
  public final static void main(String args[]) {
    String  rbnbNameL = DEFAULT_RBNB[0],
	    channelNameL = DEFAULT_CHANNEL,
	    socketNameL = null,
	    dataPathL = DEFAULT_PATH;
    boolean archiveL = DEFAULT_ARCHIVE,
	    autorunL = DEFAULT_AUTORUN,
	    useSettingsFileL = DEFAULT_USE_SETS_FILE;
    int	    cacheFramesL = DEFAULT_CACHE_SIZE,
	    archiveFramesL = DEFAULT_ARCHIVE_SIZE;

    /* Parse the command line arguments. */
    for (int idx = 0; idx < args.length; ++idx) {

      /* If an illegal argument is provided, show the syntax. */
      if ((args[idx].charAt(0) != '-') || (args[idx].length() < 2)) {
	printUsage();
        //EMF 11/24/00: switched to RBNBProcess.exit
        com.rbnb.utility.RBNBProcess.exit(-1);
	//System.exit(-1);
      }

      /* Determine where the value is. */
      int idx1 = idx,
	  idx2 = 0;

      if (args[idx].length() == 2) {
	++idx1;
      } else {
	for (idx2 = 2; idx2 < args[idx].length(); ++idx2) {
	  if ((args[idx1].charAt(idx2) != ' ') &&
	      (args[idx1].charAt(idx2) != '\t')) {
	    break;
	  }
	}
	if (idx2 == args[idx1].length()) {
	  ++idx1;
	  idx2 = 0;
	}
      }

      /* Handle the various recognized switches. */
      switch (args[idx].charAt(1)) {
	case 'r':
	  rbnbNameL = args[idx1].substring(idx2);
	  idx = idx1;
	break;

	case 'c':
	  channelNameL = args[idx1].substring(idx2);
	  idx = idx1;
	break;

	case 'p':
	  socketNameL = args[idx1].substring(idx2);
	  idx = idx1;
	break;

	case 'D':
	  dataPathL = args[idx1].substring(idx2);
	  idx = idx1;
	break;

	case 'C':
	  cacheFramesL = Integer.parseInt(args[idx1].substring(idx2));
	  idx = idx1;
	break;

	case 'A':
	  archiveL = true;
	  archiveFramesL = Integer.parseInt(args[idx1].substring(idx2));
	  idx = idx1;
	break;

        case 'R':
	  System.err.println("Use of the -R flag has been deprecated.");
	  idx = idx1;
	break;

        case 'a':
	  autorunL = true;
	break;

      case 'f':
	  if (SettingsReader.settingsExist(Capture.SETTINGS_FILE)) {
	      useSettingsFileL = true;
	  } else {
	    System.err.println("Unable to find file " + Capture.SETTINGS_FILE);
	    System.err.println("Proceeding without the settings file.");
	  }
	  break;

        case '?':
	  // print usage
	  printUsage();
	  com.rbnb.utility.RBNBProcess.exit(-1);
	break;

        default:
	  System.err.println("Unrecognized flag: " + args[idx]);
	  printUsage();
	  //EMF 11/24/00: switched to RBNBProcess.exit
	  com.rbnb.utility.RBNBProcess.exit(-1);
	  //System.exit(-1);
      }
    }

    /* Add the Capture. */
    // JPW 05/04/2005: Send the constructor a String array
    String[] rbnbNames = null;
    if ( (rbnbNameL != null) && (!rbnbNameL.trim().equals("")) ) {
	rbnbNames = new String[1];
	rbnbNames[0] = rbnbNameL;
    }
    Capture capture = new Capture
      (rbnbNames,
       channelNameL,
       socketNameL,
       dataPathL,
       cacheFramesL,
       archiveL,
       archiveFramesL,
       autorunL,
       useSettingsFileL);
    capture.setUpFrame();

    capture.init();
    capture.start();
    capture.frame.show();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	printUsage                                      ***
  ***	By   :	U. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Print the flags for the command line.                ***
  ***								***
  *****************************************************************
*/
    private static void printUsage() {
	System.err.println("Syntax: java rbnbCapture");
	System.err.println("             [-r <RBNB host>:<RBNB port>]");
	System.err.println("             [-c <RBNB channel name>]");
	System.err.println("             [-p <IP>:<port>");
	System.err.println("             [-D <data path>]");
	System.err.println("             [-C <cache frames>]");
	System.err.println("             [-A <archive frames>");
	System.err.println("             [-a] (begin running upon start-up)");
	System.err.println("             [-f] (use settings from " +
			                                 SETTINGS_FILE + ")");
	System.err.println("             [-?] (print usage)");
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	initSocketName                                  ***
  ***	By   :	U. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Initialize the socket name and multicast flag.	***
  ***								***
  *****************************************************************
*/
    private void initSocketName(String sockI) {

	// 1) Set the socketname
	if (sockI != null && !sockI.equals("")) {
	    socketName = sockI; 
	} else {
	    // JPW 05/04/2005: Set default socketName to "0"; this
	    //                 indicates that we won't bind to a
	    //                 particular address
	    // InetAddress iadd = InetAddress.getLocalHost();
	    // socketName = iadd.getHostAddress();
	    socketName = new String("0");
	}

	// 2) Check whether a port is included in the socketname.
	if (socketName.indexOf(":") == -1) {
	    socketName = new String(socketName + ":1365");
	}

	// 3) Are we multicasting?
	multicast = isMulticast(socketName);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isMulticast                                     ***
  ***	By   :	U. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Is the input sockName multicast?                     ***
  ***								***
  *****************************************************************
*/
    private static boolean isMulticast(String sockName) {
	int index = sockName.indexOf('.');
	if (index < 1) {
	    // IP address is not well-formed.  Return false, for now.
	    return false;
	}
	
	int tester;
	try {
	    tester = Integer.parseInt(sockName.substring(0, index));
	} catch (NumberFormatException e) {
	    // IP address is not well-formed. Return false, for now.
	    return false;
	}
	if ( (tester > 223) && (tester < 240) ) {
	    return true;
	}
	return false;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	init		(Initialize the capture)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method initializes the capture by ensuring that	***
  ***	all of the values are set.				***
  ***								***
  *****************************************************************
*/
  public void init() {

    /* If we are an applet, get the parameter values. */
    if (!applicationRun) {
      String parameterValue;

      /* If the RBNB name is set, grab it. */
      if ((parameterValue = getParameter("rbnbName")) != null) {
	rbnbName = new String[] {parameterValue};
      }

      /* If the channel name is set, grab it. */
      if ((parameterValue = getParameter("channelName")) != null) {
	channelName = parameterValue;
      }

      /* If the multicast flag is set, grab it. */
      if ((parameterValue = getParameter("multicast")) != null) {
	multicast = Boolean.valueOf(parameterValue).booleanValue();
      }

      /* If the socket name is set, grab it. */
      if ((parameterValue = getParameter("socketName")) != null) {
	initSocketName(parameterValue);
      }

      /* If the data path is set, grab it. */
      if ((parameterValue = getParameter("dataPath")) != null) {
	dataPath = parameterValue;
      }

      /* If the number of cache frames is set, grab it. */
      if ((parameterValue = getParameter("cacheFrames")) != null) {
	cacheFrames = Integer.parseInt(parameterValue);
      }

      /* If the archive flag is set, grab it. */
      if ((parameterValue = getParameter("archive")) != null) {
	archive = Boolean.valueOf(parameterValue).booleanValue();
      }

      /* If the number of archive frames is set, grab it. */
      if ((parameterValue = getParameter("archiveFrames")) != null) {
	archiveFrames = Integer.parseInt(parameterValue);
      }
    }

    /* Fill in any blanks. */
    if (rbnbName == null) {
      rbnbName = new String[] {"localhost:3333"};
    }
    if (channelName == null) {
      channelName = DEFAULT_CHANNEL;
    }
    if (dataPath == null) {
	dataPath = DEFAULT_PATH;
    }
    if (socketName == null) {
      initSocketName(null);
    }

  }

/*
  *****************************************************************
  ***								***
  ***	Name :	Capture.setUpFrame()		              	***
  ***	By   :	Ursula Bergstrom     (Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1999					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Sets up capture's frame and menus.    		***
  ***								***
  ***	Modification History :					***
  ***	   08/07/2000 - INB					***
  ***		UNIX seems to need more vertical space, so	***
  ***		Windows check below.				***
  ***      11/23/00 EMF: made method public so plugin wrapper   ***
  ***                    can start capture                      ***
  ***								***
  *****************************************************************
*/
  public void setUpFrame() {
    frame = new Frame(title());
    if (System.getProperty("os.name").indexOf("Windows") == 0) {
      frame.setSize(400,200);
    } else {
      frame.setSize(400,275);
    }

    MenuBar mbar = new MenuBar();
    createMenus(mbar);
    frame.setMenuBar(mbar);
    
    frame.add(this);
    frame.addWindowListener(new CloseClass(this));
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	start		(Start Capture)			***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts the Capture applet and a		***
  ***	thread to run the actual capture.			***
  ***								***
  *****************************************************************
*/
  public void start() {
    GridLayout grid = new GridLayout(6,2);

    /* Use the grid layout. */
    setLayout(grid);

    startB = new Button ("Start");
    startB.addActionListener(this);
    stopB = new Button ("Stop");
    stopB.addActionListener(this);

    /* Create various display fields. */
    add(new Label("Start Time:"));
    add(tStartTime = new TextField(24));
    add(new Label("Run Time:"));
    add(tRunTime = new TextField(24));
    add(new Label("Packets Received:"));
    add(tPacketsProcessed = new TextField(24));
    add(new Label("Bytes Transferred:"));
    add(tBytesTransferred = new TextField(24));
    add(new Label("Bytes/Second:"));
    add(tBytesSecond = new TextField(24));
    add(startB);
    add(stopB);

    statDisplay = new DisplayStats(this);

    if (autorun) { // immediately begin capture
      startB.setEnabled(false);
      // disable File|Open..., File|Socket, and the Data menu
      frame.getMenuBar().getMenu(0).getItem(0).setEnabled(false); //Open...
      frame.getMenuBar().getMenu(0).getItem(1).setEnabled(false); //Socket
      frame.getMenuBar().getMenu(1).setEnabled(false); // Data menu
      
      /* Create a thread to run the actual capture. */
      try {
	  startCapture();
      } catch (Exception e) {
	  e.printStackTrace();
	  errorBox(e.getMessage(), true);
      }
    }
    else {
      stopB.setEnabled(false);
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	startCapture	     (starts running capture)   ***
  ***	By   :	Ursula Bergstrom     (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II		      			***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method initializes the captureThread and starts ***
  ***   it.                                                     ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
  public void startCapture() throws Exception {
      if (changesMade || connection == null) {
	  openConnection();
      }
      
      Runnable r = new Runnable() {
	  public void run() {
	      runCapture();
	  }
      };
	  
      captureThread = new Thread(r);
      captureThread.start();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :  openConnection                                  ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	February 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method connects to the DataTurbine.             ***
  ***								***
  ***	Modification History:					***
  ***	07/23/2004	JPW	Extensive changes to bring up	***
  ***				from RBNB V1.3 to RBNB V2.4	***
  ***								***
  *****************************************************************
*/
    private void openConnection() throws Exception {
	
	if (connection != null) {
	    disconnect();
	}
	
	ChannelMap map = null;
	connection = new Source[rbnbName.length];
	
	for (int i = 0; i < rbnbName.length; i++) {
	    System.err.println("Open connection to RBNB at " + rbnbName[i]);
	    connection[i] =
	        new Source(cacheFrames,
	                   archive ? "create" : "none",
	                   archiveFrames);
	    connection[i].OpenRBNBConnection(rbnbName[i],dataPath);
	    
	    // Register channel
	    map = new ChannelMap();
	    map.Add(channelName);
	    connection[i].Register(map);
	    
	}
	
	poster = new PostRBNB(this, connection, channelName);
	
	// Connect to the socket
	cdIP = new CaptureDatagramIP();
	cdIP.connect(socketName, multicast);
	
	// 11/13/01 UCB - inititalize the stat display
	statDisplay.setStart(System.currentTimeMillis());
	
	changesMade = false;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	runCapture	       (Run the actual capture)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998, 1999 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method runs the actual capture of data.		***
  ***								***
  ***	Modification History :					***
  ***	   05/27/1999 - INB					***
  ***		Modified to use new API access methods.		***
  ***      01/05/2000 - UCB                                     ***
  ***           This used to be Capture.run(); it was later     ***
  ***           necessary to move the functionality into its    ***
  ***           own method.                                     ***
  ***								***
  *****************************************************************
*/
  private void runCapture() {
      ChanInfo chanInfo = null;
      
      /* Start the data posting thread and statistics display thread. */
      poster.startPoster();
      statDisplay.startDisplayStats();
      
      try {
	  
	  /* 11/08/01 UCB - initialize cdIP */
	  /* 05/04/05 JPW - remove receiveFirst(); this was a problem that */
	  /*                the first received frame wasn't being sent to  */
	  /*                the RBNB                                       */
	  // cdIP.receiveFirst();
	  
	  stopRequested = false;
	  while (!stopRequested) {
	      
	      // Get data from the socket.
	      chanInfo = cdIP.receive();
	      
	      // Make sure we got some data
	      if ( (chanInfo.data == null) || (chanInfo.data.length == 0) ) {
		  // JPW 05/04/2005: change from break to continue
		  // break;
		  continue;
	      }
	      
	      // Update the statistics fields.
	      statDisplay.updateStatistics(
	          chanInfo.start, 1, chanInfo.data.length);
	      
	      // Send the data to the RBNB.
	      poster.passToPoster(chanInfo);
	      
	  }
	  
      } catch (Exception e) {
	  e.printStackTrace();
	  errorBox(e.getMessage(), true);
      }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	Capture.requestStop()	   (Stop the capture)   ***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	January, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method stops the capture of data.		***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
  private void requestStop() {
      
    stopRequested = true;
    if (statDisplay != null) {
      statDisplay.requestStop();
    }
    if (poster != null) {
      poster.requestStop();
    }
    
    if (captureThread != null && Thread.currentThread() != captureThread) {
	try {
	    System.err.println("Waiting for the capture thread to stop...");
	    captureThread.join();
	} catch (InterruptedException ie) {}
    }
    
    System.err.println("... capture thread has stopped.");
    
    stopB.setEnabled(false);
    startB.setEnabled(true);
    
    // enable File|Open..., File|Socket, and the Data menu
    if (frame!=null) {
      frame.getMenuBar().getMenu(0).getItem(0).setEnabled(true); //Open...
      frame.getMenuBar().getMenu(0).getItem(1).setEnabled(true); //Socket
      frame.getMenuBar().getMenu(1).setEnabled(true); // Data menu
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect                                      ***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method disconnects Capture from the RBNB and    ***
  ***   UDP port.                                               ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
    private void disconnect() {
	if (connection != null) {
	    for (int i = 0; i < connection.length; ++i) {
		if (connection[i] != null) {
		    connection[i].CloseRBNBConnection();
		}
	    }
	    connection = null;
	}

	if (cdIP != null) {
	    cdIP.disconnect();
	    cdIP = null;
	}
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  Capture.actionPerformed                         ***
  ***   By   :  Ursula C. Bergstrom  (Creare Inc., Hanover, NH) ***
  ***   For  :  RBNB                                            ***
  ***   Date :  December, 1999                                  ***
  ***                                                           ***
  ***   Copyright 1999 Creare Inc., Hanover, N.H.               ***
  ***   All rights reserved.                                    ***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Handle  menu actions from the user.                 ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***       event       The event object describing the event   ***
  ***                   that has occured.                       ***
  ***                                                           ***
  *****************************************************************
*/
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	
	//
	// File->Open menu item
	//
	if (label.equals("Open...")) {
	    MultiHostAndPortDlg mhpd =
		new MultiHostAndPortDlg(
		    frame,
		    "RBNB",
		    "Specify RBNB Connections",
		    rbnbName,
		    applicationRun);
	    mhpd.show();
	    if (mhpd.state == MultiHostAndPortDlg.OK) {
		rbnbName = mhpd.hosts;
		changesMade = true;
	    }
	    mhpd.dispose();
	}
	
	//
	// File->Socket menu item
	//
	else if (label.equals("Socket")) {
	    SocketDialog sd =
		new SocketDialog(
		    frame,
		    "Input Datagram Packet Socket",
		    socketName, 
		    true);
	    sd.show();
	    if (sd.state == SocketDialog.OK) {
		initSocketName(sd.IPadd);
		changesMade = true;
		frame.setTitle(title());
	    }
	    sd.dispose();
	}
	
	//
	// File->Close menu item
	//
	else if (label.equals("Close")) {
	    requestStop();
	    disconnect();
	    if (useSettingsFile &&
		SettingsWriter.settingsExist(SETTINGS_FILE))
	    {
		saveSettings();
	    }
	}
	
	//
	// File->Exit menu item
	//
	else if (label.equals("Exit")) {
	    exitAction();
	}
	
	//
	// Data->Target menu item
	//
	else if (label.equals("Target")) {
	    TargetDialog td =
		new TargetDialog(
		    frame,
		    dataPath,
		    channelName,
		    applicationRun);
	    td.show();
	    if (td.state == TargetDialog.OK) {
		dataPath = td.path;
		channelName = td.chan;
		changesMade = true;
	    }
	    td.dispose();
	}
	
	//
	// Data->Settings menu item
	//
	else if (label.equals("Settings")) {
	    DataSettingsDialog dsd =
		new DataSettingsDialog(
		    frame,
		    cacheFrames,
		    archiveFrames,
		    true);
	    dsd.show();
	    if (dsd.state == DataSettingsDialog.OK) {
		cacheFrames = dsd.cacheFrames;
		archiveFrames = dsd.archFrames;
		archive = (archiveFrames != 0);
		changesMade = true;
	    }
	    dsd.dispose();
	}
	
	//
	// Start button
	//
	else if (label.equals("Start")) {
	    stopB.setEnabled(true);
	    startB.setEnabled(false);
	    
	    // disable File|Open..., File|Socket, and the Data menu
	    frame.getMenuBar().getMenu(0).getItem(0).setEnabled(false);
	    frame.getMenuBar().getMenu(0).getItem(1).setEnabled(false);
	    frame.getMenuBar().getMenu(1).setEnabled(false);
	    
	    // reset the title
	    frame.setTitle(title());
	    
	    try {
		startCapture();
	    } catch (Exception e) {
		e.printStackTrace();
		errorBox(
		    "Encountered an error while starting" +
		    (e.getMessage() != null ? ": " + e.getMessage() : "."), 
		    true);
	    }
	}
	
	//
	// Stop button
	//
	else if (label.equals("Stop")) {
	    requestStop();
	}
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	exitAction                                      ***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method shuts down the application.              ***
  ***								***
  *****************************************************************
*/
    public void exitAction() {
	requestStop();
	disconnect();

	if (useSettingsFile && SettingsWriter.settingsExist(SETTINGS_FILE)) {
	    saveSettings();
	}

        //EMF 11/24/00: switched to RBNBProcess.exit
        if (applicationRun) {
          com.rbnb.utility.RBNBProcess.exit(0,processID);
        }
        if (frame!=null) {
          frame.setVisible(false);
          frame.dispose();
          frame=null;
        }
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	title		(Create a title string)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates a string to be used as a title	***
  ***	for this Capture object. It is based on the various	***
  ***	name fields.						***
  ***								***
  ***	Returns :						***
  ***	   title		The title field.		***
  ***								***
  *****************************************************************
*/
  private String title() {
      
      // JPW 05/04/2005: Change the info in the title
      
      // NOTE: If address = "0", this is a special case indicating
      //       that we won't bind to any particular address.  This
      //       is broadcast mode
      
      String title = "rbnbCapture   ";
      
      if (multicast) {
	  title =
	      new String(
	          title + "accept Multicast packets from group " + socketName);
      } else {
	  String addressStr = null;
	  String portStr = "N/A";
	  int colonIdx = socketName.indexOf(':');
	  if (colonIdx != -1) {
	      // port is provided
	      addressStr = socketName.substring(0,colonIdx);
	      portStr = socketName.substring(colonIdx + 1);
	  } else {
	      // no port provided
	      addressStr = socketName;
	  }
	  if (addressStr.equals("0")) {
	      title =
		  new String(
		      title +
		      "accept Broadcast packets at port " +
		      portStr);
	  }
	  else {
	      title =
		  new String(
		      title +
		      "accept Unicast packets directed to " +
		      socketName);
	  }
	  
      }
      
      return title;
      
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  Capture.createMenus                             ***
  ***   By   :  Ursula Bergstrom  (Creare Inc., Hanover, NH)    ***
  ***   For  :  FlyScan                                         ***
  ***   Date :  December, 1999                                  ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc., Hanover, N.H.               ***
  ***   All rights reserved.                                    ***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Create the top level menu system for the Capture    ***
  ***   GUI.                                                    ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***       mbar        Top level menu bar for the GUI.         ***
  ***                                                           ***
  ***   Modification History :                                  ***
  ***                                                           ***
  *****************************************************************
*/
 private void createMenus(MenuBar mbar) {
    mbar.add(createFileMenu());
    mbar.add(createDataMenu());
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  Capture.createFileMenu                          ***
  ***   By   :  U. Bergstrom  (Creare Inc., Hanover, NH)        ***
  ***   For  :  FlyScan                                         ***
  ***   Date :  December, 1999                                  ***
  ***                                                           ***
  ***   Copyright 1999 Creare Inc., Hanover, N.H.               ***
  ***   All rights reserved.                                    ***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Create the File menu.                               ***
  ***                                                           ***
  ***   Output :                                                ***
  ***       fileMenu    The File menu that this method creates. ***
  ***                                                           ***
  *****************************************************************
*/
  private Menu createFileMenu() {
    MenuItem menuItem;
    Menu fileMenu = new Menu("File");
    fileMenu.setFont(guiFont);
    menuItem = new MenuItem("Open...");
    menuItem.setFont(guiFont);
    menuItem.addActionListener(this);
    menuItem.setEnabled(true);
    fileMenu.add(menuItem);
    menuItem = new MenuItem("Socket");
    menuItem.setFont(guiFont);
    menuItem.addActionListener(this);
    menuItem.setEnabled(true);
    fileMenu.add(menuItem);
    menuItem = new MenuItem("Close");
    menuItem.setFont(guiFont);
    menuItem.addActionListener(this);
    menuItem.setEnabled(true);
    fileMenu.add(menuItem);
    menuItem = new MenuItem("Exit");
    menuItem.setFont(guiFont);
    menuItem.addActionListener(this);
    menuItem.setEnabled(true);
    fileMenu.add(menuItem);
    
    return fileMenu;
  }
  
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  Capture.createDataMenu                          ***
  ***   By   :  U. Bergstrom    (Creare Inc., Hanover, NH)      ***
  ***   For  :  RBNB                                            ***
  ***   Date :  November, 1999                                  ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc., Hanover, N.H.               ***
  ***   All rights reserved.                                    ***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Create the Data menu.                               ***
  ***                                                           ***
  ***   Output :                                                ***
  ***       dataMenu    The data menu that this method creates. ***
  ***                                                           ***
  *****************************************************************
*/
  private Menu createDataMenu() {
    MenuItem menuItem;
    Menu dataMenu = new Menu("Data");
    dataMenu.setFont(guiFont);

    menuItem = new MenuItem("Target");
    menuItem.setFont(guiFont);
    menuItem.addActionListener(this);
    menuItem.setEnabled(true);
    dataMenu.add(menuItem);
    menuItem = new MenuItem("Settings");
    menuItem.setFont(guiFont);
    menuItem.addActionListener(this);
    menuItem.setEnabled(true);
    dataMenu.add(menuItem);

    return dataMenu;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	errorBox()                               	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	February, 2002                   		***
  ***								***
  ***	Copyright 2002 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Creates a dialog box informing user of an error.      ***
  ***								***
  *****************************************************************
*/
    void errorBox(String err, boolean terminal) {
      if (terminal) {
	  requestStop();
	  disconnect();
      }

      String[] strArray = new String[2];
      strArray[0] = "An error occurred:";
      strArray[1] = err;
      InfoDialog infoDialog =
	  new InfoDialog(frame,
			 true,
			 "Error",
			 strArray);
      infoDialog.show();
      infoDialog.dispose();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	DisplayStats	(Display statistics)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998, 1999 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This private class displays statistics for the run.	***
  ***								***
  ***	Modification History :					***
  ***	   05/04/1999 - INB					***
  ***		Compute an exponential average for the bytes/	***
  ***		second display rather than an average of the	***
  ***		entire period.					***
  ***	   07/26/2004 - JPW					***
  ***		Upgrade to RBNB V2.X; take out use of Time	***
  ***		class.						***
  ***								***
  *****************************************************************
*/
  private class DisplayStats {

    /* Private fields: */
					    // Statistics updated?
    private boolean	      updated = false;
					    // # packets from socket to RBNB.
    private long	      packetsProcessed = 0,
					    // Bytes from socket to RBNB.
			      bytesTransferred = 0,
					    // Last bytes transferred.
			      lastTransferred = 0;

    private Capture	      parent = null;// Our Capture parent.

    // Time that we got started.
    // JPW 07/27/04: No longer use Time object in RBNB V2.X
    // private Time	      startTime = null;
    private long	      startTime = 0;
    
    // Time of last display.
    // JPW 07/27/04: No longer use Time object in RBNB V2.X
    // private Time              displayTime = null;
    private long              displayTime = 0;
    
    // Exponential average for Bps.
    // JPW 07/27/04: Just calculate an instantaneous bytes/sec;
    //               don't calculate an exponential average (it
    //               didn't appear that the algorithm implemented
    //               was working anyway)
    // private Time              exponentialAverage = null;
    
    // Time of last update.
    private long              now;
    
    private volatile boolean  stopRequested = false;

/*
  *****************************************************************
  ***								***
  ***	Name :	DisplayStats	(Constructor: from Capture)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a display statistics object	***
  ***	for the input Capture object.				***
  ***								***
  ***	Input :							***
  ***	   parentI		The parent Capture object.	***
  ***								***
  *****************************************************************
*/
      DisplayStats(Capture parentI) {
	  parent = parentI;
      }

/*
  *****************************************************************
  ***								***
  ***	Name :	startDisplayStats        			***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II		      			***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method calls runDisplaStats in a new Thread.    ***
  ***								***
  *****************************************************************
*/
      public void startDisplayStats() {
	  Runnable r = new Runnable() {
		  public void run() {
		      runDisplayStats();
		  }
	      };
	  
	  Thread t = new Thread(r);
	  t.start();
      }

/*
  *****************************************************************
  ***								***
  ***	Name :	runDisplayStats	(Run display)			***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998, 1999 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method runs the capture display. It copies the	***
  ***	private statistic fields and then calculates and	***
  ***	displays various statistics.				***
  ***								***
  ***	Modification History :					***
  ***	   05/04/1999 - INB					***
  ***		Compute an exponential average for the bytes/	***
  ***		second display rather than an average of the	***
  ***		entire period.					***
  ***	   05/27/1999 - INB					***
  ***		Modified to use new API access methods.		***
  ***	   07/26/2004 - JPW					***
  ***		Modified to bring up to RBNB V2.X; got rid of	***
  ***		the use of the Time class.			***
  ***      07/27/2004 - JPW                                     ***
  ***           Just calculate an instantaneous bytes/sec; the  ***
  ***           exponential average didn't appear to be working.***
  ***								***
  *****************************************************************
*/
    public void runDisplayStats() {
      
      long packetsProcessedL;
      long bytesTransferredL;
      long nowL;
      
      stopRequested = false;

      /* Loop forever. */
      while (!stopRequested) {
	/* Wait until we get data. */
	synchronized (this) {
	  while (!updated) {
	    try {
	      wait();
	    } catch (InterruptedException e) {
		if (stopRequested) {
		    return;
		}
	    }
	  }
	  packetsProcessedL = packetsProcessed;
	  bytesTransferredL = bytesTransferred;
	  nowL = now;
	  updated = false;
	}
	
	Thread.currentThread().yield();
	
	long runTime = nowL - startTime;
	parent.tRunTime.setText(
	    Double.toString( ( (double)runTime ) / 1000.0 ));
	
	Thread.currentThread().yield();
	
	parent.tPacketsProcessed.setText(
	    Long.toString(packetsProcessedL));
	
	Thread.currentThread().yield();
	
	parent.tBytesTransferred.setText(
	    Long.toString(bytesTransferredL));
	
	Thread.currentThread().yield();
	
	if (runTime != 0) {
	  long deltaBytes = bytesTransferredL - lastTransferred;
	  long deltaTime = 0;
	  if (displayTime == 0) {
	      deltaTime = runTime;
	  } else {
	      deltaTime = nowL - displayTime;
	  }
	  
	  if (deltaTime != 0) {
	      double deltaTimeInSec = ( (double)deltaTime ) / 1000.0;
	      double bytesPerSec = deltaBytes / deltaTimeInSec;
	      // Keep 2 decimal places of precision
	      long tempVal = Math.round(bytesPerSec * 100.0);
	      bytesPerSec = ( (double)tempVal ) / 100.0;
	      parent.tBytesSecond.setText(
	          Double.toString(bytesPerSec));
	  }
	}
        
	lastTransferred = bytesTransferredL;
	displayTime = nowL;
        
	Thread.currentThread().yield();
      }
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	DisplayStats.requestStop()	                ***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method stops DisplayStats safely.		***
  ***								***
  *****************************************************************
*/
      void requestStop() {
	  stopRequested = true;
      }

/*
  *****************************************************************
  ***								***
  ***	Name :	setStart         				***
  ***	By   :	U. Bergstrom	(Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the value of startTime.             ***
  ***								***
  ***	Input :							***
  ***	   startTimeI		The start time of the data	***
  ***				received.			***
  ***								***
  ***	Modification History:					***
  ***	07/26/2004	JPW	Update method to RBNB V2.X	***
  ***				code; this means remove use of	***
  ***				the Time class.			***
  ***								***
  *****************************************************************
*/
      void setStart(long startTimeI) {
	  
	  // JPW 07/26/2004: startTime is a long, not as a Time object
	  // startTime = new Time(startTimeI, (byte) -3);
	  startTime = startTimeI;
	  
	  /* Update the display. */
	  // JPW 07/26/2004: Replace the use of Time.getFormattedString() with
	  //                 a Java Date object.
	  /*
	  parent.tStartTime.setText
	      (startTime.getFormattedString
	       (Time.AbsoluteSeconds1970,
		Time.Full,
		-1));
	  */
	  
	  Date date = new Date(startTime);
	  parent.tStartTime.setText( date.toString() );
          
	  // initialize
	  packetsProcessed = 0;
	  bytesTransferred = 0;
	  lastTransferred = 0;
	  
	  // JPW 07/26/2004: displayTime and exponentialAverage are no
	  //                 longer Time objects
	  // JPW 07/27/2004: exponentialAverage is no longer used
	  // displayTime = null;
	  // exponentialAverage = null;
	  displayTime = 0;
	  // exponentialAverage = 0.0;
      }

/*
  *****************************************************************
  ***								***
  ***	Name :	updateStatistics				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method updates the statistics.			***
  ***								***
  ***	Input :							***
  ***	   nowI			The current time (end time of	***
  ***				the data received).		***
  ***	   packetsI		The number of packets.		***
  ***	   bytesI		The number of bytes.		***
  ***								***
  *****************************************************************
*/
    synchronized void updateStatistics(long nowI,
				       int packetsI,
				       int bytesI) {
      now = nowI;
      packetsProcessed += packetsI;
      bytesTransferred += bytesI;
      updated = true;

      notify();
    }
  } // end class DisplayStats

/*
  *****************************************************************
  ***								***
  ***	Name :	PostRBNB	(Post to the RBNB)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This private class provides a thread to post data	***
  ***	to the RBNB in an asynchronous fashion, allowing us to	***
  ***	double buffer the input socket stream.			***
  ***								***
  ***	July 2001 - UCB                                         ***
  ***             Replaced the old single channel with a        ***
  ***             ChannelBuffer object.  No synchronization on  ***
  ***             calls to the ChannelBuffer are needed.        ***
  ***	05/06/2005 - JPW					***
  ***		  Avoid duplicate timestamps - force each frame	***
  ***		  sent to the RBNB to have a unique timestamp;	***
  ***		  to do this I added the "lastStartTime" member	***
  ***								***
  *****************************************************************
*/
  private class PostRBNB {
    
    // Parent.
    private Capture	        cap = null;
    
    // JPW 07/23/2004: Convert from Connection to Source
    // Connections to the RBNB.
    private Source[]            connection = null;
    
    private String              chanName;
    
    private volatile boolean    stopRequested = false;
    
    // Buffer for storing incoming channels which are waiting
    // to be sent.
    private ChanInfoBuffer      chanBuff = new ChanInfoBuffer(60);
    
    // JPW 05/06/2005 Added lastStartTime to avoid duplicate timestamps
    private double              lastStartTime = -Double.MAX_VALUE;
    
/*
  *****************************************************************
  ***								***
  ***	Name :	PostRBNB	(Constructor: from connection)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a post RBNB object from the	***
  ***	input RBNB connection.					***
  ***								***
  ***	Input :							***
  ***	   connectionI		The connection to the RBNB.	***
  ***								***
  ***	Modification History:					***
  ***	07/23/2004	JPW	Change from using Connection to	***
  ***				using Source			***
  ***								***
  *****************************************************************
*/
    PostRBNB(Capture capI, Source[] connectionI, String chanNameI) {
      cap = capI;
      connection = connectionI;
      chanName = chanNameI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	startPoster              			***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II		      			***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method calls runPoster in a new Thread.         ***
  ***								***
  *****************************************************************
*/
      public void startPoster() {
	  Runnable r = new Runnable() {
		  public void run() {
		      runPoster();
		  }
	      };
	  
	  Thread t = new Thread(r);
	  t.start();
      }

/*
  *****************************************************************
  ***								***
  ***	Name :	runPoster	(Run the post to RBNB object)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998 - January, 1999			***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method runs the post to RBNB object thread. It	***
  ***	gets data from another thread and posts it to the RBNB.	***
  ***								***
  *****************************************************************
*/
    public void runPoster() {
      
      // Loop forever posting data to the RBNB.
      stopRequested = false;
      
      // Map used to send data to RBNB
      ChannelMap sourceMap = new ChannelMap();
      
      while (!stopRequested) {
	  // a blocking call-- can be interrupted with
	  // chanBuff.interruptFetch()
	  ChanInfo cInf = chanBuff.fetchNext();
	  if (cInf == null) {
	      continue;
	  }
	  
	  // Create ChannelMap and send to RBNB
	  try {
	      int chanNumber = sourceMap.Add(chanName);
	      // JPW 05/04/2005: Set duration to 0.0
	      // JPW 05/06/2005: avoid duplicate timestamps - we want each RBNB
	      //                 frame to have a unique timestamp
	      double currStartTime = (cInf.start)/1000.0;
	      if (currStartTime <= lastStartTime) {
		  currStartTime = lastStartTime + 0.001;
	      }
	      lastStartTime = currStartTime;
	      sourceMap.PutTime( currStartTime, 0.0);
	      sourceMap.PutDataAsByteArray(chanNumber, cInf.data);
	      for (int i = 0; i < connection.length; ++i) {
		  // Post the data to the RBNB.
		  connection[i].Flush(sourceMap);
	      }
	      sourceMap.Clear();
	  } catch (Exception e) {
	      e.printStackTrace();
	      cap.errorBox(e.getMessage(), true);
	      break;
	  }
      }
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	PostRBNB.requestStop()	                        ***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method stops PostRBNB safely.	        	***
  ***								***
  *****************************************************************
*/
      void requestStop() {
	  stopRequested = true;
	  chanBuff.interruptFetch();
      }

/*
  *****************************************************************
  ***								***
  ***	Name :	passToPoster	(Pass data to the poster)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method allows another thread to pass data to	***
  ***	the post to RBNB thread. It is synchronized to ensure	***
  ***	that data is passed in a sequential fashion.		***
  ***								***
  ***	Input :							***
  ***	   channelI		The channel of data.		***
  ***								***
  *****************************************************************
*/
      void passToPoster(ChanInfo ci) {
	/* Store the channel of data. */
	chanBuff.addChanInfo(ci);
      }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	CaptureDatagramIP				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This private abstract class provides a framework for	***
  ***	capturing datagram packets.				***
  ***								***
  ***	05/04/2005 - JPW					***
  ***		Remove cInfos array; these ChanInfo objects	***
  ***		were used to calculate duration; we no longer	***
  ***		calculate duration (it is set to 0.0).		***
  ***								***
  *****************************************************************
*/
  private class CaptureDatagramIP {

    /* Fields: */
    DatagramSocket	      socket = null;// The socket.
					    // Time for last data received.
    int			      port = 4444; // The port of the socket.

    InetAddress		      ip = null;    // The IP address of the socket.

    int                       size = 0;
    
    // JPW 05/04/2005  These two ChanInfo objects were stored in order to
    //                 calculate duration; now, duration is just set to 0.0
    // ChanInfo[] cInfos = new ChanInfo[2];
    // int currIdx = 0;
    // int lastIdx = 1;

/*
  *****************************************************************
  ***								***
  ***	Name :	constructor	(Create the object)		***
  ***	By   :	John P. Wilson	(Creare Inc., Hanover, NH)	***
  ***	For  :	AJBI						***
  ***	Date :	May 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Default constructor.					***
  ***								***
  ***								***
  *****************************************************************
*/
      CaptureDatagramIP() {
	  
      }
      
/*
  *****************************************************************
  ***								***
  ***	Name :	connect		(Connect to capture socket)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method connects to the socket from which we	***
  ***	want to get datagram packets.				***
  ***								***
  ***	Input :							***
  ***	   captureFromI		The [<host>][:<port>] to get	***
  ***				datagrams from.			***
  ***								***
  ***	05/04/2005 - JPW					***
  ***		If user enters "0" for a host address, this is	***
  ***		a special case indicating that they don't want	***
  ***		to bind to a particular address.		***
  ***		Add the "bIsMulticast" parameter.		***
  ***								***
  *****************************************************************
*/
    void connect(String captureFromI, boolean bIsMulticast)
    throws UnknownHostException,
           SocketException,
	   IOException {

      /* Parse the input string to find a host and port. */
      String host = "localhost";
      int    colon = captureFromI.indexOf(":");

      if (colon == -1) {
	if (captureFromI.length() > 0) {
	  host = captureFromI;
	}
      } else {
	if (colon > 0) {
	  host = captureFromI.substring(0,colon);
	}
	if (colon < captureFromI.length() - 1) {
	  port = Integer.parseInt(captureFromI.substring(colon + 1));
	}
      }

      /* Try to get the address of the host. */
      // JPW 05/04/2005: host = "0" is a special case where the user is
      //                 indicating not to bind to a particular address
      if (host.equals("0")) {
	  ip = null;
      } else {
	  ip = InetAddress.getByName(host);
      }
      
      /* Open the socket. */
      open(bIsMulticast);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	open		(Open the socket)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	    Opens the socket.					***
  ***								***
  ***	05/04/2005 - JPW					***
  ***		Got rid of using the extended classes		***
  ***		CaptureUDP and CaptureMulticast; directly	***
  ***		open the socket connections here.		***
  ***								***
  *****************************************************************
*/
    
    void open(boolean bIsMulticast) throws SocketException, IOException {
	
	System.err.println("Open socket connection");
	
	if (bIsMulticast) {
	    System.err.println(
	        "Accept Multicast packets from " + ip + ":" + port);
	    MulticastSocket mcast = new MulticastSocket(port);
	    mcast.joinGroup(ip);
	    socket = (DatagramSocket) mcast;
	}
	
	else {
	    // JPW 05/03/2005: I think generally we shouldn't be specifying the
	    //                 IP address here.  See JavaDoc for DatagramSocket
	    //                 where it says:
	    //                 "In order to receive broadcast packets a
	    //                 DatagramSocket should be bound to the wildcard
	    //                 address. In some implementations, broadcast
	    //                 packets may also be received when a Datagram-
	    //                 Socket is bound to a more specific address."
	    
	    if (ip == null) {
		System.err.println(
		    "Accept Broadcast packets (not binding to a particular " +
		    "address) at port " +
		    port);
		socket = new DatagramSocket(port);
	    } else {
		System.err.println(
		    "Accept Unicast packets sent to " + ip + ":" + port);
		socket = new DatagramSocket(port,ip);
	    }
	}
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	receive		(Receive data into a channel)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998, 1999 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method gets datagram(s) from the socket and	***
  ***	add them to the input channel.				***
  ***								***
  ***	Input/Output :						***
  ***	   channelIO		Put the data into this channel.	***
  ***								***
  ***	Modification History :					***
  ***	   05/27/1999 - INB					***
  ***		Modified to use new API access methods.		***
  ***	   06/13/2001 - UCB		        		***
  ***	        Capture now places each datagram packet in its  ***
  ***           own RBNB frame.                                 ***
  ***	   05/04/2005 - JPW					***
  ***		No longer calculate duration; no longer use	***
  ***		cInfos[] array (which was used in calculating	***
  ***		duration					***
  ***	   05/06/2005 - JPW					***
  ***		Moved setting the timestamp to just after we	***
  ***		receive the new packet				***
  ***								***
  *****************************************************************
*/
    ChanInfo receive() throws Exception {
      
      // Force one incoming UDP packet to be one RBNB frame
      
      // JPW 05/04/2005: Duration is now set to 0.0; no longer calculate it
      // cInfos[currIdx] = new ChanInfo();
      // cInfos[currIdx].start = RBNBClock.currentTimeMillis();
      // if (cInfos[currIdx].start - cInfos[lastIdx].start < 1) {
      //     cInfos[currIdx].start = cInfos[lastIdx].start + 1;
      // }
      // cInfos[lastIdx].dur = cInfos[currIdx].start - cInfos[lastIdx].start;
      
      ChanInfo chanInfo = new ChanInfo();
      // JPW 05/06/2005: Move setting the timestamp until just after we receive
      //                 the packet
      // chanInfo.start = System.currentTimeMillis();
      
      // JPW 05/04/2005: Increase the size of the byte array from
      //                 6000 to 65536
      DatagramPacket dPacket = new DatagramPacket(new byte[65536],65536);
      
      // JPW 05/04/2005: Set a timeout on the receive
      try {
	  socket.setSoTimeout(2000);
      } catch (SocketException se) {
	  // Don't do anything if this exception is raised  
      }
      
      try {
          socket.receive(dPacket);
      } catch (java.net.SocketTimeoutException ste) {
	  // the receive() timed-out
	  return chanInfo;
      }
      
      // JPW 05/06/2005: Moved setting the timestamp to just after we receive
      //                 the new packet
      chanInfo.start = System.currentTimeMillis();
      
      /* Ensure that the packet has contents. */
      if (dPacket.getLength() == 0) {
	  return chanInfo;
      }
      
      /* Store the data in the ChanInfo. */
      size = dPacket.getLength();
      chanInfo.data = new byte[size];
      System.arraycopy
	  (dPacket.getData(),
	   0,
	   chanInfo.data,
	   0,
	   size);
      
      return chanInfo;
      
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect	(Disconnect the socket)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method disconnects the socket for this capture	***
  ***	stream.							***
  ***								***
  *****************************************************************
*/
    void disconnect() {
      if (socket != null) {
	socket.close();
	socket = null;
      }
    }
    
  } // end class CaptureDatagramIP
  
} // end class Capture
