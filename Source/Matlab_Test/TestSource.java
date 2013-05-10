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
 * An RBNB test source.
 * <p>
 * This class provides a source for testing the RBNB server.  It allows
 * for the user to set the address of the server, the name of the source,
 * the number of channels in the source, and the number of points per
 * channel per frame.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.2
 * @version 10/31/2003
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
public class TestSource
    extends Thread
{
    /**
     * debug?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/31/2003
     */
    public boolean debug = false;

    /**
     * the number of channels of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    private int numberOfChannels = 1;

    /**
     * the number of frames of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    private int numberOfFrames = 1000;

    /**
     * the number of points per channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    private int pointsPerChannel = 10;

    /**
     * the address of the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    private String rbnbAddress = "localhost:3333";

    /**
     * the name of the source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    private String sourceName = "TestSource";

    /**
     * the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2/
     * @version 10/22/2003
     */
    private Source src = null;

    /**
     * the status.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    private int status = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
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
    public TestSource() {
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
     * @since V2.2
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
     * Creates the <code>ChannelMap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ChannelMap</code>.
     * #exception com.rbnb.sapi.SAPIException
     *		  if there is a problem building the <code>ChannelMap</code>.
     * @since V2.2
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
    private final ChannelMap createCmap()
	throws SAPIException
    {
	ChannelMap cmapR = new ChannelMap();

	for (int channel = 0; channel < getNumberOfChannels(); ++channel) {
	    cmapR.Add("c" + channel);
	}

	return (cmapR);
    }

    /**
     * Creates the data array.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data array.
     * @since V2.2
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
    private final float[][][] createData() {
	int nFrames;
	int tPoints = 360;

	if ((tPoints % getPointsPerChannel()) == 0) {
	    nFrames = tPoints/getPointsPerChannel();
	} else {
	    nFrames = (tPoints/getPointsPerChannel()) + 1;
	    tPoints = nFrames*getPointsPerChannel();
	}

	float[][][] dataR = new float[nFrames][][];
	for (int frame = 0; frame < nFrames; ++frame) {
	    dataR[frame] = new float[getNumberOfChannels()][];
	    for (int channel = 0; channel < getNumberOfChannels(); ++channel) {
		dataR[frame][channel] = new float[getPointsPerChannel()];
		for (int point = 0; point < getPointsPerChannel(); ++point) {
		    dataR[frame][channel][point] = (float)
			Math.sin(2.*
				 (channel +
				  frame*getPointsPerChannel() +
				  point)*Math.PI/tPoints);
		}
	    }
	}

	return (dataR);
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
     * @since V2.2
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
	Source sourceR = new Source(10,"create",getNumberOfFrames());
	sourceR.OpenRBNBConnection(getRBNBAddress(),getSourceName());

	return (sourceR);
    }

    /**
     * Gets the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of channels.
     * @see #setNumberOfChannels(int numberOfChannelsI)
     * @since V2.2
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
    public final int getNumberOfChannels() {
	return (numberOfChannels);
    }

    /**
     * Gets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of frames.
     * @see #setNumberOfFrames(int numberOfFramesI)
     * @since V2.2
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
    public final int getNumberOfFrames() {
	return (numberOfFrames);
    }

    /**
     * Gets the number of points per channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of points.
     * @see #setPointsPerChannel(int pointsPerChannelI)
     * @since V2.2
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
    public final int getPointsPerChannel() {
	return (pointsPerChannel);
    }

    /**
     * Gets the RBNB address.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the RBNB address.
     * @see #setRBNBAddress(String rbnbAddressI)
     * @since V2.2
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
     * Gets the source name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the source name.
     * @see #setSourceName(String sourceNameI)
     * @since V2.2
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
     * @since V2.2
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
     * Runs the source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
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
	setStatus(0);

	try {
	    if (getNumberOfChannels() == 0) {
		throw new IllegalArgumentException
		    (getSourceName() +
		     " too few channels.");
	    } else if (getPointsPerChannel() == 0) {
		throw new IllegalArgumentException
		    (getSourceName() +
		     " too few points per channel.");
	    } else if (getNumberOfFrames() == 0) {
		throw new IllegalArgumentException
		    (getSourceName() +
		     " too few frames.");
	    }
	    double[] time = new double[getPointsPerChannel()];
	    float[][][] data = createData();
	    ChannelMap cmap = createCmap();
	    src = createSource();

	    for (int frame = 0; frame < getNumberOfFrames(); ++frame) {
		for (int idx = 0; idx < getPointsPerChannel(); ++idx) {
		    time[idx] = frame + idx/((double) getPointsPerChannel());;
		}
		cmap.PutTimes(time);
		    
		for (int channel = 0;
		     channel < getNumberOfChannels();
		     ++channel) {
		    cmap.PutDataAsFloat32
			(channel,
			 data[frame % data.length][channel]);
		}
		src.Flush(cmap,true);
		if (debug) {
		    System.err.println(getSourceName() + " frame " + frame);
		}
	    }

	} catch (Exception e) {
	    setStatus(1);
	    e.printStackTrace();
	}
    }

    /**
     * Sets the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numberOfChannelsI the number of channels.
     * @see #getNumberOfChannels()
     * @since V2.2
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
    public final void setNumberOfChannels(int numberOfChannelsI) {
	numberOfChannels = numberOfChannelsI;
    }

    /**
     * Sets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numberOfFramesI the number of frames.
     * @see #getNumberOfFrames()
     * @since V2.2
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
    public final void setNumberOfFrames(int numberOfFramesI) {
	numberOfFrames = numberOfFramesI;
    }

    /**
     * Sets the number of points per channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pointsPerChannelI the number of points.
     * @see #getPointsPerChannel()
     * @since V2.2
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
    public final void setPointsPerChannel(int pointsPerChannelI) {
	pointsPerChannel = pointsPerChannelI;
    }

    /**
     * Sets the RBNB address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbAddressI the RBNB address.
     * @see #getRBNBAddres()
     * @since V2.2
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
     * Sets the source name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceNameI the source name.
     * @see #getSourceName()
     * @since V2.2
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
     * @since V2.2
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
}
