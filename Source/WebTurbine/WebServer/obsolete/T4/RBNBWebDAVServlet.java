/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package org.apache.catalina.rbnb;

// A cruel lie, necessitated by the ridiculous class-loader setup used by 
//   catalina, which prevents us from otherwise extending their WebdavServlet.

//package com.rbnb.web;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

// Added for content length hack, 12/06/2002:
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;


// Added for namespace hack, 10/10/2002:
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;


// Added for GET, POST parameter hack, 03/10/2003:
import java.util.Map; 


import org.w3c.dom.Node;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/*import javax.servlet.RequestDispatcher; 
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
*/
import javax.xml.parsers.DocumentBuilder;
/*
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context; */
import javax.naming.NamingEnumeration;  
import javax.naming.NameClassPair;  /*
import javax.naming.directory.DirContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.apache.naming.resources.Resource; */
import org.apache.naming.resources.ResourceAttributes;  /*
import org.apache.catalina.util.MD5Encoder;
import org.apache.catalina.util.StringManager; */
import org.apache.catalina.util.XMLWriter;
//import org.apache.catalina.util.DOMWriter;
//import org.apache.catalina.util.RequestUtil;

import org.apache.catalina.Globals;


//
// 03/19/2003  WHF  Updated for jakarta-tomcat-4.1.21.
// 2004/02/06  WHF  Added check for zero-content length in PROPFIND requests.
// 2004/02/10  WHF  Added ServletContext property to those provided to the 
//      dir context.
//
public class RBNBWebDAVServlet 
	extends org.apache.catalina.servlets.WebdavServlet
{
	
    /**
     * Get resources. This method will try to retrieve the resources through
     * JNDI first, then in the servlet context if JNDI has failed (it could be
     * disabled). It will return null.
     *
     * @return A JNDI DirContext, or null.
     */
    protected DirContext getResources() 
	{
		// Set up the environment for creating the initial context
		Hashtable env = new Hashtable(11);

		env.put(Context.INITIAL_CONTEXT_FACTORY, 
			"com.rbnb.web.RBNBDirFactory");
	    env.put("debug", (new Boolean(debug>0)).toString());
		env.put("ServletContext", getServletContext());
		try {
			if (rbnbDirContext==null)
			// Create the initial context
				rbnbDirContext= (DirContext) new InitialDirContext(env);
			return rbnbDirContext;
		} catch (NamingException ne) 
		{ 
			ne.printStackTrace(); 
			return null; 
		}
	}
	
	// Added 01/22/2003  WHF  To overcome dropout of RBNB mime-type.
	// 03/10/2003  WHF  Added parameter map info, to allow standard GET and
	//	POST parameters to propagate into the JNDI layer.
	protected class RBNBResourceInfo extends ResourceInfo
	{
		public String mimeType;
		
		private final Map parameterMap;
		
		public RBNBResourceInfo(String path, DirContext resources, 
			Map parameterMap)
		{
			super(path,resources); // does nada, see set() override below.
			this.parameterMap=parameterMap;
			set(path, resources);
		}
		
		public void recycle()
		{
			super.recycle();
			mimeType=null;
		}
		
		public void set(String path, DirContext resources)
		{
			// Extreme kluge: When called by the ResourceInfo constructor, 
			//  parameterMap will be null.  We will then set parameterMap,
			//  and retry.
			if (parameterMap==null) return;
			try {
			resources.addToEnvironment("parameterMap", parameterMap);
			} catch (NamingException ne) { /* never */ }
			super.set(path, resources);
			if (attributes!=null && attributes instanceof ResourceAttributes) {
				ResourceAttributes tempAttrs =
					(ResourceAttributes) attributes;
				mimeType=tempAttrs.getResourceType();
			}
		}
	} // end RBNBResourceInfo
	

	/**
      * @throws NamingException if the server could not be reached for
	  *  questioning.
	  * @throws NullPointerException If the environment was not configured.
	  */
	private String getRBNBServerName(DirContext resources)
		throws NamingException
	{
		Hashtable resEnv=resources.getEnvironment();
		return resEnv.get("serverName").toString();
	}

// Added to fix content length problem, 12/06/2002:
    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content Should the content be included?
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
	 // Mods:
	 // 03/10/2003  WHF  Added support for GET, POST parameters.
    protected void serveResource(HttpServletRequest request,
                                 HttpServletResponse response,
                                 boolean content)
        throws IOException, ServletException {
if (debug>0) System.err.println("RBNBWebDAVServlet::serveResource()");
        // Identify the requested resource path
        String path = getRelativePath(request);
         // Retrieve the Catalina context and Resources implementation
        DirContext resources = getResources();

/* Now defunct RBNB/ to RBNB/currentServer redirect:
		try {
		if ("/".equals(path) && !"/rbnbUser".equals(request.getContextPath())) {
			String serverName=getRBNBServerName(resources);
			// Redirect to that RBNB server which is associated with this
			//  web server:
			response.sendRedirect(request.getRequestURI()+serverName+'/');
			return;
			// didn't work, try regular processing
		} 	
		} catch (NamingException ne) { ne.printStackTrace(); }
		catch (NullPointerException e) { e.printStackTrace(); } 
*/
        RBNBResourceInfo resourceInfo = 
			new RBNBResourceInfo(path, resources, request.getParameterMap());

        if (!resourceInfo.exists) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }

        // If the resource is not a collection, and the resource path
        // ends with "/" or "\", return NOT FOUND
        if (!resourceInfo.collection) {
            if (path.endsWith("/") || (path.endsWith("\\"))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
                return;
            }
        }

        // If the resource is a collection (aka a directory), we check
        // the welcome files list.
        if (resourceInfo.collection) {

            if (!request.getRequestURI().endsWith("/")) {
                String redirectPath = request.getRequestURI() + "/";
                redirectPath = appendParameters(request, redirectPath);
                response.sendRedirect(redirectPath);
                return;
            }

            ResourceInfo welcomeFileInfo = checkWelcomeFiles(path, resources);
            if (welcomeFileInfo != null) {
                String redirectPath = welcomeFileInfo.path;
                String contextPath = request.getContextPath();
                if ((contextPath != null) && (!contextPath.equals("/"))) {
                    redirectPath = contextPath + redirectPath;
                }
                redirectPath = appendParameters(request, redirectPath);
                response.sendRedirect(redirectPath);
                return;
            }

        } /*else {

            // Checking If headers
            boolean included =
                (request.getAttribute(Globals.CONTEXT_PATH_ATTR) != null);
            if (!included 
                && !checkIfHeaders(request, response, resourceInfo)) {
                return;
            }

        }
*/
        // Find content type.
        String contentType = resourceInfo.mimeType==null?
            getServletContext().getMimeType(resourceInfo.path)
			:resourceInfo.mimeType;

        Vector ranges = null;

        if (resourceInfo.collection) {

            // Skip directory listings if we have been configured to
            // suppress them
            if (!listings) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                   resourceInfo.path);
                return;
            }
            contentType = "text/html;charset=UTF-8";

        } else {

            // Parse range specifier

            ranges = parseRange(request, response, resourceInfo);

            // ETag header
            response.setHeader("ETag", 
				// jak4.0.1: getETag(resourceInfo, true));
				getETag(resourceInfo));

            // Last-Modified header
            response.setDateHeader("Last-Modified", resourceInfo.date);

        }

        ServletOutputStream ostream = null;
        PrintWriter writer = null;

        if (content) {

            // Trying to retrieve the servlet output stream

            try {
                ostream = response.getOutputStream();
            } catch (IllegalStateException e) {
                // If it fails, we try to get a Writer instead if we're
                // trying to serve a text file
                if ( (contentType != null)
                     && (contentType.startsWith("text")) ) {
                    writer = response.getWriter();
                } else {
                    throw e;
                }
			}
        }

        if ( (resourceInfo.collection) ||
             ( ((ranges == null) || (ranges.isEmpty()))
               && (request.getHeader("Range") == null) ) ) {

            // Set the appropriate output headers
            if (contentType != null) {
                if (debug > 0)
                    log("DefaultServlet.serveFile:  contentType='" +
                        contentType + "'");
                response.setContentType(contentType);
            }
/*            long contentLength = resourceInfo.length;
            if ((!resourceInfo.collection) && (contentLength >= 0)) {
                if (debug > 0)
                    log("DefaultServlet.serveFile:  contentLength=" +
                        contentLength);
                response.setContentLength((int) contentLength);
            }
*/
            if (resourceInfo.collection) {

                if (content) {
                    // Serve the directory browser
                    resourceInfo.setStream
                        (render(request.getContextPath(), resourceInfo));
                }

            }

            // Copy the input stream to our output stream (if requested)
            if (content) {
                try {
                    response.setBufferSize(output);
                } catch (IllegalStateException e) {
                    // Silent catch
                }
                if (ostream != null) {
                    //copy(resourceInfo, ostream);
					InputStream is=resourceInfo.getStream();
					int red=is.read();
					if (red==-1) // EOF
						response.setContentLength(0);
					else 
					{
						int sz=is.available();
						response.setContentLength(sz+1);
						byte[] b=new byte[sz+1];
						b[0]=(byte) red;
						is.read(b,1,sz);
						ostream.write(b);
					}
/*					if (is instanceof com.rbnb.web.RBNBInputStream)
					{
						byte[] arr=((com.rbnb.web.RBNBInputStream) is)
							.getByteArray();
						response.setContentLength(arr.length);
						ostream.write(arr);
					}
					else //copy(resourceInfo, ostream);
System.err.println("!*! GET (not RIS) NOT SUPPORTED !*!"); */
                } else {
//                    copy(resourceInfo, writer);
//System.err.println("!*! GET (writer) NOT SUPPORTED !*!");
					if (writer != null) {
						InputStream is=resourceInfo.getStream();
						int red=is.read();
						if (red==-1) // EOF
							response.setContentLength(0);
						else 
						{
							int sz=is.available();
							response.setContentLength(sz+1);
							byte[] b=new byte[sz+1];
							b[0]=(byte) red;
							is.read(b,1,sz);
							writer.write(new String(b, "UTF-8"));
							//ostream.write(b);
						}
					}						
                }
            }

        } else {

            if ((ranges == null) || (ranges.isEmpty()))
                return;

            // Partial content response.
System.err.println("!*! GET (partial) NOT SUPPORTED !*!");
/*
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            if (ranges.size() == 1) {

                Range range = (Range) ranges.elementAt(0);
                response.addHeader("Content-Range", "bytes "
                                   + range.start
                                   + "-" + range.end + "/"
                                   + range.length);
                response.setContentLength((int) (range.end - range.start + 1));

                if (contentType != null) {
                    if (debug > 0)
                        log("DefaultServlet.serveFile:  contentType='" +
                            contentType + "'");
                    response.setContentType(contentType);
                }

                if (content) {
                    try {
                        response.setBufferSize(output);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (ostream != null) {
                        copy(resourceInfo, ostream, range);
                    } else {
                        copy(resourceInfo, writer, range);
                    }
                }

            } else {

                response.setContentType("multipart/byteranges; boundary="
                                        + mimeSeparation);

                if (content) {
                    try {
                        response.setBufferSize(output);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (ostream != null) {
                        copy(resourceInfo, ostream, ranges.elements(),
                             contentType);
                    } else {
                        copy(resourceInfo, writer, ranges.elements(),
                             contentType);
                    }
                }

            }
*/
        }

    }

	
