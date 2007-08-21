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

/**
 * A class to represent a WebDAV HTTP connection to a remote object.
 * WebDAVURLConnection is a subclass of sun.net.www.protocol.http.HttpURLConnection
 * that overrides methods as required to support the additional WebDAV methods.
 * HttpURLConnection was not developed to be
 * subclassed as it contains private data that needs to be available in the
 * subclasses. So some of the method overrides may seem a little strange. See the
 * individual methods for details. The changes to HttpURLConnection are to allow
 * WebDAV methods, and to handle errors differently so that clients can receive
 * more specific exceptions.
 *
 * @author  Jim Amsden
 */
public class WebDAVURLConnection extends sun.net.www.protocol.http.HttpURLConnection {

	private static final String[] methods = {"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", // from HTTP/1.1
	"MKCOL", "COPY", "MOVE", "PROPFIND", "PROPPATCH", "LOCK", "UNLOCK","SEARCH"};
	/* We only have a single static authenticator for now.
	*/
	private static WebDAVAuthenticator defaultAuth;
protected WebDAVURLConnection(URL u, Handler handler) throws IOException {
	super(u, handler);
}
public WebDAVURLConnection(URL u, String host, int port) throws IOException {
	super(u, host, port);
}
/*
* Catch the IOException the superclass raises when it gets a status code
* greater than 400 and the file is not some text or HTML file that might
* contain additional error information. Without this, the DAV4J client
* API doesn't get a chance to convert the status code to a WebDAVException.
*/

public synchronized InputStream getInputStream() throws IOException {
	InputStream is = null;
	try {
		is = super.getInputStream();
	} catch (IOException exc) {
		// just ignore it, the DAV4J client API will translate the
		// status code to the proper WebDAVException
	}
	return is;
}
/*
*
* Overridden to support additional WebDAV methods.
*/

public synchronized OutputStream getOutputStream() throws IOException {
	OutputStream os = null;
	String savedMethod = method;
	// see if the method supports output
	if (method.equals("GET") || method.equals("PUT") || method.equals("POST") ||
		method.equals("PROPFIND")  || method.equals("PROPPATCH") ||
		method.equals("MKCOL") || method.equals("MOVE") || method.equals("COPY") ||
		method.equals("LOCK"))  {
		// fake the method so the superclass method sets its instance variables
		method = "PUT";
	} else {
		// use any method that doesn't support output, an exception will be
		// raised by the superclass
		method = "DELETE";
	}
	os = super.getOutputStream();
	method = savedMethod;
	return os;
}
/**
 * Set the method for the URL request, one of:
 * <UL>
 *  <LI>GET
 *  <LI>POST
 *  <LI>HEAD
 *  <LI>OPTIONS
 *  <LI>PUT
 *  <LI>DELETE
 *  <LI>PROPFIND
 *  <LI>PROPPATCH
 *  <LI>MKCOL
 *  <LI>MOVE
 *  <LI>COPY
 *  <LI>LOCK
 *  <LI>UNLOCK
 *  <LI>TRACE
 *
 * @exception ProtocolException if the method cannot be reset or if
 *              the requested method isn't valid for HTTP.
 */
public void setRequestMethod(String method) throws ProtocolException {
	if (connected) {
		throw new ProtocolException("Cannot reset method once connected");
	}
	// prevent clients from specifying invalid methods. This prevents experimenting
	// with new methods without editing this code, but should be included for
	// security reasons.
	for (int i = 0; i < methods.length; i++) {
		if (methods[i].equals(method)) {
			this.method = method;
			return;
		}
	}
	throw new ProtocolException("Invalid WebDAV method: " + method);
}
}
