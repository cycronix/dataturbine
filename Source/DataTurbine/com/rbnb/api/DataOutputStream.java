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
 * RBNB data output stream.
 * <p>
 * This class is used to write the data payloads for <code>DataBlocks</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataBlock
 * @see com.rbnb.api.DataInputStream
 * @since V2.0
 * @version 10/22/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/22/2003  INB	Made <code>setWritten</code> externally accessible.
 * 01/08/2001  INB	Created.
 *
 */
class DataOutputStream
    extends java.io.OutputStream
    implements java.io.DataOutput
{
    /**
     * Byte buffer for handling conversions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/19/2001
     */
    private byte[] buffer = null;

    /**
     * arrays to buffer multibyte objects.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/27/2001
     */
    private byte[][] mbArray = { new byte[8], new byte[8] };

    /**
     * buffer array to use.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/27/2001
     */
    private int mbIdx = 0;

    /**
     * output stream to write to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/16/2001
     */
    private java.io.OutputStream os = null;

    /**
     * the real underlying output sream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/16/2001
     */
    private java.io.OutputStream ros = null;

    /**
     * number of bytes written to the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/13/2002
     */
    private long written = 0;

    /**
     * Class constructor to build a <code>DataOutputStream</code> for another
     * stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI  the output stream.
     * @since V2.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public DataOutputStream(java.io.OutputStream osI) {
	super();
	ros =
	    os = osI;
    }

    /**
     * Class constructor to build a <code>DataOutputStream</code> with a
     * particular buffer size for another stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI    the output stream.
     * @param sizeI  the buffer size.
     * @since V2.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public DataOutputStream(java.io.OutputStream osI,int sizeI) {
	super();
	ros = osI;
	if (sizeI == 0) {
	    os = osI;
	} else {
	    os = new java.io.BufferedOutputStream(osI,sizeI);
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
	os =
	    ros = null;
    }

    /**
     * Flushes the <code>OutputStream</code>.
     * <p>v
     *
     * @author Ian Brown
     *
     * @exception java.io.Exception
     *		  thrown if there is a problem flushing the stream.
     * @since V1.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/16/2001 INB	Created.
     *
     */
    public final void flush()
	throws java.io.IOException
    {
	os.flush();
	if (ros != os) {
	    ros.flush();
	}
    }

    /**
     * Gets the file pointer for this <code>DataOutputStream</code>.
     * <p>
     * The underlying <code>DataOutputStream</code> must be a
     * <code>RandomAccessOutputStream</code>. If so, we return its file
     * pointer.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the file pointer.
     * @exception java.lang.IllegalStateException
     *		  thrown if the underlying stream is not a
     *		  <code>RandomAccessOutputStream</code>.
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
    final long getFilePointer() {
	if (!(ros instanceof RandomAccessOutputStream)) {
	    throw new java.lang.IllegalStateException
		("The underlying stream does not support file pointers.");
	}

	return (((RandomAccessOutputStream) ros).getFilePointer());
    }

    /**
     * Gets a write buffer of at least the specified size.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sizeI  the number of bytes for the write buffer.
     * @return the write buffer.
     * @since V2.0
     * @version 01/19/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final byte[] getWriteBuffer(int sizeI) {
	if ((buffer == null) || (buffer.length < sizeI)) {
	    buffer = new byte[sizeI];
	}

	return (buffer);
    }

    /**
     * Gets the number of bytes written to the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes written.
     * @see #setWritten(long)
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
    final long getWritten() {
	return (written);
    }

    /**
     * Sets the number of bytes written to the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param writtenI the number of bytes written.
     * @see #getWritten()
     * @see #size()
     * @since V2.0
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	No longer private.
     * 11/13/2002  INB	Created.
     *
     */
    final void setWritten(long writtenI) {
	written = writtenI;
    }

    /**
     * Returns the number of bytes written to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes written.
     * @see #getWritten()
     * @see #setWritten(long)
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
    public final int size() {
	return ((int) getWritten());
    }

    /**
     * Writes out the specified elements of the input byte array to the
     * <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI    the byte array.
     * @param offI  the starting offset.
     * @param lenI  the number of bytes to write.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing out the array.
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
    public final void write(byte bI[],int offI,int lenI)
	throws java.io.IOException
    {
	os.write(bI,offI,lenI);
	setWritten(getWritten() + lenI);
    }

    /**
     * Writes out an array of booleans as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param booleansI  the array of booleans.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    private final void write(boolean[] booleansI)
	throws java.io.IOException
    {
	int len = booleansI.length;
	byte[] bytes = getWriteBuffer(len);

	for (int idx = 0; idx < len; ++idx) {
	    // Convert the booleans to bytes. False = 0. True = 1.
	    bytes[idx] = (byte) (booleansI[idx] ? 1 : 0);
	}

	// Write out the byte array.
	write(bytes,0,len);
    }

    /**
     * Writes out an array of doubles as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doublesI  the array of doubles.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    private final void write(double[] doublesI)
	throws java.io.IOException
    {
	int len = doublesI.length;
	byte[] bytes = getWriteBuffer(len*8);

	for (int idx = 0,
		 idx2 = 0;
	     idx < len;
	     ++idx,
		 idx2 += 8) {
	    // Convert the doubles to bytes.
	    long value = Double.doubleToLongBits(doublesI[idx]);
	    bytes[idx2    ] = (byte) (value >>> 56);
	    bytes[idx2 + 1] = (byte) (value >>> 48);
	    bytes[idx2 + 2] = (byte) (value >>> 40);
	    bytes[idx2 + 3] = (byte) (value >>> 32);
	    bytes[idx2 + 4] = (byte) (value >>> 24);
	    bytes[idx2 + 5] = (byte) (value >>> 16);
	    bytes[idx2 + 6] = (byte) (value >>>  8);
	    bytes[idx2 + 7] = (byte) (value       );
	}

	// Write out the byte array.
	write(bytes,0,len*8);
    }

    /**
     * Writes out an array of floats as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param floatsI  the array of floats.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    private final void write(float[] floatsI)
	throws java.io.IOException
    {
	int len = floatsI.length;
	byte[] bytes = getWriteBuffer(len*4);

	for (int idx = 0,
		 idx2 = 0;
	     idx < len;
	     ++idx,
		 idx2 += 4) {
	    // Convert the floats to bytes.
	    int value = Float.floatToIntBits(floatsI[idx]);
	    bytes[idx2    ] = (byte) (value >>> 24);
	    bytes[idx2 + 1] = (byte) (value >>> 16);
	    bytes[idx2 + 2] = (byte) (value >>>  8);
	    bytes[idx2 + 3] = (byte) (value       );
	}

	// Write out the byte array.
	write(bytes,0,len*4);
    }

    /**
     * Write a single byte to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI  the byte as an int.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing out the byte.
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
    public final void write(int bI)
	throws java.io.IOException
    {
	os.write(bI);
	setWritten(getWritten() + 1);
    }

    /**
     * Writes out an array of ints as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param intsI  the array of ints.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    private final void write(int[] intsI)
	throws java.io.IOException
    {
	int len = intsI.length;
	byte[] bytes = getWriteBuffer(len*4);

	for (int idx = 0,
		 idx2 = 0;
	     idx < len;
	     ++idx,
		 idx2 += 4) {
	    // Convert the ints to bytes.
	    int value = intsI[idx];
	    bytes[idx2    ] = (byte) (value >>> 24);
	    bytes[idx2 + 1] = (byte) (value >>> 16);
	    bytes[idx2 + 2] = (byte) (value >>>  8);
	    bytes[idx2 + 3] = (byte) (value       );
	}

	// Write out the byte array.
	write(bytes,0,len*4);
    }

    /**
     * Writes out an array of longs as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param longsI  the array of longs.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    private final void write(long[] longsI)
	throws java.io.IOException
    {
	int len = longsI.length;
	byte[] bytes = getWriteBuffer(len*8);

	for (int idx = 0,
		 idx2 = 0;
	     idx < len;
	     ++idx,
		 idx2 += 8) {
	    // Convert the longs to bytes.
	    long value = longsI[idx];
	    bytes[idx2    ] = (byte) (value >>> 56);
	    bytes[idx2 + 1] = (byte) (value >>> 48);
	    bytes[idx2 + 2] = (byte) (value >>> 40);
	    bytes[idx2 + 3] = (byte) (value >>> 32);
	    bytes[idx2 + 4] = (byte) (value >>> 24);
	    bytes[idx2 + 5] = (byte) (value >>> 16);
	    bytes[idx2 + 6] = (byte) (value >>>  8);
	    bytes[idx2 + 7] = (byte) (value       );
	}

	// Write out the byte array.
	write(bytes,0,len*8);
    }

    /**
     * Writes out an array of shorts as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortsI  the array of shorts.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    private final void write(short[] shortsI)
	throws java.io.IOException
    {
	int len = shortsI.length;
	byte[] bytes = getWriteBuffer(len*2);

	for (int idx = 0,
		 idx2 = 0;
	     idx < len;
	     ++idx,
		 idx2 += 2) {
	    // Convert the shorts to bytes.
	    short value = shortsI[idx];
	    bytes[idx2    ] = (byte) (value >>> 8);
	    bytes[idx2 + 1] = (byte) (value      );
	}

	// Write out the byte array.
	write(bytes,0,len*2);
    }

    /**
     * Write out a string as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stringI the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 01/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    private final void write(String stringI)
	throws java.io.IOException
    {
	write(stringI.getBytes());
    }

    /**
     * Write out an array of strings as a series of bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stringsI the array of strings.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    private final void write(String[] stringsI)
	throws java.io.IOException
    {
	for (int idx = 0, eIdx = stringsI.length;
	     idx < eIdx;
	     ++idx) {
	    // Write out each of the strings in turn.

	    write(stringsI[idx]);
	}
    }

    /**
     * Writes out the input <code>java.util.Vector</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vectorI  the <code>java.util.Vector</code>.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the output stream.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    public final void write(java.util.Vector vectorI)
	throws java.io.IOException
    {
	for (int idx = 0, eIdx = vectorI.size(); idx < eIdx; ++idx) {
	    // Write out each of the elements of the vector.
	    Object object = vectorI.elementAt(idx);

	    if (object instanceof boolean[]) {
		write((boolean[]) object);
	    } else if (object instanceof byte[]) {
		write((byte[]) object);
	    } else if (object instanceof double[]) {
		write((double[]) object);
	    } else if (object instanceof int[]) {
		write((int[]) object);
	    } else if (object instanceof long[]) {
		write((long[]) object);
	    } else if (object instanceof float[]) {
		write((float[]) object);
	    } else if (object instanceof short[]) {
		write((short[]) object);
	    } else if (object instanceof String) {
		write((String) object);
	    } else if (object instanceof String[]) {
		write((String[]) object);
	    }
    	}
    }

    /**
     * Writes a boolean value to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the boolean value.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing out the boolean value.
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
    public final void writeBoolean(boolean vI)
	throws java.io.IOException
    {
	writeElement(OutputStream.BOOLEANS,vI ? 1 : 0);
    }

    /**
     * Writes a single byte value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the byte value.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the byte value.
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
    public final void writeByte(int vI)
	throws java.io.IOException
    {
	write(vI);
    }

    /**
     * Writes a string as a series of bytes, discarding the high eight bits of
     * the characters of the string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the string.
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
    public final void writeBytes(String vI)
	throws java.io.IOException
    {
	write(vI.getBytes());
    }

    /**
     * Writes out a character to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the character.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the character.
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
    public final void writeChar(int vI)
	throws java.io.IOException
    {
	write((vI >> 8) & 0xff);
	write((vI     ) & 0xff);
    }

    /**
     * Writes a string to the <code>OutputStream</code> as an array of
     * characters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the characters.
     * @since V1.0
     * @version 08/14/2001
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
    public final void writeChars(String vI)
	throws java.io.IOException
    {
	for (int idx = 0, eIdx = vI.length(); idx < eIdx; ++idx) {
	    writeChar((int) vI.charAt(idx));
	}
    }

    /**
     * Writes a double to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the double.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the double.
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
    public final void writeDouble(double vI)
	throws java.io.IOException
    {
	writeLong(Double.doubleToLongBits(vI));
    }

    /**
     * Writes out an array element to the <code>OutputStream</code>.
     * <p>
     * The element index is written. Depending on the number of entries in the
     * array, the element will be written as:
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
     * @param indexI  the array index to write.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing out the element.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input object is not an array.
     * @since V2.0
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
    public final void writeElement(Object arrayI,int indexI)
	throws java.io.IOException
    {
	if (!arrayI.getClass().isArray()) {
	    throw new java.lang.IllegalArgumentException
		(arrayI + " is not an array.");
	}

	int elements = java.lang.reflect.Array.getLength(arrayI);

	if (elements <= Byte.MAX_VALUE) {
	    write(indexI);
	} else if (elements <= Short.MAX_VALUE) {
	    writeShort(indexI);
	} else {
	    writeInt(indexI);
	}
    }

    /**
     * Writes a float to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the float.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the float.
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
    public final void writeFloat(float vI)
	throws java.io.IOException
    {
	writeInt(Float.floatToIntBits(vI));
    }

    /**
     * Writes an int to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the int.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the int.
     * @since V1.0
     * @version 09/27/2001
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
    public final void writeInt(int vI)
	throws java.io.IOException
    {
	/*
	write((vI >> 24) & 0xff);
	write((vI >> 16) & 0xff);
	write((vI >>  8) & 0xff);
	write((vI      ) & 0xff);
	*/
	int idx = mbIdx++;
	mbArray[idx][0] = ((byte) ((vI >> 24) & 0xff));
	mbArray[idx][1] = ((byte) ((vI >> 16) & 0xff));
	mbArray[idx][2] = ((byte) ((vI >>  8) & 0xff));
	mbArray[idx][3] = ((byte) ((vI      ) & 0xff));
	write(mbArray[idx],0,4);
	--mbIdx;
    }

    /**
     * Writes a long to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the long.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the long.
     * @since V1.0
     * @version 09/27/2001
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
    public final void writeLong(long vI)
	throws java.io.IOException
    {
	/*
	write((int) ((vI >> 56) & 0xff));
	write((int) ((vI >> 48) & 0xff));
	write((int) ((vI >> 40) & 0xff));
	write((int) ((vI >> 32) & 0xff));
	write((int) ((vI >> 24) & 0xff));
	write((int) ((vI >> 16) & 0xff));
	write((int) ((vI >>  8) & 0xff));
	write((int) ((vI      ) & 0xff));
	*/
	int idx = mbIdx++;
	mbArray[idx][0] = ((byte) ((vI >> 56) & 0xff));
	mbArray[idx][1] = ((byte) ((vI >> 48) & 0xff));
	mbArray[idx][2] = ((byte) ((vI >> 40) & 0xff));
	mbArray[idx][3] = ((byte) ((vI >> 32) & 0xff));
	mbArray[idx][4] = ((byte) ((vI >> 24) & 0xff));
	mbArray[idx][5] = ((byte) ((vI >> 16) & 0xff));
	mbArray[idx][6] = ((byte) ((vI >>  8) & 0xff));
	mbArray[idx][7] = ((byte) ((vI      ) & 0xff));
	write(mbArray[idx],0,8);
	--mbIdx;
    }

    /**
     * Writes a short to the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the short.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the short.
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
    public final void writeShort(int vI)
	throws java.io.IOException
    {
	write((vI >> 8) & 0xff);
	write((vI     ) & 0xff);
    }

    /**
     * Writes a string to the <code>OutputStream</code> as a UTF string.
     * <p>
     * The string is converted to UTF format (1, 2, or 3 bytes per
     * character). The length of the resulting byte array is written to the
     * <code>OutputStream</code> as an unsigned short value, followed by the
     * byte array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param strI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the string.
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
    public final void writeUTF(String strI)
	throws java.io.IOException
    {
	// Convert the string to a UTF string using
	// <code>java.io.DataOutputStream</code> and
	// <code>java.io.ByteArrayOutputStream</code>
	java.io.ByteArrayOutputStream baos =
	    new java.io.ByteArrayOutputStream();
	java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
	dos.writeUTF(strI);
	byte array[] = baos.toByteArray();
	dos.close();

	// Write the resulting array.
	write(array);
    }
}
