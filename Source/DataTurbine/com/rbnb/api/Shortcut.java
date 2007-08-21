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
 * Client-side representation of a shortcut connection from one server to
 * another.
 * <p>
 * Shortcuts represent a connection from one server to another that allow
 * requests and data to flow from the first server to the second. To get data
 * responses to come back or to allow requests to be made from the second to
 * the first, there must be a corresponding shortcut from the second to the
 * first.
 * <p>
 * Shortcuts can also be linked together to form a path from one server via a
 * second to a third and so on. The responses to requests do not need to follow
 * the same path in reverse, but instead may follow a completely different
 * path, so long as the data gets from the source to the destination server.
 * <p>
 * For example:
 * <p>
 * If
 * <br>server A has a shortcut to server B,
 * <br>server B has a shortcut to server C, and
 * <br>server C has a shortcut to server A,
 * <br>then server A can make a request to get data from server C via server B,
 * while the response from server C goes directly to server A.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RBNBShortcut
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 08/05/2004  INB	Added link to RBNBShortcut.
 * 01/03/2002  INB	Created.
 *
 */
public interface Shortcut
    extends com.rbnb.api.ShortcutInterface
{
    /**
     * Clones this object.
     * <p>
     * This same abstract declaration is also included in RmapInterface.java,
     * but for some unknown reason J# gives a compiler error if it is not also
     * included here.
     *
     * @author John Wilson
     *
     * @return the clone.
     * @see java.lang.Cloneable
     * @since V2.5
     * @version 09/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/29/2004  JPW	Created.
     *
     */
    public abstract Object clone();
}

