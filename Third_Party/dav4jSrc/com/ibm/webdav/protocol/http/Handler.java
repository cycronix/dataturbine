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
 */


import java.io.*;
import java.net.*;

/** open an http input stream given a URL */
public class Handler extends sun.net.www.protocol.http.Handler {
   protected String proxy;
   protected int proxyPort;

   public Handler () {
	  proxy = null;
	  proxyPort = -1;
   }   
   public Handler (String proxy, int port) {
	  this.proxy = proxy;
	  this.proxyPort = port;
   }   
   protected java.net.URLConnection openConnection(URL u)
   throws IOException {
	  return new WebDAVURLConnection(u, this);
   }   
}
