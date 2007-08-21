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
 * Extended <code>Rmap</code> that represents a set of connected
 * <code>Servers</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 06/20/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/23/2001  INB	Created.
 *
 */
class RoutingMapIO
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.RoutingMap, java.io.Serializable
{
    /**
     * the local <code>Server</code> name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/03/2001
     */
    private String localName = null;

    // Private constants:
    private final static byte PAR_LCL = 0;

    private final static String[] PARAMETERS = {
				    "LCL"
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
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    public RoutingMapIO() {
	super();
    }

    /**
     * Class constructor to build an <code>RoutingMap</code> by reading it
     * from an input stream.
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
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    RoutingMapIO(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build an <code>RoutingMap</code> by reading it
     * from an input stream.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>RoutingMapIO</code> as an
     *		     <code>Rmap</code>.
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
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2001  INB	Created.
     *
     */
    RoutingMapIO(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation returns the <code>RoutingMap's</code> local server
     * name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 12/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    final String additionalToString() {
	return ("  Local Server: " + getLocalName());
    }

    /**
     * Adds the <code>RoutingMapIO's</code> parameters to the full
     * serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    static synchronized String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = Rmap.addToParameters(null);
	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}
	return (addToParameters(parametersR,PARAMETERS));
    }

    /**
     * Clones this <code>RoutingMap</code>.
     * <p>
     * This method replaces any server-side objects with the appropriate
     * client-side objects.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 01/31/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
     *
     */
    public Object clone() {
	RoutingMapIO clonedR = new RoutingMapIO();

	try {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		if (getChildAt(idx) instanceof Server) {
		    Server serverIn = (Server) getChildAt(idx),
			serverOut = (Server) serverIn.newInstance();

		    if (serverIn.getNchildren() > 0) {
			serverOut.setChildren
			    ((RmapVector) serverIn.getChildren().clone());
			for (int idx1 = 0;
			     idx1 < serverOut.getNchildren();
			     ++idx1) {

			    Rmap entry = serverOut.getChildAt(idx1);
			    entry.setParent(serverOut);
			}
		    }

		    clonedR.addChild(serverOut);

		} else {
		    clonedR.addChild((Rmap) getChildAt(idx).clone());
		}
	    }

	} catch (com.rbnb.api.AddressException e) {
	    clonedR = null;

	} catch (com.rbnb.api.SerializeException e) {
	    clonedR = null;

	} catch (java.io.IOException e) {
	    clonedR = null;

	} catch (java.lang.InterruptedException e) {
	    clonedR = null;
	}

	return (clonedR);
    }

    /**
     * Defaults for all parameters.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code> into
     * this one. It is designed to be overridden by higher level objects to
     * ensure that they handle all of their parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param seenI  the fields that we've seen already.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultRoutingMapParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Default <code>RoutingMap</code> parameters.
     * <p>
     * This method fills in any fields not read from an input stream by copying
     * them from the input <code>Rmap</code>.
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>RoutingMap</code> as an
     *		     <code>Rmap</code>.
     * @param seenI  the fields seen.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    final void defaultRoutingMapParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (otherI != null) {
	    RoutingMap other = (RoutingMap) otherI;

	    if ((seenI == null) || !seenI[PAR_LCL]) {
		setLocalName(other.getLocalName());
	    }
	}
    }

    /**
     * Gets the local <code>Server</code> name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setLocalName(String)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    public final String getLocalName() {
	return (localName);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Creates a new instance of the same class as this <code>Rmap</code> (or a
     * similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    final Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RoutingMapIO rmapR = (RoutingMapIO) super.newInstance();

	rmapR.setLocalName(getLocalName());

	return (rmapR);
    }

    /**
     * Reads the <code>RoutingMap</code> from the specified input stream.
     * <p>
     * This method uses the input <code>Rmap</code> to fill in missing fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>RoutingMapIO</code> as an
     *		     <code>Rmap</code>.
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
     * @see #write(com.rbnb.api.Rmap,String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Rmap</code>.
	Serialize.readOpenBracket(isI);

	// Initialize the full parameter list.
	initializeParameters();

	boolean[] seen = new boolean[ALL_PARAMETERS.length];
	int parameter;
	while ((parameter = Serialize.readParameter(ALL_PARAMETERS,
						    isI)) != -1) {
	    seen[parameter] = true;

	    // Read parameters until we see a closing bracket.
	    if (!readRoutingMapParameter(parameter,isI,disI)) {
		readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads <code>RoutingMap</code> parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI  the parameter index.
     * @param isI	  the input stream.
     * @param disI	  the data input stream.
     * @return was the parameter recognized?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeRoutingMapParameters(com.rbnb.api.RoutingMapIO,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    final boolean readRoutingMapParameter(int parameterI,
				      InputStream isI,
				      DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean routingMapR = false;

	if (parameterI >= parametersStart) {
	    routingMapR = true;

	    switch (parameterI - parametersStart) {
	    case PAR_LCL:
		setLocalName(isI.readString());
		break;

	    default:
		routingMapR = false;
		break;
	    }
	}

	return (routingMapR);
    }

    /**
     * Sets the local <code>Server</code> name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name.
     * @see #getLocalName()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    public final void setLocalName(String nameI) {
	localName = nameI;
    }

    /**
     * Writes this <code>RoutingMap</code> to the specified stream.
     * <p>
     * This method writes out the differences between this
     * <code>RoutingMapIO</code> object and the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>RoutingMapIO</code> as an
     *			   <code>Rmap</code>.
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
     * @see #read(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 06/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    void write(Rmap otherI,
	       String[] parametersI,
	       int parameterI,
	       OutputStream osI,
	       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Write out the object.
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged(this,parametersI,parameterI);

	writeStandardParameters(otherI,osI,dosI);
	writeRoutingMapParameters((RoutingMapIO) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes out <code>RoutingMap</code> parameters.
     * <p>
     * This method writes out the differences between this
     * <code>RoutingMapIO</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>RoutingMapIO</code>.
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
     * @see #readRoutingMapParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2001  INB	Created.
     *
     */
    final void writeRoutingMapParameters(RoutingMapIO otherI,
					 OutputStream osI,
					 DataOutputStream dosI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	if ((getLocalName() != null) &&
	    ((otherI == null) ||
	     !getLocalName().equals(otherI.getLocalName()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_LCL);
	    osI.writeString(getLocalName());
	}
    }
}
