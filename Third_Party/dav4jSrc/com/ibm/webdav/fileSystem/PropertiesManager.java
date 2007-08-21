package com.ibm.webdav.fileSystem;

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

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;


/** PropertiesManager extends CachedProperties to implements its repository
 * specific abstract methods.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class PropertiesManager extends CachedPropertiesManager {

	public static String propertiesSuffix = ".wdp";	// a WebDAV properties file

	protected Document cachedPropertiesDocument = null;
	protected long cacheTimeStamp = 0;
	private static final int bufferSize = 8192;
   public PropertiesManager()
   {
   }
   // Constructors:

   public PropertiesManager(ResourceImpl resource,
							com.ibm.webdav.impl.NamespaceManager namespaceManager)
   {
	  super(resource, namespaceManager);
   }
/** Delete all properties. Delete the properties file and
* flush the cache.
* @exception com.ibm.webdav.WebDAVException
*
*/
public void deleteProperties() throws WebDAVException {
	File propertiesFile = new File(getPropertiesFileName());
	propertiesFile.delete();
	cachedPropertiesDocument = null;
}
/** Get the name of the properties file. The file name is the resource
* file name concatenated with PropertiesManager.propertiesSuffix.
* @return the properties file name
* @exception com.ibm.webdav.WebDAVException
*/
private String getPropertiesFileName() throws WebDAVException {
	String fileName = ResourceFactory.getRealPath(resource.getURL());
	// if the resource is a collection, strip off any trailing path separator
	if (fileName.endsWith(File.separator)) {
		fileName = fileName.substring(0, fileName.length() - 1);
	}

	// create the properties file name by adding a .properties extension
	return fileName + PropertiesManager.propertiesSuffix;
}
public Vector loadPropertyDefinitions() throws WebDAVException {
  return null;
}

