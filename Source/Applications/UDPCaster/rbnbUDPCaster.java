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

import java.util.StringTokenizer;
import java.util.Vector;

import com.rbnb.udpcaster.Recipient;
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
 * @version 06/27/2008
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
    
    public static final String defaultRecipient = "localhost:5555";
    
    /**************************************************************************
     * Main method; parse command line arguments.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @version 06/27/2008
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/27/2008  JPW  Add "-g" option to run in headless mode
     * 10/01/2007  JPW  Change "-r" from a single recipient to (optionally) a
     *                  comma-delimited list of host:port recipients.
     * 09/26/2007  JPW  Added "-o" (stream from oldest) and "-x" (autostart)
     * 06/02/2005  JPW  Created.
     *
     */
    
    public static void main(String[] argsI) {
	
	String serverAddressL = null;
	String chanNameL = null;
	Vector recipients = new Vector();
	int senderPortL = -1;
	boolean bStreamFromOldestL = false;
	boolean bAutostartL = false;
	boolean bHeadlessL = false;
	
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
	    
	    if (ah.checkFlag('g')) {
		bHeadlessL = true;
	    }
	    
	    if (ah.checkFlag('o')) {
		bStreamFromOldestL = true;
	    }
	    
	    if (ah.checkFlag('r')) {
		// Recipient addresses that packets are sent to; this can be
		// one address or a comma-delimited list
		String addresses = ah.getOption('r');
		if ( (addresses == null) || (addresses.trim().equals("")) ) {
		    throw new Exception(
			"When using the \"-r\" flag, must specify " +
			"at least one valid host:port recipient address.");
		}
		// Would be simpler to use String.split(), but this wouldn't
		// keep the code at Java 1.1.4-compliant.
		// String[] addrComponents = addresses.split(",");
		StringTokenizer st = new StringTokenizer(addresses,",");
		Recipient recipient = null;
		while (st.hasMoreTokens()) {
		    String nextAddr = st.nextToken();
		    try {
			recipient = new Recipient(nextAddr);
			recipients.addElement(recipient);
		    } catch (Exception excep) {
			System.err.println(
			    "Error with recipient address " +
			    nextAddr +
			    ":\n\t" +
			    excep);
		    }
		}
		if (recipients.isEmpty()) {
		    throw new Exception(
			"When using the \"-r\" flag, must specify " +
			"at least one valid host:port recipient address.");
		}
	    }
	    
	    if (ah.checkFlag('s')) {
		String portStr = ah.getOption('s');
		senderPortL = Integer.parseInt(portStr);
	    }
	    
	    if (ah.checkFlag('x')) {
		bAutostartL = true;
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
	    System.err.println(" -h                    : print this usage info");
	    System.err.println(" -a <server address>   : RBNB address");
	    System.err.println("               default : localhost:3333");
	    System.err.println(" -c <input chan>       : RBNB channel to subscribe to");
	    System.err.println(" -g                    : run in headless (no GUI) mode");
	    System.err.println("                       : NOTE: to use headless mode, all parameters must be specified on command");
	    System.err.println("                       :       line and the autostart (\"-x\") option must also be used");
	    System.err.println(" -o                    : stream from oldest");
	    System.err.println("               default : stream from newest");
	    System.err.println(" -r <host:port list>   : comma-delimited list of host:port recipients to send the UDP packets to");
	    System.err.println("               default : " + defaultRecipient);
	    System.err.println(" -s <port>             : socket port for sending out UDP packets");
	    System.err.println("               default : 3456");
	    System.err.println(" -x                    : auto-start");
	    RBNBProcess.exit(0);
	}
	
	// If user is running headless (no GUI), then make sure they
	// have also specified the autostart option
	if (bHeadlessL && !bAutostartL) {
	    System.err.println("Can only run in headless mode (\"-g\") if all parameters are");
	    System.err.println("specified on command line and auto-start (\"-x\") is also specified.");
	    RBNBProcess.exit(0);
	}
	
	// Use default recipient if no others were specified on command line
	if (recipients.isEmpty()) {
	    try {
		Recipient recipient = new Recipient(defaultRecipient);
		recipients.addElement(recipient);
	    } catch (Exception e) {
		// Nothing to do
	    }
	}
	
	UDPCaster caster =
	    new UDPCaster(
	        serverAddressL,
		chanNameL,
		senderPortL,
		recipients,
		bStreamFromOldestL,
		bAutostartL,
		bHeadlessL);
	
    }
}

