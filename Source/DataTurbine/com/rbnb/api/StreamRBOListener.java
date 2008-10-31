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

package com.rbnb.api;

// JPW 04/14/2005
import java.util.Vector;

/**
 * Extended <code>StreamDHListener</code> that Listens for
 * <code>SourceHandler</code> "events" for a
 * <code>StreamRequestHandler</code>.
 * <p>
 * <code>SourceHandler</code> "events" are the addition or removal of frame
 * <code>Rmap</code> objects. They trigger the thread associated with this
 * listener to attempt to match the <code>Rmap</code> request stored for this
 * listener. The result of that match (if any) is then passed to the
 * <code>StreamRequestHandler</code> that is an ancestor of this
 * <code>StreamRBOListener</code>.
 * <p>
 * There are three basic subtypes of requests that are handled by this class:
 * <p><ol>
 *    <li>Request/response requests that request a specific time range
 *	  (possibly relative to the oldest or newest data),</li>
 *    <li>Subscription streaming requests that start from either the oldest
 *	  existing or the next data to show up and then retrieve every
 *	  subsequent frame, and</li>
 *    <li>Monitor streaming requests that start with the next data to show up
 *	  and then repeatedly get the newest frame of data currently available
 *	  (which may not be the very next frame).</li>
 * </ol><p>
 * The code proceeds as follows:
 * <p><ol>
 *    <li>Ensure that the request type is legal and create the working request
 *	  from the original request
 *	  (<code>StreamRBOListener.createWorking</code>),</li>
 *    <li>Start processing the request by determining whether the NBO client
 *	  is permitted to access the client
 *        (<code>StreamRBOListener.run</code>),</li>
 *    <li>Determine if there could ever possibly be data matching the request
 *	  and for any non-monitor mode request that is relative to the
 *	  oldest/newest data, convert the request to an absolute time request
 *	  (<code>StreamRBOListener.checkStart</code>,
 *	  <code>StreamRBOListener.findStart</code>,
 *	  <code>StreamRBOListener.findAllRequest</code>, and
 *	  <code>StreamRBOListener.findStartRequest</code>),</li>
 *    <li>Loop until the request has been fully satisfied or is terminated
 *	  by the client,</li>
 *    <li>If necessary, wait for data (and/or an acknowledgment of receipt from
 *	  the sink application) to show up
 *	  (<code>StreamRBOListener.waitToPickup</code>),</li>
 *    <li>If we haven't already found the start point (as for subscriptions
 *	  for which no data had arrived), check the request against the limits
 *	  of the data that is now available, and, if we find the start point,
 *	  switch to an absolute request,</li>
 *    <li>Extract the information matching the request from the RBO
 *	  (<code>StreamRBOListener.processWorking</code> using
 *	  <code>Rmap.extractRmap</code>),</li>
 *    <li>If the request was not for an absolute time, then the returned Rmap
 *	  hierarchy will be a new, absolute time request.  Match it against the
 *	  RBO,</li>
 *    <li>If we got nothing, determine whether or not the request should be
 *	  continued.  For cases where we run off the beginning of the data or
 *	  we run off the end and started at the oldest data, then the request
 *	  is simply terminated,</li>
 *    <li>Send any match (or the lack of a match) to the sink application
 *	  (<code>StreamRBOListener.post</code>,
 *	  <code>StreamListener.post</code>, ),
 *	  <code>StreamRemoteListener.post</code>, ),
 *	  <code>StreamServerListener.post</code>, and ),
 *	  <code>StreamRequestHandler.post</code>).  For monitor-type requests,
 *	  this will result in a request for an acknowledgment of receipt from
 *	  the sink application,</li>
 *    <li>For subscription type requests, increment the frame index so that
 *	  next time we get the next frame.  Note that this will not properly
 *	  work with multiple ring buffers in a single RBO.</li>
 * </ol><p>
 * The handling of the extraction of a new request or data is described in
 * <code>Rmap.extractRmap</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see #checkStart()
 * @see #createWorking()
 * @see #findAllRequest()
 * @see #findStart()
 * @see #findStartRequest()
 * @see #post(com.rbnb.api.Serializable serializableI)
 * @see #processWorking(long countI)
 * @see #run()
 * @see #waitToPickup()
 * @see com.rbnb.api.Rmap#extractRmap(com.rbnb.api.Rmap requestI,boolean dataFlagI)
 * @see com.rbnb.api.StreamServerListener
 * @see com.rbnb.api.StreamRequestHandler
 * @since V2.0
 * @version 08/27/2008
 */

/*
 * Copyright 2001, 2002, 2003, 2004, 2005, 2007 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/31/2008  MJM	in updateWorking(), limit increment to not get ahead of actual data.  
 * 08/27/2008  JPW	Make change in processWorking():
 * 			Support monitor mode data request with more than 1 child
 * 03/13/2007  JPW	Make a change in findStartRequest():
 *			For subscription to oldest, if there is no Frange in
 *			the extracted DataRequest, set foundR to false and wait
 *			for frames to show up.  This fixes a bug when a
 *			subscription by frame to oldest starts up before any
 *			data is in the Source, and where there are registered
 *			channels in the Source.  In this case, a DataRequest
 *			was extracted, but it didn't have a start frame.
 * 2005/07/15  WHF	Added OutOfMemoryError handling to processWorking().
 * 04/25/2005  JPW	Change in processWorking(): If we got a match and if
 *			the user is in Monitor mode, check that we don't send
 *			out a repeated frame.  Use a new class variable to
 *			perform this check: monitorFrameIdx.
 * 04/25/2005  JPW	Change in accept(): Comment out the code added on
 *			4/21/2005; I didn't support wildcards in the String
 *			matches.  I could have added this, but instead we
 *			decided to move the check to catch sending out the
 *			same frame multiple times to processWorking().
 * 04/21/2005  JPW	Change in accept(): if FRAME-mode request, see if the
 *			new event actually matches a channel in the request.
 *			If it does, and only if it does, then call
 *			setToPickup().
 * 04/15/2005  JPW	Change in updateWorking(): Increment the time of the
 *			first child in the request Rmap only.  This avoids the
 *			problem of incrementing multiple times if there is more
 *			than one RingBuffer in the RBO.
 * 07/30/2004  INB	Added documentation describing how this class operates.
 * 04/14/2004  INB	Handle <code>REASON_BOD</code> the same as
 *			<code>REASON_END</code> for streaming requests - i.e.,
 *			it terminates the stream.
 * 04/07/2004  INB	In monitor mode, we want to wait until we've been
 *			synchronized by the client before moving on, even
 *			if there is data waiting.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 01/15/2004  INB	Added posting of <code>EndOfStream</code> if we are
 *			shutting down without having done so.
 * 12/10/2003  INB	Changed the <code>maxWait</code> handling to ignore
 *			(i.e., not wait for) channels that have not shown up at
 *			all.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.  Added identification to
 *			<code>Door</code>.
 * 11/07/2003  INB	Eliminated some unnecessary synchronization.
 * 10/27/2003  INB	Ensure that we update the working request only if it
 *			makes sense to do so.
 * 10/23/2003  INB	Added <code>checkSkip</code> logic.
 * 06/20/2003  INB	Added handling of streaming request for
 *			<code>oldest</code>.
 * 06/16/2003  INB	Added handling of <code>RequestOptions</code>.  In
 *			particular, the <code>maxWait</code> value.
 * 03/27/2001  INB	Created.
 *
 */
