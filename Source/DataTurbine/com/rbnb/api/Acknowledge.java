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
 * Acknowledgement of receipt.
 * <p>
 * Requests for acknowledgement (see <code>RSVP</code>) are used when
 * performing monitor mode requests (a streaming request repeatedly asking for
 * the newest, allowing for gaps - see <code>NBO.initiateRequestAt</code> for
 * documentation on requests).  Each time a matching frame is retrieved by a
 * listener (see <code>StreamListener</code>) from a plugin or an RBO, the
 * listener wraps it in a request for acknowledgement.  The listener then waits
 * for the acknowledgement before retrieving another frame.
 * <p>
 * The request for acknowledgement is forwarded to the client application's
 * API (across routing if necessary).  When the client uses the API to read the
 * frame and its request for acknowledgement, the API responds with an
 * <code>Acknowledge</code> indicating receipt of that frame.  The
 * <code>Acknowledge</code> is passed back to the appropriate listener.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.NBO#initiateRequestAt(int indexI)
 * @see com.rbnb.api.RSVP
 * @see com.rbnb.api.StreamListener
 * @since V2.0
 * @version 08/13/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/13/2004  INB	Added documentation.
 * 06/07/2001  INB	Created.
 *
 */
final class Acknowledge
    extends com.rbnb.api.RSVP
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    Acknowledge() {
	super();
    }

    /**
     * Class constructor to build an <code>Acknowledge</code> for a particular
     * <code>identification</code> and <code>Serializable</code>.
     * <p>>
     *
     * @author Ian Brown
     *
     * @param identificationI identification of the object making the request.
     * @param serializableI   the <code>Serializable</code>.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    Acknowledge(long identificationI,Serializable serializableI) {
	super(identificationI,serializableI);
    }

    /**
     * Class constructor to build a <code>Acknowledge</code> by deserializing it.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the input stream.
     * @param disI the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    Acknowledge(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(isI,disI);
    }
}
