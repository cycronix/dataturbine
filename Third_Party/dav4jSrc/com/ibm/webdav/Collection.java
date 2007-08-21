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
import java.net.*;
import java.util.*;

import org.w3c.dom.*;

import com.ibm.webdav.impl.*;


/** A Collection is a Resource that contains other
 * resources including other Collections. It provides a
 * concrete, client side implementation of Collection.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class Collection extends Resource  {
	
   /** shallow means copy just this resource. */
	public static final String shallow = "0";
	/** deep means copy this resource and recursively all its members. */
	public static final String deep = "infinity";


	/** thisResource means get properties on this resource only. */
	public static final String thisResource = "0";
	/** immediateMembers means get properties on this resource and its
	immediate members. */
	public static final String immediateMembers = "1";
	/** allMembers means get properties on this resource and recursively
	all its members. */
	public static final String allMembers = "infinity";

   private Vector members = null;   // lazy retrieve the members of the collection for the server

public Collection() {
	super();
}
/** Construct a Collection with the given URL. This is the constructor most clients
* will use to construct and access collections using WebDAV. The collection having
* the url may not exist as this constructor does not access the resource from
* the server. Use exists() or attmept to get the members of the collection to
* see if it exists. Other constructors are provided using parameters for the
* various parts of the URL. See java.net.URLConnection.
*
* @param url the URL of the resource.
* @exception com.ibm.webdav.WebDAVException
*/
public Collection(String url) throws WebDAVException {
   try {
	  initialize(new URL(url), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Collection with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @param targetSelector the revision target selector for this Collection
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Collection(String url, TargetSelector targetSelector) throws WebDAVException {
   try {
	  initialize(new URL(url), targetSelector);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Create a Collection from the given URL components.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param port the TCP port to use. HTTP uses 80 by default.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Collection(String protocol, String host, int port, String file) throws WebDAVException {
   try {
	  initialize(new URL(protocol, host, port, file), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Create a Collection from the given URL components. This constructor uses the default
 * HTTP port.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception com.ibm.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Collection(String protocol, String host, String file) throws WebDAVException {
   try {
	  initialize(new URL(protocol, host, file), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Collection with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Collection(URL url) throws WebDAVException {
   try {
	  initialize(url, null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Collection with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @param targetSelector the revision target selector for this Collection
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Collection(URL url, TargetSelector targetSelector) throws WebDAVException {
   try {
	  initialize(url, targetSelector);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Collection with the given URL specification in the given context.
 * The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Collection.
 *
 * @param context a URL giving the context in which the spec is evaluated
 * @param spec a URL whose missing parts are provided by the context
 * @exception com.ibm.webdav.Exception
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Collection(URL context, String spec) throws WebDAVException {
   try {
	  initialize(new URL(context, spec), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/**
 * Put this collection under baseline control.
 */
public void baseline() throws WebDAVException {
}
/** Deep copy this resource to the destination URL overwriting any existing contents.
* All live properties must remain live at the destination server.
* Partial results are possible, check the returned status for details.
*
* @param destinationURL the destination
*
* @return the status of the copy operation for each resource copied
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus copy(String destinationURL) throws WebDAVException {
	return copy(destinationURL, true, null, Collection.deep);
}
/** Deep copy this resource to the destination URL.
* Partial results are possible, check the returned status for details.
*
* @param destinationURL the destination
* @param overwrite true implies overwrite the destination if it exists
* @param propertiesToCopy a collection of properties that must be copied or
* the method will fail. propertiesToCopy may have one of the following values:
* <ul>
*    <li>null - ignore properties that cannot be copied</li>
*    <li>empty collection - all properties must be copied or the method will fail</li>
*    <li>a collection of URIs - a list of the properties that must be copied 
*        or the method will fail</li>
* </ul>
*
* @return the status of the copy operation for each resource copied
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus copy(String destinationURL, boolean overwrite, Vector propertiesToCopy) throws WebDAVException {
	return copy(destinationURL, overwrite, propertiesToCopy, Collection.deep);
}
/** Copy this resource to the destination URL.
* Partial results are possible, check the returned status for details.
*
* @param destinationURL the destination
* @param overwrite true implies overrite the destination if it exists
* @param propertiesToCopy a collection of properties that must be copied or
* the method will fail. propertiesToCopy may have one of the following values:
* <ul>
*    <li>null - ignore properties that cannot be copied</li>
*    <li>empty collection - all properties must be copied or the method will fail</li>
*    <li>a collection of URIs - a list of the properties that must be copied 
*        or the method will fail</li>
* @param depth an indicator for immediate members or recursively all children.
* </ul>
* <ul>
*    <li>shallow: copy only this resource</li>
*    <li>deep: copy this resource and recursively all of its children</li>
* </ul>
*
* @return the status of the copy operation for each resource copied
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus copy(String destinationURL, boolean overwrite, Vector propertiesToCopy, String depth) throws WebDAVException {
	flushCaches();
	return ((IRCollection)impl).copy(context, destinationURL, overwrite, propertiesToCopy, depth);
}
/** Actually create the collection in the repository. The resource indicated 
* by the URL must not already exist. All ancestors of this URL must already 
* exist.
* @exception com.ibm.webdav.WebDAVException
*/
public void createCollection() throws WebDAVException {
	createCollection((Document) null);
}
/** Actually create the collection in the repository. The resource indicated 
* by the URL must not already exist. All ancestors of this URL must already 
* exist.
*
* @param contents an XML Document describing the members of this collection, bodies
* of members, and properties on the collections or members. Not completely defined in 
* version 10 of the WebDAV specification
*
* @return Multistatus describing the result
* of the operation
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus createCollection(Document contents) throws WebDAVException {
	MultiStatus result = null;
	result = ((IRCollection)impl).createCollection(context, contents);
	return result;
}
/** Create a sub-collection of this collection. The resource indicated 
* by the URL must not already exist. All ancestors of this URL must already 
* exist.
* @param collectionName the name of the collection to create relative to the 
* URL of this resource.
* @return the newly created Collection
* @exception com.ibm.webdav.WebDAVException
*/
public Collection createSubCollection(String collectionName) throws WebDAVException {
	Collection subCollection = null;
	try {
		subCollection = new Collection(getURL().toString() + collectionName);
	} catch (WebDAVException exc) {
		throw exc;
	} 
	subCollection.getRequestContext().precondition(getRequestContext().precondition());
	subCollection.getRequestContext().authorization(getRequestContext().authorization());
	subCollection.createCollection();
	return subCollection;
}
/** Flush any caches so that subsequent methods obtain fresh data from the server. Currently,
* only the contents of the resource and members of a resource collection are cached. 
* @exception com.ibm.webdav.WebDAVException
*/
public void flushCaches() throws WebDAVException {
	super.flushCaches();
	members = null;
}
/** Get the members of this Collection.
*
* @return a Vector of CollectionMembers
* @exception com.ibm.webdav.WebDAVException
*/
public Vector getMembers() throws WebDAVException {
	if (members == null) {
		members = new Vector();
		MultiStatus multiStatus = getProperties(Collection.immediateMembers);
		URL thisURLu = getURL();
		String thisURL = thisURLu.toString();

		// each response contains a reference to an element in the collection
		Enumeration responses = multiStatus.getResponses();
		while (responses.hasMoreElements()) {
			PropertyResponse response = ((Response)responses.nextElement()).toPropertyResponse();
			String memberName = response.getResource();
			URL uu = null;
			try {
				uu = new URL( thisURLu, memberName );
			} catch (java.net.MalformedURLException exc) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
			}
			memberName = uu.toString();
			// don't put myself in my own collection
			if (!memberName.equals(thisURL)) {
				Resource resource = null;
				// resource might contain a URI rather than a URL, but
				//    the Resource*P constructors need a full URL, so 
				//    we construct one here.
				try {
					URL urll = new URL(getURL(), response.getResource());
					if (response.isOnACollection()) {
						resource = new Collection(urll);
					} else {
						resource = new Resource(urll);
					}
				} catch (Exception exc) {
					throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
				}
				MultiStatus childProperties = new MultiStatus();
				childProperties.addResponse(response);
				CollectionMember member = new CollectionMember(this, resource, childProperties);
				members.addElement(member);
			}
		}
	}
	return members;
}
/** Get the named properties for this resource and (potentially) its children.
*
* @param names an arrary of property names to retrieve. 
* @param depth an indicator for immediate members or recursively all children.
* <ul>
*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
*    <li>allMembers: properties of this resource and recursively all its children</li>
* </ul>
*
* @return a MultiStatus of PropertyResponses
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getProperties(PropertyName names[], String depth) throws WebDAVException {
	return ((IRCollection)impl).getProperties(context, names, depth);
}
/** Get all the properties for this resource and (potentially) its children.
*
* @param depth an indicator for immediate members or recursively all children.
* <ul>
*    <li>thisResource: propeprties of this resource</li>
*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
*    <li>allMembers: properties of this resource and recursively all its children</li>
* </ul>
*
* @return a MultiStatus of PropertyResponses
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getProperties(String depth) throws WebDAVException {
	return ((IRCollection)impl).getProperties(context, depth);
}
/** Get the named property for this resource and (potentially) its children.
*
* @param name the name of the property to retrieve.
* @param depth an indicator for immediate members or recursively all children.
* <ul>
*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
*    <li>allMembers: properties of this resource and recursively all its children</li>
* </ul>
*
* @return a MultiStatus of PropertyResponses
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getProperty(PropertyName name, String depth) throws WebDAVException {
	PropertyName[] names = new PropertyName[1];
	names[0] = name;
	return getProperties(names, depth);
}
/** Get the names of all properties for this resource and (potentially) its children.
*
* @param depth an indicator for immediate members or recursively all children.
* <ul>
*    <li>thisResource: propeprties of this resource</li>
*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
*    <li>allMembers: properties of this resource and recursively all its children</li>
* </ul>
*
* @return a MultiStatus of PropertyResponses
* (PropertyValue.value is always null, PropertyValue.status contains the status)
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getPropertyNames(String depth) throws WebDAVException {
	return ((IRCollection)impl).getPropertyNames(context, depth);
}
/** Initialize this collection instance. Make sure the URL ends in a '/'.
*/
protected void initialize(URL url, TargetSelector targetSelector) throws WebDAVException {
	String file = url.getFile();
	if (!file.endsWith("/")) {
		file = file + "/";
	}
	try {
		this.url = new URL(url, file);
		impl = ResourceFactory.createCollection(url, targetSelector);
	} catch (Exception exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
	}
}
/** Lock this resource collection and recursively all its members based
* on the given parameters. This allows control of the lock scope 
*(exclusive or shared) the lock type (write), owner information, etc.
*
* @param scope the scope of the lock, exclusive or shared
* @param type the type of the lock, currently only write
* @param timeout the number of seconds before the lock times out or
*     0 for infinite timeout.
* @param owner an XML element containing useful information that can be
*     used to identify the owner of the lock. An href to a home page, an
*     email address, phone number, etc. Can be null if no owner information
*     is provided.
*
* @return a MultiStatus containing a lockdiscovery property indicating
* the results of the lock operation.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus lock(String scope, String type, int timeout, Element owner) throws WebDAVException {
	return lock(scope, type, timeout, owner, Collection.deep);
}
/** Lock this resource based on the given parameters. This allows control of
* the lock scope (exclusive or shared) the lock type (write), owner information, etc.
*
* @param scope the scope of the lock, exclusive or shared
* @param type the type of the lock, currently only write
* @param timeout the number of seconds before the lock times out or
*     0 for infinite timeout.
* @param owner an XML element containing useful information that can be
*     used to identify the owner of the lock. An href to a home page, an
*     email address, phone number, etc. Can be null if no owner information
*     is provided.
* @param depth
*     <ul>
*         <li>shallow lock only this resource</li>
*         <li>deep lock this resource and all its children</li>
*     </ul>
*
* @return a MultiStatus containing a lockdiscovery property indicating
* the results of the lock operation.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus lock(String scope, String type, int timeout, Element owner, String depth) throws WebDAVException {
	return ((IRCollection) impl).lock(context, scope, type, timeout, owner, depth);
}
}
