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

/**
 * Extended <code>StreamDHListener</code> that Listens for
 * <code>RBO</code> "events" for a <code>StreamRequestHandler</code> using
 * a <code>TimeRelativeRequest</code>.
 * <p>
 * There are two basic subtypes of requests that are handled by this class:
 * <p><ol>
 *    <li>Request/response time relative requests that ask for a specific
 *	  amount of data relative to a particular time (could be newest or
 *	  oldest), and</li>
 *    <li>Subscription streaming time relative requests that start from either
 *        the oldest existing or the next data to show up and then retrieve
 *	  a specific amount of data each time.</li>
 * </ol><p>
 * The code proceeds as follows:
 * <p><ol>
 *    <li>Ensure that the request type is legal, create the working time
 *	  relative request from the original request and put it into a list of
 *	  requests to satisfy
 *	  (<code>StreamTimeRelativeListener.createWorking</code>).</li>
 *    <li>For oldest or newest requests, determine an absolute time.  If the
 *	  request is request/response, then there must be existing data.
 *	  Otherwise, the code will wait for data if none is yet available
 *	  (<code>StreamTimeRelativeListener.updateReference</code>),</li>
 *    <li>Start handling the request by determining if the NBO sink is
 *	  allowed to access the RBO source
 *	  (<code>StreamTimeRelativeListener.run</code>,</li>
 *    <li>Loop until the request has been fully satisfied or is terminated
 *	  by the client,</li>
 *    <li>If necessary, wait for the data
 *	  (<code>StreamTimeRelativeListener.waitToPickup</code> and
 *	  <code>StreamTimeRelativeListener.waitForData</code>),</li>
 *    <li>Loop until all of the list of requests to satisfy has been emptied
 *        (there may be more than one entry in here if different channels have
 *	  different times)
 *	  (<code>StreamTimeRelativeListener.process</code>),</li>
 *    <li>Convert the time relative request back to a regular request by
 *	  locating, for each channel if necessary, the data point whose time
 *	  has the requested relationship (less, less-equal, greater-equal, or
 *	  greater than) the reference time of the request
 *	  (<code>RBO.extractTimeRelative</code>),</li>
 *    <li>If no such data points can be located, then determine whether we ran
 *	  off the beginning of the available data (in which case, no data
 *	  matches or will ever match the request), or off the end of the
 *	  available data (if the request has a number of repetitions, then we
 *	  can wait for more data),</li>
 *    <li>If data points could be found, then add the regular request to a
 *	  merged request representing all of the input time relative
 *	  requests.  Store the time relative list in a new list of requests to
 *	  be worked on for the next repetition (if any),</li>
 *    <li>Get the matching data from the RBO
 *	  (<code>Rmap.extractRmap</code>),</li>
 *    <li>If there is no such match, then we've run off the beginning.  If
 *	  we're moving backwards, that's OK, we'll probably determine next time
 *	  that there is no available data.  If we're moving forwards, then
 *	  we've fallen so far behind that we won't be able to catch up.  Post
 *	  an <code>EndOfStream</code> and abort the request
 *	  (<code>StreamTimeRelativeListener.post</code>,
 *	  <code>StreamListener.post</code>, ),
 *	  <code>StreamRemoteListener.post</code>, ),
 *	  <code>StreamServerListener.post</code>, and ),
 *	  <code>StreamRequestHandler.post</code>),</li>
 *    <li>If we got something, then update the time references for each of the
 *	  requests saved for next time
 *	  (<code>StreamTimeRelativeListener.updateReference</code>) and post
 *	  the result,</li>
 * </ol><p>
 *
 * @author Ian Brown
 *
 * @see #createWorking()
 * @see #process()
 * @see #post(com.rbnb.api.Serializable serializableI)
 * @see #run()
 * @see #waitForData()
 * @see #waitToPickup()
 * @see com.rbnb.api.RBO#extractTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions optionsI)
 * @see com.rbnb.api.Rmap#extractRmap(com.rbnb.api.Rmap requestI,boolean dataFlagI)
 * @see com.rbnb.api.StreamRBOListener
 * @see com.rbnb.api.StreamServerListener
 * @see com.rbnb.api.StreamRequestHandler
 * @since V2.2
 * @version 03/13/2007
 */

