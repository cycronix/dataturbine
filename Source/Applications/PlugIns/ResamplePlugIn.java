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
	ResamplePlugIn.java
	
	An RBNB plug-in to adjust the sample rate of a time-series RBNB channel.
	
	---  History  ---
	2007/01/15  WHF  Created.
	2007/04/04  WHF  Added dynamic options.
	2007/08/15  WHF  Changed the meaning of 'maxSamples' so that it is an 
		aggregate across all channels.  Then commented it.
*/

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;


import java.lang.reflect.Array;



/**
  * Adjusts the sample rate of a time-series RBNB channel.
  *  Note that it has no package.  This is to be consistent with other
  *  RBNB plug-ins.
  *
  * <p>Supports the following dynamic options:
  *  minDecimation = integer  Sets the minimum decimation factor.
  *  maxSamples = integer     Sets the maximum number of samples.
  *  antiAlias = boolean      Activates or deactivates the anti-aliasing filter.
  */
public class ResamplePlugIn extends com.rbnb.plugins.PlugInTemplate
{
	public ResamplePlugIn()
	{}
	
//*******************************  Accessors  *******************************//
	/**
	  * Get the maximum number of samples <!--for all channels--> which will be copied
	  *   on any request.
	  *  Larger requests are resampled such that their size is less than this
	  *   value.
	  *  <p>The default is 100.
	  */
	public int getMaxSamples() { return maxSamples; }
	
	/**
	  * Set the maximum number of samples which will be copied on any request.
	  *
	  * @throws IllegalArgumentException  if maxSamples < 1.
	  */
	public void setMaxSamples(int maxSamples)
	{
		if (maxSamples < 1) throw new IllegalArgumentException(
				"Property maxSamples must be greater than one."); 
		this.maxSamples = maxSamples; 
	}
	
	/**
	  * If set, a low pass filter is applied to the data so that frequencies
	  *  above the new Nyquist frequncy of the downsampled data are rolled off.
	  *  The default is false.
	  */
	public boolean getAntiAlias() { return antiAlias; }
	/**
	  * Returns the anti-aliasing state.
	  */
	public void setAntiAlias(boolean antiAlias) { this.antiAlias = antiAlias; }

