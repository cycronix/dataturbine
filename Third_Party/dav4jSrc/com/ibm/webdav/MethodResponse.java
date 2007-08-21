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

import org.w3c.dom.*;
import java.util.*;
import javax.xml.parsers.*;

/** A MethodResponse describes the effect of a method on a resource. A
 * <code> MultiStatus</code> contains a collection of Response instances,
 * one for each resource effected by the method sent.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.PropertyResponse
 * @see com.ibm.webdav.MultiStatus
 */
public class MethodResponse extends Response {

   //------------------------------------------------------------------------

   private Vector resources = new Vector(); // other resources that have this
											// same status
   private int status = WebDAVStatus.SC_OK;
/** Construct a MethodResponse from an XML response element
 *
 * @param document the document containing the element
 * @param element the XML response element that is the source
 * of this response
 */
public MethodResponse(Document document, Element element) throws ServerException {
	super(document);
	try {
		NodeList hrefs = element.getElementsByTagNameNS("DAV:", "href");
		Element href = (Element) hrefs.item(0);
		setResource(((Text) href.getFirstChild()).getData());
		Element hrefn = null;
		for (int i = 1; i < hrefs.getLength(); i++) {
			hrefn = (Element) hrefs.item(i);
			addResource(((Text) hrefn.getFirstChild()).getData());
		}

		Element status = (Element) (element.getElementsByTagNameNS("DAV:", "status").item(0));
		String statusText = ((Text) status.getFirstChild()).getData();
		StringTokenizer statusFields = new StringTokenizer(statusText, " ");
		statusFields.nextToken(); // skip the HTTP version

		int statusCode = Integer.parseInt(statusFields.nextToken());
		setStatus(statusCode);

		Element responseDescription = (Element)element.getElementsByTagNameNS("DAV:", "responsedescription").item(0);
		if (responseDescription != null) {
			setDescription(((Text) responseDescription.getFirstChild()).getData());
		}
	} catch (Exception exc) {
		exc.printStackTrace();
		throw new ServerException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Invalid MethodResponse");
	}
}
/** Construct an empty Response for some resource.
 *
 * @param url the URL of the resource this is a response for
 * @param status the HTTP status code for the response.
 * @see com.ibm.webdav.WebDAVStatus
 */
public MethodResponse(String url, int status) {
	super(url);
	this.status = status;
}
/** Construct an empty Response for some resource.
 *
 * @param url the URL of the resource this is a response for
 * @param status the HTTP status code for the response.
 * @see com.ibm.webdav.WebDAVStatus
 */
public MethodResponse(String url, WebDAVException exc) {
	super(url);
	this.status = exc.getStatusCode();
	this.setDescription( exc.getMessage());
}
/** Add a URL to a resource that is effected by the method in the
 * same way as that returned by <code>getResource()</code>. The
 * URL can only be added once to this Response.
 *
 * @param url the resource URL
 * @exception com.ibm.webdav.ServerException thrown if the URL is already in the response
 */
public void addResource(String url) throws ServerException {
	boolean found = url.equals(getResource());
	if (!found) {
		Enumeration urls = getResources();
		while (!found && urls.hasMoreElements()) {
			String aUrl = (String) urls.nextElement();
			found = aUrl.equals(url);
		}
	}
	if (found) {
		throw new ServerException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Duplicate URL in a Response");
	} else {
		resources.addElement(url);
	}
}
/** Translate this MethodResponse into an XML response element.
 * @return a DAV:response XML element
 */
public Element asXML() {

	Element response = document.createElementNS("DAV:","D:response");

	Element href = document.createElementNS("DAV:","D:href");

	href.appendChild(document.createTextNode(getResource()));
	response.appendChild(href);
	Enumeration urls = getResources();
	while (urls.hasMoreElements()) {
		String url = (String) urls.nextElement();
		Element hrefn = document.createElementNS("DAV:","D:href");

		hrefn.appendChild(document.createTextNode(url));
		response.appendChild(hrefn);
	}

	Element status = document.createElementNS("DAV:","D:status");

	status.appendChild(document.createTextNode(HTTPVersion + " " + getStatus() + " " + WebDAVStatus.getStatusMessage(getStatus())));
	response.appendChild(status);

	if (getDescription() != null) {
		Element description = document.createElementNS("DAV:","D:responsedescription");

		description.appendChild(document.createTextNode(getDescription()));
		response.appendChild(description);
	}
	return response;
}
/** Other resources effected by the method that share the
 * status response in this MethodResponse
 *
 * @return an Enumeration of Strings representing URLs of resources that
 *     responded in the same way to a request.
 */
public Enumeration getResources() {
	return resources.elements();
}
/** Get the status of the URLs in this MethodResponse.
 *
 * @return the HTTP status code for this response
 */
public int getStatus() {
	return status;
}
/** Check to see if this response does not contain an error.
 *
 * @return true if all response status code is less that 300, false otherwise.
 */
public boolean isOK() {
	return status < 300;
}
/** Set the status of the URLs in this MethodResponse.
 *
 * @param value an HTTP status code
 * @see com.ibm.webdav.WebDAVStatus
 */
public void setStatus(int value) {
	status = value;
}
/** Convert this Response to a PropertyResponse.
 * This method is used to convert MethodResponses to PropertyResponses
 * when an error occurred accessing the properties of some member.
 *
 */
public PropertyResponse toPropertyResponse() {
	PropertyResponse response = new PropertyResponse(getResource());
	response.setDescription(getDescription());
	try {
          response.setDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
	} catch(Exception e) {
          e.printStackTrace(System.err);
        }
        return response;
}
}
