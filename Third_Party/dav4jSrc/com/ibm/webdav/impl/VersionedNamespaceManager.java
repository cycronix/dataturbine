/*
 * (C) Copyright Simulacra Media Ltd, 2004.  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * Simulacra Media Ltd will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will Simulacra Media Ltd be liable for any
 * special, indirect or consequential damages or lost profits even if
 * Simulacra Media Ltd has been advised of the possibility of their occurrence. 
 * Simulacra Media Ltd will not be liable for any third party claims against you.
 * 
 */
package com.ibm.webdav.impl;

import java.util.List;

import com.ibm.webdav.WebDAVException;

/**
 * VersionedNamespaceManager implements all WebDAV namespace methods which deal with
 * versioned resources that are dependent on a specific repository manager interface. 
 * This manager is used by ResourceImpl and its subclasses to interface with a particular
 * repository manager for accessing and controlling versions of resources. 
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 * @since November 17, 2003
 */
public interface VersionedNamespaceManager extends NamespaceManager {

	/**
	 * Checks resource in and returns the verion URI for the checked in version
	 * 
	 * @return
	 * @throws WebDAVException
	 */
	public String checkin() throws WebDAVException;
	
	/**
	 * Checks out the current resource
	 * 
	 * @throws WebDAVException
	 */
	public void checkout() throws WebDAVException;
	
	/**
	 * Returns a <code>List</code> of versions of this resource
	 * 
	 * @return
	 * @throws WebDAVException
	 */
	public List getVersions() throws WebDAVException;

	/**
	 * Uncheckouts trhe current resource
	 * 
	 * @throws WebDAVException
	 */
	public void uncheckout() throws WebDAVException;

	/**
	 * Adds this resource to version control
	 */
	public void versionControl() throws WebDAVException;
	
	/**
	 * Returns <code>true</code> if resource has a version history
	 * 
	 * @return
	 * @throws WebDAVException
	 */
	public boolean isVersioned() throws WebDAVException;
	
	/**
	 * Returns <code>true</code> if resource is checked in
	 * 
	 * @return
	 * @throws WebDAVException
	 */
	public boolean isCheckedInVersion() throws WebDAVException;
	
	/**
	 * Returns <code>true</code> if resource is checked out
	 * 
	 * @return
	 * @throws WebDAVException
	 */
	public boolean isCheckedOutVersion() throws WebDAVException;
	
	/**
	 * Returns <code>true</code> if the given url is a verion url
	 * 
	 * @param parentURL
	 * @return
	 */
	public boolean isVersionURL(String url);

	/**
	 * Returns the resource URL for the resource, used if the url used to obtain
	 * the resource was a version url
	 * 
	 * @return
	 */
	public String getResourceURL() throws WebDAVException;
	

}
