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


package com.rbnb.trim;

import java.util.Enumeration;
import java.util.Vector;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.HostAndPortDialog;
import com.rbnb.utility.Utility;

public class Trim {
    
    // If we are going to output a mix of compressed and timepoint frames, this
    // is the required minimum number of points per compressed frame
    private int fs_min = 10;
    
    // The maximum number of points per frame
    private int fs_max = 10000;
    
    // Time tolerance used in the time compression algorithm.
    private double t_tol = 0.25;
    
    // Only output timepoint frames?
    private boolean bTimepointOnly = false;
    
    // Cache frames in the output source
    private int cacheFrames = 100;
    
    // Archive frames in the output source
    private int archiveFrames = 999999999;
    
    public Trim(String[] argsI) {
	
	String serverAddress = "localhost:3333";
	double startTime = -Double.MAX_VALUE;
	double endTime = -Double.MAX_VALUE;
	String inputSourceName = null;
	String outputSourceName = null;
	// JPW 11/17/2005: Default 10 sec timeout on fetch
	long timeout = 10000;
	
	// Parse command line args
	try {
	    ArgHandler ah = new ArgHandler(argsI);
	    if (ah.checkFlag('h') || ah.checkFlag('?')) {
		throw new Exception("Help message");
	    }
	    if (ah.checkFlag('a')) {
		// RBNB address
		String address = ah.getOption('a');
		if (address != null) {
		    serverAddress = address;
		}
	    }
	    if (ah.checkFlag('b')) {
		// Start (begin) time
		String startStr = ah.getOption('b');
		if (startStr != null) {
		    // Make sure this is a double value
		    try {
			startTime = Double.parseDouble(startStr);
		    } catch (NumberFormatException nfe) {
			throw new Exception(
			    "Must enter a number for the start time.");
		    }
		}
	    }
	    // cache frames
            if (ah.checkFlag('c')) {
               String ncf = ah.getOption('c');
               if (ncf != null) {
		   try {
		       cacheFrames = Integer.parseInt(ncf);
		       if (cacheFrames < 1) {
			   throw new NumberFormatException("");
		       }
		   } catch (NumberFormatException nfe) {
		       System.err.println(
		         "Error parsing cache frame size; " +
			 "set num cache frames = " +
			 cacheFrames);
		   }
	       }
            }
	    if (ah.checkFlag('e')) {
		// Stop (end) time
		String endStr = ah.getOption('e');
		if (endStr != null) {
		    // Make sure this is a double value
		    try {
			endTime = Double.parseDouble(endStr);
		    } catch (NumberFormatException nfe) {
			throw new Exception(
			    "Must enter a number for the end time.");
		    }
		    // Make sure startTime < endTime
		    if (endTime <= startTime) {
			throw new Exception(
			    "End time must be greater than start time.");
		    }
		}
	    }
	    if (ah.checkFlag('i')) {
		// Input source
		String sourceStr = ah.getOption('i');
		if (sourceStr != null) {
		    inputSourceName = sourceStr;
		}
	    } else {
		throw new Exception("Must specify the input source.");
	    }
	    if (ah.checkFlag('k')) {
               String naf = ah.getOption('k');
               if (naf != null) {
		   try {
		       archiveFrames = Integer.parseInt(naf);
		       if (archiveFrames < 0) {
			   throw new NumberFormatException("");
		       }
		   } catch (NumberFormatException nfe) {
		       System.err.println(
		           "Error parsing archive frame size; " +
			   "set num archive frames = " +
			   archiveFrames);
		   }
	       }
            }
	    if (ah.checkFlag('o')) {
		// Output source
		String sourceStr = ah.getOption('o');
		if (sourceStr != null) {
		    outputSourceName = sourceStr;
		}
	    } else {
		throw new Exception("Must specify the output source.");
	    }
	    if (ah.checkFlag('t')) {
		// Fetch timeout
		String timeoutStr = ah.getOption('t');
		if (timeoutStr != null) {
		    // Make sure this is a long value
		    try {
			timeout = Long.parseLong(timeoutStr);
		    } catch (NumberFormatException nfe) {
			throw new Exception(
			    "Must enter an integer for the timeout");
		    }
		}
	    }
	    if (ah.checkFlag('T')) {
		// User only wants to output timepoint frames
		bTimepointOnly = true;
	    }
	    
	    // Frame compression algorithm parameters
	    if (ah.checkFlag('d')) {
		// Compression algorithm time tolerance
		String tolStr = ah.getOption('d');
		if (tolStr != null) {
		    // Make sure this is a double value
		    try {
			t_tol = Double.parseDouble(tolStr);
		    } catch (NumberFormatException nfe) {
			throw new Exception(
			    "Must enter a number for the time tolerance.");
		    }
		}
	    }
	    if (ah.checkFlag('m')) {
		// Minimum number of data points per compressed frame
		String numFramesStr = ah.getOption('m');
		if (numFramesStr != null) {
		    // Make sure this is an integer
		    try {
			fs_min = Integer.parseInt(numFramesStr);
		    } catch (NumberFormatException nfe) {
			throw new Exception(
			    "Must enter an integer for the minimum number of data points per compressed frame");
		    }
		}
	    }
	    if (ah.checkFlag('x')) {
		// Maximum number of data points per frame
		String numFramesStr = ah.getOption('x');
		if (numFramesStr != null) {
		    // Make sure this is an integer
		    try {
			fs_max = Integer.parseInt(numFramesStr);
		    } catch (NumberFormatException nfe) {
			throw new Exception(
			    "Must enter an integer for the maximum number of data points per frame");
		    }
		}
	    }
	} catch (Exception e) {
	    if (!e.getMessage().equals("Help message")) {
	        System.err.println(
	    	    "rbnbTrim argument exception " + e.getMessage());
	    }
	    // Print a help message
	    System.err.println("Produce RBNB archive by slicing a section from an existing RBNB source:");
	    System.err.println("Print help message:    <-h | -?>");
	    System.err.println("RBNB address:          -a <host[:port]>");
	    System.err.println("                        default: localhost:3333");
	    System.err.println("Start (begin) time:    -b <start time>");
	    System.err.println("                        default: input source's start time");
	    System.err.println("Output source cache frames:      -c <cache frames>");
	    System.err.println("                                 default: 100");
	    System.err.println("Stop (end) time:       -e <stop time>");
	    System.err.println("                        default: input source's end time");
	    System.err.println("Output source archive frames:    -k <archive frames>");
	    System.err.println("                                 default: 999999999; \"-k 0\" means no archive");
	    System.err.println("Input source:          -i <input source name>");
	    System.err.println("Output source:         -o <output source name>");
	    System.err.println("Fetch timeout:         -t <timeout in msec>");
	    System.err.println("                        default: 10000 msec");
	    System.err.println("Only output timepoint frames:    -T");
	    System.err.println("Frame compression algorithm parameters:");
	    System.err.println("Time tolerance                                       -d <tolerance, sec>");
	    System.err.println("                                                      default: 0.25 sec");
	    System.err.println("Minimum number of data points per compressed frame:  -m <num points>");
	    System.err.println("                                                      default: 10");
	    System.err.println("Maximum number of data points per frame:             -x <num points>");
	    System.err.println("                                                      default: 10000");
	    System.exit(0);
	}
	
	try {
	    processData(
	        serverAddress,
	        startTime,
	        endTime,
	        inputSourceName,
	        outputSourceName,
		timeout);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(0);
	}
	
    }
    
