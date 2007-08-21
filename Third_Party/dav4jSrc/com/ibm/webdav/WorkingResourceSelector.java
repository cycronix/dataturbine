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
 * A target selector can be a label name, version name, working resource id,
 * or an indicator that the VersionedResource itself should be selected.
 * A TargetSelector selects the indicated revision.
 */
public class WorkingResourceSelector extends TargetSelector {
/**
 * Get the selector key for this TargetSelector.
 *
 * @return the Target-Selector key
 * @exception com.ibm.webdav.WebDAVException
 */
public String getSelectorKey() throws WebDAVException {
	return "working-resource-id "+targetSelector;
}
}
