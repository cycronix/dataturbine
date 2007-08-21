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
 */
import java.util.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;

/** LockManager implements all WebDAV locking methods that are
 * dependent on a specific repository manager interface. This manager is
 * used by ResourceImpl and its subclasses to interface with a particular
 * repository manager for locking and unlocking resources. Implementing
 * this interface along with NamespaceManager and PropertiesManager is all that
 * is needed to provide WebDAV access to a particular repository manager.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public interface LockManager {
/** Get the lockdiscovery DAV property for the resource.
*
* @return an Element with tag name D:lockdiscovery
* @exception com.ibm.webdav.WebDAVException
*/
public Element getLockDiscovery() throws WebDAVException;
/** Get the locks that exist on this resource. May be null if the resource
* is not locked.
*
* @return a Vector of ActiveLock objects
* @exception com.ibm.webdav.WebDAVException
*/
public Vector getLocks() throws WebDAVException;
/** Get information about locks supported by this resource.
*
* @return an Element with tag name D:supportedlock
*/
public Element getSupportedLock();
/** Initialize an this LockManager instance.
* @param resource the resource to manage locks for
* @param namespaceManager its namespace manager
* @param propertiesManager its properties manager
*/
public void initialize(ResourceImpl resource, 
					   com.ibm.webdav.impl.NamespaceManager namespaceManager,
					   com.ibm.webdav.impl.PropertiesManager propertiesManager);
/** Lock this resource Using the given activeLock.
*
* @param activeLock the lock to activate (i.e., persist)
*
* @return a MultiStatus containing a lockdiscovery property indicating
* the results of the lock operation.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus lock(ActiveLock activeLock) throws WebDAVException;
/** Refresh the lock on this resource by resetting the lock timeout.
* The context must contain the proper authorization for the requesting
* principal.
*
* @param activeLock the lock to refresh. Contains the updated timeout
* and expiration date.
*
* @return updated information about the lock status of this resource
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus refreshLock(ActiveLock activeLock) throws WebDAVException;
/** Unlock the lock identified by the lockToken on this resource
*
* @param activeLock the lock to unlock
*
* @return a MultiStatus containing any responses on resources that could not
*     be unlocked.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus unlock(ActiveLock activeLock) throws WebDAVException;
}
