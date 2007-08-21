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
 * A list of <code>Rmaps</code> representing an inheritance chain through an
 * <code>Rmap</code> hierarchy.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 04/05/2002
 */

/*
 * Copyright 2000, 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/30/2000  INB	Created.
 *
 */
class RmapChain
    implements java.io.Serializable,
	       java.lang.Cloneable
{
    /**
     * have we seen a data payload?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/08/2000
     */
    private boolean havePayload = false;

    /**
     * the list of <code>Rmaps</code> in the chain.
     * <p>
     * The order of the <code>Rmaps</code> in the list is the order of
     * inheritance from parent to child.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/25/2001
     */
    private java.util.Vector rmaps = new java.util.Vector();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #RmapChain(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    RmapChain() {
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
     * @see #RmapChain()
     * @since V2.0
     * @version 01/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    RmapChain(Rmap rootI) {
	this();
	addRmap(rootI);
    }

    /**
     * Adds an <code>Rmap</code> to the end of the chain.
     * <p>
     * The input may be null, indicating that there is no <code>Rmap</code> at
     * this point in the chain.
     * <p>
     * This method also determines if the input <code>Rmap</code> has a data
     * payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code>.
     * @since V2.0
     * @version 01/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    void addRmap(Rmap rmapI) {
	rmaps.addElement(rmapI);
	if (!getHavePayload() &&
	    (rmapI != null) &&
	    (rmapI.getDblock() != null) &&
	    (rmapI.getDblock().getData() != null)) {
	    setHavePayload(true);
	}
    }

    /**
     * Clones this <code>Rmap</code> chain.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cloned chain.
     * @since V2.0
     * @version 05/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    protected Object clone() {
	RmapChain clonedR = null;

	try {
	    clonedR = (RmapChain) super.clone();
	} catch (CloneNotSupportedException e) {
	    return (null);
	}

	if (getRmaps() != null) {
	    clonedR.rmaps = (java.util.Vector) getRmaps().clone();
	}

	return (clonedR);
    }

    /**
     * Gets an <code>Rmap</code> containing the fully expanded information
     * contained in this chain.
     * <p>
     * This method executes all of the <code>Rmap</code> inheritance rules
     * using the <code>Rmaps</code> in the chain to build a new
     * <code>Rmap</code> containing a fully qualified name, a completely filled
     * <code>TimeRange</code> and a completely filled <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataFlagI  extract the data?
     * @return the expanded <code>Rmap</code>. This is <code>null</code> if
     *	       the chain is empty.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getRmaps()
     * @since V2.0
     * @version 04/05/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    final Rmap expandRmap(boolean dataFlagI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getRmaps().size() == 0) {
	    // With no <code>Rmaps</code> in the chain, we cannot perform the
	    // expansion, so return null.
	    return (null);
	}

	// Build the expanded <code>Rmap</code> from the <code>Rmaps</code> in
	// the chain.
	Rmap rmapR = new Rmap();
	RmapInheritor rmapIn = new RmapInheritor();
	rmapIn.setDataFlag(dataFlagI);

	DataBlock dBlock = null;
	for (int idx = getRmaps().size() - 1; idx >= 0; --idx) {
	    Rmap rmap = (Rmap) getRmaps().elementAt(idx);

	    // Inherit information from the next <code>Rmap</code> in the
	    // chain. As necessary, we build a temporary object called an
	    // <code>RmapInheritor</code>. It holds a set of <code>Rmaps</code>
	    // temporarily that we can then inherit from.

	    if (!rmapR.inheritFrom(rmap,rmapIn) &&
		dataFlagI &&
		(dBlock == null)) {
		// If nothing was inherited from an <code>Rmap</code> with a
		// <code>DataBlock</code> and we haven't had that happen
		// before, then hold the <code>DataBlock</code> for later.
		dBlock = rmap.getDblock();
	    }

	    if ((rmapR.getDblock() != null) &&
		(rmapR.getDblock().getData() !=	null)) {
		// Once we have a data payload, we don't have any further
		// interest in adding data.
		rmapIn.setWantData(false);
	    }
	}

	if ((rmapR.getDblock() == null) &&
	    (dBlock != null)) {
	    // If we get here with no <code>DataBlock</code>, but we saw one,
	    // then return it.
	    rmapR.setDblock(dBlock);
	}

	return (rmapR);
    }

    /**
     * Gets the have payload flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return have we seen a data payload?
     * @see #setHavePayload(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final boolean getHavePayload() {
	return (havePayload);
    }

    /**
     * Gets the last <code>Rmap</code> in the chain.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last <code>Rmap</code>.
     * @see #addRmap(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final Rmap getLast() {
	return ((Rmap) ((getRmaps().size() == 0) ?
			null :
			getRmaps().lastElement()));
    }

    /**
     * Gets the list of <code>Rmaps</code> in the chain.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of <code>Rmaps</code>.
     * @see #addRmap(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    final java.util.Vector getRmaps() {
	return (rmaps);
    }

    /**
     * Sets the have payload flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param havePayloadI  have we seen a data payload?
     * @see #getHavePayload()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final void setHavePayload(boolean havePayloadI) {
	havePayload = havePayloadI;
    }

    /**
     * Returns a string representation of the <code>RmapChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public String toString() {
	String stringR = "";

	if (rmaps.size() > 0) {
	    for (int idx = 0; idx < rmaps.size(); ++idx) {
		Rmap rmap = (Rmap) rmaps.elementAt(idx);

		if (idx > 0) {
		    stringR += Rmap.PATHDELIMITER;
		}
		stringR +=
		    ((rmap == null) ? "<null>" :
		     (((rmap.getName() == null)  ?
		       "" :
		       rmap.getName()) +
		      ((rmap.getTrange() == null) ?
		       "" :
		       (" " + rmap.getTrange().toString())) +
		      ((rmap.getFrange() == null) ?
		       "" :
		       (" F" + rmap.getFrange().toString()))));
	    }
	}

	return (stringR);
    }
}

