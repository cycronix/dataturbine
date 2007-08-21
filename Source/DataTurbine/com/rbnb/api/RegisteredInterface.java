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
 * Interface for getting registration information.
 * <p>
 * Classes implementing this interface support the
 * <code>getRegistered(com.rbnb.api.Rmap)</code> call.
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/28/2004
 */

/*
 * Copyright 2001, 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/28/2004  JPW	Declare this interface public; otherwise, J# has
 *			compile time errors.
 * 11/28/2001  INB	Created.
 *
 */
public interface RegisteredInterface {

    /**
     * Gets the registration list for this object matching the input hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return the matching registration information.
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
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/28/2001  INB	Created.
     *
     */
    public abstract Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
