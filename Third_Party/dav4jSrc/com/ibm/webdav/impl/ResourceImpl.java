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

package com.ibm.webdav.impl;

//import java.net.URLConnection;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.Collection;
import com.ibm.webdav.protocol.http.*;

/** Implements the Resource interface and all the WebDAV semantics. ResourceImpl
* delegates certain low-level repository operations to managers provided for a
* particular repository implementation. There are three repository managers
* factoring the repository-specific behavior: NamesapceManager, PropertiesManager,
* and LockManager. ResourceImplFactory constructs the appropriate managers for
* this resource based on its URL. Mapping information from URL to repository
* manager is configured in the dav4j.properties file.
* <p>
* ResourceImpl is generally used by a server to implement the WebDAV protocol.
* However, it may also be used directly on the client if the resource URL is the localhost
* and in that case, there are no remote procedure calls, and no need for a
* server to run.
* @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
* @see ResourceImplCollection
* @see ResourceHTTPSkel
*/
public class ResourceImpl implements IRResource {
	/** Setting debug to true causes debug information to be printed to System.err for
	* each method. This value can be changed by setting its value in
	* the dav4j.properties file and restarting the server.
	*/
	public static boolean debug = false;
	public static java.util.Properties webdavProperties =
		new java.util.Properties();
	// properties taken from dav4j.properties.
	private static Vector liveProperties = new Vector();
	// the generic live properties

	static {
		// Find the dav4j.properties file in the classpath
		String classpath = System.getProperty("java.class.path");
		StringTokenizer paths = new StringTokenizer(classpath, ";");
		File propertiesFile = null;
		boolean found = false;

		while (!found && paths.hasMoreTokens()) {
			String path = paths.nextToken();
			propertiesFile = new File(path, "dav4j.properties");
			found = propertiesFile.exists();
		}

		if (found) {
			try {
				webdavProperties.load(new FileInputStream(propertiesFile));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		String debugString = webdavProperties.getProperty("debug");
		debug = (debugString != null) && debugString.equals("true");

		// create the live properties
		liveProperties.addElement(new LockDiscovery());
	}

	// contexts for communicating HTTP and WebDAV headers (method contol couples)
	protected ResourceContext context = new ResourceContext();

	//------------------------------------------------------------------------------------
	protected String fileName = null;
	protected URL url = null;
	protected NamespaceManager namespaceManager = null;
	protected PropertiesManager propertiesManager = null;
	protected LockManager lockManager = null;
	static protected SearchManager searchManager = null;
	static protected UserAuthenticator authenticator = null;

	public ResourceImpl() {
		this.url = null;
		this.fileName = null;
	}

	/** Construct a ResourceImpl for the given URL.
	*
	* @param url the URL of the resource
	* @param localName a translation of the URL (filePortion) into
	* a name that has local meaning to a server.
	* @exception com.ibm.webdav.WebDAVException
	*/
	public ResourceImpl(URL url, String localName) throws WebDAVException {
		this.url = url;
		this.fileName = localName;

		if (url.getProtocol().equals("rmi")) {
			try {
				UnicastRemoteObject.exportObject(this);
			} catch (java.rmi.RemoteException exc) {
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"Unable to export rmi object");
			}
		}

		// TODO: get the Namespace to use from the dav4j.properties file
		// need to use a factory to do this
		namespaceManager = ResourceImplFactory.createNamespaceManager(this);
		propertiesManager =
			ResourceImplFactory.createPropertiesManager(this, namespaceManager);
		lockManager =
			ResourceImplFactory.createLockManager(
				this,
				namespaceManager,
				propertiesManager);
		
		if(searchManager == null) {
			searchManager = ResourceImplFactory.createSearchManager(this);
		}
		
		if(authenticator == null) {
			authenticator = ResourceImplFactory.getAuthenticator(this);
		}
		

		// Set some default response context
		// Don't let proxy servers cache contents or properties
		getResponseContext().put("Cache-Control", "No-Cache");
		getResponseContext().put("Pragma", "No-Cache");

		// identify ourselves
		getResponseContext().put("Server", "IBM DAV4J Server/1.0");
	}

	/** Construct a ResourceImpl for the given URL.
	*
	* @param url the URL of the resource
	* @param localName a translation of the URL (filePortion) into
	* a name that has local meaning to a server.
	* @param targetSelector the revision target selector for this Collection
	* @exception com.ibm.webdav.WebDAVException
	*/
	public ResourceImpl(
		URL url,
		String localName,
		TargetSelector targetSelector)
		throws WebDAVException {
		this.url = url;
		this.fileName = localName;

		if (url.getProtocol().equals("rmi")) {
			try {
				UnicastRemoteObject.exportObject(this);
			} catch (java.rmi.RemoteException exc) {
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"Unable to export rmi object");
			}
		}

		// TODO: get the Namespace to use from the dav4j.properties file
		// need to use a factory to do this
		namespaceManager = ResourceImplFactory.createNamespaceManager(this);
		propertiesManager =
			ResourceImplFactory.createPropertiesManager(this, namespaceManager);
		lockManager =
			ResourceImplFactory.createLockManager(
				this,
				namespaceManager,
				propertiesManager);
		if(searchManager == null) {
			searchManager = ResourceImplFactory.createSearchManager(this);
		}
	
		if(authenticator == null) {
			authenticator = ResourceImplFactory.getAuthenticator(this);
		}

		// Set some default response context
		// Don't let proxy servers cache contents or properties
		getResponseContext().put("Cache-Control", "No-Cache");
		getResponseContext().put("Pragma", "No-Cache");

		// identify ourselves
		getResponseContext().put("Server", "IBM DAV4J Server/1.0");
	}

