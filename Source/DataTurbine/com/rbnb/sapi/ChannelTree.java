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

package com.rbnb.sapi;

//import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.rbnb.api.Rmap;

/**
  * A <code>ChannelMap</code> accessory which provides a read-only hierarchical
  * view of channels.  This is useful mainly for clients who wish to present
  * such a view to end users, such as RBNB Admin. 
  * <p>
  * The <code>ChannelTree</code> is created from a <code>ChannelMap</code>
  * via a factory method,
  * {@link #createFromChannelMap(ChannelMap)}.  The tree consists of objects 
  * of type <code>ChannelTree.Node</code>.  Each node represents a name
  * between slashes.
  * <p>
  * For example, a ChannelMap consisting of the channel: 
  * '/Server/source/c0' would yield a root node 'Server', with a child 
  * 'source', who has a child 'c0'.  Note that for user constructed 
  * ChannelMaps, it is possible to have multiple roots.  So a map with the
  * channels 'foo' and 'bar' would yield a "tree" with two roots, 
  * 'foo' and 'bar'.
  * <p>
  * For an explanation of how name matching works, see
  * <code>Sink.Request</code>.
  * <p>
  * Requires Java 1.2 or later.
  * <p>
  *
  * @see com.rbnb.sapi.Sink#Request(com.rbnb.sapi.ChannelMap cm,
  *				    double start,
  *				    double duration,
  *				    String reference)
  * @author WHF
  * @since V2.2
  * 
*/

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 2005/10/06  WHF  Added ChannelTree.createFromChannelMap(ChannelMap, String)
 * 2005/08/10  WHF  ChannelTree.Node.equals() returns false if the input is
 *						null.
 * 2005/03/21  WHF  Always include start & duration information in channel 
 						nodes, if available.
 * 08/13/2004  INB	Added link to Sink.Request.
 * 05/14/2003  WHF	Fixed ordering of children after merge.
 * 04/24/2003  WHF	Created.
*/

public final class ChannelTree implements java.io.Serializable
{
	/** 
	  * Construction is not possible.  Use the Factory method.
	  */
	private ChannelTree() { }
	
	/**
	  * A deep copy of a tree.
	  */
	private ChannelTree(ChannelTree x) 
	{
		ChannelTree.Node prev=null;
		for (Iterator iter=x.iterator(); iter.hasNext(); ) {
			ChannelTree.Node node=(ChannelTree.Node) iter.next(),
				parent=(node.parent==null?null
					:(ChannelTree.Node) map.get(node.parent.fullName)),
				newNode=new ChannelTree.Node(node, parent);
			if (parent!=null) parent.addChild(newNode); 
			else rootList.add(newNode);
			map.put(newNode.fullName, newNode);
		}
	}
	
	/**
	  * Creates a new read only view of the provided channel map.
	  * 
	  * @throws NullPointerException if map is null.
	  */
	public static ChannelTree createFromChannelMap(ChannelMap cmap) 
	{
		try {
			ChannelTree tree=new ChannelTree();		
			Rmap rmaps=cmap.produceOutput();
			tree.forEachNamedNode(cmap, null, rmaps, 0, 0);
			
			return tree;
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }		
	}
	
	/**
	  * Creates a new read only view of the provided channel map with a 
	  *  single named root node.  The  
	  *  faux root node has the name provided and is of type {@link #FOLDER}.
	  *  It forms the base of the hierarchy, such that all roots in the 
	  *  ChannelMap are children of this root, and it is the only root.
	  * 
	  * @throws NullPointerException if the map of root name is null.
	  * @throws IllegalArgumentException if the root name is empty.
	  * @since V2.5B8
	  */
	public static ChannelTree createFromChannelMap(
			ChannelMap cmap,
			String rootName)
	{
		try {
			ChannelTree tree=new ChannelTree(); 
			/* doesn't work because the rmap functions are used to get full 
					names:
			Node node = new Node(
					rootName,
					rootName,
					null,
					ChannelTree.FOLDER,
					0
			);
					
			tree.rootList.add(node);
			tree.map.put(node.getFullName(), node); */

			Rmap rmaps=cmap.produceOutput();
			Rmap newRoot = new Rmap(rootName);
			newRoot.addChild(rmaps);
			tree.forEachNamedNode(cmap, null, newRoot, 0, rootName.length()+2);
			// Restore the hierarchy to its original form:
			newRoot.removeChild(rmaps);
			
			return tree;
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }						
	}
	
