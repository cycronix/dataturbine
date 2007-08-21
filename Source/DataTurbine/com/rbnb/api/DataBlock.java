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
 * Describes and provides the data belonging to an <code>Rmap</code>.
 * <p>
 * A <code>DataBlock</code> consists of three basic parts:
 * <p><ol>
 * <li>The size of the data,</li>
 * <li>The description of the data, and</li>
 * <li>The data payload.</li>
 * </ol><p>
 * The size of the data in the <code>DataBlock</code> is required and consists
 * of: 
 * <p><ul>
 * <li>The number of data points, and</li>
 * <li>The size of each data point in bytes.</li>
 * </ul><p>
 * The description of the data (<code>DataReference</code>) is optional and
 * consists of: 
 * <p><ul>
 * <li>The data primitive type (INT16, FLOAT32, etc.),</li>
 * <li>The order of bytes for multi-byte values (MSB, LSB, UNKNOWN),</li>
 * <li>A flag indicating the indivisibility of the data,</li>
 * <li>The offset in bytes to the first point, and</li>
 * <li>The stride in bytes from the start of one point to the start of the
 *     next.</li>
 * </ul><p>
 * The data payload is optional and consists of a <code>java.util.Vector</code>
 * containing one or more arrays of data. The arrays can be one of two cases:
 * <p><ol>
 * <li>Arrays of primitive objects, or</li>
 * <li>Arrays of arrays of bytes.</li>
 * </ol>
 * A special case: a byte[0] array of data is a placeholder indicating that
 * a data payload exists, but has been omitted.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataReference
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 2005/03/23
 */

/*
 * Copyright 2000, 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/17/2006  EMF      Use marker to save space on archive recovery.
 * 2005/03/22  WHF      Added TYPE_USER data type, and associated values.
 * 11/10/2003  INB	Added <code>copyDataFromArray</code> case in
 *			<code>getDataPoints</code> to the multiple data
 *			elements case to handle things on the server side.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/02/2003  INB	Call <code>Vector(1)</code> rather than
 *			<code>Vector()</code>.
 * 03/27/2003  INB	Took out unnecessary flush in <code>write</code>.
 * 11/30/2000  INB	Created.
 *
 */
