/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.api;

/**
 * Limits access to a type of resource to a specified maximum number of
 * simultaneous users.
 * <p>
 * This class is used to limit access to things like the files belonging to
 * archives (where the number of simultaneous open files may be limited by the
 * operating system). It allows a certain number of clients access, and then
 * blocks others until one of the current users is done.
 * <p>
 * If desired, an object can hold onto a resource that it may need again in the
 * future.  It must inform the <code>LimitedResource</code> object handling
 * that resource by calling the <code>holdResource</code> method.  If
 * necessary, the <code>LimitedResource</code> object can demand access to the
 * resource if it is needed.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.1
 * @version 10/28/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/28/2003  INB	All synchronization is now on the
 *			<code>heldResources</code> list.
 * 10/23/2003  INB	Added ability to hold resources for future use.
 * 10/22/2003  INB	Use <code>TimerPeriod.LOCK_WAIT</code>.
 * 03/13/2003  INB	Created.
 *
 */
final class LimitedResource {

    /**
     * the current number of users.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int currentUsers = 0;

    /**
     * list of objects holding resources.
     * <p>
     * The list also contains an optional value object that represents the
     * specific resource held so that the object holding it can identify it
     * from a list of resources held.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/23/2003
     */
    private java.util.Hashtable heldResources = new java.util.Hashtable();

    /**
     * the maximum number of allowed users.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int maximumUsers = 1;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    LimitedResource() {
	super();
    }

    /**
     * Class constructor to build a <code>LimitedResources</code> for the
     * specified maximum number of simultaneous users.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maximumUsersI the maximum number of users allowed.
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    LimitedResource(int maximumUsersI) {
	this();
	maximumUsers = maximumUsersI;
    }

    /**
     * Adds a user.
     * <p>
     * If necessary, a held resource is released to this object.
     * <p>
     *
     * @author Ian Brown
     *
     * #exception java.lang.InterruptedException
     *		  thrown if the add is interrupted.
     * @see #removeUser()
     * @since V2.1
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/28/2003  INB	Redid code so that we only synchronize on the
     *			<code>heldResources</code> list.
     * 10/22/2003  INB	Use <code>TimerPeriod.LOCK_WAIT</code>.
     * 03/13/2003  INB	Created.
     *
     */
    public final void addUser()
	throws java.lang.InterruptedException
    {
	long lastAt = System.currentTimeMillis();
	long nowAt;
	synchronized (heldResources) {
	    while (currentUsers == maximumUsers) {
		java.util.Enumeration keys = heldResources.keys();
		if (keys.hasMoreElements()) {
		    // If someone is holding a resource for later, grab it from
		    // them.
		    LimitedResourceInterface lri =
			(LimitedResourceInterface) keys.nextElement();
		    Object heldResource = heldResources.get(lri);
		    heldResources.remove(lri);
		    lri.forcedRelease(heldResource);

		    return;
		}

		heldResources.wait(TimerPeriod.NORMAL_WAIT);

		if (((nowAt = System.currentTimeMillis()) - lastAt) >=
		    TimerPeriod.LOCK_WAIT) {
		    try {
			throw new Exception(this +
					    " at maximum user limit " +
					    maximumUsers +
					    " for " +
					    Thread.currentThread());
		    } catch (Exception e) {
			e.printStackTrace();
			lastAt = nowAt;
		    }
		}
	    }

	    ++currentUsers;
	}
    }

    /**
     * Allows an object to grab a resource that it was holding.
     * <p>
     * A grabbed resource cannot be retrieved by the
     * <code>LimitedResource</code> object, but must instead be explicitly
     * released or held again.
     * <p>
     *
     * @author Ian Brown
     *
     * @param controlledByI the object wishing to grab the resource.
     * @param resourceI     the object representing the resource to be grabbed.
     * @return was the resource grabbed?
     * @see #holdResource(LimitedResourceInterface,Object)
     * @since V2.2
     * @version 10/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Created.
     *
     */
    public final boolean grabResource(LimitedResourceInterface controlledByI,
				      Object resourceI)
    {
	boolean grabbedR = false;

	synchronized (heldResources) {
	    Object actualResource = heldResources.get(controlledByI);
	    if (resourceI == actualResource) {
		heldResources.remove(controlledByI);
		grabbedR = true;
	    }
	}

	return (grabbedR);
    }

    /**
     * Allows an object to hold onto a resource controlled by this
     * <code>LimitedResource</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param controlledByI the object wishing to hold the resource.
     * @param resourceI     the object representing the resource to be held.
     * @see #grabResource(LimitedResourceInterface,Object)
     * @since V2.2
     * @version 10/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Created.
     *
     */
    public final void holdResource(LimitedResourceInterface controlledByI,
				   Object resourceI)
    {
	synchronized (heldResources) {
	    heldResources.put(controlledByI,resourceI);
	    heldResources.notify();
	}
    }

    /**
     * Removes a user.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #addUser()
     * @since V2.1
     * @version 10/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/28/2003  INB	Synchronize on the <code>heldResources</code>.
     * 03/13/2003  INB	Created.
     *
     */
    public final void removeUser() {
	synchronized (heldResources) {
	    --currentUsers;
	    heldResources.notify();
	}
    }
}
