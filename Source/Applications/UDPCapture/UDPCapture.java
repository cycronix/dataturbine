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

// UDPCapture - receives UDP packets, writes them to a RBNB
// EMF
// 3/31/05
// for IOScan

import java.lang.NumberFormatException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

public class UDPCapture {
	DatagramSocket ds = null; //listen for data here
	Source src = null; //write data here
	String serverAddress = "localhost:3333";
	// JPW 07/08/08: change "name" to "srcName"
	String srcName = new String("UDPCapture");
	// JPW 07/08/08: add chanName
	String chanName = new String("UDP");
	int cache = 100;
	String mode = new String("none");
	int archive = 0;
	int ssNum = 4444;
	double lastTime = 0;
	
	// JPW 01/12/2007: using "-i" flag, user can specify a list of IP addresses from which to accepts packets
	Vector<InetAddress> allowedIPAddresses = null;
	
	// JPW 01/12/2007: using "-l" flag, user can specify either:
	//                 - the required exact size of a received packet
	//                 - the min,max size limit on received packets
	int packetExactSize = -1;
	int packetMinSize = -1;
	int packetMaxSize = -1;
	
	public static void main(String[] arg) {
		new UDPCapture(arg).start();
	}
	
	public UDPCapture(String[] arg) {
		try {
		    	System.err.println("");
			ArgHandler ah=new ArgHandler(arg);
			if (ah.checkFlag('h')) {
				throw new Exception("show usage");
			}
			if (ah.checkFlag('a')) {
				String serverAddressL=ah.getOption('a');
				if (serverAddress!=null) serverAddress=serverAddressL;
			}
			if (ah.checkFlag('i')) {
				String hostStr = ah.getOption('i');
				if ( (hostStr != null) && (!hostStr.trim().equals("")) ) {
				    String[] hostStrArray = hostStr.trim().split(",");
				    if ( (hostStrArray != null) && (hostStrArray.length > 0) ) {
					for (int i = 0; i < hostStrArray.length; ++i) {
					    if ( (hostStrArray[i] != null) && (!hostStrArray[i].trim().equals("")) ) {
						String tempHostStr = hostStrArray[i].trim();
						try {
						    InetAddress addr = InetAddress.getByName(tempHostStr);
						    if (allowedIPAddresses == null) {
							allowedIPAddresses = new Vector<InetAddress>();
						    }
						    allowedIPAddresses.addElement(addr);
						} catch (UnknownHostException e) {
						    System.err.println("Exception resolving IP address for host \"" + tempHostStr + "\":\n" + e);
						}
					    }
					}
				    }
				}
			}
			if (ah.checkFlag('k')) {
				String naf=ah.getOption('k');
				if (naf!=null) archive=Integer.parseInt(naf);
				if (archive>0) {
					mode=new String("append");  // was "create"
					if (archive<cache) cache=archive;
				}
			}
			if (ah.checkFlag('K')) {
				String naf=ah.getOption('K');
				if (naf!=null) archive=Integer.parseInt(naf);
				if (archive>0) {
					mode=new String("create");  
					if (archive<cache) cache=archive;
				}
			}
			if (ah.checkFlag('l')) {
				String sizeStr = ah.getOption('l');
				// See if a ',' is separating min/max values
				int commaIdx = sizeStr.indexOf(',');
				if (commaIdx == -1) {
				    // This must be an exact packet length
				    try {
					packetExactSize = Integer.parseInt(sizeStr);
					if (packetExactSize <= 0) {
					    throw new NumberFormatException("Packet size must be greater than 0.");
					}
				    } catch (NumberFormatException e) {
					System.err.println("Exception parsing exact packet size:\n" + e);
					packetExactSize = -1;
				    }
				} else {
				    // Must contain "min,max" values
				    String minSizeStr = sizeStr.substring(0,commaIdx);
				    String maxSizeStr = sizeStr.substring(commaIdx + 1);
				    try {
					packetMinSize = Integer.parseInt(minSizeStr);
					if (packetMinSize <= 0) {
					    throw new NumberFormatException("Minimum packet size must be greater than 0.");
					}
					packetMaxSize = Integer.parseInt(maxSizeStr);
					if (packetMaxSize <= 0) {
					    throw new NumberFormatException("Maximum packet size must be greater than 0.");
					}
					if (packetMaxSize <= packetMinSize) {
					    throw new NumberFormatException("Max packet size must be greater than the min packet size.");
					}
				    } catch (NumberFormatException e) {
					System.err.println("Exception parsing packet sizes:\n" + e);
					packetMinSize = -1;
					packetMaxSize = -1;
				    }
				}
			}
			if (ah.checkFlag('n')) {
				String nameL = ah.getOption('n');
				if (nameL != null) {
				    srcName = nameL;
				}
			}
			if (ah.checkFlag('o')) {
				String chanNameL = ah.getOption('o');
				if (chanNameL != null) {
				    chanName = chanNameL;
				}
			}
			if (ah.checkFlag('s')) {
				String nss=ah.getOption('s');
				if (nss!=null) ssNum=Integer.parseInt(nss);
			}
		} catch (Exception e) {
			String msgStr = e.getMessage();
			if ( (msgStr == null) || (!msgStr.equalsIgnoreCase("show usage")) ) {
			    System.err.println("Exception parsing arguments:");
			    e.printStackTrace();
			}
			System.err.println("UDPCapture");
			System.err.println(" -h                     : print this usage info");
			System.err.println(" -a <server address>    : address of RBNB server to write packets to");
			System.err.println("                default : localhost:3333");
			System.err.println(" -n <source name>       : name of RBNB source to write packets to");
			System.err.println("                default : " + srcName);
			System.err.println(" -o <channel name>      : name of RBNB channel to write packets to");
			System.err.println("                default : " + chanName);
			System.err.println(" -s <server socket>     : socket number to listen for UDP packets on");
			System.err.println("                default : 4444");
			System.err.println(" -k <num>               : archive (disk) frames, append");
			System.err.println(" -K <num>               : archive (disk) frames, create");
			System.err.println("                default : 0 (no archiving)");
			System.err.println(" -i <host1[,host2,...]> : optional list of IP addresses from which packets will be accepted");
			System.err.println(" -l <num1[,num2]>       : Optional arguments to specify the required size of received packets");
			System.err.println("                        : Two options:");
			System.err.println("                        : a) If only num1 is provided, this specifies the number of bytes that must be in the received packet.");
			System.err.println("                        : b) If num1,num2 is provided, then num1 is treated as a minimum acceptable received packet size and num2 is a maximum acceptable received packet size.");
			RBNBProcess.exit(0);
		}
		
		System.err.println("\nDatagram Socket: " + ssNum);
		System.err.println("RBNB Server: " + serverAddress);
		System.err.println("Source name: " + srcName);
		System.err.println("Channel name: " + chanName);
		System.err.println("Cache size: " + cache);
		System.err.println("Archive mode: " + mode);
		System.err.println("Archive size: " + archive);
		if (allowedIPAddresses != null) {
		    System.err.println("Only accept packets from the following addresses:");
		    for (Enumeration e = allowedIPAddresses.elements(); e.hasMoreElements();) {
		         InetAddress addr = (InetAddress)e.nextElement();
		         System.err.println("\t" + addr.toString());
		    }
		}
		if (packetExactSize > -1) {
		    System.err.println("Packets must be exactly " + packetExactSize + " bytes.");
		} else if (packetMinSize > -1) {
		    System.err.println("Packets must be in the range " + packetMinSize + " <= packet-size <= " + packetMaxSize);
		}
		
		try {
			ds=new DatagramSocket(ssNum); //open port for incoming UDP
			src=new Source(cache,mode,archive);
			src.OpenRBNBConnection(serverAddress,srcName);
                        System.err.println("\nUDPCapture opened RBNB connection to " + src.GetServerName() + "\n");

		} catch (Exception e) { e.printStackTrace(); }
                
	}
	
