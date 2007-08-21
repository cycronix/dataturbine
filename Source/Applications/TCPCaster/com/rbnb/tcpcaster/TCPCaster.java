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


package com.rbnb.tcpcaster;

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
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
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
 * all client TCP sockets.
 * <p>
 *
 * @author John P. Wilson
 *
 * @version 06/09/2005
 */

/*
 * Copyright 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/09/2005  JPW	Several small changes to keep this application's
 *			features on-par with UDPCaster:
 *			    1. Have an option to not display channels that
 *				start with '_' in selectRBNBChan()
 *			    2. Add oldest/newest streaming options
 *			    3. When connected to RBNB, disable GUI components;
 *				when disconnected, enable GUI components
 *			In connect() when subscribing to oldest: after starting
 *			    the server socket, sleep for a bit before
 *			    subscribing to oldest.  This will give clients who
 *			    are ready to connect some time to do so and RBNB
 *			    data won't be missed.
 * 05/16/2005  JPW	In runFetch(): Extract the data from the ChannelMap
 *			using GetDataAsByteArray() only if the type of the data
 *			is TYPE_BYTEARRAY.  Otherwise, just treat the data as a
 *			blob of bytes.
 * 05/04/2005  JPW	Created.
 *
 */

public class TCPCaster extends JFrame implements ActionListener {
    
    /**
     * RBNB server to connect to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/04/2005
     */
    private String serverAddress = "localhost:3333";
    
    /**
     * RBNB channel to subscribe to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/04/2005
     */
    private String chanName = null;
    
    /**
     * Server port to listen for client connection on
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/04/2005
     */
    private int port = 13280;
    
    /**
     * RBNB Sink connection
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private Sink sink = null;
    
    /**
     * Server socket; this is what TCP clients will connect to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private ServerSocket serverSocket = null;
    
    /**
     * Hashtable to store client Socket connections and OutputStreams
     * <p>
     * The client Sockets are the keys; the values are the corresponding
     * OutputStreams.
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private Hashtable clientInfo = new Hashtable();
    
    /**
     * Are we connected to the RBNB and is the TCP server socket open?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private boolean bConnected = false;
    
    /**
     * Keep accepting new client connections and keep sending out RBNB data?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private boolean bKeepRunning = true;
    
    /**
     * Thread which accepts connections from TCP client applications
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private Thread acceptThread = null;
    
    /**
     * Thread which fetches data from the RBNB and sends it to the TCP clients
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private Thread rbnbThread = null;
    
    /**
     * The number of frames fetched from the RBNB server
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    private int frameNumber = 0;
    
    /**
     * Subscribe from oldest?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/09/2005
     */
    private boolean bStreamFromOldest = false;
    
    /**
     * GUI objects
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/09/2005
     *
     * 06/09/2005  JPW  Added streamOldestRB, streamNewestRB, and streamGroup
     */
    private JTextField serverAddressTextField = null;
    private JTextField chanNameTextField = null;
    private JTextField portTextField = null;
    private JRadioButton streamOldestRB = null;
    private JRadioButton streamNewestRB = null;
    private ButtonGroup streamGroup = null;
    private JTextField frameNumberTextField = null;
    private JTextField tcpClientNumberTextField = null;
    private JTextField timestampTextField = null;
    private JButton selectChanButton = null;
    
    /**************************************************************************
     * Constructor
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @version 05/04/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/04/2005  JPW  Created.
     *
     */
    
    public TCPCaster() {
	this(null, null, -1);
    }
    
