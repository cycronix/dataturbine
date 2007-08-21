package com.ibm.webdav.protocol;

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
 */
 
/** A factory class for creating a URL stream handler for DAV4J.
 * At present, http and rmi protocols are implemented.
 *
 */
public class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory {
/**
 * Construct a URLStreamHandlerFactory.
 */
public URLStreamHandlerFactory() {
	super();
}
/**
 * Create a URL stream handler for WebDAV.
 * At present, http and rmi protocols are implemented.
 * If null is returned, then the JDK uses sun.net.www.protocol.<protocol>.Handler.
 *
 * @return java.net.URLStreamHandler: The URL stream handler for this protocol.
 * @param name String: The protocol name, e.g. "http".
 * @author Arthur Ryman
 *
 */
public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
	if (protocol.equalsIgnoreCase ("http")) {
		return new com.ibm.webdav.protocol.http.Handler ();
	} else if (protocol.equalsIgnoreCase ("rmi")) {
		return new com.ibm.webdav.protocol.rmi.Handler ();
	}

	return null;
}
}
