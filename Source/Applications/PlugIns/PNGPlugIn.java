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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Stack;

import com.rbnb.plot.Channel;
import com.rbnb.plot.DataTimeStamps;
import com.rbnb.plot.Environment;
import com.rbnb.plot.LayoutCubby;
import com.rbnb.plot.PlotContainer;
import com.rbnb.plot.PosDurCubby;
import com.rbnb.plot.RegChannel;
import com.rbnb.plot.Time;

import com.rbnb.sapi.*;

import com.rbnb.utility.ArgHandler;

/**
* SAPI PlugIn to convert RBNB channel data to a PNG format. 
* <p>
* @author Bill Finger
* @since V2.0
* @version 2006/11/13
*/

/*
* Copyright 2004 Creare Inc.
* All Rights Reserved.
*
*   Date      By   Description
* MM/DD/YYYY
* ----------  --   -----------
* 02/11/2002  WHF  Created.
* 02/22/2002  WHF	Handles requests for "...", which return EndOfStream.
* 04/16/2002  WHF  Handles requests for registration
* 05/16/2002  WHF  Passes through requests for registration that are not "...".
* 05/22/2002  WHF  Removed references to deprecated items.
* 11/14/2002  WHF  Returns data as a ByteArray instead of Int8.
* 11/15/2002  WHF  Fixed some bugs in synchronization.
* 01/22/2003  WHF  Incorporated server-side meta-data kluge here.
* 2004/08/11  WHF  Switched from JAI to javax.imageio, which comes with JVM 1.4
* 2006/10/19  EMF  Modified PNG drawing to eliminate dead space from Frame borders.
* 2006/10/19  EMF  Pulled Sink connection out of individual threads, so now shared
*                  but saves lots of connect calls which are slow.
* 2006/11/13  WHF  Updated to template architecture.
*/

public class PNGPlugIn extends com.rbnb.plugins.PlugInTemplate
{
	public static void main(String[] args) throws SAPIException
	{	
		ArgHandler ah=null;
		try {
			ah=new ArgHandler(args);
		} catch (Exception e) { showUsage(); }

		if (ah.getOption('?')!=null)
			showUsage();
		
		PNGPlugIn pi=new PNGPlugIn();
		pi.setHostName(ah.getOption('a',"localhost:3333"));
		pi.setClientName(ah.getOption('n',"PNGPlugIn"));
		pi.setWidth(Integer.parseInt(ah.getOption('w',"320")));
		pi.setHeight(Integer.parseInt(ah.getOption('h',"200")));
		
		pi.run();
	}
	
	private static void showUsage()
	{
		System.err.println(PNGPlugIn.class.getName()
		+": Converts RBNB channels to PNG (Portable Network"
		+" Graphics) files.\nCopyright Creare, Inc. 2004"
		+"\nOptions:"
		+"\n\t-a host:port [localhost:3333]\t- RBNB server"
		+" to connect to"
		+"\n\t-n name [PNGPlugIn]\t- client name for plugin"
		+"\n\t-w width [320]\t- Width of produced image, pixels"
		+"\n\t-h height [200]\t- Height of image, pixels");
		System.exit(1);
	}
	
	/**
	* Constructor.
	*/
	public PNGPlugIn()
	{
		setUserRequestClass(PNGAnswerRequest.class);
	}
	
	/**
	* Sets the host name to connect to.
	* @deprecated Use {@link #setHost(String)} instead.
	*/
	public void setHostName(String host) { setHost(host); }
	/** 
	* Sets the name of the RBNB PlugIn client.
	* @deprecated Use {@link #setName(String)} instead.
	*/
	public void setClientName(String name) { setName(name); }
	public void setWidth(int width) { this.width=width; }
	public void setHeight(int height) { this.height=height; }
	
	/**
	* Starts plugin thread, then blocks.
	*/
	public void run() throws SAPIException
	{		
		start();
		
		try {
			synchronized (this) { wait(); }
		} catch (InterruptedException ie) { }	    
	}
	
	//***************************  Protected Methods  ***************************//
	/*  Inherited from com.rbnb.plugins.PlugInTemplate.  */
	protected void processRequest(
			ChannelMap fwdData,
			PlugInChannelMap out) throws SAPIException
	{
		((PNGAnswerRequest) getUserRequestObject()).handleRequest(fwdData, out);
	}
	
	protected void processRegistrationRequest(
			ChannelMap fwdReg,
			PlugInChannelMap out	
	) throws SAPIException
	{
		out.Clear();
		for (int ii = 0; ii < fwdReg.NumberOfChannels(); ++ii) {
			int index = out.Add(fwdReg.GetName(ii));
			out.PutTimeRef(fwdReg, ii);
			
			if ("text/xml".equals(fwdReg.GetMime(ii))) {
				// probably server meta-data, override
				String result=
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
				+"<!DOCTYPE rbnb>\n"
				+"<rbnb>\n"
				+"\t\t<size>"+1+"</size>\n"
				+"\t\t<mime>image/png</mime>\n"
				+"</rbnb>\n";
				out.PutDataAsString(index, result);
				out.PutMime(index, "text/xml");
			}
			else {
				out.PutDataRef(index, fwdReg, ii);
				out.PutMime(index, fwdReg.GetMime(ii));
			}
		}		
	}
	
