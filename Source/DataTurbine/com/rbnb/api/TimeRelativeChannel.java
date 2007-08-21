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

import com.rbnb.utility.SortCompareInterface;
import com.rbnb.utility.SortException;

/**
 * Represents a channel for a <code>TimeRelativeRequest</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.TimeRelativeRequest
 * @since V2.2
 * @version 11/07/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
* 10/10/2003   INB	Created.
 *
 */
final class TimeRelativeChannel
    implements Cloneable,
	       SortCompareInterface
{
    /**
     * sort by channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/10/2003
     */
    public final static String SORT_CHANNEL_NAME = "name";

    /**
     * the name of the channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/10/2003
     */
    private String channelName = null;

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
    public TimeRelativeChannel() {
	super();
    }

    /**
     * Clones this <code>TimeRelativeChannel</code>.
     * <p>
     * This method clones the <code>ptimes</code> array.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.2
     * @version 11/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/03/2003  INB	Created.
     *
     */
    public final Object clone() {
	Object resultR = null;
	try {
	    resultR = super.clone();

	} catch (java.lang.CloneNotSupportedException e) {
	    throw new java.lang.InternalError();
	}

	return (resultR);
    }

    /**
     * Compares the sorting value of this <code>TimeRelativeChannel</code> to
     * the input sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * The sorting value for an <code>TimeRelativeChannel</code> depends on
     * the sort identifier.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p>less than 0 if this <code>TimeRelativeChannel</code> compares
     *		  less than the input,
     *	       <p>equals 0 if this <code>TimeRelativeChannel</code> compares
     *		  equal to the input, and
     *	       <p>greater than 0 if this <code>TimeRelativeChannel</code>
     *		  compares greater than the input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
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
    public final int compareTo
	(Object sidI,
	 Object otherI)
	throws SortException
    {
	String mine = (String) sortField(sidI);
	String other =
	    ((otherI instanceof String) ?
	     (String) otherI :
	     (String) ((TimeRelativeChannel) otherI).sortField(sidI));

	return (mine.compareTo(other));
    }

    /**
     * Determines the next time reference for this channel based on the input
     * match.
     * <p>
     *
     * @author Ian Brown
     *
     * @param matchI   the matching regular request.
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @return the time reference or <code>NaN</code> if no reference is found.
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
     * @version 11/07/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Created.
     * 2005/07/07  WHF  Added check to see if time range is null.
     *
     */
    public final double determineNextReference
	(Rmap matchI,
	 TimeRelativeRequest requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	double referenceR = Double.NaN;
	DataArray data = matchI.extract(getChannelName());

	if (data == null || data.getNumberOfPoints() == 0 
		|| data.timeRanges == null) {
	    // No data was found, so we have no reference.
	    referenceR = Double.NaN;

	} else {
	    // Otherwise, calculate the reference from the data.
	    double[] times;
	    TimeRange tr;

	    switch (requestI.getRelationship()) {
	    case TimeRelativeRequest.BEFORE:
	    case TimeRelativeRequest.AT_OR_BEFORE:
		// If we're backing up, then use the earliest time.
		referenceR = ((TimeRange)
			      data.timeRanges.firstElement()).getTime();
		break;

	    case TimeRelativeRequest.AT_OR_AFTER:
	    case TimeRelativeRequest.AFTER:
		// When moving forwards, we need to get the time of the last
		// point.
		tr = (TimeRange) data.timeRanges.lastElement();
		if (tr.getInclusive()) {
		    // With an inclusive <code>TimeRange</code>, simply use the
		    // last time.
		    referenceR =
			tr.getPtimes()[tr.getNptimes() - 1] +
			tr.getDuration();
		} else {
		    // Otherwise, we need to actually get the time for the last
		    // point.
		    times = data.getTime();
		    referenceR = times[times.length - 1];
		}
		break;
	    }
	}

	return (referenceR);
    }

    /**
     * Gets the name of the channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name of the channel.
     * @see #setChannelName(String channelNameI)
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
    public final String getChannelName() {
	return (channelName);
    }

    /**
     * Returns the next part of the channel name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameOffsetI the index to the start of the next part.
     * @return the next part of the name.
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
    public final String nextNameLevel(int nameOffsetI) {
	String nextLevelR = null;

	if (nameOffsetI < getChannelName().length()) {
	    int endIndex = getChannelName().indexOf("/",nameOffsetI + 1);

	    if (endIndex == -1) {
		endIndex = getChannelName().length();
	    }
	    nextLevelR = getChannelName().substring(nameOffsetI + 1,endIndex);
	}

	return (nextLevelR);
    }

    /**
     * Sets the name of the channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelNameI the channel name.
     * @see #getChannelName()
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
    public final void setChannelName(String channelNameI) {
	channelName = channelNameI;
    }

    /**
     * Gets the sorting value for this <code>TimeRelativeChannel</code>.
     * <p>
     * The sort identifier depends on the sort type identifier:
     * <p><ul>
     *    <li><code>SORT_CHANNEL_NAME</code> - the channel name field.</li>
     *    </ul>
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI  the sort type identifier.
     * @return the sort value.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is not legal.
     * @see #compareTo(Object,Object)
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
    public final Object sortField(Object sidI)
	throws SortException
    {
	if (sidI != SORT_CHANNEL_NAME) {
	    throw new com.rbnb.utility.SortException
		("The sort identifier is not valid.");
	}

	return (getChannelName());
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @since V2.2
     * @version 11/03/2003
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
	return ("TimeRelativeChannel: "	+ getChannelName());
    }
}
