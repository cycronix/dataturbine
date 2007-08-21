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
import java.util.*;

/** A Condition represents some state configuration of a particular resource that must be
 * satisfied in order for the associated request to be valid. At least one of
 * the ConditionTerms in a Condition must match with states of the resource, i.e., 
 * they are OR'd together. Conditions are contained in a Precondition which is used in a
 * WebDAV If header.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.Precondition
 * @see com.ibm.webdav.ConditionFactor
 * @see com.ibm.webdav.ConditionTerm
 * @see com.ibm.webdav.EntityTag
 * @see com.ibm.webdav.StateToken
 */
public class Condition {

   private String uri = null;
   private Vector conditionTerms = new Vector();
/** Construct a Condition on the default resource.
 */
public Condition() {
}
/** Construct a Condition with the given URI.
 * @param uri the URI of the resource associated with this condition
 */
public Condition(String uri) {
	this.uri = uri;
}
/** Add a ConditionTerm to a Condition.
 * @param term the term to add
 */
public void addConditionTerm(ConditionTerm term) throws WebDAVException {
	conditionTerms.addElement(term);
}
/** Does this Condition contain the given ConditionTerm?
 * @param term the term to check for
 * @return true if the condition contains the given term, false otherwise
 */
public boolean contains(ConditionTerm term) {
	// iterate through the factors looking for a match
	boolean match = false;
	Enumeration terms = getConditionTerms();
	while (!match && terms.hasMoreElements()) {
		ConditionTerm t = (ConditionTerm) terms.nextElement();
		match = term.matches(t);
	}
	return match;
}
/** Create a Condition by parsing the given If header as defined by
 * section 9.4 in the WebDAV spec.
 *
 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
 * @return the parsed condition
 */
public static Condition create(StreamTokenizer tokenizer) throws WebDAVException {
	Condition condition = new Condition();
	try {
		int token = tokenizer.ttype;
		if (token == '<') {
			token = tokenizer.nextToken();
			if (token == StreamTokenizer.TT_WORD) {
				condition.setResourceURI(tokenizer.sval);
			} else {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: missing resource URI");
			}
			token = tokenizer.nextToken();
			if (token == '>') {
				token = tokenizer.nextToken();
			} else {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: >");
			}
		}
		if (token == '(') {
			while (token == '(') {
				condition.addConditionTerm(ConditionTerm.create(tokenizer));
				token = tokenizer.ttype;
			}
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: ( or <");
		}
	} catch (IOException exc) {
	}
	return condition;
}
/** Create a Condition by parsing the given If header as defined by
 * section 9.4 in the WebDAV spec.
 *
 * @param ifHeader the contents of a WebDAV If header
 * @return the parsed condition
 * @exception com.ibm.webdav.WebDAVException thrown if there is a syntax error in the header
 */
public static Condition create(String ifHeader) throws WebDAVException {
	StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(ifHeader));
	// URI characters
	tokenizer.wordChars('!', '/');
	tokenizer.wordChars(':', '@');
	tokenizer.ordinaryChar('(');
	tokenizer.ordinaryChar(')');
	tokenizer.ordinaryChar('<');
	tokenizer.ordinaryChar('>');
	tokenizer.ordinaryChar('[');
	tokenizer.ordinaryChar(']');
	tokenizer.quoteChar('"');
	Condition condition = null;
	try {
		int token = tokenizer.nextToken();
		condition = Condition.create(tokenizer);
		token = tokenizer.ttype;
		if (token != StreamTokenizer.TT_EOF) {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: EOF");
		}
	} catch (IOException exc) {
	}
	return condition;
}
/** Get all the ConditionTerms for this Condition. At least one of the ConditionTerms in
 * a Condition must match with states of the resource, i.e., they are OR'd
 * together. Conditions are contained in a Precondition which is used in a
 * WebDAV If header.
 */
public Enumeration getConditionTerms() {
	return conditionTerms.elements();
}
/** Get the URI of the associated Resource. The condition must match on this 
 * resource. This is useful for Preconditions that span multiple resources.
 * @return the resource URI whose state is described by this Condition, may be null
 *    indicating the condition applies to the resource receiving the request
 */
public String getResourceURI() {
	return uri;
}
/** See if this Condition matches the given Condition. This is an
 * OR operation.
 * @param condition the condition to match against
 * @return true if the conditions match, false otherwise.
 */
public boolean matches(Condition condition) {
	// check the Resource if one was given
	boolean match = true;
	if (uri != null) {
		try {
			/* Don't match on the protocol, host, and port, they don't really matter.
			 * If we're here on this server, then the client must have provided
			 * a protocol, host, and port that gets to a resource this server manages.
			 * So we shouldn't interpret these URIs as URLs, but rather as opaque
			 * strings that identify the resource on this server
			 */
			/* 
			URL url1 = new URL(uri);
			URL url2 = new URL(condition.getResourceURI());
			match = match && url1.getProtocol().equals(url2.getProtocol());
			match = match && url1.getHost().equals(url2.getHost());
			int port1 = url1.getPort();
			if (port1 == -1) { // use the default port
				port1 = 80;
			}
			int port2 = url2.getPort();
			if (port2 == -1) {
				port2 = 80;
			}
			match = match && (port1 == port2);
			match = match && url1.getFile().equals(url2.getFile());
			*/
			match = uri.equals(condition.getResourceURI());
		} catch (Exception exc) {
			match = false;
		}
	}
	if (!match) {
		return false;
	}
	// is each term in the condition in the given condition
	match = false;
	Enumeration terms = getConditionTerms();
	while (!match && terms.hasMoreElements()) {
		ConditionTerm term = (ConditionTerm) terms.nextElement();
		match = condition.contains(term);
	}
	return match;
}
/** Set the URI of the associated Resource. The condition must match on this 
 * resource. This is useful for Preconditions that span multiple resources.
 * @param value the resource URI whose state is described by this Condition.
 *    value can be null if the condition applies to the resource executing 
 *    the method.
 */
public void setResourceURI(String value) {
	uri = value;
}
/** Return a String representation of this Condition as defined by section 9.4
 * of the WebDAV Spec.
 * @return a String representation of this condition
 */
public String toString() {
	StringWriter os = new StringWriter();
	if (getResourceURI() != null) {
		os.write('<');
		os.write(getResourceURI());
		os.write("> ");
	}
	Enumeration terms = getConditionTerms();
	while (terms.hasMoreElements()) {
		ConditionTerm term = (ConditionTerm) terms.nextElement();
		os.write(term.toString());
		if (terms.hasMoreElements()) {
			os.write(' ');
		}
	}
	try {
		os.close();
	} catch (Exception exc) {
	}
	return os.toString();
}
}
