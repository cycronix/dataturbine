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

import com.rbnb.api.Client;
import com.rbnb.api.Controller;
import com.rbnb.api.Server;
import com.rbnb.api.Shortcut;
import com.rbnb.api.Sink;
import com.rbnb.api.Source;
import com.rbnb.api.Rmap;

/******************************************************************************
 * An instance of this class is stored as a tree node's userObject.
 * <p>
 *
 * @author John P. Wilson
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 05/23/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/22/2002  JPW	Add support for shortcuts.
 * 05/23/2001  JPW	Created.
 *
 */

public class AdminTreeUserObject {
    
    /**
     * Rmap for this node in the JTree.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    Rmap rmap = null;
    
    /**************************************************************************
     * Initialize a tree node's userObject.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param rmapI  Rmap for this tree node.
     * @since V2.0
     * @version 05/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2001  JPW  Created.
     *
     */
    
    public AdminTreeUserObject(Rmap rmapI) {
	rmap = rmapI;
    }
    
    /**************************************************************************
     * Gets the stored Rmap for the tree node.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return the Rmap stored for the tree node
     * @see Rmap
     * @since V2.0
     * @version 05/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2001  JPW  Created.
     *
     */
    
    public Rmap getRmap() {
	return rmap;
    }
    
    /**************************************************************************
     * Gets the String representation of this object.
     * <p>
     * This is the String that is displayed in the tree view for the node.
     *
     * @author John P. Wilson
     *
     * @return the String representation of this object
     * @since V2.0
     * @version 05/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2002  JPW	Add support for Shortcuts.
     * 05/23/2001  JPW  Created.
     *
     */
    
    public String toString() {
	
	String nameStr;
	String infoStr;
	String nodeStr;
	
	nameStr = rmap.getName();
	if (nameStr == null) {
	    nameStr = "<UNNAMED>";
	}
	
	infoStr = "";
	
	// JPW 01/22/2002: Add support for Shortcuts
	if (rmap instanceof Shortcut) {
	    infoStr =
	        " >>> " +
	        ((Shortcut)rmap).getDestinationAddress();
	}
	else if (rmap instanceof Server) {
	    infoStr = " " + ((Server)rmap).getAddress();
	}
	else if ( (rmap instanceof Sink) ||
		  (rmap instanceof Source) )
	{
	    infoStr = "";
	    Client client = (Client)rmap;
	    if (client.getType() == Client.MIRROR) {
		infoStr = " " + client.getRemoteID();
	    }
	}
	
	nodeStr = nameStr + infoStr;
	
	return nodeStr;
	
    }
}
