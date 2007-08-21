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
  ***	Name :	UDPToTCP                         	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This class recieves UDP packets on one port, and    ***
  ***   transmits the received data over TCP on another port.   ***
  ***								***
  ***	Modification History:					***
  ***	09/28/2004	JPW	In closeClient():		***
  ***				In order to compile under J#,	***
  ***				(which only supports up to Java	***
  ***				version 1.1.4) replace calls to	***
  ***				Vector.remove(index) with a	***
  ***				combination of calls to:	***
  ***				Vector.elementAt(index)		***
  ***				Vector.removeElementAt(index)	***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

import java.io.DataOutputStream;
import java.io.InterruptedIOException;
import java.io.IOException;

import java.net.*;

import java.util.Vector;

public class UDPToTCP {

    private ServerSocket servSock = null;
    private DatagramSocket udpSock = null;
    private InetAddress udpAdd = null;
    private int udpPort;
    private int tcpPort;

    private Vector clients = new Vector();
    private Vector doss = new Vector();

    private static final int BUFFER_SIZE = 5000;

    private Thread serverThread = null;
    private Thread sessionThread = null;
    private volatile boolean stopRequested = false;

    private U2TProcessor processor = new DummyU2TProcessor();

/*
  *****************************************************************
  ***								***
  ***	Name :	UDPToTCP                         	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Constructors.                                       ***
  ***								***
  *****************************************************************
*/
    public UDPToTCP(InetAddress udpAddI, int udpPortI) {
	this (udpAddI, udpPortI, udpPortI + 50);
    }

