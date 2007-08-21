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
 * The child server class.
 * <p>
 * A child server is one that represents an <bold>RBNB</bold> that is the
 * child of the local one. It is one of the three directions a message can be
 * routed towards its destination, with the others being
 * <code>PeerServer</code> and <code>ParentServer</code> servers.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.ParentServer
 * @see com.rbnb.api.PeerServer
 * @since V2.0
 * @version 02/18/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/18/2004  INB	Ensure that the route is not off in addition to
 *			connected.
 * 02/26/2003  INB	When we lose routing, call <code>stop</code> rather
 *			than <code>disconnectedRouting</code>.
 * 11/20/2001  INB	Created.
 *
 */
final class ChildServer
    extends com.rbnb.api.HierarchicalServer
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/26/2001  INB	Created.
     *
     */
    ChildServer() {
	super();
    }

    /**
     * Class constructor to build a <code>ChildServer</code> for a name and an
     * address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
     * @param addressI the server's address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address provided.
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
     * @version 11/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/27/2001  INB	Created.
     *
     */
    ChildServer(String nameI,String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI,addressI);
    }

    /**
     * Creates a <code>Router</code> to the "real" hierarchical server from the
     * other side.
     * <p>
     * This method uses the input <code>Router</code> to create a second one by
     * asking the remote side to make a reverse connection to here.
     * <p>
     *
     * @author Ian Brown
     *
     * @param routerI the existing <code>Router</code>.
     * @return handled the create?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/06/2001  INB	Created.
     *
     */
    final boolean createReversedRouter(Router routerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean handledR = false;

	if (!(handledR = super.createReversedRouter(routerI))) {
	    Rmap pServer = getParent().newInstance();
	    pServer.addChild(newInstance());
	    ReverseRoute reverse = new ReverseRoute(pServer);
	    routerI.send(reverse);
	    routerI.receive(null,false,Client.FOREVER);
	    handledR = true;
	}

	return (handledR);
    }

    /**
     * Finds the lowest cost <code>Path</code> from the local server
     * (<code>RBNB</code> object) to this <code>RemoteServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the lowest cost <code>Path</code>.
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
     * @version 02/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Ensure that the route is not off in addition to
     *			connected.
     * 11/20/2001  INB	Created.
     *
     */
    public final Path findPath()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	LocalPath pathR = null;

	if (getConnected() && (getType() != ROUTE_OFF)) {
	    pathR = new LocalPath();
	    pathR.add((Server) getParent());
	    pathR.add(this);
	    pathR.setCost(1);
	}

	return (pathR);
    }

    /**
     * Gets the log class mask for this <code>ChildServer</code>.
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
	return (super.getLogClass() | Log.CLASS_CHILD_SERVER);
    }

    /**
     * Lost routing to this <code>ChildServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	When we lose routing, call <code>stop</code> rather
     *			than <code>disconnectedRouting</code>.
     * 12/14/2001  INB	Created.
     *
     */
    final void lostRouting() {
	if (!getConnected()) {
	    return;
	}
	setConnected(false);

	synchronized (getAvailableRouters()) {
	    getAvailableRouters().notifyAll();
	}

	try {
	    getLog().addMessage
		(getLogLevel(),
		 getLogClass(),
		 getName(),
		 "Lost all connections to child server. " +
		 "Removing child server from routing map.");
	} catch (java.lang.Exception e) {
	}

	if (true) {
	    stop();

	} else {
	    disconnectedRouting();

	    try {
		Rmap lParent = getParent();
		lParent.removeChild(this);
		if (getNchildren() > 0) {
		    RemoteServer rServer = new RemoteServer(getName(),
							    getAddress());
		    rServer.metricsDeadBytes = metricsDeadBytes;
		    lParent.addChild(rServer);
		    while (getNchildren() > 0) {
			Rmap child = getChildAt(0);
			removeChildAt(0);
			rServer.addChild(child);
		    }

		} else if (lParent instanceof RemoteServer) {
		    ((RemoteServer) lParent).metricsDeadBytes +=
			bytesTransferred();

		} else if (lParent instanceof RBNBRoutingMap) {
		    ((RBNBRoutingMap) lParent).metricsDeadBytes +=
			bytesTransferred();
		}

	    } catch (java.lang.Exception e) {
	    }
	}
    }
}
