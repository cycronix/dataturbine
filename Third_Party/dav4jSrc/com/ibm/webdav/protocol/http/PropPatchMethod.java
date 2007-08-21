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

/** Executes the WebDAV PROPPATCH method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class PropPatchMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(PropPatchMethod.class.getName());

	
/** Construct a PropPatchMethod.
* @param request the servlet request
* @param response the servlet response
* @exception com.ibm.webdav.WebDAVException
*/
public PropPatchMethod(HttpServletRequest request, HttpServletResponse response) throws WebDAVException {
	super(request, response);
	methodName = "PROPPATCH";
        context.setMethodName(methodName);
}
/** Execute the method.
* @return the result status code
*/
public WebDAVStatus execute() {
	setStatusCode(WebDAVStatus.SC_OK); // the default status code
	MultiStatus multiStatus = null;
	try {
		// get any arguments out of the headers
		String depth = context.getRequestContext().depth();

		// get the request entity body and parse it
		WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(resource.getURL().toString());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                docbuilder.setErrorHandler(errorHandler);
                /*Parser xmlParser = new Parser(resource.getURL().toString(), errorListener, null);
		xmlParser.setWarningNoDoctypeDecl(false);
		xmlParser.setProcessNamespace(true);
		Document contents = xmlParser.readStream(request.getReader());*/
                Document contents = docbuilder.parse(new org.xml.sax.InputSource(request.getReader()));
		if (errorHandler.getErrorCount() > 0) {
                  if(true) throw new RuntimeException();
			throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Syntax error in PROPPATCH request entity body");
		}
		if (ResourceImpl.debug) {
			System.err.println("property update request entity:");
			PrintWriter pout = new PrintWriter(System.err);
			pout.print(XMLUtility.printNode(contents.getDocumentElement()));
                        //((Document) contents).printWithFormat(pout);
		}
		
        context.setMethodName(methodName);
		multiStatus = resource.setProperties(context, contents);
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
