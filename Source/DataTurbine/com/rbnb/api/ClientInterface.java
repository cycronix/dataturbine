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
 * Common representation of a client application connection to an
 * <bold>RBNB</bold> server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 09/28/2004  JPW	Declare the interface public; otherwise, J# has
 *			compile-time errors.
 * 04/17/2003  INB	Added <code>tryReconnect</code> method.
 * 05/09/2001  INB	Created.
 *
 */
public interface ClientInterface
    extends com.rbnb.api.RegisteredInterface,
	    com.rbnb.api.RmapInterface,
	    com.rbnb.api.UsernameInterface
{
    /**
     * general-purpose client connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #MIRROR
     * @see #PLUGIN
     * @since V2.0
     * @version 06/01/2001
     */
    public final static byte CLIENT = 0;

    /**
     * mirror connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #CLIENT
     * @see #PLUGIN
     * @since V2.0
     * @version 06/01/2001
     */
    public final static byte MIRROR = 1;

    /**
     * plug-in connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #CLIENT
     * @see #MIRROR
     * @since V2.0
     * @version 06/01/2001
     */
    public final static byte PLUGIN = 2;

    /**
     * wait until a response appears (block forever).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    public final static long FOREVER = Long.MIN_VALUE;

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
     * Gets the remote identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the remote identification.
     * @see #setRemoteID(String)
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    public abstract String getRemoteID();

    /**
     * Gets the type of client connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the client type.
     * @see #CLIENT
     * @see #MIRROR
     * @see #PLUGIN
     * @see #setType(byte)
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    public abstract byte getType();

    /**
     * Is this <code>ClientInterface</code> running?
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
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public abstract boolean isRunning()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the remote identification.
     * <p>
     * The remote identification specifies the fully-qualified name of the
     * remote RBNB object with which this RBNB object is communicating, if
     * any.
     * <p>
     *
     * @author Ian Brown
     *
     * @param remoteIDI the remote identification.
     * @see #getRemoteID()
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    public abstract void setRemoteID(String remoteIDI);

    /**
     * Sets the type of client connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI the type of client connection.
     * @see #CLIENT
     * @see #getType()
     * @see #MIRROR
     * @see #PLUGIN
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    public abstract void setType(byte typeI);

    /**
     * Try to reconnect to an existing <code>ClientHandler</code>?
     * <p>
     * If this method returns <code>true</code>, then the server will look for
     * an existing <code>ClientHandler</code> when the client represented by
     * the <code>ClientInterface</code> logs in. If a match is found, then the
     * server will try to reconnect to that existing handler rather than start
     * a new one.
     * <p>
     *
     * @author Ian Brown
     *
     * @return try to reconnect?
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
    public abstract boolean tryReconnect();
}