	/** This method must be called after the client has completed writing to the contents
	 * output stream that was obtained from <code>getContentsOutputStream()</code>.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public void closeContentsOutputStream(ResourceContext context)
		throws WebDAVException {
			closeContentsOutputStream(context,null);
	}

	/** Copy this resource to the destination URL.
	 * Partial results are possible, check the returned status for details.
	 *
	 * @param destinationURL the destination
	 * @param overwrite true implies overrite the destination if it exists
	 * @param propertiesToCopy a collection of properties that must be copied or
	 * the method will fail. propertiesToCopy may have one of the following values:
	 * <ul>
	 *    <li>null - ignore properties that cannot be copied</li>
	 *    <li>empty collection - all properties must be copied or the method will fail</li>
	 *    <li>a collection of URIs - a list of the properties that must be copied
	 *        or the method will fail</li>
	 * </ul>
	 *
	 * @return the status of the copy operation for each resource copied
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus atomicMove(
		ResourceContext context,
		String destinationURL,
		boolean overwrite)
		throws WebDAVException {
		this.context = context;

		setStatusCode(WebDAVStatus.SC_CREATED);

		// create a MultiStatus to hold the results
		MultiStatus multiStatus = new MultiStatus();

		try {
			// validate the uri
			if (!hasValidURI()) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (!exists()) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}

			// the destination may be a relative URL
			URL destURL = new URL(this.url, destinationURL);
			Resource destination = new Resource(destURL.toString());

			// are the source and destination the same?
			if (this.equals(destination)) {
				throw new WebDAVException(
					WebDAVStatus.SC_FORBIDDEN,
					"Can't copy source on top of itself");
			}

			// is the destination locked?
			destination.getRequestContext().precondition(
				getRequestContext().precondition());
			destination.getRequestContext().authorization(
				getRequestContext().authorization());

			if (destination.exists()) {
				if (destination.isLocked()) {
					if (!destination.isLockedByMe()) {
						throw new WebDAVException(
							WebDAVStatus.SC_LOCKED,
							"Destination resource is locked");
					}
				}
			}

			// check to see if the destination exists and its OK to overwrite it
			if (destination.exists()) {
				if (!overwrite) {
					throw new WebDAVException(
						WebDAVStatus.SC_PRECONDITION_FAILED,
						"Destination exists and overwrite not specified");
				} else {
					setStatusCode(WebDAVStatus.SC_NO_CONTENT);
				}
			}

			this.namespaceManager.move(
				URLDecoder.decode(ResourceFactory.getRealPath(destURL)));

			// everything must have gone OK, there
			// is no response for a successful delete
			getResponseContext().contentType("text/xml");
		} catch (WebDAVException exc) {
			throw exc;
		} catch (java.net.MalformedURLException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Malformed URL");
		} catch (java.io.IOException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
				"IO Error");
		}

		return multiStatus;
	}

	/** Copy this resource to the destination URL.
	 * Partial results are possible, check the returned status for details.
	 *
	 * @param destinationURL the destination
	 * @param overwrite true implies overrite the destination if it exists
	 * @param propertiesToCopy a collection of properties that must be copied or
	 * the method will fail. propertiesToCopy may have one of the following values:
	 * <ul>
	 *    <li>null - ignore properties that cannot be copied</li>
	 *    <li>empty collection - all properties must be copied or the method will fail</li>
	 *    <li>a collection of URIs - a list of the properties that must be copied
	 *        or the method will fail</li>
	 * </ul>
	 *
	 * @return the status of the copy operation for each resource copied
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus copy(
		ResourceContext context,
		String destinationURL,
		boolean overwrite,
		Vector propertiesToCopy)
		throws WebDAVException {
		this.context = context;

		setStatusCode(WebDAVStatus.SC_CREATED);

		// create a MultiStatus to hold the results
		MultiStatus multiStatus = new MultiStatus();

		try {
			// validate the uri
			if (!hasValidURI()) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (!exists()) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}

			// the destination may be a relative URL
			URL destURL = new URL(this.url, destinationURL);
			Resource destination = new Resource(destURL.toString());
			
			String sContentType = namespaceManager.getContentType();

			// are the source and destination the same?
			if (this.equals(destination)) {
				throw new WebDAVException(
					WebDAVStatus.SC_FORBIDDEN,
					"Can't copy source on top of itself");
			}

			// is the destination locked?
			destination.getRequestContext().precondition(
				getRequestContext().precondition());
			destination.getRequestContext().authorization(
				getRequestContext().authorization());

			if (destination.exists()) {
				if (destination.isLocked()) {
					if (!destination.isLockedByMe()) {
						throw new WebDAVException(
							WebDAVStatus.SC_LOCKED,
							"Destination resource is locked");
					}
				}
			}

			// check to see if the destination exists and its OK to overwrite it
			if (destination.exists()) {
				if (!overwrite) {
					throw new WebDAVException(
						WebDAVStatus.SC_PRECONDITION_FAILED,
						"Destination exists and overwrite not specified");
				} else {
					setStatusCode(WebDAVStatus.SC_NO_CONTENT);
				}
			}

			InputStream is = getContentsInputStream(context);
			
			
			OutputStream os = destination.getContentsOutputStream();
			if(is != null) {
				byte[] buf = new byte[8192];
				int numRead = 0;
	
				while ((numRead = is.read(buf, 0, buf.length)) != -1) {
					os.write(buf, 0, numRead);
				}
				is.close();
			}
	
			destination.closeContentsOutputStream(sContentType);	

			// copy the properties
			WebDAVStatus savedStatusCode = getStatusCode();
			MultiStatus ms2 = copyProperties(destination, propertiesToCopy);

			if (!ms2.isOK()) {
				// todo: add code here to back out this partial copy. That might require
				//    restoring the resource that used to be at the destination. For now, we'll throw
				//    an exception.
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"problem copying properties");
			}

			setStatusCode(savedStatusCode);

			// remove all locks on the destination. Copy doesn't copy locks
			// become super-user for this operation
			String authorization = getRequestContext().authorization();
			destination.getRequestContext().setBasicAuthorization("root", "");

			Enumeration locks = destination.getLocks().elements();

			while (locks.hasMoreElements()) {
				ActiveLock lock = (ActiveLock) locks.nextElement();

				// ignore exceptions, the unlock should work
				try {
					destination.unlock(lock.getLockToken());
				} catch (Exception exc) {
				}
			}

			destination.getRequestContext().authorization(authorization);

			// everything must have gone OK, there
			// is no response for a successful delete
			getResponseContext().contentType("text/xml");
		} catch (WebDAVException exc) {
			throw exc;
		} catch (java.net.MalformedURLException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Malformed URL");
		} catch (java.io.IOException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
				"IO Error");
		}

		return multiStatus;
	}

	/** Copy the properties of this resource to the destination resource.
	* Follow any keepalive instructions in the propertiesToCopy vector.
	* @param destination the destination resource
	* @param propertiesToCopy properties that must be kept alive at the destination
	 * <ul>
	 *    <li>null - ignore properties that cannot be copied</li>
	 *    <li>empty collection - all properties must be copied or the method will fail</li>
	 *    <li>a collection of URIs - a list of the properties that must be copied
	 *        or the method will fail</li>
	 * </ul>
	* @return a MultiStatus indicating the result of the copy operation
	* @exception com.ibm.webdav.WebDAVException
	*/
	protected MultiStatus copyProperties(
		Resource destination,
		Vector propertiesToCopy)
		throws WebDAVException {
		MultiStatus result = getProperties(context);
		boolean bOnlySomeProperties =
			((propertiesToCopy != null) && (propertiesToCopy.size() > 0));

		// create a property update document
		Document document = null;

		try {
			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder docbuilder = factory.newDocumentBuilder();
			document = docbuilder.newDocument();
		} catch (Exception e) {
			throw new WebDAVException(
				WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
				e.getMessage());
		}

		//document.setVersion(Resource.XMLVersion);
		//document.setEncoding(Resource.defaultXMLEncoding);
		Element propertyUpdate =
			document.createElementNS("DAV:", "D:propertyupdate");

		propertyUpdate.setAttribute("xmlns:D", "DAV:");
		document.appendChild(propertyUpdate);

		Element set = document.createElementNS("DAV:", "D:set");

		propertyUpdate.appendChild(set);

		Element prop = document.createElementNS("DAV:", "D:prop");

		set.appendChild(prop);

		Hashtable PropsWillCopy = new java.util.Hashtable();

		// fill in the properties from the source
		PropertyResponse response =
			(PropertyResponse) result.getResponses().nextElement();
		Enumeration propertyNames = response.getPropertyNamesPN();

		while (propertyNames.hasMoreElements()) {
			PropertyName name = (PropertyName) propertyNames.nextElement();
			PropertyValue value = response.getProperty(name);
			Node node = document.importNode(value.value, true);
			PropsWillCopy.put(name, node);
			prop.appendChild((Element) node);
		}

		// attempt to update all the properties at the destination
		MultiStatus msRc = destination.setProperties(document);

		// now look at what happened, and adjust based on the propertiesToCopy
		Enumeration resources = msRc.getResponses();

		while (resources.hasMoreElements()) {
			Response resmember = (Response) resources.nextElement();
			PropertyResponse propresponse = resmember.toPropertyResponse();
			Dictionary htProperties =
				(Hashtable) propresponse.getPropertiesByPropName();
			Enumeration propertynames = htProperties.keys();

			while (propertynames.hasMoreElements()) {
				PropertyName propname =
					(PropertyName) propertynames.nextElement();
				PropertyValue pv = (PropertyValue) htProperties.get(propname);
				int stat = pv.getStatus();

				if ((stat != WebDAVStatus.SC_OK)
					&& (stat != WebDAVStatus.SC_FAILED_DEPENDENCY)) {
					Node node = (Node) PropsWillCopy.get(propname);

					if ((propertiesToCopy == null)
						|| (propertiesToCopy.size() > 0
							&& !propertiesToCopy.contains(propname))) {
						prop.removeChild(node); // don't need to copy this one
					}
				}
			}
		}

		// attempt to update the remaining properties again
		// after removing the ones that can be allowed to fail.
		// This has to be a two step process because there's no
		// way to determine what properties are live on anoter
		// server. We're trying to get a propertyupdate element
		// on PROPPATCH too so this extra step can be avoided.
		return destination.setProperties(document);
	}

	/** Create a instance of a ResourceImpl with the given URL and localName.
	*
	* @param url the URL of the resource to create
	* @param localName the name of the resource on the server machine
	* @return a ResourceImpl or one of its subclasses.
	* @exception com.ibm.webdav.WebDAVException
	*/
	public static ResourceImpl create(URL url, String localName)
		throws WebDAVException {
		ResourceImpl resource = new ResourceImpl(url, localName);

		if (resource.isCollection()) {
			resource = new CollectionImpl(url, localName, null);
		}

		return resource;
	}

