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
 * Ask the RBNB server for information (usually).
 * <p>
 * Information that can be requested includes:
 * <p><ul>
 *    <li><code>CHILDAT</code> - requests information about a specific child.
 *				 This is not used and could be removed,</li>
 *    <li><code>ISRUNNING</code> - requests the run status of a specific
 *				   <code>Client</code>,</li>
 *    <li><code>REGISTERED</code> - requests matching registration
 *				    information,</li>
 *    <li><code>REQUESTAT</code> - initiates a specific request out of an NBO's
 *				   ring buffer (see
 *				   <code>NBO.initiateRequestAt</code>),</li>
 *    <li><code>ROUTEFROM</code> - requests that a route be initiated from the
 *				   local server.</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Client
 * @see com.rbnb.api.NBO#initiateRequestAt(int indexI)
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 08/13/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/13/2004  INB	Added documentation.
 * 07/30/2004  INB	In order to support communications between different
 *			JVMs that might disagree about how to serialize Java
 *			objects, added a new internal serialization capability.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 01/16/2001  INB	Created.
 *
 */
public final class Ask
    extends com.rbnb.api.Serializable
{
    /**
     * Ask for a child at a particular index.
     * <p>
     * The additional arguments are:
     * <p><ol>
     * <li><code>java.lang.Integer</code> - the index of the child.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    public final static String CHILDAT = "CA";

    /**
     * Ask if something is running.
     * <p>
     * The additional arguments are:
     * <p><ol>
     * <li><code>com.rbnb.api.Client</code> - the client to check.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    public final static String ISRUNNING = "IR";

    /**
     * Ask for registration information.
     * <p>
     * The additional arguments are:
     * <p><ol>
     * <li><code>com.rbnb.api.Rmap</code> - the <code>Rmap</code> hierarchy to
     *     match.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    public final static String REGISTERED = "RG";

    /**
     * Request for the matches to the child at a particular index.
     * <p>
     * The additional arguments are:
     * <p><ol>
     * <li><code>java.lang.Integer</code> - the index of the request.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    public final static String REQUESTAT = "RA";

    /**
     * Route from another <bold>RBNB</bold> server.
     * <p>
     * The additional arguments are:
     * <p><ol>
     * <li><code>java.lang.String</code> - the type of connection:
     *     <p><ul>
     *     <li>"PARENT" - accept a connection from a parent server,</li>
     *     <li>"CHILD" - accept a connection from a child server,</li>
     *     <li>"PEER" - accept a connecion from a peer server.</li>
     *     </ul><p>
     * </li>
     * <li><code>com.rbnb.api.Rmap</code> - the <code>Rmap</code> hierarchy
     *     representing the other <bold>RBNB</code>. The hierarchy depends on
     *     the type of connection:
     *     <p><ul>
     *     <li>type = "PARENT" or type = "PEER" - the hierarchy consists of a
     *         <code>RoutingMap</code> and <code>Servers</code>, or</li>
     *     <li>type = "CHILD" - the hierarchy consists of a single
     *         <code>Server</code>.</li>
     *     </ul><p>
     * </li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    public final static String ROUTEFROM = "RF";

    /*
     * the type of information desired.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private String informationType = null;

    /**
     * additional parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private java.util.Vector additional = null;

    // Private constants:
    private final static byte	IOS_DBL = 0,
				IOS_FLT = 1,
				IOS_INT = 2,
				IOS_LNG = 3,
				IOS_RBN = 4,
				IOS_SHR = 5,
				IOS_STR = 6,
				PAR_ADT = 0,
				PAR_INF = 1,
				PAR_IOS = 2;

    private final static String[] IOS_OBJECTS = {
				    "DBL",
				    "FLT",
				    "INT",
				    "LNG",
				    "RBN",
				    "SHR",
				    "STR"
				};
	

    private final static String[] PARAMETERS = {
				    "ADT",
				    "INF",
				    "IOS"
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
     * 01/16/2001  INB	Created.
     *
     */
    Ask() {
	super();
    }

    /**
     * Class constructor to build a <code>Ask</code> object from the specified
     * input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
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
     * 01/16/2001  INB	Created.
     *
     */
    Ask(InputStream isI,DataInputStream disI)
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
     * Class constructor to build an <code>Ask</code> object with the input
     * information type.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationTypeI  the type of information desired.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2001  INB	Created.
     *
     */
    Ask(String informationTypeI) {
	setInformationType(informationTypeI);
    }

    /**
     * Class constructor to build an <code>Ask</code> object with the input
     * information type and additional parameters vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationTypeI  the type of information desired.
     * @param additionalI	additional parameters.
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
    Ask(String informationTypeI,java.util.Vector additionalI) {
	this(informationTypeI);
	setAdditional(additionalI);
    }

    /**
     * Class constructor to build an <code>Ask</code> object with the input
     * information type and an additional <code>Object</code> parameter.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationTypeI  the type of information desired.
     * @param objectI		the additional object parameter.
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
    Ask(String informationTypeI,Object objectI) {
	this(informationTypeI);
	addAdditional(objectI);
    }

    /**
     * Adds an additional parameter.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI  the additional <code>Object</code>
     *			      parameter.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 04/24/2001 INB	Created.
     *
     */
    final void addAdditional(Object objectI) {
	if (!(objectI instanceof Serializable) &&
	    !(objectI instanceof Number) &&
	    !(objectI instanceof String)) {
	    throw new java.lang.IllegalArgumentException
		(objectI + " is not a valid type for an additional argument.");
	}
	if (getAdditional() == null) {
	    setAdditional(new java.util.Vector());
	}
	getAdditional().addElement(objectI);
    }

    /**
     * Clones this <code>Ask</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 05/09/2001
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
	Ask clonedR = (Ask) super.clone();

	if (clonedR != null) {
	    if (getAdditional() != null) {
		clonedR.setAdditional(new java.util.Vector());
		for (int idx = 0; idx < getAdditional().size(); ++idx) {
		    Object object = getAdditional().elementAt(idx);

		    if (object instanceof Serializable) {
			clonedR.addAdditional(((Serializable) object).clone());
		    } else if ((object instanceof Number) ||
			       (object instanceof String)) {
			clonedR.addAdditional(object);
		    }
		}
	    }
	}

	return (clonedR);
    }

    /**
     * Gets the additional information.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information <code>Vector</code>.
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/23/2001  INB	Created.
     *
     */
    public final java.util.Vector getAdditional() {
	return (additional);
    }

    /**
     * Gets the type of information desired.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the type of information desired.
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2001  INB	Created.
     *
     */
    public final String getInformationType() {
	return (informationType);
    }

    /**
     * Reads the <code>Ask</code> from the specified input stream.
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
     * @version 07/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2004  INB	In order to support communications between different
     *			JVMs that might disagree about how to serialize Java
     *			objects, added a new internal serialization capability.
     * 01/16/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Ask</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	java.util.Vector additional = new java.util.Vector();
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_ADT:
		// This version uses Java object serialization, which is not
		// always compatible between JVMs.  See PAR_IOS.
		Serialize.readOpenBracket(isI);
		Object object;
		while ((object = Language.readObject(null,isI,disI)) != null) {
		    additional.addElement(object);
		}
		setAdditional(additional);
		break;

	    case PAR_INF:
		setInformationType(isI.readUTF());
		break;

	    case PAR_IOS:
		// This version uses internal serialization, which will always
		// work.  Each element consists of an object type (one of the
		// Java numeric types, Java string, or RBNB serializable), and
		// the object's value.
		Serialize.readOpenBracket(isI);
		int oType;
		while ((oType =
			Serialize.readParameter(IOS_OBJECTS,isI)) != -1) {
		    switch (oType) {
		    case IOS_DBL:
			additional.addElement(new Double(isI.readDouble()));
			break;

		    case IOS_FLT:
			additional.addElement(new Float(isI.readFloat()));
			break;
			    
		    case IOS_INT:
			additional.addElement(new Integer(isI.readInt()));
			break;

		    case IOS_LNG:
			additional.addElement(new Long(isI.readLong()));
			break;

		    case IOS_RBN:
			additional.addElement(Language.readObject(null,
								  isI,
								  disI));
			break;

		    case IOS_STR:
			additional.addElement(isI.readUTF());
			break;

		    case IOS_SHR:
			additional.addElement(new Short(isI.readShort()));
			break;
		    }
		}
		setAdditional(additional);
		break;
	    }
	}
    }

    /**
     * Sets the additional information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param additionalI  the <code>Vector</code> of additional information.
     * @see #getAdditional()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/23/2001  INB	Created.
     *
     */
    final void setAdditional(java.util.Vector additionalI) {
	additional = additionalI;
    }

    /**
     * Sets the type of information desired.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationTypeI  the type of information desired.
     * @see #getInformationType()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2001  INB	Created.
     *
     */
    final void setInformationType
	(String informationTypeI)
    {
	informationType = informationTypeI;
    }

    /**
     * Writes this <code>Ask</code> to the specified stream.
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
     * @version 07/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2004  INB	In order to support communications between different
     *			JVMs that might disagree about how to serialize Java
     *			objects, added a new internal serialization capability.
     * 04/24/2001  INB	Created.
     *
     */
    final void write(String[] parametersI,
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

	if (getAdditional() != null) {
	    Object object;

	    if (IsSupported.isSupported
		(IsSupported.FEATURE_ASK_NO_JAVA_SERIALIZE,
		 osI.getBuildVersion(),
		 osI.getBuildDate())) {
		// Don't use Java serialization if the other side understands
		// the internal serialization capability.
		osI.writeParameter(PARAMETERS,PAR_IOS);
		Serialize.writeOpenBracket(osI);

		// Write out each object as a type indicator (a Java numeric
		// type, a Java string, or an RBNB serializable object), and
		// the value.
		for (int idx = 0; idx < getAdditional().size(); ++idx) {
		    object = getAdditional().elementAt(idx);
		    if (object instanceof com.rbnb.api.Serializable) {
			osI.writeParameter(IOS_OBJECTS,IOS_RBN);
			Language.writeObject(object,null,osI,dosI);

		    } else if (object instanceof Double) {
			osI.writeParameter(IOS_OBJECTS,IOS_DBL);
			osI.writeDouble(((Double) object).doubleValue());

		    } else if (object instanceof Float) {
			osI.writeParameter(IOS_OBJECTS,IOS_FLT);
			osI.writeFloat(((Float) object).floatValue());

		    } else if (object instanceof Integer) {
			osI.writeParameter(IOS_OBJECTS,IOS_INT);
			osI.writeInt(((Integer) object).intValue());

		    } else if (object instanceof Long) {
			osI.writeParameter(IOS_OBJECTS,IOS_LNG);
			osI.writeLong(((Long) object).longValue());

		    } else if (object instanceof Short) {
			osI.writeParameter(IOS_OBJECTS,IOS_SHR);
			osI.writeShort(((Short) object).shortValue());

		    } else if (object instanceof String) {
			osI.writeParameter(IOS_OBJECTS,IOS_STR);
			osI.writeUTF((String) object);

		    } else {
			throw new com.rbnb.api.SerializeException
			    ("Cannot serialize " + object +
			     "; it is not a type supported for internal " +
			     "serialization.");
		    }
		}

		Serialize.writeCloseBracket(osI);

	    } else {
		// If the other side only understands Java serialization, then
		// I guess we'll just have to use it.
		osI.writeParameter(PARAMETERS,PAR_ADT);
		Serialize.writeOpenBracket(osI);
		for (int idx = 0; idx < getAdditional().size(); ++idx) {
		    object = getAdditional().elementAt(idx);
		    Language.writeObject(object,null,osI,dosI);
		}
		Serialize.writeCloseBracket(osI);
	    }
	}

	osI.writeParameter(PARAMETERS,PAR_INF);
	osI.writeUTF(getInformationType());
	Serialize.writeCloseBracket(osI);
    }

    /**
     * Gets a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final String toString() {
	return ("Ask: " + getInformationType() + " " + getAdditional());
    }
}

