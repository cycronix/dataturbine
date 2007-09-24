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
	Derivative.java
	
	An RBNB PlugIn that calculates the time derivative of one or more channels.
	
	2007/08/28  WHF  Created.
*/


import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.SAPIException;


import java.lang.reflect.Array;

/**
  * An RBNB PlugIn that calculates the time derivative of one or more channels.
  *  It works well for requests for zero points and for decent durations.  
  *  Performance degrades for small durations (around two sample periods).
  */
public class DerivativePlugIn extends com.rbnb.plugins.PlugInTemplate
{
	public DerivativePlugIn()
	{
		setCutOffRatio(ratio); // sets alpha
	}

//****************************  Accessors  **********************************//	
	public double geCutOffRatio() { return ratio; }
	/**
	  * Sets the ratio between the low-pass filter cut-off and the sample 
	  *  frequency.
	  */
	public void setCutOffRatio(double ratio)
	{
		this.ratio = ratio;
		alpha = Math.exp(-ratio * 2 * Math.PI);
	}

	public double getPreFetchFactor() { return preFetchFactor; }
	/**
	  * Sets the factor to multiply the (non-zero) incoming duration by to 
	  *  ensure enough data is available to have a clean derivative.
	  */
	public void setPreFetchFactor(double preFetchFactor) 
	{ this.preFetchFactor = preFetchFactor; }

	public double getZeroOverride() { return zeroOverride; }
	/**
	  * Sets the duration to use when zero is requested or pre-fetch fails.
	  */
	public void setZeroOverride(double zeroOverride)
	{ this.zeroOverride = zeroOverride; }
	
	
//************************  Template Overrides  *****************************//
	/**
	  * Adds to the request duration to obtain a clean derivative.
	  */
	protected ChannelMap getForwardData(
			Sink sink,
			ChannelMap toFwd,
			PlugInChannelMap picm) throws SAPIException
	{
		double duration = picm.GetRequestDuration();
		ChannelMap response = null;
		
		for (int ntries = 0; ntries < 2; ++ntries) {
			if (duration == 0) duration = zeroOverride;
			else duration *= preFetchFactor;
			
			sink.Request(
					toFwd,
					picm.GetRequestStart(),
					duration,
					picm.GetRequestReference()
			); 
			response = sink.Fetch(getTimeout());
			// Check the response to see if we got a decent result on all
			//  channels:
			boolean ok = true;
			for (int ii = 0; ii < response.NumberOfChannels(); ++ii) {
				if (response.GetTimeDuration(ii) == 0 
						|| response.GetTimes(ii).length < MIN_SIZE) {
					ok = false;
					if (duration < zeroOverride) duration = zeroOverride;
					break;
				}
			}
			if (ok) break;
		}
		
		return response;
	}
	
