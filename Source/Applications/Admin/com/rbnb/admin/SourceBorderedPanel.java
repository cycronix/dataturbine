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
 * This class defines a bordered panel which contains information for
 * specifying a Source (its server address and data path).
 * <p>
 *
 * @author John P. Wilson
 * @author Ian A. Brown
 *
 * @since V2.0
 * @version 03/04/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/04/2002  INB	Eliminated the server name.
 * 06/08/2001  JPW	Changed hostPortTextField to addressTextField;
 *			added nameTextField.
 * 06/07/2001  JPW	Created (Taken from V1.1 RBNB code)
 *
 */
public class SourceBorderedPanel extends BorderedPanel
                                 implements FocusListener,
                                            MouseListener
{
    
    /**
     * SourceInfo object whose data is displayed in this panel.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private SourceInfo sourceInfo = null;
    
    /**
     * Dialog box in which this panel is located.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private MirrorDialog mirrorDialog = null;
    
    /**
     * TextField for specifying Server address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private TextField addressTextField;
    
    /**
     * TextField for specifying data path.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private TextField dataPathTextField;
    
    /**************************************************************************
     * Create a SourceBorderedPanel containing 2 TextField components: one for
     * entering Server address and one for entering the Source's data path.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @param sourceInfoI  Object containing the initial adress and data
     *                     path info.
     * @param mirrorDialogI  Dialog box in which this panel resides.
     * @since V2.0
     * @version 03/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/04/2002  INB	Eliminated server name.
     * 06/08/2001  JPW	Add nameTextField.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public SourceBorderedPanel(SourceInfo sourceInfoI,
                               MirrorDialog mirrorDialogI)
    {
        
        super();
        
	mirrorDialog = mirrorDialogI;
	sourceInfo = sourceInfoI;
        
	setFont(new Font("Dialog", Font.PLAIN, 12));
	setBackground(Color.lightGray);
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 100;
	gbc.weighty = 100;
	
	Label tempLabel;
	
	// Row 1: addressTextField
	tempLabel = new Label("Address",Label.LEFT);
	addressTextField = new TextField(sourceInfo.addressStr,20);
	// NOTE: Make the right inset "40" such that addressTextField roughly
	//       lines up with the "To" Address and DataPath TextFields in the
	//       MirrorDialog
	gbc.insets = new Insets(1, 1, 0, 40);
	Utility.add(this,tempLabel,gbl,gbc,0,0,1,1);
	gbc.insets = new Insets(1, 0, 0, 1);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	Utility.add(this,addressTextField,gbl,gbc,1,0,1,1);
	gbc.fill = GridBagConstraints.NONE;
	
	// Row 1: dataPathTextField
	tempLabel = new Label("Data Path",Label.LEFT);
	dataPathTextField = new TextField(sourceInfo.dataPathStr,20);
	// NOTE: Make the right inset "40" such that dataPathTextField roughly
	//       lines up with the "To" Address and DataPath TextFields in the
	//       MirrorDialog
	gbc.insets = new Insets(1, 1, 1, 40);
	Utility.add(this,tempLabel,gbl,gbc,0,1,1,1);
	gbc.insets = new Insets(1, 0, 1, 1);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	Utility.add(this,dataPathTextField,gbl,gbc,1,1,1,1);
	gbc.fill = GridBagConstraints.NONE;
	
        //////////////////////
        // Add event listeners
        //////////////////////
        
        // Select this panel whenever the focus is gained by this panel or
        // any of its components
	addressTextField.addFocusListener(this);
	dataPathTextField.addFocusListener(this);
        addFocusListener(this);
        
        // When I click the mouse on this panel, it doesn't fire a Focus
        // event. Therefore, add a mouse listener to this panel
        addMouseListener(this);
        
    }
    
    /**************************************************************************
     * Called when any of the components in this panel gain focus.  Make sure
     * this panel becomes the currently selected panel.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  FocusEvent object.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void focusGained(FocusEvent e) {
	
        // Make sure this panel is set as the currently selected panel
        SelectionPanel panel = (SelectionPanel)getParent();
        panel.setSelectedPanel(this);
        return;
	
    }
    
    /**************************************************************************
     * Called when focus leaves any of the components in this panel. No need
     * to do anything here.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  FocusEvent object.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void focusLost(FocusEvent e) {
	
    }

    /**************************************************************************
     * Gets the server address TextField component.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return The Server address TextField component.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/08/2001  JPW	Change from getHostPortTextField() to
     *                  getAddressTextField().
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public TextField getAddressTextField() {
        
        return addressTextField;
        
    }
    
    /**************************************************************************
     * Update the SourceInfo object (based on current TextField entry) and
     * return the object to the user.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @return The updated SourceInfo object.
     * @since V2.0
     * @version 03/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/04/2002  INB	Eliminated server name.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public SourceInfo getUpdatedSourceInfo() {

	sourceInfo.addressStr = addressTextField.getText().trim();
	sourceInfo.dataPathStr = dataPathTextField.getText().trim();
        return sourceInfo;
        
    }
    
    /**************************************************************************
     * Defined as part of implementing MouseListener.  No action needed from
     * this method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  MouseEvent which has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void mouseClicked(MouseEvent e) {}
    
    /**************************************************************************
     * Defined as part of implementing MouseListener.  No action needed from
     * this method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  MouseEvent which has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void mouseEntered(MouseEvent e) {}
    
    /**************************************************************************
     * Defined as part of implementing MouseListener.  No action needed from
     * this method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  MouseEvent which has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void mouseExited(MouseEvent e) {}
    
    /**************************************************************************
     * Defined as part of implementing MouseListener.  No action needed from
     * this method.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  MouseEvent which has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void mousePressed(MouseEvent e) {}
    
    /**************************************************************************
     * Mouse button has been released over this panel.  Request focus for this
     * panel.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param e  MouseEvent which has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void mouseReleased(MouseEvent e) {
        this.requestFocus();
    }
    
}
