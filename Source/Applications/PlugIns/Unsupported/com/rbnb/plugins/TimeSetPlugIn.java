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

// TimeSetPlugIn.java

package com.rbnb.plugins;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;


import com.rbnb.sapi.*;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.ChannelParse;

/**  
  * A plugin to generate folder views of RBNB time-series data.&nbsp; 
  *  Each source when viewed with this plugin will appear to have subchannels
  *  'Data' and 'Year'.&nbsp; The 'Data' subchannel will contain the original 
  *  data; the 'Year' subchannel will have one entry for every years worth of
  *  data available, such as 1970 through to 2002.&nbsp; Under the 2002 folder,
  *  say, there will be another 'Data' folder, as well as a 'Month' entry.
  *
  * @author WHF
  * @since V2.0B9
*/

//
// 01/02/2003  WHF  Created.
//
public class TimeSetPlugIn implements SimplePlugIn.PlugInCallback
{
	/**
	  * Passes generic options to the plugin handler.
	  */
	public void setOptions(Hashtable options) { this.options=options; }
	
	/**
	  * Handles the request defined by the provided <code>PlugInChannelMap.
	  *  </code>
	  */
	public void processRequest(PlugInChannelMap picm) throws SAPIException
	{ 
		Calendar date=new GregorianCalendar(java.util.TimeZone.getTimeZone(
			"GMT+0:00")); // grenwich mean, no daylight savings.
		date.clear();  // reset to the epoch
		
		String[] chans=picm.GetChannelList();
		picm.Clear();
		picm.PutTimeAuto("timeofday");
		
		cmap.Clear();
		checkSinkConnection();
		for (int ii=0; ii<chans.length; ++ii)
		{
			final String cname=chans[ii];
			ChannelParse cp=new ChannelParse(cname);
			ChannelMap reg=new ChannelMap();
			try {
			int depth=measureDepth(cp, date);
System.err.println("Date: "+date.getTime());

			if (cp.lastToken().equals("*")||cp.lastToken().equals("..."))
			{
				String base=cp.compose(0,cp.size()-1);
				if (base.length()==0) {
					picm.PutDataAsByteArray(picm.Add("rbnbSource/."),
						new byte[1]);
					continue;
				}
				if (cp.compose(depth,depth+1).equals("Data"))
				{
					// forward to client
					String toFetch
						=cp.compose(0,1)+'/'+cp.compose(depth+1,cp.size());
					cmap.Add(toFetch);
//FIXME: Should perform all fetches at one time:
sink.RequestRegistration(cmap); 
// date.getTime().getTime()/1e3, 0, "absolute");
sink.Fetch(-1,cmap);
System.err.println("toFetch: "+toFetch+", got "+cmap);
					String[] result=cmap.GetChannelList();
					picm.PutTime(date.getTime().getTime()/1e3,0.0);
					for (int iii=0; iii<result.length; ++iii)
					{
						int index=picm.Add(
							base+result[iii].substring(
							result[iii].indexOf('/')));
						picm.PutDataAsByteArray(index,new byte[1]);
					}
				}
				else if (depth<TIMETEXT.length&&TIMETEXT[depth]!=null) 
				{ // on a Data/Time level
					int index=picm.Add(base+"/Data/.");
					picm.PutDataAsByteArray(index, new byte[1]);
					index=picm.Add(base+'/'+TIMETEXT[depth]+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
				else // either requesting data or time
				{
//					if (depth!=cp.size()-1 
	//						|| cp.compose(depth-1,depth).equals("Data"))
					{
						addChannels(reg,picm,base,cp.compose(depth-1,depth), 
							date);
					}
				}
			} /*
			else if (cp.lastToken().equals("Year"))
			{
				int index=picm.Add(cname+"/.");
				picm.PutDataAsByteArray(index, new byte[1]);
			} */
			// Check if the depth permits a 'Data/somechan' sequence:
			else if (depth<cp.size()-1 
					&&cp.compose(depth,depth+1).equals("Data"))
			{
				// forward to client
				String toFetch
					=cp.compose(0,1)+'/'+cp.compose(depth+1,cp.size());
				cmap.Add(toFetch);
//FIXME: Should perform all fetches at one time:
sink.Request(
	cmap,
	date.getTime().getTime()/1e3,
	picm.GetRequestDuration(),
	"absolute"); 

sink.Fetch(-1,cmap);
System.err.println("toFetch (Request): "+toFetch+", got "+cmap);
				String[] result=cmap.GetChannelList();
				picm.PutTime(date.getTime().getTime()/1e3,0.0);
				for (int iii=0; iii<result.length; ++iii)
				{
					int index=picm.Add(cname);
					picm.PutDataAsByteArray(index,new byte[1]);
				}
			}
			else // requesting some directory, just give it back:
			{
				int index=picm.Add(cname+"/.");
				picm.PutDataAsByteArray(index, new byte[1]);
			} 
			} catch (BadChannelNameException bcne) { bcne.printStackTrace(); }
		} 
	}
	
	/**
	  * Prepares this object for reuse.  Return false if the object should
	  *  be discarded.  The object will also be discarded if this method
	  *  throws a run-time exception.
	  */
	public boolean recycle()
	{
		return true;
	}	

	public static void main(String args[])
	{
		SimplePlugIn spi=new SimplePlugIn();
		
		spi.setHandlerClass(TimeSetPlugIn.class);
		spi.setPlugInName("com.rbnb.plugins.TimeSetPlugIn");
		spi.run();
	}	
	
	/**
	  * Returns the index into the TIMETEXT array of the last level in the
	  *  specified channel.
	  */
	private int measureDepth(ChannelParse cp, Calendar date)
	{
		int depth=0;
		while (cp.hasMoreElements())
		{
			String level=cp.nextToken();
System.err.println("level="+level+", depth="+depth);
			if (depth%2!=0)
			{
				TimeSet ts=(TimeSet) timeSetMap.get(level);
				if (ts==null || !cp.hasMoreElements()) return depth;
				else {
					++depth;
					String value=cp.nextToken();
					try {
					ts.setField(date, Integer.parseInt(value));
					} catch (NumberFormatException nfe) { return depth; }
				}
			}
			++depth;
		}
		return depth;
//		return cp.size()-1;
	}
	
	/**
	  * Adds channels appropriate to the current depth being observed.
	  */
	private void addChannels(ChannelMap reg, PlugInChannelMap picm, 
			String base, String time, Calendar date) 
		throws BadChannelNameException, SAPIException
	{
		TimeSet ts=(TimeSet) timeSetMap.get(time);
		if (ts!=null)
			ts.addChannels(reg, picm, base, date);
		else throw new BadChannelNameException("Time reference \""
			+time+"\" not recognized.");
	}
	
	private void checkSinkConnection() throws SAPIException
	{
		try {
			sink.GetServerName();
		}
		catch (Exception e) {
		sink.OpenRBNBConnection(
			options.get("hostname").toString(),
			options.get("sinkname").toString(),
			options.get("user").toString(),
			options.get("password").toString());
		}
	}
		
	private Hashtable options;
	private final Sink sink=new Sink();
	private final ChannelMap cmap=new ChannelMap();
		
	private final static String[] TIMETEXT= {
		null,
		"Year",
		null,
		"Month",
		null,
		"Day",
		null,
		"Hour",
		null,
		"minute",
		null,
		"second"
	};
	
	private final static Hashtable timeSetMap=new Hashtable();
	private final static DecimalFormat twoDigitFormat=new DecimalFormat("00");
	
	static {
		timeSetMap.put("Year", new TimeSet() {
			public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
					String base, Calendar date) throws SAPIException {
				for (int ii=1970; ii<=2003; ++ii)
				{
					int index=picm.Add(base+'/'+ii+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
			}
			
			public void setField(Calendar date, int value) {
				date.set(Calendar.YEAR, value); 
			}
		});

		timeSetMap.put("Month", new TimeSet() {
			public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
					String base, Calendar date) throws SAPIException {
				for (int ii=1; ii<=12; ++ii)
				{
					int index=picm.Add(base+'/'+twoDigitFormat.format(ii)+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
			}
			
			public void setField(Calendar date, int value) {
				date.set(Calendar.MONTH, value-1+Calendar.JANUARY); 
				// months are zero based,  just to be inconsistent.
			}
		});

		timeSetMap.put("Day", new TimeSet() {
			public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
					String base, Calendar date) throws SAPIException {
				for (int ii=1; ii<=date.getActualMaximum(Calendar.DAY_OF_MONTH);
					++ii)
				{
					int index=picm.Add(base+'/'+twoDigitFormat.format(ii)+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
			}
			
			public void setField(Calendar date, int value) {
				if (date.isSet(Calendar.MONTH)) 
					date.set(Calendar.DAY_OF_MONTH, value);  // days are 
				else date.set(Calendar.DAY_OF_YEAR, value);  //  one based
			}
		});

		timeSetMap.put("Hour", new TimeSet() {
			public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
					String base, Calendar date) throws SAPIException {
				for (int ii=0; ii<=23; ++ii)
				{
					int index=picm.Add(base+'/'+twoDigitFormat.format(ii)+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
			}
			
			public void setField(Calendar date, int value) {
				date.set(Calendar.HOUR, value); 
			}
		});

		timeSetMap.put("minute", new TimeSet() {
			public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
					String base, Calendar date) throws SAPIException {
				for (int ii=0; ii<=59; ++ii)
				{
					int index=picm.Add(base+'/'+twoDigitFormat.format(ii)+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
			}
			
			public void setField(Calendar date, int value) {
				date.set(Calendar.MINUTE, value); 
			}
		});

		timeSetMap.put("second", new TimeSet() {
			public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
					String base, Calendar date) throws SAPIException {
				for (int ii=0; ii<=59; ++ii)
				{
					int index=picm.Add(base+'/'+twoDigitFormat.format(ii)+"/.");
					picm.PutDataAsByteArray(index, new byte[1]);
				}
			}
			
			public void setField(Calendar date, int value) {
				date.set(Calendar.SECOND, value); 
			}
		});
	}
	
	private interface TimeSet
	{
		public void addChannels(ChannelMap reg, PlugInChannelMap picm, 
			String base, Calendar date) throws SAPIException;
			
		public void setField(Calendar date, int value);
	}
}

class BadChannelNameException extends Exception
{
	public BadChannelNameException() {  }
	public BadChannelNameException(String msg) { super(msg); }	
}