public final class DataBlock
    extends com.rbnb.api.Serializable
{
    // Public constants:

    /**
     * Unknown data type or byte ordering.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte UNKNOWN = 0;

    /**
     * Least-Significant-Byte first (big-endian, Intel) byte ordering.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte ORDER_LSB = 1;

    /**
     * Most-Significant-Byte first (little-endian, Java) byte order.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte ORDER_MSB = 2;

    /**
     * Take the data type from the input data payload.
     * <p>
     * The resulting data type value will be one of the other types.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_FROM_INPUT = 1;

    /**
     * Boolean (8-bit, one byte) data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_BOOLEAN = 2;

    /**
     * 8-bit (one byte) signed integer data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_INT8 = 3;

    /**
     * 16-bit (two byte) signed integer data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_INT16 = 4;

    /**
     * 32-bit (four byte) signed integer data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_INT32 = 5;

    /**
     * 64-bit (eight byte) signed integer data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_INT64 = 6;

    /**
     * 32-bit (four byte) floating point data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_FLOAT32 = 7;

    /**
     * 64-bit (eight byte) floating point data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_FLOAT64 = 8;

    /**
     * Character string data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final byte TYPE_STRING = 9;

    /**
     * Byte array data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/20/2002
     */
    public static final byte TYPE_BYTEARRAY = 10;
	
	/**
	  * User registration type.
     * <p>
     *
     * @author WHF
     *
     * @since V2.5B4
     */
	public static final byte TYPE_USER = 11;

    // Class fields:
    static final String[]	ORDERS = {
				    "UNKNOWN",
				    "LSB",
				    "MSB"
				},
				TYPES = {
				    "UNKNOWN",
				    "<input>",
				    "BOOLEAN",
				    "INT8",
				    "INT16",
				    "INT32",
				    "INT64",
				    "FLOAT32",
				    "FLOAT64",
				    "STRING",
				    "BYTEARRAY",
					"USER"
				};

    // Private fields:
    /**
     * Optional descriptive information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/09/2001
     */
    private DataReference reference = null;

    /**
     * The number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private int numPoints = 0;

    /**
     * The number of bytes in a single point.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private int pointSize = 0;

    /**
     * The data payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private java.util.Vector data = null;

    // Private constants:
    private static final byte PAR_DAT = 0;
    private static final byte PAR_DRF = 1;
    private static final byte PAR_PTS = 2;
    private static final byte PAR_SIZ = 3;

    private static final byte[]	TYPE_SIZES =
    {
	0,		// Unknown.
	0,		// From input.
	1,		// Boolean.
	1,		// Int8.
	2,		// Int16.
	4,		// Int32.
	8,		// Int64.
	4,		// Float32.
	8,		// Float64.
	0,		// String.
	0,		// Byte array.
	0		// User
    };

    private static final String[] PARAMETERS =
    {
	"DAT",
	"DRF",
	"PTS",
	"SIZ"
    };


    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public DataBlock() {
	super();
    }

    /**
     * Class constructor to build a <code>DataBlock</code> by reading it from
     * an input stream.
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
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    DataBlock(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>DataBlock</code> by reading it from
     * an input stream.
     * <p>
     * This version uses the input <code>DataBlock</code> to fill in default
     * values for any fields not read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataBlock</code>.
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
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    DataBlock(DataBlock otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
//(new Exception("DataBlock dis "+disI)).printStackTrace();
	read(otherI,isI,disI);
    }

    /**
     * Class constructor to build a <code>DataBlock</code> from the basic
     * fields. 
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI    the data payload.
     * @param ndataI   the number of points.
     * @param ptsizeI  the number of bytes in a single point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of points or point size is not positive
     *		  or if they do not match the size of the data payload.
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public DataBlock(Object dataI,
		     int ndataI,
		     int ptsizeI)
    {
	this();
	set(dataI,ndataI,ptsizeI);
    }

    /**
     * Class constructor to build a <code>DataBlock</code> from the data
     * payload and description from the input values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI      the data payload.
     * @param ndataI     the number of points.
     * @param ptsizeI    the number of bytes in a single point.
     * @param dtypeI	 the type of data.
     * @param worderI	 the word order of the data.
     * @param indivflgI	 are points are individually accessible?
     * @param offsetI	 the offset in bytes to the first point.
     * @param strideI	 the stride in bytes to the next point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the number of points is not positive,</li>
     *		  <li>the point size is not positive,</li>
     *		  <li>the offset is not 0 or positive,</li>
     *		  <li>the stride is not 0 or positive,</li>
     *		  <li>the number of points or point size does not match the
     *		      size of the data payload,</li>
     *		  <li>the offset is non-zero and the data payload is not
     *		      null,</li>
     *		  <li>the stride is non-zero and not equal to the point size
     *		      and the data payload is not null, or</li>
     *		  <li>the data type does not match the point size or data
     *		      payload type.</li>
     *		  </ul>
     * @see #getData()
     * @see #getDtype()
     * @see #getIndivFlg()
     * @see #getNpts()
     * @see #getOffset()
     * @see #getPtsize()
     * @see #getStride()
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public DataBlock(Object dataI,
		     int ndataI,
		     int ptsizeI,
		     byte dtypeI,
		     byte worderI,
		     boolean indivflgI,
		     int offsetI,
		     int strideI)
    {
	this();
	set(dataI,
	    ndataI,
	    ptsizeI,
	    dtypeI,
	    worderI,
	    indivflgI,
	    offsetI,
	    strideI);
    }

    /**
     * Class constructor to build a <code>DataBlock</code> from the data
     * payload and description from the input values.
     * <p>
     * This version handles MIME data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI      the data payload.
     * @param ndataI     the number of points.
     * @param ptsizeI    the number of bytes in a single point.
     * @param dtypeI	 the type of data.
     * @param mimeTypeI  the MIME type.
     * @param worderI	 the word order of the data.
     * @param indivflgI	 are points are individually accessible?
     * @param offsetI	 the offset in bytes to the first point.
     * @param strideI	 the stride in bytes to the next point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the number of points is not positive,</li>
     *		  <li>the offset is not 0 or positive,</li>
     *		  <li>the stride is not 0 or positive,</li>
     *		  <li>the number of points does not match the size of the data
     *		      payload,</li>
     *		  <li>the offset is non-zero and the data payload is not
     *		      null,</li>
     *		  <li>the stride is non-zero and not equal to the point size
     *		      and the data payload is not null, or</li>
     *		  <li>the data type does not match the point size or data
     *		      payload type.</li>
     *		  </ul>
     * @see #getData()
     * @see #getDtype()
     * @see #getIndivFlg()
     * @see #getMIMEType()
     * @see #getNpts()
     * @see #getOffset()
     * @see #getStride()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    public DataBlock(Object dataI,
		     int ndataI,
		     int ptsizeI,
		     byte dtypeI,
		     String mimeTypeI,
		     byte worderI,
		     boolean indivflgI,
		     int offsetI,
		     int strideI)
    {
	this();
	set(dataI,
	    ndataI,
	    ptsizeI,
	    dtypeI,
	    mimeTypeI,
	    worderI,
	    indivflgI,
	    offsetI,
	    strideI);
    }

    /**
     * Adds more data.
     * <p>
     * This method adds the additional data as a new element of the data
     * vector. There must already be data in the vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI the data to add.
     * @param nPtsI the number of additional points.
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/09/2001  INB	Created.
     *
     */
    public final void addData(Object dataI,int nPtsI) {
	java.util.Vector lData = getData();
	lData.addElement(dataI);
	setNpts(getNpts() + nPtsI);
    }

    /**
     * Computes the number of bytes in the specified element of the data array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the array index.
     * @return the number of bytes.
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/08/2001  INB	Created.
     *
     */
    final int bytesInElement(int indexI) {
	Object entry = getData().elementAt(indexI);
	int bytesR;

	if (entry instanceof byte[]) {
	    bytesR = ((byte[]) entry).length;
	    if (indexI == 0) {
		bytesR -= getOffset();
	    }
	} else {
	    bytesR = pointsInElement(indexI)*getPtsize();
	}

	return (bytesR);
    }

    /**
     * Clones this <code>DataBlock</code>.
     * <p>
     * This method clones the data reference and payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 02/20/2002
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
	DataBlock clonedR = (DataBlock) super.clone();

//System.err.println("DataBlock.clone() called");
//(new Exception()).printStackTrace();

	if (clonedR != null) {
	    if (getDreference() != null) {
		clonedR.setDreference(null);
		clonedR.setDreference((DataReference) getDreference().clone());
	    }

	    if (getData() != null) {
		int nEntries = getData().size();
		java.util.Vector nData = new java.util.Vector(nEntries);

		for (int idx = 0; idx < nEntries; ++idx) {
		    Object entry = getData().elementAt(idx),
			nEntry = null;

		    if (entry instanceof byte[]) {
			nEntry = ((byte[]) entry).clone();

		    } else if (entry instanceof byte[][]) {
			nEntry = ((byte[][]) entry).clone();

		    } else if (entry instanceof boolean[]) {
			nEntry = ((boolean[]) entry).clone();

		    } else if (entry instanceof double[]) {
			nEntry = ((double[]) entry).clone();

		    } else if (entry instanceof float[]) {
			nEntry = ((float[]) entry).clone();

		    } else if (entry instanceof int[]) {
			nEntry = ((int[]) entry).clone();

		    } else if (entry instanceof long[]) {
			nEntry = ((long[]) entry).clone();

		    } else if (entry instanceof String) {
			nEntry = entry;

		    } else if (entry instanceof String[]) {
			nEntry = ((String[]) entry).clone();
		    }

		    nData.addElement(nEntry);
		}

		clonedR.setData(nData);
	    }
	}

	return (clonedR);
    }

    /**
     * Copies elements from the input array of bytes to the output array of
     * primitives, converting along the way.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputI	the input array.
     * @param fromI	the starting index into the input array.
     * @param outputI	the output array.
     * @param toI	the starting index into the output array.
     * @param nPointsI  the number of points to copy.
     * @since V2.0
     * @version 01/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    private final void convertBytesToArray(byte[] inputI,
					   int fromI,
					   Object outputI,
					   int toI,
					   int nPointsI)
    {
	int dStride = getStride();

	switch (getDtype()) {
	case TYPE_BOOLEAN:
	    boolean[] outputBool = (boolean[]) outputI;
	    for (int idx = 0,
		     from = fromI,
		     to = toI;
		 idx < nPointsI;
		 ++idx,
		     from += dStride,
		     ++to) {
		outputBool[to] = (inputI[from] != 0);
	    }
	    break;

	case TYPE_INT16:
	    short[] outputShort = (short[]) outputI;
	    if (getWorder() == ORDER_LSB) {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    outputShort[to] = (short)
			((((short) inputI[from    ] & 0x0ff)     ) +
			 (((short) inputI[from + 1] & 0x0ff) << 8));
		}
	    } else {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    outputShort[to] = (short)
			((((short) inputI[from + 1] & 0x0ff)     ) +
			 (((short) inputI[from    ] & 0x0ff) << 8));
		}
	    }
	    break;

	case TYPE_INT32:
	    int[] outputInt = (int[]) outputI;
	    if (getWorder() == ORDER_LSB) {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    outputInt[to] =
			(((int) inputI[from    ] & 0x0ff)      ) +
			(((int) inputI[from + 1] & 0x0ff) << 8 ) +
			(((int) inputI[from + 2] & 0x0ff) << 16) +
			(((int) inputI[from + 3] & 0x0ff) << 24);
		}
	    } else {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    outputInt[to] =
			(((int) inputI[from + 3] & 0x0ff)      ) +
			(((int) inputI[from + 2] & 0x0ff) << 8 ) +
			(((int) inputI[from + 1] & 0x0ff) << 16) +
			(((int) inputI[from    ] & 0x0ff) << 24);
		}
	    }
	    break;

	case TYPE_FLOAT32:
	    // Floats are handled by going through the work of making an
	    // integer and then converting that to a float.
	    float[] outputFloat = (float[]) outputI;
	    if (getWorder() == ORDER_LSB) {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    int value =
			(((int) inputI[from    ] & 0x0ff)      ) +
			(((int) inputI[from + 1] & 0x0ff) << 8 ) +
			(((int) inputI[from + 2] & 0x0ff) << 16) +
			(((int) inputI[from + 3] & 0x0ff) << 24);
		    outputFloat[to] = Float.intBitsToFloat(value);
		}
	    } else {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    int value =
			(((int) inputI[from + 3] & 0x0ff)      ) +
			(((int) inputI[from + 2] & 0x0ff) << 8 ) +
			(((int) inputI[from + 1] & 0x0ff) << 16) +
			(((int) inputI[from    ] & 0x0ff) << 24);
		    outputFloat[to] = Float.intBitsToFloat(value);
		}
	    }
	    break;

	case TYPE_INT64:
	    long[] outputLong = (long[]) outputI;
	    if (getWorder() == ORDER_LSB) {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    outputLong[to] =
			(((long) inputI[from    ] & 0x0ff)      ) +
			(((long) inputI[from + 1] & 0x0ff) << 8 ) +
			(((long) inputI[from + 2] & 0x0ff) << 16) +
			(((long) inputI[from + 3] & 0x0ff) << 24) +
			(((long) inputI[from + 4] & 0x0ff) << 32) +
			(((long) inputI[from + 5] & 0x0ff) << 40) +
			(((long) inputI[from + 6] & 0x0ff) << 48) +
			(((long) inputI[from + 7] & 0x0ff) << 56);
		}
	    } else {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    outputLong[to] =
			(((long) inputI[from + 7] & 0x0ff)      ) +
			(((long) inputI[from + 6] & 0x0ff) << 8 ) +
			(((long) inputI[from + 5] & 0x0ff) << 16) +
			(((long) inputI[from + 4] & 0x0ff) << 24) +
			(((long) inputI[from + 3] & 0x0ff) << 32) +
			(((long) inputI[from + 2] & 0x0ff) << 40) +
			(((long) inputI[from + 1] & 0x0ff) << 48) +
			(((long) inputI[from    ] & 0x0ff) << 56);
		}
	    }
	    break;

	case TYPE_FLOAT64:
	    // Doubles are handled by going through the work of making an
	    // long and then converting that to a double.
	    double[] outputDouble = (double[]) outputI;
	    if (getWorder() == ORDER_LSB) {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    long value =
			(((long) inputI[from    ] & 0x0ff)      ) +
			(((long) inputI[from + 1] & 0x0ff) << 8 ) +
			(((long) inputI[from + 2] & 0x0ff) << 16) +
			(((long) inputI[from + 3] & 0x0ff) << 24) +
			(((long) inputI[from + 4] & 0x0ff) << 32) +
			(((long) inputI[from + 5] & 0x0ff) << 40) +
			(((long) inputI[from + 6] & 0x0ff) << 48) +
			(((long) inputI[from + 7] & 0x0ff) << 56);
		    outputDouble[to] = Double.longBitsToDouble(value);
		}
	    } else {
		for (int idx = 0,
			 from = fromI,
			 to = toI;
		     idx < nPointsI;
		     ++idx,
			 from += dStride,
			 ++to) {
		    long value =
			(((long) inputI[from + 7] & 0x0ff)      ) +
			(((long) inputI[from + 6] & 0x0ff) << 8 ) +
			(((long) inputI[from + 5] & 0x0ff) << 16) +
			(((long) inputI[from + 4] & 0x0ff) << 24) +
			(((long) inputI[from + 3] & 0x0ff) << 32) +
			(((long) inputI[from + 2] & 0x0ff) << 40) +
			(((long) inputI[from + 1] & 0x0ff) << 48) +
			(((long) inputI[from    ] & 0x0ff) << 56);
		    outputDouble[to] = Double.longBitsToDouble(value);
		}
	    }
	    break;

	case TYPE_STRING:
	    String[] outputS = (String[]) outputI;

	    for (int idx = 0,
		     dPtsize = getPtsize(),
		     from = fromI,
		     to = toI;
		 idx < nPointsI;
		 ++idx,
		     from += dStride,
		     ++to) {
		if (outputS[to] == null) {
		    outputS[to] = new String();
		}
		outputS[to] = new String(inputI,from,dPtsize);
	    }
	    break;

	case TYPE_BYTEARRAY:
	default:
	    // For cases where we do not recognize the output type or for byte
	    // arrays, the output must be an array of byte arrays.
	    int start = ((getDtype() != TYPE_BYTEARRAY) &&
			 (getWorder() == ORDER_LSB)) ? getPtsize() - 1 : 0,
		by = ((getDtype() != TYPE_BYTEARRAY) &&
		      (getWorder() == ORDER_LSB)) ? -1 : 1;
	    byte[][] outputBArray = (byte[][]) outputI;

	    for (int idx = 0,
		     from = fromI + start;
		 idx < nPointsI;
		 ++idx,
		     from += dStride) {
		if ((outputBArray[toI + idx] == null) ||
		    (outputBArray[toI + idx].length != getPtsize())) {
		    outputBArray[toI + idx] = new byte[getPtsize()];
		}

		for (int idx1 = 0,
			 from2 = 0;
		     idx1 < getPtsize();
		     ++idx1,
			 from2 += by) {
		    outputBArray[toI + idx][idx1] = inputI[from + from2];
		}
	    }
	}
     }

    /**
     * Copies elements from the input array to the output array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputI	the input array.
     * @param fromI	the starting index into the input array.
     * @param outputI	the output array.
     * @param toI	the starting index into the output array.
     * @param nPointsI  the number of points to copy.
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
    private final void copyArray(Object inputI,
				 int fromI,
				 Object outputI,
				 int toI,
				 int nPointsI)
    {
	if (getStride() == getPtsize()) {
	    // When the stride is equal to the point size, we can perform a
	    // single array copy.
	    System.arraycopy
		(inputI,
		 fromI,
		 outputI,
		 toI,
		 nPointsI);

	} else {
	    // When the stride is not the same as the point size, we have to
	    // copy the bytes point by point.
	    for (int idx = 0; idx < nPointsI; ++idx) {

		// Copy the data for a single point from the input to the
		// output array.
		System.arraycopy
		    (inputI,
		     fromI + idx*getStride()/getPtsize(),
		     outputI,
		     toI + idx,
		     1);
	    }
	}
    }

    /**
     * Copies byte arrays from the input data array to the output data array
     * based on the information in this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bytesI  the array of byte arrays to get data out of.
     * @param fromI   the starting index into the input array.
     * @param bytesO  the array of byte arrays to put data into.
     * @param toI     the starting index into the output array.
     * @param nPointsI  the number of points to copy.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem with the data sizes.
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
    private final void copyByteArrays(byte[][] bytesI,
				      int fromI,
				      byte[][] bytesO,
				      int toI,
				      int nPointsI)
    {
	if (bytesI.length < fromI + nPointsI) {
	    throw new java.lang.IllegalStateException
		("Cannot get " + nPointsI +
		 " points at offset " +
		 fromI + ", stride " + getStride() +
		 " from an array of " + bytesI.length + " points.");
	}

	if (getStride() == getPtsize()) {
	    // When the stride is equal to the point size, we can perform a
	    // single array copy.
	    System.arraycopy
		(bytesI,
		 fromI,
		 bytesO,
		 toI,
		 nPointsI);

	} else {
	    // When the stride is not the same as the point size, we have to
	    // copy the bytes point by point.
	    for (int idx = 0; idx < nPointsI; ++idx) {

		// Copy the bytes for a single point from the input to the
		// output array.
		System.arraycopy
		    (bytesI,
		     fromI + idx*getStride()/getPtsize(),
		     bytesO,
		     toI + idx,
		     1);
	    }
	}
    }

    /**
     * Copies bytes from the input data array to the output data array based
     * on the information in this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bytesI  the byte array to get bytes out of.
     * @param fromI   the starting index into the input array.
     * @param bytesO  the byte array to put bytes into.
     * @param toI     the starting index into the output array.
     * @param nPointsI  the number of points to copy.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem with the data sizes.
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
    private final void copyBytes(byte[] bytesI,
				 int fromI,
				 byte[] bytesO,
				 int toI,
				 int nPointsI)
    {
	if (bytesI.length < fromI + (nPointsI - 1)*getStride()) {
	    throw new java.lang.IllegalStateException
		("Cannot get " + (nPointsI*getPtsize()) +
		 " bytes at offset " +
		 fromI + ", stride " + getStride() +
		 " from an array of " + bytesI.length + " bytes.");
	}

	if (getStride() == getPtsize()) {
	    // When the stride is equal to the point size, we can perform a
	    // single array copy.
	    System.arraycopy
		(bytesI,
		 fromI,
		 bytesO,
		 toI*getPtsize(),
		 nPointsI*getPtsize());

	} else {
	    // When the stride is not the same as the point size, we have to
	    // copy the bytes point by point.
	    for (int idx = 0; idx < nPointsI; ++idx) {

		// Copy the bytes for a single point from the input to the
		// output array.
		System.arraycopy
		    (bytesI,
		     fromI + idx*getStride(),
		     bytesO,
		     (toI + idx)*getPtsize(),
		     getPtsize());
	    }
	}
    }

    /**
     * Copies primitives from the input data array to the output data array
     * based on the information in this <code>DataReference</code>.
     * <p>
     * When the input and output arrays are byte arrays, the method uses
     * the <code>copyBytes</code> method.
     * <p>
     * When the input array is a byte array and the output array is an array
     * of arrays of bytes, the method calls the <code>copyByteArrays</code>
     * method.
     * <p>
     * When the input array is a byte array and the output array is an array
     * of some other primitive type, the method calls the
     * <code>convertBytesToArray</code> method.
     * <p>
     * When the input is a string and the output is a string array, the
     * method uses the <code>copyStringToStringArray</code> method.
     * <p>
     * When the input is a string array and the output is a string array, the
     * method uses the <code>copyStringArray</code> method.
     * <p>
     * For all other cases, the input and output array match and the method
     * uses the <code>copyArray</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputI    the input data array.
     * @param fromI     the starting byte of the input data array.
     * @param outputI   the output data array.
     * @param toI       the starting index into the output data array.
     * @param nPointsI  the number of points to copy. -1 means all.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem with the data sizes.
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
    private final void copyDataFromArray(Object inputI,
					 int fromI,
					 Object outputI,
					 int toI,
					 int nPointsI)
    {
	int nPoints = (nPointsI == -1) ? getNpts() : nPointsI;

	if (inputI instanceof byte[]) {
	    if (outputI instanceof byte[]) {
		copyBytes((byte[]) inputI,
			  fromI + getOffset(),
			  (byte[]) outputI,
			  toI,
			  nPoints);
	    } else {
		convertBytesToArray((byte[]) inputI,
				    fromI + getOffset(),
				    outputI,
				    toI,
				    nPoints);
	    }

	} else if (inputI instanceof byte[][]) {
	    copyByteArrays
		((byte[][]) inputI,
		 (fromI + getOffset())/getPtsize(),
		 (byte[][]) outputI,
		 toI,
		 nPoints);

	} else if (inputI instanceof String) {
	    copyStringToStringArray
		((String) inputI,
		 fromI + getOffset(),
		 (String[]) outputI,
		 toI,
		 nPoints);

	} else if (inputI instanceof String[]) {
	    copyStringArray
		((String[]) inputI,
		 (fromI + getOffset())/getPtsize(),
		 (String[]) outputI,
		 toI,
		 nPoints);

	} else {
	    copyArray(inputI,
		      (fromI + getOffset())/getPtsize(),
		      outputI,
		      toI,
		      nPoints);
	}
    }

    /**
     * Copies characters from the input string array to the output string array
     * for this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputI	the input string array.
     * @param fromI	the starting input into the input string.
     * @param outputI	the output string array.
     * @param toI	the starting index into the output string buffer.
     * @param nPointsI  the number of points to copy.
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
    private final void copyStringArray(String[] inputI,
				       int fromI,
				       String[] outputI,
				       int toI,
				       int nPointsI)
    {
	for (int idx = 0,
		 dPtsize = getPtsize(),
		 dStride = getStride()/dPtsize,
		 from = fromI,
		 to = toI;
	     idx < nPointsI;
	     ++idx,
		 from += dStride,
		 ++to) {
	    // Copy the characters for a single point from the input to the
	    // output.
	    if (outputI[to] == null) {
		outputI[to] = new String();
	    }
	    outputI[to] = inputI[from];
	}
    }

    /**
     * Copies characters from the input string to the output string array
     * for this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputI	the input string.
     * @param fromI	the starting input into the input string.
     * @param outputI	the output string array.
     * @param toI	the starting index into the output string buffer.
     * @param nPointsI  the number of points to copy.
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
    private final void copyStringToStringArray(String inputI,
					       int fromI,
					       String[] outputI,
					       int toI,
					       int nPointsI)
    {
	for (int idx = 0,
		 dStride = getStride(),
		 dPtsize = getPtsize(),
		 from = fromI,
		 to = toI;
	     idx < nPointsI;
	     ++idx,
		 from += dStride,
		 ++to) {
	    // Copy the characters for a single point from the input to the
	    // output.
	    if (outputI[to] == null) {
		outputI[to] = new String();
	    }
	    outputI[to] = inputI.substring(to,to + dPtsize);
	}
    }

    /**
     * Creates an output buffer for this data pool for the input
     * <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dRefI  the <code>DataBlock</code>.
     * @return the output array.
     * @exception java.lang.IllegalStateException
     *		  thrown if the output type does not match the input type.
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
    private final Object createOutputBuffer(DataBlock dRefI) {
	Object entry = getData().firstElement(),
	       bufferR = null;
	int    nPts = getNpts()*dRefI.getNpts();

	if (entry instanceof byte[]) {
	    bufferR = new byte[nPts*dRefI.getPtsize()];

	} else if (entry instanceof byte[][]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_BYTEARRAY)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from BOOLEAN to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new byte[nPts][];

	} else if (entry instanceof boolean[]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_BOOLEAN)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from BOOLEAN to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new short[nPts];

	} else if (entry instanceof short[]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_INT16)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from INT16 to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new short[nPts];

	} else if (entry instanceof int[]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_INT32)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from INT32 to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new int[nPts];

	} else if (entry instanceof long[]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_INT64)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from INT64 to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new long[nPts];
	} else if (entry instanceof float[]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_FLOAT32)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from FLOAT32 to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new float[nPts];

	} else if (entry instanceof double[]) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_FLOAT64)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from FLOAT64 to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new double[nPts];

	} else if ((entry instanceof String) ||
		   (entry instanceof String[])) {
	    if ((dRefI.getDtype() != UNKNOWN) &&
		(dRefI.getDtype() != TYPE_STRING)) {
		throw new java.lang.IllegalStateException
		    ("Cannot convert from STRING to " +
		     TYPES[dRefI.getDtype()] + ".");
	    }
	    bufferR = new String[nPts];
	}

	return (bufferR);
    }

    /**
     * Fills in missing parameters from the input <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataBlock</code>.
     * @param seenI  the parameters seen.
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
     * 11/10/2006  EMF  Use marker if appropriate.
     * 07/30/2001  INB	Created.
     *
     */
    final void defaultParameters(DataBlock otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (otherI != null) {
	    if (((seenI == null) || !seenI[PAR_DRF]) &&
		(otherI.getDreference() != null)) {
		setDreference(otherI.getDreference().duplicate());
	    }

	    if ((seenI == null) || !seenI[PAR_PTS]) {
		setNpts(otherI.getNpts());
	    }

	    if ((seenI == null) || !seenI[PAR_SIZ]) {
		setPtsize(otherI.getPtsize());
	    }

	    if (((seenI == null) || !seenI[PAR_DAT]) &&
		(otherI.getData() != null) &&
		(getNpts()*getPtsize() > 0)) {
                //EMF 11/10/06: if previous DataBlock had marker
                // create marker instead of full byte array
		//byte[] dBytes = new byte[getNpts()*getPtsize()];
		//setData(dBytes);
		byte[] dBytes = null;
	        if ((otherI.getData().size() == 1) &&
	            (otherI.getData().firstElement() instanceof byte[]) &&
		    (((byte[]) otherI.getData().firstElement()).length == 0)) {
                    dBytes = new byte[0];
	        } else {
                    dBytes = new byte[getNpts()*getPtsize()];
	        }
		setData(dBytes);
	    }
	}		
    }

    /**
     * Copies this <code>DataBlock</code> without copying the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the copy.
     * @since V2.0
     * @version 10/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2001  INB	Created.
     *
     */
    final DataBlock duplicate() {
	DataBlock copyR = (DataBlock) super.clone();

	if (getDreference() != null) {
	    copyR.setDreference(null);
	    copyR.setDreference(getDreference().duplicate());
	}

	return (copyR);
    }

    /**
     * Extracts data from this data pool for the input <code>DataBlock</code>.
     * <p>
     * This method supports extracting from byte arrays in all cases and from
     * other primitive types so long as the extraction doesn't change the type.
     * <p>
     * When extracting from an array of bytes or from an vector of arrays of
     * bytes, the method creates an output byte array and fills it in using
     * <code>System.arraycopy</code>.
     * <p>
     * When extracting from an array of some other primitive type, the method
     * creates an output array of that data type and fills it in using <code>
     * System.arraycopy</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dRefI  the <code>DataReference</code> (<code>DataBlock</code>).
     * @return the extracted data.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a mismatch between this data pool and the
     *		  the <code>DataReference</code>.
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
    public final Object extractData(DataBlock dRefI) {
	if ((getDtype() != UNKNOWN) &&
	    (dRefI.getDtype() != getDtype())) {
	    throw new java.lang.IllegalStateException
		("Cannot convert from " + TYPES[getDtype()] +
		 " to " + TYPES[dRefI.getDtype()] + ".");
	} else if ((dRefI.getPtsize() > getPtsize()) ||
		   ((getPtsize() % dRefI.getPtsize()) != 0)) {
	    throw new java.lang.IllegalStateException
		("Cannot extract " + dRefI.getPtsize() +
		 " byte points from " + getPtsize() +
		 " byte data pool entries.");
	}

	// The total number of points is equal to the number of points in this
	// data pool times the number of points in the
	// <code>DataBlock/code>. Create an output object of the correct type.
	Object dataR = createOutputBuffer(dRefI);

	if ((getStride() == getPtsize()) &&
	    (dRefI.getNpts() == 1) &&
	    ((getPtsize() == dRefI.getPtsize()) ||
	     (getPtsize() == dRefI.getStride()))) {
	    // If we're dealing with a case where we're copying whole points
	    // and the points are contiguous, do the copy in a single pass per
	    // element of the vector.
	    for (int idx = 0,
		     offset = getOffset(),
		     point = 0,
		     remaining = getNpts(),
		     nEntries = getData().size();
		 idx < nEntries;
		 ++idx) {
		int nPoints = Math.min(pointsInElement(idx),remaining);
		dRefI.copyDataFromArray
		    (getData().elementAt(idx),
		     offset,
		     dataR,
		     point,
		     nPoints);
		offset = 0;
		point += nPoints;
		remaining -= nPoints;
	    }

	} else {
	    // If we have to skip through the input, do so.
	    for (int idx = 0,
		     idx1 = 0,
		     sOffset = 0,
		     nBytes = bytesInElement(idx1),
		     pStride = getStride(),
		     pNpts = getNpts(),
		     dNpts = dRefI.getNpts(),
		     from = getOffset(),
		     to = 0,
		     nvPts = getNpts(),
		     nEntries = getData().size();
		 idx < nvPts;
		 ++idx,
		     from += pStride,
		     to += dNpts) {
		dRefI.copyDataFromArray
		    (getData().elementAt(idx1),
		     from - sOffset,
		     dataR,
		     to,
		     dNpts);
		if (from + pStride - sOffset >= nBytes) {
		    ++idx1;
		    sOffset += nBytes;
		    if (idx1 < nEntries) {
			nBytes = bytesInElement(idx1);
		    }
		}
	    }
	}

	return (dataR);
    }

    /**
     * Extracts inherited information from this <code>DataBlock</code> and the
     * input <code>DataBlock</code> to produce a new inherted
     <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dBlockI    the <code>DataBlock</code> inherited from above.
     * @param dataFlagI  extract the data?
     * @return the new <code>DataBlock</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a mismatch the input
     *		  <code>DataBlock</code> and this one.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    DataBlock extractInheritance(DataBlock dBlockI,boolean dataFlagI) {
	DataBlock dBlockR = null;

	// If we need the data payload and this <code>Rmap</code> has a
	// </code>DataBlock, we may be able to make use of the
	// <code>DataBlock</code>.
	if (dBlockI == null) {
	    // If this is the <code>Rmap</code> containing the
	    // <code>DataBlock</code> with the data payload, clone the
	    // <code>DataBlock</code>.
	    dBlockR = (DataBlock) clone();

	} else {
	    // If this <code>DataBlock</code> inherits from the
	    // <code>Rmap</code> that had the data payload, extract
	    // <code>DataBlock's</code> data from the payload.
	    byte lwOrder = getWorder();

	    if (lwOrder == DataBlock.UNKNOWN) {
		lwOrder = dBlockI.getWorder();
	    }

	    Object dp =	dataFlagI ?
		dBlockI.extractData(this) :
		new byte[0];
	    dBlockR = new DataBlock
		(dp,
		 dBlockI.getNpts()*getNpts(),
		 getPtsize(),
		 getDtype(),
		 getMIMEType(),
		 lwOrder,
		 getIndivFlg(),
		 0,
		 0);
	}

	return (dBlockR);
    }

    /**
     * Gets the data payload.
     * <p>
     * The data payload is a <code>java.util.Vector</code> containing either:
     * <p><ul>
     * <li>A single array of primitives (shorts or floats, etc.), or</li>
     * <li>One byte array per data point.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @return the data payload.
     * @see #setData(Object)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final java.util.Vector getData() {
	return (data);
    }

    /**
     * Gets the data for one or more points starting at the specified point
     * and copies it into the output array starting at the specified index.
     * <p>
     * This method assumes that the <code>DataBlock</code> contains both the
     * data and the <code>DataReference</code>. The offset and stride should be
     * defaults. The method does not check for this state.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startI	the starting point.
     * @param outputI	the output array.
     * @param indexI	the starting index.
     * @param nPointsI  the number of points to copy. -1 is all.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem with the data sizes.
     * @since V2.0
     * @version 11/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/10/2003  INB	Added <code>copyDataFromArray</code> case to the
     *			multiple data elements case to handle things on the
     *			server side.
     * 11/30/2000  INB	Created.
     *
     */
    final void getDataPoints(int startI,
			     Object outputI,
			     int indexI,
			     int nPointsI)
    {
	if (getData().size() == 1) {
	    copyDataFromArray(getData().firstElement(),
			      startI*getPtsize(),
			      outputI,
			      indexI,
			      nPointsI);
	} else {
	    for (int idx = 0,
		     from = startI,
		     to = indexI;
		 idx < nPointsI;
		 ++idx,
		     ++from,
		     ++to) {
		if (getData().getClass() != outputI.getClass()) {
		    copyDataFromArray(getData().elementAt(from),
				      0,
				      outputI,
				      to,
				      1);
		} else {
		    copyArray(getData().elementAt(from),
			      0,
			      outputI,
			      to,
			      1);
		}
	    }
	}
    }

    /**
     * Gets the <code>DataReference</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>DataReference</code>
     * @see #setDreference(com.rbnb.api.DataReference)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final DataReference getDreference() {
	return (reference);
    }

    /**
     * Gets the type of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the type of data.
     * @see #setDtype(byte)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final byte getDtype() {
	return ((getDreference() == null) ?
		DataBlock.UNKNOWN :
		getDreference().getDataType());
    }

    /**
     * Gets the indivisible flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return individual points are not accessible?
     * @see #setIndivFlg(boolean)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final boolean getIndivFlg() {
	return ((getDreference() == null) ?
		false :
		getDreference().getIndivisibility());
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
    public final String getMIMEType() {
	return ((getDreference() == null) ?
		null :
		getDreference().getMIMEType());
    }

    /**
     * Gets the number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of points.
     * @see #setNpts(int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getNpts() {
	return (numPoints);
    }

    /**
     * Gets the number of bytes to the first point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes to the first point.
     * @see #setOffset(int)
     * @see #setOffsetStride(int,int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getOffset() {
	return ((getDreference() == null) ?
		0 :
		getDreference().getOffset());
    }

    /**
     * Gets the size of a point in bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the point size in bytes.
     * @see #setPtsize(int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getPtsize() {
	return (pointSize);
    }

    /**
     * Gets the number of bytes to the next point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes to the next point.
     * @see #setStride(int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getStride() {
	int rStride = ((getDreference() == null) ?
		       0 :
		       getDreference().getStride());

	return ((rStride == 0) ? getPtsize() : rStride);
    }

    /**
     * Gets the order of bytes within a point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the word order.
     * @see #setWorder(byte)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final byte getWorder() {
	return ((getDreference() == null) ?
		UNKNOWN :
		getDreference().getWordOrder());
    }

    /**
     * Nullifies this <code>DataBlock</code>.
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
	if (reference != null) {
	    reference.nullify();
	    reference = null;
	}
	if (data != null) {
	    data.removeAllElements();
	    data = null;
	}
    }

    /**
     * Returns the number of points in the specified element of the data
     * vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the element index.
     * @return the number of points.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/08/2001  INB	Created.
     *
     */
    final int pointsInElement(int indexI) {
	Object element = getData().elementAt(indexI);
	int nPointsR = 0;

	if (element instanceof byte[][]) {
	    byte[][] bElement = (byte[][]) element;
	    nPointsR = bElement.length*bElement[0].length/getPtsize();
	} else if (element instanceof String) {
	    nPointsR = ((String) element).length()/getPtsize();
	} else {
	    int nElements = java.lang.reflect.Array.getLength(element);
	    if ((element instanceof byte[]) ||
		(element instanceof boolean[])) {
		nPointsR = nElements/getPtsize();

	    } else if (element instanceof short[]) {
		nPointsR = nElements*2/getPtsize();

	    } else if ((element instanceof int[]) ||
		       (element instanceof float[])) {
		nPointsR = nElements*4/getPtsize();

	    } else if ((element instanceof long[]) ||
		       (element instanceof double[])) {
		nPointsR = nElements*8/getPtsize();

	    } else if (element instanceof String[]) {
		String[] sElement = (String[]) element;
		nPointsR = nElements*sElement[0].length()/getPtsize();
	    }
	}

	if ((indexI == 0) && (getOffset() > 0)) {
	    nPointsR -= getOffset()/getPtsize();
	}

	return (nPointsR);
    }

    /**
     * Reads the <code>DataBlock</code> from the specified input stream.
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
     *		  thrown if this operation is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	read(null,isI,disI);
    }

    /**
     * Reads the <code>DataBlock</code> from the specified input stream.
     * <p>
     * This version uses an input <code>DataBlock</code> to provide the default
     * values for all of the fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataBlock</code>.
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
     *		  thrown if this operation is interrupted.
     * @see #write(com.rbnb.api.DataBlock,String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final void read(DataBlock otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] seen = new boolean[PARAMETERS.length];

	// Read the open bracket marking the start of the
	// <code>DataBlock</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    // Read parameters until we see a closing bracket.
	    seen[parameter] = true;

	    switch (parameter) {
	    case PAR_DAT:
		if ((getNpts()*getPtsize() <= 0) && (otherI != null)) {
		    if (!seen[PAR_PTS]) {
			setNpts(otherI.getNpts());
			seen[PAR_PTS] = true;
		    }
		    if (!seen[PAR_SIZ]) {
			setPtsize(otherI.getPtsize());
			seen[PAR_SIZ] = true;
		    }
		}
		if (getNpts()*getPtsize() <= 0) {
		    throw new com.rbnb.api.SerializeException
			("The number of points and the size of a point in " +
			 "bytes must preceed the data block.");
		}
		isI.readByte();

		byte[] dLoad;
		if (disI == null) {
		    dLoad = new byte[0];
		} else {
		    dLoad = new byte[getNpts()*getPtsize()];
		}
		setData(dLoad);
		break;

	    case PAR_DRF:
		setDreference(new DataReference(((otherI == null) ?
						 null :
						 otherI.getDreference()),
						isI,
						disI));
		break;
		
	    case PAR_PTS:
		setNpts(isI.readInt());
		break;

	    case PAR_SIZ:
		setPtsize(isI.readInt());
		break;
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads the data for this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param disI the data input stream.
     * @exception java.io.EOFException
     *		  thrown if the end of the stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @see #writeData(com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2006  EMF  Move data file pointer even if not reading, so
     *                  offsets file will have correct values.
     * 08/15/2001  INB	Created.
     *
     */
    final void readData(DataInputStream disI)
	throws java.io.EOFException,
	       java.io.IOException
    {
	if (getData() != null) {
          //EMF 11/14/06: keep pointer in right place even when not reading
          //              data, so offsets.rbn file will have correct values
          if (((byte[])getData().firstElement()).length==0) {
            disI.skipBytes(getNpts()*getPtsize());
          } else {
	    disI.read((byte[]) getData().firstElement());
          }
	}
    }

    /**
     * Sets the data payload and description using basic values.
     * <p>
     * The extended <code>DataReference</code> values are defaulted as follows:
     * <p>
     * <ul>
     * <li><code>dtype</code> is set to <code>DataBlock.UNKNOWN</code>,</li>
     * <li><code>worder</code> is set to <code>DataBlock.UNKNOWN</code>,
     * </li>
     * <li><code>indivflg</code> is set to false,</li>
     * <li><code>offset</code> is set to 0, and</li>
     * <li><code>stride</code> is set to <code>ptsizeI</code>.</li></ul>
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI	 the data payload.
     * @param ndataI	 the number of points.
     * @param ptsizeI	 the number of bytes in a single point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of points or point size are not
     *		  positive or if they do not match the size of the data
     *		  payload.
     * @see #getData()
     * @see #getNpts()
     * @see #getPtsize()
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(Object dataI,
			  int ndataI,
			  int ptsizeI)
    {
	set(dataI,
	    ndataI,
	    ptsizeI,
	    DataBlock.UNKNOWN,
	    DataBlock.UNKNOWN,
	    false,
	    0,
	    ptsizeI);
    }

    /**
     * Sets the data payload and description from the input values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI      the data payload.
     * @param ndataI     the number of points.
     * @param ptsizeI    the number of bytes in a single point.
     * @param dtypeI	 the type of data.
     * @param worderI	 the word order of the data.
     * @param indivflgI	 are points are individually accessible?
     * @param offsetI	 the offset in bytes to the first point.
     * @param strideI	 the stride in bytes to the next point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li> the number of points is not positive,</li>
     *		  <li> the point size is not positive,</li>
     *		  <li> the offset is not 0 or positive,</li>
     *		  <li> the stride is not 0 or positive,</li>
     *		  <li> the number of points or point size does not match the
     *		       size of the data payload,</li>
     *		  <li> the offset is non-zero and the data payload is not
     *		       null,</li>
     *		  <li> the stride is non-zero and not equal to the point size
     *		       and the data payload is not null,</li>
     *		  <li> the data type does not match the point size or data
     *		       payload type.</li>
     *		  </ul>
     * @see #getData()
     * @see #getDtype()
     * @see #getIndivFlg()
     * @see #getNpts()
     * @see #getOffset()
     * @see #getPtsize()
     * @see #getStride()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(Object dataI,
			  int ndataI,
			  int ptsizeI,
			  byte dtypeI,
			  byte worderI,
			  boolean indivflgI,
			  int offsetI,
			  int strideI)
    {
	set(dataI,
	    ndataI,
	    ptsizeI,
	    dtypeI,
	    null,
	    worderI,
	    indivflgI,
	    offsetI,
	    strideI);
    }

    /**
     * Sets the data payload and description from the input values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI      the data payload.
     * @param ndataI     the number of points.
     * @param ptsizeI    the number of bytes in a single point.
     * @param dtypeI	 the type of data.
     * @param mimeTypeI  the MIME type.
     * @param worderI	 the word order of the data.
     * @param indivflgI	 are points are individually accessible?
     * @param offsetI	 the offset in bytes to the first point.
     * @param strideI	 the stride in bytes to the next point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li> the number of points is not positive,</li>
     *		  <li> the point size is not positive,</li>
     *		  <li> the offset is not 0 or positive,</li>
     *		  <li> the stride is not 0 or positive,</li>
     *		  <li> the number of points or point size does not match the
     *		       size of the data payload,</li>
     *		  <li> the offset is non-zero and the data payload is not
     *		       null,</li>
     *		  <li> the stride is non-zero and not equal to the point size
     *		       and the data payload is not null,</li>
     *		  <li> the data type does not match the point size or data
     *		       payload type.</li>
     *		  </ul>
     * @see #getData()
     * @see #getDtype()
     * @see #getIndivFlg()
     * @see #getNpts()
     * @see #getOffset()
     * @see #getPtsize()
     * @see #getStride()
     * @since V2.0
     * @version 02/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void set(Object dataI,
			  int ndataI,
			  int ptsizeI,
			  byte dtypeI,
			  String mimeTypeI,
			  byte worderI,
			  boolean indivflgI,
			  int offsetI,
			  int strideI)
    {
	byte inputType = UNKNOWN;

	if (ndataI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The number of points must be positive.");
	} else if (ptsizeI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The point size must be positive.");
	} else if (offsetI < 0) {
	    throw new java.lang.IllegalArgumentException
		("The offset must be greater than or equal to 0.");
	} else if (strideI < 0) {
	    throw new java.lang.IllegalArgumentException
		("The stride must be greater than or equal to 0.");
	}

	// Save the inputs.
	setData(dataI);
	setNpts(ndataI);
	setPtsize(ptsizeI);

	if (dataI != null) {
	    // When we have a data payload, we want to ensure that the sizes
	    // given match it.
	    if (offsetI > 0) {
		throw new java.lang.IllegalArgumentException
		    ("The offset must be zero when the data payload is " +
		     "provided.");
	    } else if ((strideI > 0) && (strideI != ptsizeI)) {
		throw new java.lang.IllegalArgumentException
		    ("The stride must be equal to the point size when the " +
		     "data payload is provided.");
	    }

	    // The input must be either an array of some primitive type or
	    // a vector of such arrays. In the latter case, we check all of
	    // the entries.
	    int nPoints = ndataI,
		totalSize = 0;

	    for (int idx = 0, eIdx = getData().size();
		 idx < eIdx;
		 ++idx) {
		Object element = getData().elementAt(idx);
		byte lInputType = UNKNOWN;

		if (element instanceof byte[]) {
		    lInputType = DataBlock.TYPE_INT8;
		} else if (element instanceof byte[][]) {
		    lInputType = DataBlock.TYPE_BYTEARRAY;
		} else if (element instanceof boolean[]) {
		    lInputType = DataBlock.TYPE_BOOLEAN;
		} else if (element instanceof short[]) {
		    lInputType = DataBlock.TYPE_INT16;
		} else if (element instanceof int[]) {
		    lInputType = DataBlock.TYPE_INT32;
		} else if (element instanceof long[]) {
		    lInputType = DataBlock.TYPE_INT64;
		} else if (element instanceof float[]) {
		    lInputType = DataBlock.TYPE_FLOAT32;
		} else if (element instanceof double[]) {
		    lInputType = DataBlock.TYPE_FLOAT64;
		} else if (element instanceof String) {
		    lInputType = DataBlock.TYPE_STRING;
		}
		if ((inputType != UNKNOWN) && (lInputType != inputType)) {
		    inputType = UNKNOWN;
		} else if (idx == 0) {
		    inputType = lInputType;
		}

		int bie = bytesInElement(idx);
		if (bie == 0) {
		    throw new java.lang.IllegalArgumentException
			("All elements in a data vector must be an " +
			 "integer (>0) multiple of the point size.");
		}
		totalSize += bie;
	    }

	    if (totalSize != ndataI*ptsizeI) {
		throw new java.lang.IllegalArgumentException
		    ("The total amount of data (" + totalSize + " bytes) " +
		     "does not match the specified amount (" + ndataI*ptsizeI +
		     " bytes).");
	    }
	}

	if (dtypeI == TYPE_FROM_INPUT) {
	    if (inputType != UNKNOWN) {
		setDtype(inputType);
	    }
	} else if (dtypeI != UNKNOWN) {
	    setDtype(dtypeI);
	}
	setMIMEType(mimeTypeI);
	if (worderI != UNKNOWN) {
	    setWorder(worderI);
	}
	if (indivflgI) {
	    setIndivFlg(indivflgI);
	}
	if ((offsetI != 0) || (strideI != 0)) {
	    setOffsetStride(offsetI,strideI);
	}
    }

    /**
     * Sets the data payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI  the data payload.
     * @see #getData()
     * @since V2.0
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>Vector(1)</code> rather than
     *			<code>Vector()</code>.
     * 11/30/2000  INB	Created.
     *
     */
    public final void setData(Object dataI) {
	if (dataI != null) {
	    if (dataI instanceof java.util.Vector) {
		data = (java.util.Vector) dataI;
	    } else {
		data = new java.util.Vector(1);
		data.addElement(dataI);
	    }
	}
    }

    /**
     * Sets the <code>DataReference</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dRefI  the <code>DataReference</code>.
     * @exception java.lang.IllegalState
     *		  thrown if the input <code>DataReference</code> is non-null
     *		  and there is already a <code>DataReference</code> for this
     *		  <code>DataBlock</code>.
     * @see #getDreference()
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final void setDreference(DataReference dRefI) {
	if ((dRefI != null) &&
	    (getDreference() != null) &&
	    (dRefI != getDreference())) {
	    throw new java.lang.IllegalStateException
		("Cannot change the DataReference once it has been set.");
	}

	reference = dRefI;
    }

    /**
     * Sets the type of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dtypeI  the type of data.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the data type doesn't match the point size or the
     *		  type of the data payload.
     * @see #getDtype()
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setDtype(byte dtypeI) {
	if (dtypeI == TYPE_FROM_INPUT) {
	    throw new java.lang.IllegalArgumentException
		("Cannot set the type from input after the fact.");
	}

	int expectedSize = TYPE_SIZES[dtypeI];

	if (expectedSize != 0) {
	    // If there is an expected size for the specified type, check it
	    // against the point size and the primitive type of the data
	    // payload.

	    if (expectedSize != getPtsize()) {
		throw new java.lang.IllegalArgumentException
		    ("The data type does not match the point size.");
	    }

	    if (getData() != null) {
		Object element = getData().firstElement();

		if (((element instanceof byte[][]) &&
		     ((dtypeI != UNKNOWN) && (dtypeI != TYPE_BYTEARRAY))) ||
		    ((element instanceof boolean[]) &&
		     (dtypeI != TYPE_BOOLEAN)) ||
		    ((element instanceof short[]) && (dtypeI != TYPE_INT16)) ||
		    ((element instanceof int[]) && (dtypeI != TYPE_INT32)) ||
		    ((element instanceof long[]) && (dtypeI != TYPE_INT64)) ||
		    ((element instanceof float[]) &&
		     (dtypeI != TYPE_FLOAT32)) ||
		    ((element instanceof double[]) &&
		     (dtypeI != TYPE_FLOAT64)) ||
		    ((element instanceof String) &&
		     (dtypeI != TYPE_STRING))) {
		    throw new java.lang.IllegalArgumentException
			("The data type does not match the primitive type " +
			 "of the data payload.");
		}
	    }
	}

	if ((dtypeI != UNKNOWN) && (getDreference() == null)) {
	    // If the data type is known, make sure there is a reference.
	    setDreference(new DataReference());
	}

	if (getDreference() != null) {
	    // If there is a reference, set the data type.
	    getDreference().setDataType(dtypeI);

	    if (getDreference().isDefault(getPtsize())) {
		// Toss out the <code>DataReference</code> if it is now all
		// default values.
		setDreference(null);
	    }
	}
    }

    /**
     * Sets the indivisible flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indivflgI  individual points are not accessible?
     * @see #getIndivFlg()
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setIndivFlg(boolean indivflgI) {
	if (indivflgI && (getDreference() == null)) {
	    // If the flag is set, make sure that there is a reference.
	    setDreference(new DataReference());
	}

	if (getDreference() != null) {
	    // If there is a reference, set the indivisble flag.
	    getDreference().setIndivisibility(indivflgI);
	    
	    if (getDreference().isDefault(getPtsize())) {
		// Toss out the <code>DataReference</code> if it is now all
		// default values.
		setDreference(null);
	    }
	}
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
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    public final void setMIMEType(String mimeTypeI) {
	if ((mimeTypeI != null) && (getDreference() == null)) {
	    // If the MIME type is specified, make sure that there is a
	    // reference.
	    setDreference(new DataReference());
	}

	if (getDreference() != null) {
	    // If there is a reference, set the MIME type.
	    getDreference().setMIMEType(mimeTypeI);
	}
    }

    /**
     * Sets the number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nptsI  the number of points.
     * @see #getNpts()
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
    public final void setNpts(int nptsI) {
	numPoints = nptsI;
    }

    /**
     * Sets the number of bytes to the first point.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the number of bytes to the first point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the offset is negative or if there is a data
     *		  payload.
     * @see #getOffset()
     * @see #setOffsetStride(int,int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setOffset(int offsetI) {
	if (offsetI < 0) {
	    throw new java.lang.IllegalArgumentException
		("The offset must be positive or zero.");
	}

	if ((offsetI > 0) && (getDreference() == null)) {
	    // Ensure that there is a <code>DataReference</code> when the
	    // offset is positive.
	    setDreference(new DataReference());
	}

	if (getDreference() != null) {
	    // Set the offset when the <code>DataReference</code> is non-null.
	    getDreference().setOffset(offsetI);
	    
	    if (getDreference().isDefault(getPtsize())) {
		// Toss out the <code>DataReference</code> if it is now all
		// default values.
		setDreference(null);
	    }
	}
    }

    /**
     * Sets the offset in bytes to the start of the first point and the
     * stride in bytes between points.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  The number of bytes to the first point.
     * @param strideI  The number of bytes between points.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the offset is negative, the stride is nnegative,
     *		  or if there is a data payload.
     * @see #getOffset()
     * @see #getStride()
     * @see #setOffset(int)
     * @see #setStride(int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setOffsetStride(int offsetI,int strideI) {
	setOffset(offsetI);
	setStride(strideI);
    }

    /**
     * Sets the size of a point in bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ptsizeI  the point size in bytes.
     * @see #getPtsize()
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
    public final void setPtsize(int ptsizeI) {
	pointSize = ptsizeI;
    }

    /**
     * Sets the number of bytes to the next point.
     * <p>
     *
     * @author Ian Brown
     *
     * @param strideI  the number of bytes to the next point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the stride is negative or if there is a data
     *		  payload and the stride is non-zero and not equal to the point
     *		  size.
     * @see #getStride()
     * @see #setOffsetStride(int,int)
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setStride(int strideI) {
	if (strideI < 0) {
	    throw new java.lang.IllegalArgumentException
		("The stride must be positive or zero.");
	}

	if ((strideI > 0) &&
	    (strideI != getPtsize()) &&
	    (getDreference() == null)) {
	    // Ensure that there is a <code>DataReference</code> when the
	    // stride is positive.
	    setDreference(new DataReference());
	}

	if (getDreference() != null) {
	    // Set the stride when the <code>DataReference</code> is non-null.
	    getDreference().setStride(strideI);
	    
	    if (getDreference().isDefault(getPtsize())) {
		// Toss out the <code>DataReference</code> if it is now all
		// default values.
		setDreference(null);
	    }
	}
    }

    /**
     * Sets the order of bytes within a point.
     * <p>
     *
     * @author Ian Brown
     *
     * @param worderI  the word order.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the word order is not a legal value.
     * @see #getWorder()
     * @since V2.0
     * @version 08/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setWorder(byte worderI) {
	if ((worderI != UNKNOWN) && (getDreference() == null)) {
	    // Ensure that there is a <code>DataReference</code> when there is
	    // an order.
	    setDreference(new DataReference());
	}

	if (getDreference() != null) {
	    // Set the worder order when the <code>DataReference</code> is
	    // non-null.
	    getDreference().setWordOrder(worderI);
	    
	    if (getDreference().isDefault(getPtsize())) {
		// Toss out the <code>DataReference</code> if it is now all
		// default values.
		setDreference(null);
	    }
	}
    }

    /**
     * Gets a displayable string representation of this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 11/26/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final String toString() {
	String stringR = "";

	if (getData() != null) {
	    if ((getData().size() == 1) &&
		(getData().firstElement() instanceof byte[]) &&
		(((byte[]) getData().firstElement()).length == 0)) {
		stringR += "Marker ";
	    } else {
		stringR += "Payload ";
	    }
	}
	stringR += getNpts() + "x" + getPtsize();
	if (getDreference() != null) {
	    stringR += " (" + getDreference() + ")";
	}

	/*
	if (getData() != null) {
	    if ((getData().size() == 1) &&
		(getData().firstElement() instanceof byte[]) &&
		(((byte[]) getData().firstElement()).length == 0)) {
	    } else {
		try {
		    stringR += "\n";
		    StringBuffer sb = new StringBuffer("");
		    Rmap tRmap;
		    tRmap = new Rmap(null,
				     this,
				     new TimeRange(0.,getNpts()));
		    DataArrayExtractor dae = new DataArrayExtractor();
		    dae.addInformation(tRmap);
		    dae.setDtype(getDtype());
		    dae.setNpoints(getNpts());
		    dae.setPtsize(getPtsize());
		    DataArray da = dae.getTimeAndData(false,true,true);
		    Object myData = da.getData();
		    double[] myTimes = da.getTime();
		    if (getDtype() == TYPE_BYTEARRAY) {
			byte[][] ba = (byte[][]) myData;
			for (int idx = 0; idx < myTimes.length; ++idx) {
			    stringR += "   T" + ((int) myTimes[idx]);
			    for (int idx2 = 0; idx2 < ba[idx].length; ++idx2) {
				sb.append(" ");
				sb.append(ba[idx][idx2]);
			    }
			}
		    } else {
			for (int idx = 0; idx < myTimes.length; ++idx) {
			    sb.append("   ");
			    sb.append(((int) myTimes[idx]) +
				      " " +
				      java.lang.reflect.Array.get(myData,idx));
			}
		    }
		    stringR += sb.toString();

		} catch (java.lang.Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	*/

	return (stringR);
    }

    /**
     * Writes this <code>DataBlock</code> to the specified stream.
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
     * 01/08/2001  INB	Created.
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
     * Writes this <code>DataBlock</code> to the specified stream.
     * <p>
     * This method determines what has changed between this
     * <code>DataBlock</code> and the input one and writes out only the
     * changes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the <code>DataBlock</code> to compare to.
     *			   <p>
     *			   If <code>null</code>, then this
     *			   <code>DataBlock</code> is always written out in
     *			   full.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @see #read(com.rbnb.api.DataBlock,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 03/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2003  INB	Took out unnecessary flush.
     * 07/26/2001  INB	Created.
     *
     */
    final void write(DataBlock otherI,
		     String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws java.io.IOException
    {
	long before = osI.getWritten();
	boolean dbChanged;

	if (otherI == null) {
	    dbChanged = true;
	} else {
	    dbChanged =
		(getNpts() != otherI.getNpts()) ||
		(getPtsize() != otherI.getPtsize());
	}

	int valid = osI.setStage(true,false);
	osI.addStaged(this,parametersI,parameterI);
	osI.setStage(false,false);

	if ((getDreference() != null) &&
	    ((otherI == null) ||
	     (getDreference() != otherI.getDreference()))) {
	    getDreference().write(((otherI == null) ?
				   null :
				   otherI.getDreference()),
				  PARAMETERS,
				  PAR_DRF,
				  osI,
				  dosI);
	    dbChanged = dbChanged || (osI.getWritten() > before);
	}

	if (dbChanged) {
	    osI.writeParameter(PARAMETERS,PAR_PTS);
	    osI.writeInt(getNpts());
	    osI.writeParameter(PARAMETERS,PAR_SIZ);
	    osI.writeInt(getPtsize());

	    if (getData() != null) {
		osI.writeParameter(PARAMETERS,PAR_DAT);
		osI.writeByte(0);
	    }
	}

	if (dbChanged || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0)  {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes the data for this <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dosI the data output stream.
     * @exception java.io.EOFException
     *		  thrown if the end of the stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @see #readData(com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2001  INB	Created.
     *
     */
    final void writeData(DataOutputStream dosI)
	throws java.io.EOFException,
	       java.io.IOException
    {
	if (getData() != null) {
	    dosI.write(getData());
	}
    }
}
