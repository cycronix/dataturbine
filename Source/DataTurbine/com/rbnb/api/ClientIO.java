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
 * Object that represents a client application connection to an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/17/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/17/2003  INB	Added <code>tryReconnect</code> method.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 05/09/2001  INB	Created.
 *
 */
abstract class ClientIO
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.Client
{
    /**
     * the client connection type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/01/2001
     */
    private byte type = CLIENT;

    /**
     * the remote identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/01/2001
     */
    private String remoteID = null;

    /**
     * the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private Username username = null;

    // Private constants:
    private final static byte PAR_RID = 0;
    private final static byte PAR_TYP = 1;

    private final static String[] PARAMETERS = {
	"RID",
	"TYP",
    };

    private final static String[] TYPES = {
	"Client",
	"Mirror",
	"PlugIn"
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
     * 05/09/2001  INB	Created.
     *
     */
    ClientIO() {
	super();
    }

    /**
     * Class constructor to build a <code>ClientIO</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name.
     * @since V2.0
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    ClientIO(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This method calls the static <code>ClientIO</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
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
    String additionalToString() {
	return (additionalToString(this));
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This static method provides a single method for handling
     * <code>ClientInterface</code> implementations. It adds:
     * <p><ul>
     * <li>the desired number of cache frames,</li>
     * <li>the desired maximum size of the cache in bytes,</li>
     * <li>the desired number of cache <code>FrameSets</code>,</li>
     * <li>the archive access mode,</li>
     * <li>the desired maximum number of archive frames, and</li>
     * <li>the desired maximum size of the archive in bytes.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientInterface</code>.
     * @return the additional information.
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/25/2001  INB	Created.
     *
     */
    static String additionalToString(ClientInterface clientI) {
	String stringR = "";

	if (clientI.getType() != CLIENT) {
	    stringR += " " + TYPES[clientI.getType()];
	}
	if (clientI.getRemoteID() != null) {
	    stringR += " (Remote: " + clientI.getRemoteID() + ")";
	}
	if (clientI.getUsername() != null) {
	    stringR += " " + clientI.getUsername();
	}

	return (stringR);
    }

    /**
     * Adds the <code>ClientIO's</code> parameters to the full serialization
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
     * 06/01/2001  INB	Created.
     *
     */
    static String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = Rmap.addToParameters(null);
	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}
	return (addToParameters(parametersR,PARAMETERS));
    }

    /**
     * Copies some standard fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Rmap</code> to copy.
     * @since V2.0
     * @version 03/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2002  INB	Created.
     *
     */
    void copyFields(Rmap clientI) {
	ClientInterface client = (ClientInterface) clientI;
	setType(client.getType());
	setRemoteID(client.getRemoteID());
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
     * @version 02/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2002  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultClientParameters(this,(ClientInterface) otherI,seenI);
    }

    /**
     * Defaults for client parameters.
     * <p>
     * This method copies unread fields from the other
     * <code>ClientInterface</code> into the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ciIO   the <code>ClientInterface</code>.
     * @param otherI the other <code>ClientInterface</code>.
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
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    final static void defaultClientParameters(ClientInterface ciIO,
					      ClientInterface otherI,
					      boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (otherI != null) {
	    if ((seenI == null) || !seenI[parametersStart + PAR_RID]) {
		ciIO.setRemoteID(otherI.getRemoteID());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_TYP]) {
		ciIO.setType(otherI.getType());
	    }

	    Rmap ci = (Rmap) ciIO,
		other = (Rmap) otherI;
	    ci.defaultStandardParameters(other,seenI);
	}
    }

    /**
     * Gets the list of registered <code>Rmaps</code> matching the requested
     * <code>Rmap</code> hierarchy.
     * <p>
     * At this time the following <code>Rmap</code> hierarchies are
     * implemented:
     * <p><ul>
     *	  <li>{unnamed}[/...]</code>,</li>
     *    <li>[{unnamed}/]servername[/...],</li>
     *    <li>[{unnamed}/]servername/clientname[/...],</li>
     *    <li>[{unnamed}/]servername/clientname1[/....],/clientname2[/...],
     *	      ...</li>
     * </ul><p>
     * Syntax notes:
     * <br>/ indicates a child <code>Rmap</code>.
     * <br>[/...] indicates an optional child <code>Rmap</code> with a name of
     *	   "...". This  causes the registration maps of the children of the
     *	   <code>Rmap</code> to be expanded.
     * <br>, indicates multiple children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the requested <code>Rmap</code> hierarchy.
     * @return the <code>Rmap</code> hierarchy containing the registered
     *	       <code>Rmaps</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
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
    public Rmap getRegistered(com.rbnb.api.Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot get registered information for " + requestI);
    }

    /**
     * Gets the remote identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the remote identification.
     * @see #setRemoteID(String)
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
    public final String getRemoteID() {
	return (remoteID);
    }

    /**
     * Gets the type of client connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the client type.
     * @see #CLIENT
     * @see #MIRROR
     * @see #PLUGIN
     * @see #setType(byte)
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
    public final byte getType() {
	return (type);
    }

    /**
     * Gets the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Username</code>.
     * @see #setUsername(com.rbnb.api.Username usernameI)
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public final Username getUsername() {
	return (username);
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
     * 06/01/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Is this <code>ClientHandle</code> synchronized with the server?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the client synchronized?
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2002  INB	Created.
     *
     */
    public boolean isSynchronized() {
	return (true);
    }

    /**
     * Is this <code>ClientInterface</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is in a bad state.
     * @exception java.lang.InterruptedException
     *		  thrown if the check is interrupted.
     * @return is this <code>Client</code> running?
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
    public boolean isRunning()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    ("Cannot determine if " + this + " is running.");
    }

    /**
     * Creates a new instance of the same class as this
     * <code>ClientIO</code> (or a similar class).
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
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (newInstance(this));
    }

    /**
     * Creates a new instance of the same class as the input <code>Rmap</code>
     * (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the <code>Rmap</code> to create a new instance of.
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
     * @return the new instance.
     * @since V2.0
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    static Rmap newInstance(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;

	if (rmapI instanceof PlugInInterface) {
	    rmapR = new PlugInIO();
	} else if (rmapI instanceof RouterInterface) {
	    rmapR = new RouterIO();
	} else if (rmapI instanceof ControllerInterface) {
	    rmapR = new ControllerIO();
	} else if (rmapI instanceof SinkInterface) {
	    rmapR = new SinkIO();
	} else if (rmapI instanceof SourceInterface) {
	    rmapR = new SourceIO();
	}
	((ClientIO) rmapR).copyFields(rmapI);
	rmapR.setName(rmapI.getName());

	if (rmapI instanceof ClientInterface) {
	    ClientInterface ci = (ClientInterface) rmapI,
		ciR = (ClientInterface) rmapR;

	    ciR.setType(ci.getType());
	    ciR.setRemoteID(ci.getRemoteID());
	}

	return (rmapR);
    }

    /**
     * Reads the <code>ClientIO</code> from the specified input stream.
     * <p>
     * This version uses the input <code>ClientInterface</code> to fill in
     * default values for fields not read from the input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>ClientInterface</code> as an
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
     * 06/01/2001  INB	Created.
     *
     */
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	read(isI,disI,this,otherI);
    }

    /**
     * Reads the <code>ClientInterface</code> from the specified input stream.
     * <p>
     * The input <code>ClientInterface</code> is used to provide default values
     * for fields that are not seen on the input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @param ciIO   the <code>ClientInterface</code>.
     * @param otherI the other <code>ClientInteface</code> as an
     *		     <code>Rmap</code>.
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
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream,com.rbnb.api.ClientInterface,com.rbnb.api.ClientInterface)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    static void read(InputStream isI,
		     DataInputStream disI,
		     ClientInterface ciIO,
		     Rmap otherI)
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
	    if (!readClientParameter(parameter,isI,disI,ciIO)) {
		((Rmap) ciIO).readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultClientParameters(ciIO,(ClientInterface) otherI,seen);
    }

    /**
     * Reads <code>ClientInterface</code> parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI the parameter index.
     * @param isI	 the input stream.
     * @param disI	 the data input stream.
     * @param ciIO	 the <code>ClientInterface</code>.
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
     * @see #writeClientParameters(com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream,com.rbnb.api.ClientInterface,com.rbnb.api.ClientInterface)
     * @since V2.0
     * @version 07/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    static final boolean readClientParameter(int parameterI,
					     InputStream isI,
					     DataInputStream disI,
					     ClientInterface ciIO)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean successR = false;

	if (parameterI >= parametersStart) {
	    successR = true;
	    switch (parameterI - parametersStart) {
	    case PAR_RID:
		ciIO.setRemoteID(isI.readUTF());
		break;

	    case PAR_TYP:
		ciIO.setType(isI.readByte());
		break;

	    default:
		successR = false;
		break;
	    }
	}

	return (successR);
    }

    /**
     * Sets the remote identification.
     * <p>
     * The remote identification specifies the fully-qualified name of the
     * remote RBNB object with which this RBNB object is communicating, if
     * any.
     * <p>
     *
     * @author Ian Brown
     *
     * @param remoteIDI the remote identification.
     * @see #getRemoteID()
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
    public final void setRemoteID(String remoteIDI) {
	remoteID = remoteIDI;
    }

    /**
     * Sets the type of client connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI the type of client connection.
     * @see #CLIENT
     * @see #getType()
     * @see #MIRROR
     * @see #PLUGIN
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
    public final void setType(byte typeI) {
	type = typeI;
    }

    /**
     * Sets the <code>Username</code> for this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @see #getUsername()
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public final void setUsername(Username usernameI) {
	username = usernameI;
    }

    /**
     * Starts this <code>ClientIO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>ClientIO</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>ClientIO</code> is already running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @see #stop()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be started.");
    }

    /**
     * Starts the specified <code>ClientIO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientIO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #stop(com.rbnb.api.Client)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public void start(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be used to start " + clientI);
    }

    /**
     * Starts the specified <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>Shortcut</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #stop(com.rbnb.api.Shortcut)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public void start(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be used to start " + shortcutI);
    }

    /**
     * Stops this <code>ClientIO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>ClientIO</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>ClientIO</code> is already running.</li>
     *		  </ul><p>
     * @see #start()
     * @since V2.0
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be stopped.");
    }

    /**
     * Stops the <code>ClientIO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>ClientIO</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #start(com.rbnb.api.Client)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public void stop(Client clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be used to stop " + clientI);
    }

    /**
     * Stops the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI the <code>Server</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public void stop(Server serverI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be used to stop " + serverI);
    }

    /**
     * Stops the <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>Shortcut</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #start(com.rbnb.api.Shortcut)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public void stop(Shortcut shortcutI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be used to stop " + shortcutI);
    }

    /**
     * Synchronizes with the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>ClientIO</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>ClientIO</code> is not running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the synchronization is interrupted.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/22/2000  INB	Created.
     *
     */
    public void synchronizeWserver()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	throw new java.lang.IllegalStateException
	    (this + " cannot be synchronized.");
    }

    /**
     * Writes this <code>ClientIO</code> to the specified stream.
     * <p>
     * This method writes out the differences between this
     * <code>ClientInterface</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * #param otherI	   the other <code>ClientInterface</code> as an
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
     * @version 07/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
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
	write(parametersI,parameterI,osI,dosI,this,(ClientInterface) otherI);
    }

    /**
     * Writes the <code>ClientInterface</code> to the specified stream.
     * <p>
     * This method writes out differences between this
     * <code>ClientInterface</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI our parent's parameter list.
     * @param paramterI	  the parent parameter to use.
     * @param osI	  the output stream.
     * @param dosI	  the data output stream.
     * @param ciI	  the <code>ClientInterface</code>.
     * @param otherI	  the <code>ClientInterface</code> to compare to.
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
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    static void write(String[] parametersI,
		      int parameterI,
		      OutputStream osI,
		      DataOutputStream dosI,
		      ClientInterface ciI,
		      ClientInterface otherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Write out the object.
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged((Serializable) ciI,parametersI,parameterI);

	((Rmap) ciI).writeStandardParameters((Rmap) otherI,osI,dosI);
	writeClientParameters(osI,dosI,ciI,otherI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes out <code>ClientInterface</code> parameters.
     * <p>
     * This method only writes out differences between this
     * <code>ClientInterface</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI    the output stream.
     * @param dosI   the data output stream.
     * @param ciI    the <code>ClientInterface</code>.
     * @param otherI the other <code>ClientInterface</code>.
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
     * @see #readClientParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream,com.rbnb.api.ClientInterface)
     * @since V2.0
     * @version 03/01/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    static final void writeClientParameters(OutputStream osI,
					    DataOutputStream dosI,
					    ClientInterface ciI,
					    ClientInterface otherI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	if ((ciI.getRemoteID() != null) &&
	    ((otherI == null) ||
	     (otherI.getRemoteID() == null) ||
	     !otherI.getRemoteID().equals(ciI.getRemoteID()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_RID);
	    osI.writeUTF(ciI.getRemoteID());
	}

	if ((otherI == null) ||
	    ((otherI != null) && (ciI.getType() != otherI.getType()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_TYP);
	    osI.writeByte(ciI.getType());
	}
    }

    /**
     * Try to reconnect to an existing <code>ClientHandler</code>?
     * <p>
     * Generally when a client logs into the server, and an existing
     * <code>ClientHandler</code> is found that matches, the new client is
     * renamed. This method should be overridden if that behavior needs to be
     * changed.
     * <p>
     *
     * @author Ian Brown
     *
     * @return try to reconnect?
     * @since V2.1
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	Created.
     *
     */
    public boolean tryReconnect() {
	return (false);
    }
}
