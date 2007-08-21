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
 * Implementation of the <code>java.util.Timer</code> class.
 * <p>
 * This class exists because the referenced class does not exist in early
 * versions of the JDK. This became an issue when we tried to run the
 * <bold>RBNB</bold> V2 server on a machine running Personal Java.
 * <p>
 *
 * @author Ian Brown
 *
 * @see java.util.Timer
 * @since V2.0
 * @version 10/15/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/15/2004  JPW	Couple minor changes that fix typing errors INB
 *			had aparently made.
 * 10/01/2004  JPW	Use new IndirectTimer class; isolate references to
 *			java.util.Timer to that class.
 * 09/03/2002  INB	Created.
 *
 */
final class Timer {
    /**
     * is <code>java.util.Timer</code> supported?
     * <p>
     * A value of 0 is unknown, -1 is not supported, and 1 is supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/03/2002
     */
    private static byte Supported = 0;

    /**
     * priority queue for <code>TimerTasks</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private PriorityQueue queue = null;

    /**
     * the timer thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/03/2002
     */
    private TimerThread thread = null;

    /**
     * <code>Timer</code> object for when <code>java.util.Timer</code> exists.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/01/2004
     */
    // JPW 10/01/2004: Move references to java.util.Timer to the new
    //                 IndirectTimer class
    // private java.util.Timer timer = null;
    private IndirectTimer timer = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #cancel
     * @since V2.0
     * @version 09/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/03/2002  INB	Created.
     *
     */
    public Timer() {
	super();

	if (Supported == 1) {
	    // JPW 10/01/2004: Move java.util.Timer references to the new
	    //                 IndirectTimer class
	    // timer = new java.util.Timer();
	    timer = new IndirectTimer();
	} else if (Supported == -1) {
	    queue = new PriorityQueue();
	    thread = new TimerThread(queue);
	    thread.start();
	} else {
	    try {
		// JPW 10/01/2004: Move java.util.Timer references to the
		//                 new IndirectTimer class
		// timer = new java.util.Timer();
		timer = new IndirectTimer();
		Supported = 1;
	    } catch (java.lang.NoClassDefFoundError e) {
		Supported = -1;
		timer = null;
		queue = new PriorityQueue();
		thread = new TimerThread(queue);
		thread.start();
	    }
	}
    }

    /**
     * Creates a new <code>Timer</code> whose associated thread may be
     * specified to run as a daemon.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isDaemonI true if associated thread should run as a daemon.
     * @see #cancel
     * @since V2.0
     * @version 09/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/03/2002  INB	Created.
     * 10/15/2004  JPW	Add isDaemonI argument to constructor call
     *
     */
    public Timer(boolean isDaemonI) {
	super();

	if (Supported == 1) {
	    // JPW 10/01/2004: Move references to java.util.Timer to the new
	    //                 IndirectTimer class.
	    // timer = new java.util.Timer();
	    // JPW 10/15/2004: Add isDaemonI argument to constructor call
	    timer = new IndirectTimer(isDaemonI);
	} else if (Supported == -1) {
	    queue = new PriorityQueue();
	    thread = new TimerThread(queue);
	    thread.setDaemon(isDaemonI);
	    thread.start();
	} else {
	    try {
		// JPW 10/01/2004: Move references to java.util.Timer to the
		//                 new IndirectTimer class
		// timer = new java.util.Timer(isDaemonI);
		timer = new IndirectTimer(isDaemonI);
		Supported = 1;
	    } catch (java.lang.NoClassDefFoundError e) {
		Supported = -1;
		timer = null;
		queue = new PriorityQueue();
		thread = new TimerThread(queue);
		thread.setDaemon(isDaemonI);
		thread.start();
	    }
	}
    }

    /**
     * Adds a new <code>TimerTask</code> to the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI	 the <code>TimerTask</code>.
     * @param fixedRateI fixed-rate execution?
     * @param timeI      the start time.
     * @param periodI    the period between executions or zero for no repeats.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
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
    private final void addTask(TimerTask taskI,
			       boolean fixedRateI,
			       long timeI,
			       long periodI)
    {
	if (thread.cancelled) {
	    throw new java.lang.IllegalStateException
		("Timer has been cancelled.");
	} else if (thread.queue == null) {
	    throw new java.lang.IllegalStateException("Timer has terminated.");
	}

	taskI.queue(queue,fixedRateI,timeI,periodI);
	queue.add(taskI);
    }

    /**
     * Terminates this timer, discarding any currently scheduled events.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/03/2002  INB	Created.
     *
     */
    public final void cancel() {
	if (timer != null) {
	    timer.cancel();
	} else if (Supported == -1) {
	    thread.cancel();
	    thread = null;
	}
    }

