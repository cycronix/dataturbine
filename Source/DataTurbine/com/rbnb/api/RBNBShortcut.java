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
 * Server-side implementation of a shortcut connection from one server to
 * another.
 * <p>
 * Shortcuts are a form of routing that allow sink clients on one RBNB
 * DataTurbine server (the one with the shortcut) to get data from or via a
 * remote DataTurbine server (the target of the shortcut).  Routing is
 * described in more detail in <code>RemoteServer</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RemoteServer
 * @see com.rbnb.api.Shortcut
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
 * 08/05/2004  INB	Updated documentation.
 * 04/27/2004  INB	Instead of waiting for ready in
 *			<code>getRegistered</code>, simply return no contents
 *			if the connection is not ready.
 * 02/19/2004  INB	Ensure that we strip off our parent's name when looking
 *			for a descendant in <code>deliver</code>.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 04/08/2003  INB	Make sure that the reverse route is to a
 *			fully-qualified name.
 * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
 * 03/24/2003  INB	Added code to indicate whether the
 *			<code>Shortcut</code> is ready for use.
 * 03/21/2003  INB	Added exception reporting on delivery. Added
 *			<code>getLogLevel/Class</code> methods.
 * 03/18/2003  INB	Clone the request and use the clone rather than the
 *			request to get data from the remote system.
 * 02/26/2003  INB	Failures to initiate routes result in the destruction
 *			of the <code>Shortcut</code>. Also added handling of
 *			<code>null</code> pointers. Don't broadcast updates for
 *			passive connections when starting up.
 * 01/04/2002  INB	Created.
 *
 */
