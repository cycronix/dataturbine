/*
 * (C) Copyright SimulacraMedia 2003.  All rights reserved.
 * 
 * Created on Nov 13, 2003
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
 * Executes the WebDAV Delta-V Report method.
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *
 */
public class ReportMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(ReportMethod.class.getName());


	public static final String METHOD_NAME = "REPORT";

	/**
	 * @param request
	 * @param response
	 * @throws WebDAVException
	 */
	public ReportMethod(
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
		MultiStatus multiStatus = null;

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

			Element versionTree =
				(Element) ((contents == null)
					? null
					: contents.getDocumentElement());

			if(versionTree == null || versionTree.getElementsByTagNameNS("DAV:","version-tree").getLength() == 0) {
				 throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,"Invalid request body");
			} else if (versionTree.getElementsByTagNameNS("DAV:", "allprop").getLength()
					> 0) {


					multiStatus = resource.getVersionTreeReport(context);

			} else {
				Vector tempNames = new Vector();
				Node prop =
					versionTree.getElementsByTagNameNS("DAV:", "prop").item(0);
				NodeList propnames = prop.getChildNodes();
				String sss = "bleh";
				Element propname = null;

				for (int i = 0; i < propnames.getLength(); i++) {
					//propname = (Element) propnames.item(i);
					Object objj = (Object) propnames.item(i);

					if (objj instanceof Text) {
						// skip.  Probably just a CRLF or something
						Text tt = (Text) objj;
						sss = "x" + tt.getNodeValue() + "y";
					} else {
						propname = (Element) objj;

						Element tx = (Element) propname;

						//tempNames.addElement(propname.getTagName());
						tempNames.addElement(new PropertyName(tx));
					}
				}

				PropertyName[] names = new PropertyName[tempNames.size()];
				Enumeration nameEnumerator = tempNames.elements();
				int i = 0;

				while (nameEnumerator.hasMoreElements()) {
					names[i] = (PropertyName) nameEnumerator.nextElement();
					i++;
				}

				if (resource.exists() == true) {
					multiStatus = resource.getVersionTreeReport(context, names);
				}
			}

			context.getResponseContext().contentType("text/xml");
			setResponseHeaders();
			setStatusCode(WebDAVStatus.SC_MULTI_STATUS);

			// output the results as an XML document
			Document results = multiStatus.asXML();

			// set the character encoding for the document to that specified by
			// the client in the Accept header
			//((Document) results).setEncoding(getResponseCharset());
			if (ResourceImpl.debug) {
				System.err.println("property results:");

				PrintWriter pout = new PrintWriter(System.err);
				pout.print(XMLUtility.printNode(results.getDocumentElement()));

				//((Document) results).printWithFormat(pout);
			}

			PrintWriter pout = new PrintWriter(response.getWriter(), false);
			pout.print(XMLUtility.printNode(results.getDocumentElement()));

			//((Document) results).print(pout);
			//pout.print(multiStatus.toString());
			pout.close();
		} catch (WebDAVException exc) {
			setStatusCode(exc.getStatusCode());
		} catch (Exception exc) {
			m_logger.log(Level.WARNING, exc.getMessage(), exc);
			setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
		}

		return context.getStatusCode();
	}

}
