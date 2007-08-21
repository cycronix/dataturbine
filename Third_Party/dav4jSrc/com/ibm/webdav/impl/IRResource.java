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
import java.util.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;


/** A Resource represents any network data object or service that can be
 * identified by a URL. Resources may be available in multiple 
 * representations (e.g., multiple languages, data formats, size, resolutions)
 * or vary in other ways.
 * <p>
 * Resources may have arbitrary properties consisting of name/value pairs that
 * define additional meta-data about the resource. A resource may be locked
 * in order to serialize updates by multiple users in a distributed environment.
 * Resources may be copied and moved in the network, and may be deleted when 
 * no longer needed.</p>
 * <p>
 * Resources may be logically grouped into collections for content management.
 * A resource collection may have properties of its own, and can be moved and
 * copied just like any other resource.</p>
 * <p>
 * The Resource methods correspond to the capabilities defined 
 * by the WebDAV extensions to HTTP. These methods allow clients to perform
 * remote web content authoring operations.</p>
 * <p>
 * Note: all methods that may modify a locked resource (either this resource or one
 * of its collaborators) must include the lock token of the effected resources
 * as a Precondition in the resource request context before the method is called. See
 * Precondition.addStateTokenCondition() for a method that provides a convenient
 * way to set these preconditions.</p>
 * @see com.ibm.webdav.Collection
 * @see com.ibm.webdav.ResourceP
 * @see com.ibm.webdav.Precondition#addStateTokenCondition
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 *
 */
