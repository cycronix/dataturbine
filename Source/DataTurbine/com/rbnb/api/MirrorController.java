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
 * Extended <code>MirrorIO</code> that actually performs the mirroring
 * task.
 * <p>
 * This class is the main workhorse for the mirroring operation. It creates a
 * chain that goes from an <code>NBO</code> to collect the data to be mirrored
 * through this object via the API and on to an <code>RBO</code> to store the
 * mirrored data. Depending on the direction of the mirror, various
 * communications will be RAM-based, while the rest will use the appropriate
 * communications medium for the remote <code>Server</code>.
 * <p>
 * If the direction is <code>PULL</code>, then the <code>RBO</code> is a local
 * RAM <code>RBO</code>, while the <code>NBO</code> is on the remote
 * <code>Server</code>. Data is "pulled" from the remote and placed into the
 * local <code>Server</code>.
 * <p>
 * If the direction is <code>PUSH</code>, then the <code>NBO</code> is a local
 * RAM <code>NBO</code>, while the <code>RBO</code> is on the remote
 * <code>Server</code>. Data is "pushed" from the local side to the remote.
 * <p>
 * Actually, in both cases, the actual data transfer operation is driven from
 * the <code>NBO</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/29/2007
 */

/*
 * Copyright 2001, 2002, 2003, 2004, 2007 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/07/2007  JPW	Add isSinkRunning(); this method is called to check on
 *			the Sink connection; it is called when the Mirror is
 *			trying to reconnect to the Source - if the user has
 *			closed down the Sink connection, then don't bother
 *			trying to reconnect to the Source any further.
 * 05/29/2007  JPW	In post(), try to reconnect the Mirror source if a
 *			SocketException is thrown.
 *			Move Sink reconnect logic from run() to loopRequest()
 * 04/27/2007  JPW	Add reconnection logic on the Sink connection; when
 *			reconnecting the Sink, start data request at NEWEST.
 * 02/20/2007  JPW	Add setRegistration(); when the Mirror is being set up,
 *			request registration information from the Sink.  If any
 *			channels have User Info, then pass registration onto
 *			the Source object that is the destination of the mirror
 *			data.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 10/13/2003  INB	Use CREATE mode for LOADed sources.
 * 04/18/2001  INB	Created.
 *
 */
