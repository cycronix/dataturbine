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

import com.rbnb.utility.ToString;

/**
 * The range of times that applies to the data of an <code>Rmap</code>.
 * <p>
 * A <code>TimeRange</code> consists of:
 * <p><ul>
 * <li>A list of one or more time values, and</li>
 * <li>A duration greater than or equal to 0.</li>
 * </ul><p>
 * The number of time values in the <code>TimeRange</code> and the number of
 * data points must satisfy one of the following three cases:
 * <p><ol>
 * <li>number of times = number of points,</li>
 * <li>one time and any number of points, or</li>
 * <li>any number of times and one point.</li>
 * </ol><p>
 * When the number of times equals the number of points, the time for a point
 * is equal to the time corresponding to the point.
 * <p>
 * When there is a single time and a number of points larger than one, the time
 * for a point is equal to:
 * <p><pre>    time + (point #)/(number of points)</pre><p>
 * When there are multiple times and a single data point, all of the times
 * apply to the point. This is usually used when the "point" of data is
 * is actually a data pool. The children of the data pool are inheriting the
 * times.
 * <p>
 * All data points have a duration that is either:
 * <p><ul>
 * <li>The duration of the <code>TimeRange</code> (when there is just a single
 *     time in the <code>TimeRange</code>), or</li>
 * <li>The duration of the <code>TimeRange</code> divided by the number of
 *     data points.</li>
 * </ul><p>
 * <p>
 * <code>TimeRanges</code> can inherit time values and duration from other
 * <code>TimeRanges</code>. The way that the inheritance works is described
 * in the documentation for </code>Rmaps</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 05/19/2005
 */

/*
 * Copyright 2000, 2001, 2002, 2003, 2004, 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/19/2005  JPW/MJM	Fixed a bug in matchTimeRelative() where point number
 *			could be calculated incorrectly due to limited floating
 *			point precision.
 * 02/25/2004  INB	Added code to skip over duplicate points in
 *			<code>matchTimeRelative</code>.
 * 12/11/2003  INB	Added <code>afterTimeRelative</code> and
 *			<code>beforeTimeRelative</code>.  Reworked point index
 *			calculations in <code>extractRequestWithData</code> to
 *			reduce roundoff errors.
 * 11/14/2003  INB	Eliminate synchronization.
 * 11/05/2003  INB	Added <code>matchTimeRelative</code>.
 * 09/30/2003  INB	Cannot use duplicate in
 *			<code>extractRequestWithData</code>.  Also need to
 *			ensure that we don't try to copy the entire data
 *			array unless we actually need it all.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 03/27/2003  INB	When extracting all of the data in a
 *			<code>DataBlock</code>, duplicate it in
 *			<code>extractRequestWithData</code>.
 * 11/30/2000  INB	Created.
 *
 */
