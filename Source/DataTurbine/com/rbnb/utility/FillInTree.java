package com.rbnb.utility;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;

import com.rbnb.sapi.Sink;

import java.util.Iterator;


/**
  * Utility class to provide a fully-filled in ChannelTree for clients that
  *  need it.  Note that it does not browse through PlugIns, as they may form
  *  infinite loops.
  *
  * @author WHF
  * @version 05/01/2003
  */
public class FillInTree
{
	/**
	  * Creates a full map of an entire RBNB hierarchy, following routing
	  *  connections no matter what the cost.
	  * <p><strong>Note:</strong> The Sink provided is assumed to be already
	  *   connected to the desired hierarchy.
	  */
	public static ChannelTree buildFullTree(Sink sink)
		throws com.rbnb.sapi.SAPIException
	{
		ChannelMap map=new ChannelMap();
		
		map.Add("/...");
		
		sink.RequestRegistration(map);
		sink.Fetch(-1, map);
		
		ChannelTree result=ChannelTree.createFromChannelMap(map);
		return checkChildren(
			result,
			(ChannelTree.Node) result.rootIterator().next(),
			sink,
			map);
	}
	
	/**
	  * Test program to print to stdout the list of all channels in the
	  * RBNB hierarchy of which localhost:3333 is a part.
	  * <p>@see #buildFullTree(Sink)
	  */
	public static void main(String args[])
	{
		try {
		Sink sink=new Sink();
		sink.OpenRBNBConnection();
		System.out.println(buildFullTree(sink));
		} catch (com.rbnb.sapi.SAPIException se)
		{ se.printStackTrace(); }
	}
	
	/** 
	  * Utility to fill in the children of a specified node.  If the node 
	  *  is not a server or already has children, the input tree is returned.
	  *  This method assumes that the initial tree used with the method
	  *  was produced with RequestRegistration("/...").
	  */
	public static ChannelTree fillInChildren(
		ChannelTree tree,
		ChannelTree.Node toCheck,
		Sink sink) throws com.rbnb.sapi.SAPIException
	{
		if (toCheck.getType()==ChannelTree.SERVER 
				&& toCheck.getChildren().isEmpty())
		{
			ChannelMap map=new ChannelMap();
			map.Add(toCheck.getFullName()+"/...");
			sink.RequestRegistration(map);
			sink.Fetch(-1,map);
			tree=tree.merge(ChannelTree.createFromChannelMap(map));
		}
		return tree;
	}  
	
	/**
	  * Recursive to check if child is a server without children.  If so,
	  *  fills it in.
	  */
	private static ChannelTree checkChildren(
		ChannelTree tree,
		ChannelTree.Node toCheck, 
		Sink sink, 
		ChannelMap map) throws com.rbnb.sapi.SAPIException
	{
		Iterator iter=toCheck.getChildren().iterator();
		
		while (iter.hasNext())
		{
			ChannelTree.Node child=(ChannelTree.Node) iter.next();
			if (child.getType()==ChannelTree.SERVER) {
				if (child.getChildren().isEmpty())
				{
					map.Clear();
					map.Add(child.getFullName()+"/...");
					sink.RequestRegistration(map);
					sink.Fetch(-1, map);
					tree=tree.merge(ChannelTree.createFromChannelMap(map));				
				}
				// Note that we only check the children of servers, 
				// as no other objects can have servers as children.
				tree=checkChildren(
					tree,
					tree.findNode(child.getFullName()), 
					sink, 
					map);
			}
		}
		
		return tree;
	}
	
}
