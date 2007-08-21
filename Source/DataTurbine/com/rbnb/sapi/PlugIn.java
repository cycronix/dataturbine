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
 * A simple PlugIn for RBNB servers.
 *
 * <p><strong>NOTE:</strong>
  *  it is not recommended that this class be used directly.  Instead consider
 *  subclassing {@link com.rbnb.plugins.PlugInTemplate} for a more streamlined
 *  plug-in development.
 * <p>This class is designed to present a simple interface for generating
 *   data to match requests.
 * <p>
 * A plug-in client has the following tasks: 
 * <ol>
 * <li>Build the list of channels to which this <code>PlugIn</code> responds
 *   with {@link ChannelMap#Add(String)} and {@link #Register(ChannelMap)}.
 *   This step may be omitted, in which case all channels which request
 *   from this PlugIn's name will be forwarded by the server.</li>
 * <li>{@link #Fetch(long,PlugInChannelMap)} requests from the RBNB server.
 *   </li>
 * <li>The {@link PlugInChannelMap} will be filled with the channels requested
 *   by the client, as well as the request mode and time range.  These
 *   can be determined using {@link PlugInChannelMap#GetRequestStart()}, 
 *  {@link PlugInChannelMap#GetRequestDuration()}, and {@link 
 *  PlugInChannelMap#GetRequestReference()}.  This last
 *  method may return "regsitration", in which case the registered channels
 *  should be returned, with any matching meta-data.
 *  <li>By default, 
 *  the time-range requested will be used for the output, although this may
 *  be overridden by <code>PutTime()</code>. Push data into the map to 
 *  fullfill the 
 *  request using the <code>PutData()</code> methods.</li>
 * <li> When all channels have been set, {@link #Flush(PlugInChannelMap) } the 
 *  response to the server. </li>
 * <li>Reat steps 2 through 4 indefinitely.  For some applications, it
 *   will be necessary to spawn a new thread in step 2 to handle each
 *  new request.  It should <strong>NOT</strong> be necessary to <code>
 *	synchronize</code> on the <code>PlugIn</code> object, as it can <code>
 *  Fetch</code> and <code>Flush</code> simultaneously; however, a new
 *  <code>PlugInChannelMap</code> object should be created before returning
 *  to step 2.</li>
 * </ol>
 * <p>
 * @author WHF
 *
 * @since V2.0
 * @version 2006/11/09
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/30/2002  WHF	Created.
 * 11/15/2002  WHF	General cleanup, moved some code into the ChannelMap 
 *						classes.
 * 01/14/2003  INB	Added setting of username.
 * 09/14/2004  MJM	Changed Flush(map,doSync) to Flush(map,doStream)
 * 10/25/2005  EMF      Added BytesTransferred method.
 * 2006/11/08  WHF  Deprecated Flush(map, boolean).
 */

public class PlugIn extends Client
{
    /**
     * Default constructor.
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  WHF	Created.
     *
     */	public PlugIn()
	{
		super(1,null,0);
	}

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF	Created
     *
     */
    public long BytesTransferred() {
      if (plugin!=null) return plugin.bytesTransferred();
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
		plugin=server.createPlugIn(clientName);

		// PlugIns don't have archives:
//		plugin.setAmode((byte)getArchiveMode());
//		plugin.setAframes(getArchiveSize());
		if (userName != null) {
		    plugin.setUsername(new Username(userName,password));
		}
		plugin.start();
	}

	/**
	  * Queries the server to see if any requests have been made of this
	  *  PlugIn.  If so, the channel names will be placed in the provided
	  *  (or a newly created) PlugInChannelMap object.
	  * <p> <code>PutTime()</code> is called internally on the ChannelMap
	  * in this function with the 
	  * start and duration specified in the request.  This may be
	  *  overridden with successive calls to <code>PutTime()</code>.
	  * <p>
     *
     * @author WHF
     *
     * @param picm The ChannelMap object which is filled with the data received
     *    from the server.  If this parameter is null, a new ChannelMap is
     *    created.
     * @param blockTimeout The amount of time (ms) to wait for data to become
     *  available.  Use 0 for no delay or any negative number for an 
     *  infinite delay.
     * @return The <code>PlugInChannelMap</code> provided, or a newly created
     *   one.
     *    
     * @exception SAPIException If there are problems obtaining data from
     *  the server.
     * @see PlugInChannelMap
     * @see ChannelMap#PutTime(double,double)
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  WHF	Created.
     * 02/05/2002  WHF  Gave up on getting extractNames to work.  Looks for
     *     endpoints instead.
     */
	public final PlugInChannelMap Fetch(
						long blockTimeout,
						PlugInChannelMap picm)
		throws SAPIException
	{		
		if (picm==null) picm=new PlugInChannelMap();
		else picm.Clear();

		try {
//		DataRequest request;
		Object result=plugin.fetch(blockTimeout<0
			?com.rbnb.api.Sink.FOREVER:blockTimeout);
/* 11/14/2002  WHF  Moved to PlugInChannelMap.processRequest():
		if (result!=null)
		{
			picm.setIfFetchTimedOut(false);

//System.err.println("Got request: "+result);
			if (result instanceof com.rbnb.api.Ask)
			{ // wants registration
				request=(DataRequest) ((com.rbnb.api.Ask) result)
					.getAdditional().elementAt(0);
				picm.raiseForRegistrationFlag();
			}
			else request=(DataRequest) result;

			picm.processRequest(request);
		}
		else picm.setIfFetchTimedOut(true);
*/
		plugin.fillRequestOptions(picm.GetRequestOptions());
		picm.processPlugInRequest(result);
		return picm;

		} catch (java.lang.RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}

	/**
	  * Queries the server to see if any requests have been made of this
	  *  PlugIn.  The channel names are placed in a newly created
	  *  <code>PlugInChannelMap</code> object.
	  * <p>Calls <code>Fetch(long,null)</code>.
	  * <p>
	  * @see #Fetch(long, PlugInChannelMap)
	  */
	public final PlugInChannelMap Fetch(long blockTimeout)
		throws SAPIException
	{ return Fetch(blockTimeout,null); }

	/**
	  * Sends the pending channels to the server, optionally waiting for
	  *  confirmation before returning.
	  * <p><strong>Note:</strong> This version replaces the old <code>doSynch</code>
	  *  flag with the <code>doStream</code> flag.  Synchronization was not recommended
	  *  for plug-ins, so this new function was substituted in its place.
	  *  Behavior for a false value of this flag is the same as before.
	  * <p>
	  *
	  * @deprecated  PlugIns no longer receive streaming requests directly,
	  *   so it is no longer necessary to call this method with true.
     *
     * @author WHF
     *
     * @param ch The plugin channelmap to send
     * @param doStream If true, does not wrap response with EOS (end of stream), i.e.
     *   multi-part streaming response is being provided.
     * @return The number of channels that PutData was called on, between
     *   this Flush() and the previous one.
     * @exception SAPIException If there is an error while sending data to 
     *   the server.
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  WHF	Created.
     * 02/07/2002  WHF  Flush regardless of data put into channels.  This
     *   allows the PlugIn to respond to requests which do not match any
     *   data.
     * 09/14/2004  MJM  doSynch changed to doStream
	 * 2006/11/08  WHF  Deprecated; throws IllegalArgumentException if 
	 *   doStream is true.
     */
	public int Flush(PlugInChannelMap ch, boolean doStream) 
		throws SAPIException
	{
		if (doStream) throw new IllegalArgumentException(
			"Streaming not supported for PlugIns."
		);
		int toFlush=ch.getChannelsPut();
		try {
		    // mjm 9/14/04:  streaming response set doSynch=true (hijack flag)
		    Rmap response=ch.produceOutput(doStream);
		    // Rmap response=ch.produceOutput();
		    plugin.addChild(response);
		    ch.clearData();
		    ch.incrementNext();
		    
		    // following line commented out to hijack dosync to be dostream
		    //if (doSynch) plugin.synchronizeWserver();
		} catch (Exception e) { throw new SAPIException(e); }
		return toFlush;
	}

	/**
	  * Sends the pending channels to the server, as single-response (non-streaming).  Calls 
	  *  {@link #Flush(PlugInChannelMap,boolean) }
	  *  with a false value for the streaming parameter.
	  * <p>
     *
     * @author WHF
     *
     * @return The number of channels flushed.
     * @exception SAPIException If there is an error while sending data to the
     *  server.
     * @see #Flush(PlugInChannelMap,boolean)
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  WHF	Created.
     */
	public int Flush(PlugInChannelMap ch) throws SAPIException
	{ return Flush(ch, false); }

	/**
	  * Tells the server which channels this <code>PlugIn</code> answers
	  *  to.  If this method is omitted, the server will forward any 
	  *  channel that identifies this PlugIn as its source.
	  * <p>For example, for a PlugIn called "MyPlugIn", if Register 
	  *  is called with a channel "MyChannel", only the channel
	  *  "MyPlugIn/MyChannel" will be passed through.  If Register is
	  *  not called, any channel that begins with "MyPlugIn/" will be
	  *  forwarded to the PlugIn.
	  * <p>Any data which is present in the <code>ChannelMap</code>
	  *   will be passed to the server as time-independent meta-data.
	  * <p>
	  * 
     *
     * @author WHF
     *
     * @exception SAPIException If there is an error while sending the map to
     *  the server.
     * @since V2.0
     * @version 01/31/2002
     */

    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2002  WHF	Created.
     * 04/05/2002  WHF  Moved registration code to Client.
     */
	public void Register(ChannelMap cm) throws SAPIException
	{
		super.doRegister(cm);
	}

	/**
	  * This operation is not supported in PlugIns.
	  * <p>
	  * @author WHF
	  * @exception UnsupportedOperationException Not supported.
	  */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  WHF	Created.
     *
     */
	public void SetRingBuffer(int cache, String mode, int archive)
	{
//		throw new UnsupportedOperationException(
		throw new RuntimeException(
			"PlugIns do not support archiving.");
	}


////////////////////////////////////////////////////////////////////////////
////////////////////// Package private functions from client: //////////////
	com.rbnb.api.Client getClient() { return plugin; }
	final void clearData()
	{
		plugin=null;
	}

////////////////////////////////////////////////////////////////////////////
////////////////////// Private data: //////////////
	private com.rbnb.api.PlugIn plugin;

////////////////////////////////////////////////////////////////////////////
////////////////////// Private utilities: //////////////
}