// Added for namespace hack, 10/10/2002:
	
    /**
     * Generate the namespace declarations.
     */
    private String generateNamespaceDeclarations() {
        return " xmlns:"+DN_ALIAS+"=\"" + DEFAULT_NAMESPACE + "\"";
    }
    /**
     * Default namespace.
     */
    protected static final String DEFAULT_NAMESPACE = "DAV:";
	
    /**
     * Default namespace alias.
     */
    protected static final String DN_ALIAS = "D";

    /**
     * Default depth is infite.
     */
    private static final int INFINITY = 3; // To limit tree browsing a bit


    /**
     * PROPFIND - Specify a property mask.
     */
    private static final int FIND_BY_PROPERTY = 0;


    /**
     * PROPFIND - Display all properties.
     */
    private static final int FIND_ALL_PROP = 1;


    /**
     * PROPFIND - Return property names.
     */
    private static final int FIND_PROPERTY_NAMES = 2;

		
    /**
     * Repository of the lock-null resources.
     * <p>
     * Key : path of the collection containing the lock-null resource<br>
     * Value : Vector of lock-null resource which are members of the
     * collection. Each element of the Vector is the path associated with
     * the lock-null resource.
     */
    private Hashtable lockNullResources = new Hashtable();
	
    /**
     * PROPFIND Method.
     */
    protected void doPropfind(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if (!listings) {
            resp.sendError(WebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String path = getRelativePath(req);
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        if ((path.toUpperCase().startsWith("/WEB-INF")) ||
            (path.toUpperCase().startsWith("/META-INF"))) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        // Properties which are to be displayed.
        Vector properties = null;
        // Propfind depth
        int depth = INFINITY;
        // Propfind type
        int type = FIND_ALL_PROP;

        String depthStr = req.getHeader("Depth");

        if (depthStr == null) {
            depth = INFINITY;
        } else {
            if (depthStr.equals("0")) {
                depth = 0;
            } else if (depthStr.equals("1")) {
                depth = 1;
            } else if (depthStr.equals("infinity")) {
                depth = INFINITY;
            }
        }

        Node propNode = null;

        DocumentBuilder documentBuilder = getDocumentBuilder();

		// 2004/02/06  WHF  Added for Microsoft blank propfinds:
		if (req.getContentLength() != 0) {
        try {
            Document document = documentBuilder.parse
                (new InputSource(req.getInputStream()));

            // Get the root element of the document
            Element rootElement = document.getDocumentElement();
            NodeList childList = rootElement.getChildNodes();

            for (int i=0; i < childList.getLength(); i++) {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType()) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    if (currentNode.getNodeName().endsWith("prop")) {
                        type = FIND_BY_PROPERTY;
                        propNode = currentNode;
                    }
                    if (currentNode.getNodeName().endsWith("propname")) {
                        type = FIND_PROPERTY_NAMES;
                    }
                    if (currentNode.getNodeName().endsWith("allprop")) {
                        type = FIND_ALL_PROP;
                    }
                    break;
                }
            }
        } catch(Exception e) {
            // Most likely there was no content : we use the defaults.
            // TODO : Enhance that !
        }
		}

        if (type == FIND_BY_PROPERTY) {
            properties = new Vector();
            NodeList childList = propNode.getChildNodes();

            for (int i=0; i < childList.getLength(); i++) {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType()) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = currentNode.getNodeName();
                    String propertyName = null;
                    if (nodeName.indexOf(':') != -1) {
                        propertyName = nodeName.substring
                            (nodeName.indexOf(':') + 1);
                    } else {
                        propertyName = nodeName;
                    }
                    // href is a live property which is handled differently
                    properties.addElement(propertyName);
                    break;
                }
            }

        }

        // Retrieve the resources
        DirContext resources = getResources();

        if (resources == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        boolean exists = true;
        Object object = null;
        try {
            object = resources.lookup(path);
        } catch (NamingException e) {
            exists = false;
            int slash = path.lastIndexOf('/');
            if (slash != -1) {
                String parentPath = path.substring(0, slash);
                Vector currentLockNullResources =
                    (Vector) lockNullResources.get(parentPath);
                if (currentLockNullResources != null) {
                    Enumeration lockNullResourcesList =
                        currentLockNullResources.elements();
                    while (lockNullResourcesList.hasMoreElements()) {
                        String lockNullPath = (String)
                            lockNullResourcesList.nextElement();
                        if (lockNullPath.equals(path)) {
                            resp.setStatus(WebdavStatus.SC_MULTI_STATUS);
                            resp.setContentType("text/xml; charset=UTF-8");
                            // Create multistatus object
                            XMLWriter generatedXML = 
                                new XMLWriter(resp.getWriter());
                            generatedXML.writeXMLHeader();
                            generatedXML.writeElement
                                (DN_ALIAS, "multistatus"
                                 + generateNamespaceDeclarations(),
                                 XMLWriter.OPENING);
                            parseLockNullProperties
                                (req, generatedXML, lockNullPath, type,
                                 properties);
                            generatedXML.writeElement(DN_ALIAS, "multistatus",
                                                      XMLWriter.CLOSING);
                            generatedXML.sendData();
                            return;
                        }
                    }
                }
            }
        }

        if (!exists) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }

        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);

        resp.setContentType("text/xml; charset=UTF-8");

        // Create multistatus object
        XMLWriter generatedXML = new XMLWriter(resp.getWriter());
        generatedXML.writeXMLHeader();

        generatedXML.writeElement(DN_ALIAS, "multistatus"
                                  + generateNamespaceDeclarations(),
                                  XMLWriter.OPENING);

        if (depth == 0) {
            parseProperties(req, resources, generatedXML, path, type,
                            properties);
        } else {
            // The stack always contains the object of the current level
            Stack stack = new Stack();
            stack.push(path);

            // Stack of the objects one level below
            Stack stackBelow = new Stack();

            while ((!stack.isEmpty()) && (depth >= 0)) {

                String currentPath = (String) stack.pop();
                parseProperties(req, resources, generatedXML, currentPath,
                                type, properties);

                try {
                    object = resources.lookup(currentPath);
                } catch (NamingException e) {
                    continue;
                }

                if ((object instanceof DirContext) && (depth > 0)) {

                    try {
                        NamingEnumeration _enum = resources.list(currentPath);
                        while (_enum.hasMoreElements()) {
                            NameClassPair ncPair =
                                (NameClassPair) _enum.nextElement();
                            String newPath = currentPath;
                            if (!(newPath.endsWith("/")))
                                newPath += "/";
                            newPath += ncPair.getName();
                            stackBelow.push(newPath);
                        }
                    } catch (NamingException e) {
                        resp.sendError
                            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                             path);
                        return;
                    }

                    // Displaying the lock-null resources present in that
                    // collection
                    String lockPath = currentPath;
                    if (lockPath.endsWith("/"))
                        lockPath = 
                            lockPath.substring(0, lockPath.length() - 1);
                    Vector currentLockNullResources =
                        (Vector) lockNullResources.get(lockPath);
                    if (currentLockNullResources != null) {
                        Enumeration lockNullResourcesList =
                            currentLockNullResources.elements();
                        while (lockNullResourcesList.hasMoreElements()) {
                            String lockNullPath = (String)
                                lockNullResourcesList.nextElement();
                            parseLockNullProperties
                                (req, generatedXML, lockNullPath, type,
                                 properties);
                        }
                    }

                }

                if (stack.isEmpty()) {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack();
                }

                generatedXML.sendData();

            }
        }

        generatedXML.writeElement(DN_ALIAS, "multistatus",
                                  XMLWriter.CLOSING);

        generatedXML.sendData();

    }
	
	
    /**
     * Propfind helper method.
     *
     * @param resources Resources object associated with this context
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by
     * name, then this Vector contains those properties
     */
	// 03/10/2003  WHF  Added support for request parameters.
    private void parseProperties(HttpServletRequest req,
                                 DirContext resources, XMLWriter generatedXML,
                                 String path, int type,
                                 Vector propertiesVector) {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if (path.toUpperCase().startsWith("/WEB-INF") ||
            path.toUpperCase().startsWith("/META-INF"))
            return;

        RBNBResourceInfo resourceInfo = new RBNBResourceInfo(path, resources,
			req.getParameterMap());

        generatedXML.writeElement(DN_ALIAS, "response", XMLWriter.OPENING);
        String status = new String("HTTP/1.1 " + WebdavStatus.SC_OK + " "
                                   + WebdavStatus.getStatusText
                                   (WebdavStatus.SC_OK));

        // Generating href element
        generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);

        String href = req.getContextPath();
        if ((href.endsWith("/")) && (path.startsWith("/")))
            href += path.substring(1);
        else
            href += path;
        if ((resourceInfo.collection) && (!href.endsWith("/")))
            href += "/";

        generatedXML.writeText(rewriteUrl(href));

        generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.CLOSING);

        String resourceName = path;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1)
            resourceName = resourceName.substring(lastSlash + 1);

        switch (type) {

        case FIND_ALL_PROP :

            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

            generatedXML.writeProperty
                (DN_ALIAS, "creationdate",
                 getISOCreationDate(resourceInfo.creationDate));
            generatedXML.writeElement(DN_ALIAS, "displayname", XMLWriter.OPENING);
            generatedXML.writeData(resourceName);
            generatedXML.writeElement(DN_ALIAS, "displayname", XMLWriter.CLOSING);
            generatedXML.writeProperty(DN_ALIAS, "getcontentlanguage",
                                       Locale.getDefault().toString());
            if (!resourceInfo.collection) {
                generatedXML.writeProperty
                    (DN_ALIAS, "getlastmodified", resourceInfo.httpDate);
                generatedXML.writeProperty
                    (DN_ALIAS, "getcontentlength",
                     String.valueOf(resourceInfo.length));
                generatedXML.writeProperty
                    (DN_ALIAS, "getcontenttype",resourceInfo.mimeType==null?
                     getServletContext().getMimeType(resourceInfo.path)
					 :resourceInfo.mimeType);
                generatedXML.writeProperty(DN_ALIAS, "getetag",
                            // jak 4.0.1: getETagValue(resourceInfo, true));
							getETagValue(resourceInfo));
                generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                          XMLWriter.NO_CONTENT);
            } else {
                generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                          XMLWriter.OPENING);
                generatedXML.writeElement(DN_ALIAS, "collection",
                                          XMLWriter.NO_CONTENT);
                generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                          XMLWriter.CLOSING);
            }

            generatedXML.writeProperty(DN_ALIAS, "source", "");

            String supportedLocks = "<lockentry>"
                + "<lockscope><exclusive/></lockscope>"
                + "<locktype><write/></locktype>"
                + "</lockentry>" + "<lockentry>"
                + "<lockscope><shared/></lockscope>"
                + "<locktype><write/></locktype>"
                + "</lockentry>";
            generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                      XMLWriter.OPENING);
            generatedXML.writeText(supportedLocks);
            generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                      XMLWriter.CLOSING);

            generateLockDiscovery(path, generatedXML);

            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            break;

        case FIND_PROPERTY_NAMES :

            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

            generatedXML.writeElement(DN_ALIAS, "creationdate",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "displayname",
                                      XMLWriter.NO_CONTENT);
            if (!resourceInfo.collection) {
                generatedXML.writeElement(DN_ALIAS, "getcontentlanguage",
                                          XMLWriter.NO_CONTENT);
                generatedXML.writeElement(DN_ALIAS, "getcontentlength",
                                          XMLWriter.NO_CONTENT);
                generatedXML.writeElement(DN_ALIAS, "getcontenttype",
                                          XMLWriter.NO_CONTENT);
                generatedXML.writeElement(DN_ALIAS, "getetag",
                                          XMLWriter.NO_CONTENT);
                generatedXML.writeElement(DN_ALIAS, "getlastmodified",
                                          XMLWriter.NO_CONTENT);
            }
            generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "source", XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "lockdiscovery",
                                      XMLWriter.NO_CONTENT);

            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            break;

        case FIND_BY_PROPERTY :

            Vector propertiesNotFound = new Vector();

            // Parse the list of properties

            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

            Enumeration properties = propertiesVector.elements();

            while (properties.hasMoreElements()) {

                String property = (String) properties.nextElement();

                if (property.equals("creationdate")) {
                    generatedXML.writeProperty
                        (DN_ALIAS, "creationdate",
                         getISOCreationDate(resourceInfo.creationDate));
                } else if (property.equals("displayname")) {
                    generatedXML.writeElement
                        (DN_ALIAS, "displayname", XMLWriter.OPENING);
                    generatedXML.writeData(resourceName);
                    generatedXML.writeElement
                        (DN_ALIAS, "displayname", XMLWriter.CLOSING);
                } else if (property.equals("getcontentlanguage")) {
                    if (resourceInfo.collection) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            (DN_ALIAS, "getcontentlanguage",
                             Locale.getDefault().toString());
                    }
                } else if (property.equals("getcontentlength")) {
                    if (resourceInfo.collection) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            (DN_ALIAS, "getcontentlength",
                             (String.valueOf(resourceInfo.length)));
                    }
                } else if (property.equals("getcontenttype")) {
                    if (resourceInfo.collection) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            (DN_ALIAS, "getcontenttype",
                             getServletContext().getMimeType
                             (resourceInfo.path));
                    }
                } else if (property.equals("getetag")) {
                    if (resourceInfo.collection) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            (DN_ALIAS, "getetag",
                             // jak 4.0.1: getETagValue(resourceInfo, true));
							 getETagValue(resourceInfo));
                    }
                } else if (property.equals("getlastmodified")) {
                    if (resourceInfo.collection) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            (DN_ALIAS, "getlastmodified", resourceInfo.httpDate);
                    }
                } else if (property.equals("resourcetype")) {
                    if (resourceInfo.collection) {
                        generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                                  XMLWriter.OPENING);
                        generatedXML.writeElement(DN_ALIAS, "collection",
                                                  XMLWriter.NO_CONTENT);
                        generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                                  XMLWriter.CLOSING);
                    } else {
                        generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                                  XMLWriter.NO_CONTENT);
                    }
                } else if (property.equals("source")) {
                    generatedXML.writeProperty(DN_ALIAS, "source", "");
                } else if (property.equals("supportedlock")) {
                    supportedLocks = "<lockentry>"
                        + "<lockscope><exclusive/></lockscope>"
                        + "<locktype><write/></locktype>"
                        + "</lockentry>" + "<lockentry>"
                        + "<lockscope><shared/></lockscope>"
                        + "<locktype><write/></locktype>"
                        + "</lockentry>";
                    generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                              XMLWriter.OPENING);
                    generatedXML.writeText(supportedLocks);
                    generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                              XMLWriter.CLOSING);
                } else if (property.equals("lockdiscovery")) {
                    if (!generateLockDiscovery(path, generatedXML))
                        propertiesNotFound.addElement(property);
                } else {
                    propertiesNotFound.addElement(property);
                }

            }

            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            Enumeration propertiesNotFoundList = propertiesNotFound.elements();

            if (propertiesNotFoundList.hasMoreElements()) {

                status = new String("HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND
                                    + " " + WebdavStatus.getStatusText
                                    (WebdavStatus.SC_NOT_FOUND));

                generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
                generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

                while (propertiesNotFoundList.hasMoreElements()) {
                    generatedXML.writeElement
                        (DN_ALIAS, (String) propertiesNotFoundList.nextElement(),
                         XMLWriter.NO_CONTENT);
                }

                generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
                generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
                generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            }

            break;

        }

        generatedXML.writeElement(DN_ALIAS, "response", XMLWriter.CLOSING);

    }


    /**
     * Propfind helper method. Dispays the properties of a lock-null resource.
     *
     * @param resources Resources object associated with this context
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by
     * name, then this Vector contains those properties
     */
    private void parseLockNullProperties(HttpServletRequest req,
                                         XMLWriter generatedXML,
                                         String path, int type,
                                         Vector propertiesVector) {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if (path.toUpperCase().startsWith("/WEB-INF") ||
            path.toUpperCase().startsWith("/META-INF"))
            return;

        // Retrieving the lock associated with the lock-null resource
        LockInfo lock = (LockInfo) resourceLocks.get(path);

        if (lock == null)
            return;

        generatedXML.writeElement(DN_ALIAS, "response", XMLWriter.OPENING);
        String status = new String("HTTP/1.1 " + WebdavStatus.SC_OK + " "
                                   + WebdavStatus.getStatusText
                                   (WebdavStatus.SC_OK));

        // Generating href element
        generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath(req);
        String toAppend = path.substring(relativePath.length());
        if (!toAppend.startsWith("/"))
            toAppend = "/" + toAppend;

        generatedXML.writeText(rewriteUrl(normalize(absoluteUri + toAppend)));

        generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.CLOSING);

        String resourceName = path;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1)
            resourceName = resourceName.substring(lastSlash + 1);

        switch (type) {

        case FIND_ALL_PROP :

            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

            generatedXML.writeProperty
                (DN_ALIAS, "creationdate",
                 getISOCreationDate(lock.creationDate.getTime()));
            generatedXML.writeElement
                (DN_ALIAS, "displayname", XMLWriter.OPENING);
            generatedXML.writeData(resourceName);
            generatedXML.writeElement
                (DN_ALIAS, "displayname", XMLWriter.CLOSING);
            generatedXML.writeProperty(DN_ALIAS, "getcontentlanguage",
                                       Locale.getDefault().toString());
            generatedXML.writeProperty(DN_ALIAS, "getlastmodified",
                                       formats[0].format(lock.creationDate));
            generatedXML.writeProperty
                (DN_ALIAS, "getcontentlength", String.valueOf(0));
            generatedXML.writeProperty(DN_ALIAS, "getcontenttype", "");
            generatedXML.writeProperty(DN_ALIAS, "getetag", "");
            generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                      XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "lock-null", XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                      XMLWriter.CLOSING);

            generatedXML.writeProperty(DN_ALIAS, "source", "");

            String supportedLocks = "<lockentry>"
                + "<lockscope><exclusive/></lockscope>"
                + "<locktype><write/></locktype>"
                + "</lockentry>" + "<lockentry>"
                + "<lockscope><shared/></lockscope>"
                + "<locktype><write/></locktype>"
                + "</lockentry>";
            generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                      XMLWriter.OPENING);
            generatedXML.writeText(supportedLocks);
            generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                      XMLWriter.CLOSING);

            generateLockDiscovery(path, generatedXML);

            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            break;

        case FIND_PROPERTY_NAMES :

            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

            generatedXML.writeElement(DN_ALIAS, "creationdate",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "displayname",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "getcontentlanguage",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "getcontentlength",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "getcontenttype",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "getetag",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "getlastmodified",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "source",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "lockdiscovery",
                                      XMLWriter.NO_CONTENT);

            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            break;

        case FIND_BY_PROPERTY :

            Vector propertiesNotFound = new Vector();

            // Parse the list of properties

            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

            Enumeration properties = propertiesVector.elements();

            while (properties.hasMoreElements()) {

                String property = (String) properties.nextElement();

                if (property.equals("creationdate")) {
                    generatedXML.writeProperty
                        (DN_ALIAS, "creationdate",
                         getISOCreationDate(lock.creationDate.getTime()));
                } else if (property.equals("displayname")) {
                    generatedXML.writeElement
                        (DN_ALIAS, "displayname", XMLWriter.OPENING);
                    generatedXML.writeData(resourceName);
                    generatedXML.writeElement
                        (DN_ALIAS, "displayname", XMLWriter.CLOSING);
                } else if (property.equals("getcontentlanguage")) {
                    generatedXML.writeProperty
                        (DN_ALIAS, "getcontentlanguage",
                         Locale.getDefault().toString());
                } else if (property.equals("getcontentlength")) {
                    generatedXML.writeProperty
                        (DN_ALIAS, "getcontentlength", (String.valueOf(0)));
                } else if (property.equals("getcontenttype")) {
                    generatedXML.writeProperty
                        (DN_ALIAS, "getcontenttype", "");
                } else if (property.equals("getetag")) {
                    generatedXML.writeProperty(DN_ALIAS, "getetag", "");
                } else if (property.equals("getlastmodified")) {
                    generatedXML.writeProperty
                        (DN_ALIAS, "getlastmodified",
                         formats[0].format(lock.creationDate));
                } else if (property.equals("resourcetype")) {
                    generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                              XMLWriter.OPENING);
                    generatedXML.writeElement(DN_ALIAS, "lock-null",
                                              XMLWriter.NO_CONTENT);
                    generatedXML.writeElement(DN_ALIAS, "resourcetype",
                                              XMLWriter.CLOSING);
                } else if (property.equals("source")) {
                    generatedXML.writeProperty(DN_ALIAS, "source", "");
                } else if (property.equals("supportedlock")) {
                    supportedLocks = "<lockentry>"
                        + "<lockscope><exclusive/></lockscope>"
                        + "<locktype><write/></locktype>"
                        + "</lockentry>" + "<lockentry>"
                        + "<lockscope><shared/></lockscope>"
                        + "<locktype><write/></locktype>"
                        + "</lockentry>";
                    generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                              XMLWriter.OPENING);
                    generatedXML.writeText(supportedLocks);
                    generatedXML.writeElement(DN_ALIAS, "supportedlock",
                                              XMLWriter.CLOSING);
                } else if (property.equals("lockdiscovery")) {
                    if (!generateLockDiscovery(path, generatedXML))
                        propertiesNotFound.addElement(property);
                } else {
                    propertiesNotFound.addElement(property);
                }

            }

            generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
            generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            Enumeration propertiesNotFoundList = propertiesNotFound.elements();

            if (propertiesNotFoundList.hasMoreElements()) {

                status = new String("HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND
                                    + " " + WebdavStatus.getStatusText
                                    (WebdavStatus.SC_NOT_FOUND));

                generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
                generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

                while (propertiesNotFoundList.hasMoreElements()) {
                    generatedXML.writeElement
                        (DN_ALIAS, (String) propertiesNotFoundList.nextElement(),
                         XMLWriter.NO_CONTENT);
                }

                generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
                generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
                generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

            }

            break;

        }

        generatedXML.writeElement(DN_ALIAS, "response", XMLWriter.CLOSING);

    }
	

    /**
     * Get creation date in ISO format.
     */
    private String getISOCreationDate(long creationDate) {
        StringBuffer creationDateValue = new StringBuffer
            (creationDateFormat.format
             (new Date(creationDate)));
        /*
        int offset = Calendar.getInstance().getTimeZone().getRawOffset()
            / 3600000; // FIXME ?
        if (offset < 0) {
            creationDateValue.append("-");
            offset = -offset;
        } else if (offset > 0) {
            creationDateValue.append("+");
        }
        if (offset != 0) {
            if (offset < 10)
                creationDateValue.append("0");
            creationDateValue.append(offset + ":00");
        } else {
            creationDateValue.append("Z");
        }
        */
        return creationDateValue.toString();
    }

    /**
     * Print the lock discovery information associated with a path.
     *
     * @param path Path
     * @param generatedXML XML data to which the locks info will be appended
     * @return true if at least one lock was displayed
     */
    private boolean generateLockDiscovery
        (String path, XMLWriter generatedXML) {

        LockInfo resourceLock = (LockInfo) resourceLocks.get(path);
        Enumeration collectionLocksList = collectionLocks.elements();

        boolean wroteStart = false;

        if (resourceLock != null) {
            wroteStart = true;
            generatedXML.writeElement(DN_ALIAS, "lockdiscovery",
                                      XMLWriter.OPENING);
            resourceLock.toXML(generatedXML);
        }

        while (collectionLocksList.hasMoreElements()) {
            LockInfo currentLock =
                (LockInfo) collectionLocksList.nextElement();
            if (path.startsWith(currentLock.path)) {
                if (!wroteStart) {
                    wroteStart = true;
                    generatedXML.writeElement(DN_ALIAS, "lockdiscovery",
                                              XMLWriter.OPENING);
                }
                currentLock.toXML(generatedXML);
            }
        }

        if (wroteStart) {
            generatedXML.writeElement(DN_ALIAS, "lockdiscovery",
                                      XMLWriter.CLOSING);
        } else {
            return false;
        }

        return true;

    }
	
    /**
     * Check to see if a default page exists.
     *
     * @param pathname Pathname of the file to be served
     */
    private ResourceInfo checkWelcomeFiles(String pathname,
                                           DirContext resources) {

        String collectionName = pathname;
        if (!pathname.endsWith("/")) {
            collectionName += "/";
        }

        // Refresh our currently defined set of welcome files
        String[] welcomes = (String[]) getServletContext().getAttribute
                (Globals.WELCOME_FILES_ATTR);
            if (welcomes == null)
                welcomes = new String[0];

        // Serve a welcome resource or file if one exists
        for (int i = 0; i < welcomes.length; i++) {

            // Does the specified resource exist?
            String resourceName = collectionName + welcomes[i];
            ResourceInfo resourceInfo =
                new ResourceInfo(resourceName, resources);
            if (resourceInfo.exists()) {
                return resourceInfo;
            }

        }

        return null;

    }
	

    /**
     * Holds a lock information.
     */
    private class LockInfo {


        // -------------------------------------------------------- Constructor


        /**
         * Constructor.
         *
         * @param pathname Path name of the file
         */
        public LockInfo() {

        }


        // ------------------------------------------------- Instance Variables


        String path = "/";
        String type = "write";
        String scope = "exclusive";
        int depth = 0;
        String owner = "";
        Vector tokens = new Vector();
        long expiresAt = 0;
        Date creationDate = new Date();


        // ----------------------------------------------------- Public Methods


        /**
         * Get a String representation of this lock token.
         */
        public String toString() {

            String result =  "Type:" + type + "\n";
            result += "Scope:" + scope + "\n";
            result += "Depth:" + depth + "\n";
            result += "Owner:" + owner + "\n";
            result += "Expiration:" +
                formats[0].format(new Date(expiresAt)) + "\n";
            Enumeration tokensList = tokens.elements();
            while (tokensList.hasMoreElements()) {
                result += "Token:" + tokensList.nextElement() + "\n";
            }
            return result;

        }


        /**
         * Return true if the lock has expired.
         */
        public boolean hasExpired() {
            return (System.currentTimeMillis() > expiresAt);
        }


        /**
         * Return true if the lock is exclusive.
         */
        public boolean isExclusive() {

            return (scope.equals("exclusive"));

        }


        /**
         * Get an XML representation of this lock token. This method will
         * append an XML fragment to the given XML writer.
         */
        public void toXML(XMLWriter generatedXML) {
            toXML(generatedXML, false);
        }


        /**
         * Get an XML representation of this lock token. This method will
         * append an XML fragment to the given XML writer.
         */
        public void toXML(XMLWriter generatedXML, boolean showToken) {

            generatedXML.writeElement(DN_ALIAS, "activelock", XMLWriter.OPENING);

            generatedXML.writeElement(DN_ALIAS, "locktype", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, type, XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "locktype", XMLWriter.CLOSING);

            generatedXML.writeElement(DN_ALIAS, "lockscope", XMLWriter.OPENING);
            generatedXML.writeElement(DN_ALIAS, scope, XMLWriter.NO_CONTENT);
            generatedXML.writeElement(DN_ALIAS, "lockscope", XMLWriter.CLOSING);

            generatedXML.writeElement(DN_ALIAS, "depth", XMLWriter.OPENING);
            if (depth == INFINITY) {
                generatedXML.writeText("Infinity");
            } else {
                generatedXML.writeText("0");
            }
            generatedXML.writeElement(DN_ALIAS, "depth", XMLWriter.CLOSING);

            generatedXML.writeElement(DN_ALIAS, "owner", XMLWriter.OPENING);
            generatedXML.writeText(owner);
            generatedXML.writeElement(DN_ALIAS, "owner", XMLWriter.CLOSING);

            generatedXML.writeElement(DN_ALIAS, "timeout", XMLWriter.OPENING);
            long timeout = (expiresAt - System.currentTimeMillis()) / 1000;
            generatedXML.writeText("Second-" + timeout);
            generatedXML.writeElement(DN_ALIAS, "timeout", XMLWriter.CLOSING);

            generatedXML.writeElement(DN_ALIAS, "locktoken", XMLWriter.OPENING);
            if (showToken) {
                Enumeration tokensList = tokens.elements();
                while (tokensList.hasMoreElements()) {
                    generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);
                    generatedXML.writeText("opaquelocktoken:"
                                           + tokensList.nextElement());
                    generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.CLOSING);
                }
            } else {
                generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);
                generatedXML.writeText("opaquelocktoken:dummytoken");
                generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.CLOSING);
            }
            generatedXML.writeElement(DN_ALIAS, "locktoken", XMLWriter.CLOSING);

            generatedXML.writeElement(DN_ALIAS, "activelock", XMLWriter.CLOSING);

        }


    }

    /**
     * Repository of the locks put on single resources.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private Hashtable resourceLocks = new Hashtable();

   /**
     * Vector of the heritable locks.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private Vector collectionLocks = new Vector();

	
// end hack	
	
// ************************ Private Instance Data ***************************//
	private DirContext rbnbDirContext; 
}



