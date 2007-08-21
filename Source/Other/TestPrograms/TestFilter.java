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

import com.rbnb.sapi.*;

/**
 * An RBNB test filter.
 * <p>
 * This class provides a filter (a streaming sink, a simple data conversion,
 * and an output source).  It allows the user to set the address of server,
 * the name of the source, a list of streamed channels, a list of requested
 * channels, and a maximum number of frames to process.
 * <p>
 * The filter is a simple inversion filter that works on any numeric input.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.3
 * @version 10/22/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/22/2003  INB	Created.
 *
 */
public final class TestFilter
    extends Thread
{
    /**
     * the last time seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/23/2003
     */
    private double lastTime = -Double.MAX_VALUE;

    /**
     * the maximum number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */
    private int maximumFrames = 100;

    /**
     * the address of the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */
    private String rbnbAddress = "localhost:3333";

    /**
     * the channels to request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */
    private String[] requestChannels = null;

    /**
     * the name of the source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */
    private String sourceName = "TestSource";

    /**
     * the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3/
     * @version 10/22/2003
     */
    private Source src = null;

    /**
     * the status.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */
    private int status = 0;

    /**
     * the channels to stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */
    private String[] streamChannels = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public TestFilter() {
	super();
    }

    /**
     * Closes the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param keepCacheI   keep the cache?
     * @param keepArchiveI keep the archive?
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void close(boolean keepCacheI,boolean keepArchiveI) {
	if (src != null) {
	    src.CloseRBNBConnection(keepCacheI,keepArchiveI);
	    src = null;
	}
    }

    /**
     * Creates a <code>ChannelMap</code> for the request channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ChannelMap</code> or <code>null</code>.
     * @exception com.rbnb.sapi.SAPIException
     *		  if the <code>ChannelMap</code> cannot be created.
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    private final ChannelMap createRequestMap()
	throws SAPIException
    {
	ChannelMap cmapR = null;

	if (getRequestChannels() != null) {
	    cmapR = new ChannelMap();

	    for (int idx = 0; idx < getRequestChannels().length; ++idx) {
		cmapR.Add(getRequestChannels()[idx]);
	    }
	}

	return (cmapR);
    }

    /**
     * Creates the RBNB sink.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the RBNB sinke.
     * #exception com.rbnb.sapi.SAPIException
     *		  if there is a problem creating the <code>Sink</code>.
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    private final Sink createSink()
	throws SAPIException
    {
	Sink sinkR = new Sink();
	sinkR.OpenRBNBConnection(getRBNBAddress(),getSourceName() + "_sink");

	return (sinkR);
    }

    /**
     * Creates the RBNB source.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the RBNB source.
     * #exception com.rbnb.sapi.SAPIException
     *		  if there is a problem creating the <code>Source</code>.
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    private final Source createSource()
	throws SAPIException
    {
	Source sourceR = new Source(10,"create",getMaximumFrames());
	sourceR.OpenRBNBConnection(getRBNBAddress(),getSourceName());

	return (sourceR);
    }

    /**
     * Creates a <code>ChannelMap</code> for the stream channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ChannelMap</code> or <code>null</code>.
     * @exception com.rbnb.sapi.SAPIException
     *		  if the <code>ChannelMap</code> cannot be created.
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    private final ChannelMap createStreamMap()
	throws SAPIException
    {
	ChannelMap cmapR = null;

	if (getStreamChannels() != null) {
	    cmapR = new ChannelMap();

	    for (int idx = 0; idx < getStreamChannels().length; ++idx) {
		cmapR.Add(getStreamChannels()[idx]);
	    }
	}

	return (cmapR);
    }

    /**
     * Gets the maximum number of frames to process.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum number of frames to process.
     * @see #setMaximumFrames(int maximumFramesI)
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final int getMaximumFrames() {
	return (maximumFrames);
    }

    /**
     * Gets the RBNB address.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the RBNB address.
     * @see #setRBNBAddress(String rbnbAddressI)
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final String getRBNBAddress() {
	return (rbnbAddress);
    }

    /**
     * Gets the list of channels to request.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of channels to request.
     * @see #setRequestChannels(String[] requestChannelsI)
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final String[] getRequestChannels() {
	return (requestChannels);
    }

    /**
     * Gets the source name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the source name.
     * @see #setSourceName(String sourceNameI)
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final String getSourceName() {
	return (sourceName);
    }

    /**
     * Gets the status code.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the status.
     * @see #setStatus(int statusI)
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final int getStatus() {
	return (status);
    }

    /**
     * Gets the list of channels to stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of channels to stream.
     * @see #setStreamChannels(String[] streamChannelsI)
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final String[] getStreamChannels() {
	return (streamChannels);
    }

    /**
     * Processes a frame of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI	  the expected frame number.
     * @param streamMapI  the streamed <code>ChannelMap</code>.
     * @param requestMapI the requested <code>ChannelMap</code>.
     * @param srcI	  the RBNB <code>Source</code>.
     * @exception com.rbnb.sapi.SAPIException
     *		  if a problem occurs filtering the data.
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void process(int frameI,
			      ChannelMap streamMapI,
			      ChannelMap requestMapI,
			      Source srcI)
	throws SAPIException
    {
	ChannelMap outmap = new ChannelMap();
	ChannelMap inMap;
	int lchan;
	int nPoints = 0;

	if ((streamMapI == null) ||
	    (streamMapI.NumberOfChannels() != getStreamChannels().length)) {
	    throw new IllegalArgumentException
		(getSourceName() + " got bad stream map: " +
		 streamMapI);
	}

	if ((getRequestChannels() != null) &&
	    ((requestMapI == null) ||
	     (requestMapI.NumberOfChannels() !=
	      getRequestChannels().length))) {
	    throw new IllegalArgumentException
		(getSourceName() + " got bad request map: " +
		 requestMapI);
	}

	double[] time = null;
	for (inMap = streamMapI;
	     inMap != null;
	     inMap = ((inMap == streamMapI) ? requestMapI : null)) {
	    for (int channel = 0;
		 channel < inMap.NumberOfChannels();
		 ++channel) {
		time = inMap.GetTimes(channel);
		if ((lastTime != -Double.MAX_VALUE) &&
		    (time[0] > lastTime + 1.)) {
		    System.err.println
			(getSourceName() + " jump in time from " +
			 lastTime + " to " + time[0] +
			 " on channel " + inMap.GetName(channel) +
			 ((inMap == streamMapI) ? "(s)" : "(r)"));
		}
		lchan = outmap.Add(inMap.GetName(channel).replace('/','_'));
		outmap.PutTimeRef(inMap,channel);
		switch (inMap.GetType(channel)) {
		case ChannelMap.TYPE_INT8:
		    byte[] bdata = inMap.GetDataAsInt8(channel);
		    if (nPoints == 0) {
			nPoints = bdata.length;
		    } else if (bdata.length != nPoints) {
			throw new IllegalArgumentException
			    (inMap.GetName(channel) + " has " +
			     bdata.length + " points versus " + nPoints);
		    }
		    for (int point = 0; point < bdata.length; ++point) {
			bdata[point] = (byte) -bdata[point];
		    }
		    outmap.PutDataAsInt8(lchan,bdata);
		    break;
		case ChannelMap.TYPE_INT16:
		    short[] sdata = inMap.GetDataAsInt16(channel);
		    if (nPoints == 0) {
			nPoints = sdata.length;
		    } else if (sdata.length != nPoints) {
			throw new IllegalArgumentException
			    (getSourceName() + " " +
			     inMap.GetName(channel) + " has " +
			     sdata.length + " points versus " + nPoints);
		    }
		    for (int point = 0; point < sdata.length; ++point) {
			sdata[point] = (short) -sdata[point];
		    }
		    outmap.PutDataAsInt16(lchan,sdata);
		    break;
		case ChannelMap.TYPE_INT32:
		    int[] idata = inMap.GetDataAsInt32(channel);
		    if (nPoints == 0) {
			nPoints = idata.length;
		    } else if (idata.length != nPoints) {
			throw new IllegalArgumentException
			    (inMap.GetName(channel) + " has " +
			     idata.length + " points versus " + nPoints);
		    }
		    for (int point = 0; point < idata.length; ++point) {
			idata[point] = -idata[point];
		    }
		    outmap.PutDataAsInt32(lchan,idata);
		    break;
		case ChannelMap.TYPE_INT64:
		    long[] ldata = inMap.GetDataAsInt64(channel);
		    if (nPoints == 0) {
			nPoints = ldata.length;
		    } else if (ldata.length != nPoints) {
			throw new IllegalArgumentException
			    (inMap.GetName(channel) + " has " +
			     ldata.length + " points versus " + nPoints);
		    }
		    for (int point = 0; point < ldata.length; ++point) {
			ldata[point] = -ldata[point];
		    }
		    outmap.PutDataAsInt64(lchan,ldata);
		    break;
		case ChannelMap.TYPE_FLOAT32:
		    float[] fdata = inMap.GetDataAsFloat32(channel);
		    if (nPoints == 0) {
			nPoints = fdata.length;
		    } else if (fdata.length != nPoints) {
			throw new IllegalArgumentException
			    (inMap.GetName(channel) + " has " +
			     fdata.length + " points versus " + nPoints);
		    }
		    for (int point = 0; point < fdata.length; ++point) {
			fdata[point] = -fdata[point];
		    }
		    outmap.PutDataAsFloat32(lchan,fdata);
		    break;
		case ChannelMap.TYPE_FLOAT64:
		    double[] ddata = inMap.GetDataAsFloat64(channel);
		    if (nPoints == 0) {
			nPoints = ddata.length;
		    } else if (ddata.length != nPoints) {
			throw new IllegalArgumentException
			    (inMap.GetName(channel) + " has " +
			     ddata.length + " points versus " + nPoints);
		    }
		    for (int point = 0; point < ddata.length; ++point) {
			ddata[point] = -ddata[point];
		    }
		    outmap.PutDataAsFloat64(lchan,ddata);
		    break;
		}
	    }
	}
	if (time != null) {
	    lastTime = time[time.length - 1];
	}

	System.out.println(getSourceName() + " frame " + frameI);
	System.out.flush();
	srcI.Flush(outmap,true);
    }

    /**
     * Runs the filter.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void run() {
	Sink snk1 = null;
	Sink snk2 = null;

	try {
	    src = createSource();
	    snk1 = createSink();
	    snk2 = createSink();

	    ChannelMap streamMap = createStreamMap();
	    ChannelMap requestMap = createRequestMap();
	    ChannelMap gotSmap = null;
	    ChannelMap gotRmap = null;
	    RequestOptions reqOpt = new RequestOptions();
	    reqOpt.setMaxWait(120000);
	    snk1.Subscribe(streamMap,"oldest");

	    for (int frame = 0; frame < getMaximumFrames(); ++frame) {
		gotSmap = snk1.Fetch(120000);
		if (gotSmap.GetIfFetchTimedOut()) {
		    try {
			throw new Exception("Stream fetch timed out.");
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    setStatus(2);
		    break;
		}

		if (requestMap != null) {
		    snk2.Request(requestMap,
				 gotSmap.GetTimeStart(0),
				 gotSmap.GetTimeDuration(0),
				 "absolute",
				 reqOpt);
		    gotRmap = snk2.Fetch(-1);
		}

		process(frame,gotSmap,gotRmap,src);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    setStatus(1);

	} finally {
	    if (snk1 != null) {
		snk1.CloseRBNBConnection();
	    }
	    if (snk2 != null) {
		snk2.CloseRBNBConnection();
	    }
	}
    }

    /**
     * Sets the maximum number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maximumFramesI the maximum number of frames.
     * @see #getMaximumFrames()
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void setMaximumFrames(int maximumFramesI) {
	maximumFrames = maximumFramesI;
    }

    /**
     * Sets the RBNB address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbAddressI the RBNB address.
     * @see #getRBNBAddres()
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void setRBNBAddress(String rbnbAddressI) {
	rbnbAddress = rbnbAddressI;
    }

    /**
     * Sets the channels to request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestChannelsI the channels to request.
     * @see #getRequestChannels()
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void setRequestChannels(String[] requestChannelsI) {
	requestChannels = requestChannelsI;
    }

    /**
     * Sets the source name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceNameI the source name.
     * @see #getSourceName()
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void setSourceName(String sourceNameI) {
	sourceName = sourceNameI;
    }

    /**
     * Sets the status code.
     * <p>
     *
     * @author Ian Brown
     *
     * @param statusI the status code (0 is good).
     * @see #getStatus()
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void setStatus(int statusI) {
	status = statusI;
    }

    /**
     * Sets the channels to stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param streamChannelsI the channels to stream.
     * @see #getStreamChannels()
     * @since V2.3
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Created.
     *
     */
    public final void setStreamChannels(String[] streamChannelsI) {
	streamChannels = streamChannelsI;
    }
}
