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

package com.rbnb.proxy;

/**
 * RBNB proxy source application.
 * <p>
 * Reads byte stream off server port, fills into RBNB frames, sends to server.
 * <p>
 *
 * @author Eric Friets
 * @author Ian Brown
 *
 * @since V2.1
 * @version 06/29/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/29/2004  INB	Replaced deprecated <code>Source.CloseRBNBConnection(boolean,boolean)</code> 
 *			method with <code>Source.CloseRBNBConnection</code> or
 *			<code>Source.Detach</code>.
 * 03/10/2004  INB	Ensure that multiple connections are closed
 *			even if they haven't been written.
 * 02/19/2004  INB	Use the start time of the current entry rather
 *			than last time as the end time of the file.
 * 02/09/2004  INB	Added retry connection logic.
 * 01/16/2004  INB	Added -X debug flag.
 * 01/14/2004  INB	Added single thread mode operation.
 * 01/09/2004  INB	Modified "N" connection logic to use new ClearCache
 *			capability to avoid an extra close and reload.  This
 *			has been replaced with a clear and detach.
 * 01/06/2004  INB	Check the name of the source created to ensure
 *			that it hasn't been changed by the server.  Added code
 *			to hold on to no more than "N" connections at a time.
 * 12/22/2003  INB	Added shutdown hook.
 * 12/02/2003  INB	Added various new capabilities:
 *				-C <chan>[,<chan>[,...]]
 *					Specifies the channel names. Not
 *					compatible with -n.
 *				-M	Specifies that multiple sources are
 *					to be created.
 *				-T	Specifies that time-stamps preceed each
 *					frame.
 *				-O <time cutoff>
 *					Specifies that archives are to be
 *					closed at regular invervals.
 *					If the cutoff is 600, then archives are
 *					closed at times ending in 0 minutes
 *					(i.e., the hour, ten minutes after, 20
 *					minutes after, etc.).
 * 11/21/2003  INB	Moved into the V2 directory hierarchy
 * 03/26/2003  EMF	Created.
 *
 */
