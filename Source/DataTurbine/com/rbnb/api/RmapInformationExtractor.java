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
 * Extracts <code>Rmap</code> information from a <code>RmapChains</code>.
 * <p>
 * This subclass of <code>InformationExtractor</code> is used by the Java API
 * and the server to extract <code>Rmap</code> information. The class supports
 * the extraction of <code>Rmaps</code> with or without the actual data
 * payloads.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 10/28/2002
 */

/*
 * Copyright 2000, 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/15/2000  INB	Created.
 *
 *
 */
class RmapInformationExtractor
    extends com.rbnb.api.InformationExtractor
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    RmapInformationExtractor() {
	super();
    }

    /**
     * Extracts information from the input <code>RmapChain</code>.
     * <p>
     * The input <code>RmapChain</code> is assumed to actually be an
     * <code>ExtractedChain</code>.
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
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    final void extract(Rmap requestI,
		       RmapChain chainI,
		       boolean extractFrameI,
		       boolean extractDataI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	ExtractedChain eChain = (ExtractedChain) chainI;

	// Build an <code>Rmap</code> hierarchy of extracted information from
	// the input <code>ExtractedChain</code> and add it to the information
	// stored for this <code>RmapInformationExtractor</code>.
	Rmap extracted = eChain.buildRmapHierarchy(extractDataI);

	//System.err.println(eChain + " ->\n" + extracted);

	if (extracted != null) {
	    addInformation(extracted);
	}
    }
}
