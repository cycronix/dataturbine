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
 * Class used to mark part of an archive or the entire archive as having been
 * closed correctly.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/06/2002
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/04/2001  INB	Created.
 *
 */
final class Seal
    extends com.rbnb.api.Serializable
{
    /**
     * the date and time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/04/2001
     */
    private long asOf = System.currentTimeMillis();

    // Private constants:
    private final static byte PAR_AOF = 0;

    private final static String[] PARAMETERS = {
				    "AOF"
				};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    Seal() {
	super();
    }

    /**
     * Class constructor to build a <code>Seal</code> by reading it in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the input stream.
     * @param disI the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    Seal(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Does the seal file for the input directory exist?
     * <p>
     *
     * @author Ian Brown
     *
     * @param directoryI the directory to check.
     * @return seal file exists?
     * @since V2.0
     * @version 12/06/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2001  INB	Created.
     *
     */
    static final boolean exists(String directoryI) {
	boolean existsR = false;

	try {
	    java.io.File sealFile = new java.io.File
		(directoryI + Archive.SEPARATOR + "seal.rbn");

	    existsR = sealFile.exists();
	} catch (java.lang.Exception e) {
	}

	return (existsR);
    }

    /**
     * Gets the time of this <code>Seal</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time in milliseconds since 1970.
     * @see #setAsOf(long)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final long getAsOf() {
	return (asOf);
    }

    /**
     * Reads the object from an input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Ask</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_AOF:
		setAsOf(isI.readLong());
		break;
	    }
	}
    }

    /**
     * Seals the specified directory.
     * <p>
     *
     * @author Ian Brown
     *
     * @param directoryI the directory to seal.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #validate(String,long,long)
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final static void seal(String directoryI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Seal seal = new Seal();

	java.io.FileOutputStream sfos = new java.io.FileOutputStream
	    (directoryI + Archive.SEPARATOR + "seal.rbn");
	OutputStream sos = new OutputStream(sfos,
					    Archive.TEXTARCHIVE,
					    0);
	BuildFile.loadBuildFile(sos);
	Language.write(seal,null,sos,null);
	sos.close();
	sfos.close();
    }

    /**
     * Sets the time that this <code>Seal</code> was created.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI the time in milliseconds since 1970.
     * @see #getAsOf()
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final void setAsOf(long timeI) {
	asOf = timeI;
    }

    /**
     * Returns a string representation of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/05/2001  INB	Created.
     *
     */
    public final String toString() {
	return ("Seal: " + (new java.util.Date(getAsOf())));
    }

    /**
     * Validates this <code>Seal</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param afterI     the minimum valid time.
     * @param beforeI    the maximum valid time.
     * @exception com.rbnb.api.InvalidSealException
     *		  thrown if the <code>Seal</code> found does not have a valid
     *		  time. The <code>Seal</code> and the problem are saved in the
     *		  exception.
     * @see #seal(String)
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/05/2001  INB	Created.
     *
     */
    final void validate(long afterI,long beforeI)
	throws com.rbnb.api.InvalidSealException
    {
	if ((getAsOf() < afterI) || (getAsOf() > beforeI)) {
	    throw new com.rbnb.api.InvalidSealException
		(this,
		 afterI,
		 beforeI);
	}
    }

    /**
     * Validates a <code>Seal</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param directoryI the directory containing the <code>Seal</code>.
     * @param afterI     the minimum valid time.
     * @param beforeI    the maximum valid time.
     * @return the <code>Seal</code> or null if none can be found.
     * @exception com.rbnb.api.InvalidSealException
     *		  thrown if the <code>Seal</code> found does not have a valid
     *		  time. The <code>Seal</code> and the problem are saved in the
     *		  exception.
     * @see #seal(String)
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final static Seal validate(String directoryI,long afterI,long beforeI)
	throws com.rbnb.api.InvalidSealException
    {
	// Any error indicates an invalid <code>Seal</code>.
	Seal validR = null;
//EMF 11/7/06: set seal as invalid for all but FS
//             objective is more robust archive loading
//String name=directoryI.substring(directoryI.lastIndexOf(Archive.SEPARATOR)+1);
//if (!name.startsWith("FS")) {
//	System.err.println("Seal.validate: name "+name+", returning null");
//        return validR;
//}

	try {

	    // Read the <code>Seal</code> from a <code>Seal</code> file.
	    java.io.FileInputStream sfis = new java.io.FileInputStream
		(directoryI + Archive.SEPARATOR + "seal.rbn");
	    InputStream sis = new InputStream(sfis,
					      Archive.TEXTARCHIVE,
					      0);
	    Seal check = (Seal) Language.read(null,sis,null);
	    sis.close();
	    sfis.close();

	    // Validate the <code>Seal</code>.
	    check.validate(afterI,beforeI);
	    validR = check;

	} catch (com.rbnb.api.InvalidSealException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	}
//System.err.println("Seal.validate returning "+validR+" for "+directoryI);
	return (validR);
    }

    /**
     * Writes the object to an output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the object's parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final void write(String[] parametersI,
			int parameterI,
			OutputStream osI,
			DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);

	osI.writeParameter(PARAMETERS,PAR_AOF);
	osI.writeLong(getAsOf());
	Serialize.writeCloseBracket(osI);
    }
}

