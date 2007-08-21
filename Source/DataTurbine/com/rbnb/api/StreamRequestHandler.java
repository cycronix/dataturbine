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
 * Streaming request handler.
 * <p>
 * This class is used by <code>NBOs</code> to perform requests for data.  While
 * it was originally designed for streaming requests, it became apparent that
 * request/response requests could be handled as a special case of streaming
 * that terminates after a single answer.
 * <p>
 * A description of how <code>StreamRequestHandlers</code> are used to retrieve
 * data can be found in <code>NBO.initiateRequestAt</code>, while a description
 * of how listeners work can be found in <code>StreamListener</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.NBO#initiateRequestAt(int indexI)
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/05/2004  INB	Updated documentation.
 * 05/26/2004  INB	Added <code>stop</code> call to end of
 *			<code>run</code>.
 * 02/23/2004  INB	Optionally log new listeners.
 * 02/03/2004  INB	Any time we have no children and we have a request
 *			for existing data, we need to abort.
 * 12/24/2003  INB	Ensure that children don't go away while we're posting
 *			notifications to them.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.  Added identification to
 *			<code>Door</code>.
 * 05/23/2003  INB	Ensure that we clear the start up stage count.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT, LONG_WAIT</code>.
 * 03/24/2003  INB	To ensure that we don't run afoul of a timing bug, make
 *			sure that our <code>NBO</code> still thinks we're
 *			active before posting.
 * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
 * 03/19/2001  INB	Created.
 *
 */
