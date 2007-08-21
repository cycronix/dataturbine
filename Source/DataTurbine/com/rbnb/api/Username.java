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
 * Username class for the <bold>RBNB V2</bold> DataTurbine server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/17/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/17/2003  INB	Added <code>allowAccess(Username)</code>.
 * 01/14/2003  INB	Created.
 *
 */
public final class Username
    extends com.rbnb.api.Serializable
{

    /**
     * the password string or an indicator of how to get it.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private String password = null;

    /**
     * the username string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private String username = null;

    // Class constants:
    private final static int PAR_PWD = 0;
    private final static int PAR_USN = 1;

    private final static String[] PARAMETERS = {
			    "PWD",
			    "USN"
			};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
    public Username() {
	super();
    }

    /**
     * Class constructor to build a <code>Username</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
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
    Username(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>Username</code> object from
     * username and password strings.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the username string.
     * @param passwordI the password string.
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
    public Username(String usernameI,String passwordI) {
	this();
	setUsername(usernameI);
	setPassword(passwordI);
    }

    /**
     * Allow the specified <code>Username</code> access to this
     * <code>Username</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @return is the <code>Username</code> allowed access?
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
    public final boolean allowAccess(Username usernameI) {
	boolean allowAccessR = true;

	String[] roles = getRoles();
	if ((roles != null) &&
	    (roles.length > 0) &&
	    ((roles.length > 1) || (roles[0].length() > 0))) {
	    String[] otherRoles = ((usernameI == null) ?
				   null :
				   usernameI.getRoles());
	    if (allowAccessR = (otherRoles != null)) {
		allowAccessR = false;
		for (int idx = 0;
		     !allowAccessR && (idx < otherRoles.length);
		     ++idx) {
		    for (int idx1 = 0;
			 !allowAccessR && (idx1 < roles.length);
			 ++idx1) {
			allowAccessR = otherRoles[idx].equals(roles[idx1]);
		    }
		}
	    }
	}

	return (allowAccessR);
    }

    /**
     * Is this <code>Username</code> equal to the input one?
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Username</code>.
     * @return are the two equal?
     * @since V2.0
     * @version 01/14/2003a
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public final boolean isEqual(Username otherI) {
	return (getUsername().equals(otherI.getUsername()) &&
		getPassword().equals(otherI.getPassword()));
    }

    /**
     * Gets the password string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the password string.
     * @see #setPassword(String passwordI)
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
    public final String getPassword() {
	return (password);
    }

    /**
     * Gets the list of roles granted to this <code>Username</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of roles.
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
    public final String[] getRoles() {
	String[] rolesR = null;
	if (getPassword() != null) {
	    rolesR = new String[1];
	    rolesR[0] = getPassword();
	}

	return (rolesR);
    }

    /**
     * Gets the username string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the username string.
     * @see #setUsername(String usernameI)
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
    public final String getUsername() {
	return (username);
    }

    /**
     * Reads the <code>Username</code> from the specified input stream.
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
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the command of the
	// <code>Command</code>. 
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    readStandardParameter(parameter,isI,disI);
	}
    }

    /**
     * Reads standard parameters.
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
     * @see #writeStandardParameters(com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
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
    final boolean readStandardParameter(int parameterI,
					InputStream isI,
					DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean standardR = true;

	switch (parameterI) {
	case PAR_PWD:
	    setPassword(isI.readUTF());
	    break;

	case PAR_USN:
	    setUsername(isI.readUTF());
	    break;

	default:
	    standardR = false;
	    break;
	}

	return (standardR);
    }

    /**
     * Sets the password string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param passwordI the password string.
     * @see #getPassword()
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
    public final void setPassword(String passwordI) {
	password = passwordI;
    }

    /**
     * Sets the username string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the username string.
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
    public final void setUsername(String usernameI) {
	username = usernameI;
    }

    /**
     * Returns a string representation of this <code>Username</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
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
    public final String toString() {
	return ("Username: " + getUsername());
    }

    /**
     * Writes this <code>Username</code> to the specified stream.
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
	writeStandardParameters(osI,dosI);
	Serialize.writeCloseBracket(osI);
    }

    /**
     * Writes out standard parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI    the output stream.
     * @param dosI   the data output stream.
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
     * @see #readStandardParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
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
    final void writeStandardParameters(OutputStream osI,
				       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getUsername() != null) {
	    osI.writeParameter(PARAMETERS,PAR_USN);
	    osI.writeUTF(getUsername());
	}

	if (getPassword() != null) {
	    osI.writeParameter(PARAMETERS,PAR_PWD);
	    osI.writeUTF(getPassword());
	}
    }
}
