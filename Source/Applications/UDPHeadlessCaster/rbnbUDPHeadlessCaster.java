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

import java.util.StringTokenizer;
import java.util.Vector;

import com.rbnb.udpheadlesscaster.Recipient;
import com.rbnb.udpheadlesscaster.UDPHeadlessCaster;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

/******************************************************************************
 *
 * Based on code from UDPCaster, Copyright 2007 Creare Inc.
 *
 * Utility class for creating an instance of UDPHeadlessCaster.
 * <p>
 * UDPHeadlessCaster requests data from an RBNB channel and sends it out as
 * UDP packets to the specified address(es) and port(s).
 * <p>
 * This program is largely based on UDPCaster with the following changes:
 * 1. Got rid of all GUI elements to make it a truly headless program.
 *    UDPCaster has a headless mode, but even using this flag there were
 *    problems running this on the Gumstix.  Therefore, we created this
 *    version to get rid of all references to GUI components.
 * 2. Change from Subscribe to Request/Response for getting data.
 * 3. Add "fetchPeriod" option to make a sleepy polling request loop.
 *    This works well with Request/Response mode (change #2 above).
 *
 * @author John P. Wilson
 *
 * @version 04/19/2010
 */

/*
 * Copyright 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/19/2010  JPW	Created UDPHeadlessCaster based on UDPCaster
 * 06/02/2005  JPW	Created UDPCaster at Creare.
 *
 */

public class rbnbUDPHeadlessCaster {
    
    public static final String defaultRecipient = "localhost:5555";
    
    /**************************************************************************
     * Main method; parse command line arguments.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @version 04/19/2010
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/19/2010  JPW  Created.
     *
     */
    
    public static void main(String[] argsI) {
	
	String serverAddressL = null;
	String chanNameL = null;
	long fetchPeriodL = 0;
	Vector recipients = new Vector();
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
	    
	    if (ah.checkFlag('p')) {
		String periodStr = ah.getOption('p');
		fetchPeriodL = Long.parseLong(periodStr);
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
	    
	} catch (Exception e) {
	    if ( (e.getMessage() != null) &&
	    	 (!e.getMessage().equals("Print help message")) )
	    {
		System.err.println(
		    "UDPHeadlessCaster argument exception:\n" + e);
	    }
	    // Print a help message
	    System.err.println("UDPHeadlessCaster");
	    System.err.println(" -h                    : print this usage info");
	    System.err.println(" -a <server address>   : RBNB address");
	    System.err.println("               default : localhost:3333");
	    System.err.println(" -c <input chan>       : RBNB channel to request data from");
	    System.err.println(" -p <fetch period>     : How long to sleep between requests for newest from the channel");
	    System.err.println("               default : 0 (this will make requests as quickly as possible)");
	    System.err.println(" -r <host:port list>   : comma-delimited list of host:port recipients to send the UDP packets to");
	    System.err.println("               default : " + defaultRecipient);
	    System.err.println(" -s <port>             : socket port for sending out UDP packets");
	    System.err.println("               default : 3456");
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
	
	UDPHeadlessCaster caster =
	    new UDPHeadlessCaster(
	        serverAddressL,
		chanNameL,
		senderPortL,
		recipients,
		fetchPeriodL);
	
    }
}

