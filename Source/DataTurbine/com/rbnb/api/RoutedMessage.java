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
 * Routed message class.
 * <p>
 * This class is used to wrap the standard RBNB messages for transmission from
 * one RBNB server to another via routing (routing is described in more detail
 * in <code>RemoteServer</code>).  This class provides the routing information
 * needed to move the message from its source (generally a
 * <code>RemoteClient</code> on one machine) to its destination (generally
 * either the remote RBNB DataTurbine server or the
 * <code>RemoteClientHandler</code> at the other end of the virtual
 * connection).
 * <p>
 * The routing information consists of:
 * <p><ul>
 *    <li>The fully-qualified name of the source of the message,</li>
 *    <li>The fully-qualified name of the target of the message,</li>
 *    <li>The <code>Path</code> that the message should take to get from its
 *	  source RBNB DataTurbine server to its target,</li>
 *    <li>The index of the local RBNB DataTurbine within the <code>Path</code>,
 *	  and</li>
 *    <li>The message to be delivered.</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Path
 * @see com.rbnb.api.RemoteClient
 * @see com.rbnb.api.RemoteClientHandler
 * @see com.rbnb.api.RemoteServer
 * @since V1.2
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/05/2004  INB	Updated documentation.
 * 11/21/2001  INB	Created.
 *
 */
final class RoutedMessage
    extends com.rbnb.api.Serializable
{
    /**
     * the index into the <code>Path</code> representing the local
     * <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/21/2001
     */
    private int atIndex = 0;

    /**
     * the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/21/2001
     */
    private Serializable message = null;

    /**
     * the <code>Path</code> to take to deliver the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/21/2001
     */
    private Path path = null;

    /**
     * the source of the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */
    private String source = null;

    /**
     * the target of the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/28/2001
     */
    private String target = null;

    // Class constants:
    private final static int PAR_ATI = 0,
			     PAR_MSG = 1,
			     PAR_PTH = 2,
			     PAR_SRC = 3,
			     PAR_TGT = 4;

    private final static String[] PARAMETERS = {
			    "ATI",
			    "MSG",
			    "PTH",
			    "SRC",
			    "TGT"
			};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    RoutedMessage() {
	super();
    }

    /**
     * Class constructor to build a <code>RoutedMessage</code> by reading it
     * from input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the command input stream.
     * @param disI the data input stream.
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
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    RoutedMessage(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>RoutedMessage</code> by deserializing
     * it.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the last <code>Rmap</code> received.
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
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    RoutedMessage(Rmap rmapI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(rmapI,isI,disI);
    }

    /**
     * Gets the index of the local <bold>RBNB</bold> server within the
     * <code>Path</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the at index.
     * @see #setAtIndex(int)
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final int getAtIndex() {
	return (atIndex);
    }

    /**
     * Gets the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Serializable</code> message.
     * @see #setMessage(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final Serializable getMessage() {
	return (message);
    }

    /**
     * Gets the <code>Path</code> the message should take.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Path</code>.
     * @see #setPath(com.rbnb.api.Path)
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final Path getPath() {
	return (path);
    }

    /**
     * Gets the source of the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the source name.
     * @see #setSource(String)
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final String getSource() {
	return (source);
    }

    /**
     * Gets the target of the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the target string.
     * @see #setTarget(String)
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final String getTarget() {
	return (target);
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
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
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
	read(null,isI,disI);
    }

    /**
     * Reads the object from an input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the previous <code>Rmap</code> read.
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
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final void read(Rmap rmapI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the
	// <code>RoutedMessage</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_ATI:
		setAtIndex(isI.readInt());
		break;

	    case PAR_MSG:
		setMessage(Language.read(rmapI,isI,disI));
		break;

	    case PAR_PTH:
		setPath(new Path(isI,disI));
		break;

	    case PAR_SRC:
		setSource(isI.readUTF());
		break;

	    case PAR_TGT:
		setTarget(isI.readUTF());
		break;
	    }
	}
    }

    /**
     * Reads the data for this <code>RoutedMessage</code> and its children.
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
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
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
	if (getMessage() instanceof Rmap) {
	    ((Rmap) getMessage()).readData(disI);
	}
    }

    /**
     * Sets the index of the local <bold>RBNB</bold> server within the
     * <code>Path</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param atIndexI the at index.
     * @see #getAtIndex()
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void setAtIndex(int atIndexI) {
	atIndex = atIndexI;
    }

    /**
     * Sets the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the <code>Serializable</code> message.
     * @see #getMessage()
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void setMessage(Serializable messageI) {
	message = messageI;
    }

    /**
     * Sets the <code>Path</code> that the message should take.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pathI the <code>Path</code> for the message.
     * @see #getPath()
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void setPath(Path pathI) {
	path = pathI;
    }

    /**
     * Sets the source of the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceI the source name.
     * @see #getSource()
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void setSource(String sourceI) {
	source = sourceI;
    }

    /**
     * Sets the target of the message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param targetI the target name.
     * @see #getTarget()
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void setTarget(String targetI) {
	target = targetI;
    }

    /**
     * Returns a string representation of this <code>RoutedMessage</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/03/2001  INB	Created.
     *
     */
    public final String toString() {
	String stringR = "Routed: " + getMessage();

	stringR += "\nFrom: " + getSource();
	stringR += "\nTo: " + getTarget();
	stringR += "\nVia: " + getPath();
	stringR += "\nAt: " + getAtIndex();

	return (stringR);
    }

    /**
     * Writes this <code>RoutedMessage</code> to the specified stream.
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
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void write(String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	write(null,parametersI,parameterI,osI,dosI);
    }

    /**
     * Writes this <code>RoutedMessage</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI	   the previous <code>Rmap</code> transmitted.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 12/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
     *
     */
    final void write(Rmap rmapI,
		     String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);

	if (getMessage() != null) {
	    osI.writeParameter(PARAMETERS,PAR_MSG);
	    Language.write(getMessage(),rmapI,osI,dosI);
	}

	if (getPath() != null) {
	    osI.writeParameter(PARAMETERS,PAR_ATI);
	    osI.writeInt(getAtIndex());
	    getPath().write(PARAMETERS,PAR_PTH,osI,dosI);
	}

	if (getSource() != null) {
	    osI.writeParameter(PARAMETERS,PAR_SRC);
	    osI.writeUTF(getSource());
	}

	if (getTarget() != null) {
	    osI.writeParameter(PARAMETERS,PAR_TGT);
	    osI.writeUTF(getTarget());
	}
    
	Serialize.writeCloseBracket(osI);
    }

    /**
     * Writes the data for this <code>RoutedMessage</code> and its children.
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
     * @version 11/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/21/2001  INB	Created.
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
	if (getMessage() instanceof Rmap) {
	    ((Rmap) getMessage()).writeData(dosI);
	}
    }
}
