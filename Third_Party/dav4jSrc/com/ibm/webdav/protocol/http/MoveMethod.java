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
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.Collection;
import com.ibm.webdav.impl.*;

/** Executes the WebDAV MOVE method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class MoveMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(MoveMethod.class.getName());

/** Construct a MoveMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public MoveMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "MOVE";
        context.setMethodName("MOVE");
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	setStatusCode(WebDAVStatus.SC_CREATED); // the default status code
	MultiStatus multiStatus = null;
	try {
		// get any arguments out of the headers
		String destination = context.getRequestContext().destination();
		String overwriteString = context.getRequestContext().overwrite();
		boolean overwrite = overwriteString != null && overwriteString.equals("T");
		String depth = context.getRequestContext().depth();
		if (depth == null) {
			depth = Collection.deep;
		}
		if (!depth.equals(Collection.deep)) {
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Depth header must be infinity if present");
		}

		// get the request entity body and parse it. This will contain the rules
		// for moving properties
		Vector propertiesToMove = null;
		if (request.getContentLength() > 0) {
			WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(resource.getURL().toString());
                        /*Parser xmlParser = new Parser(resource.getURL().toString(), errorListener, null);
			xmlParser.setWarningNoDoctypeDecl(false);
			xmlParser.setProcessNamespace(true);
			Document document = xmlParser.readStream(request.getReader());*/
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder docbuilder = factory.newDocumentBuilder();
                        docbuilder.setErrorHandler(errorHandler);
                        Document document = docbuilder.parse(new org.xml.sax.InputSource(request.getReader()));
			if (errorHandler.getErrorCount() > 0) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Syntax error in MOVE request entity body");
			}
			Element propertybehavior = (Element) document.getDocumentElement();
			Element keepalive = (Element) propertybehavior.getElementsByTagNameNS("DAV:", "keepalive").item(0);
			if (keepalive != null) {
				propertiesToMove = new Vector();
				NodeList hrefs = keepalive.getElementsByTagNameNS("DAV:", "href");
				Element href = null;
				for (int i = 0; i < hrefs.getLength(); i++) {
					href = (Element) hrefs.item(i);
					String propertyURI = ((Text) href.getFirstChild()).getData();
					propertiesToMove.addElement(propertyURI);
				}
			}
		} // endif (request has body)
                context.setMethodName("MOVE");
		multiStatus = resource.move(context, destination, overwrite, propertiesToMove);
		Enumeration responses = multiStatus.getResponses();
		if (responses.hasMoreElements()) {
			MethodResponse methodResponse = (MethodResponse) responses.nextElement();
			if (responses.hasMoreElements()) {
				// there's more than one response, so return a multistatus
				context.getResponseContext().contentType("text/xml");
				setStatusCode(WebDAVStatus.SC_MULTI_STATUS);
				setResponseHeaders();

				// output the results as an XML document
				Document results = multiStatus.asXML();
				//((Document) results).setEncoding(getResponseCharset());
				if (ResourceImpl.debug) {
					System.err.println("move results:");
					PrintWriter pout = new PrintWriter(System.err);
					pout.print(XMLUtility.printNode(results.getDocumentElement()));
                                        //((Document) results).printWithFormat(pout);
				}
				PrintWriter pout = new PrintWriter(response.getWriter(), false);
				pout.print(XMLUtility.printNode(results.getDocumentElement()));
                                        //((Document) results).print(pout);
				pout.close();
			} else {
				// there was just one MethodResponse, so return it directly instead
				// of wrapped in a multistatus
				setStatusCode(methodResponse.getStatus());
				setResponseHeaders();
			}
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