/*
 * Copyright 2003, 2004, 2005, 2007 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/13/2007  JPW	Make a change in createWorking():
 *			For subscription to oldest, if there is no Trange in
 *			the extracted DataRequest, wait for data to show up.
 *			This fixes a bug when a subscription by time to oldest
 *			starts up before any data is in the Source, and where
 *			there are registered channels in the Source.  In this
 *			case, a DataRequest was extracted, but it didn't have a
 *			start time.
 * 04/19/2005  JPW	Made change in createWorking():
 *                      If Subscribing by time and the request start time is
 *                      zero, then adjust the request start time to ensure
 *                      that an appropriate first frame is sent to the
 *                      Sink.  For example, this fixes a problem where
 *                      Subscribe(cmap,0,0,"oldest") wasn't returning the
 *                      very first frame in the Source.
 * 08/03/2004  INB	Added documentation describing how this class operates.
 * 02/26/2004  INB	<code>accept</code> is a NOP if neither event nor match
 *			is set.  Don't wait one time after we get something,
 *			but make sure we decrement the to pickup counter.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 12/10/2003  INB	Handle <code>maxWait</code> for non-streaming requests.
 * 12/08/2003  INB	Pass the <code>NBO's RequestOptions</code> to the
 *			<code>RBO</code> for processing.
 * 11/06/2003  INB	Created from <code>StreamRBOListener</code>.
 *
 */
