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
 * Server-side object that represents a client application connection to an
 * RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 02/09/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/12/2005  MJM	Added yield in stop() method so Detach lets Flush
 *			finish before stopping
 * 02/09/2004  INB	Ensure that by the time the <code>stop</code> method
 *			exits, our parent no longer knows about us.
 * 01/16/2004  INB	Added in the current local metrics bytes to the
 *			<code>RCO's</code> in case more than one has been
 *			attached to this <code>RBNBClient</code>.
 * 01/14/2004  INB	Added <code>metricsSyncObj</code> and handling.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code>.
 * 11/12/2003  INB	Added is in startup handling.
 * 09/08/2003  INB	Eliminated a redudant notify in
 *			<code>doShutdown</code>.
 * 04/17/2003  INB	Added <code>reconnect</code>,
 *			</code>allowAccess(Username)</code>, and
 *			<code>allowReconnect</code> methods. Allow
 *			<code>start</code> to be called even if the
 *			<code>RBNBClient</code> is already running.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/14/2003  INB	The <code>stop</code> method is a NOP if the handler is
 *			already stopping.
 * 02/18/2003  INB	The methods <code>getTerminateRequested</code> and
 *			<code>setTerminateRequested</code> are now part of an
 *			interface and are public. 
 * 05/14/2001  INB	Created.
 *
 */