final class StreamRBOListener
    extends com.rbnb.api.StreamDHListener
    implements com.rbnb.api.GetLogInterface
{

    /**
     * the number of frames per repetition.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */
    private int framesPerIteration = 1;

    /**
     * new <code>RBO</code> being handled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/23/2001
     */
    private boolean haveNewRBO = false;

    /**
     * do we need to figure out the start time of the request?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private boolean needStart = false;

    /**
     * do we need to wait for things to arrive?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/22/2001
     */
    private boolean needWait = false;

    /**
     * time that request was initiated.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/16/2003
     */
    private long requestStarted = 0;

    /**
     * Used in processWorking() when user is in Monitor mode: If we got a
     * match, check that we don't send out a repeated frame. This class
     * member is used to store the largest frame index which has been matched.
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.5B
     * @version 04/25/2005
     */
    private double monitorFrameIdx = -1.0;

    /**
     * Holds new frames, so searches can be of them instead of full RBO.
     * The RBO now has compressed frames, so frame based searches don't work.
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 06/28/2006
     */
    //private Vector newFrames=new Vector();


    /**
     * Class constructor to build a <code>StreamRBOListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>NotificationFrom</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>NotificationFrom</code> source.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
     *
     */
    StreamRBOListener(StreamParent parentI,
		      Rmap requestI,
		      NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
    }

    /**
     * Class constructor to build a <code>StreamRBOListener</code> for the
     * specified code>StreamParent</code>, <code>DataRequest</code>, and
     * <code>SourceHandler</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI     our parent.
     * @param requestI    the <code>DataRequest</code
     * @param sourceI     the <code>SourceHandler</code> source.
     * @param haveNewRBOI is the <code>RBO</code> a new one?
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    StreamRBOListener(StreamParent parentI,
		      DataRequest requestI,
		      SourceHandler sourceI,
		      boolean haveNewRBOI)
	throws java.lang.InterruptedException
    {
	this(parentI,requestI,(NotificationFrom) sourceI);
	haveNewRBO = haveNewRBOI;
    }

    /**
     * Accepts a notification event <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the "event" <code>Serializable</code>.
     * @param matchI the matched request <code>Rmap</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the post operation is interrupted.
     * @since V2.0
     * @version 04/25/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2005  JPW	Comment out the code added on 4/21/2005; I didn't
     *			    support wildcards in the String matches.  I could
     *			    have added this, but instead we decided to move
     *			    the check to catch sending out the same frame
     *			    multiple times to processWorking().
     * 04/21/2005  JPW	If we have a frame-based request, check that a chan
     *                      in the new event actually matches a chan in the
     *                      request.  If there is a match, call setToPickup
     *                      which will "tickle" the run method to call
     *                      processWorking() to process the new event.
     *                  This change fixes a bug that would occur in Monitor
     *                      mode when there were 2 RingBuffers in a single RBO
     *                      and the user is only Monitoring one of the chans.
     *                      When a new frame from the other chan (the chan the
     *                      user is not Monitoring) would come in, process-
     *                      Working() would be called.  getSource() would
     *                      contain the new event frame (from the chan the user
     *                      is not Monitoring) as well as the existing/newest
     *                      frame from the chan the user is Monitoring.  This
     *                      existing frame would be matched and posted, thus
     *                      resulting in the user getting repeated frames of
     *                      the chan they are Monitoring - they would get a
     *                      repeat whenever a new frame of the chan they are
     *                      **not** Monitoring would come in!!!
     * 03/26/2001  INB	Created.
     *
     */
    public synchronized void accept(Serializable eventI,Rmap matchI)
        throws java.lang.InterruptedException
    {
//	System.err.println("\n---StreamRBOListener.accept():eventI:\n" + eventI);
        //EMF 6/28/06: keep a copy of the eventI,
	//             so can search it instead of full RBO
	//newFrames.add(eventI);
	setToPickup(getToPickup() + 1);
	return;
    }

    /**
     * Checks to see if we should skip the request forward.
     * <p>
     *
     * @author Ian Brown
     *
     * @return should we skip forward?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 10/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Created.
     *
     */
    private final boolean checkSkip()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	// This isn't really the right thing to do, but it fixes a temporary
	// problem where the code skips forward incorrectly when reading from
	// oldest and I haven't come up with a better solution.  Perhaps the
	// newer logic for matching time requests rather than frame requests
	// will fix the issue.
	boolean skipForwardR =
	    !((myBase == null) ||
	      (myBase.getReference() == DataRequest.OLDEST));

	return (skipForwardR);
    }

    /**
     * Checks for an initial start point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return was the start point located?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Set EOS when an <code>EndOfStream</code> is posted.
     * 06/20/2003  INB	Need wait if we're looking for <code>NEWEST</code>.
     * 06/16/2003  INB	Handle <code>RequestOptions.maxWait</code>.
     * 10/25/2001  INB	Created.
     *
     */
    private final boolean checkStart()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean continueR = true;
	DataRequest base =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null),
	    myWorking =
	    ((getWorking() instanceof DataRequest) ?
	     ((DataRequest) getWorking()) :
	     null);
	RequestOptions ro = getNBO().getRequestOptions();
	
