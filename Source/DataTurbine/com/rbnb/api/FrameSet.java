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
 * Provides a single unit for storage within either the <code>Cache</code> or
 * the <code>FileSet</code> objects.
 * <p>>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.FileSet
 * @see com.rbnb.api.Cache
 * @since V2.0
 * @version 01/06/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/08/2006  EMF      Use markers on archive recovery to reduce memory needs.
 * 01/06/2004  INB	The <code>reduceToSkeleton</code> method always does
 *			the reduction, after ensuring that a parent is
 *			updated.  Always do a reduce in <code>clear</code>.
 * 12/11/2003  INB	Added <code>RequestOptions</code> to
 *			<code>TimeRelativeRequest</code> handling to allow the
 *			code to do the right thing for
 *			<code>extendStart</code>.
 * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 11/05/2003  INB	Added <code>afterTimeRelaive</code> and
 *			</code>beforeTimeRelative</code>.
 * 11/03/2003  INB	Added <code>matchTimeRelative</code> and
 *			<code>loadFromArchive</code>.
 * 10/22/2003  INB	Force read/write access when writing.
 * 10/17/2003  INB	Ensure that a read in <code>FrameSet</code> has its
 *			own <code>Door</code>.
 * 10/14/2003  INB	Put the frame index of reframed <code>FrameSets</code>
 *			into a top level child rather than the
 *			<code>FrameSet</code> itself.
 * 09/30/2003  INB	Find the first frame index in the <code>FrameSet</code>
 *			and use that as the value for the reframed
 *			<code>FrameSet</code>.
 * 09/30/2003  INB	Summarize the reframed <code>FrameSet</code> rather
 *			than the original.
 * 09/09/2003  INB	Reordered the archive writes so that data is always
 *			written before the headers.  Modify the offset methods
 *			to be accessible from outside here.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/06/2003  INB	Added method <code>reduceToSkeleton</code>.
 * 04/24/2003  INB	Mark the <code>FileSet</code> as out-of-date before
 *			actually writing information to it.
 * 03/28/2003  INB	Eliminated unnecessary locks.
 * 03/13/2003  INB	Reduced the amount of time that the
 *			<code>FrameSet</code> has the files open.
 * 02/19/2003  INB	Modified to handle multiple <code>RingBuffers</code>
 *			per <code>RBO</code>.
 * 02/09/2001  INB	Created.
 *
 */