    public TCPCaster(String serverAddressI, String chanNameI, int portI) {
	
	super("TCPCaster    disconnected");
	
	if ( (serverAddressI != null) && (!serverAddressI.equals("")) ) {
	    serverAddress = serverAddressI;
	}
	if ( (chanNameI != null) && (!chanNameI.equals("")) ) {
	    chanName = chanNameI;
	}
	if (portI > 0) {
	    port = portI;
	}
	
	createMenus();
	
	setFont(new Font("Dialog", Font.PLAIN, 12));
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
	if (port > 0) {
	    portTextField = new JTextField(Integer.toString(port),30);
	} else {
	    portTextField = new JTextField(30);
	}
	streamOldestRB = new JRadioButton("From oldest", bStreamFromOldest);
	streamNewestRB = new JRadioButton("From newest", !bStreamFromOldest);
	streamGroup = new ButtonGroup();
	streamGroup.add(streamOldestRB);
	streamGroup.add(streamNewestRB);
	tcpClientNumberTextField = new JTextField("0",30);
	tcpClientNumberTextField.setEditable(false);
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
        gbc.insets = new Insets(15,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(15,0,0,15);
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
	
	// TCP server port
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel = new JLabel("TCP server port",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,0,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
	Utility.add(guiPanel,portTextField,gbl,gbc,1,row,1,1);
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
	
	// Number of TCP Clients
	gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
	tempLabel = new JLabel("Number of TCP clients",SwingConstants.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        Utility.add(guiPanel,tempLabel,gbl,gbc,0,row,1,1);
	gbc.insets = new Insets(5,0,0,15);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 100;
	Utility.add(guiPanel,tcpClientNumberTextField,gbl,gbc,1,row,1,1);
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
        getContentPane().setLayout(gbl);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,0,0,0);
        Utility.add(getContentPane(),guiPanel,gbl,gbc,0,0,1,1);
	
	pack();
	
	// Handle the close operation in the windowClosing() method of the
	// registered WindowListener object.  This will get around
	// JFrame's default behavior of automatically hiding the window when
	// the user clicks on the '[x]' button.
	this.setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	
	addWindowListener(
            new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    exit();
		}
	    });
	
	setVisible(true);
	
    }
    
    /**************************************************************************
     * Creates menu bar and menus for the GUI.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/05/2005  JPW  Created.
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
	
	setJMenuBar(menuBar);
	
    }
    
    /**************************************************************************
     * ActionEvent handler.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/05/2005  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	
	if (label.equals("Open")) {
	    openAction();
	}
	
	else if (label.equals("Close")) {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    disconnect();
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
     * @version 05/05/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/09/2005  JPW  After connecting, disable GUI fields
     * 05/05/2005  JPW  Created.
     *
     */
    