	/**
	  * Gives the minimum integer divisor for the number of samples in each 
	  *  request.  Any value less than two turns this feature off,
	  *  so decimation will only occur if maxSamples is
	  *  exceeded.
	  *  The default is zero.
	  */
	public int getMinDecimation() { return minDecimation; }
	/**
	  * Sets the minimum decimation factor to apply in all cases.
	  */
	public void setMinDecimation(int minDecimation)
	{ this.minDecimation = minDecimation; }
	
//**********************  PlugInTemplate Overrides  *************************//
	protected void processRequest(ChannelMap fwdData, PlugInChannelMap out)
			throws SAPIException
	{
		if (fwdData.NumberOfChannels() == 0) return;
		
		// Override member defaults with dynamic options: 
		int minDecimation = this.minDecimation, maxSamples = this.maxSamples;
		boolean antiAlias = this.antiAlias;
		
		java.util.Properties opts = getRequestOptions();
		String temp;
		if ((temp = opts.getProperty("minDecimation")) != null)
			minDecimation = Integer.parseInt(temp);
		if ((temp = opts.getProperty("maxSamples")) != null)
			maxSamples = Integer.parseInt(temp);
		if ((temp = opts.getProperty("antiAlias")) != null)
			antiAlias = "true".equals(temp);
		
		// 2007/08/15  WHF  maxSamples applies to all channels:
		// 2007/08/15  WHF  Changed their mind.  Uncomment to re-enable.
		/*if (fwdData.NumberOfChannels() == 0) return;
		maxSamples /= fwdData.NumberOfChannels();
		if (maxSamples < 1) maxSamples = 1; */

		for (int index = 0; index < fwdData.NumberOfChannels(); ++index) {
			Object data;
			
			// Will add if necessary, otherwise just a lookup:
			int outIndex = out.Add(fwdData.GetName(index));
			
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
				return;
			}
	
			int npts = Array.getLength(data);
			
			if (npts <= maxSamples && minDecimation < 2) {
				// length okay, just copy:
				out.PutTimeRef(fwdData, index);
				out.PutDataRef(outIndex, fwdData, index);
				continue;
			} 
	
			// Calculate the decimation factor for this set:
			int ndeci = (int) Math.ceil(((double) npts) / maxSamples);
			if (ndeci < minDecimation) ndeci = minDecimation;
			
			double[] ddata = null;
			if (antiAlias) {
				if (fwdData.GetType(index) == ChannelMap.TYPE_FLOAT64) {
					ddata = (double[]) data;
				} else {
					ddata = new double[npts];
					for (int ii = 0; ii < npts; ++ii)
						ddata[ii] = Array.getDouble(data, ii);
				}
				double[] dataOut = new double[ddata.length];
				Filter lowPass = new Filter(1.0 / ndeci);
				lowPass.filter(ddata, dataOut);
				data = dataOut;
			}		
			
			Object result = decimate(data, ndeci);
			out.PutTime(
					fwdData.GetTimeStart(index),
					fwdData.GetTimeDuration(index)
			);
			if (antiAlias) {
				out.PutDataAsFloat64(outIndex, (double []) result);
			} else {
				switch (fwdData.GetType(index)) {
					case ChannelMap.TYPE_FLOAT32:
					out.PutDataAsFloat32(outIndex, (float []) result);
					break;
					
					case ChannelMap.TYPE_FLOAT64:
					out.PutDataAsFloat64(outIndex, (double []) result);
					break;
					
					case ChannelMap.TYPE_INT16:
					out.PutDataAsInt16(outIndex, (short []) result);
					break;
		
					case ChannelMap.TYPE_INT32:
					out.PutDataAsInt32(outIndex, (int []) result);
					break;
		
					case ChannelMap.TYPE_INT64:
					out.PutDataAsInt64(outIndex, (long []) result);
					break;
		
					case ChannelMap.TYPE_INT8:
					out.PutDataAsInt8(outIndex, (byte []) result);
					break;
				}		
			}
		}
	}
	
//***************************  Private Methods  *****************************//
	/**
	  * Performs integer decimation of the input array.
	  */
	private Object decimate(Object in, int ndeci)
	{
		int oldLength = Array.getLength(in), 
			newLength = oldLength / ndeci + (oldLength % ndeci != 0 ? 1 : 0),
			ii, // index into input
			iii = 0; // index into output
			
		Object out = Array.newInstance(
				in.getClass().getComponentType(),
				newLength
		);
			
		for (ii = 0; ii < oldLength; ii += ndeci) {
			Array.set(out, iii++, Array.get(in, ii)); 
		}
		
		return out;
	}
		
//****************************  Member Data  ********************************//
	private int maxSamples = 100;
	private int minDecimation = 0;
	
	private boolean antiAlias = false;
	
//********************************  Statics  ********************************//
	private static void showHelp(ResamplePlugIn rpi)
	{
		System.err.println("ResamplePlugIn [options]\n"
				+"\t-a address:port        RBNB address (localhost:3333)\n"
				+"\t-n name                Client name (ResamplePlugIn)\n"
				+"\t-s max samples         Max samples per request ("
						+ rpi.maxSamples+")\n"
				+"\t-m min. decimation     Min decimation factor to apply ("
						+ rpi.minDecimation+")\n" 
				+"\t-f true/false          Activates anti-aliasing ("
						+ rpi.antiAlias+")\n"
		);
	}
	
	public static void main(String[] args) throws Exception
	{
		ResamplePlugIn respi = new ResamplePlugIn();
		
		// Parse Command-line:
		try {
			for (int ii = 0; ii < args.length; ++ii) {
				// General template properties:
				if ("-a".equals(args[ii])) respi.setHost(args[++ii]);
				else if ("-n".equals(args[ii])) respi.setName(args[++ii]);
				
				// Resample specific:
				else if ("-h".equals(args[ii]) || "-?".equals(args[ii])) {
					showHelp(respi);
					return;
				} else if ("-s".equals(args[ii])) 
					respi.setMaxSamples(Integer.parseInt(args[++ii]));
				else if ("-f".equals(args[ii]) && "true".equals(args[++ii]))
					respi.setAntiAlias(true);
				else if ("-m".equals(args[ii]))
					respi.setMinDecimation(Integer.parseInt(args[++ii]));
			}
		} catch (Exception e) {
			System.err.println("Error with command line arguments.");
			e.printStackTrace();
			showHelp(respi);
			return;
		}
		
		// Start the PlugIn:
		respi.start();
	}

