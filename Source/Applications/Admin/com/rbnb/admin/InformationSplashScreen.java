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
import java.io.*;

import com.rbnb.utility.Utility;

/******************************************************************************
 * A simple class which extends Dialog; used to display a message to the user.
 * <p>
 * There are no buttons or other controls on this Dialog.
 * <p>
 *
 * @author John P. Wilson
 *
 * @since V2.0
 * @version 02/15/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/15/2002  JPW	Created.
 *
 */

public class InformationSplashScreen extends Dialog {
    
    /**************************************************************************
     * Create the information dialog.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param parent  parent frame
     * @param textForLabel  the text to display on the dialog
     * @since V2.0
     * @version 02/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/15/2002  JPW  Created.
     *
     */
    
    public InformationSplashScreen(Frame parent, String textForLabel) {
	
	super(parent,false);
	
	setFont(new Font("Dialog", Font.BOLD, 12));
	setBackground(Color.lightGray);
        
        GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 100;
	gbc.weighty = 100;
	
	GridBagLayout panelgbl = new GridBagLayout();
	GridBagConstraints panelgbc = new GridBagConstraints();
	panelgbc.fill = GridBagConstraints.BOTH;
	panelgbc.anchor = GridBagConstraints.CENTER;
	panelgbc.weightx = 100;
	panelgbc.weighty = 100;
	
	Panel panel = new Panel(panelgbl);
	panel.setBackground(Color.lightGray);
	
	// NOTE: Can't put any space around the label (any space around the
	//       label shows up transparent!!).  Therefore, "make" some space
	//       by adding some blank labels and pad with whitespaces.
	panelgbc.insets = new Insets(0,0,0,0);
	
	Label label = new Label("   ", Label.CENTER);
	Utility.add(panel,label,panelgbl,panelgbc,0,0,1,1);
	
	String tempStr = "   " + textForLabel + "   ";
	label = new Label(tempStr, Label.CENTER);
	Utility.add(panel,label,panelgbl,panelgbc,0,1,1,1);
	
	label = new Label("   ", Label.CENTER );
	Utility.add(panel,label,panelgbl,panelgbc,0,2,1,1);
	
	Utility.add(this,panel,gbl,gbc,0,0,1,1);
        
        setTitle("Information");
        
        pack();
	
	// Center the dialog within the bounds of the parent frame.
	setLocation(
	    Utility.centerRect(
		getBounds(),
		getParent().getBounds()));
        
    }
}
