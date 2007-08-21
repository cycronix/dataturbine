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
 * The hierarchical server class.
 * <p>
 * A hierarchical server is either a parent or a child of the local
 * <bold>RBNB</bold>. The local <bold>RBNB</bold> can communicate directly with
 * a parent or child.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 02/27/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/26/2001  INB	Created.
 *
 */
abstract class HierarchicalServer
    extends com.rbnb.api.PeerServer
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/26/2001  INB	Created.
     *
     */
    HierarchicalServer() {
	super();
    }

    /**
     * Class constructor to build a <code>HierarchicalServer</code> for a name
     * and an address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
     * @param addressI the server's address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address provided.
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
     * @version 11/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/27/2001  INB	Created.
     *
     */
    HierarchicalServer(String nameI,String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI,addressI);
    }
}
