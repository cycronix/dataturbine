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

// TCPCapture - receives TCP packets, writes them to a RBNB
// Based on EMF's UDPCapture application
// John P. Wilson
// 06/14/2011

//
// A simple way to try this out is to send messages out using netcat;
// for example, run netcat in server mode (port 4444) as follows:
//
//    nc -l -p 4444
//
// Then in netcat's terminal window, enter messages (standard input); these
// messages will be cpatured by the TCPCapture and put into RBNB server.
//

import java.lang.NumberFormatException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

public class TCPCapture {
	Socket ds = null; //listen for data here
	InputStream fromTCPServer = null;  // read from TCP server
	Source src = null; //write data here
	String serverAddress = "localhost:3333";
	String srcName = new String("TCPCapture");
	String chanName = new String("TCP");
	int cache = 100;
	String mode = new String("none");
	int archive = 0;
	String tcpHost = "localhost";
	int tcpPort = 4444;
	double lastTime = 0;
	
	// JPW 01/12/2007: using "-l" flag, user can specify either:
	//                 - the required exact size of a received packet
	//                 - the min,max size limit on received packets
	int packetExactSize = -1;
	int packetMinSize = -1;
	int packetMaxSize = -1;
	
	// Interpret incoming bytes as Strings and flush String data to the
	// RBNB server?
	boolean bFlushStr = false;
	
	public static void main(String[] arg) {
		new TCPCapture(arg).start();
	}
	
	public TCPCapture(String[] arg) {
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
			if (ah.checkFlag('A')) {
				String tcpHostTemp = ah.getOption('A');
				if (tcpHostTemp!=null) tcpHost = tcpHostTemp;
			}
			if (ah.checkFlag('p')) {
				String portStr = ah.getOption('p');
				if (portStr!=null) tcpPort = Integer.parseInt(portStr);
				if (tcpPort < 1) throw new Exception("The TCP port must be an integer greater than 0.");
			}
			if (ah.checkFlag('S')) {
				bFlushStr = true;
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
			System.err.println(" -k <num>               : archive (disk) frames, append");
			System.err.println(" -K <num>               : archive (disk) frames, create");
			System.err.println("                default : 0 (no archiving)");
			System.err.println(" -l <num1[,num2]>       : Optional arguments to specify the required size of received packets");
			System.err.println("                        : Two options:");
			System.err.println("                        : a) If only num1 is provided, this specifies the number of bytes that must be in the received packet.");
			System.err.println("                        : b) If num1,num2 is provided, then num1 is treated as a minimum acceptable received packet size and num2 is a maximum acceptable received packet size.");
			System.err.println(" -A <TCP server addr>   : address of the TCP server to connect to");
			System.err.println("                default : localhost");
			System.err.println(" -p <port num>          : TCP server port number");
			System.err.println("                default : 4444");
			System.err.println(" -S                     : String mode; interpret incoming bytes as String data and flush Strings to RBNB server");
			RBNBProcess.exit(0);
		}
		System.err.println("Connect to TCP server at " + tcpHost + ":" + tcpPort);
		System.err.println("RBNB Server: " + serverAddress);
		System.err.println("Source name: " + srcName);
		System.err.println("Channel name: " + chanName);
		System.err.println("Cache size: " + cache);
		System.err.println("Archive mode: " + mode);
		System.err.println("Archive size: " + archive);
		if (bFlushStr) {
			System.err.println("Flush String data to RBNB server.");
		}
		if (packetExactSize > -1) {
		    System.err.println("Packets must be exactly " + packetExactSize + " bytes.");
		} else if (packetMinSize > -1) {
		    System.err.println("Packets must be in the range " + packetMinSize + " <= packet-size <= " + packetMaxSize);
		}
		
		try {
			ds=new Socket(tcpHost, tcpPort); // open the data socket
			fromTCPServer =  ds.getInputStream();
			src=new Source(cache,mode,archive);
			src.OpenRBNBConnection(serverAddress,srcName);
                        System.err.println("\nTCPCapture opened RBNB connection to " + src.GetServerName() + "\n");

		} catch (Exception e) { e.printStackTrace(); }
                
	}
	
	public void start() {
		try {
			while (true) {
				int numAvailable = 0;
				if ((numAvailable = fromTCPServer.available()) == 0) {
					continue;
				}
				byte[] data = new byte[numAvailable];
				int numRead = fromTCPServer.read(data);
				if (numRead == 0) {
					continue;
				}
				// Check packet size
				if ( (packetExactSize > -1) && (packetExactSize != numRead) ) {
				    System.err.println("Reject packet: packet size (" + numRead + " bytes) is not equal to " + packetExactSize);
				    continue;
				} else if ( (packetMinSize > -1) && ((numRead < packetMinSize) || (numRead > packetMaxSize)) ) {
				    System.err.println("Reject packet: packet size (" + numRead + " bytes) is not in the range " + packetMinSize + " <= packet-size <= " + packetMaxSize);
				    continue;
				}
				ChannelMap cm = new ChannelMap();
				cm.Add(chanName);
				double time=System.currentTimeMillis()/1000.0;
				System.err.println("received "+data.length+" bytes at "+com.rbnb.api.Time.since1970(time));
				if (time <= lastTime) {
					time=lastTime+0.001;
				}
				lastTime=time;
				cm.PutTime(time,0);
				if (bFlushStr) {
					String dataStr = new String(data);
					cm.PutDataAsString(0,dataStr + "\n");
				} else {
					cm.PutDataAsByteArray(0,data);
				}
				src.Flush(cm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
} //end class UDPCapture
