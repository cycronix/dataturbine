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

import java.util.Hashtable;

import com.rbnb.api.*;

/**
  * A simple data source for RBNB servers.
  * This class is designed to present a simple interface for sourcing data
  *  to RBNB servers, via the RMap API.
 * <p>
 * A data source client has the following tasks:
 * <ol>
 * <li>Create a {@link ChannelMap} object.</li>
 * <li>Define channels by name, via {@link ChannelMap#Add(java.lang.String)}.
	</li>
 * <li>Set a TimeStamp, using {@link ChannelMap#PutTime(double,double)} or 
 *	{@link ChannelMap#PutTimeAuto(java.lang.String)}.</li>
 * <li>Add data for each channel, through the various PutChannel methods,
 *  such as {@link ChannelMap#PutData(int,byte[],int)}.</li>
 * <li>{@link #Flush(ChannelMap)} data to RBNB server. </li>
 * </ol>
 * <p>Steps 2 - 4 may be repeated as desired.  Also note that the time
 *   may be set for the entire frame, that is only once per Flush() before 
 *   any calls to PutData(); for each channel, 
 *   that is once per PutData(); or for individual data blocks, where
 *   PutTime & PutData() are called multiple times per frame on the same 
 *   channel.
 * <p>
 *   Use {@link #Register(ChannelMap)} to publicize channels in advance of
 *   sending them to the RBNB server. 
 *<p>
 *   <i>Note:</i>  Multi-channel ChannelMaps
 *   must remain consistent (same number and names) from one flush to another
 *   for the life of the data source.  This is because consistent sets of
 *   channels are written to unique ring-buffers in the server, which for
 *   efficiency are restricted to contain a consistent set of channels across
 *   all frames stored in each such ring-buffer.
 *<p>
 *
 * @author WHF
 *
 * @since V2.0
 * @version 2004/01/08
 */

/*
 * Copyright 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/17/2001  WHF	Created.
 * 07/30/2001  WHF      Added new PutChannel method & support code.
 * 08/10/2001  WHF	Added DeepCVT support code.
 * 08/13/2001  WHF	Added appendData(), inheritTimes()
 * 08/16/2001  WHF	Added multilevel data optimization, AddChannel & 
 *			PutChannels by Handle.
 * 08/24/2001  WHF	Deprecated PutChannel(String,etc.) functions.  Added
 *			ClearChannels().
 * 08/31/2001  INB	Added new string handling to take advantage of the fact
 *			that the API does, in fact, properly handle strings so
 *			long as they appear one per data block.
 * 10/17/2001  INB	checkConsistency now takes a new argument that tells it
 *			whether the Rmap should have a name or not.  This is
 *			necessary when the Rmap hierarchy contains data at
 *			multiple levels.
 * 01/28/2002  WHF	Implemented new ChannelMap approach.
 * 08/07/2002  WHF	Added serialVersionUID member.
 * 01/14/2003  INB	Added setting of username.
 * 03/04/2003  WHF  Added check in Flush() to ensure flushed channel sets are
 *			consistent.
 * 2003/07/30  WHF  Added Delete() method.
 * 2004/01/08  WHF  Added ClearCache() and Detach() methods.
 * 2005/10/25  EMF  Added BytesTransferred method.
 */

public class Source extends Client
{
	static final long serialVersionUID = 3938978994160732623L;

	// Transient objects which have no persistence:
	private transient com.rbnb.api.Source source;

	private final static String	
					serverName="Server",
					delimStr=""+Rmap.PATHDELIMITER;

	/**
	  * Maps channel names to the number of channels in each set.
	  *
	  * <p>
	  * @since V2.0B10
	  */
	private final Hashtable groups=new Hashtable();

