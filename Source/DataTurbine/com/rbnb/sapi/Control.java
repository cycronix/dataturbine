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

/*
	Control.java
	
	Copyright Creare Inc.  2006.  All rights reserved.
	
	
	*** Modification History ***
	2007/07/20  WHF  Created.
	2009/05/20  WHF  Implemented Terminate(String) method.  Renamed Terminate()
		method to TerminateServer().
	2010/08/30  JPW, Erigo	Added createMirror and createTimeMirror.
				Commented out CreateMirrorIn, as it is not
				    fully implemented.
				Filled out several CreateMirrorOut methods as
				    convenience functions for calling createMirror.
*/

package com.rbnb.sapi;

import java.lang.reflect.Method;
import java.io.IOException;
import java.util.Vector;

import com.rbnb.api.Controller;
import com.rbnb.api.ControllerHandle;
import com.rbnb.api.DataRequest;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Username;

/**
  * A client for controlling RBNB server behavior.
  * <p>
  * @author WHF
  * @since V3.1
  *
  */
public class Control extends Client
{
	public Control()
	{
		super(1, null, 0);
	}
	
//*****************************  Public Methods  ****************************//
	/**
	  * Creates a mirror to copy data from a source on the specified server 
	  *  address to the connected server (pull mirror).
	  *  
	  *  THIS METHOD IS NOT FULLY IMPLEMENTED.
	  */
	
	/*
	 * 
	 *  *** Modification History ***
	 *  2010/08/30  JPW, Erigo  Commented out this method as it is not
	 *                          fully implemented.  It could be
	 *                          implemented using the new createMirror/
	 *                          createTimeMirror calls found below.
	 */
	
	/*
	public void CreateMirrorIn(
			String remoteServer,
			String remoteSource,
			String localName) throws SAPIException
	{
		
		if (!CheckServerConnection()) {
		    throw new SAPIException("Unable to create mirror: not connected to the RBNB server.");
		}
		
		try {
			controller.mirror(Server.createMirror());	
		} catch (Exception e) {
			throw new SAPIException(e);
		}
		
	}
	*/
	
	/**
	  * Creates a mirror to copy data from a local source to the specified
	  * remote server (push mirror).  This method is a convenience wrapper
	  * around the createMirror method. This method assumes we are going to
	  * do a frame-based mirror, mirroring starting NOW and going on forever
	  * (that is, CONTINUOUS).  Furthermore, this method will specify that the
	  * new mirror source will match the original source's cache and archive
	  * settings.
	  */
	
	/*
	 * 
	 *  *** Modification History ***
	 *  2010/08/30  JPW, Erigo  Filled out this method by adding a call to
	 *                          the new createMirror method.
	 * 
	 */
	
	public void CreateMirrorOut(
			String localSourceI,
			String remoteServerI,
			String remoteNameI) throws SAPIException
	{
	    
	    if (!CheckServerConnection()) {
		 throw new SAPIException("Unable to create mirror: not connected to the RBNB server.");
	    }
	    
	    try {
		
		Control.createMirror(
		    controller,
		    getServer(),
		    null,
		    getServer().getAddress(),
		    null,
		    localSourceI,
		    null,
		    remoteServerI,
		    remoteNameI,
		    com.rbnb.api.Mirror.NOW,
		    com.rbnb.api.Mirror.CONTINUOUS,
		    0,
		    0,
		    com.rbnb.api.SourceInterface.ACCESS_NONE,
		    true);
	    } catch (Exception e) {
		if ( (e.getMessage() != null) && (!e.getMessage().isEmpty()) )
		    throw new SAPIException(e.getMessage());
		else {
		    String errStr = new String("Unable to create mirror\n" + e);
		    throw new SAPIException(errStr);
		}
	    }
	}
	
	/**
	  * Creates a mirror to copy data from a local source to the specified
	  * remote server (push mirror).  This method is a convenience wrapper
	  * around the createMirror method. This method assumes we are going to
	  * do a frame-based mirror, mirroring starting NOW and going on forever,
	  * that is CONTINUOUS.
	  */
	
	/*
	 *
	 *  *** Modification History ***
	 *  2010/08/30  JPW, Erigo  Filled out this method by adding a call to
	 *                          the new createMirror method.
	 * 
	 */
	
