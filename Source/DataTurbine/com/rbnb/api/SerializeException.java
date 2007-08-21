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
 * Serialization exception class.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/10/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/08/2001  INB	Created.
 *
 *
 */
public class SerializeException
    extends com.rbnb.api.RBNBException
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #SerializeException(String)
     * @since V2.0
     * @version 01/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public SerializeException() {
	super();
    }

    /**
     * Class constructor to build a <code>SerializeException</code> for the
     * input detail message.
     * <p>
     *
     * @author Ian Brown
     *
     * #param messageI  the detail message.
     * @see #SerializeException()
     * @since V2.0
     * @version 01/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public SerializeException(String messageI) {
	super(messageI);
    }
}
