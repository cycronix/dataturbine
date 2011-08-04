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

// UDPChatSource - sends text messages as UDP packets to the specified
//                 host/port; text messages are read from System.in.
//                 This is based on UDPWrite
// EMF/JPW
// 07/23/07
// for Global Scan

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class UDPChatSource {
	BufferedReader br=null;
	DatagramSocket ds=null;
	DatagramPacket dp=null;
	
	String nickname = null;
	int hoursAdjustFromUTC = 0;
	
	private SimpleDateFormat dateFormat = null;
	private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	
	public static void main(String[] arg) {
		UDPChatSource uw=new UDPChatSource(arg);
		uw.start();
	}
	
	public UDPChatSource(String[] arg) {
		try {
			//parse args
			if (arg.length!=3) {
				System.err.println(
					"\nSend chat messages to a UDP port.  The format of the messages will be:\n" +
					"    [HH:mm (UTC)] <nickname> message\n\n" +
					"usage: java UDPChatSource <host> <port> <nickname>");
				System.exit(0);
			}
			nickname = arg[2];
			// We will just always use UTC; don't have hour adjustment as a user input
			// hoursAdjustFromUTC = Integer.parseInt(arg[3]);
			dateFormat = new SimpleDateFormat("HH:mm");
			dateFormat.setTimeZone(calendar.getTimeZone());
			//set up to read from standard input
			br=new BufferedReader(new InputStreamReader(System.in));
			//set up to write via UDP
			ds=new DatagramSocket(3456);
			InetSocketAddress isa=new InetSocketAddress(arg[0],Integer.parseInt(arg[1]));
			dp=new DatagramPacket(new byte[1024],1024);
			dp.setSocketAddress(isa);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void start() {
		try {
			String input=null;
			while (!(input=br.readLine()).equals("quit")) {
				String nextMsg =
				    "[" + dateFormat.format(calendar.getTime()) + "] <" + nickname + "> " + input + "\n";
				// NOTE: nextMsg must be < 1024 bytes
				if (nextMsg.length() > 1024) {
					throw new Exception("Total message length (including header info) must be less than 1024 bytes");
				}
				dp.setData(nextMsg.getBytes());
				ds.send(dp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

