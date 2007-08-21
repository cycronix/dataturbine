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

import com.rbnb.utility.ToString;

import com.rbnb.api.DataBlock;
import com.rbnb.api.DataRequest;
import com.rbnb.api.EndOfStream;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Sink;
import com.rbnb.api.Source;
import com.rbnb.api.DataArray;
import com.rbnb.api.TimeRange;

import java.util.Date;
import java.util.Vector;

/**
 * Simple sink application using the RBNB API.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/31/2002
 */

/*
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/04/2001  INB	Created.
 *
 */
public class SimpleSink extends Thread {
    /**
     * disconnect after run?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private boolean disconnectAfterRun = true;

    /**
     * use "..." syntax?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/03/2001
     */
    private boolean useDotDotDot = true;

    /**
     * run in frame mode?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/01/2001
     */
    private boolean frameMode = false;

    /**
     * are we running?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    private boolean running = false;

    /**
     * stream?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/21/2001
     */
    private boolean stream = false;

    /**
     * run in timing mode?
     * <p>
     * When a <code>SimpleSink</code> is run in timing mode, it times how
     * long various operations take, including the total of the
     * addChild()/getChildAt() calls. The number of frames is always limited
     * (default is 1000) and there is no sleep time between frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private boolean timing = false;

    /**
     * archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/27/2001
     */
    private byte aMode = Source.ACCESS_NONE;

    /**
     * time to sleep between frames in milliseconds.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private int sleepTime = 1000;

    /**
     * the number of frames to retrieve.
     * <p>
     * A value of -1 is treated as infinite.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private long numFrames = -1;

    /**
     * the <code>Server</code> that this sink application is attached to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/04/2001
     */
    private Server server = null;

    /**
     * the <code>Sink</code> object that this sink application uses to
     * communicate with the actual <code>Sink</code> object in the RBNB
     * DataTurbine <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/04/2001
     */
    private Sink sink = null;

    /**
     * the <code>Source</code> that this sink application is to get data from.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/04/2001
     */
    private Source source = null;

