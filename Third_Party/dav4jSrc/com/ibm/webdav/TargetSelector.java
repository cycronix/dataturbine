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

/**
 * A target selector can be a label name, label id, working resource id,
 * or an indicator that the VersionedResource itself should be selected.
 * A TargetSelector selects the revision with the associated label or id.
 */
public abstract class TargetSelector {
	Workspace workspace = null;
	String targetSelector = null;
/**
 * Create a TargetSelector that has no Workspace or TargetSelector.
 *
 * @param targetSelector the target selector
 * @exception com.ibm.webdav.WebDAVException
 */
protected TargetSelector() {
}
/**
 * Create a TargetSelector for the given Workspace and selector override.
 * The targetSelector overrides the Workspace for the requested resource.
 * The Workspace is used to select revisions of all other referenced
 * resources.
 *
 * @param workspace the Workspace used by this TargetSelector
 * @param targetSelector the target selector
 * @exception com.ibm.webdav.WebDAVException
 */
public TargetSelector(Workspace workspace, String targetSelector) throws WebDAVException {
	this.workspace = workspace;
	this.targetSelector = targetSelector;
}
/**
 * Create a TargetSelector that has no Workspace.
 *
 * @param targetSelector the target selector
 * @exception com.ibm.webdav.WebDAVException
 */
public TargetSelector(String targetSelector) throws WebDAVException {
	this.targetSelector = targetSelector;
}
/**
 * Get the selector key for this TargetSelector. Each subtype has a different
 * key in order to indicate to the server the type of the TargetSelector. This
 * method is used for marshalling only.
 *
 * @return the selected revision
 * @exception com.ibm.webdav.WebDAVException
 */
public abstract String getSelectorKey() throws WebDAVException;
/**
 * Get the Workspace for this TargetSelector.
 *
 * @return the Workspace used by this TargetSelector
 * @exception com.ibm.webdav.WebDAVException
 */
public Workspace getWorkspace() throws WebDAVException {
	return workspace;
}
}
