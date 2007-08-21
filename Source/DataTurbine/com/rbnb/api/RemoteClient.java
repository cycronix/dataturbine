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
 * Virual remote server client.
 * <p>
 * This class provides local sink clients (via
 * <code>StreamRemoteListener</code>) with a virtual connection to the RBNB
 * DataTurbine server represented by a local <code>RemoteServer</code> or one
 * of its subclasses.
 * <p>
 * The operation of this class is described as part of the documentation on
 * routing in <code>RemoteServer</code>.
 * <p.

 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RemoteClientHandler
 * @see com.rbnb.api.RemoteServer
 * @see com.rbnb.api.StreamRemoteListener
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
 * 08/05/2004  INB	Updated documentation and added link to RemoteServer
 *			and StreamRemoteListener.
 * 05/26/2004  INB	Added <code>stopAwaiting</code> method.
 * 02/12/2004  INB	If there is more than one receiver, clone the message
 *			for all but the last one.
 * 05/23/2003  INB	Added startup thread handling.
 * 04/30/2003  INB	Added firewalls to <code>send/start</code> methods.
 * 04/01/2003  INB	Replaced use of <code>interrupt</code> with stop thread
 *			logic.
 * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
 * 03/24/2003  INB	Throw an exception if the <code>start</code> fails.
 * 03/20/2003  INB	Add a unique identifier. Use a timeout on
 *			<code>wait</code> calls.
 * 03/18/2003  INB	Ensure that we don't remove <code>Shortcuts</code>.
 * 11/28/2001  INB	Created.
 *
 */
