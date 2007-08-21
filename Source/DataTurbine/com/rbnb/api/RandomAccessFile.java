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
 * Extended <code>RandomAccessFile</code> that provides an
 * <code>InputStream</code> and an <code>OutputStream</code> so that the
 * regular I/O streams can be used to perform the I/O.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RandomAccessInputStream
 * @see com.rbnb.api.RandomAccessOutputStream
 * @since V2.0
 * @version 08/30/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/08/2001  INB	Created.
 *
 */
class RandomAccessFile
    extends java.io.RandomAccessFile
{
    /**
     * the <code>RandomAccessInputStream</code> for reading from the file.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/09/2001
     */
    private RandomAccessInputStream is = null;

    /**
     * the <code>RandomAccessOutputStream</code> for writing to the file.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/09/2001
     */
    private RandomAccessOutputStream os = null;

    /**
     * Class constructor to build a <code>RandomAccessFile</code> from a
     * the name of the file and a mode.
     * <p>
     * Creates a random access file stream to read from, and optionally to
     * write to, the file specified by the name of the file. A new
     * FileDescriptor object is created to represent this file connection.
     * <p>
     * The mode argument must either be equal to "r" or "rw", indicating that
     * the file is to be opened for input only or for both input and output,
     * respectively. The write methods on this object will always throw an
     * <code>IOException</code> if the file is opened with a mode of "r". If
     * the mode is "rw" and the file does not exist, then an attempt is made to
     * create it. An <code>IOException</code> is thrown if the file argument
     * refers to a directory.
     * <p>
     * If there is a security manager, its checkRead method is called with the
     * pathname of the file argument as its argument to see if read access to
     * the file is allowed. If the mode is "rw", the security manager's
     * checkWrite method is also called with the path argument to see if write
     * access to the file is allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the filename.
     * @param modeI  the access object.
     * @exception java.io.FileNotFoundException
     *		  thrown if the file exists but is a directory rather than a
     *		  regular file, or cannot be opened or created for any other
     *		  reason.
     * @exception java.io.IOException
     *		  thrown if an error occurs opening the file.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the mode argument is not equal to "r" or to "rw".
     * @exception java.lang.SecurityException
     *		  thrown if a security manager exists and its checkRead method
     *		  denies read access to the file or the mode is "rw" and the
     *		  security manager's checkWrite method denies write access to
     *		  the file.
     * @see #RandomAccessFile(String,String)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     * 06/15/2006  EMF  Pass input stream to output stream, so it can be 
     *                  notified when write occurs (cache will be out of date).
     *
     */
    RandomAccessFile(String nameI,String modeI)
	throws java.io.FileNotFoundException,
	       java.io.IOException
    {
	super(nameI,modeI);

	setRAIS(new RandomAccessInputStream(this));
	if (modeI.equalsIgnoreCase("RW")) {
	    setRAOS(new RandomAccessOutputStream(this,getRAIS()));
	}
    }

    /**
     * Class constructor to build a <code>RandomAccessFile</code> from a
     * regular <code>File</code> and a mode.
     * <p>
     * Creates a random access file stream to read from, and optionally to
     * write to, the file specified by the File argument. A new FileDescriptor
     * object is created to represent this file connection.
     * <p>
     * The mode argument must either be equal to "r" or "rw", indicating that
     * the file is to be opened for input only or for both input and output,
     * respectively. The write methods on this object will always throw an
     * <code>IOException</code> if the file is opened with a mode of "r". If
     * the mode is "rw" and the file does not exist, then an attempt is made to
     * create it. An <code>IOException</code> is thrown if the file argument
     * refers to a directory.
     * <p>
     * If there is a security manager, its checkRead method is called with the
     * pathname of the file argument as its argument to see if read access to
     * the file is allowed. If the mode is "rw", the security manager's
     * checkWrite method is also called with the path argument to see if write
     * access to the file is allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fileI  the file object.
     * @param modeI  the access object.
     * @exception java.io.FileNotFoundException
     *		  thrown if the file exists but is a directory rather than a
     *		  regular file, or cannot be opened or created for any other
     *		  reason.
     * @exception java.io.IOException
     *		  thrown if an I/O exception occurs opening the file.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the mode argument is not equal to "r" or to "rw".
     * @exception java.lang.SecurityException
     *		  thrown if a security manager exists and its checkRead method
     *		  denies read access to the file or the mode is "rw" and the
     *		  security manager's checkWrite method denies write access to
     *		  the file.
     * @see #RandomAccessFile(String,String)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/09/2001  INB	Created.
     * 06/15/2006  EMF  Pass input stream to output stream, so it can be 
     *                  notified when write occurs (cache will be out of date).
     *
     */
    RandomAccessFile(java.io.File fileI,String modeI)
	throws java.io.FileNotFoundException,
	       java.io.IOException
    {
	super(fileI,modeI);

	setRAIS(new RandomAccessInputStream(this));
	if (modeI.equalsIgnoreCase("RW")) {
	    setRAOS(new RandomAccessOutputStream(this,getRAIS()));
	}
    }

    /**
     * Closes the <code>RandomAccessFile</code> and releases any system
     * resources associated with it.
     * <p>
     * A closed <code>RandomAccessFile</code> cannot perform an input or output
     * operations and cannot be re-opened.
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
	if (getRAIS() != null) {
	    getRAIS().close();
	    setRAIS(null);
	}
	if (getRAOS() != null) {
	    getRAOS().close();
	    setRAOS(null);
	}
	super.close();
    }

    /**
     * Gets the <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RandomAccessInputStream</code>.
     * @see #setRAIS(com.rbnb.api.RandomAccessInputStream)
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
    final RandomAccessInputStream getRAIS() {
	return (is);
    }

    /**
     * Gets the <code>RandomAccessOutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RandomAccessOutputStream</code>.
     * @see #setRAOS(com.rbnb.api.RandomAccessOutputStream)
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
    final RandomAccessOutputStream getRAOS() {
	return (os);
    }

    /**
     * Sets the <code>RandomAccessInputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param raisI  the <code>RandomAccessInputStream</code>.
     * @see #getRAIS()
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
    private final void setRAIS(RandomAccessInputStream raisI) {
	is = raisI;
    }

    /**
     * Sets the <code>RandomAccessOutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param raosI  the <code>RandomAccessOutputStream</code>.
     * @see #getRAOS()
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
    private final void setRAOS(RandomAccessOutputStream raosI) {
	os = raosI;
    }
}