final class FrameSet
    extends com.rbnb.api.FrameManager
{
    /**
     * the parent <code>FileSet</code>.
     * <p>
     * A <code>FrameSet</code> can be a child of both a <code>Cache</code>
     * and a <code>FileSet</code> object. When that is true, the
     * <code>Rmap</code> getParent() method returns the <code>Cache</code>,
     * so the <code>FileSet</code> gets stored here.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/08/2001
     */
    private FileSet filesetParent = null;

    /**
     * the file offset of the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #headerOffset
     * @see #regDataOffset
     * @see #regHeaderOffset
     * @since V2.0
     * @version 03/12/2001
     */
    private long dataOffset = -1;

    /**
     * the file offset of the header.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #dataOffset
     * @see #regDataOffset
     * @see #regHeaderOffset
     * @since V2.0
     * @version 03/12/2001
     */
    private long headerOffset = -1;

    /**
     * the index of the last child registered.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/18/2001
     */
    private int lastRegistrationIndex = -1;

    /**
     * the file offset of the <code>Registration</code> data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #dataOffset
     * @see #headerOffset
     * @see #regHeaderOffset
     * @since V2.0
     * @version 03/12/2001
     */
    private long regDataOffset = -1;

    /**
     * the file offset of the <code>Registration</code> header.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #dataOffset
     * @see #headerOffset
     * @see #regDataOffset
     * @since V2.0
     * @version 03/12/2001
     */
    private long regHeaderOffset = -1;

    //EMF 6/19/06
    // 2007/08/06  WHF  Since not currently used, removed to save memory.
//    private DataArray da = null;

    // Private class constants:
    /*private*/ final static String[] ARCHIVE_PARAMETERS = {
				    "FST",
				    "REG"
				};

    private final static int  ARC_FST = 0;
    /*private*/ final static int  ARC_REG = 1;

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
     * @see #FrameSet(long)
     * @since V2.0
     * @version 03/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    FrameSet()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
    }

    /**
     * Class constructor to build a <code>FrameSet</code> from a frameset
     * index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param framesetIndexI  the <code>FrameSet</code> index.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #FrameSet()
     * @since V2.0
     * @version 03/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    FrameSet(long framesetIndexI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(framesetIndexI);
    }

    /**
     * Builds a request for data for the requested channels starting at the
     * beginning of this <code>FrameSet</code> (which is after the time
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
     * 11/04/2003  INB	Created.
     *
     */
    final TimeRelativeResponse afterTimeRelative(TimeRelativeRequest requestI,
						 RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(1);

	/*
	System.err.println("FrameSet.afterTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nSummary: " + getSummary() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	if ((getParent() instanceof FileSet) &&
	    (getRegistered() == null) &&
	    (getNchildren() == 0)) {
	    if (getFileSet() == null) {
		responseR.setStatus(-1);
	    } else {
		FrameSet fs = loadFromArchive();
		if (fs != null) {
		    fs.lastRegistrationIndex = -1;
		    fs.setRegistered(new Registration());
		    fs.buildRegistration();
		    responseR = fs.afterTimeRelative(requestI,roI);
		}
	    }

	} else {
	    responseR = super.afterTimeRelative(requestI,roI);
	}

	/*
	System.err.println("FrameSet.afterTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds a request for data for the requested channels ending at the
     * end of this <code>FrameSet</code> (which is before the time
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
     * @see #beforeTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions optionsI)
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
     * 11/04/2003  INB	Created.
     *
     */
    final TimeRelativeResponse beforeTimeRelative(TimeRelativeRequest requestI,
						  RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(1);

	/*
	System.err.println("FrameSet.beforeTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nSummary: " + getSummary() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	if ((getParent() instanceof FileSet) &&
	    (getRegistered() == null) &&
	    (getNchildren() == 0)) {
	    if (getFileSet() == null) {
		responseR.setStatus(-1);
	    } else {
		FrameSet fs = loadFromArchive();
		if (fs != null) {
		    responseR = fs.beforeTimeRelative(requestI,roI);
		}
	    }

	} else {
	    responseR = super.beforeTimeRelative(requestI,roI);
	}

	/*
	System.err.println("FrameSet.beforeTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds the registration for this <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return were any changes made?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/18/2001  INB	Created.
     *
     */
    final boolean buildRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean changedR = false;
	for (int idx = lastRegistrationIndex + 1,
		 endIdx = getNchildren();
	     idx < endIdx;
	     ++idx) {
	    Rmap rmapI = getChildAt(idx);
	    changedR =
		getRegistered().updateRegistration(rmapI,false,false) ||
		changedR;
	    lastRegistrationIndex = Math.max(lastRegistrationIndex,idx);
	}

	return (changedR);
    }

    /**
     * Clears this <code>FrameSet's</code> contents.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/06/2004  INB	Throw <code>AddressExceptions</code> up to our caller
     *			and always reduce.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 05/06/2003  INB	Call <code>reduceToSkeleton</code>.
     * 06/05/2001  INB	Created.
     *
     */
    final void clear()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
   {
	try {
	    // Lock the door.
	    getDoor().lock("FrameSet.clear");

	    // Clear out our children.
	    setChildren(null);

	    // Reduce to a skeleton.
	    reduceToSkeleton();

	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }

    /**
     * Closes this <code>FrameSet</code> to further additions.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Our grandparent is now a <code>RingBuffer</code>.
     * 06/05/2001  INB	Created.
     *
     */
    final void close()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//Exception e=new Exception("FrameSet.close called from");
//e.printStackTrace();

        //EMF 6/19/06: force new DataArray
        //da = null;

	// If there is an <code>Archive</code>, then write this
	// <code>FrameSet</code> to it.
	if (((RingBuffer) getParent().getParent()).getArchive() != null) {
	    ((RingBuffer)
	     getParent().getParent()).getArchive().addElement(this);
	}
    }

    /**
     * Gets the byte offset to the data for this <code>FrameSet</code> within
     * the <code>FileSet</code> archive data file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the data offset.
     * @see #setDoffset(long)
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final long getDoffset() {
	return (dataOffset);
    }

    /**
     * Gets the <code>FileSet</code> parent of this <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>FileSet</code> parent.
     * @see #setFileSet(com.rbnb.api.FileSet)
     * @see #setParent(com.rbnb.api.Rmap)
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
    final FileSet getFileSet() {
	return (filesetParent);
    }

    /**
     * Gets the byte offset to the header for this <code>FrameSet</code> within
     * the <code>FileSet</code> archive header file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the header offset.
     * @see #setHoffset(long)
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final long getHoffset() {
	return (headerOffset);
    }

    /**
     * Gets the byte offset to the registration data for this
     * <code>FrameSet</code> within the <code>FileSet</code> archive
     * registration data file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the registration data offset.
     * @see #setRdoffset(long)
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final long getRdoffset() {
	return (regDataOffset);
    }

    /**
     * Gets the byte offset to the registration header for this
     * <code>FrameSet</code> within the <code>FileSet</code> archive
     * registration header file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the registration header offset.
     * @see #setRhoffset(long)
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final long getRhoffset() {
	return (regHeaderOffset);
    }

    /**
     * Loads this <code>FrameSet</code> from the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the loaded copy of the <code>FrameSet</code>.
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
     * @since V2.2
     * @version 11/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/03/2003  INB	Created.
     *
     */
    private final FrameSet loadFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean gotIt;
	FrameSet fsR = null;
	FrameSet fs = null;

	if (getFileSet() == null) {
	    gotIt = false;
	} else if (((fs =
		     ((Archive)
		      getFileSet().getParent()).getCachedSet()) == null) ||
		   (fs.getIndex() != getIndex()) ||
		   (fs.getParent() != getParent())) {
	    // For performance, we cache one <code>FrameSet</code> at this
	    // time.  Don't use clone.  Instead, simply copy the relevant
	    // fields.
	    fs = new FrameSet(getIndex());
	    fs.setSummary((Registration) getSummary().clone());
	    fs.setParent(getFileSet());
	    fs.setFileSet(getFileSet());
	    fs.setDoffset(getDoffset());
	    fs.setHoffset(getHoffset());
	    fs.setRdoffset(getRdoffset());
	    fs.setRhoffset(getRhoffset());
	    gotIt = fs.readFromArchive();

	} else {
	    gotIt = true;
	}

	if (gotIt) {
	    ((Archive) getFileSet().getParent()).setCachedSet(fs);
	    fsR = fs;
	}

	return (fsR);
    }

    /**
     * Matches the contents of this <code>FrameSet</code> against a
     * <code>TimeRelativeRequest</code>.
     * <p>
     * This method performs the following steps:
     * <p><ol>
     *    <li>Compare the time reference for each of the channels in the
     *	      request to the limits of the <code>FileSet</code>,<li>
     *    <li>If there comparison produces different results for different
     *	      channels, then return a status of -2,</li>
     *    <li>If time references falls outside of the limits for all of the
     *	      channels, then return a status code indicating which way to move
     *	      next,</li>
     *    <li>If the time reference falls inside the limits, then start a
     *	      binary search of the <code>Rmaps</code> contained herein.
     *        Return the status code built from that search.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @param roI      the <code>RequestOptions</code>.
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
    final TimeRelativeResponse matchTimeRelative
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
	System.err.println("FrameSet.matchTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nSummary: " + getSummary() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	if ((getSummary() == null) || (getSummary().getTrange() == null)) {
	    responseR.setStatus(1);
	} else {
	    responseR.setStatus
		(requestI.compareToTimeRange(getSummary().getTrange()));
	}

	if (responseR.getStatus() == 0) {
	    if (getNchildren() == 0) {
		if (getFileSet() == null) {
		    responseR.setStatus(-1);
		} else {
		    FrameSet fs = loadFromArchive();
		    if (fs != null) {
			responseR = fs.matchTimeRelative(requestI,roI);

		    } else {
			responseR.setStatus(1);
		    }
		}

	    } else {
		boolean locked = false;
		try {
		    lockRead("FrameSet.matchTimeRelative");
		    locked = true;
		    responseR = super.matchTimeRelative(requestI,roI);

		} finally {
		    if (locked) {
			unlockRead();
		    }
		}
	    }
	}

	/*
	System.err.println("FrameSet.matchTimeRelative: " +
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
     * @version 11/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/03/2003  INB	Use <code>loadFromArchive</code>.
     * 10/17/2003  INB	Create a new <code>Door</code> for the
     *			<code>FrameSet</code> we read in.
     * 03/28/2003  INB	Eliminated unnecessary locks.
     * 03/13/2000  INB	Created.
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
	byte reasonR = Rmap.MATCH_UNKNOWN;

	if (getChildren() != null) {
	    // If this <code>FrameSet</code> has children, just work on
	    // them normally.
	    reasonR = super.moveDownFrom(extractorI,
					 unsatisfiedI,
					 unsatisfiedO);

	} else if (getFileSet() != null) {
	    // If this <code>FrameSet</code> does not have children, but
	    // there is an archive, then load a copy of the frameset from
	    // the archive and move down from there.
	    FrameSet fs = loadFromArchive();

	    if (fs != null) {
		reasonR = fs.moveDownFrom(extractorI,
					  unsatisfiedI,
					  unsatisfiedO);
	    } else {
		// If we failed to read the <code>FrameSet</code>, it is
		// most likely because the <code>FileSet</code> has been
		// deleted, which means we're asking for the wrong data.
		reasonR = Rmap.MATCH_BEFORE;
	    }

	} else {
	    // Reaching this point means that there just isn't any data in
	    // this frameset; we've run off the end.
	    reasonR = Rmap.MATCH_BEFORE;
	}

	return (reasonR);
    }

    /**
     * Nullifies this <code>FrameSet</code>.
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
	setFileSet(null);
    }

    /**
     * Reads a <code>FrameSet</code> from an archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return did the read succeed?
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
     * @see #readOffsetsFromArchive()
     * @see #readSkeletonFromArchive()
     * @see #writeToArchive()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/08/2006  EMF  Added switch for allocating data or using markers.
     *                  Markers avoid huge memory usage on archive recovery.
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 10/17/2003  INB	Ensure that there is a children vector.  Access the
     *			files outside of locks and put the file lock last.
     * 03/13/2003  INB	Reduced the amount of time the files are marked
     *			accessed.
     * 02/21/2001  INB	Created.
     *
     */
    final boolean readFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
      //EMF 11/8/06: try to avoid huge memory needs on archive recovery
      return readFromArchive(true);
    }


    //old readFromArchive method with data/marker switch
    final boolean readFromArchive(boolean doReadData)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean successR = false;
	boolean[] locks = new boolean[3];

	/*
	String id;
	if ((getParent() instanceof Cache) ||
	    (getParent() instanceof Archive)) {
	    id = getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) getParent().getParent()).getIndex() + "/" +
		getParent().getClass() + "/" +
		getClass() + "#" + getIndex();
	} else if (getParent() == null) {
	    id = getFileSet().getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) getFileSet().getParent().getParent()).getIndex() + "/" +
		getFileSet().getParent().getClass() + "/" +

		getFileSet().getClass() + "#" +
		getFileSet().getIndex() +
		getClass() + "#" + getIndex();
	} else {
	    id = getParent().getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) getParent().getParent().getParent()).getIndex() + "/" +
		getParent().getParent().getClass() + "/" +

		getParent().getClass() + "#" +
		((FrameManager) getParent()).getIndex() +
		getClass() + "#" + getIndex();
	}
	*/

	try {
	    /*
	    System.err.println(Thread.currentThread() + " entering " +
			       id +
			       " readFromArchive.");
	    */

	    if (!getFileSet().deleted) {
		// Access the files.
		getFileSet().accessFiles();
		locks[2] = true;

		// Lock the door.
		getDoor().lock("FrameSet.readFromArchive");
		locks[1] = true;
		getFileSet().getFileDoor().lock("FrameSet.readFromArchive");
		locks[0] = true;

		// Reposition the files.
		InputStream his = getFileSet().getHIS();
		DataInputStream dis = getFileSet().getDIS();

		his.seek(getHoffset());
		dis.seek(getDoffset());

		// Read the <code>FrameSet</code>.
		if (his.readParameter(ARCHIVE_PARAMETERS) == ARC_FST) {
		    //EMF 11/8/06: only read data if told to do so
                    //             (readData still called to move pointer,
                    //             but data space not allocated)
                    //readData(dis);
		    if (doReadData) {
                      read(his,dis);
                      readData(dis);
		    } else {
                      read(his,null);
                      readData(dis);
                    }
		} else {
		    throw new com.rbnb.api.SerializeException
			("Cannot find frameset " +
			 getFullName() + "/" + getIndex() +
			 " at header " + getHoffset() +
			 " and data " + getDoffset() +
			 " thread " + Thread.currentThread());
		}

		// Release the file lock.
		getFileSet().getFileDoor().unlock();
		locks[0] = false;
		
		// Release the files.
		getFileSet().releaseFiles();
		locks[2] = false;

		// If there are no children, then create an empty vector.
		if (getChildren() == null) {
		    setChildren(new RmapVector(1));
		}
		
		// Note that this <code>FrameSet</code> is up-to-date.
		successR = true;
		setUpToDate(true);

		getDoor().setIdentification(getFullName() + "/" +
					    getClass() + "_" +
					    getIndex());
	    }

	} catch (com.rbnb.api.SerializeException e) {
	    throw e;

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileSet().getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		getFileSet().releaseFiles();
	    }

	    /*
	    System.err.println(Thread.currentThread() + " exiting " +
			       id +
			       " readFromArchive.");
	    */
	}

	return (successR);
    }

    /**
     * Reads the <code>FrameSet</code> index and file offsets for this
     * <code>FrameSet</code> from the archive.
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
     * @see #readSkeletonFromArchive()
     * @see #writeToArchive()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 10/17/2003  INB	Access the files outside of locks and put the file lock
     *			last.
     * 03/13/2003  INB	Reduced the amount of time the files are marked
     *			accessed.
     * 03/12/2001  INB	Created.
     *
     */
    final void readOffsetsFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] locks = new boolean[3];

	try {
	    // Access the files.
	    getFileSet().accessFiles();
	    locks[2] = true;

	    // Lock the door.
	    getDoor().lock("FrameSet.readOffsetsFromArchive");
	    locks[1] = true;
	    getFileSet().getFileDoor().lock("FrameSet.readOffsetsFromArchive");
	    locks[0] = true;

	    DataInputStream dis = getFileSet().getODIS();

	    // Read the identification index.
	    setIndex(dis.readLong());

	    // Read the file offsets.
	    setHoffset(dis.readLong());
	    setDoffset(dis.readLong());
	    setRhoffset(dis.readLong());
	    setRdoffset(dis.readLong());

	    getDoor().setIdentification(getFullName() + "/" +
					    getClass() + "_" +
					    getIndex());

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileSet().getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		getFileSet().releaseFiles();
	    }
	}
    }

    /**
     * Reads the skeleton of the <code>FrameSet</code> from the archive.
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
     * @see #writeToArchive()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/17/2003  INB	Access the files outside of locks and put the file lock
     *			last.
     * 03/13/2003  INB	Reduced the amount of time the files are marked
     *			accessed.
     * 02/21/2001  INB	Created.
     *
     */
    final void readSkeletonFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] locks = new boolean[3];

	try {
	    // Access the files.
	    getFileSet().accessFiles();
	    locks[2] = true;

	    // Lock the door.
	    getDoor().lock("FrameSet.readSksletonFromArchive");
	    locks[1] = true;
	    getFileSet().getFileDoor().lock("FrameSet.readSkeletonFromArchive");
	    locks[0] = true;

	    // Reposition the files.
	    InputStream his = getFileSet().getRHIS();
	    DataInputStream dis = getFileSet().getRDIS();
	    his.seek(getRhoffset());
	    dis.seek(getRdoffset());

	    // Read the summary <code>Registration</code>.
	    if (his.readParameter(ARCHIVE_PARAMETERS) == ARC_REG) {
		setSummary(new Registration(his,dis));
		getSummary().readData(dis);
	    }

	    // Note that this <code>FrameSet</code> is up-to-date.
	    setUpToDate(true);

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileSet().getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		getFileSet().releaseFiles();
	    }
	}
    }

    /**
     * Reduces this <code>FrameSet</code> to a skeleton.
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
     * @since V2.1
     * @version 01/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/06/2004  INB	Always reduce to a skeleton if we have no children
     *			after ensuring that our parent has been updated.
     * 05/06/2003  INB	Created.
     *
     */
    final void reduceToSkeleton()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getNchildren() == 0) {
	    if (getRegistered() != null) {
		if (getParent() != null) {
		    if (getParent() instanceof FrameManager) {
			((FrameManager) getParent()).updateRegistration();
		    } else if (getParent() instanceof StorageManager) {
			((StorageManager) getParent()).updateRegistration();
		    }
		}
		if ((getFileSet() != null) &&
		    (getFileSet() != getParent())) {
		    getFileSet().updateRegistration();
		}
	    }
	    setRegistered(null);
	    setUpToDate(true);
	}
    }

    /**
     * Reframes this <code>FrameSet</code> by collapsing time information as
     * much as possible.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the reframed <code>FrameSet</code> hierarchy or null if the
     *	       reframing fails.
     * @since V2.0
     * @version 10/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/14/2003  INB	Put the frame index into a top level child rather than
     *			the <code>FrameSet</code> itself.
     * 09/30/2003  INB	Find the first frame index in the <code>FrameSet</code>
     *			and use that as the value for the reframed
     *			<code>FrameSet</code>.
     * 09/17/2002  INB	Created.
     *
     */
