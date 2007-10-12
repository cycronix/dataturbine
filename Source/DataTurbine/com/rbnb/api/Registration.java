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
 * Stores <code>Rmap</code> hierarchy name information.
 * <p>
 * In many cases, the server may need to have fast access to the names, the
 * data limits, and other abstracted information for a
 * <code>Source</code>. This class provides a place to store that information
 * that is separated from the standard <code>Rmap</code> hierarchy of the
 * server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/28/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/14/2003  INB	Eliminated <code>updateFromParent</code> - it is not
 *			used.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/12/2003  INB	Modified list of parent types handled by
 *			<code>updateFromParent</code>. Modified
 *			<code>updateLowerRegistration</code> to use new
 *			<code>RmapVector.findName</code> version that allows us
 *			to look for just a single match.
 * 01/02/2001  INB	Created.
 *
 */
class Registration
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.RegisteredInterface
{
    /**
     * is this <code>Registration</code> being updated?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private boolean updating = false;

    /**
     * last reset time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long lastReset = -Long.MAX_VALUE;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #Registration(String)
     * @see #Registration(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    Registration() {
	super();
    }

    /**
     * Class constructor to build a <code>Registration</code> from the input
     * name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name to use.
     * @see #Registration()
     * @see #Registration(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    Registration(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	setName(nameI);
    }

    /**
     * Class constructor to build an <code>Rmap</code> by reading it from an
     * input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #Registration()
     * @see #Registration(String)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/07/2001  INB	Created.
     *
     */
    Registration(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(isI,disI);
    }

    /**
     * Class constructor to build an <code>Rmap</code> by reading it from an
     * input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code> to use to fill in missing
     *		     information.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #Registration()
     * @see #Registration(String)
     * @since V2.0
     * @version 12/06/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2001  INB	Created.
     *
     */
    Registration(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(otherI,isI,disI);
    }

    /**
     * Compares the sorting value of this <code>Registration</code> to the
     * input sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * This method overrides the one in <code>Rmap</code> to compare only the
     * names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Rmap</code> compares less than the input,
     *	       <p> 0 if this <code>Rmap</code> compares equal to the input, and
     *	       <p>>0 if this <code>Rmap</code> compares greater than the input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @see #compareTo(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    public int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	if (sidI != null) {
	    // Only the null sort identifier is supported.
	    throw new com.rbnb.utility.SortException
		("The sort identifier for Rmaps must be null.");
	}

	if (this == otherI) {
	    // A <code>Rmap</code> is always equal to itself.
	    return (0);
	}

	// The input sorting value is just the other <code>Rmap</code>.
	Rmap other = (Rmap) otherI;

	// Compare the names first. Null names come before any real names.
	return (compareNames(other));
    }

    /**
     * Gets the last reset time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last reset time.
     * @see #setLastReset(long)
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
    private long getLastReset() {
	return (lastReset);
    }

    /**
     * Gets the owner of this <code>Registration</code>.
     * <p>
     * The owner is the <code>FrameManager</code>, <code>StorageManager</code>,
     * or <code>SourceHandler</code> subclass that is the first ancestor of
     * this <code>Registration</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the owner.
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
     * @version 05/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/30/2001  INB	Created.
     *
     */
    final Rmap getOwner()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap ownerR = getParent();

	if ((ownerR != null) &&
	    (ownerR instanceof Registration)) {
	    ownerR = ((Registration) ownerR).getOwner();
	}

	return (ownerR);
    }

    /**
     * Gets the registration list for this <code>RBO</code> matching the input
     * hierarchy.
     * <p>
     * At the moment, the only valid input is an <code>Rmap</code> with the
     * same name as this <code>RBO</code> with an optional child named "...".
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
     * @version 03/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2002  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean sameLevel = false;
	Registration child;
	Rmap rmapR = null,
	    rmap,
	    subRequest;
	String prepend = "/";

	if (getName() == null) {
	    if (requestI.getName() == null) {
		rmapR = new Rmap();

	    } else if (requestI.compareNames("*") == 0) {
		rmapR = new Rmap();
		if (getTrange() != null) {
		    rmapR.setTrange((TimeRange) getTrange().clone());
		}
		if (getFrange() != null) {
		    rmapR.setFrange((TimeRange) getFrange().clone());
		}
		if (getDblock() != null) {
		    rmapR.setDblock((DataBlock) getDblock().clone());
		}
		for (int idx = 0,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    child = (Registration) getChildAt(idx);
		    if ((rmap = child.getRegistered(requestI)) != null) {
			rmapR.addChild(rmap);
		    }
		}
		sameLevel = true;

	    } else {
		if ((child = (Registration) findDescendant
		     ("/" + requestI.getName(),
		      false)) != null) {
		    rmapR = child.getRegistered(requestI);
		}

		sameLevel = true;
	    }

	} else if (requestI.getName() == null) {
	    if (requestI.getNchildren() == 0) {
		rmapR = new Rmap(getName());
		prepend += getName() + "/";

	    } else {
		rmapR = new Rmap();
		sameLevel = true;
		for (int idx = 0,
			 endIdx = requestI.getNchildren();
		     idx < endIdx;
		     ++idx) {
		    subRequest = requestI.getChildAt(idx);

		    if ((rmap = getRegistered(subRequest)) != null) {
			rmapR.addChild(rmap);
		    }
		}
	    }

	} else if ((compareNames(requestI) == 0) ||
		   (requestI.compareNames("*") == 0)) {
	    rmapR = new Rmap(getName());
	    prepend += getName() + "/";
	}

	if ((rmapR != null) && !sameLevel) {
	    if (getTrange() != null) {
		rmapR.setTrange((TimeRange) getTrange().clone());
	    }
	    if (getFrange() != null) {
		rmapR.setFrange((TimeRange) getFrange().clone());
	    }
	    if (getDblock() != null) {
		rmapR.setDblock((DataBlock) getDblock().clone());
	    }

	    if (requestI.getNchildren() == 0) {
		if ((getNchildren() > 0) &&
		    (getChildAt(0).getName() == null)) {
		    rmapR.addChild((Rmap) getChildAt(0).clone());
		}

	    } else {
		for (int idx = 0,
			 endIdx = requestI.getNchildren();
		     idx < endIdx;
		     ++idx) {
		    subRequest = requestI.getChildAt(idx);

		    if (subRequest.getName() == null) {
			for (int idx1 = 0,
				 endIdx1 = getNchildren();
			     idx1 < endIdx1;
			     ++idx1) {
			    child = (Registration) getChildAt(idx1);

			    if ((rmap = child.getRegistered
				 (subRequest)) != null) {
				rmapR.addChild(rmap);
			    }
			}

		    } else if (subRequest.compareNames("...") == 0) {
			rmapR = (Rmap) clone();
			break;

		    } else if (subRequest.compareNames("*") == 0) {
			for (int idx1 = 0,
				 endIdx1 = getNchildren();
			     idx1 < endIdx1;
			     ++idx1) {
			    child = (Registration) getChildAt(idx1);

			    if (((rmap = child.getRegistered
				  (subRequest)) != null) &&
				((rmap.getName() != null) ||
				 (rmap.getNchildren() > 0))) {
				rmapR.addChild(rmap);
			    }
			}

		    } else {
			if ((child = (Registration) findDescendant
			     (prepend + subRequest.getName(),
			      false)) != null) {
			    if ((rmap = child.getRegistered
				 (subRequest)) != null) {
				rmapR.addChild(rmap);
			    }
			}
		    }
		}
	    }
	}

	return (rmapR);
    }

    /**
     * Is this <code>Registration</code> identifiable (by name)?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this identifiable?
     * @since V2.0
     * @version 05/10/2001
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
	return (getName() != null);
    }

    /**
     * Could the <code>Rmap</code> hierarchy represented by this
     * <code>Registration</code> possibly contain information matching the
     * input <code>Rmap</code> request?
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the <code>Rmap</code> request.
     * @return the match results.
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
     *		  thrown if the information matching the request cannot be
     *		  extracted for any reason.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    final byte isWithinLimits(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte matchesR = MATCH_NOINTERSECTION;

	if ((getNchildren() == 0) &&
	    ((getTrange() != null) || (getFrange() != null)) &&
	    !(getParent() instanceof Registration)) {
	    // Special case: <code>TimeRange</code> only
	    // <code>Registration</code> at top of hierarchy. In this case,
	    // we just need to check against the highest level
	    // <code>TimeRange(s)</code> in the hierarchy.

	    if (requestI == null) {
		// With no input request, we always have to check the actual
		// data for a match.
		matchesR = MATCH_EQUAL;

	    } else if ((requestI.getTrange() != null) &&
		       (getTrange() != null)) {
		// With a <code>TimeRange</code> in the input request, see if
		// there is a match between the it and that of this
		// <code>Registration</code>.

		matchesR = getTrange().matches(requestI.getTrange());
                
// System.err.println("mjm Registration matchesR: "+matchesR+", getTrange: "+getTrange()+", reqTrange: "+requestI.getTrange()+", request: "+requestI);

	    } else if ((requestI.getFrange() != null) &&
		       (getFrange() != null)) {
		// With a frame <code>TimeRange</code> in the input request,
		// see if there is a match between the it and that of this
		// <code>Registration</code>.
		matchesR = getFrange().matches(requestI.getFrange());

	    } else {
		// With no <code>TimeRange</code> in the input request, we
		// always have to check the actual data that the
		// <code>Registration</code> map describes.
		matchesR = MATCH_EQUAL;
	    }

	} else {
	    // Normal case. Try extracting from the
	    // <code>Registration</code>.
	    RmapExtractor extractor = new RmapExtractor
		(requestI,
		 true,
		 true,
		 true,
		 false);
	    if (extractor.extract(this) != null) {
		// If we get anything, then there is a match.
		matchesR = MATCH_INTERSECTION;
	    }
	}

	return (matchesR);
    }

    /**
     * Could the <code>Rmap</code> hierarchy represented by this
     * <code>Registration</code> possibly contain information matching the
     * unsatisfied <code>ExtractedChain</code>?
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI    the <code>RmapExtractor</code> performing the
     *			    extraction.
     * @param unsatisfiedI  the unsatisfied <code>ExtractedChain</code>.
     * @return the match results.
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
     *		  thrown if the information matching the request cannot be
     *		  extracted for any reason.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2001  INB	Created.
     *
     */
    final byte isWithinLimits(RmapExtractor extractorI,
			      ExtractedChain unsatisfiedI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte matchesR = MATCH_UNKNOWN;

	if ((getNchildren() == 0) &&
	    (getTrange() != null) &&
	    !(getParent() instanceof Registration)) {
	    // Special case: <code>TimeRange</code> only
	    // <code>Registration</code> at top of hierarchy. In this case,
	    // we just need to check against the highest level
	    // <code>TimeRange(s)</code> in the hierarchy.

	    if (unsatisfiedI.getInherited() == null) {
		// If there is no inherited request, then use the current
		// request as a match.
		matchesR = isWithinLimits(unsatisfiedI.getRequest());

	    } else {
		// With an inherited request, we may need to use both the
		// inherited request and the current request to get the
		// <code>TimeRange</code>.
		boolean tried = false;

		if ((unsatisfiedI.getRequest() != null) &&
		    (unsatisfiedI.getRequest().getTrange() != null)) {
		    // If there is a <code>TimeRange</code> in the current
		    // request, then first check against it. With a match,
		    // we'll be done.
            
		    matchesR = isWithinLimits(unsatisfiedI.getRequest());
		    tried = true;
		}

		if ((matchesR != MATCH_EQUAL) &&
		    (matchesR != MATCH_SUBSET) &&
		    (matchesR != MATCH_SUPERSET) &&
		    (matchesR != MATCH_INTERSECTION)) {
		    // If we haven't found a match yet, try using the inherited
		    // request.

		    if (!tried) {
			// If there was no <code>TimeRange</code> in the
			// current request, then we can use just the inherited
			// request.                                     
			matchesR = isWithinLimits(unsatisfiedI.getInherited());

		    } else {
			// Otherwise, we need to combine the two request
			// <code>TimeRanges</code>.
			TimeRange combinedT =
			    unsatisfiedI.getRequest().getTrange().add
			    (unsatisfiedI.getInherited().getTrange());
			Rmap combined = new Rmap(null,null,combinedT);         
			matchesR = isWithinLimits(combined);
		    }
		}
	    }

	} else {
	    // Normal case. Try extracting from the
	    // <code>Registration</code>. If we get anything, then there is a
	    // match.                           
	    RmapExtractor extractor = new RmapExtractor
		(extractorI.getWorkRequest(),
		 extractorI.getExtractRmaps(),
		 extractorI.getExtractFrame(),
		 extractorI.getExtractTime(),
		 false);
	    ExtractedChain eChain = new ExtractedChain
		(extractor,
		 unsatisfiedI.getRequest());
	    eChain.setInherited(unsatisfiedI.getInherited());
	    matchesR = moveDownFrom(extractor,eChain,null);
	    if (extractor.combine(matchesR) != null) {
		matchesR = MATCH_INTERSECTION;
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
     * @param unsatisfiedO  the new unsatisified <code>ExtractedChains</code>.
     * @return the reason for a failed match.
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
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/22/2001 INB	Created.
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
	// Determine if the <code>TimeRanges</code> for this
	// <code>Registration</code> are out of date.
	long resetAt =
	    ((getParent() instanceof Registration) ?
	     ((Registration) getParent()).getLastReset() :
	     getLastReset());
	boolean reset = (getLastReset() < resetAt);

	if (reset) {
	    // If the <code>TimeRanges</code> should have been reset, then we
	    // can just delete them and update the reset time.
	    setTrange(null);
	    setFrange(null);
	    setLastReset(resetAt);
	}

	// Now perform the match.
	return (super.moveDownFrom(extractorI,unsatisfiedI,unsatisfiedO));
    }

    /**
     * Sets the last reset time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI  the time of the last reset in milliseconds.
     * @see #getLastReset()
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
    private final void setLastReset(long timeI) {
	lastReset = timeI;
    }

    /**
     * Updates the registration based on the input <code>Rmap</code>
     * hierarchy.
     * <p>
     * This method recursively creates a matching <code>Registration</code>
     * hierarchy under this <code>Registration</code> for the input
     * <code>Rmap</code> hierarchy.
     * <p>
     * If <code>resetI</code> is set, the frame and time information in the
     * input <code>Rmap</code> hierarchy is just copied into the
     * <code>Registration</code>.
     * <p>
     * If <code>resetI</code> is clear, the frame and time information in the
     * input <code>Rmap</code> hierarchy extends the ranges already in the
     * <code>Registration</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI	the <code>Rmap</code> hierarchy.
     * @param resetI	reset the frame and time limits to match those of the
     *			input hierarchy?
     * @param keepDataI keep the <code>DataBlock</code> from the input?
     *			<p><ul>
     *			<li>false - place a one-byte <code>DataBlock</code>
     *			    marker wherever there is a <code>DataBlock</code>
     *			    in the input, or</li>
     *			<li>true - copy the <code>DataBlock</code> from the
     *			    input into the <code>Registration</code>.</li>
     *			</ul>
     * @return were any changes made?
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
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    final boolean updateRegistration(Rmap rmapI,
				     boolean resetI,
				     boolean keepDataI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean changedR = false;

	if ((getName() == null) && (rmapI.getName() != null)) {
	    // Special case: at the top of the <code>Registration</code>
	    // hierarchy, there is always an unnamed entry. If the input
	    // <code>Rmap</code> has a name, then we need to move down a
	    // level.
	    changedR = updateLowerRegistration(rmapI,
					       resetI,
					       keepDataI,
					       null,
					       false,
					       null,
					       null);

	} else {
	    // Otherwise, we process things at this level.
	    changedR = updateRegistration(rmapI,
					  resetI,
					  keepDataI,
					  null,
					  false,
					  null,
					  null);
	}

	return (changedR);
    }
    
    /**
      * Overrides the auto-generated limits with existing registration data.
      *  If the existing data is an rbnb document, instead merge the user
      *  data from the input with the server meta-data.
      * <p>
      * @author WHF
      * @since V2.5B4
      */
    // 2005/03/23  WHF  Created.    
    // 2006/03/24  EMF  Fixed bug that occurred when updating userinfo
    //                  on reloaded archive, wherein only userinfo would
    //                  get returned, rather than server xml + userinfo
    // 2006/04/19  EMF  Do the same for limits, not just input.

    private void updateLimitsFromKeptInput(Registration limits, Rmap input)
    {
//System.err.println("Registration.updateLimitsFromKeptInput: limits "+limits.getDblock().getData().firstElement());
//System.err.println("input ************************ "+input.getDblock().getData().firstElement());

        //EMF 3/24/06: if input is of type USER, change it to xml format
        // else skips following if block, and just gets cloned at end of
        // method - such cloning is for backwards compatibility, before there
        // was a type USER...
        if (input.getDblock().getDtype() == DataBlock.TYPE_USER) {
            String userStr = input.xmlRegistration();
            input.setDblock((new DataBlock(userStr,1,userStr.length(),
                             DataBlock.TYPE_STRING,"text/xml",
                             DataBlock.ORDER_MSB,false,0,userStr.length())));
        }
//System.err.println("input after xml-ify ************************ "+input.getDblock().getData());
	if (limits.getDblock().getDtype() == DataBlock.TYPE_USER) {
            String userStr = limits.xmlRegistration();
            limits.setDblock((new DataBlock(userStr,1,userStr.length(),
                             DataBlock.TYPE_STRING,"text/xml",
                             DataBlock.ORDER_MSB,false,0,userStr.length())));
        }
//System.err.println("limits after xml-ify ************************ "+limits.getDblock().getData());
	
	if ("text/xml".equals(input.getDblock().getMIMEType())) {
	    String[] dataArray = new String[1];
	    input.getDblock().getDataPoints(0,
					     dataArray,
					     0,
					     1);
	    if (dataArray[0].indexOf("<!DOCTYPE rbnb") != -1) {
		// We have server meta-data in the input.  Merge:
		String data = dataArray[0];
		int index = data.indexOf("<user>");
		if (index >= 0) {
		    int index2 = data.indexOf("</user>");
		    if (index2 >= 0) {
			// Get user data, including user tags:
			String userStr = data.substring(index, index2+7);
			limits.getDblock().getDataPoints(0,
							 dataArray,
							 0,
							 1);
			// 2007/10/11  WHF  Replace old user data with new
			//  user data:
			/* 
			// Merge user data with server-meta:
			index = dataArray[0].indexOf("</rbnb>");
			data = dataArray[0].substring(0, index)+"  "+userStr
					+'\n'+dataArray[0].substring(index);
			*/
			String reg = dataArray[0]; // current registration
			index = reg.indexOf("<user>");
			index2 = reg.indexOf("</user>");
			if (index != -1 && index2 != -1)
			    data = reg.substring(0, index) + userStr
				    + reg.substring(index2 + "</user>".length());
			else
			    index = dataArray[0].indexOf("</rbnb>");
			    data = dataArray[0].substring(0, index)+"  "+userStr
					+'\n'+dataArray[0].substring(index);
			
			limits.setDblock(new DataBlock(data,
						       1,
						       data.length(),
						       DataBlock.TYPE_STRING,
							   "text/xml",
						       DataBlock.ORDER_MSB,
						       false,
						       0,
						       data.length())
			);
			return;
		    }
		}
	    }
	}
	
	// If we should keep the data from the input, then
	// determine what we have in the input.
	limits.setDblock((DataBlock) input.getDblock().clone());
    }


    /**
     * Updates this <code>Registration</code> based on the input
     * <code>Rmap</code>.
     * <p>
     * This method recursively creates a matching <code>Registration</code>
     * hierarchy under this <code>Registration</code> for the input
     * <code>Rmap</code> hierarchy.
     * <p>
     * If <code>resetI</code> is set, the frame and time information in the
     * input <code>Rmap</code> hierarchy is just copied into the
     * <code>Registration</code>.
     * <p>
     * If <code>resetI</code> is clear, the frame and time information in the
     * input <code>Rmap</code> hierarchy extends the ranges already in the
     * <code>Registration</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI	  the <code>Rmap</code> to work from.
     * @param resetI	  reset the time and frame range information?
     * @param keepDataI   keep the <code>DataBlock</code> from the input?
     *			  <p><ul>
     *			  <li>false - place a one-byte <code>DataBlock</code>
     *			      marker wherever there is a <code>DataBlock</code>
     *			      in the input, or</li>
     *			  <li>true - copy the <code>DataBlock</code> from the
     *			      input into the <code>Registration</code>.</li>
     *			  </ul>
     * @param tLimitsI	  the inherited time limits information.
     * @param tInclusiveI are the time limits inclusive of both ends?
     * @param fLimitsI	  the inherited frame limits information.
     * @param membershipO group membership list; null if not in a group.
     * @return were any changes made?
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
     * @version 12/09/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2001  INB	Created.
     *
     */
    private final boolean updateRegistration
	(Rmap rmapI,
	 boolean resetI,
	 boolean keepDataI,
	 double[] tLimitsI,
	 boolean tInclusiveI,
	 double[] fLimitsI,
	 com.rbnb.utility.SortedVector membershipO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean changedR = false;

	// Determine if the <code>TimeRanges</code> for this
	// <code>Registration</code> need to be completely reset.
	boolean reset = resetI;
	long resetAt = 0;
	if (reset) {
	    resetAt = System.currentTimeMillis();
	} else if (!resetI) {
	    resetAt =
		((getParent() instanceof Registration) ?
		 ((Registration) getParent()).getLastReset() :
		 getLastReset());
	    reset = (getLastReset() < resetAt);
	}

	// Calculate the time and frame range information.
	double[] wTLimits = tLimitsI,
		 wFLimits = fLimitsI;
	boolean wTInclusive = tInclusiveI;
	TimeRange rrange;

	if (rmapI.getTrange() != null) {
	    // If the <code>Rmap</code> has time information, then combine
	    // it with the input time information.
	    double[] limitsV = rmapI.getTrange().getLimits();

	    if (wTLimits == null) {
		wTLimits = limitsV;
		wTInclusive = rmapI.getTrange().isInclusive();
	    } else {
		double limits1 = wTLimits[0] + limitsV[1];
		wTLimits[0] += limitsV[0];
		wTLimits[1] = Math.min(wTLimits[1],limits1);
		if (wTLimits[1] == limits1) {
		    wTInclusive = rmapI.getTrange().isInclusive();
		}
	    }
	}

	if (rmapI.getFrange() != null) {
	    // If the <code>Rmap</code> has frame information, then it
	    // replaces the input frame information.
	    wFLimits = rmapI.getFrange().getLimits();
	}

	Registration limits = null;
	if (rmapI.getDblock() == null) {
	    // If the input <code>Rmap</code> does not have data, then we
	    // may want to clear out the registration limits.
	    if (reset &&
		((getNchildren() > 0) &&
		 ((limits =
		   (Registration) getChildAt(0)).getName() == null))) {
		// If there is a limits entry, we either delete the time/frame
		// information or the entry itself.

		if ((limits.getDblock().getNpts() == 1) &&
		    (limits.getDblock().getPtsize() == 1) &&
		    (limits.getDblock().getDtype() == DataBlock.UNKNOWN)) {
		    // If the limits entry has just a marker
		    // <code>DataBlock</code>, we can eliminate it entirely.
		    removeChildAt(0);

		} else {
		    // Otherwise, we need to eliminate the time information.
		    limits.setTrange(null);
		    limits.setFrange(null);
		}
		setLastReset(resetAt);

		// Note that we've made changes.
		changedR = true;
	    }

	} else {
	    // If the input <code>Rmap</code> has data, then we need to
	    // store limit information for this <code>Rmap</code>.

	    if ((getNchildren() == 0) ||
		((limits =
		  (Registration) getChildAt(0)).getName() != null)) {
		// If there is no existing limit information, then create a
		// new one.
		limits = new Registration();
		if ((keepDataI ||
		     (rmapI instanceof Registration)) &&
		    (rmapI.getDblock().getNpts() > 0)
		    // 2005/03/22  WHF  Also check to see if not 'user' data:
		    //  if it is not, do not use server data:
		    && rmapI.getDblock().getDtype() != DataBlock.TYPE_USER) {
		    limits.setDblock((DataBlock) rmapI.getDblock().clone());
		} else if (rmapI.getDblock().getNpts() > 0) {
		    String userdata = rmapI.xmlRegistration();
		    limits.setDblock(new DataBlock(userdata,
						   1,
						   userdata.length(),
						   DataBlock.TYPE_STRING,
						   DataBlock.ORDER_MSB,
						   false,
						   0,
						   userdata.length()));
		    limits.getDblock().setMIMEType("text/xml");
		} else {
		    limits.setDblock(MarkerBlock);
		}
		addChild(limits);

	    } else {
		// If there is an existing limit, determine whether or not to
		// update its <code>DataBlock</code> and whether to reset its
		// time information.
		if ((keepDataI ||
		     (rmapI instanceof Registration)) &&
		    (rmapI.getDblock().getNpts() > 0)) {
		    updateLimitsFromKeptInput(limits, rmapI);
			    
		} else {
		    // If we don't want to keep the data from the input, then
		    // flag it that way.
		    boolean markIt = false;
		    if (limits.getDblock().getMIMEType().equals("text/xml") &&
			(limits.getDblock().getDtype() ==
			 DataBlock.TYPE_STRING) &&
			(limits.getDblock().getNpts() == 1)) {
			String[] data = new String[1];
			limits.getDblock().getDataPoints(0,
							 data,
							 0,
							 1);
			if (data[0].indexOf("<!DOCTYPE rbnb") != -1) {
			    markIt = true;
			}

		    } else if ((limits.getDblock().getNpts() != 1) ||
			       (limits.getDblock().getPtsize() != 1) ||
			       (limits.getDblock().getDtype() !=
				DataBlock.UNKNOWN)) {
			markIt = true;
		    }

		    if (markIt) {
			String userdata = rmapI.xmlRegistration();
			limits.setDblock(new DataBlock(userdata,
						       1,
						       userdata.length(),
						       DataBlock.TYPE_STRING,
						       DataBlock.ORDER_MSB,
						       false,
						       0,
						       userdata.length()));
			limits.getDblock().setMIMEType("text/xml");
		    }
		}

		if (reset) {
		    // If we're supposed to reset this entry, do so.
		    limits.setTrange(null);
		    limits.setFrange(null);
		    setLastReset(resetAt);
		}
	    }

	    // Update the limits information.
	    if (wTLimits != null) {
		if ((wTLimits[1] - wTLimits[0]) < 0.) {
		    if ((wTLimits[0] - wTLimits[1]) < 0.) {
			wTLimits[0] = Double.NEGATIVE_INFINITY;
			wTLimits[1] = Double.POSITIVE_INFINITY;
		    } else {
			double temp = wTLimits[1];
			wTLimits[1] = wTLimits[0];
			wTLimits[0] = temp;
		    }
		}
		if (limits.getTrange() == null) {
		    limits.setTrange
			(rrange = new TimeRange
			    (wTLimits[0],
			     wTLimits[1] - wTLimits[0]));
		    rrange.setInclusive(wTInclusive);

		} else {
		    limits.getTrange().addLimits(wTLimits);
		    if (limits.getTrange().getPtimes()[0] +
			limits.getTrange().getDuration() == wTLimits[1]) {
			limits.getTrange().setInclusive(wTInclusive);
		    }
		}
	    }

	    if (wFLimits != null) {
		if ((wFLimits[1] - wFLimits[0]) < 0.) {
		    if ((wFLimits[0] = wFLimits[1]) < 0.) {
			wFLimits[0] = Double.NEGATIVE_INFINITY;
			wFLimits[1] = Double.POSITIVE_INFINITY;
		    } else {
			double temp = wFLimits[1];
			wFLimits[1] = wFLimits[0];
			wFLimits[0] = temp;
		    }
		}
		if (limits.getFrange() == null) {
		    limits.setFrange
			(rrange = new TimeRange
			    (wFLimits[0],
			     wFLimits[1] - wFLimits[0]));
		    rrange.setInclusive(true);

		} else {
		    limits.getFrange().addLimits(wFLimits);
		}
	    }

	    // Note that we've made changes.
	    changedR = true;
	}

	// Update the next level.
	changedR = (updateNextLevelRegistration(rmapI,
						reset,
						keepDataI,
						wTLimits,
						wTInclusive,
						wFLimits,
						membershipO) ||
		    changedR);

	return (changedR);
    }

    /**
     * Updates a lower level registration by locating the entry corresponding
     * to the input <code>Rmap</code> and then updating that entry.
     * <p>
     * If no entry can be found, this method creates one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI	  the <code>Rmap</code> to work from.
     * @param resetI	  reset the time and frame range information?
     * @param keepDataI   keep the <code>DataBlock</code> from the input?
     *			  <p><ul>
     *			  <li>false - place a one-byte <code>DataBlock</code>
     *			      marker wherever there is a <code>DataBlock</code>
     *			      in the input, or</li>
     *			  <li>true - copy the <code>DataBlock</code> from the
     *			      input into the <code>Registration</code>.</li>
     *			  </ul>
     * @param tLimitsI	  the inherited time limits information.
     * @param tInclusiveI are the time limits inclusive of both ends?
     * @param fLimitsI	  the inherited frame limits information.
     * @param membershipO group membership list; null if not in a group.
     * @return were any changes made?
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
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Look for a single match - for registration maps, only
     *			one is possible.
     * 09/20/2001  INB	Created.
     *
     */
    private final boolean updateLowerRegistration
	(Rmap rmapI,
	 boolean resetI,
	 boolean keepDataI,
	 double[] tLimitsI,
	 boolean tInclusiveI,
	 double[] fLimitsI,
	 com.rbnb.utility.SortedVector membershipO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean changedR = false;
	Registration lower = null;

	if (getNchildren() != 0) {
	    // If this <code>Registration</code> contains children, then we
	    // need to see if we can find a matching entry.
	    java.util.Vector matches = getChildren().findName(rmapI.getName(),
							      false);
	    if ((matches != null) && (matches.size() > 1)) {
		throw new java.lang.InternalError();
	    }
	    if (matches.size() == 1) {
		lower = (Registration) matches.firstElement();
	    }
	}

	if (lower == null) {
	    // If no such <code>Registration</code> exists, create one.
	    lower = new Registration(rmapI.getName());
	    addChild(lower);
	    changedR = true;
	}

	// Update the lower level entry.
	changedR =
	    lower.updateRegistration(rmapI,
				     resetI,
				     keepDataI,
				     tLimitsI,
				     tInclusiveI,
				     fLimitsI,
				     membershipO) ||
	    changedR;
	return (changedR);
    }

    /**
     * Looks at the children of the input <code>Rmap</code> and determines how
     * to move down the <code>Registration</code> hierarchy for each of those
     * children.
     * <p>
     * The decision is based on whether or not the child is named:
     * <p><ul>
     * <li>Unnamed children correspond to this <code>Registration</code>,
     *	   and</li>
     * <li>Named children correspond to an entry in the next lower level of the
     *     <code>Registration</code> hierarchy.</li>
     * </u><p>
     *
     * @author Ian Brown
     *
     * @param rmapI	  the <code>Rmap</code> to work from.
     * @param resetI	  reset the time and frame range information?
     * @param keepDataI   keep the <code>DataBlock</code> from the input?
     *			  <p><ul>
     *			  <li>false - place a one-byte <code>DataBlock</code>
     *			      marker wherever there is a <code>DataBlock</code>
     *			      in the input, or</li>
     *			  <li>true - copy the <code>DataBlock</code> from the
     *			      input into the <code>Registration</code>.</li>
     *			  </ul>
     * @param tLimitsI	  the inherited time limits information.
     * @param tInclusiveI are the time limits inclusive of both ends?
     * @param fLimitsI	  the inherited frame limits information.
     * @param membershipO group membership list; null if not in a group.
     * @return were any changes made?
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
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2001  INB	Created.
     *
     */
    private final boolean updateNextLevelRegistration
	(Rmap rmapI,
	 boolean resetI,
	 boolean keepDataI,
	 double[] tLimitsI,
	 boolean tInclusiveI,
	 double[] fLimitsI,
	 com.rbnb.utility.SortedVector membershipO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean changedR = false;

	if (rmapI.getNmembers() > 0) {
	    throw new java.lang.IllegalStateException
		("Group membership is not yet supported.");
	}

	for (int idx = 0,
		 endIdx = rmapI.getNchildren();
	     idx < endIdx;
	     ++idx) {
	    // Work through the input <code>Rmap's</code> children.
	    Rmap child = rmapI.getChildAt(idx);

	    if (child.getName() == null) {
		// For unnamed children, work at the current level.
		changedR = updateRegistration(child,
					      resetI,
					      keepDataI,
					      tLimitsI,
					      tInclusiveI,
					      fLimitsI,
					      membershipO);

	    } else {
		// For named children, work down a level.
		changedR = updateLowerRegistration(child,
						   resetI,
						   keepDataI,
						   tLimitsI,
						   tInclusiveI,
						   fLimitsI,
						   null);
	    }
	}

	return (changedR);
    }
}
