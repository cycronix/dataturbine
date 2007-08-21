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
 * RBNB Control Object (RCO) class.
 * <p>
 * All communications between the RBNB server and the API are handled by the
 * application control object (<code>ACO</code>) class (and its subclasses) on
 * the client API side and this class (and its subclasses) on the RBNB server
 * side.  For an explanation of the API side, see <code>ACO</code>.
 * <p>
 * Objects of the <code>RCO</code> class are paired with a client handler
 * object (such as an <code>RBO</code>).  The <code>RCO</code> handles all of
 * the communications between the client and the server.  It interprets
 * messages it receives from the client, and, as needed, passes them on to the
 * client handler.  It also sends messages to the client on behalf of the
 * client handler.
 * <p>
 * The control hierarchies are described in more detail in <code>ACO</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.ACO
 * @see com.rbnb.api.ClientHandle
 * @see com.rbnb.api.RBNBClient
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	----------
 * 2007/07/23  WHF      Implemented Get/SetAddressAuthorization command support.
 * 08/12/2004  EMF      Added debug logging to login method.
 * 08/05/2004  INB	Added documentation.
 * 05/27/2004  INB	Don't log EOF exceptions.
 * 04/29/2004  INB	Make use of the <code>isAllowedAccess</code> method to
 *			restrict access to certain control type functions.
 * 04/28/2004  INB	Added method <code>isAllowedAccess</code>.
 * 02/11/2004  INB	Log errors at standard level if we do not have a client
 *			handler.  Log exceptions at standard level.
 * 02/09/2004  INB	Assume that I/O exceptions mean that there is a problem
 *			communicating with the client and don't try to send
 *			the exception.
 * 01/26/2004  INB	If there isn't a client handler, we aren't
 *			automatically stopping ourselves in <code>stop</code>.
 *			Added <code>wasClientHandler</code> field handling.
 * 01/08/2004  INB	Added handling of <code>ClearCache</code>.
 * 11/14/2003  INB	Use a <code>ThreadWithLocks</code> rather than a
 *			regular <code>Thread</code>.  Ensure that we have no
 *			<code>Locks</code> when we go around the
 *			<code>run</code> loop.
 *			Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 07/30/2003  INB	Added support for <code>DeleteChannels</code>.
 * 06/11/2003  INB	Added support for <code>RequestOptions</code>.
 * 04/30/2003  INB	Handle Java <code>Errors</code>.
 * 04/17/2003  INB	Allow clients to try to reconnect to existing handlers.
 * 04/07/2003  INB	If an <code>SocketException</code> occurs for an
 *			<code>RCO</code> with an <code>SourceHandler</code>
 *			client handler, call <code>stopOnIOException</code>.
 * 04/04/2003  INB	Handle Java errors.
 * 04/01/2003  INB	Simply acknowledge <code>Stops</code> of non-existent
 *			children.
 * 03/31/2003  INB	Use <code>lostRouting</code> to stop a server. Fail
 *			routes from servers that don't agree on our name.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/19/2003  INB	Modified the <code>stop(ServerInterface)</code> method
 *			to handle a non-named multi-level server interface.
 * 02/26/2003  INB	When <code>Pings</code> with data are used, they
 *			contain a sequence number that should monotonically
 *			increase.
 * 02/25/2003  INB	Use the <code>Ping</code> read from the remote as the
 *			<code>Ping</code> used in the reply.
 * 02/19/2003  INB	Use <code>interrupt</code> rather than
 *			<code>Thread.interrupt</code>. Added
 *			<code>stopMe</code> method to allow for special code.
 * 05/08/2001  INB	Created.
 *
 */
