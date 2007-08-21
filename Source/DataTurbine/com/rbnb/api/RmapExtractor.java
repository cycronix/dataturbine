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
 * Extracts information from an <code>Rmap</code> hierarchy based on a request
 * <code>Rmap</code> hierarchy. 
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap
 * @see com.rbnb.api.RmapChain
 * @see com.rbnb.api.DataArray
 * @since V2.0
 * @version 03/18/2003
 */

/*
 * Copyright 2000, 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/18/2003  INB	Ensure that combined results contain marker blocks.
 * 11/30/2000  INB	Created.
 *
 */
class RmapExtractor {
    /**
     * extract the data?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/04/2000
     */
    private boolean extractData = false;

    /**
     * extract <code>Rmaps</code> from the hierarchy?
     * <p>
     * If this is true, the <code>RmapExtractor</code> extracts an
     * <code>Rmap</code> hierarchy from the source <code>Rmap</code> hierarchy
     * that satisfies the request.
     * <code>
     * If this is false, the <code>RmapExtractor</code> extracts a
     * <code>DataArray</code> object from the source <code>Rmap</code>
     * hierarchy that satisfies the request. The request should not contain
     * more than a single leaf <code>Rmap</code> or the results 
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/11/2000
     */
    private boolean extractRmaps = false;

    /**
     * extract the frame information?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/18/2000
     */
    private boolean extractFrame = false;

    /**
     * extract the time information?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/11/2000
     */
    private boolean extractTime = false;

    /**
     * require data to terminate matching operation?
     * <p>
     * If this flag is set (the default), then the request is matched only if a
     * <code>DataBlock</code> has been found in the <code>RmapChain</code>
     * leading to an entry matched out of the request. If it is false, then a
     * match occurs whenever the bottom of the request hierarchy is reached
     * successfully.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/03/2001
     */
    private boolean requireData = true;

    /**
     * the information extractor.
     * <p>
     * This object actually performs the work of pulling information of the a
     * matched source <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/11/2000
     */
    private InformationExtractor extractor = null;

    /**
     * the request <code>Rmap</code> being worked on.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/07/2000
     */
    private Rmap workRequest = null;


    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/18/2000
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    RmapExtractor() {
	super();
    }

