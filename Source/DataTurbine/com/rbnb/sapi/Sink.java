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

package com.rbnb.sapi;

import com.rbnb.api.*;

/**
 * A simple data sink for RBNB servers.
 * This class is designed to present a simple interface for pulling data
 *  from RBNB servers, via the RMap API.
 * <p>
 * A data sink client has the following tasks: 
 * <ol>
 * <li>Build a channel request with {@link ChannelMap#Add(String)}. </li>
 * <li>Select a sink mode, using {@link #Subscribe(ChannelMap)}, 
 *    {@link #Monitor(ChannelMap,int)},
 *    or {@link #Request(ChannelMap,double,double,String timeRef)}.</li>
 * <li>{@link #Fetch(long,ChannelMap)} data from RBNB server. </li>
 * <li>Extract data and time from API buffers, using the various
 *    GetData functions, such as {@link ChannelMap#GetData(int)}. </li>
 * <li>Repeat step 3 as desired for Subscribe and Monitor, or 2 and 3 for
 *    Request mode. </li>
 * </ol>
 * <p>
 * @author WHF
 *
 * @since V2.0
 * @version 08/19/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/17/2001  WHF	Created.
 * 05/24/2001  WHF      ChannelType returns TYPE_UNKNOWN if data==null.
 * 10/05/2001  WHF	Changed meaning of AddChannel()'s return value:
 *				it now signifies the total number of channels
 *				in the map, and it not useful for the 
 *				Channel*() methods.  Added wildcard support.
 * 10/09/2001  WHF	Fixed bug in ChannelIndex().
 * 01/11/2002  WHF	Added Fetch(long) method.
 * 01/18/2002  WHF	Deimplemented Monitor's gapControl option.
 * 01/25/2002  WHF	Implemented new ChannelMap scheme.
 * 02/04/2002  WHF	Disallowed FrameByFrame requests.
 * 08/07/2002  WHF	Added serialVersionUID member.
 * 01/13/2003  INB	Added "ALIGNED" request reference.
 * 01/14/2003  INB	Added setting of username.
 * 11/10/2003  WHF	Added special '<=' case for abolute requests duration
 *			0.  Added time based subscription.
 * 08/13/2004  INB	Added name matching explanation to Request.
 * 08/19/2004  INB	Added previous/next request references.  Added
 *			Subscribe(ChannelMap,double,double,String).
 * 01/25/2005  EMF      Deprecated Sink(cache,mode,archive) constructor.
 * 10/25/2005  EMF      Added BytesTransferred method.
 * 11/20/2006  EMF      Changed default registration request to * / ...
 *                      so channel list includes one level of plugins and routes
 * 12/21/2006  MJM	Reversed EMF 11/20/2006 change, registration back to "..."
 * 03/08/2007  WHF  Added RequestFrame().
 *
 */
public class Sink extends Client
{
	static final long serialVersionUID = -3107073470270413675L;

	private static final int repeatCountDefault=1;
	private static final double repeatIntervalDefault=1.0;
	private static final boolean fetchByFrameDefault=false;

	// Transient objects which have no persistence:
	private transient com.rbnb.api.Sink sink;

	private Rmap registrationResult=null;

	private static final String 		timeRefErrStr=
		"TimeRef must be one of \"Newest\", \"Oldest\", "
		+"\"After\", \"Modified\", \"Aligned\", \"Next\", "
	        +"\"Previous\", or \"Absolute\" \nfor "
		+"Request()";

	private static final RequestOptions DEFAULT_OPTIONS=new RequestOptions();
		
    /**
     * Default constructor.  Initializes default values of the parameters:
     * <ul><li><code>cacheSize</code> = 1 frame</li>
     * <li><code>archiveSize</code> = 0 frames (off) </li>
     * </ul>
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public Sink()
	{
		super(1,null,0);
	}

    /**
     * A convenience constructor which initializes values for the cache size,
     *  archive mode, and archive size.  Calls {@link Client#SetRingBuffer
     *  (int, String, int) } with the specified options.
     * <p>
     *
     * @author WHF
     *
     * @param cacheSize Size of the memory cache, in frames.
     * @param archiveMode The mode for the optional archive.  Should be one of the
     *   following: <ul>
     *  <li> "none"  - no Archive is made. </li>
     *  <li> "load"  - load an archive, but do not allow any further writing to it. </li>
     *  <li> "create" - create an archive. </li>
     *  <li> "append" - load an archive, but allow writing new data to it. </li>
     * </ul>
     * @param archiveSize The size of the desired archive, in frames.  Ignored except for
     *   "create" and "append".
     *
     * @see Client#CloseRBNBConnection()
     * @see Client#SetRingBuffer(int,String,int)
     *
     * @since V2.0
     * @version 06/04/2001
     * @deprecated This constructor calls {@link #Sink()} instead, since caching
     *   and archiving of requests is not supported by the server.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  WHF	Created.
     * 01/25/2005  EMF  Deprecated and switched call to default, to avoid bugs
     *                  in server.
     *
     */
	public Sink(int cacheSize, String archiveMode, int archiveSize)
	{
		//EMF 01/25/2005: force use of default constructor
		//super(cacheSize,archiveMode,archiveSize);
		super(1,null,0);
	}

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF	Created.
     *
     */
	public long BytesTransferred() {
          if (sink!=null) return sink.bytesTransferred();
          else return 0L;
	}


