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


package com.rbnb.plot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.rbnb.utility.Utility;

/**
 * Presents channel list in separate window using JTree; non modal.
 * <p>
 * NOTE ON POTENTIAL SYNCHRONIZATION PROBLEM:
 * A problem may result if the user is able to "sneak" in a channel selection
 * or deselection in the JTree in between UserControl's calls to
 * setAvailableChannels() and setSelectedChannels().  If this occurs, then
 * UserControl's list of selected channels (and what displays for plots) may
 * be one channel more or less than what is currently selected in the JTree.
 * It doesn't appear that this would be easy to do, and there is no simple
 * fix that we could think of for this potential issue, so we havn't done
 * anything about it (other than note that this problem may occur).
 *
 * @author John P. Wilson
 *
 * @version 09/15/2005
 */

/*
 * Copyright 1999 - 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/14/2005  JPW	Remove the rest of the synchronization blocks from
 *			the code; not sure they are necessary.
 *			Add the following methods:
 *			    convertTreePathsToOrderedChannels()
 *			    convertChannelsToTreePaths()
 * 09/13/2005  JPW	Swing components are only supposed to be modified from
 *			the Swing event dispatch thread (EDT).  With the
 *			synchronization blocks in place in this class, we had
 *			a lockup problem in setAvailableChannels() due to the
 *			fact that this method was modifying the JTree from a
 *			non-Swing thread.  For details on this situation, see:
 *
 *			    http://java.sun.com/docs/books/tutorial/
 *			                         uiswing/misc/threads.html#EDT
 *
 *			To change places in the code where we were modifying
 *			Swing components from a non-Swing thread, I have added
 *			calls to SwingUtilities.invokeLater()
 * 09/12/2005  JPW	Change in updateSelectedChannels():
 *			Implement new channel selection logic to handle Sources
 *			which have a large number of channels.  We don't want
 *			the user to be suprised by clicking on a Source node
 *			and suddenly have the GUI crawl through adding a huge
 *			number of plots (which will be unusable anyway because
 *			they will be so small). If the user clicks on a folder
 *			node which has leaf node (channel) children, implement
 *			the following logic:
 *			1. If the number of child leaf nodes is less than or
 *			   equal to MAX_NUM_NODES_TO_AUTOSELECT then the
 *			   selection/deselection logic remains unchanged.
 *			2. If MAX_NUM_NODES_TO_AUTOSELECT or more number of
 *			   leaf nodes are currently selected, then unselect all
 *			   of the child leaf nodes in this folder.
 *			2. If zero or more, but less than
 *			   MAX_NUM_NODES_TO_AUTOSELECT leaf nodes are selected,
 *			   then select additional child leaf nodes, up to a
 *			   maximum of either:
 *				o MAX_NUM_NODES_TO_AUTOSELECT,
 *				OR
 *				o the total number of child lead nodes,
 *			   whichever is smaller.
 * 05/25/2005  JPW	Change in expandPaths(): Just expand the tree 1 level
 *			down; don't recursively call expandPaths()
 * 04/05/2005  JPW	Remove "All" button; change name of "None" to
 *			    "Reset"; add "Refresh" button to do an RBNB
 *			    channel refresh.  Add LayoutCubby to the
 *			    constructor.
 * 03/29/2005  JPW	Code taken from ChannelDialog; convert to Swing and use
 *			    a JTree for displaying chans
 * 05/01/1999  EMF	Created.
 *
 */

