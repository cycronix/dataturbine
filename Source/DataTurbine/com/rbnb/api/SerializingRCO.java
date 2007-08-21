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
 * Extended <code>RCO</code> that communicates via serialization of objects.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/04/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/04/2004  JPW      Changes in exception handling in receive():
 *                      add check on null message; also check if Interrupted-
 *                      IOException message contains "Timeout" - this is to
 *                      support running under J#.
 * 07/21/2004  INB	If the serialize object is null in receive, then
 *			simply return null.
 * 02/09/2004  INB	Use STARTUP_WAIT when waiting for the data connection.
 * 01/23/2004  INB	Added code to the login method to ensure that it can
 *			break out of the wait loop.
 * 11/14/2003  INB	Added location to the <code>Lock</code>.
 * 03/27/2003  INB	Do not duplicate transmitted information except for
 *			Controllers.
 * 03/26/2003  INB	Use TimerPeriod.LONG_WAIT.
 * 03/21/2003  INB	Log messages in send and receive.
 * 03/20/2003  INB	Changed to a wait with a timeout.
 * 05/09/2001  INB	Created.
 *
 */
abstract class SerializingRCO
    extends com.rbnb.api.RCO
{
    /**
     * the last <code>Rmap</code> received.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 07/31/2001
     */
    private Rmap lastReceived = null;

    /**
     * the last <code>Rmap</code> transmitted.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 07/31/2001
     */
    private Rmap lastTransmitted = null;

    /**
     * the language serializing object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private Language serialize = null;

    // Private class variables:
    private static String compressRmaps =
	System.getProperty("rbnb.compressRmaps");

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
    SerializingRCO() {
	super();
    }

    /**
     * Class constructor to build an <code>SerializingRCO</code> for a
     * connection to a <code>ServerHandler</code>.
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
    SerializingRCO(Object connectionI,ServerHandler rbnbI) {
	super(connectionI,rbnbI);
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
    public final long bytesTransferred() {
	long bytesR = super.bytesTransferred();

	if (getSerialize() != null) {
	    bytesR += getSerialize().bytesTransferred();
	}

	return (bytesR);
    }

    /**
     * Closes this <code>RCO</code>.
     * <p>
     * The close operation performs any steps necessary to shutdown anything
     * added to the connection by the <code>RCO</code> in order to talk to the
     * <code>ACO</code>. It does NOT close the connection itself.
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
     * @version 11/19/2002
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
	if (getSerialize() != null) {
	    getSerialize().close();
	    metricsBytes += getSerialize().bytesTransferred();
	    setSerialize(null);
	}
    }

    /**
     * Gets the <code>Language</code> serializing object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Language</code> object.
     * @see #setSerialize(com.rbnb.api.Language)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    final Language getSerialize() {
	return (serialize);
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
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2002  INB	Created.
     *
     */
    final boolean isSupported(int featureI) {
	boolean isSupportedR = true;

	OutputStream os;
	if ((getSerialize() != null) &&
	    ((os = getSerialize().getOcontrol()) != null)) {
	    isSupportedR = IsSupported.isSupported(featureI,
						   os.getBuildVersion(),
						   os.getBuildDate());
	}

	return (isSupportedR);
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
     * @version 06/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    final synchronized boolean isWaiting()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return ((getSerialize() != null) && (getSerialize().available() > 0));
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
     * @version 02/09/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2004  INB	Use <code>STARTUP_WAIT</code>.
     * 01/23/2004  INB	Ensure that there are ways that the wait loop can
     *			terminate.  Quit on a terminate request or if too
     *			much time goes by.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 05/08/2001  INB	Created.
     *
     */
    synchronized void login(Login loginI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.login(loginI);
	getSerialize().getOcontrol().setBinary(true);
	getSerialize().getOcontrol().setBuildDate(loginI.getBuildDate());
	getSerialize().getOcontrol().setBuildVersion(loginI.getBuildVersion());
	getSerialize().getIcontrol().setBinary(true);

	long startAt = System.currentTimeMillis();
	long nowAt = 0L;
	if (System.getProperty("SHORTCONNECTIONS") != null) {
	    while ((getSerialize().getIdata() == null) &&
		   ((nowAt = System.currentTimeMillis()) - startAt < 100) &&
		   !getTerminateRequested()) {
		wait(10);
	    }
	} else {	
	    while ((getSerialize().getIdata() == null) &&
		   ((nowAt = System.currentTimeMillis()) - startAt <
		    TimerPeriod.STARTUP_WAIT) &&
		   !getTerminateRequested()) {
		wait(TimerPeriod.NORMAL_WAIT);
	    }
	}

	if (!getTerminateRequested() &&
	    (getSerialize().getIdata() == null)) {
	    throw new java.io.IOException
		(this + " failed to get expected data connection.");
	}
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
     * @since V2.0
     * @version 08/04/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/04/2004  JPW  In exception handling: add check on null message;
     *                  also check if InterruptedIOException message contains
     *                  "Timeout" - this is to support running under J#.
     * 07/21/2004  INB	Return null on null serialize connection (which
     *			probably means the connection got closed).
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
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
	Serializable serializableR = null;

	try {
	    getReadDoor().lock("SerializingRCO.receive");

	    try {
		if ((timeOutI != 0) || isWaiting()) {
		    if (!isWaiting()) {
			setTimeout(timeOutI);
			if (timeOutI != Client.FOREVER) {
			    InputStream is = getSerialize().getIcontrol();
			    is.mark(100);
			    is.read();
			    is.reset();
			}
		    }
		    setTimeout(Client.FOREVER);
		    if (getSerialize() == null) {
			return (null);
		    }
		    serializableR = getSerialize().read(lastReceived);
		    getSerialize().readData(serializableR);
		    if ((compressRmaps == null) ||
			!compressRmaps.equalsIgnoreCase("OFF")) {
			if (serializableR instanceof Rmap) {
			    lastReceived =
				((Rmap) serializableR).duplicate();
			} else if ((serializableR instanceof RSVP) &&
				   (((RSVP) serializableR).getSerializable()
				    instanceof Rmap)) {
			    lastReceived =
				((Rmap)
				 ((RSVP) serializableR).getSerializable
				 ()).duplicate();
			}
		    }
		}

	    } catch (java.io.InterruptedIOException e) {
		// JPW 08/04/2004: Add check for null message and add check
		//                 for "Timeout" string (changes made to
		//                 allow code to run under J#)
		if ( (e.getMessage() != null) &&
		     (e.getMessage().indexOf("timed out") == -1) &&
		     (e.getMessage().indexOf("Timeout") == -1) )
		{
		    throw e;
		}

	    } catch (java.net.SocketException e) {
		// JPW 08/04/2004: Just as in SerializingACO, throw exception
		//                 if message is null
		if (e.getMessage() == null) {
		    throw e;
		}
		else if (e.getMessage().indexOf("timed out") == -1) {
		    throw e;
		}
	    } 

	    if (Thread.currentThread().interrupted()) {
		throw new java.io.InterruptedIOException();
	    }

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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 03/27/2003  INB	No need to duplicate the transmitted <code>Rmaps</code>
     *			except if they are coming from a
     *			<code>Controller</code>.
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
	try {
	    /*
	    if (getLog() != null) {
		getLog().addMessage
		    (getLogLevel() + 10,
		     getLogClass(),
		     toString(),
		     "RCO sending: " + serializableI);
	    }
	    */

	    getWriteDoor().lock("SerializingRCO.send");
	    if (getSerialize() != null) {
		getSerialize().write(serializableI,lastTransmitted);
		if (getSerialize() != null) {
		    getSerialize().writeData(serializableI);
		}
		if ((compressRmaps == null) ||
		    !compressRmaps.equalsIgnoreCase("OFF")) {
		    if (serializableI instanceof Rmap) {
			if (getClientHandler() instanceof
			    ControllerInterface) {
			    lastTransmitted =
				((Rmap) serializableI).duplicate();
			} else {
			    lastTransmitted = (Rmap) serializableI;
			}
		    } else if ((serializableI instanceof RSVP) &&
			       (((RSVP) serializableI).getSerializable()
				instanceof Rmap)) {
			if (getClientHandler() instanceof
			    ControllerInterface) {
			    lastTransmitted =
				((Rmap) ((RSVP)
					 serializableI).getSerializable
				 ()).duplicate();
			} else {
			    lastTransmitted =
				(Rmap) ((RSVP)
					serializableI).getSerializable();
			}
		    }
		}
	    }
	} finally {
	    getWriteDoor().unlock();
	}
    }

    /**
     * Sets the language serializing object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializeI  the language serializing object.
     * @see #getSerialize()
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
    final void setSerialize(Language serializeI) {
	serialize = serializeI;
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
    abstract void setTimeout(long timeOutI)
	throws java.io.IOException;
}
