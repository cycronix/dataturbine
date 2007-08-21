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


package webTurbine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.*;
import javax.servlet.*;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;

// 2001  WHF  Created.
// 02/15/2002  WHF  Removed V1 compatability.
// 05/14/2002  WHF  Supports new meta-data search capabilities.
//

public class Viewer
{
	private final com.rbnb.sapi.Sink sink=new com.rbnb.sapi.Sink();
	private final static java.util.Map listIDMap =
		java.util.Collections.synchronizedMap(new HashMap());
	private final static Thread listWatch=
		new Thread(new ListThread(),"List Watch");
	
	static { listWatch.start(); }
	
	String //queryString, 
		time="0", duration="0", reference="newest", channels="...",
		keywords="", errorString="No error.", isMeta="false", convert="",
		convertOther="", listID="", toExpand="", currentServer="";
	boolean byFrame=true,
			submitHit=false;
	int toExpandCode=0;
	String ServerAddress;

	public Viewer()
	{
		try {
		ServerAddress=java.net.InetAddress.getLocalHost().getHostAddress()
			+":3333";
		} catch (Throwable t) { ServerAddress="localhost:3333"; }
	}
	
	public String getAddress() 
	{ return ServerAddress; }
	
	public void setAddress(String add) 
	{ ServerAddress=add;  }
/*	
	public String getQueryString() 
	{ return queryString; }
*/	
	public String getTime() 
	{ return time; }
	
	public void setTime(String t)
	{ time=t; }
	
	public String getReference()
	{ return reference; }
	
	public void setReference(String r)
	{ reference=r; }
	
	public String getDuration()
	{ return duration; }
	
	public void setDuration(String duration)
	{ this.duration=duration; }

	public String getByFrame() { return String.valueOf(byFrame); }	
	public void setByFrame(String byFrame) 
	{ if ("true".equals(byFrame)) this.byFrame=true; else this.byFrame=false; }
	
	public String getChannels() { return channels; }	
	public void setChannels(String channels) { this.channels=channels; }
	
	public String getKeywords() { return keywords; }
	public void setKeywords(String keywords) { this.keywords=keywords; }
	
	public String getMetaData() { return isMeta; }
	public void setMetaData(String isMeta) 
	{ this.isMeta=isMeta; submitHit=true; }
	
	public String getConvert() { return convert; }
	public void setConvert(String convert) { this.convert=convert; }
	
	public String getConvertOther() { return convertOther; }
	public void setConvertOther(String convertOther) 
	{ this.convertOther=convertOther; }
	
	public String getListID() { return listID; }
	public void setListID(String listID) { this.listID=listID; }

	public String getToExpand() { return toExpand; }
	public void setToExpand(String toExpand) 
	{ 
		this.toExpand=toExpand;
		try {
		toExpandCode=Integer.parseInt(toExpand,16);
		} catch (NumberFormatException nfe)
		{ toExpandCode=0; }
	}
	
	public String getCurrentServer() { return currentServer; }
	
	public String getErrorString() { return errorString; }

	/**
	  * Returns true if all fields have been set on the request.
	  */
	public boolean processRequest(HttpServletRequest request) 
	{
		return submitHit;
	}
    
	public LinkedList getChannelList() throws ServletException
	{
		if (listID.length()>0)
		{
			ListNode node=(ListNode) listIDMap.get(listID);
			if (node!=null)
			{
				ViewEntity ve=(ViewEntity) node.getEntityMap().get(toExpand);
				if (ve!=null)
					ve.setExpandSubList(!ve.isExpandSubList());
					
				currentServer=node.getCurrentServer();
				return node.getList();
			} // otherwise is no longer in cache, rebuild.
		}
		
		try { 
		sink.OpenRBNBConnection(ServerAddress,
			"WebTurbine.Viewer");
//		list=sink.GetChannelList(channels);
		String serverName=sink.GetServerName();
		
		ChannelMap 
			requestMap=new ChannelMap(),
			fetchedMap=new ChannelMap();
			
		requestMap.Add(channels==null?"...":channels);
		sink.RequestRegistration(requestMap);
		sink.Fetch(10000,fetchedMap);
		sink.CloseRBNBConnection();
		if (fetchedMap.NumberOfChannels()==0)
		{
			if (fetchedMap.GetIfFetchTimedOut())
				errorString="Timeout receiving registration.";
			else
				errorString="No channels match the specified channel string."
					+"&nbsp; Please	try a more general search expression.";
			listID="";
			currentServer="";
			return new LinkedList();
		}
		else
		{
			//String children[]=fetchedMap.GetServerList();
			ChannelTree tree = ChannelTree.createFromChannelMap(fetchedMap);
			ArrayList al = new ArrayList();
			for (Iterator iter = tree.iterator(); iter.hasNext(); ) {
				ChannelTree.Node node = (ChannelTree.Node) iter.next();
				if (node.getType() == ChannelTree.SERVER) 
					al.add(node.getFullName());
			}
			String children[] = new String[al.size()];
			al.toArray(children);
			
			// Servers in list will be sorted by their place in the hierarchy:
			//  parent(s) first, then the currently queried server, then the 
			//  currently queried servers children.
			int childIndex=0;
			if (channels.charAt(0)=='/')
			{
				currentServer=children[0];
				for (	; 
						childIndex<children.length
							&&channels.startsWith(children[childIndex]);
						++childIndex)
					currentServer=children[childIndex];
			}
			else
				currentServer=serverName;
				
			ListNode node=doSearch(fetchedMap,keywords);
			final LinkedList nodeList=node.getList();
			recurseList(nodeList,"",node.getEntityMap());

			ListIterator lIter=nodeList.listIterator();

			// Add parent:
			int lastSlash=currentServer.lastIndexOf('/');
			String parentName=currentServer.substring(0,lastSlash);
			if (lastSlash!=0)
			{
				ViewEntity ve=new ViewEntity(parentName,
					null);
				ve.setParent(true);
				lIter.add(ve);
				//node.map.put(ve.toString(),ve);
			}
			// Add children:
			for (int ii=childIndex; ii<children.length; ++ii)
			{
//				if (!"..".equals(children[ii])
	//				&&!parentName.equals(children[ii]))
				{ // exclude parent if included above
					if (children[ii].charAt(0)!='/')
						children[ii]=currentServer+'/'+children[ii];
					ViewEntity ve=new ViewEntity(children[ii],null);
					ve.setChild(true);
					lIter.add(ve);
					//node.map.put(ve.toString(),ve);
				}
			}
			
			listID=node.toString();
			listIDMap.put(listID,node);
			return nodeList;
		}
		} catch (Exception e)
		{ 
			sink.CloseRBNBConnection();
			throw new ServletException(e); 
		} 		
	}	