    private void processData(
        String serverAddressI,
	double startTimeI,
	double endTimeI,
	String inputSourceNameI,
	String outputSourceNameI,
	long timeoutI)
    throws Exception
    {
	
	System.err.println("\nRBNB:\t\t" + serverAddressI);
	System.err.println("Input source:\t" + inputSourceNameI);
	System.err.println("Output source:\t" + outputSourceNameI);
	System.err.println("Fetch timeout:\t" + timeoutI);
	
	// Get registration of input source to determine all the input chans
	System.err.print(
	    "\nObtain channel information from the input source...");
	Sink sink = new Sink();
	sink.OpenRBNBConnection(serverAddressI, "tempSink");
	ChannelMap cm = new ChannelMap();
	cm.Add( new String(inputSourceNameI + "/...") );
	sink.RequestRegistration(cm);
	ChannelMap registrationMap = sink.Fetch(timeoutI);
	sink.CloseRBNBConnection();
	System.err.println("done.");
	if ( (registrationMap == null) ||
	     (registrationMap.NumberOfChannels() == 0) )
	{
	    throw new Exception(
	        "No channels available from input source " + inputSourceNameI);
	}
	System.err.println("Channels:\n" + registrationMap);
	
	// If user hasn't supplied the start and/or end time, get the missing
	// time information from the registration map
	if ( (startTimeI == -Double.MAX_VALUE) ||
	     (endTimeI == -Double.MAX_VALUE) )
	{
	    double startT = Double.MAX_VALUE;
	    double endT = -Double.MAX_VALUE;
	    for (int i = 0; i < registrationMap.GetChannelList().length; ++i) {
		double chanStartT = registrationMap.GetTimeStart(i);
		double chanEndT =
		    chanStartT + registrationMap.GetTimeDuration(i);
		if (chanStartT < startT) {
		    startT = chanStartT;
		}
		if (chanEndT > endT) {
		    endT = chanEndT;
		}
	    }
	    if (startTimeI == -Double.MAX_VALUE) {
		startTimeI = startT;
	    }
	    if (endTimeI == -Double.MAX_VALUE) {
		endTimeI = endT;
	    }
	}
	System.err.println("Start time:\t" + startTimeI);
	System.err.println("End time:\t" + endTimeI);
	
	if (bTimepointOnly) {
	    System.err.println("Only output timepoint frames (no compressed frames)");
	} else {
	    System.err.println("Frame compression algorithm parameters");
	    System.err.println("Time tolerance:\t" + t_tol);
	    System.err.println("Minimum number of data points per compressed frame:\t" + fs_min);
	}
	System.err.println("Maximum number of data points per frame:\t" + fs_max);
	
	// Open source and sink connections
	System.err.print("Open sink and source connections to the RBNB...");
	sink = new Sink();
	sink.OpenRBNBConnection(serverAddressI,"trimSink");
	Source source = null;
	if (archiveFrames > 0) {
	    source = new Source(cacheFrames, "create", archiveFrames);
	} else {
	    source = new Source(cacheFrames, "none", 0);
	}
	source.OpenRBNBConnection(serverAddressI,outputSourceNameI);
	System.err.println("done.");
	String actualOutputSourceName = source.GetClientName();
	if (!actualOutputSourceName.equals(outputSourceNameI)) {
	    System.err.println(
		"\nThe actual output source name = " +
		actualOutputSourceName +
		"\n");
	}
	
	double duration = endTimeI - startTimeI;
	for (int i = 0; i < registrationMap.NumberOfChannels(); ++i) {
	    System.err.println(
		"\nRequest data from " + registrationMap.GetName(i));
	    ChannelMap requestMap = new ChannelMap();
	    requestMap.Add(registrationMap.GetName(i));
	    sink.Request(requestMap,startTimeI,duration,"absolute");
	    ChannelMap dataMap = sink.Fetch(timeoutI);
	    if ( (dataMap == null) || (dataMap.NumberOfChannels() != 1) ) {
		if (dataMap.GetIfFetchTimedOut()) {
		    System.err.println(
		        "Fetch timeout; consider running Trim again with " +
			"larger timeout.");
		}
		System.err.println(
		    "No data in channel " + registrationMap.GetName(i));
	    } else {
		// Send data to the new Source
		outputData(source, dataMap, inputSourceNameI);
	    }
	}
	
	System.err.print(
	    "\nDetaching output source and closing RBNB connections...");
	source.Detach();
	sink.CloseRBNBConnection();
	source.CloseRBNBConnection();
	System.err.println("done.");
	System.exit(0);
	
    }
    
