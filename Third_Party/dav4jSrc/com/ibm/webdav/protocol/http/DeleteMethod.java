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

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;

/** Executes the WebDAV DELETE method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class DeleteMethod extends WebDAVMethod {
	
	private static Logger m_logger = Logger.getLogger(DeleteMethod.class.getName());

/** Construct a DeleteMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
* @exception IOException
*/
public DeleteMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "DELETE";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	setStatusCode(WebDAVStatus.SC_NO_CONTENT); // the default status code
	try {
		MultiStatus multiStatus = resource.delete(context);
		Enumeration responses = multiStatus.getResponses();
		if (responses.hasMoreElements()) {
			MethodResponse methodResponse = (MethodResponse) responses.nextElement();
			if (responses.hasMoreElements()) {
				// there's more than one response, so return a multistatus
				context.getResponseContext().contentType("text/xml");
				setStatusCode(WebDAVStatus.SC_MULTI_STATUS);
				setResponseHeaders();

				// output the multiStatus results as an XML document
				Document results = multiStatus.asXML();
				//((Document) results).setEncoding(getResponseCharset());
				if (ResourceImpl.debug) {
					System.err.println("delete results:");
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
			// there was nothing in the MultiStatus (shouldn't happen), so default
			setStatusCode(WebDAVStatus.SC_NO_CONTENT);
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
