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
 * Server-side object that represents the map of all known RBNB servers that
 * have been routed together.
 * <p>
 * The RBNB server hierarchy is anchored at an object termed a "routing map",
 * represented by an instance of this class.  The structure of the hierarchy is
 * shown below (note that all of the children are optional, although exactly
 * one RBNB object must appear somewhere in the hierarchy.  [...] indicates
 * that children have been omitted):
 * <p><dl>
 *    <dd>RBNBRoutingMap<dl>
 *        <dd>ParentServer<dl>
 *            <dd>RBNB [...]
 *            <dd>RBNBRouter(s)
 *        </dl>
 *        <dd>PeerServer<dl>
 *            <dd>RBNBRouter(s)
 *            <dd>RemoteServer(s) [...]
 *        </dl>
 *        <dd>RBNB<dl>
 *            <dd>ChildServer(s)<dl>
 *                <dd>RBNB [...]
 *                <dd>RBNBRouter(s)
 *            </dl>
 *            <dd>NBO(s) [...]
 *            <dd>RBNBController(s)
 *            <dd>RBNBPlugIns
 *            <dd>RBO(s) [...]
 *            <dd>RouterHandle(s)
 *	      <dd>RBNBShortcut(s)
 *        </dl>
 *        <dd>RemoteServer(s)<dl>
 *	      <dd>ParentServer [...]
 *            <dd>RBNB [...]
 *            <dd>RemoteServer(s) [...]
 *        </dl>
 *    </dl>
 * </dl><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.NBO
 * @see com.rbnb.api.ParentServer
 * @see com.rbnb.api.PeerServer
 * @see com.rbnb.api.RBNB
 * @see com.rbnb.api.RBNBController
 * @see com.rbnb.api.RBNBPlugIn
 * @see com.rbnb.api.RBNBRouter
 * @see com.rbnb.api.RBNBShortcut
 * @see com.rbnb.api.RBO
 * @see com.rbnb.api.RouterHandle
 * @see com.rbnb.api.RemoteServer
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
 * 08/04/2004  INB	Added documentation.
 * 02/18/2004  INB	Allow for <code>null</code> server handler in
 *			<code>getRegistered</code>.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 01/15/2004  INB	Added name to the thread that runs this.
 * 01/14/2004  INB	Added <code>metricsSyncObj</code> and handling.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code>.
 *			Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 04/08/2003  INB	Ensure that both the name and address of the peer match
 *			an existing entry.
 * 03/31/2003  INB	Use <code>lostRouting</code> to shutdown
 *			<code>ConnectedServers</code>.
 * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
 * 03/21/2003  INB	Log exceptions on delivery.
 * 03/20/2003  INB	Modified the error message for no path. Use timeout on
 *			<code>wait</code> calls.
 * 02/28/2003  INB	Use the name of the local server for the log channel.
 * 02/27/2003  INB	Make sure we don't eliminate the input peer server when
 *			it is already in the map. If we are going to eliminate
 *			it, also move its routers over to the entry in the map.
 * 05/15/2001  INB	Created.
 *
 */
