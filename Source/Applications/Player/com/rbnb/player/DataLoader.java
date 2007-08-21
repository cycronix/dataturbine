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


package com.rbnb.player;

/**
 * DataLoader class to load files from a file or set of files.
 * <p>
 * The file or files can be located in any of the following places:
 * <p><ol>
 * <li>The local directory,</li>
 * <li>The user's home directory, or</li>
 * <li>The Java CLASSPATH.</li>
 * </ol><p>
 *
 * @author Ian Brown
 *
 * @since V1.2
 * @version 10/08/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/08/2004  JPW	Upgrade to RBNB V2 Player; moved from the package
 *			COM.Creare.RBNB.Widgets to com.rbnb.player.
 * 11/13/2001  INB	Created.
 *			This is a complete re-write of the DataLoader written
 *			by Ursula Bergstrom from code I'd previously
 *			implemented. This version uses a ClassLoader to load
 *			the file from the CLASSPATH and doesn't support any
 *			other locations.
 *
 */
public class DataLoader {

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.2
     * @version 11/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/13/2001  INB	Created.
     *
     */
    public DataLoader() {
	super();
    }

    /**
     * Loads data from a file with the specified name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the file.
     * @return the byte array containing the loaded data.
     * @exception java.io.IOException
     *		  if there is a problem reading the data.
     * @since V1.2
     * @version 11/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/13/2001  INB	Created.
     *
     */
    public final byte[] loadData(String nameI)
	throws java.io.IOException
    {
	
	// Another method Ian developed to load images by specifically
	// searching the classpath and "user.home" and "user.dir" properties
	// is found in:
	// Applications/AudioVideo/com/rbnb/media/protocol/VideoRetriever.java
	// in the method loadImages()
	
	java.io.InputStream is =
	    ClassLoader.getSystemClassLoader().getResourceAsStream(nameI);

	if (is == null) {
	    throw new java.io.FileNotFoundException
		("Cannot find file " + nameI + " in classpath.");
	}
	java.util.Vector entries = new java.util.Vector();
	int length = 0,
	    rl;
	do {
	    byte[] dataEntry = new byte[256];
	    rl = is.read(dataEntry);
	    if (rl > 0) {
		length += rl;
		entries.addElement(dataEntry);
		entries.addElement(new Integer(rl));
	    }
	} while (rl == 256);

	byte[] dataR = new byte[length];
	for (int idx = 0, position = 0; idx < entries.size(); idx += 2) {
	    byte[] dataEntry = (byte[]) entries.elementAt(idx);
	    int deL = ((Integer) entries.elementAt(idx + 1)).intValue();
	    System.arraycopy(dataEntry,
			     0,
			     dataR,
			     position,
			     deL);
	    position += deL;
	}

	return (dataR);
    }
}

