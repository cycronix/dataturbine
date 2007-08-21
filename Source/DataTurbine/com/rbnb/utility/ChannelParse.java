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

/**
  * A channel name parsing utility independent of the Rmap package, which may
  *  be obfuscated.
  * <p>
  * @author WHF
  * @since V2.0B9
  */
public class ChannelParse implements java.util.Enumeration
{ 
	public ChannelParse(String chan)
	{
		java.util.StringTokenizer st=new java.util.StringTokenizer(chan,"/");
		
		curr=0;
		tokens=new String[st.countTokens()];
		while (st.hasMoreElements()) tokens[curr++]=st.nextToken();
		curr=0;
	}
	
	public boolean hasMoreElements() 
	{
		return curr<tokens.length;
	}
	
	public Object nextElement()
	{
		return nextToken();
	}

	public String nextToken()
	{
		try {
		return tokens[curr++];
		} catch (IndexOutOfBoundsException ioobe) 
		{ throw new java.util.NoSuchElementException(); }
	}
	
	public String lastToken() 
	{ 
		if (tokens.length==0) return "";
		return tokens[tokens.length-1]; 
	}
	
	public int size()
	{
		return tokens.length;
	}
	
	/**
	  * Builds a channel name based on the contents of this parser,
	  *  and the start (inclusive) and end (exclusive) indices.&nbsp; The 
	  *  resulting string neither begins nor ends with a slash.
	  * <p>If start==end, "" is returned.
	  * <p>
	  * @throws NoSuchElementException If start&lt;0 or start&gt;end or 
	  *   end&gt;size().
	  */
	public String compose(int start, int end)
	{
		if (start<0 || start>end || end>tokens.length) 
			throw new java.util.NoSuchElementException();
		if (start==end) return "";
		
		StringBuffer sb=new StringBuffer();
		for (int ii=start; ii<end-1; ++ii) {
			sb.append(tokens[ii]);
			sb.append('/');
		}
		sb.append(tokens[end-1]);
		return sb.toString();
	}
	
	private int curr;
	private final String[] tokens;	
}