/** Load the properties document from the file system properties file.
* Check to see if the cache has expired.
* @return an XML document containing a properties element.
* @exception com.ibm.webdav.WebDAVException
*/
public Document loadProperties() throws WebDAVException {
	File propertiesFile = new File(getPropertiesFileName());
	try {
		// make sure the user is authorized to read the properties
		if (propertiesFile.exists() && !propertiesFile.canRead()) {
			throw new WebDAVException(WebDAVStatus.SC_UNAUTHORIZED, "Insufficient permissions");
		}

		if (cachedPropertiesDocument != null && propertiesFile.lastModified() <= cacheTimeStamp) {
			return cachedPropertiesDocument;
		}
		cacheTimeStamp = propertiesFile.lastModified();

		// read the properties file and parse it's contents
		Reader reader = new InputStreamReader(new FileInputStream(propertiesFile), Resource.defaultCharEncoding);
		Reader is = new BufferedReader(reader);
		WebDAVErrorHandler errorHandler = new WebDAVErrorHandler(resource.getURL().toString());

		/*Parser xmlParser = new Parser(propertiesFile.getName(), errorListener, null);
		xmlParser.setWarningNoDoctypeDecl(false);
		xmlParser.setProcessNamespace(true);
		cachedPropertiesDocument = xmlParser.readStream(is);*/
                DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                docbuilder.setErrorHandler(errorHandler);
                cachedPropertiesDocument = docbuilder.parse(new org.xml.sax.InputSource(is));
		if (errorHandler.getErrorCount() > 0) {
			throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Syntax error in properties file");
		}

		// get the properties element from the document so we can put it
		// in a MultiStatus
		Element properties = (Element) cachedPropertiesDocument.getDocumentElement();
		if (properties == null || !properties.getTagName().equals("properties")) {
			throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Bad properties file");
		}
	} catch (FileNotFoundException exc) {
		if (propertiesFile.exists()) {
			throw new WebDAVException(WebDAVStatus.SC_UNAUTHORIZED, "Insufficient permissions");
		} else {
			// we have a resouce that doesn't have any properties yet
			try {
                        cachedPropertiesDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			} catch(Exception e) {
                          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
                        }
                        //cachedPropertiesDocument.setVersion(Resource.XMLVersion);
			Element properties = cachedPropertiesDocument.createElement("properties");
			properties.setAttribute("xmlns:D", "DAV:");
			cachedPropertiesDocument.appendChild(properties);
		}
	} catch (SecurityException exc) {
		throw new WebDAVException(WebDAVStatus.SC_UNAUTHORIZED, "Insufficient permissions");
	} catch (Exception exc) {
		exc.printStackTrace();
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Can't read properties file");
	}
	return cachedPropertiesDocument;
}
/** Remove the live DAV properties from the properties document that
* do not need to be saved. There is no reason to save them as long
* as they are recalculated each time the properties are loaded. This
* method removes the ones that are repository specific.
*
* @param propertiesDocument an XML document containing a properties element.
*/
public void removeLiveProperties(Document propertiesDocument) {
	Element properties = propertiesDocument.getDocumentElement();
	Element p = null;
	p = (Element)((Element) properties).getElementsByTagNameNS("DAV:", "getcontentlength").item(0);
	if (p != null)
		properties.removeChild(p);
	p = (Element)((Element) properties).getElementsByTagNameNS("DAV:", "resourcetype").item(0);
	if (p != null)
		properties.removeChild(p);
	p = (Element)((Element) properties).getElementsByTagNameNS("DAV:", "getlastmodified").item(0);
	if (p != null)
		properties.removeChild(p);
	// I haven't read this carefully, but getcontent type seems to be
	//    treated as a live property in the other methods, so treat it
	//    as such here.  (jlc 991002)
	p = (Element)((Element) properties).getElementsByTagNameNS("DAV:", "getcontenttype").item(0);
	if (p != null)
		properties.removeChild(p);
}
/** Save the properties to the persistent store.
*
* @param propertiesDocument an XML document containing a properties element.
* @exception com.ibm.webdav.WebDAVException
*/
public void saveProperties(Document propertiesDocument) throws WebDAVException {
	File propertiesFile = new File(getPropertiesFileName());
	try {
		// make sure the user is authorized to read the properties
		if (propertiesFile.exists() && !propertiesFile.canWrite()) {
			throw new WebDAVException(WebDAVStatus.SC_UNAUTHORIZED, "Insufficient permissions");
		}

		// write the properties file
		Writer writer = new OutputStreamWriter(new FileOutputStream(propertiesFile), Resource.defaultCharEncoding);
		PrintWriter pout = new PrintWriter(writer, false);
		pout.print(XMLUtility.printNode(propertiesDocument.getDocumentElement()));
                //propertiesDocument.print(pout);
		pout.close();
	} catch (Exception exc) {
		exc.printStackTrace();
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Can't write properties file");
	}
}
/** Update the live properties that are unique to the
* repository implementation
*
* @param document an XML document containing a properties to update.
* @exception com.ibm.webdav.WebDAVException
*/
public void updateLiveProperties(Document document) throws WebDAVException {
	Element properties = document.getDocumentElement();

	// update the repository specific live properties
	Element getContentLength = document.createElement("D:getcontentlength");
	long length = 0;
	File file = new File(ResourceFactory.getRealPath(resource.getURL()));
	if (file.exists()) {
		length = file.length();
	}
	// TODO: this doesn't account for the content length for text files
	// which are encoded. The content length is therefore client specific
	// because it depends on the encoding the client requests
	getContentLength.appendChild(document.createTextNode(new Long(length).toString()));
	properties.appendChild(getContentLength);
	Element resourceType = document.createElement("D:resourcetype");
	if (file.exists() && file.isDirectory()) {
		resourceType.appendChild(document.createElement("D:collection"));
	}
	properties.appendChild(resourceType);
	Element lastModifiedDate = document.createElement("D:getlastmodified");
	// TODO: bug, we can't assume that the value returned from file.lastModified can be used in the Date constructor.  See the java.io.File.lastModified() documentation.
	// TODO: we actually are supposed to insure that this is the same value returned by the main server in response to GET.  I don't know if that's possible right now.  It may return nothing at all.  Check later.
	String cdstring = new SimpleRFC1123DateFormat().format(new Date(file.lastModified()));
	lastModifiedDate.appendChild(document.createTextNode(cdstring));
	properties.appendChild(lastModifiedDate);

	Element contentType = document.createElement("D:getcontenttype");
	String mimetype = com.ibm.webdav.fileSystem.NamespaceManager.guessAtContentTypeForName(ResourceFactory.getRealPath(resource.getURL()));
	contentType.appendChild(document.createTextNode(mimetype));
	properties.appendChild(contentType);
}
}