abstract class RCO
    implements com.rbnb.api.BuildInterface,
	       com.rbnb.api.GetLogInterface,
	       com.rbnb.api.Interruptable,
	       com.rbnb.api.IOMetricsInterface
{
    /**
     * the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private java.util.Date buildDate = null;

    /**
     * the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private String buildVersion = null;

    /**
     * the <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/08/2001
     */
    private ClientHandler clientHandler = null;

    /**
     * the license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    //    private String licenseString = null;

    /**
     * metrics: final bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    long metricsBytes = 0;

    /**
     * the next expected <code>Ping</code> data value.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */
    private short pingValue = -1;

    /**
     * the <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/08/2001
     */
    private ServerHandler rbnb = null;

    /**
     * the read door.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/13/2001
     */
    private Door readDoor = null;

    /**
     * has this connection been reversed?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/11/2002
     */
    private boolean reversed = false;

    /**
     * the server-side communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private Object serverSide = null;

    /**
     * <code>RCO</code> is stopping itself?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private boolean stopMyself = false;

    /**
     * stop this <code>RCO</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private boolean terminateRequested = false;

    /**
     * the thread running this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/08/2001
     */
    private Thread thread = null;

    /**
     * the former <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/26/2004
     */
    private ClientHandler wasClientHandler = null;


    /**
     * the write door.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/13/2001
     */
    private Door writeDoor = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    RCO() {
	super();
	try {
	    setReadDoor(new Door(Door.STANDARD));
	    setWriteDoor(new Door(Door.STANDARD));
	} catch (java.lang.InterruptedException e) {
	}
    }

    /**
     * Class constructor to build an <code>RCO</code> for a connection to a
     * <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI the connection object.
     * @param rbnbI	  the <code>ServerHandler</code>.
     * @since V2.0
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    RCO(Object serverSideI,ServerHandler rbnbI) {
	this();
	try {
	    setReadDoor(new Door(Door.STANDARD));
	    setWriteDoor(new Door(Door.STANDARD));
	} catch (java.lang.InterruptedException e) {
	}
	setServerSide(serverSideI);
	setServerHandler(rbnbI);
    }

    /**
     * Provide requested information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param askI the request for information.
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
     * @version 08/05/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/05/2004  INB	Added in-line documentation.
     * 04/29/2004  INB	Route from requires router permission.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 03/31/2003  INB	Fail routes from servers that don't agree on our name.
     * 05/09/2001  INB	Created.
     *
     */
    private final void ask(Ask askI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable answer = null;

	try {
	    if (askI.getInformationType().equalsIgnoreCase(Ask.REGISTERED)) {
		// If the request is for registered information, then pass it
		// to the <code>RoutingMapHandler</code>.
		Rmap rmap = (Rmap) askI.getAdditional().firstElement(),
		     pmap;

		if (!isSupported(IsSupported.FEATURE_REQUEST_LEAF_NODES)) {
		    // Mark leaf nodes.
		    rmap.markLeaf();
		}

		// Match the request map against the routing map.
		answer =
		    getServerHandler().getRoutingMapHandler
		    ().getRegistered(rmap);
		if (answer == null) {
		    answer = new Rmap();
		}

	    } else if (askI.getInformationType().equalsIgnoreCase
		       (Ask.CHILDAT)) {
		// Request is for a child of our client handler.
		int index = ((Integer)
			     askI.getAdditional().firstElement()).intValue();

		answer = getClientHandler().getChildAt(index);

	    } else if (askI.getInformationType().equalsIgnoreCase
		       (Ask.ISRUNNING)) {
		// If we're looking to see if something is running, then check
		// it now.
		Client client = (Client) askI.getAdditional().firstElement();

		// Locate the client handler matching the requested client.
		ClientHandler handler = (ClientHandler)
		    getServerHandler().findChild((Rmap) client);

		if ((handler == null) ||
		    !handler.isRunning() ||
		    (handler.getThread() == null) ||
		    !handler.getThread().isAlive()) {
		    // If the client handler doesn't exist, has no thread, or
		    // if its thread is dead, then the client handler is not
		    // considered to be running.
		    answer = new Stop(client);

		} else {
		    // Otherwise, the client handler is running.
		    answer = new Start(client);
		}

	    } else if (askI.getInformationType().equalsIgnoreCase
		       (Ask.REQUESTAT)) {
		// Request is to match a child at a index. The response to this
		// request is a <code>Ping</code> unless an error occurs.
		if (getClientHandler() instanceof SinkHandler) {
		    // Only sink handlers (NBOs) can handle the request.
		    int index =
			((Integer)
			 askI.getAdditional().firstElement()).intValue();
		    ((SinkHandler)
		     getClientHandler()).initiateRequestAt(index);

		} else {
		    // For other client handlers, we throw an illegal argument
		    // exception.
		    answer = Language.exception
			(new java.lang.IllegalArgumentException
			    ("Cannot match request " +
			     askI.getInformationType() +
			     " from " + this + "."));
		}

	    } else if (askI.getInformationType().equalsIgnoreCase
		       (Ask.ROUTEFROM)) {
		// If the request is to accept a routing connection from
		// another server, then try to do so.
		if (!isAllowedAccess
		    (((RBNB) getServerHandler()).getAddressHandler(),
		     AddressPermissions.ROUTER)) {
		    // If the server is not allowed access, then refuse the
		    // connection.
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to start " +
			 "routing connections.");
		}

		// Get the argument, which consist of:
		//    the type of routing connection to create,
		//    the ancestral server hierarchy of the remote server, and
		//    the ancestral server hierarchy that the remote server
		//	 wishes to connect to.
		String type = (String) askI.getAdditional().firstElement();
		Rmap hierarchy = (Rmap) askI.getAdditional().elementAt(1),
		    hierarchy2 = (askI.getAdditional().size() <= 2) ? null :
		    (Rmap) askI.getAdditional().lastElement();
		boolean goodName = true;
		if (hierarchy2 != null) {
		    // If the remote server specified anything for our
		    // hierarchy, then make sure that the remote server has
		    // correctly specified our ancestral server hierarchy.
		    // This check ensures that the two servers are in sync with
		    // each other.
		    Rmap h2Bottom = hierarchy2.moveToBottom();
		    if (h2Bottom.getFullName().compareTo
			(getServerHandler().getFullName()) != 0) {
			answer = Language.exception
			    (new java.lang.IllegalArgumentException
				("Cannot accept route from " + type + " " +
				 hierarchy +
				 ".\nMy name (" +
				 getServerHandler().getFullName() +
				 ") does not match the name (" +
				 h2Bottom.getFullName() +
				 ") expected by the remote server."));
			goodName = false;
		    }
		}

		if (goodName) {
		    // If we found a good name (either because the remote
		    // didn't specify one or because it specified the right
		    // one), then try to accept the route.

		    if (getClientHandler() instanceof RouterHandler) {
			// If our client handler is a router handler, then this
			// is just the next step along the way of setting it
			// up.
			answer = ((RouterHandler)
				  getClientHandler()).acceptRouteFrom
			    (type,
			     hierarchy);

		    } else {
			// Throw an illegal argument exception if the client
			// handler is not a router handler.
			answer = Language.exception
			    (new java.lang.IllegalArgumentException
				("Cannot accept route from " + type + " " +
				 hierarchy + "."));
		    }
		}

	    } else {
		// Throw an illegal argument exception if the request is not
		// recognized.
		answer = Language.exception
		    (new java.lang.IllegalArgumentException
			("Unknown request " + askI.getInformationType() +
			 " sent to " + this + "."));
	    }

	} catch (java.lang.RuntimeException e) {
	    getLog().addException
		(Log.STANDARD,
		 getLogClass(),
		 toString(),
		 e);
	    answer = Language.exception(e);
	}

	if (answer != null) {
	    send(answer);
	}
    }

    /**
     * Assigns the connection represented by the input <code>RCO</code> to this
     * <code>RCO</code> as a data line.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code> representing the connection.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is already a data connection.
     * @exception java.io.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    abstract void assignConnection(RCO rcoI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Calculates the total number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes transferred.
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
    public long bytesTransferred() {
	return (metricsBytes);
    }

    /**
     * Clears the <code>Cache</code> of something that is running.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clearCacheI the <code>ClearCache</code> message.
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
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    private final void clearCache(ClearCache clearCacheI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (clearCacheI.getObject() instanceof SourceInterface) {
	    clearCache((SourceInterface) clearCacheI.getObject());
	} else {
	    throw new java.lang.IllegalArgumentException
		("Cannot clear the cache of " + clearCacheI.getObject() + ".");
	}
    }

    /**
     * ClearCaches the <code>SourceHandler</code> for a
     * <code>SourceInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceInterfaceI the <code>SourceInterface</code>.
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
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    private final void clearCache(SourceInterface sourceInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmap = getServerHandler().findChild((Rmap) sourceInterfaceI);
	if (rmap == null) {
	    throw new com.rbnb.api.AddressException
		(sourceInterfaceI + " not found.");
	} else if (!(rmap instanceof SourceHandler)) {
	    throw new com.rbnb.api.AddressException
		(sourceInterfaceI + " does not represent a source of this " +
		 "server.");
	}
	SourceHandler sHandler = (SourceHandler) rmap;
	sHandler.update(sourceInterfaceI);
	synchronized (sHandler) {
	    sHandler.setPerformClearCache(true);
	    while (sHandler.getPerformClearCache()) {
		sHandler.wait(TimerPeriod.NORMAL_WAIT);
	    }
	}
	send(Language.ping());
    }

    /**
     * Closes this <code>RCO</code>.
     * <p>
     * The close operation performs any steps necessary to shutdown anything
     * added to the connection by the <code>RCO</code> in order to talk to the
     * <code>ACO</code>. It does NOT close the connection itself.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #disconnect()
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    abstract void close()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Connect to an existing <code>ClientHandler</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the <code>ClientInterface</code> to match.
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
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    private final synchronized void connectToExistingClient
	(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmap = getServerHandler().findChild((Rmap) clientInterfaceI);

	if (rmap == null) {
	    throw new com.rbnb.api.AddressException
		(clientInterfaceI + " not found.");
	} else if (!(rmap instanceof ClientHandler)) {
	    throw new com.rbnb.api.AddressException
		(clientInterfaceI + " does not represent a client of this " +
		 "server.");
	}
	ClientHandler cHandler = (ClientHandler) rmap;

	cHandler.assignConnection(this,clientInterfaceI);
    }

    /**
     * Converts this <code>ACO</code> to an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @return the <code>RCO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    abstract ACO convertToACO(Client clientI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException;

    /**
     * Disconnects from the <code>ACO</code>.
     * <p>
     * The disconnect operation shuts down the connection to the
     * <code>ACO</code> completely. It assumes that the <code>close</code>
     * operation has already been performed.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #close()
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    abstract void disconnect()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build date.
     * @see #setBuildDate(java.util.Date)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final java.util.Date getBuildDate() {
	return (buildDate);
    }

    /**
     * Gets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build version string.
     * @see #setBuildVersion(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final String getBuildVersion() {
	return (buildVersion);
    }

    /**
     * Gets the <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ClientHandler</code>.
     * @see #setClientHandler(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final ClientHandler getClientHandler() {
	return (clientHandler);
    }

    /**
     * Gets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the license string.
     * @see #setLicenseString(String)
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
    /*
    public final String getLicenseString() {
	return (licenseString);
    }
    */

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
	if (getServerHandler() != null) {
	    logR = getServerHandler().getLog();
	} else if (getClientHandler() != null) {
	    logR = getClientHandler().getLog();
	}

	return (logR);
    }

    /**
     * Gets the log class mask for this <code>RCO</code>.
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
	return (Log.CLASS_RCO);
    }

    /**
     * Gets the base log level for this <code>RCO</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log errors at standard level if we do not have a client
     *			handler.
     * 01/11/2002  INB	Created.
     *
     */
    public final byte getLogLevel() {
	return ((getClientHandler() == null) ?
		Log.STANDARD :
		getClientHandler().getLogLevel());
    }

    /**
     * Gets the read door.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the read door.
     * @see #setReadDoor(com.rbnb.api.Door)
     * @since V2.0
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    final Door getReadDoor() {
	return (readDoor);
    }

    /**
     * Gets the <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @see #setServerHandler(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final ServerHandler getServerHandler() {
	return (rbnb);
    }

    /**
     * Gets the server-side communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the server-side communications object.
     * @see #setServerSide(java.lang.Object)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final Object getServerSide() {
	return (serverSide);
    }

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return has a stop been requested?
     * @see #setTerminateRequested(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
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
     * 05/10/2001  INB	Created.
     *
     */
    final Thread getThread() {
	return (thread);
    }

    /**
     * Gets the write door.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the write door.
     * @see #setWriteDoor(com.rbnb.api.Door)
     * @since V2.0
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    final Door getWriteDoor() {
	return (writeDoor);
    }

    /**
     * Interrupts this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.SecurityException
     *		  thrown if the interrupt is not allowed by the security
     *		  system.
     * @see java.lang.Thread#interrupt()
     * @since V2.0
     * @version 05/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2001  INB	Created.
     *
     */
    public final void interrupt() {
	if (getThread() != null) {
	    getThread().interrupt();
	}
    }

    /**
     * Is this <code>RCO<code> allowed the specified access?
     * <p>
     * This implementation allows all access.  Subclasses should override this
     * method if they wish to actually check.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the local <code>Address</code> object.
     * @param accessI  the desired access.
     * @return was access allowed?
     * @exception com.rbnb.api.AddressException
     *		  if an error occured.
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    boolean isAllowedAccess(Address addressI,int accessI)
	throws com.rbnb.api.AddressException
    {
	return (true);
    }

    /**
     * Is a feature supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @param featureI the feature.
     * @return is the feature supported?
     * @see com.rbnb.api.IsSupported
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
    boolean isSupported(int featureI) {
	return (true);
    }

    /**
     * Is anything waiting to be read?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is anything waiting to be read?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    abstract boolean isWaiting()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Logs into this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param loginI the <code>Login</code> message.
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
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/07/2004  MJM  Commented out the EMF debug
     * 08/12/2004  EMF  Added logging on enter and exit, as debug for routing
     *                  lockups over ratty networks.
     * 04/28/2004  INB	Block reconnects to client handlers that the client
     *			is not authorized to start.
     * 04/17/2003  INB	Allow clients to try to reconnect to an existing
     *			handler.
     * 05/08/2001  INB	Created.
     *
     */
    synchronized void login(Login loginI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//	try {
//	    getLog().addMessage
//		(Log.STANDARD,
//		 getLogClass(),
//		 "RCO",
//		 "RCO.login commencing");
//	} catch (java.lang.Throwable e) {
//	}
	ClientInterface clientInterface =
	    (ClientInterface) loginI.getChildAt(0);

	// Save the login information.
	setBuildDate(loginI.getBuildDate());
	setBuildVersion(loginI.getBuildVersion());
	//	setLicenseString(loginI.getLicenseString());

	if (clientInterface.tryReconnect()) {
	    // If this client is trying something that should do a
	    // reconnect, then perform that operation.
	    Rmap rmap = getServerHandler
		().findChild((Rmap) clientInterface);

	    if (rmap != null) {
		ClientHandler clientHandler = (ClientHandler) rmap;
		if (!clientHandler.allowReconnect(loginI.getUsername())) {
		    throw new java.lang.IllegalStateException
		        ("Cannot reconnect to existing client handler " +
		         clientHandler.getFullName() +
		         ".");
		} else if ((clientInterface instanceof RouterInterface) &&
			   !isAllowedAccess
			   (((RBNB) getServerHandler
			     ()).getAddressHandler(),
			    AddressPermissions.ROUTER)) {
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to reconnect " +
			 "to routing connections.");
		} else if ((clientInterface instanceof
			    ControllerInterface) &&
			   !isAllowedAccess
			   (((RBNB) getServerHandler
			     ()).getAddressHandler(),
			    AddressPermissions.CONTROL)) {
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to reconnect " +
			 "to control connections.");
		} else if ((clientInterface instanceof PlugInInterface) &&
			   !isAllowedAccess
			   (((RBNB) getServerHandler
			     ()).getAddressHandler(),
			    AddressPermissions.PLUGIN)) {
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to reconnect " +
			 "to plugin connections.");
		} else if ((clientInterface instanceof SinkInterface) &&
			   !isAllowedAccess
			   (((RBNB) getServerHandler
			     ()).getAddressHandler(),
			    AddressPermissions.SINK)) {
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to reconnect " +
			 "to sink connections.");
		} else if ((clientInterface instanceof SourceInterface) &&
			   !isAllowedAccess
			   (((RBNB) getServerHandler
			     ()).getAddressHandler(),
			    AddressPermissions.SOURCE)) {
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to reconnect " +
			 "to source connections.");
		}
		clientHandler.setUsername(loginI.getUsername());
		clientHandler.setRCO(this);
		setClientHandler(clientHandler);
		clientHandler.reconnect(clientInterface);
	    }
	}

	if (getClientHandler() == null) {
	    synchronized (getServerHandler()) {
		getServerHandler().uniqueName(clientInterface);
		setClientHandler(getServerHandler().createClientHandler
				 (this,
				  clientInterface));
	    }
	    getClientHandler().setUsername(loginI.getUsername());
	}

	// Let the application know what is going on.
	Server myServer = (Server) ((Server)
				    getServerHandler()).newInstance();
	try {
	    Rmap myClient = (Rmap)
		clientInterface.getClass().newInstance();
	    myClient.setName(getClientHandler().getName());
	    getThread().setName("RCO." + myClient.getName());
	    myServer.addChild(myClient);
	    send(myServer);
	} catch (java.lang.IllegalAccessException e) {
	    throw new com.rbnb.api.AddressException
		("Failed to gain access.");
	} catch (java.lang.InstantiationException e) {
	    throw new com.rbnb.api.AddressException
		("Failed to instantiate client handler.");
	}
