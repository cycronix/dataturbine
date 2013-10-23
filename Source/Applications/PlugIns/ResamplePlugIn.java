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
                        
                        //data = GetDataAsArray.get(fwdData, index);
                        data = fwdData.GetDataAsArray(index);
                        if (data == null) {
                                System.err.println("ResamplePlugIn: Unsupported datatype.");
                                continue;
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
                        // post-pad and shift data to avoid filter startup transient and adjust for delay.  MJM 10/2013
                                int padpts = ddata.length/2;
                                double[] pdata = new double[padpts+ddata.length];
//                                for(int i=0; i<padpts; i++) pdata[i]=ddata[0];
                                System.arraycopy(ddata, 0, pdata, 0, ddata.length);		// MJM 10/2013:  post pad to remove delay
//                                System.arraycopy(ddata, 0, pdata, padpts, ddata.length);
                        
                                double[] dataOut = new double[pdata.length];
                                Filter lowPass = new Filter(1.0 / ndeci);
                                lowPass.filter(pdata, dataOut);
                                System.arraycopy(dataOut, padpts, ddata, 0, ddata.length);
                                data = ddata;
                                
//                              double[] dataOut = new double[ddata.length];
//                              Filter lowPass = new Filter(1.0 / ndeci);
//                              lowPass.filter(ddata, dataOut);
//                              data = dataOut;
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
}
