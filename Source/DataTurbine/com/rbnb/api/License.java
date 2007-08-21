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
 * License file handler.
 * <p>
 * This class handles licensing for the RBNB. Every license has a unique
 * number and a set of parameters that determine what the RBNB is able to do
 * using that license. The license is by a password to ensure that
 * the file cannot be simply changed to gain extra privileges.
 * <p>
 * This file and the file LicenseStrings.java must be kept in sync.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.LicenseStrings
 * @since V1.0
 * @version 09/04/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/22/2001  INB	Converted for use with V2.
 * 11/01/1998  INB	Created.
 *
 */
class License {
    /**
     * are archives enabled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean larchives = false;

    /**
     * is broadcasting enabled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean lbroadcast = false;

    /**
     * are duplicate license checks enabled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean lcheck = false;

    /**
     * the license expiration date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private java.util.Date lexpires = null;

    /**
     * are mirrors enabled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean lmirrors = false;

    /**
     * the number of simultaneous clients supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private int lnclients = 0;

    /**
     * are remote connections allowed?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean lremote = false;

    /**
     * is routing enabled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean lrouting = false;

    /**
     * are the security features enabled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private boolean lsecurity = false;

    /**
     * the license serial number.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private long lserial = -1;

    /**
     * the license support ends on date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private java.util.Date lsupport = null;

    /**
     * the license version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/22/2001
     */
    private String lversion = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.IllegalStateExcption
     *		  thrown if there is a licensing problem.
     * @exception java.io.FileNotFoundException
     *		  if the license file cannot be found.
     * @exception java.io.IOException
     *		  if there is an I/O problem reading the license file.
     * @since V1.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    License()
	throws java.io.FileNotFoundException,
	       java.io.IOException
    {
	String classPath = System.getProperty("java.class.path"),
		pathSeparator = System.getProperty("path.separator"),
		fileSeparator = System.getProperty("file.separator");
	java.io.File licenseFile = null,
		     baseFile = null;

	/*
	 * Locate the license file. There are two possible files: the regular
	 * license file and the base license file. We'd prefer to get the
	 * regular one, so if we find a base file before we find the regular
	 * one, we save the path for the base until we run out of
	 * possibilities.
	*/
	for (int start = 0,
		 idx = classPath.indexOf(pathSeparator);
	     ;
	     start = idx + 1,
		 idx = classPath.indexOf(pathSeparator,start)) {
	    String path;

	    if (idx >= 0) {
		path = classPath.substring(start,idx);
	    } else {
		path = classPath.substring(start);
	    }
	    java.io.File directory = new java.io.File(path);

	    if (directory.isFile()) {
		// If the "directory" is actually a file, move up.
		if (path.lastIndexOf(fileSeparator) != -1) {
		    path = path.substring(0,path.lastIndexOf(fileSeparator));
		} else {
		    path = System.getProperty("user.dir");
		}
		directory = new java.io.File(path);
	    }

	    // See if the license file exists.
	    licenseFile = new java.io.File
		(directory,
		 LicenseStrings.getString(LicenseStrings.LICENSE_FILE));
	    if (licenseFile.exists()) {
		break;
	    }
	    licenseFile = null;

	    // Try the base license.
	    if (baseFile == null) {
		baseFile = new java.io.File
		    (directory,
		     LicenseStrings.getString(LicenseStrings.BASE_FILE));
		if (!baseFile.exists()) {
		    baseFile = null;
		}
	    }

	    if (idx == -1) {
		break;
	    }
	}
    
	java.io.InputStream inStr = null;
	java.io.InputStreamReader inStrReader = null;
	java.io.FileReader fRead = null;
	java.io.BufferedReader bRead = null;
    
	if (licenseFile != null) {
	    // If there is a license file, then we'll use it.
	    fRead = new java.io.FileReader(licenseFile);
	    bRead = new java.io.BufferedReader(fRead);

	} else if (baseFile != null) {
	    // If there is a base license file, then we'll use that.
	    fRead = new java.io.FileReader(baseFile);
	    bRead = new java.io.BufferedReader(fRead);

	} else {
	    // See if base file can be found in the class path using a
	    // ClassLoader.
	    ClassLoader cl = getClass().getClassLoader();
	    if (cl == null) {
		try {
		    cl = ClassLoader.getSystemClassLoader();
		} catch (java.lang.NoClassDefFoundError e) {
		}
	    }
	    inStr = cl.getResourceAsStream
		(LicenseStrings.getString(LicenseStrings.BASE_FILE));
	    if (inStr != null) {
		inStrReader = new java.io.InputStreamReader(inStr);
		bRead = new java.io.BufferedReader(inStrReader);
	    }
	}
    
	if (bRead == null) {
	    // NO LICENSE FILE WAS FOUND!
	    throw new java.lang.IllegalStateException
		(LicenseStrings.getString(LicenseStrings.NO_FILE));
	}
    
	// Read the file and get the signature string
	String signatureStr = read(bRead);
    
	// Close files.
	bRead.close();
	if (fRead != null) {
	    fRead.close();
	} else {
	    inStrReader.close();
	    inStr.close();
	}
	bRead = null;
	fRead = null;
	inStr = null;
	inStrReader = null;
    
	if ((signatureStr == null) || (signatureStr.equals(""))) {
	    throw new java.lang.IllegalStateException
		(LicenseStrings.getString(LicenseStrings.BAD_SIGNATURE));
	}
    
	// Now, open the **same** file again and check the signature
	// (It would be nice if Java had an appropriate rewind/reset feature
	//  for files, but they don't.  Therefore, we must open the stream
	//  again.)
	if (licenseFile != null) {
	    fRead = new java.io.FileReader(licenseFile);
	    bRead = new java.io.BufferedReader(fRead);
	} else if (baseFile != null) {
	    fRead = new java.io.FileReader(baseFile);
	    bRead = new java.io.BufferedReader(fRead);
	} else {
	    // See if base file can be found in the class path using a
	    // ClassLoader.
	    ClassLoader cl = getClass().getClassLoader();
	    if (cl == null) {
		try {
		    cl = ClassLoader.getSystemClassLoader();
		} catch (java.lang.NoClassDefFoundError e) {
		}
	    }
	    inStr = cl.getResourceAsStream
		(LicenseStrings.getString(LicenseStrings.BASE_FILE));
	    if (inStr != null) {
		inStrReader = new java.io.InputStreamReader(inStr);
		bRead = new java.io.BufferedReader(inStrReader);
	    }
	}
    
	if (bRead == null) {
	    // NO LICENSE FILE WAS FOUND!
	    throw new java.lang.IllegalStateException
		(LicenseStrings.getString(LicenseStrings.NO_FILE));
	}
    
	boolean bSignatureOK = checkSignature(bRead,signatureStr);

	// Close files.
	bRead.close();
	if (fRead != null) {
	    fRead.close();
	} else {
	    inStrReader.close();
	    inStr.close();
	}
    
	if (!bSignatureOK) {
	    throw new java.lang.IllegalStateException
		(LicenseStrings.getString(LicenseStrings.BAD_SIGNATURE));
	}
    }

