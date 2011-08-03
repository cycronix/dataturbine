/*
Copyright 2011 Erigo Technologies LLC

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


package com.rbnb.udpheadlesscaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/******************************************************************************
 *
 * Based on code from UDPCaster, Copyright 2007 Creare Inc.
 *
 * Subscribe to an RBNB channel and send the data, one frame at a time, to
 * the given UDP address.
 * <p>
 *
 * @author John P. Wilson
 *
 * @version 04/19/2010
 */

/*
 * Copyright 2005 - 2008 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/27/2008  JPW	Add headless (no GUI) mode.  Class no longer extends JFrame.
 * 10/02/2007  JPW	Replace the single recipient with a Vector of
 *			Recipient objects (provided as an argument in the
 *			constructor).  To do this, I removed the use of
 *			recipientHost, recipientPort, and inetSocketAddress;
 *			added Vector recipients.
 * 09/26/2007  JPW	Add stream from oldest and autostart arguments
 *			to the constructor.
 * 06/03/2005  JPW	Created.  Based on the TCPCaster class.
 *
 */

public class UDPHeadlessCaster {

    /**
     * RBNB server to connect to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private String serverAddress = "localhost:3333";

    /**
     * RBNB channel to subscribe to
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private String chanName = null;

    /**
     * Socket port packets are sent from (the sender's port)
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private int senderPort = 3456;

    /**
     * Destination addresses of the UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/02/2007
     */
    private Vector recipients = new Vector();

    /**
     * DatagramSocket, used to send out UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private DatagramSocket datagramSocket = null;

    /**
     * RBNB Sink connection
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private Sink sink = null;

    /**
     * Are we connected to the RBNB?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private boolean bConnected = false;

    /**
     * Keep sending out RBNB data?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private boolean bKeepRunning = true;

    /**
     * Thread which fetches data from the RBNB and sends it out as UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private Thread rbnbThread = null;

    /**
     * The number of frames fetched from the RBNB server
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */
    private long frameNumber = 0;

    /**
     * Sleep time (in milliseconds) between requests for the newest frame.
     * <p>
     * 
     * @author John P. Wilson
     * 
     * @version 04/19/2010
     */
    private long fetchPeriod = 0;

    /**************************************************************************
     * Constructor
     * <p>
     *
     * @author John P. Wilson
     *
     * @param serverAddressI      RBNB server to connect to
     * @param chanNameI           Channel to subscribe to
     * @param senderPortI         The local bind port
     * @param recipientsI         Where to send the UDP packets
     * @param fetchPeriodI        Sleep time between requests for the newest frame.
     *
     * @version 04/19/2010
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/27/2008  JPW	Add bHeadlessI argument
     * 10/02/2007  JPW  Remove recipientHostI and recipientPortI; add
     *			Vector recipientsI.
     * 09/26/2007  JPW  Add stream from oldest and autostart arguments.
     * 06/03/2005  JPW  Created.
     *
     */

