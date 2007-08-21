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
  ***	Name :	GroupDlg.java			        	    ***
  ***	By   :	Ian Brown					    ***
  ***		Ursula Bergstrom				    ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	July, 1999/February 2000			    ***
  ***								    ***
  ***	Copyright 1999, 2000, 2003 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class produces a dialog box that allows the user to ***
  ***	choose from a list of known messaging groups to join,  or   ***
  ***   to enter a group that is not on the list.		    ***
  ***                                                               ***
  ***     Most of the code was borrowed from Admin's RoutingDialog, ***
  ***     written by INB in July 1999.                              ***
  ***								    ***
  *********************************************************************
*/
package com.rbnb.chat;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

class GroupDlg extends Dialog
  implements ActionListener,
	     ItemListener,
	     WindowListener {


  /* Public fields: */
  public String			groupName = null;
  public String                 oldGroupName = null;

  public int			state = CANCEL;

  /* Public constants: */
  public final static int       OK = 1,
				CANCEL = 2;

  /* Private fields: */
  private Choice		dtC = null;

  private TextField		dtT = null;

  private Frame			parent = null;

  private Client		mc = null;

  private boolean               isSecure = false;


/*
  *********************************************************************
  ***								    ***
  ***	Name :	GroupDlg         				    ***
  ***	By   :	Ian Brown					    ***
  ***		Ursula Bergstrom				    ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	July, 1999/February 2000		      	    ***
  ***								    ***
  ***	Copyright 1999, 2000, 2003 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method constructs a group dialog in the specified   ***
  ***	parent frame, using the specified title, the specified	    ***
  ***	descriptive string, MsgConnection, and current group name.  ***
  ***								    ***
  ***	Input :							    ***
  ***	   parentI	    The parent frame.		            ***
  ***	   titleI	    The title for this dialog.	            ***
  ***	   descriptionI	    The description string.	            ***
  ***	   mcI              The MsgConnection in use by the parent. ***
  ***	   currentGr        The parent's current group name.        ***
  ***								    ***
  *********************************************************************
*/
  GroupDlg(Frame parentI, String titleI, String descriptionI,
		 Client mcI, String currentGr, 
	   boolean isSecureI) {
    super(parentI, true);
    parent = parentI;
    setBackground(Color.lightGray);
    mc = mcI;
    isSecure = isSecureI;
    oldGroupName = currentGr;

    Label  tempLabel;
    Button okButton,
	   cancelButton;

    setFont(new Font("Dialog",Font.PLAIN,12));

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
        
    int row = 0;
        
    GridBagConstraints gbc = new GridBagConstraints();
        
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    gbc.weighty = 100;
        
    tempLabel = new Label(descriptionI);
    tempLabel.setAlignment(Label.CENTER);
    gbc.insets = new Insets(15,15,0,15);
    Utility.add(this,tempLabel,gbl,gbc,0,row,2,1);
    ++row;

    gbc.anchor = GridBagConstraints.WEST;
    tempLabel = new Label("Join Group:");
    gbc.insets = new Insets(15,15,0,5);
    Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);

    dtC = new Choice();
    dtC.setFont(new Font("Dialog",Font.PLAIN,12));
    gbc.fill = GridBagConstraints.BOTH;
    String groups[] = null;
    if (mc != null) {
      try {
	groups = mc.receiveRooms();
      } catch (Exception e) {
	e.printStackTrace();
	groups = new String[0];
	groups[0] = new String("An error has occurred");
      }
      for (int i = 0; i < groups.length; i++) {
	  dtC.add(groups[i]);
      }
      dtC.select(currentGr);
    } else {
      dtC.add(currentGr);
      dtC.select(currentGr);
    }
    gbc.insets = new Insets(15,0,0,15);
    Utility.add(this,dtC,gbl,gbc,1,row,1,10);
    row += 10;

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;
    tempLabel = new Label("New Group: ");
    gbc.insets = new Insets(15,15,0,5);
    Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);

    dtT = new TextField(32);
    dtT.setFont(new Font("Dialog",Font.PLAIN,12));
    if (currentGr != null) {
      dtT.setText(currentGr);
    }
    dtT.setEnabled(true);
    dtT.addActionListener(this);
    gbc.insets = new Insets(15,0,0,15);
    Utility.add(this,dtT,gbl,gbc,1,row,1,1);
    ++row;
            
    // Want to get the 2 bottom buttons the same size:
    // put both buttons in a panel which uses GridLayout (to force all
    // components to the same size) and then add the panel to the dialog
    Panel buttonPanel = new Panel(new GridLayout(1,2,15,5));
    okButton = new Button("OK");
    buttonPanel.add(okButton);
    cancelButton = new Button("Cancel");
    buttonPanel.add(cancelButton);
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(15,15,15,15);
    Utility.add(this,buttonPanel,gbl,gbc,0,row,2,1);
        
    setTitle(titleI);
        
    pack();
        
    setResizable(false);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
    dtC.addItemListener(this);
    addWindowListener(this);
        
    // Center the dialog inside the parent Frame.
    setLocation(Utility.centerRect(getBounds(), 
				   parent.getBounds()));
        
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	itemStateChanged				    ***
  ***	By   :	Ian Brown	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	July, 1999					    ***
  ***								    ***
  ***	Copyright 1999 Creare Inc.				    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method is called whenever the group name list	    ***
  ***	menu items are selected. It copies the selection into the   ***
  ***	text field.						    ***
  ***								    ***
  ***	Input :							    ***
  ***	   eventI		    The item event.		    ***
  ***								    ***
  *********************************************************************
*/
  public void itemStateChanged(ItemEvent eventI) {
      int change = eventI.getStateChange();
      String item = dtC.getSelectedItem();
      
      if (change == ItemEvent.SELECTED) {
	  dtT.setText(item);
      }
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	actionPerformed					    ***
  ***	By   :	INB/UCB  	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	July, 1999/February 2000	      		    ***
  ***								    ***
  ***	Copyright 1999, 2000 Creare Inc.			    ***
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
  public void actionPerformed(ActionEvent eventI) {
      if (eventI.getActionCommand().equals("Cancel")) {
	  state = CANCEL;
      }
      
      if ((eventI.getSource() instanceof TextField) ||
	  (eventI.getActionCommand().equals("OK"))) {
	  
	  groupName = dtT.getText().trim();

	  if (groupName.equals(oldGroupName)) {
	      state = CANCEL;

	  } else if (!groupName.equals("")) {
	      int idx = groupName.lastIndexOf("::");
	      if (idx != -1) {
		  groupName = groupName.substring(idx+2);
	      }
	      state = OK;

	  } else {
	      state = CANCEL;
	  }
      }

      setVisible(false);
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
