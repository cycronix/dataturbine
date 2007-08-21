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
 * 
 * Portions Copyright (C) Simulacra Media Ltd, 2004.
 */
import com.ibm.webdav.*;
import com.ibm.webdav.impl.IRCollection;
import java.util.*;
import java.io.*;
import java.net.URL;
import org.w3c.dom.*;

/** A CollectionHTTPStub is a ResourceHTTPStub that contains other
 * resources including other CollectionHTTPStubs. It provides a
 * concrete, client side implementation of Collection for client/server
 * communication over HTTP.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class CollectionHTTPStub extends ResourceHTTPStub implements IRCollection {

   private Vector members = null;   // lazy retrieve the members of the collection for the server
public CollectionHTTPStub() {
	super();
}
/** Construct a CollectionHTTPStub with the given URL. The collection having
* the url may not exist as this constructor does not access the resource from
* the server. Use exists() or attmept to get the members of the collection to
* see if it exists. Other constructors are provided using parameters for the
* various parts of the URL. See java.net.URLConnection.
*
* @param url the URL of the resource.
* @exception com.ibm.webdav.WebDAVException
*/
public CollectionHTTPStub(String url) throws WebDAVException {
	if (!url.endsWith("/")) {
		url = url + "/";
	}
	try {
		this.url = new URL(url);
	} catch (java.net.MalformedURLException exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
	}
}
/** Create a CollectionHTTPStub from the given URL components.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param port the TCP port to use. HTTP uses 80 by default.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public CollectionHTTPStub(String protocol, String host, int port, String file) throws WebDAVException {
	if (!file.endsWith("/")) {
		file = file + "/";
	}
	try {
		this.url = new URL(protocol, host, port, file);
	} catch (java.net.MalformedURLException exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
	}
}
/** Create a CollectionHTTPStub from the given URL components. This constructor uses the default
 * HTTP port.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.ResourceFactory
 */
public CollectionHTTPStub(String protocol, String host, String file) throws WebDAVException {
	if (!file.endsWith("/")) {
		file = file + "/";
	}
	try {
		this.url = new URL(protocol, host, file);
	} catch (java.net.MalformedURLException exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
	}
}
/** Construct a CollectionHTTPStub with the given URL. The resource having
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
public CollectionHTTPStub(URL url, TargetSelector targetSelector) throws WebDAVException {
	super(url, targetSelector);
}
/** Construct a CollectionHTTPStub with the given URL specification in the given context.
 * The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Collection.
 *
 * @param context a URL giving the context in which the spec is evaluated
 * @param spec a URL whose missing parts are provided by the context
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public CollectionHTTPStub(URL context, String spec) throws WebDAVException {
	if (!spec.endsWith("/")) {
		spec = spec + "/";
	}
	try {
		this.url = new URL(context, spec);
	} catch (java.net.MalformedURLException exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
	}
}
/** Lock this resource collection and potentially all its members
* based on the given parameters. This allows control of the lock
* scope (exclusive or shared) the lock type (write), owner information, etc.
*
* @param scope the scope of the lock, exclusive or shared
* @param type the type of the lock, currently only write
* @param depth
*     <ul>
*         <li>shallow lock only this resource</li>/** Copy this resource to the destination URL.
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
 * </ul>
 *
 * @return the status of the copy operation for each resource copied
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus copy(ResourceContext context, String destinationURL, boolean overwrite, Vector propertiesToCopy, String depth) throws WebDAVException {
	context.getRequestContext().depth(depth);
	return super.copy(context, destinationURL, overwrite, propertiesToCopy);
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
public MultiStatus createCollection(ResourceContext context, Document contents) throws WebDAVException {
	this.context = context;

	try {
		if (contents != null) {
			connection.setDoOutput(true);
			context.getRequestContext().contentType("text/xml");
		}
		setupRequest("MKCOL");
		if (contents != null) {
			OutputStream os = connection.getOutputStream();
			PrintWriter pw = new PrintWriter(os, false);
			pw.print(XMLUtility.printNode(contents.getDocumentElement()));
                        //((Document) contents).printWithFormat(pw);
			pw.flush();
		}

		getResults();

	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Exception");
	}
	return responseToMultiStatus();
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
public MultiStatus getProperties(ResourceContext context, PropertyName names[], String depth) throws WebDAVException {
	context.getRequestContext().depth(depth);
	return super.getProperties(context, names);
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
public MultiStatus getProperties(ResourceContext context, String depth) throws WebDAVException {
	context.getRequestContext().depth(depth);
	return super.getProperties(context);
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
public MultiStatus getPropertyNames(ResourceContext context, String depth) throws WebDAVException {
	context.getRequestContext().depth(depth);
	return super.getPropertyNames(context);
}
/** Lock this resource collection and potentially all its members
* based on the given parameters. This allows control of the lock
* scope (exclusive or shared) the lock type (write), owner information, etc.
*
* @param scope the scope of the lock, exclusive or shared
* @param type the type of the lock, currently only write
* @param depth
*     <ul>
*         <li>shallow lock only this resource</li>
*         <li>deep lock this resource and all its children</li>
*     </ul>
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
public MultiStatus lock(ResourceContext context, String scope, String type, int timeout, Element owner, String depth) throws WebDAVException {
	context.getRequestContext().depth(depth);
	return super.lock(context, scope, type, timeout, owner);
}
}
