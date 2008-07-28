/*
	Fragmenter.java
	
	2007/06/05  WHF  Created.
	2007/06/07  WHF  Extended to allow out-of-order fragments and packets,
		increasing complexity considerably.	

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

package com.rbnb.udp;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
  *	A utility to break UDP packets into chunks smaller than a specified 
  *	threshold.  Created to work around problems with UDP and IP fragmentation.
  *  UDP packets larger than the Ethernet MTU are often dropped because
  *  fragmentation of UDP packets at the IP layer is not supported by all 
  *  implementations.
  */
public class Fragmenter
{
//**************************  Public Constants  *****************************//
	/**
	  * Mode constants.
	  */
	public static final int
		MODE_FRAG      = 1,
		MODE_DEFRAG    = 2,
		MODE_UNDEFINED = 0;
	
	/**
	  * Constructs a new fragmenter in UNDEFINED mode with no addresses.
	  */
	public Fragmenter()
	{
		setThreshold(1484);
	}
	
	/**
	  * Bind to the specified local address.
	  */
	public void bind(SocketAddress sa) throws SocketException
	{
		if (socket != null) socket.close();
		socket = new DatagramSocket(sa);
		bindAddress = sa;
	}
	
	public void bind(String s) throws SocketException
	{
		int port;
		int index = s.indexOf(':');
		
		if (index == -1) {
			// use default port, for which mode must be defined
			if (mode == MODE_UNDEFINED) 
				throw new IllegalStateException(PORT_REQUIRES_MODE);
			if (mode == MODE_FRAG)
				port = 5000;
			else port = 5001;
		} else {
			port = Integer.parseInt(s.substring(index+1));
			s = s.substring(0, index);
		}
		
		bind(new InetSocketAddress(s, port));
	}
		
	
	/**
	  * Adds a socket address to the list of addresses to receive packets,
	  *  either fragmented or reassembled.
	  */
	public void addRemoteAddress(SocketAddress sa)
	{
		remoteAddr.add(sa);
	}
	
	/**
	  * Parse host[:port] into correct form.
	  * @throws IllegalStateException  If the mode is not set.
	  */
	public void addRemoteAddress(String s)
	{		
		int port;
		int index = s.indexOf(':');
		
		if (index == -1) {
			// use default port, for which mode must be defined
			if (mode == MODE_UNDEFINED) 
				throw new IllegalStateException(PORT_REQUIRES_MODE);
			if (mode == MODE_FRAG)
				port = 5001;
			else port = 5000;
		} else {
			port = Integer.parseInt(s.substring(index+1));
			s = s.substring(0, index);
		}
		
		addRemoteAddress(new InetSocketAddress(s, port));
	}
	
	/**
	  * Removes all addresses from the list.
	  */
	public void clearRemoteAddresses()
	{
		remoteAddr.clear();
	}
	
	int getBufferSize() { return bufferSize; }
	/**
	  * The size in bytes used in 
	  *  {@link DatagramSocket#setReceiveBufferSize(int)}
	  *  and {@link DatagramSocket#setSendBufferSize(int) }.
	  * If zero the platform dependent value is used.
	  */
	void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
	
	boolean getDebugFlag() { return debugFlag; }
	void setDebugFlag(boolean debugFlag) { this.debugFlag = debugFlag; }
	
	public int getMode() { return mode; }
	
	/**
	  * Set the mode, either MODE_FRAG or MODE_DEFRAG.
	  * Note that setting this clears any cached bind address, so that 
	  * calling bind, stop, setMode, start will bind to the default port the
	  *  second time.
	  */
	public void setMode(int mode)
	{
		if (mode > MODE_DEFRAG || mode < MODE_UNDEFINED) throw new 
			IllegalArgumentException("Illegal mode.");
		this.mode = mode;
		bindAddress = null;
	}
	
