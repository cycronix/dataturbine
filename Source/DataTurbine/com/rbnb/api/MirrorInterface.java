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
 * Common representation of a mirror of data from one RBNB server to another.
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
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/10/2001  INB	Created.
 *
 */
interface MirrorInterface
    extends java.lang.Cloneable,
	    java.io.Serializable
{
    /**
     * pull data from the remote <code>Server</code> to the local
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #PUSH
     * @since V2.0
     * @version 04/17/2001
     */
    public final static byte PULL = 0;

    /**
     * push data from the local <code>Server</code> to the remote
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #PULL
     * @since V2.0
     * @version 04/17/2001
     */
    public final static byte PUSH = 1;

    /**
     * Clones this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @see java.lang.Cloneable
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public abstract Object clone();

    /**
     * Gets the direction of the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the direction of the mirror.
     * @see #PULL
     * @see #PUSH
     * @see #setDirection(byte)
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract byte getDirection();

    /**
     * Gets the remote <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the remote <code>Server</code>.
     * @see #setRemote(com.rbnb.api.Server)
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract Server getRemote();

    /**
     * Gets the <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>DataRequest</code>.
     * @see #setRequest(com.rbnb.api.DataRequest)
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract DataRequest getRequest();

    /**
     * Gets the <code>Source</code> object that is the destination of the
     * mirrored data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Source</code>.
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract Source getSource();

    /**
     * Sets the direction of the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @param directionI  the direction of the mirror. This can be one of:
     *			  <br><ul>
     *			  <li><code>PULL</code> - get data from the remote
     *						  <code>Server</code>, or</li>
     *			  <li><code>PUSH</code> - send data to the remote
     *						  <code>Server</code>.</li>
     *			  </ul>
     * @see #getDirection()
     * @see #PULL
     * @see #PUSH
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract void setDirection(byte directionI);

    /**
     * Sets the remote <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param remoteI  the remote <code>Server</code>.
     * @see #getRemote()
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract void setRemote(Server remoteI);

    /**
     * Sets the <code>DataRequest</code>.
     * <p>
     * The <code>DataRequest</code> must be one that generates data with
     * monotonically increasing time-stamps and must refer to only a single
     * <code>Source</code> on a single <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>DataRequest</code>.
     * @see #getRequest()
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public abstract void setRequest(DataRequest requestI);
}
