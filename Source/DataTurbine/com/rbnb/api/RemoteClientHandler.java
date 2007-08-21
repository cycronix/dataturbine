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
 * Remote client handler class.
 * <p>
 * This class provides the client handler for remote sink clients wishing to
 * get data from this RBNB DataTurbine server or from a server known to this
 * one.
 * <p>
 * The operation of this class is described as part of the documentation on
 * routing in <code>RemoteServer</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RemoteClient
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/05/2004  INB	Updated documentation and added link to RemoteServer
 *			and StreamRemoteListener.
 * 02/16/2004  INB	Added handling of
 *			<code>DisconnectedClientException</code>.  This is a
 *			special one that means that the client of this handler
 *			has gone away during the time it took us to deliver a
 *			message to it.  This is either benign (the client meant
 *			to close down and the timing just happened to cause a
 *			problem) or a domino exception (i.e., another exception
 *			caused the client to shutdown).
 * 02/11/2004  INB	Log exceptions at standard level.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 04/10/2003  INB	Added <code>UsernameInterface</code>.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 12/03/2001  INB	Created.
 *
 */
final class RemoteClientHandler
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.GetLogInterface,
	       com.rbnb.api.UsernameInterface,
	       java.lang.Runnable
{
    /**
     * have we been started?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private boolean started = false;

    /**
     * the active request message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private RoutedMessage aRequest = null;

    /**
     * the <code>RoutedMessage</code> to work on.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */
    private RoutedMessage rMessage = null;

    /**
     * our parent <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */
    private ServerHandler serverHandler = null;

    /**
     * the <code>Sink</code> to direct messages to or receive messages from.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */
    private Sink sink = null;

    /**
     * the running thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */
    private Thread thread = null;

    /**
     * the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/10/2003
     */
    private Username username = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    RemoteClientHandler() {
	super();
    }

    /**
     * Handles a request for information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rMessageI the <code>RoutedMessage</code>.
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 12/03/2001  INB	Created.
     *
     */
    private final void ask(RoutedMessage rMessageI) {
	Ask ask = (Ask) rMessageI.getMessage();
	Serializable response = null;

	try {
	    if (ask.getInformationType().equalsIgnoreCase(Ask.REGISTERED)) {
		Rmap rmap = (Rmap) ask.getAdditional().firstElement();
		response = getSink().getRegistered(rmap);

	    } else if (ask.getInformationType().equalsIgnoreCase
		       (Ask.REQUESTAT)) {
		// Request is to match a child at a index.
		int index =
		    ((Integer)
		     ask.getAdditional().firstElement()).intValue();
		getSink().initiateRequestAt(index);
		setActiveRequest(rMessageI);

	    } else {
		response = Language.exception
		    (new java.lang.IllegalStateException
			(ask + " is not supported yet."));
	    }

	} catch (java.lang.Exception e) {
	    response = Language.exception(e);
	}

	if (response != null) {
	    try {
		respond(rMessageI,response);
	    } catch (java.lang.Exception e) {
		try {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		} catch (java.lang.Exception e1) {
		}
	    }
	}
    }

    /**
     * Gets the active request.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the active request <code>RoutedMessage</code>.
     * @see #setActiveRequest(com.rbnb.api.RoutedMessage)
     * @since V2.0
     * @version 12/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    private final com.rbnb.api.RoutedMessage getActiveRequest() {
	return (aRequest);
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
	return (getServerHandler().getLog());
    }

    /**
     * Gets the log class mask for this <code>RemoteClientHandler</code>.
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
	return (Log.CLASS_REMOTE_CLIENT_HANDLER);
    }

    /**
     * Gets the base log level for this <code>RemoteClientHandler</code>.
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
	return (Log.STANDARD);
    }

    /**
     * Gets the <code>RoutedMessage</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RoutedMessage</code>.
     * @see #setRoutedMessage(com.rbnb.api.RoutedMessage)
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final RoutedMessage getRoutedMessage() {
	return (rMessage);
    }

    /**
     * Gets our parent <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @see #setServerHandler(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final ServerHandler getServerHandler() {
	return (serverHandler);
    }

    /**
     * Gets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Sink</code>.
     * @see #setSink(com.rbnb.api.Sink)
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final Sink getSink() {
	return (sink);
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
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final Thread getThread() {
	return (thread);
    }

    /**
     * Gets the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Username</code>.
     * @see #setUsername(com.rbnb.api.Username usernameI)
     * @since V2.1
     * @version 04/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2003  INB	Created.
     *
     */
    public final Username getUsername() {
	return (username);
    }

    /**
     * Receives a <code>RoutedMessage</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RoutedMessage</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #send(com.rbnb.api.RoutedMessage)
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 12/03/2001  INB	Created.
     *
     */
    private final synchronized RoutedMessage receive()
	throws java.lang.InterruptedException
    {
	while (getRoutedMessage() == null) {
	    if (getActiveRequest() != null) {
		break;
	    }
	    wait(TimerPeriod.NORMAL_WAIT);
	}

	RoutedMessage rMessageR = getRoutedMessage();
	setRoutedMessage(null);
	notifyAll();

	return (rMessageR);
    }

    /**
     * Responds to a request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rMessageI the <code>RoutedMessage</code>.
     * @param responseI the <code>Serializable</code> response.
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
     * @version 01/02/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final void respond(RoutedMessage rMessageI,Serializable messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RoutedMessage rMessageL = new RoutedMessage();
	rMessageL.setMessage(messageI);
	rMessageL.setSource(rMessageI.getTarget());
	rMessageL.setTarget(rMessageI.getSource());

	Serializable serializable =
	    getServerHandler().getRoutingMapHandler().deliver
	    (rMessageL,
	     1);
	if (!Language.isOK(serializable)) {
	    if (serializable instanceof ExceptionMessage) {
		Language.throwException((ExceptionMessage) serializable);
	    } else {
		throw new com.rbnb.api.SerializeException
		    ("Unexpected response: " + serializable + ".");
	    }
	}
    }

    /**
     * Runs this <code>RemoteClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.0
     * @version 02/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/16/2004  INB	Added handling of
     *			<code>DisconnectedClientException</code>.  This is a
     *			special one that means that the client of this handler
     *			has gone away during the time it took us to deliver a
     *			message to it.  This is either benign (the client meant
     *			to close down and the timing just happened to cause a
     *			problem) or a domino exception (i.e., another exception
     *			caused the client to shutdown).
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 04/10/2003  INB	Set the <code>Username</code> for the
     *			<code>Sink</code>.
     * 12/03/2001  INB	Created.
     *
     */
    public final void run() {
	try {
	    synchronized (this) {
		setSink
		    (getServerHandler().getClientSide().createRAMSink
		     (getName()));
		getSink().setUsername(getUsername());
		getSink().start();
		started = true;
		notifyAll();
	    }
	    Thread.yield();

	    RoutedMessage wMessage;
	    while (true) {
		wMessage = receive();

		if (wMessage != null) {
		    Serializable message = wMessage.getMessage();

		    if (message instanceof Acknowledge) {
			((ClientHandle) getSink()).getACO().send(message);

		    } else if (Language.isRequest(message)) {
			ask(wMessage);

		    } else if (Language.isStop(message)) {
			break;

		    } else if (message instanceof Rmap) {
			getSink().addChild((Rmap) message);
		    }
		}

		if (getActiveRequest() != null) {
		    Rmap response = getSink().fetch(100);
		    if (response != null) {
			respond(getActiveRequest(),response);
			if (response instanceof EndOfStream) {
			    setActiveRequest(null);
			}
		    }
		}

		if (getThread() != null) {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RemoteClientHandler.run(1)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		}
	    }

	} catch (com.rbnb.api.DisconnectedClientException e) {

	} catch (com.rbnb.api.AddressException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}

	} catch (java.io.IOException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}

	} catch (java.lang.InterruptedException e) {
	    try {
		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     getName(),
		     e);
	    } catch (java.lang.Exception e1) {}

	} finally {
	    if (!started) {
		synchronized (this) {
		    started = true;
		    notifyAll();
		}
		Thread.currentThread().yield();
	    }
	    if (getSink() != null) {
		try {
		    getSink().stop();
		    Thread.currentThread().yield();
		    started = false;
		    getServerHandler().removeRemoteClientHandler(this);
		} catch (java.lang.Exception e) {
		}
		setSink(null);
	    }
	    if (getThread() != null) {
		try {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RemoteClientHandler.run(2)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		} catch (java.lang.Exception e) {
		}
	    }
	    setThread(null);
	}
    }

    /**
     * Sends a <code>RoutedMessage</code> to the primary thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rMessageI the <code>RoutedMessage</code> to send.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #receive()
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 12/03/2001  INB	Created.
     *
     */
    final synchronized void send(RoutedMessage rMessageI)
	throws java.lang.InterruptedException
    {
	while (getRoutedMessage() != null) {
	    wait(TimerPeriod.NORMAL_WAIT);
	}

	setRoutedMessage(rMessageI);
	notifyAll();
    }

    /**
     * Sets the active request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param aRequestI the active request <code>RoutedMessage</code>.
     * @see #getActiveRequest()
     * @since V2.0
     * @version 12/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2001  INB	Created.
     *
     */
    private final void setActiveRequest(RoutedMessage aRequestI) {
	aRequest = aRequestI;
    }

    /**
     * Sets the <code>RoutedMessage</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rMessageI the <code>RoutedMessage</code>.
     * @see #getRoutedMessage()
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final void setRoutedMessage(RoutedMessage rMessageI) {
	rMessage = rMessageI;
    }

    /**
     * Sets our parent <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverHandlerI the <code>ServerHandler</code>.
     * @see #getServerHandler()
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    final void setServerHandler(ServerHandler serverHandlerI) {
	serverHandler = serverHandlerI;
    }

    /**
     * Sets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI the <code>Sink</code>.
     * @see #getSink()
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final void setSink(Sink sinkI) {
	sink = sinkI;
    }

    /**
     * Sets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI the running thread.
     * @see #getThread()
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI){
	thread = threadI;
    }

    /**
     * Sets the <code>Username</code> for this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @see #getUsername()
     * @since V2.1
     * @version 04/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2003  INB	Created.
     *
     */
    public final void setUsername(Username usernameI) {
	username = usernameI;
    }

    /**
     * Starts this <code>RemoteClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #run()
     * @see #stop()
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
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 12/03/2001  INB	Created.
     *
     */
    final void start()
	throws java.lang.InterruptedException
    {
	setThread(new ThreadWithLocks(this,"_RCH." + getName()));
	synchronized (this) {
	    getThread().start();
	    while (!started) {
		wait(TimerPeriod.NORMAL_WAIT);
	    }
	}
    }

    /**
     * Stops this <code>RemoteClientHandler<code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #run()
     * @see #start()
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    final void stop() {
	if (getThread() != null) {
	    getThread().interrupt();
	    setThread(null);
	}
    }
}