	public int getSleepTime() { return sleepTime; }
	/** 
	  * Set the delay between transmission of fragments.  The default is zero,
	  *  which can work for low fragment counts.
	  */
	public void setSleepTime(int sleepTime) { this.sleepTime = sleepTime; }
	
	/**
	  * Get the packet size beyond which packets will be fragmented.
	  */
	public int getThreshold() { return threshold; }
	/**
	  * Set the packet size beyond which packets will be fragmented.
	  *  This is also the resulting fragment data payload size.  The default
	  *  is 1484.
	  */
	public void setThreshold(int threshold) 
	{
		this.threshold = threshold;
		fragPacket.setData(new byte[threshold + HEADER_SIZE]);
	}
	
	/**
	  * Starts the fragmenter or defragmenter in a new thread.
	  *
	  * If bind() has not yet been called, the socket will be bound to the
	  *  default port for this mode.
	  */
	public void start() throws SocketException
	{
		if (running) throw new IllegalStateException("Already running.");
		if (remoteAddr.isEmpty())
			throw new IllegalStateException("Recipient list empty.");
		if (socket == null) {
			if (bindAddress == null)
				bind(new InetSocketAddress(mode == MODE_FRAG?5000:5001));
			else bind(bindAddress);
		}
		if (bufferSize != 0) {
			socket.setReceiveBufferSize(bufferSize);
			socket.setSendBufferSize(bufferSize);
			if (debugFlag) {
				System.err.println("Socket buffer size actual value: "
					+ socket.getReceiveBufferSize()
				);
			}
		}
		stopSignal = false;
		switch (mode) {
			case MODE_FRAG:
			new Thread(fragRun, "Fragmenter").start();
			break;
			
			case MODE_DEFRAG:
			new Thread(defragRun, "Defragmenter").start();
			break;
			
			default:
			throw new IllegalStateException("Illegal mode.");
		}
	}
	
	public void stop()
	{
		stopSignal = true;
		socket.close();
		while (running) {
			synchronized (stopEvent) {
				try {
					stopEvent.wait(1000);
				} catch (InterruptedException ie) {}
			}
		}
		socket = null;
	}
	
//*****************************  Utility Methods  ***************************//
	private void send(DatagramPacket dp) throws IOException
	{
		for (int ii = 0; ii < remoteAddr.size(); ++ii) {
			dp.setSocketAddress((SocketAddress) remoteAddr.get(ii));
		
			socket.send(dp);
		}
	}
	
//*****************************  Inner Classes  *****************************//
	private abstract class FragDefragRunnable implements Runnable
	{
		protected abstract void receiveAndSend() throws Exception;
		
		public void run()
		{
			try {
				running = true;
				while (!stopSignal) {
					try {
						receiveAndSend();
					} catch (BadPacketException bpe) {
						System.err.println(bpe.getMessage());
					}
				}
			} catch (Exception e) {
				if (!stopSignal)
					e.printStackTrace();	
			} finally {
				running = false;
				synchronized (stopEvent) {
					stopEvent.notify();
				}
			}
		}
	}
	
	private Runnable fragRun = new FragDefragRunnable() {
		protected void receiveAndSend() throws Exception
		{
			// Block, waiting for packets.
			socket.receive(defragPacket);
			if (defragPacket.getOffset() != 0) badarg(
				"Code assumes incoming packets have zero offsets.");
			
			// Compute packet checksum:
			int inLength = defragPacket.getLength();
			short csum = onesComplementChecksum(
					defragPacket.getData(),
					inLength
			);
			
			// Fragment the packet and transmit.
			int nFrag = inLength / threshold;
			if (inLength != nFrag * threshold) ++nFrag;
			
			int len;
			short iFrag = 0;
			byte[] out = fragPacket.getData();
			do {
				len = fragment(
						defragPacket.getData(),
						inLength,
						out,
						threshold,
						iFrag,
						HEADER_SIZE
				);
				if (len == 0) break;
				
				fragPacket.setLength(len + HEADER_SIZE);
				
				writeBytes(out, seqNum, 0);
				writeBytes(out, (short) nFrag, 2);
				writeBytes(out, iFrag++, 4);
				writeBytes(out, csum, 6);

				send(fragPacket);
				try { Thread.sleep(sleepTime); } catch (Throwable t) {}
				if (debugFlag)
					System.err.println(
							"Sent packet #"+iFrag+" of "+nFrag+" of seq "
							+(seqNum+1)+", len "+fragPacket.getLength());
			} while (len == threshold);
			
			++seqNum;
		}
		private short seqNum = 0;		
	};
	
