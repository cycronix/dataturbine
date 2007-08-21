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

import com.rbnb.utility.SortedVector;
import com.rbnb.utility.SortException;
import com.rbnb.utility.ToString;

/**
 * Represents a time-relative request within the RBNB server.
 * <p>
 * Requests have a time reference and a list of channels.  They can:
 * <p><ul>
 *    <li>End before the time (BEFORE),</li>
 *    <li>End at or before the time (AT-OR-BEFORE),</li>
 *    <li>Start at or after the time (AT-OR-AFTER), or</li>
 *    <li>Start after the time (AFTER).</li>
 * </ul>
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataRequest
 * @since V2.2
 * @version 12/11/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/10/2003  INB	Created.
 *
 */
final class TimeRelativeRequest {

    /**
     * request starts after the reference time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #AT
     * @see #AT_OR_AFTER
     * @see #AT_OR_BEFORE
     * @see #BEFORE
     * @since V2.2
     * @version 10/10/2003
     */
    public final static byte AFTER = DataRequest.GREATER;

    /**
     * request starts at or after the reference time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #AFTER
     * @see #AT_OR_BEFORE
     * @see #BEFORE
     * @since V2.2
     * @version 10/10/2003
     */
    public final static byte AT_OR_AFTER = DataRequest.GREATER_EQUAL;

    /**
     * request starts at the reference time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/06/2003
     */
    public final static byte AT = DataRequest.EQUAL;

    /**
     * request ends at or before the reference time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #AFTER
     * @see #AT_OR_AFTER
     * @see #BEFORE
     * @since V2.2
     * @version 10/10/2003
     */
    public final static byte AT_OR_BEFORE = DataRequest.LESS_EQUAL;

    /**
     * request ends before the reference time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #AFTER
     * @see #AT_OR_AFTER
     * @see #AT_OR_BEFORE
     * @since V2.2
     * @version 10/10/2003
     */
    public final static byte BEFORE = DataRequest.LESS;

    /**
     * the list of channel references by channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/10/2003
     */
    private SortedVector byChannel = new SortedVector
	(TimeRelativeChannel.SORT_CHANNEL_NAME);

    /**
     * the offset within the channel names to compare to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/05/2003
     */
    private int nameOffset = 0;

    /**
     * relationship to reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/10/2003
     */
    private byte relationship = AT_OR_BEFORE;

    /**
     * the <code>TimeRange</code> for the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/04/2003
     */
    private TimeRange timeRange = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public TimeRelativeRequest() {
	super();
    }

    /**
     * Adds a <code>TimeRelativeChannel</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelI the <code>TimeRelativeChannel</code>.
     * @exception com.rbnb.utility.SortException
     *		  if a sorting problem occurs.
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public final void addChannel(TimeRelativeChannel channelI)
	throws SortException
    {
	getByChannel().add(channelI);
    }

    /**
     * Factory method to build a <code>TimeRelativeRequest</code> from a
     * regular request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the regular request hierarchy.
     * @return the <code>TimeRelativeRequest</code> or <code>null</code> if the
     *	       regular request cannot be translated.
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
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.2
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    public final static TimeRelativeRequest createFromRequest(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRelativeRequest requestR = null;

	if (requestI instanceof DataRequest) {
	    DataRequest request = (DataRequest) requestI;
	    requestR = new TimeRelativeRequest();
	    requestR.setRelationship(request.getRelationship());

	    Rmap child = request.getChildAt(0);
	    while ((child.getTrange() == null) &&
		   (child.getNchildren() != 0)) {
		child = child.getChildAt(0);
	    }
	    requestR.setTimeRange(child.getTrange());

	    String[] names = request.extractNames();
	    TimeRelativeChannel trc;
	    for (int idx = 0; idx < names.length; ++idx) {
		trc = new TimeRelativeChannel();
		trc.setChannelName(names[idx]);
		try {
		    requestR.addChannel(trc);
		} catch (com.rbnb.utility.SortException e) {
		    throw new java.lang.InternalError();
		}
	    }
	}

	return (requestR);
    }

    /**
     * Compares this request to the input limits.
     * <p>
     *
     * @author Ian Brown
     *
     * @param limitsI the <code>DataArray</code> containing the data limits.
     * @return the direction to move:
     *         <br><ol start=-1>
     *		   <li>the request is for data before the limits,</li>
     *		   <li>the request is for data within the limits,</li>
     *		   <li>the request is for data after the limits, or</li>
     *		   <li>no limits were found.</li>
     *	       </ol>
     * @since V2.2
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/04/2003  INB	Created.
     *
     */
    public final int compareToLimits(DataArray limitsI) {
	int statusR = 2;
	double limitStart = limitsI.getStartTime();

	if (limitStart > -Double.MAX_VALUE) {
	    double limitEnd = limitStart + limitsI.getDuration();

/*
 * JPW debug print
try {
System.err.println(
"TimeRelativeRequest.compareToLimits():\n" +
"\tlimitStart = " + ToString.toString("%.17f",limitStart) + "\n" +
"\tlimitEnd = " + ToString.toString("%.17f",limitEnd) + "\n" +
"\tgetTimeRange().getTime() = " +
    ToString.toString("%.17f",getTimeRange().getTime()));
} catch (Exception e) {}
*
*/

	    if (getTimeRange().getTime() < limitStart) {
		statusR = -1;
	    } else if (getTimeRange().getTime() > limitEnd) {
		statusR = 1;
	    } else if (!((TimeRange) limitsI.timeRanges.lastElement
			 ()).getInclusive() &&
		       (getTimeRange().getTime() == limitEnd)) {
		statusR = 1;
	    } else {
		statusR = 0;
	    }
	}

	return (statusR);
    }

