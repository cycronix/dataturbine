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
 * <code>RemoteServer</code> "events" for a <code>StreamRequestHandler</code>.
 * <p>
 * <code>RemoteServer</code> "events" are the addition or removal of
 * <code>Server</code> objects. If event matches an entry in the request, then
 * a listener of the appropriate type for the event <code>Rmap</code> is
 * created.
 * <p>
 * <code>RemoteServer</code> objects also represent remote <bold>RBNB</bold>
 * servers. If one or more of the children of the input request cannot be
 * matched locally, then a <code>Sink</code> connection is made to the remote
 * <bold>RBNB</bold> and the request is forwarded.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 05/26/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/26/2004  INB	Call <code>RemoteClient.stopAwaiting</code> in
 *			<code>disconnectFromRemote</code>.
 * 02/23/2004  INB	Optionally log creation of remote clients.
 * 02/19/2004  INB	Added code to insert a level if necessary in
 *			<code>postMessage</code>.
 * 02/13/2003  INB	Use <code>syncThreadObj</code> to ensure that
 *			operations that shouldn't be interrupted are not
 *			interrupted.
 *			Block if we're waiting for an acknowledgement rather
 *			than immediately returning.  This will ensure that
 *			the other end doesn't waste time.  The block times out
 *			eventually.  Added TimeOutDelivery subclass and pulled
 *			some code out into <code>postMessage</code>.
 *			Eliminated unnecessary synchronization on this object.
 *			Clear the <code>waiting</code> flag when stopping.
 * 02/12/2004  INB	Set the <code>isStopping</code> flag.
 * 02/11/2004  INB	Log exceptions at standard level.
 *			If the request handled is a streaming one, then
 *			shutdown the remote client rather than save it.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.  Added identification to
 *			<code>Door</code>.
 * 11/11/2003  INB	Handle relationships in requests.
 * 07/07/2003  INB	Added handling of posted <code>RSVP</code>.
 * 05/23/2003  INB	Ensure that we clear the start up stage count. Reworked
 *			logic to perform connection in <code>run</code> method.
 * 03/26/2003  INB	Add exceptions at log level 0.
 * 03/25/2003  INB	Post an <code>EndOfStream</code> if the
 *			<code>RemoteClient</code> fails to start.
 * 03/24/2003  INB	Wait for a <code>Shortcut</code> to be ready.
 * 03/21/2003  INB	Use the new <code>NBO RemoteClients</code> list.
 * 03/20/2003  INB	Add a unique identifier to the remote client.
 * 03/18/2003  INB	Ensure that the bottom of the request hierarchy is
 *			marked.
 * 12/04/2001  INB	Created.
 *
 */