final class RBNBShortcut
    extends com.rbnb.api.ShortcutIO
    implements com.rbnb.api.ShortcutHandler
{
    /**
     * is this <code>Shortcut</code> ready for use?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/24/2003
     */
    private boolean readyForUse = false;

    /**
     * list of objects awaiting update notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/04/2002
     */
    private java.util.Vector awaiting = new java.util.Vector();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
     *
     */
    RBNBShortcut() {
	super();
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
     * @version 01/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
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
     * @version 02/19/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2004  INB	Ensure that we strip off our parent's name when looking
     *			for a descendant.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 03/21/2003  INB	Log delivery failures.
     * 01/10/2002  INB	Created.
     *
     */
    public Serializable deliver(RoutedMessage messageI,int offsetI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializableR = null;

	if (offsetI == -1) {
	    throw new com.rbnb.api.AddressException
		(messageI + " cannot be delivered to a shortcut object.");

	} else {
	    // Attempt to deliver the message to something that may be local.
	    // Determine the name of the next level.
	    RoutedTarget rTarget = null;
	    int nSlash = -1;

	    if (offsetI < messageI.getTarget().length()) {
		nSlash = messageI.getTarget().indexOf(PATHDELIMITER,offsetI);

		String nextName;
		if (nSlash == -1) {
		    nSlash = messageI.getTarget().length();
		}
		nextName = messageI.getTarget().substring(offsetI,nSlash);

		// Find the target.
		rTarget = (RoutedTarget) findChild(new Rmap(nextName));
	    }

	    if (rTarget != null) {
		// If the target is local, then deliver it.
		serializableR = rTarget.deliver(messageI,nSlash + 1);

	    } else {
		// If the target isn't here, then deliver it to this
		// <code>RemoteServer</code> via the correct path.
		Path dRoute = findPath();

		if (dRoute == null) {
		    com.rbnb.api.AddressException addrEx =
			new com.rbnb.api.AddressException
			   (getFullName() +
			    " cannot find path to " +
			    messageI.getTarget()  +
			    ".\nNo path for " + messageI);
		    serializableR = Language.exception(addrEx);
		    if (getLog() != null) {
			getLog().addException
			    (Log.STANDARD,
			     getLogClass(),
			     getName(),
			     addrEx);
		    }

		} else if ((messageI.getPath() != null) &&
			   (messageI.getAtIndex() <
			    messageI.getPath().getOrdered().size())) {
		    serializableR = deliver(messageI,-1);

		} else if (messageI.getPath() == null) {
		    messageI.setPath(dRoute);
		    messageI.setAtIndex(1);
		    serializableR = deliver(messageI,-1);
		    
		} else {
		    RoutedMessage message = new RoutedMessage();
		    message.setMessage(messageI.getMessage());
		    message.setPath(dRoute);
		    message.setAtIndex(1);
		    message.setSource(messageI.getSource());
		    message.setTarget(messageI.getTarget());
		    if (getFullName().compareTo
			((String) dRoute.getOrdered().elementAt(1)) == 0) {
			messageI.setPath(dRoute);
			messageI.setAtIndex(1);
			serializableR = deliver(messageI,-1);
		    } else {
			String targetName = (String)
			    dRoute.getOrdered().elementAt(1);
			String myName = getFullName();
			targetName = targetName.substring
			    (myName.lastIndexOf("/"));
			rTarget = (RoutedTarget) findDescendant
			    (targetName,
			     false);
			if (rTarget != null) {
			    serializableR = rTarget.deliver(message,-1);
			} else {
			    com.rbnb.api.AddressException addrEx =
				new com.rbnb.api.AddressException
				    (getFullName() +
				     " cannot find descendent " +
				     dRoute.getOrdered().elementAt(1) +
				     ".\nNo path for " +
				     messageI + ".");
			    serializableR = Language.exception(addrEx);

			    if (getLog() != null) {
				getLog().addException
				    (Log.STANDARD,
				     getLogClass(),
				     getName(),
				     addrEx);
			    }
			}
		    }
		}
	    }
	}

	return (serializableR);
    }

    /**
     * Finds a <code>Path</code> from the local <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Path</code> found.
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
     * @version 03/25/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/25/2003  INB	Wait for ready to use to be set. Throw an exception if
     *			the target isn't found at all.
     * 01/04/2002  INB	Created.
     *
     */
    public final Path findPath()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Path pathR = null;

	if (waitReadyForUse()) {
	    PeerServer myParent = (PeerServer) getParent(),
		       target = (PeerServer) (myParent.getRoutingMapHandler
					      ().findDescendant
					      (getDestinationName(),
					       false));

	    if (target != null) {
		pathR = getLocalServerHandler().findPathTo(target);

	    } else {
		throw new com.rbnb.api.AddressException
		    ("Cannot find destination " +
		     getDestinationName() + " for shortcut " +
		     getFullName() + ".");
	    }
	}

	return (pathR);
    }

    /**
     * Gets this <code>ServerHandler</code> for this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2002  INB	Created.
     *
     */
    public final ServerHandler getLocalServerHandler() {
	ServerHandler sHandlerR = null;

	if ((getParent() != null) &&
	    (getParent() instanceof GetServerHandlerInterface)) {
	    sHandlerR =
		((GetServerHandlerInterface)
		 getParent()).getLocalServerHandler();
	}

	return (sHandlerR);
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
	Log logR = null;

	if (getParent() instanceof GetLogInterface) {
	    logR = ((GetLogInterface) getParent()).getLog();
	}

	return (logR);
    }

    /**
     * Gets the log class mask for this <code>RBNBShortcut</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.1
     * @version 03/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2003  INB	Created.
     *
     */
    public final long getLogClass() {
	return (Log.CLASS_RBNB_SHORTCUT);
    }

    /**
     * Gets the base log level for this <code>RBNBClient</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.1
     * @version 03/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2003  INB	Created.
     *
     */
    public final byte getLogLevel() {
	return ((getName() == null) ? Log.STANDARD :
		(getName().charAt(0) == '_') ? Log.STANDARD + 100 :
		Log.STANDARD);
    }

    /**
     * Gets the ready for use flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>Shortcut</code> ready for use?
     * @see #setReadyForUse(boolean)
     * @since V2.1
     * @version 03/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2003  INB	Created.
     *
     */
    final boolean getReadyForUse() {
	return (readyForUse);
    }

    /**
     * Gets the registration list for this object matching the input hierarchy.
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
     * @version 04/27/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2004  INB	Return no contents if the <code>Shortcut</code> is
     *			not ready.
     * 03/24/2003  INB	Wait for the <code>Shortcut</code> to be ready.
     * 03/18/2003  INB	Clone the request and use the clone rather than the
     *			request to get data from the remote system.
     * 01/10/2002  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;

	if (getReadyForUse()) {
	    if (compareNames(requestI) != 0) {
		throw new java.lang.IllegalArgumentException
		    (requestI + " does not have a matching name.");

	    } else if (getActive() == ACTIVE) {
		if ((requestI.getNchildren() == 1) &&
		    (requestI.getChildAt(0).getName().equals(">..."))) {
		    rmapR = newInstance();

		} else {
		    Rmap peerMap =
			((ServerHandler) getParent
			 ()).getRoutingMapHandler().findDescendant
			(getDestinationName(),
			 false);

		    if (peerMap != null) {
			Shortcut shortcut = (Shortcut) newInstance();
			rmapR = (Rmap) shortcut;
			PeerServer peer = (PeerServer) peerMap;
			Rmap request = (Rmap) requestI.clone();
			request.setName(createFromName
					(getDestinationName()).moveToBottom
					().getName());
			Rmap answer = peer.getRegistered(request);

			if (answer != null) {
			    Rmap child;
			    while (answer.getNchildren() > 0) {
				child = answer.getChildAt(0);
				answer.removeChildAt(0);
				rmapR.addChild(child);
			    }
			}
		    }
		}
	    }
	}

	return (rmapR);
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
     * @version 01/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
     *
     */
    public void post(Serializable serializableI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronized (awaiting) {
	    for (int idx = 0; idx < awaiting.size(); ++idx) {
		AwaitNotification an = (AwaitNotification)
		    awaiting.elementAt(idx);

		an.addEvent(serializableI,false);
	    }
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
     * @version 01/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
     *
     */
    public final void removeNotification(AwaitNotification anI) {
	awaiting.removeElement(anI);
    }

    /**
     * Reversed a route connection.
     * <p>
     * This method is called whenever an attempt is made to create a reverse
     * connection from the child to the parent on the parent's behalf or by a
     * peer on the other side of a shortcut and the other side succeeds.  The
     * input <code>PeerServer</code> object describes what has been reversed,
     * while the <code>RCO</code> represents the connection.
     * <p>
     * This method is responsible for creating a <code>Router</code> on this
     * end using an <code>ACO</code> built from the provided <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI     the <code>RCO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    public final void reversed(RCO rcoI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PeerServer pServer = (PeerServer)
	    ((ServerHandler) getParent
	     ()).getRoutingMapHandler().findDescendant
	    (getDestinationName(),
	     false);

	pServer.reversed(rcoI,true);
    }

    /**
     * Starts this <code>ShortcutHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
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
     *		  thrown if the start is interrupted.
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
     * 04/08/2003  INB	Added code to fully-qualify the server object created
     *			to initiate the reverse route. This ensures that a
     *			search for the real object in the
     *			<code>RoutingMap</code> will succeed.
     * 03/24/2003  INB	Sets the ready for use flag.
     * 02/26/2003  INB	Failures to initiate routes result in the destruction
     *			of the <code>Shortcut</code>. Only broadcast an update
     *			for an active connection.
     * 01/08/2002  INB	Created.
     *
     */
    public final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server serverHierarchy = null,
	    bottom,
	    local = null;

	if (getDestinationAddress().compareTo
	    (((ServerHandler) getParent()).getAddress()) == 0) {
	    local = (Server) getParent();

	} else if (getDestinationName() == null) {
	    serverHierarchy = new Server(null,getDestinationAddress());
	    
	} else {
	    serverHierarchy = ((Server) createFromName
			       (getDestinationName(),
				new Server()));
	    for (bottom = serverHierarchy;
		 bottom.getNchildren() != 0;
		 bottom = (Server) bottom.getChildAt(0)) {
	    }
	    bottom.setAddress(getDestinationAddress());
	    local = (Server)
		((ServerHandler)
		 getParent()).getRoutingMapHandler().findDescendant
		(getDestinationName(),
		 (getActive() == PASSIVE));
	}

	PeerServer lPeer = null;
	if (getActive() == ACTIVE) {
	    if (local == getParent()) {
		lPeer = (PeerServer) local;
	    } else {
		try {
		    lPeer = (PeerServer)
			((ServerHandler) getParent()).initiateRouteTo
			(serverHierarchy,
			 local);
		} catch (java.lang.Exception e) {
		    stop();
		    Language.throwException(e);
		}
	    }

	    setDestinationName(lPeer.getFullName());
	    lPeer.setActiveShortcuts(lPeer.getActiveShortcuts() + 1);
	} else {
	    lPeer = (PeerServer) local;
	    lPeer.setPassiveShortcuts(lPeer.getPassiveShortcuts() + 1);
	}
	lPeer.setLocalServerHandler((ServerHandler) getParent());

	if (lPeer == getParent()) {
	    setReadyForUse(true);

	} else {
	    ((RBNB) getParent()).findPaths(null);

	    if (getActive() != ACTIVE) {
		setReadyForUse(true);

	    } else {
		((RBNB) getParent()).setUpdateCounter
		    (((RBNB) getParent()).getUpdateCounter() + 1);
		((RBNB) getParent()).broadcastUpdate
		    (new PeerUpdate((PeerServer) getParent()));
		Shortcut reverseSC = new ShortcutIO();
		reverseSC.setName("_R." + getName());
		reverseSC.setActive(PASSIVE);
		reverseSC.setDestinationName(getParent().getFullName());
		reverseSC.setDestinationAddress
		    (((Server) getParent()).getAddress());
		reverseSC.setCost(PASSIVE_COST);
		Router router = lPeer.grabRouter();
		router.start(reverseSC);
		lPeer.releaseRouter(router);

		Rmap pServer = lPeer.newInstance(),
		    top = pServer,
		    up,
		    above;
		for (up = lPeer.getParent();
		     (up instanceof Server);
		     up = up.getParent()) {
		    above = up.newInstance();
		    above.addChild(top);
		    top = above;
		}
		pServer.addChild((Rmap) reverseSC);
		ReverseRoute reverse = new ReverseRoute(pServer);
		ServerHandler sHandler = (ServerHandler) getParent();
		Serializable response = sHandler.initiateReverseRoute(reverse);
		if (!(response instanceof ExceptionMessage)) {
		    setReadyForUse(true);

		} else {
		    try {
			getLog().addException
			    (Log.STANDARD,
			     sHandler.getLogClass(),
			     getName(),
			     ((ExceptionMessage) response).toException());
		    } catch (java.lang.Exception e) {
		    }
		    stop();
		}
	    }
	}
    }

    /**
     * Sets the ready for use flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param readyForUseI <code>Shortcut</code> ready for use?
     * @see #getReadyForUse()
     * @since V2.1
     * @version 03/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2003  INB	Created.
     *
     */
    final synchronized void setReadyForUse(boolean readyForUseI) {
	readyForUse = readyForUseI;
	notifyAll();
    }

    /**
     * Stops this <code>ShortcutHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @see #start()
     * @since V2.0
     * @version 03/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2003  INB	Clear the ready for use flag.
     * 02/26/2003  INB	Handle <code>null</code> destination name.
     * 01/08/2002  INB	Created.
     *
     */
    public final void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	setReadyForUse(false);

	RoutingMapHandler rMap =
	    ((ServerHandler) getParent()).getRoutingMapHandler();
	PeerServer pServer = null;
	if (getDestinationName() != null) {
	    Rmap rmap = rMap.findDescendant(getDestinationName(),false);
	    if (rmap == null) {
		throw new com.rbnb.api.AddressException
		    (getDestinationName() + " not found.");
	    } else if (!(rmap instanceof PeerServer)) {
		throw new com.rbnb.api.AddressException
		    (getDestinationName() +
		     " does not represent a server known to this server.");
	    }

	    pServer = (PeerServer) rmap;
	    if (getActive() == ACTIVE) {
		if (pServer != getParent()) {
		    try {
			Shortcut reverse = new ShortcutIO();
			reverse.setName("_R." + getName());
			reverse.setActive(PASSIVE);
			Router router = pServer.grabRouter();
			router.stop(reverse);
			pServer.releaseRouter(router);
		    } catch (java.lang.Exception e) {
		    }
		}
		pServer.setActiveShortcuts(pServer.getActiveShortcuts() - 1);
	    } else {
		pServer.setPassiveShortcuts(pServer.getPassiveShortcuts() - 1);
	    }

	    if ((pServer != getParent()) &&
		!(pServer instanceof HierarchicalServer) &&
		(pServer.getActiveShortcuts() +
		 pServer.getPassiveShortcuts() == 0)) {
		pServer.stop();

	    }
	}

	ServerHandler sHandler = (ServerHandler) getParent();
	sHandler.removeChild(this);
	setReadyForUse(false);

	if (pServer != sHandler) {
	    sHandler.findPaths(null);
	    ((RBNB) sHandler).setUpdateCounter
		(((RBNB) sHandler).getUpdateCounter() + 1);
	    ((RBNB) sHandler).broadcastUpdate
		(new PeerUpdate((PeerServer) sHandler));
	}
    }

    /**
     * Waits for the ready for use flag to be set.
     * <p>
     * If the <code>Shortcut</code> is terminated before the flag gets set,
     * then return <code>false</code>.
     *
     * @author Ian Brown
     *
     * @return is the <code>Shortcut</code> usable?
     * @exception java.lang.InterruptedException
     *		  throw if this operation is interrupted.
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2003  INB	Created.
     *
     */
    final synchronized boolean waitReadyForUse()
	throws java.lang.InterruptedException
    {
	while (!readyForUse && (getParent() != null)) {
	    wait(TimerPeriod.LONG_WAIT);
	}

	return (getParent() != null);
    }
}
