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
 * This class allows for the joining of multiple <code>JoinInALine</code>
 * streams into a single output.
 * <p>
 * The code streams from oldest on the first <code>JoinInALine</code> and then
 * requests the same time range from each of the others using a
 * <code>RequestOptions</code> timeout.
 * <p>
 *
 * @author Ian Brown
 *
 * @see InALine
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
public final class Merger
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
     * the list of <code>JoinInALines</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private JoinInALines[] joiners;

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
     * the output channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private String outChan;

    /**
     * the output <code>ChannelMap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    ChannelMap out;

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
     * the <code>Sink</code> connection to use for doing time-based requests.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private Sink request;

    /**
     * the <code>ChannelMap</code> to request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    ChannelMap rIn;

    /**
     * the <code>ChannelMap</code> to stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private ChannelMap sIn;

    /**
     * the output <code>Source</code> connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private Source source;

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
     * the <code>Sink</code> connection to stream from.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/30/2003
     */
    private Sink stream;

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
     * Builds a <code>Merger</code> from the inputs.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbAddressI the RBNB server address.
     * @param nameI	   the name to use.
     * @param joinersI	   the list of <code>JoinInALines</code>.
     * @param nFramesI	   the number of frames to expect.
     * @param timeoutI	   the timeout in milliseconds.
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     *
     */
    public Merger(String rbnbAddressI,
		  String nameI,
		  JoinInALines[] joinersI,
		  int nFramesI,
		  long timeoutI)
    {
	super(nameI);
	setRBNBAddress(rbnbAddressI);
	setJoiners(joinersI);
	setNFrames(nFramesI);
	setTimeout(timeoutI);
    }

    /**
     * Gets the list of input <code>JoinInALines</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list.
     * @see #setJoiners(JoinInALines[] joinersI)
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
    public final JoinInALines[] getJoiners() {
	return (joiners);
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
     * Gets the output channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the output channel name.
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
     * Gets the RBNB address.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the RBNB address.
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
     * Initializes the <code>JoinInALines</code>.
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
	throws Exception
    {
	if ((getJoiners() == null) ||
	    (getJoiners().length == 0)) {
	    throw new IllegalArgumentException
		(getName() + " too few inputs.");
	} else if (getNFrames() == 0) {
	    throw new IllegalArgumentException
		(getName() + " too few frames.");
	}
	stream = new Sink();
	stream.OpenRBNBConnection(getRBNBAddress(),
				  getName() + "_Stream");
	sIn = new ChannelMap();
	sIn.Add(getJoiners()[0].getOutChan());

	if (getJoiners().length > 1) {
	    request = new Sink();
	    request.OpenRBNBConnection(getRBNBAddress(),
				       getName() + "_Request");
	    rIn = new ChannelMap();
	    for (int idx = 1; idx < getJoiners().length; ++idx) {
		rIn.Add(getJoiners()[idx].getOutChan());
	    }
	}

	source = new Source(10,"create",100);
	source.OpenRBNBConnection(getRBNBAddress(),getName());
	out = new ChannelMap();
	out.Add("m0");
	setOutChan(source.GetClientName() + "/m0");;
    }

    /**
     * Main method for testing.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @since V2.2
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/31/2003  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {
	    com.rbnb.utility.ArgHandler ah =
		new com.rbnb.utility.ArgHandler(argsI);
	    String value;
	    String lrbnbAddress = "localhost";
	    int lnLines = 2;
	    int lnFilters = 2;
	    int lnChannels = 3;
	    int lnFrames = 100;
	    boolean ldebug = false;
	    long ltimeout = 90000;

	    if ((value = ah.getOption('a')) != null) {
		lrbnbAddress = value;
	    }
	    if ((value = ah.getOption('l')) != null) {
		lnLines = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('f')) != null) {
		lnFilters = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('c')) != null) {
		lnChannels = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('F')) != null) {
		lnFrames = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('t')) != null) {
		ltimeout = Long.parseLong(value);
	    }
	    ldebug = ah.checkFlag('D');

	    TestSource[] sources = new TestSource[lnLines];
	    JoinInALines[] joinInALines = new JoinInALines[lnLines];
	    InALine[][] inALines = new InALine[lnLines][];
	    for (int idx = 0; idx < lnLines; ++idx) {
		sources[idx] = new TestSource();
		sources[idx].debug = ldebug;
		sources[idx].setRBNBAddress(lrbnbAddress);
		sources[idx].setSourceName("S_" + idx);
		sources[idx].setNumberOfChannels(lnChannels);
		sources[idx].setNumberOfFrames(lnFrames);
		sources[idx].start();
		sources[idx].join();
		sources[idx].close(false,true);

		inALines[idx] = new InALine[lnChannels];
		for (int idx1 = 0; idx1 < lnChannels; ++idx1) {
		    inALines[idx][idx1] = new InALine
			(lrbnbAddress,
			 sources[idx],
			 idx1,
			 "IAL_" + idx + "_" + idx1,
			 lnFilters,
			 lnFrames,
			 ltimeout);
		    inALines[idx][idx1].debug = ldebug;
		    inALines[idx][idx1].init();
		}

		joinInALines[idx] = new JoinInALines
		    (lrbnbAddress,
		     "J_" + idx,
		     inALines[idx],
		     lnFrames,
		     ltimeout);
		joinInALines[idx].debug = ldebug;
		joinInALines[idx].init();
	    }

	    Merger merger = new Merger(lrbnbAddress,
				       "M",
				       joinInALines,
				       lnFrames,
				       ltimeout);
	    merger.debug = ldebug;
	    merger.init();

	    Source[] tSources = new Source[lnLines];
	    for (int idx = 0; idx < lnLines; ++idx) {
		tSources[idx] = new Source(100,"load",lnFrames);
		tSources[idx].OpenRBNBConnection(lrbnbAddress,"S_" + idx);
	    }
	    Thread.currentThread().sleep(1000);
	    for (int idx = 0; idx < lnLines; ++idx) {
		for (int idx1 = 0; idx1 < lnChannels; ++idx1) {
		    inALines[idx][idx1].start();
		}
		joinInALines[idx].start();
	    }
	    merger.start();
	    merger.join();
	    merger.terminate();
	    for (int idx = 0; idx < lnLines; ++idx) {
		tSources[idx].CloseRBNBConnection(false,false);
		joinInALines[idx].terminate();
		for (int idx1 = 0; idx1 < lnChannels; ++idx1) {
		    inALines[idx][idx1].terminate();
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Runs this <code>Merger</code>.
     * <p>
     *
     * @author Ian Brown
     *
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
    public final void run() {
	setStatus(0);

	try {
	    ChannelMap sInt;
	    ChannelMap rInt;
	    RequestOptions reqOpt = new RequestOptions();

	    if (debug) {
		System.err.println(source.GetClientName() +
				   " subscribe: " + sIn);
	    }

	    if (getTimeout() != -1) {
		reqOpt.setMaxWait(90000);
	    }
	    stream.Subscribe(sIn,"oldest",.9999);
	    float[] outData = null;
	    float[][] inData = new float[getJoiners().length][];
	    double lastTime = 0;

	    for (int frame = 0; frame < getNFrames(); ++frame) {
		sInt = stream.Fetch(getTimeout());
		if (sInt.GetIfFetchTimedOut()) {
		    try {
			throw new Exception
			    (source.GetClientName() +
			     " abort on stream timeout.");
		    } catch (Exception e) {
			if (debug) {
			    e.printStackTrace();
			}
		    }
		    setStatus(2);
		    break;
		}
		if ((frame > 0) && (sInt.GetTimeStart(0) > lastTime + .1)) {
		    try {
			throw new Exception(source.GetClientName() +
					    " jump in streamed time from " +
					    lastTime + " to " +
					    sInt.GetTimeStart(0));
		    } catch (Exception e) {
			if (debug) {
			    e.printStackTrace();
			}
		    }
		    setStatus(3);
		    break;
		}
		lastTime = sInt.GetTimeStart(0) + sInt.GetTimeDuration(0);
		inData[0] = sInt.GetDataAsFloat32(0);

		if (getJoiners().length > 1) {
		    if (debug) {
			System.err.println(source.GetClientName() +
					   " request: " + rIn +
					   " at " + sInt.GetTimeStart(0) +
					   " for " + sInt.GetTimeDuration(0));
		    }
		    if (getTimeout() == -1) {
			request.Request(rIn,
					sInt.GetTimeStart(0),
					sInt.GetTimeDuration(0),
					"absolute");
		    } else {
			request.Request(rIn,
					sInt.GetTimeStart(0),
					sInt.GetTimeDuration(0),
					"absolute",
					reqOpt);
		    }
		    rInt = request.Fetch(-1);
		    if (rInt.NumberOfChannels() !=
			rIn.NumberOfChannels()) {
			try {
			    throw new Exception
				(source.GetClientName() +
				 " abort on request timeout.");
			} catch (Exception e) {
			    if (debug) {
				e.printStackTrace();
			    }
			}
			setStatus(3);
			break;
		    }
		    for (int idx = 0;
			 idx < rInt.NumberOfChannels();
			 ++idx) {
			inData[idx + 1] = rInt.GetDataAsFloat32(idx);
		    }
		}

		outData = new float[inData[0].length];
		for (int idx = 0; idx < inData[0].length; ++idx) {
		    outData[idx] = inData[0][idx];
		    for (int idx1 = 1; idx1 < inData.length; ++idx1) {
			outData[idx] += inData[idx1][idx];
		    }
		    outData[idx] /= inData.length;
		}
		out.PutTimeRef(sInt,0);
		out.PutDataAsFloat32(0,outData);
		source.Flush(out,true);
		if (debug) {
		    System.err.println(source.GetClientName() +
				       " frame " + sInt.GetTimes(0)[0]);
		}
	    }

	} catch (Exception e) {
	    if (debug) {
		e.printStackTrace();
	    }
	    setStatus(1);

	} finally {
	    stream.CloseRBNBConnection();
	    if (joiners.length > 1) {
		request.CloseRBNBConnection();
	    }
	}

	if (debug) {
	    System.err.println(getName() + " report: " + status);
	}
    }

    /**
     * Sets the list of <code>JoinInALines</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param joinersI the list.
     * @see #getJoiners()
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
    public final void setJoiners(JoinInALines[] joinersI) {
	joiners = joinersI;
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
     * Terminates this <code>JoinInALine</code>.
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
	source.CloseRBNBConnection(false,false);
    }
}
