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
 * Event queue that allows one or more threads to handle simple action events.
 * <p>
 * This class provides a FIFO and a list of handler threads. The handler
 * threads block waiting for entries to appear in the FIFO. When something is
 * added to it, the first available thread is woken up and it handles the
 * event.
 * <p>
 * Events are objects that implement <code>Action</code>, which has a single
 * method, <code>action</code>. No event is allowed to appear on the queue more
 * than once.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Action
 * @since V2.1
 * @version 04/13/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/13/2004  INB	Create new threads if there are fewer available than
 *			there are events rather than if there are none
 *			available.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 03/13/2003  INB	Created.
 *
 */
final class ActionThreadQueue {

    /**
     * the number of available threads.
     * <p>
     * This counter specifies how many threads are currently available for
     * use.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int availableThreads = 0;

   /**
     * the events that have been queued.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/14/2003
     */
    private java.util.Vector events = new java.util.Vector(10000);

    /**
     * the total number of events ever queued.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private long eventsQueued = 0;

    /**
     * the current number of threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int numberOfThreads = 0;

    /**
     * hash table to allow us to check on queued events.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/14/2003
     */
    private java.util.Hashtable queued = new java.util.Hashtable(10000,.5F);

    /**
     * is the queue being stopped?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private boolean isStopping = false;

    /**
     * the threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private Thread[] threads = null;

    /**
     * count of the action threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/28/2003
     */
    private long threadCount = 0;

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
    ActionThreadQueue() {
	this(100);
    }

    /**
     * Class constructor to build an <code>ActiongThreadQueue</code> with the
     * specified maximum number of threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maximumThreadsI the maximum number of threads allowed.
     * @since V2.1
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public ActionThreadQueue(int maximumThreadsI) {
	super();
	threads = new Thread[maximumThreadsI];
    }

    /**
     * Adds an event to the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @see #removeEvent(com.rbnb.api.Action)
     * @since V2.1
     * @version 04/13/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/13/2004  INB	Create new threads if there are fewer available than
     *			there are events rather than if there are none
     *			available.
     * 03/13/2003  INB	Created.
     *
     */
    public final void addEvent(Action eventI) {
	synchronized (events) {
	    if (isStopping) {
		return;
	    } else if (queued.get(eventI) != null) {
		return;
	    }

	    // Add the event to the list of events and mark it as queued.
	    events.addElement(eventI);
	    queued.put(eventI,new Long(eventsQueued));
	    ++eventsQueued;

	    if ((availableThreads < events.size()) &&
		(numberOfThreads < threads.length)) {
		// If there are no available threads and there is room for a
		// new thread, create one.
		threads[numberOfThreads] = new ActionThread(this);
		threads[numberOfThreads++].start();
	    }

	    events.notify();
	}
    }

    /**
     * Removes and returns the next event from the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the event.
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
    private final Action nextEvent() {
	Action eventR = null;

	synchronized (events) {
	    if (events.size() > 0) {
		eventR = (Action) events.firstElement();
		events.removeElementAt(0);
		queued.remove(eventR);
	    }
	}

	return (eventR);
    }

    /**
     * Removes a previously queued event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
     * @see #addEvent(com.rbnb.api.Action)
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
    public final void removeEvent(Action eventI) {
	synchronized (events) {
	    Long eventIdL = (Long) queued.remove(eventI);

	    if (eventIdL != null) {
		// If the event appears to be in the queue, then locate it and
		// remove it.
		long eventId = eventIdL.longValue();
		int idx = (int) (eventId - (eventsQueued - events.size()));

		if ((idx >= 0) && (idx < events.size())) {
		    // If the event is indeed in the queue, then remove it.
		    events.removeElementAt(idx);

		    if (idx != 0) {
			// If this wasn't the first event in the queue, then
			// pretend it never was in the queue.
			--eventsQueued;
			for (; idx < events.size(); ++idx) {
			    queued.put(events.elementAt(idx),
				       new Long(eventId + idx));
			}
		    }
		}
	    }
	}
    }

    /**
     * Stops this queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public final void stop() {
	synchronized (events) {
	    isStopping = true;
	    events.notifyAll();

	    for (int idx = 0; idx < numberOfThreads; ++idx) {
		threads[idx].interrupt();
	    }

	    long startAt = System.currentTimeMillis(),
		endAt;
	    while ((events.size() > 0) &&
		   ((endAt = System.currentTimeMillis()) <
		    startAt + TimerPeriod.SHUTDOWN)) {
		try {
		    events.wait(TimerPeriod.NORMAL_WAIT);
		    events.notifyAll();
		} catch (java.lang.InterruptedException e) {
		    break;
		}
	    }
	    events.removeAllElements();
	    queued.clear();

	    events.notifyAll();
	}
    }

    /**
     * Waits for an event to be queued and returns the first one.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the event.
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    final Action waitEvent() {
	Action eventR = null;

	synchronized (events) {
	    ++availableThreads;

	    while (eventR == null) {
		while (events.size() == 0) {
		    if (isStopping) {
			events.notifyAll();
			return (null);
		    }
		    try {
			events.wait(TimerPeriod.NORMAL_WAIT);
		    } catch (java.lang.InterruptedException e) {
			break;
		    }
		}

		eventR = nextEvent();
	    }

	    --availableThreads;
	}

	return (eventR);
    }

    /**
     * Action thread class.
     * <p>
     * Threads of this class work on events in the
     * <code>ActionThreadQueue</code>.
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Action
     * @since V2.1
     * @version 11/14/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 10/28/2003  INB	Name the threads.
     * 03/13/2003  INB	Created.
     *
     */
    private final class ActionThread
	extends com.rbnb.api.ThreadWithLocks
    {
	/**
	 * the <code>ActionThreadQueue</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 03/13/2003
	 */
	private ActionThreadQueue queue = null;

	/**
	 * Class constructor to build an <code>ActionThread</code> for an
	 * <code>ActionThreadQueue</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param queueI the queue.
	 * @since V2.1
	 * @version 10/28/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 10/28/2003  INB	Name the threads.
	 * 03/13/2003  INB	Created.
	 *
	 */
	ActionThread(ActionThreadQueue queueI) {
	    super("Action Thread #" + (++threadCount));
	    queue = queueI;
	}

	/**
	 * Runs this thread.
	 * <p>
	 * The thread runs in a loop waiting to either be interrupted or to
	 * receive events. If it gets an event, it handles it and returns to
	 * waiting. If it doesn't get an event, it terminates.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 11/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/14/2003  INB	Ensure that we don't have a <code>Lock</code>
	 *			when we're supposed to be doing nothing and
	 *			clear the locks at the end.
	 * 03/13/2003  INB	Created.
	 *
	 */
	public final void run() {
	    while (true) {
		try {
		    Action event = queue.waitEvent();
		    if (event == null) {
			break;
		    }

		    event.performAction();

		    ensureLocksCleared(toString(),
				       "ActionThreadQueue.run(1)",
				       null,
				       (byte) 0,
				       0L);
		} catch (java.lang.Exception e) {
		}
	    }

	    // Ensure that we are not holding any <code>Locks</code>.
	    ensureLocksCleared(toString(),
			       "ActionThreadQueue.run(2)",
			       null,
			       (byte) 0,
			       0L);
	}
    }
}
