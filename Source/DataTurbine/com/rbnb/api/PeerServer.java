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
 * Peer server class.
 * <p>
 * Peer servers are connected servers whose connection is not related to the
 * normal <code>Rmap</code> hierarchy. Any two servers that support
 * peer-to-peer connections can be directly routed together, providing an
 * alternative path. If one server is a peer of a second, the first server is
 * also considered to be a peer of all of the peers of the second server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 07/21/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/06/2011  MJM  Handle parse exception in SimpleDateFormat (Android JVM)
 * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
 * 01/14/2004  INB	Added synchronization of metrics.
 * 05/23/2003  INB	Find paths only if things change when disconnected.
 * 05/02/2003  INB	Call <code>SortedVector(1)</code> or
 *			<code>SortedVector(size())</code> rather than
 *			<code>SortedVector()</code>.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/20/2003  INB	Allow related <code>Routers</code> to keep us up.
 * 02/27/2003  INB	If the search for the next target turns up this
 *			<code>PeerServer</code>, then let our superclass handle
 *			delivery. Also let our superclass handle things if we
 *			believe we need to deliver the message directly and
 *			routing for this server is on.
 * 02/26/2003  INB	Terminate related <code>RouterHandlers</code>.
 *			Perform superclass disconnect. Ensure that we don't try
 *			to shutdown again in <code>findPath</code>.
 * 12/17/2001  INB	Created.
 *
 */
