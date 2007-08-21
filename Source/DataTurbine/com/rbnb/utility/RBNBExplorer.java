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

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;

import java.util.Iterator;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
  * A tree view for RBNB hierarchies.
  *<p>
  * @author WHF
  * @since V2.6
  * @version 2004/12/28
*/

/* History:
	2004/12/28  WHF  Created.
*/

public class RBNBExplorer extends JScrollPane
{
// ***************************  Construction  *******************************//
	public RBNBExplorer()
	{
		setViewportView(treeView);
		treeView.setEditable(false);
	}
	
// ***************************  Methods  ************************************//
	/**
	  * The specified node type is added to the list of those not displayed.
	  */
	public void addDisplayFilter(ChannelTree.NodeTypeEnum toFilter)
	{
		displayFilter.add(toFilter);
		invalidate();
	}

	/**
	  * Convenience method which creates a ChannelTree from the provided map,
	  *  and then calls {@link #display(ChannelTree)}.
	  */
	public void display(ChannelMap cm)
	{
		display(ChannelTree.createFromChannelMap(cm));
	}
	
	public void display(ChannelTree tree)
	{
		treeView.setModel(new ChannelTreeModel(tree, displayFilter, dispSys));			
	}
	
// **************************  Accessors  ***********************************//
	/**
	  * If true, system sources are shown.  System sources are currently 
	  *  defined as those without leading underscores.  False by default.
	  */
	public boolean getDisplaySystemChannels() { return dispSys; } 

	/**
	  * @see #getDisplaySystemChannels()
	  */
	public void setDisplaySystemChannels(boolean dispSys)
	{ this.dispSys = dispSys; }
	
	/**
	  * Returns the currently selected node, or null if no selection has been
	  *  made.
	  */
	public ChannelTree.Node getSelectedNode()
	{
		Object lspc = treeView.getLastSelectedPathComponent();
		if (lspc != null)
			return ((ChannelTreeModel.NodeWrapper) lspc).getNode();
		return null;
	}
	
	/**
	  * Access the tree for customization, adding listeners, etc.
	  */
	public JTree getTree() { return treeView; }

	/**
	  * Test method.
	  */
	public static void main(String[] args)
	{
		javax.swing.JFrame jf = new javax.swing.JFrame("Test");
		jf.setDefaultCloseOperation(jf.DISPOSE_ON_CLOSE);
		RBNBExplorer re = new RBNBExplorer();
		re.addDisplayFilter(ChannelTree.SINK);
		//re.setDisplaySystemChannels(true);
		jf.getContentPane().add(re);

		try {		
			com.rbnb.sapi.Sink sink = new com.rbnb.sapi.Sink();
			sink.OpenRBNBConnection();
			ChannelMap cm = new ChannelMap();
			cm.Add("/...");
			sink.RequestRegistration(cm);
			cm = sink.Fetch(-1);
			sink.CloseRBNBConnection();
			ChannelTree ct = ChannelTree.createFromChannelMap(cm);
			System.err.println("Tree to display:\n"+ct);
			re.display(ct);
			jf.setVisible(true);
		} catch (SAPIException se) { se.printStackTrace(); }
	}
	
// ************************  Data Members  **********************************//
	private final JTree treeView = new JTree();
	private final java.util.HashSet displayFilter = new java.util.HashSet();
	private boolean dispSys = false;
} // end class RBNBExplorer

/**
  * A wrapper around a ChannelTree for use with a JTree.  Like the ChannelTree
  *   it contains, this model is immutable.
  */
class ChannelTreeModel implements TreeModel
{
	public ChannelTreeModel(ChannelTree tree, Set displayFilter,
			boolean dispSys)
	{
		this.tree = tree;
		this.displayFilter = displayFilter;
		this.dispSys = dispSys;
	}
	
// **********************  TreeModel Interface Methods  *********************//
	/**
	  * Does nothing, as the tree never changes.
	  */
	public void addTreeModelListener(TreeModelListener tml) {}
	
	public Object getChild(Object parent, int index)
	{
		ChannelTree.Node node = //(ChannelTree.Node) parent;
				((NodeWrapper) parent).getNode();
		if (displayFilter.isEmpty() && dispSys) // use all nodes
			return new NodeWrapper(node.getChildren().get(index));
		else {
			int count = 0;
			for (Iterator iter=node.getChildren().iterator(); iter.hasNext();) {
				node = (ChannelTree.Node) iter.next();
				if (!filter(node)) { // don't filter
					if (count == index) return new NodeWrapper(node);
					++count;
				}
			}
			return new IndexOutOfBoundsException(""+index+" >= "+count);
		}
	}
	
	public int getChildCount(Object parent)
	{
		ChannelTree.Node node = //(ChannelTree.Node) parent;
				((NodeWrapper) parent).getNode();
		if (displayFilter.isEmpty() && dispSys) // use all nodes
			return node.getChildren().size();
		else {
			int count = 0;
			for (Iterator iter=node.getChildren().iterator(); iter.hasNext();) {
				node = (ChannelTree.Node) iter.next();
				if (!filter(node)) { // don't filter
					++count;
				}
			}
			return count;
		}
	}
	
	public int getIndexOfChild(Object parent, Object child)
	{
		ChannelTree.Node node = //(ChannelTree.Node) parent;
				((NodeWrapper) parent).getNode();
		if (displayFilter.isEmpty() && dispSys) // use all nodes
			return node.getChildren().indexOf(child);
		else {
			int count = 0;
			for (Iterator iter=node.getChildren().iterator(); iter.hasNext();) {
				node = (ChannelTree.Node) iter.next();
				if (!filter(node)) { // don't filter
					if (child.equals(node)) return count;
					++count;
				}
			}
			return -1;
		}			
	}
	
	/**
	  * Obtains the first available root.
	  *
	  * @return The first value returned by {@link ChannelTree#rootIterator()},
	  *  or <b>null</b>.
	  */
	public Object getRoot() 
	{ 
		return tree.rootIterator().hasNext()
				?new NodeWrapper(tree.rootIterator().next()):null;
	}	

	/**
	  * Channels are considered leaves.
	  *
	  * @return <b>true</b> if the node is a {@link ChannelTree#CHANNEL}; 
	  * otherwise <b>false</b>.
	  */
	public boolean isLeaf(Object _node)
	{
		ChannelTree.Node node = //(ChannelTree.Node) _node;
				((NodeWrapper) _node).getNode();
		if (node.getType() == ChannelTree.CHANNEL) return true;
		return false;
	}
	
	/**
	  * Does nothing, as there is nothing to report.
	  */
	public void removeTreeModelListener(TreeModelListener tml) {}
	
	/**
	  * As this is a read only tree, this will do nothing.
	  */
	public void valueForPathChanged(TreePath path, Object newValue) { }
	
// *************************  Other Methods  ********************************//
	/**
	  * @return true if and only if the node should be filtered (not displayed).
	  */
	private boolean filter(ChannelTree.Node node)
	{
		return displayFilter.contains(node.getType()) 
				|| (!dispSys && ChannelTree.SOURCE.equals(node.getType())
					&& node.getName().charAt(0) == '_');
	}
	
	/**
	  * Wrapper which controls the formatting of nodes.
	  */
	static class NodeWrapper
	{
		NodeWrapper(Object _node)
		{
			node = (ChannelTree.Node) _node;
		}
		
		ChannelTree.Node getNode() { return node; }
		
		public String toString() { return node.getName(); }
		
		private final ChannelTree.Node node;
	}
	
	private ChannelTree tree;
	private final Set displayFilter;
	private final boolean dispSys;
	
} // end class ChannelTreeModel


