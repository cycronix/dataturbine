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
  ***	Name :	PlayerEngine			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004, 2005, 2006 Creare Inc.		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   The PlayerEngine runs the rbnbPlayer session.        ***
  ***								***
  ***   Modification History:                                   ***
  ***	07/06/2006	JPW	In play_singlePoint(), update	***
  ***				the time position label in	***
  ***				order to provide feedback to	***
  ***				the user (so they don't think	***
  ***				Player is just "stuck").	***
  ***	06/16/2005	JPW	Added a connect() method for	***
  ***				creating TCPServerPlayerOutput	***
  ***	05/16/2005	JPW	On the rate calculations for	***
  ***				playback modes (not realtime),	***
  ***				I changed the decay of the	***
  ***				exponential average: only weigh	***
  ***				current value at 1%		***
  ***	01/11/2005	JPW	Create play_singlePoint() to	***
  ***				specifically play data when in	***
  ***				single point mode.		***
  ***	01/10/2005	JPW	Add bForceAbsoluteI arg to	***
  ***				step() and fetch().  This is to	***
  ***				allow us to force an "absolute"	***
  ***				request when duration=0 and the	***
  ***				user is moving the position	***
  ***				slider.  Also, add		***
  ***				bOutputDataI arg to step().	***
  ***				In play(): In duration=0 mode,	***
  ***				update the startTime, update	***
  ***				the time display on the Player	***
  ***				GUI, and send out the data	***
  ***				*after* the nap time.		***
  ***	10/14/2004	JPW	In incrementStartTime() and	***
  ***				fetch(), add case for duration	***
  ***				equal to 0			***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player	***
  ***	11/07/01	UCB	Modified the PlayerEngine to	***
  ***				allow multiple modes (not just	***
  ***				two).				***
  ***								***
  *****************************************************************
*/
// NOTE: Some of these methods are "PRIVILEGED"-- that is, they
// are executed ONLY by the RequestHandler (directly or through any
// method called directly by RequestHandler), which ensures that only
// one of them will run at a time.  They need not (and in fact,
// must not) therefore be synchronized.  Some methods, on the other
// hand, are "ASYNCHRONOUS STATE-CHANGE" methods.  They may be
// executed by the RequestHandler at any time, and care must be taken
// to make synchronous access to any class variables changed by these
// methods.  An attempt has been made to label the methods in this
// class as one or the other.  (UCB 11/28/00)

package com.rbnb.player;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ToString;
import com.rbnb.utility.Utility;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;

public class PlayerEngine {

    /******* CONSTANTS ********/
    /****** Public constants (for error messages) ******/
    public static final String EMPTYCHANNELS = 
	"No currently selected channel contains any data!";
    public static final String ONEFRAME =
	"Currently selected set of channels contains only one frame of data!";

    /****** Private constants ******/
    private static final int NORMAL = 0,
	                     GOTNULLFRAME = 1,
	                     UPPERLIMIT = 2,
	                     LOWERLIMIT = 3,
	                     NO_CHANGE = 0,
	                     MODE_CHANGE = 1,
	                     PARAM_CHANGE = 2;
    private static final double ZERO_TIME = 0.0;
    private static final double RT_MAX_LATENCY = 1.5;

    /******** VARIABLES *******/
    /**** ControlBox and display of position ****/
    private ControlBox     cb = null;
    private double         rate;
    private double         increment;
    private int            mode;

    /***** variables for connections *****/
    private Sink           sink = null;
    private PlayerOutput   source = null;

    /**** variables for making requests ****/
    private ChannelMap     requestMap = null,
	                   outputMap = null;
    private double         startTime = 0.0,
	                   duration = 0.0;
    private double         timeMax = 0.0,
	                   timeMin = 0.0,
	                   masterMax = 0.0,
	                   masterMin = 0.0;
    // private int         requestFlag = DataRequest.defaults;

    /***** variables for managing interrupts ******/
    /***** synchronize before accessing any of the following! ******/
    private int            currentAction = RequestHandler.DISCONNECT;
    private boolean        interrupted = false;
    private Object         sleepWaker = new Object();

    /**** variables for managing parameter and mode changes ****/
    /***** synchronize before accessing any of the following! ******/
    // UCB 11/05/01: modeChanged replaces timeBaseChanged;
    //paramChanged replaces durationChanged, incrementChanged
    private boolean        modeChanged = false;
    private boolean        paramChanged = false;
    private double         pendingRate;
    private double         pendingIncrement;
    private double         pendingDurTime = 0.0;
    // private int         pendingReqFlag = DataRequest.defaults;
    private int            pendingMode;

