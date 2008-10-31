/*
	ChartServlet.java
	
	Copyright Creare Inc.  2008.  All rights reserved.

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at 
	
	http://www.apache.org/licenses/LICENSE-2.0 
	
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.
	
	
	Note that this servlet depends upon JFreeChart, which is released as 
	open source under the Lesser GPL.  You may find the source and license
	in the Third_Party area.
	

	***  History  ***	
	2004/02/06  WHF  Only plot data which does not already have a meaningful
		 mime type.
	2004/03/15  WHF  Only display last two segments of channel name.
	2004/10/14  WHF  Use javax.imageio instead of JAI.
	2008/08/29  WHF  Integrated into the WebTurbine.
	2008/10/03  WHF  Improved DataTurbine connection management.
	2008/10/31  WHF  Check registration for units.
*/


package com.creare.chart;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.rbnb.sapi.*;

/**
  * A servlet which produces PNGs of plots of RBNB numerical data.
  *
  * @since V3.2B2
  */
public class ChartServlet extends HttpServlet
{
	public ChartServlet()
	{
		Thread t = new Thread(new NodeFreshnessEnforcer());
		t.setDaemon(true);
		t.start();
	}		
	
	protected void doGet(
		HttpServletRequest request, 
		HttpServletResponse response) 
			throws ServletException, java.io.IOException
	{
		response.setHeader("Expires","0");
		response.setHeader("Pragma","no-cache");
		
		double 
			start=getParameterDouble(request, "time", 0.0),
			duration=getParameterDouble(request, "duration", 0.0);
		
		String reference = getParameterString(request, "reference", "newest");
			
		WorkNode work=null;
		
		try {
			work=getWorkNode();
			work.width=getParameterInt(request, "width", 320);
			work.height=getParameterInt(request, "height", 240);
			work.channel = java.net.URLDecoder.decode(
					getParameterString(request, "channel", "rbnbSource/c0"),
					"utf-8"
			);
	
			work.map.Clear();
			work.map.Add(work.channel);
			// 2008/10/31  WHF  RequestRegistration to find unit data.
			work.sink.RequestRegistration(work.map);
			work.sink.Fetch(-1, work.map);
			if (work.map.NumberOfChannels() > 0) 
				work.userInfo = work.map.GetUserInfo(0);
			else work.userInfo = null;
			work.sink.Request(work.map, start, duration, reference);
			work.sink.Fetch(-1, work.map);
		} catch (SAPIException sapiE) {
			// In this case, do not recycle the node:
			throw new ServletException(sapiE);
		}
		
		if (work.map.NumberOfChannels() == 0) {
			response.setContentType("text/html");
			java.io.Writer w=response.getWriter();
			w.write("<HTML><BODY><p>No data matches the specified request"
				+" criteria.</p></BODY></HTML>");
			w.close();
		} else {
			String mime = work.map.GetMime(0);
			if (mime!=null && (mime.startsWith("image/") 
					|| mime.startsWith("text/"))) {
				// Data is an object of some kind.  Forward.
				response.setContentType(mime);
				byte[] bytes = work.map.GetData(0);
				response.setContentLength(bytes.length);
				response.getOutputStream().write(bytes);
			} else  // might be time-series:
				processData(work, response);
		}
				
		recycleWorkNode(work);
	}
	
	private void processData(WorkNode work, HttpServletResponse response)
			throws java.io.IOException
	{
		double[] time=work.map.GetTimes(0);
		for (int ii=0; ii<time.length; ++ii)
			time[ii]*=1e3;  // convert back to ms since 1970.
		Object out;
		
		switch (work.map.GetType(0))
		{
			case ChannelMap.TYPE_FLOAT64:
				out=work.map.GetDataAsFloat64(0);
				break;

			case ChannelMap.TYPE_FLOAT32:
				out=work.map.GetDataAsFloat32(0);
				break;

			case ChannelMap.TYPE_INT32:
				out=work.map.GetDataAsInt32(0);
				break;

			case ChannelMap.TYPE_INT16:
				out=work.map.GetDataAsInt16(0);
				break;
				
			case ChannelMap.TYPE_INT8:
				out=work.map.GetDataAsInt8(0);
				break;
				
			case ChannelMap.TYPE_INT64:
				out=work.map.GetDataAsInt64(0);
				break;				

			default:
				throw new ClassCastException(
					"Unsupported type.");
		}

		int lastSlash = work.channel.lastIndexOf('/'),
				penultimateSlash = work.channel.lastIndexOf('/', lastSlash-1);
		String theTitle = (penultimateSlash < 0 
				? work.channel : work.channel.substring(penultimateSlash+1));
		work.chart.setTitle(theTitle);
		
		// 2008/10/31  WHF  Unit support.  Set Range Label initially to empty,
		//  then override if units found.
		((com.jrefinery.chart.plot.XYPlot) work.chart.getPlot())
				.getRangeAxis().setLabel("");
		if (work.userInfo != null) {
			java.util.StringTokenizer st = new java.util.StringTokenizer(
					work.userInfo,
					","
			);
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				if (s.startsWith("units=")) {
					String u = s.substring(s.indexOf('=')+1);
					((com.jrefinery.chart.plot.XYPlot) work.chart.getPlot())
							.getRangeAxis().setLabel(u);
				}
			}
		}
		
		work.dataSet.setValues(time, out);
		
		BufferedImage img=work.chart.createBufferedImage(
				work.width,
				work.height);
		work.baos.reset();						
			
		// Perform the encode operation
		javax.imageio.ImageIO.write(
				(java.awt.image.RenderedImage) img,
				"png",
				work.baos
		);

