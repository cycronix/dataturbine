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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.rbnb.api.Client;
import com.rbnb.api.Controller;
import com.rbnb.api.PlugIn;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Shortcut;
import com.rbnb.api.Sink;
import com.rbnb.api.Source;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.HostAndPortDialog;
import com.rbnb.utility.RBNBProcessInterface;

/******************************************************************************
 * Top level class for <code>Rmap</code> browser application.
 * <p>
 *
 * @author John P. Wilson
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 06/20/2006
 */

/*
 * Copyright 2001 - 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/20/2006  JPW	Add "-u" command line option for setting userID
 * 01/24/2005  JPW	Add Username back to Admin, so it can be set in the
 *			    Server connection.
 * 01/07/2005  JPW	Remove the use of Username; this wasn't being set in
 *			    the Server anyway and is of minimal use even if
 *			    it was set in the Server.
 * 01/06/2005  JPW	Add LOAD_ARCHIVE action to the run thread.
 * 02/20/2002  JPW	Add processID (used in RBNBProcess.exit()).
 *			Change the structure of the constructors to add an
 *			    RBNBProcessInterface variable.
 * 02/12/2002  JPW	Add START_SHORTCUT action to the run thread.
 * 01/25/2002  JPW	Add TERMINATE action to the run thread.
 * 01/09/2002  JPW	Implement ItemListener to handle the new "Hidden"
 *			   checkbox in the View menu.
 * 05/01/2001  JPW	Created.
 *
 */

