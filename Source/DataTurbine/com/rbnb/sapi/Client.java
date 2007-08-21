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

//import java.util.ArrayList;
import java.util.Vector;

import com.rbnb.api.*;

/**
  * Base class of all simple clients to RBNB servers.  This class encapsulates 
  *  functionality common to all clients.  It is not intended for end-user
  *  subclassing.
  *  
  * <p>
 * @author WHF
 *
 * @since V2.0
 * @version 09/28/2004
*/

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/17/2001  WHF	Created.
 * 05/24/2001  WHF      Added TYPE_UNKNOWN constant.
 * 06/05/2001  WHF	Added TYPE_INT8 constant.
 * 01/18/2002  WHF	Add GetChannelList(void) and GetList() methods.
 * 01/28/2002  WHF	Switched to new ChannelMap implementation.  Added 
 *	SetRingBuffer().
 * 02/08/2002  WHF	Made public to reduce repeated method count in 
 *				documentation of subclasses.
 * 02/12/2002  WHF	Changed implementation of SetRingBuffer() to call
 *	com.rbnb.api.Source.reset().
 * 03/05/2002  WHF	Made object serializable for storage in ActiveX.
 * 04/10/2002  WHF	Deprecated GetChannelList(), removed GetList(),
 *	GetRegistration().
 * 06/12/2002  WHF  Added forEachNodeToDepth() static utility method.
 * 09/24/2002  WHF  Removed 1.2 dependencies.
 * 2004/01/08  WHF  Deprecated CloseRBNBConnection(boolean, boolean).
 * 2004/09/28  JPW  To allow code to compile under J#, moved the definition
 *			of dataMarker from being defined as a class constant
 *			to a local variable in the one method where it is
 *			actually used.
 * 2005/10/25  EMF  Added BytesTransferred method, to support client metrics.
 */

public abstract class Client implements java.io.Serializable
{
	// Default constructor: 
	Client(int cache, String mode, int archive) 
	{
		try {
		if (!(this instanceof PlugIn))
			SetRingBuffer(cache,mode,archive);
		} //catch (UnsupportedOperationException uoe)
		// { } // ah, you must be a plugin.  Ignore.
		catch (SAPIException se)
		{} // can only happen when connected, which we aren't.
	}

    /**
      *  Reports the total number of bytes read and written by this Client. <p>
      * <p>
      *
      * @author EMF
      *
      * @return The total number of bytes read and written.
      * @since V2.6
      * @version 10/25/2005
      */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF	Created.
     *
     */
	public abstract long BytesTransferred();

	/**
	 * Allows the cache size, archive mode, and archive size to be 
	 *  specified outside of the constructor.  This is useful for the 
	 *  following purposes:
	 *  <ul>
	 *  <li>For JavaBean / ActiveX users, who cannot call the constructors
	 *  which take arguments.</li>
	 *  <li>To change the cache and archive settings while connected to
	 *   the server.</li>
	 *  <li>To delete the cache and archive at the server.</li>
	 *  </ul>
	 * <p><STRONG>Note:</STRONG> This method will delete any data which
	 *  lies in the current cache or archive, and replace it with a new
	 *  cache or archive of the new size.
	 * <p>
	 * @exception SAPIException If connected to a server 
	 *  when called, and the reset fails.
	 * @exception IllegalArgumentException If cache is less than one;
	 *  mode is not one of "none", "load", "create", "append", or
	 *  "delete"; or if archive size is less than zero (for mode other
	 *  than null or "none".
	*/
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  WHF	Created.
     * 02/12/2002  WHF  Now supports changing RBO while connected.
	 * 03/03/2003  WHF  Exception for illegal mode indicates what mode was 
	 *						attempted.
     *
     */
	public void SetRingBuffer(int cache, String mode, int archive)
		throws SAPIException
	{
/*		Will work now:
		if (getClient()!=null) 
			throw new IllegalStateException(
				"The ring buffer cannot be altered while"
				+" connected to a server.");
*/
		if (cache<1)
			throw new IllegalArgumentException(
				"A value less than 1 is not permitted for "
				+"the cache.");
		cacheSize=cache;

		boolean ignoreSize=true;

		if (mode!=null)
		{ 
			mode=mode.toLowerCase();
			if (mode.equals("none")) 
				archiveMode=com.rbnb.api.Source.ACCESS_NONE;
			else if (mode.equals("load")) 
				archiveMode=com.rbnb.api.Source.ACCESS_LOAD;
			else if (mode.equals("create")) 
			{ 
				archiveMode=com.rbnb.api.Source.ACCESS_CREATE;
				ignoreSize=false;
			}
			else if (mode.equals("append")) 
			{
				archiveMode=com.rbnb.api.Source.ACCESS_APPEND;
				ignoreSize=false;
			}
			else if (mode.equals("delete"))  // TODO
				archiveMode=com.rbnb.api.Source.ACCESS_NONE;
			else throw new IllegalArgumentException(
				"Archive mode \""+mode+"\" not recognized.");
		}
		else archiveMode=com.rbnb.api.Source.ACCESS_NONE;

		if (!ignoreSize)
		{
			if (archive<0)
				throw new IllegalArgumentException(
					"A negative value is not permitted for the archive.");
			archiveSize=archive;
		}
		com.rbnb.api.Client c=getClient();
		if (c!=null) // do reset
			prepareArchive(true);
	}


