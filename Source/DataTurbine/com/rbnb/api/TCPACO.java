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
 * Extended <code>SerializingACO</code> that communicates via a TCP socket.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 07/20/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/20/2004  INB	Copy our build information across to an RCO rather than
 *			loading from the local side.
 * 04/17/2003  INB	Added <code>setBuildDate</code> and
 *			<code>setBuildVersion</code> methods.
 * 05/09/2001  INB	Created.
 *
 */
final class TCPACO
    extends com.rbnb.api.SerializingACO
{
    /**
     * the data socket.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #dataSocket
     * @since V2.0
     * @version 05/09/2001
     */
    private java.net.Socket dataSocket = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    TCPACO() {
	super();
    }

    /**
     * Class constructor to build a <code>TCPACO</code> for the specified
     * <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code..
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the <code>TCP</code>
     *		  address.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    TCPACO(Client clientI)
	throws com.rbnb.api.AddressException
    {
	super(clientI);
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
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    private final java.net.Socket getDataSocket() {
	return (dataSocket);
    }

    /**
     * Connects the data socket.
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
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final void connectData()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Address address = ((Server)
			   getClient().getParent()).getAddressHandler();
	setDataSocket((java.net.Socket) address.newClientSide(this));
	address.connect(getDataSocket());

	Language tSer = new Language
	    (new InputStream(getDataSocket().getInputStream(),
			     false,
			     0),
	     null,
	     new OutputStream(getDataSocket().getOutputStream(),
			      false,
			      0),
	     null);
	tSer.write((ClientHandle) getClient());
	tSer.getOcontrol().setBinary(true);
	tSer.getIcontrol().setBinary(true);
	metricsBytes += tSer.bytesTransferred();
	tSer.close();
	getSerialize().setOdata
	    (new DataOutputStream(getDataSocket().getOutputStream(),32768));
	getSerialize().setIdata
	    (new DataInputStream(getDataSocket().getInputStream(),32768));
	if (getSerialize().getIdata().read() != 1) {
	    throw new java.lang.IllegalStateException
		("Lost synchronization with RBNB server.");
	}
    }

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
     * @version 07/20/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/20/2004  INB	Copy our build information across rather than loading
     *			from the local side.
     * 01/25/2002  INB	Created.
     *
     */
    final RCO convertToRCO(ServerHandler rbnbI)
	throws java.io.IOException,
	       java.lang.InterruptedException
    {
	TCPRCO rcoR = new TCPRCO(null,rbnbI);

	rcoR.setServerSide(getClientSide());
	rcoR.setSerialize(getSerialize());
	rcoR.setDataSocket(getDataSocket());
	rcoR.metricsBytes = metricsBytes;
	metricsBytes = 0;

	/*
	if (getClient() instanceof BuildInterface) {
	    BuildInterface bi = (BuildInterface) getClient();
	    rcoR.setBuildDate(bi.getBuildDate());
	    rcoR.setBuildVersion(bi.getBuildVersion());
	    //	rcoR.setLicenseString(bi.getLicenseString());
	} else {
	    BuildFile.loadBuildFile(rcoR);
	}
	*/
	rcoR.setBuildDate(getBuildDate());
	rcoR.setBuildVersion(getBuildVersion());

	return (rcoR);
    }

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
    final void createRCO()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.createRCO();

	java.net.Socket socket = (java.net.Socket) getClientSide();
	setSerialize
	    (new Language(new InputStream(socket.getInputStream(),
					  false,
					  32768),
			  null,
			  new OutputStream(socket.getOutputStream(),
					   false,
					   32768),
			  /*
			  new DebugOutputStream(socket.getOutputStream(),
						false,
						32768),
			  */
			  null));
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
    final void destroyRCO()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSerialize().close();
	metricsBytes += getSerialize().bytesTransferred();
	super.destroyRCO();
	setSerialize(null);
    }

    /**
     * Disonnects the data socket.
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
    final void disconnectData()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Address address = ((Server)
			   getClient().getParent()).getAddressHandler();
	address.disconnect(getDataSocket());
	setDataSocket(null);
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
     * 05/09/2001  INB	Created.
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
	java.net.Socket socket = (java.net.Socket) getClientSide();
	socket.setSoTimeout((int) timeOutI);
    }
}
