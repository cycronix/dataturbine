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
 * Client-side object that represents a client application connection to an
 * RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/14/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/14/2004  INB	Added metrics synchronization.
 * 03/31/2003  INB	Throw an exception on a name mismatch for our parent.
 * 05/11/2001  INB	Created.
 *
 */
abstract class ClientHandle
    extends com.rbnb.api.ClientIO
    implements com.rbnb.api.Client
{
    /**
     * the <code>ACO</code> for communicating with the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private transient ACO aco = null;

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
     * have we been started?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/11/2002
     */
    private boolean started = false;

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
    ClientHandle() {
	super();
    }

    /**
     * Class constructor to build a <code>ClientHandle</code> by reading it in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
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
    ClientHandle(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>ClientHandle</code> from a name.
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    ClientHandle(String nameI)
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
     * Finalizes this connection to the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/11/2002  INB	Created.
     *
     */
    protected final void finalize() {
	try {
	    stop();
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Gets the <code>ACO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ACO</code>.
     * @see #setACO(com.rbnb.api.ACO)
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
    final ACO getACO() {
	return (aco);
    }

    /**
     * Gets the list of registered <code>Rmaps</code> matching the requested
     * <code>Rmap</code> hierarchy.
     * <p>
     * At this time the following <code>Rmap</code> hierarchies are
     * implemented:
     * <p><ul>
     *	  <li>{unnamed}[/...]</code>,</li>
     *    <li>[{unnamed}/]servername[/...],</li>
     *    <li>[{unnamed}/]servername/clientname[/...],</li>
     *    <li>[{unnamed}/]servername/clientname1[/....],/clientname2[/...],
     *	      ...</li>
     * </ul><p>
     * Syntax notes:
     * <br>/ indicates a child <code>Rmap</code>.
     * <br>[/...] indicates an optional child <code>Rmap</code> with a name of
     *	   "...". This  causes the registration maps of the children of the
     *	   <code>Rmap</code> to be expanded.
     * <br>, indicates multiple children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the requested <code>Rmap</code> hierarchy.
     * @return the <code>Rmap</code> hierarchy containing the registered
     *	       <code>Rmaps</code>.
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
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public synchronized Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	Rmap registeredR = getACO().getRegistered(requestI);
	return (registeredR);
    }

    /**
     * Gets the started flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return has this <code>Client</code> started?
     * @see #setStarted(boolean)
     * @since V2.0
     * @version 03/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/11/2002  INB	Created.
     *
     */
    final boolean getStarted() {
	return (started);
    }

    /**
     * Is this <code>ClientHandle</code> running?
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
     *		  thrown if the <code>Client</code> is in a bad state.
     * @exception java.lang.InterruptedException
     *		  thrown if the check is interrupted.
     * @return is this <code>Client</code> running?
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public final synchronized boolean isRunning()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return ((getACO() == null) ? false : getACO().isRunning(this));
    }

    /**
     * Sets the <code>ACO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acoI the <code>ACO</code>.
     * @see #getACO()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *c
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    final void setACO(ACO acoI) {
	aco = acoI;
    }

    /**
     * Sets the started flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startedI has this <code>Client</code> started?
     * @see #getStarted()
     * @since V2.0
     * @version 03/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/11/2002  INB	Created.
     *
     */
    final void setStarted(boolean startedI) {
	started = startedI;
    }

    /**
     * Starts the <code>ClientHandle</code>.
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
     *		  <li>the <code>ClientHandle</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>ClientHandle</code> is already running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #stop()
     * @since V2.0
     * @version 03/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/31/2003  INB	Throw an exception on a name mismatch for our parent.
     * 12/21/2000  INB	Created.
     *
     */
    public synchronized void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!getStarted()) {
	    getACO().start();
	    setStarted(true);

	    // Fill in the <code>Server</code> hierarchy.
	    if (!(getParent() instanceof RemoteServer) &&
		(getParent() instanceof Server) &&
		(getParent().getParent() == null)) {
		Rmap rmap = getRegistered(new Rmap());
		RoutingMap rtMap = (RoutingMap) rmap.getChildAt(0);
		if ((rtMap != null) && (rtMap.getLocalName() != null)) {
		    String name = rtMap.getLocalName();
		    if ((getParent().getName() != null) &&
			(getParent().compareNames("") != 0) &&
			(getParent().compareNames("DTServer") != 0)) {
			if (getParent().getFullName().compareTo(name) != 0) {
			    throw new java.lang.IllegalStateException
				("Our parent server already has a name (" +
				 getParent().getFullName() +") and it " +
				 "doesn't match the name returned by the " +
				 "remote (" + name + ").");
			}
		    } else {
			getParent().setName
			    (name.substring(name.lastIndexOf("/") + 1));
			Rmap hierarchy = createFromName(name,new Server()),
			    bottom = hierarchy.moveToBottom(),
			    above = bottom.getParent();
			if (above != null) {
			    above.removeChildAt(0);
			    above.addChild(getParent());
			}
		    }
		}
	    }
	}
    }

    /**
     * Starts the specified <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
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
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #stop(com.rbnb.api.Client)
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public final synchronized void start(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().start(clientI);
    }

    /**
     * Starts a shortcut connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>Shortcut</code>.
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
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #stop(com.rbnb.api.Server)
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final void start(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().start(shortcutI);
    }

    /**
     * Stops the <code>ClientHandle</code>.
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
     *		  <li>the <code>ClientHandle</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>ClientHandle</code> is already running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #start()
     * @since V2.0
     * @version 01/14/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2004  INB	Added metrics synchronization.
     * 12/21/2000  INB	Created.
     *
     */
    public synchronized void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getStarted()) {
	    getACO().stop();
	    metricsBytes = getACO().bytesTransferred();

	    Rmap lParent = getParent();

	    if (!(this instanceof IOMetricsInterface)) {
		lParent.removeChild(this);
	    } else {
		if (lParent instanceof RemoteServer) {
		    RemoteServer rParent = (RemoteServer) lParent;
		    synchronized (rParent.metricsSyncObj) {
			lParent.removeChild(this);
			rParent.metricsDeadBytes +=
			    ((IOMetricsInterface) this).bytesTransferred();
		    }

		} else if (lParent instanceof RBNBRoutingMap) {
		    RBNBRoutingMap rtmap = (RBNBRoutingMap) lParent;
		    synchronized (rtmap.metricsSyncObj) {
			lParent.removeChild(this);
			rtmap.metricsDeadBytes +=
			    ((IOMetricsInterface) this).bytesTransferred();
		    }
		}
	    }
		
	    setStarted(false);
	}
    }

    /**
     * Stops the <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #start(com.rbnb.api.Client)
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public final synchronized void stop(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().stop(clientI);
    }

    /**
     * Stops the local <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI the <code>Server</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public final synchronized void stop(Server serverI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().stop(serverI);
    }

    /**
     * Stops a local <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>Shortcut</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final synchronized void stop(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().stop(shortcutI);
    }

    /**
     * Synchronizes with the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>ClientHandle</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>ClientHandle</code> is not running.</li>
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
     * 12/22/2000  INB	Created.
     *
     */
    public synchronized void synchronizeWserver()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getACO().synchronizeWserver();
    }
}
