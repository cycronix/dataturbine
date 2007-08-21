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
 * The connected server class.
 * <p>
 * A connected server is one that is connected to the local <bold>RBNB</bold>
 * via a routing connection of some type. The connection can be either a
 * hierarchical one (parent/child) or a peer-to-peer one.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/27/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/27/2004  INB	Stop the <code>RCO</code> associated with each related
 *			router rather than the router itself.
 * 02/18/2004  INB	Timeout waiting for reversed routers.
 *			Added <code>WaitForRouters</code> code to timeout
 *			connections that do not quite succeed in connecting or
 *			when a route is lost.
 * 02/11/2004  INB	Handle an exception during the startup exchange as if
 *			we lost routing.
 *			Log exceptions at standard level.
 * 01/14/2004  INB	Added synchronization of metrics.
 * 11/18/2003  INB	Added some debug print on waits.
 * 05/23/2003  INB	Added dot to router names.
 * 04/01/2003  INB	Eliminate some infinite waits.
 * 03/31/2003  INB	Add a unique identifier to the routers built. Made
 *			<code>get/setTerminateRequested</code> public methods.
 * 03/26/2003  INB	Added <code>MAXIMUM_SIMULTANEOUS_ROUTERS</code>. Use
 *			<code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/25/2003  INB	If this side isn't connected, but the other side is,
 *			then wait in <code>grabRouter</code>.
 * 03/20/2003  INB	Changed the <code>wait</code> call when looking for a
 *			router to only block for one second. Added
 *			<code>getRelatedRouters</code> and added notification
 *			when a <code>Router</code> is created.
 * 03/19/2003  INB	When the server is started, mark it as connected.
 * 03/04/2003  INB	Attempt to shutdown the remote end when we're stopped.
 * 02/27/2003  INB	Added <code>moveRouters</code>. Timeout waiting for
 *			responses to delivered messages.
 * 02/26/2003  INB	Added the list of routing connections that come from
 *			the remote server. Connections in this list are
 *			terminated when the routing is unexpectedly lost.
 *			Added <code>disconnectedRouting</code> method to do
 *			the actual cleaning up.
 * 12/17/2001  INB	Created.
 *
 */
