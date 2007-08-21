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
  * A subclass of ChannelMap which contains information about requests that
  *  PlugIns should handle.
  * <p>
  * @author WHF
  * 
*/

/*
 * Copyright 2002, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/30/2002  WHF	Created.
 * 02/13/2002  WHF	Strip off leading slash in names generated from
 *    received request.
 * 08/07/2002  WHF  Made inner class serializable.    
 * 09/24/2002  WHF  Removed 1.2 dependencies.
 * 09/14/2004  MJM  Added new form of produceoutput to enable streaming  
 * 03/13/2007  WHF  Minor refactoring; slight change in meaning of 
 *    IsRequestFrames() to exclude registration requests.
 *
 */

public class PlugInChannelMap extends ChannelMap
{
	static final long serialVersionUID = 6543714829227271536L;
	
	/**
	  * Request type constants.
	  *
	  * @deprecated  Only requests now received by PlugIns; use 
	  *    GetIfFetchTimedOut to detect timeout status.
	  */
	public static final int
			RT_ENDOFSTREAM = 1,
			RT_MONITOR = 2,
			RT_REQUEST = 3,
			RT_SUBSCRIBE = 4,
			RT_TIMEOUT = 5;

	/**
	  * For a plugin-request, returns the start time of the request.
	  *  The start time must be interpretted with the request reference.
	  * <p>
     *
     * @author WHF
     *
     * @see #GetRequestDuration()
     * @see #GetRequestReference()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  WHF	Created.
     *
     */
	public double GetRequestStart()
	{ return pluginStart; }

	/**
	  * For a plugin-request, returns the duration of the request, or
	  * how many seconds worth of data to return.
	  * <p>
     *
     * @author WHF
     *
     * @see #GetRequestStart()
     * @see #GetRequestReference()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  WHF	Created.
     *
     */
	public double GetRequestDuration()
	{ return pluginDuration; }

	/**
	  * For a plugin-request, returns the time reference of the request.
	  *  This is one of "newest", "oldest", "absolute", "modified", "next", or
	  *  "after", if the original
	  *  request was a request for data.  (See {@link Sink#Request(
	  *   ChannelMap,double,double,String,boolean)}.)  If the sink used 
	  *  {@link Sink#RequestRegistration(ChannelMap)}, then this method 
	  *  will return "registration".  If the sink used {@link 
	  *  Sink#Subscribe(ChannelMap)} or {@link Sink#Monitor(ChannelMap,
	  *  int)}, then "subscribe" or "monitor" will be returned, 
	  *  respectively.
	  *  <p>If this map is empty, null will be returned.
	  * <p>
     *
     * @author WHF
     *
     * @see Sink#Request(ChannelMap, double,double,String)
     * @see #GetRequestStart()
     * @see #GetRequestDuration()
	 * @see #GetRequestType()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  WHF	Created.
     * 09/23/2004  MJM  Added "next" type
     * 02/07/2005  MJM  Added "previous" type
     *
     */
	public String GetRequestReference()
	{ return pluginTimeReference; }
	
	
	/**
	  * For a plugin-request, returns the type of the originating request.
	  *  If the original request was made using {@link Sink#Request(
	  *   ChannelMap,double,double,String)} or  
	  * {@link Sink#RequestRegistration(ChannelMap)}, then this method 
	  *  will return {@link #RT_REQUEST}.  If the sink used {@link 
	  *  Sink#Subscribe(ChannelMap)} or {@link Sink#Monitor(ChannelMap,
	  *  int)}, then {@link #RT_SUBSCRIBE} or {@link #RT_MONITOR} will be 
	  *  returned, respectively, <strong>unless</strong>, the stream has been
	  *  closed by the requestor, in which case {@link #RT_ENDOFSTREAM} is
	  *  returned.
	  *  <p>If the fetch timed out, the return value is {@link #RT_TIMEOUT}.
	  *   The method {@link ChannelMap#GetIfFetchTimedOut()} will return true
	  *   also.
	  *  <p>If this map is not the result of a fetch, the value is undefined.
	  * <p>
     *
	 * @deprecated Only requests are now received by PlugIns, in order to
	 *  simplify their implementation.
     * @author WHF
     *
     * @see Sink#Request(ChannelMap,double,double,String)
     * @see #GetRequestReference()
	 * @see #RT_ENDOFSTREAM
     * @since V2.5B7
     * @version 09/07/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2005/09/07  WHF	Created.
     *
     */
	public int GetRequestType()
	{ return requestType; }

