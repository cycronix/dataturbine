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
import java.net.*;
import java.rmi.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;


/** 
 * WebDAVMethod is the abstract superclass of classes corresponding to the
 * WebDAV methods using the command pattern. It maintains the state and logic
 * that is common to the execution of all webDAV methods. These subclasses
 * are a good place to see how the WebDAV protocol is mapped back onto the
 * DAV4J API.
 * <p>
 * WebDAVMethods are constructed by the HTTP server skeleton ResourceHTTPSkel
 * in order to dispatch Resource methods to ResourceImpl methods on the server.
 * This treats HTTP just like any other remote procedure call mechanism, and
 * unifies the client/server communication.</p>
 * <p>
 * In general, the execution of a WebDAV method consists of:
 * <ol>
 *   <li>create an instance of a Resource implementation corresponding to the resource being
 *       manipulated by the method</li>
 *   <li>set the ResourceImpl request context from the request headers using getRequestHeaders()</li>
 *   <li>get the request entity (if any), parse it, and marshal any arguments
 *       it contains for the method</li>
 *   <li>call the ResoureImpl method corresponding to the WebDAVMethod subclass (WebDAV or HTTP method)</li>
 *   <li>put any response context into the result headers using setResponseHeaders()</li>
 *   <li>output the response if any</li>
 *   <li>catch exceptions and translate them to set the status code and message
 *       for method</li>
 * </ol>
 * <p>
 * Some of these operations are generic to all methods while others are specific
 * to each subclass. In particular, how the context values,
 * request entity, generation of the result entity, and status codes are handled
 * are subclass specific.
 * </p>
 * <p>Note it is critical that the execute method sets status codes and the request
 * and response headers at the right time. The order is, (1) use setStatusCode() and set
 * other response headers the method needs to set. (2) Call setResponseHeaders() to
 * copy the resource response context to the response headers. (3) Output any response entity
 * body. The headers and response code are written the the HTTP response output
 * stream just before the first byte of the response entity body. Any
 * headers or status code set after the first byte of the response entity body has
 * been written will be lost.</p>
 */
public abstract class WebDAVMethod extends Object {
    protected ResourceImpl resource; // t10he resource the method is operating on
    protected HttpServletRequest request; // the request from the client
    protected HttpServletResponse response; // the response from the server

    // contexts for communicating HTTP and WebDAV headers (method contol couples)
    protected ResourceContext context = new ResourceContext();
    protected String methodName = null;
    private static final Logger logger = Logger.getLogger(WebDAVMethod.class.getName());

    /** Construct a WebDAVMethod.
    * @param request the servlet request
    * @param response the servlet response
    * @exception com.ibm.webdav.WebDAVException
    */
    public WebDAVMethod(HttpServletRequest request,
                        HttpServletResponse response) throws WebDAVException {
        this.request = request;
        this.response = response;

        // this isn't available from the HttpServletRequest	directly, but should be
        // the javax.servlet.http.HttpUtils.getRequestURL() doesn't strip off the query string,
        // and looses the default port if it was specified.
        URL requestURL = getRequestURL();

        resource = new ResourceImpl(requestURL, request.getPathTranslated());

        if ((resource.exists() && resource.isCollection()) ||
                request.getMethod().equals("MKCOL")) {
            resource = new CollectionImpl(requestURL,
                                          request.getPathTranslated(), null);
        }
    }

