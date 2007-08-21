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
 * Superclass of the short- and long-term storage objects for a
 * <code>SourceHandler</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.SourceHandler
 * @since V2.0
 * @version 12/11/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/26/2007  MJM	Fixed bug with flushInterval/1000 in trimbytime mode
 * 10/11/2006  EMF      Added trim by time for flushing and looping.
 * 05/02/2006  EMF      Compress FrameSets when they fill, rather than when
 *                      writing them to archive.  Improves performance.
 * 12/11/2003  INB	Added <code>RequestOptions</code> to
 *			<code>TimeRelativeRequest</code> handling to allow the
 *			code to do the right thing for
 *			<code>extendStart</code>.
 * 12/09/2003  INB	Don't subtract out the <code>TimeRelativeRequest</code>
 *			duration.
 * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 11/12/2003  INB	Added <code>before/afterTimeRelative</code>.
 * 10/22/2003  INB	Don't access the <code>FileSet</code> files in
 *			<code>addElement</code>.
 * 10/17/2003  INB	Don't update the registration when matching or
 *			moving down - it has already been done.
 * 10/13/2003  INB	Added <code>matchTimeRelative</code> method.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/22/2003  INB	Eliminated specific <code>getDataSize</code> method.
 *			Locking things during that code is problematic during
 *			shutdown.
 * 05/06/2003  INB	Only set us as parent for <code>FileSets</code> in
 *			<code>trim</code>.
 * 03/28/2003  INB	Eliminated unnecessary synchronization/locks.
 * 02/17/2003  INB	Modified for multiple <code>RingBuffers</code> per
 *			<code>RBO</code>.
 * 03/12/2001  INB	Created.
 *
 */