abstract class RBNBClient
    extends com.rbnb.api.ClientIO
    implements com.rbnb.api.ClientHandler
{
    /**
     * is this <code>RBNBClient</code> new?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/23/2001
     */
    private boolean amNew = true;

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

    /**
     * metrics: synchronization object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/14/2004
     */
	// 2005/08/25  WHF  Made this object an unnamed inner class which implements
	//  serializable but otherwise has no additional methods.
    final Object metricsSyncObj // = new Object();
							 = new java.io.Serializable() {};
    /**
     * the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/11/2002
     */
    private transient RCO rco = null;

    /**
     * is this <code>RBNBClient</code> starting up?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/12/2003
     */
    private boolean isInStartup = true;

    /**
     * stop this <code>RBNBClient</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private boolean terminateRequested = false;

    /**
     * the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/11/2002
     */
    private transient Thread thread = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    RBNBClient() {
	super();
    }

    /**
     * Class constructor to build an <code>RBNBClient</code> for an
     * <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code>.
     * @since V2.0
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    RBNBClient(RCO rcoI) {
	this();
	setRCO(rcoI);
	rcoI.setClientHandler(this);
    }

    /**
     * Allow the specified <code>ClientHandler</code> access to this one?
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>ClientHandler</code>.
     * @return is the other <code>ClientHandler</code> allowed access?
     * @since V2.0
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	Rewritten using <code>allowAccess(Username)</code>.
     * 01/15/2003  INB	Created.
     *
     */
    public final boolean allowAccess(ClientHandler otherI) {
	return (allowAccess(otherI.getUsername()));
    }

    /**
     * Allow the specified <code>Username</code> access to this
     * <code>ClientHandler</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @return is the <code>Username</code> allowed access?
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
    public final boolean allowAccess(Username usernameI) {
	boolean allowAccessR = true;

	if (getUsername() != null) {
	    allowAccessR = getUsername().allowAccess(usernameI);
	}

	return (allowAccessR);
    }

    /**
     * Allow reconnects to happen?
     * <p>
     * Generally, reconnects are not allowed. Override this method if your
     * <code>ClientHandler</code> wishes to allow reconnects.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code> trying to reconnect.
     * @return are reconnects allowed?
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
    public boolean allowReconnect(Username usernameI) {
	return (false);
    }

    /**
     * Assigns a new connection to this <code>RBNBClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI             the <code>RCO</code> that was handling the
     *			       connection.
     * @param clientInterfaceI the <code>ClientInterface</code> object
     *			       identifying the connection.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
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
    public final void assignConnection(RCO rcoI,
				       ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getRCO().assignConnection(rcoI);
	update(clientInterfaceI);
    }

    /**
     * Calculates the total number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes transferred.
     * @since V2.0
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Added in the current local metrics bytes to the
     *			<code>RCO's</code> in case more than one has been
     *			attached to this <code>RBNBClient</code>.
     * 01/14/2004  INB	Added handling of <code>metricsSyncObj</code>.
     * 11/19/2002  INB	Created.
     *
     */
    public final long bytesTransferred() {
	long bytesR = 0;

	if (getRCO() != null) {
	    bytesR = metricsBytes + getRCO().bytesTransferred();
	} else {
	    synchronized (metricsSyncObj) {
		bytesR = metricsBytes;
	    }
	}

	return (bytesR);
    }

    /**
     * Handles an <code>ExceptionMessage</code> received from the client.
     * <p>
     *
     * @author Ian Brown
     *
     * @param emsgI the <code>ExceptionMessage</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 04/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/23/2002  INB	Created.
     *
     */
    public void exception(ExceptionMessage emsgI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// If a subclass can handle this message, it should overload this
	// method.
	Language.throwException(emsgI);
    }

    /**
     * Gets the is in startup flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>RBNBClient</code> starting up?
     * @see #setIsInStartup(boolean)
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    public final boolean getIsInStartup() {
	return (isInStartup);
    }

    /**
     * Gets this <code>ServerHandler</code> for this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
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
    public final ServerHandler getLocalServerHandler() {
	ServerHandler sHandlerR = null;

	if ((getParent() != null) &&
	    (getParent() instanceof GetServerHandlerInterface)) {
	    sHandlerR =
		((GetServerHandlerInterface)
		 getParent()).getLocalServerHandler();
	}

	return (sHandlerR);
    }

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
	Log logR = null;

	if (getParent() instanceof GetLogInterface) {
	    logR = ((GetLogInterface) getParent()).getLog();
	}

	return (logR);
    }

    /**
     * Gets the log class mask for this <code>RBNBClient</code>.
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
    public long getLogClass() {
	return (Log.CLASS_RBNB_CLIENT);
    }

    /**
     * Gets the base log level for this <code>RBNBClient</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.0
     * @version 12/12/2002
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
	return ((getName() == null) ? Log.STANDARD :
		(getName().charAt(0) == '_') ? Log.STANDARD + 100 :
		Log.STANDARD);
    }

    /**
     * Gets the <code>RCO</code> for this <code>RBNBClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RCO</code>.
     * @see #setRCO(com.rbnb.api.RCO)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2001  INB	Created.
     *
     */
    public final RCO getRCO() {
	return (rco);
    }

    /**
     * Gets the registration list for this <code>RBNBClient</code> matching the
     * input hierarchy.
     * <p>
     * At the moment, the only valid input is an <code>Rmap</code> with the
     * same name as this <code>RBO</code> with an optional child named "...".
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return the matching registration information.
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
     * @version 05/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (compareNames(requestI) != 0) {
	    throw new java.lang.IllegalArgumentException
		(requestI + " does not have a matching name.");
	}

	ClientInterface cI = null;

	if ((this instanceof SinkInterface) ||
	    (this instanceof SourceInterface) ||
	    (this instanceof ControllerInterface) ||
	    (this instanceof PlugInInterface)) {
	    cI = (ClientInterface) newInstance();
	} else {
	    throw new java.lang.IllegalStateException
		("Cannot get the registration for a " +
		 this);
	}
	cI.setName(getName());
	cI.setType(getType());
	cI.setRemoteID(getRemoteID());

	return ((Rmap) cI);
    }

    /**
     * Gets the role(s) needed to access this <code>RBNBClient</code>.
     * <p>
     * To access this <code>RBNBClient</code>, the accessing
     * <code>ClientHandler</code> must have one or more of the roles listed.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the roles required.
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public final String[] getRoles() {
	return ((getUsername() == null) ?
		null :
		getUsername().getRoles());
    }

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return has a stop been requested?
     * @see #setTerminateRequested(boolean)
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Made public.
     * 01/12/2001  INB	Created.
     *
     */
    public final boolean getTerminateRequested() {
	return (terminateRequested);
    }

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
     * @see #setThread(java.lang.Thread)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    public final Thread getThread() {
	return (thread);
    }

    /**
     * Interrupts this <code>RBNBClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.SecurityException
     *		  thrown if the interrupt is not allowed by the security
     *		  system.
     * @see java.lang.Thread#interrupt()
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2001  INB	Created.
     *
     */
    public final void interrupt() {
	if (getThread() != null) {
	    getThread().interrupt();
	}
    }

    /**
     * Is this <code>RBNBClient</code> new?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>RBNBClient</code> new?
     * @since V2.0
     * @version 10/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    public final boolean isNew() {
	return (amNew);
    }

    /**
     * Is this <code>RBNBClient</code> running?
     * <p>
     * This method just checks to see if either the <code>RCO</code> or the
     * data thread is running.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>RBNBClient</code> running?
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Fixed documentation.
     * 01/02/2001  INB	Created.
     *
     */
    public final boolean isRunning() {
	try {
	    boolean isRunningR =
		((getRCO() != null) && getRCO().getThread().isAlive()) ||
		((getThread() != null) && getThread().isAlive());

	    return (isRunningR);
	} catch (java.lang.Exception e) {
	    return (false);
	}
    }

    /**
     * Logs the status of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param prefixI the prefix string for the message.
     * @since V2.0
     * @version 01/16/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    public void logStatus(String prefixI) {
	if (getName().charAt(0) != '_') {
	    try {
		getLog().addMessage(getLogLevel(),
				    getLogClass(),
				    getName(),
				    prefixI + ".");
	    } catch (java.lang.Exception e) {
	    }
	}
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy in response to a
     * request <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI    the <code>RmapExtractor</code> performing the
     *			    extraction.
     * @param unsatisfiedI  the unsatisfied <code>ExtractedChain</code>.
     * @param unsatsifiedO  the new list of unsatisfied
     *			    <code>ExtractedChains</code>.
     * @param the reason for a failed match.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to the
     *		  source <code>Rmap</code> hierarchy or in extracting the
     *		  desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/25/2001  INB	Created.
     *
     */
    byte moveDownFrom(RmapExtractor extractorI,
		      ExtractedChain unsatisfiedI,
		      java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (super.moveDownFrom
		(extractorI,
		 unsatisfiedI,
		 new java.util.Vector()));
    }

    /**
     * Reconnect to an existing <code>ClientHandler</code>.
     * <p>
     * Generally, reconnects are not possible. Override this method for
     * handlers that allow it.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the new <code>ClientInterface</code>.
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
     *		  thrown if the <code>ClientInterface</code> is not a
     *		  <code>RBO</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
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
    public void reconnect(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    ("Cannot reconnect to client handler " + getFullName() +
	     ". Reconnects are not allowed for this type of client.");
    }

    /**
     * Sets the "am new" flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param amNewI is this <code>RBNBClient</code> new?
     * @see #isNew()
     * @since V2.0
     * @version 06/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    public final void setAmNew(boolean amNewI) {
	amNew = amNewI;
    }

    /**
     * Sets the is in startup flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isInStartupI is this <code>RBNBClient</code> starting up?
     * @see #getIsInStartup()
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    public final void setIsInStartup(boolean isInStartupI) {
	isInStartup = isInStartupI;
    }

    /**
     * Sets the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI  the <code>RCO</code>.
     * @see #getRCO()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2001  INB	Created.
     *
     */
    public final void setRCO(RCO rcoI) {
	rco = rcoI;
    }

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stopI  stop this <code>RBNBClient</code>?
     * @see #getTerminateRequested()
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Made public. And removed interrupt.
     * 01/12/2001  INB	Created.
     *
     */
    public final synchronized void setTerminateRequested(boolean stopI) {
	terminateRequested = stopI;
	notifyAll();
    }

    /**
     * Sets the data thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI  the data thread.
     * @see #getThread()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    final void setThread(Thread threadI) {
	thread = threadI;
    }

    /**
     * Starts the <code>RBNBClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> starting us.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #isRunning()
     * @see #run()
     * @see #stop(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 04/17/2003  INB	Allow restarts (treat as NOP).
     * 01/02/2001  INB	Created.
     *
     */
    public final void start(ClientHandler clientI)
	throws java.lang.InterruptedException
    {
	synchronized (this) {
	    if ((getThread() != null) && getThread().isAlive()) {
		return;
	    }
	    setThread(new ThreadWithLocks(this,getName()));
	    getThread().start();
	    wait();
	    if (getThread() == null) {
		throw new java.lang.IllegalStateException
		    (getName() + " closed down.");
	    }
	}
    }

    /**
     * Stops the <code>RBNBClient</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> stopping us.
     * @exception java.lang.InterruptedException
     *		  thrown if the stop is interrupted.
     * @see #run()
     * @see #start(com.rbnb.api.ClientHandler)
     * @since V2.0
     * @version 02/09/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/12/2005  MJM  Added yield so Detach lets Flush cleans up before
     *                  being terminated. It would be better to make Flush
     *                  more robust but this is expedient
     * 02/09/2004  INB	Ensure that by the time this method exits, our parent
     *			no longer knows about us.
     * 09/08/2003  INB	Eliminated redundant notify after setting flag.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/14/2003  INB	NOP if already terminating.
     * 02/19/2003  INB	Call <code>interrupt</code>.
     * 01/02/2001  INB	Created.
     *
     */
    public final void stop(ClientHandler clientI)
	throws java.lang.InterruptedException
    {

	if (!getTerminateRequested()) {
	    //Thread.sleep(1);  // works here
            Thread.currentThread().yield();  // this works
	    // notifyAll();  // no good
		setTerminateRequested(true);  
	    //	        Thread.sleep(1000);  // no good here
	    if ((Thread.currentThread() != getThread()) &&
		(getThread() != null) &&
		getThread().isAlive()) {

		boolean cKeep = ((this instanceof SourceInterface) ?
				 ((SourceInterface) this).getCkeep() :
				 false);
		synchronized (this) {
		    for (long startAt = System.currentTimeMillis(),
			     nowAt = System.currentTimeMillis();
			 (  ((nowAt - startAt) < TimerPeriod.SHUTDOWN) &&
			    ( ( cKeep && (getRCO() != null)) ||
			      (!cKeep && (getThread() != null) && getThread().isAlive())
			    )
			 );
			 nowAt = System.currentTimeMillis()) {
			wait(TimerPeriod.NORMAL_WAIT);
		    }
		}

		if (!cKeep && (getThread() != null) && getThread().isAlive()) {
		    if (!(this instanceof Log)) {
			try {
			    getLog().addMessage
				(getLogLevel(),
				 getLogClass(),
				 getName(),
				 "Forced down after wait period expired.");
			} catch (java.lang.Exception e) {
			}
		    }
		    interrupt();
		}

	    } else if ((getThread() == null) || !getThread().isAlive()) {
		if (getParent() != null) {
		    try {
			getParent().removeChild(this);
		    } catch (Exception e) {
		    }
		}
	    }
	}
    }

    /**
     * Synchronizes with the client-side.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>Client</code> has not been connected to a
     *		      server,</li>
     *		  <li>the <code>Client</code> is not running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the synchronization is interrupted.
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2001  INB	Created.
     *
     */
    public void synchronizeWclient()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isRunning()) {
	    throw new java.io.EOFException
		("Client handler stopped before synchronization could occur.");
	}

	return;
    }

    /**
     * Updates the <code>RBNBClient</code> from the specified
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the <code>ClientInterface</code> to use.
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
     *		  thrown if the <code>ClientInterface</code> is not a
     *		  <code>RBNBClient</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    public void update(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getName() == null) {
	    setName(clientInterfaceI.getName());
	} else if ((clientInterfaceI.getName() != null) &&
		   (compareNames((Rmap) clientInterfaceI) != 0)) {
	    if (compareNames((Rmap) clientInterfaceI) != 0) {
		throw new com.rbnb.api.SerializeException
		    ("Cannot change a name once it has been set.");
	    }
	}

	if (clientInterfaceI.getType() != CLIENT) {
	    setType(clientInterfaceI.getType());
	}

	if (clientInterfaceI.getRemoteID() != null) {
	    setRemoteID(clientInterfaceI.getRemoteID());
	}
    }
}
