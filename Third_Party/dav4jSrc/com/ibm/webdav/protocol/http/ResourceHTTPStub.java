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
import com.ibm.webdav.protocol.http.WebDAVURLConnection;
import com.ibm.webdav.protocol.http.WebDAVAuthenticator;
import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;
import com.ibm.webdav.ServerException;
import java.io.*;
import java.net.URL;
import org.w3c.dom.*;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import javax.xml.parsers.*;

/** Implements a client-side proxy stub of the Resource interface using HTTP plus WebDAV extensions
 * as a remote procedure call protocol. It does this by creating a WebDAVURLConnection
 * and dispatching the request to the servlet ResourceHTTPSkel which in turn dispatches
 * to ResourceImpl. This is the place to see how the DAV4J client API is mapped to
 * the WebDAV protocol.
 */
public class ResourceHTTPStub implements IRResource {
	protected URL url = null;

	// contexts for communicating HTTP and WebDAV headers (method contol couples)
	protected ResourceContext context = new ResourceContext();

	static {

		WebDAVURLConnection.setDefaultAuthenticator(new WebDAVAuthenticator());
	}

	protected WebDAVURLConnection connection = null;

		// properties taken from the dav4j.properties file.
	protected static Properties properties = ResourceFactory.properties;
/** The default constructor. Should be rarely used.
 */
public ResourceHTTPStub() {
	this.url = null;
}
/** Construct a ResourceHTTPStub with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details.
 *
 * @param url the URL of the resource.
 * @exception MalformedURLException
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public ResourceHTTPStub(String url) throws java.io.IOException {
	this(new URL(url), (TargetSelector)null);
}
/** Create a ResourceHTTPStub from the given URL components.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param port the TCP port to use. HTTP uses 80 by default.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public ResourceHTTPStub(String protocol, String host, int port, String file) throws java.io.IOException {
	this(new URL(protocol, host, port, file), (TargetSelector)null);
}
/** Create a ResourceHTTPStub from the given URL components. This constructor uses the default
 * HTTP port.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public ResourceHTTPStub(String protocol, String host, String file) throws java.io.IOException {
	this(new URL(protocol, host, file), (TargetSelector)null);
}
/** Construct a ResourceHTTPStub with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details.
 *
 * @param url the URL of the resource.
 * @param targetSelector the revision target selector for this Collection
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public ResourceHTTPStub(URL url, TargetSelector targetSelector) throws WebDAVException {
	this.url = url;
}
/** Construct a ResourceP with the given URL specification in the given context.  The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details.
 *
 * @param context a URL giving the context in which the spec is evaluated
 * @param spec a URL whose missing parts are provided by the context
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public ResourceHTTPStub(URL context, String spec) throws java.io.IOException {
	this(new URL(context, spec), (TargetSelector)null);
}
/** This method must be called after the client has completed writing to the contents
 * output stream that was obtained from <code>getContentsOutputStream()</code>.
 * @exception com.ibm.webdav.WebDAVException
 */
public void closeContentsOutputStream(ResourceContext context) throws WebDAVException {
	this.closeContentsOutputStream(context, null);
}

public void closeContentsOutputStream(ResourceContext context,String sContentType) throws WebDAVException {
	this.context = context;

	getResults();
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
 * </ul>
 *
 * @return the status of the copy operation for each resource copied
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus copy(ResourceContext context, String destinationURL, boolean overwrite, Vector propertiesToCopy) throws WebDAVException {
	this.context = context;

	context.getRequestContext().overwrite(overwrite ? "T" : "F");
	context.getRequestContext().destination(destinationURL);

	setupRequest("COPY");

	return doCopyOrMove(propertiesToCopy);
}
/** Delete this resouce from the server. The actual effect of the delete operation is
 * determined by the underlying repository manager. The visible effect to WebDAV
 * is that the resource is no longer available.
 *
 * @return a MultiStatus containing the status of the delete method on each
 *         effected resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus delete(ResourceContext context) throws WebDAVException {
	this.context = context;

	setupRequest("DELETE");
	getResults();

	return responseToMultiStatus();
}
/** Do the work involved in a remote procedure call for a copy or move
* operation.
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
private MultiStatus doCopyOrMove(Vector properties) throws WebDAVException {
	context.getRequestContext().contentType("text/xml");

	// Convert the propertiesToCopy to XML
	Document document = null;
        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element propertybehavior = document.createElementNS("DAV:","D:propertybehavior");
	propertybehavior.setAttribute("xmlns:D", "DAV:");
	document.appendChild(propertybehavior);

	if (properties == null) {
		propertybehavior.appendChild(document.createElementNS("DAV:","D:omit"));
	} else {
		Element keepalive = document.createElementNS("DAV:","D:keepalive");
		propertybehavior.appendChild(keepalive);
		if (properties.isEmpty()) {
			keepalive.appendChild(document.createTextNode("*"));
		} else {
			Enumeration props = properties.elements();
			while (props.hasMoreElements()) {
				String uri = (String) props.nextElement();
				Element href = document.createElementNS("DAV:","D:href");
				href.appendChild(document.createTextNode(uri));
				keepalive.appendChild(href);
			}
		}
	}

	// output the properties to copy request entity
	connection.setDoOutput(true);
	try {
		Writer writer = new OutputStreamWriter(connection.getOutputStream(), Resource.defaultCharEncoding);
		PrintWriter pw = new PrintWriter(writer, false);
		pw.print(XMLUtility.printNode(document.getDocumentElement()));
                //document.print(pw);
		pw.flush();
		getResults();
	} catch (java.io.UnsupportedEncodingException exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Unsupported encoding requiested");
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	return responseToMultiStatus();
}
/** Get an InputStream for accessing the contents of this resource. This method may provide
 * more efficient access for resources that have large contents. Clients may want to create
 * a Reader to perform appropriate character conversions on this stream.
 *
 * @return an InputStream on the contents
 * @exception com.ibm.webdav.WebDAVException
 */
public InputStream getContentsInputStream(ResourceContext context) throws WebDAVException {
	this.context = context;
         
	setupRequest("GET");
	getResults();
	InputStream stream = null;
	try {
		stream = connection.getInputStream();
	} catch (WebDAVException exc) {
                System.err.println("ResourceHTTPStub.getContentsInputStream: Exception -");
		exc.printStackTrace(System.err);
                throw exc;
	} catch (java.io.IOException exc) {
		System.err.println("ResourceHTTPStub.getContentsInputStream: Exception -");
		exc.printStackTrace(System.err);
                throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	return stream;
}
/** Get an OutputStream for setting the contents of this resource. This method may provide
 * more efficient access for resources that have large contents. Remember to call
 * closeContentsOutputStream() when all the data has been written.
 *
 * @return an OutputStream to set the contents
 * @exception com.ibm.webdav.WebDAVException
 */
public OutputStream getContentsOutputStream(ResourceContext context) throws WebDAVException {
	this.context = context;
	HTTPHeaders requestContext = context.getRequestContext();
	if (requestContext.contentType() == null) {
		requestContext.contentType("text/plain");
	}

	setupRequest("PUT");
	connection.setDoOutput(true);
	OutputStream stream = null;
	try {
		stream = connection.getOutputStream();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	return stream;
}
/** This method can be used for obtaining meta-information about this resource without
 * actually reading the resource contents. This meta-information is maintained by the server
 * in addition to the resource properties. The meta-information is obtained from an
 * HTTP HEAD method.</p>
 * <p>
 * After this call, the resource context has been updated and
 * <code>getStatusCode()</code>, <code>getStatusMessage()</code>, and <code>getResponseContext()</code>
 * as well as all the ResourceContext methods return updated values based on the current
 * state of the resource.</p>
 * <p>This methods corresponds to the HTTP HEAD method.</p>
 * <p>
 * Do a getContentsInputStream() to set the response context,
 * then just don't return the stream.
 * @exception com.ibm.webdav.WebDAVException
 */
public void getMetaInformation(ResourceContext context) throws WebDAVException {
	this.context = context;

	setupRequest("HEAD");
	getResults();
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
public MultiStatus getProperties(ResourceContext context) throws WebDAVException {
	this.context = context;
	context.getRequestContext().contentType("text/xml");
	 // default is deep in WebDAV, but we don't want this behavior
	if (context.getRequestContext().depth() == null) {
		context.getRequestContext().depth(Collection.thisResource);
	}

	setupRequest("PROPFIND");

	Document requestBody = null;
        try {
          requestBody = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
	//requestBody.setVersion(Resource.XMLVersion);
	//requestBody.setEncoding(Resource.defaultXMLEncoding);
	Element propfind = requestBody.createElementNS("DAV:","D:propfind");
	propfind.setAttribute("xmlns:D", "DAV:");
	requestBody.appendChild(propfind);
	propfind.appendChild(requestBody.createElementNS("DAV:","D:allprop"));

	connection.setDoOutput(true);
	try {
		Writer writer = new OutputStreamWriter(connection.getOutputStream(), Resource.defaultCharEncoding);
		PrintWriter pw = new PrintWriter(writer, false);
		pw.print(XMLUtility.printNode(requestBody.getDocumentElement()));
                //requestBody.print(pw);
		pw.flush();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO error or bad encoding");
	}

	getResults();

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
public MultiStatus getProperties(ResourceContext context, PropertyName names[]) throws WebDAVException {
	this.context = context;
	context.getRequestContext().contentType("text/xml");
	 // default is deep in WebDAV, but we don't want this behavior
	if (context.getRequestContext().depth() == null) {
		context.getRequestContext().depth(Collection.thisResource);
	}

	setupRequest("PROPFIND");

	Document requestBody = null;
        try {
          requestBody = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
        //requestBody.setVersion(Resource.XMLVersion);
	//requestBody.setEncoding(Resource.defaultXMLEncoding);
	Element propfind = requestBody.createElementNS("DAV:","D:propfind");
	propfind.setAttribute("xmlns:D", "DAV:");
	requestBody.appendChild(propfind);
	Element prop = requestBody.createElementNS("DAV:","D:prop");
	propfind.appendChild( prop );
	for (int i = 0; i < names.length; i++) {
		// substitute the prefix for any namespace
		// todo: has to iterate through all the namespaces
		PropertyName name = names[i];
		Element el = requestBody.createElementNS("DAV:","D:"+name.getLocal());
		if (!name.getNamespace().equals("DAV:")) {
			el.setAttribute( "xmlns:D", name.getNamespace());
		}

		prop.appendChild( el );
	}

	connection.setDoOutput(true);
	try {
		Writer writer = new OutputStreamWriter(connection.getOutputStream(), Resource.defaultCharEncoding);
		PrintWriter pw = new PrintWriter(writer, false);
		pw.print(XMLUtility.printNode(requestBody.getDocumentElement()));
                //requestBody.print(pw);
		pw.flush();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO error or bad encoding");
	}
	getResults();

	return responseToMultiStatus();
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
public MultiStatus getPropertyNames(ResourceContext context) throws WebDAVException {
	this.context = context;
	context.getRequestContext().contentType("text/xml");
	 // default is deep in WebDAV, but we don't want this behavior
	if (context.getRequestContext().depth() == null) {
		context.getRequestContext().depth(Collection.thisResource);
	}

	setupRequest("PROPFIND");

	Document requestBody = null;
        try {
          requestBody = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
        //requestBody.setVersion(Resource.XMLVersion);
	//requestBody.setEncoding(Resource.defaultXMLEncoding);
	Element propfind = requestBody.createElementNS("DAV:","D:propfind");
	propfind.setAttribute("xmlns:D", "DAV:");
	requestBody.appendChild(propfind);
	propfind.appendChild(requestBody.createElementNS("DAV:","D:propname"));
	connection.setDoOutput(true);

	try {
		Writer writer = new OutputStreamWriter(connection.getOutputStream(), Resource.defaultCharEncoding);
		PrintWriter pw = new PrintWriter(writer, false);
		pw.print(XMLUtility.printNode(requestBody.getDocumentElement()));
                //requestBody.print(pw);
		pw.flush();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO error or bad encoding");
	}

	getResults();

	return responseToMultiStatus();
}
/** Get the response entity for the previous WebDAVURLConnection request.
* @return the response entity contents
* @exception com.ibm.webdav.WebDAVException
*/
protected byte[] getResponseEntity() throws WebDAVException {
	byte[] data = new byte[0];
	try {
		BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
		int length = (int) context.getResponseContext().contentLength();
		if (length != -1) {
			int rcvd = 0;
			int size = 0;
			data = new byte[length];
			do {
				size += rcvd;
				rcvd = is.read(data, size, length - size);
			} while (size < length && rcvd != -1);
			if (rcvd == -1)
			// premature EOF
				data = resizeArray(data, size);
		} else {
			data = new byte[0];
			int inc = 8192;
			int off = data.length;
			int rcvd = 0;
			do {
				off += rcvd;
				data = resizeArray(data, off + inc);
				rcvd = is.read(data, off, inc);
			} while (rcvd != -1);
			data = resizeArray(data, off);
		}
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	return data;
}
/** Cause a WebDAVURLConnection request to be sent and get the
* status code and message. After this method has been called,
* getResponseEntity() can be called.
* @exception com.ibm.webdav.WebDAVException
*/
protected void getResults() throws WebDAVException {
	// cause the request to be sent and raise any necessary exceptions
	String statusMessage = null;
	int responseCode = 0;
	try {
		responseCode = connection.getResponseCode();
		context.getStatusCode().setStatusCode(responseCode);
		statusMessage = connection.getResponseMessage();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}

	if (statusMessage == null || statusMessage.equals("Unknown")) {
		statusMessage = context.getStatusCode().getStatusMessage();
	}
	// copy the response and contents headers into the response context
	// Note the index starts at 1 not 0
	int i = 1;
	String name = null;
	while ((name = connection.getHeaderFieldKey(i)) != null) {
		name = name.toLowerCase();
		String value = connection.getHeaderField(i);
		context.getResponseContext().put(name, value);
		i++;
	}

	if (responseCode >= 300 && responseCode <= 399) {
		throw new RedirectionException(responseCode, statusMessage);
	} else if (responseCode >= 400 && responseCode <= 499) {
		throw new ClientException(responseCode, statusMessage);
	} else if (responseCode >= 500 && responseCode <= 599) {
		throw new ServerException(responseCode, statusMessage);
	}
}
/** Lock this resource collection and potentially all its members
* based on the given parameters. This allows control of the lock
* scope (exclusive or shared) the lock type (write), owner information, etc.
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
public MultiStatus lock(ResourceContext context, String scope, String type, int timeout, Element owner) throws WebDAVException {
	this.context = context;

	context.getRequestContext().contentType("text/xml");
	context.getRequestContext().setTimeout(timeout);
	context.getRequestContext().precondition((String) null);
	 // default is deep in WebDAV, but we don't want this behavior
	if (context.getRequestContext().depth() == null) {
		context.getRequestContext().depth(Collection.thisResource);
	}

	setupRequest("LOCK");

	// construct the request entity body
	Document requestBody = null;
        try {
          requestBody = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
        //requestBody.setVersion(Resource.XMLVersion);
	//requestBody.setEncoding(Resource.defaultXMLEncoding);
	Element lockinfo = requestBody.createElementNS("DAV:","D:lockinfo");
	lockinfo.setAttribute("xmlns:D", "DAV:");
	requestBody.appendChild(lockinfo);
	Element lockscope = requestBody.createElementNS("DAV:","D:lockscope");
	lockscope.appendChild(requestBody.createElementNS("DAV:","D:" + scope));
	lockinfo.appendChild(lockscope);
	Element locktype = requestBody.createElementNS("DAV:","D:locktype");
	locktype.appendChild(requestBody.createElementNS("DAV:","D:" + type));
	lockinfo.appendChild(locktype);
	if (owner != null) {
		lockinfo.appendChild((Element) owner);
	}

	// output the request entity body
	connection.setDoOutput(true);
	try {
		PrintWriter pw = new PrintWriter(connection.getOutputStream(), false);
		pw.print(XMLUtility.printNode(requestBody.getDocumentElement()));
                //requestBody.print(pw);
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		System.err.println(exc);
		exc.printStackTrace();
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}

	// either a prop containing a lockdiscovery, or a multistatus
	getResults();

	return lockResponseToMultiStatus();
}
/** Convert the response from a LOCK method (an XML multistatus with a prop element
* containing a lockdiscovery) into a MultiStatus.
* @return a MultiStatus created from the DAV:multistatus from a LOCK method
* @exception com.ibm.webdav.ServerException thrown if there is a syntax error in the XML DAV:multistatus
*/
protected MultiStatus lockResponseToMultiStatus() throws WebDAVException {
	// parse the response
	WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(url.toString());
	Reader reader = null;
        Document document = null;

        /*Parser xmlParser = new Parser(url.toString(), errorListener, null);
	xmlParser.setWarningNoDoctypeDecl(false);
	xmlParser.setProcessNamespace(true);*/


	try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                docbuilder.setErrorHandler(errorHandler);
		reader = new InputStreamReader(connection.getInputStream(), Resource.defaultCharEncoding);
                document = docbuilder.parse(new org.xml.sax.InputSource(reader));
        } catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	} catch (Exception exc) {
          throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, exc.getMessage());
        }


	if (errorHandler.getErrorCount() > 0) {
		context.getStatusCode().setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
		throw new ServerException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Syntax error in multistatus response entity body");
	}

	// see if it's a multistatus
	if (context.getStatusCode().getStatusCode() != WebDAVStatus.SC_MULTI_STATUS) {
		// it's a prop containing a lockdiscovery. Put the prop in a propstat in a
		// response and then convert that to a MultiStatus
		Element prop = document.getDocumentElement();
		Element response = document.createElementNS("DAV:","D:response");

		Element href = document.createElementNS("DAV:","D:href");
		href.appendChild(document.createTextNode(url.toString()));

		Element status = document.createElementNS("DAV:","D:status");
		String statusText = Response.HTTPVersion + " " + context.getStatusCode().getStatusCode() + " " + context.getStatusCode().getStatusMessage();
		status.appendChild(document.createTextNode(statusText));

		Element propstat = document.createElementNS("DAV:","D:propstat");
		propstat.appendChild((Element) ((Element) prop).cloneNode(true));
		propstat.appendChild(status);
		response.appendChild(href);
		response.appendChild(propstat);

		Element multistatus = document.createElementNS("DAV:","D:multistatus");
		multistatus.setAttribute("xmlns:D", "DAV:");
		multistatus.appendChild(response);
		Document newDocument = null;
                try {
                  newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch(Exception exc) {
                  throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, exc.getMessage());
                }
                //newDocument.setVersion(Resource.XMLVersion);
		//newDocument.appendChild(multistatus);
		document = newDocument;
	}
	MultiStatus multiStatus = new MultiStatus(document);
	return multiStatus;
}
/** Move this resource to the destination URL.
 * Partial results are possible, check the returned status for details
 *
 * @param destinationURL the destination
 * @param overwrite true implies overrite the destination if it exists
 * @param propertiesToMove a collection of properties that must be moved or
 * the method will fail. propertiesToMove may have one of the following values:
 * <ul>
 *    <li>null - ignore properties that cannot be moved</li>
 *    <li>empty collection - all properties must be moved or the method will fail</li>
 *    <li>a collection of URIs - a list of the properties that must be moved
 *        or the method will fail</li>
 * </ul>
 *
 * @return the status of the move operation for each resource moved
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus move(ResourceContext context, String destinationURL, boolean overwrite, Vector propertiesToMove) throws WebDAVException {
	this.context = context;

	context.getRequestContext().depth(Collection.deep); // (always deep for move, set it anyway)
	context.getRequestContext().overwrite(overwrite ? "T" : "F");
	context.getRequestContext().destination(destinationURL);

	setupRequest("MOVE");

	return doCopyOrMove(propertiesToMove);
}
/** This method treats this resource as a method or service, and sends its parameter to
 * this resource where it is handled in a resource-specific way. For example,
 * sending data from an HTML form to a URL representing a Servlet or CGI script that processes
 * the form data to produce some result.
 *
 * @param args a string representing the arguments to the method represented by this URL. The
 * arguments are in the form ?parameterName1=value1&amp;parameterName2=value2... as specified
 * for URL queries.
 *
 * @return the results of sending the arguments to the URL
 * @exception com.ibm.webdav.WebDAVException
 */
public byte[] performWith(ResourceContext context, String args) throws WebDAVException {
	this.context = context;

	setupRequest("POST");
	putRequestEntity(args.getBytes());
	getResults();
	return getResponseEntity();
}
/** Write the request entity body to the WebDAVURLConnection.
* @param value the request entity body to write
* @exception com.ibm.webdav.WebDAVException
*/
protected void putRequestEntity(byte[] value) throws WebDAVException {
	connection.setDoOutput(true);
	try {
		BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
		outputStream.write(value, 0, value.length);
		outputStream.flush();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
}
/** Refresh the lock on this resource by resetting the lock timeout.
 * The context must contain the proper authorization for the requesting
 * principal.
 *
 * @param lockToken the lock token identifying the lock.
 * @param timeout the new timeout in seconds. -1 means infinite timeout.
 *
 * @return updated information about the lock status of this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus refreshLock(ResourceContext context, String lockToken, int timeout) throws WebDAVException {
	this.context = context;

	// on lock refresh, the precondition (If header) must contain the
	// lock token
	context.getRequestContext().precondition("(<" + lockToken + ">)");
	context.getRequestContext().setTimeout(timeout);
	 // default is deep in WebDAV, but we don't want this behavior
	context.getRequestContext().depth(Collection.thisResource);

	// A lock refresh doesn't have a request entity body
	setupRequest("LOCK");

	// either a prop containing a lockdiscovery, or a multistatus
	getResults();
	return lockResponseToMultiStatus();
}
/** A utility to resize a byte array and copy its current contents.
 * @param src the source array
 * @param new_size the new size to make the array
 * @param the newly sized array (may be smaller than src)
 */
private final static byte[] resizeArray(byte[] src, int new_size) {
	byte tmp[] = new byte[new_size];
	System.arraycopy(src, 0, tmp, 0, (src.length < new_size ? src.length : new_size));
	return tmp;
}
/** Convert the response (an XML multistatus or simple status code) into
* a MultiStatus.
* @return a MultiStatus constructed from a DAV:multistatus or response code
* @exception com.ibm.webdav.ServerException thrown if there is a syntax error in the DAV:multistatus
*/
protected MultiStatus responseToMultiStatus() throws WebDAVException {
	MultiStatus multiStatus = null;
	if (context.getStatusCode().getStatusCode() == WebDAVStatus.SC_MULTI_STATUS) {
		BufferedReader reader = null;
                Document document = null;
                //WebDAVErrorListener errorListener = new WebDAVErrorListener(url.toString());
                WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(url.toString());


                try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                docbuilder.setErrorHandler(errorHandler);
                /*Parser xmlParser = new Parser(url.toString(), errorListener, null);
		xmlParser.setWarningNoDoctypeDecl(false);
		xmlParser.setProcessNamespace(true);*/


			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Resource.defaultCharEncoding));
		        document = docbuilder.parse(new org.xml.sax.InputSource(reader));
                } catch (WebDAVException exc) {
			throw exc;
		} catch (java.io.IOException exc) {
			throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error or bad encoding");
		} catch (Exception exc) {
			throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, exc.getMessage());
		}

		if (errorHandler.getErrorCount() > 0) {
			context.getStatusCode().setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
			throw new ServerException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Syntax error in multistatus response entity body");
		}
		multiStatus = new MultiStatus(document);
	} else {
		multiStatus = new MultiStatus();
		MethodResponse response = new MethodResponse(url.toString(), context.getStatusCode().getStatusCode());
		response.setDescription(context.getStatusCode().getStatusMessage());
		multiStatus.addResponse(response);
	}
	return multiStatus;
}
/** Edit the properties of a resource. The updates must refer to a Document containing a WebDAV
 * DAV:propertyupdates element as the document root.
 *
 * @param updates an XML Document containing DAV:propertyupdate elements
 * describing the edits to be made
 * @return a MultiStatus indicating the status of the updates
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus setProperties(ResourceContext context, Document updates) throws WebDAVException {
	this.context = context;
	context.getRequestContext().contentType("text/xml");

	setupRequest("PROPPATCH");

	// output the request entity body
	connection.setDoOutput(true);
	try {
		PrintWriter pw = new PrintWriter(connection.getOutputStream(), false);
		pw.print(XMLUtility.printNode(updates.getDocumentElement()));

                //((Document) updates).print(pw);
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}

	getResults();

	return responseToMultiStatus();
}
/** Setup a WebDAVURLConnection by opening a connection and setting the
* method. This should be done before outputting any request entity body.
* @param method the HTTP/1.1 or WebDAV request method
* @exception com.ibm.webdav.WebDAVException
*/
protected void setupRequest(String method) throws WebDAVException {
	// open a connection
	try {
                connection = new WebDAVURLConnection(url,"localhost",url.getPort());

		//connection = (WebDAVURLConnection) url.openConnection();
		connection.setRequestMethod(method);
	} catch (java.net.ProtocolException exc) {
		exc.printStackTrace();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}

	// put the context into the request headers
	Enumeration propertyNames = context.getRequestContext().keys();
	while (propertyNames.hasMoreElements()) {
		String name = (String) propertyNames.nextElement();
		String value = (String) context.getRequestContext().get(name);
		connection.setRequestProperty(name, value);
	}
}
/** Unlock the lock identified by the lockToken on this resource. The request context
 * must contain the proper authorization.
 *
 * @param lockToken the lock token obtained from the ActiveLock of a previous <code>lock() </code>
 * or <code>getLocks()</code>.
 *
 * @return a MultiStatus containing any responses on resources that could not
 *     be unlocked.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus unlock(ResourceContext context, String lockToken) throws WebDAVException {
	this.context = context;

	context.getRequestContext().lockToken(lockToken);

	setupRequest("UNLOCK");

	// a status code or a multistatus if the unlock fails
	getResults();

	return responseToMultiStatus();
}
}
