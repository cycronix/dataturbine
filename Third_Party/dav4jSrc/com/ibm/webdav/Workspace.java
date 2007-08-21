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

import java.util.*;

/**
 * A workspace is a resource that provides a means to map 
 * versioned resources to particular versions. The members
 * of a workspace are version selectors and unversioned
 * resources. The targets of the version selectors identifies
 * the version contained in the workspace. A workspace 
 * may contain a set of activitys that encapsulate changes 
 * made to resources in that workspace.
 */
public class Workspace extends Collection {
/**
 * Baseline this Workspace to save the selected revisions.
 *
 * @return a Baseline containing all the revisions selectable through
 * this workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public void baseline() throws WebDAVException {
}
/**
 * Create a Workspace at the given location. Servers may require workspaces
 * to be created in a designated portion of the URL namespace.
 *
 * @return the newly created Workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public static Workspace create(java.net.URL url) throws WebDAVException {
	return null;
}
/**
 * Return a list of differences in activities between this 
 * workspace and the given workspace. The differences
 * in activities between a workspace and a baseline gives a high level
 * view of their differences.
 *
 * @param target the baseline to compare with
 * this workspace
 * @return an Enumeration of the Activities that are different
 * between the given baseline and this workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration differencesWith(Baseline target) throws WebDAVException {
	return null;
}
/**
 * Get the activities for revisions that were changed in this workspace.
 * These activities are candidates for merging into some integration
 * Workspace.
 *
 * @return an Enumeration ofthe Activities for revisions modified in this workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getActivities() throws WebDAVException {
	return null;
}
/**
 * Get the current activity for this workspace. Checkouts are
 * done in the context of the current activity if any. A workspace
 * can only have one current activity at a time. If this activity
 * is in the revision selection rule of the workspace used to
 * create the revision, the updates to the revision will remain
 * visible after the working resource is checked in. The current
 * label may also be used to perform this function if activities
 * are not supported or used.
 *
 * @return the current Activity associated with changes made in
 * this workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public Activity getCurrentActivity() throws WebDAVException {
	return null;
}
/**
 * Return an XML document containing the merge conflicts that would
 * result if the mergeSource was merged into this Workspace.
 * Merge conflicts arrise when a revision selected by one revision
 * selector is on a different line of descent than that selected
 * by some other revision selector in the workspace. Merge conflicts
 * can be resolved by merging the mergeSource into the workspace and
 * updating any working resources that result from merge conflicts.
 *
 * @return an Enumeration of the Resources that would be in conflict
 * resulting from merging the mergeSource into this Workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMergeConflicts(Activity mergeSource) throws WebDAVException {
	return null;
}
/**
 * Return an XML document containing the merge conflicts that would
 * result if the mergeSource was merged into this Workspace.
 * Merge conflicts arrise when a revision selected by one revision
 * selector is on a different line of descent than that selected
 * by some other revision selector in the workspace. Merge conflicts
 * can be resolved by merging the mergeSource into the workspace and
 * updating any working resources that result from merge conflicts.
 *
 * @return an Enumeration of the Resources that would be in conflict
 * resulting from merging the mergeSource into this Workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMergeConflicts(Baseline mergeSource) throws WebDAVException {
	return null;
}
/**
 * Return an XML document containing the merge conflicts that would
 * result if the mergeSource was merged into this Workspace.
 * Merge conflicts arrise when a revision selected by one revision
 * selector is on a different line of descent than that selected
 * by some other revision selector in the workspace. Merge conflicts
 * can be resolved by merging the mergeSource into the workspace and
 * updating any working resources that result from merge conflicts.
 *
 * @return an Enumeration of the Resources that would be in conflict
 * resulting from merging the mergeSource into this Workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMergeConflicts(Workspace mergeSource) throws WebDAVException {
	return null;
}
/**
 * Merge the mergeSource with this workspace. 
 * This results in potentially selecting different revisions, 
 * and the creation of working resources resulting from merge conflicts. 
 * 
 * If the mergeSource includes mutable revisions, the merge conflicts are not reliable.
 *
 * @param mergeSource the Workspace to merge into this workspace
 * @return an Enumeration of the Resources that were in conflict as a result of the merge.
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.Resource#mergeWith
 */
public Enumeration mergeWith(Activity mergeSource) throws WebDAVException {
	return null;
}
/**
 * Merge the mergeSource with this workspace. 
 * This results in potentially selecting different revisions, 
 * and the creation of working resources resulting from merge conflicts. 
 * 
 * If the mergeSource includes mutable revisions, the merge conflicts are not reliable.
 *
 * @param mergeSource the Workspace to merge into this workspace
 * @return an Enumeration of the Resources that were in conflict as a result of the merge.
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.Resource#mergeWith
 */
public Enumeration mergeWith(Baseline mergeSource) throws WebDAVException {
	return null;
}
/**
 * Merge the mergeSource with this workspace. 
 * This results in potentially selecting different revisions, 
 * and the creation of working resources resulting from merge conflicts. 
 * 
 * If the mergeSource includes mutable revisions, the merge conflicts are not reliable.
 *
 * @param mergeSource the Workspace to merge into this workspace
 * @return an Enumeration of the Resources that were in conflict as a result of the merge.
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.Resource#mergeWith
 */
public Enumeration mergeWith(Resource mergeSource) throws WebDAVException {
	return null;
}
/**
 * Merge the mergeSource with this workspace. 
 * This results in potentially selecting different revisions, 
 * and the creation of working resources resulting from merge conflicts. 
 * 
 * If the mergeSource includes mutable revisions, the merge conflicts are not reliable.
 *
 * @param mergeSource the Workspace to merge into this workspace
 * @return an Enumeration of the Resources that were in conflict as a result of the merge.
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.Resource#mergeWith
 */
public Enumeration mergeWith(Workspace mergeSource) throws WebDAVException {
	return null;
}
/**
 * Set the current activity for this workspace. Checkouts are
 * done in the context of the current activity if any. A workspace
 * can only have one current activity at a time. If this activity
 * is in the revision selection rule of the workspace used to
 * create the revision, the updates to the revision will remain
 * visible after the working resource is checked in. The current
 * label may also be used to perform this function if activities
 * are not supported or used.
 *
 * @param value the current Activity associated with changes made in
 * this workspace
 * @exception com.ibm.webdav.WebDAVException
 */
public void setCurrentActivity(Activity currentActivity) throws WebDAVException {
}
}
