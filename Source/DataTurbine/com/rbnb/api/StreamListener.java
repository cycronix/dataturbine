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
 * Listens for "events" for a <code>StreamParent</code>.
 * <p>
 * This abstract class forms the basis for all listeners used to handle
 * data requests.  The class was originally designed to handle just streaming
 * requests (hence the name).  However, "request/response" type requests can be
 * (and are) treated as a simple extreme case of a streaming request wherein a
 * single answer containing the combined results from all of the matched data
 * sources is returned to the caller.
 * <p>
 * All <code>StreamListeners</code> "listen" for "events" from a source
 * object.  What an event is depends on the type of source object.
 * Additionally, some <code>StreamListeners</code> may attempt to directly get
 * data from their source (for things such as <code>RBOs</code>) or may
 * propagate their request to their source (for sources that represent other
 * servers).
 * <p>
 * The objects, the events, and the listeners for handling those events are:
 * <p>
 * <table alignment="center" valign="top" cellpadding=5>
 *   <thead align="center" valign="center">
 *     <tr>
 *	 <th>StreamListener</th>
 *	 <th>Source</th>
 *	 <th>Event(s)</th>
 *     </tr>
 *   </thead>
 *   <tbody align="left" valign="center">
 *     <tr>
 *	 <td><code>StreamRequestHandler</code></td>
 *	 <td><code>RBNBRoutingMap</code></td>
 *       <td>A <code>RemoteServer</code>, a <code>ParentServer</code>, a
 *	     <code>PeerServer</code>, or an <code>RBNB</code> has been added or
 *	     removed.</td>
 *     </tr>
 *     <tr>
 *	 <td><code>StreamRemoteListener</code></td>
 *	 <td><code>RemoteServer</code>, <code>ParentServer</code>,
 *	     <code>PeerServer</code>, or <code>ChildServer</code></td>
 *       <td>A <code>RemoteServer</code>, <code>ParentServer</code>,
 *	     <code>PeerServer</code>, or an <code>RBNB</code> has been added or
 *	     removed.</td>
 *     </tr>
 *     <tr>
 *	 <td><code>StreamRBNBListener</code></td>
 *	 <td><code>RBNB</code></td>
 *	 <td>A <code>ChildServer</code>, a <code>RBNBShortcut</code>, or a
 *	     subclass of <code>RBNBClient</code> such as an <code>RBO</code> or
 *	     <code>RBNBPlugIn</code> has been added or removed.</td>
 *     </tr>
 *     <tr>
 *	 <td><code>StreamShortcutListener</code></td>
 *	 <td><code>RBNBShortcut</code></td>
 *	 <td>No events are generated.</td>
 *     </tr>
 *     <tr>
 *	 <td><code>StreamRBOListener</code>
 *	 <td><code>RBO</code></td>
 *	 <td>A frame has been added to the <code>Rmap</code>.</td>
 *     </tr>
 *     <tr>
 *	 <td><code>StreamPlugInListener</code>
 *	 <td><code>RBNBPlugIn</code></td>
 *	 <td>No events are generated.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * <p>
 * To listen for events on the source object, the <code>StreamListener</code>
 * creates an <code>AwaitNotification</code> object that ties the source and
 * the listener together.  The <code>AwaitNotification</code> object handles
 * moving the notification of the event from the source object to the
 * listener as follows:
 * <p><ol>
 *    <li>When one of the specified events occurs for the source object, the
 *	  thread generating that event calls the source object's
 *	  <code>post</code> method, which adds the event to the proper (or all)
 *	  <code>AwaitNotification</code> objects tied to that source object
 *	  (<code>AwaitNotification.addEvent</code>),</li>
 *    <li>The thread running for the <code>AwaitNotification</code> object
 *	  picks up the notification of the event (which is usually the object
 *	  that caused the event - <code>AwaitNotification.run</code>),</li>
 *    <li>The <code>AwaitNotification</code> thread passes the event object to
 *	  the listener (<code>StreamListener.accept</code>),</li>
 *    <li>For most listeners (aside from actual sources of data), the event
 *	  object is compared to the request being handled by the listener, and,
 *	  if it matches, a new child <code>StreamListener</code> is created or
 *	  an existing one is deleted.</li>
 * </ol><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.StreamParent
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
 * 08/05/2004  INB	Added documentation.
 * 04/07/2004  INB	Replaced boolean <code>waiting</code> with
 *			<code>int</code>.  Old code left commented out.
 * 04/06/2004  INB	Separate wait loop in <code>waitToPickup</code> until
 *			two parts, one to wait for an acknowledge and one to
 *			wait for data.
 *			Also, clear the count of messages to pickup if we
 *			waited for the acknowledge before we wait for data.
 * 02/13/2004  INB	Use <code>syncThreadObj</code> to ensure that
 *			operations that shouldn't be interrupted are not
 *			interrupted.
 *			Clear the <code>waiting</code> flag when stopping.
 * 02/12/2004  INB	Added <code>isStopping</code> and handling to ensure
 *			that <code>stop</code> operations are not interrupted.
 * 01/26/2004  INB	Added <code>syncThreadObj</code> and handling.
 * 01/15/2004  INB	Replaced synchronization in <code>stop</code> method
 *			to use <code>syncStopObj</code> to eliminate potential
 *			blocked forever problems.  Force an EndOfStream in the
 *			stop method.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.  Added identification to
 *			<code>Doors</code> and locations to <code>Locks</code>.
 * 10/11/2003  INB	Always perform notify in <code>setWaiting</code>.
 *			Ensure that we don't lose the relationship in
 *			requests.
 * 07/08/2003  INB	Use the multiple level of identification fields in the
 *			<code>RSVPs</code> and <code>Acknowledges</code>.
 * 06/16/2003  INB	Added version of <code>waitToPickup</code> that takes
 *			a timeout period.
 * 06/09/2003  INB	Allow for multiple listeners that match a particular
 *			entry, so long as their requests are different.
 * 05/23/2003  INB	Move the <code>child.stop()</code> call in
 *			<code>accept</code> outside of the locked section.
 *			Ensure that we clear the start up stage.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT, LONG_WAIT</code>.
 * 03/24/2003  INB	Don't post after a terminate.
 * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
 * 03/27/2001  INB	Created.
 *
 */
