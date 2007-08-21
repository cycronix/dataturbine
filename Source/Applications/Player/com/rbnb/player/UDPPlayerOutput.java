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
  ***	Name :	UDPPlayerOutput		                 	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   UDPPlayerOutput is a subclass of PlayerOutput, and   ***
  ***   used to output Maps to a unicast UDP socket.            ***
  ***								***
  ***	Modification History					***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.UDPOutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPPlayerOutput extends PlayerOutput {

    protected UDPOutputStream udpOut = null;
    private String[]          chans = null;
    private byte[]            data = null;

    // UCB 06/22/01: for testing
//      private int currSeqNum = 0;
//      private int lastSeqNum = 0;
//      private int seqNum = 0;
//      private byte[] seqBytes;

    private static final byte zero = (byte) 0;

/*
  *****************************************************************
  ***								***
  ***	Name :	UDPPlayerOutput		                 	***
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
    public UDPPlayerOutput() {}

/*
  *****************************************************************
  ***								***
  ***	Name :	UDPPlayerOutput		                 	***
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
    public UDPPlayerOutput(InetAddress iAdd, int port) throws Exception {
	connect(iAdd, port);
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
  ***	   Connects to UDPOutputStream.                         ***
  ***								***
  *****************************************************************
*/
    public void connect(InetAddress iAdd, int port) throws Exception {
	udpOut = new UDPOutputStream(iAdd, port);
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
  ***	   Disconnects source from DataTurbine.                 ***
  ***								***
  *****************************************************************
*/
    public final void disconnect() {
	if (udpOut != null) {
	    try {
		udpOut.close();
	    } catch (Exception e) {}
	    udpOut = null;
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
    public final void setOutputChannels(
	ChannelMap outMap,
	boolean useShortNamesI)
    throws Exception
    {
	if (outMap.GetChannelList().length > 1) {
	    throw new Exception(
		"Only one channel is permitted for UDP output.");
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
  ***	UCB 05/25/01 - moved this method from PlayerEngine to   ***
  ***                  UDPPlayerOutput.                         ***
  ***								***
  *****************************************************************
*/
    public final void outputData(ChannelMap outMap) throws Exception {
	// Should not receive a null outMap or an outMap
	// with no data!

	chans = outMap.GetChannelList();
	if (chans.length > 1) {
	    throw new Exception(
	        "Only one channel is permitted for UDP output.");
	}

	data = outMap.GetData(0);
	
	// JPW 10/07/2004: In RBNB V2, there are no time intervals
	/*
	DataTimeStamps stamp = chans[0].getTimeStamp();
	int intLen = 0;
	int offset = 0;
	int upperBnd = stamp.getNumberOfIntervals();
	for (int i = 0; i < upperBnd; i++) {
	    intLen = stamp.getPointsInInterval(i);
	    udpOut.setBufferSize(intLen);

	    udpOut.write(data, offset, intLen);
	    offset += intLen;
	}
	*/
	
	udpOut.setBufferSize(data.length);
	udpOut.write(data, 0, data.length);
	udpOut.flush();
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
//      private static int seqNum(byte[] packet) {
//  	return ByteConvert.byte2Int(new byte[] {zero,
//  						zero,
//  						packet[2],
//  						packet[3]}, 
//  				    false)[0];
//      }
}

