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


package com.rbnb.timedrive;

    /**************************************************************************
     * Munge parameter settings which will be added to RBNB requests.
     * <p>
     * TimeDrive server adds a munge to RBNB requests and then sends that
     * request off to the RBNB.  This class stores/manages all the munge
     * parameters.
     *
     * @author John P. Wilson
     *
     * @version 05/31/2006
     */
     
    /*
     * Copyright 2005, 2006 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/31/2006  JPW  Add LIVE play mode constant.
     *			We used to use "t=0.0" as a special case to indicate
     *			live mode; now live mode is indicated directly by
     *			the value of playMode
     * 11/20/2005  JPW	Created
     *
     */
    
    public class MungeParameters {
	
	// Constants for play modes
	// JPW 05/31/2006: Add LIVE play mode constant.
	public static final int PAUSE = 0;
	public static final int FORWARD = 1;
	public static final int BACKWARD = 2;
	public static final int LIVE = 3;
	
	// The elements of this array must be in the exact same order as the
	// PAUSE, FORWARD, BACKWARD, LIVE constants defined above
	public static final String[] playStr =
	    {"pause", "forward", "backward", "live"};
	
	private String reference = "absolute";
	// This time represents the time at the *end* of the RBNB request
	// interval.  In other words, we want to request an interval from the
	// RBNB that covers (time - duration) up to (time).
	private double time = 0.0;
	private double duration = 0.0;
	private int playMode = MungeParameters.LIVE;
	// JPW 09/06/2006: Change default rate from 0.0 to 1.0;
	//                 0.0 was not actually a valid value for rate
	private double rate = 1.0;
	
	// For playing through data: this is the actual/real wallclock time
	// when the last play updated was performed
	private long lastPlayUpdate = 0;
	
	// JPW 01/27/2006: Add debug level for printouts
	private int debug = 0;
	
	/**********************************************************************
	 * Constructor.
	 * <p>
	 * Initialize time to current wall-clock time.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public MungeParameters() {
	    // Initialize the time field to current system time
	    time = ((double)System.currentTimeMillis()) / 1000.0;
	}
	
	/**********************************************************************
	 * Constructor.
	 * <p>
	 * Initialize munge parameters using the method arguments.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param referenceI	Reference
	 * @param timeI		Time
	 * @param durationI	Duration
	 * @param playModeI	Play mode
	 * @param rateI		Play rate
	 * @param debugI	Debug level for printouts
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/27/2006  JPW	Add debugI argument
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public MungeParameters(
	    String referenceI,
	    double timeI,
	    double durationI,
	    int playModeI,
	    double rateI,
	    int debugI)
	{
	    debug = debugI;
	    setTimeMunge(referenceI, timeI, durationI, playModeI, rateI);
	}
	
	/**********************************************************************
	 * Accessor method to get reference.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public String getReference() {
	    return reference;
	}
	
	/**********************************************************************
	 * Accessor method to get time.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public double getTime() {
	    return time;
	}
	
	/**********************************************************************
	 * Set time.
	 * <p>
	 * Check that time is not going into the future.
	 *
	 * @author John P. Wilson
	 *
	 * @param timeI		The new time
	 *
	 * @version 01/27/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public void setTime(double timeI) {
	    // Make sure time doesn't go beyond current wall clock time
	    long now = System.currentTimeMillis();
	    double nowSec = now / 1000.0;
	    if (timeI == Double.MAX_VALUE) {
		// This indicates that we will continue to use whatever is
		// currently stored in the "time" variable; double check this
		// variable to make sure it isn't in the future
		if (time > nowSec) {
		    time = nowSec;
		} else if (time < 0.0) {
		    time = 0.0;
		}
	    } else {
		if (timeI > nowSec) {
		    time = nowSec;
		} else if (timeI < 0.0) {
		    time = 0.0;
		} else {
		    time = timeI;
		}
	    }
	}
	
	/**********************************************************************
	 * Accessor method to get duration.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public double getDuration() {
	    return duration;
	}
	
	/**********************************************************************
	 * Accessor method to get play mode.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public int getPlayMode() {
	    return playMode;
	}
	
	/**********************************************************************
	 * Accessor method to get rate.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public double getRate() {
	    return rate;
	}
	
	/**********************************************************************
	 * Get the time of the last play update.
	 * <p>
	 * This is only used when playmode = FORWARD or BACKWARD
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public long getLastPlayUpdate() {
	    return lastPlayUpdate;
	}
	
	/**********************************************************************
	 * Set the time of the last play update.
	 * <p>
	 * This is only used when playmode = FORWARD or BACKWARD
	 *
	 * @author John P. Wilson
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public void setLastPlayUpdate(long msSinceEpochI) {
	    if (msSinceEpochI >= 0) {
		lastPlayUpdate = msSinceEpochI;
	    }
	}
	
	/**********************************************************************
	 * Get the time munge string to be appended to RBNB requests.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param tempDurationI	    The value of duration to use for this
	 *			    request as long as it falls in the bounds:
	 *			      0.0 <= tempDurationI < Double.MAX_VALUE
	 *
	 * @version 11/13/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/13/2006  JPW	Change bSetDurationToZeroI to tempDurationI;
	 *			tempDurationI will be the duration to use for
	 *			this request only (don't set it as a permanent
	 *			value in the MungeParameters).
	 * 05/31/2006  JPW      We used to use "t=0.0" as a special case to
	 *			indicate live mode; now live mode is indicated
	 *			directly by the value of playMode
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public String getTimeMunge(double tempDurationI) {
	    
	    // Update time reference if playing through data
	    updatePlayTime();
	    
	    // The variable "time" represents the time at the *end* of the RBNB
	    // request interval.  There are two cases to consider:
	    // 1. referece = newest:
	    //    Since "newest" mode is a backward looking reference, it will
	    //    work out fine to submit a request to the RBNB with
	    //    start time = "time" and duration = "duration"
	    // 2. reference = oldest or absolute:
	    //    The interval we want to request from the RBNB is from
	    //    (time - duration) up to (time).  So what we need to give the
	    //    RBNB for a start time = (time - duration)
	    
	    double startTime = getTime();
	    
	    // JPW 05/31/2006: We used to use "t=0.0" as a special case to
	    //                 indicate live mode; now live mode is indicated
	    //                 directly by the value of playMode
	    // if (startTime == 0.0) {
	    if (playMode == MungeParameters.LIVE) {
		// Set startTime equal to current system time
		startTime = ((double)System.currentTimeMillis()) / 1000.0;
	    }
	    
	    // JPW 11/13/2006: If tempDurationI falls within bounds, use it
	    //                 for the duration value.
	    double requestDuration = getDuration();
	    if ((tempDurationI >= 0.0) && (tempDurationI < Double.MAX_VALUE)) {
		requestDuration = tempDurationI;
	    }
	    
	    if ( (getReference().equals("oldest")) ||
	         (getReference().equals("absolute")) )
	    {
		if (startTime > requestDuration) {
		    startTime = startTime - requestDuration;
		} else {
		    // duration is greater than time;
		    // can't use a negative start time!
		    requestDuration = startTime;
		    startTime = 0.0;
		}
	    }
	    
	    // JPW 04/11/2006: If this is an absolute request, don't need
	    //                 to include the reference, it is implied.
	    if (!getReference().equals("absolute")) {
		return
	            new String(
		    	"r=" + getReference() +
			"&t=" + startTime +
			"&d=" + requestDuration);
	    }
	    
	    return
	        new String(
		    "t=" + startTime +
		    "&d=" + requestDuration);
	    
	}
	
	/**********************************************************************
	 * Set time munge parameters.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param referenceI	Reference
	 * @param timeI		Time
	 * @param durationI	Duration
	 * @param playModeI	Play mode
	 * @param rateI		Play rate
	 *
	 * @version 01/06/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public void setTimeMunge(
	    String referenceI,
	    double timeI,
	    double durationI,
	    int playModeI,
	    double rateI)
	{
	    
	    if ( (referenceI != null) &&
		 ( (referenceI.equals("oldest")) ||
		   (referenceI.equals("newest")) ||
		   (referenceI.equals("absolute")) ) )
	    {
		reference = referenceI;
	    }
	    
	    setTime(timeI);
	    
	    if ( (durationI < Double.MAX_VALUE) &&
		 (durationI >= 0.0) )
	    {
		duration = durationI;
	    }
	    
	    // JPW 05/31/2006: Add LIVE mode
	    if ( (playModeI == MungeParameters.PAUSE) ||
	         (playModeI == MungeParameters.FORWARD) ||
		 (playModeI == MungeParameters.BACKWARD) ||
		 (playModeI == MungeParameters.LIVE) )
	    {
		if (playModeI != playMode) {
		    // This is a new play mode; set the timer ticking in order
		    // to get accurate updates for playing through data
		    lastPlayUpdate = System.currentTimeMillis();
		}
		playMode = playModeI;
	    }
	    
	    if ( (rateI < Double.MAX_VALUE) &&
		 (rateI >= 0.0) )
	    {
		rate = rateI;
	    }
	    
	    if (debug > 2) {
		System.err.println(
		    "Updated time munge: " +
		    "ref = " + reference +
		    ", time = " + time +
		    ", dur = " + duration +
		    ", play mode = " + playStr[playMode] +
		    ", rate = " + rate);
	    }
	    
	}
	
	/**********************************************************************
	 * Update time reference if playing through data.
	 * <p>
	 * Don't do anything if user is in PAUSE or LIVE mode.
	 *
	 * @author John P. Wilson
	 *
	 * @return The boolean return value indicates whether there was a
	 *         change from play mode to step mode; this method makes that
	 *	   change when time goes beyond current time (that is, we don't
	 *	   play into the future).
	 *
	 * @version 09/28/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/28/2006  JPW	If mode == LIVE, update the time to current
	 *			system time.
	 * 01/26/2006  JPW	Add boolean return value indicating whether
	 *			play mode has been changed from play to step.
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public boolean updatePlayTime() {
	    
	    if (getPlayMode() == MungeParameters.PAUSE) {
		// No need to update time - we aren't playing through data
		return false;
	    } else if (getPlayMode() == MungeParameters.LIVE) {
		// JPW 09/28/2006: Update to current system time
		setTime( ((double)System.currentTimeMillis()) / 1000.0 );
		return false;
	    }
	    
	    // JPW 01/26/2006: Flag to indicate if we change from
	    //                 play mode to step mode.
	    boolean bChangeFromPlayToStep = false;
	    
	    // deltaT (in msec), since the last data update
	    long now = System.currentTimeMillis();
	    double nowSec = now / 1000.0;
	    double deltaT = (double)(now - getLastPlayUpdate());
	    
	    // Convert deltaT into a time difference in the data domain,
	    // considering the user-specified rate
	    double dataDeltaT = (deltaT / 1000.0) * getRate();
	    
	    if (getPlayMode() == MungeParameters.BACKWARD) {
		dataDeltaT = -1.0 * dataDeltaT;
	    }
	    
	    // update the time
	    double newTime = 0.0;
	    if ( (getReference().equals("absolute")) ||
		 (getReference().equals("oldest")) )
	    {
		newTime = getTime() + dataDeltaT;
	    } else {
		newTime = getTime() - dataDeltaT;
	    }
	    // Keep time positive
	    // JPW 01/26/2006: If newTime has gone beyond current time ("now"),
	    //                 then set time to now and pause play
	    if ( (newTime >= 0.0) && (newTime <= nowSec) ) {
		setTime(newTime);
	    } else if (newTime > nowSec) {
		// JPW 06/02/2006: Maintain the same rate value instead
		//                 of setting rate to 0.0
		setTimeMunge(
		    reference,
		    nowSec,
		    duration,
		    MungeParameters.PAUSE,
		    rate);
		bChangeFromPlayToStep = true;
	    } else {
		// setTime(0.0);
		// JPW 06/01/2006: don't go into negative time; pause play
		setTimeMunge(
		    reference,
		    0.0,   // set time=0.0
		    duration,
		    MungeParameters.PAUSE,
		    rate);
		bChangeFromPlayToStep = true;
	    }
	    
	    // Reset the timer
	    setLastPlayUpdate(now);
	    
	    return bChangeFromPlayToStep;
	    
	}
	
	/**********************************************************************
	 * Return a string containing the current munge values plus the
	 * name of the sync channel.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @return Time munge string.
	 *
	 * @version 04/13/2006
	 */
	 
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/13/2006  JPW	Make the returned String more compact;
	 *			  this includes leaving out certain elements
	 *			  (such as "reference") when the default value
	 *			  can be implied.  For the case of reference,
	 *			  if no "reference" key is given in the
	 *			  munge string then "absolute" is implied.
	 *			Cast time, duration, and rate as integers,
	 *			  as this is only what the TimeDrive web
	 *			  interface currently supports.
	 * 11/20/2005  JPW	Created
	 *
	 */
	
	public String toString(String syncChanI) {
	    // JPW 04/13/2006: Cast time to be an integer
	    // String timeStr = Double.toString(time);
	    String timeStr = Integer.toString((int)time);
	    
	    // JPW 05/31/2006: Live mode is now indicated in the "play=" string
	    // String liveModeStr = "";
	    // JPW 05/31/2006: Live mode is now indicated by playMode == LIVE
	    // if (time == 0.0) {
	    if (playMode == MungeParameters.LIVE) {
		// Set the time to current system time
		// JPW 04/13/2006: Cast time to be an integer
		timeStr =
		    Integer.toString(
		        (int)(((double)System.currentTimeMillis()) / 1000.0));
	    }
	    String syncChanStr = "";
	    if ( (syncChanI != null) && (!syncChanI.equals("")) ) {
		syncChanStr = "&syncchan=" + syncChanI;
	    }
	    // JPW 04/13/2006: Only include a reference string if
	    //                 reference is not "absolute"
	    String referenceStr = "";
	    if ( (reference != null) &&
	         (!reference.equals("")) &&
	         (!reference.equals("absolute")) )
	    {
		referenceStr = "r=" + reference + "&";
	    }
	    return
		new String(
		    referenceStr +
		    "t=" + timeStr + "&" +
		    // JPW 04/13/2006: Cast duration to be an integer
		    "d=" + Integer.toString((int)duration) + "&" +
		    "play=" +  playStr[playMode] + "&" +
		    // JPW 04/13/2006: Cast rate to be an integer
		    "rate=" + Integer.toString((int)rate) +
		    syncChanStr);
	}
	
    }
    
