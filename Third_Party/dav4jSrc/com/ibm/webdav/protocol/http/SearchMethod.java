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

import java.io.*;
import java.util.logging.*;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;


/** 
 * Executes the WebDAV DASL Search method.
 * 
 * @author Michael Bell
 * @since November 13, 2003
 */
public class SearchMethod extends WebDAVMethod {
	private static Logger m_logger = Logger.getLogger(SearchMethod.class.getName());

	
    public static final String TAG_QUERY_SCHEMA_DISCOVERY = "query-schema-discovery";
    public static final String TAG_SEARCHREQUEST = "searchrequest";

    /** Construct a PropFindMethod.
    * @param request the servlet request
    * @param response the servlet response
    * @exception com.ibm.webdav.WebDAVException
    */
    public SearchMethod(HttpServletRequest request,
                          HttpServletResponse response)
                   throws WebDAVException {
        super(request, response);
        methodName = "SEARCH";
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

                /*Parser xmlParser = new Parser(resource.getURL().toString(), errorListener, null);
                xmlParser.setWarningNoDoctypeDecl(false);
                xmlParser.setProcessNamespace(true);
                contents = xmlParser.readStream(request.getReader());*/
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);

                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                docbuilder.setErrorHandler(errorHandler);
                contents = docbuilder.parse(
                                   new org.xml.sax.InputSource(
                                           request.getReader()));

                if (errorHandler.getErrorCount() > 0) {
                    throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
                                              "Syntax error in SEARCH request entity body");
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

            Element rootEl = (Element) ((contents == null)
                                          ? null : contents.getDocumentElement());

            if(rootEl.getNamespaceURI().equals("DAV:") == false) {
              throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,
                                              "Wrong namespace for request");
            }


            if(rootEl.getLocalName().equals(SearchMethod.TAG_QUERY_SCHEMA_DISCOVERY)) {
              Element searchEl = (Element)rootEl.getElementsByTagName("*").item(0);

              SearchRequest searchReq = SearchRequestFactory.getSearchRequest(searchEl);

              multiStatus = resource.getSearchSchema(context,searchReq);

            } else if(rootEl.getLocalName().equals(SearchMethod.TAG_SEARCHREQUEST)) {
              Element searchEl = (Element)rootEl.getElementsByTagName("*").item(0);

              SearchRequest searchReq = SearchRequestFactory.getSearchRequest(searchEl);
              try {
                multiStatus = resource.executeSearch(context,searchReq);
              } catch(WebDAVException e) {
              	String sMsg = e.getMessage();
                if(sMsg != null && sMsg.indexOf("scope") >0) {
                  setStatusCode(e.getStatusCode());
                  PrintWriter pout = new PrintWriter(response.getWriter(), false);
                  pout.print(this.getScopeErrorResponse(searchReq));
                  pout.close();
                }
                throw e;
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

            }

            PrintWriter pout = new PrintWriter(response.getWriter(), false);
            pout.print(XMLUtility.printNode(results.getDocumentElement()));
            pout.close();
        } catch (WebDAVException exc) {
        	m_logger.log(Level.INFO, exc.getMessage() + " - " + request.getQueryString());
            setStatusCode(exc.getStatusCode());
        } catch (Exception exc) {
        	m_logger.log(Level.WARNING, exc.getMessage(), exc);
            setStatusCode(WebDAVStatus.SC_INTERNAL_SERVER_ERROR);
        }

       
        return context.getStatusCode();
    }

    private String getScopeErrorResponse(SearchRequest searchReq) {
      String response = "";
      try {
            Document document = DocumentBuilderFactory.newInstance()
                                               .newDocumentBuilder()
                                               .newDocument();

            Element multiEl = document.createElementNS("DAV:","D:multistatus");
            multiEl.setAttribute("xmlns:D","DAV:");

            Element respEl = document.createElementNS("DAV:","D:response");

            Element hrefEl = document.createElementNS("DAV:","D:href");

            hrefEl.appendChild(document.createTextNode(searchReq.getScopeURI()));

            respEl.appendChild(hrefEl);

            Element statusEl = document.createElementNS("DAV:","D:status");

            Element scopeErrorEl = document.createElementNS("DAV:","D:scopeerror");

            statusEl.appendChild(document.createTextNode("HTTP/1.1 404 Object Not Found"));

            respEl.appendChild(scopeErrorEl);

            respEl.appendChild(statusEl);

            multiEl.appendChild(respEl);

            response = XMLUtility.printNode(multiEl);


        } catch (Exception e) {
        	m_logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        }

      return response;
    }
}