    private void openAction() {
	
	// Check that the user has entered something in the server address,
	// channel name, and port fields
	String tempServerAddress = serverAddressTextField.getText().trim();
	String tempChanName = chanNameTextField.getText().trim();
	String portStr = portTextField.getText().trim();
	
	if ((tempServerAddress == null) || (tempServerAddress.length() == 0)) {
	    JOptionPane.showMessageDialog(
		this,
		"Must enter an RBNB address in the " +
		    "\"RBNB Address\" field.",
		"Connect Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	if ( (tempChanName == null) || (tempChanName.length() == 0) ) {
	    JOptionPane.showMessageDialog(
		this,
		"Must enter an RBNB channel in the " +
		    "\"RBNB Channel\" field.",
		"Connect Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	if ( (portStr == null) || (portStr.length() == 0) ) {
	    JOptionPane.showMessageDialog(
		this,
		"Must enter a port number in the " +
		    "\"TCP Server Port\" field.",
		"Connect Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	int tempPortInt = -1;
	try {
	    tempPortInt = Integer.parseInt(portStr);
	} catch (NumberFormatException nfe) {
	    JOptionPane.showMessageDialog(
		this,
		"Must enter a port number in the " +
		    "\"TCP Server Port\" field.",
		"Connect Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	// The GUI values checked out OK; save the values
	bStreamFromOldest = streamOldestRB.isSelected();
	serverAddress = tempServerAddress;
	chanName = tempChanName;
	port = tempPortInt;
	
	try {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    connect();
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	} catch (Exception e) {
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    JOptionPane.showMessageDialog(
		this,
		"Error opening connections:\n" +
		    e.getMessage(),
		"Connect Error",
		JOptionPane.ERROR_MESSAGE);
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    disconnect();
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    return;
	}
	
	// JPW 06/09/2005: Disable GUI fields
	serverAddressTextField.setEnabled(false);
	chanNameTextField.setEnabled(false);
	portTextField.setEnabled(false);
	selectChanButton.setEnabled(false);
	streamOldestRB.setEnabled(false);
	streamNewestRB.setEnabled(false);
	
    }
    
    /**************************************************************************
     * Connect to the RBNB, start Subscribing to chanName, and open the server
     * socket.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/09/2005  JPW  When subscribing to oldest: after starting the server
     *                  socket, sleep for a bit before subscribing to oldest.
     *                  This will give clients who are ready to connect some
     *                  time to do so and RBNB data won't be missed.
     * 05/05/2005  JPW  Created.
     *
     */
    
    private void connect() throws IOException, SAPIException {
	
	// Make sure we are disconnected first
	disconnect();
	
	bKeepRunning = true;
	
	serverSocket = new ServerSocket(port);
	// Set a timeout so the ServerSocket's accept() method will return
	// after waiting for 1 second
	serverSocket.setSoTimeout(1000);
	// Start the thread which will accept client TCP connections
	Runnable serverSocketRunnable = new Runnable() {
	    public void run() {
		runAccept();
	    }
	};
	acceptThread = new Thread(serverSocketRunnable);
	acceptThread.start();
	
	sink = new Sink();
	sink.OpenRBNBConnection(serverAddress,"CasterSink");
	ChannelMap cmap = new ChannelMap();
	cmap.Add(chanName);
	// JPW 06/09/2005: Add stream from oldest/newest option
	if (bStreamFromOldest) {
	    // JPW 06/09/2005: Sleep for a bit just in case there are any TCP
	    //                 clients ready to connect - give them time to
	    //                 connect before starting subscribe to oldest
	    try {
		Thread.currentThread().sleep(5000);
	    } catch (Exception e) {
		// nothing to do
	    }
	    // Time-based subscription from oldest, 0 duration
	    sink.Subscribe(cmap,0.0,0.0,"oldest");
	} else {
	    // Frame-based subscription from newest
	    sink.Subscribe(cmap);
	}
	
	// Start thread to fetch RBNB data and send it to the TCP clients
	Runnable rbnbRunnable = new Runnable() {
	    public void run() {
		runFetch();
	    }
	};
	rbnbThread = new Thread(rbnbRunnable);
	rbnbThread.start();
	
	System.err.println("TCP server and RBNB data fetch connections open.");
	
	setTitle("TCPCaster    connected to " + serverAddress);
	
	bConnected = true;
	
    }
    
    /**************************************************************************
     * Disconnect from the RBNB, close the server Socket, and close all
     * client socket connections.
     * socket.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/09/2005  JPW  After disconnecting, enable GUI fields
     *                  This method no longer throws IOException
     * 05/05/2005  JPW  Created.
     *
     */
    
    private void disconnect() {
	
	if ( (!bConnected)                    &&
	     (sink == null)                   &&
	     (serverSocket == null)           &&
	     (clientInfo.size() == 0) )
	{
	    // JPW 06/09/2005: Make sure components are reset
	    resetComponents();
	    return;
	}
	
	bKeepRunning = false;
	
	// Wait for the TCP server socket thread to exit
	if ( (acceptThread != null) &&
	     (Thread.currentThread() != acceptThread) )
	{
	    try {
		System.err.println(
		    "Waiting for the TCP server thread to stop...");
		acceptThread.join(3000);
	    } catch (InterruptedException ie) {}
	}
	System.err.println("TCP server thread has stopped.");
	acceptThread = null;
	
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
	
	// Close the Socket connection and OutputStream to each client
	for (Enumeration keys = clientInfo.keys(); keys.hasMoreElements();) {
	    Socket socket = (Socket)keys.nextElement();
	    OutputStream os = (OutputStream)clientInfo.get(socket);
	    try {
		os.close();
		socket.close();
	    } catch (IOException ioe) {
		ioe.printStackTrace();
		JOptionPane.showMessageDialog(
		    this,
		    "Caught exception closing connections:\n" +
		    ioe.getMessage(),
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    }
	}
	clientInfo.clear();
	
	// Close the server socket
	if (serverSocket != null) {
	    try {
		serverSocket.close();
	    } catch (IOException ioe) {
		ioe.printStackTrace();
		JOptionPane.showMessageDialog(
		    this,
		    "Caught exception closing server socket connection:\n" +
		    ioe.getMessage(),
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    }
	    serverSocket = null;
	}
	
	// Reset the GUI fields
	resetComponents();
	
	System.err.println("All connections closed.");
	setTitle("TCPCaster    disconnected");
	
	bConnected = false;
	
    }
    
    /**************************************************************************
     * Reset GUI components.
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
     * 06/09/2005  JPW  Created.
     *
     */
    
    private void resetComponents() {
	// JPW 06/09/2005: Enable GUI fields
	serverAddressTextField.setEnabled(true);
	chanNameTextField.setEnabled(true);
	portTextField.setEnabled(true);
	selectChanButton.setEnabled(true);
	streamOldestRB.setEnabled(true);
	streamNewestRB.setEnabled(true);
	tcpClientNumberTextField.setText(Integer.toString(clientInfo.size()));
	frameNumber = 0;
	frameNumberTextField.setText(Integer.toString(frameNumber));
	timestampTextField.setText("");
    }
    
    /**************************************************************************
     * Exit the application.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/05/2005  JPW  Created.
     *
     */
    
    private void exit() {
	
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	disconnect();
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	
	System.exit(0);
	
    }
    
    /**************************************************************************
     * Allow user to select an RBNB channel from a dialog box.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/06/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/09/2005  JPW  Add bIncludeUnderscoreChansI; if this is false, exclude
     *                  channels that start with an underscore.
     * 05/06/2005  JPW  Created.
     *
     */
    
    private void selectRBNBChan(boolean bIncludeUnderscoreChansI) {
	
	String address = serverAddressTextField.getText().trim();
	if ( (address == null) || (address.length() == 0) ) {
	    JOptionPane.showMessageDialog(
		this,
		"You must first enter an RBNB address in the " +
		"\"RBNB Address\" field.",
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	// Get list channels from the RBNB
	setCursor(new Cursor(Cursor.WAIT_CURSOR));
	Sink tempSink = new Sink();
	ChannelMap cm = null;
	try {
	    tempSink.OpenRBNBConnection(address,"TempSink");
	    tempSink.RequestRegistration();
	    cm = tempSink.Fetch(5000);
	    tempSink.CloseRBNBConnection();
	} catch (SAPIException e) {
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    JOptionPane.showMessageDialog(
		this,
		"Caught exception trying to obtain channel names:\n" +
		    e.getMessage(),
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	
	if ( (cm == null) || (cm.NumberOfChannels() == 0) ) {
	    JOptionPane.showMessageDialog(
		this,
		"No channels available in the data server.",
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	String[] channelList = cm.GetChannelList();
	// JPW 06/09/2005: If desired by the caller, exclude channels that
	//                 start with an underscore
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
		this,
		"No channels available in the data server.",
		"Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	String selectedChanStr = (String)channelList[0];
	Object selectedValue =
	    JOptionPane.showInputDialog(
	        this,
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
     * Main TCP Server loop.  Accept connections from TCP clients.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/05/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/05/2005  JPW  Created.
     *
     */
    
    private void runAccept() {
	
	try {
	
	while (bKeepRunning) {
	    
	    Socket clientSocket = null;
	    try {
		clientSocket = serverSocket.accept();
	    } catch (SocketTimeoutException ste) {
		continue;
	    }
	    
	    // OutputStream to write to the client
	    OutputStream out = clientSocket.getOutputStream();
	    
	    synchronized (clientInfo) {
		// Add the client socket and OutputStream to the Hashtable
		clientInfo.put(clientSocket, out);
		tcpClientNumberTextField.setText(
		    Integer.toString(clientInfo.size()));
	    }
	    
	    System.err.println(
	        "TCP client connection accepted from " +
		clientSocket.getInetAddress());
	    
	}
	
	} catch (Exception e) {
	    e.printStackTrace();
	    JOptionPane.showMessageDialog(
		this,
		"Error accepting new TCP client connections:\n" +
		    e.getMessage(),
		"TCP Server Socket Error",
		JOptionPane.ERROR_MESSAGE);
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    disconnect();
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
    }
    
    /**************************************************************************
     * Fetch data from RBNB and send it to the TCP clients.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/16/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/16/2005  JPW  Extract the data from the ChannelMap using
     *			GetDataAsByteArray() only if the type of the data is
     *			TYPE_BYTEARRAY.  Otherwise, just treat the data as a
     *			blob of bytes.
     * 05/05/2005  JPW  Created.
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
		// JPW 05/16/2005: Extract the data from the ChannelMap using
		//                 GetDataAsByteArray() only if the type of the
		//                 data is TYPE_BYTEARRAY.  Otherwise, just
		//                 treat the data as a blob of bytes.
		if (dataMap.GetType(i) != ChannelMap.TYPE_BYTEARRAY) {
		    byte[] dataArray = dataMap.GetData(i);
		    if (dataArray == null) {
			continue;
		    }
		    length = 1;
		    // Write data to the connected TCP client OutputStreams
		    writeData(dataArray);
		} else {
		    byte[][] dataArray = dataMap.GetDataAsByteArray(i);
		    if (dataArray == null) {
			continue;
		    }
		    length = dataArray.length;
		    for (int j = 0; j < length; ++j) {
			// Write data to the connected TCP client OutputStreams
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
	    }
	    
	} // end while loop
	
	} catch (Exception e) {
	    e.printStackTrace();
	    JOptionPane.showMessageDialog(
		this,
		"Error fetching RBNB data or sending data to TCP clients:\n" +
		    e.getMessage(),
		"Data Error",
		JOptionPane.ERROR_MESSAGE);
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    disconnect();
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
    }
    
    /**************************************************************************
     * Write data to the connected TCP client OutputStreams.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/06/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2005  JPW  Created.
     *
     */
    
    private void writeData(byte[] dataI) {
	synchronized (clientInfo) {
	    Vector socketsToRemove = new Vector();
	    for (Enumeration sockets = clientInfo.keys();
		 sockets.hasMoreElements();)
	    {
		Socket socket = (Socket)sockets.nextElement();
		OutputStream out = (OutputStream)clientInfo.get(socket);
		
		//If Socket is closed, remove it from Hashtable
		try {
		    out.write(dataI);
		} catch (Exception e) {
		    // Don't remove the socket here since we are in the midst
		    // of an Enumeration over the socket entries!!!
		    socketsToRemove.addElement(socket);
		    System.err.println(
		        "Exception caught writing to client socket\n" +
			"(the client Socket connection might have closed)\n" +
			"Exception:\n" +
			e);
		}
	    }
	    // Remove any sockets that have closed from the Hashtable
	    if (socketsToRemove.size() > 0) {
		for (int i = 0; i < socketsToRemove.size(); ++i) {
		    Socket socket =
		        (Socket)socketsToRemove.elementAt(i);
		    clientInfo.remove(socket);
		    System.err.println(
			"TCP client connection dropped: " +
			socket.getInetAddress());
		}
		tcpClientNumberTextField.setText(
		    Integer.toString(clientInfo.size()));
	    }
	} // end synchronized block
    }
    
}

