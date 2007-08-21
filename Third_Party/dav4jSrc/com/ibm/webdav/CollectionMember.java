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

/** A CollectionMember encapsulates a Resource in a collection along with
 * a number of live properties that might be useful to client applications. 
 * Since WebDAV requires the use of a Collection's properties to determine
 * the members of a collection, we might as well make use of the properties returned
 * so that clients don't have to make a lot of unnecessary server requests. The
 * properties collected in the CollectionMember are those used by the WebDAV 
 * Explorer.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class CollectionMember implements java.io.Serializable
{
   private Collection parent = null;
   private Resource resource = null;
   private MultiStatus properties = null;  
   private String name = null;                  // the name relative to the parent
/** Create a CollectionMember for a resource. Include it parent and some
 * convenient DAV properties.
 * @param parent the parent collection
 * @param resource the member resource of the parent
 * @param initialProperties a MultiStatus containing some useful DAV properties
 */
public CollectionMember(Collection parent, Resource resource, MultiStatus initialProperties) throws WebDAVException {
	this.parent = parent;
	this.resource = resource;
	this.properties = initialProperties;
	String parentURI = parent.getURL().getFile();
	String memberURI = resource.getURL().getFile();
	name = memberURI.substring(parentURI.length());
	// in case the parent collection didn't end with a /
	if (name.startsWith("/")) {
		name = name.substring(1);
	}
}
/** Create a CollectionMember for a resource. The resource is
 * a root collection that has no parent or properties
 * @param resource the member resource
 */
public CollectionMember(Resource resource) throws WebDAVException {
	this.parent = null;
	this.properties = null;
	this.resource = resource;
	name = resource.getURL().toString();
}
/** Return the name of the Resource in this CollectionMember relative to its parent. Use
 * getResource().getURL().toString() to get the full URL.
 * @return the name of this member relative to its parent collection
 */
public String getName() {
	return name;
}
/** Return the parent of this CollectionMember. i.e., the Collection
 * it's associated Resource is contained in.
 * @return the parent collection
 */
public Collection getParent() {
	return parent;
}
/** Return the properties of the Resource in this CollectionMember. These
 * properties are retained only for convenience of client applications. The
 * properties can always be obtained using getProperties().
 * @return A PropertyResponse containing some useful DAV properties
 * @see com.ibm.webdav.Resource#getProperties
 */
public PropertyResponse getProperties() {
	PropertyResponse response = (PropertyResponse) properties.getResponses().nextElement();
	return response;
}
/** Return the Resource represented by this CollectionMember.
 * @return the resource associted with this CollectionMember
 */
public Resource getResource() {
	return resource;
}
/** Return a String representation of this CollectionMember.
 * @return the name of the CollectionMember
 * @see com.ibm.webdav.CollectionMember#getName
 */
public String toString() {
	return getName();
}
}
