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
 * Extended <code>ACO</code> that communicates via serialization of objects.
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
 * 12/01/2004  MJM      Catch System.getProperty exception for applets to work
 * 08/04/2004  JPW      In receive(), add check on "Timeout" in exception
 *                      message (this is so the code will run under J#).
 * 12/09/2003  INB	If the last transmitted object is the same as the one
 *			being sent now, ignore it.
 * 05/23/2003  INB	Added timeout to <code>send</code> method.
 * 04/17/2003  INB	Added <code>setBuildDate</code> and
 *			<code>setBuildVersion</code> methods.
 * 03/27/2003  INB	Do not duplicate transmitted <code>Rmaps</code> except
 *			for <code>Controllers</code>.
 * 05/09/2001  INB	Created.
 *
 */
abstract class SerializingACO
    extends com.rbnb.api.ACO
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
	null;  // mjm cluge to try to get applets to run 12-1-04
	//	System.getProperty("rbnb.compressRmaps");

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

    SerializingACO() {

	super();

	try {
	    compressRmaps = System.getProperty("rbnb.compressRmaps");
	} catch (Exception e) { // ignore, use default mjm
	    System.err.println("Denied compressRmaps property, using default (applet?)");
	    compressRmaps = null;
	}
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
     * Class constructor to build an <code>SerializingACO</code> for a
     * <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>Client</code>.
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
    SerializingACO(Client clientI) {
	super(clientI);

	try {
	    compressRmaps = System.getProperty("rbnb.compressRmaps");
	} catch (Exception e) { // ignore, use default mjm
	    System.err.println("Denied compressRmaps property, using default (applet?)");
	    compressRmaps = null;
	}

    }

    /**
     * Gets the language serializing object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the language serializing object.
     * @see #setSerialize(com.rbnb.api.Language)
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
    final boolean isWaiting()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (getSerialize().available() > 0);
    }

    /**
     * Logs into the RBNB server.
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
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/16/2001  INB	Created.
     *
     */
    final void login()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.login();
	getSerialize().getOcontrol().setBinary(true);
	getSerialize().getIcontrol().setBinary(true);
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
     * @version 08/04/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/04/2004  JPW  Add check on "Timeout" in exception message (this is
     *                  so the code will run under J#)
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
// 01/05/2005 JPW and EMF
// In debugging RoutingPlugIn, EMF noticed that the PlugIn's call to Fetch(6000)
// would initially timeout appropriately, but after the PlugIn had passed
// through some data, Fetch(6000) would wait indefinitely.
// System.err.println("SerializingACO.receive called with timeout "+timeOutI);
	// Set the timeout to the specified duration.
	setTimeout(timeOutI);

	Serializable serializableR = null;

	try {
	    if ((timeOutI != 0) || isWaiting()) {
		if (getSerialize() == null) {
		    throw new java.io.EOFException("Connection lost.");
		}
		if (!isWaiting()) {
// System.err.println("setTimeout reset to "+timeOutI);
		    setTimeout(timeOutI);
		    if (timeOutI != Client.FOREVER) {
				InputStream is = getSerialize().getIcontrol();
				is.mark(100);
				is.read();
				is.reset();
		    }
		}
//System.err.println("SerializingACO.receive: setTimeout reset to 60000, sleeping for 10 seconds");
//try {Thread.currentThread().sleep(10000);}catch(Exception e){}
		//setTimeout(60000);
		setTimeout(Client.FOREVER);
		serializableR = getSerialize().read(lastReceived);
		getSerialize().readData(serializableR);
		if ((compressRmaps == null) ||
		    !compressRmaps.equalsIgnoreCase("OFF")) {
		    if (serializableR instanceof Rmap) {
			lastReceived = ((Rmap) serializableR).duplicate();
		    } else if ((serializableR instanceof RSVP) &&
			       (((RSVP) serializableR).getSerializable()
				instanceof Rmap)) {
			lastReceived =
			    ((Rmap)
			     ((RSVP)
			      serializableR).getSerializable
			     ()).duplicate();
		    }
		}
	    }

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
		throw e;
	    } else if (e.getMessage().indexOf("timed out") == -1) {
		throw e;
	    }
	}

	return (serializableR);
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
     * @since V2.0
     * @version 12/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/09/2003  INB	If the last transmitted object is the same as the one
     *			being sent now, ignore it.
     * 05/23/2003  INB	Added timeout.
     * 03/27/2003  INB	Do not duplicate transmitted <code>Rmaps</code> except
     *			for <code>Controllers</code>.
     * 05/08/2001  INB	Created.
     *
     */
    final void send(Serializable serializableI,long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    if ((timeOutI > 0) && (timeOutI != Client.FOREVER)) {
		setTimeout(timeOutI);
	    }
	    getSerialize().write
		(serializableI,
		 ((lastTransmitted == serializableI ?
		   null :
		   lastTransmitted)));
	    getSerialize().writeData(serializableI);
	    if ((compressRmaps == null) ||
		!compressRmaps.equalsIgnoreCase("OFF")) {
		if (serializableI instanceof Rmap) {
		    if ((getClient() instanceof ControllerInterface) ||
			(getClient() instanceof PlugInInterface)) {
			lastTransmitted = ((Rmap) serializableI).duplicate();
		    } else {
			lastTransmitted = (Rmap) serializableI;
		    }

		} else if ((serializableI instanceof RSVP) &&
			   (((RSVP) serializableI).getSerializable()
			    instanceof Rmap)) {
		    if ((getClient() instanceof ControllerInterface) ||
			(getClient() instanceof PlugInInterface)) {
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
	} finally {
	    setTimeout(Client.FOREVER);
	}
    }

    /**
     * Sets the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
     * @since V2.1
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	Created.
     *
     */
    public final void setBuildDate(java.util.Date buildDateI) {
	super.setBuildDate(buildDateI);
	getSerialize().getOcontrol().setBuildDate(getBuildDate());
    }

    /**
     * Sets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version.
     * @see #getBuildVersion()
     * @since V2.1
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	Created.
     *
     */
    public final void setBuildVersion(String buildVersionI) {
	super.setBuildVersion(buildVersionI);
	getSerialize().getOcontrol().setBuildVersion(getBuildVersion());
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