    public UDPHeadlessCaster(String serverAddressI,
	    String chanNameI,
	    int senderPortI,
	    Vector recipientsI,
	    long fetchPeriodI)
    {

	if ( (serverAddressI != null) && (!serverAddressI.equals("")) ) {
	    serverAddress = serverAddressI;
	}
	if ( (chanNameI != null) && (!chanNameI.equals("")) ) {
	    chanName = chanNameI;
	}
	if (senderPortI > 0) {
	    senderPort = senderPortI;
	}
	if (recipientsI != null) {
	    recipients = recipientsI;
	}
	if (fetchPeriodI >= 0) {
	    fetchPeriod = fetchPeriodI;
	}

	// Check to make sure we have all needed data
	if ( (serverAddress != null) && (!serverAddress.equals("")) &&
		(chanName != null) && (!chanName.equals(""))           &&
		(senderPort > 0)                                       &&
		(recipients != null) && (!recipients.isEmpty()) )
	{
	    System.err.println("\nStart UDP headless caster using the following parameters:");
	    System.err.println("Server address: " + serverAddress);
	    System.err.println("Channel name: " + chanName);
	    System.err.println("Local bind port: " + senderPort);
	    System.err.println("Recipient addresses:");
	    for (Enumeration e=recipients.elements(); e.hasMoreElements();) {
		Recipient rec = (Recipient)e.nextElement();
		System.err.println("\t" + rec);
	    }
	    System.err.println("\n");
	    if (fetchPeriod > 0) {
		System.err.println("Request newest frame every " + fetchPeriod + " milliseconds.");
	    } else {
		System.err.println("Request newest frame as quickly as possible.");
	    }
	    // Start
	    openAction();
	}
	else
	{
	    System.err.print("\n\nCannot start UDP headless caster: ");
	    if ( (serverAddress == null) || (serverAddress.equals("")) ) {
		System.err.println("Server address not initialized\n");
	    } else if ( (chanName == null) || (chanName.equals("")) ) {
		System.err.println("Input channel name not initialized\n");
	    } else if (senderPort > 0) {
		System.err.println("Local bind port less than or equal to 0\n");
	    } else if ( (recipients == null) || (recipients.isEmpty()) ) {
		System.err.println("No recipients have been specified.\n");
	    }
	}

    }

    /**************************************************************************
     * Check that the user has entered values in the GUI fields and then call
     * connect().
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */

    private void openAction() {

	try {
	    connect();
	} catch (Exception e) {
	    String errorMsg =
		new String("Error opening connections:\n" + e.getMessage());
	    System.err.println(errorMsg);
	    disconnect();
	    return;
	}

    }

    /**************************************************************************
     * Connect to the RBNB, start Subscribing to chanName, and open the
     * DatagramSocket
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */

    private void connect() throws IOException, SAPIException {

	// Make sure we are disconnected first
	disconnect();

	bKeepRunning = true;

	int portAttempt = 0;
	while (portAttempt < 100) {
	    try {
		datagramSocket = new DatagramSocket(senderPort);
		break;
	    } catch (SocketException se) {
		datagramSocket = null;
		++portAttempt;
		++senderPort;
	    }
	}
	if (datagramSocket == null) {
	    throw new IOException(
		    "Could not find a port to bind the DatagramSocket to.");
	}
	System.err.println("DatagramSocket bound to local port " + senderPort);

	sink = new Sink();
	sink.OpenRBNBConnection(serverAddress,"UDPCasterSink");

	// Start thread to fetch RBNB data and send it out as UDP packets
	Runnable rbnbRunnable = new Runnable() {
	    public void run() {
		runFetch();
	    }
	};
	rbnbThread = new Thread(rbnbRunnable);
	rbnbThread.start();

	System.err.println("UDP socket and RBNB data fetch connections open.");

	bConnected = true;

    }

    /**************************************************************************
     * Disconnect from the RBNB and close the DatagramSocket.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/09/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/09/2005  JPW  This method no longer throws IOException
     * 06/03/2005  JPW  Created.
     *
     */

    public void disconnect() {

	if ( (!bConnected)  &&
		(sink == null) &&
		(datagramSocket == null) )
	{
	    return;
	}

	bKeepRunning = false;

	// Wait for the RBNB data fetch/send thread to exit
	if ( (rbnbThread != null) &&
		(Thread.currentThread() != rbnbThread) )
	{
	    try {
		System.err.println(
			"Waiting for the RBNB data fetch/send thread to stop...");
		long timeToWait = 3000 + (2 * fetchPeriod);
		rbnbThread.join(timeToWait);
	    } catch (InterruptedException ie) {}
	}
	System.err.println("RBNB data fetch/send thread has stopped.");
	rbnbThread = null;

	// Close RBNB connection
	if (sink != null) {
	    sink.CloseRBNBConnection();
	    sink = null;
	}

	// Close the DatagramSocket
	if (datagramSocket != null) {
	    datagramSocket.close();
	    datagramSocket = null;
	}

	System.err.println("All connections closed.");
	bConnected = false;

    }