class StreamRemoteListener
    extends com.rbnb.api.StreamServerListener
    implements com.rbnb.api.RemoteClientOwner
{

    /**
     * connect to remote server?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/23/2003
     */
    private boolean[] doConnect = null;

    /**
     * the <code>RemoteClient</code> connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private RemoteClient rClient = null;

    /**
     * is the request being handled a streaming one?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 02/11/2004
     */
    private boolean streamingRequest = false;

    /**
     * Class constructor to build a <code>StreamRemoteListener</code> for the
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
    StreamRemoteListener(StreamParent parentI,
			 Rmap requestI,
			 NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
    }

    /**
     * Class constructor to build a <code>StreamRemoteListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>RemoteServer</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>RemoteServer</code> source.
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
    StreamRemoteListener(StreamParent parentI,
			 Rmap requestI,
			 RemoteServer sourceI)
	throws java.lang.InterruptedException
    {
	this(parentI,requestI,(NotificationFrom) sourceI);
    }

    /**
     * Connects to the remote <bold>RBNB</bold>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param matchedI flags indicating which child requests are being matched
     *			     locally.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a
     *		  child of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnectFromRemote()
     * @since V2.0
     * @version 02/23/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2004  INB	Optionally log creation of remote clients.
     * 02/13/2003  INB	Use <code>syncThreadObj</code> to ensure that
     *			operations that shouldn't be interrupted are not
     *			interrupted.
     * 03/24/2003  INB	Wait for a <code>Shortcut</code> to be ready.
     * 03/21/2003  INB	Use the new <code>NBO RemoteClients</code> list.
     * 03/20/2003  INB	Add a unique identifier to the remote client.
     * 12/04/2001  INB	Created.
     *
     */
    private final void connectToRemote(boolean[] matchedI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getSource() instanceof RBNBShortcut) {
	    if (!((RBNBShortcut) getSource()).waitReadyForUse()) {				throw new com.rbnb.api.AddressException
		    (((Rmap) getSource()).getFullName() +
		     " was terminated before the listener could be started.");
	    }
	}

	synchronized (syncThreadObj) {
	    setAdded(getAdded() + 1);
	    boolean madeNew = false;

	    setRemoteClient
		(getNBO().getRemoteClient(((Rmap) getSource()).getFullName()));

	    if (getRemoteClient() == null) {
		setRemoteClient
		    (new RemoteClient
			("_RC.sr." + System.currentTimeMillis() +
			 "." + getThread().getName()));
		madeNew = true;

		/*
		if ((System.getProperty("LOGRBNBEVENTS").indexOf("Remote") !=
		     -1) &&
		    (getNBO().getLog() != null)) {
		    try {
			getNBO().getLog().addMessage
			    (Log.STANDARD,
			     Log.CLASS_NBO,
			     toString(),
			     "Created remote client: " +
			     getRemoteClient().getFullName());
		    } catch (Exception e) {
		    }
		}
		*/
	    }
	    getRemoteClient().setOwner(this);
	    if (madeNew) {
		((Rmap) getSource()).addChild((Rmap) getRemoteClient());
		getRemoteClient().setUsername(getNBO().getUsername());
		getRemoteClient().start();
	    }

	    (new AwaitNotification(null,getRemoteClient(),this)).start();
	    sendRequest(matchedI);
	}
    }

    /**
     * Disconnects the <code>RemoteClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #connectToRemote(boolean[])
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Call <code>RemoteClient.stopAwaiting</code>.
     * 02/13/2003  INB	Use <code>syncThreadObj</code> to ensure that
     *			operations that shouldn't be interrupted are not
     *			interrupted.
     * 02/11/2004  INB	If the request was a streaming one, shutdown rather
     *			than store the remote client.
     * 03/21/2003  INB	Use the new <code>NBO RemoteClients</code> list.
     * 12/04/2001  INB	Created.
     *
     */
    private final void disconnectFromRemote()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronized (syncThreadObj) {
	    if (getRemoteClient() != null) {

		// Terminate any awaiting notification threads.
		getRemoteClient().stopAwaiting();

		if (!streamingRequest) {
		    // If we're not streaming, then we can detach the
		    // client for future use.
		    getNBO().addRemoteClient
			(((Rmap) getSource()).getFullName(),getRemoteClient());

		} else {
		    // If we're streaming, we cannot reuse the remote client.
		    getRemoteClient().stop();
		    if (getRemoteClient().getParent() != null) {
			getRemoteClient().getParent
			    ().removeChild(getRemoteClient());
		    }
		}

		setRemoteClient(null);
		setAdded(getAdded() - 1);
	    }
	}
    }

    /**
     * Gets the <code>RemoteClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RemoteClient</code>.
     * @see #setRemoteClient(com.rbnb.api.RemoteClient)
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    public final RemoteClient getRemoteClient() {
	return (rClient);
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
     * @version 02/13/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2004  INB	Eliminated unnecessary synchronization.
     * 12/17/2001  INB	Created.
     *
     */
    final boolean isAlive(boolean checkThisI) {
	boolean isAliveR =
	    (super.isAlive(checkThisI) ||
	     (checkThisI || (getRemoteClient() != null)));

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
     * @version 02/13/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2004  INB	Block if we're waiting for an acknowledgement rather
     *			than immediately returning.  This will ensure that
     *			the other end doesn't waste time.
     *			Eventually, the other side will time out waiting for
     *			this method to return.  That causes the remote side to
     *			shutdown, but the local side won't know it.  Added a
     *			local time out matching the remote side.  When it
     *			happens, set up a thread to deliver an exception to
     *			the local client.  An exception is also returned.
     *			Pulled out the actual post work into
     *			<code>postMessage</code>.
     * 07/07/2003  INB	Added handling of posted <code>RSVP</code>.
     * 01/16/2002  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Wait for any previous message to be delivered.
	long startAt = System.currentTimeMillis();
	long nowAt;
	synchronized (this) {
	   while (getWaiting() &&
		  ((nowAt = System.currentTimeMillis()) - startAt <
		   TimerPeriod.SHUTDOWN)) {
	       wait(TimerPeriod.NORMAL_WAIT);
	   }
	}

	if (getWaiting()) {
	    // If we get here and the previous message still hasn't been
	    // delivered, then the above loop timed out.  In that case, we want
	    // to deliver a failure message to both sides.  We can throw an
	    // exception here and it will be delivered to the remote, but that
	    // doesn't help the local side.  For that, we start a
	    // <code>TimeOutDelivery</code> object.
	    com.rbnb.api.AddressException e = new com.rbnb.api.AddressException
		(this + " failed to deliver " + serializableI +
		 " within a reasonable period of time.");
	    (new TimeOutDelivery(this,e)).start();
	    throw e;
	}

	// Post the message.
	postMessage(serializableI);
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
     * @version 02/19/2004
     */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2004  INB	Added code to insert a level if necessary.
     * 02/13/2004  INB	Created from <code>post</code>.
     *
     */
    final void postMessage(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// If this is an <code>RSVP</code>, we need to figure out the actual
	// message along with the <code>RSVP</code>.
	String myName = ((Rmap) getSource()).getFullName();
	boolean isRSVP = (serializableI instanceof RSVP);
	Serializable serializable = serializableI;
	RSVP rsvp;
	Rmap rmap,
	    parent,
	    child,
	    sc;
	if (!isRSVP) {
	    rsvp = null;
	    rmap = (Rmap) serializableI;
	} else {
	    for (rsvp = (RSVP) serializableI;
		 rsvp.getSerializable() instanceof RSVP;
		 rsvp = (RSVP) rsvp.getSerializable()) {
	    }
	    rmap = (Rmap) rsvp.getSerializable();
	}

	if (rmap.compareNames(".") == 0) {
	    // If the response does not have a name for the remote server, then
	    // insert one here.
	    sc = ((Rmap) getSource()).newInstance();
	    sc.setTrange(rmap.getTrange());
	    sc.setFrange(rmap.getFrange());
	    while (rmap.getNchildren() > 0) {
		Rmap entry = rmap.getChildAt(0);
		rmap.removeChildAt(0);
		sc.addChild(entry);
	    }
	    if (isRSVP) {
		rsvp.setSerializable(sc);
	    } else {
		serializable = sc;
	    }

	} else if (rmap.getName() == null) {
	    // If there is no name at the top of the list, then we may have to
	    // put it in several different places.
	    java.util.Vector list = new java.util.Vector(),
			     vector = new java.util.Vector();
	    int idx = 0,
		idx1;

	    vector.addElement(rmap);
	    vector.addElement(new Integer(0));
	    while (idx >= 0) {
		parent = (Rmap) vector.elementAt(idx);
		idx1 = ((Integer) vector.elementAt(idx + 1)).intValue();

		for (; idx1 < parent.getNchildren(); ++idx1) {
		    child = parent.getChildAt(idx1);

		    if (child.getName() == null) {
			vector.setElementAt(new Integer(idx1 + 1),idx + 1);
			idx += 2;
			if (idx == vector.size()) {
			    vector.addElement(child);
			    vector.addElement(new Integer(0));
			} else {
			    vector.setElementAt(child,idx);
			    vector.setElementAt(new Integer(0),idx + 1);
			}
			idx += 2;
			break;

		    } else if (child.compareNames(".") == 0) {
			sc = ((Rmap) getSource()).newInstance();
			sc.setTrange(child.getTrange());
			sc.setFrange(child.getFrange());
			while (child.getNchildren() > 0) {
			    Rmap entry = child.getChildAt(0);
			    child.removeChildAt(0);
			    sc.addChild(entry);
			}
			parent.removeChild(child);
			list.addElement(parent);

			list.addElement(sc);

		    } else if ((child.compareNames
				(((Rmap) getSource()).getName()) != 0) &&
			       !myName.startsWith(child.getFullName())) {
			sc = ((Rmap) getSource()).newInstance();
			parent.removeChild(child);
			sc.addChild(child);
			parent.addChild(sc);
			idx1 = 0;
		    }
		}

		idx -= 2;
	    }

	    for (idx = 0; idx < list.size(); idx += 2) {
		parent = (Rmap) list.elementAt(idx);
		sc = (Rmap) list.elementAt(idx + 1);
		parent.addChild(sc);
	    }

	} else if ((rmap.compareNames
		    (((Rmap) getSource()).getName()) != 0) &&
		   !myName.startsWith(rmap.getFullName())) {
	    // If the response is headed by something with a name other than
	    // ours, then we need to insert a layer.
	    sc = ((Rmap) getSource()).newInstance();
	    sc.addChild(rmap);
	    if (rsvp != null) {
		rsvp.setSerializable(sc);
	    }
	}

	// Determine if we need to pass on an <code>RSVP</code> from this
	// level.
	DataRequest myBase =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);
	if (!isRSVP && (myBase != null) && myBase.getGapControl()) {
	    // With gap control enabled, we need to send a request for
	    // acknowledgement.
	    setWaiting(true);
	    serializable = new RSVP(0,serializable);
	}

	// Use our superclass to actually post the message.
	super.post(serializable);
    }

    /**
     * The <code>RemoteClient</code> has terminated.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eI the <code>Exception</code> (if any) that terminated this.
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    public final void remoteClientTerminated(java.lang.Exception eI) {
	if (eI != null) {
	    setEOS(true);
	}
	try {
	    disconnectFromRemote();
	} catch (java.lang.Exception e) {
	}
	synchronized (this) {
	    notifyAll();
	}
    }

    /**
     * Runs this <code>StreamRemoteListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.  Added identification to
     *			<code>Door</code>.
     * 05/23/2003  INB	Created.
     *
     */
    public final void run() {
	getDoor().setIdentification(toString());

	if (doConnect != null) {
	    try {
		connectToRemote(doConnect);

	    } catch (java.lang.Throwable e) {
		try {
		    if (getNBO().getLog() != null) {
			if (e instanceof java.lang.Exception) {
			    getNBO().getLog().addException
				(Log.STANDARD,
				 getNBO().getLogClass(),
				 toString(),
				 (java.lang.Exception) e);
			} else if (e instanceof java.lang.Error) {
			    getNBO().getLog().addError
				(Log.STANDARD,
				 getNBO().getLogClass(),
				 toString(),
				 (java.lang.Error) e);
			}
		    }
		} catch (java.lang.Throwable e1) {
		}
		try {
		    post(new EndOfStream());
		} catch (java.lang.Throwable e1) {
		}
		setEOS(true);
	    }
	}

	try {
	    super.run();
	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
	    }
	}
    }

    /**
     * Sends a request to the <code>RemoteClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param matchedI the list of subrequests matched locally.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Save whether or not the request is a streaming one.
     * 11/11/2003  INB	Ensure that we don't lose the relationship in the
     *			request.
     * 03/18/2003  INB	Ensure that the bottom of the request hierarchy is
     *			marked.
     * 12/04/2001  INB	Created.
     *
     */
    private final void sendRequest(boolean[] matchedI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Our ultimate ancestor has the original request <code>Rmap</code> to
	// be matched. That should provide us with information about what we're
	// doing.
	DataRequest base =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	// The working request is our input request plus the hierarchy above it
	// from our parentage minus any subrequests that have been matched
	// locally.
	Rmap wWorking = (Rmap) getRequest().clone();
	wWorking.setParent(null);

	for (int idx = 0, removed = 0; idx < matchedI.length; ++idx) {
	    if (matchedI[idx]) {
		wWorking.removeChildAt(idx - removed);
		--removed;
	    }
	}

	DataRequest working = new DataRequest
	    (base.getName(),
	     base.getTrange(),
	     base.getFrange(),
	     base.getReference(),
	     base.getRelationship(),
	     base.getDomain(),
	     base.getNrepetitions(),
	     base.getIncrement(),
	     base.getSynchronized(),
	     base.getMode(),
	     base.getGapControl());
	streamingRequest = (working.getNrepetitions() == DataRequest.INFINITE);

	StreamListener up = (StreamListener) getParent();
	Rmap highest = null,
	     lowest = null;
	while (up.getRequest() != base) {
	    Rmap above;
	    above = new Rmap();
	    above.setTrange(up.getRequest().getTrange());
	    above.setFrange(up.getRequest().getFrange());
	    above.setDblock(up.getRequest().getDblock());
	    if (highest != null) {
		above.addChild(highest);
	    }
	    highest = above;
	    if (lowest == null) {
		lowest = highest;
	    }
	    up = (StreamListener) up.getParent();
	}

	if ((wWorking.compareNames(".") != 0) &&
	    (wWorking.compareNames(((Rmap) getSource()).getName()) != 0)) {
	    working.addChild(wWorking);
	} else {
	    wWorking.setName(null);
	    Rmap real = new Rmap(".");
	    if (highest == null) {
		real.setTrange(wWorking.getTrange());
		real.setFrange(wWorking.getFrange());
		real.setDblock(wWorking.getDblock());
		wWorking.setTrange(null);
		wWorking.setFrange(null);
		wWorking.setDblock(null);
		if ((wWorking.getName() == null) &&
		    (wWorking.getNchildren() == 1)) {
		    wWorking = wWorking.getChildAt(0);
		    wWorking.getParent().removeChildAt(0);
		}
		lowest = real;

	    } else {
		real.setTrange(highest.getTrange());
		real.setFrange(highest.getFrange());
		real.setDblock(highest.getDblock());
		if (lowest != highest) {
		    highest = highest.getChildAt(0);
		    highest.getParent().removeChildAt(0);
		    real.addChild(highest);
		} else {
		    lowest = real;
		}
	    }
	    if (lowest.getDblock() == null) {
		lowest.setDblock(Rmap.MarkerBlock);
	    }
	    highest = real;
	    working.addChild(highest);
	    lowest.addChild(wWorking);
	}

	// Send the request.
	getRemoteClient().send(working);

	// Start it moving.
	getRemoteClient().send(new Ask(Ask.REQUESTAT,new Integer(0)));
    }

    /**
     * Sets the <code>RemoteClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rClientI the <code>RemoteClient</code>.
     * @see #getRemoteClient()
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    public final void setRemoteClient(RemoteClient rClientI) {
	rClient = rClientI;
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
     * 05/23/2003  INB	Ensure that we clear the start up stage count. Just
     *			set <code>doConnect</code> here.
     * 03/25/2003  INB	Post an <code>EndOfStream</code> if the
     *			<code>RemoteClient</code> fails to start.
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
	boolean doStart = false;

	try {
	    setStartupStage(getStartupStage() + 1);
	    super.start();

	    if (!(getSource() instanceof ServerHandler)) {
		RmapVector sourceList = null;
		if (((Rmap) getSource()).getChildren() != null) {
		    sourceList = ((Rmap)
				  getSource()).getChildren().copyVector();
		}
		DataRequest myBase =
		    ((getBaseRequest() instanceof DataRequest) ?
		     ((DataRequest) getBaseRequest()) :
		     null);
		int count = getRequest().getNchildren();
		boolean[] matched = new boolean[count];
		for (int idx = 0;
		     (sourceList != null) && (idx < sourceList.size());
		     ++idx) {
		    // Add all of the children of the "source" (server) as
		    // events. This matches existing <code>Servers</code>
		    // against the request.
		    Rmap child = (Rmap) sourceList.elementAt(idx);
		    Serializable event = child;

		    if (count > 0) {
			java.util.Vector lVector = null;
			if ((lVector = getRequest().getChildren().findName
			     (child.getName())) != null) {
			    count -= lVector.size();
			    for (int idx1 = 0; idx1 < lVector.size(); ++idx1) {
				Rmap entry = (Rmap) lVector.elementAt(idx1);
				matched[getRequest
				       ().getChildren().indexOf(entry)] =
				    true;
			    }
			}
		    }

		    if ((getAnotification() == null) ||
			(getAnotification().size() == 0)) {
			setTerminateRequested(true);
			break;
		    }
		    ((AwaitNotification)
		     getAnotification().firstElement()).addEvent(event,true);
		}

		String thrName = "_SSL.";
		if (getSource() instanceof ShortcutInterface) {
		    thrName +=
			((ShortcutInterface) getSource()).getDestinationName();
		} else {
		    thrName += ((Rmap) getSource()).getFullName();
		}
		thrName += "." + getNBO().getFullName();
		thrName = thrName.replace('/','_');
		setThread(new ThreadWithLocks(this,thrName));
		doStart = true;
		if (count > 0) {
		    doConnect = matched;
		}
	    }

	} finally {
	    setStartupStage(getStartupStage() - 1);
	}

	if (doStart) {
	    getThread().start();
	}
    }

    /**
     * Terminates this <code>StreamRemoteListener</code>.
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
     * 03/27/2001  INB	Created.
     *
     */
    final void stop() {
	setIsStopping(true);
	setWaiting(false);
	if (!getTerminateExecuting()) {
	    try {
		disconnectFromRemote();
	    } catch (java.lang.Exception e) {
	    }
	}
	super.stop();
    }

    /**
     * Time out message delivery class.
     * <p>
     * This internal class delivers time out messages to the client of the
     * <code>StreamRemoteListener</code> when that client gets around to
     * actually listening for them.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 02/13/2004
     */

    /*
     * Copyright 2004 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2004  INB	Created.
     *
     */
    private final class TimeOutDelivery
	extends java.lang.Thread
    {

	/**
	 * our parent <code>StreamRemoteListener</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 02/13/2004
	 */
	private StreamRemoteListener parent = null;

	/**
	 * the timeout exception message.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 02/13/2004
	 */
	private ExceptionMessage timeOut = null;

	/**
	 * Builds a <code>TimeOutDelivery</code> for the input timeout
	 * <code>Exception</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param parentI  our parent <code>StreamRemoteListener</code>.
	 * @param timeOutI the timeout message.
	 * @since V2.2
	 * @version 02/13/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/13/2004  INB	Created.
	 *
	 */
	public TimeOutDelivery(StreamRemoteListener parentI,
			       Exception timeOutI)
	{
	    super();
	    parent = parentI;
	    timeOut = new ExceptionMessage(timeOutI);
	}

	/**
	 * Runs this <code>TimeOutDelivery</code>, which delivers the message
	 * via the <code>NBO</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 02/13/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/13/2004  INB	Created.
	 *
	 */
	public final void run() {
	    try {
		parent.getNBO().asynchronousException(timeOut.toException());
	    } catch (java.lang.Exception e) {
	    }
	}
    }
}
