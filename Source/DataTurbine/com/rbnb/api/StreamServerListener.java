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
 * Extended <code>StreamListener</code> that listens for
 * <code>ServerInterface</code> "events" for a
 * <code>StreamRequestHandler</code>.
 * <p>
 * Server "events" are the addition or removal of <code>Server</code>
 * objects. If an event matches an entry in the request, then a listener of the
 * appropriate type for the event <code>Rmap</code> is created.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 02/23/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
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
 *			The <code>run</code> method is no longer final.
 * 05/15/2003  INB	When building listener, check for no children of the
 *			match.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT, LONG_WAIT</code>.
 * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
 * 03/27/2001  INB	Created.
 *
 */
abstract class StreamServerListener
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
     * Class constructor to build a <code>StreamServerListener</code> for the
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
    StreamServerListener(StreamParent parentI,
			 Rmap requestI,
			 NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
    }

    /**
     * Class constructor to build a <code>StreamServerListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>ServerInterface</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>Server</code> source.
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
    StreamServerListener(StreamParent parentI,
			 Rmap requestI,
			 Server sourceI)
	throws java.lang.InterruptedException
    {
	this(parentI,requestI,(NotificationFrom) sourceI);
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
     * 05/15/2003  INB	Handle no children in match.
     * 01/16/2002  INB	Created.
     *
     */
    StreamListener buildListener(Serializable eventI,Rmap matchI)
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
	    /* 09/16/2002 - too chatty.
	    throw new java.lang.IllegalStateException
		("Cannot perform request of " +
		 cEvent.getFullName() +
		 ": no channels were specified.");
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
	    if (matchI.getNchildren() > 0) {
		match.setChildren
		    ((RmapVector) matchI.getChildren().clone());
	    }
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
		listenerR = create(this,match,sInterface);
	    }

	} else if (cEvent instanceof ShortcutHandler) {
	    ShortcutHandler scHandler = (ShortcutHandler) cEvent;
	    if (scHandler.getActive() != Shortcut.PASSIVE) {
		listenerR = new StreamShortcutListener(this,
						       match,
						       scHandler);
	    }
	}

	return (listenerR);
    }

    /**
     * Creates the appropriate type of <code>StreamServerListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>ServerInterface</code> source.
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
     * 12/03/2001  INB	Created.
     *
     */
    final static StreamServerListener create(StreamParent parentI,
					     Rmap requestI,
					     ServerInterface sourceI)
	throws java.lang.InterruptedException
    {
	StreamServerListener listenerR = null;

	if (sourceI instanceof ServerHandler) {
	    listenerR = new StreamRBNBListener(parentI,
					       requestI,
					       (ServerHandler) sourceI);
	} else {
	    listenerR = new StreamRemoteListener(parentI,
						 requestI,
						 (RemoteServer) sourceI);
	}

	return (listenerR);
    }

    /**
     * Posts a response <code>Rmap</code> to the application.
     * <p>
     * If the base request indicates that this is a <code>CONSOLIDATED</code>
     * request, then this method keeps track of which of the <code>RBOs</code>
     * have responded. Once all of them have responded, the method passes the
     * consolidated response to its parent and kicks all of its children.
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
     * @version 12/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/24/2003  INB	Synchronize on the children list so that it doesn't
     *			get modified while we're doing notifications.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 10/30/2001  INB	Created.
     *
     */
    public void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
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
	    super.post(serializable);

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
			super.post(response);
		    }
		}
	    }
	}
    }

    /**
     * Runs this <code>StreamServerListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/03/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/03/2004  INB	Any time we have no children and we have a request
     *			for existing data, we need to abort.
     * 01/15/2004  INB	Break out of loop on EOS.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.  Added identification to
     *			<code>Door</code>.
     * 05/23/2003  INB	No longer final.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 06/11/2001  INB	Created.
     *
     */
    public void run() {
	getDoor().setIdentification(toString());

	try {
	    DataRequest myBase =
		((getBaseRequest() instanceof DataRequest) ?
		 ((DataRequest) getBaseRequest()) :
		 null);
	    boolean existing = ((myBase == null) ||
				(myBase.getDomain() == DataRequest.EXISTING));

	    while (!getTerminateRequested() && !getEOS()) {
		if (!isAlive(false)) {
		    // If the are no running child
		    // <code>StreamListeners</code>, then this stream may be
		    // done.
		    if (getEOS()) {
			break;

		    } else if (getAdded() == getRemoved()) {
			if (existing) {
			    post(new EndOfStream());
			    break;
			}
		    }
		}

		synchronized (this) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}
	    }

	} catch (java.lang.InterruptedException e) {
	} catch (java.lang.Exception e) {
	    setEOS(true);

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
     * Starts this <code>StreamServerListener</code> running.
     * <p>
     * 
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
     * @see #stop()
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Ensure that we clear the start up stage.
     * 02/06/2001  INB	Created.
     *
     */
    void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    setStartupStage(getStartupStage() + 1);
	    super.start();

	    if (((Rmap) getSource()).getParent() instanceof Server) {
		AwaitNotification aNotification = null;

		if (getAnotification() == null) {
		    setTerminateRequested(true);
		    return;
		}

		synchronized (getAnotification()) {
		    if (getAnotification().size() == 0) {
			setTerminateRequested(true);
			return;
		    }

		    aNotification = (AwaitNotification)
			getAnotification().firstElement();
		}

		aNotification.addEvent(((Rmap) getSource()).getParent(),true);
	    }

	} finally {
	    setStartupStage(getStartupStage() - 1);
	}
    }
}
