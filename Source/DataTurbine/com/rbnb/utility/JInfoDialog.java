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


package com.rbnb.utility;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/////////////////////////////////////////////////////////////////////////////
//
//  Class:      JInfoDialog
//  Date:       March, 2005
//  Programmer: John P. Wilson
//  For:        
//
//  Copyright 2005 Creare Inc, Hanover, N.H.
//  All rights reserved.
//
//  This class defines a simple dialog box which displays any number of lines
//  of informational text and has an "OK" button control to pop down the
//  dialog box.
//
//  Modification History:
//  03/15/2005	JPW	Created.  Taken from InfoDialog
//
/////////////////////////////////////////////////////////////////////////////

public class JInfoDialog extends JDialog implements ActionListener {
    
    private JButton OKbutton;
    
    public final static int CENTER = 1;
    public final static int LEFT   = 2;
    private int alignment = CENTER;
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     JInfoDialog.JInfoDialog()     (constructor)
    //  Date:       March 2005
    //  Programmer: John P. Wilson
    //  For:        
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the Information Dialog. Use the default (CENTER)
    //  alignment.
    //
    //  Modification History:
    //  03/15/2005	JPW	Created
    //
    /////////////////////////////////////////////////////////////////////////
    
    public JInfoDialog(JFrame parent,
  	               boolean modal,
  	               String title,
  	               String[] strArray)
    {
	this(parent, modal, title, strArray, CENTER);
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     JInfoDialog.JInfoDialog()     (constructor)
    //  Date:       March 2005
    //  Programmer: John P. Wilson
    //  For:        
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the Information Dialog.
    //
    //  Modification History:
    //  03/15/2005	JPW	Created
    //
    /////////////////////////////////////////////////////////////////////////
    
    public JInfoDialog(JFrame parent,
                       boolean modal,
  	               String title,
  	               String[] strArrayI,
  	               int align)
    {
	
	super(parent, title, modal);
	
	String[] strArray;
	int	 strIdx = 0,
		 lastAt = 0,
		 newlineAt = 0;
	
	for (strIdx = 0; strIdx < strArrayI.length; ++strIdx) {
	    if ((newlineAt = strArrayI[strIdx].indexOf("\n")) != -1) {
		break;
	    }
	}
	
	if (strIdx == strArrayI.length) {
	    strArray = strArrayI;
	} else {
	    Vector strVector = new Vector(strArrayI.length + 1);
	    
	    for (int idx = 0; idx < strIdx; ++idx) {
		strVector.addElement(strArrayI[idx]);
	    }
	    
	    while (true) {
		strVector.addElement(
		    strArrayI[strIdx].substring(lastAt,newlineAt));
		if ( (lastAt = newlineAt + 1) >= strArrayI[strIdx].length()) {
		    if (++strIdx == strArrayI.length) {
			break;
		    }
		    lastAt = 0;
		}
		if ((newlineAt =
		     strArrayI[strIdx].indexOf("\n",lastAt)) == -1) {
		    newlineAt = strArrayI[strIdx].length();
		}
	    }
	    
	    strArray = new String[strVector.size()];
	    strVector.copyInto(strArray);
	}
	
	if ( (align != CENTER) && (align != LEFT) ) {
	    alignment = CENTER;
	}
	else {
	    alignment = align;
	}
	
	setFont(new Font("Dialog", Font.PLAIN, 12));
	setBackground(Color.lightGray);
	
	GridBagLayout gbl = new GridBagLayout();
	getContentPane().setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	
	if (alignment == CENTER) {
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.anchor = GridBagConstraints.CENTER;
	}
	else {
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.anchor = GridBagConstraints.WEST;
	}
	gbc.weightx = 100;
	gbc.weighty = 100;
	
	JLabel tempLabel;
	int row = 0;
	
	// specify padding around component
	gbc.insets = new Insets(10,20,0,20);
	for (; row < strArray.length; ++row) {
	    tempLabel =
	        new JLabel(strArray[row], SwingConstants.CENTER);
	    Utility.add(getContentPane(),tempLabel,gbl,gbc,0,row,1,1);
	}
	
	// Specify padding around component
	gbc.insets = new Insets(10,10,10,10);
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.ipadx = 8;
	OKbutton = new JButton("OK");
	Utility.add(getContentPane(),OKbutton,gbl,gbc,0,row,1,1);
	
	pack();
	
	setResizable(true);
	
	setLocationRelativeTo(parent);
	
	// Handle the close operation in the windowClosing() method of the
	// registered WindowListener object.  This will get around
	// JFrame's default behavior of automatically hiding the window when
	// the user clicks on the '[x]' button.
	this.setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	
	addWindowListener(new HandleWindowEvents());
	
	OKbutton.addActionListener(this);
	
	setVisible(true);
	
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     JInfoDialog.actionPerformed()
    //  Date:       March 2005
    //  Programmer: John P. Wilson
    //  For:        
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  This is the action callback method for the JInfoDialog object.
    //
    //  Modification History:
    // 03/15/2005	JPW	Created
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void actionPerformed(ActionEvent event) {
	if (event.getSource() == OKbutton) {
	    okAction();
	}
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     JInfoDialog.okAction()
    //  Date:       March 2005
    //  Programmer: John P. Wilson
    //  For:        
    //
    //  Copyright 2005 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  OK button has been clicked. Pop down the dialog box.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void okAction() {
	setVisible(false);
    }
    
    /**************************************************************************
     * Inner class that extends WindowAdapter to handle the user clicking on
     * the small '[x]' button on the window.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/15/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/15/2005  JPW  Created.
     *
     */
    
    class HandleWindowEvents extends WindowAdapter {
	public void windowClosing(WindowEvent event) {
	    okAction();
	}
    }
    
}
