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

package com.rbnb.admin;

import com.rbnb.api.Client;
import com.rbnb.api.Controller;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Shortcut;
import com.rbnb.api.Source;

/******************************************************************************
 * Handles interactions with the DataTurbine.
 * <p>
 * These interactions include: connecting, disconnecting, administrative
 * operations, and data passing.
 * <p>
 *
 * @author John P. Wilson
 *
 * @since V2.0
 * @version 08/27/2010
 */

/*
 * Copyright 2001,2002,2003,2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/27/2010  JPW	Moved the guts of createMirror() and createTimeMirror()
 * 			to com.rbnb.sapi.Control.
 * 01/24/2005  JPW	Set a Username on the Server connection.
 * 01/06/2005  JPW	Added <code>loadArchive</code>.
 * 11/24/2003  INB	Added <code>createTimeMirror</code>.
 * 04/08/2003  INB	If the client is a Source, clear the keep cache
 *			flag in stop(Client). This will ensure that the RBO
 *			will actually go away.
 * 11/20/2002  INB	Mark leaf nodes before making requests.
 * 02/15/2002  JPW	Add "currentThread" cubbyhole for managing cases
 *			    where a thread gets blocked in a synchronized
 *			    method.  This object will get set and then
 *			    cleared in each synchronized method.
 *			Make closeConnection() *not* synchronized.
 * 02/13/2002  JPW	Add startShortcut(ShortcutData shortDataI).
 * 01/23/2002  JPW	Add stop(Shortcut shortcutI).
 * 06/04/2001  JPW	Add controllerName and getControllerName().
 *			Add resetState().
 * 05/01/2001  JPW	Created.
 *
 */

public class RBNBDataManager {
    
    /**
     * The Admin object using this instance of RBNBDataManager.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private Admin admin = null;
    
    /**
     * The full name of the server to which Admin is connected.
     * <p>
     * Only used in the following: set in the constructor and used in
     * openConnection().
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private String serverName = null;
    
    /**
     * Address of the server to which Admin is connected.
     * <p>
     * Used in the following:
     *   o value is set in the constructor
     *   o used in openConnection()
     *   o used in loadArchive()
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private String serverAddress = null;
    
    /**
     * Server to which this class maintains a connection.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private Server server = null;
    
    /**
     * Name of the controller.
     * <p>
     * The default controller name is "Admin", but if more than one Admin is
     * connected the Server will automatically reassign a unique name.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/04/2001
     */
    private String controllerName = null;
    
    /**
     * Client connected to the Server.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private Controller controller = null;
    
    /**
     * Connected to a DataTurbine?
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private boolean connected = false;
    
    /**
     * Cubbyhole variable for managing cases where a thread gets blocked
     * in a synchronized method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/15/2002
     */
    public Thread currentThread = null;
    
    /**
     * Username to set on the Server connection.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.5
     * @version 01/24/2005
     */
    public Username username = null;
    
