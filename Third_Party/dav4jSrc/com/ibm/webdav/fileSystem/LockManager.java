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
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;
import com.ibm.webdav.impl.*;

/** Implement the LockManager interface using the resource's
* PropertiesManager to persist the DAV:lockdiscovery property.
* @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
* @see PropertiesManager
* @see CachedPropertiesManager
*/
public class LockManager implements com.ibm.webdav.impl.LockManager {
	private ResourceImpl resource = null;
	private com.ibm.webdav.impl.PropertiesManager propertiesManager = null;
public LockManager() {
}
/** Create a lock manager for the given resource.
* @param resource the resource to manage locks for
* @param namespaceManager its namespace manager
* @param propertiesManager and its properties manager
*/
public LockManager(ResourceImpl resource, com.ibm.webdav.impl.NamespaceManager namespaceManager, com.ibm.webdav.impl.PropertiesManager propertiesManager) {
	initialize(resource, namespaceManager, propertiesManager);
}
/** Get the DAV:lockdiscovery property for the resource.
*
* @return an Element with tag name D:lockdiscovery
* @exception com.ibm.webdav.WebDAVException
*/
public Element getLockDiscovery() throws WebDAVException {
	// get the raw properties from the property manager
	Document propertiesDocument = propertiesManager.loadProperties();
	Element properties = propertiesDocument.getDocumentElement();
	Element lockdiscovery = (Element)((Element) properties).getElementsByTagNameNS("DAV:", "lockdiscovery").item(0);

	// timeout any expired locks by
	// deleting any locks that have timed out
	boolean locksHaveChanged = false;
	if (lockdiscovery != null) {
		NodeList locks = ((Element) lockdiscovery).getElementsByTagNameNS("DAV:", "activelock");
		Element lock = null;
		Date now = new Date();
		for (int i = 0; i < locks.getLength(); i++) {
			lock = (Element) locks.item(i);
			ActiveLock activeLock = new ActiveLock(lock);
			// see if the lock has timed out
			Date expiration = activeLock.getExpiration();
			if (expiration != null && expiration.before(now)) {
				// has timed out, remove the lock
				locksHaveChanged = true;
				try {
					lockdiscovery.removeChild(lock);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}
	if (locksHaveChanged) {
		propertiesManager.saveProperties(propertiesDocument);
	}
	return lockdiscovery;
}
/** Get the locks that exist on this resource. May be null if the resource
* is not locked. Get the locks from the DAV:lockdiscovery property
*
* @return a Vector of ActiveLock objects
* @exception com.ibm.webdav.WebDAVException
*/
public Vector getLocks() throws WebDAVException {
	Element lockdiscovery = (Element) getLockDiscovery();
	Vector allLocks = new Vector();
	if (lockdiscovery != null) {
		NodeList activeLocks = ((Element) lockdiscovery).getElementsByTagNameNS("DAV:", "activelock");
		Element activeLock = null;
		for (int i = 0; i < activeLocks.getLength(); i++) {
			activeLock = (Element) activeLocks.item(i);
			allLocks.addElement(new ActiveLock(activeLock));
		}
	}
	return allLocks;
}
/** Get information about locks supported by this resource.
* The file system supports shared and exclusive write locks.
*
* @return an Element with tag name D:supportedlock
*/
public Element getSupportedLock() {
	Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch(Exception e) {
          e.printStackTrace(System.err);
        }

	Element supportedlock = document.createElement("D:supportedlock");
 	  Element lockentry = document.createElement("D:lockentry");
	  supportedlock.appendChild(lockentry);
	    Element lockscope = document.createElement("D:lockscope");
	      lockscope.appendChild(document.createElement("D:exclusive"));
	    lockentry.appendChild(lockscope);
	    Element locktype = document.createElement("D:locktype");
	      locktype.appendChild(document.createElement("D:write"));
	    lockentry.appendChild(locktype);
 	  lockentry = document.createElement("D:lockentry");
	  supportedlock.appendChild(lockentry);
	    lockscope = document.createElement("D:lockscope");
	      lockscope.appendChild(document.createElement("D:shared"));
	    lockentry.appendChild(lockscope);
	    locktype = document.createElement("D:locktype");
	      locktype.appendChild(document.createElement("D:write"));
	    lockentry.appendChild(locktype);

	return supportedlock;
}
/** Initialize this lock manager
* @param resource the resource to manage locks for
* @param namespaceManager its namespace manager
* @param propertiesManager and its properties manager
*/
public void initialize(ResourceImpl resource, com.ibm.webdav.impl.NamespaceManager namespaceManager, com.ibm.webdav.impl.PropertiesManager propertiesManager) {
	this.resource = resource;
	this.propertiesManager = propertiesManager;
}
/** Lock this resource Using the given activeLock. Create the
* lock by adding a new active lock to the lock discovery property.
*
* @param activeLock the lock to activate (i.e., persist)
*
* @return a MultiStatus containing a lockdiscovery property indicating
* the results of the lock operation.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus lock(ActiveLock activeLock) throws WebDAVException {
        Element activeLockEl = activeLock.asXML();
        Document xmlDoc = activeLockEl.getOwnerDocument();
        MultiStatus result = new MultiStatus();
	PropertyResponse propertyResponse = new PropertyResponse(resource.getURL().toString());
	result.addResponse(propertyResponse);

	PropertyValue l = resource.getProperty( PropertyName.pnLockdiscovery);
	Element lockdiscovery = null;
	if (l == null) {
		lockdiscovery = xmlDoc.createElement("D:lockdiscovery");
		lockdiscovery.setAttribute("xmlns:D", "DAV:");
	} else {
		lockdiscovery = (Element) ((Element) l.value).cloneNode(true);
	}
	lockdiscovery.appendChild(activeLockEl);
	propertiesManager.setProperty("DAV:lockdiscovery", lockdiscovery);

	// all lock methods return the lockdiscovery property, even if the lock failed
	PropertyName propname = PropertyName.createPropertyNameQuietly( "DAV:lockdiscovery" );

	propertyResponse.addProperty( propname
		, (Element) ((Element) lockdiscovery).cloneNode(true)
		, WebDAVStatus.SC_OK);
	return result;
}
/** Refresh the lock on this resource by resetting the lock timeout.
* The context must contain the proper authorization for the requesting
* principal. Refresh the lock by updating the timeout element of the
* active lock in the lock discovery property.
*
* @param activeLock the lock to refresh. Contains the updated timeout
* and expiration date.
*
* @return updated information about the lock status of this resource
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus refreshLock(ActiveLock activeLock) throws WebDAVException {
	Element activeLockEl = activeLock.asXML();
        Document xmlDoc = activeLockEl.getOwnerDocument();
        MultiStatus result = new MultiStatus();
	PropertyResponse propertyResponse = new PropertyResponse(resource.getURL().toString());
	result.addResponse(propertyResponse);

	// get the locks on this resource
	PropertyValue l = resource.getProperty( PropertyName.pnLockdiscovery );
	Element lockdiscovery = null;
	if (l == null) {
		lockdiscovery = xmlDoc.createElement("D:lockdiscovery");
		lockdiscovery.setAttribute("xmlns:D", "DAV:");
	} else {
		lockdiscovery = (Element) ((Element) l.value).cloneNode(true);
	}

	// find the lock
	boolean lockFound = false;
	NodeList locks = ((Element) lockdiscovery).getElementsByTagNameNS("DAV:", "activelock");
	Element lock = null;
	for (int i = 0; i < locks.getLength(); i++) {
		lock = (Element) locks.item(i);
		ActiveLock aLock = new ActiveLock(lock);
		if (aLock.getLockToken().equals(activeLock.getLockToken())) {
			lockFound = true;
			lockdiscovery.removeChild(lock);
			break;
		}
	}
	if (!lockFound) {
		throw new WebDAVException(WebDAVStatus.SC_PRECONDITION_FAILED, "principal does not own a lock");
	}
	lockdiscovery.appendChild(activeLockEl);
	propertiesManager.setProperty("DAV:lockdiscovery", lockdiscovery);

	PropertyName propname = PropertyName.createPropertyNameQuietly( "DAV:lockdiscovery" );
	// all lock methods return the lockdiscovery property, even if the lock failed
	propertyResponse.addProperty( propname, (Element) ((Element) lockdiscovery).cloneNode(true), WebDAVStatus.SC_OK);
	return result;
}
/** Unlock the lock identified by the lockToken on this resource by
* removing the active lock from the lock discovery property.
*
* @param activeLock the lock to unlock
*
* @return a MultiStatus containing any responses on resources that could not
*     be unlocked.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus unlock(ActiveLock activeLock) throws WebDAVException {
        Element activeLockEl = activeLock.asXML();
        Document xmlDoc = activeLockEl.getOwnerDocument();
	MultiStatus result = new MultiStatus();
	PropertyResponse propertyResponse = new PropertyResponse(resource.getURL().toString());
	result.addResponse(propertyResponse);

	// get the locks on this resource
	PropertyValue l = resource.getProperty( PropertyName.pnLockdiscovery );
	Element lockdiscovery = null;
	if (l == null) {
		lockdiscovery = xmlDoc.createElement("D:lockdiscovery");
		lockdiscovery.setAttribute("xmlns:D", "DAV:");
	} else {
		lockdiscovery = (Element) ((Element) l.value).cloneNode(true);
	}

	// find the lock
	ActiveLock lockToRemove = null;
	NodeList locks = ((Element) lockdiscovery).getElementsByTagNameNS("DAV:", "activelock");
	Element lock = null;
	for (int i = 0; i < locks.getLength(); i++) {
		lock = (Element) locks.item(i);
		ActiveLock aLock = new ActiveLock(lock);
		if (aLock.getLockToken().equals(activeLock.getLockToken())) {
			lockToRemove = aLock;
			lockdiscovery.removeChild(lock);
			break;
		}
	}
	if (lockToRemove == null) {
		throw new WebDAVException(WebDAVStatus.SC_PRECONDITION_FAILED, "resource is not locked with the given lock token");
	} else {
		propertiesManager.setProperty("DAV:lockdiscovery", lockdiscovery);
	}

	// all lock methods return the lockdiscovery property, even if the lock failed
	PropertyName propname = PropertyName.createPropertyNameQuietly( "DAV:lockdiscovery" );
	propertyResponse.addProperty( propname, (Element) ((Element) lockdiscovery).cloneNode(true), WebDAVStatus.SC_OK);
	return result;
}
}