	private class DefragRunnable extends FragDefragRunnable
	{
		public DefragRunnable()
		{
			for (int ii = 0; ii < packetQueue.length; ++ii) {
				packetQueue[ii] = new Reassembler();
				packetQueue[ii].setDebugFlag(debugFlag);
			}
		}
		
		protected void receiveAndSend() throws Exception
		{
			// Block, waiting for packets.
			socket.receive(fragPacket);
			
			if (fragPacket.getOffset() != 0) badarg(
				"Code assumes incoming packets have zero offsets.");
			
			// Packet too small to even hold sequence number, reject:
			if (fragPacket.getLength() < 2) return;
			short seqNum = readBytes(fragPacket.getData(), 0);
			
			if (debugFlag) {
				short nFrag = readBytes(fragPacket.getData(), 2),
					iFrag = readBytes(fragPacket.getData(), 4);
				System.err.println("Rcvd packet #"+(iFrag+1)+" of "+nFrag
						+" of seq "+(seqNum+1)+", len "+fragPacket.getLength());
			}
			
			// Quick linear search for matching sequence number.  If we find
			//  a complete packet first, it accepts the fragment.
			for (int ii = 0; ii < packetQueue.length; ++ii) {
				Reassembler reassembler = packetQueue[ii];
				if (reassembler.isComplete()) {
					reassembler.startPacket(fragPacket);
					if (!checkComplete(ii) && ii != 0) {
						// Move new packet to front of queue:
						System.arraycopy(
								packetQueue,
								0,
								packetQueue,
								1,
								ii
						);
						packetQueue[ii] = reassembler;
					}
					return;					
				}
				if (reassembler.getSeqNum() == seqNum) {
					reassembler.reassemble(fragPacket);
					checkComplete(ii);
					return;
				}
			}
			// Search failed, roll off oldest:
			Reassembler oldest = packetQueue[packetQueue.length - 1];
			// 3 2 1 => 4 3 2
			System.arraycopy(
					packetQueue,
					0,
					packetQueue,
					1,
					packetQueue.length - 1
			);
			packetQueue[0] = oldest;
			oldest.markComplete();
			oldest.startPacket(fragPacket);
			checkComplete(0);		
		}
		
		/**
		  * Checks if the reassembler is complete, sends it and moves it to the
		  *  end of the queue if it is.
		  * @param ii the index of the reassembler.
		  * @return true if the packet is complete.
		  */
		private boolean checkComplete(int ii) throws IOException
		{
			Reassembler reassembler = packetQueue[ii];
			if (reassembler.isComplete()) {
				// Packet complete:
				send(reassembler.getCompletedPacket());
				if (ii == 0) { 
					// Newest packet complete.  Discard others.
					// c p p => c c c
					for (int iii = 1; iii < packetQueue.length; ++iii)
						packetQueue[iii].markComplete();
				} else {
					// Otherwise, move completed packet to end:
					// p c p => p p c
					System.arraycopy(
							packetQueue,
							ii+1,
							packetQueue,
							ii,
							packetQueue.length - (ii+1)
					);
					packetQueue[packetQueue.length-1] = reassembler;
				}
				return true;
			}
			return false;
		}
		
