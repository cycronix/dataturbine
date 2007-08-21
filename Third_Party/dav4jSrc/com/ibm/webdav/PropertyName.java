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
 * 
 * Portions Copyright (C) Simulacra Media Ltd, 2004.
 */

 /** This class represents the universal name for a
 * property.
 * @author Jason Crawford &lt;ccjason@us.ibm.com&gt;
 */
public class PropertyName {

	protected String ns = null;
	protected String local = null;

	public static final PropertyName pnResourcetype = createPropertyNameQuietly( "DAV:resourcetype" );
	public static final PropertyName pnCreationdate = createPropertyNameQuietly( "DAV:creationdate" );
	public static final PropertyName pnSupportedlock = createPropertyNameQuietly( "DAV:supportedlock" );
	public static final PropertyName pnContenttype = createPropertyNameQuietly( "DAV:getcontenttype" );
	public static final PropertyName pnGetlastmodified = createPropertyNameQuietly( "DAV:getlastmodified" );
	public static final PropertyName pnGetcontentlength = createPropertyNameQuietly( "DAV:getcontentlength" );
	public static final PropertyName pnLockdiscovery = createPropertyNameQuietly( "DAV:lockdiscovery" );

/**
 * PropertyName constructor.
 * @param tag a tag whose name will be used as the property name of
 *  constructed PropertyName.
 */
public PropertyName( org.w3c.dom.Element tag ) {
	// (jlc 4/4/99) we dont' check for valid local names
	//   because if
	//   it was already in a tag, it must be okay.  Perhaps
	//   we should declare that we throw a
	//   InvalidPropertyNameException as a place holder
	//   for possibly having stricter checks down the
	//   road?  It's tempting.

	local = tag.getLocalName();
	ns =  tag.getNamespaceURI();

}
/**
 * PropertyName constructor.  This constructor will only
 * work for property names in the "DAV:" namespace and
 * the tag parameter must begin with "DAV:".
 * @param tag the property name.  ie. DAV:resourcetype
 */
public PropertyName( String tag ) throws InvalidPropertyNameException {
	if (!tag.startsWith("DAV:")) {
		throw new InvalidPropertyNameException( "Invalid tag parameter: "+tag );
	} else {
		tag = tag.substring( 4 );
	}
	local = tag;
	ns = "DAV:";
}
/**
 * PropertyName constructor.
 * @param tag is the local property name.   ie "resourcetype"
 * @param namespace is the namespace string.  ie "DAV:"
 */
public PropertyName( String namespace, String tag ) throws InvalidPropertyNameException {
	ns = namespace;
	local = tag;
	if (local.indexOf(':') >= 0) {
		throw new InvalidPropertyNameException( "local part is "+tag );
	}
}
/**
 * Returns a combined namespace+local string.
 * @return java.lang.String
 */
public String asExpandedString() {
	return ns+":"+local;
}
/**
 * This factory takes a propertyname string.  Unlike
 *  the constructor with the same argument signature,
 *  this method will throw a runtime exception in
 *  situations where the contructor would throw
 *  an InvalidPropertyNameException.  This is
 *  important because runtime exceptions don't have
 *  to be declared.  This can be convenient if a
 *  caller knows that their provided property name
 *  is correct (by the definition of the PropertyName(String)
 *  constructor) and don't want to sprinkle exception
 *  declarations all over their code... or
 *  add try/catch blocks... when they
 *  know an InvalidPropertyNameException will never be thrown.
 * @return com.ibm.webdav.PropertyName
 * @param propname java.lang.String - See the documentation
 *    for the PropertyName( String) constructor for info.
 */
public static PropertyName createPropertyNameQuietly( String propname ) {
	PropertyName res;
	try {
		res = new PropertyName( propname );
	} catch (InvalidPropertyNameException excc) {
		// we catch this so that we don't have to
		//   declare it in the callers.  If it
		//   occurs we have an internal problem.
		throw new RuntimeException( "internal error: bad property: "+propname );
	}
	return res;
}
/**
 * Compares two objects for equality. Returns a boolean that indicates
 * whether this object is equivalent to the specified object. This method
 * is used when an object is stored in a hashtable.
 * <p>
 * In the case of this class, if the contatenated ns+local string names
 * are equal, then the PropertyNames are equal.
 * <p>
 * It should be noted that this method can take a String as a
 * parameter and match on it.  That equality relationship is
 * not communative though since the String.equals(Object) method
 * will never return true if passed a PropertyName.
 *
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals( Object obj) {
	String s1 = ns+local;
	if (obj instanceof PropertyName) {
		PropertyName param1 = (PropertyName)obj;
		String s2 = param1.ns + param1.local;
		return s1.equals(s2);
	} else if (obj instanceof String) {
		String param1 = (String)obj;
		return s1.equals(param1);
	}
	return false;
}
/**
 * Returns a the local portion of a property name.  For
 * example it would return "resourcetype" if the PropertyName
 * was representing a property name for DAV:resourcetype.
 * @return a string representation of the local property name
 * without any prefix or namespace.
 */
public String getLocal() {
	return local;
}
/**
 * Returns a the namespace of the PropertyName.
 * @return a string representation of the namespace of a property name.
 * For example "DAV:".
 */
public String getNamespace() {
	return ns;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	// Insert code to generate a hash code for the receiver here.
	// This implementation forwards the message to super.  You may replace or supplement this.
	// NOTE: if two objects are equal (equals(Object) returns true) they must have the same hash code
	String join = ns + local;
	return join.hashCode();
}
/**
 * Returns a consise human readable string describing the property name.
 * @return java.lang.String
 */
public String toString() {
	return asExpandedString();
}
}
