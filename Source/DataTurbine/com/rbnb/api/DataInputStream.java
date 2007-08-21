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
 * RBNB data input stream.
 * <p>
 * This class is used to read the data payloads for <code>DataBlocks</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataBlock
 * @see com.rbnb.api.DataOutputStream
 * @since V2.0
 * @version 11/13/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/08/2001  INB	Created.
 *
 *
 */
class DataInputStream
    extends java.io.InputStream
    implements java.io.DataInput
{
    /**
     * byte array for binary reads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/02/2001
     */
    private byte[] bytes = new byte[8];

    /**
     * number of bytes read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/13/2002
     */
    private long bytesRead = 0;

    /**
     * byte array for skipping bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/12/2001
     */
    private byte[] skips = null;

    /**
     * input stream to do the actual reads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/16/2001
     */
    private java.io.InputStream is = null;

    /**
     * the real underlying input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/16/2001
     */
    private java.io.InputStream ris = null;

    /**
     * Class constructor to build a <code>DataInputStream</code> for another
     * stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the input stream.
     * @since V2.0
     * @version 01/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public DataInputStream(java.io.InputStream isI) {
	super();
	ris =
	    is = isI;
    }

    /**
     * Class constructor to build a <code>DataInputStream</code> with a
     * particular buffer size for another stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI    the input stream.
     * @param sizeI  the buffer size.
     * @since V2.0
     * @version 01/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public DataInputStream(java.io.InputStream isI,int sizeI) {
	super();
	ris = isI;
	if (sizeI == 0) {
	    is = isI;
	} else {
	    is = new java.io.BufferedInputStream(isI,sizeI);
	}
    }

    /**
     * Closes this stream.
     * <p>
     * This method leaves the underlying stream open.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    public final void close() {
	is =
	    ris = null;
    }

    /**
     * Gets the amount of data available.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the number of bytes waiting to be read.
     * @exception java.io.IOException
     *		  thrown if there is a problem getting the amount of available
     *		  data.
     * @since V2.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/16/2001  INB	Created.
     *
     */
    public final int available()
	throws java.io.IOException
    {
	return (is.available());
    }

    /**
     * Gets the number of bytes read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes read.
     * @see #setRead(long)
     * @since V2.0
     * @version 11/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/13/2002  INB	Created.
     *
     */
    final long getRead() {
	return (bytesRead);
    }

    /**
     * Reads the next byte of data from this <code>InputStream</code>.
     * <p>
     * The value byte is returned as an int between 0 and 255. If no byte is
     * available because the end of the stream has been reached, the value
     * returned is -1. The method blocks until a byte is read, the end of the
     * stream is reached, or an exception is thrown.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the byte read.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the byte.
     * @since V1.0
     * @version 11/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/16/2001  INB	Created.
     *
     */
    public final int read()
	throws java.io.IOException
    {
	int valueR = is.read();

	setRead(getRead() + 1);
	return (valueR);
    }

    /**
     * Reads an array of bytes from the stream starting at the specified offset
     * and going for the specified length.
     * <p>
     * Unfortunately, the standard read method can return fewer bytes than we
     * except to see. This method attempts to ensure that we get everything
     * we're looking for.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bytesO   the output data array.
     * @param offsetI  the starting offset into the array.
     * @param lengthI  the length to read.
     * @return the number of bytes actually read.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the I/O.
     * @since V2.0
     * @version 11/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    public int read(byte[] bytesO,int offsetI,int lengthI)
	throws java.io.IOException
    {
	int readR = 0,
	    bRead = 0;

	for (int offset = offsetI,
		 remainder = lengthI;
	     remainder > 0;
	     offset += bRead,
		 remainder -= bRead,
		 readR += bRead) {
	    if ((bRead = is.read(bytesO,offset,remainder)) > 0) {
		lengthI += bRead;
		setRead(getRead() + bRead);
	    } else if (bRead < 0) {
		break;
	    }
	}

	return (readR);
    }

    /**
     * Reads a boolean from the <code>InputStream</code>.
     * <p>
     * This method uses the <code>readElement()</code> method and the constant
     * array <code>OutputStream.BOOLEANS</code> to read in the boolean value.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the boolean.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the boolean.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final boolean readBoolean()
	throws java.io.IOException
    {
	return (readElement(OutputStream.BOOLEANS) == 1);
    }

    /**
     * Reads a byte from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the byte.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the byte.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final byte readByte()
	throws java.io.IOException
    {
	int value = read();

	if (value == -1) {
	    throw new java.io.EOFException();
	}
		
	return ((value <= Byte.MAX_VALUE) ?
		(byte) value :
		(byte) (value - 256));
    }

    /**
     * Reads a character from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the character.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the character.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final char readChar()
	throws java.io.IOException
    {
	if (read(bytes,0,2) < 2) {
	    throw new java.io.EOFException();
	}

	return ((char) ((bytes[0] << 8) | (bytes[1] & 0xff)));
    }

    /**
     * Reads a double from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the double.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the double.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final double readDouble()
	throws java.io.IOException
    {
	return (Double.longBitsToDouble(readLong()));
    }

    /**
     * Reads in an array element from the <code>InputStream</code>.
     * <p>
     * The element index is read. Depending on the number of entries in the
     * array, the element is read as:
     * <p><ul>
     * <li>a byte if the number of elements <=
     *	   <code>Byte.MAX_VALUE</code>,</li>
     * <li>a short if the number of elements <=
     *	   <code>Short.MAX_VALUE</code>, or</li>
     * <li>an int if the number of elements <= <code>Int.MAX_VALUE</code>.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param arrayI  the array.
     * @return the array index read.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the element.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input object is not an array.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    public final int readElement(Object arrayI)
	throws java.io.IOException
    {
	if (!arrayI.getClass().isArray()) {
	    throw new java.lang.IllegalArgumentException
		(arrayI + " is not an array.");
	}

	int elements = java.lang.reflect.Array.getLength(arrayI),
	    indexR = -1;

	if (elements <= Byte.MAX_VALUE) {
	    indexR = read();

	} else if (elements <= Short.MAX_VALUE) {
	    indexR = readShort();

	} else {
	    indexR = readInt();
	}

	if ((indexR < 0) || (indexR >= elements)) {
	    throw new java.io.IOException
		("Illegal index " + indexR + " of " + elements + ".");
	}

	return (indexR);
    }

    /**
     * Reads the entire input array from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI    the output byte array.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the bytes.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void readFully(byte[] bI)
	throws java.io.IOException
    {
	readFully(bI,0,bI.length);
    }

    /**
     * Reads <code>lenI</code> bytes from the <code>InputStream</code> into
     * <code>bI</code> at offset <code>offI</code>.
     * <p>
     * This method blocks until either the specified number of bytes are read
     * or until the end of the stream is reached.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI    the output byte array.
     * @param offI  the starting offset.
     * @param lenI  the number of bytes to read.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the bytes.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void readFully(byte[] bI,int offI,int lenI)
	throws java.io.IOException
    {
	if (read(bI,offI,lenI) < lenI) {
	    throw new java.io.EOFException();
	}
    }

    /**
     * Reads a float from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the float.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the float.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final float readFloat()
	throws java.io.IOException
    {
	return (Float.intBitsToFloat(readInt()));
    }

    /**
     * Reads an int from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the int.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the int.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final int readInt()
	throws java.io.IOException
    {
	if (read(bytes,0,4) < 4) {
	    throw new java.io.EOFException();
	}

	return (((bytes[0] & 0xff) << 24) |
		((bytes[1] & 0xff) << 16) |
		((bytes[2] & 0xff) <<  8) |
		((bytes[3] & 0xff)));
    }

    /**
     * Reads a line of input from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the line read as a string.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the line.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final String readLine()
	throws java.io.IOException
    {
	throw new java.io.IOException("readLine() is not supported.");
    }

    /**
     * Reads a long from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the long.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the long.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final long readLong()
	throws java.io.IOException
    {
	if (read(bytes) < 8) {
	    throw new java.io.EOFException();
	}

	return (((long) (bytes[0] & 0xff) << 56) |
		((long) (bytes[1] & 0xff) << 48) |
		((long) (bytes[2] & 0xff) << 40) |
		((long) (bytes[3] & 0xff) << 32) |
		((long) (bytes[4] & 0xff) << 24) |
		((long) (bytes[5] & 0xff) << 16) |
		((long) (bytes[6] & 0xff) <<  8) |
		((long) (bytes[7] & 0xff)      ));
    }

    /**
     * Reads a short from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the short.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the short.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final short readShort()
	throws java.io.IOException
    {
	if (read(bytes,0,2) < 2) {
	    throw new java.io.EOFException();
	}

	return ((short) (((short) (bytes[0] & 0xff) << 8) |
			 ((short) (bytes[1] & 0xff)     )));
    }

    /**
     * Reads an unsigned byte from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the unsigned byte as an int.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the unsigned byte.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final int readUnsignedByte()
	throws java.io.IOException
    {
	if (read(bytes,0,1) < 1) {
	    throw new java.io.EOFException();
	}

	return ((bytes[0] >= 0) ? (int) bytes[0] : ((int) bytes[0] + 256));
    }

    /**
     * Reads an unsigned short from the <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the unsigned short as an int.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the unsigned short.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final int readUnsignedShort()
	throws java.io.IOException
    {
	return (((readUnsignedByte() & 0xff) << 8) |
		((readUnsignedByte() & 0xff)));
    }

    /**
     * Reads a string from the <code>InputStream</code> as a UTF string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the string.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final String readUTF()
	throws java.io.IOException
    {
	// Read the length of the UTF string in bytes from the input stream as
	// an unsigned short.
	int nBytes = readUnsignedShort();

	// Create an array to hold those two bytes and the actual bytes of the
	// UTF string.
	byte[] array = new byte[2 + nBytes];

	// Copy the length into the byte array.
	array[0] = (byte) ((nBytes >> 8) & 0xff);
	array[1] = (byte) ((nBytes) & 0xff);

	// Read the bytes of the UTF string.
	if (read(array,2,nBytes) < nBytes) {
	    throw new java.io.EOFException();
	}

	// Convert to a string by using
	// <code>java.io.ByteArrayInputStream</code> and
	// <code>java.io.DataInputStream</code>.
	java.io.ByteArrayInputStream bais = new
	    java.io.ByteArrayInputStream(array);
	java.io.DataInputStream dis = new java.io.DataInputStream(bais);
	String stringR = dis.readUTF();
	dis.close();

	return (stringR);
    }

    /**
     * Seeks to a particular location in the file.
     * <p>
     * The underlying <code>InputStream</code> for this
     * <code>InputStream</code> must be a <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the position to go to.
     * @exception java.lang.IllegalStateException
     *		  thrown if the underlying stream is not a
     *		  <code>RandomAccessInputStream</code>.
     * @since V2.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final void seek(long offsetI) {
	if (!(ris instanceof RandomAccessInputStream)) {
	    throw new java.lang.IllegalStateException
		("The underlying stream does not support file pointers.");
	}

	// Reposition the file pointer.
	((RandomAccessInputStream) ris).setFilePointer(offsetI);
    }

    /**
     * Sets the number of bytes read.
     * <p>
     *
     * @author Ian Brown
     *
     * @param readI  the new number of bytes read.
     * @see #getRead()
     * @since V2.0
     * @version 11/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/13/2002  INB	Created.
     *
     */
    private final void setRead(long readI) {
	bytesRead = readI;
    }

    /**
     * Skips over a specified number of bytes.
     * <p>
     *
     *
     * @author Ian Brown
     *
     * @param nI the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @exception java.io.IOException
     *		  thrown if there is a problem skipping the bytes.
     * @since V1.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final int skipBytes(int nI)
	throws java.io.IOException
    {
	if ((skips == null) || (skips.length < nI)) {
	    skips = new byte[nI];
	}

	return (read(skips,0,nI));
    }
}