final class StreamRequestHandler
    extends com.rbnb.api.StreamListener
{
    /**
     * the consolidated response.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/30/2001
     */
    private Rmap consolidated = null;

    /**
     * the number of responses to expect.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private int toExpect = -1;

    /**
     * list of children that have responded.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/30/2001
     */
    private java.util.Vector responded = new java.util.Vector();

    /**
     * Class constructor to build a <code>StreamRequestHandler</code> for
     * the specified <code>NBO</code> to work on the specified request
     * <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nboI	the <code>NBO</code>.
     * @param requestI  the request <code>DataRequest</code>.
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
     * 03/19/2001  INB	Created.
     *
     */
    StreamRequestHandler(NBO nboI,DataRequest requestI)
	throws java.lang.InterruptedException
    {
	super();
	setParent(nboI);
	setRequest(requestI);
    }

//EMF 9/19/05
public String toString() {
return new String("StreamRequestHandler: "+super.toString());
}

    /**
     * Builds a new <code>StreamListener</code> for an event and a match.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the "event" <code>Serializable</code>.
     * @param matchI the matched request <code>Rmap</code>.
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
     * @version 02/23/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2004  INB	Optionally log new listeners.
     * 01/16/2002  INB	Created.
     *
     */
    final StreamListener buildListener(Serializable eventI,Rmap matchI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	StreamListener listenerR = null;
	DataRequest match;
	Rmap cEvent = (Rmap) eventI;
	if ((matchI.compareNames("...") != 0) &&
	    (matchI.getNchildren() == 0)) {
	    /* INB 09/16/2002 - too chatty.
	    throw new java.lang.IllegalStateException
		("Cannot perform request of " +
		 cEvent.getFullName() +
		 ": no channels were specified in\n" +
		 matchI);
	    */
	    if (getNBO().getLog() != null) {
		getNBO().getLog().addMessage
		    (getNBO().getLogLevel(),
		     getNBO().getLogClass(),
		     toString(),
		     "Cannot perform request of " +
		     cEvent.getFullName() +
		     ": no channels were specified in\n" +
		     matchI);
	    }
	    return (null);
	}

	if (matchI instanceof DataRequest) {
	    match = (DataRequest) matchI;
	} else {
	    match = new DataRequest();
	    match.setName(matchI.getName());
	    match.setTrange(matchI.getTrange());
	    match.setFrange(matchI.getFrange());
	    match.setDblock(matchI.getDblock());
	    match.setChildren
		((RmapVector) matchI.getChildren().clone());
	}

	if (cEvent instanceof ServerInterface) {
	    ServerInterface sInterface = (ServerInterface) cEvent;
	    boolean isOK = true;
	    if (sInterface instanceof PeerServer) {
		PeerServer peer = (PeerServer) sInterface;
		LocalPath path = (LocalPath) peer.findPath();
		if ((path == null) ||
		    (path.getCost() >=
		     ShortcutHandler.PASSIVE_COST)) {
		    isOK = false;
		}
	    }
	    if (isOK) {
		listenerR = StreamServerListener.create(this,
							match,
							sInterface);
	    }
	}

	return (listenerR);
    }

    /**
     * Gets the "base" request.
     * <p>
     * The "base" request is defined as the request at the top of the
     * <code>StreamListener</code> chain. This implementation returns the
     * <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the "base" <code>DataRequest</code>.
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    final Rmap getBaseRequest() {
	return (getRequest());
    }

    /**
     * Matches the request against the <code>RoutingMapHandler</code>.
     * <p>
     * At this level, we MUST start a listener for every piece of the request
     * to be handled. The reason for this is that with routing, it is possible
     * that nothing will ever match locally, in which case we want to try to
     * pass that part of the request up to our parent.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void matchRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getRequest().compareNames("...") == 0) {
	    // This is not a practical request, so we just throw an exception.
	    throw new java.lang.IllegalArgumentException
		("Making a request for every channel in the world is not " +
		 "allowed.");
	}

	matchRequest(getRequest());
    }

    /**
     * Matches a request against the <code>RoutingMapHandler</code>.
     * <p>
     * At this level, we MUST start a listener for every piece of the request
     * to be handled. The reason for this is that with routing, it is possible
     * that nothing will ever match locally, in which case we want to try to
     * pass that part of the request up to our parent.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request to be matched.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void matchRequest(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RoutingMapHandler rHand = (RoutingMapHandler) getSource();

	if (requestI.getName() == null) {
	    // When there is no name to this part of the request, then we need
	    // to move down a level in the request hierarchy.
	    for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
		matchRequest(requestI.getChildAt(idx));
	    }

	} else if (requestI.compareNames("*") == 0) {
	    RmapVector rVector =
		((RmapVector) ((Rmap) rHand).getChildren()).copyVector();
	    Rmap event,
		 request;
	    for (int idx = 0; idx < rVector.size(); ++idx) {
		event  = (Rmap) rVector.elementAt(idx);
		request = (Rmap) requestI.clone();
		request.setName(event.getName());
		accept(event,request);
	    }

	} else {
	    // Otherwise, we need to try to find a child of the
	    // <code>RoutingMapHandler</code> that matches the request.
	    Rmap event = null;
	    if (requestI.compareNames(".") == 0) {
		event = (Rmap) rHand.getLocalServerHandler();
	    } else {
		event = rHand.findDescendant(requestI.getFullName(),false);
	    }

	    if (event == null) {
		// If there is no such child, then we need to assume that it
		// represents a <code>RemoteServer</code>.
		if (rHand.getLocalServerHandler().getParent() != null) {
		    event = rHand.getLocalServerHandler().getParent();
		} else {
		    return;
		}
	    }

	    // Set up the listener for the object.
	    accept(event,requestI);
	}
    }

    /**
     * Posts a response <code>Serializable</code> to the application.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Serializable</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 12/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/24/2003  INB	Synchronize on the children list so that it doesn't
     *			get modified while we're doing notifications.
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/24/2003  INB	To ensure that we don't run afoul of a timing bug, make
     *			sure that our <code>NBO</code> still thinks we're
     *			active.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 03/29/2001  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//System.err.println("StreamRequestHandler.post called");
	if ((getNBO().getSRH() != this) ||
	    getTerminateRequested() ||
	    getEOS()) {
	    // If the <code>NBO</code> thinks we're gone, then ignore this
	    // post.
	    return;
	}

	Rmap workOn;
	RSVP rsvp = null;
	Serializable serializable = serializableI;
	if (serializableI instanceof RSVP) {
	    rsvp = (RSVP) serializableI;
	    workOn = (Rmap) rsvp.getSerializable();
	} else {
	    workOn = (Rmap) serializableI;
	}

	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	if ((myBase != null) &&
	    (myBase.getMode() != DataRequest.CONSOLIDATED)) {
	    // For <code>FRAMES</code> requests, just use our superclass's
	    // <code>post</code> method.
	    if (serializableI instanceof EndOfStream) {
		setEOS(true);
	    }
	    ((NBO) getParent()).asynchronousResponse(serializableI);

	} else {
	    // For <code>CONSOLIDATED</code> responses, merge the response with
	    // those from the other <code>RBO</code> listeners and post only
	    // when we have a response from everyone.
	    Rmap response = (Rmap) serializable;

	    if (response != null) {
		// If there was a real response, merge into the consolidated
		// set.
		if (consolidated == null) {
		    consolidated = response;
		} else {
		    Rmap temp,
			child;
		    if (consolidated instanceof EndOfStream) {
			if (!(response instanceof EndOfStream)) {
			    temp = consolidated;
			    consolidated = new Rmap();
			    consolidated.setTrange(temp.getTrange());
			    consolidated.setFrange(temp.getFrange());
			    while (temp.getNchildren() > 0) {
				child = temp.getChildAt(0);
				temp.removeChildAt(0);
				consolidated.addChild(child);
			    }
			}
		    } else {
			if (response instanceof EndOfStream) {
			    temp = response;
			    response = new Rmap();
			    response.setTrange(temp.getTrange());
			    response.setFrange(temp.getFrange());
			    while (temp.getNchildren() > 0) {
				child = temp.getChildAt(0);
				temp.removeChildAt(0);
				response.addChild(child);
			    }
			}
		    }
		    consolidated.mergeWith(response);
		}
	    }

	    synchronized (this) {
		while (getStartupStage() > 0) {
		    wait(TimerPeriod.LONG_WAIT);
		}
	    }

	    synchronized (responded) {
		responded.addElement(Thread.currentThread());
		if (toExpect == -1) {
		    toExpect = getAdded();
		}

		if (responded.size() >= toExpect) {
		    // If everyone has responded, then we need to post the
		    // response and start everyone on the next response.
		    toExpect = getAdded() - getRemoved();
		    response = consolidated;
		    consolidated = null;
		    responded = new java.util.Vector();
		    synchronized (getChildren()) {
			for (int idx = 0; idx < getChildren().size(); ++idx) {
			    ((StreamListener)
			     getChildren().elementAt(idx)).accept(null,null);
			}
		    }

		    if (response != null) {
			if (response instanceof EndOfStream) {
			    setEOS(true);
			}
			((NBO) getParent()).asynchronousResponse(response);
		    }
		}
	    }
	}
    }

    /**
     * Runs this <code>StreamRequestHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #stop()
     * @since V2.0
     * @version 05/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Added <code>stop</code> call to end of
     *			<code>run</code>.
     * 02/03/2004  INB	Any time we have no children and we have a request
     *			for existing data, we need to abort.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.  Added identification to
     *			<code>Door</code>.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/19/2001  INB	Created.
     *
     */
    public final void run() {
	getDoor().setIdentification(toString());
	try {
	    // Initiate the request.
	    long count = 1;
	    DataRequest myBase =
		((getBaseRequest() instanceof DataRequest) ?
		 ((DataRequest) getBaseRequest()) :
		 null);
	    boolean existing = ((myBase == null) ||
				(myBase.getDomain() == DataRequest.EXISTING));

	    while ((count != 0) && !getTerminateRequested()) {
		if (!isAlive(false)) {
		    if (getEOS()) {
			break;

		    } else if (getAdded() == getRemoved()) {
			if (existing) {
			    post(new EndOfStream());
			    break;
			}
		    }
		}

		// Loop until we reach an end of stream condition or we are
		// terminated on by a request from the <code>NBO</code>.
		synchronized (this) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}
	    }

	} catch (java.lang.InterruptedException e) {

	} catch (java.lang.Exception e) {
	    setEOS(true);
	    try {
		((NBO) getParent()).asynchronousException(e);
	    } catch (java.lang.Exception e1) {
	    }

	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
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
     * Starts this <code>StreamRequestHandler</code> running.
     * <p>
     * 
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #run()
     * @see #stop()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 05/23/2003  INB	Ensure that we clear the start up stage count.
     * 03/27/2001  INB	Created.
     *
     */
    final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    setStartupStage(getStartupStage() + 1);
	    if ((getRequest().compareNames("...") != 0) &&
		(getRequest().getNchildren() == 0)) {
		throw new java.lang.IllegalStateException
		    ("Cannot perform request: no channels were specified " +
		     "in\n" + getRequest());
	    }

	    Rmap rHandler = null;
	    for (rHandler = getNBO().getParent();
		 !(rHandler instanceof RoutingMapHandler);
		 rHandler = rHandler.getParent()) {
	    }
	    setSource((NotificationFrom) rHandler);
	    super.start();

	    matchRequest();

	} finally {
	    setStartupStage(getStartupStage() - 1);
	}

	setThread(new ThreadWithLocks(this,
				      "_SRH." +
				      ((NBO) getParent()).getName()));
	getThread().start();
    }
}
