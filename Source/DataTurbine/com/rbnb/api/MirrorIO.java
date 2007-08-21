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
 * Object that represents a mirror from one RBNB to another.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/01/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/17/2001  INB	Created.
 *
 */
class MirrorIO
    extends com.rbnb.api.Serializable
    implements com.rbnb.api.Mirror
{
    /**
     * the direction of the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #PULL
     * @see #PUSH
     * @since V2.0
     * @version 05/11/2001
     */
    private byte direction = PUSH;

    /**
     * the request to satisfy.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private DataRequest request = null;

    /**
     * remote <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Server remote = null;

    /**
     * destination <code>Source</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Source source = null;

    // Package constants:
    final static byte	PAR_DIR = 0,
			PAR_REM = 1,
			PAR_REQ = 2,
			PAR_SRC = 3;

    final static String[] PARAMETERS = {
			    "DIR",
			    "REM",
			    "REQ",
			    "SRC"
			};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #MirrorIO(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    MirrorIO() {
	super();
	setSource(new SourceIO());
    }

    /**
     * Class constructor to build a <code>MirrorIO</code> by reading it
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
     * @see #MirrorIO()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    MirrorIO(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	read(isI,disI);
    }

    /**
     * Clones this <code>MirrorIO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public Object clone() {
	MirrorIO clonedR = (MirrorIO) super.clone();

	if (clonedR != null) {
	    if (getRemote() != null) {
		try {
		    clonedR.setRemote
			(Server.newServerHandle
			 (getRemote().getName(),
			  getRemote().getAddress()));
		} catch (com.rbnb.api.AddressException e) {
		    throw new java.lang.InternalError();
		} catch (com.rbnb.api.SerializeException e) {
		    throw new java.lang.InternalError();
		} catch (java.io.IOException e) {
		    throw new java.lang.InternalError();
		} catch (java.lang.InterruptedException e) {
		    throw new java.lang.InternalError();
		}
	    }

	    if (getRequest() != null) {
		clonedR.setRequest((DataRequest) getRequest().clone());
	    }

	    if (getSource() != null) {
		clonedR.setSource((Source) ((Rmap) getSource()).clone());
	    }
	}

	return (clonedR);
    }

    /**
     * Gets the direction of the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the direction of the mirror.
     * @see #PULL
     * @see #PUSH
     * @see #setDirection(byte)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final byte getDirection() {
	return (direction);
    }

    /**
     * Gets the remote <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the remote <code>Server</code>.
     * @see #setRemote(com.rbnb.api.Server)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final Server getRemote() {
	return (remote);
    }

    /**
     * Gets the <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>DataRequest</code>.
     * @see #setRequest(com.rbnb.api.DataRequest)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final DataRequest getRequest() {
	return (request);
    }

    /**
     * Gets the <code>Source</code> object that is the destination of the
     * mirrored data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Source</code>.
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final Source getSource() {
	return (source);
    }

    /**
     * Reads the <code>MirrorIO</code> from the specified input stream.
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
     * @version 03/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
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
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_DIR:
		setDirection(isI.readByte());
		break;

	    case PAR_REM:
		setRemote(Server.newServerHandle(null,isI.readUTF()));
		break;

	    case PAR_REQ:
		setRequest(new DataRequest(isI,disI));
		break;

	    case PAR_SRC:
		setSource(new SourceIO(isI,disI));
		break;
	    }
	}
    }

    /**
     * Sets the direction of the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @param directionI  the direction of the mirror. This can be one of:
     *			  <br><ul>
     *			  <li><code>PULL</code> - get data from the remote
     *						  <code>Server</code>, or</li>
     *			  <li><code>PUSH</code> - send data to the remote
     *						  <code>Server</code>.</li>
     *			  </ul>
     * @see #getDirection()
     * @see #PULL
     * @see #PUSH
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final void setDirection(byte directionI) {
	direction = directionI;
    }

    /**
     * Sets the remote <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param remoteI  the remote <code>Server</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the remote <code>Server</code> is a
     *		  <code>RAMServer</code>.
     * @see #getRemote()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final void setRemote(Server remoteI) {
	remote = remoteI;
    }

    /**
     * Sets the <code>DataRequest</code>.
     * <p>
     * The <code>DataRequest</code> must be one that generates data with
     * monotonically increasing time-stamps.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>DataRequest</code>.
     * @see #getRequest()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final void setRequest(DataRequest requestI) {
	request = requestI;
    }

    /**
     * Sets the destination <code>Source</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceI  the destination <code>Source</code> object.
     * @see #getSource()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    final void setSource(Source sourceI) {
	source = sourceI;
    }

    /**
     * Returns a string representation of this <code>MirrorIO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 03/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final String toString() {
	String stringR = "Mirror ";

	if (getDirection() == PULL) {
	    stringR += "from ";
	} else if (getDirection() == PUSH) {
	    stringR += "to ";
	}

	if (getRemote() != null) {
	    stringR += getRemote().getAddress();
	}

	if (getRequest() != null) {
	    stringR += "\n" + getRequest();
	}

	if (getSource() != null) {
	    stringR += "\n" + getSource();
	}

	return (stringR);
    }

    /**
     * Writes this <code>MirrorIO</code> to the specified stream.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 03/01/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
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

	osI.writeParameter(PARAMETERS,PAR_DIR);
	osI.writeByte(getDirection());

	if (getRemote() != null) {
	    osI.writeParameter(PARAMETERS,PAR_REM);
	    if (getRemote().getAddress() instanceof String) {
		osI.writeUTF((String) getRemote().getAddress());
	    } else {
		throw new java.lang.IllegalArgumentException
		    ("Cannot handle mirror address " +
		     getRemote().getAddress() +
		     ".\n");
	    }
	}

	if (getRequest() != null) {
	    getRequest().write(PARAMETERS,PAR_REQ,osI,dosI);
	}

	if (getSource() != null) {
	    ((Serializable) getSource()).write(PARAMETERS,PAR_SRC,osI,dosI);
	}

	Serialize.writeCloseBracket(osI);
    }
}
