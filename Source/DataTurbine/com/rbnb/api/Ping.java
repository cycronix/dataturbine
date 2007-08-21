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
 * Ping a <code>Server</code> or <code>Client</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/21/2003
 */

/*
 * Copyright 2001, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/21/2003  INB	Added <code>toString</code> method.
 * 02/25/2003  INB	Added support for data associated with the
 *			<code>Ping</code>.
 * 01/09/2001  INB	Created.
 *
 */
final class Ping
    extends com.rbnb.api.Serializable
{
    /**
     * the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/26/2003
     */
    private short data = -1;

    /**
     * should data accompany this <code>Ping</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/25/2003
     */
    private boolean hasData = false;

    // Class constants:
    private final static int PAR_DAT = 0;

    private final static String[] PARAMETERS = {
			    "DAT",
			};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    public Ping() {
	super();
    }

    /**
     * Class constructor to build a <code>Ping</code> with optional data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hasDataI does this <code>Ping</code> have data?
     * @param dataI    the data value.
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Created.
     *
     */
    public Ping(boolean hasDataI,short dataI) {
	super();
	setHasData(hasDataI);
	setData(dataI);
    }

    /**
     * Class constructor to build a <code>Ping</code> object from the specified
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
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #Ping()
     * @since V2.0
     * @version 02/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    Ping(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Clones this <code>Ping</code>.
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
     * Gets the data associated with this <code>Ping</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data.
     * @see #setData(short)
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Created.
     *
     */
    public final short getData() {
	return (data);
    }

    /**
     * Gets the has data flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return does this <code>Ping</code> have data?
     * @see #setHasData(boolean)
     * @since V2.1
     * @version 02/25/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Created.
     *
     */
    public final boolean getHasData() {
	return (hasData);
    }

    /**
     * Reads the <code>Ping</code> from the specified input stream.
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
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Added support for <code>PAR_DAT</code>.
     * 01/09/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException
    {
	// Read the open bracket marking the start of the <code>Ping</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_DAT:
		setHasData(isI.readBoolean());
		break;
	    }
	}
    }

    /**
     * Reads the data for this <code>Ping</code>.
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
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Created.
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
	if (getHasData()) {
	    setData(disI.readShort());
	}
    }

    /**
     * Sets the data value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI the data.
     * @see #getData()
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2003  INB	Created.
     *
     */
    public final void setData(short dataI) {
	data = dataI;
    }

    /**
     * Sets the has data flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hasDataI does this <code>Ping</code> have associated data.
     * @see #getHasData()
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Created.
     *
     */
    public final void setHasData(boolean hasDataI) {
	hasData = hasDataI;
    }

    /**
     * Returns a string representation of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.1
     * @version 03/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2003  INB	Created.
     *
     */
    public final String toString() {
	return ("Ping" + (getHasData() ? " data=" + getData() : ""));
    }

    /**
     * Writes this <code>Ping</code> to the specified stream.
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
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Added support for <code>PAR_DAT</code>.
     * 01/09/2001  INB	Created.
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

	if (IsSupported.isSupported(IsSupported.FEATURE_PINGS_WITH_DATA,
				    osI.getBuildVersion(),
				    osI.getBuildDate())) {
	    osI.writeParameter(PARAMETERS,PAR_DAT);
	    osI.writeBoolean(getHasData());
	}

	Serialize.writeCloseBracket(osI);
    }

    /**
     * Writes the data for this <code>Ping</code>.
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
     * @since V2.1
     * @version 02/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/25/2003  INB	Created.
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
	if (getHasData()) {
	    dosI.writeShort(getData());
	}
    }
}
