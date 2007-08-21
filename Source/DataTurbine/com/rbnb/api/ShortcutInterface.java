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
 * Common representation of a shortcut connection from one server to another.
 * <p>
 *
 * @author Ian Brown
 *
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
 * 01/03/2002  INB	Created.
 *
 */
interface ShortcutInterface
    extends com.rbnb.api.RmapInterface
{
    /**
     * <code>Shortcut</code> is active.
     * <p>
     * <code>Shortcut</code> that in this state are fully enabled. They can be
     * used to pass both requests and responses to those requests.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #PASSIVE
     * @since V2.0
     * @version 01/10/2002
     */
    public final static byte ACTIVE = 0;

    /**
     * <code>Shortcut</code> is passive.
     * <p>
     * <code>Shortcuts</code> that in this state can only be used to pass back
     * responses to requests. These <code>Shortcuts</code> are used to provide
     * a reverse connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ACTIVE
     * @since V2.0
     * @version 01/10/2002
     */
    public final static byte PASSIVE = 1;

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
     * Gets the active state.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the value.
     * @see #ACTIVE
     * @see #PASSIVE
     * @see #setActive(byte)
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    public abstract byte getActive();

    /**
     * Gets the cost of using this <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cost.
     * @see #setCost(double)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public abstract double getCost();

    /**
     * Gets the address of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address.
     * @see #setDestinationAddress(String)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public abstract String getDestinationAddress();

    /**
     * Gets the fully-qualified name of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setDestinationName(String)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public abstract String getDestinationName();

    /**
     * Sets the active state.
     * <p>
     *
     * @author Ian Brown
     *
     * @param activeI the new active state.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the <code>activeI</code> state value is not
     *		  <code>ACTIVE</code> or <code>PASSIVE</code>.
     * @see #ACTIVE
     * @see #PASSIVE
     * @see #getActive()
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    public abstract void setActive(byte activeI);

    /**
     * Sets the cost of using this <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param costI the cost.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the cost is less than 1.
     * @see #getCost()
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public abstract void setCost(double costI);

    /**
     * Sets the address of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param destinationAddressI the destination address.
     * @see #getDestinationAddress()
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public abstract void setDestinationAddress(String destinationAddressI);

    /**
     * Sets the name of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param destinationNameI the destination name.
     * @see #getDestinationName()
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public abstract void setDestinationName(String destinationNameI);
}