		/**
		  * A list of incomplete packets, sorted by receipt-time order, newest
		  *  first.  A Reassembler in the 'complete' phase is ready for reuse,
		  *  and is located at the end of the queue.
		  */
		private final Reassembler[] packetQueue 
				= new Reassembler[MAX_PENDING_PACKETS];
	}
		
//******************************  Member Data  ******************************//
	// Cannot be final due to 'design' of socket class.
	private DatagramSocket socket;
	private SocketAddress bindAddress;
	private int threshold,
		mode = MODE_UNDEFINED;
		
	private volatile boolean running = false, stopSignal = false;
	private final Object stopEvent = new Object();

	/**
	  * If true, emits some messages during packet receive/transmit.
	  */
	private boolean debugFlag = false;
	
	/**
	  * Delay between transmission of fragments.
	  */
	private int sleepTime = 0;
	
	/**
	  * The size in bytes used in 
	  *  {@link DatagramSocket#setReceiveBufferSize(int)}
	  *  and {@link DatagramSocket#setSendBufferSize(int) }.
	  * If zero the platform dependent value is used.
	  */
	private int bufferSize = 0;
	
	/**
	  * A collection of InetSocketAddresses to receive fragments/reassembled
	  *  packets.
	  */
	private final java.util.Vector remoteAddr = new java.util.Vector();
	private final DatagramPacket 
			fragPacket = new DatagramPacket(new byte[1], 0),
			defragPacket = new DatagramPacket(new byte[64*1024], 64*1024);
//	private final Reassembler reassembler = new Reassembler();
	private DefragRunnable defragRun = new DefragRunnable();	
	
	
//********************************  Statics  ********************************//
	/**
	  * Fragmentation algorithm.  The packet contained in the first <i>length
	  *  </i> bytes of <i>in</i>, is broken conceptually into <i>n</i>
	  *  fragments, where n = length / fragSize (+1 unless length is a 
	  *  multiple of fragSize).  The fragment corresponding to the zero based 
	  *  fragment index <i>iFrag</i> is placed in
	  *  <i>out</i> starting at index <i>offset</i>.
	  * <p>The number of valid bytes in out is returned.  This will be 
	  *  fragSize for all but the last fragment.  This fact can be used for
	  *  loop termination.
	  * <p>
	  * @throws NullPointerException if in or out are null.
	  * @throws IndexOutOfBoundsException  if fragSize * iFrag > in.length, or 
	  *   if fragSize+offset > out.length.
	  */
	public static int fragment(
			byte[] in,
			int length,
			byte[] out,
			int fragSize,
			int iFrag,
			int offset)
	{		
		int toCopy = Math.min(length - fragSize * iFrag, fragSize);
		
		System.arraycopy(
				in,
				fragSize * iFrag,
				out,
				offset,
				toCopy
		);
		
		return toCopy;
	}
	
/* Not used.  See Reassembler.
	public static int defrag(
			byte[] in,
			int length,
			int offIn,
			byte[] out,
			int offOut)
	{
		System.arraycopy(
				in,
				offIn,
				out,
				offOut,
				length
		);

		return offOut+length;		
	}
*/

	/**
	  * Utility method which computes the 16-bit checksum of the input buffer.
	  *  The algorithm used is as follows:
	  *<p>The checksum field is the 16 bit one's complement of the one's
	  * complement sum of all 16 bit words in the header.  For purposes of
	  * computing the checksum, the value of the checksum field is zero.
	  *
	  * @param buff The input buffer, which is not modified in any way.
	  * @param nBytes Number of bytes to use in the buffer.  If odd, will be
	  *   padded with a zero byte.
	  *
	  * @return The checksum.  If it would be zero, return 0xFFFF.
	  */	
	public static short onesComplementChecksum(
			byte[] buff,
			int nBytes)
	{
		int nShorts = nBytes / 2;
		int pos = 0, end = pos + nShorts * 2;
		int checksum = 0;
		
		for ( ; pos < end; pos += 2)
			checksum += (readBytes(buff, pos) & 0xffff);
		if ((nBytes & 1) != 0) 
			checksum += (buff[pos] & 0xff) << 8;  // low eight zero
			
		checksum = ~((checksum & 0xffff) + (checksum >> 16));
		
		if (checksum == 0) return (short) -1;
		
		return (short) checksum;
	}
	