final class RemoteClient
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.NotificationFrom,
	       com.rbnb.api.RoutedTarget,
	       com.rbnb.api.UsernameInterface
{
    /**
     * list of objects awaiting update notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    java.util.Vector awaiting = new java.util.Vector();

    /**
     * are we already stopping?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/23/2003
     */
    private boolean inStop = false;

    /**
     * the message being delivered.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */
    private Serializable message = null;

    /**
     * the <code>RemoteClientOwner</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/17/2001
     */
    private RemoteClientOwner owner = null;

    /**
     * 
     * the thread that is expecting to receive a response.
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 01/02/2002
     */
    private Thread receiver = null;

    /**
     * name of the remote handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */
    private String remoteName = null;

    /**
     * the <code>RoutingMapHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */
    private RoutingMapHandler rHandler = null;

    /**
     * the remote <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */
    private Sink sink = null;

    /**
     * 
     * the thread that is starting the client up.
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 05/23/2003
     */
    private Thread startThread = null;

    /**
     * stop the specified thread from listening.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/01/2003
     */
    private Thread stopThread = null;

    /**
     * the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private Username username = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/28/2001  INB	Created.
     *
     */
    RemoteClient() {
	super();
    }

    /**
     * Class constructor to create a <code>RemoteClient</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name.
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
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/28/2001  INB	Created.
     *
     */
    RemoteClient(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
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
     * 12/04/2001  INB	Created.
     *
     */
    public final void addNotification(AwaitNotification anI) {
	awaiting.addElement(anI);
    }

    /**
     * Delivers a <code>RoutedMessage</code> to its target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @param offsetI  the offset to the current level of the target.
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
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 11/28/2001  INB	Created.
     *
     */
    public final Serializable deliver
	(RoutedMessage messageI,
	 int offsetI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializableR = null;
	if (offsetI < messageI.getTarget().length()) {
	    serializableR = Language.exception
		(new com.rbnb.api.AddressException
		    (getFullName() + " cannot find path to " +
		     messageI.getTarget() + "."));

	} else if ((awaiting != null) && (awaiting.size() > 0)) {
	    post(messageI.getMessage());
	    serializableR = Language.ping();

	} else {
	    synchronized (this) {
		while (message != null) {
		    wait(TimerPeriod.LONG_WAIT);
		}

		message = messageI.getMessage();
		notify();
	    }

	    serializableR = Language.ping();
	}

	return (serializableR);
    }

    /**
     * Gets the <code>RemoteClientOwner</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RemoteClientOwner</code>.
     * @see #setOwner(com.rbnb.api.RemoteClientOwner)
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    private final RemoteClientOwner getOwner() {
	return (owner);
    }

    /**
     * Gets the receiver thread, if any.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the receiver thread.
     * @see #setReceiver(java.lang.Thread)
     * @since V1.0
     * @version 01/02/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2002  INB	Created.
     *
     */
    private final Thread getReceiver() {
	return (receiver);
    }

    /**
     * Gets the name of the remote object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setRemoteName(String)
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    private final String getRemoteName() {
	return (remoteName);
    }

    /**
     * Gets the <code>Sink</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Sink</code>.
     * @see #setSink(com.rbnb.api.Sink)
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    private final Sink getSink() {
	return (sink);
    }

    /**
     * Gets the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Username</code>.
     * @see #setUsername(com.rbnb.api.Username usernameI)
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public final Username getUsername() {
	return (username);
    }

    /**
     * Notifies all objects awaiting notification of the arrival of an "event"
     * <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> event.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/12/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2004  INB	If there is more than one receiver, clone the message
     *			for all but the last one.
     * 12/04/2001  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronized (awaiting) {
	    for (int idx = 0; idx < awaiting.size(); ++idx) {
		AwaitNotification an = (AwaitNotification)
		    awaiting.elementAt(idx);

		try {
		    if (idx == (awaiting.size() - 1)) {
			((StreamListener) an.getTo()).post(serializableI);
		    } else {
			((StreamListener) an.getTo()).post
			    ((Serializable) serializableI.clone());
		    }
		} catch (com.rbnb.api.AddressException e) {
		    throw new com.rbnb.api.SerializeException
			(e.getMessage());
		}
	    }
	}
	
    }

    /**
     * Receives a message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the message <code>Serializable</code>.
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
     * @version 04/01/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/01/2003  INB	Allow this to be interrupted with a
     *			<code>notify</code>.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 11/28/2001  INB	Created.
     *
     */
    public final Serializable receive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable messageR;

	synchronized (this) {
	    while ((getReceiver() != null) &&
		   (getReceiver() != Thread.currentThread())) {
		wait(TimerPeriod.LONG_WAIT);
	    }
	    setReceiver(Thread.currentThread());

	    while ((stopThread != Thread.currentThread()) &&
		   (message == null)) {
		wait(TimerPeriod.LONG_WAIT);
	    }

	    messageR = message;
	    message = null;
	    setReceiver(null);
	    notifyAll();
	}

	if (messageR instanceof ExceptionMessage) {
	    Language.throwException((ExceptionMessage) messageR);
	}

	return (messageR);
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
     * @version 12/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    public final void removeNotification(AwaitNotification anI) {
	awaiting.removeElement(anI);
    }

    /**
     * Sends a message to the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> message.
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
     * @see #receive()
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/30/2003  INB	Added firewalls.
     * 11/28/2001  INB	Created.
     *
     */
    final void send(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getParent() == null) {
	    throw new com.rbnb.api.AddressException
		("Cannot send message to illegal remote client - " +
		 "not connected to parent.");
	} else if (rHandler == null) {
	    throw new com.rbnb.api.AddressException
		("Cannot send message to illegal remote client - " +
		 "not connected to routing map.");
	}

	ServerHandler local = rHandler.getLocalServerHandler();
	RoutedMessage rMessage = new RoutedMessage();
	rMessage.setSource(local.getFullName() +
			   PATHDELIMITER +
			   getFullName());
	rMessage.setTarget(getRemoteName());
	rMessage.setPath(((PathFinder) getParent()).findPath());
	if (rMessage.getPath() == null) {
	    throw new com.rbnb.api.AddressException
		("Cannot find path to " + getParent().getFullName() + ".");
	} else if (rMessage.getPath().getOrdered() == null) {
	    throw new com.rbnb.api.AddressException
		("Cannot find legal path to " +
		 getParent().getFullName() +
		 ".");
	}
	rMessage.setMessage(serializableI);
	rMessage.setAtIndex(1);

	if (rMessage.getPath().getOrdered().size() <= rMessage.getAtIndex()) {
	    throw new com.rbnb.api.AddressException
		("Cannot find " + getParent().getFullName() +
		 " in path:\n" + rMessage.getPath());
	}

	String nextName = (String)
	    rMessage.getPath().getOrdered().elementAt(rMessage.getAtIndex());
	RemoteServer rServer =
	    (RemoteServer) ((Rmap) rHandler).findDescendant(nextName,
							    false);
	if (rServer == null) {
	    throw new com.rbnb.api.AddressException
		("Cannot find server " + nextName + " from path:\n" +
		 rMessage.getPath());
	}

	Serializable response = rServer.deliver(rMessage,0);
	if (response instanceof ExceptionMessage) {
	    Language.throwException((ExceptionMessage) response);

	} else if (!(response instanceof Ping)) {
	    throw new com.rbnb.api.SerializeException
		(response + " is not a valid response.");
	}
    }

    /**
     * Sets the <code>RemoteClientOwner</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ownerI the <code>RemoteClientOwner</code>.
     * @see #getOwner()
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    final void setOwner(RemoteClientOwner ownerI) {
	owner = ownerI;
    }

    /**
     * Sets the receiver thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param receiverI the thread that is expecting a response.
     * @see #getReceiver()
     * @since V1.0
     * @version 01/02/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2002  INB	Created.
     *
     */
    private final void setReceiver(Thread receiverI) {
	receiver = receiverI;
    }

    /**
     * Sets the remote name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param remoteNameI the remote name.
     * @see #getRemoteName()
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    private final void setRemoteName(String remoteNameI) {
	remoteName = remoteNameI;
    }

    /**
     * Sets the <code>Sink</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI the <code>Sink</code>.
     * @see #getSink()
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    private final void setSink(Sink sinkI) {
	sink = sinkI;
    }

    /**
     * Sets the <code>Username</code> for this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @see #getUsername()
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public final void setUsername(Username usernameI) {
	username = usernameI;
    }

    /**
     * Starts this <code>RemoteClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
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
     * @see #stop()
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Set/clear startup thread.
     * 04/30/2003  INB	Added firewalls.
     * 03/24/2003  INB	Throw an exception if we give up.
     * 03/20/2003  INB	Add a unique identifier.
     * 11/28/2001  INB	Created.
     *
     */
    final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    startThread = Thread.currentThread();

	    if (getParent() == null) {
		throw new com.rbnb.api.AddressException
		    ("Cannot start illegal remote client - " +
		     "not connected to parent.");
	    }
	    if (getParent() instanceof Shortcut) {
		Rmap pEntry;
		for (pEntry = getParent();
		     !(pEntry instanceof RoutingMapHandler);
		     pEntry = pEntry.getParent()) {
		}
		rHandler = (RoutingMapHandler) pEntry;
	    } else {
		rHandler = ((RemoteServer) getParent()).getRoutingMapHandler();
	    }
	    if (rHandler == null) {
		throw new com.rbnb.api.AddressException
		    ("Cannot start illegal remote client - " +
		     "not connected to routing map.");
	    }

	    String rcName =
		"_RC." + System.currentTimeMillis() + "." +
		rHandler.getLocalName() +
		"_" + getName();
	    rcName = rcName.replace('/','_');
	    if (getParent() instanceof Shortcut) {
		setRemoteName(((Shortcut) getParent()).getDestinationName());
	    } else {
		setRemoteName(getParent().getFullName());
	    }

/*
try {
    System.err.println(getFullName() + " to " + rcName);
} catch (Exception e) {
    e.printStackTrace();
}
*/


	    boolean success = false,
		retry = false;
	    int count = 0;
	    String counter = null;
	    java.lang.Throwable exp = null;
	    while (!success) {
		try {
		    retry = false;
		    setSink(new SinkIO());
		    getSink().setName(rcName);
		    Login login = new Login();
		    login.addChild((Rmap) getSink());
		    login.setUsername(getUsername());
		    send(login);
		    success = true;

		} catch (java.lang.IllegalStateException e) {
		    if (count++ == 0) {
			counter = "_" + count;
		    } else {
			rcName = rcName.substring(counter.length());

			counter = "_" + count;
		    }
		    rcName = counter + "." + rcName;
		    retry = true;

		} catch (java.lang.Throwable e) {
		    exp = e;

		} finally {
		    startThread = null;
		    if (!success && !retry) {
			Rmap lParent = getParent();
			setRemoteName(null);
			stop();
			if (exp != null) {
			    if (exp instanceof java.lang.Exception) {
				Language.throwException
				    ((java.lang.Exception) exp);
			    } else if (exp instanceof java.lang.Error) {
				throw (java.lang.Error) exp;
			    }
			} else if (lParent != null) {
			    throw new com.rbnb.api.AddressException
				("Failed to create remote client on " +
				 lParent.getFullName() +
				 "/" + getName() + "!");
			} else {
			    throw new com.rbnb.api.AddressException
				("Failed to create remote client " +
				 getName() + ".");
			}
		    }
		}
	    }

	    setRemoteName(getRemoteName()  + PATHDELIMITER + rcName);
	} finally {
	    startThread = null;
	}
    }

    /**
     * Stops this <code>RemoteClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
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
     * @see #start()
     * @since V2.0
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Use new <code>stopAwaiting</code> method.
     * 05/23/2003  INB	Interrupt the start thread. Post EOS as needed.
     * 04/01/2003  INB	Replaced use of <code>interrupt</code> with stop thread
     *			logic.
     * 03/18/2003  INB	Don't remove <code>Shortcuts</code>.
     * 11/28/2001  INB	Created.
     *
     */
    final void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (inStop) {
	    return;
	}
	inStop = true;
	java.lang.Exception exp = null;

	if (startThread != null) {
	    startThread.interrupt();
	    Thread.currentThread().yield();
	}

	if (getRemoteName() != null) {
	    if (getReceiver() != null) {
		synchronized (this) {
		    stopThread = getReceiver();
		    setReceiver(null);
		    notifyAll();
		}
	    }

	    try {
		Stop stopIt = new Stop(sink);
		send(stopIt);
	    } catch (java.lang.Exception e) {
		// There is the possibility that the remote connection went
		// down and that is why we are stopping. In that case, we'll
		// end up here. Since all we wanted to do was to ensure that
		// the remote is down, we're happy to continue. We save the
		// exception to push up the line.
		exp = e;
	    }
	    setRemoteName(null);
	    setSink(null);
	}

	// Stop all awaiting threads.
	stopAwaiting();

	try {
	    if (getOwner() != null) {
		RemoteClientOwner rOwner = getOwner();
		setOwner(null);
		rOwner.remoteClientTerminated(exp);
	    }
	} catch (java.lang.Exception e) {
	}

	Rmap lParent;
	if ((lParent = getParent()) != null) {
	    getParent().removeChild(this);
	    if ((lParent.getNchildren() == 0) &&
		!(lParent instanceof PeerServer) &&
		!(lParent instanceof ShortcutHandler) &&
		(lParent.getParent() != null)) {
		lParent.getParent().removeChild(lParent);
	    }
	}
    }

    /**
     * Stops all awaiting threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3.1
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Created.
     *
     */
    public final void stopAwaiting() {
	java.util.Vector lAwaiting = (java.util.Vector) awaiting.clone();
	for (int idx = 0; idx < lAwaiting.size(); ++idx) {
	    ((AwaitNotification) lAwaiting.elementAt(idx)).interrupt();
	}
    }
}
