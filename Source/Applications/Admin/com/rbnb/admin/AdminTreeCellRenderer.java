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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import java.awt.image.MemoryImageSource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JTree;

import com.rbnb.api.Client;
import com.rbnb.api.Controller;
import com.rbnb.api.Mirror;
import com.rbnb.api.PlugIn;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Shortcut;
import com.rbnb.api.Sink;
import com.rbnb.api.Source;

/******************************************************************************
 * Enables the display of node-specific icons in the JTree.
 * <p>
 * For example, nodes which represent Servers have a small RBNB-web icon in
 * the JTree.
 * <p>
 *
 * @author John P. Wilson
 *
 * @since V2.0
 * @version 09/19/2005
 */

/*
 * Copyright 2001 - 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/19/2005  JPW	Add an image specifically for PlugIns
 * 02/14/2002  JPW	Add new member variable: admin; this is needed
 *			    in getTreeCellRendererComponent() to see if
 *			    the given node represents the server the user
 *			    is currently connected to.  If it is, the text
 *			    on this node will be bold red.
 *			Add new constructor for passing in Admin variable.
 * 02/11/2002  JPW	Fixed error with what icon was being displayed for
 *			    Server/Shortcuts and PlugIns.
 * 01/29/2002  JPW	Added support for PlugIn objects
 * 01/22/2002  JPW	Added support for Shortcut objects
 * 05/23/2001  JPW	Created.
 *
 */

public class AdminTreeCellRenderer extends DefaultTreeCellRenderer {
    
    /**
     * Top level Admin class.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/14/2002
     */
    private Admin admin = null;
    
    /**************************************************************************
     * Create a new AdminTreeCellRenderer.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param adminI  Top level Admin class for this application.
     * @since V2.0
     * @version 02/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/14/2002  JPW	Created.
     *
     */
    
    public AdminTreeCellRenderer(Admin adminI) {
	admin = adminI;
    }
    
    /**************************************************************************
     * Configures the renderer based on the passed in components.
     * <p>
     * Call getTreeCellRendererComponent() in the parent class and then
     * override the icon displayed for this node.  The icon type is selected
     * based on the node's user object Rmap (different icons are displayed for
     * Servers, Sources, Sinks, etc.).
     * <p>
     *
     * @author John P. Wilson
     *
     * @return Component that the renderer uses to draw the value.
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent
     * @since V2.0
     * @version 09/19/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/19/2005  JPW	Add an image specifically for PlugIns
     * 02/14/2002  JPW	If this node's user object isn't an AdminTree-
     *			    UserObject: don't display an icon; display text
     *			    in italic.
     *			If this node is for the server the user is connected
     *			    to: display the text in bold red.
     * 02/11/2002  JPW	Fixed error with what icon was being displayed for
     *			    Server/Shortcuts and PlugIns.
     * 05/23/2001  JPW  Created.
     *
     */
    
    public Component getTreeCellRendererComponent(
	JTree treeI,
	Object valueI,
	boolean selI,
	boolean expandedI,
	boolean leafI,
	int rowI,
	boolean hasFocusI)
    {
	
	ImageIcon icon = null;
	
	// Get the default renderer and then override its icon
	Component defaultComp = super.getTreeCellRendererComponent(
	    treeI, valueI, selI, expandedI, leafI, rowI, hasFocusI);
	
	// Get the selected component
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)valueI;
	
	defaultComp.setFont(new Font("Dialog", Font.PLAIN, 12));
	
	Object userObject = node.getUserObject();
	
	if ( (userObject == null) ||
	     (userObject instanceof AdminTreeUserObject) != true )
	{
	    // JPW 02/14/2002: Display text in italic and don't display icon.
	    defaultComp.setFont(new Font("Dialog", Font.ITALIC, 12));
	    setIcon(null);
	    return defaultComp;
	}
	
	Rmap storedRmap =
	    ((AdminTreeUserObject)node.getUserObject()).getRmap();
	
	// Return the image corresponding to the type of Rmap
	
	// NOTE: Must test for instanceof Sink *before* testing for
	//       instanceof Source since Sink is a subclass of Source.
	
	int width = -1;
	int height = -1;
	int imageArray[] = null;
	
	if (storedRmap instanceof Controller) {
	    width = controllerImageWidth;
	    height = controllerImageHeight;
	    imageArray = controllerImage;
	}
	else if (storedRmap instanceof PlugIn) {
	    // JPW 09/19/2005: PlugIns have their own image to display
	    /*
	    width = sourceImageWidth;
	    height = sourceImageHeight;
	    imageArray = sourceImage;
	    */
	    width = pluginImageWidth;
	    height = pluginImageHeight;
	    imageArray = pluginImage;
	}
	else if ( (storedRmap instanceof Server) ||
	          (storedRmap instanceof Shortcut) )
	{
	    // JPW 01/22/2002: Add support for Shortcut objects
	    width = serverImageWidth;
	    height = serverImageHeight;
	    imageArray = serverImage;
	    // JPW 02/14/2002: If this is the server the user is connected
	    //                 to, make the node label stand out.
	    try {
		if (storedRmap.getFullName().equals(
		    admin.getRBNBDataManager().getServerName()))
		{
		    defaultComp.setFont(new Font("Dialog", Font.BOLD, 12));
	            defaultComp.setForeground(Color.red);
		}
	    } catch (Exception exception) {
		System.err.println(
		    "ERROR obtaining full name from the Server's Rmap.");
	    }
	}
	else if (storedRmap instanceof Sink) {
	    // Test to see if this is a Mirror
	    Client client = (Client)storedRmap;
	    if (client.getType() == Client.MIRROR) {
		width = sinkSideMirrorImageWidth;
		height = sinkSideMirrorImageHeight;
		imageArray = sinkSideMirrorImage;
	    }
	    else {
		width = sinkImageWidth;
		height = sinkImageHeight;
		imageArray = sinkImage;
	    }
	}
	else if (storedRmap instanceof Source) {
	    // Test to see if this is a Mirror
	    Client client = (Client)storedRmap;
	    if (client.getType() == Client.MIRROR) {
		width = sourceSideMirrorImageWidth;
		height = sourceSideMirrorImageHeight;
		imageArray = sourceSideMirrorImage;
		
	    }
	    else {
		width = sourceImageWidth;
		height = sourceImageHeight;
		imageArray = sourceImage;
	    }
	}
	
