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
 * The parent server class.
 * <p>
 * A parent server is one that represents the <bold>RBNB</bold> that is the
 * parent of the local one. It is one of the three directions a message can be
 * routed towards its destination, with the others being
 * <code>PeerServer</code> and <code>ChildServer</code> servers.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.ChildServer
 * @see com.rbnb.api.PeerServer
 * @since V2.0
 * @version 05/18/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
 * 02/18/2004  INB	Ensure that the route is not off in addition to
 *			connected.
 * 01/14/2004  INB	Added synchronization of metrics.
 * 09/25/2003  INB	Reworked reconnect logic again to minimize the size
 *			of the synchronized block.
 * 05/07/2003  INB	Reworked reconnect logic to ensure that only one
 *			reconnect task runs and that once it succeeds, it
 *			always turns off further reconnect attempts.
 * 03/31/2003  INB	Re-establish the reconnect logic.
 * 11/20/2001  INB	Created.
 *
 */
final class ParentServer
    extends com.rbnb.api.HierarchicalServer
{
    /**
     * are we in the process of reconnecting?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/14/2001
     */
    private boolean inReconnect = false;

    /**
     * reconnect timer-task.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/31/2003
     */
    TimerTask reconnectTT = null;

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
    ParentServer() {
	super();
    }

    /**
     * Class constructor to build a <code>ParentServer</code> from an address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address.
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
    ParentServer(String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
 	setAddress(addressI);
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
     * @version 02/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Ensure that the route is not off in addition to
     *			connected.
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
	LocalPath pathR = null;

	if (getConnected() && (getType() != ROUTE_OFF)) {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		Rmap entry = getChildAt(idx);

		if (entry instanceof ServerHandler) {
		    pathR = (LocalPath) ((PathFinder) entry).findPath();
		    break;
		}
	    }

	    if (pathR != null) {
		pathR.add(this);
		pathR.addCost(1);
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Cannot find path to " + getFullName() + ".");
	    }
	}

	return (pathR);
    }

    /**
     * Gets the log class mask for this <code>ParentServer</code>.
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
	return (super.getLogClass() | Log.CLASS_PARENT_SERVER);
    }

    /**
     * Lost routing to this <code>ParentServer</code>.
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
     * 05/07/2003  INB	Ensure that only one reconnect task ever gets started.
     * 03/31/2003  INB	Re-establish the reconnect logic.
     * 12/14/2001  INB	Created.
     *
     */
    final void lostRouting() {
	synchronized (this) {
	    if (!getConnected()) {
		return;
	    }
	    setConnected(false);
	}

	synchronized (getAvailableRouters()) {
	    getAvailableRouters().notifyAll();
	}

	try {
	    ServerHandler sHandler = getLocalServerHandler();
	    if (!sHandler.getTerminateRequested()) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getFullName(),
		     "Lost all connections to parent server. " +
		     "Initiating reconnection task.");
	    }

	    disconnectedRouting();
	    synchronized (this) {
		if ((!sHandler.getTerminateRequested()) &&
		    (reconnectTT == null)) {
		    reconnectTT = new TimerTask(sHandler,
						sHandler.TT_RECONNECT);
		    sHandler.getTimer().scheduleAtFixedRate
			(reconnectTT,
			 0L,
			 TimerPeriod.RECONNECT);

/*
		    try {
			System.err.println(getFullName() +
					   " launched reconnect as " +
					   reconnectTT);
		    } catch (Exception e) {
		    }
*/

		}
	    }

	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Attempts to reconnect to the <bold>RBNB</bold> represented by this
     * <code>ParentServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return was a connection re-established?
     * @since V2.0
     * @version 05/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 02/18/2004  INB	Ensure that the route is not off in addition to
     *			connected.
     * 01/14/2004  INB	Added synchronization of metrics.
     * 09/25/2003  INB	Reduced the size of the block that is actually
     *			synchronized.
     * 05/07/2003  INB	Synchronized the reconnect block and added check for
     *			existing connection.
     * 04/01/2003  INB	Eliminated infinite wait.
     * 03/31/2003  INB	Re-establish reconnect logic. Add reverse route.
     * 12/05/2001  INB	Created.
     *
     */
    public final boolean reconnect() {

	/*
	try {
	    System.err.println(System.currentTimeMillis() + " " +
			       getFullName() + " reconnecting: " +
			       inReconnect +
			       " already connected: " + getConnected() +
			       " thread " +
			       Thread.currentThread());
	} catch (Exception e) {
	}
	*/

	boolean reconnectedR = false;

	Router router = null;
	Rmap lServer = null;

	synchronized (this) {
	    if (getConnected() && (getType() != ROUTE_OFF)) {
		if (reconnectTT != null) {
		    try {
			reconnectTT.cancel();
		    } catch (java.lang.Exception e) {
		    }
		    reconnectTT = null;
		}

	    } else if (inReconnect) {
		setConnected(false);
		/*
		try {
		    System.err.println(getFullName() +
				      " already reconnecting.");
		} catch (Exception e) {
		}
		*/

		return (false);
	    } else {
		inReconnect = true;
		setConnected(false);
	    }
	}

	/*
	try {
	    System.err.println(getFullName() + " in reconnect.");
	} catch (Exception e) {
	}
	*/

	try {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof Router) {
		    /*
		    try {
			System.err.println(getFullName() +
					  " already connected.");
		    } catch (Exception e) {
		    }
		    */

		    return (false);
		}
	    }

	    lServer = newInstance();
	    Rmap entry,
		nEntry,
		top = lServer;
	    for (entry = getParent();
		 entry != null;
		 entry = entry.getParent()) {
		nEntry = entry.newInstance();
		nEntry.addChild(top);
		top = nEntry;
	    }
	    java.util.Vector additional = new java.util.Vector();
	    additional.addElement("child");
	    additional.addElement
		(((Rmap) getLocalServerHandler()).newInstance());
	    additional.addElement(top);

	    /*
	    try {
		System.err.println(getFullName() + " create router.");
	    } catch (Exception e) {
	    }
	    */

	    router = createRouter();

	    /*
	    try {
		System.err.println(getFullName() + " ask for route.");
	    } catch (Exception e) {
	    }
	    */

	    router.send(new Ask(Ask.ROUTEFROM,additional));
	    Serializable response = router.receive
		(ACO.rmapClass,
		 false,
		 TimerPeriod.PING_WAIT);

	    /*
	    try {
		System.err.println(getFullName() + " response:\n" + response);
	    } catch (Exception e) {
	    }
	    */

	    if (response == null) {
		throw new com.rbnb.api.AddressException
		    ("Timed out waiting for connection to " +
		     getFullName() + " to re-establish.");

	    } else if (response instanceof ExceptionMessage) {
		if (getLog() != null) {
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getLocalServerHandler().getFullName(),
			 "Cannot re-establish connection to parent " +
			 "server " + lServer.getFullName() +
			 ",\n" +
			 "a different server has been " +
			 "established at the same address.");
		}

	    } else {
		Rmap hierarchy = (Rmap) response;
		releaseRouter(router);
		router = null;

		lServer.addChild
		    (((Rmap) getLocalServerHandler()).newInstance());
		ReverseRoute reverse = new ReverseRoute(lServer);
		getLocalServerHandler().initiateReverseRoute(reverse);
		regainedRouting();
		reconnectedR = true;		
	    }

	    synchronized (this) {
		if (reconnectTT != null) {
		    try {
			reconnectTT.cancel();
		    } catch (java.lang.Exception e) {
		    }
		    reconnectTT = null;
		}
	    }

	    /*
	    try {
		System.err.println(getFullName() + " done: " + getConnected());
	    } catch (Exception e) {
	    }
	    */

	} catch (java.lang.IllegalArgumentException e) {
	    if (getLog() != null) {
		try {
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getLocalServerHandler().getFullName(),
			 "Cannot re-establish connection to parent " +
			 "server " + lServer.getFullName() +
			 ",\n" +
			 "a different server has been " +
			 "established at the same address.");
		} catch (java.lang.Exception e1) {
		}
	    }
	    if (router != null) {
		try {
		    router.stop();
		    synchronized (metricsSyncObj) {
			removeChild((Rmap) router);
			metricsDeadBytes +=
			    ((RouterHandle) router).bytesTransferred();
		    }
		} catch (java.lang.Exception e1) {}
	    }
	    router = null;

	    synchronized (this) {
		if (reconnectTT != null) {
		    try {
			reconnectTT.cancel();
		    } catch (java.lang.Exception e1) {
		    }
		    reconnectTT = null;
		}
	    }

	} catch (java.lang.Throwable e) {

	    /*
	    try {
		System.err.println(getFullName() +				   " failed on exception.\n" + router);
	        e.printStackTrace();
	    } catch (Exception e1) {
	    }
	    */

	    if (router != null) {
		try {
		    router.stop();
		    synchronized (metricsSyncObj) {
			removeChild((Rmap) router);
			metricsDeadBytes +=
			    ((RouterHandle) router).bytesTransferred();
		    }
		} catch (java.lang.Exception e1) {}
	    }
	    router = null;

	} finally {
	    inReconnect = false;
	}

	return (reconnectedR);
    }
}
