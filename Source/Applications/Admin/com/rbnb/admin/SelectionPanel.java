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
import java.util.*;

/******************************************************************************
 * A panel which contains instances of BorderedPanel objects.
 * <p>
 * If the user clicks on one of the BorderedPanel objects, its border is colored
 * black to indicate the selection. If there was a previously selected panel,
 * this panel's border will be repainted in the default color. There is at most
 * one currently selected BorderedPanel.
 * <p>
 *
 * @author John P. Wilson
 *
 * @since V2.0
 * @version 06/07/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/07/2001  JPW	Created (Taken from V1.1 RBNB code)
 *
 */
public class SelectionPanel extends Panel {
    
    /**
     * The currently selected panel.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private BorderedPanel currentSelection = null;
    
    /**************************************************************************
     * Create the SelectionPanel.
     * <p>
     *
     * @author John P. Wilson
     *
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
    public SelectionPanel() {
        super();
    }
    
    /**************************************************************************
     * Create the SelectionPanel; use the given layout manager.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param lm  Layout manager to use for this panel.
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
    public SelectionPanel(LayoutManager lm) {
        super(lm);
    }
    
    /**************************************************************************
     * If there is a currently selected BorderedPanel, unselect it. Then
     * mark the given BorderedPanel as selected.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param borderedPanel  Panel to select.
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
    public void setSelectedPanel(BorderedPanel borderedPanel) {

        // If given panel is same as currently selected panel, return false
        if (borderedPanel == currentSelection) {
            return;
        }
        
        if (currentSelection != null) {
            currentSelection.setColor( currentSelection.getBackground() );
            currentSelection.repaint();
        }
        
        // make the given BorderedPanel the current selection
        currentSelection = borderedPanel;
        currentSelection.setColor(Color.black);
        currentSelection.repaint();
        
	/*
        // JPW 4/22/98: add special case for HostBorderedPanel
        if (borderedPanel instanceof HostBorderedPanel) {
            // display Username information in userPanel
            EditAuthorization ea =
                ((HostBorderedPanel)borderedPanel).getEditAuthorization();
            ea.displayUsernameData((HostBorderedPanel)borderedPanel);
        }
	*/
        
    }
    
    /**************************************************************************
     * Gets the currently selected panel.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return The currently selected BorderedPanel.
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
    public BorderedPanel getSelectedPanel() {
        return currentSelection;
    }
    
}
