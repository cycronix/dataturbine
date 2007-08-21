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
import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

/////////////////////////////////////////////////////////////////////////////
//
//  Class:      UsernamePasswordDialog
//  Date:       June, 1998
//  Programmer: John P. Wilson
//  For:        Flyscan
//
//  Copyright 1998 Creare Inc, Hanover, N.H.
//  All rights reserved.
//
//  This is a simple dialog in which the user can enter a username and a
//  password.  The user must successfully input the password in duplicate.
//  This dialog has an OK and a Cancel button.
//
//  Modification History:
//  07/13/98    JPW     Add a boolean flag, "bConfirmPassword", that
//                      specifies whether to ask the user for a confirming
//                      password or not (ie: the user has to enter the
//                      password twice or not).
//  07/28/98    JPW     Got rid of the show() method which contained a call
//                      to super.show(). JRE 1.1.6 doesn't like this call
//                      occuring from within this dialog.
//  11/13/98	INB	Added sUsernameI field that specifies the username.
//			Only if this is null do we actually ask the user
//			to enter a username.
//  03/08/01	JPW	Added sPasswordI and bAllowBlankUsernameI fields in
//			the constructor.  Perform check on username in
//			okAction().
//  03/09/01	JPW	If user edits the username field, blank out the
//			password field(s).
//  03/20/2001  JPW	Add optional label text argument to constructor.
//
/////////////////////////////////////////////////////////////////////////////

