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
 * Provides access protection based on connection address.
 * <p>
 * Addresses are specified as a wildcard string match.  When adding/removing
 * addresses, the specification strings are compared to find an exact match.
 * <p>
 * Addresses can be allowed to do various things based on the permissions
 * granted.  If no permissions are specified, then all are granted.  Otherwise
 * only the specific permissions are granted (codes are case-insensitive):
 * <p><ul>
 *    <li><bold>X</bold> - control connections are allowed,</li>
 *    <li><bold>T</bold> - routing connections are allowed,</li>
 *    <li><bold>P</bold> - plug-in connections are allowed,</li>
 *    <li><bold>R</bold> - sink connections are allowed,</li>
 *    <li><bold>W</bold> - source connections are allowed.</li>
 * </ul><p>
 * It is not possible to deny specific permissions - simply do not grant them.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.AddressPermissions
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/23/2007  WHF      Updated documentation, changed output to facilitate
 *                      parsing.
 * 09/29/2004  JPW	In order to compile under J# (which is only compatable
 *			with Java 1.1.4), support for HTTPS has been disabled
 *			for reading the address authorization file.
 * 08/13/2004  INB	Added link to AddressPermissions.
 * 04/28/2004  INB	Added permissions handling.  Moved the localhost
 *			specification as well as the various methods used to
 *			to determine what specification is the most wild to
 *			the AddressPermissions class.
 * 10/11/2002  INB	Created.
 *
 */
final class AddressAuthorization {

    /**
     * addresses to grant authorization to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/11/2002
     */
    private java.util.Vector allow = new java.util.Vector();

    /**
     * addresses to deny access to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/11/2002
     */
    private java.util.Vector deny = new java.util.Vector();

    /**
     * wildcard match.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/28/2004
     */
    static com.rbnb.utility.Wildcards Wildcard = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The <code>Wildcard</code> field now simply holds a
     *			single <code>com.rbnb.utility.Wildcard</code>.
     *			Create a default <code>AddressPermissions</code>.
     * 10/11/2002  INB	Created.
     *
     */
    AddressAuthorization()
	throws java.lang.Exception
    {
	this((String) null);
	if (Wildcard == null) {
	    Wildcard = new com.rbnb.utility.Wildcards("*");
	}
	addAllow(new AddressPermissions("*"));
    }

    /**
     * Class constructor to build an <code>AddressAuthorization</code> object
     * by reading a file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param authFileI the name of the authorization file.
     * @exception java.io.IOException if an I/O error occurs.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The <code>Wildcard</code> field now simply holds a
     *			single <code>com.rbnb.utility.Wildcard</code>.
     * 10/11/2002  INB	Created.
     *
     */
    AddressAuthorization(String authFileI)
	throws java.io.IOException,
	       java.lang.Exception
    {
	super();
	if (Wildcard == null) {
	    Wildcard = new com.rbnb.utility.Wildcards("*");
	}
	if (authFileI != null) {
	    readFromFile(authFileI);
	}
   }

    /**
     * Class constructor to build an <code>AddressAuthorization</code> object
     * by reading an input stream.
     * <p>
     *
     * @author WHF
     *
     * @param stream  The input stream to parse.
     * @exception java.io.IOException if an I/O error occurs.
     * @since V3.0
     * @version 07/23/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/23/2007  WHF	Created.
     *
     */
    AddressAuthorization(java.io.InputStream stream)
	throws java.io.IOException, Exception
    {
	this((String) null);
	readFromStream(stream);
    }
   
    /**
     * Adds an address to the ALLOW list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address specification to allow.
     * @exception java.lang.Exception if an error occurs.
     * @see #removeAllow(com.rbnb.api.AddressPermissions)
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The address is now an <code>AddressPermissions</code>
     *			object rather than a string.  The localhost check is
     *			done in that class.
     * 10/11/2002  INB	Created.
     *
     */
    final void addAllow(AddressPermissions addressI)
	throws java.lang.Exception
    {
	// See if there is already an entry matching the address provided.
	boolean exists = false;
	AddressPermissions address = null;
	for (int idx = 0; idx < allow.size(); ++idx) {
	    address = (AddressPermissions) allow.elementAt(idx);
	    if (addressI.getAddress().equals(address.getAddress())) {
		exists = true;
		break;
	    }
	}

	if (!exists) {
	    // If not, eliminate any denied entry and add the new allowed
	    // entry.
	    removeDeny(addressI);
	    allow.addElement(addressI);

	} else {
	    // Otherwise, replace the permissions granted by the current entry
	    // with the ones granted by the new entry.
	    address.setPermissions(addressI.getPermissions());
	}
    }