final class StreamTimeRelativeListener
    extends com.rbnb.api.StreamDHListener
    implements com.rbnb.api.GetLogInterface
{
    /**
     * new <code>RBO</code> being handled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/06/2003
     */
    private boolean haveNewRBO = false;

    /**
     * do we need to wait for things to arrive?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/06/2003
     */
    private boolean needWait = false;

    /**
     * time that request was initiated.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/06/2003
     */
    private long requestStarted = 0;

    /**
     * the list of <code>TimeRelativeRequests</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/06/2003
     */
    private java.util.Hashtable trRequests = new java.util.Hashtable();

    /**
     * Class constructor to build a <code>StreamTimeRelativeListener</code> for
     * the specified code>StreamParent</code>, request <code>Rmap</code>, and
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
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    StreamTimeRelativeListener(StreamParent parentI,
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
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    StreamTimeRelativeListener(StreamParent parentI,
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
     * @since V2.2
     * @version 02/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2004  INB	NOP if neither event nor match is set.
     * 11/06/2003  INB	Created.
     *
     */
    public synchronized void accept(Serializable eventI,Rmap matchI)
        throws java.lang.InterruptedException
    {
	if ((eventI != null) || (matchI != null)) {
	    setToPickup(getToPickup() + 1);
	}
    }

    /**
     * Creates the working <code>Rmap</code> from the original
     * <code>Rmap</code>.
     * <p>
     * This method actually creates the <code>TimeRelativeRequest</code>.
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
     * @since V2.2
     * @version 03/13/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2007  JPW	For subscription to oldest, if there is no Trange in
     *			the extracted DataRequest, wait for data to show up.
     *			This fixes a bug when a subscription by time to oldest
     *			starts up before any data is in the Source, and where
     *			there are registered channels in the Source.  In this
     *			case, a DataRequest was extracted, but it didn't have a
     *			start time.
     * 04/19/2005  JPW	If Subscribing by time and the request start time is
     *                      zero, then adjust the request start time to ensure
     *                      that an appropriate first frame is sent to the
     *                      Sink.  For example, this fixes a problem where
     *                      Subscribe(cmap,0,0,"oldest") wasn't returning the
     *                      very first frame in the Source.
     * 11/06/2003  INB	Created.
     *
     */
    final void createWorking()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest request = (DataRequest) getOriginal();
	
/*
 * JPW debug print
System.err.println(
"\n\nStreamTimeRelList.createWorking(): original request:\n" + request);
 *
*/

	TimeRelativeRequest trRequest = 
	    TimeRelativeRequest.createFromRequest(request);
	
/*
 * JPW debug print
System.err.println(
"StreamTimeRelList.createWorking(): trRequest:\n" + trRequest);
 *
*/

	TimeRelativeChannel trc;

	if (request.getReference() == DataRequest.ABSOLUTE) {
	    // Absolute requests are used directly.
	    trRequests.put(trRequest,new Integer(0));

	} else {
	    // For non-absolute requests, find the initial time.  After that,
	    // we can treat this as a normal request.

	    // NOTE: will need to change this handling if we want to support
	    // MONITOR mode.
	    
	    // JPW 04/19/2005: Bug fix to ensure that we get the first frame
	    //                 in a time-based Subscription
	    // Get the requests start time
	    TimeRange tr = trRequest.getTimeRange();
	    double[] times = tr.getPtimes();
	    // If Subscribing to NEWEST then we'll have to wait until we see
	    // if there is existing data or not before adjusting start time
	    boolean bAdjustTimeIfNoExistingData = false;
	    if ( (times.length == 1) && (times[0] == 0.0) ) {
		if (request.getReference() == DataRequest.NEWEST) {
		    bAdjustTimeIfNoExistingData = true;
		} else if (request.getReference() == DataRequest.OLDEST) {
		    // Set a new start time such that the time of the earliest
		    // source frame will always be after the request start
		    // time. Thus, the earliest source frame will match and be
		    // sent to the sink
		    tr.set(-Double.MAX_VALUE, tr.getDuration());
		}
	    }
	    
	    // JPW 03/13/2007: Add bExtractAgain
	    boolean bExtractAgain = false;
	    Rmap match;
	    do {
		match = ((Rmap) getSource()).extractRmap(request,true);
		// System.err.println(
		//     "createWorking(): return from extractRmap(): match:\n" +
		//     match);
		
		// JPW 03/13/2007: Add bExtractAgain; add a check if the
		//                 DataRequest is doing a time-based subscribe
		//                 to oldest but Trange in match is null
		bExtractAgain =
		    ( (match == null)                ||
		      (match instanceof EndOfStream) ||
		      ( (request != null)                                   &&
		        (request.getReference() == DataRequest.OLDEST)      &&
		        (request.getNrepetitions() == DataRequest.INFINITE) &&
		        (match.getChildAt(0).getTrange() == null) ) );
		
		// JPW 03/13/2007: Use bExtractAgain
		// if ((match == null) || (match instanceof EndOfStream)) {
		if (bExtractAgain) {
		    if (request.getNrepetitions() == 1) {
			return;
		    }
		    // JPW 04/19/2005: No existing data; if Subscribing to
		    //                 NEWEST then set a new start time such
		    //                 that the time of the earliest source
		    //                 frame will always be after the request
		    //                 start time.  Thus, the earliest source
		    //                 frame will match and be sent to the sink
		    if (bAdjustTimeIfNoExistingData) {
			bAdjustTimeIfNoExistingData = false;
			TimeRange temptr = trRequest.getTimeRange();
			temptr.set(Double.MAX_VALUE, tr.getDuration());
		    }
		    haveNewRBO = false;
		    setNeedWait(true);
		    waitToPickup();
		    setNeedWait(false);
		}
	    } while (bExtractAgain);  // JPW 03/13/2007: Use bExtract again instead of "((match == null) || (match instanceof EndOfStream));"

//System.err.println("StreamTimeRelList.createWorking(): before call to updateReference(): trRequest:\n" + trRequest);

            updateReference(trRequest,0,match);

//System.err.println("StreamTimeRelList.createWorking(): after call to updateReference(): trRequest:\n" + trRequest);

	}
    }

    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    public final Log getLog() {
	return (getNBO().getLog());
    }

    /**
     * Gets the need wait flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return need to find the wait of the request?
     * @see #setNeedWait(boolean)
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    final boolean getNeedWait() {
	return (needWait);
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
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
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

	super.post(serializable);
    }

    /**
     * Performs the processing loop.
     * <p>
     * This method handles one or more <code>TimeRelativeRequests</code> that
     * are all associated with a particular <code>RBO</code>.  It starts with a
     * single one and, so long as the time reference stays the same for all of
     * the channels, the method continues to use that one.  If, however, at any
     * time, the reference changes, the method splits the request into a set of
     * requests, one for each channel, and then handles them individually.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the reason for the end.
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
     * @since V2.2
     * @version 02/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2004  INB	Don't wait one time after we get something, but make
     *			sure we decrement the to pickup counter.
     * 12/10/2003  INB	Handle <code>maxWait</code> for non-streaming requests.
     * 12/08/2003  INB	Pass the <code>NBO's RequestOptions</code> to the
     *			<code>RBO</code> for processing.
     * 11/06/2003  INB	Created.
     *
     */
    private final byte process()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest oRequest = (DataRequest) getOriginal();
	TimeRelativeRequest request;
	TimeRelativeRequest nRequest;
	TimeRelativeChannel trc;
	// Save a handle to the existing requests as iRequests
	java.util.Hashtable iRequests = trRequests;
	java.util.Hashtable workRequests = new java.util.Hashtable();
	java.util.Enumeration requests = iRequests.keys();
	Integer icount;
	int count;
	Rmap fullRequest = null;
	Rmap regRequest = null;
	EndOfStream eos;
	Rmap match;
	boolean gotSomething = false;
	byte reasonR = EndOfStream.REASON_END;
	
	// Create a new Hashtable to store new requests
	trRequests = new java.util.Hashtable();


