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
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;

import com.ibm.webdav.*;


/** Executes the WebDAV OPTIONS method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class OptionsMethod extends WebDAVMethod
{
	private static Logger m_logger = Logger.getLogger(OptionsMethod.class.getName());

	
/** Construct an OptionsMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public OptionsMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "OPTIONS";
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	try {
		context.getResponseContext().put("Allow", getCommaSeparatedString(resource.getAllowedMethods()));
		context.getResponseContext().DAV("1, 2, binding"); // level 2 compliant, see section 15

		setResponseHeaders();
		setStatusCode(WebDAVStatus.SC_OK);

                /*PrintWriter out = response.getWriter();

                out.print("<D:multistatus xmlns:D=\"DAV:\"><D:response></D:response></D:multistatus>");
	        out.close();*/
        } catch (Exception exc) {
        m_logger.log(Level.WARNING, exc.getMessage(), exc);
		setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
	}
	return context.getStatusCode();
}

/**
 * Returns a comma separated list of values taken from the given <code>List</code> as a <code>String</code>
 * 
 * @param allowedMethods
 * @return
 */
private String getCommaSeparatedString(List allowedMethods) {
	Iterator iter = allowedMethods.iterator();
	StringBuffer strbuf = new StringBuffer();
	while(iter.hasNext()) {
		strbuf.append(iter.next());
		if(iter.hasNext()) {
			strbuf.append(",");
		}
	}
	
	return strbuf.toString();
}
}