// More hack:

// --------------------------------------------------------  WebdavStatus Class


/**
 * Wraps the HttpServletResponse class to abstract the
 * specific protocol used.  To support other protocols
 * we would only need to modify this class and the
 * WebDavRetCode classes.
 *
 * @author              Marc Eaddy
 * @version             1.0, 16 Nov 1997
 */
class WebdavStatus {


    // ----------------------------------------------------- Instance Variables


    /**
     * This Hashtable contains the mapping of HTTP and WebDAV
     * status codes to descriptive text.  This is a static
     * variable.
     */
    private static Hashtable mapStatusCodes = new Hashtable();


    // ------------------------------------------------------ HTTP Status Codes


    /**
     * Status code (200) indicating the request succeeded normally.
     */
    public static final int SC_OK = HttpServletResponse.SC_OK;


    /**
     * Status code (201) indicating the request succeeded and created
     * a new resource on the server.
     */
    public static final int SC_CREATED = HttpServletResponse.SC_CREATED;


    /**
     * Status code (202) indicating that a request was accepted for
     * processing, but was not completed.
     */
    public static final int SC_ACCEPTED = HttpServletResponse.SC_ACCEPTED;


    /**
     * Status code (204) indicating that the request succeeded but that
     * there was no new information to return.
     */
    public static final int SC_NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;


