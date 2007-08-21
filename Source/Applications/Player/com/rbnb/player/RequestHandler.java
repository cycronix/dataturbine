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
  ***	Name :	RequestHandler			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2004, 2005 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   The RequestHandler listens for changes to its menu   ***
  ***   items and to the ControlBox.  It uses a PlayerEngine    ***
  ***   to act on the user's choices.                           ***
  ***	   Basically, RequestHandler is a scheduler for the     ***
  ***   requests the user wants to make of the PlayerEngine.    ***
  ***								***
  ***	Modification History:                                   ***
  ***	JPW 07/22/05:  In createMenus(), by default, don't	***
  ***		       display Duration and Increment sliders.	***
  ***	JPW 06/16/05:  Make changes to allow TCP server output:	***
  ***		       1. Change menu from "Open (UDP Output)"	***
  ***			    to "Open (IP Output)"		***
  ***		       2. Change method openUDPOutput() to	***
  ***			    openIPOutput()			***
  ***		       3. Change class RBNBToUDPDlg to		***
  ***			    RBNBToIPDlg				***
  ***		       4. Add new PlayerEngine.connect() method	***
  ***			    for opening a TCP server socket	***
  ***			    connection.				***
  ***   JPW 02/24/05:  Remove the "Milliseconds" menu item from ***
  ***                      the Time Format menu                 ***
  ***	JPW 10/29/04:  Add "Time Format" menu			***
  ***	JPW 10/07/04:  Upgrade to RBNB V2 Player		***
  ***	UCB 05/01/01:  Added boolean autoRun to                 ***
  ***                  theRealConstructor() and constructors.   ***
  ***                  It determines whether to automatically   ***
  ***                  open the Connections.                    ***
  ***   UCB 02/28/02:  The duration and increment sliders are   ***
  ***                  now always enabled.  RequestHandler      ***
  ***                  must check whether PlayerEngine is       ***
  ***                  connected and has channels set, before   ***
  ***                  permitting a change to the dur or inc to ***
  ***                  request that PlayerEngine take a step.   ***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.RBNBProcessInterface;
import com.rbnb.utility.SettingsReader;
import com.rbnb.utility.SettingsWriter;
import com.rbnb.utility.Utility;

