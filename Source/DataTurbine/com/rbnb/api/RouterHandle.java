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
 * Client-side object that represents a routing connection to another
 * <bold>RBNB</bold> server.
 * <p>
 * The "client" in this case is actually an <bold>RBNB</bold>.  Documentation
 * for routing can be found in <code>RemoteServer</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RBNBRouter
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 08/10/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/10/2004  EMF	Removed synchronization from stop method.
 * 08/05/2004  INB	Updated documentation and added link to RemoteServer
 *			and RBNBRouter.
 * 05/27/2004  INB	Added code to attempt to recover after receiving an
 *			out-of-order ping or some other unexpected message.
 * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 05/23/2003  INB	To avoid blocking on a <code>RouterHandle</code>
 *			that is being stopped, added <code>RouterStopper</code>
 *			class and handling. Added timeout when sending a
 *			login/stop to the remote.
 * 04/01/2003  INB	Don't <code>Ping</code> if the connection is down.
 * 03/31/2003  INB	On transmission or <code>Ping</code> errors, mark
 *			routing as lost rather than simply destroying the
 *			specific <code>Router</code>.
 * 02/27/2003  INB	Time out waiting for responses.
 * 02/26/2003  INB	The data in the <code>Ping</code> should monotonically
 *			increase. Added some logging.
 * 02/25/2003  INB	Use a <code>Ping</code> with data.
 * 11/21/2001  INB	Created.
 *
 */