    /**
     * Read the license file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bReadI the buffered reader connected to the file.
     * @exception java.io.IOException
     *		  thrown if there is a problem reading the license.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem with the license.
     * @since V1.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    private final String read(java.io.BufferedReader bReadI)
	throws java.io.IOException
    {
	String line;
	boolean gotSignature = false,
	    gotSupport = false;
	String signatureStr = null;
    
	/* Read lines from the license file and figure out what they mean. */
	while ((line = bReadI.readLine()) != null) {

	    if ((line.length() != 0) && (line.charAt(0) == '#')) {
		/* Lines starting with '#' are ignored. */
		continue;

	    } else if (gotSignature) {
		/* Only comments allowed after the signature. */
		throw new java.lang.IllegalStateException
		    (LicenseStrings.getString
		     (LicenseStrings.BAD_LICENSE_FILE));
	    }

	    int theEquals = line.indexOf("=");
	    if (theEquals == -1) {
		throw new java.lang.IllegalStateException
		    (LicenseStrings.getString
		     (LicenseStrings.BAD_LICENSE_FILE));
	    }

	    String parameter = line.substring(0,theEquals),
		value = null;

	    if (theEquals + 1 < line.length()) {
		value = line.substring(theEquals + 1);
	    }

	    if (parameter.equalsIgnoreCase
		/* Version # of license file. */
		(LicenseStrings.getString(LicenseStrings.VERSION))) {
		lversion = value;

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.SERIAL))) {
		/* Serial number of license. */
		lserial = Long.parseLong(value);

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.NCLIENTS))) {
		/* Number of clients supported. */
		if (value.equalsIgnoreCase
		    (LicenseStrings.getString(LicenseStrings.UNLIMITED))) {
		    lnclients = Integer.MAX_VALUE;
		} else {
		    lnclients = Integer.parseInt(value);
		}

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.SECURITY))) {
		/* Security enabled? */
		lsecurity = Boolean.valueOf(value).booleanValue();

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.ROUTING))) {
		/* Routing enabled? */
		lrouting = Boolean.valueOf(value).booleanValue();

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.MIRRORS))) {
		/* Mirrors enabled? */
		lmirrors = Boolean.valueOf(value).booleanValue();

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.ARCHIVES))) {
		/* Archives enabled? */
		larchives = Boolean.valueOf(value).booleanValue();

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.REMOTE))) {
		/* Remote connections enabled? */
		lremote = Boolean.valueOf(value).booleanValue();

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.CHECK))) {
		lcheck = Boolean.valueOf(value).booleanValue();
		/* Check serial number for duplicates? */

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.BROADCAST))) {
		/*
		 * Broadcast serial number so others can check for duplicates?
		 */
		lbroadcast = Boolean.valueOf(value).booleanValue();

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.SUPPORT))) {
		/*
		 * Support ends date. Make sure that the RBNB build date is
		 * before this date.
		 */

		gotSupport = true;
		if (value == null) {
		    throw new java.lang.IllegalStateException
			(LicenseStrings.getString
			 (LicenseStrings.BAD_SUPPORT_DATE));

		} else {
		    try {
			try {
			    lsupport =
				java.text.DateFormat.getDateInstance
				(java.text.DateFormat.SHORT,
				 java.util.Locale.US).parse(value);

			} catch (java.text.ParseException e1) {
			    try {
				lsupport =
				    java.text.DateFormat.getDateInstance
				    (java.text.DateFormat.MEDIUM,
				     java.util.Locale.US).parse(value);

			    } catch (java.text.ParseException e2) {
				try {
				    lsupport =
					java.text.DateFormat.getDateInstance
					(java.text.DateFormat.LONG,
					 java.util.Locale.US).parse(value);

				} catch (java.text.ParseException e3) {
				    lsupport =
					java.text.DateFormat.getDateInstance
					(java.text.DateFormat.FULL,
					 java.util.Locale.US).parse(value);
				}
			    }
			}

		    } catch (java.text.ParseException e) {
			throw new java.lang.IllegalStateException
			    (LicenseStrings.getString
			     (LicenseStrings.BAD_LICENSE_FILE));
		    }
		}

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.EXPIRES))) {
		/*
		 * Expiration date.
		*/
		if (value != null) {
		    try {
			try {
			    lexpires =
				java.text.DateFormat.getDateInstance
				(java.text.DateFormat.SHORT,
				 java.util.Locale.US).parse(value);

			} catch (java.text.ParseException e1) {
			    try {
				lexpires =
				    java.text.DateFormat.getDateInstance
				    (java.text.DateFormat.MEDIUM,
				     java.util.Locale.US).parse(value);

			    } catch (java.text.ParseException e2) {
				try {
				    lexpires =
					java.text.DateFormat.getDateInstance
					(java.text.DateFormat.LONG,
					 java.util.Locale.US).parse(value);

				} catch (java.text.ParseException e3) {
				    lexpires =
					java.text.DateFormat.getDateInstance
					(java.text.DateFormat.FULL,
					 java.util.Locale.US).parse(value);
				}
			    }
			}

		    } catch (java.text.ParseException e) {
			throw new java.lang.IllegalStateException
			    (LicenseStrings.getString
			     (LicenseStrings.BAD_LICENSE_FILE));
		    }
		}

	    } else if (parameter.equalsIgnoreCase
		       (LicenseStrings.getString(LicenseStrings.SIGNATURE))) {
		signatureStr = value;
		gotSignature = true;

	    } else {
		/* Otherwise, we've got an illegal license. */
		throw new java.lang.IllegalStateException
		    (LicenseStrings.getString
		     (LicenseStrings.BAD_LICENSE_FILE));
	    }
	}
    
	if (!gotSupport && !lversion.equalsIgnoreCase("V1.0Pre")) {
	    /* If there is no support entry, we reject the license. */
	    throw new java.lang.IllegalStateException
		(LicenseStrings.getString(LicenseStrings.BAD_SUPPORT_DATE));
	}
    
	if ((!gotSignature) ||
	    (signatureStr == null) ||
	    signatureStr.equals("")) {
	    /* If we didn't get a good signature, that's an error. */
	    throw new java.lang.IllegalStateException
		(LicenseStrings.getString(LicenseStrings.BAD_SIGNATURE));
	}
    
	return (signatureStr);
    }

    /**
     * Return the version number of the license file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the version number.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final String version() {
	return (lversion);
    }

    /**
     * Return the serial number of the license file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the serial number.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final long serialNumber() {
	return (lserial);
    }

    /**
     * Return the number of clients supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of clients.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final int numberOfClients() {
	return (lnclients);
    }

    /**
     * Return the support ends date.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the support ends date.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final java.util.Date support() {
	return (lsupport);
    }

    /**
     * Return the license expiration date.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the expiration date.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final java.util.Date expires() {
	return (lexpires);
    }

    /**
     * Are security features supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are security features supported?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean security() {
	return (lsecurity);
    }

    /**
     * Are routing features supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are routing features supported?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean routing() {
	return (lrouting);
    }

    /**
     * Are mirrors supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are mirrors supported?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean mirrors() {
	return (lmirrors);
    }

    /**
     * Are archives supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are archives supported?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean archives() {
	return (larchives);
    }

    /**
     * Are remote connections supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are remote connections supported?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean remoteConnections() {
	return (lremote);
    }

    /**
     * Should the serial number be checked?
     * <p>
     *
     * @author Ian Brown
     *
     * @return check the serial number?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean checkSerialNumber() {
	return (lcheck);
    }

    /**
     * Should the serial number be broadcast?
     * <p>
     *
     * @author Ian Brown
     *
     * @return broadcast the serial number?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final boolean broadcastSerialNumber() {
	return (lbroadcast);
    }

    /**
     * Check the signature against the file contents.
     * <p>
     * This method builds a signature for the input license file and compares
     * it to the input signature.
     *
     * @author Ian Brown
     *
     * @param bReadI     the buffered reader connected to the license file.
     * @param signatureI the signature to check.
     * @return is the signature valid?
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    private final boolean checkSignature(java.io.BufferedReader bReadI,
					 String signatureI)
    {
	try {
	    String line;
	    long checkSum = 99991,
		lineCount = 0;

	    /* Read lines from the license file and build the checksum. */
	    while ((line = bReadI.readLine()) != null) {

		if (line.charAt(0) == '#') {
		    /* Lines starting with '#' are ignored. */
		    continue;
		}

		int    theEquals = line.indexOf("=");

		if (theEquals == -1) {
		    throw new java.lang.IllegalStateException
			(LicenseStrings.getString
			 (LicenseStrings.BAD_LICENSE_FILE));
		}

		String parameter = line.substring(0,theEquals);
		++lineCount;

		/*
		 * It is the duty of another method to ensure that the contents
		 * of the file are good.
		 */
		if (parameter.equalsIgnoreCase
		    (LicenseStrings.getString(LicenseStrings.SIGNATURE))) {
		    // When we hit the signature line, we are done.
		    break;
		}

		/*
		 * Otherwise, build the checksum from the characters of this
		 * line. This checksum is simply a rotate the current value
		 * left one bit, add in the value of the next character, and
		 * scramble the result somewhat.
		*/
		for (int idx = 0; idx < line.length(); ++idx) {
		    if ((checkSum & 0x4000000000000000L) ==
			0x4000000000000000L) {
			checkSum = ((checkSum & 0x3fffffffffffffffL) << 1) + 1;
		    } else {
			checkSum <<= 1;
		    }
		    checkSum *= line.charAt(idx) * (lineCount*3 + 13);
		}
	    }
	    checkSum = Math.abs(checkSum);

	    /*
	     * Decode the input signature. For now, the encoding is just to
	     * convert it to a string, but we'll want something more complex
	     * later.
	     */
	    String decryptedSignature = signatureI;

	    /*
	     * Convert the decrypted signature to a long and compare to the
	     * checksum.
	     */
	    long dSig = Long.parseLong(decryptedSignature);

	    return (dSig == checkSum);

	} catch (java.lang.Exception e) {
	    return (false);
	}
    }

    /**
     * Returns the amount of time until the license expires.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time remaining.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final long timeUntilExpiration() {
	if (expires() == null) {
	    return (Long.MAX_VALUE);
	}
	return (expires().getTime() - (new java.util.Date()).getTime());
    }
}