	/**
	  * Produces a new tree, which is the result of merging <b>this</b>
	  *  with the map toMerge.  In the case of a discrepancy in node type,
	  *  the <strong>this</strong> tree will win.
	  * <p>In general, <code>bigTree.merge(smallTree)</code> is faster than
	  *  <code>smallTree.merge(bigTree)</code>.
	  */
	public ChannelTree merge(ChannelTree toMerge) 
	{
		ChannelTree tree=new ChannelTree(this); //(ChannelTree) this.clone();
	
		for (Iterator iter=toMerge.iterator(); iter.hasNext(); ) {
			ChannelTree.Node node=(ChannelTree.Node) iter.next();
			if (!tree.map.containsKey(node.getFullName())) {
				if (node.getParent()!=null) {
					ChannelTree.Node parent=(ChannelTree.Node) tree.map.get(
						node.getParent().getFullName());
					ChannelTree.Node newNode=new ChannelTree.Node(node, parent);
					tree.map.put(node.getFullName(), newNode);
					parent.addChild(newNode);
				} else {
					ChannelTree.Node newNode=new ChannelTree.Node(node, null);
					tree.map.put(node.getFullName(), newNode);
					addSorted(tree.rootList, newNode);
				}
			}
		}
/*
		recursiveMerge(
			this.rootIterator(),
			toMerge.rootIterator(),
			tree.rootList,
			null,
			false,
			0);
*/		
		return tree;
	}
	
/* approach #1, not complete.
	private void recursiveMerge(
		Iterator iter1, 
		Iterator iter2,
		List childList,
		ChannelTree.Node parent,
		boolean toClone,
		int depth)		
	{
		while (iter1.hasNext() && iter2.hasNext()) {
			ChannelTree.Node node1=(ChannelTree.Node) iter1.next(),
				node2=(ChannelTree.Node) iter2.next();
			int compres=node1.compareTo(node2);
			if (compres < 0) { // node1 first
				childList.add(node1); // and its hierarchy!
				iter2.previous(); // go back
			}
			else if (compres == 0) { // node1 == node2
				ChannelTree.Node newNode=new Node(
					node1.getName(),
					node1.getName(),
					null,
					node1.getType(),
					0);					
			}
			else { // node2 first
				tree.rootList.add(node2);
				iter1.previous();
			}			
		}
			
		while (iter1.hasNext()) { // finish out iter1 if iter2 quit first
			tree.rootList.add(iter1.next());
		}
		
		while (iter2.hasNext()) { // finish out iter2 if iter1 quit first
			tree.rootList.add(iter2.next());
		}
	}
*/	
	/**
	  * Returns the node which exactly matches the given full name, or null.
	  */
	public ChannelTree.Node findNode(String fullname)
	{		
		return (ChannelTree.Node) map.get(fullname); 	
	}
	
	/**
	  * Allows iteration over the root nodes only.
	  */
	public Iterator rootIterator()
	{
		return rootList.iterator();
	}
	
	/**
	  * Allows iteration over the entire tree.  The traversal is guaranteed to
	  *  be alphabetical by fully qualified channel name.  Therefore, 
	  *  "/Server/foo/bar/c0" will come before "/Server/kluge".
	  */
	public Iterator iterator()
	{
		return new RecursingIterator(rootList.iterator());
	}
	
	/**
	  * Uses {@link #iterator} to traverse this Tree and provide a 
	  *  hierarchical debug output.
	  */
	public String toString()
	{
		Iterator iter=iterator();
		StringBuffer sb=new StringBuffer(this.getClass().getName());
		sb.append(" with ");
		sb.append(map.size());
		sb.append(" nodes.");
		while (iter.hasNext())
		{
			Node node=(Node) iter.next();
			sb.append('\n');
			for (int ii=0; ii<node.getDepth(); ++ii)
				sb.append('\t');
			sb.append(node);
		}
		return sb.toString();
	}

// **************************** Private Methods *****************************//
	
	/** 
	  * Recursive utility function to create node hierarchy.
	  * 
	  * @param prefix  Number of characters to delete when finding the 
	  *    channel for the node in cmap.
	  */
	  