    /**************************************************************************
     * Initialize data manager.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param adminI  administrative GUI that the user interacts with
     * @param serverNameI  name of the server to connect to
     * @param serverAddressI  address of the server to connect to
     * @param usernameI  Username to set in the Server connection
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add usernameI argument
     * 06/04/2001  JPW	Initialize controllerName.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public RBNBDataManager(
	Admin adminI,
	String serverNameI,
	String serverAddressI,
	Username usernameI)
    {
	
	admin = adminI;
	
	if ( (serverNameI == null) || (serverNameI.equals("")) ) {
	    serverName = "DTServer";
	}
	else {
	    serverName = serverNameI;
	}
	
	if ( (serverAddressI == null) || (serverAddressI.equals("")) ) {
	    serverAddress = "localhost:3333";
	}
	else {
	    serverAddress = serverAddressI;
	}
	
	// JPW 01/24/2005: Add username setting
	username = usernameI;
	
	// JPW 6/4/2001: Set default controllerName.
	controllerName = "Admin";
	
    }
    
    /**************************************************************************
     * Opens a connection to the DataTurbine.
     * <p>
     *
     * @author John P. Wilson
     *
     * @exception java.lang.Exception
     *            thrown if there is an error establishing the DataTurbine
     *            connection
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Set the username for the Server connection.
     * 02/15/2002  JPW	Add support for currentThread.
     * 06/04/2001  JPW	Add setting of controllerName.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public synchronized void openConnection() throws Exception {
	
	currentThread = Thread.currentThread();
	
	connected = false;
	
	try {
	    server = Server.newServerHandle(serverName, serverAddress);
	    controller = server.createController(controllerName);
	    // JPW 01/24/2005: Added Username to the connection
	    com.rbnb.api.Username tempUsername =
	        new com.rbnb.api.Username(username.username,username.password);
	    controller.setUsername(tempUsername);
	    controller.start();
	    // JPW 01/29/2002: Call "getFullName" instead of just "getName"
	    //                 in order to obtain the full server name.
	    serverName = server.getFullName();
	    serverAddress = server.getAddress();
	    controllerName = controller.getName();
	} catch (Exception e) {
	    resetState();
	    currentThread = null;
	    throw new Exception(
		"Failed to connect to " + serverAddress + ".\n" +
		e.getMessage());
	}
	
	if ( (server == null) || (controller == null) ) {
	    resetState();
	    currentThread = null;
	    throw new Exception("Failed to connect to " + serverAddress + ".");
	}
	
	System.err.println(
	    "Controller \"" +
	    controllerName +
	    "\" connected to Server \"" +
	    serverName +
	    "\" at address \"" +
	    serverAddress +
	    "\".");
	
	connected = true;
	
	currentThread = null;
	
    }
    
    /**************************************************************************
     * Closes a previously opened connection to the DataTurbine.
     * <p>
     *
     * @author John P. Wilson
     *
     * @exception java.lang.Exception
     *            thrown if there is an error closing the connection
     * @since V2.0
     * @version 05/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/15/2002  JPW	Make this method *not* synchronized.  When the user
     *			    wants to close the connection to the Server, we
     *			    don't want another thread blocking this action.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void closeConnection() throws Exception {
	
	if (!connected) {
	    return;
	}
	
	controller.stop();
	
	System.err.println(
	    "Controller \"" +
	    controllerName +
	    "\" disconnected from Server \"" +
	    serverName +
	    "\".");
	
	resetState();
	
    }
    
    /**************************************************************************
     * Reset variables in order to be consistent with having a closed
     * connection.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  JPW  Created.
     *
     */
    
    private void resetState() {
	
	server = null;
	controller = null;
	controllerName = "Admin";
	connected = false;
	
    }
    
    /**************************************************************************
     * Gets the top level Rmap object with all of its children from the
     * DataTurbine connection.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param fullPathNameI  Full path to the object of interest.  If this
     *                       argument is null, then request the full hierarchy
     *                       from the ultimate parent server down.
     * @return the <code>Rmap</code> obtained from the DataTurbine
     * @exception java.lang.Exception
     *            thrown if there is an error obtaining the <code>Rmap</code>
     * @since V2.0
     * @version 02/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/15/2002  JPW	Add support for currentThread.
     * 02/13/2002  JPW	Re-obtain the server name everytime we request
     *                  an Rmap for the connected server.
     * 12/21/2001  JPW  Add fullPathNameI
     * 11/05/2001  INB  Make the name absolute and use createFromName.
     * 06/04/2001  JPW	To request an Rmap from the Server which includes all
     *                  of its children, I've switched over to using the
     *                  server name (stored in serverName) obtained directly
     *                  from the Server object.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public synchronized Rmap getRmap(String fullPathNameI) throws Exception {
	
	currentThread = Thread.currentThread();
	
	if (!connected) {
	    currentThread = null;
	    throw new Exception(
		"Cannot obtain Rmap; " +
		"not currentely connected to a DataTurbine.");
	}
	
	// JPW 12/21/2001: Instead of just getting the expansion of the
	//                 connected server, request an expansion of the
	//                 child whose path is specified by the argument.
	// NOTE: "..." specifies to get all children in the Rmap hierarchy
	Rmap tempRmap = null;
	if ( (fullPathNameI != null) && (!fullPathNameI.equals("")) ) {
	    String name =
	        new String(fullPathNameI + Rmap.PATHDELIMITER + "...");
	    tempRmap = Rmap.createFromName(name);
	} else {
	    // JPW 02/13/2002: Re-obtain the server name everytime we request
	    //                 an Rmap for the connected server (in case the
	    //                 server name changes).
	    // JPW 01/22/2002: Request Rmap specifically for
	    //                 the connected server.
	    serverName = server.getFullName();
	    String name = serverName + Rmap.PATHDELIMITER + "...";
	    tempRmap = Rmap.createFromName(name);
	}

	// INB 11/20/2002: Mark the leaf node.
	tempRmap.markLeaf();

	Rmap rmap = controller.getRegistered(tempRmap);
	
	// Get rid of all the unnamed stuff in the Rmap hierarchy
	rmap = rmap.toNameHierarchy();
	
	// System.err.println(
	//     "\n\nRBNBDataManager.getRmap():\n" + rmap + "\n\n");
	
	currentThread = null;
	
	return rmap;
	
    }
    
    /**************************************************************************
     * Gets the Server Admin is connected to.
     * <p>
     * Returns null if there is no active connection.
     *
     * @author John P. Wilson
     *
     * @return the Server
     * @since V2.0
     * @version 06/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/08/2001  JPW  Created.
     *
     */
    
