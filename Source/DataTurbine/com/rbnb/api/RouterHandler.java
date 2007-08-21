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
 * Server-side representation of a routing connection from an <bold>RBNB</bold>
 * server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 11/21/2001  INB	Created.
 *
 */
interface RouterHandler
    extends com.rbnb.api.ControllerHandler,
	    com.rbnb.api.RouterInterface
{

    /**
     * Accepts a routing connection from another <code>RBNB</code> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI	 the type of connection:
     *			 <p><ul>
     *			 <li>"PARENT" - accept a connection from a parent
     *			     server,</li>>
     *			 <li>"CHILD" - accept a connection from a child
     *			     server,</li>
     *			 <li>"PEER" - accept a connecion from a peer
     *			     server.</li>
     *			 </ul><p>
     * @param hierarchyI the <code>Rmap</code> hierarchy representing the other
     *			 <bold>RBNB</code>. The hierarchy depends on the type
     *			 of connection: 
     *			 <p><ul>
     *			 <li>type = "PARENT" or type = "PEER" - the hierarchy
     *			     consists of a <code>RoutingMap</code> and
     *			     <code>Servers</code>, or</li>
     *			 <li>type = "CHILD" - the hierarchy consists of a
     *			     single <code>Server</code>.</li>
     *			 </ul><p>
     * @return the <code>Rmap</code> hierarchy representing this side of the
     *	       connection.
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
    public abstract Rmap acceptRouteFrom(String typeI,Rmap hierarchyI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Clones this object.
     * <p>
     * This same abstract declaration is also included in RmapInterface.java,
     * but for some unknown reason J# gives a compiler error if it is not also
     * included here.
     *
     * @author John Wilson
     *
     * @return the clone.
     * @see java.lang.Cloneable
     * @since V2.5
     * @version 09/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/29/2004  JPW	Created.
     *
     */
    public abstract Object clone();

    /**
     * Delivers a <code>RoutedMessage</code> to a local target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @return a response.
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
    public abstract Serializable deliverLocal(RoutedMessage messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Delivers a <code>RoutedMessage</code> to a remote target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>RoutedMessage</code>.
     * @return a response.
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
    public abstract Serializable deliverRemote(RoutedMessage messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Updates the information stored about a <code>PeerServer</code> of the
     * local <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateI the <code>PeerUpdate</code> message.
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
    public abstract void updatePeer(PeerUpdate peerUpdateI);
}
