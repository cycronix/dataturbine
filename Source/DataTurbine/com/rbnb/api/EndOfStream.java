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
 * Marks the end of a stream (request).
 * <p>
 * This class is an extension of the <code>Rmap</code> class that is used to
 * mark the normal end of the stream of responses to a request. An object of
 * this class is always returned as the last response to a request (unless an
 * error occurs).
 * <p>
 * The <code>EndOfStream</code> object may contain data from the request and
 * should be processed normally. It may turn out to be empty.
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
 * 04/16/2001  INB	Created.
 *
 */
public class EndOfStream
    extends com.rbnb.api.Rmap
{
    /**
     * the request was before the beginning of the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */
    public final static byte REASON_BOD = 2;

    /**
     * reached the end of the request.
     * <p>
     * This indicates a normal end of the request. It indicates that all of the
     * repetitions of the request have been matched.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */
    public final static byte REASON_END = 0;

    /**
     * the request was after the end of the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */
    public final static byte REASON_EOD = 3;

    /**
     * an error occured trying to retrieve the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/23/2002
     */
    public final static byte REASON_ERROR = 5;

    /**
     * failed to match any data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */
    public final static byte REASON_NODATA = 4;

    /**
     * failed to match any names.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */
    public final static byte REASON_NONAME = 1;

    /**
     * text strings for reporting the reason for the end of the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/23/2002
     */
    public final static String[] REASONS = {
	"Normal end of request.",
	"Unable to match any of the channel names in the request.",
	"Request was for a time before that of any data.",
	"Request was for a time after that of any data.",
	"Request did not match a name or time.",
	"A processing error occured.  No data could be retrieved."
    };

    /**
     * the reason for the end of stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */
    private byte reason = REASON_END;

    // Private constants:
    private final static byte	PAR_RSN = 0;

    private final static String[] PARAMETERS = {
				    "RSN"
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
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
     *
     */
    public EndOfStream() {
	super();
    }

    /**
     * Class constructor to build an <code>EndOfStream</code> for the specified
     * reason code.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reasonI the reason code.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the reason code is not valid.
     * @see #REASON_BOD
     * @see #REASON_END
     * @see #REASON_EOD
     * @see #REASON_ERROR
     * @see #REASON_NODATA
     * @see #REASON_NONAME
     * @since V2.0
     * @version 04/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    public EndOfStream(byte reasonI) {
	this();
	setReason(reasonI);
    }

    /**
     * Class constructor to build an <code>EndOfStream</code> by reading it
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
     * 04/25/2001  INB	Created.
     *
     */
    EndOfStream(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build an <code>EndOfStream</code> by reading it
     * from an input stream.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>EndOfStream</code> as an
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
     * 04/25/2001  INB	Created.
     *
     */
    EndOfStream(Rmap otherI,InputStream isI,DataInputStream disI)
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
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation adds the additional <code>EndOfStream</code> fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    final String additionalToString() {
	String resultR = " " + REASONS[getReason()];

	return (resultR);
    }

    /**
     * Adds the <code>EndOfStream's</code> parameters to the full serialization
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
     * 09/28/2001  INB	Created.
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
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultEndOfStreamParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Default <code>EndOfStream</code> parameters.
     * <p>
     * This method fills in any fields not read from an input stream by copying
     * them from the input <code>Rmap</code>.
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>EndOfStream</code> as an
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
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    final void defaultEndOfStreamParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((otherI != null) && (otherI instanceof EndOfStream)) {
	    EndOfStream other = (EndOfStream) otherI;

	    if ((seenI == null) || !seenI[parametersStart + PAR_RSN]) {
		setReason(other.getReason());
	    }
	}
    }

    /**
     * Gets the reason code.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the reason code.
     * @see #REASON_BOD
     * @see #REASON_END
     * @see #REASON_EOD
     * @see #REASON_ERROR
     * @see #REASON_NODATA
     * @see #REASON_NONAME
     * @since V2.0
     * @version 04/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    public final byte getReason() {
	return (reason);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Creates a new instance of this <code>EndOfStream</code>.
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
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    final Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = super.newInstance();

	((EndOfStream) rmapR).setReason(getReason());
	return (rmapR);
    }

    /**
     * Reads the <code>EndOfStream</code> from the specified input stream.
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
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
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
	    if (!readEndOfStreamParameter(parameter,isI,disI)) {
		readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads <code>EndOfStream</code> parameters.
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
     * @see #writeEndOfStreamParameters(com.rbnb.api.EndOfStream,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    final boolean readEndOfStreamParameter(int parameterI,
					   InputStream isI,
					   DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean endOfStreamR = false;

	if (parameterI >= parametersStart) {
	    endOfStreamR = true;
	    switch (parameterI - parametersStart) {
	    case PAR_RSN:
		setReason(isI.readByte());
		break;

	    default:
		endOfStreamR = false;
		break;
	    }
	}

	return (endOfStreamR);
    }

    /**
     * Sets the reason code.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reasonI the reason code.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the reason code is not valid.
     * @see #REASON_BOD
     * @see #REASON_END
     * @see #REASON_EOD
     * @see #REASON_ERROR
     * @see #REASON_NODATA
     * @see #REASON_NONAME
     * @since V2.0
     * @version 04/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    public final void setReason(byte reasonI) {
	if ((reasonI < 0) || (reasonI >= REASONS.length)) {
	    throw new java.lang.IllegalArgumentException
		("Reason code " + reasonI + " is not valid.");
	}

	reason = reasonI;
    }

    /**
     * Writes this <code>EndOfStream</code> to the specified stream.
     * <p>
     * This method writes out differences between this <code>EndOfStream</code>
     * and the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>EndOfStream</code> as an
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
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
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
	writeEndOfStreamParameters((EndOfStream) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes out <code>EndOfStream</code> parameters.
     * <p>
     * This method writes out differences between this <code>EndOfStream</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>EndOfStream</code>.
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
     * @see #readEndOfStreamParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2001  INB	Created.
     *
     */
    final void writeEndOfStreamParameters(EndOfStream otherI,
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

	if (((otherI == null) && (getReason() != REASON_END)) ||
	    ((otherI != null) && (getReason() != otherI.getReason()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_RSN);
	    osI.writeByte(getReason());
	}
    }
}