class MirrorController
    extends com.rbnb.api.MirrorIO
    implements java.lang.Runnable,
	       com.rbnb.api.MirrorHandler
{
    /**
     * the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private ServerHandler local = null;
    
    /**
     * the <code>Sink</code> used to retrieve the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #source
     * @since V2.0
     * @version 05/11/2001
     */
    private Sink sink = null;

    /**
     * the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Thread thread = null;
    
    /**
     * Copied from com.rbnb.sapi.Client; used when we register Mirror channels.
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 03/06/2007
     */
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
    
    /**
     * Copied from com.rbnb.sapi.Client; used when we register Mirror channels.
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 03/06/2007
     */
    static Action removeTimeAction=new Action()
    { public void doAction(Object o) throws Exception {
	Rmap r=(Rmap) o;
	r.setTrange(null);
    } };
    
    /**
     * Used when we register Mirror channels.
     * <p>
     *
     * @author JPW
     *
     * @since V2.0
     * @version 03/06/2007
     */
    static Action removeFrameAction=new Action()
    { public void doAction(Object o) throws Exception {
	Rmap r=(Rmap) o;
	r.setFrange(null);
    } };
    
    /**
     * For Source/Sink auto-reconnections, this is the initial value for the
     * number of milliseconds to sleep between reconnect attempts.
     * <p>
     *
     * @author JPW
     *
     * @since V2.0
     * @version 05/29/2007
     */
    private final static long INITIAL_RETRY_PERIOD = 1000;
    
    /**
     * For Source/Sink auto-reconnections, this is the maximum number of
     * milliseconds to sleep between reconnect attempts.
     * <p>
     *
     * @author JPW
     *
     * @since V2.0
     * @version 05/29/2007
     */
    private final static long RETRY_PERIOD_MAX = 60000;
    
    /**
     * For Source/Sink auto-reconnections, this is the maximum number of times
     * the reconnection will be attempted.  When sleeping 60 seconds between
     * connection attempts, this roughly corresponds to 1 day.  Note, however,
     * that Source reconnections involve a timeout of their own, so the total
     * length of time reconnections will be attempted will be longer than this.
     * <p>
     *
     * @author JPW
     *
     * @since V2.0
     * @version 05/29/2007
     */
    private final static long MAX_NUM_RETRIES = 1500;
    
    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    MirrorController() {
	super();
    }

    /**
     * Class constructor to build a <code>MirrorController</code> running on
     * the specified <code>ServerHandler</code> to run the specified
     * <code>Mirror</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param localI  the local <code>ServerHandler</code>.
     * @param mirrorI the <code>Mirror</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception java.lang.IllegalStateException
     *		  thrown if mirror is not supported by the
     *		  <code>License</code>.
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 04/18/2001  INB	Created.
     *
     */
    MirrorController(ServerHandler localI,Mirror mirrorI)
	throws com.rbnb.api.AddressException
    {
	/*
	License license = ((RBNB) localI).getLicense();
	if (!license.mirrors()) {
	    throw new java.lang.IllegalStateException
		("This license (" +
		 license.version() + " #" + license.serialNumber() +
		 ") does not support mirroring.");
	}
	*/

	setLocal(localI);
	setDirection(mirrorI.getDirection());
	Address rAddress = Address.newAddress
	    (mirrorI.getRemote().getAddress());
	mirrorI.getRemote().setAddress(rAddress.getAddress());

	if (mirrorI.getRemote().getAddress().compareTo
	    (getLocal().getAddress()) == 0) {
	    setRemote((Server) localI);
	} else {
	    setRemote(mirrorI.getRemote());
	}
	setRequest(mirrorI.getRequest());
	setSource(mirrorI.getSource());

	setThread(new ThreadWithLocks(this,
				      ("_MC." +
				       localI.getAddress() +
				       ((getDirection() == PUSH) ?
					"->" :
					"<-") +
				       getRemote().getAddress())));
	getThread().start();
    }
    
    /**
     * A general Action interface.
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 03/06/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/06/2007  JPW	Copied from com.rbnb.sapi.Client
     *
     */
    static interface Action {
	public void doAction(Object o) throws Exception;
    }
    
    /**
     * Creates the <code>Sink</code>.
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
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnectSink()
     * @since V2.0
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void createSink()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server snkServer = ((getDirection() == PULL) ?
			    getRemote() :
			    (Server) getLocal()),
	       srcServer = ((getDirection() == PULL) ?
			    (Server) getLocal() :
			    getRemote());

	if (snkServer instanceof ServerHandler) {
	    snkServer = ((ServerHandler) snkServer).getClientSide();
	    setSink(snkServer.createRAMSink
		    ("_Mirror." + getSource().getName()));
	} else {
	    setSink(snkServer.createSink("_Mirror." + getSource().getName()));
	}
	getSink().setType(Client.MIRROR);
	getSink().setRemoteID(srcServer.getAddress() +
			      Rmap.PATHDELIMITER +
			      getSource().getName());
	getSink().start();
    }

    /**
     * Disconnects the <code>Sink</code>.
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
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not running.
     * @exception java.lang.InterruptedException
     *		  thrown if the terminate is interrupted.
     * @see #createSink()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void disconnectSink()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSink().stop();
    }

    /**
     * Disconnects the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param bForceStopI  If this is true, stop the source; otherwise, check
     *                     the value of persistentmirror to see if the source
     *                     should be stopped or detached.
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not running.
     * @exception java.lang.InterruptedException
     *		  thrown if the terminate is interrupted.
     * @see #initializeSource()
     * @since V2.0
     * @version 05/21/2008
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/21/2008  JPW  Add bForceStopI flag.  if bForceStopI is true, then
     *                  stop the source.  Otherwise, check the value of the
     *                  system property, "persistentmirror" - if it is true,
     *                  detach the source, otherwise stop the source.
     * 04/18/2001  INB	Created.
     *
     */
    private final void disconnectSource(boolean bForceStopI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// JPW 05/21/2008: Original code for this method:
	// getSource().stop();
	
	if (bForceStopI) {
	    getSource().stop();
	} else {
	    // Check the value of the system variable "persistentmirror"
	    String bPersistentMirrorStr = System.getProperty("persistentmirror","true");
	    if (bPersistentMirrorStr.equals("false")) {
		getSource().stop();
	    } else {
		// System.err.println(
		//     "MirrorController.disconnectSource(): detach the source");
		// detach the source
		// this code is based on what is done in sapi.Source.Detach() and
		//     sapi.Client._close(true, true) methods
		getSource().setAkeep(true);
		getSource().setCkeep(true);
		try {
		    getSource().synchronizeWserver();
		} catch (Exception e) {
		    // Nothing to do
		}
		getSource().stop();
	    }
	}
    }
    
    /**
     * Computes the value of the specified <code>Action</code> at
     * every end-point in the Rmap hierarchy.  An end-point is
     * defined as a Rmap with no children.
     * <p>
     * <strong>Note:</strong>The Action should neither add nor delete children.
     *
     * @author WHF
     *
     * @since V2.0
     * @version 03/06/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/06/2007  JPW	Copied from com.rbnb.sapi.Client.
     *
     */
    private static void forEachEndpoint(Rmap r, Action a) throws Exception
    {
	int n=r.getNchildren();
	if (n==0)
	    a.doAction(r);
	else for (int ii=0; ii<n; ++ii)
	    forEachEndpoint(r.getChildAt(ii),a);
    }
    
    /**
     * Performs the specified <code>action</code> at every node in
     * the hierarchy.
     * <p>
     * <strong>Note:</strong>The Action should neither add nor delete children.
     *
     * @author WHF
     *
     * @since V2.0
     * @version 03/06/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/06/2007  JPW	Copied from com.rbnb.sapi.Client.
     *
     */
    private static void forEachNode(Rmap r, Action a) throws Exception
    {
	int n=r.getNchildren();
	a.doAction(r);
	for (int ii=0; ii<n; ++ii)
	    forEachNode(r.getChildAt(ii),a);		
    }
    
    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    public final Log getLog() {
	return (getLocal().getLog());
    }

    /**
     * Gets the log class mask for this <code>MirrorController</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final long getLogClass() {
	return (Log.CLASS_MIRROR_CONTROLLER);
    }

    /**
     * Gets the base log level for this <code>MirrorController</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final byte getLogLevel() {
	return (Log.STANDARD);
    }

    /**
     * Gets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the local <code>ServerHandler</code>.
     * @see #setLocal(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final ServerHandler getLocal() {
	return (local);
    }

    /**
     * Gets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Sink</code>.
     * @see #setSink(com.rbnb.api.Sink)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final Sink getSink() {
	return (sink);
    }

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
     * @see #setThread(Thread)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final Thread getThread() {
	return (thread);
    }
    
    /**
     * Initializes the <code>Source</code>.
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
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnectSource()
     * @since V2.0
     * @version 10/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/13/2003  INB	Use CREATE mode for LOADed sources.
     * 04/18/2001  INB	Created.
     *
     */
    private final void initializeSource()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server srcServer = ((getDirection() == PULL) ?
			    (Server) getLocal() :
			    getRemote()),
	       snkServer = ((getDirection() == PULL) ?
			    getRemote() :
			    (Server) getLocal());
	Source lSource = getSource();

	if (srcServer instanceof ServerHandler) {
	    srcServer = ((ServerHandler) srcServer).getClientSide();
	    setSource(srcServer.createRAMSource(lSource.getName()));
	} else {
	    setSource(srcServer.createSource(lSource.getName()));
	}
	getSource().setAframes(lSource.getAframes());
	if (lSource.getAmode() != Source.ACCESS_LOAD) {
	    getSource().setAmode(lSource.getAmode());
	} else {
	    getSource().setAmode(Source.ACCESS_CREATE);
	}
	//	getSource().setAsize(lSource.getAsize());
	if (lSource.getCframes() > 0) {
	    getSource().setCframes(lSource.getCframes());
	}
	//	getSource().setCsize(lSource.getCsize());
	if (lSource.getNfs() > 0) {
	    getSource().setNfs(lSource.getNfs());
	}
	getSource().setType(Client.MIRROR);
	getSource().setRemoteID(snkServer.getAddress() +
				Rmap.PATHDELIMITER +
				"_Mirror." + getSource().getName());
	getSource().start();
    }
    
    /**
     * Check if the Sink is still running.  We do this by creating a control
     * connection to the Sink's RBNB and verify that the Sink object still
     * exists.
     * <p>
     * An easier way to do this would be to call "getSink().isRunning()", but
     * this method is not reliable when the Sink is streaming data.  The
     * "isRunning()" method does a round-trip to the Server - a Ping packet is
     * sent out and the Sink's ACO object checks that a Ping packet is received
     * from the Server on the Sink's data line.  However, if the Sink is
     * streaming data, then it is quite possible that the next frame in the
     * Sink's queue is NOT the Ping response (there may very well be other
     * frames ahead of the Ping packet); isRunning() will erroneously
     * return "false" in this case, because it didn't think it got back the
     * correct Ping response.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2007  JPW	Created.
     *
     */
    private boolean isSinkRunning() {
	try {
	    // Make a connection to the RBNB
	    // (the RBNB that contains the Mirror's Sink)
	    
	    Server snkServer = ((getDirection() == PULL) ?
			    	getRemote() :
				(Server) getLocal());
	    
	    Controller controller = null;
	    
	    // When the Sink server is local, make a RAM Controller connection
	    // to it; this way, we will be able to check to see if the Sink
	    // object still exists (or if the user has terminated it) even
	    // when the local network connection has been terminated.
	    if (snkServer instanceof ServerHandler) {
		Server server = ((ServerHandler) snkServer).getClientSide();
		controller = server.createRAMController("tempRAMController");
	    } else {
		Server server =
		    Server.newServerHandle(
			snkServer.getName(), snkServer.getAddress());
		controller =
		    server.createController("tempController");
	    }
	    controller.start();
	    Rmap tempRmap =
		Rmap.createFromName(
		    getSink().getName() + Rmap.PATHDELIMITER + "...");
	    tempRmap.markLeaf();
	    Rmap rmap = controller.getRegistered(tempRmap);
	    controller.stop();
	    if (rmap == null) {
		return false;
	    }
	    // Get rid of all the unnamed stuff in the Rmap hierarchy
	    rmap = rmap.toNameHierarchy();
	    if (rmap == null) {
		return false;
	    }
	    // System.err.println(
	    //  "\nMirrorController.isSinkRunning(): Full Rmap =\n" +
	    //  rmap +
	    //  "\n");
	    Rmap sinkRmap = rmap.findDescendant(getSink().getName(),false);
	    // System.err.println(
	    //  "\nMirrorController.isSinkRunning(): Sink Rmap =\n" +
	    // 	sinkRmap +
	    // 	"\n");
	    if (sinkRmap == null) {
		return false;
	    }
	    return true;
	} catch (Exception e) {
	    // System.err.println(
	    //   "Caught exception checking on Mirror Sink connection:\n" + e);
	    // Assume Sink connection is OK - this problem might have occurred
	    // if the Sink Server is currently off-line, but the Sink object
	    // might still be there.
	    return true;
	}
    }
    
    /**
     * Issues the request.
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
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #loopRequest()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void issueRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSink().addChild(getRequest());
	getSink().initiateRequestAt(0);
    }
    
    /**
     * THIS IS THE ORIGINAL VERSION OF THE loopRequest() METHOD (BEFORE
     * SINK RECONNECTION LOGIC WAS ADDED).
     * Loops on the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.EndOfStreamException
     *		  thrown if the request stream ends prematurely.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #issueRequest()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 04/18/2001  INB	Created.
     *
     */
    private final void loopRequest_original()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap response = null;

	while ((response = getSink().fetch(Sink.FOREVER)) != null) {
	    if (response instanceof EndOfStream) {
		if (response.getNchildren() == 1) {
		    Rmap child = response.getChildAt(0);
		    response.removeChild(child);
		    post(child,false);
		}
		break;
	    } else {
		post(response,false);
	    }
	    
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "MirrorController.loopRequest",
		     getLog(),
		     getLogLevel(),
		     getLogClass());
	    }
	}
    }
    
    /**
     * Loops on the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.EndOfStreamException
     *		  thrown if the request stream ends prematurely.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #issueRequest()
     * @since V2.0
     * @version 05/29/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/29/2007  JPW	Add Sink reconnection logic.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 04/18/2001  INB	Created.
     *
     */
    private final void loopRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap response = null;
	
	while (true) {
	    // Keep trying until fetch succeeds
	    while (true) {
		try {
		    response = getSink().fetch(Sink.FOREVER);
		    // Fetch succeeded; break out of fetch loop
		    break;
		} catch (java.net.SocketException se) {
		    getLog().addException(
			getLogLevel(),
			getLogClass(),
			getSource().getName(),
			se);
		    // If the original request was only for "EXISTING"
		    // data, then throw the exception - we're done
		    DataRequest dr = getRequest();
		    if (dr.getDomain() == DataRequest.EXISTING) {
			getLog().addMessage(
			    getLogLevel(),
			    getLogClass(),
			    getSource().getName(),
			    "Mirror sink request was only for existing data; we're done.");
			throw se;
		    }
		    int reconnectAttempt = 0;
		    long retryPeriod = MirrorController.INITIAL_RETRY_PERIOD;
		    while (true) {
			try {
			    getLog().addMessage(
			    	getLogLevel(),
				getLogClass(),
				getSource().getName(),
				"Restarting Mirror sink...");
			    reinitializeSink();
			    getLog().addMessage(
				getLogLevel(),
				getLogClass(),
				getSource().getName(),
				"Mirror sink successfully restarted");
			    // We succesfully reinitialized the Mirror sink; break out of the while loop
			    break;
			} catch (Exception reconnectException) {
			    getLog().addException(
				getLogLevel(),
				getLogClass(),
				getSource().getName(),
				reconnectException);
			    if (reconnectAttempt >= MirrorController.MAX_NUM_RETRIES) {
				getLog().addMessage(
				    getLogLevel(),
				    getLogClass(),
				    getSource().getName(),
				    "Exceeded maximum number of Mirror sink restart attempts");
				// Throw the original exception
				throw se;
			    } else {
				++reconnectAttempt;
				retryPeriod = retryPeriod * 2;
				if (retryPeriod > MirrorController.RETRY_PERIOD_MAX) {
				    retryPeriod = RETRY_PERIOD_MAX;
				}
				try {
				    Thread.currentThread().sleep(retryPeriod);
				} catch (Exception sleepException) {
				    // Nothing to do
				}
			    }
			}
		    }
		}
	    }
	    if (response == null) {
		break;
	    }
	    if (response instanceof EndOfStream) {
		if (response.getNchildren() == 1) {
		    Rmap child = response.getChildAt(0);
		    response.removeChild(child);
		    post(child,false);
		}
		break;
	    } else {
		post(response,false);
	    }
	    
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "MirrorController.loopRequest",
		     getLog(),
		     getLogLevel(),
		     getLogClass());
	    }
	}
    }
    
    /**
     * Posts a data response.
     * <p>
     * This method strips off the <code>Server</code> and <code>Source</code>
     * information from the input response before passing it to the target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param responseI  the response <code>Rmap</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.EndOfStreamException
     *		  thrown if the request stream ends prematurely.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/29/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/29/2007  JPW	Try to reconnect Source if a SocketException is thrown
     * 04/18/2001  INB	Created.
     *
     */
    private final void post(Rmap responseI, boolean bIsRegistrationI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	
	// Strip out the <code>Server</code> and <code>Source</code>
	// information from the response. In addition, strip out unnecessary
	// unnamed <code>Rmaps</code>.
	Rmap response = responseI,
	     level;
	boolean foundServer = false,
		foundSource = false,
		hasInfo = false;
	
	for (level = responseI;
	     !hasInfo ||!foundServer || !foundSource;
	     level = level.getChildAt(0)) {
	    boolean hadInfo = hasInfo;
	    
	    if (!hasInfo && (level.getParent() != null)) {
		response = level;
	    }
	    
	    if (!foundServer) {
		if (level instanceof Server) {
		    level.setName(null);
		    foundServer = true;
		} else if (level instanceof Client) {
		    throw new java.lang.IllegalArgumentException
			("Mirror request produced an Rmap that cannot be " +
			 "properly placed into the output." +
			 responseI);
		}
	    } else if (!foundSource) {
		if (level instanceof Client) {
		    level.setName(null);
		    foundSource = true;
		}
	    }
	    
	    if (!level.isNamelessTimeless()) {
		hasInfo = true;
	    }
	    if (!foundServer || !foundSource) {
		if (level.getNchildren() > 1) {
		    throw new java.lang.IllegalArgumentException
			("Mirror request produced an Rmap that cannot be " +
			 "properly placed into the output.\n" +
			 responseI);
		}
	    } else {
		hasInfo = hasInfo || (level.getNchildren() > 1);
	    }
	    
	    if (hasInfo && foundServer && foundSource) {
		if (!hadInfo) {
		    response = response.getParent();
		}
		break;
	    }
	}
	
	if (response != responseI) {
	    response.getParent().removeChild(response);
	}
	
	// Send the result to our <code>Source</code>.
	if (bIsRegistrationI) {
	    // System.err.println(
	    //     "MirrorController.post(): Sending out registration:\n" +
	    //     response);
	    getSource().register(response);
	} else {
	    // JPW 05/29/07: Try Source reconnect if SocketException is thrown
	    while (true) {
		try {
		    getSource().addChild(response);
		    // We successfully added the child to the Mirror source;
		    // break out of the do...while loop
		    break;
		} catch (java.net.SocketException se) {
		    // If the Mirror's Sink connection is down, the user must
		    // have terminated it;  assume the user wanted to terminate
		    // the Mirror
		    if (!isSinkRunning()) {
			throw se;
		    }
		    getLog().addException(
			getLogLevel(),
			getLogClass(),
			getSource().getName(),
			se);
		    int reconnectAttempt = 0;
		    long retryPeriod = MirrorController.INITIAL_RETRY_PERIOD;
		    while (true) {
			try {
			    getLog().addMessage(
				getLogLevel(),
				getLogClass(),
				getSource().getName(),
				"Restarting Mirror source...");
			    // If the Mirror's Sink connection is down, the
			    // user must have terminated it; assume the user
			    // wanted to terminate the Mirror
			    if (!isSinkRunning()) {
				// JPW 05/21/08: Change from calling break to throwing the original exception
				// break;
				// Throw the original exception
				throw se;
			    }
			    // First, the output Source must be terminated and
			    // then the Source can reconnect.  Otherwise, when
			    // the Source reconnects, an IllegalStateException
			    // will be thrown (“Cannot reconnec to existing
			    // client handler”).
			    stopOutputSource();
			    reinitializeSource();
			    getLog().addMessage(
				getLogLevel(),
				getLogClass(),
				getSource().getName(),
				"Mirror source successfully restarted");
			    // We succesfully reinitialized Mirror source;
			    // break out of the while loop
			    break;
			} catch (Exception reconnectException) {
			    getLog().addException(
				getLogLevel(),
				getLogClass(),
				getSource().getName(),
				reconnectException);
			    if (reconnectAttempt >= MirrorController.MAX_NUM_RETRIES) {
				getLog().addMessage(
				    getLogLevel(),
				    getLogClass(),
				    getSource().getName(),
				    "Exceeded maximum number of Mirror source restart attempts");
				// Throw the original exception
				throw se;
			    } else {
				++reconnectAttempt;
				retryPeriod = retryPeriod * 2;
				if (retryPeriod > MirrorController.RETRY_PERIOD_MAX) {
				    retryPeriod = RETRY_PERIOD_MAX;
				}
				try {
				    Thread.currentThread().sleep(retryPeriod);
				} catch (Exception sleepException) {
				    // Nothing to do
				}
			    }
			}
		    }
		}
	    }
	}
	
    }
    
    /**
     * Reinitialize the <code>Sink</code>.
     * <p>
     * This method will be called if a SocketException occurs when trying
     * to fetch data on the Sink connection.
     *
     * @author John Wilson
     *
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
     * @version 05/29/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/29/2007  JPW	Created.
     *
     */
    private final void reinitializeSink()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    disconnectSink();
	} catch (Exception ignoreException) {
	    // don't do anything
	}
	// Tweak the request to start from newest
	DataRequest dr = getRequest();
	dr.setReference(DataRequest.NEWEST);
	dr.setDomain(DataRequest.FUTURE);
	dr.setRepetitions(DataRequest.INFINITE, dr.getIncrement());
	setRequest(dr);
	createSink();
	// Reissue the request
	issueRequest();
    }
    
    /**
     * Reinitialize the <code>Source</code>.
     * <p>
     * This method will be called if a SocketException occurs when trying
     * to post data to a Source.
     *
     * @author John Wilson
     *
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
     * @version 05/29/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/29/2007  JPW	Created.
     *
     */
    private final void reinitializeSource()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Save current settings
	long aFrames = getSource().getAframes();
	long cFrames = getSource().getCframes();
	int nfs = getSource().getNfs();
	String nameStr = getSource().getName();
	
	// Close the existing Source
	try {
	    disconnectSource(true);
	} catch (Exception ignoreException) {
	    // don't do anything
	}
	
	Server srcServer = ((getDirection() == PULL) ?
			    (Server) getLocal() :
			    getRemote()),
	       snkServer = ((getDirection() == PULL) ?
			    getRemote() :
			    (Server) getLocal());
	
	if (srcServer instanceof ServerHandler) {
	    srcServer = ((ServerHandler) srcServer).getClientSide();
	    setSource(srcServer.createRAMSource(nameStr));
	} else {
	    setSource(srcServer.createSource(nameStr));
	}
	getSource().setAframes(aFrames);
	// Load archive for append
	if (aFrames > 0) {
	    getSource().setAmode(Source.ACCESS_APPEND);
	}
	getSource().setCframes(cFrames);
	getSource().setNfs(nfs);
	getSource().setType(Client.MIRROR);
	getSource().setRemoteID(snkServer.getAddress() +
				Rmap.PATHDELIMITER +
				"_Mirror." + nameStr);
	getSource().start();
	
	// Should we re-register Sink chans?
	// setRegistration();
    }
    
    /**
     * Runs the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/27/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2007  JPW	Add reconnection logic on the Sink connection;
     *			when reconnecting the Sink, start data request
     *			at NEWEST.
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 04/18/2001  INB	Created.
     *
     */
    public final void run() {
	
	try {
	    // Initialize the destination <code>Source</code>.
	    initializeSource();

	    // Create the <code>Sink</code>.
	    createSink();

	    // JPW 02/20/2007: Get registration info from the Sink; if any
	    //                 chans have User Info, then send registration
	    //                 to the Source object which is the destination
	    //                 of the mirrored data.
	    setRegistration();

	    // Issue the request.
	    issueRequest();

	    // Loop until the request completes.
	    loopRequest();
	} catch (java.lang.Exception e) {
	    try {
		String name;
		if (getDirection() == PULL) {
		    name = getLocal().getFullName();
		} else {
		    name = getRemote().getFullName();
		}
		name += "<-" + getSource().getFullName();
		for (int idx = 0; idx < name.length(); ++idx) {
		    if (name.charAt(idx)  == '/') {
			name = (name.substring(0,idx) +
				"_" +
				name.substring(idx + 1));
		    }
		}

		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     name,
		     e);
	    } catch (java.lang.Exception e1) {
	    }

	} finally {
	    try {
		disconnectSource(false);
	    } catch (java.lang.Exception e) {
	    }
	    try {
		disconnectSink();
	    } catch (java.lang.Exception e) {
	    }
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "MirrorController.run",
		     getLog(),
		     getLogLevel(),
		     getLogClass());
	    }
	    setThread(null);
	}
    }

    /**
     * Sets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param localI  the local <code>ServerHandler</code>.
     * @see #getLocal()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void setLocal(ServerHandler localI) {
	local = localI;
    }
    
    /**
     * Request registration information from the Sink.  If any channels have
     * User Info, then pass registration onto the Source object that is
     * the destination of the mirror data.
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.6
     * @version 02/20/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2007  JPW	Created.
     *
     */
    private final void setRegistration() {
	
	try {
	    
	    // Set up a temporary sink connection to request registration on
	    Sink tempSink = null;
	    Server snkServer =
		((getDirection() == PULL) ? getRemote() : (Server) getLocal());
	    if (snkServer instanceof ServerHandler) {
		snkServer = ((ServerHandler) snkServer).getClientSide();
	    }
	    tempSink =
		snkServer.createSink("_MirrorReg." + getSource().getName());
	    tempSink.start();
	    
	    // Produce the registration request
	    Rmap srcRmap = (Rmap)getRequest().getChildAt(0).clone();
	    forEachNode(srcRmap,removeTimeAction);
	    forEachNode(srcRmap,removeFrameAction);
	    DataRequest regReq =
		new DataRequest(
		    null,
		    null,
		    null,
		    DataRequest.NEWEST,
		    DataRequest.EQUAL,
		    DataRequest.EXISTING,
		    1,
		    1.,
		    false,
		    DataRequest.CONSOLIDATED,
		    false);
	    regReq.addChild(srcRmap);
	    
	    // Get registration
	    Rmap regRmap = tempSink.getRegistered(regReq);
	    // We're all done with the Sink now
	    tempSink.stop();
	    // System.err.println(
	    // 	"MirrorController.setReg: got registration result:\n" +
	    //  regRmap);
	    
	    if (regRmap != null) {
		String[] names = regRmap.extractNames();
		// Check to see if we have any User Info; we will register all
		// the chans even if we only have 1 with User Info
		// To determine if a channel has UserInfo, I use the same tests
		// as is done in com.rbnb.sapi.ChannelMap.GetUserInfo()
		boolean bHaveUserInfo = false;
		for (int ii=0; ii<names.length; ++ii) {
		    String name = names[ii];
		    DataArray res = regRmap.extract(name);
		    Object data = res.getData();
		    if (data != null)
		    {
			Class cl=data.getClass();
			if ( (cl == byte[][].class) &&
			     (res.getDataType() == DataBlock.TYPE_USER) )
			{
			    bHaveUserInfo = true;
			    // System.err.println(
			    // 	"User Info on chan \"" +
			    // 	name +
			    // 	"\": \"" +
			    // 	res.toString() +
			    // 	"\"");
			    break;
			}
			else if ((res.getMIMEType() != null)            &&
			         (res.getMIMEType().equals("text/xml")) &&
			         (res.getDataType() == DataBlock.TYPE_STRING))
			{
			    // Make sure data has "<user>" and "</user>" tags
			    String[] dataStrArray = (String[])res.getData();
			    for (int j = 0; j < dataStrArray.length; ++j) {
				String tempStr = dataStrArray[j];
				int index1 = tempStr.indexOf("<user>");
				int index2 = tempStr.lastIndexOf("</user>");
				if ( (index1 >= 0) &&
				     (index2 >= 0) &&
				     (index2 > index1) )
				{
				    bHaveUserInfo = true;
				    // String tempUserInfo =
				    //     tempStr.substring(index1+6,index2);
				    // System.err.println(
				    // 	"User Info on chan \"" +
				    // 	name +
				    // 	"\": \"" +
				    // 	tempUserInfo +
				    // 	"\"");
				    break;
				}
			    }
			    if (bHaveUserInfo) {
				break;
			    }
			}
		    }
		}
		if (bHaveUserInfo) {
		    // Rmap has UserInfo on at least one channel;
		    // we will register this Rmap (all the chans, even those
		    // which don't have User Info).
		    // Follow a similar procedure here as is done in
		    // com.rbnb.sapi.Client.doRegister()
		    forEachEndpoint(regRmap,addDataMarkerAction);
		    forEachNode(regRmap,removeTimeAction);
		    // Also remove Frame range from each node
		    // NOTE: this isn't done in Client.doRegister(), but it
		    //       seems like the right thing to do.
		    forEachNode(regRmap,removeFrameAction);
		    regRmap.setName(null);
		    post(regRmap,true);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * Sets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI  the <code>Sink</code>.
     * @see #getSink()
     * @since V2.0
     * @version 05/22/2001
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void setSink(Sink sinkI) {
	sink = sinkI;
    }
    
    /**
     * Sets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI  the thread.
     * @see #getThread()
     * @since V2.0
     * @version 05/22/2001
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI) {
	thread = threadI;
    }
    
    /**
     * Stop the output source.  This is used when the Mirror's output Source is
     * trying to reconnect - first, the output Source must be terminated
     * and then the Source can reconnect.  Otherwise, when the Source
     * reconnects, an IllegalStateException will be thrown (“Cannot reconnec
     * to existing client handler”).
     * <p>
     * This method uses the same logic as rbnbAdmin for terminating a Source.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/06/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/06/2007  JPW	Created.
     *
     */
    private void stopOutputSource() throws Exception {
	
	// Make a connection to the RBNB
	// (the RBNB that contains the Mirror output Source)
	Server srcServer = ((getDirection() == PULL) ?
			    (Server) getLocal() :
			    getRemote());
	// Server server = Server.newServerHandle("DTServer", getRemote().getAddress());
	Server server = Server.newServerHandle(srcServer.getName(), srcServer.getAddress());
	Controller controller = server.createController("tempController");
	controller.start();
	Rmap tempRmap =
	    Rmap.createFromName(
		getSource().getName() + Rmap.PATHDELIMITER + "...");
	tempRmap.markLeaf();
	Rmap rmap = controller.getRegistered(tempRmap);
	if (rmap == null) {
	    controller.stop();
	    return;
	}
	// Get rid of all the unnamed stuff in the Rmap hierarchy
	rmap = rmap.toNameHierarchy();
	if (rmap == null) {
	    controller.stop();
	    return;
	}
	// System.err.println(
	//     "\nMirrorController.stopOutputSource(): Full Rmap =\n" +
	//     rmap +
	//     "\n");
	Rmap startingRmap = rmap.findDescendant(getSource().getName(),false);
	if (startingRmap == null) {
	    controller.stop();
	    return;
	}
	// System.err.println(
	//     "\nMirrorController.stopOutputSource(): Starting Rmap =\n" +
	//     startingRmap +
	//     "\n");
	try {
	    // If the client is a Source, clear the keep cache flag.  This will
	    // ensure that the RBO will actually go away.
	    if (startingRmap instanceof Source) {
		((Source) startingRmap).setCkeep(false);
	    }
	    controller.stop((Client)startingRmap);
	} catch (Exception e) {
	    controller.stop();
	    throw e;
	}
	controller.stop();
	
    }
}