    /**
     * Class constructor to build an <code>RmapExtractor</code> from a request
     * <code>Rmap</code>, extract <code>Rmaps</code> flag, and extract frame,
     * time,  and data flags.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	     the request <code>Rmap</code>.
     * @param extractRmapsI  extract <code>Rmaps</code>?
     * @param extractFrameI  extract the frame information?
     * @param extractTimeI   extract the time information?
     * @param extractDataI   extract the data payloads?
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the request contains any group members. This method
     *		  only checks top level of the request.
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    RmapExtractor(Rmap requestI,
		  boolean extractRmapsI,
		  boolean extractFrameI,
		  boolean extractTimeI,
		  boolean extractDataI) {
	this(requestI,
	     extractRmapsI,
	     extractFrameI,
	     extractTimeI,
	     extractDataI,
	     true);
    }

    /**
     * Class constructor to build an <code>RmapExtractor</code> from a request
     * <code>Rmap</code>, extract <code>Rmaps</code> flag, and extract frame,
     * time, and data flags.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	     the request <code>Rmap</code>.
     * @param extractRmapsI  extract <code>Rmaps</code>?
     * @param extractFrameI   extract the frame information?
     * @param extractTimeI   extract the time information?
     * @param extractDataI   extract the data payloads?
     * @param requireDataI   require that a <code>DataBlock</code> be found?
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the request contains any group members. This method
     *		  only checks top level of the request.
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    RmapExtractor(Rmap requestI,
		  boolean extractRmapsI,
		  boolean extractFrameI,
		  boolean extractTimeI,
		  boolean extractDataI,
		  boolean requireDataI) {
	this();
	setWorkRequest(requestI);
	setExtractRmaps(extractRmapsI);
	setExtractFrame(extractFrameI);
	setExtractTime(extractTimeI);
	setExtractData(extractDataI);
	setRequireData(requireDataI);
    }

    /**
     * Adds an <code>Rmap</code> to the extracted information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param informationI  the <code>Rmap</code> containing the extracted
     *			    information. 
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    final void addInformation(Rmap informationI) {
	getExtractor().addInformation(informationI);
    }

    /**
     * Combines the information in the <code>InformationExtractor</code> into a
     * single object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reasonI  the reason (match value) that explains why there might
     *		       be no data.
     * @return the combined information.  This is an <code>EndOfStream</code>
     *	       if the request was should have returned an <code>Rmap</code> and
     *	       no matches were found.
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
     *		  thrown if there is a problem in combined the desired
     *		  information.
     * @since V2.0
     * @version 03/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2003  INB	Ensure that combined results contain marker blocks.
     * 12/11/2000  INB	Created.
     *
     */
    final Object combine(byte reasonI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Object combinedR = null;

	if (getExtractor() != null) {
	    // If there is an <code>InformationExtractor</code>, then use it to
	    // produce the combined information.

	    if (getExtractor() instanceof DataArrayExtractor) {
		// If we're extracting a <code>DataArray</code> object, then
		// build it from the <code>DataArrayExtractor</code>.
		combinedR =
		    ((DataArrayExtractor) getExtractor()).getTimeAndData
		    (getExtractFrame(),
		     getExtractTime(),
		     getExtractData());

	    } else {
		// If we're extracting a <code>Rmap</code> object, we create
		// a single <code>Rmap</code> holding all of the information.
		java.util.Vector info = getExtractor().getInformation();

		if ((info != null) && (info.size() > 0)) {
		    Rmap combined = (Rmap) info.firstElement();
		    for (int idx = 1; idx < info.size(); ++idx) {
			Rmap rmap = (Rmap) info.elementAt(idx);
			combined = combined.mergeWith(rmap);
		    }

		    combinedR = combined;
		}

		if (combinedR == null) {
		    // If there is no response, check the input reason
		    // code. 
		    switch (reasonI) {
		    case Rmap.MATCH_BEFORE:
			// The data was all before the request.
			combinedR = new EndOfStream(EndOfStream.REASON_EOD);
			break;

		    case Rmap.MATCH_BEFORENAME:
		    case Rmap.MATCH_AFTERNAME:
			// The data did not match a name.
			combinedR = new EndOfStream(EndOfStream.REASON_NONAME);

		    case Rmap.MATCH_AFTER:
			// The data was all after the request.
			combinedR = new EndOfStream(EndOfStream.REASON_BOD);
			break;

		    default:
			// The request did not match any data.
			combinedR = new EndOfStream(EndOfStream.REASON_NODATA);
			break;
		    }
		}
	    }

	    if ((combinedR instanceof Rmap) &&
		(getWorkRequest() != null) &&
		(getWorkRequest() instanceof DataRequest)) {
		Rmap combined = (Rmap) combinedR;
		DataRequest dr = (DataRequest) getWorkRequest();
		if (dr.getReference() != DataRequest.ABSOLUTE) {
		    boolean needMerge = false;
		    for (int idx = 0;
			 (idx < dr.getNchildren()) && !needMerge;
			 ++idx) {
			if (dr.getChildAt(idx).getFrange() != null) {
			    needMerge = true;
			}
		    }

		    if (needMerge) {
			TimeRange fRange = combined.mergeFrange
			    (dr.getReference(),
			     null);
			String[] names = dr.extractNames();
			if ((names.length > 1) ||
			    ((names.length == 1) &&
			     (!names[0].endsWith("/*") &&
			      !names[0].endsWith("/...")))) {
			    for (int idx = 0;
				 idx < combined.getNchildren();
				 ++idx) {
				combined.getChildAt(idx).setFrange(fRange);
			    }

			} else if (names.length == 1) {
			    while (combined.getNchildren() > 0) {
				combined.removeChildAt(0);
			    }
			    combined.addChild(Rmap.createFromName(names[0]));
			    combined.getChildAt(0).setFrange(fRange);
			    Rmap bottom = combined.moveToBottom();
			    bottom.setDblock(Rmap.MarkerBlock);

			} else if (names.length == 0) {
			    combinedR = null;
			}
		    }
		}
	    }
	}

	return (combinedR);
    }