public class Admin
    extends JFrame
    implements ActionListener, ItemListener, Runnable, WindowListener
{

    /**
     * Application version.  This is tied to the DataTurbine version number.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
// Version of the application
//# ifdef SERIALVERSION
//+	 public final static String  version = SERIALVERSION;
//# else
         public final static String  version = "Development";
//# endif
    
    /**
     * Name of the server to which Admin is connected.
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
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private String serverAddress = null;
    
    /**
     * Handles interactions with the DataTurbine (including connecting,
     * disconnecting, and data passing).
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private RBNBDataManager rbnbDataManager = null;
    
    /**
     * <code>Rmap</code> being examined viewed in the tree view.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private Rmap rmap = null;
    
    /**
     * Displays <code>Rmap</code> information in a scrolling tree view.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private AdminTreePanel treePanel = null;
    
    /**
     * Is the user Sys Admin?
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private boolean bSysAdmin = false;
    
    /**
     * User's identity.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 01/24/2005
     */
    // JPW 01/24/2005: Add Username back to Admin
    // JPW 01/07/2005: Remove the use of Username
    private Username username = null;
    
    /**
     * Checkbox to hide/display "special" Rmap objects.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 01/09/2002
     */
    private JCheckBoxMenuItem hiddenCB = null;
    
    /**
     * Indicates whether "special" Rmap objects should be shown.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 01/22/2002
     */
    public boolean addHidden = false;
    
    /**
     * Processes actions specified by the user; used to offload the AWT
     * event handling thread.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private Thread actionThread = null;
    
    /**
     * Is actionThread busy?
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    private volatile boolean actionThreadBusy = false;
    
    /**
     * Event queue for actionThread.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    private Vector actionFIFO = new Vector();
    
    /**
     * Argument queue for actionThread.
     * <p>
     * Each queued action in actionFIFO must have a corresponding argument
     * stored in argumentFIFO.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    private Vector argumentFIFO = new Vector();
    
    /**
     * No action needs to be performed by actionThread.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    public static final int NO_ACTION = 0;
    
    /**
     * Constant stored in actionFIFO which specifies to connect to a
     * DataTurbine.
     * <p>
     * The corresponding argument in argumentFIFO is a Vector containing 2
     * arguments: the server name and address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    public static final int CONNECT = 1;
    
    /**
     * Constant stored in actionFIFO which specifies to disconnect from the
     * DataTurbine.
     * <p>
     * The corresponding argument in argumentFIFO is a dummy string (not used
     * in performing the action, simply acts as a placeholder).
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/30/2001
     */
    public static final int DISCONNECT = 2;
    
    /**
     * Constant stored in actionFIFO which specifies to load an archive
     * onto the connected server.
     * <p>
     * The corresponding argument in argumentFIFO is a String containing
     * the name of the archive to attempt to load.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.5
     * @version 01/06/2005
     */
    public static final int LOAD_ARCHIVE = 3;
    
    /**
     * Constant stored in actionFIFO which specifies to prompt the user
     * for a server address and then connect to this server.
     * <p>
     * The corresponding argument in argumentFIFO is a dummy string (not used
     * in performing the action, simply acts as a placeholder).
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    public static final int PROMPT_AND_CONNECT = 4;
    
    /**
     * Constant stored in actionFIFO which specifies to start a shortcut
     * to another server.
     * <p>
     * The corresponding argument in argumentFIFO is an object of type
     * ShortcutData.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 02/12/2002
     */
    public static final int START_SHORTCUT = 5;
    
    /**
     * Constant stored in actionFIFO which specifies to terminate/stop
     * the object specified in the argument.
     * <p>
     * The corresponding argument in argumentFIFO is the Rmap that must
     * be terminated.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    public static final int TERMINATE = 6;
    
    /**
     * Constant stored in actionFIFO which specifies to obtain and display an
     * updated Rmap from the server.
     * <p>
     * The corresponding argument in argumentFIFO is the fully qualified path
     * to the desired Rmap object to update.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see #actionThread
     * @since V2.0
     * @version 05/01/2001
     */
    public static final int UPDATE_RMAP = 7;
    
    /**
     * Used in the calls to RBNBProcess.exit().
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/20/2002
     */
    private RBNBProcessInterface processID = null;
    
    /**************************************************************************
     * Constructor.  Simply call the constructor which does the "real work"
     * of constructing the Admin interface.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @since V2.0
     * @version 02/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2002  JPW  Created.
     *
     */
    
    public Admin(String[] argsI) {
	this(argsI,null);
    }
    
    /**************************************************************************
     * Initialize the administrative interface.
     * <p>
     * If a valid (i.e. non-null and non-empty) server name and address are
     * provided as arguments then save these values and schedule a
     * <code>CONNECT</code> event.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @since V2.0
     * @version 06/20/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2006  JPW  Add "-u" command line option for setting userID
     * 01/24/2005  JPW  Add Username back to Admin
     * 01/07/2005  JPW	Remove the use of Username
     * 02/21/2002  JPW	Set the DO_NOTHING_ON_CLOSE flag so that when the
     *			    user clicks on '[x]' the window doesn't
     *			    automatically get disposed.
     * 02/20/2002  JPW	Instead of passing in the server name and address,
     *			    now just send in the unparsed argument list.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public Admin(String[] argsI, RBNBProcessInterface idI) {
	
	super("Admin");
	
	processID = idI;
	
	// JPW 01/24/2005: Add Username back to Admin
	// JPW 01/07/2005: Remove the use of Username
	if (processID != null) {
	    String userID = processID.getUsername();
	    String password = processID.getPassword();
	    if ( (userID != null) &&
	         (!userID.equals("")) &&
	         (password != null) )
	    {
	        setUsername(userID,password);
	    }
	}
	
	ArgHandler ah = null;
	try {
	    ah = new ArgHandler(argsI);
	} catch (Exception e) {
	    System.out.println
		("Admin: Error parsing command line arguments.");
	    System.out.println("Syntax:");
	    System.out.println
		("    Admin -n <server name> -a <host>:<port> -u <username>");
	    com.rbnb.utility.RBNBProcess.exit(-1);
	}
	
	String name = null;
	String address = null;
	String userID = null;
	String argument = null;
	if ( (argument = ah.getOption('n')) != null) {
	    name = argument;
	}
	if ( (argument = ah.getOption('a')) != null) {
	    address = argument;
	}
	// JPW 06/20/2006: Add setting userID from command line;
	//                 only set the userID if the current username
	//                 object is null
	if ( ((argument = ah.getOption('u')) != null) &&
	     (getUsername(false) == null) )
	{
	    setUsername(argument,"");
	}
	
	createMenus();
	
	// Create and add AdminTreePanel to the JFrame
	treePanel = new AdminTreePanel(this);
	getContentPane().add(treePanel);
	
	pack();
	
	setSize(300,400);
	setVisible(true);
	
	// JPW 02/21/2002: Have my code handle the close operation in the
	//                 windowClosing() method of the registered
	//                 WindowListener object.  This will get around the
	//		   JFrame's default behavior of automatically hiding
	//		   the window when the user clicks on the '[x]'
	//		   button.
	this.setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	
	addWindowListener(this);
	
	// JPW 3/23/2001: Add seperate thread for performing certain actions
	actionThread = new Thread(this);
	actionThread.start();
	
	if ( (address != null) && (!address.equals("")) ) {
	    // Schedule a connect action to be processed by the action thread
	    Vector args = new Vector(2);
	    if ( (name != null) && (!name.equals("")) ) {
		args.addElement(name);
	    }
	    else {
		args.addElement("DTServer");
	    }
	    args.addElement(address);
	    addAction(CONNECT, args);
	}
	
	setFrameTitle();
	
    }
    
    /**************************************************************************
     * Creates menu bar and menus for the GUI.
     * <p>
     *
     * @author John P. Wilson
     *
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
    
    private void createMenus() {
	
	Font font = new Font("Dialog", Font.PLAIN, 12);
	JMenuBar menuBar = new JMenuBar();
	menuBar.setFont(font);
	
	JMenu menu = null;
	JMenuItem menuItem = null;
	
	// Add File menu
	menu = new JMenu("File");
	menu.setFont(font);
	menuItem = new JMenuItem("Open...");
	menuItem.setFont(font);
	menuItem.addActionListener(this);
	menuItem.setEnabled(true);
	menu.add(menuItem);
	menuItem = new JMenuItem("Close");
	menuItem.setFont(font);
	menuItem.addActionListener(this);
	menuItem.setEnabled(true);
	menu.add(menuItem);
	menuItem = new JMenuItem("Exit");
	menuItem.setFont(font);
	menuItem.addActionListener(this);
	menuItem.setEnabled(true);
	menu.add(menuItem);
	menuBar.add(menu);
	
	// Add View menu
	menu = new JMenu("View");
	menu.setFont(font);
	hiddenCB = new JCheckBoxMenuItem("Hidden");
	hiddenCB.setFont(font);
	hiddenCB.addItemListener(this);
	hiddenCB.setEnabled(true);
	hiddenCB.setSelected(addHidden);
	menu.add(hiddenCB);
	menuItem = new JMenuItem("Refresh (F5)");
	menuItem.setFont(font);
	menuItem.addActionListener(this);
	menuItem.setEnabled(true);
	menu.add(menuItem);
	menuBar.add(menu);
	
	setJMenuBar(menuBar);
	
    }
    
    /**************************************************************************
     * Gets the Username object.
     * <p>
     * If username is null and bPromptIfNullI is true, then prompt the user to
     * enter a new Username.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param bPromptIfNullI  Prompt user if username is null?
     * @return the Username identifying the user or null if none is specified
     * @see #username
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add Username back to Admin
     * 01/07/2005  JPW  Remove the use of Username; comment out this method
     * 05/01/2001  JPW  Created.
     *
     */
    
    private synchronized Username getUsername(boolean bPromptIfNullI) {
	
	if ( (username == null) && (bPromptIfNullI) ) {
	    // Prompt the user to enter a new username/password
	    UsernamePasswordDialog dlg =
                new UsernamePasswordDialog(
		    this,
		    true,
		    "",
		    "",
		    true,
		    false,
		    true,
		    "Establish identity");
            dlg.show();
	    String usernameStr = dlg.usernameStr;
	    String passwordStr = dlg.passwordStr;
	    int state = dlg.state;
	    dlg.dispose();
	    if (state != UsernamePasswordDialog.CANCEL) {
		// User hit OK button on dialog; store the username/password
	        setUsername(usernameStr,passwordStr);
	    }
	}
	
	return username;
	
    }
    
    /**************************************************************************
     * Sets the user's identity.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param usernameI  new name to save in the Username object
     * @param passwordI  new password to save in the Username object
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add Username back to Admin
     * 01/07/2005  JPW  Remove the use of Username; comment out this method
     * 05/01/2001  JPW  Created.
     *
     */
    
    private synchronized void setUsername(String usernameI, String passwordI) {
	
	username = new Username();
	username.username = usernameI;
	username.password = passwordI;
	
	// Refresh the title bar
	setFrameTitle();
	
    }
    
    /**************************************************************************
     * Set the title in this frame's title bar.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add Username back to Admin
     * 01/07/2005  JPW  Remove the use of Username
     * 02/14/2002  JPW	Only display the server string (connectionStr)
     *                  if currently connected to a server.
     * 02/13/2002  JPW	Change order of items in the title bar.
     * 05/01/2001  JPW  Created.
     *
     */
    
    private void setFrameTitle() {
        
	// JPW 01/24/2005: Add Username back to Admin
	// JPW 01/07/2005: Remove the use of Username
        String usernameStr = "  User: \"\"";
        Username currentUsername = null;
        if ( (currentUsername = getUsername(false)) != null) {
	    usernameStr =
	        "  User: \"" + currentUsername.username + "\"";
        }
	
	// JPW 02/14/2002: Only display this string if connected to a server
	// JPW 02/13/2002: Remove "Connected to:" from this string
	// JPW 12/21/2001: Add connectionStr
	String connectionStr = "";
	if (rbnbDataManager != null) {
	    // We are connected to a server
	    if (serverAddress != null) {
	        connectionStr = "  \"" + serverAddress + "\"";
	    } else {
	        connectionStr = "  \"N/A\"";
	    }
	}
	
        if (bSysAdmin) {
	    setTitle(
		"rbnbAdmin*  " +
		connectionStr +
		usernameStr +
		"  " +
		version);
        }
        else {
	    setTitle(
		"rbnbAdmin  " +
		connectionStr +
		usernameStr +
		"  " +
		version);
	}
        
    }
    
    /**************************************************************************
     * Add another action for actionThread to process.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param newActionI  new action to add to actionFIFO
     * @param argumentI  argument for the new action; this gets
     *                   added to argumentFIFO
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
    
    public synchronized void addAction(int newActionI, Object argumentI) {
	
	actionFIFO.addElement(new Integer(newActionI));
	if (argumentI == null) {
	    // Just add a dummy string as a placeholder in the FIFO
	    argumentFIFO.addElement(new String("<no arg>"));
	}
	else {
	    argumentFIFO.addElement(argumentI);
	}
	if (!actionThreadBusy) {
	    notifyAll();
	}
	
    }
    
    /**************************************************************************
     * Gets the next action to process from actionFIFO.
     * <p>
     * If there are no more actions to process, returns NO_ACTION.
     * <p>
     * Note that this method doesn't have to be synchronized because it is
     * only called from a synchronized block within run().
     * <p>
     *
     * @author John P. Wilson
     *
     * @return the next action to process
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
    
    private /*synchronized*/ int getNextAction() {
	
	int nextAction = NO_ACTION;
	if (!actionFIFO.isEmpty()) {
	    Integer intObject = (Integer)actionFIFO.elementAt(0);
	    nextAction = intObject.intValue();
	    actionFIFO.removeElementAt(0);
	}
	return nextAction;
	
    }
    
    /**************************************************************************
     * Gets the argument associated with the next action from argumentFIFO.
     * <p>
     * Note that this method doesn't have to be synchronized because it is only
     * called from a synchronized block within run().
     * <p>
     *
     * @author John P. Wilson
     *
     * @return an Object which is the argument to the next action to be
     *         processed
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
    
    private /*synchronized*/ Object getNextActionArgument() {
	
	Object nextActionArgument = null;
	if (!argumentFIFO.isEmpty()) {
	    nextActionArgument = argumentFIFO.elementAt(0);
	    argumentFIFO.removeElementAt(0);
	}
	return nextActionArgument;
	
    }
    
    /**************************************************************************
     * Reads and processes event requests that are in actionFIFO.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add Username back in the PROMPT_AND_CONNECT action.
     *			Add usernameStr to the LOAD_ARCHIVE action.
     * 01/07/2005  JPW	Remove the use of Username in the PROMPT_AND_CONNECT
     *                      action.
     * 01/06/2005  JPW	Add LOAD_ARCHIVE action.
     * 02/13/2002  JPW	Add START_SHORTCUT action.
     * 01/29/2002  JPW  Support the termination of PlugIn objects
     * 01/25/2002  JPW  Add TERMINATE action.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void run() {
	
	int currentAction = NO_ACTION;
	Object currentActionArgument = null;
	
	while (true) {
	    
	    actionThreadBusy = false;
	    
	    synchronized (this) {
		while ( (currentAction = getNextAction()) == NO_ACTION ) {
		    try {
			wait();
		    } catch (InterruptedException e) {}
		}
		
		currentActionArgument = getNextActionArgument();
		
		actionThreadBusy = true;
		
	    } // synchronized (this)
	    
	    switch (currentAction) {
	    
	    case CONNECT:
		// Connect to a DT
		Vector args = (Vector)currentActionArgument;
		if (args.size() != 2) {
		    System.err.println("ERROR with CONNECT arguments");
		}
		String name = (String)args.elementAt(0);
		String address = (String)args.elementAt(1);
		connectAction(name, address);
		break;
	    
	    case DISCONNECT:
		// Disconnect from the Server
		disconnectAction();
		break;
	    
	    case LOAD_ARCHIVE:
		// Load an archive on the connected server
		Vector archiveArgs = (Vector)currentActionArgument;
		if (archiveArgs.size() != 3) {
		    System.err.println("ERROR with LOAD_ARCHIVE arguments");
		}
		// JPW 01/24/2005: Add usernameStr to loadArchive()
		String archiveStr = (String)archiveArgs.elementAt(0);
		String userStr = (String)archiveArgs.elementAt(1);
		String passStr = (String)archiveArgs.elementAt(2);
		try {
		    // JPW 01/24/2005: Add username argument to loadArchive()
	            rbnbDataManager.loadArchive(archiveStr, userStr, passStr);
	            // Schedule an update
		    try { Thread.sleep(1000); } catch (Exception e) {}
		    addAction(UPDATE_RMAP, null);
	        } catch (Exception e) {
		    StringWriter sw = new StringWriter();
		    PrintWriter pw = new PrintWriter(sw, true);
		    e.printStackTrace(pw);
		    StringReader sr = new StringReader(sw.toString());
		    BufferedReader br = new BufferedReader(sr);
		    // See if there was an exception thrown by the Server
		    String exceptionMsg = null;
		    try {
		        while ( (exceptionMsg = br.readLine()) != null ) {
			    if (exceptionMsg.startsWith("Nested exception:")) {
			        // Read the next line; this will contain the
			        // Server exception message we want to display
			        exceptionMsg = br.readLine();
			        break;
			    }
			}
		    } catch (Exception readException) {
			exceptionMsg = null;
		    }
		    if ( (exceptionMsg == null) ||
		         (exceptionMsg.trim().equals("")) )
		    {
			exceptionMsg = e.getMessage();
		    }
	            JOptionPane.showMessageDialog(
		        this,
		        "Error loading archive:\n" + exceptionMsg,
		        "Load Archive Error",
		        JOptionPane.ERROR_MESSAGE);
	        }
		break;
	    
	    case PROMPT_AND_CONNECT:
		// Ask user for address of server to connect to
	        // Derive initial values for the dialog
	        String hostStr = "localhost";
	        int portInt = 3333;
		// Create a local copy of serverAddress for this thread to use
		String addressStr = null;
		if (serverAddress != null) {
		    addressStr = new String(serverAddress);
		}
	        if ( (addressStr != null) &&
		     (!addressStr.equals("")) )
		{
		    int colon = addressStr.lastIndexOf(":");
		    if (colon != -1) {
		        hostStr = addressStr.substring(0,colon);
		        portInt =
			    Integer.parseInt(
				addressStr.substring(colon + 1));
		    }
		}
		// JPW 01/24/2005: Add Username back to Admin
		// JPW 01/07/2005: Remove use of Username
	        Username tempUsername = getUsername(false);
	        String usernameStr = "";
	        String passwordStr = "";
	        if (tempUsername != null) {
		    usernameStr = tempUsername.username;
		    passwordStr = tempUsername.password;
	        }
                HostAndPortDialog hpd =
                    new HostAndPortDialog((Frame)this,
                                          true,
                                          "DataTurbine",
                                          "Specify DataTurbine Connection",
                                          hostStr,
                                          portInt,
                                          true,
				          usernameStr,
				          passwordStr,
				          true,
				          true);
                hpd.show();
                String machine = new String(hpd.machine);
                int port = hpd.port;
	        int state = hpd.state;
		// JPW 01/24/2005: Add Username back to Admin
		// JPW 01/07/2005: Remove use of Username
	        usernameStr = hpd.username;
	        passwordStr = hpd.password;
	        hpd.dispose();
                if (state == HostAndPortDialog.OK) {
		    // JPW 01/24/2005: Add Username back to Admin
		    // JPW 01/07/2005: Remove use of Username
		    setUsername(usernameStr,passwordStr);
		    // Schedule a connect action
		    Vector connectargs = new Vector(2);
	            connectargs.addElement("DTServer");
	            connectargs.addElement(machine + ":" + port);
	            addAction(CONNECT, connectargs);
		}
		break;
	    
	    case START_SHORTCUT:
	        ShortcutData shortData = (ShortcutData)currentActionArgument;
	        try {
	            rbnbDataManager.startShortcut(shortData);
	            // Schedule an update
		    try { Thread.sleep(1000); } catch (Exception e) {}
		    addAction(UPDATE_RMAP, null);
	        } catch (Exception e) {
	            JOptionPane.showMessageDialog(
		        this,
		        "Error starting shortcut:\n" + e.getMessage(),
		        "Shortcut Error",
		        JOptionPane.ERROR_MESSAGE);
	        }
	        break;
	    
	    case TERMINATE:
	        try {
	            // JPW 01/25/2002: Add TERMINATE action
	            if (currentActionArgument instanceof Controller) {
	                rbnbDataManager.stop(
	                    (Controller)currentActionArgument);
	                // Schedule an update
		        try { Thread.sleep(1000); } catch (Exception e) {}
		        addAction(UPDATE_RMAP, null);
		    } else if (currentActionArgument instanceof PlugIn) {
	                rbnbDataManager.stop(
	                    (Client)currentActionArgument);
	                // Schedule an update
		        try { Thread.sleep(1000); } catch (Exception e) {}
		        addAction(UPDATE_RMAP, null);
	            } else if (currentActionArgument instanceof Shortcut) {
	                rbnbDataManager.stop(
	                    (Shortcut)currentActionArgument);
	                // Schedule an update
		        try { Thread.sleep(1000); } catch (Exception e) {}
		        addAction(UPDATE_RMAP, null);
	            } else if (currentActionArgument instanceof Server) {
	        	Server tempServerObj = (Server)currentActionArgument;
	                rbnbDataManager.stop(tempServerObj);
	                // JPW 04/21/2003: Only disconnect if this is the
	                //                 connected server. Otherwise,
	                //                 do a refresh.
	                try {
			    if (tempServerObj.getFullName().equals(
			                 rbnbDataManager.getServerName()))
			    {
				addAction(DISCONNECT, null);
			    }
			    else
			    {
			        addAction(UPDATE_RMAP, null);
			    }
			} catch (Exception exception) {
			  System.err.println(
		          "ERROR obtaining full name from the Server's Rmap.");
			}
	            } else if (currentActionArgument instanceof Sink) {
	                rbnbDataManager.stop(
	                    (Sink)currentActionArgument);
	                // Schedule an update
		        try { Thread.sleep(1000); } catch (Exception e) {}
		        addAction(UPDATE_RMAP, null);
	            } else if (currentActionArgument instanceof Source) {
	                rbnbDataManager.stop(
	                    (Source)currentActionArgument);
	                // Schedule an update
		        try { Thread.sleep(1000); } catch (Exception e) {}
		        addAction(UPDATE_RMAP, null);
	            }
	        } catch (Exception e) {
		    JOptionPane.showMessageDialog(
		        this,
		        "Error terminating object:\n" + e.getMessage(),
		        "Terminate Error",
		        JOptionPane.ERROR_MESSAGE);
	        }
	        break;
	    
	    case UPDATE_RMAP:
		// Obtain and display a new Rmap
		if (currentActionArgument.equals("<no arg>")) {
		    displayRmap(null);
		} else {
		    displayRmap((String)currentActionArgument);
		}
		break;
	    
	    default:
		System.err.println(
		    "ERROR: Unknown command in Admin: " + currentAction);
		break;
	    
	    }
	    
	}
	
    }
    
    /**************************************************************************
     * Action callback method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param event ActionEvent that has been fired
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
    
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	
	if (label.equals("Open...")) {
	    addAction(PROMPT_AND_CONNECT, null);
	}
	else if (label.equals("Close")) {
	    // Schedule a disconnect to be processed by the action thread
	    addAction(DISCONNECT, null);
	}
	else if (label.equals("Exit")) {
	    exitAction();
	}
	else if (label.equals("Refresh (F5)")) {
	    // Schedule an update to be processed by the action thread
	    addAction(UPDATE_RMAP, null);
	}
	
    }
    
    /**************************************************************************
     * Item state changed callback method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param event ItemEvent that has been fired
     * @since V2.0
     * @version 01/09/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2002  JPW  Created.
     *
     */
    
    public void itemStateChanged(ItemEvent event) {
	
	Object item = event.getItem();
	if (item == hiddenCB) {
	    addHidden = ((JCheckBoxMenuItem)item).isSelected();
	    addAction(UPDATE_RMAP, null);
	}
	
    }
    
    /**************************************************************************
     * Connect to the specified DataTurbine.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param serverNameI  name of the server to connect to
     * @param serverAddressI  address of the server to connect to
     * @since V2.0
     * @version 01/24/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2005  JPW  Add Username back to Admin; add username argument to
     *			    the RBNBDataManager constructor
     * 01/07/2005  JPW  Remove the use of Username
     * 05/01/2001  JPW  Created.
     *
     */
    
    private void connectAction(String serverNameI, String serverAddressI) {
	
	boolean bChangeCursor = false;
	if (getCursor().getType() == Cursor.DEFAULT_CURSOR) {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    bChangeCursor = true;
	}
	
	// First, make sure we are disconnected
	disconnectAction();
	
	// JPW 01/24/2005: Add Username back to Admin
	// JPW 01/07/2005: Remove use of Username
	// Second, prompt for a Username if one isn't already set.
	Username currentUsername = getUsername(true);
	if (currentUsername == null) {
	    if (bChangeCursor) {
	        // Change back to the original cursor
	        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    rbnbDataManager = null;
	    rmap = null;
	    JOptionPane.showMessageDialog(
		this,
		"No username entered; connection cancelled.",
		"Connection Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	// Third, make the connection
	// JPW 01/24/2005: Add username to the RBNBDataManager constructor
	rbnbDataManager =
	    new RBNBDataManager(
	        this, serverNameI, serverAddressI, currentUsername);
	try {
	    rbnbDataManager.openConnection();
	} catch (Exception e) {
	    if (bChangeCursor) {
	        // Change back to the original cursor
	        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    rbnbDataManager = null;
	    rmap = null;
	    JOptionPane.showMessageDialog(
		this,
		e.getMessage(),
		"Connection Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	if (rbnbDataManager != null) {
	    // Update serverName and serverAddress
	    serverName = rbnbDataManager.getServerName();
	    serverAddress = rbnbDataManager.getServerAddress();
	    // JPW 12/21/2001: Refresh the title bar
	    setFrameTitle();
	    // Schedule an update to be processed by the action thread
	    addAction(UPDATE_RMAP, null);
	}
	
	if (bChangeCursor) {
	    // Change back to the original cursor
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
    }
    
    /**************************************************************************
     * Disconnects from the DataTurbine and resets the GUI.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/25/2003  JPW  Only call closeConnection() if currentThread is null.
     * 02/15/2002  JPW	Use the new "currentThread" cubbyhole in
     *			    RBNBDataManager; if another thread is currently
     *			    within a method in rbnbDataManager then give it
     *			    some time before calling closeConnection().
     * 02/14/2002  JPW	Reset the frame's title bar after disconnecting.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void disconnectAction() {
	
	boolean bChangeCursor = false;
	if (getCursor().getType() == Cursor.DEFAULT_CURSOR) {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    bChangeCursor = true;
	}
	
	// Reset variables
	rmap = null;
	
	// Reset the tree view
	treePanel.reset(null);
	
	if (rbnbDataManager == null) {
	    if (bChangeCursor) {
	        // Change back to the original cursor
	        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    return;
	}
	
	// JPW 02/15/2002: See if there is a thread currently using a
	//                 synchronized method in RBNBDataManager; if there
	//                 is a current thread using a method in this object
	//                 then wait for some time before calling
	//		   closeConnection().
	if (rbnbDataManager.currentThread != null) {
	    for (int i = 0; i < 9; ++i) {
		try {
		    Thread.currentThread().sleep(500);
		} catch (Exception e) {
		    // Nothing to do.
		}
		if (rbnbDataManager.currentThread == null) {
		    break;
		}
	    }
	    if (rbnbDataManager.currentThread != null) {
		// The Thread in RBNBDataManager won't give up, so we will
		// try to force it to end.
		rbnbDataManager.currentThread.interrupt();
	    }
	}
	
	// JPW 11/25/03: Only call closeConnection() if currentThread is null
	if (rbnbDataManager.currentThread == null) {
	    try {
	        rbnbDataManager.closeConnection();
	    } catch (Exception e) {
	        // Don't print out anything
	    }
	} else {
	    System.err.println(
	        "Connection to the RBNB is busy; close connection failed.");
	}
	
	rbnbDataManager = null;
	
	// JPW 02/14/2002: Reset the frame's title bar
	setFrameTitle();
	
	if (bChangeCursor) {
	    // Change back to the original cursor
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
    }
    
    /**************************************************************************
     * Close down the open connection (if there is one) and then exit the
     * application.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/25/2003  JPW	Change the logic for calling disconnect so that Admin
     *			    doesn't get hung up trying to disconnect from RBNB.
     * 02/21/2002  JPW	Before exiting, clean up some variables and then pop
     *			    down the window (call to setVisible(false))
     *			Once again, change the logic of how to disconnect
     *			    from the Server.
     * 02/20/2002  JPW	Include processID member variable in the
     *			    call to RBNBProcess.exit().
     * 02/15/2002  JPW	Change the logic of how to disconnect from the Server.
     * 05/01/2001  JPW  Created.
     *
     */
    
    private void exitAction() {
	
	System.err.println("Exiting Admin...");
	
	if (rbnbDataManager != null) {
	    InformationSplashScreen infoDlg =
	        new InformationSplashScreen(this,"Exiting Admin...");
	    infoDlg.setVisible(true);
	    
	    // JPW 11/25/2003
	    // There are two problems that can occur trying to disconnect:
	    // 1. RBNB is blocked trying to service another request from Admin
	    //    - In this case the action thread is probably blocked (and
	    //      besides, the RBNB will be blocked here anyway so we wouldn't
	    //      be able to disconnect in either case).
	    // 2. RBNB is blocked trying to service another client
	    //    - Not much we can do about this.
	    // To try to gracefully shut down (and at the same time make sure
	    // we don't get blocked when trying to shut down) no longer call
	    // disconnectAction() directly, but rather use the action thread to
	    // handle the disconnect.  If we haven't disconnected in a bit, just
	    // go ahead and exit.
	    
	    addAction(Admin.DISCONNECT, null);
	    // Check to see if the disconnect occurred (disconnectAction() sets
	    // rbnbDataManager to null, so we can check on this variable)
	    for (int i = 0; i < 9; ++i) {
		try {
		    Thread.sleep(500);
		} catch (Exception e) {
		    // Nothing to do.
		}
		if (rbnbDataManager == null) {
		    break;
		}
	    }
	    
	    infoDlg.setVisible(false);
	}
	
	// JPW 02/21/2002: Clean up a bit and pop down the window
	treePanel.reset(null);
	treePanel = null;
	rbnbDataManager = null;
	rmap = null;
	setVisible(false);
	
	// JPW 02/20/2002: Include processID member variable in the
	//                 call to RBNBProcess.exit()
	com.rbnb.utility.RBNBProcess.exit(0, processID);
    }
    
    /**************************************************************************
     * Obtain and display a new <code>Rmap</code>.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param fullPathNameI  Full path to the object of interest.  If this
     *                       argument is null, then request the full hierarchy
     *                       from the ultimate parent server down.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  JPW	Add fullPathNameI argument.
     * 05/14/2001  JPW	Method is no longer synchronized; aparently,
     *			calling JOptionPane.showMessageDialog(this,...)
     *			in a synchronized block is BAD NEWS.  The app
     *			was hanging up in this case.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void displayRmap(String fullPathNameI) {
	
	boolean bChangeCursor = false;
	if (getCursor().getType() == Cursor.DEFAULT_CURSOR) {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    bChangeCursor = true;
	}
	
	if (rbnbDataManager != null) {
	    try {
		rmap = rbnbDataManager.getRmap(fullPathNameI);
	    } catch (Exception e) {
		if (bChangeCursor) {
		    // Change back to the original cursor
		    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		disconnectAction();
		e.printStackTrace();
		JOptionPane.showMessageDialog(
		    this,
		    e.getMessage(),
		    "Error obtaining Rmap",
		    JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    
	}
	
	try {
	    treePanel.update(fullPathNameI,rmap);
	} catch (Exception e) {
	    if (bChangeCursor) {
		// Change back to the original cursor
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    disconnectAction();
	    e.printStackTrace();
	    JOptionPane.showMessageDialog(
		this,
		e.getMessage(),
		"Error updating tree with new Rmap",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	if (bChangeCursor) {
	    // Change back to the original cursor
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
    }
    
    /**************************************************************************
     * Gets rbnbDataManager.
     * <p>
     *
     * @author John P. Wilson
     *
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
    
    public RBNBDataManager getRBNBDataManager() {
	return rbnbDataManager;
    }
    
    /**************************************************************************
     * Method just returns - no operation.
     * <p>
     * Defined as part of implementing WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowActivated(WindowEvent e) {}
    
    /**************************************************************************
     * Method just returns - no operation.
     * <p>
     * Defined as part of implementing WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowClosed(WindowEvent e) {}
    
    /**************************************************************************
     * User has clicked on the small "x" button in the upper right hand corner
     * of the window; exit the application.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowClosing(WindowEvent e) {
        exitAction();
    }
    
    /**************************************************************************
     * Method just returns - no operation.
     * <p>
     * Defined as part of implementing WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowDeactivated(WindowEvent e) {}
    
    /**************************************************************************
     * Method just returns - no operation.
     * <p>
     * Defined as part of implementing WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowDeiconified(WindowEvent e) {}
    
    /**************************************************************************
     * Method just returns - no operation.
     * <p>
     * Defined as part of implementing WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowIconified(WindowEvent e) {}
    
    /**************************************************************************
     * Method just returns - no operation.
     * <p>
     * Defined as part of implementing WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  WindowEvent that has occurred
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
    
    public void windowOpened(WindowEvent e) {}
    
}
