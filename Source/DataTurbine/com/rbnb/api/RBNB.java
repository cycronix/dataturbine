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

// JPW 11/16/2006
import java.util.Random;

// JPW 06/22/2006
import java.io.File;

// JPW 11/16/2006
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * The RBNB server class.
 * <p>
 * The RBNB DataTurbine server is a network-aware middleware application that
 * allows multiple sources of high-speed data to be connected to multiple sink
 * applications.
 * <p>
 * From the point of view of a source of data (such as a data acquisition
 * program), the RBNB server acts as a perfect sink - accepting all data sent
 * to it.  The source can send the data to the server and then forget about it.
 * <p>
 * From the point of view of a sink of data (such as a strip chart display
 * program), the RBNB server acts as a perfect source - seamlessly
 * transitioning between current and historical data and merging data from
 * multiple sources on demand.
 * <p>
 * Additional features provided by the RBNB DataTurbine server include:
 * <p><ul>
 *    <li>External Server Administration,</li>
 *    <li>On-request Data Source PlugIns,</li>
 *    <li>Mirroring of Data Between Serers, and</li>
 *    <li>Routing between Servers (Parent-Child Hierarchies and
 *	  Shortcuts).</li>
 * </ul><p>
 * Control clients allow the administration of various services provided by the
 * server, including:
 * <p><ul>
 *    <li>Terminating the Server,</li>
 *    <li>Terminating Client Connections,</li>
 *    <li>Starting and Stopping Mirrors,
 *    <li>Terminaing Parent-Child Routing, and</li>
 *    <li>Starting and Stopping Shortcuts.</li>
 * </ul><p>
 * Plugin clients provide data on-demand.  When a sink makes a request of a
 * plugin, the request is sent to the plugin application, which is then
 * expected to provide the answer.  The server enables this communication by
 * passing the requests to the plugin and the responses back to the sink.
 * <p>
 * Mirrors copy the data provided by a data source on one RBNB DataTurbine
 * server to another.  In effect, a mirror consists of a sink, subscribed to
 * the original data source, and a source that puts the data into the target
 * DataTurbine server.
 * <p>
 * Routing connects DataTurbine servers together, making data in one server
 * available to sink clients on the other and, possibly, vice-versa.
 * Parent-child relationships build a hierarchy of servers using bi-directional
 * connections, while shortcuts allow one server to gain access to data in the
 * other server as if the latter were a source connected to the former.
 * <p>
 * These capabilities are documented in detail in the following:
 * <p><ol>
 *    <li>Accepting data from a source - <code>RBO.addChild</code>,</li>
 *    <li>Providing data to a sink - <code>NBO.initiateRequestAt</code>,</li>
 *    <li>Controlling the DataTurbine server -
 *	  <code>RBNBController</code>,</li>
 *    <li>Getting data on demand from a plugin - <code>RBNBPlugIn</code>,</li>
 *    <li>Mirroring data to another server -
 *	  <code>MirrorController</code>, and</li>
 *    <li>Routing between servers - <code>RemoteServer</code>.</li>
 * </ol><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.MirrorController
 * @see com.rbnb.api.NBO#initiateRequestAt(int indexI)
 * @see com.rbnb.api.RBNBController
 * @see com.rbnb.api.RBNBPlugIn
 * @see com.rbnb.api.RBO#addChild(com.rbnb.api.Rmap childI)
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 06/22/2006
 */

/*
 * Copyright 2001, 2002, 2003, 2004, 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/22/2006  JPW	Add "-H" archive home directory in parseArguments().
 *			Add archiveHomeDirectory, getArchiveHomeDirectory()
 *			and setArchiveHomeDirectory().
 *			Use archiveHomeDirectory in loadArchives().
 * 10/05/2004  JPW	In run(), add preprocessor directives which can be used
 *			by "sed" to create a version of the code appropriate
 *			for compilation under J# (which supports Java 1.1.4).
 *			In the Java 1.1.4 version, don't call addShutdownHook()
 * 07/30/2004  INB	Added some documentation.
 * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
 * 04/29/2004  INB	If a remote server is connecting to this one using
 *			routing, then that remote server needs to be able to
 *			start a reverse route via a controller.
 * 04/28/2004  INB	Use AddressPermissions objects.  Ensure that the client
 *			is allowed to create the handler before creating it.
 * 02/17/2004  INB	In <code>initiateRouteTo</code>, moved
 *			<code>receive</code> into loop to ensure that the loop
 *			is not infinite.
 * 02/12/2004  INB	Give the <code>Log</code> a little time on shutdown.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 01/16/2004  INB	Synchronize when calculating data sizes.
 *			Initialize the data byte sizes to 0 when calculating
 *			the metrics.
 * 01/07/2004  INB	Renamed the reverse route <code>Controller</code> to
 *			distinguish from a regular <code>RouteHandler</code>.
 * 11/19/2003  INB	Added -C flag.  This flag provides for best-effort
 *			compatibility with earlier versions of the software
 *			where an application get a different response from the
 *			server when using the latest version.  Added various
 *			constants and code to support this.
 * 11/17/2003  INB	Handle <code>IOExceptions</code> in <code>run</code>.
 *			Added code to limit the maximum number of connections
 *			(this includes both incoming and outgoing connections).
 *			it.
 *			Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code>.
 *			Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 09/08/2003  INB	Clear the interrupt when closing down.
 * 05/23/2003  INB	Added dot to router name when reversing routes.
 * 04/24/2003  INB	Added <code>lock/unlockRouting</code> and
 *			<code>routingDoor</code>.
 * 04/17/2003  INB	When stopping an <code>SourceHandler</code> at
 *			shutdown, clear the keep cache flag.
 * 04/10/2003  INB	Added <code>Usernames</code> to
 *			<code>RemoteClientHandlers</code>.
 * 04/03/2003  INB	Added auto load of archives capability.
 * 04/02/2003  INB	Turning off the log simply means that there is no ring
 *			buffer in the log - the object will still exist.
 * 03/31/2003  INB	Moved stop of <code>ChildServer</code> above stop of
 *			<code>RoutingMap</code>.
 * 03/26/2003  INB	When finding paths, the resulting list is sorted by
 *			name, while the intermediate list is sorted by cost.
 *			Use <code>TimerPeriod.NORMAL_WAIT</code>. Rewrote
 *			<code>getValidPaths</code>.
 * 03/25/2003  INB	Changed the path <code>Door</code> to a read/write
 *			<code>Door</code> and use it to protect attempts to
 *			access it for read as well as write.
 * 03/21/2003  INB	Log exceptions on delivery.
 * 03/20/2003  INB	Use <code>StringTokenizer</code> to parse debug
 *			settings. Use timeouts on <code>wait</code> calls.
 * 03/19/2003  INB	Ensure that we reverse routes to the right place when
 *			starting up. Ensure that peers are started.
 * 03/13/2003  INB	Added a <code>ActionThreadQueue</code> to handle
 *			various actions such <code>RingBuffer</code> adds.
 *			Added a <code>LimitedResource</code> to limit the
 *			number of simultaneous open <code>FileSets</code>.
 * 02/28/2003  INB	Reject children whose names are the same as ourself.
 *			Use the name of the local server for the log channel.
 * 02/26/2003  INB	Ensure that reversed <code>RouterHandlers</code> are
 *			correctly tied to the remote. Also ensure that the
 *			<code>Router</code> created to initiate a route ends up
 *			connected to the proper <code>Server</code> object.
 * 05/09/2001  INB	Created.
 *
 */
