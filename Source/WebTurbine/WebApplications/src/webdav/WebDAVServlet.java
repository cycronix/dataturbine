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

package com.rbnb.web;

import java.net.URLDecoder;
import java.net.URLEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

// Required to look up MIME types based on extension:
import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;

import org.w3c.dom.Node;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.apache.catalina.util.DOMWriter;
import org.apache.catalina.util.MD5Encoder;
import org.apache.catalina.util.XMLWriter;

// Modification History:
// 2005/07/06  WHF	Created.
// 2005/10/03  WHF	Code review.
// 2005/11/30  WHF  Made multi-threaded.
// 2005/12/09  WHF  Added support for POST.
// 2005/12/20  WHF  Broke some code into getFile(); uses setContentLength().
// 2006/01/30  WHF  Added If-Modifed-Since logic.
// 2006/08/14  WHF  Removed 'uplink' from 'Up one level'; now uses '..'.
// 2007/04/20  WHF  Added plug-in option parsing.
//

/**
  * An instance of a servlet to handle HTTP requests of RBNB channels, including
  *	  the methods specified by the WebDAV extension.<br><br>
  *
*/
public final class WebDAVServlet extends HttpServlet 
	implements PropertyConstants
{
	/**
	 * Default namespace.
	 */
	protected static final String DEFAULT_NAMESPACE = "DAV:";
	
	/**
	 * Default namespace alias.
	 */
	static final String DN_ALIAS = "D";
	
	/**
	 * Generate the namespace declarations.
	 */
	static String NAMESPACE_DECL = 
		" xmlns:"+DN_ALIAS+"=\"" + DEFAULT_NAMESPACE + "\"";
		
	/**
	  * MIME types used to identify special zero-length RBNB channels.
	  */
	private static final String 
			COLLECTION_MIME = "application/collection",
			ZEROFILE_MIME = "application/nullfile";
			
	/**
	  * Maximum number of connection objects retained.  Others are discarded.
	  */
	private static final int MAX_POOL_SIZE = 4;
		
	//--------------------------------------------------------
	/**
	* Constructor loads properites from the "env-entry" section of the 
	*	web.xml file.
	*/	 
	public WebDAVServlet() throws NamingException
	{
		// Load properites from the "env-entry" section of the web.xml file.
		InitialContext webEnv=new InitialContext();
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.defaulthost",
			"hostname",
			"localhost:3333");
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.defaultarchive",
			"archivesize",
			new Integer(10000));
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.defaultarchivemode",
			"archivemode",
			"none");
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.defaultcache",
			"cachesize",
			new Integer(100));
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.sinkname",
			"sinkname",
			"rbnbWebDavSink");
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.defaultuser",
			"username",
			"guest");			
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.defaultpassword",
			"password",
			"");
		checkEnv(
			webEnv,
			"java:comp/env/com.rbnb.web.debug",
			"debug",
			Boolean.FALSE);
		debug = ((Boolean) defaultProperties.get("debug")).booleanValue();		
	}
	
	//--------------------------------------------------------
	/**
	*  Where all servlet requests first enter.
	*
	*/	
	protected void service(
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException
	{
		try{	  
			res.setStatus(res.SC_OK);
			String method = req.getMethod();
			
if (debug) System.err.print(method);
if (debug) System.err.print(' ');
if (debug) System.err.print(req.getRequestURI());
String qs = req.getQueryString();
if (qs != null) {
	if (debug) System.err.print('?');
	if (debug) System.err.println(qs);
} else if (debug) System.err.println();
			
			// Implemented methods
			if (method.equals("GET"))
				doGet(req,res);
			else if (method.equals("PUT"))
				doPut(req,res);
			else if (method.equals("POST"))
				doPost(req, res);
			else if ("DELETE".equals(method))
				doDelete(req, res);
			// WebDAV Extensions:
			else if ("LOCK".equals(method))
				doLock(req, res);
			else if ("MKCOL".equals(method))
				doMkCol(req, res);
			else if(method.equals("PROPFIND"))
				doPropFind(req, res);
			else if ("PROPPATCH".equals(method))
				doPropPatch(req, res);
			else if ("UNLOCK".equals(method))
				doUnlock(req, res);
			else if(method.equals("OPTIONS"))
				doOptions(req,res);			
			else if(method.equals("HEAD"))
				doHead(req,res);
			else if ("COPY".equals(method))
				doCopy(req, res);
			else if ("MOVE".equals(method))
				doMove(req, res);
			else
				res.sendError(res.SC_METHOD_NOT_ALLOWED);
		}
		catch (SAPIException ex)
		{ ex.printStackTrace(); throw new ServletException(ex); }
	}
	
	/**
	 * LOCK Method.
	 */
	protected void doLock(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException, SAPIException
	{
		Connection conn = null;
		try {
		conn = connect(req);


		if (readOnly) {
			resp.sendError(WebdavStatus.SC_FORBIDDEN);
			return;
		}
		
		if (isLocked(req, conn.reqParam.path)) {
			resp.sendError(WebdavStatus.SC_LOCKED);
			return;
		}

		lockParser.doLock(req, resp, conn.reqParam.path, conn.ctree);
		} finally { recycleConnection(conn); }
	}


	/**
	 * UNLOCK Method.
	 */
	protected void doUnlock(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException, SAPIException
	{
		Connection conn = null;
		try {
		conn = connect(req);


		if (readOnly) {
			resp.sendError(WebdavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req, conn.reqParam.path)) { // checks to see if supplied tokens match
			resp.sendError(WebdavStatus.SC_LOCKED);
			return;
		}

		lockParser.doUnlock(req, resp, conn.reqParam.path);
		} finally { recycleConnection(conn); }
	}

	
	/**
	  * MKCOL method.
	  */
	protected void doMkCol(
			HttpServletRequest req,
			HttpServletResponse res) throws IOException, SAPIException
	{
		// RFC 2518 8.3.1 specifies that if the body is not understood, 
		//   the server should return 415 (Unsupported Media Type).  Since
		//   we don't support any body types, we should return that whenever
		//   a body is present.
		if (req.getContentLength() > 0) {
			res.sendError(res.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		
		Connection conn = null;
		try {
		conn = connect(req);
		
		if (conn.reqParam.source == null || conn.reqParam.source.length() == 0) {
			if (debug) System.err.println(
					"It is illegal to place files in the root directory.");
			res.sendError(res.SC_FORBIDDEN);
			return;
		}

		//look up source in hashtable & connect if new source
		Source src = (Source)sourcesHT.get(conn.reqParam.source);
		if (conn.reqParam.name.length() == 0) { // create source
			if (src==null ||
					!src.VerifyConnection()) { // check to ensure it didn't die
				src = new Source(
						conn.reqParam.getCacheSize(),
						conn.reqParam.getArchiveMode(),
						conn.reqParam.getArchiveSize());
				src.OpenRBNBConnection(
						defaultProperties.get("hostname").toString(),
						conn.reqParam.source,
						defaultProperties.get("username").toString(),
						defaultProperties.get("password").toString()						
				);
				// We store the source under the requested name, 
				//   even though the actual client name may be something
				//   else.
				sourcesHT.put(conn.reqParam.source, src);
				if (conn.reqParam.getProtect()) 
					protectSet.add(conn.reqParam.source);
			} else {
				// 2005/11/01  WHF  RFC specifies 405 for this error:
				//res.sendError(res.SC_METHOD_NOT_ALLOWED,
				//		"Source already exists.");
				// 2007/06/26  WHF  Even though this is supposed to be illegal,
				//  we allow it so that the user may change the ring buffer
				//  settings.
				src.SetRingBuffer(
						conn.reqParam.getCacheSize(),
						conn.reqParam.getArchiveMode(),
						conn.reqParam.getArchiveSize()
				);						
				return;
			}
		} else { // create subdirectory.
			if (src == null || !src.VerifyConnection()) {
				// Fail because parent source does not exist
				res.sendError(res.SC_CONFLICT, 
						"Parent container does not exist.");
				return;
			}

			ChannelTree.Node node = conn.ctree.findNode(conn.reqParam.path);
			if (node != null) { 
				// 2005/11/01  WHF  RFC specifies 405 for this error:
				res.sendError(res.SC_METHOD_NOT_ALLOWED, 
						"Collection already exists.");
				return;
			}
			node = conn.ctree.findNode(upLink(conn.reqParam.path));
			if (node == null) {
				res.sendError(res.SC_CONFLICT, 
						"Parent container does not exist.");
				return;
			}
			if (isChannel(node)) {
				// Contains data; we do not allow it to be a parent 
				//  folder through this interface.
				res.sendError(res.SC_FORBIDDEN, "Parent is not a container.");
				return;
			}
		
			// Create registration for null file.
			ChannelMap cmap = new ChannelMap();
			int index=cmap.Add(conn.reqParam.name);
			cmap.PutMime(index, "text/xml");
			cmap.PutDataAsString(
					index,
					"<rbnb><size>0</size><mime>"
						+COLLECTION_MIME+"</mime></rbnb>"
			);
			src.Register(cmap);
		}
		} finally { recycleConnection(conn); }
	}
	
	/**
	  * PROPFIND method.
	  */
	protected void doPropFind(
			HttpServletRequest req,
			HttpServletResponse res)
		throws IOException, SAPIException, ServletException
	{
		// Properties which are to be displayed.
		ArrayList properties = null;
		// Propfind type
		int type = FIND_ALL_PROP;

		// Parse propfind request XML:
		Node propNode = null;
		DocumentBuilder documentBuilder = getDocumentBuilder();

		// 2004/02/06  WHF	Added for Microsoft blank propfinds:
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
			properties = new ArrayList();
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
					properties.add(propertyName);
					break;
				}
			}

		}
		
		Connection conn = null;
		try {
		conn = connect(req);

		String path = conn.reqParam.path;

if (debug) System.err.println(conn.ctree);
if (debug) System.err.println(path);
		ChannelTree.Node parentNode = conn.ctree.findNode(path);
if (debug) System.err.println(parentNode);
		if (path.length() != 0 && parentNode == null) {
			// Properties on a non-existent resource:
			if (!lockParser.nullProperties(req, res, path, type, properties)) 
				res.sendError(HttpServletResponse.SC_NOT_FOUND, path);
				
			return; // done
		}
		
		// Propfind depth
		int depth = INFINITY;
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
		res.setStatus(WebdavStatus.SC_MULTI_STATUS);

		setNoCache(res);
		res.setContentType("text/xml; charset=UTF-8");

		// Create multistatus object
		XMLWriter generatedXML = new XMLWriter(res.getWriter());
		generatedXML.writeXMLHeader();

		generatedXML.writeElement(DN_ALIAS, "multistatus"
								  + NAMESPACE_DECL,
								  XMLWriter.OPENING);

		if (depth == 0) {
			parseProperties(req, parentNode, generatedXML, type,
							properties);
		} else {
			// The stack always contains the object of the current level
			Stack stack = new Stack();
			stack.push(path);

			// Stack of the objects one level below
			Stack stackBelow = new Stack();

			while ((!stack.isEmpty()) && (depth >= 0)) {

				String currentPath = (String) stack.pop();
				ChannelTree.Node node = conn.ctree.findNode(currentPath);
				parseProperties(req, node, generatedXML, type, properties);

				if (node == null) continue;

				if (!isChannel(node) && (depth > 0)) {
					Iterator iter = node.getChildren().iterator();
					while (iter.hasNext()) {
						ChannelTree.Node child = (ChannelTree.Node) iter.next();
						if (child.getType() == ChannelTree.SINK 
								|| child.getType() == ChannelTree.CONTROLLER)
							continue;
						/*
						String newPath = currentPath;
						if (!(newPath.endsWith("/")))
							newPath += "/";
						newPath += child.getName();
						stackBelow.push(newPath); */
						stackBelow.push(child.getFullName());
					}
					
					lockParser.parseCollectionLockNull(
							currentPath,
							req,
							generatedXML,
							type,
							properties
					);
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
		} finally { recycleConnection(conn); }
	} // end doPropFind()
	
	/**
	 * PROPPATCH Method.
	 */
	protected void doPropPatch(HttpServletRequest req,
							   HttpServletResponse resp)
		throws ServletException, IOException, SAPIException
	{
		Connection conn = null;
		try {
		conn = connect(req);

		if (readOnly) {
			resp.sendError(WebdavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req, conn.reqParam.path)) {
			resp.sendError(WebdavStatus.SC_LOCKED);
			return;
		}

		resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		} finally { recycleConnection(conn); }
	}

	
	/**
	 * OPTIONS Method.
	 */
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		// Indicate WEBDAV version supported:
		resp.addHeader("DAV", "1,2");

		RequestParameters2 reqParam = new RequestParameters2(defaultProperties);
		reqParam.parseQueryString(
				req.getRequestURI(),
				req.getQueryString(),
				null
		);

		String methodsAllowed = null;
		if (reqParam.source == null || reqParam.source.length() == 0) {
			// options for root
			methodsAllowed = "OPTIONS, GET, HEAD, POST";
		} else if (reqParam.name.length() == 0) {
			// Requesting a source.	 See if exists.
			if (sourcesHT.containsKey(reqParam.source)) {
				methodsAllowed = "OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, "
						+"PROPFIND, PROPPATCH, COPY, MOVE, LOCK, UNLOCK";
			} else { // source does not exist
				methodsAllowed = "OPTIONS, MKCOL";
			}
		} else { // general resource
			methodsAllowed = "OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, "
					+ "PROPFIND, PROPPATCH, COPY, MOVE, LOCK, UNLOCK";
		}		

		resp.addHeader("Allow", methodsAllowed);
		// A WebFolder's compatibility flag:
		resp.addHeader("MS-Author-Via", "DAV");
	}

	/**
	  * DELETE method.
	  */
	protected void doDelete(
			HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException
	{
		Connection conn = null;
		try {
		conn = connect(req);

        if (isLocked(req, conn.reqParam.path)) {
            res.sendError(WebdavStatus.SC_LOCKED);
            return;
        }
		
		if (conn.reqParam.source == null || conn.reqParam.source.length() == 0) {
			if (debug) System.err.println(
					"Cannot delete the root directory.");
			res.sendError(res.SC_FORBIDDEN);
			return;
		}

		//look up source in hashtable & delete if found
		Source src = (Source)sourcesHT.get(conn.reqParam.source);
		if (src != null) {
			if (conn.reqParam.name.length() == 0) { // delete the source itself
				src.CloseRBNBConnection();
				sourcesHT.remove(conn.reqParam.source);
				protectSet.remove(conn.reqParam.source);
			} else {
				if (conn.ctree.findNode(conn.reqParam.path) != null) {
					if (!protectSet.contains(conn.reqParam.source)) {
						// If not protected, delete:
						ChannelMap cmap = new ChannelMap();
						// We call for a recursive delete to remove any children.
						cmap.Add(conn.reqParam.name + "/...");
						src.Delete(cmap);
						// Delete the channel itself also.  Must be in a 
						//   separate request:
						cmap.Clear();
						cmap.Add(conn.reqParam.name); 
						src.Delete(cmap);
					} else res.sendError(res.SC_FORBIDDEN);
				} else res.sendError(res.SC_NOT_FOUND);
			}
		} else { // We don't control the source, so cannot delete.
			// See if file exists.  If so, return FORBIDDEN.  Otherwise, 
			//  return NOT_FOUND.
			if (conn.ctree.findNode(conn.reqParam.path) != null)
				res.sendError(res.SC_FORBIDDEN);
			else
				res.sendError(res.SC_NOT_FOUND);
		}
		} catch (SAPIException se) { throw new ServletException(se); }
		finally { recycleConnection(conn); }
	}
		
	//--------------------------------------------------------
	/**
	*  Handles the Http PUT operation.
	*
	*/	
	protected void doPut(
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException
	{		
		Connection conn = null;
		try {
			conn = connect(req);

			if (conn.reqParam.name.length() == 0) {
				System.out.println(
						"It is illegal to place files in the root directory.");
				res.sendError(res.SC_FORBIDDEN);
				return;
			}				
			if (isLocked(req, conn.reqParam.path)) {
				res.sendError(WebdavStatus.SC_LOCKED);
				return;
			}
			String fileName = conn.reqParam.name;

			//look up source in hashtable & connect if new source
			Source src = (Source)sourcesHT.get(conn.reqParam.source);
			if(src==null) {
				res.sendError(res.SC_CONFLICT);
				return;
			}
			
			//Get byteorder
			ChannelMap.ByteOrderEnum byteorder = conn.reqParam.byteorder;
			
			//Get datatype
			int datatype = conn.reqParam.datatype;
			
			//Get mime
			String mime = conn.reqParam.getMime(getServletContext(), fileName);
			// System.out.println("Mime: "+mime);
			
			// Get data as an array of bytes:
			ServletInputStream sis = req.getInputStream();
			byte[] array;
			int len = req.getContentLength();
			if (len == 0) { // Microsoft client likes to put empty files.
				registerNullChannels(src, conn);
				return;
			}				
			if (len != -1) { // length is known
				array = new byte[len];
				int bytesRead, totalBytes = 0;
				while ((bytesRead = sis.read(array, totalBytes, len-totalBytes))
						!= -1) {
					totalBytes += bytesRead;
					if (totalBytes == len) break;
				}
				if (totalBytes != len)
					if (debug) System.err.println("WARNING: File size unequal to "
							+"specified content length!"); 
			} else { // length is unknown, aggregate:
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				array = new byte[1024];
				int bytesRead;
				while ((bytesRead = sis.read(array)) != -1) {
					baos.write(array, 0, bytesRead);
				}
				array = baos.toByteArray();
			}

			ChannelMap cmp = new ChannelMap();
			// Check to see if a zerofile has been registered for this
			//  channel.  If so, delete it to make way for the data.
			ChannelTree.Node node = conn.ctree.findNode(conn.reqParam.path);
			if (node != null && node.getSize() == 0 
					&& ZEROFILE_MIME.equals(node.getMime())) {
				cmp.Add(conn.reqParam.name);
				src.Delete(cmp);
				cmp.Clear();
			} else if (node == null) {
				// 2005/11/01  WHF  If the resource does not presently exist
				//    we should indicate this fact:
				res.setStatus(res.SC_CREATED);
			}
			// Put the timestamp:				
			if (conn.reqParam.isTimeSet()) {
				// 2008/04/22  WHF  Handle case where duration was set by 
				//  client, but not time.  Use 'now'.
				if (conn.reqParam.start == 0.0)
					conn.reqParam.start = System.currentTimeMillis() * 1e-3;
				cmp.PutTime(conn.reqParam.start, conn.reqParam.duration);
			} else cmp.PutTimeAuto("timeofday");
						
			// 04/16/2003  WHF	Multiplex support:
			if (conn.reqParam.mux==1) {
				int index = cmp.Add(fileName);
				cmp.PutData(
					index,
					array,
					conn.reqParam.datatype,
					conn.reqParam.byteorder);
					
				cmp.PutMime(index, mime);
			} else	{ // multichannel mux:
				//ChannelMap conn.cm=new ChannelMap();

				int wordSize=conn.reqParam.getWordSize();
				int repeatSize=wordSize*conn.reqParam.blockSize;
				int totalSize=array.length/conn.reqParam.mux;
				for (int ii=0; ii<conn.reqParam.mux; ++ii) {
					// Create size information:
					/*
					int index=conn.cm.Add(name+ii);
					conn.cm.PutMime(index, "text/xml");
					conn.cm.PutDataAsString(index, new String("<rbnb><size>"
						+totalSize+"</size></rbnb>")); */

					int index = cmp.Add(fileName+ii);

					// Note that multiple arrays must be created.  This is
					//	because the ChannelMap does not copy the data.
					byte [] temp=new byte[totalSize];
					for (int iii=0; iii<totalSize/repeatSize; ++iii)
						System.arraycopy(
								array,	 // src 
								iii*repeatSize*conn.reqParam.mux
									+ii*repeatSize,	 // src offset
								temp,	// dest 
								iii*repeatSize,			// dest offset
								repeatSize);	// len

					cmp.PutData(
							index,
							temp,
							conn.reqParam.datatype,
							conn.reqParam.byteorder);
					cmp.PutMime(index, mime);
				}
			}
if (debug) System.err.println("Putting:\n"+cmp);
			src.Flush(cmp);
			
		} catch (SAPIException se) { throw new ServletException(se); }
		finally { recycleConnection(conn); }
	}
		
	/**
	*  Handles the Http GET operation.
	*
	*/	
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{		
		Connection conn = null;
		try {
			conn = connect(req);

			ChannelTree.Node node = conn.ctree.findNode(conn.reqParam.path);
if (debug) System.err.println(node);

			long ifModSince = -1;
			try {
				ifModSince = req.getDateHeader("If-Modified-Since");
			} catch (Throwable t) { }

			if (node == null){
				res.sendError(
						HttpServletResponse.SC_NOT_FOUND,
						conn.reqParam.path);
				return;
			}	

			//**********************************
			// GET a file:
			if (isChannel(node)) {
				getFile(ifModSince, node, res, conn);				
			} else { 
				if (!conn.reqParam.pathHadTrailingSlash)
					res.sendRedirect(conn.reqParam.path+'/');
				else
					printDirectoryListing(node, res, conn);
			}
		} catch(SAPIException sapiE){
			throw new ServletException(sapiE);
		}
		finally { recycleConnection(conn); }
	}
	
	//--------------------------------------------------------
	/**	 
	*  Passes to doGet(req,res)
	*/
	protected void doPost(
			HttpServletRequest req,
			HttpServletResponse res)
		throws ServletException, IOException
	{
		doGet(req,res);
	}
	
	/**
	  * WebDAV COPY.
	  */
	protected void doCopy(
			HttpServletRequest req,
			HttpServletResponse res)
		throws ServletException, IOException
	{
		copy(req, res, false);
	}
	
	/**
	  * WebDAV MOVE.
	  */
	protected void doMove(
			HttpServletRequest req,
			HttpServletResponse res)
		throws ServletException, IOException
	{
		copy(req, res, true);
	}
	
	/**
	  * Copy/move utility function.
	  */
	private void copy(
			HttpServletRequest req,
			HttpServletResponse res,
			boolean deleteSource)
		throws ServletException, IOException
	{
		Connection conn = null;
		try {
			conn = connect(req);
		
			ChannelTree.Node node = conn.ctree.findNode(conn.reqParam.path);
			if (node == null){
				res.sendError(
						HttpServletResponse.SC_NOT_FOUND,
						conn.reqParam.path);
				return;
			}
			
			// Check the destination:
			if(conn.destSourceName == null) {
				res.sendError(res.SC_CONFLICT);
				return;
			}
			
			if (isChannel(node)) {
				// Get the data:
				ChannelMap data = fetch(conn);
				
				// Copy the data into an output map:
				ChannelMap output = new ChannelMap();
				int outI = output.Add(conn.destChannel);
				output.PutTimeRef(data, 0);
				output.PutDataRef(outI, data, 0);
				
				// Put the data:
				conn.destSource.Flush(output);
				if (deleteSource)
					conn.destSource.Delete(data);
			} else if (node.getType() == ChannelTree.SOURCE) {
				if (conn.destSource != null) // already exists
					res.sendError(res.SC_CONFLICT);
				else {
					Source original = (Source) sourcesHT.get(node.getName());
					Source src = new Source(
							original.GetCacheSize(),
							original.GetArchiveMode(),
							original.GetArchiveSize()
					);
					src.OpenRBNBConnection(
							defaultProperties.get("hostname").toString(),
							conn.destSourceName,
							defaultProperties.get("username").toString(),
							defaultProperties.get("password").toString()
					);
					// We store the source under its requested name, not
					//  its actual name.
					sourcesHT.put(conn.destSourceName, src);
					if (deleteSource)
						original.CloseRBNBConnection();
				}
						
			} else res.sendError(res.SC_NOT_IMPLEMENTED);
		} catch(SAPIException sapiE){
			throw new ServletException(sapiE);
		}
		finally { recycleConnection(conn); }
	}		
	
	private void parsePlugInOptionStr(String options, ChannelMap cmap)
	{
//System.err.println("Parsing: "+options);		
		// The option string is of the form:
		// plug-in-name,key1=value1,key2=value2,...,keyn=valuen
		try {
			int comma = options.indexOf(','), nextComma;
			if (comma < 1) return; // no channel
			String chan = options.substring(0, comma);
			while (comma != options.length()
					&& ((nextComma = options.indexOf(',', comma+1)) > 0
					|| (nextComma = options.length()) != 0)) {
				int eq = options.indexOf('=', comma+1);
//System.err.println(chan+": "+options.substring(comma+1, eq)+" = "+options.substring(eq+1, nextComma));
				
				if (eq < nextComma)
					cmap.AddPlugInOption(
							chan,
							options.substring(comma+1, eq),
							options.substring(eq+1, nextComma)
					);
				comma = nextComma;
			}
		} catch (Exception e) {
			System.err.println("Error parsing option string: \""+options+"\":");
			e.printStackTrace();
		}
	}
	
	private ChannelMap fetch(Connection conn) throws SAPIException
	{
		//create new channelmap and add current channel to it, using
		//directory structure as channel
		ChannelMap c = new ChannelMap();
		int idx = c.Add(conn.reqParam.source + '/' + conn.reqParam.name);

		// Add PlugIn request message, if any:				
		if (conn.reqParam.requestData != null) {
			for (int ii=0; ii < conn.reqParam.requestData.length; ++ii) {
				// We cannot use zero length strings:
				if (conn.reqParam.requestData[ii].length()>0)
					c.PutDataAsString(idx, conn.reqParam.requestData[ii]);
			}
		}
		
		// 2007/04/20  WHF  Add PlugInOptions, if any:
		String[] opts = conn.reqParam.getPlugInOptions();
		for (int ii = 0; opts!=null && ii < opts.length; ++ii)
			parsePlugInOptionStr(opts[ii], c);
		
		
		conn.sink.Request(c, 
				conn.reqParam.start,
				conn.reqParam.duration,
				conn.reqParam.reference
		);
		conn.sink.Fetch(60000,c);
		
		return c;
	}
	
	/**
	  * doGet() utility function.
	  */
	private void getFile(
			long ifModSince,
			ChannelTree.Node node,
			HttpServletResponse res,
			Connection conn)
		throws IOException, SAPIException
	{
		setNoCache(res);

		double start, dur;
		String ref;
		
		start = conn.reqParam.start;
		dur = conn.reqParam.duration;
		ref = conn.reqParam.reference;
		
		// Absolute time of the munged request:
		long requestDate;
		if ("absolute".equals(ref)) {
			requestDate = (long) ((start + dur)*1e3);
		} else if ("oldest".equals(ref)) {
			requestDate = (long) ((node.getStart() + start + dur) * 1e3);
		} else if ("newest".equals(ref)) {
			requestDate = (long) ((node.getStart() + node.getDuration() 
					+ start + dur) * 1e3);
		} else {
			// Some other silly reference among the plethora.  Request date is 
			//  unknown.
			requestDate = -10000;
		}
if (debug) System.err.println("If-Modified-Since: "+ifModSince
+"\nLast-Modified (reg): "+requestDate);
		 
		// 2006/02/21  WHF  Check to see if ifModSince is included:
		if (ifModSince > 0 && requestDate/1000 == ifModSince/1000) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		
		ChannelMap c = fetch(conn);
		
		if (c.GetIfFetchTimedOut()) {
			res.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			return;
		}
		if (c.NumberOfChannels() == 0) {
			if (node.getSize() == 0) {
				// Empty file anyway
				res.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else { // no matching data
				res.sendError(HttpServletResponse.SC_GONE);
			}
			return;
		}
		
		if(debug){
			java.io.PrintStream out = System.err;
			out.println("<hr>");
			out.println("Url: "+conn.reqParam.path+"<br>");
			out.println("Current Dir: "+conn.reqParam.path+"<br>");
			out.println("Start: "+start+"<br>");
			out.println("Dur: "+dur+"<br>");
			out.println("Ref: "+ref+"<br>");
			out.println("<hr>");
		}
		
		//Check to see what URL request wants us to do
		int fetchtype = conn.reqParam.fetchtype;			   
		
		//-------------
		// Data		
		if(fetchtype == conn.reqParam.FETCH_DATA){
			if(conn.reqParam.datatype==ChannelMap.TYPE_STRING) {
				PrintWriter out = res.getWriter();
				res.setContentType("text/plain");
				out.println(convertToString(c));
			} else {
				OutputStream os = res.getOutputStream();
				
				//Adding last modified header
				String name = "Last-Modified";
				requestDate = (long) 
						((c.GetTimeStart(0)+c.GetTimeDuration(0))*1e3);
				res.setDateHeader(name, requestDate);
				
				//Adding duration header
				name = "X-Duration";
				res.setHeader(name, String.valueOf(c.GetTimeDuration(0)));
				
				String mime = c.GetMime(0);				 
				res.setContentType(mime);			   

if (debug) System.err.println("Last-Modified (data): "+requestDate);
				// We div 1000 because clients seem to only have second res.
				// 2006/02/27  WHF  Only use if ifModSince included:
				if (ifModSince > 0 && requestDate/1000 == ifModSince/1000) {
					res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					res.setContentLength(0);
				} else {
					byte arr[] = c.GetData(0);
					// 2007/06/01  WHF  arr can be null with PlugIns.
					if (arr == null) arr = new byte[0];   					
					res.setContentLength(arr.length);
					
					if(conn.reqParam.byteorder == ChannelMap.LSB) {
						arr = byteSwap(c.GetType(0),arr);
						os.write(arr);							
					} else {
						os.write(arr);
					}
				}
			}
		}
		
		//-------------
		// Datatype
		else if(fetchtype == conn.reqParam.FETCH_DATATYPE) {
			int x = c.GetType(0);//int
			String datatype = c.TypeName(x);			
			
			// 2005/10/07  WHF  Always prints dataype as a string.
			//    If a binary code is needed, check conn.reqParam.datatype
			//    and cast x.
			PrintWriter out = res.getWriter();
			res.setContentType("text/plain");
			out.println(datatype);
		}	 
		
		//-------------
		// Info
		else if(fetchtype == conn.reqParam.FETCH_INFO) {
			String info = c.GetUserInfo(0); //string
			PrintWriter out = res.getWriter();							
			res.setContentType("text/plain");
			out.println(info);
		}
		
		//-------------
		// Mime
		else if(fetchtype == conn.reqParam.FETCH_MIME){
			String mime = c.GetMime(0); //string
			PrintWriter out = res.getWriter();
			res.setContentType("text/plain");
			out.println(mime);
		}
		
		//-------------
		// Size
		else if(fetchtype == conn.reqParam.FETCH_SIZE){
			byte arr[] = c.GetData(0);
			if (conn.reqParam.datatype==ChannelMap.TYPE_STRING){
				PrintWriter out = res.getWriter();
				res.setContentType("text/plain");
				out.println(arr.length);
			} else {
				res.getOutputStream().write(arr);
			}
		}
		
		//-------------
		// Time
		else if(fetchtype == conn.reqParam.FETCH_TIMES) {
			double[] d = c.GetTimes(0); //double[]
			
			if(conn.reqParam.datatype==ChannelMap.TYPE_STRING){
				PrintWriter out = res.getWriter();
				res.setContentType("text/html");
				for(int i=0;i<d.length;i++)
					out.println(d[i]);
			} else {
				byte b[] = com.rbnb.utility.ByteConvert.double2Byte(d);
				OutputStream os = res.getOutputStream();
				res.setContentType("application/octet-stream");
				res.setContentLength(b.length);
				os.write(b);
			}
		}
	} // end getFile()

	/**
	  * Prints the HTML listing obtained with GET on a folder.
	  */
	private void printDirectoryListing(
			ChannelTree.Node node,
			HttpServletResponse res,
			Connection conn)
		throws IOException
	{
		//Checking to see if directory exists
		if(node == null) {
			res.sendError(
					HttpServletResponse.SC_NOT_FOUND,
					conn.reqParam.path);
			return;
		}
		
		Iterator iter = node.getChildren().iterator();

		setNoCache(res);		
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		
		//Build html
		out.println("<html><head><title>Directory listing for: "
				+conn.reqParam.path+"</title></head>");
		
		out.println("<a href=\"../\"><tt>[Up one level]</tt></a>");
		out.println("<h3 bgcolor=\"0000ff\">Directory listing for: "+conn.reqParam.path+"</h3>");
		out.println("<hr>");
		out.println("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">");
		out.println("<tr>");
		out.println("<td align=\"left\"><font size=\"+1\"><strong>Filename</strong></font></td>");
		out.println("<td align=\"right\"><font size=\"+1\"><strong>Size</strong></font></td>");
		out.println("<td align=\"right\"><font size=\"+1\"><strong>Last Modified</strong></font></td>");
		out.println("</tr>");
		
		int toggle = 0;
		while (iter.hasNext()) {
			//each node becomes link name
			ChannelTree.Node ctn = (ChannelTree.Node)iter.next();
			if (ctn.getType() == ChannelTree.SINK 
					|| ctn.getType() == ChannelTree.CONTROLLER)
				continue;

			String temp = ctn.getName();
				
			//building table
			if(toggle == 0)
				out.println("<tr>");
			else
				out.println("<tr bgcolor=\"eeeeee\">");
			out.println("<td align=\"left\">&nbsp;&nbsp;");
			
			//get size
			int size = ctn.getSize();
			String pSize="";
			
			if (size < 1024)
				pSize = size + " bytes";
			else if (size < 1048576)
				pSize = numberFormatter.format(size/1024.0) + " kb";
			else
				pSize = numberFormatter.format(size/1048576.0) + " Mb";
			
			if(size == -1 || size == 0 && COLLECTION_MIME.equals(ctn.getMime()))
			{
				// Subdirectory, print with slash:
				out.println("<a href=\""+temp+"/\"><tt>"+temp+"/</tt></a></td>");							
				out.println("<td align=\"right\"><tt> -</tt></td>");
			} else {
				out.println("<a href=\""+temp+"\"><tt>"+temp+"</tt></a></td>");
				out.println("<td align=\"right\"><tt>"+pSize+"</tt></td>");
			}
			
			//get last time modified
			out.println("<td align=\"right\"><tt>"+lastModified(ctn)+"</tt></td>");
			out.println("</tr>");
			toggle ^= 1;
		}
		out.println("</table>");
		out.println("<HR size=\"1\" noshade><h3>RBNB WebDAV 2.0</h3></body>");
		out.println("</html>");
		out.close();		
	}
	
	private void setNoCache(HttpServletResponse res)
	{
		res.addHeader("Pragma", "no-cache");
		res.addHeader("Cache-Control", "no-cache");
		res.addIntHeader("Expires", -1); 
	}		
	
	//--------------------------------------------------------
	/**	 
	*  Format the date for last modified time on a channel tree node.
	*  For nodes with children, this will look one level deep.
	*/
	private Date lastModified(ChannelTree.Node ctn)
	{
		List list = ctn.getChildren();
		Date date;
		
		if(list.isEmpty())	//no children
		{
			long l = (long) ((ctn.getStart() + ctn.getDuration())*1e3);
			if(l <= 0)
				date = new Date();
			else
				date = new Date (l);
		}
		else
		{
			//Check all children's modification times
			long last = 0;
			for(int i=0;i<list.size();i++) {
				ChannelTree.Node ct = (ChannelTree.Node)list.get(i);
				/*Double s = new Double(ct.getStart());
				Double d = new Double(ct.getDuration());
				long l = (s.longValue()+d.longValue()); */

				long l = (long) ((ct.getStart() + ct.getDuration())*1e3);
				if(l>last)
					last = l;
			}
			
			date = new Date(last);
		}
			
		return date;
	}
	
	private void registerNullChannels(Source source, Connection conn)
		throws SAPIException
	{
		ChannelMap sourceMap = new ChannelMap();
		if (conn.reqParam.mux==1) {
			int index=sourceMap.Add(conn.reqParam.name);
			sourceMap.PutMime(index, "text/xml");
			sourceMap.PutDataAsString(
					index,
					"<rbnb><size>0</size><mime>"
					+ZEROFILE_MIME+"</mime></rbnb>");
		} else {  // multichannel
			for (int ii=0; ii < conn.reqParam.mux; ++ii) {
				int index=sourceMap.Add(conn.reqParam.name+ii);
				sourceMap.PutMime(index, "text/xml");
				sourceMap.PutDataAsString(
						index,
						"<rbnb><size>0</size><mime>"
						+ZEROFILE_MIME+"</mime></rbnb>");
			}
		}
if (debug) System.err.println("registering null channel(s): "+sourceMap);
		source.Register(sourceMap);
	}
	
	//-------------------------------------------------------
	/**	 
	*  Provides an easy way to create up one level link
	*  Takes "123.net/a/b/" returns "123.net/a/"
	*/	
	private String upLink(String str)
	{
		return str.substring(0, str.lastIndexOf('/', str.length() - 2));
	}
	
	//--------------------------------------------------------
	/**	 
	*  Connects sink to localhost:3333
	*/	  
	private Connection connect(HttpServletRequest req) 
			throws SAPIException, IOException
	{
		Connection conn;
		
		// Get existing connection, if available:
		synchronized (connectionStack) {
			if (connectionStack.empty()) {
				// Create new:
				conn = new Connection(defaultProperties);
			} else {
				conn = (Connection) connectionStack.pop();
			}
		}

		String postQuery;		
		if ("application/x-www-form-urlencoded".equals(req.getContentType())) {
			// We have a posted form.  The body will be in the form of a 
			//  query string.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ServletInputStream is = req.getInputStream();
			int ch;
			while ((ch = is.read()) >= 0) baos.write(ch);
			postQuery = new String(baos.toByteArray()).trim();
		} else postQuery = null;
		// We could potentially support other POST content types.  I would
		//  suggest manadating that the first string in the array be for
		//  the query parameters, the next string be for the POST body,
		//  and the MIME type of the request be equal to the content type.
		
		conn.reqParam.parseQueryString(
			req.getRequestURI(),
			req.getQueryString(),
			postQuery
		);
		
		if (!conn.sink.VerifyConnection()) {
			conn.sink.CloseRBNBConnection();
			conn.sink.OpenRBNBConnection(
					defaultProperties.get("hostname").toString(),
					defaultProperties.get("sinkname").toString(),
					defaultProperties.get("username").toString(),
					defaultProperties.get("password").toString()
			);
		}
		//retrieve channel listing
		conn.sink.RequestRegistration();
		conn.sink.Fetch(60000, conn.cm);

		/*
		  * Checks to see if the path matches a server or plugin.  If so,
		  *  passes a registration request along for the whole path.
		  */
		conn.ctree = ChannelTree.createFromChannelMap(
				conn.cm,
				conn.reqParam.servlet);
				
		// Check for destination header field in the case of Copy/Move:
		String dest = req.getHeader("destination");
		conn.destSource = null;
		conn.destSourceName = conn.destChannel = null;
		if (dest != null) {
			java.net.URL destUrl = new java.net.URL(dest);
			// Ex: http://localhost/RBNB/source/channel
			// getPath() -> /RBNB/source/channel
			String path = destUrl.getPath();
			if (path.startsWith(conn.reqParam.servlet, 1)) {
				// Remove /RBNB/
				path = path.substring(
						conn.reqParam.servlet.length() + 2);
				// 2007/05/01  WHF  Might be URL encoded:
				try {
				path = URLDecoder.decode(path, "UTF-8");
				} catch (Throwable t) {}
						
				String srcStr;
				int slash = path.indexOf('/');
				if (slash == -1) {
					srcStr = path;
					path = "";
				} else {
					srcStr = path.substring(0, slash);
					path = path.substring(slash + 1);
				}
				conn.destSourceName = srcStr;
				conn.destSource = (Source) sourcesHT.get(srcStr);
				conn.destChannel = path;
			}
		}
				
		ChannelTree.Node theSource = (ChannelTree.Node) conn.ctree.findNode(
				"/"+conn.reqParam.servlet).getChildrenMap().get(
				conn.reqParam.source);
		if (theSource == null || theSource.getType()!=ChannelTree.SERVER
				&& theSource.getType()!=ChannelTree.PLUGIN)
			return conn; // No further processing needed.

		conn.cm.Clear();
		String toAdd;
		if (conn.reqParam.pathHadTrailingSlash) { // wants a directory listing
			toAdd = "/*";
		} else {
			toAdd = "";
		}
		if (conn.reqParam.name.length() == 0)
			conn.cm.Add(conn.reqParam.source+toAdd);
		else
			conn.cm.Add(conn.reqParam.source+'/'+conn.reqParam.name+toAdd);
		
if (debug) System.err.println("Requesting: "+conn.cm);		
		conn.sink.RequestRegistration(conn.cm);
		conn.sink.Fetch(60000, conn.cm);
if (debug) System.err.println("Got: "+conn.cm);		

		ChannelTree pluginTree = ChannelTree.createFromChannelMap(
				conn.cm, conn.reqParam.servlet);			
		conn.ctree = conn.ctree.merge(pluginTree);
				
		return conn;
	}
	
	/**
	  * Return unused connections to the pool.
	  */
	private void recycleConnection(Connection conn)
	{
		if (conn == null) return;   // we do this check to facilitate the use
									// of finally with connection objects.
				
		// The boolean is used to minimize the stack lock duration.
		boolean doClose = false;
		
		synchronized (connectionStack) {
			if (connectionStack.size() < MAX_POOL_SIZE) {
				connectionStack.push(conn);
			} else {
				doClose = true;
			}
		}

		if (doClose) {
			conn.sink.CloseRBNBConnection();
		}
	}
	
	//--------------------------------------------------------
	// 2005/??/??  JPB  Created.
	// 2005/12/19  WHF  Fixed String handling so as to expand string arrays.
	/**	 
	*  Converts an array of bytes to a string based on the datatype passed
	* to the function.
	*/			
	
	private String convertToString(ChannelMap cm)
	{
		StringBuffer result = new StringBuffer();
		
		switch (cm.GetType(0)) {
			case (ChannelMap.TYPE_STRING):
			//already string, just copy it
			String[] sa = cm.GetDataAsString(0);
			for (int ii = 0; ii < sa.length; ++ii)
				result.append(sa[ii]);
			break;
			case (ChannelMap.TYPE_INT8):
			byte[] datab=cm.GetDataAsInt8(0);
			for (int j=0;j<datab.length;j++) {
				result.append(Byte.toString(datab[j]));
				result.append('\n');
			}
			break;
			case (ChannelMap.TYPE_INT16):
			short[] datas=cm.GetDataAsInt16(0);
			for (int j=0;j<datas.length;j++) {
				result.append(Short.toString(datas[j]));
				result.append('\n');
			}
			break;
			case (ChannelMap.TYPE_INT32):
			int[] datai=cm.GetDataAsInt32(0);
			for (int j=0;j<datai.length;j++) {
				result.append(Integer.toString(datai[j]));
				result.append('\n');
			}
			break;
			case (ChannelMap.TYPE_INT64):
			long[] datal=cm.GetDataAsInt64(0);
			for (int j=0;j<datal.length;j++) {
				result.append(Long.toString(datal[j]));
				result.append('\n');
			}
			break;
			case (ChannelMap.TYPE_FLOAT32):
			float[] dataf=cm.GetDataAsFloat32(0);
			for (int j=0;j<dataf.length;j++) {
				result.append(Float.toString(dataf[j]));
				result.append('\n');
			}
			break;
			case (ChannelMap.TYPE_FLOAT64):
			double[] datad=cm.GetDataAsFloat64(0);
			for (int j=0;j<datad.length;j++) {
				result.append(Double.toString(datad[j]));
				result.append('\n');
			}
			break;
			default:
			// Conversion is not done for this type:
			return "";//cm.GetData(0);
		} //end switch(type)
		
		return result.toString();
	}		
	
	//--------------------------------------------------------
	/**	 
	*  Converts array of bytes to LSB based on the size of the type 
	* retrieved from the ChannelMap.
	*
	* 2005/??/??  JBP  Original implementation.
	* 2005/12/20  WHF  Reimplemented with considerable speed improvement.
	*/
	/*
	private byte[] byteSwap(ChannelMap cm, byte[]oldArray)
	{
		byte[] newArray = new byte[oldArray.length];
		
		if(cm.GetType(0)==ChannelMap.TYPE_FLOAT64 ||
				cm.GetType(0)==ChannelMap.TYPE_INT64){
			
			for(int i=0;i<oldArray.length-1;i++)
				for(int j=0;j<8;j++)
					newArray[(i*8)+j]=oldArray[(i*8)+7-j];			
		}
		
		else if(cm.GetType(0)==ChannelMap.TYPE_FLOAT32 ||
				cm.GetType(0)==ChannelMap.TYPE_INT32){
			
			for(int i=0;i<oldArray.length/4;i++)
				for(int j=0;j<4;j++)
					newArray[(i*4)+j]=oldArray[(i*4)+3-j];	
		}					 
		
		else if(cm.GetType(0)==ChannelMap.TYPE_INT16){
			
			for(int i=0;i<oldArray.length/2;i++)
				for(int j=0;j<2;j++)
					newArray[(i*2)+j]=oldArray[(i*2)+1-j];
		}
		
		else //type unknown, return old array.
			return oldArray;	  
		
		return newArray;
	} */
	private static byte[] byteSwap(int type, byte[] array)
	{
		int ii;
		byte swap;
		
		switch (type) {
		case ChannelMap.TYPE_INT16:
			for (ii = 0; ii < array.length; ii+=2) {
				swap = array[ii];
				array[ii] = array[ii+1];
				array[ii+1] = swap;
			}
			break;
			
		case ChannelMap.TYPE_INT32:
		case ChannelMap.TYPE_FLOAT32:
			for (ii = 0; ii < array.length; ii+=4) {
				swap = array[ii];
				array[ii] = array[ii+3];
				array[ii+3] = swap;
				swap = array[ii+1];
				array[ii+1] = array[ii+2];
				array[ii+2] = swap;
			}
			break;
			
		case ChannelMap.TYPE_INT64:
		case ChannelMap.TYPE_FLOAT64:
			for (ii = 0; ii < array.length; ii+=8) {
				swap = array[ii];
				array[ii] = array[ii+7];
				array[ii+7] = swap;
				swap = array[ii+1];
				array[ii+1] = array[ii+6];
				array[ii+6] = swap;
				swap = array[ii+2];
				array[ii+2] = array[ii+5];
				array[ii+5] = swap;
				swap = array[ii+3];
				array[ii+3] = array[ii+4];
				array[ii+4] = swap;
			} 
			/* Proved to be a slower implementation:
			for (ii = 0; ii < array.length; ii+=5) {
				iii = ii+7;
				swap = array[ii];
				array[ii] = array[iii];
				array[iii] = swap;
				swap = array[++ii];
				array[ii] = array[--iii];
				array[iii] = swap;
				swap = array[++ii];
				array[ii] = array[--iii];
				array[iii] = swap;
				swap = array[++ii];
				array[ii] = array[--iii];
				array[iii] = swap;
			} */

			break;

		}
		return array;
	}


	/**
	  * Utilty method to load parameters from the "env-entry" fields in the
	  *	  servlet web.xml file.
	  */
	private void checkEnv(
		InitialContext webEnv, 
		String propertyEnv,
		String propertyLocal,
		Object _default)
	{
		try {
		Object result=webEnv.lookup(propertyEnv);
		defaultProperties.put(propertyLocal, result);
		return;
		} catch (NamingException ne) { }  // eat exception
		defaultProperties.put(propertyLocal, _default);
	}
	
	/**
	 * Return JAXP document builder instance.
	 */
	static DocumentBuilder getDocumentBuilder()
		throws ServletException {
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		try {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new ServletException("webdavservlet.jaxpfailed", e);
		}
		return documentBuilder;
	}
		
	/**
	 * Propfind helper method.
	 *
	 * @param node Resource for which properties are to be parsed
	 * @param generatedXML XML response to the Propfind request
	 * @param path Relative path of the current node in web-land
	 * @param type Propfind type
	 * @param propertiesArrayList If the propfind type is find properties by
	 * name, then this ArrayList contains those properties
	 */
	private void parseProperties(HttpServletRequest req,
								 ChannelTree.Node node,
								 XMLWriter generatedXML,
								 int type,
								 List propertiesArrayList)
	{
		generatedXML.writeElement(DN_ALIAS, "response", XMLWriter.OPENING);
		String status = new String("HTTP/1.1 " + WebdavStatus.SC_OK + " Ok");

		// Generating href element
		generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);

		String href = node.getFullName();

		String encoded = urlEncode(href);
		generatedXML.writeText(encoded);
		
		generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.CLOSING);

		String resourceName = node.getName();

		switch (type) {

		case FIND_ALL_PROP :

			generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
			generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

			generatedXML.writeProperty(DN_ALIAS, "creationdate",
					 getISOCreationDate((long) (node.getStart()*1e3)));
			generatedXML.writeElement(DN_ALIAS, "displayname", XMLWriter.OPENING);
			generatedXML.writeData(resourceName);
			generatedXML.writeElement(DN_ALIAS, "displayname", XMLWriter.CLOSING);
			generatedXML.writeProperty(DN_ALIAS, "getcontentlanguage",
					Locale.getDefault().toString());
			if (isChannel(node)) {
				generatedXML.writeProperty(
						DN_ALIAS, "getlastmodified",
						WebDAVServlet.getHttpDate(lastModified(node))
				);
						
				generatedXML.writeProperty(
						DN_ALIAS, "getcontentlength",
						String.valueOf(node.getSize()));
				generatedXML.writeProperty(
						DN_ALIAS, "getcontenttype", node.getMime()==null?
						 getServletContext().getMimeType(node.getName())
						 :node.getMime());
				generatedXML.writeProperty(DN_ALIAS, "getetag",
							// jak 4.0.1: getETagValue(resourceInfo, true));
							getETag(node));
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

			lockParser.generateLockDiscovery(href, generatedXML);

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
			if (isChannel(node)) {
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

			boolean isChan = isChannel(node);
			java.util.ArrayList propertiesNotFound = new java.util.ArrayList();

			// Parse the list of properties

			generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
			generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

			Iterator properties = propertiesArrayList.iterator();

			while (properties.hasNext()) {

				String property = (String) properties.next();

				if (property.equals("creationdate")) {
					generatedXML.writeProperty
						(DN_ALIAS, "creationdate",
						 getISOCreationDate((long)node.getStart()));
				} else if (property.equals("displayname")) {
					generatedXML.writeElement
						(DN_ALIAS, "displayname", XMLWriter.OPENING);
					generatedXML.writeData(resourceName);
					generatedXML.writeElement
						(DN_ALIAS, "displayname", XMLWriter.CLOSING);
				} else if (property.equals("getcontentlanguage")) {
					if (!isChan) {
						propertiesNotFound.add(property);
					} else {
						generatedXML.writeProperty
							(DN_ALIAS, "getcontentlanguage",
							 Locale.getDefault().toString());
					}
				} else if (property.equals("getcontentlength")) {
					if (!isChan) {
						propertiesNotFound.add(property);
					} else {
						generatedXML.writeProperty
							(DN_ALIAS, "getcontentlength",
							 (String.valueOf(node.getSize())));
					}
				} else if (property.equals("getcontenttype")) {
					if (!isChan) {
						propertiesNotFound.add(property);
					} else {
						generatedXML.writeProperty
							(DN_ALIAS, "getcontenttype",
							 getServletContext().getMimeType
							 (node.getName()));
					}
				} else if (property.equals("getetag")) {
					if (!isChan) {
						propertiesNotFound.add(property);
					} else {
						generatedXML.writeProperty
							(DN_ALIAS, "getetag",
							 // jak 4.0.1: getETagValue(resourceInfo, true));
							 getETag(node));
					}
				} else if (property.equals("getlastmodified")) {
					if (!isChan) {
						propertiesNotFound.add(property);
					} else {
						generatedXML.writeProperty(
								DN_ALIAS, 
								"getlastmodified",
								WebDAVServlet.getHttpDate(lastModified(node))
						);
					}
				} else if (property.equals("resourcetype")) {
					if (!isChan) {
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
					if (!lockParser.generateLockDiscovery(href, generatedXML))
						propertiesNotFound.add(property);
				} else {
					propertiesNotFound.add(property);
				}
			}

			generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
			generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
			generatedXML.writeText(status);
			generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
			generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

			Iterator propertiesNotFoundList = propertiesNotFound.iterator();

			if (propertiesNotFoundList.hasNext()) {

				status = new String("HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND
									+ " Not Found");

				generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
				generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

				while (propertiesNotFoundList.hasNext()) {
					generatedXML.writeElement
						(DN_ALIAS, (String) propertiesNotFoundList.next(),
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
	
	private String getETag(ChannelTree.Node node)
	{
//		return "W/\"" + node.getSize() + "-"
//				+ ((long) ((node.getStart()+node.getDuration())*1e3)) + "\"";
		return "" + node.getSize() + "-"
				+ ((long) ((node.getStart()+node.getDuration())*1e3));
	}

	/**
	 * Get creation date in ISO format.
	 */
	static String getISOCreationDate(long creationDate)
	{
		synchronized (creationDateFormat) {
			if (creationDate <= 0) { 
				// Replace with now
				return creationDateFormat.format(new Date());
			}
			return creationDateFormat.format(new Date(creationDate));
		}
	}

	static String getISOCreationDate(Date creationDate) 
	{
		synchronized (creationDateFormat) {
			return creationDateFormat.format(creationDate); 
		}
	}
	
	/**
	  * Get creation data in HTTP format.
	  */
	static String getHttpDate(Date date)
	{
		synchronized (httpDateFormat) {
			return httpDateFormat.format(date);
		}
	}

		
	/**
	  * Returns true if the specified node is a data bearing channel.
	  */
	static boolean isChannel(ChannelTree.Node node)
	{
		return node.getType() == ChannelTree.CHANNEL
				&& !COLLECTION_MIME.equals(node.getMime());
	}
	
	/**
	 * Check to see if a resource is currently write locked. The method
	 * will look at the "If" header to make sure the client
	 * has give the appropriate lock tokens.
	 *
	 * @param req Servlet request
	 * @return boolean true if the resource is locked (and no appropriate
	 * lock token has been found for at least one of the non-shared locks which
	 * are present on the resource).
	 */
	private boolean isLocked(HttpServletRequest req, String path)
	{
		//String path = getRelativePath(req);
		// connect(req); should already be connected

		String ifHeader = req.getHeader("If");
		if (ifHeader == null)
			ifHeader = "";

		String lockTokenHeader = req.getHeader("Lock-Token");
		if (lockTokenHeader == null)
			lockTokenHeader = "";

		return lockParser.isLocked(path, ifHeader + lockTokenHeader);
	}
	
	static String urlEncode(String toEncode)
	{
		// RBNB channels will never contain '*', so we use it to trick out
		//  the URLEncoder class.
		// 2007/05/01  WHF  Repaired to better handle spaces.
		toEncode = toEncode.replaceAll("/", "*1");
		toEncode = toEncode.replaceAll(" ", "*2");
		
		try { toEncode = URLEncoder.encode(toEncode, "UTF-8"); 
		} catch (java.io.UnsupportedEncodingException uee) { } // never happen
		
		toEncode = toEncode.replaceAll("\\*1", "/");
		toEncode = toEncode.replaceAll("\\*2", "%20");
		
		return toEncode;
	}
	
	/**
	 * HTTP date format.
	 */
	private static final SimpleDateFormat httpDateFormat = 
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

	/**
	  * Must be in GMT or the Microsoft client fails.
	  */
	static {
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    private static final SimpleDateFormat creationDateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

//***************************  Data Members	 ********************************//
	
	/** True turns on debug statements (written to html page) */ 
	private final boolean debug; 

	/**
	  * If true, servlet cannot modify data.
	  */
	private boolean readOnly = false;
	
	/** Holds list of sources on RBNB server */
	private final HashMap sourcesHT = new HashMap(),
			defaultProperties = new HashMap();
			
	private final Stack connectionStack = new Stack();
			
	private static final NumberFormat numberFormatter 
			= NumberFormat.getInstance();
	static {
		numberFormatter.setMaximumFractionDigits(2);
		numberFormatter.setMinimumFractionDigits(2);
	}
						
	/**
	  * Contains the list of protected sources.
	  */
	private static final HashSet protectSet = new HashSet();
	
	/**
	  * Handles all locks.
	  */
	private static final LockParser lockParser = new LockParser();
	
//*****************************  Inner Classes  *****************************//
	private static class Connection
	{
		final Sink sink = new Sink();
		final ChannelMap cm = new ChannelMap();
		/** Class used to parse URL munging string */
		final RequestParameters2 reqParam;
		
		ChannelTree ctree;
		
		/**
		  * Destination for MOVE/COPY commands.
		  */
		Source destSource;
		String destChannel, destSourceName;
		
		Connection(Map defprop)
		{
			reqParam = new RequestParameters2(defprop);
		}
	}
		
}

class LockParser implements PropertyConstants
{
	private final boolean debug = false;

	/**
	  * Handles resource locking.
	  */
	public synchronized void doLock(
			HttpServletRequest req,
			HttpServletResponse resp,
			String path, ChannelTree ctree)
		throws ServletException, IOException, SAPIException
	{
		LockInfo lock = new LockInfo();

		// Parsing lock request

		// Parsing depth header

		String depthStr = req.getHeader("Depth");

		if (depthStr == null) {
			lock.depth = INFINITY;
		} else {
			if (depthStr.equals("0")) {
				lock.depth = 0;
			} else {
				lock.depth = INFINITY;
			}
		}

		// Parsing timeout header

		int lockDuration = DEFAULT_TIMEOUT;
		String lockDurationStr = req.getHeader("Timeout");
		if (lockDurationStr == null) {
			lockDuration = DEFAULT_TIMEOUT;
		} else {
			int commaPos = lockDurationStr.indexOf(",");
			// If multiple timeouts, just use the first
			if (commaPos != -1) {
				lockDurationStr = lockDurationStr.substring(0,commaPos);
			}
			if (lockDurationStr.startsWith("Second-")) {
				lockDuration =
					(new Integer(lockDurationStr.substring(7))).intValue();
			} else {
				if (lockDurationStr.equalsIgnoreCase("infinity")) {
					lockDuration = MAX_TIMEOUT;
				} else {
					try {
						lockDuration =
							(new Integer(lockDurationStr)).intValue();
					} catch (NumberFormatException e) {
						lockDuration = MAX_TIMEOUT;
					}
				}
			}
			if (lockDuration == 0) {
				lockDuration = DEFAULT_TIMEOUT;
			}
			if (lockDuration > MAX_TIMEOUT) {
				lockDuration = MAX_TIMEOUT;
			}
		}
		lock.expiresAt = System.currentTimeMillis() + (lockDuration * 1000);

		int lockRequestType = LOCK_CREATION;

		Node lockInfoNode = null;

		DocumentBuilder documentBuilder = WebDAVServlet.getDocumentBuilder();

		try {
			Document document = documentBuilder.parse(new InputSource
				(req.getInputStream()));

			// Get the root element of the document
			Element rootElement = document.getDocumentElement();
			lockInfoNode = rootElement;
		} catch(Exception e) {
			lockRequestType = LOCK_REFRESH;
		}

		if (lockInfoNode != null) {

			// Reading lock information

			NodeList childList = lockInfoNode.getChildNodes();
			StringWriter strWriter = null;
			DOMWriter domWriter = null;

			Node lockScopeNode = null;
			Node lockTypeNode = null;
			Node lockOwnerNode = null;

			for (int i=0; i < childList.getLength(); i++) {
				Node currentNode = childList.item(i);
				switch (currentNode.getNodeType()) {
				case Node.TEXT_NODE:
					break;
				case Node.ELEMENT_NODE:
					String nodeName = currentNode.getNodeName();
					if (nodeName.endsWith("lockscope")) {
						lockScopeNode = currentNode;
					}
					if (nodeName.endsWith("locktype")) {
						lockTypeNode = currentNode;
					}
					if (nodeName.endsWith("owner")) {
						lockOwnerNode = currentNode;
					}
					break;
				}
			}

			if (lockScopeNode != null) {

				childList = lockScopeNode.getChildNodes();
				for (int i=0; i < childList.getLength(); i++) {
					Node currentNode = childList.item(i);
					switch (currentNode.getNodeType()) {
					case Node.TEXT_NODE:
						break;
					case Node.ELEMENT_NODE:
						String tempScope = currentNode.getNodeName();
						if (tempScope.indexOf(':') != -1) {
							lock.scope = tempScope.substring
								(tempScope.indexOf(':') + 1);
						} else {
							lock.scope = tempScope;
						}
						break;
					}
				}

				if (lock.scope == null) {
					// Bad request
					resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
				}

			} else {
				// Bad request
				resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
			}

			if (lockTypeNode != null) {

				childList = lockTypeNode.getChildNodes();
				for (int i=0; i < childList.getLength(); i++) {
					Node currentNode = childList.item(i);
					switch (currentNode.getNodeType()) {
					case Node.TEXT_NODE:
						break;
					case Node.ELEMENT_NODE:
						String tempType = currentNode.getNodeName();
						if (tempType.indexOf(':') != -1) {
							lock.type =
								tempType.substring(tempType.indexOf(':') + 1);
						} else {
							lock.type = tempType;
						}
						break;
					}
				}

				if (lock.type == null) {
					// Bad request
					resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
				}

			} else {
				// Bad request
				resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
			}

			if (lockOwnerNode != null) {

				childList = lockOwnerNode.getChildNodes();
				for (int i=0; i < childList.getLength(); i++) {
					Node currentNode = childList.item(i);
					switch (currentNode.getNodeType()) {
					case Node.TEXT_NODE:
						lock.owner += currentNode.getNodeValue();
						break;
					case Node.ELEMENT_NODE:
						strWriter = new StringWriter();
						domWriter = new DOMWriter(strWriter, true);
						domWriter.setQualifiedNames(false);
						domWriter.print(currentNode);
						lock.owner += strWriter.toString();
						break;
					}
				}

				if (lock.owner == null) {
					// Bad request
					resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
				}

			} else {
				lock.owner = new String();
			}

		}

		lock.path = path;
		ChannelTree.Node node = ctree.findNode(lock.path);
		boolean exists = node != null;

		Iterator locksList = null;

		if (lockRequestType == LOCK_CREATION) {

			// Generating lock id
			String lockTokenStr = req.getServletPath() + "-" + lock.type + "-"
				+ lock.scope + "-" + req.getUserPrincipal() + "-"
				+ lock.depth + "-" + lock.owner + "-" + lock.tokens + "-"
				+ lock.expiresAt + "-" + System.currentTimeMillis() + "-"
				+ secret;
			String lockToken =
				md5Encoder.encode(md5Helper.digest(lockTokenStr.getBytes()));

			if (	(exists) 
					//&& (object instanceof DirContext)
					&& !WebDAVServlet.isChannel(node)
					&& (lock.depth == INFINITY) ) {

				// Locking a collection (and all its member resources)

				// Checking if a child resource of this collection is
				// already locked
				ArrayList lockPaths = new ArrayList();
				locksList = collectionLocks.iterator();
				while (locksList.hasNext()) {
					LockInfo currentLock = (LockInfo) locksList.next();
					if (currentLock.hasExpired()) {
						resourceLocks.remove(currentLock.path);
						continue;
					}
					if ( (currentLock.path.startsWith(lock.path)) &&
						 ((currentLock.isExclusive()) ||
						  (lock.isExclusive())) ) {
						// A child collection of this collection is locked
						lockPaths.add(currentLock.path);
					}
				}
				locksList = resourceLocks.values().iterator();
				while (locksList.hasNext()) {
					LockInfo currentLock = (LockInfo) locksList.next();
					if (currentLock.hasExpired()) {
						resourceLocks.remove(currentLock.path);
						continue;
					}
					if ( (currentLock.path.startsWith(lock.path)) &&
						 ((currentLock.isExclusive()) ||
						  (lock.isExclusive())) ) {
						// A child resource of this collection is locked
						lockPaths.add(currentLock.path);
					}
				}

				if (!lockPaths.isEmpty()) {

					// One of the child paths was locked
					// We generate a multistatus error report

					Iterator lockPathsList = lockPaths.iterator();

					resp.setStatus(WebdavStatus.SC_CONFLICT);

					XMLWriter generatedXML = new XMLWriter();
					generatedXML.writeXMLHeader();

					generatedXML.writeElement
						(null, "multistatus" + WebDAVServlet.NAMESPACE_DECL,
						 XMLWriter.OPENING);

					while (lockPathsList.hasNext()) {
						generatedXML.writeElement(null, "response",
												  XMLWriter.OPENING);
						generatedXML.writeElement(null, "href",
												  XMLWriter.OPENING);
						generatedXML
							.writeText((String) lockPathsList.next());
						generatedXML.writeElement(null, "href",
												  XMLWriter.CLOSING);
						generatedXML.writeElement(null, "status",
												  XMLWriter.OPENING);
						generatedXML.writeText(
								"HTTP/1.1 "+WebdavStatus.SC_LOCKED+" Locked");
						generatedXML.writeElement(null, "status",
												  XMLWriter.CLOSING);

						generatedXML.writeElement(null, "response",
												  XMLWriter.CLOSING);
					}

					generatedXML.writeElement(null, "multistatus",
										  XMLWriter.CLOSING);

					Writer writer = resp.getWriter();
					writer.write(generatedXML.toString());
					writer.close();

					return;

				}

				boolean addLock = true;

				// Checking if there is already a shared lock on this path
				locksList = collectionLocks.iterator();
				while (locksList.hasNext()) {

					LockInfo currentLock = (LockInfo) locksList.next();
					if (currentLock.path.equals(lock.path)) {

						if (currentLock.isExclusive()) {
							resp.sendError(WebdavStatus.SC_LOCKED);
							return;
						} else {
							if (lock.isExclusive()) {
								resp.sendError(WebdavStatus.SC_LOCKED);
								return;
							}
						}

						currentLock.tokens.add(lockToken);
						lock = currentLock;
						addLock = false;

					}

				}

				if (addLock) {
					lock.tokens.add(lockToken);
					collectionLocks.add(lock);
				}

			} else {

				// Locking a single resource

				// Retrieving an already existing lock on that resource
				LockInfo presentLock = (LockInfo) resourceLocks.get(lock.path);
				if (presentLock != null) {

					if ((presentLock.isExclusive()) || (lock.isExclusive())) {
						// If either lock is exclusive, the lock can't be
						// granted
						resp.sendError(WebdavStatus.SC_PRECONDITION_FAILED);
						return;
					} else {
						presentLock.tokens.add(lockToken);
						lock = presentLock;
					}

				} else {

					lock.tokens.add(lockToken);
					resourceLocks.put(lock.path, lock);

					// Checking if a resource exists at this path
					exists = true;
					/*try {
						object = resources.lookup(path);
					} catch (NamingException e) {
						exists = false;
					} */
					node = ctree.findNode(path);
					exists = node != null;
					if (!exists) {

						// "Creating" a lock-null resource
						int slash = lock.path.lastIndexOf('/');
						String parentPath = lock.path.substring(0, slash);

						ArrayList lockNulls =
							(ArrayList) lockNullResources.get(parentPath);
						if (lockNulls == null) {
							lockNulls = new ArrayList();
							lockNullResources.put(parentPath, lockNulls);
						}

						lockNulls.add(lock.path);

					}
					// Add the Lock-Token header as by RFC 2518 8.10.1
					// - only do this for newly created locks
					resp.addHeader("Lock-Token", "<opaquelocktoken:"
								   + lockToken + ">");
				}

			}

		}

		if (lockRequestType == LOCK_REFRESH) {

			String ifHeader = req.getHeader("If");
			if (ifHeader == null)
				ifHeader = "";

			// Checking resource locks

			LockInfo toRenew = (LockInfo) resourceLocks.get(path);
			Iterator tokenList = null;
			if (lock != null) {

				// At least one of the tokens of the locks must have been given

				tokenList = toRenew.tokens.iterator();
				while (tokenList.hasNext()) {
					String token = (String) tokenList.next();
					if (ifHeader.indexOf(token) != -1) {
						toRenew.expiresAt = lock.expiresAt;
						lock = toRenew;
					}
				}

			}

			// Checking inheritable collection locks

			Iterator collectionLocksList = collectionLocks.iterator();
			while (collectionLocksList.hasNext()) {
				toRenew = (LockInfo) collectionLocksList.next();
				if (path.equals(toRenew.path)) {

					tokenList = toRenew.tokens.iterator();
					while (tokenList.hasNext()) {
						String token = (String) tokenList.next();
						if (ifHeader.indexOf(token) != -1) {
							toRenew.expiresAt = lock.expiresAt;
							lock = toRenew;
						}
					}

				}
			}

		}

		// Set the status, then generate the XML response containing
		// the lock information
		XMLWriter generatedXML = new XMLWriter();
		generatedXML.writeXMLHeader();
		// 2005/11/02  WHF  Litmus failed to parse with null prefix here.
		//   Do other clients choke if a prefix is used?
		String thePrefix = DN_ALIAS;  // = null;
		generatedXML.writeElement(thePrefix, "prop"
								  + WebDAVServlet.NAMESPACE_DECL,
								  XMLWriter.OPENING);

		generatedXML.writeElement(thePrefix, "lockdiscovery",
								  XMLWriter.OPENING);

		lock.toXML(generatedXML);

		generatedXML.writeElement(thePrefix, "lockdiscovery",
								  XMLWriter.CLOSING);

		generatedXML.writeElement(thePrefix, "prop", XMLWriter.CLOSING);

		resp.setStatus(WebdavStatus.SC_OK);
		resp.setContentType("text/xml; charset=UTF-8");
		Writer writer = resp.getWriter();
		writer.write(generatedXML.toString());
		writer.close();
	}
	
		/**
	 * UNLOCK Method.
	 */
	synchronized public void doUnlock(
			HttpServletRequest req, HttpServletResponse resp, String path)
		throws ServletException, IOException, SAPIException
	{
		String lockTokenHeader = req.getHeader("Lock-Token");
		if (lockTokenHeader == null)
			lockTokenHeader = "";

		// Checking resource locks

		LockInfo lock = (LockInfo) resourceLocks.get(path);
		Iterator tokenList = null;
		if (lock != null) {

			// At least one of the tokens of the locks must have been given

			tokenList = lock.tokens.iterator();
			while (tokenList.hasNext()) {
				String token = (String) tokenList.next();
				if (lockTokenHeader.indexOf(token) != -1) {
					lock.tokens.remove(token);
					// Restart iterator:
					tokenList = lock.tokens.iterator();
				}
			}

			if (lock.tokens.isEmpty()) {
				resourceLocks.remove(path);
				// Removing any lock-null resource which would be present
				lockNullResources.remove(path);
			}

		}

		// Checking inheritable collection locks

		Iterator collectionLocksList = collectionLocks.iterator();
		while (collectionLocksList.hasNext()) {
			lock = (LockInfo) collectionLocksList.next();
			if (path.equals(lock.path)) {

				tokenList = lock.tokens.iterator();
				while (tokenList.hasNext()) {
					String token = (String) tokenList.next();
					if (lockTokenHeader.indexOf(token) != -1) {
						lock.tokens.remove(token);
						break;
					}
				}

				if (lock.tokens.isEmpty()) {
					collectionLocks.remove(lock);
					// Removing any lock-null resource which would be present
					lockNullResources.remove(path);
					// Restart iterator:
					collectionLocksList = collectionLocks.iterator();
				}
			}
		}

		resp.setStatus(WebdavStatus.SC_NO_CONTENT);
	}
	
	/**
	 * Print the lock discovery information associated with a path.
	 *
	 * @param path Path
	 * @param generatedXML XML data to which the locks info will be appended
	 * @return true if at least one lock was displayed
	 */
	public synchronized boolean generateLockDiscovery
		(String path, XMLWriter generatedXML)
	{
		LockInfo resourceLock = (LockInfo) resourceLocks.get(path);
		Iterator collectionLocksList = collectionLocks.iterator();
		boolean wroteStart = false;

		if (resourceLock != null) {
			wroteStart = true;
			generatedXML.writeElement(DN_ALIAS, "lockdiscovery",
									  XMLWriter.OPENING);
			resourceLock.toXML(generatedXML);
		}

		while (collectionLocksList.hasNext()) {
			LockInfo currentLock =
				(LockInfo) collectionLocksList.next();
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
	 * Check to see if a resource is currently write locked.
	 *
	 * @param path Path of the resource
	 * @param ifHeader "If" HTTP header which was included in the request
	 * @return boolean true if the resource is locked (and no appropriate
	 * lock token has been found for at least one of the non-shared locks which
	 * are present on the resource).
	 */
	public synchronized boolean isLocked(String path, String ifHeader)
	{
		// Checking resource locks

		LockInfo lock = (LockInfo) resourceLocks.get(path);
		Iterator tokenList = null;
		if ((lock != null) && (lock.hasExpired())) {
			resourceLocks.remove(path);
		} else if (lock != null) {

			// At least one of the tokens of the locks must have been given
if (debug) System.err.println("Checking locks.  If header = "+ifHeader);
			tokenList = lock.tokens.iterator();
			boolean tokenMatch = false;
			while (tokenList.hasNext()) {
				String token = (String) tokenList.next();
if (debug) System.err.println("\ttoken = "+token);				
				if (ifHeader.indexOf(token) != -1)
					tokenMatch = true;
			}
			if (!tokenMatch)
				return true;

		}

		// Checking inheritable collection locks

		Iterator collectionLocksList = collectionLocks.iterator();
		while (collectionLocksList.hasNext()) {
			lock = (LockInfo) collectionLocksList.next();
			if (lock.hasExpired()) {
				collectionLocks.remove(lock);
				// Restart iteration to prevent iterator corruption
				collectionLocksList = collectionLocks.iterator();
			} else if (path.startsWith(lock.path)) {

				tokenList = lock.tokens.iterator();
				boolean tokenMatch = false;
				while (tokenList.hasNext()) {
					String token = (String) tokenList.next();
					if (ifHeader.indexOf(token) != -1)
						tokenMatch = true;
				}
				if (!tokenMatch)
					return true;

			}
		}

		return false;
	}
	
	public synchronized void parseCollectionLockNull(
			String lockPath,
			HttpServletRequest req,
			XMLWriter generatedXML,
			int type,
			List properties		
	)
	{
		// Displaying the lock-null resources present in that
		// collection
		if (lockPath.endsWith("/"))
			lockPath = 
				lockPath.substring(0, lockPath.length() - 1);
		ArrayList currentLockNullResources =
			(ArrayList) lockNullResources.get(lockPath);
		if (currentLockNullResources != null) {
			Iterator lockNullResourcesList =
				currentLockNullResources.iterator();
			while (lockNullResourcesList.hasNext()) {
				String lockNullPath = (String)
					lockNullResourcesList.next();
				parseLockNullProperties
					(req, generatedXML, lockNullPath, type,
					 properties);
			}
		}
	}

	/**
	 *  @return true if properties committed for object.
	 */
	public synchronized boolean nullProperties( 	
			HttpServletRequest req, 
			HttpServletResponse res,
			String path,
			int type,
			List properties)
		throws IOException
	{
		int slash = path.lastIndexOf('/');
		if (slash != -1) {
			String parentPath = path.substring(0, slash);
			ArrayList currentLockNullResources =
				(ArrayList) lockNullResources.get(parentPath);
			if (currentLockNullResources != null) {
				Iterator lockNullResourcesList =
					currentLockNullResources.iterator();
				while (lockNullResourcesList.hasNext()) {
					String lockNullPath = (String)
						lockNullResourcesList.next();
					if (lockNullPath.equals(path)) {
						res.setStatus(WebdavStatus.SC_MULTI_STATUS);
						res.setContentType("text/xml; charset=UTF-8");
						// Create multistatus object
						XMLWriter generatedXML = 
							new XMLWriter(res.getWriter());
						generatedXML.writeXMLHeader();
						generatedXML.writeElement
							(DN_ALIAS, "multistatus"
							 + WebDAVServlet.NAMESPACE_DECL,
							 XMLWriter.OPENING);
						parseLockNullProperties
							(req, generatedXML, lockNullPath, type,
							 properties);
						generatedXML.writeElement(DN_ALIAS, "multistatus",
												  XMLWriter.CLOSING);
						generatedXML.sendData();
						return true; // found
					}
				}
			}
		}
		
		return false; // not done
	}

	
	/**
	 * Propfind helper method. Dispays the properties of a lock-null resource.
	 *
	 * @param resources Resources object associated with this context
	 * @param generatedXML XML response to the Propfind request
	 * @param path Path of the current resource
	 * @param type Propfind type
	 * @param propertiesArrayList If the propfind type is find properties by
	 * name, then this ArrayList contains those properties
	 */
	private void parseLockNullProperties(HttpServletRequest req,
										 XMLWriter generatedXML,
										 String path, int type,
										 List propertiesArrayList) {

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
		String status = new String("HTTP/1.1 " + WebdavStatus.SC_OK + " Ok");

		// Generating href element
		generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);

		String absoluteUri = req.getRequestURI();
		String relativePath = getRelativePath(req);
		String toAppend = path.substring(relativePath.length());
		if (!toAppend.startsWith("/"))
			toAppend = "/" + toAppend;

		// normalize guarantees there are no .., \, or . in the path, and that
		//  it begins with a slash.
		//generatedXML.writeText(rewriteUrl(normalize(absoluteUri + toAppend)));
if (debug) System.err.println("Final path: "+(absoluteUri + toAppend));

		generatedXML.writeText(WebDAVServlet.urlEncode(absoluteUri + toAppend));
		generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.CLOSING);

		String resourceName = path;
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash != -1)
			resourceName = resourceName.substring(lastSlash + 1);

		switch (type) {

		case FIND_ALL_PROP :

			generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
			generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

			generatedXML.writeProperty(DN_ALIAS, "creationdate",
					WebDAVServlet.getISOCreationDate(lock.creationDate));
			generatedXML.writeElement(DN_ALIAS,
					"displayname", XMLWriter.OPENING);
			generatedXML.writeData(resourceName);
			generatedXML.writeElement
				(DN_ALIAS, "displayname", XMLWriter.CLOSING);
			generatedXML.writeProperty(DN_ALIAS, "getcontentlanguage",
									   Locale.getDefault().toString());
			generatedXML.writeProperty(DN_ALIAS, "getlastmodified",
									   WebDAVServlet.getHttpDate(lock.creationDate));
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

			ArrayList propertiesNotFound = new ArrayList();

			// Parse the list of properties

			generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
			generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

			Iterator properties = propertiesArrayList.iterator();

			while (properties.hasNext()) {

				String property = (String) properties.next();

				if (property.equals("creationdate")) {
					generatedXML.writeProperty
						(DN_ALIAS, "creationdate",
						 WebDAVServlet.getISOCreationDate(lock.creationDate.getTime()));
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
						 WebDAVServlet.getHttpDate(lock.creationDate));
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
						propertiesNotFound.add(property);
				} else {
					propertiesNotFound.add(property);
				}

			}

			generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.CLOSING);
			generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.OPENING);
			generatedXML.writeText(status);
			generatedXML.writeElement(DN_ALIAS, "status", XMLWriter.CLOSING);
			generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.CLOSING);

			Iterator propertiesNotFoundList = propertiesNotFound.iterator();

			if (propertiesNotFoundList.hasNext()) {

				status = new String("HTTP/1.1 " + HttpServletResponse.SC_NOT_FOUND
									+ " Not Found");

				generatedXML.writeElement(DN_ALIAS, "propstat", XMLWriter.OPENING);
				generatedXML.writeElement(DN_ALIAS, "prop", XMLWriter.OPENING);

				while (propertiesNotFoundList.hasNext()) {
					generatedXML.writeElement
						(DN_ALIAS, (String) propertiesNotFoundList.next(),
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
     * Return the relative path associated with this servlet.
     *
     * @param request The servlet request we are processing
     */
    private static String getRelativePath(HttpServletRequest request)
	{
        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return (result);
    }


//*******************************  Constants  *******************************//
	static final String DN_ALIAS = WebDAVServlet.DN_ALIAS;

	/**
	  * Used in lock key construction.
	  */
	private static final String secret = "rbnb_rules";

	/**
	 * Create a new lock.
	 */
	private static final int LOCK_CREATION = 0;

	/**
	 * Refresh lock.
	 */
	private static final int LOCK_REFRESH = 1;

	/**
	 * Default lock timeout value.
	 */
	private static final int DEFAULT_TIMEOUT = 3600;

	/**
	 * Maximum lock timeout.
	 */
	private static final int MAX_TIMEOUT = 604800;

//*****************************  Data Members  ******************************//
	/**
	 * Repository of the locks put on single resources.
	 * <p>
	 * Key : path <br>
	 * Value : LockInfo
	 */
	private final HashMap resourceLocks = new HashMap();

   /**
	 * ArrayList of the heritable locks.
	 * <p>
	 * Key : path <br>
	 * Value : LockInfo
	 */
	private final ArrayList collectionLocks = new ArrayList();

	/**
	 * Repository of the lock-null resources.
	 * <p>
	 * Key : path of the collection containing the lock-null resource<br>
	 * Value : ArrayList of lock-null resource which are members of the
	 * collection. Each element of the ArrayList is the path associated with
	 * the lock-null resource.
	 */
	private final HashMap lockNullResources = new HashMap();
	
	private static final MD5Encoder md5Encoder = new MD5Encoder();
	private static final MessageDigest md5Helper;
	
	static {
		try { 
			md5Helper = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
//****************************  Inner Classes  ******************************//
	/**
	 * Stores lock information.
	 */
	private class LockInfo {
		// -------------------------------------------------------- Constructor
		/**
		 * Constructor.
		 *
		 * @param pathname Path name of the file
		 */
		public LockInfo() { }
	
		// ----------------------------------------------------- Public Methods
		/**
		 * Get a String representation of this lock token.
		 */
		public String toString() {
			String result =	 "Type:" + type + "\n";
			result += "Scope:" + scope + "\n";
			result += "Depth:" + depth + "\n";
			result += "Owner:" + owner + "\n";
			result += "Expiration:" +
				WebDAVServlet.getHttpDate(new Date(expiresAt)) + "\n";
			Iterator tokensList = tokens.iterator();
			while (tokensList.hasNext()) {
				result += "Token:" + tokensList.next() + "\n";
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
			
			/* 2005/10/11  WHF  So sayeth RFC2518:
	Having a lock token provides no special access rights. Anyone can
	find out anyone else's lock token by performing lock discovery.
	Locks MUST be enforced based upon whatever authentication mechanism
	is used by the server, not based on the secrecy of the token values. */			
			toXML(generatedXML, true);
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
				Iterator tokensList = tokens.iterator();
				while (tokensList.hasNext()) {
					generatedXML.writeElement(DN_ALIAS, "href", XMLWriter.OPENING);
					generatedXML.writeText("opaquelocktoken:"
										   + tokensList.next());
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
	
		// ------------------------------------------------- Instance Variables
		String path = "/";
		String type = "write";
		String scope = "exclusive";
		int depth = 0;
		String owner = "";
		final ArrayList tokens = new ArrayList();
		long expiresAt = 0;
		final Date creationDate = new Date();		
	} // end LockInfo

}

interface WebdavStatus extends HttpServletResponse
{
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
}

interface PropertyConstants
{
	/**
	 * Default depth is infite.
	 */
	static final int INFINITY = 3; // To limit tree browsing a bit

	/**
	 * PROPFIND - Specify a property mask.
	 */
	static final int FIND_BY_PROPERTY = 0;


	/**
	 * PROPFIND - Display all properties.
	 */
	static final int FIND_ALL_PROP = 1;


	/**
	 * PROPFIND - Return property names.
	 */
	static final int FIND_PROPERTY_NAMES = 2;
}

