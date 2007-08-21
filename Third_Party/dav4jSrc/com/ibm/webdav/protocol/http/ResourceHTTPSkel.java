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

import javax.servlet.*;
import javax.servlet.http.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;


/** This implementation of WebDAV uses a distributed object model. Each WebDAV method
 * correspondes to some method on a Resource object. ResourceHTTPStub and ResourceHTTPSkel are the
 * client proxy, and server listener classes that enable remote invocation of Resource
 * methods to a ResourceImpl instance over HTTP. ResourceHTTPSkel implements the server side of
 * ResourceHTTPStub. It marshals arguments (HTTP headers and entity request bodies in
 * this case), dispatches the remote method, and marshals the results (HTTP response
 * headers, response entity bodies, and statuses) back to the client.
 * <p>
 * ResourceHTTPStub is a servlet that is intended to replace the file servlet in the
 * JavaWebServer or IBM WebSphere AppServer. In conjunction with the file servlet,
 * it provides a WebDAV service for the JavaWebServer.</p>
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see ResourceHTTPStub
 * @see WebDAVMethod
 */
public class ResourceHTTPSkel extends HttpServlet {
    /**
         * Initialize global variables
         */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

    }

    /** Service all HTTP requests including WebDAV extensions. This is the servlet
     * entry point for all method dispatching for the HTTP protocol.
     *
     * @param request contains information about the client request: the method,
     *    request headers, and request entity body.
     * @param response provides a means for the server to send a response back to
     *    the client including response headers and a response entity body.
     */
    protected void service(HttpServletRequest request,
                           HttpServletResponse response)
                    throws ServletException, IOException {
        try {
            // create an instance of a WebDAV request method
            WebDAVMethod method = WebDAVMethod.create(request, response);
            response.setHeader("dav4j-server-version", Resource.DAV4JVersion);

            ResourceImpl resource = method.getResource();

            if (this.allowUser(resource) == false) {
                // Not allowed, so report he's unauthorized
                response.setHeader("WWW-Authenticate", "BASIC realm=\"OHRM\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                if (ResourceImpl.debug) {
                    System.err.println(method.getMethodName() + " " +
                                       resource.getURL().getFile());

                    Enumeration propertyNames = resource.getRequestContext()
                                                        .keys();

                    while (propertyNames.hasMoreElements()) {
                        String name = (String) propertyNames.nextElement();
                        String value = (String) resource.getRequestContext()
                                                        .get(name);
                        System.err.println(name + ": " + value);
                    }

                    System.err.println();
                }

                // dispatch the request method. Each method handles the request entity,
                // response headers, and response entity differently.
                WebDAVStatus statusCode = method.execute();

                if (ResourceImpl.debug) {
                    System.err.println("server statusCode = " + statusCode);
                }
            }
        } catch (Exception exc) {
            // all exceptions should have been caught, but just in case...
            System.err.println("ResourctHTTPSkel internal error: " + exc);
            exc.printStackTrace();
        }
    }

    protected boolean allowUser(ResourceImpl resource)
                         throws IOException {
        String user = resource.getContext().getRequestContext()
                              .getAuthorizationId();
        String pwd = resource.getContext().getRequestContext().getPassword();

        if ((user != null) && (user.length() > 0) && (pwd != null) &&
                (pwd.length() > 0)) {
            return resource.authenticateUser(user, pwd);
        } else {
            return false;
        }
    }
}