	// 05/02/2003  WHF  Original Folder vs. Channel identification scheme
	//   was to check for named children.  Does not work for channel maps
	//   produced with *.  The new approach: if an Rmap has a child with a 
	//   null name and times, it is a Channel, otherwise it is a Folder.
	// 2005/03/21  WHF  Place start & duration into the ChannelTree 
	//   regardless of the mime type of the data.
	// 2005/10/07  WHF  Added prefix parameter, which is used to map
	//   rooted channel tree full names to rootless channel map names.
	private void forEachNamedNode(
		ChannelMap cmap, 
		Node lastNode, 
		Rmap rmap, 
		int depth,
		int prefix)
		throws Exception
	{
		if (rmap.getName()!=null&&!".".equals(rmap.getName())) {
			Node node=new Node(
				rmap.getName(),
				rmap.getFullName(),
				lastNode,
				findNodeType(rmap),
				depth);
			if (lastNode==null)
				rootList.add(node);
			else lastNode.addChild(node);
			
			map.put(node.getFullName(), node);
			lastNode=node;
			++depth;
			
			// Set registration map fields if available:
			if (node.getType()==CHANNEL) {
				int index=cmap.GetIndex(node.getFullName().substring(prefix));
				if (index>=0) {
					node.start=cmap.GetTimeStart(index);
					node.duration=cmap.GetTimeDuration(index);
					if (cmap.GetType(index) == cmap.TYPE_STRING
							&& "text/xml".equals(cmap.GetMime(index))) {
						String [] result=cmap.GetDataAsString(index);
						node.parseXml(result[0]);
					}
				}
			} // otherwise leave node defaults.					
		}
		int n=rmap.getNchildren();
		for (int ii=0; ii<n; ++ii) {
			forEachNamedNode(cmap, lastNode, 
					rmap.getChildAt(ii), depth, prefix);
		}
	}
	
// ************************** Static helper methods *************************//
	private static NodeTypeEnum findNodeType(Rmap r) throws Exception
	{
		if (r instanceof com.rbnb.api.Controller) return CONTROLLER;
		if (r instanceof com.rbnb.api.Sink) return SINK;
		if (r instanceof com.rbnb.api.Source) return SOURCE;
		if (r instanceof com.rbnb.api.PlugIn) return PLUGIN;
		if (r instanceof com.rbnb.api.Server) return SERVER;
		if (r.getDblock()!=null) return CHANNEL;
		if (r.getNchildren()>0)
		{
			Rmap child=r.getChildAt(0);
			if (child.getName()==null 
					&& (child.getTrange()!=null || child.getDblock()!=null))
				return CHANNEL;
		}
		return FOLDER; 
	}
	
	/**
	  * Adds a node to a list with the correct ordering.
	  * @throws Error if the node is already present.
	  */
	private static void addSorted(List list, Node node)
	{
		int index=Collections.binarySearch(list, node);
		if (index>=0) throw new Error(
			"Logic error: An attempt was made to add a node to a list which"
			+" already contained that node.  Please check the ChannelTree "
			+"implementation.");
		list.add(-index-1, node);
	}
	
// **************************** Private instance data ************************//
	private final HashMap map=new HashMap();
	private final LinkedList rootList=new LinkedList();
	
// *************************** Static constants *****************************//	
	static final long serialVersionUID = -5459292206096099221L;

	/**
	  * The node type which represents an RBNB control client.
	  */
	public static final NodeTypeEnum CONTROLLER 
		= new NodeTypeEnum("Controller");
	
	/**
	  * The node type which represents an RBNB server.
	  */
	public static final NodeTypeEnum SERVER = new NodeTypeEnum("Server");
	
	/**
	  * The node type which represents a data source.
	  */
	public static final NodeTypeEnum SOURCE = new NodeTypeEnum("Source");
	
	/**
	  * The node type which represents a PlugIn (a dynamic data source).
	  */
	public static final NodeTypeEnum PLUGIN = new NodeTypeEnum("PlugIn");

	/**
	  * The node type which represents a data sink.
	  */
	public static final NodeTypeEnum SINK = new NodeTypeEnum("Sink");

	/**
	  * The node type which represents a organizational node without data.
	  */
	public static final NodeTypeEnum FOLDER = new NodeTypeEnum(
		"Folder");

	/**
	  * The node type which represents a data bearing channel node.
	  */
	public static final NodeTypeEnum CHANNEL = new NodeTypeEnum(
		"Channel");
	
	public static final ChannelTree EMPTY_TREE = new ChannelTree();
	
	/**
	  * Represents a piece of a channel name.  Each node has several properties
	  *   which define its role in a hierarchy.
	  * <p>For example, the channel "/Server/Source/directory/channel"
	  *   would produce the following nodes:
	  * <ol>
	  *		<li>"Server", full name "/Server", type {@link ChannelTree#SERVER}
	  *			</li>
	  *		<li>"Source", full name "/Server/Source", type 
	  *			{@link ChannelTree#SOURCE} </li>
	  *		<li>"directory", full name "/Server/Source/directory", type
	  *			{@link ChannelTree#FOLDER} </li>
	  *		<li>"channel", full name "/Server/Source/directory/channel", type
	  *			{@link ChannelTree#CHANNEL} </li>
	  * </ol>
	  */
	public final static class Node implements java.io.Serializable, Comparable
	{
		/**
		  * No outside construction possible.
		  */
		Node(
			String _name,
			String _fullName,
			Node _parent,			
			ChannelTree.NodeTypeEnum _type,
			int _depth) 
		{
			name=_name;  fullName=_fullName;  parent=_parent;
			type=_type; depth=_depth;
		}
		
