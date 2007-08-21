package com.ibm.webdav.impl;

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

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.Collection;

/** A CollectionImpl is a ResourceImpl that contains other
 * resources including other CollectionImpls. It provides a
 * concrete, server side implementation of Collection.
 * <p>
 * CollectionImpl should inherit from ResourceImpl and CollectionP.
 * However, Java doesn't support multiple inheritance, so CollectionImpl
 * must re-implement all of CollectionP's methods. Many of these methods
 * are overridden anyway for server-side behavior.
 */
public class CollectionImpl extends ResourceImpl implements IRCollection {
	private Vector members = null;
	// lazy retrieve the members of the collection for the server
	public CollectionImpl() {
		super();
	}
	/** Construct a CollectionImpl for the given URL.
	*
	* @param url the URL of the resource
	* @param localName a translation of the URL (filePortion) into
	* a name that has local meaning to a server.
	* @param targetSelector the revision target selector for this Collection
	* @exception com.ibm.webdav.WebDAVException
	*/
	public CollectionImpl(
		URL url,
		String localName,
		TargetSelector targetSelector)
		throws WebDAVException {
		super(url, localName);
		String file = url.getFile();
		if (!file.endsWith("/")) {
			file = file + "/";
			try {
				this.url = new URL(url, file);
			} catch (java.net.MalformedURLException exc) {
				throw new WebDAVException(
					WebDAVStatus.SC_BAD_REQUEST,
					"Malformed URL");
			}

		}
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
		return copy(
			context,
			destinationURL,
			overwrite,
			propertiesToCopy,
			Collection.deep);
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
	* @param depth an indicator for immediate members or recursively all children.
	* <ul>
	*    <li>shallow: copy only this resource</li>
	*    <li>deep: copy this resource and recursively all of its children</li>
	* </ul>
	*
	* @return the status of the copy operation for each resource copied
	* @exception com.ibm.webdav.WebDAVException
	*/
	public MultiStatus copy(
		ResourceContext context,
		String destinationURL,
		boolean overwrite,
		Vector propertiesToCopy,
		String depth)
		throws WebDAVException {
		this.context = context;

		setStatusCode(WebDAVStatus.SC_CREATED);

		// create a MultiStatus to hold the results
		MultiStatus result = new MultiStatus();

		// validate the uri
		if (!hasValidURI()) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid URI");
		}

		// check the depth parameter
		if (!(depth.equals(Collection.shallow)
			|| depth.equals(Collection.deep))) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}