	/**
	  * Performs work of connecting to the server.  Called by 
	  *  {@link Client#OpenRBNBConnection(String,String,String,String) }
	  * <p>
     * @author WHF
     *
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/08/2002  WHF	Created (from old OpenRBNBConnection)
     * 02/12/2002  WHF  Archive setup moved to Client().
     * 04/05/2002  WHF  Moved server handle creation to Client.
     * 01/14/2003  INB	Added setting of username.
     *
     */
	void doOpen(
		Server server,
		String clientName,
		String userName,
		String password)  throws Exception
	{
		sink=server.createSink(clientName);
/*
		sink.setCframes(getCacheSize());
		sink.setAmode((byte)getArchiveMode());
		sink.setAframes(getArchiveSize());
*/
		prepareArchive(false); // don't reset
		if (userName != null) {
		    sink.setUsername(new Username(userName,password));
		}
		sink.start();
	}


	/**
	  * Initiates a request for a specific time slice of data.
	  *  The data may then be extracted with
	  *  <code>Fetch()</code>.
	  * <p>
     *
     * @author WHF
     *
     * @param start The start time for the request.  If <code>fetchByFrame</code> is
     *  set, this is calculated in frames; otherwise, it is in seconds.
     * @param duration The duration of the request.  Again, its unit is seconds unless
     *  <code>fetchByFrame</code> is set.
     * @param timeRef One of "absolute", "newest", or "oldest".  Determines how
     *   the start parameter is interpreted.
     * @param repeatCount The number of times the request will be repeated automatically.
     * @param repeatInterval The multiple of duration which is used to advance each repeated
     *   request from the start time.
     * @param fetchByFrame If true, the time range is interpreted as frame
     *   indices.  All frames that match are returned in a consolidated 
     *   response.
     * @return None.
     * @exception IllegalStateException If not connected.
     * @exception IllegalArgumentException If any parameters are illegal.
     * @see #Fetch(long,ChannelMap)
     * @since V2.0
     * @version 01/13/2003
     * @deprecated Please use {@link Sink#Request(ChannelMap,double,double,
     *	String)} instead.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 02/13/2002  WHF  Throws IllegalStateException instead of SAPIException
     *     when not connected.
     * 04/10/2002  WHF  Deprecated.
     *
     */
	public final void Request(	ChannelMap cm,
					double start,
					double duration,
					String timeRef,
					int repeatCount,
					double repeatInterval,
					boolean fetchByFrame)
		throws SAPIException
	{
		if (repeatCount<1) throw new IllegalArgumentException(
			"repeatCount must be equal to or greater than one.");
		if (repeatInterval<0.0) throw new IllegalArgumentException(
			"repeatInterval must be equal to or greater than zero.");
		if (sink==null) throw new IllegalStateException(
			"A connection is required to make a request.");

		Request(cm,start,duration,timeRef,fetchByFrame);
/*
		DataRequest dataReq=cm.getDataRequest();

		if (timeRef!=null)
		{
			timeRef=timeRef.toLowerCase();
			if (timeRef.equals("newest"))
			{
				dataReq.setReference(DataRequest.NEWEST);
			}
			else if (timeRef.equals("oldest"))
			{
				dataReq.setReference(DataRequest.OLDEST);
			}
			else if (timeRef.equals("absolute"))
			{
				dataReq.setReference(DataRequest.ABSOLUTE);
			}
			else
				throw new IllegalArgumentException(
					timeRefErrStr);

			try {
			dataReq.setRepetitions((long)repeatCount,repeatInterval);
			// 02/4/2002  WHF/INB: Remove Frame by Frame 
			//   request mode option.  All frame requests will 
			//   be returned as a single block:
//			dataReq.setMode(fetchByFrame
//				?DataRequest.FRAMES:DataRequest.CONSOLIDATED);
			dataReq.setMode(DataRequest.CONSOLIDATED);
			dataReq.setSynchronized(true);
			dataReq.setDomain(DataRequest.EXISTING);
		// INB/MJM 5/25/01: if fetchByFrame, use frame-range
			TimeRange range = new TimeRange(start,duration);
			if (fetchByFrame) {
			    // INB 11/05/2001 - set the range for all children.
			    for (int idx = 0;
				 idx < dataReq.getNchildren();
				 ++idx) {
				dataReq.getChildAt(idx).setFrange(range);
			    }
			} else {
			    // INB 11/05/2001 - set the range for all children.
			    for (int idx = 0;
				 idx < dataReq.getNchildren();
				 ++idx) {
				dataReq.getChildAt(idx).setTrange(range);
			    }
			}
//			dataReq.setAresponses(true);
			sink.addChild(dataReq);
			sink.initiateRequestAt(0);
			} catch (Exception e) { throw new SAPIException(e); }
		}
		else throw new IllegalArgumentException(timeRefErrStr);
*/
	}

