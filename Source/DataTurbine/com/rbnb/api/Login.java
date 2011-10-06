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

import java.text.ParsePosition;

/**
 * Login to an <code>RBNB</code> server.
 * <p>
 * This is the first message that should be sent to the RBNB server when an
 * application is connecting.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 07/21/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/06/2011  MJM  Handle parse exception in SimpleDateFormat (Android JVM)
 * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 05/09/2001  INB	Created.
 *
 */
final class Login
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.BuildInterface,
	       com.rbnb.api.UsernameInterface
{
    /**
     * the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private java.util.Date buildDate = null;

    /**
     * the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private String buildVersion = null;

    /**
     * the license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    //    private String licenseString = null;

    /**
     * the <code>Username</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private Username username = null;

    // Private constants:
    private final static byte PAR_BLD = 0;
    private final static byte PAR_BLV = 1;
    private final static byte PAR_LIC = 2;
    private final static byte PAR_USR = 3;

    private final static String[] PARAMETERS = {
				    "BLD",
				    "BLV",
				    "LIC",
				    "USR"
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
    Login() {
	super();
    }

    /**
     * Class constructor to build a <code>Login</code> by reading it
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
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    Login(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>Login</code> by reading it
     * from an input stream.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Login</code> as an <code>Rmap</code>.
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
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    Login(Rmap otherI,InputStream isI,DataInputStream disI)
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
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    String additionalToString() {
	String stringR = "";

	if (getBuildDate() != null) {
	    stringR += " Built: " + getBuildDate();
	}
	if (getBuildVersion() != null) {
	    stringR += " Version: " + getBuildVersion();
	}
	/*
	if (getLicenseString() != null) {
	    stringR += " License: " + getLicenseString();
	}
	*/
	if (getUsername() != null) {
	    stringR += " " + getUsername();
	}

	return (stringR);
    }

    /**
     * Adds the <code>Login's</code> parameters to the full serialization
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
     * 12/20/2001  INB	Created.
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultLoginParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Default <code>Login</code> parameters.
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
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    final void defaultLoginParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((otherI != null) && (otherI instanceof Login)) {
	    Login other = (Login) otherI;

	    if ((seenI == null) || !seenI[parametersStart + PAR_BLD]) {
		setBuildDate(other.getBuildDate());
	    }
	    if ((seenI == null) || !seenI[parametersStart + PAR_BLV]) {
		setBuildVersion(other.getBuildVersion());
	    }
	    /*
	    if ((seenI == null) || !seenI[parametersStart + PAR_LIC]) {
		setLicenseString(other.getLicenseString());
	    }
	    */
	    if ((seenI == null) || !seenI[parametersStart + PAR_USR]) {
		setUsername(other.getUsername());
	    }
	}
    }

    /**
     * Gets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build date.
     * @see #setBuildDate(java.util.Date)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final java.util.Date getBuildDate() {
	return (buildDate);
    }

    /**
     * Gets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build version string.
     * @see #setBuildVersion(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final String getBuildVersion() {
	return (buildVersion);
    }

    /**
     * Gets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the license string.
     * @see #setLicenseString(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    /*
    public final String getLicenseString() {
	return (licenseString);
    }
    */

    /**
     * Gets the <code>Username</code>.
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Reads the <code>Login</code> from the specified input stream.
     * <p>
     * This method uses the input <code>Rmap</code> to provide default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Login</code> as an <code>Rmap</code>.
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
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
	    if (!readLoginParameter(parameter,isI,disI)) {
		readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads <code>Login</code> parameters.
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
     * @see #writeLoginParameters(com.rbnb.api.Login,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 07/21/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
     * 12/20/2001  INB	Created.
     *
     */
    final boolean readLoginParameter(int parameterI,
				      InputStream isI,
				      DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean loginR = false;

	if (parameterI >= parametersStart) {
	    loginR = true;

	    switch (parameterI - parametersStart) {
	    case PAR_BLD:
		try {
		    setBuildDate
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy HH:mm:ss z",	// MJM: z can cause parse exception?
			     java.util.Locale.US)).parse(isI.readUTF()));	
		} 
		catch (java.text.ParseException e) {
			System.err.println("OOPS exception on parse date: "+e.getMessage());
			setBuildDate(new java.util.Date());	// MJM 10/6/11: avoid parseException?
//		    throw new com.rbnb.api.SerializeException(e.getMessage());
		}
		break;

	    case PAR_BLV:
		setBuildVersion(isI.readUTF());
		break;

	    /*
	    case PAR_LIC:
		setLicenseString(isI.readUTF());
		break;
	    */

	    case PAR_USR:
		setUsername(new Username(isI,disI));
		break;

	    default:
		loginR = false;
		break;
	    }
	}

	return (loginR);
    }

    /**
     * Sets the build date.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final void setBuildDate(java.util.Date buildDateI) {
	buildDate = buildDateI;
    }

    /**
     * Sets the build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version.
     * @see #getBuildVersion()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2001  INB	Created.
     *
     */
    public final void setBuildVersion(String buildVersionI) {
	buildVersion = buildVersionI;
    }

    /**
     * Sets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param licenseStringI the license string.
     * @see #getLicenseString()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    /*
    public final void setLicenseString(String licenseStringI) {
	licenseString = licenseStringI;
    }
    */

    /**
     * Sets the <code>Username</code>.
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
     * Writes this <code>Login</code> to the specified stream.
     * <p>
     * This method writes out the differences between this <code>Login</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>Login</code> as an
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
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
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
	writeLoginParameters((Login) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /** 
     * Writes out <code>Login</code> parameters.
     * <p>
     * This method writes out the differences between this <code>Login</code>
     * and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Login</code>.
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
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readLoginParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 07/21/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
     * 12/20/2001  INB	Created.
     *
     */
    final void writeLoginParameters(Login otherI,
				     OutputStream osI,
				     DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	if ((getBuildDate() != null) &&
	    ((otherI == null) ||
	     !getBuildDate().equals(otherI.getBuildDate()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_BLD);
	    osI.writeUTF
		((new java.text.SimpleDateFormat
		    ("MMM dd yyyy HH:mm:ss z",
		     java.util.Locale.US)).format(getBuildDate()));
	}

	if ((getBuildVersion() != null) &&
	    ((otherI == null) ||
	     !getBuildVersion().equals(otherI.getBuildVersion()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_BLV);
	    osI.writeUTF(getBuildVersion());
	}

	/*
	if ((getLicenseString() != null) &&
	    ((otherI == null) ||
	     !getLicenseString().equals(otherI.getLicenseString()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_LIC);
	    osI.writeUTF(getLicenseString());
	}
	*/

	if (IsSupported.isSupported(IsSupported.FEATURE_USERNAMES,
				    osI.getBuildVersion(),
				    osI.getBuildDate()) &&
	    (getUsername() != null)) {
	    getUsername().write(ALL_PARAMETERS,
				parametersStart + PAR_USR,
				osI,
				dosI);
	}
    }
}
