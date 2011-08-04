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

// UDPWrite - sends UDP packets to the specified host/port
//            reads input from System.in
// EMF
// 4/5/05
// for IOScan

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPWrite {
	BufferedReader br=null;
	DatagramSocket ds=null;
	DatagramPacket dp=null;
	
	public static void main(String[] arg) {
		UDPWrite uw=new UDPWrite(arg);
		uw.start();
	}
	
	public UDPWrite(String[] arg) {
		try {
			//parse args
			if (arg.length!=2) {
				System.err.println("usage: java UDPWrite <host> <port>");
				System.exit(0);
			}
			//set up to read from standard input
			br=new BufferedReader(new InputStreamReader(System.in));
			//set up to write via UDP
			ds=new DatagramSocket(3456);
			InetSocketAddress isa=new InetSocketAddress(arg[0],Integer.parseInt(arg[1]));
			dp=new DatagramPacket(new byte[256],256);
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
				input=input+"\n";
				dp.setData(input.getBytes());
				ds.send(dp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