	public static void main(String[] args) throws SocketException, IOException
	{
		Fragmenter f = new Fragmenter();
		
		try {
			for (int ii = 0; ii < args.length; ++ii) {
				char c = args[ii].charAt(0);
				if (c != '-' && c != '/') 
					badarg("Switches must start with '-' or '/'.");
				
				switch (args[ii].charAt(1)) {
					case 'h':
					case '?':
					throw new IllegalArgumentException();
					
					case 'm':
					if ("fragment".startsWith(args[++ii])) {
						f.setMode(f.MODE_FRAG);
					} else if ("defragment".startsWith(args[ii])) {
						f.setMode(f.MODE_DEFRAG);
					} else badarg("Unrecognized mode.");
					break;
					
					case 'r':
					f.addRemoteAddress(args[++ii]);
					break;
					
					case 'b':
					f.bind(args[++ii]);
					break;
					
					case 'n':
					f.setThreshold(Integer.parseInt(args[++ii]));
					break;
					
					case 's':
					f.setSleepTime(Integer.parseInt(args[++ii]));
					break;
					
					case 'd':
					f.setDebugFlag(true);
					break;
					
					case 'u':
					f.setBufferSize(Integer.parseInt(args[++ii]));
					break;
					
					default:
					badarg("Unrecognized argument.");
				}
			}
			f.start();
			System.out.println("Press enter to exit.");
			System.in.read();
			f.stop();
		} catch (IllegalArgumentException iae) {
			System.err.println(iae.getMessage());
			showHelp();
		}
	}
	
	private static void badarg(String s)
	{ throw new IllegalArgumentException(s); }
	
	/**
	  * Quick and dirty to read shorts from a byte array without using
	  *  library calls.  Network byte order!
	  */
	static short readBytes(byte[] in, int off)
	{
		return (short) ((in[off] << 8) | (in[off+1] & 0xff));
	}
	
	/**
	  * Quick and dirty to write shorts out a byte array without using
	  *  library calls.  Network byte order!
	  */
	private static void writeBytes(byte[] out, short d, int off)
	{
		out[off]   = (byte) (d >>> 8);
		out[off+1] = (byte) (d & 0xff);
	}
	
	private static void showHelp()
	{
		System.err.println(SYNTAX);	
	}
	
	private static final String SYNTAX = 
 "udpfrag.jar -m frag|defrag -r addr[:port] [-r addr2[:port2] ...] [options]\n"
+"        with the flags are defined as:\n"
+"                 -m   mode, either fragment or defragment,\n"
+"                 -r   remote address and optional port, with\n"
+"                      defaults of 5001 for frag, 5000 for defrag modes,\n"
+"            and where options are zero or more of:\n"
+"                 -s   sleep time between fragments, millis (frag mode only)\n"
+"                 -b   local bind address and or port, with defaults of\n"
+"                      localhost:5000 for frag, :5001 for defrag mode,\n"
+"                 -n   packet fragment threshold; packets with data payloads\n"
+"                      larger than this are broken into fragments no bigger\n"
+"                      than this value, which defaults to 1484,\n"
+"                 -u   UDP socket send & receive buffer size (bytes),\n"
+"                 -d   emit debug information.\n",
		PORT_REQUIRES_MODE = 
			"Mode must be defined before adding addresses without ports.";

	/**
	  * In bytes.
	  */
	static final int HEADER_SIZE = 8;
	private static final int MAX_PENDING_PACKETS = 3;
}

/**
  * Thrown when packets are received by the defragmenter which cannot
  *  be reassembled.
  */
class BadPacketException extends Exception
{
	public BadPacketException(String m) { super(m); }
}


