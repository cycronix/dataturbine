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
 * Path finding interface.
 * <p>
 * Classes implementing this interface are capable of finding a
 * <code>Path</code> from the local <bold>RBNB</bold> server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V1.2
 * @version 01/02/2002
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/20/2001  INB	Created.
 *
 */
interface PathFinder {

    /**
     * Finds a <code>Path</code> from the local <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Path</code> found.
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
     * @version 11/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2001  INB	Created.
     *
     */
    public abstract Path findPath()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
