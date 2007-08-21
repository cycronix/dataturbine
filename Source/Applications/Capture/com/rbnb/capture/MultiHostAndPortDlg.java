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


package com.rbnb.capture;

import java.awt.*;
import java.awt.event.*;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

/////////////////////////////////////////////////////////////////////////////
//
//  Class:      MultiHostAndPortDlg
//  Date:       April 2002
//  Programmer: U. C. Bergstrom
//  For:        E-Scan II
//
//  Copyright 2002 Creare Inc, Hanover, N.H.
//  All rights reserved.
//
//  This class defines a dialog box which prompts the user to enter
//  multiple Hosts and Ports for RBNB connections.
//
//  Modification History:
//  07/23/2004	    JPW	    Brought this class into the com.rbnb.capture
//				package; it was previously in
//				COM.Creare.Widgets
//
/////////////////////////////////////////////////////////////////////////////

public class MultiHostAndPortDlg extends Dialog
                               implements ActionListener,
					  WindowListener {
    
    public static final int OK = 1;
    public static final int CANCEL = 2;

    private TextArea hostsTA = null;

    private InfoDialog infoDialog = null;
    private boolean bEnableChecking = true;
    
    public String[] hosts = null;
    public int    state = CANCEL;
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     MultiHostAndPortDlg.MultiHostAndPortDlg()   (constructor)
    //  Date:       April, 2002
    //  Programmer: U. C. Bergstrom
    //  For:        E-Scan II
    //
    //  Copyright 2002 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public MultiHostAndPortDlg(Frame parentI,
			       String titleStrI,
			       String descriptionStrI,
			       String hostsIn[],
			       boolean enableCheckingI)
    {
        
        super(parentI, true);
	bEnableChecking = enableCheckingI;
	
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
        tempLabel = new Label("Hosts:");
        gbc.insets = new Insets(15,15,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        
        hostsTA = new TextArea(10, 30);
        gbc.insets = new Insets(15,0,0,15);
        Utility.add(this,hostsTA,gbl,gbc,1,row,1,1);
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
        
        setTitle(titleStrI);
        
        pack();
        
        setResizable(false);
        
        //////////////////////
        // ADD EVENT LISTENERS
        //////////////////////
	
        OKbutton.addActionListener(this);
        Cancelbutton.addActionListener(this);
        addWindowListener(this);
        
        ///////////////////////////////
        // Initialize the dialog fields
        ///////////////////////////////
        
        initDialog(hostsIn);
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     MultiHostAndPortDlg.actionPerformed
    //  Date:       April, 2002
    //  Programmer: U. C. Bergstrom
    //  For:        E-Scan II
    //
    //  Copyright 2002 Creare Inc, Hanover, N.H.
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
        } else if (evt.getActionCommand().equals("Cancel")) {
            cancelAction();
        }
        
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     MultiHostAndPortDlg.OKAction
    //  Date:       April 2002
    //  Programmer: U. C. Bergstrom
    //  For:        E-Scan II
    //
    //  Copyright 2002 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Check that the data entered by the user is valid before saving data
    //  and popping down the dialog.
    //
    /////////////////////////////////////////////////////////////////////////
    
    private void OKAction() {
        
	String packedHosts = hostsTA.getText();
	String[] unpackedHosts = Utility.unpack(packedHosts, "\n");

	if (bEnableChecking && 
	    (unpackedHosts.length == 1 && unpackedHosts[0].equals("")) ) {
	    String[] strArray = new String[1];
	    strArray[0] =
	        new String("User must enter at least one valid host name.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
		               true,
		               "Error",
		               strArray);
	    infoDialog.show();
	    infoDialog.dispose();
            return;
	}
	
        // Save the data entered by the user
        state = OK;
        hosts = unpackedHosts;
	
        setVisible(false);
	
    }
      
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     MultiHostAndPortDlg.cancelAction
    //  Date:       April, 2002
    //  Programmer: U. C. Bergstrom
    //  For:        Flyscan
    //
    //  Copyright 2002 Creare Inc, Hanover, N.H.
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
    //  Method:     MultiHostAndPortDlg.initDialog()
    //  Date:       April, 2002
    //  Programmer: U. C. Bergstrom
    //  For:        Flyscan
    //
    //  Copyright 2002 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Initialize data displayed to the user by filling dialog components.
    //
    /////////////////////////////////////////////////////////////////////////

    public synchronized void initDialog(String[] hostsIn) {
        
        // Initialize hostsTA
        if (hostsIn == null || hostsIn.length == 0) {
	    hostsTA.setText("localhost:3333\n");
	} else {
	    for (int i = 0; i < hostsIn.length; ++i) {
		hostsTA.append(hostsIn[i] + "\n");
	    }
	}
        
        // set focus on hostsTA
        hostsTA.requestFocus();
        
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