final class RouterHandle
    extends com.rbnb.api.ControllerHandle
    implements com.rbnb.api.BuildInterface,
	       com.rbnb.api.GetLogInterface,
	       com.rbnb.api.Router
{
    /**
     * the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private java.util.Date buildDate = null;

    /**
     * the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private String buildVersion = null;

    /**
     * last write time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private long lastWriteTime = Long.MIN_VALUE;

    /**
     * the license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    //    private String licenseString = null;

    /**
     * the ping timer task.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/05/2001
     */
    private TimerTask pingTask = null;

    /**
     * the next <code>Ping</code> data value.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */
    private short pingValue = -1;

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
    RouterHandle() {
	super();
    }

    /**
     * Class constructor to build a <code>RouterHandle</code> by reading it
     * in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    RouterHandle(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Class constructor to build a <code>RouterHandle</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    RouterHandle(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
    }

    /**
     * Deliver a <code>RoutedMessage</code> to its destination.
     * <p>
     * The destination must be reached by sending the message via this routing
     * connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @return the response message, if any.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @since V2.0
     * @version 02/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Expect a response within the <code>PING_WAIT</code>
     *			period.
     * 02/27/2003  INB	Destroy the <code>Router</code> if we don't get a
     *			response in a reasonable amount of time.
     * 11/21/2001  INB	Created.
     *
     */
    public final Serializable deliver(RoutedMessage messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	send(messageI);
	Serializable serializableR = receive(TimerPeriod.PING_WAIT);

	if (serializableR == null) {
	    ((ConnectedServer) getParent()).destroyRouter(this);
	    serializableR = new ExceptionMessage
		(new com.rbnb.api.SerializeException
		    ("Timed out waiting for response to " + messageI));
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final String getBuildVersion() {
	return (buildVersion);
    }

    /**
     * Gets the last write time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time in millis.
     * @see #setLastWriteTime(long)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    private final long getLastWriteTime() {
	return (lastWriteTime);
    }

    /**
     * Gets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the license string.
     * @see #setLicenseString(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    /*
    public final String getLicenseString() {
	return (licenseString);
    }
    */

    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
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
    public final Log getLog() {
	return (((GetLogInterface) getParent()).getLog());
    }

    /**
     * Gets the log class mask for this <code>RouterHandle</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final long getLogClass() {
	return (Log.CLASS_ROUTER_HANDLE);
    }

    /**
     * Gets the base log level for this <code>RouterHandle</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final byte getLogLevel() {
	return (Log.STANDARD + 1);
    }

    /**
     * Gets the ping <code>TimerTask</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the ping <code>TimerTask</code>.
     * @see #setPingTask(com.rbnb.api.TimerTask)
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    private final TimerTask getPingTask() {
	return (pingTask);
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
     * @version 12/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/27/2001  INB	Created.
     *
     */
    public final Serializable receive(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (receive(null,false,timeOutI));
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
     * @param classI the expected response class.
     * @param ignoreI ignore unexpected responses?
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
     * @version 12/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/27/2001  INB	Created.
     *
     */
    public final Serializable receive(Class classI,
				      boolean ignoreI,
				      long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializableR =
	    getACO().receive(classI,ignoreI,false,timeOutI);

	/*
System.err.println(getFullName() + " received " + serializableR);
	*/

	return (serializableR);
    }

    /**
     * Sends a regular message to the <code>RBNBRouter</code> on the other
     * side.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>Serializable</code> message to send.
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
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 05/23/2003  INB	Added timeout when sending a login/stop to the remote.
     * 03/31/2003  INB	Mark routing as lost if an error occurs.
     * 03/26/2003  INB	Add exceptions at log level 0.
     * 11/27/2001  INB	Created.
     *
     */
    public final void send(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {

	/*
System.err.println(getFullName() + " sending " + serializableI);
	*/

	try {
	    /*
	    if (getLog() != null) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Sending:\n" + serializableI);
	    }
	    */

	    setLastWriteTime(System.currentTimeMillis());

	    long timeOut = Client.FOREVER;
	    if (serializableI instanceof RoutedMessage) {
		RoutedMessage rMessage = (RoutedMessage) serializableI;
		if (rMessage.getMessage() instanceof Login) {
		    timeOut = TimerPeriod.STARTUP_WAIT;
		} else if (rMessage.getMessage() instanceof Stop) {
		    timeOut = TimerPeriod.SHUTDOWN_ROUTER;
		}
	    }
	    getACO().send(serializableI,timeOut);

	} catch (com.rbnb.api.AddressException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}
	    ((ConnectedServer) getParent()).lostRouting();
	    throw e;
	} catch (java.io.EOFException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}
	    ((ConnectedServer) getParent()).lostRouting();
	    throw e;
	} catch (java.io.IOException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}
	    ((ConnectedServer) getParent()).lostRouting();
	    throw e;
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
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final void setBuildDate(java.util.Date buildDateI) {
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final void setBuildVersion(String buildVersionI) {
	buildVersion = buildVersionI;
    }

    /**
     * Sets the last write time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lastWriteTimeI the time in millis.
     * @see #getLastWriteTime()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    private final void setLastWriteTime(long lastWriteTimeI) {
	lastWriteTime = lastWriteTimeI;
    }

    /**
     * Sets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param licenseStringI the license string.
     * @see #getLicenseString()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    /*
    public final void setLicenseString(String licenseStringI) {
	licenseString = licenseStringI;
    }
    */

    /**
     * Sets the ping <code>TimerTask</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pingTaskI the ping <code>TimerTask</code>.
     * @see #getPingTask()
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    private final void setPingTask(TimerTask pingTaskI) {
	pingTask = pingTaskI;
    }

    /**
     * Starts the <code>RouterHandle</code>.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>RouterHandle</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>RouterHandle</code> is already running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #stop()
     * @since V2.0
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Added logging.
     * 12/05/2000  INB	Created.
     *
     */
    public final synchronized void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.start();

	/*
	try {
	    getLog().addMessage(getLogLevel(),
				getLogClass(),
				getName(),
				"Starting up router handle to " +
				getParent().getFullName());
	} catch (java.lang.Exception e) {
	}
	*/

	setPingTask(new TimerTask(this,TT_PING));
	((GetServerHandlerInterface) getParent()).getLocalServerHandler
	    ().getTimer().schedule
	    (getPingTask(),
	     TimerPeriod.PING,
	     TimerPeriod.PING);
    }

    /**
     * Stops the <code>RouterHandle</code>.
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
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>RouterHandle</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>RouterHandle</code> is already running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #start()
     * @since V2.0
     * @version 08/10/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/10/2004  EMF	Remove synchronization to allow a connected server
     *			to complete its shutdown process even if this router
     *			ends up in a bad state.
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 05/23/2003  INB	Perform the actual shutdown in a separate thread.
     * 02/26/2003  INB	Added logging.
     * 12/05/2001  INB	Created.
     *
     */
    public final void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getPingTask() != null) {
	    try {
		getPingTask().cancel();
	    } catch (java.lang.Exception e) {
	    }
	    setPingTask(null);
	}

	/*
	try {
	    getLog().addMessage(getLogLevel(),
				getLogClass(),
				getName(),
				"Shutting down router handle to " +
				getParent().getFullName());
	} catch (java.lang.Exception e) {
	}
	*/

	RouterStopper rStop = new RouterStopper();
	rStop.setDaemon(true);
	rStop.start();
    }

    /**
     * Stops this <code>RouterHandle</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Created.
     *
     */
    private final void superStop() {
	try {
	    super.stop();
	} catch (java.lang.Throwable e) {
	}
    }

    /**
     * Executes a task on a timer.
     * <p>
     * This method executes one of a number of possible tasks based on the code
     * specified in the input <code>TimerTask</code>. The tasks are:
     * <p><ul>
     * <li><code>TT_PING</code> - pings the remote side to ensure that it is
     *     still there to receive messages. If not, the
     *	   <code>RouterHandle</code> shuts down.<li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param ttI the <code>TimerTask</code>.
     * @since V2.0
     * @version 05/27/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/27/2004  INB	Added code to attempt to recover after receiving an
     *			out-of-order ping or some other unexpected message.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 04/01/2003  INB	Don't <code>Ping</code> if the connection is down.
     * 03/31/2003  INB	On a <code>Ping</code> failure, don't simply destroy
     *			the <code>Router</code> - mark the connection as lost.
     * 02/25/2003  INB	Use a <code>Ping</code> with data.
     * 12/05/2001  INB	Created.
     *
     */
    public final void timerTask(TimerTask ttI) {
	try {
	    if (((ConnectedServer) getParent()).getConnected() &&
		(ttI.getCode().equalsIgnoreCase(TT_PING)) &&
		(System.currentTimeMillis() - TimerPeriod.PING/2 >=
		 getLastWriteTime())) {
		if (((ConnectedServer) getParent()).grabRouter(this) ==
		    this) {
		    try {
			send(new Ping(true,pingValue));
			Serializable serializable;
			Ping ping = null;

			do {
			    serializable = receive
				(null,
				 false,
				 TimerPeriod.PING_WAIT);

			    if (serializable == null) {
				throw new com.rbnb.api.SerializeException
				    ("Timed out waiting for ping.");
			    } else if
				(serializable instanceof ExceptionMessage) {
				Language.throwException
				    ((ExceptionMessage) serializable);
			    } else if (!(serializable instanceof Ping)) {
				if (getLog() != null) {
				    getLog().addMessage
					(Log.STANDARD,
					 getLogClass(),
					 getParent().getFullName(),
					 "Received out-of-order message. " +
					 "Expected Ping " + pingValue +
					 ", got: " + serializable);
				}
			    } else {
				ping = (Ping) serializable;
				if (!ping.getHasData() ||
				    (ping.getData() != pingValue)) {
				    getLog().addMessage
					(Log.STANDARD,
					 getLogClass(),
					 getParent().getFullName(),
					 "Received out-of-order ping. " +
					 "Expected " +
					 pingValue + ", got " +
					 (ping.getHasData() ?
					  Short.toString(ping.getData()) :
					  "no counter"));
				    ping = null;
				}
			    }
			} while (ping == null);

			++pingValue;
			((ConnectedServer) getParent()).releaseRouter(this);

		    } catch (java.lang.Exception e) {
			if (((ConnectedServer) getParent()).getConnected()) {
			    try {
				((GetServerHandlerInterface)
				 getParent()).getLocalServerHandler().getLog
				    ().addException(Log.STANDARD,
						    getLogClass(),
						    getParent().getFullName(),
						    e);
			    } catch (java.lang.Exception e1) {}
			    ((ConnectedServer) getParent()).lostRouting();
			}
		    }
		}
	    }

	} catch (java.lang.Exception e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getParent().getFullName(),
		     e);
	    } catch (java.lang.Exception e1) {
	    }
	}
    }

    /**
     * Internal class for stopping <code>RouterHandles</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 11/14/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 05/23/2003  INB	Created.
     *
     */
    private final class RouterStopper
	extends com.rbnb.api.ThreadWithLocks
    {

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 05/23/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/23/2003  INB	Created.
	 *
	 */
	RouterStopper() {
	    super();
	}

	/**
	 * Runs this <code>RouterStopper</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 11/14/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
	 *			<code>Thread</code> and ensure that
	 *			<code>Locks</code> are released.
	 * 05/23/2003  INB	Created.
	 *
	 */
	public final void run() {
	    // Run the <code>stop</code> method of the parent class of
	    // <code>RouterHandle</code>.
	    superStop();

	    clearLocks();
	}
    }
}