public final class TimeRange
    extends com.rbnb.api.Serializable
{
    // Public constants:
    /**
     * Duration value is inherited.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/06/2001
     */
    public static final double INHERIT_DURATION = Double.NaN;

    /**
     * Time values are inherited.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final double[] INHERIT_TIMES = null;

    /**
     * server time of day.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/23/2002
     */
    public final static TimeRange SERVER_TOD = new TimeRange
	(0.,
	 -Double.MAX_VALUE/10.);

    // Package constants:
    /**
     * Decreasing times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte DECREASING = 3;

    /**
     * Increasing times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte INCREASING = 2;

    /**
     * Random time ordering.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte RANDOM = 1;

    /**
     * Unknown time ordering.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte UNKNOWN = 0;

    private static final byte EQUAL = 0,
			      SUBSET = 1,
			      SUPERSET = 2,
			      OVERLAP = 3,
			      PAR_DUR = 0,
			      PAR_PTM = 1,
			      PAR_STA = 2,
			      PAR_INC = 3;

    private static final byte[][] MATCHES =
	{
	    { Rmap.MATCH_BEFORE, Rmap.MATCH_AFTER },
	    { Rmap.MATCH_BEFORENAME, Rmap.MATCH_AFTERNAME },
	    { Rmap.MATCH_NOINTERSECTION, Rmap.MATCH_NOINTERSECTION },
	    { Rmap.MATCH_EQUAL, Rmap.MATCH_EQUAL },
	    { Rmap.MATCH_SUBSET, Rmap.MATCH_SUPERSET },
	    { Rmap.MATCH_SUPERSET, Rmap.MATCH_SUBSET },
	    { Rmap.MATCH_INTERSECTION, Rmap.MATCH_INTERSECTION },
	    { Rmap.MATCH_AFTERNAME, Rmap.MATCH_BEFORENAME },
	    { Rmap.MATCH_AFTER, Rmap.MATCH_BEFORE }
	};

    private static final double TOLERANCE = (((1 << 4) - 1)/
					     ((double) (1L<<52))),
				LOW_TOLERANCE = (1. - TOLERANCE),
				HI_TOLERANCE = (1. + TOLERANCE);

    private static final String[] PARAMETERS = {
				    "DUR",
				    "PTM",
				    "STA",
				    "INC"
				};

    /**
     * how does time change across the range?
     * <p>
     * The values that this field can take are:
     * <p><ul>
     * <li>INCREASING - monotonically increasing,</li>
     * <li>DECREASING - monotonically descreasing,</li>
     * <li>RANDOM - time can go in any direction,<li>
     * <li>UNKNOWN - haven't determined direction yet.<li>
     * </ul><p>
     * <it>Note: at this point, <code>TimeRanges</code> must monotonically
     *     increase.</it>
     *
     * @author Ian Brown
     * @since V2.0
     * @version 09/20/2001
     */
    private byte changing = INCREASING;

    /**
     * the comparison direction.
     * <p>
     * This field is used by requests for <code>NEWEST</code> to indicate that
     * the comparison is to reverse its direction. It is somewhat of a kludge
     * due to the fact that I needed to fix things quickly.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/21/2002
     */
    private boolean direction = false;

    /**
     * duration (applies to all of the times).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private double duration = INHERIT_DURATION;

    /**
     * inclusive of both ends?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */
    private boolean inclusive = false;

    /**
     * time values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private double[] ptimes = INHERIT_TIMES;

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
     * 11/30/2000  INB	Created.
     *
     */
    public TimeRange() {
	super();
    }

    /**
     * Class constructor to build a <code>TimeRange</code> by reading it from
     * an input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @since V2.0
     * @version 07/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    TimeRange(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>TimeRange</code> by reading it from
     * an input stream.
     * <p>
     * This version takes an input <code>TimeRange</code> that is used to fill
     * in the default values for any fields that are not read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>TimeRange</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @since V2.0
     * @version 07/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    TimeRange(TimeRange otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Class constructor to build a <code>TimeRange</code> for a single time
     * and an inherited duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI the time.
     * @see #TimeRange(double,double)
     * @see #TimeRange(double[])
     * @see #TimeRange(double[],double)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public TimeRange(double timeI) {
	this();
	set(timeI);
	setInclusive(true);
    }

    /**
     * Class constructor to build a <code>TimeRange</code> from a single time
     * and duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startI	 the time.
     * @param durationI  the duration.
     * @see #TimeRange(double)
     * @see #TimeRange(double[])
     * @see #TimeRange(double[],double)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public TimeRange(double startI,double durationI) {
	this();
	set(startI,durationI);
	setInclusive((durationI == 0.) || (durationI == INHERIT_DURATION));
    }

    /**
     * Class constructor to build a <code>TimeRange</code> from an array of
     * times and an inherited duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptimesI  the individual point times.
     * @see #TimeRange(double)
     * @see #TimeRange(double,double)
     * @see #TimeRange(double[],double)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public TimeRange(double[] ptimesI) {
	this();
	set(ptimesI);
	setInclusive(true);
    }

    /**
     * Class constructor to build a <code>TimeRange</code> from an array of
     * times and a duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptimesI  the individual point times.
     * @see #TimeRange(double)
     * @see #TimeRange(double,double)
     * @see #TimeRange(double[])
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public TimeRange(double[] ptimesI,double durationI) {
	this();
	set(ptimesI,durationI);
	setInclusive((durationI == 0.) || (durationI == INHERIT_DURATION));
    }

    /**
     * Adds the input <code>TimeRange</code> to this one.
     * <p>
     * The resulting <code>TimeRange</code> contains a number of point time
     * values equal to the number in this <code>TimeRange</code> times
     * the number of in the input <code>TimeRange</code>.
     * <p>
     * The duration of the result is one of:
     * <p><ol>
     * <li>The input duration adjusted for the difference between the first
     *     time of the input <code>TimeRange</code> and that of the summed
     *     <code>TimeRange</code>, or</li>
     * <li>The minimum of:
     *	   <br><ol>
     *	   <li>The duration of this <code>TimeRange</code>, and
     *	   <li>The input duration adjusted for the difference between the first
     *	       time of the input <code>TimeRange</code> and the summed
     *	       <code>TimeRange</code>.</li>
     *     </ol>
     * </ol>
     *
     * @author Ian Brown
     *
     * @param otherI  the <code>TimeRange</code> to add.
     * @return the sum <code>TimeRange</code>.
     * @see #subtract(TimeRange)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    final TimeRange add(TimeRange otherI) {
	if (otherI == null) {
	    return (this);
	}
	if ((otherI.getPtimes() == INHERIT_TIMES) ||
	    (otherI.getDuration() == INHERIT_DURATION)) {
	    throw new java.lang.IllegalArgumentException
		("Cannot add " + otherI + " to " + this);
	}

	// Create the sum <code>TimeRange</code>.
	TimeRange sumR = new TimeRange();
	double mEnd =
	    (getDuration() == INHERIT_DURATION) ?
	    otherI.getDuration() :
	    getDuration(),
	       iEnd = otherI.getTime() + otherI.getDuration();

	if (getPtimes() == INHERIT_TIMES) {
	    // If this <code>TimeRange</code> inherits its times, grab those
	    // from the input.
	    sumR.setPtimes(otherI.getPtimes());
	    mEnd += sumR.getTime();

	} else {
	    // If both <code>TimeRanges</code> have times, add them together.
	    double[] sumTimes = new double[getNptimes()*otherI.getNptimes()];

	    mEnd += getTime() + otherI.getTime();
	    for (int idx = 0,
		     offset = 0;
		 idx < otherI.getNptimes();
		 ++idx) {
		double base = otherI.getPtimes()[idx];

		for (int idx1 = 0;
		     idx1 < getNptimes();
		     ++idx1,
			 ++offset) {
		    sumTimes[offset] = base + getPtimes()[idx1];
		}
	    }
	    sumR.setPtimes(sumTimes);
	}

	// Calculate the duration.
	sumR.setDuration(Math.min(mEnd,iEnd) - sumR.getTime());
	sumR.setInclusive(getInclusive() || otherI.getInclusive());

	return (sumR);
    }

    /**
     * Adds the input time limits to this limit <code>TimeRange</code>.
     * <p>
     * A limit <code>TimeRange</code> has a single point time (the minimum)
     * plus a duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param limitsI  the minimum and maximum values to add.
     * @since V2.0
     * @version 09/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2001  INB	Created.
     *
     */
    final void addLimits(double[] limitsI) {
	double minimum = Math.min(getPtimes()[0],limitsI[0]),
	       maximum = Math.max(getPtimes()[0] + getDuration(),limitsI[1]);

	set(minimum,maximum - minimum);
    }

    /**
     * Adds the <code>TimeRange</code> to limit <code>TimeRange</code>.
     * <p>
     * The limits of the input <code>TimeRange</code> are calculated. That
     * value is then used:
     * <p><ol>
     * <li>to increase the range of the input limit <code>TimeRange</code>,
     *	   or</li>
     * <li>as the limits if there is no input limit
     *	   <code>TimeRange</code>.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param limitI  the limit <code>TimeRange</code>.
     * @param tRangeI  the <code>TimeRange</code>.
     * @return the updated limit <code>TimeRange</code>.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2001  INB	Created.
     *
     */
    static final TimeRange addLimits(TimeRange limitI,TimeRange tRangeI) {
	TimeRange limitR = limitI;

	// Get the limits of the input <code>TimeRange</code>.
	double[] limits = tRangeI.getLimits();

	if (limitR == null) {
	    // If there is no limit <code>TimeRange</code>, return the
	    // calculated limits.
	    limitR = new TimeRange(limits[0],limits[1] - limits[0]);
	    limitR.setInclusive(tRangeI.getInclusive());

	} else {
	    // Update the limit <code>TimeRange</code>.
	    limitR.addLimits(limits);

	    if (limitR.getLimits()[1] == limits[1]) {
		limitR.setInclusive(tRangeI.getInclusive());
	    }
	}

	return (limitR);
    }

    /**
     * Adds the input <code>TimeRange</code> as an offset to this one and
     * returns the result in a new <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param iRangeI   the offset <code>TimeRange</code>.
     * @param needAllI  do we need all of the times?
     * @return the new <code>TimeRange</code>.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    private final TimeRange addOffset(TimeRange iRangeI,boolean needAllI) {

	// Determine the output duration. It is either the same as the duration
	// for this <code>TimeRange</code> or it is inherited from the input
	// <code>TimeRange</code>.
	double rDuration = getDuration();
	if (rDuration == INHERIT_DURATION) {
	    rDuration = iRangeI.getDuration();
	}

	TimeRange tRangeR = null;

	if (!needAllI) {
	    // If we need just the first time value from this
	    // <code>TimeRange</code>, add it to the input
	    // <code>TimeRange's</code> value.
	    tRangeR = new TimeRange
		(getTime() + iRangeI.getTime(),
		 rDuration);

	} else {
	    // If we need all of the time values from this
	    // <code>TimeRange</code>, clone the array and add the in the input
	    // <code>TimeRange's</code> time value.
	    double[] ltimes = (double[]) getPtimes().clone();

	    for (int idx = 0; idx < ltimes.length; ++idx) {
		ltimes[idx] += iRangeI.getTime();
	    }

	    tRangeR = new TimeRange(ltimes,rDuration);
	}
	tRangeR.setInclusive(getInclusive() || iRangeI.getInclusive());

	return (tRangeR);
    }

    /**
     * Adds an increment to the times of this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param incrementI  the increment to add.
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    final void addToTimes(double incrementI) {
	for (int idx = 0; idx < getNptimes(); ++idx) {
	    getPtimes()[idx] += incrementI;
	}
    }

    /**
     * Builds a request for data for the requested channels starting at the
     * beginning of this <code>TimeRange</code> (which is after the time
     * reference).
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
     * @param nPointsI  the number of data points.
     * @return the <code>TimeRelativeResponse</code>.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
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
     * @see #beforeTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions roI,int nPointsI)
     * @since V2.2
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2003  INB	Created.
     *
     */
    TimeRelativeResponse afterTimeRelative(TimeRelativeRequest requestI,
					   RequestOptions roI,
					   int nPointsI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("TimeRange.afterTimeRelative: " + this +
			   "\nAgainst: " + requestI +
			   " " + roI +
			   " " + nPointsI);
	*/

	// This method actually exists purely for the possibility that in the
	// future we may want to implement <code>extendEnd</code>.  Right now,
	// it simply returns the start time of the <code>TimeRange</code>.
	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(0);
	responseR.setTime(getTime());
	responseR.setInvert(false);

	/*
	System.err.println("TimeRange.afterTimeRelative: " + this +
			   "\nAgainst: " + requestI +
			   " " + roI +
			   " " + nPointsI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds a request for data for the requested channels ending at the
     * end of this <code>TimeRange</code> (which is before the time reference).
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
     * @param dBlockI   the <code>DataBlock</code>.
     * @return the <code>TimeRelativeResponse</code>.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
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
     * @see #beforeTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions roI,int nPointsI)
     * @since V2.2
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2003  INB	Created.
     *
     */
    TimeRelativeResponse beforeTimeRelative(TimeRelativeRequest requestI,
					    RequestOptions roI,
					    int nPointsI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("TimeRange.beforeTimeRelative: " + this +
			   "\nAgainst: " + requestI +
			   " " + roI +
			   " " + nPointsI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(0);

	if ((roI != null) && roI.getExtendStart()) {
	    // For <code>extendStart</code> requests, we want the start time of
	    // the last point.
	    responseR.setTime(getPointTime(nPointsI - 1,nPointsI));
	    responseR.setInvert(false);

	} else {
	    // For non-<code>extendStart</code> requests, we want the end time
	    // of the last point.
	    responseR.setTime(getPtimes()[getNptimes() - 1] + getDuration());
	    responseR.setInvert(true);
	}

	/*
	System.err.println("TimeRange.beforeTimeRelative: " + this +
			   "\nAgainst: " + requestI +
			   " " + roI +
			   " " + nPointsI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds a new <code>TimeRange</code> containing information inherited
     * from this one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needAllI  do we need all of the times?
     * @return the new <code>TimeRange</code>.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    private final TimeRange buildRange(boolean needAllI) {
	TimeRange tRangeR = null;

	if (needAllI) {
	    // If we need all of the times, just return a clone of this
	    // <code>TimeRange</code>.
	    tRangeR = (TimeRange) clone();

	} else {
	    // Otherwise, create a new <code>TimeRange</code> consisting of the
	    // first time value and the duration of this one.
	    tRangeR = new TimeRange(getTime(),getDuration());
	    tRangeR.setInclusive(getInclusive());
	}

	return (tRangeR);
    }

    /**
     * Clones this <code>TimeRange</code>.
     * <p>
     * This method clones the <code>ptimes</code> array.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 04/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final Object clone() {
	TimeRange clonedR = (TimeRange) super.clone();

	if (clonedR != null) {
	    if (getPtimes() != null) {
		clonedR.setPtimes((double[]) getPtimes().clone());
	    }
	}

	return (clonedR);
    }

    /**
     * Compares two time intervals to determine their relationship.
     * <p>
     *
     * @author Ian Brown
     *
     * @param oneIdxI		the index of the first time interval.
     * @param oneLowI		the low value for the first time interval.
     * @param oneHighI		the high value for the first time interval.
     * @param allowOneHighI	the first high value is in the interval?
     * @param oneIntersectedIO  intersected flags for first intervals.
     * @param twoIdxI		the index of the second time interval.
     * @param twoLowI		the low value for the second time interval.
     * @param twoHighI		the high value for the second time interval.
     * @param allowTwoHighI	the second high value is in the interval?
     * @param twoIntersectedIO  intersected flags for second intervals.
     * @param numMatchesIO	the numbers of each type of match. Zero or one
     *				of these is incremented.
     * @since V2.0
     * @version 01/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private static final void compareTimeIntervals(int oneIdxI,
						   double oneLowI,
						   double oneHighI,
						   boolean allowOneHighI,
						   boolean[] oneIntersectedIO,
						   int twoIdxI,
						   double twoLowI,
						   double twoHighI,
						   boolean allowTwoHighI,
						   boolean[] twoIntersectedIO,
						   int[] numMatchesIO)
    {
	// Count the number of intervals that are equal, the number where the
	// first interval is a subset of the second the number where the first
	// interval is a superset of the second, and the number where the two
	// overlap.
	if (oneLowI < twoLowI) {
	    if (oneHighI >= twoHighI) {
		++numMatchesIO[SUPERSET];
		oneIntersectedIO[oneIdxI] =
		    twoIntersectedIO[twoIdxI] = true;
	    } else if ((oneHighI > twoLowI) ||
		       (allowOneHighI && (oneHighI == twoLowI))) {
		++numMatchesIO[OVERLAP];
		oneIntersectedIO[oneIdxI] =
		    twoIntersectedIO[twoIdxI] = true;
	    }
	} else if (oneHighI > twoHighI) {
	    if ((oneLowI < twoHighI) ||
		(allowTwoHighI && (oneLowI == twoHighI))) {
		if (oneLowI == twoLowI) {
		    ++numMatchesIO[SUPERSET];
		    oneIntersectedIO[oneIdxI] =
			twoIntersectedIO[twoIdxI] = true;
		} else {
		    ++numMatchesIO[OVERLAP];
		    oneIntersectedIO[oneIdxI] =
			twoIntersectedIO[twoIdxI] = true;
		}
	    }
	} else if (oneLowI == twoLowI) {
	    if (oneHighI == twoHighI) {
		++numMatchesIO[EQUAL];
		oneIntersectedIO[oneIdxI] =
		    twoIntersectedIO[twoIdxI] = true;
	    } else {
		++numMatchesIO[SUBSET];
		oneIntersectedIO[oneIdxI] =
		    twoIntersectedIO[twoIdxI] = true;
	    }
	} else {
	    ++numMatchesIO[SUBSET];
	    oneIntersectedIO[oneIdxI] =
		twoIntersectedIO[twoIdxI] = true;
	}
    }

    /**
     * Compares this <code>TimeRange</code> to the input one.
     * <p>
     * The only way that two <code>TimeRanges</code> can be considered "equal"
     * is if they have exactly the same values. Otherwise, one is always
     * considered to come "before" the other. The comparison depends on exactly
     * what fields are set and how they are set in the two
     * <code>TimeRanges</code>. The rules are:
     * <p>
     * <li>Compare the times of the two <code>TimeRanges</code> in order. If
     *     any of the times for one is less than the corresponding time for
     *     for the other, then that <code>TimeRange</code> is less than the
     *     other. If one <code>TimeRange</code> runs out of times before the
     *     other does, then it is less than the other.</li>
     * <li>If the duration for one <code>TimeRange</code> is inherited or
     *     less than the other, then that <code>TimeRange</code> is less than
     *     the other.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>TimeRange</code>.
     * @return the results of the comparison:
     * <p> <0 if this <code>TimeRange</code> is less than the input,
     * <p> =0 if the two <code>TimeRange</code>s are equal, or
     * <p> >0 if this <code>TimeRange</code> is greater than the input.
     * @since V2.0
     * @version 10/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int compareTo(TimeRange otherI) {
	int compareR = 0;

	for (int idx = 0; (compareR == 0) ; ++idx) {
	    // Check all of the times in order.

	    if (idx == getNptimes()) {
		// If this <code>TimeRange</code> has run out of times, it
		// is less than or equal to the input at least in terms of
		// times.
		if (idx != otherI.getNptimes()) {
		    // If the input <code>TimeRange</code> has not run out
		    // of times, then this one is less than the input.
		    compareR = -1;
		}
		break;

	    } else if (idx == otherI.getNptimes()) {
		// If the input <code>TimeRange</code> has run out of
		// times, it is less than this one.
		compareR = 1;
		break;
	    }

	    double difference = getPtimes()[idx] - otherI.getPtimes()[idx];
	    if (difference < 0.) {
		// If a time from this <code>TimeRange</code> is less than
		// the corresponding time from the input, then this
		// <code>TimeRange</code> is less than the input.
		compareR = -1;

	    } else if (difference > 0.) {
		// If a time from this <code>TimeRange</code> is greater
		// than the corresponding time from the input, then this
		// <code>TimeRange</code> is greather than the input.
		compareR = 1;
	    }
	}

	if (compareR == 0) {
	    // If the times compared equal, then the duration decides
	    // things.  Since <code>INHERIT_DURATION</code> is negative,
	    // this subtraction will show that value as less than any
	    // other.
	    double difference = getDuration() - otherI.getDuration();

	    if (difference < 0.) {
		// If the duration for this <code>TimeRange</code> is less
		// than that for the input, then this
		// <code>TimeRange</code> is less than the input.
		compareR = -1;

	    } else if (difference > 0.) {
		// If the duration for this <code>TimeRange</code> is
		// greater than that for the input, then this
		// <code>TimeRange</code> is greater than the input.
		compareR = 1;
	    }
	}

	return (compareR);
    }

    /**
     * Copies this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the copy.
     * @since V2.0
     * @version 07/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2001  INB	Created.
     *
     */
    final TimeRange duplicate() {
	return ((TimeRange) clone());
    }

    /**
     * Copies the times for all of the points in this range to an array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI  the number of data points that the
     *			<code>TimeRange</code> describes. This can be one of
     *			the following:
     *			<p><ol>
     *			<li>Equal to the value returned by
     *			    <code>getNptimes()</code>,</li>
     *			<li>Any positive value if <code>getNptimes()</code> is
     *			    1, or</li>
     *			<li>0, in which case it is assumed to be equal to
     *			    <code>getNptimes()</code>.<li>
     *			</ol>
     * @param timeI     the time array to copy the points into.
     * @param startAtI  the starting index into the time array.
     * @return the number of points copied.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>nPointsI < 0, or</li>
     *		  <li>nPointsI != <code>getNptimes()</code> and the latter is
     *		      not equal to 1.</li>
     *		  </ul>
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
    public final int copyTimes(int nPointsI,
			       double[] timeI,
			       int startAtI)
    {
	if (getNptimes() == 0) {
	    throw new IllegalStateException
		("Inherited time values are not available.");
	} else if (nPointsI < 0) {
	    throw new IllegalArgumentException
		("Negative input number of points.");
	} else if ((getNptimes() != 1) &&
		   (nPointsI != 0) &&
		   (nPointsI != getNptimes())) {
	    throw new IllegalArgumentException
		("The number of input points does not match the number of " +
		 "times.");
	}

	int nPointsR = Math.max(nPointsI,getNptimes());

	if (nPointsR == getNptimes()) {
	    System.arraycopy
		(getPtimes(),
		 0,
		 timeI,
		 startAtI,
		 nPointsR);
	} else {
	    for (int idx = 0; idx < nPointsR; ++idx) {
		timeI[startAtI + idx] = getPointTime(idx,nPointsR);
	    }
	}

	return (nPointsR);
    }

    /**
     * Merges the input <code>TimeRange</code> and this <code>TimeRange</code>
     * to produce a new <code>TimeRange</code> that will be used as offsets for
     * the next level of inheritance.
     * <p>
     * The result of the merge is a <code>TimeRange</code> containing one time
     * value for each data point in the input <code>DataBlock</code> times the
     * number of data points in the <code>DataBlock</code> corresponding to
     * this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param iRangeI  the <code>TimeRange</code> inherited from above.
     * @param iBlockI  the <code>DataBlock</code> inherited from above.
     * @param dBlockI  the <code>DataBlock</code> corresponding to this
     *		       <code>TimeRange</code>.
     * @return the new <code>TimeRange</code>.
     * @see #mergeOffsets(TimeRange,DataBlock)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    private final TimeRange dataOffsets
	(TimeRange iRangeI,
	 DataBlock iBlockI,
	 DataBlock dBlockI)
    {
	TimeRange tRangeR = null;

	if ((getDuration() == INHERIT_DURATION) &&
	    (dBlockI.getNpts() != 1)) {
	    throw new java.lang.IllegalStateException
		("Need a duration to be able to extract the times " +
		 "for more than one point from a parent data pool.");
	}

	try {
	    double ltimes[] = new double[iBlockI.getNpts()*dBlockI.getNpts()];
	    double rDuration = 0.;

	    if (getPtimes() == INHERIT_TIMES) {
		// If this <code>TimeRange</code> inherits its time values from
		// the input <code>TimeRange</code>, then produce a new
		// <code>TimeRange</code> from the input, the
		// <code>DataBlock</code> for this <code>TimeRange</code>.

		// The duration for each of the points in the output
		// <code>TimeRange</code> will be equal to the interval between
		// the points in the <code>TimeRange</code>.
		int dNpts = dBlockI.getNpts();
		rDuration = getDuration()/dNpts;

		for (int idx = 0,
			 idx2 = 0,
			 iNpts = iBlockI.getNpts();
		     idx < iNpts;
		     ++idx) {
		    // Grab the time for the inherited <code>TimeRange's</code>
		    // point.
		    double itime = iRangeI.getPointTime(idx,iNpts);

		    for (int idx1 = 0;
			 idx1 < dNpts;
			 ++idx1,
			     ++idx2,
			     itime += rDuration) {
			// The time for this point is equal to the time for the
			// previous point (or the time from the inherited
			// <code>TImeRange</code>) plus the output duration.
			ltimes[idx2] = itime;
		    }
		}
		
	    } else {
		// If this <code>TimeRange</code> has its own time values, then
		// merge the two using the <code>DataBlock</code> corresponding
		// to this <code>TimeRange</code>.

		// The output duration is going to be 0 because we've used up
		// the information at this level.

		for (int idx = 0,
			 idx2 = 0,
			 iNpts = iBlockI.getNpts(),
			 dNpts = dBlockI.getNpts();
		     idx < iNpts;
		     ++idx) {
		    // Grab the time for the inherited <code>TimeRange's</code>
		    // point.
		    double itime = iRangeI.getPointTime(idx,iNpts);

		    for (int idx1 = 0;
			 idx1 < dNpts;
			 ++idx1,
			     ++idx2) {
			// Grab the time for this <code>TimeRange's</code>
			// point.
			double ltime = getPointTime(idx1,dNpts);

			// Add the two to produce the output value.
			ltimes[idx2] = itime + ltime;
		    }
		}
	    }

	    // Create a new <code>TimeRange</code> from the new time values
	    // list and the calculated duration.
	    tRangeR = new TimeRange(ltimes,rDuration);
	    tRangeR.setInclusive(getInclusive());

	} catch (IllegalStateException e) {
	    throw new java.lang.IllegalStateException(e.getMessage());
	    
	} catch (IllegalArgumentException e) {
	    throw new java.lang.IllegalStateException(e.getMessage());
	}

	return (tRangeR);
    }

    /**
     * Determines the result of the comparison between two
     * <code>TimeRanges</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param modeI	      the comparison mode:
     *			      <p><ol start=0>
     *			      <li>Second <code>TimeRange</code> is the request,
     *				  or</li>
     *			      <li>First <code>TimeRange</code> is the
     *				  request.</li> 
     *			      </ol>
     * @param oneIntervalsI    the number of intervals in the first
     *			       <code>TimeRange</code>.
     * @param oneLowestI       the lowest value from the first
     *			       <code>TimeRange</code>.
     * @param oneHighestI      the highest value from the first
     *			       <code>TimeRange</code>.
     * @param allowOneHighI	the first high value is in the interval?
     * @param oneIntersectedI  intersected flags for first intervals.
     * @param twoIntervalsI    the number of intervals in the second
     *			       <code>TimeRange</code>.
     * @param twoLowestI       the lowest value from the second
     *			       <code>TimeRange</code>.
     * @param twoHighestI      the highest value from the second
     *			       <code>TimeRange</code>.
     * @param allowTwoHighI	the second high value is in the interval?
     * @param twoIntersectedI  intersected flags for second intervals.
     * @param numMatchesI      the number of matches of each type.
     * @return the results of the match:
     *	       <p><ul>
     *	       <li>Rmap.MATCH_EQUAL - the two compare equal,</li>
     *	       <li>Rmap.MATCH_SUBSET - the <code>TimeRange</code> is contained
     *		   in the request,</li>
     *	       <li>Rmap.MATCH_SUPERSET - the <code>TimeRange</code> contains
     *		   the request,</li>
     *	       <li>Rmap.MATCH_INTERSECTION - the <code>TimeRange</code> and
     *		   the request contain common regions,</li>
     *	       <li>Rmap.MATCH_BEFORE - the <code>TimeRange</code> is before
     *		   the request,</li>
     *	       <li>Rmap.MATCH_AFTER - the <code>TimeRange</code> is after the
     *		   request, or</li>
     *	       <li>Rmap.MATCH_NOINTERSECTION - the <code>TimeRange</code> and
     *		   the overlap, but do not have any common regions.</li>
     *	       </ul>
     * @since V2.0
     * @version 01/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private static final byte determineResults(int modeI,
					       int oneIntervalsI,
					       double oneLowestI,
					       double oneHighestI,
					       boolean allowOneHighI,
					       boolean[] oneIntersectedI,
					       int twoIntervalsI,
					       double twoLowestI,
					       double twoHighestI,
					       boolean allowTwoHighI,
					       boolean[] twoIntersectedI,
					       int[] numMatchesI)
    {
	// Determine if the two can possibly be equal or have more than an
	// intersection.
	boolean canBeEqual = ((numMatchesI[EQUAL] == oneIntervalsI) &&
			      (numMatchesI[EQUAL] == twoIntervalsI)),
		canSubset = ((numMatchesI[SUBSET] > 0) &&
			     (numMatchesI[SUPERSET] == 0)),
		canSuperset = (!canSubset && (numMatchesI[SUPERSET] > 0));

	for (int idx = 0;
	     (canBeEqual || canSubset) && (idx < oneIntervalsI);
	     ++idx) {
	    if (!oneIntersectedI[idx]) {
		canBeEqual =
		    canSubset = false;
	    }
	}

	for (int idx = 0;
	     (canBeEqual || canSuperset) && (idx < twoIntervalsI);
	     ++idx) {
	    if (!twoIntersectedI[idx]) {
		canBeEqual =
		    canSuperset = false;
	    }
	}

	// Now determine the result. Assume that there is no intersection,
	// but that the time limits of the two <code>TimeRanges</code>
	// overlap.
	byte matchesR = Rmap.MATCH_NOINTERSECTION;

	if (canBeEqual) {
	    // If we still think the two <code>TimeRanges</code> could be
	    // equal, then they are.
	    matchesR = Rmap.MATCH_EQUAL;

	} else if (canSubset) {
	    // If we still think this <code>TimeRange</code> could be a
	    // subset of the request, then it must be one.
	    matchesR = Rmap.MATCH_SUBSET;

	} else if (canSuperset) {
	    // If we still think this <code>TimeRange</code> could be a
	    // superset of the request, then it must be one.
	    matchesR = Rmap.MATCH_SUPERSET;

	} else if ((numMatchesI[EQUAL] > 0) ||
		   (numMatchesI[SUBSET] > 0) ||
		   (numMatchesI[SUPERSET] > 0) ||
		   (numMatchesI[OVERLAP] > 0)) {
	    // If there was any intersection found between the two
	    // <code>TimeRanges</code>, then that is the result.
	    matchesR = Rmap.MATCH_INTERSECTION;

	} else if ((oneHighestI < twoLowestI) ||
		   (!allowOneHighI && (oneHighestI == twoLowestI))) {
	    // If the first <code>TimeRange</code> comes entirely before the
	    // second, then that is the result.
	    matchesR = Rmap.MATCH_BEFORE;

	} else if ((oneLowestI > twoHighestI) ||
		   (!allowTwoHighI && (oneLowestI == twoHighestI))) {
	    // If the first <code>TimeRange</code> comes entirely after the
	    // second, then that is the result.
	    matchesR = Rmap.MATCH_AFTER;
	}

	// Adjust for the mode.
	matchesR = MATCHES[matchesR - Rmap.MATCH_BEFORE][modeI];

	return (matchesR);
    }

    /**
     * Extends the range of this <code>TimeRange</code> by adding the input
     * <code>TimeRange</code> to the end.
     * <p>
     * This method determines whether or not the input <code>TimeRange</code>
     * is compatible with this one. To be compatible, the two
     * <code>TimeRanges</code> must satisfy the one of the following:
     * <p><ul>
     * <li>both <code>TimeRanges</code> must have multiple start times and
     *     equal durations, or</li>
     * <li>both <code>TimeRanges</code> must have a single start time and a
     *     duration such that the start time of the input
     *     <code>TimeRange</code> is equal to the start time + duration of this
     *     <code>TimeRange</code> and the interval between points in the two
     *     <code>TimeRanges</code> are equal.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param myPointsI the number of data points represented by this
     *			<code>TimeRange</code>.
     * @param otherI	the other <code>TimeRange</code>.
     * @param oPointsI  the number of data points represented by the input
     *			<code>TimeRange</code>.
     * @return were the <code>TimeRanges</code> compatible? If this is
     *	       <code>false</code>, then this <code>TimeRange</code> is left
     *	       unchanged, otherwise this <code>TimeRange</code> is extended to
     *	       include the input.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/16/2001  INB	Created.
     *
     */
    public final boolean extend(int myPointsI,
				TimeRange otherI,
				int oPointsI)
    {
	boolean compatibleR = false;
	int myPtimes = getNptimes(),
	    oPtimes = otherI.getNptimes();
	double myD = getDuration(),
	       oD = otherI.getDuration();

	if ((myPtimes == myPointsI) &&
	    (oPtimes == oPointsI) &&
	    ((myPtimes != 1) || (myD == 0.)) &&
	    ((oPtimes != 1) || (oD == 0.))) {
	    // If the two <code>TimeRanges</code> each have one time value per
	    // data point, then we simply need to compare the durations.

	    if (compatibleR = ((oD >= myD*LOW_TOLERANCE) &&
			       (oD <= myD*HI_TOLERANCE))) {
		// If the durations are equal, then we can extend this
		// <code>TimeRange</code>.
		double[] newTimes = new double[myPtimes + oPtimes];
		System.arraycopy(getPtimes(),
				 0,
				 newTimes,
				 0,
				 myPtimes);
		System.arraycopy(otherI.getPtimes(),
				 0,
				 newTimes,
				 myPtimes,
				 oPtimes);
		setPtimes(newTimes);
		setInclusive(getInclusive() || otherI.getInclusive());
		return (true);
	    }

	} else if ((myPtimes == 1) && (oPtimes == 1)) {
	    // If the two <code>TimeRanges</code> each consist of a single
	    // start time and duration, then we need to determine if the input
	    // directly extends this.
	    double myS = getTime(),
		   myE = myS + myD,
		   oS = otherI.getTime();

	    if ((oS >= myE*LOW_TOLERANCE) && (oS <= myE*HI_TOLERANCE)) {
		// If the start time of the input is equal to the end time of
		// this, then we have a potential match. We also need to check
		// the interval between points.
		double myI = myD/myPointsI,
		       oI = oD/oPointsI;

		if (compatibleR = ((oI >= myI*LOW_TOLERANCE) &&
				   (oI <= myI*HI_TOLERANCE))) {
		    // If the intervals are equal, then we can extend this
		    // <code>TimeRange</code> by simply adding in the input
		    // duration.
		    setDuration(myD + oD);
		    setInclusive(getInclusive() || otherI.getInclusive());
		}
	    }
	}

	return (compatibleR);
    }

    /**
     * Extracts inherited information from this <code>TimeRange</code> and the
     * input <code>TimeRange</code> to produce a new inherited
     <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needAllI  are all the time values needed?
     * @param iRangeI   the <code>TimeRange</code> inherited from above.
     * @param iBlockI   the <code>DataBlock</code> inherited from above.
     * @param dBlockI   the <code>DataBlock</code> corresponding to this
     *		        <code>TimeRange</code>.
     * @return the new <code>TimeRange</code>.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB  Created.
     *
     */
    final TimeRange extractInheritance
	(boolean needAllI,
	 TimeRange iRangeI,
	 DataBlock iBlockI,
	 DataBlock dBlockI)
    {
	TimeRange tRangeR = null;

	if (iRangeI == null) {
	    // If there is no inherited <code>TimeRange</code>, we need to
	    // build a <code>TimeRange</code> directly from this one.
	    tRangeR = buildRange(needAllI);

	} else if (iBlockI == null) {
	    // If the data payload was not inherited, we need to build a
	    // <code>TimeRange</code> from this one using the inherited
	    // <code>TimeRange</code> as a time offset (and possibly grabbing
	    // its duration),
	    tRangeR = addOffset(iRangeI,needAllI);

	} else {
	    // If we inherited both a <code>TimeRange</code> and a
	    // <code>DataBlock</code>, then we need to build a new
	    // <code>TimeRange</code> from this <code>TimeRange</code> and the
	    // inherited one based on information in the
	    // <code>DataBlocks</code>.
	    tRangeR = extractOffsets(iRangeI,iBlockI,dBlockI);
	}

	return (tRangeR);
    }

    /**
     * Extracts inherited offsets from the input <code>TimeRange</code> and
     * combines them with the values from this <code>TimeRange</code> to
     * produce a new <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param iRangeI  the <code>TimeRange</code> inherited from above.
     * @param iBlockI  the <code>DataBlock</code> inherited from above.
     * @param dBlockI  the <code>DataBlock</code> corresponding to this
     *		       <code>TImeRange</code>.
     * @return the new <code>TimeRange</code>.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    private final TimeRange extractOffsets
	(TimeRange iRangeI,
	 DataBlock iBlockI,
	 DataBlock dBlockI)
    {
	TimeRange tRangeR = null;

	if (dBlockI == null) {
	    // If there is no <code>DataBlock</code> corresponding to this
	    // <code>TimeRange</code>, then this is supposed to be just an
	    // offset.
	    tRangeR = mergeOffsets(iRangeI,iBlockI);

	} else {
	    // If there is a <code>DataBlock</code> corresponding to this
	    // <code>TimeRange</code>, then we need to produce a time value for
	    // every point in the <code>DataBlock</code>.
	    tRangeR = dataOffsets(iRangeI,iBlockI,dBlockI);
	}

	return (tRangeR);
    }

    /**
     * Extracts the portion of this <code>TimeRange</code> that matches the
     * request <code>TimeRange</code>.
     * <p>
     * The method handles the case where there is a second
     * <code>TimeRange</code> corresponding to this one that also needs to
     * have a subset made.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>TimeRange</code>.
     * @param otherI	a corresponding <code>TimeRange</code>.
     * @param rangeO	the extracted <code>TimeRange</code>.
     * @param otherO	the other extracted <code>TimeRange</code>.
     * @return was anything extracted?
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/19/2000  INB	Created.
     *
     */
    final boolean extractRequest(TimeRange requestI,
				 TimeRange otherI,
				 TimeRange rangeO,
				 TimeRange otherO)
    {
	return (extractRequestWithData
		(requestI,
		 otherI,
		 null,
		 rangeO,
		 otherO,
		 null));
    }

    /**
     * Extracts the portion of this <code>TimeRange</code> that matches the
     * request <code>TimeRange</code> that corresponds to actual data.
     * <p>
     * The method handles the case where there is a second
     * <code>TimeRange</code> corresponding to this one that also needs to
     * have a subset made.
     * <p>
     * This method also handles requests for just time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>TimeRange</code>.
     * @param otherI	a corresponding <code>TimeRange</code>.
     * @param dBlockI	the <code>DataBlock</code> for this
     *			<code>TimeRange</code>.
     * @param rangeO	the extracted <code>TimeRange</code>.
     * @param otherO	the other extracted <code>TimeRange</code>.
     * @param dBlockO	the resulting <code>DataBlock</code>.
     * @return was anything extracted?
     * @since V2.0
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     *			Reworked point index calculations to reduce roundoff
     *			errors.
     * 09/30/2003  INB	Duplicate won't work.  We need to copy the fields.
     *			In addition, when working with individual point times,
     *			we need to extract data when the number of resulting
     *			times is not equal to the original number, not the
     *			number of values.
     * 03/27/2003  INB	When extracting the entire <code>DataBlock</code>,
     *			simply duplicate it.
     * 12/19/2000  INB	Created.
     *
     */
    final boolean extractRequestWithData(TimeRange requestI,
 					 TimeRange otherI,
					 DataBlock dBlockI,
					 TimeRange rangeO,
					 TimeRange otherO,
					 DataBlock dBlockO)
    {
	if (requestI == null) {
	    throw new java.lang.IllegalStateException
		("Cannot perform extraction without a request.");
	} else if (requestI.getNptimes() != 1) {
	    throw new java.lang.IllegalStateException
		("Cannot perform extraction of multiple time values " +
		 "at this time.");
	} else if (getDuration() == INHERIT_DURATION) {
	    throw new java.lang.IllegalStateException
		("Cannot perform extraction without a duration at this time.");
	}

	boolean reversed = (getDirection() != requestI.getDirection());

	DataBlock dRef = null;
	int nPoints = (dBlockI == null) ? 1 : dBlockI.getNpts();

	if (getNptimes() == 1) {
	    // With a single time, we can produce times using a contiguous
	    // group of values.
	    int sp = 0,
		ep = 0;
	    double myDuration = getDuration();

	    if (myDuration == 0.) {
		// With a zero duration, we want all of the points.
		sp = 0;
		ep = nPoints - 1;

	    } else {
		// If the duration exists, use to determine what points to get.
		int myNpoints;
		double myStart,
		    requestStart,
		    requestDuration = requestI.getDuration();

		if (reversed) {
		    myStart = -(getPtimes()[0] + myDuration);
		    myNpoints = -nPoints;
		    requestStart =
			-(requestI.getPtimes()[0] + requestDuration);
		} else {
		    myStart = getPtimes()[0];
		    myNpoints = nPoints;
		    requestStart = requestI.getPtimes()[0];
		}
		
		// Calculate the starting data point from the request start,
		// the start and duration of this <code>TimeRange</code>, and
		// the number of data points.
		double tOffset = requestStart - myStart,
		    pIndex = (myNpoints*tOffset)/myDuration;

		if (reversed) {
		    ep = (int) Math.min
			(nPoints - 1,
			 Math.max(0,Math.ceil(nPoints - 1 + pIndex)));

		} else if (requestI.getDuration() != 0.) {
		    sp = (int) Math.ceil(pIndex);

		} else {
		    sp = (int) Math.floor(pIndex);
		}

		// Calculate the ending data point from the request start and
		// duration, the start and duration of this
		// <code>TimeRange</code>, and the number of data points.
		tOffset += requestDuration;
		pIndex = (myNpoints*tOffset)/myDuration;

		if (reversed) {
		    if (!requestI.isInclusive() &&
			(pIndex == Math.ceil(pIndex))) {
			++pIndex;
		    }

		    sp = (int) Math.ceil(nPoints - 1 + pIndex);
		} else {
		    if (!requestI.isInclusive() &&
			(pIndex == Math.ceil(pIndex))) {
			--pIndex;
		    }

		    ep = (int) Math.floor(pIndex);
		}

		if (sp < 0) {
		    sp = 0;
		} else if (sp >= nPoints) {
		    return (false);
		}

		if (ep < 0) {
		    return (false);
		} else if (ep >= nPoints) {
		    ep = nPoints - 1;
		}
	    }

	    if (ep < sp) {
		return (false);
	    }

	    // Create a new <code>TimeRange</code> from the calculate start
	    // point and end point.
	    rangeO.set(getPtimes()[0] + (sp*myDuration)/nPoints,
		       ((ep - sp + 1)*myDuration)/nPoints);

	    if (dBlockI != null) {
		if ((sp == 0) &&
		    (ep == nPoints - 1) &&
		    (ep == dBlockI.getNpts())) {
		    // When extracting the entire thing, simply duplicate.
		    dBlockO.set(dBlockI.getData(),
				dBlockI.getNpts(),
				dBlockI.getPtsize(),
				dBlockI.getDtype(),
				dBlockI.getMIMEType(),
				dBlockI.getWorder(),
				dBlockI.getIndivFlg(),
				dBlockI.getOffset(),
				dBlockI.getStride());

		} else {
		    // If there is data, extract it by setting up a pair of
		    // temporary <code>DataBlocks</code> to extract the proper
		    // information.
		    DataBlock ndBlock;

		    ndBlock = new DataBlock(null,
					    (ep - sp) + 1,
					    dBlockI.getPtsize(),
					    dBlockI.getDtype(),
					    dBlockI.getMIMEType(),
					    dBlockI.getWorder(),
					    dBlockI.getIndivFlg(),
					    (dBlockI.getOffset() +
					     sp*dBlockI.getStride()),
					    dBlockI.getStride());
		    ndBlock.setData(dBlockI.getData());

		    // Create a data reference <code>DataBlock</code> to
		    // extract a  single point. The number of points comes from
		    // the input data block.
		    dRef = new DataBlock(null,
					 1,
					 dBlockI.getPtsize(),
					 dBlockI.getDtype(),
					 dBlockI.getMIMEType(),
					 dBlockI.getWorder(),
					 dBlockI.getIndivFlg(),
					 0,
					 dBlockI.getPtsize());

		    // Extract the data payload.
		    dBlockO.set(ndBlock.extractData(dRef),
				ndBlock.getNpts(),
				dBlockI.getPtsize(),
				dBlockI.getDtype(),
				dBlockI.getMIMEType(),
				dBlockI.getWorder(),
				dBlockI.getIndivFlg(),
				0,
				dBlockI.getPtsize());
		}
	    }

	    if (otherO != null) {
		// If we are extracting from a second <code>TimeRange</code>,
		// do so here.
		if (otherI.getNptimes() == 1) {
		    otherO.set((otherI.getTime() +
				sp*otherI.getDuration()/nPoints),
			       (((ep - sp) + 1)*
				otherI.getDuration()/nPoints));
		} else {
		    double[] otherTimes = new double[(ep - sp) + 1];
		    for (int idx = sp; idx <= ep; ++idx) {
			otherTimes[idx - sp] =
			    otherI.getPointTime(idx,nPoints);
		    }
		    otherO.set(otherTimes,otherI.getDuration());
		}
	    }

	} else {
	    // With a time per data point, we determine which points fall
	    // within the range and extract them.
	    java.util.Vector values = new java.util.Vector(),
			     ovalues = ((otherO == null) ?
					null :
					new java.util.Vector());
	    double rMin = requestI.getPtimes()[0],
		   rMax = rMin + requestI.getDuration();

	    for (int idx = 0; idx < getNptimes(); ++idx) {
		// Add each point that matches.
		double pMin = getPtimes()[idx],
		       pMax = pMin + getDuration();

		if ((pMin <= rMax) && (pMax >= rMin)) {
		    // If the point's range of times overlaps the request's
		    // range of times, then the point is desired.
		    values.addElement(new Double(pMin));
		    values.addElement(new Integer(idx));
		    if (ovalues != null) {
			ovalues.addElement
			    (new Double(otherI.getPointTime(idx,
							    getNptimes())));
		    }
		}
	    }

	    if (values.size() == 0) {
		throw new java.lang.IllegalStateException
		    ("No points matched?");
	    }
	    
	    // Create the new times array.
	    double[] nPtimes = new double[values.size()/2],
		     oPtimes = ((ovalues == null) ?
				null :
				new double[nPtimes.length]);

	    for (int idx = 0,
		     idx1 = 0;
		 idx < values.size();
		 idx += 2,
		     ++idx1) {
		nPtimes[idx1] = ((Double) values.elementAt(idx)).doubleValue();
		if (oPtimes != null) {
		    oPtimes[idx1] = ((Double)
				     ovalues.elementAt(idx1)).doubleValue();
		}
	    }

	    // Create the new <code>TimeRange</code>.
	    rangeO.set(nPtimes,getDuration());
	    if (otherO != null) {
		otherO.set(oPtimes,
			   ((otherI.getNptimes() == 1) ?
			    otherI.getDuration()/nPoints :
			    otherI.getDuration()));
	    }

	    if (dBlockI != null) {
		if ((nPtimes.length == getNptimes()) &&
		    (getNptimes() == dBlockI.getNpts())) {
		    // When extracting the entire thing, simply duplicate.
		    dBlockO.set(dBlockI.getData(),
				dBlockI.getNpts(),
				dBlockI.getPtsize(),
				dBlockI.getDtype(),
				dBlockI.getMIMEType(),
				dBlockI.getWorder(),
				dBlockI.getIndivFlg(),
				dBlockI.getOffset(),
				dBlockI.getStride());

		} else {
		    // If there is data, extract each of the desired data
		    // points. We create a temporary <code>DataBlock</code>
		    // that has a reference to a single point.
		    java.util.Vector points = new java.util.Vector();
		    DataBlock ndBlock;
		    ndBlock = new DataBlock(null,
					    1,
					    dBlockI.getPtsize(),
					    dBlockI.getDtype(),
					    dBlockI.getMIMEType(),
					    dBlockI.getWorder(),
					    dBlockI.getIndivFlg(),
					    0,
					    dBlockI.getPtsize());
		    ndBlock.setData(dBlockI.getData());

		    // Create a data reference <code>DataBlock</code> to
		    // extract a  single point. The number of points comes from
		    // the input data block.
		    dRef = new DataBlock(null,
					 1,
					 dBlockI.getPtsize(),
					 dBlockI.getDtype(),
					 dBlockI.getMIMEType(),
					 dBlockI.getWorder(),
					 dBlockI.getIndivFlg(),
					 0,
					 dBlockI.getPtsize());

		    for (int idx = 1; idx < values.size(); idx += 2) {
			// For each point of interest, update the offset of the
			// working <code>DataBlock</code> and then extract the
			// data value.
			int vIdx =
			    ((Integer) values.elementAt(idx)).intValue();
			ndBlock.setOffset(dBlockI.getOffset() +
					  vIdx*dBlockI.getStride());
			Object element = ndBlock.extractData(dRef);
		    
			points.addElement(element);
		    }

		    // Build the result <code>DataBlock</code>.
		    dBlockO.set(points,
				values.size()/2,
				dBlockI.getPtsize(),
				dBlockI.getDtype(),
				dBlockI.getMIMEType(),
				dBlockI.getWorder(),
				dBlockI.getIndivFlg(),
				0,
				dBlockI.getPtsize());
		}
	    }
	}

	/*
	System.err.println("\nExtracting with data:" +
			   "\nRequest: " + requestI +
			   "\nOther: " + otherI +
			   "\nDBlock: " + dBlockI +
			   "\nThis: " + this +
			   "\nRangeO: " + rangeO +
			   "\nOtherO: " + otherO +
			   "\nDBlockO: " + dBlockO);
	*/

	return (true);
    }

    /**
     * Determines how time is changing across this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return how time is changing:
     *	       <p><ul>
     *	       <li>INCREASING - monotonically increasing,</li>
     *	       <li>DECREASING - monotonically descreasing,</li>
     *	       <li>RANDOM - time can go in any direction,<li>
     *	       <li>UNKNOWN - haven't determined direction yet.<li>
     *	       </ul><p>
     * @since V2.0
     * @version 11/14//2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Eliminated synchronization.
     * 11/30/2000  INB	Created.
     *
     */
    final byte getChanging() {
	if ((changing == UNKNOWN) && (getNptimes() > 0)) {
	    // We only have to have point times to actually figure the
	    // direction of the values.

	    if (getNptimes() == 1) {
		// With exactly one point, the times are increasing, since
		// the duration is always positive or zero.
		changing = INCREASING;

	    } else {
		// Otherwise, we need to determine the direction.
		changing =
		    (getPtimes()[1] >= getPtimes()[0]) ?
		    INCREASING :
		    DECREASING;
	    }

	    for (int idx = 2;
		 (changing != RANDOM) && (idx < getNptimes());
		 ++idx) {
		byte pChanging =
		    (getPtimes()[idx] >= getPtimes()[idx - 1]) ?
		    INCREASING :
		    DECREASING;

		if (pChanging != changing) {
		    changing = RANDOM;
		}
	    }
	}

	return (changing);
     }

    /**
     * Gets the comparison direction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the direction flag (true means reversed).
     * @see #setDirection(boolean)
     * @since V2.0
     * @version 05/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/21/2002  INB	Created.
     *
     */
    final boolean getDirection() {
	return (direction);
    }

    /**
     * Gets the duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the duration.
     * @see #set(double,double)
     * @see #set(double[],double)
     * @see #setDuration(double)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final double getDuration() {
	return (duration);
    }

    /**
     * Gets the inclusive flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>TimeRange</code> inclusive of both ends?
     * @see #setInclusive(boolean)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    final boolean getInclusive() {
	return (inclusive);
    }

    /**
     * Gets the minimum and maximum times of this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the minimum and maximum times of this <code>TimeRange</code> as
     * an array. 
     * @since V2.0
     * @version 11/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final double[] getLimits() {
	if (getNptimes() == 0) {
	    // With no times, there are no limits.
	    return (null);
	}

	double addDuration = getDuration();
	if (addDuration == INHERIT_DURATION) {
	    addDuration = 0.;
	}

	double[] valuesR = new double[2];

	if (changing == INCREASING) {
	    valuesR[0] = getPtimes()[0];
	    valuesR[1] = (getPtimes()[getNptimes() - 1] +
			  addDuration);

	} else if (changing == DECREASING) {
	    valuesR[1] =
		(valuesR[0] = getPtimes()[0]) + addDuration;

	} else {
	    valuesR[1] =
		(valuesR[0] = getPtimes()[0]) + addDuration;

	    for (int idx = 1; idx < getNptimes(); ++idx) {
		double min = getPtimes()[idx],
		    max = min + addDuration;

		valuesR[0] = Math.min(valuesR[0],min);
		valuesR[1] = Math.max(valuesR[1],max);
	    }
	}

	return (valuesR);
    }

    /**
     * Gets the number of point times in a <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of point times.
     * @see #set(double)
     * @see #set(double,double)
     * @see #set(double[])
     * @see #set(double[],double)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getNptimes() {
	return ((ptimes == null) ? 0 : ptimes.length);
    }

    /**
     * Gets the time value for a particular point.
     * <p>
     * The time value equals one of:
     * <p><ol>
     * <li>The time at the point index, or</li>
     * <li><code>getTime()</code> +
     *     <code>getDuration()</code>*pointI/nPointsI.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param pointI    the desired point.
     * @param nPointsI  the number of data points that the
     *			<code>TimeRange</code> describes. This can be one of
     *			the following:
     *			<p><ol>
     *			<li>Equal to the value returned by
     *			    <code>getNptimes()</code>,</li>
     *			<li>Any positive value if <code>getNptimes()</code> is
     *			    1, or</li>
     *			<li>0, in which case it is assumed to be equal to
     *			    <code>getNptimes()</code>.<li>
     *			</ol>
     * @return the time.
     * @exception java.lang.IllegalStateException
     *		  thrown if <code>getNptimes()</code> is zero.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>nPointsI < 0,</li>
     *		  <li>nPointsI != <code>getNptimes()</code> and the latter is
     *		      not equal to 1, or</li>
     *		  <li>0 <= pointI < nPointsI is not true.</li>
     *		  </ul>
     * @since V2.0
     * @version 01/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final double getPointTime(int pointI,int nPointsI) {
	if (getNptimes() == 0) {
	    throw new IllegalStateException
		("Inherited time values are not available to compute time.");
	} else if (nPointsI < 0) {
	    throw new IllegalArgumentException
		("Negative input number of points.");
	} else if ((getNptimes() != 1) &&
		   (nPointsI != 0) &&
		   (nPointsI != getNptimes())) {
	    throw new IllegalArgumentException
		("The number of input points does not match the number of " +
		 "times.");
	}

	int nPoints = Math.max(nPointsI,getNptimes());

	if ((pointI < 0) || (pointI >= nPoints)) {
	    throw new IllegalArgumentException
		("Point is not in range: 0 <= " + pointI + " < " + nPoints +
		 ".");
	}

	double valueR;
	if (getNptimes() == 1) {
	    // With a single time, calculate the point time from the time,
	    // duration, and number of data points.
	    valueR = getTime() + getDuration()*pointI/nPoints;

	} else {
	    // With individual times, grab the appropriate one.
	    valueR = getPtimes()[pointI];
	}

	return (valueR);
    }

    /**
     * Gets the individual point times array.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the point times array.
     * @see #set(double[])
     * @see #set(double[],double)
     * @see #setPtimes(double[])
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final double[] getPtimes() {
	return (ptimes);
    }

    /**
     * Gets the time of the range.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time.
     * @since V2.0
     * @version 04/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final double getTime() {
	return (ptimes[0]);
    }

    /**
     * Inclusive of both ends of range?
     * <p>
     * If the <code>duration</code> of the <code>TimeRange</code> is zero (0),
     * then the range is inclusive.
     *
     * @author Ian Brown
     *
     * @return does this <code>TimeRange</code> include both the start and end
     *	       time, or just the one nearest the reference of a request?
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/28/2002  INB	Created.
     *
     */
    public boolean isInclusive() {
	return (getInclusive() ||
		(getDuration() == 0.) ||
		(getDuration() == INHERIT_DURATION));
    }

    /**
     * Matches this <code>TimeRange</code> against a request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>TimeRange</code>.
     * @return the results of the match:
     *	       <p><ul>
     *	       <li>Rmap.MATCH_EQUAL - the two compare equal,</li>
     *	       <li>Rmap.MATCH_SUBSET - this <code>TimeRange</code> is contained
     *		   in the request,</li>
     *	       <li>Rmap.MATCH_SUPERSET - this <code>TimeRange</code> contains
     *		   the request,</li>
     *	       <li>Rmap.MATCH_INTERSECTION - this <code>TimeRange</code> and
     *		   the request contain common regions,</li>
     *	       <li>Rmap.MATCH_BEFORE - this <code>TimeRange</code> is before
     *		   the request,</li>
     *	       <li>Rmap.MATCH_AFTER - this <code>TimeRange</code> is after the
     *		   request, or</li>
     *	       <li>Rmap.MATCH_NOINTERSECTION - this <code>TimeRange</code> and
     *		   the overlap, but do not have any common regions.</li>
     *	       </ul>
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
    final byte matches(TimeRange requestI) {
	byte matchesR = Rmap.MATCH_EQUAL;
	boolean reversed = getDirection() || requestI.getDirection();

//	System.err.println("mjm getDir: "+getDirection()+", IgetDir: "+requestI.getDirection());

	if ((requestI.getPtimes() == INHERIT_TIMES) ||
	    (getPtimes() == INHERIT_TIMES)) {
	    // If the request or this <code>TimeRange</code> inherits its
	    // times, then we have to assume an intersection between the two
	    // <code>TimeRanges</code>.
	    matchesR = Rmap.MATCH_INTERSECTION;
	}

	// Grab the durations to use. An inherited duration is treated as a
	// duration of 0.
	double myDuration = getDuration(),
	       requestDuration = requestI.getDuration();

	boolean allowMyHigh = isInclusive(),
	    allowRequestHigh = requestI.isInclusive() || allowMyHigh;

	// mjm grope 12/21/05
	//	if(requestDuration == 0.) allowMyHigh = true;  // nope

	if (myDuration == INHERIT_DURATION) {
	    myDuration = 0.;
	}
	if (requestDuration == INHERIT_DURATION) {
	    requestDuration = 0.;
	}

	// If either of the <code>TimeRanges</code> has monotonic increasing or
	// decreasing time values, then we can take advantage of that fact to
	// reduce the number of comparisons we need to make.
	byte positive = reversed ? DECREASING : INCREASING,
	    negative = reversed ? INCREASING : DECREASING;
	if (getChanging() == positive) {
	    matchesR = matchMonotonic(reversed,
				      0,
				      0,
				      1,
				      myDuration,
				      allowMyHigh,
				      requestI,
				      requestDuration,
				      allowRequestHigh);

	} else if (getChanging() == negative) {
//	    System.err.println("mjm allowMyHigh: "+allowMyHigh);
	    matchesR = matchMonotonic(reversed,
				      0,
				      getNptimes() - 1,
				      -1,
				      myDuration,
				      allowMyHigh,
				      requestI,
				      requestDuration,
				      allowRequestHigh);

	} else if (requestI.getChanging() == positive) {
	    matchesR = requestI.matchMonotonic(reversed,
					       1,
					       0,
					       1,
					       requestDuration,
					       allowRequestHigh,
					       this,
					       myDuration,
					       allowMyHigh);

	} else if (requestI.getChanging() == negative) {
	    matchesR = requestI.matchMonotonic(reversed,
					       1,
					       requestI.getNptimes() - 1,
					       -1,
					       requestDuration,
					       allowRequestHigh,
					       this,
					       myDuration,
					       allowMyHigh);


	} else {
	    // If neither <code>TimeRange</code> is known to have a monotonic
	    // relationship for its times, then we'll have to compare them all
	    // against each other.
	    matchesR = matchRandom(reversed,
				   myDuration,
				   allowMyHigh,
				   requestI,
				   requestDuration,
				   allowRequestHigh);
	}

	if (reversed) {
	    matchesR = MATCHES[matchesR - Rmap.MATCH_BEFORE][1];
	}
	
	/* mjm
	System.err.println("TimeRange.matches(): Compare: " + this +
			   " to " + requestI +
			   " = " + matchesR);
	*/
	
	return (matchesR);
    }

    /**
     * Matches the monotonic time list (increasing or decreasing) of this
     * <code>TimeRange</code> against another <code>TimeRange's</code> time
     * list.
     * <p>
     * This method determines whether or not the input <code>TimeRange</code>
     * is monotonic. If so, the method can minimize the amount of comparison
     * work to be done. If not, we can still reduce the comparison work by
     * using a binary search on this <code>TimeRange</code>.
     * <p>
     * It is assumed by this code that one of the two <code>TimeRanges</code>
     * is from a source <code>Rmap</code> and one is from a request. The
     * <code>modeI</code> parameter tells us which is which.
     * <p>
     * If the direction is reversed, then we negate the times and durations.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reversedI	      is the comparison direction reversed?
     * @param modeI	      the comparison mode:
     *			      <p><ol start=0>
     *			      <li>Input <code>TimeRange</code> is the request,
     *				  or</li>
     *			      <li>This <code>TimeRange</code> is the
     *				  request.</li> 
     *			      </ol>
     * @param startI	      the starting index into this
     *			      <code>TimeRange's</code> time list.
     * @param incrementI      the increment (-1 or 1) to compute succeeding
     *			      indexes.
     * @param myDurationI     the duration for this <code>TimeRange</code>.
     * @param allowMyHighI    allow my "high" value to be equal?
     * @param otherI	      the other <code>TimeRange</code>.
     * @param otherDurationI  the duration for the other
     *			      <code>TimeRange</code>.
     * @param allowOtherHighI allow the other "high" value to be equal?
     * @return the results of the match:
     *	       <p><ul>
     *	       <li>Rmap.MATCH_EQUAL - the two compare equal,</li>
     *	       <li>Rmap.MATCH_SUBSET - the <code>TimeRange</code> is contained
     *		   in the request,</li>
     *	       <li>Rmap.MATCH_SUPERSET - the <code>TimeRange</code> contains
     *		   the request,</li>
     *	       <li>Rmap.MATCH_INTERSECTION - the <code>TimeRange</code> and
     *		   the request contain common regions,</li>
     *	       <li>Rmap.MATCH_BEFORE - the <code>TimeRange</code> is before
     *		   the request,</li>
     *	       <li>Rmap.MATCH_AFTER - the <code>TimeRange</code> is after the
     *		   request, or</li>
     *	       <li>Rmap.MATCH_NOINTERSECTION - the <code>TimeRange</code> and
     *		   the overlap, but do not have any common regions.</li>
     *	       </ul>
     * @since V2.0
     * @version 05/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
    private final byte matchMonotonic(boolean reversedI,
				      int modeI,
				      int startI,
				      int incrementI,
				      double myDurationI,
				      boolean allowMyHighI,
				      TimeRange otherI,
				      double otherDurationI,
				      boolean allowOtherHighI)
    {
	byte matchesR = Rmap.MATCH_EQUAL,
	    positive = reversedI ? DECREASING : INCREASING,
	    negative = reversedI ? INCREASING : DECREASING;

	// If the other <code>TimeRange</code> is also monotonically increasing
	// or decreasing, we can really take some shortcuts.
	if (otherI.getChanging() == positive) {
	    matchesR = matchMonotonic2
		(reversedI,
		 modeI,
		 startI,
		 incrementI,
		 myDurationI,
		 allowMyHighI,
		 0,
		 1,
		 otherI,
		 otherDurationI,
		 allowOtherHighI);

	} else if (otherI.getChanging() == negative) {
	    matchesR = matchMonotonic2
		(reversedI,
		 modeI,
		 startI,
		 incrementI,
		 myDurationI,
		 allowMyHighI,
		 otherI.getNptimes() - 1,
		 -1,
		 otherI,
		 otherDurationI,
		 allowOtherHighI);

	} else {
	    // If the other <code>TimeRange</code> is not known to be
	    // monotonically increasing or decreasing, then we have to do some
	    // more work.
	    matchesR = matchMonotonicRandom
		(reversedI,
		 modeI,
		 startI,
		 incrementI,
		 myDurationI,
		 allowMyHighI,
		 otherI,
		 otherDurationI,
		 allowOtherHighI);
	}

	return (matchesR);
    }

    /**
     * Matches this <code>TimeRange</code> against the input
     * <code>TimeRange</code> when both <code>TimeRanges</code> are
     * monotonically increasing or decreasing.
     * <p>
     * With both <code>TimeRanges</code> having monotonically changing values,
     * we can move through the input one and use a binary search on this one,
     * with the minimum value of the binary search being increased as we work
     * through the entries in the other <code>TimeRange</code>.
     * <p>
     * It is assumed by this code that one of the two <code>TimeRanges</code>
     * is from a source <code>Rmap</code> and one is from a request. The
     * <code>modeI</code> parameter tells us which is which.
     * <p>
     * If the direction is reversed, then we negate the times and durations.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reversedI	      is the comparison direction reversed?
     * @param modeI	       the comparison mode:
     *			       <p><ol start=0>
     *			       <li>Input <code>TimeRange</code> is the request,
     *				   or</li>
     *			       <li>This <code>TimeRange</code> is the
     *				   request.</li> 
     *			       </ol>
     * @param startI	       the starting index into this
     *			       <code>TimeRange's</code> time list.
     * @param incrementI       the increment (-1 or 1) to compute succeeding
     *			       indexes.
     * @param myDurationI      the duration for this <code>TimeRange</code>.
     * @param allowMyHighI    allow my "high" value to be equal?
     * @param otherStartI      the starting index into the other
     *			       <code>TimeRange's</code> time list.
     * @param otherIncrementI  the increment (-1 or 1) to compute succeeding
     *			       indexes.
     * @param otherI	       the other <code>TimeRange</code>.
     * @param otherDurationI   the duration for the other
     *			       <code>TimeRange</code>.
     * @param allowOtherHighI allow the other "high" value to be equal?
     * @return the results of the match:
     *	       <p><ul>
     *	       <li>Rmap.MATCH_EQUAL - the two compare equal,</li>
     *	       <li>Rmap.MATCH_SUBSET - the <code>TimeRange</code> is contained
     *		   in the request,</li>
     *	       <li>Rmap.MATCH_SUPERSET - the <code>TimeRange</code> contains
     *		   the request,</li>
     *	       <li>Rmap.MATCH_INTERSECTION - the <code>TimeRange</code> and
     *		   the request contain common regions,</li>
     *	       <li>Rmap.MATCH_BEFORE - the <code>TimeRange</code> is before
     *		   the request,</li>
     *	       <li>Rmap.MATCH_AFTER - the <code>TimeRange</code> is after the
     *		   request, or</li>
     *	       <li>Rmap.MATCH_NOINTERSECTION - the <code>TimeRange</code> and
     *		   the overlap, but do not have any common regions.</li>
     *	       </ul>
     * @since V2.0
     * @version 09/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private final byte matchMonotonic2(boolean reversedI,
				       int modeI,
				       int startI,
				       int incrementI,
				       double myDurationI,
				       boolean allowMyHighI,
				       int otherStartI,
				       int otherIncrementI,
				       TimeRange otherI,
				       double otherDurationI,
				       boolean allowOtherHighI)
    {
	int    currentLow = 0;
	int[]  numMatches = new int[4];
	boolean[] mineIntersected = new boolean[getNptimes()],
		  otherIntersected = new boolean[otherI.getNptimes()];
	double myLowest = (reversedI ?
			   -(getPtimes()[getNptimes() - 1 - startI] + myDurationI) :
			   getPtimes()[startI]),
// mjm 12/20/05:  following seems wrong, it should match the otherHighest case !!??
	    myHighest = (reversedI ?
			   -(getPtimes()[getNptimes() - 1 - startI] + myDurationI) :
			   getPtimes()[startI]),
// correct logic: ???
//	    myHighest = (reversedI ?
//			   -getPtimes()[startI] :
//			   (getPtimes()[getNptimes() - 1 - startI] + myDurationI)),

	    otherLowest = (reversedI ?
			   -(otherI.getPtimes()[otherI.getNptimes() - 1 - otherStartI] + otherDurationI) :
			   otherI.getPtimes()[otherStartI]),
	    otherHighest = (reversedI ?
			    -otherI.getPtimes()[otherStartI] :
			    (otherI.getPtimes()[otherI.getNptimes() - 1 - otherStartI] + otherDurationI));

//System.err.println("enter mjm matchmonotonic2, allowMyHigh: "+allowMyHighI+", allowOtherHigh: "+allowOtherHighI+", getNptimes(): "+getNptimes()+", startI: "+startI+", getPtimes(): "+getPtimes()[0]+", myDurationI: "+myDurationI);
	// mjm 12/20/05  grope:
//	// if(otherDurationI == 0) allowMyHighI = true;  // grope

	for (int tIdx = 0,
		 idx = otherStartI;
	     (tIdx < otherI.getNptimes());
	     ++tIdx,
		 idx += otherIncrementI) {
	    // We have to check each of the input <code>TimeRange's</code>
	    // values, but we can use a binary search to compare them to the
	    // time intervals in this <code>TimeRange</code>.
	    double otherLow = (reversedI ?
			       -(otherI.getPtimes()[idx] + otherDurationI) :
			       otherI.getPtimes()[idx]),
		otherHigh = otherLow + otherDurationI;

	    for (int low = currentLow,
		     high = getNptimes() - 1,
		     idx1 = (low + high)/2;
		 (low <= high);
		 idx1 = (low + high)/2) {
		double myLow = (reversedI ?
				-(getPtimes()[startI + idx1*incrementI] +
				  myDurationI) :
				getPtimes()[startI + idx1*incrementI]),
		       myHigh = myLow + myDurationI;
//     	System.err.println("mjm myLow: "+myLow+", myHigh: "+myHigh+", otherLow: "+otherLow+", otherHigh: "+otherHigh);

		if ( (otherLow > myHigh) ||
	             (!allowMyHighI && (otherLow == myHigh)) ) {
// mjm try tweeking logic:
//		      ( (otherLow == myHigh) &&
//		        ((!reversedI && !allowMyHighI) || (reversedI && !allowOtherHighI)) ) ) {

		    // When the input <code>TimeRange's</code> interval starts
		    // after this interval, we need to move the search up. We
		    // also move the low point for the next time interval.
		    currentLow =
			low = idx1 + 1;

		} else if ((otherHigh < myLow) ||
			   (!allowOtherHighI && (otherHigh == myLow))) {
// mjm try tweeking logic:
//		      ( (otherHigh == myLow) &&
//		        ((reversedI && !allowMyHighI) || (!reversedI && !allowOtherHighI)) ) ) {

		    // When the input <code>TimeRange's</code> interval starts
		    // before this interval, we need to move the search down.
		    high = idx1 - 1;

		} else {
		    // Otherwise, we're in the right area of the time
		    // intervals. Unfortunately, since the time intervals can
		    // overlap even if they are monotonic, we can't just depend
		    // on this interval being the answer, although it may tell
		    // us something useful.
		    compareTimeIntervals
			(idx1,
			 myLow,
			 myHigh,
			 allowMyHighI,
			 mineIntersected,
			 idx,
			 otherLow,
			 otherHigh,
			 allowOtherHighI,
			 otherIntersected,
			 numMatches);

		    int idx2;
		    for (idx2 = idx1 - 1;
			 idx2 >= low;
			 --idx2) {
			// Work backwards until we find a time interval in this
			// <code>TimeRange</code> that cannot possibly have
			// any relationship to the other time interval.
			myLow = (reversedI ?
				 -(getPtimes()[startI + idx2*incrementI] +
				   myDurationI) :
				 getPtimes()[startI + idx2*incrementI]);
			myHigh = myLow + myDurationI;

			if ((myHigh < otherLow) ||
			    (!allowMyHighI && (myHigh == otherLow))) {
			    // We've gone too far.
			    break;
			}

			compareTimeIntervals
			    (idx2,
			     myLow,
			     myHigh,
			     allowMyHighI,
			     mineIntersected,
			     idx,
			     otherLow,
			     otherHigh,
			     allowOtherHighI,
			     otherIntersected,
			     numMatches);
		    }

		    // Shift the search for the next time interval to start no
		    // lower than the lowest entry from here that worked.
		    currentLow = idx2 + 1;

		    for (idx2 = idx1 + 1;
			 idx2 <= high;
			 ++idx2) {
			// Work forwards until we find a time interval in this
			// <code>TimeRange</code> that cannot possibly have
			// any relationship to the other time interval.
			myLow = (reversedI ?
				 -(getPtimes()[startI + idx2*incrementI] +
				   myDurationI) :
				 getPtimes()[startI + idx2*incrementI]);
			myHigh = myLow + myDurationI;

			if ((myLow > otherHigh) ||
			    (!allowOtherHighI && (myLow == otherHigh))) {
			    // We've gone too far.
			    break;
			}

			compareTimeIntervals
			    (idx2,
			     myLow,
			     myHigh,
			     allowMyHighI,
			     mineIntersected,
			     idx,
			     otherLow,
			     otherHigh,
			     allowOtherHighI,
			     otherIntersected,
			     numMatches);
		    }

		    break;
		}
	    }
	}

//	System.err.println("mjm matchmonotonic2, myLowest: "+myLowest+", myHigh: "+myHighest+", otherLow: "+otherLowest+", otherHigh: "+otherHighest);
	// Determine the result.
	byte resultR = determineResults(modeI,
					getNptimes(),
					myLowest,
					myHighest,
					allowMyHighI,
					mineIntersected,
					otherI.getNptimes(),
					otherLowest,
					otherHighest,
					allowOtherHighI,
					otherIntersected,
					numMatches);

	return (resultR);
    }

    /**
     * Matches this <code>TimeRange</code> against the input
     * <code>TimeRange</code> when this <code>TimeRange</code> is monotonically
     * increasing or decreasing and we don't know about the other one.
     * <p>
     * Since this <code>TimeRange</code> has a monotonic relationship between
     * its time intervals, we can use a binary search algorithm to find the
     * time interval in this <code>TimeRange</code> that is most likely to have
     * a relationship with each interval of the input <code>TimeRange</code>.
     * <p>
     * It is assumed by this code that one of the two <code>TimeRanges</code>
     * is from a source <code>Rmap</code> and one is from a request. The
     * <code>modeI</code> parameter tells us which is which.
     * <p>
     * If the direction is reversed, then we negate the times and durations.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reversedI	      is the comparison direction reversed?
     * @param modeI	      the comparison mode:
     *			      <p><ol start=0>
     *			      <li>Input <code>TimeRange</code> is the request,
     *				  or</li>
     *			      <li>This <code>TimeRange</code> is the
     *				  request.</li> 
     *			      </ol>
     * @param startI	      the starting index into this
     *			      <code>TimeRange's</code> time list.
     * @param incrementI      the increment (-1 or 1) to compute succeeding
     *			      indexes.
     * @param myDurationI     the duration for this <code>TimeRange</code>.
     * @param allowMyHighI    allow my "high" value to be equal?
     * @param otherI	      the other <code>TimeRange</code>.
     * @param otherDurationI  the duration for the other
     *			      <code>TimeRange</code>.
     * @param allowOtherHighI allow the other "high" value to be equal?
     * @return the results of the match:
     *	       <p><ul>
     *	       <li>Rmap.MATCH_EQUAL - the two compare equal,</li>
     *	       <li>Rmap.MATCH_SUBSET - the <code>TimeRange</code> is contained
     *		   in the request,</li>
     *	       <li>Rmap.MATCH_SUPERSET - the <code>TimeRange</code> contains
     *		   the request,</li>
     *	       <li>Rmap.MATCH_INTERSECTION - the <code>TimeRange</code> and
     *		   the request contain common regions,</li>
     *	       <li>Rmap.MATCH_BEFORE - the <code>TimeRange</code> is before
     *		   the request,</li>
     *	       <li>Rmap.MATCH_AFTER - the <code>TimeRange</code> is after the
     *		   request, or</li>
     *	       <li>Rmap.MATCH_NOINTERSECTION - the <code>TimeRange</code> and
     *		   the overlap, but do not have any common regions.</li>
     *	       </ul>
     * @since V2.0
     * @version 05/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private final byte matchMonotonicRandom(boolean reversedI,
					    int modeI,
					    int startI,
					    int incrementI,
					    double myDurationI,
					    boolean allowMyHighI,
					    TimeRange otherI,
					    double otherDurationI,
					    boolean allowOtherHighI)
    {
	int[]  numMatches = new int[4];
	boolean[] mineIntersected = new boolean[getNptimes()],
		  otherIntersected = new boolean[otherI.getNptimes()];
	double myLowest = (reversedI ?
			   -(getPtimes()[getNptimes() - 1 - startI] +
			     myDurationI) :
			   getPtimes()[startI]),
	    myHighest = (reversedI ?
			 -getPtimes()[startI] :
			 (getPtimes()[getNptimes() - 1 - startI] +
			  myDurationI)) ,
	    otherLowest = Double.MAX_VALUE,
	    otherHighest = -Double.MAX_VALUE;

	for (int idx = 0; idx < otherI.getNptimes(); ++idx) {
	    // We have to check each of the input <code>TimeRange's</code>
	    // values, but we can use a binary search to compare them to the
	    // time intervals in this <code>TimeRange</code>.
	    double otherLow =
		(reversedI ?
		 -(otherI.getPtimes()[idx] + otherDurationI) :
		 otherI.getPtimes()[idx]),
		otherHigh = otherLow + otherDurationI;

	    // Update the limits for the other <code>TimeRange</code>.
	    otherLowest = Math.min(otherLow,otherLowest);
	    otherHighest = Math.max(otherHigh,otherHighest);

	    for (int low = 0,
		     high = getNptimes() - 1,
		     idx1 = (low + high)/2;
		 (low <= high);
		 idx1 = (low + high)/2) {
		double myLow = (reversedI ?
				-(getPtimes()[startI + idx1*incrementI] +
				  myDurationI) :
				getPtimes()[startI + idx1*incrementI]),
		    myHigh = myLow + myDurationI;

		if ((otherLow > myHigh) ||
		    (!allowMyHighI && (otherLow == myHigh))) {
		    // When the input <code>TimeRange's</code> interval starts
		    // after this interval, we need to move the search up.
		    low = idx1 + 1;

		} else if ((otherHigh < myLow) ||
			   (!allowOtherHighI && (otherHigh == myLow))) {
		    // When the input <code>TimeRange's</code> interval starts
		    // before this interval, we need to move the search down.
		    high = idx1 - 1;

		} else {
		    // Otherwise, we're in the right area of the time
		    // intervals. Unfortunately, since the time intervals can
		    // overlap even if they are monotonic, we can't just depend
		    // on this interval being the answer, although it may tell
		    // us something useful.
		    compareTimeIntervals
			(idx1,
			 myLow,
			 myHigh,
			 allowMyHighI,
			 mineIntersected,
			 idx,
			 otherLow,
			 otherHigh,
			 allowOtherHighI,
			 otherIntersected,
			 numMatches);

		    for (int idx2 = idx1 - 1;
			 idx2 >= low;
			 --idx2) {
			// Work backwards until we find a time interval in this
			// <code>TimeRange</code> that cannot possibly have
			// any relationship to the other time interval.
			myLow = (reversedI ?
				 -(getPtimes()[startI + idx2*incrementI] +
				   myDurationI) :
				 getPtimes()[startI + idx2*incrementI]);
			myHigh = myLow + myDurationI;

			if ((myHigh < otherLow) ||
			    (!allowMyHighI && (myHigh == otherLow))) {
			    // We've gone too far.
			    break;
			}

			compareTimeIntervals
			    (idx2,
			     myLow,
			     myHigh,
			     allowMyHighI,
			     mineIntersected,
			     idx,
			     otherLow,
			     otherHigh,
			     allowOtherHighI,
			     otherIntersected,
			     numMatches);
		    }

		    for (int idx2 = idx1 + 1;
			 idx2 <= high;
			 ++idx2) {
			// Work forwards until we find a time interval in this
			// <code>TimeRange</code> that cannot possibly have
			// any relationship to the other time interval.
			myLow = (reversedI ?
				 -(getPtimes()[startI + idx2*incrementI] +
				   myDurationI) :
				 getPtimes()[startI + idx2*incrementI]);
			myHigh = myLow + myDurationI;

			if ((myLow > otherHigh) ||
			    (!allowOtherHighI && (myLow == otherHigh))) {
			    // We've gone too far.
			    break;
			}

			compareTimeIntervals
			    (idx2,
			     myLow,
			     myHigh,
			     allowMyHighI,
			     mineIntersected,
			     idx,
			     otherLow,
			     otherHigh,
			     allowOtherHighI,
			     otherIntersected,
			     numMatches);
		    }
		}
	    }
	}

	// Determine the result.
	return (determineResults(modeI,
				 getNptimes(),
				 myLowest,
				 myHighest,
				 allowMyHighI,
				 mineIntersected,
				 otherI.getNptimes(),
				 otherLowest,
				 otherHighest,
				 allowOtherHighI,
				 otherIntersected,
				 numMatches));
    }

    /**
     * Matches this <code>TimeRange</code> against the request using an
     * exhaustive compare.
     * <p>
     * This method is used when neither <code>TimeRange</code> is known to be
     * sorted in a monotonic order. Since we cannot make any assumptions about
     * the relationships between adjacent times, the method has to compare
     * every one of the times in this <code>TimeRange</code> to every one in
     * the request.
     * <p>
     * If the direction is reversed, then we negate the times and durations.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reversedI		is the comparison direction reversed?
     * @param myDurationI	the duration to use for this
     *				<code>TimeRange</code>.
     * @param allowMyHighI	allow my "high" value to be equal?
     * @param requestI		the request <code>TimeRange</code>.
     * @param requestDurationI  the duration to use for the request.
     * @param allowRequestHighI allow the request "high" value to be equal?
     * @return the results of the match:
     *	       <p><ul>
     *	       <li>Rmap.MATCH_EQUAL - the two compare equal,</li>
     *	       <li>Rmap.MATCH_SUBSET - this <code>TimeRange</code> is contained
     *		   in the request,</li>
     *	       <li>Rmap.MATCH_SUPERSET - this <code>TimeRange</code> contains
     *		   the request,</li>
     *	       <li>Rmap.MATCH_INTERSECTION - this <code>TimeRange</code> and
     *		   the request contain common regions,</li>
     *	       <li>Rmap.MATCH_BEFORE - this <code>TimeRange</code> is before
     *		   the request,</li>
     *	       <li>Rmap.MATCH_AFTER - this <code>TimeRange</code> is after the
     *		   request, or</li>
     *	       <li>Rmap.MATCH_NOINTERSECTION - this <code>TimeRange</code> and
     *		   the overlap, but do not have any common regions.</li>
     *	       </ul>
     * @since V2.0
     * @version 04/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
    private final byte matchRandom(boolean reversedI,
				   double myDurationI,
				   boolean allowMyHighI,
				   TimeRange requestI,
				   double requestDurationI,
				   boolean allowRequestHighI)
    {
	int[]  numMatches = new int[4];
	boolean[] mineIntersected = new boolean[getNptimes()],
		  requestIntersected = new boolean[requestI.getNptimes()];
	double myLowest = Double.MAX_VALUE,
	       myHighest = -Double.MAX_VALUE,
	       requestLowest = Double.MAX_VALUE,
	       requestHighest = -Double.MAX_VALUE;

	for (int idx = 0; idx < getNptimes(); ++idx) {
	    // Compare the limits of each time interval in this
	    // <code>TimeRange</code>.
	    double myLow = (reversedI ?
			    -(getPtimes()[idx] + myDurationI) :
			    getPtimes()[idx]),
		   myHigh = myLow + myDurationI;

	    // Update the overall limits.
	    myLowest = Math.min(myLow,myLowest);
	    myHighest = Math.max(myHigh,myHighest);

	    for (int idx1 = 0; idx1 < requestI.getNptimes(); ++idx1) {
		// Compare to the limits of each time interval in the request
		// <code>TimeRange</code>.
		double requestLow = (reversedI ?
				     -(requestI.getPtimes()[idx1] +
				       requestDurationI) :
				     requestI.getPtimes()[idx1]),
		       requestHigh = requestLow + requestDurationI;

		// Update the overall limits.
		requestLowest = Math.min(requestLow,requestLowest);
		requestHighest = Math.max(requestHigh,requestHighest);

		// Compare the time intervals.
		compareTimeIntervals
		    (idx,
		     myLow,
		     myHigh,
		     allowMyHighI,
		     mineIntersected,
		     idx1,
		     requestLow,
		     requestHigh,
		     allowRequestHighI,
		     requestIntersected,
		     numMatches);
	    }
	}

	// Determine the result.
	byte resultR = determineResults(0,
					getNptimes(),
					myLowest,
					myHighest,
					allowMyHighI,
					mineIntersected,
					requestI.getNptimes(),
					requestLowest,
					requestHighest,
					allowRequestHighI,
					requestIntersected,
					numMatches);
	return (resultR);
    }

    /**
     * Matches this <code>TimeRange</code> against the input
     * <code>TimeRelativeRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @param roI      the <code>RequestOptions</code>.
     * @param nPointsI the number of data points.
     * @return the <code>TimeRelativeResponse</code>.
     * @since V2.2
     * @version 02/25/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2005  JPW	Fixed a bug when Nptimes = 1 and duration > 0:
     *                  point number occasionally calculated as 1 less than
     *                  expected due to a slightly smaller value of offset
     *                  (which occurs due to limited floating point precision);
     *                  the fix was to round to the nearest point value.
     * 02/25/2004  INB	Added code to skip over duplicate points.
     * 12/11/2003  INB	Added <code>RequestOptions</code> handling.
     * 11/05/2003  INB	Created.
     *
     */
    final TimeRelativeResponse matchTimeRelative
	(TimeRelativeRequest requestI,
	 RequestOptions roI,
	 int nPointsI)
    {
	TimeRelativeResponse responseR = new TimeRelativeResponse();
	
	/*
	System.err.println("TimeRange.matchTimeRelative: " + this +
			   " " + requestI + " " + roI + " " + nPointsI);
	*/
	
	if (getNptimes() == 1) {
	    if (getDuration() == 0.) {
		// If we get here, then we must have an exact match.
		switch (requestI.getRelationship()) {
		case TimeRelativeRequest.AT_OR_BEFORE:
		case TimeRelativeRequest.AT_OR_AFTER:
		    // If the request is for this time, then use it.
		    responseR.setStatus(0);
		    responseR.setTime(getTime());
		    responseR.setInvert
			(requestI.getRelationship() ==
			 TimeRelativeRequest.AT_OR_BEFORE);
		    break;

		case TimeRelativeRequest.BEFORE:
		    // If the request is for before this time, then we want to
		    // move to the previous entry.
		    responseR.setStatus(-1);
		    break;

		case TimeRelativeRequest.AFTER:
		    // If the request is for after this time, then we want to
		    // move to the next entry.
		    responseR.setStatus(1);
		    break;
		}

	    } else {
		// Locate the actual point matching the reference.
		double offset = requestI.getTimeRange().getTime() - getTime();

/*
 * JPW debug print
try {
System.err.println("TimeRange.matchTimeRelative:\nrequestI.getTimeRange().getTime() = " + ToString.toString("%.17f",requestI.getTimeRange().getTime()) + "\ngetTime() = " + ToString.toString("%.17f",getTime()) + "\noffset = " + ToString.toString("%.17f",offset));
} catch (Exception e) {}
*
*/

		double step = getDuration()/nPointsI;

/*
 * JPW debug print
try {
System.err.println("duration = " + ToString.toString("%.17f",getDuration()) + "\nstep = " + step);
} catch (Exception e) {}
*
*/

		// JPW,MJM 05/19/2005: BUG FIX
		//      If offset is just slightly lower than expected (due
		//      to floating point precision) then the value of the
		//      variable "point" will be one increment lower than
		//      needed.  To fix this, round point to the nearest
		//      integer value.
		int point_old = (int) (offset/step);
		int point_round = (int) (0.5 + offset/step);
		// Only round out at 1e-14
		int point = (int)(offset/step + 0.00000000000005);
		

/*
 * JPW debug print
try {
System.err.println("point_old = " + point_old);
System.err.println("point_round = " + point_round);
System.err.println("point = " + point);
} catch (Exception e) {}
*
*/

		switch (requestI.getRelationship()) {
		case TimeRelativeRequest.BEFORE:
		    // If the request is for a time before the reference, then
		    // we want the previous point.
		    if (--point < 0) {
			// If there is nothing here, then we need to back up
			// the search.
			responseR.setStatus(-1);

		    } else {
			// If there is a prior point, then use its end time.
			responseR.setTime(getTime() + (point + 1)*step);
			responseR.setInvert(true);
		    }
		    break;

		case TimeRelativeRequest.AT_OR_BEFORE:
		    if ((roI != null) && roI.getExtendStart()) {
			// If the request is for a time at or before the
			// request time and this is an <code>extendStart</code>
			// request, then we want the point start time.
			responseR.setTime(getTime() + point*step);

		    } else {
			// If the request is for a time at or before the
			// request time, then we want the actual point end
			// time.
			responseR.setTime(getTime() + (point + 1)*step);
			responseR.setInvert(true);
		    }
		    break;

		case TimeRelativeRequest.AT_OR_AFTER:
		    // If the request is for a time at or after the request
		    // time, then we want either this point or the next one.
		    if (point*step == offset) {
			responseR.setTime(getTime() + point*step);
			responseR.setInvert(false);
		    } else if (++point == nPointsI) {
			responseR.setStatus(1);
		    } else {
			responseR.setTime(getTime() + point*step);
			responseR.setInvert(false);
		    }
		    break;

		case TimeRelativeRequest.AFTER:
		    // If the request is for a time after the reference, then
		    // we want the next point.
		    if (++point >= nPointsI) {
			// If there is nothing here, then we need to move the
			// search forwards.
			responseR.setStatus(1);
		    } else {
			// If there is a next point, then use its time.
// Kludge
// Original code:
// responseR.setTime(getTime() + point*step);
// New code: scootch the value up or down a hair depending on the duration
if (requestI.getTimeRange().getDuration() == 0.0) {
    // JPW debug print
    // System.err.println("TimeRange.matchTimeRel(): AFTER: scootch time up");
    responseR.setTime(getTime() + point*step + 0.00000000000005);
} else {
    // JPW debug print
    // System.err.println("TimeRange.matchTimeRel(): AFTER: scootch time down");
    responseR.setTime(getTime() + point*step - 0.00000000000005);
}
			responseR.setInvert(false);
		    }
		}
	    }

	} else {
	    // If we've got point times, then figure out where the reference
	    // time falls using a binary search.
	    int lo = 0;
	    int hi = nPointsI - 1;
	    int lastIdx = 0;
	    int idx;
	    double direction = 0.;
	    double time = 0.;

	    for (idx = (lo + hi)/2; lo <= hi; idx = (lo + hi)/2) {
		time = getPointTime(idx,nPointsI);
		direction = requestI.getTimeRange().getTime() - time;
		lastIdx = idx;

		if (direction < 0.) {
		    // If the reference comes before this point, then back up.
		    hi = idx - 1;

		} else if (direction == 0.) {
		    // If this is the correct point, then stop the search.
		    break;

		} else {
		    // If the reference comes after this point, then move
		    // forwards.
		    lo = idx + 1;
		}
	    }

	    if (lo <= hi) {
		// We hit the point on the nose.
		double lPtTime = 0.;
		double wPtTime = getPointTime(lastIdx,nPointsI);

		switch (requestI.getRelationship()) {
		case TimeRelativeRequest.AT_OR_BEFORE:
		case TimeRelativeRequest.AT_OR_AFTER:
		    // If the request is for this time, then use it.
		    responseR.setStatus(0);
		    responseR.setTime(time);
		    responseR.setInvert
			(requestI.getRelationship() ==
			 TimeRelativeRequest.AT_OR_BEFORE);
		    break;

		case TimeRelativeRequest.BEFORE:
		    // If the request is for before this time, then we want to
		    // move to the previous point or entry.  If necessary, we
		    // skip over duplicate points.
		    for (; lastIdx > 0; --lastIdx) {
			// Loop until we either run off the beginning or we
			// find a previous point.
			lPtTime = getPointTime(lastIdx - 1,nPointsI);
			if (lPtTime < wPtTime) {
			    break;
			}
		    }
		    if (lastIdx == 0) {
			responseR.setStatus(-1);
		    } else {
			responseR.setTime(lPtTime);
			responseR.setInvert(true);
		    }
		    break;

		case TimeRelativeRequest.AFTER:
		    // If the request is for after this time, then we want to
		    // move to the next point or entry.  If necessary, we
		    // skip over duplicate points.
		    // move to the next point or entry.
		    for (; lastIdx < nPointsI - 1; ++lastIdx) {
			// Loop until we either run off the end or we find
			// a next point.
			lPtTime = getPointTime(lastIdx + 1,nPointsI);
			if (lPtTime > wPtTime) {
			    break;
			}
		    }
		    if (lastIdx == nPointsI - 1) {
			responseR.setStatus(1);
		    } else {
			responseR.setTime(lPtTime);
			responseR.setInvert(false);
		    }
		    break;
		}

	    } else if ((lastIdx == 0) && (direction < 0)) {
		// If we discovered that what we want comes before this, then
		// back up.
		responseR.setStatus(-1);

	    } else if ((lastIdx == nPointsI - 1) && (direction > 0)) {
		// If we discovered that what we want comes after this, then
		// move forwards.
		responseR.setStatus(1);

	    } else {
		// If the time falls between two points, then we need to pick
		// one of them as the target.
		if (direction > 0) {
		    // Always set the index to the second
		    // <code>Rmap</code>.
		    ++lastIdx;
		}
		switch (requestI.getRelationship()) {
		case TimeRelativeRequest.BEFORE:
		case TimeRelativeRequest.AT_OR_BEFORE:
		    // If we want the point before the time, then we want the
		    // earlier of the two.
		    responseR.setTime(getPointTime(lastIdx - 1,nPointsI));
		    responseR.setInvert(true);
		    break;

		case TimeRelativeRequest.AT_OR_AFTER:
		case TimeRelativeRequest.AFTER:
		    // If we want the point after the time, then we want the
		    // later of the two.
		    responseR.setTime(getPointTime(lastIdx,nPointsI));
		    responseR.setInvert(false);
		    break;
		}
	    }
	}

	/*
	System.err.println("TimeRange.matchTimeRelative: " + this +
			   " " + requestI + " " + roI + " " + nPointsI +
			   "\ngot: " + responseR);
	*/
	
	return (responseR);
    }

    /**
     * Merges the input <code>TimeRange</code> and this <code>TimeRange</code>
     * to produce a new <code>TimeRange</code> that will be used as offsets for
     * the next level of inheritance.
     * <p>
     * The result of the merge is a <code>TimeRange</code> containing one time
     * value for each data point in the input <code>DataBlock</code> times the
     * number of time values in this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param iRangeI  the <code>TimeRange</code> inherited from above.
     * @param iBlockI  the <code>DataBlock</code> inherited from above.
     * @return the new <code>TimeRange</code>.
     * @see #dataOffsets(TimeRange,DataBlock,DataBlock)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    private final TimeRange mergeOffsets(TimeRange iRangeI,DataBlock iBlockI) {
	TimeRange tRangeR = null;
	boolean tInclusive = getInclusive();

	// Determine the output duration. It is either the same as the duration
	// for this <code>TimeRange</code> or it is inherited from the input
	// <code>TimeRange</code>.
	double rDuration = getDuration();
	if (rDuration == INHERIT_DURATION) {
	    rDuration = iRangeI.getDuration();
	    tInclusive = tInclusive || iRangeI.getInclusive();
	}

	if (getPtimes() == INHERIT_TIMES) {
	    // If this <code>TimeRange</code> inherits its time values from
	    // the input <code>TimeRange</code>, then produce a new
	    // <code>TimeRange</code> from the input and the duration from
	    // above.
	    tRangeR = new TimeRange
		(iRangeI.getPtimes(),
		 rDuration);

	} else {
	    // If this <code>TimeRange</code> has its own time values, then
	    // merge the two.
	    double ltimes[] = new double[iBlockI.getNpts()*getNptimes()];

	    for (int idx = 0,
		     idx2 = 0,
		     iNpts = iBlockI.getNpts();
		 idx < iNpts;
		 ++idx) {
		// Grab the time for the inherited <code>TimeRange's</code>
		// point.
		double itime = iRangeI.getPointTime(idx,iNpts);

		for (int idx1 = 0;
		     idx1 < getNptimes();
		     ++idx1,
			 ++idx2) {
		    // Add the inherit time value to our time values.
		    ltimes[idx2] = itime + getPtimes()[idx1];
		}
	    }

	    // Create a new <code>TimeRange</code> from the new time values
	    // list and the calculated duration.
	    tRangeR = new TimeRange(ltimes,rDuration);
	}
	tRangeR.setInclusive(tInclusive);

	return (tRangeR);
    }

    /**
     * Nullifies this <code>TimeRange</code>.
     * <p>
     * This method ensures that all pointers in this <code>DataBlock</code>
     * are cleared, reducing the effort needed by the garbage collector to
     * clean it up.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    public final void nullify() {
	setPtimes(null);
    }

    /**
     * Reads the <code>TimeRange</code> from the specified input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 07/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	read(null,isI,disI);
    }

    /**
     * Reads the <code>TimeRange</code> from the specified input stream.
     * <p>
     * This version takes an input <code>TimeRange</code> that is used to fill
     * default values for fields not read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>TimeRange</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @see #write(com.rbnb.api.TimeRange,String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    final void read(com.rbnb.api.TimeRange otherI,
		    InputStream isI,
		    DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	boolean[] seen = new boolean[PARAMETERS.length];

	// Read the open bracket marking the start of the
	// <code>TimeRange</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    // Read parameters until we see a closing bracket.
	    seen[parameter] = true;

	    switch (parameter) {
	    case PAR_DUR:
		setDuration(isI.readDouble());
		break;

	    case PAR_INC:
		setInclusive(isI.readBoolean());
		break;

	    case PAR_PTM:
		setPtimes(new double[isI.readInt()]);
		for (int idx = 0; idx < getNptimes(); ++idx) {
		    getPtimes()[idx] = isI.readDouble();
		}
		break;

	    case PAR_STA:
		setPtimes(new double[1]);
		getPtimes()[0] = isI.readDouble();
		break;
	    }
	}

	if (otherI != null) {
	    if (!seen[PAR_DUR]) {
		setDuration(otherI.getDuration());
	    }
	    if (!seen[PAR_PTM] && !seen[PAR_STA]) {
		setPtimes((double[]) otherI.getPtimes().clone());
	    }
	    if (!seen[PAR_INC]) {
		setInclusive(otherI.getInclusive());
	    }
	}
    }

    /**
     * Sets the time of the range and clears the duration so that it gets
     * inherited.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI the time.
     * @see #getTime()
     * @see #set(double,double)
     * @see #set(double[])
     * @see #set(double[],double)
     * @since V2.0
     * @version 11/30/2000
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(double timeI) {
	set(timeI,INHERIT_DURATION);
    }

    /**
     * Sets the time and duration of the range.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startI	 the time.
     * @param durationI  the duration.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the duration is negative and is not
     *		  <code>INHERIT_DURATION</code>.
     * @see #getTime()
     * @see #getDuration()
     * @see #set(double)
     * @see #set(double[])
     * @see #set(double[],double)
     * @see #setDuration(double)
     * @see #setPtimes(double[])
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Eliminated synchronization.
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(double startI,double durationI) {
	if ((durationI < 0.) &&
	    (duration != INHERIT_DURATION) &&
	    ((durationI != -Double.MAX_VALUE/10.) || (startI != 0.))) {
	    throw new IllegalStateException
		("Cannot set negative duration. Value = " + durationI);
	}

	setPtimes(new double[1]);
	ptimes[0] = startI;
	setDuration(durationI);
	changing = UNKNOWN;
    }

    /**
     * Sets the individual point times for the range and clears the duration
     * so that it gets inherited.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptimesI  the individual point times.
     * @see #getNptimes()
     * @see #getPtimes()
     * @see #set(double)
     * @see #set(double,double)
     * @see #set(double[],double)
     * @see #setDuration(double)
     * @see #setPtimes(double[])
     * @since V2.0
     * @version 11/30/2000
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(double[] ptimesI) {
	set(ptimesI,INHERIT_DURATION);
    }

    /**
     * Sets the individual point times for the range and the duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptimesI    the individual point times.
     * @param durationI  the duration.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the duration is negative and is not
     *		  <code>INHERIT_DURATION</code>.
     * @see #getDuration()
     * @see #getNptimes()
     * @see #getPtimes()
     * @see #set(double)
     * @see #set(double,double)
     * @see #set(double[])
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Eliminated synchronization.
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(double[] ptimesI,double durationI) {
	if ((durationI < 0.) && (duration != INHERIT_DURATION)) {
	    throw new IllegalStateException("Cannot set negative duration.");
	}

	setPtimes(ptimesI);
	setDuration(durationI);
	changing = UNKNOWN;
    }

    /**
     * Sets the direction multiplier.
     * <p>
     *
     * @author Ian Brown
     *
     * @param directionI the direction multiplier.
     * @since V2.0
     * @version 05/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/21/2002  INB	Created.
     *
     */
    final void setDirection(boolean directionI) {
	direction = directionI;
    }

    /**
     * Sets the duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param durationI  the new duration.
     * @see #getDuration()
     * @since V2.0
     * @version 05/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setDuration(double durationI) {
	duration = durationI;
    }

    /**
     * Sets the inclusive flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inclusiveI include both ends of the range?
     * @see #isInclusive()
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    public final void setInclusive(boolean inclusiveI) {
	inclusive = inclusiveI;
    }

    /**
     * Sets the individual point times array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptimesI  the new point times array.
     * @see #getPtimes()
     * @since V2.0
     * @version 05/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setPtimes(double[] ptimesI) {
	ptimes = ptimesI;
    }

    /**
     * Subtracts the input <code>TimeRange</code> from this one.
     * <p>
     * The resulting <code>TimeRange</code> contains a number of point time
     * values equal to the number in this <code>TimeRange</code> times
     * the number of in the input <code>TimeRange</code>.
     * <p>
     * <p>
     * The duration of the result is one of:
     * <p><ol>
     * <li>If this <code>TimeRange</code> does not have a duration,
     *	   nothing,</li>
     * <li>If the number of times in the two <code>TimeRanges</code> do not
     *     match, this <code>TimeRange's</code> duration,</li>
     * <li>If the input <code>TimeRange</code> does not have a duration, this
     *	   <code>TimeRange's</code> duration, or</li>
     * <li>The minimum of the two durations.</li>
     * </ol>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>TimeRange</code>.
     * @return the difference <code>TimeRange</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the this <code>TimeRange</code> inherits either
     *		  time or duration or if there is a problem with the
     *	          inheritance.
     * @see #add(TimeRange)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    final TimeRange subtract(TimeRange otherI) {
	if (otherI == null) {
	    // Return this <code>TimeRange</code> if the there is no input.
	    return (this);
	}

	if ((getPtimes() == INHERIT_TIMES) ||
	    (getDuration() == INHERIT_DURATION)) {
	    throw new java.lang.IllegalArgumentException
		("Cannot subtract " + otherI + " from " + this);
	}

	double oDuration = otherI.getDuration();
	if (otherI.getDuration() == INHERIT_DURATION) {
	    oDuration = getDuration();
	}
	double fDuration = Math.min(getDuration(),oDuration);

	TimeRange differenceR = null;
	if (otherI.getPtimes() == INHERIT_TIMES) {
	    differenceR = new TimeRange(getPtimes(),fDuration);

	} else if ((getChanging() != INCREASING) ||
		   (otherI.getChanging() != INCREASING)) {
	    throw new java.lang.IllegalStateException
		("Cannot subtract " + otherI + " from " + this);

	} else {
	    boolean reversed = (getDirection() || otherI.getDirection());
	    java.util.Vector values = new java.util.Vector();

	    int theirBase = 0;
	    for (int idx = 0; idx < getNptimes(); ++idx) {
		double myMin = getPtimes()[idx],
		    myMax = myMin + getDuration(),
		    cMyMin = (reversed ? -myMax : myMin),
		    cMyMax = (reversed ? -myMin : myMax);

		for (int idx2 = theirBase;
		     idx2 < otherI.getNptimes();
		     ++idx2) {
		    double theirMin = otherI.getPtimes()[idx2],
			theirMax = theirMin + oDuration,
			cTheirMin = (reversed ? -theirMax : theirMin),
			cTheirMax = (reversed ? -theirMin : theirMax);

		    if (cMyMin < cTheirMin) {
			if ((cMyMax > cTheirMin) ||
			    (((getDuration() == 0.) || (oDuration == 0.)) &&
			     (cMyMax == cTheirMin))) {
			    double value =
				Math.min(myMax,theirMax) - fDuration -
				theirMin;
			    
			    values.addElement(new Double(value));
			}

		    } else if (cMyMax > cTheirMax) {
			if ((cMyMin < cTheirMax) ||
			    (((getDuration() == 0) || (oDuration == 0.)) &&
			    (cMyMin == cTheirMax))) {
			    values.addElement(new Double(0.));
			}

		    } else {
			values.addElement(new Double(myMin - theirMin));
		    }
		}
	    }

	    if (values.size() == 0) {
		throw new java.lang.IllegalArgumentException
		    ("No intersection between " + this + " and " + otherI);
	    }

	    double[] lptimes = new double[values.size()];
	    for (int idx = 0; idx < lptimes.length; ++idx) {
		lptimes[idx] = ((Double) values.elementAt(idx)).doubleValue();
	    }
	    differenceR = new TimeRange(lptimes,fDuration);
	}
	differenceR.setInclusive(getInclusive() && otherI.getInclusive());

	return (differenceR);
    }

    /**
     * Gets a displayable string representation of this <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 01/27/2003
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
	if (getNptimes() > 0) {
try {
	    stringR += "[" + ToString.toString("%.17f",getPtimes()[0]);
	    for (int idx = 1; idx < getNptimes(); ++idx) {
		stringR +=
		    "," + ToString.toString("%.17f",getPtimes()[idx]);
	    }
	    stringR += "]";
} catch (Exception e) {}
	}
	if (getDuration() != INHERIT_DURATION) {
	    stringR += "+" + getDuration();
	}
	if (getDirection()) {
	    stringR += " (reversed comparisons)";
	}
	if (isInclusive()) {
	    stringR += " (inclusive)";
	}
	return (stringR);
    }

    /**
     * Writes this <code>TimeRange</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 07/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void write(String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws java.io.IOException
    {
	write(null,parametersI,parameterI,osI,dosI);
    }

    /**
     * Writes this <code>TimeRange</code> to the specified stream.
     * <p>
     * This version writes out only the differences between this
     * <code>TimeRange</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the <code>TimeRange</code> to compare to.
     *			   <p>
     *			   If <code>null</code>, then this
     *			   <code>TimeRange</code> is always written out in
     *			   full.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @see #read(com.rbnb.api.TimeRange,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    final void write(TimeRange otherI,
		     String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws java.io.IOException
    {
	boolean trChanged = false;

	if (otherI == null) {
	    trChanged = true;
	} else {
	    trChanged =
		(getInclusive() != otherI.getInclusive()) ||
		(getDuration() != otherI.getDuration()) ||
		(getPtimes() == otherI.getPtimes());
	    if (!trChanged) {
		trChanged = getNptimes() != otherI.getNptimes();
		for (int idx = 0; !trChanged && (idx < getNptimes()); ++idx) {
		    trChanged = getPtimes()[idx] != otherI.getPtimes()[idx];
		}
	    }
	}

	if (trChanged) {
	    osI.writeParameter(parametersI,parameterI);
	    Serialize.writeOpenBracket(osI);

	    if (getDuration() != INHERIT_DURATION) {
		osI.writeParameter(PARAMETERS,PAR_DUR);
		osI.writeDouble(getDuration());
	    }

	    if (IsSupported.isSupported
		(IsSupported.FEATURE_TIME_RANGE_INCLUSIVE,
		 osI.getBuildVersion(),
		 osI.getBuildDate())) {
		osI.writeParameter(PARAMETERS,PAR_INC);
		osI.writeBoolean(getInclusive());
	    }
	    if (getNptimes() == 1) {
		osI.writeParameter(PARAMETERS,PAR_STA);
		osI.writeDouble(getTime());

	    } else if (getPtimes() != INHERIT_TIMES) {
		osI.writeParameter(PARAMETERS,PAR_PTM);
		osI.writeInt(getNptimes());
		for (int idx = 0; idx < getNptimes(); ++idx) {
		    osI.writeDouble(getPtimes()[idx]);
		}
	    }
	    Serialize.writeCloseBracket(osI);
	}
    }
}
