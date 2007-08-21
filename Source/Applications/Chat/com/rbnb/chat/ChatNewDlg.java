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
  ***	Name :	ChatNewDlg		                        ***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001, 2003 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class is used by Chat to permit the user to	***
  ***   start a new chat host and open a connection to it.	***
  ***								***
  *****************************************************************
*/
package com.rbnb.chat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

public class ChatNewDlg extends Dialog
    implements ActionListener,
	       WindowListener {
    
    private TextField serverAddressTF = null;
    private TextField chatHostTF = null;
    private TextField usernameTF = null;
    private TextField passwordTF = null;
    private TextField cacheTF = null;
    private TextField archiveTF = null;

    public String serverAddress = "localhost:3333";
    public String chatHost = "host-chat";
    public String username = "";
    public String groupname = "room";
    public String password = "";
    public long cache = 1000;
    public long archive = 0;
    
    public int    state;
    public static final int OK = 1;
    public static final int CANCEL = 2;
    
    private InfoDialog infoDialog = null;

/*
  *****************************************************************
  ***								***
  ***	Name :	ChatNewDlg		                        ***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001, 2003 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
    public ChatNewDlg(Frame parentI,
		      String serverAddressI,
		      String chatHostI,
		      String user,
		      String grp,
		      long cacheI,
		      long archiveI) {

	super(parentI, true);
	setTitle("New Chat Session");
	setFont(new Font("Dialog", Font.PLAIN, 12));
	setBackground(Color.lightGray);

	if ((serverAddressI != null) && !serverAddressI.equals("")) {
	    serverAddress = serverAddressI;
	}
	if ((chatHostI != null) && !chatHostI.equals("")) {
	    chatHost = chatHostI;
	}
	if ((user != null) && !user.equals("")) {
	    username = user;
	}
	if ((grp != null) && !grp.equals("")) {
	    groupname = grp;
	}
	if (cacheI != 0) {
	    cache = cacheI;
	}
	archive = archiveI;

	Label tempLabel;
	Button OKbutton;
	Button Cancelbutton;
	
	GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 100;
        gbc.weighty = 100;

	int row = 0;
        
        gbc.anchor = GridBagConstraints.WEST;
        tempLabel = new Label("RBNB Server Address:");
        gbc.insets = new Insets(15,15,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        serverAddressTF = new TextField(serverAddress, 20);
        gbc.insets = new Insets(15,0,0,15);
        Utility.add(this,serverAddressTF,gbl,gbc,1,row,1,1);
        row++;
        
        tempLabel = new Label("Chat Host:");
        gbc.insets = new Insets(15,15,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        chatHostTF = new TextField(chatHost, 20);
        gbc.insets = new Insets(15,0,0,15);
        Utility.add(this,chatHostTF,gbl,gbc,1,row,1,1);
	row++;
        
	tempLabel = new Label("User Name:");
	gbc.insets = new Insets(15,15,0,5);
	Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
    
	usernameTF = new TextField(username, 20);
	gbc.insets = new Insets(15,0,0,15);
	Utility.add(this,usernameTF,gbl,gbc,1,row,1,1);
	row++;

	/*
//  	tempLabel = new Label("Group Name:");
//  	gbc.insets = new Insets(15,15,0,5);
//  	Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
    
//  	groupnameTF = new TextField(groupname, 20);
//  	gbc.insets = new Insets(15,0,0,15);
//  	Utility.add(this,groupnameTF,gbl,gbc,1,row,1,1);
//  	row++;
	*/
    
	tempLabel = new Label("Password:");
	gbc.insets = new Insets(15,15,0,5);
	Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
    
	passwordTF = new TextField(20);
	passwordTF.setEchoChar('*');
	gbc.insets = new Insets(15,0,0,15);
	Utility.add(this,passwordTF,gbl,gbc,1,row,1,1);
	row++;

	tempLabel = new Label("Cache:");
	gbc.insets = new Insets(15,15,0,5);
	Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
    
	cacheTF = new TextField("" + cache, 20);
	gbc.insets = new Insets(15,0,0,15);
	Utility.add(this,cacheTF,gbl,gbc,1,row,1,1);
	row++;

	tempLabel = new Label("Archive:");
	gbc.insets = new Insets(15,15,0,5);
	Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
    
	archiveTF = new TextField("" + archive, 20);
	gbc.insets = new Insets(15,0,0,15);
	Utility.add(this,archiveTF,gbl,gbc,1,row,1,1);
	row++;

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
        
        pack();
        setResizable(false);
        
        //////////////////////
        // ADD EVENT LISTENERS
        //////////////////////
	
        OKbutton.addActionListener(this);
        Cancelbutton.addActionListener(this);
        serverAddressTF.addActionListener(this);
        chatHostTF.addActionListener(this);
	usernameTF.addActionListener(this);
//  	groupnameTF.addActionListener(this);
	passwordTF.addActionListener(this);
        addWindowListener(this);
        
        // set up the text fields
        serverAddressTF.setSelectionStart(0);
        serverAddressTF.setSelectionEnd(0);
        serverAddressTF.setCaretPosition(0);
        
        chatHostTF.setSelectionStart(0);
        chatHostTF.setSelectionEnd(0);
        chatHostTF.setCaretPosition(0);
        
        // set focus in the serverAddressTF TextField
        serverAddressTF.requestFocus();
        
        // center the dialog inside the parent Frame
        setLocation(Utility.centerRect(getBounds(), 
				       getParent().getBounds()));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	actionPerformed          	                ***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001, 2003 Creare Inc.		       	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   ActionListener interface.                            ***
  ***      Responds to user action.                             ***
  ***								***
  *****************************************************************
*/
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals("OK")) {
            OKAction();

        } else if (evt.getActionCommand().equals("Cancel")) {
            cancelAction();

        } else if ((evt.getSource() == serverAddressTF) ||
                  (evt.getSource() == chatHostTF) ||
		   (evt.getSource() == usernameTF) ||
//  		  (evt.getSource() == groupnameTF) ||
		  (evt.getSource() == passwordTF) ||
		  (evt.getSource() == cacheTF) ||
		  (evt.getSource() == archiveTF)) {
            OKAction();
        }
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	OKAction                	                ***
  ***	By   :	U. Bergstrom					***
  ***		Ian Brown					***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001, 2003 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Validates the data entered by the user, sets state   ***
  ***   to OK, and hides the dialog.                            ***
  ***								***
  *****************************************************************