	/**
	 * @deprecated Support for 
	 *   requests by frame has been removed from the API.  Please use
	 *	 {@link #Request(ChannelMap,double,double,String) }.
	 *  This method is only here for binary compatibility; calling it throws
	 *  a SAPIException.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2002  WHF	Created.
     * 04/23/2002  WHF  Passes "registration" requests through to 
     *	RequestRegistration().
     * 05/22/2002  WHF  Deprecated.
     * 01/13/2003  INB	Added aligned request reference.
	 * 06/16/2003  WHF  Now only throws exception.  
     */
	public final void Request(	ChannelMap cm,
					double start,
					double duration,
					String timeRef,
					boolean fetchByFrame)
		throws SAPIException
	{
		throw new SAPIException(
			"Unsupported method.");
	}


	/**
	  * Initiates a request for a specific time slice of data; the data may 
	  *  then be extracted with <code>Fetch()</code>.  
	  * <p>The <code>reference</code> parameter determines the
	  *  origin of the <code>start</code> parameter, and must be one of the
	  *   following:
	  * <ul>
	  *	<li>"absolute" -- The start parameter is absolute time from
	  *	midnight, Jan 1st, 1970 UTC.</li>
	  *	<li>"newest" -- The start parameter is measured from the 
	  *	most recent data available in the server at the time this
	  *	request is received.  Note that for this case, the start parameter
	  * actually represents the <strong>end</strong> of the duration, and 
	  * positive times proceed toward oldest data.  Thus if there is data
	  *  at times 1, 2, and 3, Request(map, 1, 0, "newest") will return
	  *  the data at time 2, while Request(map, -1, 2.5, "newest") will return
	  *  the data at times 2 and 3.</li>
	  *	<li>"oldest" -- As "newest", but relative to the oldest data.
	  *	</li>
	  * <li>"aligned" -- As "newest", but rather than per channel, this
	  * is relative to the newest for all of the channels.
	  *	</li>
	  *	<li>"after" -- A combination between "absolute" and "newest",
	  *		this flag causes the server to return the newest
	  *		data available after the specified start time.  Unlike
	  *		"newest", you do not have to request the data to 
	  *		find out that you already have it.  Unlike "absolute",
	  *		a gap may be inserted in the data to provide you with 
	  *		the freshest data.</li>
	  *	<li>"modified" -- Similar to "after", but attempts to return
	  *	a <i>duration's</i> worth of data in a contiguous block.
	  *	If the data is not available after the <code>start</code>
	  *	time, it will be taken from before the start time.</li>
	  *	<li>"next" - gets the data that immediately follows the time
	  *	range specified.  This will skip over gaps.</li>
	  *     <li>"previous" - get the data that immediately preceeds the
	  *	time range specified.  This will skip over gaps.</li>
	  * </ul></p>
	  * <p>If registration and/or meta-data are desired, please use
	  *	{@link #RequestRegistration(ChannelMap)}.  (As a convenience
	  *	to PlugIn developers, this method will also accept
	  *	"registration" as a reference, and will pass it through to
	  *	the RequestRegistration method.</p>
	  * <p>The amount of data
	  *  returned is determined by the <code>duration</code> parameter.
	  *  A duration of zero represents a special case, where one single point
	  *  is returned.  For the "newest", "oldest", and "absolute" time
	  *  references, if an exact time match does not occur the point closest
	  *  to the desired time <strong>in the direction of the origin</strong>
	  *  is returned.
	  * <p>
	  * The way channel names are matched can be somewhat confusing.  There
	  * are two primary ways of specifying names:
	  * <p><ol>
	  *    <li>Absolute names start with a leading slash, while</li>
	  *    <li>Relative names do not have a leading slash.</li>
	  * </ol><p>
	  * There are two wildcards:
	  * <p><ol>
	  *    <li>* means match anything within the current server at the
	  *	   current point in the request, and</li>
	  *    <li>... means match anything within the current server from the
	  *	   current point in the request down.</li>
	  * </ol><p>
	  * Additionally, it is possible to move up using a relative request by
	  * specifying the name '../'.
	  * <p>
	  * Absolute names are matched by starting at the local server's
	  * representation of the root of the server hierarchy (termed a
	  * "RoutingMap", indicated by the leading slash).  Relative names are
	  * matched by starting with the children of the local server.
	  * <p>
	  * Consider the following hierarchy of servers:
	  * <p><dl>
	  *    <dd>/<dl>
	  *	   <dd>AAA/<dl>
	  *	       <dd>[client handlers for AAA]
	  *	       <dd>BBB/<dl>
	  *		   <dd>[client handlers for BBB]
	  *		   <dd>CCC/<dl>
	  *		       <dd>[client handlers for CCC]
	  *	           </dl>
	  *	       </dl>
	  *	    </dl>
	  *    </dl>
	  * </dl><p>
	  * This hierarchy has three servers.  The top level server AAA has a
	  * child named BBB and a grandchild named CCC.  At each level, there
	  * may be additional client handlers (for source, sink, and plugin
	  * connections).
	  * <p>
	  * Each server has its own routing map hierarchy that contains the
	  * part of the above hierarchy known to that server.  The hierarchy
	  * known to the AAA server, for example, is:
	  * <p><dl>
	  *    <dd>/ - the routing map hierarchy root<dl>
	  *	   <dd>AAA/ - the local server<dl>
	  *	       <dd>[client handlers for AAA]
	  *	       <dd>BBB/ - representation of the child BBB server
	  *	   </dl>
	  *    </dl>
	  * </dl><p>
	  * Note that this only goes down to the child BBB server.  AAA has no
	  * knowledge of any children of BBB.  The BBB server's known hierarchy
	  * is:
	  * <p><dl>
	  *    <dd>/ - the routing map hierarchy root<dl>
	  *	   <dd>AAA/ - representation of the parent AAA server<dl>
	  *	       <dd>BBB/ - the local server<dl>
	  *		   <dd>[client handlers for BBB]
	  *		   <dd>CCC/ - representation of the child CCC server
	  *	       </dl>
	  *	   </dl>
	  *    </dl>
	  * </dl><p>
	  * The BBB server knows about its parent AAA and its child CCC
	  * servers, but not their other children.
	  * <p>
	  * Some example requests and an explanation of how they are matched
	  * follows.  These examples use the preceeding hierarchy.
	  * <p>
	  * Absolute requests are the simplest to understand because they
	  * always produce the same answer, regardless of which server the
	  * request is sent to.  The difference is in the specifics of how the
	  * request is matched.  Consider the following three requests:
	  * <p><ol>
	  *    <li>/AAA/...,</li>
	  *    <li>/AAA/BBB/..., and</li>
	  *    <li>/AAA/BBB/CCC/...</li>
	  * </ol><p>
	  * Each of these requests asks for the hierarchy known to and starting
	  * at a specific server.  For example, the request '/AAA/BBB/...'
	  * would return the following:
	  * <p><dl>
	  *    <dd>/<dl>
	  *	   <dd>AAA/<dl>
	  *	       <dd>BBB/<dl>
	  *		   <dd>[client handlers, except for plugins, for BBB
	  *		        and their children
	  *		   <dd>[plugins for BBB]
	  *		   <dd>CCC/
	  *	       </dl>
	  *	   </dl>
	  *    </dl>
	  * </dl><p>
	  * Neither plugins nor child servers of BBB are expanded because this
	  * information is not known to the BBB server.
	  * <p>
	  * If this request is sent to the BBB server, it answers the request
	  * directly out of its routing map hierarchy.  If the request is sent
	  * to either the AAA or CCC servers, they will match /AAA/BBB locally
	  * and then send the remainder ('...') as a relative request to the
	  * BBB server.
	  * <p>
	  * Relative requests depend on which server the request is sent to.
	  * They are matched starting with the children of the local server
	  * rather than at the top of the routing map.  For example, consider
	  * the request 'CCC/...'.  If this request is sent to the BBB server,
	  * it will find the child CCC server as a local child and then send
	  * the remainder ('...') to the CCC server as a relative request.
	  * <p>
	  * If, on the other hand, this request is sent to the AAA server, it
	  * will fail to match anything because the AAA server does not have a
	  * child named CCC.
	  * <p>
	  *
	  * @author WHF
	  *
	  * @param start The start time for the request in seconds.
	  * @param duration The duration of the request, in seconds.
	  * @param reference One of the options listed above.  Determines how the 
	  *   start parameter is interpreted.
	  * @exception SAPIException If not connected.
	  * @exception IllegalArgumentException If any parameters are illegal.
	  * @see #Fetch(long,ChannelMap)
	  * @see #RequestRegistration(ChannelMap)
	  * @since V2.0
	  * @version 08/19/2004
	  *
	  */
	public final void Request(
			ChannelMap cm,
			double start,
			double duration,
			String reference)
		throws SAPIException
	{
		Request(cm,start,duration,reference,DEFAULT_OPTIONS);
	}
	
