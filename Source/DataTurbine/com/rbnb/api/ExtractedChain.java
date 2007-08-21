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
 * Extends <code>RmapChain</code> to provide for its construction from source
 * <code>Rmaps</code> matched against request <code>Rmaps</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/02/2004
 */

/*
 * Copyright 2000, 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/29/2004  INB	Took out check for SourceHandler in delay request
 *			check in matchRmap.  This is marked with a comment if
 *			it turns out	there is a need for it.
 * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
 *			<code>RmapVector()</code>.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/12/2003  INB	Make sure the combined requests include the children
 *			and make sure that extracted requests have marker
 *			<code>DataBlocks</code>.
 * 12/08/2000  INB	Created.
 *
 */
final class ExtractedChain
    extends com.rbnb.api.RmapChain
{
    /**
     * performing a delayed move down the request hierarchy?
     * <p>
     * When the next level of the request hierarchy consists of a number of
     * named <code>Rmaps</code>, it is more efficient to temporarily delay
     * moving down until the source hierarchy also contains names. This avoids
     * things like reading <code>FrameSets</code> from <code>FileSets</code> in
     * the <code>Archive</code> multiple times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/17/2002
     */
    private boolean delayRequest = false;
    
    /**
     * does the inherited request include any frame information that hasn't
     * been matched at all?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/11/2000
     */
    private boolean needFrameMatch = false;

    /**
     * does the inherited request include any time information that hasn't
     * been matched at all?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/11/2000
     */
    private boolean needTimeMatch = false;

    /**
     * the inherited request from the previous level.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/26/2001
     */
    private DataRequest inherited = null;

    /**
     * the request being satisfied.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/08/2000
     */
    private Rmap request = null;

    /**
     * <code>RmapChain</code> containing the request <code>Rmaps</code>
     * matched by the source <code>Rmap</code> chain.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/15/2000
     */
    private RmapChain requestChain = new RmapChain();

    /**
     * the controlling <code>RmapExtractor</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/11/2000
     */
    private RmapExtractor extractor = null;

    /**
     * the last frame <code>TimeRange</code> seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/31/2001
     */
    private TimeRange fRange = null;

    /**
     * the last <code>TimeRange</code> seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/31/2001
     */
    private TimeRange tRange = null;

private static long startTime = System.currentTimeMillis();

    /**
     * Class constructor to build an <code>ExtractedChain</code> from a
     * request being satisfied.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI  the <code>RmapExtractor</code> controlling the
     *		      extraction.
     * @param requestI    the request being satisfied.
     * @since V2.0
     * @version 01/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    ExtractedChain(RmapExtractor extractorI,Rmap requestI) {
	super();
	setExtractor(extractorI);
	setRequest(requestI);
    }

    /**
     * Is this <code>ExtractedChain</code> at its end?
     * <p>
     * The <code>ExtractedChain</code> is at its end if there is no request
     * to be satisfied, the data payload has been seen, and the last
     * <code>Rmap</code> has a <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return chain is at end?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 12/08/2000  INB	Created.
     *
     */
    final boolean atEnd()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Special time requests don't involve data. The same is also true is
	// the <code>RmapExtractor</code> explicitly says that data is not
	// required.
	Rmap workRequest = getExtractor().getWorkRequest();
	boolean specialTime = ((workRequest instanceof DataRequest) &&
			       ((((DataRequest) workRequest).getReference() !=
				 DataRequest.ABSOLUTE))),
		noRequest = (getRequest() == null),
		atEndR = (noRequest &&
			  ((getInherited() == null) ||
			   (getInherited().getName() == null) ||
			   (getInherited().compareNames("...") == 0)) &&
			  (!getExtractor().getRequireData() ||
			   (getHavePayload() &&
			    (getLast().getDblock() != null))));

	return (atEndR);
    }

    /**
     * Builds an <code>Rmap</code> hierarchy of extracted information from the
     * source chain based on the request chain.
     * <p>
     * This method is complicated by several factors:
     * <p><ol>
     * <li>Non-<code>ABSOLUTE</code> time references, in which case we're
     *	   building a new request from registration information to be matched
     *	   against the real data,</li>
     * <li>"..." syntax, which causes matches of a number of Rmaps,</li>
     * <li>Inherited time information.</li>
     * </ol><p>
     * For non-<code>ABSOLUTE</code> time references, we attempt to collapse
     * out unneeded nameless <code>Rmaps</code> from the resulting request. In
     * addition, we try to put time information as high up the resulting
     * request as possible.
     * <p>
     * For "..." syntax, the simplest case is when the "..." request is at the
     * bottom and has no time/frame information. In this case, we either clone
     * the children (<code>ABSOLUTE</code>) or keep the "..." request.
     * <p>
     * When there is time/frame information, we perform a standard match. For
     * the non-<code>ABSOLUTE</code> case, we also add a "..." request at the
     * bottom of the resulting hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataFlagI  extract the data payloads?
     * @return the <code>Rmap</code> hierarchy. This is <code>null</code> if
     *	       the chain is empty.
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
     *		  thrown if there are any problems extracting the information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    final Rmap buildRmapHierarchy(boolean dataFlagI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getRmaps().size() == 0) {
	    // With no <code>Rmaps</code> in the chain, we cannot perform the
	    // expansion, so return null.
	    return (null);
	}

	DataRequest reference = new DataRequest();
	if (getExtractor().getWorkRequest() instanceof DataRequest) {
	    reference.combineWith
		((DataRequest) getExtractor().getWorkRequest());
	}
	boolean collapse = (reference.getReference() != DataRequest.ABSOLUTE);
	Rmap rmapR = (collapse ?
		      collapsedRmapHierarchy(dataFlagI,reference) :
		      fullRmapHierarchy(dataFlagI));

	/*
	System.err.println("\n\nExtractor:\n" +
			   this +
			   "\nProduces:\n" +
			   rmapR + "\n\n");
	*/

	return (rmapR);
    }

    /**
     * Builds a collapsed (request) <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @author Ian Brown
     *
     * @param dataFlagI  extract the data payloads?
     * @param referenceI the reference <code>DataRequest</code>.
     * @return the <code>Rmap</code> hierarchy.
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
     *		  thrown if there are any problems extracting the information.
     * @since V2.0
     * @version 03/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2003  INB	Make sure that extracted requests have marker
     *			<code>DataBlocks</code>.
     * 05/22/2001  INB	Created.
     *
     */
    final Rmap collapsedRmapHierarchy(boolean dataFlagI,
				      DataRequest referenceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null,
	     last = null,
	     lastSource = null,
	     lastIdentifiable = null;
	TimeRange lTrange = getTrange(),
		  lFrange = getFrange(),
		  uTrange = null,
		  uFrange = null,
		  iTrange = ((getInherited() == null) ?
			     null :
			     getInherited().getTrange()),
		  iFrange = ((getInherited() == null) ?
			     null :
			     getInherited().getFrange());
	int startAt = getRmaps().size() - 1;
	boolean addUnnamed = false;

	for (int idx = startAt; idx >= 0; --idx) {
	    // For each of the source <code>Rmaps</code>, extract the
	    // information that matches the corresponding request
	    // <code>Rmap</code>. We start at the end of the chain and work
	    // up.
	    Rmap rmap = (Rmap) getRmaps().elementAt(idx),
		 request = (Rmap) getRchain().getRmaps().elementAt(idx);

	    if ((idx == 0) && rmap.isNamelessTimeless()) {
		// Ignore the unidentifiable <code>RoutingMap</code> when
		// building a collapsed request. We'll put it back on after
		// this.
		break;
	    }

	    if (request instanceof DataRequest) {
		// Update the reference request information based on that of
		// the request being worked on.
		referenceI.combineWith((DataRequest) request);
	    }

	    if ((idx == startAt) && (rmap instanceof Registration)) {
		if (rmap.getNchildren() > 0) {
		    Rmap parent = rmap,
			 child = rmap.getChildAt(0);

		    rmap = new Rmap();
		    rmap.setName(parent.getName());
		    rmap.setTrange(child.getTrange());
		    rmap.setFrange(child.getFrange());
		    rmap.setDblock(child.getDblock());

		    parent = request;
		    child = getInherited();
		    request = new DataRequest();
		    request.setName(parent.getName());
		    if (child != null) {
			request.setTrange(child.getTrange());
			request.setFrange(child.getFrange());
		    } else {
			request.setTrange(parent.getTrange());
			request.setFrange(parent.getFrange());
		    }

		} else {
		    addUnnamed = true;
		}
	    }

	    // Perform the extraction.
	    Rmap rmapN = rmap.extractRequest
		(request,
		 referenceI,
		 lTrange,
		 lFrange);
	    if (rmapN == null) {
		// If we get nothing from an extraction, then there is no
		// actual matching information.
		rmapR = null;
		break;
	    }

	    // Determine if we should keep the time and frame information at
	    // the next level. We keep it if:
	    // <br><ol>
	    // <li>the request was not asking for the field,</li>
	    // <li>the result does not contain the field, or</li>
	    // <li>the source does contain the field.</li>
	    // </ol>
	    Rmap rmap2 = rmapN;
	    lTrange = null;
	    lFrange = null;
	    if (rmapN.getNchildren() == 1) {
		rmap2 = rmapN.getChildAt(0);
	    }
	    
	    boolean clearTrange = (request.getTrange() == null),
		   clearFrange = (request.getFrange() == null);
	    rmap2.clearTranges(clearTrange,clearFrange);

	    if (last != null) {
		if (lastSource instanceof SinkHandler) {
		    uTrange =
			uFrange = null;
		} else {
		    if ((uTrange != null) &&
			(rmap2.getTrange() != null)) {
			last.setTrange(uTrange);
			uTrange = null;
		    }
		    if ((uFrange != null) &&
			(rmap2.getFrange() != null)) {
			uFrange = null;
		    }
		}
	    }

	    if (!(rmap instanceof Source)) {
		if (uTrange == null) {
		    uTrange = rmap2.getTrange();
		    rmap2.setTrange(null);
		}
		if (uFrange == null) {
		    uFrange = rmap2.getFrange();
		    rmap2.setFrange(null);
		}
	    }

	    if ((lastIdentifiable != null) && (lastIdentifiable != last)) {
		// If there are a set of non-identifiable <code>Rmaps</code>
		// between the last one we could identify and now, then
		// collapse them out.
		if (rmap2 == rmapN) {
		    if (rmapN.isIdentifiable()) {
			lastIdentifiable.getParent().removeChildAt(0);
			rmapR = lastIdentifiable;
		    }
		} else {
		    if (rmap2.isIdentifiable()) {
			if (lastIdentifiable.getParent() != null) {
			    lastIdentifiable.getParent().removeChild
				(lastIdentifiable);
			}
			rmapR = lastIdentifiable;
		    } else if (rmapN.isIdentifiable()) {
			rmapN.removeChildAt(0);
			rmap2 = rmapN;
			if (lastIdentifiable.getParent() != null) {
			    lastIdentifiable.getParent().removeChild
				(lastIdentifiable);
			}
			rmapR = lastIdentifiable;
		    }
		}
	    }
	    if (rmapN.isIdentifiable()) {
		lastIdentifiable = rmapN;
	    } else if (rmap2.isIdentifiable() || !request.isIdentifiable()) {
		lastIdentifiable = rmap2;
	    }
	    last = rmapN;
	    lastSource = rmap;

	    if (((rmap2 != rmapN) && !rmap2.isIdentifiable())) {
		rmapN.removeChildAt(0);
		rmap2 = rmapN;
	    }

	    if ((rmap2 != rmapN) || rmapN.isIdentifiable()) {
		if (rmapR != null) {
		    // If we've already built part of the result, make the
		    // previously created <code>Rmap</code> hierarchy a child
		    // of the newly created <code>Rmap</code>.
		    rmap2.addChild(rmapR);
		}

		// Make the new <code>Rmap</code> hierarchy the return result.
		rmapR = rmapN;
	    }
	}

	if (rmapR != null) {
	    if (rmapR.getName() == null) {
		for (int idx = 0; idx < rmapR.getNchildren(); ++idx) {
		    Rmap child = rmapR.getChildAt(idx);
		    if (uTrange != null) {
			child.setTrange(uTrange);
		    }
		    if (uFrange != null) {
			child.setFrange(uFrange);
		    }
		}
	    } else {
		if (uTrange != null) {
		    rmapR.setTrange(uTrange);
		}
		if (uFrange != null) {
		    rmapR.setFrange(uFrange);
		}
		Rmap tRmap = rmapR;
		rmapR = new DataRequest();
		rmapR.addChild(tRmap);
	    }

	    if (addUnnamed) {
		Rmap addAt;
		for (addAt = rmapR;
		     (addAt.getNchildren() > 0);
		     addAt =
			 ((addAt.getNchildren() > 0) ?
			  addAt.getChildAt(0) :
			  null)) {}
		Rmap blank = new DataRequest();
		blank.setDblock(Rmap.MarkerBlock);
		addAt.addChild(blank);
	    }
	}

	return (rmapR);
    }

    /**
     * Creates a combined request from the current and inherited requests.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the combined request.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if the input <code>Rmap</code> is missing information
     *		  that cannot be retrieved from the <code>Rmaps</code> in this
     *		  <code>RmapInheritor's</code> list.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem inheriting information.
     * @since V2.0
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
     *			<code>RmapVector()</code>.
     * 03/12/2003  INB	Add the children of the current request into the
     *			result.
     * 04/03/2001  INB	Created.
     *
     */
    final Rmap combinedRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest reference = new DataRequest(),
	    combinedRequestR;
	String name = null;
	boolean childRequest = true;

	if (((getInherited() == null) ||
	     ((name = getInherited().getName()) == null)) &&
	    (getRequest() != null)) {
	    name = getRequest().getName();
	    childRequest = false;
	}

	if (getRequest() instanceof DataRequest) {
	    reference.combineWith((DataRequest) getRequest());
	}

	if ((getRequest() == null) || delayRequest) {
	    combinedRequestR = getInherited();
	    childRequest = true;

	} else if (getInherited() == null) {
	    combinedRequestR = new DataRequest
		(name,
		 getRequest().getTrange(),
		 getRequest().getFrange());
	    childRequest = false;

	} else if (getRequest().getTrange() != null) {
	    combinedRequestR = new DataRequest
		(name,
		 getRequest().getTrange().add(getInherited().getTrange()),
		 null,
		 reference.getReference(),
		 reference.getDomain(),
		 1,
		 0,
		 reference.getSynchronized(),
		 reference.getMode());
	    childRequest = false;

	} else if (getRequest().getFrange() != null) {
	    combinedRequestR = new DataRequest
		(name,
		 null,
		 getRequest().getFrange().add(getInherited().getFrange()),
		 reference.getReference(),
		 reference.getDomain(),
		 1,
		 0,
		 reference.getSynchronized(),
		 reference.getMode());
	    childRequest = false;

	} else {
	    combinedRequestR = new DataRequest
		(name,
		 getInherited().getTrange(),
		 getInherited().getFrange(),
		 reference.getReference(),
		 reference.getDomain(),
		 1,
		 0,
		 reference.getSynchronized(),
		 reference.getMode());
	}

	if (getRequest() != null) {
	    combinedRequestR.setParent(getRequest().getParent());

	    if (childRequest) {
		combinedRequestR.setChildren(new RmapVector(1));
		combinedRequestR.getChildren().addElement(getRequest());
	    } else {
		combinedRequestR.setChildren(getRequest().getChildren());
	    }
	}


	return (combinedRequestR);
    }

    /**
     * Clones this <code>ExtractedChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cloned <code>ExtractedChain</code>.
     * @since V2.0
     * @version 05/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    protected final Object clone() {
	ExtractedChain clonedR = null;

	clonedR = (ExtractedChain) super.clone();

	if (getRchain() != null) {
	    clonedR.setRchain((RmapChain) getRchain().clone());
	}
	if (getInherited() != null) {
	    clonedR.setInherited((DataRequest) getInherited().clone());
	}
	if (getTrange() != null) {
	    clonedR.setTrange((TimeRange) getTrange());
	}
	if (getFrange() != null) {
	    clonedR.setFrange((TimeRange) getFrange());
	}

	return (clonedR);
    }

    /**
     * Builds a full (data) <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @author Ian Brown
     *
     * @param dataFlagI  extract the data payloads?
     * @param referenceI the reference <code>DataRequest</code>.
     * @return the <code>Rmap</code> hierarchy.
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
     *		  thrown if there are any problems extracting the information.
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/22/2001  INB	Created.
     *
     */
    final Rmap fullRmapHierarchy(boolean dataFlagI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;
	int lRmapIdx = getRmaps().size() - 1,
	    dPldIdx = -1;
	DataRequest dummy = new DataRequest();

	// Locate the data payload.
	for (int idx = lRmapIdx; idx >= 0; --idx) {
	    Rmap dRmap = (Rmap) getRmaps().elementAt(idx);

	    if ((dRmap.getDblock() != null) &&
		(dRmap.getDblock().getData() != null)) {
		dPldIdx = idx;
		break;
	    }
	}
	if (dPldIdx == -1) {
	    throw new java.lang.IllegalStateException
		("Cannot extract data without a data payload.");
	}

	// Locate the time information for the request.
	TimeRange fRequest = null,
		  fData = null,
	    	  tRequest = null,
		  tData = null;
	int tIdx = -1;
	for (int idx = 0; idx <= dPldIdx; ++idx) {
	    Rmap rRmap = (Rmap) getRchain().getRmaps().elementAt(idx),
		 dRmap = (Rmap) getRmaps().elementAt(idx);

	    if (rRmap.getTrange() != null) {
		if (fRequest != null) {
		    throw new java.lang.IllegalStateException
			("Multiple levels of time ranges are not supported " +
			 "at this time.\n" + this);
		}
		if (dRmap.getTrange() != null) {
		    tRequest = rRmap.getTrange();
		    tData = dRmap.getTrange();
		    tIdx = idx;
		}

	    } else if (rRmap.getFrange() != null) {
		if ((fRequest != null) || (tRequest != null)) {
		    throw new java.lang.IllegalStateException
			("Multiple levels of time ranges are not supported " +
			 "at this time.\n" + this);
		}
		if (dRmap.getTrange() != null) {
		    fRequest = rRmap.getFrange();
		    fData = dRmap.getFrange();
		    tIdx = idx;
 		}
	    }
	}
	boolean keepFrames = (fRequest == null),
		keepTimes = (tRequest == null);

	// OK, we've got the time information, now we need to actually perform
	// the extraction. We work our way up from the bottom.
	TimeRange fRange = null;
	for (int idx = lRmapIdx; idx >= 0; --idx) {
	    Rmap rRmap = (Rmap) getRchain().getRmaps().elementAt(idx),
		 dRmap = (Rmap) getRmaps().elementAt(idx);

	    Rmap rmapN = null;
	    if ((idx != tIdx) &&
		(fRequest == null) &&
		(tRequest == null) &&
		(rRmap.getTrange() != null) &&
		(dRmap.getTrange() != null)) {
		tRequest = rRmap.getTrange();
		tData = dRmap.getTrange();
	    }

	    if (idx > dPldIdx) {
		// If we are past the payload index, then we just move up.
		idx = dPldIdx + 1;
		continue;

	    } else if ((idx == dPldIdx) && (dPldIdx < lRmapIdx)) {
		// If the payload is above the data reference, then we need to
		// perform a multi-level extraction.
		rmapN = dRmap.extractMultiData
		    (rRmap,
		     fRequest,
		     fData,
		     keepFrames,
		     tRequest,
		     tData,
		     keepTimes,
		     idx + 1,
		     getRchain().getRmaps(),
		     getRmaps());

	    } else {
		// Perform a regular extraction.
		rmapN = dRmap.extractData
		    (rRmap,
		     fRequest,
		     fData,
		     keepFrames,
		     tRequest,
		     tData,
		     keepTimes,
		     (idx >= dPldIdx));
	    }

	    if (rmapN == null) {
		rmapR = null;
		break;
	    }
	    if ((fRequest != null) && (idx != tIdx)) {
		fRange = rmapN.getFrange();
		rmapN.setFrange(null);
	    } else if ((idx == tIdx) &&
		       (fRange != null)) {
		rmapN.setFrange(fRange);
		fRange = null;
	    }
	    fRequest =
		fData =
		tRequest =
		tData = null;

	    if (rmapR == null) {
		rmapR = rmapN;
	    } else {
		rmapN.addChild(rmapR);
		rmapR = rmapN;
	    }
	}

	return (rmapR);
    }

    /**
     * Gets the controlling <code>RmapExtractor</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RmapExtractor</code>.
     * @see #setExtractor(com.rbnb.api.RmapExtractor)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final RmapExtractor getExtractor() {
	return (extractor);
    }

    /**
     * Gets the last frame <code>TimeRange</code> seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last frame <code>TimeRange</code> seen.
     * @see #setFrange(com.rbnb.api.TimeRange)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    private final TimeRange getFrange() {
	return (fRange);
    }

    /**
     * Gets the inherited request.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the inherited request <code>DataRequest</code>.
     * @see #setInherited(com.rbnb.api.DataRequest)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final DataRequest getInherited() {
	return (inherited);
    }

    /**
     * Gets the need frame match flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return does the inherited request need to be matched?
     * @see #setNeedTimeMatch(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    private final boolean getNeedFrameMatch() {
	return (needFrameMatch);
    }

    /**
     * Gets the need time match flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return does the inherited request need to be matched?
     * @see #setNeedTimeMatch(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final boolean getNeedTimeMatch() {
	return (needTimeMatch);
    }

    /**
     * Gets the request <code>RmapChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the request <code>RmapChain</code>.
     * @see #setRchain(com.rbnb.api.RmapChain)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    final RmapChain getRchain() {
	return (requestChain);
    }

    /**
     * Gets the request.
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
     * 12/08/2000  INB	Created.
     *
     */
    final Rmap getRequest() {
	return (request);
    }

    /**
     * Gets the last <code>TimeRange</code> seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the last <code>TimeRange</code> seen.
     * @see #setTrange(com.rbnb.api.TimeRange)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    private final TimeRange getTrange() {
	return (tRange);
    }

    /**
     * Handles matched <code>Rmaps</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	     the matched request <code>Rmap</code>.
     * @param matchesI	     the matched list of <code>Rmaps</code>.
     * @param unsatisfiedIO  if non-null on input, this vector will hold
     *			     any unsatisfied <code>ExtractedChains</code>
     *			     created by this method.
     * @return the reason for the failed match.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *	      thrown if there is a problem matching the request to
     *	      the source <code>Rmap</code> hierarchy or in extracting
     *	      the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final byte matchedList(Rmap requestI,
				   java.util.Vector matchesI,
				   java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	for (int idx = 0; idx < matchesI.size(); ++idx) {
	    Rmap matched = (Rmap) matchesI.elementAt(idx);

	    byte reason = matchRmap(requestI,
				    matched,
				    (matchesI.size() == 1),
				    unsatisfiedO);

	    reasonR = Rmap.combineReasons(reasonR,reason);
	}

	return (reasonR);
    }

    /**
     * Matches the <code>Rmaps</code> in the input <code>RmapVector</code>.
     * <p>
     * If there is no inherited request <code>Rmap</code>, then this method
     * just tries to match the current request against the input list.
     * <p>
     * If there is just an inherited request, then this method just tries
     * to match it against the input list.
     * <p>
     * If the current request has just time, then the method combines the
     * time information from the request and the inherited request to
     * produce the request that is matched against the input list.
     * <p>
     * If the current request has just a name, then the method tries to
     * match the list against the inherited request. If that fails, but the
     * method is allowed to try alternatives, the method creates a combined
     * request from the name of the input request and the time of the
     * inherited request and tries to match that against the input list.
     * <p>
     * If the current request has both a name and a time, then the method
     * tries to match the list against the inherted request. If that fails,
     * but the method is allowed to try alternatives, the method creates a
     * combined request from the name of the input request and the times of
     * the current request and the inherited request and tries to match
     * that against the input list.
     * <p>
     * If the request to be matched is both nameless and timeless, then this
     * method just moves down a level of the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptAllI    if a request has a name and produces a null
     *			    match vector, treat it as if everything had
     *			    matched a nameless/timeless request.
     * @param listI	    the list of <code>Rmaps</code> to match.
     * @param unsatisfiedO  if non-null on input, this vector will hold any
     *			    unsatisfied <code>ExtractedChains</code>
     *			    created by this method.
     * @param reasonR	    the potential reason for a failed match.
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
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    final byte matchList(boolean acceptAllI,
			 RmapVector listI,
			 java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//long startTime=System.currentTimeMillis();
//long mli=0,mlcai=0;
	byte reasonR = Rmap.MATCH_UNKNOWN;
	if ((getRequest() != null) && getRequest().isNamelessTimeless()) {
	    // If the request is nameless and timeless, just move down a
	    // level in the request without doing anything to the source list.
	    reasonR = moveDown(getRequest(),getInherited(),listI,unsatisfiedO);

	} else if (getInherited() == null) {
	    // If there is no inherited request, then we only need to match the
	    // current request.
	    reasonR = matchListCurrent(acceptAllI,listI,unsatisfiedO);

	} else {
	    // If there is an inherited request, then we need to figure out
	    // exactly what to match.

	    if (getRequest() == null) {
		// If there is no active request, then we just need to work
		// on the inherited request.
		reasonR = matchListInherited(acceptAllI,listI,unsatisfiedO);
//mli=System.currentTimeMillis()-startTime;
//if (mli>0) System.err.println("mli "+mli);

	    } else {
		// If there is both an inherited request and a current request,
		// then combine the two to produce the real request.
		reasonR = matchListCurrentAndInherited(acceptAllI,
						       listI,
						       unsatisfiedO);
//mlcai=System.currentTimeMillis()-startTime;
//if (mlcai>0) System.err.println("mlcai "+mlcai);
	    }
	}

	return (reasonR);
    }

    /**
     * Matches the input <code>RmapVector</code> of <code>Rmaps</code>
     * against the input request <code>Rmap</code>.
     * <p>
     * First, the method matches the list against the input request
     * <code>Rmap</code>. If that succeeds, then it examines what was
     * matched to determine what further work needs to be done.
     * <p>
     * If the method fails to find a match to the input request, it second
     * tries to see if an alternative request should be tried. An
     * alternative can only be checked if a time match is not required.
     * <p>
     * If the input request has no name, then the caller is told to try an
     * alternative.
     * <p>
     * If the input request has a name and time information, then this
     * method tries to match just the name against the list. If that
     * succeeds, then it examines what was matched to determine what
     * further work needs to be done.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptAllI       if a request has a name and produces a null
     *			       match vector, treat it as if everything had
     *			       matched a nameless/timeless request.
     * @param requestI	       the request <code>Rmap</code>.
     * @param listI	       the list of <code>Rmaps</code> to match.
     * @param unsatisfiedO     if non-null on input, this vector will hold any
     *			       unsatisfied <code>ExtractedChains</code>
     *			       created by this method.
     * @return the reason for the failed match. A value that is less than
     *	       <code>Rmap.MATCH_UNKNOWN</code> means that the caller can try an
     *	       alternative match.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final byte matchListAgainst
	(boolean acceptAllI,
	 Rmap requestI,
	 RmapVector listI,
	 java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//System.err.println("ExtractedChain.matchListAgainst: start at "+(System.currentTimeMillis()-startTime));
//EMF 7/6/06
//System.err.println("\n\nExtractedChain.matchListAgainst:");
//System.err.println("requestI "+requestI);
//System.err.println("listI "+listI);
	byte reasonR = Rmap.MATCH_UNKNOWN;
	Rmap matchingRequest = requestI;
	DataRequest reference = new DataRequest();
	boolean special =
	    ((getExtractor().getWorkRequest() instanceof DataRequest) ?
	     (((DataRequest) getExtractor().getWorkRequest()).getReference() !=
	      DataRequest.ABSOLUTE) :
	     false);

	if (special &&
	    (requestI != null) &&
	    ((requestI.getTrange() != null) ||
	     (requestI.getFrange() != null))) {
	    // When dealing with special requests, we do not want time or frame
	    // information in the request.
	    matchingRequest = new DataRequest(requestI.getName());
	}

	// Find the matching <code>Rmaps</code> in the input list.
	java.util.Vector matches = new java.util.Vector();
	reasonR = listI.findMatches(matchingRequest,matches);   // does binary search to match request

	boolean stopHere = false;
	if (((matchingRequest == null) ||
	     (matchingRequest.getName() == null))) {
	    // Special case: we're at an unnamed leaf in the request, we don't
	    // want to match anything that has a name.
	    for (int idx = matches.size() - 1;
		 idx >= 0;
		 --idx) {
		Rmap entry = (Rmap) matches.elementAt(idx);

		if (entry.getName() == null) {
		    break;
		}
		matches.removeElementAt(idx);
	    }
	    stopHere =
		((getRequest() == null) ||
		 ((getRequest().getName() == null) &&
		  (getRequest().getNchildren() == 0)));
	}

	if (matches.size() > 0) {
	    // If there are now matches, then we can build new
	    // <code>ExtractedChains</code> to work on them.
	    reasonR = matchedList(requestI,matches,unsatisfiedO);

	} else if (stopHere && atEnd()) {
	    // Special case: we ran off the end of the request when there was
	    // some possibility that there might have been more to look for.
	    getExtractor().extractInformation(requestI,this);
	    reasonR = Rmap.MATCH_EQUAL;

	} else if (((Rmap) listI.lastElement()).getName() != null) {
	    // If we fail to match out of a list of names, we never want to
	    // report serious exceptions.
	    reasonR = Rmap.MATCH_NOINTERSECTION;
	}

//System.err.println("ExtractedChain.matchListAgainst: listI.size = "+listI.size());
//System.err.println("ExtractedChain.matchListAgainst: end at "+(System.currentTimeMillis()-startTime));
	return (reasonR);
    }

    /**
     * Matches the <code>Rmaps</code> in the input <code>RmapVector</code>
     * against just the current request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptAllI    if a request has a name and produces a null
     *			    match vector, treat it as if everything had
     *			    matched a nameless/timeless request.
     * @param listI	    the list of <code>Rmaps</code> to match.
     * @param unsatisfiedO  if non-null on input, this vector will hold any
     *			    unsatisfied <code>ExtractedChains</code>
     *			    created by this method.
     * @return the reason for the failed match.
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
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    final byte matchListCurrent(boolean acceptAllI,
				RmapVector listI,
				java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	reasonR = matchListAgainst(acceptAllI,
				   getRequest(),
				   listI,
				   unsatisfiedO);
	if (reasonR < Rmap.MATCH_UNKNOWN) {
	    reasonR += (1 + Rmap.MATCH_AFTER - Rmap.MATCH_UNKNOWN);
	}

	return (reasonR);
    }

    /**
     * Matches the <code>Rmaps</code> in the input <code>RmapVector</code>
     * against the current request and inherited request combined.
     * <p>
     * If the current request has just a name, then the method tries to
     * match the list against the inherited request. If that fails, but the
     * method is allowed to try alternatives, the method creates a combined
     * request from the name of the input request and the time of the
     * inherited request and tries to match that against the input list.
     * <p>
     * If the current request has both a name and a time, then the method
     * tries to match the list against the inherted request. If that fails,
     * but the method is allowed to try alternatives, the method creates a
     * combined request from the name of the input request and the times of
     * the current request and the inherited request and tries to match
     * that against the input list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptAllI    if a request has a name and produces a null
     *			    match vector, treat it as if everything had
     *			    matched a nameless/timeless request.
     * @param listI	    the list of <code>Rmaps</code> to match.
     * @param unsatisfiedO  if non-null on input, this vector will hold any
     *			    unsatisfied <code>ExtractedChains</code>
     *			    created by this method.
     * @return the reason for the failed match.
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
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    final byte matchListCurrentAndInherited(boolean acceptAllI,
					    RmapVector listI,
					    java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR;
	Rmap combined = combinedRequest();
	if (combined == null) {
	    reasonR = Rmap.MATCH_ILLEGAL;

	} else if ((reasonR = matchListAgainst(acceptAllI,
					       combined,
					       listI,
					       unsatisfiedO)) <
		   Rmap.MATCH_UNKNOWN) {
	    reasonR += (1 + Rmap.MATCH_AFTER - Rmap.MATCH_UNKNOWN);
	}

	return (reasonR);
    }

    /**
     * Matches the <code>Rmaps</code> in the input <code>RmapVector</code>
     * against just the inherited request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acceptAllI    if a request has a name and produces a null
     *			    match vector, treat it as if everything had
     *			    matched a nameless/timeless request.
     * @param listI	    the list of <code>Rmaps</code> to match.
     * @param unsatisfiedO  if non-null on input, this vector will hold any
     *			    unsatisfied <code>ExtractedChains</code>
     *			    created by this method.
     * @return the reason for the failed match.
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
     *		  thrown if there is a problem matching the request to
     *		  the source <code>Rmap</code> hierarchy or in extracting
     *		  the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    final byte matchListInherited(boolean acceptAllI,
				  RmapVector listI,
				  java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = matchListAgainst(false,
					getInherited(),
					listI,
					unsatisfiedO);

	if (reasonR < Rmap.MATCH_UNKNOWN) {
	    reasonR += (1 + Rmap.MATCH_AFTER - Rmap.MATCH_UNKNOWN);
	}

	return (reasonR);
    }

    /**
     * Handles a single matched <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	 the matched request <code>Rmap</code>.
     * @param matchedI	 the matched <code>Rmap</code>.
     * @param onlyOneI	 is this the last of the matches?
     * @param unsatisfiedIO  if non-null on input, this vector will hold
     *			 any unsatisfied <code>ExtractedChains</code>
     *			 created by this method.
     * @return the reason for a failed match at a lower level.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *	      thrown if there is a problem matching the request to
     *	      the source <code>Rmap</code> hierarchy or in extracting
     *	      the desired information.
     * @since V2.0
     * @version 07/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/29/2004  INB	Took out check for SourceHandler in delay request
     *			check.  This is marked with a comment if it turns out
     *			there is a need for it.
     * 03/18/2003  INB	Added marker block to the bottom of all requests.
     * 12/11/2000  INB	Created.
     *
     */
    private final byte matchRmap(Rmap requestI,
				 Rmap matchedI,
				 boolean lastOneI,
				 java.util.Vector unsatisfiedIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	try {
	    Rmap workRequest = getExtractor().getWorkRequest();
	    boolean specialTime =
		((workRequest instanceof DataRequest) &&
		 ((((DataRequest) workRequest).getReference() !=
		   DataRequest.ABSOLUTE)));
	    
	    // Make an <code>ExtractedChain</code> to include the matched
	    // <code>Rmap</code>.
	    ExtractedChain eChain = this;
	    if (!lastOneI) {
		// If there are further matched <code>Rmaps</code>, we'll need
		// a separate <code>ExtractedChain</code> for each one.
		eChain = (ExtractedChain) clone();
	    }

	    // Add the <code>Rmap</code> to the chain.
	    eChain.addRmap(matchedI);
	    if (!(matchedI instanceof Registration)) {
		if (matchedI.getTrange() != null) {
		    if ((requestI == null) ||
			(requestI.getTrange() == null)) {
			setTrange(matchedI.getTrange());
		    } else {
			setTrange
			    (matchedI.getTrange().subtract
			     (requestI.getTrange()));
		    }
		}
	    }

	    Rmap request1 =
		(requestI == null) ?
		new Rmap() :
		(Rmap) requestI.clone();
	    if (matchedI.getName() == null) {
		request1.setName(null);
	    }
	    if (matchedI.getTrange() == null) {
		request1.setTrange(null);
	    } else if (specialTime &&
		       (getInherited() != null) &&
		       (getInherited().getTrange() != null)) {
		request1.setTrange(getInherited().getTrange());
	    }

	    if (matchedI.getFrange() == null) {
		request1.setFrange(null);
	    } else if (specialTime &&
		       (getInherited() != null) &&
		       (getInherited().getFrange() != null)) {
		request1.setFrange(getInherited().getFrange());
	    }
	    eChain.getRchain().addRmap(request1);

	    // If the request had a name or time, continue processing
	    // it. Start by updating the inherited request.
	    Rmap wasInherited = getInherited();
	    boolean movingDown =
		((getRequest() == null) ?
		 false :
		 ((wasInherited != null) &&
		  (wasInherited.getName() != null) &&
		  (matchedI.getName() == null)) ?
		 false :
		 (!delayRequest &&
		  (getRequest().getName() != null) &&
		  (matchedI.getName() == null)) ?
		 false :
		 true);

	    eChain.delayRequest = false;
	    if (movingDown && (getRequest().getNchildren() > 1)) {
		// If the next level of the request hierarchy consists of
		// multiple, named <code>Rmaps</code>, then we will delay the
		// move down until there are actually names in the source
		// hierarchy.

		// INB 07/29/2004 - commented out the SourceHandler instanceof
		// line.  SourceHandlers have names, so I don't think it makes
		// any sense to delay moving down the request heirarchy.  In
		// fact, this causes problems when matching multiple children.
		if ((getRequest().getChildAt(0).getName() != null) &&
		    (matchedI.getNchildren() > 0) &&
		    (/*(matchedI instanceof SourceHandler) ||*/
		     (matchedI instanceof StorageManager) ||
		     (matchedI instanceof FileSet))) {
		    // For now, we only delay until we are actually inside a
		    // <code>FrameSet</code>.
		    movingDown = false;
		    eChain.delayRequest = true;
		}
	    }
	    eChain.updateInherited(requestI,matchedI,movingDown);

	    // Create a set of <code>ExtractedChains</code>, one for each child
	    // of the  current request.
	    java.util.Vector workList = new java.util.Vector();

	    if (!movingDown) {
		// If we are not going to be moving down the request hierarchy,
		// then keep the current <code>ExtractedChain</code> as our
		// working element.
		workList.addElement(eChain);

	    } else if (getRequest().getNmembers() > 0) {
		// Group members are not supported at this time. To be able to
		// support them, we'll need a second <code>RmapChain</code>
		// consisting of the matched request <code>Rmaps</code> to go
		// with the matched source <code>Rmaps</code>.
		throw new java.lang.IllegalStateException
		    ("Group members are not supported in requests: " +
		     getRequest());

	    } else if (getRequest().getNchildren() == 0) {
		// If there are no children of the current request, then just
		// set the next request to be null (or for "..." as
		// appropriate) and keep the current
		// <code>ExtractedChain</code>.
		if ((requestI != null) &&
		    (requestI.compareNames("...") == 0) &&
		    (matchedI.getName() == null)) {
		    eChain.setRequest(new DataRequest("..."));
		    eChain.getRequest().setDblock(Rmap.MarkerBlock);
		} else {
		    eChain.setRequest(null);
		}
		workList.addElement(eChain);

	    } else {
		// Create a separate chain for each of the children. We keep
		// the current <code>ExtractedChain</code> as the last entry.
		int eIdx = getRequest().getNchildren() - 1;
		for (int idx = 0;
		     idx < eIdx;
		     ++idx) {
		    ExtractedChain eChain2 = (ExtractedChain) eChain.clone();

		    eChain2.setRequest(getRequest().getChildAt(idx));
		    Rmap toBottom = eChain2.getRequest().moveToBottom();
		    if (toBottom.getDblock() == null) {
			toBottom.setDblock(Rmap.MarkerBlock);
		    }
		    workList.addElement(eChain2);
		}
		eChain.setRequest(getRequest().getChildAt(eIdx));
		workList.addElement(eChain);
	    }

	    // Now move down a level for each of the entries in the list.
	    for (int idx = 0; idx < workList.size(); ++idx) {
		eChain = (ExtractedChain) workList.elementAt(idx);
		byte reason = eChain.moveDownFrom(requestI,
						  matchedI,
						  unsatisfiedIO);

		reasonR = Rmap.combineReasons(reasonR,reason);
	    }

	} finally {
	}

	return (reasonR);
    }

    /**
     * Moves down past a nameless and timeless request.
     * <p>
     * This basically moves down one level in the request hierarchy without
     * looking at the source hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	    the request <code>Rmap</code>.
     * @param inheritedI    the inherited request. If non-null, we check
     *			    against this.
     * @param listI	    the list of elements to move down past.
     * @param unsatisfiedO  if non-null on input, this vector will hold any
     *			    unsatisfied <code>ExtractedChains</code>
     *			    created by this method.
     * @return the reason for a failed match.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
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
     * 01/05/2001  INB	Created.
     *
     */
    private final byte moveDown(Rmap requestI,
				Rmap inheritedI,
				RmapVector listI,
				java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	if (requestI.getNchildren() == 0) {
	    // if there are no children, then we place a null request in the
	    // chain and retry.
	    setRequest(null);
	    reasonR = matchList(!getExtractor().getExtractRmaps(),
				listI,
				unsatisfiedO);

	} else {
	    // If there are children in the request hierarchy, create a list of
	    // <code>ExtractedChains</code> for them and move down.
	    java.util.Vector workList = new java.util.Vector();
	    int eIdx = getRequest().getNchildren() - 1;
	    for (int idx = 0; idx < eIdx; ++idx) {
		ExtractedChain eChain = (ExtractedChain) clone();
		eChain.setRequest(getRequest().getChildAt(idx));
		workList.addElement(eChain);
	    }
	    setRequest(getRequest().getChildAt(eIdx));
	    workList.addElement(this);

	    for (int idx = 0; idx < workList.size(); ++idx) {
		ExtractedChain eChain =
		    (ExtractedChain) workList.elementAt(idx);
		byte reason = eChain.matchList
		    (!getExtractor().getExtractRmaps(),
		     listI,
		     unsatisfiedO);
		reasonR = Rmap.combineReasons(reasonR,reason);
	    }
	}

	return (reasonR);
    }
	
    /**
     * Moves down a source <code>Rmap</code> hierarchy.
     * <p>
     * If this <code>ExtractedChain</code> is at the end of the request,
     * then the method extracts the information from chain and adds it to
     * the matched list.
     * <p>
     * Otherwise, the method moves down to the next level of the source
     * hierarchy for the controlling <code>RmapExtractor</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	 the matched request <code>Rmap</code>.
     * @param matchedI	 the matched <code>Rmap</code>.
     * @param unsatisfiedIO  if non-null on input, this vector will hold
     *			 any unsatisfied <code>ExtractedChains</code>
     *			 created by this method.
     * @return the reason for a failed match at a lower level.
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
     *	      thrown if there is a problem matching the request to
     *	      the source <code>Rmap</code> hierarchy or in extracting
     *	      the desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final byte moveDownFrom(Rmap requestI,
				    Rmap matchedI,
				    java.util.Vector unsatisfiedIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	if (atEnd()) {
	    // If this <code>ExtractedChain</code> has reached the end of
	    // the request hierarchy, then perform the actual extraction of
	    // information.
	    getExtractor().extractInformation(requestI,this);

	    if ((((requestI != null) &&
		  (requestI.compareNames("...") == 0)) ||
		 ((getInherited() != null) &&
		  (getInherited().compareNames("...") == 0))) &&
		((matchedI != null) &&
		 (matchedI.getNchildren() > 0))) {
		java.util.Vector unsatisfied = new java.util.Vector();
		reasonR = matchedI.moveDownFrom
		    (getExtractor(),
		     this,
		     unsatisfied);

		// If there is an output list, add to it.
		if ((unsatisfiedIO != null) && (unsatisfied.size() > 0)) {
		    for (int idx = 0; idx < unsatisfied.size(); ++idx) {
			unsatisfiedIO.addElement
			    (unsatisfied.elementAt(idx));
		    }
		}
	    } else {
		reasonR = Rmap.MATCH_EQUAL;
	    }

	} else if ((matchedI.getNchildren() == 0) &&
		   (getRequest() != null) &&
		   (getRequest().getName() == null) &&
		   (getRequest().getNchildren() == 0)) {
	    setRequest(null);
	    if (atEnd() &&
		((requestI == null) ||
		 (requestI.getName() != null)) &&
		((getInherited() == null) ||
		 (getInherited().getName() == null))) {
		getExtractor().extractInformation(requestI,this);

		if ((((requestI != null) &&
		      (requestI.compareNames("...") == 0)) ||
		     ((getInherited() != null) &&
		      (getInherited().compareNames("...") == 0))) &&
		    ((matchedI != null) &&
		     (matchedI.getNchildren() > 0))) {
		    java.util.Vector unsatisfied = new java.util.Vector();
		    reasonR = matchedI.moveDownFrom
			(getExtractor(),
			 this,
			 unsatisfied);

		    // If there is an output list, add to it.
		    if ((unsatisfiedIO != null) && (unsatisfied.size() > 0)) {
			for (int idx = 0; idx < unsatisfied.size(); ++idx) {
			    unsatisfiedIO.addElement
				(unsatisfied.elementAt(idx));
			}
		    }
		} else {
		    reasonR = Rmap.MATCH_EQUAL;
		}
	    }

	} else {
	    // If there is still work to do, then we continue to move down the
	    // hierarchy.
	    java.util.Vector unsatisfied = new java.util.Vector();
	    reasonR = matchedI.moveDownFrom(getExtractor(),this,unsatisfied);

	    // If there is an output list, add to it.
	    if ((unsatisfiedIO != null) && (unsatisfied.size() > 0)) {
		for (int idx = 0; idx < unsatisfied.size(); ++idx) {
		    unsatisfiedIO.addElement(unsatisfied.elementAt(idx));
		}
	    }
	}

	return (reasonR);
    }

    /**
     * Sets the controlling <code>RmapExtractor</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI  the <code>RmapExtractor</code>.
     * @see #getExtractor()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final void setExtractor(RmapExtractor extractorI) {
	extractor = extractorI;
    }

    /**
     * Sets the last frame <code>TimeRange</code> seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fRangeI  the frame <code>TimeRange</code>.
     * @see #getFrange()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    private final void setFrange(TimeRange fRangeI) {
	fRange = fRangeI;
    }

    /**
     * Sets the inherited request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param inheritedI  the new inherited request <code>DataRequest</code>.
     * @see #getInherited()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final void setInherited(DataRequest inheritedI) {
	inherited = inheritedI;
    }

    /**
     * Sets the need frame match flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needFrameMatchI  the new frame match flag.
     * @see #getNeedFrameMatch()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    private final void setNeedFrameMatch(boolean needFrameMatchI) {
	needFrameMatch = needFrameMatchI;
    }

    /**
     * Sets the need time match flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needTimeMatchI  the new time match flag.
     * @see #getNeedTimeMatch()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    private final void setNeedTimeMatch(boolean needTimeMatchI) {
	needTimeMatch = needTimeMatchI;
    }

    /**
     * Sets the request <code>RmapChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rchainI  the request <code>RmapChain</code>.
     * @see #getRchain()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    private final void setRchain(RmapChain rchainI) {
	requestChain = rchainI;
    }

    /**
     * Sets the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the new request <code>Rmap</code>
     * @see #getRequest()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final void setRequest(Rmap requestI) {
	request = requestI;
    }

    /**
     * Sets the last <code>TimeRange</code> seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @param tRangeI  the <code>TimeRange</code>.
     * @see #getFrange()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2001  INB	Created.
     *
     */
    private final void setTrange(TimeRange tRangeI) {
	tRange = tRangeI;
    }

    /**
     * Returns a string representation of this <code>ExtractedChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 09/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    public String toString() {
	String stringR = ((delayRequest ? "Delayed " : "") +
			  "Request: " + getRequest() + "\n" +
			  "Inherited: " + getInherited() + "\n" +
			  "Requested: " + getRchain().toString() + "\n" +
			  "Matched: " + super.toString());

	return (stringR);
    }

    /**
     * Updates the inherited request based on the input matched
     * <code>Rmap</code> and the input source <code>Rmap</code>.
     * <p>
     * If the matched request contains a <code>TimeRange</code> or a frame
     * <code>TimeRange</code>, then the new inherited request is built by
     * subtracting out the <code>TimeRange</code> or frame
     * <code>TimeRange</code> of the input <code>Rmap</code> from the matched
     * request. The <code>needTimeMatch</code> or <code>needFrameMatch</code>
     * flag is cleared.
     * <p>
     * Otherwise, if there is no inherited request already, the new
     * inherited request is built from the <code>TimeRange</code> or frame
     * <code>TimeRange</code> of the request being satisfied and the
     * appropriate <code>needMatch</code> flag is set.
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
     * @param requestI	  the matched request <code>Rmap</code>.
     * @param matchedI	  the matched source <code>Rmap</code>.
     * @param movingDownI are we going to move down the request hierarchy?
     * @since V2.0
     * @version 09/18/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final void updateInherited(Rmap requestI,
			       Rmap matchedI,
			       boolean movingDownI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String inheritedName =
	    (getInherited() == null) ? null : getInherited().getName();

	// Grab any <code>DataRequest</code> flags.
	DataRequest reference = new DataRequest();
	if (getInherited() != null) {
	    reference.combineWith(getInherited());
	}
	if (requestI instanceof DataRequest) {
	    reference.combineWith((DataRequest) requestI);
	}

	// For special requests, any time-range in the request is
	// effectively combined with (added to) the matched value. When we
	// find a matching time or frame range, we just use the matched
	// value.
	boolean special =
	    ((getExtractor().getWorkRequest() instanceof DataRequest) ?
	     (((DataRequest) getExtractor().getWorkRequest()).getReference() !=
	      DataRequest.ABSOLUTE) :
	     false);

	if (getInherited() == null) {
	    setInherited(new DataRequest());
	}

	if ((requestI == null) || (requestI == getInherited())) {
	    // A null request just means that we are looking for missing
	    // information. If the input request is the inherited one, then
	    // we're also just looking for missing information.

	} else if ((movingDownI || delayRequest) &&
		   (requestI.getTrange() != null)) {
	    // If the matched request contains a <code>TimeRange</code>,
	    // then the new inherited request can be built by subtracting
	    // out the <code>TimeRange</code> of the source
	    // <code>Rmap</code>.
	    if (matchedI.getTrange() == null) {
		getInherited().setTrange(requestI.getTrange());
	    } else if (special) {
		getInherited().setTrange(null);
	    } else {
		// If there is a <code>DataBlock</code> associated with matched
		// <code>Rmap</code>, then the inherited request time should
		// be the entire range for a single point. This isn't entirely
		// accurate, but it should work.
		if ((matchedI.getTrange() != null) &&
		    (matchedI.getDblock() != null) &&
		    (matchedI.getTrange().getNptimes() !=
		     matchedI.getDblock().getNpts())) {
		    getInherited().setTrange
			(new TimeRange
			    (0.,
			     matchedI.getTrange().getPointTime
			     (1,
			      matchedI.getDblock().getNpts()) -
			     matchedI.getTrange().getPointTime
			     (0,
			      matchedI.getDblock().getNpts())));
		} else {
		    getInherited().setTrange
			(requestI.getTrange().subtract(matchedI.getTrange()));
		}
	    }
	    setNeedTimeMatch(false);

	} else if ((movingDownI || delayRequest) &&
		   (requestI.getFrange() != null)) {
	    // If the matched request contains a frame
	    // <code>TimeRange</code>, then the new inherited request either
	    // uses it unchanged (no match) or sets it equal to an "infinite"
	    // range (matched).
	    getInherited().setFrange
		((matchedI.getFrange() == null) ?
		 requestI.getFrange() :
		 (special ?
		  null :
		  new TimeRange(-Double.MAX_VALUE/2.,Double.MAX_VALUE)));
	    setNeedFrameMatch(false);

	} else {
	    // If there is a regular inherited request, update the time
	    // information.
	    if ((getInherited().getTrange() != null) &&
		(matchedI.getTrange() != null)) {
		getInherited().setTrange
		    (special ?
		     null :
		     getInherited().getTrange().subtract
		     (matchedI.getTrange()));
	    } else if ((getInherited().getFrange() != null) &&
		       (matchedI.getFrange() != null)) {
		getInherited().setFrange
		    (special ?
		     null :
		     new TimeRange(-Double.MAX_VALUE/2.,Double.MAX_VALUE));
	    }
	}

	if (matchedI.getName() == null) {
	    // If the matched <code>Rmap</code> was nameless, we need to keep
	    // the name.
	    if ((inheritedName == null) && (movingDownI || delayRequest)) {
		if (requestI != null) {
		    getInherited().setName(requestI.getName());
		} else if (getRequest() != null) {
		    getInherited().setName(getRequest().getName());
		}
	    }
		
	} else if ((Rmap.compareNames(inheritedName,"...") != 0) &&
		   (inheritedName != null)) {
	    // If the matched <code>Rmap</code> had a name and we are not
	    // working on the recursive wildcard name "...", then we need to
	    // replace the inherited name.
	    if (getRequest() == null) {
		getInherited().setName(null);
	    } else {
		getInherited().setName(getRequest().getName());
	    }

	} else if ((requestI != null) &&
		   (requestI.compareNames("...") == 0)) {
	    getInherited().setName("...");
	}

	if ((getInherited() != null) && getInherited().isNamelessTimeless()) {
	    setInherited(null);
	}
    }
}
