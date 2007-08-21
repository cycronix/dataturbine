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

import java.util.Vector;

/**
 * RBNB serialization input stream.
 * <p>
 * This class provides deserialization of RBNB objects using either binary or
 * text input modes.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.OutputStream
 * @since V1.0
 * @version 07/21/2004
 */

/*
 * Copyright 1997, 1998, 1999, 2000, 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/21/2004  INB	Check for null is pointer in reset.
 * 11/17/2003  INB	Propagate interrupts.
 * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
 * 03/01/1997  INB	Created.
 *
 */
class InputStream
    extends java.io.InputStream
    implements java.io.DataInput
{
    /**
     * binary mode?
     * <p>
     * If this field is set, the deserialization is done using binary values
     * for all numeric types and indexes for array and parameter values.
     * <p>
     * If this field is clear, the deserialization is done using text values
     * for all numeric types, array, and parameter values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 02/02/2001
     */
    private boolean binary = false;

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
     * byte array for handling <code>mark()/reset()</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/05/2001
     */
    private byte[] marked = null;

    /**
     * byte array for skipping bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/02/2001
     */
    private byte[] skips = null;

    /**
     * input stream to do the actual reads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/05/2001
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
     * the current location within the mark buffer.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/05/2001
     */
    private int markIndex = 0;

    /**
     * bytes since mark was placed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/05/2001
     */
    private int markLength = 0;

    /**
     * the mark limit in bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/05/2001
     */
    private int markLimit = 0;

    /**
     * number of bytes read at mark time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/02/2001
     */
    private long bytesMark = 0;

    /**
     * number of bytes read from the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 02/02/2001
     */
    private long bytesRead = 0;

    /**
     * informational string for messages that aren't exactly exceptions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0

     * @version 03/05/2001
     */
    private String infoMessage = null;

    /**
     * Class constructor to build an <code>InputStream</code> from a
     * <code>java.io.InputStream</code>, a binary mode flag, and a buffer
     * size.
     * <p>
     * If <code>sizeI</code> is equal to zero, the input stream is used
     * directly. Otherwise, a <code>java.io.BufferedInputStream</code> is
     * put in place.
     * <p>
     *
     * @author Ian Brown
     *
     * @param iStreamI  the <code>java.io.InputStream</code> to read from.
     * @param binaryI   binary mode?
     * @param sizeI	the size of the buffer.
     * @exception java.io.IOException
     *		  thrown if there is a problem connecting to the input stream.
     * @since V1.0
     * @version 03/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    InputStream(java.io.InputStream iStreamI,boolean binaryI,int sizeI)
	throws java.io.IOException
    {
	ris = iStreamI;
	if (sizeI == 0) {
	    is = iStreamI;
	} else {
	    is = new java.io.BufferedInputStream(iStreamI,sizeI);
	}
	setBinary(binaryI);
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
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    public final int available()
	throws java.io.IOException
    {
	int availableR = is.available();

	if (markIndex < markLength) {
	    availableR += markLength - markIndex;
	}

	return (availableR);
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
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    public final void close() {
	is = null;

	markIndex =
	    markLength =
	    markLimit = 0;
	marked = null;
    }

    /**
     * Gets the binary mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return binary mode?
     * @see #setBinary(boolean)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    final boolean getBinary() {
	return (binary);
    }

    /**
     * Gets the information message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the informational message.
     * @see #setInfoMessage(String)
     * @since V2.0
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    final String getInfoMessage() {
	return (infoMessage);
    }

    /**
     * Gets the number of bytes read from the stream at the time of the mark.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes read.
     * @see #setMark(long)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    final long getMark() {
	return (bytesMark);
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
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    final long getRead() {
	return (bytesRead);
    }

    /**
     * Marks the current position in this input stream.
     * <p>
     * A subsequent call to <code>reset()</code> repositions this stream at the
     * last marked position so that subsequent reads re-read the same bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param readlimitI  the maximum number of bytes that can be read before
     *			  mark position gets invalidated.
     * @see #reset()
     * @since V2.0
     * @version 03/19/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    public final void mark(int readlimitI) {
	if (is.markSupported()) {
	    is.mark(readlimitI);

	} else if (markIndex >= markLength) {
	    markLength = 0;
	    markIndex =
		markLimit = readlimitI;
	    if ((marked == null) || (marked.length < readlimitI)) {
		marked = new byte[readlimitI];
	    }

	} else if ((markIndex == 0) && (markLimit >= readlimitI)) {
	    markLimit = readlimitI;
	    
	} else {
	    byte[] old = marked;
	    markLength = markLength - markIndex;
	    markLimit = readlimitI;
	    markIndex = 0;
	    marked = new byte[readlimitI];
	    System.arraycopy(old,markIndex,marked,0,markLength);
	}

	setMark(getRead());
    }

    /**
     * Is mark supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return  mark is supported.
     * @see #mark(int)
     * @see #reset()
     * @since V2.0
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    public final boolean markSupported() {
	return (true);
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
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final int read()
	throws java.io.IOException
    {
	int valueR;

	if (markIndex < markLength) {
	    valueR = marked[markIndex++];
	} else {
	    valueR = is.read();
	    if (markLength < markLimit) {
		marked[markLength++] = (byte) valueR;
		markIndex = markLimit;
	    } else {
		markIndex =
		    markLimit =
		    markLength = 0;
		marked = null;
	    }
	    if (valueR >= 0) {
		setRead(getRead() + 1);
	    }
	}

	return (valueR);
    }

    /**
     * Reads up to <code>lenI</code> bytes from the <code>InputStream</code>
     * into <code>bI</code> at offset <code>offI</code>.
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
     * @return the number of bytes read or -1 if the end of the stream is
     *	       reached before any bytes are read.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the bytes.
     * @since V1.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final int read(byte[] bI,int offI,int lenI)
	throws java.io.IOException
    {
	int nRead = 0,
	    fromMark = 0;

	if (markIndex < markLength) {
	    nRead = Math.min(lenI,markLength - markIndex);
	    System.arraycopy(marked,markIndex,bI,offI,nRead);
	    markIndex += nRead;
	    fromMark = nRead;
	}

	int bRead;
	for (bRead = is.read(bI,offI + nRead,lenI - nRead);
	     bRead > 0;
	     bRead = is.read(bI,offI + nRead,lenI - nRead)) {
	    if (markLength + bRead < markLimit) {
		System.arraycopy(bI,offI + nRead,marked,markLength,bRead);
		markLength += bRead;
		markIndex = markLimit;
	    } else {
		markLimit =
		    markLength =
		    markIndex = 0;
		marked = null;
	    }
	    nRead += bRead;

	    if (nRead == lenI) {
		break;
	    }
	}

	if (nRead == 0) {
	    nRead = -1;
	} else if (nRead > fromMark) {
	    setRead(getRead() + nRead - fromMark);
	}

	return (nRead);
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
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
     * Repositions to the position at the time <code>mark()</code> was last
     * called on this <code>InputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.IOException
     *		  thrown if the stream has not been marked or if the mark has
     *		  been invalidated.
     * @see #mark(int)
     * @since V2.0
     * @version 07/21/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/21/2004  INB	Check for null is pointer.
     * 02/02/2001  INB	Created.
     *
     */
    public final void reset()
	throws java.io.IOException
    {
	if ((is != null) && is.markSupported()) {
	    is.reset();
	} else {
	    markIndex = 0;
	}
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
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
     * In binary mode, the method reads a single byte.
     * <p>
     * In text mode, the method reads a string and converts it to a byte.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the byte.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the byte.
     * @since V1.0
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final byte readByte()
	throws java.io.IOException
    {
	if (binary) {
	    int value = read();

	    if (value == -1) {
		throw new java.io.EOFException();
	    }
		
	    return ((value <= Byte.MAX_VALUE) ?
		    (byte) value :
		    (byte) (value - 256));

	} else {
	    return (Byte.valueOf(readString()).byteValue());
	}
    }

    /**
     * Reads a character from the <code>InputStream</code>.
     * <p>
     * In binary mode, the character is read as two bytes.
     * <p>
     * In text mode, the method reads a single byte and converts it to a
     * character.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the character.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the character.
     * @since V1.0
     * @version 09/26/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final char readChar()
	throws java.io.IOException
    {
	if (getBinary()) {
	    if (read(bytes,0,2) < 2) {
		throw new java.io.EOFException();
	    }

	    return ((char) ((bytes[0] << 8) | (bytes[1] & 0xff)));

	} else {
	    int value = read();

	    if (value == -1) {
		throw new java.io.EOFException();
	    }

	    return ((char) value);
	}
    }

    /**
     * Reads a double from the <code>InputStream</code>.
     * <p>
     * In binary mode, the method reads a long and converts that to a double.
     * <p>
     * In text mode, the method reads a string and converts that to a double.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the double.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the double.
     * @since V1.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final double readDouble()
	throws java.io.IOException
    {
	if (getBinary()) {
	    return (Double.longBitsToDouble(readLong()));
	} else {
	    return (Double.valueOf(readString()).doubleValue());
	}
    }

    /**
     * Reads in an array element from the <code>InputStream</code>.
     * <p>
     * In binary mode, the element index is read. Depending on the number of
     * entries in the array, the element is read as:
     * <p><ul>
     * <li>a byte if the number of elements <=
     *	   <code>Byte.MAX_VALUE</code>,</li>
     * <li>a short if the number of elements <=
     *	   <code>Short.MAX_VALUE</code>, or</li>
     * <li>an int if the number of elements <= <code>Int.MAX_VALUE</code>.</li>
     * </ul><p>
     * In text mode, the element is read using the appropriate
     * <code>writeXXX</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @param arrayI  the array.
     * @return the array index read or -1 if no element could be read. If -1
     *	       is set, call <code>getInfoMessage()</code> for additional
     *	       information.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the element.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input object is not an array.
     * @since V1.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Propagate interrupts.
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
	setInfoMessage(null);
	if (getBinary()) {
	    // In binary mode, read in the index using the appropriate method
	    // for the number of elements in the array.

	    if (elements <= Byte.MAX_VALUE) {
		indexR = read();

		if (indexR == -1) {
		    throw new java.io.EOFException();
		}

	    } else if (elements <= Short.MAX_VALUE) {
		indexR = readShort();

	    } else {
		indexR = readInt();
	    }
	    if (Thread.currentThread().interrupted()) {
		throw new java.io.InterruptedIOException
		    ("Failed to read parameter on interrupt.");
	    }

	} else {
	    // In text mode, read in the element using the appropriate method
	    // based on the type of array.

	    if (arrayI instanceof boolean[]) {
		boolean element = readBoolean();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		boolean[] array = (boolean[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof byte[]) {
		byte element = readByte();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		byte[] array = (byte[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof double[]) {
		double element = readDouble();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		double[] array = (double[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof float[]) {
		float element = readFloat();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		float[] array = (float[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof int[]) {
		int element = readInt();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		int[] array = (int[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof long[]) {
		long element = readLong();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		long[] array = (long[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof short[]) {
		short element = readShort();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		short[] array = (short[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && (array[indexR] != element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}

	    } else if (arrayI instanceof String[]) {
		String element = readString();
		if (Thread.currentThread().interrupted()) {
		    throw new java.io.InterruptedIOException
			("Failed to read parameter on interrupt.");
		}
		String[] array = (String[]) arrayI;

		for (indexR = 0;
		     (indexR < array.length) && !array[indexR].equals(element);
		     ++indexR) {
		}
		if (indexR >= array.length) {
		    setInfoMessage("Element read was " + element);
		    return (-1);
		}
	    }
	}

	if (indexR >= elements) {
	    // If the index is not legal for this list return -1.
	    if (getInfoMessage() == null) {
		String additional = "";

		if ((indexR >= Character.MIN_VALUE) &&
		    (indexR <= Character.MAX_VALUE)) {
		    if (((char) indexR) == '{') {
			additional = "\nThis is an open bracket.";
		    } else if (((char) indexR) == '}') {
			additional = "\nThis is a close bracket.";
		    }
		}
		setInfoMessage("Index of element was: " +
			       indexR +
			       " in " +
			       (getBinary() ? "binary" : "text") +
			       " mode." + additional);
	    }

	    indexR = -1;
	}

	return (indexR);
    }

    /**
     * Reads a float from the <code>InputStream</code>.
     * <p>
     * In binary mode, this method reads an int and converts that to a float.
     * <p>
     * In text mode, this method reads a string and converts that to a float.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the float.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the float.
     * @since V1.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final float readFloat()
	throws java.io.IOException
    {
	if (getBinary()) {
	    return (Float.intBitsToFloat(readInt()));
	} else {
	    return (Float.valueOf(readString()).floatValue());
	}
    }

    /**
     * Reads an int from the <code>InputStream</code>.
     * <p>
     * In binary mode, this method reads the int as four bytes.
     * <p>
     * In text mode, this method reads a string and converts that to an int.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the int.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the int.
     * @since V1.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final int readInt()
	throws java.io.IOException
    {
	if (getBinary()) {
	    if (read(bytes,0,4) < 4) {
		throw new java.io.EOFException();
	    }

	    return (((bytes[0] & 0xff) << 24) |
		    ((bytes[1] & 0xff) << 16) |
		    ((bytes[2] & 0xff) <<  8) |
		    ((bytes[3] & 0xff)));

	} else {
	    return (Integer.valueOf(readString()).intValue());
	}
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
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
     * In binary mode, this method reads eight bytes and converts them to a
     * long.
     * <p>
     * In text mode, this method reads a string and converts that to a long.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the long.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the long.
     * @since V1.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final long readLong()
	throws java.io.IOException
    {
	if (getBinary()) {
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
	} else {
	    return (Long.valueOf(readString()).longValue());
	}
    }

    /**
     * Read a parameter from the <code>InputStream</code>.
     * <p>
     * This method uses <code>readElement()</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the list of parameters.
     * @return the index of the parameter read or -1.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the ???.
     * @since V1.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final int readParameter(String[] parametersI)
	throws java.io.IOException
    {
	int parameterR = readElement(parametersI);

	return (parameterR);
    }

    /**
     * Reads a short from the <code>InputStream</code>.
     * <p>
     * In binary mode, this method reads two bytes and converts them to a
     * short.
     * <p>
     * In text mode, this method reads a string and converts that to a short.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the short.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the short.
     * @since V1.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final short readShort()
	throws java.io.IOException
    {
	if (getBinary()) {
	    if (read(bytes,0,2) < 2) {
		throw new java.io.EOFException();
	    }

	    return ((short) (((short) (bytes[0] & 0xff) << 8) |
			     ((short) (bytes[1] & 0xff)     )));
	} else {
	    return (Short.valueOf(readString()).shortValue());
	}
    }

    /**
     * Reads a string from the <code>InputStream</code>.
     * <p>
     * In binary mode, this method uses <code>readUTF()</code>.
     * <p>
     * In text mode, this method reads bytes until it sees either a <CR><LF> or
     * a space. In the former case, the method returns <CR><LF>, in the latter,
     * the method returns everything up to the space. It skips leading
     * whitespace.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the string.
     * @since V1.0
     * @version 09/26/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final String readString()
	throws java.io.IOException
    {
	String stringR = null;

	if (getBinary()) {
	    stringR = readUTF();

	} else {
	    // Create the result by making a byte array and then convert that
	    // to a string.
	    java.io.ByteArrayOutputStream baos = new
		java.io.ByteArrayOutputStream();
	    boolean cr = false;
	    int value;

	    while ((value = read()) >= 0) {
		if (value > 127) {
		    value -= 256;
		}
		char cValue = (char) value;

		if (!Character.isWhitespace(cValue)) {
		    baos.write(value);
		    break;
		} else if (cValue == 0x000D) {
		    cr = true;
		} else if (cr && (cValue == 0x000A)) {
		    baos.write(0x000D);
		    baos.write(0x000A);
		    break;
		}
	    }
	    if (value == -1) {
		throw new java.io.EOFException();
	    }

	    while ((value = read()) >= 0) {
		int tValue = value;
		if (value > 127) {
		    tValue -= 256;
		}
		char cValue = (char) tValue;

		if (Character.isWhitespace(cValue)) {
		    break;
		}

		baos.write(value);
	    }
	    if (value == -1) {
		throw new java.io.EOFException();
	    }

	    stringR = new String(baos.toByteArray());
	    baos.close();
	}

	return (stringR);
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
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
	String stringR = null;
	if (nBytes == 0) {
	    stringR = "";
	} else {
	    // Convert to a string by using
	    // <code>java.io.ByteArrayInputStream</code> and
	    // <code>java.io.DataInputStream</code>.
	    readFully(array,2,nBytes);
	    java.io.ByteArrayInputStream bais = new
		java.io.ByteArrayInputStream(array);
	    java.io.DataInputStream dis = new java.io.DataInputStream(bais);
	    stringR = dis.readUTF();
	    dis.close();
	}

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

	((RandomAccessInputStream) is).setFilePointer(offsetI);
    }

    /**
     * Sets the binary mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param binaryI  binary mode?
     * @exception java.io.IOException
     *		  thrown if there is a problem resynchronizing with the other
     *		  side when the mode is switched to binary.
     * @see #getBinary()
     * @since V2.0
     * @version 03/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    final void setBinary(boolean binaryI)
	throws java.io.IOException
    {
	binary = binaryI;

	try {
	    if (binary &&
		!(ris instanceof RandomAccessInputStream) &&
		(!(ris instanceof java.io.FileInputStream) ||
		 !ris.getClass().isAssignableFrom
		 (Class.forName("java.io.FileInputStream")))) {
		// On a switch to binary mode, the other end is expected to
		// send a null (0) character as an indication of where the
		// switch should occur. Skip over characters until we see the
		// null.
		int aByte = -1;

		while (((aByte = readByte()) >= 0) && (aByte != 0)) {
		}
	    }
	} catch (java.lang.ClassNotFoundException e) {
	}
    }

    /**
     * Sets the informational message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI  the new message.
     * @since V2.0
     * @version 03/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    private final void setInfoMessage(String messageI) {
	infoMessage = messageI;
    }

    /**
     * Sets the number of bytes read at the time of the mark.
     * <p>
     *
     * @author Ian Brown
     *
     * @param markI  the new number of bytes read at the mark.
     * @see #getMark()
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INb	Created.
     *
     */
    private final void setMark(long markI) {
	bytesMark = markI;
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
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
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
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
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