//	try {
//	    getLog().addMessage
//		(Log.STANDARD,
//		 getLogClass(),
//		 "RCO",
//		 "RCO.login complete");
//	} catch (java.lang.Throwable e) {
//	}
    } //end login method

    /**
     * Makes a request via a <code>Sink</code> connection to our local RBNB
     * server.
     * <p>
     * This capability allows for routed requests.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>Rmap</code> request.
     * @return the <code>Rmap</code> response:
     *	       <br><ul>
     *	       <li>null is returned if the timeout period is exceeded before a
     *		   response is seen,</li>
     *	       <li>an <code>EndOfStream</code> object is returned if the
     *		   request ends normally, or</li>
     *	       <li>an <code>Rmap</code> containing the response is
     *		   returned.</li>
     *	       </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see com.rbnb.api.EndOfStream
     * @see com.rbnb.api.Rmap
     * @since V2.0
     * @version 05/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2001  INB	Created.
     *
     */
    private final Rmap makeRequest(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	ServerHandler sHandler = (ServerHandler)
	    getClientHandler().getParent();
	Server server = Server.newServerHandle
	    (sHandler.getName(),
	     sHandler.getAddress());
	server.setServerSide(sHandler);
	Sink sink = server.createRAMSink
	    ("_snk." + getClientHandler().getName());
	sink.setCframes(1);
	sink.start();
	sink.addChild(requestI);
	sink.initiateRequestAt(0);
	Rmap rmapR = sink.fetch(Sink.FOREVER);
	sink.stop();
	return (rmapR);
    }

    /**
     * Creates a new <code>RCO</code> for the specified object.
     * <p>
     * This version uses the appropriate remote connection logic for the
     * <code>ServerHandler</code> address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI the server-side communications object.
     * @param rbnbI	  the <code>ServerHandler</code>.
     * @return the <code>RCO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem during this operation.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final static RCO newRCO(Object serverSideI,ServerHandler rbnbI)
	throws com.rbnb.api.AddressException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RCO rcoR = null;

	if (serverSideI instanceof RAMCommunications) {
	    rcoR = new RAMRCO(serverSideI,rbnbI);

	} else {
	    String strAddr = (String) rbnbI.getAddress();
	    int ss = strAddr.indexOf("://");
	    if (ss == -1) {
		rcoR = new TCPRCO(serverSideI,rbnbI);
	    } else if (strAddr.substring(0,ss).equalsIgnoreCase("INTERNAL")) {
		rcoR = new RAMRCO(serverSideI,rbnbI);
	    } else if (strAddr.substring(0,ss).equalsIgnoreCase("TCP")) {
		rcoR = new TCPRCO(serverSideI,rbnbI);
	    } else {
		throw new com.rbnb.api.AddressException
		    (strAddr + " is not a valid address.");
	    }
	}

	return (rcoR);
    }

    /**
     * Processes a <code>Serializable</code> message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>Serializable<code>.
     * @return should this <code>RCO</code> continue running?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrwon if this operation is interrupted.
     * @since V2.0
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	Router privilege is required to reverse a route and
     *			control to start a mirror.  Exceptions caught when
     *			there is no client handler are propagated rather than
     *			simply reported.  This will force the connection to
     *			close down during login sequences.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/08/2004  INB	Added handling of <code>ClearCache</code>.
     * 07/30/2003  INB	Added support for <code>DeleteChannels</code>.
     * 06/11/2003  INB	Added support for <code>RequestOptions</code>.
     * 02/26/2003  INB	When <code>Pings</code> with data are used, they
     *			contain a sequence number that should monotonically
     *			increase.
     * 02/25/2003  INB	Use the <code>Ping</code> read from the remote as the
     *			<code>Ping</code> used in the reply.
     * 05/18/2001  INB	Created.
     *
     */
    final boolean process(Serializable messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Object[] values;
	try {
	    if (getTerminateRequested()) {
		return (false);

	    } else if (getThread().interrupted()) {
		throw new java.lang.InterruptedException();

	    } else if (messageI instanceof Login) {
		login((Login) messageI);

	    } else if (messageI instanceof Username) {
		getClientHandler().setUsername((Username) messageI);
		send(Language.ping());

	    } else if (messageI instanceof ClientInterface) {
		connectToExistingClient((ClientInterface) messageI);
		return (false);

	    } else if (getClientHandler() == null) {
		if (getTerminateRequested()) {
		    return (false);
		}
		throw new java.lang.IllegalStateException
		    (messageI +
		     " cannot be processed without a client " +
		     "handler. Please login first.");

	    } else if (messageI instanceof Acknowledge) {
		if (getClientHandler() instanceof SinkHandler) {
		    ((SinkHandler) getClientHandler()).acknowledge
			((Acknowledge) messageI);
		} else {
		    throw new java.lang.IllegalStateException
			(messageI +
			 " cannot be added to a non-sink client " +
			 getClientHandler().getFullName());
		}

	    } else if (Language.isRegister(messageI)) {
		register((Register) messageI);

	    } else if (messageI instanceof RequestOptions) {
		if (getClientHandler() instanceof SinkHandler) {
		    ((SinkHandler) getClientHandler()).setRequestOptions
			((RequestOptions) messageI);
		} else {
		    throw new com.rbnb.api.SerializeException
			(messageI + " is only supported by sinks.");
		}

	    } else if (messageI instanceof DeleteChannels) {
		Rmap response;
		if (getClientHandler() instanceof SourceHandler) {
		    response =
			((SourceHandler) getClientHandler()).deleteChannels
			((Rmap) ((DeleteChannels) messageI).getObject());
		    send(response);

		} else {
		    throw new com.rbnb.api.SerializeException
			(messageI + " is only supported by sources.");
		}

	    } else if (Language.isRequest(messageI)) {
		ask((Ask) messageI);

	    } else if (messageI instanceof ClearCache) {
		clearCache((ClearCache) messageI);
		
	    } else if (messageI instanceof GetAddressAuthorization) {
		String auth;
		try {
		    auth = ((RBNB) getServerHandler()).getAddressHandler()
		    		.getAuthorization().toString();
		} catch (Exception e) {
		    auth = "ALLOW *";
		}
		send(new DataBlock(auth, 1, auth.length()));

	    } else if (messageI instanceof SetAddressAuthorization) {
		try {
		    String auth = ((SetAddressAuthorization) messageI)
		    		.getAuthorization();
		    ((RBNB) getServerHandler()).getAddressHandler()
			    .setAuthorization(new AddressAuthorization(
			    new java.io.ByteArrayInputStream(auth.getBytes())
		    ));
		} catch (Exception e)
		{ e.printStackTrace(); throw new com.rbnb.api.SerializeException(e.getMessage()); }

	    } else if (messageI instanceof Reset) {
		reset((Reset) messageI);

	    } else if (messageI instanceof ReverseRoute) {
		if (getClientHandler() instanceof ControllerHandler) {
		    if (!isAllowedAccess
			(((RBNB) getServerHandler()).getAddressHandler(),
			 AddressPermissions.ROUTER)) {
			throw new com.rbnb.api.AddressException
			    ("Client address is not authorized to " +
			     "reverse routing.");
		    } else if (((ControllerHandler)
				getClientHandler()).reverseRoute
			((ReverseRoute) messageI)) {
			return (false);
		    }

		} else {
		    throw new java.lang.IllegalStateException
			(messageI +
			 " cannot be executed by non-controller client " +
			 getClientHandler().getFullName());
		}

	    } else if (Language.isStart(messageI)) {
		start((Start) messageI);

	    } else if (Language.isStop(messageI)) {
		stop((Stop) messageI);

	    } else if (!getClientHandler().isRunning()) {
		if (getTerminateRequested()) {
		    return (false);
		}
		throw new com.rbnb.api.SerializeException
		    ("Message is out of sequence: " + messageI +
		     "\nThe client handler has not been started yet.");

	    } else if (Language.isOK(messageI)) {
		Ping ping = (Ping) messageI;
		if (ping.getHasData()) {

		    /*
		    if ((ping.getData() % 2) == 0) {
			// Ignore even pings.  This should cause problems for
			// the remote router.
			return (true);
		    }
		    */

		    /*
		    abortIt(pingValue);
		    */
		    if (ping.getData() != pingValue) {
			throw new com.rbnb.api.SerializeException
			    ("Received out-of-order ping. Expected " +
			     pingValue + ", got " +
			     (ping.getHasData() ?
			      Short.toString(ping.getData()) :
			      "no counter") +
			     ".");
		    }
		    ++pingValue;
		}

		getClientHandler().synchronizeWclient();
		send((Ping) messageI);

	    } else if (messageI instanceof PeerUpdate) {
		if (getClientHandler() instanceof RouterHandler) {
		    ((RouterHandler) getClientHandler()).updatePeer
			((PeerUpdate) messageI);
		    send(Language.ping());
		} else {
		    throw new java.lang.IllegalStateException
			(messageI +
			 " cannot be processed by " +
			 getClientHandler().getFullName());
		}

	    } else if (messageI instanceof RoutedMessage) {
		if (getClientHandler() instanceof RouterHandler) {
		    Serializable serializable =
			((RouterHandler) getClientHandler()).deliver
			((RoutedMessage) messageI);
		    if (serializable != null) {
			send(serializable);
		    }

		} else {
		    throw new java.lang.IllegalStateException
			(messageI +
			 " cannot be processed by " +
			 getClientHandler().getFullName());
		}

	    } else if (messageI instanceof Mirror) {
		if (isAllowedAccess
		    (((RBNB) getServerHandler()).getAddressHandler(),
		     AddressPermissions.CONTROL)) {
		    new MirrorController
			((ServerHandler) getClientHandler().getParent(),
			 (Mirror) messageI);
		    send(Language.ping());
		} else {
		    throw new com.rbnb.api.AddressException
			("Client address is not authorized to start mirrors.");
		}

	    } else if (messageI instanceof Rmap) {
		if ((getClientHandler() instanceof SourceHandler) ||
		    (getClientHandler() instanceof PlugInHandler)) {
		    getClientHandler().addChild((Rmap) messageI);

		} else {
		    throw new java.lang.IllegalStateException
			(messageI +
			 " cannot be added to a non-source/plugin client " +
			 getClientHandler().getFullName());
		}

	    } else if (messageI instanceof ExceptionMessage) {
		getClientHandler().exception((ExceptionMessage) messageI);

	    } else {
		throw new java.lang.IllegalStateException
		    (messageI + " is not a type that can be " +
		     "processed.");
	    }

	} catch (com.rbnb.api.RBNBException e) {
	    if (getClientHandler() == null) {
		// If there is no client handler, simply propagate the
		// exception.
		Language.throwException(e);
	    }
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     toString(),
		     e);
	    } catch (java.lang.Throwable e1) {
	    }
	    send(Language.exception(e));

	} catch (java.lang.RuntimeException e) {
	    if (getClientHandler() == null) {
		// If there is no client handler, simply propagate the
		// exception.
		Language.throwException(e);
	    }
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     toString(),
		     e);
	    } catch (java.lang.Throwable e1) {
	    }
	    send(Language.exception(e));
	}

	return (true);
    }

    /*
    private static boolean abortedPing = false;
    private final static synchronized void abortIt(short pingValueI)
	throws com.rbnb.api.SerializeException
    {
	if (!abortedPing && (pingValueI == 5)) {
	    abortedPing = true;
	    throw new com.rbnb.api.SerializeException("Abort a router.");
	}
    }
    */

    /**
     * Receives a message from the <code>ACO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>= 0 means read anything that is already waiting,</li>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li><code>Client.FOREVER</code> means wait until a message shows
     *		  up or the connection is terminated.</li>
     *	      </ul>
     * @return the <code>Serializable</code> message received or null on a
     *	       timeout.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #send(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    abstract Serializable receive(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Updates the registration for the associated <code>RBO</code> or
     * <code>PlugInHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param registerI the <code>Register</code> object.
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
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    private final void register(Register registerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable answer = null;

	try {
	    if (getClientHandler() instanceof SourceHandler) {
		if (registerI.getReplace()) {
		    throw new java.lang.IllegalArgumentException
			("Cannot replace the entire registration for a " +
			 "source connection.\n" + registerI);
		}
		((SourceHandler) getClientHandler()).register
		    ((Rmap) registerI.getObject());
		answer = Language.ping();
	    } else if (getClientHandler() instanceof PlugInHandler) {
		if (registerI.getReplace()) {
		    ((PlugInHandler) getClientHandler()).reRegister
			((Rmap) registerI.getObject());
		} else {
		    ((PlugInHandler) getClientHandler()).register
			((Rmap) registerI.getObject());
		    answer = Language.ping();
		}
	    } else {
		throw new com.rbnb.api.SerializeException
		    ("Cannot register with " +
		     this +
		     ", it has no registration map.");
	    }

	} catch (java.lang.RuntimeException e) {
	    getLog().addException
		(Log.STANDARD,
		 getLogClass(),
		 toString(),
		 e);
	    answer = Language.exception(e);
	}

	if (answer != null) {
	    send(answer);
	}
    }

    /**
     * Resets something running.
     * <p>
     *
     * @author Ian Brown
     *
     * @param resetI the <code>Reset</code> message.
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
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    private final void reset(Reset resetI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (resetI.getObject() instanceof SourceInterface) {
	    reset((SourceInterface) resetI.getObject());
	} else {
	    throw new java.lang.IllegalArgumentException
		("Cannot reset " + resetI.getObject() + ".");
	}
    }

    /**
     * Resets the <code>SourceHandler</code> for a
     * <code>SourceInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceInterfaceI the <code>SourceInterface</code>.
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
     * @see #start(com.rbnb.api.ClientInterface)
     * @since V2.0
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	Control permission is required to reset a source
     *			belonging to someone else.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 05/09/2001  INB	Created.
     *
     */
    private final void reset(SourceInterface sourceInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmap = getServerHandler().findChild((Rmap) sourceInterfaceI);
	if (rmap == null) {
	    throw new com.rbnb.api.AddressException
		(sourceInterfaceI + " not found.");
	} else if (!(rmap instanceof SourceHandler)) {
	    throw new com.rbnb.api.AddressException
		(sourceInterfaceI + " does not represent a source of this " +
		 "server.");
	}

	SourceHandler sHandler = (SourceHandler) rmap;
	if ((sHandler == getClientHandler()) ||
	    isAllowedAccess
	    (((RBNB) getServerHandler()).getAddressHandler(),
	     AddressPermissions.CONTROL)) {
	    sHandler.update(sourceInterfaceI);
	    synchronized (sHandler) {
		sHandler.setPerformReset(true);
		while (sHandler.getPerformReset()) {
		    sHandler.wait(TimerPeriod.NORMAL_WAIT);
		}
	    }
	} else {
	    throw new com.rbnb.api.AddressException
		("Client address is not authorized to reset other clients.");
	}
	send(Language.ping());
    }

    /**
     * Runs this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.0
     * @version 05/27/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/27/2004  INB	Don't log EOF exceptions.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 02/09/2004  INB	Assume that I/O exceptions mean that there is a problem
     *			communicating with the client and don't try to send
     *			the exception.
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     *		   INB	Ensure that we've released all <code>Locks</code> when
     *			we go around the loop.
     * 04/30/2003  INB	Handle Java <code>Errors</code>.
     * 04/07/2003  INB	If an <code>SocketException</code> occurs for an
     *			<code>RCO</code> with an <code>SourceHandler</code>
     *			client handler, call <code>stopOnIOException</code>.
     * 04/04/2003  INB	Handle Java errors.
     * 05/09/2001  INB	Created.
     *
     */
    public final void run() {

	Serializable message;
	try {
	    while (!getTerminateRequested() && !getThread().interrupted()) {
		getReadDoor().setIdentification(this + "_read");
		getWriteDoor().setIdentification(this + "_write");

		if ((message = receive(TimerPeriod.NORMAL_WAIT)) == null) {
		    continue;
		}

		if (getThread().interrupted()) {
		    throw new java.lang.InterruptedException();
		}

		if (!process(message)) {
		    reversed = (message instanceof ReverseRoute);
		    break;
		}

		if (getThread() != null) {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(toString(),
			 "RCO.run",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		}
	    }

	    if (getTerminateRequested() ||
		stopMyself ||
		getThread().interrupted()) {
		throw new java.lang.InterruptedException();
	    }

	} catch (java.lang.InterruptedException e) {
	} catch (java.net.SocketException e) {
	    try {

		if (getClientHandler() == null) {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  toString(),
					  e);

		}

		if (getClientHandler() instanceof SourceHandler) {
		    if (((SourceHandler)
			 getClientHandler()).stopOnIOException()) {
			setClientHandler(null);
		    }

		} else if (!stopMyself) {
		    send(Language.exception(e));
		}

	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.io.EOFException e) {
	} catch (java.io.IOException e) {
	    try {
		if (((getClientHandler() == null) ||
		     !((RBNBClient)
		       getClientHandler()).getTerminateRequested()) &&
		    !(e instanceof java.io.InterruptedIOException)) {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  toString(),
					  e);
		}

	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Exception e) {
	    try {
		if (((getClientHandler() == null) ||
		     !((RBNBClient)
		       getClientHandler()).getTerminateRequested()) &&
		    !(e instanceof java.lang.InterruptedException) &&
		    !(e instanceof java.io.InterruptedIOException)) {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  toString(),
					  e);
		}

		if (!stopMyself) {
		    send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Error e) {
	    try {
		getLog().addError(Log.STANDARD,
				  getLogClass(),
				  toString(),
				  e);
		if (!stopMyself) {
		    send(Language.exception
			 (new java.lang.Exception
			     ("A fatal error occured.\n" +
			      e.getClass() + " " + e.getMessage())));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
	    }
	}

	try {
	    getLog().addMessage
		(getLogLevel() + 10,
		 getLogClass(),
		 toString(),
		 "Terminating RCO.");
	} catch (java.lang.Throwable e) {
	}


	if (getClientHandler() != null) {
	    try {
		if (reversed) {
		    getClientHandler().setRCO(null);
		}
		getClientHandler().stop(getClientHandler());
	    } catch (java.lang.Throwable e) {
	    }
	    setClientHandler(null);
	}

	if (!reversed) {
	    try {
		send(Language.ping());
	    } catch (java.lang.Throwable e) {
	    }

	    try {
		close();
	    } catch (java.lang.Throwable e) {
	    }
	    try {
		disconnect();
	    } catch (java.lang.Throwable e) {
	    }
	}

	try {
	    getLog().addMessage
		(getLogLevel() + 11,
		 getLogClass(),
		 toString(),
		 "Terminated RCO.");
	} catch (java.lang.Throwable e) {
	}

	setThread(null);
    }

    /**
     * Sends a message to the <code>ACO</code>.
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
     * @see #receive(long)
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    abstract void send(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final void setBuildDate(java.util.Date buildDateI) {
	buildDate = buildDateI;
    }

    /**
     * Sets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version.
     * @see #getBuildVersion()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final void setBuildVersion(String buildVersionI) {
	buildVersion = buildVersionI;
    }

    /**
     * Sets the <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientHandlerI  the <code>ClientHandler</code>.
     * @see #getClientHandler()
     * @since V2.0
     * @version 01/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2004  INB	Set the former client handler.
     * 05/08/2001  INB	Created.
     *
     */
    final void setClientHandler(ClientHandler clientHandlerI) {
	clientHandler = clientHandlerI;
	if (clientHandlerI != null) {
	    wasClientHandler = clientHandlerI;
	}
    }

    /**
     * Sets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param licenseStringI the license string.
     * @see #getLicenseString()
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
    /*
    public final void setLicenseString(String licenseStringI) {
	licenseString = licenseStringI;
    }
    */

    /**
     * Sets the read door.
     * <p>
     *
     * @author Ian Brown
     *
     * @param readDoorI the read door.
     * @see #getReadDoor()
     * @since V2.0
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    private final void setReadDoor(Door readDoorI) {
	readDoor = readDoorI;
    }

    /**
     * Sets the <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbI the <code>ServerHandler</code>.
     * @see #getServerHandler()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final void setServerHandler(ServerHandler rbnbI) {
	rbnb = rbnbI;
    }

    /**
     * Sets the server-side communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI the server-side communications object.
     * @see #getServerSide()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final void setServerSide(Object serverSideI) {
	serverSide = serverSideI;
    }

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stopI  stop this <code>Controller</code>?
     * @see #getTerminateRequested()
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    final synchronized void setTerminateRequested(boolean stopI) {
	terminateRequested = stopI;
	notifyAll();
    }

    /**
     * Sets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI the thread.
     * @see #getThread()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI) {
	thread = threadI;
    }

    /**
     * Sets the write door.
     * <p>
     *
     * @author Ian Brown
     *
     * @param writeDoorI the write door.
     * @see #getWriteDoor()
     * @since V2.0
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    private final void setWriteDoor(Door writeDoorI) {
	writeDoor = writeDoorI;
    }

    /**
     * Starts this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
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
     * @see #run()
     * @see #stop()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Create a <code>ThreadWithLocks</code>.
     * 05/08/2001  INB	Created.
     *
     */
    final synchronized void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getThread() == null) {
	    setThread(new ThreadWithLocks(this));
	    getThread().start();
	}
    }

    /**
     * Starts something running.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startI the <code>Start</code> message.
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
     * @see #stop(com.rbnb.api.Stop)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    private final void start(Start startI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (startI.getObject() instanceof ClientInterface) {
	    ClientInterface clientInterface =
		(ClientInterface) startI.getObject();
	    start(clientInterface);
	} else if (startI.getObject() instanceof Shortcut) {
	    Shortcut shortcut = (Shortcut) startI.getObject();
	    start(shortcut);
	}
    }

    /**
     * Starts the <code>ClientHandler</code> for a
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the <code>ClientInterface</code>.
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
     * @see #stop(com.rbnb.api.ClientInterface)
     * @since V2.0
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	Errors starting up our own <code>ClientHandler</code>
     *			are fatal.
     * 05/09/2001  INB	Created.
     *
     */
    private final void start(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmap = getServerHandler().findChild((Rmap) clientInterfaceI);
	if (rmap == null) {
	    throw new com.rbnb.api.AddressException
		(clientInterfaceI + " not found.");
	} else if (!(rmap instanceof ClientHandler)) {
	    throw new com.rbnb.api.AddressException
		(clientInterfaceI + " does not represent a client of this " +
		 "server.");
	}
	ClientHandler cHandler = (ClientHandler) rmap;
	try {
	    cHandler.start(getClientHandler());
	} catch (java.lang.Exception e) {
	    if (cHandler == getClientHandler()) {
		stop();
	    }
	    Language.throwException(e);
	}
	send(Language.ping());

	if (cHandler instanceof PlugInInterface) {
	    cHandler.setAmNew(false);
	}
    }

    /**
     * Starts the <code>Shortcut</code> for the specified
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutInterfaceI the <code>Shortcut</code> to match.
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
     * @see #stop(com.rbnb.api.Shortcut)
     * @since V2.0
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	Starting a shortcut requires router permissions.
     * 01/08/2002  INB	Created.
     *
     */
    private final void start(Shortcut shortcutInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isAllowedAccess
	    (((RBNB) getServerHandler()).getAddressHandler(),
	     AddressPermissions.ROUTER)) {
	    throw new com.rbnb.api.AddressException
		("Client address is not authorized to start shortcuts.");
	}

	Rmap entry = getServerHandler().findDescendant
	    (Rmap.PATHDELIMITER +
	     getServerHandler().getName() +
	     Rmap.PATHDELIMITER +
	     shortcutInterfaceI.getName(),
	     false);
	Address addressR = Address.newAddress
	    (shortcutInterfaceI.getDestinationAddress());
	shortcutInterfaceI.setDestinationAddress(addressR.getAddress());

	if (entry != null) {
	    Shortcut shortcut = (Shortcut) entry;
	    if (shortcutInterfaceI.getDestinationAddress().compareTo
		(shortcut.getDestinationAddress()) != 0) {
		throw new com.rbnb.api.AddressException
		    ("Cannot start shortcut " + shortcutInterfaceI +
		     "\nIt conflicts with the existing shortcut " +
		     shortcut);
	    }
	    if (shortcut.getCost() != shortcutInterfaceI.getCost()) {
		shortcut.setCost(shortcutInterfaceI.getCost());
		getServerHandler().findPaths(null);
		((RBNB) getServerHandler()).setUpdateCounter
		    (((RBNB) getServerHandler()).getUpdateCounter() + 1);
		((RBNB) getServerHandler()).broadcastUpdate
		    (new PeerUpdate((PeerServer) getServerHandler()));
	    }

	} else {
	    ShortcutHandler scHandler = new RBNBShortcut();
	    scHandler.setName(shortcutInterfaceI.getName());
	    scHandler.setDestinationName
		(shortcutInterfaceI.getDestinationName());
	    scHandler.setDestinationAddress
		(shortcutInterfaceI.getDestinationAddress());
	    scHandler.setCost(shortcutInterfaceI.getCost());
	    scHandler.setActive(shortcutInterfaceI.getActive());
	    getServerHandler().addChild((Rmap) scHandler);
	    scHandler.start();
	}

	send(Language.ping());
    }

    /**
     * Stops this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #run()
     * @see #start()
     * @since V2.0
     * @version 01/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2004  INB	If there isn't a client handler, we aren't
     *			automatically stopping ourselves.  Use the new
     *			<code>wasClientHandler</code> field to check to see
     *			if we're stopping ourselves.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/19/2003  INB	Use <code>interrupt</code> rather than
     *			<code>Thread.interrupt</code>.
     * 05/08/2001  INB	Created.
     *
     */
    final void stop()
	throws java.lang.InterruptedException
    {
	boolean localStopMyself;
	setTerminateRequested(true);
	localStopMyself =
	    (Thread.currentThread() == getThread()) ||
	    ((wasClientHandler != null) &&
	     (Thread.currentThread() == wasClientHandler.getThread()));
	stopMyself = stopMyself || localStopMyself;

	stopMe();

	if (!stopMyself && (getClientHandler() != null)) {
	    if (!reversed) {
		try {
		    send(Language.ping());
		} catch (java.lang.Throwable e) {
		}
		try {
		    close();
		} catch (java.lang.Throwable e) {
		}
		try {
		    disconnect();
		} catch (java.lang.Throwable e) {
		}
	    }
	}
	
	if (!localStopMyself) {
	    synchronized (this) {
		for (long startAt = System.currentTimeMillis(),
			 nowAt = System.currentTimeMillis();
		     ((nowAt - startAt < TimerPeriod.SHUTDOWN) &&
		      (getThread() != null) &&
		      getThread().isAlive());
		     nowAt = System.currentTimeMillis()) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}
	    }
	    
	    interrupt();
	}
    }

    /**
     * Performs special stop code based on the subclass of <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Created.
     *
     */
    void stopMe() {
    }

    /**
     * Stops something running.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stopI the <code>Stop</code> message.
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
     * @see #start(com.rbnb.api.Start)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    private final void stop(Stop stopI)
	throws com.rbnb.api.AddressException,
					com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (stopI.getObject() instanceof ClientInterface) {
	    ClientInterface clientInterface =
		(ClientInterface) stopI.getObject();
	    stop(clientInterface);

	} else if (stopI.getObject() instanceof Server) {
	    Server serverInterface =
		(Server) stopI.getObject();
	    stop(serverInterface);

	} else if (stopI.getObject() instanceof Shortcut) {
	    Shortcut shortcutInterface =
		(Shortcut) stopI.getObject();
	    stop(shortcutInterface);
	}
    }

    /**
     * Stops the <code>ClientHandler</code> for a
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the <code>ClientInterface</code>.
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
     * @see #start(com.rbnb.api.ClientInterface)
     * @since V2.0
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	In order to stop something other than the local client
     *			requires control permission.
     * 04/01/2003  INB	Simply acknowledge <code>Stops</code> of non-existent
     *			children.
     * 05/09/2001  INB	Created.
     *
     */
    private final void stop(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmap = getServerHandler().findChild((Rmap) clientInterfaceI);
	if (rmap == null) {
	    send(Language.ping());
	    return;
	} else if (!(rmap instanceof ClientHandler)) {
	    throw new com.rbnb.api.AddressException
		(clientInterfaceI + " does not represent a client of this " +
		 "server.");
	}
	ClientHandler cHandler = (ClientHandler) rmap;
	if (clientInterfaceI instanceof SourceInterface) {
	    SourceHandler sHandler = (SourceHandler) cHandler;
	    SourceInterface sInterface = (SourceInterface) clientInterfaceI;
	    sHandler.setAkeep(sInterface.getAkeep());
	    sHandler.setCkeep(sInterface.getCkeep());
	}
	if (cHandler == getClientHandler()) {
	    stop();
	} else {
	    // Check to see that we've got the proper permissions to stop
	    // someone else.
	    if (isAllowedAccess
		(((RBNB) getServerHandler()).getAddressHandler(),
		 AddressPermissions.CONTROL)) {
		cHandler.stop(getClientHandler());
		send(Language.ping());
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Clien address is not authorized to terminate " +
		     "other clients.");
	    }
	}
    }

    /**
     * Stops the <code>ServerHandler</code> for the specified
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverInterfaceI the <code>Server</code> to match.
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
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	In order to stop the local server requires control
     *			permission, while terminating routes requires routing
     *			permission.
     * 03/31/2003  INB	Use <code>lostRouting</code> to stop a server.
     * 03/19/2003  INB	If we get a two level server hierarchy, with the top
     *			level having no name, assume that we are looking for
     *			our server.
     * 05/09/2001  INB	Created.
     *
     */
    private final void stop(Server serverInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmap = null,
	     bottom = ((Rmap) serverInterfaceI).moveToBottom();

	if (serverInterfaceI.getName() != null) {
	    rmap = getServerHandler().getRoutingMapHandler().findDescendant
		(bottom.getFullName(),
		 false);

	} else {
	    if ((bottom != serverInterfaceI) &&
		(bottom.getName() != null) &&
		bottom.getName().equals(getServerHandler().getName())) {
		rmap = (Rmap) getServerHandler();
	    }

	    if (rmap == null) {
		throw new java.lang.IllegalStateException
		    ("Stopping a peer connection by address " +
		     "is not yet supported.");
	    }
	}

	if (rmap == null) {
	    throw new com.rbnb.api.AddressException
		(serverInterfaceI + " not found.");

	} else if (!(rmap instanceof ServerInterface)) {
	    throw new com.rbnb.api.AddressException
		(serverInterfaceI + " does not represent a server known to " +
		 "this server.");
	}

	if (rmap instanceof ServerHandler) {
	    // Ensure that access is allowed before terminating the server.
	    ServerHandler sHandler = (ServerHandler) rmap;
	    if (isAllowedAccess(((RBNB) sHandler).getAddressHandler(),
				AddressPermissions.CONTROL)) {
		sHandler.stop(getClientHandler());
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not authorized to terminate " +
		     "the server.");
	    }

	} else if (rmap instanceof ConnectedServer) {
	    // Ensure that access is allowed before terminating routing.
	    if (isAllowedAccess
		(((RBNB) getServerHandler()).getAddressHandler(),
		 AddressPermissions.ROUTER)) {
		ConnectedServer cServer = (ConnectedServer) rmap;
		cServer.lostRouting();
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not authorized to terminate routing.");
	    }

	} else {
	    throw new java.lang.IllegalArgumentException
		("Cannot stop " + serverInterfaceI.getFullName() + ".");
	}

	send(Language.ping());
    }

    /**
     * Stops the <code>ShortcutHandler</code> for the specified
     * <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutInterfaceI the <code>Shortcut</code> to match.
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
     * @see #start(com.rbnb.api.Shortcut)
     * @since V2.0
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	Terminating a shortcut requires router permission.
     * 01/08/2002  INB	Created.
     *
     */
    private final void stop(Shortcut shortcutInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	ShortcutHandler scHandler = (ShortcutHandler)
	    getServerHandler().findChild((Rmap) shortcutInterfaceI);

	if (scHandler == null) {
	    throw new com.rbnb.api.AddressException
		(shortcutInterfaceI + " not found.");
	} else if (!isAllowedAccess
		   (((RBNB) getServerHandler()).getAddressHandler(),
		    AddressPermissions.ROUTER)) {
	    throw new com.rbnb.api.AddressException
		("Client address is not authorized to terminate shortcuts.");
	}

	scHandler.stop();
	send(Language.ping());
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 02/09/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2004  INB	Added the <code>super.toString</code> call to the name
     *			with a handler.
     * 05/21/2001  INB	Created.
     *
     */
    public final String toString() {
	if (getClientHandler() != null) {
	    try {
		return ("RCO " + getClientHandler().getName() +
			" (" + super.toString() + ")");
	    } catch (java.lang.Exception e) {
		return ("RCO " + super.toString());
	    }
	} else {
	    return ("RCO " + super.toString());
	}
    }
}
