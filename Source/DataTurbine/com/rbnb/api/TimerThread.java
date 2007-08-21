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
 * Extended <code>Thread</code> for handling <code>Timers</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/17/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
 * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
 * 09/04/2002  INB	Created.
 *
 */
final class TimerThread
    extends com.rbnb.api.ThreadWithLocks
{
    /**
     * has the thread been cancelled?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    boolean cancelled = false;

    /**
     * the <code>PriorityQueue</code> to work on.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    PriorityQueue queue = null;

    /**
     * Creates a <code>TimerThread</code> to operate on the specified queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @param queueI the <code>PriorityQueue</code>.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    TimerThread(PriorityQueue queueI) {
	super();
	queue = queueI;
    }

    /**
     * Cancels this <code>TimerThread</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    final void cancel() {
	if (queue != null) {
	    cancelled = true;
	    synchronized (queue) {
		queue.notify();
	    }
	}
    }

    /**
     * Runs this <code>TimerThread</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 03/26/2003  INB	Use <code>TimerPeriod.LONG_WAIT</code>.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 09/04/2002  INB	Created.
     *
     */
    public final void run() {
	try {
	    while (true) {
		try {
		    TimerTask task = null;
		    boolean go = false;

		    synchronized (queue) {
			while (!cancelled && queue.isEmpty()) {
			    // Wait for something to be added to the queue.
			    queue.wait(TimerPeriod.LONG_WAIT);
			}

			if (cancelled) {
			    break;
			} else if ((task = queue.current()) == null) {
			    continue;
			}
			if (!(go = task.readyToRun())) {
			    queue.wait(task.delay());
			} else {
			    queue.remove();
			}
		    }

		    if (go && (task != null)) {
			task.run();
		    }

		} catch (java.lang.InterruptedException e) {
		}
		    
		ensureLocksCleared
		    (toString(),
		     "TimerThread.run(1)",
		     null,
		     (byte) 0,
		     0L);
	    }

	} finally {
	    if (queue != null) {
		queue.clear();
		queue = null;
	    }

	    ensureLocksCleared
		(toString(),
		 "TimerThread.run(2)",
		 null,
		 (byte) 0,
		 0L);
	}
    }
}