	public void CreateMirrorOut(
			String localSourceI,
			String remoteServerI,
			String remoteNameI,
			long numCacheFramesI,
			long numArchiveFramesI,
			byte archiveModeI,
			boolean bMatchFromSourceI) throws SAPIException
	{
	    
	    if (!CheckServerConnection()) {
		 throw new SAPIException("Unable to create mirror: not connected to the RBNB server.");
	    }
	    
	    try {
		Control.createMirror(
		    controller,
		    getServer(),
		    null,
		    getServer().getAddress(),
		    null,
		    localSourceI,
		    null,
		    remoteServerI,
		    remoteNameI,
		    com.rbnb.api.Mirror.NOW,
		    com.rbnb.api.Mirror.CONTINUOUS,
		    numCacheFramesI,
		    numArchiveFramesI,
		    archiveModeI,
		    bMatchFromSourceI);
	    } catch (Exception e) {
		if ( (e.getMessage() != null) && (!e.getMessage().isEmpty()) )
		    throw new SAPIException(e.getMessage());
		else {
		    String errStr = new String("Unable to create mirror\n" + e);
		    throw new SAPIException(errStr);
		}
	    }
	}
	
	/**
	  * Utility method - check to see if we are connected to the server.
	  * Return true if we are connected, return false if not connected.
	  */
	
	/*
	 * 
	 *  *** Modification History ***
	 *  2010/08/30  JPW, Erigo  Created.
	 * 
	 */
	
	private boolean CheckServerConnection() {
	    
	    try {
		if ( (controller == null) ||
		     (!controller.isRunning()) ||
		     (getServer() == null) )
		{
		    return false;
		}
	    } catch (Exception e) {
		return false;
	    }
	    return true;
	    
	}
	
	/**************************************************************************
	 * Start a frame-based mirror.
	 * <p>
	 * This method starts a mirror between two servers.  The arguments specify
	 * where the data is coming from (fromServer, fromSource) and where data is
	 * going to (toServer, toSource).  The mirror streams frames with an
	 * increment of 1 frame.
	 *
	 * @author John P. Wilson
	 * @author Ian A. Brown
	 *
	 * @param fromSourceI  The source on "fromServerI" data is coming from.
	 * @param fromServerAddressI  Address of server "fromServerI".
	 * @param fromSourceNameI  Name of source "fromSourceI".
	 * @param toServerAddressI  Address of server "toServerI".
	 * @param toSourceNameI  Name of the new source.
	 * @param startFlagI  Specifies start time: oldest or now.
	 * @param stopFlagI  Specifies stop time: now or continuous.
	 * @param numCacheFramesI  Number of archive frames in the new source;
	 *     this value is ignored if bMatchFromSourceI is true.
	 * @param numArchiveFramesI  Number of archive frames in the new source;
	 *     this value is ignored if bMatchFromSourceI is true.
	 * @param archiveModeI  Archiving mode: none, create, or append.
	 * @param bMatchFromSourceI  The archive/cache specs of the new source
	 *     should be set to match "fromSource".
	 * @exception java.lang.Exception
	 *            thrown if there is an error starting the mirror
	 * @since V2.0
	 * @version 08/26/2010
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 08/26/2010  JPW	Moved from com.rbnb.admin.RBNBDataManager.
	 * 03/01/2002  INB	Reworked because we don't really need as much
	 *			information as we were getting.  I've cut this down
	 *			to the absolute minimum needed to work.
	 * 05/29/2001  JPW	Created.
	 *
	 */

