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
import java.util.Vector;
import java.util.Enumeration;
import java.io.StringWriter;
import java.io.StreamTokenizer;
import java.io.IOException;

/** A ConditionTerm represents some state configuration of a resource that must be
 * satisfied in order for the associated request to be valid. The ConditionFactors in
 * a ConditionTerm must all match with states of the resource, i.e., they are AND'ed
 * together. ConditionTerms are contained in a Condition which is used in the Precondition
 * of a WebDAV If header.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.Precondition
 * @see com.ibm.webdav.ConditionFactor
 * @see com.ibm.webdav.ConditionTerm
 * @see com.ibm.webdav.EntityTag
 * @see com.ibm.webdav.StateToken
 */
public class ConditionTerm {

   private Vector conditionFactors = new Vector();
/** Construct a Condition with no associated Resource URI.
*/
public ConditionTerm() {
}
/** Add a ConditionFactor to a ConditionTerm.
 * @param the factor to add
 * @exception com.ibm.webdav.WebDAVException thrown if the term already contains the factor
 */
public void addConditionFactor(ConditionFactor factor) throws WebDAVException {
	if (conditionFactors.contains(factor)) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: duplicate entry in list");
	}
	conditionFactors.addElement(factor);
}
/** Does this ConditionTerm contain the given ConditionFactor?
 * @param factor the factor to check for
 * @return true if the term contains the given factor
 */
public boolean contains(ConditionFactor factor) {
	return conditionFactors.contains(factor);
}
/** Create a ConditionTerm by parsing the given If header as defined by
 * section 9.4 in the WebDAV spec.
 *
 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
 * @return the parsed ConditionTerm
 * @exception com.ibm.webdav.WebDAVException thrown if there is a syntax error in the If header
 */
public static ConditionTerm create(StreamTokenizer tokenizer) throws WebDAVException {
	ConditionTerm term = new ConditionTerm();
	try {
		int token = tokenizer.ttype;
		if (token == '(') {
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: (");
		}
		while (token == StreamTokenizer.TT_WORD || token == '<' || token == '[') {
			term.addConditionFactor(ConditionFactor.create(tokenizer));
			token = tokenizer.ttype;
		}
		if (token == ')') {
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: )");
		}
	} catch (IOException exc) {
	}
	if (!term.getConditionFactors().hasMoreElements()) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: missing State-Token or entity-tag");
	}
	return term;
}
/** Get all the ConditionFactors in this Condition. The ConditionFactors in
 * a Condition must all match with states of the resource, i.e., they are AND'ed
 * together. ConditionTerms are contained in a Condition which is used in the
 * Precondition of a WebDAV If header.
 * @return an Enumeration of ConditionFactors
 */
public Enumeration getConditionFactors() {
	return conditionFactors.elements();
}
/** See if this ConditionTerm matches the given ConditionTerm. This is an
 * AND operation. All the factors in the ConditionTerm must match.
 * @param conditionTerm the term to match
 * @return true if all the factors in the term match those in this term
 */
public boolean matches(ConditionTerm conditionTerm) {
	int numberOfItemsToMatch = 0;
	boolean match = true;
	Enumeration factors = getConditionFactors();
	while (match && factors.hasMoreElements()) {
		ConditionFactor factor = (ConditionFactor) factors.nextElement();
		if (factor.not()) {
			match = !conditionTerm.contains(factor);
		} else {
			match = conditionTerm.contains(factor);
			numberOfItemsToMatch++;
		}
	}
	match = match && numberOfItemsToMatch == conditionTerm.numberOfFactors();
	return match;
}
/** Get the number of ConditionFactors in this ConditionTerm.
 * @return the number of factors in this term
 */
public int numberOfFactors() {
	return conditionFactors.size();
}
/** Return a String representation of this ConditionTerm as defined by section 9.4
 * of the WebDAV Spec.
 * @return a string representation of this term
 */
public String toString() {
	StringWriter os = new StringWriter();
	os.write('(');
	Enumeration factors = getConditionFactors();
	while (factors.hasMoreElements()) {
		ConditionFactor factor = (ConditionFactor) factors.nextElement();
		os.write(factor.toString());
		if (factors.hasMoreElements()) {
			os.write(' ');
		}
	}
	os.write(')');
	try {
		os.close();
	} catch (Exception exc) {
	}
	return os.toString();
}
}