*/
    private void OKAction() {
        serverAddress = serverAddressTF.getText().trim();
	chatHost = chatHostTF.getText().trim();
	username = usernameTF.getText().trim();
//  	groupname = groupnameTF.getText().trim();
	password = passwordTF.getText();
	cache = Long.parseLong(cacheTF.getText());
	archive = Long.parseLong(archiveTF.getText());
        
        // make sure the user has entered a machine name
        if (serverAddress.equals("")) {
	    errorBox("RBNB Server Address Error",
		     "Must enter a valid RBNB server address.");
            return;
        }

	// make sure the user has entered a chat host.
        if (chatHost.equals("")) {
	    errorBox("Chat Host Error",
		     "Must enter a valid chat host.");
            return;
        }
        
	// make sure user has entered a user name
	if (username.equals("")) {
	    errorBox("User Name Error",
		     "Must enter a non-blank user name.");
            return;
	}

//  	// make sure user has entered a group name
//  	if (groupname.equals("")) {
//  	    errorBox("Group Name Error",
//  		     "Must enter a non-blank group name.");
//              return;
//  	}

        // Save the data entered by the user
        state = OK;
	
        setVisible(false);
	
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	cancelAction                	                ***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Sets state to CANCEL and hides the dialog.           ***
  ***								***
  *****************************************************************
*/
    private void cancelAction() {
        state = CANCEL;
        setVisible(false);
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	errorBox		                        ***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2001  				***
  ***								***
  ***	Copyright 2001 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Display an error box to the user.                    ***
  ***								***
  *****************************************************************
*/
    private void errorBox(String title, String msg) {
	String[] strArray = new String[1];
	strArray[0] = msg;
	infoDialog =
	    new InfoDialog((Frame)(this.getParent()),
			   true,
			   title,
			   strArray);
	    infoDialog.show();
	    infoDialog.dispose();
    }

    /////////////////////////////////////////////////////////////////////////
    //  Methods to implement the WindowListener interface
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
