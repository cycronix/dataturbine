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
  ***	Name :	TCPServerPlayerOutput	                 	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   TCPServerPlayerOutput is a subclass of PlayerOutput,	***
  ***	used to output RBNB data to connected TCP clients.  A	***
  ***	server socket accepts client connections, which are	***
  ***	then stored in a hashtable.  When new RBNB data comes,	***
  ***	it is sent to all the clients.				***
  ***								***
  ***	Modification History					***
  ***	06/16/2005	JPW	Created; much of this code was	***
  ***				taken from TCPCaster		***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.rbnb.sapi.ChannelMap;

class TCPServerPlayerOutput extends PlayerOutput {
    
    // Server port that TCP clients will connect to
    private int serverPort;
    
    // Thread which accepts connections from TCP client applications
    private Thread acceptThread = null;
    
    // Server socket; this is what TCP clients will connect to
    private ServerSocket serverSocket = null;
    
    // Hashtable to store client Socket connections and OutputStreams
    private Hashtable clientInfo = new Hashtable();
    
    // Tells the thread accepting client connections that we are still running.
    private boolean bKeepRunning = true;
    
    // Is the Server socket still running?
    private boolean bConnected = false;
    
/*
  *****************************************************************
  ***								***
  ***	Name :	TCPServerPlayerOutput		                 	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires a TCP server port.		***
  ***								***
  *****************************************************************
*/
    public TCPServerPlayerOutput(int serverPortI) throws Exception {
	connect(serverPortI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	connect  		                 	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Start the TCP server port.				***
  ***								***
  *****************************************************************
*/
    public final void connect(int serverPortI) throws Exception {
	serverPort = serverPortI;
	serverSocket = new ServerSocket(serverPort);
	// Set a timeout so the ServerSocket's accept() method will return
	// after waiting for 1 second
	serverSocket.setSoTimeout(1000);
	// Start the thread which will accept client TCP connections
	Runnable serverSocketRunnable = new Runnable() {
	    public void run() {
		runAccept();
	    }
	};
	acceptThread = new Thread(serverSocketRunnable);
	acceptThread.start();
	bConnected = true;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect         			        ***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Disconnect client socket connections and the server	***
  ***	socket.							***
  ***								***
  *****************************************************************
*/
    public void disconnect() {
	
	if ( (!bConnected)                &&
	     (serverSocket == null)       &&
	     (clientInfo.size() == 0) )
	{
	    return;
	}
	
	bKeepRunning = false;
	
	// Wait for the TCP server socket thread to exit
	if ( (acceptThread != null) &&
	     (Thread.currentThread() != acceptThread) )
	{
	    try {
		System.err.println(
		    "Waiting for the TCP server thread to stop...");
		acceptThread.join(3000);
	    } catch (InterruptedException ie) {}
	}
	System.err.println("TCP server thread has stopped.");
	acceptThread = null;
	
	// Close the Socket connection and OutputStream to each client
	for (Enumeration keys = clientInfo.keys(); keys.hasMoreElements();) {
	    Socket socket = (Socket)keys.nextElement();
	    OutputStream os = (OutputStream)clientInfo.get(socket);
	    try {
		os.close();
		socket.close();
	    } catch (IOException ioe) {
		System.err.println("Caught exception closing connections:");
		ioe.printStackTrace();
	    }
	}
	clientInfo.clear();
	
	// Close the server socket
	if (serverSocket != null) {
	    try {
		serverSocket.close();
	    } catch (IOException ioe) {
		System.err.println(
		    "Caught exception closing server socket connection:");
		ioe.printStackTrace();
	    }
	    serverSocket = null;
	}
	
	bConnected = false;
	
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	setOutputChannels               		***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :  June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
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
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Output RBNB data from the provided ChannelMap to	***
  ***	all connected TCP clients.				***
  ***								***
  *****************************************************************
*/
    public void outputData(ChannelMap outMap) throws Exception {
	
	if (!bConnected) {
	    throw new Exception("The server socket is no longer connected.");
	}
	
	if ( (outMap == null) || (outMap.NumberOfChannels() == 0) ) {
	    return;
	}
	
	for (int i = 0; i < outMap.NumberOfChannels(); ++i) {
	    int length = 0;
	    // Extract the data from the ChannelMap using GetDataAsByteArray()
	    // only if the type of the data is TYPE_BYTEARRAY. Otherwise, just
	    // treat the data as a blob of bytes.
	    if (outMap.GetType(i) != ChannelMap.TYPE_BYTEARRAY) {
		byte[] dataArray = outMap.GetData(i);
		if (dataArray == null) {
		    continue;
		}
		// Write data to the connected TCP client OutputStreams
		writeData(dataArray);
	    } else {
		byte[][] dataArray = outMap.GetDataAsByteArray(i);
		if (dataArray == null) {
		    continue;
		}
		for (int j = 0; j < dataArray.length; ++j) {
		    // Write data to the connected TCP client OutputStreams
		    writeData(dataArray[j]);
		}
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	writeData     			        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Write the data to all output TCP clients.		***
  ***								***
  *****************************************************************
*/
    private void writeData(byte[] dataI) {
	
	synchronized (clientInfo) {
	    Vector socketsToRemove = new Vector();
	    for (Enumeration sockets = clientInfo.keys();
		 sockets.hasMoreElements();)
	    {
		Socket socket = (Socket)sockets.nextElement();
		OutputStream out = (OutputStream)clientInfo.get(socket);
		
		//If Socket is closed, remove it from Hashtable
		try {
		    out.write(dataI);
		} catch (Exception e) {
		    // Don't remove the socket here since we are in the midst
		    // of an Enumeration over the socket entries!!!
		    socketsToRemove.addElement(socket);
		    System.err.println(
		        "Exception caught writing to client socket\n" +
			"(the client Socket connection might have closed)\n" +
			"Exception:\n" +
			e);
		}
	    }
	    // Remove any sockets that have closed from the Hashtable
	    if (socketsToRemove.size() > 0) {
		for (int i = 0; i < socketsToRemove.size(); ++i) {
		    Socket socket =
		        (Socket)socketsToRemove.elementAt(i);
		    clientInfo.remove(socket);
		    System.err.println(
			"TCP client connection dropped: " +
			socket.getInetAddress());
		}
	    }
	} // end synchronized block
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	runAccept     			        	***
  ***	By   :	John Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2005					***
  ***								***
  ***	Copyright 2005 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Main TCP Server loop.  Accept connections from TCP	***
  ***	clients.						***
  ***								***
  *****************************************************************
*/
    private void runAccept() {
	
	try {
	
	while (bKeepRunning) {
	    
	    Socket clientSocket = null;
	    try {
		clientSocket = serverSocket.accept();
	    } catch (SocketTimeoutException ste) {
		continue;
	    }
	    
	    // OutputStream to write to the client
	    OutputStream out = clientSocket.getOutputStream();
	    
	    synchronized (clientInfo) {
		// Add the client socket and OutputStream to the Hashtable
		clientInfo.put(clientSocket, out);
	    }
	    
	    System.err.println(
	        "TCP client connection accepted from " +
		clientSocket.getInetAddress());
	    
	}
	
	} catch (Exception e) {
	    System.err.println("Error accepting new TCP client connections:");
	    e.printStackTrace();
	    disconnect();
	}
	
    }
    
}

