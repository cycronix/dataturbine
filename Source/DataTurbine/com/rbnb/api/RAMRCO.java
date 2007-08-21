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
 * Extended <code>RCO</code> that communicates via RAM.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/14/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 03/21/2003  INB	Log messages in <code>send/receive</code>.
 * 02/19/2003  INB	Added <code>stopMe</code> method.
 * 05/10/2001  INB	Created.
 *
 */
final class RAMRCO
    extends com.rbnb.api.RCO
{

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
    RAMRCO() {
	super();
    }

    /**
     * Class constructor to build an <code>RAMRCO</code> for a connection to a
     * <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param connectionI the connection object.
     * @param rbnbI	  the <code>ServerHandler</code>.
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
    RAMRCO(Object connectionI,ServerHandler rbnbI) {
	super(connectionI,rbnbI);
    }

    /**
     * Assigns the connection represented by the input <code>RCO</code> to this
     * <code>RCO</code> as a data line.
     * <p>
     * This method is a NOP because data lines do not currently exist with RAM
     * connections.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code> representing the connection.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is already a data connection.
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    final void assignConnection(RCO rcoI) {
	return;
    }

    /**
     * Closes this <code>RCO</code>.
     * <p>
     * The close operation performs any steps necessary to shutdown anything
     * added to the connection by the <code>RCO</code> in order to talk to the
     * <code>ACO</code>. It does NOT close the connection itself.
     * <p>
     * For <code>RAMACOs</code>, this method is a NOP.
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
     * @see #disconnect()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    final void close()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return;
    }

    /**
     * Converts this <code>ACO</code> to an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @return the <code>RCO</code>.
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
    final ACO convertToACO(Client clientI) {
	RAMCommunications rc = (RAMCommunications) getServerSide();

	ACO acoR = new RAMACO(clientI);
	acoR.setClientSide(rc.getOtherSide());

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
     * @version 05/14/2001
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
	RAMCommunications ss = (RAMCommunications) getServerSide();
	ss.setOtherSide(null);
	setServerSide(null);
    }

    /**
     * Is anything waiting to be read?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is anything waiting to be read?
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final boolean isWaiting()
	throws java.lang.InterruptedException
    {
	RAMCommunications rc = (RAMCommunications) getServerSide();
	return (rc.isWaiting());
    }

    /**
     * Receives a message from the <code>ACO</code> in the
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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 03/21/2003  INB	Log reception.
     * 05/08/2001  INB	Created.
     *
     */
    final Serializable receive(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getReadDoor().setIdentification(this + "_read");
	Serializable serializableR = null;
	try {
	    getReadDoor().lock("RAMRCO.read");
	    RAMCommunications rc = (RAMCommunications) getServerSide();
	    serializableR = rc.read(timeOutI);
	} finally {
	    getReadDoor().unlock();
	}

	/*
	if ((getLog() != null) && (serializableR != null)) {
	    getLog().addMessage
		(getLogLevel() + 10,
		 getLogClass(),
		 toString(),
		 "RCO received: " + serializableR);
	}
	*/
	return (serializableR);
    }

    /**
     * Sends a message to the <code>ACO</code>.
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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 03/21/2003  INB	Log transmission.
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
	getWriteDoor().setIdentification(this + "_write");
	try {
	    getWriteDoor().lock("RAMRCO.send");
	    RAMCommunications rc = (RAMCommunications) getServerSide();
	    rc.getOtherSide().write(serializableI);
	} finally {
	    getWriteDoor().unlock();
	}
    }

    /**
     * Performs special stop code for <code>RAMRCOs</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Created.
     *
     */
    final void stopMe() {

	RAMCommunications rc = (RAMCommunications) getServerSide();
	if (rc != null) {
	    rc.abortRead();
	}
    }
}
