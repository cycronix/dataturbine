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
 * Application Control Object (ACO) class.
 * <p>
 * All communications between the RBNB server and the API are handled by the
 * RBNB control object (<code>RCO</code>) class (and its subclasses) on
 * the RBNB server side and this class (and its subclasses) on the client API
 * side.  For an explanation of the server side, see <code>RCO</code>.
 * <p>
 * Objects of the <code>ACO</code> class are paired with a client handle
 * object (such as a <code>SourceHandle</code>).  The <code>ACO</code> handles
 * the details of sending messages to and receiving messages from the RBNB
 * server.  The client handle is the intermediary between the client
 * application and the <code>ACO</code>.  It is responsible for determining
 * what commands the client can send to the server and what information can be
 * read by the client from the server.
 * <p>
 * The <code>ACO</code> and <code>RCO</code> class hierarchies talk on an
 * <code>Address</code>.  Every RBNB server has a primary address to use to
 * connect to it.  This is normally a TCP address, but it can also be a RAM if
 * the server and clients are to be run inside a single JVM.  If the primary
 * address is not RAM, then a secondary RAM address is also provided.
 * Addresses are described in more detail in <code>Address</code>.
 * <p>
 * <code>ACO</code> and <code>RCO</code> are abstract classes that provide a
 * simplied external interface for opening the <code>Address</code>, sending
 * and receiving messages, and closing the <code>Address</code>.  Their
 * subclasses operate in conjunction with particular types of
 * <code>Addresses</code> to provide the actual communications.  The
 * hierarchies look like the following:
 * <p>
 * <table alignment="center" valign="top" cellpadding=5>
 *   <thead align="center" valign="center">
 *     <tr>
 *	 <th>ACO Hierarchy</th>
 *	 <th>RCO Hierarchy</th>
 *     </tr>
 *   </thead>
 *   <tbody align="left" valign="center">
 *     <tr>
 *	 <td><code>ACO</code></td>
 *	 <td><code>RCO</code></td>
 *     </tr>
 *     <tr>
 *	 <td><code><dir>SerializingACO</code></dir></td>
 *	 <td><code><dir>SerializingRCO</code></dir></td>
 *     </tr>
 *     <tr>
 *	 <td><code><dir><dir>TCPACO</code></dir></dir></td>
 *	 <td><code><dir><dir>TCPRCO</code></dir></dir></td>
 *     </tr>
 *     <tr>
 *	 <td><code><dir>RAMACO</code></dir></td>
 *	 <td><code><dir>RAMRCO</code></dir></td>
 *     </tr>
 *   </tbody>
 * </table>
 * <p>
 * The <code>SerializingXXX</code> abstract classes provide the send and
 * receive methods necessary to send and receive data across a serial
 * connection such as a TCP line or a serial port.
 * <p>
 * The <code>TCPXXX</code> classes provide the connect, timeout set up, and
 * disconnect methods necessary to use TCP connections.  Objects of these
 * classes serialize and deserialize the RBNB language commands and responses
 * over a TCP socket.
 * <p>
 * The <code>RAMXXX</code> classes provide all of the methods necessary to
 * communicate via memory.  They make copies the RBNB language commands and
 * responses and pass the copies between threads.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.ClientHandle
 * @see com.rbnb.api.RCO
 * @since V2.0
 * @version 09/08/2005
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 2005/09/08  WHF	Added Ping to the isRunnable code to execercise the
 *			data channel.  Added pingValue.
 * 09/28/2004  JPW	In order to compile under J# (which is only Java 1.1.4
 *			compliant), got rid of call to Throwable.initCause() in
 *			fetch() (initCause() is a Java version 1.4 method).
 * 08/05/2004  INB	Added documentation.
 * 04/06/2004  INB	Wrap server-side exceptions in <code>fetch</code>.
 * 02/17/2004  INB	Added <code>RBNBFAILREVERSE</code> code for testing.
 * 01/08/2004  INB	Added <code>clearCache</code>.
 * 06/17/2003  INB	Allow for <code>RequestOptions</code> objects in
 *			<code>fetch</code>.
 * 05/23/2003  INB	Added <code>send</code> with a timeout. Use it when
 *			stopping a router. Timeout when logging in.
 * 05/07/2003  INB	Use <code>STARTUP_WAIT</code> when sending startup
 *			commands to remote client handler.
 * 04/17/2003  INB	Assume that the remote end is at least capable of
 *			usernames initially. This may cause compatibility
 *			problems, but ensures that usernames can be used
 *			during the login process. This also meant that the
 *			method <code>setBuildDate</code> and
 *			<code>setBuildVersion</code> need to be overridden.
 * 04/01/2003  INB	Replaced some infinite waits with timeouts.
 * 03/31/2003  INB	Don't update our parent's name in <code>Login</code>.
 * 03/04/2003  INB	Don't synchronize on stop.
 * 02/26/2003  INB	When the <code>ACO</code> is shutting down, only wait a
 *			limited time for responses from the remote side.
 * 05/08/2001  INB	Created.
 *
 */
