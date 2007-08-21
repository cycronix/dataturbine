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
 * <code>Rmaps</code> that are named and do not have any children.
 * <p>
 * <code>FolderChain</code> is an extension of <code>RmapChain</code>
 * specifically used to extract names of <code>Rmaps</code> in a hierarchy that
 * do not have children.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap#extractFoldersAndChannels
 * @since V2.1
 * @version 05/14/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/06/2003  INB	Created from <code>NameChain</code>.
 *
 */
final class FolderChain
    extends com.rbnb.api.RmapChain
{
    /**
     * are we at a valid name point?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/06/2003
     */
    private boolean atName = false;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #FolderChain(com.rbnb.api.Rmap)
     * @since V2.1
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
     *
     */
    FolderChain() {
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
     * @see #FolderChain()
     * @since V2.1
     * @version 01/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
     *
     */
    FolderChain(Rmap rootI) {
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
     * @since V2.1
     * @version 05/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
     *
     */
    final void addRmap(Rmap rmapI) {
	// Add the <code>Rmap</code> into the chain.
	super.addRmap(rmapI);

	// Determine if we're at the bottom.
	try {
	    setAtName(!(rmapI instanceof ControllerInterface) &&
		      !(rmapI instanceof SinkInterface) &&
		      (rmapI.getName() != null) &&
		      (rmapI.compareNames(".") != 0) &&
		      (rmapI.getNmembers() == 0) &&
		      (rmapI.getNchildren() == 0) &&
		      (rmapI.getDblock() == null));
	} catch (java.lang.InterruptedException e) {
	    setAtName(false);
	} catch (java.io.IOException e) {
	    setAtName(false);
	} catch (com.rbnb.api.RBNBException e) {
	    setAtName(false);
	}
    }

    /**
     * Gets the at name flag.
     * <p>
     * Determines if this <code>FolderChain</code> is at a point where we have
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
     * @since V2.1
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
     *
     */
    final boolean getAtName() {
	return (atName);
    }

    /**
     * Gets the name from this <code>FolderChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>FolderChain</code> is not at a naming
     *		  point.
     * @since V2.1
     * @version 03/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
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

	String nameR = null;
	if (nameB != null) {
	    nameB.append(Rmap.PATHDELIMITER);
	    nameR = nameB.toString();
	}
	return (nameR);
    }

    /**
     * Sets the at name flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param atNameI  are we at a name point?
     * @see #getAtName()
     * @since V2.1
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
     *
     */
    private final void setAtName(boolean atNameI) {
	atName = atNameI;
    }
}