//System.err.println("\n\n"+this+ro);
//Thread.dumpStack();
//System.err.println("Before  NS: "+needStart+"  NW: "+needWait+"  CR: "+continueR+"  MW:\n"+myWorking+"  Base:\n"+base);

	if ((myWorking != null) &&
	    (myWorking.getDomain() == DataRequest.FUTURE)) {
	    // If this request is for future stuff, we want to wait for
	    // something to show up regardless on whether the source is a new
	    // one or not.
	    haveNewRBO = (getToPickup() > 0);
	    setNeedStart(true);
	    setNeedWait(myWorking.getReference() == DataRequest.NEWEST);

	} else if ((myWorking == null) ||
		   (myWorking.getReference() == DataRequest.ABSOLUTE)) {
	    // If the request is for an absolute point, we don't need to find
	    // the start unless the <code>RequestOptions.maxWait</code> value
	    // is non-zero.
	    setNeedStart((ro == null) ? false : (ro.getMaxWait() != 0));

	} else if (base.getGapControl()) {
	    // If gap control is enabled, then we always think we need to find
	    // a start.
	    setNeedStart(true);

	} else {
	    // If the request is for a relative point, see if we can find a
	    // start time now.

	    if (!findStart()) {
		// If we don't find the start time, then we're either never
		// going to find anything or we need to wait for it.

		if (myWorking.getDomain() == DataRequest.EXISTING) {
		    // If the user was looking for existing data and the
		    // <code>RequestOptions.maxWait</code> field is 0, then
		    // there is no data to respond with. We're off the start of
		    // the data.
		    post(new EndOfStream(EndOfStream.REASON_BOD));
		    setEOS(true);
		    continueR = false;

		} else {
		    // If it acceptable to take data that appears at some
		    // future point, then we need to locate the start later.
		    setNeedStart(true);
		    setNeedWait(true);
		}
	    }
	}
	
	// System.err.println("StreamRBOListener.checkStart(): After  NS: "+needStart+"  NW: "+needWait+"  CR: "+continueR+"  MW:\n"+myWorking+"  Base:\n"+base + "\n");
	
	return (continueR);
    }

    /**
     * Creates the working <code>Rmap</code> from the original
     * <code>Rmap</code>.
     * <p>
     * If the original <code>DataRequest</code> indicates that this is a
     * time-based operation, then nothing is done here.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final void createWorking()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest wOriginal =
	    ((getOriginal() instanceof DataRequest) ?
	     ((DataRequest) getOriginal()) :
	     null);

	if ((getOriginal().getChildAt(0).getTrange() != null) &&
	    (wOriginal != null) &&
	    (wOriginal.getMode() == DataRequest.FRAMES)) {
	    // We don't support time-based frame response requests yet.
	    throw new java.lang.IllegalStateException
		("Requests for time (rather than frame) ranges are not " +
		 "supported when responses are to be in frames.");

	} else if ((getOriginal().getChildAt(0).getTrange() == null) &&
		   (getOriginal().getChildAt(0).getFrange() == null)) {
	    // We don't support time- or frame-based requests that have the
	    // time or frame range down multiple levels yet.
	    throw new java.lang.IllegalStateException
		("Requests must provide a frame or time range at the top of " +
		 "match hierarchy.");
	}

	// If this is a frame-based request, clone the original and clear
	// the repetitions information.
	setWorking((Rmap) getOriginal().clone());
	if (wOriginal != null) {
	    ((DataRequest) getWorking()).setRepetitions(1,0.);
	}
    }

    /**
     * Finds out if the entire request has shown up.
     * <p>
     * This method checks the registration to see if all of the data exists.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the start point exists.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 12/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/10/2003  INB	Changed the <code>maxWait</code> handling to ignore 
     *			(i.e., not wait for) channels that have not shown up at
     *			all.
     * 06/16/2003  INB	Created.
     *
     */
    final boolean findAllRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String[] channels = getWorking().extractNames();
	Rmap entry;
	long now = System.currentTimeMillis(),
	    maxWait = getNBO().getRequestOptions().getMaxWait();
	boolean foundR = ((channels.length == 0) ||
			  ((maxWait > 0) &&
			   ((now - requestStarted) >= maxWait)));

	if (!foundR) {
	    // Locate the requested time span.  For now, we assume a single
	    // time span, which can be found in the hierarchy by simply moving
	    // down the first child.
	    for (entry = getWorking();
		 (entry != null) &&
		     (entry.getNchildren() != 0) &&
		     (entry.getTrange() == null);
		 entry = entry.getChildAt(0)) {
	    }

	    if ((entry == null) || (entry.getTrange() == null)) {
		throw new com.rbnb.api.SerializeException
		    (getWorking() +
		     " is not a request that can be handled using maxWait " +
		     "logic.");
	    }
	    double[] limits = entry.getTrange().getLimits();

	    // Request the registration information for all of the channels.
	    Rmap regReq = Rmap.createFromName(channels[0]);
	    String name;
	    for (int idx = 1; idx < channels.length; ++idx) {
		regReq.mergeWith(Rmap.createFromName(channels[idx]));
	    }

	    // Check the time limits of the registration entries against those
	    // of the request.  Only accept this if all of the entries end at
	    // or after the end of the requested range.
	    Rmap reg = ((SourceHandler) getSource()).getRegistered(regReq);
	    double[] eLimits;
	    foundR = true;
	    for (int idx = 0; foundR && (idx < channels.length); ++idx) {
		entry = reg.findDescendant(channels[idx],false);
		if ((entry == null) ||
		    (entry.getNchildren() == 0) ||
		    (entry.getChildAt(0).getTrange() == null)) {
		    continue;
		} else {
		    eLimits = entry.getChildAt(0).getTrange().getLimits();
		    foundR = (eLimits[1] >= limits[1]);
		}
	    }
	}

	if (!foundR) {
	    haveNewRBO = false;
	    setNeedWait(true);
	}

	return (foundR);
    }

    /**
     * Attempts to find the start of the request.
     * <p>
     * Normally, we simply check to see if there is at least some data that
     * is in the <code>RBO</code> matching the request.  However, if the
     * <code>RequestOptions.maxWait</code> value is non-zero, then we look
     * to see if all of the request can be matched.
     * <p>
     *
     * @author Ian Brown
     *
     * @return was the start of the request found?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 06/16/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/16/2003  INB	Added <code>RequestOptions.maxWait</code> handling.
     * 03/29/2001  INB	Created.
     *
     */
    final boolean findStart()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean foundR = false;
	RequestOptions ro = getNBO().getRequestOptions();

	if ((ro == null) || (ro.getMaxWait() == 0.)) {
	    foundR = findStartRequest();
	} else {
	    foundR = findAllRequest();
	}

	return (foundR);
    }

    /**
     * Finds out if the starting point of the requested data has arrived.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the start point exists.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 03/13/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2007  JPW	For subscription to oldest, if there is no Frange in
     *			the extracted DataRequest, set foundR to false and wait
     *			for frames to show up.  This fixes a bug when a
     *			subscription by frame to oldest starts up before any
     *			data is in the Source, and where there are registered
     *			channels in the Source.  In this case, a DataRequest
     *			was extracted, but it didn't have a start frame.
     * 06/16/2003  INB	Extracted from <code>findStart</code>.
     *
     */
    final boolean findStartRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean foundR = true;

	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null),
	    myOriginal =
	    ((getOriginal() instanceof DataRequest) ?
	     ((DataRequest) getOriginal()) :
	     null),
	    myWorking =
	    ((getWorking() instanceof DataRequest) ?
	     ((DataRequest) getWorking()) :
	     null);

	if ((myWorking == null) ||
	    (myWorking.getReference() == DataRequest.ABSOLUTE)) {
	    // If the reference point is an absolute value, then we were just
	    // waiting for something to show up.
	    foundR = true;

	} else if ((myOriginal == null) ||
		   (myOriginal.getIncrement() == 0.)) {
	    // With a zero increment, we're performing a monitoring operation
	    // where we repeatedly make the same request. In that case, we
	    // always think we've found the start.
	    foundR = true;

	} else {
	    // If the reference is not absolute, then determine what it should
	    // be now that we've gotten some new data.
//System.err.println("StreamRBOListener.findStartRequest: calling extractRmap");
	    Rmap result = ((Rmap) getSource()).extractRmap(getWorking(),true);
//EMF 
//Rmap result = null;
//if (newFrames.isEmpty()) result=(new Rmap()).extractRmap(getWorking(),true);
//else result=((Rmap)newFrames.firstElement()).extractRmap(getWorking(),true);
//System.err.println("StreamRBOListener.findStartRequest result "+result);
	    foundR = ((result != null) && !(result instanceof EndOfStream));

	    if (foundR) {
		// We found a result, ensure that it is what we really want
		// and, if so, make it the current working event.
		if ((myOriginal != null) &&
		    (myOriginal.getReference() == DataRequest.NEWEST) &&
		    (myOriginal.getDomain() == DataRequest.FUTURE) &&
		    (result.getChildAt(0).getFrange() != null))
		{
		    // Future requests for frame ranges must match the latest
		    // frame.
		    TimeRange tLimits = new TimeRange(Double.MAX_VALUE),
			fLimits = new TimeRange(Double.MAX_VALUE);
		    tLimits.setDuration(-Double.MAX_VALUE);
		    fLimits.setDuration(-Double.MAX_VALUE);
//EMF 6/30/06: use limits of first frame, not whole source
		    //((Rmap) newFrames.firstElement()).findLimits
                    //    (tLimits,fLimits);
		    ((RBO) getSource()).getRegistered().findLimits
			(tLimits,
			 fLimits);
		    foundR =
			(result.getChildAt(0).getFrange().getLimits()[1] ==
			 fLimits.getLimits()[1]);
		}
		else if ((myOriginal != null)                                   &&
			 (myOriginal.getReference() == DataRequest.OLDEST)      &&
			 (myOriginal.getNrepetitions() == DataRequest.INFINITE) &&
			 (result.getChildAt(0).getFrange() == null))
		{
		    // JPW 03/09/2007: User has subscribed to oldest, but there
		    //                 is no Frange in the DataRequest;
		    //                 set foundR to false and wait for data to
		    //                 show up
		    foundR = false;
		    setNeedWait(true);
		}
		
		if (foundR) {
		    setWorking(result);
//		    System.err.println("\nfindStartRequest(): working request:\n" + result);
		}
	    } else {
		haveNewRBO = false;
		setNeedWait
		    ((myBase != null) &&
		     ((myBase.getDomain() != DataRequest.EXISTING) ||
		      (myBase.getMode() == DataRequest.CONSOLIDATED)));
	    }
	}

	return (foundR);
    }

    /**
     * Gets the number of frames per iteration.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of frames.
     * @see #setFramesPerIteration(int)
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    private final int getFramesPerIteration() {
	return (framesPerIteration);
    }

    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    public final Log getLog() {
	return (getNBO().getLog());
    }

    /**
     * Gets the need start flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return need to find the start of the request?
     * @see #setNeedStart(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    final boolean getNeedStart() {
	return (needStart);
    }

    /**
     * Gets the need wait flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return need to find the wait of the request?
     * @see #setNeedWait(boolean)
     * @since V2.0
     * @version 10/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2001  INB	Created.
     *
     */
    final boolean getNeedWait() {
	return (needWait);
    }

    /**
     * Handles an <code>EndOfStream</code> returned as a match.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eosI      the <code>EndOfStream</code>.
     * @param frameI    the frame index.
     * @param continueI continue running after this?
     * @return one of:
     *	       <p><ul>
     *	       <li>-1 means that the stream should terminate,</li>
     *	       <li><code>frameI</code> - 1 means repeat the request,</li>
     *	       <li><code>frameI</code> means skip a frame,</li>
     *	       </ul><p>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Set EOS when an <code>EndOfStream</code> is posted.
     * 10/23/2003  INB	Added check for skip logic.
     * 09/28/2001  INB	Created.
     *
     */
    final int handleEOS(EndOfStream eosI,int frameI,boolean continueI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int frameR = frameI;
	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	// On an end of stream, determine whether we should go back to a wait
	// state or exit.
	if ((myBase == null) ||
	    (myBase.getDomain() == DataRequest.EXISTING)) {
	    // If the user wanted existing data, then we either skip this entry
	    // (on a gap) or we're done.
	    if ((eosI.getReason() == EndOfStream.REASON_NODATA) ||
		(eosI.getReason() == EndOfStream.REASON_NONAME)) {
		// If the reason is that nothing exists at this point, skip
		// the response unless we aren't supposed to continue anyway.
		if (!continueI) {
		    post(eosI);
		    setEOS(true);
		    frameR = -1;
		}
		
	    } else {
		// Otherwise, post it and we're done.
		post(eosI);
		setEOS(true);
		frameR = -1;
	    }

	} else {
	    haveNewRBO = false;
	    setNeedWait(true);

	    if (eosI.getReason() == EndOfStream.REASON_BOD) {
		// If we fell off the start of the data, then we're done.
		post(eosI);
		setEOS(true);
		frameR = -1;

	    } else {
		// If we get here, then we can go back to waiting for data to
		// show  up. Note, however, that we do want to increment the
		// current time if we are to skip.
		if (getFramesPerIteration() == 1) {
		    boolean skip = checkSkip();
		     setNeedStart(true);
		     if (skip) {
			 updateWorking();
		     }
		}
	    }
	}

	return (frameR);
    }

    /**
     * Posts a response <code>Rmap</code> to the application.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2003  INB	When an <code>EndOfStream</code> is retrieved on a
     *			request that does not repeat, then we immediately
     *			<code>stop</code>.
     * 06/07/2001  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializable = serializableI;
	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	if ((myBase != null) && myBase.getGapControl()) {
	    // With gap control enabled, we need to send a request for
	    // acknowledgement.
	    setWaiting(true);
	    serializable = new RSVP(0,serializableI);
	}
	super.post(serializable);
    }

    /**
     * Processes the working request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param countI the number of repetitions so far.
     * @return the new count or -1 if we hit the end.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.lang.IllegalStateException
     *		  thrown if an unexpected state is reached
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/27/2008
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/27/2008  JPW  Support monitor mode data request with more than 1 child
     * 2005/07/15  WHF  Added OutOfMemoryError handling.
     * 04/25/2005  JPW	If we got a match and if the user is in Monitor mode,
     *			check that we don't send out a repeated frame.
     * 04/25/2005  JPW	Throw IllegalStateException if the Monitor mode
     *			match has more than one point in the Frange.
     * 04/14/2004  INB	Handle <code>REASON_BOD</code> the same as
     *			<code>REASON_END</code> for streaming requests - i.e.,
     *			it terminates the stream.
     * 10/27/2003  INB	Update the count for infinite requests when we get a
     * 			frame.
     * 06/20/2003  INB	Allow for running off the end when not waiting for
     *			future data to arrive.  Go into a wait in that case.
     * 03/29/2001  INB	Created.
     *
     */
    final long processWorking(long countI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.lang.IllegalStateException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean continueR = true;
	Rmap subWorking =
	    (getFramesPerIteration() == 1) ?
	    getWorking() :
	    (Rmap) getWorking().clone();
	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null),
	    myOriginal =
	    ((getOriginal() instanceof DataRequest) ?
	     ((DataRequest) getOriginal()) :
	     null),
	    dSub =
	    ((subWorking instanceof DataRequest) ?
	     ((DataRequest) subWorking) :
	     null);
	double increment = ((getFramesPerIteration() == 1) ?
			    myBase.getIncrement() :
			    1.);
	long countR = countI;
	Rmap match;
	byte reasonEOS;

	for (int frame = 0; frame < getFramesPerIteration(); ++frame) {
//Rmap newFr = null;
	    reasonEOS = 
		(((dSub != null) &&
		  (dSub.getReference() != DataRequest.ABSOLUTE)) ?
		 EndOfStream.REASON_NONAME :
		 EndOfStream.REASON_NODATA);
	    
	    // System.err.println(
	    //   "\nStreamRBOListener.processWorking: call extractRmap with " +
	    //   "request =\n" +
	    //   subWorking);
	    
	    // System.err.println(
	    //  "getSource() before extractRmap call =\n" + (Rmap)getSource());
	    
	    //Runtime rt = Runtime.getRuntime();
	    // System.err.println(
	    //     "Before: free/max/total " +
	    //     rt.freeMemory()/1024 +
	    //     "k/" +
	    //     rt.maxMemory()/1024 +
	    //     "k/" +
	    //     rt.totalMemory()/1024 +
	    //     'k');
	    try {
	      match = ((Rmap) getSource()).extractRmap(subWorking,true);

//EMF
//if (newFrames.isEmpty()) newFr=new Rmap();
//else newFr=(Rmap)newFrames.remove(0);
//newFr.setName("mySource");
//System.err.println("newFr "+newFr);
//System.err.println();
            //match = newFr.extractRmap(subWorking,true);
            //match = ((Rmap)newFrames.remove(0)).extractRmap(subWorking,true);
//System.err.println("\nStreamRBOListener.processWorking: subWorking "+subWorking);
// System.err.println("\nStreamRBOListener.processWorking: match "+match);

	    } catch (OutOfMemoryError oome) {
		System.err.println(
			"OutOfMemoryError servicing request.  Recovering...");
		// Save the thread!
		match = null;
		// Free more memory before other operations fail:
		System.gc();
		getNBO().asynchronousException(new IllegalStateException(
			"Insufficient memory to service request."));
		// 2005/07/15  WHF  Tried setting this on a lark, to help
		//  reset the sink's connection state, and lo! it worked!
		setTerminateRequested(true);
		// Tells caller to set EOS:
		return -1;
	    }
	    // System.err.println(
	    //     "After:  free/max/total " +
	    //     rt.freeMemory()/1024 +
	    //     "k/" +
	    //     rt.maxMemory()/1024 +
	    //     "k/" +
	    //     rt.totalMemory()/1024 +
	    //     'k');
	    
	    // System.err.println("getSource() after extractRmap call =\n" +
	    //                    (Rmap)getSource());
	    
	    // System.err.println(
	    //     "StreamRBOListener.processWorking: return from " +
	    //     "extractRmap:\n" + match);
	    
	    // JPW 04/25/2005
	    // If we got a match and if the user is in Monitor mode,
	    // check that we don't send out a repeated frame
	    /*
	    TimeRange monitorTR = null;
	    if ( (!(match instanceof EndOfStream))        &&
		 (match.getNchildren() > 0)               &&
		 (subWorking instanceof DataRequest)      &&
		 (((DataRequest)subWorking).getDomain() ==
		                      DataRequest.FUTURE) &&
		 (((DataRequest)subWorking).getMode() ==
		                      DataRequest.FRAMES) &&
		 (((DataRequest)subWorking).getReference() ==
		                      DataRequest.NEWEST) &&
		 ((monitorTR = match.getChildAt(0).getFrange()) != null) )
	    {
		if (monitorTR.getNptimes() != 1) {
		    // I only expect 1 time point; throw this exception
		    // to notify me that some Monitor mode cases can have
		    // more than 1 point
		    throw new IllegalStateException(
			"For Monitor mode match, only 1 time point was " +
			"expected; got " +
			monitorTR.getNptimes());
		}
		// System.err.println("\n\nTimeRange:" + monitorTR);
		if (monitorTR.getPtimes()[0] <= monitorFrameIdx) {
		    // Matched a frame that has already been dealth with!
		    // NOTE: countR = the number of frames retreived;
		    //       I don't think we increment it here
		    setNeedWait(true);
		    // For Monitor mode, we always just want the most recent
		    // data, so set pickup counter to 0 (we don't "get behind"
		    // like in Subscribe mode).
		    setToPickup(0);
		    setNeedStart(false);
		    continue;
		}
		else {
		    monitorFrameIdx = monitorTR.getPtimes()[0];
		}
		// System.err.println("monitorFrameIdx: " + monitorFrameIdx);
	    }
	    */
	    
	    boolean bMonitorDataRequest = false;
	    if ( (subWorking instanceof DataRequest)                           &&
		 (((DataRequest)subWorking).getDomain() == DataRequest.FUTURE) &&
		 (((DataRequest)subWorking).getMode() == DataRequest.FRAMES)   &&
		 (((DataRequest)subWorking).getReference() == DataRequest.NEWEST) )
	    {
		bMonitorDataRequest = true;
	    }
	    
	    // JPW 07/23/08: The previous logic to check frame indeces when in
	    //               monitor mode didn't account for the fact that there
	    //               may be multiple channels, each with its own frame
	    //               index.  This new logic does account for that.
	    if ( (!(match instanceof EndOfStream)) &&
		 (match.getNchildren() > 0)        &&
		 (bMonitorDataRequest) )
	    {
		// Go through each child and make sure the frame index for
		// each child is greater than monitorFrameIdx; if any child
		// has a frame index lower than monitorFrameIdx, make a note
		// of it for later removal from the Rmap.
		Vector childrenToRemove = null;
		double newMonitorFrameIdx = -1.0;
		for (int i = 0; i < match.getNchildren(); ++i) {
		    TimeRange monitorTR = match.getChildAt(i).getFrange();
		    if (monitorTR == null) {
			continue;
		    }
		    if (monitorTR.getNptimes() != 1) {
			// I only expect 1 time point; throw this exception
			// to notify me that some Monitor mode cases can have
			// more than 1 point
			throw new IllegalStateException(
			    "For Monitor mode match, only 1 time point was " +
			    "expected; got " +
			    monitorTR.getNptimes());
		    }
		    // System.err.println("\n\nTimeRange:" + monitorTR);
		    if (monitorTR.getPtimes()[0] <= monitorFrameIdx) {
			// This child should be remove from match
			if (childrenToRemove == null) {
			    childrenToRemove = new Vector();
			}
			childrenToRemove.addElement(match.getChildAt(i));
		    }
		    if (monitorTR.getPtimes()[0] > newMonitorFrameIdx) {
			newMonitorFrameIdx = monitorTR.getPtimes()[0];
		    }
		}
		monitorFrameIdx = newMonitorFrameIdx;
		// System.err.println("monitorFrameIdx: " + monitorFrameIdx);
		if (childrenToRemove != null) {
		    // System.err.print("Orig num children = " + match.getNchildren());
		    for (int i = 0; i < childrenToRemove.size(); ++i) {
			match.removeChild((Rmap)childrenToRemove.elementAt(i));
		    }
		    // System.err.println(" ==> final num children = " + match.getNchildren());
		    if (match.getNchildren() == 0) {
			// No children to post!
			// NOTE: countR = the number of frames retreived;
			//       I don't think we increment it here
			setNeedWait(true);
			// For Monitor mode, we always just want the most recent
			// data, so set pickup counter to 0 (we don't "get behind"
			// like in Subscribe mode).
			setToPickup(0);
			setNeedStart(false);
			continue;
		    }
		}
	    }
	    
	    if ((match != null) &&
		((dSub != null) &&
		(dSub.getReference() != DataRequest.ABSOLUTE)) &&
		(!(match instanceof EndOfStream) ||
		 (match.getNchildren() > 0))) {
		// Create an Rmap with absolute timestamps by calling extractRmap() once again.
		subWorking = match;
		dSub = ((subWorking instanceof DataRequest) ?
			((DataRequest) subWorking) :
			null);
		reasonEOS = EndOfStream.REASON_NODATA;
		if ( (!bMonitorDataRequest) ||
		     ( (bMonitorDataRequest) && (match.getNchildren() == 1) ) )
		{
		    match = ((Rmap) getSource()).extractRmap(match,true);
		}
		else
		{
		    // This is a monitor mode data request with more than 1 child.
		    // If all the children have the same frame index, then go
		    // ahead and make one call to extractRmap().  Otherwise,
		    // call extractRmap() for each child and then combine the
		    // results into a final Rmap
		    // Make a stripped-down parent "shell" Rmap
		    Rmap parentRequestRmap = (Rmap)match.clone();
		    for (int i = 0; i < match.getNchildren(); ++i) {
			parentRequestRmap.removeChildAt(0);
		    }
		    Rmap finalRmap = null;
		    for (int i = 0; i < match.getNchildren(); ++i) {
			Rmap tempParent = (Rmap)parentRequestRmap.clone();
			Rmap childRmap = (Rmap)match.getChildAt(i).clone();
			tempParent.addChild(childRmap);
			Rmap nextMatch = ((Rmap) getSource()).extractRmap(tempParent,true);
			if (finalRmap == null) {
			    finalRmap = nextMatch;
			} else {
			    for (int j = 0; j < nextMatch.getNchildren(); ++j) {
				finalRmap.addChild((Rmap)nextMatch.getChildAt(j).clone());
			    }
			}
		    }
		    match = finalRmap;
		}
                //EMF
		//match = newFr.extractRmap(match,true);
		
		// System.err.println(
		//     "StreamRBOListener.processWorking(): this:\n" +
                //     this +
                //     "\nmatch 2:\n" + match);

	    } else if ((myBase.getDomain() == DataRequest.FUTURE) &&
		       (myBase.getReference() == DataRequest.OLDEST) &&
		       ((match == null) ||
			((match instanceof EndOfStream) &&
			 (((EndOfStream) match).getReason() !=
			  EndOfStream.REASON_END) &&
			 (((EndOfStream) match).getReason() !=
			  EndOfStream.REASON_BOD)))) {
		// If we got an EOS while streaming from oldest, never
		// increment the request.
		setNeedWait(true);
		countR = -2;
		break;

	    } else if (!getNeedWait() &&
		       (myBase != null) &&
		       ((myBase.getDomain() == DataRequest.ALL) ||
			(myBase.getDomain() == DataRequest.FUTURE))) {
		// If we're looking for future data without waiting, then
		// running off the end simply means that we need to go into a
		// wait for the future.
		if ((match != null) &&
		    (match instanceof EndOfStream) &&
		    (((EndOfStream) match).getReason() !=
		     EndOfStream.REASON_END) &&
		    (((EndOfStream) match).getReason() !=
		     EndOfStream.REASON_BOD)) {
		    setNeedWait(true);
		    countR = -2;
		    break;
		}
	    }

	    if (match == null) {
		match = new EndOfStream(reasonEOS);
	    }

	    if ((frame == getFramesPerIteration() - 1) &&
		((myOriginal == null) ||
		 (myOriginal.getNrepetitions() != DataRequest.INFINITE))) {
		// If the number of repetitions is not infinite, then update
		// the count.
		if (++countR ==
		    ((myOriginal == null) ?
		     1 :
		     myOriginal.getNrepetitions())) { 
		    // If the count equals the number of repetitions, then
		    // we've hit the normal end of the stream.
		    continueR = false;
		}

	    } else if (!(match instanceof EndOfStream)) {
		++countR;
	    }

	    if (!(match instanceof EndOfStream)) {
		if (!continueR) {
		    Rmap temp = match;
		    match = new EndOfStream();
		    match.addChild(temp);
		}
		
		// System.err.println(
		//  "StreamRBOListener.processWorking: call post(); match:\n" +
		//  match);
		
		// Here's a way to get the time to be able to print it out
		// Rmap child = match;
		// TimeRange timeR = child.getTrange();
		// while (timeR == null) {
		//     child = child.getChildAt(0);
		//     if (child == null) {
		// 	break;
		//     }
		//     timeR = child.getTrange();
		// }
		// System.err.println(
		//     "StreamRBOListener.processWorking(): post(); time = " +
		//     timeR +
		//     ", frame index = " +
		//     subWorking.getChildAt(0).getFrange());
		
		post(match);
		haveNewRBO = false;
		if ((myBase != null) &&
		    (myBase.getDomain() != DataRequest.EXISTING)) {
		    if ((myBase.getReference() == DataRequest.NEWEST) &&
			(myBase.getIncrement() == 0.)) {
			setNeedWait(true);
			setToPickup(0);
		    }
		}		
		if (!continueR) {
		    break;
		}
		setNeedStart(false);

	    } else {
		if ((myBase != null) &&
		    (myBase.getDomain() != DataRequest.EXISTING)) {
		    if ((myBase.getReference() == DataRequest.NEWEST) &&
			(myBase.getIncrement() == 0.)) {
			setNeedWait(true);
			setToPickup(0);
		    }
		}		
		frame = handleEOS((EndOfStream) match,frame,continueR);

		//System.err.println("Frame: " + frame);

		if (frame == -1) {
		    continueR = false;
		    break;

		} else if ((myBase.getDomain() == DataRequest.FUTURE) &&
			   (myBase.getReference() == DataRequest.OLDEST)) {
		    setNeedWait(true);
		    countR = -2;
		    break;
		}
	    }

	    if (frame < getFramesPerIteration() - 1) {
		if (subWorking != getWorking()) {
		    subWorking.addToStart(increment);
		}
		waitToPickup();
	    }
	}

	if (!continueR) {
	    countR = -1;
	}

	return (countR);
    }

    /**
     * Runs this <code>StreamRBOListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/15/2004  INB	Added posting of <code>EndOfStream</code> if we are
     *			shutting down without having done so.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 10/27/2003  INB	Ensure that we update the request only if we get a
     *			frame in stream from oldest mode.
     * 06/20/2003  INB	Don't update the request if we ran off the end when
     *			looking for future data (<code>count == -2</code>).
     * 06/16/2003  INB	Set the time when this loop begins.
     * 03/27/2001  INB	Created.
     *
     */
    public final void run() {
	getDoor().setIdentification(toString());
	long count = 0;
	requestStarted = System.currentTimeMillis();

	try {
	    if ((getSource() instanceof ClientHandler) &&
		!((ClientHandler) getSource()).allowAccess(getNBO())) {
		post(new EndOfStream(EndOfStream.REASON_EOD));
		setEOS(true);

	    } else if (checkStart()) {
		while (!getTerminateRequested()) {
		    // Loop until we reach an end of stream condition or we are
		    // terminated on a request by our parent. Wait for
		    // something to arrive if necessary.
		    waitToPickup();

		    if (getNeedStart()) {
			// If we haven't seen the start of the request (or a
			// restart), then check things out again.
			if (getTerminateRequested()) {
			    break;
			} else if (!findStart()) {
			    if (getNeedWait()) {
				continue;
			    } else {
				post(new EndOfStream(EndOfStream.REASON_BOD));
				break;
			    }
			}
		    }

		    // Process the working request.
		    if ((count = processWorking(count)) == -1) {
			break;
		    }
		    
		    if (!getNeedStart() && !(count == -2)) {
			// If we're not waiting for the start, then move on to
			// the next increment.
			updateWorking();
		    } else if (count == -2) {
			count = 0;
		    }

		    if (getThread() != null) {
			((ThreadWithLocks) getThread()).ensureLocksCleared
			    (toString(),
			     "StreamRBOListener.run(1)",
			     getNBO().getLog(),
			     getNBO().getLogLevel(),
			     getNBO().getLogClass());
		    }
		}
	    }

	} catch (java.io.InterruptedIOException e) {
	} catch (java.lang.InterruptedException e) {
	} catch (java.lang.Exception e) {
	    try {
		e.printStackTrace();

		getLog().addException
		    (Log.STANDARD,
		     getNBO().getLogClass(),
		     getNBO().getName(),
		     e);
		getNBO().asynchronousException
		    (new java.lang.IllegalStateException
			("Internal server error has occured, " +
			 "request aborted.\n" +
			 getBaseRequest()));
	    } catch (java.lang.Exception e1) {
	    }
	    setEOS(true);

	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "StreamRBOListener.run(2)",
		     getNBO().getLog(),
		     getNBO().getLogLevel(),
		     getNBO().getLogClass());
	    }
	}

	if (!getTerminateRequested()) {
	    // If this thread was not terminated on a request from another
	    // thread, then go through the termination work here.
	    try {
		if (!getEOS()) {
		    post(new EndOfStream());
		}
		stop();
	    } catch (java.lang.Exception e) {
	    }
	}

	// Notify any waiting thread.
	setTerminateRequested(false);
	setThread(null);
    }

    /**
     * Sets the number of frames per iteration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param framesPerIterationI the number of frames.
     * @see #getFramesPerIteration()
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    private final void setFramesPerIteration(int framesPerIterationI) {
	framesPerIteration = framesPerIterationI;
    }

    /**
     * Sets the need start flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needStartI  do we need to find the start of the request?
     * @see #getNeedStart()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    final void setNeedStart(boolean needStartI) {
	needStart = needStartI;
    }

    /**
     * Sets the need wait flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needWaitI  do we need to wait?
     * @see #getNeedWait()
     * @since V2.0
     * @version 10/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2001  INB	Created.
     *
     */
    final void setNeedWait(boolean needWaitI) {
	needWait = needWaitI;
    }

    /**
     * Updates the working request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/15/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/31/2008  MJM	Limit increment so it doesnt get ahead of actual data.  
     * 04/15/2005  JPW	Increment time of the first child in the request Rmap
     *                      only.  This avoids the problem of incrementing
     *                      multiple times if there is more than one RingBuffer
     *                      in the RBO.
     * 04/02/2001  INB	Created.
     *
     */
    final void updateWorking()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest myOriginal =
	    ((getOriginal() instanceof DataRequest) ?
	     ((DataRequest) getOriginal()) :
	     null);
	if ((myOriginal != null) &&
	    (myOriginal.getIncrement() != 0.)) {
	    // If there is an repetition increment to the request, add it to
	    // the start time of the request.
	    // JPW 04/15/2005: Increment time of first child only.  This
	    //                 avoids the problem of incrementing multiple
	    //                 times when there is more than one RingBuffer
	    // Used to call addToStart() on the entire request
	    // getWorking().addToStart(myOriginal.getIncrement());
	    // We shouldn't have a time at the top level of the request
	    if (getWorking().getTrange() != null) {
		throw new java.lang.IllegalStateException(
		    "Unexpected time at top level of DataRequest:\n" +
		    getWorking());
	    }

	    // MJM 10/31/08: find frame limit, and never increment more than 1 past the end
	    TimeRange tLimits = new TimeRange(Double.MAX_VALUE),
		fLimits = new TimeRange(Double.MAX_VALUE);
	    tLimits.setDuration(-Double.MAX_VALUE);
	    fLimits.setDuration(-Double.MAX_VALUE);
	    
	    ((RBO) getSource()).getRegistered().findLimits(tLimits,fLimits);	// mjm
	    
	    // Now we call addToStart() on the first child only
	    Rmap child = getWorking().getChildAt(0);
	    
	    if(child.getFrange().getLimits()[1] <= fLimits.getLimits()[1])    // mjm                     
	    	child.addToStart(myOriginal.getIncrement());
	}
    }

    /**
     * Waits for something to arrive.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 04/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2004  INB	In monitor mode, we want to wait until we've been
     *			synchronized by the client before moving on, even
     *			if there is data waiting.
     * 11/07/2003  INB	Eliminated unnecessary synchronization.
     * 06/16/2003  INB	Handle <code>RequestOptions.maxWait</code>.
     * 03/29/2001  INB	Created.
     *
     */
    private final void waitToPickup()
	throws java.lang.InterruptedException
    {
	if (getNeedWait()) {
	    if (!getWaiting() && (getToPickup() > 0)) {
		setToPickup(getToPickup() - 1);

	    } else if (!haveNewRBO || getWaiting()) {
		long stopTime = Long.MAX_VALUE,
		    maxWait;
		if ((getNBO().getRequestOptions() != null) &&
		    ((maxWait =
		      getNBO().getRequestOptions().getMaxWait()) > 0)) {
		    stopTime = requestStarted + maxWait;
		}
		super.waitToPickup(true,stopTime);
	    }
	}
    }
}