//EMF 5/2/06: change from private to protected so Cache can use reframed...
    protected final FrameSet reframe() {
	FrameSet frameSetR = null;
//System.err.println("original: "+this);

	if (System.getProperty("noreframe") != null) {
	    // System.err.println("noreframe is set, no frame compression!!!");
	    return (null);
	}

	try {
	    // Step 1:
	    // Get a list of the channels in this <code>FrameSet</code> from
	    // the registration.
	    String[] names = getRegistered().extractNames();

	    // Step 2:
	    // Loop through the names and retrieve the data associated with
	    // each name.
	    DataArray[] dataArrays = new DataArray[names.length];
	    for (int idx = 0; idx < names.length; ++idx) {
//following line causes memory growth problem...
		dataArrays[idx] = extract(names[idx]);
//System.err.println("FrameSet.dataArray "+dataArrays[idx]);
	    }

	    // Step 3:
	    // Collapse the <code>FrameSet</code>.
	    frameSetR = new FrameSet(getIndex());
	    Rmap fMap = this;
	    while (fMap.getFrange() == null) {
		fMap = fMap.getChildAt(0);
	    }
	    Rmap top = new Rmap();
	    frameSetR.addChild(top);
	    top.setFrange(fMap.getFrange());
	    Rmap rmap,
		rmap2,
		parent;
	    for (int idx = 0; idx < names.length; ++idx) {
		rmap2 = null;
		if (dataArrays[idx] != null) {
		    rmap2 = dataArrays[idx].toRmap();  // this tries to compresss the Rmap
//System.err.println("FrameSet.rmap2 "+rmap2);
		    if (rmap2 == null) {
			frameSetR = null;
			break;
		    }
		}
		rmap = top.findDescendant(names[idx],true);
		if (rmap2 != null) {
		    rmap2.setName(rmap.getName());
		    parent = rmap.getParent();
		    parent.removeChild(rmap);
		    parent.addChild(rmap2);
		}
	    }

	} catch (java.lang.Exception e) {
	    //System.err.println("Sorry, Reframe bailed on exception: "+e); // mjm debug 
	    //e.printStackTrace();
	    // On any error, we don't try to do the reframe.
	    frameSetR = null;
	}
	//System.err.println("reframed! "+frameSetR);  // mjm foo debug
//frameSetR=null;  //if do this, still have memory growth problem...
//System.err.println("reframed: "+frameSetR);
	return (frameSetR);
    }

    /**
     * Sets the byte offset to the data for this <code>FrameSet</code> within
     * the <code>FileSet</code> archive data file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the data offset.
     * @see #getDoffset()
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final void setDoffset(long offsetI) {
	dataOffset = offsetI;
    }

    /**
     * Sets the <code>FileSet</code> parent of the <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param filesetParentI  the <code>FileSet</code> parent of this
     *			      <code>FrameSet</code>.
     * @see #getFileSet()
     * @see #setParent(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    final void setFileSet(FileSet filesetParentI) {
	filesetParent = filesetParentI;
    }

    /**
     * Sets the byte offset to the header for this <code>FrameSet</code> within
     * the <code>FileSet</code> archive header file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the header offset.
     * @see #getHoffset()
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final void setHoffset(long offsetI) {
	headerOffset = offsetI;
    }

    /**
     * Sets the parent of this <code>FrameSet</code> to the input
     * <code>Rmap</code>.
     * <p>
     * If the input is a <code>FileSet</code>, this method calls
     * <code>setFileSet</code> and checks to see if there is already a
     * <code>Cache</code> parent. If not, the method calls the
     * <code>setParent</code> method of <code>Rmap</code>.
     * <p>
     * If the input is a <code>Cache</code>, the method uses
     * <code>setParent</code> in <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI  the parent <code>Rmap</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if this <code>FrameSet</code> is already a child of
     *		  another <code>Rmap</code>, the input is non-null, and the
     *		  input is the same type as a current parent.
     * @see com.rbnb.api.Rmap#getParent()
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
    final void setParent(Rmap parentI) {
	if (parentI == null) {
	    super.setParent(null);
	    super.setParent(getFileSet());

	} else if (parentI instanceof Cache) {
	    if ((getParent() == null) || (getParent() instanceof Cache)) {
		super.setParent(parentI);
	    } else {
		super.setParent(null);
		super.setParent(parentI);
	    }

	} else {
	    setFileSet((FileSet) parentI);
	    if (getParent() == null) {
		super.setParent(parentI);
	    }
	}
    }

    /**
     * Sets the byte offset to the registration data for this
     * <code>FrameSet</code> within the <code>FileSet</code> archive
     * registration data file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the registration data offset.
     * @see #getRdoffset()
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final void setRdoffset(long offsetI) {
	regDataOffset = offsetI;
    }

    /**
     * Sets the byte offset to the registration header for this
     * <code>FrameSet</code> within the <code>FileSet</code> archive
     * registration header file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the registration header offset.
     * @see #getRhoffset()
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Made this accessible from outside this class.
     * 03/12/2001  INB	Created.
     *
     */
    final void setRhoffset(long offsetI) {
	regHeaderOffset = offsetI;
    }

    /**
     * Stores a frame <code>Rmap</code> to this <code>FrameSet</code> object.
     * <p>
     * A frame <code>Rmap</code> is a single <code>Rmap</code> hierarchy
     * received from a source application.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI  the frame <code>Rmap</code>.
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
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	The <code>SinkHandler</code> is now three levels up.
     * 02/09/2001  INB	Created.
     *
     */
    final void storeElement(Rmap frameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getParent().getParent().getParent() instanceof SinkHandler) {
	    // If we are working for an <code>SinkHandler</code>, just add the
	    // frame directly into the <code>FrameSet</code>.
	    super.addChild(frameI);

	} else {
            //EMF 7/6/06: change frameI so chan at top, maybe more
            //            efficient searches?
        try {
            DataBlock db = null;
            TimeRange tr = null;
            TimeRange fr = null;
            String name = null;
            String[] names = frameI.extractNames();
            if (names.length==1 && names[0].indexOf('/')==-1) {
              name=names[0].substring(1); //strip leading /
              Rmap child = frameI;
              do {
                if (db==null) db=child.getDblock();
                if (tr==null) tr=child.getTrange();
                if (fr==null) fr=child.getFrange();
                if (child.getNchildren()==0) break;
                child=child.getChildAt(0);
              } while (db==null || tr==null || fr==null);
            }

            if (db!=null) {
              Rmap newFrame = new Rmap();
              newFrame.setName(name);
              Rmap child = new Rmap();
              child.setTrange(tr);
              child.setFrange(fr);
              child.setDblock(db);
              newFrame.addChild(child);
              Rmap ret = super.mergeWith(newFrame);
            } else {
            // For regular <code>SourceHandlers</code>, merge the frame
            // <code>Rmap</code> into the frames <code>Rmap</code>.
            Rmap ret = super.mergeWith(frameI);
            }
//System.err.println("FrameSet.storeElement: getRegistered() "+getRegistered());
lastRegistrationIndex=-1;
//System.err.println("FrameSet.storeElement: this "+this);
        }catch(Exception e) {
          e.printStackTrace();
        }
        }
    }