    /**
     * Compares this request to the input <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeRangeI the <code>TimeRange</code>.
     * @return the direction to move:
     *         <br><ol start=-1>
     *		   <li>the request is for data before the range,</li>
     *		   <li>the request is for data within the range, or</li>
     *		   <li>the request is for data after the range.</li>
     *	       </ol>
     * @since V2.2
     * @version 11/05/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/04/2003  INB	Created.
     *
     */
    public final int compareToTimeRange(TimeRange timeRangeI) {
	int statusR = 0;
	double trStart = timeRangeI.getTime();
	double trEnd =
	    timeRangeI.getPtimes()[timeRangeI.getNptimes() - 1] +
	    timeRangeI.getDuration();

	if (getTimeRange().getTime() < trStart) {
	    statusR = -1;

	} else if (getTimeRange().getTime() > trEnd) {
	    statusR = 1;

	} else if (!timeRangeI.getInclusive() &&
		   (getTimeRange().getTime() == trEnd)) {
	    statusR = 1;
	}

	return (statusR);
    }

    /**
     * Determines the next time reference for this request based on the input
     * match.
     * <p>
     *
     * @author Ian Brown
     *
     * @param matchI the matching regular request.
     * @return the time reference or <code>NaN</code> if no single reference is
     *	       found.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Rmap</code> hierarchy contains
     *		  information that cannot be processed by this code.
     * @since V2.2
     * @version 11/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    public final double determineNextReference(Rmap matchI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRelativeChannel trc;
	double referenceR;

	// Get the time for the first channel as a starting point.
	trc = (TimeRelativeChannel) getByChannel().firstElement();
	referenceR = trc.determineNextReference(matchI,this);

	double reference;
	for (int idx = 1;
	     !Double.isNaN(referenceR) && (idx < getByChannel().size());
	     ++idx) {
	    trc = (TimeRelativeChannel) getByChannel().elementAt(idx);
	    reference = trc.determineNextReference(matchI,this);

	    if (Double.isNaN(reference)) {
		referenceR = Double.NaN;
	    } else if (reference != referenceR) {
		referenceR = Double.NaN;
	    }
	}
	
	return (referenceR);
    }

    /**
     * Finds a <code>TimeRelativeChannel</code> by its channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelNameI the name of the channel to find.
     * @return the <code>TimeRelativeChannel</code>.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating the channel.
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public final TimeRelativeChannel findByChannel(String channelNameI)
	throws SortException
    {
	return ((TimeRelativeChannel) getByChannel().find(channelNameI));
    }

    /**
     * Gets the list of channel time references by channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of channel time references.
     * @see #setByChannel(com.rbnb.utility.SortedVector byChannelI)
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public final SortedVector getByChannel() {
	return (byChannel);
    }

    /**
     * Gets the offset within the channel names.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the offset.
     * @see #setNameOffset(int nameOffsetI)
     * @since V2.2
     * @version 11/05/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/05/2003  INB	Created.
     *
     */
    public final int getNameOffset() {
	return (nameOffset);
    }

