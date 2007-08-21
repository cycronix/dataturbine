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

/*
	ThumbNailPlugIn - convert JPEG to smaller thumbnail image
	
	Copyright 2006 Creare Incorporated
		
	2004/05/??  MJM  Created.
	2005/01/05  JPW  Add "-n" option to allow user to set PlugIn name
	2006/11/09  WHF  Ported to PlugInTemplate paradigm.
	2006/11/17  WHF  Verified that for the optional command line argument
		-c [channel], it is silently rejected if the data argument begins
		with a '-'.  Added back the 'throttling' feature, which was accidentally
		omitted during porting.
*/

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.SAPIException;
import com.rbnb.plugins.PlugInTemplate;
import java.awt.*;
import java.io.*;
import java.awt.image.*;
//import javax.imageio.ImageIO;
import com.sun.image.codec.jpeg.*;

public class ThumbNailPlugIn extends PlugInTemplate {
	private double scale = 0.5;                           // image scale factor (0-4)
	private float quality = (float).50;                   // image quality (0-1)
	private String imagechan=null;                        // channel name
	private int maxImage=10;                              // max images for multi-image request
	
	public ThumbNailPlugIn(String[] args) {
		
		// Set settings of the super-class:
		// Default name.  May be overridden by -n:
		setName("ThumbNail");
		// We will forward requests ourselves.
		setForwardRequestData(false);
		
		for (int i=0;i<args.length;i++) {
			if (args[i].startsWith("-a")) {
				String address;
				if (args[i].length()==2) address=args[++i];
				else address=args[i].substring(2);
				setHost(address);
			}
			if (args[i].startsWith("-c")) {
				String source;
				if (args[i].length()==2) {
					source=args[i+1];
					if (source.charAt(0) == '-') continue;
					else ++i;
				} else source=args[i].substring(2);
				addChannelToRegister(source);
			}
			if (args[i].startsWith("-s")) {
				if (args[i].length()==2) scale=Double.parseDouble(args[++i]);
				else scale=Double.parseDouble(args[i].substring(2));
			}
			if (args[i].startsWith("-q")) {
				if (args[i].length()==2) quality=Float.parseFloat(args[++i]);
				else quality=Float.parseFloat(args[i].substring(2));
			}
			if (args[i].startsWith("-m")) {
				if (args[i].length()==2) maxImage=Integer.parseInt(args[++i]);
				else maxImage=Integer.parseInt(args[i].substring(2));
			}
			// JPW 01/05/2005: Add "-n" option to let user specify the PlugIn name
			if (args[i].startsWith("-n")) {
				String pluginName;
				if (args[i].length()==2) pluginName=args[++i];
				else pluginName=args[i].substring(2);
				setName(pluginName);
			}
		}
		/*
		System.err.println("After argument parsing:\n" +
		"address = " + getHost() + "\n" +
		"scale = " + scale + "\n" +
		"quality = " + quality + "\n" +
		"maxImage = " + maxImage);
		*/
	}
	
	public static void main(String[] args) {
		(new ThumbNailPlugIn(args)).exec();
	}
	