    /**
     * Finalizes this <code>Timer</code>.
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
    public final void finalize() {
	if (timer != null) {
	    timer = null;
	} else if (Supported == -1) {
	    thread.cancel();
	    thread = null;
	}
    }

    /**
     * Schedules the specified <code>TimerTask</code> for execution after the
     * specified delay.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI  the <code>TimerTask</code>.
     * @param delayI the delay before the task is scheduled to run.
     * @exception java.lang.IllegalArgumentException
     *		  if <code>delay</code>I is negative or
     *		  <code>System.currentTimeMillis() + delayI</code> is negative.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/03/2002  INB	Created.
     *
     */
    public final void schedule(TimerTask taskI,long delayI) {
	if (timer != null) {
	    timer.schedule(taskI.getTimerTask(),delayI);
	} else if (delayI < 0) {
	    throw new java.lang.IllegalArgumentException
		("Cannot schedule task with a negative delay.");
	} else {
	    addTask(taskI,false,System.currentTimeMillis() + delayI,0);
	}
    }

    /**
     * Schedules a <code>TimerTask</code> for execution at the specified
     * time. If the time is past, the task is scheduled for immediate
     * execution.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI the <code>TimerTask</code>
     * @param timeI the time to run the task.
     * @exception java.lang.IllegalArgumentException
     *		  if <code>timeI.getTime()</code> is negative.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/03/2002  INB	Created.
     *
     */
    public final void schedule(TimerTask taskI,java.util.Date timeI) {
	if (timer != null) {
	    timer.schedule(taskI.getTimerTask(),timeI);
	} else {
	    addTask(taskI,false,timeI.getTime(),0);
	}
    }

    /**
     * Schedules a <code>TimerTask</code> for repeated <it>fixed-delay
     * execution</it>, beginning after the specified initial delay. Subsequent
     * execution takes place at approximately regular intervals separated by
     * the specified period.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI   the <code>TimerTask</code>.
     * @param delayI  the initial delay.
     * @param periodI the period between executions.
     * @exception java.lang.IllegalArgumentException
     *		  if <code>delay</code>I is negative or
     *		  <code>System.currentTimeMillis() + delayI</code> is negative.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/03/2002  INB	Created.
     *
     */
    public final void schedule(TimerTask taskI,long delayI,long periodI) {
	if (timer != null) {
	    timer.schedule(taskI.getTimerTask(),delayI,periodI);
	} else if (delayI < 0) {
	    throw new java.lang.IllegalArgumentException
		("Cannot schedule task with a negative delay.");
	} else {
	    addTask(taskI,false,System.currentTimeMillis() + delayI,periodI);
	}
    }

    /**
     * Schedules a <code>TimerTask</code> for repeated <it>fixed-delay
     * executions</it>, beginning at the specified time. Subsequent executions
     * take place at approximately regular intervals, separated by the
     * specified period.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI   the <code>TimerTask</code>.
     * @param timeI the time to run the task.
     * @param periodI the period between executions.
     * @exception java.lang.IllegalArgumentException
     *		  if <code>timeI.getTime()</code> is negative.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
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
    public final void schedule(TimerTask taskI,
			       java.util.Date timeI,
			       long periodI)
    {
	if (timer != null) {
	    timer.schedule(taskI.getTimerTask(),timeI,periodI);
	} else {
	    addTask(taskI,false,timeI.getTime(),periodI);
	}
    }

    /**
     * Schedules a <code>TimerTask</code> for repeated <it>fixed-rate
     * execution</it>, beginning after the specified initial delay. Subsequent
     * execution takes place at approximately regular intervals separated by
     * the specified period.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI   the <code>TimerTask</code>.
     * @param delayI  the initial delay.
     * @param periodI the period between executions.
     * @exception java.lang.IllegalArgumentException
     *		  if <code>delay</code>I is negative or
     *		  <code>System.currentTimeMillis() + delayI</code> is negative.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
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
    public final void scheduleAtFixedRate(TimerTask taskI,
					  long delayI,
					  long periodI) {
	if (timer != null) {
	    timer.scheduleAtFixedRate(taskI.getTimerTask(),delayI,periodI);
	} else if (delayI < 0) {
	    throw new java.lang.IllegalArgumentException
		("Cannot schedule task with a negative delay.");
	} else {
	    addTask(taskI,true,System.currentTimeMillis() + delayI,periodI);
	}
    }

    /**
     * Schedules a <code>TimerTask</code> for repeated <it>fixed-rate
     * executions</it>, beginning at the specified time. Subsequent executions
     * take place at approximately regular intervals, separated by the
     * specified period.
     * <p>
     *
     * @author Ian Brown
     *
     * @param taskI   the <code>TimerTask</code>.
     * @param timeI the time to run the task.
     * @param periodI the period between executions.
     * @exception java.lang.IllegalArgumentException
     *		  if <code>timeI.getTime()</code> is negative.
     * @exception java.lang.IllegalStateException
     *		  if the task has already been scheduled or canceled, if the
     *		  timer has been cancelled, or the timer thread has been
     *		  terminated.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     * 10/15/2004  JPW	Change call from schedule() to scheduleAtFixedRate()
     *
     */
    public final void scheduleAtFixedRate(TimerTask taskI,
					  java.util.Date timeI,
					  long periodI)
    {
	if (timer != null) {
	    // JPW 10/15/2004: Change call from schedule()
	    //                 to scheduleAtFixedRate()
	    timer.scheduleAtFixedRate(taskI.getTimerTask(),timeI,periodI);
	} else {
	    addTask(taskI,true,timeI.getTime(),periodI);
	}
    }
}
