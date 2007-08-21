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
 * Server-side object that represents a plugin client application connection to
 * an RBNB server.
 * <p>
 * A plugin client provides data in response to requests from sink
 * applications.  All requests reaching the <code>RBNBPlugIn</code> object in
 * the RBNB server are passed to the plugin client application, wrapped in an
 * Rmap that is used to direct the response back to the sink that made the
 * request.
 * <p>
 * There are two general types of requests handled via an interaction with the
 * plugin client:
 * <p><ol>
 *    <li>Requests for registration information, and</li>
 *    <li>Requests for data.</li>
 * </ol><p>
 * The plugin client can optionally provide a registration map that is stored
 * in the <code>RBNBPlugIn</code>.  If provided, that registration map will be
 * used to respond to registration requests without any interaction with the
 * plugin client.  Otherwise, the plugin client is asked to provide the
 * response.  This operation (in <code>RBNBPlugIn.getRegistered(Rmap
 * requestI)</code>) proceeds as follows:
 * <p><ol>
 *    <li>If there is a supplied registration map, then the request is compared
 *	  to that map and the matching elements are returned
 *	  (<code>Registered.getRegistered</code>),</li>
 *    <li>If there is no supplied registration map, then the code creates a
 *	  <code>RegistrationNotification</code> object to receive notification
 *	  of receipt of registration information and an
 *	  <code>AwaitNotification</code> object to provide the notification
 *	  when the registration is read from the plugin client,</li>
 *    <li>The request is sent as an <code>Ask</code> message to the plugin
 *	  client using <code>RCO.send</code>,</li>
 *    <li>The client is expected to respond by adding a child
 *	  (<code>RBNBPlugIn.addChild</code>) via the <code>RCO</code>.
 *    <li>The response is then picked up by <code>RBNBPlugIn</code> thread
 *	  (<code>RBNBPlugIn.acceptFrame</code>) and is posted to the
 *	  appropriate notification listener
 *	  (<code>RBNBPlugIn.post</code> to
 *	  <code>RegistrationNotification.accept</code>),</li>
 *    <li>The response is then picked up by the requesting thread
 *	  (<code>RegistrationNotification.getEvent</code>) and is returned to
 *	  the requesting client.</li>
 * </ol><p>
 * To get data from a plugin, a sink makes a request the same way it would to
 * get data from any regular source.  A sink can also send a message to a
 * plugin by putting data into the request.  Indeed, messaging (such as for a
 * chat program) can be done in this manner, with the plugin response being a
 * simple acknowledment of receipt of the message.
 * <p>
 * All requests are headed by an <code>Rmap</code> that is used to ensure that
 * the response goes back to the appropriate requesting NBO.  The name and time
 * information contained in the request uniquely identify an
 * <code>AwaitNotification</code> object in the list of such objects waiting
 * for responses from this plugin.
 * <p>
 * Data retrieval from a plugin proceeds as follows:
 * <p><ol>
 *    <li>The <code>AwaitNotification</code> object of the
 *	  <code>StreamPlugInListener</code> that has identified this plugin as
 *	  a source matching its request is added to the list of such objects
 *	  waiting for a response from this plugin
 *	  (<code>RBNBPlugIn.addNotification</code>),</li>
 *    <li>If the request is for the wildcard "...", the request is non-absolute
 *	  with no time offset and a duration of 0., and there is a local
 *	  registration map, then the entire registration map is returned
 *	  (<code>RBNBPlugIn.initiateRequest</code>),</li>
 *    <li>Otherwise, if there is a registration map, the request is limited to
 *	  channels in that map (<code>Rmap.limitToValid</code>,</li>
 *    <li>If there were no valid channels found in the request, then a simple
 *	  <code>EndOfStream</code> is the given as the answer by wrapping it in
 *	  an <code>Rmap</code> containing the request name and time range
 *	  information and adding it as a child
 *	  (<code>RBNBPlugIn.addChild</code>),</li>
 *    <li>Otherwise, the request is sent to the plugin application, preceeded
 *	   by any request options using <code>RCO.send</code>,</li>
 *    <li>The plugin application responds by performing an
 *	  <code>RBNBPlugIn.addChild</code> call on an <code>Rmap</code>
 *	  hierarchy headed by an <code>Rmap</code> with the request name and
 *	  time range information,</li>
 *    <li>The thread running for this <code>RBNBPlugIn</code> picks up the
 *	  <code>Rmap</code> frame and posts it to the matching
 *	  <code>AwaitNotification</code> object
 *	  (<code>RBNBPlugIn.post</code>).</li>
 * </ol><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.AwaitNotification
 * @see com.rbnb.api.DataRequest
 * @see com.rbnb.api.EndOfStream
 * @see com.rbnb.api.Rmap
 * @see com.rbnb.api.StreamPlugInListener
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/6/2005:  EMF      Ensure all AwaitNotifications are stopped in getRegistered
 * 09/02/2005  EMF      Send PlugIn client notice when stream terminates.
 * 08/04/2004  INB	Added documentation.
 * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 01/16/2004  INB	Shutdown the <code>RCO</code> before removing us as
 *			a child and ensure that metrics are transferred while
 *			synchronized.
 * 11/18/2003  INB	Added debug to waits.
 * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
 *			Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 11/12/2003  INB	Added is in startup handling.
 * 06/16/2003  INB	Added handling of <code>RequestOptions</code>.
 * 05/22/2003  INB	Do not pass ">..." requests to the plugin application.
 * 05/06/2003  INB	Strip out dots before posting events.
 * 04/04/2003  INB	Handle Java errors.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/14/2003  INB	<code>logStatus</code> uses <code>StringBuffer</code>.
 * 01/14/2002  INB	Created.
 *
 */
final class RBNBPlugIn
    extends com.rbnb.api.RBNBClient
    implements com.rbnb.api.PlugInHandler
{
	/** keep track of active streaming requests,
	 *  so can notify client when they terminate
	 *  <p>
	 *
	 * @author Eric Friets
	 *
	 * @since V2.6
	 * @version 09/02/2005
	 */
	private java.util.Hashtable activeStreams = new java.util.Hashtable();
	
    /**
     * accepting a frame?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2002
     */
    private boolean acceptingAFrame = false;

    /**
     * adding a frame?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2002
     */
    private boolean addingAFrame = false;

    /**
     * list of objects awaiting update notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */
    private com.rbnb.utility.SortedVector awaiting =
	new com.rbnb.utility.SortedVector();

    /**
     * the last frame received.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/18/2002
     */
    private Rmap frame = null;

    /**
     * the registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/20/2002
     */
    private Registration registered = null;

    /**
     * registration door.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/14/2001
     */
    private Door registrationDoor = null;


    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
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
    RBNBPlugIn()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	registrationDoor = new Door(Door.READ_WRITE);
    }

    /**
     * Class constructor to build an <code>RBNBPlugIn</code> for an
     * <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
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
    RBNBPlugIn(RCO rcoI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(rcoI);
	registrationDoor = new Door(Door.READ_WRITE);
    }

    /**
     * Class constructor to build a <code>RBNBPlugIn</code> for a
     * particular <code>RCO</code> from a <code>PlugInInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI	  the <code>RCO</code>.
     * @param plugInI the <code>PlugInInterface</code>.
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the <code>ClientInterface</code> is not a
     *		  <code>RBNBClient</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
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
    RBNBPlugIn(RCO rcoI,PlugInInterface plugInI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(rcoI);
	update(plugInI);
    }

    /**
     * Accepts a new frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param waitI  wait for a frame to arrive?
     * @return the frame <code>Rmap</code> or null.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 01/18/2002  INB	Created.
     *
     */
    private final Rmap acceptFrame(boolean waitI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap theFrameR = null;

	// Look for a frame to process.
	synchronized (this) {
	    while ((getFrame() == null) &&
		   waitI &&
		   !getTerminateRequested()) {
		wait(TimerPeriod.NORMAL_WAIT);
	    }

	    if (!getTerminateRequested() &&
		((theFrameR = getFrame()) != null)) {
		// If we get a frame, grab it.
		setAcceptingAFrame(true);
		setFrame(null);
		setAcceptingAFrame(false);
	    }
	}

	return (theFrameR);
    }

    /**
     * Adds a child <code>Rmap</code>.
     * <p>
     * This method just makes the <code>Rmap</code> available to the data
     * handler thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #acceptFrame(boolean)
     * @since V2.0
     * @version 11/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/18/2003  INB	Added debug to waits.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 01/17/2002  INB	Created.
     *
     */
    public final void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	setAddingAFrame(true);

	if ((childI instanceof StorageManager) ||
	    (childI instanceof Registration)) {
	    // If the child is the <code>StorageManager</code> or
	    // <code>Registration</code>, add it directly.
	    super.addChild(childI);
	    setAddingAFrame(false);
	    notifyAll();

	} else {
	    childI.stripDot();
	    synchronized (this) {
		// Wait for the previous frame to be processed.
		long startAt = System.currentTimeMillis();
		long nowAt;
		while ((getFrame() != null) &&
		       !getTerminateRequested() &&
		       ((getRCO() == null) ||
			!getRCO().getTerminateRequested())) {
		    wait(TimerPeriod.NORMAL_WAIT);
		    nowAt = System.currentTimeMillis();
		    if (nowAt - startAt >= TimerPeriod.LOCK_WAIT) {
			try {
			    throw new java.lang.Exception
				(nowAt + " " + getFullName() + " " +
				 Thread.currentThread() +
				 " RBNBPlugIn.addChild blocked waiting for " +
				 "work to complete.");
			} catch (java.lang.Exception e) {
			    e.printStackTrace();
			}
			startAt = nowAt;
		    }
		}
		if (getTerminateRequested() ||
		    ((getRCO() != null) &&
		    getRCO().getTerminateRequested())) {
		    return;
		}

		// Place the input frame <code>Rmap</code> where it will be
		// picked up.
		setFrame(childI);
		setAddingAFrame(false);
		notifyAll();
	    }
	}
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
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    public final void addNotification(AwaitNotification anI) {

		try {
			awaiting.add(anI);
		} catch (com.rbnb.utility.SortException e) {
			throw new java.lang.InternalError();
		}
    }

    /**
     * Handles an <code>ExceptionMessage</code> received from the client.
     * <p>
     *
     * @author Ian Brown
     *
     * @param emsgI the <code>ExceptionMessage</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 04/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/23/2002  INB	Created.
     *
     */
    public final void exception(ExceptionMessage emsgI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// We cannot tell what generated the error, so our only real option is
	// to bail on everyone and throw the exception up the line.  We cleanly
	// bail by sending an <code>EndOfStream</code> to everyone.
	AwaitNotification an;
	Rmap answer = new EndOfStream(EndOfStream.REASON_ERROR);
	for (int idx = 0; idx < awaiting.size(); ++idx) {
	    an = (AwaitNotification) awaiting.elementAt(idx);
	    an.addEvent(answer,false);
	}

	super.exception(emsgI);
    }

    /**
     * Gets the accepting a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we in the process of accepting a frame?
     * @see #setAcceptingAFrame(boolean)
     * @since V2.0
     * @version 01/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2002  INB	Created.
     *
     */
    private final boolean getAcceptingAFrame() {
	return (acceptingAFrame);
    }

    /**
     * Gets the adding a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we in the process of adding a frame?
     * @see #setAddingAFrame(boolean)
     * @since V2.0
     * @version 01/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2002  INB	Created.
     *
     */
    private final boolean getAddingAFrame() {
	return (addingAFrame);
    }

    /**
     * Gets the frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the frame <code>Rmap</code>.
     * @see #setFrame(com.rbnb.api.Rmap)
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
    private final Rmap getFrame() {
	return (frame);
    }

    /**
     * Gets the log class mask for this <code>RBNBPlugIn</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
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
    public final long getLogClass() {
	return (super.getLogClass() | Log.CLASS_RBNB_PLUGIN);
    }

    /**
     * Gets the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Registration</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @see #setRegistered(com.rbnb.api.Registration)
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
    public final Rmap getRegistered()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (registered);
    }

    /**
     * Gets the registration list for this <code>RBNBPlugIn</code> matching the
     * input hierarchy.
     * <p>
     * At the moment, the only valid input is an <code>Rmap</code> with the
     * same name as this <code>RBNBPlugIn</code> with an optional child named
     * "...".
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return the matching registration information.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @since V2.0
     * @version 11/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/6/2005:  EMF  Ensure all AwaitNotifications are stopped
     * 11/18/2003  INB	Added debug to waits.
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 11/12/2003  INB	During startup or termination, do not try to actually
     *			check registration information.
     * 05/22/2003  INB	Do not pass ">..." requests to the plugin application.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 01/14/2002  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PlugInInterface plugInR = (PlugInInterface)
	    super.getRegistered(requestI);

	if (getIsInStartup() || getTerminateRequested()) {
	    return ((Rmap) plugInR);
	}

	long startAt = System.currentTimeMillis();
	long nowAt;
	synchronized (this) {
	    while (isNew() && getThread().isAlive()) {
		wait(TimerPeriod.NORMAL_WAIT);
		nowAt = System.currentTimeMillis();
		if (nowAt - startAt >= TimerPeriod.LOCK_WAIT) {
		    try {
			throw new java.lang.Exception
			    (nowAt + " " + getFullName() + " " +
			     Thread.currentThread() +
			     " RBNBPlugIn.getRegistered blocked waiting " +
			     "for work to complete.");
		    } catch (java.lang.Exception e) {
			e.printStackTrace();
		    }
		    startAt = nowAt;
		}
	    }
	}

	if (!getThread().isAlive()) {

	} else if (requestI.getNchildren() != 0) {
	    if (getRegistered() != null) {
		boolean locked = false;
		try {
		    registrationDoor.lockRead("RBNBPlugIn.getRegistered");
		    locked = true;
		    Rmap subRequest,
			rmap;

		    Registration lreg = (Registration) getRegistered();
		    for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
			subRequest = requestI.getChildAt(idx);
			if ((subRequest.compareNames("...") == 0) ||
			    (subRequest.compareNames(">...") == 0)) {
			    plugInR.addChild((Rmap) getRegistered().clone());
			} else {
			    if ((rmap = lreg.getRegistered(subRequest)) !=
				null) {
				plugInR.addChild(rmap);
			    }
			}
		    }
		} finally {
		    if (locked) {
			registrationDoor.unlockRead();
		    }
		}

	    } else {
		RegistrationNotification rNot = new RegistrationNotification();
		AwaitNotification aNot = new AwaitNotification
		    (null,
		     this,
		     rNot);

		aNot.start();
		Rmap request = new DataRequest(aNot.getName());
		for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
		    request.addChild((Rmap) requestI.getChildAt(idx).clone());
		}
		Rmap ddd = request.findDescendant
		    ("/" + aNot.getName() + "/>...",false);
		if (ddd != null) {
		    ddd.getParent().removeChild(ddd);
//		    ddd.getParent().findChild(ddd).setName("*");  // mjm 11/17/06 wild grope to limit PI one deep request
		}

		if (request.getNchildren() > 0) {
		    Ask ask = new Ask(Ask.REGISTERED,request);
		    getRCO().send(ask);
		    Serializable event = rNot.getEvent();
		    //EMF 12/6/2005: all AwaitNotifications need to be stopped
                    //               so push this call outside if block
                    //aNot.interrupt();

		    if (event instanceof ExceptionMessage) {
			Language.throwException((ExceptionMessage) event);
		    } else if (event instanceof Rmap) {
			Rmap rmap = (Rmap) event;
			rmap.stripDot();
			plugInR.addChild(rmap);
		    } else if (event != null) {
			throw new com.rbnb.api.SerializeException
			    ("Unexpected registration response: " + event);
		    }
		}
                //EMF 12/6/2005: ensure all AwaitNotifications are stopped
                aNot.interrupt();
	    }
	}

	return ((Rmap) plugInR);
    }

    /**
     * Initiates a request for data from a <code>PlugIn</code>.
     * <p>
     * The top of the request <code>Rmap</code> hierarchy identifies the target
     * of the response to the request. It is expected that the client
     * application will include the name of the request in the response.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request <code>Rmap</code> hierarchy.
     * @param roI      the <code>RequestOptions</code> associated with the
     *		       request.  May be <code>null</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
	 * 09/02/2005  EMF  Keep track of streaming requests, to facilitate cleanup
	 *                  when they terminate.
     * 06/16/2003  INB	Added handling of <code>RequestOptions</code>.
     * 01/18/2002  INB	Created.
     *
     */
    public final void initiateRequest(Rmap requestI,RequestOptions roI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
    //EMF 9/2/05: if streaming request, remember it
	if ((requestI instanceof DataRequest) && 
	  ((DataRequest)requestI).getNrepetitions()==DataRequest.INFINITE) {
		activeStreams.put(requestI.getName(),requestI);
	}

	Rmap answer = null,
	    rChild = ((requestI.getNchildren() == 1) ?
		      requestI.getChildAt(0) :
		      null);
	int idx;

	if ((requestI instanceof DataRequest) &&
	    (rChild != null) &&
	    (rChild.compareNames("...") == 0) &&
	    (rChild.getTrange() != null) &&
	    (rChild.getTrange().compareTo
	     (new TimeRange(0.,0.)) == 0) &&
	    (getRegistered() != null) &&
	    (getRegistered().getNchildren() > 0) &&
	    (((DataRequest) requestI).getReference() !=
	     DataRequest.ABSOLUTE)) {
	    answer = new Rmap(requestI.getName());
	    answer.setTrange(requestI.getTrange());
	    answer.addChild(new EndOfStream());
	    answer.getChildAt(0).setTrange(new TimeRange(0.,Double.MAX_VALUE));
	    answer.getChildAt(0).setFrange(new TimeRange(0.,Long.MAX_VALUE));
	    answer.getChildAt(0).addChild((Rmap) getRegistered().clone());
	    addChild(answer);
	}

	if (answer == null) {
	    Rmap request = requestI;
	    if ((getRegistered() != null) &&
		(getRegistered().getNchildren() > 0)) {
		// If there are registered channels, then we need to ensure
		// that the request only asks for those channels.
		request = requestI.newInstance();
		for (idx = 0; idx < requestI.getNchildren(); ++idx) {
		    Rmap child = requestI.getChildAt(idx),
			vChild = child.limitToValid(getRegistered());

		    if (vChild != null) {
			request.addChild(vChild);
		    }
		}
	    }

	    if (request.getNchildren() > 0) {
		if (roI != null) {
		    getRCO().send(roI);
		}
		getRCO().send(request);
	    } else {
		answer = new Rmap(requestI.getName());
		answer.setTrange(requestI.getTrange());
		answer.setFrange(requestI.getFrange());
		answer.addChild(new EndOfStream(EndOfStream.REASON_NONAME));
		addChild(answer);
	    }
	}
    }

    /**
     * Logs the status of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param prefixI the prefix string for the message.
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Eliminated the locked variable as it is not needed.
     * 03/14/2003  INB	Use <code>StringBuffer</code> to build message.
     * 11/19/2002  INB	Created.
     *
     */
    public final void logStatus(String prefixI) {
	try {
	    String[] names = null;
	    boolean locked = false;
	    try {
		lockRead();
		locked = true;
		names = super.getRegistered(new Rmap("...")).extractNames();
	    } finally {
		if (locked) {
		    unlockRead();
		}
	    }

	    if ((names == null) || (names.length == 0)) {
		super.logStatus(prefixI);
	    } else {
		StringBuffer message = new StringBuffer
		    (prefixI + " with the following channels:");
		for (int idx = 0; idx < names.length; ++idx) {
		    message.append("\n\t" + names[idx].substring(1));
		}
		getLog().addMessage(getLogLevel(),
				    getLogClass(),
				    getName(),
				    message.toString());
	    }
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Creates a new instance of the same class as this
     * <code>RBNBPlugIn</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    final Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (PlugInIO.newInstance(this));
    }

    /**
     * Notifies the appropriate object that is awaiting notification of the
     * arrival of an "event" <code>Serializable</code>.
     * <p>
     * The events are always <code>Rmaps</code>. The top level of the Rmap
     * hierarchy actually identifies where the <code>Rmap</code> is actually
     * supposed to go. The first child is the actual response.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> event.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Strip out the dot from the answer, if any.
     * 01/18/2002  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap id = (Rmap) serializableI;
	if (id != null) {
	    // Figure out where this response is supposed to go.
	    AwaitNotification an = null;
	    try {
		synchronized (awaiting) {
		    an = (AwaitNotification) awaiting.find(id.getName());
		}
 
	    } catch (com.rbnb.utility.SortException e) {
		throw new java.lang.InternalError();
	    }

	    // mjm 9/14/04:  guessing here is where a plugin Flush to a missing
	    // client happens.  On null match, send notification to plugin to stop!?

	    if (an != null) {
			//EMF 9/15/05: if too many frames waiting, kill the stream
			//EMF 10/19/05: make stack depth function of stream type;
			//              delete existing stack entries when limit reached
			//              to keep requesting client more up to date
			if (activeStreams.containsKey(an.getName())) {		
				DataRequest dr=(DataRequest)activeStreams.get(an.getName());
				if (dr.getNrepetitions()==DataRequest.INFINITE) { //it is a stream
					int stackLimit;
					if (dr.getIncrement()==1.0) { //subscribe
System.err.println("RBNBPlugIn.post: set stackLimit=100");
						stackLimit=100;
						if (an.numEvents()>stackLimit) {
							System.err.println("RBNBPlugIn: too many pending frames in stream "+id.getName());
							System.err.println("            pending frames discarded");
						}
					} else { //monitor
System.err.println("RBNBPlugIn.post: set stackLimit=2");
						stackLimit=2;
					}
					if (an.numEvents()>stackLimit) {
						//an.interrupt();
						an.clearEvents();
						}
				}
			}
			// Send the response to that target.
			if (id.getNchildren() == 1) {
				Rmap postIt = id.getChildAt(0);
				id.removeChildAt(0);
				postIt.stripDot();
				an.addEvent(postIt,false);
			} else if (id.getNchildren() > 1) {
				id.setName(null);
				id.setTrange(null);
				id.setFrange(null);
				an.addEvent(id,false);
			}
	    }
	}
    }

    /**
     * Updates the registration for this <code>PlugIn</code>.
     * <p>
     * The input <code>Rmap</code> hierarchy is used to update the registration
     * for this <code>PlugIn</code>.  The hierarchy may contain
     * <code>DataBlocks</code>, but not time information.  Those
     * <code>DataBlocks</code> are copied into the appropriate locations in the
     * registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the registration <code>Rmap</code> hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #reRegister(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 01/14/2002  INB	Created.
     *
     */
    public final void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    registrationDoor.lock("RBNBPlugIn.register");
	    if (getRegistered() == null) {
		setRegistered(new Registration());
	    }
	    rmapI.stripDot();
	    ((Registration) getRegistered()).updateRegistration
		(rmapI,
		 false,
		 true);

	} finally {
	    registrationDoor.unlock();
	}
    }

    /**
     * Replace the entire registration map for this <code>PlugIn</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the new registration map.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #register(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 01/21/2002  INB	Created.
     *
     */
    public final void reRegister(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    registrationDoor.lock("RBNBPlugIn.reRegister");
	    setRegistered(new Registration());
	    rmapI.stripDot();
	    ((Registration) getRegistered()).updateRegistration
		(rmapI,
		 false,
		 true);

	} finally {
	    registrationDoor.unlock();
	}
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
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
	 * 09/02/2005  EMF  Notify plugin if stream request terminates
     * 01/18/2002  INB	Created.
     *
     */
    public final void removeNotification(AwaitNotification anI) {

    //EMF 9/2/05: if this was a streaming request, notify plugin
	try {
		if (activeStreams.containsKey(anI.getName())) {		
			Rmap req=(Rmap)activeStreams.get(anI.getName());
			//mark as end of stream
			Rmap eos=new EndOfStream(EndOfStream.REASON_END);
			eos.addChild(req);
			//send to plugin
			getRCO().send(eos);
		}
	} catch (Exception e) {
		System.err.println("Exception notifying PlugIn of stream termination:");
		e.printStackTrace();
	}
	
	awaiting.removeElement(anI);
    }

    /**
     * Runs the <code>RBNBPlugIn</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/16/2004  INB	Shutdown the <code>RCO</code> before removing us as
     *			a child and ensure that metrics are transferred while
     *			synchronized.
     * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
     *			Added identification to the <code>Door</code>.
     * 11/12/2003  INB	Added is in startup handling.
     * 04/04/2003  INB	Handle Java errors.
     * 01/02/2001  INB	Created.
     *
     */
    public final void run() {
	TimerTask statusTT = null;
	try {
	    // Log the start.
	    setIsInStartup(true);
	    String message;
	    if (getRCO().getBuildVersion() == null) {
		message = "Started for client running an old build version";
	    } else {
		message = ("Started for client running " +
			   getRCO().getBuildVersion() +
			   " from " + getRCO().getBuildDate());
	    }
	    /*
	    if (getRCO().getLicenseString() != null) {
		message += " using license " + getRCO().getLicenseString();
	    }
	    */
	    message += ".";

	    registrationDoor.setIdentification(getFullName());

	    getLog().addMessage
		(getLogLevel(),
		 getLogClass(),
		 getName(),
		 message);

	    synchronized (this) {
		notifyAll();
	    }

	    // Schedule a status logger.
	    if (getLocalServerHandler().getLogStatusPeriod() > 0) {
		MetricsCollector metricsCollector = new MetricsCollector();
		metricsCollector.setObject(this);
		statusTT = new TimerTask(metricsCollector,
					 LogStatusInterface.TT_LOG_STATUS);
		getLocalServerHandler().getTimer().schedule
		    (statusTT,
		     getLocalServerHandler().getLogStatusPeriod(),
		     getLocalServerHandler().getLogStatusPeriod());
	    }

	    // Wait until we are asked to stop.
	    setIsInStartup(false);
	    while (!getTerminateRequested() && !getThread().interrupted()) {
		Rmap theFrameR = acceptFrame(true);
		if (theFrameR != null) {
		    post(theFrameR);
		}
		if (getThread() != null) {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBNBPlugIn.run(1)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		}
	    }

	} catch (com.rbnb.api.AddressException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Exception e1) {
	    }

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Exception e1) {
	    }

	} catch (java.io.IOException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Exception e1) {
	    }

	} catch (java.lang.InterruptedException e) {
	    try {
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Exception e1) {
	    }

	} catch (java.lang.Error e) {
	    try {
		getLog().addError(Log.STANDARD,
				  getLogClass(),
				  toString(),
				  e);
		getRCO().send(Language.exception
			      (new java.lang.Exception
				  ("A fatal error occured.\n" +
				   e.getClass() + " " + e.getMessage())));
	    } catch (java.lang.Throwable e1) {
	    }

	} finally {
	    if (getThread() != null) {
		try {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBNBPlugIn.run(2)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		} catch (java.lang.Exception e) {
		}
	    }
	}

	// Stop anyone who is waiting.
	stopAwaiting();

	// Log the end.
	synchronized (this) {
	    if (statusTT != null) {
		try {
		    statusTT.cancel();
		} catch (java.lang.Exception e) {
		}
		statusTT = null;
	    }
	}

	logStatus("Is shutting down");
	synchronized (this) {
	    if (getParent() != null) {
		try {
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getName(),
			 "Stopped.");
		} catch (java.lang.Exception e) {
		}
	    }
	}

	// Ask the <code>RCO</code> to stop.
	RCO lRCO = null;
	try {
	    if (getRCO() != null) {
		lRCO = getRCO();
		getRCO().stop();
	    }
	} catch (java.lang.Exception e) {
	}
	if (lRCO != null) {
	    synchronized (metricsSyncObj) {
		metricsBytes += lRCO.bytesTransferred();
		setRCO(null);
	    }
	}

	// Remove us as a child.
	try {
	    Rmap lParent = getParent();
	    RemoteServer rParent = (RemoteServer) lParent;
	    synchronized (rParent.metricsSyncObj) {
		lParent.removeChild(this);
		((RemoteServer) lParent).metricsDeadBytes +=
		    bytesTransferred();
	    }
	} catch (java.lang.Exception e) {
	}

	setThread(null);
	synchronized (this) {
	    notifyAll();
	}
    }

    /**
     * Sets the accepting a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptingAFrameI  are we in the process of accepting a frame?
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #getAcceptingAFrame()
     * @since V2.0
     * @version 01/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2002  INB	Created.
     *
     */
    private final synchronized void setAcceptingAFrame
	(boolean acceptingAFrameI)
	throws java.lang.InterruptedException
    {
	acceptingAFrame = acceptingAFrameI;
	notifyAll();
    }

    /**
     * Sets the adding a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addingAFrameI  are we in the process of adding a frame?
     * @see #getAddingAFrame()
     * @since V2.0
     * @version 01/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2002  INB	Created.
     *
     */
    private final void setAddingAFrame(boolean addingAFrameI) {
	addingAFrame = addingAFrameI;
    }

    /**
     * Sets the frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI  the new frame <code>Rmap</code>.
     * @see #getFrame()
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
    private final void setFrame(Rmap frameI) {
	frame = frameI;
    }

    /**
     * Sets the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param registeredI  the new <code>Registration</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getRegistered()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 01/14/2002  INB	Created.
     *
     */
    final void setRegistered(Registration registeredI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    registrationDoor.lock("RBNBPlugIn.setRegistered");
	    registeredI.setParent(this);
	    registered = registeredI;
	} finally {
	    registrationDoor.unlock();
	}
    }

    /**
     * Stops the objects in the <code>AwaitNotification</code> list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    private final void stopAwaiting() {
	if (awaiting.size() > 0) {
	    // Clear the awaiting notification list.
	    java.util.Vector anv = (java.util.Vector) awaiting.clone();

	    Rmap response;
	    for (int idx = 0; idx < anv.size(); ++idx) {
		AwaitNotification an = (AwaitNotification) anv.elementAt(idx);

		try {
		    response = new Rmap(an.getName());
		    response.addChild(new EndOfStream());
		    post(response);
		} catch (java.lang.Exception e) {
		    an.interrupt();
		}
	    }
	}
    }

    /**
     * Registration notification class.
     * <p>
     * This internal class handles notification events for plugin registration
     * requests.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/25/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  INB	Created.
     *
     */
    private final class RegistrationNotification
	implements com.rbnb.api.NotificationTo
    {
	/**
	 * the list of <code>AwaitNotifications</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 04/15/2002
	 */
	private java.util.Vector aNotification = new java.util.Vector();

	/**
	 * <code>Serializable</code> event representing the results of the
	 * registration request.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 04/15/2002
	 */
	private Serializable event = null;

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
	 * @exception com.rbnb.api.EndOfStreamException
	 *		  thrown if there is no way for any data to be
	 *		  collected; for example a request for oldest or newest
	 *		  existing data when no data exists.
	 * @exception com.rbnb.api.SerializeException
	 *		  thrown if there is a serialization problem.
	 * @exception java.io.IOException
	 *		  thrown if there is an I/O problem.
	 * @exception java.lang.InterruptedException
	 *		  thrown if this operation is interrupted.
	 * @since V2.0
	 * @version 04/15/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/15/2002  INB	Created.
	 *
	 */
	public final synchronized void accept(Serializable eventI,
					      Rmap matchI)
	    throws com.rbnb.api.AddressException,
		   com.rbnb.api.SerializeException,
		   java.io.IOException,
		   java.lang.InterruptedException
	{
	    event = eventI;
	    notifyAll();
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
	 * @version 04/15/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/15/2002  INB	Created.
	 *
	 */
	public final void addNotification(AwaitNotification anI) {
	    aNotification.addElement(anI);
	}

	/**
	 * Retrieves the event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the <code>Serializable</code> event.
	 * @exception java.lang.InterruptedException
	 *	      thrown if this operation is interrupted.
	 * @since V2.0
	 * @version 04/25/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/15/2002  INB	Created.
	 *
	 */
	final synchronized Serializable getEvent()
	    throws java.lang.InterruptedException
	{
	    if (event == null) {
		wait(TimerPeriod.PLUGINREGISTRATION);
	    }

	    return (event);
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
	 * @version 04/15/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/15/2002  INB	Created.
	 *
	 */
	public final void removeNotification(AwaitNotification anI) {
	    aNotification.removeElement(anI);
	}
    }
}