abstract class StreamListener
    implements com.rbnb.api.Interruptable,
	       com.rbnb.api.NotificationTo,
	       com.rbnb.api.StreamParent
{
    /**
     * the number of child <code>StreamListeners</code> added.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private int added = 0;

    /**
     * the list of <code>AwaitNotifications</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private java.util.Vector aNotification = new java.util.Vector();

    /**
     * list of child listeners.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private java.util.Vector children = new java.util.Vector();

    /**
     * <code>Door</code> to lock out multiple, simultaneous accept events.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/16/2002
     */
    private Door door = null;

    /**
     * have we seen an EOS go past.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/12/2002
     */
    private boolean eos = false;

    /**
     * the identification for this <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private int identification = 0;

    /**
     * are we stopping?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 02/12/2004
     */
    private boolean isStopping = false;

    /**
     * the base (original) request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/05/2001
     */
    private Rmap original = null;

    /**
     * the <code>StreamParent</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private StreamParent parent = null;

    /**
     * the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/05/2001
     */
    private Rmap request = null;

    /**
     * the number of child <code>StreamListeners</code> removed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private int removed = 0;

    /**
     * the <code>ServerHandler</code> hosting this operation.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private ServerHandler server = null;

    /**
     * the source to listen to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private NotificationFrom source = null;

    /**
     * the startup stage we're in.
     * <p>
     * Zero is out of setup.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/28/2002
     */
    private int startupStage = 0;

    /**
     * synchronizes <code>stop</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/15/2004
     */
	// 2005/08/25  WHF  Made this object an unnamed inner class which implements
	//  serializable but otherwise has no additional methods.
    final Object syncStopObj //= new Object();
								= new java.io.Serializable() {};

    /**
     * synchronizes <code>thread</code> methods.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/15/2004
     */
	// 2005/08/25  WHF  Made this object an unnamed inner class which implements
	//  serializable but otherwise has no additional methods.
    final Object syncThreadObj //= new Object();
								= new java.io.Serializable() {};
    /**
     * terminate executing?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2002
     */
    private boolean terminateExecuting = false;

    /**
     * terminate requested?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private boolean terminateRequested = false;

    /**
     * the running thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Thread thread = null;

    /**
     * is there information waiting to be picked up?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private long toPickup = 0;

    /**
     * are we waiting for an <code>Acknowledge</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/07/2004
     */
//>>> INB <<<
//    private boolean waiting = false;
    private int waiting = 0;

    /**
     * the working request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/05/2001
     */
    private Rmap working = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
    StreamListener()
	throws java.lang.InterruptedException
    {
	super();
	setDoor(new Door(Door.STANDARD));
    }

    /**
     * Class constructor to build a <code>StreamListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>NotificationFrom</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code>.
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
     * 03/27/2001  INB	Created.
     *
     */
    StreamListener(StreamParent parentI,
		   Rmap requestI,
		   NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	this();
	setParent(parentI);
	setRequest(requestI);
	setSource(sourceI);
    }

    /**
     * Accepts a notification event <code>Serializable</code>.
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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to <code>Locks</code>.
     * 06/09/2003  INB	Allow for multiple listeners that match a particular
     *			entry, so long as their requests are different.
     * 05/23/2003  INB	Move the <code>child.stop()</code> call outside of the
     *			locked section.
     * 03/27/2001  INB	Created.
     *
     */
    public void accept(Serializable eventI,Rmap matchI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	StreamListener newListener = null;
	java.util.Vector deleteChildren = null;
	boolean locked = false;

	try {
	    boolean isNewEvent = false;
	    Serializable event = eventI;
	    if (eventI instanceof NewObjectEvent) {
		isNewEvent = true;
		event = ((NewObjectEvent) eventI).getObject();
	    }

	    if ((event instanceof Rmap) &&
		(event instanceof NotificationFrom)) {
		Rmap cEvent = (Rmap) event;

		getDoor().lock("accept");
		locked = true;
		if (cEvent.getParent() == null) {
		    deleteChildren = new java.util.Vector();
		    for (int idx = 0; idx < getChildren().size(); ++idx) {
			StreamListener tChild =
			    (StreamListener) getChildren().elementAt(idx);

			if (tChild.getSource() == cEvent) {
			    deleteChildren.addElement(tChild);
			}
		    }

		} else {
		    for (int idx = 0; idx < getChildren().size(); ++idx) {
			StreamListener tChild =
			    (StreamListener) getChildren().elementAt(idx);

			if ((tChild.getSource() == cEvent) &&
			    (tChild.getRequest().compareTo(matchI) == 0)) {
			    return;
			}
		    }

		    newListener = buildListener(eventI,matchI);
		}
	    }
	} finally {
	    if (locked) {
		getDoor().unlock();
		locked = false;
	    }
	}

	if (newListener != null) {
	    newListener.start();
	}
	if (deleteChildren != null) {
	    StreamListener child;
	    for (int idx = 0; idx < deleteChildren.size(); ++idx) {
		child = (StreamListener) deleteChildren.elementAt(idx);
		child.stop();
	    }
	}
    }

    /**
     * Receives acknowledgement from the client.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acknowledgeI the <code>Acknowledge</code>.
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
     * @version 07/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/08/2003  INB	Use the multiple level of identification fields.
     * 06/07/2001  INB	Created.
     *
     */
    public final void acknowledge(Acknowledge acknowledgeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long currentLevel = acknowledgeI.popIdentification();

	if (currentLevel == 0) {
	    // The acknowledgement is for someone in the current chain.
	    if (getWaiting()) {
		// If the <code>Acknowledge</code> is meant for us, then kick
		// the primary thread.
		setWaiting(false);

	    } else {
		// If the <code>Acknowledge</code> is meant for a single child,
		// then pass it down.
		StreamListener child =
		    (StreamListener) getChildren().firstElement();
		child.acknowledge(acknowledgeI);
	    }

	} else {
	    // If the <code>Acknowledge</code> is meant for a descendent,
	    // determine who the descendent is and pass the acknowledge along.
	    long childIdentification = currentLevel;

	    long idx = Math.max(0,
			       Math.min(((childIdentification - 1) -
					 (added - getChildren().size())),
					getChildren().size() - 1));;
	    StreamListener target = null;
	    for (long lo = 0,
		     hi = getChildren().size() - 1;
		 (target == null) && (lo <= hi);
		 idx = (lo + hi)/2) {
		StreamListener child =
		    (StreamListener) getChildren().elementAt
		    ((int) idx);
		long difference = (child.getIdentification() -
				   childIdentification);

		if (difference == 0) {
		    target = child;

		} else if (difference < 0) {
		    lo = idx + 1;

		} else if (difference > 0) {
		    hi = idx - 1;
		}
	    }

	    if (target != null) {
		// If we located the target child, pass along the
		// <code>Acknowledge</code>.
		target.acknowledge(acknowledgeI);
	    }
	}
    }

    /**
     * Adds a <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param listenerI  the <code>StreamListener</code>.
     * @see #removeListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    public final void addListener(StreamListener listenerI) {
	synchronized (getChildren()) {
	    getChildren().addElement(listenerI);
	    setAdded(getAdded() + 1);
	    listenerI.setIdentification(getAdded());
	}

	/*
	if ((System.getProperty("LOGRBNBEVENTS").indexOf("Listeners") != -1) &&
	    (getNBO() != null) &&
	    (getNBO().getLog() != null)) {
	    try {
		getNBO().getLog().addMessage
		    (Log.STANDARD,
		     Log.CLASS_NBO,
		     toString(),
		     "Added new listener: " + listenerI);
	    } catch (Exception e) {
	    }
	}
	*/
    }

    /**
     * Adds an <code>AwaitNotification</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param anI  the <code>AwaitNotification</code> object.
     * @see #removeNotification(com.rbnb.api.AwaitNotification)
     * @since V2.0
     * @version 12/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    public void addNotification(AwaitNotification anI) {
	if (!getTerminateRequested()) {
	    aNotification.addElement(anI);
	}
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
     * @version 02/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
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
	return (null);
    }

    /**
     * Makes a copy of the request <code>Rmap</code>.
     * <p>
     * The copy contains no children or members.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the copied request <code>Rmap</code>.
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
     * @version 11/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/11/2003  INB	Copy the relationship in the request.
     * 04/03/2001  INB	Created.
     *
     */
    final Rmap copyRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap copyR = null;

	if (getRequest() instanceof DataRequest) {
	    DataRequest lRequest = (DataRequest) getRequest();
	    copyR = new DataRequest(lRequest.getName(),
				    lRequest.getTrange(),
				    lRequest.getFrange(),
				    lRequest.getReference(),
				    lRequest.getRelationship(),
				    lRequest.getDomain(),
				    lRequest.getNrepetitions(),
				    lRequest.getIncrement(),
				    lRequest.getSynchronized(),
				    lRequest.getMode(),
				    lRequest.getGapControl());
	} else {
	    copyR = new Rmap(getRequest().getName());
	    copyR.setTrange(getRequest().getTrange());
	    copyR.setFrange(getRequest().getFrange());
	}

	return (copyR);
    }

   /**
     * Gets the total number of children added.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of children added.
     * @see #setAdded(int)
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final int getAdded() {
	return (added);
    }

    /**
     * Gets the <code>AwaitNotification</code> object vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>AwaitNotification</code> object vector.
     * @since V2.0
     * @version 12/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final java.util.Vector getAnotification() {
	return (aNotification);
    }

    /**
     * Gets the "base" request.
     * <p>
     * The "base" request is defined as the request at the top of the
     * <code>StreamListener</code> chain. This implementation just calls up the
     * chain.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the "base" request <code>Rmap</code>.
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
    Rmap getBaseRequest() {
	return (((StreamListener) getParent()).getBaseRequest());
    }

    /**
     * Gets the list of child listeners.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the child listeners.
     * @see #addListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final java.util.Vector getChildren() {
	return (children);
    }

    /**
     * Gets the accept door.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @see #setDoor(com.rbnb.api.Door)
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2002  INB	Created.
     *
     */
    final Door getDoor() {
	return (door);
    }

    /**
     * Gets the EOS flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the EOS flag.
     * @see #setEOS(boolean)
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2001  INB	Created.
     *
     */
    final boolean getEOS() {
	return (eos);
    }

    /**
     * Gets the identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the identification.
     * @see #setIdentification(int)
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final int getIdentification() {
	return (identification);
    }

    /**
     * Gets the isStopping flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we stopping?
     * @see #setIsStopping(boolean)
     * @since V2.2
     * @version 02/12/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2004  INB	Created.
     *
     */
    final boolean getIsStopping() {
	return (isStopping);
    }

    /**
     * Gets the <code>NBO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>NBO</code>.
     * @since V2.0
     * @version 06/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    final NBO getNBO() {
	NBO nboR = null;

	if (getParent() instanceof NBO) {
	    nboR = (NBO) getParent();
	} else {
	    nboR = ((StreamListener) getParent()).getNBO();
	}

	return (nboR);
    }

    /**
     * Gets the original request.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the original request.
     * @see #setOriginal(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final Rmap getOriginal() {
	return (original);
    }

    /**
     * Gets our parent <code>StreamParent</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return our parent.
     * @see #setParent(com.rbnb.api.StreamParent)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final StreamParent getParent() {
	return (parent);
    }

    /**
     * Gets the request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the request <code>Rmap</code>.
     * @see #setRequest(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final Rmap getRequest() {
	return (request);
    }

   /**
     * Gets the total number of children removed.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of children removed.
     * @see #setRemoved(int)
     * @since V2.0
     * @version 02/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2002  INB	Created.
     *
     */
    final int getRemoved() {
	return (removed);
    }

    /**
     * Gets the <code>RoutingMapHandler</code> hosting this operation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RoutingMapHandler</code>.
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
     *
     */
    final RoutingMapHandler getRoutingMap() {
	return ((RoutingMapHandler) getServer().getParent());
    }

    /**
     * Gets the <code>ServerHandler</code> hosting this operation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    public final ServerHandler getServer() {
	if (getParent() != null) {
	    server = getParent().getServer();
	}

	return (server);
    }

    /**
     * Gets the <code>NotificationFrom</code> being listened to.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>NotificationFrom</code>.
     * @see #setSource(com.rbnb.api.NotificationFrom)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final NotificationFrom getSource() {
	return (source);
    }

    /**
     * Gets the startup stage.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the stage.
     * @see #setStartupStage(int)
     * @since V2.0
     * @version 01/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  INB	Created.
     *
     */
    final int getStartupStage() {
	return (startupStage);
    }

    /**
     * Gets the terminate executing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is a terminate being processed?
     * @see #setTerminateExecuting(boolean)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    final boolean getTerminateExecuting() {
	return (terminateExecuting);
    }

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  should the <code>StreamRBOListener</code> terminate?
     * @see #setTerminateRequested(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final boolean getTerminateRequested() {
	return (terminateRequested);
    }

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
     * @see #setThread(java.lang.Thread)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final Thread getThread() {
	return (thread);
    }

    /**
     * Gets the "to pickup" flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is there something to be picked up?
     * @see #setToPickup(long)
     * @since V2.0
     * @version 10/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    final long getToPickup() {
	return (toPickup);
    }

    /**
     * Gets the waiting flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we waiting for an <code>Acknowledge</code>?
     * @since V2.0
     * @version 04/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2004  INB	Replaced boolean <code>waiting</code> with
     *			<code>int</code>.  Old code left commented out.
     * 06/07/2001  INB	Created.
     *
     */
    final boolean getWaiting() {
//>>> INB <<<
//	return (waiting);
	return (waiting != 0);
    }

    /**
     * Gets the working request <code>Rmap</code>..
     * <p>
     *
     * @author Ian Brown
     *
     * @return the working request <code>Rmap</code>.
     * @see #setWorking(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final Rmap getWorking() {
	return (working);
    }

    /**
     * Interrupts this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.SecurityException
     *		  thrown if the interrupt is not allowed by the security
     *		  system.
     * @see java.lang.Thread#interrupt()
     * @since V2.0
     * @version 02/12/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2004  INB	Check the <code>isStopping</code> flag so that we
     *			can avoid interrupting the shutdown sequence.
     * 01/26/2004  INB	Use <code>syncThreadObj</code>.
     * 05/24/2001  INB	Created.
     *
     */
    public final void interrupt() {
	synchronized (syncThreadObj) {
	    if (!isStopping) {
		if (getThread() != null) {
		    getThread().interrupt();
		}
	    }
	}
    }

    /**
     * Is this <code>StreamListener</code> active?
     * <p>
     *
     * @author Ian Brown
     *
     * @param checkThisI  check for an active thread here?
     * @return is this <code>StreamListener</code> active?
     * @since V2.0
     * @version 01/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2004  INB	Use <code>syncThreadObj</code> and
     *			<code>lThread</code>.
     * 04/09/2001  INB	Created.
     *
     */
    boolean isAlive(boolean checkThisI) {
	boolean isAliveR = true;
	Thread lThread = null;

	synchronized (syncThreadObj) {
	    lThread = getThread();
	}

	if (getTerminateRequested()) {
	    isAliveR = false;
	} else if (checkThisI && (getThread() != null)) {
	    isAliveR = (lThread != null) && lThread.isAlive();
	} else {
	    isAliveR = false;
	    java.util.Vector lChildren =
		(java.util.Vector) getChildren().clone();
	    for (int idx = 0;
		 !isAliveR && (idx < lChildren.size());
		 ++idx) {
		StreamListener child = (StreamListener)
		    lChildren.elementAt(idx);

		isAliveR = child.isAlive(true);
	    }
	}

	return (isAliveR);
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
     * @version 07/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/08/2003  INB	Use the multiple levels of identification in the
     *			<code>RSVP</code>.
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/24/2003  INB	If a terminate has been requested or the EOS was
     *			previously seen, abort the post.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 03/29/2001  INB	Created.
     *
     */
    public void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronized (this) {
	    while (getStartupStage() > 0) {
		wait(TimerPeriod.LONG_WAIT);
	    }
	    if (getTerminateRequested() || getEOS()) {
		return;
	    }
	}

	Rmap rmap;

	if (serializableI instanceof RSVP) {
	    // If the <code>Serializable</code> is a request for an
	    // <code>Acknowledge</code>, then we may tack on our
	    // identification.
	    RSVP rsvp = (RSVP) serializableI;
	    rsvp.pushIdentification(getIdentification());
	    rmap = (Rmap) rsvp.getSerializable();

	} else {
	    rmap = (Rmap) serializableI;
	}

	getParent().post(serializableI);

	if (rmap instanceof EndOfStream) {
	    setEOS(true);
	}

    }

    /**
     * Runs this <code>StreamListener</code>.
     * <p>
     * This method is a NOP. It should be overridden by subclasses that wish to
     * have their own threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2004  INB	Use <code>syncThreadObj</code>.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.  Added identification to
     *			<code>Door</code>.
     * 05/24/2001  INB	Created.
     *
     */
    public void run() {
	getDoor().setIdentification(toString());
	synchronized (syncThreadObj) {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
	    }
	}
	setThread(null);
    }

    /**
     * Removes a <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param listenerI  the <code>StreamListener</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #addListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    public final void removeListener(StreamListener listenerI)
	throws java.lang.InterruptedException
    {
	getChildren().removeElement(listenerI);
	synchronized (this) {
	    setRemoved(getRemoved() + 1);
	    notifyAll();
	}

	/*
	if ((System.getProperty("LOGRBNBEVENTS").indexOf("Listeners") != -1) &&
	    (getNBO() != null) &&
	    (getNBO().getLog() != null)) {
	    try {
		getNBO().getLog().addMessage
		    (Log.STANDARD,
		     Log.CLASS_NBO,
		     toString(),
		     "Removed old listener: " + listenerI);
	    } catch (Exception e) {
	    }
	}
	*/
    }

    /**
     * Removes an <code>AwaitNotification</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param anI  the <code>AwaitNotification</code> object.
     * @see #addNotification(com.rbnb.api.AwaitNotification)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Post an <code>EndOfStream</code> if necessary.
     * 03/27/2001  INB	Created.
     *
     */
    public final void removeNotification(AwaitNotification anI) {
	getAnotification().removeElement(anI);
	synchronized (this) {
	    notifyAll();
	}

	if (getAnotification().size() == 0) {
	    DataRequest myBase =
		((getBaseRequest() instanceof DataRequest) ?
		 ((DataRequest) getBaseRequest()) :
		 null);
	    boolean existing = ((myBase == null) ||
				(myBase.getDomain() == DataRequest.EXISTING));
	    if (existing && !getEOS()) {
		try {
		    post(new EndOfStream());
		} catch (java.lang.Exception e) {
		}
		setEOS(true);
	    }
	}
    }

    /**
     * Sets the number of children added.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addedI the new number that have been added.
     * @see #getAdded()
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final void setAdded(int addedI) {
	added = addedI;
    }

    /**
     * Sets the accept <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doorI the <code>Door</code>.
     * @see #getDoor()
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2002  INB	Created.
     *
     */
    final void setDoor(Door doorI) {
	door = doorI;
    }

    /**
     * Sets the EOS flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eosI the EOS flag.
     * @see #getEOS()
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2001  INB	Created.
     *
     */
    final void setEOS(boolean eosI) {
    	eos = eosI;
	synchronized (this) {
	    notifyAll();
	}
    }

    /**
     * Sets the identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param identificationI the identification.
     * @see #getIdentification()
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final void setIdentification(int identificationI) {
	identification = identificationI;
    }

    /**
     * Sets the isStopping flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isStoppingI are we stopping?
     * @see #getIsStopping()
     * @since V2.2
     * @version 02/12/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2004  INB	Created.
     *
     */
    final void setIsStopping(boolean isStoppingI) {
	isStopping = isStoppingI;
    }

    /**
     * Sets the original request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param originalI  the original request <code>Rmap</code>.
     * @see #getOriginal()
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final void setOriginal(Rmap originalI) {
	original = originalI;
    }

    /**
     * Sets our parent <code>StreamParent</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI  our parent.
     * @see #getParent()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final void setParent(StreamParent parentI) {
	parent = parentI;
	getParent().addListener(this);
    }

    /**
     * Sets the request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @see #getRequest()
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final void setRequest(Rmap requestI) {
	request = requestI;
    }

    /**
     * Sets the number of children removed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param removedI the new number that have been removed.
     * @see #getRemoved()
     * @since V2.0
     * @version 02/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2002  INB	Created.
     *
     */
    final void setRemoved(int removedI) {
	removed = removedI;
    }

    /**
     * Sets the <code>NotificationFrom</code> to listen to.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceI  the <code>NotificationFrom</code>.
     * @see #getSource()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final void setSource(NotificationFrom sourceI) {
	source = sourceI;
    }

    /**
     * Sets the startup stage.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startupStageI the startup stage.
     * @see #getStartupStage()
     * @since V2.0
     * @version 01/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  INB	Created.
     *
     */
    final synchronized void setStartupStage(int startupStageI) {
	startupStage = startupStageI;
	if (startupStage == 0) {
	    notifyAll();
	}
    }

    /**
     * Sets the terminate executing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param terminatingI is a terminate being processed?
     * @see #getTerminateExecuting()
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    final synchronized void setTerminateExecuting(boolean terminatingI) {
	terminateExecuting = terminatingI;
    }

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param terminateI  should the <code>StreamRBOListener</code>
     *			  terminate?
     * @see #getTerminateRequested()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final synchronized void setTerminateRequested(boolean terminateI) {
	terminateRequested = terminateI;
	notifyAll();
    }

    /**
     * Sets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI  the thread.
     * @see #getThread()
     * @since V2.0
     * @version 01/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2004  INB	Use <code>syncThreadObj</code>.
     * 03/27/2001  INB	Created.
     *
     */
    final void setThread(Thread threadI) {
	synchronized (syncThreadObj) {
	    thread = threadI;
	}
    }

    /**
     * Sets the "to pickup" flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param toPickupI  the number to pickup.
     * @see #getToPickup()
     * @since V2.0
     * @version 10/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    final synchronized void setToPickup(long toPickupI) {
	if (toPickupI > toPickup) {
	    notifyAll();
	}
	toPickup = toPickupI;
    }

    /**
     * Sets the waiting flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param waitingI are we waiting for an <code>Acknowledge</code>?
     * @see #getWaiting()
     * @since V2.0
     * @version 04/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2004  INB	Replaced boolean <code>waiting</code> with
     *			<code>int</code>.  Old code left commented out.
     * 10/11/2003  INB	Always notify.
     * 06/07/2001  INB	Created.
     *
     */