    /**
     * Status code (301) indicating that the resource has permanently
     * moved to a new location, and that future references should use a
     * new URI with their requests.
     */
    public static final int SC_MOVED_PERMANENTLY =
        HttpServletResponse.SC_MOVED_PERMANENTLY;


    /**
     * Status code (302) indicating that the resource has temporarily
     * moved to another location, but that future references should
     * still use the original URI to access the resource.
     */
    public static final int SC_MOVED_TEMPORARILY =
        HttpServletResponse.SC_MOVED_TEMPORARILY;


    /**
     * Status code (304) indicating that a conditional GET operation
     * found that the resource was available and not modified.
     */
    public static final int SC_NOT_MODIFIED =
        HttpServletResponse.SC_NOT_MODIFIED;


    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    public static final int SC_BAD_REQUEST =
        HttpServletResponse.SC_BAD_REQUEST;


    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    public static final int SC_UNAUTHORIZED =
        HttpServletResponse.SC_UNAUTHORIZED;


    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */
    public static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;


    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    public static final int SC_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;


    /**
     * Status code (500) indicating an error inside the HTTP service
     * which prevented it from fulfilling the request.
     */
    public static final int SC_INTERNAL_SERVER_ERROR =
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR;


    /**
     * Status code (501) indicating the HTTP service does not support
     * the functionality needed to fulfill the request.
     */
    public static final int SC_NOT_IMPLEMENTED =
        HttpServletResponse.SC_NOT_IMPLEMENTED;