    public Server getServer() {
	
	if (!connected) {
	    return null;
	}
	
	return server;
	
    }
    
    /**************************************************************************
     * Gets the name of the server this class has a connection with.
     * <p>
     * Returns null if there is no active connection.
     *
     * @author John P. Wilson
     *
     * @return the connected server name
     * @since V2.0
     * @version 05/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/01/2001  JPW  Created.
     *
     */
    
    public String getServerName() {
	
	if (!connected) {
	    return null;
	}
	
	return serverName;
	
    }
    
    /**************************************************************************
     * Gets the address of the server this class has a connection with.
     * <p>
     * Returns null if there is no active connection.
     *
     * @author John P. Wilson
     *
     * @return the connected server address
     * @since V2.0
     * @version 05/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/01/2001  JPW  Created.
     *
     */
    
    public String getServerAddress() {
	
	if (!connected) {
	    return null;
	}
	
	return serverAddress;
	
    }
    
    /**************************************************************************
     * Gets the Controller used in the current connection.
     * <p>
     * Returns null if there is no active connection.
     *
     * @author John P. Wilson
     *
     * @return the Controller
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/30/2001  JPW  Created.
     *
     */
    
    public Controller getController() {
	
	if (!connected) {
	    return null;
	}
	
	return controller;
	
    }
    
    /**************************************************************************
     * Gets the name of the Controller.
     * <p>
     * Returns null if there is no active connection.
     *
     * @author John P. Wilson
     *
     * @return the name of the Controller
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  JPW  Created.
     *
     */
    
    public String getControllerName() {
	
	if (!connected) {
	    return null;
	}
	
	return controllerName;
	
    }
    
    /**************************************************************************
     * Stop the specified Client object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @exception java.lang.Exception
     *            thrown if there is an error stopping the client
     * @since V2.0
     * @version 04/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/08/2003  INB	If the client is a Source, clear the keep cache
     *			flag. This will ensure that the RBO will actually go
     *			away.
     * 05/29/2001  JPW  Created.
     *
     */
    
    public void stop(Client clientI) throws Exception {
	
	if (!connected) {
	    return;
	}

	if (clientI instanceof Source) {
	    ((Source) clientI).setCkeep(false);
	}
	
	controller.stop(clientI);
	
    }
    
    /**************************************************************************
     * Start a shortcut to another server.
     * <p>
     *
     * @author John P. Wilson
     *
     * @exception java.lang.Exception
     *            thrown if there is an error starting the shortcut
     * @since V2.0
     * @version 02/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2002  JPW  Created.
     *
     */
    
    public void startShortcut(ShortcutData shortcutDataI) throws Exception {
	
	Shortcut shortcut = Server.createShortcut(
	    shortcutDataI.name,
	    shortcutDataI.destinationAddress,
	    shortcutDataI.cost);
	
	controller.start(shortcut);
	
    }
    
