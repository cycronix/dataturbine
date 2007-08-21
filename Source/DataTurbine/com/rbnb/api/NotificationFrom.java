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
 * An extended <code>NotificationHandler</code> that serves as the source of
 * notification events.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.NotificationTo
 * @since V2.0
 * @version 01/18/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/26/2001  INB	Created.
 *
 */
interface NotificationFrom
    extends com.rbnb.api.NotificationHandler
{

    /**
     * Notifies all objects awaiting notification of the arrival of an "event"
     * <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the "event" <code>Serializable</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the post operation is interrupted.
     * @since V2.0
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    public abstract void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
