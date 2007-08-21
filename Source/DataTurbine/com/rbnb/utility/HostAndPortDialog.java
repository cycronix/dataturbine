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


// moved from RBNBAdmin to Utility, 9/8/98 EMF
package com.rbnb.utility;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

/////////////////////////////////////////////////////////////////////////////
//
//  Class:      HostAndPortDialog
//  Date:       January 5, 1998
//  Programmer: John P. Wilson
//  For:        Flyscan
//
//  Copyright 1998 Creare Inc, Hanover, N.H.
//  All rights reserved.
//
//  This class defines a dialog box which prompts the user to enter
//  a Host and Port for an RBNB connection.
//
//  Modification History:
//  11/04/1998  DLL     Add method to override the default name and port 
//                      validation checks.  This permits using this class
//                      in applications where we might want to specify a 
//                      host and port and be able to disable that specification.
//  07/28/1998  JPW     Got rid of the show() method which contained a call
//                      to super.show(). JRE 1.1.6 doesn't like this call
//                      occuring from within this dialog.
//  04/09/2000  UCB     Added host-address completion to OKAction() method,
//                      so that incomplete host names will not be returned.
//  03/03/2001  JPW	Add optional username and password TextFields,
//			usernameComponent and passwordComponent.  Add
//			bShowUsernamePassword and bRequireUsername.
//  03/09/2001	JPW	If the user edits their username, blank out the
//			password textfield.
//
/////////////////////////////////////////////////////////////////////////////

