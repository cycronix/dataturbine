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
 * Abstract class providing the common interface for the <code>FrameSet</code>
 * and <code>FileSet</code> objects that are the basic units of the
 * <code>DTCaches</code> and <code>DTArchives</code>, respectively.
 * <p>>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Archive
 * @see com.rbnb.api.Cache
 * @since V2.0
 * @version 01/06/2004
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/06/2004  INB	Added <code>AddressException</code> to the list thrown
 *			by <code>clear</code> method.
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
 * 11/05/2003  INB	Added <code>afterTimeRelative</code> and
 *			<code>beforeTimeRelative</code>.
 * 10/17/2003  INB	Don't update the registration when matching or
 *			moving down from here - it is done earlier.
 *			<code>setDoor</code> is no longer private.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/22/2003  INB	Eliminated specific <code>getDataSize</code> method.
 *			Locking things during that code is problematic during
 *			shutdown.
 * 05/06/2003  INB	Allow for <code>null</code> registration.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 02/19/2003  INB	Made <code>newInstance</code> final.
 * 02/09/2001  INB	Created.
 *
 */
abstract class FrameManager
    extends com.rbnb.api.RmapWithMetrics
{
    /**
     * the <code>Door</code> to this <code>FrameManager</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/21/2001
     */
    private Door door = null;

    /**
     * index used for identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/08/2001
     */
    private long idIndex;

    /**
     * last time the <code>Registration</code> was updated.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/14/2001
     */
    private long lastRegistration = Long.MIN_VALUE;

    /**
     * the registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/09/2001
     */
    private Registration registered = null;

    /**
     * the summary registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/09/2001
     */
    private Registration summary = null;

    /**
     * registration up-to-date?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/20/2001
     */
    private boolean upToDate = false;

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
     * @see #FrameManager(long)
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    FrameManager()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	setDoor(new Door(Door.READ_WRITE));
	setDataSize(0);
    }

    /**
     * Class constructor to build a <code>FrameManager</code> from an
     * identification index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idIndexI  the identification index.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #FrameManager()
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
    FrameManager(long idIndexI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	setIndex(idIndexI);
    }

    /**
     * Adds an element <code>Rmap</code> to this <code>FrameManager</code>
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
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
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 02/09/2001  INB	Created.
     *
     */
    final void addElement(Rmap elementI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the <code>Door</code>.
	    getDoor().setIdentification(getFullName() + "/" +
					getClass() + "_" + getIndex());
	    getDoor().lock("FrameManager.addElement");

	    // The registration is no longer up-to-date.
	    setUpToDate(false);

	    // Store the element.
	    storeElement(elementI);
	    setDataSize(getDataSize() + elementI.getDataSize());

	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * For <code>FrameManagers</code>, we display the index.
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
	return (" FMIndex: " + getIndex() + ", summary "+getSummary());
//EMF 11/13/06
    }

    /**
     * Builds a request for data for the requested channels starting at the
     * beginning of this <code>FrameManager</code> (which is after the time
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
    TimeRelativeResponse afterTimeRelative(TimeRelativeRequest requestI,
					   RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("FrameManager.afterTimeRelative: " +
			   getFullName() + "/" + getClass() + "_" +
			   getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nSummary: " + getSummary() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(-1);

	boolean locked = false;
	try {
	    lockRead("FrameManager.afterTimeRelative");
	    locked = true;

	    if (getRegistered() != null) {
		com.rbnb.utility.SortedVector channels =
		    requestI.getByChannel();
		DataArray limits = getRegistered().extract
		    (((TimeRelativeChannel)
		      channels.firstElement()).getChannelName
		     ().substring(requestI.getNameOffset()));

		responseR.setTime(limits.getStartTime());
		responseR.setStatus(0);
		responseR.setInvert(false);
	    } else if ((getParent() instanceof FileSet) &&
		       (getNchildren() != 0)) {
		responseR = super.afterTimeRelative(requestI,roI);
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	/*
	System.err.println("FrameManager.afterTimeRelative: " +
			   getFullName() + "/" + getClass() + "_" +
			   getIndex() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds a request for data for the requested channels ending at the
     * end of this <code>FrameManager</code> (which is before the time
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
     * 11/04/2003  INB	Created.
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
	System.err.println("FrameManager.beforeTimeRelative: " +
			   getFullName() + "/" + getClass() + "_" +
			   getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nSummary: " + getSummary() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(-1);

	boolean locked = false;
	try {
	    lockRead("FrameSet.beforeTimeRelative");
	    locked = true;

	    if (((roI == null) || !roI.getExtendStart()) &&
		(getRegistered() != null)) {
		com.rbnb.utility.SortedVector channels =
		    requestI.getByChannel();
		DataArray limits = getRegistered().extract
		    (((TimeRelativeChannel)
		      channels.firstElement()).getChannelName
		     ().substring(requestI.getNameOffset()));

		responseR.setTime
		    (limits.getStartTime() + limits.getDuration());
		responseR.setStatus(0);
		responseR.setInvert(true);

	    } else if ((((roI != null) && roI.getExtendStart()) ||
			(getParent() instanceof FileSet)) &&
		       (getNchildren() != 0)) {
		responseR = super.beforeTimeRelative(requestI,roI);
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	/*
	System.err.println("FrameManager.beforeTimeRelative: " +
			   getFullName() + "/" + getClass() + "_" +
			   getIndex() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Builds the registration for this <code>FrameManager</code>.
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
     * @version 09/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/18/2001  INB	Created.
     *
     */
    abstract boolean buildRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Clears this <code>FrameManager's</code> contents.
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
     * 01/06/2004  INB	Throw <code>AddressExceptions</code> up to our caller.
     * 06/05/2001  INB	Created.
     *
     */
    abstract void clear()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Closes this <code>FrameManager</code> to further additions.
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
    abstract void close()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Compares the sorting value of this <code>FrameManager</code> to the
     * input sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * The sorting value for an <code>FrameManager</code> is always itself.
     * <p>
     * If the input is also a <code>FrameManager</code>, then the comparison is
     * by identification index.
     * <p>
     * If the input is not a <code>FrameManagaer</code>, then the
     * <code>Rmap</code> comparison method is used.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>FrameManager</code> compares less than the
     *		   input,
     *	       <p> 0 if this <code>FrameManager</code> compares equal to the
     *		   input, and
     *	       <p>>0 if this <code>FrameManager</code> compares greater than
     *		   the input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @exception java.lang.IllegalStateException
     *		  thrown if both the this <code>FrameManager</code> and the
     *		  input <code>Rmap</code> are nameless and timeless.
     * @see #compareTo(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    public int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	int comparedR = 0;

	if (otherI instanceof FrameManager) {
	    comparedR = (int) (getIndex() -
			       ((FrameManager) otherI).getIndex());
	} else {
	    comparedR = super.compareTo(sidI,otherI);
	}

	return (comparedR);
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
     * Gets the identification index.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the identification index.
     * @see #setIndex(long)
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
    final long getLastRegistration() {
	return (lastRegistration);
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
    public final Registration getRegistered() {
	return (registered);
    }

    /**
     * Gets the summary <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the summary <code>Registration</code>.
     * @see #setSummary(com.rbnb.api.Registration)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2001  INB	Created.
     *
     */
    public final Registration getSummary() {
	return (summary);
    }

    /**
     * Gets the registration up-to-date flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the registration up-to-date?
     * @see #setUpToDate(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    final boolean getUpToDate() {
	return (upToDate);
    }

   /**
     * Is this <code>FrameManager</code> identifiable?
     * <p>
     * All <code>FrameManagers</code> are identifiable.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this identifiable?
     * @since V2.0
     * @version 04/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2001  INB	Created.
     *
     */
    final boolean isIdentifiable() {
	return (true);
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
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/17/2003  INB	Don't update the registration here.
     * 03/13/2001  INB	Created.
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

	if (requestI instanceof FrameManager) {
	    // When the request is a <code>FrameManager</code>, this method
	    // simply compares the indexes.
	    FrameManager fmRequest = (FrameManager) requestI;
	    long compare = getIndex() - fmRequest.getIndex();

	    if (compare == 0) {
		matchesR = MATCH_EQUAL;
	    } else if (compare < 0) {
		matchesR = MATCH_BEFORE;
	    } else {
		matchesR = MATCH_AFTER;
	    }

	} else {
	    // When the request is not a <code>FrameManager</code>, then
	    // the comparison needs to be down by matching time limit
	    // information. Match against the summary, if one exists. If there
	    // is nothing, then assume that we might have a match.
	    boolean locked = false;
	    try {
		getDoor().lockRead("FrameManager.matches");
		locked = true;

		if (getSummary() != null) {
//                    System.err.println("mjm FrameManager isWithinLimits: "+requestI);
		    matchesR = getSummary().isWithinLimits(requestI);

		} else {
		    matchesR = MATCH_UNKNOWN;
		}

	    } finally {
		if (locked) {
		    getDoor().unlockRead();
		}
	    }
	}

	return (matchesR);
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to the
     *		  source <code>Rmap</code> hierarchy or in extracting the
     *		  desired information.
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
     * 03/13/2000  INB	Created.
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
	byte reasonR = Rmap.MATCH_UNKNOWN;

	/*
	String id;
	if ((getParent() instanceof Cache) ||
	    (getParent() instanceof Archive)) {
	    id = getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) getParent().getParent()).getIndex() + "/" +
		getParent().getClass() + "/" +
		getClass() + "#" + getIndex();
	} else if (getParent() == null) {
	    FileSet fls = ((FrameSet) this).getFileSet();
	    id = fls.getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) fls.getParent().getParent()).getIndex() + "/" +
		fls.getParent().getClass() + "/" +

		fls.getClass() + "#" +
		fls.getIndex() +
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

	boolean locked = false;
	try {
	    // Lock the door.

	    /*
	    System.err.println(Thread.currentThread() + " entering " +
			       id +
			       " moveDownFrom read lock.");
	    */

	    getDoor().lockRead("FrameManager.moveDownFrom");
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
			       id +
			       " moveDownFrom read lock.");
	    */
	}

	return (reasonR);
    }

    /**
     * Creates a new instance of the same class as this
     * <code>FrameManager</code> (or a similar class).
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
     * Nullifies this <code>FrameManager</code>.
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

	if (getDoor() != null) {
	    getDoor().nullify();
	    setDoor(null);
	}

	if (getRegistered() != null) {
	    getRegistered().nullify();
	    setRegistered(null);
	}

	if (getSummary() != null) {
	    getSummary().nullify();
	    setSummary(null);
	}
    }

    /**
     * Reads a <code>FrameManager</code> from an archive.
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
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readOffsetsFromArchive()
     * @see #readSkeletonFromArchive()
     * @see #writeToArchive()
     * @since V2.0
     * @version 09/26/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    abstract boolean readFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     * @version 03/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    abstract void readOffsetsFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Reads the skeleton of the <code>FrameManager</code> from the archive.
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
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readFromArchive()
     * @see #writeToArchive()
     * @since V2.0
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    abstract void readSkeletonFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the <code>Door</code> to this <code>FrameManager</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doorI  the <code>Door</code>.
     * @see #getDoor()
     * @since V2.0
     * @version 10/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2007  WHF  Reset the private tag.
     * 10/17/2003  INB	Took off the private tag.
     * 02/21/2001  INB	Created.
     *
     */
    private final void setDoor(Door doorI) {
	door = doorI;
    }

    /**
     * Sets the identification index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param idIndexI  the new identification index.
     * @see #getIndex()
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
    final void setLastRegistration(long timeI) {
	lastRegistration = timeI;
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
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Handle the case where the input is <code>null</code>.
     * 03/13/2001  INB	Created.
     *
     */
    final void setRegistered(Registration registeredI) {
	registered = registeredI;
	if (registered != null) {
	    registered.setParent(this);
	}
    }

    /**
     * Sets the summary <code>Registration</code> map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param summaryI  the new summary <code>Registration</code>.
     * @see #getSummary()
     * @since V2.0
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Set the parent of the summary.
     * 03/13/2001  INB	Created.
     *
     */
    final void setSummary(Registration summaryI) {
	summary = summaryI;
	if (summaryI != null) {
	    summaryI.setParent(this);
	}
    }

    /**
     * Sets the registration up-to-date flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param upToDateI  is registration up-to-date?
     * @see #getUpToDate()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    final void setUpToDate(boolean upToDateI) {
	upToDate = upToDateI;
    }

    /**
     * Stores an element <code>Rmap</code> in this <code>FrameManager</code>
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 03/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/08/2001  INB	Created.
     *
     */
    abstract void storeElement(Rmap elementI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Updates the registration map.
     * <p>
     * This method updates the names and time/frame limits of the
     * <code>Registration</code> for this <code>FrameManager</code>.
     *
     * @author Ian Brown
     *
     * @return was the registration updated?
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
     * 02/20/2001  INB	Created.
     *
     */
    final boolean updateRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean updatedR = false;

      if (!getUpToDate()) {  // MJM 2/20/2007: move if{} outside door lock to reduce lock/unlock cycles
	try {
	    getDoor().lock("FrameManager.updateRegistration");
//	    if (!getUpToDate()) {  // MJM
		// If the <code>Registration</code> is not up-to-date, then
		// perform an update on it.
		if (getRegistered() == null) {
		    setRegistered(new Registration());
		    setSummary(new Registration());
		}

		if (buildRegistration()) {
		    setSummary((Registration) getRegistered().summarize());
		    updatedR = true;
		}

		// Set the registration time.
		setLastRegistration(System.currentTimeMillis());

		// Note that we're now up-to-date.
		setUpToDate(true);
//	    }  // MJM

	} finally {
	    getDoor().unlock();
	}
      }  // MJM

	return (updatedR);
    }

    /**
     * Writes this <code>FrameManager</code> to the archive.
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
     * @version 03/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    abstract void writeToArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
