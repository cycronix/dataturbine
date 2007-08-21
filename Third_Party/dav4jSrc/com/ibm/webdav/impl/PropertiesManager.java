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
import java.rmi.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;


/** PropertiesManager implements all WebDAV property methods that are
 * dependent on a specific repository manager interface. This manager is
 * used by ResourceImpl and its subclasses to interface with a particular
 * repository manager for accessing and controlling resource properties. Implementing
 * this interface along with NamespaceManager and LockManager is all that
 * is needed to provide WebDAV access to a particular repository manager.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public interface PropertiesManager {
/** Delete all properties.
*
* @exception com.ibm.webdav.WebDAVException
*/
public void deleteProperties() throws WebDAVException;
/** Get all the WebDAV properties WebDAV of the managed resoure.
*
* @return a MultiStatus of PropertyResponses for the properties of the resouorce.
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getProperties() throws WebDAVException;
/** Get the named properties for the managed resource.
*
* @param names an array of property names to retrieve (namespace+localpart)
* @return a MultiStatus of PropertyResponses
* @exception com.ibm.webdav.WebDAVException
*/

public MultiStatus getProperties( PropertyName names[]) throws WebDAVException;
/** Get the names of all properties for the managed resource
*
* @return a MultiStatus of PropertyResponses
* (the contained PropertyValue.value is always null, but PropertyValue.status contains the status)
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getPropertyNames() throws WebDAVException;
/** Initialize this PropertiesManager instance.
* @param resource the resource to manager properties for
* @param namespaceManager its namespace manager
*/
public void initialize(ResourceImpl resource,
					   com.ibm.webdav.impl.NamespaceManager namespaceManager);
/** Load properties from their persistent store.
*
* @return an XML document containing a properties element.
* @exception com.ibm.webdav.WebDAVException
*/
public Document loadProperties() throws WebDAVException;
/** Remove the live DAV properties from the properties document that
* do not need to be saved. There is no reason to save them as long
* as they are recalculated each time the properties are loaded. This
* method removes the ones that are repository specific.
*
* @param propertiesDocument an XML document containing a properties element.
*/
public void removeLiveProperties(Document propertiesDocument);
/** Save the properties to the persistent store.
*
* @param propertiesDocument an XML document containing a properties element.
* @exception com.ibm.webdav.WebDAVException
*/
public void saveProperties(Document propertiesDocument) throws WebDAVException;
/** Edit the properties of the managed resource. The updates must refer to a Document containing a WebDAV
* propertyupdates element as the document root.
*
* @param updates an XML Document containing propertyupdate elements
* @return a MultiStatus describing the result of the updates
* describing the edits to be made
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus setProperties(Document updates) throws WebDAVException;
/** Set a property of the managed resource to a value.
*
* @param name the property name (namespace+local part of the value element)
* @param value the property value
* @exception com.ibm.webdav.WebDAVException
* @exception IOException
* @exception RemoteException
*/
public void setProperty(String name, Element value) throws WebDAVException;
/** Update the live properties that are unique to the
* repository implementation
*
* @param document an XML document containing a properties to update.
* @exception com.ibm.webdav.WebDAVException
*/
public void updateLiveProperties(Document document) throws WebDAVException;
}