    /**********************************************************/
    /******************* constructor **************************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	PlayerEngine			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
    public PlayerEngine(ControlBox cbI) {
	cb = cbI;

	cb.setButtonsEnabled(false);
	cb.setPositionEnabled(false);

	clearRateDisplay();

	currentAction = RequestHandler.DISCONNECT;
    }

    /**********************************************************/
    /************ handling channels and connections ***********/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	connect         			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Connects source and sink to DataTurbine.             ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    public String connect(String inHostPort,
                          String outHostPort,
			  int cacheSizeI)
    throws Exception
    {
	
	try {
	    // sink = new Connection(inHostPort, "r");
	    sink = new Sink();
	    sink.OpenRBNBConnection(inHostPort,"Player");
	    source = new RBNBPlayerOutput(outHostPort, cacheSizeI);
	} catch (Exception e1) {
	    System.err.println("Exception encountered while connecting.");
	    throw e1;
	}
	
	requestMap = new ChannelMap();
	doPause();  // reset currentAction
	return source.getName();
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	connect         			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Connects source to DataTurbine and sink to UDP       ***
  ***   socket.                                                 ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    public String connect(String inHostPort, InetAddress outAdd,
			  int outPort) throws Exception {
	try {
	    // sink = new Connection(inHostPort, "r");
	    sink = new Sink();
	    sink.OpenRBNBConnection(inHostPort,"Player");
	    if (outAdd.isMulticastAddress()) {
		source = new MulticastPlayerOutput(outAdd, outPort);
	    } else {
		source = new UDPPlayerOutput(outAdd, outPort);
	    }
	} catch (Exception e1) {
	    System.err.println("Exception encountered while connecting.");
	    throw e1;
	}

	// JPW 10/08/2004: Set default to TIME mode
	// JPW 06/16/2005: Remove these calls; cb.setMode() was resetting
	//                 sliders to their default positions, which I
	//                 don't want to do
	// cb.setMode(cb.TIME_MODE);
	// setMode(cb.TIME_MODE);

	requestMap = new ChannelMap();
	doPause();  // reset currentAction
	return source.getName();
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	connect         			        ***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Connects source to DataTurbine.  Open TCP server	***
  ***	port and be ready to accept TCP client connections to	***
  ***	send the RBNB data to.					***
  ***								***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	06/16/2006	JPW	Created				***
  ***								***
  *****************************************************************
*/
    public String connect(String inHostPort, int tcpServerPortI)
        throws Exception
    {
	
	try {
	    sink = new Sink();
	    sink.OpenRBNBConnection(inHostPort,"Player");
	    source = new TCPServerPlayerOutput(tcpServerPortI);
	} catch (Exception e1) {
	    System.err.println("Exception encountered while connecting.");
	    throw e1;
	}
	
	requestMap = new ChannelMap();
	doPause();  // reset currentAction
	return source.getName();
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect         			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Disconnects source and sink from DataTurbine.        ***
  ***	             PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    public void disconnect() {
	
	currentAction = RequestHandler.DISCONNECT; // not interruptable
	
	if (sink != null) {
	    sink.CloseRBNBConnection();
	    sink = null;
	}
	
	if (source != null) {
	    source.disconnect();
	    source = null;
	}
	
	requestMap = null;
	
	// make GUI go to a restricted state
	cb.setButtonsEnabled(false);
	cb.setPositionEnabled(false);
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getAvailableChannels         			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Returns all channels found on sink connection.       ***
  ***	             PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    public String[] getAvailableChans() throws Exception {
	
	// This is a request for relative, not fully-qualified, chan names
	// JPW 11/22/2004: Change the registration request made to the server
	//                 in order to have PlugIns without registered channels
	//                 still show up in the channel list
	// The old request:
	// sink.RequestRegistration();
	ChannelMap cm = new ChannelMap();
	// This specifies to start at the top level and then go one level down
	cm.Add("*/...");
	sink.RequestRegistration(cm);
	cm = new ChannelMap();
	cm = sink.Fetch(-1, cm);
	
	if ((cm == null) || (cm.NumberOfChannels() == 0)) {
	    return new String[] {""};
	}
	
	// exclude channels created by rbnbPlayers
	String[] allChanNames = cm.GetChannelList();
	String[] junk;
	BitSet validChans = new BitSet(cm.NumberOfChannels());
	int numValid = 0;
	for (int i = 0; i < cm.NumberOfChannels(); ++i) {
	    junk = Utility.unpack(allChanNames[i], "/");
	    // JPW 02/24/2005: exclude names that start with '_'
	    if (junk[0].charAt(0) != '_') {
		validChans.set(i);
		numValid++;
	    }
	    /*
	    if (junk.length == 3) {
		if (junk[1].indexOf("rbnbPlayer") == -1) {
		    validChans.set(i);
		    numValid++;
		}
	    } else {
		// 05/10/02 UCB - some channels will have datapaths
		// that contain "/".  Assume these are valid.
		validChans.set(i);
		numValid++;
	    }
	    */
	}
	
	String[] validChanNames = new String[numValid];
	for (int i = 0, j = 0; i < cm.NumberOfChannels(); i++) {
	    if (validChans.get(i)) {
		validChanNames[j] = allChanNames[i];
		j++;
	    }
	}
	
	return validChanNames;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getCurrentChannels         			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Returns all channels making up current request.      ***
  ***	             PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    public String[] getCurrentChannels() {
	
	if ( (requestMap == null) || (requestMap.NumberOfChannels() == 0) ) {
	    return new String[] {""};
	}
	
	return requestMap.GetChannelList();
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setChannels               			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Sets which channels to act on.                       ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	10/14/2004	JPW	If we have an RBNB source, get	***
  ***				the full channel registration	***
  ***				information to send to source.	***
  ***								***
  *****************************************************************
*/
    public void setChannels(String[] chans,
                            boolean useShortNamesI)
    throws Exception
    {
	
	// what if this is the second call to setChannels
	// on the same connection?
	
	if (chans.length == 0) {
	    return;
	}
	
	cb.setPositionEnabled(false);
	cb.setButtonsEnabled(false);
	
	requestMap.Clear();
	
	for (int i = 0; i < chans.length; ++i) {
	    requestMap.Add(chans[i]);
	}
	
	copyRequestMap();
	
	// JPW 10/14/2004: If we have an RBNB source, get the full
	//                 channel registration information to send to source.
	// DON'T DO FOR NOW - MJM IS WORKING ON RESOLVING A PROBLEM WITH THIS
	// (The problem involved the fact that we can't just pass outputMap
	//  with its registration info directly out to the Source.)

	// try it again - mjm 2/8/04
	
	ChannelMap regMap = new ChannelMap();
	if (source instanceof RBNBPlayerOutput) {
	    sink.RequestRegistration(requestMap);  // was outputMap
	    outputMap = sink.Fetch(-1);

	    // can't just use outputMap to register chans for some reason... so make copy
	    for(int i=0; i<outputMap.NumberOfChannels(); i++) {
	        int idx = regMap.Add(outputMap.GetName(i));
		regMap.PutDataRef(idx,outputMap,i);
	    }
	}

       	source.setOutputChannels(regMap, useShortNamesI);  // was outputMap
	
	synchronized (this) {
	    setDuration(cb.getDuration());
	    setRate(cb.getRate());
	    setIncrement(cb.getIncrement());
	}

	goToLimit(ControlBox.MAXPOS);  // calls resetMinMaxTimes

	cb.setPositionEnabled(true);

	cb.setButtonsEnabled(true);
	doPause();  // reset currentAction
    }

    /**********************************************************/
    /******************* play out the data ********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	play                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Play out the data in dur-sized chunks, until we hit  ***
  ***   the limits.                                             ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History:					***
  ***	01/11/2005	JPW	If duration=0, call		***
  ***				play_singlePoint().		***
  ***	01/10/2005	JPW	In duration=0 mode, update the	***
  ***				startTime, update the time	***
  ***				display on the Player GUI, and	***
  ***				send out the data *after* the	***
  ***				nap time.			***
  ***	12/14/2004	JPW	Fix bugs when operating in	***
  ***				single point mode.		***
  ***	10/15/2004	JPW	Add calculatedIncrement; use	***
  ***				this in calculations when	***
  ***				duration = 0			***
  ***								***
  *****************************************************************
*/
    /*
     * This version of the method handles both duration = 0 mode and
     * duration > 0 modes together.  This was the version of the method
     * used before I created play_singlePoint()
     *
     
    public void play(boolean forward) throws Exception {
	
	// startTime is the value used for start time in the last server
	//      request
	// lastStartTime() returns the latest start time of the data
	//      in outputMap (which is the most recent data obtained
	//      from the RBNB)
	
	// JPW 01/10/2005: for duration=0 case, this is the last data
	//                 actually output
	ChannelMap durationZeroLastDataOutput = null;
	
	// JPW 12/14/2004  NOTES:
	// startTime is the value used for start time in the last server
	//      request
	// When we are in single point mode (where duration = 0 and
	//      increment = 0), the following kind of procedure is used:
	// 1. In fetch():
	//    Submit a request to RBNB using startTime, duration = 0; if
	//    the user is playing forward then the reference string in the
	//    request will be set to "next"; if playing backward this will be
	//    "previous".
	// 2. Also in fetch(), call sink.Fetch().  The returned Channel map
	//    is stored in the variable outputMap
	// 3. When preparing to make the next request, call
	//    incrementStartTime().  For duration = 0 mode, this will set
	//    startTime equal to the value returned from lastStartTime().
	//    lastStartTime() examines all the channels in outputMap and
	//    returns the greatest of the start times.
	// 4. Go back to step 1.
	
	// prep player's state
	currentAction = RequestHandler.PLAY;
	initVariables();
	
	// JPW 01/11/2005: single-point mode is now handled in
	//                 play_singlePoint(); must call
	//                 initVariables() (above) before checking the
	//                 value of duration
	if (duration == 0.0) {
	    play_singlePoint(forward);
	    return;
	}
	
	// JPW 10/15/2004: Use calculatedIncrement in calculations when
	//                 duration = 0 (when duration = 0, it is also that
	//		   increment = 0)
	// double calculatedIncrement = lastStartTime() - startTime;
	double calculatedIncrement = 0.0;
	// JPW 12/13/2004: Problem when duration = 0 and user is at the max
	//                 position (from hitting the ">|" VCR button) and they
	//                 then hit play:
	//
	//                 outputMap doesn't contain channels and so
	//                 lastStartTime() returns -Double.MAX_VALUE;
	//                 startTime will get reset to this value when
	//                 incrementStartTime() is called
	//
	//                 NOTE: I can't just check to see if startTime equals
	//                 masterMin or masterMax (which are the time limits)
	//                 because for some reason there is a call to step()
	//                 in goToLimit()
	// JPW 12/17/2004: Bounds check
	resetMinMaxTimes();
	if ( ((startTime >= masterMax) && (forward)) ||
	     ((startTime <= masterMin) && (!forward)) )
	{
	    clearRateDisplay();
	    // Change to pause mode
	    acknowledgeInterrupt();
	    return;
	}
	
	double tempInc = 0.0;
	
	// check for stopping conditions
	boolean gotNull = false;
	
	// variables used to take naps, to maintain playback rate;
	// napError is used to deal with the variably-accurate
	// sleepAsNeeded()
	long napTime = 0;
	if (mode == cb.FRAMES_MODE) {
	    napTime = (long) (1000 / rate);
	} else {
	    // JPW 10/15/2004: Add check for duration = 0 (when duration = 0,
	    //		       it is also that increment = 0)
	    tempInc = increment;
	    if (duration == 0.) {
		tempInc = calculatedIncrement;
	    }
	    napTime = (long) ( (tempInc * 1000) / rate );
	}
	double napError = 0.0;
	
	// variables used to track the actual playback rate
	double avePlayTime;
	Timer playTimer = new Timer();
	
	// initialize rate display
	// JPW 10/15/2004: Add check for duration = 0 (when duration = 0,
	//		   it is also that increment = 0)
	tempInc = increment;
	if (duration == 0.) {
	    tempInc = calculatedIncrement;
	}
	avePlayTime = tempInc / rate;
	displayRate();
	
	// JPW 12/17/2004: Add bFirstTimeThrough to make play() a more
	//    stateless method.  This is specifically for duration = 0.0 mode.
	//    The first time through the loop, outputMap may be empty (no
	//    data), so lastStartTime() will return -Double.MAX_VALUE.  If we
	//    call incrementStartTime() in this case, startTime will get set to
	//    -Double.MAX_VALUE.  This will cause problems!  Therefore, just
	//    run through the loop once with the current value of startTime
	//    before incrementing this value.
	boolean bFirstTimeThrough = true;
	// play loop
	do {
	    playTimer.startTimer();
	    // pick up any changes user has made to mode and variables
	    if (handleVariableChanges() != NO_CHANGE) {
		
		// JPW 10/15/2004: Add check for duration = 0
		//                 (when duration = 0, it is also that
		//                 increment = 0)
		tempInc = increment;
		if (duration == 0.) {
		    tempInc = calculatedIncrement;
		}
		
		// factor of 1000 is to put napTime in millis, not seconds
		napTime = (long) ( (tempInc * 1000) / rate );
		napError = 0.0;
		
		// rate tracking
		avePlayTime = tempInc / rate;
		displayRate();
	    }
	    
	    // take a step through the data, and update GUI
	    // To keep this a "stateless" system, on the first time through
	    //    don't increment startTime.  This is particularly important
	    //    for duration=0 mode, because in this case in incrementStart-
	    //    Time() startTime gets set equal to the start time from the
	    //    last data obtained from the RBNB.  This might be a null
	    //    data set, in which case startTime would be set equal to
	    //    -Double.MAX_VALUE.  We want to avoid this situation.
	    if (!bFirstTimeThrough) {
 	        incrementStartTime(forward);
	    }
	    bFirstTimeThrough = false;
	    
	    // JPW 01/10/2005: In duration=0.0 mode, don't send out the data
	    //                 right away in step().  Rather, send it out after
	    //                 napping (see below).
	    if (duration != 0.0) {
	        gotNull = (step(forward, false, true) == GOTNULLFRAME);
		displayPosition();
	        evaluateLimits();  // adjusts slider position
	    } else {
		// duration = 0.0 mode
		// NOTE: Don't have step() send out data; we do that
		//       after the nap (see below)
		gotNull = (step(forward, false, false) == GOTNULLFRAME);
		
		// JPW 10/15/2004: Update calculatedIncrement and napTime
		//                 This needs to be done every loop iteration
		//                 since there is no set increment
	        calculatedIncrement = Math.abs(lastStartTime() - startTime);
		
		// NOTE: Can't just assume that calculatedIncrement is in sec
		// napTime = (long) ( (calculatedIncrement * 1000) / rate );
		if (cb.getTimeFormat() == ControlBox.TIME_FORMAT_MILLISECONDS) {
		    // calculatedIncrement is in millisec
		    napTime = (long) ( calculatedIncrement / rate );
		} else {
		    // calculatedIncrement is in sec
		    napTime = (long) ( (calculatedIncrement * 1000) / rate );
		}
		napError = 0.0;
		
		// rate tracking
		avePlayTime = tempInc / rate;
	    }
	    
	    // Check to see if we have fallen off of the edges
	    // of the input cache.  There are two different cases to check:
	    // 1. duration = 0.0: In this case startTime will never be
	    //        greater than masterMax or less than masterMin, so the
	    //        calculation to check if we've gone over an edge is
	    //        different than the duration > 0.0 case.  Just assume
	    //        if we've received an empty channel map (that is,
	    //        gotNull is true) that we're at an edge
	    // 2. duration > 0.0: Since the last call to resetMinMaxTimes(),
	    //        startTime might have incremented to an extent where it
	    //        has fallen out of the bounds of the data
	    
	    // JPW 12/13/2004: Add check for (duration == 0) && (gotNull)
	    if ( (duration == 0.) && (gotNull) ) {
		resetMinMaxTimes();
		if (forward) {
		    // We have reached the end of the data
		    goToLimit(ControlBox.MAXPOS);
		    break;
		} else {
		    // We have reached the start of the data
		    goToLimit(ControlBox.MINPOS);
		    break;
		}
	    }
	    // JPW 01/10/2005: Add new case
	    else if ( (duration > 0.0) && (gotNull) ) {
		resetMinMaxTimes();
		if  ( (startTime >= masterMax) && (forward) ) {
		    // Reached the end of data
		    goToLimit(ControlBox.MAXPOS);
		    break;
		} else if ( (startTime <= masterMin) && (!forward) ) {
		    // Reached the beginning of data
		    goToLimit(ControlBox.MINPOS);
		    break;
		}
	    }
	    // JPW 12/13/2004: Changed from if() to else if();
	    //                 add check that duration is greater than 0.0
	    else if (
		
		// JPW 12/13/2004: Add check that duration > 0.0
	        (duration > 0.0) &&
	        
		// 1. We got a null frame, or
		( (gotNull) ||
		
		// 2. While playing forwards, we played past the expected
		// end time AND picked up a frame with a gap of 50% the
		// duration or more at the frame's end, or
		( forward &&
		  (startTime >= masterMax) &&
		  ( ((lastEndTime() - startTime) / duration) <= 0.5 ) ) ||
		
		// 3. While playing backwards, we picked up a frame
		// with a gap of 50% the duration or more at the
		// frame's start.
		( !forward &&
		  (((lastStartTime() - startTime) / duration) >= 0.5) )
		) )
	    {
		
		resetMinMaxTimes();
		if (startTime >= masterMax) {
		    // We have reached the end of the input cache.
		    goToLimit(ControlBox.MAXPOS);
		    break;
		} else if (startTime <= masterMin) {
		    // We have "fallen off" the start of the input cache.
		    goToLimit(ControlBox.MINPOS);
		    break;
		}
	    }
	    
	    // apply playback rate control.
	    // sleepAsNeeded may be inaccurate on some systems, so
	    // we track the average difference between the desired nap length
	    // and the actual nap length, and apply a corective factor.
	    napError =
	        0.5 * napError +
		0.5 * sleepAsNeeded(
		          napTime - playTimer.getElapsed() - (long) napError);
	    
	    // JPW 01/10/2005: In duration=0 mode, update the startTime, update
	    //                 the time display on the Player GUI, and send
	    //                 out the data after nap time. This way the time
	    //                 that is displayed on the GUI will equal the time
	    //                 of the data from the last fetch and also the
	    //                 data output timing and rate will be appropriate
	    //
	    //    NOTE: Don't output the data if the user has interrupted.
	    if ( (duration == 0.0) && (!wasInterrupted()) ) {
		source.outputData(outputMap);
		durationZeroLastDataOutput = outputMap;
		incrementStartTime(forward);
		displayPosition();
	        evaluateLimits();  // adjusts slider position
	    } else if ( (duration == 0.0) && (wasInterrupted()) ) {
		// must set outputMap equal to the last data
		// we actually sent out
		if (durationZeroLastDataOutput != null) {
		    outputMap = durationZeroLastDataOutput;
		}
	    }
	    
	    // calculate actual playback rate and display to user
	    // Note: playTimer.getElapsed() returns milliseconds, so
	    //       we divide by 1000 here to convert to seconds
	    avePlayTime =
	        (0.0008 * playTimer.getElapsed()) + (0.2 * avePlayTime);
	    
	    // JPW 10/15/2004: Add check for duration = 0
	    tempInc = increment;
	    if (duration == 0.) {
		tempInc = calculatedIncrement;
	    }
	    
	    displayRate(tempInc/avePlayTime);
	    
	} while(!wasInterrupted());
	
	clearRateDisplay();
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }
    */

/*
  *****************************************************************
  ***								***
  ***	Name :	play                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2005 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Play out the data in dur-sized chunks, until we hit  ***
  ***   the limits.                                             ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History:					***
  ***	05/16/2005	JPW	User can change increment, dur-	***
  ***				ation, or rate - keep playing.	***
  ***				If they set duration = 0.0,	***
  ***				break out.			***
  ***	01/11/2005	JPW	If duration=0, call		***
  ***				play_singlePoint(); strip out	***
  ***				all duration=0 specific code.	***
  ***	01/10/2005	JPW	In duration=0 mode, update the	***
  ***				startTime, update the time	***
  ***				display on the Player GUI, and	***
  ***				send out the data *after* the	***
  ***				nap time.			***
  ***	12/14/2004	JPW	Fix bugs when operating in	***
  ***				single point mode.		***
  ***	10/15/2004	JPW	Add calculatedIncrement; use	***
  ***				this in calculations when	***
  ***				duration = 0			***
  ***								***
  *****************************************************************
*/
    public void play(boolean forward) throws Exception {
	
	// startTime is the value used for start time in the last server
	//      request
	// lastStartTime() returns the latest start time of the data
	//      in outputMap (which is the most recent data obtained
	//      from the RBNB)
	
	// prep player's state
	currentAction = RequestHandler.PLAY;
	initVariables();
	
	// JPW 01/11/2005: single-point mode is now handled in
	//                 play_singlePoint(); must call
	//                 initVariables() (above) before checking the
	//                 value of duration
	if (duration == 0.0) {
	    play_singlePoint(forward);
	    return;
	}
	
	// JPW 12/17/2004: Bounds check
	resetMinMaxTimes();
	if ( ((startTime >= masterMax) && (forward)) ||
	     ((startTime <= masterMin) && (!forward)) )
	{
	    clearRateDisplay();
	    // Change to pause mode
	    acknowledgeInterrupt();
	    return;
	}
	
	// check for stopping conditions
	boolean gotNull = false;
	
	// variables used to take naps, to maintain playback rate;
	// napError is used to deal with the variably-accurate
	// sleepAsNeeded()
	long napTime = 0;
	if (mode == cb.FRAMES_MODE) {
	    napTime = (long) (1000 / rate);
	} else {
	    napTime = (long) ( (increment * 1000) / rate );
	}
	double napError = 0.0;
	
	// variables used to track the actual playback rate
	double avePlayTime;
	Timer playTimer = new Timer();
	
	// initialize rate display
	avePlayTime = increment / rate;
	displayRate();
	
	// JPW 12/17/2004: Add bFirstTimeThrough to make play() a more
	//    stateless method.  This is specifically for duration = 0.0 mode.
	//    The first time through the loop, outputMap may be empty (no
	//    data), so lastStartTime() will return -Double.MAX_VALUE.  If we
	//    call incrementStartTime() in this case, startTime will get set to
	//    -Double.MAX_VALUE.  This will cause problems!  Therefore, just
	//    run through the loop once with the current value of startTime
	//    before incrementing this value.
	boolean bFirstTimeThrough = true;
	// play loop
	do {
	    playTimer.startTimer();
	    
	    // pick up any changes user has made to mode and variables
	    if (handleVariableChanges() != NO_CHANGE) {
		
		// JPW 01/11/2005: Just break if the user makes any changes;
		//                 basically we are avoiding the situation
		//                 where the user might go into single point
		//                 mode - don't want to have to call
		//                 play_singlePoint() from here
		// break;
		
		// JPW 05/16/2005: If duration has been reset to 0.0, break
		if (duration == 0.0) {
		    break;
		}
		
		// factor of 1000 is to put napTime in millis, not seconds
		napTime = (long) ( (increment * 1000) / rate );
		napError = 0.0;
		
		// rate tracking
		avePlayTime = increment / rate;
		displayRate();
		
	    }
	    
	    // take a step through the data, and update GUI
	    // To keep this a "stateless" system, on the first time through
	    //    don't increment startTime.  This is particularly important
	    //    for duration=0 mode, because in this case in incrementStart-
	    //    Time() startTime gets set equal to the start time from the
	    //    last data obtained from the RBNB.  This might be a null
	    //    data set, in which case startTime would be set equal to
	    //    -Double.MAX_VALUE.  We want to avoid this situation.
	    if (!bFirstTimeThrough) {
 	        incrementStartTime(forward);
	    }
	    bFirstTimeThrough = false;
	    
	    gotNull = (step(forward, false, true) == GOTNULLFRAME);
	    displayPosition();
	    evaluateLimits();  // adjusts slider position
	    
	    // Check to see if we have fallen off of the edges
	    // of the input cache. Since the last call to resetMinMaxTimes(),
	    // startTime might have incremented to an extent where it has
	    // fallen out of the bounds of the data
	    
	    if (gotNull) {
		resetMinMaxTimes();
		if  ( (startTime >= masterMax) && (forward) ) {
		    // Reached the end of data
		    goToLimit(ControlBox.MAXPOS);
		    break;
		} else if ( (startTime <= masterMin) && (!forward) ) {
		    // Reached the beginning of data
		    goToLimit(ControlBox.MINPOS);
		    break;
		}
	    }
	    // JPW 12/13/2004: Changed from if() to else if()
	    else if (
		
		// 1. We got a null frame, or
		(gotNull) ||
		
		// 2. While playing forwards, we played past the expected
		// end time AND picked up a frame with a gap of 50% the
		// duration or more at the frame's end, or
		( forward &&
		  (startTime >= masterMax) &&
		  ( ((lastEndTime() - startTime) / duration) <= 0.5 ) ) ||
		
		// 3. While playing backwards, we picked up a frame
		// with a gap of 50% the duration or more at the
		// frame's start.
		( !forward &&
		  (((lastStartTime() - startTime) / duration) >= 0.5) )
	      )
	    {
		
		resetMinMaxTimes();
		if (startTime >= masterMax) {
		    // We have reached the end of the input cache.
		    goToLimit(ControlBox.MAXPOS);
		    break;
		} else if (startTime <= masterMin) {
		    // We have "fallen off" the start of the input cache.
		    goToLimit(ControlBox.MINPOS);
		    break;
		}
	    }
	    
	    // apply playback rate control.
	    // sleepAsNeeded may be inaccurate on some systems, so
	    // we track the average difference between the desired nap length
	    // and the actual nap length, and apply a corective factor.
	    napError =
	        0.5 * napError +
		0.5 * sleepAsNeeded(
		          napTime - playTimer.getElapsed() - (long) napError);
	    
	    // calculate actual playback rate and display to user
	    // Note: playTimer.getElapsed() returns milliseconds, so
	    //       we divide by 1000 here to convert to seconds
	    // JPW 05/16/2005: Change the decay on the exponential average
	    //                 (only weigh current value at 1%)
	    // avePlayTime =
	    //     (0.0008 * playTimer.getElapsed()) + (0.2 * avePlayTime);
	    avePlayTime =
	        (0.00001 * playTimer.getElapsed()) + (0.99 * avePlayTime);
	    displayRate(increment/avePlayTime);
	    
	} while(!wasInterrupted());
	
	clearRateDisplay();
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	play_singlePoint               			***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is for single-point mode (duration = 0)	***
  ***	only.  It is called from play() and should not be	***
  ***	called directly.  This method plays out the data in	***
  ***	single-point size chunks until we hit the limits.       ***
  ***								***
  ***	NOTES:							***
  ***	1. When duration=0, it is also true that increment=0	***
  ***	2. startTime is the value used for start time in the	***
  ***	      last server request				***
  ***	   lastStartTime() returns the latest start time of the	***
  ***	      data in outputMap (which is the most recent data	***
  ***	      obtained from the RBNB)				***
  ***								***
  ***	General Play procedure for single point mode (dur = 0):	***
  ***	1. Request and fetch data from RBNB (request either	***
  ***	   "next" or "previous" depending on which direction	***
  ***	   the user is moving in)				***
  ***	2. If the ChannelMap we got back was empty, assume we	***
  ***	   are at a boundary and quit playing			***
  ***	3. Nap the appropriate amount of time such that this	***
  ***	   data is output at the actual time it is "due" out	***
  ***	4. Output the data					***
  ***	5. Update startTime to be the start time from the data	***
  ***	   that was just sent out; display this start time on	***
  ***	   the GUI.						***
  ***	6. Recalculate data rate and display it on the GUI.	***
  ***	7. Go back to step 1.					***
  ***								***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History:					***
  ***   07/06/2006	JPW	Update the time position label	***
  ***				in order to provide feedback to	***
  ***				the user (so they don't think	***
  ***				Player is just "stuck").	***
  ***	05/16/2005	JPW	Change the decay on the		***
  ***				exponential average calculation	***
  ***				of rate (only weigh current	***
  ***				value at 1%)			***
  ***				User can change rate or inc-	***
  ***				rement - keep playing.  Only if	***
  ***				duration != 0 do we break out.	***
  ***	01/11/2005	JPW	Created.  Code taken from	***
  ***				play()				***
  ***								***
  *****************************************************************
*/
    private void play_singlePoint(boolean forward) throws Exception {
	
	currentAction = RequestHandler.PLAY;
	initVariables();
	
	// Must call initVariable() before checking the value of duration
	if (duration != 0.0) {
	    throw new Exception("Error: not in single-point mode.");
	}
	
	// ChannelMap containing the last data this method output
	ChannelMap lastDataOutput = null;
	
	// Bounds check: change to pause mode if out of bounds
	resetMinMaxTimes();
	if ( ((startTime >= masterMax) && (forward)) ||
	     ((startTime <= masterMin) && (!forward)) )
	{
	    clearRateDisplay();
	    acknowledgeInterrupt();
	    return;
	}
	
	// This will be true if the last outputMap contains no data
	boolean gotNull = false;
	
	// Calculated nap time to maintain playback rate
	long napTime = 0;
	
	// Calculated value of increment.  This value will equal the difference
	// between the start times of two consecutive outputMaps from the RBNB.
	double calculatedIncrement = 0.0;
	
	// Variables used to track the actual playback rate
	double avePlayTime = 0.0;
	Timer playTimer = new Timer();
	
	// Initialize the rate display
	displayRate(0.0);
	
	// JPW 12/17/2004: Add bFirstTimeThrough to make play() a more
	//    stateless method.  This is specifically for duration = 0.0 mode.
	//    The first time through the loop, outputMap may be empty (no
	//    data), so lastStartTime() will return -Double.MAX_VALUE.  If we
	//    call incrementStartTime() in this case, startTime will get set to
	//    -Double.MAX_VALUE.  This will cause problems!  Therefore, just
	//    run through the loop once with the current value of startTime
	//    before incrementing this value.
	boolean bFirstTimeThrough = true;
	
	do {
	    playTimer.startTimer();
	    
	    // Handle pending mode or parameter changes.
	    // JPW 07/06/2006: add check on duration != 0
	    if ( (handleVariableChanges() != NO_CHANGE) || (duration != 0) ) {
		// break;
		
		// JPW 05/16/2005: If duration has been reset to some value
		//                 other than 0.0, break
		if (duration != 0.0) {
		    break;
		}
	    }
	    
	    // take a step through the data, and update GUI
	    // To keep this a "stateless" system, on the first time through
	    //    don't increment startTime.  This is particularly important
	    //    for duration=0 mode, because in this case in incrementStart-
	    //    Time() startTime gets set equal to the start time from the
	    //    last data obtained from the RBNB.  This might be a null
	    //    data set, in which case startTime would be set equal to
	    //    -Double.MAX_VALUE.  We want to avoid this situation.
	    if (!bFirstTimeThrough) {
 	        incrementStartTime(forward);
	    }
	    bFirstTimeThrough = false;
	    
	    // NOTE: Don't have step() output data; we do that after the nap
	    gotNull = (step(forward, false, false) == GOTNULLFRAME);
	    
	    // Assume if we've received an empty channel map (that is, gotNull
	    // is true) that we're at an edge
	    if (gotNull) {
		if (forward) {
		    // We have reached the end of the data
		    goToLimit(ControlBox.MAXPOS);
		    break;
		} else {
		    // We have reached the start of the data
		    goToLimit(ControlBox.MINPOS);
		    break;
		}
	    }
	    
	    // Update calculatedIncrement and napTime (needs to be done every
	    // loop iteration since there is no set increment)
	    // Note that napTime is in millisec
	    calculatedIncrement = Math.abs(lastStartTime() - startTime);
	    if (cb.getTimeFormat() == ControlBox.TIME_FORMAT_MILLISECONDS) {
		// calculatedIncrement is in millisec
		napTime = (long) ( calculatedIncrement / rate );
	    } else {
		// calculatedIncrement is in sec
		napTime = (long) ( (calculatedIncrement * 1000) / rate );
	    }
	    
	    // Nap to maintain playback rate control
	    // sleepAsNeeded(napTime - playTimer.getElapsed());
	    // JPW 07/06/2006
	    // Update the time position label in order to provide feedback to
	    // the user (so they don't think Player is just "stuck").  However,
	    // don't call evaluateLimits() because it references startTime to
	    // adjust the slider position (but we haven't actually updated
	    // startTime).
	    // totalNapTime is in milliseconds
	    long totalNapTime = napTime - playTimer.getElapsed();
	    if (totalNapTime < 2000) {
		sleepAsNeeded(totalNapTime);
	    } else {
		// total amount of time we have slept thus far in milliseconds
		long totalSlept = 0;
		while ( (totalSlept < totalNapTime) &&
		        (!wasInterrupted())         &&
		        (handleVariableChanges() == NO_CHANGE) )
		{
		    if ( (totalSlept + 2000) > totalNapTime) {
			sleepAsNeeded(totalNapTime - totalSlept);
			totalSlept = totalNapTime;
		    } else {
			sleepAsNeeded(2000);
			totalSlept += 2000;
		    }
		    // Update GUI
		    if (cb.getTimeFormat() ==
			    ControlBox.TIME_FORMAT_MILLISECONDS)
		    {
			displayPosition(
			    startTime + (totalSlept * rate) );
		    } else {
			displayPosition(
			    startTime + ((totalSlept / 1000.0) * rate) );
		    }
		    // See note above about why we don't call evaluateLimits()
		    // evaluateLimits();
		}
		if (wasInterrupted()) {
		    // Set time displayed in GUI back to the original startTime
		    displayPosition();
		}
	    }
	    
	    // Update startTime, GUI time display, and output the data
	    // In this manner, the data that the user receives will have the
	    // same start timestamp as the time shown on the GUI.  Also,
	    // outputting the data *after* the nap keeps this output data
	    // stream synchronized with how the data was input.
	    if (!wasInterrupted()) {
		source.outputData(outputMap);
		lastDataOutput = outputMap;
		incrementStartTime(forward);
		displayPosition();
	        evaluateLimits();  // adjusts slider position
	    } else {
		// Must set outputMap equal to the last data we actually sent
		// out.  Otherwise, other code elsewhere will use the newer
		// outputMap that we have obtained from the RBNB but not yet
		// sent out, thus causing an inconsistancy.
		if (lastDataOutput != null) {
		    outputMap = lastDataOutput;
		}
	    }
	    
	    // Calculate actual playback rate and display to user
	    // Make sure to calculate avePlayTime in seconds
	    double incrementInSeconds = calculatedIncrement;
	    if (cb.getTimeFormat() == ControlBox.TIME_FORMAT_MILLISECONDS) {
		// calculatedIncrement is in millisec; convert to seconds
		incrementInSeconds = calculatedIncrement / 1000.0;
	    }
	    avePlayTime = incrementInSeconds / rate;
	    // playTimer.getElapsed() returns milliseconds; convert to seconds
	    // by dividing by 1000
	    // JPW 05/16/2005: Change the decay on the exponential average
	    //                 (only weigh current value at 1%)
	    // avePlayTime =
	    //     (0.0008 * playTimer.getElapsed()) + (0.2 * avePlayTime);
	    avePlayTime =
	        (0.00001 * playTimer.getElapsed()) + (0.99 * avePlayTime);
	    displayRate(incrementInSeconds/avePlayTime);
	    
	} while(!wasInterrupted());
	
	clearRateDisplay();
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	takeAStep                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	This method is called when the user hits either step	***
  ***	button (forward, |>, or backward, <|) or when the user	***
  ***	is sliding the Position slider around.			***
  ***								***
  ***	Description :						***
  ***	   Play for a set duration and then cease.              ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    public void takeAStep(int pos,
                          boolean forwardI,
			  boolean evalLimits)
    throws Exception
    {
	
	// JPW 12/28/2004
	// Problem when in single-point (duration = 0.0) mode:
	// 
	// If the user has been playing forward or if they have been manually
	// moving the position slider (which really has the same effect as
	// playing forward because the request is always for "next" in this
	// case) and then if they hit the step back (<|) button, the first time
	// they hit this step back button the position slider will actually
	// jump forward in time, not backward as would be expected!  On
	// consecutive backward steps the position cursor will move backward
	// as expected.  The unexpected forward jump occurs because when
	// incrementStartTime() is called in this method, startTime will be
	// set equal to the latest start time value found in outputMap.  Since
	// the previous actions have been playing forward in time, this new
	// value of startTime is LATER than the one displayed on the GUI.  The
	// new value of startTime is then displayed on the GUI, which jumps the
	// position cursor ahead, not backward.  This is the startTime which is
	// then used in the call to sink.Request(); duration = 0.0 and
	// request string = "previous" is used in this request.
	//
	// Note that the same type of behavior occurs if the user has been
	// playing backward in time and then does a step forward (|>).  In this
	// case, the cursor on the position display unexpectedly jumps back
	// in time, not forward in time as expected.  This is because the
	// updated value of startTime after calling incrementStartTime() is
	// smaller than the current value of startTime (smaller because the
	// user had just been playing backward in time).
	//
	// SOLUTION: After calling step(forwardI) below, update the value of
	//           startTime by calling incrementStartTime(); see below.
	
	// JPW 12/17/2004
	// If the user is pressing on the step forward (|>) or on
	// the step backward (<|) button then evalLimits is true and
	// forwardI is set appropriately (true or false depending on
	// which direction the user has pressed).  Also in this case,
	// pos is set to Integer.MIN_VALUE.  If the user is moving the
	// position slider or any other slider bar (which all result
	// in this method being called) then evalLimits is set false and
	// the value of forwardI is *always* set true (even if it is the
	// position slider being moved, forwardI is always true
	// regardless of which way the slider is being moved).
	boolean bUsingSlider = true;
	if (pos == Integer.MIN_VALUE) {
	    bUsingSlider = false;
	}
	
	/*
	System.err.println(
	    "\n\ntakeAStep(): pos = " + pos +
	    "\n\tforward = " + forwardI +
	    "\n\tevalLimits = " + evalLimits +
	    "\n\tstartTime = " + startTime +
	    "\n\tlastStartTime() return = " + lastStartTime());
	*/
	
	// JPW 12/15/2004: If evalLimits is true, reset min/max times
	if (evalLimits) {
	    resetMinMaxTimes();
	}
	
	// Given the position on the Position slider, calculate what the
	// corresponding start time is; set startTime to this value
	if (bUsingSlider) {
	    setStartFromPos(pos);
	}
	
	// JPW 12/15/2004: Do limit checks
	// Note: When bUsingSlider is false, this means the user has pressed
	//       on either of the step buttons; the value of forwardI given
	//       to this method is accurate in this case.
	if (!bUsingSlider) {
	    if ( ( (startTime >= masterMax) && (forwardI) ) ||
	         ( (startTime <= masterMin) && (!forwardI) ) )
	    {
		evaluateLimits();
		acknowledgeInterrupt();
		return;
	    }
	} else {
	    if ( (startTime >= masterMax) || (startTime <= masterMin) ) {
		evaluateLimits();
		acknowledgeInterrupt();
		return;
	    }
	}
	
	currentAction = RequestHandler.STEP;
	
	handleVariableChanges();
	
	// JPW 12/17/2004: Only increment start time if using one of the
	//                 step buttons.
	if (!bUsingSlider) {
	    // JPW 12/17/2004: A special case: if duration = 0.0 and
	    //                 lastStartTime() returns -Double.MAX_VALUE, then
	    //                 if we call incrementStartTime(), startTime
	    //                 will be set to -Double.MAX_VALUE, which we don't
	    //                 want to happen.  Therefore, trap this case.
	    if (!((duration == 0.0) && (lastStartTime() == -Double.MAX_VALUE)))
	    {
		incrementStartTime(forwardI);
	    }
	}
	
	step(forwardI, bUsingSlider, true);
	
	// JPW 12/28/2004: Update startTime to be equal to the latest start
	//                 time from the returned outputMap; that way, when
	//                 updatePosition() is called (which updated the text
	//                 indicating position on the position slider) and also
	//                 when evaluateLimits() is called (which updates the
	//                 position of the cursor on the position slider) we
	//                 will be using the actual time of the current point.
	//                 This takes care of the problem described at the top
	//                 of this method.
	if ( (!bUsingSlider) && (duration == 0.0) ) {
	    // JPW 01/10/2005: If we didn't get data from the last fetch
	    //                 lastStartTime() will return -Double.MAX_VALUE;
	    //                 go to either the beginning of data or the end
	    //                 of data as is appropriate
	    if (lastStartTime() == -Double.MAX_VALUE) {
		// System.err.println(
		//     "takeAStep(): lastStartTime() = -Double.MAX_VALUE");
		if (forwardI) {
	            startTime = masterMax;
		} else {
		    startTime = masterMin;
		}
	    } else {
		incrementStartTime(forwardI);
	    }
	}
	
	displayPosition();
	
	if (evalLimits) {
	    // JPW 12/15/2004: This was just done above; don't need to call
	    //                 resetMinMaxTimes() again
	    // resetMinMaxTimes();
	    // Update the position of the cursor on the position slider
	    evaluateLimits();
	}
	
	// we may have been interrupted
	acknowledgeInterrupt();
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	takeAStep                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Play for a set duration and then cease.              ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	12/15/2004	JPW	Eliminate this method; just use	***
  ***				the version found above.	***
  ***								***
  *****************************************************************
*/

/*
    public void takeAStep(boolean forward, boolean evalLimits)
        throws Exception
    {
	currentAction = RequestHandler.STEP;
	
	handleVariableChanges();

	if (evalLimits) {
	    // actually, isn't the execution of this more
	    // dependent upon whether the slider
	    // has just been moved by the user or not?
	    incrementStartTime(forward);
	}
	
	step(forward);
	
	displayPosition();
	if (evalLimits) {
	    resetMinMaxTimes();
	    evaluateLimits();
	}
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }
*/

/*
  *****************************************************************
  ***								***
  ***	Name :	goToLimit                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Step to the beginning or end.                        ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    public void goToLimit(int pos) throws Exception {
	
	// sanity check
	if ((pos != ControlBox.MINPOS) &&
	    (pos != ControlBox.MAXPOS)) {
	    return;
	}
	
	currentAction = RequestHandler.STEP;
	resetMinMaxTimes();  // call this before setting startTime.
	handleVariableChanges();
	
	if (pos == ControlBox.MINPOS) {  // BOD
	    startTime = masterMin;
	} else {  // EOD
	    // MAXPOS represents the end time of the last data,
	    // not the starttime of the last data!
	    startTime = masterMax - duration;
	}
	
	// JPW 01/10/2005: set the bForceAbsolute argument true; when we are
	//                 in duration=0 mode this will make an absolute
	//                 request rather than request next or previous
	// JPW 12/16/2004: I'm not sure why this call to step() is made
	int returnVal = step(true, true, true);
	
	/*
	System.err.println(
	    "goToLimit(): called step(true, true, true); return = " +
	    returnVal +
	    ", outputMap.NumberOfChannels = " +
	    outputMap.NumberOfChannels());
	*/
	
	displayPosition();
	
	// reset slider location
	cb.setPosition(pos);
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :  realtime                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Realtime continuously.                               ***
  ***		                                                ***
  ***	11/28/01 UCB - Updated the method to improve realtiming ***
  ***                  Method now dynamically adjusts its       ***
  ***                  sleeps, to avoid sleeping so long that   ***
  ***                  it misses data, or so short that it      ***
  ***                  wastes CPU cycles.                       ***
  ***	10/11/04 JPW - Upgrade to RBNB V2			***
  ***	12/22/04 JPW - Handle duration = 0.0 case in the new	***
  ***			method realtime_singlePoint()		***
  ***		                                                ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    public void realtime() throws Exception {
	/**** PREP PLAYER'S STATE ****/
	initVariables();
	
	// JPW 12/22/2004: single-point mode is now handled in
	//                 realtime_singlePoint(); must call
	//                 initVariables() (above) before checking the
	//                 value of duration
	if (duration == 0.0) {
	    realtime_singlePoint();
	    return;
	}
	
	currentAction = RequestHandler.RT;
	cb.setPosition(ControlBox.MAXPOS);
	cb.setRateToDefault();
	cb.setRateEnabled(false);
	resetMinMaxTimes();
	startTime = masterMax - duration - increment;
	
	/**** VARIABLE DECLARATIONS AND INITIALIZATIONS ****/
	// Nap-length control; reset whenever mode or increment may have changed.
	long napAdjust = 0;
	long baseNap = (long) (1000 * increment);
	Timer rtTimer = new Timer();
	
	// Variables to determine if we are running too slow or fast.
	double latestPoint = 0.0;
	double lastStart = startTime;
	double gotDiff = 0.0;
	double incPlusLat = RT_MAX_LATENCY * increment;

	// Variables to track actual rate
	// rtRate is equal to the difference between the starttimes of two
	// sequential data retrievals, divided by the length of elapsed time
	// between outputting those data.
	Timer rtRateTimer = new Timer();
	double lastRespStart = startTime - increment;
	double currRespStart = 0.0;
	double rtRate = rate;
	
	// useful variables
	int result;
	long junk;
	boolean skipToEnd = false;

	/**** MEASURED RATE INITIALIZATIONS ****/
	// Note that rtTimer and rtRateTimer are completely different.
	// rtTimer times the passes through the Main Loop, and is used
	// only to adjust the naptimes.  rtRateTimer is used to track the
	// length of time that passes between two sequential calls to 
	// source.outputData(), as a part of measuring the actual playback
	// rate.
	rtRateTimer.startTimer();
	displayRate();

	/**** MAIN RT LOOP ****/
	do {
	    // 1. Start timing our work.
	    rtTimer.startTimer();
	    
	    // 2. Handle pending mode or parameter changes.
	    if ((result = handleVariableChanges()) != NO_CHANGE) {
		
		/*
		 * JPW 12/23/2004: If there has been any change, just break out
		 *                 of the do...while() loop
		
		// JPW 10/11/2004: Originally, handleVariableChanges() would
		//                 check whether there was a mode change (from
		//                 FRAME mode to TIME mode or vice versa)
		//                 and/or whether there were any changes in the
		//                 values of duration, increment, or rate on
		//                 the GUI. There is no more mode changes, so
		//                 this will now just pick up changes in the
		//                 other GUI parameters.
		
		// Reset lastStart to match new parameters.  If there has been a
		// mode change, masterMin and masterMax will have been reset and
		// will now be accurate.  Otherwise, force a reset.
		if (result == PARAM_CHANGE) {
		    // JPW 10/11/2004: Not sure if we need to call
		    //    resetMinMaxTimes(); this will do a server round-
		    //    trip and calculate the overall min and max times for
		    //    all chans in requestMap.  Why is this needed if the
		    //    user has just changed something on GUI?
		    resetMinMaxTimes();
		}
		// Set lastStart so that realtime will pick up
		// the next frame of data starting at masterMax.
		lastStart = masterMax - increment;
		
		// Reset increment plus extra latency, as increment has changed.
		incPlusLat = RT_MAX_LATENCY * increment;
		
		// Reset the nap variables: napAdjust and baseNap.
		napAdjust = 0;
		baseNap = (long) (1000 * increment);
		
		// Reset the variables for tracking playback rate:
		// lastRespRate and rtRate.
		lastRespStart = lastStart;
		rtRate = rate;
		
		// JPW 12/22/2004: If user has changed to single-point mode,
		//                 call realtime_singlePoint()
		if (duration == 0.0) {
		    realtime_singlePoint();
		    return;
		}
		
		*/
		
		break;
		
	    }  // end handling changes
	    
	    // 3. Determine what the newest available time is, and what the
	    //    difference is between it and the time of the last data we
	    //    received.
	    // fetchNewestTime() does a registration request; lastEndTime()
	    // will then look at the channel map returned from the registration
	    // request and calculate the latest end time.
	    fetchNewestTime();
	    if ((latestPoint = lastEndTime()) == -Double.MAX_VALUE) {
		// 04/26/02 UCB - assume input has gone away entirely;
		// stop realtiming.
		break;
	    }
	    // latestPoint = the newest time of data in the server
	    // lastStart = start time of the last data request sent to server
	    //        (it is set equal to the variable startTime)
	    gotDiff = latestPoint - lastStart - duration;
	    
	    // 4. Take action based on result in step 3:
	    // JPW 12/22/2004:
	    // NOTE: If gotDiff was less than 0.0, this means that latestPoint
	    //   is less than (lastStart + duration); don't we want to just
	    //   sleep some more in this case instead of setting startTime
	    //   equal to (latestPoint - duration)?  Otherwise, won't we end
	    //   up getting repeated data?  However, it doesn't make sense that
	    //   latestPoint could be less than (lastStart + duration) since
	    //   data doesn't go backward in time
	    if ( (gotDiff >= increment) || (gotDiff < ZERO_TIME) ) {
		if ( (gotDiff > incPlusLat) || (gotDiff < ZERO_TIME) ) {
		    // 4a.  We are too far behind.  We will skip to the end
		    // of the available data.
		    skipToEnd = true;
		    startTime = latestPoint - duration;
		} else {
		    // 4b. We are within our desired window: the distance from the
		    // last retrieved data and the latest available data is less
		    // than or equal to the increment plus the extra latency.
		    skipToEnd = false;
		    incrementStartTime(true);
		}
		
		fetch(true, false);
		
		// JPW 10/11/2004: NOTE: In RBNB V2, outputMap will only have
		//                       channels with actual data in them
		if (removeNullFrames(outputMap) != 0) {
		    // Output the data and update our displayed position.
		    lastStart = startTime;
		    source.outputData(outputMap);
		    displayPosition(lastStart);
		    
		    // Update our display of the measured rate.
		    currRespStart = lastStartTime();
		    // This rate calculation assumes that time from the server
		    // is in seconds; in this calculation we convert
		    // (currRespStart - lastRespStart) to milliseconds
		    rtRate =
		        0.2 * rtRate +
			0.8 * 1000 * (currRespStart - lastRespStart) /
			                            rtRateTimer.getElapsed();
		    displayRate(rtRate);
		    rtRateTimer.startTimer();
		    
		    lastRespStart = currRespStart;
		}
		
		if (gotDiff > ZERO_TIME) {
		    // DO NOT adjust the naptimes if gotDiff is negative.
		    // The amount we overslept is indicated by how large
		    // gotDiff is
		    napAdjust -=
		      (long)((skipToEnd ? 1000 : 500) * (gotDiff - increment));
		    if (napAdjust < -baseNap) {
			// do not let baseNap + napAdjust become less than zero
			napAdjust = -baseNap;
		    }
		}
	    
	    } else {
		// 4c. We underslept: the newest data is less than
		// one increment away from the last data we output.
		// Nap more; increase napAdjust or baseNap, according
		// to the mode.
		
		// Times mode does not alter baseNap, which is equal to the
		// increment.  Times mode increases and decreases a napAdjust
		// to addto baseNap.
		// junk = (long) (1000 * ((lastStart.addTime(increment).addTime(duration)).
		// 			   subtractTime(latestPoint)).getDoubleValue());
		
		junk =
		    (long) ( 1000 * 
		             (lastStart + increment + duration - latestPoint));
		
		if (junk > 2000) {
		    // Do not nap more than two seconds when we undersleep.
		    // This keeps our naps from growing without bound on a sluggish
		    // or paused input stream.
		    junk = 2000;
		}
		sleepAsNeeded(junk - rtTimer.getElapsed());
		napAdjust += junk;
		if (napAdjust > 2000) {
		    // Do not let napAdjust grow without bound.
		    napAdjust = 2000;
		}
		
		continue;  // don't nap twice.
	    }
	    
	    // 5. Nap
	    sleepAsNeeded(baseNap + napAdjust - rtTimer.getElapsed());
	    
	} while(!wasInterrupted());
	
	/**** CLEAN-UP ****/
	resetMinMaxTimes();
	startTime = masterMax;
	clearRateDisplay();
	
	cb.setRateEnabled(true);
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :  realtime_singlePoint           			***
  ***	By   :	John P. Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2004					***
  ***								***
  ***	Copyright 2004 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Realtime continuously; developed specifically for	***
  ***	single-point mode (duration = 0.0 and increment = 0.0).	***
  ***		                                                ***
  ***	01/11/05 JPW - Changed from public to private method;	***
  ***			this method should only be called from	***
  ***			realtime()
  ***	10/11/04 JPW - Created					***
  ***		                                                ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    private void realtime_singlePoint() throws Exception {
	/**** PREP PLAYER'S STATE ****/
	initVariables();
	
	// Must call initVariable() before checking the value of duration
	if (duration != 0.0) {
	    throw new Exception("Error: not in single-point mode.");
	}
	
	currentAction = RequestHandler.RT;
	cb.setPosition(ControlBox.MAXPOS);
	cb.setRateToDefault();
	cb.setRateEnabled(false);
	resetMinMaxTimes();
	
	long napTime = 10;
	
	double timeCurrPoint = 0.0;
	double timeLastPoint = 0.0;
	
	// Variables to track actual rate
	// rtRate is equal to the difference between the start times of two
	// sequential data retrievals, divided by the length of elapsed time
	// between outputting those data.
	Timer rtRateTimer = new Timer();
	double rtRate = rate;
	displayRate(rtRate);
	
	rtRateTimer.startTimer();
	
	// JPW 02/16/2005: Try using an "after" request
	//                 perform an initial request to find the starting time
	/*
	 * This goes along with trying "after" mode
	copyRequestMap();
	sink.Request(outputMap,
                     0.0,
                     0.0,
                     "newest");
	outputMap = sink.Fetch(-1, outputMap);
	boolean bSkipDoWhileLoop = false;
	if (removeNullFrames(outputMap) == 0) {
	    // No data; assume an error occurred;
	    // the source may no longer exist
	    // Don't enter the do...while loop
	    bSkipDoWhileLoop = true;
	} else {
	    timeCurrPoint = lastStartTime();
	}
	*/
	
	copyRequestMap();
	sink.Monitor(outputMap, 0);
	
	do {
	    
	    /* This goes along with trying "after" mode
	    if (bSkipDoWhileLoop) {
		break;
	    }
	    */
	    
	    // Handle pending mode or parameter changes.
	    if (handleVariableChanges() != NO_CHANGE) {
		break;
	    }
	    
	    // Clear out outputMap
	    copyRequestMap();
	    
	    /* Attempt #1: Request "newest"
	     * This is the way we used to do realtime mode - just ask for
	     * "newest".  Problem arises when the request map has multiple
	     * channels where one channel is advancing and the other channel
	     * is not updating: the request will return the new data on the
	     * chan that is updating, but it will return the same data point
	     * for the chan that is static
	    sink.Request(outputMap,
                         0.0,
                         0.0,
                         "newest");
	    outputMap = sink.Fetch(-1, outputMap);
	    */
	    
	    
	    
	    /* Attempt #2: Request "after"
	     *
	    sink.Request(outputMap,
                         timeCurrPoint,
                         0.0,
                         "after");
	    outputMap = sink.Fetch(-1, outputMap);
	    */
	    
	    // Change fetch time from -1 to 1000
	    // with Fetch(-1), if no new data is coming in, this method
	    // will not return
	    outputMap = sink.Fetch(1000, outputMap);
	    
	    if (removeNullFrames(outputMap) == 0) {
		// No data; assume an error occurred;
		// the source may no longer exist
		// break;
		
		// Change from break to continue
		continue;
	    }
	    
	    // Output the data and update our displayed position and rate
	    double tempTime = lastStartTime();
	    if (tempTime == timeCurrPoint) {
		// This is the same data point we obtained last time
		// Don't send out this data; increase nap time
		napTime = napTime * 2;
	    } else {
		// Got a new data point: send out data and decrease nap time
		// First, update the times
		timeLastPoint = timeCurrPoint;
		timeCurrPoint = tempTime;
		napTime = napTime / 2;
		source.outputData(outputMap);
		displayPosition(timeCurrPoint);
		rtRate =
		    (0.2 * rtRate) +
		    (0.8 * 1000.0 * (timeCurrPoint - timeLastPoint) /
			                          rtRateTimer.getElapsed());
		displayRate(rtRate);
		rtRateTimer.startTimer();
	    }
	    
	    // Put limits on sleep time
	    if (napTime < 10) {
		napTime = 10;
	    } else if (napTime > 600) {
		napTime = 600;
	    }
	    sleepAsNeeded(napTime);
	} while(!wasInterrupted());
	
	/**** CLEAN-UP ****/
	resetMinMaxTimes();
	startTime = masterMax;
	clearRateDisplay();
	
	cb.setRateEnabled(true);
	
	// we may have been interrupted
	acknowledgeInterrupt();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :  doPause                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   If play(), takeAStep() or realtime() exit normally,  ***
  ***   call doPause() to cause the pause button to be lit up.  ***
  ***								***
  ***	12/21/00 - UCB: I am making this method public so that  ***
  ***	RequestHandler may call it AFTER terminating any other  ***
  ***	action (play, RT, etc.) through an interupt, and BEFORE ***
  ***	displaying any dialog box, etc.  Note that interruptions***
  ***   of actions do not typically cause the pause button to be***
  ***   toggled, as it is usually the press of some other       ***
  ***   button which caused the interrupt.                      ***
  ***								***
  *****************************************************************
*/
    public void doPause() {
	currentAction = RequestHandler.PAUSE;
	cb.toggleButton(ControlBox.PAUSE);
    }

    /**********************************************************/
    /******************* handle interupts *********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :  interrupt                    			***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Signal the PlayerEngine to stop its current activity.***
  ***	          ASYNCHRONOUS STATE-CHANGE METHOD              ***
  ***								***
  *****************************************************************
*/
    public synchronized void interrupt() {
	// when paused or disconnected, there is nothing to
	// interrupt.  Connecting and setting channels are not
	// permitted to be interupted.
	if ((currentAction != RequestHandler.PAUSE) &&
	    (currentAction != RequestHandler.DISCONNECT) &&
	    (currentAction != RequestHandler.CONNECT) &&
	    (currentAction != RequestHandler.CHANSELECT)) {
	    interrupted = true;
	    wakeUp();
	} 
    }

/*
  *****************************************************************
  ***								***
  ***	Name :  wasInterrupted                    		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Check to see if the PlayerEngine has been interrupted***
  ***	          ASYNCHRONOUS STATE-CHECK METHOD               ***
  ***								***
  *****************************************************************
*/
    private synchronized boolean wasInterrupted() {
	return interrupted;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :  acknowledgeInterrupt                    	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Signal that the PlayerEngine has responded to the    ***
  ***   interrupt; reset interrupted flag.                      ***
  ***	          ASYNCHRONOUS STATE-CHANGE METHOD              ***
  ***								***
  *****************************************************************
*/
    private synchronized void acknowledgeInterrupt() {
	if (interrupted) {
	    currentAction = RequestHandler.PAUSE;
	    interrupted = false;
	} else {
	    doPause();
	}
    }

    /**********************************************************/
    /**************** position-setting methods ****************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	setStartFromPos                    		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Takes the position slider value as input; sets       ***
  ***   startTime from it.                                      ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    private void setStartFromPos(int pos) {
	// calculate pos as Time.
	double percentage = (((double)(pos - ControlBox.MINPOS)) / 
			     ((double)(ControlBox.MAXPOS - ControlBox.MINPOS)));
	double val =
	    (percentage * (masterMax - masterMin)) + masterMin;
	
	startTime = val;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	incrementStartTime                    		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Increments the startTime according to the direction  ***
  ***   of play.                                                ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	10/14/2004	JPW	Add case for duration = 0	***
  ***								***
  *****************************************************************
*/
    private void incrementStartTime(boolean forward) {
	
	// JPW 10/14/2004: Add special case for duration = 0.
	//                 Base startTime on the start time in the last
	//                 retreived channel map.
	if (duration == 0) {
	    startTime = lastStartTime();
	}
	else {
	    if (forward) {
	        startTime = startTime + increment;
	    } else { // going backwards
	        startTime = startTime - increment;
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	recalcStart()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Recalculates the startTime, changing from one time   ***
  ***   base to another.                                        ***
  ***								***
  ***	Modification History					***
  ***	10/08/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    private void recalcStart(double oldStart, double newMin, double newMax) {
	double timeRatio = (newMax - newMin) / (masterMax - masterMin);
	double newStart = (oldStart - masterMin) * timeRatio;
	startTime =  newStart + newMin;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	updatePosition                        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Resets the time base for the position.               ***
  ***								***
  ***	Modification History					***
  ***	10/08/2004	JPW	Took out frame mode		***
  ***								***
  *****************************************************************
*/
    private synchronized void updatePosition() {
	// JPW 10/08/2004: Only time mode now (no more frame mode)
	recalcStart(startTime, timeMin, timeMax);
	masterMax = timeMax;
	masterMin = timeMin;
    }

    /**********************************************************/
    /********** duration, rate and increment setting **********/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	setDuration                        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Resets the duration.                                 ***
  ***	          ASYNCHRONOUS STATE-CHANGE METHOD              ***
  ***								***
  *****************************************************************
*/
    public synchronized void setDuration(double dur) {
	pendingDurTime = dur;
	paramChanged = true;
	wakeUp();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setRate                         		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Resets the rate.                                     ***
  ***	          ASYNCHRONOUS STATE-CHANGE METHOD              ***
  ***								***
  *****************************************************************
*/
    public synchronized void setRate(double rateI) {
	pendingRate = rateI;
	paramChanged = true;
	wakeUp();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setIncrement                       		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Resets the increment width.                          ***
  ***      11/02/01 UCB - changed name from setStep(), to be    ***
  ***                     consistent with ControlBox.           ***
  ***	          ASYNCHRONOUS STATE-CHANGE METHOD              ***
  ***								***
  *****************************************************************
*/
    public synchronized void setIncrement(double incrementI) {
	pendingIncrement = incrementI;
	paramChanged = true;
	wakeUp();
    }

    /**********************************************************/
    /**************** handle parameter changes ****************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	initVariables                            	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Safely picks up any pending changes, resetting the   ***
  ***   corresponding global variables.                         ***
  ***								***
  *****************************************************************
*/
    private void initVariables() {
	synchronized (this) {
	    modeChanges();
	    paramChanges();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	handleVariableChanges                          	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Safely picks up any pending changes, resetting the   ***
  ***   corresponding global variables.                         ***
  ***								***
  *****************************************************************
*/
    private int handleVariableChanges() {
	synchronized (this) {
	    if (modeChanged) {
		modeChanges();
		paramChanges();
		return MODE_CHANGE;
	    } else if (paramChanged) {
		paramChanges();
		return PARAM_CHANGE;
	    }
	    return NO_CHANGE;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	modeChanges                              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Picks up new mode and request flag, and updates      ***
  ***   position.                                               ***
  ***   ONLY CALL FROM HANDLEVARIABLECHANGES OR INITVARIABLES.  ***
  ***								***
  *****************************************************************
*/
    private void modeChanges() {
	mode = pendingMode;
	// requestFlag = pendingReqFlag;
	updatePosition();
	modeChanged = false;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	paramChanges                              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Picks up new duration, increment and rate.           ***
  ***   ONLY CALL FROM HANDLEVARIABLECHANGES OR INITVARIABLES.  ***
  ***								***
  *****************************************************************
*/
    private void paramChanges() {
	duration = pendingDurTime;
	increment = pendingIncrement * duration;
	rate = pendingRate;
	paramChanged = false;
    }
    
    /**********************************************************/
    /************ request flag and mode accessors *************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	addToReqFlag                        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Adds a flag to the requestFlag variable.             ***
  ***   Note that this method checks for problems with the      ***
  ***   DataRequest.aligned flag.                               ***
  ***								***
  *****************************************************************
*/
    private synchronized void addToReqFlag(int flag) {
	
	// JPW 10/11/2004: took out the use of the flags
	// pendingReqFlag |= flag;

	// 11/20/01 UCB - this test no longer needed
	// code to add or remove the aligned flag as needed
//  	if (testFlag(pendingReqFlag, DataRequest.startFrame)) {
//  	    pendingReqFlag &= ~DataRequest.aligned;
//  	} else if (testFlag(pendingReqFlag, 
//  			    DataRequest.oldest | DataRequest.newest)) {
//  	    pendingReqFlag |= DataRequest.aligned;
//  	}

	modeChanged = true;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	removeFromReqFlag                        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Removess a flag from the requestFlag variable.       ***
  ***   Note that this method checks for problems with the      ***
  ***   DataRequest.aligned flag.                               ***
  ***								***
  *****************************************************************
*/
    private synchronized void removeFromReqFlag(int flag) {
	
	// JPW 10/11/2004: Took out the use of the flags
	// pendingReqFlag &= ~flag;

	// 11/20/01 UCB - this test no longer needed
	// code to add or remove the aligned flag as needed
//  	if (testFlag(pendingReqFlag, DataRequest.startFrame)) {
//  	    pendingReqFlag &= ~DataRequest.aligned;
//  	} else if (testFlag(pendingReqFlag, 
//  			    DataRequest.oldest | DataRequest.newest)) {
//  	    pendingReqFlag |= DataRequest.aligned;
//  	}

	modeChanged = true;
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	testFlag                                	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Test flag for inclusion of second flag.              ***
  ***								***
  *****************************************************************
*/
    private static boolean testFlag(int flag, int subFlag) {
	if ((flag & subFlag) == 0) {
	    return false;
	}
	return true;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setMode                         		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Resets the mode.                                     ***
  ***	          ASYNCHRONOUS STATE-CHANGE METHOD              ***
  ***								***
  *****************************************************************
*/
    public synchronized void setMode(int mode) {
	pendingMode = mode;

	/*
	 JPW 10/11/2004: There is no more FRAMES_MODE; always in TIME_MODE
	switch (mode) {
	case ControlBox.TIME_MODE:
	    removeFromReqFlag(DataRequest.startFrame | 
			      DataRequest.durationInFrames);
	    break;
	case ControlBox.FRAMES_MODE:
	    addToReqFlag(DataRequest.startFrame | 
			 DataRequest.durationInFrames);
	    break;
	default:
	    // NO-OP
	}
	*/
	
	// must grab new duration, increment, and rate:
	setDuration(cb.getDuration());
        setRate(cb.getRate());
	setIncrement(cb.getIncrement());
	modeChanged = true; // causes startTime to be changed safely
	                    // from a privileged method.
    }

    /**********************************************************/
    /*********************** utilities ************************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	step     			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2005 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Primary workhorse of PlayerEngine: actually gets and ***
  ***   outputs data.                                           ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History:					***
  ***	01/10/2005	JPW	Add bForceAbsoluteI arg; used	***
  ***				in call to fetch().  Also add	***
  ***				bOutputDataI arg which indicates***
  ***				if the user wants data output	***
  ***				from this method		***
  ***								***
  *****************************************************************
*/
    private int step(
        boolean bForwardI, boolean bForceAbsoluteI, boolean bOutputDataI)
    throws Exception
    {
	
	fetch(bForwardI, bForceAbsoluteI);
	
	int numChansLeft = removeNullFrames(outputMap);
	
	// System.err.println("step(): num chans left = " + numChansLeft);
	
	if (numChansLeft == 0) {
	    return GOTNULLFRAME;
	}
	
	// JPW 01/10/2005: Add bOutputDataI argument
	if (bOutputDataI) {
	    source.outputData(outputMap);
	}
	
	return NORMAL;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	fetch     			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000, 2004, 2005 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the data.                      ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	01/10/2005	JPW	Add bForceAbsoluteI arg; this	***
  ***				is used for duration = 0 mode	***
  ***				when the user is moving around	***
  ***				the position slider - do an	***
  ***				absolute request in this case.	***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***	10/14/2004	JPW	Add case for duration = 0	***
  ***								***
  *****************************************************************
*/
    private void fetch(boolean bForwardI, boolean bForceAbsoluteI)
        throws Exception
    {
	
	double start = startTime; 
	if (!bForwardI) {
	    start = startTime - duration;
	}
	
	copyRequestMap();
	try {
	    // JPW 10/14/2004: Add case for duration = 0
	    String directionString = "absolute";
	    // JPW 01/10/2005: Use the new bForceAbsoluteI argument
	    if (bForceAbsoluteI) {
		directionString = "absolute";
	    }
	    else if ( (duration == 0) && (bForwardI) ) {
		directionString = "next";
	    } else if ( (duration == 0) && (!bForwardI) ) {
		directionString = "previous";
	    }
	    // JPW 01/11/2005: NOTE:
	    // start and duration are in seconds; we are assuming here that the
	    // time base of the data in the RBNB is also seconds;
	    // if time in RBNB is in milliseconds, then must multiply start
	    // and duration by 1000 to convert to the appropriate number of
	    // milliseconds
	    sink.Request(outputMap,
                         start,
                         duration,
                         directionString);
	    outputMap = sink.Fetch(-1, outputMap);
	} catch (Exception e) {
	    System.err.println("Exception encountered fetching data.");
	    throw e;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	fetchNewestTime  		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the latest time.               ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    private void fetchNewestTime()
	throws Exception {

	// JPW 10/11/2004: Copy requestMap to outputMap
	copyRequestMap();
	
	sink.RequestRegistration(outputMap);
	outputMap = sink.Fetch(-1, outputMap);
	
	/*
	try {
	    outputMap = sink.getMap(outputMap,
				    ZERO_TIME,
				    ZERO_TIME,
				    DataRequest.startFrame | 
				    DataRequest.durationInFrames | 
				    DataRequest.newest | 
				    DataRequest.noData);
	} catch (Exception e) {
	    System.err.println("Exception encountered fetching data.");
	    throw e;
	}
	*/
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	removeNullFrames     			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Removes all channels containing no data from the     ***
  ***   input map.                                              ***
  ***								***
  ***	NOTE: There is no way under RBNB V2 to remove a single	***
  ***	      channel from a ChannelMap.			***
  ***								***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***				This method will return the	***
  ***				number of chans with data;	***
  ***				under RBNB V2 SAPI, there is	***
  ***				no way to remove a single chan	***
  ***				from a channel map		***
  ***								***
  *****************************************************************
*/
    private int removeNullFrames(ChannelMap inMap) {
	
	if (inMap == null) {
	    return 0;
	}
	
	// Mark which channels need to be deleted
	boolean[] bDeleteChan = new boolean[inMap.NumberOfChannels()];
	int numChansLeft = inMap.NumberOfChannels();
	
	for (int i = 0; i < inMap.NumberOfChannels(); ++i) {
	    byte[] chanData = inMap.GetData(i);
	    if ( (chanData == null) || (chanData.length == 0) ) {
		bDeleteChan[i] = true;
		numChansLeft--;
	    } else {
		bDeleteChan[i] = false;
	    }
	}
	
	// Now, go back through delete any marked channels
	for (int i = 0; i < inMap.NumberOfChannels(); ++i) {
	    if (bDeleteChan[i]) {
		// Under V2 SAPI, there is no way to remove a single channel
		// from a ChannelMap
	    }
	}
	
	return numChansLeft;
    }

    /**********************************************************/
    /******** position limits-checking and -resetting *********/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	evaluateLimits    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Evaluates limits of data and resets position slider. ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  *****************************************************************
*/
    private int evaluateLimits() throws Exception {
	// are we out of range?
	if (startTime <= masterMin) {
	    // reset to beginning of data
	    cb.setPosition(ControlBox.MINPOS);
	    return LOWERLIMIT;
	}
	
	if (startTime >= masterMax) {
	    // reset to end of data
	    cb.setPosition(ControlBox.MAXPOS);
	    return UPPERLIMIT;
	}
	
	// we are within range
	double percent =
	    (startTime - masterMin) / (masterMax - masterMin);
	int newPos =
	    (int)((percent * (ControlBox.MAXPOS - ControlBox.MINPOS)) + ControlBox.MINPOS);
	
	cb.setPosition(newPos);
	return NORMAL;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	resetMinMaxTimes    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Checks limits and resets minTime and maxTime.        ***
  ***   Uses requestMap.                                        ***
  ***		     PRIVILEGED METHOD                          ***
  ***								***
  ***	Modification History					***
  ***	12/14/2004	JPW	Initialize tempMaxTime to	***
  ***				    -Double.MAX_VALUE		***
  ***	10/08/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    private void resetMinMaxTimes() throws Exception {
	
	double tempMinTime = Double.MAX_VALUE;
	// JPW 12/14/2004: Initialize tempMaxTime to -Double.MAX_VALUE
	//                 (it was previously Double.MIN_VALUE)
	double tempMaxTime = -Double.MAX_VALUE;
	double tempTime;
	
	String[] chanNames = requestMap.GetChannelList();
	
	sink.RequestRegistration(requestMap);
	ChannelMap cm = new ChannelMap();
	cm = sink.Fetch(-1, cm);
	
	for (int i = 0; i < cm.NumberOfChannels(); ++i) {
	    double tempstart = cm.GetTimeStart(i);
	    double tempend = tempstart + cm.GetTimeDuration(i);
	    if (tempstart < tempMinTime) {
		tempMinTime = tempstart;
	    }
	    if (tempend > tempMaxTime) {
		tempMaxTime = tempend;
	    }
	}
	
	timeMin = tempMinTime;
	timeMax = tempMaxTime;
	masterMin = timeMin;
	masterMax = timeMax;
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	lastStartTime()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the latest startTime of the    ***
  ***   latest request result.                                  ***
  ***								***
  ***	Modification History					***
  ***	12/14/2004	JPW	Initialize startT to		***
  ***				    -Double.MAX_VALUE		***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    private double lastStartTime() {
	
	// JPW 12/14/2004: With startT initialized to Double.MIN_VALUE, this
	//                 method would sometimes return an error.  For
	//                 example, if outputMap contains a single channel
	//                 with the start time = 0.0; in this case, this
	//                 method would return Double.MIN_VALUE, and not
	//                 0.0 as it should.  Instead, startT should be
	//                 initialized to -Double.MAX_VALUE
	// double startT = Double.MIN_VALUE;
	double startT = -Double.MAX_VALUE;
	double tempT = 0.0;
	for (int i = 0; i < outputMap.NumberOfChannels(); ++i) {
	    tempT = outputMap.GetTimeStart(i);
	    if (tempT > startT) {
		startT = tempT;
	    }
	}
	
	return startT;
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	lastEndTime()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the latest end time of the     ***
  ***   last request result.                                    ***
  ***	   Note the returned value may be null if the previous  ***
  ***   call to step() returned no data, or if the previous call***
  ***   to fetchNewestTime() found no times at all.             ***
  ***								***
  ***	Modification History					***
  ***	12/14/2004	JPW	Initialize endT to		***
  ***				    -Double.MAX_VALUE		***
  ***	10/11/2004	JPW	Upgrade to RBNB V2.  Instead of	***
  ***				returning null, this method may	***
  ***				now return Double.MIN_VALUE	***
  ***								***
  *****************************************************************
*/
    private double lastEndTime() {
	
	// JPW 12/14/2004: Initialize endT to -Double.MAX_VALUE (it had been
	//                 Double.MIN_VALUE)
	double endT = -Double.MAX_VALUE;
	double tempT;
	
	for (int i = 0; i < outputMap.NumberOfChannels(); ++i) {
	    tempT = outputMap.GetTimeStart(i) + outputMap.GetTimeDuration(i);
	    if (tempT > endT) {
		endT = tempT;
	    }
	}
	
	// 10/11/2004 JPW: Now, endT may be -Double.MAX_VALUE
	// 04/26/02 UCB - NB: endT mat be null, if fetch() returned
	// no data, or fetchNewestTime() found no times at all.
	return endT;
    }
    
    /**********************************************************/
    /****************** position display **********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	displayPosition()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Displays the current position according to the       ***
  ***   correct format.                                         ***
  ***								***
  *****************************************************************
*/
    private void displayPosition() {
	displayPosition(startTime);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	displayPosition()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Displays the input position (as Time) according to   ***
  ***   the correct format.                                     ***
  ***								***
  ***	Modification History					***
  ***	05/16/2005	JPW	Round off the time values to	***
  ***				display.			***
  ***	10/28/2004	JPW	Display time using the format	***
  ***				specified by the user.		***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    private void displayPosition(double toDisp) {
	
	// This method is typically called only from privileged methods;
	// it will only be called by a non-privileged method when
	// no privileged method is running.  Hence it is effectively a 
	// privileged method.
	
	String posStr = null;
	
	if (cb.getTimeFormat() == ControlBox.TIME_FORMAT_UNSPECIFIED) {
	    try {
	        posStr = ToString.toString("%.3f",toDisp);
	    } catch (Exception e) {
	        posStr = String.valueOf(toDisp);
	    }
	    
	    // strip trailing spaces
	    while (posStr.endsWith(" ")) {
	        posStr = posStr.substring(0, posStr.length() - 1);
	    }
	    
	    // add in decimal point and additional zeroes as needed
	    int decIndex = posStr.indexOf('.');
	    if (decIndex == -1) {
	        posStr += ".000";
	    } else if (decIndex == posStr.length() - 2) {
	        posStr += "000";
	    }
	} else if (cb.getTimeFormat() == ControlBox.TIME_FORMAT_MILLISECONDS) {
	    SimpleDateFormat dateFormat =
	        new SimpleDateFormat("dd-MMM-yyyy z HH:mm:ss.SSS");
	    // toDisp is in milliseconds since 01/01/1970
	    // JPW 05/16/2005: Round off the time value
	    Date displayDate = new Date( Math.round( (double)toDisp ) );
	    posStr = dateFormat.format(displayDate);
	} else if (cb.getTimeFormat() == ControlBox.TIME_FORMAT_SECONDS) {
	    SimpleDateFormat dateFormat =
	        new SimpleDateFormat("dd-MMM-yyyy z HH:mm:ss.SSS");
	    // toDisp is in seconds since 01/01/1970; first need to convert
	    // to number of milliseconds since epoch
	    // JPW 05/16/2005: Round off the time value
	    Date displayDate =
	        new Date( Math.round( (double)(toDisp * 1000.0) ) );
	    posStr = dateFormat.format(displayDate);
	}
	
	cb.setPositionDisplay(posStr);
	
    }
    
    /**********************************************************/
    /************* rate tracking and display ******************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	displayRate()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Displays the desired rate, with correct formatting   ***
  ***   and units.                                              ***
  ***								***
  *****************************************************************
*/
    private void displayRate() {
	displayRate(rate);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	displayRate()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Displays the actual rate, with correct formatting    ***
  ***   and units.                                              ***
  ***								***
  *****************************************************************
*/
    private void displayRate(double rateI) {
	// 02/04/02 UCB - Note that this is a bit of a cheat;
	// Widgets.ControlBox has code specifying the range of
	// the Rate slider for each mode.  For both Frames
	// and Time mode, the slider values range from 0.001 to 1000.
	// Therefore, the rate string will be displayed with
	// three digits after the decimal.
	// Technically, PlayerEngine shouldn't know the internals
	// of Widgets.ControlBox.  TODO: fix this at some point.
	// For now: go home and sleep.  It's been a long day.

	String rateStr;
	try {
	    rateStr = ToString.toString("%.3f", rateI);
	} catch (Exception e) {
	    rateStr = String.valueOf(rateI);
	}
	
	// JPW 10/11/2004: Only time mode now
	rateStr += "x";
	
	cb.setRateFeedback(rateStr);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :  clearRateDisplay()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	    Clears the Rate feedback field.                     ***
  ***								***
  *****************************************************************
*/
    private void clearRateDisplay() {
	cb.setRateFeedback("");
    }

    /**********************************************************/
    /********************* rate control ***********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	sleepAsNeeded()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method inserts a sleep in the current Thread,   ***
  ***   with the input length.                                  ***
  ***                                                           ***
  ***   02/19/02 UCB -    Note that Object.wait(long) does not  ***
  ***                  always wake up exactly after sleepMillis ***
  ***                  millis, when not interrupted.            ***
  ***                     Therefore, sleepAsNeeded now          ***
  ***                  returns a double indicating the amount   ***
  ***                  by which it has overslept.  (Of course,  ***
  ***                  the calculation of that value relies on  ***
  ***                  the accuracy of System.currntTimeMillis, ***
  ***                  but this is the best we can do for now.) ***
  ***                     The source of the inaccuracy might be ***
  ***                   (a) the resolution of the system clock, ***
  ***                   (b) the scheduler algorithm or overhead ***
  ***                       for context-switching, or           ***
  ***                   (c) other.  The docs for Sun's JDK don't***
  ***                       guarantee that wait returns exactly ***
  ***                       after the time-out period.          ***
  ***								***
  *****************************************************************
*/
    private double sleepAsNeeded(long sleepMillis) {
	
	if (sleepMillis <= 0) {
	    // 02/19/02 UCB - which return value makes the most
	    // sense?  Using 0.0 for now.
	    // return (-1.0 * sleepMillis);
	    return 0.0;
	}
	
	long startMillis = System.currentTimeMillis();
	
	synchronized (sleepWaker) {
	    try {
		sleepWaker.wait(sleepMillis);
	    } catch (InterruptedException e) {}
	}
	
	return (System.currentTimeMillis() - startMillis - sleepMillis);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	wakeUp()    	         		        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method causes any sleep within the play() method***
  ***   to stop.                                                ***
  ***								***
  *****************************************************************
*/
    private void wakeUp() {
	synchronized (sleepWaker) {
	    sleepWaker.notify();
	}
    }

    /**********************************************************/
    /******************** Utility methods *********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	copyRequestMap()    	         		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	March, 2002					***
  ***								***
  ***	Copyright 2002, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method copies the request Map to the outputMap. ***
  ***	This is a "shallow" copy - just create a new map and	***
  ***	add the appropriate channel names to the new map.	***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***				Got rid of copying user data	***
  ***								***
  *****************************************************************
*/
    private void copyRequestMap() throws Exception {
	
	// JPW 10/11/2004: Here is the old call:
	//     outputMap = new Map(requestMap);
	//     This Map constructor just creates a new Map and adds
	//     new channels which have the same names as the channels
	//     in requestMap
	
	outputMap = new ChannelMap();

	if(requestMap != null) {  // mjm 2/9/05
	    String[] chanList = requestMap.GetChannelList();
	
	    if (chanList != null) {
		for (int i = 0; i < chanList.length; ++i) {
		    outputMap.Add(chanList[i]);
		}
	    }
	}
    }
    
    /**********************************************************/
    /********************* Timer Class ************************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	Timer             (private class)    		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This private class is used to time the running of    ***
  ***   portions of PlayerEngine; it is used for playback       ***
  ***   pacing.                                                 ***
  ***								***
  *****************************************************************
*/
    private class Timer {
	// This class should be accessed from a single
	// Thread only!

	long elapsedMillis = 0;
	Date latestStart = new Date();
	boolean timerRunning = true;

	public Timer() {}

	public void startTimer() {
	    // starts timing from present
	    latestStart = new Date();
	    timerRunning = true;
	}

	public void clearTimer() {
	    // clears the Timer's knowledge of
	    // any elapsed time; stops the timer
	    latestStart = null;
	    elapsedMillis = 0;
	    timerRunning = false;
	}

	public long getElapsed() {
	    // discovers how much time has passed
	    // since Timer was started (following
	    // latest reset, if any)
	    if (!timerRunning) {
		return elapsedMillis;
	    }

	    Date currTime = new Date();
	    return elapsedMillis + ((new Date()).getTime() - 
		latestStart.getTime());
	}

	public long pauseTimer() {
	    // saves and returns how much time just ran, 
	    // and stops timer
	    if (!timerRunning) {
		return elapsedMillis;
	    }
	    
	    elapsedMillis = getElapsed();
	    timerRunning = false;
	    latestStart = null;
	    return elapsedMillis;
	}

    }

}
