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
  *********************************************************************
  ***								    ***
  ***	Name :	ChannelDlg.java			        	    ***
  ***	By   :	U. Bersgtrom    (Creare Inc., Hanover, NH)          ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	October 2000            			    ***
  ***								    ***
  ***	Copyright 2000, 2004 Creare Inc.       			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class produces a dialog box that allows the user to ***
  ***	choose from a list of known channels.                       ***
  ***								    ***
  ***   Modifications:                                              ***
  ***   UCB - Dec 2000:     ChannelDlg modified to hi-lite          ***
  ***                       currently selected channels in list.    ***
  ***   UCB - 01/22/01:     Added "All" and "None" buttons, to      ***
  ***                       select and deselect all channels,       ***
  ***                       respectively.                           ***
  ***	JPW - 10/07/04:     Upgrade to RBNB V2 Player		    ***
  ***	JPW - 10/15/04:     Remove the longNamesCbox; the Player    ***
  ***			    output Source now uses folders, so      ***
  ***                       long names are no longer needed         ***
  ***								    ***
  *********************************************************************
*/

package com.rbnb.player;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

class ChannelDlg extends Dialog
  implements ActionListener,
	     ItemListener,
	     WindowListener {


  /* Public fields: */
  public String[]		chans = null;
  public boolean                useShortNames = true;        

  public int			state = CANCEL;

  /* Public constants: */
  public final static int       OK = 1,
				CANCEL = 2;

  /* Private fields: */
  private ChanList			chanList = null;
  private Checkbox                      rangeCbox = null;
  // JPW 10/15/2004: Remove the longNamesCbox; the Player output Source now
  //                 uses folders, so long names are no longer needed
  // private Checkbox                      longNamesCbox = null;
  private Frame                         parent = null;

/*
  *********************************************************************
  ***								    ***
  ***	Name :	ChannelDlg         				    ***
  ***	By   :	U. Bersgtrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	October, December 2000             		    ***
  ***								    ***
  ***	Copyright 2000 Creare Inc.       			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method constructs a channel dialog in the specified ***
  ***	parent frame, using the specified title, the specified	    ***
  ***	descriptive string, and list of channels.                   ***
  ***								    ***
  ***	Input :							    ***
  ***	   parent	    The parent frame.		            ***
  ***	   titleI	    The title for this dialog.	            ***
  ***	   descriptionI	    The description string.	            ***
  ***	   channelNames     Names of available chans.               ***
  ***	   selectedChans    Names of currently selected chans.      ***
  ***								    ***
  *********************************************************************
*/
  ChannelDlg(Frame parentI, String titleI, String descriptionI,
	     String[] channelNames, String[] selectedChans,
	     boolean useShortNamesI, boolean permitMultiple) {
    super(parentI, true);
    parent = parentI;
    useShortNames = useShortNamesI;
    setBackground(Color.lightGray);
    setSize(400, 400);

    Label  tempLabel;
    Button okButton,
	   cancelButton,
	   allButton,
	   noneButton;

    setFont(new Font("Dialog",Font.PLAIN,12));

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
        
    int row = 0;
        
    GridBagConstraints gbc = new GridBagConstraints();
        
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 0;
    gbc.weighty = 0;
    
    tempLabel = new Label(descriptionI);
    tempLabel.setAlignment(Label.CENTER);
    gbc.insets = new Insets(15,15,0,15);
    Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
    ++row;

    // 03/07/02 UCB - adding in a checkbox, to be used
    // as a kludge for now, to allow users on Linux to
    // select a range of channels without using the shift key.
    rangeCbox = new Checkbox("Select Range of Channels", false);
    gbc.insets = new Insets(15,15,0,15);
    Utility.add(this, rangeCbox, gbl, gbc, 0, row, 1, 1);
    if (!permitMultiple) {
	rangeCbox.setEnabled(false);
    }
    row++;

    chanList =
        new ChanList(
	    this, channelNames, selectedChans, permitMultiple, useShortNames);
    chanList.setFont(new Font("Dialog",Font.PLAIN,12));
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 100;
    gbc.insets = new Insets(15,15,0,15);
    Utility.add(this,chanList,gbl,gbc,0,row,1,10);
    row += 10;
    
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    gbc.weighty = 0;
            
    // 05/01/02 UCB - adding in a checkbox, to alow the user to 
    // select whether the short or long output channel names
    // are used by Player.
    // JPW 10/15/2004: Remove the longNamesCbox; the Player output Source
    //                 now uses folders, so long names are no longer needed
    /*
    longNamesCbox = new Checkbox("Output Long Channel Names", !useShortNames);
    gbc.insets = new Insets(15,15,0,15);
    gbc.anchor = GridBagConstraints.CENTER;
    Utility.add(this, longNamesCbox, gbl, gbc, 0, row, 1, 1);
    row++;
    */

    // Want to get the 4 bottom buttons the same size:
    // put both buttons in a panel which uses GridLayout (to force all
    // components to the same size) and then add the panel to the dialog
    gbc.anchor = GridBagConstraints.CENTER;

    Panel buttonPanel1 = new Panel(new GridLayout(1,2,10,5));
    allButton = new Button("All");
    allButton.setEnabled(permitMultiple);
    buttonPanel1.add(allButton);
    noneButton = new Button("None");
    noneButton.setEnabled(permitMultiple);
    buttonPanel1.add(noneButton);
    gbc.insets = new Insets(15,15,0,15);
    Utility.add(this,buttonPanel1,gbl,gbc,0,row,1,1);
    row++;

    Panel buttonPanel2 = new Panel(new GridLayout(1,2,10,5));
    okButton = new Button("OK");
    buttonPanel2.add(okButton);
    cancelButton = new Button("Cancel");
    buttonPanel2.add(cancelButton);
    gbc.insets = new Insets(5,15,10,15);
    Utility.add(this,buttonPanel2,gbl,gbc,0,row,1,1);

    setTitle(titleI);
        
    pack();
      
    // 03/06/02 UCB - changing ChannelDlg to be resizable.
    //setResizable(false);
    setResizable(true);
    allButton.addActionListener(this);
    noneButton.addActionListener(this);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
    rangeCbox.addItemListener(this);
    // JPW 10/15/2004: Remove the longNamesCbox; the Player output Source now
    //                 uses folders, so long names are no longer needed
    // longNamesCbox.addItemListener(this);
    addWindowListener(this);
        
    // Center the dialog inside the parent Frame.
    setLocation(Utility.centerRect(getBounds(), 
				   getParent().getBounds()));
        
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	actionPerformed					    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	October 2000            	      		    ***
  ***								    ***
  ***	Copyright 2000 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method handles the actionPerformed events for the   ***
  ***	OK, and cancel buttons.			                    ***
  ***								    ***
  ***	Input :							    ***
  ***	   eventI		    The event performed.	    ***
  ***								    ***
  *********************************************************************
*/
  public synchronized void actionPerformed(ActionEvent eventI) {
      if (eventI.getActionCommand().equals("All")) {
	  chanList.selectRange(0, chanList.getItemCount());

      } else if (eventI.getActionCommand().equals("None")) {
	  chanList.deselectRange(0, chanList.getItemCount());

      } else if (eventI.getActionCommand().equals("OK")) {
	  chans = chanList.getSelectedItems();
	  // JPW 10/15/2004: Remove the longNamesCbox; the Player output Source
	  //                 now uses folders, so long names are no longer
	  //                 needed
	  // useShortNames = !longNamesCbox.getState();
	  if ((chans == null) || (chans.length == 0)) {
	      state = CANCEL;
	      setVisible(false);
	  } else {
	      state = OK;
	      setVisible(false);
	  }
      } else if (eventI.getActionCommand().equals("Cancel")) {
	  state = CANCEL;
	  setVisible(false);
      }
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	itemStatChanged     (ItemListener interface)	    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  E-Scan II					    ***
  ***	Date :	March 2002               	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method is called when the user changes the state of ***
  ***   rangeCbox, the range-select Checkbox.                       ***
  ***								    ***
  ***	Input :							    ***
  ***	   itemEvent                                                ***
  ***								    ***
  *********************************************************************
*/
    public synchronized void itemStateChanged(ItemEvent itemEvent) {
	if (itemEvent.getSource() == rangeCbox) {
	    chanList.setSelectingRange(rangeCbox.getState());
	} else {
	    // JPW 10/15/2004: Remove the longNamesCbox; the Player output
	    //		       Source now uses folders, so long names are no
	    //		       longer needed
	    // useShortNames = !longNamesCbox.getState();
	    chanList.setUseShortNames(useShortNames, true);
	}
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	displayMsg                              	    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  E-Scan II					    ***
  ***	Date :	May 2002                 	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This public method displays a message box.               ***
  ***								    ***
  *********************************************************************
*/
    public void displayMsg(String[] msg, String title) {	
	InfoDialog infoDialog =
	    new InfoDialog(parent,
			   true,
			   new String(title),
			   msg);
	// center the dialog inside the parent Frame
	infoDialog.setLocation(Utility.centerRect(infoDialog.getBounds(),
						  parent.getBounds()));
	infoDialog.setVisible(true);
	infoDialog.dispose();
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	windowActionEvents				    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	July, 1999					    ***
  ***								    ***
  ***	Copyright 1999 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   These methods implement the window action listener	    ***
  ***	interface. The only thing we care about is a window close,  ***
  ***	which we treat as a cancel.				    ***
  ***								    ***
  ***	Input :							    ***
  ***	   eventI		    The window event.		    ***
  ***								    ***
  *********************************************************************
*/
  public void windowActivated(WindowEvent eventI) {}
  public void windowClosed(WindowEvent eventI) {}
  public void windowClosing(WindowEvent event) {
    state = CANCEL;
    setVisible(false);
  }
  public void windowDeactivated(WindowEvent eventI) {}
  public void windowDeiconified(WindowEvent eventI) {}
  public void windowIconified(WindowEvent eventI) {}
  public void windowOpened(WindowEvent eventI) {}
}