public class UsernamePasswordDialog extends Dialog
                                    implements ActionListener,
					       TextListener,
					       WindowListener
{
    
    // GUI components
    private TextField username = null;
    private TextField password1 = null;
    private TextField password2 = null;
    private Button okButton = null;
    private Button cancelButton = null;
    
    public String usernameStr = null;
    public String passwordStr = null;
    public static final int OK = 1;
    public static final int CANCEL = 2;
    public int state = CANCEL;
    
    // JPW 7/13/1998
    private boolean bConfirmPassword = false;
    
    // JPW 3/8/2001
    private boolean bAllowBlankUsername = true;
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     UsernamePasswordDialog.UsernamePasswordDialog()
    //  Date:       June, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the UsernamePasswordDialog class.
    //
    //  Input :
    //  parentI			Parent frame for this dialog.
    //  modal			Should this dialog be modal?
    //  sUsernameI		Initialization string for the username field.
    //	sPasswordI		Initialization string for the password field.
    //	bConfirmPasswordI	Must the user verify (via double-entry) the
    //				    password?
    //	bAllowBlankUsernameI	Is a blank username allowed?
    //	bEnableUsernameI	Should the username field be editable?  In some
    //				    cases (for example, entering a password for
    //				    the SysAdmin account) this field should
    //				    not be editable.
    //	labelTextI		Text for an optional heading label.
    //
    //  Modification History:
    //  07/13/98    JPW     Added bConfirmPasswordI parameter.
    //	11/13/98    INB	    Added sUsernameI parameter.
    //  03/08/01    JPW	    Added sPasswordI, bAllowBlankUsernameI, and
    //			    bEnableUsername parameters.
    //	03/20/2001  JPW	    Add optional label text argument.
    //
    /////////////////////////////////////////////////////////////////////////
    
    public UsernamePasswordDialog(Frame parentI,
                                  boolean modal,
				  String sUsernameI,
				  String sPasswordI,
                                  boolean bConfirmPasswordI,
				  boolean bAllowBlankUsernameI,
				  boolean bEnableUsernameI,
				  String labelTextI)
    {
        
        super(parentI, modal);
        
        bConfirmPassword = bConfirmPasswordI;
	
	bAllowBlankUsername = bAllowBlankUsernameI;
        
        Label tempLabel;
        int row = 0;
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setBackground(Color.lightGray);
        
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 100;
        gbc.weighty = 100;
        
	// JPW 3/20/2001: Add optional label text
	if ( (labelTextI != null) && (!labelTextI.trim().equals("")) ) {
	    tempLabel = new Label(labelTextI,Label.CENTER);
            gbc.insets = new Insets(15,15,0,15);
            gbc.anchor = GridBagConstraints.CENTER;
            Utility.add(this,tempLabel,gbl,gbc,0,row,2,1);
	    row++;
	}
	
        tempLabel = new Label("User:",Label.LEFT);
        gbc.insets = new Insets(15,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        username = new TextField(10);
	username.setEditable(bEnableUsernameI);
	if (sUsernameI != null) {
	  username.setText(sUsernameI);
	}
        gbc.insets = new Insets(15,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(this,username,gbl,gbc,1,row,1,1);
        row++;
        
        tempLabel = new Label("Password:",Label.LEFT);
        gbc.insets = new Insets(5,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        password1 = new TextField(10);
        password1.setEchoChar('*');
	if (sPasswordI != null) {
	  password1.setText(sPasswordI);
	}
	
        gbc.insets = new Insets(5,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(this,password1,gbl,gbc,1,row,1,1);
        row++;
        
        if (bConfirmPassword) {
            
            tempLabel = new Label("Confirm Password:",Label.LEFT);
            gbc.insets = new Insets(5,15,0,5);
            gbc.anchor = GridBagConstraints.WEST;
            Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
            
            password2 = new TextField(10);
            password2.setEchoChar('*');
	    if (sPasswordI != null) {
		password2.setText(sPasswordI);
	    }
            gbc.insets = new Insets(5,0,0,15);
            gbc.anchor = GridBagConstraints.WEST;
            Utility.add(this,password2,gbl,gbc,1,row,1,1);
            row++;
            
        }
        
        // Put the buttons in a Panel so they are all the same size
        Panel buttonPanel = new Panel(new GridLayout(1,2,15,0));
        okButton = new Button("OK");
        buttonPanel.add(okButton);
        cancelButton = new Button("Cancel");
        buttonPanel.add(cancelButton);
        // Don't have the buttons resize if the dialog is resized
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipadx = 20;
        gbc.insets = new Insets(15,25,15,25);
        gbc.anchor = GridBagConstraints.CENTER;
        Utility.add(this,buttonPanel,gbl,gbc,0,row,2,1);
        
        setTitle("Name and password entry");
        
        pack();
        
        // Add event listeners
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        addWindowListener(this);
        username.addActionListener(this);
	// JPW 3/9/2001: If the user changes any text in the username field,
	//               blank out the password field(s)
	username.addTextListener(this);
        password1.addActionListener(this);
        if (bConfirmPassword) {
            password2.addActionListener(this);
        }
        
        // Initialize the location of the dialog box
	// JPW 4/15/2001: Call centerRect() instead of setLocation()
        setLocation(
	    Utility.centerRect(
		getBounds(),
		getParent().getBounds()));
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     UsernamePasswordDialog.textValueChanged()
    //  Date:       March 2001
    //  Programmer: John P. Wilson
    //  For:        EScan
    //
    //  Copyright 2001 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  When text is modified in the username TextField, blank out the
    //  password TextField(s).
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void textValueChanged(TextEvent e) {
	
	if (e.getSource() == username) {
	    password1.setText("");
	    if ( (bConfirmPassword) && (password2 != null) ) {
	        password2.setText("");
	    }
	}
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     UsernamePasswordDialog.actionPerformed()
    //  Date:       June, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  This is the action callback method for the UsernamePasswordDialog
    //  object.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void actionPerformed(ActionEvent event) {
        
        if (event.getSource() == cancelButton)
        {
            cancelAction();
        }
        
        else if ( (event.getSource() == username)  ||
                  (event.getSource() == password1) ||
                  (event.getSource() == password2) ||
                  (event.getSource() == okButton) )
        {
            okAction();
        }
        
        return;
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     UsernamePasswordDialog.okAction()
    //  Date:       June, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Check that the user has consistently entered the password. If they
    //  have, then store the username and password.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void okAction() {
        
        if (bConfirmPassword) {
            if (password1.getText().equals(password2.getText()) != true) {
                String[] strArray = new String[1];
                strArray[0] = "Password entries do not match.";
                InfoDialog infoDialog =
                    new InfoDialog(((Frame)this.getParent()),
                                   true,
                                   "Name and Password entry",
                                   strArray);
                infoDialog.show();
                infoDialog.dispose();
                return;
            }
        }
        
	String tempUsernameStr = username.getText().trim();
	// JPW 3/8/2001: If specified, check that the username is non-blank
	if (!bAllowBlankUsername && tempUsernameStr.equals("")) {
	    String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a non-blank username.");
	    InfoDialog infoDialog =
		new InfoDialog((Frame)(this.getParent()),
		               true,
		               "Username Error",
		               strArray);
	    infoDialog.show();
	    infoDialog.dispose();
            return;
	}
	
        state = OK;
        usernameStr = new String( username.getText() );
        passwordStr = new String( password1.getText() );
        setVisible(false);
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     UsernamePasswordDialog.cancelAction()
    //  Date:       June, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Pop down the dialog.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void cancelAction() {
        state = CANCEL;
        usernameStr = null;
        passwordStr = null;
        setVisible(false);
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     UsernamePasswordDialog.windowClosing()
    //  Date:       June, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Respond to the event that occurs when the user clicks in the small
    //  "[x]" button on the right side of the title bar.
    //
    //  Modification History:
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
