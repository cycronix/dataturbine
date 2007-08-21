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
 * RBNB Ring Buffer class.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.1
 * @version 10/03/2005
 */

/*
 * Copyright 2003 - 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/11/2006  EMF      Added cache/archive looping by time instead of frame.
 * 10/03/2005  JPW	Change <code>addChild</code>: Rather than just
 *			returning, throw IllegalStateException if the start
 *			time of the current frame is before the end time of the
 *			previous frame.
 * 10/13/2004  JPW	Changed <code>addChild</code> to allow backward
 *			going time when there is no archive and the number
 *			of cache frames = 1.
 * 04/13/2004  INB	Changed <code>addChild/acceptFrame</code> to run in a
 *			single thread to eliminate potential deadlock issues.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 02/03/2004  INB	Throw <code>IllegalMonitorStateException> if our parent
 *			shuts down while we're unlocked.
 * 01/09/2004  INB	Added <code>flush</code> method.
 * 01/08/2004  INB	Added <code>clearCache</code> method.
 * 12/22/2003  INB	In <code>moveDownFrom</code>, if our parent shuts down
 *			while we're unlocked, then return immediately.
 * 12/11/2003  INB	Added <code>RequestOptions</code> to
 *			<code>TimeRelativeRequest</code> handling to allow the
 *			code to do the right thing for
 *			<code>extendStart</code>.
 * 12/08/2003  INB	Handle <code>RequestOptions.extendStart</code> in
 *			<code>individualTimeRelative</code>.
 * 11/20/2003  INB	Save the summary of the registration as the last frame
 *			summary when the registration is updated.
 *			the registration.
 * 11/17/2003  INB	Use <code>try/finally</code> to ensure that we don't
 *			leave flags set on an error.
 *			Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/12/2003  INB	Added identification and location of locks.
 * 11/05/2003  INB	Added <code>individualTimeRelative</code>.
 * 10/31/2003  INB	Added message buffers to hold the messages produced
 *			by the recovery sequence.
 * 10/29/2003  INB	Unlock our parent while we are blocked in
 *			<code>moveDownFrom</code>.
 * 10/27/2003  INB	Make sure that we lock the <code>RingBuffer</code>
 *			for read before moving down in it.  Also, lock the
 *			<code>RBO</code> for write when we're adding to the
 *			<code>RingBuffer</code>.
 * 10/10/2003  INB	Added <code>matchTimeRelative</code>.
 * 09/09/2003  INB	Allow for existing cache/archive in
 *			<code>validateArchive</code>.
 * 07/30/2003  INB	Added <code>destroy</code> method to allow the
 *			<code>RingBuffer</code> to be removed from the
 *			<code>RBO</code>.  Added <code>nullify</code> method.
 * 04/30/2003  INB	Improved <code>fatalError</code> handling and added
 *			<code>cleanupError</code> handling.
 * 04/29/2003  INB	Added <code>Error</code> handling.
 * 04/24/2003  INB	Added <code>markOutOfDate</code>.
 * 03/27/2003  INB	Eliminated unnecessary locks.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 02/17/2003  INB	Created from the original <code>RBO</code> class, which
 *			supported only a single <code>Cache</code> and
 *			<code>Archive</code>. This class provides for multiple
 *			<code>RingBuffers</code> in a single <code>RBO</code>,
 *			each storing a subset of the data.
 *
 */
