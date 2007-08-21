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
 * Command an object.
 * <p>
 * The objects that can be commanded include:
 * <p><ul>
 * <li><code>ClientInterfaces</code> such as <code>Sources</code>,</li>
 * <li><code>ServerInterfaces</code>, and</li>
 * <li><code>RouteInterfaces</code>.</li>
 * </ul><p>
 * In addition, <code>Rmap</code> can be sent as part of a command to the * client handler.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/28/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 05/02/2001  INB	Created.
 *
 */
abstract class Command
    extends com.rbnb.api.Serializable
{
    /**
     * The object to command.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private Serializable object = null;

    // Class constants:
    private final static int PAR_OBJ = 0;

    private final static String[] PARAMETERS = {
			    "OBJ",
			};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    public Command() {
	super();
    }

    /**
     * Class constructor to build a <code>Command</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    Command(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Creates a <code>Command</code> command for the specified
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>ClientInterface</code> to command.
     * 
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    Command(com.rbnb.api.ClientInterface clientI) {
	this();
	setObject((Serializable) clientI);
    }

    /**
     * Creates a <code>Command</code> command for the specified
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code> to pass as part of the command.
     * 
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    Command(com.rbnb.api.Rmap rmapI) {
	this();
	setObject(rmapI);
    }

    /**
     * Creates a <code>Command</code> command for the specified
     * <code>ServerInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>ServerInterface</code> to command.
     * 
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    Command(com.rbnb.api.ServerInterface serverI) {
	this();
	try {
	    setObject((Serializable) serverI);
	} catch (java.lang.Exception e) {
	    throw new java.lang.InternalError();
	}
    }

    /**
     * Creates a <code>Command</code> command for the specified
     * <code>ServerInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>ServerInterface</code> to command.
     * 
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    Command(com.rbnb.api.ShortcutInterface shortcutI) {
	this();
	try {
	    setObject((Serializable) shortcutI);
	} catch (java.lang.Exception e) {
	    throw new java.lang.InternalError();
	}
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation returns "".
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    String additionalToString() {
	return ("");
    }

    /**
     * Adds the <code>Command's</code> parameters to the full serialization
     * parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 01/21/2002  INB	Created.
     *
     */
    static String[] addToParameters
	(String[] parametersI)
    {
	return (addToParameters(parametersI,PARAMETERS));
    }

    /**
     * Adds the input parameters to the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param allParametersI the serialization parameters list so far.
     * @param parametersI    the list of parameters to add.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 01/21/2002  INB	Created.
     *
     */
    final static String[] addToParameters
	(String[] allParametersI,
	 String[] parametersI) {
	int startAt = 0;
	String[] parametersR;

	if (allParametersI == null) {
	    parametersR = new String[parametersI.length];
	} else {
	    parametersR = new String[allParametersI.length +
				    parametersI.length];

	    System.arraycopy
		(allParametersI,
		 0,
		 parametersR,
		 0,
		 allParametersI.length);
	    startAt = allParametersI.length;
	}

	System.arraycopy(parametersI,
			 0,
			 parametersR,
			 startAt,
			 parametersI.length);

	return (parametersR);
    }

    /**
     * Clones this <code>Command</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 05/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/16/2001  INB	Created.
     *
     */
    public Object clone() {
	Command clonedR = (Command) super.clone();

	if (getObject() != null) {
	    clonedR.setObject((Serializable) getObject().clone());
	}

	return (clonedR);
    }

    /**
     * Gets the object to command.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Serializable</code> object.
     * @see #setObject(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    final Serializable getObject() {
	return (object);
    }

    /**
     * Reads the <code>Command</code> from the specified input stream.
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
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the command of the
	// <code>Command</code>. 
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    readStandardParameter(parameter,isI,disI);
	}
    }

    /**
     * Reads standard parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI  the parameter index.
     * @param isI	  the input stream.
     * @param disI	  the data input stream.
     * @return was the parameter recognized?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeStandardParameters(com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    final boolean readStandardParameter(int parameterI,
					InputStream isI,
					DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean standardR = true;

	switch (parameterI) {
	case PAR_OBJ:
	    setObject(Language.read(null,isI,disI));
	    break;

	default:
	    standardR = false;
	    break;
	}

	return (standardR);
    }

    /**
     * Reads the data for this <code>Command</code> and its children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeData(com.rbnb.api.DataOutputStream)
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
    final void readData(DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getObject() instanceof Rmap) {
	    ((Rmap) getObject()).readData(disI);
	}
    }

    /**
     * Sets the object to command.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI  the <code>Serializable</code> object.
     * @see #getObject()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    final void setObject(Serializable objectI) {
	object = objectI;
    }

    /**
     * Returns a string representation of this <code>Command</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    public final String toString() {
	String className = getClass().toString();
	className = className.substring
	    (className.lastIndexOf(".") + 1);

	return (className + " " + getObject() + additionalToString());
    }

    /**
     * Writes this <code>Command</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    void write(String[] parametersI,
	       int parameterI,
	       OutputStream osI,
	       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);
	writeStandardParameters(osI,dosI);
	Serialize.writeCloseBracket(osI);
    }

    /**
     * Writes out standard parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI    the output stream.
     * @param dosI   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readStandardParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/21/2002  INB	Created.
     *
     */
    final void writeStandardParameters(OutputStream osI,
				       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getObject() != null) {
	    osI.writeParameter(PARAMETERS,PAR_OBJ);
	    Language.write(getObject(),null,osI,dosI);
	}
    }

    /**
     * Writes the data for this <code>Command</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dosI the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #readData(com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    final void writeData(DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getObject() instanceof Rmap) {
	    ((Rmap) getObject()).writeData(dosI);
	}
    }
}
