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
 * An object that allows an <code>NBO</code> to await notification of
 * "interesting" events, such as the addition of a new <code>RBO</code> to a
 * <code>RBNB</code> or the addition of a new frame <code>Rmap</code> to a
 * <code>RBO</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/26/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/26/2004  INB	Added <code>terminateRequested</code> flag and
 *			handling.
 * 02/24/2004  INB	Added optional debug in <code>run</code>.
 * 02/18/2004  INB	Made the thread a daemon thread so the JVM can exit
 *			even if it is still running.
 * 01/15/2004  INB	Post an <code>EndOfStream</code> message to our
 *			output client if the latter is a
 *			<code>StreamListener</code> on an exception.
 * 11/18/2003  INB	Added debug to waits.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/26/2001  INB	Created.
 *
 */
final class AwaitNotification
    extends com.rbnb.api.ThreadWithLocks
    implements com.rbnb.utility.SortCompareInterface,
	       com.rbnb.api.Interruptable
{
    /**
     * list of the <code>Serializables</code> that caused the notification to
     * occur.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private java.util.Vector events = new java.util.Vector();

    /**
     * the number of events added.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private long eventsAdded = 0;

    /**
     * the number of events handled.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private long eventsHandled = 0;

    /**
     * the object that we're watching.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/26/2001
     */
    private NotificationFrom from = null;

    /**
     * the <code>Rmap</code> representing the portion of the request that needs
     * to be matched at the current level.
     * <p>
     * If this is set, then the <code>Serializable</code> that caused the
     * notification must match a descendent of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/26/2001
     */
    private Rmap request = null;

    /**
     * has a terminate been requested?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3.1
     * @version 05/26/2004
     */
    private boolean terminateRequested = false;

    /**
     * the target of the notification event.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/26/2001
     */
    private NotificationTo to = null;
	

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2004  INB	Made the thread a daemon thread so the JVM can exit
     *			even if it is still running.
     * 03/26/2001  INB	Created.
     *
     */
    private AwaitNotification() {
	super();
	setDaemon(true);
    }

    /**
     * Class constructor to build an <code>AwaitNotification</code> object from
     * the specified request <code>Rmap</code>, source, and target of the
     * notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @param fromI     the source of the notification.
     * @param toI	the target of the notification.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @since V2.0
     * @version 12/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    AwaitNotification(Rmap requestI,
		      NotificationFrom fromI,
		      NotificationTo toI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();


	Rmap from = (Rmap) fromI;
	String thrName = (System.currentTimeMillis() + "." +
			  from.getFullName());
	if (from.getName() == null) {
	    thrName += "_" + fromI.getClass().toString();
	}
	if (toI instanceof StreamListener) {
	    thrName += "->." +
		((Rmap) ((StreamListener) toI).getNBO()).getFullName();
	} else {
	    thrName += "->reg" + Thread.currentThread().getName();
	}
	setName(thrName.replace('/','_'));
	setRequest(requestI);
	setFrom(fromI);
	setTo(toI);
	
    }

	/**
	 * Returns the number of events in the notification list.
	 * <p>
	 *
	 * @author Eric Friets
     *
     * @return the number of events
     * @since V2.0
     * @version 09/15/2005
     */
	 /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/15/2005  EMF	Created.
     *
     */
	 public int numEvents() {
		 return events.size();
	 }

    /**
     * Adds an "event" (<code>Serializable</code>) to the notification list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> event that caused
     *			    the notification.
     * @param waitI  wait for the event to be handled?
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getNextEvent()
     * @since V2.0
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Handle the <code>terminateRequested</code> flag.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/26/2001  INB	Created.
     *
     */
    final synchronized void addEvent(Serializable serializableI,boolean waitI)
	throws java.lang.InterruptedException
    {
	events.addElement(serializableI);
	long eventNumber = (++eventsAdded);
	notifyAll();
	
	if (waitI) {
	    long startAt = System.currentTimeMillis();
	    long nowAt;
	    while (isAlive() &&
		   !terminateRequested &&
		   (eventsHandled < eventNumber)) {
		wait(TimerPeriod.NORMAL_WAIT);
		nowAt = System.currentTimeMillis();
//		if (nowAt - startAt >= TimerPeriod.LOCK_WAIT) {
		if (nowAt - startAt >= (5*TimerPeriod.LOCK_WAIT)) {	// be extra patient MJM 6/13
		    try {
			throw new java.lang.Exception
			    (nowAt + " " + this +
			     " AwaitNotification.addEvent blocked waiting " +
			     "for event " + eventNumber +
			     ".  Currently at " + eventsHandled + ".");
		    } catch (java.lang.Exception e) {
			e.printStackTrace();
			// MJM 5/31/2013:  attempt to break out of infinite deadlock:
			System.err.println("AwaitNotification Deadlock: Forcibly clearing locks and events...");
			clearEvents();  	// MJM 6/13, try for a clean slate to recover
			ensureLocksCleared(toString(),"addEvent()",null,(byte) 0,0L);  // MJM
			interrupt();		// MJM bail
		    }
		    startAt = nowAt;
		}
	    }
	}
    }

    /**
     * Compares the sorting value of this <code>AwaitNotification</code> to the
     * input sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * The sorting value for an <code>AwaitNotification</code> is always
     * the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>AwaitNotification</code> compares less than
     *		  the input,
     *	       <p> 0 if this <code>AwaitNotification</code> compares equal to
     *		  the input, and
     *	       <p>>0 if this <code>AwaitNotification</code> compares greater
     *		  than the input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @exception java.lang.IllegalStateException
     *		  thrown if both the this <code>AwaitNotification</code> and
     *		  the input <code>AwaitNotification</code> are nameless and
     *		  timeless.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    public final int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	String mine = (String) sortField(sidI),
	       other = (String) otherI;

	return (mine.compareTo(other));
    }

    /**
     * Gets the source of the notification event.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the source of the notification event.
     * @see #setFrom(com.rbnb.api.NotificationFrom)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final NotificationFrom getFrom() {
	return (from);
    }

	 /**
     * Clears the event stack.
     * <p>
     *
     * @author Eric Friets
     *
     * @see #addEvent(com.rbnb.api.Serializable,boolean)
     * @since V2.6
     * @version 11/16/2006
     */

	 /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/16/2006  JPW	Change call from clear() to removeAllElements();
     *			this keeps the code more Java 1.1.4 compliant
     *			(for J# compile).
     * 10/19/2005  EMF	Created.
     *
     */
	 public synchronized void clearEvents() {
	     // JPW 11/16/2006: Change call from clear() to removeAllElements()
	     //                 (these methods are functionally equivalent).
	     events.removeAllElements();
	 }

    /**
     * Gets the next notification "event" <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the event <code>Serializable</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addEvent(com.rbnb.api.Serializable,boolean)
     * @since V2.0
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Handle the <code>terminateRequested</code> flag.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/26/2001  INB	Created.
     *
     */
    private final synchronized Serializable getNextEvent()
	throws java.lang.InterruptedException
    {
	Serializable eventR = null;

	long startAt;
	long nowAt;
	while (!terminateRequested && (events.size() == 0)) {
	    wait(TimerPeriod.NORMAL_WAIT);
	}

	if (events.size() > 0) {
	    eventR = (Serializable) events.firstElement();
	    events.removeElementAt(0);
	}

	return (eventR);
    }

    /**
     * Gets the request <code>Rmap</code> to be matched.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the request <code>Rmap</code>.
     * @see #setRequest(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final Rmap getRequest() {
	return (request);
    }

    /**
     * Gets the target of the notification event.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the target of the notification event.
     * @see #setTo(com.rbnb.api.NotificationTo)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    final NotificationTo getTo() {
	return (to);
    }

    /**
     * Interrupts the thread.
     * <p>
     * This method also sets the <code>terminateRequested</code> flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.3.1
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Created.
     *
     */
    public final void interrupt() {
	terminateRequested = true;
	synchronized (this) {
	    notifyAll();
	}
	super.interrupt();
	}

    /**
     * Matches the input <code>Rmap</code> against the request
     * <code>Rmap</code> to determine if it is really of interest.
     * <p>
     * This match is a name match only. The name of the input <code>Rmap</code>
     * must match the name of a descendent of the request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI  the event <code>Serializable</code>.
     * @return the matching request <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final Rmap matches(Serializable eventI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap resultR = null;

	if (eventI instanceof Rmap) {
	    Serializable event = eventI;
	    if (eventI instanceof NewObjectEvent) {
		event = ((NewObjectEvent) eventI).getObject();
	    }
	    String base = (Rmap.PATHDELIMITER +
			   ((getRequest().getName() == null) ?
			    "" :
			   (getRequest().getName() + Rmap.PATHDELIMITER)));
	    Rmap levent = (Rmap) event,
		 matchedR = getRequest().findDescendant
		(base + levent.getName(),
		 false);

	    if (matchedR == null) {
		if (levent instanceof ServerHandler) {
		    matchedR = getRequest().findDescendant(base + ".",
							   false);
		} else if ((levent instanceof Server) &&
			   (getTo() instanceof StreamServerListener) &&
			   (((Rmap) ((StreamListener)
				     getTo()).getSource
			     ()).getParent() == levent)) {
		    matchedR = getRequest().findDescendant(base + "..",
							   false);
		}

		if (matchedR == null) {
		    matchedR = getRequest().findDescendant(base + "...",
							   false);
		    if (matchedR == null) {
			matchedR = getRequest().findDescendant(base + "*",
							       false);
			if (matchedR != null) {
			    matchedR = (Rmap) matchedR.clone();
			    matchedR.setName(levent.getName());
			}
		    }
		}
	    }

	    resultR = (((matchedR == null) || (matchedR.getName() == null)) ?
		       null :
		       matchedR);
	}

	return (resultR);
    }

    /**
     * Runs the <code>AwaitNotification</code> handler.
     * <p>
     * This method handles notification events by comparing each event
     * <code>Serializable</code> to the request <code>Rmap</code>. If a match
     * can be found, the event <code>Serializable</code> is passed to the
     * notification target.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/26/2004  INB	Added handling of <code>terminateRequested</code>.
     * 02/24/2004  INB	Added optional debug in <code>run</code>.
     * 01/15/2004  INB	Post an <code>EndOfStream</code> message to our
     *			output client if the latter is a
     *			<code>StreamListener</code>.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2001  INB	Created.
     *
     */
    public final void run() {
	Serializable event = null;
	
	/*
	if (getTo() instanceof StreamListener) {
	    StreamListener sl = (StreamListener) getTo();
	    if ((System.getProperty("LOGRBNBEVENTS") != null) &&
		(System.getProperty("LOGRBNBEVENTS").indexOf("Await") != -1) &&
		(sl.getNBO() != null) &&
		(sl.getNBO().getLog() != null)) {
		try {
		    sl.getNBO().getLog().addMessage
			(Log.STANDARD,
			 Log.CLASS_NBO,
			 toString(),
			 "Started for " + sl);
		} catch (Exception e) {
		}
	    }
	}
	*/

	try {
	    while (true) {
		if ((event = getNextEvent()) != null) {
//System.err.println("AwaitNotification.run: event "+event);
		    Rmap match = null;
		    Serializable realEvent = event;
		    if (event instanceof NewObjectEvent) {
			realEvent = ((NewObjectEvent) event).getObject();
		    }
		    if ((getRequest() == null) ||
			((match = matches(realEvent)) != null)) {
			getTo().accept(event,match);
		    }

		    synchronized (this) {
			++eventsHandled;
			notifyAll();
		    }

		    ensureLocksCleared(toString(),
				       "AwaitNotification.run(1)",
				       null,
				       (byte) 0,
				       0L);
		}

		if (terminateRequested) {
		    break;
		}
	    }

	} catch (java.lang.Throwable e) {
	    if (getTo() instanceof StreamListener) {
		try {
		    getTo().accept(new EndOfStream(),null);
		} catch (java.lang.Throwable e1) {
		}
	    }
	}

	synchronized (this) {
	    if (eventsHandled < eventsAdded) {
		eventsHandled = eventsAdded;
		notifyAll();
	    }
	}

	/*
	if (getTo() instanceof StreamListener) {
	    StreamListener sl = (StreamListener) getTo();
	    if ((System.getProperty("LOGRBNBEVENTS") != null) &&
		(System.getProperty("LOGRBNBEVENTS").indexOf("Await") != -1) &&
		(sl.getNBO() != null) &&
		(sl.getNBO().getLog() != null)) {
		try {
		    sl.getNBO().getLog().addMessage
			(Log.STANDARD,
			 Log.CLASS_NBO,
			 toString(),
			 "Stopping for " + sl);
		} catch (Exception e) {
		}
	    }
	}
	*/
	getFrom().removeNotification(this);
	getTo().removeNotification(this);

	ensureLocksCleared(toString(),
			   "AwaitNotification.run(2)",
			   null,
			   (byte) 0,
			   0L);
    }

    /**
     * Sets the source of the notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fromI  the source of the notification.
     * @see #getFrom()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final void setFrom(NotificationFrom fromI) {
	from = fromI;
	fromI.addNotification(this);
    }

    /**
     * Sets the request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @see #getRequest()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final void setRequest(Rmap requestI) {
	request = requestI;
    }

    /**
     * Sets the target of the notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param toI  the target of the notification.
     * @see #getTo()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final void setTo(NotificationTo toI) {
	to = toI;
	to.addNotification(this);
    }

    /**
     * Gets the sorting value for this <code>AwaitNotification</code>.
     * <p>
     * The sort identifier for <code>AwaitNotifications</code> is the name.
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
     * @version 01/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    public Object sortField(Object sidI)
	throws com.rbnb.utility.SortException
    {
	if (sidI != null) {
	    // Only the null sort identifier is supported.
	    throw new com.rbnb.utility.SortException
		("The sort identifier for AwaitNotification must be null.");
	}

	return (getName());
    }
}
