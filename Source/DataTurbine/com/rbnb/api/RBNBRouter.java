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
 * Server-side client handler object that represents a routing connection from
 * another <bold>RBNB</bold> server.
 * <p>
 * Routing is described in detail in <code>RemoteServer</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/04/2010  MJM  Better error checking on child routes with non-unique names.
 * 08/05/2004  INB	Updated the class documentation and added the link to
 *			RemoteServer.
 * 07/20/2004  INB	Added version information to routing connection log
 *			message.
 * 02/18/2004  INB	In addition to using <code>getConnected</code> to
 *			determine if we can talk back, use
 *			<code>getType</code>. 
 * 02/16/2004  INB	Handle null hierarchy in <code>acceptRouteFrom</code>.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 05/22/2003  INB	Ensure that we do not leave the routing lock engaged
 *			during a wait in <code>acceptRouteFrom</code>.
 * 04/24/2003  INB	Control access to routing so that only one can be
 *			connecting at a time.
 * 04/08/2003  INB	When setting up a peer connection, always simply try to
 *			create a peer.
 * 04/01/2003  INB	Handle reconnects.
 * 03/21/2003  INB	Log exceptions on delivery.
 * 02/28/2003  INB	Routed messages may have their index exceed the length
 *			of the path - assume that they are local. Reject
 *			connections from servers whose names conflict with
 *			ours.
 * 02/26/2003  INB	Relate this <code>RouterHandler</code> to its proper
 *			<code>ConnectedServer</code>. Added
 *			<code>shutdown</code> method.
 * 11/21/2001  INB	Created.
 *
 */