	////////////////// Private methods/classes ///////////////////////////
	public static class PNGAnswerRequest
	{
		public PNGAnswerRequest(com.rbnb.plugins.PlugInTemplate pit) 
		{		
			pi = (PNGPlugIn) pit;
			
			// Neither RegChannel nor PDC have
			//  any lasting effect on the component:
			PosDurCubby posdur = new PosDurCubby();
			posdur.setTimeFormat(Time.RelativeSeconds);
			plotter = new PlotContainer(
					new RegChannel(""),
					LayoutCubby.PlotMode,
					posdur,
					env=new Environment(),
					true                //EMF 10/12/06: true means show title
			);	
			
			// 2006/11/13  WHF  Insets lazily initialized.
			synchronized (pi) {
				if (pi.insets == null) {
					// EMF 10/13/06: get frame insets so graphics context
					// is appropriately sized
					frame.setBounds(-1000,-1000,320,200);
					frame.setVisible(true);
					pi.insets = frame.getInsets();
					frame.setVisible(false);
				}
			}
					
			frame.setBounds(
					-1000,
					-1000,
					pi.width+pi.insets.left+pi.insets.right,
					pi.height+pi.insets.top+pi.insets.bottom
			);
			frame.add(plotter);			
		}
		
		private void handleRequest(
				ChannelMap sinkMap,
				PlugInChannelMap requestMap) throws SAPIException
		{
			// 2006/11/13  WHF  Remove settings from previous request, if any:
			configHash.clear();
			
			//EMF 10/12/06: pull out message
			configHash.put("scaling","auto_inc_dec");
			String[] message=null;
			//System.err.println("requestMap.GetType(0) "+requestMap.GetType(0));
			if (requestMap.GetType(0)==ChannelMap.TYPE_STRING) {
				message = requestMap.GetDataAsString(0);
			} else if (requestMap.GetType(0)==ChannelMap.TYPE_INT8) {
				message=new String[1];
				message[0]=new String(requestMap.GetDataAsInt8(0));
			}
			//System.err.println("requestMap(0) contained message "+message[0]);
			if (message!=null && message[0].trim().length()>0) {
				message[0]=message[0].trim();
				char[] term = {'&'};
				com.rbnb.utility.KeyValueHash kvh=new com.rbnb.utility.KeyValueHash(message[0],term);
				String div=kvh.get("png_div");
				if (div==null) div=new String("8");
				String min=kvh.get("png_min");
				String max=kvh.get("png_max");
				//System.err.println("div="+div+", min="+min+", max="+max);
				if (min!=null && max!=null) {
					configHash.put("scaling","manual");
					configHash.put("divisions",div);
					configHash.put("min",min);
					configHash.put("max",max);
				}
			}

			for (int ii=0; ii<sinkMap.NumberOfChannels(); ++ii) {
				double plotduration;
				double plotstart;
				
				int rIndex=requestMap.GetIndex(sinkMap.GetName(ii));					
				
				if (rIndex==-1) // channel does not exist 
					// in request, add it
					// 2006/11/13  WHF  I think this was a registration 
					//   request holdover.  I don't think this is normally
					//   possible.
					rIndex=requestMap.Add(sinkMap.GetName(ii));
				
				byte[] out=null;
				String mimeStr="image/png";
				int type=ChannelMap.TYPE_BYTEARRAY;
				
				// Convert to PNG:
				try {
					fillChannel(sinkMap,ii,chan);
					//EMF 10/16/06: following lines instead of fillChannel
					//              will create png with no data
					//chan.clear();
					//chan.channelName=sinkMap.GetName(ii);
					plotduration = requestMap.GetRequestDuration();
					double[] times=sinkMap.GetTimes(ii);
					//EMF 10/17/06: appropriate position data within plot
					if (times.length>0 && requestMap.GetRequestReference().equalsIgnoreCase("newest")) {
						plotstart=times[times.length-1] - plotduration;
					} else if (requestMap.GetRequestReference().equalsIgnoreCase("oldest")) {
						plotstart=times[0];
					} else {
						plotstart=requestMap.GetRequestStart();
					}
											
					out=convertToPNG(
							sinkMap.GetName(ii),
							times,
							plotstart,
							plotduration,
							sinkMap.GetType(ii)
					);
				} catch (IllegalArgumentException iae)
				{
					iae.printStackTrace();
					// most likely the PNG converter
					//  couldn't convert with the current
					// display settings.
					out=("<HTML><TITLE>PNGPlugIn Error"
					+"</TITLE><BODY><h1>PNGPlugIn Error"
					+"</h1><p>The data provided was " 
					+"plotted but could not be converted " 
					+"to a PNG.&nbsp; Be sure the " 
					+"webserver "
					+"machine has at least 32 bit color "
					+"on its display."
					+"</BODY></HTML>\n").getBytes();
					mimeStr="text/html";
					type=ChannelMap.TYPE_STRING;
				}					 
				catch (ClassCastException iae)
				{ 
					iae.printStackTrace();
					// we have an unsupported type.  Return 
					//  an HTML document to that effect.
					out=("<HTML><TITLE>PNGPlugIn Error"
					+"</TITLE><BODY><h1>PNGPlugIn Error"
					+"</h1><p>The data type \""
					+sinkMap.TypeName(sinkMap.GetType(ii))
					+"\", for channel "
					+"<b>"+sinkMap.GetName(ii)+"</b>, "
					+"cannot be converted to a Portable"
					+" Network Graphic.</p>"
					+"</BODY></HTML>\n").getBytes();
					mimeStr="text/html";
					type=ChannelMap.TYPE_STRING;
				}					 
				catch (java.io.IOException ioe)
				{ 
					ioe.printStackTrace();
					// we have an unsupported type.  Return 
					//  an HTML document to that effect.
					out=("<HTML><TITLE>PNGPlugIn Error"
					+"</TITLE><BODY><h1>PNGPlugIn Error"
					+"</h1><p>A problem occurred while generating the image"
					+" for channel <b>"+sinkMap.GetName(ii)+"</b>.</p>"
					+"</BODY></HTML>\n").getBytes();
					mimeStr="text/html";
					type=ChannelMap.TYPE_STRING;
				}
				
				// Set time on map:
				requestMap.PutTime(
						sinkMap.GetTimeStart(ii),
						sinkMap.GetTimeDuration(ii)
				);
				
				// Put PNG into output map:
				requestMap.PutData(
						rIndex,
						out,
						type,
						ChannelMap.MSB
				);
				requestMap.PutMime(rIndex,mimeStr);
			} // end for
		} // end AnswerRequest.handleRequest()
		
