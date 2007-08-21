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
 * Interface describing an object that is a parent of a
 * <code>StreamListener</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.StreamListener
 * @since V2.0
 * @version 06/07/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/27/2001  INB	Created.
 *
 */
interface StreamParent {

    /**
     * Adds a <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param listenerI  the <code>StreamListener</code>.
     * @see #removeListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    public abstract void addListener(StreamListener listenerI);

    /**
     * Gets the <code>ServerHandler</code> object hosting the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    public abstract ServerHandler getServer();

    /**
     * Posts a response <code>Serializable</code> to the application.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/29/2001  INB	Created.
     *
     */
    public abstract void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Removes a <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param listenerI  the <code>StreamListener</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #addListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2001  INB	Created.
     *
     */
    public abstract void removeListener(StreamListener listenerI)
	throws java.lang.InterruptedException;
}