	private static void recurseList(LinkedList llist, String start, Map nodeMap)
	{
		ListIterator li=llist.listIterator();
		while (li.hasNext())
		{
			String newStart; 
			ViewEntity firstEntity=(ViewEntity) li.next();
			try {
			if (start.length()>firstEntity.link.length())
				continue;
			String token;
			int ind=firstEntity.link.indexOf('/',start.length());
			if (ind==-1) 
				token=firstEntity.link.substring(start.length());
			else
				token=firstEntity.link.substring(start.length(),ind);
				
			newStart=start+token+'/'; // used for next recursion
			firstEntity.str=token;
			} catch (RuntimeException e) 
			{	System.err.println(e.getMessage()+"\n\tstart = "
				+start+"\n\tfirstEntity = (\""+firstEntity.str+"\",\""
				+firstEntity.link+"\")"); 
				throw e; }
			LinkedList subList=new LinkedList();
			while (li.hasNext())
			{
				ViewEntity next=(ViewEntity) li.next();
				
				// Compare next entity with current string to see if it 
				//  starts with it.  If it is, add it to the current entities
				//  sublist.
				if (next.link.regionMatches(0,newStart,0,
					newStart.length())) // with slash
				{
					li.remove();
					subList.add(next);
				}
				else if (next.link.equals(newStart)) // exact
				{ } // do nothing
				else 
				{
					li.previous();
					break;  // since list is sorted, we
				//  know that if there is not a match there
				//   are no more matches.
				}
			}
			
			// Here we check to see if the current entity needs to be 
			// broken down further.  If so, it is placed into its own 
			// sublist:
			if (!newStart.equals(firstEntity.link)
				&&!newStart.regionMatches(0,
				firstEntity.link,0,
				firstEntity.link.length())) // without slash)
			{
				ViewEntity ve=new ViewEntity(
					firstEntity.str,
					firstEntity.link);
				nodeMap.put(ve.toString(),ve);
				subList.add(0,ve);
				firstEntity.link=null;
			}
			firstEntity.subList=subList;
			recurseList(subList, newStart,nodeMap);				
		} // end while hasNext		
	} // end recurseList	

	private ListNode doSearch(ChannelMap fetchedMap, 
		String keywords)
	{
		int[] result=fetchedMap.Search(null,keywords);
		
		final LinkedList hits=new LinkedList();
		final HashMap map=new HashMap();
		for (int ii=0; ii<result.length; ++ii)
		{
			String s=fetchedMap.GetName(result[ii]);
			ViewEntity ve;
			if (s.startsWith(currentServer)) 
				s=s.substring(currentServer.length()+1);
			ve=new ViewEntity(s,s);
			hits.add(ve);
			map.put(ve.toString(),ve);
		}  // end for
		if (hits.size()==0) errorString="No meta-data matches the search "
				+"criteria.";
		return new ListNode(hits,map,currentServer);
	}
		
	// Removes list references when more than an hour old.
	private static class ListThread implements Runnable
	{
		public void run()
		{
			try {
			while (true)
			{
				Thread.sleep(3600000);	// Sleep for one hour
				
				long currTime=System.currentTimeMillis();
				synchronized (listIDMap)
				{
					Iterator iter=listIDMap.values().iterator();
					while (iter.hasNext())
					{
						ListNode node=(ListNode) iter.next();
						if (node.getAccessTime()+3600000<currTime)
						{ // has not been accessed for over an hour
							iter.remove();
						}
					}
				}
			}
			} catch (InterruptedException ie)
			{ } // Thread interrupted when process exits
		}
	}			
}// end class Viewer

class ListNode
{
	private final LinkedList list;
	private final Map entityMap;
	private final String currentServer;
	private long accessTime;
	
	public ListNode(LinkedList list, Map entityMap, String currentServer)
	{ 
		this.list=list; this.entityMap=entityMap; 
		this.currentServer=currentServer;
		accessTime=System.currentTimeMillis();
	}
	
	public LinkedList getList() 
	{ accessTime=System.currentTimeMillis(); return list; }
	
	public Map getEntityMap() 
	{ return entityMap; }
	
	public String getCurrentServer() 
	{ return currentServer; }
	
	public long getAccessTime()
	{ return accessTime; }

	public String toString() { return Integer.toHexString(hashCode()); }
}



