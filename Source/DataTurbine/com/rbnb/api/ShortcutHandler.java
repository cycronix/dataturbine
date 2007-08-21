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
 * Server-side representation of a shortcut connection from one server to
 * another.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.PeerServer
 * @see com.rbnb.api.Shortcut
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 01/03/2002  INB	Created.
 *
 */
interface ShortcutHandler
    extends com.rbnb.api.GetLogInterface,
	    com.rbnb.api.GetServerHandlerInterface,
	    com.rbnb.api.NotificationFrom,
	    com.rbnb.api.PathFinder,
	    com.rbnb.api.RegisteredInterface,
	    com.rbnb.api.RoutedTarget,
	    com.rbnb.api.ShortcutInterface
{
    /**
     * Cost of a <code>PASSIVE</code> connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2002
     */
    public final static double PASSIVE_COST = Double.MAX_VALUE/1000000.;

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
     * Reversed a route connection.
     * <p>
     * This method is called whenever an attempt is made to create a reverse
     * connection from the child to the parent on the parent's behalf or by a
     * peer on the other side of a shortcut and the other side succeeds.  The
     * input <code>PeerServer</code> object describes what has been reversed,
     * while the <code>RCO</code> represents the connection.
     * <p>
     * This method is responsible for creating a <code>Router</code> on this
     * end using an <code>ACO</code> built from the provided <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI     the <code>RCO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    public abstract void reversed(RCO rcoI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Starts this <code>ShortcutHandler</code>.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #stop()
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public abstract void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Stops this <code>ShortcutHandler</code>.
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
     * @see #start()
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public abstract void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}

