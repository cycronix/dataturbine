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


/** Executes the WebDAV PROPFIND method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class PropFindMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(PropFindMethod.class.getName());

    /** Construct a PropFindMethod.
    * @param request the servlet request
    * @param response the servlet response
    * @exception com.ibm.webdav.WebDAVException
    */
    public PropFindMethod(HttpServletRequest request,
                          HttpServletResponse response)
                   throws WebDAVException {
        super(request, response);
        methodName = "PROPFIND";
    }

    /** Execute the method.
    * @return the result status code
    */
    public WebDAVStatus execute() {
        MultiStatus multiStatus = null;

        try {
            // get any arguments out of the headers
            String depth = context.getRequestContext().depth();

            Document contents = null;

            if (context.getRequestContext().contentLength() > 0) {
                // get the request entity body and parse it
                WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(
                                                          resource.getURL()
                                                                  .toString());


                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);

                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                docbuilder.setErrorHandler(errorHandler);
                contents = docbuilder.parse(
                                   new org.xml.sax.InputSource(
                                           request.getReader()));

                if (errorHandler.getErrorCount() > 0) {
                    throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
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

            Element propfind = (Element) ((contents == null)
                                          ? null : contents.getDocumentElement());


            if ((contents == null) || (propfind == null) ||
                    (propfind.getElementsByTagNameNS("DAV:", "allprop")
                             .getLength() > 0)) {
                

                if (propfind != null) {
                    Element allpropEl = (Element) propfind.getElementsByTagNameNS(
                                                          "DAV:", "allprop")
                                                          .item(0);
                }

                if (resource.exists() && resource.isCollection()) {
                    multiStatus = ((CollectionImpl) resource).getProperties(
                                          context, depth);
                } else {
                    multiStatus = resource.getProperties(context);
                }
            } else if (propfind.getElementsByTagNameNS("DAV:", "propname")
                             .getLength() > 0) {
                if (resource.exists() && resource.isCollection()) {
                    multiStatus = ((CollectionImpl) resource).getPropertyNames(
                                          context, depth);
                } else {
                    multiStatus = resource.getPropertyNames(context);
                }
            } else {
                Vector tempNames = new Vector();
                Node prop = propfind.getElementsByTagNameNS("DAV:", "prop")
                                    .item(0);
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

                if (resource.exists() && resource.isCollection()) {
                    multiStatus = ((CollectionImpl) resource).getProperties(
                                          context, names, depth);
                } else {
                    multiStatus = resource.getProperties(context, names);
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

            //PrintWriter pout = new PrintWriter(response.getWriter(), false);
            OutputStream os = response.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(XMLUtility.printNode(results.getDocumentElement()));
            //pout.print(XMLUtility.printNode(results.getDocumentElement()));


            //((Document) results).print(pout);
            //pout.print(multiStatus.toString());
            
            //pout.close();
            osw.close();
            //os.close();
        } catch (WebDAVException exc) {
        	if(exc.getStatusCode() == 500) {
        		m_logger.log(Level.WARNING, exc.getLocalizedMessage(), exc);
        	} else {
        		m_logger.log(Level.INFO, exc.getLocalizedMessage() + " - " + request.getRequestURI());
        	}
        	
        	setStatusCode(exc.getStatusCode());
        } catch (Exception exc) {
        	m_logger.log(Level.WARNING, exc.getMessage(), exc);
            setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return context.getStatusCode();
    }
}