	public final static void createMirror(
		Controller controllerI,
		Server serverI,
		Server fromServerI,
		String fromServerAddressI,
		com.rbnb.api.Source fromSourceI,
		String fromSourceNameI,
		Server toServerI,
		String toServerAddressI,
		String toSourceNameI,
		int startFlagI,
		int stopFlagI,
		long numCacheFramesI,
		long numArchiveFramesI,
		byte archiveModeI,
		boolean bMatchFromSourceI)
	throws Exception
	{
	    if ( (controllerI == null) || (serverI == null) || (!controllerI.isRunning()) ) {
		throw new Exception("Unable to create mirror: not connected to the RBNB server.");
	    }
	    
	    Server server = serverI;

	    Server fromServer =
		((fromServerI != null) ?
			fromServerI :
			    Server.newServerHandle(null,fromServerAddressI));
	    Server toServer =
		((toServerI != null) ?
			toServerI :
			    Server.newServerHandle(null,toServerAddressI));
	    com.rbnb.api.Source fromSource = fromSourceI;

	    /////////////////////////////////////
	    //
	    // Make local copies of the arguments
	    //
	    /////////////////////////////////////

	    int startFlag = startFlagI;
	    int stopFlag = stopFlagI;
	    long numCacheFrames = numCacheFramesI;
	    long numArchiveFrames = numArchiveFramesI;
	    byte archiveMode = archiveModeI;
	    boolean bMatchFromSource = bMatchFromSourceI;

	    int numFrameSets = 10;

	    //////////////////////////////////////////////////////////
	    //
	    // Figure out values of reference, domain, and repetitions
	    //
	    //////////////////////////////////////////////////////////

	    byte reference = DataRequest.OLDEST;
	    byte domain = DataRequest.ALL;
	    long repetitions = DataRequest.INFINITE;

	    if ( (startFlag == com.rbnb.api.Mirror.OLDEST) &&
		    (stopFlag == com.rbnb.api.Mirror.CONTINUOUS) )
	    {
		reference = DataRequest.OLDEST;
		domain = DataRequest.ALL;
		repetitions = DataRequest.INFINITE;
	    }
	    else if ( (startFlag == com.rbnb.api.Mirror.OLDEST) &&
		    (stopFlag == com.rbnb.api.Mirror.NOW) )
	    {
		// This doesn't really do what we want; the mirror will continue
		// to request data until it "catches up" and comes to the end of
		// the ring buffer.  If the source's data rate is slow, this will
		// probably work fine; if it is high rate, the mirror may never
		// catch up and will continue to mirror data indefinitely.
		reference = DataRequest.OLDEST;
		domain = DataRequest.EXISTING;
		repetitions = DataRequest.INFINITE;
	    }
	    else if ( (startFlag == com.rbnb.api.Mirror.NOW) &&
		    (stopFlag == com.rbnb.api.Mirror.CONTINUOUS) )
	    {
		reference = DataRequest.NEWEST;
		domain = DataRequest.FUTURE;
		repetitions = DataRequest.INFINITE;
	    }
	    else if ( (startFlag == com.rbnb.api.Mirror.NOW) &&
		    (stopFlag == com.rbnb.api.Mirror.NOW) )
	    {
		// Get newest single frame
		reference = DataRequest.NEWEST;
		domain = DataRequest.EXISTING;
		repetitions = 1;
	    }

	    /////////////////////////////////////////////////
	    //
	    // Check if user wants to match the "FROM" source
	    //
	    /////////////////////////////////////////////////

	    if (bMatchFromSource) {
		// Get the new values from "fromSource"

		if (fromSource == null) {
		    Controller tempController = null;
		    boolean bStopControllerWhenDone = false;
		    // Is fromServer the local server?
		    if (server.getAddress().compareTo(fromServer.getAddress()) == 0) {
			fromServer = server;
			tempController = controllerI;
			bStopControllerWhenDone = false;
		    }
		    else {
			// Must create a temporary connection to fromServer
			tempController =
			    fromServer.createController("tempController");
			tempController.start();
			bStopControllerWhenDone = true;
		    }
		    Rmap tempRmap;
		    /* INB 10/28/2002 - always create the name as specified by
		     * the user.
			if (fromSourceNameI.indexOf(Rmap.PATHDELIMITER) == 0) {
			    tempRmap =
				Rmap.createFromName(fromSourceNameI.substring(1));
			} else {
		     */
		    tempRmap = Rmap.createFromName(fromSourceNameI);
		    /*
			}
		     */

		    // INB 11/20/2002: Mark the leaf node.
		    tempRmap.markLeaf();

		    Rmap rmap = tempController.getRegistered(tempRmap);

		    if (bStopControllerWhenDone) {
			tempController.stop();
		    }
		    // Now, search for the fromSource in this Rmap
		    // What gets returned from getRegistered():
		    //    EndOfStream
		    //       RoutingMap
		    //          fromServer
		    //             fromSource
		    while (true) {
			if (rmap instanceof com.rbnb.api.Source) {
			    // This must be our Source!
			    fromSource = (com.rbnb.api.Source)rmap;
			    break;
			}
			// Get the child of rmap
			if (rmap.getNchildren() != 1) {
			    throw new Exception(
				    "Error in Rmap structure: " +
				    "\"From\" Source, " +
				    fromSourceNameI +
			    ", could not be located.");
			}
			else {
			    rmap = rmap.getChildAt(0);
			}
		    }
		}
		if (fromSource == null) {
		    throw new Exception(
			    "Error in Rmap structure: " +
			    "\"From\" Source, " +
			    fromSourceNameI +
		    ", could not be located.");
		}
		numCacheFrames = fromSource.getCframes();
		numArchiveFrames = fromSource.getAframes();
		numFrameSets = fromSource.getNfs();
		archiveMode = fromSource.getAmode();

		//System.err.println("From source: " + numCacheFrames + " " +
		//		   numArchiveFrames + " " +
		//		   numFrameSets + " " +
		//		   archiveMode);

	    }

	    /////////////////////////
	    //
	    // Create the DataRequest
	    //
	    /////////////////////////

	    DataRequest req =
		new DataRequest(
			null,
			null,
			null,
			reference,
			domain,
			repetitions,
			1.,
			false,
			DataRequest.FRAMES);

	    // 03/01/2002 - INB use relative names for the request.
	    // 10/28/2002 - INB no, use the name as specified by the user.
	    Rmap src;
	    /*
		if (fromSourceNameI.indexOf(Rmap.PATHDELIMITER) == 0) {
		    src = Rmap.createFromName(fromSourceNameI.substring(1) +
					      Rmap.PATHDELIMITER +
					      "...");
		} else {
	     */
	    src = Rmap.createFromName(fromSourceNameI +
		    Rmap.PATHDELIMITER +
		    "...");
	    /*
		}
	     */
	    src.setFrange(new com.rbnb.api.TimeRange(0.,0.));
	    Rmap bottom = src.moveToBottom();
	    bottom.setDblock(new com.rbnb.api.DataBlock(new byte[1],1,1));
	    req.addChild(src);

	    ////////////////////
	    //
	    // Set up the Mirror
	    //
	    ////////////////////

	    com.rbnb.api.Mirror mirror = server.createMirror();
	    if ((fromServer == server) ||
		    (server.getAddress().compareTo(fromServer.getAddress()) == 0)) {
		// fromServer is the local server
		mirror.setRemote(toServer);
		mirror.setDirection(com.rbnb.api.Mirror.PUSH);
	    }
	    else {
		// toServer is the local server
		mirror.setRemote(fromServer);
		mirror.setDirection(com.rbnb.api.Mirror.PULL);
	    }
	    mirror.setRequest(req);
	    if (toSourceNameI.indexOf(Rmap.PATHDELIMITER) == -1) {
		mirror.getSource().setName(toSourceNameI);
	    } else {
		mirror.getSource().setName
		(toSourceNameI.substring
			(toSourceNameI.lastIndexOf(Rmap.PATHDELIMITER) + 1));
	    }
	    mirror.getSource().setCframes(numCacheFrames);
	    mirror.getSource().setNfs(numFrameSets);
	    mirror.getSource().setAmode(archiveMode);
	    mirror.getSource().setAframes(numArchiveFrames);

	    ///////////////////
	    //
	    // Start the mirror
	    //
	    ///////////////////

	    controllerI.mirror(mirror);

	}
	
