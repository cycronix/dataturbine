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
 * Build file loader class.
 * <p>
 * This class contains a static method for loading the build file into a class
 * that implements the <code>BuildInterface</code>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.BuildInterface
 * @since V2.0
 * @version 10/05/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/18/2010  MJM  explicit buffer size in BufferedReader 
 * 10/05/2004  JPW	In loadBuildFile(), add preprocessor directives which
 *			will be used by "sed" to create a version of the code
 *			appropriate for compiling under J#.
 * 08/04/2004  JPW      Change in loadBuildFile(): if search over classpath
 *                      doesn't yield anything, then just check in the local
 *                      directory for rbnbBuild.txt
 * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
 * 03/19/2003  INB	Added code to handle the case where we can't get the
 *			system class loader.
 * 12/20/2001  INB	Created.
 *
 */
public final class BuildFile {

    /**
     * Loads the build file.
     * <p>
     * The build file contains various information that specifies things like
     * when the class files were built.
     * <p>
     *
     * @author Ian Brown
     *
     * @param biI the class to store the loaded values in.
     * @since V2.0
     * @version 10/05/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/05/2004  JPW	Add preprocessor directives which will be used by
     *                  "sed" to create a version of the code appropriate for
     *                  compiling under J#.  Under J#, we don't want to include
     *			the call to getSystemClassLoader().
     * 08/04/2004  JPW  If search over classpath doesn't yield anything,
     *                  just check in the local directory for rbnbBuild.txt
     * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
     * 03/19/2003  INB	Added code to handle the case where we cannot get the
     *			system class loader.
     * 10/24/2001  INB	Created.
     *
     */
    public final static void loadBuildFile(BuildInterface biI) {
	try {
	    // See if we can find the build file using the class loader.
	    java.io.InputStream inStr = null;
	    ClassLoader cl = null;
	    try {
		cl = biI.getClass().getClassLoader();
		if (cl == null) {
		    // JPW 10/05/2004: Add preprocessor directives which will
		    //                 be used by "sed" to create a version of
		    //                 the code appropriate for compiling under
		    //                 J#.  Under J#, we don't want to include
		    //                 the call to getSystemClassLoader().
		    //#open_java2_comment#
		    cl = ClassLoader.getSystemClassLoader();
		    //#close_java2_comment#
		}
	    } catch (java.lang.NoClassDefFoundError e) {
	    } catch (java.lang.NoSuchMethodError e) {
	    }

	    if (cl != null) {
		inStr = cl.getResourceAsStream("rbnbBuild.txt");
	    }

	    if (inStr == null) {
		String classpath = System.getProperty("java.class.path");
		// JPW 08/04/2004: Make sure classpath isn't null
		if (classpath == null) {
		    classpath = new String("");
		}
		// JPW 07/29/2004: INB wondered if ";" should be
		//                 added to tokenizer
		java.util.StringTokenizer st =
		    new java.util.StringTokenizer(classpath,":");
		String path;
		java.io.File file;
		
		while (st.hasMoreTokens()) {
		    path = st.nextToken();
		    file = new java.io.File(path,"/rbnbBuild.txt");
		    if (file.exists()) {
			inStr = new java.io.FileInputStream(file);
			break;
		    }
		}
		// JPW 08/04/2004: Looking over classpath didn't work; check
		//                 in local directory
		if (inStr == null) {
		    file = new java.io.File(".","/rbnbBuild.txt");
		    if (file.exists()) {
			inStr = new java.io.FileInputStream(file);
		    }
		}
	    }

	    if (inStr != null) {
		// If we found a file, then we need to try to read it.
		java.io.InputStreamReader inStrReader =
		    new java.io.InputStreamReader(inStr);
		java.io.BufferedReader bRead =
 		    new java.io.BufferedReader(inStrReader,8192);  // MJM
		String line;

		while ((line = bRead.readLine()) != null) {
		    int idx;

		    if ((line.length() == 0) ||
			(line.charAt(0) == '#')) {
			// Ignore blank lines and lines starting with the '#'
			// character.
			continue;

		    } else if ((idx = line.indexOf("=")) == -1) {
			throw new java.lang.Exception
			    ("Bad line: " + line);

		    } else if (line.substring
			       (0,idx).equalsIgnoreCase("VERSION")) {
			biI.setBuildVersion(line.substring(idx + 1));

		    } else if (line.substring
			       (0,idx).equalsIgnoreCase("BUILT")) {
			String value = line.substring(idx + 1);
			biI.setBuildDate
			    ((new java.text.SimpleDateFormat
				("MMM dd yyyy HH:mm:ss z",  
				 java.util.Locale.US)).parse(value));
		    }
		}

		if (inStr instanceof java.io.FileInputStream) {
		    inStr.close();
		}
	    }

	} catch (java.lang.Exception e) {
	    // Ignore errors. There are defaults for the values in the
	    // file. They may cause problems with the license file, but that is
	    // OK.

	} catch (java.lang.Error e) {
	    // Ignore errors. There are defaults for the values in the
	    // file. They may cause problems with the license file, but that is
	    // OK.
	}
    }
}
