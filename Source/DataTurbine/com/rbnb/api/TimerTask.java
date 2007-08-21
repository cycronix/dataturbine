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
 * Implementation of the <code>java.util.TimerTask</code> class specifically
 * for the <code>RBNB</code>.
 * <p>
 * This class exists because the referenced class does not exist in early
 * versions of the JDK. This became an issue when we tried to run the
 * <bold>RBNB</bold> V2 server on a machine running Personal Java.
 * <p>
 * <bold>RBNB</bold> tasks are specified using a class that implements the
 * <code>TimerTaskInterface</code> and a string that specifies the specifics of
 * the task to be performed.
 * <p>
 * When the <code>run()</code> method of this class is executed, it starts a
 * separate thread that calls the <code>timerTask(TimerTask taskI</code> method
 * of the <code>TimerTaskInterface</code> object. By executing the actual task
 * using a background thread, this class ensures minimal delay to the
 * <code>Timer</code> thread and does not effect the shutdown of the server.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.TimerTaskInterface
 * @since V2.0
 * @version 10/01/2004
 */

/*
 * Copyright 2001, 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/01/2004  JPW	Break IndirectTimerTask out into its own class;
 *			isolate java.util.TimerTask references to that class
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 02/26/2003  INB	Handle null pointer values.a
 * 12/05/2001  INB	Created.
 *
 */
final class TimerTask {
    /**
     * the code to execute.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/05/2001
     */
    private String code = null;

    /**
     * priority information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private PriorityInformation priorityInfo = null;

    /**
     * is <code>java.util.TimerTask</code> supported?
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
     * <code>java.util.TimerTask</code> subclass to use when using the "real"
     * <code>java.util.Timer</code> class.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private IndirectTimerTask timerTask = null;

    /**
     * the <code>TimerTaskInterface</code> class to run.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/05/2001
     */
    private TimerTaskInterface tti = null;

    /**
     * Class constructor.
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
     * 12/05/2001  INB	Created.
     *
     */
    TimerTask() {
	super();

	if (Supported == 1) {
	    timerTask = new IndirectTimerTask(this);
	} else if (Supported == -1) {
	    priorityInfo = new PriorityInformation();
	} else if (Supported == 0) {
	    try {
		timerTask = new IndirectTimerTask(this);
		Supported = 1;
	    } catch (java.lang.NoClassDefFoundError e) {
		Supported = -1;
		timerTask = null;
		priorityInfo = new PriorityInformation();
	    }
	}
    }

    /**
     * Class constructor to build a <code>TimerTask</code> for a specific
     * <code>TimerTaskInterface</code> and a code string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ttiI  the <code>TimerTaskInterface</code> object.
     * @param codeI the task-specific string code.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    TimerTask(TimerTaskInterface ttiI,String codeI) {
	this();
	setTTI(ttiI);
	setCode(codeI);
    }

    /**
     * Cancels this <code>TimerTask</code>.
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
    public final void cancel() {
	if (timerTask != null) {
	    timerTask.cancel();
	    timerTask = null;
	} else if (Supported == -1) {
	    priorityInfo.cancelled = true;
	}
	tti = null;
    }

    /**
     * Compare this <code>TimerTask</code> to the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timerTaskI the <code>TimerTask</code> to compare to, passed as an
     *			 <code>Object</code>.
     * @return a negative integer, zero, or a positive integer depending on
     *	       whether this object is less than, equal to, or greater than the
     *	       input object.
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
    public final int compareTo(Object otherI) {
	int resultR = -1;

	if (Supported == -1) {
	    TimerTask other = (TimerTask) otherI;
	    resultR = priorityInfo.compareTo(other.priorityInfo);
	}

	return (resultR);
    }

    /**
     * How long until this task is ready to run?
     * <p>
     *
     * @author Ian Brown
     *
     * @return time until ready.
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
    final long delay() {
	long delayR = 0;

	if (Supported == -1) {
	    delayR = priorityInfo.delay();
	}

	return (delayR);
    }

    /**
     * Gets the string code.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the task-specific string code.
     * @see #setCode(String)
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    final String getCode() {
	return (code);
    }

    /**
     * Gets the standard <code>java.util.TimerTask</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>IndirectTimerTask</code> object.;
     * @since V2.0
     * @version 10/01/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2004  JPW	Change the return type from java.util.TimerTask to
     *                  IndirectTimerTask.
     * 09/04/2002  INB	Created.
     *
     */
    final IndirectTimerTask getTimerTask() {
	return (timerTask);
    }

    /**
     * Gets the <code>TimerTaskInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>TimerTaskInterface</code> object.
     * @see #setTTI(com.rbnb.api.TimerTaskInterface)
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    final TimerTaskInterface getTTI() {
	return (tti);
    }

    /**
     * Queues this <code>TimerTask</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param queueI     the <code>PriorityQueue</code>.
     * @param fixedRateI fixed rate execution?
     * @param timeI      the time of the first queue.
     * @param periodI    the period - zero means no repeat.
     * @exception java.lang.IllegalStateException
     *		  if this <code>TimerTask</code> is already scheduled or has
     *		  been cancelled.
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
    final void queue(PriorityQueue queueI,
		     boolean fixedRateI,
		     long timeI,
		     long periodI)
    {
	if (Supported == -1) {
	    priorityInfo.queue(this,queueI,fixedRateI,timeI,periodI);
	}
    }

    /**
     * Is this task ready to run?
     * <p>
     *
     * @author Ian Brown
     *
     * @return task is ready?
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
    final boolean readyToRun() {
	boolean readyR = false;

	if (Supported == -1) {
	    readyR = priorityInfo.readyToRun();
	}

	return (readyR);
    }

    /**
     * Runs this <code>TimerTask</code>.
     * <p>
     * This method starts a background thread to run the actual
     * <code>TimerTaskInterface</code> object's <code>timerTask(TimerTask
     * ttiI)</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    public final void run() {
	if ((Supported == -1) && priorityInfo.fixedRate) {
	    priorityInfo.requeue();
	}
	if ((Supported == 1) || !priorityInfo.cancelled) {
	    TimerDaemon bThread = new TimerDaemon(this);
	    bThread.setDaemon(true);
	    bThread.start();
	}
	if ((Supported == -1) && !priorityInfo.fixedRate) {
	    priorityInfo.requeue();
	}
    }

    /**
     * Sets the code string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param codeI the task-specific code string.
     * @see #getCode()
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    private final void setCode(String codeI) {
	code = codeI;
    }

    /**
     * Sets the <code>TimerTaskInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ttiI the <code>TimerTaskInterface</code> object.
     * @see #getTTI()
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    private final void setTTI(TimerTaskInterface ttiI) {
	tti = ttiI;
    }

    /**
     * Returns a string representation of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2002  INB	Created.
     *
     */
    public final String toString() {
	return ("TimerTask: " + code + " for " + tti);
    }

    /**
     * Internal priority information class.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    private final class PriorityInformation {

	/**
	 * task has been cancelled?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/04/2002
	 */
	private boolean cancelled = false;

	/**
	 * scheduled for fixed rate execution?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/04/2002
	 */
	boolean fixedRate = false;

	/**
	 * our parent <code>TimerTask</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/04/2002
	 */
	private TimerTask parent = null;

	/**
	 * the period between executions.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/04/2002
	 */
	private long period = 0;

	/**
	 * the <code>PriorityQueue</code> to which this belongs.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/04/2002
	 */
	private PriorityQueue queue = null;

	/**
	 * the next execution time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/04/2002
	 */
	private long time = 0;

	/**
	 * Class constructor.
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
	PriorityInformation() {
	    super();
	}

	/**
	 * Compare this <code>TimerTask</code> to the input one.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param timerTaskI the <code>TimerTask</code> to compare to, passed
	 *		     as an <code>Object</code>.
	 * @return a negative integer, zero, or a positive integer depending on
	 *	   whether this object is less than, equal to, or greater than
	 *	   the input object.
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
	public final int compareTo(Object otherI) {
	    PriorityInformation other = (PriorityInformation) otherI;
	    int resultR = (int) (time - other.time);

	    if (resultR == 0) {
		if (fixedRate && !other.fixedRate) {
		    resultR = -1;
		} else if (!fixedRate && other.fixedRate) {
		    resultR = 1;
		}
	    }

	    return (resultR);
	}

	/**
	 * How long until this task is ready to run?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return time until ready.
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
	final long delay() {
	    return (time - System.currentTimeMillis());
	}

	/**
	 * Queues this <code>TimerTask</code> for execution.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param parentI    our parent <code>TimerTask</code>.
	 * @param queueI     the <code>PriorityQueue</code>.
	 * @param fixedRateI fixed rate execution?
	 * @param timeI      the time of the first queue.
	 * @param periodI    the period - zero means no repeat.
	 * @exception java.lang.IllegalStateException
	 *	      if this <code>TimerTask</code> is already scheduled or
	 *	      has been cancelled. 
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
	final void queue(TimerTask parentI,
			 PriorityQueue queueI,
			 boolean fixedRateI,
			 long timeI,
			 long periodI)
	{
	    if (queue != null) {
		throw new java.lang.IllegalStateException
		    ("Timer task has already been scheduled or is cancelled.");
	    }

	    parent = parentI;
	    queue = queueI;
	    fixedRate = fixedRateI;
	    time = timeI;
	    period = periodI;
	}

	/**
	 * Is this task ready to run?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return task is ready?
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
	final boolean readyToRun() {
	    return (time <= System.currentTimeMillis());
	}

	/**
	 * Requeues this task for execution.
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
	final void requeue() {
	    if (queue != null) {
		if (!cancelled && (period > 0)) {
		    if (fixedRate) {
			time += period;
		    } else {
			time = System.currentTimeMillis() + period;
		    }

		    queue.add(parent);
		} else {
		    queue = null;
		}
	    }
	}
    }

    /**
     * Internal background (daemon) thread for running tasks on a timer.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     * Copyright 2001, 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 12/05/2001  INB	Created.
     *
     */
    private final class TimerDaemon
	extends com.rbnb.api.ThreadWithLocks
    {
	/**
	 * the <code>TimerTask</code> that started this thread.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 12/05/2001
	 */
	private TimerTask tt = null;

	/**
	 * Class constructor to build a <code>DaemonTask</code> for a specific
	 * <code>TimerTask</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param ttI the <code>TimerTask</code>.
	 * @since V2.0
	 * @version 12/05/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/05/2001  INB	Created.
	 *
	 */
	TimerDaemon(TimerTask ttI) {
	    super();
	    setTT(ttI);
	}

	/**
	 * Gets the <code>TimerTask</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the <code>TimerTask</code> object.
	 * @see #setTT(com.rbnb.api.TimerTask)
	 * @since V2.0
	 * @version 12/05/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/05/2001  INB	Created.
	 *
	 */
	private final TimerTask getTT() {
	    return (tt);
	}

	/**
	 * Runs this <code>TimerDaemon</code>.
	 * <p>
	 * This method calls the <code>timerTask(TimerTask ttI)</code> method
	 * of the <code>TimerTaskInterface</code> object in the
	 * <code>TimerTask</code> executing this <code>TimerDaemon</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/14/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
	 *			<code>Thread</code> and ensure that
	 *			<code>Locks</code> are released.
	 * 02/26/2003  INB	Handle null pointer values.a
	 * 12/05/2001  INB	Created.
	 *
	 */
	public final void run() {
	    if ((getTT() != null) && (getTT().getTTI() != null)) {
		getTT().getTTI().timerTask(getTT());
	    }
	    clearLocks();
	}

	/**
	 * Sets the <code>TimerTask</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param ttI the <code>TimerTask</code> object.
	 * @see #getTT()
	 * @since V2.0
	 * @version 12/05/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/05/2001  INB	Created.
	 *
	 */
	private final void setTT(TimerTask ttI) {
	    tt = ttI;
	}
    }
}
