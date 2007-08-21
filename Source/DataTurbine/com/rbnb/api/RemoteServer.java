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
 * Remote server class.
 * <p>
 * This class represents RBNB DataTurbine servers within the routing map of the
 * local server.  Routing allows sink clients connected to one server to
 * retrieve data from another server in a connected network of servers.
 * <p>
 * There are two types of routes:
 * <p><ol>
 *    <li>Parent-Child routes are bidirectional routes using a hierarchical
 *	  naming scheme to allow clients on either server to retrieve data from
 *	  the other, and</li>
 *    <li>Shortcuts are unidirectional routes that allow clients on the server
 *	  that established the shortcut to see and retrieve data from the other
 *	  server.</li>
 * </ol><p>
 * Objects that are instances of this class (as opposed to one of its
 * subclasses) represent servers that are not directly connected to the local
 * server, but are known to the local server because they are either in its
 * ancestry (parent-child connections) or in the ancestry of a server connected
 * to this server (shortcut to a server in a parent-child relationship).
 * <p>
 * The subclasses of this class include:
 * <p><ul>
 *    <li><code>ConnectedServer</code> is an abstract class representing a
 *	  server directly connected to the local server,</li>
 *    <li><code>PeerServer</code> instances represent servers connected to this
 *	  server via a shortcut,</li>
 *    <li>A single <code>RBNB</code> instance represents the local server,</li>
 *    <li><code>HierarchicalServer</code> is an an abstract class representing
 *	  servers connected to this server in a parent-child relationship,</li>
 *    <li><code>ChildServer</code> instances represent servers that are
 *	  children of this server, and</li>
 *    <li>A single, optional <code>ParentServer</code> instance represents the
 *	  server that is the parent of this one.</li>
 * </ul><p>
 * Both types of routes (parent-child and shortcuts) use a scheme whereby
 * messages are sent by a message source on one server to a message target on
 * another server via a specialized connection termed a "router".  Requests and
 * their responses are different types of messages that can be delivered in
 * this fashion.
 * <p>
 * Routers consist of a client end (<code>RouterHandle</code>), which sends
 * messages from local sources (or from remote sources using the local server
 * as an intermediate stepping stone), connected to a server end
 * (<code>RBNBRouter</code>), which receives those messages and delivers them
 * to a local target or to another router to a further remote  server.  For
 * every route (whether unidirectional or bidirectional), a minimum of two
 * routers need to be maintained - one passing messages from server A to server
 * B, and the other passing them from server B to server A.
 * <p>
 * Messages to be sent from one server to another are wrapped in a special
 * message termed a <code>RoutedMessage</code>, which stores the source and
 * target of the message, along with path the message is taking from the source
 * server to the target server.  As the message is moved along, servers along
 * the path can redirect it as needed to ensure that it reaches its destination
 * by changing the path, but will first try to deliver it using the path chosen
 * by the source server.
 * <p>
 * The following are performed whenever a client on the local server attempts
 * to get information (registration or data) from a remote server:
 * <p><ol>
 *    <li>A <code>RemoteClient</code> is established.  A
 *	  <code>RemoteClient</code> provides a virtual connection from the
 *	  local server to the remote server, where</li>
 *    <li>A <code>RemoteClientHandler</code> is set up,</li>
 *    <li>A sink connection is established between the
 *	  <code>RemoteClientHandler</code> and the remote server.  That
 *	  connection will then be used to actually retrieve data on the remote
 *	  server,</li>
 *    <li>The <code>RemoteClient</code> sends the request to the
 *	  <code>RemoteClientHandler</code>,</li>
 *    <li>The <code>RemoteClientHandler</code> uses its sink connection to make
 *	  the request on the remote server,</li>
 *    <li>The <code>RemoteClientHandler</code> reads the response(s) from the
 *	  sink connection,</li>
 *    <li>The <code>RemoteClientHandler</code> sends each response to the
 *	  <code>RemoteClient</code>,</li>
 *    <li>The <code>RemoteClient</code> delivers each response to the original
 *	  client.</li>
 *    <li>When the transaction is complete, the <code>RemoteClient</code> is
 *	  shut down (if the transaction was something simple like a request for
 *	  registration) or it may be saved for possible future reuse (if the
 *	  transaction was a request/response type request for data).</li>
 * </ol><p>
 * The messages between the <code>RemoteClient</code> and the
 * <code>RemoteClientHandler</code> are sent via routing rather than via a
 * direct socket.  Such messages are delivered using the following:
 * <p><ol>
 *    <li>A <code>RoutedMessage</code> is wrapped around the real message, with
 *	  the source set to the name of the <code>RemoteClient</code> or
 *	  <code>RemoteClientHandler</code> and a destination set to the name of
 *	  the other end,</li>
 *    <li>A <code>Path</code> is found leading from the message source server
 *	  to the message destination server and is added to the
 *	  <code>RoutedMessage</code>,</li>
 *    <li>The message is passed to the local <code>ConnectedServer</code>
 *	  subclass object representing the first server in the path after the
 *	  source server (this can be either the target server or an
 *	  intermediate server),</li> 
 *    <li>A <code>RouterHandle</code> is retrieved from that
 *	  <code>ConnectedServer's</code> list of available
 *	  <code>RouterHandles</code>.  If none are available, the code will
 *	  either connect a new router, wait for an existing one to be put back
 *	  into the available pool, or will report that the message could not be
 *	  delivered,</li>
 *    <li>The <code>RoutedMessage</code> is sent by the
 *	  <code>RouterHandle</code> to the <code>RBNBRouter</code> on the other
 *	  end of the connection,
 *    <li>The <code>RBNBRouter</code> determines whether it is on the target
 *	  server or an intermediate server,</li>
 *    <li>If the <code>RBNBRouter's</code> server isn't the target and the path
 *	  in the message has further entries, then the <code>RBNBRouter</code>
 *	  locates the object representing the next server in the path and
 *	  passes the message to that object for delivery,</li>
 *    <li>If the <code>RBNBRouter's</code> server isn't the target and the path
 *	  has no further entries, then the <code>RBNBRouter</code> attempts to
 *	  find a new path to the target and, if it does, passes the message
 *	  along using that path,</li>
 *    <li>If the <code>RBNBRouter's</code> server is the target, then the
 *	  <code>RBNBRouter</code> attempts to locate the target client handler
 *	  in its server's routing map (<code>RBNBRoutingMap</code>) and
 *	  delivers the message to it.</li>
 *    <li>A simple acknowledgement of receipt is sent back to the
 *	  <code>RouterHandle</code>,</li>
 *    <li>The <code>RouterHandle</code> is released back to the available pool
 *	  and the <code>RemoteClient</code> starts waiting for a response or
 *	  responses or the <code>RemoteClientHandler</code> starts waiting for
 *	  further commands,</li>
 * </ol><p>
 *     
 * @author Ian Brown
 *
 * @see com.rbnb.api.RBNBRouter
 * @see com.rbnb.api.RBNBRoutingMap
 * @see com.rbnb.api.RemoteClient
 * @see com.rbnb.api.RemoteClientHandler
 * @see com.rbnb.api.RoutedMessage
 * @see com.rbnb.api.RouterHandle
 * @since V2.0
 * @version 08/04/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/04/2004  INB	Added routing documentation.
 * 02/19/2004  INB	Ensure that we strip off our parent's name when looking
 *			for a descendant in <code>deliver</code>.
 * 02/16/2004  INB	Throw a <code>DisconnectClientException</code> when
 *			we detect a loop back.
 * 02/13/2004  INB	Ensure that messages do not loop back to their source
 *			if the local client has terminated.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 01/14/2003  INB	Added <code>metricsSyncObj</code> and handling.
 * 05/23/2003  INB	Eliminated extra <code>post</code> call in
 *			<code>addChild</code>.
 * 05/14/2003  INB	Add unique identifier to registration
 *			<code>RemoteClient</code> name.
 * 04/08/2003  INB	Ensure that both the name and address of the peer match
 *			an existing entry.
 * 04/04/2003  INB	We can lose routing to <code>RemoteServers</code>
 *			without it being a real problem right now. Don't bother
 *			to report it.
 * 03/25/2003  INB	Reordered things so that we don't look for a route
 *			until we actually need one in
 *			<code>deliver</code>. Made <code>setConnected</code>
 *			notify waiting threads.
 * 03/21/2003  INB	Log exceptions on delivery.
 * 03/20/2003  INB	Add a unique identifier to the remote client.
 * 03/19/2003  INB	Default to not connected.
 * 02/26/2003  INB	Disconnect routing in <code>stop</code>.
 * 11/20/2001  INB	Created.
 *
 */
