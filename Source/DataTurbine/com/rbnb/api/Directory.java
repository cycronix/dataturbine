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
 * Directory class.
 * <p>
 * This class allows a user to work with a <code>java.io.File</code> object
 * that represents a directory.
 * <p>
 * This class exists because some early versions of Java do not support the
 * <code>java.io.File.listFiles</code> method.
 * <p>
 *
 * @author Ian Brown
 *
 * @see java.io.File
 * @since V2.1
 * @version 10/01/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/01/2004  JPW	Make this class Java 1.1.4 compliant, in order to
 *			compile under J#.  This has been done by setting
 *			ListFilesSupported to false and commenting out the
 *			call to java.io.File.listFiles()
 * 03/10/2003  INB	Created.
 *
 */
public final class Directory {

    /**
     * is <code>listFiles</code> supported by <code>java.io.File</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 08/03/2004
     */
    // JPW 10/01/2004: set ListFilesSupported to false (it had been true)
    private static boolean ListFilesSupported = false;

    /**
     * the <code>java.io.File</code> object to work on.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/10/2003
     */
    private java.io.File directory = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2003  INB	Created.
     *
     */
    Directory() {
	super();
    }

    /**
     * Class constructor to build a <code>Directory</code> for a
     * </code>java.io.File</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param directoryI the <code>java.io.File</code> representing the
     *			 directory.
     * @since V2.1
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2003  INB	Created.
     *
     */
    public Directory(java.io.File directoryI) {
	this();
	directory = directoryI;
    }

    /**
     * Gets a list of files in the directory.
     * <p>
     *
     * @author Ian Brown
     *
     * @return an array of <code>File</code> objects.
     * @exception java.io.IOException if an I/O exception occurs.
     * @since V2.0
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2004  JPW	To compile under J#, comment out the call to
     *                  listFiles().  At runtime, this section of the code
     *                  should never be reached because ListFilesSupported
     *                  has been set false.
     * 03/10/2003  INB	Moved into the <code>Directory</code> class and made it
     *			operate on the local <code>java.io.File</code> object.
     * 08/30/2002  INB	Created.
     *
     */
    public final java.io.File[] listFiles()
	throws java.io.IOException
    {
	java.io.File[] filesR = null;

	if (ListFilesSupported) {
	    // JPW 10/01/2004: This code will not be reached because
	    //                 ListFilesSupported has been set false.  Just to
	    //                 make sure, throw an exception.
	    throw new java.io.IOException(
                            "ERROR: This code shouldn't be reached!");
	    /*
	    try {
		filesR = directory.listFiles();
	    } catch (java.lang.NoSuchMethodError e) {
		ListFilesSupported = false;
		filesR = listFilesFromNames();
	    }
	    */
	} else {
	    filesR = listFilesFromNames();
	}

	return (filesR);
    }

    /**
     * Gets a list of files in the directory via the <code>File.list</code>
     * method.
     * <p>
     *
     * @author Ian Brown
     *
     * @return an array of <code>File</code> objects.
     * @exception java.io.IOException if an I/O exception occurs.
     * @since V2.0
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2003  EMF  Switched File constructor to eliminate directory in name
     * 03/10/2003  INB	Moved into the <code>Directory</code> class and made it
     *			operate on the local <code>java.io.File</code> object.
     * 08/30/2002  INB	Created.
     *
     */
    private final java.io.File[] listFilesFromNames()
	throws java.io.IOException
    {
	java.io.File[] filesR = null;
	String[] files = directory.list();

	if (files != null) {
	    filesR = new java.io.File[files.length];

	    for (int idx = 0; idx < files.length; ++idx) {
		filesR[idx] = new java.io.File(directory,files[idx]);
	    }
	}
	
	return (filesR);
    }
}
