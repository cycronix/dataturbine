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
 * Request for an acknowledgement of receipt.
 * <p>
 * The use of <code>RSVPs</code> and their response <code>Acknowledge</code>
 * messsages, see <code>Acknowledge</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Acknowledge
 * @since V2.0
 * @version 08/13/2004
 */

/*
 * Copyright 2001, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/13/2004  INB	Added link to Acknowldge.
 * 04/07/2004  INB	Display the ID vector in toString.
 * 07/08/2003  INB	Added the multiple level identification vector to
 *			handle a hierarchy of local listeners such as will be
 *			produced when a fully-qualified name is used within a
 *			child server.
 * 06/07/2001  INB	Created.
 *
 */
class RSVP
    extends com.rbnb.api.Serializable
{
    /**
     * the identification of the requestor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private long identification = 0;

    /**
     * multiple level identification vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/08/2003
     */
    private java.util.Vector idVector = null;

    /**
     * the <code>Serializable</code> element tagged with the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Serializable serializable = null;

    // Private constants:
    private final static byte PAR_IDN = 0,
			      PAR_SER = 1;

    private final static String[] PARAMETERS = {
				    "IDN",
				    "SER"
				};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    RSVP() {
	super();
    }

    /**
     * Class constructor to build an <code>RSVP</code> for a particular
     * <code>identification</code> and <code>Serializable</code>.
     * <p>>
     *
     * @author Ian Brown
     *
     * @param identificationI identification of the object making the request.
     * @param serializableI   the <code>Serializable</code>.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    RSVP(long identificationI,Serializable serializableI) {
	this();
	setIdentification(identificationI);
	setSerializable(serializableI);
    }

    /**
     * Class constructor to build a <code>RSVP</code> by deserializing it.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the input stream.
     * @param disI the data input stream.
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
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    RSVP(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Class constructor to build a <code>RSVP</code> by deserializing it.
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
     * @version 09/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    RSVP(Rmap rmapI,InputStream isI,DataInputStream disI)
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
     * Gets the identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the requesting object's identification.
     * @see #setIdentification(long)
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final long getIdentification() {
	return (identification);
    }

    /**
     * Gets the multiple level of identification vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the vector.
     * @see #setIDVector(java.util.Vector)
     * @since V2.2
     * @version 07/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/08/2003  INB	Created.
     *
     */
    java.util.Vector getIDVector() {
	return (idVector);
    }

    /**
     * Gets the <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Serializable</code>.
     * @see #setSerializable(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final Serializable getSerializable() {
	return (serializable);
    }

    /**
     * Removes and returns a multiple level of identification value.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the current level of identification.
     * @see #pushIdentification(long)
     * @since V2.2
     * @version 07/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/08/2003  INB	Created.
     *
     */
    final long popIdentification() {
	long levelR = 0;

	if ((getIDVector() != null) && (getIDVector().size() > 0)) {
	    Long level;
	    level = (Long) getIDVector().lastElement();
	    getIDVector().removeElementAt(idVector.size() - 1);
	    levelR = level.longValue();
	}

	return (levelR);
    }

    /**
     * Adds a multiple level of identification value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param identificationI the current level identification.
     * @see #popIdentification
     * @since V2.2
     * @version 07/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/08/2003  INB	Created.
     *
     */
    final void pushIdentification(long identificationI) {
	if (getIDVector() == null) {
	    setIDVector(new java.util.Vector());
	}
	getIDVector().addElement(new Long(identificationI));
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
     * @version 09/25/2001
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
     * @version 09/25/2001
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
	// Read the open bracket marking the start of the <code>RSVP</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_IDN:
		setIdentification(isI.readLong());
		break;

	    case PAR_SER:
		setSerializable(Language.read(rmapI,isI,disI));
		break;
	    }
	}
    }

    /**
     * Reads the data for this <code>RSVP</code> and its children.
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
	if (getSerializable() instanceof Rmap) {
	    ((Rmap) getSerializable()).readData(disI);
	}
    }

    /**
     * Sets the identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param identificationI the identification of the requesting object.
     * @see #getIdentification()
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final void setIdentification(long identificationI) {
	identification = identificationI;
    }

    /**
     * Sets the multiple level of identification vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idVectorI the new vector.
     * @see #getIDVector()
     * @since V2.2
     * @version 07/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/08/2003  INB	Created.
     *
     */
    final void setIDVector(java.util.Vector idVectorI) {
	idVector = idVectorI;
    }

    /**
     * Sets the <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code>.
     * @see #getSerializable()
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
     *
     */
    final void setSerializable(Serializable serializableI) {
	serializable = serializableI;
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 04/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2004  INB	Display the ID vector.
     * 06/08/2001  INB	Created.
     *
     */
    public final String toString() {
	String className = getClass().toString();
	className = className.substring
	    (className.lastIndexOf(".") + 1);

	return (className +
		" ID: " + getIdentification() +
		" " + getIDVector() + " = " +
		" " + getSerializable());
    }

    /**
     * Writes this <code>RSVP</code> to the specified stream.
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
     * @version 09/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
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
     * Writes this <code>RSVP</code> to the specified stream.
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
     * @version 09/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  INB	Created.
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

	if (getIdentification() != 0) {
	    osI.writeParameter(PARAMETERS,PAR_IDN);
	    osI.writeLong(getIdentification());
	}

	if (getSerializable() != null) {
	    osI.writeParameter(PARAMETERS,PAR_SER);
	    Language.write(getSerializable(),rmapI,osI,dosI);
	}

	Serialize.writeCloseBracket(osI);
    }

    /**
     * Writes the data for this <code>RSVP</code> and its children.
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
    final void writeData(DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getSerializable() instanceof Rmap) {
	    ((Rmap) getSerializable()).writeData(dosI);
	}
    }
}
