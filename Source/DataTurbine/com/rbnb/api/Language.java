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
 * RBNB communication language.
 * <p>
 * This class provides methods for communications between applications and the
 * DataTurbine server via the RBNB language over a two pairs of streams:
 * <p><ol>
 * <li>Input streams (control and data), and</li>
 * <li>Output streams (control and data).</li>
 * </ol><p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/08/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/23/2007  WHF  Added Get/SetAddressAuthorization.
 * 01/08/2004  INB	Added support for <code>ClearCache</code>.
 * 07/30/2003  INB	Added support for <code>DeleteChannels</code>.
 * 06/11/2003  INB	Added support for <code>RequestOptions</code>.
 * 02/25/2003  INB	Added support for writing/read data for
 *			<code>Pings</code>.
 * 01/09/2001  INB	Created.
 *
 */
class Language
    implements com.rbnb.api.IOMetricsInterface
{

    // Public constants:
    public final static int	ADD = 0,
				COPY = 1,
				EDIT = 2,
				REMOVE = 3;

    // Package constants:
    final static byte	COM_ACK = 0,
			COM_ASK = 1,
			COM_CTL = 2,
			COM_EOS = 3,
			COM_EXM = 4,
			COM_LGN = 5,
			COM_MIR = 6,
			COM_OBJ = 7,
			COM_PNG = 8,
			COM_PTH = 9,
			COM_REG = 10,
			COM_REQ = 11,
			COM_RGR = 12,
			COM_RHD = 13,
			COM_RMP = 14,
			COM_RMS = 15,
			COM_RMU = 16,
			COM_RSV = 17,
			COM_RTM = 18,
			COM_SEA = 19,
			COM_SNK = 20,
			COM_SRC = 21,
			COM_SRV = 22,
			COM_STP = 23,
			COM_STR = 24,
			COM_SHC = 25,
			COM_PSR = 26,
			COM_PUP = 27,
			COM_PLG = 28,
			COM_RVS = 29,
			COM_RST = 30,
			COM_USR = 31,
			COM_RQO = 32,
			COM_DLC = 33,
			COM_CLC = 34,
			COM_GAA = 35,
			COM_SAA = 36;

    final static String[] COMMANDS = {
			    "ACK",
			    "ASK",
			    "CTL",
			    "EOS",
			    "EXM",
			    "LGN",
			    "MIR",
			    "OBJ",
			    "PNG",
			    "PTH",
			    "REG",
			    "REQ",
			    "RGR",
			    "RHD",
			    "RMP",
			    "RMS",
			    "RMU",
			    "RSV",
			    "RTM",
			    "SEA",
			    "SNK",
			    "SRC",
			    "SRV",
			    "STP",
			    "STR",
			    "SHC",
			    "PSR",
			    "PUP",
			    "PLG",
			    "RVS",
			    "RST",
			    "USR",
			    "RQO",
			    "DLC",
			    "CLC",
				"GAA",
				"SAA"
			};

    // Private fields:
    /**
     * the input data stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private DataInputStream inputData = null;

    /**
     * metrics: bytes transferred by closed streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    long metricsBytes = 0;

    /**
     * the output data stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private DataOutputStream outputData = null;

    /**
     * the input control stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private InputStream inputControl = null;

    /**
     * the output control stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */
    private OutputStream outputControl = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #Language(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    Language() {
	super();
    }

    /**
     * Class constructor to build a <code>Language</code> object from a set of
     * streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputControlI   the input control stream.
     * @param inputDataI      the input data stream.
     * @param outputControlI  the output control stream.
     * @param outputDataI     the output data stream.
     * @see #Language()
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    Language(InputStream inputControlI,
	     DataInputStream inputDataI,
	     OutputStream outputControlI,
	     DataOutputStream outputDataI)
    {
	this();
	setIcontrol(inputControlI);
	setIdata(inputDataI);
	setOcontrol(outputControlI);
	setOdata(outputDataI);
    }

    /**
     * Returns a <code>Serializable</code> request for information for the
     * inputs.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationTypeI  the type of information desired.
     * @param additionalI	additional parameters.
     * @return the <code>Serializable</code> request. 
     * @see #isRequest(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final Serializable ask(String informationTypeI,
			   java.util.Vector additionalI)
	throws java.io.IOException,
	       java.lang.InterruptedException,
	       SerializeException
    {
	return (new Ask(informationTypeI,additionalI));
    }

    /**
     * Is anything waiting to be read?
     * <p>
     *
     * @author Ian Brown
     *
     * @return the amount of information waiting to be read.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    final int available()
	throws java.io.IOException
    {
	return (getIcontrol().available());
    }

    /**
     * Calculates the total number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes transferred.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    public final long bytesTransferred() {
	long bytesR = metricsBytes;

	if (getIcontrol() != null) {
	    bytesR += getIcontrol().getRead();
	}
	if (getIdata() != null) {
	    bytesR += getIdata().getRead();
	}
	if (getOcontrol() != null) {
	    bytesR += getOcontrol().getWritten();
	}
	if (getOdata() != null) {
	    bytesR += getOdata().getWritten();
	}

	return (bytesR);
    }

    /**
     * Closes the streams for this <code>Language</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.IOException
     *		  thrown if there is a problem closing the streams.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    final void close()
	throws java.io.IOException
    {
	if (getIcontrol() != null) {
	    metricsBytes += getIcontrol().getRead();
	    getIcontrol().close();
	    setIcontrol(null);
	}
	if (getIdata() != null) {
	    metricsBytes += getIdata().getRead();
	    getIdata().close();
	    setIdata(null);
	}
	if (getOcontrol() != null) {
	    metricsBytes += getOcontrol().getWritten();
	    getOcontrol().close();
	    setOcontrol(null);
	}
	if (getOdata() != null) {
	    metricsBytes += getOdata().getWritten();
	    getOdata().close();
	    setOdata(null);
	}
    }

    /**
     * Returns an <code>Serializable</code> exception message for the input
     * <code>Exception</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param exceptionI  the <code>Exception</code>.
     * @return the <code>Serializable</code> exception message.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static Serializable exception(Exception exceptionI) {
	return (new ExceptionMessage(exceptionI));
    }

    /**
     * Gets the input control stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the input control stream.
     * @see #setIcontrol(com.rbnb.api.InputStream)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final InputStream getIcontrol() {
	return (inputControl);
    }

    /**
     * Gets the input data stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the input data stream.
     * @see #setIdata(com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final DataInputStream getIdata() {
	return (inputData);
    }

    /**
     * Gets the output control stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the output control stream.
     * @see #setOcontrol(com.rbnb.api.OutputStream)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final OutputStream getOcontrol() {
	return (outputControl);
    }

    /**
     * Gets the output data stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the output data stream.
     * @see #setOdata(com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final DataOutputStream getOdata() {
	return (outputData);
    }

    /**
     * Identifies the input object by returning the command to use to transmit
     * it.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the object in question.
     * @return the command index.
     * @since V2.0
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Added support for <code>ClearCache</code>.
     * 07/30/2003  INB	Added support for <code>DeleteChannels</code>.
     * 06/11/2003  INB	Added support for <code>RequestOptions</code>.
     * 07/27/2001  INB	Created.
     *
     */
    private final static int identifyObject(Object objectI) {
	int commandR = -1;
	if (objectI instanceof Serializable) {
	    if (objectI instanceof Acknowledge) {
		commandR = COM_ACK;
	    } else if (objectI instanceof Ask) {
		commandR = COM_ASK;
	    } else if (objectI instanceof Router) {
		commandR = COM_RHD;
	    } else if (objectI instanceof ClearCache) {
		commandR = COM_CLC;
	    } else if (objectI instanceof ControllerInterface) {
		commandR = COM_CTL;
	    } else if (objectI instanceof DeleteChannels) {
		commandR = COM_DLC;
	    } else if (objectI instanceof EndOfStream) {
		commandR = COM_EOS;
	    } else if (objectI instanceof ExceptionMessage) {
		commandR = COM_EXM;
	    } else if (objectI instanceof Login) {
		commandR = COM_LGN;
	    } else if (objectI instanceof MirrorInterface) {
		commandR = COM_MIR;
	    } else if (objectI instanceof Path) {
		commandR = COM_PTH;
	    } else if (objectI instanceof PeerServer) {
		commandR = COM_PSR;
	    } else if (objectI instanceof PeerUpdate) {
		commandR = COM_PUP;
	    } else if (objectI instanceof PlugInInterface) {
		commandR = COM_PLG;
	    } else if (objectI instanceof Ping) {
		commandR = COM_PNG;
	    } else if (objectI instanceof Register) {
		commandR = COM_REG;
	    } else if (objectI instanceof Reset) {
		commandR = COM_RST;
	    } else if (objectI instanceof ReverseRoute) {
		commandR = COM_RVS;
	    } else if (objectI instanceof DataRequest) {
		commandR = COM_REQ;
	    } else if (objectI instanceof Registration) {
		commandR = COM_RGR;
	    } else if (objectI instanceof RequestOptions) {
		commandR = COM_RQO;
	    } else if (objectI instanceof RoutedMessage) {
		commandR = COM_RMS;
	    } else if (objectI instanceof RSVP) {
		commandR = COM_RSV;
	    } else if (objectI instanceof RoutingMapInterface) {
		commandR = COM_RTM;
	    } else if (objectI instanceof Seal) {
		commandR = COM_SEA;
	    } else if (objectI instanceof ServerInterface) {
		commandR = COM_SRV;
	    } else if (objectI instanceof Shortcut) {
		commandR = COM_SHC;
	    } else if (objectI instanceof SinkInterface) {
		commandR = COM_SNK;
	    } else if (objectI instanceof SourceInterface) {
		commandR = COM_SRC;
	    } else if (objectI instanceof Start) {
		commandR = COM_STR;
	    } else if (objectI instanceof Stop) {
		commandR = COM_STP;
	    } else if (objectI instanceof Username) {
		commandR = COM_USR;
	    } else if (objectI instanceof GetAddressAuthorization) {
		commandR = COM_GAA;
	    } else if (objectI instanceof SetAddressAuthorization) {
		commandR = COM_SAA;
		

	    } else if (objectI instanceof Rmap) {
		// This case must be last because a lot of the other classes
		// are subclasses of <code>Rmap</code> and if this check was
		// made too early, then the wrong code will get assigned to
		// some or all of the subclasses.
		commandR = COM_RMP;
	    }
	}

	return (commandR);
    }

    /**
     * Is the input <code>Serializable</code> a <code>Login</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return is the <code>Serializable</code> a <code>Login</code>?
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/07/2001  INB	Created.
     *
     */
    final static boolean isLogin(Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Login);
    }

    /**
     * Is the input <code>Serializable</code> a <code>Mirror</code> message?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return is the input a <code>Mirror</code>?
     * @see com.rbnb.api.Mirror
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    final static boolean isMirror(com.rbnb.api.Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Mirror);
    }

    /**
     * Is the input <code>Serializable</code> a ping message?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return is everything OK?
     * @see #ping()
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static boolean isOK(com.rbnb.api.Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Ping);
    }

    /**
     * Is the input <code>Serializable</code> a <code>Register</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return is the <code>Serializable</code> a <code>Register</code>?
     * @since V2.0
     * @version 09/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    final static boolean isRegister(Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Register);
    }

    /**
     * Is the input <code>Serializable</code> a request?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return is the <code>Serializable</code> a request?
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static boolean isRequest(Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Ask);
    }

    /**
     * Is the input <code>Serializable</code> a <code>Start</code> command?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return <code>Serializable</code> is a <code>Start</code> command?
     * @see #isStop(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static boolean isStart(Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Start);
    }

    /**
     * Is the input <code>Serializable</code> a <code>Stop</code> command?
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code>.
     * @return <code>Serializable</code> is a <code>Stop</code> command?
     * @see #isStart(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static boolean isStop(Serializable serializableI)
	throws SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (serializableI instanceof Stop);
    }

    /**
     * Gets a ping message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Serializable</code> ping.
     * @see #isOK(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static Serializable ping() {
	return (new Ping());
    }

    /**
     * Reads a <code>Serializable</code> object in.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Serializable</code> object read.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final Serializable read()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (read((Rmap) null));
    }

    /**
     * Reads a <code>Serializable</code> object in.
     * <p>
     * This method handles <code>Rmap</code> update messages using the input
     * <code>Rmap</code> to provide the basis of the updates.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @return the <code>Serializable</code> object read.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(com.rbnb.api.Serializable,com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final Serializable read(Rmap otherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable objectR = read(otherI,getIcontrol(),getIdata());
	if (objectR == null) {
	    throw new com.rbnb.api.SerializeException
		("Software error:\nUnrecognized command received.");
	}

	return (objectR);
    }

    /**
     * Reads a <code>Serializable</code>object from the specified streams.
     * <p>
     * This method takes an <code>Rmap</code> that can be used to fill in
     * default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param isI    the <code>InputStream</code>.
     * @param disI   the <code>DataInputStream</code>.
     * @return the object read or null if a closing bracket was seen.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(com.rbnb.api.Serializable,com.rbnb.api.Rmap,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 07/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    final static Serializable read(Rmap otherI,
				   InputStream isI,
				   DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return ((Serializable) readObject(otherI,isI,disI));
    }

    /**
     * Reads the data for an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeData(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 02/25/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Added support for <code>Pings</code> with data.
     * 08/15/2001  INB	Created.
     *
     */
    final void readData(Serializable objectI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getIdata() != null) {
	    if (objectI instanceof Ping) {
		((Ping) objectI).readData(getIdata());
	    } else if (objectI instanceof Rmap) {
		((Rmap) objectI).readData(getIdata());
	    } else if (objectI instanceof RoutedMessage) {
		((RoutedMessage) objectI).readData(getIdata());
	    } else if (objectI instanceof RSVP) {
		((RSVP) objectI).readData(getIdata());
	    } else if (objectI instanceof Register) {
		((Register) objectI).readData(getIdata());
	    }
	}
    }

    /**
     * Reads an object from the specified streams.
     * <p>
     * This method takes an <code>Rmap</code> that can be used to fill in
     * default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param isI    the <code>InputStream</code>.
     * @param disI   the <code>DataInputStream</code>.
     * @return the object read or null if a closing bracket was seen.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeObject(java.lang.Object,com.rbnb.api.Rmap,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/23/2001  INB	Created.
     *
     */
    final static Object readObject(Rmap otherI,
				   InputStream isI,
				   DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int command = Serialize.readParameter(COMMANDS,isI);

	//System.err.print(command + " ");

	if (Thread.currentThread().interrupted()) {
	    throw new java.io.InterruptedIOException();
	}

	Object objectR = null;
	Rmap rOther = otherI;

	if (command == -1) {
	    if (isI.getInfoMessage() != null) {
		throw new com.rbnb.api.SerializeException
		    ("Failed to read an object from the input stream.\n" +
		     isI.getInfoMessage());
	    }

	} else {
	    if ((command != COM_RMU) &&
		(command != identifyObject(otherI))) {
		rOther = null;
	    }

	    switch (command) {
	    case COM_OBJ:
		int length = isI.readInt();
		byte[] bArray = new byte[length];
		isI.read(bArray);
		java.io.ObjectInputStream ois =
		    new java.io.ObjectInputStream
			(new java.io.ByteArrayInputStream(bArray));
		try {
		    objectR = ois.readObject();
		} catch (java.lang.ClassNotFoundException e) {
		    throw new com.rbnb.api.SerializeException
			("Software error:\n" + e.getMessage());
		}
		ois.close();
		break;

	    case COM_RMS:
		objectR = readSerializable(rOther,command,isI,disI);
		break;

	    case COM_RMU:
		java.util.Vector vector = readRmapUpdate(rOther,
							 Integer.MIN_VALUE,
							 true,
							 isI,
							 disI);
		command = ((Integer) vector.firstElement()).intValue();
		if (command != COPY) {
		    throw new com.rbnb.api.SerializeException
			("Software error:\n" +
			 "Unexpected Rmap update message received.");
		}
		int index = ((Integer) vector.elementAt(1)).intValue();
		if (index != -1) {
		    throw new com.rbnb.api.SerializeException
			("Software error:\n" +
			 "Unexpected Rmap copy index received: " + index);
		}
		objectR = vector.lastElement();
		break;

	    case COM_RSV:
		objectR = readSerializable(rOther,command,isI,disI);
		break;

	    default:
		objectR = readSerializable(null,command,isI,disI);
		break;
	    }
	}

	//System.err.print((objectR != null) + " ");

	//System.err.println("Read: " + objectR);

	return (objectR);
    }

    /**
     * Reads a <code>Serializable</code> from the specified streams.
     * <p>
     * This method takes an <code>Rmap</code> that can be used to fill in
     * default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI   the other <code>Rmap</code>.
     * @param commandI the command read.
     * @param isI      the <code>InputStream</code>.
     * @param disI     the <code>DataInputStream</code>.
     * @return the <code>Serializable</code> read or null if a closing bracket
     *	       was seen.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeObject(java.lang.Object,com.rbnb.api.Rmap,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Added support for <code>ClearCache</code>.
     * 07/30/2003  INB	Added support for <code>DeleteChannels</code>.
     * 06/11/2003  INB	Added support for <code>RequestOptions</code>.
     * 07/31/2001  INB	Created.
     *
     */
    private final static Serializable readSerializable(Rmap otherI,
						       int commandI,
						       InputStream isI,
						       DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable objectR = null;

	switch (commandI) {
	case COM_ACK:
	    objectR = new Acknowledge(isI,disI);
	    break;

	case COM_ASK:
	    objectR = new Ask(isI,disI);
	    break;

	case COM_CLC:
	    objectR = new ClearCache(isI,disI);
	    break;

	case COM_CTL:
	    objectR = new ControllerIO(otherI,isI,disI);
	    break;

	case COM_DLC:
	    objectR = new DeleteChannels(isI,disI);
	    break;

	case COM_EOS:
	    objectR = new EndOfStream(otherI,isI,disI);
	    break;

	case COM_EXM:
	    objectR = new ExceptionMessage(isI,disI);
	    break;

	case COM_LGN:
	    objectR = new Login(otherI,isI,disI);
	    break;

	case COM_MIR:
	    objectR = new MirrorIO(isI,disI);
	    break;

	case COM_SHC:
	    objectR = new ShortcutIO(otherI,isI,disI);
	    break;

	case COM_PLG:
	    objectR = new PlugInIO(otherI,isI,disI);
	    break;

	case COM_PNG:
	    objectR = new Ping(isI,disI);
	    break;

	case COM_PSR:
	    objectR = new PeerServer(otherI,isI,disI);
	    break;

	case COM_PTH:
	    objectR = new Path(isI,disI);
	    break;

	case COM_PUP:
	    objectR = new PeerUpdate(isI,disI);
	    break;

	case COM_REG:
	    objectR = new Register(isI,disI);
	    break;

	case COM_REQ:
	    objectR = new DataRequest(otherI,isI,disI);
	    break;

	case COM_RGR:
	    objectR = new Registration(otherI,isI,disI);
	    break;

	case COM_RQO:
	    objectR = new RequestOptions(isI,disI);
	    break;

	case COM_RST:
	    objectR = new Reset(isI,disI);
	    break;

	case COM_RHD:
	    objectR = new RouterIO(otherI,isI,disI);
	    break;

	case COM_RMS:
	    objectR = new RoutedMessage(otherI,isI,disI);
	    break;

	case COM_RSV:
	    objectR = new RSVP(otherI,isI,disI);
	    break;

	case COM_RTM:
	    objectR = new RoutingMapIO(otherI,isI,disI);
	    break;

	case COM_SEA:
	    objectR = new Seal(isI,disI);
	    break;

	case COM_SNK:
	    objectR = new SinkIO(otherI,isI,disI);
	    break;

	case COM_SRC:
	    objectR = new SourceIO(otherI,isI,disI);
	    break;

	case COM_SRV:
	    objectR = new Server(otherI,isI,disI);
	    break;

	case COM_STR:
	    objectR = new Start(isI,disI);
	    break;

	case COM_STP:
	    objectR = new Stop(isI,disI);
	    break;

	case COM_RVS:
	    objectR = new ReverseRoute(isI,disI);
	    break;

	case COM_RMP:
	    objectR = new Rmap(otherI,isI,disI);
	    break;

	case COM_USR:
	    objectR = new Username(isI,disI);
	    break;
	    
	case COM_GAA:
	    objectR = new GetAddressAuthorization(isI, disI);
	    break;
	    
	case COM_SAA:
	    objectR = new SetAddressAuthorization(isI, disI);
	    break;
	}

	if (objectR == null) {
	    if ((commandI < 0) || (commandI >= COMMANDS.length)) {
		throw new com.rbnb.api.SerializeException
		    ("Software error:\nGot bad command: " + commandI);
	    } else {
		throw new com.rbnb.api.SerializeException
		    ("Software error:\nGot nothing on: " + COMMANDS[commandI]);
	    }
	}

	return (objectR);
    }

    /**
     * Reads an update for an <code>Rmap</code> from the stream.
     * <p>
     * The input <code>Rmap</code> is used to find the older entry to use to
     * read the object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>Rmap</code>.
     * @param offsetI offset to adjust the indexes to be the "correct"
     *		      location.
     * @param childI  should the entry be a child? If not, it is assumed to be
     *		      a member.
     * @param isI     the input stream.
     * @param disI    the data input stream.
     * @return null on a closing bracket or a vector containing one of:
     *	       <p><ul>
     *	       <li>two elements:
     *		   <p><ol start=0>
     *		   <li><code>Integer(ADD)</code>, and</li>
     *		   <li><code>Rmap</code>,</li>
     *		   </ol></li>
     *	       <li>two elements:
     *		   <p><ol start=0>
     *		   <li><code>Integer(REMOVE)</code>, and</li>
     *		   <li><code>Integer(index)</code>, or</li>
     *		   </ol></li>
     *	      <li>three elements:
     *		   <p><ol start=0>
     *		   <li><code>Integer(COPY or REMOVE)</code>,<li>
     *		   <li><code>Integer(index)</code>, and</li>
     *		   <li><code>Rmap</code>.</li>
     *		   </ol></li>
     *	      </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the read is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeRmapUpdate(int,com.rbnb.api.Rmap,com.rbnb.api.Rmap,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
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
    final static java.util.Vector readRmapUpdate(Rmap otherI,
						 int offsetI,
						 boolean childI,
						 InputStream isI,
						 DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.util.Vector vectorR = null;
	int command =
	    (offsetI == Integer.MIN_VALUE) ?
	    COM_RMU :
	    Serialize.readParameter(COMMANDS,isI);
	Object object;
	Serializable entry;
	Rmap entry1;

	if (command == -1) {
	    vectorR = null;

	} else if (command != COM_RMU) {
	    entry = readSerializable(null,command,isI,disI);
	    if (entry != null) {
		vectorR = new java.util.Vector();
		vectorR.addElement(new Integer(ADD));
		vectorR.addElement(entry);
	    }

	} else {
	    Serialize.readOpenBracket(isI);
	    vectorR = new java.util.Vector();
	    command = isI.readInt();
	    vectorR.addElement(new Integer(command));

	    if (command == ADD) {
		object = readObject(null,isI,disI);
		if (object == null) {
		    throw new com.rbnb.api.SerializeException
			("Software error:\nExpected object for RMU ADD.");
		}
		vectorR.addElement(object);
	    } else {
		int index = isI.readInt();
		vectorR.addElement(new Integer(index));

		switch (command) {
		case REMOVE:
		    break;

		case COPY:
		case EDIT:
		    command = Serialize.readParameter(COMMANDS,isI);
		    entry1 = ((index == -1) ? otherI :
			      (otherI == null) ? null :
			      (childI ?
			       otherI.getChildAt(index + offsetI) :
			       otherI.getMemberAt(index + offsetI)));
		    if (command != identifyObject(entry1)) {
			entry1 = null;
		    }
		    entry = readSerializable(entry1,
					     command,
					     isI,
					     disI);
		    if (entry == null) {
			throw new com.rbnb.api.SerializeException
			    ("Software error:\n" +
			     "Expected serializable for RMU " +
			     ((command == COPY) ? "COPY" : "EDIT") +
			     ".");
		    }
		    vectorR.addElement(entry);
		    break;
		}
	    }

	    if (Serialize.readParameter(null,isI) != -1) {
		throw new com.rbnb.api.SerializeException
		    ("Cannot find closing bracket that should terminate " +
		     "the Rmap update.");
	    }
	}

	return (vectorR);
    }

    /**
     * Sets the input control stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputControlI the input control stream.
     * @see #getIcontrol()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void setIcontrol(InputStream inputControlI) {
	inputControl = inputControlI;
    }

    /**
     * Sets the input data stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inputDataI the input data stream.
     * @see #getIdata()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void setIdata(DataInputStream inputDataI) {
	inputData = inputDataI;
    }

    /**
     * Sets the output control stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param outputControlI the output control stream.
     * @see #getOcontrol()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void setOcontrol(OutputStream outputControlI) {
	outputControl = outputControlI;
    }

    /**
     * Sets the output data stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param outputDataI the output data stream.
     * @see #getOdata()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void setOdata(DataOutputStream outputDataI) {
	outputData = outputDataI;
    }

    /**
     * Throws the exception contained in an <code>ExceptionMessage</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eMessageI  the <code>ExceptionMesage</code>.
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    final static void throwException(ExceptionMessage eMessageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Exception exception = eMessageI.toException();
	throwException(exception);
    }

    /**
     * Throws an exception using the "proper" type.
     * <p>
     *
     * @author Ian Brown
     *
     * @param exceptionI the exception.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/30/2001  INB	Created.
     *
     */
    final static void throwException(java.lang.Exception exceptionI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (exceptionI instanceof com.rbnb.api.AddressException) {
	    throw (com.rbnb.api.AddressException) exceptionI;
	} else if (exceptionI instanceof com.rbnb.api.SerializeException) {
	    throw (com.rbnb.api.SerializeException) exceptionI;
	} else if (exceptionI instanceof java.io.EOFException) {
	    throw (java.io.EOFException) exceptionI;
	} else if (exceptionI instanceof java.io.InterruptedIOException) {
	    throw (java.io.InterruptedIOException) exceptionI;
	} else if (exceptionI instanceof java.io.IOException) {
	    throw (java.io.IOException) exceptionI;
	} else if (exceptionI instanceof java.lang.ArithmeticException) {
	    throw (java.lang.ArithmeticException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.ArrayIndexOutOfBoundsException) {
	    throw (java.lang.ArrayIndexOutOfBoundsException) exceptionI;
	} else if (exceptionI instanceof java.lang.ArrayStoreException) {
	    throw (java.lang.ArrayStoreException) exceptionI;
	} else if (exceptionI instanceof java.lang.ClassCastException) {
	    throw (java.lang.ClassCastException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.IllegalThreadStateException) {
	    throw (java.lang.IllegalThreadStateException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.NumberFormatException) {
	    throw (java.lang.NumberFormatException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.IllegalArgumentException) {
	    throw (java.lang.IllegalArgumentException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.IllegalMonitorStateException) {
	    throw (java.lang.IllegalMonitorStateException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.IllegalStateException) {
	    throw (java.lang.IllegalStateException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.StringIndexOutOfBoundsException) {
	    throw (java.lang.StringIndexOutOfBoundsException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.IndexOutOfBoundsException) {
	    throw (java.lang.IndexOutOfBoundsException) exceptionI;
	} else if (exceptionI instanceof java.lang.InterruptedException) {
	    throw (java.lang.InterruptedException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.NegativeArraySizeException) {
	    throw (java.lang.NegativeArraySizeException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.NullPointerException) {
	    throw (java.lang.NullPointerException) exceptionI;
	} else if (exceptionI instanceof
		   java.lang.SecurityException) {
	    throw (java.lang.SecurityException) exceptionI;
	} else {
	    exceptionI.printStackTrace();
	    throw new java.lang.IllegalStateException
		(exceptionI.getMessage());
	}
    }

    /**
     * Writes out the input <code>Serializable</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI  the <code>Serializable</code> object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the write is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the write is interrupted.
     * @see #read()
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void write(Serializable objectI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	write(objectI,(Rmap) null);
    }

    /**
     * Writes out the input <code>Serializable</code> object.
     * <p>
     * This method takes the last <code>Rmap</code> written out. If the input
     * object is an instance of the same sub-class of <code>Rmap</code>, then
     * this method writes out only the differences between the two.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the <code>Serializable</code> object.
     * @param otherI  the other <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the write is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the write is interrupted.
     * @see #read(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 11/06/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final void write(Serializable objectI,Rmap otherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {

	/*
if (getOcontrol() == null) {
    throw new java.lang.IllegalStateException(this + " control is null.");
}
	*/

	write(objectI,
	      (((otherI != null) &&
		(otherI.getClass() == objectI.getClass())) ?
		otherI :
		null),
	      getOcontrol(),
	      getOdata());
    }

    /**
     * Writes out the input <code>Serializable</code> object.
     * <p>
     * This method takes the last <code>Rmap</code> written out. If the input
     * object is an instance of the same sub-class of <code>Rmap</code>, then
     * this method writes out only the differences between the two.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the <code>Serializable</code> object.
     * @param otherI  the other <code>Rmap</code>.
     * @param osI     the output stream.
     * @param dosI    the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the write is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the write is interrupted.
     * @see #read(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final static void write(Serializable objectI,
			    Rmap otherI,
			    OutputStream osI,
			    DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	writeObject(objectI,otherI,osI,dosI);
    }

    /**
     * Writes the data for an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the write is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the write is interrupted.
     * @see #readData(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 02/25/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Write <code>Pings</code> with data.
     * 08/15/2001  INB	Created.
     *
     */
    final void writeData(Serializable objectI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getOdata() != null) {
	    if (objectI instanceof Ping) {
		if (IsSupported.isSupported
		    (IsSupported.FEATURE_PINGS_WITH_DATA,
		     getOcontrol().getBuildVersion(),
		     getOcontrol().getBuildDate())) {
		    ((Ping) objectI).writeData(getOdata());
		}
	    } else if (objectI instanceof Rmap) {
		((Rmap) objectI).writeData(getOdata());
	    } else if (objectI instanceof RoutedMessage) {
		((RoutedMessage) objectI).writeData(getOdata());
	    } else if (objectI instanceof RSVP) {
		((RSVP) objectI).writeData(getOdata());
	    } else if (objectI instanceof Register) {
		((Register) objectI).writeData(getOdata());
	    }
	    getOdata().flush();
	}
    }

    /**
     * Writes out the input object.
     * <p>
     * This method takes the last <code>Rmap</code> written out. If the input
     * object is an instance of the same sub-class of <code>Rmap</code>, then
     * this method writes out only the differences between the two.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the object.
     * @param otherI  the other <code>Rmap</code>.
     * @param osI     the output stream.
     * @param dosI    the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the write is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the write is interrupted.
     * @see #readObject(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final static void writeObject(Object objectI,
				  Rmap otherI,
				  OutputStream osI,
				  DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {

	//System.err.println("Writing: " + objectI);

	int command = identifyObject(objectI);
	if (command != -1) {
	    if (otherI == null) {
		((Serializable) objectI).write(COMMANDS,command,osI,dosI);
	    } else {
		if (objectI instanceof Rmap) {
		    Rmap other = null;
		    if (otherI != null) {
			other = ((objectI.getClass() == otherI.getClass()) ?
				 otherI :
				 null);
		    }
		    writeRmapUpdate(-1,
				    (Rmap) objectI,
				    other,
				    osI,
				    dosI);

		} else if (objectI instanceof RoutedMessage) {
		    RoutedMessage rms = (RoutedMessage) objectI;
		    Rmap other = null;
		    if ((otherI != null) &&
			(rms.getMessage() != null)) {
			other = ((rms.getMessage().getClass() ==
				  otherI.getClass()) ?
				 otherI :
				 null);
		    }
		    rms.write(other,COMMANDS,command,osI,dosI);

		} else if (objectI instanceof RSVP) {
		    RSVP rsvp = (RSVP) objectI;
		    Rmap other = null;
		    if ((otherI != null) &&
			(rsvp.getSerializable() != null)) {
			other = ((rsvp.getSerializable().getClass() ==
				  otherI.getClass()) ?
				 otherI :
				 null);
		    }
		    rsvp.write(other,COMMANDS,command,osI,dosI);

		} else {
		    ((Serializable) objectI).write(COMMANDS,command,osI,dosI);
		}
	    }

	} else if (objectI instanceof java.io.Serializable) {
	    java.io.ByteArrayOutputStream baos =
		new java.io.ByteArrayOutputStream();
	    java.io.ObjectOutputStream oos =
		new java.io.ObjectOutputStream(baos);
	    oos.writeObject(objectI);
	    oos.flush();
	    byte[] bArray = baos.toByteArray();
	    oos.close();
	    osI.writeParameter(COMMANDS,COM_OBJ);
	    osI.writeInt(bArray.length);
	    osI.write(bArray);

	} else {
	    throw new java.lang.IllegalArgumentException
		(objectI + " is not serializable.");
	}

	osI.flush();
    }

    /**
     * Writes out an <code>Rmap</code> update message.
     * <p>
     * There are four types of <code>Rmap</code> update messages:
     * <p><ul>
     * <li>ADD a new <code>Rmap</code> that didn't previously exist,</li>
     * <li>COPY an existing <code>Rmap</code> to produce a new one,</li>
     * <li>EDIT an existing <code>Rmap</code>, or</li>
     * <li>REMOVE an existing <code>Rmap</code>.</li>
     * </ul><p>
     * The ADD operation is the default; it creates a completely new
     * <code>Rmap</code>. It is performed whenever none of the other operations
     * apply.
     * <p>
     * The COPY operation is used to create a new <code>Rmap</code> based on a
     * previous one. It is performed whenever the index is -1, there is both an
     * new and an old <code>Rmap</code>, and it cannot be quickly determined
     * that the two are significantly different.
     * <p>
     * The EDIT operation is used to change a sub-<code>Rmap</code> in an
     * <code>Rmap</code> hierarchy created by a Copy operation. It is used
     * under the same circumstances as the COPY operation, except that the
     * index must be > -1.
     * <p>
     * The REMOVE operation is used to remove a sub-<code>Rmap</code> from an
     * <code>Rmap</code> hierarchy created by a Copy operation. It is used in
     * the same circumstances as an EDIT, except that the two
     * <code>Rmaps</code> must be significantly different. Alternatively, it is
     * used whenever the is just an old <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idxI     the index of the <code>Rmap</code> within its parent
     *		       hierarchy.
     * @param oldI     the old <code>Rmap</code>.
     * @param newI     the new <code>Rmap</code>.
     * @param osI      the output stream.
     * @param dosI     the data output stream.
     * @return the operation performed.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if the object cannot be serialized.
     * @exception java.lang.InterruptedIOException
     *		  thrown if the write is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the write is interrupted.
     * @see #readRmapUpdate(com.rbnb.api.Rmap,int,boolean,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 10/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    final static int writeRmapUpdate(int idx,
				     Rmap newI,
				     Rmap oldI,
				     OutputStream osI,
				     DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int operationR;

	if (idx == -1) {
	    operationR = (newI.isSignificantlyDifferent(oldI) ? ADD : COPY);

	} else if (newI == null) {
	    operationR = REMOVE;

	} else if (oldI == null) {
	    operationR = ADD;

	} else {
	    boolean wasNSD = oldI.nameNSD;
	    int compared = (oldI.nameNSD ? 0 : newI.compareNames(oldI));

	    oldI.nameNSD = true;
	    operationR = ((compared < 0) ? ADD :
			  (compared > 0) ? REMOVE :
			  (newI.isSignificantlyDifferent(oldI) ?
			   REMOVE :
			   EDIT));
	    oldI.nameNSD = wasNSD;
	}

	long before = osI.getWritten();
	if (operationR == ADD) {
	    write(newI,null,osI,dosI);
	} else {
	    int valid = -1;
	    if (operationR == EDIT) {
		valid = osI.setStage(true,false);
		osI.addStaged(COMMANDS,COM_RMU,true);
		java.util.Vector fields = new java.util.Vector();
		fields.addElement(new Integer(operationR));
		fields.addElement(new Integer(idx));
		osI.addStaged(fields);
	    } else {
		osI.setStage(false,false);
		osI.writeParameter(COMMANDS,COM_RMU);
		Serialize.writeOpenBracket(osI);
		osI.writeInt(operationR);
		osI.writeInt(idx);
	    }

	    if (operationR != REMOVE) {
		int command = identifyObject(newI);
		if ((command < 0) || (command >= COMMANDS.length)) {
		    throw new com.rbnb.api.SerializeException
			("Software error:\n" +
			 newI + "\nproduced " + command + ".");
		}
		before = osI.getWritten();
		newI.write(oldI,COMMANDS,command,osI,dosI);
		if ((operationR == COPY) && (osI.getWritten() == before)) {
		    osI.writeParameter(COMMANDS,command);
		    Serialize.writeOpenBracket(osI);
		    Serialize.writeCloseBracket(osI);
		}
	    }

	    if ((operationR != EDIT) || (osI.getWritten() > before)) {
		Serialize.writeCloseBracket(osI);
	    } else if (valid >= 0) {
		osI.removeStaged(valid);
	    }
	}
	
	return (operationR);
    }
}
