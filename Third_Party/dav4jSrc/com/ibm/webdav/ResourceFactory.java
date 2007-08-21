package com.ibm.webdav;

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
import java.rmi.UnknownHostException;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Logger;

import com.ibm.webdav.impl.*;
import com.ibm.webdav.protocol.http.*;

/** A ResourceFactory is used to construct Resources or their subclasses
 * based on the desired communication protocol, http:, rmi:, or iiop: as
 * specified in the resource URL.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.impl.ResourceHTTPStub
 * @see com.ibm.webdav.impl.ResourceImpl
 */
public class ResourceFactory {
	public static String defaultDocRoot = "c:\\www\\html\\dav";

	/** All WebDAV properties from the dav4j.properties file located somewhere
	* in the classpath. See the dav4j.properties file for details.
	*/
	public static Properties properties = null;
	
	/**
	 * Logger for this class
	 */
	private static Logger m_logger = Logger.getLogger(ResourceFactory.class.getName());

	// load the webdav properties
	static {
		properties = new Properties();
		String resource = "dav4j.properties";
		
		try {
		    // in the current build environment, dav4j.properties will most likely be in the 
		    // jar file for harmonise-dav-server, so look in here first
		    ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream istream = loader.getResourceAsStream(resource); // will return null if not found
			if (istream != null) {
			    properties.load(loader.getResourceAsStream(resource));
			}
        	else {
        	    // If that fails, find the dav4j.properties file in the classpath
    			String classpath = System.getProperty("java.class.path");
    			StringTokenizer paths = new StringTokenizer(classpath, File.pathSeparator);
    			File propertiesFile = null;
    			boolean found = false;
    			while (!found && paths.hasMoreTokens()) {
    				String path = paths.nextToken();
    				propertiesFile = new File(path, resource);
    				found = propertiesFile.exists();
    			}
    			if (found) {
    				try {
    					properties.load(new FileInputStream(propertiesFile));
    				} catch (Exception exc) {
    					exc.printStackTrace();
    				}
    			}	
        	}
		} catch (RuntimeException e) {
			m_logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (IOException e) {
        	m_logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
	}
/** Create a Resource identified by the given URL. The URL
* specifies the protocol to use for remote communication, and
* the host name. If the host name is "localhost", the communication
* is local (typical fat client).
* <p>
* This method does attempt to determine
* if the url is for a collection or a normal resource.  At the
* moment it does this by asking the server.  If it can not
* determine what type the resource is by asking the server
* (often because the resource doesn't yet exist at the server),
* then an exception is thrown.
* <p>
* The caller should be aware that just because we've
* asked the server if this URL is a collection/resouce, that doesn't
* insure that it wasn't deleted a moment or replaced a moment later
* by something different.  Locking is required to do that and this
* method doesn't lock.
*
* @param url the identifier of the resource
* @return a Resource, ResourceCollection, or one of its subclasses.
* @exception java.io.IOException
*/
public static Resource create(String url) throws java.io.IOException {
	return create(url, null);
}
/** Create a Resource identified by the given URL. The URL
* specifies the protocol to use for remote communication, and
* the host name. If the host name is "localhost", the communication
* is local (typical fat client).
* <p>
* This method does attempt to determine
* if the url is for a collection or a normal resource.  At the
* moment it does this by asking the server.  If it can not
* determine what type the resource is by asking the server
* (often because the resource doesn't yet exist at the server),
* then an exception is thrown.
* <p>
* The caller should be aware that just because we've
* asked the server if this URL is a collection/resouce, that doesn't
* insure that it wasn't deleted a moment or replaced a moment later
* by something different.  Locking is required to do that and this
* method doesn't lock.
*
* @param resourceURL the identifier of the resource
* @return a Resource, ResourceCollection, or one of its subclasses.
* @exception java.io.IOException
*/
public static Resource create(String url, TargetSelector targetSelector) throws java.io.IOException {
	Resource resource = null;
	try {
		resource = new Resource(url, targetSelector);
		if (resource.isCollection()) {
			resource = null;
			resource = new Collection(url, targetSelector);
		}
	} catch (WebDAVException exc) {
		throw exc;
	} 
	return resource;
}
/** Create a Collection identified by the given URL. The URL
* specifies the protocol to use for remote communication, and
* the host name. If the host name is "localhost", the communication
* is local (typical fat client).
*
* @param resourceURL the identifier of the resource
* @return a Collection
* @exception com.ibm.WebDAVException
*/
public static IRCollection createCollection(URL url, TargetSelector targetSelector) throws WebDAVException {
   IRCollection resource = null;
   String protocol = null;
   try {
	   protocol = url.getProtocol();

	   // if the URL host is local, and the port was not specified,
	   // do local access
   	if (isLocalHost(url)) {
	   	resource = new CollectionImpl(url, getRealPath(url), targetSelector);
   	} else if (protocol.equals("http")) {
		   	resource = new CollectionHTTPStub(url, targetSelector);
	   } else if (protocol.equals("rmi")) {
   		String name = url.toString();
		   name = name.substring(name.indexOf(":") + 1);
		   try {
	   		resource = (IRCollection) Naming.lookup(name);
   		} catch (java.rmi.NotBoundException exc) {
			   throw new WebDAVException(WebDAVStatus.SC_NOT_FOUND, "Not bound");
		   }
	   } else {
		 throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "WebDAV: Invalid communication protocol: " + protocol);
   	}
	} catch (WebDAVException exc) {
		throw exc;
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "WebDAV: Invalid communication protocol: " + protocol);
   }
	return resource;
}
/** Create a Resource identified by the given URL. The URL
* specifies the protocol to use for remote communication, and
* the host name. If the host name is "localhost", the communication
* is local (typical fat client).
*
* @param resourceURL the identifier of the resource
* @return a Resource
* @exception com.ibm.webdav.WebDAVException
*/
public static IRResource createResource(URL url, TargetSelector targetSelector) throws WebDAVException {
   IRResource resource = null;
   String protocol = null;
	try {
	   protocol = url.getProtocol();

   	// if the URL host is local, and the port was not specified,
	   // do local access
	   if (isLocalHost(url)) {
           	   resource = new ResourceImpl(url, getRealPath(url), targetSelector);
   	} else if (protocol.equals("http")) {
          //@todo this ResourceHTTPStub won't work with Tomcat - needs work
            resource = new ResourceHTTPStub(url, targetSelector);
	   } else if (protocol.equals("rmi")) {
            String name = url.toString();
   		name = name.substring(name.indexOf(":") + 1);
	   	try {
		   	resource = (IRResource) Naming.lookup(name);
   		} catch (java.rmi.NotBoundException exc) {
	   		throw new WebDAVException(WebDAVStatus.SC_NOT_FOUND, "Not bound");
		   }
   	} else {
		   throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "WebDAV: Invalid communication protocol: " + protocol);
	   }
 	} catch (WebDAVException exc) {
		throw exc;
  } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "WebDAV: Invalid communication protocol: " + protocol);
   }
	return resource;
}
/** Translate a URL into a local path name using the doc.root property
* from the webdav properties. The doc.root property in the dav4j.properties
* file is used for local access without a server.
*
* @param url the URL of the resource
* @return the translated pathname
*/

public static String getRealPath(URL url) {
	String docRoot = properties.getProperty("doc.root", defaultDocRoot);
	String pathName = docRoot + url.getFile().replace('/', File.separatorChar);
	return pathName;
}
/** Does the given URL refer to the local host? Used to determine if there doesn't
* need to be any RPC at all.
* @return true if the host in the URL is the local host, and a port was not specified
* @exception UnknownHostException
*/
public static boolean isLocalHost(URL url) throws WebDAVException {
	/*String thisHost = null;

        System.out.println("RESOURCE FACTORY: isLocalHost check for url " + url.getProtocol() + url.getHost() + url.getPort() + url.getPort() + url.getQuery());

	try {
		thisHost = InetAddress.getLocalHost().getHostName();

	} catch (java.net.UnknownHostException exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Unknown Host");
	}
	return (url.getHost().equals("localhost") ||
		    url.getHost().equals(thisHost))*//*&& url.getPort() == -1*///;
        return true;
}
}