	/**************************************************************************
	 * Start a time-based mirror.
	 * <p>
	 * This method starts a mirror between two servers.  The arguments specify
	 * where the data is coming from (fromServer, fromSource) and where data is
	 * going to (toServer, toSource).  The mirror streams by time - in this
	 * case, a duration of zero means that the data is copied point by point.
	 * @author John P. Wilson
	 * @author Ian A. Brown
	 *
	 * @param fromSourceI  The source on "fromServerI" data is coming from.
	 * @param fromServerAddressI  Address of server "fromServerI".
	 * @param fromSourceNameI  Name of source "fromSourceI".
	 * @param toServerAddressI  Address of server "toServerI".
	 * @param toSourceNameI  Name of the new source.
	 * @param startFlagI  Specifies start time: oldest or now.
	 * @param stopFlagI  Specifies stop time: now or continuous.
	 * @param numCacheFramesI  Number of archive frames in the new source;
	 *     this value is ignored if bMatchFromSourceI is true.
	 * @param numArchiveFramesI  Number of archive frames in the new source;
	 *     this value is ignored if bMatchFromSourceI is true.
	 * @param archiveModeI  Archiving mode: none, create, or append.
	 * @param bMatchFromSourceI  The archive/cache specs of the new source
	 *     should be set to match "fromSource".
	 * @param durationI the nominal time span of each data retrieval.  A value
	 *			of 0 means copy point by point.
	 * @exception java.lang.Exception
	 *            thrown if there is an error starting the mirror
	 * @since V2.2
	 * @version 08/27/2010
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 08/27/2010  JPW	Moved from com.rbnb.admin.RBNBDataManager.
	 * 11/24/2003  INB	Created from <code>createMirror</code>.
	 *
	 */
	public final static void createTimeMirror(
		Controller controllerI,
		Server serverI,
		Server fromServerI,
		String fromServerAddressI,
		com.rbnb.api.Source fromSourceI,
		String fromSourceNameI,
		Server toServerI,
		String toServerAddressI,
		String toSourceNameI,
		int startFlagI,
		int stopFlagI,
		long numCacheFramesI,
		long numArchiveFramesI,
		byte archiveModeI,
		boolean bMatchFromSourceI,
		double durationI)
	throws Exception
	{
	    if ( (controllerI == null) || (serverI == null) || (!controllerI.isRunning()) ) {
		throw new Exception("Unable to create mirror: not connected to the RBNB server.");
	    }
	    
	    Server server = serverI;
	    
	    Server fromServer =
		((fromServerI != null) ?
			fromServerI :
			    Server.newServerHandle(null,fromServerAddressI));
	    Server toServer =
		((toServerI != null) ?
			toServerI :
			    Server.newServerHandle(null,toServerAddressI));
	    com.rbnb.api.Source fromSource = fromSourceI;

	    /////////////////////////////////////
	    //
	    // Make local copies of the arguments
	    //
	    /////////////////////////////////////

	    int startFlag = startFlagI;
	    int stopFlag = stopFlagI;
	    long numCacheFrames = numCacheFramesI;
	    long numArchiveFrames = numArchiveFramesI;
	    byte archiveMode = archiveModeI;
	    boolean bMatchFromSource = bMatchFromSourceI;
	    double duration = durationI;

	    int numFrameSets = 10;

	    //////////////////////////////////////////////////////////
	    //
	    // Figure out values of reference, domain, and repetitions
	    //
	    //////////////////////////////////////////////////////////

	    byte reference = DataRequest.OLDEST;
	    byte domain = DataRequest.ALL;
	    long repetitions = DataRequest.INFINITE;

	    if ( (startFlag == com.rbnb.api.Mirror.OLDEST) &&
		    (stopFlag == com.rbnb.api.Mirror.CONTINUOUS) )
	    {
		reference = DataRequest.OLDEST;
		domain = DataRequest.ALL;
		repetitions = DataRequest.INFINITE;
	    }
	    else if ( (startFlag == com.rbnb.api.Mirror.OLDEST) &&
		    (stopFlag == com.rbnb.api.Mirror.NOW) )
	    {
		// The only real way to do this at this point is to retrieve
		// the time information for the existing data and then stream until
		// we retrieve a time equal to or greater than that number.  This
		// would have to be done by the mirror and we'll need a way to
		// tell it to end.
		throw new java.lang.IllegalArgumentException
		("Streaming from oldest to now is not currently supported.");
	    }
	    else if ( (startFlag == com.rbnb.api.Mirror.NOW) &&
		    (stopFlag == com.rbnb.api.Mirror.CONTINUOUS) )
	    {
		reference = DataRequest.NEWEST;
		domain = DataRequest.FUTURE;
		repetitions = DataRequest.INFINITE;
	    }
	    else if ( (startFlag == com.rbnb.api.Mirror.NOW) &&
		    (stopFlag == com.rbnb.api.Mirror.NOW) )
	    {
		// Get newest single frame
		reference = DataRequest.NEWEST;
		domain = DataRequest.EXISTING;
		repetitions = 1;
	    }

	    /////////////////////////////////////////////////
	    //
	    // Check if user wants to match the "FROM" source
	    //
	    /////////////////////////////////////////////////

	    if (bMatchFromSource) {
		// Get the new values from "fromSource"

		if (fromSource == null) {
		    Controller tempController = null;
		    boolean bStopControllerWhenDone = false;
		    // Is fromServer the local server?
		    if (server.getAddress().compareTo(fromServer.getAddress()) ==
			0) {
			fromServer = server;
			tempController = controllerI;
			bStopControllerWhenDone = false;
		    }
		    else {
			// Must create a temporary connection to fromServer
			tempController =
			    fromServer.createController("tempController");
			tempController.start();
			bStopControllerWhenDone = true;
		    }

		    Rmap tempRmap = Rmap.createFromName(fromSourceNameI);
		    tempRmap.markLeaf();

		    Rmap rmap = tempController.getRegistered(tempRmap);

		    if (bStopControllerWhenDone) {
			tempController.stop();
		    }

		    // Now, search for the fromSource in this Rmap
		    // What gets returned from getRegistered():
		    //    EndOfStream
		    //       RoutingMap
		    //          fromServer
		    //             fromSource
		    while (true) {
			if (rmap instanceof com.rbnb.api.Source) {
			    // This must be our Source!
			    fromSource = (com.rbnb.api.Source)rmap;
			    break;
			}
			// Get the child of rmap
			if (rmap.getNchildren() != 1) {
			    throw new Exception(
				    "Error in Rmap structure: " +
				    "\"From\" Source, " +
				    fromSourceNameI +
			    ", could not be located.");
			}
			else {
			    rmap = rmap.getChildAt(0);
			}
		    }
		}
		if (fromSource == null) {
		    throw new Exception(
			    "Error in Rmap structure: " +
			    "\"From\" Source, " +
			    fromSourceNameI +
		    ", could not be located.");
		}
		numCacheFrames = fromSource.getCframes();
		numArchiveFrames = fromSource.getAframes();
		numFrameSets = fromSource.getNfs();
		archiveMode = fromSource.getAmode();

		//System.err.println("From source: " + numCacheFrames + " " +
		//		   numArchiveFrames + " " +
		//		   numFrameSets + " " +
		//		   archiveMode);

	    }

	    /////////////////////////
	    //
	    // Create the DataRequest
	    //
	    /////////////////////////

	    DataRequest req =
		new DataRequest(
			null,
			null,
			null,
			reference,
			(repetitions == 1) ? DataRequest.EQUAL : DataRequest.GREATER,
				domain,
				repetitions,
				1.,
				false,
				DataRequest.CONSOLIDATED,
				false);

	    Rmap src;
	    src = Rmap.createFromName(fromSourceNameI +
		    Rmap.PATHDELIMITER +
	    "...");
	    src.setTrange(new com.rbnb.api.TimeRange(0.,duration));
	    Rmap bottom = src.moveToBottom();
	    bottom.setDblock(new com.rbnb.api.DataBlock(new byte[1],1,1));
	    req.addChild(src);

	    ////////////////////
	    //
	    // Set up the Mirror
	    //
	    ////////////////////

	    com.rbnb.api.Mirror mirror = server.createMirror();
	    if ((fromServer == server) ||
		    (server.getAddress().compareTo(fromServer.getAddress()) == 0)) {
		// fromServer is the local server
		mirror.setRemote(toServer);
		mirror.setDirection(com.rbnb.api.Mirror.PUSH);
	    }
	    else {
		// toServer is the local server
		mirror.setRemote(fromServer);
		mirror.setDirection(com.rbnb.api.Mirror.PULL);
	    }
	    mirror.setRequest(req);
	    if (toSourceNameI.indexOf(Rmap.PATHDELIMITER) == -1) {
		mirror.getSource().setName(toSourceNameI);
	    } else {
		mirror.getSource().setName
		(toSourceNameI.substring
			(toSourceNameI.lastIndexOf(Rmap.PATHDELIMITER) + 1));
	    }
	    mirror.getSource().setCframes(numCacheFrames);
	    mirror.getSource().setNfs(numFrameSets);
	    mirror.getSource().setAmode(archiveMode);
	    mirror.getSource().setAframes(numArchiveFrames);

	    ///////////////////
	    //
	    // Start the mirror
	    //
	    ///////////////////

	    controllerI.mirror(mirror);

	}

