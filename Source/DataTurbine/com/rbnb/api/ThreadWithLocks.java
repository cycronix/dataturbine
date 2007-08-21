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
 * Extended <code>Thread</code> that stores information about the
 * <code>Door.Locks</code> associated with it.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Door
 * @see com.rbnb.api.Lock
 * @since V2.2
 * @version 02/11/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/11/2004  INB	Log exceptions at standard level.
 * 11/14/2003  INB	Created.
 *
 */
class ThreadWithLocks
    extends java.lang.Thread
{
    /**
     * the list of <code>Locks</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/14/2003
     */
    private java.util.Vector locks = new java.util.Vector();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks() {
	super();
    }

    /**
     * Builds a <code>ThreadWithLocks</code> for a <code>Runnable</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param targetI the <code>Runnable</code>.
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks(Runnable targetI) {
	super(targetI);
    }

    /**
     * Builds a <code>ThreadWithLocks</code> for a <code>Runnable</code> object
     * with the specified name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param targetI the <code>Runnable</code>.
     * @param nameI   the name of the <code>ThreadWithLocks</code>.
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks(Runnable targetI,String nameI) {
	super(targetI,nameI);
    }

    /**
     * Builds a <code>ThreadWithLocks</code> with the specified name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the <code>ThreadWithLocks</code>.
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks(String nameI) {
	super(nameI);
    }

    /**
     * Builds a <code>ThreadWithLocks</code> for a <code>Runnable</code> object
     * in the specified <code>ThreadGroup</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param groupI  the <code>ThreadGroup</code>.
     * @param targetI the <code>Runnable</code>.
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks(ThreadGroup groupI,Runnable targetI) {
	super(groupI,targetI);
    }

    /**
     * Builds a <code>ThreadWithLocks</code> for a <code>Runnable</code> object
     * with the specified name in the specified <code>ThreadGroup</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param groupI  the <code>ThreadGroup</code>.
     * @param targetI the <code>Runnable</code>.
     * @param nameI   the name of the <code>ThreadWithLocks</code>.   
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks(ThreadGroup groupI,Runnable targetI,String nameI) {
	super(groupI,targetI,nameI);
    }

    /**
     * Builds a <code>ThreadWithLocks</code> with the specified name in the
     * specified <code>ThreadGroup</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param groupI  the <code>ThreadGroup</code>.
     * @param nameI   the name of the <code>ThreadWithLocks</code>.   
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public ThreadWithLocks(ThreadGroup groupI,String nameI) {
	super(groupI,nameI);
    }

    /**
     * Adds a new <code>Lock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lockI the <code>Lock</code> to add.
     * @exception java.lang.IllegalStateException
     *		  if the <code>Lock</code> is already held.
     * @see #removeLock(com.rbnb.api.Lock lockI)
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public final void addLock(Lock lockI) {
	if (getLocks().contains(lockI)) {
	    throw new java.lang.IllegalStateException
		(this + " already has " + lockI + " set.");
	}
	getLocks().addElement(lockI);
    }

    /**
     * Clears all of the <code>Locks</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public final void clearLocks() {
	Lock lock;

	while (!getLocks().isEmpty()) {
	    try {
		lock = (Lock) getLocks().elementAt(0);
	    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
		break;
	    }
	    lock.getDoor().clear(lock);
	    if (!getLocks().isEmpty() &&
		(getLocks().elementAt(0) == lock)) {
		getLocks().removeElementAt(0);
	    }
	}
    }

    /**
     * Ensures that all <code>Locks</code> are cleared at this time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param identificationI the identification of the caller.
     * @param locationI	      the location of the caller.
     * @param logI	      the <code>Log</code> for reporting problems.
     * @since V2.2
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/17/2003  INB	Created.
     *
     */
    public final void ensureLocksCleared(String identificationI,
					 String locationI,
					 Log logI,
					 byte levelI,
					 long classI)
    {
	if (getLocks().size() > 0) {
	    try {
		throw new java.lang.IllegalStateException
		    ("Locks still set in " +
		     locationI + ".\nThread: " + this +
		     " Locks: " + getLocks());
	    } catch (java.lang.IllegalStateException e) {
		try {
		    if (logI == null) {
			e.printStackTrace();
		    } else {
			logI.addException
			    (Log.STANDARD,
			     classI,
			     identificationI,
			     e);
		    }
		} catch (java.lang.Exception e1) {
		}
	    }

	    clearLocks();

	    if (logI == null) {
		System.err.println(identificationI +
				   " at " + locationI +
				   " locks cleared.");
	    } else {
		try {
		    logI.addMessage
			(levelI,
			 classI,
			 identificationI,
			 "At " + locationI + " locks cleared.");
		} catch (java.lang.Exception e1) {
		}
	    }
	}
    }

    /**
     * Gets the list of <code>Locks</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list.
     * @see #setLocks(java.util.Vector locksI)
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public final java.util.Vector getLocks() {
	return (locks);
    }

    /**
     * Removes the specified <code>Lock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lockI the <code>Lock</code>.
     * @exception java.lang.IllegalStateException
     *		  if the <code>Lock</code> is not held.
     * @see #addLock(com.rbnb.api.Lock lockI)
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public final void removeLock(Lock lockI) {
	if (!getLocks().contains(lockI)) {
	    throw new java.lang.IllegalStateException
		(this + " does not hold " + lockI + ".\nHas: " +
		 getLocks());
	}
	getLocks().removeElement(lockI);
    }

    /**
     * Sets the list of <code>Locks</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locksI the list.
     * @see #getLocks()
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    public final void setLocks(java.util.Vector locksI) {
	locks = locksI;
    }
}