		private byte[] convertToPNG(
				String name,
				double[] times,
				double start,
				double duration,
				int type) throws java.io.IOException
		{
			// Set up plotting sub-component:
			env.FOURBYTEASINTEGER=
			((type==ChannelMap.TYPE_INT16||type==ChannelMap.TYPE_INT8)?
			true:false);			
			
			// Set the plot's title:
			configHash.put("name",name);
			
			plotter.setConfig(configHash,"");
			plotter.setAbscissa(new Time(duration)); 
			plotter.setChannelData(chan,new Time(start));  
			
			// Produce Image:
			int w = pi.width, h = pi.height;
			
			frame.setVisible(true);
			java.awt.Image img = plotter.createImage(w,h);
			java.awt.Graphics gi = img.getGraphics();
			plotter.paint(gi);
			frame.setVisible(false);
			baos.reset();						

			// Write the PNG object using ImageIO:
			javax.imageio.ImageIO.write(
					(java.awt.image.RenderedImage) img,
					"png",
					baos
			);
			
			return baos.toByteArray();
		}
		
		///////// AnswerRequest Private Data /////////////
		/**
		  * Reference to the PNGPlugIn in which this is running.
		  * Could not be generated using automatic inner class this
		  *   because constructor is called from another class.
		  */
		private final PNGPlugIn pi;
		
		// Objects in plot package used for preparing graphs:
		private final Hashtable configHash = new Hashtable();
		private final PlotContainer plotter;
		private final Environment env;
		private final Channel chan = new Channel();
		
		// Objects used to turn plot into bytes:
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		private final java.awt.Frame frame = new java.awt.Frame();
		
	} // end inner class AnswerRequest
	
	private static void fillChannel(ChannelMap map, int ii, Channel chan)
	{
		int typecode=map.GetType(ii);
		chan.clear();
		
		chan.channelName=map.GetName(ii);
		
		// As of 02/12/2002, ignored in channel:
		chan.byteOrder=Channel.MSB; 
		
		chan.timeStamp=new DataTimeStamps(map.GetTimes(ii));		
		
		switch (typecode)
		{
			case ChannelMap.TYPE_FLOAT64:
			chan.setDataFloat64(map.GetDataAsFloat64(ii));
			break;
			
			case ChannelMap.TYPE_FLOAT32:
			chan.setDataFloat32(map.GetDataAsFloat32(ii));
			break;
			
			case ChannelMap.TYPE_INT32:
			chan.setDataInt32(map.GetDataAsInt32(ii));
			break;
			
			case ChannelMap.TYPE_INT16:
			chan.setDataInt16(map.GetDataAsInt16(ii));
			break;
			
			case ChannelMap.TYPE_INT8:
			chan.setDataInt8(map.GetDataAsInt8(ii));
			break;
			
			case ChannelMap.TYPE_INT64:
			chan.setDataInt64(map.GetDataAsInt64(ii));
			break;				
			
			default:
			throw new ClassCastException(
			"Unsupported type.");
		}
	}
	
	////////////////// Private data /////////////////////////////
	private int width, height;
	private static java.awt.Insets insets;
}