//System.err.println("\n\n" + this + " processing " + iRequests);
//System.err.println(oRequest);
//Thread.dumpStack();

	if (getNeedWait() || (getToPickup() > 0)) {
	    // Wait until something shows up.
	    waitToPickup();
	}

	DataRequest lOriginal = (DataRequest) getOriginal();
	if ((lOriginal.getNrepetitions() == 1) &&
	    (lOriginal.getReference() == DataRequest.ABSOLUTE) &&
	    (getNBO().getRequestOptions() != null) &&
	    (getNBO().getRequestOptions().getMaxWait() != 0.)) {
	    // If there is a <code>maxWait</code>, then wait for the data
	    // to show up.
	    waitForData();
	}

	while (!getTerminateRequested() && requests.hasMoreElements()) {
	    // Loop through the input requests to see what we can match.
	    request = (TimeRelativeRequest) requests.nextElement();
	    icount = ((Integer) iRequests.get(request));

	    /*
	    System.err.println(request + "\n->");
	    */

	    // convert timeRelative request into regular request
	    try {

// JPW debug print
//System.err.println("\n\nStreamTimeRelList.process(): request:\n" + request);

		regRequest = ((RBO) getSource()).extractTimeRelative
		    (request,
		     getNBO().getRequestOptions());

// JPW debug print
//System.err.println("StreamTimeRelList.process(): regRequest:\n" + regRequest);

	    } catch (com.rbnb.utility.SortException e) {
		throw new java.lang.InternalError();
	    }

	    if (regRequest == null) {
		// If we don't get a request, then we've run off an end.
		switch (request.getRelationship()) {
		case TimeRelativeRequest.BEFORE:
		case TimeRelativeRequest.AT_OR_BEFORE:
		    // If we're backing up, then we ran off the beginning.
		    // There won't be any more data coming.
		    break;

		case TimeRelativeRequest.AFTER:
		case TimeRelativeRequest.AT_OR_AFTER:
		    // If we're going forward, then we ran off the end.  Try
		    // again if there are to be more repetitions.
		    if (oRequest.getNrepetitions() != 1) {
			trRequests.put(request,icount);
		    }
		    break;
		}

	    } else {
		// If we got a regular request, then add it to the full
		// request.
		if (fullRequest == null) {
		    fullRequest = regRequest;
		} else {
		    fullRequest = fullRequest.mergeWith(regRequest);
		}

		// Add the request to the list of those being processed.
		workRequests.put(request,icount);
	    }
	}

	if (!getTerminateRequested() && (fullRequest != null)) {
	    // If we got a request to actually try, then do so now.
	    requests = workRequests.keys();

// System.err.println("StreamTimeRelList.process(): fullRequest:\n" + fullRequest);

	    match = ((Rmap) getSource()).extractRmap(fullRequest,true);

// System.err.println("StreamTimeRelList.process(): match:\n" + match);

	    if (match == null) {
		// If we don't get a request, then we've run off the
		// beginning.
		while (!getTerminateRequested() &&
		       (requests.hasMoreElements())) {
		    request = (TimeRelativeRequest) requests.nextElement();

		    switch (request.getRelationship()) {
		    case TimeRelativeRequest.BEFORE:
		    case TimeRelativeRequest.AT_OR_BEFORE:
			// If we're backing up, then we're done with this
			// particular request.  That's OK.
			break;

		    case TimeRelativeRequest.AFTER:
		    case TimeRelativeRequest.AT_OR_AFTER:
			// If we're going forward, then we need to abort
			// everything as we've fallen too far behind.
			trRequests.clear();
			reasonR = EndOfStream.REASON_BOD;
			post(new EndOfStream(reasonR));
			break;
		    }
		}

	    } else {
		// If we got a match, then post it and see what to do next.
		gotSomething = true;
		while (!getTerminateRequested() &&
		       (requests.hasMoreElements())) {
		    request = (TimeRelativeRequest) requests.nextElement();
		    icount = (Integer) workRequests.get(request);
		    count = icount.intValue();
		    if ((oRequest.getNrepetitions() == DataRequest.INFINITE) ||
			(++count < oRequest.getNrepetitions())) {
			// If we still have more to do on this request, then
			// determine where the next reference will be.
			updateReference(request,count,match);
		    }
		}

		if (!getTerminateRequested()) {
		    if (trRequests.isEmpty()) {
			// If there are no more requests, then we're done.
			eos = new EndOfStream();
			eos.addChild(match);
			match = eos;
		    }

		    /*
		    System.err.println(this + " posting " + match);
		    */

		    post(match);
		}
	    }
	}

	if (!getTerminateRequested()) {
	    if (!gotSomething) {
		// If we didn't get anything, then we'll need to wait next
		// time.
		setNeedWait(true);

	    } else {
		// If we got something, we don't wait one time.
		setNeedWait(false);
		haveNewRBO = false;
	    }
	}

	/*
	System.err.println(this + " done.\n");
	*/

	return (reasonR);
    }

    /**
     * Runs this <code>StreamTimeRelativeListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.2
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/06/2003  INB	Created.
     *
     */
    public final void run() {
	getDoor().setIdentification(toString());
	requestStarted = System.currentTimeMillis();

	try {
	    if ((getSource() instanceof ClientHandler) &&
		!((ClientHandler) getSource()).allowAccess(getNBO())) {
		post(new EndOfStream(EndOfStream.REASON_EOD));
		setEOS(true);

	    } else {
		byte reason = EndOfStream.REASON_END;

		while (!getTerminateRequested() && (trRequests.size() > 0)) {
		    // So long as there is something to do, we need to keep
		    // processing.
		    reason = process();

		    if (getThread() != null) {
			((ThreadWithLocks) getThread()).ensureLocksCleared
			    (toString(),
			     "StreamTimeRelativeListener.run(1)",
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
		     "StreamTimeRelativeListener.run(2)",
		     getNBO().getLog(),
		     getNBO().getLogLevel(),
		     getNBO().getLogClass());
	    }
	}

	if (!getTerminateRequested()) {
	    // If this thread was not terminated on a request from another
	    // thread, then go through the termination work here.
	    try {
		stop();
	    } catch (java.lang.Exception e) {
	    }
	}

	// Notify any waiting thread.
	setTerminateRequested(false);
	setThread(null);
    }

    /**
     * Sets the need wait flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needWaitI  do we need to wait?
     * @see #getNeedWait()
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    final void setNeedWait(boolean needWaitI) {
	needWait = needWaitI;
    }

    /**
     * Update the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request to work on.
     * @param countI   the current count.
     * @param matchI   the <code>Rmap</code> matched.
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
     * @version 11/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    private final void updateReference(TimeRelativeRequest requestI,
				       int countI,
				       Rmap matchI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	double reference = requestI.determineNextReference(matchI);
	Integer icount = new Integer(countI);
	TimeRelativeRequest nRequest;
	TimeRelativeChannel trc;

	if (!Double.isNaN(reference)) {
	    // If we got a new reference, then update the request and put it
	    // into the output list.
	    requestI.setTimeRange
		(new TimeRange
		    (reference,
		     requestI.getTimeRange().getDuration()));
	    requestI.setNameOffset(0);
	    trRequests.put(requestI,icount);
							       
	} else {
	    // No single reference could be found, so split the request into
	    // multiples for next time.
	    for (int idx = 0; idx <
		     requestI.getByChannel().size();
		 ++idx) {
		trc = (TimeRelativeChannel)
		    requestI.getByChannel().elementAt(idx);
		nRequest = new TimeRelativeRequest();
		requestI.setNameOffset(0);
		nRequest.setRelationship(requestI.getRelationship());
		nRequest.setTimeRange(requestI.getTimeRange());
		try {
		    nRequest.addChannel(trc);
		} catch (com.rbnb.utility.SortException e) {
		    throw new java.lang.InternalError();
		}
		reference = nRequest.determineNextReference(matchI);

		if (Double.isNaN(reference)) {
		    // If there is no reference, we either leave this channel
		    // alone or eliminate it.
		    if (countI != 0) {
			icount = new Integer(countI - 1);
		    } else {
			continue;
		    }

		} else {
		    // Update the reference for the channel.
		    nRequest.setTimeRange
			(new TimeRange
			    (reference,
			     requestI.getTimeRange().getDuration()));
		}

		trRequests.put(nRequest,icount);
	    }
	}
    }

    /**
     * Waits up to <code>maxWait</code> seconds for the desired data time to
     * be available.
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
     * @since V2.2
     * @version 12/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/10/2003  INB	Created from
     *			<code>StreamRBOListener.findAllRequest</code>.
     *
     */
    final void waitForData()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String[] channels = getOriginal().extractNames();
	DataRequest lOriginal = (DataRequest) getOriginal();
	Rmap entry;
	long now,
	    maxWait = getNBO().getRequestOptions().getMaxWait();
	boolean foundR = (channels.length == 0);

	while (!foundR) {
	    // Locate the requested time span.  For now, we assume a single
	    // time span, which can be found in the hierarchy by simply moving
	    // down the first child.
	    for (entry = getOriginal();
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
		    switch (lOriginal.getRelationship()) {
		    case DataRequest.LESS:
			foundR = (eLimits[1] >= limits[0]);
			break;

		    case DataRequest.LESS_EQUAL:
			if ((getNBO().getRequestOptions() == null) ||
			    !getNBO().getRequestOptions().getExtendStart()) {
			    foundR = (eLimits[1] >= limits[0]);
			} else {
			    foundR = (eLimits[1] >= limits[1]);
			}
			break;

		    case DataRequest.GREATER_EQUAL:
		    case DataRequest.GREATER:
			foundR = (eLimits[1] >= limits[1]);
			break;
		    }
		}
	    }

	    if (!foundR) {
		now = System.currentTimeMillis();
		if ((now - requestStarted) > maxWait) {
		    break;
		}
		setNeedWait(true);
		waitToPickup();
		setNeedWait(false);
	    }
	}

	return;
    }

    /**
     * Waits for something to arrive.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    private final void waitToPickup()
	throws java.lang.InterruptedException
    {
	if (getNeedWait()) {
	    if (getToPickup() > 0) {
		setToPickup(getToPickup() - 1);

	    } else {
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