	/**
	  *  Opens a connection to the server, using default parameters. <p>
	  * <ul>
	  * <li><code>serverAddress</code> = "localhost:3333"</li>
	  * <li><code>clientName</code> = "MyClient"</li>
	  * <li><code>userName</code> = ""</li>
	  * <li><code>password</code> = ""</li>
	  * </ul>
	  * <p>
     *
     * @author WHF
     *
     * @return None.
     * @exception SAPIException If there is a problem connecting to the server.
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
	public final void OpenRBNBConnection()  throws SAPIException
	{
		OpenRBNBConnection(serverAddressDefault,clientNameDefault,
			userNameDefault,passwordDefault);
	}

	/**
	  *  Opens a connection to the server, using some default parameters. <p>
	  * <ul>
	  * <li><code>userName</code> = ""</li>
	  * <li><code>password</code> = ""</li>
	  * </ul>
	  * <p>
     *
     * @author WHF
     *
     * @return None.
     * @exception SAPIException If there is a problem connecting to the server.
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
	public final void OpenRBNBConnection(
		String serverAddress,
		String clientName) throws SAPIException
	{
		OpenRBNBConnection(serverAddress,clientName,
			userNameDefault,passwordDefault);
	}


	/**
	  *  Opens a connection to the server.
	  * <p>
     *
     * @author WHF
     *
     * @param serverAddress The address of the server to connect to.
     * @param clientName The name used to identify this client on the server.
     * @param userName The name used to log onto the server, if secure.
     *                 <p>The username is transmitted in plain-text.  It is
     *		       not actually used to restrict access.
     * @param password The password associated with the user name.
     *                 <p>This value is transmitted in plain-text.  If set,
     *		       only other clients with the same password will be
     *		       allowed to access the data.
     *
     * @author WHF
     *
     * @exception SAPIException If there are problems connecting to the server.
     * @exception IllegalArgumentException If <code>clientName</code> is
     *  null, zero-length, or contains slashes.
     * @since V2.0
     * @version 04/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 04/08/2002  WHF  Added check on client name for slashes.
	 * 09/16/2002  WHF  Fixed serious bug, where code was not exception safe
	 *						in the advent of a connection failure.
     *
     */
	public final void OpenRBNBConnection(
		String serverAddress,
		String clientName,
		String userName,
		String password)  throws SAPIException
	{ 
		try {
		server=Server.newServerHandle(null /*serverName*/,
			serverAddress);
		// Check for slash in client name:
		if (clientName==null||clientName.length()==0
			||clientName.indexOf('/')!=-1)
			throw new IllegalArgumentException(
				"The client name must be at least one"
				+" character and may not contain slashes.");

		doOpen(server, clientName, userName, password);
		} catch (Exception e) 
		{ 
			server=null; // WHF 9/16/02  Must reset this handle!
			clearData();	// reset subclass's handles
			throw new SAPIException(e); 
		}	
	}

/**
	* Close the connection with the server, and free up associated resources.
	*  Any cache on the server will be removed, but any connected archive will
	*  be preserved on disk, although the data is no longer available through
	*  the server.
	* <p>If you have a Source and you wish to close the connection, but 
	*  allow the data to still be
	*  accessed, use {@link Source#Detach()}.
     *
     * @author WHF
     *
	 * @see Source#Detach()
     * @since V2.0
     * @version 2004/01/08
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 12/11/2001  WHF  Removed SAPIException throw.  What is the user going
     *     to do with this exception, anyway?  Close is usually placed after
     *     the catch block used for the other SAPI calls.
     *
     */
	public final void CloseRBNBConnection() //throws SAPIException
	{
		//CloseRBNBConnection(false,true);
		_close(false, true);
	}
	
/**
	* Close the connection with the server, and free up associated resources.
	* <p>
	*  The two parameters keepCache and keepArchive determine the state of 
	*  data on the server from this source, after the source has disconnected.
	*   If <code>keepCache</code> is true, the memory cache at the server will
	*  be preserved, so that Sinks can request the data even after the Source
	*  has closed.  If <code>keepArchive</code> is true, the disk archive at 
	*  the server will be preserved.  Otherwise it will be deleted from the 
	*  disk.
	* <p>
	*  In order to load a stored archive, a Source with the same name as the
	*   Source which created the archive must open a connection with the 
	*   archive option set to "load".  See {@link Source#Source(int,
	*	String,int)} and {@link #SetRingBuffer(int,String,int)}.
	* <p>
	* Currently the only implemented options are <code>keepCache = false
	*  </code>, and <code>keepArchive = true</code>.  That is to say, 
	*  regardless of the options passed to this function, it is impossible
	*  to disconnect a source from the server and leave the cache in the
	*  ring buffer, nor is it possible to delete an archive from the server's
	*  disk.  These features will be implemented in a future release.
	* <p>
     *
     * @author WHF
	 *
	 * @deprecated Flags were counter-intuitive and allowed certain impossible
	 *  requests, such as a deleted archive with a kept cache.  Use {@link
	 *  #CloseRBNBConnection() } and {@link Source#Detach()} instead.
     *
     * @param keepCache If true, the memory cache at the server
	 *    will be preserved.
     * @param keepArchive If true, the disk archive at the server will 
	 *    be preserved. Otherwise it will be deleted from the disk.
     * @since V2.0
     * @version 2004/01/08
     */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 12/11/2001  WHF  Removed SAPIException signature.
     * 10/08/2002  INB  Implemented the keep flags.
	 * 2004/01/08  WHF  Deprecated; code moved into _close().
     *
     */
	public final void CloseRBNBConnection(boolean keepCache,
		boolean keepArchive) //throws SAPIException
	{
		_close(keepCache, keepArchive);
	}