    /**************************************************************************
     * Stop the specified Shortcut object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @exception java.lang.Exception
     *            thrown if there is an error stopping the shortcut
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  JPW  Created.
     *
     */
    
    public void stop(Shortcut shortcutI) throws Exception {
	
	if (!connected) {
	    return;
	}
	
	controller.stop(shortcutI);
	
    }
    
    /**************************************************************************
     * Stop the specified Server object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @exception java.lang.Exception
     *            thrown if there is an error stopping the server
     * @since V2.0
     * @version 05/29/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/29/2001  JPW  Created.
     *
     */
    
    public void stop(Server serverI) throws Exception {
	
	if (!connected) {
	    return;
	}
	
	controller.stop(serverI);
	
    }
    
    
    /**************************************************************************
     * Start a mirror.
     * <p>
     * This method starts a mirror between two servers.  The arguments specify
     * where the data is coming from (fromServer, fromSource) and where data is
     * going to (toServer, toSource).  The mirror streams frames with an
     * increment of 1 frame.
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @param fromSourceI  The source on "fromServerI" data is coming from.
     * @param fromServerAddressI  Address of server "fromServerI".
     * @param fromSourceNameI  Name of source "fromSourceI".
     * @param toServerAddressI  Address of server "toServerI".
     * @param toSourceNameI  Name of the new source.
     * @param startFlagI  Specifies start time: oldest or now.
     * @param stopFlagI  Specifies stop time: now or continuous.
     * @param numCacheFramesI  Number of archive frames in the new source;
     *     this value is ignored if bMatchFromSourceI is true.
     * @param numArchiveFramesI  Number of archive frames in the new source;
     *     this value is ignored if bMatchFromSourceI is true.
     * @param archiveModeI  Archiving mode: none, create, or append.
     * @param bMatchFromSourceI  The archive/cache specs of the new source
     *     should be set to match "fromSource".
     * @exception java.lang.Exception
     *            thrown if there is an error starting the mirror
     * @since V2.0
     * @version 08/26/2010
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/26/2010  JPW	Moved the guts of this code to com.rbnb.sapi.Control.
     * 03/01/2002  INB	Reworked because we don't really need as much
     *			information as we were getting.  I've cut this down
     *			to the absolute minimum needed to work.
     * 05/29/2001  JPW  Created.
     *
     */
    
    public void createMirror(
	Server fromServerI,
	String fromServerAddressI,
	Source fromSourceI,
	String fromSourceNameI,
	Server toServerI,
	String toServerAddressI,
	String toSourceNameI,
	int startFlagI,
	int stopFlagI,
	long numCacheFramesI,
	long numArchiveFramesI,
	byte archiveModeI,
	boolean bMatchFromSourceI)
    throws Exception
    {
	
	com.rbnb.sapi.Control.createMirror(
		controller,
		server,
		fromServerI,
		fromServerAddressI,
		fromSourceI,
		fromSourceNameI,
		toServerI,
		toServerAddressI,
		toSourceNameI,
		startFlagI,
		stopFlagI,
		numCacheFramesI,
		numArchiveFramesI,
		archiveModeI,
		bMatchFromSourceI);
	
    }