		// make sure the resource exists
		if (!exists()) {
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		// the destination may be a relative URL
		Collection destination = null;
		try {
			URL destURL = new URL(getURL(), destinationURL);
			destination = new Collection(destURL.toString());
		} catch (WebDAVException exc) {
			throw exc;
		} catch (java.net.MalformedURLException exc) {
		} catch (java.io.IOException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Malformed URL");
		}

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
		if (destination.exists()
			&& destination.isLocked()
			&& !destination.isLockedByMe()) {
			throw new WebDAVException(
				WebDAVStatus.SC_LOCKED,
				"Destination resource is locked");
		}

		// check to see if the destination exists and its OK to overwrite it
		if (destination.exists() && !overwrite) {
			throw new WebDAVException(
				WebDAVStatus.SC_PRECONDITION_FAILED,
				"Destination exists and overwrite not specified");
		}

		// Ready to copy. For collections, copy the members
		// First, create the destination collection
		if (destination.exists()) {
			destination.delete();
			setStatusCode(WebDAVStatus.SC_NO_CONTENT);
		}
		destination.createCollection();

		// now copy the members if necessary
		if (depth.equals(Collection.deep)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();
				// calculate the destination URL
				String thisURL = getURL().toString();
				String memberURL = member.getURL().toString();
				String memberPart = memberURL.substring(thisURL.length());
				String dest = destination.getURL().toString() + memberPart;
				try {
					MultiStatus memberResult = null;
					if (member.isCollection()) {
						memberResult =
							((CollectionImpl) member).copy(
								context,
								dest,
								overwrite,
								propertiesToCopy,
								depth);
					} else {
						memberResult =
							member.copy(
								context,
								dest,
								overwrite,
								propertiesToCopy);
					}
					if (!memberResult.isOK()) {
						result.mergeWith(memberResult);
					}
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
		}

		// copy the properties
		WebDAVStatus savedStatusCode = context.getStatusCode();
		// might be overritten by copying proerties or unlocking
		MultiStatus ms2 = copyProperties(destination, propertiesToCopy);
		if (!ms2.isOK()) {
			// todo: add code here to back out this partial copy. That might require
			//    restoring the resource that used to be at the destination. For now, we'll throw
			//    an exception.
			throw new WebDAVException(
				WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
				"problem copying properties");
		}

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
		context.setStatusCode(savedStatusCode);

		// everything must have gone OK, there
		// is no response for a successful delete
		getResponseContext().contentType("text/xml");
		return result;
	}
	/** Actually create the collection in the repository. The resource indicated
	* by the URL must not already exist. All ancestors of this URL must already
	* exist.
	*
	* @param contents an XML Document describing the members of this collection, bodies
	* of members, and properties on the collections or members. Not completely defined in
	* version 10 of the WebDAV specification
	*
	* @return Multistatus describing the result
	* of the operation
	* @exception com.ibm.webdav.WebDAVException
	*/
	public MultiStatus createCollection(
		ResourceContext context,
		Document contents)
		throws WebDAVException {
		this.context = context;

		setStatusCode(WebDAVStatus.SC_CREATED);
		String fileName = ResourceFactory.getRealPath(getURL());

		// validate the uri
		if (!hasValidURI() || fileName.indexOf(File.separator) < 0) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid URI");
		}

		// see if the resource already exists
		if (exists()) {
			if (isCollection()) {
				throw new WebDAVException(
					WebDAVStatus.SC_METHOD_NOT_ALLOWED,
					"Collection already exists");
			} else {
				throw new WebDAVException(
					WebDAVStatus.SC_METHOD_NOT_ALLOWED,
					"Resource already exists");
			}
		}

		// make sure the parent collection exists
		CollectionImpl parent = (CollectionImpl) getParentCollection();
		if (parent != null && !parent.exists()) {
			throw new WebDAVException(
				WebDAVStatus.SC_CONFLICT,
				"Parent collection does not exist");
		}

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

		// check for an unsupported contents argument. The request entity format
		// for MKCOL is not yet defined, so all contents are unsupported
		if (contents != null) {
			throw new WebDAVException(
				WebDAVStatus.SC_UNSUPPORTED_MEDIA_TYPE,
				"Creating collections from contents not supported yet");
		}

		// create the collection
		namespaceManager.createCollection(
			ResourceFactory.getRealPath(getURL()));

		// get the default properties, and write them out too.
		Document properties = loadProperties();
		saveProperties(properties);

		// inherit any deep locks on the parent collection
		inheritParentDeepLocks();
		MultiStatus status = new MultiStatus();
		MethodResponse response =
			new MethodResponse(getURL().toString(), WebDAVStatus.SC_CREATED);
		status.addResponse(response);
		getResponseContext().contentType("text/xml");
		return status;
	}
	/** Delete this resouce collection and all its members from the server.
	 * The actual effect of the delete operation is determined by the underlying
	 * repository manager. The visible effect to WebDAV is that the resource
	 * is no longer available.
	 *
	 * @return a MultiStatus containing the status of the delete method on each
	 *         effected resource.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus delete(ResourceContext context) throws WebDAVException {
		this.context = context;

		MultiStatus result = new MultiStatus();

		// delete all the members first
		Enumeration members = namespaceManager.getMembers().elements();
		while (members.hasMoreElements()) {
			ResourceImpl member = (ResourceImpl) members.nextElement();
			try {
				MultiStatus memberResult = null;
				memberResult = member.delete(context);
				if (!memberResult.isOK()) {
					result.mergeWith(memberResult);
				}
			} catch (WebDAVException exc) {
				MethodResponse response =
					new MethodResponse(member.getURL().toString(), exc);
				result.addResponse(response);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebDAVException(
					WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
					"unable to delete resource");
			}
		}

		// now delete this collection
		if (result.isOK()) {
			try {
				MultiStatus thisResult = super.delete(context);
				if (!thisResult.isOK()) {
					result.mergeWith(thisResult);
				}
			} catch (WebDAVException exc) {
				//MethodResponse response = new MethodResponse(getURL().toString(), exc.getStatusCode());
				MethodResponse response =
					new MethodResponse(getURL().toString(), exc);
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
	/** Unlock the lock identified by the lockToken on this resource. This method
	 * is used internally to unlock resources copied or moved as well as unlocked.
	 * For a resource collection, unlock all the members that are locked with the
	 * same lock token.
	 *
	 * @param lockToken the lock token obtained from the ActiveLock of a previous <code>lock() </code>
	 *     or <code>getLocks()</code>.
	 *
	 * @return a MultiStatus containing any responses on resources that could not
	 *     be unlocked.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	protected MultiStatus doUnlock(String lockToken) throws WebDAVException {
		// find all the locks identified by this lockToken, and unlock them.

		MultiStatus result = new MultiStatus();

		// get the ActiveLock to unlock
		ActiveLock lockToUnlock = null;
		Enumeration activeLocks = getLocks().elements();
		while (activeLocks.hasMoreElements()) {
			ActiveLock activeLock = (ActiveLock) activeLocks.nextElement();
			if (activeLock.getLockToken().equals(lockToken)) {
				lockToUnlock = activeLock;
				break;
			}
		}

		// unlock the lock on this collection and then check all its
		// members for a lock using the same lockToken.
		if (lockToUnlock != null
			&& lockToUnlock.getDepth().equals(Collection.deep)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();
				member.getRequestContext().precondition(
					getRequestContext().precondition());
				member.getRequestContext().authorization(
					getRequestContext().authorization());
				try {
					MultiStatus memberResult = member.doUnlock(lockToken);
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
		}

		// Unlock this lock
		MultiStatus r = super.doUnlock(lockToken);
		if (!r.isOK()) {
			result.mergeWith(r);
		}
		getResponseContext().contentType("text/xml");
		return result;
	}
	/** Get the URL of a child of this resource treating the resource
	* as a collection
	*
	* @param childName the local repository name for the child resource
	* @return the child URL
	* @exception com.ibm.webdav.WebDAVException
	*/
	public URL getChildURL(String childName) throws WebDAVException {
		String uri = getURL().getFile();
		if (!uri.endsWith("/")) {
			uri = uri + "/";
		}
		URL child = null;
		try {
			child = new URL(getURL(), uri + childName);
		} catch (java.net.MalformedURLException exc) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Malformed URL");
		}
		return child;
	}
	/** The WebDAV spec does not explicitly define the contents of a collection.
	* Rather it obtains the members of a collection by doing a PROPFIND with
	* depth="infinity" and gets the href elements from the response elements
	* to determine the members of a collection. This implementation returns
	* and XML document containing the URLs of the members of the collection.
	* @return an InputStream on an XML document containing the members of this collection
	* @exception com.ibm.webdav.WebDAVException
	*/
	public InputStream getContentsInputStream() throws WebDAVException {
		// validate the uri
		if (!hasValidURI()) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid URI");
		}
		InputStream is = namespaceManager.getContentsInputStream();

		// set the content type
		getResponseContext().contentType("text/xml");
		return is;
	}
	/** WebDAV does not allow PUT to collections. Use Collection.createCollection()
	* (MKCOL) instead.
	* @return nothing
	* @exception com.ibm.webdav.WebDAVException throws METHOD_NOT_ALLOWED for all collections
	*/
	public OutputStream getContentsOutputStream() throws WebDAVException {
		throw new WebDAVException(
			WebDAVStatus.SC_METHOD_NOT_ALLOWED,
			"Cannot use PUT on collections, use MKCOL instead.");
	}
	/** Get the members of this Collection.
	* @return an Vector of CollectionMembers
	* @exception com.ibm.webdav.WebDAVException
	* @see CollectionMember
	*/
	public Vector getMembers() throws WebDAVException {
		return namespaceManager.getMembers();
	}
	/** Get the named properties for this resource and (potentially) its children.
	*
	* @param names an array of PropertyNames to retrieve.
	* @param depth an indicator for immediate members or recursively all children.
	* <ul>
	*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
	*    <li>allMembers: properties of this resource and recursively all its children</li>
	* </ul>
	*
	* @return a MultiStatus of PropertyResponses
	* @exception com.ibm.webdav.WebDAVException
	*/
	public MultiStatus getProperties(
		ResourceContext context,
		PropertyName names[],
		String depth)
		throws WebDAVException {
		this.context = context;

		// check the depth parameter
		if (!(depth.equals(Collection.thisResource)
			|| depth.equals(Collection.immediateMembers)
			|| depth.equals(Collection.allMembers))) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}

		MultiStatus result = super.getProperties(context, names);
		String memberDepth = depth;
		if (depth.equals(Collection.immediateMembers)) {
			memberDepth = Collection.thisResource;
		}

		// now get the properties of the members if necessary
		if (!depth.equals(Collection.thisResource)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();
				try {
					MultiStatus memberResult = null;
					if (member.isCollection()) {
						memberResult =
							((CollectionImpl) member).getProperties(
								context,
								names,
								memberDepth);
					} else {
						memberResult = member.getProperties(context, names);
					}
					result.mergeWith(memberResult);
				} catch (WebDAVException exc) {
					exc.printStackTrace();
					MethodResponse response =
						new MethodResponse(
							member.getURL().toString(),
							exc.getStatusCode());
					result.addResponse(response);
				} catch (Exception e) {
					e.printStackTrace();
					throw new WebDAVException(
						WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
						"Error getting properties");
				}
			}
		}
		getResponseContext().contentType("text/xml");
		return result;
	}

	/** Get all the properties for this resource and (potentially) its children.
	*
	* @param depth an indicator for immediate members or recursively all children.
	* <ul>
	*    <li>thisResource: propeprties of this resource</li>
	*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
	*    <li>allMembers: properties of this resource and recursively all its children</li>
	* </ul>
	*
	* @return a MultiStatus of PropertyResponses
	* @exception com.ibm.webdav.WebDAVException
	*/
	public MultiStatus getProperties(ResourceContext context, String depth)
		throws WebDAVException {
		this.context = context;

		// check the depth parameter
		if (!(depth.equals(Collection.thisResource)
			|| depth.equals(Collection.immediateMembers)
			|| depth.equals(Collection.allMembers))) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}

		MultiStatus result = super.getProperties(context);
		String memberDepth = depth;
		if (depth.equals(Collection.immediateMembers)) {
			memberDepth = Collection.thisResource;
		}

		// now get the properties of the members if necessary
		if (!depth.equals(Collection.thisResource)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();
				try {
					MultiStatus memberResult = null;
					if (member.isCollection()) {
						memberResult =
							((CollectionImpl) member).getProperties(
								context,
								memberDepth);
					} else {
						memberResult = member.getProperties(context);
					}
					result.mergeWith(memberResult);
				} catch (WebDAVException exc) {
					exc.printStackTrace();
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
		}
		getResponseContext().contentType("text/xml");
		return result;
	}

	/** Get the names of all properties for this resource and (potentially) its children.
	*
	* @param depth an indicator for immediate members or recursively all children.
	* <ul>
	*    <li>thisResource: propeprties of this resource</li>
	*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
	*    <li>allMembers: properties of this resource and recursively all its children</li>
	* </ul>
	*
	* @return a MultiStatus of PropertyResponses
	* (PropertyValue.value is always null, PropertyValue.status contains the status)
	* @exception com.ibm.webdav.WebDAVException
	*/
	public MultiStatus getPropertyNames(ResourceContext context, String depth)
		throws WebDAVException {
		this.context = context;

		// check the depth parameter
		if (!(depth.equals(Collection.thisResource)
			|| depth.equals(Collection.immediateMembers)
			|| depth.equals(Collection.allMembers))) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}
		MultiStatus result = super.getPropertyNames(context);
		String memberDepth = depth;
		if (depth.equals(Collection.immediateMembers)) {
			memberDepth = Collection.thisResource;
		}

		// now get the properties of the members if necessary
		if (!depth.equals(Collection.thisResource)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();
				try {
					MultiStatus memberResult = null;
					if (member.isCollection()) {
						memberResult =
							((CollectionImpl) member).getPropertyNames(
								context,
								memberDepth);
					} else {
						memberResult = member.getPropertyNames(context);
					}
					result.mergeWith(memberResult);
				} catch (WebDAVException exc) {
					exc.printStackTrace();
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
		}
		getResponseContext().contentType("text/xml");
		return result;
	}
	/** Returns true if this Resource is a collection. Returns false otherwise.
	 *
	 * @return true if this Resource is a collection.
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public boolean isCollection() throws WebDAVException {
		return true;
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

		// first lock this collection
		MultiStatus result = super.lock(activeLock);

		// now lock all the members if necessary
		if (activeLock.getDepth().equals(Collection.deep)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				ResourceImpl member = (ResourceImpl) members.nextElement();
				member.getRequestContext().precondition(
					getRequestContext().precondition());
				member.getRequestContext().authorization(
					getRequestContext().authorization());
				try {
					MultiStatus memberResult = member.lock(activeLock);
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
		}

		return result;
	}
	/** Lock this resource collection and potentially all its members
	* based on the given parameters. This allows control of the lock
	* scope (exclusive or shared) the lock type (write), owner information, etc.
	*
	* @param scope the scope of the lock, exclusive or shared
	* @param type the type of the lock, currently only write
	* @param timeout the number of seconds before the lock times out or
	*     0 for infinite timeout.
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
		return lock(context, scope, type, timeout, owner, Collection.deep);
	}
	/** Lock this resource collection and potentially all its members
	* based on the given parameters. This allows control of the lock
	* scope (exclusive or shared) the lock type (write), owner information, etc.
	*
	* @param scope the scope of the lock, exclusive or shared
	* @param type the type of the lock, currently only write
	* @param timeout the number of seconds before the lock times out or
	*     0 for infinite timeout.
	* @param owner an XML element containing useful information that can be
	*     used to identify the owner of the lock. An href to a home page, an
	*     email address, phone number, etc. Can be null if no owner information
	*     is provided.
	* @param depth
	*     <ul>
	*         <li>shallow lock only this resource</li>
	*         <li>deep lock this resource and all its children</li>
	*     </ul>
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
		Element owner,
		String depth)
		throws WebDAVException {
		this.context = context;

		// check the depth parameter
		if (!(depth.equals(Collection.shallow)
			|| depth.equals(Collection.deep))) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Invalid depth on copy");
		}

		// lock the collection, and use the same lock token for all its members
		ActiveLock activeLock = getActiveLockFor(scope, type, timeout, owner);
		activeLock.setDepth(depth);
		MultiStatus result = lock(activeLock);

		// remove all granted locks if all the requests couldn't be satisfied
		if (!result.isOK()) {
			try {
				unlock(context, activeLock.getLockToken());
			} catch (Exception exc) {
			}
			// remove all the lockdiscovery elements from the result for the locks
			// that were unlocked
			result.removeOKResponses();
		}

		// return the granted lock token in the lockToken response context
		getResponseContext().lockToken(activeLock.getLockToken());
		getResponseContext().contentType("text/xml");
		return result;
	}
	/** Refresh the lock on this resource collection and all its members locked
	 * by the same lock token by resetting the lock timeout.
	 * The context must contain the proper authorization for the requesting
	 * principal.
	 *
	 * @param lockToken the lock token identifying the lock.
	 * @param timeout the new timeout in seconds. -1 means infinite timeout.
	 * @return updated information about the lock status of this resource
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public MultiStatus refreshLock(
		ResourceContext context,
		String lockToken,
		int timeout)
		throws WebDAVException {
		this.context = context;

		// find all the locks identified by this lockToken, and refresh them.

		// first, is this the root parent collection locked by this lockToken
		if (parentIsLockedWith(lockToken)) {
			throw new WebDAVException(
				WebDAVStatus.SC_METHOD_NOT_ALLOWED,
				"Must refresh depth lock from root collection having the lock.");
		}

		// Refresh this lock
		MultiStatus result = super.refreshLock(context, lockToken, timeout);

		// get the new ActiveLock
		ActiveLock lockToRefresh = null;
		Enumeration activeLocks = getLocks().elements();
		while (activeLocks.hasMoreElements()) {
			ActiveLock activeLock = (ActiveLock) activeLocks.nextElement();
			if (activeLock.getLockToken().equals(lockToken)) {
				lockToRefresh = activeLock;
				break;
			}
		}

		// refresh the lock on this collection and then check all its
		// members for a lock using the same lockToken.
		if (lockToRefresh.getDepth().equals(Collection.deep)) {
			Enumeration members = namespaceManager.getMembers().elements();
			while (members.hasMoreElements()) {
				Resource member = (Resource) members.nextElement();
				member.getRequestContext().precondition(
					getRequestContext().precondition());
				member.getRequestContext().authorization(
					getRequestContext().authorization());
				try {
					MultiStatus memberResult =
						member.refreshLock(lockToken, timeout);
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
		}
		getResponseContext().contentType("text/xml");
		return result;
	}

	/**
	 * Sets the ordering of the members of this collection
	 * 
	 * @param context
	 * @param orderPatch
	 * @throws WebDAVException
	 */
	public MultiStatus setOrdering(
		ResourceContext context,
		Document orderPatch)
		throws WebDAVException {
		//	make sure the resource exists.
		if (exists() == false) {
			throw new WebDAVException(
				WebDAVStatus.SC_NOT_FOUND,
				"Resource does not exist");
		}

		if (isCollection() == false) {
			throw new WebDAVException(
				WebDAVStatus.SC_BAD_REQUEST,
				"Resource is not collection");
		}

		// check to see if the resource is locked by another user
		if (isLocked() && isLockedByMe() == false) {
			throw new WebDAVException(
				WebDAVStatus.SC_LOCKED,
				"Resource is locked by another user");
		}

		MultiStatus result = new MultiStatus();

		try {
			namespaceManager.setOrdering(orderPatch);
		} catch (WebDAVException e) {
			//TODO sort out proper error reporting
			result.addResponse(new MethodResponse(getURL().getPath(),e));
		}

		return result;
	}
}
