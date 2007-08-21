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
 * Extended <code>StreamRemoteListener</code> that listens for
 * <code>ShortcutHandler</code> "events" for a
 * <code>StreamRequestHandler</code>.
 * <p>
 * There are no actual "events" that occur for <code>ShortcutHandlers</code>,
 * except for their termination. However, the <code>ShortcutHandler</code>
 * actually represents a remote server, just like a <code>RemoteServer</code>
 * object usually does, so we can get stuff from that remote.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.ShortcutHandler
 * @since V2.0
 * @version 02/11/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/04/2002  INB	Created.
 *
 */
final class StreamShortcutListener
    extends StreamRemoteListener
{

    /**
     * Class constructor to build a <code>StreamShortcutListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>NotificationFrom</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>NotificationFrom</code> source.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
     *
     */
    StreamShortcutListener(StreamParent parentI,
			 Rmap requestI,
			 NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
    }

    /**
     * Class constructor to build a <code>StreamShortcutListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>ShortcutHandler</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>ShortcutHandler</code> source.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2002  INB	Created.
     *
     */
    StreamShortcutListener(StreamParent parentI,
			   Rmap requestI,
			   ShortcutHandler sourceI)
	throws java.lang.InterruptedException
    {
	this(parentI,requestI,(NotificationFrom) sourceI);
    }
}
