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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * RBNB Ring Buffer Object (RBO) class.
 * <p>
 * An <code>RBO</code> is a client handler that accepts frames of data from a
 * data source client.  The data is placed into a <code>RingBuffer</code> for
 * storage and retrieval by sink clients via their <code>NBOs</code>.
 * <p>
 * The process of adding a frame of data is described in
 * <code>RBO.addChild</code>.  Registration of channels is described in
 * <code>RBO.updateRegistration</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see #addChild(com.rbnb.api.Rmap childI)
 * @since V2.0
 * @version 06/22/2006
 */

/*
 * Copyright 2001 - 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/03/2013  MJM  	deleteArchive: try-again if file delete fails 
 * 03/15/2011  MJM  	Allow reconnect to replace old source in special case of "dot" .fileName
 * 01/14/2011  MJM  	Added lock release on prolonged blocking waits
 * 10/18/2010  MJM		Explicit buffer size in BufferedReader
 * 03/19/2010  MJM      Added logic to conservatively add FileSets for reattaching sources
 * 08/12/2008  WHF      Implemented failSafeMode.
 * 03/26/2008  WHF      Added check for incoming frame at time of termination to
 *                      avoid deadlock in later requests.
 * 01/05/2007  EMF      Avoid infinite loop on archive recovery
 * 11/16/2006  MJM      Fall back to old framesets=sqrt(cachesize) logic by default
 * 11/16/2006  EMF      Fixed deep recovery of archives.
 * 08/03/2006  MJM      Improved robustness of Archive recovery.
 * 08/02/2006  MJM      Trim number of filesets if archiveSize does not require that much data
 * 07/27/2006  EMF      In setUpCache(), set the default number of framesets to 1.
 * 06/22/2006  JPW	In getArchiveDirectory(), use
 *			RBNB.getArchiveHomeDirectory().
 * 03/29/2006  JPW      In setUpCache(), set the default number of framesets
 *                      to 2.  If user has defined "-Dnumframesets=0" on
 *                      the RBNB Server command line, then we resort back to
 *                      the original calculation of the number of framesets
 *                      as the square root of the number of cache frames.
 * 03/24/2006  JPW      In setUpCache(), user can set the number of framesets
 *                      using a system property:
 *                      "-Dnumframesets=<integer number>"
 * 10/07/2005  MJM      In setUpArchive(), firewalled zero sized frameset/set
 * 10/03/2005  JPW	Bug fix in acceptFrame():
 *			Move the frame index increment to **AFTER** the
 *			frame has been succesfully added to the RingBuffer.
 *			If time goes backward and an IllegalStateException is
 *			thrown in addChild() (and thus the frame wasn't added
 *			to the RingBuffer) then the frame index won't be
 *			incremented. This fixes a bug where frame-based
 *			Subscriptions to this Source will have an offset
 *			between the frame they are currently at and the latest
 *			frame in the RBO.
 * 03/31/2005  MJM      Fixed server side auto timestamps
 * 12/21/2004  MJM      Added null pointer check
 * 08/31/2004  INB	Added registration documentation.
 * 08/26/2004  MJM      Additional update explicit registration (could get
 *			lost).
 * 08/05/2004  INB	Added documentation.
 * 07/30/2004  INB	Added documentation of how data is accepted from a
 *			source in <code>addChild</code>.
 * 07/14/2004  EMF	Changed 7/13 fix to use built-in String method.
 * 07/13/2004  INB	Fixed a couple of name problems in moveDownFromStandard
 *			that could cause errors or match failures.
 * 07/07/2004  INB	Delete channels from the explicitly registered Rmaps
 *			as well as from the ring buffer data Rmaps.
 * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 02/03/2004  INB	Handle <code>MATCH_LOST_PATH</code>.
 * 01/16/2004  INB	Added flag indicating that we've been shutdown.  Use
 *			it to avoid getting into trouble when doing requests.
 *			Also ensure that we add the metrics into our parent
 *			before we completely shutdown.  Ensure that we're
 *			synchronized when calculating data sizes for metrics.
 * 01/14/2004  INB	Update the metrics bytes when the <code>RCO</code>
 *			is shut down on a detach.  Also, add its bytes rather
 *			than simply saving them.  Synchronize while updating
 *			the <code>metricsBytes</code>.
 * 01/09/2004  INB	Added <code>flush</code> method.  Added code to ensure
 *			that the <code>Archive</code> has been written out
 *			when a detach occurs.
 * 01/08/2004  INB	Fixed <code>deleteArchive</code> to delete as much as
 *			it can.  Also, do delete after eliminating children.
 *			Added clear cache methods.
 * 01/05/2004  INB	Allow for no children or null child in
 *			<code>readFromArchive</code>.
 * 12/22/2003  INB	Allow for closes happening during request operations.
 * 12/08/2003  INB	Handle <code>RequestOptions.extendStart</code> in
 *			<code>extractTimeRelative</code>.
 * 11/17/2003  INB	Use <code>try/finally</code> to ensure that we don't
 *			leave flags set on an error.
 *			Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/14/2003  INB	Ensure that we release <code>Locks</code>.  Ensure that
 *			the next frame index is accurate.
 * 11/12/2003  INB	Added is in startup handling.  Added <code>Door</code>
 *			identification and locations.
 * 11/11/2003  INB	Lock the <code>RBO</code> when shutting down.
 * 10/31/2003  INB	Added message buffers to hold the messages produced
 *			by the recovery sequence.
 * 10/29/2003  INB	Change for changes before locking in
 *			<code>updateRegistration</code>.
 * 10/27/2003  INB	Lock things for read when moving down.  Added write
 *			lock methods.  Ensure that we close all files.
 * 10/15/2003  INB	Fixed a timing issue in
 *			<code>updateRegistration</code>.
 * 10/13/2003  INB	Ensure that the <code>reconnect</code> method updates
 *			the <code>Cache</code> and <code>Archive</code> sizes.
 * 10/10/2003  INB	Added <code>matchTimeRelative</code>.
 * 09/10/2003  INB	Allow for <code>null</code> frame ranges when
 *			trying to calculate the next frame index.
 * 09/09/2003  INB	Create request vector and use it to build request
 *			array in <code>moveDownFromStandard</code>.
 *			Reordered the archive writes so that data is always
 *			written before the headers.
 *			Added attempts to validate ring buffers that appear
 *			invalid at first glance.
 * 09/08/2003  INB	Set reconnecting flag when the wait for reconnect
 *			loop exits.  This ensures a full shutdown even if the
 *			<code>RBO</code> is terminated by rbnbAdmin without
 *			an application ever really reconnecting.
 * 07/30/2003  INB	Added <code>deleteChannels</code> method.  This is
 *			done by deleting <code>RingBuffers</code>, which means
 *			that code that looks for them in the <code>RBO</code>
 *			needs to do a search ratehr than a straight index.
 *			Added <code>findRingBuffer</code> method.
 * 05/29/2003  INB	Ensure that the next <code>RingBuffer</code> index
 *			is set properly when the <code>Archive</code> is read.
 * 05/22/2003  INB	Added code to check for <code>RBO</code> termination
 *			during calculation of data sizes.
 * 05/19/2003  INB	Use largest cache/archive sizes rather than last.
 * 05/12/2003  INB	Replaced synchronization with a registration door
 *			lock in <code>setUpRingBuffer</code>.
 * 05/02/2003  INB	Worked on the <code>shutdown</code> method to ensure
 *			that it is less sensitive to errors.
 * 04/30/2003  INB	Flag ring buffers and filesets as RBNB files when
 *			validating. Also, for append mode, create a new archive
 *			if the old one fails to load.
 * 04/29/2003  INB	Added additional <code>Error</code> handling.
 * 04/24/2003  INB	Added <code>markOutOfDate</code>.
 * 04/17/2003  INB	Allow clients to try to reconnect to existing handlers.
 *			Write the <code>Username</code> to the archive files on
 *			output, read it and check it on input.
 * 04/07/2003  INB	Added <code>stopOnIOException</code>.
 * 04/04/2003  INB	Handle Java errors.
 * 04/03/2003  INB	Ensure that the explicit registration is added in.
 * 04/02/2003  INB	Don't log channels if the <code>Log</code> is display
 *			only. Added recovery of V2.0 archives.
 * 03/28/2003  INB	Don't set adding a frame flag unnecessarily.
 * 03/27/2003  INB	Added check for no channels in
 *			<code>acceptFrame</code>. Eliminated unnecessary locks.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/21/2003  INB	Log the <code>addChild</code> call.
 * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
 * 03/14/2003  INB	Made <code>updateRegistration</code> final.
 *			<code>logStatus</code> uses <code>StringBuffer</code>.
 * 02/17/2003  INB	Replaced the single <code>Cache</code> and
 *			<code>Archive</code> with a set of zero or more
 *			<code>RingBuffer</code> objects.
 * 05/10/2001  INB	Created.
 *
 */