    /**
     * Status code (502) indicating that the HTTP server received an
     * invalid response from a server it consulted when acting as a
     * proxy or gateway.
     */
    public static final int SC_BAD_GATEWAY =
        HttpServletResponse.SC_BAD_GATEWAY;


    /**
     * Status code (503) indicating that the HTTP service is
     * temporarily overloaded, and unable to handle the request.
     */
    public static final int SC_SERVICE_UNAVAILABLE =
        HttpServletResponse.SC_SERVICE_UNAVAILABLE;


    /**
     * Status code (100) indicating the client may continue with
     * its request.  This interim response is used to inform the
     * client that the initial part of the request has been
     * received and has not yet been rejected by the server.
     */
    public static final int SC_CONTINUE = 100;


    /**
     * Status code (405) indicating the method specified is not
     * allowed for the resource.
     */
    public static final int SC_METHOD_NOT_ALLOWED = 405;


    /**
     * Status code (409) indicating that the request could not be
     * completed due to a conflict with the current state of the
     * resource.
     */
    public static final int SC_CONFLICT = 409;


    /**
     * Status code (412) indicating the precondition given in one
     * or more of the request-header fields evaluated to false
     * when it was tested on the server.
     */
    public static final int SC_PRECONDITION_FAILED = 412;


    /**
     * Status code (413) indicating the server is refusing to
     * process a request because the request entity is larger
     * than the server is willing or able to process.
     */
    public static final int SC_REQUEST_TOO_LONG = 413;


