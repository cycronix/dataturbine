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
	FilterPlugIn.java
	
	An RBNB plug-in to perform low pass filtering, based on anti-aliasing code
	in ResamplePlugIn.java.
	
	---  History  ---
	2007/09/12  WHF  Created.
*/

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;


import java.lang.reflect.Array;



/**
  * Performs low-pass filtering on an RBNB channel.  It supports the dynamic
  *  options: 
  * <ul><li>breakPoint - Between zero and one, represents the cutoff frequency
  *  relative to Nyquist.</li>
  * </ul>
  */
public class FilterPlugIn extends com.rbnb.plugins.PlugInTemplate
{
	private static final double DEFAULT_WN = 0.1;
	
	public FilterPlugIn()
	{
		setBreakPoint(DEFAULT_WN);
	}

//*******************************  Accessors  *******************************//
	/**
	  * @return the breakpoint frequency, where 1.0 equals Nyquist (half the
	  *  sample rate).
	  */
	public double getBreakPoint()
	{				
		return Wn;
	}
	
	/**
	  * Sets the breakpoint frequency, which must lie in the interval (0,1).
	  *
	  * @param Wn the breakpoint frequency, where 1.0 equals Nyquist (half the
	  *  sample rate).
	  * @throws IllegalArgumentException  if Wn is out of bounds.
	  */
	// Note the method is final because it is called from the constructor.
	public final void setBreakPoint(double Wn)
	{
		if (Wn > 1.0 || Wn < 0) throw new IllegalArgumentException(
			"The breakpoint must lie in the interval (0,1).");
		
		this.Wn = Wn;
		defaultFilter = new Filter(Wn);		
	}

//**********************  PlugInTemplate Overrides  *************************//
	protected void processRequest(ChannelMap fwdData, PlugInChannelMap out)
			throws SAPIException
	{
		if (fwdData.NumberOfChannels() == 0) return;
		
		// Override member defaults with dynamic options: 
		double Wn = this.Wn;
		Filter filterToUse = defaultFilter;
		
		java.util.Properties opts = getRequestOptions();
		String temp;
		if ((temp = opts.getProperty("breakpoint")) != null) {
			Wn = Double.parseDouble(temp);
			filterToUse = new Filter(Wn);
		}

		for (int index = 0; index < fwdData.NumberOfChannels(); ++index) {
			Object data;
			
			data = GetDataAsArray.get(fwdData, index);
			if (data == null) {
				System.err.println("FilterPlugIn: Unsupported datatype.");
				continue;
			}
		
			double[] ddata;
			if (data instanceof double[])
				ddata = (double[]) data;
			else {
				int npts = Array.getLength(data);
				ddata = new double[npts];
				for (int ii = 0; ii < npts; ++ii)
					ddata[ii] = Array.getDouble(data, ii);
			}
			// Potential optimization: if the filter class supported 
			//  in-place filtering, this second array would be unnecessary.
			double[] dataOut = new double[ddata.length];
			filterToUse.filter(ddata, dataOut);
			out.PutTime(
					fwdData.GetTimeStart(index),
					fwdData.GetTimeDuration(index)
			);
			
			int outIndex = out.Add(fwdData.GetName(index));
			out.PutDataAsFloat64(outIndex, dataOut);
		} // end for
	} // end processRequest
	
//***************************  Private Methods  *****************************//

//***************************  Data Members  ********************************//
	private double Wn;
	private Filter defaultFilter;


//***************************  Static Methods  ******************************//
	private static void showHelp()
	{
		System.err.println("FilterPlugIn [options]\n"
				+"\t-a address:port        RBNB address (localhost:3333)\n"
				+"\t-n name                Client name (FilterPlugIn)\n"
				+"\t-w breakpoint          Cutoff frequency as a fraction of"
						+ " Nyquist ("+DEFAULT_WN+")\n"
		);
	}

	public static void main(String args[]) throws Exception
	{
		FilterPlugIn pi = new FilterPlugIn();
		
		// Parse Command-line:
		try {
			for (int ii = 0; ii < args.length; ++ii) {
				// General template properties:
				if ("-a".equals(args[ii])) pi.setHost(args[++ii]);
				else if ("-n".equals(args[ii])) pi.setName(args[++ii]);
				
				// Filter specific:
				else if ("-h".equals(args[ii]) || "-?".equals(args[ii])) {
					showHelp();
					return;
				} else if ("-w".equals(args[ii])) 
					pi.setBreakPoint(Double.parseDouble(args[++ii]));
			}
		} catch (Exception e) {
			System.err.println("Error with command line arguments.");
			e.printStackTrace();
			showHelp();
			return;
		}
		
		// Start the PlugIn:
		pi.start();
	}
}

