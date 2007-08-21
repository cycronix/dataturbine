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
 * Describes the data that belongs to an <code>Rmap</code>.
 * <p>
 * The <code>DataReference<code> contains the data description for a
 * <code>DataBlock</code> object. It consists of:
 * <p><ul>
 * <li>The data primitive type (INT16, FLOAT32, etc.),</li>
 * <li>The order of bytes for multi-byte values (MSB, LSB, UNKNOWN),</li>
 * <li>A flag indicating the indivisibility of the data,</li>
 * <li>The offset in bytes to the first point, and</li>
 * <li>The stride in bytes from the start of one point to the start of the
 *     next.</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataBlock
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2000, 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/05/2004  INB	Fixed a minor detail in the class documentation.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 11/30/2000  INB	Created.
 *
 */
final class DataReference
    extends com.rbnb.api.Serializable
{
    // Private fields:
    /**
     * individual points are not time-addressable?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private boolean indivisibleFlag = false;

    /**
     * order of multiple bytes in a single point.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.DataBlock
     * @since V2.0
     * @version 11/30/2000
     */
    private byte wordOrder = DataBlock.UNKNOWN;

    /**
     * type of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.DataBlock
     * @since V2.0
     * @version 11/30/2000
     */
    private byte dataType = DataBlock.UNKNOWN;

    /**
     * the MIME type string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/24/2002
     */
    private String mimeType = null;

    /**
     * number of bytes in the data block preceeding the first data point.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private int offset = 0;

    /**
     * number of bytes between data points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private int stride = 0;

    // Private constants:
    private final static byte PAR_IND = 0;
    private final static byte PAR_OFF = 1;
    private final static byte PAR_ORD = 2;
    private final static byte PAR_STR = 3;
    private final static byte PAR_TYP = 4;
    private final static byte PAR_MIM = 5;

    private final static String[] PARAMETERS =
    {
	"IND",
	"OFF",
	"ORD",
	"STR",
	"TYP",
	"MIM"
    };

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    DataReference() {
	super();
    }

    /**
     * Class constructor to build a <code>DataReference</code> by reading it
     * from an input stream.
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
     * 01/08/2001  INB	Created.
     *
     */
    DataReference(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>DataReference</code> by reading it
     * from an input stream.
     * <p>
     * This version uses the input <code>DataReference</code> to set the
     * defaults for values not found when reading the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataReference</;code>.
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
    DataReference(DataReference otherI,
			 InputStream isI,
			 DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Clones this <code>DataReference</code>.
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
	return (super.clone());
    }

    /**
     * Copies this <code>DataReference</code>.
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
    final DataReference duplicate() {
	return ((DataReference) clone());
    }

    /**
     * Gets the type of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the type of data.
     * @see #setDataType(byte)
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
    final byte getDataType() {
	return (dataType);
    }

    /**
     * Gets the indivisibility flag for the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return individual points are not addressable?
     * @see #setIndivisibility(boolean)
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
    final boolean getIndivisibility() {
	return (indivisibleFlag);
    }

    /**
     * Gets the MIME type string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the MIME type string.
     * @see #setMIMEType(String)
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
    final String getMIMEType() {
	return (mimeType);
    }

    /**
     * Gets the offset to the first point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes to the first point.
     * @see #setOffset(int)
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
    final int getOffset() {
	return (offset);
    }

    /**
     * Gets the stride to the next point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes between points.
     * @see #setStride(int)
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
    final int getStride() {
	return (stride);
    }

    /**
     * Gets the word byte order.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the order of multiple bytes in a single point.
     * @see #setWordOrder(byte)
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
    final byte getWordOrder() {
	return (wordOrder);
    }

    /**
     * Are all of the values for this reference defaults?
     * <p>
     * The default values are:
     * <p><ul>
     * <li><code>dataType</code> = <code>DataBlock.UNKNOWN</code>,</li>
     * <li><code>WordOrder</code> = <code>DataBlock.UNKNOWN</code>,</li>
     * <li><code>indivisibleFlag</code> = false,</li>
     * <li><code>offset</code> = 0, and</li>
     * <li><code>stride</code> = 0 or <code>ptsizeI</code>.<li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param ptsizeI  the size of a point in bytes.
     * @return the values are set to their defaults?
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
    final boolean isDefault(int ptsizeI) {
	return ((getDataType() == DataBlock.UNKNOWN) &&
		(getWordOrder() == DataBlock.UNKNOWN) &&
		!getIndivisibility() &&
		(getOffset() == 0) &&
		((getStride() == 0) || (getStride() == ptsizeI)));
    }

    /**
     * Nullifies this <code>DataReference</code>.
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
	setMIMEType(null);
    }

    /**
     * Reads the <code>DataReference</code> from the specified input stream.
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
     * 01/08/2001  INB	Created.
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
     * Reads the <code>DataReference</code> from the specified input stream.
     * <p>
     * This version uses the input <code>DataReference</code> to provide the
     * defaults for values not read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>DataReference</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    final void read(DataReference otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	boolean[] seen = new boolean[PARAMETERS.length];

	// Read the open bracket marking the start of the
	// <code>DataReference</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    // Read parameters until we see a closing bracket.
	    seen[parameter] = true;

	    switch (parameter) {
	    case PAR_IND:
		setIndivisibility(isI.readBoolean());
		break;

	    case PAR_MIM:
		setMIMEType(isI.readUTF());
		break;

	    case PAR_OFF:
		setOffset(isI.readInt());
		break;

	    case PAR_ORD:
		setWordOrder((byte) isI.readElement(DataBlock.ORDERS));
		break;

	    case PAR_STR:
		setStride(isI.readInt());
		break;

	    case PAR_TYP:
		setDataType((byte) isI.readElement(DataBlock.TYPES));
		break;
	    }
	}
    }

    /**
     * Sets the type of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataTypeI  the type of data.
     * @see #getDataType()
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
    final void setDataType(byte dataTypeI) {
	dataType = dataTypeI;
    }

    /**
     * Sets the indivisibility flag for the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indivisibleFlagI  individual points are not addressable?
     * @see #getIndivisibility()
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
    final void setIndivisibility(boolean indivisibleFlagI) {
	indivisibleFlag = indivisibleFlagI;
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
     * Sets the offset to the first point.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  number of bytes to the first point.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the offset is negative.
     * @see #getOffset()
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
    final void setOffset(int offsetI) {
	if (offsetI < 0) {
	    throw new java.lang.IllegalArgumentException
		("The offset must be larger than or equal to 0.");
	}
	offset = offsetI;
    }

    /**
     * Sets the stride to the next point.
     * <p>
     *
     * @author Ian Brown
     *
     * @param strideI  number of bytes between points.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the stride is negative.
     * @see #getStride()
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
    final void setStride(int strideI) {
	if (strideI < 0) {
	    throw new java.lang.IllegalArgumentException
		("The stride must be larger than or equal to 0.");
	}
	stride = strideI;
    }

    /**
     * Sets the word byte order.
     * <p>
     *
     * @author Ian Brown
     *
     * @param wordOrderI  the order of multiple bytes in a single point.
     * @see #getWordOrder()
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
    final void setWordOrder(byte wordOrderI) {
	wordOrder = wordOrderI ;
    }

    /**
     * Gets a displayable string representation of this DataReference.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
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
    public final String toString() {
	return (DataBlock.TYPES[getDataType()] +
		", " + DataBlock.ORDERS[getWordOrder()] +
		(getIndivisibility() ? ", indivisible" : "") +
		", " + getOffset() + "+" + getStride() +
		((getMIMEType() == null) ? "" : (", " + getMIMEType())));
    }

    /**
     * Writes this <code>DataReference</code> to the specified stream.
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
     * @version 01/24/2002
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
     * Writes this <code>DataReference</code> to the specified stream.
     * <p>
     * This method determines what has changed between this
     * <code>DataReference</code> and the input one and writes out only the
     * changes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the <code>DataReference</code> to compare to.
     *			   <p>
     *			   If <code>null</code>, then this
     *			   <code>DataReference</code> is always written out in
     *			   full.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @see #read(com.rbnb.api.DataReference,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    final void write(DataReference otherI,
		     String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws java.io.IOException
    {
	boolean drChanged;

	if (otherI == null) {
	    drChanged = true;
	} else {
	    drChanged =
		(getIndivisibility() != otherI.getIndivisibility()) ||
		(getOffset() != otherI.getOffset()) ||
		(getWordOrder() != otherI.getWordOrder()) ||
		(getStride() != otherI.getStride()) ||
		(getDataType() != otherI.getDataType()) ||
		((getMIMEType() != null) ?
		 ((otherI.getMIMEType() == null) ?
		  true :
		  !getMIMEType().equals(otherI.getMIMEType())) :
		 (otherI.getMIMEType() != null));
	}

	if (drChanged) {
	    osI.writeParameter(parametersI,parameterI);
	    Serialize.writeOpenBracket(osI);
	    osI.writeParameter(PARAMETERS,PAR_IND);
	    osI.writeBoolean(getIndivisibility());
	    osI.writeParameter(PARAMETERS,PAR_OFF);
	    osI.writeInt(getOffset());
	    osI.writeParameter(PARAMETERS,PAR_ORD);
	    osI.writeElement(DataBlock.ORDERS,getWordOrder());
	    osI.writeParameter(PARAMETERS,PAR_STR);
	    osI.writeInt(getStride());
	    osI.writeParameter(PARAMETERS,PAR_TYP);
	    osI.writeElement(DataBlock.TYPES,getDataType());
	    if (getMIMEType() != null) {
		osI.writeParameter(PARAMETERS,PAR_MIM);
		osI.writeUTF(getMIMEType());
	    }
	    Serialize.writeCloseBracket(osI);
	}
    }
}
