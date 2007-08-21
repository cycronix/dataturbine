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
 * Client-side representation of a plugin client application connection to the
 * RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/25/2005  EMF      Added bytesTransferred method.
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 06/11/2003  INB	Added <code>fillRequestOptions</code>.
 * 01/14/2002  INB	Created.
 *
 */
public interface PlugIn
    extends com.rbnb.api.Client,
	    com.rbnb.api.PlugInInterface
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
    public abstract void fillRequestOptions(RequestOptions roO);

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF	Created.
     *
     */
    public abstract long bytesTransferred();

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
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    public abstract Object fetch(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
