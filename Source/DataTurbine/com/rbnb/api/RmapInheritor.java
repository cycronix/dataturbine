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
 * Handles inheritance through multiple <code>Rmaps</code> simultaneously due
 * to  where information is actually stored.
 * <p>
 * The purpose of this class is to deal with the case where not all of the
 * information to be inherited from an <code>Rmap</code> is actually contained
 * in that <code>Rmap</code>.
 * <p>
 * For example, if a <code>Rmap</code> with multiple data points is inheriting
 * from an <code>Rmap</code> containing a <code>TimeRange</code>, it may be
 * necessary to construct an individual time value for every data point. If the
 * <code>TimeRange</code> is lacking a duration, the duration must be inherited
 * from another <code>Rmap's</code> <code>TimeRange</code> before the time
 * values can be calculated.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataBlock
 * @see com.rbnb.api.Rmap
 * @see com.rbnb.api.TimeRange
 * @since V2.0
 * @version 09/18/2001
 */

/*
 * Copyright 2000, 2001 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/01/2000  INB	Created.
 *
 *
 */
final class RmapInheritor
    implements java.io.Serializable,
	       java.lang.Cloneable
{
    /**
     * extract the data?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2000
     */
    private boolean dataFlag = true;

    /**
     * is the data of any interest?
     * <p>
     * The object is usually used to inherit from source <code>Rmaps</code>
     * where data payloads are of interest. However, it can also be used to
     * inherit request <code>Rmaps</code>, where data payloads don't
     * exist. This flag tells the <code>RmapInheritor</code> not to care about
     * data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/07/2000
     */
    private boolean wantData = true;

    /**
     * missing information bitmask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/01/2000
     */
    private byte missingMask = EVERYTHING;

    /**
     * index of the <code>Rmap</code> containing the <code>DataBlock</code>
     * with the data payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/18/2000
     */
    private int payload = -1;

    /**
     * index of the <code>Rmap</code> containing the <code>TimeRange</code>
     * with the time values for the data payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/18/2000
     */
    private int timeValues = -1;

    /**
     * <code>Rmaps</code> to inherit from.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/22/2001
     */
    private java.util.Vector rmaps = new java.util.Vector();

    // Private constants:
    private final static byte	NOTHING = 0,
				EVERYTHING = 0x07,
				TIMEVALUES = 0x01,
				DURATION = 0x02,
				DATAPAYLOAD = 0x04;

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
     * 12/01/2000  INB	Created.
     *
     */
    RmapInheritor() {
	super();
    }

    /**
     * Adds the input <code>Rmap</code> to the inheritance list and determines
     * whether or not it provides enough information to fill in the missing
     * parts.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code>.
     * @return is enough information available to inherit?
     * @see #getRmaps()
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    final boolean addRmap(Rmap rmapI) {
	int idx = getRmaps().size();

	// Add the <code> Rmap to the list.
	getRmaps().addElement(rmapI);

	// Grab its pieces.
	DataBlock dBlock = rmapI.getDblock();
	TimeRange tRange = rmapI.getTrange();

	if (!getWantData()) {
	    // When the data is not of interest, we can always say it isn't
	    // missing.
	    missingMask = (byte) (missingMask & ~DATAPAYLOAD);

	} else if ((dBlock != null) && isMissing(DATAPAYLOAD)) {
	    // If the input <code>Rmap</code> has a <code>DataBlock</code> and
	    // we need a data payload, then see if the input
	    // <code>Rmap's</code> <code>DataBlock</code> includes a data
	    // payload.

	    if (dBlock.getData() != null) {
		// If the payload exists, clear the data payload missing bit.
		missingMask = (byte) (missingMask & ~DATAPAYLOAD);

		// Store the index <code>Rmap</code> as the one with the
		// payload.
		payload = idx;
	    }
	}

	if (tRange != null) {
	    // If the input Rmap has a <code>TimeRange</code>, determine if we
	    // need to fill anything in to actually be able to do an
	    // inheritance.

	    if (tRange.getPtimes() != TimeRange.INHERIT_TIMES) {
		// If there are time values in the <code>Rmap's</code>
		// <code>TimeRange</code>, they may be of use to us.

		if (isMissing(TIMEVALUES)) {
		    // If we were missing time values, we have them now.
		    missingMask = (byte) (missingMask & ~TIMEVALUES);
		}

		if (!getWantData() ||
		    ((idx >= payload) && (timeValues < payload))) {
		    // If the data is not of interest or the data payload is
		    // missing time values, store the index of this
		    // <code>Rmap</code> as the one to retrieve them from.
		    timeValues = idx;
		}
	    }
	    
	    if (isMissing(DURATION) &&
		(tRange.getDuration() != TimeRange.INHERIT_DURATION)) {
		// If this <code>Rmap's</code> <code>TimeRange</code> can fill
		// in the duration, clear the missing bit.
		missingMask = (byte) (missingMask & ~DURATION);
	    }
	}

	return ((missingMask == NOTHING) && (timeValues >= payload));
    }

    /**
     * Apply information in the <code>Rmaps</code> in this
     * <code>RmapInheritor</code> to the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code>.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if the input <code>Rmap</code> is missing information
     *		  that cannot be retrieved from the <code>Rmaps</code> in this
     *		  <code>RmapInheritor's</code> list.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getRmaps()
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    void apply(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean needPayload =
	    getWantData() &&
	    ((rmapI.getDblock() == null) ||
	     (rmapI.getDblock().getData() == null));

	if (needPayload && (payload == -1)) {
	    throw new IllegalStateException
		("There is no data payload to be inherited.");
	}

	// Build the <code>DataBlock</code> and <code>TimeRange</code> to
	// actually inherit information from.
	String name = null;
	DataBlock dBlock = null;
	TimeRange tRange = null,
		  fRange = null;

	for (int idx = getRmaps().size() - 1; idx >= 0; --idx) {
	    DataBlock ndBlock = dBlock;
	    TimeRange ntRange = tRange,
		      nfRange = fRange;

	    // Work from the end of the inheritance list to the beginning.
	    Rmap rmap = (Rmap) getRmaps().elementAt(idx);

	    if (rmap.getName() != null) {
		// Append the name of this <code>Rmap</code> to the name being
		// built for the return <code>Rmap</code>.
		if (name == null) {
		    name = rmap.getName();
		} else {
		    name = name + Rmap.PATHDELIMITER + rmap.getName();
		}
	    }

	    if (needPayload &&
		(rmap.getDblock() != null) &&
		(idx <= payload)) {
		// If we are trying to get the data payload and this
		// <code>Rmap's</code> <code>DataBlock</code> contributes to
		// the data payload, inherit from this <code>Rmap's</code>
		// <code>DataBlock</code>.
		ndBlock = rmap.getDblock().extractInheritance(dBlock,
							      getDataFlag());
	    }

	    if (rmap.getTrange() != null) {
		// If this <code>Rmap</code> has a <code>TimeRange</code>, we
		// need to inherit from it.
		ntRange = rmap.getTrange().extractInheritance
		    ((idx == timeValues),
		     tRange,
		     dBlock,
		     rmap.getDblock());
	    }

	    if (rmap.getFrange() != null) {
		// If this <code>Rmap</code> has a frame
		// <code>TimeRange</code>, we use it if we don't already have a
		// frame <code>TimeRange</code>.
		if (fRange == null) {
		    nfRange = (TimeRange) rmap.getFrange().clone();
		}
	    }

	    dBlock = ndBlock;
	    tRange = ntRange;
	    fRange = nfRange;
	}

	if (name != null) {
	    // If we created a name, prepend it to the name of the input
	    // <code>Rmap</code>.
	    if (rmapI.getName() == null) {
		rmapI.setName(name);
	    } else {
		rmapI.setName(name + Rmap.PATHDELIMITER + rmapI.getName());
	    }
	}

	DataBlock ndBlock = dBlock;
	if ((dBlock != null) && (rmapI.getDblock() != null)) {
	    // If we created a <code>DataBlock</code> and the input
	    // <code>Rmap</code> has a <code>DataBlock</code>, extract the
	    // input <code>Rmap's</code> inheritance from it.
	    ndBlock = rmapI.getDblock().extractInheritance(dBlock,
							   getDataFlag());
	}

	TimeRange ntRange = tRange;
	if (rmapI.getTrange() != null) {
	    // If the input <code>Rmap</code> has a <code>TimeRange</code>,
	    // extract its inheritance from the created
	    // <code>TimeRange</code>.
	    ntRange = rmapI.getTrange().extractInheritance
		(true,
		 tRange,
		 dBlock,
		 rmapI.getDblock());
	}

	TimeRange nfRange = fRange;
	if (rmapI.getFrange() != null) {
	    // If the input <code>Rmap</code> has a frame
	    // <code>TimeRange</code>, use that.
	    nfRange = rmapI.getFrange();
	}

	// Update the input <code>Rmap</code>.
	if (ndBlock != null) {
	    rmapI.setDblock(ndBlock);
	}
	rmapI.setTrange(ntRange);
	rmapI.setFrange(nfRange);

	// Reset this <code>RmapInheritor</code> for next time.
	missingMask = NOTHING;
	getRmaps().removeAllElements();
	payload =
	    timeValues = -1;

	return;
    }

    /**
     * Clones this <code>RmapInheritor</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 12/18/2000
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    protected final Object clone() {
	RmapInheritor clonedR = null;

	try {
	    clonedR = (RmapInheritor) super.clone();
	} catch (CloneNotSupportedException e) {
	    return (null);
	}

	clonedR.setRmaps((java.util.Vector) getRmaps().clone());

	return (clonedR);
    }

    /**
     * Gets the data extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return extract the data?
     * @see #setDataFlag(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private final boolean getDataFlag() {
	return (dataFlag);
    }

    /**
     * Gets the <code>Rmaps vector</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Rmaps vector</code>.
     * @see #setRmaps(java.util.Vector)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2000  INB	Created.
     *
     */
    private final java.util.Vector getRmaps() {
	return (rmaps);
    }

    /**
     * Gets the want data flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the data  of interest?
     * @see #setWantData(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private final boolean getWantData() {
	return (wantData);
    }

    /**
     * Determines if the values specified by the input mask are missing.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needI  the values of interest as a mask of missing bits.
     * @return are the values missing?
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    private final boolean isMissing(byte needI) {
	return ((missingMask & needI) == needI);
    }

    /**
     * Sets the data extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataFlagI  extract the data?
     * @see #getDataFlag()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2000  INB	Created.
     *
     */
    final void setDataFlag(boolean dataFlagI) {
	dataFlag = dataFlagI;
    }

    /**
     * Sets the <code>Rmaps vector</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapsI  the new <code>Rmaps vector</code>.
     * @see #getRmaps()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2000  INB	Created.
     *
     */
    private final void setRmaps(java.util.Vector rmapsI) {
	rmaps = rmapsI;
    }

    /**
     * Sets the want data flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param wantDataI  is the data of interest?
     * @see #getWantData()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2000  INB	Created.
     *
     */
    final void setWantData(boolean wantDataI) {
	wantData = wantDataI;
    }

    /**
     * Gets a displayable string representation of this
     * <code>RmapInheritor</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 12/04/2000
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2000  INB	Created.
     *
     */
    public String toString() {
	String stringR = "(" + missingMask + ") ";

	if (rmaps.size() > 0) {
	    int idx;
	    for (idx = 0; idx < rmaps.size() - 1; ++idx) {
		Rmap rmap = (Rmap) rmaps.elementAt(idx);

		stringR +=
		    ((rmap.getName() == null)  ?
		     "" :
		     rmap.getName()) +
		    ((rmap.getTrange() == null) ?
		     "" :
		     rmap.getTrange().toString()) +
		    ((idx == payload) ? "(payload)" : "") +
		    ((idx == timeValues) ? "(times)" : "") +
		    Rmap.PATHDELIMITER;
	    }
	    Rmap rmap = (Rmap) rmaps.lastElement();
	    stringR +=
		((rmap.getName() == null)  ?
		 "" :
		 rmap.getName()) +
		((rmap.getTrange() == null) ?
		 "" :
		 rmap.getTrange().toString()) +
		((idx == payload) ? "(payload)" : "") +
		((idx == timeValues) ? "(times)" : "");
	}

	return (stringR);
    }
}