	/**
	  * Takes the derivative of the forwarded data, returns the time region
	  *  without the pre-fetch.
	  */
	protected void processRequest(ChannelMap fwdData, PlugInChannelMap out)
		throws SAPIException
	{
		for (int index = 0; index < fwdData.NumberOfChannels(); ++index) {
			Object data;
						
			switch (fwdData.GetType(index)) {
				case ChannelMap.TYPE_FLOAT32:
				data = fwdData.GetDataAsFloat32(index);
				break;
				
				case ChannelMap.TYPE_FLOAT64:
				data = fwdData.GetDataAsFloat64(index);
				break;
				
				case ChannelMap.TYPE_INT16:
				data = fwdData.GetDataAsInt16(index);
				break;
	
				case ChannelMap.TYPE_INT32:
				data = fwdData.GetDataAsInt32(index);
				break;
	
				case ChannelMap.TYPE_INT64:
				data = fwdData.GetDataAsInt64(index);
				break;
	
				case ChannelMap.TYPE_INT8:
				data = fwdData.GetDataAsInt8(index);
				break;
	
				default:
				System.err.println("ResamplePlugIn: Unsupported datatype.");
				continue;
			}
								
			int npts = Array.getLength(data);
			if (npts < MIN_SIZE) { // no derivative possible, skip
				continue;
			}
			
			// Will add if necessary, otherwise just a lookup:
			int outIndex = out.Add(fwdData.GetName(index));
			
			// Perform derivative.  This bit assumes 'newest'.
			//       |<--------------------->|  original PI request
			//   |<------------------------->|  extended request
			// We want the absolute start of the original request.
			double duration = out.GetRequestDuration(), 
				start = fwdData.GetTimeStart(index) 
					+ fwdData.GetTimeDuration(index) - duration;
				 
			double[] result = derivative(
					fwdData.GetTimeDuration(index) / npts,
					fwdData.GetTimes(index),
					data,
					start,
					start + duration
			);
			
			out.PutTime(start, duration);
			out.PutDataAsFloat64(outIndex, result);
		} // end for
	}
	
//******************************  Static Methods  ***************************//
	/**
	  * Starts a new instance.
	  */
	public static void main(String[] args) throws SAPIException
	{
		DerivativePlugIn di = new DerivativePlugIn();
		
		// Parse Command-line:
		try {
			for (int ii = 0; ii < args.length; ++ii) {
				// General template properties:
				if ("-a".equals(args[ii])) di.setHost(args[++ii]);
				else if ("-n".equals(args[ii])) di.setName(args[++ii]);
				
				// Derivative specific:
				else if ("-h".equals(args[ii]) || "-?".equals(args[ii])) {
					di.showHelp();
					return;
				} else if ("-r".equals(args[ii]))
					di.setCutOffRatio(Double.parseDouble(args[++ii]));
				else if ("p".equals(args[ii]))
					di.setPreFetchFactor(Double.parseDouble(args[++ii]));
				else if ("z".equals(args[ii]))
					di.setZeroOverride(Double.parseDouble(args[++ii]));				
			}
		} catch (Exception e) {
			System.err.println("Error with command line arguments.");
			e.printStackTrace();
			di.showHelp();
			return;
		}
		
		// Start the PlugIn:
		di.start();
	}
	
	
//****************************  Private Methods  ****************************//
	/**
	  * Calculates the derivative for the matching time range.
	  */
	private double[] derivative(double T, double[] times, Object data, double start, double end)
	{
		// First order low pass filtered digital differentiator.
		
		// Find indices of time range of interest:
		int startI;
		for (startI = 1; startI < times.length - 1; ++startI)
			if (times[startI] >= start) { --startI; break; }
		
		int endI;
		for (endI = times.length - 1; endI > startI; --endI)
			if (times[endI] < end) break;
		
		double[] result = new double[endI - startI + 1];
		
		double gain = (1.0 - alpha) / T;
		double y = 0.0, xp = Array.getDouble(data, 0);
		// Warm up derivative:
		for (int ii = 0; ii < startI; ++ii) {
			double xi = Array.getDouble(data, ii);
			y = alpha * y + gain * (xi - xp);
			// result[ii] = y; don't store
			xp = xi;
		}
		for (int ii = 0; ii < result.length; ++ii) {
			double xi = Array.getDouble(data, startI + ii);
			y = alpha * y + gain * (xi - xp);
			result[ii] = y;
			xp = xi;
		}
		
		return result;
	}
		
	private void showHelp()
	{
		System.err.println("DerivativePlugIn [options]\n"
				+"\t-a address:port         RBNB address (localhost:3333)\n"
				+"\t-n name                 Client name (ResamplePlugIn)\n"
				+"\t-r cut-off freq ratio   cut-off / sample freq ("
						+ratio+")\n"
				+"\t-p pre-fetch factor     duration multiplier to remove\n"
				+"\t                        startup transients ("
						+preFetchFactor+")\n"
				+"\t-z zero override        duration to use when zero is"
						+" requested ("+zeroOverride+")\n"
		);
	}
//****************************  Data Members  *******************************//
	/**
	  * Ratio between the low-pass filter cut-off and the sample frequency.
	  */
	private double ratio = 0.1;
	
	/**
	  * Factor to multiply the (non-zero) incoming duration by to ensure
	  *  enough data is available to have a clean derivative.
	  */
	private double preFetchFactor = 1.1;
	
	/**
	  * Duration to use when zero is requested or pre-fetch fails.
	  */
	private double zeroOverride = 1.0;

	/**
	  * alpha = exp(-aHz * 2*pi / fs)
	  * We assume a pole at a frequency one tenth of the sampling frequency,
	  *  so for an fs of 1kHz the pole would be at 100 Hertz.
	  */ 
	private double alpha; // set in setCutOffRatio

	private static final int MIN_SIZE = 2;
}

