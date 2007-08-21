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
 * Object that represents a plugin client application connection to an RBNB
 * server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 06/11/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/25/2005  EMF      Added bytesTransferred method; does nothing.
 * 06/11/2003  INB	Added <code>RequestOptions</code> handling.
 * 01/14/2002  INB	Created.
 *
 */
class PlugInIO
    extends com.rbnb.api.ClientIO
    implements com.rbnb.api.PlugIn
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
    PlugInIO() {
	super();
    }

    /**
     * Class constructor to build a <code>PlugInIO</code> by reading it in.
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
    PlugInIO(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>PlugInIO</code> by reading it in.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>PlugInIO</code> as an
     *		     <code>Rmap</code>.
     * @param isI    the <code>InputStream</code>.
     * @param disI   the <code>DataInputStream</code>.
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
    PlugInIO(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Class constructor to build a <code>PlugInIO</code> from a name.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
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
    PlugInIO(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
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
      return 0L;
    }

    /**
     * Retrieves the next request from the <code>Server</code>.
     *
     * @author Ian Brown
     *
     * @param timeOutI  timeout in milliseconds.
     * @return the <code>Object</code> request.
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
    public Object fetch(long timeOutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (null);
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
    public void fillRequestOptions(RequestOptions roO) {
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
     * @see #reRegister(com.rbnb.api.Rmap)
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
    public void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
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
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    public void reRegister(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
    }
}
