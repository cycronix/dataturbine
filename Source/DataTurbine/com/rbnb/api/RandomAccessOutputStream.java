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
 * Extended <code>OutputStream</code> for writing from
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
 *
 */
class RandomAccessOutputStream
    extends java.io.OutputStream
{
    /**
     * the current file position.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/09/2001
     */
    private long position = 0;

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
     * the associated <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 06/15/2006
     */
    private RandomAccessInputStream rais = null;

    /**
     * Class constructor to build a <code>RandomAccessOutputStream</code> for a
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
    RandomAccessOutputStream(RandomAccessFile rafI,RandomAccessInputStream raisI) {
	super();
	setRAF(rafI);
	setRAIS(raisI);
    }

    /**
     * Closes this <code>RandomAccessOutputStream</code> and releases any
     * system resources associated with it.
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
     * Flushes this <code>RandomAccessOutputStream</code>.
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
    public final void flush()
	throws java.io.IOException
    {
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
     * Sets the file pointer for this <code>RandomAccessOutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param  positionI  the new file pointer position.
     * @see #getFilePointer()
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
     * Sets the <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Eric Friets
     *
     * @param raisI  the <code>RandomAccessInputStream</code>.
     * @see #getRAIS()
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
    private final void setRAIS(RandomAccessInputStream raisI) {
	rais = raisI;
    }

    /**
     * Gets the <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Eric Friets
     *
     * @return  the <code>RandomAccessInputStream</code>.
     * @see #setRAIS()
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
    private final RandomAccessInputStream getRAIS() {
	return rais;
    }

    /**
     * Writes out a single byte.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI  the byte to write.
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
     * 06/15/2006  EMF  Notify input stream its cache may be out of date
     *
     */
    public final void write(int bI)
	throws java.io.IOException
    {
	getRAIS().clearCache(); //EMF 6/15/06: notify input stream
	getRAF().seek(getFilePointer());
	getRAF().write(bI);
	setFilePointer(getFilePointer() + 1);
    }

    /**
     * Writes <code>lenI</code> bytes from the specified byte array starting at
     * offset <code>offI</code> to this <code>RandomAccessOutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bI    the byte array to write.
     * @param offI  the starting offset.
     * @param lenI  the number of bytes to write.
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
     * 06/15/2006  EMF  Notify input stream its cache may be out of date
     *
     */
    public final void write(byte[] bI,int offI,int lenI)
	throws java.io.IOException
    {
	getRAIS().clearCache(); //EMF 6/15/06: notify input stream
	getRAF().seek(getFilePointer());
	getRAF().write(bI,offI,lenI);
	setFilePointer(getFilePointer() + lenI);
    }
}