   /**
	* Obtains the list of channels available at the server.
	* Requires a live connection to the server and a valid match string.
	* <p>
	* The wildcard for all channels is "...".  The asterisk acts
     *  as it does in path names (i.e. not recursively).  
	 *  Therefore, to access all channels
     *  of a depth of three on a server you would use: /ServerName/ * / * / *
     *
     * @author WHF
     *
     * @param matchStr A string against which the channels are 
	 *  compared to determine if they belong.
     * @return An array of currently available channels.
     * @exception SAPIException If not connected to a server or there 
	 *  is a problem with the connection.
     * @since V2.0
     * @version 05/17/2001
     * @deprecated Please use {@link Sink#RequestRegistration(
     *	ChannelMap)} instead.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 11/05/2001  INB  Use Rmap.createFromName to build the request
     *			<code>Rmap</code>.
     * 01/11/2002  WHF  Calls GetList().
     *
     */
	public final String[] GetChannelList(String matchStr) 
		throws SAPIException
	{
		if (server==null)
			throw new SAPIException("Not connected to a server!");

		try {
		return getClient().getRegistered(
			Rmap.createFromName(matchStr)).extractNames();
		} 
		catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Gets all available channels.
	  * <p>
	  * @version 01/18/2002
	  * @deprecated Please use {@link Sink#RequestRegistration(
	  *	ChannelMap)} instead.
	  */

