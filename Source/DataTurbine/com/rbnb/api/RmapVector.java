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
 * Stores an ordered list of <code>Rmaps</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/03/2004
 */

/*
 * Copyright 2000, 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/03/2004  INB	Added documentation to findMatches.
 * 02/17/2004  INB	Added <code>insertAt</code> method to provide
 *			synchronization.
 * 02/11/2004  INB	Synchronize the add relative to the read count.
 * 05/02/2003  INB	Call <code>RmapVector(1) or RmapVector(size())</code>
 *			rather than <code>RmapVector()</code>.
 * 03/12/2003  INB	Added version of <code>findName</code> that takes a
 *			flag to indicate whether or not to try for multiple
 *			matching entries.
 * 12/05/2000  INB	Created.
 *
 * This class needs to be rewritten once I get a chance to rework all the code
 * that uses it. In particular, get rid of all the handles/accepts garbage.
 * <p>
 */
final class RmapVector
    extends com.rbnb.utility.SortedVector
{
    /**
     * last entry found by name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/14/2001
     */
    private int lastName = -1;

    /**
     * the number of outstanding reads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/11/2001
     */
    private int readCount = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    RmapVector() {
	super();
    }

    /**
     * Class constructor to make an <code>RmapVector</code> of the specified
     * initial capacity.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Created.
     *
     */
    RmapVector(int initialCapacityI) {
	super(initialCapacityI);
    }

    /**
     * Determines if this <code>RmapVector</code> can accept the input variety
     * of <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code>.
     * @return does this <code>RmapVector</code> accept the input variety of
     *	       <code>Rmap</code>?
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
    final boolean accepts(Rmap rmapI) {
	return (true);
    }

    /**
     * Adds an <code>Rmap</code> to the input <code>RmapVector</code>.
     * <p>
     * This method should disappear, along with the variety of
     * <code>RmapVectors</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vectorI  the <code>RmapVector</code>. If null, create one.
     * @param rmapI    the <code>Rmap</code>.
     * @return the input <code>RmapVector</code> or the new
     *	       <code>RmapVector</code> created to allow the input
     *	       <code>Rmap</code> to be added.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Rmap</code> cannot be added.
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Synchronize the add relative to the read count.
     * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
     *			<code>RmapVector()</code>.
     * 12/05/2000  INB	Created.
     *
     */
    final static RmapVector addToVector(RmapVector vectorI,
					Rmap rmapI)
    {
	RmapVector vectorR = vectorI;

	if (vectorR == null) {
	    vectorR = new RmapVector(1);
	}
	try {
	    synchronized (vectorR) {
		while (vectorR.getReadCount() > 0) {
		    try {
			vectorR.wait(TimerPeriod.LONG_WAIT);
		    } catch (java.lang.InterruptedException e) {
		    }
		}

		vectorR.add(rmapI);
	    }

	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.IllegalStateException
		(rmapI + " cannot be added to " + vectorR);
	}

	return (vectorR);
    }

    /**
     * Clones this <code>RmapVector</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/19/2001  INB	Created.
     *
     */
    public Object clone() {
	RmapVector clonedR = null;

	clonedR = (RmapVector) super.clone();

	for (int idx = 0; idx < size(); ++idx) {
	    Rmap entryClone = (Rmap) ((Rmap) elementAt(idx)).clone();
	    clonedR.setElementAt(entryClone,idx);
	}

	return (clonedR);
    }

    /**
     * Converts this <code>RmapVector</code> to a type that supports the input
     * variety of <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code>.
     * @return the new vector.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
     final RmapVector convertFor(Rmap rmapI) {
	 return (this);
     }

    /**
     * Copies this vector (but not its contents).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the copy of the vector.
     * @see #clone()
     * @since V2.0
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>RmapVector(size())</code> rather than
     *			<code>RmapVector()</code>.
     * 06/12/2001  INB	Created.
     *
     */
    final RmapVector copyVector() {
	RmapVector rmapVectorR = null;

	try {
	    incrementReadCount();

	    rmapVectorR = new RmapVector(size());
	    for (int idx = 0; idx < size(); ++idx) {
		rmapVectorR.addElement(elementAt(idx));
	    }
	} finally {
	    decrementReadCount();
	}

	return (rmapVectorR);
    }

    /**
     * Decrements the read counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #getReadCount()
     * @see #incrementReadCount()
     * @since V2.0
     * @version 06/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    final synchronized void decrementReadCount() {
	--readCount;
	notifyAll();
    }

    /**
     * Copies this <code>RmapVector</code> without copying data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the copy.
     * @since V2.0
     * @version 07/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2001  INB	Created.
     *
     */
    public RmapVector duplicate() {
	RmapVector clonedR = null;

	clonedR = (RmapVector) super.clone();

	for (int idx = 0; idx < size(); ++idx) {
	    Rmap entryClone = ((Rmap) elementAt(idx)).duplicate();
	    clonedR.setElementAt(entryClone,idx);
	}

	return (clonedR);
    }

    /**
     * Finds a matching <code>Rmap</code>.
     * <p>
     * If there are multiple matches, the one returned is not defined.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>Rmap</code> to match.
     * @return a matching <code>Rmap</code> or null if there is no match.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 10/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2001  INB	Created.
     *
     */
    final Rmap findMatch(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;

	try {
	    incrementReadCount();

	    DataRequest reference = new DataRequest();
	    if ((requestI != null) && (requestI instanceof DataRequest)) {
		reference.combineWith((DataRequest) requestI);
	    }

	    if (requestI == null) {
		// If the request is null, it matches the first unnamed entry.
		if (size() > 0) {
		    // If there is at least one <code>Rmap</code> in the
		    // vector,  try it.
		    Rmap tMap = (Rmap) firstElement();

		    if (tMap.getName() == null) {
			// If it is nameless, we'll return it.
			rmapR = tMap;
		    }
		}

	    } else if (requestI.isNamelessTimeless() ||
		       (reference.getReference() != DataRequest.ABSOLUTE)) {
		// If the request is nameless and timeless, it matches the
		// first entry.
		if (size() > 0) {
		    // If there is at least one <code>Rmap</code> in the
		    // vector, return it.
		    rmapR = (Rmap) firstElement();
		}

	    } else if (size() > 0) {
		// If there are elements in this <code>RmapVector</code>, then
		// we need to actually look for an answer.
		int namelessTimeless;

		for (namelessTimeless = 0;
		     namelessTimeless < size();
		     ++namelessTimeless) {

		    // Skip the nameless/timeless entries.
		    Rmap rmap = (Rmap) elementAt(namelessTimeless);
		    if (!rmap.isNamelessTimeless()) {
			break;
		    }
		}

		if (size() > namelessTimeless) {
		    // If there is anything other than the nameless and
		    // timeless entry, then we need to check for a real match.
		    int[] limits = new int[2];
		    limits[0] = namelessTimeless;
		    limits[1] = size() - 1;
		    int matched = locateAMatch(requestI,reference,limits);

		    if ((matched >= -size()) && (matched < size())) {
			// If there was a match, find a real one.
		    
			if (matched >= 0) {
			    // If the first entry actually matched, then it is
			    // what we want.
			    rmapR = (Rmap) elementAt(matched);

			} else {
			    // If the first entry was in the vicinity, then
			    // look for a real match.
			    if ((rmapR = findRealMatch(requestI,
						       reference,
						       matched,
						       -1,
						       limits)) == null) {
				rmapR = findRealMatch(requestI,
						      reference,
						      matched,
						      1,
						      limits);
			    }
			}
		    }
		}
	    }

	} finally {
	    decrementReadCount();
	}

	return (rmapR);
    }

    /**
     * Finds the <code>Rmaps</code> matching the input request
     * <code>Rmap</code>.
     * <p>
     * This method performs a modified binary search to locate the
     * <code>Rmap(s)</code> of interest.  There are several things that make
     * this different than a straight binary search:
     * <p><ol>
     *    <li>We're actually looking for potential matches that may be a subset
     *	      or a superset of request,</li>
     *    <li>The list can contain multiple <code>Rmaps</code> that potentially
     *	      match rather than just a single one,</li>
     *    <li>There are two different kinds of time information: user-specified
     *	      times and RBNB server specified frame indexes,</li>
     *    <li>The list can contain <code>Rmaps</code> without names or times,
     *	      with just names, with just times, or with both names and
     *	      times, and</li>
     *    <li>The request can have no name or time, just a name, just a time,
     *	      or both a name and a time.  The time information in the request
     *	      can be for either user specified time or RBNB specified frame
     *	      index, but not both.</li>
     * </ol><p>
     * The method returns a list of zero or more potential matches based on the
     * following rules:
     * <p><ol>
     *    <li>If there is no input request, then anything without a name is a
     *	      potential match,</li>
     *    <li>If the input request has no name or time, then everything is a
     *	      potential match,</li>
     *    <li>If the request name is a wildcard and there is no requested time,
     *	      then everything is a potential match,</li>
     *    <li>If the request name is a wildcard and there is time (or frame),
     *	      then nameless/timeless list entries, plus entries matching the
     *	      time (in part or whole) are potential matches,</li>
     *    <li>If the request has no name, but does have a time, then all
     *	      nameless and timeless entries and those that are not considered
     *	      identifiable (such as FrameSets), are potential matches,</li>
     *    <li>If the request has a name and possibly a time, then all nameless
     *	      and timeless entries are potential matches, and</li>
     *    <li>Remaining matches are located first by performing a binary search
     *	      to locate something with the right name and/or time, and second
     *	      by moving forwards and backwards in the list to locate any
     *	      additional matching entries.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code> to match.
     * @param matchesO  the vector of matches.
     * @return the reason for an empty return vector. A value of
     *	       <code>Rmap.MATCH_ILLEGAL</code> is returned if the request
     *	       cannot be processed.
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
     * @version 08/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/03/2004  INB	Added documentation.
     * 12/05/2000  INB	Created.
     *
     */
    final byte findMatches
	(Rmap requestI,
	 java.util.Vector matchesO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

//System.err.println("mjm RmapVector.findMatches: "+requestI);
	try {
	    incrementReadCount();

	    int namelessTimeless = 0;
	    DataRequest reference = new DataRequest();
	    if ((requestI != null) && (requestI instanceof DataRequest)) {
		reference.combineWith((DataRequest) requestI);
	    }

	    // If the request is null, it matches all unnamed entries.
	    if (requestI == null) {
		for (int idx = 0; idx < size(); ++idx) {
		    Rmap rmap = (Rmap) elementAt(idx);
		    
		    if (rmap.getName() == null) {
			matchesO.addElement(rmap);
		    } else {
			break;
		    }
		}

	    } else if (requestI.isNamelessTimeless() ||
		       (reference.getReference() != DataRequest.ABSOLUTE)) {
		// If the request is nameless and timeless, then it matches
		// everything in this vector.
		for (int idx = 0; idx < size(); ++idx) {
		    matchesO.addElement(elementAt(idx));
		}

	    } else if ((requestI.compareNames("...") == 0) ||
		       (requestI.compareNames("*") == 0)) {
		// If the request has a name of "..." or "*", then it should
		// match every name.
		Rmap wRequest = new Rmap();
		wRequest.setTrange(requestI.getTrange());
		wRequest.setFrange(requestI.getFrange());
		if (wRequest.isNamelessTimeless()) {
		    // If the request is effectively nameless and timeless,
		    // then it matches everything in this vector.
		    for (int idx = 0; idx < size(); ++idx) {
			matchesO.addElement(elementAt(idx));
		    }
		} else {
		    for (int idx = 0; idx < size(); ++idx) {
			Rmap element = (Rmap) elementAt(idx);
			if (element.isNamelessTimeless()) {
			    matchesO.addElement(element);
			} else {
			    wRequest.setName(element.getName());
			    byte intersection = element.matches
				(wRequest,
				 reference);
			    if ((intersection == Rmap.MATCH_EQUAL) ||
				(intersection == Rmap.MATCH_SUBSET) ||
				(intersection == Rmap.MATCH_SUPERSET) ||
				(intersection == Rmap.MATCH_INTERSECTION)) {
				// If the <code>Rmap</code> at the current
				// position contains anys information that
				// could be of interest to us, we declare it to
				// be a match.
				matchesO.addElement(element);
			    }
			}			
		    }
		}

	    } else if (size() > 0) {
		// If there are elements in this <code>RmapVector</code>, then
		// we need to actually look for an answer.

		if (requestI.getName() == null) {
		    // Without a name, we can match all nameless and timeless
		    // entries.
		    for (namelessTimeless = 0;
			 namelessTimeless < size();
			 ++namelessTimeless) {
			// Include all nameless/timeless entries in the match.

			Rmap rmap = (Rmap) elementAt(namelessTimeless);
			if (!rmap.isIdentifiable()) {
			    // A non-identifiable <code>Rmap</code> always
			    // matches the request.
			    matchesO.addElement(rmap);

			} else if (rmap.isNamelessTimeless()) {
			    byte matched = rmap.matches(requestI,reference);

			    if ((matched >= Rmap.MATCH_EQUAL) &&
				(matched <= Rmap.MATCH_INTERSECTION)) {
				matchesO.addElement(rmap);
			    } else if (matched >= Rmap.MATCH_AFTERNAME) {
				break;
			    } else {
				continue;
			    }

			} else {
			    break;
			}
		    }

		} else {
		    // When we have a name, we also want to match entries that
		    // don't have a name.
		    Rmap request2 = (Rmap) requestI.clone();
		    request2.setName(null);

		    boolean wantAll = request2.isNamelessTimeless();
		    for (namelessTimeless = 0;
			 namelessTimeless < size();
			 ++namelessTimeless) {
			Rmap rmap = (Rmap) elementAt(namelessTimeless);

			if (rmap.getName() != null) {
			    break;
			} else if (wantAll) {
			    matchesO.addElement(rmap);
			} else {
			    byte intersection =	rmap.matches(request2);
			    if ((intersection == Rmap.MATCH_EQUAL) ||
				(intersection == Rmap.MATCH_SUBSET) ||
				(intersection == Rmap.MATCH_SUPERSET) ||
				(intersection == Rmap.MATCH_INTERSECTION)) {
				matchesO.addElement(rmap);
			    }
			}
		    }
		}

		if (size() > namelessTimeless) {
		    // If there is anything other than the nameless and
		    // timeless entry, then we need to check for a real match.

		    // Locate an <code>Rmap</code> that matches the
		    // request. This could be the first, the last, or one in
		    // the middle.
		    int[] limits = new int[2];

		    limits[0] = namelessTimeless;
		    limits[1] = size() - 1;
		    int matched = locateAMatch(requestI,reference,limits);

		    if (matched < -size()) {
			// A really negative value indicates that the match
			// completely failed for some reason.
			reasonR = (byte)
			    (matched + size() + 1 +
			     (Rmap.MATCH_AFTER - Rmap.MATCH_UNKNOWN));

		    } else if (matched < size()) {
			// If there was a match, find any other matches.
			if ((matched == 0) || (matched == -1)) {
			    reasonR = Rmap.MATCH_AFTER;
			} else if ((matched == size() - 1) ||
				   (matched == -size())) {
			    reasonR = Rmap.MATCH_BEFORE;
			} else {
			    reasonR = Rmap.MATCH_INTERSECTION;
			}
		    
			if (matched >= 0) {
			    // If the first entry actually matched, then add
			    // the <code>Rmap</code> to the list of matches.
			    matchesO.addElement(elementAt(matched));
			}

			// Find more matches by looking at <code>Rmaps</code>
			// that come before the match until we can be sure that
			// there are no further possibliities.
			findOtherMatches(requestI,
					 reference,
					 matched,
					 -1,
					 limits,
					 matchesO);

			// Find the remaining matches by looking at
			// <code>Rmaps</code> that come after the match until
			// we can be sure that there are no further
			// possibilities.
			findOtherMatches(requestI,
					 reference,
					 matched,
					 1,
					 limits,
					 matchesO);
		    }
		}
	    }
	} finally {
	    decrementReadCount();
	}

	// Return the reason code.
	return (reasonR);
    }

    /**
     * Finds a list of all of the <code>Rmaps</code> matching the input name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name to match.
     * @return the list of <code>Rmaps</code> matching that name.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.io.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Modified to use version that takes a flag to indicate
     *			whether to look for all matches.
     * 08/07/2001  INB	Created.
     *
     */
    public final java.util.Vector findName(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (findName(nameI,true));
    }

    /**
     * Finds a list of all (one) of the <code>Rmaps</code> matching the input
     * name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name to match.
     * @param allI  match all or just one entry?
     * @return the list of <code>Rmaps</code> matching that name.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.io.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Created from earlier version that always found all
     *			matches. When dealing with registration maps, we know
     *			that only one match can possibly be found.
     *
     */
    public final java.util.Vector findName(String nameI,boolean allI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.util.Vector vectorR = new java.util.Vector();

	// Since the regular search code requires an <code>Rmap</code> and also
	// that both the name and the <code>TimeRange</code> match, this method
	// has its own binary search.
	int lo = 0,
	    hi = size() - 1,
	    idx;

	for (idx = ((lastName == -1) ?
		    (lo + hi)/2 :
		    ((lastName > hi) ?
		     hi :
		     lastName));
	     lo <= hi;
	     idx = (lo + hi)/2) {
	    Rmap entry = (Rmap) elementAt(idx);
	    int compared;

	    if ((compared = entry.compareNames(nameI)) == 0) {
		// If we found a matching name, then break out of the loop.
		lastName = idx + 1;
		vectorR.addElement(entry);
		break;

	    } else if (compared < 0) {
		// If the name of the entry comes before what we want, then
		// move the search up.
		lo = idx + 1;

	    } else {
		// If the name of the entry comes after what we want, then
		// move the search down.
		hi = idx - 1;
	    }
	}

	if (lo > hi) {
	    lastName = lo;
	} else if (allI) {
	    // If we broke out of the loop, then we found a match. If all
	    // matches are desired, then move backwards until we find the first
	    // entry that matches.
	    for (int idx1 = idx - 1; idx1 >= lo; --idx1) {
		Rmap entry = (Rmap) elementAt(idx1);

		if (entry.compareNames(nameI) == 0) {
		    // If we found another match, insert it into the return
		    // list.
		    vectorR.insertElementAt(entry,0);

		} else {
		    // Otherwise, we're done.
		    break;
		}
	    }

	    // Move forwards until we find the last entry that matches.
	    for (++idx; idx <= hi; ++idx) {
		Rmap entry = (Rmap) elementAt(idx);

		if (entry.compareNames(nameI) == 0) {
		    // If we found another match, add to the end of the list.
		    vectorR.addElement(entry);
		    lastName = idx + 1;

		} else {
		    // Otherwise, we're done.
		    break;
		}
	    }
	}

	return (vectorR);
    }

    /**
     * Finds other <code>Rmaps</code> that match the input request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the request <code>Rmap</code>.
     * @param referenceI  the reference <code>DataRequest</code>.
     * @param matchedI    the index of the matched <code>Rmap</code>.
     * @param incrementI  the increment to use to locate the matches.
     * @param limitsI	  the limits of the search.
     * @param matchesIO   the list of matched <code>Rmaps</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    private final void findOtherMatches
	(Rmap requestI,
	 DataRequest referenceI,
	 int matchedI,
	 int incrementI,
	 int[] limitsI,
	 java.util.Vector matchesIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	for (int idx = matchedI + incrementI;
	     (idx >= limitsI[0]) && (idx <= limitsI[1]);
	     idx += incrementI) {
	    // Try the other <code>Rmaps</code> in the vector before or after
	    // the match point until we hit one that definitely isn't in the
	    // list.
	    Rmap rmap = (Rmap) elementAt(idx);
	    byte intersection = rmap.matches(requestI,referenceI);

	    if ((intersection == Rmap.MATCH_EQUAL) ||
		(intersection == Rmap.MATCH_SUBSET) ||
		(intersection == Rmap.MATCH_SUPERSET) ||
		(intersection == Rmap.MATCH_INTERSECTION)) {
		// If the <code>Rmap</code> at the current position contains
		// anys information that could be of interest to us, we declare
		// it to be a match.
		matchesIO.addElement(rmap);

	    } else if ((intersection == Rmap.MATCH_BEFORENAME) ||
		       (intersection == Rmap.MATCH_BEFORE) ||
		       (intersection == Rmap.MATCH_AFTERNAME) ||
		       (intersection == Rmap.MATCH_AFTER)) {
		// If the <code>Rmap</code> at the current position comes
		// before or after the request, then we're done.
		break;
	    }
	}

	return;
    }

    /**
     * Finds a real <code>Rmap</code> that matches the input request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the request <code>Rmap</code>.
     * @param referenceI  the reference <code>DataRequest</code>.
     * @param matchedI    the index of the matched <code>Rmap</code>.
     * @param incrementI  the increment to use to locate the matches.
     * @param limitsI	  the limits of the search.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2001  INB	Created.
     *
     */
    private final Rmap findRealMatch(Rmap requestI,
				     DataRequest referenceI,
				     int matchedI,
				     int incrementI,
				     int[] limitsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;

	for (int idx = matchedI + incrementI;
	     (rmapR == null) && (idx >= limitsI[0]) && (idx <= limitsI[1]);
	     idx += incrementI) {
	    // Try the other <code>Rmaps</code> in the vector before or after
	    // the match point until we hit one that definitely isn't in the
	    // list.
	    Rmap rmap = (Rmap) elementAt(idx);
	    byte intersection = rmap.matches(requestI,referenceI);

	    if ((intersection == Rmap.MATCH_EQUAL) ||
		(intersection == Rmap.MATCH_SUBSET) ||
		(intersection == Rmap.MATCH_SUPERSET) ||
		(intersection == Rmap.MATCH_INTERSECTION)) {
		// If the <code>Rmap</code> at the current position contains
		// anys information that could be of interest to us, we declare
		// it to be a match.
		rmapR = rmap;

	    } else if ((intersection == Rmap.MATCH_BEFORENAME) ||
		       (intersection == Rmap.MATCH_BEFORE) ||
		       (intersection == Rmap.MATCH_AFTERNAME) ||
		       (intersection == Rmap.MATCH_AFTER)) {
		// If the <code>Rmap</code> at the current position comes
		// before or after the request, then we're done.
		break;
	    }
	}

	return (rmapR);
    }

    /**
     * Gets the outstanding read count.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the read count.
     * @see #decrementReadCount()
     * @see #incrementReadCount()
     * @since V2.0
     * @version 06/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    final int getReadCount() {
	return (readCount);
    }

    /**
     * Can this <code>RmapVector</code> handle this variety of request?
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return does this <code>RmapVector</code> handle this variety of
     *	       request?
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    boolean handles(Rmap requestI) {
	return (true);
    }

    /**
     * Increments the read counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #decrementReadCount()
     * @see #getReadCount()
     * @since V2.0
     * @version 06/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    final synchronized void incrementReadCount() {
	++readCount;
    }

    /**
     * Inserts a child at the specified location in the
     * <code>RmapVector</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI the <code>Rmap</code> to add.
     * @param indexI insert at this location.
     * @since V2.2
     * @version 02/17/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2004  INB	Created.
     *
     */
    final void insertAt(Rmap childI,int indexI) {
	synchronized (this) {
	    while (getReadCount() > 0) {
		try {
		    wait(TimerPeriod.LONG_WAIT);
		} catch (java.lang.InterruptedException e) {
		}
	    }

	    insertElementAt(childI,indexI);
	}
    }

    /**
     * Locates a match for the input request <code>Rmap</code>.
     * <p>
     * Generally this method returns the index of an <code>Rmap</code> that
     * matches the request in some way. However, it is possible for an
     * <code>Rmap</code> to have the same name as the request and a
     * <code>TimeRange</code> that overlaps the request, without having an
     * actual intersection with the request. Such an <code>Rmap</code> is in
     * the right general area of the <code>RmapVector</code> and it isn't clear
     * which way to move the search, so we return the index of such an
     * <code>Rmap</code> as -1 - index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the request <code>Rmap</code>.
     * @param referenceI  the reference <code>DataRequest</code>.
     * @param limitsIO    the low and high values of the search. These can be
     *			  used to bound further searches.
     * @return the index of the matching <code>Rmap</code>:
     *	       <br><ul>
     *	       <li>A value greater than or equal to 0 and less than the size of
     *		   the vector indicates a match at that index,</li>
     *	       <li>A value less than or to -1 and greater than or equal to
     *		   -(size of the vector) indicates that the entry at that
     *		   location is not an actual match, but is in the
     *		   vicinity, or</li>
     *	       <li>A negative value less than -(size of the vector) indicates
     *		   that no match is possible. The reason based on how much less
     *		   than -(size of the vector) the value is.</li>
     *	       </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
    private final int locateAMatch(Rmap requestI,
				   DataRequest referenceI,
				   int[] limitsIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int idxR = size(),
	    reason = Rmap.MATCH_UNKNOWN;
	boolean foundSomething = false;

	// Perform a binary search for a matching <code>Rmap</code>. We'll take
	// any <code>Rmap</code> that matches the input request.
	for (int idx = (limitsIO[0] + limitsIO[1])/2;
	     limitsIO[1] >= limitsIO[0];
	     idx = (limitsIO[0] + limitsIO[1])/2) {

	    Rmap rmap = (Rmap) elementAt(idx);
	    byte intersection = rmap.matches(requestI,referenceI);

	    if ((intersection == Rmap.MATCH_EQUAL) ||
		(intersection == Rmap.MATCH_SUBSET) ||
		(intersection == Rmap.MATCH_SUPERSET) ||
		(intersection == Rmap.MATCH_INTERSECTION)) {
		// If the <code>Rmap</code> at the current position
		// contains anys information that could be of interest to
		// us, we declare it to be a match.
		idxR = idx;
		foundSomething = true;
		break;

	    } else if ((intersection == Rmap.MATCH_BEFORENAME) ||
		       (intersection == Rmap.MATCH_BEFORE)) {
		// If the <code>Rmap</code> at the current position comes
		// before the request, then push it off the low side of the
		// search.
		limitsIO[0] = idx + 1;
		reason = intersection;

	    } else if ((intersection == Rmap.MATCH_AFTERNAME) ||
		       (intersection == Rmap.MATCH_AFTER)) {
		// If the <code>Rmap</code> at the current position comes
		// after the request, then push it off the high side of the
		// search.
		limitsIO[1] = idx - 1;
		reason = intersection;

	    } else {
		// If we get here, we have an odd relationship between the
		// request and this <code>Rmap</code>. They have the same
		// name and have <code>TimeRanges</code> that overlap in
		// time, but do not have an intersection. We're in the
		// right general area, so return this <code>Rmap</code> as
		// a start point, but not as a match.
		idxR = -1 - idx;
		foundSomething = true;
		break;
	    }
	}

	if (!foundSomething) {
	    if ((limitsIO[0] >= size()) || (limitsIO[1] <= 0)) {
		// If we went off an end of the list, return a code indicating
		// that that happened.
		idxR = (reason -
			(size() + 1 +
			 (Rmap.MATCH_AFTER - Rmap.MATCH_UNKNOWN)));
	    }
	}

	return (idxR);
    }

    /**
     * Removes the object that matches the input value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sortValueI the sorting value to match.
     * @return success status.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    public final synchronized boolean remove(Object sortValueI) {
	while (getReadCount() > 0) {
	    try {
		wait(60000);
	    } catch (java.lang.InterruptedException e) {
	    }
	}
	return (super.remove(sortValueI));
    }

    /**
     * Remove the element at the specified index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the element to remove.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2001  INB	Created.
     *
     */
    public final synchronized void removeEntryAt(int indexI) {
	while (getReadCount() > 0) {
	    try {
		wait(60000);
	    } catch (java.lang.InterruptedException e) {
	    }
	}

	super.removeElementAt(indexI);
    }
}