	/**
	  * Sends the specified {@link RequestOptions} object to the server
	  *  before making the request.
	  * <p>
	  * @see #Request(ChannelMap,double,double,String)
	  * @since V2.1B4
	  * @version 10/14/2004
	  */
	// 2004/10/14  WHF  Added Math.abs() to the start time for the "newest"
	//   case, in case anyone tries to use a negative start time.
	// 2005/01/07  WHF  Removed Math.abs() in favor of more clear documentation.
	public final void Request(
			ChannelMap cm,
			double start, 
			double duration,
			String reference,
			RequestOptions ro)
		throws SAPIException
	{
		assertConnection();
		try {
		sink.sendRequestOptions(ro);
		} catch (Exception e) { throw new SAPIException (e); }

		// Each new request must clear any preexisting registration request:
		registrationResult=null;

// 11/13/2002  WHF  Trying to switch to one Rmap system:
//		DataRequest dataReq=cm.getDataRequest();
		DataRequest dataReq = cm.produceRequest();

//System.err.println("-- Request (first): \n" +dataReq);
		do {
		if (reference!=null)
		{
			reference=reference.toLowerCase();
			if (reference.equals("registration"))
			{
				RequestRegistration(cm);
				return;
			}
			if (reference.equals("newest"))
			{
				// Either a negative or positive start time may be used.
				// 2005/01/07  WHF  Too confusing, breaks code.
				//start = Math.abs(start);
				dataReq.setReference(DataRequest.NEWEST);
				// 2004/08/03  MJM  Added zero duration special case:
				if ( (duration == 0.0) && (start != 0.) )
					dataReq.setRelationship(DataRequest.LESS_EQUAL);
			}
			else if (reference.equals("oldest"))
			{
				// 2004/08/03  MJM  Added zero duration special case:
				if (( duration == 0.0) && (start != 0.) )
					dataReq.setRelationship(DataRequest.LESS_EQUAL);
				dataReq.setReference(DataRequest.OLDEST);
			}
			else if (reference.equals("absolute"))
			{
				dataReq.setReference(DataRequest.ABSOLUTE);
				// 2003/11/10  WHF  Added zero duration special case:
				if (duration == 0.0)
					dataReq.setRelationship(DataRequest.LESS_EQUAL);
			}
			else if (reference.equals("after"))
			{
				dataReq.setReference(DataRequest.AFTER);
				// 2006/11/06  WHF  We want data after the start time, 
				//  as per next, below:
				//dataReq.setRelationship(DataRequest.GREATER);
				// 2006/11/08  WHF  Above doesn't work.  Use offset of 
				// 2e-7 seconds on the start time after the last request
				//  time + duration.
			}
			else if (reference.equals("modified"))
			{
				dataReq.setReference(DataRequest.MODIFIED);
			}
			else if (reference.equals("aligned"))
			{
				dataReq.setReference(DataRequest.ALIGNED);
			}
			else if (reference.equals("next"))
			{
				// 08/19/2004  INB  Gets the data immediately
				// following the specified time range.
				dataReq.setReference(DataRequest.ABSOLUTE);
				if (duration == 0.) {
					// For zero duration, the time range is
					// equal to the start time, so we want
					// data after that.
					dataReq.setRelationship(DataRequest.GREATER);
				} else {
					// For non-zero duration, the time
					// range is exclusive of the start time
					// plus duration, so we want the data
					// at or after that.
					dataReq.setRelationship(DataRequest.GREATER_EQUAL);
				}
			}
			else if (reference.equals("previous"))
			{
				// 08/19/2004  INB  Gets the data immediately
				// preceeding the specified time range.
				dataReq.setReference(DataRequest.ABSOLUTE);
				dataReq.setRelationship(DataRequest.LESS);
			}
			else break;
			//	throw new IllegalArgumentException(timeRefErrStr);

			try {
			// INB 08/19/2004 - Extend start should not effect the
			// relationship for previous/next.
			if (ro.getExtendStart() &&
			    !reference.equals("next") &&
			    !reference.equals("previous"))
				dataReq.setRelationship(DataRequest.LESS_EQUAL);
			dataReq.setRepetitions(repeatCountDefault,
				repeatIntervalDefault);
			dataReq.setMode(DataRequest.CONSOLIDATED);
			dataReq.setSynchronized(true);
			dataReq.setDomain(DataRequest.EXISTING);
		// INB/MJM 5/25/01:  if fetchByFrame, use frame-range
			TimeRange range = new TimeRange(start,duration);
/*			if (fetchByFrame) {
			    // INB 11/05/2001 - set the range for all children.
			    for (int idx = 0;
				 idx < dataReq.getNchildren();
				 ++idx) {
				dataReq.getChildAt(idx).setFrange(range);
			    }
			} else */ {
			    // INB 11/05/2001 - set the range for all children.
			    for (int idx = 0;
				 idx < dataReq.getNchildren();
				 ++idx) {
				dataReq.getChildAt(idx).setTrange(range);
			    }
			}
if (cm.debugFlag) System.err.println("-- Request: \n" +dataReq);
			sink.addChild(dataReq);
			sink.initiateRequestAt(0);
			return;
			} catch (Exception e) { throw new SAPIException(e); }
		}
		} while (false); // ugly, but better than goto
		
		throw new IllegalArgumentException("Parameter TimeRef was = \""+
			reference+"\"; "+timeRefErrStr);
	}		