	public final String [] GetChannelList()
		throws SAPIException
	{
		return GetChannelList("...");
	}

	/**
	  * Returns the name of the server to which this client is connected.
	  *  Note that this involves a round-trip to the server to verify the
	  *  connection.
	  * <p>
     *
     * @author WHF
     *
     * @see #OpenRBNBConnection(String, String, String, String)
     * @since V2.0
     * @version 02/05/2002
     * @exception IllegalStateException If we are not currently connected
     *   to a server.
     */
	public final String GetServerName() // throws SAPIException
	{
		if (server==null) throw new IllegalStateException(
			"Not connected to a server.");
		try {
		return server.getFullName();
		} catch (Exception e) //{ throw new SAPIException(e); }
		// extremely unlikely case, particularly since no round-trip is used:
		{ throw new RuntimeException("Error finding server name: "+e); }
	}
	
	/**
	  * Returns true if the connection to the server is still valid; false, 
	  *  if the connection has been severed.
	  */
	public final boolean VerifyConnection()
	{
		com.rbnb.api.Client c=getClient();
		if (c==null) return false;
		try {
			return c.isRunning();
		} catch (Exception e) { }
		return false;
	}

	/**
	  * Returns the name of this client. &nbsp;This may not be identical
	  *  to the name given to OpenRBNBClient(), as the server will append
	  *  additional identifiers of the form ".n", where n is the number
	  *  of clients requesting that name minus one, in the case of 
	  *  duplicate client names.
	  * <p>
     *
     * @author WHF
     *
     * @see #OpenRBNBConnection(String, String, String, String)
     * @since V2.0
     * @version 02/05/2002
     * @exception IllegalStateException If we are not currently connected
     *   to a server.
     */
	public final String GetClientName()
	{
		com.rbnb.api.Client c=getClient();
		if (c==null) throw new IllegalStateException(
			"Not connected to a server.");
		return c.getName();
	}
	
	/**
	  * Returns the number of cache frames used.
	 *
	 *<p>
     * @author WHF
     *
     * @since V2.6B5
     * @version 11/13/2006
	  */
	public final int GetCacheSize()
	{
		return cacheSize;
	}
	
	/**
	 * Returns the number of archive frames used.
	 *
	 *<p>
     * @author WHF
     *
     * @since V2.6B5
     * @version 11/13/2006
	  */
	public final int GetArchiveSize()
	{
		return archiveSize;
	}
	
	/**
	 * Returns the archive mode used.
	 *
	 *<p>
     * @author WHF
     *
     * @since V2.6B5
     * @version 11/13/2006
	  */
	public final String GetArchiveMode()
	{
		String mode;
				
		switch (archiveMode) {
			case com.rbnb.api.Source.ACCESS_LOAD:
			mode = "load";
			break;
			
			case com.rbnb.api.Source.ACCESS_CREATE:
			mode = "create";
			break;
			
			case com.rbnb.api.Source.ACCESS_APPEND:
			mode = "append";
			break;
			
			default:
			mode = "none";
			break;			
		}
		
		return mode;
	}

////////////////////////////////////////////////////////////////////////////
/////////////////////////// Subclass Overrides: //////////////////////////
	abstract com.rbnb.api.Client getClient();
	abstract void clearData();
	abstract void doOpen(Server server, String client, String user,
		String password) throws Exception;


////////////////////////////////////////////////////////////////////////////
////////////////////// Package private utilities: //////////////

