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

import java.util.logging.*;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.webdav.WebDAVException;
import com.ibm.webdav.WebDAVStatus;

/**
 * Executes the WebDAV Delta-V CheckOut method.
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 * @since November 13, 2003
 */
public class CheckOutMethod extends WebDAVMethod {

	public static final String METHOD_NAME = "CHECKOUT"; 
	
	private static Logger m_logger = Logger.getLogger(CheckOutMethod.class.getName());


	/**
	 * @param request
	 * @param response
	 * @throws WebDAVException
	 */
	public CheckOutMethod(HttpServletRequest request, HttpServletResponse response)
		throws WebDAVException {
		super(request, response);
		methodName = METHOD_NAME;
	}

	/* (non-Javadoc)
	 * @see com.ibm.webdav.protocol.http.WebDAVMethod#execute()
	 */
	public WebDAVStatus execute() throws WebDAVException {
		setStatusCode(WebDAVStatus.SC_CREATED); // the default status code
		try {

			context.setMethodName(METHOD_NAME);
			resource.checkout();

			setResponseHeaders();

		} catch (WebDAVException exc) {
			m_logger.log(Level.INFO, exc.getLocalizedMessage() + " - " + request.getQueryString());
			setStatusCode(exc.getStatusCode());
			
		} catch (Exception exc) {
			m_logger.log(Level.WARNING, "Check out Method: unhandled exception: ", exc);
			setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
		}
		return context.getStatusCode();
	}

}
