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

import java.io.IOException;

import java.net.URLConnection;
import java.net.HttpURLConnection;

import java.util.Enumeration;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.*;
import javax.servlet.http.*;

/*
	*** History ***
	2006/08/14  WHF  Created.
	2006/08/23  WHF  Copies headers from request to forwarded request.
	2006/09/05  WHF  Add HTTP extension to include requestor IP address, as
		per RFC 2774.
*/


/**
  *  A servlet which forwards HTTP requests to other machines and/or ports.
  */
public final class HttpForwardServlet extends HttpServlet 
{
	/**
	  * Construction, loading options from web.xml.
	  */
	public HttpForwardServlet() throws NamingException
	{
		// Load properites from the "env-entry" section of the web.xml file.
		InitialContext webEnv=new InitialContext();
		checkEnv(
				webEnv,
				"java:comp/env/com.rbnb.web.debug",
				"debug",
				Boolean.FALSE);
		debug = ((Boolean) defaultProperties.get("debug")).booleanValue();
		checkEnv(
				webEnv,
				"java:comp/env/com.rbnb.web.destination",
				"destination",
				null);
		String dest = defaultProperties.get("destination").toString();
		if (dest.endsWith("/")) 
			destination = dest.substring(0, dest.length() - 1);
		else destination = dest;
	}
	
	/**
	  *  Handles the Http GET operation.
	  *
	  */	
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{		
		ServletOutputStream sos = res.getOutputStream();
		String qString = req.getQueryString();
		String reqUri = req.getRequestURI();
		if (servletPath == null) {
			int nextSlash = reqUri.indexOf('/', 1);
			if (nextSlash == -1) servletPath = reqUri;
			else servletPath = reqUri.substring(0, nextSlash);
		}
/*		String destUrl = destination + req.getServletPath()
				+ (qString == null?"":"?"+qString); */
		// Cut the URI to this servlet from the Request URI:
		String destUrl = destination + reqUri.substring(
				servletPath.length())
				+ (qString == null?"":"?"+qString);
				
		/* debugging for various request fields.
		sos.print("<html><body>");
		sos.print("<p>getPathInfo(): ");
		sos.println(req.getPathInfo());
		sos.print("<p>getPathTranslated(): ");
		sos.println(req.getPathTranslated());
		sos.print("<p>getQueryString(): ");
		sos.println(req.getQueryString());
		sos.print("<p>getRequestURI(): ");
		sos.println(req.getRequestURI());
		sos.print("<html><body><p>getRequestURL(): ");
		sos.println(req.getRequestURL().toString());
		sos.print("<p>getServletContextName(): ");
		sos.println(this.getServletContext().getServletContextName());
		sos.print("<p>getServletName(): ");
		sos.println(this.getServletName());
		sos.print("<p>getServletPath(): ");
		sos.println(req.getServletPath());
		sos.print("<p>Forward URL = ");
		sos.print(destUrl);
		sos.println("</body></html>");
		if (true) return;  */
				
		HttpURLConnection connection = (HttpURLConnection) 
				new java.net.URL(destUrl).openConnection();
		// 2006/08/16  WHF  Pass redirects back to original requestor:
		// (later)  WHF  Decided not to take this course.		
		//connection.setInstanceFollowRedirects(false);
		// 2006/08/17  WHF  In case it was a POST:
		connection.setRequestMethod(req.getMethod());
		// 2006/08/23  WHF  Pass request headers on to connection:
		copyHeaders(req, connection);
		connection.connect();
		try {
			int status = connection.getResponseCode(); 
			res.setStatus(status);
			
			// Copy headers from response:
			if (debug) {
				System.err.println(req.getRequestURL().toString()+"\n\t-> "
						+destUrl+"\n\t = "+connection.getResponseCode());
				System.err.println("---  Response ---");
			}	
			// 2006/08/23  WHF  Apparently, they use null as the key for the
			//  first line of the HTTP response, i.e. "HTTP/1.1 200 OK".
			//  This is not documented.
			String key, value;
			int c = 0;
			while ((value = connection.getHeaderField(c)) != null) {
				// Don't copy the HTTP response line:
				if ((key = connection.getHeaderFieldKey(c++)) != null) {
					res.setHeader(key, value);
				}
				if (debug)
					System.err.println(key+": "+connection.getHeaderField(c-1));			
			}
			
			// 2006/08/30  WHF  Copy data only if we received an Ok response:
			if (status >= 200 && status < 300) { 
				byte[] buff = new byte[1024];
				int nRead;
				java.io.InputStream is = connection.getInputStream();
				
				while ((nRead = is.read(buff)) != -1)
					sos.write(buff, 0, nRead);
			}
		} finally {
			connection.disconnect();
		}
	}
	
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
	
	private void copyHeaders(HttpServletRequest req, URLConnection conn)
	{
		if (debug) System.err.println("\n\n---  Request ---");
		Enumeration names = req.getHeaderNames();
		while (names.hasMoreElements()) {
			String header = (String) names.nextElement();
			Enumeration values = req.getHeaders(header);
			while (values.hasMoreElements()) {
				String value = (String) values.nextElement();
				// Requires Java 1.4:
				conn.addRequestProperty(header, value);
				if (debug) System.err.println(header+": "+value);			
			}
		}
		
		// 2006/09/05  WHF  Add extension header:
		// The namespace code will be from 10 to 99:
		int ns = new java.util.Random().nextInt(90)+10;
		conn.addRequestProperty("Opt", "\"http://rbnb.net/ext\"; ns="+ns);
		conn.addRequestProperty(""+ns+"-source-ip", req.getRemoteAddr());
		
	}

	/**
	  * Enables debug output if set in web.xml.
	  */
	private final boolean debug;
	/**
	  * Properties configured in web.xml.
	  */
	private final HashMap defaultProperties = new HashMap();
	/**
	  * URL prefix to forward requests to.
	  */
	private final String destination;
	
	/**
	  * URI to this servlet relative to the server root, lazily initialized.
	  */
	private String servletPath;
}