    /**
     * Copies input stream to an HTTP output stream.
     * @param in the source stream from the NamespaceManager
     * @param out the destination stream from the servlet response
     * @param length the number of bytes to read from the steam
     * @param mime the MIME type of the document to determine if its text or binary
     */
    protected void copy(InputStream in, HttpServletResponse response,
                        int length, String mime) throws WebDAVException {
        try {
            int totalRead = 0;
            int numRead = 0;
            int numToRead = 0;
            boolean isText = mime.regionMatches(0, "text", 0, 4);

            if (!isText) { // copy binary

                ServletOutputStream out = (ServletOutputStream) response.getOutputStream();
                byte[] buf = new byte[4096];

                while (totalRead < length) {
                    if ((length - totalRead) < buf.length) {
                        numToRead = length - totalRead;
                    } else {
                        numToRead = buf.length;
                    }

                    numRead = in.read(buf, 0, numToRead);

                    if (numRead == -1) {
                        break;
                    }

                    totalRead += numRead;
                    out.write(buf, 0, numRead);
                }
            } else { // copy text using the client's preferred encoding scheme

                Reader isr = new InputStreamReader(in,
                                                   Resource.defaultCharEncoding);

                ServletOutputStream out = (ServletOutputStream) response.getOutputStream();


                // Can't use response.getWriter() because JWS MIME filtering won't work
                // It gives an IllegalStateException because it thinks you already got the
                // output stream. This could be related to the AppServer bug that the response
                // headers appear to be already written. Probably caused by a clone of the
                // response for the filter.
                //Writer out = response.getWriter();
                /*PrintWriter writer = new PrintWriter(
                                             new BufferedWriter(
                                                     new OutputStreamWriter(out,
                                                                            response.getCharacterEncoding()),
                                                     1024), false);*/
                PrintWriter writer = new PrintWriter(
                                             new BufferedWriter(
                                                     new OutputStreamWriter(out,
                                                                            Resource.defaultCharEncoding),
                                                     1024), false);
                numToRead = 4096;

                char[] buf = new char[numToRead];

                while ((numRead = isr.read(buf, 0, numToRead)) != -1) {
                    writer.write(buf, 0, numRead);
                }

                writer.flush();
            }
        } catch (WebDAVException exc) {
            throw exc;
        } catch (java.io.IOException exc) {
            throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
                                      "IO Error");
        }
    }

    /**
     * Copies an HTTP input stream to an output stream.
     * @param in the source stream from the servlet request
     * @param out the destination stream from the NamespaceManager
     * @param length the number of bytes to read from the steam
     * @param mime the MIME type of the document to determine if its text or binary
     */
    protected void copy(HttpServletRequest request, OutputStream out,
                        int length, String mime) throws WebDAVException {
        try {
            int totalRead = 0;
            int numRead = 0;
            int numToRead = 0;
            boolean isText = (length > 0)
                             ? mime.regionMatches(0, "text", 0, 4) : false;

            if (!isText) { // copy binary

                ServletInputStream in = (ServletInputStream) request.getInputStream();
                byte[] buf = new byte[4096];

                while (totalRead < length) {
                    if ((length - totalRead) < buf.length) {
                        numToRead = length - totalRead;
                    } else {
                        numToRead = buf.length;
                    }

                    numRead = in.read(buf, 0, numToRead);

                    if (numRead == -1) {
                        break;
                    }

                    totalRead += numRead;
                    out.write(buf, 0, numRead);
                }
            } else { // copy text server's encoding scheme

                Reader isr = request.getReader();
                PrintWriter osw = new PrintWriter(
                                          new OutputStreamWriter(out,
                                                                 Resource.defaultCharEncoding),
                                          false);
                char[] buf = new char[4096];

                while (totalRead < length) {
                    if ((length - totalRead) < buf.length) {
                        numToRead = length - totalRead;
                    } else {
                        numToRead = buf.length;
                    }

                    numRead = isr.read(buf, 0, numToRead);

                    if (numRead == -1) {
                        break;
                    }

                    totalRead += numRead;
                    osw.write(buf, 0, numRead);
                }

                osw.flush();
            }
        } catch (WebDAVException exc) {
            throw exc;
        } catch (java.io.IOException exc) {
            throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
                                      "IO Error");
        }
    }

    /** Create a WebDAVMethod corresponding to the WebDAV or HTTP method in
    * the request.
    * @param request the servlet request
    * @param response the servlet response
    * @return a subclass of WebDAVMethod corresponding to the request method
    * @exception com.ibm.webdav.WebDAVException
    */
    public static WebDAVMethod create(HttpServletRequest request,
                                      HttpServletResponse response)
                               throws WebDAVException {
        String methodName = request.getMethod();
        WebDAVMethod method = null;
        
        if(logger.isLoggable(Level.FINE)) {
        	logger.logp(Level.FINE, WebDAVMethod.class.getName(), "create", "Processing " + methodName + " request for " + request.getRequestURI());
        }
        
        // create an instance of a ResourceImpl object that will eventually
        // field the request. To support subclasses of Resource, create a
        // a subclass of ResourceHTTPSkel and create an instance of the new
        // impl subclass here.
        if (methodName.equals("GET")) {
            method = new GetMethod(request, response);
        } else if (methodName.equals("HEAD")) {
            method = new HeadMethod(request, response);
        } else if (methodName.equals("PUT")) {
            method = new PutMethod(request, response);
        } else if (methodName.equals("DELETE")) {
            method = new DeleteMethod(request, response);
        } else if (methodName.equals("COPY")) {
            method = new CopyMethod(request, response);
        } else if (methodName.equals("MOVE")) {
            method = new MoveMethod(request, response);
        } else if (methodName.equals("PROPFIND")) {
            method = new PropFindMethod(request, response);
        } else if (methodName.equals("PROPPATCH")) {
            method = new PropPatchMethod(request, response);
        } else if (methodName.equals("MKCOL")) {
            method = new MkcolMethod(request, response);
        } else if (methodName.equals("LOCK")) {
            method = new LockMethod(request, response);
        } else if (methodName.equals("UNLOCK")) {
            method = new UnlockMethod(request, response);
        } else if (methodName.equals("OPTIONS")) {
            method = new OptionsMethod(request, response);
        } else if (methodName.equals("POST")) {
            method = new PostMethod(request, response);
        } else if (methodName.equals("SEARCH")) {
            method = new SearchMethod(request, response);
        } else if (methodName.equals("BIND")) {
            method = new BindMethod(request, response);
        } else if (methodName.equals(CheckInMethod.METHOD_NAME)) {
			method = new CheckInMethod(request, response);
		} else if (methodName.equals(CheckOutMethod.METHOD_NAME)) {
			method = new CheckOutMethod(request, response);
		} else if (methodName.equals(ReportMethod.METHOD_NAME)) {
			method = new ReportMethod(request, response);
		} else if (methodName.equals(VersionControlMethod.METHOD_NAME)) {
			method = new VersionControlMethod(request, response);
		} else if (methodName.equals(UncheckOutMethod.METHOD_NAME)) {
			method = new UncheckOutMethod(request, response);
		} else if (methodName.equals(OrderPatchMethod.METHOD_NAME)) {
			method = new OrderPatchMethod(request, response);
		} 

        method.getRequestHeaders();

        return method;
    }

    /** Execute this method. Subclasses are expected to override this method to
    * handle the request entity, do the work, update the context, return
    * the response entity result if any, and set the status code.
    *
    * @return the WebDAV status code.
    * @exception com.ibm.webdav.WebDAVException
    */
    public abstract WebDAVStatus execute() throws WebDAVException;

    /** Get the request method name
    *
    */
    public String getMethodName() {
        return methodName;
    }

    /** Initialize the request context from the request headers. This provides
    * additional parameters for the methods and perhaps other state from the
    * client for the resource.
    * @exception com.ibm.webdav.WebDAVException
    */
    public void getRequestHeaders() throws WebDAVException {
        // set the Resource request context from the request headers
        context = getResource().getContext();

        Enumeration headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = ((String) headerNames.nextElement()).toLowerCase();
            String value = request.getHeader(name);
            context.getRequestContext().put(name, value);
        }
    }

    /**
    * Reconstructs the URL used by the client used to make the
    * request.  This accounts for differences such as addressing
    * scheme (http, https), but does not attempt to
    * include query parameters. The port is retained even if the
    * default port was used.
    *
    * <P> This method is useful for creating redirect messages and for
    * reporting errors.
    */
    public URL getRequestURL() {
        StringBuffer url = new StringBuffer();

        String scheme = request.getScheme();
        int port = request.getServerPort();
        String requestURI = request.getRequestURI();
        int q = requestURI.indexOf('?');

        if (q >= 0) {
            requestURI = requestURI.substring(0, q); // supposed to be done by getRequestURI()!
        }

        url.append(scheme); // http, https
        url.append("://");


        // note, this is the IP address, not the server name. We have to be sure we normalize
        // all host names to the IP address to be sure we can compare them in Preconditions
        url.append(request.getServerName());

        if (port != -1) {
            url.append(':');
            url.append(request.getServerPort());
        }

        url.append(requestURI);

        URL result = null;

        try {
            result = new URL(url.toString());
        } catch (Exception exc) {
        }

        return result;
    }

    /** Return the Resource the method operates on.
    */
    public ResourceImpl getResource() {
        return resource;
    }

    /** Get the client prefered character encoding to be used to encode
     * text responses. This implementation gets the charset from the Accept
     * header. TODO: it should probably do something with Accept-Charset too.
     *
     * @return the MIME charset
     */
    public String getResponseCharset() {
        String accept = null;
        String charEncoding = null;

        try {
            accept = resource.getRequestContext().accept();
        } catch (Exception exc) {
        }

        if (accept != null) {
            charEncoding = ((ServletResponse) response).getCharacterEncoding();

            /*if (charEncoding != null) {
            charEncoding = MIME2Java.reverse(charEncoding);
            }*/
        }

        if (charEncoding == null) {
            charEncoding = Resource.defaultXMLEncoding;
        }

        return charEncoding;
    }

    /** Get the status code for this method.
    *
    * @return a status code as defined by HTTP/1.1 and WebDAV
    */
    public int getStatusCode() {
        return context.getStatusCode().getStatusCode();
    }

    /** Set the response headers from the response context. This must be called
    * after the remote method has been executed, and BEFORE any response entity
    * is written.
    * @exception RemoteException
    */
    public void setResponseHeaders() throws WebDAVException {
        // set the response headers from the response context
        ResourceContext context = resource.getContext();

        // let the web server calculate the content length header
        Enumeration propertyNames = context.getResponseContext().keys();

        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            String value = (String) context.getResponseContext().get(name);

            if (!name.equals("content-length")) {
                if (name.equals("content-type")) {
                    // must set Content-Type with this method or
                    // the response state will not be correct
                    response.setContentType(value);
                } else {
                    response.setHeader(name, value);
                }
            }
        }
    }

    /** Set the status code for the method. This method must be called before
    * any of the response entity is written.
    *
    * @param statusCode the status code to set as defined by HTTP/1.1
    * and WebDAV.
    *
    */
    public void setStatusCode(int statusCode) {
        context.getStatusCode().setStatusCode(statusCode);


        // set the returned status code and message
        // TODO: bug in jsdk2.1, can't set the status message, this method was
        // deprecated. The javaDoc also ways it sents an HTML response body,
        // but I think the comment is just wrong.
        response.setStatus(statusCode,
                           WebDAVStatus.getStatusMessage(statusCode));
    }
}