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


package com.rbnb.udpcaster;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.Utility;

/******************************************************************************
 * Subscribe to an RBNB channel and send the data, one frame at a time, to
 * the given UDP address.
 * <p>
 *
 * @author John P. Wilson
 *
 * @version 11/04/2010
 */

/*
 * Copyright 2005 - 2008 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/04/2010  JPW	Add bIgnoreSendErrors; if this is true, then we ignore
 *			errors from sending out the UDP packet in writeData().
 * 06/27/2008  JPW	Add headless (no GUI) mode.  Class no longer extends JFrame.
 * 10/02/2007  JPW	Replace the single recipient with a Vector of
 *			Recipient objects (provided as an argument in the
 *			constructor).  To do this, I removed the use of
 *			recipientHost, recipientPort, and inetSocketAddress;
 *			added Vector recipients.
 * 09/26/2007  JPW	Add stream from oldest and autostart arguments
 *			to the constructor.
 * 06/03/2005  JPW	Created.  Based on the TCPCaster class.
 *
 */

public class UDPCaster implements ActionListener {
    
    /**
     * RBNB server to connect to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private String serverAddress = "localhost:3333";
    
    /**
     * RBNB channel to subscribe to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private String chanName = null;
    
    /**
     * Socket port packets are sent from (the sender's port)
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private int senderPort = 3456;
    
    /**
     * Destination addresses of the UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/02/2007
     */
    // private String recipientHost = "localhost";
    // private int recipientPort = 5555;
    // private InetSocketAddress inetSocketAddress = null;
    private Vector recipients = new Vector();
    
    /**
     * DatagramSocket, used to send out UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private DatagramSocket datagramSocket = null;
    
    /**
     * RBNB Sink connection
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private Sink sink = null;
    
    /**
     * Are we connected to the RBNB?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private boolean bConnected = false;
    
    /**
     * Keep sending out RBNB data?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private boolean bKeepRunning = true;
    
    /**
     * Thread which fetches data from the RBNB and sends it out as UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private Thread rbnbThread = null;
    
    /**
     * The number of frames fetched from the RBNB server
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private int frameNumber = 0;
    
    /**
     * Subscribe from oldest?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private boolean bStreamFromOldest = false;
    
    /**
     * GUI objects
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private JTextField serverAddressTextField = null;
    private JTextField chanNameTextField = null;
    private JTextField recipientAddressTextField = null;
    private JRadioButton streamOldestRB = null;
    private JRadioButton streamNewestRB = null;
    private ButtonGroup streamGroup = null;
    private JTextField frameNumberTextField = null;
    private JTextField timestampTextField = null;
    private JButton selectChanButton = null;
    
    /**
     * Running in headless mode?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/27/2008
     */
    private boolean bHeadless = false;
    
    /**
     * If we are not running in headless mode, this
     * is the frame object displayed to the user.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/27/2008
     */
    private JFrame frame = null;
    
    /**
     * Ignore errors when trying to send a UDP packet to a recipient?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/04/2010
     */
    private boolean bIgnoreSendErrors = false;
    
    /**************************************************************************
     * Constructor
     * <p>
     *
     * @author John P. Wilson
     *
     * @param serverAddressI      RBNB server to connect to
     * @param chanNameI           Channel to subscribe to
     * @param senderPortI         The local bind port
     * @param recipientsI         Where to send the UDP packets
     * @param bStreamFromOldestI  Stream from oldest?
     * @param bAutostartI         Autostart?
     * @param bHeadlessI          Run in headless (no GUI) mode?
     * @param bIgnoreSendErrorsI  Ignore UDP packet send errors?
     *
     * @version 11/04/2010
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/04/2010  JPW  Add bIgnoreSendErrorsI argument
     * 06/27/2008  JPW	Add bHeadlessI argument
     * 10/02/2007  JPW  Remove recipientHostI and recipientPortI; add
     *			Vector recipientsI.
     * 09/26/2007  JPW  Add stream from oldest and autostart arguments.
     * 06/03/2005  JPW  Created.
     *
     */
    