	/**
	  * Stops the object with the given name.  It may be a server or any
	  *  sort of client.
	  * <p>Note: this method returns after the server has acknowledged receipt
	  *   of the command, but before the server executes the task.  Thus the
	  *  actual shutdown of the object will occur asynchronously.
	  */
	public void Terminate(String name) throws SAPIException
	{
		assertConnection();
		
		Rmap leaf;
		try {
			// Create an Rmap hierarchy to the target.  Note that we 
			//  use the fully qualified name unless a leading slash is present.
			//  This is because stop() fails with child servers using a 
			//  relative name (leading "." as the name).  Other clients work!
			String fullName = name.charAt(0) == '/' ? name 
					: getServer().getFullName() + '/' + name;
			Rmap toFind = getServer().createFromName(fullName);
				
			leaf = controller.getRegistered(toFind);
			// Find the endpoint of the Rmap chain:
			while (leaf.getNchildren() > 0)
				leaf = leaf.getChildAt(0);
		
			// The controller stop method takes one of Client, Server, 
			//  or Shortcut, none of which have a common base class.
			//  Thus we must handle each case separately.
			if (leaf instanceof com.rbnb.api.Client) {
				controller.stop((com.rbnb.api.Client) leaf);
			} else if (leaf instanceof Server) {
				controller.stop((Server) leaf);
			} else if (leaf instanceof com.rbnb.api.Shortcut) {
				controller.stop((com.rbnb.api.Shortcut) leaf);
			}

		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}
	
	/**
	  * Stops the server to which this Control is connected.
	  */
	public void TerminateServer() throws SAPIException
	{
		assertConnection();
		
		try {
			super.terminateLocalServer();
		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}
	
	/**
	  * Obtains a list of access control entries from the connected server.
	  */
	public Vector GetAccessControlList() throws SAPIException
	{
		Vector resp = new Vector();
		
		try {
			String auth = ((ControllerHandle) controller)
					.fetchAddressAuthorization(getServer()); 
			resp = CreateACLFromStream(new java.io.StringReader(auth));
		} catch (Exception e) {
			throw new SAPIException(e);
		}
		
		return resp;
	}
	
	/**
	  * Sets the access control list on the server.
	  * @throws IllegalArgumentException  if any element in the vector is
	  *   not an instance of {@link AccessControlEntry}.
	  */
	public void SetAccessControlList(Vector list) throws SAPIException
	{
		StringBuffer sb = new StringBuffer();

		for (int ii = 0; ii < list.size(); ++ii) {
			sb.append(list.elementAt(ii));
			sb.append('\n');
		}
		try {
			((ControllerHandle) controller).sendAddressAuthorization(
					getServer(),
					sb.toString()
			);
		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}

//*****************************  Client Overrides  **************************//
	public long BytesTransferred()
	{
		if (controller != null)
			return ((ControllerHandle)controller).bytesTransferred(); 
		else return 0L;
	}
		
	void doOpen(
			Server server,
			String clientName,
			String userName,
			String password
	) throws Exception
	{
	    controller = server.createController(clientName);
		if (userName != null) {
			controller.setUsername(new Username(userName,password));
		}
	    controller.start();
	}
	
	com.rbnb.api.Client getClient() { return controller; }
	final void clearData()
	{
		controller = null;
	}	
	
//*****************************  Inner Classes  *****************************//
	/**
	  * An element in a list of internet hosts which define permitted access
	  *   to an RBNB server.
	  */
	public final static class AccessControlEntry
	{
		private AccessControlEntry(
				boolean isDeny,
				String address,
				String permissions
		) {
			this.isDeny = isDeny;
			this.address = address;
			this.permissions = permissions;
		}
		
		public boolean isDeny() { return isDeny; }
		public String getAddress() { return address; }
		public String getPermissions() { return permissions; }
	
		public String toString() 
		{
			return (isDeny?"DENY ":"ALLOW ")+address
					+(permissions!=null?"="+permissions:"");
		}
		
		private boolean isDeny;
		private String address, permissions;
	}
	
//*****************************  Data Members  ******************************//
	private Controller controller;
	
//*****************************  Statics  ***********************************//
	/**
	  * Create a new access control entry that allows access.  The 
	  * <code>permissions</code> object is optional.  If specified it should
	  *  be one or more of the following:
	  *	  <ul>
	  *		<li>R - read permission (sink connections),</li>
	  *		<li>W - write permission (source connections),</li>
	  *		<li>X - execute permission (control connections and functions),</li>
	  *		<li>P - plugin permission (plugin connections), and</li>
	  *		<li>T - routing permission (routing connections).</li>
	  *	  </ul>
	  * If not set it is as if all these options were specified.
	  * <p>
	  * See the <a href="http://rbnb.creare.com:8080/documentation/Server/rbnbServer.html">
	  *   RBNB Server documentation</a> for more details.
	  */
	public static AccessControlEntry CreateAllowEntry(
			String address,
			String permissions)
	{ return new AccessControlEntry(false, address, permissions); }

	/**
	  * Create a new access control entry that forbids access.
	  */
	public static AccessControlEntry CreateDenyEntry(String address)
	{ return new AccessControlEntry(true, address, null); }
	
	/**
	  * Create an access control list from a character stream.
	  */
    /*
     *
     *   Date      By 	Description
     * YYYY/MM/DD
     * ----------  ---  -----------
     * 2007/07/23  WHF  Created from com.rbnb.api.AddressAuthorization code. 
     *
     */
	  
	public static Vector CreateACLFromStream(java.io.Reader isRead)
			throws IOException
	{
		java.io.LineNumberReader lRead = new java.io.LineNumberReader(isRead);
		String line,
			   token;
		java.util.StringTokenizer sTok;
		int allowDeny = 0;
		boolean allowAdded = false;
		Vector result = new Vector();

		try {
			while ((line = lRead.readLine()) != null) {
				// Read each line of the authorization file.

				if ((line.length() == 0) || (line.charAt(0) == '#')) {
					// Skip over blank lines and lines starting with #.
					continue;
				}

				// Tokenize the line by breaking it at whitespace.
				sTok = new java.util.StringTokenizer(line," \t\n\r");
				while (sTok.hasMoreTokens()) {
					// Loop through the tokens.
					token = sTok.nextToken();

					if (token.equalsIgnoreCase("ALLOW")) {
						// The addresses to follow will be allowed.
						allowDeny = 1;

					} else if (token.equalsIgnoreCase("DENY")) {
						// The addresses to follow will be denied.
						allowDeny = -1;

					} else if (allowDeny == 0) {
						// The allow/deny state has not been set yet.
						throw new java.lang.IllegalStateException
							("Do not know whether to allow or deny " +
							 token +
							 ".");

					} else {
						// Parse out the equals:
						int eq = token.indexOf('=');
						String perm;
						
						if (eq == -1) // no equals
							perm = null;
						else {
							perm = token.substring(eq+1);
							token = token.substring(0, eq);
						}
						
						if (allowDeny == -1) {
							// Deny mode:
							if (perm != null) throw new IllegalStateException(
								"DENY elements do not accept permissions.");
							result.addElement(CreateDenyEntry(token));
						} else {
							// Allow mode:
							allowAdded = true;
							result.addElement(CreateAllowEntry(
									token,
									perm
							));
						}
					}
				}
			}

			if (!allowAdded) {
				// If nothing was specifically allowed, then create a default
				// entry to allow everything that isn't denied.
				result.addElement(CreateAllowEntry("*", null));
			}

		} catch (java.io.EOFException e) {
		}

		lRead.close();	
		isRead.close();
		
		return result;
	}	
		
	
/*
	// Accesss control list test.
	public static void main(String args[]) throws Exception
	{
		Control c = new Control();
		c.OpenRBNBConnection();
		
		System.err.println(c.GetAccessControlList());
		
		if (args.length > 0) {
			c.SetAccessControlList(CreateACLFromStream(
					new java.io.StringReader(args[0])));
					
			// Get back to confirm:
			System.err.println(c.GetAccessControlList());
		} else System.err.println("No set.");

		c.CloseRBNBConnection();
	}
*/
}