abstract class ConnectedServer
    extends com.rbnb.api.RemoteServer
{
    /**
     * the maximum number of simultaneous routers.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    public final static int MAXIMUM_SIMULTANEOUS_ROUTERS = 10;

    /**
     * route is direct.
     * <p>
     * In this mode, <code>Routers</code> are created from this side.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ROUTE_OFF
     * @see #ROUTE_REVERSED
     * @since V2.0
     * @version 02/05/2002
     */
    final static byte ROUTE_FORWARD = 2;

    /**
     * route is off.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ROUTE_FORWARD
     * @see #ROUTE_REVERSED
     * @since V2.0
     * @version 02/05/2002
     */
    final static byte ROUTE_OFF = 0;

    /**
     * route has been reversed.
     * <p>
     * In this mode, the <code>Routers</code> are created by the other side.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ROUTE_FORWARD
     * @see #ROUTE_OFF
     * @since V2.0
     * @version 02/05/2002
     */
    final static byte ROUTE_REVERSED = 1;

    /**
     * the list of available <code>Routers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/17/2001
     */
    private java.util.Vector availableRouters = new java.util.Vector();

    /**
     * the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/17/2001
     */
    private ServerHandler local = null;

    /**
     * the unique identifier for the next router.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/31/2003
     */
    private long nextRouterId = 1;

    /**
     * related <code>RouterHandlers</code> that come from the remote.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */
    private java.util.Vector relatedRouters = new java.util.Vector();

    /**
     * are we stopping?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/18/2001
     */
    private boolean terminateRequested = false;

    /**
     * the type of route.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ROUTE_FORWARD
     * @see #ROUTE_OFF
     * @see #ROUTE_REVERSED
     * @since V2.0
     * @version 02/05/2002
     */
    private byte type = ROUTE_OFF;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
    ConnectedServer() {
	super();
    }

    /**
     * Class constructor to build a <code>ConnectedServer</code> for a name.
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
    ConnectedServer(String nameI)
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
     * Class constructor to build a <code>ConnectedServer</code> for a name
     * and an address.
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
    ConnectedServer(String nameI,String addressI)
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
     * Adds a related <code>RouterHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the <code>RouterHandler</code> to add.
     * @see #removeRelated(com.rbnb.api.RouterHandler)
     * @since V2.1
     * @version 03/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Created.
     *
     */
    final void addRelated(RouterHandler routerI) {
	getRelatedRouters().addElement(routerI);
    }

    /**
     * Builds a <code>Router</code> to the "real" hierarchical server.
     * <p>
     * This method just builds the <code>Router</code> object. It is the
     * responsibility of the caller to hook it up to the other side.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Router</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB 	Added dot to router names.
     * 03/31/2003  INB	Add a unique identifier.
     * 03/20/2003  INB	Notify when a <code>Router</code> is created.
     * 12/17/2001  INB	Created.
     *
     */
    final Router buildRouter()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String rhName = "_RH." +
	    getLocalServerHandler().getFullName() + "." +
	    nextRouterId++;
	Router routerR = new RouterHandle(rhName.replace('/','_'));
	BuildInterface bi = (BuildInterface) routerR;

	bi.setBuildDate(getLocalServerHandler().getBuildDate());
	bi.setBuildVersion(getLocalServerHandler().getBuildVersion());
//	bi.setLicenseString(getLocalServerHandler().getLicenseString());
	addChild((Rmap) routerR);

	return (routerR);
    }

    /**
     * Creates a <code>Router</code> to the "real" hierarchical server from the
     * other side.
     * <p>
     * This method uses the input <code>Router</code> to create a second one by
     * asking the remote side to make a reverse connection to here.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the existing <code>Router</code>.
     * @return handled the create?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
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
     * 02/06/2001  INB	Created.
     *
     */
    abstract boolean createReversedRouter(Router routerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Creates a <code>Router</code> to the "real" hierarchical server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Router</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added synchronization of metrics.
     * 12/17/2001  INB	Created.
     *
     */
    final Router createRouter()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte wasType = getType();
	Router routerR = buildRouter();

	((ClientHandle) routerR).setACO(ACO.newACO(routerR));
	try {
	    setType(ROUTE_FORWARD);
	    routerR.start();
	} catch (com.rbnb.api.AddressException e) {
	    try {
		setType(wasType);
		routerR.stop();
	    } catch (java.lang.Exception e1) {
	    }
	    try {
		synchronized (metricsSyncObj) {
		    removeChild((Rmap) routerR);
		    metricsDeadBytes +=
			((RouterHandle) routerR).bytesTransferred();
		}
	    } catch (java.lang.Exception e1) {
	    }
	    routerR = null;
	    throw e;

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		setType(wasType);
		routerR.stop();
	    } catch (java.lang.Exception e1) {
	    }
	    try {
		synchronized (metricsSyncObj) {
		    removeChild((Rmap) routerR);
		    metricsDeadBytes +=
			((RouterHandle) routerR).bytesTransferred();
		}
	    } catch (java.lang.Exception e1) {
	    }
	    routerR = null;
	    throw e;

	} catch (java.io.InterruptedIOException e) {
	    try {
		setType(wasType);
		routerR.stop();
	    } catch (java.lang.Exception e1) {
	    }
	    try {
		setType(wasType);
		synchronized (metricsSyncObj) {
		    removeChild((Rmap) routerR);
		    metricsDeadBytes +=
			((RouterHandle) routerR).bytesTransferred();
		}
	    } catch (java.lang.Exception e1) {
	    }
	    routerR = null;
	    throw e;

	} catch (java.io.IOException e) {
	    try {
		setType(wasType);
		routerR.stop();
	    } catch (java.lang.Exception e1) {
	    }
	    try {
		synchronized (metricsSyncObj) {
		    removeChild((Rmap) routerR);
		    metricsDeadBytes +=
			((RouterHandle) routerR).bytesTransferred();
		}
	    } catch (java.lang.Exception e1) {
	    }
	    routerR = null;
	    throw e;

	} catch (java.lang.InterruptedException e) {
	    try {
		setType(wasType);
		routerR.stop();
	    } catch (java.lang.Exception e1) {
	    }
	    try {
		setType(wasType);
		synchronized (metricsSyncObj) {
		    removeChild((Rmap) routerR);
		    metricsDeadBytes +=
			((RouterHandle) routerR).bytesTransferred();
		}
	    } catch (java.lang.Exception e1) {
	    }
	    routerR = null;
	    throw e;
	}
 
	return (routerR);
    }

    /**
     * Destroys a <code>Router</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the <code>Router</code> to destroy.
     * @see #createRouter()
     * @since V2.0
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added synchronization of metrics.
     * 12/05/2001  INB	Created.
     *
     */
    final void destroyRouter(Router routerI) {
	grabRouter(routerI);
	try {
	    routerI.stop();
	} catch (java.lang.Exception e) {
	}
	try {
	    synchronized (metricsSyncObj) {
		removeChild((Rmap) routerI);
		metricsDeadBytes +=
		    ((RouterHandle) routerI).bytesTransferred();
	    }
	} catch (java.lang.Exception e) {
	}
	if (!getTerminateRequested()) {
	    boolean noMore = true;
	    try {
		noMore = (getNchildren() == 0);
		if (!noMore) {
		    noMore = true;
		    for (int idx = 0;
			 noMore && (idx < getNchildren());
			 ++idx) {
			if (getChildAt(idx) instanceof Router) {
			    noMore = false;
			}
		    }
		}
	    } catch (java.lang.Exception e) {
		noMore = true;
	    }

	    if (noMore) {
		lostRouting();
	    }
	}
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
     * @version 02/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 05/23/2003  INB	Modify wait time for login/stop messages.
     * 02/27/2003  INB	Timeout waiting for responses.
     * 12/17/2001  INB	Created.
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
	    Router router = grabRouter();
	    try {
		router.send(messageI);

		long timeOut = TimerPeriod.SHUTDOWN;
		if (messageI instanceof RoutedMessage) {
		    RoutedMessage rMessage = (RoutedMessage) messageI;
		    if (rMessage.getMessage() instanceof Login) {
			timeOut = TimerPeriod.STARTUP_WAIT;
		    } else if (rMessage.getMessage() instanceof Stop) {
			timeOut = TimerPeriod.SHUTDOWN_ROUTER;
		    }
		}
		if ((serializableR = router.receive(timeOut)) == null) {
		    throw new com.rbnb.api.SerializeException
			("Timed out waiting for response to " + messageI);
		}

	    } catch (com.rbnb.api.AddressException e) {
		try {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  getFullName(),
					  e);
		} catch (java.lang.Exception e1) {
		}
		destroyRouter(router);
		router = null;
		serializableR = Language.exception(e);

	    } catch (com.rbnb.api.SerializeException e) {
		try {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  getFullName(),
					  e);
		} catch (java.lang.Exception e1) {}
		destroyRouter(router);
		router = null;
		serializableR = Language.exception(e);

	    } catch (java.io.IOException e) {
		try {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  getFullName(),
					  e);
		} catch (java.lang.Exception e1) {}
		destroyRouter(router);
		router = null;
		serializableR = Language.exception(e);

	    } finally {
		if (router != null) {
		    releaseRouter(router);
		    router = null;
		}
	    }

	} else {
	    serializableR = super.deliver(messageI,offsetI);
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
     * @since V2.1
     * @version 02/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Created.
     *
     */
    void disconnectedRouting() {
	// Ensure that related <code>RouterHandlers</code> are also down.
	terminateRelated();

	// Terminate all of the <code>RouterHandles</code>.
	java.util.Vector rHandles = new java.util.Vector();

	try {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof RouterHandle) {
		    rHandles.addElement(getChildAt(idx));
		}
	    }
	} catch (java.lang.Exception e) {
	}

	for (int idx = 0; idx < rHandles.size(); ++idx) {
	    RouterHandle child = (RouterHandle) rHandles.elementAt(idx);
	    destroyRouter(child);
	}

	// Use our superclass's disconnect.
	super.disconnectedRouting();
    }

    /**
     * Eliminates this <code>ConnectedServer</code>.
     * <p>
     * This method is called when initiating a new route results in our having
     * two <code>ConnectedServer</code> objects for the same server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2002  INB	Created.
     *
     */
    final void eliminate() {
	try {
	    Router router;

	    while (getNchildren() > 0) {
		router = (Router) getChildAt(0);
		destroyRouter(router);
	    }
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Gets the list of available routers.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of available routers.
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
    final java.util.Vector getAvailableRouters() {
	return (availableRouters);
    }

    /**
     * Gets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the local <code>ServerHandler</code>.
     * @see #setLocalServerHandler(ServerHandler)
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    public ServerHandler getLocalServerHandler() {
	return (local);
    }

    /**
     * Gets the log class mask for this <code>ConnectedServer</code>.
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
	return (super.getLogClass() | Log.CLASS_CONNECTED_SERVER);
    }

    /**
     * Gets the related <code>Routers</code> list.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the related <code>Routers</code> list.
     * @since V2.1
     * @version 03/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/20/2003  INB	Created.
     *
     */
    final java.util.Vector getRelatedRouters() {
	return (relatedRouters);
    }

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the terminate requested flag.
     * @see #setTerminateRequested(boolean)
     * @since V2.0
     * @version 03/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Made this a public method.
     * 12/18/2001  INB	Created.
     *
     */
    public final boolean getTerminateRequested() {
	return (terminateRequested);
    }

    /**
     * Gets the type of route.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the type of route.
     * @see #ROUTE_FORWARD
     * @see #ROUTE_OFF
     * @see #ROUTE_REVERSED
     * @see #setType(byte)
     * @since V2.0
     * @version 02/05/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/05/2002  INB	Created.
     *
     */
    final byte getType() {
	return (type);
    }

    /**
     * Grabs a <code>Router</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Router</code>.
     * @see #releaseRouter(com.rbnb.api.Router)
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Timeout waiting for reversed routers.  Also, initiate
     *			a <code>WaitForRouters</code> object any time we appear
     *			to have no routers.
     * 11/18/2003  INB	Added debug to waits.
     * 04/01/2003  INB	Add one more <code>Router</code> if we take the last.
     * 03/26/2003  INB	Use <code>MAXIMUM_SIMULTANEOUS_ROUTERS</code>. Use
     *			<code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/25/2003  INB	If this side isn't connected, but the other side is,
     *			then wait.
     * 03/20/2003  INB	Changed the <code>wait</code> call to break out once a
     *			second.
     * 12/17/2001  INB	Created.
     *
     */
    final Router grabRouter()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Router routerR = null;

	long startAt = System.currentTimeMillis();
	long beginAt = startAt;
	long nowAt;
	boolean initiatedWaitFor = false;
	synchronized (this) {
	    while (!getConnected() &&
		   (getRelatedRouters().size() > 0)) {
		wait(TimerPeriod.NORMAL_WAIT);
		nowAt = System.currentTimeMillis();
		if (nowAt - startAt >= TimerPeriod.LOCK_WAIT) {
		    try {
			throw new java.lang.Exception
			    (nowAt + " " + getFullName() + " " +
			     Thread.currentThread() +
			     " ConnectedServer.grabRouter(1) " +
			     "blocked waiting " +
			     "for connection (" +
			     getConnected() +
			     ") or related routers (" +
			     getRelatedRouters().size() + ").");
		    } catch (java.lang.Exception e) {
			e.printStackTrace();
		    }
		    startAt = nowAt;
		}
	    }
	}
	if (!getConnected()) {
	    throw new com.rbnb.api.AddressException
		("The connection to " + getFullName() + " has been lost.");
	}

	synchronized (getAvailableRouters()) {
	    int idx,
		nRouters = 0;
	    for (idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof Router) {
		    ++nRouters;
		}
	    }
	    startAt = System.currentTimeMillis();
	    while (getConnected() &&
		   (routerR == null) &&
		   (getAvailableRouters().size() == 0)) {
		// If there are no available <code>Routers</code>, then we need
		// to either create one or wait for one to be available.
		if ((nRouters == 0) &&
		    (getType() != ROUTE_REVERSED)) {
		    routerR = createRouter();
		    
		} else if ((nRouters < MAXIMUM_SIMULTANEOUS_ROUTERS) &&
			   (getType() != ROUTE_REVERSED)) {
		    routerR = createRouter();

		} else {
		    getAvailableRouters().wait(TimerPeriod.NORMAL_WAIT);
		    nowAt = System.currentTimeMillis();
		    if ((getType() == ROUTE_REVERSED) &&
			(nowAt - beginAt >= TimerPeriod.STARTUP_WAIT)) {
			(new WaitForRouters(this)).start();
			initiatedWaitFor = true;
			throw new com.rbnb.api.AddressException
			    ("No route to " + getFullName() + ".");
		    } else if (nowAt - startAt >= TimerPeriod.LOCK_WAIT) {
			if (!initiatedWaitFor &&
			    (nowAt - beginAt >= TimerPeriod.STARTUP_WAIT)) {
			    (new WaitForRouters(this)).start();
			    initiatedWaitFor = true;
			}
			try {
			    throw new java.lang.Exception
				(nowAt + " " + getFullName() + " " +
				 Thread.currentThread() +
				 " ConnectedServer.grabRouter(2)" +
				 " blocked waiting for router.");
			} catch (java.lang.Exception e) {
			    e.printStackTrace();
			}
			startAt = nowAt;
		    }
		}
	    }

	    if ((routerR != null) &&
		(getAvailableRouters().size() == 0) &&
		(getType() == ROUTE_REVERSED) &&
		(nRouters < MAXIMUM_SIMULTANEOUS_ROUTERS)) {
		createReversedRouter(routerR);
	    }

	    if (!getConnected() || (getType() == ROUTE_OFF)) {
		throw new com.rbnb.api.AddressException
		    ("The connection to " + getFullName() + " has been lost.");
	    }

	    if ((routerR == null) &&
		(getAvailableRouters().size() > 0)) {
		routerR = (Router) getAvailableRouters().firstElement();
		getAvailableRouters().removeElementAt(0);
		if ((getAvailableRouters().size() == 0) &&
		    (getType() == ROUTE_FORWARD) &&
		    (nRouters < MAXIMUM_SIMULTANEOUS_ROUTERS)) {
		    releaseRouter(createRouter());
		}
	    }
	}

	return (routerR);
    }

    /**
     * Attempts to grab a specific <code>Router</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the <code>Router</code> to grab.
     * @return <code>routerI</code> if the <code>Router</code> was successfully
     *	       grabbed.
     * @see #grabRouter()
     * @see #releaseRouter(com.rbnb.api.Router)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    final Router grabRouter(Router routerI) {
	Router routerR = null;

	synchronized (getAvailableRouters()) {
	    if (getAvailableRouters().removeElement(routerI)) {
		routerR = routerI;
	    }
	}

	return (routerR);
    }

    /**
     * Releases a <code>Router</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the <code>Router</code>.
     * @see #grabRouter()
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
    final void releaseRouter(Router routerI) {
	synchronized (getAvailableRouters()) {
	    getAvailableRouters().addElement(routerI);
	    getAvailableRouters().notifyAll();
	}
    }

    /**
     * Removes the specified <code>RouterHandler</code> from the related list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the related <code>RouterHandler</code>.
     * @see #addRelated(com.rbnb.api.RouterHandler)
     * @since V2.1
     * @version 03/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Created.
     *
     */
    final void removeRelated(RouterHandler routerI) {
	getRelatedRouters().removeElement(routerI);
    }

    /**
     * Move all of the <code>Routers</code> from this to the input.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI the <code>ConnectedServer</code> to move the
     *		      <code>Routers</code> to.
     * @since V2.1
     * @version 02/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2003  INB	Created.
     *
     */
    final void moveRouters(ConnectedServer serverI) {
	RouterHandle router;
	try {
	    for (int idx = 0; idx < getNchildren(); ) {
		if (getChildAt(idx) instanceof RouterHandle) {
		    router = (RouterHandle) getChildAt(idx);
		    grabRouter(router);
		    removeChild(router);
		    serverI.addChild(router);
		    serverI.releaseRouter(router);
		} else {
		    ++idx;
		}
	    }
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Reversed a route connection.
     * <p>
     * This method is called whenever an attempt is made to create a reverse
     * connection from the child to the parent on the parent's behalf or by a
     * peer on the other side of a shortcut and the other side succeeds.
     * <p>
     * This method is responsible for creating a <code>Router</code> on this
     * end using an <code>ACO</code> built from the provided <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI  the <code>RCO</code>.
     * @param peerI peer connection?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
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
     * 02/11/2004  INB	Handle an exception during the startup exchange as if
     *			we lost routing.
     * 04/01/2003  INB	Don't wait forever.
     * 01/25/2002  INB	Created.
     *
     */
    final void reversed(RCO rcoI,boolean peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean wasOff;
	if ((wasOff = getType() == ROUTE_OFF)) {
	    setType(ROUTE_REVERSED);
	}

	RouterHandle router = (RouterHandle) buildRouter();
	ACO aco = rcoI.convertToACO(router);
	router.setACO(aco);
	router.start();

	if (wasOff) {
	    java.util.Vector additional = new java.util.Vector();
	    if (peerI) {
		additional.addElement("peer");
		additional.addElement
		    (((Rmap) getLocalServerHandler()).newInstance());
	    } else {
		additional.addElement("parent");
		additional.addElement
		    (((Rmap) getLocalServerHandler()).newInstance());
	    }

	    Ask ask = new Ask(Ask.ROUTEFROM,additional);


	    try {
		router.send(ask);
		if (router.receive(ACO.rmapClass,false,
				   TimerPeriod.PING_WAIT) == null) {
		    throw new com.rbnb.api.AddressException
			(ask +
			 " failed to start in a reasonable amount of time.");
		}

	    } catch (Exception e) {
		// If we get an exception, then there is a problem setting up
		// this route.  Shutdown this server.
		lostRouting();
		Language.throwException(e);
	    }
	}
	releaseRouter(router);
    }

    /**
     * Sets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param localI the local <code>ServerHandler</code>.
     * @see #getLocalServerHandler()
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
    final void setLocalServerHandler(ServerHandler localI) {
	local = localI;
    }

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param terminateI  terminate this <code>RBNB</code>?
     * @see #getTerminateRequested()
     * @since V2.0
     * @version 03/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Made this a public method.
     * 01/11/2001  INB	Created.
     *
     */
    public final void setTerminateRequested(boolean terminateI) {
	terminateRequested = terminateI;
	if (!terminateI) {
	    synchronized (this) {
		notifyAll();
	    }
	}
    }

    /**
     * Sets the type of route.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI the type.
     * @see #ROUTE_FORWARD
     * @see #ROUTE_OFF
     * @see #ROUTE_REVERSED
     * @since V2.0
     * @version 02/05/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/05/2002  INB	Created.
     *
     */
    final void setType(byte typeI) {
	type = typeI;
    }

    /**
     * This method is a NOP.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #stop()
     * @since V2.0
     * @version 02/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Added <code>WaitForRouters</code> startup.
     * 03/19/2003  INB	Set the connected flag.
     * 12/17/2001  INB	Created.
     *
     */
    public final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	setConnected(true);
	(new WaitForRouters(this)).start();
    }

    /**
     * Disconnects this <code>ConnectedServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @since V2.0
     * @version 03/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Clear terminate requested.
     * 03/04/2003  INB	Attempt to shutdown the remote end.
     * 02/26/2003  INB	Simply set the terminate flag and use our superclass's
     *			<code>stop</code>.
     * 12/17/2001  INB	Created.
     *
     */
    public final void stop() {
	setTerminateRequested(true);
	super.stop();
	setTerminateRequested(false);
    }

    /**
     * Terminate all related <code>RouterHandlers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/27/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/27/2004  INB	Stop the <code>RCO</code> associated with each related
     *			router rather than the router itself.
     * 02/26/2003  INB	Created.
     *
     */
    final void terminateRelated() {
	while (getRelatedRouters().size() > 0) {
	    RouterHandler router = (RouterHandler)
		getRelatedRouters().firstElement();
	    try {
		router.getRCO().stop();
	    } catch (java.lang.Exception e) {
	    }
	    removeRelated(router);
	}
    }

    /**
     * Attempts to switch a <code>ConnectedServer</code> from a passive state
     * to an active state.
     * <p>
     * This method tries to establish a connection to the remote from this
     * end.  If it succeeds, then the route can be made active, otherwise, the
     * connection is left in a passive state.
     * <p>
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
     * @since V2.0
     * @version 02/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/06/2002  INB	Created.
     *
     */
    final void switchToActive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getType() != ROUTE_FORWARD) {
	    Router router = createRouter();
	    releaseRouter(router);
	}
    }

    /**
     * Waits for <code>RouterHandles</code> to appear on the
     * <code>ConnectedServer</code>.
     * <p>
     * If no <code>RouterHandles</code> appear and start running within a
     * reasonable amount of time, then this method forces the route down.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 02/18/2004
     */

    /*
     * Copyright 2004 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Created.
     *
     */
    private final class WaitForRouters
	extends com.rbnb.api.ThreadWithLocks
    {
	/**
	 * the <code>ConnectedServer</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 02/18/2004
	 */
	private ConnectedServer cServer = null;

	/**
	 * Builds a <code>WaitForRouters</code> for the specified
	 * <code>ConnectedServer</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param cServerI the <code>ConnectedServer</code>.
	 * @since V2.2
	 * @version 02/18/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/18/2004  INB	Created.
	 *
	 */
	public WaitForRouters(ConnectedServer cServerI) {
	    super();
	    setDaemon(true);
	    cServer = cServerI;
	}

	/**
	 * Waits for <code>Routers</code> or times out.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 02/18/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/18/2004  INB	Created.
	 *
	 */
	public final void run() {
	    long startAt = System.currentTimeMillis();
	    long nowAt;
	    Rmap child;
	    RouterHandle rh;
	    boolean found = false;

	waitLoop:
	    while (((nowAt = System.currentTimeMillis()) - startAt) <
		   TimerPeriod.STARTUP_WAIT) {
		try {
		    for (int idx = 0; idx < cServer.getNchildren(); ++idx) {
			child = cServer.getChildAt(idx);
			if (child instanceof RouterHandle) {
			    rh = (RouterHandle) child;
			    if (rh.getStarted()) {
				found = true;
				break waitLoop;
			    }
			}
		    }
		} catch (java.lang.Exception e) {
		}

		try {
		    Thread.currentThread().sleep(TimerPeriod.LONG_WAIT);
		} catch (java.lang.InterruptedException e) {
		    break waitLoop;
		}
	    }

	    if (!found) {
		cServer.lostRouting();
	    }
	}
    }
}
