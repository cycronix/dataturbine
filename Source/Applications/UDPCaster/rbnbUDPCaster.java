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


import com.rbnb.udpcaster.UDPCaster;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

/******************************************************************************
 * Utility class for creating an instance of UDPCaster.
 * <p>
 * UDPCaster subscribes to an RBNB channel and send the data, one frame at a
 * time, out as UDP packets to the specified address and port.
 *
 * @author John P. Wilson
 *
 * @version 06/02/2005
 */

/*
 * Copyright 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/02/2005  JPW	Created.
 *
 */

public class rbnbUDPCaster {
    
    /**************************************************************************
     * Main method; parse command line arguments.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @version 06/02/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/02/2005  JPW  Created.
     *
     */
    
    public static void main(String[] argsI) {
	
	String serverAddressL = null;
	String chanNameL = null;
	String recipientHostL = null;
	int recipientPortL = -1;
	int senderPortL = -1;
	
	//parse args
	try {
	    ArgHandler ah = new ArgHandler(argsI);
	    
	    if (ah.checkFlag('h') || ah.checkFlag('?')) {
		throw new Exception("Print help message");
	    }
	    
	    if (ah.checkFlag('a')) {
		// RBNB address
		serverAddressL = ah.getOption('a');
		if ( (serverAddressL != null) &&
		     (serverAddressL.length() > 0) )
		{
		    if (serverAddressL.indexOf(':') == -1) {
			serverAddressL = new String(serverAddressL + ":3333");
		    }
		}
	    }
	    
	    if (ah.checkFlag('c')) {
		chanNameL = ah.getOption('c');
	    }
	    
	    if (ah.checkFlag('r')) {
		// recipient address that packets are sent to
		String recipientAddressL = ah.getOption('r');
		// parse recipientAddressL into host and port
		int colonIdx = recipientAddressL.indexOf(':');
		if (colonIdx == -1) {
		    throw new Exception(
		    "The recipient address must be of the form <host>:<port>");
		}
		String tempRecipientHost =
		    recipientAddressL.substring(0,colonIdx);
		String recipientPortStr =
		    recipientAddressL.substring(colonIdx+1);
		int tempRecipientPort = -1;
		try {
		    tempRecipientPort = Integer.parseInt(recipientPortStr);
		} catch (Exception e) {
		    throw new Exception(
		        "The recipient port must be an integer.");
		}
		recipientHostL = tempRecipientHost;
		recipientPortL = tempRecipientPort;
	    }
	    
	    if (ah.checkFlag('s')) {
		String portStr = ah.getOption('s');
		senderPortL = Integer.parseInt(portStr);
	    }
	    
	} catch (Exception e) {
	    if ( (e.getMessage() != null) &&
	    	 (!e.getMessage().equals("Print help message")) )
	    {
		System.err.println(
		    "UDPCaster argument exception:\n" + e);
	    }
	    // Print a help message
	    System.err.println("UDPCaster");
	    System.err.println(" -h                     : print this usage info");
	    System.err.println(" -a <server address>    : RBNB address");
	    System.err.println("                default : localhost:3333");
	    System.err.println(" -c <input chan>        : RBNB channel to subscribe to");
	    System.err.println(" -r <recipient address> : address UDP packets are sent to");
	    System.err.println("                default : localhost:5555");
	    System.err.println(" -s <port>              : socket port for sending out UDP packets");
	    System.err.println("                default : 3456");
	    RBNBProcess.exit(0);
	}
	
	UDPCaster caster =
	    new UDPCaster(
	        serverAddressL,
		chanNameL,
		senderPortL,
		recipientHostL,
		recipientPortL);
	
    }
}

