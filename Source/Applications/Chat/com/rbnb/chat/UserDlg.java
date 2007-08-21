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
  ***	Name :	UserDlg.java			        	    ***
  ***	By   :	INB/UCB  	    (Creare Inc., Hanover, NH)      ***
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
  ***	September 2001 - UCB: Altered UserDlg so that user cannot   ***
  ***                    change their name while in a secure        ***
  ***                    connection.                                ***
  ***								    ***
  *********************************************************************
*/
package com.rbnb.chat;

import java.awt.Button;
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
import java.awt.TextArea;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.rbnb.utility.HostAndPortDialog;
import com.rbnb.utility.Utility;

class UserDlg extends Dialog
  implements ActionListener,
	     WindowListener {


  /* Public fields: */
  public String			userName = null,
                                oldUserName = null,
                                group = null;

  public int			state = CANCEL;

  /* Public constants: */
  public final static int       OK = 1,
				CANCEL = 2;

  /* Private fields: */
  private TextArea		unA = null;

  private TextField		dtT = null;

  private Client		mc;

  private boolean               isSecure = false;


/*
  *********************************************************************
  ***								    ***
  ***	Name :	UserDlg         				    ***
  ***	By   :	INB/UCB  	    (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	July, 1999/February 2000		      	    ***
  ***								    ***
  ***	Copyright 1999, 2000 Creare Inc.			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method constructs a user dialog in the specified    ***
  ***	parent frame, using the specified title, the specified	    ***
  ***	descriptive string, MsgConnection, and current user name.   ***
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
  UserDlg(Frame parentI, String titleI, String descriptionI,
	  Client mcI, String currentU, String groupI,
	  boolean isSecureI) {

    // super(parentI, false);
    super(parentI, true);
    setBackground(Color.lightGray);

    group = groupI;
    oldUserName = currentU;
    isSecure = isSecureI;
    mc = mcI;

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
    tempLabel = new Label("Users in this group:");
    gbc.insets = new Insets(15,15,0,5);
    Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);

    unA = new TextArea(10, 20);
    unA.setEditable(false);
    unA.setFont(new Font("Dialog",Font.PLAIN,12));
    gbc.fill = GridBagConstraints.BOTH;
    String users[] = null;
    if (mc != null) {
      try {
	users = mc.receiveUsers();
      } catch (Exception e) {
	e.printStackTrace();
	users = new String[1];
	users[0] = new String("An error has occured");
      }
      for (int i = 0; i < users.length; i++) {
	unA.append(users[i] + "\n");
      }

    }
    gbc.insets = new Insets(15,0,0,15);
    Utility.add(this,unA,gbl,gbc,1,row,1,10);
    row += 10;

    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;
    tempLabel = new Label((isSecure ? "Current user name: " : 
			  "Specify a new user name: ") +
			  mc.getServerName() + "/");
    gbc.insets = new Insets(15,15,0,5);
    Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);

    dtT = new TextField(32);
    dtT.setFont(new Font("Dialog",Font.PLAIN,12));
    if (currentU != null) {
      dtT.setText(currentU);
    }
    dtT.setEnabled(!isSecure);
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
        
    setResizable(true);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
    addWindowListener(this);
        
    // Center the dialog inside the parent Frame.
    setLocation(Utility.centerRect(getBounds(), 
				   getParent().getBounds()));
        
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

      // 09/28/01 - UCB
      // firewall for secure connection
      if (isSecure) {
	  state = CANCEL;
	  setVisible(false);
	  return;
      }

      if (eventI.getActionCommand().equals("Cancel")) {
	  state = CANCEL;
      }

      if ((eventI.getSource() instanceof TextField) ||
	  (eventI.getActionCommand().equals("OK"))) {
	  
	  userName = dtT.getText().trim();

	  if (userName.equals(oldUserName)) {
	      state = CANCEL;

	  } else if ((userName != null) && (!userName.equals(""))) {
	      int idx = userName.lastIndexOf("/");
	      userName = userName.substring(idx + 1);
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
