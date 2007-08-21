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

package com.rbnb.chat;

/**
 * Confirmation dialog class.
 * <p>
 * This class can be used to confirm or reject an action to be taken.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/13/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/13/2003  INB	Created.
 *
 */
final class ConfirmDialog
    extends java.awt.Dialog
    implements java.awt.event.ActionListener,
	       java.awt.event.WindowListener
{
    /**
     * Was the action confirmed?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/13/2003
     */
    public boolean confirmed = false;

    /**
     * The OK button.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/13/2003
     */
    private java.awt.Button okButton = null;

    /**
     * The cancel button.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/13/2003
     */
    private java.awt.Button cancelButton = null;

    /**
     * Class constructor to build a <code>ConfirmDialog</code> from parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI the parent <code>Frame</code>.
     * @param modalI  is this dialog modal?
     * @param titleI  the dialog title.
     * @param textI   the dialog text - may contain new lines.
     * @param alignI  the text alignment.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public ConfirmDialog(java.awt.Frame parentI,
			 boolean modalI,
			 String titleI,
			 String textI,
			 float alignI)
    {
	super(parentI,modalI);
	setFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
	setBackground(java.awt.Color.lightGray);
	setTitle(titleI);

	java.awt.GridBagLayout gbl = new java.awt.GridBagLayout();
	setLayout(gbl);
	java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
	if (alignI == CENTER_ALIGNMENT) {
	    gbc.fill = gbc.BOTH;
	    gbc.anchor = gbc.CENTER;
	} else if (alignI == RIGHT_ALIGNMENT) {
	    gbc.fill = gbc.NONE;
	    gbc.anchor = gbc.EAST;
	} else {
	    gbc.fill = gbc.NONE;
	    gbc.anchor = gbc.WEST;
	}
	gbc.insets = new java.awt.Insets(10,20,0,20);

	int row = 0;
	java.awt.Label label;
	java.util.StringTokenizer strtok = new java.util.StringTokenizer
	    (textI,
	     "\n");
	String line;
	while (strtok.hasMoreTokens()) {
	    line = strtok.nextToken();
	    label = new java.awt.Label(line);
	    label.setAlignment(label.CENTER);
	    com.rbnb.utility.Utility.add(this,label,gbl,gbc,0,row++,2,1);
	}

	gbc.insets = new java.awt.Insets(10,10,10,10);
	gbc.fill = gbc.NONE;
	gbc.anchor = gbc.WEST;
	gbc.ipadx = 8;
	okButton = new java.awt.Button("OK");
	com.rbnb.utility.Utility.add(this,okButton,gbl,gbc,0,row,1,1);
	okButton.addActionListener(this);

	gbc.anchor = gbc.EAST;
	cancelButton = new java.awt.Button("Cancel");
	com.rbnb.utility.Utility.add(this,cancelButton,gbl,gbc,1,row++,1,1);
	cancelButton.addActionListener(this);

	pack();
	addWindowListener(this);

	java.awt.Rectangle parentBounds = getParent().getBounds();
    	java.awt.Rectangle infoBounds = getBounds();
	setLocation(com.rbnb.utility.Utility.centerRect(infoBounds,
							  parentBounds));
    }

    /**
     * Handles a button press.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void actionPerformed(java.awt.event.ActionEvent eventI) {
	if (eventI.getSource() == okButton) {
	    okAction();
	} else if (eventI.getSource() == cancelButton) {
	    cancelAction();
	}
    }

    /**
     * Cancel button has been pressed.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #okAction()
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    private final void cancelAction() {
	confirmed = false;
	setVisible(false);
    }

    /**
     * OK button has been pressed.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #cancelAction()
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    private final void okAction() {
	confirmed = true;
	setVisible(false);
    }

    /**
     * Handle window closing event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowClosing(java.awt.event.WindowEvent eventI) {
        okAction();
    }

    /**
     * Handle window opened event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowOpened(java.awt.event.WindowEvent eventI) {}

    /**
     * Handle window activated event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowActivated(java.awt.event.WindowEvent eventI) {}

    /**
     * Handle window closed event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowClosed(java.awt.event.WindowEvent eventI) {}

    /**
     * Handle window deactivated event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowDeactivated(java.awt.event.WindowEvent eventI) {}

    /**
     * Handle window deiconified event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowDeiconified(java.awt.event.WindowEvent eventI) {}

    /**
     * Handle window iconified event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    public final void windowIconified(java.awt.event.WindowEvent eventI) {}
}
