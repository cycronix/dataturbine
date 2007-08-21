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
import com.ibm.webdav.impl.*;

/** Executes the WebDAV MKCOL method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class MkcolMethod extends WebDAVMethod {
	
	private static Logger m_logger = Logger.getLogger(MkcolMethod.class.getName());

	public static final String METHOD_NAME = "MKCOL";
/** Construct a MkcolMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public MkcolMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = METHOD_NAME;
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	setStatusCode(WebDAVStatus.SC_CREATED);	// the default status code
	MultiStatus multiStatus = null;
	try {
		// get the request entity body and parse it (the contents may be empty)
		WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(resource.getURL().toString());
		/*Parser xmlParser = new Parser(resource.getURL().toString(), errorListener, null);
		xmlParser.setWarningNoDoctypeDecl(false);
		xmlParser.setProcessNamespace(true);*/
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
		docbuilder.setErrorHandler(errorHandler);
                Document contents = null;
		if (context.getRequestContext().contentLength() > 0) {
			//contents = xmlParser.readStream(request.getReader());
                        contents = docbuilder.parse(new org.xml.sax.InputSource(request.getReader()));
			if (errorHandler.getErrorCount() > 0) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Syntax error in MKCOL request entity body");
			}
		}
		multiStatus = ((CollectionImpl) resource).createCollection(context, contents);
		Enumeration stats = multiStatus.getResponses();
		if (stats.hasMoreElements()) { stats.nextElement(); }
		if (stats.hasMoreElements()) {
			// only do this if there is more than one status.  This is unlikely, and some
			//    clients, like IE5 don't support Multi-Status responses to MKCOL requests.
			context.getResponseContext().contentType("text/xml");
			setResponseHeaders();
			setStatusCode(WebDAVStatus.SC_MULTI_STATUS);
			Document results = multiStatus.asXML();
			//((Document) results).setEncoding(getResponseCharset());
			PrintWriter pout = new PrintWriter(response.getWriter(), false);
			//((Document) results).print(pout);
                        pout.print(multiStatus.toString());
			pout.close();
		} else {
			int rc = getStatusCode();
			if (rc == WebDAVStatus.SC_MULTI_STATUS) {
				stats = multiStatus.getResponses();
				MethodResponse response = (MethodResponse) stats.nextElement();
				rc = response.getStatus();
			}
			setStatusCode( rc );
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
