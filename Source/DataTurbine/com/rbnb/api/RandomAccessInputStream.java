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
 * Extended <code>InputStream</code> for reading from
 * <code>RandomAccessFiles</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RandomAccessFile
 * @see com.rbnb.api.RandomAccessOutputStream
 * @since V2.0
 * @version 10/04/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/09/2001  INB	Created.
 * 06/14/2006  EMF      Added buffering to minimize performance drag
 *                      from single byte reads, especially to OSX over Samba.
 *
 */
class RandomAccessInputStream
    extends java.io.InputStream
{
    /**
     * the marked position.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/09/2001
     */
    private long marked = -1;

    /**
     * the current file position.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/09/2001
     */
    private long position = -1;

    /**
     * the <code>RandomAccessFile</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/09/2001
     */
    private RandomAccessFile raf = null;

    /**
     * buffer for block reads
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 06/14/2006
     */
    private byte[] buffer = new byte[512];

    /**
     * filePointer at start of buffer
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 06/14/2006
     */
    private long bufStart = 0;

    /**
     * last data point in buffer
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 06/14/2006
     */
    private int bufLength = -1;

    /**
     * Class constructor to build a <code>RandomAccessInputStream</code> for a

    /**
     * Class constructor to build a <code>RandomAccessInputStream</code> for a
     * <code>RandomAccessFile</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rafI  the <code>RandomAccessFile</code>.
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    RandomAccessInputStream(RandomAccessFile rafI) {
	super();
	setRAF(rafI);
    }

    /**
     * Returns the number of bytes that can be read from this
     * <code>RandomAccessInputStream</code> without blocking by the next caller
     * of a method for this <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the number of available bytes.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final int available()
	throws java.io.IOException
    {
	return ((int) Math.max(Integer.MAX_VALUE,
			       (getRAF().length() - getFilePointer())));

    }

    /**
     * Closes this <code>RandomAccessInputStream</code> and releases any system
     * resources associated with it.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final void close()
	throws java.io.IOException
    {
	setRAF(null);
	super.close();
    }

    /**
     * Gets the current file position.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the current file position.
     * @see #setFilePointer(long)
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final long getFilePointer() {
	return (position);
    }

    /**
     * Gets the <code>RandomAccessFile</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the <code>RandomAccessFile</code>.
     * @see #setRAF(com.rbnb.api.RandomAccessFile)
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    private final RandomAccessFile getRAF() {
	return (raf);
    }

    /**
     * Marks the current position in this
     * <code>RandomAccessInputStream</code>. A subsequent call to the
     * <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     * <p>
     * The <code>readlimitI</code> argument tells this
     * <code>RandomAccessInputStream</code> to allow that many bytes to be read
     * before the mark position gets invalidated.
     * <p>
     *
     * @author Ian Brown
     *
     * @param readlimitI  the maximum limit of bytes that can be read before
     *			  the mark position becomes invalid. This value is
     *			  ignored - the mark is never invalidated.
     * @see #reset()
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final void mark(int readlimitI) {
	marked = getFilePointer();
    }

    /**
     * Is <code>mark</code>/<code>reset</code> supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return  always returns <code>true</code>.
     * @see #mark(int)
     * @see #reset()
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final boolean markSupported() {
	return (true);
    }

    /**
     * Reads the next byte of input from the
     * <code>RandomAccessInputStream</code>.
     * <p>
     * The value byte is returned as an int in the range 0 to 255. If no byte
     * is available because the end of the stream has been reached, the value
     * -1 is returned. This method blocks until input data is available, the
     * end of the stream is detected, or an exception is thrown.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the byte read or -1 if the end of the file is reached.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     * 06/14/2006  EMF  Buffer reads to improve performance.
     *
     */
    public final int read()
	throws java.io.IOException
    {
// System.err.println("MJM DEBUG: FilePointer: "+getFilePointer()+", getRAF.ptr: "+getRAF().getFilePointer());
	// try saving some work by avoiding unecessary seeks MJM 6/13/06
	// if(getFilePointer() != getRAF().getFilePointer())

	//EMF 6/14/06
	if ((getFilePointer()>=bufStart) && (getFilePointer()<bufStart+bufLength)) {
		int idx = (int)(getFilePointer()-bufStart);
		setFilePointer(getFilePointer()+1);
		return (int)buffer[idx];
	} else {
		getRAF().seek(getFilePointer());
		bufStart=getFilePointer();
		bufLength = getRAF().read(buffer,0,buffer.length);
		if (bufLength==-1) return -1;
		setFilePointer(getFilePointer() + 1);
		return (int)buffer[0];
	}
		
	// original code below
	//int readR = getRAF().read();
	//setFilePointer(getFilePointer() + 1);
	//return (readR);
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of
     * bytes. An attempt is made to read as many as <code>lenI</code> bytes,
     * but a smaller number may be read, possibly zero. The number of bytes
     * actually read is returned as an integer.
     * <p>
     * This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     * <p>
     * If <code>bI</code> is null, a <code>NullPointerException</code> is
     * thrown.
     * <p>
     * If <code>offI</code> is negative, or <code>lenI</code> is negative, or
     * <code>offI</code>+</code>len</code> is greater than the length of the
     * array <code>bI</code>, then an <code>IndexOutOfBoundsException</code> is
     * thrown.
     * <p>
     * If <code>lenI</code> is zero, then no bytes are read and 0 is returned;
     * otherwise, there is an attempt to read at least one byte. If no byte is
     * available because the stream is at end of file, the value -1 is
     * returned; otherwise, at least one byte is read and stored into
     * <code>bI</code>.
     * <p>
     * The first byte read is stored into element <code>bI[offI]</code>, the
     * next one into <code>bI[offI+1]</code>, and so on. The number of bytes
     * read is, at most, equal to <code>lenI</code>. Let <code>k</code> be the
     * number of bytes actually read; these bytes will be stored in elements
     * <code>bI[offI]</code> through <code>bI[offI+k-1]</code>, leaving
     * elements <code>bI[offI+k]</code> through <code>bI[offI+lenI-1]</code>
     * unaffected.
     * <p>
     * In every case, elements <code>bI[0]</code> through
     * <code>bI[offI-1]</code> and elements <code>bI[offI+lenI]</code> through
     * <code>bI[bI.length-1]</code> are unaffected.
     * <p>
     * If the first byte cannot be read for any reason other than end of file,
     * then an <code>IOException</code> is thrown. In particular, an
     * <code>IOException</code> is thrown if the input stream has been closed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI    the array of bytes.
     * @param offI  the starting offset.
     * @param lenI  the number of bytes to read.
     * @return  the number of bytes actually read.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     * 06/14/2006  EMF  Buffer reads to improve performance.
     *
     */
    public final int read(byte[] bI,int offI,int lenI)
	throws java.io.IOException
    {
	//EMF 6/14/06
	if ((getFilePointer()>=bufStart) && (getFilePointer()+lenI<bufStart+bufLength)) {
		int idx = (int)(getFilePointer()-bufStart);
		for (int i=0;i<lenI;i++) {
			bI[offI+i]=buffer[idx+i];
		}
		setFilePointer(getFilePointer()+lenI);
		return lenI;
	} else {
		getRAF().seek(getFilePointer());
		bufStart=getFilePointer();
		bufLength = getRAF().read(buffer,0,buffer.length);
		if (bufLength==-1) return -1;
		int num=(lenI<bufLength?lenI:bufLength);
		for (int i=0;i<num;i++) {
			bI[offI+i]=buffer[i];
		}
		setFilePointer(getFilePointer() + num);
		return num;
	}
		
//original code
//	getRAF().seek(getFilePointer());
//	int nReadR = getRAF().read(bI,offI,lenI);
//	setFilePointer(getFilePointer() + nReadR);
//	return (nReadR);
    }

    /**
     * Clears the read cache.  Called by associated
     * <code>RandomAccessOutputStream</code> when a write
     * has occurred.
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 06/15/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/15/2006  EMF	Created.
     *
     */
    public final void clearCache() {
	bufLength = -1;
    }

    /**
     * Repositions this stream to the position at the time that the
     * <code>mark</code> method was last called on this
     * <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #mark(int)
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final void reset() {
	setFilePointer(marked);
    }

    /**
     * Sets the file pointer for this <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param  positionI  the new file pointer position.
     * @see #getFilePointer()
     * @see #skip(long)
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final void setFilePointer(long positionI) {
	position = positionI;
    }

    /**
     * Sets the <code>RandomAccessFile</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rafI  the <code>RandomAccessFile</code>.
     * @see #getRAF()
     * @since V2.0
     * @version 03/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    private final void setRAF(RandomAccessFile rafI) {
	raf = rafI;
    }

    /**
     * Skips over and discards <code>nI</code> bytes of data from this
     * <code>RandomAccessInputStream</code>. The <code>skip</code> method may,
     * for a variety of reasons, end up skipping over some smaller number of
     * bytes, possibly 0.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nI  the number of bytes to skip.
     * @return  the number of bytes skipped.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     *
     */
    public final long skip(long nI)
	throws java.io.IOException
    {
	long current = getFilePointer(),
	     skippedR = Math.min(nI,getRAF().length() - current);
	setFilePointer(current + skippedR);
	return (skippedR);
    }
}
