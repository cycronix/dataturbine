package com.ibm.webdav.protocol.http;

/*
 * (C) Copyright IBM Corp. 2000  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */
import java.net.*;

import sun.misc.*;
import sun.net.www.protocol.http.*;

/**
 * An interface for all objects that implement HTTP authentication.
 * See the HTTP spec for details on how this works in general.
 * A single class or object can implement an arbitrary number of
 * authentication schemes.  
 *
 * @author Jim Amsden
 */
public class WebDAVAuthenticator implements HttpAuthenticator {

   static BASE64Encoder base64encoder = new BASE64Encoder();
/**
* Returns the String that should be included in the HTTP
* <B>Authorization</B> field.  Return null if no info was
* supplied or could be found.
* <P>
* Example:
* --> GET http://www.authorization-required.com/ HTTP/1.0
* <-- HTTP/1.0 403 Unauthorized
* <-- WWW-Authenticate: Basic realm="WallyWorld"
* call schemeSupported("Basic"); (return true)
* call authString(u, "Basic", "WallyWorld", null);
*   return "QWadhgWERghghWERfdfQ=="
* --> GET http://www.authorization-required.com/ HTTP/1.0
* --> Authorization: Basic QWadhgWERghghWERfdfQ==
* <-- HTTP/1.0 200 OK
* @param u the resource URL
* @param scheme the authentication scheme, Basic, or Digest
* @param realm the security realm to authenticate in
* @return a valid authorization header using the scheme in the realm
*/
public String authString(URL u, String scheme, String realm) {
	String authString = null;

	// put up a dialog requesting the userid and password for this scheme
	// and realm
	/*
	JTextField userid = new JTextField();
	JPasswordField password = new JPasswordField();
	Object[] fields = {"Enter userid and password", userid, password};
	JOptionPane uidPane = new JOptionPane(fields, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = uidPane.createDialog(null, scheme + " " + realm);
	dialog.show();
	Integer result = (Integer) uidPane.getValue();
	if (result.intValue() == 0) {
		authString = base64encoder.encode((userid.getText() + ":" + password.getPassword()).getBytes());
	}
	*/
	// This was a good idea, but it is probably better for authoring
	// applications if the application handles authentication itself
	return authString;
}
/**
* Indicate whether the specified authentication scheme is
* supported.  In accordance with HTTP specifications, the
* scheme name should be checked in a case-insensitive fashion.
* @param scheme the authentication scheme to check for
* @return true if the scheme is supported, false otherwise
*/
public boolean schemeSupported(String scheme) {
	return scheme.equalsIgnoreCase("basic");
}
}
