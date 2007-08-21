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

import java.util.*;

/**
 * A Baseline represents a persistent, named set of specific 
 * revisions of versioned resources. Specifically, a Baseline is a
 * Collection that is referenced with a baseline selector (a property
 * of a baselined collection) that contains a version for each
 * version selector in the associated collection.
 */
public class Baseline extends Workspace {
/**
 * Configuration constructor comment.
 */
public Baseline() {
	super();
}
/**
 * Return a list of differences in activities between this 
 * configuration and the given configuration. The differences
 * in activities between configurations gives a high level
 * view of the differences between the configurations.
 *
 * @param target the configuration to compare with
 * this configuration
 * @return an Enumeration of the Activities that are different
 * between the given configuration and this configuration
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration differencesWith(Baseline target) throws WebDAVException {
	return null;
}
}
