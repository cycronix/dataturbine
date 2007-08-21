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
 * Server-side representation of an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 11/19/2003  INB	Added <code>get/setCompatiblityMode</code>.
 * 04/24/2003  INB	Added <code>lock/unlockRouting</code>.
 * 03/31/2003  INB	Added <code>get/setTerminateRequested</code>.
 * 03/13/2003  INB	Added <code>set/getActivityQueue</code> and
 *			<code>set/getOpenFileSets</code>.
 * 05/09/2001  INB	Created.
 *
 */
interface ServerHandler
    extends com.rbnb.api.BuildInterface,
	    com.rbnb.api.DataSizeMetricsInterface,
	    com.rbnb.api.GetLogInterface,
	    com.rbnb.api.Interruptable,
	    com.rbnb.api.MetricsInterface,
	    com.rbnb.api.NotificationFrom,
	    com.rbnb.api.PeerInterface,
	    com.rbnb.api.RegisteredInterface,
	    com.rbnb.api.RmapInterface,
	    com.rbnb.api.RoutedTarget,
	    com.rbnb.api.TimerTaskInterface
{
    /**
     * timer task - reconnect to parent.
     * <p>
     * This code specifies that the <code>RBNB</code> should attempt to connect
     * to its parent server if it has been disconnected.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/05/2001
     */
    public final static String TT_RECONNECT = "Reconnect";

    /**
     * Adds a <code>RemoteClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rchI the <code>RemoteClientHandler</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #removeRemoteClientHandler(com.rbnb.api.RemoteClientHandler)
     * @since V2.0
     * @version 02/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    public abstract void addRemoteClientHandler(RemoteClientHandler rchI)
	throws java.lang.InterruptedException;

    /**
     * Broadcast a peer update.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateI the update to broadcast.
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
    public abstract void broadcastUpdate(PeerUpdate peerUpdateI);

    /**
     * Clones this object.
     * <p>
     * This same abstract declaration is also included in RmapInterface.java,
     * but for some unknown reason J# gives a compiler error if it is not also
     * included here.
     *
     * @author John Wilson
     *
     * @return the clone.
     * @see java.lang.Cloneable
     * @since V2.5
     * @version 09/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/29/2004  JPW	Created.
     *
     */
    public abstract Object clone();

    /**
     * Creates a <code>ClientHandler</code> for the specified <code>RCO</code>
     * and <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI	      the <code>RCO</code>.
     * @param clientIntefaceI the <code>ClientInterface</code>.
     * @return the <code>ClientHandler</code>.
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
    public abstract ClientHandler createClientHandler
	(RCO rcoI,
	 ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Finds the lowest cost <code>Paths</code> to each of the
     * <code>PeerServers</code> in the input list or all of the reachable
     * <code>PeerServers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param toFindIO the list of server names to find. On return, this will
     *		       contain the list of names not found. If this is
     *		       <code>null</code>, then all reachable
     *		       <code>PeerServers</code> will be found.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    public abstract void findPaths(SortedStrings toFindIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Finds the <code>Path</code> to the specified target
     * <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerServerI the <code>PeerServer</code> to find the
     *			  <code>Path</code> to.
     * @return the <code>Path</code> found.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
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
    public abstract Path findPathTo(PeerServer peerServerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Finds a <code>RemoteClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>RemoteClientHandler</code>.
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
     * @version 02/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/15/2002  INB	Created.
     *
     */
    public abstract RemoteClientHandler findRemoteClientHandler(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the activity queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the activity queue.
     * @see #setActivityQueue(com.rbnb.api.ActionThreadQueue)
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public abstract ActionThreadQueue getActivityQueue();

    /**
     * Gets the client side <code>Server</code> for this
     * <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the client side <code>Server</code>.
     * @see #setClientSide(com.rbnb.api.Server)
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
    public abstract Server getClientSide();

    /**
     * Gets the compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the mode.
     * @see #setCompatibilityMode(String)
     * @since V2.2
     * @version 11/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2003  INB	Created.
     *
     */
    public abstract String getCompatibilityMode();

    /**
     * Gets the log class mask for this <code>ServerHandler</code>.
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
    public abstract long getLogClass();

    /**
     * Gets the base log level for this <code>ServerHandler</code>.
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
    public abstract byte getLogLevel();

    /**
     * Gets the log status period.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the log status period in milliseconds.
     * @since V2.0
     * @version 11/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/25/2002  INB	Created.
     *
     */
    public abstract long getLogStatusPeriod();

    /**
     * Gets the metrics period.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the metrics period in milliseconds.
     * @since V2.0
     * @version 11/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/25/2002  INB	Created.
     *
     */
    public abstract long getMetricsPeriod();

    /**
     * Gets the open filesets limited resource.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the open filesets.
     * @see #setOpenFileSets(com.rbnb.api.LimitedResource)
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public abstract LimitedResource getOpenFileSets();

    /**
     * Gets the <code>RAMServerCommunications</code> object.
     * <p>
     * This allows all <code>ServerHandlers</code> to provide for
     * <code>RAM</code> based connections.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RAMServerCommunications</code> object.
     * @see #setRAMPort(com.rbnb.api.RAMServerCommunications)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  INB	Created.
     *
     */
    public abstract RAMServerCommunications getRAMPort();

    /**
     * Gets the <code>RoutingMapHandler</code> for this
     * <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RoutingMapHandler</code>.
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
    public abstract RoutingMapHandler getRoutingMapHandler();

    /**
     * Gets the server-port communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the server-port communications object.
     * @see #setServerPort(java.lang.Object)
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
    public abstract Object getServerPort();

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the terminate requested flag.
     * @see #setTerminateRequested(boolean)
     * @since V2.1
     * @version 03/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Created.
     *
     */
    public abstract boolean getTerminateRequested();

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
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
    public abstract Thread getThread();

    /**
     * Gets the <code>Timer</code> for this <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Timer</code>.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    public abstract Timer getTimer();

    /**
     * Initiates a reverse route from a peer or parent server.
     * <p>
     * This method makes a connection from this server to the specified peer on
     * behalf of that peer and then reverses its direction.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reverseI the <code>ReverseRoute</code> command.
     * @return the <code>ReverseRoute</code> command on success or an
     *	       <code>ExceptionMessage</code> on an error.
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
    public abstract Serializable initiateReverseRoute(ReverseRoute reverseI);

    /**
     * Is this <code>ServerHandler</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is it running?
     * @see #start(com.rbnb.api.ClientHandler)
     * @see #start(com.rbnb.api.ClientHandler,String[])
     * @see #stop(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public abstract boolean isRunning();

    /**
     * Gets the list of registered <code>Rmaps</code> matching the requested
     * <code>Rmap</code> hierarchy.
     * <p>
     * At this time the following <code>Rmap</code> hierarchies are
     * implemented:
     * <p><ul>
     *	  <li>{unnamed}[/...]</code>,</li>
     *    <li>[{unnamed}/]servername[/...],</li>
     *    <li>[{unnamed}/]servername/clientname[/...],</li>
     *    <li>[{unnamed}/]servername/clientname1[/....],/clientname2[/...],
     *	      ...</li>
     * </ul><p>
     * Syntax notes:
     * <br>/ indicates a child <code>Rmap</code>.
     * <br>[/...] indicates an optional child <code>Rmap</code> with a name of
     *	   "...". This  causes the registration maps of the children of the
     *	   <code>Rmap</code> to be expanded.
     * <br>, indicates multiple children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the requested <code>Rmap</code> hierarchy.
     * @return the <code>Rmap</code> hierarchy containing the registered
     *	       <code>Rmaps</code>.
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
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public abstract Rmap getRegistered(com.rbnb.api.Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Initiates a route to peer server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerHierarchyI  the peer server <code>Server</code> hierarchy
     *			      leading down to the actual peer.
     * @param localI	      the local <code>Server</code> representing the
     *			      peer, if any.
     * @return the <code>Server</code> connected.
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
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    public abstract Server initiateRouteTo(Server peerHierarchyI,
					   Server localI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Locks out further attempts to route to this <code>ServerHandler</code>
     * until the current one has completed.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #unlockRouting()
     * @since V2.1
     * @version 04/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  IAB	Created.
     *
     */
    public abstract void lockRouting()
	throws java.lang.InterruptedException;

    /**
     * Removes a <code>RemoteClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rchI the <code>RemoteClientHandler</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #addRemoteClientHandler(com.rbnb.api.RemoteClientHandler)
     * @since V2.0
     * @version 02/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    public abstract void removeRemoteClientHandler(RemoteClientHandler rchI)
	throws java.lang.InterruptedException;

    /**
     * Sets the activity queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @param activityQueueI the new activity queue.
     * @see #getActivityQueue()
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public abstract void setActivityQueue(ActionThreadQueue activityQueueI);

    /**
     * Sets the client side <code>Server</code> for this
     * <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client side <code>Server</code>.
     * @see #getClientSide()
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
    public abstract void setClientSide(Server clientSideI);

    /**
     * Sets the compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param compatibilityModeI the new mode.
     * @exception java.lang.IllegalArgumentException
     *		  if the specified mode is not supported.
     * @see #getCompatibilityMode()
     * @since V2.2
     * @version 11/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2003  INB	Created.
     *
     */
    public abstract void setCompatibilityMode(String compatibilityModeI);

    /**
     * Sets the open filesets limited resource.
     * <p>
     *
     * @author Ian Brown
     *
     * @param openFileSetsI the open filesets.
     * @see #getOpenFileSets()
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public abstract void setOpenFileSets(LimitedResource openFileSetsI);

    /**
     * Sets the <code>RAMServerCommunications</code> object.
     * <p>
     * This allows all <code>ServerHandlers</code> to provide for
     * <code>RAM</code> based connections.
     * <p>
     *
     * @author Ian Brown
     *
     * @param portI the <code>RAMServerCommunications</code> object.
     * @see #getRAMPort()
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  INB	Created.
     *
     */
    public abstract void setRAMPort(RAMServerCommunications portI);

    /**
     * Sets the server-port communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverPortI the server-port communications object.
     * @see #getServerPort
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
    public abstract void setServerPort(Object serverPortI);

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param terminateI  terminate this <code>RBNB</code>?
     * @see #getTerminateRequested()
     * @since V2.1
     * @version 03/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Created.
     *
     */
    public abstract void setTerminateRequested(boolean terminateI);

    /**
     * Starts this <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> performing the stop.
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
     * @see #stop(com.rbnb.api.ClientHandler)
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
    public abstract void start(ClientHandler clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Starts this <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> performing the stop.
     * @param argsI   the command line arguments to process.
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
     * @see #stop(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    public abstract void start(ClientHandler clientI,String[] argsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Stops this <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> performing the stop.
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
     * @see #start(com.rbnb.api.ClientHandler)
     * @see #start(com.rbnb.api.ClientHandler,String[])
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    public abstract void stop(ClientHandler clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Creates a unique name for the input <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ciIO the <code>ClientInterface</code>.
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    public abstract void uniqueName(ClientInterface ciIO)
    	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Unlocks access to this <code>ServerHandler</code> for routing.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #lockRouting()
     * @since V2.1
     * @version 04/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  INB	Created.
     *
     */
    public abstract void unlockRouting()
	throws java.lang.InterruptedException;
}
