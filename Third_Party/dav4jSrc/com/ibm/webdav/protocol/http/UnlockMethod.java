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

/** Executes the WebDAV UNLOCK method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class UnlockMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(UnlockMethod.class.getName());

	
/** Construct an UnlockMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public UnlockMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "UNLOCK";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	try {
		setStatusCode(WebDAVStatus.SC_NO_CONTENT);

		// get the lock token and unlock it
		String lockToken = context.getRequestContext().lockToken();
		context.setMethodName(methodName);
		MultiStatus multiStatus = resource.unlock(context, lockToken);
		Enumeration responses = multiStatus.getResponses();
		if (responses.hasMoreElements()) {
			// there's a response (at least the lockdiscovery), so return a multistatus
			resource.getResponseContext().contentType("text/xml");
			setResponseHeaders();
			setStatusCode(WebDAVStatus.SC_MULTI_STATUS);

			// output the results as an XML document
			Document results = multiStatus.asXML();
			//((Document) results).setEncoding(getResponseCharset());
			if (ResourceImpl.debug) {
				System.err.println("unlock results:");
				PrintWriter pout = new PrintWriter(System.err);
				pout.print(XMLUtility.printNode(results.getDocumentElement()));
                                //((Document) results).printWithFormat(pout);
			}
			PrintWriter pout = new PrintWriter(response.getWriter(), false);
			pout.print(XMLUtility.printNode(results.getDocumentElement()));
                        //((Document) results).print(pout);
			pout.close();
		} else {
			setResponseHeaders();
		}
	} catch (WebDAVException exc) {
		m_logger.log(Level.INFO, exc.getMessage() + " - " + request.getQueryString());
		setStatusCode(exc.getStatusCode());
	} catch (Exception exc) {
		m_logger.log(Level.WARNING, exc.getMessage(), exc);
		setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
	}
	return context.getStatusCode();
}
}