    /**
     * Status code (415) indicating the server is refusing to service
     * the request because the entity of the request is in a format
     * not supported by the requested resource for the requested
     * method.
     */
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;


    // -------------------------------------------- Extended WebDav status code


    /**
     * Status code (207) indicating that the response requires
     * providing status for multiple independent operations.
     */
    public static final int SC_MULTI_STATUS = 207;
    // This one colides with HTTP 1.1
    // "207 Parital Update OK"


    /**
     * Status code (418) indicating the entity body submitted with
     * the PATCH method was not understood by the resource.
     */
    public static final int SC_UNPROCESSABLE_ENTITY = 418;
    // This one colides with HTTP 1.1
    // "418 Reauthentication Required"


    /**
     * Status code (419) indicating that the resource does not have
     * sufficient space to record the state of the resource after the
     * execution of this method.
     */
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    // This one colides with HTTP 1.1
    // "419 Proxy Reauthentication Required"


    /**
     * Status code (420) indicating the method was not executed on
     * a particular resource within its scope because some part of
     * the method's execution failed causing the entire method to be
     * aborted.
     */
    public static final int SC_METHOD_FAILURE = 420;


    /**
     * Status code (423) indicating the destination resource of a
     * method is locked, and either the request did not contain a
     * valid Lock-Info header, or the Lock-Info header identifies
     * a lock held by another principal.
     */
    public static final int SC_LOCKED = 423;