public class RequestHandler implements ControlBoxListener,
                                       Runnable,
                                       ActionListener,
				       ItemListener,
                                       WindowListener {

    // constants-- actions
    public static final int TERMINATE = -1,
	PAUSE = 0,
	PLAY = 1,
	STEP = 2,
	STEPLIMIT = 3,
	RT = 4,
	CONNECT = 5,
	CHANSELECT = 6,
	DISCONNECT = 7;
    private static final boolean NA = false;
    public static String SETTINGS_FILE = "PlayerSettings.dat";
    private static final String DEFAULT_OUT_UDP = "127.0.0.1";

    // GUI components
    private Frame frame = null;
    private Menu viewMenu = null;
    private ControlBox cb = null;

    // read settings from the SETTINGS_FILE?
    private boolean useSettingsFile = false;

    // variables for handling scheduling of requests
    private Request nextRequest = null;
    private Thread runningThread = null;
    private volatile boolean amBusy = false;
    private volatile boolean amConnected = false;  // 02/28/02 UCB - connected to input?

    // PlayerEngine holds code used to run rbnbPlayer
    private PlayerEngine pe = null;

    // information about server and channels
    private String inServerStr = "localhost:3333";
    private String outServerStr = null;
    private InetAddress outUDPAdd = null;
    private int outUDPPort = 5555;
    // JPW 06/16/2005: Add TCP output
    private int tcpServerPort = 5555;
    private boolean bUDPOutput = true;
    private String[] chans = null;
    private String myName = null;
    private boolean outputToRBNB = true;
    private int cacheSize = 1000;   // was 1, mjm 2/16/05
    private boolean useShortNames = true;

    // this object is used by rbnbProcess, when rbnbPlayer
    // is run within rbnbManager
    private RBNBProcessInterface processID = null;
    
    // JPW 10/29/2004: Add "Time Format" menu; allows the user to communicate
    //                 what the format of the time value is; only used for
    //                 appropriate display on the GUI
    CheckboxMenuItem unspecifiedTimeFormatCB = null;
    // JPW 02/24/2005: Remove the milliseconds menu item
    // CheckboxMenuItem millisecondsTimeFormatCB = null;
    CheckboxMenuItem secondsTimeFormatCB = null;
    
/*
  *****************************************************************
  ***								***
  ***	Name :	RequestHandler			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor-- used to register this process.         ***
  ***								***
  *****************************************************************
*/
    public RequestHandler(RBNBProcessInterface procID, 
			  String inSvr, String outSvr, boolean auto,
			  boolean useSettingsFileI) {
	theRealConstructor(inSvr, outSvr, auto, useSettingsFileI);
	processID = procID;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	RequestHandler			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor-- used to register this process.         ***
  ***								***
  *****************************************************************
*/
    public RequestHandler(RBNBProcessInterface procID, String inSvr, 
			  boolean auto, boolean useSettingsFileI) {
	theRealConstructor(inSvr, null, auto, useSettingsFileI);
	processID = procID;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	RequestHandler			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor-- used to register this process.         ***
  ***								***
  *****************************************************************
*/
    public RequestHandler(RBNBProcessInterface procID, boolean useSettingsFileI) {
	theRealConstructor(null, null, false, useSettingsFileI);
	processID = procID;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	RequestHandler			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
    public RequestHandler(String inSvr, String outSvr, boolean auto,
			  boolean useSettingsFileI) {
	theRealConstructor(inSvr, outSvr, auto, useSettingsFileI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	RequestHandler			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
    public RequestHandler() {
	theRealConstructor(null, null, false, false);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	theRealConstructor		              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Automatically connects to the input and***
  ***   output servers, as appropriate.                         ***
  ***								***
  *****************************************************************
*/
    public void theRealConstructor(String inSvr, String outSvr, 
				   boolean autoOpen, boolean useSettingsFileI) {
	inServerStr = inSvr;
	if (inServerStr == null) {
	    inServerStr = "localhost:3333";
	}
	outServerStr = outSvr;
	if (outServerStr == null) {
	    outServerStr = inServerStr;
	}

	if (useSettingsFileI &&
	    SettingsReader.settingsExist(SETTINGS_FILE)) {
	    useSettingsFile = true;
	    loadSettings();
	}

	createDisplay();

	runningThread = new Thread(this);
	runningThread.start();

	frame.setVisible(true);

	if (autoOpen) {
	    try {
		openConnection();
	    } catch (Exception e) {
		handleException(e, "Error encountered while connecting.");
		return;
	    }
	    try {
		chanSelect();
	    } catch (Exception e) {
		handleException(e, "Error encountered while selecting input channels.");
		return;
	    }
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
	inServerStr = (String) hash.get("In_RBNB");
	outServerStr = (String) hash.get("Out_RBNB");
	try {
	    outUDPAdd = InetAddress.getByName((String) hash.get("Out_UDP"));
	} catch (UnknownHostException uhe) {}
	outputToRBNB = Boolean.valueOf((String) hash.get("OutputToRBNB")).booleanValue();
	try {
	    cacheSize = Integer.parseInt((String) hash.get("CacheSize"));
	} catch (NumberFormatException nfe2) {}
	useShortNames = Boolean.valueOf((String) hash.get("UseShortNames")).booleanValue();
	chans = Utility.unpack((String) hash.get("Channels"), "\n");
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
	setHash.put("In_RBNB", inServerStr);
	setHash.put("Out_RBNB", 
		    (outServerStr == null ? inServerStr : outServerStr));
	setHash.put("Out_UDP", 
		    (outUDPAdd == null ? DEFAULT_OUT_UDP : outUDPAdd.getHostAddress()));
	setHash.put("OutputToRBNB", String.valueOf(outputToRBNB));
	setHash.put("CacheSize", String.valueOf(cacheSize));
	setHash.put("UseShortNames", String.valueOf(useShortNames));
	
	if (pe != null) {
	    String[] chanNames = pe.getCurrentChannels();
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < chanNames.length; ++i) {
		sb.append(chanNames[i]);
		sb.append("\n");
	    }
	    setHash.put("Channels", sb.toString());
	} else {
	    setHash.put("Channels", "");
	}

	return setHash;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createDisplay			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor helper method.                           ***
  ***								***
  *****************************************************************
*/
    private void createDisplay() {
	frame = new Frame("rbnbPlayer");
	frame.setBackground(Color.lightGray);
	frame.setMenuBar(createMenus());
	frame.addWindowListener(this);
	
	try {
	    // create a ControlBox with a duration slider,
	    // and with the sliders disabled.
	    // Buttons are auto-disabled.
	    cb = new ControlBox();
	} catch (Exception e) {
	    e.printStackTrace();
            //EMF 11/24/00: switched to RBNBProcess.exit
            RBNBProcess.exit(-1, processID);
	    //System.exit(-1);
	}
	
	// JPW 10/29/2004: Make sure the time format variable in ControlBox is
	// initialized correctly
	cb.setTimeFormat(ControlBox.TIME_FORMAT_UNSPECIFIED);
	
	cb.addControlBoxListener(this);
	frame.add(cb);
	
	// need to create PlayerEngine, and pass it ControlBox.
	pe = new PlayerEngine(cb);
	
	frame.pack();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :  createMenus			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Creates the menus for rbnbPlayer.                    ***
  ***								***
  ***	Modification History					***
  ***	07/22/2005	JPW	By default, don't display	***
  ***				Duration and Increment sliders	***
  ***	10/29/2004	JPW	Add "Time Format" menu		***
  ***								***
  *****************************************************************
*/
    private MenuBar createMenus() {
	MenuBar mb = new MenuBar();
	mb.setFont(new Font("dialog",Font.PLAIN,14));

	Menu file = new Menu("File");
	MenuItem open = new MenuItem("Open (RBNB Output)");
	open.addActionListener(this);
	file.add(open);
	// JPW 06/16/2005: Add TCP output; make this menu item more general
	// MenuItem open2 = new MenuItem("Open (UDP Output)");
	MenuItem open2 = new MenuItem("Open (IP Output)");
	open2.addActionListener(this);
	file.add(open2);
	MenuItem close = new MenuItem("Close");
	close.addActionListener(this);
	file.add(close);
	file.addSeparator();
	MenuItem exit = new MenuItem("Exit");
	exit.addActionListener(this);
	file.add(exit);

	viewMenu = new Menu("View");
	MenuItem viewChans = new MenuItem("Channels");
	viewChans.addActionListener(this);
	viewChans.setEnabled(false);
	viewMenu.add(viewChans);
	viewMenu.addSeparator();
	CheckboxMenuItem viewPos = new CheckboxMenuItem("Position", true);
	viewPos.addItemListener(this);
	viewMenu.add(viewPos);
	// JPW 07/22/2005: By default, don't display Duration
	//                 and Increment sliders
	CheckboxMenuItem viewDur = new CheckboxMenuItem("Duration", false);
	viewDur.addItemListener(this);
	viewMenu.add(viewDur);
	CheckboxMenuItem viewInc = new CheckboxMenuItem("Increment", false);
	viewInc.addItemListener(this);
	viewMenu.add(viewInc);
	CheckboxMenuItem viewRat = new CheckboxMenuItem("Rate", true);
	viewRat.addItemListener(this);
	viewMenu.add(viewRat);
	
	// JPW 10/29/2004: Add Time Format menu
	// JPW 02/24/2005: Remove the "Milliseconds" menu item for now
	Menu timeFormatMenu = new Menu("Time Format");
	unspecifiedTimeFormatCB =
	    new CheckboxMenuItem("Unformatted", true);
	unspecifiedTimeFormatCB.addItemListener(this);
	timeFormatMenu.add(unspecifiedTimeFormatCB);
	// millisecondsTimeFormatCB =
	//     new CheckboxMenuItem("Milliseconds", false);
	// millisecondsTimeFormatCB.addItemListener(this);
	// timeFormatMenu.add(millisecondsTimeFormatCB);
	secondsTimeFormatCB =
	    new CheckboxMenuItem("Seconds", false);
	secondsTimeFormatCB.addItemListener(this);
	timeFormatMenu.add(secondsTimeFormatCB);
	
	Menu help = new Menu("Help");
	MenuItem about = new MenuItem("About");
	//about.addActionListener(this);
	help.add(about);
	//MenuItem onlineHelp = new MenuItem("OnLine Documentation");
	//onlineHelp.addActionListener(this);
	//help.add(onlineHelp);

	mb.add(file);
	mb.add(viewMenu);
	mb.add(timeFormatMenu);
	//mb.add(help);
	//mb.setHelpMenu(help);

	return mb;
    }

    /**********************************************************/
    /************************ run() ***************************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	run()            		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Read and run requests.                               ***
  ***								***
  *****************************************************************
*/
    public void run() {
	Request currRequest = new Request();
	boolean loopForever = true;

	while (loopForever) {

	    amBusy = false;
	    synchronized (this) {
		while (getNextRequest() == null) {
		    try {
			wait();
		    } catch (InterruptedException e) {}
		}
		
		amBusy = true;

		// now have a request: copy it locally
		currRequest.action = nextRequest.action;
		currRequest.position = nextRequest.position;
		currRequest.goingForwards = nextRequest.goingForwards;
		currRequest.resetLimits = nextRequest.resetLimits;

		// clear nextRequest
		nextRequest = null;
	    } // synchronized (this)

	    switch (currRequest.action) {

	    case TERMINATE:
		amConnected = false;
		if (useSettingsFile && SettingsWriter.settingsExist(SETTINGS_FILE)) {
		    saveSettings();
		}
		pe.disconnect();
		frame.setVisible(false);
		RBNBProcess.exit(0, processID);
		frame.dispose();
		loopForever = false;
		break;
		
	    case PLAY:
		try {
		    pe.play(currRequest.goingForwards);
		} catch (Exception e) {
		    handleException(e, "Error encountered while playing.");
		}
		break;

	    case STEP:
		try {
		    if (currRequest.position != -1) {
			pe.takeAStep(currRequest.position,
				     currRequest.goingForwards, 
				     currRequest.resetLimits);
		    } else {
			// JPW 12/15/2004: Just make 1 version of this method;
			//      add Integer.MIN_VALUE
			pe.takeAStep(Integer.MIN_VALUE,
			             currRequest.goingForwards, 
				     currRequest.resetLimits);
		    }
		} catch (Exception e) {
		    handleException(e, "Error encountered while taking a single step.");
		}
		break;

	    case STEPLIMIT:
		try {
		    pe.goToLimit(currRequest.position);
		} catch (Exception e) {
		    handleException(e, "Error encountered while fetching data limit.");
		}
		break;

	    case RT:
		try {
		    pe.realtime();
		} catch (Exception e) {
		    handleException(e, "Error encountered while realtiming.");
		}
		break;

	    case CONNECT:
		// make sure pause button is lit
		pe.doPause();
		amConnected = false;
		pe.disconnect();
		try {
		    openConnection();
		} catch (Exception e) {
		    handleException(e, "Error encountered while connecting.");
		    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		    break;
		}
		try {
		    chanSelect();
		} catch (Exception e) {
		    handleException(e, "Error encountered while selecting input channels.");
		    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		break;

	    case CHANSELECT:
		// make sure pause button is lit
		pe.doPause();
		try {
		    chanSelect();
		} catch (Exception e) {
		    handleException(e, "Error encountered while selecting input channels.");
		    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		break;

	    case DISCONNECT:
		// make sure pause button is lit
		pe.doPause();
		amConnected = false;
		if (useSettingsFile && SettingsWriter.settingsExist(SETTINGS_FILE)) {
		    saveSettings();
		}
		pe.disconnect();
		frame.setTitle("rbnbPlayer-- Disconnected");
		viewMenu.getItem(0).setEnabled(false);
		break;

	    default:
		break;

	    }

	} // while (loopForever)
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	openConnection()            		        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Open connections and allow user to select channels.  ***
  ***								***
  *****************************************************************
*/
    private void openConnection() throws Exception {
	frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	frame.setTitle("rbnbPlayer-- Connecting to " + inServerStr + "...");
	// JPW 06/16/2005: Add bUDPOutput flag and TCP output option
	if (outputToRBNB) {
	    // RBNB output
	    myName = pe.connect(inServerStr, outServerStr, cacheSize);
	} else if (bUDPOutput) {
	    // UDP output
	    myName = pe.connect(inServerStr, outUDPAdd, outUDPPort);
	} else {
	    // TCP server output
	    myName = pe.connect(inServerStr, tcpServerPort);
	}
	
	// If we have retrieved a set of channel names from the
	// settings file, we will pass them to the
	// PlayerEngine.  These channels must, however,
	if (useSettingsFile &&
	    !(chans.length == 1 && chans[0].equals(""))) {
	    
	    // The channels must correspond to actual, available channels.
	    
	    String[] avail = pe.getAvailableChans();
	    Arrays.sort(avail);
	    Arrays.sort(chans);
	    Vector useChansV = new Vector();
	    
	    for (int i = 0; i < chans.length; i++) {
		if (Arrays.binarySearch(avail, chans[i]) >= 0) {
		    useChansV.addElement(chans[i]);
		    if (!outputToRBNB) {
			break; // UDP output takes only one channel input
		    }
		}
	    }
	    
	    String[] useChans = new String[useChansV.size()];
	    for (int j = 0; j < useChans.length; ++j) {
		useChans[j] = (String) useChansV.elementAt(j);
	    }
	    chans = useChans;
	    pe.setChannels(useChans, useShortNames);
	}
	
	viewMenu.getItem(0).setEnabled(true);
	frame.setTitle(myName + "-- Connected to " + inServerStr);
	frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**********************************************************/
    /************ ActionListener implementation ***************/
    /**********************************************************/
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  actionPerformed  (respond to events)            ***
  ***   By   :  Ursula Bergstrom  (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000     				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Handle actions from the user-- many of these issue  ***
  ***   Requests to PlayerEngine.                               ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***       event       The event object describing the event   ***
  ***                   that has occured.                       ***
  ***                                                           ***
  *****************************************************************
*/
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	
	if (label.equals("Open (RBNB Output)")) {
	    openAction();
	}
	
	// JPW 06/16/2005: Add TCP output;
	//                 make menu item and method name more general
	// else if (label.equals("Open (UDP Output)")) {
	//     openUDPAction();
	// }
	else if (label.equals("Open (IP Output)")) {
	    openIPAction();
	}
	
	else if (label.equals("Close")) {
	    closeAction();
	}
	
	else if (label.equals("Exit")) {
	    exitAction();
	}
	
	else if (label.equals("Channels")) {
	    chanSelectAction();
	}
    } 

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  itemStateChanged                                ***
  ***   By   :  Ursula Bergstrom  (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000     				***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Respond to CheckboxMenuItems.                       ***
  ***								***
  ***	Modification History					***
  ***	10/29/2004	JPW	Add support for the items in	***
  ***				the "Time Format" menu.		***
  ***                                                           ***
  *****************************************************************
*/
    public void itemStateChanged(ItemEvent ie) {
	CheckboxMenuItem item = (CheckboxMenuItem)ie.getSource();
	String label = item.getLabel();
	if (label.equals("Position")) {
	    cb.setPositionVisible(item.getState());
	    frame.pack();
	} else if (label.equals("Duration")) {
	    cb.setDurationVisible(item.getState());
	    frame.pack();
	} else if (label.equals("Increment")) {
	    cb.setIncrementVisible(item.getState());
	    frame.pack();
	} else if (label.equals("Rate")) {
	    cb.setRateVisible(item.getState());
	    frame.pack();
	}
	
	// JPW 10/29/2004: Add support for the "Time Format" menu items;
	//                 note that I've got to make my own checkbox group
	//                 since only one of these should be checked at any
	//                 one time
	else if (label.equals("Unformatted")) {
	    if (item.getState()) {
		// Unset the other two menu items
		// JPW 02/24/2005: Remove the milliseconds menu item
		// millisecondsTimeFormatCB.setState(false);
		secondsTimeFormatCB.setState(false);
		cb.setTimeFormat(ControlBox.TIME_FORMAT_UNSPECIFIED);
	    }
	} else if (label.equals("Seconds")) {
	    if (item.getState()) {
		// Unset the other two menu items
		unspecifiedTimeFormatCB.setState(false);
		// JPW 02/24/2005: Remove the milliseconds menu item
		// millisecondsTimeFormatCB.setState(false);
		cb.setTimeFormat(ControlBox.TIME_FORMAT_SECONDS);
	    }
	} else if (label.equals("Milliseconds")) {
	    if (item.getState()) {
		// Unset the other two menu items
		unspecifiedTimeFormatCB.setState(false);
		secondsTimeFormatCB.setState(false);
		cb.setTimeFormat(ControlBox.TIME_FORMAT_MILLISECONDS);
	    }
	}
	
    }

    /**********************************************************/
    /************* actions in response to user ****************/
    /**********************************************************/
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  openAction                                      ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000     				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Prepare to open a connection to an RBNB.            ***
  ***                                                           ***
  *****************************************************************
*/
    private void openAction() {
	String inHost = "localhost";
	int inPort = 3333,
	    index;
	TwoWayServerDlg twsd = null;

	// parse out user's previously-entered value for 
	// input server (if any).
	if (inServerStr != null) {
	    index = inServerStr.indexOf(':');
	    if (index != -1) {
		inHost = inServerStr.substring(0,index);
		try {
		    inPort = Integer.parseInt(inServerStr.substring(index+1));
		} catch (Exception e) {}
	    }
	}

	// create and display TwoWayServerDlg-- try to display
	// output server data, if available.
	if ((outServerStr == null) || (outServerStr.equals("")) ||
	    (outServerStr.equals(inServerStr))) {

	    twsd = new TwoWayServerDlg(frame,
				       inHost, inPort,
				       cacheSize);
	} else {
	    String outHost = outServerStr;
	    int outPort = 3333;
	    index = outServerStr.indexOf(':');
	    if (index != -1) {
		outHost = outServerStr.substring(0,index);
		try {
		    outPort = Integer.parseInt(outServerStr.substring(index+1));
		} catch (Exception e) {}
	    }
	    twsd = new TwoWayServerDlg(frame,
				       inHost, inPort,
				       outHost, outPort,
				       cacheSize);
	}
	twsd.setVisible(true);

	if (twsd.state == TwoWayServerDlg.CANCEL) {
	    twsd.dispose();
	    return;
	} else {  // state == OK
	    // retrieve the user's input
	    inServerStr = twsd.inMachine + ':' +
		Integer.toString(twsd.inPort);
	    outServerStr = twsd.outMachine + ':' +
		Integer.toString(twsd.outPort);
	    cacheSize = twsd.cacheSize;
	    twsd.dispose();
	}
	
	outputToRBNB = true;
	// make the connection
	setNextRequest(new Request(CONNECT));
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  openIPAction                                    ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001       				***
  ***								***
  ***	Copyright 2001 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Prepare to open connections to an RBNB and a UDP    ***
  ***   socket.                                                 ***
  ***								***
  ***	Modification History					***
  ***	06/16/2005   JPW   Change name from openUDPAction() to	***
  ***			   openIPAction(); add TCP output as	***
  ***			   an option				***
  ***                                                           ***
  *****************************************************************
*/
    private void openIPAction() {
	
	String inHost = "localhost";
	int inPort = 3333;
	
	// JPW 06/16/2005: Change RBNBToUDPDlg to RBNBToIPDlg
	// RBNBToUDPDlg rtud = null;
	RBNBToIPDlg rtid = null;
	
	// parse out user's previously-entered value for 
	// input server (if any).
	if (inServerStr != null) {
	    int index = inServerStr.indexOf(':');
	    if (index != -1) {
		inHost = inServerStr.substring(0,index);
		try {
		    inPort = Integer.parseInt(inServerStr.substring(index+1));
		} catch (Exception e) {}
	    }
	}
	
	// create and display RBNBToIPDlg-- try to display
	// output InetAddress and port, if available.
	String tempAddr = null;
	if (outUDPAdd != null) {
	    tempAddr = outUDPAdd.getHostAddress();
	}
	
	rtid =
	    new RBNBToIPDlg(
		frame,
		inHost,
		inPort,
		tempAddr,
		outUDPPort,
		tcpServerPort,
		bUDPOutput);
	
	rtid.setVisible(true);
	
	if (rtid.state == RBNBToIPDlg.CANCEL) {
	    rtid.dispose();
	    return;
	} else {
	    // retrieve the user's input
	    inServerStr = rtid.inMachine + ':' +
		Integer.toString(rtid.inPort);
	    outUDPAdd = rtid.outAdd;
	    outUDPPort = rtid.outPort;
	    tcpServerPort = rtid.serverPort;
	    bUDPOutput = rtid.bUDPOutput;
	    rtid.dispose();
	}
	
	outputToRBNB = false;
	// make the connection
	setNextRequest(new Request(CONNECT));
	
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  closeAction                                     ***
  ***   By   :  U. Bergstrom    (Creare Inc., Hanover, NH)      ***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000     				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Close connection to an RBNB.                        ***
  ***                                                           ***
  *****************************************************************
*/
    private void closeAction() {
	setNextRequest(new Request(DISCONNECT));
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  exitAction                                      ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000     				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Exits rbnbPlayer.                                   ***
  ***                                                           ***
  *****************************************************************
*/
    public void exitAction() {
	setNextRequest(new Request(TERMINATE));
	//EMF 11/24/00: switched to RBNBProcess.exit
	//COM.Creare.Utility.RBNBProcess.exit(0, processID);
	//System.exit(0);
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  chanSelectAction                                ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000     				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Select a channel to control playback on.            ***
  ***                                                           ***
  *****************************************************************
*/
    private void chanSelectAction() {
	setNextRequest(new Request(CHANSELECT));
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  chanSelect                                      ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000     				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Select channels to control playback of.             ***
  ***   CALL THIS METHOD ONLY FROM WITHIN RUN.                  ***
  ***                                                           ***
  *****************************************************************
*/
    private void chanSelect() throws Exception {
	frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	ChannelDlg cd = new ChannelDlg(frame, "Channel Selection",
				       "Select the channels you wish to control.",
				       pe.getAvailableChans(),
				       pe.getCurrentChannels(),
				       useShortNames,
				       outputToRBNB);
	cd.setVisible(true);
	if (cd.state == ChannelDlg.OK) {
	    cd.setVisible(false);
	    useShortNames = cd.useShortNames;
	    chans = cd.chans;
	    pe.setChannels(chans, useShortNames);
	    amConnected = true;
	} 
	cd.dispose();
	frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    /**********************************************************/
    /********** ControlBoxListener implementation *************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	limitOfData			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Moves to      ***
  ***   beginning or end of data.                               ***
  ***								***
  *****************************************************************
*/
    public synchronized void limitOfData(boolean dataStart,
					 boolean trackingPosition) {
	setNextRequest(new Request(STEPLIMIT, 
				   dataStart ? ControlBox.MINPOS : ControlBox.MAXPOS,
				   NA, NA));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	play     			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Plays         ***
  ***   forwards or backwards.                                  ***
  ***								***
  *****************************************************************
*/
    public synchronized void play(boolean forwards) {
	setNextRequest(new Request(PLAY, forwards));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	step    			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Steps         ***
  ***   backwards or forwards.                                  ***
  ***								***
  *****************************************************************
*/
    public synchronized void step(boolean forwards) {
	setNextRequest(new Request(STEP, forwards));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	pause    			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Pauses.       ***
  ***								***
  *****************************************************************
*/
    public synchronized void pause() {
	pe.interrupt();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	realTime			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Realtimes.    ***
  ***								***
  *****************************************************************
*/
    public synchronized void realTime() {
	setNextRequest(new Request(RT));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	positionSliderAt			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Triggered when***
  ***   user moves the position slider bar.                     ***
  ***								***
  *****************************************************************
*/
    public synchronized void positionSliderAt(int posValue, 
			   boolean trackingPosition) {
	// changing the position interupts what was going on (???):
	// if not tracking, pause, else step
	//if (trackingPosition) {
	// NOTE: In the Request object created, the forward variable is
	//       always set true.  But in actuality, we don't know which way
	//       the user has been moving the slider!
	setNextRequest(new Request(STEP, posValue, true, false));
//  	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	durationAt			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Triggered when***
  ***   user moves the duration slider bar.                     ***
  ***								***
  *****************************************************************
*/
    public synchronized void durationAt(double dur, 
					boolean trackingSlider) {
	// Reset PlayerEngine's duration.
	//	if (!trackingSlider) {
	    pe.setDuration(dur);
	    setConditionalNextRequest(new Request(STEP, true, false));
//  	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	rateAt   			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000                                  ***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Triggered when***
  ***   user moves the rate slider.                             ***
  ***								***
  *****************************************************************
*/
    public synchronized void rateAt(double rate,
				    boolean trackingSlider) {
	pe.setRate(rate);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	incrementAt   			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000                                  ***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Triggered when***
  ***   user moves the increment slider.                        ***
  ***								***
  *****************************************************************
*/
    public synchronized void incrementAt(double value,
				    boolean trackingSlider) {
	//if (!trackingSlider) {
	pe.setIncrement(value);
	    //}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setMode                 	        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Part of ControlBoxListener interface.  Triggered when***
  ***   user changes the mode.                                  ***
  ***      This method replaces setTimeBaseInSec().             ***
  ***								***
  *****************************************************************
*/
    public synchronized void setMode(int modeI) {
	pe.setMode(modeI);
	setConditionalNextRequest(new Request(STEP, true, false));
    }

    /**********************************************************/
    /************ setting and checking Requests ***************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	setConditionalNextRequest			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Sets up the next request ONLY if no other request is ***
  ***   currently being handled.                                ***
  ***      02/28/02 UCB - modification: Player must also be     ***
  ***   connected to an input source (indicated by amConnected) ***
  ***   for the next request to be set.                         ***
  ***								***
  *****************************************************************
*/
    private synchronized void setConditionalNextRequest(Request req) {
	if (!amBusy && amConnected) {
	    nextRequest = req;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setNextRequest			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Sets up the next request.                            ***
  ***								***
  *****************************************************************
*/
    private synchronized void setNextRequest(Request req) {
	pe.interrupt();
	nextRequest = req;
	notifyAll();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getNextRequest			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Gets the next request.                               ***
  ***								***
  *****************************************************************
*/
    private /*synchronized*/ Request getNextRequest() {
	return nextRequest;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	Request       (inner class)              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Inner class Request holds user's requests.           ***
  ***								***
  *****************************************************************
*/
    class Request {
	public int action = RequestHandler.PAUSE;
	public int position = -1;
	public boolean goingForwards = true;
	public boolean resetLimits = true;

/*
  *****************************************************************
  ***								***
  ***	Name :	Request                                  	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Default constructor.                                 ***
  ***								***
  *****************************************************************
*/
	Request() {}

/*
  *****************************************************************
  ***								***
  ***	Name :	Request                                  	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
	Request(int actI, int posI, /*double durI, */
		boolean forwardsI, boolean resetLimitsI) {
	    action = actI;
	    position = posI;
	    goingForwards = forwardsI;
	    resetLimits = resetLimitsI;
	}

/*
  *****************************************************************
  ***								***
  ***	Name :	Request                                  	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
	Request(int actI, boolean forwardsI, boolean resetLimitsI) {
	    action = actI;
	    goingForwards = forwardsI;
	    resetLimits = resetLimitsI;
	}

/*
  *****************************************************************
  ***								***
  ***	Name :	Request                                  	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
	Request(int actI, boolean forwardsI) {
	    action = actI;
	    goingForwards = forwardsI;
	}

/*
  *****************************************************************
  ***								***
  ***	Name :	Request                                  	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
	Request(int actI) {
	    action = actI;
	}

    } // Request

    /**********************************************************/
    /******************** Reporting errors ********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	errorBox()      				***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000		        		***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method displays an error message.               ***
  ***								***
  *****************************************************************
*/
  private void errorBox(String descrip) {
    String[] errStr = new String[1];
    errStr[0] = descrip;

    InfoDialog infoDialog =
      new InfoDialog(frame,
		     true,
		     new String("Error!"),
		     errStr);
    // center the dialog inside the parent Frame
    infoDialog.setLocation(Utility.centerRect(infoDialog.getBounds(),
					    frame.getBounds()));
    infoDialog.setVisible(true);
    infoDialog.dispose();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	handleException()      				***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000		        		***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method reports exceptions, disconnecting where  ***
  ***   necessary.                                              ***
  ***								***
  *****************************************************************
*/
    private void handleException(Exception e, String descrip) {
	String msg = e.getMessage();
	if (msg == null) {
	    msg = "";
	}

	if (msg.equals(PlayerEngine.EMPTYCHANNELS)) {
	    errorBox(PlayerEngine.EMPTYCHANNELS);
	} else if (msg.equals(PlayerEngine.ONEFRAME)) {
	    errorBox(PlayerEngine.ONEFRAME);
	} else {
	    errorBox(descrip);
	    e.printStackTrace();
	    amConnected = false;
	    pe.disconnect();
	    viewMenu.getItem(0).setEnabled(false);
	    frame.setTitle("rbnbPlayer-- Disconnected");
	}
	frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

/*
  *********************************************************************
  ***	Name :	windowActionEvents				    ***
  *********************************************************************
*/
  public void windowActivated(WindowEvent eventI) {}
  public void windowClosed(WindowEvent eventI) {}
  public void windowClosing(WindowEvent event) {
      frame.setVisible(false);
      RBNBProcess.exit(0, processID);
      frame.dispose();
  }
  public void windowDeactivated(WindowEvent eventI) {}
  public void windowDeiconified(WindowEvent eventI) {}
  public void windowIconified(WindowEvent eventI) {}
  public void windowOpened(WindowEvent eventI) {}

}
