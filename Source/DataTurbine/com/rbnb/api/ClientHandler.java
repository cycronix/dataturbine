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
 * Server-side representation of a client application connection to the RBNB
 * server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 11/12/2003  INB	Added <code>get/setInStartUp</code>.
 * 04/17/2003  INB	Added <code>reconnect</code>,
 *			</code>allowAccess(Username)</code>, and
 *			<code>allowReconnect</code> methods.
 * 02/18/2003  INB	Added <code>getTerminateRequested</code> and
 *			<code>setTerminateRequested</code>.
 * 05/09/2001  INB	Created.
 *
 */
interface ClientHandler
    extends com.rbnb.api.ClientInterface,
	    com.rbnb.api.GetLogInterface,
	    com.rbnb.api.GetServerHandlerInterface,
	    com.rbnb.api.Interruptable,
	    com.rbnb.api.IOMetricsInterface,
	    com.rbnb.api.LogStatusInterface
{

    /**
     * Allow the specified <code>ClientHandler</code> access to this one?
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>ClientHandler</code>.
     * @return is the other <code>ClientHandler</code> allowed access?
     * @since V2.0
     * @version 01/15/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2003  INB	Created.
     *
     */
    public abstract boolean allowAccess(ClientHandler otherI);

    /**
     * Allow the specified <code>Username</code> access to this
     * <code>ClientHandler</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @return is the <code>Username</code> allowed access?
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
    public abstract boolean allowAccess(Username usernameI);

    /**
     * Allow reconnects to happen?
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code> trying to reconnect.
     * @return are reconnects allowed?
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
    public abstract boolean allowReconnect(Username usernameI);

    /**
     * Assigns a new connection to this <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI             the <code>RCO</code> that was handling the
     *			       connection.
     * @param clientInterfaceI the <code>ClientInterface</code> object
     *			       identifying the connection.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    public abstract void assignConnection(RCO rcoI,
					  ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
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
     * Handles an <code>ExceptionMessage</code> received from the client.
     * <p>
     *
     * @author Ian Brown
     *
     * @param emsgI the <code>ExceptionMessage</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 04/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/23/2002  INB	Created.
     *
     */
    public abstract void exception(ExceptionMessage emsgI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the child at the specified index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the child to retrieve.
     * @return the child <code>Rmap</code>.
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
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    public abstract Rmap getChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the is in startup flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>ClientHandler</code> starting up?
     * @see #setIsInStartup(boolean)
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    public abstract boolean getIsInStartup();

    /**
     * Gets the log class mask for this <code>ClientHandler</code>.
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
    public abstract long getLogClass();

    /**
     * Gets the base log level for this <code>ClientHandler</code>.
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
    public abstract byte getLogLevel();

    /**
     * Gets the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RCO</code>.
     * @see #setRCO(com.rbnb.api.RCO)
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
    public abstract RCO getRCO();

    /**
     * Gets the role(s) needed to access this <code>ClientHandler</code>.
     * <p>
     * To access this <code>ClientHandler</code>, the accessing
     * <code>ClientHandler</code> must have one or more of the roles listed.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the roles required.
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public abstract String[] getRoles();

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return has a stop been requested?
     * @see #setTerminateRequested(boolean)
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created.
     *
     */
    public abstract boolean getTerminateRequested();

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
    public abstract Thread getThread();

    /**
     * Is this <code>ClientHandler</code> new?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>ClientHandler</code> new?
     * @since V2.0
     * @version 10/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    public abstract boolean isNew();

    /**
     * Reconnect to an existing <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the new <code>ClientInterface</code>.
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
     *		  <code>RBO</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
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
    public abstract void reconnect(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the "am new" flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param amNewI is this <code>RBNBClient</code> new?
     * @see #isNew()
     * @since V2.0
     * @version 06/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/24/2002  INB	Created.
     *
     */
    public abstract void setAmNew(boolean amNewI);

    /**
     * Sets the is in startup flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isInStartupI is this <code>ClientHandler</code> starting up?
     * @see #getIsInStartup()
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    public abstract void setIsInStartup(boolean isInStartupI);

    /**
     * Sets the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code>.
     * @see #getRCO()
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
    public abstract void setRCO(RCO rcoI);

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stopI  stop this <code>ClientHandler</code>?
     * @see #getTerminateRequested()
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created.
     *
     */
    public abstract void setTerminateRequested(boolean stopI);

    /**
     * Starts this <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> starting us.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #stop(com.rbnb.api.ClientHandler)
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
    public abstract void start(ClientHandler clientI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Stops this <code>ClientHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientHandler</code> stopping us.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #start(com.rbnb.api.ClientHandler)
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
    public abstract void stop(ClientHandler clientI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Synchronizes with the client application.
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
    public abstract void synchronizeWclient()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Updates this <code>ClientHandler</code> from the specified
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the <code>ClientInterface</code> to use.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
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
     * 05/10/2001  INB	Created.
     *
     */
    public abstract void update(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