	public byte[] thumbnailer(byte[] jpeg, double scale, float quality) {
		
		// noop check
		if((scale == 1.) && (quality == 1.)) return(jpeg);
		
		// create image from input bytearray
		Image image = Toolkit.getDefaultToolkit().createImage(jpeg);
		try {       // wait for image to be realized
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(image, 0);
			mediaTracker.waitForID(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// determine thumbnail size from WIDTH and HEIGHT
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int thumbWidth  = (int)((double)imageWidth * scale);
		int thumbHeight = (int)((double)imageHeight * scale);
		
		// draw original image to thumbnail image object and
		// scale it to the new size on-the-fly
		BufferedImage thumbImage = new BufferedImage(thumbWidth, 
		thumbHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
		// save thumbnail image to OutputStream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//    ImageIO.write(image, "jpg", out);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.
		getDefaultJPEGEncodeParam(thumbImage);
		param.setQuality((float)quality, false);
		encoder.setJPEGEncodeParam(param);
		try {
			encoder.encode(thumbImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return(out.toByteArray());
	}
	
	/**
	  * Execute this PlugIn.  Calls {@link PlugInTemplate#start()}, then blocks.
	  *  If interrupted, calls {@link PlugInTemplate#stop()}.
	  */
	public void exec() {
		try {
			start();
			// To keep behavior of original exec method, which blocked, 
			//  we will block, although the plugin is now running in another thread.
			try { while (true) Thread.sleep(10000); } catch (Throwable t) {}
			stop();
		} catch (SAPIException sapie) { 
			System.err.println("Error starting ThumbNailPlugIn:");
			sapie.printStackTrace();
		}
	}
	
	protected void processRequest(ChannelMap cmI, PlugInChannelMap picm)
		throws SAPIException
	{
		// The input ChannelMap will be empty, because we have bypassed the
		//  default forwarding.
		cmI = createForwardMap(picm);
		Sink sink = getRequestSink();
		double start = picm.GetRequestStart();
		double duration = picm.GetRequestDuration();
		double end = start + duration;
		double interval = 1.;  // for now presume 1 sec interval
		double increment = duration / maxImage;
		String reference = picm.GetRequestReference();
				
		// convert relative into absolute request
		if(duration > 0.) {
			if(reference.equals("newest")) {
				ChannelMap cmg;
				
				// would be more efficient to use Registration for time limits,
				//  but can't depend on it
				sink.Request(cmI, 0., 0., "newest");  
				cmg = sink.Fetch(getTimeout());
				start = cmg.GetTimeStart(0) - duration;
				end = cmg.GetTimeStart(0);
				reference = "absolute";
			} else if(reference.equals("oldest")) {
				ChannelMap cmg;
				
				sink.Request(cmI,picm.GetRequestStart(), 0., "oldest");
				cmg = sink.Fetch(getTimeout());
				start = cmg.GetTimeStart(0);
System.err.println("OLD Start: "+cmg.GetTimeStart(0)+", start: "+start);
				end = start + duration;
				reference = "absolute";
			}      
		}
				
		// Align times to repeatable "top-of" intervals
		//System.err.println("Start: "+picm.GetRequestStart());
		if((start>0.) && (increment>0.)) 
			start = Math.floor(start/increment) * increment;
		//System.err.println("Reference: "+picm.GetRequestReference()+", duration: "+duration);
		//System.err.println("start: "+start+", end: "+end+", increment: "+increment);
		if(increment < interval) increment = interval;
				
		for(double t=start; true; t+=increment) {
			ChannelMap cmg;

			if(maxImage <= 1) t = end;  // mjm 1/5/06: if only to get one, get most recent
					
			if((t>=end) && picm.GetRequestReference().equals("newest")) {
				sink.Request(cmI, 0., 0., "newest");
			} else
				sink.Request(cmI, t, 0., reference);    // request source data	      
			
			cmg = sink.Fetch(getTimeout());
			
			for (int ii = 0; ii < cmg.NumberOfChannels(); ++ii) {
				int picmIndex = picm.GetIndex(cmg.GetName(ii));
			
				if (picmIndex == -1) continue;

				// 2006/11/17  WHF  Note that the comments below were for
				//  a request with duration.  The incremental requests made
				//  above no longer have that concern, but we keep the
				//  implementation because it works in both situations.
				
				// 2006/11/09  WHF  This causes problems if more than one image 
				//  is being processed.  The time reference has multiple points
				//  but if we try to put data one point at a time, it barfs.
				// Copy the time reference of the original data:
				// picm.PutTimeRef(cm, ii);
	
				// This doesn't work either.
				//picm.PutTimes(cm.GetTimes(ii));
				
				double[] times = cmg.GetTimes(ii);
				byte[][] data = cmg.GetDataAsByteArray(ii);
				for (int iii = 0; iii < data.length; ++iii) {
					picm.PutTime(times[iii], 0.0);
					picm.PutDataAsByteArray(
							picmIndex, 
							thumbnailer(data[iii], scale, quality)
					);
				}
						
				picm.PutMime(picmIndex, "image/jpeg");
			}
			if(t>=end) break;			
		}
	}
	
	protected void processRegistrationRequest(
			ChannelMap cm, PlugInChannelMap picm)
		throws SAPIException
	{
		// Since we shorted out forwarding, we need to do forwarding now:
		ChannelMap fwdReg = super.getForwardData(
				getRequestSink(),
				createForwardMap(picm),
				picm
		);
		
		super.processRegistrationRequest(fwdReg, picm);
	}
	
}//end ThumbNailPlugIn class

