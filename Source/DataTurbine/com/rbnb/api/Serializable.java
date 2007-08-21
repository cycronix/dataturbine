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
 * Serializable class for RBNB objects.
 * <p>
 * This implementation of the <code>java.io.Serializable</code> class indicates
 * that an object is serializable either using the standard Java-style I/O
 * methods and also the RBNB-style methods defined by this class.
 * <p>
 * This is a class rather than an interface so that the methods (which are
 * really internal to <code>com.rbnb.api</code>) can be hidden.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/26/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/08/2001  INB	Created.
 *
 *
 */
abstract class Serializable
    implements java.io.Serializable,
	       java.lang.Cloneable
{
    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/07/2001  INB	Created.
     *
     */
    Serializable() {
	super();
    }

    /**
     * Clones this <code>Serializable</code> object.
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
     * 04/25/2001  INB	Created.
     *
     */
    public Object clone() {
	Serializable clonedR = null;

	try {
	    clonedR = (Serializable) super.clone();
	} catch (java.lang.CloneNotSupportedException e) {
	}

	return (clonedR);
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    abstract void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Writes the object to an output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the object's parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
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
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2001  INB	Created.
     *
     */
    abstract void write(String[] parametersI,
			int parameterI,
			OutputStream osI,
			DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Writes out staging information for this <code>Serializable</code>.
     * <p>
     * By staging <code>Serializable</code> objects, the I/O requirements are
     * reduced when writing collapsable objects such as <code>Rmaps</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI	  the output stream.
     * @param parametersI the parameters array.
     * @param parameterI  the index into the parameters array.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2001  INB	Created.
     *
     */
    void writeStaged(OutputStream osI,String[] parametersI,int parameterI)
	throws java.io.IOException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);
    }
}
