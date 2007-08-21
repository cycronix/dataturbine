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
 * Exception transport class.
 * <p>
 * This class is used to transport exception messages from the DataTurbine to
 * the application.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/17/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/19/2001  INB	Created.
 *
 *
 */
final class ExceptionMessage
    extends com.rbnb.api.Serializable
{
    /**
     * The name of the exception class.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/19/2001
     */
    private String exceptionClass = null;

    /**
     * The exception detail message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/19/2001
     */
    private String detailMessage = null;

    // Private class fields:
    private static Class[] ARGS = null;

    // Private constants:
    private final static byte	PAR_EXC = 0,
				PAR_MSG = 1;

    private final static String[] PARAMETERS = {
				    "EXC",
				    "MSG"
				};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ExceptionMessage(java.lang.Exception)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    public ExceptionMessage() {
	super();
    }

    /**
     * Class constructor to build an <code>ExceptionMessage</code> from input
     * streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #ExceptionMessage()
     * @see #ExceptionMessage(java.lang.Exception)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    ExceptionMessage(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Class constructor to build an <code>ExceptionMessage</code> from an
     * <code>Exception</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param exceptionI  the exception.
     * @see #ExceptionMessage()
     * @see #ExceptionMessage(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    ExceptionMessage(Exception e) {
	setFromException(e);
    }

    /**
     * Clones this <code>ExceptionMessage</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 04/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2001  INB	Created.
     *
     */
    public Object clone() {
	return (super.clone());
    }

    /**
     * Gets the detail message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the detail message.
     * @see #setDmessage(String)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final String getDmessage() {
	return (detailMessage);
    }

    /**
     * Gets the exception class name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the exception class name.
     * @see #setEclass(String)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final String getEclass() {
	return (exceptionClass);
    }

    /**
     * Initialize the constructor arguments array.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/19/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final static synchronized void initializeArguments()
	throws java.lang.ClassNotFoundException
    {
	if (ARGS == null) {
	    ARGS = new Class[1];
	    ARGS[0] = Class.forName("java.lang.String");
	}
    }

    /**
     * Reads the <code>ExceptionMessage</code> from the specified input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 04/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	// Read the open bracket marking the start of the
	// <code>ExceptionMessage</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    // Read parameters until we see a closing bracket.

	    switch (parameter) {
	    case PAR_EXC:
		setEclass(isI.readUTF());

		break;

	    case PAR_MSG:
		setDmessage(isI.readUTF());
		break;
	    }
	}
    }

    /**
     * Sets the detail message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI  the new detail message.
     * @see #getDmessage()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final void setDmessage(String messageI) {
	detailMessage = messageI;
    }

    /**
     * Sets the exception class.
     * <p>
     *
     * @author Ian Brown
     *
     * @param classI  the name of the exception class.
     * @see #getEclass()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final void setEclass(String classI) {
	exceptionClass = classI;
    }

    /**
     * Sets up the <code>ExceptionMessage</code> from the input exception.
     * <p>
     *
     * @author Ian Brown
     *
     * @param exceptionI  the exception.
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    private final void setFromException(Exception e) {
	String cName = e.getClass().getName();

	// The class name from Java is often, but not always, represented as
	// "[L<classname>;".
	if (cName.indexOf("[L") == 0) {
	    setEclass(cName.substring(2,cName.length() - 1));
	} else {
	    setEclass(cName);
	}
	setDmessage(e.getMessage());
    }

    /**
     * Converts this <code>ExceptionMessage</code> to the appropriate
     * <code>Exception</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Exception</code>.
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    final Exception toException() {
	Exception exceptionR = null;

	try {
	    // What we have to do is to get the class that corresponds to the
	    // input name and create an instance of it that has the correct
	    // detail message. The first part (get the class) is easy.
	    Class eClass = Class.forName(getEclass());

	    // The second part isn't. We have to find the constructor that
	    // takes a detail message.
	    initializeArguments();
	    java.lang.reflect.Constructor constructor =
		eClass.getConstructor(ARGS);

	    // Now instantiate it.
	    Object[] args = { getDmessage() };
	    exceptionR = (Exception) constructor.newInstance(args);
		
	} catch (Exception e) {
	    exceptionR = new Exception(getDmessage());
	}

	return (exceptionR);
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 09/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2001  INB	Created.
     *
     */
    public final String toString() {
	String stringR = null;

	try {
	    throw toException();
	} catch (java.lang.Exception e) {
	    java.io.ByteArrayOutputStream baos =
		new java.io.ByteArrayOutputStream();
	    java.io.PrintWriter pw = new java.io.PrintWriter(baos);

	    // Write the exception out to the print writer stream.
	    e.printStackTrace(pw);
	    pw.flush();

	    // Convert the output to a string.
	    stringR = new String(baos.toByteArray());
	    pw.close();
	}

	return (stringR);
    }

    /**
     * Writes this <code>ExceptionMessage</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 04/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    final void write(String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws java.io.IOException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);

	if (getEclass() != null) {
	    osI.writeParameter(PARAMETERS,PAR_EXC);
	    osI.writeUTF(getEclass());
	}

	if (getDmessage() != null) {
	    osI.writeParameter(PARAMETERS,PAR_MSG);
	    osI.writeUTF(getDmessage());
	}

	Serialize.writeCloseBracket(osI);
    }
}
