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

/*
  *****************************************************************
  ***								***
  ***	Name :	TCPClientPlayerOutput		                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   TCPClientPlayerOutput is a subclass of PlayerOutput,	***
  ***	used to output Maps to a TCP/IP socket.                 ***
  ***								***
  ***	10/07/2004: NOTE FROM JPW				***
  ***	This class is not currently used in Player.  Maybe UCB	***
  ***	began working on the implementation but never		***
  ***	integrated it into the rest of the Player code.		***
  ***								***
  ***	Modification History					***
  ***	06/16/2005	JPW	Change name from TCPPlayerOutput***
  ***				to TCPClientPlayerOutput	***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player; see	***
  ***				my note above.			***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ByteConvert;

import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.InetAddress;

class TCPClientPlayerOutput extends PlayerOutput {

    private static final byte zero = (byte) 0;

    private Socket           socket = null;
    private int              destPort;
    private InetAddress      iAdd = null;
    private DataOutputStream dos = null;

    private String[]         chans = null;
    private byte[]           dataIn = null;
    private byte[]           dataOut = null;

    // UCB 06/22/01: for testing
//      private int currSeqNum = 0;
//      private int lastSeqNum = 0;
//      private int seqNum = 0;
//      private byte[] seqBytes;

/*
  *****************************************************************
  ***								***
  ***	Name :	TCPClientPlayerOutput		                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires a destination InetAddress and ***
  ***   port.                                                   ***
  ***								***
  *****************************************************************
*/
    public TCPClientPlayerOutput() {}

/*
  *****************************************************************
  ***								***
  ***	Name :	TCPClientPlayerOutput		                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires a destination InetAddress and ***
  ***   port.                                                   ***
  ***								***
  *****************************************************************
*/
    public TCPClientPlayerOutput(InetAddress iAddI, int port)
        throws Exception
    {
	iAdd = iAddI;
	destPort = port;
	socket = new Socket(iAdd, destPort);
	dos = new DataOutputStream(socket.getOutputStream());
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	connect  		                 	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Serves to initialize a TCPClientPlayerOutput created ***
  ***   without an InetAddress or port.                         ***
  ***								***
  *****************************************************************
*/
    public final void connect(InetAddress iAddI, int port) throws Exception {
	iAdd = iAddI;
	destPort = port;
	socket = new Socket(iAdd, destPort);
	dos = new DataOutputStream(socket.getOutputStream());
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect         			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Disconnects socket.                                  ***
  ***								***
  *****************************************************************
*/
    public void disconnect() {
	if (socket != null) {
	    try {
	        dos.close();
	        socket.close();
	    } catch (Exception e) {
		// Nothing to do
	    }
	    socket = null;
	}
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	setOutputChannels               		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :  May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	    Check for just one channel.                         ***
  ***								***
  *****************************************************************
*/
    public void setOutputChannels(
        ChannelMap outMap,
        boolean useShortNamesI)
    throws Exception
    {
	if (outMap.GetChannelList().length > 1) {
	    throw new Exception(
		"Only one channel is permitted for TCP output.");
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	outputData     			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method outputs a map.                           ***
  ***								***
  *****************************************************************
*/
    public void outputData(ChannelMap outMap) throws Exception {
	// Should not receive a null outMap or an outMap
	// with no data!

	chans = outMap.GetChannelList();
	if (chans.length > 1) {
	    throw new Exception(
	        "Only one channel is permitted for UDP output.");
	}
	dataIn = outMap.GetData(0);
	
	dataOut = new byte[dataIn.length];
	System.arraycopy(dataIn, 0, dataOut, 0, dataIn.length);
	DatagramPacket packet =
            new DatagramPacket(
                dataOut, dataOut.length, iAdd, destPort);

	// output the data
	try {
	    // JPW 10/08/2004: Not sure what UCB was doing here; there is no
	    //                 send() method in socket; for now, just replace
	    //                 it with the write() call.
	    // socket.send(packet);
	    // ? Maybe we should be using packet somehow to send out the data ?
	    dos.write(dataOut, 0, dataOut.length);
	} catch (Exception e) {
	    System.err.println(
	        "Exception encountered outputting to DataTurbine.");
	    throw e;
	}

//  	int intLen = 0;
//  	int offset = 0;
//  	int upperBnd = fullForward ? stamp.getNumberOfIntervals() : 1;
//  	for (int i = 0; i < upperBnd; i++) {
//  	    intLen = stamp.getPointsInInterval(i);
//  	    // construct DatagramPacket
//  	    dataOut = new byte[intLen];
//  	    System.arraycopy(dataIn, offset, dataOut, 0, intLen);
//  	    offset += intLen;
//  	    // force seq num to increment, by one if actual data is
//  	    // going backwards, otherwise by the measured gap.
//  	    currSeqNum = seqNum(dataOut);
//  	    seqNum += currSeqNum > lastSeqNum ? currSeqNum - lastSeqNum : 1;
//  	    lastSeqNum = currSeqNum;
//  	    seqBytes = ByteConvert.int2Byte(new int[] {seqNum});
//  	    seqNum = (seqNum + 1) % Short.MAX_VALUE;
//  	    dataOut[2] = seqBytes[2];
//  	    dataOut[3] = seqBytes[3];
//  	    packet = new DatagramPacket(dataOut, dataOut.length,
//  					iAdd, destPort);
//  	    // output the data
//  	    try {
//  		socket.send(packet);
//  	    } catch (Exception e) {
//  		System.err.println(
//                  "Exception encountered outputting to DataTurbine.");
//  		throw e;
//  	    }
//  	}

    }

/*
  *****************************************************************
  ***								***
  ***	Name :	seqNum                                          ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	July, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***      Read the RTP sequence number of this packet.         ***
  ***								***
  *****************************************************************
*/
    private static int seqNum(byte[] packet) {
	return ByteConvert.byte2Int(new byte[] {zero,
						zero,
						packet[2],
						packet[3]}, 
				    false)[0];
    }
}

