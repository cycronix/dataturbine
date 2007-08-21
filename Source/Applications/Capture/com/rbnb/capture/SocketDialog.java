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
  ***	Name :	SocketDialog		                	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class defines a dialog box which prompts the    ***
  ***          user to enter an input socket address and specify***
  ***          whether the socket is multicast.                 ***
  ***								***
  ***	Modification History :					***
  ***     02/22/02 UCB - If the output values do not differ     ***
  ***                    from the input values, OKAction will   ***
  ***                    set the state to CANCEL, not OK.       ***
  ***	  07/23/04 JPW - Brought this class into the new	***
  ***			    RBNB V2.4 compliant Capture package.***
  ***								***
  *****************************************************************
*/

package com.rbnb.capture;

import java.awt.*;
import java.awt.event.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

public class SocketDialog extends Dialog
  implements ActionListener, WindowListener {
  
  private TextField IPComponent;
  private TextField portComponent;
  
  private String socket;
  private String  port;
  public  int    state;
  public  String IPadd;
  
  public static final int OK = 1;
  public static final int CANCEL = 2;
  
  private InfoDialog infoDialog = null;
  private boolean validityChecks;

/*
  *****************************************************************
  ***								***
  ***	Name :	SocketDialog.SocketDialog()  (constructor)	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Constructor for SocketDialog.                         ***
  ***     parent: frame to attach dialog to                     ***
  ***     bEnableChecking: sets validity checking               ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/

  public SocketDialog(Frame parent,
		      String title,
		      String IPaddI,
		      boolean bEnableChecking) {
    super(parent, title, true);
    validityChecks = bEnableChecking;
    
    // Set up the strings and checkbox to reflect the
    // current state of the calling class.
    IPadd = IPaddI;
    int index = IPaddI.indexOf(':');
    if (index == -1) {
      socket = IPaddI;
      port = new String("");
    } else {
      socket = IPaddI.substring(0,index);
      port = IPaddI.substring(index+1);
    }
    
    Label tempLabel;
    Button OKButton, CancelButton;
    
    setFont(new Font("Dialog", Font.PLAIN, 12));
    
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.NONE;
    
    tempLabel =
	new Label(
	    "IP Address (enter \"0\" to not bind to a particular host):",
	    Label.CENTER);
    gbc.anchor = GridBagConstraints.EAST;
    buildConstraints(gbc, 0, 0, 1, 1, 35, 30);
    gbl.setConstraints(tempLabel, gbc);
    add(tempLabel);
    
    IPComponent = new TextField(socket, 20);
    gbc.anchor = GridBagConstraints.WEST;
    buildConstraints(gbc, 1, 0, 1, 1, 65, 0);
    gbl.setConstraints(IPComponent, gbc);
    add(IPComponent);
    
    tempLabel = new Label("Port Number:", Label.CENTER);
    gbc.anchor = GridBagConstraints.EAST;
    buildConstraints(gbc, 0, 1, 1, 1, 0, 30);
    gbl.setConstraints(tempLabel, gbc);
    add(tempLabel);
    
    portComponent = new TextField(port, 8);
    gbc.anchor = GridBagConstraints.WEST;
    buildConstraints(gbc, 1, 1, 1, 1, 0, 0);
    gbl.setConstraints(portComponent, gbc);
    add(portComponent);
    
    OKButton = new Button("  OK  ");
    gbc.anchor = GridBagConstraints.EAST;
    buildConstraints(gbc, 0, 2, 1, 1, 0, 40);
    gbl.setConstraints(OKButton, gbc);
    add(OKButton);
    
    CancelButton = new Button("Cancel");
    gbc.anchor = GridBagConstraints.CENTER;
    buildConstraints(gbc, 1, 2, 1, 1, 0, 0);
    gbl.setConstraints(CancelButton, gbc);
    add(CancelButton);
    
    pack();
    setResizable(false);
    
    OKButton.addActionListener(this);
    CancelButton.addActionListener(this);
    addWindowListener(this);
    
    // center the dialog inside the parent Frame
    Rectangle bounds = getParent().getBounds();
    Rectangle abounds = getBounds();
    setLocation(Utility.centerRect(abounds, bounds));
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SocketDialog.buildConstraints()         	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Utility for setting up GridBagContraints.             ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/

  private void buildConstraints(GridBagConstraints gbc, int gx, int gy,
				int gw, int gh, int wx, int wy) {
    gbc.gridx = gx;
    gbc.gridy = gy;
    gbc.gridwidth = gw;
    gbc.gridheight = gh;
    gbc.weightx = wx;
    gbc.weighty = wy;
  }


/*
  *****************************************************************
  ***								***
  ***	Name :	SocketDialog.actionPerformed()          	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Handles all dialog box actions.                       ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/

  public void actionPerformed(ActionEvent evt) {
    if (evt.getActionCommand().equals("  OK  ")) { 
      OKAction(); 
    } else if (evt.getActionCommand().equals("Cancel")) { 
      cancelAction(); 
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SocketDialog.OKAction()                 	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Check validity of data, set IPadd, and hide dialog.   ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/

  private void OKAction() {
    socket = IPComponent.getText().trim();
    port = portComponent.getText().trim();

    if (validityChecks) {  
      // First check that neither entry has been left blank.
      if (socket.equals("")) {
	IPError("User must enter a valid IP address.");
	return;
      }
      if (port.equals("")) {
	portError();
	return;
      }
      
      // Test that the IP socket entry is a valid IP address.
      // JPW 05/04/2005: If socket = "0", this is a special case
      //                 indicating that we won't bind to a particular address
      if (!socket.equals("0")) {
	  try {
	      InetAddress testAdd = InetAddress.getByName(socket);
	  } catch (UnknownHostException uhe) {
	      IPError(socket + " is an unknown host.");
	      return;
	  }
      }
      
      // Now test that the port number is an integer
      try {
	int tester = Integer.parseInt(port);
      } catch (NumberFormatException e) {
	portError();
	return;
      }
      
    } // if(validityChecks)

    // If no actual change has been made from the input IP address,
    // the state should be CANCEL.
    if (IPadd.equals(socket + ":" + port)) {
	state = CANCEL;
    } else {
	state = OK;
	IPadd = socket + ':' + port;
    }
    
    setVisible(false);
    
  } // OKAction()

/*
  *****************************************************************
  ***								***
  ***	Name :	SocketDialog.cancelAction()                 	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  User has hit cancel button or closed dialog box.      ***
  ***								***
  ***	Modification History :					***
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
  ***	Name :	SocketDialog.IPError()                  	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Creates a dialog box informing user that the IP       ***
  ***     address  is in error.                                 ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/

  private void IPError(String ipErr) {
    String[] strArray = new String[1];
    strArray[0] = ipErr;
    infoDialog =
      new InfoDialog((Frame)(this.getParent()),
		     true,
		     "IP Error",
		     strArray);
    infoDialog.show();
    infoDialog.dispose();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SocketDialog.portError()                 	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Creates a dialog box informing user that the port     ***
  ***     number is in error.                                   ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/

  private void portError() {
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
  
  
} // SocketDialog

