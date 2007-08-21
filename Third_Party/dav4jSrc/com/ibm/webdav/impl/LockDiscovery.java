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

import org.w3c.dom.*;

import com.ibm.webdav.*;

/** A LockDiscovery is a LiveProperty corresponding to the DAV:lockdiscovery property.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.PropertyValue
 * @see com.ibm.webdav.impl.ResourceImpl#updateLiveProperties
 * @see com.ibm.webdav.impl.ResourceImpl#removeLiveProperties
 * @see com.ibm.webdav.impl.PropertiesManager#updateLiveProperties
 * @see com.ibm.webdav.impl.PropertiesManager#removeLiveProperties
 */
public class LockDiscovery extends LiveProperty {
/**
 * Get the name of this live property.
 * @return the live property name (XML namespace+localpart)
 */
public String getName() {
	return "DAV:lockdiscovery";
}
/**
 * Get the XML tag name (without a prefix) of this live property.
 * @return the live property tag name without a prefix
 */
public String getNSLocalName() {
	return "lockdiscovery";
}
/**
 * Get the preferred namespace prefix for this live property.
 * @return the XML namespace prefix for this property
 */
public String getPreferredPrefix() {
	return "D";
}
/**
 * Get the value of this live property.
 * @return the live property value
 */
public PropertyValue getValueFor(ResourceImpl resource) {
	Element lockdiscovery = null;
	int status = WebDAVStatus.SC_OK;
	try {
		lockdiscovery = (Element)((Element)resource.getLockManager().getLockDiscovery()).cloneNode(true);
	} catch (WebDAVException exc) {
		status = exc.getStatusCode();
	} catch (Exception exc) {
		status = WebDAVStatus.SC_INTERNAL_SERVER_ERROR;
	}
	PropertyValue result = new PropertyValue(lockdiscovery, status);
	return result;
}
/**
 * Is this live property updatable by the user?
 * @return true if the use can update this live property, false if only
 *    the server can update the property.
 */
public boolean isUserUpdatable() {
	return false;
}
}
