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
 * An RBNB test application.
 * <p>
 * This class allows for the testing of a series of filters (using the
 * <code>TestFilter</code> class.  The first filter gets its input from a
 * <code>TestSource</code> object.  Successive filters take their input from
 * the previous filter in line.
 * <p>
 *
 * @author Ian Brown
 *
 * @see TestFilter
 * @see TestSource
 * @since V2.2
 * @version 11/13/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/30/2003  INB	Created.
 *
 */

public final class InALine
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
     * the index of the source channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private int channel;

    /**
     * the list of <code>TestFilters</code> being run.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private TestFilter[] filters = null;

    /**
     * the name of the output channel of the last <code>TestFilter</code>,
     * including the source name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private String outChan;

    /**
     * the number of filters to run.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private int nFilters;

    /**
     * the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private int nFrames;

    /**
     * the address of the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private String rbnbAddress;

    /**
     * the <code>TestSource</code> providing the input channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private TestSource src;

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
     * the timeout duration.
     * <p>
     * a value of -1 is no timeout.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/13/2003
     */
    private long timeout = 90000;

    /**
     * Builds an <code>InALine</code> from the input values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbAddressI the address the of the RBNB server.
     * @param srcI	   the <code>TestSource</code>.
     * @param channelI	   the source channel index.
     * @param nameI	   the name of the <code>InALine</code>.
     * @param nFiltersI	   the number of filters to run.
     * @param nFramesI	   the number of input frames to expect.
     * @param timeoutI	   the timeout in milliseconds.
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public InALine(String rbnbAddressI,
		   TestSource srcI,
		   int channelI,
		   String nameI,
		   int nFiltersI,
		   int nFramesI,
		   long timeoutI) {
	super(nameI);
	setRBNBAddress(rbnbAddressI);
	setSrc(srcI);
	setChannel(channelI);
	setNFilters(nFiltersI);
	setNFrames(nFramesI);
	setTimeout(timeoutI);
    }

    /**
     * Creates a <code>TestFilter</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idxI	  the identification of this filter.
     * @param sourceNameI the name of this filter.
     * @param chanI	  the name of the source channel.
     * @return the <code>TestFilter</code>.
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    private final TestFilter createFilter(int idxI,
					  String sourceNameI,
					  String chanI)
    {
	TestFilter filterR = new TestFilter();

	filterR.setRBNBAddress(rbnbAddress);
	filterR.setSourceName(getName() + "_F" + idxI);
	filterR.setMaximumFrames(getNFrames());
	filterR.setTimeout(getTimeout());
	String[] strChns = new String[1];
	strChns[0] = sourceNameI + "/" + chanI;
	filterR.setStreamChannels(strChns);

	return (filterR);
    }

    /**
     * Gets the source channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the source channel.
     * @see #setChannel(int channelI)
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final int getChannel() {
	return (channel);
    }

    /**
     * Gets the number of filters.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of filters.
     * @see #setNFilters(int nFiltersI)
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final int getNFilters() {
	return (nFilters);
    }

    /**
     * Gets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of frames.
     * @see #setNFrames(int nFramesI)
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final int getNFrames() {
	return (nFrames);
    }

    /**
     * Gets the name of the output channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the output channel.
     * @see #setOutChan(String outChanI)
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final String getOutChan() {
	return (outChan);
    }

    /**
     * Gets the address of the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address.
     * @see #setRBNBAddress(String rbnbAddressI)
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final String getRBNBAddress() {
	return (rbnbAddress);
    }

    /**
     * Gets the <code>TestSource</code> that provides the input channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>TestSource</code>.
     * @see #setSrc(TestSource srcI)
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final TestSource getSrc() {
	return (src);
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
     * Gets the timeout.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the timeout.
     * @see #setTimeout(long timeoutI)
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/13/2003  INB	Created.
     *
     */
    public final long getTimeout() {
	return (timeout);
    }

    /**
     * Initializes this <code>InALine</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.Exception if an error occurs.
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void init()
	throws java.lang.Exception
    {
	if (getNFilters() == 0) {
	    throw new IllegalArgumentException
		(getName() + " too few filters.");
	} else if (getNFrames() == 0) {
	    throw new IllegalArgumentException
		(getName() + " too few frames.");
	}
	String chan = "c" + getChannel();

	filters = new TestFilter[getNFilters()];
	filters[0] = createFilter(0,src.getSourceName(),chan);
	chan = src.getSourceName() + "_" + chan;

	for (int idx = 1; idx < getNFilters(); ++idx) {
	    filters[idx] = createFilter
		(idx,
		 filters[idx - 1].getSourceName(),
		 chan);
	    chan = filters[idx - 1].getSourceName() + "_" + chan;
	}

	setOutChan
	    (filters[getNFilters() - 1].getSourceName() + "/" + chan);
	if (debug) {
	    System.err.println(getName() + " output " + getOutChan());
	}
    }

    /**
     * Runs this <code>InALine</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void run() {
	try {
	    if (debug) {
		System.err.println(getName() + " starting filters.");
	    }
	    for (int idx = 0; idx < filters.length; ++idx) {
		filters[idx].debug = debug;
		filters[idx].start();
	    }

	    if (debug) {
		System.err.println(getName() + " waiting for filters.");
	    }
	    for (int idx = 0; idx < filters.length; ++idx) {
		filters[idx].join();
	    }

	    if (debug) {
		System.err.println(getName() + " results:");
	    }
	    setStatus(0);
	    for (int idx = 0; idx < filters.length; ++idx) {
		if (debug) {
		    System.err.println(filters[idx].getSourceName() + " " +
				       filters[idx].getStatus());
		}
		setStatus(Math.max(getStatus(),
				   filters[idx].getStatus()));
	    }

	} catch (Exception e) {
	    if (debug) {
		e.printStackTrace();
	    }
	    setStatus(1);
	}
    }

    /**
     * Sets the source channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelI the source channel.
     * @see #getChannel()
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void setChannel(int channelI) {
	channel = channelI;
    }

    /**
     * Sets the number of filters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nFiltersI the number of filters.
     * @see #getNFilters()
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void setNFilters(int nFiltersI) {
	nFilters = nFiltersI;
    }

    /**
     * Sets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nFramesI the number of frames.
     * @see #getNFrames()
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void setNFrames(int nFramesI) {
	nFrames = nFramesI;
    }

    /**
     * Sets the name of the output channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @param outChanI the name of the output channel.
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void setOutChan(String outChanI) {
	outChan = outChanI;
    }

    /**
     * Sets the address of the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbServerI the address of the RBNB server.
     * @see #getRBNBAddress()
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void setRBNBAddress(String rbnbAddressI) {
	rbnbAddress = rbnbAddressI;
    }

    /**
     * Sets the <code>TestSource</code> that provides the input channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @param srcI the <code>TestSource</code>.
     * @see #getSrc()
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void setSrc(TestSource srcI) {
	src = srcI;
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

    /**
     * Sets the timeout.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeoutI the timeout in milliseconds. -1 is infinite.
     * @see #getTimeout()
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/13/2003  INB	Created.
     *
     */
    public final void setTimeout(long timeoutI) {
	timeout = timeoutI;
    }

    /**
     * Terminates this <code>InALine</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/30/2003  INB	Created.
     *
     */
    public final void terminate() {
	for (int idx = 0; idx < filters.length; ++idx) {
	    filters[idx].close(false,false);
	}
    }
}