class PeerServer
    extends com.rbnb.api.ConnectedServer
    implements com.rbnb.api.BuildInterface,
	       com.rbnb.api.PeerInterface
{
    /**
     * the number of active shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2002
     */
    private int activeShortcuts = 0;

    /**
     * the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/24/2001
     */
    private java.util.Date buildDate = null;

    /**
     * the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/25/2001
     */
    private String buildVersion = null;

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
     * the number of passive shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2002
     */
    private int passiveShortcuts = 0;

    /**
     * the shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/02/2003
     */
    private com.rbnb.utility.SortedVector shortcuts =
	new com.rbnb.utility.SortedVector(1,"SCN");

    /**
     * update counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private long updateCounter = 0;

    // Private constants:
    private final static byte PAR_BLD = 0;
    private final static byte PAR_BLV = 1;
    private final static byte PAR_LIC = 2;
    private final static byte PAR_UPC = 3;

    private final static String[] PARAMETERS = {
				    "BLD",
				    "BLV",
				    "LIC",
				    "UPC"
				};

    // Private class fields:
    private static String[] ALL_PARAMETERS = null;

    private static int parametersStart = 0;

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
    PeerServer() {
	super();
    }

    /**
     * Class constructor to build a <code>PeerServer</code> for a name.
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
    PeerServer(String nameI)
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
     * Class constructor to build a <code>PeerServer</code> for a name
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
    PeerServer(String nameI,String addressI)
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
     * Class constructor to build a <code>PeerServer</code> by reading it in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
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
    PeerServer(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>PeerServer</code> by reading it in.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>PeerServer</code> as an <code>Rmap</code>.
     * @param isI    the <code>InputStream</code>.
     * @param disI   the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
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
    PeerServer(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation returns the <code>Server's</code> address.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 02/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    final String additionalToString() {
	String stringR = "  Address: " + getAddress();

	if (getBuildDate() != null) {
	    stringR += " Built: " + getBuildDate();
	}
	if (getBuildVersion() != null) {
	    stringR += " Version: " + getBuildVersion();
	}
	/*
	if (getLicenseString() != null) {
	    stringR += " License: " + getLicenseString();
	}
	*/

	return (stringR);
    }

    /**
     * Adds a child <code>Rmap</code> to this <code>PeerServer</code>.
     * <p>
     * If the input is a <code>Shortcut</code>, it is also added to the list of
     * <code>Shortcuts</code>.
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
     *		  thrown if the input <code>Rmap</code> is already a
     *		  child of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
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

	if (childI instanceof Shortcut) {
	    getShortcuts().addElement(childI);
	}
    }

    /**
     * Adds the <code>PeerServer's</code> parameters to the full serialization
     * parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 12/17/2001  INB	Created.
     *
     */
    final static String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = Server.addToParameters(null);

	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}

	return (addToParameters(parametersR,PARAMETERS));
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
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/06/2001  INB	Created.
     *
     */
    boolean createReversedRouter(Router routerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean handledR = false;

	ServerHandler sHandler = getLocalServerHandler();
	Shortcut sc = sHandler.findShortcut(this);

	if (sc != null) {
	    Rmap pServer = ((Rmap) sHandler).newInstance();
	    pServer.addChild(((Rmap) sc).newInstance());
	    ReverseRoute reverse = new ReverseRoute(pServer);
	    routerI.send(reverse);
	    routerI.receive(null,false,Client.FOREVER);
	    handledR = true;
	}

	return (handledR);
    }

    /**
     * Defaults for all parameters.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code> into
     * this one. It is designed to be overridden by higher level objects to
     * ensure that they handle all of their parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param seenI  the fields that we've seen already.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
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
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultPeerServerParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Default <code>PeerServer</code> parameters.
     * <p>
     * This method fills in any fields not read from an input stream by copying
     * them from the input <code>Rmap</code>.
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code> as an <code>Rmap</code>.
     * @param seenI  the fields seen.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    final void defaultPeerServerParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((otherI != null) && (otherI instanceof PeerServer)) {
	    PeerServer other = (PeerServer) otherI;

	    if ((seenI == null) || !seenI[parametersStart + PAR_BLD]) {
		setBuildDate(other.getBuildDate());
	    }
	    if ((seenI == null) || !seenI[parametersStart + PAR_BLV]) {
		setBuildVersion(other.getBuildVersion());
	    }
	    /*
	    if ((seenI == null) || !seenI[parametersStart + PAR_LIC]) {
		setLicenseString(other.getLicenseString());
	    }
	    */
	    if ((seenI == null) || !seenI[parametersStart + PAR_UPC]) {
		setUpdateCounter(other.getUpdateCounter());
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
     * @version 02/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2003  INB	If the search for the next target turns up this
     *			<code>PeerServer</code>, then let our superclass
     *			handle delivery. Also let our superclass handle things
     *			if we believe we need to deliver the message directly
     *			and routing for this server is on.
     * 01/02/2002  INB	Created.
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
	    ServerHandler sHandler = getLocalServerHandler();

	    if ((sHandler.findShortcut(this) == null) &&
		(getType() == ROUTE_OFF)) {
		Path path = messageI.getPath();

		if (messageI.getAtIndex() < path.getOrdered().size()) {
		    // If this message is being routed via this server to a
		    // very remote target, then pass it via the next routing
		    // server.
		    Rmap rTarget = getRoutingMapHandler().findChild
			(Rmap.createFromName
			 ((String) path.getOrdered().elementAt
			  (messageI.getAtIndex())));

		    if (rTarget == null) {
			// If there is no way to get to the desired target,
			// then try to deliver it using the local information.
			messageI.setPath(null);
			messageI.setAtIndex(0);

		    } else if (rTarget == this) {
			// If we reach ourself, then we break out and allow
			// our superclass to handle this message.

		    } else {
			serializableR =
			    ((RemoteServer) rTarget).deliver(messageI,-1);
		    }
		}
	    }
	}

	if (serializableR == null) {
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
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/20003 INB	Find paths only if things have changed.
     * 05/02/2003  INB	Call <code>SortedVector(size())</code> rather than
     *			<code>SortedVector()</code>.
     * 02/26/2003  INB	Ensure that we do our superclass's disconenct, too.
     * 01/02/2002  INB	Created.
     *
     */
    final void disconnectedRouting() {
	SortedStrings toFind = new SortedStrings();

	try {
	    // Disconnect all of the shortcuts.
	    com.rbnb.utility.SortedVector lShortcuts =
		new com.rbnb.utility.SortedVector(getShortcuts().size(),
						    "SCN");
	    for (int idx = 0; idx < getShortcuts().size(); ++idx) {
		lShortcuts.addElement(getShortcuts().elementAt(idx));
	    }
	    for (int idx = 0; idx < lShortcuts.size(); ++idx) {
		Shortcut shortCut = (Shortcut) lShortcuts.elementAt(idx);

		toFind.add(shortCut.getDestinationName());
		removeChild((Rmap) shortCut);
	    }

	    // Find the new paths.
	    if (toFind.size() > 0) {
		getLocalServerHandler().findPaths(toFind);
	    }

	    super.disconnectedRouting();
	} catch (java.lang.Exception e) {
	}
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
     * @version 03/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/20/2003  INB	Allow related <code>Routers</code> to keep us up.
     * 02/26/2003  INB	If we're being terminated, we need not try to cleanup.
     * 12/17/2001  INB	Created.
     *
     */
    public Path findPath()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Path pathR = getLocalServerHandler().findPathTo(this);

	if ((pathR == null) && !getTerminateRequested()) {
	    // When no <code>Path</code> can be found to a
	    // <code>PeerServer</code>, then we need to find a reason for 
	    // the <code>PeerServer</code> to continue to exist.
	    if ((getNchildren() == 0) && (getRelatedRouters().size() == 0)) {
		// With no children, we can shutdown.
		lostRouting();
		stop();
	    }
	}

	return (pathR);
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
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final void findPaths(long pathFindCounterI,
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

		// Create <code>LocalPaths</code> to the other end of each of
		// this <code>PeerServer's</code> shortcuts.
		for (int idx = 0; idx < getShortcuts().size(); ++idx) {
		    Shortcut shortCut = (Shortcut)
			getShortcuts().elementAt(idx);

		    LocalPath lPath = new LocalPath(pathToHereI,shortCut);
		    pathsToCheckIO.add(lPath);
		}

		// Add in our parent for a cost of one.
		if (getParent() instanceof RemoteServer) {
		    Shortcut shortCut = new ShortcutIO();
		    shortCut.setDestinationName(getParent().getFullName());
		    shortCut.setCost(1);
		    LocalPath lPath = new LocalPath(pathToHereI,shortCut);
		    pathsToCheckIO.add(lPath);
		}

		// Also, add in any child servers with a cost of one.
		for (int idx = 0; idx < getNchildren(); ++idx) {
		    Rmap child;
		    if ((child = getChildAt(idx)) instanceof Server) {
			Shortcut shortCut = new ShortcutIO();
			shortCut.setDestinationName(child.getFullName());
			shortCut.setCost(1);
			LocalPath lPath = new LocalPath(pathToHereI,shortCut);
			pathsToCheckIO.add(lPath);
		    }
		}
	    }

	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.InternalError();
	}
    }

    /**
     * Finds a <code>Shortcut</code> to the specified <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerI the <code>PeerServer</code>.
     * @return the <code>Shortcut</code> or null.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final Shortcut findShortcut(PeerServer peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    Shortcut shortcutR = (Shortcut)
		((com.rbnb.utility.SortedVector)
		 getShortcuts()).find(peerI.getFullName());

	    return (shortcutR);
	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.InternalError();
	}
    }

    /**
     * Gets the number of active shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of active shortcuts.
     * @see #setActiveShortcuts(int)
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    final int getActiveShortcuts() {
	return (activeShortcuts);
    }

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
     * Gets the log class mask for this <code>PeerServer</code>.
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
	return (super.getLogClass() | Log.CLASS_PEER_SERVER);
    }

    /**
     * Gets the number of passive shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of passive shortcuts.
     * @see #setPassiveShortcuts(int)
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    final int getPassiveShortcuts() {
	return (passiveShortcuts);
    }

    /**
     * Gets the registration list for this <code>PeerServer</code> matching
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
     * @version 06/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2002  INB	Created.
     *
     */
    public Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;

	LocalPath lPath = (LocalPath) findPath();

	if ((lPath != null) &&
	    (lPath.getCost() < ShortcutHandler.PASSIVE_COST)) {
	    rmapR = super.getRegistered(requestI);
	} else {
	    rmapR = newInstance();
	    getLocalRegistered(requestI,null,rmapR);
	}

	return (rmapR);
    }

    /**
     * Gets the list of <code>Shortcuts</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of <code>Shortcuts</code>.
     * @see #setShortcuts(java.util.Vector)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final java.util.Vector getShortcuts() {
	return (shortcuts);
    }

    /**
     * Gets the update counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the update counter value.
     * @see #setUpdateCounter(long)
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
    public final long getUpdateCounter() {
	return (updateCounter);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
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
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Lost routing to this <code>ParentServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added synchronization of metrics.
     * 02/26/2003  INB	Terminate related <code>RouterHandlers</code>.
     * 12/14/2001  INB	Created.
     *
     */
    void lostRouting() {
	if (!getConnected()) {
	    return;
	}
	setConnected(false);

	synchronized (getAvailableRouters()) {
	    getAvailableRouters().notifyAll();
	}

	// Disconnect and terminate related <code>RouterHandlers</code>.
	disconnectedRouting();
	terminateRelated();

	// Find and shut down the local peer connection to this server.
	try {
	    while (getActiveShortcuts() + getPassiveShortcuts() > 0) {
		ShortcutHandler shortCut = (ShortcutHandler)
		    getLocalServerHandler().findShortcut(this);
		if (shortCut != null) {
		    shortCut.stop();
		} else {
		    setActiveShortcuts(0);
		    setPassiveShortcuts(0);
		}
	    }
	} catch (java.lang.Exception e) {
	}

	try {
	    if (getNchildren() == 0) {
		Rmap lParent = getParent();
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getFullName(),
		     "Lost all connections to peer server. " +
		     "Removing peer server from routing map.");
		if (lParent instanceof RemoteServer) {
		    RemoteServer rParent = (RemoteServer) lParent;
		    synchronized (rParent.metricsSyncObj) {
			lParent.removeChild(this);
			rParent.metricsDeadBytes += bytesTransferred();
		    }
		    rParent.lostRouting();
		} else if (lParent instanceof RBNBRoutingMap) {
		    RBNBRoutingMap rParent = (RBNBRoutingMap) lParent;
		    synchronized (rParent.metricsSyncObj) {
			lParent.removeChild(this);
			rParent.metricsDeadBytes += bytesTransferred();
		    }
		}
	    }

	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Creates a new instance of the same class as this
     * <code>PeerServer</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PeerServer pServerR = new PeerServer(getName(),getAddress());

	pServerR.setBuildDate(getBuildDate());
	pServerR.setBuildVersion(getBuildVersion());
	//	pServerR.setLicenseString(getLicenseString());
	pServerR.setUpdateCounter(getUpdateCounter());

	return (pServerR);
    }

    /**
     * Pass a peer's update to the <bold>RBNB</bold> server represented by this
     * <code>PeerInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateI the <code>PeerUpdate</code> to pass on.
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
    public final void passUpdate(PeerUpdate peerUpdateI) {
	Router router = null;
	try {
	    router = grabRouter();
	    router.send(peerUpdateI);
	    router.receive(ACO.okClass,false,Client.FOREVER);
	} catch (com.rbnb.api.AddressException e) {
	} catch (com.rbnb.api.SerializeException e) {
	} catch (java.io.IOException e) {
	} catch (java.lang.InterruptedException e) {
	} finally {
	    if (router != null) {
		releaseRouter(router);
	    }
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
     * 12/20/2001  INB	Created.
     *
     */
    final void updatePeer(PeerUpdate peerUpdateI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getUpdateCounter() < peerUpdateI.getPeerUpdateCounter()) {
	    setUpdateCounter(peerUpdateI.getPeerUpdateCounter());
	    RoutingMapHandler rHandler = getRoutingMapHandler();

	    if (peerUpdateI.getShortcuts() != null) {
		for (int idx = 0;
		     idx < peerUpdateI.getShortcuts().size();
		     ++idx) {
		    Shortcut shortcut =
			(Shortcut) peerUpdateI.getShortcuts().elementAt(idx);
		    Rmap entry = rHandler.findDescendant
			(shortcut.getDestinationName(),
			 false);
		    PeerServer peerServer = null;

		    if (entry == null) {
			Rmap hierarchy = Rmap.createFromName
			    (shortcut.getDestinationName(),
			     new Server()),
			    bottom = null;
			for (bottom = hierarchy;
			     bottom.getNchildren() == 1;
			     bottom = bottom.getChildAt(0)) {}
			Rmap above = bottom.getParent();
			peerServer = new PeerServer
			    (bottom.getName(),
			     shortcut.getDestinationAddress());
			if (above != null) {
			    above.removeChildAt(0);
			    above.addChild(peerServer);
			} else {
			    hierarchy = peerServer;
			}
			entry = rHandler.createPeer(hierarchy,null);

		    } else if (!(entry instanceof PeerServer)) {
			Rmap hierarchy = Rmap.createFromName
			    (shortcut.getDestinationName(),
			     new Server()),
			    bottom = null;
			for (bottom = hierarchy;
			     bottom.getNchildren() == 1;
			     bottom = bottom.getChildAt(0)) {}
			peerServer = new PeerServer
			    (bottom.getName(),
			     shortcut.getDestinationAddress());
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
		}
	    }

	    com.rbnb.utility.SortedVector nShortcuts =
		peerUpdateI.getShortcuts();
	    Shortcut oShortcut = ((getShortcuts().size() == 0) ?
				  null :
				  (Shortcut) getShortcuts().firstElement()),
		nShortcut = ((nShortcuts.size() == 0) ?
			     null :
			     (Shortcut) nShortcuts.firstElement());
	    for (int idx = 0, idx1 = 0;
		 ((idx < getShortcuts().size()) || (idx1 < nShortcuts.size()));
		 ) {
		int difference;

		try {
		    if (oShortcut == null) {
			difference = 1;
		    } else if (nShortcut == null) {
			difference = -1;
		    } else {
			difference =
			    ((String) ((Rmap) oShortcut).sortField
			     ("SCN")).compareTo
			    ((String) ((Rmap) nShortcut).sortField("SCN"));
		    }
		} catch (com.rbnb.utility.SortException e) {
		    throw new java.lang.InternalError();
		}

		if (difference == 0) {
		    oShortcut.setCost(nShortcut.getCost());
		    ++idx;
		    ++idx1;
		} else if (difference < 0) {
		    removeChild((Rmap) oShortcut);
		} else {
		    addChild((Rmap) nShortcut);
		    ++idx;
		    ++idx1;
		}
	    }

	    getLocalServerHandler().findPaths(null);
	    getLocalServerHandler().broadcastUpdate(peerUpdateI);
	}
    }

    /**
     * Reads the <code>PeerServer</code> from the specified input stream.
     * <p>
     * This method uses the input <code>Rmap</code> to provide default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code> as an <code>Rmap</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
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
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Rmap</code>.
	Serialize.readOpenBracket(isI);

	// Initialize the full parameter list.
	initializeParameters();

	boolean[] seen = new boolean[ALL_PARAMETERS.length];
	int parameter;
	while ((parameter = Serialize.readParameter(ALL_PARAMETERS,
						    isI)) != -1) {
	    seen[parameter] = true;

	    // Read parameters until we see a closing bracket.
	    if (!readPeerServerParameter(parameter,isI,disI)) {
		if (!readServerParameter(parameter,isI,disI)) {
		    readStandardParameter(otherI,parameter,isI,disI);
		}
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads <code>PeerServer</code> parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI  the parameter index.
     * @param isI	  the input stream.
     * @param disI	  the data input stream.
     * @return was the parameter recognized?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeServerParameters(com.rbnb.api.Server,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 07/21/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
     * 12/18/2001  INB	Created.
     *
     */
    final boolean readPeerServerParameter(int parameterI,
					  InputStream isI,
					  DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean serverR = false;

	if (parameterI >= parametersStart) {
	    int parameter = parameterI - parametersStart;
	    serverR = true;

	    switch (parameter) {
	    case PAR_BLD:
		try {
		    String date = isI.readUTF();
		    setBuildDate
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy HH:mm:ss z",		// MJM parse error on z?
			     java.util.Locale.US)).parse(date));
		} catch (java.text.ParseException e) {
			setBuildDate(new java.util.Date());	// MJM 10/6/11: avoid parseException?
//		    throw new com.rbnb.api.SerializeException(e.getMessage());
		}
		break;

	    case PAR_BLV:
		setBuildVersion(isI.readUTF());
		break;

	    /*
	    case PAR_LIC:
		setLicenseString(isI.readUTF());
		break;
	    */

	    case PAR_UPC:
		setUpdateCounter(isI.readLong());
		break;

	    default:
		serverR = false;
		break;
	    }
	}

	return (serverR);
    }

    /**
     * Regained routing to this <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #lostRouting()
     * @since V2.0
     * @version 12/19/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    final void regainedRouting() {
	setConnected(true);
    }

    /**
     * Removes a child <code>Rmap</code> from this <code>PeerServer</code>.
     * <p>
     * If the input is a <code>Shortcut</code>, it is also removed from the
     * list of <code>Shortcuts</code>.
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
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final void removeChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.removeChild(childI);

	if (childI instanceof Shortcut) {
	    getShortcuts().removeElement(childI);
	}
    }

    /**
     * Removes the child <code>Rmap</code> at a particular index from this
     * <code>PeerServer</code>.
     * <p>
     * If the input is a <code>Shortcut</code>, it is also removed from the
     * list of <code>Shortcuts</code>.
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
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final void removeChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap child = getChildAt(indexI);
	super.removeChildAt(indexI);

	if (child instanceof Shortcut) {
	    try {
		getShortcuts().removeElement(child.sortField("SCN"));
	    } catch (com.rbnb.utility.SortException e) {
		throw new java.lang.InternalError();
	    }
	}
    }

    /**
     * Sets the number of active shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #getActiveShortcuts()
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    final void setActiveShortcuts(int activeShortcutsI) {
	activeShortcuts = activeShortcutsI;
    }

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
     * Sets the number of passive shortcuts.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #getPassiveShortcuts()
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    final void setPassiveShortcuts(int passiveShortcutsI) {
	passiveShortcuts = passiveShortcutsI;
    }

    /**
     * Sets the list of <code>Shortcuts</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutsI the list.
     * @see #getShortcuts()
     * @since V2.0
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>SortedVector(size())</code> rather than
     *			<code>SortedVector()</code>.
     * 01/08/2002  INB	Created.
     *
     */
    public final void setShortcuts(java.util.Vector shortcutsI) {
	if (!(shortcutsI instanceof com.rbnb.utility.SortedVector)) {
	    shortcuts = new com.rbnb.utility.SortedVector
		(shortcutsI.size(),
		 "SCN");
	    for (int idx = 0; idx < shortcutsI.size(); ++idx) {
		shortcuts.addElement(shortcutsI.elementAt(idx));
	    }
	} else {
	    shortcuts = (com.rbnb.utility.SortedVector) shortcutsI;
	}
    }

    /**
     * Sets the update counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @param updateCounterI the new update counter value.
     * @see #getUpdateCounter()
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    final void setUpdateCounter(long updateCounterI) {
	updateCounter = updateCounterI;
    }

    /**
     * Writes this <code>PeerServer</code> to the specified stream.
     * <p>
     * This method writes out the differences between this <code>Server</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>Server</code> as an
     *			   <code>Rmap</code>.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    void write(Rmap otherI,
	       String[] parametersI,
	       int parameterI,
	       OutputStream osI,
	       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Write out the object.
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged(this,parametersI,parameterI);

	writeStandardParameters(otherI,osI,dosI);
	writeServerParameters((Server) otherI,osI,dosI);
	writePeerServerParameters((PeerServer) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /** 
     * Writes out <code>PeerServer</code> parameters.
     * <p>
     * This method writes out the differences between this <code>Server</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code>.
     * @param osI    the output stream.
     * @param dosI   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readServerParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 07/21/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
     * 12/18/2001  INB	Created.
     *
     */
    final void writePeerServerParameters(PeerServer otherI,
					 OutputStream osI,
					 DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	if ((getBuildDate() != null) &&
	    ((otherI == null) ||
	     !getBuildDate().equals(otherI.getBuildDate()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_BLD);
	    String date = 
		(new java.text.SimpleDateFormat
		    ("MMM dd yyyy HH:mm:ss z",
		     java.util.Locale.US)).format(getBuildDate());
	    osI.writeUTF(date);
	}

	if ((getBuildVersion() != null) &&
	    ((otherI == null) ||
	     !getBuildVersion().equals(otherI.getBuildVersion()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_BLV);
	    osI.writeUTF(getBuildVersion());
	}

	/*
	if ((getLicenseString() != null) &&
	    ((otherI == null) ||
	     !getLicenseString().equals(otherI.getLicenseString()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_LIC);
	    osI.writeUTF(getLicenseString());
	}
	*/

	if ((otherI == null) ||
	    (getUpdateCounter() != otherI.getUpdateCounter())) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_UPC);
	    osI.writeLong(getUpdateCounter());
	}
    }
}