abstract class StorageManager
    extends com.rbnb.api.Rmap
    implements GetLogInterface, Runnable
{
    /**
     * have any sets been removed from the <code>StorageManager</code> since
     * the last time the <code>Registration</code> was updated?
     * <p>
     *
     * @author Ian Brown
     *
     * @see #addedSets
     * @since V2.0
     * @version 05/31/2001
     */
    private boolean removedSets = false;

    /**
     * the door to the <code>StorageManager</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Door door = null;

    /**
     * the current set.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/12/2001
     */
    private FrameManager set = null;

    /**
     * what is the index of the first set added since the last time the
     * <code>Registration</code> was updated?
     * <p>
     * This value is valid only if <code>removedSets</code> is not set.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #removedSets
     * @since V2.0
     * @version 03/12/2001
     */
    private int addedSets = -1;

    /**
     * the maximum number of sets.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/12/2001
     */
    private int maxSets = -1;

    /**
     * the maximum number of elements per set.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/12/2001
     */
    private int maxElementsPerSet = -1;

    /**
     * the maximum amount of memory per set.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/12/2001
     */
    private int maxMemoryPerSet = -1;

    /**
     * last time the <code>Registration</code> was updated.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/12/2001
     */
    private long lastRegistration = Long.MIN_VALUE;

    /**
     * the next set index to create.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/14/2001
     */
    private long nextIndex = 1;

    /**
     * the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Registration registered = null;

    /**
     * compress FrameSets after adding this many frames
     * set later to sqrt (frameset size)
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 05/03/2006
     */
    private int compressAfter = Integer.MAX_VALUE;

    /**
     * number of frames added to current FrameSet
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.6
     * @version 05/03/2006
     */
    private int framesAdded=0;

    /**
     * milliseconds to sleep between data flushes
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/02/2006
     */
    private long flushInterval=0;

    /**
     * seconds to keep in cache/archive
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/02/2006
     */
    private float trimInterval=0;

    /**
     * close by time rather than frames
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/03/2006
     */
    private boolean closeByTime=false;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
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
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/11/2006  EMF  Added arguments, thread starting.
     * 03/12/2000  INB	Created.
     *
     */
    StorageManager(float flushIntervalI, float trimIntervalI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	setDoor(new Door(Door.READ_WRITE));
        //EMF 10/11/2006
        if (flushIntervalI>0 && trimIntervalI>0) {
          closeByTime=true;
          flushInterval=(long)Math.round(flushIntervalI*1000); //convert to milliseconds
          trimInterval=trimIntervalI;
        } else closeByTime=false;
        if (closeByTime) {
          (new Thread(this)).start(); //start timer
        }
//System.err.println("StorageManager: flushInterval="+flushIntervalI+", trimInterval="+trimInterval);
    }

    /**
     * Runs timer to close FrameSets and FileSets as needed
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/02/2006
     *
     */

    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/02/2006  EMF 	Created.
     */
    public void run() {

      //don't flush right away
      try {Thread.currentThread().sleep(flushInterval);} catch (Exception e) {}
      //so long as still attached to RingBuffer, check for data to flush
      do {
        close();
        try {Thread.currentThread().sleep(flushInterval);} catch (Exception e) {}
      } while (getParent()!=null) ;

    } //end run method


    /**
     * Closes current set
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/03/2006
     *
     */

    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2006  EMF  Make public so FileSet can be closed on Source.Detach
     * 10/03/2006  EMF 	Created.
     */
    /*private*/ synchronized void close() {
	if (getSet()!=null) {
          try {
            getSet().close();
	    setSet(null);
          } catch (Exception e) {
            System.err.println("Exception closing set:");
            e.printStackTrace();
          }
        }
    }

    /**
     * Adds an element <code>Rmap</code> to this <code>StorageManager</code>
     * object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param elementI  the element <code>Rmap</code>.
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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/11/2006  EMF  Added trimByTime logic, calls to close().
                        If trimByTime, close() never called from here.
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 10/22/2003  INB	Don't access the <code>FileSet</code> files here.
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/12/2000  INB	Created.
     *
     */
    final synchronized void addElement(Rmap elementI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the door.
	    getDoor().setIdentification(getFullName() + "/" + getClass());
	    getDoor().lock("StorageManager.addElement");

	    if (getSet() == null) {
		// If there isn't a set, then create a new one.
		if (this instanceof Cache) {
		    setSet(new FrameSet(getNextIndex()));
		    compressAfter=(int)Math.floor(Math.sqrt(getMeps()));
		    framesAdded=0;
		} else {
		    setSet(new FileSet(getNextIndex()));
		}
		setNextIndex(getNextIndex() + 1);
	    }
	    // Add the element to the current set. Note that the last set has
	    // been added to, which means its registration needs to be
	    // updated.
	    getSet().addElement(elementI);
	    framesAdded++;
	    if (getAddedSets() == -1) {
		setAddedSets(getNchildren() - 1);
	    }

	    //EMF 5/3/06: compress FrameSet every N frames, to
	    //            improve request speed from cache
	    //EMF 5/3/06: since compressed, Nchildren no longer accurate
	    //            count of number of frames in FrameSet
	    //if ((framesAdded%compressAfter == 0)||(framesAdded==getMeps())) {
	    if (framesAdded==getMeps()) {
		if (this instanceof Cache) {
			//FrameSet oldfs=(FrameSet)getChildAt(getNchildren()-1);
			//oldfs.updateRegistration();
//System.err.println("getSet() "+getSet());
			FrameSet newfs=null; //((FrameSet)getSet()).reframe();
//System.err.println("newfs "+newfs);
//System.err.println("\n");
			if (newfs!=null) {
                		newfs.updateRegistration();
				removeChildAt(getNchildren()-1);
//oldfs.clear(); //try this
//oldfs.nullify();
				removedSets=true;
				setSet(newfs);
				//EMF 5/19/06: reset metrics size cache, or
				// get zero (cache and archive size)
				getSet().setDataSize(-1); 
			}
		// If the current set is full, then close it.
		if (!closeByTime && framesAdded==getMeps()) {
                  close();
		}
		}
	    }
		    

	    if (!closeByTime&&(this instanceof Archive)&&(getSet().getNchildren()==getMeps())) {
              close();
	    }

	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * For <code>StorageManagers</code>, we display:
     * <p><ul>
     * <li>the maximum number of <code>FrameManager</code> sets,</li>
     * <li>the maximum number of elements in a set, and</li>
     * <li>the maximum amount of memory per set.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 05/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/25/2001  INB	Created.
     *
     */
    String additionalToString() {
	return (" Sets: " + getMs() +
		" EPS: " + getMeps() +
		" MPS: " + getMmps());
    }

    /**
     * Builds a request for data for the requested channels starting at the
     * beginning of this <code>StorageManager</code> (which is after the time
     * reference).
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
     * @return the <code>TimeRelativeResponse</code>.
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
     * @see #beforeTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions roI)
     * @since V2.2
     * @version 11/18/2003
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
     * 11/12/2003  INB	Created.
     *
     */
    TimeRelativeResponse afterTimeRelative
	(TimeRelativeRequest requestI,
	 RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("StorageManager.afterTimeRelative: " +
			   getFullName() + "/" + getClass() +
			   "\nRegistration: " + getRegistered() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(-1);

	boolean locked = false;
	try {
	    lockRead();
	    locked = true;

	    if (getRegistered() != null) {
		com.rbnb.utility.SortedVector channels =
		    requestI.getByChannel();
		DataArray limits = getRegistered().extract
		    (((TimeRelativeChannel)
		      channels.firstElement()).getChannelName
		     ().substring(requestI.getNameOffset()));

		if ((limits.timeRanges != null) &&
		    (limits.timeRanges.size() != 0)) {
		    responseR.setTime(limits.getStartTime());
		    responseR.setStatus(0);
		    responseR.setInvert(false);
		}
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	/*
	System.err.println("StorageManager.afterTimeRelative: " + 
			   getFullName() + "/" + getClass() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds a request for data for the requested channels ending at the
     * end of this <code>StorageManager</code> (which is before the time
     * reference).
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
     * @return the request.
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
     * @see #afterTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions roI)
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
     * 12/09/2003  INB	Don't subtract out the <code>TimeRelativeRequest</code>
     *			duration.
     * 11/12/2003  INB	Created.
     *
     */
    TimeRelativeResponse beforeTimeRelative(TimeRelativeRequest requestI,
					    RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("StorageManager.beforeTimeRelative: " +
			   getFullName() + "/" + getClass() +
			   "\nRegistration: " + getRegistered() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(-1);

	boolean locked = false;
	try {
	    lockRead();
	    locked = true;

	    if (getRegistered() != null) {
		if ((roI != null) && roI.getExtendStart()) {
		    // If this is an <code>extendStart</code> request, then
		    // work our way down from the last child.
		    Rmap lastChild = getChildAt(getNchildren() - 1);
		    responseR = lastChild.beforeTimeRelative(requestI,roI);

		} else {
		    com.rbnb.utility.SortedVector channels =
			requestI.getByChannel();
		    DataArray limits = getRegistered().extract
			(((TimeRelativeChannel)
			  channels.firstElement()).getChannelName
			 ().substring(requestI.getNameOffset()));

		    if ((limits.timeRanges != null) &&
			(limits.timeRanges.size() != 0)) {
			responseR.setTime
			    (limits.getStartTime() + limits.getDuration());
			responseR.setStatus(0);
			responseR.setInvert(true);
		    }
		}
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	/*
	System.err.println("StorageManager.beforeTimeRelative: " +
			   getFullName() + "/" + getClass() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Gets the index of the first of the added sets.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the index of the first added set.
     * @see #setAddedSets(int)
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/05/2001  INB	Created.
     *
     */
    final int getAddedSets() {
	return (addedSets);
    }

    /**
     * Gets the <code>Door</code> to this <code>FrameManager</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @see #setDoor(com.rbnb.api.Door)
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
    final Door getDoor() {
	return (door);
    }

    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
     * @since V2.0
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Our parent is now a <code>RingBuffer</code>, which has
     *			an <code>RBO</code> parent.
     * 11/19/2002  INB	Created.
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
     * Returns the current set.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the current set.
     * @see #setSet(com.rbnb.api.FrameManager)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final FrameManager getSet() {
	return (set);
    }

    /**
     * Gets the last registration time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last registration time.
     * @see #setLastRegistration(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final long getLastRegistration() {
	return (lastRegistration);
    }

    /**
     * Gets the maximum number of elements allowed in a set.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum number of elements per set.
     * @see #setMeps(int)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final int getMeps() {
	return (maxElementsPerSet);
    }

    /**
     * Gets the maximum amount of memory allowed in a set.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum amount of memory allowed in a set.
     * @see #setMmps(int)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final int getMmps() {
	return (maxMemoryPerSet);
    }

    /**
     * Gets the maximum number of sets.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum number of sets.
     * @see #setMs(int)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final int getMs() {
	return (maxSets);
    }

    /**
     * Gets the next set index.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the next index to use.
     * @see #setNextIndex(long)
     * @since V2.0
     * @version 12/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    final long getNextIndex() {
	return (nextIndex);
    }

    /**
     * Gets the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Registration</code>.
     * @see #setRegistered(com.rbnb.api.Registration)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    final Registration getRegistered() {
	return (registered);
    }

    /**
     * Gets the removed sets flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return have sets been removed?
     * @see #setRemovedSets(boolean)
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/05/2001  INB	Created.
     *
     */
    final boolean getRemovedSets() {
	return (removedSets);
    }

    /**
     * Matches the input request <code>Rmap</code> against this one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the request <code>Rmap</code>.
     * @param referenceI  the reference request.
     * @return the results of the match:
     *	       <p><ul>
     *	       <li><code>MATCH_EQUAL</code> - the two compare equal,</li>
     *	       <li><code>MATCH_SUBSET</code> - this <code>Rmap</code> is
     *		   contained in the request,</li>
     *	       <li><code>MATCH_SUPERSET</code> - this <code>Rmap</code>
     *		   contains the request,</li>
     *	       <li><code>MATCH_INTERSECTION</code> - this <code>Rmap</code> and
     *		   the request contain common regions,</li>
     *	       <li><code>MATCH_BEFORENAME</code> - this <code>Rmap</code> is
     *		   before the request based solely on name,</li>
     *	       <li><code>MATCH_BEFORE</code> - this <code>Rmap</code> is before
     *		   the request,</li>
     *	       <li><code>MATCH_AFTERNAME</code> - this <code>Rmap</code> is
     *		   after the request based solely on name, or</li>
     *	       <li><code>MATCH_AFTER</code> - this <code>Rmap</code> is after
     *		   the request, or</li>
     *	       <li><code>MATCH_NOINTERSECTION</code> - this <code>Rmap</code>
     *		   and the request have the same name and have overlapping
     *		   <code>TimeRanges</code>, but do not have any common
     *		   regions.</li>
     *	       </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 06/12/2001  INB	Created.
     *
     */
    final byte matches(Rmap requestI,DataRequest referenceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte matchesR = MATCH_NOINTERSECTION;
	boolean locked = false;
	try {
	    // Lock the door.
	    getDoor().lockRead("StorageManager.matches");
	    locked = true;

	    // Match the request.
	    matchesR = super.matches(requestI,referenceI);

	} finally {
	    // Unlock the door.
	    if (locked) {
		getDoor().unlockRead();
	    }
	}

	return (matchesR);
    }

    /**
     * Matches the contents of this <code>StorageManager</code> against a
     * <code>TimeRelativeRequest</code>.
     * <p>
     * This method performs the following steps:
     * <p><ol>
     *    <li>Compare the time reference against the limits of the data for
     *	      each of the channels in the <code>StorageManager</code>,</li>
     *    <li>If the relationship between the time reference and the limits is
     *	      different for different channels, then set the return response
     *	      status to -2 and we're done, </li>
     *    <li>If the relationship is always that the request is before the
     *	      data, then set the return response status to -1 and we're
     *	      done,</li>
     *    <li>If the relationship is always that the request is after the data,
     *	      then set the return response status to 1 and we're done,</li>
     *    <li>If the relationship is that the request appears to be within
     *	      limits, then start a binary search of the contents of the
     *	      <code>StorageManager</code>.</li>
     *    </ol>
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI     the <code>TimeRelativeRequest</code>.
     * @param roI	   the <code>RequestOptions</code>.
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
	/*
	System.err.println("StorageManager.matchTimeRelative: " +
			   getFullName() + "/" + getClass() +
			   "\nRegistration: " + getRegistered() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();

	boolean locked = false;
	try {
	    getDoor().lockRead("StorageManager.matchTimeRelative");
	    locked = true;

	    com.rbnb.utility.SortedVector toMatch = requestI.getByChannel();
	    java.util.Hashtable requests = new java.util.Hashtable();
	    TimeRelativeRequest request;
	    String channelName;
	    DataArray limits;
	    TimeRelativeChannel trc;
	    int direction;
	    int finalDirection = 2;
	    int idx;
	    FrameManager fm;

	    for (idx = 0;
		 (finalDirection != -2) && (idx < toMatch.size());
		 ++idx) {
		// For each channel in the input request, determine where its
		// reference is relative to the data available.
		trc = (TimeRelativeChannel) toMatch.elementAt(idx);
		channelName = trc.getChannelName().substring
		    (requestI.getNameOffset());
		limits = getRegistered().extract(channelName);
		if ((limits.timeRanges == null) ||
		    (limits.timeRanges.size() == 0)) {
		    continue;

		} else {
		    direction = requestI.compareToLimits(limits);
		    if (idx == 0) {
			// If this is the first channel, save the result.
			finalDirection = direction;

		    } else if (direction != finalDirection) {
			// If the direction changes, then we need to work on
			// the channels individually.
			finalDirection = -2;
		    }
		}
	    }

	    if (finalDirection == 2) {
		// If there is no data contained in here, then pretend the
		// request comes before the data.
		responseR.setStatus(-1);

	    } else if (finalDirection != 0) {
		// If we have gone too far or not far enough or need to split
		// the channels, return that status.
		responseR.setStatus(finalDirection);

	    } else {
		// If the request is for data that appears to be within this
		// <code>StorageManager</code>, then perform a binary search
		// for it.
		int lo = 0;
		int hi = getNchildren() - 1;
		int lastIdx = 0;
		int lastGoodStatus = Integer.MIN_VALUE;
		responseR.setStatus(-1);
		for (idx = (lo + hi)/2;
		     (responseR.getStatus() != -2) &&
			 (responseR.getStatus() != 0) &&
			 (lo <= hi);
		     idx = (lo + hi)/2) {
		    // Search each <code>FrameManager</code> for the data.
		    fm = (FrameManager) getChildAt(idx);
		    responseR = fm.matchTimeRelative(requestI,roI);
		    if ((responseR.getStatus() != -3) &&
			(responseR.getStatus() != 3)) {
			lastGoodStatus = responseR.getStatus();
		    }
		    lastIdx = idx;

		    switch (responseR.getStatus()) {
		    case -2:
			// Different channels have different time bases.  We'll
			// need to split this requesst into per channel
			// requests.
			break;

		    case -1:
		    case -3:
			// The request is for data before the
			// <code>FrameManager</code>.
			hi = idx - 1;
			break;

		    case 0:
			// The request is for data within the
			// <code>FrameManager</code> - we're done.
			break;

		    case 1:
		    case 3:
			// The request is for data after the
			// <code>FrameManager</code>.
			lo = idx + 1;
			break;
		    }
		}

		if (((responseR.getStatus() == -1) && (lastIdx > 0)) ||
		    ((responseR.getStatus() == 1) &&
		     (lastIdx < getNchildren() - 1))) {
		    if (responseR.getStatus() == 1) {
			// Always set the index to the second
			// <code>FrameManager</code>.
			++lastIdx;
		    }
		    // If we discover that the request is between two
		    // <code>FrameManagers</code>, then we can figure out the
		    // time from one of the two.
		    switch (requestI.getRelationship()) {
		    case TimeRelativeRequest.BEFORE:
		    case TimeRelativeRequest.AT_OR_BEFORE:
			// For requests before the time reference, use the
			// end of the previous <code>FrameManager</code>.
			fm = (FrameManager) getChildAt(lastIdx - 1);
			responseR = fm.beforeTimeRelative(requestI,roI);
			if ((responseR.getStatus() == -3) ||
			    (responseR.getStatus() == 3)) {
			    responseR.setStatus(-1);
			}
			break;

		    case TimeRelativeRequest.AT_OR_AFTER:
		    case TimeRelativeRequest.AFTER:
			// For requests before the time reference, use the
			// start of the current <code>FrameManager</code>.
			fm = (FrameManager) getChildAt(lastIdx);
			responseR = fm.afterTimeRelative(requestI,roI);
			if ((responseR.getStatus() == -3) ||
			    (responseR.getStatus() == 3)) {
			    responseR.setStatus(1);
			}
			break;
		    }
		}

		if ((responseR.getStatus() == -3) ||
		    (responseR.getStatus() == 3)) {
		    if (lastGoodStatus != Integer.MIN_VALUE) {
			responseR.setStatus(lastGoodStatus);
		    }
		}
	    }
	    
	} finally {
	    if (locked) {
		getDoor().unlockRead();
	    }
	}

	/*
	System.err.println("StorageManager.matchTimeRelative: " + 
			   getFullName() + "/" + getClass() +
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
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 06/12/2001  INB	Created.
     *
     */
    byte moveDownFrom(RmapExtractor extractorI,
		      ExtractedChain unsatisfiedI,
		      java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = MATCH_UNKNOWN;
	boolean locked = false;
	try {
	    // Lock the door.

	    /*
	    System.err.println(Thread.currentThread() + " entering " +
			       getParent().getParent().getName() + "/RB" +
			       ((RingBuffer) getParent()).getIndex() + "/" +
			       getClass() +
			       " moveDownFrom read lock.");
	    */

	    getDoor().lockRead("StorageManager.moveDownFrom");
	    locked = true;

	    // Match against the children of this <code>FrameManager</code>.
	    reasonR = super.moveDownFrom(extractorI,unsatisfiedI,unsatisfiedO);

	} finally {
	    // Unlock the door.
	    if (locked) {
		getDoor().unlockRead();
	    }

	    /*
	    System.err.println(Thread.currentThread() + " exited " +
			       getParent().getParent().getName() + "/RB" +
			       ((RingBuffer) getParent()).getIndex() + "/" +
			       getClass() +
			       " moveDownFrom read lock.");
	    */
	}
	
	return (reasonR);
    }

    /**
     * Creates a new instance of the same class as this
     * <code>StorageManager</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @since V2.0
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Made this method final.
     * 08/02/2001  INB	Created.
     *
     */
    final Rmap newInstance() {
	return (new Rmap());
    }

    /**
     * Nullifies this <code>StorageManager</code>.
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
    public void nullify() {
	super.nullify();

	try {
	    if (getDoor() != null) {
		getDoor().nullify();
		setDoor(null);
	    }

	    if (getSet() != null) {
		getSet().nullify();
		setSet(null);
	    }

	    if (getRegistered() != null) {
		getRegistered().nullify();
		setRegistered(null);
	    }

	} catch (java.lang.Throwable e) {
	}
    }

    /**
     * Sets the index of the first of the added sets.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addedSetsI the index of the first added set.
     * @see #getAddedSets()
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/05/2001  INB	Created.
     *
     */
    final void setAddedSets(int addedSetsI) {
	addedSets = addedSetsI;
    }

    /**
     * Sets the <code>Door</code> to this <code>FrameManager</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doorI  the <code>Door</code>.
     * @see #getDoor()
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
    private final void setDoor(Door doorI) {
	door = doorI;
    }

    /**
     * Sets the last registration time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI  the new registration time.
     * @see #getLastRegistration()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/22/2001  INB	Created.
     *
     */
    final void setLastRegistration(long timeI) {
	lastRegistration = timeI;
    }

    /**
     * Sets the maximum number of elements per set.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maxElementsPerSet  the maximum number of elements per set.
     * @see #getMeps()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final void setMeps(int maxElementsPerSetI) {
	maxElementsPerSet = maxElementsPerSetI;
    }

    /**
     * Sets the maximum amount of memory per set.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maxMemoryPerSetI  the maximum amount of memory per set.
     * @see #getMmps()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final void setMmps(int maxMemoryPerSetI) {
	maxMemoryPerSet = maxMemoryPerSetI;
    }

    /**
     * Sets the maximum number of sets.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maxSetsI  the maximum number of sets.
     * @see #getMs()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final void setMs(int maxSetsI) {
	maxSets = maxSetsI;
    }

    /**
     * Sets the next set index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nextIndexI the next index to use.
     * @see #getNextIndex()
     * @since V2.0
     * @version 12/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/14/2001  INB	Created.
     *
     */
    final void setNextIndex(long nextIndexI) {
	nextIndex = nextIndexI;
    }

    /**
     * Sets the <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param registeredI  the new <code>Registration</code>.
     * @see #getRegistered()
     * @since V2.0
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001 INB	Created.
     *
     */
    final void setRegistered(Registration registeredI) {
	registered = registeredI;
	registered.setParent(this);
    }

    /**
     * Sets the removed sets flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param removedSetsI have there been any removed sets?
     * @see #getRemovedSets()
     * @since V2.0
     * @version 06/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/05/2001  INB	Created.
     *
     */
    final void setRemovedSets(boolean removedSetsI) {
	removedSets = removedSetsI;
    }

    /**
     * Sets the current set.
     * <p>
     *
     * @author Ian Brown
     *
     * @param setI  the new set.
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
     * @see #getSet()
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/12/2001  INB	Created.
     *
     */
    void setSet(FrameManager setI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	set = setI;
	if (set != null) {
	    addChild(set);
	    if (getAddedSets() == -1) {
		setAddedSets(getNchildren() - 1);
	    }
	}
	trim();
    }

    /**
     * Trims the <code>StorageManager</code> object back to the specified size
     * by deleting the oldest sets out of it.
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getSet()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/11/2006  EMF  Added trimByTime code.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 07/30/2003  INB	Nullify the <code>FrameManager</code> if it doesn't
     *			belong to anyone after being removed.
     * 05/06/2003  INB	Only set us as parent for <code>FileSets</code>.
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/14/2001  INB	Created.
     *
     */
    final void trim()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int empties =
	    (getSet() == null) ? 0 :
	    (getSet().getNchildren() > 1) ? 0 :
	    1;

        if (getNchildren()<2) return; //nothing to trim...

        //EMF 10/3/06: trim by time, not frame count
        if (closeByTime) {
          double duration=0;
          FrameManager fm = null;
          Registration reg = null;
          TimeRange tr = null;
          try {
            for (int i=getNchildren()-1;i>=0;i--) {
              fm=(FrameManager)getChildAt(i);
              reg=fm.getSummary();
              if (reg==null || (tr=reg.getTrange())==null) {
                //System.err.println("StorageManager.trim: tr null!!");
                return;
              }
              //System.err.println("ichild: "+i+", set duration: "+duration+", trimInterval: "+trimInterval);
              if (duration>trimInterval) { //need to trim
                for (int j=0;j<i;j++) removeSet();
                break;
              }
              if (tr.getDuration()>0) duration+=tr.getDuration();
              else duration+=flushInterval/1000.; //gotta do something... (MJM 6/26/07:  added /1000 !!)
            }
            //System.err.println("duration "+duration);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else { //old trim by frame count code below
	  while (((getMs() == 0) &&
	     ((getNchildren() - empties) > 1)) ||
	    ((getMs() > 0) &&
	     ((getNchildren() - empties) > getMs()))) {
	      // If the maximum number of sets is exceeded, then delete the
	      // oldest one.
              removeSet();
	  } //end while
        } //end else
    }

    //EMF: removeSet created since called from two places above
    private void removeSet()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	    FrameManager set = (FrameManager) getChildAt(0);
//if (set instanceof FrameSet) System.err.println("FrameSet");
//if (set instanceof FileSet) System.err.println("removing FileSet");
	    boolean isMine = (set.getParent() == this);

	    // Remove the set.
	    removeChildAt(0);
	    removedSets = true;
	    if (this instanceof Archive) {
		((Archive) this).setOldest
		    (((FileSet) getChildAt(0)).getIndex());
	    }

	    try {
		// Lock the set.
//		set.getDoor().lock("StorageManager.trim"); // MJM set.clear() locks it anyways

		// Delete the set.
		if (set instanceof FileSet) {
		    set.setParent(this);
		}
		set.clear();

		if (set instanceof FileSet) {
		    set.setParent(null);
		}

	    } finally {
		// Unlock the <code>FileSet</code>.
//		set.getDoor().unlock();		// MJM
	    }

	    if (isMine && (set.getParent() == null)) {
		set.nullify();
	    }
    } //end method removeSet


    /**
     * Updates the <code>Registration</code> for the
     * <code>StorageManager</code>.
     * <p>
     * The <code>Registration</code> is updated from the
     * <code>Registrations</code> for the <code>FrameSets</code> in the
     * <code>StorageManager</code>.
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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/12/2001  INB	Created.
     *
     */
    final int updateRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int changedR = 0;

	try {
	    getDoor().lock("StorageManager.updateRegistration");

	    boolean rSets;
	    int aSets;
	    rSets = getRemovedSets();
	    setRemovedSets(false);
	    aSets = getAddedSets();
	    setAddedSets(-1);
	    boolean reset = rSets || (aSets == 0);
	    int startAt = rSets ? 0 : aSets;

	    // If there is no registration or it is definitely out of date,
	    // create one.
	    if ((getRegistered() == null) || (startAt == 0)) {
		changedR = -1;
		setRegistered(new Registration());
	    }

	    if (startAt >= 0) {
		// If one or more sets have been added or removed, then update
		// the <code>Registration</code>.

		for (int idx = startAt,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    // Loop through the sets that need to processed to update
		    // the <code>Registration</code>.
		    FrameManager set = (FrameManager) getChildAt(idx);

		    // Ensure that the <code>FrameManager's Registration</code>
		    // is up-to-date.
		    if (set.updateRegistration() ||
			(set.getLastRegistration() > getLastRegistration()) ||
			(changedR == -1)) {
			if (getRegistered().updateRegistration
			    (set.getRegistered(),
			     reset,
			     false) &&
			    (changedR == 0)) {
			    changedR = 1;
			}
		    }

		    // After the first <code>FrameManager</code>, the
		    // <code>TimeRanges</code> in the <code>Registration</code>
		    // are OK.
		    reset = false;
		}
	    }

	    if (changedR != 0) {
		// Update the last registration time.
		setLastRegistration(System.currentTimeMillis());
	    }
	} finally {
	    getDoor().unlock();
	}

	return (changedR);
    }
}
