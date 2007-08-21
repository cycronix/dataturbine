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
 * Reverses a route (parent<->child or shortcut) by establishing the
 * connections from the primary side.
 * <p>
 * This command is used by the secondary side (the parent of a parent<->child
 * route or the destination of a shortcut) to establish a reverse connection to
 * the primary side in the face the primary side not refusing to accept a
 * connection (for example, when a firewall allows connections from inside to
 * outside, but not from outside to inside).
 * <p>
 * When the primary side receives this command, it establishes a control
 * connection from its end and then sends this command back to the secondary
 * side. The two sides then reverse roles.  The primary side replaces its
 * <code>Controller</code> with a <code>RouterHandler</code> and the secondary
 * side replaces its <code>ControllerHandler</code> with a
 * <code>Router</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/28/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 01/25/2002  INB	Created.
 *
 */
final class ReverseRoute
    extends com.rbnb.api.Command
{
    /**
     * was this message sent by the primary side?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/25/2002
     */
    private boolean fromPrimary = false;

    // Private constants:
    private final static byte PAR_PRM = 0;

    private final static String[] PARAMETERS = {
				    "PRM"
				};

    // Private class fields:
    private static String[] ALL_PARAMETERS = null;

    private static int parametersStart = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    public ReverseRoute() {
	super();
    }

    /**
     * Class constructor to build a <code>ReverseRoute</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
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
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    ReverseRoute(InputStream isI,DataInputStream disI)
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
     * Creates a <code>ReverseRoute</code> command for the specified
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the <code>Rmap</code> hierarchy to reverseRoute.
     * 
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    ReverseRoute(com.rbnb.api.Rmap rmapI) {
	this(rmapI,false);
    }

    /**
     * Creates a <code>ReverseRoute</code> command for the specified
     * <code>Rmap</code> with the specified from primary flag setting.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI	   the <code>Rmap</code> hierarchy to reverseRoute.
     * @param fromPrimaryI is this from the primary?
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    ReverseRoute(com.rbnb.api.Rmap rmapI,boolean fromPrimaryI) {
	super(rmapI);
	setFromPrimary(fromPrimaryI);
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation returns the fromPrimary registration flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final String additionalToString() {
	return (" fromPrimary? " + getFromPrimary());
    }

    /**
     * Adds the <code>ReverseRoute's</code> parameters to the full
     * serialization parameters list.
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
     * 01/25/2002  INB	Created.
     *
     */
    final static String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = Command.addToParameters(null);
	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}
	return (addToParameters(parametersR,PARAMETERS));
    }

    /**
     * Gets the fromPrimary registration flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return fromPrimary the existing registration?
     * @see #setFromPrimary(boolean)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final boolean getFromPrimary() {
	return (fromPrimary);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Reads the <code>ReverseRoute</code> from the specified input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI    the input stream.
     * @param disI   the data input stream.
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
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the
	// <code>ReverseRoute</code>.
	Serialize.readOpenBracket(isI);

	// Initialize the full parameter list.
	initializeParameters();

	boolean[] seen = new boolean[ALL_PARAMETERS.length];
	int parameter;
	while ((parameter = Serialize.readParameter(ALL_PARAMETERS,
						    isI)) != -1) {
	    seen[parameter] = true;

	    // Read parameters until we see a closing bracket.
	    if (!readReverseRouteParameter(parameter,isI,disI)) {
		readStandardParameter(parameter,isI,disI);
	    }
	}
    }

    /**
     * Reads <code>ReverseRoute</code> parameters.
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
     * @see #writeReverseRouteParameters(com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final boolean readReverseRouteParameter(int parameterI,
					InputStream isI,
					DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean reverseRouteR = false;

	if (parameterI >= parametersStart) {
	    reverseRouteR = true;

	    switch (parameterI - parametersStart) {
	    case PAR_PRM:
		setFromPrimary(isI.readBoolean());
		break;

	    default:
		reverseRouteR = false;
		break;
	    }
	}

	return (reverseRouteR);
    }

    /**
     * Sets the fromPrimary registration flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fromPrimaryI fromPrimary the existing registration?
     * @see #getFromPrimary()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final void setFromPrimary(boolean fromPrimaryI) {
	fromPrimary = fromPrimaryI;
    }

    /**
     * Writes this <code>ReverseRoute</code> to the specified stream.
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
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final void write(String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);
	writeStandardParameters(osI,dosI);
	writeReverseRouteParameters(osI,dosI);
	Serialize.writeCloseBracket(osI);
    }

    /** 
     * Writes out <code>ReverseRoute</code> parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI    the output stream.
     * @param dosI   the data output stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readReverseRouteParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final void writeReverseRouteParameters(OutputStream osI,
				       DataOutputStream dosI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Initialize the full parameter list.
	initializeParameters();

	osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_PRM);
	osI.writeBoolean(getFromPrimary());
    }
}