	void prepareArchive(boolean doReset) throws SAPIException
	{
		com.rbnb.api.Source source=(com.rbnb.api.Source) getClient();

		try {
		source.setCframes(cacheSize);
		source.setAmode((byte)archiveMode);
		source.setAframes(archiveSize);
		if (doReset)
			source.reset();	
		} catch (Exception e) { throw new SAPIException(e); }
	}
	
	final void doRegister(ChannelMap cm) throws SAPIException
	{
		assertConnection();
		
		try {
		    // JPW 03/06/2007: Replace the guts of this method with a call
		    //                 to the new method, createRegistrionRmap().
		    Rmap toRegister = createRegistrationRmap(cm);
		    //System.err.println("Registering: "+toRegister);
		    if (getClient() instanceof com.rbnb.api.PlugIn) {
			((com.rbnb.api.PlugIn) getClient()).reRegister(toRegister);
		    } else {
			((com.rbnb.api.Source) getClient()).register(toRegister);
		    }
		} catch (Exception e) {
		    throw new SAPIException(e);
		}
	}
	
	final void assertConnection() throws SAPIException
	{
		if (getClient()==null)
			throw new SAPIException("This operation requires a connection.");
	}
	
	/**
	 * Produce a full RBNB API Rmap object from the given SAPI ChannelMap.
	 * @exception Exception thrown on error.
	*/
	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 03/06/2007  JPW	Created out of the guts of doRegister().
	 *			This method was originally created because I thought I might call it from
	 *			com.rbnb.api.MirrorController.getUserInfo(), but I don't.
	 */
	
	private static Rmap createRegistrationRmap(ChannelMap cm) throws Exception {
		
		/*
		 * 11/13/2002  WHF  this functionality duplicated by ChannelMap.produceRequest():
		 Rmap toRegister=(Rmap) cm.getDataRequest().clone(),
		      metaData=cm.produceOutput();
		 metaData.collapse();
		 cm.clearData();
		 *
		 */
		
		// JPW 02/28/2007: Add new boolean argument to ChannelMap.produceRequest();
		//                 "false" argument will prevent an exception being thrown
		//                 if there is a mix of channels with and without data - which
		//                 is OK when registering channels (in registration, we can
		//                 have a mix of channels with and without data (User Info)).
		Rmap toRegister=cm.produceRequest(false);
		
		// The Rmap's mergeWith doesn't do what we need it to do here:
		// toRegister = toRegister.mergeWith(metaData);
		// System.err.println(toRegister);
		
		// Add data markers to end points, to signify that they 
		//  represent channels that the plugin will answer to:
		forEachEndpoint(toRegister, addDataMarkerAction);
		
		// Remove any time information which may have snuck into the
		// registration map:
		// forEachNode(metaData, removeTimeAction);
		forEachNode(toRegister, removeTimeAction);
		
		//System.err.println(metaData);
		// Meta data should be a subset of the toRegister channels
		//  (produced with ChannelMap.Add()).
		//  Therefore, we need to merge the meta-data with the added
		//  channels.
		
		/*
		 * 11/14/2002  WHF  None of this should be necessary:
		 *
		String [] metaChan=metaData.extractNames();
		for (int ii=0; ii<metaChan.length; ++ii)
		{
			Rmap redundant=toRegister.findDescendant(metaChan[ii],false);
			//System.err.println(metaChan[ii]+"\n"+redundant);
			redundant.getParent().removeChild(redundant);
		} // if we get a null pointer exception here, something weird is
		//  happening either with findDescendant or the state of the 
		//  channelmap.
		
		// In case any null hierarchies are left over from deleting 
		//  redundancies above:
		toRegister.collapse();
		
		toRegister.addChild(metaData);
		*
		*/
		
		toRegister.setName(null);
		
		return toRegister;
	}

