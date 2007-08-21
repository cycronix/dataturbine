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
 * Response for a <code>TimeRelativeRequest</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.TimeRelativeRequest
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
 * 12/08/2003  INB	Handle <code>RequestOptions.extendStart</code> when
 *			building the request.
 * 11/04/2003  INB	Created.
 *
 */
final class TimeRelativeResponse {

    /**
     * invert the resulting request?
     * <p>
     * For cases where we grab the end time of something, we want to invert the
     * request handling so that it is exclusive start, inclusive end, rather
     * than the standard inclusive start, exclusive end.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/18/2003
     */
    private boolean invert = false;

    /**
     * the status.
     * <p>
     * Status values are:
     * <p><ol start=-2>
     *    <li>channels in request have different time bases,</li>
     *    <li>request is before the data object checked,</li>
     *    <li>request was matched in the data object, or</li>
     *    <li>request is after the data object checked.</li>
     * </ol>
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/04/2003
     */
    private int status = 0;

    /**
     * the time found.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/04/2003
     */
    private double time = 0.;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
    public TimeRelativeResponse() {
	super();
    }

    /**
     * Builds the regular request from this <code>TimeRelativeResponse</code>
     * and the input <code>TimeRelativeRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @param optionsI the <code>RequestOptions</code>.
     * @return the regular request <code>Rmap</code>.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
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
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Handle <code>RequestOptions.extendStart</code> when
     *			building the request.
     * 11/05/2003  INB	Created.
     *
     */
    final Rmap buildRequest(TimeRelativeRequest requestI,
			    RequestOptions optionsI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap requestR = new DataRequest();

	TimeRelativeChannel trc;
	Rmap trChild;
	Rmap child = null;
	for (int idx = 0; idx < requestI.getByChannel().size(); ++idx) {
	    trc = (TimeRelativeChannel)
		requestI.getByChannel().elementAt(idx);
	    trChild = Rmap.createFromName("/" + trc.getChannelName());
	    trChild.moveToBottom().setDblock(Rmap.MarkerBlock);
	    if (child == null) {
		child = trChild;
	    } else {
		child = child.mergeWith(trChild);
	    }
	}

	double ltime = getTime(); 
	double lduration = requestI.getTimeRange().getDuration();
	boolean ldirection = getInvert(); 
	if ((optionsI != null) && optionsI.getExtendStart()) {
	    if (ltime != 0.) {
		// This little kludge is due to the fact that there is a
		// calculation in <code>TimeRange.extractRequestWithData</code>
		// that rounds the start time up to the next point.  That
		// calculation depends on the difference between this time and
		// the start time of a block of data - a difference that can be
		// different than the time of the last point by a slight
		// amount.
		long lltime = Double.doubleToLongBits(ltime);
		lltime--;
		ltime = Double.longBitsToDouble(lltime);
	    }
	    lduration += requestI.getTimeRange().getTime() - ltime;
	    ldirection = false;

	} else {
	    switch (requestI.getRelationship()) {
	    case TimeRelativeRequest.BEFORE:
	    case TimeRelativeRequest.AT_OR_BEFORE:
		ltime -= lduration;
		break;
	    case TimeRelativeRequest.AFTER:
	    case TimeRelativeRequest.AT_OR_AFTER:
		break;
	    }
	}
	child.setTrange(new TimeRange(ltime,lduration));
	child.getTrange().setDirection(ldirection);
	requestR.addChild(child);

	return (requestR);
    }

    /**
     * Gets the invert flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return invert the request?
     * @see #setInvert(boolean invertI)
     * @since V2.2
     * @version 11/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/18/2003  INB	Created.
     *
     */
    public final boolean getInvert() {
	return (invert);
    }

    /**
     * Gets the status.
     * <p>
     * Status values are:
     * <p><ol start=-2>
     *    <li>channels in request have different time bases,</li>
     *    <li>request is before the data object checked,</li>
     *    <li>request was matched in the data object, or</li>
     *    <li>request is after the data object checked.</li>
     * </ol>
     * <p>
     *
     * @author Ian Brown
     *
     * @return the status.
     * @see #setStatus(int statusI)
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
    public final int getStatus() {
	return (status);
    }

    /**
     * Gets the time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time.
     * @see #setTime(double timeI)
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
    public final double getTime() {
	return (time);
    }

    /**
     * Sets the invert flag.
     * <p>
     * For cases where we grab the end time of something, we want to invert the
     * request handling so that it is exclusive start, inclusive end, rather
     * than the standard inclusive start, exclusive end.
     * <p>
     *
     * @author Ian Brown
     *
     * @param invertI invert the request?
     * @see #getInvert()
     * @since V2.2
     * @version 11/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/18/2003  INB	Created.
     *
     */
    public final void setInvert(boolean invertI) {
	invert = invertI;
    }

    /**
     * Sets the status.
     * <p>
     * Status values are:
     * <p><ol start=-2>
     *    <li>channels in request have different time bases,</li>
     *    <li>request is before the data object checked,</li>
     *    <li>request was matched in the data object, or</li>
     *    <li>request is after the data object checked.</li>
     * </ol>
     * <p>
     *
     * @author Ian Brown
     *
     * @param statusI the status.
     * @see #getStatus()
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
    public final void setStatus(int statusI) {
	status = statusI;
    }

    /**
     * Sets the time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI the time.
     * @see #getTime()
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
    public final void setTime(double timeI) {
	time = timeI;
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
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
    public final String toString() {
	return ("TimeRelativeResponse: " + getStatus() + " " + getTime());
    }
}

