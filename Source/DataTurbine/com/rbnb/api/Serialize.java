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
 * General-purpose methods for serialization of RBNB object.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/11/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/08/2001  INB	Created.
 *
 *
 */
class Serialize {

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    Serialize() {
	super();
    }

    /**
     * Reads the open bracket marking the start of an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the open bracket is not seen.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @see #readParameter(String[],com.rbnb.api.InputStream)
     * @see #writeOpenBracket(com.rbnb.api.OutputStream)
     * @since V2.0
     * @version 07/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final static void readOpenBracket(InputStream isI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	if (isI.readChar() != '{') {
	    throw new com.rbnb.api.SerializeException
		("The open bracket ({) that should mark the start of the " +
		 "object was not seen.");
	}
    }

    /**
     * Reads the parameters of an RBNB object.
     * <p>
     * An object starts with an open bracket and ends with a close
     * bracket. Between the brackets are the parameters as specified by the
     * input list of valid names.
     * <p>
     * This method assumes that the open bracket has been seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the object parameters.
     * @param isI	   the input stream.
     * @return the index of the parameter read or -1 if the closing bracket is
     *	       seen.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if an unrecognizable parameter is seen.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @see #readOpenBracket(com.rbnb.api.InputStream)
     * @since V2.0
     * @version 03/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final static int readParameter(String[] parametersI,InputStream isI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	int parameterR = -1;
	isI.mark(8);
	char value;

	if (isI.getBinary()) {
	    value = isI.readChar();
	    if (Thread.currentThread().interrupted()) {
		Thread.currentThread().interrupt();
	    }

	} else {
	    do {
		value = isI.readChar();
		if (Thread.currentThread().interrupted()) {
		    Thread.currentThread().interrupt();
		}
	    } while (Character.isWhitespace(value));
	}

	if (value != '}') {
	    if  (parametersI == null) {
		throw new com.rbnb.api.SerializeException
		    ("No parameters are accepted.");
	    }

	    // If the next value on the input stream is not a closing bracket,
	    // then read it as a parameter.
	    isI.reset();

	    if ((parameterR = isI.readParameter(parametersI)) == -1) {
		throw new com.rbnb.api.SerializeException
		    ("Unrecognizable parameter read from input stream.\n" +
		     isI.getInfoMessage());
	    }
	}

	return (parameterR);
    }

    /**
     * Writes the closing bracket marking the end of an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI  the output stream.
     * @exception java.io.IOException
     *		  thrown if there is an error writing the output stream.
     * @see #readParameter(String[],com.rbnb.api.InputStream)
     * @see #writeOpenBracket(com.rbnb.api.OutputStream)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final static void writeCloseBracket(OutputStream osI)
	throws java.io.IOException
    {
	osI.writeChar('}');
    }

    /**
     * Writes the opening bracket marking the end of an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI  the output stream.
     * @exception java.io.IOException
     *		  thrown if there is an error writing the output stream.
     * @see #readOpenBracket(com.rbnb.api.InputStream)
     * @see #writeCloseBracket(com.rbnb.api.OutputStream)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    final static void writeOpenBracket(OutputStream osI)
	throws java.io.IOException
    {
	osI.writeChar('{');
    }
}