    /**
     * the name of the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/04/2001
     */
    private String name = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #SimpleSink(com.rbnb.api.Server,String,com.rbnb.api.Source)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public SimpleSink() {
	super();
    }

    /**
     * Class constructor to build a <code>SimpleSink</code> attached to the
     * specified <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the name of the <code>Source</code>.
     * @param serverI  the <code>Server</code>.
     * @param sourceI  the <code>Source</code> of data.
     * @see #SimpleSink()
     * @since V2.0
     * @version 01/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public SimpleSink(String nameI,Server serverI,Source sourceI) {
	this();
	setSinkName(nameI);
	setServer(serverI);
	setSource(sourceI);
    }

    /**
     * Creates the first request <code>Rmap</code> from the
     * <code>Source</code>.
     * <p>
     * This first implementation assumes that the registration map is available
     * (i.e., that we're working with an internal server). This should be
     * replaced with a method to get the registration list for a source in the
     * future.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @return the request <code>Rmap</code>.
     * @since V2.0
     * @version 04/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    private final synchronized DataRequest createFirstRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Create the request. The source list is built by converting the
	// registration list to a name hierarchy. We also have to set the name
	// of the source.
	DataRequest requestR = new DataRequest
			(null,
			 null,
			 null,
			 DataRequest.ABSOLUTE,
			 getFmode() ? DataRequest.ALL : DataRequest.EXISTING,
			 (getStream() ?
			  ((getNframes() == -1) ?
			   DataRequest.INFINITE :
			   getNframes()) :
			  1),
			 1.,
			 false,
			 (getFmode() ?
			  DataRequest.FRAMES :
			  DataRequest.CONSOLIDATED)),
		    serverL = new DataRequest
			(getServer().getName(),
			 getFmode() ? null : new TimeRange(0.,1.),
			 getFmode() ? new TimeRange(1.,0.) : null);
	Rmap sourceL = null,
	     regMap = new Rmap(getSource().getParent().getName()),
	     regSrc = new Rmap(getSource().getName());
	regMap.addChild(regSrc);
	regSrc.addChild(new Rmap("..."));
	while (sourceL == null) {
	    Rmap registrationMap = getSink().getRegistered(regMap);

	    if (registrationMap.getNchildren() > 0) {
		sourceL = registrationMap.getChildAt(0).toNameHierarchy();
		if (sourceL.getNchildren() == 0) {
		    sourceL = null;
		    sleep(getStime());
		}

	    } else {
		sleep(getStime());
	    }
	}

	sourceL.setName(getSource().getName());
	requestR.addChild(serverL);
	serverL.addChild(sourceL);
	return (requestR);
    }

    /**
     * Disconnect from the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not running.
     * @exception java.lang.InterruptedException
     *		  thrown if the terminate is interrupted.
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void disconnect()
	throws java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException
    {
	if (getSink() != null) {
	    if (getSink().isRunning()) {
		getSink().stop();
		setSink(null);
	    }
	}
    }

    /**
     * Displays a response <code>Rmap</code>.
     * <p>
     * This first implementation uses a simple tabulated scheme.
     * <p>
     *
     * @author Ian Brown
     *
     * @param responseI  the response <code>Rmap</code>.
     * @param namesI     the names of the channels.
     * @see com.rbnb.api.Rmap#extract(String)
     * @see com.rbnb.api.DataArray
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    private final synchronized void displayResponse(Rmap responseI,
						    String[] namesI)
	throws InterruptedException,
	       Exception
    {
	synchronized (System.out) {
	    if (!getTiming()) {
		System.out.println("");
		System.out.println("Data from: " + getSink().getName());
		System.out.println(responseI);
		System.out.println("Channel names:");
		for (int cIdx = 0; cIdx < namesI.length; ++cIdx) {
		    System.out.println((cIdx + 1) + ". " + namesI[cIdx]);
		}
		System.out.println("");
		System.out.print("Time        " + ToString.toString("%8i",1));
		for (int cIdx = 1; cIdx < namesI.length; ++cIdx) {
		    System.out.print("    " +
				     ToString.toString("%8i",cIdx + 1));
		}
		System.out.println("");
	    }

	    // Get time and data information for all of the channels.
	    DataArray[] td = new DataArray[namesI.length];
	    int[] idx = new int[namesI.length];

	    for (int cIdx = 0; cIdx < namesI.length; ++cIdx) {
		// Loop through the channels and get <code>DataArray</code>
		// blocks for each of them.
		td[cIdx] = responseI.extract(namesI[cIdx]);
		if (td[cIdx] == null) {
		    return;
		}
		idx[cIdx] = 0;
	    }

	    if (!getTiming()) {
		int cDone = 0;
		do {
		    // Print the data in time order, showing all of the
		    // channels that have data at each time value on the same
		    // line.
		    double minTime = Double.MAX_VALUE;
		    int nMatched = 0;
		    int[] matching = new int[namesI.length];

		    cDone = 0;
		    for (int cIdx = 0; cIdx < namesI.length; ++cIdx) {
			// Loop through the channels and find the minimum time
			// value not yet printed.
			int tIdx = idx[cIdx];
			double[] times = td[cIdx].getTime();

			if (tIdx == times.length) {
			    // If the channel is done, count it.
			    ++cDone;

			} else {
			    // If the channel has more data, update the minimum
			    // time found.
			    double time = times[tIdx];
			    time = Math.round(time*10000.)/10000.;

			    if (time == minTime) {
				matching[nMatched++] = cIdx;
			    } else if (time < minTime) {
				minTime = time;
				matching[0] = cIdx;
				nMatched = 1;
			    }
			}
		    }

		    if (nMatched > 0) {
			// If there were any channels matched, print out a line
			// of time and data.
			System.out.print(ToString.toString("%10.4f",minTime) +
					 "  ");

			int lIdx = 0;
			for (int nIdx = 0; nIdx < nMatched; ++nIdx) {
			    // Loop through the channels and print out the data
			    // for any that have an unprinted value at the
			    // current minimum time.
			    int cIdx = matching[nIdx];
			    int tIdx = idx[cIdx]++;
			    float[] data = null;

			    if (td[cIdx].getData() == null) {
				double[] times = td[cIdx].getTime();
				data = new float[times.length];
			    } else {
				data = (float[]) td[cIdx].getData();
			    }
			    
			    for (int idx1 = lIdx + 1; idx1 <= cIdx; ++idx1) {
				System.out.print("            ");
			    }
			    System.out.print
				(ToString.toString("%8.4f",
						   data[tIdx]) + "    ");
			    lIdx = cIdx + 1;
			}
			System.out.println("");
		    }
		} while (cDone < namesI.length);
	    }
	}
    }

    /**
     * Finds the names of the channels in the input request <code>Rmap</code>.
     * <p>
     * The assumption made here is that there are no group entries in the
     * request, which should be true if it was built from the current
     * implementation of the registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @return the array of names.
     * @since V2.0
     * @version 01/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    private final synchronized String[] findNames(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// This method could be recursive, but I've chosen to implement it
	// in-line, so we use a vector of "levels". We build a vector of names.
	Vector levels = new Vector(),
	       names = new Vector();

	// Add the first entry to the vector. Each "entry" consists of three
	// elements:
	// <p><ol>
	// <li>the level's <code>Rmap</code>,</li>
	// <li>the fully qualified name to the level's <code>Rmap</code>,
	//     and</li>
	// <li>the index of the last chlid processed.</li>
	// </ol><p>
	levels.addElement(requestI);
	levels.addElement(null);
	levels.addElement(new Integer(-1));

	while (levels.size() > 0) {
	    // Loop until the levels vector is emptied.

	    // Get the elements of the current (last) entry in the vector.
	    Rmap parent = (Rmap) levels.elementAt(levels.size() - 3);
	    String name = (String) levels.elementAt(levels.size() - 2);
	    Integer lastIdx = (Integer) levels.elementAt(levels.size() - 1);

	    // Update the index to that of the next child.
	    int idx = lastIdx.intValue() + 1;

	    if (idx == parent.getNchildren()) {
		// If there is no child, remove the entry from the levels
		// vector.
		levels.removeElementAt(levels.size() - 1);
		levels.removeElementAt(levels.size() - 1);
		levels.removeElementAt(levels.size() - 1);

	    } else {
		// Otherwise, move down a level.
		Rmap child = parent.getChildAt(idx);

		if (child.getName() != null) {
		    // If the child has a name, add it in.

		    if (name == null) {
			name = child.getName();
		    } else {
			name = name + Rmap.PATHDELIMITER + child.getName();
		    }
		}
		
		// Update the current entry's last index.
		levels.setElementAt(new Integer(idx),levels.size() - 1);

		if (child.getNchildren() == 0) {
		    // If the child has no children, then it is an entry we
		    // want.
		    names.addElement(name);

		} else {
		    // If the child has children, make an entry in the levels
		    // vector for it. This becomes the current entry next
		    // time.
		    levels.addElement(child);
		    levels.addElement(name);
		    levels.addElement(new Integer(-1));
		}
	    }
	}

	// Build the array of names.
	String[] namesR = new String[names.size()];

	for (int idx = 0; idx < namesR.length; ++idx) {
	    namesR[idx] = (String) names.elementAt(idx);
	}

	return (namesR);
    }

    /**
     * Gets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive access mode.
     * @see #setAmode(byte)
     * @since V2.0
     * @version 02/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2001  INB	Created.
     *
     */
    public final synchronized byte getAmode() {
	return (aMode);
    }

    /**
     * Gets the disconnect flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return disconnect after run?
     * @see #setDisconnect(boolean)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized boolean getDisconnect() {
	return (disconnectAfterRun);
    }

    /**
     * Gets the frame mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return request data in frames?
     * @see #setFmode(boolean)
     * @since V2.0
     * @version 04/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2001  INB	Created.
     *
     */
    public final synchronized boolean getFmode() {
	return (frameMode);
    }

    /**
     * Gets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of frames.
     * @see #setNframes(long)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized long getNframes() {
	return (numFrames);
    }

    /**
     * Gets the running flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we running?
     * @see #setRunning(boolean)
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    private final synchronized boolean getRunning() {
	return (running);
    }

    /**
     * Gets the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Server</code>.
     * @see #setServer(com.rbnb.api.Server)
     * @since V2.0
     * @version 01/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized Server getServer() {
	return (server);
    }

    /**
     * Gets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Sink</code>.
     * @see #setSink(com.rbnb.api.Sink)
     * @since V2.0
     * @version 01/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized Sink getSink() {
	return (sink);
    }

    /**
     * Gets the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setName(String)
     * @since V2.0
     * @version 01/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized String getSinkName() {
	return (name);
    }

    /**
     * Gets the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Source</code>.
     * @see #setSource(com.rbnb.api.Source)
     * @since V2.0
     * @version 01/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized Source getSource() {
	return (source);
    }

    /**
     * Gets the sleep time between frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time between frames in milliseconds.
     * @see #setStime(int)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized int getStime() {
	return (sleepTime);
    }

    /**
     * Gets the streaming mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return should the retrieval be done by streaming (repetitions)?
     * @see #setStream(boolean)
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    public final synchronized boolean getStream() {
	return (stream);
    }

    /**
     * Gets the timing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this run being timed?
     * @see #setTiming(boolean)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized boolean getTiming() {
	return (timing);
    }

    /**
     * Is this <code>SimpleSink</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we running?
     * @exception java.lang.InterruptedException
     *		  thrown if the check is interrupted.
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public final synchronized boolean isRunning()
	throws java.lang.InterruptedException
    {
	while (isAlive() && !getRunning()) {
	    wait(1000);
	}

	return (isAlive() && getRunning());
    }

    /**
     * Runs the sink.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/31/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final void run() {
	boolean error = false;
	long frame = 0,
	     retries = 0;

	try {
	    Date last = new Date(),
		 now;
	    boolean needUpdate = false;

	    setSink(getServer().createSink(getSinkName()));
	    //	    setSink(getServer().createRAMSink(getSinkName()));
	    if (getTiming()) {
		now = new Date();
		System.err.println(getSinkName() +
				   " created in " +
				   (now.getTime() - last.getTime())/1000.);
		last = now;
	    }

	    // Start the <code>Sink</code>.
	    getSink().setCframes(1);
	    getSink().start();

	    // We're now running.
	    setRunning(true);
	    
	    if (getTiming()) {
		now = new Date();
		System.err.println(getSinkName() +
				   " started in " +
				   (now.getTime() - last.getTime())/1000.);
		last = now;
	    }

	    // Create the first request.
	    DataRequest request = createFirstRequest();
	    String[] names = null;

	    if (!useDotDotDot) {
		names = findNames(request);
	    } else {
		Rmap lSource;

		for (lSource = request;
		     lSource.compareNames((Rmap) getSource()) != 0;
		     lSource = lSource.getChildAt(0)) {}
		while (lSource.getNchildren() > 0) {
		    lSource.removeChildAt(0);
		}
		Rmap child = new Rmap("...");
		lSource.addChild(child);
	    }

	    if (getTiming()) {
		now = new Date();
		System.err.println(getSinkName() +
				   " created first request in " +
				   (now.getTime() - last.getTime())/1000.);
		last = now;

	    } else {
		// Give things a little bit of time to set up.
		sleep(getStime());
	    }

	    boolean gotSomething = false;
	    long lastT = System.currentTimeMillis();
	    for (frame = 0;
		 (//getStream() ||
		  (getNframes() == -1) ||
		  (frame < getNframes()));
		 ++frame) {
		// Repeatedly make requests. Start by updating the request.
		if (needUpdate || (gotSomething && !getStream())) {
		    updateRequest(request);
		}

		gotSomething = (getSource().getAmode() == Source.ACCESS_LOAD);

		// Make the request of the sink.
		if (needUpdate || (frame == 0) || !getStream()) {
		    getSink().addChild(request);
		    getSink().initiateRequestAt(0);
		    needUpdate = false;
		}
		Rmap response = null;

		while ((response = getSink().fetch
			(getStime())) == null) {
		}

		EndOfStream e = null;
		if (response instanceof EndOfStream) {
		    e = (EndOfStream) response;
		    if (e.getReason() == e.REASON_BOD) {
			if (!getStream() && (++retries < 10)) {
			    sleep(getStime()/2);
			    System.err.println("Off end of data.");
			    continue;
			}
			break;
		    } else if (e.getReason() == e.REASON_EOD) {
			if (retries == 0) {
			    System.err.println("Off beginning of data.");
			}
			++retries;
		    } else if ((e.getReason() == e.REASON_NODATA) ||
			       (e.getReason() == e.REASON_NONAME)) {
			if (retries == 0) {
			    System.err.println("No data found.");
			}
			++retries;
		    }
		    needUpdate = true;
		}
		retries = 0;

		if ((response != null) && (response.getNchildren() > 0)) {
		    // If we got something, display it.
		    if (useDotDotDot) {
			Rmap parent = new Rmap();
			parent.addChild(response);
			names = parent.extractNames();
			parent.removeChild(response);
		    }
		    displayResponse(response,names);

		    if (!getTiming()) {
			// Wait a little while.
			if (!getStream() &&
			    (getSource().getAmode() != Source.ACCESS_LOAD)) {
			    long nowT = System.currentTimeMillis(),
				duration = nowT - lastT;
			    if (getStime()/2 > duration) {
				sleep(getStime()/2 - duration);
			    }
			    lastT = nowT;
			}
		    }

		    gotSomething = true;
		}

		if (getStream() && (response instanceof EndOfStream)) {
		    break;
		}
	    }
	    if (getTiming()) {
		now = new Date();
		System.err.println(getSinkName() +
				   " got " +
				   getNframes() +
				   " frames in  " +
				   (now.getTime() - last.getTime())/1000.);
		last = now;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("After " + frame + " frames.");
	    error = true;
	}

	try {
	    if (error || getDisconnect()) {
		// Disconnect if there was an error or if we're supposed to.
		disconnect();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// We're no longer running.
	setRunning(false);
    }

    /**
     * Sets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param aModeI  the archive access mode.
     * @see #getAmode()
     * @since V2.0
     * @version 02/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2001  INB	Created.
     *
     */
    public final synchronized void setAmode(byte aModeI) {
	aMode = aModeI;
    }

    /**
     * Sets the disconnect flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param disconnectAfterRunI  disconnect after running?
     * @see #getDisconnect()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setDisconnect(boolean disconnectAfterRunI) {
	disconnectAfterRun = disconnectAfterRunI;
    }

    /**
     * Sets the frame mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameModeI  makes requests for frames?
     * @see #getFmode()
     * @since V2.0
     * @version 04/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2001  INB	Created.
     *
     */
    public final synchronized void setFmode(boolean frameModeI) {
	frameMode = frameModeI;
    }

    /**
     * Sets the number of frames.
     * <p>
     * A value of -1 means that the number of frames is not limited.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numFramesI  the number of frames.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of frames <= 0 and is not -1.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSink</code> is running.
     * @see #getNframes()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setNframes(long numFramesI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the number of frames once the sink is " +
		 "running.");
	} else if ((!getTiming() && (numFramesI != -1)) &&
		   (numFramesI <= 0)) {
	    throw new java.lang.IllegalArgumentException
		("The number of frames must be positive.");
	}

	numFrames = numFramesI;
    }

    /**
     * Sets the running flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param runningI  are we running?
     * @see #getRunning()
     * @see #isRunning()
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    private final synchronized void setRunning(boolean runningI) {
	running = runningI;
	notifyAll();
    }

    /**
     * Sets the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>Server</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSink</code> is running.
     * @see #getServer()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized void setServer(Server serverI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the server once the sink is " +
		 "running.");
	}

	server = serverI;
    }

    /**
     * Sets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI  the <code>Sink</code>.
     * @see #getSink()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized void setSink(Sink sinkI) {
	sink = sinkI;
    }

    /**
     * Sets the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSink</code> is running.
     * @see #getSinkName()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized void setSinkName(String nameI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the sink name once the sink is " +
		 "running.");
	}

	name = nameI;
    }

    /**
     * Sets the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceI  the <code>Source</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSink</code> is running.
     * @see #getSource()
     * @since V2.0
     * @version 01/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    public final synchronized void setSource(Source sourceI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the server once the sink is " +
		 "running.");
	}

	source = sourceI;
    }

    /**
     * Sets the sleep time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sleepTimeI  the sleep time in milliseconds.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the sleep time <= 0.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSink</code> is running.
     * @see #getStime()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setStime(int sleepTimeI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the sleep time once the sink is " +
		 "running.");
	} else if (sleepTimeI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The sleep time must be positive.");
	}

	sleepTime = sleepTimeI;
    }

    /**
     * Sets the streaming mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param streamI  should the retrieval be done by streaming (repetitions)?
     * @see #getStream()
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    public final synchronized void setStream(boolean streamI) {
	stream = streamI;
    }

    /**
     * Sets the timing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timingI  should the run be timed?
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSink</code> is running.
     * @see #getTiming()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setTiming(boolean timingI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the timing flag once the sink is " +
		 "running.");
	}

	timing = timingI;
	if (getNframes() == -1) {
	    setNframes(1000);
	}
    }

    /**
     * Updates the request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestIO  the request <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    private final synchronized void updateRequest(DataRequest requestIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Update the request <code>TimeRange</code>.
	if (getFmode()) {
	    requestIO.getChildAt(0).setFrange
		(new TimeRange
		    (requestIO.getChildAt(0).getFrange().getTime() + 1.,
		     requestIO.getChildAt(0).getFrange().getDuration()));
	} else {
	    requestIO.getChildAt(0).setTrange
		(new TimeRange
		    (requestIO.getChildAt(0).getTrange().getTime() + 1.,
		     requestIO.getChildAt(0).getTrange().getDuration()));
	}
    }

    /**
     * Main method for running a <code>SimpleSink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI  the command line arguments:
     *		     <p><ul>
     *		     <li>-a serverAddress</li>
     *		     <li>-f numberOfFrames</li>
     *		     <li>-n serverName</li>
     *		     <li>-s sourceName</li>
     *		     <li>-t runInTimingMode</li>
     *		     </ul>
     * @since V2.0
     * @version 04/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	Server server = null;
	boolean started = false,
	        timing = false;
	int sourcesSinks = 1,
	    numChans = 1,
	    sleepTime = 100;
	long numFrames = -1;
	String serverName = "Server",
	    serverAddress = "internal://",
	    sourceName = "TestSource";
	SimpleSink sink = null;

	try {
	    for (int idx = 0; idx < argsI.length;) {
		if (argsI[idx].equals("-n")) {
		    serverName = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-a")) {
		    serverAddress = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-s")) {
		    sourceName = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-f")) {
		    numFrames = Long.parseLong(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-t")) {
		    timing = true;
		    ++idx;
		}
	    }

	    server = com.rbnb.api.Server.newServerHandle
		(serverName,
		 serverAddress);

	    if (!server.isRunning()) {
		throw new com.rbnb.api.AddressException("Unable to connect to " + server);
	    }

	    Source source = server.createSource(sourceName);
	    sink = new SimpleSink("TestSink",server,source);
	    sink.setStime(sleepTime);
	    sink.setTiming(timing);
	    sink.start();

	    do {
		Thread.currentThread().sleep(1000);
	    } while (sink.isRunning());

	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    if (started) {
		if ((server != null) && server.isRunning()) {
		    server.stop();
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