	/**
	  * For a plugin-request, returns true if the request is for a list
	  *  of frames.  This form of request may be generated by
	  * {@link Sink#RequestFrame(ChannelMap)}.  The response to this request
	  *  should not yet exist; it should be considered to be for FUTURE data.
	  *  Therefore the time values can be ignored.
	  * <p>
     *
     * @author WHF
     *
     * @see #GetRequestStart()
     * @see #GetRequestDuration()
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  WHF	Created.
     * 02/19/2002  WHF  Fixed bug where we returned inverse of what was
     *   intended.
	 * 05/22/2002  WHF  Deprecated.
	 * 03/12/2007  WHF  Undeprecated.
	 * 03/13/2007  WHF  Returns false if the request was for registration.
     *
     */
	public boolean IsRequestFrames()
	{ return !pluginTimeMode && !"registration".equals(pluginTimeReference); }


	/**
	  * Yields the identifier which uniquely identifies this Request
	  * to the server.
	  * <p>
     *
     * @author WHF
     *
     * @return The request id, or null if this map is not the result of
     *	a fetch.
     * @see PlugIn#Flush(PlugInChannelMap,boolean)
     * @since V2.0
     * @version 04/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2002  WHF	Created.
     *
     */	
	public String GetRequestId() { return requestId; }
	
	/** 
	  * Returns the options that were set for this request.
	  * <p>
	  * @see Sink#Request(ChannelMap, double,double,String,RequestOptions)
	  * @since V21.B4
	  */
	public RequestOptions GetRequestOptions() { return requestOptions; }
	