	/** Build a multistatus property response that returns the error specified
	by the given exception.  It should return this error for every property provided
	in the specified document which should represent the XML of a PROPPATCH
	request.
	 *
	 * @param exc an exception that describes the error to be placed in the MultiStatus created.
	 * @param updates an XML Document containing DAV:propertyupdate elements
	 * describing the edits that were request orginally requested and
	 * apparently generated the given exception.
	 * @return a MultiStatus indicating the status of the updates
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus createPropPatchMultiStatus(
		WebDAVException exc,
		Document updates)
		throws WebDAVException {
		MultiStatus ms2 = new MultiStatus();
		Element el0 = updates.getDocumentElement();
		Element txel0 = (Element) el0;
		NodeList nl = txel0.getElementsByTagNameNS("DAV:", "prop");

		/*Element na[] = txel0.searchDescendantsAll(  Match.NSLOCAL, //Match.QNAME,
		"DAV:", "prop" );*/
		int nllen = nl.getLength();
		int idx = 0;
		String lxx = "xx";

		if (nllen <= 0) {
			throw exc;
		}

		while (idx < nllen) {
			Element txelProp = (Element) nl.item(idx);
			Node node2 = txelProp.getFirstChild();
			Element txel2 = null;

			try {
				txel2 = (Element) node2;
			} catch (Exception exc2) {
				throw exc;
			}

			{
				// todo: add code to handle the responsedescription in the exception and
				//     include it in the multistatus response.
				PropertyName pn = new PropertyName(txel2);
				PropertyResponse response =
					new PropertyResponse(getURL().toString());
				response.addProperty(
					pn,
					(Element) txel2.cloneNode(false),
					exc.getStatusCode());
				ms2.addResponse(response);
			}

			idx++;
		}

