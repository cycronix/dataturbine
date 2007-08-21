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
  ***	Name :	DataSettingsDialog    	                	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	December, 1999                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class defines a dialog box which prompts the    ***
  ***          user to enter the max number of data frames to   ***
  ***          store in the DataTurbine cache, and the max no.  ***
  ***          of data frames to store in the DataTurbine       ***
  ***          archive.                                         ***
  ***								***
  ***	Modification History :					***
  ***	  02/22/02 UCB - DataSettingsDialog no longer has       ***
  ***                    fields or methods to handle setting    ***
  ***                    the desired frames/second data output  ***
  ***                    rate for Capture.                      ***
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

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

public class DataSettingsDialog extends Dialog
                               implements ActionListener, WindowListener {

  private TextField cacheComponent;
  private TextField archComponent;

  private int origCache;
  private int origArch;

  public int cacheFrames;
  public int archFrames;

  public int state;
  public static final int OK = 1;
  public static final int CANCEL = 2;

  private InfoDialog infoDialog = null;
  private boolean validityChecks;

/*
  *****************************************************************
  ***								***
  ***	Name :	DataSettingsDialog.TargetDialog() (constructor)	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Constructor for DataSettingsDialog.                   ***
  ***     parent: frame to attach dialog to                     ***
  ***     cacheI: the current max cache storage in frames       ***
  ***     archI: the current max archive storage in frames      ***
  ***     rateI: the current transfer rate in frames/sec        ***
  ***     bEnableChecking: sets validity checking               ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
    public DataSettingsDialog(Frame parent, int cacheI, int archI, 
			      boolean bEnableChecking) {
	super(parent, "Settings For Data Storage", true);
	origCache = cacheFrames = cacheI;
	origArch = archFrames = archI;
	validityChecks = bEnableChecking;
	
	Label tempLabel;
	Button OKButton, CancelButton;
	
	setFont(new Font("Dialog", Font.PLAIN, 12));
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	
	tempLabel = new Label("Max number of frames to store in cache:", Label.CENTER);
	gbc.anchor = GridBagConstraints.EAST;
	buildConstraints(gbc, 0, 0, 1, 1, 30, 24);
	gbl.setConstraints(tempLabel, gbc);
	add(tempLabel);
	
	cacheComponent = new TextField(Integer.toString(cacheFrames),12);
	gbc.anchor = GridBagConstraints.WEST;
	buildConstraints(gbc, 1, 0, 1, 1, 70, 0);
	gbl.setConstraints(cacheComponent, gbc);
	add(cacheComponent);
	
	tempLabel = new Label("Max number of frames to archive:", Label.CENTER);
	gbc.anchor = GridBagConstraints.EAST;
	buildConstraints(gbc, 0, 1, 1, 1, 0, 24);
	gbl.setConstraints(tempLabel, gbc);
	add(tempLabel);
	
	archComponent = new TextField(Integer.toString(archFrames), 12);
	gbc.anchor = GridBagConstraints.WEST;
	buildConstraints(gbc, 1, 1, 1, 1, 0, 0);
	gbl.setConstraints(archComponent, gbc);
	add(archComponent);
	
	OKButton = new Button("  OK  ");
	gbc.anchor = GridBagConstraints.EAST;
	buildConstraints(gbc, 0, 3, 1, 1, 0, 28);
	gbl.setConstraints(OKButton, gbc);
	add(OKButton);
	
	CancelButton = new Button("Cancel");
	gbc.anchor = GridBagConstraints.CENTER;
	buildConstraints(gbc, 1, 3, 1, 1, 0, 0);
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
  ***	Name :	DataSettingsDialog.buildConstraints()         	***
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
  ***	Name :	DataSettingsDialog.actionPerformed()          	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
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
  ***	Name :	DataSettingsDialog.OKAction()                 	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Check validity of data, set member ints, and hide     ***
  ***         dialog.                                           ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
  private void OKAction() {
      String cacheStr = cacheComponent.getText().trim();
      String archStr = archComponent.getText().trim();

      int test;

      if (validityChecks) {  
	  // Check that cache textfield is not blank
	  // a blank archive value indicates archiving is off.
	  if (cacheStr.equals("")) {
	      cacheError();
	      return;
	  }
      } // if(validityChecks)

      // a blank or zero archive entry indicates archiving is off.
      if (archStr.equals("")) {
	  archStr = "0";
      }

      // Check that both entries contain integers.
      try {
	  cacheFrames = Integer.parseInt(cacheStr);
      } catch (NumberFormatException e) {
	  cacheError();
	  return;
      }
      try {
	  archFrames = Integer.parseInt(archStr);
      } catch (NumberFormatException e) {
	  archError();
	  return;
      }

      // If the outputs don't differ from the input values,
      // the state should be CANCEL.
      if (cacheFrames == origCache &&
	  archFrames == origArch) {
	  state = CANCEL;
      } else {
	  state = OK;
      }

      setVisible(false);

  } // OKAction()

/*
  *****************************************************************
  ***								***
  ***	Name :	DataSettingsDialog.cancelAction()             	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 1999                   		***
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
  ***	Name :	DataSettingsDialog.cacheError()               	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Creates a dialog box informing user that the cache    ***
  ***     storage entry is in error.                            ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
  private void cacheError() {
      String[] strArray = new String[1];
      strArray[0] =
	  new String("Must enter a valid number of frames to store in the cache.");
      infoDialog =
	  new InfoDialog((Frame)(this.getParent()),
			 true,
			 "Cache Setting Error",
			 strArray);
      infoDialog.show();
      infoDialog.dispose();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	DataSettingsDialog.archError()                 	***
  ***	By   :	Ursula Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	RBNB						***
  ***	Date :	January, 2000                   		***
  ***								***
  ***	Copyright 1999 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  Creates a dialog box informing user that the archive  ***
  ***     storage entry is in error.                            ***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
  private void archError() {
      String[] strArray = new String[1];
      strArray[0] =
	  new String("Must enter a valid number of frames to archive.");
      infoDialog =
	  new InfoDialog((Frame)(this.getParent()),
			 true,
			 "Archive Setting Error",
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

} // DataSettingsDialog


