import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.ByteConvert;
import com.rbnb.utility.RBNBProcess;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public final class Proxy
    implements java.lang.Runnable
{
    /**
     * the number of frames in the archive.
     * <p>
     * Zero (0) means no archive is created.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private int archiveFrames = 0;

    /**
     * the duration of one block of data in seconds.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private double blockDuration = 1;

    /**
     * the number of points per channel per block.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private int blockSize = 1000;

    /**
     * the number of frames in the archive.
     * <p>
     * This must be >= 1 and <= archive size if the latter is non-zero.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private int cacheFrames = 1000;

    /**
     * the names of the channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private Vector channelNames = null;

    /**
     * the root of channel names (if not explicitly set).
     * <p>
     * Channels will be numbered, starting from 0.
     * <p>
     *
     * @author Ian Brown
     *
     * @see
     * @since
     * @version
     */
    private String chanName = "c";

    /**
     * number of attempts to make to connect to the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 02/09/2004
     */
    final static int CONNECTION_RETRIES = 3;

    /**
     * the time interval for cutting off the archive.
     * <p>
     * Specifies that archives are to be closed at regular invervals.  If the
     * cutoff is 600, then archives are closed at times ending in 0 minutes
     * (i.e., the hour, ten minutes after, 20 minutes after, etc.).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private double cutoff = 0.;

    /**
     * display debug print?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/16/2004
     */
    private boolean debug = true;

    /**
     * the data type.
     * <p>
     * i8, i16, i32, i64, f32, or f64.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private String dataType = "i16";

    /**
     * do the <code>run()</code> method?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private boolean doRun = true;

    /**
     * the size of an RBNB frame in blocks.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private int frameSize = 1;

    /**
     * are there input time-stamps?
     * <p>
     * If this is set, each block of data will be preceeded by a time-stamp in
     * Java time format (long integer milliseconds since 1970).  The cutoffs
     * are done against this time-stamp.
     * <p>
     *n
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private boolean inputTimestamps = false;

    /**
     * is the input data most-significant byte first?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version
     */
    private boolean msbOrder = true;

    /**
     * use multiple sources (one per channel)?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private boolean multipleSources = false;

    /**
     * number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private int numChan = 1;

    /**
     * number of connections to maintain.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 01/06/2004
     */
    private int numConnections = 1;

    /**
     * the server socket port to listen on.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private int port = 3000;

    /**
     * the list of <code>ProxySources</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/03/2003
     */
    private ProxySource[] proxySources = null;

    /**
     * the RBNB server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private String rbnbServer = "localhost:3333";

    /**
     * the list of <code>ServerSockets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/03/2003
     */
    private static Hashtable serverSockets = new Hashtable();

    /**
     * shutdown?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/22/2003
     */
    private boolean shutdown = false;

    /**
     * single thread mode?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/14/2004
     */
    private boolean singleThreadMode = true;

    /**
     * the name of the source or the prefix for multiple sources or with cutoff
     * times.
     * <p>
     * If there are multiple sources, they will be named sourceName-
     * channelName.
     * <p>
     * If the cutoff time is set, then the sources will be named by appending
     * "-#".
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/03/2003
     */
    private String sourceName = "Proxy";

    /**
     * the RBNB data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/03/2003
     */
    private int type = ChannelMap.TYPE_UNKNOWN;

    /**
     * Builds a <code>Proxy</code> from command line arguments.
     * <p>
     *
     * @author Ian Brown
     *
     * @param arg the command line arguments.
     * @since V2.1
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Added debug flag.
     * 01/14/2004  INB	Added single thread mode flag.
     * 01/06/2004  INB	Added number of connections flag.
     * 12/03/2003  INB	Added new command line arguments and reworked to allow
     *			for multiple <code>Proxies</code> to run
     *			simultaneously.
     * 03/26/2003  EMF	Created.
     *
     */
    public Proxy(String[] arg) {
	//parse command line options
	try {
	    ArgHandler ah=new ArgHandler(arg);

	    if (ah.checkFlag('a')) {
		rbnbServer = ah.getOption('a');
		if (rbnbServer == null) {
		    throw new Exception
			("Must specify rbnb server with -a");
		}
	    }

	    if (ah.checkFlag('b')) {
		blockDuration = Double.parseDouble(ah.getOption('b'));
		if (blockDuration < 0) {
		    throw new Exception("Block duration must be positive");
		}
	    }

	    if (ah.checkFlag('c')) {
		cacheFrames = Integer.parseInt(ah.getOption('c'));
		if (cacheFrames < 1) {
		    throw new Exception("Cache frames must be positive");
		}
	    }

	    if (ah.checkFlag('C')) {
		String value = ah.getOption('C');
		StringTokenizer stok = new StringTokenizer(value,",");
		channelNames = new Vector();
		while (stok.hasMoreTokens()) {
		    channelNames.addElement(stok.nextToken());
		}
		numChan = channelNames.size();
	    }

	    if (ah.checkFlag('d')) {
		archiveFrames = Integer.parseInt(ah.getOption('d'));
		if (archiveFrames < 0) {
		    throw new Exception
			("Disk archive frames must be nonnegative");
		}
	    }


	    if (ah.checkFlag('f')) {
		frameSize = Integer.parseInt(ah.getOption('f'));
		if (frameSize < 1) {
		    throw new Exception("Frame size must be positive");
		}
	    }

	    if (ah.checkFlag('h')) {
		throw new Exception("");
	    }

	    if (ah.checkFlag('M')) {
		multipleSources = true;
	    }

	    if (ah.checkFlag('n')) {
		int nChanL = Integer.parseInt(ah.getOption('n'));
		if ((channelNames != null) && (nChanL != numChan)) {
		    throw new Exception
			("Inconsistent number of channels.");
		}
		numChan = nChanL;
		if (numChan < 1) {
		    throw new Exception
			("Number of channels must be positive");
		}
	    }

	    if (ah.checkFlag('N')) {
		numConnections = Integer.parseInt(ah.getOption('N'));
		if (numConnections < 0) {
		    throw new Exception
			("Number of connections must be positive or zero.");
		}
	    }

	    if (ah.checkFlag('O')) {
		cutoff = Double.parseDouble(ah.getOption('O'));
		if (cutoff < 0.) {
		    throw new Exception
			("Cutoff must be 0 (none) or positive");

		} else if (cutoff > 0.) {
		    long cutoffMillis = (long) (cutoff*1000.);
		    cutoff = cutoffMillis/1000.;
		    if ((cutoffMillis % 1000) != 0) {
			throw new Exception
			    ("Cannot handle millisecond cutoffs.");
		    } else if (cutoffMillis < 24*60*60*1000) {
			if ((24*60*60*1000 % cutoffMillis) != 0) {
			    throw new Exception
				("Cannot handle cutoff less than a day that " +
				 "is not an integer divisor of 24 hours.");
			}
		    } else {
			throw new Exception
			    ("Cannot handle cutoff times greater than a day.");
		    }

		    if (numConnections == 1) {
			numConnections = 10;
		    }
		}

	    } else if (numConnections != 1) {
		numConnections = 1;
	    }

	    if (ah.checkFlag('p')) {
		blockSize = Integer.parseInt(ah.getOption('p'));
		if (blockSize < 1) {
		    throw new Exception("Block size must be positive");
		}
	    }

	    if (ah.checkFlag('s')) {
		sourceName = ah.getOption('s');
		if (sourceName == null) {
		    throw new Exception
			("Must specify source name with -s");
		}
	    }

	    if (ah.checkFlag('S')) {
		singleThreadMode = !singleThreadMode;
	    }

	    if (ah.checkFlag('t')) {
		dataType = ah.getOption('t');
		if (!(dataType.equals("i8") ||
		      dataType.equals("i16") ||
		      dataType.equals("i32") ||
		      dataType.equals("i64") ||
		      dataType.equals("f32") ||
		      dataType.equals("f64"))) {
		    throw new Exception
			("Data type must be one of i8, i16, i32, " +
			 "i64, f32, f64");
		}
	    }

	    if (ah.checkFlag('T')) {
		inputTimestamps = true;
	    }

	    if (ah.checkFlag('w')) {
		String word = ah.getOption('w');
		if (word.equals("MSB")) {
		    msbOrder = true;
		} else if (word.equals("LSB")) {
		    msbOrder = false;
		} else {
		    throw new Exception("Word order must be MSB or LSB");
		}
	    }

	    if (ah.checkFlag('x')) {
		port = Integer.parseInt(ah.getOption('x'));
	    }

	    if (ah.checkFlag('X')) {
		debug = false;
	    }

	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.err.println("Usage:  java Proxy");
	    System.err.println("\t-h\tprint this usage guide");
	    System.err.println("\t-x\t<proxy port>\tdefault 3000");
	    System.err.println
		("\t-a\t<rbnb server host:port>\tdefault localhost:3333");
	    System.err.println("\t-s\t<rbnb source name>\tdefault Proxy");
	    System.err.println("\t-M\t\tcreates per-channel sources");
	    System.err.println("\t-S\t\tsingle threading mode");
	    System.err.println("\t-N\t<to keep>\tnumber of RBOs to keep");
	    System.err.println("\t-c\t<cache frames>\tdefault 1000");
	    System.err.println("\t-d\t<disk archive frames>\tdefault 0");
	    System.err.println("");
	    System.err.println("\t-n\t<number of channels>\tdefault 1");
	    System.err.println("\t-C\t<chan1>[,chan2[,....]]\tdefault -n");
	    System.err.println
		("\t-t\t<datatype (i8,i16,i32,i64,f32,f64)>\tdefault i16");
	    System.err.println("\t-w\t<word order (MSB,LSB)>\tdefault MSB");
	    System.err.println("\t-b\t<block duration in seconds>\tdefault 1");
	    System.err.println("\t-p\t<block size in points>\tdefault 1000");
	    System.err.println("\t-f\t<frame size in blocks>\tdefault 1");
	    System.err.println("\t-O\t<file cutoff seconds>\tdegault 0");
	    System.err.println("\t-T\ttime-stamps preceed data blocks");
	    doRun = false;
	    RBNBProcess.exit(0);
	}

	// Display the flag values (DEBUG).
	if (debug) {
	    System.err.println("Proxy:");
	    System.err.println("   -a " + rbnbServer);
	    System.err.println("   -b " + blockDuration);
	    System.err.println("   -c " + cacheFrames);
	    System.err.println("   -C " + channelNames);
	    System.err.println("   -d " + archiveFrames);
	    System.err.println("   -f " + frameSize);
	    System.err.println("   -M " + multipleSources);
	    System.err.println("   -n " + numChan);
	    System.err.println("   -N " + numConnections);
	    System.err.println("   -O " + cutoff);
	    System.err.println("   -p " + blockSize);
	    System.err.println("   -s " + sourceName);
	    System.err.println("   -S " + singleThreadMode);
	    System.err.println("   -t " + dataType);
	    System.err.println("   -T " + inputTimestamps);
	    System.err.println("   -w " + msbOrder);
	    System.err.println("   -x " + port);
	}

	// Ensure that there is no existing proxy and connect to the server.
	setup();
    }

    /**
     * Creates and runs a proxy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param arg the command line arguments.
     * @since V2.1
     * @version 12/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/06/2003  EMF	Created.
     *
     */
    public static void main(String[] arg) {
	Proxy proxy=new Proxy(arg);
	new Thread(proxy).start();
    }

    /**
     * Runs the <code>Proxy</code>.
     * <p>
     *
     * @author Eric Friets
     * @author Ian Brown
     *
     * @since V2.1
     * @version 12/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2003  INB	Reworked to handle <code>ProxySources</code>.
     * 03/26/2003  EMF	Created.
     *
     */
    public void run() {
	if (!doRun) {
	    return;
	}
	ShutdownThread sdThread = new ShutdownThread();
	sdThread.primaryThread = Thread.currentThread();
	try {
	    Runtime.getRuntime().addShutdownHook(sdThread);
	} catch (java.lang.NoSuchMethodError e) {
	}

	Socket socket = null;
	InputStream is = null;
	
	// Create server socket, listen for connection, get input stream.
	boolean kill = false;
	ServerSocket ss;
	synchronized (serverSockets) {
	    if (serverSockets.get(new Integer(port)) == null) {
		try {
		    ss = new ServerSocket(port);
		    serverSockets.put(new Integer(port),ss);
		    socket = ss.accept();
		    is = socket.getInputStream();
		    ss.close();
		    ss = null;

		} catch (Exception e) {
		    System.err.println();
		    System.err.println
			("Proxy: exception accepting connection.");
		    System.err.println();
		    e.printStackTrace();
		    ss = null;
		    kill = true;
		    for (int idx = 0; idx < proxySources.length; ++idx) {
			proxySources[idx].abort(kill);
		    }
		    return;

		} finally {
		    serverSockets.remove(new Integer(port));
		    serverSockets.notifyAll();
		}
	    } else {
		System.err.println("Proxy: failed to get server port");
		return;
	    }
	}
	
	//set up arrays and types
	int numBytes = blockSize;
	if (dataType.equalsIgnoreCase("i8")) {
	    type = ChannelMap.TYPE_INT8;
	    numBytes *= 1;
	} else if (dataType.equalsIgnoreCase("i16")) {
	    type = ChannelMap.TYPE_INT16;
	    numBytes *= 2;
	} else if (dataType.equalsIgnoreCase("i32")) {
	    type = ChannelMap.TYPE_INT32;
	    numBytes *= 4;
	} else if (dataType.equalsIgnoreCase("i64")) {
	    type = ChannelMap.TYPE_INT64;
	    numBytes *= 8;
	} else if (dataType.equalsIgnoreCase("f32")) {
	    type = ChannelMap.TYPE_FLOAT32;
	    numBytes *= 4;
	} else if (dataType.equalsIgnoreCase("f64")) {
	    type = ChannelMap.TYPE_FLOAT64;
	    numBytes *= 8;
	}

	byte[][] data = new byte[numChan][frameSize*numBytes];
	BufferedInputStream bis = new BufferedInputStream(is,32768);
	DataInputStream dis = new DataInputStream(bis);

	double startAt = -Double.MAX_VALUE;
	double inputTime = 0.;
	double time = 0.;
	int offset;
	long frame = 0;
	byte[] tBytes = new byte[8];
	double[] timeV;
	long tBytesRead = 0L;
	boolean wasInterrupted = false;

	/*
	long readElapsed = 0L;
	long writeElapsed = 0L;
	*/
	double elDur;
	long beginAt = 0L;
	long endAt;

	try {
	    while (!shutdown) {
		if (debug) {
		    beginAt = System.currentTimeMillis();
		}

		if (inputTimestamps) {
		    if (msbOrder) {
			inputTime = dis.readDouble();
		    } else {
			dis.readFully(tBytes);
			timeV = ByteConvert.byte2Double(tBytes,true);
			inputTime = timeV[0];
		    }
		}

		for (int f = 0; f < frameSize; ++f) {
		    offset = f*numBytes;
		    for (int i = 0; i < numChan; ++i) {
			dis.readFully(data[i],offset,numBytes);
			tBytesRead += numBytes;
		    }
		}

		if (startAt == -Double.MAX_VALUE) {
		    if (inputTimestamps) {
			startAt = inputTime;
		    } else {
			startAt = System.currentTimeMillis()/1000.;
		    }
		}

		if (!inputTimestamps) {
		    inputTime = startAt + time;
		}
		time = (frame++)*blockDuration;

		if (debug) {
		    endAt = System.currentTimeMillis();
		    //		    readElapsed += (endAt - beginAt);
		    elDur = (endAt - beginAt)/1000.;
		    if (elDur > 50.*blockDuration) {
			System.err.println("Read took " + elDur + " seconds.");
		    }
		    beginAt = endAt;
		}

		if (!multipleSources) {
		    proxySources[0].putData(startAt + time,
						       inputTime,data);
		} else {
		    for (int idx = 0; idx < numChan; ++idx) {
			proxySources[idx].putData(startAt + time,
						  inputTime,
						  data[idx]);
		    }
		}

		if (debug) {
		    endAt = System.currentTimeMillis();
		    //		    writeElapsed += (endAt - beginAt);
		    elDur = (endAt - beginAt)/1000.;
		    if (elDur > 50.*blockDuration) {
			System.err.println("Write took " + elDur +
					   " seconds.");
		    }
		}

	        /*
		if ((frame % 10) == 0) {
		    long now = System.currentTimeMillis();
		    long elapsed = now - ((long) (startAt*1000.));
		    System.err.println
			("\n" +
			 sourceName + " @" + (new Date(now)) +
			 " elapsed: " + elapsed/1000. + "\n" +
			 "Read elapsed: " + readElapsed/1000. +
			 " Write Elapsed: " + writeElapsed/1000. + "\n" +
			 "Time: " + (startAt + time) + " " +
			 (new java.util.Date
			     ((long) ((startAt + time)*1000.))) +
			 "\nInputTime: " + inputTime + " " +
			 (new java.util.Date((long) (inputTime*1000.))) +
			 "\nFrame: " + frame + " channels: " + numChan +
			 " bytes: " + numBytes + " actual: " + tBytesRead +
			 " expected: " + (frame*numChan*numBytes));
		    readElapsed = 0L;
		    writeElapsed = 0L;
		}
		*/
	    }

	} catch (java.lang.InterruptedException e) {
	    wasInterrupted = true;

	} catch (java.lang.Exception e) {
	    wasInterrupted = true;
	    System.err.println("Proxy " + sourceName +
			       ": error handling data.  Aborting.");
	    e.printStackTrace();

	} finally {
	    while (!wasInterrupted && !Thread.currentThread().interrupted()) {
		try {
		    Thread.currentThread().sleep(1000);
		} catch (java.lang.InterruptedException e) {
		    wasInterrupted = true;
		}
	    }
	    try {
		dis.close();
	    } catch (Exception e) {
	    }
	    try {
		socket.close();
	    } catch (Exception e) {
	    }

	    time += blockDuration;
	    inputTime += blockDuration;
	    for (int idx = 0; idx < proxySources.length; ++idx) {
		if (cutoff != 0.) {
		    try {
			proxySources[idx].putTime(startAt + time,
						  inputTime,
						  true);
		    } catch (java.lang.Exception e) {
		    }
		}
		proxySources[idx].abort(kill);
	    }

	    sdThread.primaryThread = null;
	    shutdown = false;
	    synchronized (Thread.currentThread()) {
		Thread.currentThread().notifyAll();
	    }
	}
    }

    /**
     * Sets up the <code>Proxy</code>.
     * <p>
     * Ensures that there is no running <code>Proxy</code> on the
     * <code>ServerSocket</code> and connects to the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added single thread mode handling.
     * 12/03/2003  INB	Created.
     *
     */
    private final void setup() {
	ServerSocket ss;
	synchronized (serverSockets) {

	    // Force any existing instance using the same port to close down.
	    if ((ss = (ServerSocket) serverSockets.get
		 (new Integer(port))) != null) {
		try {
		    ss.close();
		} catch (Exception e) {
		    System.err.println
			("Proxy: exception closing server socket.");
		    e.printStackTrace();
		    doRun = false;
		    return;
		}
	    }

	    // Wait for the other to go away.
	    while ((ss = (ServerSocket) serverSockets.get
		    (new Integer(port))) != null) {
		// Wait for the other instance to go away.
		System.err.println
		    ("Proxy: waiting for other instance to clean up");
		try {
		    serverSockets.wait(50);
		} catch (Exception e) {
		}
	    }
	}

	// Create the source connections to the server.
	if (!multipleSources) {
	    try {
		proxySources = new ProxySource[1];
		if (channelNames == null) {
		    proxySources[0] = new ProxySource(numChan,chanName);
		} else {
		    proxySources[0] = new ProxySource(channelNames);
		}
		if (!singleThreadMode) {
		    proxySources[0].start();
		}

	    } catch (Exception e) {
		System.err.println("Proxy: exception creating source");
		e.printStackTrace();
		synchronized (serverSockets) {
		    try {
			ss.close();
		    } catch (Exception e1) {
		    }
		    serverSockets.remove(new Integer(port));
		}
		doRun = false;
	    }

	} else {
	    proxySources = new ProxySource[numChan];
	    int idx = 0;
	    try {
		for (idx = 0; idx < numChan; ++idx) {
		    if (channelNames == null) {
			proxySources[idx] = new ProxySource(numChan,
							    chanName,
							    idx);
		    } else {
			proxySources[idx] = new ProxySource
			    ((String) channelNames.elementAt(idx));
		    }

		    if (!singleThreadMode) {
			proxySources[idx].start();
		    }
		}
	    } catch (Exception e) {
		System.err.println("Proxy: exception creating source");
		e.printStackTrace();
		synchronized (serverSockets) {
		    try {
			ss.close();
		    } catch (Exception e1) {
		    }
		    serverSockets.remove(new Integer(port));
		}
		for (int idx1 = 0; idx1 < idx; ++idx1) {
		    proxySources[idx1].abort(true);
		}
		doRun = false;
	    }
	}
    }

    /**
     * RBNB source connection handler for <code>Proxy</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 03/10/2004
     */

    /*
     * Copyright 2003, 2004 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2004  INB	Ensure that multiple connections are closed
     *			even if they haven't been written.
     * 02/19/2004  INB	Use the start time of the current entry rather
     *			than last time as the end time of the file.
     * 02/09/2004  INB	Added retry connection logic.
     * 01/14/2004  INB	Added single thread mode handling.
     * 01/06/2004  INB	Added handling of number of connections limit.
     * 12/03/2003  INB	Created.
     *
     */
    private final class ProxySource
	extends java.lang.Thread
    {
	/**
	 * the active source index.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private int activeSource = 0;

	/**
	 * a local <code>Calendar</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private Calendar calendar = Calendar.getInstance();

	/**
	 * the <code>ChannelMap</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private ChannelMap channelMap = null;

	/**
	 * run the cutoff close/open thread?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private boolean doRun = false;

	/**
	 * do the source swap?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private boolean doSwap = false;

	/**
	 * the last input time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/05/2003
	 */
	private double lastITime = 0.;

	/**
	 * the last actual time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/05/2003
	 */
	private double lastTime = 0.;

	/**
	 * next cutoff time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private double nextCutoff = -Double.MAX_VALUE;

	/**
	 * the base source name.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private String sourceBase = null;

	/**
	 * the <code>Source</code> connections to handle.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private Source[] sources = null;

	/**
	 * the number of sources created.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/03/2003
	 */
	private long sourcesCreated = 2;

	/**
	 * the cutoff start input time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/05/2003
	 */
	private double startITime = 0.;

	/**
	 * the cutoff start actual time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/05/2003
	 */
	private double startTime = 0.;

	/**
	 * the time <code>ChannelMap</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/22/2003
	 */
	private ChannelMap timeChannelMap = null;

	/**
	 * Builds a <code>ProxySource</code> for a number of channels and
	 * a channel prefix.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param numChansI the number of channels.
	 * @param prefixI   the channel name prefix.
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 02/09/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/09/2004  INB	Added retry connection logic.
	 * 01/06/2004  INB	Check the name of the source created to ensure
	 *			that it hasn't been changed by the server.
	 * 12/03/2003  INB	Created.
	 *
	 */
	public ProxySource(int numChansI,String prefixI)
	    throws java.lang.Exception
	{
	    super();

	    channelMap = new ChannelMap();
	    for (int idx = 0; idx < numChansI; ++idx) {
		channelMap.Add(prefixI + idx);
	    }
	    if (cutoff != 0.) {
		timeChannelMap = new ChannelMap();
		timeChannelMap.Add("time");
	    }
	    
	    sourceBase = sourceName;
	    if (cutoff == 0.) {
		sources = new Source[1];
		sources[0] = new Source
		    (cacheFrames,
		     (archiveFrames == 0) ? "none" : "create",
		     archiveFrames);
		for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
		    try {
			sources[0].OpenRBNBConnection(rbnbServer,sourceBase);
			break;
		    } catch (java.lang.Exception e) {
			if (retry == (CONNECTION_RETRIES - 1)) {
			    throw e;
			} else if (debug) {
			    sources[0].CloseRBNBConnection();
			    System.err.println
				("Retrying " + sourceBase +
				 " connection after exception.");
			}
		    }
		}
		if (!sources[0].GetClientName().equals(sourceBase)) {
		    throw new com.rbnb.api.AddressException
			("Did not get correct name " + sourceBase + ".");
		}

	    } else {
		String name;
		sources = new Source[2];
		for (int idx = 0; idx < 2; ++idx) {
		    name = sourceBase + "-" + idx;
		    sources[idx] = new Source
			(cacheFrames,
			 (archiveFrames == 0) ? "none" : "create",
			 archiveFrames);
		    for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
			try {
			    sources[idx].OpenRBNBConnection(rbnbServer,name);
			    break;
			} catch (java.lang.Exception e) {
			    if (retry == (CONNECTION_RETRIES - 1)) {
				throw e;
			    } else if (debug) {
				sources[idx].CloseRBNBConnection();
				System.err.println
				    ("Retrying " + name +
				     " connection after exception.");
			    }
			}
		    }
		    if (!sources[idx].GetClientName().equals(name)) {
			throw new com.rbnb.api.AddressException
			    ("Did not get correct name " + name + ".");
		    }
		}

		doRun = true;
	    }
	}

	/**
	 * Builds a <code>ProxySource</code> for list of channels.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param channelsI the list of channels.
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 02/09/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/09/2004  INB	Added retry connection logic.
	 * 01/06/2004  INB	Check the name of the source created to ensure
	 *			that it hasn't been changed by the server.
	 * 12/03/2003  INB	Created.
	 *
	 */
	public ProxySource(Vector channelsI)
	    throws java.lang.Exception
	{
	    super();

	    channelMap = new ChannelMap();
	    for (int idx = 0; idx < channelNames.size(); ++idx) {
		channelMap.Add((String) channelNames.elementAt(idx));
	    }
	    if (cutoff != 0.) {
		timeChannelMap = new ChannelMap();
		timeChannelMap.Add("time");
	    }
	    
	    sourceBase = sourceName;
	    if (cutoff == 0.) {
		sources = new Source[1];
		sources[0] = new Source
		    (cacheFrames,
		     (archiveFrames == 0) ? "none" : "create",
		     archiveFrames);
		for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
		    try {
			sources[0].OpenRBNBConnection(rbnbServer,sourceBase);
			break;
		    } catch (java.lang.Exception e) {
			if (retry == (CONNECTION_RETRIES - 1)) {
			    throw e;
			} else if (debug) {
			    sources[0].CloseRBNBConnection();
			    System.err.println
				("Retrying " + sourceBase +
				 " connection after exception.");
			}
		    }
		}
		if (!sources[0].GetClientName().equals(sourceBase)) {
		    throw new com.rbnb.api.AddressException
			("Did not get correct name " + sourceBase + ".");
		}

	    } else {
		String name;
		sources = new Source[2];
		for (int idx = 0; idx < 2; ++idx) {
		    sources[idx] = new Source
			(cacheFrames,
			 (archiveFrames == 0) ? "none" : "create",
			 archiveFrames);
		    name = sourceBase + "-" + idx;
		    for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
			try {
			    sources[idx].OpenRBNBConnection(rbnbServer,name);
			    break;
			} catch (java.lang.Exception e) {
			    if (retry == (CONNECTION_RETRIES - 1)) {
				throw e;
			    } else if (debug) {
				sources[idx].CloseRBNBConnection();
				System.err.println
				    ("Retrying " + name +
				     " connection after exception.");
			    }
			}
		    }
		    if (!sources[idx].GetClientName().equals(name)) {
			throw new com.rbnb.api.AddressException
			    ("Did not get correct name " + name + ".");
		    }
		}
		doRun = true;
	    }
	}

	/**
	 * Builds a <code>ProxySource</code> for one channel of some specified
	 * number of channels with a specified prefix.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param numChansI the number of channels.
	 * @param prefixI   the channel name prefix.
	 * @param idxI	    the channel index.
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 02/09/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/09/2004  INB	Added retry connection logic.
	 * 01/06/2004  INB	Check the name of the source created to ensure
	 *			that it hasn't been changed by the server.
	 * 12/03/2003  INB	Created.
	 *
	 */
	public ProxySource(int numChansI,String prefixI,int idxI)
	    throws java.lang.Exception
	{
	    super();
	    channelMap = new ChannelMap();
	    channelMap.Add(prefixI + idxI);
	    if (cutoff != 0.) {
		timeChannelMap = new ChannelMap();
		timeChannelMap.Add("time");
	    }

	    sourceBase = sourceName + "." + channelMap.GetName(0);
	    if (cutoff == 0.) {
		sources = new Source[1];
		sources[0] = new Source
		    (cacheFrames,
		     (archiveFrames == 0) ? "none" : "create",
		     archiveFrames);
		for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
		    try {
			sources[0].OpenRBNBConnection(rbnbServer,sourceBase);
			break;
		    } catch (java.lang.Exception e) {
			if (retry == (CONNECTION_RETRIES - 1)) {
			    throw e;
			} else if (debug) {
			    sources[0].CloseRBNBConnection();
			    System.err.println
				("Retrying " + sourceBase +
				 " connection after exception.");
			}
		    }
		}
		if (!sources[0].GetClientName().equals(sourceBase)) {
		    throw new com.rbnb.api.AddressException
			("Did not get correct name " + sourceBase + ".");
		}

	    } else {
		String name;
		sources = new Source[2];
		for (int idx = 0; idx < 2; ++idx) {
		    sources[idx] = new Source
			(cacheFrames,
			 (archiveFrames == 0) ? "none" : "create",
			 archiveFrames);
		    name = sourceBase + "-" + idx;
		    for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
			try {
			    sources[idx].OpenRBNBConnection(rbnbServer,name);
			    break;
			} catch (java.lang.Exception e) {
			    if (retry == (CONNECTION_RETRIES - 1)) {
				throw e;
			    } else if (debug) {
				sources[idx].CloseRBNBConnection();
				System.err.println
				    ("Retrying " + name +
				     " connection after exception.");
			    }
			}
		    }
		    if (!sources[idx].GetClientName().equals(name)) {
			throw new com.rbnb.api.AddressException
			    ("Did not get correct name " + name + ".");
		    }
		}
		doRun = true;
	    }
	}

	/**
	 * Builds a <code>ProxySource</code> for a channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param channelI the channel.
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 02/09/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/09/2004  INB	Added retry connection logic.
	 * 01/06/2004  INB	Check the name of the source created to ensure
	 *			that it hasn't been changed by the server.
	 * 12/03/2003  INB	Created.
	 *
	 */
         public ProxySource(String channelI)
	    throws java.lang.Exception
	{
	    super();
	    channelMap = new ChannelMap();
	    channelMap.Add(channelI);
	    if (cutoff != 0.) {
		timeChannelMap = new ChannelMap();
		timeChannelMap.Add("time");
	    }
	    sourceBase = sourceName + "." + channelI;

	    if (cutoff == 0.) {
		sources = new Source[1];
		sources[0] = new Source
		    (cacheFrames,
		     (archiveFrames == 0) ? "none" : "create",
		     archiveFrames);
		for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
		    try {
			sources[0].OpenRBNBConnection(rbnbServer,sourceBase);
			break;
		    } catch (java.lang.Exception e) {
			if (retry == (CONNECTION_RETRIES - 1)) {
			    throw e;
			} else if (debug) {
			    sources[0].CloseRBNBConnection();
			    System.err.println
				("Retrying " + sourceBase +
				 " connection after exception.");
			}
		    }
		}
		if (!sources[0].GetClientName().equals(sourceBase)) {
		    throw new com.rbnb.api.AddressException
			("Did not get correct name " + sourceBase + ".");
		}

	    } else {
		String name;
		sources = new Source[2];
		for (int idx = 0; idx < 2; ++idx) {
		    sources[idx] = new Source
			(cacheFrames,
			 (archiveFrames == 0) ? "none" : "create",
			 archiveFrames);
		    name = sourceBase + "-" + idx;
		    for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
			try {
			    sources[idx].OpenRBNBConnection(rbnbServer,name);
			    break;
			} catch (java.lang.Exception e) {
			    if (retry == (CONNECTION_RETRIES - 1)) {
				throw e;
			    } else if (debug) {
				sources[idx].CloseRBNBConnection();
				System.err.println
				    ("Retrying " + name +
				     " connection after exception.");
			    }
			}
		    }
		    if (!sources[idx].GetClientName().equals(name)) {
			throw new com.rbnb.api.AddressException
			    ("Did not get correct name " + name + ".");
		    }
		}
		doRun = true;
	    }
	}

	/**
	 * Aborts this <code>ProxySource</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param killI kill this connection?
	 * @since V2.2
	 * @version 06/29/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 06/29/2004  INB	Replaced deprecated <code>Source.CloseRBNBConnection(boolean,boolean)</code> 
	 *			method with
	 *			<code>Source.CloseRBNBConnection</code> or
	 *			<code>Source.Detach</code>.
	 * 03/10/2004  INB	Ensure that multiple connections are closed
	 *			even if they haven't been written.
	 * 01/14/2004  INB	Added single thread mode handling.
	 * 01/09/2004  INB	Use the new clear cache and detach capability
	 *			of the server to leave data in memory.
	 * 01/06/2004  INB	Close only the active two connections.
	 * 12/03/2003  INB	Created.
	 *
	 */
	public final void abort(boolean killI) {
	    synchronized (this) {
		doRun = false;
		notifyAll();
		if (!singleThreadMode) {
		    interrupt();
		}
	    }

	    if (numConnections == 1) {
		if (sources.length == 1) {
		    if (!killI) {
			sources[0].Detach();
		    } else {
			sources[0].CloseRBNBConnection();
		    }
		} else {
		    for (int idx = 0; idx < sources.length; ++idx) {
			if (!killI) {
			    sources[0].Detach();
			} else {
			    sources[0].CloseRBNBConnection();
			}
		    }
		}

	    } else {
		String name = sourceBase + "-" + (sourcesCreated - 2);
		for (int idx = 0; idx < 2; ++idx) {
		    if (sources[idx] != null) {
			if (sources[idx].GetClientName().equals(name) &&
			    (numConnections >= 2) &&
			    !killI &&
			    (archiveFrames != 0)) {
			    // Leave the last archive loaded by clearing its
			    // cache and detaching.
			    try {
				sources[idx].ClearCache();
				sources[idx].Detach();
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			} else {
			    // Close all other connections.
			    if (!killI) {
				sources[idx].Detach();
			    } else {
				sources[idx].CloseRBNBConnection();
			    }
			}
		    }
		}
	    }
	}

	/**
	 * Computes the next cutoff.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param timeI the current time.
	 * @since V2.2
	 * @version 12/03/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/03/2003  INB	Created.
	 *
	 */
	private final void computeCutoff(double timeI) {
	    long currentTime = (long) ((timeI + blockDuration)*1000.);
	    long cutoffDuration = (long) (cutoff*1000.);
	    calendar.setTime(new Date(currentTime));

	    currentTime -= (currentTime % 1000);
	    calendar.setTime(new Date(currentTime));
	    calendar.set(Calendar.MILLISECOND,0);
	    calendar.set(calendar.SECOND,0);
	    calendar.set(calendar.MINUTE,0);
	    calendar.set(calendar.HOUR_OF_DAY,0);
	    long startOfDay = calendar.getTime().getTime();
	    long difference = currentTime - startOfDay;
	    difference =
		difference - (difference % cutoffDuration) + cutoffDuration;

	    nextCutoff = (startOfDay + difference)/1000.;

	    if (System.getProperty("PROXYDEBUG") != null) {
		System.err.println
		    (sourceName + " block end: " +
		     (new Date((long) ((timeI + blockDuration)*1000.))) +
		     " cutoff: " +
		     (new Date((long) (nextCutoff*1000.))));
	    }
	}

	/**
	 * Performs the swap of the active and inactive connections.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @exception java.lang.Exception
	 *	      if an error occurs.
	 * @since V2.2
	 * @version 06/29/2004
	 */


	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 06/29/2004  INB	Replaced deprecated <code>Source.CloseRBNBConnection(boolean,boolean)</code> 
	 *			method with
	 *			<code>Source.CloseRBNBConnection</code> or
	 *			<code>Source.Detach</code>.
	 * 02/09/2004  INB	Added retry connection logic.
	 * 01/14/2004  INB	Created from code extracted from
	 *			<code>run</code>.
	 *
	 */
	private final void performSwap()
	    throws java.lang.Exception
	{
	    long beginAt = 0L;
	    long now;
	    double duration;
	    Source loadSource;
	    int nOpen = (activeSource + 1) % 2;

	    if (debug) {
		beginAt = System.currentTimeMillis();
	    }

	    if ((archiveFrames == 0) || (numConnections < 2)) {
		sources[nOpen].Detach();
		sources[nOpen] = null;
		if (debug) {
		    now = System.currentTimeMillis();
		    duration = (now - beginAt)/1000.;
		    if (duration > 50.*blockDuration) {
			System.err.println("Close took " + duration +
					   " seconds.");
		    }
		}
	    } else {
		sources[nOpen].ClearCache();
		sources[nOpen].Detach();
		if (debug) {
		    now = System.currentTimeMillis();
		    duration = (now - beginAt)/1000.;
		    if (duration > 50.*blockDuration) {
			System.err.println("Clear/detach took " + duration +
					   " seconds.");
		    }
		    beginAt = now;
		}

		if (sourcesCreated - numConnections >= 0) {
		    // Force the oldest one closed.
		    loadSource = new Source(cacheFrames,
					    "append",
					    archiveFrames);
		    loadSource.OpenRBNBConnection
			(rbnbServer,
			 sourceBase + "-" +
			 (sourcesCreated - numConnections));
		    loadSource.Detach();
		    if (debug) {
			now = System.currentTimeMillis();
			duration = (now - beginAt)/1000.;
			if (duration > 50.*blockDuration) {
			    System.err.println("Close took " + duration +
					       " seconds.");
			}
			beginAt = now;
		    }
		}
	    }

	    sources[nOpen] = new Source(cacheFrames,
					"create",
					archiveFrames);
	    String name = sourceBase + "-" + (sourcesCreated++);
	    for (int retry = 0; retry < CONNECTION_RETRIES; ++retry) {
		try {
		    sources[nOpen].OpenRBNBConnection(rbnbServer,name);
		    break;
		} catch (java.lang.Exception e) {
		    if (retry == (CONNECTION_RETRIES - 1)) {
			throw e;
		    } else if (debug) {
			sources[nOpen].CloseRBNBConnection();
			System.err.println
			    ("Retrying " + name +
			     " connection after exception.");
		    }
		}
	    }
	    if (!sources[nOpen].GetClientName().equals(name)) {
		throw new com.rbnb.api.AddressException
		    ("Did not get correct name " + name + ".");
	    }

	    if (debug) {
		now = System.currentTimeMillis();
		duration = (now - beginAt)/1000.;
		if (duration > 50.*blockDuration) {
		    System.err.println("Open took " + duration +
				       " seconds.");
		}
	    }
	}

	/**
	 * Adds a frame of data for all of the channels.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param timeI  the time-stamp.
	 * @param iTimeI the input source time-stamp.
	 * @param dataI  the data array.
	 * @return did cutoff occur?
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 12/22/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/03/2003  INB	Created.
	 *
	 */
	public final boolean putData(double timeI,double iTimeI,byte[][] dataI)
	    throws java.lang.Exception
	{
	    boolean cutoffR = false;

	    putTime(timeI,iTimeI,false);

	    channelMap.PutTime(timeI,blockDuration);
	    for (int idx = 0; idx < dataI.length; ++idx) {
		channelMap.PutData
		    (idx,
		     dataI[idx],
		     type,
		     (msbOrder ? ChannelMap.MSB : ChannelMap.LSB));
	    }

	    if (cutoff == 0.) {
		sources[0].Flush(channelMap,false);

	    } else if (iTimeI + blockDuration <= nextCutoff) {
		waitForActive();
		sources[activeSource].Flush(channelMap,false);

	    } else {
		computeCutoff(iTimeI);

		swapActive();
		sources[activeSource].Flush(channelMap,false);
		cutoffR = true;
	    }

	    lastTime = timeI;
	    lastITime = iTimeI;

	    return (cutoffR);
	}

	/**
	 * Adds a frame of data for a single channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param timeI  the time-stamp.
	 * @param iTimeI the input source time-stamp.
	 * @param dataI  the data array.
	 * @return did cutoff occur?
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 12/22/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/03/2003  INB	Created.
	 *
	 */
	public final boolean putData(double timeI,double iTimeI,byte[] dataI)
	    throws java.lang.Exception
	{
	    boolean cutoffR = false;

	    putTime(timeI,iTimeI,false);

	    channelMap.PutTime(timeI,blockDuration);
	    channelMap.PutData
		(0,
		 dataI,
		 type,
		 (msbOrder ? ChannelMap.MSB : ChannelMap.LSB));

	    if (cutoff == 0.) {
		sources[0].Flush(channelMap,false);

	    } else if (iTimeI + blockDuration <= nextCutoff) {
		waitForActive();
		sources[activeSource].Flush(channelMap,false);

	    } else {
		computeCutoff(iTimeI);

		swapActive();
		sources[activeSource].Flush(channelMap,false);
		cutoffR = true;
	    }

	    lastTime = timeI;
	    lastITime = iTimeI;

	    return (cutoffR);
	}

	/**
	 * Puts time information out.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param timeI  the time-stamp.
	 * @param iTimeI the input source time-stamp.
	 * @param forceI force cutoff?
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 02/19/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/19/2004  INB	Use the start time of the current entry rather
	 *			than last time as the end time of the file.
	 * 12/05/2003  INB	Created.
	 *
	 */
	public final void putTime(double timeI,double iTimeI,boolean forceI)
	    throws java.lang.Exception
	{
	    if (cutoff != 0.) {
		if (nextCutoff == -Double.MAX_VALUE) {
		    waitForActive();
		    computeCutoff(iTimeI);
		    startTime = timeI;
		    startITime = iTimeI;

		} else if (forceI ||
			   (iTimeI + blockDuration > nextCutoff)) {

/*
if (debug) {
    String[] timeLabels = { "Running Time",
			    "Source Time",
			    "Cutoff Time",
			    "Source End" };
    double[] times = { timeI, iTimeI, nextCutoff, iTimeI + blockDuration };
    System.err.println("\n" + sourceName + " values:");
    long lTime;
    int nanosec;
    java.text.SimpleDateFormat frmt = new java.text.SimpleDateFormat
	("yyyy-MM-dd HH:mm:ss.SSS");
    StringBuffer nanos;
    int size;
    for (int idx = 0; idx < timeLabels.length; ++idx) {
	lTime = (long) (times[idx]*1000.);
	nanosec = (int) ((times[idx]*1000. - lTime)*1000000);
	nanos = new StringBuffer("");
	for (size = 100000; nanosec < size; size = size/10) {
	    nanos.append("0");
	}
	nanos.append(Integer.toString(nanosec));
	System.err.println("   " + timeLabels[idx] + ": " +
			   frmt.format(new Date(lTime)) +
			   nanos.toString());
    }
}
*/

		    waitForActive();
		    timeChannelMap.PutTime(startTime,0.);
		    timeChannelMap.PutDataAsFloat64
			(0,
			 new double[] { startITime });
		    sources[activeSource].Flush(timeChannelMap,false);
		    timeChannelMap.PutTime(timeI,0.);
		    timeChannelMap.PutDataAsFloat64
			(0,
			 new double[] { iTimeI });
		    sources[activeSource].Flush(timeChannelMap,true);

		    startTime = timeI;
		    startITime = iTimeI;
		}
	    }
	}

	/**
	 * Runs this <code>ProxySource</code>.
	 * <p>
	 * This method is responsible for doing the actual work of closing
	 * a finished source connection to the RBNB and of opening the new
	 * one.  It runs in the background to allow the overlapping of work.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/14/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/14/2004  INB	Simply exit in single thread mode.  Call the
	 *			new <code>performSwap</code> method to do the
	 *			swap work.
	 * 01/09/2004  INB	Use new clear cache and detach logic.
	 * 01/06/2004  INB	Disconnect really old connections.
	 * 12/03/2003  INB	Created.
	 *
	 */
	public final void run() {
	    if (singleThreadMode) {
		return;
	    }

	    try {
		synchronized (this) {
		    while (doRun) {
			while (!doSwap) {
			    wait(10000);
			}

			performSwap();
			doSwap = false;
			notifyAll();
		    }
		}

	    } catch (java.lang.InterruptedException e) {
		doRun = false;
		doSwap = false;

	    } catch (java.lang.Exception e) {
		System.err.println("Proxy: source connection aborted");
		e.printStackTrace();
		doRun = false;
		doSwap = false;
	    }
	}

	/**
	 * Initiates a swap of the active and inactive source connections.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 01/14/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/14/2004  INB	In single thread mode, we actually do the
	 *			swap here.
	 * 12/03/2003  INB	Created.
	 *
	 */
	private final void swapActive()
	    throws java.lang.Exception
	{
	    if (singleThreadMode) {
		activeSource = (activeSource + 1) % 2;
		performSwap();

	    } else {
		synchronized (this) {
		    if (!isAlive()) {
			throw new Exception
			    ("Proxy: source connection aborted");
		    }

		    activeSource = (activeSource + 1) % 2;
		    doSwap = true;
		    notifyAll();
		}
	    }
	}

	/**
	 * Waits for the previously inactive source connection to become
	 * available.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @exception java.lang.Exception if an exception occurs.
	 * @since V2.2
	 * @version 01/14/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/14/2004  INB	Simply return in single thread mode.
	 * 12/03/2003  INB	Created.
	 *
	 */
	public final void waitForActive()
	    throws java.lang.Exception
	{
	    if (singleThreadMode) {
		return;
	    }

	    synchronized (this) {
		while (doSwap) {
		    if (!isAlive()) {
			throw new Exception
			    ("Proxy: source connection aborted");
		    }
		    wait(10000);
		}
	    }
	}
    }

    /**
     * Internal class to handle shutdowns.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/22/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/22/2003  INB	Created.
     *
     */
    private class ShutdownThread
	extends Thread
    {
	/**
	 * the primary thread.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/22/2003
	 */
	private Thread primaryThread = null;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/22/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/22/2003  INB	Created.
	 *
	 */
	public ShutdownThread() {
	    super();
	}

	/**
	 * Runs this <code>ShutdownThread</code>.
	 * <p>
	 * Forces a shutdown of the proxy thread.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 12/22/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/22/2003  INB	Created.
	 *
	 */
	public final void run() {
	    shutdown = true;
	    if (primaryThread != null) {
		primaryThread.interrupt();
		synchronized (primaryThread) {
		    while (shutdown) {
			try {
			    primaryThread.wait();
			} catch (Exception e) {
			}
		    }
		}
	    }
	}
    }

} //end class Proxy