public class HostAndPortDialog extends Dialog
                               implements ActionListener,
					  TextListener,
					  WindowListener
{
    
    private TextField nameComponent = null;
    private TextField portComponent = null;
    
    // JPW 3/3/2001
    private TextField usernameComponent = null;
    private TextField passwordComponent = null;
    public String username = null;
    public String password = null;
    private boolean bShowUsernamePassword = false;
    private boolean bRequireUsername = false;
    
    public String machine = null;
    public int    port    = -1;
    public int    state;
    public static final int OK = 1;
    public static final int CANCEL = 2;
    
    private InfoDialog infoDialog = null;
    private boolean validityChecks = true;
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.HostAndPortDialog()   (constructor)
    //  Date:       March 2001
    //  Programmer: John P. Wilson
    //  For:        EScan
    //
    //  Copyright 2001 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the HostAndPort dialog box.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public HostAndPortDialog(Frame parentI,
                             boolean modalI,
                             String titleStrI,
                             String descriptionStrI,
                             String machineStrI,
                             int portIntI,
                             boolean bEnableNameComponentI)
    {
	
	this(parentI,
	     modalI,
	     titleStrI,
	     descriptionStrI,
	     machineStrI,
	     portIntI,
	     bEnableNameComponentI,
	     "",
	     "",
	     false,
	     false);
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.HostAndPortDialog()   (constructor)
    //  Date:       January 5, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the HostAndPort dialog box.
    //
    //  Modification History:
    //  06/29/1998  JPW     Added the titleStrI and descriptionStrI parameters
    //  07/28/1998  JPW     Added the machineStr, portInt, and
    //                      bEnableNameComponent parameters
    //  03/05/2001  JPW     Add bShowUsernamePasswordI, bRequireUsernameI,
    //				usernameI, and passwordI arguments.
    //
    /////////////////////////////////////////////////////////////////////////
    
    public HostAndPortDialog(Frame parentI,
                             boolean modalI,
                             String titleStrI,
                             String descriptionStrI,
                             String machineStrI,
                             int portIntI,
                             boolean bEnableNameComponentI,
			     String usernameI,
			     String passwordI,
			     boolean bShowUsernamePasswordI,
			     boolean bRequireUsernameI)
    {
        
        super(parentI, modalI);
        
	// JPW 3/5/2001: Add bShowUsernamePassword and bRequireUsername
	bShowUsernamePassword = bShowUsernamePasswordI;
	bRequireUsername = bRequireUsernameI;
	
        Label tempLabel;
        Button OKbutton;
        Button Cancelbutton;
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
        
        setBackground(Color.lightGray);
        
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        
        int row = 0;
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // NOTE: In order to center the top label, can't set fill to BOTH
        // gbc.fill = GridBagConstraints.BOTH;
        // For some reason, this label doesn't center in the dialog properly!
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 100;
        gbc.weighty = 100;
        
        tempLabel = new Label(descriptionStrI);
        tempLabel.setAlignment(Label.CENTER);
        gbc.insets = new Insets(15,15,0,15);
        Utility.add(this,tempLabel,gbl,gbc,0,row,2,1);
        row++;
        
        gbc.anchor = GridBagConstraints.WEST;
        tempLabel = new Label("Host:");
        gbc.insets = new Insets(15,15,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        nameComponent = new TextField(20);
        gbc.insets = new Insets(15,0,0,15);
        Utility.add(this,nameComponent,gbl,gbc,1,row,1,1);
        row++;
        
        tempLabel = new Label("Port:");
        gbc.insets = new Insets(15,15,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        portComponent = new TextField(7);
        gbc.insets = new Insets(15,0,0,15);
        Utility.add(this,portComponent,gbl,gbc,1,row,1,1);
        row++;
        
	// JPW 3/5/2001: Add Username and Password TextFields
	if (bShowUsernamePassword) {
	    tempLabel = new Label("Username:");
            gbc.insets = new Insets(15,15,0,5);
            Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
            
            usernameComponent = new TextField(20);
	    if (usernameI != null) {
		usernameComponent.setText(usernameI);
	    }
            gbc.insets = new Insets(15,0,0,15);
            Utility.add(this,usernameComponent,gbl,gbc,1,row,1,1);
            row++;
	    
	    tempLabel = new Label("Password:");
            gbc.insets = new Insets(15,15,0,5);
            Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
            
            passwordComponent = new TextField(20);
	    passwordComponent.setEchoChar('*');
	    if (passwordI != null) {
		passwordComponent.setText(passwordI);
	    }
            gbc.insets = new Insets(15,0,0,15);
            Utility.add(this,passwordComponent,gbl,gbc,1,row,1,1);
            row++;
	    
	}
	
        // Want to get the 2 bottom buttons the same size:
        // put both buttons in a panel which uses GridLayout (to force all
        // components to the same size) and then add the panel to the dialog
        Panel buttonPanel = new Panel(new GridLayout(1,2,15,5));
        OKbutton = new Button("OK");
        buttonPanel.add(OKbutton);
        Cancelbutton = new Button("Cancel");
        buttonPanel.add(Cancelbutton);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15,15,15,15);
        Utility.add(this,buttonPanel,gbl,gbc,0,row,2,1);
        
        setTitle(titleStrI);
        
        pack();
        
        setResizable(false);
        
        //////////////////////
        // ADD EVENT LISTENERS
        //////////////////////
	
        OKbutton.addActionListener(this);
        Cancelbutton.addActionListener(this);
        nameComponent.addActionListener(this);
        portComponent.addActionListener(this);
	if (bShowUsernamePassword) {
	    usernameComponent.addActionListener(this);
	    usernameComponent.addTextListener(this);
	    passwordComponent.addActionListener(this);
	}
        addWindowListener(this);
        
        ///////////////////////////////
        // Initialize the dialog fields
        ///////////////////////////////
        
        initDialog(machineStrI, portIntI, bEnableNameComponentI);
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.textValueChanged()
    //  Date:       March 2001
    //  Programmer: John P. Wilson
    //  For:        EScan
    //
    //  Copyright 2001 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  When text is modified in the username TextField, blank out the
    //  password TextField.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void textValueChanged(TextEvent e) {
	
	if (e.getSource() == usernameComponent) {
	    passwordComponent.setText("");
	}
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.actionPerformed
    //  Date:       January 5, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Handles all dialog box actions.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////

    public void actionPerformed(ActionEvent evt) {
        
        if (evt.getActionCommand().equals("OK")) {
            OKAction();
        }
        else if (evt.getActionCommand().equals("Cancel")) {
            cancelAction();
        }
        else if ( (evt.getSource() == nameComponent) ||
                  (evt.getSource() == portComponent) ||
		  (evt.getSource() == usernameComponent) ||
		  (evt.getSource() == passwordComponent) )
        {
            OKAction();
        }
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.OKAction
    //  Date:       January 13, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Check that the data entered by the user is valid before saving data
    //  and popping down the dialog.
    //
    //  Modification History:
    //  03/08/2001	JPW	Add a check on username of bRequireUsername
    //				is true.
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void OKAction() {
        
        int portInt;
        
        String nameStr = nameComponent.getText().trim();
        String portStr = portComponent.getText().trim();
	String usernameStr = "";
	String passwordStr = "";
	if (bShowUsernamePassword) {
	    usernameStr = usernameComponent.getText().trim();
	    // NOTE: Don't trim off white space from password
	    passwordStr = passwordComponent.getText();
	}
        
        // make sure the user hasn't entered an empty string
        if ( validityChecks && nameStr.equals("")) {
            String[] strArray = new String[1];
	    strArray[0] =
	        new String("Must enter a valid host name.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
		               true,
		               "Host Error",
		               strArray);
	    infoDialog.show();
	    infoDialog.dispose();
            return;
        }
        if ( validityChecks && portStr.equals("")) {
            String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a valid port number.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
		               true,
		               "Port Error",
		               strArray);
	    infoDialog.show();
	    infoDialog.dispose();
            return;
        }
        
        // make sure there is a valid number in the port field
        try {
            portInt = Integer.parseInt(portStr);
        }
        catch (NumberFormatException e) {
	    if ( validityChecks ) {
                portComponent.requestFocus();
                portComponent.selectAll();
                String[] strArray = new String[1];
		strArray[0] =
		    new String("Must enter a valid port number.");
		infoDialog =
		    new InfoDialog((Frame)(this.getParent()),
		                   true,
		                   "Port Error",
		                   strArray);
		infoDialog.show();
		infoDialog.dispose();
                return;
	    }
	    else {
		portInt = 0;
	    }
        }
        
        if ( validityChecks && portInt <= 0) {
            portComponent.requestFocus();
            portComponent.selectAll();
            String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a valid port number.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
		               true,
		               "Port Error",
		               strArray);
	    infoDialog.show();
	    infoDialog.dispose();
            return;
        }
        
	if ( bShowUsernamePassword &&
             bRequireUsername &&
             usernameStr.equals("") )
	{
	    String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a non-blank username.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
		               true,
		               "Username Error",
		               strArray);
	    infoDialog.show();
	    infoDialog.dispose();
            return;
	}
	
        // Save the data entered by the user
        state = OK;
        machine = nameStr;
        port = portInt;
	username = usernameStr;
	password = passwordStr;
	
        setVisible(false);
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.SetValidityChecks
    //  Date:       November 4, 1998
    //  Programmer: Daniel L. Leavitt
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Enable or disable validity checking of user input
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
      
    public void SetValidityChecking( boolean state )
    {
      validityChecks = state;
    }
      
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.cancelAction
    //  Date:       June, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  User has hit the Cancel button.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void cancelAction() {
        state = CANCEL;
        setVisible(false);
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     HostAndPortDialog.initDialog()
    //  Date:       January 5, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Initialize data displayed to the user by filling dialog components.
    //
    //  Input :
    //      machineStr              server machine host name
    //      portInt                 server machine port ID
    //      bEnableNameComponent    if this is an Applet run, this boolean
    //                                  will be false and the name TextField
    //                                  will not be enabled
    //
    //  Modification History:
    //  07/28/98    JPW     Changed name from show() to initDialog();
    //                      removed call to super.show().
    //	03/07/2000  JPW	    Use "localhost:3333" as the default.
    //
    /////////////////////////////////////////////////////////////////////////

    public synchronized void initDialog(String machineStr,
                                        int portInt,
                                        boolean bEnableNameComponent)
    {
        
        // Initialize class variables
        if (machineStr == null) {
            machine = new String("localhost");
        }
        else {
            machine = new String(machineStr);
        }
        if (portInt <= 0) {
            port = 3333;
        }
        else {
            port = portInt;
        }
        
        // initialize the dialog fields
        nameComponent.setText(machine);
        portComponent.setText(Integer.toString(port));
        
        // only enable the dialog if the user wishes to
        nameComponent.setEnabled(bEnableNameComponent);
        
        // set up the text fields
        nameComponent.setSelectionStart(0);
        nameComponent.setSelectionEnd(0);
        nameComponent.setCaretPosition(0);
        
        portComponent.setSelectionStart(0);
        portComponent.setSelectionEnd(0);
        portComponent.setCaretPosition(0);
        
        // set focus in the nameComponent TextField
        nameComponent.requestFocus();
        
        // center the dialog inside the parent Frame
        setLocation(Utility.centerRect(getBounds(), 
				       getParent().getBounds()));
        
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
        cancelAction();
    }
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    
}
