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
 */

package com.ibm.webdav;

import java.util.Enumeration;

/**
 * An activity represents a named set of revisions to versioned 
 * resources. A revision of a resource may be checked out in the 
 * context of the current activity of the workspace. Edits are 
 * made in the context of that activity, and the association is 
 * maintained when the resource is checked back in. The same resource 
 * can be checked out in many different activities at the same time 
 * if the server supports parallel development on that resource. These
 * activities may be merged into other workspaces at some later time 
 * to combine the changes. Activities may be used to manage units of 
 * work required to update a set of resources in some related way. 
 */
public class Activity extends Resource {
/**
 * Activity constructor comment.
 */
private Activity() {
	super();
}
/**
 * Activity constructor comment.
 * @param resource com.ibm.webdav.Resource
 * @exception com.ibm.webdav.WebDAVException
 */
private Activity(Resource resource) throws WebDAVException {
	super(resource);
}
/**
 * Activity constructor comment.
 * @param url java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
private Activity(String url) throws WebDAVException {
	super(url);
}
/**
 * Activity constructor comment.
 * @param protocol java.lang.String
 * @param host java.lang.String
 * @param port int
 * @param file java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
private Activity(String protocol, String host, int port, String file) throws WebDAVException {
	super(protocol, host, port, file);
}
/**
 * Activity constructor comment.
 * @param protocol java.lang.String
 * @param host java.lang.String
 * @param file java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
private Activity(String protocol, String host, String file) throws WebDAVException {
	super(protocol, host, file);
}
/**
 * Activity constructor comment.
 * @param url java.net.URL
 * @exception com.ibm.webdav.WebDAVException
 */
private Activity(java.net.URL url) throws WebDAVException {
	super(url);
}
/**
 * Activity constructor comment.
 * @param context java.net.URL
 * @param spec java.lang.String
 * @exception com.ibm.webdav.WebDAVException
 */
private Activity(java.net.URL context, String spec) throws WebDAVException {
	super(context, spec);
}
/** 
* An activity may depend on a number of other activities in order to
* be complete. When an activity is added to a workspace revision
* selection rule, its dependent activities are also added.
*
* @param dependent the dependent activity to add
* @exception com.ibm.webdav.WebDAVException
*/
public void addDependent(Activity dependent) throws WebDAVException {
}
/** 
* Add a label to the latest revision of all resources mofified in this
* activity. Any effected revision
* must not already have the label, and the label cannot be the 
* ame as the revision id. The operation will fail if the collection
* is not a versioned resource.
* <p>
* Labels are used to provide meaningful names that distinguish 
* revisions of versioned resources. They can be used in the revision
* selection rule of the workspace to specify what revision should be
* selected by that workspace. A specific label overriding the workspace 
* may also be used to access revisions.
*
* @param label the label to add to this revision and potentially
* all its members
* @exception com.ibm.webdav.WebDAVException
*/
public void addLabel(String label) throws WebDAVException {
}
/**
 * See if an activity contains a resource. That is, see if a resource
 * was revised in the context of this activity.
 *
 * @param revision the resource to check for
 * @return true if the resource revision was updated in this
 * activity
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean contains(Resource revision) throws WebDAVException {
	return false;
	
}
/**
 * Create an Activity at the given location. Servers may require Activities
 * to be created in a designated portion of the URL namespace.
 *
 * @return the newly created Activity
 * @exception com.ibm.webdav.WebDAVException
 */
public static Activity create(java.net.URL url) throws WebDAVException {
	return null;
}
/** 
* An activity may depend on a number of other activities in order to
* be complete. When an activity is added to a workspace revision
* selection rule, its dependent activities are also added.
*
* @return an Enumeration of the Activities this activity dependents on
* @exception com.ibm.webdav.WebDAVException
*/
public Enumeration getDependents() throws WebDAVException {
	return null;
}
/**
 * The members of an activity are the revisions that were modified
 * in that activity.
 *
 * @return an Enumeration of Resources that were revised in this activity.
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMembers() throws WebDAVException {
	return null;
	
}
/** 
* An activity may depend on a number of other activities in order to
* be complete. When an activity is added to a workspace revision
* selection rule, its dependent activities are also added.
*
* @param dependent the dependent activity to remove
* @exception com.ibm.webdav.WebDAVException
*/
public void removeDependent(Activity dependent) throws WebDAVException {
}
/** 
* Remove a label from the latest revision of all resource modified
* in the context of this activity. An exception is raised if the revision 
* does not have this label.
*
* @param label the label to remove from this revision and potentially
* all its members
* @exception com.ibm.webdav.WebDAVException
*/
public void removeLabel(String label) throws WebDAVException {
}
}
