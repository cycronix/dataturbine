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
import java.io.StringWriter;
import java.io.IOException;
import java.io.StreamTokenizer;

/** An EntityTag is a ConditionFactor describing some state of a resource represented
 * as an opaque string. See section 3.11 of the HTTP/1.1 spec.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.Precondition
 * @see com.ibm.webdav.ConditionFactor
 * @see com.ibm.webdav.ConditionTerm
 * @see com.ibm.webdav.EntityTag
 * @see com.ibm.webdav.StateToken
 */
public class EntityTag extends ConditionFactor {

   private static int bcnt = 0;
   private static String basetime = Long.toHexString( new java.util.Date().getTime() );

   private String eTag = null;  // represents some state of a resource expressed as a ETag
   private boolean weak = false;
/** Construct a EntityTag. Should never be called.
 */
private EntityTag() {
}
/** Construct a EntityTag with the given opaque string tag.
 * @param tag the opaque string defining the entity tag
 */
public EntityTag(String tag) {
	this.eTag = tag;
}
/** Create an EntityTag by parsing the given If header as defined by
 * section 3.11 of the HTTP/1.1 spec.
 *
 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
 * @return the parsed ConditionFactor (EntityTag)
 * @exception com.ibm.webdav.WebDAVException thrown if there is a syntax error in the If header
 */
public static ConditionFactor create(StreamTokenizer tokenizer) throws WebDAVException {
	EntityTag entityTag = new EntityTag();
	try {
		int token = tokenizer.ttype;
		if (token == '[') {
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: [");
		}
		if (token == '"') {
			entityTag.setETag(tokenizer.sval);
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected a quoted string");
		}
		if (token == ']') {
			token = tokenizer.nextToken();
		} else {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Error parsing If header: saw: " + (char) token + " expected: ]");
		}
	} catch (IOException exc) {
	}
	return entityTag;
}
/** Compare with another EntityTag.
 * @param etag the entity tag to compare
 * @return true if the tags are equal, false otherwise
 */
public boolean equals(Object etag) {
	return etag != null && etag instanceof EntityTag && 
		   getETag().equals(((EntityTag) etag).getETag());
}
/** Construct a unique EntityTag. The tag is constructed by concatening the current time with
 * the current thread's hash code.
 * @return a unique entity tag that servers may use for any purpose
 */
public static EntityTag generateEntityTag() {
	String xx = basetime + ":" + Integer.toHexString(Thread.currentThread().hashCode());
	bcnt++;
	xx += ":" + bcnt;
	return new EntityTag(xx);
}
/** Get the ETag of this EntityTag. The ETag represents some state of the
 * resource in the containing Condition.
 * @return the etag
 */
public String getETag() {
	return eTag;
}
/** Is this a weak EntityTag?
 * @return true if this is a weak entity tag
 */
public boolean isWeak() {
	return weak;
}
/** Set the ETag of this EntityTag. The ETag represents some state of the
 * resource in the containing Condition, for example, the lock token.
 * @value the etag to set
 */
public void setETag(String value) {
	eTag = value;
}
/** Set the strength of this EntityTag.
 * value true indicates this is a weak entity tag
 */
public void setWeak(boolean value) {
	weak = value;
}
/** Return a String representation of this EntityTag as defined by the If
 * header in section 9.4 of the WebDAV spec.
 * @return a string representation of this entity tag
 */
public String toString() {
	StringWriter os = new StringWriter();
	if (not()) {
		os.write("Not ");
	}
	if (isWeak()) {
		os.write("W/");
	}
	os.write("[\"");
	os.write(getETag());
	os.write("\"]");
	try {
		os.close();
	} catch (Exception exc) {
	}
	return os.toString();
}
}