	/**
	  * Extends {@link ChannelMap#toString()} with request information.
	  * @author WHF
	  * @since V2.6B5
	  * @version 2006/11/08
	  */
	public String toString()
	{
		String chStr = super.toString();
		
		chStr += "\n[id="+requestId+", start="+pluginStart
				+", duration="+pluginDuration
				+", reference="+pluginTimeReference
				+"]";
		
		return chStr;
	}

/////////////////// Package Utilities: ///////////////////////
	// Processes a plugin request:
	// 2005/09/07  WHF  Added requestType logic.
	final void processPlugInRequest(Object result) throws Exception
	{
		requestType = 0;	
		if (result!=null)
		{
			//EMF 9/2/05: check if EOS
			if (result instanceof EndOfStream) {
 				result=((Rmap)result).getChildAt(0);
				//do something about EOS
				requestType = RT_ENDOFSTREAM;
			}
			DataRequest req;
			boolean forRegistrationFlag;
			if (result instanceof com.rbnb.api.Ask)
			{ // wants registration
				req=(DataRequest) ((com.rbnb.api.Ask) result)
					.getAdditional().elementAt(0);
				forRegistrationFlag = true;
			}
			else {
				req=(DataRequest) result;
				forRegistrationFlag = false;
			}

			// Generate a Channel object in the map for each
			//  endpoint in the request:
			requestId=req.getName(); // Store Id
			req.setName(null); // Remove it so it doesn't show up
			// 11/14/2002  WHF  Should be handled by channelMap.processResult():
			processResult(req, false, true);
			req.setName(requestId);

			Rmap child=null;
			responseHeader.setName(req.getName());
			child=req.getChildAt(0);
			TimeRange tr=child.getTrange();

			if (tr!=null)
			{
				pluginTimeMode=true;
			}
			else 
			{
				tr=child.getFrange();
				pluginTimeMode=false;
			}

			if (tr==null) // registration request has no time
				pluginStart=pluginDuration=0;
			else
			{
				pluginStart=tr.getTime();
				pluginDuration=tr.getDuration();
			}

			// Put the start and duration on the source
			//  side of the map, so that it will be sent back
			//  to the server as is by default.
			PutTime(pluginStart, pluginDuration);

			if (requestType == 0) {  // not yet set
				if (req.getNrepetitions()==DataRequest.INFINITE) {
					if (req.getIncrement()==1.0)
						//pluginTimeReference="subscribe";
						requestType = RT_SUBSCRIBE;
					else
						//pluginTimeReference="monitor";
						requestType = RT_MONITOR;
				} else requestType = RT_REQUEST;
			}
			
			if (forRegistrationFlag) {
				pluginTimeReference="registration";
			} else
				switch (req.getReference()) {
				case DataRequest.ABSOLUTE:
				    // mjm 9/23/04, handle "next"
				    if( (req.getRelationship() == DataRequest.GREATER) || 
					(req.getRelationship() == DataRequest.GREATER_EQUAL)) 
					pluginTimeReference = "next";
				    // mjm 2/7/05, handle "prev"
				    else if(req.getRelationship() == DataRequest.LESS)
						// 2006/11/08  WHF  Previous is always LESS; absolute
						// is either EQUAL or LESS_EQUAL depending on 
						//  duration.
						//|| req.getRelationship() == DataRequest.LESS_EQUAL) 
					pluginTimeReference = "previous";
				    else
					pluginTimeReference = "absolute";
					break;
				case DataRequest.OLDEST:
					pluginTimeReference = "oldest";
					break;
				case DataRequest.NEWEST:
					pluginTimeReference = "newest";
					break;
				case DataRequest.AFTER:
					pluginTimeReference = "after";
					break;
				case DataRequest.MODIFIED:
					pluginTimeReference = "modified";
					break;
				default:
	//				throw new UnsupportedOperationException(
					throw new IllegalArgumentException(
						"Unsupported time reference.");
				}

			repeatInterval=req.getIncrement();
			repeatCount=req.getNrepetitions()>Integer.MAX_VALUE?
				Integer.MAX_VALUE:(int) req.getNrepetitions();
		} // end if result != null
		else {
			processResult(null,true, false); // store the null
			requestType = RT_TIMEOUT;
		}
	}

	/**
	  *  Produces a Rmap used to respond to a request given
	  *   to this plugin.
	  */
	Rmap produceOutput() throws Exception
	{
		Rmap responseParent;

		responseHeader.addChild(responseParent=new EndOfStream());

		responseParent.addChild(super.produceOutput());		

if (debugFlag) System.err.println(
	"PlugInChannelMap Produced: "+responseHeader);
		return responseHeader;
	}

    // mjm 9/14/04:  add a way to produce streaming output
	Rmap produceOutput(boolean dostream) throws Exception
	{
	    //System.err.println("produceOutput("+dostream+")");
		if(dostream) {
		    responseHeader.addChild(super.produceOutput());
		    return responseHeader;
		}
		else return produceOutput();
	}

	void clearData() throws Exception
	{
		super.clearData();
		requestId=null;
		pluginTimeReference=null;

		// Although undocumented, removeChildAt ignores
		//  removes greater than number of children:
		responseHeader.removeChildAt(1);
		responseHeader.removeChildAt(0);
	}

/////////////////// Private Data: ///////////////////////
	private double pluginStart, pluginDuration, repeatInterval=1.0;
	private boolean pluginTimeMode;
	private int repeatCount=1,
			requestType;

	private String pluginTimeReference,
		requestId;

	private final Rmap responseHeader=new Rmap();
	
	private final RequestOptions requestOptions=new RequestOptions();

} // end class PlugInChannelMap