	/**
	  * Sends a request for the current registration
	  *  map of all channels on the server.
	  * <p>Delegates to {@link #RequestRegistration(ChannelMap)} with 
	  *  a null parameter.
	  *
	  * @author WHF
	  * @version 05/06/2002
	  */
	public final void RequestRegistration() throws SAPIException
	{ RequestRegistration(null); }

	/**
	  * Sends a request to the server for the current registration
	  *  map for the channels in the provided <code>ChannelMap</code>.
	  * &nbsp; If the input <code>ChannelMap</code> is empty or null, it 
	  * will be treated as if it were a request for "&#42/..." (all channels).
	  * <p>The result of the request can be obtained using {@link 
	  *  #Fetch(long, ChannelMap)}.  The result will also contain the 
	  *  meta-data and time ranges for each requested channel.
	  *
	  * <code>requestMap</code> works as it would for a sink requesting data, except you are just 
          * getting the "skeleton" channel map (names).
	  * <p>You can use wildcards to match channels. Available wildcards and their meanings are:
	  * <ul><li>"*"  match all objects at this level</li>
	  * <li>"..."  match from here down recursively (but not across routing links or PlugIns)</li>
	  * <li>".."  up a level</li>
	  * </ul><p> To request registration maps from local and routed servers, use a <code>requestMap</code> 
	  * with one channel named as follows:
	  * <ul><li> Local chans:            "..."</li>
	  * <li>Parent chans:           "../..."</li>
	  * <li>Parent &amp local chans: "../&#42/..."</li>
          * <li>Local &amp child chans:  "&#42/..."</li>
          * </ul><p>Similarly, targeted requests could also be made using absolute paths, such as:
          * <ul><li>Uncle&#39s children:       "/myparent/myuncle/..."</li>
	  * </ul>
	  * @author WHF
	  *
	  * @param requestMap The channels to obtain from the registration map,
	  *  or a null or empty ChannelMap to obtain all.
	  * @exception SAPIException If not connected to a server or there
	  *  is a problem with the connection.
	  * @see #Fetch(long,ChannelMap)
	  * @since V2.0
	  * @version 11/20/2006
	*/
	public final void RequestRegistration(ChannelMap requestMap)
		throws SAPIException
	{
		assertConnection();
		if (requestMap==null) requestMap=new ChannelMap();
		if (requestMap.NumberOfChannels()==0) {
                        //EMF 11/20/06: try one more level, so PlugIns return channels
			//MJM 12/21/06: back this off, it causes high overhead w/ plugins
			requestMap.Add("...");
			//requestMap.Add("*/...");
                }

//		DataRequest dr=requestMap.getDataRequestA();
		DataRequest dr=requestMap.produceRequest();
//System.err.println("-- RequestRegistration: \n" +dr);

		try {
		// TODO: Registration should be requested, and retrieved with 
		//  fetch, just like everything else:
		registrationResult=getClient().getRegistered(dr);
//		requestMap.Clear();
//		processFetchedResult(result,requestMap);
		} catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Similar to Subscribe, but allows for continuous frames of data
	  *  without gaps.
	  * <p>
	  * In this mode, the server will attempt to send the most recent
	  *  data available.  If the client cannot maintain synchronicity,
	  *  the server will omit some frames in an effort to keep the 
	  *  client up to date.
	  * <p>
	  * <b>Note:</b> The gap control feature is currently not implemented.
     *
     * @author WHF
     *
     * @param gapControl Identifies the number of frames sent without gaps
	before a gap may be inserted to resynchronize this sink's output
	with the source.
     * @return None.
     * @exception IllegalArgumentException If gapControl &#60; 0.
     * @exception SAPIException If not connected or no channels have been 
	 *		specified.
     * @see #Fetch(long,ChannelMap)
     * @see #Subscribe(ChannelMap)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public final void Monitor(ChannelMap cm,int gapControl) 
		throws SAPIException
	{
		if (gapControl<0)
			throw new IllegalArgumentException(
				"Gap control must be greater than or equal to"+
				" zero in Monitor()");

		DataRequest dataReq=cm.produceRequest();
		// INB - changed from 1.0 to 0.0, which means jump.
		// WHF (i.e., we always get the newest frame.)
		dataReq.setRepetitions(DataRequest.INFINITE,0.0);
//		dataReq.setGapControl(gapControl>0);  // mjm 6/8/01
		dataReq.setGapControl(true);
		startsubscription(dataReq, DataRequest.NEWEST);
	}

	/**
	  * Makes a subscription for newest data with the server.
	  *  Effectively calls {@link #Subscribe(ChannelMap, String) } with 
	  *  "newest" as the time reference.
	  * <p>
     *
     * @author WHF
     *
     * @return None.
     * @exception SAPIException If not connected or no channels have 
	 *  been specified.
     * @see #Fetch(long,ChannelMap)
     * @since V2.0
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
	 * 06/20/2003  WHF  Passes timeref argument to startsubscription().
     *
     */
	public final void Subscribe(ChannelMap cm) throws SAPIException	{
		DataRequest dataReq=cm.produceRequest();
		// 1.0 means increment one frame:
		dataReq.setRepetitions(DataRequest.INFINITE,1.0);
		startsubscription(dataReq, DataRequest.NEWEST);
	}
	
	/**
	  * Starts a continuous feed of data on the specified channels to this
	  *  sink, for retrieval with <code>Fetch()</code>.
	  * <p>
     *
     * @author WHF
     *
	 * @param timeReference One of "newest" or "oldest".
     * @return None.
     * @exception SAPIException If not connected or no channels have been 
	 *		specified.
	 * @throws NullPointerException If timeReference is null.
	 * @throws IllegalArgumentException If timeReference is illegal.
     * @see #Fetch(long,ChannelMap)
     * @since V2.1B4
     * @version 08/19/2004
     * @deprecated Please use {@link Sink#Subscribe(ChannelMap,double,double,String)} instead. 
     * Subscription from oldest by frames can have problems reading from compressed archives.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  WHF	Created.
     *
     */
	public final void Subscribe(ChannelMap cm, String timeReference)
		throws SAPIException
	{
		byte timeref;
		timeReference=timeReference.toLowerCase();
		if ("newest".equals(timeReference)) timeref=DataRequest.NEWEST;
		else if ("oldest".equals(timeReference)) timeref=DataRequest.OLDEST;
		else throw new IllegalArgumentException(
			"The Subscribe() time reference must be one "
			+"of \"newest\" or \"oldest\".");
		DataRequest dataReq=cm.produceRequest();
		// 1.0 means increment one frame:
		dataReq.setRepetitions(DataRequest.INFINITE,1.0);
		startsubscription(dataReq, timeref);
	}

	/**
	  * Starts a continuous feed of data on the specified channels to this
	  *  sink, for retrieval with <code>Fetch()</code>.  Each block retrieved
	  *  by Fetch will be <code>duration</code> time units in length.
	  * <p>
	  *
	  * @author WHF
	  * 
	  * @param channelMap ChannelMap of desired channels.
	  * @param timeReference One of "newest" or "oldest".
	  * @param duration The number of time units of data for each Fetch().
	  * @exception SAPIException If not connected or no channels have been 
	  *		specified.
	  * @throws NullPointerException If timeReference is null.
	  * @throws IllegalArgumentException If timeReference is illegal.
	  * @see #Fetch(long,ChannelMap)
	  * @since V2.2B5
	  * @version 2003/11/10
	  * @deprecated Please use {@link
	  * Sink#Subscribe(ChannelMap,double,double,String)} instead.  A start time
	  *  of zero provides the functionality of this method for "newest" and 
	  *  "oldest" start times.
	  * 
	 */

	 /*
	  *
	  *   Date      By	Description
	  * MM/DD/YYYY
	  * ----------  --	-----------
	  * 2004/19/08  INB	Deprecated.
	  * 2003/11/10  WHF	Created.
	  *
	  */
	public final void Subscribe(
			ChannelMap channelMap,
			String timeReference,
			double duration) throws SAPIException
	{
		byte timeref, domain;
		double start;

		timeReference=timeReference.toLowerCase();
		if ("newest".equals(timeReference)) {
			timeref = DataRequest.NEWEST;
			domain = DataRequest.FUTURE;
			start = 0.0; 
		} else if ("oldest".equals(timeReference)) {
			timeref = DataRequest.ABSOLUTE;
			domain = DataRequest.ALL;
			start = Double.NEGATIVE_INFINITY;
		} else throw new IllegalArgumentException(
			"The Subscribe() time reference must be one "
			+"of \"newest\" or \"oldest\".");
		DataRequest dataReq = channelMap.produceRequest();
		// 1.0 means increment one frame:
		dataReq.setRepetitions(DataRequest.INFINITE,1.0);
		startTimeSubscription(dataReq, timeref, domain, start, duration);
	}

	/**
	  * Starts a continuous feed of data on the specified channels to this
	  * sink, for retrieval with <code>Fetch()</code>.  Each block
	  * retrieved by Fetch will be <code>duration</code> time units in
	  * length.
	  *
	  * <p>The <code>Subscribe</code> method is essentially the same as an 
	  *  infinite series of <code>Request</code>s, except that performance and
	  *  latency are improved.  After the first Fetch(), the start time is 
	  *  adjusted automatically as appropriate for the time reference provided.
	  *
	  * <p>Please see {@link #Request(ChannelMap, double, double, String)}
	  *  for the definition of the time references, and the meaning of the 
	  *  start parameter for each case.
	  *
	  * @author Ian Brown
	  * 
	  * @param channelMap ChannelMap of desired channels.
	  * @param startTime the start time of the request.
	  * @param duration The number of time units of data for each Fetch().
	  * @param timeReference Any of "newest", "oldest", "absolute",
	  *			 "next", or "previous".
	  * @exception SAPIException If not connected or no channels have been 
	  *		specified.
	  * @throws NullPointerException If timeReference is null.
	  * @throws IllegalArgumentException If timeReference is illegal.
	  * @see #Fetch(long,ChannelMap)
	  * @see #Request(ChannelMap,double,double,String)
	  * @since V2.2B5
	  * @version 2004/19/08
	  * 
	  */

	 /*
	  *
	  *   Date      By	Description
	  * MM/DD/YYYY
	  * ----------  --	-----------
	  * 2004/19/08  INB	Created.
	  *
	  */
	public final void Subscribe(
			ChannelMap channelMap,
			double startTime,
			double duration,
			String timeReference) throws SAPIException
	{
		byte timeref, domain;

		timeReference=timeReference.toLowerCase();
		if ("newest".equals(timeReference)) {
			timeref = DataRequest.NEWEST;
			domain = DataRequest.FUTURE;
		} else if ("oldest".equals(timeReference)) {
			timeref = DataRequest.OLDEST;
			domain = DataRequest.ALL;
		} else if ("absolute".equals(timeReference)) {
			timeref = DataRequest.ABSOLUTE;
			domain = DataRequest.ALL;
		} else if ("next".equals(timeReference)) {
			timeref = DataRequest.ABSOLUTE;
			domain = DataRequest.ALL;
		} else if ("previous".equals(timeReference)) {
			timeref = DataRequest.ABSOLUTE;
			domain = DataRequest.EXISTING;
		} else throw new IllegalArgumentException(
			"The Subscribe() time reference must be one "
			+"of \"Newest\", \"Oldest\", \"Next\", "
			+"\"Previous\", or \"Absolute\".\n");

		DataRequest dataReq = channelMap.produceRequest();
		// 1.0 means increment one frame:
		dataReq.setRepetitions(
				DataRequest.INFINITE,
				("previous".equals(timeReference) ? -1. : 1.));
		startTimeSubscription(dataReq, timeref, domain, startTime, duration);
	}

	/**
	  * Obtains the data and time values for the set of channels
	  *  added via {@link ChannelMap#Add}</code> and stores them in 
	  *  the <code>ChannelMap</code> you provide.  This function returns
	  *  a ChannelMap filled with the channels which matched the
	  *  request; if none, an empty map is returned.  This may
	  *    be less than the number of calls to Add(), if some channels
	  *    lack data, or it may be greater, if wildcards were used.
	  * <p>
     *
     * @author WHF
     *
     * @param cm The ChannelMap object which is filled with the data received
     *    from the server.  If this parameter is null, a new ChannelMap is
     *    created.
     * @param blockTimeout The amount of time (ms) to wait for data to become
     *  available.  Use 0 for no delay or any negative number for an 
     *  infinite delay.
     * @return The ChannelMap object provided, or a newly created one.
     *    
     * @exception SAPIException If there are problems obtaining data from
     *  the server.
     * @see #Request(ChannelMap,double,double,String)
     * @see #Subscribe(ChannelMap)
     * @see #Monitor(ChannelMap,int)
     * @see ChannelMap#GetData(int)
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  WHF	Created.
     */
	public final ChannelMap Fetch(long blockTimeout, ChannelMap cm)
		throws SAPIException
	{
		assertConnection();

		if (cm==null) cm=new ChannelMap();
		else cm.Clear();
		try {
		Rmap result;
		if (registrationResult!=null)
		{
			result=registrationResult;
			registrationResult=null;
		}
		else
		// This will update the RMap.
			result=sink.fetch(blockTimeout<0
				?com.rbnb.api.Sink.FOREVER:blockTimeout);
//System.err.println("ChannelMap::Fetch(): "+result);
//		processFetchedResult(result,cm);
		cm.processResult(result, true, false);

		// mjm 9/2004:: about here put check for "redirection", re-issue request or subscribe 
		// using sink.getDataRequest() or equivalent
		return cm;
		// INB - pass runtime exceptions through.
		} catch (java.lang.RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}

	/**
	  * Obtains the data and time values for the set of channels
	  *  added via {@link ChannelMap#Add}</code> and stores them in 
	  *  a newly created <code>ChannelMap</code>.
	  * <p>Calls <code>Fetch(null,long)</code>.
	  * <p>
	  * @see #Fetch(long, ChannelMap)
	  */
	public final ChannelMap Fetch(long blockTimeout)
		throws SAPIException
	{ return Fetch(blockTimeout,null); }
	
	/**
	  * Makes a request for the next new frame.  This is similar to 
	  *   {@link #Monitor(ChannelMap, int)},
	  *  except only one response is given.
	  *
	  * <p>Note: this is only applicable to new data,
	  *   as archived data never has new frames.
	  */
	public final void RequestFrame(ChannelMap cm) throws SAPIException
	{
		DataRequest dataReq=cm.produceRequest();
		// 1.0 means increment one frame:
		dataReq.setRepetitions(1, 1.0);
		startsubscription(dataReq, DataRequest.NEWEST);
	}

////////////////////////////////////////////////////////////////////////////
////////////////////// Package private functions from client: //////////////
	com.rbnb.api.Client getClient() { return sink; }
	final void clearData()
	{
		sink=null;
	}

////////////////////////////////////////////////////////////////////////////
////////////////////// Private utilities: //////////////
	// 06/20/2003  WHF  Added timeref.
	private void startsubscription(
			DataRequest dataReq,
			byte timeref) throws SAPIException
	{
		assertConnection();

		// Each new request must clear any preexisting registration request:
		registrationResult=null;

		try {
		dataReq.setReference(timeref);
		dataReq.setMode(DataRequest.FRAMES);
		dataReq.setSynchronized(true);
		dataReq.setDomain(DataRequest.FUTURE);
		// INB 11/05/2001 - set the range for all children.
		for (int idx = 0; idx < dataReq.getNchildren(); ++idx) {
		    dataReq.getChildAt(idx).setFrange(new TimeRange(0.0,0.0));
		}
//System.err.println("ChannelMap::startsubscription(): "+dataReq);
		sink.addChild(dataReq);
		sink.initiateRequestAt(0);
		} catch (Exception e) { throw new SAPIException(e); }
	}

	// 2004/19/08  INB  If the domain is existing, then we're going
	//		    backwards.
	// 2003/11/10  WHF  Created.
	private void startTimeSubscription(
			DataRequest dataReq,
			byte timeref,
			byte domain,
			double start,
			double duration) throws SAPIException
	{
		assertConnection();

		// Each new request must clear any preexisting registration request:
		registrationResult=null;

		try {
		dataReq.setReference(timeref);
		dataReq.setMode(DataRequest.CONSOLIDATED);
		dataReq.setSynchronized(true);
		dataReq.setDomain(domain);
		if (domain == DataRequest.EXISTING) {
		    dataReq.setRelationship(DataRequest.LESS);
		} else {
		    dataReq.setRelationship(DataRequest.GREATER);
		}
		// INB 11/05/2001 - set the range for all children.
		for (int idx = 0; idx < dataReq.getNchildren(); ++idx) {
		    dataReq.getChildAt(idx).setTrange(new TimeRange(start,duration));
		}
//System.err.println("ChannelMap::startsubscription(): "+dataReq);
		sink.addChild(dataReq);
		sink.initiateRequestAt(0);
		} catch (Exception e) { throw new SAPIException(e); }
	}

} // end class Sink


