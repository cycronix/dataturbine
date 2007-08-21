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

/** Executes the WebDAV HEAD method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class HeadMethod extends WebDAVMethod
{
	
	private static Logger m_logger = Logger.getLogger(HeadMethod.class.getName());

/** Construct a HeadMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public HeadMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "HEAD";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	try {
		resource.getMetaInformation(context);
		// fill in any response headers here

		// set the response headers and status code. This must
		// be done before a byte is written to the output stream
		// or the headers and status code will be lost
		setStatusCode(WebDAVStatus.SC_OK);
		setResponseHeaders();
	} catch (WebDAVException exc) {
		setStatusCode(exc.getStatusCode());
	} catch (Exception exc) {
		m_logger.log(Level.WARNING, exc.getMessage(), exc);
		setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
	}
	return context.getStatusCode();
}
}