    /**
     * Extracts the desired information from the input <code>Rmap</code> as a
     * single object.
     * <p>
     * The returned information depends on the inputs and is one of:
     * <p><ol>
     * <li>an <code>Rmap</code> hierarchy, or</li>
     * <li>a <code>DataArray</code> object.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param rootI  the root <code>Rmap</code> of the source hierarchy.
     * @return the extracted information.
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
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    final Object extract(Rmap rootI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reason = Rmap.MATCH_NOINTERSECTION;

	if (getExtractRmaps()) {
	    // If we're extracting <code>Rmaps</code>, create a
	    // <code>RmapInformationExtractor</code> to extract them.
	    setExtractor(new RmapInformationExtractor());

	} else {
	    // If we're extracting time and data, create a
	    // <code>DataArrayExtractor</code> to extract
	    // <code>DataArray</code> objects.
	    setExtractor(new DataArrayExtractor());
	}

	// actual work is done by matchRoot - mjm
	if (getWorkRequest() != null) {
	    // Match the input <code>Rmap</code> against the request
	    // <code>Rmap</code>. This builds partially extracted information
	    // that needs to be combined into a single return object.
	    reason = matchRoot(rootI);
// System.err.print("RmapExtractor.extract(): reason = " + reason);
	}

	// Combine the extracted information in a single return object.
	Object answerR = combine(reason);
// System.err.println(", combined reason = " + answerR);
	return (answerR);
    }

    /**
     * Extracts the desired information from the input
     * <code>ExtractedChain</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	 the end of the request.
     * @param chainI	 the <code>ExtractedChain</code>.
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
    final void extractInformation(Rmap requestI,
				  ExtractedChain chainI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//long startEI;
	synchronized (this) {
//startEI=System.currentTimeMillis();
	    if (getExtractor() == null) {
		// If there is no extractor yet, then build one.

		if (getExtractRmaps()) {
		    // If we're extracting <code>Rmaps</code>, create a
		    // <code>RmapInformationExtractor</code> to extract them.
		    setExtractor(new RmapInformationExtractor());

		} else {
		    // If we're extracting time and data, create a
		    // <code>DataArrayExtractor</code> to extract
		    // <code>DataArray</code> objects.
		    setExtractor(new DataArrayExtractor());
		}
	    }
	}

	// Extract the information from the <code>RmapChain</code>.
	getExtractor().extract(requestI,
			       chainI,
			       getExtractFrame(),
			       getExtractData());
//System.err.println("RmapExtractor.extractInformation: "+(System.currentTimeMillis()-startEI));
    }

    /**
     * Gets the data extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return extract the data?
     * @see #setExtractData(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2000  INB	Created.
     *
     */
    final boolean getExtractData() {
	return (extractData);
    }

    /**
     * Gets the frame extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return extract the frame indexes?
     * @see #setExtractFrame(boolean)
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/18/2001  INB	Created.
     *
     */
    final boolean getExtractFrame() {
	return (extractFrame);
    }

