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
 * Extended <code>StreamServerListener</code> that listens for
 * <code>ServerHandler</code> "events" for a
 * <code>StreamRequestHandler</code>.
 * <p>
 * <code>ServerHandler</code> "events" are the addition or removal of
 * <code>ClientHandler</code> objects and <code>Server</code> objects. If an
 * event matches an entry in the request, then a listener of the appropriate
 * type for the event <code>Rmap</code> is created.
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
 * 11/19/2003  INB	0 duration requests for at-or-before are treated as at
 *			for V2.1 compatibility mode.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 11/11/2003  INB	Create <code>StreamTimeRelativeListeners</code> for
 *			relationships other than <code>EQUAL</code>.
 * 05/23/2003  INB	Ensure that we clear the start up stage count.
 * 03/27/2001  INB	Created.
 *
 */
final class StreamRBNBListener
    extends com.rbnb.api.StreamServerListener
{

    /**
     * Class constructor to build a <code>StreamRBNBListener</code> for the
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
    StreamRBNBListener(StreamParent parentI,
		       Rmap requestI,
		       NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
    }

    /**
     * Class constructor to build a <code>StreamRBNBListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>ServerHandler</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>ServerHandler</code> source.
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
    StreamRBNBListener(StreamParent parentI,
			 Rmap requestI,
			 ServerHandler sourceI)
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
     * 11/19/2003  INB	0 duration requests for at-or-before are treated as at
     *			for V2.1 compatibility mode.
     * 11/06/2003  INB	Create <code>StreamTimeRelativeListeners</code> for
     *			relationships other than <code>EQUAL</code>.
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

	boolean isNewEvent = false;
	Serializable event = eventI;
	if (eventI instanceof NewObjectEvent) {
	    isNewEvent = true;
	    event = ((NewObjectEvent) eventI).getObject();
	}

	if (!(event instanceof ClientHandler)) {
	    listenerR = super.buildListener(eventI,matchI);

	} else {
	    Rmap cEvent = (Rmap) event;
	    if ((matchI.compareNames("...") != 0) &&
		(matchI.getNchildren() == 0)) {
		/* INB 09/16/2002 - too chatty.
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
		match.setDblock(matchI.getDblock());
		if (matchI.getChildren() != null) {
		    match.setChildren
			((RmapVector) matchI.getChildren().clone());
		}
	    }

	    if (event instanceof SourceHandler) {
		SourceHandler rbo = (SourceHandler) event;
		DataRequest br =
		    ((getBaseRequest() instanceof DataRequest) ?
		     (DataRequest) getBaseRequest() :
		     null);
		ServerHandler sh;
		Rmap entry = getNBO();
		while (!(entry instanceof ServerHandler)) {
		    if (entry instanceof GetServerHandlerInterface) {
			entry = (Rmap)
			    ((GetServerHandlerInterface)
			     entry).getLocalServerHandler();
		    } else {
			entry = entry.getParent();
		    }
		}
		sh = (ServerHandler) entry;

		if ((br == null) ||
		    (br.getRelationship() == DataRequest.EQUAL)) {
		    listenerR = new StreamRBOListener(this,
						      (DataRequest) match,
						      rbo,
						      isNewEvent);

		} else if
		    ((sh.getCompatibilityMode().equalsIgnoreCase
		      (RBNB.VERSION_V2_1)) &&
		     (br.getRelationship() == DataRequest.LESS_EQUAL) &&
		     (br.getChildAt(0).getTrange() != null) &&
		     (br.getChildAt(0).getTrange().getDuration() == 0.)) {
		    listenerR = new StreamRBOListener(this,
						      (DataRequest) match,
						      rbo,
						      isNewEvent);

		} else {
		    listenerR = new StreamTimeRelativeListener
			(this,
			 (DataRequest) match,
			 rbo,
			 isNewEvent);
		}

	    } else if (event instanceof PlugInHandler) {
		PlugInHandler piH = (PlugInHandler) event;
		listenerR = new StreamPlugInListener(this,
						     match,
						     piH);
	    }
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
     * @version 06/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getServer() != null) {
	    Rmap workOn;
	    RSVP rsvp = null;
	    Serializable serializable = serializableI;
	    if (serializableI instanceof RSVP) {
		rsvp = (RSVP) serializableI;
		workOn = (Rmap) rsvp.getSerializable();
	    } else {
		workOn = (Rmap) serializableI;
	    }

	    if (!(workOn instanceof RoutingMapInterface)) {
		Rmap work = ((Rmap) getServer()).newInstance(),
		    bottom = work,
		    top = work,
		    input = (Rmap) getServer();

		if (getRequest().compareNames(".") == 0) {
		    work.setName(".");
		} else {
		    while ((input = input.getParent()) != null) {
			work = input.newInstance();
			work.addChild(top);
			top = work;
		    }
		}

		if (!(workOn instanceof EndOfStream)) {
		    bottom.addChild(workOn);

		    if (rsvp != null) {
			rsvp.setSerializable(top);
		    } else {
			serializable = top;
		    }

		} else {
		    boolean added = false;
		    for (int idx = 0; idx < workOn.getNchildren(); ++idx) {
			work = workOn.getChildAt(idx);

			workOn.removeChild(work);
			bottom.addChild(work);
			--idx;
			added = true;
		    }
		    if (added) {
			workOn.addChild(top);
		    }
		}
	    }

	    super.post(serializable);
	}
    }

    /**
     * Starts this <code>StreamRBNBListener</code> running.
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
     * 04/02/2001  INB	Created.
     *
     */
    final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    setStartupStage(getStartupStage() + 1);
	    super.start();

	    RmapVector sourceList =
		((Rmap) getSource()).getChildren().copyVector();
	    DataRequest base =
		((getBaseRequest() instanceof DataRequest) ?
		 ((DataRequest) getBaseRequest()) :
		 null);
	    boolean waitForChildren =
		(base == null) ||
		(base.getMode() == DataRequest.CONSOLIDATED);
	    for (int idx = 0;
		 idx < sourceList.size();
		 ++idx) {
		// Add all of the children of the "source" (server) as
		// events. This matches existing <code>ClientHandlers</code>
		// and <code>Servers</code> against the request.
		Rmap child = (Rmap) sourceList.elementAt(idx);
		Serializable event = child;
		AwaitNotification aNotification = null;

		if ((child instanceof ClientHandler) &&
		    ((ClientHandler) child).isNew()) {
		    event = new NewObjectEvent(child);
		}
		if (getAnotification() == null) {
		    setTerminateRequested(true);
		    break;
		} else {
		    synchronized (getAnotification()) {
			if (getAnotification().size() == 0) {
			    setTerminateRequested(true);
			    break;
			} else {
			    aNotification = (AwaitNotification)
				getAnotification().firstElement();
			}
		    }

		    aNotification.addEvent(event,waitForChildren);
		}
	    }

	} finally {
	    setStartupStage(getStartupStage() - 1);
	}
	setThread(new ThreadWithLocks(this,
				      ("_SRL." +
				       getNBO().getName() +
				       "." +
				       ((Rmap) getSource()).getName())));
	getThread().start();
    }
}