final class RBNB
    extends com.rbnb.api.PeerServer
    implements com.rbnb.api.ServerHandler
{
    /**
     * version 2.1 of the RBNB for compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public final static String VERSION_V2_1 = "V2.1";

    /**
     * version 2.2 of the RBNB for compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public final static String VERSION_V2_2 = "V2.2";

    /**
     * current version of the RBNB for compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public final static String VERSION_CURRENT = VERSION_V2_2;

    /**
     * compatibility versions supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public final static String[] COMPATIBLE_VERSIONS = {
	VERSION_V2_1,
	VERSION_V2_2
    };

    /**
     * activity queue to provide one or more threads to handle events.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private ActionThreadQueue activityQueue = null;

    /**
     * archive home directory (can be set via "-H" command line flag)
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.6
     * @version 06/22/2006
     */
    private String archiveHomeDirectory = ".";

    /**
     * automatically load archives in the current directory?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/03/2003
     */
    private boolean autoLoadArchives = false;

    /**
     * the client side <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/26/2001
     */
    private Server clientSide = null;
    
    /**
      * Stores the values entered with the -P flag, or null.
      * @author WHF
      * @since V2.5
      * @version 2005/01/20
      */
    private Username username;

    /**
     * compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    private String compatibilityMode = VERSION_CURRENT;

    /**
     * the current number of connections.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/17/2003
     */
    private int currentConnections = 0;

    /**
     * the debug level.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Log
     * @see #debugMask
     * @since V2.0
     * @version 08/03/2004
     */
    private byte debugLevel = (byte) Log.STANDARD;

    /**
     * the debug mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Log
     * @see #debugLevel
     * @since V2.0
     * @version 01/30/2002
     */
    private long debugMask = Log.NONE;

    /**
     * the <code>License</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.00
     * @version 10/25/2001
     */
    //    private License license = null;

    /**
     * the log for this RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Log log = null;

    /**
     * the period for logging current status.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/25/2002
     */
    private long logStatusPeriod = TimerPeriod.LOG_STATUS;

    /**
     * the maximum number of activity threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int maxActivityThreads = 100;

    /**
     * the maximum number of connections allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/17/2003
     */
    private int maximumConnections = 100;

    /**
     * the maximum number of open filesets.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int maxOpenFileSets = 25;

   /**
     * metrics <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    private Rmap metrics = null;

    /**
     * metrics: number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    private long metricsBytes = 0;

    /**
     * the period for collecting metrics.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/25/2002
     */
    private long metricsPeriod = TimerPeriod.METRICS;

    /**
     * the <code>Source</code> to write metrics to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/25/2002
     */
    private Source metricsSource = null;

    /**
     * limited resource: open filesets.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    LimitedResource openFileSets = null;

    /**
     * the <code>Path</code> finding <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private Door pathDoor = null;

    /**
     * the list of <code>Paths</code> to use to get to our peers.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private com.rbnb.utility.SortedVector paths = new
	com.rbnb.utility.SortedVector();

    /**
     * the <code>RAMServerCommunications</code> port.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/17/2001
     */
    private RAMServerCommunications ramPort = null;

    /**
     * list of <code>RemoteClientHandlers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */
    private RmapVector rClients = null;

    /**
     * Door for the <code>rClients</code> vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/15/2002
     */
    private Door rClientsDoor = null;

    /**
     * controls access to this <code>RBNB</code> by remote servers attempting
     * to initiate routes.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */
    private Door routingDoor = null;

    /**
     * the server-port communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private Object serverPort = null;

    /**
     * terminating client's name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private String terminatingClient = null;

    /**
     * the thread running this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Thread thread = null;

    /**
     * the task timer.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private Timer timer = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  INB	Initialize <code>routingDoor</code>.
     * 03/25/2003  INB	Changed path <code>Door</code> to read/write.
     * 05/11/2001  INB	Created.
     *
     */
    RBNB() {
	super();
	try {
	    setPathDoor(new Door(Door.READ_WRITE));
	    rClientsDoor = new Door(Door.READ_WRITE);
	    routingDoor = new Door(Door.STANDARD);
	} catch (java.lang.Exception e) {
	}
	setServerSide(this);
    }

    /**
     * Class constructor to build an <code>RBNB</code> from a name and an
     * address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the name.
     * @param addressI the address.
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
     * @version 04/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  INB	Initialize <code>routingDoor</code>.
     * 03/25/2003  INB	Changed path <code>Door</code> to read/write.
     * 05/11/2001  INB	Created.
     *
     */
    RBNB(String nameI,String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI,addressI);
	setPathDoor(new Door(Door.READ_WRITE));
	setServerSide(this);
	rClientsDoor = new Door(Door.READ_WRITE);
	routingDoor = new Door(Door.STANDARD);
    }

    /**
     * Class constructor to build an <code>RBNB</code> from a name and an
     * <code>Address</code> handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI	      the name.
     * @param addressHandlerI the <code>AddressHandler</code>.
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
     * @version 03/25/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  INB	Initialize <code>routingDoor</code>.
     * 03/25/2003  INB	Changed path <code>Door</code> to read/write.
     * 05/11/2001  INB	Created.
     *
     */
    RBNB(String nameI,Address addressHandlerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
	setPathDoor(new Door(Door.READ_WRITE));
	setAddressHandler(addressHandlerI);
	setServerSide(this);
	rClientsDoor = new Door(Door.READ_WRITE);
	routingDoor = new Door(Door.STANDARD);
    }

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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 12/04/2001  INB	Created.
     *
     */
    public final void addRemoteClientHandler(RemoteClientHandler rchI)
	throws java.lang.InterruptedException
    {
	try {
	    rClientsDoor.lock("RBNB.addRemoteClientHandler");
	    rClients = RmapVector.addToVector(rClients,rchI);
	} finally {
	    rClientsDoor.unlock();
	}
    }

    /**
     * Broadcast a peer update.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateI the update to broadcast.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    public final void broadcastUpdate(PeerUpdate peerUpdateI) {
	synchronized (getShortcuts()) {
	    for (int idx = 0; idx < getShortcuts().size(); ++idx) {
		try {
		    Shortcut shortcut =
			(Shortcut) getShortcuts().elementAt(idx);

		    if (!shortcut.getDestinationName().equals
			(peerUpdateI.getPeerName())) {
			Rmap rmap = getRoutingMapHandler().findDescendant
			    (shortcut.getDestinationName(),
			     false);

			if (rmap != null) {
			    ((PeerServer) rmap).passUpdate(peerUpdateI);
			}
		    }

		} catch (com.rbnb.api.AddressException e) {
		} catch (com.rbnb.api.SerializeException e) {
		} catch (java.io.IOException e) {
		} catch (java.lang.InterruptedException e) {
		}
	    }
	}
    }

    /**
     * Calculates the cache and archive data sizes in bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheDSIO   the cache data size.
     * @param archiveDSIO the archive data size.
     * @since V2.0
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Synchronize to ensure that nothing is happening while
     *			we calculate metrics.
     * 11/19/2002  INB	Created.
     *
     */
    public final void calculateDataSizes(long[] cacheDSIO,
					    long[] archiveDSIO)
    {
	try {
	    synchronized (metricsSyncObj) {
		for (int idx = 0; idx < getNchildren(); ++idx) {
		    if (getChildAt(idx) instanceof DataSizeMetricsInterface) {
			DataSizeMetricsInterface
			    dsm = (DataSizeMetricsInterface)
			    getChildAt(idx);
			dsm.calculateDataSizes(cacheDSIO,archiveDSIO);
		    }
		}
	    }
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Calculate the metrics for this <code>MetricsInterface</code> that apply
     * for the specified time range.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lastTimeI the last time metrics were calculated; if
     *			<code>Long.MIN_VALUE</code>, then this is the first
     *			time they've been calculated.
     * @param nowI	the current time.
     * @return an <code>Rmap</code> containing the metrics.
     * @see com.rbnb.api.MetricsCollector#timerTask(com.rbnb.api.TimerTask)
     * @since V2.0
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Initialize the data byte sizes to 0.
     * 11/15/2002  INB	Created.
     *
     */
    public final Rmap calculateMetrics(long lastTimeI,long nowI) {
	Rmap rmapR = null;
	try {
	    if (metrics == null) {
		metrics = new Rmap();
		metrics.addChild(new Rmap("TotalMemory"));
		metrics.addChild(new Rmap("MemoryUsed"));
		metrics.addChild(new Rmap("SocketBytes"));
		metrics.addChild(new Rmap("SocketRate"));
		metrics.addChild(new Rmap("CacheDataBytes"));
		metrics.addChild(new Rmap("ArchiveDataBytes"));
	    }

	    rmapR = metrics;
	    double duration = 1.;
	    if (lastTimeI == Long.MIN_VALUE) {
		rmapR.setTrange(new TimeRange(nowI/1000. - duration,duration));
	    } else {
		duration = (nowI - lastTimeI - 1)/1000.;
		if (duration < 0.) {
		    duration = 0.;
		}
		rmapR.setTrange(new TimeRange(nowI/1000. - duration,duration));
	    }

	    long[] totalMemory = { Runtime.getRuntime().totalMemory() };

	    rmapR.findDescendant("/TotalMemory",false).setDblock
		(new DataBlock(totalMemory,
			       1,
			       8,
			       DataBlock.TYPE_INT64,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       8));

	    long[] availableMemory = { totalMemory[0] -
				       Runtime.getRuntime().freeMemory() };
	    rmapR.findDescendant("/MemoryUsed",false).setDblock
		(new DataBlock(availableMemory,
			       1,
			       8,
			       DataBlock.TYPE_INT64,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       8));

	    long[] socketBytes = { getRoutingMapHandler().bytesTransferred() };
	    rmapR.findDescendant("/SocketBytes",false).setDblock
		(new DataBlock(socketBytes,
			       1,
			       8,
			       DataBlock.TYPE_INT64,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       8));

	    double[] socketRate = { (socketBytes[0] - metricsBytes)/duration };
	    metricsBytes = socketBytes[0];
	    rmapR.findDescendant("/SocketRate",false).setDblock
		(new DataBlock(socketRate,
			       1,
			       8,
			       DataBlock.TYPE_FLOAT64,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       8));

	    long[] cacheDS = new long[1],
		archiveDS = new long[1];
	    cacheDS[0] =
		archiveDS[0] = 0L;
	    calculateDataSizes(cacheDS,archiveDS);
	    rmapR.findDescendant("/CacheDataBytes",false).setDblock
		(new DataBlock(cacheDS,
			       1,
			       8,
			       DataBlock.TYPE_INT64,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       8));
	    rmapR.findDescendant("/ArchiveDataBytes",false).setDblock
		(new DataBlock(archiveDS,
			       1,
			       8,
			       DataBlock.TYPE_INT64,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       8));

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}

	return (rmapR);
    }

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
     * @exception java.lang.IllegalStateException
     *		  thrown if mirrors are not supported.
     * @since V2.0
     * @version 04/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/29/2004  INB	If a remote server is connecting to this one using
     *			routing, then that remote server needs to be able to
     *			start a reverse route via a controller.
     * 04/28/2004  INB	Ensure that the client is allowed to create the
     *			handler.
     * 05/10/2001  INB	Created.
     *
     */
    public final ClientHandler createClientHandler
	(RCO rcoI,
	 ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	ClientHandler cHandlerR = null;

	/*
	if (!getLicense().mirrors()) {
	    String mirrorString = "_Mirror.";
	    if (clientInterfaceI.getName().regionMatches
		(0,
		 mirrorString,
		 0,
		 mirrorString.length())) {
		throw new java.lang.IllegalStateException
		    ("This license (" +
		     getLicense().version() + " #" +
		     getLicense().serialNumber() +
		     ") does not support mirroring.");
	    }
	}
	*/

	if (clientInterfaceI instanceof RouterInterface) {
	    // Routers require routing permissions.
	    if (rcoI.isAllowedAccess(getAddressHandler(),
				     AddressPermissions.ROUTER)) {
		cHandlerR = new RBNBRouter
		    (rcoI,
		     (RouterInterface) clientInterfaceI);
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not allowed to start routing " +
		     "connections.");
	    }

	} else if (clientInterfaceI instanceof ControllerInterface) {
	    // Controllers generally require control permission, however, if
	    // the client name starts with _RR., then it is most likely a
	    // reverse router.  For those clients, we only require routing
	    // permission.
	    if ((clientInterfaceI.getName().startsWith("_RR.") &&
		 rcoI.isAllowedAccess(getAddressHandler(),
				      AddressPermissions.ROUTER)) ||
		rcoI.isAllowedAccess(getAddressHandler(),
				     AddressPermissions.CONTROL)) {
		cHandlerR = new RBNBController
		    (rcoI,
		     (ControllerInterface) clientInterfaceI);
	    } else if (clientInterfaceI.getName().startsWith("_RR.")) {
		throw new com.rbnb.api.AddressException
		    ("Client address is not allowed to start routing " +
		     "connections.");
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not allowed to start control " +
		     "connections.");
	    }

	} else if (clientInterfaceI instanceof PlugInInterface) {
	    if (rcoI.isAllowedAccess(getAddressHandler(),
				     AddressPermissions.PLUGIN)) {
		cHandlerR = new RBNBPlugIn(rcoI,
					   (PlugInInterface) clientInterfaceI);
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not allowed to start plugin " +
		     "connections.");
	    }

	} else if (clientInterfaceI instanceof SinkInterface) {
	    if (rcoI.isAllowedAccess(getAddressHandler(),
				     AddressPermissions.SINK)) {
		cHandlerR = new NBO(rcoI,(SinkInterface) clientInterfaceI);
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not allowed to start sink " +
		     "connections.");
	    }


	} else if (clientInterfaceI instanceof SourceInterface) {
	    if (rcoI.isAllowedAccess(getAddressHandler(),
				     AddressPermissions.SOURCE)) {
		cHandlerR = new RBO(rcoI,(SourceInterface) clientInterfaceI);
	    } else {
		throw new com.rbnb.api.AddressException
		    ("Client address is not allowed to start source " +
		     "connections.");
	    }

	} else {
	    throw new java.lang.IllegalArgumentException
		(clientInterfaceI + " is not a supported client.");
	}

	addChild((Rmap) cHandlerR);
	return (cHandlerR);
    }

    /**
     * Delivers a <code>RoutedMessage</code> to this <code>RBNB</code>.
     * <p>
     * The only valid message that can be sent to a <code>RBNB</code> is a
     * <code>Login</code> message, which initiates a log in sequence.
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
     * 04/10/2003  INB	Add <code>Username</code> to
     *			<code>RemoteClientHandler</code>.
     * 03/21/2003  INB	Log exceptions.
     * 02/27/2003  INB	Eliminate "_Log" from one of the messages.
     * 11/26/2001  INB	Created.
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

	if (offsetI >= messageI.getTarget().length()) {
	    // If this is a message to the <code>RBNB</code>, then deliver it.
	    if (!(messageI.getMessage() instanceof Login)) {
		java.lang.IllegalArgumentException laEx =
		    new java.lang.IllegalArgumentException
			(messageI + " is not a valid message.");
		serializableR = Language.exception(laEx);
		if (getLog() != null) {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 laEx);
		}

	    } else {
		Login login = (Login) messageI.getMessage();
		ClientInterface clientInterface =
		    (ClientInterface) login.getChildAt(0);
		if (clientInterface instanceof Sink) {
		    if (findRemoteClientHandler
			(clientInterface.getName()) != null) {
			java.lang.IllegalStateException isEx =
			    new java.lang.IllegalStateException
				(messageI +
				 " produces a naming conflict with an" +
				 " existing remote client object.");
			serializableR = Language.exception(isEx);
			if (getLog() != null) {
			    getLog().addException
				(Log.STANDARD,
				 getLogClass(),
				 getName(),
				 isEx);
			}

		    } else {
			RemoteClientHandler rClient =
			    new RemoteClientHandler();
			rClient.setServerHandler(this);
			rClient.setName(clientInterface.getName());
			rClient.setUsername(login.getUsername());
			addRemoteClientHandler(rClient);
			rClient.start();
			serializableR = Language.ping();
		    }

		} else {
		    java.lang.IllegalArgumentException laEx =
			new java.lang.IllegalArgumentException
			    (messageI +
			     " tries to create an illegal remote client.");
		    serializableR = Language.exception(laEx);
		    if (getLog() != null) {
			getLog().addException
			    (Log.STANDARD,
			     getLogClass(),
			     getName(),
			     laEx);
		    }
		}
	    }

	} else {
	    // Attempt to deliver the message to something that may be local.
	    // Determine the name of the next level.
	    int nSlash = messageI.getTarget().indexOf(PATHDELIMITER,offsetI);
	    String nextName;

	    if (nSlash == -1) {
		nSlash = messageI.getTarget().length();

	    } else if (nSlash == offsetI) {
		// Deliver it via the routing map.
		serializableR = getRoutingMapHandler().deliver(messageI,
							       nSlash + 1);

	    }

	    if (nSlash != offsetI) {
		// Find the target.
		nextName = messageI.getTarget().substring(offsetI,nSlash);
		Rmap rTarget = findChild(new Rmap(nextName));

		if (rTarget == null) {
		    com.rbnb.api.AddressException addrEx =
			new com.rbnb.api.AddressException
			    (getFullName() +
			     " does not have a child named " +
			     nextName +
			     ".\nCannot find " +
			     messageI.getTarget() + ".");
		    serializableR = Language.exception(addrEx);
		    if (getLog() != null) {
			getLog().addException
			    (Log.STANDARD,
			     getLogClass(),
			     getName(),
			     addrEx);
		    }

		} else {
		    if (rTarget instanceof RoutedTarget) {
			serializableR =
			    ((RoutedTarget) rTarget).deliver(messageI,
							     nSlash + 1);

		    } else {
			RemoteClientHandler rClient = findRemoteClientHandler
			    (nextName);
			if (rClient != null) {
			    rClient.send(messageI);
			    serializableR = Language.ping();
			} else {
			    com.rbnb.api.AddressException addrEx =
				new com.rbnb.api.AddressException
				    (getFullName() +
				     " failed to find handler for " +
				     nextName + ".");
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
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    public final Path findPath()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	LocalPath pathR = new LocalPath();
	pathR.add(this);
	pathR.setCost(0);

	return (pathR);
    }

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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 03/26/2003  INB	Sort <code>pathsToCheck</code> by cost.
     * 12/21/2001  INB	Created.
     *
     */
    public final void findPaths(SortedStrings toFindIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    getPathDoor().lock("RBNB.findPaths");
	    long lPathFindCounter = getPathFindCounter() + 1;

	    LocalPath lPath = new LocalPath();
	    lPath.add(this);

	    com.rbnb.utility.SortedVector
		pathsFound = getValidPaths(toFindIO),
		pathsToCheck = new
		    com.rbnb.utility.SortedVector(Path.BY_COST);
	    findPaths(lPathFindCounter,
		      lPath,
		      toFindIO,
		      pathsToCheck,
		      pathsFound);

	    while (((toFindIO == null) || (toFindIO.size() > 0)) &&
		   (pathsToCheck.size() > 0)) {
		LocalPath pToCheck = (LocalPath) pathsToCheck.firstElement();
		pathsToCheck.removeElementAt(0);

		RemoteServer rServer = (RemoteServer)
		    getRoutingMapHandler().findDescendant
		    ((String) pToCheck.getOrdered().lastElement(),
		     false);

		if (rServer != null) {
		    rServer.findPaths(lPathFindCounter,
				      pToCheck,
				      toFindIO,
				      pathsToCheck,
				      pathsFound);
		}
	    }

	    setPaths(pathsFound);

	} finally {
	    getPathDoor().unlock();
	}
    }

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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 03/25/2003  INB	Protect the path list from access while we're reading
     *			it.
     * 12/21/2001  INB	Created.
     *
     */
    public final Path findPathTo(PeerServer peerServerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Path pathR = null;
	boolean locked = false;
	String peerName = peerServerI.getFullName();

	try {
	    getPathDoor().lockRead("RBNB.findPathTo");
	    locked = true;

	    if ((pathR = (Path) getPaths().find(peerName)) == null) {
		// If no <code>Path</code> can be found in the existing list,
		// then try to build a new list with the <code>Path</code> to
		// the input <code>PeerServer</code>.
		SortedStrings toFind = new SortedStrings();
		toFind.add(peerName);
		getPathDoor().unlockRead();
		locked = false;
		findPaths(toFind);
		getPathDoor().lockRead("RBNB.findPathTo_2");
		locked = true;
		pathR = (Path) getPaths().find(peerName);
	    }

	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.InternalError();

	} finally {
	    if (locked) {
		getPathDoor().unlockRead();
		locked = false;
	    }
	}

	return (pathR);
    }

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
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 02/15/2002  INB	Created.
     *
     */
    public final RemoteClientHandler findRemoteClientHandler(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RemoteClientHandler rchR = null;

	boolean locked = false;
	try {
	    rClientsDoor.lockRead("RBNB.findRemoteClientHandler");
	    locked = true;
	    java.util.Vector check = null;

	    if ((rClients != null) &&
		(rClients.size() > 0) &&
		((check = rClients.findName(nameI)) != null) &&
		(check.size() > 0)) {
		rchR = (RemoteClientHandler) check.firstElement();
	    }
	} finally {
	    if (locked) {
		rClientsDoor.unlockRead();
	    }
	}

	return (rchR);
    }

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
    public final ActionThreadQueue getActivityQueue() {
	return (activityQueue);
    }

    /**
     * Get the archive home directory.
     * <p>
     *
     * @author John Wilson
     *
     * @return A String indicating the archive home directory.
     * @see #setArchiveHomeDirectory(String)
     * @since V2.6
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2006  JPW	Created.
     *
     */
    public final String getArchiveHomeDirectory() {
	return (archiveHomeDirectory);
    }

    /**
     * Gets the auto load archives flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return automatically load archives?
     * @see #setAutoLoadArchives(boolean)
     * @since V2.1
     * @version 04/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2003  INB	Created.
     *
     */
    public final boolean getAutoLoadArchives() {
	return (autoLoadArchives);
    }

    /**
     * Gets the client side <code>Server</code> for this <code>RBNB</code>.
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
    public final Server getClientSide() {
	return (clientSide);
    }

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
    public final String getCompatibilityMode() {
	return (compatibilityMode);
    }

    /**
     * Gets the debug level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the debug level.
     * @see com.rbnb.api.Log
     * @see #setDebugLevel(byte)
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  INB	Created.
     *
     */
    final byte getDebugLevel() {
	return (debugLevel);
    }

    /**
     * Gets the debug mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the debug mask.
     * @see com.rbnb.api.Log
     * @see #setDebugMask(long)
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  INB	Created.
     *
     */
    final long getDebugMask() {
	return (debugMask);
    }

    /**
     * Gets the <code>License</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>License</code>
     * @see #setLicense(com.rbnb.api.License)
     * @since V2.0
     * @version 10/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    /*
    final License getLicense() {
	return (license);
    }
    */

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
     * 11/20/2002  INB	Created.
     *
     */
    public final ServerHandler getLocalServerHandler() {
	return (this);
    }

    /**
     * Gets the log class mask for this <code>RBNB</code>.
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
	return (super.getLogClass() | Log.CLASS_RBNB);
    }

    /**
     * Gets the <code>ServerHandler</code> log.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code> log.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    public final Log getLog() {
	return (log);
    }

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
    public final long getLogStatusPeriod() {
	return (logStatusPeriod);
    }

    /**
     * Gets the maximum number of activity threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum number of activity threads.
     * @see #setMaxActivityThreads(int)
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
    final int getMaxActivityThreads() {
	return (maxActivityThreads);
    }

    /**
     * Gets the maximum number of open filesets.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum number of open filesets.
     * @see #setMaxOpenFileSets(int)
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
    final int getMaxOpenFileSets() {
	return (maxOpenFileSets);
    }

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
    public final long getMetricsPeriod() {
	return (metricsPeriod);
    }

    /**
     * Gets the metrics <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the metrics <code>Source</code>.
     * @see #setMetricsSource(com.rbnb.api.Source)
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
    private final Source getMetricsSource() {
	return (metricsSource);
    }

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
    public final LimitedResource getOpenFileSets() {
	return (openFileSets);
    }

    /**
     * Gets the list of <code>Paths</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of <code>Paths</code>.
     * @see #setPaths(com.rbnb.utility.SortedVector)
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
    private final com.rbnb.utility.SortedVector getPaths() {
	return (paths);
    }

    /**
     * Returns the <code>Door</code> used to ensure that we only perform a
     * single <code>Path</code> finding operation at a time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @see #setPathDoor(com.rbnb.api.Door)
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
    private final Door getPathDoor() {
	return (pathDoor);
    }

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
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  INB	Created.
     *
     */
    public final RAMServerCommunications getRAMPort() {
	if (ramPort == null) {
	    setRAMPort(new RAMServerCommunications(this));
	    getRAMPort().start();
	}
	return (ramPort);
    }

    /**
     * Gets the registration list for this <code>RBNB</code> matching
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
     * @version 06/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((requestI.compareNames(".") != 0) &&
	    (requestI.compareNames("..") != 0) &&
	    (compareNames(requestI) != 0)) {
	    throw new java.lang.IllegalArgumentException
		(requestI + " does not have a matching name.");
	}

	Rmap rmapR = null;
	if (requestI.compareNames("..") == 0) {
	    Rmap request = (Rmap) requestI.clone();
	    request.setName(getParent().getName());
	    rmapR = ((RegisteredInterface) getParent()).getRegistered(request);
	    if (rmapR instanceof EndOfStream) {
		if (rmapR.getNchildren() == 0) {
		    return (null);
		}
		rmapR = rmapR.getChildAt(0);
		rmapR.getParent().removeChildAt(0);

	    }
	    rmapR.setName("..");

	} else {
	    Rmap child,
		nChild,
		request,
		request2,
		answer;
	    java.util.Vector rList;

	    rmapR = newInstance();
	    if (requestI.compareNames(".") == 0) {
		rmapR.setName(".");
	    }
	
	    for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
		request = requestI.getChildAt(idx);
		rList = new java.util.Vector();

		if ((request.compareNames("...") != 0) &&
		    (request.compareNames(">...") != 0)) {
		    rList.addElement(request);

		} else {
		    for (int idx1 = 0; idx1 < getNchildren(); ++idx1) {
			child = getChildAt(idx1);
			request2 = child.newInstance();
			request2.addChild(new Rmap(">...",MarkerBlock,null));
			rList.addElement(request2);
		    }
		}

		for (int idx1 = 0; idx1 < rList.size(); ++idx1) {
		    request2 = (Rmap) rList.elementAt(idx1);
		    if (request2.compareNames("..") == 0) {
			request2 = (Rmap) request2.clone();
			request2.setName(getParent().getName());
			answer =
			    ((RegisteredInterface) getParent()).getRegistered
			    (request2);
			if (answer instanceof EndOfStream) {
			    if (answer.getNchildren() == 0) {
				continue;
			    }
			    answer = answer.getChildAt(0);
			    answer.getParent().removeChildAt(0);
			}
			answer.setName("..");
			rmapR.addChild(answer);

		    } else if (request2.compareNames("*") == 0) {
			request2 = (Rmap) request2.clone();
			for (int idx2 = 0; idx2 < getNchildren(); ++idx2) {
			    child = getChildAt(idx2);
			    if (child instanceof RegisteredInterface) {
				request2.setName(child.getName());
				if ((child instanceof RegisteredInterface) &&
				    ((nChild =
				      ((RegisteredInterface)
				       child).getRegistered
				      (request2)) != null)) {
				    rmapR.addChild(nChild);
				}
			    }
			}

		    } else {
			child = findChild(request2);
			if (child != null) {
			    if ((child instanceof RegisteredInterface) &&
				((nChild =
				  ((RegisteredInterface) child).getRegistered
				  (request2)) != null)) {
				rmapR.addChild(nChild);
			    }
			}
		    }
		}
	    }
	}

	return (rmapR);
    }

    /**
     * Gets the <code>RoutingMapHandler</code> for this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RoutingMapHandler</code>.
     * @since V2.0
     * @version 12/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/28/2001  INB	Created.
     *
     */
    public final RoutingMapHandler getRoutingMapHandler() {
	return (super.getRoutingMapHandler());
    }

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
    public final Object getServerPort() {
	return (serverPort);
    }

    /**
     * Gets the terminating client's name.
     * <p>
     * The terminating client is the <code>Client</code> object that asked this
     * <code>RBNB</code> to terminate. If it exists, it cannot be
     * terminated while the <code>RBNB</code> is shutting down.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name of the terminating client.
     * @see #setTerminatingClient(String)
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
    final String getTerminatingClient() {
	return (terminatingClient);
    }

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
    public final Thread getThread() {
	return (thread);
    }

    /**
     * Gets the task timer.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the task timer.
     * @see #setTimer(com.rbnb.api.Timer)
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
    public final Timer getTimer() {
	return (timer);
    }

    /**
     * Gets the list of "valid" <code>Paths</code>.
     * <p>
     * Any <code>Path</code> in the current list of <code>Paths</code> that
     * does not lead to one of the servers named in the input list is
     * considered to be still valid.
     * <p>
     *
     * @author Ian Brown
     *
     * @param toFindI the list of server names to be found. <code>Paths</code>
     *		      to entries in this list are not considered to be valid.
     * @return the list of valid <code>Paths</code>.
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
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Rewrote to use binary search of path list.
     * 12/21/2001  INB	Created.
     *
     */
    private final com.rbnb.utility.SortedVector getValidPaths
	(SortedStrings toFindI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	com.rbnb.utility.SortedVector validR = null;

	if (toFindI == null) {
	    validR = new com.rbnb.utility.SortedVector();

	} else {
	    validR = (com.rbnb.utility.SortedVector) getPaths().clone();
	    String invalid;
	    Path path,
		lPath;
	    int low = 0,
		lo,
		hi,
		idx1,
		direction;
	    for (int idx = 0; idx < toFindI.size(); ++idx) {
		invalid = toFindI.elementAt(idx);
		path = null;
		for (lo = low,
			 hi = validR.size() - 1,
			 idx1 = (lo + hi)/2;
		     lo <= hi;
		     idx1 = (lo + hi)/2) {
		    lPath = (Path) validR.elementAt(idx1);
		    try {
			direction = lPath.compareTo(null,invalid);
		    } catch (com.rbnb.utility.SortException e) {
			continue;
		    }
		    if (direction == 0) {
			path = lPath;
			low = idx1;
			break;
		    } else if (direction < 0) {
			lo = idx1 + 1;
		    } else if (direction > 0) {
			hi = idx1 - 1;
		    }
		}
			
		if (path != null) {
		    validR.remove(path);
		}
	    }
	}

	return (validR);
    }

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
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/07/2004  INB	Renamed the <code>Controller</code> used to create
     *			the reverse route from "_RH..." to "_RR...".
     * 05/23/2003  INB	Added dot to router name.
     * 03/26/2003  INB	Add exceptions at log level 0. Initiate multiple
     *			connections.
     * 02/28/2003  INB	Use our name as our log channel.
     * 02/26/2003  INB	The reversed <code>RouterHandler</code> needs to be
     *			tied to the other side.
     * 02/04/2002  INB	Created.
     *
     */
    public final Serializable initiateReverseRoute(ReverseRoute reverseI) {
	Serializable answerR = Language.ping();
	int idx = 0;

	try {
	    PeerServer pServer = (PeerServer) reverseI.getObject();
	    Server server = (Server) pServer.newInstance();
	    RoutingMapHandler rmh = null;
	    PeerServer lpServer = null;
	    String rhName;
	    Controller controller;
	    RouterHandler rHandler;

	    for (idx = 0; idx < MAXIMUM_SIMULTANEOUS_ROUTERS; ++idx) {
		rhName = "_RR." + getFullName() + "." + idx;
		controller = server.createController
		    (rhName.replace('/','_'));
		controller.start();
		reverseI.setFromPrimary(false);
		rHandler =
		    ((ControllerHandle) controller).reverseRoute(reverseI,
								 this);
		addChild((Rmap) rHandler);
		((RBNBRouter) rHandler).serverName = pServer.getFullName();

		if (rmh == null) {
		    rmh = getRoutingMapHandler();
		    lpServer = (PeerServer)
			((Rmap) rmh).findDescendant(pServer.getFullName(),
						    false);
		}

		lpServer.addRelated(rHandler);
		rHandler.start((ClientHandler) null);
	    }
	    answerR = Language.ping();

	} catch (java.lang.Exception e) {
	    try {
		getLog().addException(Log.STANDARD,
				      getLogClass(),
				      getName(),
				      e);
	    } catch (java.lang.Exception e1) {
	    }
	    if (idx == 0) {
		answerR = new ExceptionMessage(e);
	    } else {
		answerR = Language.ping();
	    }
	}

	return (answerR);
    }

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
     * @version 02/17/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2004  INB	Moved <code>receive</code> into loop.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 03/19/2003  INB	Ensure that the server is started.
     * 02/26/2003  INB	Ensure that the <code>Router</code> grabbed here ends
     *			up beloging to the correct parent.
     * 12/18/2001  INB	Created.
     *
     */
    public final Server initiateRouteTo(Server peerHierarchyI,
					Server localI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server localR = null;

	try {
	    getRoutingMapHandler().getPeerDoor().lock("RBNB.initiateRouteTo");

	    PeerServer pServer = null;

	    if (localI == null) {
		if (peerHierarchyI.getName() == null) {
		    pServer = new PeerServer(null,peerHierarchyI.getAddress());
		    pServer.setLocalServerHandler(this);

		} else {
		    pServer = getRoutingMapHandler().createPeer
			(peerHierarchyI,
			 null);
		}

	    } else if (localI instanceof PeerServer) {
		pServer = (PeerServer) localI;
		 
	    } else {
		pServer = new PeerServer(localI.getName(),localI.getAddress());
		pServer.setLocalServerHandler(this);

		pServer.setChildren(localI.getChildren());
		for (int idx = 0; idx < pServer.getNchildren(); ++idx) {
		    Rmap child = pServer.getChildAt(idx);
		    child.setParent(null);
		    child.setParent(pServer);
		}

		Rmap tParent = localI.getParent();
		tParent.removeChild(localI);
		tParent.addChild(pServer);
	    }

	    if (!pServer.getConnected()) {
		pServer.start();
	    }
	    if (pServer.getActiveShortcuts() == 0) {
		if ((pServer.getPassiveShortcuts() > 0) ||
		    (pServer instanceof ChildServer)) {
		    pServer.switchToActive();
		} else {
		    Router router = pServer.grabRouter();

		    java.util.Vector additional = new java.util.Vector();
		    additional.addElement("peer");
		    Server top = (Server) this.newInstance(),
			current = this,
			build;

		    while ((current.getParent() != null) &&
			   (current.getParent() instanceof Server)) {
			current = (Server) current.getParent();
			build = (Server) current.newInstance();
			build.addChild(top);
			top = build;
		    }
		    additional.addElement(top);
		    router.send(new Ask(Ask.ROUTEFROM,additional));
		    Serializable serializable;
		    do {
			serializable = router.receive(ACO.rmapClass,
						      false,
						      Client.FOREVER);
			if (serializable instanceof ExceptionMessage) {
			    Language.throwException
				((ExceptionMessage) serializable);

			} else if (serializable instanceof ReverseRoute) {
			    router.send
				(initiateReverseRoute
				 ((ReverseRoute) serializable));
			    serializable = null;
			}
		    } while (serializable == null);

		    Rmap hierarchy = (Rmap) serializable;
		    localR = pServer;
		    pServer.releaseRouter(router);

		    if ((peerHierarchyI.getName() == null) &&
			(localI == null)) {
			localR = getRoutingMapHandler().createPeer
			    (hierarchy,
			     pServer);
		    }
		    ((PeerServer) localR).setConnected(true);
		}
	    }

	} finally {
	    getRoutingMapHandler().getPeerDoor().unlock();
	}

	return (localR);
    }

    /**
     * Interrupts this <code>RBNB</code>.
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
     * Is this <code>ServerHandler</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is it running?
     * @see #run()
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
    public final boolean isRunning() {
	return ((getThread() != null) && getThread().isAlive());
    }

    /**
     * Loads any archives in the current directory.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2006  JPW	Use archiveHomeDirectory
     * 04/03/2003  INB	Created.
     *
     */
    private final void loadArchives() {
	try {
	    // JPW 06/22/2006: Use archiveHomeDirectory
	    // java.io.File homeDir = new java.io.File(".");
	    java.io.File homeDir = new java.io.File(getArchiveHomeDirectory());
	    Directory asDirectory = new Directory(homeDir);
	    java.io.File[] files = asDirectory.listFiles();

	    for (int idx = 0; idx < files.length; ++idx) {
		if (files[idx].isDirectory()) {
		    getActivityQueue().addEvent
			(new LoadSource(this.getClientSide(),
					files[idx].getName()));
		}
	    }
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Locks out further attempts to route to this <code>RBNB</code>
     * until the current one has completed.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #unlockRouting()
     * @since V2.1
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 04/24/2003  IAB	Created.
     *
     */
    public final void lockRouting()
	throws java.lang.InterruptedException
    {
	routingDoor.lock("RBNB.lockRouting");
    }

    /**
     * Lost routing to this <code>ParentServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/19/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    final void lostRouting() {
	return;
    }

    /**
     * Parses command line arguments.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.  Supported values are:
     *		    <p><ul>
     *		    <li><it>-A authorization file</it> - sets up address
     *			authorization from a file,</li>
     *		    <li><it>-C compatibility version</it> - tells the server
     *			to behave like the specified version,</li>
     *		    <li><it>-c maximum number of connections</it> - sets the
     *			maximum number of connections (incoming and outgoing)
     *			supported (NOT IMPLEMENTED YET),</li>
     *		    <li><it>-D mask,level</it> - sets the debug
     *			level,</li>
     *		    <li><it>-F</it> - automatically loads archives.</li>
     *		    <li><it>-H</it> - archive home directory.</li>
     *		    <li><it>-L</it> - authorizes only the local machine.</li>
     *		    <li><it>-l period,cache,archive,access</it> - sets up the
     *			log,</li>
     *		    <li><it>-M max activity threads</it> - sets the maximum
     *			number of activity threads available,</li>
     *		    <li><it>-m period,cache,archive,access</it> - sets up
     *			metrics,</li>
     *		    <li><it>-S maximum open filesets,</li>
     *		    </ul>
     * @exception com.rbnb.api.SerializeException
     *		  thrown if any other error occurs.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if there is a problem with an argument.
     * @since V2.0
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2006  JPW	Added -H flag (archive home directory).
     * 04/28/2004  INB	Use <code>AddressPermissions</code> objects.
     * 11/19/2003  INB	Added -C flag.
     * 11/17/2003  INB	Added -c flag.
     * 04/03/2003  INB	Renamed -F to -S, added new -F flag.
     * 03/20/2003  INB	Use <code>StringTokenizer</code> to parse debug
     *			settings.
     * 03/13/2003  INB	Added the -F and -M switches.
     * 01/30/2002  INB	Created.
     *
     */
    private final void parseArguments(String[] argsI)
	throws com.rbnb.api.SerializeException,
	       java.lang.IllegalArgumentException
    {
	com.rbnb.utility.ArgHandler argHandler = null;
	boolean haveAddrAuth = false;

	try {
	    argHandler = new com.rbnb.utility.ArgHandler(argsI);
	} catch (java.lang.Exception e) {
	    throw new java.lang.IllegalArgumentException(e.getMessage());
	}
	String value;

	if (((value = argHandler.getOption('A')) != null) &&
	    !value.equalsIgnoreCase("<none>")) {
	    if (haveAddrAuth) {
		throw new java.lang.IllegalArgumentException
		    ("Inconsistent security switches.");
	    }

	    try {
		getAddressHandler().setAuthorization
		    (new AddressAuthorization(value));
		haveAddrAuth = true;
	    } catch (java.lang.Exception e) {
		java.io.ByteArrayOutputStream baos =
		    new java.io.ByteArrayOutputStream();
		java.io.PrintWriter pw = new java.io.PrintWriter(baos);
		e.printStackTrace(pw);
		pw.flush();
		String message = new String(baos.toByteArray());
		pw.close();
		throw new com.rbnb.api.SerializeException
		    ("Failed to read authorization file: " + value + ".\n" +
		     message);
	    }
	}

	if ((value = argHandler.getOption('C')) != null) {
	    setCompatibilityMode(value);
	}

	/*
	if ((value = argHandler.getOption('c')) != null) {
	    setMaximumConnections(Integer.parseInt(value));
	}
	*/

	if ((value = argHandler.getOption('D')) != null) {
	    java.util.StringTokenizer st = new java.util.StringTokenizer
		(value,",+",true);
	    String subvalue,
		mask = null,
		level = null;

	    if (st.hasMoreTokens()) {
		subvalue = st.nextToken();
		if (!subvalue.equals(",") && !subvalue.equals("+")) {
		    mask = subvalue;
		    if (st.hasMoreTokens()) {
			subvalue = st.nextToken();
		    }
		}
		if (st.hasMoreTokens()) {
		    level = st.nextToken();
		}
	    }
	    if (mask != null) {
		if ((mask.indexOf("0x") == 0) ||
		    (mask.indexOf("0X") == 0)) {
		    setDebugMask(Long.parseLong(mask.substring(2),16));
		} else {
		    setDebugMask(Long.parseLong(mask));
		}
	    }

	    if (level != null) {
		setDebugLevel(Byte.parseByte(level));
	    }
	}

	setAutoLoadArchives(argHandler.checkFlag('F'));
	
	// JPW 06/22/2006: Add "-H" archive home directory flag
	if ((value = argHandler.getOption('H')) != null) {
	    // The given value must be a directory that we have
	    // permission to read/write to
	    File tempDir = new File(value);
	    if (tempDir.exists() && tempDir.isDirectory()) {
		// Make sure we have permission to write a file in this dir
		// JPW 11/16/2006: Do this in a Java 1.1 compliant manner
		//                 (for compiling under J#).  This means
		//                 I can't use File.createTempFile().
		Random random = new Random(System.currentTimeMillis());
		try {
		    /*
		     * To be Java 1.1 compliant (for J# compile) don't use
		     * createTempFile().
		    File tempFile =
			File.createTempFile(
			    "rbnb",
			    ".tmp",
			    tempDir);
		    */
		    File tempFile =
			new File(
			    tempDir,
			    new String("rbnb" + random.nextInt() + ".tmp"));
		    FileWriter fw = new FileWriter(tempFile);
		    PrintWriter pw = new PrintWriter(fw);
		    pw.println("Test");
		    pw.close();
		    // Delete the temporary file
		    tempFile.delete();
		    // Success!  We can use the given directory for archives
		    setArchiveHomeDirectory(value);
		} catch (Exception e) {
		    System.err.println(
			"\nWARNING: Unable to use the specified archive " +
			"home directory, \"" +
			value +
			"\". (You may not have permission to use " +
			"this directory.)\n" +
			"Using the default archive home directory \"" +
			getArchiveHomeDirectory() +
			"\"\n");
		}
	    } else {
		System.err.println(
		    "\nWARNING: The specified archive home directory, \"" +
		    value +
		    "\", either does not exist or is not a directory.\n" +
		    "Using the default archive home directory \"" +
		    getArchiveHomeDirectory() +
		    "\"\n");
	    }
	}
	
	if ((value = argHandler.getOption('l')) != null) {
	    PeriodicSource logSettings = new PeriodicSource
		("log",
		 value,
		 TimerPeriod.LOG_STATUS,
		 1000,
		 0,
		 SourceInterface.ACCESS_NONE);
	    try {
		setLogStatusPeriod(logSettings.getPeriod());
		getLog().setCframes(logSettings.getCframes());
		getLog().setAframes(logSettings.getAframes());
		getLog().setAmode(logSettings.getAmode());
	    } catch (java.io.IOException e) {
		throw new java.lang.IllegalArgumentException
		    ("Unable to parse log argument: " + value +
		     ".\n" +
		     e.getClass() + " " + e.getMessage());
	    } catch (java.lang.InterruptedException e) {
		throw new java.lang.IllegalArgumentException
		    ("Unable to parse log argument: " + value +
		     ".\n" +
		     e.getClass() + " " + e.getMessage());
	    }
	}

	if ((value = argHandler.getOption('m')) != null) {
	    PeriodicSource metricsSettings = new PeriodicSource
		("metrics",
		 value,
		 TimerPeriod.METRICS,
		 60*60*1000/TimerPeriod.METRICS,
		 0,
		 SourceInterface.ACCESS_NONE);
	    try {
		setMetricsPeriod(metricsSettings.getPeriod());
		getMetricsSource().setCframes(metricsSettings.getCframes());
		getMetricsSource().setAframes(metricsSettings.getAframes());
		getMetricsSource().setAmode(metricsSettings.getAmode());
	    } catch (java.io.IOException e) {
		throw new java.lang.IllegalArgumentException
		    ("Unable to parse metrics argument: " + value +
		     ".\n" +
		     e.getClass() + " " + e.getMessage());
	    } catch (java.lang.InterruptedException e) {
		throw new java.lang.IllegalArgumentException
		    ("Unable to parse metrics argument: " + value +
		     ".\n" +
		     e.getClass() + " " + e.getMessage());
	    }
	}

	if ((value = argHandler.getOption('M')) != null) {
	    setMaxActivityThreads(Integer.parseInt(value));
	    if (getMaxActivityThreads() <= 0) {
		throw new java.lang.IllegalArgumentException
		    ("Need at least one activity thread.");
	    }
	}

	if (argHandler.checkFlag('L')) {
	    if (haveAddrAuth) {
		throw new java.lang.IllegalArgumentException
		    ("Inconsistent security switches.");
	    }

	    try {
		AddressAuthorization addrAuth = new AddressAuthorization();
		addrAuth.removeAllow(new AddressPermissions("*"));
		addrAuth.addAllow(new AddressPermissions("localhost"));
		getAddressHandler().setAuthorization(addrAuth);
		haveAddrAuth = true;
	    } catch (java.lang.Exception e) {
		java.io.ByteArrayOutputStream baos =
		    new java.io.ByteArrayOutputStream();
		java.io.PrintWriter pw = new java.io.PrintWriter(baos);
		e.printStackTrace(pw);
		pw.flush();
		String message = new String(baos.toByteArray());
		pw.close();
		throw new com.rbnb.api.SerializeException
		    ("Failed to lock down authorization for server.\n" +
		     message);
	    }

	    haveAddrAuth = true;
	}
	
	if ((value = argHandler.getOption('P')) != null) {
	    String user, pwd;
	    int commaI = value.indexOf(',');
	    if (commaI != -1) {
		user = value.substring(0, commaI);
		pwd = value.substring(commaI+1);
	    } else {
		user = value;
		try {
		    pwd = promptUser("PASSWORD: ");
		} catch (java.io.IOException ioe) {
		    pwd = null;
		}
	    } 
	    try {
		username = new Username(user, pwd);
	    } catch (Exception e) { throw new IllegalArgumentException(
	    		"Cannot set user id or password properties.");
	    } 
	} else if (argHandler.checkFlag('P')) { // P set with no accompaniment
	    try {
		username = new Username(
			promptUser("User [rbnb]: "), promptUser("PASSWORD: "));
		if (username.getUsername() == null 
			|| username.getUsername().length() == 0) {
		    username.setUsername("rbnb");
		}
	    } catch (Exception e) { throw new IllegalArgumentException(
	    		"Cannot set user id or password properties.");
	    } 
	}

	if ((value = argHandler.getOption('S')) != null) {
	    setMaxOpenFileSets(Integer.parseInt(value));
	    if (getMaxOpenFileSets() <= 0) {
		throw new java.lang.IllegalArgumentException
		    ("Need at least one open filesets allowed.");
	    }
	}
    }
    
    /**
      * Prompt the user for input.
      *
      * @author WHF
      * @version 2005/01/19
      * @since V2.5
      */
    private String promptUser(String prompt) throws java.io.IOException
    {
	System.out.print(prompt);
	return (new java.io.BufferedReader(new java.io.InputStreamReader(
		System.in))).readLine();
    }


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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 12/04/2001  INB	Created.
     *
     */
    public final void removeRemoteClientHandler(RemoteClientHandler rchI)
	throws java.lang.InterruptedException
    {
	try {
	    rClientsDoor.lock("RBNB.removeRemoteClientHandler");

	    if (rClients != null) {
		rClients.remove(rchI);
	    }
	} finally {
	    rClientsDoor.unlock();
	}
    }

    /**
     * Runs this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start(com.rbnb.api.ClientHandler)
     * @see #start(com.rbnb.api.ClientHandler,String[])
     * @see #stop(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 2005/01/20
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2005/01/20  WHF  For authentication purposes, pass the username object
     *			to the Address before accepting
     * 10/05/2004  JPW	Add preprocessor directives which can be used by "sed"
     *			to create a version of the code appropriate for
     *			compilation under J# (which supports Java 1.1.4).  In
     *			the Java 1.1.4 version, don't call addShutdownHook()
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 02/12/2004  INB	Give the <code>Log</code> a little time on shutdown.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/17/2003  INB	Don't let <code>IOExceptions</code> cause the whole
     *			server to try to shut down.
     * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
     * 			Added identification to the <code>Door</code>.
     * 09/08/2003  INB	Clear any interrupt before going through shutdown.
     * 04/17/2003  INB	When stopping an <code>SourceHandler</code> at
     *			shutdown, clear the keep cache flag.
     * 04/02/2003  INB	The log may not need to be running.
     * 03/31/2003  INB	Moved stop of <code>ChildServer</code> above stop of
     *			<code>RoutingMap</code>.
     * 03/19/2003  INB	Ensure that the reverse route contains the entire
     *			hierarchy above us.
     * 03/13/2003  INB	Create and stop the activity queue. Create the open
     *			filesets limited resource.
     * 02/28/2003  INB	Use our name as our log channel.
     * 02/27/2003  INB	Removed "_Log" from one of the messages.
     * 05/11/2001  INB	Created.
     *
     */
    public final void run() {
	setConnected(true);
	TimerTask metricsTT = null;
	try {
	    // Prepare to gracefully shutdown.
	    try {
		// JPW 10/05/2004: Add preprocessor directives which can be
		//                 used by "sed" to create a version of the
		//                 code appropriate for compilation under J#
		//                 (which supports Java 1.1.4).	 In the
		//                 Java 1.1.4 version, don't call
		//                 addShutdownHook()
		//#open_java2_comment#
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
		//#close_java2_comment#
	    } catch (java.lang.NoSuchMethodError e) {
	    }

	    // Initiate the task timer.
	    setTimer(new Timer(true));

	    // Create the activity thread queue.
	    setActivityQueue(new ActionThreadQueue(getMaxActivityThreads()));

	    // Create the open filesets limited resource.
	    setOpenFileSets(new LimitedResource(getMaxOpenFileSets()));

	    // Attach the <code>RBNB</code> log RBO.
	    try {
		addChild(getLog());
		if ((getLog().getCframes() > 0) ||
		    (getLog().getAframes() > 0)) {
		    getLog().start((ClientHandler) null);
		} else {
		    removeChild(getLog());
		    getLog().setRunnable(false);
		}
	    } catch (SerializeException e) {
		removeChild(getLog());
		getLog().setRunnable(false);
	    }

	    // Wait until the log is running.
	    while (getLog().getRunnable() && !getLog().isRunning()) {
		Thread.currentThread().sleep(100);
	    }

	    // Check for the IBM 1.3.0 JVM - it has difficulties running the
	    // server reliably.
	    if ((System.getProperty("os.name").indexOf("Windows") != -1) &&
		(System.getProperty("java.fullversion") != null) &&
		(System.getProperty("java.fullversion").indexOf
		("J2RE 1.3.0 IBM") == 0)) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "WARNING: This JVM (" +
		     System.getProperty("java.fullversion") +
		     ")\ndoes not reliably run the RBNB V2 server.");
	    }

	    // Load the build information file.
	    BuildFile.loadBuildFile(this);

	    /*
	    // Load the license.
	    setLicense(new License());
	    */

	    /*
	    // Set the license string.
	    setLicenseString(getLicense().version() +
			     "." +
			     getLicense().serialNumber());
	    */

	    // Connect to the <code>RBNB</code>'s address.
	    setServerPort(getAddressHandler().newServerSide(this));
	    synchronized (this) {
		notifyAll();
	    }

	    // Create the ultimate parent <code>RoutingMapHandler</code>.
	    RoutingMapHandler rMap = new RBNBRoutingMap();
	    rMap.start();
	    Rmap hierarchy = this;

	    if (getParent() == null) {
		rMap.addChild(this);
	    } else {
		ParentServer pServer = (ParentServer) getParent();
		pServer.setConnected(true);
		rMap.addChild(pServer);
		pServer.setLocalServerHandler(this);
		Router router = pServer.grabRouter();

		java.util.Vector additional = new java.util.Vector();
		additional.addElement("child");
		additional.addElement(this.newInstance());
		router.send(new Ask(Ask.ROUTEFROM,additional));
		Serializable serializable = router.receive(ACO.rmapClass,
							   false,
							   Client.FOREVER);
		if (serializable instanceof ExceptionMessage) {
		    Language.throwException((ExceptionMessage) serializable);
		}

		hierarchy = (Rmap) serializable;
		if (hierarchy != null) {
		    Rmap above = hierarchy.getChildAt(0),
			locator = null;
		    hierarchy = null;
		    above.getParent().removeChildAt(0);
		    Rmap bottom,
			temp;
		    for (bottom = above;
			 (bottom.getNchildren() == 1);
			 bottom = bottom.getChildAt(0)) {
			temp = new RemoteServer
			    (bottom.getName(),
			     ((Server) bottom).getAddress());
			if (locator != null) {
			    locator.addChild(temp);
			    locator = temp;
			} else {
			    hierarchy =
				locator = temp;
			}
		    }
		    getParent().setName(bottom.getName());
		    if (hierarchy != null) {
			((Rmap) rMap).removeChild(getParent());
			locator.addChild(getParent());
			rMap.addChild(hierarchy);
		    }
		}

		if (getParent() instanceof PeerServer) {
		    ((PeerServer) getParent()).setConnected(true);
		    Rmap lServer = getParent().newInstance(),
			entry,
			nEntry,
			top = lServer;
		    for (entry = getParent().getParent();
			 entry != null;
			 entry = entry.getParent()) {
			nEntry = entry.newInstance();
			nEntry.addChild(top);
			top = nEntry;
		    }
		    lServer.addChild(newInstance());
		    ReverseRoute reverse = new ReverseRoute(lServer);
		    initiateReverseRoute(reverse);
		}
		pServer.releaseRouter(router);
	    }

	    rMap.setLocalName(getFullName());

	    getPathDoor().setIdentification(getFullName() + "_path");
	    rClientsDoor.setIdentification(getFullName() + "_rclients");
	    routingDoor.setIdentification(getFullName() + "_routing");

	    // State that we're started.
	    String message;
	    message = ("RBNB (Ring Buffered Network Bus) " +
		       getBuildVersion() +
		       " (built " + getBuildDate() + ")");
	    message += "\nCopyright 2006 Creare Inc.";

	    /*
	    if (getLicense().timeUntilExpiration() <= 0) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "This license (" +
		     getLicense().version() +
		     " #" + getLicense().serialNumber() + ") expired " +
		     getLicense().expires() + ".");
		throw new java.lang.InterruptedException();

	    } else if ((getLicense().support() != null) &&
		       (getLicense().support().before(getBuildDate()))) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),a
		     "This license (" +
		     getLicense().version() +
		     " #" + getLicense().serialNumber() +
		     ") is not supported.");
		throw new java.lang.InterruptedException();
	    }

	    message += ("\nThis license (" +
			getLicense().version() + " #" +
			getLicense().serialNumber() +
			") " +
			((getLicense().expires() == null) ?
			 "has no expiration date." :
			 "expires on " + getLicense().expires() + "."));
	    if (getLicense().support() != null) {
		message += ("\nSupport for this license ends on " +
			    getLicense().support() + ".");
	    }
	    */

	    message += "\nStarted at address " + getAddress() + ".";
	    // JPW 06/22/2006
	    message += "\nArchive home directory: \"" +
		       getArchiveHomeDirectory() + "\"";
	    getLog().setClasses(getDebugMask());
	    getLog().setLevel(getDebugLevel());
	    if ((getLog().getClasses() != Log.NONE) ||
		(getLog().getLevel() != Log.STANDARD)) {
		message +=
		    "\nDebug mask=" +
		    Long.toString(getLog().getClasses(),16) +
		    "  Debug level=" + getLog().getLevel() + ".";
	    }

	    getLog().addMessage(getLogLevel(),
				getLogClass(),
				getName(),
				message);

	    // Launch metrics.
	    if (getMetricsSource() != null) {
		getMetricsSource().start();
		Thread.currentThread().yield();
		MetricsCollector metricsCollector = new MetricsCollector();
		metricsCollector.setObject(this);
		metricsCollector.setSource(getMetricsSource());
		metricsTT = new TimerTask(metricsCollector,
					  MetricsInterface.TT_METRICS);
		getTimer().schedule(metricsTT,
				    getMetricsPeriod(),
				    getMetricsPeriod());
	    }

	    // Load archives, if desired.
	    if (getAutoLoadArchives()) {
		loadArchives();
	    }

	    // Wait until we are asked to stop.
	    while (!getTerminateRequested() && !getThread().interrupted()) {
		try {
		    // 2005/01/20  WHF  Set username field before accepting:
		    getAddressHandler().setUsername(username);
		    Object clientSideObj =
			getAddressHandler().accept(getServerPort(),
						   1000);

		    if (clientSideObj != null) {
			RCO rco = RCO.newRCO(clientSideObj,this);
			rco.start();
		    }

		} catch (com.rbnb.api.AddressException e) {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  getName(),
					  e);
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getName(),
			 "A client failed to connect to the server.");

		} catch (java.io.IOException e) {
		    getLog().addException(Log.STANDARD,
					  getLogClass(),
					  getName(),
					  e);
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getName(),
			 "A client failed to connect to the server.");
		}
	    }

	} catch (java.lang.InterruptedException e) {
	} catch (java.io.InterruptedIOException e) {
	} catch (com.rbnb.api.AddressException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {
	    }

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {
	    }

	} catch (java.io.IOException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {
	    }

	} catch (java.lang.Exception e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {
	    }

	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
	    }
	}

	// Clear any interrupt.
	getThread().interrupted();

	// Terminate metrics.
	if (metricsTT != null) {
	    try {
		metricsTT.cancel();
	    } catch (java.lang.Exception e) {
	    }
	    metricsTT = null;
	}

	// Terminate the timer.
	if (getTimer() != null) {
	    try {
		getTimer().cancel();
	    } catch (java.lang.Exception e) {
	    }
	    setTimer(null);
	}

	// Terminate all of our clients, except for the log and the client that
	// asked us to terminate.
	java.util.Vector lChildren = new java.util.Vector();
	try {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		try {
		    lChildren.addElement(getChildAt(idx));
		} catch (java.lang.Exception e) {
		}
	    }
	} catch (java.lang.Exception e) {
	}

	for (int idx = 0; idx < lChildren.size(); ++idx) {
	    if (lChildren.elementAt(idx) instanceof ClientHandler) {
		ClientHandler client = (ClientHandler)
		    lChildren.elementAt(idx);

		if ((client != getLog()) &&
		    ((getTerminatingClient() == null) ||
		     (client.compareNames(getTerminatingClient()) != 0))) {
		    // Terminate all but the <code>DTLog</code>.
		    try {
			if (client instanceof SourceHandler) {
			    ((SourceHandler) client).setCkeep(false);
			}
			client.stop(null);
		    } catch (java.lang.Exception e) {
		    }
		}
	    }
	}

	// Disconnect from the <code>RBNB</code>'s address.
	if (getRAMPort() != null) {
	    getRAMPort().interrupt();
	    setRAMPort(null);
	}
	try {
	    if (getAddressHandler() != null) {
		getAddressHandler().close(getServerPort());
		setServerPort(null);
		System.gc();
	    }
	} catch (java.lang.Exception e) {
	}


	// If we're a child of any server, then close our connection to that
	// server.
	if (getParent() instanceof Server) {
	    try {
		Rmap hierarchy = createFromName(getParent().getFullName(),
						getParent());
		hierarchy.moveToBottom().addChild(newInstance());
		Stop stop = new Stop((ServerInterface) hierarchy);
		ConnectedServer cServer = (ConnectedServer) getParent();
		Router router = cServer.grabRouter();
		if (router != null) {
		    router.send(stop);
		    router.receive(TimerPeriod.SHUTDOWN_ROUTER);
		    cServer.releaseRouter(router);
		}
	    } catch (java.lang.Exception e) {
	    }
	}

	// Terminate the <code>RoutingMapHandler</code>.
	if (getRoutingMapHandler() != null) {
	    getRoutingMapHandler().stop();
	    Thread.currentThread().yield();
	}

	// Terminate the log.
	try {
	    if ((getLog() == null) || !getLog().getRunnable()) {
		if (getLog() != null) {
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getName(),
			 "Cleaning up (terminating connections).");
		}
	    } else {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Cleaning up (terminating connections and log).");
		Thread.currentThread().sleep(2000);
		getLog().stop((ClientHandler) null);
	    }
	    Thread.currentThread().yield();
	} catch (java.lang.Exception e) {
	}

	lChildren = new java.util.Vector();
	try {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		try {
		    lChildren.addElement(getChildAt(idx));
		} catch (java.lang.Exception e) {
		}
	    }
	} catch (java.lang.Exception e) {
	}

	for (int idx = 0; idx < lChildren.size(); ++idx) {
	    if (lChildren.elementAt(idx) instanceof ClientHandler) {
		ClientHandler client =
		    (ClientHandler) lChildren.elementAt(idx);
		    
		if ((getTerminatingClient() != null) &&
		    (client.compareNames(getTerminatingClient()) == 0)) {
		    try {
			if (client instanceof SourceHandler) {
			    ((SourceHandler) client).setCkeep(false);
			}
			client.stop(null);
			Thread.currentThread().yield();
		    } catch (java.lang.Exception e) {
		    }
		    break;
		}
	    }
	}

	try {
	    String time = Time.since1970(System.currentTimeMillis()/1000.);
	    while (time.length() < 28) {
		time += " ";
	    }
	    String message =
		"<" + time  + "> <" + getName() + ">\n   Terminated.";
	    System.err.println(message);
	} catch (java.lang.Exception e) {
	}

	// Terminate the activity queue.
	getActivityQueue().stop();

	// Clear the terminate request and the thread so that the
	// requesting thread can see that we're done.
	setTerminateRequested(false);
	setThread(null);
	synchronized (this) {
	    notifyAll();
	}
	Thread.currentThread().yield();
    }
    

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
    public final void setActivityQueue(ActionThreadQueue activityQueueI) {
	activityQueue = activityQueueI;
    }

    /**
     * Sets the archive home directory.
     * <p>
     *
     * @author John Wilson
     *
     * @param archiveHomeDirectoryI The archive home directory.
     * @see #getArchiveHomeDirectory()
     * @since V2.6
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2006  JPW	Created.
     *
     */
    public final void setArchiveHomeDirectory(String archiveHomeDirectoryI) {
	archiveHomeDirectory = archiveHomeDirectoryI;
    }

    /**
     * Sets the auto load archives flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param autoLoadArchivesI automatically load archives?
     * @see #getAutoLoadArchives()
     * @since V2.1
     * @version 04/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2003  INB	Created.
     *
     */
    public final void setAutoLoadArchives(boolean autoLoadArchivesI) {
	autoLoadArchives = autoLoadArchivesI;
    }

    /**
     * Sets the client side <code>Server</code> for this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client side <code>Server</code>.
     * @see #getClientSide()
     * @since V2.0     * @version 11/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/26/2001  INB	Created.
     *
     */
    public final void setClientSide(Server clientSideI) {
	clientSide = clientSideI;
    }

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
    public final void setCompatibilityMode(String compatibilityModeI) {
	int idx;
	for (idx = 0; idx < COMPATIBLE_VERSIONS.length; ++idx) {
	    if (compatibilityModeI.equalsIgnoreCase
		(COMPATIBLE_VERSIONS[idx])) {
		break;
	    }
	}

	if (idx == COMPATIBLE_VERSIONS.length) {
	    throw new java.lang.IllegalArgumentException
		(compatibilityModeI + " is not a supported version.");
	}

	compatibilityMode = compatibilityModeI;
    }

    /**
     * Sets the debug level.
     * <p>
     *
     * @author Ian Brown
     *
     * @param debugLevelI the debug level.
     * @see com.rbnb.api.Log
     * @see #getDebugLevel()
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  INB	Created.
     *
     */
    private final void setDebugLevel(byte debugLevelI) {
	debugLevel = debugLevelI;
    }

    /**
     * Sets the debug mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @param debugMaskI the debug mask.
     * @see com.rbnb.api.Log
     * @see #getDebugMask()
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  INB	Created.
     *
     */
    private final void setDebugMask(long debugMaskI) {
	debugMask = debugMaskI;
    }

    /**
     * Sets the <code>License</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param licenseI the <code>License</code>.
     * @see #getLicense()
     * @since V2.0
     * @version 10/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    /*
    private final void setLicense(License licenseI) {
	license = licenseI;
    }
    */

    /**
     * Sets the log for this RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param logI the <code>Log</code>.
     * @see #getLog()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    private final void setLog(Log logI) {
	log = logI;
    }

    /**
     * Sets the log status period.
     * <p>
     *
     * @author Ian Brown
     *
     * @param logStatusPeriodI the log status period.
     * @see #getLogStatusPeriod()
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
    private final void setLogStatusPeriod(long logStatusPeriodI) {
	logStatusPeriod = logStatusPeriodI;
    }

    /**
     * Sets the maximum number of activity threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maxActivityThreadsI the maximum number of activity threads.
     * @see #getMaxActivityThreads()
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
    final void setMaxActivityThreads(int maxActivityThreadsI) {
	maxActivityThreads = maxActivityThreadsI;
    }

    /**
     * Sets the maximum number of open filesets.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maxOpenFileSetsI the maximum number of open filesets.
     * @see #getMaxOpenFileSets()
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
    final void setMaxOpenFileSets(int maxOpenFileSetsI) {
	maxOpenFileSets = maxOpenFileSetsI;
    }

    /**
     * Sets the metrics period.
     * <p>
     *
     * @author Ian Brown
     *
     * @param metricsPeriodI the metrics period.
     * @see #getMetricsPeriod()
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
    private final void setMetricsPeriod(long metricsPeriodI) {
	metricsPeriod = metricsPeriodI;
    }

    /**
     * Sets the metrics <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param metricsSourceI the metrics <code>Source</code>.
     * @see #getMetricsSource()
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
    private final void setMetricsSource(Source metricsSourceI) {
	metricsSource = metricsSourceI;
    }

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
    public final void setOpenFileSets(LimitedResource openFileSetsI) {
	openFileSets = openFileSetsI;
    }

    /**
     * Sets the <code>Paths</code> for this <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pathsI the <code>Paths</code>.
     * @see #getPaths()
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
    private final void setPaths(com.rbnb.utility.SortedVector pathsI) {
	paths = pathsI;
    }

    /**
     * Sets the <code>Door</code> for finding <code>Paths</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pathDoorI the new <code>Door</code>.
     * @see #getPathDoor()
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
    private final void setPathDoor(Door pathDoorI) {
	pathDoor = pathDoorI;
    }

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
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  INB	Created.
     *
     */
    public final  void setRAMPort(RAMServerCommunications portI) {
	ramPort = portI;
    }

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
    public final void setServerPort(Object serverPortI) {
	serverPort = serverPortI;
	if (serverPort instanceof RAMServerCommunications) {
	    setRAMPort((RAMServerCommunications) serverPortI);
	}
    }

    /**
     * Sets the terminating client's name.
     * <p>
     * The terminating client is the <code>Client</code> object that asked this
     * <code>RBNB</code> to terminate. If it exists, it cannot be
     * terminated while the <code>RBNB</code> is shutting down.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the name of the terminating client.
     * @see #getTerminatingClient()
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
    final void setTerminatingClient(String clientI) {
	terminatingClient = clientI;
    }

    /**
     * Sets the thread running this <code>RBNB</code>.
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
     * 05/11/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI) {
	thread = threadI;
    }

    /**
     * Sets the task timer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timerI the task timer.
     * @see #getTimer()
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
    private final void setTimer(Timer timerI) {
	timer = timerI;
    }

    /**
     * Starts this <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> performing the start.
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
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    public final void start(ClientHandler clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	start(clientI,null);
    }

    /**
     * Starts this <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> performing the start.
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
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 05/10/2001  INB	Created.
     *
     */
    public final void start(ClientHandler clientI,String[] argsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Create the log.
	setLog(new Log());
        //EMF 11/7/2006: add archive, extend times if trimbytime
        String doTrim=System.getProperty("trimbytime","false");
	if (!doTrim.equals("false")) {
          getLog().setCframes(3600); //one hour
          getLog().setAframes(31536000); //one year
          getLog().setAmode(Source.ACCESS_CREATE);
	} else {
          getLog().setCframes(1000);
        }

	// Create the metrics <code>Source</code>.
	setMetricsSource(getClientSide().createRAMSource("_Metrics"));
	getMetricsSource().setCframes(60*60*1000/TimerPeriod.METRICS);

	// Parse the command line arguments.
	if (argsI != null) {
	    parseArguments(argsI);
	    if (getMetricsPeriod() == 0) {
		setMetricsSource(null);
	    }
	}

	// Start the thread.
	setThread(new ThreadWithLocks(this,getName()));
	getThread().start();
	synchronized (this) {
	    while ((getThread() != null) &&
		   getThread().isAlive() &&
		   (getServerPort() == null)) {
		wait(TimerPeriod.NORMAL_WAIT);
	    }
	}
	Thread.currentThread().yield();
    }

    /**
     * Terminates the <code>RBNB</code>.
     * <p>
     * The terminating client is not stopped by the <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> performing the stop.
     * @see #start(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 05/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/28/2003  INB	Use our name as our log channel.
     * 01/02/2001  INB	Created.
     *
     */
    public final void stop(ClientHandler clientI) {
	try {
	    // Terminate the timer.
	    if (getTimer() != null) {
		try {
		    getTimer().cancel();
		} catch (java.lang.Exception e) {
		}
		setTimer(null);
	    }

	    if (clientI == null) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Terminating.");
	    } else {
		getLog().addMessage(getLogLevel(),
				    getLogClass(),
				    getName(),
				    "Terminating on request from " +
				    clientI.getName() +
				    ".");
	    }
	    Thread.currentThread().yield();
	} catch (java.lang.Exception e) {
	}

	synchronized (this) {
	    // Set the terminating client name.
	    if (clientI != null) {
		setTerminatingClient(clientI.getName());
	    } else {
		setTerminatingClient(null);
	    }

	    // Ask for the <code>RBNB</code> thread to terminate.
	    setTerminateRequested(true);
	    notifyAll();

	    if (getThread() != null) {
		getThread().interrupt();
	    }

	    // Wait for it to stop.
	    long shutdownAt = System.currentTimeMillis();
	    while (isRunning() &&
		   getTerminateRequested() &&
		   (System.currentTimeMillis() - shutdownAt <
		    TimerPeriod.SHUTDOWN)) {
		try {
		    wait(TimerPeriod.NORMAL_WAIT);
		} catch (java.lang.InterruptedException e) {
		    break;
		}
	    }
	}
    }

    /**
     * Executes a task on a timer.
     * <p>
     * This method executes one of a number of possible tasks based on the code
     * specified in the input <code>TimerTask</code>. The tasks are:
     * <p><ul>
     * <li><code>TT_RECONNECT</code> - reconnects to our parent if the
     *     connection has been lost. This task is cancelled if the connection
     *     is already established or can be re-established.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param ttI the <code>TimerTask</code>.
     * @since V2.0
     * @version 05/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 03/26/2003  INB	Add exceptions at log level 0.
     * 02/28/2003  INB	Use our name as our log channel.
     * 12/05/2001  INB	Created.
     *
     */
    public final void timerTask(TimerTask ttI) {
	try {
	    if (ttI.getCode().equalsIgnoreCase(TT_RECONNECT)) {
		if (((ParentServer) getParent()).reconnect()) {
		    try {
			ttI.cancel();
		    } catch (java.lang.Exception e) {
		    }
		}
	    }
	} catch (java.lang.Exception e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {
	    }
	}
    }

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
     * @version 02/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/28/2003  INB	Ensure that the client does not share a name with
     *			the server.
     * 06/04/2001  INB	Created.
     *
     */
    public final synchronized void uniqueName(ClientInterface ciIO)
    	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (ciIO.getName() == null) {
	    ciIO.setName("_");
	    uniqueName(ciIO);

	} else {
	    String name = ciIO.getName();
	    int idx = 1;
	    if (name.equals(getName())) {
		// If the name of the client conflicts with the name of the
		// server, then try to find a unique name.
		ciIO.setName(name + "_" + idx);
		++idx;
	    }
	    Rmap chld;
	    for (; (chld = findChild((Rmap) ciIO)) != null; ++idx) {
		if (ciIO != chld) {
		    // If there is an existing child with the caller specified
		    // name, then try to find a unique name.
		    ciIO.setName(name + "_" + idx);
		} else {
		    break;
		}
	    }
	}
    }

    /**
     * Unlocks access to this <code>RBNB</code> for routing.
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
    public final void unlockRouting()
	throws java.lang.InterruptedException
    {
	routingDoor.unlock();
    }

    /**
     * Stores the setup for a periodic source such as the log or metrics.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/19/2003
     */

    /*
     * Copyright 2002, 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2003  INB	Handle lack of <code>Double.parseDouble</code>.
     * 11/25/2002  INB	Created.
     *
     */
    private final class PeriodicSource {

	/**
	 * the size of the archive in frames.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
	private long aFrames;

	/**
	 * the archive access mode.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
	private byte aMode;

	/**
	 * the size of the cache in frames.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
	private long cFrames;

	/**
	 * the period in milliseconds.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
	private long period;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
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
	private PeriodicSource() {
	    super();
	}

	/**
	 * Creates a <code>PeriodicSource</code> from parameters.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param nameI   the name of the source.
	 * @param valueI  the command line switch value to parse.
	 * @param periodI the default period.
	 * @param cFrames the default cache size in frames.
	 * @param aFrames the default archive size in frames.
	 * @param aModeI  the default archive mode.
	 * @exception java.lang.IllegalArgumentException
	 *	      if the switch value cannot be parsed.
	 * @since V2.0
	 * @version 03/19/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 03/19/2003  INB	Handle lack of <code>Double.parseDouble</code>.
	 * 11/25/2002  INB	Created.
	 *
	 */
	PeriodicSource(String nameI,
		       String valueI,
		       long periodI,
		       long cFramesI,
		       long aFramesI,
		       byte aModeI)
	{
	    this();
	    setPeriod(periodI);
	    setCframes(cFramesI);
	    setAframes(aFramesI);
	    setAmode(aModeI);
	    try {
		java.util.StringTokenizer st = new java.util.StringTokenizer
		    (valueI,
		     ",",
		     true);
		String token;

		for (int aIdx = 0; st.hasMoreTokens() && (aIdx < 4); ++aIdx) {
		    token = st.nextToken();

		    if (!token.equals(",")) {
			switch (aIdx) {
			case 0:
			    if (token.equalsIgnoreCase("OFF")) {
				setPeriod(0);
				setCframes(0);
				setAframes(0);
				setAmode(SourceInterface.ACCESS_NONE);

			    } else if (token.equalsIgnoreCase("NONE")) {
				setPeriod(0);

			    } else {
				try {
				    setPeriod((long)
					      (Double.parseDouble
					       (token)*1000.));
				} catch (java.lang.NoSuchMethodError e) {
				    setPeriod
					((long)
					 ((new Double(token)).doubleValue()*
					   1000.));
				}
			    }
			    break;

			case 1:
			    setCframes(Long.parseLong(token));
			    break;

			case 2:
			    setAframes(Long.parseLong(token));
			    if (getAmode() != SourceInterface.ACCESS_APPEND) {
				if (getAframes() == 0) {
				    setAmode(SourceInterface.ACCESS_NONE);
				} else {
				    setAmode(SourceInterface.ACCESS_CREATE);
				}
			    }
			    break;

			case 3:
			    if (token.equalsIgnoreCase("CREATE")) {
				setAmode(SourceInterface.ACCESS_CREATE);
				if (getAframes() == 0) {
				    setAframes(getCframes());
				}
			    } else if (token.equalsIgnoreCase("APPEND")) {
				setAmode(SourceInterface.ACCESS_APPEND);
			    } else {
				setAmode(SourceInterface.ACCESS_NONE);
			    }
			}
			if (st.hasMoreTokens()) {
			    token = st.nextToken();
			}
		    }
		}

	    } catch (java.lang.Exception e) {
		throw new java.lang.IllegalArgumentException
		    ("Unable to parse " + nameI + " argument: " + valueI +
		     ".\n" +
		     e.getClass() + " " + e.getMessage());
	    }
	}

	/**
	 * Gets the number of frames in the archive.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the number of archive frames.
	 * @see #setAframes(long)
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
	final long getAframes() {
	    return (aFrames);
	}

	/**
	 * Gets the archive access mode.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the archive access mode.
	 * @see #setAmode(byte)
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
	final byte getAmode() {
	    return (aMode);
	}

	/**
	 * Gets the number of frames in the cache.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the number of cache frames.
	 * @see #setCframes(long)
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
	final long getCframes() {
	    return (cFrames);
	}

	/**
	 * Gets the period.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the period.
	 * @see #setPeriod(long)
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
	final long getPeriod() {
	    return (period);
	}

	/**
	 * Sets the number of frames in the archive.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param aFramesI the number of archive frames.
	 * @see #getAframes()
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
	private final void setAframes(long aFramesI) {
	    aFrames = aFramesI;
	}

	/**
	 * Sets the archive access mode.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param aModeI the archive access mode.
	 * @see #getAmode()
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
	private final void setAmode(byte aModeI) {
	    aMode = aModeI;
	}

	/**
	 * Sets the number of frames in the cache.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param aFramesI the number of cache frames.
	 * @see #getCframes()
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
	private final void setCframes(long cFramesI) {
	    cFrames = cFramesI;
	}

	/**
	 * Sets the period.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param periodI the period.
	 * @see #getPeriod()
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
	private final void setPeriod(long periodI) {
	    period = periodI;
	}
    }
}