class RBNBRoutingMap
    extends com.rbnb.api.RoutingMapIO
    implements com.rbnb.api.RoutingMapHandler
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
     * the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/04/2001
     */
    private ServerHandler localServerHandler = null;

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
    final Object metricsSyncObj // = new Object();
							 = new java.io.Serializable() {};

    /**
     * the door for controlling <code>PeerServer</code> additions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private Door peerDoor = null;

    /**
     * list of <code>PeerUpdates</code> waiting to be processed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private java.util.Vector peerUpdates = new java.util.Vector();

    /**
     * the thread running the peer updates.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private Thread thread = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code>.
     * 05/15/2001  INB	Created.
     *
     */
    RBNBRoutingMap() {
	super();
	try {
	    setPeerDoor(new Door("RBNBRoutingMap",Door.STANDARD));
	} catch (java.lang.InterruptedException e) {
	}
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
    public final void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.addChild(childI);
	post(childI);
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
System.err.println(getClass() + " " + bytesR + "\n");
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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 04/08/2003  INB	Ensure that both the name and address of the peer match
     *			an existing entry.
     * 02/27/2003  INB	Make sure we don't eliminate the input peer server
     *			when it is already in the map. If we are going to
     *			eliminate it, also move its routers over to the entry
     *			in the map.
     * 12/18/2001  INB	Created.
     *
     */
    public final PeerServer createPeer(Rmap peerHierarchyI,
				       PeerServer peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PeerServer pServerR = null;

	try {
	    getPeerDoor().lock("RBNBRoutingMap.createPeer");

	    if (peerHierarchyI.getName() == null) {
		pServerR = createPeer(peerHierarchyI.getChildAt(0),peerI);

	    } else {
		Rmap entry = findChild(peerHierarchyI);

		if (entry != null) {
		    Server sEntry = (Server) entry;
		    
		    boolean islocal = ((Server)peerHierarchyI).getAddress().contains("127.0.0.1");	// mjm grope to fix shortcuts
		    
		    if (!islocal && !sEntry.getAddress().equals
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
			    pServerR = (PeerServer)
				peerHierarchyI.newInstance();
			} else {
			    pServerR = peerI;
			    pServerR.setName(entry.getName());
			    PeerServer oPeer = (PeerServer) peerHierarchyI;
			    pServerR.setBuildDate(oPeer.getBuildDate());
			    pServerR.setBuildVersion(oPeer.getBuildVersion());
			    /*
			    pServerR.setLicenseString
				(oPeer.getLicenseString());
			    */
			    pServerR.setUpdateCounter
				(oPeer.getUpdateCounter());
			}
			pServerR.setLocalServerHandler
			    (getLocalServerHandler());
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
	    }
	} finally {
	    getPeerDoor().unlock();
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
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 03/21/2003  INB	Log exceptions.
     * 03/20/2003  INB	Modified the error message for no path.
     * 11/28/2001  INB	Created.
     *
     */
    public final Serializable deliver(RoutedMessage messageI,int offsetI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializableR = null;

	// Determine the name of the next level.
	int nSlash = messageI.getTarget().indexOf(PATHDELIMITER,offsetI);
	String nextName;
	if (nSlash == -1) {
	    nextName = messageI.getTarget().substring(offsetI);
	    nSlash = messageI.getTarget().length();
	} else {
	    nextName = messageI.getTarget().substring(offsetI,nSlash);
	}

	// Find the target.
	RoutedTarget rTarget = (RoutedTarget) findChild(new Rmap(nextName));

	if (rTarget != null) {
	    // If the target is local, pass it on to the next level.
	    serializableR = rTarget.deliver(messageI,nSlash + 1);

	} else {
	    // If the target is not local, see if we can find it via our
	    // default route.
	    Path dRoute = findPath();

	    if (dRoute == null) {
		String message = (getLocalName() +
				  " cannot find a path to " +
				  messageI.getTarget() +
				  ".\n" + nextName +
				  " is not known to this server.\nKnown:\n");
		for (int idx = 0, eIdx = getNchildren();
		     idx < eIdx;
		     ++idx) {
		    if ((getChildAt(idx) instanceof Server) &&
			!(getChildAt(idx) instanceof ServerHandler)) {
			message += "   " + getChildAt(idx).getFullName();
		    }
		}
		com.rbnb.api.AddressException addrEx =
		    new com.rbnb.api.AddressException(message);
		serializableR = Language.exception(addrEx);
		if (getLog() != null) {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 "_RoutingMap",
			 addrEx);
		}

	    } else {
		RoutedMessage message = new RoutedMessage();
		message.setMessage(messageI.getMessage());
		message.setPath(dRoute);
		message.setAtIndex(1);
		message.setSource(messageI.getSource());
		message.setTarget(messageI.getTarget());

		rTarget = (RoutedTarget) findDescendant
		    ((String) dRoute.getOrdered().elementAt(1),
		     false);
		serializableR = rTarget.deliver(message,-1);
	    }
	}

	return (serializableR);
    }

    /**
     * Finds a <code>Path</code> from the local <bold>RBNB</bold> server.
     * <p>
     * The <code>Path</code> always leads to the local <bold>RBNB</bold>
     * server's parent.
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
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2001  INB	Created.
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

	if (getLocalServerHandler().getParent() != this) {
	    pathR =
		((PathFinder) getLocalServerHandler().getParent()).findPath();
	}

	return (pathR);
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
     * 05/04/2001  INB	Created.
     *
     */
    public final ServerHandler getLocalServerHandler() {
	try {
	    if ((localServerHandler == null) && (getLocalName() != null)) {
		localServerHandler = (ServerHandler) findDescendant
		    (getLocalName(),
		     false); 
	    }
	} catch (java.lang.Exception e) {
	}

	return (localServerHandler);
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

	try {
	    logR = getLocalServerHandler().getLog();
	} catch (java.lang.Exception e) {
	}

	return (logR);
    }

    /**
     * Gets the log class mask for this <code>RBNBRoutingMap</code>.
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
	return (Log.CLASS_RBNB_ROUTING_MAP);
    }

    /**
     * Gets the base log level for this <code>RBNBRoutingMap</code>.
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
     * Gets the <code>Door</code> for controlling the creation of
     * <code>PeerServers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @see #setPeerDoor(com.rbnb.api.Door)
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
    public final Door getPeerDoor() {
	return (peerDoor);
    }

    /**
     * Gets the registration list for this <code>DTRoutingMap</code> matching
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
     * @version 02/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Allow for <code>null</code> server handler.
     * 04/25/2001  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = new EndOfStream(),
	    requestW = requestI;
	RBNB local = (RBNB) getLocalServerHandler();
	java.util.Vector rList = new java.util.Vector();
	RoutingMap rtMap = new RoutingMapIO();
	rtMap.setLocalName(getLocalName());
	boolean addRTMap = false;

	if ((requestI.getName() == null) &&
	    (requestI.getNchildren() == 1) &&
	    (requestI.getChildAt(0).getName() == null)) {
	    requestW = requestI.getChildAt(0);
	}

	if ((requestW.compareNames("...") == 0) ||
	    ((requestW.getName() == null) &&
	     ((requestW.getNchildren() == 0) ||
	      ((requestW.getNchildren() == 1) &&
	       (requestW.getChildAt(0).compareNames("...") == 0))))) {
	    // If this is a request for the entire <code>DTRoutingMap</code>,
	    // match all of the servers we can find.
	    boolean expand = ((requestW.compareNames("...") == 0) ||
			      (requestW.getNchildren() > 0));

 	    for (int idx = 0; idx < getNchildren(); ++idx) {
		Rmap entry = getChildAt(idx),
		     request = entry.newInstance();
		if (expand) {
		    request.addChild(new Rmap(">...",MarkerBlock,null));
		}
		rList.addElement(request);
	    }

	} else if (requestW.getName() != null) {
	    // If this is a request for a particular <code>Server</code>,
	    // find it an return it as a match.
	    rList.addElement(requestW);

	} else {
	    // If this is a request for specific <code>Servers</code>, locate
	    // each in turn.
	    for (int idx = 0; idx < requestW.getNchildren(); ++idx) {
		Rmap child = requestW.getChildAt(idx);
		rList.addElement(child);
	    }
	}

	for (int idx = 0; idx < rList.size(); ++idx) {
	    Rmap request = (Rmap) rList.elementAt(idx);

	    if (request.compareNames(".") == 0) {
		if (local != null) {
		    rmapR.addChild(local.getRegistered(request));
		} else {
		    // We got here too early (the server is still being set up
		    // - probably a routing connection.
		    rmapR.addChild(new RBNB(".",""));
		}

	    } else if (request.compareNames("*") == 0) {
		Rmap child,
		    nChild;
		request = (Rmap) request.clone();
		for (int idx2 = 0; idx2 < getNchildren(); ++idx2) {
		    child = getChildAt(idx2);
		    if (child instanceof RegisteredInterface) {
			request.setName(child.getName());
			if ((child instanceof RegisteredInterface) &&
			    ((nChild =
			      ((RegisteredInterface)
			       child).getRegistered
			      (request)) != null)) {
			    rmapR.addChild(nChild);
			}
		    }
		}

	    } else {
		Rmap child = findChild(request);

		if (child != null) {
		    addRTMap = true;
		    Rmap nChild = null;
		    if (child instanceof RegisteredInterface) {
			nChild =
			    ((RegisteredInterface) child).getRegistered
			    (request);
		    }
		    if (nChild != null) {
			rtMap.addChild(nChild);
		    }
		}
	    }
	}

	if (addRTMap) {
	    rmapR.addChild((Rmap) rtMap);
	}

	return (rmapR);
    }

    /**
     * Gets the peer updates thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the peer updates thread.
     * @see #setThread(java.lang.Thread)
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
    private final Thread getThread() {
	return (thread);
    }

    /**
     * Interrupts this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.SecurityException
     *		  thrown if the interrupt is not allowed by the security
     *		  system.
     * @see java.lang.Thread#interrupt()
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
    public final void interrupt() {
	if (getThread() != null) {
	    getThread().interrupt();
	}
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
     * Runs the peer updates task.
     * <p>
     * Whenever a new <code>PeerUpdate</code> is received, this method uses it
     * to update the information stored about the corresponding
     * <code>PeerServer</code>.
     *
     * @author Ian Brown
     *
     * @see #start()
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
     * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 02/28/2003  INB	Use the name of the local server for the log channel.
     * 12/20/2001  INB	Created.
     *
     */
    public final void run() {
	boolean interrupted = false;

	while (!interrupted) {
	    try {
		PeerUpdate peerUpdate = null;

		synchronized (peerUpdates) {
		    while (peerUpdates.size() == 0) {
			peerUpdates.wait(TimerPeriod.LONG_WAIT);
		    }
		    peerUpdate = (PeerUpdate) peerUpdates.elementAt(0);
		    peerUpdates.removeElementAt(0);
		}

		Rmap entry = findDescendant(peerUpdate.getPeerName(),
					    false);
		PeerServer peerServer = null;

		if (entry == null) {
		    Rmap hierarchy = Rmap.createFromName
			(peerUpdate.getPeerName()),
			bottom = null;
		    for (bottom = hierarchy;
			 bottom.getNchildren() == 1;
			 bottom = bottom.getChildAt(0)) {}
		    Rmap above = bottom.getParent();
		    peerServer = new PeerServer
			(bottom.getName(),
			 peerUpdate.getPeerAddress());
		    peerServer.setUpdateCounter
			(peerUpdate.getPeerUpdateCounter());
		    if (above != null) {
			above.removeChildAt(0);
			above.addChild(peerServer);
		    } else {
			hierarchy = peerServer;
		    }
		    entry = createPeer(hierarchy,null);

		} else if (!(entry instanceof PeerServer)) {
		    Rmap hierarchy = Rmap.createFromName
			(peerUpdate.getPeerName()),
			bottom = null;
		    for (bottom = hierarchy;
			 bottom.getNchildren() == 1;
			 bottom = bottom.getChildAt(0)) {}
		    peerServer = new PeerServer
			(bottom.getName(),
			 peerUpdate.getPeerAddress());
		    peerServer.setUpdateCounter
			(peerUpdate.getPeerUpdateCounter());
		    while (entry.getNchildren() > 0) {
			Rmap sub = entry.getChildAt(0);
			entry.removeChildAt(0);
			peerServer.addChild(sub);
		    }
		    Rmap above = entry.getParent();
		    above.removeChild(entry);
		    above.addChild(peerServer);
		    entry = peerServer;
		} 

		peerServer = (PeerServer) entry;
		peerServer.updatePeer(peerUpdate);

		if (getThread() != null) {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName() + "/RoutingMap",
			 "RBNBRoutingMap.run(1)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		}

	    } catch (java.lang.InterruptedException e) {
		interrupted = true;

	    } catch (java.lang.Exception e) {
		try {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getLocalServerHandler().getName(),
			 e);
		} catch (java.lang.Exception e1) {
		}
	    }
	}

	if (getThread() != null) {
	    try {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (getFullName() + "/RoutingMap",
		     "RBNBRoutingMap.run(2)",
		     getLog(),
		     getLogLevel(),
		     getLogClass());
	    } catch (java.lang.Exception e) {
	    }
	}
	setThread(null);
    }

    /**
     * Sets the <code>Door</code> for controlling the creation of
     * <code>PeerServers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerDoorI the <code>Door</code>.
     * @see #getPeerDoor()
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
    private final void setPeerDoor(Door peerDoorI) {
	peerDoor = peerDoorI;
    }

    /**
     * Sets the peer updates thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI the peer updates thread.
     * @see #getThread()
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
    private final void setThread(Thread threadI) {
	thread = threadI;
    }

    /**
     * Starts the peer updates thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #stop()
     * @since V2.0
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Added name to the thread.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 12/20/2001  INB	Created.
     *
     */
    public final void start() {
	if (getThread() == null) {
	    setThread(new ThreadWithLocks(this,"RoutingMap"));
	    getThread().start();
	}
    }

    /**
     * Stops the peer updates thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @since V2.0
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added synchronization.
     * 03/31/2003  INB	Use <code>lostRouting</code> to close
     *			<code>ConnectedServers</code>.
     * 12/20/2001  INB	Created.
     *
     */
    public final void stop() {
	interrupt();

	try {
	    // Ensure that any <code>RemoteServers</code> are closed down.
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if ((getChildAt(idx) instanceof ConnectedServer) &&
		    ((ConnectedServer) getChildAt(idx)).getConnected()) {
		    ConnectedServer cServer =
			(ConnectedServer) getChildAt(idx);
		    cServer.lostRouting();
		} else if (getChildAt(idx) instanceof RemoteServer) {
		    RemoteServer rServer = (RemoteServer) getChildAt(idx);
		    rServer.stop();
		    synchronized (metricsSyncObj) {
			removeChildAt(idx);
			metricsDeadBytes += rServer.bytesTransferred();
		    }
		    --idx;
		}
	    }
	} catch (java.lang.Exception e) {
	}
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
	synchronized (peerUpdates) {
	    peerUpdates.addElement(peerUpdateI);
	    peerUpdates.notify();
	}
    }
}
