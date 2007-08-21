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
 * Internal class to represent addresses and their permissions.
 * <p>
 * <code>AddressPermissions</code> are used by
 * <code>AddressAuthorization</code> objects to explicitly grant specific
 * permissions (such as control, sink routing, and source connections).
 * Permissions not explicitly granted are denied.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.AddressAuthorization
 * @since V2.3
 * @version 09/28/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/23/2007  WHF	Fixed ALL_MASK so that it includes WRITE.  Changed toString
 *                	output for easier parsing.
 * 09/28/2004  JPW	To make the code Java 1.1.4 compliant (in order to
 *			compile under J#), remove the use of
 *			Character.toString() in method toString()
 * 08/13/2004  INB	Added documentation and link to AddressAuthorization.
 * 04/28/2004  INB	Created.
 *
 */
final class AddressPermissions {

    /**
     * access permission characters.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/29/2004
     */
    public final static char[] ACCESS_VALUES = {
	'X',
	'P',
	'T',
	'R',
	'W'
    };

    /**
     * the address string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    private String address = null;

    /**
     * all access mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 07/23/2007
     */
    public final static int ALL_MASK = 0x1f;

    /**
     * the permissions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    private int permissions = ALL_MASK;

    /**
     * control access bit.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    public final static int CONTROL = 0;


    /**
     * the localhost address.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/28/2004
     */
    private static String Localhost = null;

    /**
     * the localhost wildcard.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    private static com.rbnb.utility.Wildcards Localhost_Wildcard = null;

    /**
     * plugin access bit.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    public final static int PLUGIN = 1;

    /**
     * router access bit.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    public final static int ROUTER = 2;

    /**
     * sink access bit.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    public final static int SINK = 3;

    /**
     * sink access bit.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    public final static int SOURCE = 4;

    /**
     * the address wildcard matching string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3
     * @version 04/28/2004
     */
    private com.rbnb.utility.Wildcards wildcard = null;

    /**
     * Builds an <code>AddressPermissions</code> from an input string.
     * <p>
     * The strings take the form:
     * <p>
     * address[=permissions]
     * <p>
     * Where:
     * <p><ul>
     *    <li>address is the string to match connection addresses
     *	  against, and</li>
     *    <li>permissions is the optional list of permissions to allow
     *	  (default is all).</li>
     * </p>
     *
     * @author Ian Brown
     *
     * @param stringI the string containing the address and its
     *		  permissions.
     * @exception java.lang.Exception
     *		  if an error occurs.
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public AddressPermissions(String stringI)
	throws java.lang.Exception
    {
	super();

	int eql = stringI.indexOf("=");

	if (eql == -1) {
	    // If there is no equals (=), then the entire thing is the
	    // address.
	    setAddress(stringI);

	} else if (eql == 0) {
	    // No actual address was provided.
	    throw new java.lang.IllegalArgumentException
		("No address provided in " + stringI);

	} else {
	    // Otherwise, we have permissions as well as an address.
	    String permString = stringI.substring(eql + 1);

	    if (permString.length() == 0) {
		// If there are no permissions provided, then that's an
		// error.
		throw new java.lang.IllegalArgumentException
		    ("No permissions provided with address " +
		     "epecification " + stringI);
	    }

	    // Turn on the appropriate permissions bits.
	    setPermissions(0x00);
	    char permChar;
	    int idx1;

	    for (int idx = 0; idx < permString.length(); ++idx) {
		// Determine which permission is being granted by each
		// character in the permissions string.
		permChar = permString.charAt(idx);

		// Find the matching access character.
		for (idx1 = 0; idx1 < ACCESS_VALUES.length; ++idx1) {
		    if (permChar == ACCESS_VALUES[idx1]) {
			break;
		    }
		}

		if (idx1 == ACCESS_VALUES.length) {
		    // No matching character was found, throw an exception.
		    throw new java.lang.IllegalArgumentException
			("Unrecognized permission access character in " +
			 stringI);
		}

		// Set the corresponding bit.
		setPermissions(getPermissions() | (0x01 << idx1));
	    }

	    // Set the address.
	    setAddress(stringI.substring(0,eql));
	}

	if (getAddress().equals("*")) {
	    // If this is the default wildcard address, then use the
	    // default wildcard.
	    setWildcard(AddressAuthorization.Wildcard);

	} else if (getAddress().equalsIgnoreCase("localhost")) {
	    // If the address is localhost, then map it to the actual localhost
	    // address.
	    if (Localhost == null) {
		Localhost = TCP.buildAddress("localhost");
		Localhost = Localhost.substring(6,Localhost.lastIndexOf(":"));
		Localhost_Wildcard = new com.rbnb.utility.Wildcards
		    (Localhost);
	    }
	    setAddress(Localhost);
	    setWildcard(Localhost_Wildcard);

	} else {
	    // Otherwise, create a new wildcard.
	    setWildcard(new com.rbnb.utility.Wildcards(getAddress()));
	}
    }

    /**
     * Determines if a specific access is allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param accessI the access bit desired.
     * @return access allowed?
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final boolean allowAccess(int accessI) {
	int mask = 1 << accessI;

	return ((getPermissions() & mask) == mask);
    }

    /**
     * Gets the address string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address string.
     * @see #setAddress(String addressI)
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final String getAddress() {
	return (address);
    }

    /**
     * Gets the access permissions bit mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the permissions.
     * @see #setPermissions(int permissionsI)
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final int getPermissions() {
	return (permissions);
    }

    /**
     * Gets the wildcard.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the wildcard.
     * @see #setWildcard(com.rbnb.utility.Wildcards wildcardI)
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final com.rbnb.utility.Wildcards getWildcard() {
	return (wildcard);
    }

    /**
     * How wild is this <code>AddressSpecification</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the wildness rating.
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Moved from <code>AddressAuthorization</code> to this
     *			class and changed to operate on the current object.
     * 10/11/2002  INB	Created.
     *
     */
    final double howWild() {
	String wildChars =
	    com.rbnb.utility.Wildcards.getWildcardCharacters();
	int count = 0;

	for (int idx = 0; idx < getAddress().length(); ++idx) {
	    if (wildChars.indexOf(getAddress().charAt(idx)) != -1) {
		++count;
	    }
	}

	return (count/((double) getAddress().length()));
    }

    /**
     * Is this address specification more specific than the input?
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address specification.
     * @return is this address more specific?
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Moved from <code>AddressAuthorization</code> to this
     *			class and changed to operate on the current object.
     * 10/11/2002  INB	Created.
     *
     */
    final boolean isMoreSpecific(AddressPermissions addressI) {
	boolean moreR = false;

	if (!addressI.getWildcard().isConstant()) {
	    if (getWildcard().isConstant()) {
		moreR = true;
	    } else {
		moreR = howWild() <= addressI.howWild();
	    }
	}

	return (moreR);
    }

    /**
     * Sets the address string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address string.
     * @see #getAddress()
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final void setAddress(String addressI) {
	address = addressI;
    }

    /**
     * Sets the access permissions mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @param permissionsI the permissions.
     * @see #getPermissions()
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final void setPermissions(int permissionsI) {
	permissions = permissionsI;
    }

    /**
     * Sets the wildcard.
     * <p>
     *
     * @author Ian Brown
     *
     * @param wildcardI the wildcard.
     * @see #getWildcard()
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     *
     */
    public final void setWildcard(com.rbnb.utility.Wildcards wildcardI) {
	wildcard = wildcardI;
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @since V2.3
     * @version 09/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created.
     * 09/28/2004  JPW  To make the code Java 1.1.4 compliant (in order to
     *			compile under J#), remove the use of
     *			Character.toString()
     * 07/23/2007  WHF  Changed to better support parsing.
     */
    public final String toString() {
	StringBuffer sb = new StringBuffer(//"Address: " + 
			getAddress());

	if (permissions != 0) {
	    //sb.append(" Permissions: ");
	    sb.append('=');
	    for (int idx = 0; idx < ACCESS_VALUES.length; ++idx) {
		if (allowAccess(idx)) {
		    // JPW 09/28/2004: To make the code Java 1.1.4 compliant (in
		    //                 order to compile under J#), remove the use
		    //                 of Character.toString()
		    // sb.append(Character.toString(ACCESS_VALUES[idx]));
		    //char charData[] = { ACCESS_VALUES[idx] };
		    //String tempStr = new String(charData);
		    //sb.append(tempStr);
		    // WHF 07/23/2007  Creating a String here is wasteful.
		    sb.append(ACCESS_VALUES[idx]);
		}
	    }
	}

	return (sb.toString());
    }
}