/*  EMF 7/6/06: abandon below effort for now - too complicated with streaming
                and requests.  See StreamRBOListener.
        boolean useDA=false;
        boolean firstDA=false;
        String name=null;
        TimeRange fr=null;
        try {
            //EMF 6/19/06: try DataArray to create more efficient structures
            DataBlock db = null;
            TimeRange tr = null;
            //TimeRange fr = null;
            String[] names = frameI.extractNames();
            if (names.length==1) {
              name=names[0].substring(1); //strip leading /
              Rmap child = frameI;
              do {
                if (db==null) db=child.getDblock();
                if (tr==null) tr=child.getTrange();
                if (fr==null) fr=child.getFrange();
                if (child.getNchildren()==0) break;
                child=child.getChildAt(0);
              } while (db==null || tr==null || fr==null);
            }

//EMF 6/30/06
            if (db!=null) {
              byte dType = db.getDtype();
              if (dType==DataBlock.TYPE_BOOLEAN ||
                  dType==DataBlock.TYPE_INT8 ||
                  dType==DataBlock.TYPE_INT16 ||
                  dType==DataBlock.TYPE_INT32 ||
                  dType==DataBlock.TYPE_FLOAT32 ||
                  dType==DataBlock.TYPE_FLOAT64) {
                if (da==null) {
                  da=new DataArray();
                  da.setNumberOfPoints(1000,db.getPtsize(),dType);
                  firstDA=true;
//System.err.println("da created: "+da);
                }
                if (db.getPtsize()==da.getPointSize() &&
                    dType==(byte)da.getDataType() &&
                    da.getNumInArray()+db.getNpts()<=da.getNumberOfPoints()) {
                    da.add(db.getNpts(),db,tr,fr);
//System.err.println("adding tr "+tr+", fr "+fr+", db "+db);
//System.err.println("da added to: "+da);
                    useDA=true;
                }
              }
            }
          } catch (Exception e) {
            System.err.println("FrameSet.storeElement: exception using DataArray");
            e.printStackTrace();
          }
//System.err.println("frameI "+frameI);
//System.err.println();
          if (useDA) { 
            if (firstDA) {
              Rmap ret=super.mergeWith(frameI); //bug in DataArray messes up
              //Rmap ret=super.mergeWith((Rmap)frameI.clone()); //bug in DataArray messes up
                                                //toRmap when only 1 point...
                             //clone so streaming has unattached copy of frame
                             //to play with EMF 6/30/06
            } else {
              if (!firstDA) {
//System.err.println("removing child "+super.getChildAt(super.getNchildren()-1));
//fr=super.getChildAt(super.getNchildren()-1).getFrange();
//System.err.println();
                super.removeChildAt(super.getNchildren()-1);
              }
//System.err.println("this "+this);
//System.err.println();
              Rmap toAdd = new Rmap();
              toAdd.setFrange(fr); //keep track of frame #...
              Rmap daMap = da.toRmap();
//System.err.println("daMap "+daMap);
//System.err.println();
              daMap.setName(name);
              toAdd.addChild(daMap);
              //Rmap child = new Rmap();
              //toAdd.addChild(child);
              //child.setTrange(daMap.getTrange());
              //Rmap gchild = new Rmap();
              //child.addChild(gchild);
              //gchild.setName(name);
              //gchild.setDblock(daMap.getDblock());
              //toAdd.addChild(da.toRmap());
              //toAdd.setName(name);
//System.err.println("toAdd "+toAdd);
//System.err.println();
              Rmap ret = super.mergeWith(toAdd);
//System.err.println("added da, now "+this);
//System.err.println();
//System.err.println("parent now "+this.getParent());
//System.err.println();
//System.err.println();
//updateRegistration(); //grope...
lastRegistrationIndex=-1;
//if (getRegistered()==null) setRegistered(new Registration());
//buildRegistration();
            }
          } else { //do not build up dataarray; start over next time
            da = null;
            // For regular <code>SourceHandlers</code>, merge the frame
            // <code>Rmap</code> into the frames <code>Rmap</code>.
            Rmap ret = super.mergeWith(frameI);
//System.err.println("parent now "+this.getParent());
//System.err.println("\n");
          }
//System.err.println("FrameSet.storeElement: frameI "+frameI);
//System.err.println("\n this "+this);
//System.err.println("\n\n");
	}
    }
*/

    /**
     * Writes this <code>FrameSet</code> to the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hosI  the header output streams.
     * @param dosI  the data output streams.
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
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/22/2003  INB	Force read/write access.
     * 10/17/2003  INB	Access the files outside of locks and put the file lock
     *			last.
     * 09/30/2003  INB	Summarize the reframed <code>FrameSet</code> rather
     *			than the original.
     * 09/09/2003  INB	Reordered the archive writes so that data is always
     *			written before the headers.
     * 04/24/2003  INB	Mark the <code>FileSet</code> as out of date.
     * 03/13/2003  INB	Reduced the amount of time the files are marked
     *			accessed.
     * 02/21/2001  INB	Created.
     *
     */
    final void writeToArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] locks = new boolean[3];
	// System.err.println("FrameSet.writeToArchive() start\n" + this);
	try {
	    // Ask the <code>FileSet</code> to mark the archive as out of date.
	    getFileSet().markOutOfDate();

	    // Access the files.
	    getFileSet().accessFiles(true);
	    locks[2] = true;

	    // Lock the door.
	    getDoor().lock("FrameSet.writeToArchive");
	    locks[1] = true;
	    getFileSet().getFileDoor().lock("FrameSet.writeToArchive");
	    locks[0] = true;

	    // Write to the archive header and data files.
	    OutputStream hos = getFileSet().getHOS();
	    DataOutputStream dos = getFileSet().getDOS();

	    setHoffset(hos.getFilePointer());
	    setDoffset(dos.getFilePointer());

	    /*	    FrameSet reframed = null;*/
	    updateRegistration();
	    //EMF 5/2/06: reframing now done in StorageManager.addElement
	    //            for better cache performance, no need to repeat here
	    FrameSet reframed = reframe(); //null;

	    if (reframed == null) {
		writeData(dos);
		dos.flush();
		write(ARCHIVE_PARAMETERS,ARC_FST,hos,dos);
		hos.flush();
	    } else {
		reframed.writeData(dos);
		dos.flush();
		reframed.write(ARCHIVE_PARAMETERS,ARC_FST,hos,dos);
		hos.flush();
	    }

	    // Write to the registration header and data files.
//NOTE: EMF 11/17/06
//NOTE: if make changes here, also make them in FileSet.recoverFromDataFiles
//NOTE: (a common routine would be better)
	    hos = getFileSet().getRHOS();
	    dos = getFileSet().getRDOS();
	    setRhoffset(hos.getFilePointer());
	    setRdoffset(dos.getFilePointer());
	    if (reframed == null) {
//EMF 8/8/06-give registration time to percolate - on some dual processor
//           machines, registration is not correct on archive write
try{Thread.currentThread().sleep(1);}catch(Exception e){}
		getSummary().writeData(dos);
		dos.flush();
		getSummary().write(ARCHIVE_PARAMETERS,
				   ARC_REG,
				   hos,
				   dos);
	    } else {
		reframed.updateRegistration();
//EMF 8/8/06-give registration time to percolate - on some dual processor
//           machines, registration is not correct on archive write
try{Thread.currentThread().sleep(1);}catch(Exception e){}
		reframed.getSummary().writeData(dos);
		dos.flush();
		reframed.getSummary().write(ARCHIVE_PARAMETERS,
					    ARC_REG,
					    hos,
					    dos);
	    }
	    hos.flush();

	    // Write the offsets.
	    dos = getFileSet().getODOS();
	    dos.writeByte(1);
	    dos.writeLong(getIndex());
	    dos.writeLong(getHoffset());
	    dos.writeLong(getDoffset());
	    dos.writeLong(getRhoffset());
	    dos.writeLong(getRdoffset());
	    dos.flush();

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileSet().getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		getFileSet().releaseFiles();
	    }
	}
	// System.err.println("FrameSet.writeToArchive() end\n" + this);
    }
}