    /**
     * Gets the <code>InformationExtractor</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>InformationExtractor</code>.
     * @see #setExtractor(com.rbnb.api.InformationExtractor)
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
    private final InformationExtractor getExtractor() {
	return (extractor);
    }

    /**
     * Gets the extract <code>Rmaps</code> flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return extract <code>Rmaps</code>?
     * @see #setExtractRmaps(boolean)
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
    final boolean getExtractRmaps() {
	return (extractRmaps);
    }

    /**
     * Gets the time extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return extract the time?
     * @see #setExtractTime(boolean)
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
    final boolean getExtractTime() {
	return (extractTime);
    }

    /**
     * Gets the require data flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  is data required?
     * @see #setRequireData(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    final boolean getRequireData() {
	return (requireData);
    }

    /**
     * Gets the work request <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the work request <code>Rmap</code>.
     * @see #setWorkRequest(com.rbnb.api.Rmap)
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
    final Rmap getWorkRequest() {
	return (workRequest);
    }

    /**
     * Matches the input <code>Rmap</code> hierarchy against the request
     * <code>Rmap</code>.
     * <p>
     * The results of this operation is a vector of partially digested
     * information that will need to be combined into a single object by the
     * caller.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rootI  the root of the source <code>Rmap</code> hierarchy.
     * @return the possible reason for no result.
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
     * @version 09/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2000  INB	Created.
     *
     */
    private final byte matchRoot(Rmap rootI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	// Create a single unsatisified <code>ExtractedChain</code> consisting
	// of the work request for this <code>RmapExtractor</code>.
	ExtractedChain eChain = new ExtractedChain(this,getWorkRequest());

	// Create a vector to hold the input <code>Rmap</code>.
	RmapVector rootList = RmapVector.addToVector(null,rootI);

	// If the request is nameless and timeless, we'll have to handle
	// further <code>ExtractedChains</code> in this method.
	java.util.Vector unsatisfied =
	    getWorkRequest().isNamelessTimeless() ?
	    new java.util.Vector() :
	    null;

	// Match the <code>ExtractedChain</code> against the source root.
	reasonR = eChain.matchList(!getExtractRmaps(),rootList,unsatisfied);

	if (unsatisfied != null) {
	    // If the original request was nameless and timeless, match again.
	    for (int idx = 0; idx < unsatisfied.size(); ++idx) {
		// For all of the unsatisfied chains, work on satisfying them.
		eChain = (ExtractedChain) unsatisfied.elementAt(idx);

		byte reason = eChain.matchList(!getExtractRmaps(),
					       rootList,
					       null);

		reasonR = Rmap.combineReasons(reasonR,reason);
	    }
	}

	return (reasonR);
    }

    /**
     * Sets the data extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractDataI  extract the data?
     * @see #getExtractData()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/04/2000  INB	Created.
     *
     */
    final void setExtractData(boolean extractDataI) {
	extractData = extractDataI;
    }

    /**
     * Sets the frame extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractFrameI  extract the frame indexes?
     * @see #getExtractFrame()
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/18/2001  INB	Created.
     *
     */
    final void setExtractFrame(boolean extractFrameI) {
	extractFrame = extractFrameI;
    }

    /**
     * Sets the <code>InformationExtractor</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI  the <code>InformationExtractor</code>.
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
    final void setExtractor(InformationExtractor extractorI) {
	extractor = extractorI;
    }

    /**
     * Sets the extract <code>Rmaps</code> flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractRmapsI  extract <code>Rmaps</code>?
     * @see #getExtractRmaps()
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
    final void setExtractRmaps(boolean extractRmapsI) {
	extractRmaps = extractRmapsI;
    }

    /**
     * Sets the time extraction flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractTimeI  extract the time?
     * @see #getExtractTime()
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
    final void setExtractTime(boolean extractTimeI) {
	extractTime = extractTimeI;
    }

    /**
     * Sets the require data flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requireDataI  is data required?
     * @see #getRequireData()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    private final void setRequireData(boolean requireDataI) {
	requireData = requireDataI;
    }

    /**
     * Sets the work request <code>Rmap</code>.
     * <p>
     * If there is no current request <code>Rmap</code>, this method sets the
     * work request to be the current request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param workRequestI  the work request <code>Rmap</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the request contains any group members. This method
     *		  only checks top level of the request.
     * @see #getWorkRequest()
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
    final void setWorkRequest(Rmap workRequestI) {
	try {
	    if ((workRequestI != null) &&
		(workRequestI.getNmembers() > 0)) {
		throw new java.lang.IllegalArgumentException
		    ("Cannot perform extraction with a request Rmap that " +
		     "contains group members.");
	    }
	} catch (com.rbnb.api.AddressException e) {
	} catch (com.rbnb.api.SerializeException e) {
	} catch (java.io.IOException e) {
	} catch (java.lang.InterruptedException e) {
	}

	workRequest = workRequestI;
    }
}
