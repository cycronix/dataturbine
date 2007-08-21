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
 * Interface for working with a peer.
 * <p>
 * <code>Server</code> subclasses implementing this interface provide
 * <code>Shortcuts</code> to other <code>Servers</code> (peers) or are the
 * destination of <code>Shortcuts</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Shortcut
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
 * 01/08/2002  INB	Created.
 *
 */
interface PeerInterface
    extends com.rbnb.api.ServerInterface
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

    /**
     * Finds a <code>Shortcut</code> to the specified <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerI the <code>PeerServer</code>.
     * @return the <code>Shortcut</code> or null.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public abstract Shortcut findShortcut(PeerServer peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the list of <code>Shortcuts</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list.
     * @see #setShortcuts(java.util.Vector)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public abstract java.util.Vector getShortcuts();

    /**
     * Sets the list of <code>Shortcuts</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutsI the list.
     * @see #getShortcuts()
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public abstract void setShortcuts(java.util.Vector shortcutsI);
}
