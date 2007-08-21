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
 * Request for data from the DataTurbine.
 * <p>
 * The <code>DataRequest</code> class extends <code>Rmap</code> to provide
 * additional fields and flags to support requests for data out of the
 * DataTurbine.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/06/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/06/2003  INB	Added the time relationships.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 01/24/2001  INB	Created.
 *
 */
public final class DataRequest
    extends com.rbnb.api.Rmap
{
    // Public constants:
    /**
     * absolute time reference.
     * <p>
     * The request time values are used as is.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #AFTER
     * @see #ALIGNED
     * @see #MODIFIED
     * @see #NEWEST
     * @see #OLDEST
     * @since V2.0
     * @version 11/06/2003
     */
    public final static byte ABSOLUTE = 0;

    /**
     * aligned time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #MODIFIED
     * @see #NEWEST
     * @see #OLDEST
     * @since V2.0
     * @version 11/06/2003
     */
    public final static byte ALIGNED = 5;

    /**
     * newest data starting after the specified time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @see #ALIGNED
     * @see #MODIFIED
     * @see #NEWEST
     * @see #OLDEST
     * @since V2.0
     * @version 11/06/2003
     */
    public final static byte AFTER = 3;

    /**
     * get data from both <code>EXISTING</code> and <code>FUTURE</code>
     * domains.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #EXISTING
     * @see #FUTURE
     * @since V2.0
     * @version 03/01/2001
     */
    public final static byte ALL = 0;

    /**
     * respond using a single consolidated <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #FRAMES
     * @since V2.0
     * @version 03/01/2001
     */
    public final static byte CONSOLIDATED = 0;

    /**
     * data at the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @since V2.2
     * @version 11/06/2003
     */
    public final static byte EQUAL = 0;

    /**
     * get data already in the ring buffers.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ALL
     * @see #FUTURE
     * @since V2.0
     * @version 03/01/2001
     */
    public final static byte EXISTING = 1;

    /**
     * respond a single source frame <code>Rmap</code> hierarchy at a time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #CONSOLIDATED
     * @since V2.0
     * @version 03/01/2001
     */
    public final static byte FRAMES = 1;

    /**
     * get data that appears after the request is issued.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/01/2001
     */
    public final static byte FUTURE = 2;

    /**
     * data starting after the specified time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #EQUAL
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @since V2.2
     * @version 11/06/2003
     */
    public final static byte GREATER = 2;

    /**
     * data starting at or after the specified time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #EQUAL
     * @see #GREATER
     * @see #LESS
     * @see #LESS_EQUAL
     * @since V2.2
     * @version 11/06/2003
     */
    public final static byte GREATER_EQUAL = 1;

    /**
     * data starting before the specified time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #EQUAL
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS_EQUAL
     * @since V2.2
     * @version 11/06/2003
     */
    public final static byte LESS = -2;

    /**
     * data starting at or before the specified time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #EQUAL
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @since V2.2
     * @version 11/06/2003
     */
    public final static byte LESS_EQUAL = -1;

    /**
     * newest data including at least some data after the specified time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #ALIGNED
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @see #NEWEST
     * @see #OLDEST
     * @since V2.0
     * @version 11/06/2003
     */
    public final static byte MODIFIED = 4;

    /**
     * times are relative to that of the newest available data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #ALIGNED
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @see #MODIFIED
     * @see #OLDEST
     * @since V2.0
     * @version 11/06/2003
     */
    public final static byte NEWEST = 1;

    /**
     * times are relative to that of the oldest available data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #ALIGNED
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @see #MODIFIED
     * @see #NEWEST
     * @since V2.0
     * @version 11/06/2003
     */
    public final static byte OLDEST = 2;

    /**
     * infinite repetitions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/03/2001
     */
    public final static long INFINITE = Long.MAX_VALUE;

    // Private fields:
    /**
     * control when gaps can occur?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private boolean gapControl = false;

    /**
     * the time increment.
     * <p>
     * If this is not 0, then it is added to the calculated start time of the
     * previous request to produce an <code>ABSOLUTE</code> start time for
     * subsequent repetitions     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @since V2.0
     * @version 04/03/2001
     */
    private double increment = 0.;

    /**
     * the number of repetitions.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #INFINITE
     * @since V2.0
     * @version 04/03/2001
     */
    private long repetitions = 1;

    /**
     * response mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #CONSOLIDATED
     * @see #FRAMES
     * @since V2.0
     * @version 03/01/2001
     */
    private byte responseMode = CONSOLIDATED;

    /**
     * synchronized channel responses?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/01/2001
     */
    private boolean synchronizedChannels = false;

    /**
     * the time domain.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ALL
     * @see #EXISTING
     * @see #FUTURE
     * @since V2.0
     * @version 03/01/2001
     */
    private byte timeDomain = EXISTING;

    /**
     * the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #MODIFIED
     * @see #NEWEST
     * @see #OLDEST
     * @since V2.0
     * @version 04/11/2002
     */
    private byte timeReference = ABSOLUTE;

    /**
     * the time relationship.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #EQUAL
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @since V2.2
     * @version 11/06/2003
     */
    private byte timeRelationship = EQUAL;

    // Private constants:
    private final static byte	PAR_DOM = 0,
				PAR_GAP = 1,
				PAR_MOD = 2,
				PAR_REF = 3,
				PAR_REP = 4,
				PAR_SYN = 5,
				PAR_REL = 6;

    private final static String[] PARAMETERS = {
				    "DOM",
				    "GAP",
				    "MOD",
				    "REF",
				    "REP",
				    "SYN",
				    "REL"
				};

    // Private class fields:
    private static String[] ALL_PARAMETERS = null;

    private static int parametersStart = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2001  INB	Created.
     *
     */
    public DataRequest() {
	super();
    }

    /**
     * Class constructor to build an <code>DataRequest</code> by reading it
     * from an input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2001  INB	Created.
     *
     */
    public DataRequest(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build an <code>DataRequest</code> by reading it
     * from an input stream.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataRequest</code> as an
     *		     <code>Rmap</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2001  INB	Created.
     *
     */
    public DataRequest(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Class constructor to build a <code>DataRequest</code> for the specified
     * name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI	     the name of the <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public DataRequest(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(nameI,null,null);
    }

    /**
     * Class constructor to build a <code>DataRequest</code> from the specified
     * values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI	     the name of the <code>Rmap</code>.
     * @param trangeI	     the <code>TimeRange</code>.
     * @param frangeI	     the frame index <code>TimeRange</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @see #DataRequest()
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/09/2001  INB	Created.
     *
     */
    public DataRequest(String nameI,
		       TimeRange trangeI,
		       TimeRange frangeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(nameI,
	     trangeI,
	     frangeI,
	     DataRequest.ABSOLUTE,
	     DataRequest.EXISTING,
	     1,
	     1.,
	     false,
	     DataRequest.CONSOLIDATED);
    }

    /**
     * Class constructor to build a <code>DataRequest</code> from the specified
     * values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI	     the name of the <code>Rmap</code>.
     * @param trangeI	     the <code>TimeRange</code>.
     * @param frangeI	     the frame index <code>TimeRange</code>.
     * @param referenceI     the time reference.
     * @param domainI	     the time domain.
     * @param repetitionsI   the number of repetitions.
     * @param incrementI     the repitition increment.
     * @param synchronizedI  synchronize channels?
     * @param modeI	     the response mode.
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/2001  INB	Created.
     *
     */
    public DataRequest(String nameI,
		       TimeRange trangeI,
		       TimeRange frangeI,
		       byte referenceI,
		       byte domainI,
		       long repetitionsI,
		       double incrementI,
		       boolean synchronizedI,
		       byte modeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(nameI,
	     trangeI,
	     frangeI,
	     referenceI,
	     domainI,
	     repetitionsI,
	     incrementI,
	     synchronizedI,
	     modeI,
	     false);
    }

    /**
     * Class constructor to build a <code>DataRequest</code> from the specified
     * values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI	     the name of the <code>Rmap</code>.
     * @param trangeI	     the <code>TimeRange</code>.
     * @param frangeI	     the frame index <code>TimeRange</code>.
     * @param referenceI     the time reference.
     * @param domainI	     the time domain.
     * @param repetitionsI   the number of repetitions.
     * @param incrementI     the repitition increment.
     * @param synchronizedI  synchronize channels?
     * @param modeI	     the response mode.
     * @param gapControlI    control when gaps can occur?
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Call the version with the time relationship.
     * 03/01/2001  INB	Created.
     *
     */
    public DataRequest(String nameI,
		       TimeRange trangeI,
		       TimeRange frangeI,
		       byte referenceI,
		       byte domainI,
		       long repetitionsI,
		       double incrementI,
		       boolean synchronizedI,
		       byte modeI,
		       boolean gapControlI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(nameI,
	     trangeI,
	     frangeI,
	     referenceI,
	     EQUAL,
	     domainI,
	     repetitionsI,
	     incrementI,
	     synchronizedI,
	     modeI,
	     gapControlI);
    }

    /**
     * Class constructor to build a <code>DataRequest</code> from the specified
     * values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI	     the name of the <code>Rmap</code>.
     * @param trangeI	     the <code>TimeRange</code>.
     * @param frangeI	     the frame index <code>TimeRange</code>.
     * @param referenceI     the time reference.
     * @param relationshipI  the time relationship.
     * @param domainI	     the time domain.
     * @param repetitionsI   the number of repetitions.
     * @param incrementI     the repitition increment.
     * @param synchronizedI  synchronize channels?
     * @param modeI	     the response mode.
     * @param gapControlI    control when gaps can occur?
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
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
    public DataRequest(String nameI,
		       TimeRange trangeI,
		       TimeRange frangeI,
		       byte referenceI,
		       byte relationshipI,
		       byte domainI,
		       long repetitionsI,
		       double incrementI,
		       boolean synchronizedI,
		       byte modeI,
		       boolean gapControlI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI,null,trangeI);
	setFrange(frangeI);
	setReference(referenceI);
	setRelationship(relationshipI);
	setDomain(domainI);
	setRepetitions(repetitionsI,incrementI);
	setSynchronized(synchronizedI);
	setMode(modeI);
	setGapControl(gapControlI);
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation adds the additional <code>DataRequest</code> fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
	 * 11/01/2006  WHF  Added text explaining booleans.
     * 11/06/2003  INB	Added the time relationships.
     * 03/26/2001  INB	Created.
     *
     */
    final String additionalToString() {
	String resultR = " (";

	switch (getDomain()) {
	case ALL:
	    resultR += "ALL";
	    break;
	case EXISTING:
	    resultR += "EXISTING";
	    break;
	case FUTURE:
	    resultR += "FUTURE";
	    break;
	}

	resultR += ", ";
	switch (getReference()) {
	case ABSOLUTE:
	    resultR += "ABSOLUTE";
	    break;

	case AFTER:
	    resultR += "AFTER";
	    break;

	case ALIGNED:
	    resultR += "ALIGNED";
	    break;

	case MODIFIED:
	    resultR += "MODIFIED";
	    break;

	case OLDEST:
	    resultR += "OLDEST";
	    break;

	case NEWEST:
	    resultR += "NEWEST";
	    break;
	}

	resultR += ", ";
	switch (getRelationship()) {
	case EQUAL:
	    resultR += "=";
	    break;


	case GREATER:
	    resultR += ">";
	    break;

	case GREATER_EQUAL:
	    resultR += ">=";
	    break;

	case LESS:
	    resultR += "<";
	    break;

	case LESS_EQUAL:
	    resultR += "<=";
	    break;
	}

	resultR += ", ";
	if (getNrepetitions() != 1) {
	    resultR +=
		getNrepetitions() + "x" + getIncrement() +
		", ";
	}
	switch (getMode()) {
	case CONSOLIDATED:
	    resultR += "CONSOLIDATED";
	    break;

	case FRAMES:
	    resultR += "FRAMES";
	    break;
	}

	resultR +=
	    ", synch = " + getSynchronized() +
	    ", gap = " + getGapControl() +
	    ")";

	return (resultR);
    }

    /**
     * Adds the <code>DataRequest's</code> parameters to the full serialization
     * parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 06/01/2001  INB	Created.
     *
     */
    static String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = Rmap.addToParameters(null);
	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}
	return (addToParameters(parametersR,PARAMETERS));
    }

    /**
     * Defaults for all parameters.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code> into
     * this one. It is designed to be overridden by higher level objects to
     * ensure that they handle all of their parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param seenI  the fields that we've seen already.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultDataRequestParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Default <code>DataRequest</code> parameters.
     * <p>
     * This method fills in any fields not read from an input stream by copying
     * them from the input <code>Rmap</code>.
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataRequest</code> as an
     *		     <code>Rmap</code>.
     * @param seenI  the fields seen.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Added the time relationships.
     * 07/30/2001  INB	Created.
     *
     */
    final void defaultDataRequestParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((otherI != null) && (otherI instanceof DataRequest)) {
	    DataRequest other = (DataRequest) otherI;

	    if ((seenI == null) || !seenI[parametersStart + PAR_DOM]) {
		setDomain(other.getDomain());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_GAP]) {
		setGapControl(other.getGapControl());
	    }

	    if ((seenI == null) || !seenI[parametersStart  + PAR_MOD]) {
		setMode(other.getMode());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_REF]) {
		setReference(other.getReference());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_REP]) {
		setRepetitions(other.getNrepetitions(),other.getIncrement());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_SYN]) {
		setSynchronized(other.getSynchronized());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_REL]) {
		setRelationship(other.getRelationship());
	    }
	}
    }

    /**
     * Gets the time domain.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time domain.
     * @see #ALL
     * @see #EXISTING
     * @see #FUTURE
     * @see #setDomain(byte)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final byte getDomain() {
	return (timeDomain);
    }

    /**
     * Combines this <code>DataRequest</code> with the input one to produce a
     * coherent set of flags.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>DataRequest</code>.
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Added the relationship.
     * 03/02/2001  INB	Created.
     *
     */
    public final void combineWith(DataRequest otherI) {
	if (otherI.getDomain() != EXISTING) {
	    setDomain(otherI.getDomain());
	}
	if (otherI.getReference() != ABSOLUTE) {
	    setReference(otherI.getReference());
	}
	if (otherI.getMode() != CONSOLIDATED) {
	    setMode(otherI.getMode());
	}
	if (otherI.getSynchronized()) {
	    setSynchronized(otherI.getSynchronized());
	}
	if (otherI.getGapControl()) {
	    setGapControl(otherI.getGapControl());
	}
	if (otherI.getRelationship() != EQUAL) {
	    setRelationship(otherI.getRelationship());
	}
    }

    /**
     * Gets the gap control flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return control gaps?
     * @see #setGapControl(boolean)
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    public final boolean getGapControl() {
	return (gapControl);
    }

    /**
     * Gets the repitition increment.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the repitition increment.
     * @see #getNrepetitions()
     * @see #setRepetitions(long,double)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final double getIncrement() {
	return (increment);
    }

    /**
     * Gets the response mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the response mode.
     * @see #CONSOLIDATED
     * @see #FRAMES
     * @see #setMode(byte)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final byte getMode() {
	return (responseMode);
    }

    /**
     * Gets the number of repetitions.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of repetitions.
     * @see #getIncrement()
     * @see #setRepetitions(long,double)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final long getNrepetitions() {
	return (repetitions);
    }

    /**
     * Gets the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time reference.
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #ALIGNED
     * @see #MODIFIED
     * @see #NEWEST
     * @see #OLDEST
     * @see #setReference(byte)
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final byte getReference() {
	return (timeReference);
    }

    /**
     * Gets the time relationship.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the relationship to the time reference.
     * @see #EQUAL
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
     * @see #setRelationship(byte)
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
    public final byte getRelationship() {
	return (timeRelationship);
    }

    /**
     * Gets the synchronized channels flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the synchronized channels flag.
     * @see #setSynchronized(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final boolean getSynchronized() {
	return (synchronizedChannels);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Creates a new instance of the same class as this <code>Rmap</code> (or a
     * similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Added the time relationships.
     * 01/23/2002  INB	Created.
     *
     */
    final Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest rmapR = (DataRequest) super.newInstance();

	rmapR.setDomain(getDomain());
	rmapR.setGapControl(getGapControl());
	rmapR.setMode(getMode());
	rmapR.setReference(getReference());
	rmapR.setRelationship(getRelationship());
	rmapR.setRepetitions(getNrepetitions(),getIncrement());
	rmapR.setSynchronized(getSynchronized());

	return (rmapR);
    }

    /**
     * Reads the <code>DataRequest</code> from the specified input stream.
     * <p>
     * The input <code>Rmap</code> is used to fill in any fields that are not
     * read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(com.rbnb.api.Rmap,String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2001  INB	Created.
     *
     */
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Rmap</code>.
	Serialize.readOpenBracket(isI);

	// Initialize the full parameter list.
	initializeParameters();

	boolean[] seen = new boolean[ALL_PARAMETERS.length];
	int parameter;
	while ((parameter = Serialize.readParameter(ALL_PARAMETERS,
						    isI)) != -1) {
	    seen[parameter] = true;

	    // Read parameters until we see a closing bracket.
	    if (!readDataRequestParameter(parameter,isI,disI)) {
		readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads <code>DataRequest</code> parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI  the parameter index.
     * @param isI	  the input stream.
     * @param disI	  the data input stream.
     * @return was the parameter recognized?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeDataRequestParameters(com.rbnb.api.DataRequest,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Added the time relationships.
     * 01/26/2001  INB	Created.
     *
     */
    final boolean readDataRequestParameter(int parameterI,
					   InputStream isI,
					   DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean dataRequestR = false;

	if (parameterI >= parametersStart) {
	    dataRequestR = true;
	    switch (parameterI - parametersStart) {
	    case PAR_DOM:
		setDomain(isI.readByte());
		break;

	    case PAR_GAP:
		setGapControl(isI.readBoolean());
		break;

	    case PAR_MOD:
		setMode(isI.readByte());
		break;

	    case PAR_REF:
		setReference(isI.readByte());
		break;

	    case PAR_REP:
		setRepetitions(isI.readLong(),isI.readDouble());
		break;

	    case PAR_SYN:
		setSynchronized(isI.readBoolean());
		break;

	    case PAR_REL:
		setRelationship(isI.readByte());
		break;

	    default:
		dataRequestR = false;
		break;
	    }
	}

	return (dataRequestR);
    }

    /**
     * Sets the time domain.
     * <p>
     *
     * @author Ian Brown
     *
     * @param domainI  the time domain.
     * @see #ALL
     * @see #EXISTING
     * @see #FUTURE
     * @see #getDomain()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final void setDomain(byte domainI) {
	timeDomain = domainI;
    }

    /**
     * Sets the gap control flag.
     * <p>
     * When gap control is on, the server waits for acknowledgement of each
     * repetition of each source's data to be received by the sink before
     * starting the next repetition. When used in conjunction with
     * <code>reference=OLDEST</code> or <code>NEWEST</code> and
     * <code>domain=FUTURE</code> or <code>ALL</code>, this allows the client
     * to control when gaps occur in the data it receives.
     * <p>
     * When gap control is off, the server runs as fast as it can. Gaps occur
     * between repetitions, but their timing is determined entirely by when
     * buffers (such as TCP socket buffers) fill up.
     * <p>
     *
     * @author Ian Brown
     *
     * @param gapControlI turn on gap control?
     * @see #getGapControl()
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    public final void setGapControl(boolean gapControlI) {
	gapControl = gapControlI;
    }

    /**
     * Sets the response mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param modeI  the response mode.
     * @see #CONSOLIDATED
     * @see #FRAMES
     * @see #getMode()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final void setMode(byte modeI) {
	responseMode = modeI;
    }

    /**
     * Sets the number of repetitions and the increment for repetitions after
     * the first.
     * <p>
     * When the increment is 0, the request is repeated unchanged.
     * <p>
     * When the increment is non-zero, repetitions after the first are
     * essentially requests for <code>ABSOLUTE</code> times.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numberI     the number of repetitions.
     * @param incrementI  the increment between repetitions.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @see #ABSOLUTE
     * @see #getIncrement()
     * @see #getNrepetitions()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final void setRepetitions(long numberI,double incrementI) {
	if (numberI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The number of repetitions must be positive.");
	}

	repetitions = numberI;
	increment = incrementI;
    }

    /**
     * Sets the time reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @param referenceI  the time reference.
     * @see #ABSOLUTE
     * @see #AFTER
     * @see #ALIGNED
     * @see #MODIFIED
     * @see #NEWEST
     * @see #OLDEST
     * @see #setReference(byte)
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final void setReference(byte referenceI) {
	timeReference = referenceI;
    }

    /**
     * Sets the time relationship.
     * <p>
     *
     * @author Ian Brown
     *
     * @param relationshipI the relationship to the time reference.
     * @see #EQUAL
     * @see #getRelationship()
     * @see #GREATER
     * @see #GREATER_EQUAL
     * @see #LESS
     * @see #LESS_EQUAL
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
    public final void setRelationship(byte relationshipI) {
	timeRelationship = relationshipI;
    }

    /**
     * Sets the synchronized channels flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param synchronizedI  synchronize the channels?
     * @see #getSynchronized()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/02/2001  INB	Created.
     *
     */
    public final void setSynchronized(boolean synchronizedI) {
	synchronizedChannels = synchronizedI;
    }

    /**
     * Updates the limits of a request based on the input limits.
     * <p>
     *
     * @author Ian Brown
     *
     * @param tLimitsI the time limits.
     * @param fLimitsI the frame limits.
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
     * @since V2.0
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    final void updateRequestLimits(TimeRange tLimitsI,TimeRange fLimitsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean end = (getReference() == ALIGNED);
	setReference(ABSOLUTE);

	for (int idx = 0; idx < getNchildren(); ++idx) {
	    getChildAt(idx).updateLimits(end,tLimitsI,fLimitsI);
	}
    }

    /**
     * Writes this <code>DataRequest</code> to the specified stream.
     * <p>
     * This method writes out differences between this <code>DataRequest</code>
     * and the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>DataRequest</code> as an
     *			   <code>Rmap</code>.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/26/2001  INB	Created.
     *
     */
    void write(Rmap otherI,
	       String[] parametersI,
	       int parameterI,
	       OutputStream osI,
	       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Write out the object.
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged(this,parametersI,parameterI);

	writeStandardParameters(otherI,osI,dosI);
	writeDataRequestParameters((DataRequest) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes out <code>DataRequest</code> parameters.
     * <p>
     * This method writes out differences between this <code>DataRequest</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataRequest</code>.
     * @param osI    the output stream.
     * @param dosI   the data output stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readDataRequestParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 11/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2003  INB	Added support for the time relationships.
     * 01/26/2001  INB	Created.
     *
     */
    final void writeDataRequestParameters(DataRequest otherI,
					  OutputStream osI,
					  DataOutputStream dosI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	if (((otherI == null) && (getDomain() != EXISTING)) ||
	    ((otherI != null) && (getDomain() != otherI.getDomain()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_DOM);
	    osI.writeByte(getDomain());
	}

	if (((otherI == null) && getGapControl()) ||
	    ((otherI != null) &&
	     (getGapControl() != otherI.getGapControl()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_GAP);
	    osI.writeBoolean(getGapControl());
	}

	if (((otherI == null) && (getMode() != CONSOLIDATED)) ||
	    ((otherI != null) && (getMode() != otherI.getMode()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_MOD);
	    osI.writeByte(getMode());
	}

	if (((otherI == null) && (getReference() != ABSOLUTE)) ||
	    ((otherI != null) && (getReference() != otherI.getReference()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_REF);
	    osI.writeByte(getReference());
	}

	if (((otherI == null) && (getRelationship() != EQUAL)) ||
	    ((otherI != null) &&
	     (getRelationship() != otherI.getRelationship()))) {
	    if (!IsSupported.isSupported
		(IsSupported.FEATURE_REQUEST_TIME_RELATIVE,
		 osI.getBuildVersion(),
		 osI.getBuildDate())) {
		throw new com.rbnb.api.SerializeException
		    ("The remote server or application does not support " +
		     "time relationships in requests.");
	    }
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_REL);
	    osI.writeByte(getRelationship());
	}

	if (((otherI == null) && (getNrepetitions() > 0)) ||
	    ((otherI != null) &&
	     (getNrepetitions() != otherI.getNrepetitions()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_REP);
	    osI.writeLong(getNrepetitions());
	    osI.writeDouble(getIncrement());
	}

	if (((otherI == null) && getSynchronized()) ||
	    ((otherI != null) &&
	     (getSynchronized() != otherI.getSynchronized()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_SYN);
	    osI.writeBoolean(getSynchronized());
	}
    }
}