    /**
     * Adds an address to the DENY list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address specification to allow.
     * @exception java.lang.Exception if an error occurs.
     * @see #removeDeny(com.rbnb.api.AddressPermissions)
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The address is now an <code>AddressPermissions</code>
     *			object rather than a string.  The localhost check is
     *			done in that class.
     * 10/11/2002  INB	Created.
     *
     */
    final void addDeny(AddressPermissions addressI)
	throws java.lang.Exception
    {
	if (addressI.getPermissions() != AddressPermissions.ALL_MASK) {
	    // Cannot deny specific permissions.
	    throw new java.lang.IllegalArgumentException
		("Cannot deny specific permissions - just turn on the ones " +
		 "you want to allow.\n" + addressI);
	}

	// See if there is already an entry matching the address provided.
	boolean exists = false;
	AddressPermissions address = null;
	for (int idx = 0; idx < deny.size(); ++idx) {
	    address = (AddressPermissions) deny.elementAt(idx);
	    if (addressI.getAddress().equals(address.getAddress())) {
		exists = true;
		break;
	    }
	}

	if (!exists) {
	    // If not, eliminate any allowed entry and add the new denied
	    // entry.  Turn off all permissions.
	    removeAllow(addressI);
	    addressI.setPermissions(0x00);
	    deny.addElement(addressI);
	}
    }

    /**
     * Finds the most specific <code>AddressPermissions</code> that applies
     * to the input address or addresses.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address(es) to check.
     * @return the <code>AddressPermissions</code> that applies or
     *	       <code>null</code> if no allowed entry matches.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.3
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Created from code moved from <code>isAllowed</code>.
     *
     */
    final AddressPermissions findAddressPermissions(Object addressI)
	throws java.lang.Exception
    {
	AddressPermissions bestR = null;

	String[] toCheck = null;
	int[] checked = null;

	// The input can be either a single address or an array of addresses.
	if (addressI instanceof String) {
	    toCheck = new String[1];
	    toCheck[0] = (String) addressI;

	} else if (addressI instanceof String[]) {
	    toCheck = (String[]) addressI;
	}

	AddressPermissions allowedBy;
	AddressPermissions deniedBy;
	for (int idx = 0; idx < toCheck.length; ++idx) {
	    // Check each of the addresses in turn.  All must be allowed for
	    // us to return anything other than a denial (null).
	    allowedBy = findBestMatch(toCheck[idx],allow);

	    if (allowedBy == null) {
		if ((bestR != null) && (bestR.getWildcard() == Wildcard)) {
		    // If the current address is not explicitly allowed, and
		    // we've only been allowed by the default (*) wildcard so
		    // far, then deny access.
		    bestR = null;
		}

	    } else {
		// If we are explicitly allowed by something, then we also need
		// to check for explicit denial.
		deniedBy = findBestMatch(toCheck[idx],deny);

		if (deniedBy == null) {
		    // If we aren't denied, then we must be allowed for this
		    // address.  Combine that with the best entry so far.
		    if ((bestR != null) &&
			(bestR.getPermissions() != 0x00)) {
			// If we were allowed before this, then choose the most
			// specific entry.
			if (allowedBy.isMoreSpecific(bestR)) {
			    bestR = allowedBy;
			}

		    } else {
			// If we weren't allowed before this, then we might be
			// allowed now if this entry is more specific.
			if ((bestR == null) ||
			    allowedBy.isMoreSpecific(bestR)) {
			    bestR = allowedBy;
			}
		    }

		} else {
		    // If we have both an allow and a deny, then we need to
		    // determine which is more specific and then compare that
		    // to the best entry found so far.

		    if (allowedBy.isMoreSpecific(deniedBy)) {
			// If the allowed entry is more specific, then combine
			// that with what we've gotten so far.
			if ((bestR != null) &&
			    (bestR.getPermissions() != 0x00)) {
			    // If we were allowed before this, then choose the
			    // most specific entry.
			    if (allowedBy.isMoreSpecific(bestR)) {
				bestR = allowedBy;
			    }

			} else {
			    // If we weren't allowed before this, then we might
			    // be allowed now if this entry is more specific.
			    if ((bestR == null) ||
				allowedBy.isMoreSpecific(bestR)) {
				bestR = allowedBy;
			    }
			}

		    } else {
			// If the denied entry is more specific, then combine
			// that with what we've gotten so far.

			if ((bestR != null) &&
			    (bestR.getPermissions() != 0x00)) {
			    // If we were allowed before this, then we might
			    // become denied.
			    if (!bestR.isMoreSpecific(deniedBy)) {
				bestR = deniedBy;
			    }

			} else {
			    // If we weren't allowed before this, then choose
			    // the most specific entry to represent that.
			    if ((bestR == null) ||
				deniedBy.isMoreSpecific(bestR)) {
				bestR = deniedBy;
			    }
			}
		    }
		}
	    }
	}

	return (bestR);
    }

