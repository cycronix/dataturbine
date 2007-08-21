package com.ibm.webdav;

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
import java.io.*;

/** A StateToken is a ConditionFactor describing some state of a resource represented
 * as a URI. A typical example would be the WebDAV lock token.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.Precondition
 * @see com.ibm.webdav.ConditionTerm
 * @see com.ibm.webdav.ConditionFactor
 * @see com.ibm.webdav.EntityTag
 * @see com.ibm.webdav.StateToken
 */
public class StateToken extends ConditionFactor {
	private String uri = null; // represents some state of a resource expressed as a URI
   /** Construct a StateToken. Should never be called.
	*/
   private StateToken()
   {
   }   
/** Construct a StateToken with the given URI.
 * @param uri the URI of the state token
 */
public StateToken(String uri) {
	this.uri = uri;
}
/** Create a StateToken by parsing the given If header as defined by
 * section 9.4 in the WebDAV spec.
 *
 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
 * @return the parsed ConditionFactor (StateToken)
 */
public static ConditionFactor create(StreamTokenizer tokenizer) throws WebDAVException {
	StateToken stateToken = new StateToken();
	try {
		int token = tokenizer.ttype;
		if (token == '<') {
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: <");
		}
		if (token == StreamTokenizer.TT_WORD) {
			stateToken.setURI(tokenizer.sval);
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected a URI");
		}
		if (token == '>') {
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: >");
		}
	} catch (IOException exc) {
	}
	return stateToken;
}
/** Compare with another StateToken.
 * @param factor the state token to compare with
 * @return true if this state token has the same URI as the factor
 */
public boolean equals(Object factor) {
	return factor != null && factor instanceof StateToken && getURI().equals(((StateToken) factor).getURI());
}
/** Get the URI of this StateToken. The URI represents some state of the
 * resource in the containing Condition, for example, the lock token.
 * @return the URI for this state token
 */
public String getURI() {
	return uri;
}
/** Set the URI of this StateToken. The URI represents some state of the
 * resource in the containing Condition, for example, the lock token.
 * @param value the URI for this state token
 */
public void setURI(String value) {
	uri = value;
}
/** Return a String representation of this StateToken as defined by the If
 * header in section 9.4 of the WebDAV spec.
 * @return a string representation of this state token
 */
public String toString() {
	StringWriter os = new StringWriter();
	if (not()) {
		os.write("Not ");
	}
	os.write('<');
	os.write(getURI());
	os.write('>');
	try {
		os.close();
	} catch (Exception exc) {
	}
	return os.toString();
}
}
