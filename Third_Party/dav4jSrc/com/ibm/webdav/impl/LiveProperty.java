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
import com.ibm.webdav.*;


/** A LiveProperty represents some property whose semantics is controlled
 * or managed in some way by the server. A live property may be considered
 * an abstraction of a function call for properties. There are a number of
 * contributors of live properties including:
 * <ul>
 *   <li>WebDAV defined live properties that are independent of any repository manager</li>
 *   <li>live properties that are specific to a particular repository manager</li>
 *   <li>user defined live properties</li>
 * </ul></p>
 * <p>
 * Subclasses provide the specifics for each live property.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.PropertyValue
 * @see com.ibm.webdav.impl.ResourceImpl#updateLiveProperties
 * @see com.ibm.webdav.impl.ResourceImpl#removeLiveProperties
 * @see com.ibm.webdav.impl.PropertiesManager#updateLiveProperties
 * @see com.ibm.webdav.impl.PropertiesManager#removeLiveProperties
 */
public abstract class LiveProperty extends Object {
/**
 * Get the name of this live property.
 * @return the live property name (XML namespace+localpart)
 */
public String getName() {
	return getNSName()+getNSLocalName();
}
/**
 * Get the XML tag name (without a prefix) of this live property.
 * @return the live property tag name without a prefix
 */
public abstract String getNSLocalName();
/**
 * Get the XML namespace name (not the prefix) of this live property.
 * The default is "DAV:". Subclasses may want to override this method
 * with their specific namespace name.
 * @return the live property namespace name
 */
public String getNSName() {
	return "DAV:";
}
/**
 * Get the preferred namespace prefix for this live property.
 * @return the XML namespace prefix for this property
 */
public abstract String getPreferredPrefix();
/**
 * Get the value of this live property.
 * @return the live property value
 */
public abstract PropertyValue getValueFor(ResourceImpl resource);
/**
 * Is this live property updatable by the user?
 * @return true if the use can update this live property, false if only
 *    the server can update the property.
 */
public abstract boolean isUserUpdatable();
}
