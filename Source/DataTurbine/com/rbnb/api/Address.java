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

package com.rbnb.api;

/**
 * Abstract class that deals with RBNB server addresses.
 * <p>
 * RBNB server addresses serve as the means by which a client application using
 * the API connects to the RBNB DataTurbine server.  They represent the
 * server's address and provide an abstraction of the details of communicating
 * over that sort of address.
 * <p>
 * The files are organized into a hierarchy as follows:
 * <p>
 * <dir><code>Address</code>
 *   <dir><code>TCP</code></dir>
 *   <dir><code>RAM</code>
 *     <dir>Adjunct classes: <code>RAMCommunications</code> and
 *	    <code>RAMServerCommunications</code></dir>
 *   </dir>
 * </dir>
 * <p>
 * The <code>Address</code> class provides the external interface for using
 * addresses.
 * <p>
 * The <code>TCP</code> class provides the low-level TCP socket handling
 * methods for using TCP server sockets as the address of an RBNB DataTurbine
 * server.
 * <p>
 * The <code>RAM</code> class provides the low-level in-memory (RAM) handling
 * methods for using direct memory-to-memory copy to communicate between the
 * API and the RBNB DataTurbine server.
 * <p>
 * The <code>RAMCommunications</code> adjunct class provides the client side
 * handling of the memory-to-memory copies.  It is the RAM equivalent of a
 * standard TCP socket.
 * <p>
 * The <code>RAMServerCommunications</code> adjunct class provides the server
 * side handling of the memory-to-memory copies.  It is the RAM equivalent of a
 * TCP server socket.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/06/2004
 */

/*
 * Copyright 2001, 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 2005/01/20  WHF	Now implements UsernameInterface.
 * 08/06/2004  INB	Added documentation.
 * 05/11/2001  INB	Created.
 *
 */
abstract class Address implements UsernameInterface {

    /**
     * the address being handled.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private String address = null;

    /**
     * the authorization list to check incoming addresses against.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/18/2002
     */
    private AddressAuthorization authorization = null;
    
    private Username username;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    Address() {
	super();
    }

    /**
     * Class constructor to build an <code>Address</code> for an address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the TCP DNS or IP address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    Address(String addressI)
	throws com.rbnb.api.AddressException
    {
	this();
	setAddress(addressI);
    }

    /**
     * Accepts connections to a server-side connection object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI the server-side connection object.
     * @param timeOutI  timeout in milliseconds.
     *			<br><ul>
     *			<li><code>Client.FOREVER</code> means wait for a
     *			    response to show up, or</li>
     *			<li>anything else means wait for a response to show up
     *			    or for the timeout period to elapse.</li>
     *			</ul>
     * @return the connection object.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception com.rbnb.api.AddressException
     *		  thrown if the address is rejected for any reason.
     * @see #connect(java.lang.Object)
     * @since V2.0
     * @version 10/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    abstract Object accept(Object serverSideI,long timeOutI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Closes a connection object for this <code>Address</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param connectionI the connection object.
     * @since V2.0
     * @version 03/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    abstract void close(Object connectionI);

    /**
     * Connects to the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client-side object.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #accept(java.lang.Object,long)
     * @see #disconnect(java.lang.Object)
     * @since V2.0
     * @version 03/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    abstract void connect(Object clientSideI)
	throws java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Disconnects from the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client-side object.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #accept(java.lang.Object,long)
     * @since V2.0
     * @version 03/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    abstract void disconnect(Object clientSideI)
	throws java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the address being handled.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address.
     * @see #setAddress(String)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    final String getAddress() {
	return (address);
    }

    /**
     * Gets the authorization list.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the authorization list.
     * @see #setAuthorization(com.rbnb.api.AddressAuthorization)
     * @since V2.0
     * @version 10/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/18/2002  INB	Created.
     *
     */
    final AddressAuthorization getAuthorization() {
	return (authorization);
    }

    /**
     * Is the address one that can be handled by a local RBNB server?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the address local?
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    abstract boolean isLocal();

    /**
     * Creates a new <code>Address</code> to handle the specified address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address to handle.
     * @return the <code>Address</code> object for handling the address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @since V2.0
     * @version 08/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2004  INB	Added some in-line documentation.
     * 05/11/2001  INB	Created.
     *
     */
    final static Address newAddress(String addressI)
	throws com.rbnb.api.AddressException
    {
	int ss = addressI.indexOf("//");
	Address addressR = null;

	if (ss == -1) {
	    // If the address does not include //, then assume that it is a TCP
	    // address.
	    addressR = new TCP(TCP.buildAddress(addressI));

	} else {
	    // Otherwise, determine the type of address from the string
	    // preceeding the //.
	    String type = addressI.substring(0,ss);

	    if (type.equalsIgnoreCase("RAM:") ||
		type.equalsIgnoreCase("INTERNAL:")) {
		// RAM and INTERNAL addresses use the RAM class for in-memory
		// thread to thread communications.
		addressR = new RAM(addressI);

	    } else if (type.equalsIgnoreCase("TCP:")) {
		// TCP addresses use the TCP class for TCP socket to server
		// socket communications.
		addressR = new TCP(TCP.buildAddress(addressI));

	    } else {
		throw new com.rbnb.api.AddressException
		    ("Unsupported address: " + addressI);
	    }
	}

	return (addressR);
    }

    /**
     * Creates a new client-side connection to the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acoI the <code>ACO</code>.
     * @return the connection object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem creating the connection.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem creating the connection.
     * @exception java.lang.SecurityException
     *		  thrown if the security manager refuses the connection.
     * @see #newServerSide(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    abstract Object newClientSide(ACO acoI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException,
	       java.lang.SecurityException;

    /**
     * Creates a new server-side connection to the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverHandlerI the <code>ServerHandler</code>.
     * @return the connection object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem creating the connection.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem creating the connection.
     * @exception java.lang.SecurityException
     *		  thrown if the security manager refuses the connection.
     * @see #newClientSide(com.rbnb.api.ACO)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    abstract Object newServerSide(ServerHandler serverHandlerI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException,
	       java.lang.SecurityException;

    /**
     * Sets the address to handle.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if the address is not valid.
     * @see #getAddress()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    void setAddress(String addressI)
	throws com.rbnb.api.AddressException	
    {
	address = addressI;
    }
    
    /**
      * @author WHF
      * @since V2.5
      */
    public Username getUsername() { return username; }
    /**
      * @author WHF
      * @since V2.5
      */
    public void setUsername(Username un) { username = un; }

    /**
     * Sets the address authorization list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param authorizationI the address authorization list.
     * @see #getAuthorization()
     * @since V2.0
     * @version 10/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/18/2002  INB	Created.
     *
     */
    final void setAuthorization(AddressAuthorization authorizationI) {
	authorization = authorizationI;
    }

    /**
     * Returns a string representation of this <code>Address</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 05/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/16/2001  INB	Created.
     *
     */
    public String toString() {
	String stringR = null;

	try {
	    String className = getClass().toString();
	    className = className.substring
		(className.lastIndexOf(".") + 1);

	    stringR = className + " " + getAddress();
	} catch (java.lang.Exception e) {
	}

	return (stringR);
    }
}