	/**
	  * Implementation of CloseRBNBConnection() and Detach().
	  *
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2004/01/08  WHF	Code moved from CloseRBNBConnection(boolean, boolean).
	 */
	final void _close(
			boolean keepCache,
			boolean keepArchive)
	{
		com.rbnb.api.Client c=getClient();
		if (c instanceof com.rbnb.api.Source) {
		    com.rbnb.api.Source s = (com.rbnb.api.Source) c;
		    s.setAkeep(keepArchive);
		    s.setCkeep(keepCache);
		}
		server=null;
		clearData();
		if (c!=null)
			try {
			c.stop();
			} catch (Exception e) 
		{ /* throw new SAPIException(e); */ }
	}
	
	final Server getServer() { return server; }

	/**
	  * Intended for use with Control clients.
	  */
/*	final void terminateLocalServer()
	{
		server.stop();
		server = null;
		clearData();
	} */
////////////////////////////////////////////////////////////////////////////
////////////////////// Static Utility Methods: /////////////////////////////
	/**
	  * Computes the value of the specified <code>Action</code> at
	  *  every end-point in the Rmap hierarchy.  An end-point is 
	  *  defined as a Rmap with no children.
	  * <p><strong>Note:</strong> The Action should neither add nor delete
	  *  children.
	  */
	static void forEachEndpoint(Rmap r, Action a) throws Exception
	{
		int n=r.getNchildren();
		if (n==0)
			a.doAction(r);
		else for (int ii=0; ii<n; ++ii)
			forEachEndpoint(r.getChildAt(ii),a);
	}

	/**
	  * Performs the specified <code>action</code> at every node in 
	  *  the hierarchy.
	  * <p><strong>Note:</strong> The Action should neither add nor delete
	  *  children.
	  */
	static void forEachNode(Rmap r, Action a) throws Exception
	{
		int n=r.getNchildren();
		a.doAction(r);
		for (int ii=0; ii<n; ++ii)
			forEachNode(r.getChildAt(ii),a);		
	}

	/**
	  * Performs the specified <code>action</code> at the specified node
	  * and its children, up to the specified depth.
	  * <p><strong>Note:</strong> The Action should neither add nor delete
	  *  children.
	  */
	static void forEachNodeToDepth(Rmap r, Action a, int depth) 
		throws Exception
	{
		a.doAction(r);
		if (depth>0)
		{
			int n=r.getNchildren();
			for (int ii=0; ii<n; ++ii)
				forEachNodeToDepth(r.getChildAt(ii),a,depth-1);
		}
	}

////////////////////////////////////////////////////////////////////////////
////////////////////// Static Inner Classes: //////////////

	/**
	  * Class to support GetList().
	*/
	static class GetListAction implements Action
	{		
		//private ArrayList myList=new ArrayList();
		private Vector myList=new Vector();
		public void doAction(Object o) throws Exception
		{
			Rmap r=(Rmap) o;
			if (r.getName()!=null&&!".".equals(r.getName())) 
				// verify has name.  '.' case will return getFullName() 
				//  of null.
				myList.addElement(r.getFullName());
		}
		
		public String[] getNames()
		{ 
			String[] names=new String[myList.size()];
			myList.copyInto(names);
			return names;
			//{ return (String[]) myList.toArray(new String[myList.size()]); }
		}
	}
	
	/**
	  * Similar to GetListAction, but looks upward to find the name of the
	  *  channel.
	  */
	static class GetNamedListAction extends GetListAction
	{
		public void doAction(Object o) throws Exception
		{
			Rmap r=(Rmap) o;
			while (r.getName()==null || ".".equals(r.getName())) {
				r=r.getParent();
				if (r==null) return;
			}
			super.doAction(r);
		}
	}
			
/*
	static class GetServersAction implements Action
	{		
//		private ArrayList myList=new ArrayList();
		private Vector myList=new Vector();
		public void doAction(Object o) throws Exception
		{
			Rmap r=(Rmap) o;
			if ((r instanceof com.rbnb.api.Server)
					&&r.getName()!=null&&!".".equals(r.getName())) 
				// verify has name.  '.' case will return getFullName() 
				//  of null.
				myList.addElement(r.getFullName());
		}
		
		public String[] getNames()
		{ 
			String[] names=new String[myList.size()];
			myList.copyInto(names);
			return names;
			//{ return (String[]) myList.toArray(new String[myList.size()]); }
		}
	}
*/
	/**
	  * General search tool for arbitrary client types.
	  */
	static class GetClientsAction implements Action
	{
		private final Class targetClass, exclusionClass;
//		private ArrayList myList=new ArrayList();
		private final Vector myList=new Vector();
		
