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
 * Object representing a handle to an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 06/22/2006
 */

/*
 * Copyright 2001, 2002, 2003, 2004, 2005, 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/22/2006  JPW	Added -H flag (archive home directory).
 * 2005/01/19  WHF	Added -P flag.
 * 09/30/2004  JPW	In order to compile under J# (which is only compatable
 *			with Java 1.1.4), support for HTTPS has been disabled
 *			for reading the Options file.
 * 05/11/2004  INB	Handle the case where clients aren't actually running
 *			in the stop method.
 * 11/19/2003  INB	Added -C flag.
 * 11/17/2003  INB	Added -c flag.
 * 11/03/2003  INB	Added -T flag for testing.
 * 04/03/2003  INB	Renamed -F to -S, added new -F flag.
 * 03/19/2003  INB	Allow for "+" is -s switch handling.
 * 03/13/2003  INB	Added -F and -M switches.
 * 02/28/2003  INB	The default name of the server is now set equal to its
 *			address.
 * 05/11/2001  INB	Created.
 *
 */
public class Server
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.ServerInterface
{
    /**
     * the address handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/11/2002
     */
    private transient Address addressHandler = null;

    /*
     * the server-side object.
     * <p>
     * This field is set only if the server-side is running as a thread within
     * the current application.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/11/2002
     */
    private transient ServerHandler serverSide = null;

    /**
     * the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private String address = null;

    // Private constants:
    private final static byte PAR_ADR = 0;

    private final static String[] PARAMETERS = {
				    "ADR"
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
    Server() {
	super();
    }

    /**
     * Class constructor to build a <code>Server</code> for a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
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
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    Server(String nameI)
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
     * Class constructor to build a <code>Server</code> for a name and an
     * address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
     * @param addressI the server's address.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address provided.
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
     * @since V2.0
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
    Server(String nameI,String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(nameI);
	setAddress(addressI);
    }

    /**
     * Class constructor to build a <code>Server</code> by reading it in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
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
     * 05/10/2001  INB	Created.
     *
     */
    Server(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>Server</code> by reading it in.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code> as an <code>Rmap</code>.
     * @param isI    the <code>InputStream</code>.
     * @param disI   the <code>DataInputStream</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
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
     * 05/10/2001  INB	Created.
     *
     */
    Server(Rmap otherI,InputStream isI,DataInputStream disI)
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
     * This implementation returns the <code>Server's</code> address.
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
     * 05/18/2001  INB	Created.
     *
     */
    String additionalToString() {
	return ("  Address: " + getAddress());
    }

    /**
     * Adds the <code>Server's</code> parameters to the full serialization
     * parameters list.
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
     * Adds an argument without a value to the list of "remaining arguments".
     * <p>
     *
     * @author Ian Brown
     *
     * @param argumentI the argument to add.
     * @param remArgsI  the current list.
     * @return the updated list.
     * @since V2.0
     * @version 12/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/12/2002  INB	Created.
     *
     */
    private final static String[] addToRemaining(String argumentI,
						 String[] remArgsI)
    {
	String[] remArgsR;
	int idx = 0;

	if (remArgsI == null) {
	    remArgsR = new String[1];

	} else {
	    remArgsR = new String[remArgsI.length + 1];
	    System.arraycopy(remArgsI,0,remArgsR,0,remArgsI.length);
	    idx = remArgsI.length;
	}

	remArgsR[idx++] = argumentI;
	return (remArgsR);
    }

    /**
     * Adds an argument with a value to the list of "remaining arguments".
     * <p>
     *
     * @author Ian Brown
     *
     * @param argumentI the argument to add.
     * @param argsI	the list of command line arguments.
     * @param idxI	the index of the argument containing the value.
     * @param offsetI	the offset to the value.
     * @param remArgsI  the current list.
     * @return the updated list.
     * @since V2.0
     * @version 12/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/12/2002  INB	Created.
     *
     */
    private final static String[] addToRemaining(String argumentI,
						 String[] argsI,
						 int idxI,
						 int offsetI,
						 String[] remArgsI)
    {
	String[] remArgsR = remArgsI;

	if ((idxI < argsI.length) &&
	    ((offsetI > 0) || (argsI[idxI].charAt(0) != '-'))) {
	    int idx = 0;

	    if (remArgsI == null) {
		remArgsR = new String[2];

	    } else {
		remArgsR = new String[remArgsI.length + 2];
		System.arraycopy(remArgsI,0,remArgsR,0,remArgsI.length);
		idx = remArgsI.length;
	    }

	    remArgsR[idx++] = argumentI;
	    remArgsR[idx++] = argsI[idxI].substring(offsetI);
	}

	return (remArgsR);
    }

    /**
     * Creates a <code>Controller</code> for this <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>Controller</code>.
     * @return the <code>Controller</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createRAMController(String nameI)
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
    public final Controller createController(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Controller controllerR = new ControllerHandle(nameI);

	addChild((Rmap) controllerR);
	((ClientHandle) controllerR).setACO(ACO.newACO(controllerR));

	return (controllerR);
    }

    /**
     * Creates a <code>Mirror</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Mirror</code>.
     * @since V2.0
     * @version 04/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final static Mirror createMirror() {
	return (new MirrorHandle());
    }

    /**
     * Creates a <code>PlugIn</code> for this <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>PlugIn</code>.
     * @return the <code>PlugIn</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createRAMPlugIn(String nameI)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    public final PlugIn createPlugIn(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PlugIn plugInR = new PlugInHandle(nameI);

	addChild((Rmap) plugInR);
	((ClientHandle) plugInR).setACO(ACO.newACO(plugInR));

	return (plugInR);
    }

    /**
     * Creates a <code>Controller</code> that communicates via RAM.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>Controller</code>
     * @return the <code>Controller</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createController(String nameI)
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
    public final Controller createRAMController(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Controller controllerR = new ControllerHandle(nameI);

	addChild((Rmap) controllerR);
	((ClientHandle) controllerR).setACO(ACO.newRAMACO(controllerR));

	return (controllerR);
    }

    /**
     * Creates a <code>PlugIn</code> that communicates via RAM.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>PlugIn</code>
     * @return the <code>PlugIn</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createPlugIn(String nameI)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2002  INB	Created.
     *
     */
    public final PlugIn createRAMPlugIn(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	PlugIn plugInR = new PlugInHandle(nameI);

	addChild((Rmap) plugInR);
	((ClientHandle) plugInR).setACO(ACO.newRAMACO(plugInR));

	return (plugInR);
    }

    /**
     * Creates a <code>Sink</code> that communicates via RAM.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>Sink</code>
     * @return the <code>Sink</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createSink(String nameI)
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
    public final Sink createRAMSink(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Sink sinkR = new SinkHandle(nameI);

	addChild((Rmap) sinkR);
	((ClientHandle) sinkR).setACO(ACO.newRAMACO(sinkR));

	return (sinkR);
    }

    /**
     * Creates a <code>Source</code> that communicates via RAM.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>Source</code>
     * @return the <code>Source</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createSource(String nameI)
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
    public final Source createRAMSource(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Source sourceR = new SourceHandle(nameI);

	addChild((Rmap) sourceR);
	((ClientHandle) sourceR).setACO(ACO.newRAMACO(sourceR));

	return (sourceR);
    }

    /**
     * Creates a <code>Shortcut</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI		  the name of the <code>Shortcut</code>.
     * @param destinationAddressI the destination of the <code>Shortcut</code>.
     * @param costI		  the cost of the <code>Shortcut</code>.
     * @return the <code>Shortcut</code>.
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    public static final Shortcut createShortcut(String nameI,
						String destinationAddressI,
						double costI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Shortcut shortcutR = new ShortcutIO();

	shortcutR.setName(nameI);
	shortcutR.setDestinationAddress(destinationAddressI);
	shortcutR.setCost(costI);

	return (shortcutR);
    }

    /**
     * Creates a <code>Sink</code> for this <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>Sink</code>.
     * @return the <code>Sink</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createRAMSink(String nameI)
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
    public final Sink createSink(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Sink sinkR = new SinkHandle(nameI);

	addChild((Rmap) sinkR);
	((ClientHandle) sinkR).setACO(ACO.newACO(sinkR));

	return (sinkR);
    }

    /**
     * Creates a <code>Source</code> for this <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>Source</code>.
     * @return the <code>Source</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with the address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #createRAMSource(String nameI)
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
    public final Source createSource(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Source sourceR = new SourceHandle(nameI);

	addChild((Rmap) sourceR);
	((ClientHandle) sourceR).setACO(ACO.newACO(sourceR));

	return (sourceR);
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
	defaultServerParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Default <code>Server</code> parameters.
     * <p>
     * This method fills in any fields not read from an input stream by copying
     * them from the input <code>Rmap</code>.
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code> as an <code>Rmap</code>.
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
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    final void defaultServerParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((otherI != null) && (otherI instanceof Server)) {
	    Server other = (Server) otherI;

	    if ((seenI == null) || !seenI[parametersStart + PAR_ADR]) {
		setAddress(other.getAddress());
	    }
	}
    }

    /**
     * Gets the server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address of the server.
     * @see #setAddress(String)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public final String getAddress() {
	return (address);
    }

    /**
     * Gets the server address handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Address</code> handler.
     * @see #setAddressHandler(com.rbnb.api.Address)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    final Address getAddressHandler() {
	return (addressHandler);
    }

    /**
     * Gets the server-side object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
     * @see #setServerSide(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2001  INB	Created.
     *
     */
    final ServerHandler getServerSide() {
	return (serverSide);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Is this <code>Server</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the <code>Server</code> running?
     * @since V2.0
     * @version 05/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    public boolean isRunning() {
	boolean runningR = false;
	Controller controller = null;

	try {
	    controller = createController("_RunCheck");
	    controller.start();
	    controller.stop();
	    runningR = true;
	} catch (java.lang.Exception e) {
	    try {
		if (controller != null) {
		    removeChild((Rmap) controller);
		}
	    } catch (java.lang.Exception e1) {
	    }
	}

	return (runningR);
    }

    /**
     * Launches a new server based on the command line arguments.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @return the <code>Server</code> handle.
     * @exception java.lang.IllegalArgumentException
     *		  if an illegal argument is found.
     * @exception java.lang.Exception
     *		  if any other error occurs.
     * @since V2.0
     * @version 11/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/03/2003  INB	Added handling of tests.
     * 02/28/2003  INB	The default name of the server is now set equal to its
     *			address.
     * 12/12/2002  INB	Created.
     *
     */
    public final static Server launchNewServer(String[] argsI)
	throws java.lang.IllegalArgumentException,
	       java.lang.Exception
    {
	Server serverR = null;
	String[] defaultId = new String[3],
	    identification = new String[3];
	defaultId[0] = "tcp://localhost:3333";
	defaultId[1] =
	    Address.newAddress(defaultId[0]).getAddress();
	defaultId[1] = defaultId[1].substring(defaultId[1].indexOf(":") + 3);
	System.arraycopy(defaultId,0,identification,0,defaultId.length);
	String[] remArgs = null;
	java.util.Vector peers = new java.util.Vector();
	StringBuffer tests = new StringBuffer("");

	    // Process the command line arguments.
	remArgs = processArguments(argsI,remArgs,identification,peers,tests);
	if (!identification[0].equals(defaultId[0]) &&
	    identification[1].equals(defaultId[1])) {
	    identification[1] =
		Address.newAddress(identification[0]).getAddress();
	    identification[1] =
		identification[1].substring
		(identification[1].indexOf("://") + 3);
	}

	// Create the server.
	serverR = newServerHandle(identification[1],identification[0]);
	if (identification[2] != null) {
	    Server parentServer = newServerHandle(null,
						  identification[2]);
	    parentServer.addChild(serverR);
	}
	serverR.start(remArgs);

	if (peers.size() > 0) {
	    Controller pController = serverR.createController("PeerSetup");
	    pController.start();

	    for (int idx = 0; idx < peers.size(); idx += 3) {
		String peerName = (String) peers.elementAt(idx),
		    peerAddress = (String) peers.elementAt(idx + 1),
		    peerCostS = (String) peers.elementAt(idx + 2);
		double peerCost;
		try {
		    peerCost = ((peerCostS == null) ?
				1. :
				Double.parseDouble(peerCostS));
		} catch (java.lang.NoSuchMethodError e) {
		    peerCost = ((peerCostS == null) ?
				1. :
				Double.valueOf(peerCostS).doubleValue());
		}

		try {
		    Shortcut shortcut = createShortcut(peerName,
						       peerAddress,
						       peerCost);
		    pController.start(shortcut);
		} catch (java.lang.Exception e) {
		    e.printStackTrace();
		}
	    }

	    pController.stop();
	}

	if (tests.length() > 0) {
	    serverR.runTests(tests);
	}

	return (serverR);
    }

    /**
     * Main DataTurbine <code>RBNB</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI  the command line arguments.
     * @since V2.0
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2006  JPW	Added -H flag.
     * 2005/01/19  WHF  Added -P flag.
     * 04/03/2003  INB	Added -S flag.
     * 01/22/2001  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {
	    Server server = launchNewServer(argsI);

	} catch (java.lang.IllegalArgumentException e) {
	    e.printStackTrace();
	    System.err.println("Syntax:");
	    System.err.println("java com.rbnb.api.Server");
	    System.err.println("  -a <server address - [host][:port]>");
	    System.err.println("  -A <security file URL>");
	    System.err.println("  -D <mask>,<level>");
	    System.err.println("  -F");
	    System.err.println("  -H <archive home directory>");
	    System.err.println("  -l [<log period (sec)>]" +
			       "[,<log cache frames>]" +
			       "[,[<log cache frames>]" +
			       "[,[<log archive frames>]" +
			       "[,[<log archive mode>]]]]");
	    System.err.println("  -L");
	    System.err.println("  -m [<metrics period (sec)>]" +
			       "[,[<metrics cache frames>]" +
			       "[,[<metrics archive frames>]" +
			       "[,[<metrics archive mode>]]]]");
	    System.err.println("  -M <maximum activity threads>");
	    System.err.println("  -n <server name>");
	    System.err.println("  -O <options file URL>");
	    System.err.println("  -p <parent server address>");
	    System.err.println("  -P [<userid>][,<password>]");
	    System.err.println("  -s <shortcut name>,<remote address>," +
			       "[<cost>]");
	    System.err.println("  -S <maximum open filesets>");

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Creates a new instance of the same class as this <code>Server</code> (or
     * a similar class).
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
     * @version 12/18/2001
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
	return (new Server(getName(),getAddress()));
    }

    /**
     * Creates a new <code>Server</code> handle to an RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the server's name.
     *		       <p>
     *		       This field may be omitted if the server is an external
     *		       one.  If so, the getName() method returns the actual
     *		       name of the <code>Server</code> once a
     *		       <code>Client</code> connection has been established.
     * @param addressI the server's address.
     * @return the <code>Server</code> handle.
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
     * @since V2.0
     * @version 10/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public final static Server newServerHandle(String nameI,
					       String addressI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server serverR = new Server(nameI,addressI);

	return (serverR);
    }

    /**
     * Processes command line arguments.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI	      the command line arguments.
     * @param remArgsI	      the "remaining arguments" so far.
     * @param identificationO identification argument settings:
     *			      <p><ol start=0>
     *			         <li>Server's address,</li>
     *				 <li>Server's name,</li>
     *				 <li>Parent server's address.</li>
     *			      </ol>
     * @param peersO	      the list of peers (shortcuts).
     * @param testsO	      the list of tests to run.
     * @return additional arguments to be processed by the RBNB itself.
     * @exception java.lang.IllegalArgumentException
     *		  if an illegal argument is found.
     * @exception java.lang.Exception
     *		  if any other error occurs.
     * @since V2.0
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2006  JPW	Added -H flag.
     * 01/19/2005  WHF	Added -P flag (username/password)
     * 11/19/2003  INB	Added -C flag.
     * 11/17/2003  INB	Added -c flag.
     * 11/03/2003  INB	Added -T testing flag.
     * 04/03/2003  INB	Renamed -F to -S, added new -F flag.
     * 03/19/2003  INB	Allow for "+" is -s switch handling.
     * 03/13/2003  INB	Added -F and -M switches.
     * 12/12/2002  INB	Created.
     *
     */
    private final static String[] processArguments(String[] argsI,
						   String[] remArgsI,
						   String[] identificationO,
						   java.util.Vector peersO,
						   StringBuffer testsO)
	throws java.lang.IllegalArgumentException,
	       java.lang.Exception
    {
	String[] remArgsR = remArgsI;

	for (int idx = 0; idx < argsI.length; ) {
	    // Search for valid command line arguments.
	    int idx1,
		idx2;

	    if ((argsI[idx].charAt(0) != '-') &&
		(argsI[idx].charAt(0) != '/')) {
		throw new java.lang.IllegalArgumentException
		    (argsI[idx] + " (" + idx + ") is not a switch.");

	    } else if (argsI[idx].length() == 1) {
		throw new java.lang.IllegalArgumentException
		    ("- (" + idx + ") is not a valid switch.");

	    } else if (argsI[idx].length() == 2) {
		// If there is no switch value in the same argument as the
		// switch, then it must be in the next argument (if any).
		idx1 = idx + 1;
		idx2 = 0;

	    } else {
		// If the switch value is in the same argument as the
		// switch, then point to it.
		idx1 = idx;
		for (idx2 = 2;
		     ((idx2 < argsI[idx].length()) &&
		      Character.isWhitespace(argsI[idx].charAt(idx2)));
		     ++idx2) {}

		if (idx2 == argsI[idx].length()) {
		    // If there is only whitespace in the argument after
		    // the switch, the value must actually be in the next
		    // entry.
		    idx1 = idx + 1;
		    idx2 = 0;
		}
	    }

	    char sName = argsI[idx].charAt(1);
	    String[] oldArgs = remArgsR;
	    if (sName == 'a') {
		if ((idx1 < argsI.length) &&
		    ((idx2 != 0) || (argsI[idx1].charAt(0) != '-'))) {
		    identificationO[0] = argsI[idx1].substring(idx2);
		}

	    } else if (sName == 'A') {
		remArgsR = addToRemaining("-A",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'C') {
		remArgsR = addToRemaining("-C",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'c') {
		remArgsR = addToRemaining("-c",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}
		
	    } else if (sName == 'D') {
		remArgsR = addToRemaining("-D",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'F') {
		remArgsR = addToRemaining("-F",remArgsR);
		--idx1;

	    } else if (sName == 'H') {   // JPW 06/22/2006: archive home dir
		remArgsR = addToRemaining("-H",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'l') {
		remArgsR = addToRemaining("-l",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'L') {
		remArgsR = addToRemaining("-L",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'm') {
		remArgsR = addToRemaining("-m",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'M') {
		remArgsR = addToRemaining("-M",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'n') {
		if ((idx1 < argsI.length) &&
		    ((idx2 != 0) || (argsI[idx1].charAt(0) != '-'))) {
		    identificationO[1] = argsI[idx1].substring(idx2);
		} else {
		    --idx1;
		}

	    } else if (sName == 'O') {
		if ((idx1 < argsI.length) &&
		    ((idx2 != 0) || (argsI[idx1].charAt(0) != '-'))) {
		    remArgsR = readOptionsFile(argsI[idx1].substring(idx2),
					       remArgsR,
					       identificationO,
					       peersO,
					       testsO);
		} else {
		    --idx1;
		}

	    } else if (sName == 'p') {
		if ((idx1 < argsI.length) &&
		    ((idx2 != 0) || (argsI[idx1].charAt(0) != '-'))) {
		    identificationO[2] = argsI[idx1].substring(idx2);
		    if (identificationO[2].equalsIgnoreCase("<none>")) {
			identificationO[2] = null;
		    }
		} else {
		    --idx1;
		}

	    } else if (sName == 'P') { // 2005/01/19  WHF  username/password
		// addToRemaining has the surprising quality of eating flags
		//  which have no values following.  So, we have to check
		//  for that case.
		if (idx1 < argsI.length && idx2 < argsI[idx1].length()
			&& argsI[idx1].charAt(idx2)!='-') {
		    remArgsR = addToRemaining("-P",argsI,idx1,idx2,remArgsR);
		    if (remArgsR == oldArgs) {
			--idx1;
		    }		    
		} else {
		    remArgsR = addToRemaining("-P", remArgsR);
		    --idx1;
		}
	    } else if (sName == 's') {
		if ((idx1 < argsI.length) &&
		    ((idx2 != 0) || (argsI[idx1].charAt(0) != '-'))) {
		    String arguments  = argsI[idx1].substring(idx2);
		    java.util.StringTokenizer sTok =
			new java.util.StringTokenizer(arguments,",+");
		    String[] values = new String[3];
		    int nArgs = 0;
		    while (sTok.hasMoreTokens()) {
			if (nArgs == values.length) {
			    throw new java.lang.IllegalArgumentException
				("Too many arguments to the -s switch.");
			}
			values[nArgs++] = sTok.nextToken();
		    }
		    peersO.addElement(values[0]);
		    peersO.addElement(values[1]);
		    if (nArgs == 3) {
			peersO.addElement(values[2]);
		    } else {
			peersO.addElement(null);
		    }
		} else {
		    --idx1;
		}


	    } else if (sName == 'S') {
		remArgsR = addToRemaining("-S",argsI,idx1,idx2,remArgsR);
		if (remArgsR == oldArgs) {
		    --idx1;
		}

	    } else if (sName == 'T') {
		if ((idx1 < argsI.length) &&
		    ((idx2 !=  0) || (argsI[idx1].charAt(0) != '-'))) {
		    testsO.append(argsI[idx1].substring(idx2));

		} else {
		    --idx1;
		}

	    } else {
		throw new java.lang.IllegalArgumentException
		    (argsI[idx] + " is not a recognized switch.");
	    }

	    idx = idx1 + 1;
	}

	return (remArgsR);
    }

    /**
     * Reads the <code>Server</code> from the specified input stream.
     * <p>
     * This method uses the input <code>Rmap</code> to provide default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code> as an <code>Rmap</code>.
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
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
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
	    if (!readServerParameter(parameter,isI,disI)) {
		readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads an options file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fileNameI	      the name of the file (a URL).
     * @param remArgsI	      the "remaining arguments" so far.
     * @param identificationO identification argument settings:
     *			      <p><ol start=0>
     *			         <li>Server's address,</li>
     *				 <li>Server's name,</li>
     *				 <li>Parent server's address.</li>
     *			      </ol>
     * @param peersO	      the list of peers (shortcuts).
     * @param testsO	      the list of tests to run.
     * @return additional arguments to be processed by the RBNB itself.
     * @exception java.lang.IllegalArgumentException
     *		  if an illegal argument is found.
     * @exception java.lang.Exception
     *		  if any other error occurs.
     * @since V2.0
     * @version 09/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/29/2004  JPW  In order to compile under J# (which is only
     *			compatable with Java 1.1.4), support for HTTPS
     *			has been disabled.  Throw an exception if the
     *			protocol is HTTPS
     * 12/12/2002  INB	Created.
     *
     */
    private final static String[] readOptionsFile(String fileNameI,
						  String[] remArgsI,
						  String[] identificationO,
						  java.util.Vector peersO,
						  StringBuffer testsO)
	throws java.lang.IllegalArgumentException,
	       java.lang.Exception
    {
	String[] remArgsR = remArgsI;
	String fileName = fileNameI,
	    localhost = "://localhost";
	int idx = fileName.indexOf(localhost);
	if (idx != -1) {
	    fileName = (fileName.substring(0,idx) +
			"://" + TCP.getLocalHost().getHostName() +
			fileName.substring(idx + localhost.length()));
	}
	java.net.URL url = new java.net.URL(fileName);
	java.net.URLConnection urlCon = url.openConnection();

	if (url.getProtocol().equalsIgnoreCase("HTTPS")) {
	    
	    // JPW 09/29/2004: In order to compile under J# (which is only
	    //                 compatable with Java 1.1.4), support for HTTPS
	    //                 has been disabled.  Throw an exception if the
	    //                 protocol is HTTPS
	    throw new Exception(
	        "HTTPS not supported for reading Options file.");
	    
	    /*
	    if (urlCon instanceof javax.net.ssl.HttpsURLConnection) {
		javax.net.ssl.HttpsURLConnection sslCon =
		    (javax.net.ssl.HttpsURLConnection) urlCon;
		sslCon.setSSLSocketFactory
		    ((javax.net.ssl.SSLSocketFactory)
		     RBNBSSLSocketFactory.getDefault());

	    } else {
		Class conCls = urlCon.getClass();
		Class[] cParameters = { Class.forName
					("javax.net.ssl.SSLSocketFactory") };
		java.lang.reflect.Method method = conCls.getMethod
		    ("setSSLSocketFactory",
		     cParameters);
		Object[] oParameters = { RBNBSSLSocketFactory.getDefault() };
		method.invoke(urlCon,oParameters);
	    }
	    */
	}

	urlCon.connect();
	java.io.InputStreamReader isr = new java.io.InputStreamReader
	    (urlCon.getInputStream());
	java.io.LineNumberReader lnr = new java.io.LineNumberReader(isr);

	try {
	    String line;
	    while ((line = lnr.readLine()) != null) {
		if ((line.length() == 0) || (line.charAt(0) == '#')) {
		    continue;
		}
		java.util.Vector entries = new java.util.Vector();
		java.util.StringTokenizer st = new java.util.StringTokenizer
		    (line,
		     " ");
		while (st.hasMoreTokens()) {
		    entries.addElement(st.nextToken());
		}
		String[] args = new String[entries.size()];
		entries.copyInto(args);
		remArgsR = processArguments(args,
					    remArgsR,
					    identificationO,
					    peersO,
					    testsO);
	    }
	} catch (java.io.EOFException e) {
	}
	isr.close();
	lnr.close();

	return (remArgsR);
    }

    /**
     * Reads <code>Server</code> parameters.
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
     * @see #writeServerParameters(com.rbnb.api.Server,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
     *
     */
    final boolean readServerParameter(int parameterI,
				      InputStream isI,
				      DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean serverR = false;

	if (parameterI >= parametersStart) {
	    serverR = true;

	    switch (parameterI - parametersStart) {
	    case PAR_ADR:
		setAddress(isI.readString());
		break;

	    default:
		serverR = false;
		break;
	    }
	}

	return (serverR);
    }

    /**
     * Runs special tests for debugging purposes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param testsI <code>StringBuffer</code> containing the test
     *		     descriptions.
     * @since V2.2
     * @version 11/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/03/2003  INB	Created.
     *
     */
    private final void runTests(StringBuffer testsI) {
	Source src = null;
	try {
	    src = createRAMSource("TestSource");
	    src.setCframes(25);
	    src.start();
	    Rmap frame = new Rmap();
	    float[] data = new float[1];
	    DataBlock dBlock = new DataBlock(data,
					     1,
					     4,
					     DataBlock.TYPE_FLOAT32,
					     DataBlock.ORDER_MSB,
					     false,
					     0,
					     4);
	    TimeRange tRange = new TimeRange();
	    Rmap channel = new Rmap("c",dBlock,tRange);
	    frame.addChild(channel);
	    for (int fIdx = 0; fIdx < 10; ++fIdx) {
		data[0] = fIdx;
		tRange.set(fIdx,1.);
		src.addChild(frame);
		src.synchronizeWserver();
	    }
	    TimeRelativeChannel rChan = new TimeRelativeChannel();
	    rChan.setChannelName("c");

	    TimeRelativeRequest request = new TimeRelativeRequest();
	    request.setTimeRange(new TimeRange(5.,1.));
	    request.addChannel(rChan);
	    request.setRelationship(request.AFTER);
	    ServerHandler sh = getServerSide();
	    RBO rbo = (RBO) sh.findDescendant("/" + getName() + "/TestSource",
					      false);
	    Rmap result = rbo.extractTimeRelative(request,null);
	    System.err.println("\nTime relative test:");
	    System.err.println("Request:\n" + request);
	    System.err.println("Result:\n" + result);
	    src.stop();

	} catch (Exception e) {
	    e.printStackTrace();

	} finally {
	    if (src != null) {
		try {
		    src.stop();
		} catch (Exception e1) {
		}
	    }
	}
    }

    /**
     * Sets the server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the address of the server.
     * @exception com.rbnb.api.AddressException
     *		  thrown if the address is not legal.
     * @see #getAddress()
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public final void setAddress(String addressI)
	throws com.rbnb.api.AddressException
    {
	address = addressI;
	if ((address != null) &&
	    ((getAddressHandler() == null) ||
	     !getAddressHandler().getAddress().equals(addressI))) {
	    setAddressHandler(Address.newAddress(addressI));
	}
    }

    /**
     * Sets the server address handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressHandlerI the address handler.
     * @exception com.rbnb.api.AddressException
     *		  thrown if the address is not legal.
     * @see #getAddressHandler()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    final void setAddressHandler(Address addressHandlerI)
	throws com.rbnb.api.AddressException
    {
	addressHandler = addressHandlerI;
	if ((getAddress() == null) ||
	    !getAddress().equals(addressHandlerI.getAddress())) {
	    setAddress(addressHandlerI.getAddress());
	}
    }

    /**
     * Sets the server-side object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverSideI  the <code>ServerHandler</code>.
     * @see #getServerSide()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2001  INB	Created.
     *
     */
    final void setServerSide(ServerHandler serverSideI) {
	serverSide = serverSideI;
    }

    /**
     * Starts the RBNB server as a thread in the current process.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #stop()
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public synchronized void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	start(null);
    }

    /**
     * Starts the RBNB server as a thread in the current process.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments that should be processed by
     *		    the running server.
     * @see #stop()
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem completing the connection due
     *		  addressing problems.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if the I/O is interrupted.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    public synchronized void start(String[] argsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((getServerSide() != null) && getServerSide().isRunning()) {
	    throw new java.lang.IllegalStateException
		("Cannot start " + getName() + " on " + getAddress() +
		 "; it is already running.");
	}

	setServerSide(new RBNB(getName(),getAddressHandler()));
	if ((getParent() != null) && (getParent() instanceof Server)) {
	    Server parentServer = new ParentServer
		(((Server) getParent()).getAddress());
	    parentServer.addChild((Rmap) getServerSide());
	}
	getServerSide().setClientSide(this);
	getServerSide().start(null,argsI);
    }

    /**
     * Stops the RBNB server.
     * <p>
     * All children of this <code>Server</code> object are stopped and removed
     * as children before the RBNB server itself is stopped.
     * <p>
     * If the server is not running locally, a control connection is made to it
     * and is used to stop the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the start is interrupted.
     * @since V2.0
     * @version 05/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2004  INB	Handle the case where clients aren't actually running.
     * 05/11/2001  INB	Created.
     *
     */
    public synchronized void stop()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    int wasChildren;
	    while (getNchildren() > 0) {
		wasChildren = getNchildren();
		Client client = (Client) getChildAt(0);
		client.stop();
		if (getNchildren() == wasChildren) {
		    removeChild((Rmap) client);
		}
	    }
	} catch (java.lang.Exception e) {
	}

	Controller controller = createController("_Terminator");
	controller.start();
	controller.stop(this);
	setServerSide(null);
	removeChildAt(0);
    }

    /**
     * Writes this <code>Server</code> to the specified stream.
     * <p>
     * This method writes out the differences between this <code>Server</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>Server</code> as an
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
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
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
	writeServerParameters((Server) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /** 
     * Writes out <code>Server</code> parameters.
     * <p>
     * This method writes out the differences between this <code>Server</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Server</code>.
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
     * @see #readServerParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/25/2001  INB	Created.
     *
     */
    final void writeServerParameters(Server otherI,
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

	if ((getAddress() != null) &&
	    ((otherI == null) || !getAddress().equals(otherI.getAddress()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_ADR);
	    osI.writeString(getAddress());
	}
    }
}