public class JChannelDialog extends JFrame
                            implements ActionListener, TreeExpansionListener
{
    
    Frame parent = null;
    RBNBCubby rbnbCubby = null;
    LayoutCubby layoutCubby = null;
    
    // availableChans array: Contains all available channels in the RBNB
    RegChannel[] availableChans = new RegChannel[0];
    
    // selectedChans array: Contains all selected channels in the RBNB
    RegChannel[] selectedChans = new RegChannel[0];
    
    /**
     * Some general info on terms used in JTree's documentation:
     * expanded node: a node which displays its children
     * collapsed node: a node which hides its children
     * hidden node: a node which resides under a collapsed parent
     * viewable node: a node which is under an expanded parent; may or may
     *                not be displayed
     * displayed node: a node which is both viewable *and* in the display
     *                 area (i.e. it can be seen by the user)
     */
    private DefaultMutableTreeNode rootNode = null;
    private JTree tree = null;
    private DefaultTreeModel treeModel = null;
    private JScrollPane treeScrollPane = null;
    private JPanel scrollPanel = null;
    // JPW 04/05/2005: Remove "All" button; change "None" to "Reset";
    //                 add "Refresh"
    // private JButton allButton = null;
    private JButton resetButton = null;
    private JButton refreshButton = null;
    
    // A Vector of TreePath objects which are currently selected in the JTree
    private Vector selectedTreePaths = new Vector();
    
    // JPW 09/12/2005: The max number of channel lead nodes to automatically
    //                 select when the user clicks on a Server, Source, or
    //                 other folder object in the tree view.
    private final static int MAX_NUM_NODES_TO_AUTOSELECT = 64;
    
    /**************************************************************************
     * Constructor.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param frameI the parent frame
     * @param rbcI cubby in which to write the channels to display
     * @version 09/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/13/2005  JPW  Use SwingUtilities.invokeLater() to create the GUI
     *			from within the Swing event dispatch thread.
     * 04/05/2005  JPW  Add LayoutCubby to constructor.
     * 04/04/2005  JPW  Synchronize methods that access availableChans or
     *			    selectedTreePaths, because different threads can
     *			    set or access these objects
     * 03/29/2005  JPW  Created.
     *
     */
    
    public JChannelDialog(Frame frameI, LayoutCubby locI, RBNBCubby rbcI) {
	
	super("rbnbPlot Channel Configuration");
	
	parent = frameI;
	rbnbCubby = rbcI;
	layoutCubby = locI;
	
	// JPW 09/13/2005: Make sure we create the Swing JTree component
	//                 only from the Swing event dispatch thread.
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		createGUI();
	    }
	});
	
    }
    
    /**************************************************************************
     * Create the GUI.
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     *
     * @author John P. Wilson
     *
     * @version 09/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/13/2005  JPW  Created.
     *
     */
    
    private void createGUI() {
	
	/*
	 *
	 * CREATE AND ADD JTree TO THE GUI
	 *
	 */
	
	// Create the JTree
	rootNode = new DefaultMutableTreeNode("ChannelTree");
	tree = new JTree(rootNode);
        //EMF 4/11/07: use Environment font so size can be changed
	tree.setFont(Environment.FONT10);
        java.awt.FontMetrics fm=tree.getFontMetrics(Environment.FONT10);
        tree.setRowHeight(fm.getHeight());
	treeModel = (DefaultTreeModel)tree.getModel();
	// Don't display the root node
	tree.setRootVisible(false);
	// Show the toggle button/handles on the root nodes
	tree.setShowsRootHandles(true);
	// Show lines joining parents and children
	tree.putClientProperty("JTree.lineStyle","Angled");
	// User can only select one item at a time
	tree.getSelectionModel().setSelectionMode(
	    TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	// Listener for handling user mouse clicks
	/*
	MouseListener ml = new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		if (selRow != -1) {
		    // JPW 11/01/05: No longer automatically select/unselect
		    //               channels in a Source; this would cause
		    //               too many "suprises" if selecting a large
		    //               number of channels in the Source.
		    // updateSelectedChannels(selPath);
		}
	    }
	};
	*/
	MouseListener ml = new MouseHandlerClass(this);
	// Notes on callbacks from the tree:
	// When the user presses on a tree node, mousePressed() is called
	// When the user expands/collapses a branch in the tree (either by
	//     clicking on the small '[+]' or '[-]' buttons in the tree or
	//     by double-clicking on a node
	tree.addMouseListener(ml);
	tree.addTreeExpansionListener(this);
	
	// Add the JTree to a JScrollPane
	treeScrollPane = new JScrollPane(tree);
	
	// Add the JScrollPane to a JPanel
	scrollPanel = new JPanel();
	GridBagLayout panelgbl = new GridBagLayout();
        scrollPanel.setLayout(panelgbl);
        GridBagConstraints panelgbc = new GridBagConstraints();
        panelgbc.anchor = GridBagConstraints.CENTER;
	panelgbc.fill = GridBagConstraints.BOTH;
        panelgbc.weightx = 100;
        panelgbc.weighty = 100;
        panelgbc.insets = new Insets(0,0,0,0);
        Utility.add(scrollPanel,treeScrollPane,panelgbl,panelgbc,0,0,1,1);
	
	// Add the panel to the content pane of the JFrame
	GridBagLayout gbl = new GridBagLayout();
        getContentPane().setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,0,0,0);
        Utility.add(getContentPane(),scrollPanel,gbl,gbc,0,0,1,1);
	
	/*
	 *
	 * ADD "Reset" AND "Refresh" BUTTONS TO THE GUI
	 *
	 */
	
	// allButton = new JButton("All");
	// allButton.addActionListener(this);
	resetButton = new JButton("Reset");
	resetButton.setFont(Environment.FONT12);
	resetButton.addActionListener(this);
	refreshButton = new JButton("Refresh");
	refreshButton.setFont(Environment.FONT12);
	refreshButton.addActionListener(this);
	JPanel buttonPanel = new JPanel(new GridLayout(1,2,15,0));
        buttonPanel.add(resetButton);
        buttonPanel.add(refreshButton);
        // Don't have the buttons resize if the dialog is resized
        gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(15,15,15,15);
        Utility.add(getContentPane(),buttonPanel,gbl,gbc,0,1,1,1);
	
	// Handle the close operation in the windowClosing() method of the
	// registered WindowListener object.  This will get around
	// JFrame's default behavior of automatically hiding the window when
	// the user clicks on the '[x]' button.
	this.setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	
	addWindowListener(new CloseClass());
	
	pack();
	
	// Must set size *after* we call pack()
	Dimension d = parent.getSize();
	setSize(300,d.height);
	
    }
    
    /**************************************************************************
     * Sets size and position adjacent to parent frame
     * <p>
     *
     * @author Eric Friets
     *
     * @param eventI the event which has occurred
     * @version 09/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/13/2005  JPW  Use SwingUtilities.invokeLater() to modify the GUI
     *			from within the Swing event dispatch thread.
     * 03/29/2005  EMF  Created.
     *
     */
    
    public void firstShow() {
	// JPW 09/13/2005: Make sure we modify the Swing JTree component
	//                 only from the Swing event dispatch thread.
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		// Make sure the window is not iconified
		setExtendedState(JFrame.NORMAL);
		Dimension d = parent.getSize();
		Dimension thisd = getSize();
		Point p = parent.getLocation();
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension sz = tk.getScreenSize();
		p.x += d.width;
		if ( (p.x + thisd.width) > sz.width ) {
		    p.x = sz.width - thisd.width;
		}
		// JPW 09/14/2005: Moved the following 3 calls from
		//                 UserControl.actionPerformed() to here so
		//                 we could handle them with invokeLater()
		setLocation(p);
		setVisible(true);
		toFront();
		requestFocus();
	    }
	});
    }
    
    /**************************************************************************
     * Respond to actions which occur in this frame.
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param eventI the event which has occurred
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2005  JPW  Remove "All" button; change "None" to "Reset";
     *                      add "Refresh"
     * 03/31/2005  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
        if (event.getSource() == resetButton) {
            selectedTreePaths = new Vector();
	    // Since actionPerformed() is called by the Swing event dispatch
	    // thread, can call setSelectionPaths() directly.
	    setSelectionPaths();
	    RegChannel[] emptyRegChannelArray = new RegChannel[0];
	    rbnbCubby.setSelectedChannels(emptyRegChannelArray,true);
	    repaint();
        } else if (event.getSource() == refreshButton) {
	    layoutCubby.set(LayoutCubby.RefreshRBNB);
	}
	
    }
    
    /**************************************************************************
     * Part of implementing TreeExpansionListener.  Part of the JTree has
     * collapsed. Redraw the selected paths.
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param eventI the event which has occurred
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created.
     *
     */
    
    public void treeCollapsed(TreeExpansionEvent eventI) {
	// JPW 09/14/05: Since treeCollapsed() is called by the Swing event
	//               dispatch thread, can call setSelectionPaths()
	//               directly.
	setSelectionPaths();
    }
    
    /**************************************************************************
     * Part of implementing TreeExpansionListener.  Part of the JTree has
     * expanded.  Redraw the selected paths.  This callback will occur when the
     * user clicks on one of the small [+] buttons in the tree to expand that
     * branch of the tree.
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param eventI the event which has occurred
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created.
     *
     */
    
    public void treeExpanded(TreeExpansionEvent eventI) {
	// JPW 09/14/05: Since treeExpanded() is called by the Swing event
	//               dispatch thread, can call setSelectionPaths()
	//               directly.
	setSelectionPaths();
    }
    
    /**************************************************************************
     * Pop down this frame.
     * <p>
     *
     * @author Eric Friets
     *
     * @param eventI the event which has occurred
     * @version 03/29/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2005  EMF  Created.
     *
     */
    
    public void quitCancel() {
	setVisible(false);
    }
    
    /**************************************************************************
     * Reset the JTree with a new set of channels.
     * <p>
     * NOTE ON POTENTIAL SYNCHRONIZATION PROBLEM:
     * A problem may result if the user is able to sneak in a channel selection
     * or deselection in the JTree in between UserControl's calls to
     * setAvailableChannels() and setSelectedChannels().  If this occurs, then
     * UserControl's list of selected channels and what displays for plots may
     * be one channel more or less than what is currently selected in the tree.
     * It doesn't appear that this would be easy to do, and there is no simple
     * fix that we could think of for this potential issue, so we havn't done
     * anything about it (other than note that this problem may occur).
     * <p>
     *
     * @author John P. Wilson
     *
     * @param availableChansI new set of available channels
     * @version 09/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/14/2005  JPW  Only synchronize access to array availableChans
     * 09/13/2005  JPW  Use SwingUtilities.invokeLater() to make changes in the
     *			JTree from the Swing event dispatch thread.
     * 03/31/2005  JPW  Created.
     *
     */
    
    public void setAvailableChannels(RegChannel[] availableChansI) {
	
	// Don't need to make a copy of availableChansI - this is already a
	// copy of the array for our own use
	availableChans = availableChansI;
	
	// JPW 09/13/2005: Modify the Swing JTree component only from the
	//                 Swing event dispatch thread.
	try {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		    setupNewTree();
	        }
	    });
	} catch (Exception e) {
	    // do nothing
	}
	
	repaint();
	
    }
    
    /**************************************************************************
     * Create and display a new JTree using the array of channels specified by
     * availableChans
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     *
     * @author John P. Wilson
     *
     * @version 09/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/13/2005  JPW  Created.
     *
     */
    
    private void setupNewTree() {
	
	// JPW 09/15/2005: Save the currently selected paths
	RegChannel[] selectedChans =
	    convertTreePathsToOrderedChannels(selectedTreePaths);
	
	scrollPanel.removeAll();
	// Remove nodes from the JTree
	rootNode.removeAllChildren();
	treeModel.nodeStructureChanged(rootNode);
	treeModel.reload();
	
	// ?? May want to synchronize access to availableChans here ??
	for (int i = 0; i < availableChans.length; ++i) {
	    try {
		addNode(
		    rootNode, availableChans[i].name, availableChans[i].name);
	    } catch (Exception e) {
		System.err.println(
		    "Error adding a node to JTree: " + e);
	    }
	}
	
	// JPW 09/15/2005: Now that the tree is constructed, set the Vector
	//                 of selected TreePath objects
	selectedTreePaths = convertChannelsToTreePaths(selectedChans);
	
	// Expand the root node and then expand all children of the
	// root node.  In our case, the root node is not visible.
	// Expanding the root node will display the top level node that is
	// visible in our JTree.  This top node will be the RBNB server
	// to which we are connected.  Expanding one more level down
	// from that will display the children of the RBNB server.
	// NOTE: Calls to tree.expandPath() will result in the event dispatch
	//       thread calling treeExpanded()
	tree.expandPath( getTreePathFromNode(rootNode) );
	for (Enumeration e = rootNode.children(); e.hasMoreElements();) {
	    DefaultMutableTreeNode nextChild =
		(DefaultMutableTreeNode)e.nextElement();
	    tree.expandPath( getTreePathFromNode(nextChild) );
	}
	
	// Make sure the base of the tree is visible
	tree.scrollPathToVisible(new TreePath(rootNode));
	
	GridBagConstraints panelgbc = new GridBagConstraints();
	panelgbc.anchor = GridBagConstraints.CENTER;
	panelgbc.fill = GridBagConstraints.BOTH;
	panelgbc.weightx = 100;
	panelgbc.weighty = 100;
	panelgbc.insets = new Insets(0,0,0,0);
	Utility.add(
	    scrollPanel,
	    treeScrollPane,
	    (GridBagLayout)scrollPanel.getLayout(),
	    panelgbc,
	    0,0,1,1);
	
    }
    
    /**************************************************************************
     * Add a new node to the JTree.
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param parentNodeI   the new node's parent in the JTree
     * @param fullChanStrI  the full channel path
     * @param remainderStrI remainder of the channel path to add to the JTree
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created.
     *
     */
    
    private void addNode(DefaultMutableTreeNode parentNodeI,
                         String fullChanStrI,
                         String remainderStrI)
    throws Exception
    {
	if ( (remainderStrI == null) || (remainderStrI.trim().equals("")) ) {
	    return;
	}
	// Find the next segment in remainderStrI
	int slashIdx = remainderStrI.indexOf('/');
	String childStr = remainderStrI;
	String remainderStr = null;
	if (slashIdx >= 0) {
	    childStr = remainderStrI.substring(0,slashIdx);
	    if (!remainderStrI.endsWith("/")) {
		remainderStr = remainderStrI.substring(slashIdx + 1);
	    }
	}
	
	if ( (childStr == null) || (childStr.equals("")) ) {
	    throw new Exception(
	    	"addNode error: string must end in a slash: " + remainderStrI);
	}
	
	// See if parentNodeI already has a child node with the name childStr
	DefaultMutableTreeNode childNode = null;
	for (int i = 0; i < treeModel.getChildCount(parentNodeI); ++i) {
	    DefaultMutableTreeNode node =
	    	(DefaultMutableTreeNode)treeModel.getChild(parentNodeI, i);
	    if (node.toString().equals(childStr)) {
		childNode = node;
		break;
	    }
	}
	if (childNode == null) {
	    // Add a new node
	    // If this is a leaf node (noted by the fact that remainderStr
	    // is empty) then give the full channel name to the ChannelStr object
	    if ( (remainderStr == null) || (remainderStr.equals("")) ) {
		childNode =
	            new DefaultMutableTreeNode(
	                new ChannelStr(childStr, fullChanStrI));
	    } else {
		childNode =
	            new DefaultMutableTreeNode(
	                new ChannelStr(childStr, null));
	    }
	    parentNodeI.add(childNode);
	}
	
	if (remainderStr != null) {
	    addNode(childNode, fullChanStrI, remainderStr);
	    return;
	}
	
    }
    
    /**************************************************************************
     * Select the channels in the JTree that are in the given channel array
     * <p>
     * NOTE ON POTENTIAL SYNCHRONIZATION PROBLEM:
     * A problem may result if the user is able to sneak in a channel selection
     * or deselection in the JTree in between UserControl's calls to
     * setAvailableChannels() and setSelectedChannels().  If this occurs, then
     * UserControl's list of selected channels and what displays for plots may
     * be one channel more or less than what is currently selected in the tree.
     * It doesn't appear that this would be easy to do, and there is no simple
     * fix that we could think of for this potential issue, so we havn't done
     * anything about it (other than note that this problem may occur).
     * <p>
     *
     * @author John P. Wilson
     *
     * @param selectedChansI   array of channels to select in the JTree
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created.
     *
     */
    
    public void setSelectedChannels(RegChannel[] selectedChansI) {
	
	selectedChans = selectedChansI;
	
	// Select these paths in the JTree
	// Since setSelectedChannels() is called from the thread
	// operating in UserControl, need to schedule setSelectionPaths()
	// for later execution from the event dispatch thread
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		if (selectedChans != null) {
		    selectedTreePaths =
		        convertChannelsToTreePaths(selectedChans);
		    selectedChans = null;
		}
		setSelectionPaths();
	    }
	});
	
	repaint();
	
    }
    
    /**************************************************************************
     * Convert the given array of RegChannel objects into a Vector of JTree-
     * specific TreePath objects.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param selectedChansI  RegChannel objects to convert to TreePaths
     * @return Vector of TreePath objects
     * @version 09/14/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/14/2005  JPW  Created.
     *
     */
    
    private Vector convertChannelsToTreePaths(RegChannel[] selectedChansI) {
	
	// Should only call this method with a non-null array of channels
	// to select
	if ( (selectedChansI == null) || (selectedChansI.length == 0) ) {
	    return (new Vector());
	}
	
	Vector selectedTreePathsVector = new Vector();
	for (int i = 0; i < selectedChansI.length; ++i) {
	    // Convert a full channel name into a TreePath
	    TreePath path = null;
	    /*
	    * Method 1: Use findByName().  Note that we need to add the
	    *           name of the root node, "ChannelTree"
	    *
	    String newChanStr =
		new String("ChannelTree/" + selectedChansI[i].name);
	    String[] pathComponents = newChanStr.split("/");
	    path = findByName(tree, pathComponents);
	    */
	    
	    /*
	     * Method 2
	     */
	    DefaultMutableTreeNode node =
		getNextTreeNodeInPath(rootNode, selectedChansI[i].name);
	    if (node != null) {
		path = getTreePathFromNode(node);
	    } else {
		path = null;
	    }
	    
	    if (path != null) {
		selectedTreePathsVector.add(path);
	    }
	}
	
	return selectedTreePathsVector;
	
    }
    
    /**************************************************************************
     * Convert the given Vector of TreePath objects to an alphabetically
     * ordered array of RegChannel objects.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param treePathsI   TreePath objects to convert to RegChannels
     * @return array of RegChannel objects
     * @version 09/14/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/14/2005  JPW  Created.
     *
     */
    
    private RegChannel[] convertTreePathsToOrderedChannels(Vector treePathsI) {
	
	if ( (treePathsI == null) || (treePathsI.size() == 0) ) {
	    return (new RegChannel[0]);
	}
	
	DefaultMutableTreeNode node = null;
	TreePath treePath = null;
	ChannelStr nodeObj = null;
	
	// Convert from TreePaths to RegChannels
	TreePath[] treePathArray =
	    (TreePath[])treePathsI.toArray(new TreePath[0]);
	RegChannel[] regChans = new RegChannel[treePathArray.length];
	for (int i = 0; i < treePathArray.length; ++i) {
	    treePath = treePathArray[i];
	    node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
	    nodeObj = (ChannelStr)node.getUserObject();
	    regChans[i] = new RegChannel(nodeObj.getFullChannelStr());
	}
	
	// Create the sorted set
	SortedSet set = new TreeSet();
	for (int i = 0; i < regChans.length; ++i) {
	    set.add(regChans[i]);
	}
	/*
	// Here is how to iterate over the elements in the set
	Iterator it = set.iterator();
	while (it.hasNext()) {
	    // Get element
	    Object element = it.next();
	}
	*/
	// Create an array containing the elements from this set in order
	RegChannel[] orderedRegChans =
	    (RegChannel[])set.toArray(new RegChannel[set.size()]);
	
	return orderedRegChans;
	
    }
    
    /**************************************************************************
     * The user has selected or unselected a channel.  Reset the Plot cubbyhole
     * with the channels that are selected
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param treePathI TreePath which the user has selected or unselected
     * @version 09/12/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/12/2005  JPW	Implement new channel selection logic to handle Sources
     *			which have a large number of channels.  We don't want
     *			the user to be suprised by clicking on a Source node
     *			and suddenly have the GUI crawl through adding a huge
     *			number of plots (which will be unusable anyway because
     *			they will be so small). If the user clicks on a folder
     *			node which has leaf node (channel) children, implement
     *			the following logic:
     *			1. If the number of child leaf nodes is less than or
     *			   equal to MAX_NUM_NODES_TO_AUTOSELECT then the
     *			   selection/deselection logic remains unchanged.
     *			2. If MAX_NUM_NODES_TO_AUTOSELECT or more number of
     *			   leaf nodes are currently selected, then unselect all
     *			   of the child leaf nodes in this folder.
     *			2. If zero or more, but less than
     *			   MAX_NUM_NODES_TO_AUTOSELECT leaf nodes are selected,
     *			   then select additional child leaf nodes, up to a
     *			   maximum of either:
     *				o MAX_NUM_NODES_TO_AUTOSELECT,
     *				OR
     *				o the total number of child lead nodes,
     *			   whichever is smaller.
     * 04/05/2005  JPW  If treePathI corresponds to a Server, Source, or other
     *                  folder in the JTree which has child leaf nodes, then
     *                  either select all or clear all of these child nodes.
     * 03/31/2005  JPW  Created.
     *
     */
    
    private void updateSelectedChannels(TreePath treePathI) {
      
      // JPW 09/14/2005: remove synchronize
      // synchronized (this) {
	
	// JPW 09/12/2005: Add support for Sources with large number of
	//                 channels; add logic with MAX_NUM_NODES_TO_AUTOSELECT
	// If user has clicked on a folder with child leaf nodes:
	// 1. If none of the leaf nodes is currently selected, then
	//    select up to MAX_NUM_NODES_TO_AUTOSELECT of the nodes
	// 2. If MAX_NUM_NODES_TO_AUTOSELECT or more of the leaf nodes are
	//    currently selected, then clear all of the leaf nodes
	// 3. If all of the leaf nodes are currently selected, then clear all
	//    of the leaf nodes
	// 4. If some of the leaf nodes are currently selected (but less than
	//    MAX_NUM_NODES_TO_AUTOSELECT), then select up to
	//    MAX_NUM_NODES_TO_AUTOSELECT of the leaf nodes
	DefaultMutableTreeNode node =
	    (DefaultMutableTreeNode)treePathI.getLastPathComponent();
	if (node.isLeaf()) {
	    // The user has clicked on a channel leaf node
	    if (selectedTreePaths.contains(treePathI)) {
		// This channel is currently selected; unselect it
	        selectedTreePaths.remove(treePathI);
	    } else if (!selectedTreePaths.contains(treePathI)) {
		// This channel is not currently selected; select it
		selectedTreePaths.add(treePathI);
	    }
	} else {
	    // The user has clicked on a Server, Source, or other folder
	    int totalNumLeafNodes = 0;
	    int numPathsCurrentlySelected = 0;
	    DefaultMutableTreeNode childNode = null;
	    Vector vectorOfLeafPaths = new Vector();
	    Vector vectorOfCurrentlySelectedLeafPaths = new Vector();
	    for (int i = 0; i < treeModel.getChildCount(node); ++i) {
		childNode =
		    (DefaultMutableTreeNode)treeModel.getChild(node, i);
		// Only select/deselect if this is a leaf node
		if (childNode.isLeaf()) {
		    totalNumLeafNodes++;
		    TreePath path = getTreePathFromNode(childNode);
		    vectorOfLeafPaths.add(path);
		    if (selectedTreePaths.contains(path)) {
			numPathsCurrentlySelected++;
			vectorOfCurrentlySelectedLeafPaths.add(path);
		    }
		}
	    }
	    if (vectorOfLeafPaths.size() == 0) {
		// This folder didn't contain any child leaf nodes (that is,
		// it didn't contain any channels); just reselect the currently
		// selected paths and return.
		// NOTE: When the user pressed on this folder node, the folder
		//       node gets automatically selected. One reason to
		//       call setSelectionPaths() and reselect the currently
		//       selected paths is to *unselect* this folder node.
		// JPW 09/14/05: Since updateSelectedChannels() is called by
		//               the Swing event dispatch thread, can call
		//               setSelectionPaths() directly.
		setSelectionPaths();
		return;
	    }
	    else if ( (numPathsCurrentlySelected == totalNumLeafNodes) ||
	              (numPathsCurrentlySelected >= MAX_NUM_NODES_TO_AUTOSELECT) )
	    {
		// JPW 09/12/2005: Add "MAX_NUM_NODES_TO_AUTOSELECT" logic
		//                 to support Sources which have large numbers
		//                 of channels.
		// Unselect all channels in this Source.
		Enumeration enumeration =
		    vectorOfCurrentlySelectedLeafPaths.elements();
		while (enumeration.hasMoreElements()) {
		    TreePath path = (TreePath)enumeration.nextElement();
		    selectedTreePaths.remove(path);
		}
	    }
	    else {
		// Add leaf nodes to the vector of selected tree paths
		// Select up to MAX_NUM_NODES_TO_AUTOSELECT leaf nodes
		int maxNumLeafNodesToAdd =
		    MAX_NUM_NODES_TO_AUTOSELECT - numPathsCurrentlySelected;
		Enumeration enumeration = vectorOfLeafPaths.elements();
		while (enumeration.hasMoreElements()) {
		    TreePath path = (TreePath)enumeration.nextElement();
		    if (!selectedTreePaths.contains(path)) {
			selectedTreePaths.add(path);
			maxNumLeafNodesToAdd--;
		    }
		    if (maxNumLeafNodesToAdd == 0) {
			// We've reached out limit; don't add anymode
			break;
		    }
		}
	    }
	}
	
	// JPW 09/14/05: Since updateSelectedChannels() is called by the Swing
	//               event dispatch thread, can call setSelectionPaths()
	//               directly.
	setSelectionPaths();
	
	// JPW 09/15/2005: move code to orderedSelectedChans()
	// For setting the selected channels in rbnbCubby, the list of chans
	// must be in the same order they appear in availableChans (which is
	// alphabetical order
	RegChannel[] orderedSelectedChans =
	    convertTreePathsToOrderedChannels(selectedTreePaths);
	
	// Set the cubby variable
	rbnbCubby.setSelectedChannels(orderedSelectedChans,true);
	
      // JPW 09/14/2005: remove synchronize
      // }
      
      repaint();
      
    }
    
    /**************************************************************************
     * Select the TreePaths that are stored in Vector selectedTreePaths.
     * <p>
     * CAUTION: THIS METHOD SHOULD ONLY BE CALLED FROM THE SWING EVENT DISPATCH
     *          THREAD SINCE WE MODIFY A SWING COMPONENT IN THIS METHOD.
     * <p>
     * If a TreePath is not currently viewable (meaning all if its ancestors
     * are expanded) then DON'T select this TreePath.  Go up to the parent node
     * and see if it is viewable.  If it is viewable then select this node.
     * If not, keep working up the chain until a node is found which is
     * viewable and select that node.
     * <p>
     * This is to avoid the following problem: Say the user selects one
     * node, root/foo/chan1.  The user then collapses the foo branch.  Then
     * the user selects root/bar/chan1.  When this happens, the method
     * updateSelectedChannels() is called to add this new channel to Vector
     * selectedTreePaths and to then re-select all nodes in this Vector.
     * When this re-selection occurs, the foo branch is automatically
     * expanded (this making chan1 viewable) and chan1 is selected.  WE DON'T
     * WANT THIS AUTOMATIC BRANCH EXPANSION TO OCCUR.
     *
     * @author John P. Wilson
     *
     * @version 09/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/13/2005  JPW  Remove synchronization, since this method should only
     *			be called from the event dispatch thread.
     * 04/04/2005  JPW  synchronize access to selectedTreePaths
     * 04/01/2005  JPW  Created.
     *
     */
    
    private void setSelectionPaths() {
	
	if ( (selectedTreePaths == null) || (selectedTreePaths.size() == 0) ) {
	    tree.setSelectionPaths(new TreePath[0]);
	    return;
	}
	
	// Set up a new Vector of the TreePaths we are actually going to select
	Vector actualSelectedTreePaths = new Vector();
	
	TreePath[] treePathArray =
	    (TreePath[])selectedTreePaths.toArray(new TreePath[0]);
	
	for (int i = 0; i < treePathArray.length; ++i) {
	    TreePath path = treePathArray[i];
	    if (tree.isVisible(path)) {
		actualSelectedTreePaths.add(path);
	    } else {
		// Find the closest ancestor which is viewable
		boolean bViewable = false;
		while (!bViewable) {
		    DefaultMutableTreeNode node = getNodeFromTreePath(path);
		    // Move up one level
		    node = (DefaultMutableTreeNode)node.getParent();
		    if (node == null) {
			path = null;
			break;
		    }
		    path = getTreePathFromNode(node);
		    if (tree.isVisible(path)) {
			bViewable = true;
		    }
		}
		if (path != null) {
		    actualSelectedTreePaths.add(path);
		}
	    }
	}
	
	// Now select the content of Vector actualSelectedTreePaths
	treePathArray =
	    (TreePath[])actualSelectedTreePaths.toArray(new TreePath[0]);
	tree.setSelectionPaths(treePathArray);
	
    }
    
    /**************************************************************************
     * Return the TreePath for a corresponding DefaultMutableTreeNode
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created
     *
     */
    
    private TreePath getTreePathFromNode(DefaultMutableTreeNode treeNodeI) {
	TreeNode[] nodes = treeModel.getPathToRoot(treeNodeI);
	return new TreePath(nodes);
    }
    
    /**************************************************************************
     * Return the DefaultMutableTreeNode for a corresponding TreePath.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created
     *
     */
    
    private DefaultMutableTreeNode getNodeFromTreePath(TreePath pathI) {
	return (DefaultMutableTreeNode)pathI.getLastPathComponent();
    }
    
    /**************************************************************************
     * This routine recursively searches for the next tree node in the path.
     * <p>
     *
     * @param curNode The parent tree node, whose children we are searching
     * @param path    The remaining path or names we are searching for.
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Code copied from a source on the Internet:
     *			    http://www.koders.com/java
     *
     */
    
    private DefaultMutableTreeNode getNextTreeNodeInPath(
	DefaultMutableTreeNode curNode, String path)
    {
	// If "path" is empty/null, then curNode is the last node in the path
	
	String itemName;
	DefaultMutableTreeNode targetNode = null;
	
	if ( (path == null) || (path.trim().equals("")) ) {
	    targetNode = curNode;
	} else {
	    String[] pathComponents = path.split("/");
	    if (pathComponents.length > 1) {
		// There are 2 or more elements in the path.
		itemName = pathComponents[0];
		path = path.substring( path.indexOf('/') + 1 );
	    } else {
		// This is the last element in the path.
		itemName = path;
		path = null;
	    }
	    // Look at each child of this node
	    Enumeration children = curNode.children();
	    while (children.hasMoreElements()) {
		DefaultMutableTreeNode childNode =
		    (DefaultMutableTreeNode)children.nextElement();
		if (childNode.toString().equals(itemName)) {
		    // We have found a match!
		    targetNode = getNextTreeNodeInPath(childNode, path);
		    break;
		}
	    }
	}
	
	return targetNode;
	
    }
    
    /**************************************************************************
     * Find the path (regardless of visibility) that matches the specified
     * sequence of names.
     * <p>
     * For example:
     *     path = findByName(tree, new String[]{"JTree", "food", "bananas"});
     * NOTE: The first element in the String array must correspond to the root
     *       node of the JTree.  In the above example, "JTree" must be the
     *       root node.
     * Comparison is done using String.equals(). Returns null if not found.
     *
     * @param names A sequence of names where names[0] is the root and names[i]
     *              is a child of names[i-1].
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Code copied from a source on the Internet.
     *
     */
    
    public TreePath findByName(JTree tree, String[] names) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        return findTreePath(tree, new TreePath(root), names, 0, true);
    }
    
    /**************************************************************************
     * Finds the path in tree as specified by the node array. The node array is
     * a sequence of nodes where nodes[0] is the root and nodes[i] is a child
     * of nodes[i-1]. Comparison is done using Object.equals(). Returns null if
     * not found.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Code copied from a source on the Internet.
     *
     */
    
    public TreePath findByNode(JTree tree, Object[] nodes) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        return findTreePath(tree, new TreePath(root), nodes, 0, false);
    }
    
    /**************************************************************************
     * Find a TreePath
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Code copied from a source on the Internet.
     *
     */
    
    private TreePath findTreePath(JTree tree,
                                  TreePath parent,
			          Object[] nodes,
			          int depth,
			          boolean byName)
    {
        
	// NOTE: Could also use JTree.getNextMatch() to do the matching.
	
	TreeNode node = (TreeNode)parent.getLastPathComponent();
        Object o = node;
        
        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }
        
        // If equal, go down the branch
        if (o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length-1) {
                return parent;
            }
	    
            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result =
		        findTreePath(tree, path, nodes, depth+1, byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
	
        // No match at this branch
        return null;
    }
    
    /**************************************************************************
     * User object for storing data in the tree nodes.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created.
     *
     */
    
    class ChannelStr {
	
	private String displayStr = null;
	private String fullChannelStr = null;
	
	public ChannelStr(String displayStrI, String fullChannelStrI) {
	    displayStr = displayStrI;
	    fullChannelStr = fullChannelStrI;
	}
	
	// This is the String displayed at the JTree node.
	public String toString() {
	    return displayStr;
	}
	
	public String getFullChannelStr() {
	    return fullChannelStr;
	}
	
    }
    
    /**************************************************************************
     * Handle the user click on the small [x] in the upper right hand corner
     * of the window.
     * <p>
     * Do not select Server or RBO nodes.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 03/31/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2005  JPW  Created.
     *
     */
    
    class CloseClass extends WindowAdapter {
	
	public void windowClosing(WindowEvent e) {
	    quitCancel();
	}
	
    }
    
    /**************************************************************************
     * Handle mouse events.
     * <p>
     * Only interested in mouse events for which a popup menu should be
     * displayed.  If the event is a popup event, then display a node-specific
     * popup menu.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/01/2005
     */

    /*
     * Copyright 2005 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/01/2005  JPW	Created.
     *
     */
    
    private class MouseHandlerClass extends MouseAdapter {
	
	JChannelDialog parentDlg = null;
	
	/**********************************************************************
	 * Initialize the mouse class.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param parentDlgI  the dialog containing the JTree object
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	public MouseHandlerClass(JChannelDialog parentDlgI) {
	    parentDlg = parentDlgI;
	}
	
	/**********************************************************************
	 * Capture mouse click events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	public void mouseClicked(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse pressed events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	public void mousePressed(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse released events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	public void mouseReleased(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse entered events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	public void mouseEntered(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse exited events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	public void mouseExited(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Process a mouse event.  If appropriate, display a node-specific
	 * popup menu.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @version 11/01/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/01/2005  JPW	Created.
	 *
	 */
	
	private void handlePopup(MouseEvent e) {
	    
	    ///////////
	    // FIREWALL
	    ///////////
	    
	    JTree tree = (JTree)e.getSource();
	    int selRow = tree.getRowForLocation(e.getX(), e.getY());
	    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	    if ( (selRow == -1) || (selPath == null) ) {
		return;
	    }
	    DefaultMutableTreeNode selectedNode =
		(DefaultMutableTreeNode)selPath.getLastPathComponent();
	    
	    if (selectedNode.isLeaf()) {
		// The user has clicked on a channel leaf node
		if (selectedTreePaths.contains(selPath)) {
		    // This channel is currently selected; unselect it
		    selectedTreePaths.remove(selPath);
		} else if (!selectedTreePaths.contains(selPath)) {
		    // This channel is not currently selected; select it
		    selectedTreePaths.add(selPath);
		}
		// Since this method is called by the Swing event dispatch
		// thread, can call setSelectionPaths() directly.
		setSelectionPaths();
		
		// For setting the selected channels in rbnbCubby, the list of
		// chans must be in the same order they appear in
		// availableChans (which is alphabetical order)
		RegChannel[] orderedSelectedChans =
	    	    convertTreePathsToOrderedChannels(selectedTreePaths);
	    	
	    	// Set the cubby variable
		rbnbCubby.setSelectedChannels(orderedSelectedChans,true);
		
		repaint();
	    }
	    
	    // TEMPORARY
	    else {
		// Just reselect the currently selected paths and return.
		// NOTE: When the user pressed on this folder node, the
		//       folder node gets automatically selected. One
		//       reason to call setSelectionPaths() and reselect
		//       the currently selected paths is to *unselect* this
		//       folder node.
		// Since updateSelectedChannels() is called by the Swing
		// event dispatch thread, can call setSelectionPaths()
		// directly.
		setSelectionPaths();
	    }
	    
	    /*
	     * FOR FUTURE DEVELOPMENT: Pop up a menu asking the user if
	     * they wish to select/unselect all the channels in this node.
	     * For "selecting all channels", may want to indicate how many
	     * channels there are to add.  For example:
	     *         "Select all 25 channels"
	    else {
		
		if (!e.isPopupTrigger()) {
		    // Just reselect the currently selected paths and return.
		    // NOTE: When the user pressed on this folder node, the
		    //       folder node gets automatically selected. One
		    //       reason to call setSelectionPaths() and reselect
		    //       the currently selected paths is to *unselect* this
		    //       folder node.
		    // Since updateSelectedChannels() is called by the Swing
		    // event dispatch thread, can call setSelectionPaths()
		    // directly.
		    setSelectionPaths();
		    return;
		}
		
		// The user has clicked on a Server, Source, or other folder
		int totalNumLeafNodes = 0;
		int numPathsCurrentlySelected = 0;
		DefaultMutableTreeNode childNode = null;
		Vector vectorOfLeafPaths = new Vector();
		Vector vectorOfCurrentlySelectedLeafPaths = new Vector();
		for (int i=0; i<treeModel.getChildCount(selectedNode); ++i) {
		    childNode =
		    	(DefaultMutableTreeNode)
			    treeModel.getChild(selectedNode, i);
		    // Only select/deselect if this is a leaf node
		    if (childNode.isLeaf()) {
			totalNumLeafNodes++;
			TreePath path = getTreePathFromNode(childNode);
			vectorOfLeafPaths.add(path);
			if (selectedTreePaths.contains(path)) {
			    numPathsCurrentlySelected++;
			    vectorOfCurrentlySelectedLeafPaths.add(path);
			}
		    }
		}
		if (vectorOfLeafPaths.size() == 0) {
		    // This folder didn't contain any child leaf nodes (that is,
		    // it didn't contain any channels); just reselect the currently
		    // selected paths and return.
		    // NOTE: When the user pressed on this folder node, the folder
		    //       node gets automatically selected. One reason to
		    //       call setSelectionPaths() and reselect the currently
		    //       selected paths is to *unselect* this folder node.
		    // JPW 09/14/05: Since updateSelectedChannels() is called by
		    //               the Swing event dispatch thread, can call
		    //               setSelectionPaths() directly.
		    setSelectionPaths();
		    return;
		}
		else {
		    // Need to add support for this in actionPerformed()
		    JPopupMenu popupMenu = new JPopupMenu();
		    JMenuItem menuItem;
		    menuItem = new JMenuItem("Select all channels");
		    menuItem.addActionListener(parentDlg);
		    popupMenu.add(menuItem);
		    menuItem = new JMenuItem("Remove all channels");
		    menuItem.addActionListener(parentDlg);
		    popupMenu.add(menuItem);
		    popupMenu.show(
		        (Component)e.getSource(), e.getX(), e.getY());
		}
	    }
	    *
	    */
	    
	}
	
    } // end private class MouseHandlerClass
    
}

