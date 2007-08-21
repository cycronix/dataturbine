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


import com.rbnb.tcpcaster.TCPCaster;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

/******************************************************************************
 * Utilit class for creating an instance of TCPCaster.
 * <p>
 * TCPCaster subscribes to an RBNB channel and send the data, one frame at a
 * time, to all connected client TCP sockets.
 *
 * @author John P. Wilson
 *
 * @version 05/05/2005
 */

/*
 * Copyright 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/05/2005  JPW	Created.
 *
 */

public class rbnbTCPCaster {
    
    /**************************************************************************
     * Main method; parse command line arguments.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @version 05/04/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/04/2005  JPW  Created.
     *
     */
    
    public static void main(String[] argsI) {
	
	String serverAddressL = null;
	String chanNameL = null;
	int portL = -1;
	
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
	    
	    if (ah.checkFlag('s')) {
		String portStr = ah.getOption('s');
		portL = Integer.parseInt(portStr);
	    }
	    
	} catch (Exception e) {
	    if ( (e.getMessage() != null) &&
	    	 (!e.getMessage().equals("Print help message")) )
	    {
		System.err.println(
		    "TCPCaster argument exception:\n" + e);
	    }
	    // Print a help message
	    System.err.println("TCPCaster");
	    System.err.println(" -h                  : print this usage info");
	    System.err.println(" -a <server address> : RBNB address");
	    System.err.println("             default : localhost:3333");
	    System.err.println(" -c <input chan>     : RBNB channel to subscribe to");
	    System.err.println(" -s <server socket>  : socket number to listen for client connections");
	    System.err.println("             default : 13280");
	    RBNBProcess.exit(0);
	}
	
	TCPCaster caster = new TCPCaster(serverAddressL, chanNameL, portL);
	
    }
}

