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

import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rbnb.utility.Utility;

/////////////////////////////////////////////////////////////////////////////
//
//  Class:      LoadArchiveDialog
//  Date:       January 2005
//  Programmer: John P. Wilson
//  For:        Flyscan
//
//  Copyright 2005 Creare Inc, Hanover, N.H.
//  All rights reserved.
//
//  This is a simple dialog in which the user can enter an archive name and a
//  username and password for the archive.  This dialog has an OK and a Cancel
//  button.
//
//  Modification History:
//  01/24/2005  JPW     Add usernameStr field
//  01/07/2005  JPW	Created
//
/////////////////////////////////////////////////////////////////////////////

public class LoadArchiveDialog extends JDialog
                               implements ActionListener,
					  WindowListener
{
    
    // GUI components
    private JTextField archiveTF = null;
    // JPW 01/24/2005: Add username field
    private JTextField usernameTF = null;
    private JPasswordField passwordTF = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    public String archiveStr = null;
    // JPW 01/24/2005: Add username field
    public String usernameStr = null;
    public String passwordStr = null;
    public static final int OK = 1;
    public static final int CANCEL = 2;
    public int state = CANCEL;
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     LoadArchiveDialog constructor
    //  Date:       January 2005
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the LoadArchiveDialog class.
    //
    //  Input :
    //  parentI			Parent frame for this dialog.
    //  modal			Should this dialog be modal?
    //  archiveStrI		Initialization string for the archive field.
    //	usernameStrI		Initialization string for the username field.
    //	passwordStrI		Initialization string for the password field.
    //
    //  Modification History:
    //  01/24/2005  JPW     Add usernameStrI argument.
    //  01/07/2005  JPW     Created
    //
    /////////////////////////////////////////////////////////////////////////
    
    public LoadArchiveDialog(Frame parentI,
                                   boolean modal,
				   String archiveStrI,
				   String usernameStrI,
				   String passwordStrI)
    {
        
        super(parentI, "Load Archive", modal);
        
        JLabel tempLabel;
        int row = 0;
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setBackground(Color.lightGray);
        
        GridBagLayout gbl = new GridBagLayout();
        getContentPane().setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 100;
        gbc.weighty = 100;
        
	//////////////////////
	// ROW 1: Archive name
	//////////////////////
	
        tempLabel = new JLabel("Archive name",javax.swing.SwingConstants.LEFT);
        gbc.insets = new Insets(15,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,row,1,1);
        
        archiveTF = new JTextField(15);
	if (archiveStrI != null) {
	  archiveTF.setText(archiveStrI.trim());
	}
        gbc.insets = new Insets(15,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),archiveTF,gbl,gbc,1,row,1,1);
        row++;
        
	/////////////////////////////
	// ROW 2: Message to the user
	/////////////////////////////
	
	tempLabel =
	    new JLabel("Optional username and password",
	    javax.swing.SwingConstants.LEFT);
        gbc.insets = new Insets(15,15,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,row,2,1);
	row++;
	
	//////////////////
	// ROW 3: Username
	//////////////////
	
	tempLabel = new JLabel("Username",javax.swing.SwingConstants.LEFT);
        gbc.insets = new Insets(8,25,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,row,1,1);
        
        usernameTF = new JTextField(15);
	if (usernameStrI != null) {
	  usernameTF.setText(usernameStrI.trim());
	}
        gbc.insets = new Insets(8,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),usernameTF,gbl,gbc,1,row,1,1);
        row++;
	
	//////////////////
	// ROW 4: Password
	//////////////////
	
        tempLabel = new JLabel("Password",javax.swing.SwingConstants.LEFT);
        gbc.insets = new Insets(8,25,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,row,1,1);
        
        passwordTF = new JPasswordField(15);
        passwordTF.setEchoChar('*');
	if (passwordStrI != null) {
	  passwordTF.setText(passwordStrI);
	}
	
        gbc.insets = new Insets(8,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),passwordTF,gbl,gbc,1,row,1,1);
        row++;
	
        ///////////////////////////////
	// ROW 5: OK and Cancel buttons
	///////////////////////////////
	
        // Put the buttons in a JPanel so they are all the same size
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,15,0));
        okButton = new JButton("OK");
        buttonPanel.add(okButton);
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        // Don't have the buttons resize if the dialog is resized
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipadx = 20;
        gbc.insets = new Insets(15,25,15,25);
        gbc.anchor = GridBagConstraints.CENTER;
        Utility.add(getContentPane(),buttonPanel,gbl,gbc,0,row,2,1);
        
        pack();
        
        // Add event listeners
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        addWindowListener(this);
        archiveTF.addActionListener(this);
	usernameTF.addActionListener(this);
        passwordTF.addActionListener(this);
        
        // Initialize the location of the dialog box
        setLocation(
	    Utility.centerRect(
		getBounds(),
		getParent().getBounds()));
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     LoadArchiveDialog.actionPerformed()
    //  Date:       January 2005
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Action callback method.
    //
    //  Modification History:
    //  01/24/2005  JPW  Add support for usernameTF callback
    //  01/07/2005  JPW  Created.
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void actionPerformed(ActionEvent event) {
        
        if (event.getSource() == cancelButton)
        {
            cancelAction();
        }
        
        else if ( (event.getSource() == archiveTF)  ||
		  (event.getSource() == usernameTF) ||
                  (event.getSource() == passwordTF) ||
                  (event.getSource() == okButton) )
        {
            okAction();
        }
        
        return;
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     LoadArchiveDialog.okAction()
    //  Date:       January 2005
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Store the archive name, username, and password.
    //
    //  Modification History:
    //  01/24/2005  JPW  Add username
    //  01/07/2005  JPW  Created.
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void okAction() {
        
	String tempArchiveStr = archiveTF.getText().trim();
	// Make sure the archive name is not blank
	if (tempArchiveStr.equals("")) {
	    JOptionPane.showMessageDialog(
		this,
		"Must enter an archive name.",
		"Archive Name Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	String tempUsernameStr = usernameTF.getText().trim();
	// NOTE: DO NOT TRIM PASSWORD FIELD (spaces may be part of password)
        String tempPasswordStr = new String(passwordTF.getPassword());
	
	// Pop up an error dialog if the username field is blank but the
	// password field is not blank
	if ( (!tempPasswordStr.equals("")) && (tempUsernameStr.equals("")) ) {
	    JOptionPane.showMessageDialog(
		this,
		"You have entered a password; you must also enter a username.",
		"Username/Password Error",
		JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	state = OK;
	archiveStr = archiveTF.getText().trim();
	usernameStr = tempUsernameStr;
	passwordStr = tempPasswordStr;
        setVisible(false);
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     LoadArchiveDialog.cancelAction()
    //  Date:       January 2005
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Pop down the dialog.
    //
    //  Modification History:
    //
    //  Modification History:
    //  01/24/2005  JPW     Set usernameStr to null
    //  01/07/2005  JPW     Created
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void cancelAction() {
        state = CANCEL;
        archiveStr = null;
	// JPW 01/24/2005: Add usernameStr
	usernameStr = null;
        passwordStr = null;
        setVisible(false);
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     LoadArchiveDialog.windowClosing()
    //  Date:       January 2005
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Respond to the event that occurs when the user clicks in the small
    //  "[x]" button on the right side of the title bar.
    //
    //  Modification History:
    //  01/07/2005  JPW  Created.
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void windowClosing(WindowEvent event) {
        cancelAction();
    }
    
    public void windowOpened(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    
}