    /**
     * Finds the most specific match for the input address in the input list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address to match.
     * @param listI    the list to search.
     * @return the most specific match.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The list now contains <code>AddressPermissions</code>
     *			objects, which are what we return.
     * 10/11/2002  INB	Created.
     *
     */
    private final static AddressPermissions findBestMatch
	(String addressI,
	 java.util.Vector listI)
	throws java.lang.Exception
    {
	AddressPermissions bestR = null;
	AddressPermissions address;

	for (int idx = 0; idx < listI.size(); ++idx) {
	    address = (AddressPermissions) listI.elementAt(idx);

	    if (address.getWildcard().matches(addressI) &&
		((bestR == null) || address.isMoreSpecific(bestR))) {
		bestR = address;
	    }
	}

	return (bestR);
    }

    /**
     * Is a specific type of access allowed for the input address(es)?
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address(es) to check.
     * @param accessI  the access permission desired.
     * @return access allowed?
     * @exception java.lang.Exception if an error occurs.
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
    final boolean isAccessAllowed(Object addressI,int accessI)
	throws java.lang.Exception
    {
	AddressPermissions best = findAddressPermissions(addressI);
	boolean allowedR;

	if ((best == null) || (best.getPermissions() == 0x00)) {
	    // If nothing is returned, or if what is returned has no
	    // permissions, then we're denied.
	    allowedR = false;

	} else {
	    // Otherwise, we need to check for the specific access desired.
	    allowedR = best.allowAccess(accessI);
	}

	return (allowedR);
    }

    /**
     * Is an (are the) address(es) allowed?
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address(es) to check.
     * @return grant access?
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The lists now contain <code>AddressPermissions</code>
     *			objects,  The method now uses the
     *			<code>findAddressPermissions</code> method.
     * 10/11/2002  INB	Created.
     *
     */
    final boolean isAllowed(Object addressI)
	throws java.lang.Exception
    {
	AddressPermissions best = findAddressPermissions(addressI);
	boolean allowedR;

	if ((best == null) || (best.getPermissions() == 0x00)) {
	    // If nothing is returned, or if what is returned has no
	    // permissions, then we're denied.
	    allowedR = false;

	} else {
	    // Otherwise, we're allowed to connect.
	    allowedR = true;
	}

	return (allowedR);
    }

