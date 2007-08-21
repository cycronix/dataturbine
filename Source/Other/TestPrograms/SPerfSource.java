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

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;

/**
 * Performance test source using the simple RBNB API.
 * <p>
 *
 * @author Ian Brown
 *
 * @see PerfSource
 * @see com.rbnb.sapi
 * @since V2.0
 * @version 11/07/2002
 */

/*
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/12/2001  INB	Created.
 *
 */

public class SPerfSource
    extends java.lang.Thread
{
    /**
     * the number of cache frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/14/2001
     */
    private int cFrames = 10;

    /**
     * the number of channels per frame.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */
    private int nChans = 5000;

    /**
     * the number of frames to send to the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */
    private int nFrames = 10;

    /**
     * the number of points per channel per frame.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */
    private int nPoints = 200;

    /**
     * the address of the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */
    private String sAddress = "tcp://localhost:3333";

    /**
     * Sets up and runs the performance test.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	SPerfSource perfSource = new SPerfSource();

	for (int idx = 0; idx < argsI.length; ) {
	    int idx1,
		idx2;

	    if ((argsI[idx].charAt(0) != '-') &&
		(argsI[idx].charAt(0) != '/')) {
		throw new java.lang.IllegalArgumentException
		    (argsI[idx] + " is not a switch.");

	    } else if (argsI[idx].length() == 1) {
		throw new java.lang.IllegalArgumentException
		    ("- is not a valid switch.");

	    } else if (argsI[idx].length() == 2) {
		// If there is no switch value in the same argument as the
		// switch, then it must be in the next argument (if any).
		idx1 = idx + 1;
		idx2 = 0;

	    } else {
		// If the switch value is in the same argument as the
		// switch, then point to it.
		idx1 = idx;
		for (idx2 = 2;
		     ((idx2 < argsI[idx].length()) &&
		      Character.isWhitespace(argsI[idx].charAt(idx2)));
		     ++idx2) {}

		if (idx2 == argsI[idx].length()) {
		    // If there is only whitespace in the argument after
		    // the switch, the value must actually be in the next
		    // entry.
		    idx1 = idx + 1;
		    idx2 = 0;
		}
	    }

	    char switchName = argsI[idx].charAt(1);
	    if (switchName == 'a') {
		perfSource.setSaddress(argsI[idx1].substring(idx2));
	    } else if (switchName == 'c') {
		perfSource.setNchans
		    (Integer.parseInt(argsI[idx1].substring(idx2)));
	    } else if (switchName == 'C') {
		perfSource.setCframes
		    (Integer.parseInt(argsI[idx1].substring(idx2)));
	    } else if (switchName == 'f') {
		perfSource.setNframes
		    (Integer.parseInt(argsI[idx1].substring(idx2)));
	    } else if (switchName == 'p') {
		perfSource.setNpoints
		    (Integer.parseInt(argsI[idx1].substring(idx2)));
	    }
	    idx = idx1 + 1;
	}

	perfSource.start();
    }

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2001  INB	Created.
     *
     */
    public SPerfSource() {
	super();
    }

    /**
     * Displays an elapsed time periond in seconds.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the message to display along with the elapsed time.
     * @param lastI    the start of the time period.
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2001  INB	Created.
     *
     */
    public final long displayElapsed(String messageI,long lastI) {
	long nowR = System.currentTimeMillis();
	System.err.println(messageI + " elapsed time: " +
			   ((nowR - lastI)/1000.));
	return (nowR);
    }

    /**
     * Gets the number of cache frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of cache frames.
     * @see #setCframes(int)
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/14/2001  INB	Created.
     *
     */
    public final int getCframes() {
	return (cFrames);
    }

    /**
     * Gets the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of channels.
     * @see #setNchans(int)
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2001  INB	Created.
     *
     */
    public final int getNchans() {
	return (nChans);
    }

    /**
     * Gets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of frames.
     * @see #setNframes(int)
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    public final int getNframes() {
	return (nFrames);
    }

    /**
     * Gets the number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of points.
     * @see #setNpoints(int)
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    public final int getNpoints() {
	return (nPoints);
    }

    /**
     * Gets the address of the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address of the RBNB.
     * @see #setSaddress(String)
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2001  INB	Created.
     *
     */
    public final String getSaddress() {
	return (sAddress);
    }

    /**
     * Runs the performance test.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    public final void run() {
	boolean started = false;
	long first,
	     last = System.currentTimeMillis();
	first = last;
	Source source = null;

	try {
	    // Set up the data area for the frame.
	    float[][] data = new float[getNchans()][];

	    for (int chanIdx = 0; chanIdx < getNchans(); ++chanIdx) {
		data[chanIdx] = new float[getNpoints()];
	    }

	    last = displayElapsed("Set up frame",last);

	    // Create the source connection.
	    source = new Source(getCframes(),"none",0);
	    source.OpenRBNBConnection(getSaddress(),
				      "SPerfSource",
				      "inb",
				      "inb");
	    ChannelMap cmap = new ChannelMap();
	    for (int chanIdx = 0; chanIdx < getNchans(); ++chanIdx) {
		cmap.Add("C" + chanIdx);
	    }
	    last = displayElapsed("Launch source",last);
	    long bof = last;

	    // Post the frames.
	    for (int frameIdx = 0; frameIdx < getNframes(); ++frameIdx) {
		cmap.PutTime(frameIdx,1.);
		for (int chanIdx = 0; chanIdx < getNchans(); ++chanIdx) {
		    /*
		    for (int pointIdx = 0;
			 pointIdx < getNpoints();
			 ++pointIdx) {
			data[chanIdx][pointIdx] =
			    ((float) frameIdx)*getNchans()*getNpoints() +
			    ((float) chanIdx)*getNpoints() +
			    pointIdx;
		    }
		    */
		    cmap.PutDataAsFloat32(chanIdx,data[chanIdx]);
		}
		last = displayElapsed("Frame " + frameIdx + " built",last);
		source.Flush(cmap);
		last = displayElapsed("Frame " + frameIdx + " sent",last);
	    }
	    displayElapsed("Frame transmission",bof);

	} catch (java.lang.Exception e) {
	    e.printStackTrace();

	} finally {
	    // Close down the source (or the server if started it).

	    try {
		if (source != null) {
		    source.CloseRBNBConnection();
		}
		displayElapsed("Total run",first);
	    } catch (java.lang.Exception e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Sets the number of cache frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cFramesI the number of cache frames.
     * @see #getCframes()
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/14/2001  INB	Created.
     *
     */
    public final void setCframes(int cFramesI) {
	cFrames = cFramesI;
    }

    /**
     * Sets the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nChansI the number of channels.
     * @see #getNchans()
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2001  INB	Created.
     *
     */
    public final void setNchans(int nChansI) {
	nChans = nChansI;
    }

    /**
     * Sets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nFramesI the number of frames.
     * @see #getNframes()
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    public final void setNframes(int nFramesI) {
	nFrames = nFramesI;
    }

    /**
     * Sets the number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI the number of points.
     * @see #getNpoints()
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/13/2001  INB	Created.
     *
     */
    public final void setNpoints(int nPointsI) {
	nPoints = nPointsI;
    }

    /**
     * Sets the address of the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address of the RBNB.
     * @see #getSaddress()
     * @since V2.0
     * @version 06/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2001  INB	Created.
     *
     */
    public final void setSaddress(String sAddressI) {
	sAddress = sAddressI;
    }
}