    /**************************************************************************
     * Start a mirror.
     * <p>
     * This method starts a mirror between two servers.  The arguments specify
     * where the data is coming from (fromServer, fromSource) and where data is
     * going to (toServer, toSource).  The mirror streams by time - in this
     * case, a duration of zero means that the data is copied point by point.
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @param fromSourceI  The source on "fromServerI" data is coming from.
     * @param fromServerAddressI  Address of server "fromServerI".
     * @param fromSourceNameI  Name of source "fromSourceI".
     * @param toServerAddressI  Address of server "toServerI".
     * @param toSourceNameI  Name of the new source.
     * @param startFlagI  Specifies start time: oldest or now.
     * @param stopFlagI  Specifies stop time: now or continuous.
     * @param numCacheFramesI  Number of archive frames in the new source;
     *     this value is ignored if bMatchFromSourceI is true.
     * @param numArchiveFramesI  Number of archive frames in the new source;
     *     this value is ignored if bMatchFromSourceI is true.
     * @param archiveModeI  Archiving mode: none, create, or append.
     * @param bMatchFromSourceI  The archive/cache specs of the new source
     *     should be set to match "fromSource".
     * @param durationI the nominal time span of each data retrieval.  A value
     *			of 0 means copy point by point.
     * @exception java.lang.Exception
     *            thrown if there is an error starting the mirror
     * @since V2.2
     * @version 08/27/2010
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/27/2010  JPW	Moved the guts of this code to com.rbnb.sapi.Control.
     * 11/24/2003  INB	Created from <code>createMirror</code>.
     *
     */
    public void createTimeMirror(
	Server fromServerI,
	String fromServerAddressI,
	Source fromSourceI,
	String fromSourceNameI,
	Server toServerI,
	String toServerAddressI,
	String toSourceNameI,
	int startFlagI,
	int stopFlagI,
	long numCacheFramesI,
	long numArchiveFramesI,
	byte archiveModeI,
	boolean bMatchFromSourceI,
	double durationI)
	throws Exception
    {
	com.rbnb.sapi.Control.createTimeMirror(
		controller,
		server,
		fromServerI,
		fromServerAddressI,
		fromSourceI,
		fromSourceNameI,
		toServerI,
		toServerAddressI,
		toSourceNameI,
		startFlagI,
		stopFlagI,
		numCacheFramesI,
		numArchiveFramesI,
		archiveModeI,
		bMatchFromSourceI,
		durationI);
    }
    
    /**************************************************************************
     * Load an archive on the connected server.
     * <p>
     * This method uses SAPI methods to start a new Source on the Server
     * and have that Source load an existing archive.  A username and password
     * can be provided by the user.
     *
     * @author John P. Wilson
     *
     * @param archiveStrI   Name of the archive to load.
     * @param usernameStrI  Username to use in loading the archive.
     * @param passwordStrI  Password to use in loading the archive.
     *
     * @exception java.lang.Exception
     *            thrown if there is an error loading the archive
     *
     * @since V2.5
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add usernameStrI argument
     * 01/10/2005  JPW	If an exception is thrown, explicitly call
     *			    CloseRBNBConnection()
     * 01/07/2005  JPW	Replace the Username argument with just a
     *                      password string
     * 01/06/2005  JPW	Created
     *
     */
    public void loadArchive(
        String archiveStrI, String usernameStrI, String passwordStrI)
    throws Exception
    {
	
	////////////
	// FIREWALLS
	////////////
	
	if (!connected) {
	    throw new Exception(
		"Cannot load archive; " +
		"not currentely connected to a DataTurbine.");
	}
	
	if ( (archiveStrI == null) || (archiveStrI.equals("")) ) {
	    throw new Exception(
		"The archive name is empty; cannot load archive.");
	}
	
	///////////////////////////////////////////////////////////////
	// Use SAPI methods to load archive and then detach from Server
	///////////////////////////////////////////////////////////////
	
	com.rbnb.sapi.Source tempSource = null;
	
	try {
	    System.err.println("Try loading archive " + archiveStrI);
	    tempSource = new com.rbnb.sapi.Source(100,"load",0);
	    // Allow for an empty password string (as long as it isn't null)
	    if ( (usernameStrI != null)     &&
		 (!usernameStrI.equals("")) &&
		 (passwordStrI != null) )
	    {
		// Use a password to load the archive
		tempSource.OpenRBNBConnection(
		    serverAddress,
		    archiveStrI,
		    usernameStrI,
		    passwordStrI);
	    } else
	    {
		// Don't use any username or password to load the archive
		tempSource.OpenRBNBConnection(serverAddress, archiveStrI);
	    }
	    tempSource.Detach();
	    System.err.println("Loaded archive " + archiveStrI);
	} catch (Exception e) {
	    System.err.println("Load archive failed.");
	    // Close the connection to the RBNB
	    if (tempSource != null) {
	        tempSource.CloseRBNBConnection();
	    }
	    throw e;
	}
	
    }
    
}
