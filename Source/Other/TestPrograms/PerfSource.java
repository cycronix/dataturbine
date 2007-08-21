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

import com.rbnb.api.DataBlock;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Source;
import com.rbnb.api.TimeRange;

/**
 * Performance test source using the regular RBNB API.
 * <p>
 *
 * @author Ian Brown
 *
 * @see PerfSource
 * @see com.rbnb.sapi
 * @since V2.0
 * @version 06/14/2001
 */

/*
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/12/2001  INB	Created.
 *
 */

public class PerfSource
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
     * the name of RBNB.
     * <p>
     * This field need only be set if the RBNB is to be started by this
     * program.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/13/2001
     */
    private String sName = "Server";

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
    public PerfSource() {
	super();
    }

    /**
     * Sets up and runs the performance test.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @since V2.0
     * @version 06/14/2001
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
	PerfSource perfSource = new PerfSource();

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
	    } else if (switchName == 'n') {
		perfSource.setSname(argsI[idx1].substring(idx2));
	    } else if (switchName == 'p') {
		perfSource.setNpoints
		    (Integer.parseInt(argsI[idx1].substring(idx2)));
	    }
	    idx = idx1 + 1;
	}

	perfSource.start();
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
     * Gets the name of the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name of the RBNB.
     * @see #setSname(String)
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
    public final String getSname() {
	return (sName);
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
     * @version 06/14/2001
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
	Server server = null;
	Source source = null;
	long first,
	     last = System.currentTimeMillis();
	first = last;

	try {
	    // Create the basic frame structure with an unnamed Rmap at the top
	    // and channel Rmaps below.
	    Rmap frame = new Rmap(null,null,new TimeRange(0.,1.)),
		 channels[] = new Rmap[getNchans()];
	    float[][] data = new float[getNchans()][];

	    for (int chanIdx = 0; chanIdx < getNchans(); ++chanIdx) {
		data[chanIdx] = new float[getNpoints()];
		DataBlock dBlock = new DataBlock
		    (data[chanIdx],
		     getNpoints(),
		     4,
		     DataBlock.TYPE_FLOAT32,
		     DataBlock.ORDER_MSB,
		     false,
		     0,
		     4);
		channels[chanIdx] = new Rmap("C" + chanIdx,dBlock,null);
		frame.addChild(channels[chanIdx]);
	    }

	    last = displayElapsed("Set up frame",last);

	    // Get a handle to the server. If it isn't running, try to launch
	    // it here.
	    server = Server.newServerHandle(getSname(),getSaddress());
	    if (!server.isRunning()) {
		server.start();
		started = true;
	    }

	    // Create the source connection.
	    source = server.createSource("PerfSource");
	    source.setCframes(getCframes());
	    source.start();

	    last = displayElapsed("Launch source",last);
	    long bof = last;

	    // Post the frames.
	    for (int frameIdx = 0; frameIdx < getNframes(); ++frameIdx) {
		/*
		for (int chanIdx = 0; chanIdx < getNchans(); ++chanIdx) {
		    for (int pointIdx = 0;
			 pointIdx < getNpoints();
			 ++pointIdx) {
			data[chanIdx][pointIdx] =
			    ((float) frameIdx)*getNchans()*getNpoints() +
			    ((float) chanIdx)*getNpoints() +
			    pointIdx;
		    }
		}
		last = displayElapsed("Frame " + frameIdx + " built",last);
		*/
		source.addChild(frame);
		last = displayElapsed("Frame " + frameIdx + " sent",last);
		frame.getTrange().set(frameIdx,1.);
	    }
	    displayElapsed("Frame transmission",bof);

	} catch (java.lang.Exception e) {
	    e.printStackTrace();

	} finally {
	    // Close down the source (or the server if started it).

	    try {
		if (started) {
		    server.stop();
		} else {
		    source.stop();
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
     * Sets the name of the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sNameI the name of the RBNB.
     * @see #getSname()
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
    public final void setSname(String sNameI) {
	sName = sNameI;
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