public interface IRResource extends java.rmi.Remote
{
/** This method must be called after the client has completed writing to the contents
 * output stream that was obtained from <code>getContentsOutputStream()</code>.
 * @exception com.ibm.webdav.WebDAVException
 */
public void closeContentsOutputStream(ResourceContext context) throws WebDAVException;
/** Copy this resource to the destination URL.
 * Partial results are possible, check the returned status for details.
 *
 * @param destinationURL the destination
 * @param overwrite true implies overwrite the destination if it exists
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
public MultiStatus copy(ResourceContext context, String destinationURL, boolean overwrite, Vector propertiesToCopy) throws WebDAVException;
/** Delete this resouce from the server. The actual effect of the delete operation is
 * determined by the underlying repository manager. The visible effect to WebDAV
 * is that the resource is no longer available.
 *
 * @return a MultiStatus containing the status of the delete method on each
 *         effected resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus delete(ResourceContext context) throws WebDAVException;
/** Get an InputStream for accessing the contents of this resource. This method may provide
 * more efficient access for resources that have large contents. Clients may want to create
 * a Reader to perform appropriate character conversions on this stream.
 *
 * @return an InputStream on the contents
 * @exception com.ibm.webdav.WebDAVException
 */
public InputStream getContentsInputStream(ResourceContext context) throws WebDAVException;
/** Get an OutputStream for setting the contents of this resource. This method may provide
 * more efficient access for resources that have large contents. Remember to call
 * closeContentsOutputStream() when all the data has been written.
 *
 * @return an OutputStream to set the contents
 * @exception com.ibm.webdav.WebDAVException
 */
public OutputStream getContentsOutputStream(ResourceContext context) throws WebDAVException;
/** This method can be used for obtaining meta-information about this resource without
 * actually reading the resource contents. This meta-information is maintained by the server
 * in addition to the resource properties.</p>
 * <p>
 * After this call, the resource context has been updated and 
 * <code>getStatusCode()</code>, <code>getStatusMessage()</code>, and <code>getResponseContext()</code>
 * as well as all the ResourceContext methods return updated values based on the current
 * state of the resource.</p>
 * <p>This methods corresponds to the HTTP HEAD method.</p>
 *
 * @exception com.ibm.webdav.WebDAVException
 */
public void getMetaInformation(ResourceContext context) throws WebDAVException;
/** Get all the properties of this resource.
 *
 * @return a MultiStatus of PropertyResponses. It should contain only one
 * response element.
 * @see com.ibm.webdav.MultiStatus
 * @see com.ibm.webdav.PropertyResponse
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus getProperties(ResourceContext context) throws WebDAVException;
/** Get the named properties of this resource. 
 * 
 * @param names an array of property names to retrieve
 *
 * @return a MultiStatus of PropertyResponses
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.PropertyResponse
 */
public MultiStatus getProperties(ResourceContext context, PropertyName names[]) throws WebDAVException;
/** Get the names of all properties for this resource. The result is similar to
 * getProperties(), but the properties have no values.
 *
 * @return a MultiStatus of PropertyResponses
 * (PropertyValue.value is always null, PropertyValue.status contains the status)
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.PropertyResponse
 */
public MultiStatus getPropertyNames(ResourceContext context) throws WebDAVException;
/** Lock this resource based on the given parameters. This allows control of
 * the lock scope (exclusive or shared) the lock type (write), owner information, etc.
 *
 * @param scope the scope of the lock, exclusive or shared
 * @param type the type of the lock, currently only write
 * @param timeout the number of seconds before the lock times out or
 *     -1 for infinite timeout.
 * @param owner an XML element containing useful information that can be
 *     used to identify the owner of the lock. An href to a home page, an
 *     email address, phone number, etc. Can be null if no owner information
 *     is provided.
 *
 * @return a MultiStatus containing a lockdiscovery property indicating
 * the results of the lock operation.
 * @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus lock(ResourceContext context, String scope, String type, int timeout, Element owner) throws WebDAVException;
/** Move this resource to the destination URL.
 * Partial results are possible, check the returned status for details
 *
 * @param destinationURL the destination
 * @param overwrite true implies overrite the destination if it exists
 * @param propertiesToMove a collection of properties that must be moved or
 * the method will fail. propertiesToMove may have one of the following values:
 * <ul>
 *    <li>null - ignore properties that cannot be moved</li>
 *    <li>empty collection - all properties must be moved or the method will fail</li>
 *    <li>a collection of URIs - a list of the properties that must be moved 
 *        or the method will fail</li>
 * </ul>
 *
 * @return the status of the move operation for each resource moved
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus move(ResourceContext context, String destinationURL, boolean overwrite, Vector propertiesToMove) throws WebDAVException;
/** This method treats this resource as a method or service, and sends its parameter to
 * this resource where it is handled in a resource-specific way. For example,
 * sending data from an HTML form to a URL representing a Servlet or CGI script that processes
 * the form data to produce some result.
 *
 * @param args a string representing the arguments to the method represented by this URL. The
 * arguments are in the form ?parameterName1=value1&amp;parameterName2=value2... as specified
 * for URL queries.
 *
 * @return the results of sending the arguments to the URL
 * @exception com.ibm.webdav.WebDAVException
 */
public byte[] performWith(ResourceContext context, String args) throws WebDAVException;
/** Refresh the lock on this resource by resetting the lock timeout.
 * The context must contain the proper authorization for the requesting
 * principal.
 *
 * @param lockToken the lock token identifying the lock.
 * @param timeout the new timeout in seconds. -1 means infinite timeout.
 *
 * @return updated information about the lock status of this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus refreshLock(ResourceContext context, String lockToken, int timeout) throws WebDAVException;
/** Edit the properties of a resource. The updates must refer to a Document containing a WebDAV
 * DAV:propertyupdates element as the document root.
 *
 * @param updates an XML Document containing DAV:propertyupdate elements 
 * describing the edits to be made
 * @return a MultiStatus indicating the status of the updates
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus setProperties(ResourceContext context, Document updates) throws WebDAVException;
/** Unlock the lock identified by the lockToken on this resource. The request context
 * must contain the proper authorization.
 *
 * @param lockToken the lock token obtained from the ActiveLock of a previous <code>lock() </code>
 * or <code>getLocks()</code>.
 *
 * @return a MultiStatus containing any responses on resources that could not
 *     be unlocked.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus unlock(ResourceContext context, String lockToken) throws WebDAVException;
/**
 * @param context
 * @param sContentType
 */
public void closeContentsOutputStream(ResourceContext context, String sContentType) throws WebDAVException;
}