//***************************  Inner Classes  *******************************//
	/**
	  * An immutable Complex implementation.
	  * Note: this class is not efficient.  For efficiency, see C++.
	  */
	private static class Complex
	{
		public Complex() { real = imag = 0; }
		public Complex(double real, double imag)
		{ this.real = real; this.imag = imag; }
		
		// *** Scalar Operations ***
		public Complex add(double x)
		{ return new Complex(real+x, imag); }
		public Complex mul(double x)
		{ return new Complex(real*x, imag*x); }
		public Complex div(double x)
		{ return new Complex(real/x, imag/x); }
		
		// *** Complex Operations ***
		public Complex add(Complex c)
		{ return new Complex(real+c.real, imag+c.imag); }
		public Complex mul(Complex c)
		{
			return new Complex(
					real * c.real - imag * c.imag,
					real * c.imag + imag * c.real
			);
		}
	
		public Complex div(Complex c)
		{
			double _Yre = c.real, _Yim = c.imag;
			// Find most robust scaling.
			if ((_Yim < 0 ? -_Yim : +_Yim)
			  < (_Yre < 0 ? -_Yre : +_Yre)) {
				// Divide by real first.
				double _Wr = _Yim / _Yre;
				double _Wd = _Yre + _Wr * _Yim;				
				double _W = (real + imag * _Wr) / _Wd;
				
				return new Complex(
						_W,
						(imag - real * _Wr) / _Wd
				);
			}
			// Divide by imag first.
			double _Wr = _Yre / _Yim,
					_Wd = _Yim + _Wr * _Yre,
					_W = (real * _Wr + imag) / _Wd;
			return new Complex(
					_W,
					(imag * _Wr - real) / _Wd
			);
		}
		
		// *** Unary Operations ***
		public Complex neg() { return new Complex(-real, -imag); }
		public double abs() { return Math.sqrt(real*real+imag*imag); }
		public double real() { return real; }
		public double imag() { return imag; }
		
		public String toString() { return ""+real+" + "+imag+"i"; } 
		
		// *** Utility ***
		public static double[] poly(Complex[] e)
		{
			Complex[] c = new Complex[e.length+1]; 

			c[0] = new Complex(1, 0);
			
			for (int j = 0; j < e.length; ++j) {
				c[j+1] = new Complex();
				for (int k = j+1; k >= 1; --k) {
					c[k] = c[k].add(e[j].neg().mul(c[k-1]));

//System.err.println(c[k]+", ");
				}				
//System.err.println();
			}
			
			double[] p = new double[c.length];
			for (int ii = 0; ii < c.length; ++ii)
				p[ii] = c[ii].real;
			
			return p;
		}
		
		
		private final double real, imag;
	} // end class Complex
	
	/**
	  * A low pass filter implementaion.
	  */
	private static class Filter
	{
		/**
		  * Prototype Butterworth 8th order filter.
		  */
		private static final Complex[] protoPoles = {
				new Complex(-0.1950903220,  0.9807852804),
				new Complex(-0.1950903220, -0.9807852804),
				new Complex(-0.5555702330,  0.8314696123),
				new Complex(-0.5555702330, -0.8314696123),
				new Complex(-0.8314696123,  0.5555702330),
				new Complex(-0.8314696123, -0.5555702330),
				new Complex(-0.9807852804,  0.1950903220),
				new Complex(-0.9807852804, -0.1950903220)
		};       
		
		private static final Complex minusOne = new Complex(-1, 0);
		/**
		  * These are already in the Z domain.
		  */
		private static final Complex[] protoZeros = {
				minusOne,
				minusOne,
				minusOne,
				minusOne,
				minusOne,
				minusOne,
				minusOne,
				minusOne
		};
			
		/**
		  * Low pass filter.  Wn is normalized breakpoint frequency,
		  *  where Wn = 1 equals Nyquist.
		  */
		public Filter(double Wn)
		{
//System.err.println("Filter.  Wn = "+Wn+"\nContinuous:");
			final double fs = 2,
				u = 2*fs*Math.tan(Math.PI*Wn/fs);
				
			Complex[] poles = new Complex[protoPoles.length];
			
			double kn = 1, kd = 1;
			for (int ii = 0; ii < poles.length; ++ii) {
				poles[ii] = protoPoles[ii].mul(u);
				kn *= poles[ii].abs();
				kd *= poles[ii].neg().add(2*fs).abs();
//System.err.println("poles["+ii+"] = "+poles[ii]+", kn = "+kn+", kd = "+kd);				
			}
			bilinear(poles);
//System.err.println("Discrete:");
//for (int ii = 0; ii < poles.length; ++ii) System.err.println("poles["+ii+"] = "+poles[ii]);
			den = Complex.poly(poles);
//for (int ii = 0; ii < den.length; ++ii) System.err.println("den["+ii+"] = "+den[ii]);

			double k = kn / kd;
//System.err.println("Gain: "+k);			
			num = Complex.poly(protoZeros);
			for (int ii = 0; ii < num.length; ++ii) {
				num[ii] *= k;
//System.err.println("num["+ii+"] = "+num[ii]);				
			}
		}
		
		/**
		  * Perform filtering.  The vector src and dest should be the same
		  *  length, and cannot be the same vector.
		  */
		public void filter(double[] src, double[] dest)
		{
			// This code assumes the numerator and denominator are the 
			//  same length, and den[0] == 1.0.
			int n = src.length, nb = n < num.length ? n : num.length;
			
			// Startup.  src and outputs are zero before start of array.
			dest[0] = num[0] * src[0];
			for (int ii = 1; ii < nb; ++ii) {
				double y = num[0] * src[ii];
				for (int iii = 1; iii <= ii; ++iii)
					y += num[iii] * src[ii - iii] - den[iii] * dest[ii - iii];
				dest[ii] = y;
			}
			
			for (int ii = nb; ii < n; ++ii) {
				double y = num[0] * src[ii - 0];
				for (int iii = 1; iii < nb; ++iii)
					y += num[iii] * src[ii - iii] - den[iii] * dest[ii - iii];
				dest[ii] = y;
			}			
		}
		
		private static void bilinear(Complex[] p)
		{
/*
		fs = 2*fs;
	end
	z = z(finite(z));	 % Strip infinities from zeros
	pd = (1+p/fs)./(1-p/fs); % Do bilinear transformation
	zd = (1+z/fs)./(1-z/fs);
% real(kd) or just kd?
	kd = (k*prod(fs-z)./prod(fs-p));
	zd = [zd;-ones(length(pd)-length(zd),1)];  % Add extra zeros at -1
*/			
			int fs = 2 * 2;
			double kc = 1, kd = 1;
			for (int ii = 0; ii < p.length; ++ii) {
				kc *= p[ii].abs();
				kd *= p[ii].neg().add(fs).abs();
				p[ii] = p[ii].div(fs).add(1).div(p[ii].div(fs).neg().add(1));
			}
		}
		
		private final double[] num, den;
	} // end class Filter		
}

