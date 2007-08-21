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
 * Disconnected client exception class.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.2
 * @version 02/16/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/16/2004  INB	Created.
 *
 */
public final class DisconnectedClientException
    extends com.rbnb.api.AddressException
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #DisconnectedClientException(String)
     * @since V2.2
     * @version 02/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/16/2004  INB	Created.
     *
     */
    public DisconnectedClientException() {
	super();
    }

    /**
     * Class constructor to build a <code>DisconnectedClientException</code> for the
     * input detail message.
     * <p>
     *
     * @author Ian Brown
     *
     * #param messageI  the detail message.
     * @see #DisconnectedClientException()
     * @since V2.2
     * @version 02/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/16/2004  INB	Created.
     *
     */
    public DisconnectedClientException(String messageI) {
	super(messageI);
    }
}
