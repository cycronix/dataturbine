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
import java.util.logging.*;

import javax.servlet.http.*;

import com.ibm.webdav.*;

/** Executes the WebDAV POST method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class PostMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(PostMethod.class.getName());

	
/** Construct a PostMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public PostMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "POST";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	try {
		if (!resource.exists()) {
			setStatusCode( WebDAVStatus.SC_NOT_FOUND );
			return context.getStatusCode();
		}

		// Submit the request entity to the requested resource and
		// allow it to process it in some resource-specific way.

		setResponseHeaders();
		setStatusCode(WebDAVStatus.SC_OK);
	} catch (Exception exc) {
		m_logger.log(Level.WARNING, exc.getMessage(), exc);
		setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
	}
	return context.getStatusCode();
}
}