class RBO
    extends com.rbnb.api.RBNBClient
    implements com.rbnb.api.SourceHandler
{
//EMF 1/5/07
private boolean alreadyReset=false;

    /**
     * If true, an archive which fails to load for append will be moved and a
     *  new archive created.
     * <p>
     *
     * @author WHF
     *
     * @since V3.1
     * @version 2008/08/12
     */
    private final boolean failSafeMode;


    /**
     * accepting a frame?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private boolean acceptingAFrame = false;

    /**
     * the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Source#ACCESS_APPEND
     * @see com.rbnb.api.Source#ACCESS_CREATE
     * @see com.rbnb.api.Source#ACCESS_LOAD
     * @see com.rbnb.api.Source#ACCESS_NONE
     * @since V2.0
     * @version 05/10/2001
     */
    private byte accessMode = Source.ACCESS_NONE;

    /**
     * adding a frame?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private boolean addingAFrame = false;

    /**
     * the desired number of archive frames allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long archiveFrames = 0;

    /**
     * seconds between archive flushes
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/18/2006
     */
    private float archiveflush = 0;

    /**
     * cumulative seconds in archive
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/18/2006
     */
    private float archivetrim = 0;

    /**
     * archive <code>FileSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    int aFileSets = 0;

    /**
     * archive <code>FrameSets</code> per <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    int aFrSFileSet = 0;

    /**
     * keep the archive?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/08/2002
     */
    private boolean archiveKeep = true;

    /**
     * the desired size of the archive in bytes.
     * <p>
     * A value of -1 means that the archive is not limited by memory use.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long archiveSize = 0;

    /**
     * list of objects awaiting update notification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2001
     */
    private java.util.Vector awaiting = new java.util.Vector();

    /**
     * the desired number of cache frames allowed.
     * <p>
     * A value of -1 means that the cache is not limited to a particular number
     * of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long cacheFrames = -1;

    /**
     * seconds between cache flushes
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/18/2006
     */
    private float cacheflush = 0;

    /**
     * cumulative seconds in cache
     * <p>
     *
     * @author Eric Friets
     *
     * @since V2.7
     * @version 10/18/2006
     */
    private float cachetrim = 0;

    /**
     * cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    int cFrameSets = 1;  // was 10, mjm 8/03/2006

    /**
     * cache frames per <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    int cFrFrameSet = 1;

    /**
     * keep the cache?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/08/2002
     */
    private boolean cacheKeep = false;

    /**
     * the desired size of the cache in bytes.
     * <p>
     * A value of -1 means that the cache is not limited by memory use.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long cacheSize = -1;

    /**
     * channel names to <code>RingBuffer</code> indexes mappings.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/14/2003
     */
    private java.util.Hashtable channelToRB = new java.util.Hashtable(10000,
								      .5F);

    /**
     * channel deletion code strings.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/30/2003
     */
    private final static String[] DELETE_CODES = {
	"SUCCESS: Channel deleted.",
	"SUCCESS: Channel did not exist.",
	"FAILURE: There are additional channels in the ring buffer."
    };

    /**
     * the last frame received.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Rmap frame = null;

    /**
     * the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private int frameSets = 10;

    /**
     * has this <code>RBO</code> been shutdown?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/15/2004
     */
    private boolean hasBeenShutdown = false;

    /**
     * the last registration time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long lastRegistration = 0;

    /**
     * next <code>Rmap</code> frame index.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long nFindex = 1;

    /**
     * next <code>RingBuffer</code> index.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/17/2003
     */
    private long nRBindex = 1;

    /**
     * is <code>RBO</code> known to be out-of-date?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */
    private boolean outOfDate = true;

    /**
     * perform a clear cache?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/08/2004
     */
    private boolean performClearCache = false;

    /**
     * perform a reset?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/12/2002
     */
    private boolean performReset = false;

    /**
     * list of the <code>RingBuffers</code> that have changed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/14/2003
     */
    java.util.Hashtable rbsChanged = new java.util.Hashtable(10000,.5F);

    /**
     * list of the <code>RingBuffers</code> that need to be synchronized.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/14/2003
     */
    java.util.Hashtable rbsToSync = new java.util.Hashtable(10000,.5F);

    /**
     * reconnecting?
     * <p>
     * An <code>RBO</code> can be reconnected to by a new <code>RCO</code> if
     * desired.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/17/2003
     */
    private boolean reconnecting = false;

    /**
     * the registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Registration registered = null;

    /**
     * explicitly registered information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/04/2002
     */
    private Rmap registeredExplicitly = null;
    
    /**
     * registration door.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/14/2001
     */
    private Door registrationDoor = null;
    
    /**
      * If set, determines the value of getArchiveDirectory().
      * @author WHF
      * @since 2008/01/31
      */
    private String archiveDirectoryOverride = null;

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
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2001  INB	Created.
     *
     */
    RBO()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	registrationDoor = new Door(Door.READ_WRITE);

	boolean tempFailSafe = true;	
	try {
	    tempFailSafe = !Boolean.getBoolean("com.rbnb.api.RBO.noFailSafe");
	} catch (Throwable t) {	}
	finally {
	    failSafeMode = tempFailSafe;
	}
    }

    /**
     * Class constructor to build an <code>RBO</code> for an
     * <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    RBO(RCO rcoI)
	throws java.lang.InterruptedException
    {
	super(rcoI);
	registrationDoor = new Door(Door.READ_WRITE);
	
	boolean tempFailSafe = true;	
	try {
	    tempFailSafe = !Boolean.getBoolean("com.rbnb.api.RBO.noFailSafe");
	} catch (Throwable t) {	}
	finally {
	    failSafeMode = tempFailSafe;
	}
    }

    /**
     * Class constructor to build a <code>RBO</code> for a
     * particular <code>RCO</code> from a <code>SourceInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI    the <code>RCO</code>.
     * @param sourceI the <code>SourceInterface</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    RBO(RCO rcoI,SourceInterface sourceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(rcoI);
	update(sourceI);
    }

    /**
     * Accepts a new frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param waitI  wait for a frame to arrive?
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
     * @since V2.0
     * @version 10/03/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/03/2005  JPW	Move the frame index increment to **AFTER** the
     *			frame has been succesfully added to the RingBuffer.
     *			If time goes backward and an IllegalStateException is
     *			thrown in addChild() (and thus the frame wasn't added
     *			to the RingBuffer) then the frame index won't be
     *			incremented. This fixes a bug where frame-based
     *			Subscriptions to this Source will have an offset
     *			between the frame they are currently at and the latest
     *			frame in the RBO.
     * 01/08/2004  INB	Added clear cache handling.
     * 11/17/2003  INB	Use <code>try/finally</code> to ensure that we don't
     *			leave the flag set.
     * 07/31/2003  INB	Use <code>findRingBuffer</code> method.
     * 03/27/2003  INB	Added check for no channels in child. Eliminate locks.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     *			In particular, this code now determines which one
     *			the frame belongs in and adds it to that one.
     * 01/02/2001  INB	Created.
     *
     */
    final void acceptFrame(boolean waitI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap theFrame = null;

	// Look for a frame to process.
	try {
	    synchronized (this) {
		while ((getFrame() == null) &&
		       waitI &&
		       !getTerminateRequested() &&
		       !getPerformReset() &&
		       !getPerformClearCache()) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}
		
		// 2008/03/26  WHF  Added warning; clear frame when terminating
		//   so requests of detached sources do not get stuck in the
		//   moveDownFrom() method.
		if (getFrame() != null && getTerminateRequested()) {
		    System.err.println("WARNING: Unprocessed frame "+getFrame()
			    +" in server at time of termination; frame lost.");
		    setFrame(null);
		}

		if (!getTerminateRequested() &&
		    !getPerformClearCache() &&
		    !getPerformReset() &&
		    ((theFrame = getFrame()) != null)) {
		    // If we get a frame, grab it.
		    setAcceptingAFrame(true);
		    setFrame(null);
		}
	    }

	    if (!getTerminateRequested() &&
		!getPerformReset() &&
		getAcceptingAFrame()) {
		setAmNew(false);

		// Set the frame index for the frame.
		//  System.err.println("theFrame: " + theFrame);
		//  System.err.println("Trange: " +
		//      theFrame.getChildAt(0).getTrange());
		theFrame.setFrange(new TimeRange(getNfindex(),0.));
		
		/*
		if ((theFrame.getTrange() != null) &&
      		    (theFrame.getTrange().compareTo
		     (TimeRange.SERVER_TOD ) == 0)) {
		     theFrame.setTrange
		     (new TimeRange(System.currentTimeMillis()/1000.,0.));
		*/	
		// mjm SERVER_TOD timestamp is actually one level down
		if ((theFrame.getNchildren() > 0) &&
		    (theFrame.getChildAt(0).getTrange() != null) &&
		    (theFrame.getChildAt(0).getTrange().compareTo
		     (TimeRange.SERVER_TOD ) == 0)) {
		    theFrame.getChildAt(0).setTrange
			(new TimeRange(System.currentTimeMillis()/1000.,0.));
		}
		
		// Increment the frame index
		// JPW 10/03/2005: Move increment until after the frame has
		//                 been successfully added to the RingBuffer
		//                 (see below).
		// setNfindex(getNfindex() + 1);
		
		// Find the <code>RingBuffer</code> to add to.
		RingBuffer rb = null;
		String[] channelNames = null;
		Long indexL = null;
		boolean badChannels = false;
		if (this instanceof NBO) {
		    if (getNchildren() > 0) {
			rb = (RingBuffer) getChildAt(0);
		    }

		} else {
		    channelNames = theFrame.extractNames();
		    if (!(badChannels = (channelNames.length == 0))) {
			indexL = (Long) channelToRB.get(channelNames[0]);
			if (indexL != null) {
			    long rbIndex = indexL.longValue();
			    rb = findRingBuffer(rbIndex);
			}
		    }
		}

		if (!badChannels && (rb == null)) {
		    rb = new RingBuffer(getNrbindex());
		    if (channelNames != null) {
			indexL = new Long(rb.getIndex());
			for (int idx = 0; idx < channelNames.length; ++idx) {
			    channelToRB.put(channelNames[idx],indexL);
			}
		    }
		    // JPW 09/30/2005: I wonder if the RingBuffer index should
		    //                 be incremented *after* the new child has
		    //                 been successfully added to the RBO? In
		    //                 other words, I wonder if the order of
		    //                 the following 2 lines should be changed?
		    //                 With the order the way they currently
		    //                 are, what happens if the ring buffer
		    //                 index is incremented and then the
		    //                 RingBuffer is not successfully added
		    //                 to the RBO?  Might this cause a problem?
		    setNrbindex(rb.getIndex() + 1);
		    addChildAt(rb,getNchildren());
                    //EMF 10/18/06: add trim by time info
                    rb.setupTrimTimes(cacheflush,cachetrim,archiveflush,archivetrim);
		    rb.setup(cFrameSets,cFrFrameSet,aFileSets,aFrSFileSet);
		    rb.start();
		}

		// Add the frame to the <code>RingBuffer</code>.
		if (rb != null) {
		    rbsToSync.put(rb,rb);
		    // JPW 09/30/2005: RingBuffer.addChild() now throws an
		    //                 IllegalStateException if the frame
		    //                 cannot be added to the RingBuffer
		    //                 (for example, for backward going time)
		    //                 Increment the frame index **AFTER**
		    //                 the successful addition of this
		    //                 current frame.
		    try {
			rb.addChild(theFrame);
			// Move this increment from earlier in the method
			// to after the successful return from addChild();
			// if an IllegalStateException occurs, this won't get
			// incremented.
			setNfindex(getNfindex() + 1);
		    } catch (java.lang.IllegalStateException ise) {
			// Nothing to do
		    }
		}
	    }

	} finally {
	    // We're done accepting the frame, just need to perform special
	    // handling.
	    setAcceptingAFrame(false);
	}
    }

    /**
     * Adds a child <code>Rmap</code>.
     * <p>
     * This method starts the process of adding new data to the RBNB system
     * after the RCO associated with this RBO determines that it has received
     * new data.  What follows is an overview of how data is received by the
     * RBNB server and where it goes.
     * <p>
     * Data is stored in a ring buffer, a virtual structure that acts like a
     * snake eating its own tail.  As data is added, it fills up the ring
     * buffer.  Once the ring buffer is full, the oldest data is removed to
     * make room for new data.  The size of the ring buffer determines how much
     * historical data is available.
     * <p>
     * Data in the RBNB system is represented by a recursive, hierarchical
     * object termed an "Rmap" (RBNB map).  Rmaps are explained in
     * <code>Rmap</code>.  The source application sends its data to the server
     * as a series of Rmaps, each of which is termed a "frame".  A frame is a
     * self-contained Rmap hierarchy containing data for one or more
     * "channels".  A channel is defined as a path through the hierarchy for
     * which all three primary values have been defined, namely:
     * <p><ol>
     *    <li>A name, consisting of the appended names of the Rmaps in the
     *	      path,</li>
     *    <li>A time, that of the lowest Rmap in the path with a specified time
     *	      value, and</li>
     *    <li>Data.</li>
     * </ol><p>
     * <p>
     * The structure used by the RBNB system to store these frames is actually
     * a hierarchy of one or more ring buffers, rooted at a "ring buffer
     * object" or RBO as shown below:
     * <p><dir>
     *    <li>RBO<dir>
     *        <li>RingBuffer(s)<dir>
     *            <li>Cache<dir>
     *                <li>FrameSet(s)<dir>
     *                    <li>Frame(s)</li>
     *                </dir></li>
     *            </dir></li>
     *            <li>Archive<dir>
     *                <li>FileSet(s)<dir>
     *                    <liFrameSet(s) [...]</li>
     *                </dir></li>
     *            </dir></li>
     *        </dir></li>
     *    </dir></li>
     * </dir><p>
     * Frames are added to the RBNB system as follows:
     * <p><ol>
     *    <li>The frame is received by the thread running the "RBNB control
     *	      object" or RCO, which determines that it has received a frame of
     *	      data and passes it off to its associated RBO
     *	      (<code>RCO.process</code> via <code>RBO.addChild</code>),</li>
     *    <li>The frame is picked up by a thread running the RBO and is
     *	      examined to determine which ring buffer it should be added to
     *	      (this may require a new ring buffer)
     *	      (<code>RBO.acceptFrame</code> to
     *	      <code>RingBuffer.addChild</code>),</li>
     *    <li>The frame is added to the cache belonging to the ring buffer
     *	      (<code>RingBuffer.acceptFrame</code> to
     *	      <code>StorageManager.addElement</code>),</li>
     *    <li>If there is no current frameset in the cache, then one is
     *	      created.  If the cache has reached its configured limit of
     *	      framesets, then the oldest frameset is removed
     *	      <code>StorageManager.setSet</code>,
     *	      <code>StorageManager.trim</code>, and
     *	      <code>FrameSet.clear</code>),</li>
     *    <li>The frame is appended to the current frameset of the cache
     *	      (<code>FrameManager.addElement</code> to
     *	      <code>FrameSet.storeElement</code>),</li>
     *    <li>The cache then determines if the current frameset has reached the
     *	      configured limit (currently a specific number of frames).  If so,
     *	      the frameset is closed and added to the archive
     *	      <code>FrameSet.close</code>) and is no longer considered
     *	      current,</li>
     *    <li>If there is no current fileset in the archive, then one is
     *	      created.  If the archive has reached its configured limit of
     *	      filesets, then the oldest fileset is removed
     *	      <code>StorageManager.setSet</code>,
     *	      <code>StorageManager.trim</code>, and
     *	      <code>FileSet.clear</code>).  The fileset's disk files are
     *	      deleted (<code>FileSet.deleteFromArchive</code>),</li>
     *    <li>The frameset is appended to the current fileset of the archive
     *	      (<code>FrameManager.addElement</code> to
     *	      <code>FileSet.storeElement</code>).  The frameset is written to
     *	      the disk files associated with the fileset
     *	      <code>FrameSet.writeToArchive</code>), and</li>
     *    <li>The archive then determines if the current fileset has reached
     *	      the configured limit (currently a specific number of framesets).
     *	      If so, the fileset is closed and is written to the disk files
     *	      <code>FileSet.close</code>) and is no longer considered
     *	      current.</li>
     * </ol><p>
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #acceptFrame(boolean waitI)
     * @see com.rbnb.api.FileSet#clear()
     * @see com.rbnb.api.FileSet#close()
     * @see com.rbnb.api.FileSet#deleteFromArchive()
     * @see com.rbnb.api.FileSet#storeElement(com.rbnb.api.Rmap elementI)
     * @see com.rbnb.api.FileSet#writeToArchive()
     * @see com.rbnb.api.FrameManager#addElement(com.rbnb.api.Rmap elementI)
     * @see com.rbnb.api.FrameSet#clear()
     * @see com.rbnb.api.FrameSet#close()
     * @see com.rbnb.api.FrameSet#storeElement(com.rbnb.api.Rmap elementI)
     * @see com.rbnb.api.FrameSet#writeToArchive()
     * @see com.rbnb.api.RCO#process(com.rbnb.api.Serializable messageI)
     * @see com.rbnb.api.RingBuffer#acceptFrame(com.rbnb.api.Rmap frameI)
     * @see com.rbnb.api.RingBuffer#addChild(com.rbnb.api.Rmap childI)
     * @see com.rbnb.api.Rmap
     * @see com.rbnb.api.StorageManager#addElement(com.rbnb.api.Rmap elementI)
     * @see com.rbnb.api.StorageManager#setSet(com.rbnb.api.FrameManager setI)
     * @see com.rbnb.api.StorageManager#trim()
     * @since V2.0
     * @version 07/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2011  MJM  Added lock release on prolonged blocking waits
     * 07/30/2004  INB	Added overview of how data is added.
     * 11/17/2003  INB	Use <code>try/finally</code> to ensure that we don't
     *			leave the flag set.
     * 03/28/2003  INB	Don't set adding a frame unless we're adding a frame.
     * 03/27/2003  INB	Eliminated locks.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/21/2003  INB	Log add.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 01/02/2001  INB	Created.
     *
     */
    public final void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	if (!(this instanceof Log) && (getLog() != null)) {
	    getLog().addMessage
		(getLogLevel() + 10,
		 getLogClass(),
		 getName(),
		 "Adding:\n" + childI);
	}
	*/

	if (childI instanceof RingBuffer) {
	    // If the child is the <code>RingBuffer</code>, add it directly.
	    super.addChild(childI);

	} else {
	    if (!(this instanceof NBO)) {
		childI.stripDot();
	    }

	    try {
		setAddingAFrame(true);
		synchronized (this) {
		    // Wait for the previous frame to be processed.
		    long startAt = System.currentTimeMillis();
		    long now;
		    while ((getThread() != null) && getThread().isAlive() &&
			   !getTerminateRequested() &&
			   ((getRCO() == null) ||
			    !getRCO().getTerminateRequested()) &&
			   (getAcceptingAFrame() ||
			    (getFrame() != null))) {
			wait(TimerPeriod.NORMAL_WAIT);

			if (((now = System.currentTimeMillis()) - startAt) >=
			    TimerPeriod.LOCK_WAIT) {
			    try {
				throw new Exception
				    (System.currentTimeMillis() + " " +
				     getName() + 
			             " Warning:  waiting a long time, may be a problem;" +
				     " blocked in addChild waiting for " +
				     "work to complete: " +
				     childI + " " +
				     getFrame() + " " +
				     getAcceptingAFrame());
			    } catch (Exception e) {
				e.printStackTrace();
			    System.err.println("Releasing Locks! (hope for the best)");
			    unlockWrite();		// MJM FOO:  try release deadlock w/ reckless abandon
			    }
			    startAt = now;
			}
		    }
		    if (getTerminateRequested() ||
			((getRCO() != null) &&
			 getRCO().getTerminateRequested())) {
			return;
		    }
		    // 2008/03/25  WHF  Moved setFrame() call here inside the
		    //   synch block.  With it outside the synch block, the
		    //   frame would sometimes be set just as the server
		    //   was shutting down, thus blocking inside 'moveDownFrom'.
		    setFrame(childI);		    
		}

		// Place the input frame <code>Rmap</code> where it will be
		// picked up.
		//setFrame(childI);

	    } finally {
		setAddingAFrame(false);
		synchronized (this) {
		    notifyAll();
		}
	    }
	}
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
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
	return (SourceIO.additionalToString(this));
    }

    /**
     * Adds an <code>AwaitNotification</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param anI  the <code>AwaitNotification</code> object.
     * @see #removeNotification(com.rbnb.api.AwaitNotification)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    public final void addNotification(AwaitNotification anI) {
	awaiting.addElement(anI);
    }

    /**
     * Allow reconnects to happen?
     * <p>
     * <code>RBOs</code> allow reconnects if they have an archive and are not
     * active.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code> trying to reconnect.
     * @return are reconnects allowed?
     * @since V2.1
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/15/2011  MJM  Allow reconnect to replace old source in special case
     * 04/17/2003  INB	Created.
     *
     */
    public final boolean allowReconnect(Username usernameI) {

	    // MJM 3/15/11:  allow reconnect with kill of old RCO... (!)
	    if( /* (getAmode() == ACCESS_NONE) || */ !allowAccess(usernameI)) return(false);		// check if allowed?
		if(getRCO() == null) return(true);
		
		// ok let's be mean and kill the old connection...
		if(getName().startsWith(".")) {	// only if source name starts with "."
			try{
				this.setAkeep(true);
				this.setCkeep(true);
				stop((ClientHandler) this);		// MJM buh bye
			} catch (Exception e) { 
				System.err.println("oops can't disconnect RCO, e:"+e);
				return(false); 
				}
			return(true);		// old source is gone, let new one reconnect
    	} else {	// old code MJM
			return ((getRCO() == null) &&
			(getAmode() != ACCESS_NONE) &&
			allowAccess(usernameI));
		}	
    }

    /**
     * Does the archive exist?
     * <p>
     * For the archive to exist, the following things must exist:
     * <p><ol>
     * <li>the archive directory, and</li>
     * <li>the archive seal file.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @return does the archive exist?
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created from the <code>Archive.exists()</code> method.
     *
     */
    final boolean archiveExists() {
	boolean existsR = false;

	try {
	    String directory = getArchiveDirectory();

	    java.io.File archive = new java.io.File(directory);
	    if (archive.exists()) {
		// If the archive directory exists, check for one of the files
		// that should be in it.
		existsR =
		    (Seal.exists(directory) ||
		     (archive.list(new ArchiveExistsFilter()).length > 0));
	    }

	} catch (java.lang.Exception e) {
	}

	return (existsR);
    }

    /**
     * Archive exists file filter class.
     * <p>
     * This class is used to determine if the archive can be considered to
     * "exist".
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     * Copyright 2001, 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Moved from <code>Archive</code> to <code>RBO</code> and
     *			modified to work with multiple
     *			<code>RingBuffers</code>.
     * 12/11/2001  INB	Created.
     *
     */
     private class ArchiveExistsFilter
	 implements java.io.FilenameFilter
     {

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 12/11/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 12/11/2001  INB	Created.
	 *
	 */
	 ArchiveExistsFilter() {
	     super();
	 }

	/**
	 * Is the input filename acceptable as proof of an archive's existance?
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param dirI  the directory.
	 * @param nameI the name of the file.
	 * @return is the filename acceptable?
	 * @since V2.0
	 * @version 02/18/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/18/2003  INB	Moved from <code>Archive</code> to
	 *			<code>RBO</code> and modified to work with
	 *			multiple <code>RingBuffers</code>.
	 * 12/11/2001  INB	Created.
	 *
	 */
	 public final boolean accept(java.io.File dirI,String nameI) {
	     return (nameI.equals("summary.rbn") ||
		     nameI.equals("reghdr.rbn") ||
		     nameI.equals("regdat.rbn") ||
		     ((nameI.indexOf("RB") == 0) && (nameI.length() > 2)));
	 }
     }

    /**
     * Calculates the cache and archive data sizes in bytes.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheDSIO   the cache data size.
     * @param archiveDSIO the archive data size.
     * @since V2.0
     * @version 01/16/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2004  INB	Synchronize on the metrics calculation.
     * 05/22/2003  INB	Protect against <code>RBO</code> being shut down.
     * 02/17/2003  INB	Modified to handle multiple
     *			<code>RingBuffers</code>. The total size is the sum of
     *			the <code>RingBuffer</code> sizes.
     * 11/19/2002  INB	Created.
     *
     */
    public final void calculateDataSizes(long[] cacheDSIO,
					 long[] archiveDSIO)
    {
	try {
	    synchronized (metricsSyncObj) {
		RingBuffer rb;
		for (int idx = 0,
			 eIdx = getNchildren();
		     (idx < eIdx) &&
			 !getTerminateRequested() &&
			 !getPerformReset();
		     ++idx) {
		    rb = (RingBuffer) getChildAt(idx);
		    rb.calculateDataSizes(cacheDSIO,archiveDSIO);
		}
	    }

	} catch (java.lang.Exception e) {
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
     * @version 01/16/2004
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
	boolean locked = false;
	try {
	    // Wait until nothing else is happening.
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
				 getName() + 
			         " Warning:  waiting a long time, may be a problem;" +
				 " blocked in clearCache waiting for work " +
				 " to complete: " +
				 getFrame() + " " +
				 getAcceptingAFrame() + " " +
				 getAddingAFrame());
			} catch (Exception e) {
			    e.printStackTrace();
			    System.err.println("Releasing Locks (hope for the best)");
			    unlockWrite();		// MJM:  try release deadlock w/ reckless abandon
			    startAt = now;
			}
		    }
		}
	    }
	    lockWrite("RBO.clearCache");
	    locked = true;

	    synchronized (metricsSyncObj) {
		int endIdx;
		if ((endIdx = getNchildren()) > 0) {
		    // Clear the <code>Cache</code> of each of the
		    // <code>RingBuffers</code>.
		    RingBuffer rb;
		    for (int idx = 0; idx < endIdx; ++idx) {
			rb = (RingBuffer) getChildAt(idx);
			if (rb != null) {
			    rb.clearCache();
			}
		    }
		}
	    }

	    // Notify anyone that is waiting for this to complete.
	    synchronized (this) {
		setPerformClearCache(false);
		notifyAll();
	    }

	} finally {
	    if (locked) {
		unlockWrite();
	    }
	}
    }

    /**
     * Converts what appears to be a V2.0 archive into a form that can be
     * recovered into a V2.1 archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @param logI	    the server <code>Log</code>.
     * @param afterI	    the minimum valid time.
     * @param beforeI	    the maximum valid time.
     * @param filesI	    the list of files.
     * @param validSealsO   the list of valid <code>RingBuffers</code> and
     *			    their <code>Seals</code>.
     * @param invalidSealsO the list of invalid <code>RingBuffers</code> and
     *			    their <code>Seals</code>.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Archive</code> is in such a state that it
     *		  cannot possibly be validated.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 04/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/02/2003  INB	Created.
     *
     */
    private final void convertFromV2p0(Log logI,
				       long afterI,
				       long beforeI,
				       java.io.File[] filesI,
				       java.util.Vector validSealsO,
				       java.util.Vector invalidSealsO)
	throws java.io.InterruptedIOException,
	       java.lang.InterruptedException
    {
	if (logI != null) {
	    try {
		logI.addMessage(getLogLevel(),
				getLogClass(),
				getName(),
				"This appears to be a V2.0 archive. " +
				"Attempting conversion...");
		Thread.currentThread().yield();
	    } catch (java.lang.Throwable e) {
	    }
	}

	// Create a <code>RingBuffer</code> directory.
	java.io.File rbDirectory = new java.io.File(getArchiveDirectory(),
						    "RB1");
	rbDirectory.mkdir();

	// Move all of the files into it.
	StringBuffer sbuffer = new StringBuffer("Failed to rename files:\n");
	boolean failure = false;
	for (int idx = 0; idx < filesI.length; ++idx) {
	    filesI[idx].renameTo(new java.io.File(rbDirectory.getPath(),
						  filesI[idx].getName()));
	}
	if (failure && (logI != null)) {
	    try {
		logI.addMessage(getLogLevel(),
				getLogClass(),
				getName(),
				sbuffer.toString());
		Thread.currentThread().yield();
	    } catch (java.lang.Throwable e) {
	    }
	}

	// Create a <code>RingBuffer</code> object for it.
	RingBuffer rb = null;
	try {
	    rb = new RingBuffer(1);
            rb.setupTrimTimes(cacheflush,cachetrim,archiveflush,archivetrim);
	} catch (java.lang.Exception e) {
	    return;
	}
	rb.setParent(this);
	Seal theSeal;
	java.util.Vector rbValidSeals = new java.util.Vector();
	java.util.Vector rbInvalidSeals = new java.util.Vector();
	try {
	    if ((theSeal = rb.validateArchive
		 (afterI,
		  beforeI,
		  rbValidSeals,
		  rbInvalidSeals)) == null) {
		throw new com.rbnb.api.InvalidSealException
		    (theSeal,
		     afterI,
		     beforeI);
	    } else {
		validSealsO.addElement(rb);
		validSealsO.addElement(theSeal);
	    }

	} catch (java.lang.Exception e) {
	    invalidSealsO.addElement(rb);
	    invalidSealsO.addElement(rbValidSeals);
	    invalidSealsO.addElement(rbInvalidSeals);
	    invalidSealsO.addElement(e);
	}
    }

    /**
     * Deletes the <code>Archive</code> files in their entirety.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem performing the delete.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/03/2013  MJM  try-again if file delete fails 
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/08/2004  INB	Don't abort on first failed delete, but instead
     *			hold a list of the files that don't delete.
     * 11/12/2003  INB	Identify the location when locking.
     * 10/27/2003  INB	Use write lock methods.
     * 02/18/2003  INB	Created from the corresponding <code>Archive</code>
     *			method.
     *
     */
    final void deleteArchive()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Lock the door.
	Log log = null;
	try {
	    if (!(this instanceof Log)) {
		log = getLog();
	    }
	} catch (Exception e) {
	}
	try {
	    lockWrite("RBO.deleteArchive");

	    // Create a <code>File</code> object for the archive directory.
	    java.io.File aDir = new java.io.File(getArchiveDirectory());
	    if (aDir.exists()) {
		Directory asDirectory = new Directory(aDir);

	    // Delete the files in the archive.
		java.io.File[] files = asDirectory.listFiles(),
		    nFiles = null;

	    // This code performs a recursive removal without using a recursive
	    // method.
		java.util.Vector levels = new java.util.Vector();
		levels.addElement(files);
		levels.addElement(new Integer(0));
		levels.addElement(new Boolean(false));
		int filesIdx = 0,
		    indexIdx = 1,
		    seenIdx = 2;

		java.util.Vector failures = new java.util.Vector();

		while (levels.size() > 0) {
		    files = (java.io.File []) levels.elementAt(filesIdx);
		    int idx =
			((Integer) levels.elementAt(indexIdx)).intValue();
		    boolean seenBefore =
			((Boolean) levels.elementAt(seenIdx)).booleanValue();

		    for (; (files != null) && (idx < files.length); ++idx) {
			Directory fAsDirectory = new Directory(files[idx]);
			nFiles = fAsDirectory.listFiles();
			if ((nFiles != null) && (nFiles.length != 0)) {
			    // Ensure that directories are empty before we get
			    // rid of them.
			    if (seenBefore) {
				// If we've seen this before, then we simply
				// skip it.
				failures.addElement(files[idx].toString());
				continue;
			    } else {
				break;
			    }
			}
			seenBefore = false;

			files[idx].delete();
			if (files[idx].exists()) {
			    failures.addElement(files[idx].toString());
			}
		    }

		    if ((files != null) && (idx < files.length)) {
			// If we broke out of the loop, then we need to move
			// down a level.
			levels.setElementAt(new Integer(idx),indexIdx);
			levels.setElementAt(new Boolean(true),seenIdx);
			levels.addElement(nFiles);
			levels.addElement(new Integer(0));
			levels.addElement(new Boolean(false));
			filesIdx += 3;
			indexIdx += 3;
			seenIdx += 3;

		    } else {
			// If we reached the end of the loop, then we need to
			// move up a level.
			levels.removeElementAt(seenIdx);
			levels.removeElementAt(indexIdx);
			levels.removeElementAt(filesIdx);
			indexIdx -= 3;
			filesIdx -= 3;
			seenIdx -= 3;
		    }
		}

		// Delete the directory.
		aDir.delete();
		if (aDir.exists()) {
		    failures.addElement(aDir.toString());
		}

		if (failures.size() > 0) {
			try { // MJM 10/9/12:  try again using simple recursive delete
				System.err.println("Trouble deleting archive: "+aDir);
				Thread.sleep(2000);	// let collision pass...
				deleteRecursive(aDir);	
				System.err.println("Recursive delete succeeded!");
			} catch (IOException e) {	// failed second try
				System.err.println(e.getMessage());	// more info
			    StringBuffer errorMsg = new StringBuffer
				("Failed to delete: ");
			    for (int idx = 0; idx < failures.size(); ++idx) {
				errorMsg.append("\n   ");
				errorMsg.append((String) failures.elementAt(idx));
			    }
			    throw new java.io.IOException(errorMsg.toString());
			}
		}
	    }

	} catch (java.io.IOException e) {
	    if (log != null) {
		try {
		    log.addException(Log.STANDARD,
				     getLogClass(),
				     getName(),
				     e);
		} catch (Exception e1) {
		}
	    }
	    throw e;

	} catch (java.lang.InterruptedException e) {
	    if (log != null) {
		try {
		    log.addException(Log.STANDARD,
				     getLogClass(),
				     getName(),
				     e);
		} catch (Exception e1) {
		}
	    }
	    throw e;

	} catch (Exception e) {
	    if (log != null) {
		try {
		    log.addException(Log.STANDARD,
				     getLogClass(),
				     getName(),
				     e);
		} catch (Exception e1) {
		}
	    }

	} finally {
	    // OK, we can unlock the door now.
	    unlockWrite();
	}
    }
    
    /**
     * Recursive delete file folder method. 
     * Backup for failure in deleteArchive()
     * 
     * @author Matt Miller
     * @param file File or Folder to delete
     * @throws IOException
     * @since V3.6
     * @version 10/09/2012
     */
    /*
    *
    *   Date      By	Description
    * MM/DD/YYYY
    * ----------  --	-----------
    * 10/09/2012  MJM	Created.
    *
    */
	private static void deleteRecursive(File file) throws IOException {
		if (file.isDirectory()) {
//		    for (File c : file.listFiles())	// no for-each loop in java1.4
			File[] c = file.listFiles();
			for(int i=0;i<c.length;i++)
		      deleteRecursive(c[i]);
		}
		if (!file.delete()) {
		    throw new IOException("Failed to delete: " + file);
		}
	}

    /**
     * Deletes one or more channels from the <code>RBO</code>.
     * <p>
     * Channels are deleted by deleting the <code>RingBuffer</code> that
     * contains them.  If that <code>RingBuffer</code> contains more than one
     * channel, then the request must specify that all of the channels are to
     * be deleted or those that are specified will not be deleted.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelsI <code>Rmap</code> hierarchy specifying the channels
     *			to be deleted.
     * @return <code>Rmap</code> containing status information for each of
     *	       the channels.
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
     * @since V2.2
     * @version 07/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/07/2004  INB	Delete channels from the explicitly registered Rmaps
     *			as well as from the ring buffer data Rmaps.
     * 07/30/2003  INB	Created.
     *
     */
    public final Rmap deleteChannels(Rmap channelsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap channelsR = null;

	// Wait until nothing else is happening.
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
			     getName() + 
			     " Warning:  waiting a long time, may be a problem;" +
			     " blocked in deleteChannels waiting for " +
			     "work to complete: " +
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

	// Create an output channel map that is a clone of the input.
	channelsR = (Rmap) channelsI.duplicate();

	// Extract the names of the channels to delete.
	String[] channelNames = channelsI.extractNames();
	Rmap entry;

	if ((channelNames.length == 1) &&
	    (channelNames[0].equals("...") ||
	     channelNames[0].equals("/..."))) {
	    // Special case: a single entry with a "..." wildcard means
	    // delete the whole thing.
	    reset();
	    entry = channelsR.findDescendant(channelNames[0],false);
	    entry.setDblock
		(new DataBlock(DELETE_CODES[0],
			       1,
			       DELETE_CODES[0].length(),
			       DataBlock.TYPE_STRING,
			       DataBlock.ORDER_MSB,
			       false,
			       0,
			       DELETE_CODES[0].length()));

	} else {
	    // Standard case: locate and delete the channels.
	    channelsR = deleteSomeChannels(channelNames,channelsR);
	}
		
	return (channelsR);
    }

    /**
     * Deletes some of the channels in this <code>RBO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelNamesI the names of the channels to delete.
     * @param channelsO	    <code>Rmap</code> hierarchy specifying the channels
     *			    to be deleted.  Filled with information about what
     *			    was done.
     * @return <code>Rmap</code> containing status information for each of
     *	       the channels.
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
     * @since V2.2
     * @version 07/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/07/2004  INB	Delete channels from the explicitly registered Rmaps
     *			as well as from the ring buffer data Rmaps.
     * 11/14/2003  INB	Eliminated the isLocked variable - it is not needed.
     * 11/12/2003  INB	Added location to locks.     
     * 10/27/2003  INB	Use write lock methods.
     * 07/31/2003  INB	Created.
     *
     */
    private final Rmap deleteSomeChannels(String[] channelNamesI,
					  Rmap channelsO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap channelsR = channelsO;

	try {
	    // following grope NG
	    // String[] knownChannels = getRegistered().extractNames();  // mjm 1/12/06 moved up to avoid deadlock
	    lockWrite("RBO.deleteSomeChannels");
	    updateRegistration();

	    // Create a map matching channels in the delete list to
	    // <code>RingBuffers</code>.  The list consists of the ring
	    // buffer (used as the key) and a vector of channels (used as
	    // the value).
	    java.util.Vector deletedChannels = new java.util.Vector();
	    java.util.Hashtable map = new java.util.Hashtable();
	    java.util.Vector channels;
	    String channelName;
	    RingBuffer rb;
	    Rmap entry;


	    String[] knownChannels = getRegistered().extractNames(); 

	    for (int idx = 0; idx < channelNamesI.length; ++idx) {
		if (!mapChannel(channelNamesI[idx],
				channelNamesI[idx],
				knownChannels,
				map,
				deletedChannels)) {
		    entry = channelsR.findDescendant(channelNamesI[idx],
						     false);
		    entry.setDblock
			(new DataBlock(DELETE_CODES[1],
				       1,
				       DELETE_CODES[1].length(),
				       DataBlock.TYPE_STRING,
				       DataBlock.ORDER_MSB,
				       false,
				       0,
				       DELETE_CODES[1].length()));
		}
	    }

	    // For each ring buffer in the map, determine if all of its
	    // channels are to be deleted.  If so, then mark it for delete.
	    // Otherwise, report a failure.
	    java.util.Enumeration rbs = map.keys();
	    java.util.Vector deleteRBs = new java.util.Vector();
	    String message;
	    String[] names;
	    String[] channelNames;
	    while (rbs.hasMoreElements()) {
		rb = (RingBuffer) rbs.nextElement();
		channels = (java.util.Vector) map.get(rb);
		channelNames = rb.getRegistered().extractNames();
		if (channels.size() == channelNames.length) {
		    deleteRBs.addElement(rb);

		} else {
		    names = (String[]) channels.firstElement();
		    channelName = names[1];
		    entry = channelsR.findDescendant(channelName,false);
		    if (entry != null) {
			message = DELETE_CODES[2] +
			    "\nThe channels in the ring buffer are:";
			for (int idx1 = 0;
			     idx1 < channelNames.length;
			     ++idx1) {
			    channelName = channelNames[idx1];
			    if (channelName.charAt(0) == '/') {
				channelName = channelName.substring(1);
			    }
			    message += "\n" + channelName;
			}
			if (entry.getDblock().getDtype() !=
			    DataBlock.TYPE_STRING) {
			    entry.setDblock
				(new DataBlock(message,
					       1,
					       message.length(),
					       DataBlock.TYPE_STRING,
					       DataBlock.ORDER_MSB,
					       false,
					       0,
					       message.length()));
			}
		    
			message = DELETE_CODES[2] +
			    "\nSee result for " +
			    channels.firstElement() + ".";
			for (int idx1 = 1;
			     idx1 < channels.size();
			     ++idx1) {
			    names = (String[]) channels.elementAt(idx1);
			    channelName = names[1];
			    entry = channelsR.findDescendant(channelName,
							     false);
			    if ((entry != null) &&
				(entry.getDblock().getDtype() !=
				 DataBlock.TYPE_STRING)) {
				entry.setDblock
				    (new DataBlock(message,
						   1,
						   message.length(),
						   DataBlock.TYPE_STRING,
						   DataBlock.ORDER_MSB,
						   false,
						   0,
						   message.length()));
			    }
			}
		    }
		}
	    }

	    // Delete the ring buffers for which all the channels were
	    // specified.
	    for (int idx = 0; idx < deleteRBs.size(); ++idx) {
		rb = (RingBuffer) deleteRBs.elementAt(idx);
		channels = (java.util.Vector) map.get(rb);
		rb.destroy();
		rbsChanged.remove(rb);
		removeChild(rb);
		rb.nullify();

		for (int idx1 = 0; idx1 < channels.size(); ++idx1) {
		    names = (String[]) channels.elementAt(idx1);
		    channelName = names[1];
		    channelToRB.remove(names[0]);
		    deletedChannels.addElement(names[0]);
		    entry = channelsR.findDescendant(channelName,false);
		    if ((entry != null) &&
			(entry.getDblock().getDtype() !=
			 DataBlock.TYPE_STRING)) {
			entry.setDblock
			    (new DataBlock(DELETE_CODES[0],
					   1,
					   DELETE_CODES[0].length(),
					   DataBlock.TYPE_STRING,
					   DataBlock.ORDER_MSB,
					   false,
					   0,
					   DELETE_CODES[0].length()));
		    }
		}
	    }

	    if (getExplicitRegistration() != null) {
		// Delete the channels from the explicitly registered data.
		Rmap regMap;
		Rmap nxtMap;
		for (int idx = 0; idx < deletedChannels.size(); ++idx) {
		    regMap = getExplicitRegistration().findDescendant
			((String) deletedChannels.elementAt(idx),
			 false);

		    if (regMap != null) {
			// If the specified name exists, then attempt to delete
			// it.
			if ((regMap.getNchildren() > 0) &&
			    (regMap.getChildAt(0).getName() == null)) {
			    // If there is a marker Rmap that represents the
			    // channel, then delete it.
			    regMap.removeChildAt(0);

			    entry = channelsR.findDescendant
				(channelNamesI[idx],
				 false);
			    entry.setDblock
				(new DataBlock(DELETE_CODES[0],
					       1,
					       DELETE_CODES[0].length(),
					       DataBlock.TYPE_STRING,
					       DataBlock.ORDER_MSB,
					       false,
					       0,
					       DELETE_CODES[0].length()));
			}

			// We'll delete up the line until we either hit the top
			// or we find an Rmap with children still left.
			while ((regMap != getExplicitRegistration()) &&
			       (regMap.getNchildren() == 0)) {
			    nxtMap = regMap.getParent();
			    nxtMap.removeChild(regMap);
			    regMap = nxtMap;
			}

			if ((regMap == getExplicitRegistration()) &&
			    (regMap.getNchildren() == 0)) {
			    // If we got to the top and there are no children
			    // here any more, then we can completely delete the
			    // explicit registration.  We're obviously done
			    // doing deletes.
			    registeredExplicitly = null;
			    break;
			}
		    }
		}
	    }

	    // Update the registration.
	    setLastRegistration(Long.MIN_VALUE);
	    if (getRegistered() != null) {
		getRegistered().nullify();
		setRegistered(null);
	    }
	    updateRegistration();

	} finally {
	    unlockWrite();
	}

	return (channelsR);
    }

    /**
     * Performs the initial connection logic.
     * <p>
     *
     * @author Ian Brown
     *
     * @return was this operation successful?
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/12/2003  INB	Added is in startup handling.
     * 04/17/2003  INB	Created from code extracted from the <code>run</code>
     *			method.
     *
     */
    private final boolean doConnect() {
	boolean successR = false;
	try {
	    setIsInStartup(true);
	    if (!(this instanceof Log)) {
		// Log the start.
		String message;
		if (getRCO().getBuildVersion() == null) {
		    message = ("Started for " +
			       ((this instanceof SinkHandler) ?
				"sink" :
				"source") +
			       " running an old build version");
		} else {
		    message = ("Started for " +
			       ((this instanceof SinkHandler) ?
				"sink" :
				"source") +
			       " running " +
			       getRCO().getBuildVersion() +
			       " from " + getRCO().getBuildDate());
		}
		/*
		if (getRCO().getLicenseString() != null) {
		    message += " using license " + getRCO().getLicenseString();
		}
		*/
		message += ".";
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     message);
	    }

	    // Set up the ring buffer.
	    setUpRingBuffer();

	    // We were successful.
	    successR = true;
	    setIsInStartup(false);

	} catch (com.rbnb.api.AddressException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.io.IOException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.InterruptedException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Exception e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }

	} catch (java.lang.Error e) {
	    try {
		getLog().addError(Log.STANDARD,
				  getLogClass(),
				  toString(),
				  e);
		getRCO().send(Language.exception
			      (new java.lang.Exception
				  ("A fatal error occured.\n" +
				   e.getClass() + " " + e.getMessage())));
	    } catch (java.lang.Throwable e1) {
	    }
	}

	return (successR);
    }

    /**
     * Performs the main loop operation.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/18/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/18/2004  INB	Catch exceptions on the timer cancel and ignore them.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/08/2004  INB	Added clear cache handling.
     * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
     * 11/12/2003  INB	Added identification to the <code>Door</code>.
     * 09/08/2003  INB	Always clear reconnecting flag.
     * 04/17/2003  INB	Created from logic extracted from the <code>run</code>
     *			method.
     *
     */
    private final void doLoop() {
	try {
	    if (getTerminateRequested()) {
		reconnecting = false;
		return;
	    }
	    registrationDoor.setIdentification(getFullName());

	    // Schedule a status logger.
	    TimerTask statusTT = null;
	    if (!(this instanceof Log) &&
		(getName().indexOf("_Metrics") == -1) &&
		(getLocalServerHandler().getLogStatusPeriod() > 0)) {
		MetricsCollector metricsCollector = new MetricsCollector();
		metricsCollector.setObject(this);
		statusTT = new TimerTask(metricsCollector,
					 LogStatusInterface.TT_LOG_STATUS);
		getLocalServerHandler().getTimer().schedule
		    (statusTT,
		     getLocalServerHandler().getLogStatusPeriod(),
		     getLocalServerHandler().getLogStatusPeriod());
	    }

	    // Wait until we are asked to stop.
	    if (reconnecting) {
		logStatus("Reconnected");
		reconnecting = false;
	    }
	    synchronized (this) {
		notifyAll();
	    }
	    while (!getTerminateRequested() && !getThread().interrupted()) {
		acceptFrame(processData());
		if (getPerformReset()) {
		    reset();
		} else if (getPerformClearCache()) {
		    clearCache();
		}
		if (getThread() != null) {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBO.doLoop(1)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		}
	    }

	    synchronized (this) {
		if (statusTT != null) {
		    try {
			statusTT.cancel();
		    } catch (java.lang.Exception e) {
		    }
		    statusTT = null;
		}
	    }

	} catch (com.rbnb.api.AddressException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    setCkeep(false);
	    reconnecting = false;

	} catch (com.rbnb.api.SerializeException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    setCkeep(false);
	    reconnecting = false;

	} catch (java.io.IOException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    setCkeep(false);
	    reconnecting = false;

	} catch (java.lang.InterruptedException e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    setCkeep(false);
	    reconnecting = false;

	} catch (java.lang.Exception e) {
	    try {
		if (this instanceof Log) {
		    e.printStackTrace();
		} else {
		    getLog().addException
			(Log.STANDARD,
			 getLogClass(),
			 getName(),
			 e);
		    getRCO().send(Language.exception(e));
		}
	    } catch (java.lang.Throwable e1) {
	    }
	    setCkeep(false);
	    reconnecting = false;

	} catch (java.lang.Error e) {
	    try {
		getLog().addError(Log.STANDARD,
				  getLogClass(),
				  toString(),
				  e);
		getRCO().send(Language.exception
			      (new java.lang.Exception
				  ("A fatal error occured.\n" +
				   e.getClass() + " " + e.getMessage())));
	    } catch (java.lang.Throwable e1) {
	    }
	    setCkeep(false);
	    reconnecting = false;

	} finally {
	    if (getThread() != null) {
		try {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBO.doLoop(2)",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		} catch (java.lang.Exception e) {
		}
	    }
	}
    }

    /**
     * Performs shutdown logic, with allowance for reconnects.
     * <p>
     *
     * @author Ian Brown
     *
     * @param logI the <code>Log</code>.
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/19/2013  MJM	move lRCO refs inside try/catch block: can be null on reconnect errors
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/16/2004  INB	Ensure that the RCO shutdown happens before we
     *			disconnect from our parent to ensure that we properly
     *			update our parent's metrics.
     * 01/15/2004  INB	Added setting of <code>hasBeenShutdown</code>.
     * 01/14/2004  INB	Update the metrics bytes when the <code>RCO</code>
     *			is shut down on a detach.  Also, add its bytes rather
     *			than simply saving them.  Synchronize while updating
     *			the <code>metricsBytes</code>.
     * 01/09/2004  INB	Ensure that the <code>Archive</code> is up-to-date
     *			when a detach occurs.  If an error occurs during a
     *			detach, then force a complete shutdown instead.
     * 01/08/2004  INB	Clear the clear cache flag.
     * 11/12/2003  INB	Identify the location of the lock.
     * 11/11/2003  INB	Lock the <code>RBO</code>.
     * 09/08/2003  INB	Set reconnecting flag when the wait for reconnect
     *			loop exits.  This ensures a full shutdown even if the
     *			<code>RBO</code> is terminated by rbnbAdmin without
     *			an application ever really reconnecting.
     * 04/17/2003  INB	Created from logic extracted from the <code>run</code>
     *			method.
     *
     */
    private final void doShutdown(Log logI) {
	boolean locked = false;

// 2008/03/26  WHF  Var "frame" should be null here or there may be deadlocks.
//System.err.println("RBO::doShutdown, frame = "+frame);
	try {
	    try {
		lockWrite("RBO.doShutdown");
		locked = true;
	    } catch (java.lang.Exception e) {
	    }

	    // The request to terminate has been handled.
	    setTerminateRequested(false);

	    if (getCkeep()) {
		// If we're keeping the <code>Cache</code>, then just detach
		// the <code>RCO</code>. This allows for reconnects later.  The
		// <code>Cache</code> is flushed to the <code>Archive</code>
		// and the <code>Archive</code> is sealed.
		try {
		    if ((getAmode() == ACCESS_CREATE) ||
			(getAmode() == ACCESS_APPEND)) {
			flush();
			writeToArchive();
		    }

		    if (locked) {
			try {
			    unlockWrite();
			    locked = false;
			} catch (java.lang.Exception e) {
			}
		    }
		    synchronized (metricsSyncObj) {
			RCO lRCO = getRCO();
			setRCO(null);
			try {			// MJM 6/2013:  move lRCO refs inside try/catch block:  can be null on reconnect errors
				lRCO.setClientHandler(null);
			    lRCO.stop();
				metricsBytes += lRCO.bytesTransferred();
			} catch (java.lang.Throwable e) {}
		    }
		    try {
			lockWrite("RBO.doShutdown(keepCache)");
			locked = true;
		    } catch (java.lang.Exception e) {
		    }

		    logStatus("Has disconnected");

		    synchronized (this) {
			setPerformReset(false);
			setPerformClearCache(false);
			notifyAll();
		    }

		} catch (java.lang.Exception e) {
		    // If a problem occurs when trying to do a detach, then
		    // report the error and force a complete shutdown.
		    if ((logI != null) && !(this instanceof Log)) {
			try {
			    logI.addException
				(Log.STANDARD,
				 getLogClass(),
				 getName(),
				 e);
			} catch (java.lang.Exception e1) {
			}
		    }
		    setCkeep(false);
		}
	    }

	    if (!getCkeep()) {
		// If the data is not to be maintained, then toss it.
		logStatus("Is shutting down");
		hasBeenShutdown = true;

		// Perform shutdown operations.
		shutdown();

		// Log the end.
		if ((logI != null) && !(this instanceof Log)) {
		    try {
			logI.addMessage
			    (getLogLevel(),
			     getLogClass(),
			     getName(),
			     "Stopped.");
			Thread.currentThread().yield();
		    } catch (java.lang.Throwable e) {
		    }

		    // Ensure that anyone waiting for a reset or clear cache
		    // thinks it is done.
		    synchronized (this) {
			setPerformReset(false);
			setPerformClearCache(false);
			notifyAll();
		    }
		}

		if (getRCO() != null) {
		    RCO lRCO = getRCO();

		    // Ask the <code>RCO</code> to stop.
		    synchronized (metricsSyncObj) {
			metricsBytes += lRCO.bytesTransferred();
			try {
			    setRCO(null);
			    lRCO.setClientHandler(null);
			    lRCO.stop();
			    Thread.currentThread().yield();
			} catch (java.lang.Throwable e) {
			}
		    }
		}

		// Remove us as a child.
		try {
		    Rmap lParent = getParent();
		    RemoteServer rParent = (RemoteServer) lParent;
		    synchronized (rParent.metricsSyncObj) {
			rParent.metricsDeadBytes += bytesTransferred();
			lParent.removeChild(this);
		    }
		    Thread.currentThread().yield();
		} catch (java.lang.Throwable e) {
		}
	    }

	    if (locked) {
		try {
		    unlockWrite();
		    locked = false;
		} catch (Exception e) {
		}
	    }

	    if (getCkeep()) {
		try {
		    synchronized (this) {
			while (!reconnecting &&
			       !getTerminateRequested() &&
			       !getThread().interrupted()) {
			    wait(TimerPeriod.NORMAL_WAIT);
			}
		    }
		} catch (java.lang.InterruptedException e) {
		    setCkeep(false);
		}
		reconnecting = true;
	    }

	} finally {
	    if (locked) {
		try {
		    unlockWrite();
		} catch (Exception e) {
		}
	    }
	}
    }

    /**
     * Matches the contents of this <code>RBO</code> against a
     * <code>TimeRelativeRequest</code>.
     * <p>
     * This method performs the following steps:
     * <p><ol>
     *    <li>Compare the time reference for each of the channels in the
     *	      request to the limits of <code>RBO</code>,</li>
     *    <li>If the reference is to data outside of the limits, then add
     *	      a regular request to the result for the channel starting at the
     *	      appropriate limit (assuming that the direction of the request is
     *	      into the data)</li>
     *    <li>If the reference is to data within the limits, then
     *        <br><ol>
     *            <li>Find the <code>RingBuffer</code> that contains the data
     *		      for the channel,<li>
     *		  <li>Assign the channel to a request for data from that
     *		      <code>RingBuffer</code>, and</li>
     *            <li>Work on each <code>RingBuffer</code> in turn, or<li>
     *        </ol></li>
     *    <li>If the response from any <code>RingBuffer</code> matching
     *	      indicates that the channels have different time bases, then
     *	      the request for that <code>RingBuffer</code> is split into one
     *	      per channel and those new requests are processed.</li>
     *    </ol>
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @param optionsI the <code>RequestOptions</code>.
     * @return a regular request for the desired data.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
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
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Handle <code>hasBeenShutdown</code> flag.
     * 12/08/2003  INB	Handle <code>RequestOptions.extendStart</code>.
     * 10/10/2003  INB	Created.
     *
     */
    final Rmap extractTimeRelative(TimeRelativeRequest requestI,
				   RequestOptions optionsI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap requestR = null;

	if (getIsInStartup() || getTerminateRequested() || hasBeenShutdown) {
	    return (null);
	}
	updateRegistration();

	/*
	System.err.println("\n" + getFullName() +
			   "\nRegistered: " + getRegistered() +
			   "\nprocessing: " + requestI);
	*/

	if (getRegistered() == null) {
	    return (null);
	}

	boolean locked = false;
	try {
	    lockRead("RBO.extractTimeRelative");
	    locked = true;

	    if (hasBeenShutdown) {
		return (null);
	    }
	    TimeRelativeRequest workRequest = requestI;
	    com.rbnb.utility.SortedVector toMatch =
		workRequest.getByChannel();

	    TimeRelativeChannel trc;
	    Rmap wildcard;
	    Rmap matched;
	    String[] names;
	    for (int idx = 0; idx < toMatch.size(); ++idx) {
		trc = (TimeRelativeChannel) toMatch.elementAt(idx);

		if (!trc.getChannelName().endsWith("...") &&
		    (trc.getChannelName().indexOf("*") == -1)) {
		    // This channel contains no wildcards, copy it over if
		    // necessary.
		    if (workRequest != requestI) {
			workRequest.addChannel(trc);
		    }

		} else {
		    // If we've got a channel with a wildcard, then we need to
		    // expand it.
		    if (workRequest == requestI) {
			workRequest = new TimeRelativeRequest();
			workRequest.setTimeRange(requestI.getTimeRange());
			workRequest.setRelationship
			    (requestI.getRelationship());
			workRequest.setNameOffset(requestI.getNameOffset());
			for (int idx1 = 0; idx1 < idx; ++idx1) {
			    workRequest.addChannel
				((TimeRelativeChannel)
				 toMatch.elementAt(idx1));
			}
		    }

		    wildcard = Rmap.createFromName(trc.getChannelName());
		    matched = getRegistered(wildcard);
		    if (matched != null) {
			names = matched.extractNames();
			if (names.length > 0) {
			    for (int idx1 = 0; idx1 < names.length; ++idx1) {
				trc = new TimeRelativeChannel();
				trc.setChannelName(names[idx1]);
				workRequest.addChannel(trc);
			    }
			}
		    }
		}
	    }
	    if (workRequest != requestI) {
		toMatch = workRequest.getByChannel();

	    }

	    java.util.Hashtable requests = new java.util.Hashtable();
	    TimeRelativeRequest request;
	    TimeRelativeResponse response;
	    RingBuffer rb = null;
	    DataArray limits;
	    Long indexL;
	    boolean keepChannel = false;
	    double endLimits;
	    double endRequest;
	    int direction;
	    Rmap rRequest = null;
	    Rmap result;
	    Rmap child;
	    String channelName;

	    requestR = new Rmap();
	    workRequest.setNameOffset(getName().length() + 1);
	    for (int idx = 0; idx < toMatch.size(); ++idx) {
		// For each channel in the input request, determine where its
		// reference is relative to the data available.
		trc = (TimeRelativeChannel) toMatch.elementAt(idx);
		channelName =
		    trc.getChannelName().substring
		    (workRequest.getNameOffset());

		limits = getRegistered().extract(channelName);

		if ((limits.timeRanges == null) ||
		    (limits.timeRanges.size() == 0)) {
		    continue;
		}
		
		direction = workRequest.compareToLimits(limits);
		
		if (direction == 0) {
		    // If the reference is inside the limits, then add the
		    // channel to a <code>TimeRelativeRequest</code> matched to
		    // the appropriate <code>RingBuffer</code>.
		    indexL = (Long) channelToRB.get(channelName);
		    if (indexL != null) {
			rb = findRingBuffer(indexL.longValue());
			if (rb != null) {
			    request = (TimeRelativeRequest) requests.get(rb);
			    if (request == null) {
				request = new TimeRelativeRequest();
				request.setTimeRange
				    (workRequest.getTimeRange());
				request.setRelationship
				    (workRequest.getRelationship());
				request.setNameOffset
				    (workRequest.getNameOffset());
			    }
			    request.addChannel(trc);
			    requests.put(rb,request);
			}
		    }

		} else if (direction == 2) {
		    // If the channel doesn't actually have any data, then
		    // skip.
		    continue;

		} else if ((direction == -1) &&
			   ((workRequest.getRelationship() ==
			     TimeRelativeRequest.AT_OR_AFTER) ||
			    (workRequest.getRelationship() ==
			     TimeRelativeRequest.AFTER))) {
		    // If the reference is before any available data and the
		    // request is for data after the reference, then build a
		    // request for the oldest data available.
		    response = new TimeRelativeResponse();
		    response.setStatus(0);
		    response.setTime(limits.getStartTime());
		    response.setInvert(false);
		    // JPW 09/08/2005: Don't send buildRequest() the entire
		    //                 working request - we'll end up adding
		    //                 a whole copy of the working request
		    //                 onto requestR.  Instead, create a new
		    //                 temporary request that only contains
		    //                 the channel of interest. Then, call
		    //                 buildRequest() just on that temporary
		    //                 request.
		    // rRequest = response.buildRequest(workRequest,optionsI);
		    TimeRelativeRequest tempRequest =
			new TimeRelativeRequest();
		    tempRequest.setTimeRange(workRequest.getTimeRange());
		    tempRequest.setRelationship(workRequest.getRelationship());
		    tempRequest.setNameOffset(workRequest.getNameOffset());
		    tempRequest.addChannel(trc);
		    rRequest = response.buildRequest(tempRequest,optionsI);
		    requestR.addChild(rRequest);
		    /*
		    System.err.println(getFullName() +
				       ": Built (from oldest) request: " +
				       rRequest +
				       "\nagainst: " + requestI);
		    */

		} else if ((direction == 1) &&
			   ((workRequest.getRelationship() ==
			     TimeRelativeRequest.AT_OR_BEFORE) ||
			    (workRequest.getRelationship() ==
			     TimeRelativeRequest.BEFORE))) {
		    // If the reference is after any available data and the
		    // request is for data before the reference, then build a
		    // request for the newest data available.
		    response = new TimeRelativeResponse();
		    response.setStatus(0);
		    response.setTime
			(limits.getStartTime() + limits.getDuration());
		    response.setInvert(true);
		    // JPW 09/08/2005: Don't send buildRequest() the entire
		    //                 working request - we'll end up adding
		    //                 a whole copy of the working request
		    //                 onto requestR.  Instead, create a new
		    //                 temporary request that only contains
		    //                 the channel of interest. Then, call
		    //                 buildRequest() just on that temporary
		    //                 request.
		    // rRequest = response.buildRequest(workRequest,optionsI);
		    TimeRelativeRequest tempRequest =
			new TimeRelativeRequest();
		    tempRequest.setTimeRange(workRequest.getTimeRange());
		    tempRequest.setRelationship(workRequest.getRelationship());
		    tempRequest.setNameOffset(workRequest.getNameOffset());
		    tempRequest.addChannel(trc);
		    rRequest = response.buildRequest(tempRequest,optionsI);
		    requestR.addChild(rRequest);
		    /*
		    System.err.println(getFullName() +
				       ": Built (from newest) request: " +
				       rRequest +
				       "\nagainst: " + requestI);
		    */
		}
	    }

	    for (java.util.Enumeration keys = requests.keys();
		 keys.hasMoreElements();
		 ) {
		// For each of the <code>RingBuffers</code> containing data of
		// possible interest, work on that <code>RingBuffer</code>.
		rb = (RingBuffer) keys.nextElement();
		request = (TimeRelativeRequest) requests.get(rb);
		toMatch = request.getByChannel();
		
		response = rb.matchTimeRelative(request,optionsI);
		
		switch (response.getStatus()) {
		case -1:
		case 1:
		    // There is no matching data for the channels.
		    rRequest = null;
		    break;

		case 0:
		    // We found a time base for the request.
		    rRequest = response.buildRequest(request,optionsI);
		    break;
		    
		case -2:
		    // The channels have different <code>TimeRanges</code>.
		    rRequest = rb.individualTimeRelative(request,optionsI);
		    break;
		}

		if (rRequest != null) {
		    // If there is a resulting request for data, then add it
		    // to the return request.
		    requestR.addChild(rRequest);
		}
	    }

	    if (requestR.getNchildren() == 0) {
		// If the result has no children, then there is no actual data
		// available.
		requestR = null;
	    }

	    /*
	    System.err.println(getFullName() +
			       ": Resulting request: " + requestR +
			       "\nagainst: " + requestI);
	    */

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	return (requestR);
    }

    /**
     * Finds a <code>RingBuffer</code> by its index.
     * <p>
     * The <code>RingBuffers</code> are sorted by index, but some may have
     * been deleted.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbIndexI the index to locate.
     * @return the <code>RingBuffer</code> or <code>null</code>.
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
     * @since V2.2
     * @version 07/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/31/2003  INB	Created.
     *
     */
    final RingBuffer findRingBuffer(long rbIndexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RingBuffer rbR = null,
	    entry;
	int lo = 0,
	    hi = getNchildren() - 1;
	long diff;

	for (int idx =
		 ((rbIndexI > Integer.MAX_VALUE) ?
		  hi :
		  Math.min((int) (rbIndexI - 1),hi));
	     lo <= hi;
	     idx = (hi + lo)/2) {
	    entry = (RingBuffer) getChildAt(idx);
	    if (entry == null) {
	      return (null);
	    }
	    diff = rbIndexI - entry.getIndex();

	    if (diff < 0) {
		hi = idx - 1;
	    } else if (diff > 0) {
		lo = idx + 1;
	    } else {
		rbR = entry;
		break;
	    }
	}

	return (rbR);
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
	RingBuffer rb;
	for (int idx = 0; idx < getNchildren(); ++idx) {
	    rb = (RingBuffer) getChildAt(idx);
	    if (rb != null) {
		rb.flush();
	    }
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
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/05/2001  INB	Created.
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
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/08/2001  INB	Created.
     *
     */
    final boolean getAddingAFrame() {
	return (addingAFrame);
    }

    /**
     * Gets the desired number of frames allowed in the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive limit in frames.
     * @see #setAframes(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getAframes() {
	return (archiveFrames);
    }

    /**
     * Gets the keep archive flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return keep the archive on disk?
     * @see #setAkeep(boolean)
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public final boolean getAkeep() {
	return (archiveKeep);
    }

    /**
     * Gets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive access mode.
     * @see com.rbnb.api.Source#ACCESS_APPEND
     * @see com.rbnb.api.Source#ACCESS_CREATE
     * @see com.rbnb.api.Source#ACCESS_LOAD
     * @see com.rbnb.api.Source#ACCESS_NONE
     * @see #setAmode(byte)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public final byte getAmode() {
	return (accessMode);
    }

    /**
     * Gets the archive directory.
     * <p>
     * This method works up the <code>Rmap</code> chain until it sees the
     * <code>ServerHandler</code> object and prepends names.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive directory.
     * @since V2.0
     * @version 06/22/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2008/01/31  WHF  Added override.
     * 06/22/2006  JPW	Use RBNB.getArchiveHomeDirectory()
     * 03/10/2003  INB	Allow for null ancestor.
     * 02/26/2001  INB	Created.
     *
     */
    public final String getArchiveDirectory() {
	if (archiveDirectoryOverride != null) return archiveDirectoryOverride;
	
	StringBuffer sBuffer = new StringBuffer(getName());
	Rmap ancestor = getParent();

	while ((ancestor != null) && !(ancestor instanceof ServerHandler)) {
	    if (ancestor.getName() != null) {
		sBuffer.insert
		    (0,
		     (ancestor.getName() +
		      System.getProperty("file.separator")));
	    }

	    ancestor = ancestor.getParent();
	}
	
	// JPW 06/22/2006: Use archive home directory
	if ( (ancestor != null) && (ancestor instanceof RBNB) ) {
	    sBuffer.insert
		(0,
		 (((RBNB)ancestor).getArchiveHomeDirectory() +
		  System.getProperty("file.separator")));
	}
	
	return (sBuffer.toString());
    }

    /**
     * Gets the desired amount of memory usage allowed for the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive size in bytes.
     * @see #setAsize(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getAsize() {
	return (archiveSize);
    }

    /**
     * Gets the desired number of frames allowed in the cache.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cache limit in frames.
     * @see #setCframes(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getCframes() {
	return (cacheFrames);
    }

    /**
     * Gets the keep cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return keep the cache in the server on disconnect?
     * @see #setCkeep(boolean)
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public final boolean getCkeep() {
	return (cacheKeep);
    }

    /**
     * Gets the child at the specified index.
     * <p>
     * This method ensures that the code waits until there is no frame being
     * processed before actually getting the child.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the child to retrieve.
     * @return the child <code>Rmap</code>.
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
     * @see #addChild(com.rbnb.api.Rmap)
     * @see com.rbnb.api.Rmap#getNchildren()
     * @since V2.0
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Handle a terminated child and eliminate wait code.
     * 04/01/2001  INB	Created.
     *
     */
    public Rmap getChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap childR = null;
	if (isRunning() && !getTerminateRequested()) {
	    childR = super.getChildAt(indexI);
	}
	return (childR);
    }

    /**
     * Gets the desired amount of memory usage allowed for the cache.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cache size in bytes.
     * @see #setCsize(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getCsize() {
	return (cacheSize);
    }

    /**
     * Gets the explicitly registered <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the explicitly registered <code>Rmap</code> hierarchy.
     * @since V2.0
     * @version 04/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2002  INB	Created.
     *
     */
    public final Rmap getExplicitRegistration() {
	return (registeredExplicitly);
    }

    /**
     * Gets the frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the frame <code>Rmap</code>.
     * @see #setFrame(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/05/2001  INB	Created.
     *
     */
    final Rmap getFrame() {
	return (frame);
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
     * 02/22/2001  INB	Created.
     *
     */
    final long getLastRegistration() {
	return (lastRegistration);
    }

    /**
     * Gets the log class mask for this <code>RBO</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public long getLogClass() {
	return (super.getLogClass() | Log.CLASS_RBO);
    }

    /**
     * Gets the next <code>Rmap</code> frame index to assign.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the next <code>Rmap</code> frame index.
     * @see #setNfindex(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2001  INB	Created.
     *
     */
    public final long getNfindex() {
	return (nFindex);
    }

    /**
     * Gets the next <code>RingBuffer</code> index to assign.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the next <code>RingBuffer</code> index.
     * @see #setNrbindex(long)
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
    public final long getNrbindex() {
	return (nRBindex);
    }

    /**
     * Gets the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of <code>FrameSets</code>.
     * @see #setNfs(int)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2001  INB	Created.
     *
     */
    public final int getNfs() {
	return (frameSets);
    }

    /**
     * Gets the perform clear cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return perform a clear cache?
     * @see #setPerformClearCache(boolean)
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public final boolean getPerformClearCache() {
	return (performClearCache);
    }

    /**
     * Gets the perform reset flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return perform a reset?
     * @see #setPerformReset(boolean)
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public final boolean getPerformReset() {
	return (performReset);
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
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public final Rmap getRegistered()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (registered);
    }

    /**
     * Gets the registration list for this <code>RBO</code> matching the input
     * hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return the matching registration information.
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
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Handle <code>hasBeenShutdown</code> flag.
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Reworked things so that the locked variable isn't
     *			needed.
     * 11/12/2003  INB	Added is in startup/terminating handling.
     * 02/19/2003  INB	Allow for the case where the registration map hasn't
     *			yet been created.
     * 04/24/2001  INB	Created.
     *
     */
    public Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getName().indexOf("_RC.") != -1) {
	    return (null);
	}

	SourceInterface sourceR = (SourceInterface)
	    super.getRegistered(requestI);

	((SourceIO) sourceR).copyFields(this);
	if (!hasBeenShutdown &&
	    !getIsInStartup() &&
	    !getTerminateRequested() &&
	    (requestI.getNchildren() != 0)) {
	    // mjm: 1/12/06 taking out the following doesn't help the deadlock
      	    updateRegistration();     // make sure "dynamic" parts of registration up to date
	    boolean locked = false;
	    try {
		/* mjm 1/12/06:  grope, try not locking here to debug deadlock problem? NG */
       		lockRead("RBO.getRegistered");
		locked = true;
		//     System.err.println("mjm RBO.getRegistered lock removed!");

		if (!hasBeenShutdown) {
		    Registration lreg = (Registration) getRegistered();
		    Rmap subRequest,
			rmap;
		    for (int idx = 0,
			     endIdx = requestI.getNchildren();
			 idx < endIdx;
			 ++idx) {
			subRequest = requestI.getChildAt(idx);

			if (lreg != null) {
			    if ((subRequest.compareNames("...") == 0) ||
				(subRequest.compareNames(">...") == 0)) {
				sourceR.addChild((Rmap) lreg.clone());

			    } else {
				if ((rmap = lreg.getRegistered
				     (subRequest)) != null) {
				    sourceR.addChild(rmap);
				}
			    }
			}
		    }
		}

	    } finally {
		if (locked) {
		    unlockRead();
		}
	    }
	}

	return ((Rmap) sourceR);
    }

    /**
     * Handles a new frame <code>Rmap</code>.
     * <p>
     * This method is called whenever a frame <code>Rmap</code> is accepted by
     * the <code>RBO</code>. It allows for any special handling needed by the
     * subclasses of the class <code>RBO</code>.
     * <p>
     * For the <code>RBO</code> it is posts the frame as an event.
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
     * @version 05/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2001  INB	Created.
     *
     */
    void handleNewFrame(Rmap theFrameI)
	throws com.rbnb.api.AddressException,
	       java.lang.InterruptedException
    {
	// Notify people of the arrival.
	try {
//EMF 6/30/06: listeners need client name to match against this frame instead
//             of looking into RBO
//System.err.println("RBO is NBO? "+(this instanceof NBO));
//System.err.println("theFrameI "+theFrameI);
//System.err.println("theFrameI.getParent() "+theFrameI.getParent());
//if (!(this instanceof NBO) && theFrameI.getParent()==null) {
  //theFrameI.setName(this.getName());
//}
//System.err.println("theFrameI "+theFrameI);
	    post(theFrameI);
	} catch (com.rbnb.api.SerializeException e) {
	    throw new java.lang.InternalError();
	} catch (java.io.IOException e) {
	    throw new java.lang.InternalError();
	}
    }

    /**
     * Locates the child at the specified index in the <code>RBO</code>.
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
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     *			This initial release always uses the first one.
     * 02/12/2001  INB	Created.
     *
     */
    final Rmap locateChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long startAt = System.currentTimeMillis();
	long now;
	synchronized (this) {
	    while ((getFrame() != null) ||
		   (getAcceptingAFrame() &&
		    (Thread.currentThread() != getThread())) ||
		   (getAddingAFrame() &&
		    (Thread.currentThread() != getRCO().getThread()))) {
		wait(TimerPeriod.NORMAL_WAIT);

		if (((now = System.currentTimeMillis()) - startAt) >=
		    TimerPeriod.LOCK_WAIT) {
		    try {
			throw new Exception
			    (System.currentTimeMillis() + " " +
			     getName() + 
			     " Warning:  waiting a long time, may be a problem;" +
			     " blocked in locateChildAt waiting for " +
			     "work to complete: " +
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

	Rmap childR = null;
	if (getNchildren() > 0) {
	    RingBuffer rb = (RingBuffer) getChildAt(0);
	    childR = rb.locateChildAt(indexI);
	}

	return (childR);
    }

    /**
     * Locks this <code>RBO</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Use version that takes a location.
     * 06/14/2001  INB	Created.
     *
     */
    final void lockRead()
	throws java.lang.InterruptedException
    {
	lockRead("RBO.lockRead");
    }

    /**
     * Locks this <code>RBO</code> hierarchy for read access.
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
     * Locks this <code>RBO</code> hierarchy for write access.
     * <p>
     *
     * @author Ian Brown
     *
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
     * 10/27/2003  INB	Created.
     *
     */
    final void lockWrite()
	throws java.lang.InterruptedException
    {
	lockWrite("RBO.lockWrite");
    }

    /**
     * Locks this <code>RBO</code> hierarchy for write access.
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
    final void lockWrite(String locationI)
	throws java.lang.InterruptedException
    {
	registrationDoor.lock(locationI);
    }

    /**
     * Logs the status of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param prefixI the prefix string.
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
     * 11/14/2003  INB	Reworked things so that the locked variable isn't needed.
     * 11/12/2003  INB	Added location of locks.
     * 04/02/2003  INB	Don't log channels if the <code>Log</code> is display
     *			only.
     * 03/14/2003  INB	Use <code>StringBuffer</code> to build message. Log up
     *			to 101 channels.
     * 02/19/2003  INB	Handle <code>null getRegistered</code> return.
     * 11/19/2002  INB	Created.
     *
     */
    public final void logStatus(String prefixI) {
	try {
	    if (!(this instanceof Log) && (getName().charAt(0) != '_')) {
		if ((this instanceof NBO) || !getLog().getRunnable()) {
		    super.logStatus(prefixI);
		} else {
		    String[] names = null;
		    updateRegistration();
		    boolean locked = false;
		    try {
			lockRead("RBO.logStatus");
			locked = true;
			if (getRegistered() != null) {
			    names = getRegistered().extractNames();
			}
		    } finally {
			if (locked) {
			    unlockRead();
			}
		    }

		    if ((names == null) || (names.length == 0)) {
			super.logStatus(prefixI);
		    } else {
			StringBuffer message = new StringBuffer
			    (prefixI + " with the following channels:");
			if (names.length <= 100) {
			    for (int idx = 0; idx < names.length; ++idx) {
				message.append("\n\t").append
				    (names[idx].substring(1));
			    }
			} else {
			    for (int idx = 0; idx < 100; ++idx) {
				message.append("\n\t").append
				    (names[idx].substring(1));
			    }
			    message.append("\n\t...\n\t").append
				(names[names.length - 1]);
			}
			getLog().addMessage(getLogLevel(),
					    getLogClass(),
					    getName(),
					    message.toString());
		    }
		}
	    }
	} catch (java.lang.Throwable e) {
	}
    }

    /**
     * Maps the channel specifier to the channels that correspond to it and
     * then to the <code>RingBuffer(s)</code> containing those channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelNameI	the channel to match.
     * @param originalNameI	the original name (may be different if it
     *				contains wildcards).
     * @param knownChannelsI	the list of known channels.
     * @param mapIO		the <code>Hashtable</code> mapping
     *				<code>RingBuffers</code> to the channels
     *				matched.
     * @param unmappedChannelsO the list of channels that couldn't be mapped to
     *				a ring buffer, even if they matched a known
     *				name.
     * @return was the channel mapped?
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
     * @since V2.2
     * @version 07/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/07/2004  INB	Allow for a list of unmapped channels.
     * 07/31/2003  INB	Created.
     *
     */
    private final boolean mapChannel(String channelNameI,
				     String originalNameI,
				     String[] knownChannelsI,
				     java.util.Hashtable mapIO,
				     java.util.Vector unmappedChannelsO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean mappedR = false;
	String channelName =
	    ((channelNameI.charAt(0) != '/') ?
	     "/" + channelNameI :
	     channelNameI);

	if (!channelNameI.endsWith("/...")) {
	    // Non-wildcards simply need to match a known channel in a ring
	    // buffer.
	    Long indexL = (Long) channelToRB.get(channelName);
	    if (indexL != null) {
		long rbIndex = indexL.longValue();
		RingBuffer rb = findRingBuffer(rbIndex);
		java.util.Vector channels = (java.util.Vector) mapIO.get(rb);

		if (channels == null) {
		    channels = new java.util.Vector();
		}
		String[] names = new String[2];
		names[0] = channelName;
		names[1] = originalNameI;
		channels.addElement(names);
		mapIO.put(rb,channels);
		mappedR = true;

	    } else if (unmappedChannelsO != null) {
		// Find the matching known channel name from the registration
		// to put into the output list of unmapped channels.
		int lo = 0,
		    hi = knownChannelsI.length - 1,
		    idx = -1,
		    diff = -1;
		for (idx = (lo + hi)/2; lo <= hi; idx = (lo + hi)/2) {
		    diff = knownChannelsI[idx].compareTo(channelName);

		    if (diff == 0) {
			break;
		    } else if (diff < 0) {
			lo = idx + 1;
		    } else if (diff > 0) {
			hi = idx - 1;
		    }
		}

		if (diff == 0) {
		    // If we found the right entry, then put into the unmapped
		    // channels list.
		    unmappedChannelsO.addElement(knownChannelsI[idx]);
		}
	    }
		

	} else {
	    // Special case: wildcard matching all channels in a folder.  Find
	    // the known channels matching the prefix up to the wildcard and
	    // map them.
	    String prefix = channelName.substring(0,
						  channelName.length() - 4);
	    int lo = 0,
		hi = knownChannelsI.length - 1,
		idx,
		diff;
	    for (idx = (lo + hi)/2; lo <= hi; idx = (lo + hi)/2) {
		diff =
		    ((knownChannelsI[idx].length() < prefix.length()) ?
		     knownChannelsI[idx].compareTo(prefix) :
		     knownChannelsI[idx].substring
		     (0,prefix.length()).compareTo(prefix));

		if (diff == 0) {
		    break;
		} else if (diff < 0) {
		    lo = idx + 1;
		} else if (diff > 0) {
		    hi = idx - 1;
		}
	    }

	    if (lo <= hi) {
		mappedR =
		    mapChannel(knownChannelsI[idx],
			       originalNameI,
			       knownChannelsI,
			       mapIO,
			       unmappedChannelsO) ||
		    mappedR;
		for (int idx1 = idx - 1; idx1 >= lo; --idx1) {
		    if ((knownChannelsI[idx1].length() <= prefix.length()) ||
			!knownChannelsI[idx1].substring
			(0,prefix.length()).equals(prefix)) {
			break;
		    }
		    mappedR =
			mapChannel(knownChannelsI[idx1],
				   originalNameI,
				   knownChannelsI,
				   mapIO,
				   unmappedChannelsO) ||
			mappedR;
		}
		for (int idx1 = idx + 1; idx1 <= hi; ++idx1) {
		    if ((knownChannelsI[idx1].length() <= prefix.length()) ||
			!knownChannelsI[idx1].substring
			(0,prefix.length()).equals(prefix)) {
			break;
		    }
		    mappedR =
			mapChannel(knownChannelsI[idx1],
				   originalNameI,
				   knownChannelsI,
				   mapIO,
				   unmappedChannelsO) ||
			mappedR;
		}
	    }
	}

	return (mappedR);
    }

    /**
     * Marks this <code>Archive</code> and its parent <code>RingBuffer</code>
     * as out-of-date.
     * <p>
     * Deletes the <code>Archive</code> summary and <code>Seal</code> files.
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
	if (!outOfDate) {
	    outOfDate = true;

	    String directory = getArchiveDirectory() + Archive.SEPARATOR;
	    java.io.File file = new java.io.File(directory + "summary.rbn");
	    if (file.exists()) {
		file.delete();
	    }
	    file = new java.io.File(directory + "seal.rbn");
	    if (file.exists()) {
		file.delete();
	    }
	}
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
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Added <code>hasBeenShutdown</code> handling.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/12/2003  INB	Use a list that maps channel names to
     *			<code>RingBuffer</code> indexes. Separated out code
     *			into standard and special request methods.
     * 12/11/2000  INB	Created.
     *
     */
    byte moveDownFrom(RmapExtractor extractorI,
		      ExtractedChain unsatisfiedI,
		      java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	/*
	System.err.println(Thread.currentThread() + " entering " +
			   getName() + " moveDownFrom sync block.");
	*/

	long startAt = System.currentTimeMillis();
	long now;
	synchronized (this) {
	    while (isRunning() &&
		   ((getFrame() != null) ||
		    getAcceptingAFrame() ||
		    getAddingAFrame())) {
		wait(TimerPeriod.NORMAL_WAIT);

		if (((now = System.currentTimeMillis()) - startAt) >=
		    TimerPeriod.LOCK_WAIT) {
		    try {
			throw new Exception
			    (System.currentTimeMillis() + " " +
			     getName() + 
			     " Warning:  waiting a long time, may be a problem;" +
			     " blocked in moveDownFrom waiting for " +
			     "work to complete: " +
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

	/*
	System.err.println(Thread.currentThread() + " exited " +
			   getName() + " moveDownFrom sync block.");
	*/

	if (hasBeenShutdown) {
	    return (MATCH_ILLEGAL);
	}
	if (!(extractorI.getWorkRequest() instanceof DataRequest) ||
	    ((((DataRequest) extractorI.getWorkRequest()).getReference() ==
	      DataRequest.ABSOLUTE))) {
	    reasonR = moveDownFromStandard(extractorI,
					   unsatisfiedI,
					   unsatisfiedO);

	} else {
	    reasonR = moveDownFromSpecial(extractorI,
					  unsatisfiedI,
					  unsatisfiedO);
	}
//System.err.println("\n\n\nRBO.moveDownFrom: RBO is");
//System.err.println(this);
	return (reasonR);
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy in response to a
     * request <code>Rmap</code> hierarchy.
     * <p>
     * This method handles special (oldest/newest type) requests.
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
     * @see #moveDownFromStandard(RmapExtractor,ExtractedChain,java.util.Vector)
     * @since V2.1
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Added handling of <code>hasBeenShutdown</code>.
     * 11/12/2003  INB	Added location of locks.
     * 03/12/2003  INB	Created.
     *
     */
    private final byte moveDownFromSpecial(RmapExtractor extractorI,
					   ExtractedChain unsatisfiedI,
					   java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// If the work request is a special time request, then match against
	// the <code>Registration</code> rather than the
	// <code>RBO</code>. Insert an empty entry into the
	// <code>RmapChain</code>.
	byte reasonR = Rmap.MATCH_UNKNOWN;
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

	/*
	System.err.println(Thread.currentThread() + " entering " +
			   getName() +
			   " moveDownFromSpecial updateRegistration.");
	*/
	updateRegistration();
	/*
	System.err.println(Thread.currentThread() + " exited " +
			   getName() +
			   " moveDownFromSpecial updateRegistration.");
	*/

	boolean locked = false;
	try {
	    // Now move down from the <code>Registration</code>.
	    /*
	    System.err.println(Thread.currentThread() + " entering " +
			       getName() +
			       " moveDownFromSpecial readLock.");
	    */
	    lockRead("RBO.moveDownFromSpecial");
	    locked = true;

	    if (hasBeenShutdown) {
		return (MATCH_ILLEGAL);
	    }
	    
	    Rmap lReg = getRegistered();

	    if (lReg == null) {
		reasonR = MATCH_ILLEGAL;
	    } else {
		RmapVector rVector =
		    RmapVector.addToVector(null,lReg);
		reasonR = unsatisfiedI.matchList
		    (!extractorI.getExtractRmaps(),
		     rVector,
		     unsatisfiedO);
	    }
	} finally {
	    if (locked) {
		unlockRead();
	    }

	    /*
	    System.err.println(Thread.currentThread() + " exited " +
			       getName() +
			       " moveDownFromSpecial readLock.");
	    */
	}

	return (reasonR);
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy in response to a
     * request <code>Rmap</code> hierarchy.
     * <p>
     * This method handles standard requests.
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
     * @see #moveDownFromSpecial(RmapExtractor,ExtractedChain,java.util.Vector)
     * @since V2.1
     * @version 07/13/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/13/2004  INB	Added length check to "..." substring comparison to
     *			ensure that no string index out of range errors can
     *			occur.  Eliminated check for RBO name in the channel
     *			names - this should have been stripped already.
     * 02/03/2004  INB	Handle <code>IllegalMonitorStateExceptions</code>.
     * 01/15/2004  INB	Added handling of <code>hasBeenShutdown</code>.  Handle
     *			<code>null</code> child.
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Lock things for read.
     * 09/09/2003  INB	Create request vector and use it to build request
     *			array.
     * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
     *			<code>RmapVector()</code>.
     * 03/12/2003  INB	Created.
     *
     */
    private final byte moveDownFromStandard(RmapExtractor extractorI,
					    ExtractedChain unsatisfiedI,
					    java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// If this is a normal request, match against the <code>RBO</code>.
	byte reasonR = Rmap.MATCH_UNKNOWN;

	updateRegistration();
	boolean locked = false;
	try {
	    /*
	    System.err.println(Thread.currentThread() + " entering " +
			       getName() +
			       " moveDownFromStandard read lock.");
	    */
	    lockRead("RBO.moveDownFromStandard");
	    locked = true;

	    if (hasBeenShutdown) {
		return (MATCH_ILLEGAL);
	    }

	    if (getNchildren() <= 1) {
		// If we have just a single <code>RingBuffer</code>, use our
		// superclass' method.
		reasonR = super.moveDownFrom(extractorI,
					     unsatisfiedI,
					     unsatisfiedO);
	    } else {
		// Otherwise, for efficiency, we need to split the request
		// across the appropriate <code>RingBuffers</code> (which
		// contain subsets of the channels). To do that, we build up a
		// set of new requests, one for each <code>RingBuffer</code>
		// that contains one or more channels of interest.
		Rmap currentRequest = unsatisfiedI.getRequest();
		String[] channelNames = currentRequest.extractNames();

		boolean doAll = false,
		    doSome = false;
		java.util.Vector requestVector = new java.util.Vector();
		for (int idx = 0;
		     !doAll && (idx < channelNames.length);
		     ++idx) {
		    //EMF 07/14/2004: use built-in String method
		    if (channelNames[idx].endsWith("...")) {
		    //if ((channelNames[idx].length() >= 3) &&
			//channelNames[idx].substring
			//(channelNames[idx].length() - 3).equals("...")) {
			// Channel names that end in "..." can match a variable
			// number of channels and we can't be sure which
			// <code>RingBuffers</code> they'll match, so we simply
			// do all of the <code>RingBuffers</code>.
			doAll = true;

		    } else {
			// Explicit channel names should map to specific
			// <code>RingBuffers</code>.
			String channelName = channelNames[idx];

			/* INB - should already be stripped out, which would
			   make this check cause it to be impossible to get
			   channels with the same name as the RBO.
			if (channelName.startsWith("/" + getName()) {
			    channelName = channelName.substring
				(getName().length() + 1);
			}
			*/

			Long indexL = (Long) channelToRB.get(channelName);

			/*
			System.err.println("Find: " + channelName);
			System.err.println("In: " + channelToRB);
			System.err.println("Result: " + indexL);
			*/

			if (indexL != null) {
			    // If we find a match, extract the request for the
			    // channel.
			    long rbIndex = (long) indexL.longValue();
			    
			    Rmap cReq = Rmap.createFromName(channelNames[idx]);
			    cReq.moveToBottom().setDblock(Rmap.MarkerBlock);
			    Rmap channelRequest = currentRequest.extractRmap
				(cReq,
				 true);
			    if (channelRequest.getName().equals(getName())) {
				channelRequest.setName(null);
			    }

			    int workIdx = (int) (rbIndex - 1);
			    if (requestVector.size() < workIdx + 1) {
				requestVector.setSize(workIdx + 1);
			    }
			    if (requestVector.elementAt(workIdx) == null) {
				requestVector.setElementAt(channelRequest,
							   workIdx);
			    } else {
				requestVector.setElementAt
				    (((Rmap)
				      requestVector.elementAt
				      (workIdx)).mergeWith
				     (channelRequest),
				     workIdx);
			    }
			    doSome = true;
			}
		    }
		}

		Rmap[] requests = new Rmap[requestVector.size()];
		for (int inIdx = 0;
		     inIdx < requestVector.size();
		     ++inIdx) {
		    requests[inIdx] = (Rmap) requestVector.elementAt(inIdx);
		}


		if (doAll) {
		    // If we're to do all of the <code>RingBuffers</code>, then
		    // use our superclass' method.
		    reasonR = super.moveDownFrom(extractorI,
						 unsatisfiedI,
						 unsatisfiedO);

		} else if (doSome) {
		    // If we're to do some of the <code>RingBuffers</code>,
		    // then handle each of them individually.
		    ExtractedChain unsatisfied;
		    byte reason;
		    RmapVector rbVector;
		    int rbMissing = 0;
		    RingBuffer entry;
		    Rmap child;
		    for (int idx = 0; idx < requests.length; ++idx) {
			if (requests[idx] != null) {
			    unsatisfied = (ExtractedChain)
				unsatisfiedI.clone();
			    unsatisfied.setRequest(requests[idx]);
			    child = getChildAt(idx - rbMissing);
			    if (child != null) {
				rbVector = new RmapVector(1);
				rbVector.addElement
				    (getChildAt(idx - rbMissing));
				reason = moveDownFrom(null,
						      rbVector,
						      extractorI,
						      unsatisfied,
						      unsatisfiedO);
				reasonR = combineReasons(reasonR,reason);
			    }

			} else {
			    if (idx - rbMissing >= getNchildren()) {
				++rbMissing;
			    } else {
				entry = (RingBuffer)
				    getChildAt(idx - rbMissing);
				if (entry.getIndex() != idx + 1) {
				    ++rbMissing;
				}
			    }
			}
		    }
		}
	    }

	} catch (java.lang.IllegalMonitorStateException e) {
	    locked = false;
	    return (MATCH_ILLEGAL);

	} finally {
	    if (locked) {
		unlockRead();
	    }

	    /*
	    System.err.println(Thread.currentThread() + " exited " +
			       getName() +
			       " moveDownFromStandard read lock.");
	    */
	}

	return (reasonR);
    }

    /**
     * Creates a new instance of the same class as this <code>RBO</code> (or a
     * similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (SourceIO.newInstance(this));
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
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    public void post(Serializable serializableI)
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
     * Performs any special data handling.
     * <p>
     * This default implementation is a NOP and always returns true.
     * <p>
     *
     * @author Ian Brown
     *
     * @return wait for frames?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    boolean processData()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (true);
    }

    /**
     * Loads the <code>RingBuffers</code> from the archive files.
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
     * @since V2.1
     * @version 01/05/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2006  EMF  Added reset call when recovering archive, to force
     *                  write and reload.
     * 01/05/2004  INB	Check for no children or null child.
     * 11/14/2003  INB	Added update of frame index.
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods.
     * 07/31/2003  INB	Eliminated bad <code>RingBuffer</code> entries.
     * 04/24/2003  INB	Mark <code>Archive</code> as not out-of-date.
     * 02/17/2003  INB	Created.
     *
     */
    final void readFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    RingBuffer rb;

	    // Lock the door.
	    lockWrite("RBO.readFromArchive");

	    // Validate the <code>Archive</code>.
	    if (validateArchive()) {
		// If the <code>Archive</code> is valid, then read it from the
		// files, starting with the basic information.
		int nRB = readOffsetsFromArchive();

		// Read a skeleton of the data from the archive.
		readSkeletonFromArchive();

		// Load the <code>RingBuffers</code> from the archive.
		for (int idx = 1; idx <= nRB; ++idx) {
		    java.io.File rbDirectory = new java.io.File
			(getArchiveDirectory() +
			 Archive.SEPARATOR +
			 "RB" + idx);
		    if (rbDirectory.exists()) {
			rb = new RingBuffer(idx);
			rb.setParent(this);
                        //EMF 10/18/06: add trim by time info
                        rb.setupTrimTimes(cacheflush,cachetrim,archiveflush,archivetrim);
			rb.setup(cFrameSets,cFrFrameSet,aFileSets,aFrSFileSet);
			rb.readFromArchive();
			rb.setParent(null);

			String[] channelNames =
			    rb.getRegistered().extractNames();
			Long indexL = new Long(rb.getIndex()),
			    indexL2 = (Long) channelToRB.get(channelNames[0]);
			if ((indexL2 != null) &&
			    (indexL2.longValue() != indexL.longValue())) {
			    throw new java.lang.IllegalStateException
				("Channels in ring buffer " + rb.getIndex() +
				 " conflict with those in " +
				 indexL2.longValue() + ".");
			}
			String name;
			for (int idx1 = 0;
			     idx1 < channelNames.length;
			     ++idx1) {
			    channelToRB.put(channelNames[idx1],indexL);
			}

			addChild(rb);
			rb.start();
		    }
		}
	    }
            //EMF 11/10/06: if archive was recovered, force a reset
            //  This writes clean versions
            //  of summary files to disk, and loads a clean version.  This also
            //  reduces memory usage, since less is needed for loading than
            //  recovering.
            else {
	    //EMF 1/5/07:  Avoid infinite loop on archive recovery
		System.err.println("RBO.readFromArchive: alreadyReset "+alreadyReset);
		if (!alreadyReset) {
		    alreadyReset=true;
		    reset();
		} else {
		    System.err.println("Archive recovery logic failed.");
		}
              //reset();
            }
	    alreadyReset=false;

	    if (getNchildren() == 0) {
		throw new java.io.IOException
		    ("No ring buffers were read in.");
	    } else if (getChildAt(getNchildren() - 1) == null) {
		throw new com.rbnb.api.SerializeException
		    ("Null ring buffer stored.");
	    } else {
		long nRB = ((RingBuffer) getChildAt
			    (getNchildren() - 1)).getIndex() + 1;
		setNrbindex(nRB);
	    }

	    // Summarize the registration to fInd out the frame limits.
	    if (getRegistered() != null) {
		Rmap summary = getRegistered().summarize();
		if (summary.getFrange() != null) {
		    double fivalue =
			summary.getFrange().getTime() +
			summary.getFrange().getDuration() +
			1.;
		    if (getNfindex() < fivalue) {
			setNfindex((long) fivalue);
		    }
		}
	    }

	    // Mark as not out-of-date.
	    outOfDate = false;

	} finally {
	    // Unlock the door.
	    unlockWrite();
	}
    }

    /**
     * Reads the basic information describing what is in the archive files.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of <code>RingBuffers</code>.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #writeToArchive()
     * @since V2.1
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Copy the file username if we don't have one.
     * 04/17/2003  INB	Read and check the username.
     * 02/17/2003  INB	Created.
     *
     */
    final int readOffsetsFromArchive()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int nRBR = 0;
	String directory = getArchiveDirectory() + Archive.SEPARATOR;
	java.io.FileInputStream fis = new java.io.FileInputStream
	    (directory + "summary.rbn");
	java.io.BufferedReader br = new java.io.BufferedReader
	    (new java.io.InputStreamReader(fis),8192);  // MJM

	String line;
	int idx;
	while ((line = br.readLine()) != null) {
	    if (line.indexOf("Date: ") == 0) {
		// Skip the date.
	    } else if (line.indexOf("Username: ") == 0) {
		// Check the username.
		Username fileUser = new Username();
		java.util.StringTokenizer st = new java.util.StringTokenizer
		    (line.substring(10),",",true);
		String token;
		if (st.hasMoreTokens()) {
		    token = st.nextToken();
		    if (!token.equals(",")) {
			fileUser.setUsername(token);
			if (st.hasMoreTokens()) {
			    token = st.nextToken();
			}
		    }
		    if (st.hasMoreTokens()) {
			token = st.nextToken();
			fileUser.setPassword(token);
		    }
		}
		if (!fileUser.allowAccess(getUsername())) {
		    throw new java.lang.IllegalStateException
			("Cannot access archive " + getName() + ".");
		}
		if (getUsername() == null) {
		    setUsername(fileUser);
		}

	    } else if (line.indexOf("Number Of RingBuffers: ") == 0) {
		// Grab the number of <code>RingBuffers</code>.
		nRBR = Integer.parseInt(line.substring(23));
	    } else {
		int cA = 0;
		if (line.indexOf("Cache ") == 0) {
		    cA = 1;
		    idx = 6;
		} else if (line.indexOf("Archive ") == 0) {
		    cA = 0;
		    idx = 8;
		} else {
		    throw new com.rbnb.api.SerializeException
			("Unrecognized line read from archive summary: " +
			 line);
		}

		if (line.indexOf("Sets: ",idx) == idx) {
		    idx += 6;
		    if (cA == 1) {
			cFrameSets = Integer.parseInt(line.substring(idx));
		    } else {
			aFileSets = Integer.parseInt(line.substring(idx));
		    }
		} else if (line.indexOf("Elements/Set: ",idx) == idx) {
		    idx += 14;
		    if (cA == 1) {
			cFrFrameSet = Integer.parseInt(line.substring(idx));
		    } else {
			aFrSFileSet = Integer.parseInt(line.substring(idx));
		    }
		} else if (line.indexOf("Memory/Set: ",idx) == idx) {
		    idx += 12;
		} else {
		    throw new com.rbnb.api.SerializeException
			("Unrecognized line read from archive summary: " +
			 line);
		}
	    }
	}

	br.close();

	return (nRBR);
    }

    /**
     * Reads a skeleton of the <code>Archive</code> from the archive files.
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
     * @see #readSkeletonFromArchive()
     * @see #writeToArchive()
     * @since V2.1
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods.
     * 02/17/2003  INB	Created.
     *
     */
    final void readSkeletonFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the door.
	    lockWrite("RBO.readSkeletonFromArchive");

	    // Open the <code>Registration</code> files for the
	    // <code>RBO</code> as a whole.
	    String directory = getArchiveDirectory() + Archive.SEPARATOR;
	    java.io.FileInputStream hfis =
		new java.io.FileInputStream(directory + "reghdr.rbn"),
				    dfis =
		new java.io.FileInputStream(directory + "regdat.rbn");
	    InputStream his = new InputStream(hfis,Archive.BINARYARCHIVE,0);
	    DataInputStream dis = new DataInputStream(dfis,0);

	    // Read the <code>Registration</code>.
	    if (his.readParameter(Archive.ARCHIVE_PARAMETERS) ==
		Archive.ARC_REG) {
		setRegistered(new Registration(his,dis));
		getRegistered().readData(dis);
	    }
	    try {
		if (his.readParameter(Archive.ARCHIVE_PARAMETERS) ==
		    Archive.ARC_EXP) {
		    Rmap rmap = new Rmap(his,dis);
		    rmap.readData(dis);
		    register(rmap);
		}
	    } catch (java.io.EOFException e) {
	    }
	    setLastRegistration(System.currentTimeMillis());

	    // Close the registration files; they won't be needed again.
	    his.close();
	    hfis.close();
	    dis.close();
	    dfis.close();
	    
	} finally {
	    // Unlock the door.
	    unlockWrite();
	}
    }

    /**
     * Reconnect to an existing <code>RBO</code>.
     * <p>
     * <code>RBOs</code> allow reconnects to existing archives (for load or
     * append).
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI the new <code>ClientInterface</code>.
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the <code>ClientInterface</code> is not a
     *		  <code>RBO</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 10/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/13/2003  INB	Call <code>updateXXXFromUser</code> methods.
     * 04/17/2003  INB	Created.
     *
     */
    public final void reconnect(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	update(clientInterfaceI);
	updateCacheFromUser();
	updateArchiveFromUser();
	synchronized (this) {
	    reconnecting = true;
	    notifyAll();
	    while (reconnecting) {
		wait(TimerPeriod.NORMAL_WAIT);
	    }
	}
    }

    /**
     * Rebuild an invalid <code>RBO's Archives</code> from the last good
     * sequence of <code>RingBuffers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param validSealsO   the valid <code>RingBuffer Seals</code>.
     * @param invalidSealsO the invalid <code>RingBuffer Seals</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is just no way to recover anything from the
     *		  <code>Archive</code>.
     * @since V2.1
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added update of frame index.
     * 10/31/2003  INB	Added message buffers to hold the messages produced
     *			by the recovery sequence.
     * 09/10/2003  INB	Allow for <code>null</code> frame range.
     * 05/19/2003  INB	Use largest cache/archive sizes rather than last.
     * 02/18/2003  INB	Created from the corresponding <code>Archive</code>
     *			method.
     *
     */
    private final void recoverArchive(java.util.Vector validSealsO,
				      java.util.Vector invalidSealsO)
	throws java.lang.IllegalStateException
    {
	Log log = getLog();
	if (log == this) {
	    log = null;
	} else {
	    try {
		log.addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Archive could not be validated! Attempting recovery...");
		Thread.currentThread().yield();
	    } catch (java.lang.Throwable e) {
	    }
	}

	StringBuffer recoveryMessage = new StringBuffer("");

	if ((validSealsO.size() == 0) && (invalidSealsO.size() == 0)) {
	    throw new java.lang.IllegalStateException
		("No ringbuffers were found in the archive.");
	}

	// Attempt to read whatever <code>RingBuffers</code> we can get.
	try {
	    // Start by trying to recover the offset information, which
	    // includes the sizes of things.
	    readOffsetsFromArchive();
	} catch (java.lang.Exception e) {
	    cFrameSets = 0;
	    cFrFrameSet = 0;
	    aFileSets = 0;
	    aFrSFileSet = 0;
	}

	recoverRingBuffers(validSealsO,invalidSealsO,recoveryMessage);
	if (log != null) {
	    try {
		log.addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     recoveryMessage.toString());
		Thread.currentThread().yield();
	    } catch (java.lang.Throwable e) {
	    }
	}

	try {
	    // Update the registration from the result.
	    setRegistered(new Registration());
	    setLastRegistration(Long.MIN_VALUE);
	    updateRegistration();

	    // Update the sizes and limits.
	    RingBuffer rb = null;
	    Archive archive;
	    FileSet fls;
	    FrameSet frs;
	    long lnfindex;
	    for (int idx = 0,
		     eIdx = getNchildren();
		 idx < eIdx;
		 ++idx) {
		rb = (RingBuffer) getChildAt(idx);
		archive = rb.getArchive();
		fls = (FileSet) archive.getChildAt(archive.getNchildren() - 1);
		if (fls.getNchildren() == 0) {
		    lnfindex = archive.getNchildren();
		} else {
		    frs = (FrameSet) fls.getChildAt(fls.getNchildren() - 1);
		    // mjm 12/21/04 check for null ptr
		    //		    if (frs.getSummary().getFrange() != null) {
		    if ((frs.getSummary() != null) && (frs.getSummary().getFrange() != null)) {
			lnfindex = (long)
			    (frs.getSummary().getFrange().getTime() +
			     frs.getSummary().getFrange().getDuration() +
			     1);
		    } else {
			lnfindex = archive.getNchildren();
		    }
		}
		if (lnfindex > getNfindex()) {
		    setNfindex(lnfindex);
		}
		cFrameSets = Math.max(cFrameSets,rb.getCache().getMs());
		cFrFrameSet = Math.max(cFrFrameSet,rb.getCache().getMeps());
		// System.err.println("Debug: RecoverArchive, aFileSets: "+aFileSets+", getMs: "+rb.getArchive().getMs()); 
		aFileSets = Math.max(aFileSets,rb.getArchive().getMs());
		aFrSFileSet = Math.max(aFrSFileSet,rb.getArchive().getMeps());
	    }

//System.err.println("RBO.recoverArchive before write: "+this);
	    // Rewrite the archive as a whole.
	    writeToArchive();
//System.err.println("RBO.recoverArchive after write: "+this);

	    // Summarize the registration to find out the frame limits.
	    if (getRegistered() != null) {
		Rmap summary = getRegistered().summarize();
		if (summary.getFrange() != null) {
		    double fivalue =
			summary.getFrange().getTime() +
			summary.getFrange().getDuration() +
			1.;
		    if (getNfindex() < fivalue) {
			setNfindex((long) fivalue);
		    }
		}
	    }

	} catch (java.lang.Exception e) {
	    // mjm e.getMessage throws its own exception!
	    e.printStackTrace();
	    throw new java.lang.IllegalStateException
		("Archive recovery failed to rebuild final seal.\n\t");
	    //		  + e.getMessage());
	}
    }

    /**
     * Recovers any <code>RingBuffers</code> that can be made valid in some way
     * or another.
     * <p>
     *
     * @author Ian Brown
     *
     * @param validSealsI      the list of definitely valid <code>Seals</code>.
     * @param invalidSealsI    the list of <code>Seals</code> with problems.
     * @param recoveryMessageO the recovery status message.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is just no way to recover anything from the
     *		  <code>RBO's Archives</code>.
     * @since V2.1
     * @version 10/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/31/2003  INB	Build a recovery message buffer rather than report to
     *			the log.
     * 09/09/2003  INB	Attempt to validate a ring buffer that fails to load.
     * 02/18/2003  INB	Created from the <code>Archive.recoverFileSets</code>
     *			method.
     *
     */
    private final void recoverRingBuffers(java.util.Vector validSealsI,
					  java.util.Vector invalidSealsI,
					  StringBuffer recoveryMessageO)
    {
	boolean useValid = false,
		notrecovered = false,
	        unrecoverable = false;
	int lmeps = 0,
	    ceps = 0;
	RingBuffer lastRB = null,
	    validRB = null,
	    invalidRB = null,
	    rb;
	java.io.File unrecoverableDirectory = new java.io.File
	    (getArchiveDirectory() + Archive.SEPARATOR + "UNRECOVERABLE"),
	    notrecoveredDirectory = new java.io.File
		(getArchiveDirectory() + Archive.SEPARATOR + "NOTRECOVERED");
	Seal lastSeal = null,
	    validSeal = null,
	    invalidSeal = null,
	    theSeal;
	java.util.Vector rbValidSeals = null,
	    rbInvalidSeals = null,
	    ivValidSeals = null,
	    ivInvalidSeals = null;
	StringBuffer goodmessage =
	    new StringBuffer("Archive recovered.\nRingBuffers recovered:"),
	    notmessage = new StringBuffer(""),
	    unmessage = new StringBuffer(""),
	    rbmessage,
	    rbnotmessage,
	    rbunmessage;
	boolean goodRB = false;

	setNfindex(0);
	for (int idx = 0,
		 endIdx = validSealsI.size(),
		 idx1 = 0,
		 endIdx1 = invalidSealsI.size();
	     (idx < endIdx) || (idx1 < endIdx1);
	     ) {
	    // Read through the <code>Seals</code> to produce a sequence of
	    // valid <code>RingBuffers</code>.
	    rbmessage = new StringBuffer("");
	    rbnotmessage = new StringBuffer("");
	    rbunmessage = new StringBuffer("");

	    // Get the next <code>RingBuffer/code> and its <code>Seal</code>.
	    if ((validRB == null) && (idx < endIdx)) {
		validRB = (RingBuffer) validSealsI.elementAt(idx++);
		validSeal = (Seal) validSealsI.elementAt(idx++);
	    }
	    if ((invalidRB == null) && (idx1 < endIdx1)) {
		invalidRB = (RingBuffer) invalidSealsI.elementAt(idx1++);
		ivValidSeals = (java.util.Vector)
		    invalidSealsI.elementAt(idx1++);
		ivInvalidSeals = (java.util.Vector)
		    invalidSealsI.elementAt(idx1++);
		invalidSeal =
		    ((com.rbnb.api.InvalidSealException)
		     invalidSealsI.elementAt(idx1++)).getInvalid();
	    }

	    if ((validRB != null) &&
		((invalidRB == null) ||
		 (validRB.getIndex() < invalidRB.getIndex()))) {
		// If the <code>RingBuffer</code> from the valid list comes
		// before the one from the invalid list, then it is the "next"
		// one.
		rb = validRB;
		theSeal = validSeal;
		rbValidSeals = null;
		rbInvalidSeals = null;
		validRB = null;
		validSeal = null;
		goodRB = true;
	    } else {
		// If the <code>RingBuffer</code> from the invalid list comes
		// before the one from the valid list, then it is the "next"
		// one.
		rb = invalidRB;
		theSeal = invalidSeal;
		rbValidSeals = ivValidSeals;
		rbInvalidSeals = ivInvalidSeals;
		invalidRB = null;
		invalidSeal = null;
		ivValidSeals = null;
		ivInvalidSeals = null;
		goodRB = false;
	    }

	    if (goodRB) {
		try {
		    rb.readFromArchive();
		} catch (java.lang.Exception e) {
		    goodRB = false;
		}
	    }

	    if (!goodRB) {
		try {
		    theSeal = rb.recoverFromArchive(rbValidSeals,
						    rbInvalidSeals,
						    rbmessage,
						    rbnotmessage,
						    rbunmessage);
		    goodRB = (theSeal != null);
		} catch (java.lang.Exception e) {
		    goodRB = false;
		}
	    }

	    try {
		if (goodRB) {
		    String[] channelNames =
			rb.getRegistered().extractNames();
		    Long indexL2 = (Long) channelToRB.get(channelNames[0]);
		    if ((indexL2 != null) &&
			(indexL2.longValue() != rb.getIndex())) {
			throw new java.lang.IllegalStateException
			    ("Channels in ring buffer " + rb.getIndex() +
			     " conflict with those in " +
			     indexL2.longValue() + ".");
		    }

//EMF 10/25/06: inform ringbuffer of trim time attributes so will
//              flush/trim appropriately
		    rb.setupTrimTimes(cacheflush,cachetrim,archiveflush,archivetrim);

		    if (lastRB == null) {
			// If this is the first good <code>RingBuffer</code>,
			// then grab it.
			lastRB = rb;
			lastSeal = theSeal;
			goodmessage.append("\n\t").append(getName()).append
			    ("/RB").append(rb.getIndex());
			addChild(rb);

		    } else {
			// Otherwise, we need to ensure that it comes after the
			// last good one.
			try {
			    theSeal.validate(lastSeal.getAsOf(),
					     Long.MAX_VALUE);
			    goodmessage.append("\n\t").append(getName()).append
				(Archive.SEPARATOR).append("RB").append
				(rb.getIndex());
			    addChild(rb);
			    lastRB = rb;
			    lastSeal = theSeal;

			} catch (com.rbnb.api.InvalidSealException e) {
			    goodRB = false;
			}
		    }
		    goodmessage.append(rbmessage);
		    if (rbnotmessage.length() > 0) {
			goodmessage.append(rbnotmessage);
		    }
		    if (rbunmessage.length() > 0) {
			goodmessage.append(rbunmessage);
		    }

		    if (goodRB) {
			Long indexL = new Long(rb.getIndex());
			String name;
			for (int idx2 = 0;
			     idx2 < channelNames.length;
			     ++idx2) {
			    channelToRB.put(channelNames[idx2],indexL);
			}
		    }
		}

	    } catch (java.lang.Exception e) {
		goodRB = false;
	    }

	    if (!goodRB) {
		try {
		    if (!notrecovered) {
			notrecovered = true;
			notmessage.append
			    ("\nRingBuffers not recovered ").append
			    ("(in NOTRECOVERED):");
			notrecoveredDirectory.mkdirs();
		    }
		    java.io.File oldName = new java.io.File
			(getArchiveDirectory() +
			 Archive.SEPARATOR +
			 "RB" + rb.getIndex()),
			newName = new java.io.File
			    (notrecoveredDirectory.getAbsolutePath() +
			     Archive.SEPARATOR +
			     "RB" + rb.getIndex());
		    if (oldName.renameTo(newName)) {
			notmessage.append("\n\t").append(oldName.getPath());
			if (rbmessage.length() > 0) {
			    notmessage.append("\n\t").append(rbmessage);
			}
			if (rbnotmessage.length() > 0) {
			    notmessage.append("\n\t").append(rbnotmessage);
			}
			if (rbunmessage.length() > 0) {
			    notmessage.append("\n\t").append(rbunmessage);
			}
		    } else {
			unmessage.append("\n\t").append
			    (oldName.getPath()).append(" rename failed.");
			if (rbmessage.length() > 0) {
			    unmessage.append("\n\t").append(rbmessage);
			}
			if (rbnotmessage.length() > 0) {
			    unmessage.append("\n\t").append(rbnotmessage);
			}
			if (rbunmessage.length() > 0) {
			    unmessage.append("\n\t").append(rbunmessage);
			}
		    }
		} catch (java.lang.Exception e) {
		}
	    }
	}

	if (lastRB == null) {
	    throw new java.lang.IllegalStateException
		("No ringbuffers could be recovered from the archive.\n" +
		 notmessage +
		 unmessage);
	}

	recoveryMessageO.append(goodmessage);
	if (notrecovered) {
	    recoveryMessageO.append(notmessage);
	}
	if (unrecoverable) {
	    recoveryMessageO.append(unmessage);
	}
    }

    /**
     * Updates the registration for this <code>Source</code>.
     * <p>
     * The input <code>Rmap</code> hierarchy is used to update the registration
     * for this <code>Source</code>.  The hierarchy may contain
     * <code>DataBlocks</code>, but not time information.  Those
     * <code>DataBlocks</code> are copied into the appropriate locations in the
     * registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the registration <code>Rmap</code> hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods.
     * 03/12/2003  INB	Set the last registration time.
     * 09/25/2001  INB	Created.
     *
     */
    public final void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    lockWrite("RBO.register");
	    if (getRegistered() == null) {
		setRegistered(new Registration());
		setLastRegistration(Long.MIN_VALUE);
	    }
	    if (!(this instanceof NBO)) {
		rmapI.stripDot();
	    }

	    if (getExplicitRegistration() == null) {
		registeredExplicitly = new Registration();
	    }
	    if (rmapI != getExplicitRegistration()) {
		((Registration) getExplicitRegistration()).updateRegistration
		    (rmapI,
		     false,
		     true);
	    }
	    ((Registration) getRegistered()).updateRegistration
		(rmapI,
		 false,
		 true);

	} finally {
	    unlockWrite();
	}
    }

    /**
     * Reset this <code>RBO's</code> ring buffer.
     * <p>
     * This method performs the functional equivalent of closing and re-opening
     * the <code>RBO</code>.  A completely new ring buffer is created to
     * replace the existing one.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 07/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/07/2004  INB	Delete the explicitly registered Rmaps.
     * 01/15/2004  INB	Added setting/clearing of <code>hasBeenShutdown</code>.
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 02/12/2002  INB	Created.
     *
     */
    public final void reset()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    lockWrite("RBO.reset");

	    // Shutdown any activity.
	    hasBeenShutdown = true;
	    shutdown();

	    // Set up the ring buffer again.
	    registeredExplicitly = null;
	    setRegistered(new Registration());
	    setLastRegistration(Long.MIN_VALUE);
	    setUpRingBuffer();

	    synchronized (this) {
		setPerformReset(false);
		notifyAll();
	    }
	    hasBeenShutdown = false;
	    
	} finally {
	    unlockWrite();
	}
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
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    public final void removeNotification(AwaitNotification anI) {
	awaiting.removeElement(anI);
    }

    /**
     * Runs the <code>RBO</code>.
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
     * 11/14/2003  INB	Ensure that we release <code>Locks</code>.
     * 04/17/2003  INB	Rewrote logic to allow reconnects.
     * 04/04/2003  INB	Handle Java errors.
     * 03/20/2003  INB	Changed to a <code>wait</code> with a timeout.
     * 01/02/2001  INB	Created.
     *
     */
    public final void run() {
	Log log = getLog();

	// Do the initial connect.
	try {
	    if (doConnect()) {
		do {
		    doLoop();
		    doShutdown(log);
		} while (reconnecting);

	    } else {
		setCkeep(false);
		doShutdown(log);
	    }
	} finally {
	    if (getThread() != null) {
		try {
		    ((ThreadWithLocks) getThread()).ensureLocksCleared
			(getFullName(),
			 "RBO.run",
			 getLog(),
			 getLogLevel(),
			 getLogClass());
		} catch (java.lang.Exception e) {
		}
	    }
	}

	setThread(null);
	synchronized (this) {
	    notifyAll();
	}
    }

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
     * @since V2.0
     * @version 03/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/27/2003  INB	Don't notify if the input is set.
     * 01/05/2001  INB	Created.
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
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/08/2001  INB	Created.
     *
     */
    final void setAddingAFrame(boolean addingAFrameI) {
	addingAFrame = addingAFrameI;
    }

    /**
     * Sets the desired number of frames allowed in the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a desired number
     * of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param archiveFramesI  the archive limit in frames.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the archive limit specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getAframes()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setAframes(long archiveFramesI) {
	if (archiveFramesI < -1) {
	    throw new IllegalArgumentException
		("Negative archive frame limit of " + archiveFramesI);
	}

	archiveFrames = archiveFramesI;
        //EMF 10/18/06: set trim times if appropriate
        String dotrim=System.getProperty("trimbytime","false");
        if (!dotrim.equals("false")) {
          archivetrim=archiveFrames;
          //if (cacheflush>(archivetrim/10)) cacheflush=archivetrim/10;
          archiveflush=cacheflush*60;
          if (archiveflush>(archivetrim/10)) archiveflush=archivetrim/10;
          if (cacheflush>archiveflush) {
            cacheflush=archiveflush;
            cachetrim=archiveflush;
          }
//System.err.println("Trim-By-Time! cacheflush="+cacheflush+", cachetrim="+cachetrim+", archiveflush="+archiveflush+", archivetrim="+archivetrim);
        }
    }

    /**
     * Sets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param modeI  the archive access mode.
     * @see com.rbnb.api.Source#ACCESS_APPEND
     * @see com.rbnb.api.Source#ACCESS_CREATE
     * @see com.rbnb.api.Source#ACCESS_LOAD
     * @see com.rbnb.api.Source#ACCESS_NONE
     * @see #getAmode()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public final void setAmode(byte modeI) {
	accessMode = modeI;
    }

    /**
     * Sets the keep archive flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param keepI keep the archive on disk?
     * @see #getAkeep()
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public final void setAkeep(boolean keepI) {
	archiveKeep = keepI;
    }

    /**
     * Sets the desired amount of memory usage allowed for the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a desired amount
     * of memory.
     * <p>
     *
     * @author Ian Brown
     *
     * @param archiveSizeI  the archive size in bytes.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the archive size specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getAsize()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setAsize(long archiveSizeI) {
	if (archiveSizeI < -1) {
	    throw new IllegalArgumentException
		("Negative archive frame limit of " + archiveSizeI);
	}

	archiveSize = archiveSizeI;
    }

    /**
     * Sets the desired number of frames allowed in the cache.
     * <p>
     * A value of -1 means that the cache is not limited to a desired number of
     * frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheFramesI  the cache limit in frames.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the cache limit specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getCframes()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setCframes(long cacheFramesI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (cacheFramesI < -1) {
	    throw new IllegalArgumentException
		("Negative cache frame limit of " + cacheFramesI);
	}

	cacheFrames = cacheFramesI;
        //EMF 10/18/06: set trim times if appropriate
        String dotrim=System.getProperty("trimbytime","false");
        if (!dotrim.equals("false")) {
          cacheflush=cacheFrames;
          cachetrim=cacheflush;
//System.err.println("Trim-By-Time Mode! RBO.setCframes: cacheflush="+cacheflush+", cachetrim="+cachetrim);

        }
//System.err.println("RBO.setCframes: cacheflush="+cacheflush+", cachetrim="+cachetrim);
    }

    /**
     * Sets the keep cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param keepI keep the cache in the server?
     * @see #getCkeep()
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public final void setCkeep(boolean keepI) {
	cacheKeep = keepI;
    }

    /**
     * Sets the desired amount of memory usage allowed for the cache.
     * <p>
     * A value of -1 means that the cache is not limited to a desired amount of
     * memory.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheSizeI  the cache size in bytes.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the cache size specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getCsize()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setCsize(long cacheSizeI) {
	if (cacheSizeI < -1) {
	    throw new IllegalArgumentException
		("Negative cache frame limit of " + cacheSizeI);
	}

	cacheSize = cacheSizeI;
    }

    /**
     * Sets the frame <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI  the new frame <code>Rmap</code>.
     * @see #getFrame()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/05/2001  INB	Created.
     *
     */
    final void setFrame(Rmap frameI) {
	frame = frameI;
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
    private final void setLastRegistration(long timeI) {
	lastRegistration = timeI;
    }

    /**
     * Sets the next <code>Rmap</code> frame index to be assigned.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nFindexI  the next <code>Rmap</code> frame index.
     * @see #getNfindex()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2001  INB	Created.
     *
     */
    public final void setNfindex(long nFindexI) {
	nFindex = nFindexI;
    }

    /**
     * Sets the next <code>RingBuffer</code> index to be assigned.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nRBindexI  the next <code>RingBuffer</code> index.
     * @see #getNrbindex()
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
    public final void setNrbindex(long nRBindexI) {
	nRBindex = nRBindexI;
    }

    /**
     * Sets the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameSetsI  the number of cache <code>FrameSets</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of cache <code>FrameSets</code>
     *		  specified is not a legal value:
     *		  <p><ul>
     *		  <li>values less than 0, or</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getNfs()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2001  INB	Created.
     *
     */
    public final void setNfs(int frameSetsI) {
	if (frameSetsI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The number of cache framesets must be greater than 0.");
	}

	frameSets = frameSetsI;
    }

    /**
     * Sets the perform clear cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param performClearCacheI clear the cache?
     * @see #getPerformClearCache()
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public final void setPerformClearCache(boolean performClearCacheI) {
	performClearCache = performClearCacheI;
    }

    /**
     * Sets the perform reset flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param performResetI perform a reset?
     * @see #getPerformReset()
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public final void setPerformReset(boolean performResetI) {
	performReset = performResetI;
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
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods.
     * 07/30/2003  INB	Allow for <code>null</code> input.
     * 01/03/2001  INB	Created.
     *
     */
    final void setRegistered(Registration registeredI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    lockWrite("RBO.setRegistered");
	    if (registeredI != null) {
		registeredI.setParent(this);
	    }
	    registered = registeredI;
	} finally {
	    unlockWrite();
	}
    }

    /**
     * Sets up the <code>Archive</code>.
     * <p>
     * This method is called to set up the <code>Archive</code> based on the
     * user-specified values. It is only called if there is no existing archive
     * file.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem calculating the size of a
     *		  <code>FrameSet</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2006  MJM  Trim number of filesets if archiveSize does not require that much data
     * 10/07/2005  MJM  Firewalled zero sized frameset/set
     * 02/17/2003  INB	Modified to store the calculated sizes locally so that
     *			they can later be used to create
     *			<code>RingBuffers</code>.
     * 12/06/2001  INB	Created.
     *
     */
    private final void setUpArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Calculate the limits.
	long mLimit = getAframes();
	int aFS = getNfs(),
	    fsA = frameSets,
	    fsFS = 1;

	if (mLimit == 0) {
	    // If the size limit is 0, then we don't actually do anything.
	    setAmode(ACCESS_NONE);
	    /*  mjm 10/05. misleading message. Can only get here if there is no archive to delete
	    getLog().addMessage
		(getLogLevel(),
		 getLogClass(),
		 getName(),
		 "The archive size was specified as 0. Any existing archive " +
		 "has been deleted, but no new archive is being created.");
	    */
	    return;

	} else if ((mLimit != -1) && (cFrFrameSet != -1)) {
	    // If the number of frames should be limited, then do so.
	    fsA = (int) Math.ceil(mLimit/cFrFrameSet);
	}
	fsFS = (int) Math.ceil(fsA*1./aFS);
   	aFS = (int) Math.min(aFS, Math.ceil(mLimit / cFrFrameSet));  // trim FileSets if not needed (mjm 8/06)
//System.err.println("Debug:  fsFS: "+fsFS+", fsA: "+fsA+", aFS: "+aFS+", mLimit: "+mLimit+", cFrFrameSet: "+cFrFrameSet);

	// Set up the <code>Archive</code>.
	aFileSets = aFS;
	aFrSFileSet = fsFS;
	if(aFrSFileSet <= 0) aFrSFileSet = 1;  // mjm 10/7/05: avoid zero-sized framesets/set
    }

    /**
     * Sets up the <code>Archive</code> and the <code>Cache</code>.
     * <p>
     * This method is called to set up the <code>Archive</code> and the
     * <code>Cache</code>. It deals with adjusting things if there is an
     * existing file.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem calculating the size of a
     *		  <code>FrameSet</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/30/2003  INB	If the <code>Archive</code> fails to load when doing an
     *			append, create a new one.
     * 02/17/2003  INB	Modified to store the calculated sizes locally so that
     *			they can later be used to set up the
     *			<code>RingBuffers</code>.
     * 12/06/2001  INB	Created.
     *
     */
    private final void setUpArchiveAndCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getAmode() == ACCESS_NONE) {
	    // If there is not going to be an <code>Archive</code>, we simply
	    // set up the cache.
	    setUpCache();

	} else {
	    // If there is to be an <code>Archive</code>, set it up.

	    if (getAmode() == ACCESS_CREATE) {
		// If we are starting from a blank slate, blow away any
		// existing archive, create the <code>Cache</code>, and then
		// the <code>Archive</code>.
		deleteArchive();
		setUpCache();
		setUpArchive();

	    } else if (getAmode() == ACCESS_APPEND) {
		// In append mode, we want to create a new <code>Archive</code>
		// if one doesn't already exist, otherwise we'll load the
		// existing <code>Archive</code>.
		if (!archiveExists()) {
		    setAmode(ACCESS_CREATE);
		    setUpCache();
		    setUpArchive();
		}
	    }

	    // If the <code>Archive</code> already exists, load it.
	    if ((getAmode() == ACCESS_APPEND) ||
		(getAmode() == ACCESS_LOAD)) {
		try {
		    readFromArchive();
//System.err.println("RBO.setUpArchiveAndCache, after readFromArchive, RBO is");
//System.err.println(this);
			// 2008/08/12  WHF  Catch all Throwables:
		} //catch (java.lang.IllegalStateException e) {
		catch (Throwable e) {

		    /*
		    System.err.println("Caught for: " +
				       getAmode() + " " + ACCESS_LOAD + " " +
				       ACCESS_APPEND);
		    */

		    // 2008/08/12  WHF  If failsafe false, recreate original
		    //    behavior:
		    if (getAmode() == ACCESS_LOAD
		    		|| !failSafeMode 
		    		&& e instanceof IllegalStateException) {
			throw new RuntimeException(e);
		    } else {
			if (!(this instanceof Log) && (getLog() != null)) {
			    getLog().addMessage
				(getLogLevel(),
				 getLogClass(),
				 getName(),
				 "Existing archive could not be recovered, " +
				 "creating a new one.");
			}
			moveOldArchive();
			setAmode(ACCESS_CREATE);
			setUpCache();
			setUpArchive();
		    }
		}
		updateRegistration();

		// If the user specified new <code>Cache</code> and/or
		// <code>Archive</code> sizes, then we can change the total
		// number of sets allowed by each of those objects as
		// appropriate.
		updateCacheFromUser();
		updateArchiveFromUser();
	    }
	}
    }
    
    /**
      * Moves an old, probably invalid archive to a new name.
      *
      * @author Bill Finger
      * @since V3.1
      * @version 2008/08/12
      *
      * @throws Nothing.
      */
    private void moveOldArchive()
    {
	final String FAIL_EXT = ".fail.";
	try {
	    java.io.File aDir = new java.io.File(getArchiveDirectory());
	    
	    // Get parent directory, using Java 1.1 APIs:
	    java.io.File parent = new java.io.File(
	    		new java.io.File(aDir.getAbsolutePath()).getParent()
	    );
	    
	    int maxBak = 0;
	    String[] children = parent.list();
	    for (int ii = 0; ii < children.length; ++ii) {
		String child = children[ii];
		
		if (child.startsWith(aDir.getName()) 
			&& !child.equals(aDir.getName())) {
		    // Find extension, if any:
		    String ext = child.substring(aDir.getName().length());
		    if (!ext.startsWith(FAIL_EXT)) continue; // not a retry file
		    int bakNum = Integer.parseInt(
		    		ext.substring(FAIL_EXT.length())
		    );
		    if (bakNum > maxBak) maxBak = bakNum;
		}
	    }
	    
	    String numStr;
	    int num = maxBak + 1;
	    // Quick & dirty text formatting:
	    if (num > 99) numStr = Integer.toString(num);
	    else if (num > 9) numStr = "0" + Integer.toString(num);
	    else numStr = "00" + Integer.toString(num);
	    java.io.File newDir = new java.io.File(
	    		parent,
			aDir.getName() + FAIL_EXT + numStr
	    );
	    if (!aDir.renameTo(newDir)) throw new Exception("Rename failed: "
			+aDir+" to " + newDir + ".");	    
	    
	} catch (Throwable t) {
	    System.err.println("WARNING: Could not move archive "
	    		+getArchiveDirectory());
	    t.printStackTrace();
	}
    }


    /**
     * Sets up the <code>Cache</code>.
     * <p>
     * This method is called to set up the <code>Cache</code> based on the
     * user-specified values. It is only called if there is no existing archive
     * file.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem calculating the size of a
     *		  <code>FrameSet</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 03/29/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2006  EMF  Default framesets set back to 1.
     * 03/29/2006  JPW  Set the default number of framesets to 2.  If user has
     *                  defined "-Dnumframesets=0" on the RBNB Server command
     *                  line, then resort back to the original calculation of
     *                  the number of framesets as the square root of the
     *                  number of cache frames.
     * 03/24/2006  JPW  User can set the number of framesets using a
     *                  system property, "-Dnumframesets=<integer number>"
     * 02/17/2003  INB	Modified to store the calculated values locally to
     *			later be used to set up the <code>RingBuffers</code>.
     * 12/06/2001  INB	Created.
     *
     */
    private final void setUpCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int nFS = getNfs(),
	    fFS = -1;

	if ((getCframes() == -1) && (getCsize() == -1)) {
	    // If the cache is not limited, then we choose a default of
	    // 1000 frames.
	    setCframes(1000);
	}
	
	// Here was the original calculation of the number of framesets.
	// nFS = (int) Math.ceil(Math.sqrt(getCframes()));
	
        // EMF 07/27/2006: Default set back to 1.
	// JPW 03/29/2006: Default number of framesets is 2; if user has
	//                 defined "-Dnumframesets=0" on the RBNB Server
	//                 command line then resort back to the original
	//                 calculation of the number of framesets as the
	//                 square root of the number of cache frames.
	// JPW 03/24/2006: User can set the number of framesets using a
	//                 system property
	
