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

/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBToIPDlg			        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   The RBNBToIPDlg is a dialog in which the user	***
  ***   selects whether they want UDP or TCP output.  In either	***
  ***	case the user enters an RBNB input host and port. For	***
  ***	UDP output, they enter an output UDP address and port.	***
  ***	For TCP server output they specify a TCP server port;	***
  ***	all clients connected to this server port will receive	***
  ***	the RBNB data.						***
  ***								***
  ***	Modification History:                                   ***
  ***	06/16/2005	JPW	Extended from RBNBToUDPDlg	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

public class RBNBToIPDlg
    extends Dialog
    implements ActionListener, ItemListener, WindowListener
{
    
    private TextField inNameTF = null,
	              outAddTF = null,
	              inPortTF = null,
	              outPortTF = null,
		      serverPortTF = null;
    
    private CheckboxGroup buttonGroup = null;
    private Checkbox udpRB = null;
    private Checkbox tcpRB = null;
    
    // RBNB input connection
    public String inMachine = null;
    public int inPort;
    
    // UDP output connection
    public InetAddress outAdd = null;
    public int outPort;
    
    // TCP output connection
    public int serverPort;
    
    public boolean bUDPOutput = true;
    
    public int state;
    public static final int OK = 1;
    public static final int CANCEL = 2;
    
    private InfoDialog infoDialog = null;
   
/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBToIPDlg			        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/  
    public RBNBToIPDlg (Frame parent) {
	this(parent, "localhost", 3333, "127.0.0.1", 5555, 5555, true);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBToIPDlg			        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/  
    public RBNBToIPDlg(Frame parent, String inMachineI, int inPortI) {
	this(parent, inMachineI, inPortI, "127.0.0.1", 5555, 5555, true);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBToIPDlg			        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    public RBNBToIPDlg(Frame parent,
		       String inMachineI,
		       int inPortI,
		       String outAddI,
		       int outPortI,
		       int serverPortI,
		       boolean bUDPOutputI)
    {
	
        super(parent, true);
	
	bUDPOutput = bUDPOutputI;
	
	setTitle("Specify Input Server and Output Socket");
	setFont(new Font("Dialog", Font.PLAIN, 12));
	setBackground(Color.lightGray);
	
	GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
        
        Label tempLabel = null;
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.anchor = GridBagConstraints.CENTER;
	
	int row = 0;
	
        //////////////////////////////////////////
        // Construct Input Server Bordered Panel
        //////////////////////////////////////////
	
	GridBagLayout gbl2 = new GridBagLayout();
	Panel inP = new Panel(gbl2);
	
        tempLabel = new Label("Host:Port");
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(inP, tempLabel, gbl2, gbc, 0, 0, 1, 1);
        inNameTF = new TextField(20);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(inP, inNameTF, gbl2, gbc, 1, 0, 1, 1);
        tempLabel = new Label(" : ",Label.CENTER);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(inP, tempLabel, gbl2, gbc, 2, 0, 1, 1);
        inPortTF = new TextField(7);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(inP, inPortTF, gbl2, gbc, 3, 0, 1, 1);
        
	gbc.insets = new Insets(7, 10, 0, 10);
	Utility.add(
	this, new BorderedPanel(inP, "Input Server"), gbl, gbc, 0, row, 1, 1);
	++row;
	
	/////////////////////////////////////////
	// Allow user to select UDP or TCP output
	/////////////////////////////////////////
	
	buttonGroup = new CheckboxGroup();
	udpRB = new Checkbox("UDP Output", buttonGroup, bUDPOutput);
	tcpRB = new Checkbox("TCP Output", buttonGroup, !bUDPOutput);
	
	gbl2 = new GridBagLayout();
	Panel radioButtonP = new Panel(gbl2);
	
        Utility.add(radioButtonP, udpRB, gbl2, gbc, 0, 0, 1, 1);
        Utility.add(radioButtonP, tcpRB, gbl2, gbc, 1, 0, 1, 1);
        
	gbc.insets = new Insets(7, 10, 0, 10);
	Utility.add(this, radioButtonP, gbl, gbc, 0, row, 1, 1);
	++row;
	
        //////////////////////////////////////
        // Construct UDP Output Bordered Panel
        //////////////////////////////////////
	
	gbl2 = new GridBagLayout();
	Panel outP = new Panel(gbl2);
	
        tempLabel = new Label("IP Address:Port");
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(outP, tempLabel, gbl2, gbc, 0, 0, 1, 1);
        outAddTF = new TextField(20);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(outP, outAddTF, gbl2, gbc, 1, 0, 1, 1);
        tempLabel = new Label(" : ", Label.CENTER);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(outP, tempLabel, gbl2, gbc, 2, 0, 1, 1);
        outPortTF = new TextField(7);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(outP, outPortTF, gbl2, gbc, 3, 0, 1, 1);
	
	gbc.insets = new Insets(7, 10, 0, 10);
	Utility.add(
	this, new BorderedPanel(outP, "UDP Output"), gbl, gbc, 0, row, 1, 1);
	++row;
	
	//////////////////////////////////////
        // Construct TCP Output Bordered Panel
        //////////////////////////////////////
	
	gbl2 = new GridBagLayout();
	outP = new Panel(gbl2);
	
        tempLabel = new Label("TCP Server Port");
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(outP, tempLabel, gbl2, gbc, 0, 0, 1, 1);
        serverPortTF = new TextField(7);
	gbc.insets = new Insets(0, 0, 0, 0);
        Utility.add(outP, serverPortTF, gbl2, gbc, 1, 0, 1, 1);
	
	gbc.insets = new Insets(7, 10, 0, 10);
	Utility.add(
	this, new BorderedPanel(outP, "TCP Output"), gbl, gbc, 0, row, 1, 1);
	++row;
	
	///////////////////////////////////
        // Construct OK/Cancel Button Panel
        ///////////////////////////////////
	
        Panel buttonPanel = new Panel(new GridLayout(1,2,15,0));
        Button OKbutton = new Button("OK");
        buttonPanel.add(OKbutton);
        Button Cancelbutton = new Button("Cancel");
        buttonPanel.add(Cancelbutton);
	
	gbc.insets = new Insets(7, 10, 7, 10);
        Utility.add(this, buttonPanel, gbl, gbc, 0, row, 1, 1);
	++row;
        
        pack();
        setResizable(false);
	
        //////////////////////
        // ADD EVENT LISTENERS
        //////////////////////
        OKbutton.addActionListener(this);
        Cancelbutton.addActionListener(this);
        udpRB.addItemListener(this);
	tcpRB.addItemListener(this);
        addWindowListener(this);
        
        ///////////////////////////////
        // Initialize the components
        ///////////////////////////////
        initDialog(inMachineI, inPortI, outAddI, outPortI, serverPortI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	actionPerformed 		        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Respond to user selections.                          ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals("OK")) {
            OKAction();
        } else if (evt.getActionCommand().equals("Cancel")) {
	    state = CANCEL;
	    setVisible(false);
        }
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	itemStateChanged 		        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Respond to the user clicking the TCP or UDP radio	***
  ***	button.							***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    public void itemStateChanged(ItemEvent event) {
	Checkbox cb = (Checkbox) event.getItemSelectable();
	if (cb == udpRB) {
	    bUDPOutput = true;
	} else if (cb == tcpRB) {
	    bUDPOutput = false;
	}
	setTextFields();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	OKAction()      		        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Check the validity of the entered data, save, and    ***
  ***   go invisible.                                           ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    private void OKAction() {
	String inStr;
	int i, j;
	
	/////////////////
	// RBNB Host name
	/////////////////
	inMachine = inNameTF.getText().trim();
	if (inMachine.equals("")) {
	    errorBox("RBNB Host Error!",
		     "User must enter an RBNB host.");
	    return;
	}
	
	///////////////////
	// RBNB port number
	///////////////////
	inStr = inPortTF.getText().trim();
	if (inStr.equals("")) {
	    errorBox("RBNB Port Error!",
		     "User must enter an RBNB port.");
	    return;
	}
	try {
	    inPort = Integer.parseInt(inStr);
	} catch (NumberFormatException e) {
	    errorBox("RBNB Port Error!",
		     "User must enter a valid RBNB port.");
	    return;
	}
	
	///////////////////////
	// UDP address and port
	///////////////////////
	inStr = outAddTF.getText().trim();
	if ( (inStr.equals("")) && (bUDPOutput) ) {
	    errorBox("UDP Address Error!",
		     "User must enter a UDP address.");
	    return;
	}
	if (!inStr.equals("")) {
	    try {
		outAdd = InetAddress.getByName(inStr);
	    } catch (UnknownHostException e) {
		if (bUDPOutput) {
		    errorBox("UDP Address Error!",
			     "User must enter a valid UDP address.");
		    return;
		} else {
		    outAdd = null;
		}
	    }
	}
	inStr = outPortTF.getText().trim();
	if ( (inStr.equals("")) && (bUDPOutput) ) {
	    errorBox("UDP Port Error!",
		     "User must enter a UDP port.");
	    return;
	}
	if (!inStr.equals("")) {
	    try {
		outPort = Integer.parseInt(inStr);
	    } catch (NumberFormatException e) {
		if (bUDPOutput) {
		    errorBox("UDP Port Error!",
			     "User must enter a valid UDP port.");
		    return;
		} else {
		    outPort = -1;
		}
	    }
	}
	
	//////////////////
	// TCP server port
	//////////////////
	inStr = serverPortTF.getText().trim();
	if ( (inStr.equals("")) && (!bUDPOutput) ) {
	    errorBox("TCP Server Port Error!",
		     "User must enter a TCP server port.");
	    return;
	}
	if (!inStr.equals("")) {
	    try {
		serverPort = Integer.parseInt(inStr);
	    } catch (NumberFormatException e) {
		if (!bUDPOutput) {
		    errorBox("TCP Server Port Error!",
			     "User must enter a valid TCP server port.");
		    return;
		} else {
		    serverPort = -1;
		}
	    }
	}
	
        state = OK;
        setVisible(false);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	errorBox()          		                ***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Display an error box.                                ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    private void errorBox(String title, String message) {
	String[] strArray = new String[1];
	strArray[0] = message;
	infoDialog =
	    new InfoDialog((Frame)(this.getParent()),
			   true,
			   title,
			   strArray);
	infoDialog.setVisible(true);
	infoDialog.dispose();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	initDialog      		        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Initialize all components.                           ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    public synchronized void initDialog(String inMachineI,
                                        int inPortI,
					String outAddI,
					int outPortI,
					int serverPortI)
    {
	
	// RBNB input fields
	if ((inMachineI == null) || (inMachineI.equals(""))) {
	    inNameTF.setText("localhost");
	} else {
	    inNameTF.setText(inMachineI);
	}
	if ((inPortI < 1) || (inPortI > 65535)) {
	    inPortTF.setText("3333");
	} else {
	    inPortTF.setText(Integer.toString(inPortI));
	}
	
	// UDP output fields
	if ((outAddI == null) || (outAddI.equals(""))) {
	    outAddTF.setText("127.0.0.1");
	} else {
	    outAddTF.setText(outAddI);
	}
	if ((outPortI < 1040) || (outPortI > 65535)) {
	    outPortTF.setText("5555");
	} else {
	    outPortTF.setText(Integer.toString(outPortI));
	}
        
	// TCP output fields
	if ((serverPortI < 1040) || (serverPortI > 65535)) {
	    serverPortTF.setText("7777");
	} else {
	    serverPortTF.setText(Integer.toString(serverPortI));
	}
	
	// Enable/disable the fields appropriately
	setTextFields();
	
        // set focus in the inNameTF TextField
        inNameTF.requestFocus();
        
        // center the dialog inside the parent Frame
        setLocation(Utility.centerRect(getBounds(), 
				       getParent().getBounds()));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setTextFields      		        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Enable/disable the text fields as appropriate based	***
  ***	on the current value of bUDPOutput			***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    private void setTextFields() {
	if (bUDPOutput) {
	    outAddTF.setEnabled(true);
	    outPortTF.setEnabled(true);
	    serverPortTF.setEnabled(false);
	} else {
	    outAddTF.setEnabled(false);
	    outPortTF.setEnabled(false);
	    serverPortTF.setEnabled(true);
	}
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Methods to implement the WindowListener interface
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowClosing(WindowEvent event) {
        // User has clicked on the small "x" button in the upper right
        // corner of the screen.
	state = CANCEL;
	setVisible(false);
    }
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    
}
