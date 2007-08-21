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

package com.rbnb.chat;

/*
  *****************************************************************
  ***								***
  ***	Name :	Chat.java	(Simple RBNB chat program)	***
  ***	By   :	Ian Brown					***
  ***		U. Bergstrom					***
  ***	For  :	DataTurbine     				***
  ***	Date :	February, 2000					***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		      	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides a RBNB-based chat program.	***
  ***								***
  ***	Modification History :					***
  ***	   08/04/2003 - INB					***
  ***		Added support for non-string messages.		***
  ***      04/02/2003 - INB					***
  ***		Added -R switch.				***
  ***	   03/18/2003 - INB					***
  ***		For consistency, the chat host name is never	***
  ***		adjusted from what the user specifies. We also	***
  ***		always label it "chat host".			***
  ***	   03/19/2003 - INB					***
  ***		Added abort method.				***
  ***								***
  *****************************************************************
*/
import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.RBNBProcessInterface;
import com.rbnb.utility.Utility;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.Toolkit;

import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class Chat
    implements ActionListener,
	       ItemListener,
	       WindowListener,
	       com.rbnb.api.BuildInterface,
	       com.rbnb.chat.ClientReceiverInterface
{
  private boolean	     isSecureHost = false;
  private boolean	     fancyReceipts = false;
  private java.util.Date     buildDate = null;
  private String	     buildVersion = "V2";

  private volatile String    currentName = "user",
			     currentChatHost = "host-chat",
                             currentGroup = "chat",
                             currentServer = null,
                             currentPassword = null;

  private volatile Host      currentChatHostObject = null;

  private volatile boolean   stopRequested = false,
                             showDate = false,
                             showTime = true,
                             beepOnNew = false,
                             useMilitary = false,
                             useGMT = false;

  private Toolkit defTool = null;

  private volatile com.rbnb.chat.Client mcon = null;

  protected Frame            displayFrame = null;

  protected SidePanel	     sidePanel = null;

  private TextArea           displayArea = null;

  private Panel              displayPanel = null;

  protected TextField        inputArea = null;

  private Button             alertB = null;

  private Thread             listenThread = null;

  private double             startTime = 0.,
			     durTime = 0.;

  private CheckboxMenuItem   chosenFont = null,
		             logging = null,
                             showTimeCMI = null,
                             showDateCMI = null,
                             useMilitaryCMI = null,
                             useGMTCMI = null,
                             useLocalCMI = null;

  private FileOutputStream  logFile = null;
  private PrintStream	    log = null;
  private RBNBProcessInterface processID = null;

    private final static String[] DATA_TYPES = {
	"Unknown",
	"Unknown",
	"Boolean",
	"8-bit Integer",
	"16-bit Integer",
	"32-bit Integer",
	"64-bit Integer",
	"32-bit Float",
	"64-bit Float",
	"String",
	"Byte Array"
    };

    /****************** Constructors ************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	Chat		(Constructor: default)		***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
  public Chat(String server, String chatHost, String group, String name) {

    // Load the build file (provides the date and version).
    com.rbnb.api.BuildFile.loadBuildFile(this);
      
    // set defaults; currentName has no default,
    // as it can be set by MsgConnection
    currentServer = ((server == null) || (server.equals(""))) ?
	"localhost:3333" : new String(server);
    currentChatHost = (chatHost == null) ? "host-chat" : chatHost;
    currentGroup = group;
    currentName = name;

    // set up displayFrame basics
    displayFrame = new Frame();
    displayFrame.addWindowListener((WindowListener) this);
    displayFrame.setLayout(new BorderLayout());
    displayFrame.setMenuBar(createMenus());

    // Create the side panel.
    sidePanel = new SidePanel(this);
    displayFrame.add(sidePanel,"East");

    /* Create the display area in the center of the screen. */
    createDisplay();
    
    /* Create an input area at the bottom of the screen. */
    Panel panel = new Panel(new BorderLayout(10,10));
    panel.setBackground(Color.lightGray);
    displayFrame.add(panel,"South");
    panel.add(new Label("  Message", Label.RIGHT), BorderLayout.WEST);
    inputArea = new TextField(40);
    inputArea.addActionListener(this);
    panel.add(inputArea, BorderLayout.CENTER);
    alertB = new Button("Send Alert");
    alertB.addActionListener(this);
    alertB.setEnabled(false);
    panel.add(alertB, BorderLayout.EAST);
    inputArea.setEnabled(false);

    /* Display the frame. */
    displayFrame.setSize(700,500);
    title();
    displayFrame.show();
    inputArea.requestFocus();

    defTool = Toolkit.getDefaultToolkit();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	Chat		(Constructor: default)		***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
  public Chat(String server, String chatHost, String group, String name, 
	      boolean autoconnect, boolean refresh, boolean promptForPass, 
	      RBNBProcessInterface processIDI) {
	this(server, group, chatHost, name, autoconnect, refresh,
	     promptForPass);
	processID = processIDI;
  }
  
/*
  *****************************************************************
  ***								***
  ***	Name :	Chat		(Constructor: default)		***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
  public Chat(String server, String chatHost, String group, String name, 
	      boolean autoconnect, boolean refresh, 
	      RBNBProcessInterface processIDI) {
	this(server, chatHost, group, name, autoconnect, refresh, false);
	processID = processIDI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	Chat		(Constructor)    		***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
  public Chat(String server, String chatHost, String group, String name, 
	      boolean autoconnect, boolean refresh,
	      boolean promptForPass) {
      this(server, chatHost, group, name);

      // determine whether to initialize a chat session, and
      // whether to refresh the screen.
      if (autoconnect) {
	  if ( promptForPass || (name == null || name.equals("")) ) {
	      openAction();
	  } else {
	      connect();
	  }

	  if (refresh) {
	      refreshAction((long) 3600); // refresh an hour by default
	  }
      }
  }

  /***** Methods to help set-up and maintain GUI appearance *****/

/*
  *****************************************************************
  ***								***
  ***	Name :	createMenus	    (add menus)		        ***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates and adds menus to Chat           ***
  ***								***
  *****************************************************************
*/
  private final MenuBar createMenus() {
    Font mfont = new Font("Dialog", Font.PLAIN, 12);
    MenuItem mi;
    MenuBar mbar = new MenuBar();

    // file menu
    Menu fileM = new Menu("File");
    fileM.setFont(mfont);
    mi = new MenuItem("New");
    mi.setFont(mfont);
    mi.addActionListener(this);
    fileM.add(mi);
    mi = new MenuItem("Open");
    mi.setFont(mfont);
    mi.addActionListener(this);
    fileM.add(mi);
    mi = new MenuItem("Close");
    mi.setFont(mfont);
    mi.addActionListener(this);
    fileM.add(mi);
    mi = new MenuItem("Exit");
    mi.setFont(mfont);
    mi.addActionListener(this);
    fileM.add(mi);
    mbar.add(fileM);

    // view menu
    Menu viewM = new Menu("View");
    viewM.setFont(mfont);
    mi = new MenuItem("Clear");
    mi.setFont(mfont);
    mi.addActionListener(this);
    viewM.add(mi);
    mi = new MenuItem("-");
    viewM.add(mi);
    showDateCMI = new CheckboxMenuItem("Date");
    showDateCMI.setFont(mfont);
    showDateCMI.setState(false);
    showDateCMI.addItemListener(this);
    viewM.add(showDateCMI);
    showTimeCMI = new CheckboxMenuItem("Time");
    showTimeCMI.setFont(mfont);
    showTimeCMI.setState(true);
    showTimeCMI.addItemListener(this);
    viewM.add(showTimeCMI);
    useMilitaryCMI = new CheckboxMenuItem("Military");
    useMilitaryCMI.setFont(mfont);
    useMilitaryCMI.setState(false);
    useMilitaryCMI.addItemListener(this);
    viewM.add(useMilitaryCMI);
    mi = new MenuItem("-");
    viewM.add(mi);
    useGMTCMI = new CheckboxMenuItem("Greenwich Mean Time");
    useGMTCMI.setFont(mfont);
    useGMTCMI.setState(useGMT);
    useGMTCMI.addItemListener(this);
    viewM.add(useGMTCMI);
    useLocalCMI = new CheckboxMenuItem("Local Time");
    useLocalCMI.setFont(mfont);
    useLocalCMI.setState(!useGMT);
    useLocalCMI.addItemListener(this);
    viewM.add(useLocalCMI);

    // font sub-menu, on view menu
    mi = new MenuItem("-");
    viewM.add(mi);
    Menu fontMenu = new Menu("Font Size");
    fontMenu.setFont(mfont);
    viewM.add(fontMenu);
    CheckboxMenuItem cmi;
    cmi = new CheckboxMenuItem(" 8");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    cmi = new CheckboxMenuItem(" 9");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    cmi = new CheckboxMenuItem("10");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    cmi = new CheckboxMenuItem("11");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    cmi = new CheckboxMenuItem("12", true);
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    chosenFont = cmi;   // initailly-selected font
    cmi = new CheckboxMenuItem("14");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    cmi = new CheckboxMenuItem("16");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    cmi = new CheckboxMenuItem("18");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    fontMenu.add(cmi);
    mbar.add(viewM);

    // Refresh menu
    Menu refM = new Menu("Refresh");
    refM.setFont(mfont);
    mi = new MenuItem("10 seconds");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("1 minute");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("10 minutes");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("1 hour");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("10 hours");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("1 day");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("10 days");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mi = new MenuItem("-");
    refM.add(mi);
    mi = new MenuItem("All");
    mi.setFont(mfont);
    mi.addActionListener(this);
    refM.add(mi);
    mbar.add(refM);

    // options menu
    Menu setM = new Menu("Options");
    setM.setFont(mfont);
    /*
    mi = new MenuItem("Groups...");
    mi.setFont(mfont);
    mi.addActionListener(this);
    setM.add(mi);
    mi = new MenuItem("Users...");
    mi.setFont(mfont);
    mi.addActionListener(this);
    setM.add(mi);
    mi = new MenuItem("-");
    setM.add(mi);
    */
    cmi = new CheckboxMenuItem("Beep on New");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    setM.add(cmi);
    cmi = new CheckboxMenuItem("Log");
    cmi.setFont(mfont);
    cmi.addItemListener(this);
    logging = cmi;
    setM.add(cmi);

    mbar.add(setM);

    // help menu
    Menu helpM = new Menu("Help");
    helpM.setFont(mfont);
    mi = new MenuItem("About");
    mi.setFont(mfont);
    mi.addActionListener(this);
    helpM.add(mi);
    mbar.add(helpM);

    return mbar;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	main		(Main method)			***
  ***	By   :	Ian Brown					***
  ***		U. Bergstrom					***
  ***	For  :	FlyScan  					***
  ***	Date :	1998-2001					***
  ***								***
  ***	Copyright 1998, 1999, 2000, 2001, 2003 Creare Inc.	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is the first method executed when the	***
  ***	rbnbChat program is started as an application.		***
  ***								***
  ***	Syntax :						***
  ***	    java com.rbnb.chat.Chat \				***
  ***		[-a [<RBNB host>][:<RBNB server port>]] \	***
  ***		[-f] \						***
  ***		[-u <username>] \			      	***
  ***		[-g <groupname>] \			      	***
  ***		[-h <chathostname>] \				***
  ***	        [-R]						***
  ***								***
  *****************************************************************
*/
  public final static void main(String args[]) {
      boolean     autoconnect = false;
      boolean	  refresh = false;
      boolean     promptForPass = false;
      boolean     fancy = false;
      int	  idx, idx1, idx2;
      String      inputServer = null;
      String	  inputChatHost = "host-chat";
      String      inputGroup = "chat";
      String      inputName = "user";

      // if there are any command line arguments, Chat will automatically
      // initialize a chat session; otherwise not. Exception: the -R switch
      // alone doesn't trigger a connection.
      if (args.length != 0) {
	  autoconnect = true;
      }

      /* Handle the argument list. */
      for (idx = 0; idx < args.length; ++idx) {
	  if (args[idx].charAt(0) != '-') {
	      inputName = args[idx];
	  }

	  /* Determine if there is any except the switch. */
	  if (args[idx].length() == 2) {
	      idx1 = idx + 1;
	      idx2 = 0;
	  } else {
	      idx1 = idx;
	      idx2 = 2;
	  }
	
	  /* Hande the arguments. */
	  switch (args[idx].charAt(1)) {
	    
	  /* -a [<RBNB host>][:<RBNB port>] */
	  case 'a': 
	      inputServer = args[idx1].substring(idx2);
	      break;
	    
	  /* -f */
	  case 'f': 
	      refresh = true;
	      idx1 = idx;
	      break;

  	  /* -g <groupname> */
  	  case 'g':
  	      inputGroup = args[idx1].substring(idx2);
  	      break;

  	  /* -h <chathostname> */
  	  case 'h':
  	      inputChatHost = args[idx1].substring(idx2);
  	      break;

	  /* -u <username> */
	  case 'u':
	      inputName = args[idx1].substring(idx2);
	      break;
	  
	  /* -p */
	  case 'p': 
	      promptForPass = true;
	      idx1 = idx;
	      break;

	  /* -R */
	  case 'R':
	      fancy = true;
	      if (args.length == 1) {
		  autoconnect = false;
	      }
	      break;

	  default:
	      System.err.println("Unrecognized switch: " + args[idx]);
	      System.err.println("\nLegal Usage:");
	      System.err.println("-a [<RBNB host>][:<RBNB port>] :" +
				 "set DataTurbine for connection");
	      System.err.println("-f                             : " +
				 "refresh at start-up");
  	      System.err.println("-g <groupname>                 : set groupname");
  	      System.err.println("-h <chathostname>              : set chathostname");
	      System.err.println("-u <username>                  : " +
				 "set username");
	      System.err.println("-p                             : " +
				 "prompt for password at start-up");
	      System.err.println("-R                             : " +
				 "provides fancy receipts.");
	      com.rbnb.utility.RBNBProcess.exit(-1);
	      break;
	  }
	
	  idx = idx1;
      }
    
      Chat chat = new Chat(inputServer, inputChatHost, inputGroup, inputName, 
			   autoconnect, refresh, promptForPass);
      chat.fancyReceipts = fancy;
  }

    /**
     * Receives notification of an abort by the host side.
     * <p>
     * This method is called whenever a chat <code>Client</code> detects a
     * fatal error when trying to receive messages from the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2003  INB	Created.
     *
     */
    public final void abort() {
	closeAction();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createDisplay                                   ***
  ***	By   :	Ian Brown					***
  ***		U. Bergstrom					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates the area responsible for         ***
  ***   displaying messages                                     ***
  ***								***
  *****************************************************************
*/
  protected void createDisplay() {
      /* Create a FocusListener which will force the displayArea
       * to give up focus whenever it receives it.
       */
      FocusListener fl = new FocusListener() {
	      public void focusGained(FocusEvent feg) {
		  displayArea.transferFocus();
	      }

	      public void focusLost(FocusEvent fel) {}
	  };

      /* Create displayArea. */
      displayArea = new TextArea();
      displayArea.addFocusListener(fl);
      displayArea.setEditable(false);

      /* Create displayPanel and embed displayArea within it.
       * We will use the displayPanel's insets to give a colored
       * border to the displayArea.
       */
      GridBagLayout gbl = new GridBagLayout();
      displayPanel = new Panel(gbl);
      displayPanel.setBackground(Color.lightGray);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = gbc.gridy = 0;
      gbc.fill = GridBagConstraints.BOTH; // fill must be BOTH, weightx and
      gbc.weightx = gbc.weighty = 1;  //weighty 1, for displayArea to size OK.
      gbc.insets = new Insets(5, 5, 5, 5);
      gbl.setConstraints(displayArea, gbc);
      displayPanel.add(displayArea);

      /* Place the displayScreen at the center of the screen. */
      displayFrame.add(displayPanel,"Center");
      setDisplayFont(12);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	title		(display the title)		***
  ***	By   :	Ian Brown					***
  ***		U. Bergstrom					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method displays the title of the session.       ***
  ***								***
  *****************************************************************
*/
  private final void title() {
      String prefix = "rbnbChat " + getBuildVersion();

      if (mcon == null) {
	  displayFrame.setTitle(prefix + ", not connected to any server");
	  displayPanel.setBackground(Color.lightGray);
      } else {
	  displayFrame.setTitle(prefix +
				((currentChatHostObject == null) ?
				 "" :
				 "   *HOST*"));
	  if (isSecure()) {
	      displayPanel.setBackground(Color.red);
	  } else {
	      displayPanel.setBackground(Color.lightGray);
	  }
      }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setCursor                                	***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2000            				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the cursor over the display.        ***
  ***								***
  *****************************************************************
*/
  protected void setCursor(Cursor cur) {
    displayArea.setCursor(cur);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setDisplayFont                                	***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2000            				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the font of the display area.       ***
  ***								***
  *****************************************************************
*/
  protected void setDisplayFont(int pointSize) {
      displayArea.setFont(new Font("Monospaced", 
				   Font.PLAIN,
				   pointSize));
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	enableRefresh()                                     ***
  ***	By   :	U. Bergstrom	        (Creare Inc., Hanover, NH)  ***
  ***	For  :  FlyScan                  			    ***
  ***	Date :	May, 2000                			    ***
  ***								    ***
  ***	Copyright 2000 Creare Inc.	         		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method enables or disables the Refresh menu option. ***
  ***                                                               ***
  *********************************************************************
*/
  private final void enableRefresh(boolean enableIt) {
  }

  /************** Methods to display messages *****************/
/*
  *****************************************************************
  ***								***
  ***	Name :	report                                   	***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method appends the input string to the display. ***
  ***								***
  *****************************************************************
*/
  protected void report(String rep) {
    displayArea.append(rep);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	receive		   (display the message)	***
n  ***	By   :	Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2003					***
  ***								***
  ***	Copyright 2003 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method displays the input message string, along ***
  ***   with the appropriate sender name, server, and date.     ***
  ***								***
  *****************************************************************
*/
  public final void receive(Client clientI,com.rbnb.sapi.ChannelMap messageI) {
      showMessage(clientI,messageI,false);
  }

  public final void refresh(Client clientI,com.rbnb.sapi.ChannelMap messageI) {
      showMessage(clientI,messageI,true);
  }

  private final void showMessage
      (Client clientI,
       com.rbnb.sapi.ChannelMap messageI,
       boolean refreshI)
  {
    String sender;
    String message;
    double[][] times = new double[messageI.NumberOfChannels()][];
    int[] index = new int[messageI.NumberOfChannels()];
    for (int idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
	times[idx] = messageI.GetTimes(idx);
	index[idx] = 0;
    }

    boolean done = false;
    double wtime,
	ctime;
    int cIdx;
    boolean updateUsers = false;
    while (!done) {
	done = true;
	cIdx = -1;
	wtime = Double.MAX_VALUE;
	for (int idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
	    if (index[idx] < times[idx].length) {
		done = false;
		ctime = times[idx][index[idx]];
		if ((cIdx == -1) || (ctime < wtime)) {
		    cIdx = idx;
		    wtime = ctime;
		}
	    }
	}

	if (!done && (cIdx >= 0)) {
	    sender = messageI.GetName(cIdx);
	    sender = sender.substring(clientI.getHostName().length() + 1 +
				      clientI.getChatRoom().length());
	    if (!updateUsers) {
		if (sidePanel.chatUserList == null) {
		    updateUsers = true;
		} else {
		    int lo = 0,
			hi = sidePanel.chatUserList.length - 1,
			idx1;

		    updateUsers = true;
		    for (int idx = (lo + hi)/2;
			 updateUsers && (lo <= hi);
			 idx = (lo + hi)/2) {
			idx1 = sender.compareTo(sidePanel.chatUserList[idx]);
			if (idx1 == 0) {
			    updateUsers = false;
			} else if (idx1 < 0) {
			    hi = idx1 - 1;
			} else {
			    lo = idx1 + 1;
			}
		    }
		}
	    }

	    String say;
	    int count = 1;
	    if (messageI.GetType(cIdx) ==
		com.rbnb.sapi.ChannelMap.TYPE_STRING) {
		say = messageI.GetDataAsString(cIdx)[index[cIdx]];
	    } else {
		count = messageI.GetTimes(cIdx).length;
		say = ("Binary data message - " +
		       count + " " + DATA_TYPES[messageI.GetType(cIdx)] +
		       ((count > 1) ? "s." : "."));
	    }

	    String time = formatDisplayTime
		(com.rbnb.api.Time.since1970(wtime));

	    String serv = null;
	    String s = sender;
	    int idx1 = s.lastIndexOf("/");
	    s = s.substring(idx1+1);
	    serv = "<" + s + ">: ";

	    if (say.startsWith("\n")) {
		say = say.substring(1);
	    }
	    if (say.endsWith("\n")) {
		say = say.substring(0,say.length() - 1);
	    }

	    String disp = time + serv + say + "\n";
	    report(disp);

	    if (log != null) {
		log.print(disp);
		log.flush();
	    }

	    if (!refreshI) {
		if (beepOnNew) {
		    defTool.beep();
		}

		if (say.startsWith("ALERT")) {
		    showAlert(time, serv, say);
		}
	    }

	    index[cIdx] += count;
	}

	if (updateUsers) {
	    sidePanel.updateUsers();
	}
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	formatDisplayTime                        	***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method accepts as input a full time string (in  ***
  ***   local time) and formats it for display in the message   ***
  ***   area.                                                   ***
  ***								***
  ***	Modification History :					***
  ***	   04/04/2003 - INB					***
  ***		Added milliseconds to GMT time.			***
  ***								***
  *****************************************************************
*/
    private String formatDisplayTime(String rawTime) {
	String time = rawTime;

	if (useGMT) {
	    try {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy zzz HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = sdf.parse(rawTime);
		String[] str = Utility.unpack(time, " ");
		time = sdf.format(d);
		String[] str2 = Utility.unpack(time, " ");
		time =
		    str2[0] + " " + str2[1] + " " + str2[2] +
		    str[2].substring(str[2].indexOf("."));
	    } catch (Exception e) {
		return "<unknown> ";
	    }
	}
      
	if (useMilitary) {
	    String str[] = Utility.unpack(time, " ");
	    // str[0] is dd-MMM-yyyy, str[1] is TMZ, str[2] is hh:mm:ss.
	    String d[] = Utility.unpack(str[0], "-");
	    String t[] = Utility.unpack(str[2], ":");
	    
	    // d[0] is dd, d[1] is MMM, d[2] is yyyy.
	    // t[0] is hh, t[1] is mm, t[2] is ss.
	    // want: ddhhmmssZ MMM yy
	    return "<" + d[0] + t[0] + t[1] + t[2] + 
		(useGMT ? "Z " : "L ") +
		d[1] + " " + d[2].substring(2) + "> ";

	} else {
	    if (showTime && showDate) {
		return "<" + time + "> ";
	    } else if (!showTime && showDate) {
		String str[] = Utility.unpack(time, " ");
		return "<" + str[0] + " " + str[1] + "> ";
	    } else if (showTime && !showDate) {
		String str[] = Utility.unpack(time, " ");
		return "<" + str[2] + (useGMT ? " GMT" : "") + "> ";
	    } else {
		return "";
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	showAlert()      				***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2001		        		***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method displays an alert message.               ***
  ***								***
  *****************************************************************
*/
  private void showAlert(String time, String serv, String text) {
    String[] str = new String[] {time + serv, text};

    InfoDialog infoDialog =
      new InfoDialog(displayFrame,
		     true,
		     "Alert!",
		     str);
    // center the dialog inside the parent Frame
    infoDialog.setLocation(Utility.centerRect(infoDialog.getBounds(),
					    displayFrame.getBounds()));

    if (!beepOnNew) {  // make certain we beep!
	defTool.beep();
    }

    infoDialog.show();
    infoDialog.dispose();
  }

  /******* Methods to control the listening Thread *************/
/*
  *****************************************************************
  ***								***
  ***	Name :	listen	    (run the chat)		        ***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method loops on getting and displaying messages.***
  ***   It will sleep briefly if there are no messages          ***
  ***   currently available to get.                             ***
  ***								***
  *****************************************************************
*/
  private final void listen() {
    mcon.start();
    while (!stopRequested) {
	try {
	    Thread.currentThread().sleep(1000);
	} catch (Exception e) {
	    e.printStackTrace();
	    report("An error occurred while reading messages.\n");
	    break;
	}
    }
    mcon.stop();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	startListening	   (run listen())		***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts listen() in listenThread.         ***
  ***								***
  *****************************************************************
*/
  private final synchronized void startListening() {
      if (mcon != null) {
	  Runnable r = new Runnable() {
		  public void run() {
		      mcon.stopped = false;
		      mcon.run();
		  }
	      };
	  
	  listenThread = new Thread(r);
	  listenThread.start();
	  mcon.waitForStart();
      }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	stopListening	(stop running listen())		***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method stops listenThread.                      ***
  ***   NEVER CALL THIS METHOD FROM WITHIN THE LISTENING THREAD!***
  ***   It will have an unpredictable result.                   ***
  ***								***
  *****************************************************************
*/
  private final synchronized void stopListening() {
    // this causes the listenThread to safely die
    stopRequested = true;
    mcon.stop();

    if ((listenThread != null) && (Thread.currentThread() != listenThread)) {
	// Note that without the second test in the above if statement,
	// this method would be completely unsafe to use from within
	// the listening Thread.  If a Thread calls join() on itself,
	// it will remain hung permanently.
	try {
	    listenThread.join();
	} catch (InterruptedException e) {}
    }

    // Note that, if this method is called from within listenThread,
    // listenThread might not actually terminate between the 
    // statements setting stopRequested to true and then to
    // false-- it probably will *not* exit, in fact.
    stopRequested = false;
    mcon.close();
    mcon = null;
  }

  /******* Methods to respond to user input and actions *********/
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  itemStateChanged  (respond to item events)      ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Handle item events from the user.                   ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***       event       The event object describing the event   ***
  ***                   that has occured.                       ***
  ***                                                           ***
  *****************************************************************
*/
  public final synchronized void itemStateChanged(ItemEvent event) {
    CheckboxMenuItem item = (CheckboxMenuItem)event.getSource();
    String label = item.getLabel();
    
    if (label.equals("Date")) {
      if (showDate = showDateCMI.getState()) {
	  useMilitaryCMI.setState(false);
	  useMilitary = false;
      }
      
    } else if (label.equals("Time")) {
      if (showTime = showTimeCMI.getState()) {
	  useMilitaryCMI.setState(false);
	  useMilitary = false;
      }

    } else if (label.equals("Military")) {
	if (useMilitary = useMilitaryCMI.getState()) {
	    showTimeCMI.setState(false);
	    showDateCMI.setState(false);
	    showTime = false;
	    showDate = false;
	}

    } else if (label.equals("Greenwich Mean Time")) {
	if (useGMT = useGMTCMI.getState()) {
	    useLocalCMI.setState(false);
	} else {
	    useLocalCMI.setState(true);
	}

    } else if (label.equals("Local Time")) {
	useGMT = !useLocalCMI.getState();
	if (!useGMT) {
	    useGMTCMI.setState(false);
	} else {
	    useGMTCMI.setState(true);
	}

    // INB 11/29/2000 - added logging toggle.
    } else if (label.equals("Log")) {
      try {
	if (item.getState()) {
	  if (logFile == null) {
	    if (log != null) {
	      log.close();
	      log = null;
	    }
	    logFile = new FileOutputStream("Chat.log",true);
	  }

	  if (log == null) {
	    log = new PrintStream(logFile);
	  }
	} else {
	  if (log != null) {
	    log.close();
	    log = null;
	  }
	  if (logFile != null) {
	    logFile.close();
	    logFile = null;
	  }
	}
      } catch (java.io.IOException e) {
	e.printStackTrace();
      }

      // UCB 08/06/01 - toggle state of beepOnNew
    } else if (label.equals("Beep on New")) {
	beepOnNew = item.getState();

    } else /*if ( (label.equals(" 8")) || (label.equals(" 9")) ||
		(label.equals("10")) || (label.equals("11")) ||
		(label.equals("12")) || (label.equals("14")) ||
		(label.equals("16")) || (label.equals("18")))*/ {
      chosenFont.setState(false);
      chosenFont = item;
      int point = 12;
      try {
	point = Integer.parseInt(label.trim());
      } catch (NumberFormatException e) {}
      setDisplayFont(point);
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  actionPerformed  (respond to events)            ***
  ***   By   :  U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***       Handle actions from the user.                       ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***       event       The event object describing the event   ***
  ***                   that has occured.                       ***
  ***                                                           ***
  *****************************************************************
*/
  public final synchronized void actionPerformed(ActionEvent event) {

    if (event.getSource() instanceof TextField) {
      String sendStr = inputArea.getText().trim() + "\n";
      if ( (sendStr != null) &&
	   !sendStr.equals("") &&
	   !sendStr.equals("\n") ) {
	try {
	  isSecureHost = mcon.send(sendStr);
	} catch (Exception e) {
      	  e.printStackTrace();
	  report("An error occurred while sending a message.\n");
	}
	inputArea.setText("");
      }

    } else if (event.getSource() instanceof Button) {
	// Send Alert button
	String sendStr = "ALERT: " + inputArea.getText().trim() + "\n";
	if ( (sendStr != null) &&
	     !sendStr.equals("") &&
	     !sendStr.equals("ALERT: \n")) {
	    try {
		isSecureHost = mcon.send(sendStr);
	    } catch (Exception e) {
		e.printStackTrace();
		report("An error occurred while sending a message.\n");
	    }
	    inputArea.setText("");
	}

    } else { // assume it is a menu item
      String label = event.getActionCommand();

      if (label.equals("Open")) {
	openAction();

      } else if (label.equals("New")) {
	newAction();

      } else if (label.equals("Close")) {
	// disconnect the refreshConnection only when the "Close"
	// button has actually been pushed; much of the time, Chat
	// uses CloseAction to close and clean up mcon.
	boolean doClose = true;
	if (currentChatHostObject != null) {
	    ConfirmDialog cfd = new ConfirmDialog
		(displayFrame,
		 true,
		 "Shutdown " + currentChatHostObject.getHostName(),
		 ("Do you really want to shut down the " +
		  currentChatHostObject.getHostName() +
		  " chat host server?"),
		 ConfirmDialog.CENTER_ALIGNMENT);
	    cfd.show();
	    try {
		doClose = cfd.confirmed;
	    } finally {
		cfd.dispose();
	    }
	}
	if (doClose) {
	    enableRefresh(false);
	    closeAction();
	}

      } else if (label.equals("Exit")) {
	  exitAction();

      } else if (label.equals("Clear")) {
	clearAction();

      } else if (label.equals("All")) {
	refreshAllAction();
      } else if (label.equals("10 seconds")) {
	refreshAction((long) 10);
      } else if (label.equals("1 minute")) {
	refreshAction((long) 60);
      } else if (label.equals("10 minutes")) {
	refreshAction((long) 600);
      } else if (label.equals("1 hour")) {
	refreshAction((long) 3600);
      } else if (label.equals("10 hours")) {
	refreshAction((long) 36000);
      } else if (label.equals("1 day")) {
	refreshAction((long) 86400);
      } else if (label.equals("10 days")) {
	refreshAction((long) 864000);

      } else if (label.equals("About")) {
	Frame frame = new Frame("About rbnbChat");
	Panel panel = new Panel();
	
	frame.add(panel);
	frame.addWindowListener((WindowListener) this);
	
	panel.setLayout(new BorderLayout());
	
	panel.add(new Label("rbnbChat " + getBuildVersion() +
			    " - Communicate via RBNB"),"North");
	panel.add(new Label("Copyright 2001, 2003 Creare Inc."),"Center");
	panel.add(new Label("All Rights Reserved"),"South");
	
	frame.setSize(300,100);
	Rectangle bounds = displayFrame.getBounds();
	Rectangle abounds = frame.getBounds();
	frame.setLocation(bounds.x + (bounds.width - abounds.width)/ 2,
		    bounds.y + (bounds.height - abounds.height)/2);

	frame.addNotify();
	frame.validate();
	frame.show();
      }
    }
  }

  /************** Methods to make connections ****************/
/*
  *********************************************************************
  ***								    ***
  ***	Name :	connect	    (initailizes a session)                 ***
  ***	By   :	U. Bergstrom					    ***
  ***		Ian Brown					    ***
  ***	For  :  DataTurbine     				    ***
  ***	Date :	September 2001		                 	    ***
  ***								    ***
  ***	Copyright 2001, 2003 Creare Inc.	        	    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method initializes a chat session by initializing   ***
  ***   mcon and performing some "housekeeping".                    ***
  ***								    ***
  ***	Modification History :					    ***
  ***	   03/18/2003 - INB					    ***
  ***		Do not update the current chat host from the client ***
  ***		connection name.				    ***
  ***								    ***
  *********************************************************************
*/
  protected void connect() {
    boolean wasListening = (mcon != null);

    // if mcon is null, establish a new connection; otherwise, 
    // add the new group
    displayFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    try {
	if (mcon == null) {
	    mcon = new Client();
	    mcon.setHostName(currentChatHost);
	    mcon.setChatRoom(currentGroup);
	    mcon.setCRI(this);
	    mcon.open(currentServer,currentName,currentPassword);
//	    currentChatHost = mcon.getHostName();
	    currentName = mcon.getName();

	} else {
	    isSecureHost = mcon.send(mcon.getName() + " has left this group.");

	    // add the new group
	    synchronized (mcon) {
		mcon.setHostName(currentChatHost);
//		currentChatHost = mcon.getHostName();
		mcon.setChatRoom(currentGroup);
	    }
	}
/*
	if (currentChatHost.charAt(0) != '/') {
	    currentChatHost = mcon.getServerName() + "/" + currentChatHost;
	}
*/
	isSecureHost = mcon.send(currentName + " has entered this group.\n");
	Thread.currentThread().sleep(1000);
	clearAction();
	refreshAction(10);

    } catch (Exception e) {
      stopListening();
      e.printStackTrace();
      report("An error occurred while trying to connect.\n");
      displayFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      
      String txt = e.getMessage();
      if ((txt != null) && (txt.indexOf("maximum number of clients") != -1)) {
	report("The maximum number of clients supported by this RBNB license has been reached!\n");
      }
      return;
    }
    
    title();
    sidePanel.updateHost();
    sidePanel.updateGroups();
    sidePanel.updateUsers();
    alertB.setEnabled(true);
    inputArea.setEnabled(true);
    if (!wasListening) {
	startListening();
    }
    displayFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /************** Methods to take actions ****************/
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  openAction  (respond to Open event)             ***
  ***   By   :  U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***      respond to an "Open" event.				***
  ***                                                           ***
  *****************************************************************
*/
  private final void openAction() {

    // display cod
    String oldChatHost = currentChatHost;
    ChatOpenDlg cod = new ChatOpenDlg(displayFrame,
				      currentServer,
				      currentChatHost,
				      currentName,
				      currentGroup);
    cod.show();

    try {
	if (cod.state != ChatOpenDlg.CANCEL) {
	    currentServer = cod.serverAddress;
	    currentChatHost = cod.chatHost;
	    currentName = cod.username;
	    currentGroup = cod.groupname;
	    currentPassword = cod.password;

	    if ((currentChatHostObject != null) &&
		!currentChatHost.equals(oldChatHost)) {
		ConfirmDialog cfd = new ConfirmDialog
		    (displayFrame,
		     true,
		     "Shutdown " + currentChatHostObject.getHostName(),
		     ("Do you really want to shut down the " +
		      currentChatHostObject.getHostName() +
		      " chat host server?"),
		     ConfirmDialog.CENTER_ALIGNMENT);
		cfd.show();
		try {
		    if (!cfd.confirmed) {
			return;
		    }
		} finally {
		    cfd.dispose();
		}
	    }

	    // disconnect
	    Host oldHostObject = currentChatHostObject;
	    currentChatHostObject = null;
	    closeAction();
	    currentChatHostObject = oldHostObject;

	    // reconnect
	    connect();
	}
    } finally {
	cod.dispose();
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  newAction	  (respond to New event)        ***
  ***   By   :  U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***      respond to an "New" event.				***
  ***								***
  ***	Modification History :					***
  ***	   03/18/2003 - INB					***
  ***		Do not change the chat host name.		***
  ***	   04/02/2003 - INB					***
  ***		Handle fancy receipts.				***
  ***                                                           ***
  *****************************************************************
*/
  private final void newAction() {

    // display cnd
    int lio;
    String host = currentChatHost;
    if ((lio = host.lastIndexOf("/")) != -1) {
	host = host.substring(lio + 1);
    }
    ChatNewDlg cnd = new ChatNewDlg(displayFrame,
				    currentServer,
				    host,
				    currentName,
				    "chat",
				    1000,
				    0);
    cnd.show();

    if (cnd.state == ChatNewDlg.CANCEL) {
      cnd.dispose();
      return;
    } else {
      currentServer = cnd.serverAddress;
      currentChatHost = cnd.chatHost;
      currentName = cnd.username;
      currentGroup = cnd.groupname;
      currentPassword = cnd.password;
      long cache = cnd.cache;
      long archive = cnd.archive;
      cnd.dispose();

      if (currentChatHostObject != null) {
	  ConfirmDialog cfd = new ConfirmDialog
	      (displayFrame,
	       true,
	       "Shutdown " + currentChatHostObject.getHostName(),
	       ("Do you really want to shut down the " +
		currentChatHostObject.getHostName() +
		" chat host server?"),
	       ConfirmDialog.CENTER_ALIGNMENT);
	  cfd.show();
	  try {
	      if (!cfd.confirmed) {
		  return;
	      }
	  } finally {
	      cfd.dispose();
	  }
      }

      // disconnect
      closeAction();

      // launch the host.
      try {
	  currentChatHostObject = new Host(currentServer,
					   currentChatHost,
					   currentPassword,
					   cache,
					   ((archive == 0) ?
					    "None" :
					    "Append"),
					   archive);
	  currentChatHostObject.setFancyReceipts(fancyReceipts);
	  currentChatHostObject.start();

	  if (currentChatHostObject.getHostName() != null) {
	      // reconnect
	      connect();
	  }
      } catch (Exception e) {
	  e.printStackTrace();
	  report("An error occurred while trying to connect.\n");
	  displayFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	  setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      
	  String txt = e.getMessage();
	  if ((txt != null) && (txt.indexOf("maximum number of clients") != -1)) {
	      report("The maximum number of clients supported by this RBNB license has been reached!\n");
	  }
	  return;
      }
    
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  closeAction  (respond to Close event)           ***
  ***   By   :  U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***      respond to a "Close" event.				***
  ***								***
  ***	Modification History :					***
  ***	   03/18/2003 - INB					***
  ***		Update all the side panel fields.		***
  ***                                                           ***
  *****************************************************************
*/
  private final void closeAction() {
    if (mcon != null) {
      try {
	  isSecureHost = mcon.send(mcon.getName() + " has left this group.");
      } catch (java.lang.Exception e) {
      }
      stopListening();
      mcon = null;
      inputArea.setEnabled(false);
      alertB.setEnabled(false);
      clearAction();
      title();
      sidePanel.updateHost();
      sidePanel.updateGroups();
      sidePanel.updateUsers();
    }
    if (currentChatHostObject != null) {
      currentChatHostObject.stop();
      currentChatHostObject = null;
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  exitAction  (respond to Exit event)             ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***		Ian Brown					***
  ***	For  :	E-scan		        			***
  ***	Date :	November, 2000  				***
  ***								***
  ***	Copyright 2000, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***      Respond to Exit command.                             ***
  ***                                                           ***
  *****************************************************************
*/
    public void exitAction() {
	if (currentChatHostObject != null) {
	    ConfirmDialog cfd = new ConfirmDialog
		(displayFrame,
		 true,
		 "Shutdown " + currentChatHostObject.getHostName(),
		 ("Do you really want to shut down the " +
		  currentChatHostObject.getHostName() +
		  " chat host server?"),
		 ConfirmDialog.CENTER_ALIGNMENT);
	    cfd.show();
	    try {
		if (!cfd.confirmed) {
		    return;
		}
	    } finally {
		cfd.dispose();
	    }
	}

	
        if (displayFrame!=null) {
	    displayFrame.setVisible(false);
        }
	closeAction();

	if (displayFrame!=null) {
	    displayFrame.dispose();
	    displayFrame = null;
	}
	
        //EMF 11/24/00: switch to RBNBProcess exit
        com.rbnb.utility.RBNBProcess.exit(0,processID);
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  clearAction  (respond to Clear event)           ***
  ***   By   :  U. Bergstrom      (Creare Inc., Hanover, NH)    ***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2000       				***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***      respond to a "Clear" event by clearing the display   ***
  ***                                                           ***
  *****************************************************************
*/
  protected void clearAction() {
    displayArea.setText("");
    inputArea.setText("");
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	refreshAllAction()   (Refresh the display area)	    ***
  ***	By   :	U. Bergstrom	        (Creare Inc., Hanover, NH)  ***
  ***	For  :  E-Scan           				    ***
  ***	Date :	May 2001			                    ***
  ***								    ***
  ***	Copyright 2001 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	  This method refreshes the display area with all available ***
  ***   frames of data.                                             ***
  ***                                                               ***
  *********************************************************************
*/
    protected void refreshAllAction() {
	refreshAction(0);
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	refreshAction()	     (Refresh the display area)	    ***
  ***	By   :	Ian Brown/U. Bergstrom	(Creare Inc., Hanover, NH)  ***
  ***	For  :  FlyScan/DataTurbine				    ***
  ***	Date :	January, 1999/February, 2000			    ***
  ***								    ***
  ***	Copyright 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method refreshes the display area by:		    ***
  ***								    ***
  ***		o - create a new temporary conneciton to the RBNB   ***
  ***		o - use request/response mode to get all of the	    ***
  ***		    data for all of the Chat channels		    ***
  ***	        o - close the connection			    ***
  ***		o - clear the display				    ***
  ***		o - add the messages received			    ***
  ***                                                               ***
  *********************************************************************
*/
  protected void refreshAction(long dur) {
    if (mcon == null) {
	return;
    }

    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    try {
	Thread.sleep(10); // try to permit cursor to set
    } catch (InterruptedException e) {}

    clearAction();
    try {
	if (dur == 0) {
	    mcon.refresh(Double.MAX_VALUE);
	} else {
	    mcon.refresh(dur);
	}
	sidePanel.updateUsers();
    } catch (com.rbnb.sapi.SAPIException e) {
	e.printStackTrace();
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
/****************** Utility Methods ********************/
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  isSecure                                        ***
  ***   By   :  U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2002   				***
  ***								***
  ***	Copyright 2002, 2003 Creare Inc.			***
  ***	All Rights Reserved					***
  ***                                                           ***
  ***   Description :                                           ***
  ***      Returns true if this Chat is running in secure mode. ***
  ***                                                           ***
  *****************************************************************
*/
    public boolean isSecure() {
	return ((currentPassword != null) && 
		!currentPassword.equals("") &&
		isSecureHost);
    }

  /////////////////////////////////////////////////////////////////////////
  //
  //  Methods to implement the WindowListener interface
  //
  /////////////////////////////////////////////////////////////////////////
  
  public void windowActivated(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowClosing(WindowEvent e) {
    if ((Frame) e.getWindow() == displayFrame) {
	exitAction();
    } else if ((Frame) e.getWindow() != displayFrame) {
	((Frame) e.getWindow()).dispose();
    }
  }
  public void windowDeactivated(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}


    /**
     * Gets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build date.
     * @see #setBuildDate(java.util.Date)
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2003  INB	Created.
     *
     */
    public final java.util.Date getBuildDate() {
	return (buildDate);
    }

    /**
     * Gets the <bold>RBNB</bold> build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build version string.
     * @see #setBuildVersion(String)
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2003  INB	Created.
     *
     */
    public final String getBuildVersion() {
	return (buildVersion);
    }

    /**
     * Sets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2003  INB	Created.
     *
     */
    public final void setBuildDate(java.util.Date buildDateI) {
	buildDate = buildDateI;
    }

    /**
     * Sets the <bold>RBNB</bold> build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version string.
     * @see #getBuildVersion()
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2003  INB	Created.
     *
     */
    public final void setBuildVersion(String buildVersionI) {
	buildVersion = buildVersionI;
    }

    /**
     * Side panel class.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/18/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2003  INB	Don't act on cancels.
     * 01/16/2003  INB	Created.
     *
     */
    private final class SidePanel
	extends Panel
	implements ItemListener,
		   MouseListener
    {

	/**
	 * the groups choice pulldown.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */
	private Choice chatGroups;

	/**
	 * the chat host text field.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */
	private TextField chatHost;

	/**
	 * the current user.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	private Label chatUser;

	/**
	 * the current list of users.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/21/2003
	 */
	public String[] chatUserList = null;

	/**
	 * the users list.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */
	private TextArea chatUsers;

	/**
	 * our parent chat.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	private Chat parent = null;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param parentI our parent chat.
	 * @since V2.0
	 * @version 03/18/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 03/18/2003  INB	Label the chat host "chat host".
	 * 01/16/2003  INB	Created.
	 *
	 */
	SidePanel(Chat parentI) {
	    super();
	    parent = parentI;
	    Font font = new Font("Dialog",Font.PLAIN,12);
	    setBackground(Color.lightGray);
	    setFont(font);

	    GridBagLayout gbl = new GridBagLayout();
	    setLayout(gbl);

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.anchor = GridBagConstraints.SOUTH;

	    int row = 0,
		col = 0;
	    gbc.weightx = 100;
	    gbc.weighty = 100;

	    Label label = new Label("Chat Host");
	    Utility.add(this,label,gbl,gbc,col,row++,1,1);
	    chatHost = new TextField(20);
	    chatHost.setFont(font);
	    chatHost.setEditable(false);
	    updateHost();
	    Utility.add(this,chatHost,gbl,gbc,col,row++,1,1);
	    label = new Label("");
	    Utility.add(this,label,gbl,gbc,col,row++,1,1);

	    label = new Label("Group");
	    Utility.add(this,label,gbl,gbc,col,row++,1,1);
	    chatGroups = new Choice();
	    chatGroups.addMouseListener(this);
	    chatGroups.addItemListener(this);
	    updateGroups();
	    Utility.add(this,chatGroups,gbl,gbc,col,row++,1,1);
	    label = new Label("");
	    Utility.add(this,label,gbl,gbc,col,row++,1,1);

	    label = new Label("Users");
	    Utility.add(this,label,gbl,gbc,col,row++,1,1);
	    chatUser = new Label("");
	    Utility.add(this,chatUser,gbl,gbc,col,row++,1,1);
	    chatUsers = new TextArea(10,20);
	    chatUsers.addMouseListener(this);
	    chatUsers.setFont(font);
	    chatUsers.setEditable(false);
	    Utility.add(this,chatUsers,gbl,gbc,col,row,1,11);
	    row += 11;
	    updateUsers();
	}

	/**
	 * Handles item state changed event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the item state changed event.
	 * @since V2.0
	 * @version 03/18/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 03/18/2003  INB	On cancel or blank group name, return
	 *			immediately.
	 * 01/16/2003  INB	Created.
	 *
	 */
	public final void itemStateChanged(ItemEvent e) {
	    String group = (String) e.getItem();

	    if (group.equals("<NEW>")) {
		NewGroupDialog ngd = new NewGroupDialog(parent.displayFrame);
		ngd.show();

		if (ngd.state == ngd.CANCEL) {
		    group = currentGroup;
		    ngd.dispose();
		    return;
		} else {
		    group = ngd.groupName;
		    ngd.dispose();
		}
	    }
		
	    if ((group != null) &&
		!group.equals("") &&
		((currentGroup == null) ||
		 ((group != currentGroup) &&
		 !group.equals(currentGroup)))) {
		currentGroup = group;
		connect();
	    }
	}

	/**
	 * Handles mouse clicked event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the mouse clicked event.
	 * @since V2.0
	 * @version 01/16/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	public final void mouseClicked(MouseEvent eventI) {
	}

	/**
	 * Handles mouse entered event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the mouse entered event.
	 * @since V2.0
	 * @version 01/16/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	public final void mouseEntered(MouseEvent eventI) {
	}

	/**
	 * Handles mouse exited event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the mouse exited event.
	 * @since V2.0
	 * @version 01/16/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	public final void mouseExited(MouseEvent eventI) {
	}

	/**
	 * Handles mouse pressed event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the mouse pressed event.
	 * @since V2.0
	 * @version 01/16/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	public final void mousePressed(MouseEvent eventI) {
	    if (eventI.getSource() == chatGroups) {
		updateGroups();
	    } else if (eventI.getSource() == chatUsers) {
		updateUsers();
	    }
	}

	/**
	 * Handles mouse released event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the mouse released event.
	 * @since V2.0
	 * @version 01/16/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	public final void mouseReleased(MouseEvent eventI) {
	}

	/**
	 * Updates the list of groups.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	final void updateGroups() {
	    String[] list = new String[chatGroups.getItemCount()];
	    for (int idx = 0; idx < list.length; ++idx) {
		list[idx] = chatGroups.getItem(idx);
	    }
	    String[] list2;
	    if (mcon == null) {
		list2 = new String[1];
		list2[0] = "";
	    } else {
		try {
		    String[] rooms = mcon.receiveRooms();
		    list2 = new String[rooms.length + 1];
		    System.arraycopy(rooms,0,list2,0,rooms.length);
		    list2[rooms.length] = "<NEW>";
		} catch (Exception e) {
		    list2 = new String[1];
		    list2[0] = "";
		}
	    }
	    int adjust = 1;
	    for (int idx = 0, idx2 = 0;
		 ((idx < list.length) || (idx2 < list2.length));
		 ) {
		if (idx >= list.length) {
		    chatGroups.add(list2[idx2++]);
		} else if (idx2 >= list2.length) {
		    chatGroups.remove(list[idx++]);
		} else {
		    int cmprd = list[idx].compareTo(list2[idx2]);

		    if (cmprd == 0) {
			++idx;
			++idx2;
		    } else if (cmprd < 0) {
			chatGroups.remove(list[idx++]);
			--adjust;
		    } else {
			chatGroups.insert(list2[idx2++],idx + adjust);
			++adjust;
		    }
		}
	    }
	    if (currentGroup != null) {
		chatGroups.select(currentGroup);
	    }
	}

	/**
	 * Updates the host.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	final void updateHost() {
	    if (mcon == null) {
		chatHost.setText("");
	    } else {
		chatHost.setText(currentChatHost);
	    }
	}

	/**
	 * Updates the list of users.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/21/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/16/2003  INB	Created.
	 *
	 */
	final void updateUsers() {
	    String cUser = "",
		value = "";
	    if (mcon != null) {
		cUser = currentName;
		try {
		    chatUserList = mcon.receiveUsers();
		    for (int idx = 0;
			 idx < chatUserList.length;
			 ++idx) {
			if (!chatUserList[idx].equals(currentName)) {
			    value += chatUserList[idx] + "\n";
			}
		    }
		} catch (Exception e) {
		}
	    }
	    chatUser.setText(cUser);
	    chatUsers.setText(value);
	}
    }

    /**
     * New group dialog.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/18/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2003  INB	Don't act on blank group name selections.
     * 01/17/2003  INB	Created.
     *
     */
    private final class NewGroupDialog
	extends Dialog
	implements ActionListener,
		   WindowListener
    {

	/**
	 * dialog cancelled.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	public final static int CANCEL = 0;

	/**
	 * the group name.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	public String groupName = null;

	/**
	 * the new group field.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	private TextField newGroup = null;

	/**
	 * dialog accepted.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	public final static int OK = 1;

	/**
	 * the state.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
	public int state = CANCEL;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param parentI our parent class.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	NewGroupDialog(Frame parentI) {
	    super(parentI,true);
	    setTitle("Create New Group");
	    Font font = new Font("Dialog",Font.PLAIN,12);
	    setBackground(Color.lightGray);
	    setFont(font);

	    GridBagLayout gbl = new GridBagLayout();
	    setLayout(gbl);

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx =
		gbc.weighty = 100;

	    Label label;
	    int row = 0,
		col = 0;

	    label = new Label("New Group:");
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.insets = new Insets(15,15,0,5);
	    Utility.add(this,label,gbl,gbc,col++,row,1,1);

	    newGroup = new TextField(32);
	    newGroup.setText(currentGroup);
	    newGroup.setFont(font);
	    newGroup.setEnabled(true);
	    newGroup.addActionListener(this);
	    gbc.insets = new Insets(15,0,0,15);
	    Utility.add(this,newGroup,gbl,gbc,col,row++,1,1);
	    col = 0;

	    Panel buttonPanel = new Panel(new GridLayout(1,2,15,5));
	    Button okButton = new Button("OK"),
		cancelButton = new Button("Cancel");
	    buttonPanel.add(okButton);
	    okButton.addActionListener(this);
	    buttonPanel.add(cancelButton);
	    cancelButton.addActionListener(this);
	    gbc.anchor = GridBagConstraints.CENTER;
	    gbc.insets = new Insets(15,15,15,15);
	    Utility.add(this,buttonPanel,gbl,gbc,col,row++,2,1);

	    pack();
	    setResizable(false);
	    addWindowListener(this);

	    setLocation(Utility.centerRect(getBounds(),parentI.getBounds()));
	}

	/**
	 * Handle an action event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the action event.
	 * @since V2.0
	 * @version 04/09/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/09/2003  INB	Cancel if the group name contains a slash.
	 * 03/18/2003  INB	Cancel if the group name is blank.
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void actionPerformed(ActionEvent eventI) {
	    if (eventI.getActionCommand().equals("Cancel")) {
		state = CANCEL;

	    } else if (eventI.getActionCommand().equals("OK") ||
		       (eventI.getSource() == newGroup)) {
		groupName = newGroup.getText().trim();
		if ((groupName == currentGroup) ||
		    (groupName.indexOf("/") != -1) ||
		    groupName.equals("")) {
		    groupName = currentGroup;
		    state = CANCEL;
		} else {
		    state = OK;
		}
	    } else {
		state = CANCEL;
	    }

	    setVisible(false);
	}

	/**
	 * Handle window activated event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowActivated(WindowEvent eventI) {
	}

	/**
	 * Handle window closed event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowClosed(WindowEvent eventI) {
	}

	/**
	 * Handle window closing event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowClosing(WindowEvent eventI) {
	    state = CANCEL;
	    setVisible(false);
	}

	/**
	 * Handle window deactivated event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowDeactivated(WindowEvent eventI) {
	}

	/**
	 * Handle window deiconified event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowDeiconified(WindowEvent eventI) {
	}

	/**
	 * Handle window iconified event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowIconified(WindowEvent eventI) {
	}

	/**
	 * Handle window opened event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param eventI the window event.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/17/2003  INB	Created.
	 *
	 */
	public final void windowOpened(WindowEvent eventI) {
	}
    }
} // Chat
