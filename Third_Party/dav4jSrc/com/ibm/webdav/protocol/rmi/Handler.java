package com.ibm.webdav.protocol.rmi;

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
import java.io.*;
import java.net.*;

/** Open an rmi input stream given a URL. This handler is only
 * used to allow construction of URLs having rmi as their protocol. 
 * WebDAV uses this to determine what protocol to use for accessing
 * a resource.
 */
public class Handler extends java.net.URLStreamHandler {
   protected String proxy;
   protected int proxyPort;
public Handler() {
	proxy = null;
	proxyPort = -1;
}
public Handler(String proxy, int port) {
	this.proxy = proxy;
	this.proxyPort = port;
}
/** It is not expected that a connection would ever be opened
* for RMI. Use Naming.lookup() instead.
*/
protected java.net.URLConnection openConnection(URL u) throws IOException {
	return null;
}
}
