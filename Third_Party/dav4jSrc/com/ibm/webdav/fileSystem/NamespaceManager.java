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
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;


/** fileSystem.NamespaceManager implements all WebDAV namespace methods using
 * the native file system as a repository manager. A resource collection
 * corresponds to a directory while a resource corresponds to a file in a
 * directory.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class NamespaceManager implements com.ibm.webdav.impl.NamespaceManager {

   private ResourceImpl resource = null;
   private OutputStream openedOutputStream = null;

   private static final int bufferSize = 8192;
/** Create a NamespaceManager for creating resources. (This is necessary because
* createResource can't be static and declared in the NamespaceManager interface.)
*/
public NamespaceManager() {
}
/** Create a NamesapceManager for a given resource.
* @param resource the resource to manage
*/
public NamespaceManager(ResourceImpl resource) {
	initialize(resource);
}
/** Close any opened OutputStream on the contents of the managed resource.
* In this case, just close any open file output stream.
* @exception com.ibm.webdav.WebDAVException
*/
public void closeContentsOutputStream() throws WebDAVException {
	// close any opened output stream
	if (openedOutputStream == null) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "No output stream is opened");
	}
	try {
		openedOutputStream.close();
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	openedOutputStream = null;
}
/** Create a collection with the given local name by creating a
* directory with the local name.
* @param localName the repository specific local name for the resource
* @exception com.ibm.webdav.WebDAVException
*/
public void createCollection(String localName) throws WebDAVException {
	// be sure you can write to the parent directory
	String parentName = getParentName();
	File parent = new File(parentName);
	if (!parent.canWrite()) {
		throw new WebDAVException(WebDAVStatus.SC_FORBIDDEN, "Insufficient permissions");
	}
	File dir = new File(localName);
	if (!dir.mkdir()) {
		throw new WebDAVException(WebDAVStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE, "Could not create collection");
	}
}
/** Create this resource as a lock-null resource, a resource created by locking one that
* did not previously exist. This implementation creates a properties file for the resource,
* but no resource file. That is, a lock-null resource is a resource with no contents file,
* but having a properties file.
* @exception com.ibm.webdav.WebDAVException
*/
public void createLockNullResource() throws WebDAVException {
	Document document = resource.loadProperties();
	resource.saveProperties(document);
}
/** create a binding
* @exception com.ibm.webdav.WebDAVException
*/
public void createBinding(URL destURL) throws WebDAVException {
  //@todo
}
/** Move the managed resource.
* @exception com.ibm.webdav.WebDAVException
*/
public void move(String path) throws WebDAVException {
  //@todo
}
/** Delete the managed resource from the repository. Just delete
* the file or directory after making sure the server has sufficient
* permission.
* @exception com.ibm.webdav.WebDAVException
*/
public void delete() throws WebDAVException {
	String fileName = ResourceFactory.getRealPath(resource.getURL());
	File file = new File(fileName);

	// see if the file is writeable
	if (!file.canWrite()) {
		throw new WebDAVException(WebDAVStatus.SC_FORBIDDEN, "Insufficient permissions");
	}

	// Attempt to delete this resource
	if (!file.delete()) {
		throw new WebDAVException(WebDAVStatus.SC_CONFLICT, "Unable to delete resource, may be in use");
	}
}
/** Create an XML file containing the contents of a collection.
* @param file the directory to read
* @return an InputStream on the resulting XML file.
* @exception com.ibm.webdav.WebDAVException
*/
private InputStream directoryToXMLStream(File file) throws WebDAVException {
	Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR,e.getMessage());
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element collection = document.createElement("D:collection");
	collection.setAttribute("xmlns:D", "DAV:");
	document.appendChild(collection);
	String[] fileNames = file.list();
	for (int i = 0; i < fileNames.length; i++) {
		String fileName = fileNames[i];
		String propertiesFileName = null;
		if (fileName.endsWith(PropertiesManager.propertiesSuffix)) {
			propertiesFileName = fileName;
			fileName = propertiesFileName.substring(0, propertiesFileName.length() - PropertiesManager.propertiesSuffix.length());
		}
		// add resources and lock-null resources
		if (propertiesFileName == null || (propertiesFileName != null && !new File(fileName).exists())) {
			Element uri = document.createElement("D:member");
			uri.appendChild(document.createTextNode(fileName));
			collection.appendChild(uri);
		}
	}
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	try {
		PrintWriter pout = new PrintWriter(new OutputStreamWriter(os, Resource.defaultCharEncoding), false);
		pout.print(XMLUtility.printNode(document.getDocumentElement()));
                //document.print(pout);
	} catch (Exception exc) {
	}
	// TODO: depends on charset encoding
	//resource.getResponseContext().contentLength(os.toByteArray().length);
	resource.getResponseContext().contentType("text/xml");
	return new ByteArrayInputStream(os.toByteArray());
}
/** See if this resource exists in the repository.
*
* @return true if the resource exists, false otherwise.
* @exception com.ibm.webdav.WebDAVException
*/
public boolean exists() throws WebDAVException {
	File file = new File(ResourceFactory.getRealPath(resource.getURL()));
	return file.exists();
}
/** Open an InputStream on the contents of the managed resource. This implementation
* opens the resource file, or gets an input stream on the an XML document describing
* the contents of a directory.
*
* @return an InputStream on the contents of the managed resource.
* @exception com.ibm.webdav.WebDAVException
*/
public InputStream getContentsInputStream() throws WebDAVException {
	File file = new File(ResourceFactory.getRealPath(resource.getURL()));
	InputStream is = null;

	try {
		// is the uri a collection (directory in this implementation)?
		if (file.isDirectory()) {
			resource.getResponseContext().contentType("text/xml");
			is = directoryToXMLStream(file);
		} else {
			is = new BufferedInputStream(new FileInputStream(file), bufferSize);
			// get the content type. Default to text/plain
			String contentType = guessAtContentTypeForName( file.getName() );
			resource.getResponseContext().contentType(contentType);
			// content type for text files must handle charset encodings
			if (!contentType.startsWith("text/")) {
				resource.getResponseContext().contentLength(file.length());
			}
			String cdstring = new SimpleRFC1123DateFormat().format(new Date(file.lastModified()));
			resource.getResponseContext().lastModified(cdstring);
		}
	} catch (FileNotFoundException exc) {
		if (file.exists()) {
			throw new WebDAVException(WebDAVStatus.SC_UNAUTHORIZED, "Insufficient permissions");
		} else {
			throw new WebDAVException(WebDAVStatus.SC_NOT_FOUND, "Resource not found");
		}
	} catch (SecurityException exc) {
		throw new WebDAVException(WebDAVStatus.SC_UNAUTHORIZED, "Insufficient permissions");
	}
	return is;
}
/** Open an OutputStream in order to write the contents of the managed resource.
*
* @return an OutputStream on the contents of the managed resource.
* @exception com.ibm.webdav.WebDAVException
*/
public OutputStream getContentsOutputStream() throws WebDAVException {
	String fileName = ResourceFactory.getRealPath(resource.getURL());
	File file = new File(fileName);

	// see if the file is writeable
	if (file.exists() && !file.canWrite()) {
		throw new WebDAVException(WebDAVStatus.SC_FORBIDDEN, "Insufficient permissions");
	}

	// don't allow writes to properties files
	if (fileName.endsWith(PropertiesManager.propertiesSuffix)) {
		throw new WebDAVException(WebDAVStatus.SC_FORBIDDEN, "Can't write to WebDAV properties files");
	}

	// Resources that already exist are overwritten
	try {
		openedOutputStream = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
	} /*catch (WebDAVException exc) {
		throw exc;
	}*/ catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	return openedOutputStream;
}
/** Get the members of a collection.
*
* @return a Vector of Resources (ResourceP or CollectionP)
* @exception com.ibm.webdav.WebDAVException
*/
public Vector getMembers() throws WebDAVException {
	Vector members = new Vector();
	String parentName = ResourceFactory.getRealPath(resource.getURL());
	if (!parentName.endsWith(File.separator)) {
		parentName = parentName + File.separator;
	}

	File parent = new File(parentName);
	if (!parent.isDirectory()) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Resource is not a collection");
	}

	String[] fileNames = parent.list();
	if (fileNames == null) {
		// this can happen if the directory was deleted, but there was an
		// explorer open on it that hasn't been refreshed
		return members;
	}

	for (int i = 0; i < fileNames.length; i++) {
		String fileName = fileNames[i];
		String propertiesFileName = null;
		if (fileName.endsWith(PropertiesManager.propertiesSuffix)) {
			propertiesFileName = fileName;
			fileName = propertiesFileName.substring(0, propertiesFileName.length() - PropertiesManager.propertiesSuffix.length());
		}
		// add resources and lock-null resources
		String childName = parentName + fileName;
		File file = new File(childName);
		if (propertiesFileName == null || !file.exists()) {
			ResourceImpl member = null;
			if (file.isDirectory()) {
				member = new CollectionImpl(((CollectionImpl) resource).getChildURL(fileName), childName, null);
			} else {
				member = new ResourceImpl(((CollectionImpl) resource).getChildURL(fileName), childName);
			}
			member.setRequestContext(resource.getContext());
			members.addElement(member);
		}
	}
	return members;
}
/** Get the name of the collection (directory) containing this resource (file).
*
* @return the parent collection name, always ending in a separator
* @exception com.ibm.webdav.WebDAVException
*/
public String getParentName() throws WebDAVException {
	String fileName = ResourceFactory.getRealPath(resource.getURL()) ;
	int delimiterPosition = 0;
	if (fileName.endsWith(File.separator)) {
		delimiterPosition = fileName.substring(0, fileName.length() - 1).lastIndexOf(File.separator);
	} else {
		delimiterPosition = fileName.lastIndexOf(File.separator);
	}
	return fileName.substring(0, delimiterPosition + 1);
}
/** Guess at the mimetype of a file based on it's filename extention.
*
* @param fn the filename
* @return the mimetype string
*/
public static String guessAtContentTypeForName(String fn) {
	int i = fn.lastIndexOf('.');
	if (i == -1)
		return "text/plain";
	String ext = fn.substring(i + 1);
	String mtype = ResourceFactory.properties.getProperty(ext, "text/plain").trim();
//	if (mtype == null) {
//		mtype = URLConnection.guessContentTypeFromStream(is);
//	}
	if (mtype == null) {
		mtype = "text/plain";
	}
	if (fn.endsWith(".xml")) {
		mtype = "text/xml";
	}
	return mtype;
}
/** Initialize this NamesapceManager on a given resource.
* @param resource the resource to manage
*/
public void initialize(ResourceImpl resource) {
	this.resource = resource;
}
/** Is the managed resource a collection (i.e., is this file a directory)?
* @return true if this resource is a collection, false otherwise
* @exception com.ibm.webdav.WebDAVException
*/
public boolean isCollection() throws WebDAVException {
	File file = new File(ResourceFactory.getRealPath(resource.getURL()));
	if (!file.exists()) {
		throw new ClientException( 404, "File Not Found" );
	}
	return file.isDirectory();
}
/** Is this resource in the lock-null state? In this implementation, does
* this resource have a properties file, but no contents file.
* @return true if this resource is a lock-null resource, false otherwise
* @exception com.ibm.webdav.WebDAVException
*/
public boolean isLockNull() throws WebDAVException {
	File file = new File(ResourceFactory.getRealPath(resource.getURL()));
	String propertiesFileName = ResourceFactory.getRealPath(resource.getURL());
	if (propertiesFileName.endsWith(File.separator)) {
		propertiesFileName = propertiesFileName.substring(0, propertiesFileName.length() - 1);
	}
	propertiesFileName = propertiesFileName + PropertiesManager.propertiesSuffix;
	File propertiesFile = new File(propertiesFileName);
	return !file.exists() && propertiesFile.exists();
}
/** Treat the managed resource as a method and execute it with the given arguments.
* Ths should be done by the host Web server. Executes CGI scripts and Servlets. But
* the repository may have some additional capabilities of its own.
* @param args the URL query string (text following the ?)
* @return the contents of the result
* @exception com.ibm.webdav.WebDAVException
*/
public byte[] performWith(String args) throws WebDAVException {
	// todo: need to get the request entity body too
	StringWriter buf = new StringWriter();

	try {
		Runtime runtime = Runtime.getRuntime();
		Process p = runtime.exec(ResourceFactory.getRealPath(resource.getURL()) + " " + args);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		int ch = -1;
		while ((ch = inputStream.read()) != -1) {
			buf.write(ch);
		}
	} catch (Exception exc) {
		System.err.println("MsxlxServlet exception: " + exc);
	}

	return buf.toString().getBytes();
}
/* (non-Javadoc)
 * @see com.ibm.webdav.impl.NamespaceManager#isVersionable()
 */
