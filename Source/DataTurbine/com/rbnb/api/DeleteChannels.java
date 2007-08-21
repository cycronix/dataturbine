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
 * Delete channels from a source.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.2
 * @version 07/30/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/30/2003  INB	Created.
 *
 *
 */
final class DeleteChannels
    extends com.rbnb.api.Command
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    public DeleteChannels() {
	super();
    }

    /**
     * Class constructor to build a <code>DeleteChannels</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
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
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    DeleteChannels(InputStream isI,DataInputStream disI)
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
     * Creates a <code>DeleteChannels</code> command for the specified
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the <code>Rmap</code> hierarchy to delete.
     * 
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    DeleteChannels(com.rbnb.api.Rmap rmapI) {
	super(rmapI);
    }

    /**
     * Writes the object to an output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the object's parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
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
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    final void write(String[] parametersI,
			int parameterI,
			OutputStream osI,
			DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (IsSupported.isSupported(IsSupported.FEATURE_REQUEST_OPTIONS,
				    osI.getBuildVersion(),
				    osI.getBuildDate())) {
	    super.write(parametersI,parameterI,osI,dosI);
	}
    }
}