    /**
     * Gets the relationship to the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the relationship.
     * @see #AFTER
     * @see #AT_OR_AFTER
     * @see #AT_OR_BEFORE
     * @see #BEFORE
     * @see #setRelationship(byte relationshipI)
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public final byte getRelationship() {
	return (relationship);
    }

    /**
     * Gets the <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>TimeRange</code>.
     * @see #setTimeRange(com.rbnb.api.TimeRange timeRangeI)
     * @since V2.2
     * @version 11/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/04/2003  INB	Created.
     *
     */
    public final TimeRange getTimeRange() {
	return (timeRange);
    }

    /**
     * Sets the list of channel time references by channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param byChannelI the list of channel time references.
     * @see #getByChannel()
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public final void setByChannel(SortedVector byChannelI) {
	byChannel = byChannelI;
    }

    /**
     * Sets the offset within the channel names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameOffsetI the offset.
     * @see #getNameOffset()
     * @since V2.2
     * @version 11/05/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/05/2003  INB	Created.
     *
     */
    public final void setNameOffset(int nameOffsetI) {
	nameOffset = nameOffsetI;
    }

    /**
     * Sets the relationship to the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @param relationshipI the relationship.
     * @see #AFTER
     * @see #AT_OR_AFTER
     * @see #AT_OR_BEFORE
     * @see #BEFORE
     * @see #getRelationship()
     * @since V2.2
     * @version 10/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/10/2003  INB	Created.
     *
     */
    public final void setRelationship(byte relationshipI) {
	relationship = relationshipI;
    }

    /**
     * Sets the <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeRangeI the <code>TimeRange</code>.
     * @see #getTimeRange()
     * @since V2.2
     * @version 11/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/04/2003  INB	Created.
     *
     */
    public final void setTimeRange(TimeRange timeRangeI) {
	timeRange = timeRangeI;
    }

    /**
     * Splits this request into separate requests, each having a unique next
     * name level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of split requests.
     * @since V2.2
     * @version 11/07/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     *
     */
    public final java.util.Vector splitByNameLevel() {
	java.util.Vector splitR = new java.util.Vector();
	TimeRelativeRequest trr = new TimeRelativeRequest();
	TimeRelativeChannel trc;
	String part = null;
	String cPart;

	trr.setNameOffset(getNameOffset());
	trr.setRelationship(getRelationship());
	trr.setTimeRange(getTimeRange());
	for (int idx = 0; idx < getByChannel().size(); ++idx) {
	    // Loop through the channels and figure out which ones go in which
	    // groups.  Note that they are sorted, so we can depend on a change
	    // in a name indicating a new, unique group.
	    trc = (TimeRelativeChannel) getByChannel().elementAt(idx);
	    cPart = trc.nextNameLevel(getNameOffset());

	    if (cPart != null) {
		// If this channel has a name, then we want to keep it for the
		// output group.

		if ((part != null) && cPart.equals(part)) {
		    // If the name hasn't changed, then just add the channel to
		    // the current group.
		    try {
			trr.addChannel(trc);
		    } catch (com.rbnb.utility.SortException e) {
			throw new InternalError();
		    }

		} else {
		    // If the name has changed, then we need to make a new
		    // group.

		    if (part != null) {
			// If there was a group being worked on, then split
			// here.
			splitR.addElement(trr);
			trr = new TimeRelativeRequest();
			trr.setNameOffset(getNameOffset());
			trr.setRelationship(getRelationship());
			trr.setTimeRange(getTimeRange());
		    }

		    try {
			trr.addChannel(trc);
		    } catch (com.rbnb.utility.SortException e) {
			throw new InternalError();
		    }
		    part = cPart;
		}
	    }
	}

	if (trr.getByChannel().size() > 0) {
	    // If there are channels in the last request, add it to the return
	    // list.
	    splitR.addElement(trr);
	}

	return (splitR);
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @since V2.2
     * @version 11/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/03/2003  INB	Created.
     *
     */
    public final String toString() {
	String stringR =
	    "TimeRelativeRequest: " +
	    getRelationship() + " " + getTimeRange();
	TimeRelativeChannel trc;

	for (int idx = 0; idx < getByChannel().size(); ++idx) {
	    trc = (TimeRelativeChannel) getByChannel().elementAt(idx);
	    stringR += "\n  " + trc;
	}

	return (stringR);
    }
}