/*
//>>> INB <<<
protected Exception lastChange = null;
*/

    final synchronized void setWaiting(boolean waitingI) {

//>>> INB <<<
//	waiting = waitingI;
	if (waitingI) {
	    ++waiting;

/*
	    if (++waiting != 1) {
		try {
		    throw new Exception(this + " " + Thread.currentThread() +
					" waiting should be 1 = " + waiting);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
*/

	} else {
	    if (waiting > 0) {
		--waiting;

/*
		if (--waiting != 0) {
		    try {
			throw new Exception
			    (this + " " + Thread.currentThread() +
			     " waiting should be 0 = " + waiting);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
*/

	    }
	}

/*
lastChange = new Exception(this + " " +
			   Thread.currentThread() +
			   " set waiting to " + waiting);
*/

	notifyAll();
    }

    /**
     * Sets the working request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param workingI  the working request <code>Rmap</code>.
     * @see #getWorking()
     * @since V2.0
     * @version 11/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    final void setWorking(Rmap workingI) {
	working = workingI;
    }

    /**
     * Starts this <code>StreamListener</code> running.
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
     * @see #stop()
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Ensure that we clear the start up stage count.
     * 03/27/2001  INB	Created.
     *
     */
    void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    setStartupStage(getStartupStage() + 1);
	    if (getSource() != null) {
		(new AwaitNotification(getRequest(),getSource(),this)).start();
	    }
	} finally {
	    setStartupStage(getStartupStage() - 1);
	}
    }

    /**
     * Terminates this <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @since V2.0
     * @version 02/13/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2004  INB	Clear the <code>waiting</code> flag.
     * 02/12/2004  INB	Set the <code>isStopping</code> flag.
     * 01/26/2004  INB	Use <code>syncThreadObj</code> and
     *			<code>lThread</code>.
     * 01/15/2004  INB	Replaced synchronization in <code>stop</code> method
     *			to use <code>syncStopObj</code> to eliminate potential
     *			blocked forever problems.  Force an EndOfStream.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/27/2001  INB	Created.
     *
     */
    void stop() {
	Thread lThread = null;
	setWaiting(false);
	synchronized (syncThreadObj) {
	    setIsStopping(true);
	    lThread = getThread();
	}
	if ((lThread != null) &&
	    (lThread != Thread.currentThread())) {
	    synchronized (syncStopObj) {
		if (getTerminateExecuting()) {
		    return;
		}
		setTerminateExecuting(true);
	    }
	    if (!getTerminateRequested()) {
		setTerminateRequested(true);
	    }
	    interrupt();
	} else {
	    setTerminateExecuting(true);
	}

	if (getAnotification() != null) {
	    java.util.Vector anL = getAnotification();
	    for (int idx = 0; idx < anL.size(); ++idx) {
		((AwaitNotification) anL.elementAt(idx)).interrupt();
	    }
	}

	java.util.Vector list = (java.util.Vector) getChildren().clone();
	for (int idx = 0; idx < list.size(); ++idx) {
	    StreamListener child = (StreamListener) list.elementAt(idx);

	    child.stop();
	}

	/*
	synchronized (this) {
	    while ((getAnotification().size() > 0) ||
		   (getChildren().size() > 0)) {
		try {
		    wait(TimerPeriod.NORMAL_WAIT);
		} catch (java.lang.InterruptedException e) {
		    // We're shutting down anyway, so we don't really want to
		    // deal with the interrupt here.
		}
	    }
	}
	*/

	if (getParent() != null) {
	    if (!getEOS()) {
		try {
		    post(new EndOfStream());
		} catch (java.lang.Throwable e) {
		}
	    }
	    try {
		getParent().removeListener(this);
	    } catch (java.lang.InterruptedException e) {
		// We're shutting down anyway, so we don't really want to deal
		// with the interrupt here.
	    }
	}
    }

    /**
     * Gets a string representation of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2002  INB	Created.
     *
     */
    public /*final*/ String toString() {
	String stringR = "";

	if (getParent() instanceof NBO) {
	    try {
		stringR = "SRH(" +
		    ((NBO) getParent()).getFullName() +")\\";
	    } catch (java.lang.Exception e) {
		stringR = "SRH(NBO)\\";
	    }
	} else {
	    stringR = getParent().toString() + "\\";
	}
	try {
	    if (getSource() instanceof RoutingMapHandler) {
		stringR += "(routing map)";
	    } else if (getSource() instanceof Rmap) {
		stringR += ((Rmap) getSource()).getFullName();
	    } else {
		stringR += "(unknown type)";
	    }
	} catch (java.lang.Exception e) {
	    stringR += "(unknown)";
	}
	stringR += "[" + hashCode() + "]";

	return (stringR);
    }

    /**
     * Waits for something to arrive.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stateI  the state of the pickup flag desired.
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
     * 06/16/2003  INB	Use version that takes a stop time.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/29/2001  INB	Created.
     *
     */
    synchronized void waitToPickup(boolean stateI)
	throws java.lang.InterruptedException
    {
	waitToPickup(stateI,Long.MAX_VALUE);
    }

    /**
     * Waits for something to arrive.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stateI   the state of the pickup flag desired.
     * @param stopAtI  the stop time.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 04/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/06/2004  INB	Separate wait loop until two parts, one to wait for
     *			an acknowledge and one to wait for data.
     *			Also, clear the count of messages to pickup if we
     *			waited for the acknowledge before we wait for data.
     * 06/16/2003  INB	Created from <code>waitToPickup</code> without the
     *			stop time.
     *
     */
    synchronized void waitToPickup(boolean stateI,long stopTimeI)
	throws java.lang.InterruptedException
    {
	long startAt = System.currentTimeMillis();
	long now = System.currentTimeMillis();
	long timeOut = TimerPeriod.NORMAL_WAIT;

	// Wait for an acknowledge of the previous message.
	boolean waited = getWaiting();
	while (!getTerminateRequested() && (now < stopTimeI) && getWaiting()) {
	    wait(Math.min(timeOut,(stopTimeI - now)));
	    now = System.currentTimeMillis();
	}

	if (!getWaiting() && waited) {
	    // Clear the to pickup flag now that we've seen the
	    // acknowledgment.
	    setToPickup(0);
	}

	// Wait for data to appear.
	if (!getWaiting()) {
	    while (!getTerminateRequested() &&
		   (now < stopTimeI) &&
		   ((getToPickup() > 0) != stateI)) {
		wait(Math.min(timeOut,(stopTimeI - now)));
		now = System.currentTimeMillis();
	    }

	    if (((getToPickup() > 0) == stateI) && (getToPickup() > 0)) {
		setToPickup(getToPickup() - 1);
	    }
	}

/*
//>>> INB <<<
if (getWaiting()) {
    try {
	throw new Exception
	    (this + " " + Thread.currentThread() +
	     " waited? " + waited +
	     " has an unexpected waiting value of " + waiting);
    } catch (Exception e) {
	e.printStackTrace();
	if (lastChange != null) {
	    lastChange.printStackTrace();
	}
    }
}
*/


    }
}