public boolean isVersionable() throws WebDAVException {
	// TODO Auto-generated method stub
	return false;
}
/* (non-Javadoc)
 * @see com.ibm.webdav.impl.NamespaceManager#getAllowedMethods()
 */
public List getAllowedMethods() throws WebDAVException {
	// TODO Auto-generated method stub
	return null;
}
/* (non-Javadoc)
 * @see com.ibm.webdav.impl.NamespaceManager#setOrdering(org.w3c.dom.Document)
 */
public void setOrdering(Document orderPatch) {
	// TODO Auto-generated method stub
	
}
/* (non-Javadoc)
 * @see com.ibm.webdav.impl.NamespaceManager#createBinding(java.lang.String, java.net.URL)
 */
public void createBinding(String bindName, URL source) throws WebDAVException {
	// TODO Auto-generated method stub
	
}
/* (non-Javadoc)
 * @see com.ibm.webdav.impl.NamespaceManager#getContentType()
 */
public String getContentType() throws WebDAVException {
	// TODO Auto-generated method stub
	return null;
}
/* (non-Javadoc)
 * @see com.ibm.webdav.impl.NamespaceManager#closeContentsOutputStream(java.lang.String)
 */
public void closeContentsOutputStream(String sContentType) throws WebDAVException {
	// TODO Auto-generated method stub
	
}
}
