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
 * Extracts <code>DataArray</code> information from <code>RmapChains</code>.
 * <p>
 * This subclass of <code>InformationExtractor</code> is used by the Java API
 * to extract <code>DataArray</code> information. The class supports the
 * extraction of just frames, just time, just data, or any combination of the
 * three.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataArray
 * @since V2.0
 * @version 09/20/2002
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
class DataArrayExtractor
    extends com.rbnb.api.InformationExtractor
{
    /**
     * the type of the data points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private byte dType = DataBlock.TYPE_FROM_INPUT;

    /**
     * the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/24/2002
     */
    private String mimeType = null;

    /**
     * the total number of data points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private int nPoints = 0;

    /**
     * the point size in bytes of the data points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private int pointSize = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    DataArrayExtractor() {
	super();
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
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @see #getInformation()
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    final void extract(Rmap requestI,
		       RmapChain chainI,
		       boolean extractFrameI,
		       boolean extractDataI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Expand the chain into a single fully qualified <code>Rmap</code>.
	Rmap information = chainI.expandRmap(extractDataI);

	if (information != null) {
	    // If we got anything, add it to this
	    // <code>DataArrayExtractor</code>.
	    Rmap dBlockAt;
	    for (dBlockAt = information;
		 (dBlockAt != null) && (dBlockAt.getDblock() == null);
		 dBlockAt =
		     ((dBlockAt.getNchildren() == 0) ?
		      null :
		      dBlockAt.getChildAt(0))) {}

	    if (dBlockAt == null) {
		throw new java.lang.IllegalStateException
		    ("Cannot extract data array from an Rmap without data:\n"
		     + chainI);
	    }

	    if (getInformation() == null) {
		// If this is the first <code>RmapChain</code>, then set the
		// data type and point size.
		setDtype(dBlockAt.getDblock().getDtype());
		setPtsize(dBlockAt.getDblock().getPtsize());
		setMIMEType(dBlockAt.getDblock().getMIMEType());
	    }

	    addInformation(information);

	    if (getNpoints() > 0) {
		// If something has changed about the data, that is not
		// acceptable.

		if (dBlockAt.getDblock().getDtype() != getDtype()) {
		    throw new java.lang.IllegalStateException
			("The data type (" +
			 DataBlock.TYPES[dBlockAt.getDblock().getDtype()] +
			 " of this extracted information does not match " +
			 "that of previously extracted information (" +
			 DataBlock.TYPES[dType] + ").");
		} else if ((dBlockAt.getDblock().getDtype() !=
			    DataBlock.TYPE_STRING) &&
			   (dBlockAt.getDblock().getDtype() !=
			    DataBlock.TYPE_BYTEARRAY) &&
			   (dBlockAt.getDblock().getPtsize() !=
			    getPtsize())) {
		    throw new java.lang.IllegalStateException
			("The data size (" +
			 dBlockAt.getDblock().getPtsize() +
			 ") of this extracted information does not match " +
			 "that of previously extracted information (" +
			 getPtsize() + ")");
		} else if ((getMIMEType() == null) ?
			   (dBlockAt.getDblock().getMIMEType() != null) :
			   ((dBlockAt.getDblock().getMIMEType() == null) ||
			    !dBlockAt.getDblock().getMIMEType().equals
			    (getMIMEType()))) {
		    throw new java.lang.IllegalStateException
			("The MIME type (" +
			 dBlockAt.getDblock().getMIMEType() +
			 ") of this extracted information does not match " +
			 "that of the previously extracted information (" +
			 getMIMEType() + ")");
		}
	    }

	    // Add to the total number of points.
	    setNpoints(getNpoints() + dBlockAt.getDblock().getNpts());
	}
    }

    /**
     * Gets the data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data type.
     * @see #setDtype(byte)
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    final byte getDtype() {
	return (dType);
    }

    /**
     * Gets the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the MIME type.
     * @see #setMIMEType(String)
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    final String getMIMEType() {
	return (mimeType);
    }

    /**
     * Gets the number of data points.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of data points.
     * @see #setNpoints(int)
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    final int getNpoints() {
	return (nPoints);
    }

    /**
     * Gets the point size.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the point size.
     * @see #setPtsize(int)
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    final int getPtsize() {
	return (pointSize);
    }

    /**
     * Get the time and data from this extracted information.
     * <p>
     * The time array is sorted by increasing time. The data array is
     * sorted by the time array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameFlagI return the extract frame indexes?
     * @param timeFlagI  return the extacted times?
     * @param dataFlagI  return the extraced data?
     * @return a <code>DataArray</code> object containing the extracted
     *	   times and data.
     * @exception java.lang.IllegalStateException
     *	      thrown if the data cannot be returned using a single
     *	      primitive type.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    final DataArray getTimeAndData(boolean frameFlagI,
				   boolean timeFlagI,
				   boolean dataFlagI)
    {
	if (getNpoints() == 0) {
	    // With no data points, we can return an empty time and data
	    // block.
	    return (new DataArray());
	}

	DataArray dArrayR = new DataArray();
	dArrayR.setNumberOfPoints(getNpoints(),getPtsize(),getDtype());
	dArrayR.setMIMEType(getMIMEType());

	for (int idx = 0; idx < getInformation().size(); ++idx) {
	    Rmap rMap = (Rmap) getInformation().elementAt(idx);

	    int nPoints = 0;
	    if (rMap.getDblock() != null) {
		nPoints = rMap.getDblock().getNpts();
	    } else if (rMap.getTrange() != null) {
		nPoints = rMap.getTrange().getNptimes();
	    } else if (rMap.getFrange() != null) {
		nPoints = rMap.getFrange().getNptimes();
	    }

	    dArrayR.add(nPoints,
		       rMap.getDblock(),
		       rMap.getTrange(),
		       rMap.getFrange());
	}

	return (dArrayR);
    }

    /**
     * Sets the data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dTypeI the data type.
     * @see #getDtype()
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    final void setDtype(byte dTypeI) {
	dType = dTypeI;
    }

    /**
     * Sets the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @param mimeTypeI the MIME type.
     * @see #getMIMEType()
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    final void setMIMEType(String mimeTypeI) {
	mimeType = mimeTypeI;
    }

    /**
     * Sets the number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI the number of points.
     * @see #getNpoints()
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    final void setNpoints(int nPointsI) {
	nPoints = nPointsI;
    }

    /**
     * Sets the point size.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptSizeI the point size.
     * @see #getPtsize()
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    final void setPtsize(int ptSizeI) {
	pointSize = ptSizeI;
    }
}
