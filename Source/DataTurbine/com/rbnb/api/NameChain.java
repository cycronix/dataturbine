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
 * Chain of <code>Rmaps</code> used for getting the names of all
 * <code>Rmaps</code> that have names and data.
 * <p>
 * <code>NameChain</code> is an extension of <code>RmapChain</code>
 * specifically used to extract names of <code>Rmaps</code> in a hierarchy that
 * have data associated with them.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap#extractNames()
 * @since V2.0
 * @version 03/28/2003
 */

/*
 * Copyright 2001, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/14/2003  INB	Use <code>StringBuffer</code> to make name.
 * 01/11/2001  INB	Created.
 *
 */
final class NameChain
    extends com.rbnb.api.RmapChain
{
    /**
     * are we at a valid name point?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2001
     */
    private boolean atName = false;

    /**
     * have we seen a name after the last data reference?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2001
     */
    private boolean nameAfterReference = false;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #NameChain(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    NameChain() {
	super();
    }

    /**
     * Class constructor to build an <code>RmapChain</code> starting with a
     * root <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rootI  the root <code>Rmap</code>.
     * @see #NameChain()
     * @since V2.0
     * @version 01/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    NameChain(Rmap rootI) {
	super();
	addRmap(rootI);
    }

    /**
     * Adds an <code>Rmap</code> to the end of the chain.
     * <p>
     * The input may be null, indicating that there is no <code>Rmap</code> at
     * this point in the chain.
     * <p>
     * This method also determines if the input <code>Rmap</code> puts us at
     * new name point.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code>.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 01/11/2001  INB	Created.
     *
     */
    final void addRmap(Rmap rmapI) {
	// Store the current state of the payload.
	boolean hadPayload = getHavePayload();

	// Assume that we're no longer at a naming point.
	setAtName(false);

	if (rmapI.getDblock() != null) {
	    // If the input <code>Rmap</code> has a data block, it is probably
	    // a naming point, although it might not be.

	    if (getNameAfterReference() || (rmapI.getName() != null)) {
		// If the <code>Rmap</code> also has a name or we've seen a
		// name since the previous data block, then we're at a naming
		// point.
		setAtName(true);
	    }

	    // Clear the name after reference flag.
	    setNameAfterReference(false);

	} else if (rmapI.getName() != null) {
	    // If this <code>Rmap</code> has a name, but no data, then we've
	    // got a name after a reference.

	/*
	  INB 08/10/2001
	  * I'm not sure why the check for payload was being made here. I
	  * can't think of any particular reason for it. However, I'm leaving
	  * this in just in case I figure it out. However, taking it out ensure
	  * that it works in cases where the name is in one Rmap and the data
	  * is in a descendant Rmap.

	} else if ((rmapI.getName() != null) && getHavePayload()) {
	    // If this <code>Rmap</code> has a name, but no data, and we've
	    // seen the data payload already, then we've got a name after a
	    // reference.
	*/
	    setNameAfterReference(true);
	}

	// Add the <code>Rmap</code> into the chain.
	super.addRmap(rmapI);

	if (!hadPayload && getHavePayload() && (rmapI.getName() != null)) {
	    // If this <code>Rmap</code> supplies everything we need, we're at
	    // a naming point.
	    setAtName(true);
	}
    }

    /**
     * Gets the at name flag.
     * <p>
     * Determines if this <code>NameChain</code> is at a point where we have
     * everything we need to extract a name?
     * <p>
     * To be at a point where we can extract a name, one of the following has
     * to have occured:
     * <p><ol>
     * <li>we have to have just found the data payload, or</li>
     * <li>we have to have just found a data reference after seeing the data
     *	   payload and a name.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @return are we at a name?
     * @see #setAtName(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    final boolean getAtName() {
	return (atName);
    }

    /**
     * Gets the name from this <code>NameChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>NameChain</code> is not at a naming
     *		  point.
     * @since V2.0
     * @version 03/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2003  INB	Use <code>StringBuffer</code> to make name.
     * 01/11/2001  INB	Created.
     *
     */
    final String getName() {
	if (!getAtName()) {
	    throw new java.lang.IllegalStateException
		(this + " is not at a naming point.");
	}

	// Create the return name.
	StringBuffer nameB = null;

	for (int idx = 0; idx < getRmaps().size(); ++idx) {
	    // Append the name of each <code>Rmap</code> in the list to the
	    // return name.
	    Rmap rmap = (Rmap) getRmaps().elementAt(idx);

	    if (rmap.getName() != null) {
		// If this <code>Rmap</code> has a name, add it to the return
		// name.

		if (nameB == null) {
		    // If there was no return name yet, create it from this
		    // <code>Rmap's</code> name.
		    if (rmap.compareNames(".") != 0) {
			// If the name of the <code>Rmap</code> is not a
			// relative name indicator, create an absolute name.
			nameB = new StringBuffer("");
			nameB.append(Rmap.PATHDELIMITER);
			nameB.append(rmap.getName());

		    } else {
			// Otherwise, create a relative name.
			nameB = new StringBuffer("");
		    }

		} else if (nameB.length() == 0) {
		    // If we have a relative name, then just make the name
		    // equal to the current <code>Rmap</code>.
		    nameB = nameB.append(rmap.getName());

		} else {
		    // If there is a return name, append this
		    // <code>Rmap's</code> name to it.
		    nameB.append(Rmap.PATHDELIMITER).append(rmap.getName());
		}
	    }
	}

	String nameR = (nameB == null) ? null : nameB.toString();
	return (nameR);
    }

    /**
     * Gets the name after reference flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return have we seen a name since the last data reference?
     * @see #setNameAfterReference(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    private final boolean getNameAfterReference() {
	return (nameAfterReference);
    }

    /**
     * Sets the at name flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param atNameI  are we at a name point?
     * @see #getAtName()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    private final void setAtName(boolean atNameI) {
	atName = atNameI;
    }

    /**
     * Sets the name after data reference flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameAfterReferenceI  have we seen a name after the last data
     *				   reference?
     * @see #getNameAfterReference()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    private final void setNameAfterReference(boolean nameAfterReferenceI) {
	nameAfterReference = nameAfterReferenceI;
    }
}