abstract class ACO
    implements com.rbnb.api.BuildInterface,
	       com.rbnb.api.IOMetricsInterface
{
    /**
     * the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */
    private java.util.Date buildDate = null;

    /**
     * the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */
    private String buildVersion = null;

    /**
     * the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/08/2001
     */
    private Client client = null;

    /**
     * the client-side communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private Object clientSide = null;
    
    /**
      * Data value which is sent to the RCO in the call to isRunning.
      *
      * <p>
      * @author WHF
      * @since V2.7
      * @version 2005/09/08
      */
    private short pingValue = -1;

    /**
     * is the <code>ACO</code> stopping?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */
    boolean stopping = false;

    /**
     * metrics: final bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    long metricsBytes = 0;

    // Package class variables:
    final static Class okClass = (new Ping()).getClass(),
		       rmapClass = (new Rmap()).getClass(),
		       serverClass = (new Server()).getClass();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    ACO() {
	super();
    }

    /**
     * Class constructor to build an <code>ACO</code> for a
     * <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code>.
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    ACO(Client clientI) {
	this();
	setClient(clientI);
    }

    /**
     * Adds a child <code>Rmap</code> to the <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI the child <code>Rmap</code> hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (childI instanceof DataRequest) {
	    DataRequest child = (DataRequest) childI;
	    if ((child.getReference() == child.ALIGNED) &&
		!isSupported(IsSupported.FEATURE_REQUEST_ALIGNED)) {
		throw new java.lang.IllegalArgumentException
		    ("Aligned requests are not supported by this server.");
	    }
	}
	send(childI);
    }

    /**
     * Calculates the total number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes transferred.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    public long bytesTransferred() {
	return (metricsBytes);
    }

    /**
     * Clears the <code>Cache</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public final void clearCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	clearCache(getClient());
    }

    /**
     * Clears the <code>Cache</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code> to clear.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public final void clearCache(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new ClearCache(clientI));
	receive(okClass,false,Client.FOREVER);
    }

    /**
     * Connects the data communications channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnectData()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    abstract void connectData()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Converts this <code>ACO</code> to an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbI the <code>ServerHandler</code>.
     * @return the <code>RCO</code>.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
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
     * 01/25/2002  INB	Created.
     *
     */
    abstract RCO convertToRCO(ServerHandler rbnbI)
	throws java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Creates the <code>RCO</code> in the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is problem connecting to the RBNB.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #destroyRCO()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    void createRCO()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Address address = ((Server)
			   getClient().getParent()).getAddressHandler();

	if ((this instanceof RAMACO) &&
	    !(address instanceof RAM)) {
	    // If this is a <code>RAMACO</code> connected to a non-RAM server,
	    // then we need to create a temporary <code>RAM</code> address.
	    address = new RAM("internal://" + address.getAddress());
	}
	
	address.setUsername(client.getUsername());

	setClientSide(address.newClientSide(this));
	address.connect(getClientSide());
    }

    /**
     * Destroys the <code>RCO</code> in the RBNB.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is problem connecting to the RBNB.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #createRCO()
     * @since V2.0
     * @version 05/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    void destroyRCO()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Address address = ((Server)
			   getClient().getParent()).getAddressHandler();

	if ((this instanceof RAMACO) &&
	    !(address instanceof RAM)) {
	    // If this is a <code>RAMACO</code> connected to a non-RAM server,
	    // then we need to create a temporary <code>RAM</code> address.
	    address = new RAM("internal://" + address.getAddress());
	}
	address.disconnect(getClientSide());
	setClientSide(null);
    }

    /**
     * Disonnects the data communications channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #connectData()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    abstract void disconnectData()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Fetches the next response to a request for data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>= 0 means read anything that is already waiting,</li>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li><code>Client.FOREVER</code> means wait until a message shows
     *		  up or the connection is terminated.</li>
     *	      </ul>
     * @return the <code>Serializable</code> fetched or null on a timeout.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #initiateRequestAt(int)
     * @since V2.0
     * @version 09/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2004  JPW	In order to compile under J# (which is only Java 1.1.4
     *			compliant), got rid of call to Throwable.initCause(),
     *			which is a Java version 1.4 method.
     * 04/06/2004  INB	Instead of simply throwing the server-side exception,
     *			wrap it in another exception if possible.
     * 06/17/2003  INB	Allow for <code>RequestOptions</code> objects.
     * 05/08/2001  INB	Created.
     *
     */
    final Serializable fetch(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializableR = receive(null,false,timeOutI);

	if (serializableR instanceof Ping) {
	    throw new java.io.EOFException
		("The server closed the connection.");

	} else if ((serializableR != null) &&
		   (!(serializableR instanceof Rmap)) &&
		   (!(serializableR instanceof Ask)) &&
		   (!(serializableR instanceof RequestOptions))) {
	    if (serializableR instanceof ExceptionMessage) {
		ExceptionMessage eMsg = (ExceptionMessage) serializableR;
		try {
		    Exception serverException = eMsg.toException();
		    // JPW 09/28/2004: The call to initCause() here actually
		    //                 calls Throwable.initCause(), which is
		    //                 a Java 1.4 call.  To compile under J#
		    //                 (which is only Java 1.1.4 compatable)
		    //                 we needed to get rid of the call to
		    //                 initCause().
		    // SerializeException se = new SerializeException
		    // 	    ("Server side exception occured:");
		    // se.initCause(serverException);
		    SerializeException se = new SerializeException
			("Server side exception occured:" + serverException);
		    throw se;
		} catch (java.lang.NoSuchMethodError e) {
		    Language.throwException(eMsg);
		}

	    } else {
		com.rbnb.api.SerializeException e =
		    new com.rbnb.api.SerializeException
			("Unexpected response: " + serializableR);
		send(new ExceptionMessage(e));
		throw e;
	    }
	}

	return (serializableR);
    }

    /**
     * Gets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build date.
     * @see #setBuildDate(java.util.Date)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    public final java.util.Date getBuildDate() {
	return (buildDate);
    }

    /**
     * Gets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build version string.
     * @see #setBuildVersion(String)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    public final String getBuildVersion() {
	return (buildVersion);
    }

    /**
     * Gets the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Client</code>.
     * @see #setClient(com.rbnb.api.Client)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final Client getClient() {
	return (client);
    }

    /**
     * Gets registration information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the request <code>Rmap</code> hierarchy to match.
     * @return the matching registration information <code>Rmap</code>
     *	      hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Ask(Ask.REGISTERED,requestI));

	Serializable response = fetch(Client.FOREVER);
	if (response instanceof Ask) {
	    throw new com.rbnb.api.SerializeException
		("Unexpected response: " + response);
	}

	Rmap rmapR = (Rmap) response;
	return (rmapR);
    }

    /**
     * Gets the client-side communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the client-side communications object.
     * @see #setClientSide(java.lang.Object)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final Object getClientSide() {
	return (clientSide);
    }

    /**
     * Initiates a request by its index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the request index.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #fetch(long)
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final void initiateRequestAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Ask(Ask.REQUESTAT,new Integer(indexI)));
    }

    /**
     * Is a feature supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @param featureI the feature.
     * @return is the feature supported?
     * @see com.rbnb.api.IsSupported
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    boolean isSupported(int featureI) {
	return (true);
    }

    /**
     * Is the <code>Client</code> running in the RBNB server?
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @return is it running?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 08/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2004  INB	Added in-line documentation.
     * 04/01/2003  INB	Don't wait forever for the response.
     * 05/11/2001  INB	Created.
     *
     */
    final boolean isRunning(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Ask the server if the provided client is running.  The answer should
	// come back reasonably soon; if not, we'll timeout and throw an
	// exception.
	send(new Ask(Ask.ISRUNNING,clientI));
	Serializable serializable = receive(TimerPeriod.PING_WAIT);
	if (serializable == null) {
	    throw new com.rbnb.api.AddressException
		("Failed to receive response from handler in a reasonable " +
		 "amount of time.");
	}

	// The response should be either a Start or a Stop message wrapped
	// around an object with a name equal to the client we asked about.
	boolean isRunningR = false;
	if (serializable instanceof Command) {
	    Command command = (Command) serializable;
	    if (command.getObject() instanceof Client) {
		Client clientS = (Client) command.getObject();

		if (clientI.compareNames((Rmap) clientS) == 0) {
		    isRunningR = (command instanceof Start);
		} else {
		    com.rbnb.api.SerializeException e =
			new com.rbnb.api.SerializeException
			    (command + " is not a valid response.");
		    send(new ExceptionMessage(e));
		    throw e;
		}
	    } else {
		com.rbnb.api.SerializeException e =
		    new com.rbnb.api.SerializeException
			(command + " is not a valid response.");
		send(new ExceptionMessage(e));
		throw e;
	    }

	} else if (serializable instanceof ExceptionMessage) {
	    Language.throwException((ExceptionMessage) serializable);

	} else {
	    com.rbnb.api.SerializeException e =
		new com.rbnb.api.SerializeException
		    (serializable + " is not a valid response.");
	    send(new ExceptionMessage(e));
	    throw e;
	}
	
	// 2005/09/08  WHF  Also send a ping with data to work the 
	//   data channel.
	if (isRunningR) {
	    //short pingValue = -1; // -1234;
	    
	    send(new Ping(true, pingValue++));
	    
	    // Get the response Ping:
	    //Serializable serializable;
	    Ping ping = null;

	    while (true) { // loop until timeout, ping or bad message recvd.
		serializable = receive(null, false, TimerPeriod.PING_WAIT);

		if (serializable == null) {
		    throw new com.rbnb.api.SerializeException
			("Timed out waiting for ping.");
		} else if (serializable instanceof ExceptionMessage) {
		    // We received an exception instead of a Ping!
		    Language.throwException((ExceptionMessage) serializable);
		} else if (!(serializable instanceof Ping)) {
System.err.println("Ping response not a Ping.");
                  //EMF 11/1/05: return false in this case - garbage on the data line is not good
                  isRunningR = false;
		} else {
		    ping = (Ping) serializable;
		    // Compare to pingValue - 1 because pingValue has been 
		    //  incremented.
		    if (!ping.getHasData() || (ping.getData() != pingValue-1)) {
System.err.println("Bad Ping response.");
			isRunningR = false;
		    }
		    break;
		}
	    }	    
	}

	return (isRunningR);
    }

    /**
     * Is anything waiting to be read?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is anything waiting to be read?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    abstract boolean isWaiting()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Logs into the RBNB server.
     * <p>
     * This method sends a <code>Login</code> message to the RBNB. The response
     * from the RBNB is a <code>Server</code> object contains a child that has
     * a type equal to the type of our parent <code>Client</code>. These two
     * objects are used to update the name information for the local
     * <code>Client</code> and <code>Server</code> objects.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Add timeout to <code>send</code>.
     * 05/07/2003  INB	Use <code>STARTUP_WAIT</code> rather than
     *			<code>PING_WAIT</code>.
     * 04/17/2003  INB	Assume that the remote end is at least capable of
     *			usernames initially. This may cause compatibility
     *			problems, but ensures that usernames can be used
     *			during the login process. This also meant that the
     *			method <code>setBuildDate</code> and
     *			<code>setBuildVersion</code> need to be overridden.
     * 04/01/2003  INB	Throw an exception if we fail to connect in a
     *			reasonable amount of time.
     * 03/31/2003  INB	Don't update our parent's name, unless it has no name.
     * 05/16/2001  INB	Created.
     *
     */
    void login()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Build the <code>Login</code> message to create a <code>Client</code>
	// that looks like our parent.
	Login login = new Login();
	if (getClient() instanceof BuildInterface) {
	    BuildInterface bi = (BuildInterface) getClient();
	    login.setBuildDate(bi.getBuildDate());
	    login.setBuildVersion(bi.getBuildVersion());
	    //	    login.setLicenseString(bi.getLicenseString());
	} else {
	    BuildFile.loadBuildFile(login);
	}
	login.addChild((Rmap) ((Rmap) getClient()).clone());
	if (getClient().getUsername() != null) {
	    login.setUsername(getClient().getUsername());
	}

	// Initially, assume that the remote end is at least up to usernames.
	try {
	    setBuildDate
		((new java.text.SimpleDateFormat
		    ("MMM dd yyyy",
		     java.util.Locale.US)).parse
		 ("Jan 14 2003"));
	} catch (java.text.ParseException e) {
	    throw new java.lang.Error();
	}
	setBuildVersion("V2.0");

	// Login to the RBNB.
	send(login,TimerPeriod.STARTUP_WAIT);

	// Collect the <code>Server</code> response.
	Server server = (Server)
	    receive(serverClass,false,TimerPeriod.STARTUP_WAIT);
	if (server == null) {
	    throw new com.rbnb.api.AddressException
		("Login to server failed; no server appears to be available " +
		 "at the specified address.");
	}

	// Set our build date/version.
	if (server instanceof BuildInterface) {
	    setBuildDate(((BuildInterface) server).getBuildDate());
	    setBuildVersion(((BuildInterface) server).getBuildVersion());
	}

	if ((getClient().getParent() instanceof RemoteServer) &&
	    (getClient().getParent().getName() == null)) {
	    // Update our parent's name if it isn't set at all.
	    getClient().getParent().setName(server.getName());
	}

	// Update our name if necessary.
	Rmap child = server.getChildAt(0);
	if (getClient().compareNames(child) != 0) {
	    getClient().setName(child.getName());
	}
    }
 
    /**
     * Creates a new <code>ACO</code> for the specified <code>Client</code>.
     * <p>
     * This version uses the appropriate remote connection logic for the
     * <code>Server</code> address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code>.
     * @return the <code>ACO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address of the
     *		  <code>Client's Server</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not a child of a
     *		  <code>Server</code>.
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final static ACO newACO(Client clientI)
	throws com.rbnb.api.AddressException
    {
	ACO acoR = null;
	Server server = (Server) clientI.getParent();
	if (server == null) {
	    throw new java.lang.IllegalStateException
		(clientI + " is not a child of a server.");
	}

	String strAddr = (String) server.getAddress();
	int ss = strAddr.indexOf("://");

	if (ss == -1) {
	    acoR = new TCPACO(clientI);
	} else if (strAddr.substring(0,ss).equalsIgnoreCase("INTERNAL")) {
	    acoR = new RAMACO(clientI);
	} else if (strAddr.substring(0,ss).equalsIgnoreCase("TCP")) {
	    acoR = new TCPACO(clientI);
	} else {
	    throw new com.rbnb.api.AddressException
		(strAddr + " is not a valid address.");
	}

	return (acoR);
    }
 
    /**
     * Creates a new <code>ACO</code> for the specified <code>Client</code>.
     * <p>
     * This version uses an internal RAM connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code>.
     * @return the <code>ACO</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not a child of a
     *		  <code>Server</code>.
     * @since V2.0
     * @version 05/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final static ACO newRAMACO(Client clientI)
	throws com.rbnb.api.AddressException
    {
	ACO acoR = null;
	Server server = (Server) clientI.getParent();
	if (server == null) {
	    throw new java.lang.IllegalStateException
		(clientI + " is not a child of a server.");
	}

	return (new RAMACO(clientI));
    }

    /**
     * Receives a message from the <code>RCO</code> in the
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>= 0 means read anything that is already waiting,</li>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li><code>Client.FOREVER</code> means wait until a message shows
     *		  up or the connection is terminated.</li>
     *	      </ul>
     * @return the <code>Serializable</code> message received or null on a
     *	       timeout.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #send(com.rbnb.api.Serializable)
     * @see #send(com.rbnb.api.Serializable,long)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    abstract Serializable receive(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Receives a message from the <code>RCO</code>.
     * <p>
     * This version expects to see an object of the specified class as the
     * response. It also handles <code>RSVPs</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param classI   the expected response class.
     * @param ignoreI  ignore unexpected responses?
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>= 0 means read anything that is already waiting,</li>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li><code>Client.FOREVER</code> means wait until a message shows
     *		  up or the connection is terminated.</li>
     *	      </ul>
     * @return the <code>Serializable</code> message received or null on a
     *	       timeout.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #send(com.rbnb.api.Serializable)
     * @see #send(com.rbnb.api.Serializable,long)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final Serializable receive(Class classI,boolean ignoreI,long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (receive(classI,ignoreI,true,timeOutI));
    }

    /**
     * Receives a message from the <code>RCO</code>.
     * <p>
     * This version expects to see an object of the specified class as the
     * response. It also handles <code>RSVPs</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param classI   the expected response class.
     * @param ignoreI  ignore unexpected responses?
     * @param throwI   throw exception messages received from the remote?
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>= 0 means read anything that is already waiting,</li>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li><code>Client.FOREVER</code> means wait until a message shows
     *		  up or the connection is terminated.</li>
     *	      </ul>
     * @return the <code>Serializable</code> message received or null on a
     *	       timeout.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #send(com.rbnb.api.Serializable)
     * @see #send(com.rbnb.api.Serializable,long)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final Serializable receive(Class classI,
			       boolean ignoreI,
			       boolean throwI,
			       long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializableR;
	ExceptionMessage em = null;
	RSVP rsvp = null;
	while ((serializableR = receive(timeOutI)) != null) {
	    // Loop until we get a valid response.
	    rsvp = null;
	    if (serializableR instanceof RSVP) {
		// If we get a request for acknowledgement, strip out the child
		// and see if that is good.
		rsvp = (RSVP) serializableR;
		serializableR = rsvp.getSerializable();
	    }

	    if ((classI == null) || classI.isInstance(serializableR)) {
		// If the response is of the requested type, then return it.
		break;

	    } else if (serializableR instanceof ExceptionMessage) {
		// Always process <code>ExceptionMessages</code>.
		if (ignoreI && (classI == okClass)) {
		    // If we're synchronizing, then we need to keep looking for
		    // the synchronization.
		    if (em != null) {
			em = (ExceptionMessage) serializableR;
			continue;
		    }
		}
		break;

	    } else if (ignoreI) {
		// If we're ignoring bad responses, then skip anything else.
		continue;

	    } else {
		// Anything else is a serialization problem.
		com.rbnb.api.SerializeException e =
		    new com.rbnb.api.SerializeException
			("Unexpected response: " + serializableR +
			 ", expected: " + classI);
		send(new ExceptionMessage(e));
		throw e;
	    }
	}

	if (rsvp != null) {
	    // On an <code>RSVP</code>, send an <code>Acknowledge</code>.
	    send(new Acknowledge(rsvp.getIdentification(),null));
	}

	if (em != null) {
	    serializableR = em;
	}

	if (throwI && (serializableR instanceof ExceptionMessage)) {
	    Language.throwException((ExceptionMessage) serializableR);
	}

	return (serializableR);
    }

    /**
     * Updates the registration for this <code>Source</code>.
     * <p>
     * The input <code>Rmap</code> hierarchy is used to update the registration
     * for this <code>Source</code>.  The hierarchy may contain
     * <code>DataBlocks</code>, but not time information.  Those
     * <code>DataBlocks</code> are copied into the appropriate locations in the
     * registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the registration <code>Rmap</code> hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #reRegister(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    final void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Register register = new Register(rmapI);
	send(register);
	receive(okClass,false,Client.FOREVER);
    }

    /**
     * Replace the entire registration map for this <code>PlugIn</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the new registration map.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #register(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    final void reRegister(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Register register = new Register(rmapI,true);
	send(register);
    }

    /**
     * Reset the <code>Client</code>.
     * <p>
     * This method performs the functional equivalent of closing and re-opening
     * the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public final void reset()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	reset(getClient());
    }

    /**
     * Reset the <code>Client</code>.
     * <p>
     * This method performs the functional equivalent of closing and re-opening
     * the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code> to reset.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public final void reset(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Reset(clientI));
	receive(okClass,false,Client.FOREVER);
    }

    /**
     * Sends a message to the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> message.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #receive(long)
     * @see #receive(java.lang.Class,boolean,long)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Implemented using version w/ timeout.
     * 05/08/2001  INB	Created.
     *
     */
    final void send(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(serializableI,Client.FOREVER);
    }

    /**
     * Sends a message to the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> message.
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li>0 or <code>Client.FOREVER</code> means wait until a message
     *		  shows up or the connection is terminated.</li>
     *	      </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #receive(long)
     * @see #receive(java.lang.Class,boolean,long)
     * @since V2.1
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Created from version w/o timeout.
     *
     */
    abstract void send(Serializable serializableI,long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
     * @since V2.0
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	This method is no longer final.
     * 11/06/2002  INB	Created.
     *
     */
    public void setBuildDate(java.util.Date buildDateI) {
	buildDate = buildDateI;
    }

    /**
     * Sets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version.
     * @see #getBuildVersion()
     * @since V2.0
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	This method is no longer final.
     * 11/06/2002  INB	Created.
     *
     */
    public void setBuildVersion(String buildVersionI) {
	buildVersion = buildVersionI;
    }

    /**
     * Sets the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code>.
     * @see #getClient()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final void setClient(Client clientI) {
	client = clientI;
    }

    /**
     * Sets the client-side communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client-side communications object.
     * @see #getClientSide()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    void setClientSide(Object clientSideI) {
	clientSide = clientSideI;
    }

    /**
     * Starts the <code>ClientHandler</code> for the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is problem connecting to the RBNB.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #stop()
     * @since V2.0
     * @version 02/17/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/1/2004   MJM  catch System.getProperty exception for applets
     * 02/17/2004  INB	Added <code>RBNBFAILREVERSE</code> code for testing.
     * 05/07/2003  INB	Use <code>STARTUP_WAIT</code> instead of
     *			<code>PING_WAIT</code>.
     * 04/01/2003  INB	Don't wait forever for a response.
     * 05/08/2001  INB	Created.
     *
     */
     void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean RFR=false;  // mjm

	if (getClientSide() == null) {
	    createRCO();
	    login();

	    try {  // mjm
		RFR = (System.getProperty("RBNBFAILREVERSE") != null);
	    } catch (Exception e) {
		System.err.println("Denied RBNBFAILREVERSE property, using default (applet?)");
		// ignore 
	    }

	    if ( RFR &&
		getClient().getName().startsWith("_RR.")) {
		throw new java.lang.IllegalStateException
		    ("Lost synchronization with RBNB server.");

	    } else {
		connectData();
	    }
	    start(getClient());

	    if ((getClient().getUsername() != null) &&
		IsSupported.isSupported(IsSupported.FEATURE_USERNAMES,
					getBuildVersion(),
					getBuildDate())) {
		send(getClient().getUsername());
		if (receive(okClass,false,TimerPeriod.STARTUP_WAIT) == null) {
		    throw new com.rbnb.api.AddressException
			("Failed to start up handler in a reasonable " +
			 "amount of time.");
		}
	    }
	}
    }

    /**
     * Starts the <code>ClientHandler</code> for the specified
     * <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #stop(com.rbnb.api.Client)
     * @since V2.0
     * @version 05/07/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/07/2003  INB	Use <code>STARTUP_WAIT</code> rather than
     *			<code>PING_WAIT</code>.
     * 04/01/2003  INB	Don't wait forever.
     * 05/08/2001  INB	Created.
     *
     */
    final void start(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Start(clientI));
	if (receive(okClass,false,TimerPeriod.STARTUP_WAIT) == null) {
	    throw new com.rbnb.api.AddressException
		("Failed to start up " + clientI +
		 " in a reasonable amount of time.");
	}
    }

    /**
     * Starts a shortcut peer connection to another server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>Shortcut</code> to start.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #stop(com.rbnb.api.Shortcut)
     * @since V2.0
     * @version 05/07/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/07/2003  INB	Use <code>STARTUP_WAIT</code> rather than
     *			<code>PING_WAIT</code>.
     * 04/01/2003  INB	Don't wait forever.
     * 01/08/2002  INB	Created.
     *
     */
    final void start(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Start(shortcutI));
	if (receive(okClass,false,TimerPeriod.STARTUP_WAIT) == null) {
	    throw new com.rbnb.api.AddressException
		("Failed to start up " + shortcutI +
		 " in a reasonable amount of time.");
	}
    }

    /**
     * Stops the <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address of the
     *		  <code>ClientHandler</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #start()
     * @since V2.0
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Moved all of the code into the
     *			an<code>stop(com.rbnb.api.ClientHandler)</code> method.
     * 05/08/2001  INB	Created.
     *
     */
    final void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	stop(getClient());
    }

    /**
     * Stops the <code>ClientHandler</code> for the specified
     * <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #stop(com.rbnb.api.Client)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Use timeout when stopping a router.
     * 03/04/2003  INB	Don't try to synchronize here.
     * 02/26/2003  INB	Moved code to special stop ourself into here. Wait a
     *			limited period of time if the <code>Client</code> being
     *			stopped is ourself.
     * 05/08/2001  INB	Created.
     *
     */
    final void stop(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long timeOut = Client.FOREVER;
	if ((clientI == getClient()) ||
	    (clientI.getFullName().equals(getClient().getFullName()))) {
	    stopping = true;
	    if (clientI instanceof RouterInterface) {
		timeOut = TimerPeriod.SHUTDOWN_ROUTER;
	    } else {		
		timeOut = TimerPeriod.SHUTDOWN;
	    }
	}
	send(new Stop(clientI),timeOut);
	receive(okClass,clientI == getClient(),timeOut);
	if (stopping) {
	    destroyRCO();
	    disconnectData();
	}
    }

    /**
     * Stops the <code>ServerHandler</code> for the specified
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI the server to disconnect from. This <code>Server</code>
     *		      object should be the bottom of the hierarchy representing
     *		      the <code>Server</code> of interest or it should have
     *		      just an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 03/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/04/2003  INB	Only wait a short period for the response.
     * 05/08/2001  INB	Created.
     *
     */
    final void stop(Server serverI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server top = (Server) serverI.newInstance(),
	       current = serverI,
	       build;

	while ((current.getParent() != null) &&
	       (current.getParent() instanceof Server)) {
	    current = (Server) current.getParent();
	    build = (Server) current.newInstance();
	    build.addChild(top);
	    top = build;
	}

	send(new Stop(top));
	receive(okClass,
		false,
		TimerPeriod.SHUTDOWN_ROUTER);
    }

    /**
     * Stops a shortcut peer connection to another server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>Shortcut</code> to stop.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #stop(com.rbnb.api.Shortcut)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    final void stop(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Stop(shortcutI));
	receive(okClass,false,Client.FOREVER);
    }

    /**
     * Synchronizes the <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing something.
     * @exception java.io.EOFException
     *		  thrown if the connection is closed.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>ClientHandler</code> is already running.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Wait a limited period of time if we're stopping.
     * 05/08/2001  INB	Created.
     *
     */
    final void synchronizeWserver()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(new Ping());
	receive(okClass,
		true,
		(stopping ?
		 TimerPeriod.SHUTDOWN :
		 Client.FOREVER));
    }
}