    // ------------------------------------------------------------ Initializer


    static {
        // HTTP 1.0 tatus Code
        addStatusCodeMap(SC_OK, "OK");
        addStatusCodeMap(SC_CREATED, "Created");
        addStatusCodeMap(SC_ACCEPTED, "Accepted");
        addStatusCodeMap(SC_NO_CONTENT, "No Content");
        addStatusCodeMap(SC_MOVED_PERMANENTLY, "Moved Permanently");
        addStatusCodeMap(SC_MOVED_TEMPORARILY, "Moved Temporarily");
        addStatusCodeMap(SC_NOT_MODIFIED, "Not Modified");
        addStatusCodeMap(SC_BAD_REQUEST, "Bad Request");
        addStatusCodeMap(SC_UNAUTHORIZED, "Unauthorized");
        addStatusCodeMap(SC_FORBIDDEN, "Forbidden");
        addStatusCodeMap(SC_NOT_FOUND, "Not Found");
        addStatusCodeMap(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        addStatusCodeMap(SC_NOT_IMPLEMENTED, "Not Implemented");
        addStatusCodeMap(SC_BAD_GATEWAY, "Bad Gateway");
        addStatusCodeMap(SC_SERVICE_UNAVAILABLE, "Service Unavailable");
        addStatusCodeMap(SC_CONTINUE, "Continue");
        addStatusCodeMap(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
        addStatusCodeMap(SC_CONFLICT, "Conflict");
        addStatusCodeMap(SC_PRECONDITION_FAILED, "Precondition Failed");
        addStatusCodeMap(SC_REQUEST_TOO_LONG, "Request Too Long");
        addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        // WebDav Status Codes
        addStatusCodeMap(SC_MULTI_STATUS, "Multi-Status");
        addStatusCodeMap(SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity");
        addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE,
                         "Insufficient Space On Resource");
        addStatusCodeMap(SC_METHOD_FAILURE, "Method Failure");
        addStatusCodeMap(SC_LOCKED, "Locked");
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Returns the HTTP status text for the HTTP or WebDav status code
     * specified by looking it up in the static mapping.  This is a
     * static function.
     *
     * @param   nHttpStatusCode [IN] HTTP or WebDAV status code
     * @return  A string with a short descriptive phrase for the
     *                  HTTP status code (e.g., "OK").
     */
    public static String getStatusText(int nHttpStatusCode) {
        Integer intKey = new Integer(nHttpStatusCode);

        if (!mapStatusCodes.containsKey(intKey)) {
            return "";
        } else {
            return (String) mapStatusCodes.get(intKey);
        }
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Adds a new status code -> status text mapping.  This is a static
     * method because the mapping is a static variable.
     *
     * @param   nKey    [IN] HTTP or WebDAV status code
     * @param   strVal  [IN] HTTP status text
     */
    private static void addStatusCodeMap(int nKey, String strVal) {
        mapStatusCodes.put(new Integer(nKey), strVal);
    }

	
	

};

