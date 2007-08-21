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

package com.ibm.webdav;

import java.io.*;
import java.rmi.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * A versioned resource represents all revisions of a 
 * particular resource. A versioned resource corresponds
 * to the root or initial version of all the revisions of 
 * a resource.
 */
public class VersionedResource extends Resource {
/**
 * VersionedResource constructor comment.
 */
public VersionedResource() {
	super();
}
/**
 * VersionedResource constructor comment.
 * @param resource com.ibm.webdav.Resource
 * @exception com.ibm.webdav.WebDAVException
 */
public VersionedResource(Resource resource) throws WebDAVException {
	super(resource);
}
/**
 * VersionedResource constructor comment.
 * @param url java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
public VersionedResource(String url) throws WebDAVException {
	super(url);
}
/**
 * VersionedResource constructor comment.
 * @param protocol java.lang.String
 * @param host java.lang.String
 * @param port int
 * @param file java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
public VersionedResource(String protocol, String host, int port, String file) throws WebDAVException {
	super(protocol, host, port, file);
}
/**
 * VersionedResource constructor comment.
 * @param protocol java.lang.String
 * @param host java.lang.String
 * @param file java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
public VersionedResource(String protocol, String host, String file) throws WebDAVException {
	super(protocol, host, file);
}
/**
 * VersionedResource constructor comment.
 * @param url java.net.URL
 * @exception com.ibm.webdav.WebDAVException
 */
public VersionedResource(java.net.URL url) throws WebDAVException {
	super(url);
}
/**
 * VersionedResource constructor comment.
 * @param context java.net.URL
 * @param spec java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
public VersionedResource(java.net.URL context, String spec) throws WebDAVException {
	super(context, spec);
}
/** Delete this versioned resource. The versioned resource 
 * and all its revisions are removed from the repository. Any
 * workspaces resolving to a revision of this versioned resource
 * will return resource not found status. When used in the revision
 * selection rule for a workspace, configurations containing a 
 * revision of this resource will result in a resource not found status.
 *
 * @return a MultiStatus containing the status of the delete method on each
 *         effected resource.
 * @exception com.ibm.webdav.WebDAVException
 * @exception RemoteException
 * @exception IOException
 * @exception java.rmi.NotBoundException
 */
public MultiStatus delete() throws WebDAVException {
	return null;
}
/**
 * Get the default target for this versioned resource. The default target
 * specifies the revision selected when no TargetSelector is specified.
 *
 * @return the TargetSelector for the default revision. Must be either
 * a LabelSelector, a RevisionSelector, or null. Null means the versioned resource
 * has no default revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public TargetSelector getDefaultTarget() throws WebDAVException {
	return null;
}
/**
 * A resource may have a number of mutable properties. These are
 * properties that may change even when the resource is checked in.
 * Changes to these properties does not require a new revision.
 *
 * @return an Enumeration of the mutable properties of this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMutableProperties() throws WebDAVException {
	return null;
}
/**
 * Get the revision history for a versioned resource. The revision
 * history lists the revisions of a resource and their predecessors
 * and successors. The format of the document is given in section 
 * Revision History. The document will not contain any revisions 
 * if the resource is not versioned.
 *
 * @return an XML document containing the revision history of the
 * associated versioned resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public Document getRevisionHistory() throws WebDAVException {
	return null;
}
/**
 * Get the revisions of this versioned resource. The first revision is always
 * the initial revision of the versioned resource, but subsequent entries
 * are not in any particular order.
 *
 * @return An Enumeration of revisions of this VersionedResource
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getRevisions() throws WebDAVException {
	return null;
}
/**
 * Get the versioning options for this resource. Versioning options 
 * are established by the server and include:
 * <ul>
 *   <li>Mutable/immutable revisions</li>
 *   <li>Supports multiple activities </li>
 *   <li>Can be a member of a configuration</li>
 *   <li>Is automatically versioned</li>
 * </ul>
 *
 * @return an XML Element containing the versioning options for
 * this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public Element getVersioningOptions() throws WebDAVException {
	return null;
}
/**
 * Get the current working resources of this versioned resource.
 *
 * @return An Enumeration of current working resources of this VersionedResource
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getWorkingResources() throws WebDAVException {
	return null;
}
/**
 * A resource can be automatically versioned on each method
 * that updates its state (content or properties). Non-versioning
 * aware clients use automatic versioning to support updates. If
 * a resource is not automatically versioned, attempts to update
 * the revision without explicitly checking it out first will fail.
 *
 * @return true if this resource is automatically versioned,
 * false if not.
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isAutomaticallyVersioned() throws WebDAVException {
	return false;
}
/**
 * When the DAV:linear property of a versioned resource is true, only 
 * one working resource can be checked out from that versioned resource 
 * at any time, and only the revision that has no successors can be checked
 * out.
 *
 * @return true if this resource is constrained to have a linear revision history,
 * false if not.
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isLinear() throws WebDAVException {
	return false;
}
/**
 * Set the default target for this versioned resource. The default target
 * specifies the revision selected when no TargetSelector is specified.
 *
 * @param targetSelector the TargetSelector for the default revision. Must be either
 * a LabelSelector, a RevisionSelector, or null. Null means the versioned resource
 * has no default revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public void setDefaultTarget(TargetSelector targetSelector) throws WebDAVException {
	
}
}