		public GetClientsAction(Class targetClass)
		{
			this.targetClass=targetClass;
			this.exclusionClass=null;
		}

		public GetClientsAction(Class targetClass, Class exclusionClass)
		{
			this.targetClass=targetClass;
			this.exclusionClass=exclusionClass;
		}

		public void doAction(Object o) throws Exception
		{
			Rmap r=(Rmap) o;
			if (targetClass.isInstance(r)
					&&(exclusionClass==null || !exclusionClass.isInstance(r))
					&&r.getName()!=null&&!".".equals(r.getName()))
				// verify has name.  '.' case will return getFullName() 
				//  of null.
				myList.addElement(r.getFullName());
		}
		
		public String[] getNames()
		{ 
			String[] names=new String[myList.size()];
			myList.copyInto(names);
			return names;
			//{ return (String[]) myList.toArray(new String[myList.size()]); }
		}
	}


////////////////////////////////////////////////////////////////////////////
////////////////////// Static data: //////////////
	private static final String
			serverAddressDefault="localhost:3333",
			clientNameDefault="MyClient",
			userNameDefault="",
			passwordDefault="";

	// JPW 09/28/2004: J# threw a compiler error with the definition of
	//		   dataMarker, as follows:
	//                 "The blank final field 'dataMarker' is not
	//		   initialized at declaration or in constructor".
	//		   dataMarker is only referenced once in this class (and
	//		   it is never referenced by any other classes) so
	//		   its definition was moved to where it was actually
	//		   used.  This fixed the J# compiler error.
	// private final static DataBlock dataMarker
	// 	=new com.rbnb.api.DataBlock(new byte[1],1,1);

	//
	// 04/05/2002  WHF  Endpoints may now have data, due to the presence
	//   of meta-data in the registration map.  So, we only add data 
	//   markers to Rmaps without data.
	// 11/14/2002  WHF  Made package private, as called by CMap.produceRequest.
	// 09/28/2004  JPW  Moved dataMarker to be locally defined in this method.
	static Action addDataMarkerAction=new Action()
	{ public void doAction(Object o) throws Exception { 
		Rmap r=(Rmap) o;
		if (r.getDblock()==null) {
			// JPW 09/28/2004: Moved dataMarker from being a private
			//                 static final variable to here
			DataBlock dataMarker =
			    new com.rbnb.api.DataBlock(new byte[1],1,1);
			r.setDblock(dataMarker);
		}
	} };

	// 2003/07/30  WHF  Made package-protected for use by Source for
	//   Delete() method.
	static Action removeTimeAction=new Action()
	{ public void doAction(Object o) throws Exception {
		Rmap r=(Rmap) o;
		r.setTrange(null);
	} };
	
	// Create and initialize 'dot' Rmap:
	private static Rmap dotRmap; 
	static {
		try { dotRmap=new Rmap("."); }
		catch (Exception e) { } // no throw
	};

////////////////////////////////////////////////////////////////////////////
////////////////////// Package data: //////////////
	private transient Server server;  

////////////////////////////////////////////////////////////////////////////
////////////////////// Private data: //////////////
	private int	cacheSize,
			archiveSize,
			archiveMode;

	/**
	  * A general Action interface.  Eventually move to the utility 
	  *   package.
	  */ 
	static interface Action
	{ 
		public void doAction(Object o) throws Exception;
	}

} // end class Client

