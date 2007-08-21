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
 * <code>RemoteClient</code> owning interface.
 * <p>
 * Classes implementing this interface can be the "owners" of a
 * <code>RemoteClient</code> object. Owning objects are notified of the
 * termination of a <code>RemoteClient</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RemoteClient
 * @since V2.0
 * @version 12/17/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/17/2001  INB	Created.
 *
 */
interface RemoteClientOwner {

    /**
     * Gets the <code>RemoteClient</code> owned by this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RemoteClient</code>.
     * @see #setRemoteClient(com.rbnb.api.RemoteClient)
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    public abstract RemoteClient getRemoteClient();

    /**
     * The <code>RemoteClient</code> for this object has terminated.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eI the <code>Exception</code> (if any) that terminated this.
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    public abstract void remoteClientTerminated(java.lang.Exception eI);

    /**
     * Sets the <code>RemoteClient</code> owned by this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rClientI the <code>RemoteClient</code>.
     * @see #getRemoteClient()
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2001  INB	Created.
     *
     */
    public abstract void setRemoteClient(RemoteClient rClientI);
}
