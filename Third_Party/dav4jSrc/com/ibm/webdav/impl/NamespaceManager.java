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

/** NamespaceManager implements all WebDAV namespace methods that are
 * dependent on a specific repository manager interface. This manager is
 * used by ResourceImpl and its subclasses to interface with a particular
 * repository manager for accessing and controlling resources. Implementing
 * this interface along with PropertiesManager and LockManager is all that
 * is needed to provide WebDAV access to a particular repository manager.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public interface NamespaceManager {
/** Close any opened OutputStream on the contents of the managed resource.
* @exception com.ibm.webdav.WebDAVException
*/
public void closeContentsOutputStream() throws WebDAVException;
/** Create a collection with the given local name.
* @param localName the repository specific local name for the resource
* @exception com.ibm.webdav.WebDAVException
*/
public void createCollection(String localName) throws WebDAVException;
/** Create this resource as a lock-null resource, a resource created by locking one that
* did not previously exist.
* @exception com.ibm.webdav.WebDAVException
*/
public void createLockNullResource() throws WebDAVException;
/** Delete the managed resource from the repository.
* @exception com.ibm.webdav.WebDAVException
*/
public void delete() throws WebDAVException;
/**
 * Create new binding
 * @param path
 * @throws WebDAVException
 */
public void createBinding(String bindName,URL source) throws WebDAVException;

/** Move the managed resource
* @exception com.ibm.webdav.WebDAVException
*/
public void move(String path) throws WebDAVException;
/** See if this resource exists in the repository.
*
* @return true if the resource exists, false otherwise.
* @exception com.ibm.webdav.WebDAVException
*/
public boolean exists() throws WebDAVException;
/** Open an InputStream on the contents of the managed resource.
*
* @return an InputStream on the contents of the managed resource.
* @exception com.ibm.webdav.WebDAVException
*/
public InputStream getContentsInputStream() throws WebDAVException;
/** Open an OutputStream in order to write the contents of the managed resource.
*
* @return an OutputStream on the contents of the managed resource.
* @exception com.ibm.webdav.WebDAVException
*/
public OutputStream getContentsOutputStream() throws WebDAVException;
/** Get the members of a collection.
*
* @return a Vector of Resources (ResourceImpl or CollectionImpl)
* @exception com.ibm.webdav.WebDAVException
*/
public Vector getMembers() throws WebDAVException;
/** Initialize this NamespaceManager instance.
* @param resource the resource to manage
*/
public void initialize(ResourceImpl resource);
/** Is the managed resource a collection?
* @return true if this resource is a collection, false otherwise
* @exception com.ibm.webdav.WebDAVException
*/
public boolean isCollection() throws WebDAVException;
/**
 * Is the managed resource versionable?
 * 
 * @return
 * @throws WebDAVException
 */
public boolean isVersionable() throws WebDAVException;
/** Is this resource in the lock-null state?
* @return true if this resource is a lock-null resource, false otherwise
* @exception com.ibm.webdav.WebDAVException
*/
public boolean isLockNull() throws WebDAVException;
/** Treat the managed resource as a method and execute it with the given arguments.
* Ths should be done by the host Web server. Executes CGI scripts and Servlets. But
* the repository may have some additional capabilities of its own.
* @param args the URL query string (text following the ?)
* @return the contents of the result
* @exception com.ibm.webdav.WebDAVException
*/
public byte[] performWith(String args) throws WebDAVException;

/**
 * Returns a list of methods allowed on this resource
 * 
 * @return
 * @throws WebDAVException
 */
public List getAllowedMethods() throws WebDAVException;
/**
 * Sets the ordering of members within collection
 * 
 * @param orderPatch
 */
public void setOrdering(Document orderPatch) throws WebDAVException;
/**
 * @return
 */
public String getContentType() throws WebDAVException;
/**
 * @param sContentType
 */
public void closeContentsOutputStream(String sContentType) throws WebDAVException;

}