final class RingBuffer
    extends com.rbnb.api.Rmap
    implements /*com.rbnb.api.Action,*/
	       com.rbnb.api.DataSizeMetricsInterface,
	       com.rbnb.api.GetLogInterface,
	       com.rbnb.api.NotificationFrom
{
    /**
     * accepting a frame?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private boolean acceptingAFrame = false;

    /**
     * adding a frame?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private boolean addingAFrame = false;

    /**
     * the file archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private Archive archive = null;

    /**
     * list of objects awaiting update notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private java.util.Vector awaiting = new java.util.Vector();

    /**
     * "bad" <code>RingBuffer</code>.
     * <p>
     * If this flag is set, then this <code>RingBuffer</code> is a "bad"
     * one. It contains no useful data, but simply acts as a place holder.
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/12/2003
     */
    private boolean bad = false;

    /**
     * the cache memory.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private Cache cache = null;

    /**
     * cleanup an error that occured in this <code>RingBuffer</code>?
     * <p>
     * These are errors that do not necessarily mean that anything has been
     * corrupted.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #fatalError
     * @since V2.1
     * @version 03/10/2003
     */
    private boolean cleanupError = false;

    /**
     * fatal error occured in this <code>RingBuffer</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @see #cleanupError
     * @since V2.1
     * @version 03/10/2003
     */
    private boolean fatalError = false;

    /**
     * the last frame received.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private Rmap frame = null;

    /**
     * is the ring buffer full?
     * <p>
     * The ring buffer is considered to be full if the <code>Cache</code>
     * contains more <code>FrameSets</code> than it is supposed to and the
     * <code>Archive</code> has not archived one or more of those
     * <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private boolean full = false;

    /**
     * has something changed in this <code>RingBuffer</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/12/2003
     */
    private boolean hasChanged = false;

    /**
     * index used for identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private long idIndex;

    /**
     * the last frame time summary.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private Rmap lastFrameSummary = null;

    /**
     * the last registration time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private long lastRegistration = 0;

    /**
     * the last registration result.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/12/2003
     */
    private int lastRegistrationResult = 0;

    /**
     * the names of the channels contained herein.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/10/2003
     */
    private String[] myNames = null;

    /**
     * the registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private Registration registered = null;

    /**
     * registration door.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private Door registrationDoor = null;

    /**
     * stop this <code>RingBuffer</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/19/2003
     */
    private boolean terminateRequested = false;

//EMF 10/11/06: temporary variables for cache/archive loop by time
// ADD TO SAPI; CURRENTLY DEBUG FLAGS
    private float cacheflush=0;
    private float cachetrim=0;
    private float archiveflush=0;
    private float archivetrim=0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code>
     *			constructor.
     *
     */
    RingBuffer()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	registrationDoor = new Door(Door.READ_WRITE);
        //EMF 10/5/06: temporary way to set flush & trim intervals
/*        String trimtimes=System.getProperty("trimtimes");
        if (trimtimes!=null && trimtimes.length()>=7) {
          try {
            String[] toks=trimtimes.split(",");
            cacheflush=Float.parseFloat(toks[0]);
            cachetrim=Float.parseFloat(toks[1]);
            archiveflush=Float.parseFloat(toks[2]);
            archivetrim=Float.parseFloat(toks[3]);
System.err.println("using times for cache/archive flush/trim: ");
System.err.println("cacheflush="+cacheflush+", cachetrim="+cachetrim+", archiveflush="+archiveflush+", archivetrim="+archivetrim);
          } catch (Exception e) {
            System.err.println("Exception parsing trimtimes property, using frame counts instead of times:");
            e.printStackTrace();
            cacheflush=0;
            cachetrim=0;
            archiveflush=0;
            archivetrim=0;
          }
        } 
*/
    }

    /**
     * Class constructor to build a <code>RingBuffer</code> from an
     * identification index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idIndexI	      the identification index.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 03/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created.
     *
     */
    RingBuffer(long idIndexI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	setIndex(idIndexI);
    }

    /**
     * Accepts a new frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI the frame to process.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 04/13/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/13/2004  INB	Changed <code>addChild/acceptFame</code> to run in a
     *			single thread to eliminate potential deadlock issues.
     * 11/17/2003  INB	Use <code>try/finally</code> to ensure that we don't
     *			leave flags set on an error.
     * 11/12/2003  INB	Add location to the locks.
     * 10/27/2003  INB	Lock the <code>RBO</code>.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code>
     *			method.
     *
     */
    final void acceptFrame(Rmap frameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	SourceHandler sh = (SourceHandler) getParent();
	boolean addedFrame = false;

	// Look for a frame to process.
	try {
	    setAcceptingAFrame(true);

	    if (!getTerminateRequested() && !sh.getPerformReset()) {
		try {
		    ((RBO) sh).lockWrite("RingBuffer.acceptFrame");
		    registrationDoor.lock("RingBuffer.acceptFrame");
		    
		    // Note that something has changed.
		    if (!hasChanged) {
			hasChanged = true;
			((RBO) sh).rbsChanged.put(this,this);
		    }

		    // Add the frame to the <code>Cache</code>.
		    getCache().addElement(frameI);

		} finally {
		    if (registrationDoor != null) {
			registrationDoor.unlock();
			((RBO) sh).unlockWrite();
		    } else {
			((RBO) sh).unlockWrite();
			return;
		    }
		}

		addedFrame = true;
	    }

	} finally {
	    // We're done accepting the frame, just need to perform special
	    // handling.
	    setAcceptingAFrame(false);

	    // Do any specialized handling of the frame.
	    if (addedFrame) {
		handleNewFrame(frameI);
	    }
	}
    }

    /**
     * Adds a child <code>Rmap</code>.
     * <p>
     * This method just makes the <code>Rmap</code> available to the data
     * handler thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.IllegalStateException
     *		  thrown if start time of current frame is before end time of
     *		  previous frame
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #acceptFrame(com.rbnb.api.Rmap frameI)
     * @since V2.1
     * @version 10/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/03/2005  JPW	Rather than just returning, throw IllegalStateException
     *			if the start time of the current frame is before the
     *			end time of the previous frame.
     * 10/13/2004  JPW	Allow backward going time for the special case that
     *                  there's no archive and the number of cache frames = 1
     * 04/13/2004  INB	Changed <code>addChild/acceptFrame</code> to run in a
     *			single thread to eliminate potential deadlock issues.
     * 11/17/2003  INB	Use <code>try/finally</code> to ensure that we don't
     *			leave flags set on an error.
     * 04/30/2003  INB	When waiting, check
     *			<code>fatalError/cleanupError</code>.
     * 03/27/2003  INB	Eliminated locks.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code>
     *			method.
     *
     */
    public final void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.IllegalStateException,
	       java.lang.InterruptedException
    {
	if (cleanupError || fatalError) {
	    return;
	}

	if (childI instanceof StorageManager) {
	    // If the child is the <code>StorageManager</code>, add it
	    // directly.
	    super.addChild(childI);

	} else {
	    try {
		setAddingAFrame(true);
		if (getFull()) {
		    throw new java.io.IOException
			("Cannot add frame " + childI + " to " + getName() +
			 ", the ring buffer is full.");
		}

		SourceHandler sh = (SourceHandler) getParent();
		if (!(sh instanceof NBO) && !(sh instanceof Log)) {
		    Rmap summary = childI.summarize();
		    double endOfLast =
			(lastFrameSummary == null) ?
			Double.NEGATIVE_INFINITY :
			(lastFrameSummary.getTrange().getTime() +
			 lastFrameSummary.getTrange().getDuration());
		    // JPW 10/13/2004: Allow backward going time for the
		    //                 special case that there's no archive
		    //                 and the number of cache frames = 1
		    if (((sh.getCframes() > 1) || (sh.getAframes() > 0)) &&
			(lastFrameSummary != null) &&
			(summary.getTrange() != null) &&
			(endOfLast > summary.getTrange().getTime())) {
			if (endOfLast - (endOfLast*1e-14) >
			    summary.getTrange().getTime()) {
			    setAddingAFrame(false);
			    // JPW 09/30/2005: Don't print out the Rmaps
			    //                 in this message
			    sh.getLog().addMessage
				(sh.getLogLevel(),
				 sh.getLogClass(),
				 sh.getName(),
				 // "Cannot add frame to " + sh.getName() +
				 //     ", it starts before the end of the " +
				 //     "previous frame.\n" + childI + "\n" +
				 //     lastFrameSummary);
				 "Cannot add frame to " + sh.getName() +
				     ", it starts before the end of the " +
				     "previous frame.\n" +
				     "\tEnd of previous frame: " +
				     endOfLast +
				     "\tStart of new frame: " +
				     summary.getTrange().getTime());
			    // JPW 09/30/2005: Throw an exception (rather than
			    //                 just return) if time is going
			    //                 backward.
			    // return;
			    throw new java.lang.IllegalStateException(
			        "Backward going time");
			}
		    }
		    lastFrameSummary = summary;
		}

		// Add the frame.
		// picked up.
		acceptFrame(childI);

	    } finally {
		setAddingAFrame(false);
	    }
	}
    }

    /**
     * Adds an <code>AwaitNotification</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param anI  the <code>AwaitNotification</code> object.
     * @see #removeNotification(com.rbnb.api.AwaitNotification)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code>
     *			method.
     *
     */
    public final void addNotification(AwaitNotification anI) {
	awaiting.addElement(anI);
    }

    /**
     * Calculates the cache and archive data sizes in bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheDSIO   the cache data size.
     * @param archiveDSIO the archive data size.
     * @since V2.1
     * @version 04/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    public final void calculateDataSizes(long[] cacheDSIO,
					 long[] archiveDSIO)
    {
	if (fatalError) {
	    return;
	}

	if (getCache() != null) {
	    try {
		cacheDSIO[0] += getCache().getDataSize();
	    } catch (java.lang.Exception e) {
	    }
	}
	if (getArchive() != null) {
	    try {
		archiveDSIO[0] += getArchive().getDataSize();
	    } catch (java.lang.Exception e) {
	    }
	}
    }

    /**
     * Clears the contents of the <code>Cache</code>.
     * <p>
     * If there is an <code>Archive</code>, all data will be flushed to it
     * before the <code>Cache</code> is cleared to ensure that the data is not
     * lost.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.2
     * @version 01/12/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public final void clearCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    long startAt = System.currentTimeMillis();
	    long now;
	    synchronized (this) {
		while ((getFrame() != null) ||
		       getAcceptingAFrame() ||
		       getAddingAFrame()) {
		    wait(TimerPeriod.NORMAL_WAIT);

		    if (((now = System.currentTimeMillis()) - startAt) >=
			TimerPeriod.LOCK_WAIT) {
			try {
			    throw new Exception
				(System.currentTimeMillis() + " " +
				 getParent().getName() + "/RB" +
				 getIndex() +
				 " blocked in clearCache waiting " +
				 "for work to complete: " +
				 getFrame() + " " +
				 getAcceptingAFrame() + " " +
				 getAddingAFrame());
			} catch (Exception e) {
			    e.printStackTrace();
			    startAt = now;
			}
		    }
		}
	    }

	    registrationDoor.lock("RingBuffer.clearCache");
	    if (getCache() != null) {
		getCache().clearCache();
		hasChanged = true;
		updateRegistration();
	    }
	} finally {
	    registrationDoor.unlock();
	}
    }

    /**
     * Compares the sorting value of this <code>RingBuffer</code> to the
     * input sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * The sorting value for an <code>RingBuffer</code> is always itself.
     * <p>
     * If the input is also a <code>RingBuffer</code>, then the comparison is
     * by identification index.
     * <p>
     * If the input is not a <code>RingBuffer</code>, then the
     * <code>Rmap</code> comparison method is used.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>RingBuffer</code> compares less than the
     *		   input,
     *	       <p> 0 if this <code>RingBuffer</code> compares equal to the
     *		   input, and
     *	       <p>>0 if this <code>RingBuffer</code> compares greater than
     *		   the input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @exception java.lang.IllegalStateException
     *		  thrown if both the this <code>RingBuffer</code> and the
     *		  input <code>Rmap</code> are nameless and timeless.
     * @see #compareTo(com.rbnb.api.Rmap)
     * @since V2.1
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created.
     *
     */
    public final int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	int comparedR = 0;

	if (otherI instanceof RingBuffer) {
	    RingBuffer other = (RingBuffer) otherI;
	    long diff = getIndex() - other.getIndex();
	    comparedR = (diff < 0) ? -1 : (diff == 0) ? 0 : 1;

	} else {
	    comparedR = super.compareTo(sidI,otherI);
	}

	return (comparedR);
    }

    /**
     * Destroys this <code>RingBuffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem performing the delete.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    final void destroy()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    registrationDoor.lock("RingBuffer.destroy");

	    if (getArchive() != null) {
		getArchive().deleteArchive();
		getArchive().nullify();
		setArchive(null);
	    }

	} finally {
	    registrationDoor.unlock();
	}
    }

    /**
     * Flushes the <code>Cache</code> of this <code>RingBuffer</code> to the
     * <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.2
     * @version 01/09/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2004  INB	Created.
     *
     */
    final void flush()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getCache() != null) {
	    getCache().flush();
	}