		Node(
			Node x,
			Node parent)
		{
			this.parent=parent;
			this.name=x.name;
			this.fullName  = x.fullName;
			this.type  = x.type;
			this.depth  = x.depth;
			this.size=x.size;
			this.mime=x.mime;
			this.start=x.start;
			this.duration=x.duration;
		}			
		
		/**
		  * Utility to add a child to this node during hierarchy construction.
		  */
		private void addChild(Node child)
		{
//			children.add(child);
			addSorted(children, child);
			childrenMap.put(child.name, child);
		}
		
		/**
		  * Returns the parent of this node, or <strong>null</strong>.
		  */
		public Node getParent() { return parent; }
		
		/**
		  * Returns a {@link List} of the children of this node.
		  *  If there are no children, the list will be empty.
		  */
		public List getChildren() { return readOnlyChildren; }
		
		/**
		  * Returns a {@link Map} of the children of this node.
		  *  If there are no children, the map will be empty.
		  */
		public Map getChildrenMap() { return readOnlyMap; }
		
		/**
		  * Returns the type of this node.
		  * @see ChannelTree#SERVER
		  * @see ChannelTree#SOURCE
		  * @see ChannelTree#PLUGIN
		  * @see ChannelTree#SINK
		  * @see ChannelTree#FOLDER
		  * @see ChannelTree#CHANNEL
		  */
		public ChannelTree.NodeTypeEnum getType() { return type; }
		
		/**
		  * Necessary because the node type (specifically Folder vs.
		  *  Channel) may not be known at construction time.
		  */
		private void setType(ChannelTree.NodeTypeEnum type) { this.type=type; }
		
		/**
		  * Returns the depth of the node.  The depth is defined as the 
		  *  number of recursive calls to {@link #getParent} which may be made
		  *  before returning <b>null</b>.
		  *  <p>For example, if <code>node.getParent().getParent()</code>
		  *  does not return 
		  *  <b>null</b>, but <code>node.getParent().getParent().getParent()
		  *  </code> does, than the depth is 2.
		  */
		public int getDepth() { return depth; }
		
		/**
		  * Returns the name of this node, which will not contain slashes.
		  */
		public String getName() { return name; }
		
		/**
		  *  Returns the fully qualified name of this node.
		  */
		public String getFullName() { return fullName; }
		
		/**
		  * Performs a lexicographical comparison of the full names of <strong>
		  *  this</strong> and the argument, which must be of type node.
		  *
		  * <p>
		  * @see String#compareTo(Object)
		  * @throws ClassCastException If the argument is not a <code>
		  *  ChannelTree.Node</code>.
		  * @throws NullPointerException if o is null.
		  */
		public int compareTo(Object o)
		{
			return fullName.compareTo(((ChannelTree.Node) o).getFullName());
		}
		
		/**
		  * Compares the full names of <strong>this</strong> and the argument
		  *  (of type node) for equality.
		  *
		  * <p>
		  * @see String#equals(Object)
		  * @see #compareTo(Object)
		  * @throws ClassCastException If the argument is not a <code>
		  *  ChannelTree.Node</code>.
		  *
		  * @since V2.5B6.
		  */
		public boolean equals(Object o)
		{
			if (o == null) return false;
			return fullName.equals(((ChannelTree.Node) o).getFullName());
		}
		
		/**
		  * The returned string the node's name and type.
		  */
		public String toString() 
		{
			StringBuffer sb=new StringBuffer(name);
			sb.append(" (");
			sb.append(type);
			if (type==CHANNEL) {
				sb.append(", size=");
				sb.append(size);
				sb.append(", start=");
				sb.append(new java.util.Date((long) (start*1e3)));
				sb.append(", duration=");
				sb.append(duration);
				sb.append(", mime=");
				sb.append(mime);
			}
			sb.append(")");
			return sb.toString();
		}
		