    public UDPCaster(String serverAddressI,
                     String chanNameI,
		     int senderPortI,
		     Vector recipientsI,
		     boolean bStreamFromOldestI,
		     boolean bAutostartI,
		     boolean bHeadlessI,
		     boolean bIgnoreSendErrorsI)
    {
	
	bHeadless = bHeadlessI;
	
	bIgnoreSendErrors = bIgnoreSendErrorsI;
	
	// super("UDPCaster    disconnected");
	if (!bHeadless) {
	    frame = new JFrame("UDPCaster    disconnected");
	}
	
	if ( (serverAddressI != null) && (!serverAddressI.equals("")) ) {
	    serverAddress = serverAddressI;
	}
	if ( (chanNameI != null) && (!chanNameI.equals("")) ) {
	    chanName = chanNameI;
	}
	if (senderPortI > 0) {
	    senderPort = senderPortI;
	}
	if (recipientsI != null) {
	    recipients = recipientsI;
	}
	
	bStreamFromOldest = bStreamFromOldestI;
	
	if (!bHeadless) {
	    JMenuBar menuBar = createMenus();
	    frame.setJMenuBar(menuBar);
	    frame.setFont(new Font("Dialog", Font.PLAIN, 12));
	}
	
        GridBagLayout gbl = new GridBagLayout();
	
	JPanel guiPanel = new JPanel(gbl);
	
        int row = 0;
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	
	// Create GUI components
	if (serverAddress != null) {
	    serverAddressTextField = new JTextField(serverAddress,30);
	} else {
	    serverAddressTextField = new JTextField(30);
	}
	if (chanName != null) {
	    chanNameTextField = new JTextField(chanName,30);
	} else {
	    chanNameTextField = new JTextField(30);
	}
	recipientAddressTextField = new JTextField(30);
	if ( (recipients != null) && (!recipients.isEmpty()) ) {
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < recipients.size(); ++i) {
		Recipient rec = (Recipient)recipients.elementAt(i);
		if (i > 0) {
		    sb.append(",");
		}
		sb.append(rec.toString());
	    }
	    recipientAddressTextField.setText(sb.toString());
	}
	streamOldestRB = new JRadioButton("From oldest", bStreamFromOldest);
	streamNewestRB = new JRadioButton("From newest", !bStreamFromOldest);
	streamGroup = new ButtonGroup();
	streamGroup.add(streamOldestRB);
	streamGroup.add(streamNewestRB);
	frameNumberTextField = new JTextField("0",30);
	frameNumberTextField.setEditable(false);
	timestampTextField = new JTextField(30);
	timestampTextField.setEditable(false);
	selectChanButton = new JButton("Select Chan...");
	selectChanButton.addActionListener(this);
	