		byte[] bytes = work.baos.toByteArray();
		
		response.setContentLength(bytes.length);
		response.setContentType("image/png");
		response.getOutputStream().write(bytes);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException
	{ doGet(req, resp); }
	
// ************************* Private Utilities ******************************//
	/**
	  * Reuses an existing work node, or creates a new one.
	  */
	private WorkNode getWorkNode() throws SAPIException
	{
		WorkNode wn;
		boolean needConnect=true;

		if (workNodeStack.empty())
			wn=new WorkNode();
		else
		{
			wn=(WorkNode) workNodeStack.pop();
			try {
				wn.sink.GetClientName();
				needConnect=false;
			} catch (IllegalStateException ise)
			{  }
		}
		if (needConnect)
		{
			// If this exception occurs, we are not connected to a server.
			wn.sink.OpenRBNBConnection("localhost:3333", "ChartServletSink");
		}
		return wn;
	}
	
	/**
	  * Return a used node to the stack.
	  */
	private void recycleWorkNode(WorkNode node)
	{
		node.lastRecycleTime = System.currentTimeMillis();
		workNodeStack.push(node);
	}

	private double getParameterDouble(
		HttpServletRequest request, 
		String paramName, 
		double _default)
	{
		String s=request.getParameter(paramName);
		if (s==null) return _default;
		try { _default=Double.parseDouble(s); }
		catch (NumberFormatException nfe) { }
		return _default;
	}
	
	private int getParameterInt(
		HttpServletRequest request, 
		String paramName, 
		int _default)
	{
		String s=request.getParameter(paramName);
		if (s==null) return _default;
		try { _default=Integer.parseInt(s); }
		catch (NumberFormatException nfe) { }
		return _default;
	}

	private String getParameterString(
		HttpServletRequest request, 
		String paramName, 
		String _default)
	{
		String s=request.getParameter(paramName);
		if (s==null) return _default;
		return s;
	}	
	
	private static class WorkNode {
		public WorkNode() { }

		public final ByteArrayOutputStream baos=new ByteArrayOutputStream();
		public double[] data;
		public final ArrayXYDataSet dataSet=new ArrayXYDataSet(); 
		public final com.jrefinery.chart.JFreeChart chart=
			com.jrefinery.chart.ChartFactory.// createLineXYChart(
												createTimeSeriesChart(
				null, null, null,
				dataSet, false, false, false);
		public final ChannelMap map=new ChannelMap();
		public final Sink sink=new Sink();
		// Set on request:
		public String channel, userInfo;
		public int width;
		public int height;
		public long lastRecycleTime;
	}
	
	private class NodeFreshnessEnforcer implements Runnable
	{
		public void run() {
			final Stack closeStack = new Stack();
			
			try {
				while (true) {
					if (workNodeStack.size() > MAX_RETAINED_CONNECTIONS) {
						long now = System.currentTimeMillis();
						// Lock the stack for a moment:
						synchronized (workNodeStack) {
							// Iterate over nodes, removing those that are old:
							for (Iterator iter = workNodeStack.iterator(); 
									iter.hasNext(); ) {
								WorkNode node = (WorkNode) iter.next();
								if (now - node.lastRecycleTime 
										> MAX_CONNECTION_AGE) {
									iter.remove();
									closeStack.push(node);
								}
							}
						}
						
						// Now close all marked nodes:
						while (!closeStack.empty()) {
							WorkNode node = (WorkNode) closeStack.pop();
							node.sink.CloseRBNBConnection();
						}					
					}
					Thread.sleep(MAX_CONNECTION_AGE);
				}
			} catch (InterruptedException ie) {}
		}
	}
	
	private final Stack workNodeStack=new Stack();
	private static final int MAX_RETAINED_CONNECTIONS = 5;
	private static final long MAX_CONNECTION_AGE = 10000; // in ms
	
	private static class ArrayXYDataSet 
			extends com.jrefinery.data.AbstractSeriesDataset 
			implements com.jrefinery.data.XYDataset {
		public ArrayXYDataSet() { }
		
		public void setXValues(Object x) 
		{ this.x=x; fireDatasetChanged(); }
		public void setYValues(Object y) 
		{ this.y=y; fireDatasetChanged(); }
		public void setValues(Object x, Object y) 
		{
			this.x=x;
			this.y=y;
			fireDatasetChanged();
		}
		public void setSeriesName(String s) { name=s; }
		
// *************************** XYDataset Methods ****************************//
		public int getItemCount(int series)
		{
			if (x==null || y==null) return 0;
			assert(Array.getLength(x)==Array.getLength(y));
			return Array.getLength(x);
		}
		
		public Number getXValue(int series, int index)
		{
			nx.setDouble(Array.getDouble(x,index));
			return nx;
		}
		
		public Number getYValue(int series, int index)
		{
			ny.setDouble(Array.getDouble(y,index));
			return ny;
		}
		
// ************************* SeriesDataset Methods **************************//
		public int getSeriesCount() { return 1; }
		public String getSeriesName(int series) { return name; }

		private Object x, y;
		private String name="some series name";
		private final ReusableNumber 
			nx=new ReusableNumber(),
			ny=new ReusableNumber();
	}
}

/**
  * To improve efficiency of the DataSet interfaces, which call for a 
  *  Number return object.
  */
class ReusableNumber extends Number
{
	public ReusableNumber() { } 

	void setDouble(double val) { this.val=val; }	
	
// ************************* Number Abstract Methods ************************//

	public double doubleValue() { return val; }
	public float floatValue() { return (float) val; }
	public int intValue() { return (int) val; }	
	public long longValue() { return (long) val; }
	
	private double val;
}