    /**
     * Default constructor.  Initializes default values of the parameters:
     * <ul><li><code>cacheSize</code> = 100 frames</li>
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
	public Source()
	{
		super(100,null,0);
	}


    /**
     * A convenience constructor which initializes values for the cache size,
     *  archive mode, and archive size.  Calls 
	 *  {@link Client#SetRingBuffer(int, String, int) } 
	 *  with the specified options.
     * <p>
     *
     * @author WHF
     *
     * @param cacheSize Size of the memory cache, in frames.
     * @param archiveMode The mode for the optional archive.  Should be one 
	 *   of the following: <ul>
     *  <li> "none"  - no Archive is made. </li>
     *  <li> "load"  - load an archive, but do not allow any further writing 
	 *   to it. </li>
     *  <li> "create" - create an archive. </li>
     *  <li> "append" - load an archive, but allow writing new data to it. </li>
     * </ul>
     * @param archiveSize The size of the desired archive, in frames.  Ignored 
	 *   except for "create" and "append".
     *
     * @see Client#CloseRBNBConnection()
     * @see Client#SetRingBuffer(int,String,int)
     *
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  WHF	Created.
     *
     */
	public Source(int cacheSize, String archiveMode, int archiveSize)
	{
		super(cacheSize,archiveMode,archiveSize);
	}


    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF 	Created.
     *
     */
	public long BytesTransferred() {
          if (source!=null) return source.bytesTransferred();
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
     *
     *
     */
	void doOpen(
		Server server,
		String clientName,
		String userName,
		String password)  throws Exception
	{
//		server=Server.newServerHandle(serverName,serverAddress);
		source=server.createSource(clientName);
/*		source.setCframes(getCacheSize());
		source.setAmode((byte)getArchiveMode());
		source.setAframes(getArchiveSize()); */
		prepareArchive(false); // don't reset
		if (userName != null) {
		    source.setUsername(new Username(userName,password));
		}
		source.start();
	}

	/**
	  * Sends the pending channels to the server, unsynchronized.  Calls 
	  *  {@link #Flush(ChannelMap,boolean) }
	  *  with a false value for the synchronization parameter.
	  * <p>
     *
     * @author WHF
     *
     * @return The number of channels flushed.
     * @exception SAPIException If there is an error while sending data to 
	 *  the server.
     * @see #Flush(ChannelMap,boolean)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     */
	public int Flush(ChannelMap ch) throws SAPIException
	{ return Flush(ch, false); }

	/**
	  * Sends the pending channels to the server, optionally waiting for
	  *  confirmation before returning.  The data and times in the provided 
	  *  {@link ChannelMap} are erased after they are sent, although the names
	  *  are preserved.
	  * <p>
     *
     * @author WHF
     *
     * @param doSynch If true, this Source will communicate with the server
     *   to verify that the communication was successful.
     * @return The number of channels flushed.
     * @exception SAPIException If there is an error while sending data to
	 *  the server.
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 08/10/2001  WHF  Added collapse method call on baseFrame.
     * 10/24/2001  WHF  Added boolean synch parameter, and added check for
     *  added channels to verify that data needs to be sent.
     * 10/24/2001  INB	Always synchronize even if no channels are sent and
     *			only increment the next count when channels are sent.
	 * 03/04/2003  WHF  Added check to ensure that channels are packaged
	 *  every time, just as they were the first time they were created.
	 *
     */
	public int Flush(ChannelMap ch, boolean doSynch) throws SAPIException
	{
		assertConnection();

//		int toFlush=ch.getChannelsPut();
//		timePerChannel=false;
		String[] list=ch.GetChannelList();
		int toFlush=list.length;

		try {
		Rmap output;

		// MJM 2/5/05:  This looks like it could take a lot of work to check...

		if (toFlush>0)
		{
			Integer sz=(Integer) groups.get(list[0]);
			if (sz==null) // first channel never before sent
			{ // check to be sure other channels have not been sent
				for (int ii=1; ii<list.length; ++ii)
					if (groups.containsKey(list[ii]))
						throw new IllegalArgumentException(
							"Channel sets must remain consistent"
							+" for every Flush.");
				// Only after checks do we add mappings:
				sz=new Integer(list.length);
				for (int ii=0; ii<list.length; ++ii)
					groups.put(list[ii],sz);
			}
			else // first channel was sent
			{ // check other channels are in same list
				boolean failure=sz.intValue()!=list.length;
				for (int ii=1; !failure&&ii<list.length; ++ii)
				{
					Integer toCheck=(Integer) groups.get(list[ii]);
					failure=toCheck==null||toCheck!=sz;
				}
				if (failure) throw new IllegalArgumentException(
					"Channel sets must remain consistent for every Flush.");
			}

			output=ch.produceOutput();
			output.collapse();
			source.addChild(output);
			ch.clearData();
			ch.incrementNext();
		}
		else
		{
			output=ch.getResponse();
			if (output==null) return 0;
			output.collapse();
			source.addChild(output);
			ch.clearData();
			// dont increment in this case
		}
//System.err.println("Source.Flush():\n"+output);
		if (doSynch) source.synchronizeWserver();
		} catch (IllegalArgumentException iae) { throw iae; }
		catch (Exception e) { throw new SAPIException(e); }
		return toFlush;
	}

	/**
	  * Tells the server which channels this <code>Source</code> will
	  *  generate. It also sends any data present in the <code>
	  *  ChannelMap</code> as time-independent meta-data.
	  * <p>
	  * <strong>NOTE:</strong> It is strongly recommended that the data placed
	  *  into the ChannelMap used with Register be placed with 
	  * {@link ChannelMap#PutUserInfo(int, String)}.  This allows the server
	  *  to combine this data with its server generated meta-data.  Otherwise
	  *  the server generated data is overridden.
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
     * 04/05/2002  WHF	Created.
     */
	public void Register(ChannelMap cm) throws SAPIException
	{
		super.doRegister(cm);
	}
	
	
	/**
	  * Deletes the maps specified in <i>toDelete</i>.  The results of the 
	  *  deletion, as determined by the server, are placed in <i>result</i>,
	  *  which is returned.
	  * <p>If result is null, a new channel map is created.
	  */
	  
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2003/07/30  WHF	Created.
     */
	public ChannelMap Delete(ChannelMap toDelete, ChannelMap result) 
			throws SAPIException
	{
		assertConnection();

		try {
		Rmap rmap=toDelete.produceRequest();
		// Add data markers to end points, to signify that they 
		//  represent channels that the plugin will answer to:
		Client.forEachEndpoint(rmap, addDataMarkerAction);

		// Remove any time information which may have snuck into the 
		//  registration map:
		Client.forEachNode(rmap, removeTimeAction);

		rmap.setName(null);
		
		rmap=source.deleteChannels(rmap);

		// Do this here, in case result == toDelete		
		if (result==null) result=new ChannelMap();
		else result.Clear();
		// keep data, no slash removal
		result.processResult(rmap, true, false); 
		return result;
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) { throw new SAPIException(e); } 
	}
	
	/**
	  * Calls {@link #Delete(ChannelMap, ChannelMap)} with <b>null</b>
	  *  for the <i>result</i> parameter.
	*/
	public ChannelMap Delete(ChannelMap toDelete) throws SAPIException
	{
		return Delete(toDelete, null);
	}
	
	/**
	  * Flushes any data currently in the Ring Buffer cache into the
	  *  disk archive.  If there is no archive, the data is discarded.
	  */
	public void ClearCache() throws SAPIException
	{
		try {
			source.clearCache();
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) { throw new SAPIException(e); }
	}
	
	
	/**
	* Close the connection with the server, but retain the server-side
	* handler and its ring buffer(s) such that sink clients can continue to
	* access previously written data.
	* <p>If you wish to close the connection and prevent access to the data
	*  at the server, use {@link Client#CloseRBNBConnection()}.
	* <p>A new client can reattach to the detached server-side handler by
	* using the following calls:
	* <p><code>Source src = new Source(cacheSize,"append",archiveSize)</code>
	* <br><code>src.OpenRBNBConnection(rbnbAddress,clientName)</code>
	* <p>Note: while it is possible to <code>Detach</code> from a
	* <code>Source</code> that does not have an archive, it is not possible
	* at this time to reattach to it.
	* <p>
	* 
     *
     * @author WHF
     *
     * @see Client#OpenRBNBConnection(String,String)
	 * @see Client#CloseRBNBConnection()
     * @since V2.0
     * @version 2004/02/16
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2004/02/16  INB	Modified documentation to provide more detail.
     * 2004/01/08  WHF	Created.
     *
     */
	public final void Detach()
	{
		_close(true, true);	
	}
	

////////////////////////////////////////////////////////////////////////////
////////////////////// Package private functions from client: //////////////
	com.rbnb.api.Client getClient() { return source; }

	final void clearData()
	{
		source=null;
		groups.clear();
	}

////////////////////////////////////////////////////////////////////////////
////////////////////// Private utilities: //////////////	

} // end class Source