//	nFS = 1;
// MJM 11/16/06:  nope, fall back to original default.  Small number of framesets
//		  leads to large reframe-CPU loads for large (~500) numbers of channels
//		  Use the old approach by default until we can figure out a more 
//		  universal logic.
	nFS = (int) Math.ceil(Math.sqrt(getCframes()));
	
	String nfsPropertyStr = System.getProperty("numframesets");
	if (nfsPropertyStr != null) {
	    try {
		int nFSTemp = Integer.parseInt(nfsPropertyStr);
		if (nFSTemp < 0) {
		    throw new NumberFormatException("");
		} else if (nFSTemp == 0) {
		    // JPW 03/29/2006: Resort back to the original calc
		    nFS = (int) Math.ceil(Math.sqrt(getCframes()));
		} else {
		    nFS = nFSTemp;
		}
	    } catch (NumberFormatException nfe) {
		System.err.println(
		    "Illegal \"numframesets\" setting: " + nfsPropertyStr);
	    }
	}
	
	if (getCframes() == 1) {
	    // If the number of frames in the <code>Cache</code> is supposed
	    // to be 1, then we have a special case.
	    nFS = 0;
	    fFS = 1;

	} else {
	    // If the number of frames in the <code>Cache</code> is limited,
	    // then we calculate the <code>FrameSet</code> sizes from the frame
	    // limit.

	    if ((getCframes() % nFS) == 0) {
		// If the number of <code>FrameSets</code> is an integer
		// divisor of the number of <code>Cache</code> frames, then
		// we can just do the division.
		fFS = (int) (getCframes()/nFS);
	    } else if (getCframes() < nFS) {
		// JPW 03/24/2006: should set fFS = 1 in this case
		// nFS = fFS = (int) getCframes();
		nFS = (int) getCframes();
		fFS = 1;
	    } else {
		// If the number of <code>FrameSets</code> is not an integer
		// divisor of the number of <code>Cache</code>, then we need
		// to find a set of values near the desired entries. Ideally,
		// we want to come as close as possible, but for now we just
		// increase the size of a <code>FrameSet</code> to an integer.
		fFS = (int) (Math.ceil(getCframes()*1./nFS));
	    }
	}

	// Set the <code>Cache</code> sizes.
	cFrameSets = nFS;
	cFrFrameSet = fFS;
    }

    /**
     * Sets up the ring buffer.
     * <p>
     * This method calculates the size of a <code>FrameSet</code> and
     * determines how many <code>FrameSets</code> there should be in the
     * <code>Cache</code> and in the <code>Archive</code>.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem calculating the size of a
     *		  <code>FrameSet</code>.
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
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods.
     * 05/12/2003  INB	Use the registration door rather than synchronization.
     * 02/17/2003  INB	Modified to store the calculated values locally to
     *			later be used to setup the <code>RingBuffers</code>.
     * 02/12/2001  INB	Created.
     *
     */
    private final void setUpRingBuffer()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    lockWrite("RBO.setUpRingBuffer");

	    if (getAmode() == ACCESS_NONE) {
		setUpCache();
	    } else {
		setUpArchiveAndCache();
	    }
	} finally {
	    unlockWrite();
	}

	// Report the results.
	if (!(this instanceof Log) && !(this instanceof NBO)) {
	  String report = "";
          String dotrim=System.getProperty("trimbytime","false");
          if (dotrim.equals("false")) {
	    report =
		"Set up cache with " +
		((cFrameSets == 0) ?
		 "1 frameset of 1 frame/set (total frames = 1)." :
		 (cFrameSets + " framesets of " +
		  cFrFrameSet + " frames/set (total frames = " +
		  cFrameSets*cFrFrameSet + ")."));
	    if (aFileSets != 0) {
		report +=
		    "\nSet up archive with " +
		    aFileSets + " filesets of " +
		    aFrSFileSet + " framesets/set " +
		    "(total frames = " +
		    aFileSets*aFrSFileSet*cFrFrameSet + ")";
	    }
	  }
	  else {
	    report="Trim-By-Time! cacheflush="+cacheflush+", cachetrim="+cachetrim+", archiveflush="+archiveflush+", archivetrim="+archivetrim;
	  }
	  getLog().addMessage(getLogLevel(),
				getLogClass(),
				getName(),
				report);
	}
    }

    /**
     * Shutdown the <code>RBO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 01/08/2004  INB	Remove our children before deleting the
     *			<code>Archive</code>.
     * 07/31/2003  INB	Remove and nullify the <code>RingBuffers</code>.
     * 05/02/2003  INB	Reworked the code to try harder to clean things up.
     *			Also, don't throw out the archive just because an
     *			error occurs.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 06/11/2001  INB	Created.
     *
     */
    void shutdown() {
	stopAwaiting();
	RingBuffer rb;

	int eIdx = 0;
	try {
	    eIdx = getNchildren();
	} catch (java.lang.Throwable e1) {
	}

	for (int idx = 0; idx < eIdx; ++idx) {
	    try {
		rb = (RingBuffer) getChildAt(idx);
		if (rb != null) {
		    rb.stop();
		}

	    } catch (java.lang.Throwable e) {
		if ((getLog() != null) && (getLog() != this)) {
		    try {
			if (e instanceof java.lang.Exception) {
			    getLog().addException(Log.STANDARD,
						  getLogClass(),
						  getName(),
						  (java.lang.Exception) e);
			} else if (e instanceof java.lang.Error) {
			    getLog().addError(Log.STANDARD,
					      getLogClass(),
					      getName(),
					      (java.lang.Error) e);
			}
		    } catch (java.lang.Throwable e1) {
		    }
		}
	    }
	}

	Thread.currentThread().yield();
	if (((getAmode() == Source.ACCESS_CREATE) ||
	     (getAmode() == Source.ACCESS_APPEND))) {
	    try {
		writeToArchive();

	    } catch (java.lang.Throwable e) {
		if ((getLog() != null) && (getLog() != this)) {
		    try {
			if (e instanceof java.lang.Exception) {
			    getLog().addException(Log.STANDARD,
						  getLogClass(),
						  getName(),
						  (java.lang.Exception) e);
			} else if (e instanceof java.lang.Error) {
			    getLog().addError(Log.STANDARD,
					      getLogClass(),
					      getName(),
					      (java.lang.Error) e);
			}
		    } catch (java.lang.Throwable e1) {
		    }
		}
	    }
	}

	Rmap entry;
	try {
	    while (getNchildren() > 0) {
		entry = getChildAt(0);
		removeChildAt(0);
		entry.nullify();
	    }
	} catch (java.lang.Exception e) {
	}

	if (!getAkeep() && (getAmode() != Source.ACCESS_NONE)) {
	    // Destroy the archive if we aren't supposed to keep it around and
	    // we were accessing it.
	    try {
		deleteArchive();
	    } catch (java.lang.Throwable e) {
	    }
	}
    }

    /**
     * Stops the <code>SourceHandler</code> on an I/O exception in the
     * <code>RCO</code>.
     * <p>
     * If there is an open archive that is to be kept, then this method
     * forces a detach rather than a stop.
     * <p>
     *
     * @author Ian Brown
     *
     * @return did the stop actually occur?
     * @exception java.lang.InterruptedException
     *		  thrown if this method is interrupted.
     * @since V2.1
     * @version 04/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2003  INB	Created.
     *
     */
    public boolean stopOnIOException()
	throws java.lang.InterruptedException
    {
	boolean stoppedR = false;

	if ((getAmode() != Source.ACCESS_NONE) && getAkeep()) {
	    stoppedR = true;
	    setCkeep(true);
	    stop((ClientHandler) this);
	}

	return (stoppedR);
    }

    /**
     * Synchronizes with the client-side.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>RBO</code> has not been connected to a
     *		      server,</li>
     *		  <li>the <code>RBO</code> is not running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the synchronization is interrupted.
     * @see #acceptFrame(boolean)
     * @see #addChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 01/02/2001  INB	Created.
     *
     */
    public void synchronizeWclient()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long startAt = System.currentTimeMillis();
	long now;
	synchronized (this) {
	    while (isRunning() &&
		   ((getFrame() != null) ||
		    getAddingAFrame() ||
		    getAcceptingAFrame())) {
		wait(TimerPeriod.NORMAL_WAIT);

		if (((now = System.currentTimeMillis()) - startAt) >=
		    TimerPeriod.LOCK_WAIT) {
		    try {
			throw new Exception
			    (System.currentTimeMillis() + " " +
			     getName() + 
			     " Warning:  waiting a long time, may be a problem;" +
			     " blocked in synchronizeWclient waiting " +
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

	RingBuffer rb;
	java.util.Hashtable lrbsT = (java.util.Hashtable) rbsToSync.clone();
	rbsToSync.clear();
	java.util.Enumeration lrbs;
	for (lrbs = lrbsT.keys(); lrbs.hasMoreElements(); ) {
	    rb = (RingBuffer) lrbs.nextElement();
	    startAt = System.currentTimeMillis();
	    synchronized (rb) {
		while ((rb.getFrame() != null) ||
			rb.getAddingAFrame() ||
			rb.getAcceptingAFrame()) {
		    rb.wait(TimerPeriod.NORMAL_WAIT);

		    if (((now = System.currentTimeMillis()) - startAt) >=
			TimerPeriod.LOCK_WAIT) {
			try {
			    throw new Exception
				(System.currentTimeMillis() + " " +
				 getName() + "/RB" +
				 rb.getIndex() +
			         " Warning:  waiting a long time, may be a problem;" +
				 " blocked in synchronizedWclient waiting " +
				 "for work to complete: " +
				 rb.getFrame() + " " +
				 rb.getAcceptingAFrame() + " " +
				 rb.getAddingAFrame());
			} catch (Exception e) {
			    e.printStackTrace();
			    startAt = now;
			}
		    }
		}
	    }
	}

	if (!isRunning()) {
	    throw new java.io.EOFException
		("Source stopped before synchronization could occur.");
	}

	return;
    }

    /**
     * Stops the objects in the <code>AwaitNotification</code> list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2001  INB	Created.
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
     * Unlocks this <code>RBO</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/14/2001  INB	Created.
     *
     */
    void unlockRead()
	throws java.lang.InterruptedException
    {
	registrationDoor.unlockRead();
    }

    /**
     * Unlocks this <code>RBO</code> hierarchy for write access.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 10/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/27/2003  INB	Created.
     *
     */
    void unlockWrite()
	throws java.lang.InterruptedException
    {
	registrationDoor.unlock();
    }

    /**
     * Updates this <code>RBO</code> using the input
     * <code>ClientInterface</code>. 
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientInterfaceI  the <code>ClientInterface</code> providing the
     *				update.
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the <code>ClientInterface</code> is not a
     *		  <code>RBO</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2001  INB	Created.
     *
     */
    public void update(ClientInterface clientInterfaceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!(clientInterfaceI instanceof SourceInterface)) {
	    throw new java.lang.IllegalArgumentException
		(clientInterfaceI + " cannot be used to update " + this);
	}

	// Update the general fields.
	super.update(clientInterfaceI);
	
	// Grab the <code>SourceInterface</code> so that we can figure out what
	// to update.
	SourceInterface source = (SourceInterface) clientInterfaceI;

	if (source.getCframes() != getCframes()) {
	    // If the number of cache frames has been changed, update the
	    // local value.
	    setCframes(source.getCframes());
	}

	if (source.getCsize() != getCsize()) {
	    // If the size of the cache has been changed, update the local
	    // value.
	    setCsize(source.getCsize());
	}

	if (source.getAframes() != getAframes()) {
	    // If the number of archive frames has been changed, update the
	    // local value.
	    setAframes(source.getAframes());
	}

	if (source.getAmode() != getAmode()) {
	    // If the number of archive mode has been changed, update the
	    // local value.
	    setAmode(source.getAmode());
	}

	if (source.getAsize() != getAsize()) {
	    // If the size of the archive has been changed, update the local
	    // value.
	    setAsize(source.getAsize());
	}

	if (source.getNfs() != getNfs()) {
	    // If the number of cache <code>FrameSets</code> has been changed,
	    // update the local value.
	    setNfs(source.getNfs());
	}
    }

    /**
     * Updates the <code>Archive</code> by adjusting its size to match the
     * user's specified values.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem calculating the size of a
     *		  <code>FrameSet</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 03/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2010  MJM  Add FileSets for reconnecting sources - avoid unexpectedly small archive
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 12/06/2001  INB	Created.
     *
     */
    private final void updateArchiveFromUser()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//EMF 12/6/06: always do what the user asked for, not what the archive is,
//             but don't trim existing data
      if (getAframes() > 0) {
	    // If the user specified a maximum size in terms of frames, try to
	    // adjust the size of the <code>Archive</code> by increasing or
	    // decreasing the number of <code>FileSets</code> stored.

 /*
	    double fsSize = aFrSFileSet*cFrFrameSet;

	    int aFS = Math.max(2,(int) Math.ceil(getAframes()/fsSize));
  System.err.println("Debug: getAframes(): "+getAframes()+", fsSize: "+fsSize+", aFS: "+aFS+", aFileSets: "+aFileSets);
 
	    if (aFS < aFileSets) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Reducing archive size from " + aFileSets +
		     " to " + aFS +
		     " filesets. Some existing data may be lost.");
	    }
	    aFileSets = aFS;
*/
  	    double fsSize = aFrSFileSet*cFrFrameSet;	// mjm
  	    
		setUpArchive();
	    RingBuffer rb;
	    for (int idx = 0,
		     eIdx = getNchildren();
		 	idx < eIdx;
		 	++idx) {
	    		rb = (RingBuffer) getChildAt(idx);
	    		Archive archive=rb.getArchive();
	    		
	    		// following patch presumes that prior filesets may have only a single frame per fileset 
	    		// (conservatively keep maybe lots-more vs trim lots-less)	    		
	    		int nOFS = archive.getNchildren();		// number old file sets.  mjm 4/10
	    	    int aFS = nOFS - (int)(nOFS/fsSize);	// tweek down presuming at least 1 frame per old fileset.  mjm 4/10
 //System.err.println("ajusted FileSets: "+(aFileSets+aFS)+" ("+aFileSets+"+"+aFS+")");	// mjm debug
	    		if (archive!=null) {
	    			//rb.getArchive().setMs(aFS);
	    			//rb.getArchive().trim();
//	    			archive.setMs(aFileSets);	
	    			archive.setMs(aFileSets + aFS);		// mjm 4/10:  shrinking buffer fix?
	    			archive.setMeps(aFrSFileSet);
	    		}
	    }
	  }
   }

    /**
     * Updates the <code>Cache</code> by adjusting its size to match the
     * user's specified values.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem calculating the size of a
     *		  <code>FrameSet</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 03/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/01/2006  MJM 	Changed minimum cFS to 1 (from 2)
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 12/06/2001  INB	Created.
     *
     */
    private final void updateCacheFromUser()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//EMF 12/6/06: always do what the user asked for, not what the loaded archive
//             says, but don't trim existing data
	if (getCframes() > 0) {
	    // If the user specified a maximum size in terms of frames, try to
	    // adjust the size of the <code>Cache</code> by increasing or
	    // decreasing the number of <code>FileSets</code> stored.
/*
	    double fsSize = cFrFrameSet;
	    int cFS = Math.max(1,(int) Math.ceil(getCframes()/fsSize));  // was 2,ceil mjm 8/1/06

	    if (cFS < cFrameSets) {
		getLog().addMessage
		    (getLogLevel(),
		     getLogClass(),
		     getName(),
		     "Reducing cache size from " + cFrameSets +
		     " to " + cFS +" framesets.");
	    }
	    cFrameSets = cFS;
*/
setUpCache();
	    RingBuffer rb;
	    for (int idx = 0,
		     eIdx = getNchildren();
		 idx < eIdx;
		 ++idx) {
		rb = (RingBuffer) getChildAt(idx);
//EMF 11/10/06: avoid null pointers...
Cache cache=rb.getCache();
if (cache!=null) {
		//rb.getCache().setMs(cFS);
		//rb.getCache().trim();
cache.setMs(cFrameSets);
cache.setMeps(cFrFrameSet);
}
	    }
	}
    }

    /**
     * Updates the <code>Registration</code> for the <code>RBO</code>.
     * <p>
     * The registration map is used to determine what channels have data within
     * the <code>RBO</code>.  The information contained with the regimentation
     * map (a hierarchy of <code>Rmaps</code>) is:
     * <p><ul>
     *    <li>The names of the channels of data contained within the
     *	      <code>RBO</code>,</li>
     *    <li>The time limits (start + duration) of the data stored for the
     *	      channel,</li>
     *    <li>The automatically generated meta-data describing the data stored
     *	      for the channel, and</li>
     *    <li>The application-provided user meta-data for the channel.</li>
     * </ul><p>
     * The automatically generated part of the meta-data and time information
     * is generated by calculating it at the lowest level
     * (<code>FrameSets</code>).  At each level above that, the registration
     * maps for the levels below are combined to produce a single registration
     * map.  Thus, registration maps are kept at each of the following levels:
     * <p><dir>
     *    <li>RBO<dir>
     *	      <li>RingBuffer(s)<dir>
     *		  <li>Cache<dir>
     *		      <li>FrameSet(s)</li>
     *		  </dir></li>
     *		  <li>Archive<dir>
     *		      <li>FileSet(s)<dir>
     *			  <li>FrameSet(s)</li>
     *		      </dir></li>
     *		  </dir></li>
     *        </dir></li>
     *    </dir></li>
     * </dir><p>
     * The process of updating the automatically generated registration maps is
     * optimized to reduce the amount of work actually performed to a minimum.
     * This operation proceeds as follows:
     * <p><ol>
     *    <li>If there is a registration map at the RBO and there have been no
     *	      changes to any the <code>RingBuffers</code> since it was last
     *	      updated, then the registration map is assumed to be
     *	      up-to-date,</li>
     *    <li>Work on the registration map of each <code>RingBuffer</code>
     *	      changed since the last update,
     *	      <br><ol>
     *            <li>If the <code>RingBuffer</code> has a <code>Cache</code>,
     *		      update its registration,
     *		      <br><ol>
     *			  <li>Work on the registration map for each of the
     *			      <code>FrameSets</code> in the <code>Cache</code>
     *			      starting with the first one listed as new,
     *			      <br><ol>
     *				  <li>If the <code>FrameSet</code> contains any
     *				      new frames of data, update its
     *				      registration map channel list and limits
     *				      to reflect the contents of the new
     *				      frame(s),</li>
     *				  <li>If a change was made to the registration
     *				      map, return an indication that a minor
     *				      registration map update has occured.</li>
     *				  <li>If no changes were made to the
     *				      registration map, then note that in the
     *				      return status,</li>
     *			      </ol><br>
     *			  </li>
     *			  <li>If a major update occured, then start with a
     *			      clean registration map for the
     *			      <code>Cache</code>.  All <code>FrameSets</code>
     *			      are considered "changed",</li>
     *			  <li>Add the changed registration maps of the
     *			      <code>FrameSets</code> in the <code>Cache</code>
     *			      to the registration of the <code>Cache</code> to
     *			      form a single registration map, collapsing
     *			      redundancies (such as channel name
     *			      hierarchies),</li>
     *			  <li>If all of the <code>FrameSets</code> were new
     *			      (which generally indicates that something has
     *			      been deleted from the <code>Cache</code>) or if
     *			      there was no registration map for the
     *			      <code>Cache</code>, return an indication that a
     *			      major registration map update has occured,</li>
     *			   <li>If some of the <code>FrameSets</code> were
     *			       changed, then return an indication that a minor
     *			       update has occured,</li>
     *			    <li>If no changes were made to the registration
     *				map, then note that in the return status,</li>
     *		      </ol><br>
     *		  </li>
     *		  <li>If the <code>RingBuffer</code> has an
     *		      <code>Archive</code>, update its registration.  This
     *		      works likes the <code>Cache</code> update, except that
     *		      there are <code>FileSets</code> in the
     *		      <code>Archive</code> rather than <code>FrameSets</code>.
     *		      With the <code>FileSets</code> are
     *		      <code>FrameSets</code>.</li>
     *		   <li>If a major update occured, then clear the registration
     *		       map for the <code>RingBuffer</code>.  Both the
     *		       <code>Cache</code> and the <code>Archive</code> are
     *		       considered "changed",/li>
     *		   <li>Add the changed registration maps of the
     *		       <code>Cache</code> and the <code>Archive</code> to that
     *		       of the <code>RingBuffer</code> to form a single
     *		       registration map, collapsing redundancies (such as
     *		       channel name hierarchies),</li>
     *		   <li>If either the <code>Cache</code> or the
     *		       <code>Archive</code> had a major update or if there was
     *		       no registration map, return an indication that a major
     *		       update occured,</li>
     *		   <li>If either the <code>Cache</code> or the
     *		       <code>Archive</code> had a minor update, then return an
     *		       indication that a minor update occured,</li>
     *	      </ol><br>
     *    <li>If a major update occured, then clear the registration map of the
     *	      <code>RBO</code.  All of the <code>RingBuffers</code> are
     *	      considered "changed",</li>
     *    <li>Add the changed registration maps of the <code>RingBuffers</code>
     *	      to the registration map of the <code>RBO</code> to form a single
     *	      registration map, collapsing redundancies (such as channel name
     *	      hierarchies),</li>
     *	  <li>Add the application-supplied user meta-data (termed the
     *	      "explicit registration") to the <code>RBO's</code> registration
     *	      map.  The user meta-data overrides automatic meta-data where both
     *	      appear.</li>
     * </ol><p>
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
     * @since V2.0
     * @version 08/31/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/31/2004  INB	Added documentation.
     * 08/26/2004  MJM  Additional update explicit registration (could get
     *			lost).
     * 12/22/2003  INB	Handle <code>null RingBuffer</code>.
     * 11/20/2003  INB	Eliminated improper <code>getChildAt</code> call in
     *			the update some <code>RingBuffers</code> loop.
     * 11/12/2003  INB	Added location of locks.
     * 10/29/2003  INB	Check for updated information before locking.
     * 10/27/2003  INB	Use write lock methods.
     * 10/15/2003  INB	Set the size of the <code>rbUpdated</code> field based
     *			on the maximum of the number of children and the number
     *			of ring buffers updated.  Consitently use the index of
     *			the for loop as the position in the list.
     * 04/03/2003  INB	Ensure that the explicit registraiton is added in.
     * 03/14/2003  INB	Made this method final.
     * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>.
     * 02/12/2001  INB	Created.
     *
     */
    public final void updateRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// updated is 3 value flag
	// -1 :  significant update (e.g. ring buffer looped, info deleted)
	// 0  :  no update needed
	// 1  :  minor update (e.g. only added data, no deletions)
	int updated = (getLastRegistration() == Long.MIN_VALUE) ? -1 : 0;

	if ((updated == 0) && (rbsChanged.size() == 0)) {
	    // Registration updated and nothing changed.
	    return;
	}

	try {
	    lockWrite("RBO.updateRegistration");

	    RingBuffer rb;
	    // Grab copy of ringbuffers (in this rbo) changed-list.
	    java.util.Hashtable lrbsT = (java.util.Hashtable)
		rbsChanged.clone();
	    rbsChanged.clear();

	    int[] rbUpdated = new int[Math.max(getNchildren(),lrbsT.size())];
	    int idx;
	    java.util.Enumeration lrbs;
	    // loop thru changed ring buffers
	    for (idx = 0, lrbs = lrbsT.keys(); lrbs.hasMoreElements(); ++idx) {
		rb = (RingBuffer) lrbs.nextElement();
		rbUpdated[idx] = rb.updateRegistration();

		// Check if registration map of ring buffer is newer than rbo
		// reg map.
		if ((rbUpdated[idx] == 0) &&
		    (rb.getLastRegistration() > getLastRegistration())) {
		    rbUpdated[idx] = rb.getLastRegistrationResult();
		}
		if (rbUpdated[idx] == -1) {
		    updated = -1;
		} else if ((rbUpdated[idx] == 1) && (updated == 0)) {
		    updated = 1;
		}
	    }
	    if (getRegistered() == null) {
		updated = -1;
	    }

	    if (updated == -1) {
		// Significant change, need complete new regmap.
		if (getNchildren() == 0) {
		    if (getExplicitRegistration() == null) {
			setRegistered(new Registration());
		    } else {
			setRegistered
			    ((Registration) getExplicitRegistration().clone());
		    }
		    return;
		}
		rb = (RingBuffer) getChildAt(0);
		if ((rb == null) || (rb.getRegistered() == null)) {
		    if (getExplicitRegistration() == null) {
			setRegistered(new Registration());
		    } else {
			setRegistered
			    ((Registration) getExplicitRegistration().clone());
		    }
		    return;
		}
		setRegistered((Registration) rb.getRegistered().clone());
		int eIdx;
		for (idx = 1,
			 eIdx = getNchildren();
		     idx < eIdx;
		     ++idx) {
		    rb = (RingBuffer) getChildAt(idx);
		    if ((rb == null) || (rb.getRegistered() == null)) {
			return;
		    }
		    ((Registration) getRegistered()).updateRegistration
			(rb.getRegistered(),
			 false,
			 true);
		}
		if (getExplicitRegistration() != null) {  // i.e. "user data"
		    ((Registration) getRegistered()).updateRegistration
			(getExplicitRegistration(),
			 false,
			 true);
		}

	    } else if (updated == 1) {
		for (idx = 0, lrbs = lrbsT.keys();
		     lrbs.hasMoreElements();
		     ++idx) {
		    rb = (RingBuffer) lrbs.nextElement();
		    if (rbUpdated[idx] != 0) {
			if ((rb == null) || (rb.getRegistered() == null)) {
			    return;
			}
			((Registration) getRegistered()).updateRegistration
			    (rb.getRegistered(),
			     false,
			     true);
		    }
		}
		// mjm 8/26/04 update user data here, also.  otherwise could get tromped
		if (getExplicitRegistration() != null) {  // i.e. "user data"
		    ((Registration) getRegistered()).updateRegistration
			(getExplicitRegistration(),
			 false,
			 true);
		}
	    }

	} finally {
	    if (updated != 0) {
		setLastRegistration(System.currentTimeMillis());
	    }

	    unlockWrite();
	}
    }

    /**
     * Validates the archive for this <code>RBO</code>.
     * <p>
     * This method attempts to automatically recover as much of the
     * <code>Archive(s)</code> associated with this <code>RBO</code> as
     * possible.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Archive(s)</code> cannot be validated or
     *		  recovered.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 11/16/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/16/2006  JPW	Change calls from Vector.clear() to
     *			Vector.removeAllElements(); this keeps the
     *			code more Java 1.1.4 compliant (for J# compile).
     * 08/03/2006  MJM  Improved robustness of Archive recovery.
     * 02/18/2003  INB	Created from the <code>Archive.validate()</code>
     *			method.
     *
     */
    final boolean validateArchive()
	throws java.io.InterruptedIOException,
	       java.lang.InterruptedException
    {
	long after = Long.MIN_VALUE,
	     before = Long.MAX_VALUE;
	Log log = getLog();
	if (log == getParent()) {
	    log = null;
	} else {
	    try {
		log.addMessage(getLogLevel() + 1,
			       getLogClass(),
			       getParent().getName(),
			       "Validating archive.");
	    } catch (java.lang.Exception e) {
	    }
	}
	
	{
	    String archDir = getArchiveDirectory();
	    java.io.File dir = new java.io.File(archDir);
	    if (!dir.exists()) {
		// Assumes 'directory' starts with "./" or ".\".  Remove to
		//  try absolute:
		archDir = archDir.substring(2);
		dir = new java.io.File(archDir);
		if (dir.exists())
		    // Set override so that archive can be found:
		    archiveDirectoryOverride = archDir;
		// Otherwise, allow existing code to fail.
	    }
	}	
        
	// Ensure that there is an <code>RBO</code> seal. Without it, the
	// <code>Archive(s)</code> definitely need to be recovered.
	Seal rboSeal = null;

	try {
	    rboSeal = Seal.validate
		(getArchiveDirectory(),
		 after,
		 before);
	} catch (com.rbnb.api.InvalidSealException e) {
	}
	if (rboSeal != null) {
	    before = rboSeal.getAsOf();
	}

	// Read and validate the <code>Seals</code> for the
	// <code>RingBuffers</code>. If all of the <code>Seals</code> are
	// valid, then the <code>Archive(s)</code> can be read without any
	// further trouble.
	java.util.Vector validSeals = new java.util.Vector(),
			 invalidSeals = new java.util.Vector();
	boolean valid = (validateRingBuffers(log,
					     after,
					     before,
					     validSeals,
					     invalidSeals) &&
			 (rboSeal != null));
	
	if(!valid) {   // MJM 8/03/06:  if any not valid, make them all not valid. 
	               // Grope to make Archive Recovery more robust
	    before = Long.MAX_VALUE;
	    // JPW 11/16/2006: Change calls from clear() to removeAllElements()
	    //                 (they are functionally equivalent).  This keeps the
	    //                 code more Java 1.1.4-compliant (for J# compile).
	    validSeals.removeAllElements(); invalidSeals.removeAllElements();
	    validateRingBuffers(log,after,before,validSeals,invalidSeals);
	}

	if (!valid) {
	    // If the <code>RingBuffer(s)</code> could not be validated, try
	    // recovering them using the information found in the valid/invalid
	    // <code>Seals</code> lists.
	    recoverArchive(validSeals,invalidSeals);

	} else if (log != null) {
	    try {
		log.addMessage(getLogLevel() + 1,
			       getLogClass(),
			       getParent().getName(),
			       "Archive validated.");
	    } catch (java.lang.Exception e) {
	    }
	}

	return (valid);
    }

    /**
     * Validates the <code>RingBufferss</code> in the <code>RBO</code>.
     * <p>
     * This method reads all of the <code>RingBuffer Archives</code> from the
     * <code>RBO's</code> directory, whether or not they are considered to
     * be part of the <code>RBO's Archives</code>. It ensures that they are
     * consistent with respect to each other and then checks them against the
     * <code>RBO's Archive</code> as a whole.
     * <p>
     *
     * @author Ian Brown
     *
     * @param logI	    the server <code>Log</code>.
     * @param afterI	    the minimum valid time.
     * @param beforeI	    the maximum valid time.
     * @param validSealsO   the list of valid <code>RingBuffers</code> and
     *			    their <code>Seals</code>.
     * @param invalidSealsO the list of invalid <code>RingBuffers</code> and
     *			    their <code>Seals</code>.
     * @return is the <code>RBO</code> valid?
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Archive</code> is in such a state that it
     *		  cannot possibly be validated.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 04/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/30/2003  INB	Flag ring buffers and filesets as RBNB files.
     * 02/18/2003  INB	Created from the
     *			<code>Archive.validateFileSets()</code> method.
     */
    private final boolean validateRingBuffers(Log logI,
					      long afterI,
					      long beforeI,
					      java.util.Vector validSealsO,
					      java.util.Vector invalidSealsO)
	throws java.io.InterruptedIOException,
	       java.lang.InterruptedException
    {
	boolean validR = true;

	long after = afterI,
	     before = beforeI;
	java.io.File[] files;
	String directory = getArchiveDirectory();
	java.io.File archive = new java.io.File(directory);
	Directory asDirectory = new Directory(archive);
	        
	// Ensure that the <code>RBO's</code> directory is valid. If
	// not, we've got a serious problem.
	try {
	    if (!archive.exists()) {
		throw new java.lang.IllegalStateException
		    ("Cannot find archive directory " + directory);
	    } else if ((files = asDirectory.listFiles()) == null) {
		throw new java.lang.IllegalStateException
		    ("Archive location " + directory + " is not a directory.");
	    }
	} catch (java.io.IOException e) {
	    throw new java.lang.IllegalStateException
		("Archive in " + directory + " is in a bad state.\n" +
		 e.getMessage());
	}

	// Find the summary file and create a sorted list of the
	// <code>RingBuffer</code> directories.
	boolean summary = false;
	boolean foundFileSet = false;
	boolean foundRBNBFile = false;
	java.util.Vector ringBuffers = new java.util.Vector();
	RingBuffer rb,
		   other;
	for (int idx = 0; idx < files.length; ++idx) {
	    // Loop through the files.

	    try {
		String name = files[idx].getName();

		if (!files[idx].isDirectory()) {
		    // If the file isn't a directory, then check for standard
		    // files.
		    if (name.equalsIgnoreCase("summary.rbn")) {
			// The only one of definite interest is the summary.
			foundRBNBFile =
			    summary = true;

		    } else if (name.endsWith(".rbn")) {
			// We've found an RBNB file.
			foundRBNBFile = true;
		    }

		} else if (name.length() >= 3) {
		    // If this is a directory, then we need to check it out.
		    String type = name.substring(0,2);

		    if (!type.equals("RB") && !type.equals("FS")) {
			// This is some random directory. We ignore it.
			continue;
		    }

		    // This might be a <code>RingBuffer</code> or
		    // <code>FileSet</code> directory.
		    long rbIndex = 0;
		    try {
			rbIndex = Long.parseLong(name.substring(2));
		    } catch (java.lang.NumberFormatException e) {
			// Nope, not a legal one anyway. It is some random
			// directory. We ignore it.
			continue;
		    }

		    Directory fAsDirectory = new Directory(files[idx]);
		    if (fAsDirectory.listFiles() == null) {
			// If this directory is empty, ignore it.
			continue;
		    }
		    foundRBNBFile = true;

		    if (type.equals("FS")) {
			// If this is a potential <code>FileSet</code>, then
			// flag that fact.
			foundFileSet = true;

		    } else {
			// If this is a potential <code>RingBuffer</code>,
			// attempt to add it to the list to check. We put these
			// <code>RingBuffers</code> into numerical order,
			// which, unfortunately, is not the natural sort order,
			// so we have to do it by hand.
			rb = new RingBuffer(rbIndex);
			rb.setParent(this);
                        rb.setupTrimTimes(cacheflush,cachetrim,archiveflush,archivetrim);
			int lo,
			    hi,
			    idx1;
			for (lo = 0,
				 hi = ringBuffers.size() - 1,
				 idx1 = (lo + hi)/2;
			     (lo <= hi);
			     idx1 = (lo + hi)/2) {
			    other = (RingBuffer) ringBuffers.elementAt(idx1);
			    
			    if (rbIndex < other.getIndex()) {
				hi = idx1 - 1;
			    } else if (rbIndex > other.getIndex()) {
				lo = idx1 + 1;
			    } else {
				break;
			    }
			}
			ringBuffers.insertElementAt(rb,lo);
		    }
		}
	    } catch (com.rbnb.api.SerializeException e) {
	    } catch (java.io.IOException e) {
	    }
	}

	if (!foundRBNBFile) {
	    // If there are no RBNB files, then we can't recover this.
	    ringBuffers.removeAllElements();

	} else if (foundFileSet && (ringBuffers.size() == 0)) {
	    // If we don't have any <code>RingBuffers</code>, but we've seen at
	    // least one <code>FileSet</code>, then we've probably got a V2.0
	    // archive. Try to convert it over.
	    convertFromV2p0(logI,
			    afterI,
			    beforeI,
			    files,
			    validSealsO,
			    invalidSealsO);
	    validR = false;

	} else {
	    // At this point, we supposedly have a sorted list of
	    // <code>RingBuffers</code>. Validate them in order.
	    java.util.Vector rbValidSeals = null;
	    java.util.Vector rbInvalidSeals = null;
	    Seal theSeal;
	    for (int idx = 0,
		     endIdx = ringBuffers.size();
		 idx < endIdx;
		 ++idx) {
		rb = (RingBuffer) ringBuffers.elementAt(idx);
		try {
		    rbValidSeals = new java.util.Vector();
		    rbInvalidSeals = new java.util.Vector();
		    if ((theSeal = rb.validateArchive
			 (after,
			  before,
			  rbValidSeals,
			  rbInvalidSeals)) == null) {
			throw new com.rbnb.api.InvalidSealException
			    (theSeal,
			     after,
			     before);
		    } else {
			validSealsO.addElement(rb);
			validSealsO.addElement(theSeal);
			after = theSeal.getAsOf();
		    }

		} catch (java.lang.Exception e) {
		    invalidSealsO.addElement(rb);
		    invalidSealsO.addElement(rbValidSeals);
		    invalidSealsO.addElement(rbInvalidSeals);
		    invalidSealsO.addElement(e);
		    validR = false;
		}
	    }

	    // If we lack a summary, then the <code>RBO's Archives</code> are
	    // not valid.
	    validR = validR && summary;
	}

	return (validR);
    }

    /**
     * Writes the <code>RBO's Archive</code> to the archive files.
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
     * @see #readOffsetsFromArchive()
     * @see #readSkeletonFromArchive()
     * @since V2.1
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Handle case where there are no children when we try to
     *			calculate the number of <code>RingBuffers</code>.
     * 11/12/2003  INB	Added location of locks.
     * 10/27/2003  INB	Use write lock methods. Ensure that we close all files.
     * 09/09/2003  INB	Reordered the archive writes so that data is always
     *			written before the headers.
     * 04/17/2003  INB	If it exists, write the <code>Username</code> to the
     *			archive.
     * 02/18/2003  INB	Created from the corresponding <code>Archive</code>
     *			method.
     *
     */
    final void writeToArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the door.
	    lockWrite("RBO.writeToArchive");

	    // Update the <code>Registration</code>.
	    updateRegistration();

	    int endIdx;
	    if ((endIdx = getNchildren()) > 0) {
		// Write out all of the <code>RingBuffers</code>.
		RingBuffer rb;
		for (int idx = 0; idx < endIdx; ++idx) {
		    rb = (RingBuffer) getChildAt(idx);
		    if (rb != null) {
			rb.writeToArchive();
		    }
		}

		// Open the <code>Registration</code> files for the
		// <code>RBO</code> as a whole.
		String directory = getArchiveDirectory() + Archive.SEPARATOR;
		java.io.FileOutputStream hfos =
		    new java.io.FileOutputStream(directory + "reghdr.rbn"),
					 dfos =
		    new java.io.FileOutputStream(directory + "regdat.rbn");
		OutputStream hos = new OutputStream(hfos,
						    Archive.BINARYARCHIVE,
						    0);
		BuildFile.loadBuildFile(hos);
		DataOutputStream dos = new DataOutputStream(dfos,0);

		// Write the <code>Registration</code> out.
		getRegistered().writeData(dos);
		getRegistered().write(Archive.ARCHIVE_PARAMETERS,
				      Archive.ARC_REG,
				      hos,
				      dos);
		if (getExplicitRegistration() != null) {
		    getExplicitRegistration().writeData(dos);
		    getExplicitRegistration().write(Archive.ARCHIVE_PARAMETERS,
						    Archive.ARC_EXP,
						    hos,
						    dos);
		}

		// Close the files.
		dos.close();
		dfos.close();
		hos.close();
		hfos.close();

		// Open the summary file for the archive as a whole.
		hfos = new java.io.FileOutputStream
		    (directory + "summary.rbn");
		java.io.PrintStream pos = new java.io.PrintStream(hfos);

		// Write the summary.
		pos.print("Date: ");
		pos.println((new java.util.Date()).toString());
		int nValue = getNchildren() - 1;
		rb = null;
		for (; (rb == null) && (nValue >= 0); --nValue) {
		    try {
			rb = (RingBuffer) getChildAt(nValue);
		    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
			nValue = 0;
		    }
		}
		if (rb != null) {
		    pos.println("Number Of RingBuffers: " +
				(((RingBuffer) getChildAt(getNchildren() -
							  1)).getIndex() + 1));
		}
		int sets = 0;
		int eps = 0;
		String header = null;
		for (int idx = 0; idx < 2; ++idx) {
		    switch (idx) {
		    case 0:
			header = "Cache ";
			sets = cFrameSets;
			eps = cFrFrameSet;
			break;
		    case 1:
			header = "Archive ";
			sets = aFileSets;
			eps = aFrSFileSet;
			break;
		    }
		    pos.println(header + "Sets: " + sets);
		    pos.println(header + "Elements/Set: " + eps);
		}
		if (getUsername() != null) {
		    pos.print("Username: " +
			      ((getUsername().getUsername() != null) ?
			       getUsername().getUsername() : "") +
			      ((getUsername().getPassword() != null) ?
			       "," + getUsername().getPassword() :
			       ""));
		}

		// Close the summary file.
		pos.close();
		hfos.close();

		// Seal the <code>RBO</code>.
		Seal.seal(getArchiveDirectory());
	    }

	} finally {
	    // Unlock the door.
	    unlockWrite();
	}
    }
}
