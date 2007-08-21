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
 * Client-side representation of a client application connection to the RBNB
 * server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 05/09/2001  INB	Created.
 *
 */
public interface Client
    extends com.rbnb.api.ClientInterface
{

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
     * Is this <code>Client</code> synchronized with the server?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the client synchronized?
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2002  INB	Created.
     *
     */
    public abstract boolean isSynchronized();

    /**
     * Starts this <code>Client</code>.
     * <p>
     * This method ensures that the name of this object and the name of the
     * <code>Server</code> that is running this <code>Client</code> are set
     * correctly.
     * <p>
     * The <code>Client's</code> parent <code>Server</code> object's name is
     * set equal to the name of the RBNB server that is running the handler for
     * this <code>Client</code>.
     * <p>
     * The name of the <code>Client</code> object is set equal to the name
     * given to the handler in the RBNB server. This name is one of:
     * <p><ol>
     * <li>the name provided for the <code>Client</code> object originally,<li>
     * <li>the name provided plus the addition of "_#", where # is a unique
     *     number within the RBNB server, or</li>
     * <li>"_#", where # is a unique number within the RBNB server.</li>
     * </ol><p>
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
     *		  <li>the <code>Client</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>Client</code> is already running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #stop()
     * @since V2.0
     * @version 10/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
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
    public abstract void start(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    public abstract void start(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Stops this <code>Client</code>.
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
     *		  <li>the <code>Client</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>Client</code> is already running.</li>
     *		  </ul><p>
     * @see #start()
     * @since V2.0
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public abstract void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
    public abstract void stop(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public abstract void stop(Server serverI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Stops the local <code>Shortcut</code>.
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
    public abstract void stop(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     *		  <li>the <code>Client</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>Client</code> is not running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the synchronization is interrupted.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/22/2000  INB	Created.
     *
     */
    public abstract void synchronizeWserver()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