    // Flush data in the given ChannelMap to the output source.  Each channel
    // will have its own RingBuffer.
    public void outputData(
        Source sourceI, ChannelMap dataMapI, String inputSourceNameI)
    throws Exception
    {
	for (int i = 0; i < dataMapI.NumberOfChannels(); ++i) {
	    String chanName = dataMapI.GetName(i);
	    // Peel off the original input source name from the beginning
	    if (chanName.startsWith(inputSourceNameI)) {
		chanName =
		    chanName.substring(inputSourceNameI.length());
	    }
	    
	    // Determine compressed and timepoint frame boundaries
	    double[] time = dataMapI.GetTimes(i);
	    Vector frameInfoV = frameSegmentation(time, true);
	    
	    // Pull out the channel's data
	    byte[][] byteData = null;
	    String[] strData = null;
	    float[] dataFloat = null;
	    double[] dataDouble = null;
	    short[] dataShort = null;
	    int[] dataInt = null;
	    long[] dataLong = null;
	    byte[] dataByte = null;
	    switch (dataMapI.GetType(i)) {
		case (ChannelMap.TYPE_BYTEARRAY):
		    byteData = dataMapI.GetDataAsByteArray(i);
		    break;
		case (ChannelMap.TYPE_STRING):
		    strData = dataMapI.GetDataAsString(i);
		    break;
		case (ChannelMap.TYPE_FLOAT32):
		    dataFloat = dataMapI.GetDataAsFloat32(i);
		    break;
		case (ChannelMap.TYPE_FLOAT64):
		    dataDouble = dataMapI.GetDataAsFloat64(i);
		    break;
		case (ChannelMap.TYPE_INT16):
		    dataShort = dataMapI.GetDataAsInt16(i);
		    break;
		case (ChannelMap.TYPE_INT32):
		    dataInt = dataMapI.GetDataAsInt32(i);
		    break;
		case (ChannelMap.TYPE_INT64):
		    dataLong = dataMapI.GetDataAsInt64(i);
		    break;
		case (ChannelMap.TYPE_INT8):
		    dataByte = dataMapI.GetDataAsInt8(i);
		    break;
		default:
		    throw new Exception(
		        "Unknown data type for channel " + chanName);
	    } //end switch(type)
	    
	    // Write out the frames
	    for (Enumeration e = frameInfoV.elements(); e.hasMoreElements();) {
		FrameInfo fi = (FrameInfo)e.nextElement();
		ChannelMap cm = new ChannelMap();
		int chanIdx = cm.Add(chanName);
		String mimeType = dataMapI.GetMime(i);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    cm.PutMime(chanIdx,mimeType);
		}
		/*
		 * I think user info is only added if we register a channel
		 *
		String userInfo = dataMapI.GetUserInfo(i);
		if ( (userInfo != null) && (!userInfo.equals("")) ) {
		    cm.PutUserInfo(chanIdx,userInfo);
		}
		*/
		int frameLength = fi.endIndex - fi.startIndex + 1;
		if (fi.type == FrameInfo.COMPRESSED) {
		    // Write out the frame's single start time and duration
		    // NOTE: The frame's end time is the time of the data point
		    //       just *after* the last data point in the frame.
		    //       The way the frame segmentation is done the last
		    //       frame is always a timepoint frame, therefore
		    //       "time[fi.endIndex + 1]" will always exist.
		    //       for this compressed frame.
		    double duration =
		        time[fi.endIndex + 1] - time[fi.startIndex];
		    cm.PutTime(time[fi.startIndex], duration);
		} else {
		    // Add array of times to the ChannelMap
		    double[] timeSlice = new double[frameLength];
		    System.arraycopy(
		        time, fi.startIndex, timeSlice, 0, frameLength);
		    cm.PutTimes(timeSlice);
		}
		switch (dataMapI.GetType(i)) {
		    case (ChannelMap.TYPE_BYTEARRAY):
			for (int ii = fi.startIndex; ii <= fi.endIndex; ++ii) {
			    cm.PutDataAsByteArray(chanIdx, byteData[ii]);
			}
			break;
		    case (ChannelMap.TYPE_STRING):
			for (int ii = fi.startIndex; ii <= fi.endIndex; ++ii) {
			    cm.PutDataAsString(chanIdx, strData[ii]);
			}
			break;
		    case (ChannelMap.TYPE_FLOAT32):
			float[] floatSlice = new float[frameLength];
			System.arraycopy(
		            dataFloat,
			    fi.startIndex,
			    floatSlice,
			    0,
			    frameLength);
			cm.PutDataAsFloat32(chanIdx, floatSlice);
			break;
		    case (ChannelMap.TYPE_FLOAT64):
			double[] doubleSlice = new double[frameLength];
			System.arraycopy(
		            dataDouble,
			    fi.startIndex,
			    doubleSlice,
			    0,
			    frameLength);
			cm.PutDataAsFloat64(chanIdx, doubleSlice);
			break;
		    case (ChannelMap.TYPE_INT16):
			short[] shortSlice = new short[frameLength];
			System.arraycopy(
		            dataShort,
			    fi.startIndex,
			    shortSlice,
			    0,
			    frameLength);
			cm.PutDataAsInt16(chanIdx, shortSlice);
			break;
		    case (ChannelMap.TYPE_INT32):
			int[] intSlice = new int[frameLength];
			System.arraycopy(
		            dataInt,
			    fi.startIndex,
			    intSlice,
			    0,
			    frameLength);
			cm.PutDataAsInt32(chanIdx, intSlice);
			break;
		    case (ChannelMap.TYPE_INT64):
			long[] longSlice = new long[frameLength];
			System.arraycopy(
		            dataLong,
			    fi.startIndex,
			    longSlice,
			    0,
			    frameLength);
			cm.PutDataAsInt64(chanIdx, longSlice);
			break;
		    case (ChannelMap.TYPE_INT8):
			byte[] byteSlice = new byte[frameLength];
			System.arraycopy(
		            dataByte,
			    fi.startIndex,
			    byteSlice,
			    0,
			    frameLength);
			cm.PutDataAsInt8(chanIdx, byteSlice);
			break;
		} //end switch(type)
		sourceI.Flush(cm,true);
	    } // end Enumeration over all the FrameInfo objects
	} // end iterating over all channels in dataMapI
    }
    
    // Determine frame segments using the given timeI array of timestamps.
    // There are two kinds of frames:
    // 1. Compressed frames: the time for each data point in the frame can be
    //    determined knowing the frame's start time and duration
    // 2. Timepoint frames: each data point in the frame has its own timestamp.
    private Vector frameSegmentation(double[] timeI, boolean bDebugI) {
	
	Vector frameInfoV = new Vector();
	int numPoints = timeI.length;
	int numCompressedFrames = 0;
	int numTimepointFrames = 0;
	
	/////////////////////////////////////////////
	// User only wants to output timepoint frames
	/////////////////////////////////////////////
	if (bTimepointOnly) {
	    // The only limitation on the frames is that we must still obey
	    // the maximum frame size, fs_max
	    if (numPoints <= fs_max) {
		// All the data will fit into 1 timepoint frame
		numTimepointFrames++;
		frameInfoV.add(
		    new FrameInfo(
			0,
			numPoints - 1,
			FrameInfo.TIMEPOINT));
	    } else {
		// Break up the input into a series of timepoint frames, where
		// each frame has no larger than fs_max number of points
		int numMaxFrames = (int)(numPoints / fs_max);
		for (int i = 0; i < numMaxFrames; ++i) {
		    numTimepointFrames++;
		    frameInfoV.add(
			new FrameInfo(
			    i*fs_max,
			    ((i+1)*fs_max) - 1,
			    FrameInfo.TIMEPOINT));
		}
		// Now add the final timepoint frame (if one is needed)
		int startIndex = numMaxFrames * fs_max;
		int endIndex = numPoints - 1;
		if (startIndex <= endIndex) {
		    numTimepointFrames++;
		    frameInfoV.add(
			new FrameInfo(
			    startIndex,
			    endIndex,
			    FrameInfo.TIMEPOINT));
		}
	    }
	}
	
	/////////////////////////////////////////////////
	// Ouput a mix of timepoint and compressed frames
	/////////////////////////////////////////////////
	else {
	    int tindex = 0;
	    while (tindex < numPoints) {
		int percentDone = (int)(100 * tindex/numPoints);
		System.err.print(percentDone + "%...");
		// See if the next fs_min number of data points can be
		// compressed into a frame.  The range of indeces in the frame
		// will go from tindex to (tindex + fs_min - 1); the
		// RBNB calculated end time of the compressed frame will be
		// time[tindex + fs_min], although this data point itself will
		// not be included in this compressed frame
		if ( ((tindex + fs_min) < numPoints) &&
		     (isCompressible(timeI, tindex, tindex + fs_min)) )
		{
		    // OK, so we know that at least the frame from
		    // tindex to (tindex + fs_min - 1) is compressible.
		    // See how far the compression can go; each time through the
		    // test loop we will double the frame size
		    
		    // For the frame that we know compresses, this is the index
		    // at which is found the frame end time
		    int lastEndTimeIndex = tindex + fs_min;
		    
		    // New proposed frame length
		    int proposedFrameLength = 2 * fs_min;
		    
		    // The index at which is found the proposed frame's end time
		    int proposedEndTimeIndex = tindex + proposedFrameLength;
		    
		    while ( (proposedEndTimeIndex < numPoints) &&
		    	    (proposedFrameLength <= fs_max) &&
		            (isCompressible(timeI, tindex, proposedEndTimeIndex)) )
		    {
			// We were able to double the size of the compressed frame
			lastEndTimeIndex = proposedEndTimeIndex;
			
			// Now we'll try doubling the frame size again
			proposedFrameLength = 2 * proposedFrameLength;
			proposedEndTimeIndex = tindex + proposedFrameLength;
		    }
		    numCompressedFrames++;
		    frameInfoV.add(
		    	new FrameInfo(
			    tindex,                // index of the first point in the new frame
			    lastEndTimeIndex - 1,  // index of the last point in the new frame
			    FrameInfo.COMPRESSED));
		    // Increment the index
		    tindex = lastEndTimeIndex;
		}
		else
		{
		    int endIdx = tindex + fs_min - 1;
		    if (endIdx >= numPoints) {
			endIdx = numPoints - 1;
		    }
		    // Put the data points into a timepoint frame.
		    // If the last FrameInfo object added to the Vector was
		    // for a Timepoint frame, and if extending this timepoint
		    // frame won't increase the size of the frame beyond
		    // fs_max, then just update its "endIndex" field to include
		    // this new set of points
		    FrameInfo fi = null;
		    if (frameInfoV.size() > 0) {
			fi = (FrameInfo)frameInfoV.lastElement();
		    }
		    if ( (fi != null) &&
			 (fi.type == FrameInfo.TIMEPOINT) &&
		     	 ((endIdx - fi.startIndex + 1) <= fs_max) )
		    {
			fi.endIndex = endIdx;
		    }
		    else
		    {
			numTimepointFrames++;
			frameInfoV.add(
			    new FrameInfo(
				tindex,
				endIdx,
				FrameInfo.TIMEPOINT));
		    }
		    // Increment the index
		    tindex = endIdx + 1;
		}
	    }
	}
	
	if (bDebugI) {
	    System.err.println(
		"\n\nNumber of points = " + numPoints);
	    System.err.println(
		"Number of compressed frames = " + numCompressedFrames);
	    System.err.println(
		"Number of timepoint frames = " + numTimepointFrames);
	    System.err.println("\nFrame segmentation:");
	    for (Enumeration e = frameInfoV.elements(); e.hasMoreElements();) {
		FrameInfo fi = (FrameInfo)e.nextElement();
		System.err.println("\n" + fi);
	    }
	}
	
	return frameInfoV;
	
    }
    
    // A compressed frame is being proposed to include indeces in the range
    // fStartIdx to (idxOfEndTime - 1).  The start time of the proposed frame
    // is timeI[fStartIdx] and the end time is timeI[idxOfEndTime].  Note,
    // however, that the data point at idxOfEndTime is not itself included in
    // this proposed frame; this is simply the index at which we find the end
    // time of the proposed compressed frame.
    //
    // To see if this proposed frame can actually compress, we perform a linear
    // regression of the timestamps.  If the difference between each of the
    // calculated/fit timestamps and the actual timestamps is within t_tol
    // then the frame can be compressed and this method returns true.
    // Otherwise, this method returns false, indicating that the frame
    // cannot be compressed.
    private boolean isCompressible(
        double[] timeI, int fStartIdx, int idxOfEndTime)
    {
	
	// System.err.println(
	//     "isCompressible: startIdx = " +
	//     fStartIdx +
	//     ", endIdx = " +
	//     (idxOfEndTime - 1));
	
	// The index of the first data point in the frame is fStartIdx
	// The index of the last data point in the frame is (idxOfEndTime - 1)
	int numPts = (idxOfEndTime - 1) - fStartIdx + 1;
	double duration = timeI[idxOfEndTime] - timeI[fStartIdx];
	double dt = duration / numPts;
	// Check the time error for each timestamp in the proposed frame
	for (int i = fStartIdx; i < idxOfEndTime; ++i) {
	    double t_calc = timeI[fStartIdx] + (dt * (i - fStartIdx));
	    double t_act = timeI[i];
	    if (Math.abs(t_calc - t_act) > t_tol) {
		return false;
	    }
	}
	return true;
    }
    
    private class FrameInfo {
	
	public final static int COMPRESSED = 1;
	public final static int TIMEPOINT = 2;
	
	public int startIndex = -1;
	public int endIndex = -1;
	public int type = COMPRESSED;
	
	public FrameInfo(int startIndexI, int endIndexI, int typeI) {
	    startIndex = startIndexI;
	    endIndex = endIndexI;
	    type = typeI;
	}
	
	public String toString() {
	    String typeStr = "timepoint";
	    if (type == 1) {
		typeStr = "compressed";
	    }
	    return
	        new String(
		    "Start: " +
		    startIndex +
		    ", End: " +
		    endIndex +
		    ", Type: " +
		    typeStr);
	}
	
    }
}