class RemoteServer
    extends com.rbnb.api.Server
    implements com.rbnb.api.GetLogInterface,
	       com.rbnb.api.GetServerHandlerInterface,
	       com.rbnb.api.IOMetricsInterface,
	       com.rbnb.api.NotificationFrom,
	       com.rbnb.api.PathFinder,
	       com.rbnb.api.RegisteredInterface,
	       com.rbnb.api.RoutedTarget
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
    private java.util.Vector awaiting = new java.util.Vector();

    /**
     * is there a connection to this server?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/19/2003
     */
    private boolean connected = false;

    /**
     * path find counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private long pathFindCounter = 0;

    /**
     * number of bytes transferred by dead children.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    long metricsDeadBytes = 0;

    /**
     * metrics synchronization object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 01/14/2004
     */
	// 2005/08/25  WHF  Made this object an unnamed inner class which implements
	//  serializable but otherwise has no additional methods.
    final Object metricsSyncObj = new java.io.Serializable() {};

    /**
     * <code>RemoteClient</code> counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/14/2003
     */
    private long remoteClientCount = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
    RemoteServer() {
	super();
    }

    /**
     * Class constructor to build a <code>RemoteServer</code> for a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address provided.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 12/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    RemoteServer(String nameI)
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
     * Class constructor to build a <code>RemoteServer</code> for a name and an
     * address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
     * @param addressI the server's address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address provided.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 12/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    RemoteServer(String nameI,String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI,addressI);
    }

    /**
     * Adds a child <code>Rmap</code> to this <code>RBNB</code>.
     * <p>
     * Posts a notification event to any objects awaiting notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the new child <code>Rmap</code>.
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
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Eliminated extra <code>post</code>. 
     * 12/04/2001  INB	Created.
     *
     */
    public void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.addChild(childI);

	if (childI instanceof ClientHandler) {
	    NewObjectEvent event = new NewObjectEvent(childI);
	    post(event);
	} else {
	    post(childI);
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
     * Calculates the total number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes transferred.
     * @since V2.0
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added synchronization.
     * 11/19/2002  INB	Created.
     *
     */
    public final long bytesTransferred() {
	long bytesR = 0;
	synchronized (metricsSyncObj) {
	    bytesR = metricsDeadBytes;
	}
	try {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof IOMetricsInterface) {
		    IOMetricsInterface ioMetrics =
			(IOMetricsInterface) getChildAt(idx);
		    bytesR += ioMetrics.bytesTransferred();
		}
	    }


	    /*
System.err.println(getFullName() + " " + getClass() + " " + bytesR);
	    */

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}

	return (bytesR);
    }

    /**
     * Creates a <code>PeerServer</code> based on the input hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerHierarchyI the <code>PeerServer's</code> hierarchy.
     * @param peerI	     an existing <code>PeerServer</code> object to
     *			     tie in at the "correct" level.
     * @return the <code>PeerServer</code> created.
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
     * @version 04/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/08/2003  INB	Ensure that both the name and address of the peer match
     *			an existing entry.
     * 12/18/2001  INB	Created.
     *
     */
    public final PeerServer createPeer(Rmap peerHierarchyI,PeerServer peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PeerServer pServerR = null;
	Rmap entry = findChild(peerHierarchyI);

	if (entry != null) {
	    Server sEntry = (Server) entry;
	    if (!sEntry.getAddress().equals
		(((Server) peerHierarchyI).getAddress())) {
		throw new com.rbnb.api.AddressException
		    ("Cannot create entry for " +
		     ((Server) peerHierarchyI).getFullName() +
		     "; its address (" +
		     ((Server) peerHierarchyI).getAddress() +
		     " does not match the existing address of " +
		     sEntry.getAddress());
	    }
	    if (peerHierarchyI.getNchildren() > 0) {
		pServerR = ((RemoteServer) entry).createPeer
		    (peerHierarchyI.getChildAt(0),
		     peerI);

	    } else if (entry instanceof PeerServer) {
		pServerR = (PeerServer) entry;
		if ((peerI != null) && (peerI != pServerR)) {
		    peerI.moveRouters(pServerR);
		    peerI.eliminate();
		}

	    } else {
		if (peerI == null) {
		    pServerR = (PeerServer) peerHierarchyI.newInstance();
		} else {
		    pServerR = peerI;
		    pServerR.setName(entry.getName());
		    PeerServer oPeer = (PeerServer) peerHierarchyI;
		    pServerR.setBuildDate(oPeer.getBuildDate());
		    pServerR.setBuildVersion(oPeer.getBuildVersion());
//		    pServerR.setLicenseString(oPeer.getLicenseString());
		    pServerR.setUpdateCounter(oPeer.getUpdateCounter());
		}
		pServerR.setLocalServerHandler(getLocalServerHandler());
		removeChild(entry);
		addChild(peerI);
		while (entry.getNchildren() > 0) {
		    Rmap child = entry.getChildAt(0);
		    entry.removeChildAt(0);
		    pServerR.addChild(child);
		}
	    }

	} else {
	    entry = this;
	    for (Server iPServer = (Server) peerHierarchyI;
		 iPServer != null;
		 iPServer =
		     ((iPServer.getNchildren() == 0) ?
		      null :
		      (Server) iPServer.getChildAt(0))) {
		if (iPServer.getNchildren() == 0) {
		    if (peerI != null) {
			pServerR = peerI;
		    } else {
			pServerR = (PeerServer) iPServer.newInstance();
		    }
		    pServerR.setLocalServerHandler
			(getLocalServerHandler());
		    entry.addChild(pServerR);

		} else {
		    RemoteServer rServer = new RemoteServer
			(iPServer.getName(),
			 iPServer.getAddress());
		    entry.addChild(rServer);
		    entry = rServer;
		}
	    }
	}

	return (pServerR);
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
     * 02/16/2004  INB	Throw a <code>DisconnectClientException</code> when
     *			we detect a loop back.
     * 02/13/2004  INB	Ensure that messages do not loop back to their source
     *			if the local client has terminated.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 03/25/2003  INB	Reordered things so that we don't look for a route
     *			until we actually need one here.
     * 03/21/2003  INB	Log exceptions.
     * 11/26/2001  INB	Created.
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
	    // Attempt to deliver the message to the actual <bold>RBNB</bold>
	    // server associated with this <code>RemoteServer</code>.
	    com.rbnb.api.AddressException addrEx =
		new com.rbnb.api.AddressException
		    (getRoutingMapHandler().getLocalName() +
		     " attempt to deliver to remote server " +
		     getFullName() + " without a path.\nCannot deliver to " +
		     messageI.getTarget() + ".\nNo path for " + messageI);
	    serializableR = Language.exception(addrEx);
	    if (getLog() != null) {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     addrEx);
	    }

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

		// Look for the local target.
		rTarget = (RoutedTarget) findChild(new Rmap(nextName));
	    }

	    if (rTarget != null) {
		// If the target is local, then deliver it.
		serializableR = rTarget.deliver(messageI,nSlash + 1);

	    } else {
		// If the target isn't here, then deliver it to this
		// <code>RemoteServer</code> via the correct path.
		Path lPath = messageI.getPath();
		Path dRoute = null;

		if ((lPath != null) &&
		    (messageI.getAtIndex() < lPath.getOrdered().size())) {
		    // We'got a path and we're not at the end of it.  Deliver
		    // it to the next part of the path.
		    serializableR = deliver(messageI,-1);

		} else if ((lPath != null) &&
			   (lPath.getOrdered().size() > 1) &&
			   (((String) lPath.getOrdered().elementAt
			     (lPath.getOrdered().size() - 2)).equals
			    (getFullName()))) {
		    // If the previous entry in the path of the message was
		    // the RBNB server represented by this object, then we have
		    // a loop.  This probably means that a local object
		    // shutdown before a message could be delivered to it.
		    com.rbnb.api.DisconnectedClientException addrEx =
			new com.rbnb.api.DisconnectedClientException
			    (getRoutingMapHandler().getLocalName() +
			     ": Local target is missing for " + messageI);
		    serializableR = Language.exception(addrEx);

		} else if ((dRoute = findPath()) == null) {
		    // If we cannot find a path to the server represented by
		    // this object, then we are unable to actually deliver the
		    // message.
		    com.rbnb.api.AddressException addrEx =
			new com.rbnb.api.AddressException
			    (getRoutingMapHandler().getLocalName() +
			     " cannot find path to " +
			     messageI.getTarget() + " via " +
			     getFullName() + ".\nNo path for " + messageI);
		    serializableR = Language.exception(addrEx);
		    if (getLog() != null) {
			getLog().addException
			    (Log.STANDARD,
			     getLogClass(),
			     getName(),
			     addrEx);
		    }

		} else if (messageI.getPath() == null) {
		    // If the message did not have a path, then create one from
		    // the local server to the remote one and deliver the
		    // message.
		    messageI.setPath(dRoute);
		    messageI.setAtIndex(1);
		    serializableR = deliver(messageI,-1);
		    
		} else {
		    // If the message had a path, then we need to figure out
		    // how the local path effects that existing path.
		    if (getFullName().compareTo
			((String) dRoute.getOrdered().elementAt(1)) == 0) {
			// If we're at the representation of the actual end of
			// the path, then we can simply update the original
			// message and deliver that via the new path.
			messageI.setPath(dRoute);
			messageI.setAtIndex(1);
			serializableR = deliver(messageI,-1);

		    } else {
			// Otherwise, we're going to deliver the message to
			// something local.
			RoutedMessage message = new RoutedMessage();
			message.setMessage(messageI.getMessage());
			message.setPath(dRoute);
			message.setAtIndex(1);
			message.setSource(messageI.getSource());
			message.setTarget(messageI.getTarget());

			String targetName = (String)
			    dRoute.getOrdered().elementAt(1);
			String myName = getFullName();
			targetName = targetName.substring
			    (myName.lastIndexOf("/"));
			rTarget = (RoutedTarget) findDescendant
			    (targetName,
			     false);

			if (rTarget != null) {
			    // If the target is actually local, then we can
			    // deliver it now.
			    serializableR = rTarget.deliver(message,-1);

			} else {
			    // Otherwise, we've lost the path.
			    com.rbnb.api.AddressException addrEx =
				new com.rbnb.api.AddressException
				    (getRoutingMapHandler().getLocalName() +
				     " cannot find " +
				     getFullName() + "'s descendant " +
				     dRoute.getOrdered().elementAt(1) +
				     ", cannot find path to " +
				     messageI.getTarget() +
				     ".\nNo path for " + messageI);
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
     * Disconnect anything that depends directly on this server having
     * routing.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/01/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/01/2003  INB	Look for a complete loss of connectivity.
     * 12/17/2001  INB	Created.
     *
     */
    void disconnectedRouting() {
	try {
	    stopAwaiting();
	} catch (java.lang.Exception e) {
	}

	try {
	    java.util.Vector rClients = new java.util.Vector();

	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof RemoteClient) {
		    rClients.addElement(getChildAt(idx));
		}
	    }

	    for (int idx = 0; idx < rClients.size(); ++idx) {
		RemoteClient child = (RemoteClient) rClients.elementAt(idx);

		try {
		    child.stop();
		} catch (java.lang.Exception e1) {
		}
	    }

	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Finds the lowest cost <code>Path</code> from the local server
     * (<code>RBNB</code> object) to this <code>RemoteServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the lowest cost <code>Path</code>.
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
     * @version 11/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2001  INB	Created.
     *
     */
    public Path findPath()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (((PathFinder) getParent()).findPath());
    }

    /**
     * Finds <code>LocalPaths</code> to every <code>PeerServer</code> in the
     * input list (if any) that is reachable.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pathFindCounterI the path find counter value.
     * @param pathToHereI      the <code>LocalPath</code> to this point.
     * @param toFindIO	       the remaining server names to find
     *			       <code>Paths</code> to. If this is
     *			       <code>non-null</code>, then the method adds this
     *			       <code>PeerServer's</code> path if the
     *			       <code>PeerServer</code> is found in this list.
     * @param pathsToCheckIO   The list of <code>LocalPaths</code> to search.
     * @param pathsFoundIO     The list of paths found so far.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Moved declaration of variables up.
     * 12/21/2001  INB	Created.
     *
     */
    void findPaths(long pathFindCounterI,
			 LocalPath pathToHereI,
			 SortedStrings toFindIO,
			 com.rbnb.utility.SortedVector pathsToCheckIO,
			 com.rbnb.utility.SortedVector pathsFoundIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    if (getPathFindCounter() < pathFindCounterI) {
		setPathFindCounter(pathFindCounterI);
		Rmap child;
		Shortcut shortCut;
		LocalPath lPath;

		// If we haven't been here for this path find operation, then
		// we can examine this to see if it is of use to us.

		if (!(this instanceof ServerHandler) &&
		    ((toFindIO == null) || toFindIO.contains(getFullName()))) {
		    // If this is one of <code>PeerServers</code> that we need
		    // a <code>Path</code> to, then note that it has been found
		    // and save the <code>Path</code>.
		    if (toFindIO != null) {
			toFindIO.remove(getFullName());
		    }
		    pathsFoundIO.add(pathToHereI);
		}

		// Add in our parent for a cost of one.
		if (getParent() instanceof RemoteServer) {
		    shortCut = new ShortcutIO();
		    shortCut.setDestinationName(getParent().getFullName());
		    shortCut.setCost(1);
		    lPath = new LocalPath(pathToHereI,shortCut);
		    pathsToCheckIO.add(lPath);
		}

		// Add in any child servers with a cost of one.
		for (int idx = 0; idx < getNchildren(); ++idx) {
		    if ((child = getChildAt(idx)) instanceof Server) {
			shortCut = new ShortcutIO();
			shortCut.setDestinationName(child.getFullName());
			shortCut.setCost(1);
			lPath = new LocalPath(pathToHereI,shortCut);
			pathsToCheckIO.add(lPath);
		    }
		}
	    }

	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.InternalError();
	}
    }

    /**
     * Gets the connected flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>RemoteServer</code> connected?
     * @see #setConnected(boolean)
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
    final boolean getConnected() {
	return (connected);
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
    public Log getLog() {
	Log logR = null;

	if (getParent() instanceof GetLogInterface) {
	    logR = ((GetLogInterface) getParent()).getLog();
	}

	return (logR);
    }

    /**
     * Gets the registration list for this <code>RemoteServer</code> matching
     * the input hierarchy against locally known information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @param requestO	the output request to match against remote
     *			information).
     * @param rmapO	the output <code>Rmap</code> hierarchy.
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
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/18/2002  INB	Created.
     *
     */
    void getLocalRegistered(Rmap requestI,Rmap requestO,Rmap rmapO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (compareNames(requestI) != 0) {
	    throw new java.lang.IllegalArgumentException
		(requestI + " does not have a matching name.");
	}

	for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
	    Rmap request1 = requestI.getChildAt(idx),
		 request2,
		 rmap;

	    if ((request1.compareNames(">...") == 0) ||
		((requestO == null) &&
		 (request1.compareNames("...") == 0))) {
		java.util.Vector rList = new java.util.Vector();
		for (int idx1 = 0; idx1 < getNchildren(); ++idx1) {
		    Rmap child = getChildAt(idx1);
		    request2 = new Rmap(child.getName());
		    request2.addChild(new Rmap(">...",MarkerBlock,null));
		    Rmap nChild = null;
		    if ((child instanceof RemoteServer) ||
			(child instanceof ShortcutHandler)) {
			nChild =
			    ((RegisteredInterface) child).getRegistered
			    (request2);
		    }
		    if (nChild != null) {
			rmapO.addChild(nChild);
		    }
		}

	    } else if (request1.compareNames("..") == 0) {
		request1 = (Rmap) request1.clone();
		request1.setName(getParent().getName());
		Rmap answer =
		    ((RegisteredInterface) getParent()).getRegistered
		    (request1);
		if (answer instanceof EndOfStream) {
		    if (answer.getNchildren() == 0) {
			continue;
		    }
		    answer = answer.getChildAt(0);
		    answer.getParent().removeChildAt(0);
		}
		answer.setName("..");
		rmapO.addChild(answer);

	    } else if (request1.compareNames("...") == 0) {
		if (requestO != null) {
		    requestO.addChild((Rmap) request1.clone());
		}

	    } else if (request1.compareNames("*") == 0) {
		Rmap child,
		    nChild;
		request2 = (Rmap) request1.clone();
		for (int idx2 = 0; idx2 < getNchildren(); ++idx2) {
		    child = getChildAt(idx2);
		    if ((child instanceof RemoteServer) ||
			(child instanceof ShortcutInterface)) {
			request2.setName(child.getName());
			if ((child instanceof RegisteredInterface) &&
			    ((nChild =
			      ((RegisteredInterface)
			       child).getRegistered
			      (request2)) != null)) {
			    rmapO.addChild(nChild);
			}
		    }
		}
		if (requestO != null) {
		    requestO.addChild((Rmap) request1.clone());
		}

	    } else if (((rmap = findChild(request1)) != null) &&
		       (rmap instanceof RegisteredInterface)) {
		if (rmap instanceof RegisteredInterface) {
		    Rmap nChild = ((RegisteredInterface) rmap).getRegistered
			(request1);
		    if (nChild != null) {
			rmapO.addChild(nChild);
		    }
		}

	    } else if (requestO != null) {
		requestO.addChild((Rmap) request1.clone());
	    }
	}
    }

    /**
     * Gets the log class mask for this <code>RemoteServer</code>.
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
    public long getLogClass() {
	return (Log.CLASS_REMOTE_SERVER);
    }

    /**
     * Gets the base log level for this <code>RemoteServer</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
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
    public final byte getLogLevel() {
	return (Log.STANDARD);
    }

    /**
     * Gets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the local <code>ServerHandler</code>.
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    public ServerHandler getLocalServerHandler() {
	ServerHandler localR = null;

	try {
	    if ((getParent() != null) &&
		(getParent() instanceof GetServerHandlerInterface)) {
		localR =
		    ((GetServerHandlerInterface) getParent
		     ()).getLocalServerHandler();
	    }

	} catch (java.lang.Exception e) {
	}

	return (localR);
    }

    /**
     * Gets the path find counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the path find counter value.
     * @see #setPathFindCounter(long)
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final long getPathFindCounter() {
	return (pathFindCounter);
    }

    /**
     * Gets the registration list for this <code>RemoteServer</code> matching
     * the input hierarchy.
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
     * @version 05/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2003  INB	Use unique identifier in name.
     * 04/01/2003  INB	Handle <code>null</code> return from remote by
     *			returning an empty map. Never return
     *			<code>null</code>.
     * 03/20/2003  INB	Add a unique identifier to the remote client.
     * 12/14/2001  INB	Created.
     *
     */
    public Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = newInstance(),
	     request = new Rmap(".");

	getLocalRegistered(requestI,request,rmapR);

	if (request.getNchildren() > 0) {
	    RemoteClient rClient = new RemoteClient
		("_RC.gr." + System.currentTimeMillis() +
		 "." + (++remoteClientCount) +
		 "." + Thread.currentThread().getName());
	    addChild(rClient);
	    try {
		rClient.start();

		Ask ask = new Ask(Ask.REGISTERED,request);
		rClient.send(ask);
		Rmap rmap = (Rmap) rClient.receive();
		rClient.stop();
		removeChild(rClient);

		if (rmap != null) {
		    rmap = rmap.getChildAt(0);
		    rmap.getParent().removeChildAt(0);
		    Rmap entry,
			entry2;
		    if (rmap instanceof PeerServer) {
			while (rmapR.getNchildren() > 0) {
			    entry = rmapR.getChildAt(0);
			    rmapR.removeChildAt(0);
			    if ((entry2 = rmap.findChild(entry)) == null) {
				rmap.addChild(entry);
			    } else {
				if (this instanceof ParentServer) {
				    rmap.removeChild(entry2);
				    rmap.addChild(entry);
				}
			    }
			}
			rmap.setName(rmapR.getName());
			rmapR = rmap;
		    }
		}

	    } catch (java.lang.Exception e) {
	    }
	}

	return (rmapR);
    }

    /**
     * Gets the <code>RoutingMapHandler</code> for this
     * <code>RemoteServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RoutingMapHandler</code>.
     * @since V2.0
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    RoutingMapHandler getRoutingMapHandler() {
	RoutingMapHandler rmhR = null;

	if (getParent() != null) {
	    if (getParent() instanceof RoutingMapHandler) {
		rmhR = (RoutingMapHandler) getParent();
	    } else {
		rmhR = ((RemoteServer) getParent()).getRoutingMapHandler();
	    }
	}

	return (rmhR);
    }

    /**
     * Lost routing to this <code>RemoteServer</code>.
     * <p>
     * This method should be overridden for <code>RemoteServers</code> that
     * need special handling.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    void lostRouting() {
	if (!getConnected()) {
	    return;
	}
	setConnected(false);

	disconnectedRouting();
    }

    /**
     * Notifies all objects awaiting notification of the arrival of an "event"
     * <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the "event" <code>Serializable</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the post operation is interrupted.
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
    public final void post(Serializable eventI)
        throws java.lang.InterruptedException
    {
	synchronized (awaiting) {
	    for (int idx = 0; idx < awaiting.size(); ++idx) {
		AwaitNotification an = (AwaitNotification)
		    awaiting.elementAt(idx);

		an.addEvent(eventI,false);
	    }
	}
    }

    /**
     * Regained routing to this <code>RemoteServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #lostRouting()
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
    void regainedRouting() {
	setConnected(true);

	if (getParent() instanceof RemoteServer) {
	    ((RemoteServer) getParent()).regainedRouting();
	}
    }

    /**
     * Removes a child <code>Rmap</code> from this <code>Rmap</code>.
     * <p>
     * Posts a notification event to any objects awaiting notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI the child to remove.
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
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #getParent()
     * @see #removeChildAt(int)
     * @see #setParent(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2000  INB	Created.
     *
     */
    public void removeChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (childI.getParent() == this) {
	    super.removeChild(childI);
	    post(childI);
	}
    }

    /**
     * Removes the child <code>Rmap</code> at a particular index from this
     * <code>Rmap</code>.
     * <p>
     * Posts a notification event to any objects awaiting notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of child to remove.
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
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #getParent()
     * @see #removeChild(com.rbnb.api.Rmap)
     * @see #setParent(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2000  INB	Created.
     *
     */
    public void removeChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap child = getChildAt(indexI);
	super.removeChildAt(indexI);
	post(child);
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
     * Sets the connected flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param connectedI is this <code>RemoteServer</code> connected?
     * @see #getConnected()
     * @since V2.0
     * @version 03/25/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/25/2003  INB	Made synchronized and added <code>notifyAll</code>.
     * 12/17/2001  INB	Created.
     *
     */
    final synchronized void setConnected(boolean connectedI) {
	connected = connectedI;
	notifyAll();
    }

    /**
     * Sets the path find counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pathFindCounterI the new path find counter value.
     * @see #getPathFindCounter()
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final void setPathFindCounter(long pathFindCounterI) {
	pathFindCounter = pathFindCounterI;
    }

    /**
     * Disconnects this server and its descendent servers.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @since V2.0
     * @version 04/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2003  INB	We can lose routing to <code>RemoteServers</code>
     *			without it being a real problem right now. Don't bother
     *			to report it.
     * 02/26/2003  INB	Disconnect all routing. If there is no
     *			<code>Path</code> to this <code>RemoteServer</code>,
     *			then we remove this from its parent.
     * 01/02/2002  INB	Created.
     *
     */
    public void stop() {
	try {
	    // Note that we've lost routing.
	    setConnected(false);
	    disconnectedRouting();

	    // Ensure that any <code>RemoteServers</code> are closed down.
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof RemoteServer) {
		    RemoteServer rServer = (RemoteServer) getChildAt(idx);

		    rServer.stop();
		    removeChildAt(idx);
		    metricsDeadBytes += rServer.bytesTransferred();
		    --idx;
		}
	    }

	    try {
		if (findPath() == null) {
		    Rmap lParent = getParent();
		    if (getNchildren() == 0) {
			/*
			 * INB 04/04/2003
			 * If we ever want to go back to true peer to peer
			 * routing, then we may need to look into some of the
			 * reasons for getting to this point. However, at this
			 * time, with shortcuts, we don't need to worry about
			 * it.
			 */
			/*
			ServerHandler sHandler =
			    getRoutingMapHandler().getLocalServerHandler();
			sHandler.getLog().addMessage
			    (getLogLevel(),
			     getLogClass(),
			     getFullName(),
			     "Lost all paths to remote server. " +
			     "Removing remote server from routing map.");
			*/
			lParent.removeChild(this);
		    }
		    if (lParent instanceof RemoteServer) {
			RemoteServer rParent = (RemoteServer) lParent;
			if (getParent() == null) {
			    rParent.metricsDeadBytes += bytesTransferred();
			}
			rParent.lostRouting();
		    } else if (lParent instanceof RBNBRoutingMap) {
			if (getParent() == null) {
			    RBNBRoutingMap rParent = (RBNBRoutingMap) lParent;
			    rParent.metricsDeadBytes += bytesTransferred();
			}
		    }
		}

	    } catch (java.lang.Exception e) {
	    }

	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Stops the objects in the <code>AwaitNotification</code> list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    final void stopAwaiting() {
	if (awaiting.size() > 0) {
	    // Clear the awaiting notification list.
	    java.util.Vector anv = (java.util.Vector) awaiting.clone();

	    for (int idx = 0; idx < anv.size(); ++idx) {
		Interruptable an = (Interruptable) awaiting.elementAt(idx);

		an.interrupt();
	    }
	}
    }
}
