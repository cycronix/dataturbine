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
 * <code>Rmap</code> information extractor.
 * <p>
 * This abstract class provides the base class for the classes that are used by
 * the <code>RmapExtractor</code> class to actually pull interesting
 * information out of the <code>RmapChains</code> that it has matched.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/30/2002
 */

/*
 * Copyright 2000, 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/11/2000  INB	Created.
 *
 *
 */
abstract class InformationExtractor {
    /**
     * the list of extracted information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/22/2001
     */
    private java.util.Vector information = null;

    /**
     * Adds an <code>Rmap</code> to the extracted information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationI  the <code>Rmap</code> containing the extracted
     *			    information. 
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    final synchronized void addInformation(Rmap informationI) {
	if (informationI != null) {
	    // If something is to be placed into this
	    // <code>InformationExtractor</code>.
	    if (getInformation() == null) {
		// When there is no existing information, create a vector.
		information = new java.util.Vector();
	    }

	    // Copy the input information onto the end of the existing
	    // information.
	    information.addElement(informationI);
	}
    }

    /**
     * Extracts information from the input <code>RmapChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	    the matched request <code>Rmap</code>.
     * @param chainI	    the <code>RmapChain</code>.
     * @param extractFrameI is the frame information desired?
     * @param extractDataI  is the actual data payload desired?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    abstract void extract(Rmap requestI,
			  RmapChain chainI,
			  boolean extractFrameI,
			  boolean extractDataI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the extracted information.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the extracted information.
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    final java.util.Vector getInformation() {
	return (information);
    }
}