    /**
     * Reads the list of allowed/denied addresses from a file.
     * <p>
     * This file is compatible with the routing file for <bold>RBNB</bold>
     * V1.x.
     * <p>
     *
     * @author Ian Brown
     *
     * @param authFileI the name of the authorization file (a URL).
     * @exception java.io.IOException if an I/O error occurs.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 09/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/23/2007  WHF  Now depends on readFromStream.
     * 09/29/2004  JPW  In order to compile under J# (which is only
     *			compatable with Java 1.1.4), support for HTTPS
     *			has been disabled.  Throw an exception if the
     *			protocol is HTTPS
     * 04/28/2004  INB	Create <code>AddressPermissions</code> objects.
     * 10/11/2002  INB	Created.
     *
     */
    private final void readFromFile(String authFileI)
	throws java.io.IOException,
	       java.lang.Exception
    {
	String authFile = authFileI,
	    localhost = "://localhost";
	int idx = authFile.indexOf(localhost);
	if (idx != -1) {
	    authFile = (authFile.substring(0,idx) +
			"://" + TCP.getLocalHost().getHostName() +
			authFile.substring(idx + localhost.length()));
	}

	// Find the URL for the authorization file and connect to it.
	java.net.URL url = new java.net.URL(authFile);
	java.net.URLConnection urlCon = url.openConnection();

	if (url.getProtocol().equalsIgnoreCase("HTTPS")) {
	    
	    // JPW 09/29/2004: In order to compile under J# (which is only
	    //                 compatable with Java 1.1.4), support for HTTPS
	    //                 has been disabled.  Throw an exception if the
	    //                 protocol is HTTPS
	    throw new Exception(
	        "HTTPS not supported for reading address authorization file.");
	    
	    /*
	    // If the protocol is HTTPS, then check for a secure connection.

	    if (urlCon instanceof javax.net.ssl.HttpsURLConnection) {
		// If this is a secure connection, then use SSL.
		javax.net.ssl.HttpsURLConnection sslCon =
		    (javax.net.ssl.HttpsURLConnection) urlCon;
		sslCon.setSSLSocketFactory
		    ((javax.net.ssl.SSLSocketFactory)
		     RBNBSSLSocketFactory.getDefault());

	    } else {
		// Otherwise, use an RBNB SSL connection.
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

	// Connect and set up a line number reader so that we can read whole
	// lines.
	urlCon.connect();
	readFromStream(urlCon.getInputStream());
    }
    
    /**
     * Reads the list of allowed/denied addresses from an InputStream.
     * <p>
     * @author WHF
     *
     * @param stream   The input stream to parse from.
     * @exception java.io.IOException if an I/O error occurs.
     * @since V3.0
     * @version 07/27/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/23/2007  WHF  Now depends on readFromStream.
     * 09/29/2004  JPW  In order to compile under J# (which is only
     *			compatable with Java 1.1.4), support for HTTPS
     *			has been disabled.  Throw an exception if the
     *			protocol is HTTPS
     * 04/28/2004  INB	Create <code>AddressPermissions</code> objects.
     * 10/11/2002  INB	Created.
     *
     */
    private final void readFromStream(java.io.InputStream is)
    	throws java.io.IOException, Exception
    {
	java.io.InputStreamReader isRead = new java.io.InputStreamReader(is);
	java.io.LineNumberReader lRead = new java.io.LineNumberReader(isRead);
	String line,
	       token;
	java.util.StringTokenizer sTok;
	int allowDeny = 0;
	boolean allowed = false;

	try {
	    while ((line = lRead.readLine()) != null) {
		// Read each line of the authorization file.

		if ((line.length() == 0) || (line.charAt(0) == '#')) {
		    // Skip over blank lines and lines starting with #.
		    continue;
		}

		// Tokenize the line by breaking it at whitespace.
		sTok = new java.util.StringTokenizer(line," \t\n\r");
		while (sTok.hasMoreTokens()) {
		    // Loop through the tokens.
		    token = sTok.nextToken();

		    if (token.equalsIgnoreCase("ALLOW")) {
			// The addresses to follow will be allowed.
			allowDeny = 1;

		    } else if (token.equalsIgnoreCase("DENY")) {
			// The addresses to follow will be denied.
			allowDeny = -1;

		    } else if (allowDeny == 0) {
			// The allow/deny state has not been set yet.
			throw new java.lang.IllegalStateException
			    ("Do not know whether to allow or deny " +
			     token +
			     ".");

		    } else if (allowDeny == -1) {
			// If this is a deny, then create an
			// <code>AddressPermissions</code> object and put it in
			// the deny list.
			addDeny(new AddressPermissions(token));

		    } else {
			// If this is an allow, then create an
			// <code>AddressPermissions</code> object and put it in
			// the allow list.
			allowed = true;
			addAllow(new AddressPermissions(token));
		    }
		}
	    }

	    if (!allowed) {
		// If nothing was specifically allowed, then create a default
		// entry to allow everything that isn't denied.
		addAllow(new AddressPermissions("*"));
	    }

	} catch (java.io.EOFException e) {
	}

	lRead.close();	
	isRead.close();
    }	

    /**
     * Removes an address from the ALLOW list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address specification to remove.
     * @return was the address removed?
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The address is now an <code>AddressPermissions</code>
     *			object rather than a string.  The localhost check is
     *			done in that class.
     * 10/11/2002  INB	Created.
     *
     */
    final boolean removeAllow(AddressPermissions addressI) {
	boolean removedR = false;

	AddressPermissions address;
	for (int idx = 0; idx < allow.size(); ++idx) {
	    address = (AddressPermissions) allow.elementAt(idx);
	    if (addressI.getAddress().equals(address.getAddress())) {
		allow.removeElementAt(idx);
		removedR = true;
		break;
	    }
	}

	return (removedR);
    }

    /**
     * Removes an address from the DENY list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the address specification to remove.
     * @return was the address removed?
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	The address is now an <code>AddressPermissions</code>
     *			object rather than a string.  The localhost check is
     *			done in that class.
     * 10/11/2002  INB	Created.
     *
     */
    final boolean removeDeny(AddressPermissions addressI) {
	boolean removedR = false;

	AddressPermissions address;
	for (int idx = 0; idx < deny.size(); ++idx) {
	    address = (AddressPermissions) deny.elementAt(idx);
	    if (addressI.getAddress().equals(address.getAddress())) {
		deny.removeElementAt(idx);
		removedR = true;
		break;
	    }
	}

	return (removedR);
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 04/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/28/2004  INB	Take advantage of the <code>AddressPermissions</code>
     *			objects.
     * 11/12/2002  INB	Created.
     *
     */
    public final String toString() {
	StringBuffer sb = new StringBuffer("# AddressAuthorization: \n");
	Object[] element;

	if ((allow != null) && (allow.size() > 0)) {
	    sb.append("ALLOW ");
	    //sb.append(allow.toString());
	    for (int ii = 0; ii < allow.size(); ++ii) {
		sb.append(allow.elementAt(ii));
		sb.append(' ');
	    }
	}
	if ((deny != null) && (deny.size() > 0)) {
	    sb.append("DENY ");
//	    sb.append(deny.toString());
	    for (int ii = 0; ii < deny.size(); ++ii) {
		sb.append(deny.elementAt(ii));
		sb.append(' ');
	    }
	}

	return (sb.toString());
    }
}