final class RBNBRouter
    extends com.rbnb.api.RBNBController
    implements com.rbnb.api.RouterHandler
{
    /**
     * name of the <code>ConnectedServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */
    String serverName = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    RBNBRouter() {
	super();
    }

    /**
     * Class constructor to build a <code>RBNBRouter</code> for a
     * particular <code>RCO</code> from a <code>RouterInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI	  the <code>RCO</code>.
     * @param routerI the <code>RouterInterface</code>.
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    RBNBRouter(RCO rcoI,RouterInterface routerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(rcoI);
	update(routerI);
    }

    /**
     * Accepts a routing connection from another <code>RBNB</code> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI	 the type of connection:
     *			 <p><ul>
     *			 <li>"PARENT" - accept a connection from a parent
     *			     server,</li>>
     *			 <li>"CHILD" - accept a connection from a child
     *			     server,</li>
     *			 <li>"PEER" - accept a connecion from a peer
     *			     server.</li>
     *			 </ul><p>
     * @param hierarchyI the <code>Rmap</code> hierarchy representing the other
     *			 <bold>RBNB</code>. The hierarchy depends on the type
     *			 of connection: 
     *			 <p><ul>
     *			 <li>type = "PARENT" or type = "PEER" - the hierarchy
     *			     consists of a <code>RoutingMap</code> and
     *			     <code>Servers</code>, or</li>
     *			 <li>type = "CHILD" - the hierarchy consists of a
     *			     single <code>Server</code>.</li>
     *			 </ul><p>
     * @return the <code>Rmap</code> hierarchy representing this side of the
     *	       connection.
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
     * @version 07/20/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/20/2004  INB	Added version information to log message.
     * 02/18/2004  INB	In addition to using <code>getConnected</code> to
     *			determine if we can talk back, use
     *			<code>getType</code>. 
     * 02/16/2004  INB	Handle null hierarchy.
     * 05/22/2003  INB	Ensure that we do not leave the routing lock engaged
     *			while waiting for termination to occur.
     * 04/24/2003  INB	Control access to routing so that only one can be
     *			connecting at a time. Also, ensure that we don't
     *			have a change of address for a connected server.
     * 04/08/2003  INB	When setting up a peer connection, always simply try to
     *			create a peer.
     * 04/01/2003  INB	Handle reconnects.
     * 02/28/2003  INB	Reject connections from servers whose names match our
     *			server's name.
     * 02/26/2003  INB	Add this <code>RouterHandler</code> to the
     *			<code>ConnectedServer's</code> related list.
     * 11/27/2001  INB	Created.
     *
     */
    public final Rmap acceptRouteFrom(String typeI,Rmap hierarchyI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap hierarchyR = null;
	boolean locked = false;

	try {
	    ConnectedServer cServer = null;
	    Rmap entry,
		bottom = hierarchyI.moveToBottom();
	    if ((hierarchyI == null) || (bottom == null)) {
		throw new com.rbnb.api.AddressException
		    ("Cannot accept route from unspecified location.");
	    }
	    
	    // MJM:  following is not reasonable?  /Foo/Foo should be OK, i.e. parent and child names can be same
//	    System.err.println("ParentName: "+getParent().getName()+", bottomName: "+bottom.getName());
/*
	    if (bottom.getName().equals(getParent().getName())) {
		throw new java.lang.IllegalStateException
		    ("Cannot accept " + typeI + " route from " + hierarchyI +
		     "; a naming conflict exists.");
	    }
*/
	    
	    ((ServerHandler) getParent()).lockRouting();
	    locked = true;

	    if (typeI.equalsIgnoreCase("CHILD")) {
		if (((entry = getParent().findChild(hierarchyI)) != null)
//		    && !(entry instanceof ChildServer)		// MJM: child server or not, not legal for dupes
		    ) {
		    throw new java.lang.IllegalStateException
			("Cannot accept " + typeI + " route from " +
			 hierarchyI + "; a naming conflict exists!");

		} else if (entry != null) {
		    ((ServerHandler) getParent()).unlockRouting();
		    locked = false;
		    cServer = (ConnectedServer) entry;
		    synchronized (cServer) {
			while (cServer.getTerminateRequested()) {
			    cServer.wait(TimerPeriod.NORMAL_WAIT);
			}
		    }
		    ((ServerHandler) getParent()).lockRouting();
		    locked = true;
		    if (cServer.getParent() == null) {
			cServer = null;
		    }
//		    System.err.println("cServer.getAddress: "+cServer.getAddress()+", bottomAddr: "+((Server)bottom).getAddress());

		    if ((cServer != null) &&
			cServer.getConnected() &&
			(cServer.getType() != ConnectedServer.ROUTE_OFF)) {
			if (!cServer.getAddress().equals
			    (((Server) bottom).getAddress())) {
			    throw new java.lang.IllegalStateException
				("Cannot accept " + typeI + " route from " +
				 hierarchyI + "; a naming conflict exists.");
			}
			try {
			    Router router = cServer.grabRouter();
			    router.send(new Ping());
			    Serializable response = router.receive
				(ACO.okClass,
				 false,
				 TimerPeriod.PING_WAIT);
			    if (!(response instanceof Ping)) {
				cServer.lostRouting();
				cServer = null;
			    }
			} catch (java.lang.Exception e) {
			    cServer.lostRouting();
			    cServer = null;
			}
		    }
		}

		if (cServer == null) {
		    // Create a <code>ChildServer</code> to represent the
		    // connection to the new child.
		    cServer = new ChildServer
			(hierarchyI.getName(),
			 ((Server) hierarchyI).getAddress());
		    getParent().addChild(cServer);

		    // Start the <code>ChildServer</code> initialization
		    // thread.
		    cServer.setLocalServerHandler((ServerHandler) getParent());
		}
		cServer.start();

	    } else if (typeI.equalsIgnoreCase("PARENT")) {
		// Parent server connections are always accepted without the
		// need to do anything specific.
		cServer = (ConnectedServer) getParent().getParent();

	    } else if (typeI.equalsIgnoreCase("PEER")) {
		// Peer server connections are accepted so long as no such
		// connection already exists.
		cServer =
		    ((ServerHandler) getParent
		     ()).getRoutingMapHandler().createPeer
		    (hierarchyI,
		     null);
		cServer.start();

	    } else {
		throw new java.lang.IllegalStateException
		    ("Cannot accept " + typeI + " route from " + hierarchyI +
		     "; capability is not yet implemented.");
	    }

	    // Relate us to the <code>ConnectedServer</code>.
	    cServer.addRelated(this);
	    serverName = cServer.getFullName();

	    // Log the connection.
	    ServerHandler sHandler = (ServerHandler) getParent();
	    String message = 
		"Accepted " + typeI + " routing connection from " +
		cServer.getFullName() + "\n   at " + cServer.getAddress();
	    if (getRCO().getBuildVersion() == null) {
		message += "\n   running an old build version.";
	    } else {
		message += "\n   running " +
		    getRCO().getBuildVersion() +
		    " from " + getRCO().getBuildDate() + ".";
	    }
	    getLog().addMessage
		(sHandler.getLogLevel(),
		 sHandler.getLogClass(),
		 sHandler.getFullName(),
		 message);

	    // Return the local <bold>RBNB</bold> server full hierarchy.
	    hierarchyR = getParent().newInstance();
	    Rmap parent;
	    for (parent = getParent().getParent();
		 parent != null;
		 parent = parent.getParent()) {
		entry = parent.newInstance();
		entry.addChild(hierarchyR);
		hierarchyR = entry;
	    }

	} finally {
	    if (locked) {
		((ServerHandler) getParent()).unlockRouting();
		locked = false;
	    }
	}

	
	return (hierarchyR);
    }

    /**
     * Deliver a <code>RoutedMessage</code> to its destination.
     * <p>
     * The destination may not be a local one, in which case, the message is
     * passed on to the next server in the <code>Path</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @return the response message, if any.
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
     * @version 02/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/28/2003  INB	Allow for the message index to get larger than the path
     *			size.
     * 11/21/2001  INB	Created.
     *
     */
    public final Serializable deliver(RoutedMessage messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {

	/*
if (messageI.getMessage() instanceof Ask) {
    System.err.println(getFullName() + " failing to deliver " + messageI);
    return (null);
}
if (messageI.getMessage() instanceof EndOfStream) {
    System.err.println(getFullName() + " failing to deliver " + messageI);
    return (null);
}

System.err.println(getFullName() + " delivering " + messageI);
	*/

	Serializable responseR = null;
	messageI.setAtIndex(messageI.getAtIndex() + 1);
	if (messageI.getAtIndex() >= messageI.getPath().getOrdered().size()) {
	    // If we're at the end of the <code>Path</code>, then deliver the
	    // message to a local object.
	    /*
	    if (getLog() != null) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Delivering to a local object:\n" + messageI);
	    }
	    */

	    responseR = deliverLocal(messageI);

	} else {
	    // Otherwise, move the message to the next server in the
	    // <code>Path</code>.
	    /*
	    if (getLog() != null) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Delivering to a remote object:\n" + messageI);
	    }
	    */

	    responseR = deliverRemote(messageI);
	}

	/*
	if (getLog() != null) {
	    getLog().addMessage
		(getLogLevel(),
		 getLogClass(),
		 getName(),
		 "Responding to:\n" + messageI +
		 "\nWith:\n" + responseR);
	}

System.err.println(getFullName() + " responding with " + responseR);
	*/

	return (responseR);
    }

    /**
     * Delivers a <code>RoutedMessage</code> to a local target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @return a response.
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
     * @version 11/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/26/2001  INB	Created.
     *
     */
    public final Serializable deliverLocal(RoutedMessage messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	ServerHandler sHandler = (ServerHandler) getParent();
	RoutingMapHandler rHandler = sHandler.getRoutingMapHandler();

	return (rHandler.deliver(messageI,1));
    }

    /**
     * Delivers a <code>RoutedMessage</code> to a remote target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @return a response.
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
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 03/21/2003  INB	Log exceptions.
     * 11/21/2001  INB	Created.
     *
     */
    public final Serializable deliverRemote(RoutedMessage messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	ServerHandler sHandler = (ServerHandler) getParent();
	RoutingMapHandler rHandler = sHandler.getRoutingMapHandler();
	String nextName = (String)
	    messageI.getPath().getOrdered().elementAt
	    (messageI.getAtIndex());
	RemoteServer rServer = (RemoteServer)
	    ((Rmap) rHandler).findDescendant(nextName,false);
	Serializable serializableR = null;

	if (rServer != null) {
	    serializableR = rServer.deliver(messageI,0);
	    com.rbnb.api.AddressException addrEx =
		new com.rbnb.api.AddressException
		    ("Path from " + sHandler.getFullName() +
		     " to " + nextName + " no longer exists.");
	    serializableR = Language.exception(addrEx);
	    if (getLog() != null) {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     addrEx);
	    }
	}

	return (serializableR);
    }

    /**
     * Gets the log class mask for this <code>RBNBRouter</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final long getLogClass() {
	return (super.getLogClass() | Log.CLASS_RBNB_ROUTER);
    }

    /**
     * Gets the registration list for this <code>RBNBClient</code> matching the
     * input hierarchy.
     * <p>
     * At the moment, the only valid input is an <code>Rmap</code> with the
     * same name as this <code>RBO</code> with an optional child named "...".
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
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (null);
    }

    /**
     * Shuts down this <code>RBNBRouter</code>, removing it from its
     * <code>ConnectedServer's</code> related list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Created.
     *
     */
    public final void shutdown() {
	try {
	    ServerHandler myParent = (ServerHandler) getParent();
	    RoutingMapHandler rmh = myParent.getRoutingMapHandler();
	    ConnectedServer cServer = (ConnectedServer)
		((Rmap) rmh).findDescendant(serverName,false);

	    if (cServer != null) {
		cServer.removeRelated(this);
	    }
	} catch (java.lang.Exception e) {
	}

	super.shutdown();
    }

    /**
     * Updates the information stored about a <code>PeerServer</code> of the
     * local <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateI the <code>PeerUpdate</code> message.
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public final void updatePeer(PeerUpdate peerUpdateI) {
	((ServerHandler) getParent()).getRoutingMapHandler().updatePeer
	    (peerUpdateI);
    }
}
