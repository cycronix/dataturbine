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
 * Local path class.
 * <p>
 * A local path is a <code>Path</code> that leads from the local
 * <bold>RBNB</bold> server to one of its peers. The local path has a cost in
 * addition to the list of servers that it passes through.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/28/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/26/2003  INB	Replaced <code>compareTo(Path)</code> with
 *			<code>compareTo(Path,boolean)</code>.
 * 12/21/2001  INB	Created.
 *
 */
final class LocalPath
    extends com.rbnb.api.Path
{
    /**
     * the cost of this <code>LocalPath</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private double cost = 0.;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    LocalPath() {
	super();
    }

    /**
     * Class constructor to build a <code>LocalPath</code> from an existing
     * <code>LocalPath</code> and a <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lPathI the existing <code>LocalPath</code>
     * @param pConI  the <code>PeerConnection</code>.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    LocalPath(LocalPath lPathI,Shortcut shortcutI) {
	this();

	setOrdered((java.util.Vector) lPathI.getOrdered().clone());
	try {
	    add(shortcutI.getDestinationName());
	} catch (java.lang.Exception e) {
	    throw new java.lang.InternalError();
	}
	addCost(lPathI.getCost() + shortcutI.getCost());
    }

    /**
     * Adds to the cost of this <code>LocalPath</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param costI the cost to add.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final void addCost(double costI) {
	setCost(getCost() + costI);
    }

    /**
     * Compares this <code>LocalPath</code> to the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>LocalPath</code> passed as a
     *		      <code>Path</code>.
     * @param costI   is cost the primary comparison?
     * @return the results of the comparison:
     *	       <p><0 if this <code>LocalPath</code> compares less than the
     *		  input,
     *	       <p> 0 if this <code>LocalPath</code> compares equal to the
     *		  input, and
     *	       <p>>0 if this <code>LocalPath</code> compares greater than the
     *		   input.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Added <code>costI</code> parameter and handling.
     * 12/21/2001  INB	Created.
     *
     */
    public final int compareTo(Path otherI,boolean costI) {
	int comparedR = 0,
	    costCompared = 0;

	if (otherI instanceof LocalPath) {
	    double otherCost = ((LocalPath) otherI).getCost();

	    if (getCost() < otherCost) {
		costCompared = -1;
	    } else if (getCost() > otherCost) {
		costCompared = 1;
	    }
	}

	if (costI &&(costCompared != 0)) {
	    comparedR = costCompared;
	} else {
	    comparedR = super.compareTo(otherI,costI);
	    if ((comparedR = super.compareTo(otherI,false)) == 0) {
		comparedR = costCompared;
	    }
	}

	return (comparedR);
    }

    /**
     * Gets the cost of this <code>LocalPath</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cost.
     * @see #setCost(double)
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final double getCost() {
	return (cost);
    }

    /**
     * Sets the cost of this <code>LocalPath</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param costI the new cost.
     * @see #getCost()
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final void setCost(double costI) {
	cost = costI;
    }

    /**
     * Converts this <code>LocalPath</code> to a standard <code>Path</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the standard <code>Path</code>.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final Path toPath() {
	Path pathR = new Path();
	pathR.setOrdered((java.util.Vector) getOrdered().clone());
	return (pathR);
    }

    /**
     * Gets a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2001  INB	Created.
     *
     */
    public String toString() {
	return (super.toString() + " cost: " + getCost());
    }
}

