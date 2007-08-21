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


/** A Collection is a Resource that contains other
 * resources including other Collections following the composite pattern.
 * It is useful for managing logically
 * related groups of resources for authorization, permissions, move, copy, group properties,
 * location, etc. by clients.
 * @see com.ibm.webdav.CollectionP
 * @see com.ibm.webdav.ResourceP
 * @see com.ibm.webdav.Precondition#addStateTokenCondition
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public interface IRCollection extends IRResource {
/** Copy this resource to the destination URL.
* Partial results are possible, check the returned status for details.
*
* @param destinationURL the destination
* @param depth an indicator for immediate members or recursively all children.
* <ul>
*    <li>shallow: copy only this resource</li>
*    <li>deep: copy this resource and recursively all of its children</li>
* </ul>
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
public MultiStatus copy(ResourceContext context, String destinationURL, boolean overwrite, Vector propertiesToCopy, String depth) throws WebDAVException;
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
public MultiStatus createCollection(ResourceContext context, Document contents) throws WebDAVException;
/** Delete this resouce collection and all its members from the server. 
 * The actual effect of the delete operation is determined by the underlying
 * repository manager. The visible effect to WebDAV is that the resource
 * is no longer available.
 *
 * @return a MultiStatus containing the status of the delete method on each
 *         effected resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus delete(ResourceContext context) throws WebDAVException;
/** Get the named properties for this resource and (potentially) its children.
*
* @param names an arrary of property names to retrieve. 
* @param depth an indicator for immediate members or recursively all children.
* <ul>
*    <li>immediateMembers: propeprties of this resource and its immediate children</li>
*    <li>allMembers: properties of this resource and recursively all its children</li>
* </ul>
*
* @return a MultiStatus of PropertyResponses
* @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus getProperties(ResourceContext context, PropertyName names[], String depth) throws WebDAVException;
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
public MultiStatus getProperties(ResourceContext context, String depth) throws WebDAVException;
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
public MultiStatus getPropertyNames(ResourceContext context, String depth) throws WebDAVException;
/** Lock this resource collection and potentially all its members
* based on the given parameters. This allows control of the lock 
* scope (exclusive or shared) the lock type (write), owner information, etc.
*
* @param scope the scope of the lock, exclusive or shared
* @param type the type of the lock, currently only write
* @param depth
*     <ul>
*         <li>shallow lock only this resource</li>
*         <li>deep lock this resource and all its children</li>
*     </ul>
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
public MultiStatus lock(ResourceContext context, String scope, String type, int timeout, Element owner, String depth) throws WebDAVException;
}
