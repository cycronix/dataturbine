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
  ***	Name :	TargetDialog		                	***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class defines a dialog box which prompts the    ***
  ***          user to enter a data storage path and channel    ***
  ***          name.                                            ***
  ***								***
  ***	Modification History :					***
  ***     02/22/02 UCB - If the output values do not differ     ***
  ***                    from the input values, OKAction will   ***
  ***                    set the state to CANCEL, not OK.       ***
  ***     04/05/02 UCB - TargetDialog now deals with arrays of  ***
  ***                    channels and datapaths, rather than a  ***
  ***                    single one of each.                    ***
  ***     04/25/02 UCB - TargetDialog now back to dealing with  ***
  ***                    a single channel and datapath, not     ***
  ***                    arrays of them.                        ***
  ***	  07/23/04 JPW - Brought this class into the new	***
  ***			    RBNB V2.4 compliant Capture package.***
  ***								***
  *****************************************************************
*/
package com.rbnb.capture;

import java.awt.*;
import java.awt.event.*;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

public  class TargetDialog extends Dialog
    implements ActionListener, WindowListener {
    
    public static final int OK = 1;
    public static final int CANCEL = 2;
    
    private TextField pathComponent;
    private TextField chanComponent;
    
    public String path;
    public String chan;
    public int state = CANCEL;
    
    private InfoDialog infoDialog = null;
    private boolean validityChecks = true;

/*
  *****************************************************************
  ***								***
  ***	Name :	TargetDialog.TargetDialog()  (constructor)	***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Constructor for TargetDialog.                         ***
  ***     parent: frame to attach dialog to                     ***
  ***     pathI: the current data path                          ***
  ***     chanI: the current data channel                       ***
  ***     bEnableChecking: sets validity checking               ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
    public TargetDialog(Frame parent, 
			String pathI, 
			String chanI,
			boolean bEnableChecking) {
	super(parent, "Target Path and Channel For Data Storage", true);
	validityChecks = bEnableChecking;
	path = (pathI == null ? Capture.DEFAULT_PATH : pathI);
	chan = (chanI == null ? Capture.DEFAULT_CHANNEL : chanI);
	
	Label tempLabel;
	Button OKButton, CancelButton;
	
	setFont(new Font("Dialog", Font.PLAIN, 12));
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	
	tempLabel = new Label("Path for Data Storage:", Label.CENTER);
	gbc.anchor = GridBagConstraints.EAST;
	Utility.add(this, tempLabel, gbl, gbc, 0, 0, 1, 1);
	
	pathComponent = new TextField(path, 40);
	gbc.anchor = GridBagConstraints.WEST;
	Utility.add(this, pathComponent, gbl, gbc, 1, 0, 1, 1);
	
	tempLabel = new Label("Storage Channel Name:", Label.CENTER);
	gbc.anchor = GridBagConstraints.EAST;
	Utility.add(this, tempLabel, gbl, gbc, 0, 1, 1, 1);
	
	chanComponent = new TextField(chan, 40);
	gbc.anchor = GridBagConstraints.WEST;
	Utility.add(this, chanComponent, gbl, gbc, 1, 1, 1, 1);
	
	OKButton = new Button("  OK  ");
	gbc.anchor = GridBagConstraints.EAST;
	Utility.add(this, OKButton, gbl, gbc, 0, 2, 1, 1);
	
	CancelButton = new Button("Cancel");
	gbc.anchor = GridBagConstraints.CENTER;
	Utility.add(this, CancelButton, gbl, gbc, 1, 2, 1, 1);
	
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
  ***	Name :	TargetDialog.actionPerformed()          	***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
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
  ***	Name :	TargetDialog.OKAction()                 	***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000/April, 2002             		***
  ***								***
  ***	Copyright 2000, 2002 Creare Inc.	      		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Check validity of data, set strings, and hide dialog. ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
    private void OKAction() {
	String newPath = pathComponent.getText().trim();
	String newChan = chanComponent.getText().trim();

	// Check whether the current values differ from the originals.
	boolean noChanges = true;

	if (newPath == null || !newPath.equals(path) ||
	    newChan == null || !newChan.equals(chan)) {
	    noChanges = false;
	}

	// If no changes to the inputs have been made, state is CANCEL.
	if (noChanges) {
	    state = CANCEL;
	    setVisible(false);
	    return;
	} 

	if (validityChecks) {
	    // Check that the channel entry has not been left blank.
	    if (newChan == null || newChan.equals("")) {
		chanError();
		return;
	    }
	    
	    path = newPath;
	    chan = newChan;
	    state = OK;
	}
	setVisible(false);
	
    } // OKAction()

/*
  *****************************************************************
  ***								***
  ***	Name :	TargetDialog.cancelAction()                 	***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
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
  ***	Name :	TargetDialog.chanError()                 	***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999/April, 2002                   	***
  ***								***
  ***	Copyright 1999, 2002 Creare Inc.	        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Creates a dialog box informing user that the channel  ***
  ***     name is in error.                                     ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
    private void chanError() {
	String[] strArray = new String[1];
	strArray[0] =
	    new String("The user must enter a data storage channel name.");
	infoDialog =
	    new InfoDialog((Frame)(this.getParent()),
			   true,
			   "Channel Error",
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
    
} // TargetDialog

























