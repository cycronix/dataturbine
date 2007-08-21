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
import java.io.StreamTokenizer;
import java.io.IOException;


/** A ConditionFactor represents some state of a resource that must be
 * satisfied in order for the associated request to be valid. The ConditionFactors in
 * a ConditionTerm must all match with states of the resource, i.e., they are AND'ed
 * together. Conditions are contained in a Precondition which is used in a
 * WebDAV If header. ConditionFactors are either constructed by the client, or may
 * have been given to the client in a previous method request. A ConditionFactor can
 * be either a StateToken or an EntityTag as defined by section 9.4 of the WebDAV
 * spec.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.Precondition
 * @see com.ibm.webdav.ConditionFactor
 * @see com.ibm.webdav.ConditionTerm
 * @see com.ibm.webdav.EntityTag
 * @see com.ibm.webdav.StateToken
 */
public abstract class ConditionFactor
{
   // ------------------------------------------------------------------------

   private boolean not_ = false;
/** Create a ConditionFactor (either a StateToken or EntityTag) by parsing
 * the tokenizer contining an If header value.
 * @param tokenizer a StreamTokenizer containing the contents of a state token or entity tag
 *    from a WebDAV If header
 * @return the parsed ConditionFactor
 * @exception com.ibm.webdav.WebDAVException thrown if there is a syntax error in the If header
 */
public static ConditionFactor create(StreamTokenizer tokenizer) throws WebDAVException {
	boolean not = false;
	ConditionFactor factor = null;
	try {
		int token = tokenizer.ttype;
		if (token == StreamTokenizer.TT_WORD) {
			if (tokenizer.sval.equalsIgnoreCase("Not")) {
				not = true;
			} else {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: expected Not");
			}
			token = tokenizer.nextToken();
		}
		switch (token) {
			case '<' :
				{
					factor = StateToken.create(tokenizer);
					break;
				}
			case '[' :
				{
					factor = EntityTag.create(tokenizer);
					break;
				}
			default :
				{
					throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: < or [");
				}
		}
	} catch (IOException exc) {
	}
	factor.setNot(not);
	return factor;
}
/** Negate the comparison on this ConditionFactor?
 @return true if the condition factor was negated in the If header
 */
public boolean not() {
	return not_;
}
/** Set how to compare to this ConditionFactor. Value is true implies match for
 * a valid request, false implies the request is valid only if the ConditionFactor
 * doesn't match.
 * @param value true means negate the condition
 */
public void setNot(boolean value) {
	not_ = value;
}
/** Return a String representation of this ConditionFactor as defined by the If
 * header in section 9.4 of the WebDAV spec.
 * @return a string representation of a state token or entity tag
 */
public abstract String toString();
}