/**
  * Stores incomplete packets for defragmentation.  Code assumes that 
  *  incoming DatagramPackets have zero offsets.
  */
class Reassembler
{
	public Reassembler()
	{}

/*
	public boolean reassemble() throws BadPacketException
	{
		byte[] in = fragPacket.getData();
		if (fragPacket.getLength() <= HEADER_SIZE) {
			// reject
			throw new BadPacketException("Packet too small ("
					+fragPacket.getLength()+" bytes).");
		}
			
		// Read the header:
		short fSeqNum = readBytes(in, 0),
			fNFrag = readBytes(in, 2),
			fIFrag = readBytes(in, 4),
			fCSum  = readBytes(in, 6);
			
		// Verify:
		if (fNFrag == 0) {
			throw new BadPacketException (
					"Fragment number zero reserved."
			);
		}
		if (fIFrag == 0) {
			// new packet started, initialize:
			if (iFrag != 0) System.err.println(
				"Warning: new packet started, abandoning previous packet.");
			if (seqNum != fSeqNum) System.err.println(
				"Warning: sequence number mismatch.  Expected "+seqNum
					+", got "+fSeqNum
			);
			nextPacket();
			seqNum = fSeqNum;
			nFrag = fNFrag;
		} else if (iFrag != fIFrag) {
			BadPacketException bpe = new BadPacketException(
					"Fragment index out of order.  "
					+"Expected "+iFrag+", got "+fIFrag
			);
			nextPacket();
			++seqNum;				
			throw bpe;
		} else if (fSeqNum != seqNum) {
			throw new BadPacketException(
			"Packet sequence number does not match.  Rejecting fragment.");
		}
		
		// Reassemble:
		defragPacket.setLength(
				defrag(
						in,
						fragPacket.getLength() - HEADER_SIZE,
						HEADER_SIZE,
						defragPacket.getData(),
						defragPacket.getLength()
				)
		);							
		
		if (++iFrag == nFrag) {
			boolean result = checkCsum(fCSum, defragPacket); 
			iFrag = 0; // prevents warning when new packet recvd.
			++seqNum;
			return result;
		}
		return false;
	}
*/


	/**
	  * The packet is ready for transmission.  To mark incomplete, use
	  *  {@link #startPacket(DatagramPacket) }.
	  */
	public void markComplete() { complete = true; }
	public boolean isComplete() { return complete; }
	
	public DatagramPacket getCompletedPacket() { return pendingPacket; }
	public short getSeqNum() { return seqNum; }
	
	boolean getDebugFlag() { return debugFlag; }
	void setDebugFlag(boolean debugFlag) { this.debugFlag = debugFlag; }
	
	public void startPacket(DatagramPacket dp) throws BadPacketException
	{
		byte[] in = dp.getData();
		if (dp.getLength() <= Fragmenter.HEADER_SIZE) badPacket(
			"Packet too small ("+dp.getLength()+" bytes).");
		
		// Initialize:		
		recvFrags.clear();
		complete = false;		
			
		// Read the header:
		seqNum = Fragmenter.readBytes(in, 0);
		nFrag = Fragmenter.readBytes(in, 2);
		
		short fIFrag = Fragmenter.readBytes(in, 4);
		if (debugFlag)
			System.err.println("Starting packet "+seqNum
					+" ("+fIFrag+" of "+nFrag+')');		
		if (nFrag != 1 && fIFrag == nFrag - 1) {
			// Special case.  Last fragment may not be the prototypical size.
			// Copy into our pending packet for now (sans header):
			System.arraycopy(
					in,
					Fragmenter.HEADER_SIZE,
					pendingPacket.getData(),
					0,
					dp.getLength()-Fragmenter.HEADER_SIZE
			);
			pendingPacket.setLength(dp.getLength()-Fragmenter.HEADER_SIZE);
			protoSize = PROTO_SIZE_UNKNOWN; // signals special case	
			recvFrags.set(fIFrag);
		} else {
			protoSize = dp.getLength() - Fragmenter.HEADER_SIZE;
			complete = reassemble(dp);
		}
	}

