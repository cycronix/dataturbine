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
 * Client-side object that represents a plugin client application connection to
 * an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/14/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/25/2005  EMF      Added bytesTransferred method.
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 06/11/2003  INB	Added <code>RequestOptions</code> handling.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 01/14/2002  INB	Created.
 *
 */
final class PlugInHandle
    extends com.rbnb.api.ClientHandle
    implements com.rbnb.api.PlugIn
{
    /**
     * read synchronization object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2002
     */
    private Door readLock = null;

    /**
     * the <code>RequestOptions</code> object read from the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/11/2003
     */
    private RequestOptions ro = null;

    /**
     * write synchronization object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2002
     */
    private Door writeLock = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    PlugInHandle() {
	super();
	try {
	    readLock = new Door(Door.STANDARD);
	    writeLock = new Door(Door.STANDARD);
	} catch (java.lang.InterruptedException e) {
	}
    }

    /**
     * Class constructor to build a <code>PlugInHandle</code> by reading it
     * in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
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
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    PlugInHandle(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>PlugInHandle</code> from a name.
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
     * @version 11/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    PlugInHandle(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
	try {
	    readLock = new Door(Door.STANDARD);
	    writeLock = new Door(Door.STANDARD);
	} catch (java.lang.InterruptedException e) {
	}
    }

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF	Created.
     *
     */
    public long bytesTransferred() {
      return getACO().bytesTransferred();
    }

    /**
     * Adds a child <code>Rmap</code> to this <code>PlugInHandle</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the new child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			location to the <code>Lock</code> and eliminated 
     *			unnecessary synchronization.
     * 01/14/2002  INB	Created.
     *
     */
    public final void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    writeLock.setIdentification(getFullName() + "_handle_write");
	    writeLock.lock("PlugInHandle.addChild");

	    getACO().addChild(childI);
	} finally {
	    writeLock.unlock();
	    synchronized (this) {
		notify();
	    }
	}
    }

    /**
     * Retrieves the next request from the <code>Server</code>.
     * <p>
     * The request can either be an <code>Rmap</code> hierarchy or an
     * <code>Ask</code> object.  In the latter case, the <code>Ask</code>
     * object specifies that it is looking for registration information and the
     * additional information is an <code>Rmap</code> hierarchy.
     * <p>
     * The <code>Rmap</code> hierarchy of the request returned consists of a
     * top-level <code>DataRequest</code> object with a name and time/frame
     * information that identifies which request this is. This information
     * should be copied to an <code>Rmap</code> at the top of the response
     * hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI  timeout in milliseconds.
     *			<br><ul>
     *			<li>0 means return a response only if one is
     *			    ready,</li>
     *			<li><code>FOREVER</code> means wait for a response to
     *			    show up, or</li>
     *			<li>anything else means wait for a response to show up
     *			    or for the timeout period to elapse.</li>
     *			</ul>
     * @return the request as an <code>Object</code>:
     *	       <br><ul>
     *	       <li>null is returned if the timeout period is exceeded before a
     *		   request is seen,</li>
     *	       <li>an <code>EndOfStream</code> object is returned if the
     *		   request ends normally,</li>
     *	       <li>an <code>Ask</code> object requesting registration, or</li>
     *	       <li>an <code>Rmap</code> containing the request is
     *		   returned.</li>
     *	       </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is no active request.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see com.rbnb.api.DataRequest
     * @see com.rbnb.api.Rmap
     * @see #addChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			location to the <code>Lock</code> and eliminated 
     *			unnecessary synchronization.
     * 06/11/2003  INB	Handle reception of a <code>RequestOptions</code>
     *			object by copying it for later and then reading the
     *			next response.
     * 01/14/2002  INB	Created.
     *
     */
    public final Object fetch(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Object requestR = null;
	
	try {
	    readLock.setIdentification(getFullName() + "_handle_read");
	    readLock.lock("PlugInHandle.fetch");
	    do {
		requestR = getACO().fetch(timeOutI);
		if (requestR instanceof RequestOptions) {
		    ro = (RequestOptions) requestR;
		}
	    } while ((requestR != null) &&
		     (requestR instanceof RequestOptions));

	} finally {
	    readLock.unlock();
	    synchronized (this) {
		notify();
	    }
	}

	return (requestR);
    }

    /**
     * Fills the input <code>RequestOptions</code> object with values read from
     * the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param roO the <code>RequestOptions</code> to fill.
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    public final void fillRequestOptions(RequestOptions roO) {
	if (ro != null) {
	    roO.copy(ro);
	    ro = null;
	}
    }

    /**
     * Creates a new instance of the same class as this
     * <code>PlugInHandle</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (PlugInIO.newInstance(this));
    }

    /**
     * Updates the registration for this <code>PlugIn</code>.
     * <p>
     * The input <code>Rmap</code> hierarchy is used to update the registration
     * for this <code>PlugIn</code>.  The hierarchy may contain
     * <code>DataBlocks</code> and time information.  That information is
     * copied into the appropriate locations in the registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the registration <code>Rmap</code> hierarchy.
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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			location to the <code>Lock</code> and eliminated 
     *			unnecessary synchronization.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 01/14/2002  INB	Created.
     *
     */
    public final void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    readLock.setIdentification(getFullName() + "_handle_read");
	    writeLock.setIdentification(getFullName() + "_handle_write");
	    synchronized (this) {
		while (readLock.isLocked() || writeLock.isLocked()) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}
		readLock.lock("PlugInHandle.register");
		writeLock.lock("PlugInHandle.register");
	    }
	    getACO().register(rmapI);
	} finally {
	    writeLock.unlock();
	    readLock.unlock();
	    synchronized (this) {
		notify();
	    }
	}
    }

    /**
     * Replace the entire registration map for this <code>PlugIn</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the new registration map.
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
     * @see #register(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			location to the <code>Lock</code> and eliminated 
     *			unnecessary synchronization.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 01/21/2002  INB	Created.
     *
     */
    public final void reRegister(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    readLock.setIdentification(getFullName() + "_handle_read");
	    writeLock.setIdentification(getFullName() + "_handle_write");
	    synchronized (this) {
		while (readLock.isLocked() || writeLock.isLocked()) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}
		readLock.lock("PlugInHandle.reRegister");
		writeLock.lock("PlugInHandle.reRegister");
	    }
	    getACO().reRegister(rmapI);
	} finally {
	    writeLock.unlock();
	    readLock.unlock();
	    synchronized (this) {
		notify();
	    }
	}
    }
}