		/** 
		  * Returns the MIME type of this channel node.
		  * If this map was built from a registration channel map
		  *  (i.e. the result of <code>Sink.RequestRegistration()</code>),
		  *  it contains the MIME type of a data request made of this channel.
		  *  Otherwise returns <strong>null</strong>.
		  *<p>
		  * @see Sink#RequestRegistration
		  */
		public String getMime() { return mime; }
		
			
		/** 
		  * Returns the size in bytes of a newest request on this 
		  *  channel.  
		  * If this map was built from a registration channel map
		  *  (i.e. the result of <code>Sink.RequestRegistration()</code>),
		  *  it contains the MIME type of a data request made of this channel.
		  *  Otherwise returns <strong>null</strong>.
		  *<p>
		  * @see Sink#RequestRegistration
		  */
		public int getSize() { return size; }
		
		/**
		  * Returns the start time of the oldest data in this channel.
		  * If this map was built from a registration channel map
		  *  (i.e. the result of <code>Sink.RequestRegistration()</code>),
		  *  it contains the MIME type of a data request made of this channel.
		  *  Otherwise returns <strong>null</strong>.
		  *<p>
		  * @see Sink#RequestRegistration
		  * @see ChannelMap#GetTimeStart(int)
		  */
		public double getStart() { return start; }
		
		/**
		  * Returns the difference between the timestamp of the newest data 
		  *  and the timestamp of the oldest data in this channel.
		  * If this map was built from a registration channel map
		  *  (i.e. the result of <code>Sink.RequestRegistration()</code>),
		  *  it contains the MIME type of a data request made of this channel.
		  *  Otherwise returns <strong>null</strong>.
		  *<p>
		  * @see Sink#RequestRegistration
		  * @see ChannelMap#GetTimeDuration(int)
		  */
		public double getDuration() { return duration; }
		
		/**
		  * Extracts registration meta-data from the server-side XML.  Could
		  *  use SAX, but doesn't.
		  */
		private void parseXml(String toParse)
		{
			final String sizeField="<size>", mimeField="<mime>";
			
			if (toParse.indexOf("<rbnb>")!=-1)
			{
				int index=toParse.indexOf(sizeField);
				if (index!=-1) {
					index+=sizeField.length();
					int endIndex=toParse.indexOf('<', index);
					try {
						size=Integer.parseInt(toParse.substring(index,
							endIndex).trim());
					} catch (NumberFormatException nfe) { }
				}
				index=toParse.indexOf(mimeField);
				if (index!=-1) {
					index+=mimeField.length();
					int endIndex=toParse.indexOf('<', index);
					mime=toParse.substring(index, endIndex).trim();
				}
			}
		}
		
// ************************** Node Internal Data ****************************//		
		static final long serialVersionUID = 6312795055754506624L;
		
		private final String name, fullName;
		private final LinkedList children=new LinkedList();
		private final HashMap childrenMap=new HashMap();
		private final List readOnlyChildren
			=Collections.unmodifiableList(children);
		private final Map readOnlyMap
			=Collections.unmodifiableMap(childrenMap);
		
		private final int depth;
		private ChannelTree.NodeTypeEnum type;
		private final ChannelTree.Node parent;
		
		// Optional registration fields:
		private String mime=null;
		private int size=-1;
		private double start=0.0;
		private double duration=0.0;
		
	} // end class ChannelTree.Node

	/**
	  * Enumerated type for various channel node types.  TODO: Not atomically 
	  *  equal after serialization.
	  */
	public static class NodeTypeEnum implements java.io.Serializable
	{
		/** 
		  * No public construction.
		  */
		NodeTypeEnum(String typename) { this.typename=typename; }
		
		public String toString() { return typename; }
		
		private final String typename;
		
		static final long serialVersionUID = 8888487910653971066L;

	}  // end class ChannelTree.NodeTypeEnum

	private static class RecursingIterator implements Iterator
	{
		public RecursingIterator(Iterator rootIterator)
		{ iteratorStack.push(rootIterator); }
		
		public boolean hasNext() 
		{
			if (iteratorStack.empty()) return false;
			return ((Iterator) iteratorStack.peek()).hasNext(); 
		}
		
		public Object next() 
		{
			if (iteratorStack.empty()) 
				throw new java.util.NoSuchElementException();
			
			Node node=(Node) ((Iterator) iteratorStack.peek()).next();
			if (!node.children.isEmpty())
				iteratorStack.push(node.children.iterator());
			else while (!((Iterator) iteratorStack.peek()).hasNext()) {
				iteratorStack.pop();
				if (iteratorStack.empty()) break;
			}
			return node;
		}
		
		public void remove() 
		{ 
			throw new UnsupportedOperationException(
				"The ChannelTree is a read only view of the ChannelMap.");
		} 
		
		private final Stack iteratorStack=new Stack();
	} // end class ChannelTree.RecursingIterator

} // end class ChannelTree


