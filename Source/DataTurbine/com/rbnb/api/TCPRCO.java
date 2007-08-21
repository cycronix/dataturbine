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
 * Extended <code>RCO</code> that communicates via TCP.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/28/2004
 */

/*
 * Copyright 2001, 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/28/2004  INB	Added method <code>isAllowedAccess</code>.
 * 05/10/2001  INB	Created.
 *
 */
final class TCPRCO
    extends com.rbnb.api.SerializingRCO
{
    /**
     * the data socket.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/15/2001
     */
    private java.net.Socket dataSocket = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    TCPRCO() {
	super();
    }

    /**
     * Class constructor to build an <code>TCPRCO</code> for a connection to a
     * <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI the connection object.
     * @param rbnbI	  the <code>ServerHandler</code>.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    TCPRCO(Object serverSideI,ServerHandler rbnbI)
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(serverSideI,rbnbI);

	java.net.Socket socket = (java.net.Socket) getServerSide();
	if (socket != null) {
	    setSerialize
		(new Language(new InputStream(socket.getInputStream(),
					      false,
					      0),
			      null,
			      new OutputStream(socket.getOutputStream(),
					       false,
					       0),
			      null));
	}
    }

    /**
     * Assigns the connection represented by the input <code>RCO</code> to this
     * <code>RCO</code> as a data line.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code> representing the connection.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is already a data connection.
     * @exception java.io.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    final synchronized void assignConnection(RCO rcoI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getDataSocket() != null) {
	    throw new java.lang.IllegalStateException
		("Cannot connect a data socket when one already exists.");
	}
	TCPRCO tcprco = (TCPRCO) rcoI;
	tcprco.getSerialize().getOcontrol().setBinary(true);
	tcprco.getSerialize().getIcontrol().setBinary(true);
	rcoI.close();
	metricsBytes += rcoI.bytesTransferred();
	rcoI.metricsBytes = 0;

	java.net.Socket socket = (java.net.Socket) rcoI.getServerSide();
	rcoI.setServerSide(null);
	if (getSerialize() == null) {
	    socket.close();
	    return;
	}

	attachDataSocket(socket);
	getSerialize().getOdata().write(0x01);
	getSerialize().getOdata().flush();
	notifyAll();
    }

    /**
     * Attaches an existing data socket.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataSocketI the data socket.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final void attachDataSocket(java.net.Socket dataSocketI)
	throws java.io.IOException
    {
	setDataSocket(dataSocketI);
	getSerialize().setOdata
	    (new DataOutputStream(getDataSocket().getOutputStream(),
				  32768));
	getSerialize().setIdata
	    (new DataInputStream(getDataSocket().getInputStream(),
				 32768));
    }

    /**
     * Converts this <code>ACO</code> to an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @return the <code>RCO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final ACO convertToACO(Client clientI)
	throws com.rbnb.api.AddressException
    {
	TCPACO acoR = new TCPACO(clientI);

	acoR.setClientSide(getServerSide());
	acoR.setSerialize(getSerialize());
	acoR.setDataSocket(getDataSocket());
	acoR.metricsBytes = metricsBytes;
	metricsBytes = 0;

	return (acoR);
    }

    /**
     * Disconnects from the <code>ACO</code>.
     * <p>
     * The disconnect operation shuts down the connection to the
     * <code>ACO</code> completely. It assumes that the <code>close</code>
     * operation has already been performed.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #close()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    final void disconnect()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Address tcp = null;

	try {
	    if (getServerSide() != null) {
		java.net.Socket lsocket = (java.net.Socket) getServerSide();
		java.net.InetAddress laddress = lsocket.getLocalAddress();
		tcp = Address.newAddress
		    ("tcp://" + laddress.getHostName() + ":" +
		     lsocket.getLocalPort());
	    }
	} catch (com.rbnb.api.AddressException e) {
	    tcp = null;
	}

	if (tcp != null) {
	    tcp.disconnect(getServerSide());
	}
	setServerSide(null);
	if (getDataSocket() != null) {
	    if (tcp != null) {
		tcp.disconnect(getDataSocket());
	    }
	    setDataSocket(null);
	}
    }

    /**
     * Gets the data socket.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data socket.
     * @see #setDataSocket(java.net.Socket)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    private final java.net.Socket getDataSocket() {
	return (dataSocket);
    }

    /**
     * Is this <code>RCO<code> allowed the specified access?
     * <p>
     * This implementation checks the address of the socket against the
     * addresses allowed by the input <code>Address</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the local <code>Address</code> object.
     * @param accessI  the desired access.
     * @return was access allowed?
     * @exception com.rbnb.api.AddressException
     *		  if an error occured.
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    final boolean isAllowedAccess(Address addressI,int accessI)
	throws com.rbnb.api.AddressException
    {
	java.net.Socket socket = (java.net.Socket) getServerSide();
	java.net.InetAddress netAddr = null;
	boolean allowedR = false;

	try {
	    netAddr = socket.getInetAddress();
	    Object addr;
	    if ((netAddr.getHostName() == null) ||
		(netAddr.getHostName().equals(""))) {
		addr = netAddr.getHostAddress();
	    } else {
		String[] values = new String[2];
		values[0] = netAddr.getHostName();
		values[1] = netAddr.getHostAddress();
		addr = values;
	    }
	    if ((addressI == null) ||
		(addressI.getAuthorization() == null) ||
		addressI.getAuthorization().isAccessAllowed(addr,accessI)) {
		allowedR = true;
	    }

	} catch (java.lang.Exception e) {
	    if (e instanceof com.rbnb.api.AddressException) {
		throw (com.rbnb.api.AddressException) e;
	    }
	    java.io.ByteArrayOutputStream baos =
		new java.io.ByteArrayOutputStream();
	    java.io.PrintWriter pw = new java.io.PrintWriter(baos);
	    e.printStackTrace(pw);
	    pw.flush();
	    String message = new String(baos.toByteArray());
	    pw.close();
	    throw new com.rbnb.api.AddressException
		(netAddr +
		 " was denied " +
		 AddressPermissions.ACCESS_VALUES[accessI] +
		 " access due to an exception.\n" +
		 message);
	}

	return (allowedR);
    }

    /**
     * Logs into this <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param loginI the <code>Login</code> message.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final synchronized void login(Login loginI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSerialize().close();
	metricsBytes += getSerialize().bytesTransferred();
	java.net.Socket cSocket = (java.net.Socket) getServerSide();
	setSerialize
	    (new Language(new InputStream(cSocket.getInputStream(),
					  false,
					  32768),
			  null,
			  new OutputStream(cSocket.getOutputStream(),
					   false,
					   32768),
			  null));
	super.login(loginI);
    }

    /**
     * Sets the data socket.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataSocketI the data socket.
     * @see #getDataSocket()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final void setDataSocket(java.net.Socket dataSocketI) {
	dataSocket = dataSocketI;
    }

    /**
     * Sets the timeout for reads on the control connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI the timeout.
     * @exception java.io.IOException
     *		  thrown if there is a problem setting the timeout.
     * @since V2.0
     * @version 05/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    final void setTimeout(long timeOutI)
	throws java.io.IOException
    {
	java.net.Socket socket = (java.net.Socket) getServerSide();
	socket.setSoTimeout((int) timeOutI);
    }
}
