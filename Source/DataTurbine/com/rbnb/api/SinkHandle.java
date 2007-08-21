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
 * Client-side object that represents a client sink application connection to
 * an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 06/11/2003
 */

/*
 * Copyright 2001, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/25/2005  EMF      Added bytesTransferred method.
 * 06/11/2003  INB	Added <code>sendRequestOptions</code>.
 * 05/11/2001  INB	Created.
 *
 */
final class SinkHandle
    extends com.rbnb.api.SourceHandle
    implements com.rbnb.api.Sink
{
    /**
     * Is there an active request?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/30/2001
     */
    private boolean activeRequest = false;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    SinkHandle() {
	super();
	try {
	    setCframes(1);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	}
    }

    /**
     * Class constructor to build a <code>SinkHandle</code> by reading it in.
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
    SinkHandle(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>SinkHandle</code> from a name.
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
    SinkHandle(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
	setCframes(1);
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


Rmap lastRequest = null;

    /**
     * Adds a child <code>Rmap</code> to this <code>SinkHandle</code>.
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
     * @version 10/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2000  INB	Created.
     *
     */
    public final synchronized void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {

if (true) {
    lastRequest = (Rmap) childI.clone();
}

	// The following check is only necessary until the Rmap request
	// matching logic handles inheritance of unmatched data times as well
	// as unmatched request times.
	if ((childI.getTrange() == null) &&
	    (childI.getFrange() == null)) {
	    boolean hasTime = false;
	    for (int idx = 0, eIdx = childI.getNmembers();
		 (idx < eIdx) && !hasTime;
		 ++idx) {
		Rmap member = childI.getMemberAt(idx);
		hasTime = ((member.getTrange() != null) ||
			   (member.getFrange() != null));
	    }
	    for (int idx = 0, eIdx = childI.getNchildren();
		 (idx < eIdx) && !hasTime;
		 ++idx) {
		Rmap child = childI.getChildAt(idx);
		hasTime = ((child.getTrange() != null) ||
			   (child.getFrange() != null));
	    }

	    if (!hasTime) {
		throw new com.rbnb.api.AddressException
		    (childI +
		     " does not contain a time at the top of the hierarchy.");
	    }
	}

	super.addChild(childI);
    }

    /**
     * Retrieves the next response from the <code>Server</code>.
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
     * @return the <code>Rmap</code> response:
     *	       <br><ul>
     *	       <li>null is returned if the timeout period is exceeded before a
     *		   response is seen,</li>
     *	       <li>an <code>EndOfStream</code> object is returned if the
     *		   request ends normally, or</li>
     *	       <li>an <code>Rmap</code> containing the response is
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
     * @see com.rbnb.api.EndOfStream
     * @see com.rbnb.api.Rmap
     * @see #initiateRequestAt(int)
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/13/2001  INB	Created.
     *
     */
    public final synchronized Rmap fetch(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!getActiveRequest()) {
	    throw new java.lang.IllegalStateException
		("Need an active request to be able to fetch data.");
	}

	boolean exception = false;
	Rmap result = null;
	try {
	    result = (Rmap) getACO().fetch(timeOutI);

	} catch (java.lang.Exception e) {
	    exception = true;
	    Language.throwException(e);

	} finally {
	    if (exception) {
		setActiveRequest(false);
	    } else if ((result != null) && (result instanceof EndOfStream)) {
		setActiveRequest(false);
	    }
	}

	return (result);
    }

    /**
     * Gets the active request flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is there an active request?
     * @see #setActiveRequest(boolean)
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/30/2001  INB	Created.
     *
     */
    private final boolean getActiveRequest() {
	return (activeRequest);
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
    public final synchronized Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronizeWserver();
	return (super.getRegistered(requestI));
    }

    /**
     * Initiates a request for the child at the specified index.
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
     * @return the <code>Rmap</code> response:
     *	       <br><ul>
     *	       <li>null is returned if the timeout period is exceeded before a
     *		   response is seen,</li>
     *	       <li>an <code>EndOfStream</code> object is returned if the
     *		   request ends normally, or</li>
     *	       <li>an <code>Rmap</code> containing the response is
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 07/31/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/13/2001  INB	Created.
     *
     */
    public final synchronized void initiateRequestAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,	       
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    if (getActiveRequest()) {
		fetch(FOREVER);
		if (getActiveRequest()) {
		    throw new java.lang.IllegalStateException
			("Cannot initiate a new request once a continuous " +
			 "request (monitor or subscribe) has been started.\n" +
			 "Please disconnect and start a new sink connection.");
		}
	    }

	    setActiveRequest(true);
	    getACO().initiateRequestAt(indexI);
	} catch (java.lang.Exception e) {
	    setActiveRequest(false);
	    Language.throwException(e);
	}
    }

    /**
     * Creates a new instance of the same class as this
     * <code>SinkHandle</code> (or a similar class).
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
     * @version 08/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (SinkIO.newInstance(this));
    }

    /**
     * Sends a <code>RequestOptions</code> object to the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param roI the <code>RequestOptions</code> object.
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
    public final void sendRequestOptions(RequestOptions roI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getACO().send(roI);
    }

    /**
     * Sets the active request flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param activeRequestI is a request active?
     * @see #getActiveRequest()
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/30/2001  INB	Created.
     *
     */
    private final void setActiveRequest(boolean activeRequestI) {
	activeRequest = activeRequestI;
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
     * 05/30/2001  INB	Created.
     *
     */
    public final synchronized void synchronizeWserver()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	setActiveRequest(false);
	super.synchronizeWserver();
    }
}
