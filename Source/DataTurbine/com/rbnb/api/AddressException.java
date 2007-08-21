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
 * Extended <code>RBNBException</code> for addressing problems.
 * <p>
 * Exceptions of this type are thrown whenever there is a problem with an
 * address within the RBNB server.  An address is the qualified name of an Rmap
 * that is supposed to be in the server hierarchy.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/13/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/13/2004  INB	Added documentation.
 * 01/09/2001  INB	Created.
 *
 *
 */
public class AddressException
    extends com.rbnb.api.RBNBException
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #AddressException(String)
     * @since V2.0
     * @version 01/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    public AddressException() {
	super();
    }

    /**
     * Class constructor to build a <code>AddressException</code> for the
     * input detail message.
     * <p>
     *
     * @author Ian Brown
     *
     * #param messageI  the detail message.
     * @see #AddressException()
     * @since V2.0
     * @version 01/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    public AddressException(String messageI) {
	super(messageI);
    }
}
