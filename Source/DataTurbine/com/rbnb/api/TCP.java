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
 * Extended <code>Address</code> to handle TCP addresses.
 * <p>
 * This class provides the fields and methods for handling TCP addresses.
 * <p>
 * TCP addresses take the form:
 * <p>
 * [tcp://][host][:port]
 * <p><ul>
 * <li>[tcp://] indicates that the string "tcp://" is optional,</li>
 * <li>[host] indicates that the host DNS name or IP address may be
 *     supplied. The default is localhost, and</li>
 * <li>[:port] indicates the socket port ID may be supplied following a colon
 *     (:) character. The default is 3333.</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 06/16/2011
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/16/2011  JPW      In accept(), added catch SocketTimeoutException;
 *                      ServerSocket.accept() will throw this exception on
 *                      timeout.  This is a Java 1.4 feature.
 * 10/18/2010  MJM      Added SoLinger(false) on socket close to ensure connection not hung
 * 09/15/2010  MJM      Edited OpenSocket to use default listen-on-all-interfaces
 * 			behavior for corresponding default "localhost" connection.
 * 11/12/2004  JPW	In order to compile under J# (which is only Java 1.1.4
 *			compatable) I added preprocessor directives to be used
 *			by sed to comment out calls to Throwable.initCause()
 *			in accept() and connect().
 * 11/11/2004  JPW	Integrate security changes from NEES into two methods:
 *			    accept() and connect()
 * 10/05/2004  JPW	In close(): added preprocessor directives which can be
 *			used by "sed" to create a version of this class file
 *			appropriate for compilation under J#.
 * 08/04/2004  JPW      In accept(), add check on "Timeout" in exception
 *                      message (this is so the code will run under J#).
 * 04/27/2004  INB	Added the internal <code>SocketConnector</code> class
 *			to allow for timeouts during connect operations.  While
 *			Java 1.4 has a <code>Socket.connect</code> method that
 *			allows for a timeout, this code works for pre-Java 1.4
 *			JVMs.
 * 10/03/2003  INB	Set the backlog large enough to ensure that we can
 *			load all of the archives and still have a few slots
 *			for additional connections when creating a server
 *			side socket.
 * 04/09/2003  INB	Added <code>checkForQualifiedName</code> logic.
 * 01/10/2001  INB	Created.
 *
 */
class TCP
    extends com.rbnb.api.Address
{
    // Private class fields:
    private static java.net.InetAddress localhost = null;
    private static String localhostName = null;
    private static java.util.Hashtable hostMap = new java.util.Hashtable();

    // Private fields:
    /**
     * the host <code>java.net.InetAddress</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/16/2001
     */
    private java.net.InetAddress host = null;

    /**
     * the socket port ID.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/16/2001
     */
    private int port = Integer.MIN_VALUE;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #TCP(String)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2001  INB	Created.
     *
     */
    TCP() {
	super();
    }

    /**
     * Class constructor to build a <code>TCP</code> object for the input
     * address.
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
     * 01/10/2001  INB	Created.
     *
     */
    TCP(String addressI)
	throws com.rbnb.api.AddressException
    {
	super(addressI);
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
     * @see #newServerSide(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 06/16/2011
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/16/2011  JPW  Add catch SocketTimeoutException; ServerSocket.accept()
     *                  will throw this exception on timeout.  This is a
     *                  Java 1.4 feature.
     * 11/12/2004  JPW  Add sed preprocessor directives such that in the J#
     *                  version of the code the call to Throwable.initCause()
     *                  is commented out.
     * 11/11/2004  JPW  Add security code from NEES
     * 08/04/2004  JPW  Add check on "Timeout" in exception message (this is
     *                  so the code will run under J#)
     * 05/11/2001  INB	Created.
     *
     */
    final Object accept(Object serverSideI,long timeOutI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.net.ServerSocket ss = (java.net.ServerSocket) serverSideI;
	java.net.Socket socket;
	java.net.InetAddress netAddr = null;
	Object connectionR = null;
	String authenticate = null;

	// Turn on the timeout for the socket.
	if ((timeOutI == 0) || (timeOutI == Client.FOREVER)) {
	    ss.setSoTimeout(0);
	} else {
	    ss.setSoTimeout((int) timeOutI);
	}

	try {
	    // log4j.debug("server socket accepting");
	    socket = ss.accept();
	    // log4j.debug("server socket accepted");
	    
	    // JPW 11/11/2004: Security code from NEES
	    try {
		authenticate = System.getProperty("com.rbnb.authenticate");
	    } catch (Exception e) {  //  catch and ignore exception (applets)
		authenticate = null;
	    }

	    if (authenticate == null) {
		authenticate = "false";
	    };

	    // log4j.debug("authentication set to: " + authenticate);

	    if (!authenticate.equals("false")) {

		try {

		    String factoryProperty="com.rbnb.securityProviderFactory";
		    String factoryName = System.getProperty(factoryProperty);
		    if (factoryName == null) {
			throw new Exception(
			    "Property not specified: " + factoryProperty);
		    }
		    Class factoryClass = Class.forName(factoryName);
		    SecurityProviderFactory factory = (SecurityProviderFactory)
			factoryClass.newInstance();
		    SecurityProvider provider = factory.create(this);
		    
		    provider.serverSideAuthenticate(
		        socket.getInputStream(),
			socket.getOutputStream());

		} catch (Exception gsse) {
		    // usually org.ietf.jgss.GSSException
		    // log4j.error(
		    //     "Authentication failed: " + gsse.toString());
		    socket.close();
		    java.io.IOException hack =
			new java.io.IOException(gsse.toString());
		    // JPW 11/12/2004: Add preprocessor directives which can be
		    //                 used by "sed" to create a version of the
		    //                 code appropriate for compilation under
		    //                 J# (which supports Java 1.1.4).  In the
		    //                 Java 1.1.4 version, don't call
		    //                 Throwable.initCause()
		    //#open_java2_comment#
		    hack.initCause(gsse);
		    //#close_java2_comment#
		    throw hack;
		}
	    }
	    
	    if (getAuthorization() != null) {
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
		    if (!getAuthorization().isAllowed(addr)) {
			socket.close();
			throw new com.rbnb.api.AddressException
			    (netAddr + " is not authorized to connect.");
		    }

		} catch (java.lang.Exception e) {
		    if ((e instanceof com.rbnb.api.AddressException) ||
			(e instanceof java.io.IOException) ||
			(e instanceof java.lang.InterruptedException)) {
			try {
			    Language.throwException(e);
			} catch (com.rbnb.api.SerializeException e1) {
			}
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
			 " was denied access due to an exception.\n" +
			 message);
		}
	    }
	    setup(socket);
	    connectionR = socket;

	} catch (java.net.SocketTimeoutException e) {
	    // JPW 06/16/2011: Added check on SocketTimeoutException
	    //                 ServerSocket.accept() will throw this exception
	    //                 on timeout.
	    //
	    // NOTE THAT THIS IS A JAVA 1.4 FEATURE
	    //
	    // This is a benign exception; don't do anything.
	    // System.err.println("socket timeout:\n" + e);
	} catch (java.io.InterruptedIOException e) {
	    // JPW 08/04/2004: Add check on "Timeout" in exception message
	    //                 (this is so the code will run under J#)
	    if ((e.getMessage() != null) &&
		(e.getMessage().indexOf("timed out") == -1) &&
		(e.getMessage().indexOf("Timeout") == -1)) {
		throw e;
	    }
	} catch (java.net.SocketException e) {
	    if (e.getMessage() == null) {
	    } else if (e.getMessage().indexOf("Interrupted") != -1) {
		throw new java.io.InterruptedIOException(e.getMessage());
	    } else if (e.getMessage().indexOf("timed out") == -1) {
		throw e;
	    }
	}

	return (connectionR);
    }
    
    /**
     * Builds a valid TCP address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the address object.
     * @return the valid TCP address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if the address cannot be made into a valid one.
     * @since V2.0
     * @version 04/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/09/2003  INB	Added call to <code>checkForQualifiedName</code>.
     * 05/02/2001  INB	Created.
     *
     */
    public final static String buildAddress(String addressI)
	throws com.rbnb.api.AddressException
    {
	String addressR = (String) addressI,
	       lhost = "localhost",
	       lport = "3333";
	if (addressR != null) {
	    if (addressR.indexOf("tcp://") == 0) {
		addressR = addressR.substring(6);
	    }
	    int colon = addressR.indexOf(":");

	    if (colon == 0) {
		lport = addressR.substring(colon + 1);
	    } else if (colon == -1) {
		lhost = addressR;
	    } else if (colon < addressR.length() - 1) {
		lhost = addressR.substring(0,colon);
		lport = addressR.substring(colon + 1);
	    } else if (colon == addressR.length() - 1) {
		lhost = addressR.substring(0,colon);
	    }
	}

	java.net.InetAddress addr = null;
	String upcase;
	Object mapped = null;
	if (lhost.equalsIgnoreCase("localhost") ||
	    ((localhostName != null) &&
	     lhost.equalsIgnoreCase(localhostName))) {
	    addr = getLocalHost();

	} else if ((mapped =
		    hostMap.get(upcase = lhost.toUpperCase())) == null) {
	    try {
		java.net.InetAddress[] addresses =
		    java.net.InetAddress.getAllByName(lhost);

		if ((addr = checkForQualifiedName(addresses)) != null) {
		    lhost = addr.getHostName();

		} else {
		    java.net.InetAddress addr1 =
			java.net.InetAddress.getByName
			(addresses[0].getHostAddress());
		    java.net.InetAddress[] addresses2;
		    for (int idx = 0; idx < addresses.length; ++idx) {
			addresses2 = java.net.InetAddress.getAllByName
			    (addresses[idx].getHostAddress());
			if ((addr = checkForQualifiedName
			     (addresses2)) != null) {
			    lhost = addr.getHostName();
			    break;
			}
		    }
		    if (addr == null) {
			addr = addr1;
		    }
		}
		hostMap.put(upcase,addr);

	    } catch (java.net.UnknownHostException e) {
		hostMap.put(upcase,new String(lhost));
	    }

	} else if (mapped instanceof java.net.InetAddress) {
	    addr = (java.net.InetAddress) mapped;
	}
	    

	if ((lhost.indexOf(".") == -1) && (addr != null)) {
	    String hostName = addr.getHostName();
	    if (hostName.indexOf(".") == -1) {
		lhost = addr.getHostAddress();
	    } else {
		lhost = hostName;
	    }
	}

	try {
	    int portI = Integer.parseInt(lport);
	} catch (java.lang.NumberFormatException e) {
	    throw new com.rbnb.api.AddressException
		(addressI + " is not a valid TCP address.");
	}

	addressR = "tcp://" + lhost + ":" + lport;
	return (addressR);
    }

    /**
     * Checks a list of addresses for a fully qualified name that is not an
     * address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressesI the list to check.
     * @return the entry with the qualified name or null.
     * @since V2.1
     * @version 04/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/09/2003  INB	Created.
     *
     */
    private final static java.net.InetAddress checkForQualifiedName
	(java.net.InetAddress[] addressesI)
    {
	java.net.InetAddress addrR = null;

	for (int idx = 0;
	     (addrR == null) &&
		 (idx < addressesI.length);
	     ++idx) {
	    if ((addressesI[idx].getHostName().indexOf(".") != -1) &&
		!addressesI[idx].getHostName().equals
		(addressesI[idx].getHostAddress())) {
		addrR = addressesI[idx];
	    }
	}

	return (addrR);
    }

    /**
     * Closes a connection object for this <code>TCP</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param connectionI the connection object.
     * @since V2.0
     * @version 10/05/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/05/2004  JPW	Added preprocessor directives which can be used by
     *			"sed" to manipulate the Java calls made to close down
     *			the socket.  This was done to support a version of the
     *			code for compilation under J#.
     * 05/15/2001  INB	Created.
     *
     */
    final void close(Object connectionI) {
	try {
	    if (connectionI instanceof java.net.ServerSocket) {
		java.net.ServerSocket sSocket =
		    (java.net.ServerSocket) connectionI;
		sSocket.close();
	    } else if (connectionI instanceof java.net.Socket) {
		java.net.Socket socket = (java.net.Socket) connectionI;
		try {
		    // JPW 10/05/2004: Add preprocessor directives which can be
		    //                 used by "sed" to create a version of the
		    //                 code appropriate for compilation under
		    //                 J# (which supports Java 1.1.4).  In the
		    //                 Java 1.1.4 version, don't call
		    //                 Socket.shutdownInput() or
		    //                 Socket.shutdownOutput().
		    //#open_java2_comment#
		    socket.shutdownInput();
		    socket.shutdownOutput();
		    //#close_java2_comment#
		    // The Java documentation says that the call to close()
		    // does not actually do anything.
		    //#java11_line# socket.getInputStream().close();
		    //#java11_line# socket.getOutputStream().close();
		} catch (java.lang.NoSuchMethodError e) {
		}
		socket.close();
	    }
	} catch (java.lang.Exception e) {
	}
    }

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
     * 11/12/2004  JPW  Add sed preprocessor directives such that in the J#
     *                  version of the code the call to Throwable.initCause()
     *                  is commented out.
     * 11/11/2004  JPW	Add security code from NEES
     * 05/11/2001  INB	Created.
     *
     */
    final void connect(Object clientSideI)
	throws java.io.IOException,
	       java.lang.InterruptedException
    {
	String authenticate = null;

	// JPW 11/11/2004: Add security code from NEES
	//                 (previously this was an empty method)
	java.net.Socket socketR = (java.net.Socket)clientSideI;

	try {
	    authenticate = System.getProperty("com.rbnb.authenticate");
	} catch (Exception e) {  //  catch and ignore exception (applets)
	    authenticate = null;
	}

	if (authenticate == null) {
	    authenticate = "false";
	};

	// log4j.debug("authentication set to: " + authenticate);

	if (!authenticate.equals("false")) {
	
	  try {

	      String factoryProperty="com.rbnb.securityProviderFactory";
	      String factoryName = System.getProperty(factoryProperty);
	      if (factoryName == null) {
		  throw new Exception(
		      "Property not specified: " + factoryProperty);
	      }
	      Class factoryClass = Class.forName(factoryName);
	      SecurityProviderFactory factory = (SecurityProviderFactory)
		  factoryClass.newInstance();
	      // 2005/01/20  WHF  Now passes this as an input: 
	      SecurityProvider provider = factory.create(this);
		    
	      provider.clientSideAuthenticate(
	          socketR.getInputStream(),
		  socketR.getOutputStream());

	  } catch (Exception gsse) {
	      // 2005/01/21  WHF  If authentication fails, close socket:
	      socketR.close();
	      
	      // log4j.error("Authentication failed: " + gsse.toString());
	      java.io.IOException hack =
		  new java.io.IOException(gsse.toString());
	      // JPW 11/12/2004: Add preprocessor directives which can be
	      //                 used by "sed" to create a version of the
	      //                 code appropriate for compilation under
	      //                 J# (which supports Java 1.1.4).  In the
	      //                 Java 1.1.4 version, don't call
	      //                 Throwable.initCause()
	      //#open_java2_comment#
	      hack.initCause(gsse);
	      //#close_java2_comment#
	      throw hack;
	  }
	}//authenticate
        
    }

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
    final void disconnect(Object clientSideI)
	throws java.io.IOException,
	       java.lang.InterruptedException
    {
    ((java.net.Socket) clientSideI).setSoLinger(false, 0);	// MJM:  don't hang on lost network (Android especially)
	((java.net.Socket) clientSideI).close();
    }

    /**
     * Gets the host <code>java.net.InetAddress</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the host <code>java.net.InetAddress</code>.
     * @see #setHost(java.net.InetAddress)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    private final java.net.InetAddress getHost() {
	return (host);
    }

    /**
     * Gets the local host <code>java.net.InetAddress</code>
     * <p>
     *
     * @author Ian Brown
     *
     * @return the local host <code>java.net.InetAddress</code>.
     * @since V2.0
     * @version 12/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final static java.net.InetAddress getLocalHost() {
	if (localhost == null) {
	    // If the local host has not been set yet, try to figure out what
	    // to use.
	    try {
		localhost = java.net.InetAddress.getLocalHost();
		if ((localhostName =
		     localhost.getHostName()).indexOf(".") == -1) {
		    localhost =
			java.net.InetAddress.getByName
			(localhost.getHostAddress());
		    if ((localhostName =
			 localhost.getHostName()).indexOf(".") == -1) {
			localhostName = localhost.getHostAddress();
		    }
		}

	    } catch (Exception e) {
		// On any exception, we leave the field blank. This probably
		// means that there is no way to use TCP, but we'll hope for
		// better later.
	    }
	}

	return (localhost);
    }

    /**
     * Gets the socket port ID.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the socket port ID.
     * @see #setPort(int)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    private final int getPort() {
	return (port);
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
     * 2007/11/26  WHF  Removed.  Was unused, and the check below does not work
     *   in multi-homed environments.
     *
     */
/*    final boolean isLocal() {
	return (host == getLocalHost());
    } */

    /**
     * Gets a socket connection to the address of this <code>TCP</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acoI the <code>ACO</code>.
     * @return the connection object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem connecting to the address.
     * @exception java.io.IOException
     *		  thrown if an I/O error occurs during the socket creation.
     * @exception java.lang.SecurityException
     *		  thrown if a security manager exists and doesn't allow the
     *		  connection.
     * @see #newServerSide(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 04/27/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2004  INB	Use new <code>SocketConnector</code> to allow us to
     *			timeout while waiting for the connection to be made
     *			rather than waiting forever.
     * 01/10/2001  INB	Created.
     *
     */
    final Object newClientSide(ACO acoI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException,
	       java.lang.SecurityException
    {
	java.net.Socket socketR = null;

	synchronized (this) {
	    if (getHost() == null) {
		parseAddress(getAddress());
	    }
	}
	if (getHost() == null) {
	    throw new com.rbnb.api.AddressException
		("Unable to locate host for address " + getAddress());
	}

	// Create a socket connector to make the socket and wait for it.  If
	// the connection isn't made in a reasonable amount of time, this will
	// produce an address exception.
	SocketConnector sc = new SocketConnector();
	sc.start();
	socketR = sc.waitForSocket(TimerPeriod.STARTUP_WAIT);

	// Set it up the way we want it.
	setup(socketR);

	return (socketR);
    }

    /**
     * Gets a server socket.
     * <p>
     * The address must be on the local host, as determined by
     * {@link java.net.ServerSocket(int, int, java.net.InetAddress) }. 
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverHandlerI the <code>ServerHandler</code>.
     * @return the connection object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if the address is for a something other than the local
     *		  host.
     * @exception java.io.IOException
     *		  thrown if an I/O error occurs during the socket creation.
     * @exception java.lang.SecurityException
     *		  thrown if a security manager exists and doesn't allow the
     *		  connection.
     * @see #newClientSide(com.rbnb.api.ACO)
     * @since V2.0
     * @version 11/26/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/26/2007  WHF  Supports multi-homed machines.
     * 10/03/2003  INB	Set the backlog large enough to ensure that we can
     *			load all of the archives and still have a few slots
     *			for additional connections.
     * 01/11/2001  INB	Created.
     *
     */
    final Object newServerSide(ServerHandler serverHandlerI)
	throws com.rbnb.api.AddressException,
	       java.io.IOException,
	       java.lang.SecurityException
    {
	synchronized (this) {
	    if (getHost() == null) {
		parseAddress(getAddress());
	    }
	}
	// 2007/11/26  WHF  The following logic will not work on a computer
	//  with more than one adapter.
/*	if ((getHost() == null) || !getHost().equals(getLocalHost())) {
	    throw new com.rbnb.api.AddressException
		(getAddress() +
		 " cannot be used for a server socket; it is not on the " +
		 "local machine.");
	}
*/
	RBNB sh = (RBNB) serverHandlerI;
	int backlog = Math.max(100,sh.getMaxActivityThreads()*2 + 8);

	// 2007/11/26  WHF  Instead, let this constructor throw in the case
	//  where the host cannot be resolved to a local binding:
	
	// MJM 9/3/10:  if host="localhost", bind to all interfaces by dropping host arg.
	//				Some recent versions of Linux started to inconsistently treat 
	//				an explicit "localhost" as only-local-connections vs the old 
	//				default behavior of all local interfaces.
	
//	System.err.println("============getHost: "+getHost()+", getLocalHost: "+getLocalHost());
	if(getHost().equals(getLocalHost())) {
		System.err.println("ServerSocket: "+host+", listening on all interfaces...");
		return (new java.net.ServerSocket(getPort(),backlog));
	} else
		return (new java.net.ServerSocket(getPort(),backlog, host));

    }

    /**
     * Parses the input address into a host and port.
     * <p>
     * TCP addresses take the form:
     * <p>
     * [tcp://][host][:port]
     * <p><ul>
     * <li>[tcp://] indicates that the string "tcp://" is optional,</li>
     * <li>[host] indicates that the host DNS name or IP address may be
     *     supplied. The default is localhost, and</li>
     * <li>[:port] indicates the socket port ID may be supplied following a
     *     colon (:) character. The default is 3333.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param addressI  the address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem parsing the address.
     * @see #setHost(java.net.InetAddress)
     * @see #setPort(int)
     * @since V2.0
     * @version 03/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    private final void parseAddress(String addressI)
	throws com.rbnb.api.AddressException
    {
	if (addressI != null) {
	    // If there is an address supplied, try to parse it.

	    String lAddress = addressI,
		   hostName = null;
	    if ((lAddress.length() >= 6) &&
		lAddress.substring(0,6).equalsIgnoreCase("tcp://")) {
		lAddress = lAddress.substring(6);
	    }

	    // Start by finding the optional colon that should separate the
	    // host from the port.
	    int colon = lAddress.indexOf(":");

	    if (colon == -1) {
		// If there is no colon, then the address is just the host
		// name; the port defaults.
		hostName = lAddress;
		setPort(3333);
	    } else if (colon == 0) {
		// If the colon is the first character, then the address is
		// just the port; the host defaults.
		setPort(Integer.parseInt(lAddress.substring(1)));
	    } else if (colon == lAddress.length() - 1) {
		// If the colon is the last character, then the address is hust
		// the host name; the port defaults.
		hostName = lAddress.substring(0,colon);
		setPort(3333);
	    } else {
		// If the colon appears in the middle of the address, then the
		// address contains both the host and the port.
		hostName = lAddress.substring(0,colon);
		setPort(Integer.parseInt(lAddress.substring(colon + 1)));
	    }

	    if (hostName != null) {
		// If the host name was specified, get the corresponding
		// address.

		java.net.InetAddress addr = null;
		String upcase = null;
		Object mapped = null;
		if (hostName.equalsIgnoreCase("localhost") ||
		    ((localhostName != null) &&
		     (hostName.equalsIgnoreCase(localhostName)))) {
		    addr = getLocalHost();

		} else if ((mapped = hostMap.get
			    (upcase = hostName.toUpperCase())) == null) {
		    // If the host name is not "localhost", convert it to an
		    // java.net.InetAddress.
		    try {
			addr = java.net.InetAddress.getByName(hostName);
			hostMap.put(upcase,addr);
		    } catch (java.lang.Exception e) {
			addr = null;
		    }

		} else if (mapped instanceof java.net.InetAddress) {
		    addr = (java.net.InetAddress) mapped;
		}

		setHost(addr);
	    }
	}
    }

    /**
     * Sets the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the new address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @see #getAddress()
     * @since V2.0
     * @version 03/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final synchronized void setAddress(String addressI)
	throws com.rbnb.api.AddressException
    {
	parseAddress(addressI);
	if ((addressI.indexOf(".") != -1) || (getHost() == null)) {
	    if (addressI.indexOf("tcp://") == 0) {
		super.setAddress(addressI);
	    } else {
		super.setAddress("tcp://" + addressI);
	    }
	} else {
	    String hostName = getHost().getHostName();
	    if (hostName.indexOf(".") != -1) {
		super.setAddress
		    ("tcp://" + hostName + ":" + getPort());
	    } else {
		super.setAddress
		    ("tcp://" + getHost().getHostAddress() + ":" + getPort());
	    }
	}
    }

    /**
     * Sets the host <code>java.net.InetAddress</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hostI  the host <code>java.net.InetAddress</code>.
     * @see #getHost()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    private final void setHost(java.net.InetAddress hostI) {
	host = hostI;
    }

    /**
     * Sets the socket port ID.
     * <p>
     *
     * @author Ian Brown
     *
     * @param portI  the socket port ID.
     * @see #getPort()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INb	Created.
     *
     */
    private final void setPort(int portI) {
	port = portI;
    }

    /**
     * Sets up a TCP socket for use with by the RBNB code.
     * <p>
     *
     * @author Ian Brown
     *
     * @param socketI  the socket.
     * @exception java.net.SocketException
     *		  thrown if there is problem setting up the socket.
     * @since V2.0
     * @version 03/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    final static void setup(java.net.Socket socketI)
	throws java.net.SocketException
    {
	// Turn on various settings to ensure that the socket operates the way
	// we want it to. If we can't get exactly what we want, accept whatever
	// we get.
	try {
	    socketI.setTcpNoDelay(true);
	} catch (java.lang.NoSuchMethodError e) {
	}
	try {
	    socketI.setSoLinger(true,1000);
	} catch (java.lang.NoSuchMethodError e) {
	}
	/*
	try {
	    socketI.setKeepAlive(true);
	} catch (java.lang.NoSuchMethodError e) {
	}
	*/
    }

    /**
     * Internal class for connecting sockets with a timeout.
     * <p>
     * Use this rather than the <code>Socket.connect</code> method that takes a
     * timeout because this is usable with pre-Java 1.4 JVMs.
     *
     * @author Ian Brown
     *
     * @since V2.2.3
     * @version 04/27/2004
     */

    /*
     * Copyright 2004 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2004  INB	Created.
     *
     */
    private final class SocketConnector
	extends java.lang.Thread
    {

	/**
	 * the socket.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2.3
	 * @version 04/27/2004
	 */
	private java.net.Socket socket = null;

	/**
	 * the throwable caught if an error occurs.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2.3
	 * @version 04/27/2004
	 */
	private java.lang.Throwable throwable = null;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public SocketConnector() {
	    super();
	}

	/**
	 * Gets the socket.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the socket.
	 * @see #setSocket(java.net.Socket socketI)
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public final java.net.Socket getSocket() {
	    return (socket);
	}

	/**
	 * Gets the throwable.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the throwable.
	 * @see #setThrowable(java.lang.Throwable throwableI)
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public final Throwable getThrowable() {
	    return (throwable);
	}

	/**
	 * Runs the connector.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public final void run() {
	    try {
		java.net.Socket lSocket = new java.net.Socket(getHost(),
							      getPort());
		setSocket(lSocket);

	    } catch (java.lang.Exception e) {
		setThrowable(e);
	    } catch (java.lang.Error e) {
		setThrowable(e);
	    }
	}

	/**
	 * Sets the socket.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param socketI the socket.
	 * @see #getSocket()
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public final synchronized void setSocket(java.net.Socket socketI) {
	    socket = socketI;
	    notifyAll();
	}

	/**
	 * Sets the throwable.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param throwableI the throwable.
	 * @see #getThrowable()
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public final synchronized void setThrowable(Throwable throwableI) {
	    throwable = throwableI;
	    notifyAll();
	}

	/**
	 * Waits for the socket.
	 * <p>
	 * If the socket is set within a reasonable amount of time, this method
	 * will return it.  Otherwise, it interrupts the connection and throws
	 * an addressing exception.  If the socket has an exception, then this
	 * method will propagate it.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param timeOutI the timeout (milliseconds).
	 * @return the socket.
	 * @exception com.rbnb.api.AddressException
	 *	      thrown if there is a problem connecting to the address.
	 * @exception java.io.IOException
	 *	      thrown if an I/O error occurs during the socket creation.
	 * @exception java.lang.SecurityException
	 *	      thrown if a security manager exists and doesn't allow the
	 *	      connection.
	 * @since V2.2.3
	 * @version 04/27/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/27/2004  INB	Created.
	 *
	 */
	public final synchronized java.net.Socket waitForSocket(long timeOutI)
	    throws com.rbnb.api.AddressException,
		   java.io.IOException,
		   java.lang.SecurityException
	{
	    long startAt = System.currentTimeMillis();
	    long nowAt;
	    long period = TimerPeriod.NORMAL_WAIT;
	    try {
		// Wait for the socket to connect.  Break if an exception
		// occurs or if we have waited for a reasonable amount of
		// time.
		while ((getSocket() == null) && (getThrowable() == null) &&
		       isAlive() &&
		       ((nowAt = System.currentTimeMillis()) - startAt <
			timeOutI)) {
		    period = Math.min(period,
				      timeOutI - (nowAt - startAt));
		    wait(period);
		}

	    } catch (java.lang.InterruptedException e) {
		throw new com.rbnb.api.AddressException
		    ("Interrupted while waiting for connection.");
	    }

	    if (getThrowable() != null) {
		// If the socket failed to connect for some reason, then
		// propagate the exception.
		getThrowable().fillInStackTrace();
		if (getThrowable() instanceof com.rbnb.api.AddressException) {
		    throw (com.rbnb.api.AddressException) getThrowable();
		} else if (getThrowable() instanceof java.io.IOException) {
		    throw (java.io.IOException) getThrowable();
		} else if (getThrowable() instanceof
			   java.lang.SecurityException) {
		    throw (java.lang.SecurityException) getThrowable();
		} else if (getThrowable() instanceof
			   java.lang.RuntimeException) {
		    throw (java.lang.RuntimeException) throwable;
		} else {
		    // Other types of exceptions are propagated as address
		    // exceptions.
		    throw new com.rbnb.api.AddressException
			("Failed to connect to address.\n" + getThrowable());
		}

	    } else if (getSocket() == null) {
		// If the socket did not get connected and there was no error,
		// then we timed out here.  Throw an address exception after
		// interrupting the thread.
		interrupt();
		throw new com.rbnb.api.AddressException
		    ("Failed to connect to server in a reasonable amount " +
		     "of time.");
	    }

	    return (getSocket());
	}
    }
}