    public UDPToTCP(InetAddress udpAddI, int udpPortI, int tcpPortI) {
	udpAdd = udpAddI;
	udpPort = udpPortI;
	tcpPort = tcpPortI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	addU2TProcessor                     	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	September, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***      Allows client class to attach a U2TProcessor to this ***
  ***   object, to receive update messages and be able to       ***
  ***   process data received on the UDP port.                  ***
  ***								***
  *****************************************************************
*/
    public void addU2TProcessor(U2TProcessor processorI) {
	if (processorI != null) {
	    processor = processorI;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	start                           	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***      Starts server thread and session thread.             ***
  ***								***
  *****************************************************************
*/
    public void start() {
	stopRequested = false;

	if (serverThread == null || !serverThread.isAlive()) {
	    try {
		openTCP();
		startTCPServer();
	    } catch (Exception e1) {
		processor.statusUpdated(new U2TUpdate(U2TUpdate.TCP_SERVER_EXCEPTION,
						      e1.toString()));
		return;
	    }
	}

	if (sessionThread == null || !sessionThread.isAlive()) {
	    try {
		openUDP();
		startSession();
	    } catch (Exception e2) {
		processor.statusUpdated(new U2TUpdate(U2TUpdate.UDP_SOCKET_EXCEPTION,
						      e2.toString()));
		return;
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stopAndClose                           	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	September, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***      Stops threads, closes and cleans up after the        ***
  ***   sockets.                                                ***
  ***								***
  ***	 DO NOT CALL ANY OTHER UDPToTCP METHOD AFTER THIS ONE   ***
  ***								***
  *****************************************************************
*/
    public void stopAndClose() {
	// first, stop the threads which are running
	stopRequested = true;

	// if any I/O is still blocked on the server socket
	// or the UDP socket, we will need to unblock it
	// to halt the threads.  (Darned Java, which hasn't
	// got any non-blocking I/O!)
	if (servSock != null) {
	    try {
		servSock.close();
	    } catch (Exception e1) {}
	}
	processor.statusUpdated(new U2TUpdate(U2TUpdate.TCP_SERVER_CLOSED,
					      "The TCP server has closed."));

	if (udpSock != null) {
	    try {
		udpSock.close();
	    } catch (Exception e2) {}
	}
	processor.statusUpdated(new U2TUpdate(U2TUpdate.UDP_SOCKET_CLOSED,
					      "The UDP socket is now closed."));

	// now we have to close the client sockets and data streams
	DataOutputStream dos = null;
	Socket client = null;
	String clientAdd = null;

	synchronized (doss) {
	    for (int i = 0; i < doss.size(); i++) {
		dos = (DataOutputStream) doss.elementAt(i);
		client = (Socket) clients.elementAt(i);
		clientAdd = client.getInetAddress().getHostName();

		try {
		    dos.close();
		} catch (Exception e1) {
		    // don't post error message; 
		    //we're disconnecting this client anyway
		}
		
		try {
		    client.close();
		} catch (Exception e2) {
		    // don't post error message; 
		    //we're disconnecting this client anyway
		}

		processor.statusUpdated(new U2TUpdate(U2TUpdate.CLIENT_DISCONNECTED,
						      "Client " + clientAdd +
						      " disconnected."));
	    } // end for loop over i

	    doss.removeAllElements();
	    clients.removeAllElements();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	openTCP                          	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Opens the TCP server socket.                        ***
  ***								***
  *****************************************************************
*/
    private void openTCP() throws IOException {
	servSock = new ServerSocket(tcpPort);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	openUDP                          	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Opens the UDP socket.                               ***
  ***								***
  *****************************************************************
*/
    private void openUDP() throws IOException, SocketException {
	if (udpAdd.isMulticastAddress()) {
	    MulticastSocket ms = new MulticastSocket(udpPort);
	    ms.joinGroup(udpAdd);
	    udpSock = (DatagramSocket) ms;
	} else {
	    udpSock = new DatagramSocket(udpPort, udpAdd);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	startTCPServer                     	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method starts a thread to handle incoming      ***
  ***   connection requests.                                    ***
  ***								***
  *****************************************************************
*/
    private void startTCPServer() {
	Runnable r = new Runnable() {
	      public void run() {
		  acceptRequests(); // loops
	      }
	  };
      
      serverThread = new Thread(r);
      serverThread.start();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	acceptRequests                       	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method handles incoming connection requests    ***
  ***   by adding their Socket objects to clients list.         ***
  ***								***
  *****************************************************************
*/
    private void acceptRequests() {
	while (!stopRequested) {
	    try {
		Socket client = servSock.accept();
		String clientAdd = client.getInetAddress().getHostName();
		synchronized (doss) {
		    clients.addElement(client);
		    doss.addElement(new DataOutputStream(client.getOutputStream()));
		}
		processor.statusUpdated(new U2TUpdate(U2TUpdate.CLIENT_CONNECTED,
						      "Client " + clientAdd + 
						      " connected."));
	    } catch (InterruptedIOException iioe) {
		// occurs when socket is closed by stopAndClose()
		break;
	    } catch (Exception e1) {
		processor.statusUpdated(new U2TUpdate(U2TUpdate.TCP_SERVER_EXCEPTION,
						      e1.toString()));
		break;
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	startSession                     	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method starts pumping data in a new thread.    ***
  ***								***
  *****************************************************************
*/
    private void startSession() {
	Runnable r = new Runnable() {
	      public void run() {
		  runSession(); // loops
		  // report session thread stopped
	      }
	  };
      
      sessionThread = new Thread(r);
      sessionThread.start();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	runSession                       	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Enters a loop, pumps data from UDP to TCP, with     ***
  ***   optional processing by the U2TProcessor.                ***
  ***								***
  *****************************************************************
*/
    private void runSession() {
	DatagramPacket dPacket = null;
	byte[] data = null;
	byte[] buffer = new byte[BUFFER_SIZE];

	while (!stopRequested) {
	    dPacket = new DatagramPacket(buffer, BUFFER_SIZE);
	    
	    try {
		udpSock.receive(dPacket);
	    } catch (InterruptedIOException iioe) {
		// occurs when socket is closed by stopAndClose()
		break;
	    } catch (Exception e1) {
		processor.statusUpdated(new U2TUpdate(U2TUpdate.UDP_SOCKET_EXCEPTION,
						      e1.toString()));
		break;
	    }

	    data = new byte[dPacket.getLength()];

	    if (data.length != 0) {
		System.arraycopy
		    (dPacket.getData(),
		     0,
		     data,
		     0,
		     dPacket.getLength());
		
		data = processor.process(data);
		tcpOut(data);
	    } else {
		// is this an error state?
		// ignoring for now.
	    }
	} // end while
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	tcpOut                          	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method sends the data to all of the TCP        ***
  ***   clients.                                                ***
  ***								***
  *****************************************************************
*/
    private void tcpOut(byte[] data) {
	synchronized (doss) {
	    for (int i = 0; i < doss.size(); i++) {
		DataOutputStream dos = null;
		try {
		    dos = (DataOutputStream) doss.elementAt(i);
		    dos.write(data, 0, data.length);
		    dos.flush();
		} catch (Exception e) {
		    String clientAdd = ((Socket) clients.elementAt(i)).getInetAddress().getHostName();
		    processor.statusUpdated(new U2TUpdate(U2TUpdate.CLIENT_EXCEPTION,
							  "Client " + clientAdd + 
							  "encountered error " +
							  e.toString() + " -- closing..."));
		    closeClient(i);
		    i--;
		}
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	closeClient                          	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method shuts down a particular client and      ***
  ***   removes it from the vectors.                            ***
  ***								***
  ***	Modification History:					***
  ***	09/28/2004	JPW	In order to compile under J#,	***
  ***				(which only supports up to Java	***
  ***				version 1.1.4) replace calls to	***
  ***				Vector.remove(index) with a	***
  ***				combination of calls to:	***
  ***				Vector.elementAt(index)		***
  ***				Vector.removeElementAt(index)	***
  ***								***
  *****************************************************************
*/
    private void closeClient(int index) {
	DataOutputStream dos = null;
	Socket client = null;

	synchronized (doss) {
	    // JPW 09/28/2004: To make the code Java 1.1.4 compliant, replace
	    //                 calls to Vector.remove(index) with a combination
	    //                 of two older Java methods:
	    //                     Vector.elementAt(index)
	    //                     Vector.removeElementAt(index)
	    // dos = (DataOutputStream) doss.remove(index);
	    // client = (Socket) clients.remove(index);
	    dos = (DataOutputStream)doss.elementAt(index);
	    client = (Socket) clients.elementAt(index);
	    doss.removeElementAt(index);
	    clients.removeElementAt(index);
	}
	String clientAdd = client.getInetAddress().getHostName();

	try {
	    dos.close();
	} catch (Exception e1) {
	    // don't post error message; 
	    //we're disconnecting this client anyway
	}
	
	try {
	    client.close();
	} catch (Exception e2) {
	    // don't post error message; 
	    //we're disconnecting this client anyway
	}

	processor.statusUpdated(new U2TUpdate(U2TUpdate.CLIENT_DISCONNECTED,
					      "Client " + clientAdd +
					      " disconnected."));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	DummyU2TProcessor                 	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	September, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       A dummy processor, in case client class does not    ***
  ***   provide one.                                            ***
  ***								***
  *****************************************************************
*/
    private class DummyU2TProcessor implements U2TProcessor {
	
	DummyU2TProcessor() {}

	public void statusUpdated(U2TUpdate update) {}

	public byte[] process(byte[] data) {
	    return data;
	}

    }

}