//EMF 11/16/06: close FileSet as well
if (getArchive() != null) {
getArchive().close();
}
    }

    /**
     * Gets the accepting a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we in the process of accepting a frame?
     * @see #setAcceptingAFrame(boolean)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final boolean getAcceptingAFrame() {
	return (acceptingAFrame);
    }

    /**
     * Gets the adding a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we in the process of adding a frame?
     * @see #setAddingAFrame(boolean)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final boolean getAddingAFrame() {
	return (addingAFrame);
    }

    /**
     * Gets the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Archive</code>.
     * @see #setArchive(com.rbnb.api.Archive)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final Archive getArchive() {
	return (fatalError ? null : archive);
    }

    /**
     * Returns the <code>Archive</code> directory for this
     * <code>RingBuffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Archive</code> directory.
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created.
     *
     */
    final String getArchiveDirectory() {
	return (((getParent() == null) ? "." :
		 ((SourceHandler) getParent()).getArchiveDirectory()) +
		Archive.SEPARATOR +
		"RB" + getIndex());
    }

    /**
     * Gets the bad flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>RingBuffer</code> bad?
     * @see #setBad(boolean)
     * @since V2.1
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Created.
     *
     */
    final boolean getBad() {
	return (bad);
    }

    /**
     * Gets the <code>Cache</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Cache</code>.
     * @see #setCache(com.rbnb.api.Cache)
     * @since V2.1
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final Cache getCache() {
	return (fatalError ? null : cache);
    }

    /**
     * Gets the ring buffer full flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the ring buffer full?
     * @see #setFull(boolean)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final boolean getFull() {
	return (full);
    }

    /**
     * Gets the frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the frame <code>Rmap</code>.
     * @see #setFrame(com.rbnb.api.Rmap)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final Rmap getFrame() {
	return (frame);
    }

    /**
     * Gets the identification index.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the identification index.
     * @see #setIndex(long)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created.
     *
     */
    final long getIndex() {
	return (idIndex);
    }

    /**
     * Gets the last registration time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last registration time.
     * @see #setLastRegistration(long)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final long getLastRegistration() {
	return (lastRegistration);
    }

    /**
     * Gets the last registration result.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last registration result.
     * @see #setLastRegistrationResult(int)
     * @see #updateRegistration()
     * @since V2.1
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Created.
     *
     */
    final int getLastRegistrationResult() {
	return (lastRegistrationResult);
    }

    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created from the corresponding <code>Archive</code>
     *			method.
     *
     */
    public final Log getLog() {
	Log logR = null;

	if (getParent() instanceof GetLogInterface) {
	    logR = ((GetLogInterface) getParent()).getLog();
	}

	return (logR);
    }

    /**
     * Gets the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Registration</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @see #setRegistered(com.rbnb.api.Registration)
     * @since V2.1
     * @version 03/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final Rmap getRegistered()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (fatalError ? null : registered);
    }

    /**
     * Gets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return has a stop been requested?
     * @see #setTerminateRequested(boolean)
     * @since V2.1
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Created from corresponding <code>RBNBClient</code>
     *			method.
     *
     */
    final boolean getTerminateRequested() {
	return (terminateRequested);
    }

    /**
     * Handles a new frame <code>Rmap</code>.
     * <p>
     * This method is called whenever a frame <code>Rmap</code> is accepted by
     * the <code>RingBuffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param theFrameI  the frame to handle.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception java.lang.InterruptedException
     *		  thrown if the display operation is interrupted.
     * @since V2.0
     * @version 03/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void handleNewFrame(Rmap theFrameI)
	throws com.rbnb.api.AddressException,
	       java.lang.InterruptedException
    {
	// INB 03/18/2003 - for now, we post via the
	// <code>RBO</code>. Eventually, we may want to allow individual
	// <code>RingBuffers</code> to be watched, which will improve
	// performance.
	((RBO) getParent()).handleNewFrame(theFrameI);

	/*
	// Notify people of the arrival.
	try {
	    post(theFrameI);
	} catch (com.rbnb.api.SerializeException e) {
	    throw new java.lang.InternalError();
	} catch (java.io.IOException e) {
	    throw new java.lang.InternalError();
	}
	*/
    }

    /**
     * Splits the input request into individual requests for each of the
     * channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param optionsI  the <code>RequestOptions</code>.
     * @return a regular request.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Rmap</code> hierarchy contains
     *		  information that cannot be processed by this code.
     * @since V2.2
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Handle <code>RequestOptions.extendStart</code>.
     * 10/10/2003  INB	Created.
     *
     */
    final Rmap individualTimeRelative(TimeRelativeRequest requestI,
				      RequestOptions optionsI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap requestR = new DataRequest();

	boolean locked = false;
	try {
	    lockRead("RingBuffer.individualTimeRelative");
	    locked = true;
	    Rmap rRequest;
	    Rmap child;
	    com.rbnb.utility.SortedVector channels = requestI.getByChannel();
	    TimeRelativeResponse response;
	    TimeRelativeRequest trr;

	    for (int idx = 0; idx < channels.size(); ++idx) {
		trr = new TimeRelativeRequest();
		trr.setNameOffset(requestI.getNameOffset());
		trr.setRelationship(requestI.getRelationship());
		trr.setTimeRange(requestI.getTimeRange());
		trr.addChannel((TimeRelativeChannel) channels.elementAt(idx));

		response = matchTimeRelative(trr,optionsI);
		if (response.getStatus() == 0) {
		    rRequest = response.buildRequest(trr,optionsI);
		    if (rRequest != null) {
			child = rRequest.getChildAt(0);
			rRequest.removeChild(child);
			requestR.addChild(child);
		    }
		}
	    }

	    if (requestR.getNchildren() == 0) {
		requestR = null;
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	return (requestR);
    }

   /**
     * Is this <code>RingBuffer</code> identifiable?
     * <p>
     * All <code>RingBuffers</code> are identifiable.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this identifiable?
     * @since V2.1
     * @version 03/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/11/2003  INB	Created.
     *
     */
    final boolean isIdentifiable() {
	return (true);
    }

    /**
     * Locates the child at the specified index in the <code>RingBuffer</code>.
     * <p>
     * This method eventually needs to search the <code>Archive</code> as
     * well as the <code>Cache</code>. It also needs to allow for
     * <code>FrameSets</code> limited by memory and not number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI  the child index.
     * @return the child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see com.rbnb.api.Rmap#getChildAt(int)
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final Rmap locateChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (fatalError) {
	    return (null);
	}

	SourceHandler sh = (SourceHandler) getParent();
	RCO rco = sh.getRCO();
	long startAt = System.currentTimeMillis();
	long now;
	synchronized (this) {
	    while ((getFrame() != null) ||
		   getAcceptingAFrame() ||
		   getAddingAFrame()) {
		wait(TimerPeriod.NORMAL_WAIT);

		if (((now = System.currentTimeMillis()) - startAt) >=
		    TimerPeriod.LOCK_WAIT) {
		    try {
			throw new Exception
			    (System.currentTimeMillis() + " " +
			     getParent().getName() + "/RB" +
			     getIndex() +
			     " blocked in locateChildAt waiting " +
			     "for work to complete: " +
			     getFrame() + " " +
			     getAcceptingAFrame() + " " +
			     getAddingAFrame());
		    } catch (Exception e) {
			e.printStackTrace();
			startAt = now;
		    }
		}
	    }
	}

	// Determine which <code>FrameSet</code> the child is in.
	int fs = indexI/getCache().getMeps();
	FrameSet fSet = null;
	if (getArchive() == null) {
	    if (fs >= getCache().getNchildren()) {
		throw new java.lang.ArrayIndexOutOfBoundsException
		    ("Frameset " + fs + " containing child " + indexI +
		     " is not in range.");
	    }

	    // Now try to get child from that <code>FrameSet</code>.
	    fSet = (FrameSet) getCache().getChildAt(fs);

	} else {
	    throw new java.lang.IllegalStateException
		("Cannot locate child in archive yet.");
	}

	Rmap childR = fSet.getChildAt(indexI % getCache().getMeps());
	return (childR);
    }

    /**
     * Locks this <code>RingBuffer</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Use version that takes a location.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void lockRead()
	throws java.lang.InterruptedException
    {
	lockRead("RingBuffer.lockRead");
    }

    /**
     * Locks this <code>RingBuffer</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locationI the location of the caller.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    final void lockRead(String locationI)
	throws java.lang.InterruptedException
    {
	registrationDoor.lockRead(locationI);
    }

    /**
     * Marks this <code>RingBuffer</code> and its parent <code>RBO</code>
     * as out-of-date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  INB	Created.
     *
     */
    final void markOutOfDate() {
	((RBO) getParent()).markOutOfDate();
    }

    /**
     * Matches the contents of this <code>RingBuffer</code> against a
     * <code>TimeRelativeRequest</code>.
     * <p>
     * This method performs two steps:
     * <p><ol>
     *    <li>Match all of the channels found to be in the request against the
     *	      cache, and</li>
     *    <li>Match all channels found, minus any completely matched out of the
     *	      cache against the archive.</li>
     *    </ol>
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @param roI      the <code>RequestOptions</code>.
     * @return a <code>TimeRelativeResponse</code> containing the results.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Rmap</code> hierarchy contains
     *		  information that cannot be processed by this code.
     * @since V2.2
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2003  INB	Added <code>RequestOptions</code> to
     *			<code>TimeRelativeRequest</code> handling to allow the
     *			code to do the right thing for
     *			<code>extendStart</code>.
     * 10/10/2003  INB	Created.
     *
     */
    final TimeRelativeResponse matchTimeRelative(TimeRelativeRequest requestI,
						 RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRelativeResponse responseR = null;

	/*
	System.err.println("RingBuffer.matchTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	boolean locked = false;
	try {
	    lockRead("RingBuffer.matchTimeRelative");
	    locked = true;
	    responseR = getCache().matchTimeRelative(requestI,roI);
	    if (responseR.getStatus() == 3) {
		responseR.setStatus(1);

	    } else if ((responseR.getStatus() == -1) ||
		       (responseR.getStatus() == -3)) {
		// If the request is for data before the <code>Cache</code>,
		// then try the archive.

		if (getArchive() == null) {
		    responseR.setStatus(-1);

		} else {
		    // If there is an archive, then see if we can get the data
		    // out of it.
		    responseR = getArchive().matchTimeRelative(requestI,roI);

		    if (responseR.getStatus() == -3) {
			responseR.setStatus(-1);

		    } else if ((responseR.getStatus() == 1) ||
			       (responseR.getStatus() == 3)) {
			// The data is between the cache and the archive.
			switch (requestI.getRelationship()) {
			case TimeRelativeRequest.BEFORE:
			case TimeRelativeRequest.AT_OR_BEFORE:
			    // If we want a time before the specified time,
			    // then use the end of the archive.
			    responseR = getArchive().beforeTimeRelative
				(requestI,
				 roI);
			    if ((responseR.getStatus() == -3) ||
				(responseR.getStatus() == 3)) {
				responseR.setStatus(-1);
			    }
			    break;

			case TimeRelativeRequest.AT_OR_AFTER:
			case TimeRelativeRequest.AFTER:
			    // If we want a time after the specified time, then
			    // use the beginning of the cache.
			    responseR = getCache().beforeTimeRelative
				(requestI,
				 roI);
			    if ((responseR.getStatus() == -3) ||
				(responseR.getStatus() == 3)) {
				responseR.setStatus(1);
			    }
			    break;
			}
		    }
		}
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	/*
	System.err.println("RingBuffer.matchTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy in response to a
     * request <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI    the <code>RmapExtractor</code> performing the
     *			    extraction.
     * @param unsatisfiedI  the unsatisfied <code>ExtractedChain</code>.
     * @param unsatsifiedO  the new list of unsatisfied
     *			    <code>ExtractedChains</code>.
     * @param the reason for a failed match.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to the
     *		  source <code>Rmap</code> hierarchy or in extracting the
     *		  desired information.
     * @since V2.0
     * @version 02/03/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/03/2004  INB	Throw <code>IllegalMonitorStateException> if our parent
     *			shuts down while we're unlocked.
     * 12/22/2003  INB	If our parent shuts down while we're unlocked, then
     *			return immediately.
     * 11/12/2003  INB	Added location to the locks.
     * 10/29/2003  INB	Unlock our parent while we wait in the synch block.
     * 10/27/2003  INB	Always lock for read.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final byte moveDownFrom(RmapExtractor extractorI,
			    ExtractedChain unsatisfiedI,
			    java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (fatalError) {
	    return (MATCH_ILLEGAL);
	}

	/*
	System.err.println(Thread.currentThread() + " entering " +
			   getParent().getName() + "/RB" +
			   getIndex() +
			   " moveDownFrom synch block.");
	*/

	/* Unlock our parent so we don't cause deadlocks. */
	boolean parentLocked = true;
	RBO myRBO = (RBO) getParent();
	Exception internalException = null;
	try {
	    if (myRBO == null) {
		return (MATCH_ILLEGAL);
	    }
	    myRBO.unlockRead();
	    parentLocked = false;

	    long startAt = System.currentTimeMillis();
	    long now;
	    synchronized (this) {
		while (!getTerminateRequested() &&
		       ((getFrame() != null) ||
			getAcceptingAFrame() ||
			getAddingAFrame())) {
		    wait(TimerPeriod.NORMAL_WAIT);

		    if (((now = System.currentTimeMillis()) - startAt) >=
			TimerPeriod.LOCK_WAIT) {
			try {
			    throw new Exception
				(System.currentTimeMillis() + " " +
				 myRBO.getName() + "/RB" +
				 getIndex() +
				 " blocked in moveDownFrom waiting " +
				 "for work to complete: " +
				 getFrame() + " " +
				 getAcceptingAFrame() + " " +
				 getAddingAFrame());
			} catch (Exception e) {
			    e.printStackTrace();
			    startAt = now;
			}
		    }
		}
	    }

	} catch (Exception e) {
	    internalException = e;

	} finally {
	    if (!parentLocked) {
		/* Lock our parent again now that we're ready to move on. */
		if (getParent() == null) {
		    throw new java.lang.IllegalMonitorStateException
			("RBO terminated during retrieval of data.");
		}

		try {
		    /*  mjm grope 1/12/06:  following involved in deadlock.  grope remove it to debug
		    System.err.println("mjm 1/12/06:  lock removed debug only Ringbuffer.java");
		    */
		    myRBO.lockRead("RingBuffer.moveDownFrom1");

		} catch (Exception e) {
		    throw new java.lang.IllegalMonitorStateException
			("RBO terminated during retrieval of data.");
		}
		parentLocked = true;
	    }

	    if (internalException != null) {
		Language.throwException(internalException);
	    }
	}

	/*
	System.err.println(Thread.currentThread() + " exited " +
			   getParent().getName() + "/RB" +
			   getIndex() +
			   " moveDownFrom synch block.");
	*/

	if (getTerminateRequested()) {
	    return (MATCH_ILLEGAL);
	}

	byte reasonR = Rmap.MATCH_UNKNOWN;
	boolean locked = false;
	try {
	    if (!(extractorI.getWorkRequest() instanceof DataRequest) ||
		((((DataRequest) extractorI.getWorkRequest()).getReference() ==
		  DataRequest.ABSOLUTE))) {
		// If this is a normal request, match against the
		// <code>RingBuffer</code>.
		/*
		System.err.println(Thread.currentThread() + " entering " +
				   getParent().getName() + "/RB" +
				   getIndex() +
				   " moveDownFrom read lock.");
		*/

		lockRead("RingBuffer.moveDownFrom2");
		locked = true;
		reasonR = super.moveDownFrom(extractorI,
					     unsatisfiedI,
					     unsatisfiedO);

	    } else {
		// If the work request is a special time request, then match
		// against the <code>Registration</code> rather than the
		// <code>RingBuffer</code>. Insert an empty entry into the
		// <code>RmapChain</code>.
		unsatisfiedI.addRmap(new Rmap());

		DataRequest dr = (DataRequest) extractorI.getWorkRequest();
		unsatisfiedI.getRchain().addRmap
		    (new DataRequest(null,
				     null,
				     null,
				     dr.getReference(),
				     dr.getDomain(),
				     1,
				     0,
				     dr.getSynchronized(),
				     dr.getMode()));

		// Now move down from the <code>Registration</code>.
		/*
		System.err.println(Thread.currentThread() + " entering " +
				   getParent().getName() + "/RB" +
				   getIndex() +
				   " moveDownFrom read lock.");
		*/
		lockRead("RingBuffer.moveDownFrom3");
		locked = true;
		Rmap lreg = getRegistered();
		if (lreg  == null) {
		    reasonR = MATCH_ILLEGAL;
		} else {
		    RmapVector rVector = RmapVector.addToVector(null,lreg);
		    reasonR = unsatisfiedI.matchList
			(!extractorI.getExtractRmaps(),
			 rVector,
			 unsatisfiedO);
		}
	    }

	} finally {
	    if (locked) {
		unlockRead();

		/*
		System.err.println(Thread.currentThread() + " exited " +
				   getParent().getName() + "/RB" +
				   getIndex() +
				   " moveDownFrom read lock.");
		*/
	    }
	}

	return (reasonR);
    }

    /**
     * Creates a new instance of the same class as this
     * <code>RingBuffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @since V2.1
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Created from the corresponding
     *			<code>StorageManager</code> method.
     *
     */
    final Rmap newInstance() {
	return (new Rmap());
    }

    /**
     * Nullifies this <code>RingBuffer</code>.
     * <p>
     * This method ensures that all pointers in this <code>DataBlock</code>
     * are cleared, reducing the effort needed by the garbage collector to
     * clean it up.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    public final void nullify() {
	super.nullify();

	try {
	    if (getArchive() != null) {
		getArchive().nullify();
		setArchive(null);
	    }

	    if (awaiting != null) {
		awaiting.removeAllElements();
		awaiting = null;
	    }

	    if (getCache() != null) {
		getCache().nullify();
		setCache(null);
	    }

	    if (getFrame() != null) {
		getFrame().nullify();
		setFrame(null);
	    }

	    if (lastFrameSummary != null) {
		lastFrameSummary.nullify();
		lastFrameSummary = null;
	    }

	    myNames = null;

	    if (getRegistered() != null) {
		getRegistered().nullify();
		setRegistered(null);
	    }

	    if (registrationDoor != null) {
		registrationDoor.nullify();
		registrationDoor = null;
	    }

	} catch (java.lang.Throwable e) {
	}
    }

    /**
     * Notifies all objects awaiting notification of the arrival of an "event"
     * <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> event.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronized (awaiting) {
	    for (int idx = 0,
		     endIdx = awaiting.size();
		 idx < endIdx;
		 ++idx) {
		AwaitNotification an = (AwaitNotification)
		    awaiting.elementAt(idx);

		an.addEvent(serializableI,false);
	    }
	}
    }

    /**
     * Loads the <code>RingBuffer</code> (<code>Cache</code> and
     * <code>Archive</code>) with information from the archive files.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #writeToArchive()
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location to the locks.
     * 02/18/2003  INB	Created from the corresponding <code>Archive</code>
     *			method.
     *
     */
    final void readFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getBad() || fatalError) {
	    return;
	}

	getArchive().readFromArchive();

	try {
	    registrationDoor.lock("RingBuffer.readFromArchive");
	    if (!hasChanged) {
		hasChanged = true;
		((RBO) getParent()).rbsChanged.put(this,this);
	    }
	    updateRegistration();
	} finally {
	    registrationDoor.unlock();
	}
    }

    /**
     * Rebuild an invalid <code>Archive</code> from the last good sequence of
     * <code>Archive</code> <code>FileSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param validSealsI   the valid <code>FileSet Seals</code>.
     * @param invalidSealsI the invalid <code>FileSet Seals</code>.
     * @param goodMessageO  the message buffer for good messages.
     * @param notMessageO   the message buffer for not recovered messages.
     * @param unMessageO    the message buffer for unrecoverable messages.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.InvalidSealException
     *		  thrown if the <code>Seal</code> is invalid.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is just no way to recover anything from the
     *		  <code>Archive</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location to the locks.
     * 10/31/2003  INB	Build a recovery message buffers rather than report to
     *			the log.
     * 04/24/2003  INB	Destroy any old <code>Cache</code> and
     *			<code>Archive</code>.
     * 02/18/2003  INB	Created.
     *
     */
    final Seal recoverFromArchive(java.util.Vector validSealsI,
				  java.util.Vector invalidSealsI,
				  StringBuffer goodMessageO,
				  StringBuffer notMessageO,
				  StringBuffer unMessageO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.InvalidSealException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.IllegalStateException,
	       java.lang.InterruptedException
    {
	try {
	    registrationDoor.lock("RingBuffer.recoverFromArchive");

	    setCache(null);
	    setCache(new Cache(cacheflush,cachetrim));
	    setArchive(null);
	    setArchive(new Archive(archiveflush,archivetrim));
	    getArchive().recoverFromArchive(validSealsI,
					    invalidSealsI,
					    goodMessageO,
					    notMessageO,
					    unMessageO);
	    writeToArchive();

	} finally {
	    registrationDoor.unlock();
	}

	return (Seal.validate(getArchiveDirectory(),
			      Long.MIN_VALUE,
			      Long.MAX_VALUE));
    }

    /**
     * Removes an <code>AwaitNotification</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param anI  the <code>AwaitNotification</code> object.
     * @see #addNotification(com.rbnb.api.AwaitNotification)
     * @since V2.0
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    public final void removeNotification(AwaitNotification anI) {
	awaiting.removeElement(anI);
    }

    /**
     * Performs the "action" of this <code>RingBuffer</code>.
     * <p>
     * The action for the <code>RingBuffer</code> is to add frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 04/29/2003  INB	Handle <code>Errors</code>.
     * 03/13/2003  INB	Created.
     *
     */
    /*
    public final void performAction() {
	SourceHandler sh = (SourceHandler) getParent();
	Log log = sh.getLog();
	if (log == sh) {
	    log = null;
	}

	try {
	    if (!getTerminateRequested()) {
		acceptFrame(true);
	    } else {
		stopAction();
	    }

	} catch (com.rbnb.api.AddressException e) {
	    fatalError = true;
	    try {
		if (log == null) {
		    e.printStackTrace();
		} else {
		    log.addException
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		    sh.getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    try {
		sh.stop(null);
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (com.rbnb.api.SerializeException e) {
	    fatalError = true;
	    try {
		if (sh instanceof Log) {
		    e.printStackTrace();
		} else {
		    log.addException
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		    sh.getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    try {
		sh.stop(null);
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.io.IOException e) {
	    fatalError = true;
	    try {
		if (sh instanceof Log) {
		    e.printStackTrace();
		} else {
		    log.addException
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		    sh.getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    try {
		sh.stop(null);
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.InterruptedException e) {
	    fatalError = true;
	    try {
		if (sh instanceof Log) {
		    e.printStackTrace();
		} else {
		    sh.getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    try {
		sh.stop(null);
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Exception e) {
	    fatalError = true;
	    try {
		if (sh instanceof Log) {
		    e.printStackTrace();
		} else {
		    log.addException
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		    sh.getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    try {
		sh.stop(null);
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Error e) {
	    if (e instanceof java.lang.OutOfMemoryError) {
		cleanupError = true;
	    } else {
		fatalError = true;
	    }
	    try {
		if (sh instanceof Log) {
		    e.printStackTrace();
		} else {
		    log.addError
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		    sh.getRCO().send
			(Language.exception
			 (new com.rbnb.api.SerializeException
			     (e + "\n" + e.getMessage())));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    try {
		sh.stop(null);
	    } catch (java.lang.Throwable e1) {
	    }
	}
    }
    */

    /**
     * Sets the accepting a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptingAFrameI  are we in the process of accepting a frame?
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #getAcceptingAFrame()
     * @since V2.1
     * @version 03/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final synchronized void setAcceptingAFrame(boolean acceptingAFrameI)
	throws java.lang.InterruptedException
    {
	acceptingAFrame = acceptingAFrameI;
	if (!acceptingAFrame) {
	    notifyAll();
	}
    }

    /**
     * Sets the adding a frame flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addingAFrameI  are we in the process of adding a frame?
     * @see #getAddingAFrame()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void setAddingAFrame(boolean addingAFrameI) {
	addingAFrame = addingAFrameI;
    }

    /**
     * Sets the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param archiveI  the new <code>Archive</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>License</code> does not support
     *		  <code>Archives</code>.
     * @see #getArchive()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void setArchive(Archive archiveI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (archiveI == null) {
	    if (getArchive() != null) {
		removeChild(getArchive());
		archive = null;
	    }

	} else {
	    /*
	    SourceHandler sh = (SourceHandler) getParent().getParent();
	    License license = ((RBNB) sh.getParent()).getLicense();
	    if (!license.archives()) {
		throw new java.lang.IllegalStateException
		    ("This license (" +
		     license.version() + " #" + license.serialNumber() +
		     ") does not support archives.");
	    } else*/ if (archive != null) {
		throw new java.lang.IllegalStateException
		    ("Cannot set the archive for a ring buffer that already " +
		     "has one.");
	    }
	    archive = archiveI;
	    addChild(archiveI);
	}
    }

    /**
     * Sets the bad flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param badI bad <code>RingBuffer</code>?
     * @see #getBad()
     * @since V2.1
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Created.
     *
     */
    final void setBad(boolean badI) {
	bad = badI;
    }

    /**
     * Sets the <code>Cache</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheI  the new <code>Cache</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the <code>RBO</code> already has a
     *		  <code>Cache</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getCache()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void setCache(Cache cacheI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((cacheI != null) && (cache != null)) {
	    throw new IllegalStateException
		("Cannot set the cache for a source that already has one.");

	} else if (cache != null) {
	    removeChild(cache);
	}

	cache = cacheI;

	if (cacheI != null) {
	    addChild(cacheI);
	}
    }

    /**
     * Sets the frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI  the new frame <code>Rmap</code>.
     * @see #getFrame()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void setFrame(Rmap frameI) {
	frame = frameI;
    }

    /**
     * Sets the ring buffer full flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fullI  is the ring buffer full?
     * @see #getFull()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    final void setFull(boolean fullI) {
	full = fullI;
    }

    /**
     * Sets the identification index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idIndexI  the new identification index.
     * @see #getIndex()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created.
     *
     */
    final void setIndex(long idIndexI) {
	idIndex = idIndexI;
    }

    /**
     * Sets the last registration time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI  the new registration time.
     * @see #getLastRegistration()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    private final void setLastRegistration(long timeI) {
	lastRegistration = timeI;
    }

    /**
     * Sets the last registration result.
     * <p>
     *
     * @author Ian Brown
     *
     * @param resultI the last registration result.
     * @see #getLastRegistrationResult()
     * @since V2.1
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Created.
     *
     */
    final void setLastRegistrationResult(int resultI) {
	lastRegistrationResult = resultI;
    }

    /**
     * Sets the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param registeredI  the new <code>Registration</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getRegistered()
     * @since V2.1
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location to the locks.
     * 07/30/2003  INB	Allow for <code>null</code> input.
     * 02/17/2003  INB	Created from the corresponing <code>RBO</code> method.
     *
     */
    final void setRegistered(Registration registeredI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    registrationDoor.lock("RingBuffer.setRegistered");

	    if (registeredI != null) {
		registeredI.setParent(this);
	    }
	    registered = registeredI;
	} finally {
	    registrationDoor.unlock();
	}
    }

    /**
     * Sets the terminate requested flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stopI  stop this <code>RBNBClient</code>?
     * @see #getTerminateRequested()
     * @since V2.1
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Created from the corresponding <code>RBNBClient</code>
     *			method.
     *
     */
    final synchronized void setTerminateRequested(boolean stopI) {
	terminateRequested = stopI;
	notifyAll();
    }

    /**
     * Sets up the <code>RingBuffer</code> to accept data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cFrameSetsI  the number of <code>Cache FrameSets</code>.
     * @param cFrFrameSetI the number of frames per <code>Cache
     *			   FrameSet</code>.
     * @param aFileSetsI   the number of <code>Archive FileSets</code>.
     * @param aFrSFileSetI the number of <code>FrameSets</code> per
     *			   <code>Archive FileSet</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created.
     *
     */
    final void setup(int cFrameSetsI,
		     int cFrFrameSetI,
		     int aFileSetsI,
		     int aFrSFileSetI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
        //EMF 10/11/06: if sink connection, use old logic or requests
        //              miss data
        if ((getParent() instanceof NBO)) {
          cacheflush=0;
          cachetrim=0;
          archiveflush=0;
          archivetrim=0;
        }
	setCache(new Cache(cacheflush,cachetrim));
	getCache().setMs(cFrameSetsI);
	getCache().setMeps(cFrFrameSetI);
	if (aFileSetsI != 0) {
	    setArchive(new Archive(archiveflush,archivetrim));
	    getArchive().setMs(aFileSetsI);
	    getArchive().setMeps(aFrSFileSetI);
	}
    }

    /**
     * Sets up the <code>RingBuffer</code> trim times.  Zero values 
     * indicates use old frame count logic.
     * <p>
     *
     * @author Eric Friets
     *
     * @param cacheflushI  seconds between cache flushes (frameset closes)
     * @param cachetrimI   cumulative seconds in cache
     * @param archiveflushI seconds between archive flushes (fileset closes)
     * @param archivetrimI  cumulative seconds in archive
     * @since V2.7
     * @version 10/18/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/18/2006  EMF  Created.
     *
     */
    final void setupTrimTimes(float cacheflushI, 
		     float cachetrimI,
		     float archiveflushI,
		     float archivetrimI)
     {
        //EMF 10/11/06: if sink connection, use old logic or requests
        //              miss data
        if ((getParent() instanceof NBO)) {
          cacheflush=0;
          cachetrim=0;
          archiveflush=0;
          archivetrim=0;
        } else {
          cacheflush=cacheflushI;
          cachetrim=cachetrimI;
          archiveflush=archiveflushI;
          archivetrim=archivetrimI;
	}
    }

    /**
     * Shutdown the <code>RingBuffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/09/2004  INB	Call the new <code>flush</code> method.
     * 04/29/2003  INB	Handle <code>Errors</code>.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    void shutdown() {
	stopAwaiting();
	SourceHandler sh = (SourceHandler) getParent();
	if (((sh.getAmode() == Source.ACCESS_CREATE) ||
	     (sh.getAmode() == Source.ACCESS_APPEND)) &&
	    (getCache() != null) &&
	    (getArchive() != null)) {
	    // If we have a <code>Archive</code>, flush the cache.
	    try {
		flush();
		
	    } catch (java.lang.InterruptedException e) {
	    } catch (java.lang.Exception e) {
		try {
		    getLog().addException
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		} catch (java.lang.Throwable e1) {
		}
	    } catch (java.lang.Error e) {
		try {
		    getLog().addError
			(Log.STANDARD,
			 sh.getLogClass(),
			 sh.getName(),
			 e);
		} catch (java.lang.Throwable e1) {
		}
	    }
	}
    }

    /**
     * Starts the <code>RingBuffer</code>.
     * <p>
     * This method is a NOP.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.IllegalStateException
     *		  thrown if the source is already running or if the setup is
     *		  inconsistent.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #stop()
     * @since V2.1
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Identify the lock.
     * 02/17/2003  INB	Created from the corresponding <code>RBNBClient</code>
     *			method.
     *
     */
    final void start()
	throws java.lang.InterruptedException
    {
	registrationDoor.setIdentification
	    (getParent().getName() + "/RB" + getIndex());
    }

    /**
     * Stops the <code>RingBuffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if the stop is interrupted.
     * @see #start()
     * @since V2.1
     * @version 04/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/30/2003  INB	Cleanup on errors, just don't bother with frames.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Created from the corresponding <code>RBNBClient</code>
     *			method.
     *
     */
    final void stop()
	throws java.lang.InterruptedException
    {
	setTerminateRequested(true);

	if (!cleanupError && !fatalError) {
	    synchronized (this) {
		long startAt = System.currentTimeMillis(),
		    endAt;
		while ((frame != null) &&
		       ((endAt = System.currentTimeMillis()) <
			startAt + TimerPeriod.SHUTDOWN)) {
		    try {
			wait(TimerPeriod.NORMAL_WAIT);
		    } catch (java.lang.InterruptedException e) {
			break;
		    }
		}
	    }
	}

//	stopAction();
	shutdown();
    }

    /**
     * Stops this action (removes it from the queue).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    /*
    public final void stopAction() {
	frame = null;
	((ServerHandler) getParent().getParent()).getActivityQueue
	    ().removeEvent(this);
	synchronized (this) {
	    notifyAll();
	}
    }
    */

    /**
     * Stops the objects in the <code>AwaitNotification</code> list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the correpsonding <code>RBO</code> method.
     *
     */
    private final void stopAwaiting() {
	if (awaiting.size() > 0) {
	    // Clear the awaiting notification list.
	    java.util.Vector anv = (java.util.Vector) awaiting.clone();

	    for (int idx = 0,
		     endIdx = anv.size();
		 idx < endIdx;
		 ++idx) {
		Interruptable an = (Interruptable) anv.elementAt(idx);

		an.interrupt();
	    }
	}
    }

    /**
     * Unlocks this <code>Ring Buffer</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    void unlockRead()
	throws java.lang.InterruptedException
    {
	registrationDoor.unlockRead();
    }

    /**
     * Updates the <code>Rregistration</code> for the <code>Ring Buffer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return was the <code>Registration</code> changed?
     *	       <p><ol start=-1>
     *	       <li>The <code>Registration</code> was completely reset,</li>
     *	       <li>The <code>Registration</code> wasn't changed,</li>
     *	       <li>The <code>Registration</code> was updated.</li>
     *	       </ol>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 11/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --   -----------
     * 11/20/2003  INB	Save the summary of the registration as the last frame
     *			summary.
     * 11/12/2003  INB	Added location to the locks.
     * 07/30/2003  INB	Nullify the registration if it is to be replaced.
     * 02/17/2003  INB	Created from the corresponding <code>RBO</code> method.
     *
     */
    public final int updateRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (fatalError || !hasChanged) {
	    return (0);
	}

	int updatedR = 0;

	try {
	    registrationDoor.lock("RingBuffer.updateRegistration");

	    boolean fullUpdate = (getRegistered() == null);
	    int changedCache = 0,
		changedArchive = 0;
	    if (getCache() != null) {
		// Update the <code>Cache Registration</code>.
		if (((changedCache = getCache().updateRegistration()) == 0) &&
		    (getCache().getLastRegistration() >
		     getLastRegistration())) {
		    changedCache = -1;
		}
	    }
	    if (getArchive() != null) {
		// Update the <code>Archive Registration</code>.
		if (((changedArchive =
		      getArchive().updateRegistration()) == 0) &&
		    (getArchive().getLastRegistration() >
		     getLastRegistration())) {
		    changedArchive = -1;
		}
	    }

	    fullUpdate = (fullUpdate ||
			  ((changedArchive == -1) ||
			   ((getArchive() == null) &&
			    (changedCache == -1))));
	    if (fullUpdate) {
		if (getRegistered() != null) {
		    getRegistered().nullify();
		    setRegistered(null);
		}

		updatedR = -1;
		if (getArchive() != null) {
		    setRegistered((Registration)
				  getArchive().getRegistered().clone());
		    if (getCache() != null) {
			((Registration) getRegistered()).updateRegistration
			    (getCache().getRegistered(),
			     false,
			     true);
		    }

		} else if (getCache() != null) {
		    setRegistered((Registration)
				  getCache().getRegistered().clone());

		} else {
		    setRegistered(new Registration());
		}

	    } else {
		if (changedArchive != 0) {
		    ((Registration) getRegistered()).updateRegistration
			(getArchive().getRegistered(),
			 false,
			 true);
		    updatedR = 1;
		}
		if (changedCache != 0) {
		    ((Registration) getRegistered()).updateRegistration
			(getCache().getRegistered(),
			 false,
			 true);
		    updatedR = 1;
		}
	    }

	    if ((myNames == null) || (myNames.length == 0)) {
		myNames = getRegistered().extractNames();
	    }

	} finally {
	    hasChanged = false;

	    if (updatedR != 0) {
		setLastRegistration(System.currentTimeMillis());
		setLastRegistrationResult(updatedR);
		if (lastFrameSummary == null) {
		    lastFrameSummary = getRegistered().summarize();
		}
	    }
	    registrationDoor.unlock();
	}

	return (updatedR);
    }

    /**
     * Validates the <code>RingBuffer's Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param afterI	    the <code>Seal</code> must be after this.
     * @param beforeI	    the <code>Seal</code> must be before this.
     * @param validSealsO   the list of valid <code>FileSet Seals</code>.
     * @param invalidSealsO the list of invalid <code>FileSet Seals</code>.
     * @return the validated <code>Seal</code>, if any.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.InvalidSealException
     *		  thrown if the <code>Seal</code> is invalid.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is just no way to recover anything from the
     *		  <code>Archive</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Allow for existing cache/archive.
     * 02/18/2003  INB	Created from the corresponding method in
     *			<code>Archive</code>.
     *
     */
    final Seal validateArchive(long afterI,
			       long beforeI,
			       java.util.Vector validSealsO,
			       java.util.Vector invalidSealsO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.InvalidSealException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long after = afterI,
	     before = beforeI;

	// Ensure that there is a <code>RingBuffer</code> seal. Without it,
	// the <code>RingBuffer</code> definitely has to be recovered.
	Seal rbSealR = null;

	rbSealR = Seal.validate
	    (getArchiveDirectory(),
	     after,
	     before);
	if (rbSealR != null) {
	    before = rbSealR.getAsOf();
	}

	if (getCache() == null) {
	    setCache(new Cache(cacheflush,cachetrim));
	}
	if (getArchive() == null) {
	    setArchive(new Archive(archiveflush,archivetrim));
	}
	getArchive().validate(after,before,validSealsO,invalidSealsO);

	return (rbSealR);
    }

    /**
     * Writes the <code>RingBuffer</code> to the archive files.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readFromArchive()
     * @since V2.1
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location to the locks.
     * 04/24/2003  INB	Ensure that the registration is up-to-date.
     * 02/18/2003  INB	Created.
     *
     */
    final void writeToArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getBad() || fatalError) {
	    return;
	}

	try {
	    registrationDoor.lock("RingBuffer.writeToArchive");
	    if (getArchive() != null) {
		getArchive().writeToArchive();
		hasChanged = true;
		updateRegistration();
	    }

	} finally {
	    registrationDoor.unlock();
	}
    }
}
