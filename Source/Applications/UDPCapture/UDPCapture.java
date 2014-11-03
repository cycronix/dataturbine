

/*
Copyright 2011 Erigo Technologies LLC
Copyright 2013 Cycronix

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

// Added word-parsing option -w, MJM 1/31/2013
// Added digital packet signature (password) option -p, MJM 10/22/2014


import java.lang.NumberFormatException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.SignBytes;

import java.nio.ByteBuffer;


public class UDPCapture {
	DatagramSocket ds = null; //listen for data here
	Source src = null; 	//write data here
	Source srcR = null;	// secondary raw source if -r
	String serverAddress = "localhost:3333";
	// JPW 07/08/08: change "name" to "srcName"
	String srcName = new String("UDPCapture");
	// JPW 07/08/08: add chanName
	String chanName = new String("c");
	
	int cache = 100;
	String mode = new String("none");
	int archive = 0;
	int ssNum = 4444;
	double lastTime = 0;
	double packetTime = 0;
	String wordfmt=null;
	boolean swapflag = false;		// word order swap flag.  false = BigEndian (java default)
	
	int nchan=1;					// num chans for -W multiplex. nchan=0 for auto multiplex Nchan per frame
	double deltaTime=0.;			// channel sample delta-time for multi-point frames
	boolean doRaw=false;			// force raw output (in addition to -W words)
	String Marker=null;				// optional prepend marker to output
	
	// JPW 01/12/2007: using "-i" flag, user can specify a list of IP addresses from which to accepts packets
	Vector<InetAddress> allowedIPAddresses = null;
	
	// JPW 01/12/2007: using "-l" flag, user can specify either:
	//                 - the required exact size of a received packet
	//                 - the min,max size limit on received packets
	int packetExactSize = -1;
	int packetMinSize = -1;
	int packetMaxSize = -1;
	
	SignBytes signBytes = null;		// MJM Cycronix 10/2014
	String password = null;

	//--------------------------------------------------------------------------------------------------------
	public static void main(String[] arg) {
		new UDPCapture(arg).start();
	}
	
	//--------------------------------------------------------------------------------------------------------
	public UDPCapture(String[] arg) {
		boolean uniqueFlag=false;
		
		try {
	//	    System.err.println("");
			ArgHandler ah=new ArgHandler(arg);
			if (ah.checkFlag('h')) {
				throw new Exception("show usage");
			}
			if (ah.checkFlag('a')) {
				String serverAddressL=ah.getOption('a');
				if (serverAddressL!=null) serverAddress=serverAddressL;
			}
			if (ah.checkFlag('m')) {					// optionally prepend data marker
				String markerL=ah.getOption('m');
				if (markerL!=null) Marker=markerL;
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
			if (ah.checkFlag('j')) {
				String ncf=ah.getOption('j');
				if (ncf!=null) cache=Integer.parseInt(ncf);
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
			if (ah.checkFlag('w') || ah.checkFlag('W')) {
				if(ah.checkFlag('W')) 	{  swapflag=false; wordfmt=ah.getOption('W'); }		// big endian (java default)
				else					{  swapflag=true; wordfmt=ah.getOption('w'); }		// little endian (intel)
			}
			if(ah.checkFlag('R')) {
				if(wordfmt != null) doRaw = true;		//  no extra effort if not in -W mode
			}
			if (ah.checkFlag('c')) {
				String nc=ah.getOption('c');
				if (nc!=null) nchan=Integer.parseInt(nc);
			}	
			if (ah.checkFlag('t')) {
				String st=ah.getOption('t');
				if (st!=null) deltaTime=Double.parseDouble(st);
			}
			if (ah.checkFlag('r')) {
				String st=ah.getOption('r');
				if (st!=null) deltaTime=1./Double.parseDouble(st);
			}
//			System.err.println("deltatime: "+deltaTime);
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
			if (ah.checkFlag('p')) {
				password = ah.getOption('p');
			}
			if (ah.checkFlag('u')) {
				uniqueFlag = true;
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
			System.err.println(" -m <Marker>            : prepend line marker string to output");
			System.err.println("                default : none");
			System.err.println(" -n <source name>       : name of RBNB source to write packets to");
			System.err.println("                default : " + srcName);
			System.err.println(" -o <channel name>      : name of RBNB channel to write packets to");
			System.err.println("                default : " + chanName);
			System.err.println(" -p <password>          : optional password (UDPCaster must match)");
			System.err.println("                default : none");
			System.err.println(" -u                     : require unique packets (along with password)");
			System.err.println("                default : false");
			System.err.println(" -s <server socket>     : socket number to listen for UDP packets on");
			System.err.println("                default : 4444");
			System.err.println(" -k <num>               : archive (disk) frames, append");
			System.err.println(" -K <num>               : archive (disk) frames, create");
			System.err.println(" -j <num>               : cache (RAM) frames");
			System.err.println("                default : 0 (no archiving)");
			System.err.println(" -w <word fmt>          : j,i,I,f,F for i16,I32,I64,f32,F64 respectively");
			System.err.println("                default : byteArray");
			System.err.println(" -W <word fmt>          : same as -w with reverse byte order");
			System.err.println(" -c <nchan>             : chans per multiplexed frame (-w,-W). ");
			System.err.println("                default : default=1, nchan=0 for auto");
			System.err.println(" -t <deltaTime>         : sampling interval for multiplexed frames (-w,-W)");
			System.err.println("                default : default = heuristic based on packet arrival time");
			System.err.println(" -r <sampRate>          : sample rate for multiplexed frames (-w,-W)");
			System.err.println("                default : default = 1/deltaTime");
			System.err.println(" -R                     : force raw output (in addition to -W words)");
			System.err.println(" -i <host1[,host2,...]> : optional list of acceptable IP addresses from which packets will be accepted");
			System.err.println(" -l <num1[,num2]>       : specify required size of received packets, two options:");
			System.err.println("                        : a) If only num1 provided, specifies required packet size");
			System.err.println("                        : b) If num1,num2 provided, specifies min,max packet size range");
			RBNBProcess.exit(0);
		}
		
		System.err.println("\nDatagram Socket: " + ssNum);
		System.err.println("RBNB Server: " + serverAddress);
		System.err.println("Source name: " + srcName);
		System.err.println("Channel name: " + chanName);
		System.err.println("Cache size: " + cache);
		System.err.println("Archive mode: " + mode);
		System.err.println("Archive size: " + archive);
		System.err.println("DataWord format: "+wordfmt);
		if (allowedIPAddresses != null) {
		    System.err.println("Only accept packets from the following addresses:");
		    for (Enumeration e = allowedIPAddresses.elements(); e.hasMoreElements();) {
		         InetAddress addr = (InetAddress)e.nextElement();
		         System.err.println("\t" + addr.toString());
		    }
		}
		
		if(password != null) signBytes = new SignBytes("SHA-1", password, uniqueFlag);		// MJM Cycronix 10/2014
		
		if (packetExactSize > -1) {
		    System.err.println("Packets must be exactly " + packetExactSize + " bytes.");
		} else if (packetMinSize > -1) {
		    System.err.println("Packets must be in the range " + packetMinSize + " <= packet-size <= " + packetMaxSize);
		}
		
		try {
			ds=new DatagramSocket(ssNum); //open port for incoming UDP
			src=new Source(cache,mode,archive);
			
			src.OpenRBNBConnection(serverAddress,srcName);
            System.err.println("\nOpened RBNB connection:" + src.GetServerName() + "\n");
            if(doRaw) { 		 // open second raw data source
    			srcR=new Source(cache,mode,archive);
    			srcR.OpenRBNBConnection(serverAddress,srcName+"_raw");
                System.err.println("Secondary Raw Source:" + srcR.GetClientName() + "\n");
            }
		} catch (Exception e) { e.printStackTrace(); }
                
	}
	
	
	//--------------------------------------------------------------------------------------------------------
	public void start() {
		try {
			DatagramPacket dp=new DatagramPacket(new byte[65536],65536);
			double oldTime=0.;
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
					
					double time=System.currentTimeMillis()/1000.0;
					packetTime = time - oldTime;		// for estimating dt in processW()
					oldTime = time;

					if(lastTime == 0.) lastTime = time;					// startup transient
					if(deltaTime > 0.) time = lastTime;					// force time-step to spec
					else if (time <= lastTime) time=lastTime+0.01;		// avoid duplicate timestamps (was +0.01)
					lastTime=time;										// over-ridden if processW with deltaTime

					byte[] data = readUDP(dp);
					if(password != null) {
						data = signBytes.getSigned(data, password);		// MJM Cycronix 10/2014
						if(data == null) {
							System.err.println("UDPCapture bad signature! (discarded)");
							continue;
						}
					}
					System.err.println("received "+data.length+" bytes at "+com.rbnb.api.Time.since1970(time));

					ChannelMap cm;
					if(wordfmt==null || wordfmt.equals("")) cm=process(time,data);
					else									cm=processW(time,data,deltaTime,wordfmt);
					if (cm!=null) src.Flush(cm);
					
					if(doRaw) srcR.Flush(process(time,data));		// secondary raw data source
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		finally{ src.CloseRBNBConnection(); }
	}
	
	//--------------------------------------------------------------------------------------------------------
	// readUDP:  get UDP data into buffer 		(MJM 1/2014)
	public byte[] readUDP(DatagramPacket dp) {
		byte[] data=new byte[dp.getLength()];		// accessed from process() and/or processW()
		System.arraycopy(dp.getData(),dp.getOffset(),data,0,dp.getLength());
		return data;
	}
	
	//--------------------------------------------------------------------------------------------------------
	//method may be overridden to convert data into more meaningful values
	public ChannelMap process(double time, byte[] data) {
		ChannelMap cm=new ChannelMap();
		cm=new ChannelMap();
		try {
			int idx=cm.Add(chanName);
			cm.PutTime(time,0);
			if(Marker == null) cm.PutDataAsByteArray(idx,data);
			else {		// prepend Marker plus current time fields to make CSVDemux happy
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");		// CSVDemux default time format
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
				String prepend = Marker + "," + df.format(new Date()) + ",";		// marker plus time
				byte[] prependb = prepend.getBytes();
				byte[] concat = new byte[prependb.length + data.length];
				System.arraycopy(prependb, 0, concat, 0, prependb.length);
				System.arraycopy(data, 0, concat, prependb.length, data.length);
				cm.PutDataAsByteArray(idx,concat);
			}
		} catch (Exception e) { e.printStackTrace(); }
		return cm;
	}
	
	//--------------------------------------------------------------------------------------------------------
	// process into words (vs bytearray).  MJM 1/2013
	public ChannelMap processW(double time, byte[] data, double dt, String wordfmt) {
		ChannelMap cm=new ChannelMap();
		try {
//			cm.PutTime(time,0);		// move to inner loops

			//cm[0].PutTimeAuto("timeofday");
			
			int i=0;
			int idx;
			java.nio.ByteOrder border = java.nio.ByteOrder.BIG_ENDIAN;
			if(swapflag) border = java.nio.ByteOrder.LITTLE_ENDIAN;
	    	int wsize = 4;			// word size
	    	
			if		(wordfmt.equals("f")) wsize = 4;
			else if	(wordfmt.equals("F")) wsize = 8;
			else if	(wordfmt.equals("i")) wsize = 4;
			else if	(wordfmt.equals("I")) wsize = 8;
			else if	(wordfmt.equals("j")) wsize = 2;
			else if	(wordfmt.equals("J")) wsize = 2;
			else  {
				System.err.println("unrecognized word format!");
				return null;
			}

			int nword = data.length / wsize;
			int nch = nchan;
			if(nchan == 0) 	{ nch = nword; nword = 1; }		// mux-by-1, auto-nchan
			else  			{ nword /= nchan; }				// block-muxed, spec-nchan
			if((dt == 0.) && (nword > 1)) dt = packetTime / nword;		// est multi-word blocks w/o spec dt (single-word blocks @ systime)
//			System.err.println("nch: "+nch+", nword: "+nword+", dt: "+dt);
			for(int j=0; j<nch; j++) {
				if(nch==1) 	idx = cm.Add(chanName);
				else		idx=cm.Add(cname(j));
				double t = time;
				for(i=0; i<nword; i++) {
					cm.PutTime(t,0);
					int ipt = j*nword + i;
					if(wordfmt.equals("f")) {
						float  dd[] = new float[1];
						dd[0] = ByteBuffer.wrap(data, ipt*4, 4).order(border).getFloat();
						cm.PutDataAsFloat32(idx,dd);
					}
					else if	(wordfmt.equals("F")) {
						double dd[] = new double[1]; 
						dd[0] = ByteBuffer.wrap(data, ipt*8, 8).order(border).getDouble();
//						dd[0]=buf.getDouble(i*8); 
						cm.PutDataAsFloat64(idx,dd);
					}
					else if	(wordfmt.equals("i")) {
						int    dd[] = new int[1];    
						dd[0] = ByteBuffer.wrap(data, ipt*4, 4).order(border).getInt();
						cm.PutDataAsInt32(idx,dd);
					}
					else if	(wordfmt.equals("j")) {
						short    dd[] = new short[1]; 
						dd[0] = ByteBuffer.wrap(data, ipt*2, 2).order(border).getShort();
						cm.PutDataAsInt16(idx,dd);
					}
					else if	(wordfmt.equals("J")) {		// read as short, put as int (rbnbPlot etc don't like shorts)
						short    sdd[] = new short[1];    
						sdd[0] = ByteBuffer.wrap(data, ipt*2, 2).order(border).getShort();
						int idd[] = new int[sdd.length];
						for(int k=0; k<sdd.length; k++) idd[k] = sdd[k];
						cm.PutDataAsInt32(idx,idd);
					}
					else if	(wordfmt.equals("I")) {
						long   dd[] = new long[1];   
						dd[0] = ByteBuffer.wrap(data, ipt*8, 8).order(border).getLong();
						cm.PutDataAsInt64(idx,dd);
					}
					t += dt;			// time-increment multi-point blocks
				}
				lastTime = t;		// skootch
			}

//			cm.PutDataAsByteArray(idx,data);
		} catch (Exception e) { e.printStackTrace(); }
		return cm;
	}
	
	//--------------------------------------------------------------------------------------------------------
	private String cname(int idx) {
    	String cname = null;
    	cname = chanName+idx;
    	return(cname);
	}
	
} //end class UDPCapture