		return ms2;
	}

	/** Delete this resouce from the server. The actual effect of the delete operation is
	 * determined by the underlying repository manager. The visible effect to WebDAV
	 * is that the resource is no longer available.
	 *
	 * @return a MultiStatus containing the status of the delete method on each
	 *         effected resource.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus delete(ResourceContext context) throws WebDAVException {
		this.context = context;

		setStatusCode(WebDAVStatus.SC_NO_CONTENT);

		MultiStatus result = new MultiStatus();

		// validate the uri
		if (!hasValidURI()) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid URI");
		}

		// make sure the parent collection exists
		CollectionImpl parent = (CollectionImpl) getParentCollection();

		if ((parent != null) && !parent.exists()) {
			throw new WebDAVException(
				WebDAVStatus.SC_CONFLICT,
				"Parent collection does not exist");
		}
		
			// TODO support shared locks here or wherever needs it
		  /*
		   * 
	 
		   QUICK FIX
	 
		   Commenting this out to allow addition of members to locked collections
	 
		   Will have to implement the shared lock thing eventually to support this
	 

		// make sure the parent collection is not locked, or is locked by this user
		if (parent != null) {
			parent.getRequestContext().precondition(
				getRequestContext().precondition());
			parent.getRequestContext().authorization(
				getRequestContext().authorization());

			if (parent.isLocked() && !parent.isLockedByMe()) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Parent collection is locked by another user");
			}
		}
		*/

		if (!exists()) {
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		// check to see if the resource is locked by another user
		if (isLocked() && !isLockedByMe()) {
			throw new WebDAVException(
				WebDAVStatus.SC_LOCKED,
				"Resource is locked by another user");
		}

		// reset statusCode because isLocked has a side effect of changing it
		//    to 207 multistatus.
		setStatusCode(WebDAVStatus.SC_NO_CONTENT);

		// Attempt to delete this resource
		namespaceManager.delete();
		propertiesManager.deleteProperties();
		getResponseContext().contentType("text/xml");

		return result;
	}

	/** Unlock the lock identified by the lockToken on this resource. This method
	 * is used internally to unlock resources copied or moved as well as unlocked.
	 *
	 * @param lockToken the lock token obtained from the ActiveLock of a previous <code>lock() </code>
	 *     or <code>getLocks()</code>.
	 *
	 * @return a MultiStatus containing any responses on resources that could not
	 *     be unlocked.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	protected MultiStatus doUnlock(String lockToken) throws WebDAVException {
		String principal = getRequestContext().getAuthorizationId();

		// get the locks on this resource
		Enumeration locks = getLocks().elements();

		// find the lock to unlock
		ActiveLock lockToRemove = null;

		while (locks.hasMoreElements()) {
			ActiveLock activeLock = (ActiveLock) locks.nextElement();
			
			if (activeLock.getLockToken().equals(lockToken)
				&& (activeLock.getPrincipal().equals(principal)
					|| principal.equals("root")
					|| authenticator.isSuperUser(this) == true)) {
				lockToRemove = activeLock;

				break;
			}
		}

		if (lockToRemove == null) {
			throw new WebDAVException(
				WebDAVStatus.SC_PRECONDITION_FAILED,
				"resource is not locked by this principal");
		}

		MultiStatus result = lockManager.unlock(lockToRemove);

		// delete a lock-null resource that has no remaining activelocks
		locks = getLocks().elements();

		if (!locks.hasMoreElements() && namespaceManager.isLockNull()) {
			propertiesManager.deleteProperties();
		}

		getResponseContext().contentType("text/xml");

		return result;
	}

	/** See if the contents of this resource exists. A resource exists
	 * if it has contents or state maintained by a server.
	 *
	 * @return true if the contents exists, false otherwise
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public boolean exists() throws WebDAVException {
		return namespaceManager.exists();
	}

	public boolean authenticateUser(String user, String pwd)
		throws WebDAVException {
		boolean bIsAuthenticated = true;

		if (authenticator != null) {
			bIsAuthenticated = authenticator.authenticate(user, pwd);
		}

		return bIsAuthenticated;
	}

	/** Get the active lock on this resource owned by the given principal if any.
	 * NOTE: this method cannot be reliably implemented based on version 10 of
	 * the WebDAV spec as an activelock element in a lockdiscovery does not contain
	 * the authorization credentials of the owner of the lock. For now, this method
	 * relies on an additional principal element in the activelock that contains
	 * the required id. This is an IBM EXTENSTION. When WebDAV ACLs are introduced,
	 * the principal will likely be added to the activelock element.
	 *
	 * @param principal the authorization id of the requesting principal
	 *
	 * @return the active lock owned by that principal or null if the resource is
	 * not locked by that principal.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	protected ActiveLock getActiveLockFor(
		String scope,
		String type,
		int timeout,
		Element owner)
		throws WebDAVException {
		String principal = getRequestContext().getAuthorizationId();

		if (principal == null) {
			throw new WebDAVException(
				WebDAVStatus.SC_UNAUTHORIZED,
				"missing authorization identification");
		}

		// check all the parameters
		if ((scope == null)
			|| (!scope.equals(ActiveLock.exclusive)
				&& !scope.equals(ActiveLock.shared))) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"unsupported or missing lock scope: " + scope);
		}

		if ((type == null) || !type.equals(ActiveLock.writeLock)) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"unsupported or missing lock type: " + type);
		}

		// handle locking non-existant resources
		if (!exists()) {
			namespaceManager.createLockNullResource();
		}

		// We extend ActiveLock  to include the principal and expiration date
		ActiveLock activeLock = new ActiveLock();
		activeLock.setScope(scope);
		activeLock.setLockType(type);
		activeLock.setDepth(Collection.shallow);

		if (owner != null) {
			activeLock.setOwner(owner);
		}

		if (timeout < 0) {
			activeLock.setTimeout("Infinite");
		} else {
			activeLock.setTimeout("Second-" + timeout);
		}

		String lockToken = "opaquelocktoken:" + new UUID();
		activeLock.setLockToken(lockToken);
		activeLock.setPrincipal(principal);

		return activeLock;
	}

	/** Get an InputStream for accessing the contents of this resource. This method may provide
	 * more efficient access for resources that have large contents. Clients may want to create
	 * a Reader to perform appropriate character conversions on this stream.
	 *
	 * @return an InputStream on the contents
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public InputStream getContentsInputStream(ResourceContext context)
		throws WebDAVException {
		this.context = context;

		// validate the uri
		if (!hasValidURI()) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid URI");
		}

		InputStream is = namespaceManager.getContentsInputStream();

		return is;
	}

	/** Get an OutputStream for setting the contents of this resource. This method may provide
	 * more efficient access for resources that have large contents. Remember to call
	 * closeContentsOutputStream() when all the data has been written.
	 *
	 * @return an OutputStream to set the contents
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public OutputStream getContentsOutputStream(ResourceContext context)
		throws WebDAVException {
		this.context = context;

		// validate the uri
		if (!hasValidURI()) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid URI");
		}

		// make sure the parent collection exists
		CollectionImpl parent = (CollectionImpl) getParentCollection();

		if ((parent != null) && !parent.exists()) {
			throw new WebDAVException(
				WebDAVStatus.SC_CONFLICT,
				"Parent collection does not exist");
		}

		//TODO support shared locks here or wherever needs it
		/*
		 * 
		 
		 QUICK FIX
		 
		 Commenting this out to allow addition of members to locked collections
		 
		 Will have to implement the shared lock thing eventually to support this
		 
		 
		// make sure the parent collection is not locked, or is locked by this user
		if (parent != null) {
			// use this resource's precondition, it should contain the
			// parent locktoken if needed
			parent.getRequestContext().precondition(
				getRequestContext().precondition());
			parent.getRequestContext().authorization(
				getRequestContext().authorization());

			if (parent.isLocked() && !parent.isLockedByMe()) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Parent collection is locked by another user");
			}
		}*/

		// check to see if the resource is locked by another user
		if (exists() && isLocked() && !isLockedByMe()) {
			throw new WebDAVException(
				WebDAVStatus.SC_LOCKED,
				"Resource is locked by another user");
		}

		// Resources that already exist are overwritten
		return namespaceManager.getContentsOutputStream();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/14/2000 4:14:55 PM)
	 * @return com.ibm.webdav.ResourceContext
	 */
	public com.ibm.webdav.ResourceContext getContext() {
		return context;
	}

	/** Return lock manager for this resource
	*/
	public LockManager getLockManager() {
		return lockManager;
	}

	/**Return authenticator for this resource
	 *
	 */
	public UserAuthenticator getUserAuthenticator() {
		return ResourceImpl.authenticator;
	}

	/** Get the locks that exist on this resource.
	 *
	 * @return a Vector of ActiveLock objects
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public Vector getLocks() throws WebDAVException {
		return lockManager.getLocks();
	}

	/** This method can be used for obtaining meta-information about this resource without
	 * actually reading the resource contents. This meta-information is maintained by the server
	 * in addition to the resource properties.</p>
	 * <p>
	 * After this call, the resource context has been updated and
	 * <code>getStatusCode()</code>, <code>getStatusMessage()</code>, and <code>getResponseContext()</code>
	 * as well as all the ResourceContext methods return updated values based on the current
	 * state of the resource.</p>
	 * <p>This methods corresponds to the HTTP HEAD method.</p>
	 * <p>
	 * Do a getContentsInputStream() to set the response context,
	 * then just don't return the stream.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public void getMetaInformation(ResourceContext context)
		throws WebDAVException {
		this.context = context;

		InputStream is = getContentsInputStream(context);

		try {
			is.close();
		} catch (WebDAVException exc) {
			throw exc;
		} catch (java.io.IOException exc) {
		}
	}

	/** Return the local name of the resource. What this name actually is
	* depends on the interpretation of the resource URL by the namespace
	* manager servicing it. Repository implementations are free to translate
	* the URL any way they want for their own purposes. For example, the
	* file system implementation uses the doc.root property to translate
	* the file part of the URL into a full pathname.
	* @return the repository specific name for this resource
	* @exception com.ibm.webdav.WebDAVException
	*/
	public String getName() throws WebDAVException {
		if ((fileName == null) && (url != null)) {
			fileName = ResourceFactory.getRealPath(url);
		}

		String sDecoded = null;
		try {
			sDecoded = URLDecoder.decode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new WebDAVException(
				WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
				e.getLocalizedMessage());
		}

		return sDecoded;
	}

	/** Get the collection containing this resource.
	*
	* @return the parent collection
	* @exception com.ibm.webdav.WebDAVException
	*/
	public IRCollection getParentCollection() throws WebDAVException {
		String parentURL = getURL().toString();
		String parentLocalName = getName();
		int delimiterPosition = 0;
		
		if(namespaceManager instanceof VersionedNamespaceManager) {
			if(((VersionedNamespaceManager)namespaceManager).isVersionURL(parentURL) == true) {
				parentURL = ((VersionedNamespaceManager)namespaceManager).getResourceURL();
				try {
					parentLocalName = ResourceFactory.getRealPath(new URL(url,parentURL));
				} catch (MalformedURLException e) {
					throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR,e.getLocalizedMessage());
				}
			}
		}
		

		if (parentURL.endsWith("/")) {
			delimiterPosition =
				parentURL.substring(0, parentURL.length() - 1).lastIndexOf("/");
		} else {
			delimiterPosition = parentURL.lastIndexOf("/");
		}

		parentURL = parentURL.substring(0, delimiterPosition + 1);

		if (parentLocalName.endsWith(File.separator)) {
			delimiterPosition =
				parentLocalName.substring(
					0,
					parentLocalName.length() - 1).lastIndexOf(
					File.separator);
		} else {
			delimiterPosition = parentLocalName.lastIndexOf(File.separator);
		}

		parentLocalName = parentLocalName.substring(0, delimiterPosition + 1);

		CollectionImpl parent = null;

		try {
			URL url = new URL(getURL(),parentURL);
			parent = new CollectionImpl(url, parentLocalName, null);
		} catch (java.net.MalformedURLException exc) {
			exc.printStackTrace();
		}
		
		return parent;
	}

	/** Get the URL of the collection containing this resource.
	*
	* @return the parent collection URL, always ending in a separator
	* @exception com.ibm.webdav.WebDAVException
	*/
	public URL getParentURL() throws WebDAVException {
		String uri = getURL().getFile();
		int delimiterPosition = 0;

		if (uri.endsWith("/")) {
			delimiterPosition =
				uri.substring(0, uri.length() - 1).lastIndexOf("/");
		} else {
			delimiterPosition = uri.lastIndexOf("/");
		}

		URL parentURL = null;

		try {
			parentURL =
				new URL(getURL(), uri.substring(0, delimiterPosition + 1));
		} catch (java.net.MalformedURLException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Malformed URL");
		}

		return parentURL;
	}

	public MultiStatus getSearchSchema(
		ResourceContext context,
		SearchRequest searchReq)
		throws WebDAVException {
		this.context = context;

		// create a MultiStatus to hold the results
		MultiStatus results = new MultiStatus();

		// create a document
		Document document = null;

		try {
			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder docbuilder = factory.newDocumentBuilder();
			document = docbuilder.newDocument();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new WebDAVException(
				WebDAVStatus.SC_PROCESSING,
				e.getMessage());
		}

		SearchSchema schema = searchManager.getSearchSchema(searchReq);

		SchemaResponse response =
			new SchemaResponse(document, schema, searchReq.getScopeURI());

		results.addResponse(response);

		return results;
	}

	public MultiStatus executeSearch(
		ResourceContext context,
		SearchRequest searchReq)
		throws WebDAVException {
		this.context = context;

		// create a MultiStatus to hold the results
		MultiStatus result = new MultiStatus();

		if (searchManager.validate(searchReq)) {
			Vector resources = searchManager.executeSearch(searchReq, this);

			// now get the properties of the members if necessary
			Enumeration members = resources.elements();

			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();

				try {
					MultiStatus memberResult = null;

					if (member.isCollection()) {
						if (searchReq.isAllSelectProperties()) {
							memberResult =
								((CollectionImpl) member).getProperties(
									context,
									Collection.thisResource);
						} else {
							memberResult =
								((CollectionImpl) member).getProperties(
									context,
									(PropertyName[]) searchReq
										.getSelectProperties()
										.toArray(
										new PropertyName[0]),
									Collection.thisResource);
						}
					} else {
						if (searchReq.isAllSelectProperties()) {
							memberResult = member.getProperties(context);
						} else {
							memberResult =
								member.getProperties(
									context,
									(PropertyName[]) searchReq
										.getSelectProperties()
										.toArray(
										new PropertyName[0]));
						}
					}

					result.mergeWith(memberResult);
				} catch (WebDAVException exc) {
					MethodResponse response =
						new MethodResponse(
							member.getURL().toString(),
							exc.getStatusCode());
					result.addResponse(response);
				} catch (Exception e) {
					e.printStackTrace();
					throw new WebDAVException(
						WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
						"unable to get properties");
				}
			}
		} else {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid query");
		}

		getResponseContext().contentType("text/xml");

		return result;
	}

	/** Get all the properties of this resource.
	 *
	 * @return a MultiStatus of PropertyResponses. It should contain only one
	 * response element.
	 * @see com.ibm.webdav.MultiStatus
	 * @see com.ibm.webdav.PropertyResponse
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus getProperties(ResourceContext context)
		throws WebDAVException {
		this.context = context;

		// make sure the resource exists.
		if (!(exists() || namespaceManager.isLockNull())) {
			
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		setStatusCode(WebDAVStatus.SC_MULTI_STATUS);
		getResponseContext().contentType("text/xml");

		return propertiesManager.getProperties();
	}

	/** Get the named properties of this resource.
	 *
	 * @param names an arrary of property names to retrieve
	 *
	 * @return a MultiStatus of PropertyResponses
	 * @exception com.ibm.webdav.WebDAVException
	 * @see com.ibm.webdav.PropertyResponse
	 */
	public MultiStatus getProperties(
		ResourceContext context,
		PropertyName[] names)
		throws WebDAVException {
		this.context = context;

		// make sure the resource exists.
		if (!(exists() || namespaceManager.isLockNull())) {
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		getResponseContext().contentType("text/xml");

		return propertiesManager.getProperties(names);
	}

	/** Get the value of the given property for this resource.
	 *
	 * @param name the name of the property to retrieve
	 *
	 * @return PropertyValue or null if the resource does not have the requested property
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public PropertyValue getProperty(PropertyName name)
		throws WebDAVException {
		PropertyName[] names = new PropertyName[1];
		names[0] = name;

		Enumeration responses = getProperties(context, names).getResponses();
		PropertyResponse response = (PropertyResponse) responses.nextElement();
		Dictionary properties = response.getPropertiesByPropName();

		return (PropertyValue) properties.get(name);
	}

	/** Get the names of all properties for this resource. The result is similar to
	 * getProperties(), but the properties have no values.
	 *
	 * @return a MultiStatus of PropertyResponses
	 * (PropertyValue.value is always null, PropertyValue.status contains the status)
	 * @exception com.ibm.webdav.WebDAVException
	 * @see com.ibm.webdav.PropertyResponse
	 */
	public MultiStatus getPropertyNames(ResourceContext context)
		throws WebDAVException {
		this.context = context;

		// make sure the resource exists.
		if (!(exists() || namespaceManager.isLockNull())) {
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		getResponseContext().contentType("text/xml");

		return propertiesManager.getPropertyNames();
	}

	/** Get the request context for this resource. The context contains information
	 * used by methods on a resource when the method is called.
	 *
	 * @return the HTTPHeaders providing information that controls
	 * method execution.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public HTTPHeaders getRequestContext() throws WebDAVException {
		return context.getRequestContext();
	}

	/** Get the response context for this resource. The context contains information
	 * returned from invocations of methods on a resource.
	 *
	 * @return the HTTPHeaders providing information that
	 * is returned by method execution.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public HTTPHeaders getResponseContext() throws WebDAVException {
		return context.getResponseContext();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/13/2000 8:53:11 PM)
	 * @return com.ibm.webdav.WebDAVStatus
	 */
	public com.ibm.webdav.WebDAVStatus getStatusCode() {
		return context.getStatusCode();
	}

	/** Get the name that identifies this resource.
	 *
	 * @return the URL for this resource
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public URL getURL() throws WebDAVException {
		return url;
	}

	/** Is this resource locked with the given lock token?
	 * @param lockToken the lock token to check for
	 * @exception com.ibm.webdav.WebDAVException
	 */
	protected boolean hasLock(String lockToken) throws WebDAVException {
		boolean hasLock = false;
		Enumeration locks = getLocks().elements();

		while (!hasLock && locks.hasMoreElements()) {
			ActiveLock lock = (ActiveLock) locks.nextElement();
			hasLock = lock.getLockToken().equals(lockToken);
		}

		return hasLock;
	}

	/** Check for a valid URI
	* @return true if this resource has a valid URI
	* @exception com.ibm.webdav.WebDAVException
	*/
	public boolean hasValidURI() throws WebDAVException {
		return getName() != null;
	}

	/** Inherit all deep locks on the parent of this resource.
	* @exception com.ibm.webdav.WebDAVException
	*/
	protected void inheritParentDeepLocks() throws WebDAVException {
		CollectionImpl parent = (CollectionImpl) getParentCollection();
		
		Enumeration parentLocks = parent.getLocks().elements();

		while (parentLocks.hasMoreElements()) {
			ActiveLock parentLock = (ActiveLock) parentLocks.nextElement();

			if (parentLock.getDepth().equals(Collection.deep)) {
				if (!hasLock(parentLock.getLockToken())) {
					lock(parentLock);
				}
			}
		}
	}

	/** Returns true if this Resource is a collection. Returns false otherwise.
	 *
	 * @return true if this Resource is a collection.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public boolean isCollection() throws WebDAVException {
		return namespaceManager.isCollection();
	}

	/** See if this resource is locked.
	 *
	 * @return true if this resource is locked, false otherwise.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public boolean isLocked() throws WebDAVException {
		// see if there are any active locks
		return !getLocks().isEmpty();
	}

	/** Is this resource locked by the current authorized user? That is, does the
	 * current user have sufficient locking access to modify this resource. The
	 * method, like all methods that do modify the resource, must have a precondition
	 * set in the context containing the lock token of the resource owned by this
	 * user. The user is set in the request context using the authorization method.
	 * @return true if this resource is locked by the principal in the context
	 *    sufficient to modify the resource.
	 * @exception com.ibm.webdav.WebDAVException
	 * @see com.ibm.webdav.ResourceContext#authorization
	 */
	public boolean isLockedByMe() throws WebDAVException {
		String principal = getRequestContext().getAuthorizationId();
		Precondition precondition = getRequestContext().precondition();

		if (precondition == null) {
			return false; // it is not locked by me, or the server doesn't know

			//throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Missing If header containing lock token");
		}

		// get the locks on this resource
		Enumeration locks = getLocks().elements();
		boolean isLockedByMe = false;

		// look for a matching lock
		while (locks.hasMoreElements()) {
			ActiveLock activeLock = (ActiveLock) locks.nextElement();
			Condition condition = new Condition(getURL().getFile());
			ConditionTerm term = new ConditionTerm();
			StateToken stateToken = new StateToken(activeLock.getLockToken());
			term.addConditionFactor(stateToken);
			condition.addConditionTerm(term);

			
			if (precondition.matches(condition)
				&& activeLock.getPrincipal().equals(principal)
				&& activeLock.getLockType().equals(ActiveLock.writeLock)) {
				isLockedByMe = true;

				break;
			}
		}

		return isLockedByMe;
	}

	/** See if the target URL has the same host and port (e.g., the same server)
	* as this resource. Matches on the host name, not its Internet address.
	*
	* @target the URL of the target resource
	*
	* @return true if the target is supported by the same server
	*/
	public boolean isSameServerAs(URL target) {
		return target.getHost().equals(url.getHost())
			&& (target.getPort() == url.getPort());
	}

	/** Load properties from the properties manager and update live properties.
	*
	* @return an XML document containing a properties element.
	* @exception com.ibm.webdav.WebDAVException
	*/
	public Document loadProperties() throws WebDAVException {
		Document propertiesDocument = propertiesManager.loadProperties();
		updateLiveProperties(propertiesDocument);
		if (propertiesManager instanceof VersionedPropertiesManager) {
			if (((VersionedNamespaceManager) namespaceManager).isVersioned()
				== true) {
				((VersionedPropertiesManager) propertiesManager).updateVersionProperties(propertiesDocument);
			}
		}

		return propertiesDocument;
	}

	/** Lock this resource with the information contained in the given active lock.
	* @param activeLock information about the lock
	* @return a MultiStatus containing a lockdiscovery property indicating
	* @exception com.ibm.webdav.WebDAVException
	*/
	protected MultiStatus lock(ActiveLock activeLock) throws WebDAVException {
		// get the locks on this resource
		Enumeration locks = getLocks().elements();

		while (locks.hasMoreElements()) {
			ActiveLock lock = (ActiveLock) locks.nextElement();

			if (lock.getScope().equals(ActiveLock.exclusive)) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Resource has an exclusive lock");
			}

			if (lock.getScope().equals(ActiveLock.shared)
				&& activeLock.getScope().equals(ActiveLock.exclusive)) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Resource already has a shared lock");
			}

			if (lock.getScope().equals(ActiveLock.shared)
				&& lock.getPrincipal().equals(activeLock.getPrincipal())) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"The principal already has a lock on this resource");
			}
		}

		return lockManager.lock(activeLock);
	}

	/** Lock this resource based on the given parameters. This allows control of
	 * the lock scope (exclusive or shared) the lock type (write), owner information, etc.
	 *
	 * @param scope the scope of the lock, exclusive or shared
	 * @param type the type of the lock, currently only write
	 * @param timeout the number of seconds before the lock times out or
	 *     -1 for infinite timeout.
	 * @param owner an XML element containing useful information that can be
	 *     used to identify the owner of the lock. An href to a home page, an
	 *     email address, phone number, etc. Can be null if no owner information
	 *     is provided.
	 *
	 * @return a MultiStatus containing a lockdiscovery property indicating
	 * the results of the lock operation.
	 * @exception com.ibm.webdav.WebDAVException
	*/
	public MultiStatus lock(
		ResourceContext context,
		String scope,
		String type,
		int timeout,
		Element owner)
		throws WebDAVException {
		this.context = context;

		ActiveLock activeLock = getActiveLockFor(scope, type, timeout, owner);
		MultiStatus result = lock(activeLock);

		// return the granted lock token in the lockToken response context
		getResponseContext().lockToken(activeLock.getLockToken());
		getResponseContext().contentType("text/xml");

		return result;
	}

	/** Startup an RMI server for a ResourceImpl. The URL would most likely be for
	* some root WebDAV collection corresponding to the doc.root of a typical web
	* server. The resource identified by the URL must exist.
	* @param args root URL, its local file pathname
	*/
	public static void main(String[] args) {
		// get the URL of the resource to export
		if (args.length != 2) {
			System.err.println("Usage: java ResourceImpl url localName");
			System.exit(-1);
		}

		// Create a ResourceImpl, export it, and bind it in the RMI Registry
		try {
			URL url = new URL(args[0]);
			String localName = args[1];

			// Create and install a security manager
			System.setSecurityManager(new RMISecurityManager());

			ResourceImpl resource = ResourceImpl.create(url, localName);

			// strip the protocol off the URL for the registered name
			String name = url.toString();
			name = name.substring(name.indexOf(":") + 1);
			Naming.rebind(name, resource);
			
		} catch (Exception exc) {
			System.err.println("ResourceImpl error: " + exc.getMessage());
			exc.printStackTrace();
			System.exit(-1);
		}
	} // main

	public MultiStatus createBinding(
		ResourceContext context,
		String bindName,String resourceURI)
		throws WebDAVException {
		this.context = context;

		setStatusCode(WebDAVStatus.SC_CREATED);

		MultiStatus result = new MultiStatus();

		try {
			// validate the uri
			if (!hasValidURI()) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (!exists()) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}
			
			//make sure the resource is a collection
			if(isCollection() == false) {
				throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST,"URI indentified in BIND request must be an existing collection");
			}

			// the destination may be a relative URL
			StringBuffer strbuf = new StringBuffer();
			strbuf.append(url.toExternalForm());
			
			if(strbuf.toString().endsWith("/") == false) {
				strbuf.append("/");
			}
			strbuf.append(bindName);
			
			Resource destination = new Resource(strbuf.toString());
			
			Resource bindSource = new Resource(resourceURI);

			// are the source and destination the same?
			if (bindSource.equals(destination)) {
				throw new WebDAVException(
					WebDAVStatus.SC_FORBIDDEN,
					"Can't copy source on top of itself");
			}

			destination.getRequestContext().precondition(
				getRequestContext().precondition());
			destination.getRequestContext().authorization(
				getRequestContext().authorization());

			if (destination.exists()) {
				throw new WebDAVException(
					WebDAVStatus.SC_PRECONDITION_FAILED,
					"Destination exists and overwrite not specified");
			}

			this.namespaceManager.createBinding(bindName,new URL(resourceURI));

			// everything must have gone OK, there
			// is no response for a successful delete
			getResponseContext().contentType("text/xml");
		} catch (WebDAVException exc) {
			throw exc;
		} catch (java.net.MalformedURLException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Malformed URL");
		} catch (java.io.IOException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
				"IO Error");
		}

		return result;
	}

	/** Move this resource to the destination URL.
	 * Partial results are possible, check the returned status for details
	 *
	 * @param destinationURL the destination
	 * @param overwrite true implies overrite the destination if it exists
	 * @param propertiesToMove a collection of properties that must be moved or
	 * the method will fail. propertiesToMove may have one of the following values:
	 * <ul>
	 *    <li>null - ignore properties that cannot be moved</li>
	 *    <li>empty collection - all properties must be moved or the method will fail</li>
	 *    <li>a collection of URIs - a list of the properties that must be moved
	 *        or the method will fail</li>
	 * </ul>
	 *
	 * @return the status of the move operation for each resource moved
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus move(
		ResourceContext context,
		String destinationURL,
		boolean overwrite,
		Vector propertiesToMove)
		throws WebDAVException {
		this.context = context;

		MultiStatus result = new MultiStatus();

		if (webdavProperties
			.getProperty("atomicmove")
			.equalsIgnoreCase("true")) {
			
			// check to see if the resource is locked by another user
			if (isLocked() && !isLockedByMe()) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Resource is locked by another user");
			}
			
			result = atomicMove(context, destinationURL, overwrite);

			Enumeration propertyNames = context.getResponseContext().keys();
		} else {
			// proceed with the move even if the source is locked by
			// another principal. Best effort will perform the copy but
			// not the delete.
			result = copy(context, destinationURL, overwrite, propertiesToMove);

			ResourceContext copyContext = context;
			WebDAVStatus oldStatus = context.getStatusCode();
			result.mergeWith(delete(context));

			if (getStatusCode().getStatusCode() == 204) {
				// it was a clean delete that doesn't affect the
				//     final result code.
				context.setStatusCode(oldStatus);
			}

			Enumeration propertyNames = copyContext.getResponseContext().keys();

			while (propertyNames.hasMoreElements()) {
				String name = (String) propertyNames.nextElement();
				String value =
					(String) copyContext.getResponseContext().get(name);

				if (!context.getResponseContext().containsKey(name)) {
					getResponseContext().put(name, value);
				}
			}
		}

		getResponseContext().contentType("text/xml");

		return result;
	}

	/** Is the parent of this resource depth locked with the given lock token?
	* @param lockToken the lock token to check
	* @return true if the parant of this resource is locked with the lock token
	* @exception com.ibm.webdav.WebDAVException
	*/
	public boolean parentIsLockedWith(String lockToken)
		throws WebDAVException {
		// get the locks on the parent of this resource
		boolean isLocked = false;
		CollectionImpl parent = null;

		try {
			parent = (CollectionImpl) getParentCollection();
		} catch (Exception exc) {
		}

		if (parent != null) {
			Enumeration locks = parent.getLocks().elements();

			// look for a matching lock
			while (locks.hasMoreElements()) {
				ActiveLock activeLock = (ActiveLock) locks.nextElement();

				if (activeLock.getLockToken().equals(lockToken)) {
					isLocked = true;

					break;
				}
			}
		}

		return isLocked;
	}

	/** This method treats this resource as a method or service, and sends its parameter to
	 * this resource where it is handled in a resource-specific way. For example,
	 * sending data from an HTML form to a URL representing a Servlet or CGI script that processes
	 * the form data to produce some result.
	 *
	 * @param args a string representing the arguments to the method represented by this URL. The
	 * arguments are in the form ?parameterName1=value1&amp;parameterName2=value2... as specified
	 * for URL queries.
	 *
	 * @return the results of sending the arguments to the URL
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public byte[] performWith(ResourceContext context, String args)
		throws WebDAVException {
		this.context = context;

		return namespaceManager.performWith(args);
	}

	/** Refresh the lock on this resource by resetting the lock timeout.
	 * The context must contain the proper authorization for the requesting
	 * principal.
	 *
	 * @param lockToken the lock token identifying the lock.
	 * @param timeout the new timeout in seconds. -1 means infinite timeout.
	 *
	 * @return updated information about the lock status of this resource
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus refreshLock(
		ResourceContext context,
		String lockToken,
		int timeout)
		throws WebDAVException {
		this.context = context;

		String principal = getRequestContext().getAuthorizationId();

		// find the lock
		ActiveLock lockToRefresh = null;
		Enumeration activeLocks = getLocks().elements();

		while (activeLocks.hasMoreElements()) {
			ActiveLock activeLock = (ActiveLock) activeLocks.nextElement();

			if (activeLock.getLockToken().equals(lockToken)
				&& (activeLock.getPrincipal().equals(principal)
					|| principal.equals("root")
					|| ResourceImpl.authenticator.isSuperUser(this) == true)) {
				lockToRefresh = activeLock;

				break;
			}
		}

		if (lockToRefresh == null) {
			throw new WebDAVException(
				WebDAVStatus.SC_PRECONDITION_FAILED,
				"principal does not own a lock");
		}

		// finally, reset the lock.
		lockToRefresh.setTimeout(timeout);
		getResponseContext().contentType("text/xml");

		return lockManager.refreshLock(lockToRefresh);
	}

	/** Remove the live DAV properties from the properties document. There is no
	* reason to save them as they are recalculated each time the properties are
	* loaded.
	* @param propertiesDocument an XML document containing the current resource properties
	*/
	public void removeLiveProperties(Document propertiesDocument) {
		Element properties = (Element) propertiesDocument.getDocumentElement();
		Element p = null;
		p =
			(Element) ((Element) properties)
				.getElementsByTagNameNS("DAV:", "supportedlock")
				.item(0);

		if (p != null) {
			properties.removeChild(p);
		}

		// remove the live properties that are repository specific
		propertiesManager.removeLiveProperties(propertiesDocument);
	}

	/** Save the properties after removing any live properties that don't need
	* to be saved.
	* @param propertiesDocument an XML document containing the resource's properties
	* @exception com.ibm.webdav.WebDAVException
	*/
	public void saveProperties(Document propertiesDocument)
		throws WebDAVException {
		// don't save the live properties, they will be recalculated on each load
		removeLiveProperties(propertiesDocument);
		if (propertiesManager instanceof VersionedPropertiesManager) {
			// don't save versoined properties, they will be recalculated on each load
			(
				(
					VersionedPropertiesManager) propertiesManager)
						.removeVersionProperties(
				propertiesDocument);
		}

		propertiesManager.saveProperties(propertiesDocument);
	}

	/** Set the contents of this resource. This may create a new resource on the server,
	 * or update the contents of an existing resource. Sufficient authorization is required
	 * and administered by the target web server. For text/* MIME types, the caller should
	 * be sure to convert Strings to byte codes using an acceptable charset, and to set
	 * that charset in the request context so the server knows how to decode the byte
	 * stream.
	 *
	 * @param value the new contents for the resource
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public void setContents(byte[] value) throws WebDAVException {
		BufferedOutputStream os =
			(BufferedOutputStream) getContentsOutputStream(context);

		for (int i = 0; i < value.length; i++) {
			try {
				os.write(value[i]);
			} catch (WebDAVException exc) {
				throw exc;
			} catch (java.io.IOException exc) {
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"IO Error");
			}
		}

		closeContentsOutputStream(context);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/14/2000 4:14:55 PM)
	 * @param newContext com.ibm.webdav.ResourceContext
	 */
	void setContext(com.ibm.webdav.ResourceContext newContext) {
		context = newContext;
	}

	/** Edit the properties of a resource. The updates must refer to a Document containing a WebDAV
	 * DAV:propertyupdates element as the document root.
	 *
	 * @param updates an XML Document containing DAV:propertyupdate elements
	 * describing the edits to be made
	 * @return a MultiStatus indicating the status of the updates
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus setProperties(ResourceContext context, Document updates)
		throws WebDAVException {
		this.context = context;

		// make sure the resource exists.
		if (!exists()) {
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		// check to see if the resource is locked by another user
		if (isLocked() && !isLockedByMe()) {
			return createPropPatchMultiStatus(
				new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Resource is locked by another user"),
				updates);
		}

		getResponseContext().contentType("text/xml");

		return propertiesManager.setProperties(updates);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/13/2000 8:48:11 PM)
	 * @param newRequestContext com.ibm.webdav.ResourceContext
	 */
	public void setRequestContext(ResourceContext newRequestContext) {
		context.setRequestContext(newRequestContext.getRequestContext());
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/13/2000 8:48:11 PM)
	 * @param newResponseContext com.ibm.webdav.ResourceContext
	 */
	public void setResponseContext(
		com.ibm.webdav.ResourceContext newResponseContext) {
		context.setResponseContext(newResponseContext.getResponseContext());
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/13/2000 8:53:11 PM)
	 * @param newStatusCode com.ibm.webdav.WebDAVStatus
	 */
	void setStatusCode(int newStatusCode) {
		context.getStatusCode().setStatusCode(newStatusCode);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (4/13/2000 8:53:11 PM)
	 * @param newStatusCode com.ibm.webdav.WebDAVStatus
	 */
	void setStatusCode(com.ibm.webdav.WebDAVStatus newStatusCode) {
		context.getStatusCode().setStatusCode(newStatusCode.getStatusCode());
	}

	/** Translate the URI reference relative to this resource in order to obtain
	* the real path name if the URI references a resource on the same machine.
	*
	* @param target the URL of the path to translate
	*
	* @return the pathname to the file on this machine, or the URL if the file
	* is not on this machine
	* @exception com.ibm.webdav.WebDAVException
	*/
	public String translatePathRelativeToMe(String target)
		throws WebDAVException {
		String result = target;
		URL targetURL = null;

		try {
			targetURL = new URL(url, target);
		} catch (java.net.MalformedURLException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"malformed target URL");
		}

		if (targetURL.getHost().equals(url.getHost())) {
			// is on this machine, convert the target to a file on this machine
			result = targetURL.getFile().replace('/', File.separatorChar);

			// append it to doc.root
			String uri = url.getFile();
			String docRoot =
				fileName.substring(0, fileName.length() - uri.length());
			result = docRoot + result;
		}

		return result;
	}

	/** Unlock the lock identified by the lockToken on this resource
	 *
	 * @param lockToken the lock token obtained from the ActiveLock of a previous <code>lock() </code>
	 *     or <code>getLocks()</code>.
	 *
	 * @return a MultiStatus containing any responses on resources that could not
	 *     be unlocked.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus unlock(ResourceContext context, String lockToken)
		throws WebDAVException {
		this.context = context;

		// first, is this the root parent collection locked by this lockToken
		if (parentIsLockedWith(lockToken)) {
			throw new WebDAVException(
				WebDAVStatus.SC_METHOD_NOT_ALLOWED,
				"Must unlock depth lock from root collection having the lock.");
		}

		MultiStatus result = doUnlock(lockToken);
		setStatusCode(WebDAVStatus.SC_NO_CONTENT); // the default status code

		return result;
	}

	/** Update live properties that are supported by this server. This method
	* updates those that are not unique to any repository implementation.
	* This is mostly the live DAV properties as defined in the WebDAV spec.
	* @param document an XML document containing the resource properties
	* @exception com.ibm.webdav.WebDAVException
	*/
	public void updateLiveProperties(Document document)
		throws WebDAVException {
		Element properties = document.getDocumentElement();

		// remove any live properties that need to be recalculated
		removeLiveProperties(document);

		// now recalculate the live properties, the repository independent ones first
		Enumeration liveprops = liveProperties.elements();

		while (liveprops.hasMoreElements()) {
			LiveProperty liveProperty = (LiveProperty) liveprops.nextElement();
			PropertyValue value = liveProperty.getValueFor(this);
			Element e =
				(Element) ((Element) properties)
					.getElementsByTagNameNS(
						liveProperty.getNSName(),
						liveProperty.getNSLocalName())
					.item(0);

			if ((e != null) && (value != null) && (value.getValue() != null)) {
				properties.removeChild(e);
			}

			if ((value != null) && (value.getValue() != null)) {
				properties.appendChild(value.getValue());
			} else {
				// put in an empty element
				e =
					document.createElementNS(
						liveProperty.getNSName(),
						liveProperty.getPreferredPrefix()
							+ ":"
							+ liveProperty.getNSLocalName());

				e.setAttribute(
					"xmlns:" + liveProperty.getPreferredPrefix(),
					liveProperty.getNSName());
				properties.appendChild(e);
			}

			// TODO: missing status
		}

		// lockdiscovery

		/*
		Element lockdiscovery = (TXElement) lockManager.getLockDiscovery();
		Element l = ((TXElement) properties).getElementNamed("DAV:", "lockdiscovery");
		if (l != null && lockdiscovery != null) {
		properties.removeChild(l);
		}
		properties.appendChild(lockdiscovery);
		*/

		// creationdate
		Element creationDate =
			(Element) ((Element) properties)
				.getElementsByTagNameNS("DAV:", "creationdate")
				.item(0);

		if (creationDate == null) {
			creationDate = document.createElementNS("DAV:", "D:creationdate");

			String cdstring =
				new SimpleISO8601DateFormat().format(new java.util.Date());
			creationDate.appendChild(document.createTextNode(cdstring));
			properties.appendChild(creationDate);
		}

		// displayname - set the default. Subclasses may want to override
		Element displayName =
			(Element) ((Element) properties)
				.getElementsByTagNameNS("DAV:", "displayname")
				.item(0);

		if (displayName == null) {
			displayName = document.createElementNS("DAV:", "D:displayname");

			displayName.appendChild(
				document.createTextNode(URLEncoder.encode(getName())));
			properties.appendChild(displayName);
		}

		// get the supportedlock element from the lock manager
		properties.appendChild(
			document.importNode(lockManager.getSupportedLock(), true));

		// do the ones that are repository specific
		propertiesManager.updateLiveProperties(document);
	}

	/**
	 * Checks in current resource
	 * 
	 * @throws WebDAVException
	 */
	public void checkin() throws WebDAVException {
		if (namespaceManager instanceof VersionedNamespaceManager) {
			//			validate the uri
			if (hasValidURI() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (exists() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}

			if (((VersionedNamespaceManager) namespaceManager)
				.isCheckedOutVersion()
				== false) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Not checked out");
			}

			// check to see if the resource is locked by another user
			if (isLocked() && !isLockedByMe()) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Resource is locked by another user");
			}
			
			String sLoc =
				((VersionedNamespaceManager) namespaceManager).checkin();
			context.getResponseContext().location(sLoc);
			setStatusCode(WebDAVStatus.SC_CREATED);
		} else {
			throw new WebDAVException(
				WebDAVStatus.SC_METHOD_NOT_ALLOWED,
				"Repository does not support this method");
		}
	}

	/**
	 * Checks out the current resource
	 * 
	 * @throws WebDAVException
	 */
	public void checkout() throws WebDAVException {
		if (namespaceManager instanceof VersionedNamespaceManager) {
			//			validate the uri
			if (hasValidURI() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (exists() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}
			
			// check to see if the resource is locked by another user
			if (isLocked() && !isLockedByMe()) {
				throw new WebDAVException(
					WebDAVStatus.SC_LOCKED,
					"Resource is locked by another user");
			}

			((VersionedNamespaceManager) namespaceManager).checkout();
			setStatusCode(WebDAVStatus.SC_OK);
		} else {
			throw new WebDAVException(
				WebDAVStatus.SC_METHOD_NOT_ALLOWED,
				"Repository does not support this method");
		}
	}

	/**
	 * Get the named properties for all versions of this resource.
	 *
	 * @param context
	 * @param names an array of PropertyNames to retrieve.
	 * 
	 * @return
	 * @throws WebDAVException
	 */
	public MultiStatus getVersionTreeReport(
		ResourceContext context,
		PropertyName[] names)
		throws WebDAVException {
		this.context = context;

		// check the depth parameter
		if (namespaceManager.isVersionable() == false) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}
		MultiStatus result = new MultiStatus();
		// now get the properties of the versions if necessary

		Iterator memberIter =
			((VersionedNamespaceManager) namespaceManager)
				.getVersions()
				.iterator();
		while (memberIter.hasNext()) {
			ResourceImpl member = (ResourceImpl) memberIter.next();
			try {
				MultiStatus memberResult = member.getProperties(context, names);

				result.mergeWith(memberResult);
			} catch (WebDAVException exc) {
				MethodResponse response =
					new MethodResponse(
						member.getURL().toString(),
						exc.getStatusCode());
				result.addResponse(response);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"unable to delete resource");
			}
		}

		getResponseContext().contentType("text/xml");
		return result;

	}

	/**
	 * Get the properties for all versions of this resource.
	 * 
	 * @param context
	 * @return
	 * @throws WebDAVException
	 */
	public MultiStatus getVersionTreeReport(ResourceContext context)
		throws WebDAVException {
		this.context = context;

		// check the depth parameter
		if (namespaceManager.isVersionable() == false) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}
		MultiStatus result = new MultiStatus();
		// now get the properties of the versions if necessary

		Iterator memberIter =
			((VersionedNamespaceManager) namespaceManager)
				.getVersions()
				.iterator();
		while (memberIter.hasNext()) {
			ResourceImpl member = (ResourceImpl) memberIter.next();
			try {
				MultiStatus memberResult = member.getProperties(context);

				result.mergeWith(memberResult);
			} catch (WebDAVException exc) {
				MethodResponse response =
					new MethodResponse(
						member.getURL().toString(),
						exc.getStatusCode());
				result.addResponse(response);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"unable to delete resource");
			}
		}

		getResponseContext().contentType("text/xml");
		return result;

	}

	/**
	 * 
	 */
	public void uncheckout() throws WebDAVException {
		if (namespaceManager instanceof VersionedNamespaceManager) {
			//			validate the uri
			if (hasValidURI() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (exists() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}

			if (((VersionedNamespaceManager) namespaceManager)
				.isCheckedOutVersion()
				== false) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Not checked out");
			}

			((VersionedNamespaceManager) namespaceManager).uncheckout();
			setStatusCode(WebDAVStatus.SC_OK);
		} else {
			throw new WebDAVException(
				WebDAVStatus.SC_METHOD_NOT_ALLOWED,
				"Repository does not support this method");
		}

	}

	/**
	 * 
	 */
	public void versionControl() throws WebDAVException {
		if (namespaceManager instanceof VersionedNamespaceManager) {
			//			validate the uri
			if (hasValidURI() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Invalid URI");
			}

			// make sure the resource exists
			if (exists() == false) {
				throw new WebDAVException(
					WebDAVStatus.SC_NOT_FOUND,
					"Cannot copy a lock-null resource");
			}

			if (((VersionedNamespaceManager) namespaceManager).isVersioned()
				== true) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Already versioned");
			}

			((VersionedNamespaceManager) namespaceManager).versionControl();
			setStatusCode(WebDAVStatus.SC_OK);
		} else {
			throw new WebDAVException(
				WebDAVStatus.SC_METHOD_NOT_ALLOWED,
				"Repository does not support this method");
		}

	}

	/**
	 * @return
	 */
	public List getAllowedMethods() throws WebDAVException {

		return namespaceManager.getAllowedMethods();
	}

	/* (non-Javadoc)
	 * @see com.ibm.webdav.impl.IRResource#closeContentsOutputStream(com.ibm.webdav.ResourceContext, java.lang.String)
	 */
	public void closeContentsOutputStream(ResourceContext context, String sContentType) throws WebDAVException {
		this.context = context;

		if(sContentType == null) {
			namespaceManager.closeContentsOutputStream();
		} else {
			namespaceManager.closeContentsOutputStream(sContentType);
		}
		

		// update the live properties
		Document propertiesDocument = loadProperties();
		saveProperties(propertiesDocument);

		// inherit any deep locks on the parent collection
		inheritParentDeepLocks();
		
	}
}