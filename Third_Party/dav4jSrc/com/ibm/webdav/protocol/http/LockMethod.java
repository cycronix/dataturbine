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
import java.io.*;
import java.util.logging.*;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;

/** Executes the WebDAV LOCK method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class LockMethod extends WebDAVMethod {
	
	private static Logger m_logger = Logger.getLogger(LockMethod.class.getName());

/** Construct a LockMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public LockMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "LOCK";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {

	try {
		setStatusCode(WebDAVStatus.SC_OK);

		// get information from the headers
		String depth = context.getRequestContext().depth();
		if (depth == null) {
			depth = Collection.deep;
		}
		if (!(depth.equals(Collection.shallow) || depth.equals(Collection.deep))) {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Depth header must be 0 or infinity");
		}

		int timeout = context.getRequestContext().getTimeout();
		context.setMethodName(methodName);
		String lockToken = null;
		Precondition precondition = context.getRequestContext().precondition();
		if (precondition != null) {
			lockToken = context.getRequestContext().precondition().toString();
		}
		if (lockToken != null && lockToken.length() > 2) {
			// strip off the (<...>) delimiters
			lockToken = lockToken.substring(2, lockToken.length() - 2);
		}

		// get the request entity body and lock or refresh the lock
		// on the resource
		Element lockinfo = null;
		// don't attempt to read an empty request body if there's a
		// lock token. It must be a refresh which doesn't have a request
		// entity body.
		if (lockToken == null) {
			WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(resource.getURL().toString());
		        /*Parser xmlParser = new Parser(resource.getURL().toString(), errorListener, null);
			xmlParser.setWarningNoDoctypeDecl(false);
			xmlParser.setProcessNamespace(true);
			Document contents = xmlParser.readStream(request.getReader());*/
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder docbuilder = factory.newDocumentBuilder();
                        docbuilder.setErrorHandler(errorHandler);
                        
                        Document contents = docbuilder.parse(new org.xml.sax.InputSource(request.getReader()));
			if (errorHandler.getErrorCount() > 0) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Syntax error in LOCK request entity body");
			}
			lockinfo = (Element) contents.getDocumentElement();
		}

                

		MultiStatus multiStatus = null;
		if (lockinfo != null) { // doing a lock


			Element lockscope = (Element)lockinfo.getElementsByTagNameNS("DAV:","lockscope").item(0);

                        

                        String type = null;
			Element locktype = (Element)lockinfo.getElementsByTagNameNS("DAV:", "locktype").item(0);
			String scope = null;
			if (lockscope != null) {
				if (lockscope.getElementsByTagNameNS("DAV:", "exclusive").item(0) != null) {
					scope = ActiveLock.exclusive;
				} else
					if (lockscope.getElementsByTagNameNS("DAV:", "shared").item(0) != null) {
						scope = ActiveLock.shared;
					}
			}

			if (locktype != null) {
				if (locktype.getElementsByTagNameNS("DAV:", "write").item(0) != null) {
					type = ActiveLock.writeLock;
				}
			}
			Element owner = (Element)lockinfo.getElementsByTagNameNS("DAV:", "owner").item(0);
			if (resource.exists() && resource.isCollection()) {
				multiStatus = ((CollectionImpl) resource).lock(context, scope, type, timeout, owner, depth);
			} else {
				multiStatus = resource.lock(context, scope, type, timeout, owner);
			}

		} else {

			if (lockToken != null) { // doing a refresh
				multiStatus = resource.refreshLock(context, lockToken, timeout);
			} else { // bad lock request
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "LOCK missing Lock-Token header or bad LOCK request entity body");
			}

                }
		if (multiStatus.getResponses().hasMoreElements()) {
			context.getResponseContext().contentType("text/xml");
			setResponseHeaders();

			// output the results as an XML document
			// if the lock request is on a single resource (depth=shallow),
			// output a document with a prop containing a lockdiscovery on the
			// resource as the document element. Otherwise, use a multistatus.
			//
			Document results = null;
			if (depth.equals(Collection.shallow)) {
				results = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				//results.setVersion(Resource.XMLVersion);
				Element prop = results.createElementNS("DAV:","D:prop");
				prop.setAttribute("xmlns:D", "DAV:");
				results.appendChild(prop);
				// the property must be a lockdiscovery property
				PropertyResponse response = (PropertyResponse) multiStatus.getResponses().nextElement();
				PropertyValue lockDiscovery = response.getProperty( PropertyName.pnLockdiscovery );
				if (lockDiscovery != null) {
					prop.appendChild(results.importNode(lockDiscovery.value,true));
				}
			} else {
				setStatusCode(WebDAVStatus.SC_MULTI_STATUS);
				results = (Document) multiStatus.asXML();
			}
			//((Document) results).setEncoding(getResponseCharset());
			PrintWriter pout = new PrintWriter(response.getWriter(), false);
			pout.print(XMLUtility.printNode(results.getDocumentElement()));
                        //((Document) results).print(pout);
			pout.close();
		} else {
			setResponseHeaders();
		}
	} catch (WebDAVException exc) {
		m_logger.log(Level.INFO, exc.getLocalizedMessage() + " - " + request.getQueryString());
		setStatusCode(exc.getStatusCode());
	} catch (Exception exc) {
		m_logger.log(Level.WARNING, exc.getMessage(), exc);
		setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
	}
	return context.getStatusCode();
}
}
