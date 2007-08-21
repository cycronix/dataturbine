/*
 * (C) Copyright Simulacra Media Ltd, 2004.  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * Simulacra Media Ltd will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will Simulacra Media Ltd be liable for any
 * special, indirect or consequential damages or lost profits even if
 * Simulacra Media Ltd has been advised of the possibility of their occurrence. 
 * Simulacra Media Ltd will not be liable for any third party claims against you.
 * 
 */

package com.ibm.webdav.protocol.http;


import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;
import com.ibm.webdav.impl.ResourceImpl;

import javax.servlet.http.*;
import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.util.Enumeration;
import org.w3c.dom.*;

/**
 * Executes the WebDAV BIND method.
 * 
 * @author Michael Bell
 * @since November 13, 2003
 */
public class BindMethod extends WebDAVMethod {
	/**
	 * Construct a BindMethod.
	 * 
	 * @param request
	 *            the servlet request
	 * @param response
	 *            the servlet response
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public BindMethod(HttpServletRequest request, HttpServletResponse response)
			throws WebDAVException {
		super(request, response);
		methodName = "BIND";
	}

	/**
	 * Execute the method.
	 * 
	 * @return the result status code
	 */
	public WebDAVStatus execute() {
		setStatusCode(WebDAVStatus.SC_CREATED); // the default status code
		MultiStatus multiStatus = null;
		try {
			context.setMethodName("BIND");

			Document contents = null;

			if (context.getRequestContext().contentLength() > 0) {
				// get the request entity body and parse it
				WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(
						resource.getURL().toString());

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);

				DocumentBuilder docbuilder = factory.newDocumentBuilder();
				docbuilder.setErrorHandler(errorHandler);
				contents = docbuilder.parse(new org.xml.sax.InputSource(request
						.getReader()));

				if (errorHandler.getErrorCount() > 0) {
					throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
							"Syntax error in PROPFIND request entity body");
				}
			}

			if (contents == null) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
						"No body in request");
			}

			Element bindEl = contents.getDocumentElement();

			if (bindEl.getLocalName().equals("bind") == false) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
						"Invalid root element in request body");
			}

			NodeList segmentNL = bindEl.getElementsByTagNameNS("DAV:",
					"segment");

			NodeList hrefNL = bindEl.getElementsByTagNameNS("DAV:", "href");

			if (segmentNL.getLength() != 1 || hrefNL.getLength() != 1) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
						"Invalid request body");
			}

			String segment = segmentNL.item(0).getFirstChild().getNodeValue();

			String href = hrefNL.item(0).getFirstChild().getNodeValue();

			multiStatus = resource.createBinding(context, segment, href);
			Enumeration responses = multiStatus.getResponses();
			if (responses.hasMoreElements()) {
				MethodResponse methodResponse = (MethodResponse) responses
						.nextElement();
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
						pout.print(XMLUtility.printNode(results
								.getDocumentElement()));
						//((Document) results).printWithFormat(pout);
					}
					PrintWriter pout = new PrintWriter(response.getWriter(),
							false);
					pout.print(XMLUtility.printNode(results
							.getDocumentElement()));
					//((Document) results).print(pout);
					pout.close();
				} else {
					// there was just one MethodResponse, so return it directly
					// instead
					// of wrapped in a multistatus
					setStatusCode(methodResponse.getStatus());
					setResponseHeaders();
				}
			} else {
				setResponseHeaders();
			}
		} catch (WebDAVException exc) {
			setStatusCode(exc.getStatusCode());

		} catch (Exception exc) {
			setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
		}
		return context.getStatusCode();
	}
}