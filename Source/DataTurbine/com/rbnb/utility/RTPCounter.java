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

// UCB 06/01/01: a temp class; will read the sequence
// number from an RTP packet and report some of the
// values periodically, together with information
// regarding any irregularities or missing packets.

package com.rbnb.utility;

public class RTPCounter {

    private int currSeqNum = 0;
    private long rtpTimeStamp = 0;  // a long to avoid negative numbers
    private int mpegTempRef = 0;

    private int counter = 1;
    private int reportFreq = 10;

    private String name = "";

    private static final byte zero = (byte) 0;

    public RTPCounter() {}

    public RTPCounter(String nameI) {
	name = nameI;
    }

    public RTPCounter(int reportFreqI) {
	reportFreq = reportFreqI;
    }

    public RTPCounter(int reportFreqI, String nameI) {
	reportFreq = reportFreqI;
	name = nameI;
    }

    public void countPacket(byte[] data) {
	if ((data == null) || (data.length < 4)) {
	    return;
	}

	if (counter % reportFreq == 0) {
//  	    boolean ext = false;
//  	    byte cc;
//  	    byte xBit;
//  	    int mpegOff = 12;  // next byte after RTP SSRC

//  	    // using extension?
//  	    xBit = (byte) ((data[0] >>> 4) & 0x01);
//  	    ext = (xBit == 1);

//  	    // how many CSRC IDs?
//  	    cc = (byte) (data[0] & 0x0f);

	    // report packet number
	    currSeqNum = ByteConvert.byte2Int(new byte[] {zero,
							  zero,
							  data[2],
							  data[3]}, 
					      false)[0];
	    
	    System.err.println("Packet " + currSeqNum +
			       (name.equals("") ? "" : "  (" + name + ")"));

//  	    // report RTP timestamp
//  	    rtpTimeStamp = ByteConvert.byte2Long(new byte[] {zero,
//  							     zero,
//  							     zero,
//  							     zero,
//  							     data[4],
//  							     data[5],
//  							     data[6],
//  							     data[7]},
//  						 false)[0];
	    
//  	    // report MPEG temporal reference
//  	    mpegOff += cc * 4;
//  	    if (ext) {
//  		// must account for variable-length extension,
//  		// whose length is recorded in extension header
//  		int extLength = ByteConvert.byte2Int(new byte[] {zero,
//  								 zero,
//  								 data[mpegOff + 2],
//  								 data[mpegOff + 3]},
//  						     false)[0];
//  		mpegOff += (extLength + 1) * 4;  // one extra for ext header
//  	    }
//  	    mpegTempRef = ByteConvert.byte2Int(new byte[] {zero,
//  							   zero,
//  							   (byte) (data[mpegOff] & 0x03),
//  							   data[mpegOff + 1]},
//  					       false)[0];
	    
//  	    System.err.println("Packet " + currSeqNum +
//  			       "\nRTP Timestamp " + rtpTimeStamp +
//  			       "\nMPEG Temporal Reference " + mpegTempRef);

//  	    System.err.println("");
	}

	counter++;
    }

}
