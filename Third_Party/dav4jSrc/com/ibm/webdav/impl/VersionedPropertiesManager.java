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

import org.w3c.dom.Document;

import com.ibm.webdav.WebDAVException;


/**
 * VersionedPropertiesManager implements all WebDAV property methods that deal with
 * versioned resources that are dependent on a specific repository manager interface. 
 * This manager is used by ResourceImpl and its subclasses to interface with a particular
 * repository manager for accessing and controlling resource version properties. 
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 * @since November 14, 2003
 */
public interface VersionedPropertiesManager extends PropertiesManager {
	public static final String TAG_COMMENT = "comment";
	public static final String TAG_CREATOR_DISPLAYNAME = "creator-displayname";


	/** Remove the version DAV properties from the properties document that
	* do not need to be saved. There is no reason to save them as long
	* as they are recalculated each time the properties are loaded. This
	* method removes the ones that are repository specific.
	*
	* @param propertiesDocument an XML document containing a properties element.
	*/
	public void removeVersionProperties(Document propertiesDocument);
	
	/** Update the version properties that are unique to the
	* repository implementation
	*
	* @param document an XML document containing a properties to update.
	* @exception com.ibm.webdav.WebDAVException
	*/
	public void updateVersionProperties(Document document) throws WebDAVException;

	public boolean isVersionProperty(String sPropName);
}