	if ( (width != -1) && (height != -1) && (imageArray != null) ) {
	    MemoryImageSource mis =
        	new MemoryImageSource(
		    width,
                    height,
                    imageArray,
                    0,
		    width);
	    Image image = createImage(mis);
	    icon = new ImageIcon(image);
	    setIcon(icon);
	}
	else {
	    setIcon(getDefaultLeafIcon());
	}
	
	return this;
	
    }
    
    /**
     * Width of the controller image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int controllerImageWidth=18;
    
    /**
     * Height of the controller image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int controllerImageHeight=16;
    
    /**
     * Raw controller image data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int controllerImage[] = {
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-16777216,-16777216,-1,-1,
-1,-1,-1,-1,-16777216,-1,-16777216,-16777216,
-16777216,-16777216,-16777216,-1,-1,-1,-16777216,-16777216,
-1,-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-16777216,-16777216,-1,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1};
    
    /**
     * Width of the sink-side mirror image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sinkSideMirrorImageWidth=20;
    
    /**
     * Height of the sink-side mirror image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sinkSideMirrorImageHeight=16;
    
    /**
     * Raw sink-side mirror image data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sinkSideMirrorImage[] = {
-1,-1,-1,-1,-16777216,-16777216,-16777216,-1,
-1,-1,-1,-1,-16777216,-16777216,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-16777216,-16777216,-1,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-16777216,-16777216,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-16777216,-16777216,-1,-1,-1,-1,-1,
-16777216,-16777216,-16777216,-1,-1,-1,-1,-1
};
    
    /**
     * Width of the source-side mirror image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sourceSideMirrorImageWidth=20;
    
    /**
     * Height of the source-side mirror image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sourceSideMirrorImageHeight=16;
    
    /**
     * Raw source-side mirror image data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sourceSideMirrorImage[] = {
-1,-1,-1,-1,-1,-16777216,-16777216,-16777216,
-1,-1,-1,-1,-1,-16777216,-16777216,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-16777216,-16777216,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-1,-16777216,-16777216,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-16777216,-16777216,-16777216,-1,-1,-1,-1,
-1,-16777216,-16777216,-16777216,-1,-1,-1,-1
};
    
    /**
     * Width of the server image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int serverImageWidth=20;
    
    /**
     * Height of the server image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int serverImageHeight=19;
    
    /**
     * Raw server image data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int serverImage[] = {
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-16777216,-16777216,-16777216,-16777216,-16777216,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-16777216,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-16777216,-16777216,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-16777216,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-16777216,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-16777216,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-16777216,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-16777216,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-16777216,-16777216,-1,-16777216,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-16777216,-16777216,-16777216,-16777216,-16777216,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216};

    /**
     * Width of the sink image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sinkImageWidth=20;
    
    /**
     * Height of the sink image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sinkImageHeight=15;
    
    /**
     * Raw sink image data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sinkImage[] = {
-1,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-16777216,-16777216,-1,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-16777216,-16777216,-1,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1};
    
    /**
     * Width of the source image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sourceImageWidth=20;
    
    /**
     * Height of the source image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sourceImageHeight=17;
    
    /**
     * Raw source image data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private final static int sourceImage[] = {
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-16777216,-1,-1,-16777216,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-16777216,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,
-16777216,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-16777216,-16777216,-1,-1,-1,-1,
-1,-1,-1,-16777216,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-16777216,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-16777216,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-16777216,-16777216,-1,-1,
-1,-1,-1,-16777216,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,-1,
-1,-1,-1,-1};

    /**
     * Width of the PlugIn image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 09/19/2005
     */
    private final static int pluginImageWidth=20;
    
    /**
     * Height of the PlugIn image.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 09/19/2005
     */
    private final static int pluginImageHeight=17;
    
    /**
     * Raw PlugIn image data.  This image has a black RBO ring and red arrow.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 09/19/2005
     */
    private final static int pluginImage[] = {
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-16777216,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-16777216,-1,-1,-16777216,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,-1,-1,-1,-1,-16777216,
-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,
-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-1,
-1,-1,-1,-1,-1,-1,-1,-16777216,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-16777216,-1,-1,-1,-1,-1,-16777216,-16777216,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,-1,-1,-1,-1,-1};
    
    /**
     * Raw PlugIn image data.  This image is all in red.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 09/19/2005
     */
    private final static int pluginImage_all_red[] = {
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-65536,-65536,-65536,-65536,-1,-1,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-65536,-1,-1,-65536,-65536,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-65536,-1,-1,
-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-1,
-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-1,
-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-65536,
-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,-1,-1,-1,-1,-65536,
-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-65536,-1,-1,-1,-1,-1,-1,-65536,
-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,-1,-1,-1,-1,-65536,
-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-65536,
-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-1,
-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-1,
-1,-1,-1,-1,-1,-1,-1,-65536,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,-1,-1,-65536,-65536,-1,-1,-1,
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-65536,-65536,-65536,-65536,-65536,-1,-1,-1,-1,-1};

}

