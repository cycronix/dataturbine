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
 * RBNB serialization output stream.
 * <p>
 * This class provides serialization of RBNB objects using either binary or
 * text output modes.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.InputStream
 * @since V2.0
 * @version 10/22/2003
 */

/*
 * Copyright 1997, 1998, 1999, 2000, 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/22/2003  INB	Made <code>setWritten</code> externally accessible.
 * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
 * 03/01/1997  INB	Created.
 *
 */
class OutputStream
    extends java.io.OutputStream
    implements com.rbnb.api.BuildInterface,
	       java.io.DataOutput
{
    /**
     * binary mode?
     * <p>
     * If this field is set, the serialization is done using binary values for
     * all numeric types and indexes for array and parameter values.
     * <p>
     * If this field is clear, the serialization is done using text values for
     * all numeric types, array, and parameter values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 02/02/2001
     */
    private boolean binary = false;

    /**
     * the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */
    private java.util.Date buildDate = null;

    /**
     * the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */
    private String buildVersion = null;

    /**
     * stage writes for a future flush?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 07/26/2001
     */
    private boolean stage = false;

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
     * number of bytes written at the last flush.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 02/02/2001
     */
    private long lastFlush = 0;

    /**
     * number of bytes written to the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 02/02/2001
     */
    private long written = 0;

    /**
     * output stream to write to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/05/2001
     */
    private java.io.OutputStream os = null;

    /**
     * staged output.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 07/26/2001
     */
    private java.util.Vector staged = null;

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

    // Package class constants:
    final static String[] BOOLEANS = { "FALSE", "TRUE" };

    /**
     * Class constructor to build an <code>OutputStream</code> from a
     * <code>java.io.OutputStream</code>, a binary mode flag, and a buffer
     * size.
     * <p>
     * If <code>sizeI</code> is equal to zero, the putput stream is used
     * directly. Otherwise, a <code>java.io.BufferedOutputStream</code> is
     * put in place.
     *
     * @author Ian Brown
     *
     * @param oStreamI  the <code>java.io.OutputStream</code> to write to.
     * @param binaryI   binary mode?
     * @param sizeI	the size of the buffer.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the output stream.
     * @since V1.0
     * @version 03/07/2001
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
    OutputStream(java.io.OutputStream oStreamI,boolean binaryI,int sizeI)
	throws java.io.IOException
    {
	ros = oStreamI;

	if (sizeI == 0) {
	    os = oStreamI;
	} else {
	    os = new java.io.BufferedOutputStream(oStreamI,sizeI);
	}
	setBinary(binaryI);
    }

    /**
     * Adds an entry to the staging buffer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param entryI the byte array to add.
     * @see #removeStaged(int)
     * @see #writeStaged()
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    private final void addStaged(byte[] entryI) {
	if (staged == null) {
	    staged = new java.util.Vector();
	}
	staged.addElement(entryI);
    }

    /**
     * Adds a vector of objects to the staging buffer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vectorI the vector.
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
    final void addStaged(java.util.Vector vectorI) {
	if (staged == null) {
	    staged = new java.util.Vector();
	}
	staged.addElement(vectorI);
    }

    /**
     * Adds a parameter with an optional bracket to the staging buffer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the parameters array.
     * @param parameterI  the index into the parameters array.
     * @param bracketI	  write a bracket?
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
    final void addStaged(String[] parametersI,
			 int parameterI,
			 boolean bracketI)
    {
	if (staged == null) {
	    staged = new java.util.Vector();
	}
	staged.addElement(parametersI);
	staged.addElement
	    (new Integer(bracketI ? -(parameterI + 1) : parameterI));
    }

    /**
     * Adds a <code>Serializable</code> object to the staging buffer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code>.
     * @param parametersI   the parameters array.
     * @param parameterI    the index into the parameters array.
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
    final void addStaged(Serializable serializableI,
			 String[] parametersI,
			 int parameterI)
    {
	if (staged == null) {
	    staged = new java.util.Vector();
	}
	staged.addElement(serializableI);
	staged.addElement(parametersI);
	staged.addElement(new Integer(parameterI));
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
     * @version 08/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    public void close() {
	os = null;
    }

    /**
     * Flushes the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.Exception
     *		  thrown if there is a problem flushing the stream.
     * @since V1.0
     * @version 08/20/2001
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
    public void flush()
	throws java.io.IOException
    {
	setStage(false,false);
	if (getWritten() != getLastFlush()) {

	    /* Send a new line and then flush the stream. */
	    if (!getBinary()) {
		writeString("\r\n");
	    }
	    try {
		os.flush();
		if (ros != os) {
		    ros.flush();
		}

	    } catch (java.lang.NullPointerException e) {
		// If our client goes away unexpectedly, we seem to have a
		// chance to end up with a null pointer.  We throw an
		// java.io.IOException in this case, which allows our caller to
		// clean up, since that is what it expects.
		throw new java.io.IOException(e.getMessage());
	    }

	    setLastFlush(getWritten());
	}
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
     * Gets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build date.
     * @see #setBuildDate(java.util.Date)
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
    public final java.util.Date getBuildDate() {
	return (buildDate);
    }

    /**
     * Gets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build version string.
     * @see #setBuildVersion(String)
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
    public final String getBuildVersion() {
	return (buildVersion);
    }

    /**
     * Gets the file pointer for this <code>OutputStream</code>.
     * <p>
     * The underlying <code>OutputStream</code> must be a
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
    final long getFilePointer()
	throws java.io.IOException
    {
	if (!(ros instanceof RandomAccessOutputStream)) {
	    throw new java.lang.IllegalStateException
		("The underlying stream does not support file pointers.");
	}

	return (((RandomAccessOutputStream) ros).getFilePointer());
    }

    /**
     * Gets the number of bytes written at the last flush.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes written at the last flush.
     * @see #setLastFlush(long)
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
    private final long getLastFlush() {
	return (lastFlush);
    }

    /**
     * Gets the stage output flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return stage output?
     * @see #setStage(boolean,boolean)
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
    final boolean getStage() {
	return (stage);
    }

    /**
     * Gets the number of bytes written to the stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes written.
     * @see #setWritten(long)
     * @see #size()
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
    final long getWritten() {
	return (written);
    }

    /**
     * Removes the last element of the staged output.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the first index following the remaining valid information.
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/26/2001  INB	Created.
     *
     */
    public final void removeStaged(int indexI) {
	if (staged != null) {
	    for (int length = staged.size();
		 length > indexI;
		 --length) {
		staged.removeElementAt(length - 1);
	    }
	}
    }

    /**
     * Sets the binary mode flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param binaryI  binary mode?
     * @exception java.io.IOException
     *		  thrown if there is a problem switching modes.
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
		!(ros instanceof RandomAccessOutputStream) &&
		(!(ros instanceof java.io.FileOutputStream) ||
		 !ros.getClass().isAssignableFrom
		 (Class.forName("java.io.FileOutputStream")))) {
		// In binary mode, we write out a null character to indicate
		// where the mode switch occurs.
		write(0);
		flush();
	    }
	} catch (java.lang.ClassNotFoundException e) {
	}
    }

    /**
     * Sets the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
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
    public final void setBuildDate(java.util.Date buildDateI) {
	buildDate = buildDateI;
    }

    /**
     * Sets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version.
     * @see #getBuildVersion()
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
    public final void setBuildVersion(String buildVersionI) {
	buildVersion = buildVersionI;
    }

    /**
     * Sets the number of bytes written at the last flush.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lastFlushI  the number of bytes written at the last flush.
     * @see #getLastFlush()
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
    final void setLastFlush(long lastFlushI) {
	lastFlush = lastFlushI;
    }

    /**
     * Sets the stage output flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stageI stage the output?
     * @param clearI clear the existing staging buffer?
     * @return the index into the staging buffer marking the start of this
     *	       staging sequence.
     * @see #getStage()
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
    public final int setStage(boolean stageI,boolean clearI) {
	stage = stageI;
	if (clearI) {
	    staged = null;
	}

	return ((staged == null) ? 0 : staged.size());
    }

    /**
     * Sets the number of bytes written.
     * <p>
     *
     * @author Ian Brown
     *
     * @param writtenI  the new number of bytes written.
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
     * 02/02/2001  INB	Created.
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
     * @version 07/26/2001
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
    public final void write(byte bI[],int offI,int lenI)
	throws java.io.IOException
    {
	if (getStage()) {
	    java.io.ByteArrayOutputStream baos =
		new java.io.ByteArrayOutputStream();
	    baos.write(bI,offI,lenI);
	    addStaged(baos.toByteArray());
	    baos.close();
	} else {
	    writeStaged();
	    os.write(bI,offI,lenI);
	    setWritten(getWritten() + lenI);
	}
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
     * @version 08/20/2001
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
    public void write(int bI)
	throws java.io.IOException
    {
	if (getStage()) {
	    java.io.ByteArrayOutputStream baos =
		new java.io.ByteArrayOutputStream();
	    baos.write(bI);
	    addStaged(baos.toByteArray());
	    baos.close();
	} else {
	    writeStaged();
	    os.write(bI);
	    setWritten(getWritten() + 1);
	}
    }

    /**
     * Writes a boolean value to the <code>OutputStream</code>.
     * <p>
     * This method uses the <code>writeElement()</code> method and the constant
     * array <code>BOOLEANS</code> to write out the boolean value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the boolean value.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing out the boolean value.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeBoolean(boolean vI)
	throws java.io.IOException
    {
	writeElement(BOOLEANS,vI ? 1 : 0);
    }

    /**
     * Writes a single byte value.
     * <p>
     * In binary mode, the method uses <code>write()</code> to write the byte.
     * <p>
     * In text mode, the method converts the byte to a string and writes that
     * out.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the byte value.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the byte value.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeByte(int vI)
	throws java.io.IOException
    {
	if (getBinary()) {
	    write(vI);
	} else {
	    writeString(Byte.toString((byte) vI));
	}
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
     * @version 08/20/2001
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
    public void writeBytes(String vI)
	throws java.io.IOException
    {
	write(vI.getBytes());
    }

    /**
     * Writes out a character to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the character as a pair of bytes.
     * <p>
     * In text mode, the method writes the character as a single byte.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the character.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the character.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeChar(int vI)
	throws java.io.IOException
    {
	if (getBinary()) {
	    write((vI >> 8) & 0xff);
	    write((vI     ) & 0xff);
	} else {
	    write(vI);
	}
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
     * @version 08/20/2001
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
    public void writeChars(String vI)
	throws java.io.IOException
    {
	for (int idx = 0; idx < vI.length(); ++idx) {
	    writeChar((int) vI.charAt(idx));
	}
    }

    /**
     * Writes a double to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method converts the double to a long using
     * doubleToLongBits and writes that.
     * <p>
     * In text mode, the method converts the double to a string and writes
     * that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the double.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the double.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeDouble(double vI)
	throws java.io.IOException
    {
	if (getBinary()) {
	    writeLong(Double.doubleToLongBits(vI));
	} else {
	    writeString(Double.toString(vI));
	}
    }

    /**
     * Writes out an array element to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the element index is written. Depending on the number of
     * entries in the array, the element will be written as:
     * <p><ul>
     * <li>a byte if the number of elements <=
     *	   <code>Byte.MAX_VALUE</code>,</li>
     * <li>a short if the number of elements <=
     *	   <code>Short.MAX_VALUE</code>, or</li>
     * <li>an int if the number of elements <= <code>Int.MAX_VALUE</code>.</li>
     * </ul><p>
     * In text mode, the element is written using the appropriate
     * <code>writeXXX</code> method.
     * <p>
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
     * @version 08/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    public void writeElement(Object arrayI,int indexI)
	throws java.io.IOException
    {
	if (!arrayI.getClass().isArray()) {
	    throw new java.lang.IllegalArgumentException
		(arrayI + " is not an array.");
	}

	if (getBinary()) {
	    // In binary mode, write out the index using the appropriate method
	    // for the number of elements in the array.
	    int elements = java.lang.reflect.Array.getLength(arrayI);
	    if (elements <= Byte.MAX_VALUE) {
		write(indexI);
	    } else if (elements <= Short.MAX_VALUE) {
		writeShort(indexI);
	    } else {
		writeInt(indexI);
	    }

	} else {
	    // In text mode, write out the element using the appropriate method
	    // based on the type of array.

	    if (arrayI instanceof boolean[]) {
		writeBoolean(((boolean[]) arrayI)[indexI]);
	    } else if (arrayI instanceof byte[]) {
		writeByte(((byte[]) arrayI)[indexI]);
	    } else if (arrayI instanceof double[]) {
		writeDouble(((double[]) arrayI)[indexI]);
	    } else if (arrayI instanceof float[]) {
		writeFloat(((float[]) arrayI)[indexI]);
	    } else if (arrayI instanceof int[]) {
		writeInt(((int[]) arrayI)[indexI]);
	    } else if (arrayI instanceof long[]) {
		writeLong(((long[]) arrayI)[indexI]);
	    } else if (arrayI instanceof short[]) {
		writeShort(((short[]) arrayI)[indexI]);
	    } else if (arrayI instanceof String[]) {
		writeString(((String[]) arrayI)[indexI]);
	    }
	}
    }

    /**
     * Writes a float to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method converts the float to an int using
     * floatToIntBits and writes that.
     * <p>
     * In text mode, the method converts the float to a string and writes that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the float.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the float.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeFloat(float vI)
	throws java.io.IOException
    {
	if (getBinary()) {
	    writeInt(Float.floatToIntBits(vI));
	} else {
	    writeString(Float.toString(vI));
	}
    }

    /**
     * Writes an int to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the integer as four bytes.
     * <p>
     * In text mode, the method converts the integer to a string and writes
     * that.
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
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public void writeInt(int vI)
	throws java.io.IOException
    {
	if (getBinary()) {
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

	} else {
	    writeString(Integer.toString(vI));
	}
    }

    /**
     * Writes a long to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the long as eight bytes.
     * <p>
     * In text mode, the method converts the long to a string and writes that.
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
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public void writeLong(long vI)
	throws java.io.IOException
    {
	if (getBinary()) {
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
	} else {
	    writeString(Long.toString(vI));
	}
    }

    /**
     * Writes a parameter to the <code>OutputStream</code>.
     * <p>
     * This method uses <code>writeElement()</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the list of parameters.
     * @param indexI	   the index of the parameter.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the parameter.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeParameter(String[] parametersI,
						  int parameterI)
	throws java.io.IOException
    {
	writeElement(parametersI,parameterI);
    }

    /**
     * Writes a short to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the short as two bytes.
     * <p>
     * In text mode, the method converts the short to a string and writes that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the short.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the short.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeShort(int vI)
	throws java.io.IOException
    {
	if (getBinary()) {
	    write((vI >> 8) & 0xff);
	    write((vI     ) & 0xff);
	} else {
	    writeString(Short.toString((short) vI));
	}
    }

    /**
     * Writes out staged information.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @see  #addStaged(byte[])
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
    private void writeStaged()
	throws java.io.IOException
    {
	if (staged != null) {
	    java.util.Vector oldStaged = staged;
	    staged = null;
	    for (int idx = 0, eIdx = oldStaged.size();
		 idx < eIdx;
		 ++idx) {
		Object entry = oldStaged.elementAt(idx);

		if (entry instanceof byte[]) {
		    byte[] bytes = (byte[]) entry;
		    write(bytes,0,bytes.length);

		} else if (entry instanceof String[]) {
		    String[] parameters = (String[]) entry;
		    int parameter =
			((Integer) oldStaged.elementAt(++idx)).intValue();
		    if (parameter >= 0) {
			writeParameter(parameters,parameter);
		    } else {
			writeParameter(parameters,-(parameter + 1));
			Serialize.writeOpenBracket(this);
		    }

		} else if (entry instanceof java.util.Vector) {
		    java.util.Vector vector = (java.util.Vector) entry;
		    for (int idx1 = 0; idx1 < vector.size(); ++idx1) {
			entry = vector.elementAt(idx1);
			if (entry instanceof byte[]) {
			    write((byte[]) entry);
			} else if (entry instanceof Byte) {
			    writeByte(((Byte) entry).byteValue());
			} else if (entry instanceof Double) {
			    writeDouble(((Double) entry).doubleValue());
			} else if (entry instanceof Float) {
			    writeFloat(((Float) entry).floatValue());
			} else if (entry instanceof Integer) {
			    writeInt(((Integer) entry).intValue());
			} else if (entry instanceof Long) {
			    writeLong(((Long) entry).longValue());
			} else if (entry instanceof Short) {
			    writeShort(((Short) entry).shortValue());
			}
		    }
		    
		} else {
		    String[] parameters =
			(String[]) oldStaged.elementAt(++idx);
		    int parameter =
			((Integer) oldStaged.elementAt(++idx)).intValue();
		    ((Serializable) entry).writeStaged(this,
						       parameters,
						       parameter);
		}
	    }
	}
    }

    /**
     * Writes a string to the <code>OutputStream</code>.
     * <p>
     * In binary mode, this method uses <code>writeUTF()</code>.
     * <p>
     * In text mode, this method uses <code>writeBytes()</code> and adds a
     * space character after the string, unless the string is a <CR><LF>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the string.
     * @since V1.0
     * @version 08/20/2001
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
    public void writeString(String vI)
	throws java.io.IOException
    {
	if (getBinary()) {
	    writeUTF(vI);
	} else {
	    writeBytes(vI);
	    if (!vI.equals("\r\n")) {
		writeChar(' ');
	    }
	}
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
     * @version 08/20/2001
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
    public void writeUTF(String strI)
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
