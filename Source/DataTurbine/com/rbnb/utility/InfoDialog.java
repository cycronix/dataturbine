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


// moved from RBNBAdmin to Utility, 9/8/98 EMF
package com.rbnb.utility;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

/////////////////////////////////////////////////////////////////////////////
//
//  Class:      InfoDialog
//  Date:       April, 1998
//  Programmer: John P. Wilson
//  For:        Flyscan
//
//  Copyright 1998 Creare Inc, Hanover, N.H.
//  All rights reserved.
//
//  This class defines a simple dialog box which displays any number of lines
//  of informational text and has an "OK" button control to pop down the
//  dialog box.
//
//  Modification History:
//  07/28/98    JPW     Got rid of the show() method which contained a call
//                      to super.show(). JRE 1.1.6 doesn't like this call
//                      occuring from within this dialog.
//  08/30/2000	INB	Added code to search through the input string array
//			to locate embedded newlines. The code now rebuilds
//			the array to split lines at those points.
//
/////////////////////////////////////////////////////////////////////////////

public class InfoDialog extends Dialog implements ActionListener,
                                                  WindowListener
{
	
	private Button OKbutton;
	
	public final static int CENTER = 1;
	public final static int LEFT   = 2;
	private int alignment = CENTER;
	
   /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     InfoDialog.InfoDialog()     (constructor)
    //  Date:       April, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the Information Dialog. Use the default (CENTER)
    //  alignment.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////

  	public InfoDialog(Frame parent,
  	                  boolean modal,
  	                  String title,
  	                  String[] strArray)
  	{
  	    this(parent, modal, title, strArray, CENTER);
  	}
	
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     InfoDialog.InfoDialog()     (constructor)
    //  Date:       April, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Constructor for the Information Dialog.
    //
    //  Modification History:
    //    08/30/2000	INB	Renamed strArray input parameter to strArrayI
    //				and added code to build a new strArray that
    //				doesn't contain any newlines.
    //
    /////////////////////////////////////////////////////////////////////////
    
  	public InfoDialog(Frame parent,
  	                  boolean modal,
  	                  String title,
  	                  String[] strArrayI,
  	                  int align)
  	{
		

	    super(parent, modal);

		String[] strArray;
		int	 strIdx,
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
			strVector.addElement
			    (strArrayI[strIdx].substring(lastAt,newlineAt));
			if ((lastAt = newlineAt + 1) >=
			    strArrayI[strIdx].length()) {
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
		
		// JPW 03/19/98: add alignment
		if ( (align != CENTER) && (align != LEFT) ) {
		    alignment = CENTER;
		}
		else {
		    alignment = align;
		}
		
		setFont(new Font("Dialog", Font.PLAIN, 12));
		setBackground(Color.lightGray);
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		// JPW 03/19/98: add alignment
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
		
		Label tempLabel;
		int row = 0;
		
		// specify padding around component
		gbc.insets = new Insets(10,20,0,20);
		for (; row < strArray.length; ++row) {
			tempLabel = new Label(strArray[row]);
			tempLabel.setAlignment(Label.CENTER);
			Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
		}
		
		// Specify padding around component
		gbc.insets = new Insets(10,10,10,10);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.ipadx = 8;
		OKbutton = new Button("OK");
		Utility.add(this,OKbutton,gbl,gbc,0,row,1,1);
		
		pack();
		
		setTitle(title);

		// Add event listeners
		this.addWindowListener(this);
		OKbutton.addActionListener(this);
		
		// Initialize the location of the dialog
		Rectangle parentBounds  = getParent().getBounds();
    	Rectangle infoBounds    = getBounds();

	// 02/28/02 UCB - use com.rbnb.utility.Utility.centerRect() to
	// set the location of the infoBox, to avoid it being displayed
	// partially off-screen.
	setLocation(Utility.centerRect(infoBounds, parentBounds));
//      	setLocation(
//      		parentBounds.x + (parentBounds.width  - infoBounds.width)/2,
//      		parentBounds.y + (parentBounds.height - infoBounds.height)/2);
		
	}
	
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     InfoDialog.actionPerformed()
    //  Date:       April, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  This is the action callback method for the InfoDialog object.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
	
	public void actionPerformed(ActionEvent event) {
	    
		if (event.getSource() == OKbutton) {
			okAction();
		}
		
	}
	
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     InfoDialog.okAction()
    //  Date:       April, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
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
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     InfoDialog.windowClosing()
    //  Date:       April, 1998
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1998 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Respond to the event that occurs when the user clicks in the small
    //  "[x]" button on the right side of the title bar.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
	
	public void windowClosing(WindowEvent event) {
        okAction();
    }
    public void windowOpened(WindowEvent event) {}
	public void windowActivated(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    
}
