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
 * Client-side representation of a sink client application connection to the
 * RBNB server.
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
 * 06/11/2003  INB	Added <code>sendRequestOptions</code>.
 * 05/09/2001  INB	Created.
 *
 */
public interface Sink
    extends com.rbnb.api.SinkInterface,
	    com.rbnb.api.Source
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
    public abstract Rmap fetch(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
    public abstract void sendRequestOptions(RequestOptions roI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
