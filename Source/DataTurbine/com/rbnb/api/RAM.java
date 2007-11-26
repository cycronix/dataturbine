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
 * Extended <code>Address</code> for dealing RAM communications.
 * <p>
 * In memory communications are handled by copying RBNB language commands and
 * responses from thread to thread.  This is done by creating a pair of
 * objects:
 * <p><ol>
 *    <li>A <code>RAMCommunications</code> object for the client side, and</li>
 *    <li>A <code>RAMServerCommunications</code> object for the server
 *	  side.</li>
 * </ol><p>
 * Messages sent using one of these objects are copied into a buffer held by
 * the other.  The receiver returns the oldest message in its buffer,
 * optionally waiting for some period of time for such a message to show up.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/06/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/06/2004  INB	Added documentation.
 * 12/24/2003  INB	Allow for <code>nulls</code>.
 * 05/11/2001  INB	Created.
 *
 */
final class RAM
    extends com.rbnb.api.Address
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #RAM(String)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2001  INB	Created.
     *
     */
    private RAM() {
	super();
    }

    /**
     * Class constructor to build a <code>RAM</code> object for the input
     * address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
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
    RAM(String addressI)
	throws com.rbnb.api.AddressException
    {
	super(addressI);
    }

    /**
     * Accepts connections to a server-side connection object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI the server-side connection object.
     * @param timeOutI  timeout in milliseconds.
     *			<br><ul>
     *			<li><code>Client.FOREVER</code> means wait for a
     *			    response to show up, or</li>
     *			<li>anything else means wait for a response to show up
     *			    or for the timeout period to elapse.</li>
     *			</ul>
     * @return the connection object.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
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
     * 05/11/2001  INB	Created.
     *
     */
    final Object accept(Object serverSideI,long timeOutI)
	throws java.io.IOException,
	       java.lang.InterruptedException
    {
	RAMServerCommunications ss = (RAMServerCommunications) serverSideI;

	return (ss.accept(timeOutI));
    }

    /**
     * Closes a connection object for this <code>RAM</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param connectionI the connection object.
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
    final void close(Object connectionI) {
    }

    /**
     * Connects to the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code> to connect.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnect(java.lang.Object)
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
    final void connect(Object clientSideI)
	throws java.io.IOException,
	       java.lang.InterruptedException
    {
	RAMCommunications cs = (RAMCommunications) clientSideI;
	ServerHandler sh =
	    ((Server)
	     (cs.getACO().getClient().getParent())).getServerSide();
	RAMServerCommunications rsc = sh.getRAMPort();

	rsc.connect(cs);
    }

    /**
     * Disconnects from the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientSideI the client-side object.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #accept(java.lang.Object,long)
     * @since V2.0
     * @version 12/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/24/2003  INB	Check for <code>null</code>.
     * 05/11/2001  INB	Created.
     *
     */
    final void disconnect(Object clientSideI)
	throws java.io.IOException,
	       java.lang.InterruptedException
    {
	RAMCommunications cs = (RAMCommunications) clientSideI;

	if (cs != null) {
	    cs.disconnect();
	}
    }

    /**
     * Is the address one that can be handled by a local RBNB server?
     * <p>
     * The answer is always yes.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the address local?
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     * 2007/11/26  WHF  Removed to be consistent with TCP.java.
     *
     */
    //final boolean isLocal() { return (true); }

    /**
     * Creates a new client-side connection to the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acoI the <code>ACO</code>.
     * @return the connection object.
     * @see #newServerSide(com.rbnb.api.ServerHandler)
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
    final Object newClientSide(ACO acoI) {
	return (new RAMCommunications(acoI));
    }

    /**
     * Creates a new server-side connection to the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverHandlerI the <code>ServerHandler</code>.
     * @return the connection object.
     * @see #newClientSide(com.rbnb.api.ACO)
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
    final Object newServerSide(ServerHandler serverHandlerI) {
	return (new RAMServerCommunications(serverHandlerI));
    }
}
