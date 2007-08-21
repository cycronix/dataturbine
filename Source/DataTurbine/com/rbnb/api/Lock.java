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
 * Lock class.
 * <p>
 * This class serves the <code>Door</code> class. It provides the actual
 * synchronization on the <code>Door</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Door
 * @see com.rbnb.api.ThreadWithLocks
 * @since V2.0
 * @version 11/17/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/03/2007  WHF	Made 'locations' debug object optional and final.
 *                      Made some methods private that didn't need to be public.
 * 11/17/2003  INB	Added <code>clear</code>.  Don't clear the thread field
 *			when unlocking.  Made the <code>count</code> externally
 *			accessible.
 * 11/14/2003  INB	Moved this class out of <code>Door</code> and made it
 *			accessible within the package.  Added
 *			<code>ThreadWithLocks</code> handling.
 * 11/12/2003  INB	Added location information.  The pending list is now a
 *			<code>Hashtable</code> rather than a
 *			<code>Vector</code>.
 * 10/28/2003  INB	Added <code>toString</code> method.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/23/2003  INB	Handle cleared threads in comparisons.
 * 05/15/2003  INB	Clear the thread when we unlock.
 * 02/21/2001  INB	Created.
 *
 */
final class Lock
    implements com.rbnb.utility.SortCompareInterface
{
    /**
     * the number of times the <code>Lock</code> has been set.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/17/2003
     */
    private long count = 0;

    /**
     * the <code>Door</code> that this <code>Lock</code> is on.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Door door = null;

    /**
     * the locations that have applied this <code>Lock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/12/2003
     */                                // 2007/08/03  WHF
    private final java.util.Vector locations; //new java.util.Vector();

    /**
     * the list of pending locks.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/12/2003
     */
    private java.util.Hashtable pending = new java.util.Hashtable();

    /**
     * the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Thread thread = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    Lock() {
	super();
	setThread(Thread.currentThread());
	// 2007/08/03  WHF
	if (Door.isDebug()) locations = new java.util.Vector();
	else locations = null;
    }

    /**
     * Class constructor to build a <code>Lock</code> for the specified
     * <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doorI  the <code>Door</code>.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    Lock(Door doorI) {
	super();
	setDoor(doorI);
	// 2007/08/03  WHF
	if (Door.isDebug()) locations = new java.util.Vector();
	else locations = null;
    }

    /**
     * Adds a pending lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locationI the location of the caller.
     * @see #removePending()
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location handling.
     * 06/15/2001  INB	Created.
     *
     */
    final synchronized void addPending(String locationI) {
	java.util.Vector lLocations = (java.util.Vector)
	    pending.get(Thread.currentThread());
	if (lLocations == null) {
	    lLocations = new java.util.Vector();
	}
	lLocations.addElement(locationI);
	pending.put(Thread.currentThread(),lLocations);
    }

    /**
     * Checks to see if this <code>Lock</code> is still active.
     * <p>
     * The <code>Lock</code> is active if:
     * <p><ul>
     * <li>The number of times that the <code>Lock</code> has been set is
     *     > 0, and</li>
     * <li>The thread for the <code>Lock</code> is still active.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param locationI	the location of the caller.
     * @param checkPendingI check the pending locks?
     * @param havePendingI  does this thread have a pending lock?
     * @return is the lock active?
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location handling.
     * 02/21/2001  INB	Created.
     *
     */
    final synchronized boolean check(String locationI,
				     boolean checkPendingI,
				     boolean havePendingI)
    {
	return (checkPendingI &&
		 (havePendingI ?
		  !pending.containsKey(Thread.currentThread()) :
		  !pending.isEmpty())) ||
		((count > 0) && (getThread() != null) && getThread().isAlive());
    }

    /**
     * Completely clears this <code>Lock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lockI the <code>Lock</code> to be released.
     * @since V2.2
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Created.
     *
     */
    public final void clear() {
	if (locations != null)
	    locations.removeAllElements();
	count = 0;
	setThread(null);
    }

    /**
     * Compares the sorting value of this <code>Lock</code> to the input
     * sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * The sorting value for an <code>Lock</code> is always the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *       <p><0 if this <code>Lock</code> compares less than the input,
     *       <p> 0 if this <code>Lock</code> compares equal to the input,
     *	     and
     *       <p>>0 if this <code>Lock</code> compares greater than the
     *	    input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/23/2003  INB	Handle cleared threads in comparisons.
     * 02/21/2001  INB	Created.
     *
     */
    public int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	Thread mine = (Thread) sortField(sidI),
	    theirs = (Thread) otherI;
	int comparedR = 
	    ((mine == theirs) ? 0 :
	     (mine == null) ? -1 :
	     (theirs == null) ? 1 :
	     mine.hashCode() - theirs.hashCode());

	 return (comparedR); 
    }

    /**
     * Gets the <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @see #setDoor(com.rbnb.api.Door)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Made package accessible.
     * 02/21/2001  INB	Created.
     *
     */
    final Door getDoor() {
	return (door);
    }

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
     * @see #setThread(Thread)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    private final Thread getThread() {
	return (thread);
    }

    /**
     * Grabs this <code>Lock</code>.
     * <p>
     * A grab is similar to a lock, except that the method waits until the
     * <code>Lock</code> becomes available.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locationI	the location of the caller.
     * @param checkPendingI check for pending locks?
     * @param havePendingI  do we have a pending ID?
     * @see #release()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location handling.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/21/2001  INB	Created.
     *
     */
    final synchronized void grab(String locationI,
				 boolean checkPendingI,
				 boolean havePendingI)
	throws java.lang.InterruptedException
    {
	long lastAt = System.currentTimeMillis();
	long firstAt = lastAt; // MJM
	long nowAt;

	while ((getThread() != Thread.currentThread()) &&
	       check(locationI,checkPendingI,havePendingI)) {
	    wait(TimerPeriod.NORMAL_WAIT);

// MJM 2/20/07:  grope to break deadlock bug
	    if ( (System.currentTimeMillis() - firstAt) >=
		2*TimerPeriod.LOCK_WAIT) {
		  System.err.println("Cannot Grab Lock!!!  Forcibly clearing other locks!");
		  clear();
		  break;
	    }

	    if ((nowAt = System.currentTimeMillis()) - lastAt >=
		TimerPeriod.LOCK_WAIT) {
		try {
		    throw new Exception
			(System.currentTimeMillis() + " " +
			 this +
			 " grab: blocked at " + locationI +
			 " waiting for check pending: " +
			 checkPendingI +
			 ", have pending: " + havePendingI +
			 ", or lock thread to clear.\n" +
			 "My thread: " + Thread.currentThread() + "\n");
		} catch (Exception e) {
		    e.printStackTrace();
		    lastAt = nowAt;
		}
	    }
	}

	// Set the lock.
	lock(locationI,havePendingI);
    }

    /**
     * Sets this <code>Lock</code>.
     * <p>
     * The <code>Lock</code> is set if no other thread has set it. A
     * <code>Lock</code> can be set as many times as the thread wants, but
     * it must be unset (unlocked) the same number of times before it
     * becomes inactive.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locationI    the location of the caller.
     * @param havePendingI does this thread have a pending lock?
     * @exception java.lang.IllegalStateException
     *	      thrown if another thread has set the <code>Lock</code>.
     * @see #unlock()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added <code>ThreadWithLocks</code> handling.
     * 11/12/2003  INB	Added location handling.
     * 02/21/2001  INB	Created.
     *
     */
    final synchronized void lock(String locationI,boolean havePendingI) {
	if ((getThread() != Thread.currentThread()) &&
	    check(locationI,false,havePendingI)) {
	    throw new java.lang.IllegalStateException
		(this + " at " + locationI +
		 ": lock is already set by another thread.");
	}

	setThread(Thread.currentThread());
	if (locations != null)
	    locations.addElement(locationI);
	++count;
	if ((count == 1) && (getThread() instanceof ThreadWithLocks)) {
	    ((ThreadWithLocks) getThread()).addLock(this);
	}
    }

    /**
     * Nullifies this <code>Lock/code>.
     * <p>
     * This method ensures that all pointers in this <code>DataBlock</code>
     * are cleared, reducing the effort needed by the garbage collector to
     * clean it up.
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
     * 11/14/2003  INB	Added <code>ThreadWithLocks</code> handling.
     * 07/30/2003  INB	Created.
     *
     */
    public void nullify() {
	setDoor(null);

	if (locations != null) {
	    locations.removeAllElements();
	    // 2007/08/06  WHF  locations now final.
//	    locations = null;
	}

	if (pending != null) {
	    pending.clear();
	    pending = null;
	}

	if ((getThread() instanceof ThreadWithLocks) &&
	    (((ThreadWithLocks) getThread()).getLocks().contains(this))) {
	    ((ThreadWithLocks) getThread()).removeLock(this);
	}
	setThread(null);
    }

    /**
     * Releases this <code>Lock</code>.
     * <p>
     * This method calls unlock(), then sets the thread to null if the lock
     *  count is zero.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #grab(String,boolean,boolean)
     * @since V2.0
     * @version 08/13/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/13/2007  WHF  Sets thread to null if count equals zero.
     * 11/12/2003  INB	Updated see also list.
     * 02/21/2001  INB	Created.
     *
     */
    final synchronized boolean release() {
	unlock();
	if (count == 0) {
	    thread = null;
	    return true;
	}
	return false;
    }

    /**
     * Removes a pending lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #addPending(String)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Made package accessible.
     * 11/12/2003  INB	Handle <code>Hashtable</code> pending.
     * 03/18/2002  INB	Created.
     *
     */
    final synchronized void removePending() {
	pending.remove(Thread.currentThread());
	notifyAll();
    }

    /**
     * Sets the <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doorI  the <code>Door</code>.
     * @see #getDoor()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Made package accessible.
     * 02/21/2001  INB	Created.
     *
     */
    final void setDoor(Door doorI) {
	door = doorI;
    }

    /**
     * Sets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI  the thread.
     * @see #getThread()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Made package accessible.
     * 02/21/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI) {
	thread = threadI;
    }
    
    /**
      * Performs a thread comparison, the only use of getThread outside this
      *  class.
      * @author WHF
      * @since V3.0
      * @version 2007/08/06
      */
    final boolean threadEquals(Thread t) {
	return thread == t;
    }

    /**
     * Gets the sorting value for this <code>Lock</code>.
     * <p>
     * The sort identifier for <code>Locks</code> is the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI  the sort type identifier -- must be null.
     * @return the sort value.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @see #compareTo(Object,Object)
     * @since V2.0
     * @version 05/10/2001
     */
	
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/02/2001  INB	Created.
     *
     */
    public Object sortField(Object sidI)
	throws com.rbnb.utility.SortException
    {
	if (sidI != null) {
	    // Only the null sort identifier is supported.
	    throw new com.rbnb.utility.SortException
		("The sort identifier for Locks must be null.");
	}

	return (getThread());
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/28/2003  INB	Created.
     *
     */
    public final String toString() {
	return ("Lock: thread: " + thread +
		" count: " + count +
		" Door: " + getDoor().getIdentification() +
		" Location list: " + locations +
		" Pending list: " + pending);
    }

    /**
     * Clears this <code>Lock</code>.
     * <p>
     * If the <code>Lock</code> belongs to the current thread, the count of
     * the number of times it has been set is decremented. If the new count
     * is equal to zero, the <code>Lock</code> becomes available to other
     * threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #lock(String,boolean)
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Don't clear the thread field!
     * 11/14/2003  INB	Added <code>ThreadWithLocks</code> handling.
     * 11/12/2003  INB	Added location handling.
     * 05/13/2003  INB	Clear the thread.
     * 02/21/2001  INB	Created.
     *
     */
    final synchronized boolean unlock() {
	if (Thread.currentThread() == getThread()) {
	    // If the current thread owns the lock, decrement the count.
	    if (locations != null && locations.size() > 0) {
		locations.removeElementAt(locations.size() - 1);
	    }

	    if (--count <= 0) {
		// If the count has been cleared, notify anyone waiting for
		// the <code>Lock</code>.
		if (count == 0) {
		    if (getThread() instanceof ThreadWithLocks) {
			((ThreadWithLocks) getThread()).removeLock(this);
		    }
		}
		notifyAll();
	    }
	}

	return (count <= 0);
    }
}