	public void start() {
		try {
			DatagramPacket dp=new DatagramPacket(new byte[65536],65536);
			while (true) {
				ds.receive(dp);
				int packetSize = dp.getLength();
				InetAddress addr = dp.getAddress();
				if (packetSize > 0) {
					// JPW 01/12/2007: Check packet size
					if ( (packetExactSize > -1) && (packetExactSize != packetSize) ) {
					    System.err.println("Reject packet from " + addr + ": packet size (" + packetSize + " bytes) is not equal to " + packetExactSize);
					    continue;
					} else if ( (packetMinSize > -1) && ((packetSize < packetMinSize) || (packetSize > packetMaxSize)) ) {
					    System.err.println("Reject packet from " + addr + ": packet size (" + packetSize + " bytes) is not in the range " + packetMinSize + " <= packet-size <= " + packetMaxSize);
					    continue;
					}
					// JPW 01/12/2007: Check InetAddress
					if (allowedIPAddresses != null) {
					    if (!allowedIPAddresses.contains(addr)) {
						System.err.println("Reject packet from " + addr + ": not from an allowed IP address");
						continue;
					    }
					}
					ChannelMap[] cm=process(dp);
					for (int i=0;i<cm.length;i++) {
					    //System.err.println("flushing "+cm[i]);
					    if (cm[i]!=null) src.Flush(cm[i]);
					}
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	//method may be overridden to convert data into more meaningful values
	public ChannelMap[] process(DatagramPacket dp) {
		ChannelMap[] cm=new ChannelMap[1];
		cm[0]=new ChannelMap();
		try {
			byte[] data=new byte[dp.getLength()];
			System.arraycopy(dp.getData(),dp.getOffset(),data,0,dp.getLength());
			//System.err.println("received "+new String(data));
			// JPW 07/08/08: No longer hardwire the channel name to "UDP";
			//               use variable chanName (which the user can set
			//               via the "-o" command line option)
			int idx=cm[0].Add(chanName);
			double time=System.currentTimeMillis()/1000.0;
			System.err.println("received "+data.length+" bytes at "+com.rbnb.api.Time.since1970(time));
			//avoid duplicate timestamps
			if (time <= lastTime) time=lastTime+0.01;
			lastTime=time;
			cm[0].PutTime(time,0);
			//cm[0].PutTimeAuto("timeofday");
			cm[0].PutDataAsByteArray(idx,data);
		} catch (Exception e) { e.printStackTrace(); }
		return cm;
	}
	
} //end class UDPCapture
