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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;

/**
 * Executes the WebDAV ordered collections ORDERPATCH method.
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 * @since November 21, 2003
 */
public class OrderPatchMethod extends WebDAVMethod {
	
	private static Logger m_logger = Logger.getLogger(OrderPatchMethod.class.getName());

	
	public static final String METHOD_NAME = "ORDERPATCH";

	/**
	 * @param request
	 * @param response
	 * @throws WebDAVException
	 */
	public OrderPatchMethod(
		HttpServletRequest request,
		HttpServletResponse response)
		throws WebDAVException {
		super(request, response);
		methodName = METHOD_NAME;
	}

	/* (non-Javadoc)
	 * @see com.ibm.webdav.protocol.http.WebDAVMethod#execute()
	 */
	public WebDAVStatus execute() throws WebDAVException {

		try {
			// get any arguments out of the headers
			String depth = context.getRequestContext().depth();
			
			Document contents = null;
			
			if (context.getRequestContext().contentLength() > 0) {
				// get the request entity body and parse it
				WebDAVErrorHandler errorHandler =
					new WebDAVErrorHandler(resource.getURL().toString());
			
				DocumentBuilderFactory factory =
					DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
			
				DocumentBuilder docbuilder = factory.newDocumentBuilder();
				docbuilder.setErrorHandler(errorHandler);
				contents =
					docbuilder.parse(
						new org.xml.sax.InputSource(request.getReader()));
			
				if (errorHandler.getErrorCount() > 0) {
					throw new WebDAVException(
						WebDAVStatus.SC_BAD_REQUEST,
						"Syntax error in PROPFIND request entity body");
				}
			}
			
			// get the arguments for the getProperties() method, and figure
			// out which method variant to call.
			if (ResourceImpl.debug) {
				System.err.println("property request entity:");
			
				PrintWriter pout = new PrintWriter(System.err);
				pout.print(XMLUtility.printNode(contents.getDocumentElement()));
			
				//((Document) contents).printWithFormat(pout);
			}
			
			if(resource.isCollection() == true) {
				MultiStatus multiStatus = ((CollectionImpl)resource).setOrdering(context,contents);
				Enumeration responses = multiStatus.getResponses();
		
				if (responses.hasMoreElements()) {
					// there's more than one response, so return a multistatus
					context.getResponseContext().contentType("text/xml");
					setResponseHeaders();
					setStatusCode(WebDAVStatus.SC_MULTI_STATUS);

					// output the results as an XML document
					Document results = multiStatus.asXML();
					//((Document) results).setEncoding(getResponseCharset());
					if (ResourceImpl.debug) {
						System.err.println("property update results:");
						PrintWriter pout = new PrintWriter(System.err);
						pout.print(XMLUtility.printNode(results.getDocumentElement()));
										//((Document) results).printWithFormat(pout);
					}
					PrintWriter pout = new PrintWriter(response.getWriter(), false);
					//((Document) results).print(pout);
					pout.print(multiStatus.toString());
								pout.close();
				} else {
					setStatusCode(WebDAVStatus.SC_OK); // the default status code
					setResponseHeaders();
				}
			} else {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,"Invalid request on a non-collection resource");
			}
			
		
		} catch (WebDAVException exc) {
			m_logger.log(Level.INFO, exc.getLocalizedMessage());
			setStatusCode(exc.getStatusCode());
		} catch (Exception exc) {
			m_logger.log(Level.WARNING, exc.getMessage(), exc);
			setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
		}
				
		return context.getStatusCode();
	}

}
