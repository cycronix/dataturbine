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

import com.ibm.webdav.*;

/** Executes the WebDAV PUT method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class PutMethod extends WebDAVMethod {
	
	private static Logger m_logger = Logger.getLogger(PutMethod.class.getName());

/** Construct a PutMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public PutMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "PUT";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	try {
		OutputStream os = resource.getContentsOutputStream(context);

		// attempt to write the request entity body to the resource
		int length = (int) context.getRequestContext().contentLength();
		String mimeType = context.getRequestContext().contentType();
		if (mimeType==null) {
			// (http 1.1, sec 7.2.1)
			//   the client SHOULD specify this.  If they don't
			//   we are allowed to guess.  If we can't guess based
			//   on the content or name, we are to assume...
			mimeType = "application/octet-stream";
		}
		copy(request, os, length, mimeType);
		resource.closeContentsOutputStream(context);
		setResponseHeaders();
		setStatusCode(WebDAVStatus.SC_OK);
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