	/**
	  * Assemble the fragment into the pending packet.
	  *  Assumes startPacket() has already been called, and the sequence number
	  *  matches.
	  *
	  * @return true if the packet is fully reassembled.
	  */
	public boolean reassemble(DatagramPacket fragPacket)
		throws BadPacketException
	{
		byte[] in = fragPacket.getData();
		if (fragPacket.getLength() <= Fragmenter.HEADER_SIZE) {
			// reject
			throw new BadPacketException("Packet too small ("
					+fragPacket.getLength()+" bytes).");
		}
			
		// Read the header:
		short
			fNFrag = Fragmenter.readBytes(in, 2),
			fIFrag = Fragmenter.readBytes(in, 4),
			fCSum  = Fragmenter.readBytes(in, 6);
			
//System.err.println("Reassy "+seqNum+" ("+fIFrag+" of "+nFrag+')');		
		// Verify:
		if (recvFrags.get(fIFrag)) badPacket(
			"Fragment index "+fIFrag+" already received for sequence "+seqNum);
		if (fNFrag == 0) badPacket("Fragment number zero reserved.");
		if (fNFrag != nFrag) badPacket(
			"Fragment number mismatch.  Expected "+nFrag+", got "+fNFrag);

		if (protoSize == PROTO_SIZE_UNKNOWN) {
			// Packet in pending is last packet.  Move it to its correct spot.
			protoSize = fragPacket.getLength() - Fragmenter.HEADER_SIZE;
			int offset = protoSize * (nFrag - 1);
			System.arraycopy(
					pendingPacket.getData(),
					0,
					pendingPacket.getData(),
					offset,
					pendingPacket.getLength()
			);
			setPendingPacketLength(pendingPacket.getLength());
		} else if (fIFrag == nFrag - 1)
			// Last packet tells us header size:
			setPendingPacketLength(
					fragPacket.getLength() - Fragmenter.HEADER_SIZE
			);
		
		// Copy fragment into correct location:
		System.arraycopy(
				in,
				Fragmenter.HEADER_SIZE,
				pendingPacket.getData(),
				protoSize * fIFrag,
				fragPacket.getLength() - Fragmenter.HEADER_SIZE
		);
		
		recvFrags.set(fIFrag);

		if (recvFrags.cardinality() == nFrag) {
			complete = true;
			checkCsum(fCSum, pendingPacket); // throws if bad
		}

		return complete;
	}

	private void badPacket(String s) throws BadPacketException
	{ throw new BadPacketException(s); }

	private boolean checkCsum(short cs, DatagramPacket dp)
			throws BadPacketException
	{
		short localCS = Fragmenter.onesComplementChecksum(
				dp.getData(), dp.getLength());
		if (cs != localCS) badPacket(
				"Checksum does not match, rejecting packet."
		);
		return true;
	}
	
	private void setPendingPacketLength(int lastPacketLength)
	{
		pendingPacket.setLength(protoSize * (nFrag - 1) + lastPacketLength);
	}
		
	//***********************  Data Members  *****************************//
	private short nFrag, seqNum;
	/**
	  * The size of N-1 fragments, not including header.
	  */
	private int protoSize = PROTO_SIZE_UNKNOWN;
	/**
	  * Starts out with 'complete' packets, until startPacket() is called.
	  */
	private boolean complete = true;
	
	/**
	  * If true, emits some messages during packet receive/transmit.
	  */
	private boolean debugFlag = false;
	
	/**
	  * Used to remember received fragments.
	  */
	private final java.util.BitSet recvFrags = new java.util.BitSet();
	
	/** 
	  * The packet under construction.
	  */
	private final DatagramPacket pendingPacket 
			= new DatagramPacket(new byte[64*1024], 64*1024);
			
	private static final int PROTO_SIZE_UNKNOWN = -1;	
}
