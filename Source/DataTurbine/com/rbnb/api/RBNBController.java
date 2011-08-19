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
 * Server-side object that represents a client application control
 * connection to an RBNB server.
 * <p>
 * Control connections allow clients to examine the routing map
 * (<code>RBNBRoutingMap</code>) of a server and to control the various
 * connections to that server from clients or between that server and other
 * servers.
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
 * 08/19/2011  MJM  Catch *any* exceptions on timer
 * 08/04/2004  INB	Added documentation.
 * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 01/16/2004  INB	Ensure that we are synchronized while switching over
 *			the metrics when reversing the route.  Ensure that the
 *			<code>RCO</code> is stopped before we're removed from
 *			our parent.
 * 01/14/2004  INB	Added synchronization of metrics.
 * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
 * 04/04/2003  INB	Handle Java errors.
 * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
 * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
 * 03/19/2003  INB	Allow for the <code>ServerHandler</code> to become
 *			null.
 * 02/26/2003  INB	Moved the shutdown code from the <code>run</code>
 *			method to a <code>shutdown</code> method.
 * 05/15/2001  INB	Created.
 *
 */
class RBNBController
    extends com.rbnb.api.RBNBClient
    implements com.rbnb.api.ControllerHandler
{

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
    RBNBController() {
	super();
    }

    /**
     * Class constructor to build an <code>RBNBController</code> for an
     * <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code>.
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
    RBNBController(RCO rcoI) {
	super(rcoI);
    }

    /**
     * Class constructor to build a <code>RBNBController</code> for a
     * particular <code>RCO</code> from a <code>ControllerInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI	  the <code>RCO</code>.
     * @param controllerI the <code>ControllerInterface</code>.
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
     * 05/15/2001  INB	Created.
     *
     */
    RBNBController(RCO rcoI,ControllerInterface controllerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(rcoI);
	update(controllerI);
    }

    /**
     * Gets the log class mask for this <code>RBNBController</code>.
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
	return (super.getLogClass() | Log.CLASS_RBNB_CONTROLLER);
    }

    /**
     * Creates a new instance of the same class as this
     * <code>RBNBController</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    final Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (ControllerIO.newInstance(this));
    }

    /**
     * Handle a request to reverse a route.
     * <p>
     * This method passes the request off to the handler thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reverseI the <code>ReverseRoute</code> request.
     * @return has the <code>RCO</code> reversed roles?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is an serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Ensure that we are synchronized while switching over
     *			the metrics.
     * 01/25/2002  INB	Created.
     *
     */
    public boolean reverseRoute(ReverseRoute reverseI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RCO theRCO = getRCO();
	synchronized (metricsSyncObj) {
	    setRCO(null);
	    theRCO.metricsBytes += metricsBytes;
	    metricsBytes = 0;
	}

	PeerServer pServer = (PeerServer) reverseI.getObject();
	Rmap child = pServer.getChildAt(0);
	if (child instanceof PeerServer) {
	    Server sChild = (Server) child;
	    ChildServer childServer =
		(ChildServer) getParent().findDescendant(sChild.getFullName(),
							 false);

	    if (childServer == null) {
		childServer = new ChildServer(sChild.getName(),
					      sChild.getAddress());
		childServer.setLocalServerHandler((ServerHandler) getParent());
	        getParent().addChild(childServer);
	    }
	    childServer.reversed(theRCO,false);

	} else {
	    Shortcut sc = (Shortcut) pServer.getChildAt(0);
	    ShortcutHandler scHandler = (ShortcutHandler)
		getParent().findDescendant((PATHDELIMITER +
					    getParent().getName() +
					    PATHDELIMITER +
					    sc.getName()),
					   false);
	    scHandler.reversed(theRCO);
	}

	return (true);
    }

    /**
     * Runs the <code>RBNBController</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
     * 04/04/2003  INB	Handle Java errors.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 03/19/2003  INB	Allow for the <code>ServerHandler</code> to become
     *			null.
     * 01/02/2001  INB	Created.
     *
     */
    public final void run() {
	TimerTask statusTT = null;
	try {
	    // Log the start.
	    String message;
	    if (getRCO().getBuildVersion() == null) {
		message = "Started for client running an old build version";
	    } else {
		message = ("Started for client running " +
			   getRCO().getBuildVersion() +
			   " from " + getRCO().getBuildDate());
	    }
	    /*
	    if (getRCO().getLicenseString() != null) {
		message += " using license " + getRCO().getLicenseString();
	    }
	    */
	    message += ".";
	    getLog().addMessage
		(getLogLevel(),
		 getLogClass(),
		 getName(),
		 message);

	    synchronized (this) {
		notifyAll();
	    }

	    // Schedule a status logger.
	    if (getLocalServerHandler().getLogStatusPeriod() > 0) {
		MetricsCollector metricsCollector = new MetricsCollector();
		metricsCollector.setObject(this);
		statusTT = new TimerTask(metricsCollector,
					 LogStatusInterface.TT_LOG_STATUS);
		if (getLocalServerHandler() == null) {
		    setTerminateRequested(true);
		} else {
		    try {
			getLocalServerHandler().getTimer().schedule
			    (statusTT,
			     getLocalServerHandler().getLogStatusPeriod(),
			     getLocalServerHandler().getLogStatusPeriod());
//		    } catch (java.lang.NullPointerException e) {
		    } catch (Exception e) {		// mjm 8.18.11 catch any exception!
			setTerminateRequested(true);
		    }
		}
	    }

	    // Wait until we are asked to stop.
	    while (!getTerminateRequested() && !getThread().interrupted()) {
		synchronized (this) {
		    wait(TimerPeriod.LONG_WAIT);
		}
		if (getThread() != null) {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBNBController.run(1)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		}
	    }

	} catch (com.rbnb.api.AddressException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.io.IOException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.InterruptedException e) {
	    try {
		getRCO().send(Language.exception(e));
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Error e) {
	    try {
		getLog().addError(Log.STANDARD,
				  getLogClass(),
				  toString(),
				  e);
		getRCO().send(Language.exception
			      (new java.lang.Exception
				  ("A fatal error occured.\n" +
				   e.getClass() + " " + e.getMessage())));
	    } catch (java.lang.Throwable e1) {
	    }

	} finally {
	    if (getThread() != null) {
		try {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBNBController.run(2)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		} catch (java.lang.Exception e) {
		}
	    }
	}

	// Log the end.
	synchronized (this) {
	    if (statusTT != null) {
		try {
		    statusTT.cancel();
		} catch (java.lang.Exception e) {
		}
		statusTT = null;
	    }
	}
	logStatus("Is shutting down");
	synchronized (this) {
	    if (getLog() != null) {
		try {
		    getLog().addMessage
			(getLogLevel(),
			 getLogClass(),
			 getName(),
			 "Stopped.");
		} catch (java.lang.Exception e) {
		}
	    }
	}

	// Shutdown.
	shutdown();
    }

    /**
     * Shuts down this <code>RBNBController</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Ask the <code>RCO</code> to stop before removing us.
     * 01/14/2004  INB	Added synchronization of metrics.
     * 02/26/2003  INB	Created.
     *
     */
    public void shutdown() {
	// Ask the <code>RCO</code> to stop.
	RCO lRCO = null;
	try {
	    if (getRCO() != null) {
		lRCO = getRCO();
		getRCO().stop();
	    }
	} catch (java.lang.Exception e) {
	}
	if (lRCO != null) {
	    synchronized (metricsSyncObj) {
		metricsBytes += lRCO.bytesTransferred();
		setRCO(null);
	    }
	}

	// Remove us as a child.
	try {
	    Rmap lParent = getParent();
	    RemoteServer rParent = (RemoteServer) lParent;
	    synchronized (rParent.metricsSyncObj) {
		lParent.removeChild(this);
		((RemoteServer) lParent).metricsDeadBytes +=
		    bytesTransferred();
	    }

	} catch (java.lang.Exception e) {
	}

	setThread(null);
	synchronized (this) {
	    notifyAll();
	}
    }
}
