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

package com.rbnb.udpcaster;

import java.net.InetSocketAddress;

/******************************************************************************
 * Store information pertaining to one UDP recipient.
 * <p>
 *
 * @author John P. Wilson
 *
 * @version 10/01/2007
 */

/*
 * Copyright 2007 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/01/2007  JPW	Created.
 *
 */

public class Recipient {
    
    private String host = null;
    private int port = 0;
    private InetSocketAddress socketAddr = null;
    
    /**************************************************************************
     * Constructor
     * <p>
     * Must be able to create a legal InetSocketAddress with the given
     * host and port.  Throw an exception on any error.
     *
     * @author John P. Wilson
     *
     * @param hostI  The recipient host name.
     * @param portI  The recipient port.
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    public Recipient(String hostI, int portI) throws Exception {
	processInputs(hostI,portI);
    }
    
    /**************************************************************************
     * Constructor
     * <p>
     * Must be able to create a legal InetSocketAddress with the given
     * host and port.  Throw an exception on any error.
     *
     * @author John P. Wilson
     *
     * @param hostPortStrI  The recipient host:port as a String.
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    public Recipient(String hostPortStrI) throws Exception {
	
	// Firewall
	if ( (hostPortStrI == null) || (hostPortStrI.trim().equals("")) ) {
	    throw new Exception(
		"The recipient address must be of the form <host>:<port>");
	}
	
	// Get rid of empty space
	String hostPortStr = hostPortStrI.trim();
	
	// Parse the given argument into host and port
	int colonIdx = hostPortStr.indexOf(':');
	if (colonIdx == -1) {
	    throw new Exception(
		"The recipient address must be of the form <host>:<port>");
	}
	String tempRecipientHost = hostPortStr.substring(0,colonIdx);
	String recipientPortStr = hostPortStr.substring(colonIdx+1);
	int tempRecipientPort = -1;
	try {
	    tempRecipientPort = Integer.parseInt(recipientPortStr);
	} catch (Exception e) {
	    throw new Exception(
	        "The recipient port must be an integer.");
	}
	
	processInputs(tempRecipientHost,tempRecipientPort);
	
    }
    
    /**************************************************************************
     * Generate an InetSocketAddress from the given host and port.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param hostI  The recipient host name.
     * @param portI  The recipient port.
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    private void processInputs(String hostI, int portI) throws Exception {
	
	// Check parameters
	if ( (hostI == null) || (hostI.trim().equals("")) ) {
	    throw new Exception("Must specify a non-empty host name.");
	} else if ( (portI <= 0) || (portI > 65535) ) {
	    throw new Exception(
		"Must specify an integer port in the range 0 < x <= 65535");
	}
	
	host = hostI.trim();
	port = portI;
	
	try {
	    socketAddr = new InetSocketAddress(host, port);
	    if (socketAddr.isUnresolved()) {
		throw new IllegalArgumentException("");
	    }
	} catch (IllegalArgumentException iae) {
	    throw new Exception(
		"Error creating InetSocketAddress " +
		"(possibly host could not be resolved)");
	}
	
    }
    
    /**************************************************************************
     * Get host.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return the host name.
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    public String getHost() {
	return host;
    }
    
    /**************************************************************************
     * Get port.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return the port.
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    public int getPort() {
	return port;
    }
    
    /**************************************************************************
     * Get the socket address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return the socket address.
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    public InetSocketAddress getSocketAddr() {
	return socketAddr;
    }
    
    /**************************************************************************
     * Return a string representation of this object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return a string representation of this object.
     *
     * @version 10/02/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/02/2007  JPW  Created.
     *
     */
    public String toString() {
	return new String(getHost() + ":" + getPort());
    }
    
}