	// RBNB address
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	JLabel tempLabel = new JLabel("RBNB address",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,0,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
        Utility.add(guiPanel,serverAddressTextField,gbl,gbc,1,row,1,1);
        row++;
	
	// RBNB channel name
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel = new JLabel("RBNB channel",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,0,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
	Utility.add(guiPanel,chanNameTextField,gbl,gbc,1,row,1,1);
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	gbc.insets = new Insets(5,0,0,15);
        Utility.add(guiPanel,selectChanButton,gbl,gbc,2,row,1,1);
	row++;
	
	// Recipient addresses
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel =
	    new JLabel("Recipients (comma-delim list)",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,0,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
        Utility.add(guiPanel,recipientAddressTextField,gbl,gbc,1,row,1,1);
        row++;
	
	// Stream from oldest or newest selection
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel = new JLabel("Stream RBNB data:",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	JPanel rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	rbPanel.add(streamOldestRB);
	rbPanel.add(streamNewestRB);
	gbc.insets = new Insets(5,0,0,15);
        Utility.add(guiPanel,rbPanel,gbl,gbc,1,row,1,1);
        row++;
	
	// Number of frames fetched from RBNB
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel =
	    new JLabel("Number of RBNB frames fetched",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,0,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
	Utility.add(guiPanel,frameNumberTextField,gbl,gbc,1,row,1,1);
	row++;
	
	// Timestamp of last RBNB frame
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel =
	    new JLabel("Timestamp of last RBNB frame",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,15,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,15,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
	Utility.add(guiPanel,timestampTextField,gbl,gbc,1,row,1,1);
	row++;
	
	// Add the panel to the content pane of the JFrame
	gbl = new GridBagLayout();
	gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,0,0,0);
	if (!bHeadless) {
	    frame.getContentPane().setLayout(gbl);
	    Utility.add(frame.getContentPane(),guiPanel,gbl,gbc,0,0,1,1);
	    
            frame.pack();
            
            // Handle the close operation in the windowClosing() method of the
            // registered WindowListener object.  This will get around
            // JFrame's default behavior of automatically hiding the window when
            // the user clicks on the '[x]' button.
            frame.setDefaultCloseOperation(
        	javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            
            frame.addWindowListener(
        	new WindowAdapter() {
        	    public void windowClosing(WindowEvent e) {
		        exit();
        	    }
        	});
            
	    frame.setVisible(true);
	}
	
	if (bAutostartI) {
	    // Check to make sure we have all needed data
	    if ( (serverAddress != null) && (!serverAddress.equals("")) &&
	         (chanName != null) && (!chanName.equals(""))           &&
	         (senderPort > 0)                                       &&
		 (recipients != null) && (!recipients.isEmpty()) )
	    {
		System.err.println(
		    "\nAuto start using the following parameters:");
		System.err.println("Server address: " + serverAddress);
		System.err.println("Channel name: " + chanName);
		System.err.println("Local bind port: " + senderPort);
		System.err.println("Recipient addresses:");
		for (Enumeration e=recipients.elements(); e.hasMoreElements();)
		{
		    Recipient rec = (Recipient)e.nextElement();
		    System.err.println("\t" + rec);
		}
		if (bStreamFromOldest) {
		    System.err.println("Stream from oldest");
		} else {
		    System.err.println("Stream from newest");
		}
		if (bIgnoreSendErrors) {
		    System.err.println("Ignore UDP send errors");
		} else {
		    System.err.println("Don't ignore UDP send errors");
		}
		System.err.println("\n");
		// Start
		openAction();
	    }
	    else
	    {
		System.err.print("\n\nCannot autostart: ");
		if ( (serverAddress == null) || (serverAddress.equals("")) ) {
		    System.err.println("Server address not initialized\n");
		} else if ( (chanName == null) || (chanName.equals("")) ) {
		    System.err.println("Input channel name not initialized\n");
		} else if (senderPort > 0) {
		    System.err.println("Local bind port less than or equal to 0\n");
		} else if ( (recipients == null) || (recipients.isEmpty()) ) {
		    System.err.println("No recipients have been specified.\n");
		}
	    }
	}
	
    }
    
    /**************************************************************************
     * Creates menu bar and menus for the GUI.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    private JMenuBar createMenus() {
	
	Font font = new Font("Dialog", Font.PLAIN, 12);
	JMenuBar menuBar = new JMenuBar();
	menuBar.setFont(font);
	
	JMenu menu = null;
	JMenuItem menuItem = null;
	
	// Add File menu
	menu = new JMenu("File");
	menu.setFont(font);
	menuItem = new JMenuItem("Open");
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
	
	return menuBar;
	
    }
    
    /**************************************************************************
     * ActionEvent handler.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	
	if (label.equals("Open")) {
	    openAction();
	}
	
	else if (label.equals("Close")) {
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    }
	    disconnect();
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	}
	
	else if (label.equals("Exit")) {
	    exit();
	}
	
	else if (label.equals("Select Chan...")) {
	    selectRBNBChan(false);
	}
	
    }
    
    /**************************************************************************
     * Check that the user has entered values in the GUI fields and then call
     * connect().
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void openAction() {
	
	// Check that the user has entered something in the server address,
	// channel name, and recipients fields
	String tempServerAddress = serverAddressTextField.getText().trim();
	String tempChanName = chanNameTextField.getText().trim();
	String recipientsStr = recipientAddressTextField.getText().trim();
	
	if ((tempServerAddress == null) || (tempServerAddress.length() == 0)) {
	    if (!bHeadless) {
		JOptionPane.showMessageDialog(
		    frame,
		    "Must enter an RBNB address in the " +
		    	"\"RBNB address\" field.",
		    "Connect Error",
		    JOptionPane.ERROR_MESSAGE);
	    }
	    System.err.println("No server address was provided.");
	    return;
	}
	if ( (tempChanName == null) || (tempChanName.length() == 0) ) {
	    if (!bHeadless) {
		JOptionPane.showMessageDialog(
		    frame,
		    "Must enter an RBNB channel in the " +
		        "\"RBNB channel\" field.",
		    "Connect Error",
		    JOptionPane.ERROR_MESSAGE);
	    }
	    System.err.println("No channel name was provided.");
	    return;
	}
	if (recipientsStr.equals("")) {
	    if (!bHeadless) {
		JOptionPane.showMessageDialog(
		    frame,
		    "Must enter at least one recipient address in the " +
		    	"\"Recipients\" field.",
		    "Connect Error",
		    JOptionPane.ERROR_MESSAGE);
	    }
	    System.err.println("No recipient address was provided.");
	    return;
	}
	// convert recipientsStr into Vector of Recipient objects
	// Would be simpler to use String.split(), but this wouldn't
	// keep the code at Java 1.1.4-compliant.
	// String[] addrComponents = recipientsStr.split(",");
	Vector tempRecipients = new Vector();
	StringTokenizer st = new StringTokenizer(recipientsStr,",");
	Recipient recipient = null;
	while (st.hasMoreTokens()) {
	    String nextAddr = st.nextToken();
	    try {
		recipient = new Recipient(nextAddr);
		tempRecipients.addElement(recipient);
	    } catch (Exception excep) {
		System.err.println(
		    "Error with recipient address " +
		    nextAddr +
		    ":\n" +
		    excep);
	    }
	}
	if (tempRecipients.isEmpty()) {
	    if (!bHeadless) {
		JOptionPane.showMessageDialog(
		    frame,
		    "No valid recipient addresses in the \"Recipients\" field.",
		    "Connect Error",
		    JOptionPane.ERROR_MESSAGE);
	    }
	    System.err.println("No valid recipient address was provided.");
	    return;
	}
	
	// The GUI values checked out OK; save the values
	bStreamFromOldest = streamOldestRB.isSelected();
	serverAddress = tempServerAddress;
	chanName = tempChanName;
	recipients = tempRecipients;
	
	try {
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    }
	    connect();
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	} catch (Exception e) {
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    String errorMsg =
	        new String("Error opening connections:\n" + e.getMessage());
	    System.err.println(errorMsg);
	    if (!bHeadless) {
		JOptionPane.showMessageDialog(
		    frame,
		    errorMsg,
		    "Connect Error",
		    JOptionPane.ERROR_MESSAGE);
	    }
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    }
	    disconnect();
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    return;
	}
	
	// Disable GUI fields
	serverAddressTextField.setEnabled(false);
	chanNameTextField.setEnabled(false);
	recipientAddressTextField.setEnabled(false);
	selectChanButton.setEnabled(false);
	streamOldestRB.setEnabled(false);
	streamNewestRB.setEnabled(false);
	
    }
    
    /**************************************************************************
     * Connect to the RBNB, start Subscribing to chanName, and open the
     * DatagramSocket
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void connect() throws IOException, SAPIException {
	
	// Make sure we are disconnected first
	disconnect();
	
	bKeepRunning = true;
	
	int portAttempt = 0;
	while (portAttempt < 100) {
	    try {
		datagramSocket = new DatagramSocket(senderPort);
		break;
	    } catch (SocketException se) {
		datagramSocket = null;
		++portAttempt;
		++senderPort;
	    }
	}
	if (datagramSocket == null) {
	    throw new IOException(
	        "Could not find a port to bind the DatagramSocket to.");
	}
	System.err.println("DatagramSocket bound to local port " + senderPort);
	
	sink = new Sink();
	sink.OpenRBNBConnection(serverAddress,"CasterSink");
	ChannelMap cmap = new ChannelMap();
	cmap.Add(chanName);
	if (bStreamFromOldest) {
	    // Time-based subscription from oldest, 0 duration
	    sink.Subscribe(cmap,0.0,0.0,"oldest");
	} else {
	    // Frame-based subscription from newest
	    sink.Subscribe(cmap);
	}
	
	// Start thread to fetch RBNB data and send it out as UDP packets
	Runnable rbnbRunnable = new Runnable() {
	    public void run() {
		runFetch();
	    }
	};
	rbnbThread = new Thread(rbnbRunnable);
	rbnbThread.start();
	
	System.err.println("UDP socket and RBNB data fetch connections open.");
	
	if (!bHeadless) {
	    frame.setTitle("UDPCaster    connected to RBNB at " + serverAddress);
	}
	
	bConnected = true;
	
    }
    
    /**************************************************************************
     * Disconnect from the RBNB and close the DatagramSocket.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/09/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/09/2005  JPW  This method no longer throws IOException
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void disconnect() {
	
	if ( (!bConnected)  &&
	     (sink == null) &&
	     (datagramSocket == null) )
	{
	    return;
	}
	
	bKeepRunning = false;
	
	// Wait for the RBNB data fetch/send thread to exit
	if ( (rbnbThread != null) &&
	     (Thread.currentThread() != rbnbThread) )
	{
	    try {
		System.err.println(
		    "Waiting for the RBNB data fetch/send thread to stop...");
		rbnbThread.join(3000);
	    } catch (InterruptedException ie) {}
	}
	System.err.println("RBNB data fetch/send thread has stopped.");
	rbnbThread = null;
	
	// Close RBNB connection
	if (sink != null) {
	    sink.CloseRBNBConnection();
	    sink = null;
	}
	
	// Close the DatagramSocket
	if (datagramSocket != null) {
	    datagramSocket.close();
	    datagramSocket = null;
	}
	
	// Reset the GUI fields
	serverAddressTextField.setEnabled(true);
	chanNameTextField.setEnabled(true);
	recipientAddressTextField.setEnabled(true);
	selectChanButton.setEnabled(true);
	streamOldestRB.setEnabled(true);
	streamNewestRB.setEnabled(true);
	frameNumber = 0;
	frameNumberTextField.setText(Integer.toString(frameNumber));
	timestampTextField.setText("");
	
	System.err.println("All connections closed.");
	if (!bHeadless) {
	    frame.setTitle("UDPCaster    disconnected");
	}
	
	bConnected = false;
	
    }
    
    /**************************************************************************
     * Exit the application.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void exit() {
	
	if (!bHeadless) {
	    frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}
	disconnect();
	if (!bHeadless) {
	    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	System.exit(0);
	
    }
    
    /**************************************************************************
     * Allow user to select an RBNB channel from a dialog box.
     * <p>
     *
     * @param bIncludeUnderscoreChansI  Include channels that start with an
     *                                  underscore character ("_") in the list?
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void selectRBNBChan(boolean bIncludeUnderscoreChansI) {
	
	// NOTE: This method will never be called if we are in headless
	//       mode, so there is no need to protect out use of the
	//       "frame" variable in this method.
	
	String address = serverAddressTextField.getText().trim();
	if ( (address == null) || (address.length() == 0) ) {
	    JOptionPane.showMessageDialog(
		frame,
		"You must first enter an RBNB address in the " +
		"\"RBNB address\" field.",
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	// Get list channels from the RBNB
	frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	Sink tempSink = new Sink();
	ChannelMap cm = null;
	try {
	    tempSink.OpenRBNBConnection(address,"TempSink");
	    tempSink.RequestRegistration();
	    cm = tempSink.Fetch(5000);
	    tempSink.CloseRBNBConnection();
	} catch (SAPIException e) {
	    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    JOptionPane.showMessageDialog(
		frame,
		"Caught exception trying to obtain channel names:\n" +
		    e.getMessage(),
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	
	if ( (cm == null) || (cm.NumberOfChannels() == 0) ) {
	    JOptionPane.showMessageDialog(
		frame,
		"No channels available in the data server.",
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	String[] channelList = cm.GetChannelList();
	// Exclude channels that start with an underscore
	if (!bIncludeUnderscoreChansI) {
	    Vector reducedChanVector = new Vector();
	    for (int i = 0; i < channelList.length; ++i) {
		if (channelList[i].charAt(0) != '_') {
		    reducedChanVector.addElement(channelList[i]);
		}
	    }
	    if (reducedChanVector.size() > 0) {
	        channelList =
		    (String[])reducedChanVector.toArray(new String[0]);
	    } else {
		channelList = null;
	    }
	}
	if ( (channelList == null) || (channelList.length == 0) ) {
	    JOptionPane.showMessageDialog(
		frame,
		"No channels available in the data server.",
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	String selectedChanStr = (String)channelList[0];
	Object selectedValue =
	    JOptionPane.showInputDialog(
	        frame,
		"Select the desired RBNB channel",
		"RBNB Channel",
		JOptionPane.QUESTION_MESSAGE,
		null,
		channelList,
		selectedChanStr);
	if (selectedValue != null) {
	    chanNameTextField.setText((String)selectedValue);
	}
	
    }
    
    /**************************************************************************
     * Fetch data from RBNB and send it out as a UDP packet
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void runFetch() {
	
	try {
	
	while (bKeepRunning) {
	    
	    ChannelMap dataMap = sink.Fetch(1000);
	    if ( (dataMap == null) || (dataMap.NumberOfChannels() == 0) ) {
		continue;
	    }
	    
	    for (int i = 0; i < dataMap.NumberOfChannels(); ++i) {
		int length = 0;
		// Extract data from ChannelMap using GetDataAsByteArray() only
		// if the type of the data is TYPE_BYTEARRAY.  Otherwise, just
		// treat the data as a blob of bytes.
		if (dataMap.GetType(i) != ChannelMap.TYPE_BYTEARRAY) {
		    byte[] dataArray = dataMap.GetData(i);
		    if (dataArray == null) {
			continue;
		    }
		    length = 1;
		    // Write data out as UDP packet
		    writeData(dataArray);
		} else {
		    byte[][] dataArray = dataMap.GetDataAsByteArray(i);
		    if (dataArray == null) {
			continue;
		    }
		    length = dataArray.length;
		    for (int j = 0; j < length; ++j) {
			// Write data out as UDP packet
			writeData(dataArray[j]);
		    }
		}
		// Update GUI info
		frameNumber += length;
		frameNumberTextField.setText(Integer.toString(frameNumber));
		// Update the time display with the start time
		// This is rather arbitrary, but if the start time is less than
		// 10^6, assume the time is relative (not seconds since epoch)
		double time = dataMap.GetTimeStart(i);
		String startTimeStr = Double.toString(time);
		if (time > 1000000) {
		    SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd-MMM-yyyy z HH:mm:ss.SSS");
		    // time is in seconds since 01/01/1970; first need to
		    // convert to number of milliseconds since epoch
		    Date displayDate = new Date( (long)(time * 1000.0) );
		    startTimeStr = dateFormat.format(displayDate);
		}
		timestampTextField.setText(startTimeStr);
		System.err.println("Send " + length + " frames (total = " + frameNumber + "); start time = " + startTimeStr);
	    }
	    
	} // end while loop
	
	} catch (Exception e) {
	    System.err.println(
		"Error fetching RBNB data or sending UDP packet out:\n" +
	        e.getMessage());
	    e.printStackTrace();
	    if (!bHeadless) {
		JOptionPane.showMessageDialog(
		    frame,
		    "Error fetching RBNB data or sending UDP packet out:\n" +
		        e.getMessage(),
		    "Data Error",
		    JOptionPane.ERROR_MESSAGE);
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    }
	    disconnect();
	    if (!bHeadless) {
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	}
    }
    
    /**************************************************************************
     * Write data out as a UDP packet
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/04/2010
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/04/2010  JPW  Only throw an exception if bIgnoreSendErrors is false.
     * 10/02/2007  JPW  Send DatagramPacket to all recipients
     * 06/03/2005  JPW  Created.
     *
     */
    
    private void writeData(byte[] dataI) throws Exception {
	
	if ( (dataI == null) || (dataI.length == 0) ) {
	    if (!bIgnoreSendErrors) {
		throw new Exception("Error: tried to write out empty packet.");
	    }
	}
	
	DatagramPacket dp =
	    new DatagramPacket( new byte[dataI.length], dataI.length );
	dp.setData(dataI);
	for (Enumeration e = recipients.elements(); e.hasMoreElements(); ) {
	    Recipient rec = (Recipient)e.nextElement();
	    dp.setSocketAddress(rec.getSocketAddr());
	    try {
		datagramSocket.send(dp);
	    } catch (Exception exc) {
		if (!bIgnoreSendErrors) {
		    // throw the exception
		    throw exc;
		}
	    }
	}
	
    }
    
}

