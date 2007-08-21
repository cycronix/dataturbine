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
 * RAM server-side communications object.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/14/2003
 */

/*
 * Copyright 2001, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 05/11/2001  INB	Created.
 *
 */
final class RAMServerCommunications
    implements com.rbnb.api.Interruptable
{
    /**
     * the <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private ServerHandler serverHandler = null;

    /**
     * the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/17/2001
     */
    private Thread thread = null;

    /**
     * list of objects waiting to be accepted.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private java.util.Vector waiting = new java.util.Vector();

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
    RAMServerCommunications() {
	super();
    }

    /**
     * Class constructor to build a <code>RAMServerCommunications</code> object
     * for the specified <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverHandlerI the <code>ServerHandler</code>.
     * @param ramI the <code>RAM</code> object.
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    RAMServerCommunications(ServerHandler serverHandlerI) {
	this();
	setServerHandler(serverHandlerI);
    }

    /**
     * Accepts connections.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI  timeout in milliseconds.
     *			<br><ul>
     *			<li><code>Client.FOREVER</code> means wait for a
     *			    response to show up, or</li>
     *			<li>anything else means wait for a response to show up
     *			    or for the timeout period to elapse.</li>
     *			</ul>
     * @return a connection object.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #connect(java.lang.Object)
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
    final RAMCommunications accept(long timeOutI)
	throws java.lang.InterruptedException
    {
	RAMCommunications clientSide = null,
			  serverSideR = null;

	synchronized (waiting) {
	    if (waiting.size() == 0) {
		if (timeOutI == Client.FOREVER) {
		    waiting.wait();
		} else if (timeOutI > 0) {
		    waiting.wait(timeOutI);
		}
	    }

	    if (waiting.size() > 0) {
		clientSide = (RAMCommunications) waiting.firstElement();
		waiting.removeElementAt(0);
	    }
	}

	if (clientSide != null) {
	    synchronized (clientSide) {
		serverSideR = new RAMCommunications();
		clientSide.setOtherSide(serverSideR);
		serverSideR.setOtherSide(clientSide);
	    }
	}

	return (serverSideR);
    }

    /**
     * Connects to the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client-side communications object.
     * @see #accept(long)
     * @exception java.lang.InterruptedException
     *		  thrown if this operaiton is interrupted.
     * @see com.rbnb.api.RAMCommunications#disconnect()
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 05/11/2001  INB	Created.
     *
     */
    final void connect(Object clientSideI)
	throws java.lang.InterruptedException
    {
	RAMCommunications cs = (RAMCommunications) clientSideI;

	synchronized (waiting) {
	    waiting.addElement(cs);
	    waiting.notifyAll();
	}

	synchronized (cs) {
	    while (cs.getOtherSide() == null) {
		cs.wait(TimerPeriod.NORMAL_WAIT);
	    }
	}
    }

    /**
     * Gets the <code>ServerHandler</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @see #setServerHandler(com.rbnb.api.ServerHandler)
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
    final ServerHandler getServerHandler() {
	return (serverHandler);
    }

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
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
    public final Thread getThread() {
	return (thread);
    }

    /**
     * Interrupts this <code>RAMServerCommunications</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.SecurityException
     *		  thrown if the interrupt is not allowed by the security
     *		  system.
     * @see java.lang.Thread#interrupt()
     * @since V2.0
     * @version 05/07/2001
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
     * Runs this <code>RAMServerCommunications</code> object.
     * <p>
     * This method should only be used by non-RAM servers.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 05/17/2001  INB	Created.
     *
     */
    public final void run() {
	try {
	    while (true) {
		try {
		    Object clientSide = accept(Client.FOREVER);
		    if (clientSide != null) {
			RCO rco = RCO.newRCO(clientSide,getServerHandler());
			rco.start();
		    }

		    if (getThread() != null) {
			((ThreadWithLocks) getThread()).ensureLocksCleared
			    (toString(),
			     "RAMServerCommunications.run(1)",
			     null,
			     (byte) 0,
			     0L);
		    }

		} catch (java.lang.RuntimeException e) {
		    e.printStackTrace();

		} catch (com.rbnb.api.RBNBException e) {
		    e.printStackTrace();
		}
	    }

	} catch (java.lang.Exception e) {

	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "RAMServerCommunications.run(2)",
		     null,
		     (byte) 0,
		     0L);
	    }
	    setThread(null);
	}
    }

    /**
     * Sets the <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverHandlerI the <code>ServerHandler</code>.
     * @see #getServerHandler()
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
    final void setServerHandler(ServerHandler serverHandlerI) {
	serverHandler = serverHandlerI;
    }

    /**
     * Sets the thread running this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI the thread.
     * @see #getThread()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI) {
	thread = threadI;
    }

    /**
     * Starts this <code>RAMServerCommunications</code>.
     * <p>
     * This method should only be used by non-RAM servers.
     * <p>
     *
     * @author Ian Brown
     *
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
     * 05/17/2001  INB	Created.
     *
     */
    final void start() {
	setThread(new ThreadWithLocks(this));
	getThread().start();
    }

    /**
     * Stops this <code>RAMServerCommunications</code>.
     * <p>
     * This method should only be used by non-RAM servers.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #run()
     * @see #start()
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  INB	Created.
     *
     */
    final void stop() {
	interrupt();
    }
}