    /**************************************************************************
     * Exit the application.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */

    private void exit() {

	disconnect();
	System.exit(0);

    }

    /**************************************************************************
     * Fetch data from RBNB and send it out as a UDP packet
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/03/2005  JPW  Created.
     *
     */

    private void runFetch() {

	double timeOfLastFrame = -1.0 * Double.MAX_VALUE;

	try {

	    while (bKeepRunning) {

		ChannelMap requestMap = new ChannelMap();
		requestMap.Add(chanName);
		sink.Request(requestMap, 0.0, 0.0, "newest");
		ChannelMap dataMap = sink.Fetch(1000);
		if ( (dataMap == null) || (dataMap.NumberOfChannels() == 0) ) {
		    continue;
		}

		double time = dataMap.GetTimeStart(0);
		if (time == timeOfLastFrame) {
		    Thread.sleep(fetchPeriod);
		    continue;
		}
		timeOfLastFrame = time;

		// We are only expecting 1 channel
		if (dataMap.NumberOfChannels() > 1) {
		    System.err.println(
			    "Warning: we will only UDP data for 1 channel even though we received " +
			    dataMap.NumberOfChannels() +
		    " channels from the RBNB.");
		}

		int length = 0;
		// Extract data from ChannelMap using GetDataAsByteArray() only
		// if the type of the data is TYPE_BYTEARRAY.  Otherwise, just
		// treat the data as a blob of bytes.
		if (dataMap.GetType(0) != ChannelMap.TYPE_BYTEARRAY) {
		    byte[] dataArray = dataMap.GetData(0);
		    if (dataArray == null) {
			continue;
		    }
		    length = 1;
		    // Write data out as UDP packet
		    writeData(dataArray);
		} else {
		    byte[][] dataArray = dataMap.GetDataAsByteArray(0);
		    if (dataArray == null) {
			continue;
		    }
		    length = dataArray.length;
		    for (int j = 0; j < length; ++j) {
			// Write data out as UDP packet
			writeData(dataArray[j]);
		    }
		}
		frameNumber += length;
		// Display time
		String startTimeStr = Double.toString(time);
		// This is rather arbitrary, but if the start time is less than
		// 10^6, assume the time is relative (not seconds since epoch)
		if (time > 1000000) {
		    SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd-MMM-yyyy z HH:mm:ss.SSS");
		    // time is in seconds since 01/01/1970; first need to
		    // convert to number of milliseconds since epoch
		    Date displayDate = new Date( (long)(time * 1000.0) );
		    startTimeStr = dateFormat.format(displayDate);
		}
		System.err.println(
			"Send " +
			length +
			" frames (total = " +
			frameNumber +
			"); start time = " +
			startTimeStr);

		Thread.sleep(fetchPeriod);

	    } // end while loop

	} catch (Exception e) {
	    /*
	     * 
	     * MAYBE TRY AN AUTO-RECONNECT HERE?
	     * 
	     */
	    System.err.println(
		    "Error fetching RBNB data or sending UDP packet out:\n" +
		    e.getMessage());
	    e.printStackTrace();
	    disconnect();
	}
    }

    /**************************************************************************
     * Write data out as a UDP packet
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/02/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/02/2007  JPW  Send DatagramPacket to all recipients
     * 06/03/2005  JPW  Created.
     *
     */

    private void writeData(byte[] dataI) throws Exception {

	if ( (dataI == null) || (dataI.length == 0) ) {
	    throw new Exception("Error: tried to write out empty packet.");
	}

	DatagramPacket dp =
	    new DatagramPacket( new byte[dataI.length], dataI.length );
	dp.setData(dataI);
	for (Enumeration e = recipients.elements(); e.hasMoreElements(); ) {
	    Recipient rec = (Recipient)e.nextElement();
	    dp.setSocketAddress(rec.getSocketAddr());
	    datagramSocket.send(dp);
	}

    }
    
    public long getFrameNumber() {		// for status update
    	return(frameNumber);
    }

}
