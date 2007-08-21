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
public class VersionSelector extends TargetSelector {
/**
 * Get the selector key for this TargetSelector. 
 *
 * @return the Target-Selector key
 * @exception com.ibm.webdav.WebDAVException
 */
public String getSelectorKey() throws WebDAVException {
	return "revision-id "+targetSelector;
}
}
