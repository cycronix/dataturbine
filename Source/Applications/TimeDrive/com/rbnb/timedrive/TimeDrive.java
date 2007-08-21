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


package com.rbnb.timedrive;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;

/******************************************************************************
 * Add time munge to RBNB WebDAV requests
 * <p>
 * TimeDrive acts as either as a redirector or as a Web proxy for requests to
 * an RBNB WebDAV server.  User-specified "URL munge" parameters are added to
 * the received request.  In redirect mode (which is the default) TimeDrive
 * sends back a redirect HTTP message to the client and specifies the
 * resolved, munged URL.  In pass-through mode, TimeDrive directly makes the
 * request of the ultimate server and then passes the response back to the
 * original requestor.
 * <p>
 * This top-level class listens on a server socket, spawning a thread to handle
 * each request.
 *
 * @author John P. Wilson
 *
 * @version 09/14/2006
 */

/*
 * Copyright 2005, 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/14/2006  JPW	Add MULTIUSER_COMBO mode.
 * 09/05/2006  JPW	Different multi-user modes, specified by multiUserMode
 * 08/29/2006  JPW	Add multi-user support.  Each user (identified by a
 *			unique ID) will have their own copy of MungeParameters.
 *			These user IDs and MungeParameter objects are stored
 *			in Hashtable multiUserMP.
 * 07/20/2006  JPW	Add bPassThrough and bSecureRedirect, which are sent
 *			as parameters to SocketHandler.
 * 01/30/2006  JPW	The default ultimateServerHost is null; if no
 *			ultimateServerHost is provided, then this server must
 *			be specified in the request URL.
 * 11/20/2005  JPW	Created from EMF's WebCache application
 *
 */

public class TimeDrive {
  
  // JPW 09/05/2006: The various multi-user modes:
  public final static int MULTIUSER_OFF        = 1;
  public final static int MULTIUSER_IP         = 2;
  public final static int MULTIUSER_BASIC_AUTH = 3;
  public final static int MULTIUSER_COMBO      = 4;
  
  // The "utlimate server" host and port: this is the WebTurbine to which RBNB
  // requests will be sent off.  This is set with the "-u" command line
  // option.  If no ultimate server host is specified, then it is assumed that
  // this server will be specified in the URL from the browser.
  private String ultimateServerHost = null;
  private int ultimateServerPort = 80;
  
  // Socket port on which to listen for client requests
  private int serverSocket=6677;
  
  // Debug level for info printouts
  private int debug=0;
  
  // Shut the TimeDrive server down?
  private boolean shutdown=false;
  
  // JPW 08/29/2006: Each user has their own MungeParameters
  // Munge parameters; must synchronize access to this object
  // private MungeParameters mungeParameters = new MungeParameters();
  private Hashtable multiUserMP = new Hashtable();
  
  // Name of the time synchronization channel (used to fetch absolute start
  // and end time from the RBNB); the name of this channel is sent back with
  // the updated HTML code to the TimeDrive web interface browser
  private String syncChannel = "";
  
  // JPW 07/20/2006: Pass-through (proxy) data requests?
  //		     When this is true, data requests are made from
  //		     TimeDrive to the ultimate server and the data
  //		     response from the server is sent back to the
  //		     browser. This is the opposite of redirect mode.
  private boolean bPassThrough = false;
  
  // JPW 07/20/2006: Use "https" as the protocol in redirection URLs?
  private boolean bSecureRedirect = false;
  
  // Specify multi-user mode
  private int multiUserMode = MULTIUSER_COMBO;
  
  /**************************************************************************
   * Constructor.
   * <p>
   * Parse command line arguments and then call handleRequests() to manage
   * the server socket and requests.
   *
   * @author John P. Wilson
   *
   * @param debugI		  Debug level (indicated how much debug printout to provide)
   * @param syncChannelI	  Synchronization channel from which TimeDrive may fetching absolute times
   * @param serverSocketI	  Socket port on which to listen for client requests
   * @param ultimateServerHostI	  Name of the WebTurbine which will fulfill client requests
   * @param ultimateServerPortI	  Port of the WebTurbine which will fulfill client requests
   * @param bPassThroughI	  Pass-through (proxy) data requests?
   * @param bSecureRedirectI	  Use "https" as protocol in redirection URLs?
   * @param multiUserModeI	  Multi-user mode
   *
   * @version 09/05/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 09/05/2006  JPW	Add multiUserModeI
   * 07/20/2006  JPW	Add bPassThroughI and bSecureRedirectsI
   * 11/20/2005  JPW	Created
   *
   */
  
  public TimeDrive(
    int debugI,
    String syncChannelI,
    int serverSocketI,
    String ultimateServerHostI,
    int ultimateServerPortI,
    boolean bPassThroughI,
    boolean bSecureRedirectI,
    int multiUserModeI)
  {
    
    debug = debugI;
    syncChannel = syncChannelI;
    serverSocket = serverSocketI;
    ultimateServerHost = ultimateServerHostI;
    ultimateServerPort = ultimateServerPortI;
    
    // JPW 07/20/2006: Add these fields to support redirection logic
    bPassThrough = bPassThroughI;
    bSecureRedirect = bSecureRedirectI;
    
    // JPW 09/05/2006: Specify the multi-user mode
    if ( (multiUserModeI == MULTIUSER_OFF)        ||
	 (multiUserModeI == MULTIUSER_IP)         ||
         (multiUserModeI == MULTIUSER_BASIC_AUTH) ||
	 (multiUserModeI == MULTIUSER_COMBO) )
    {
	multiUserMode = multiUserModeI;
    }
    
    
    // If a reference/sync channel is provided, this would be a good point to
    // set the current server time equal to the newest time in the given
    // reference/sync channel.
    
    
    // Start server socket
    handleRequests();
    
  }
  
  /**************************************************************************
   * Set up a server socket to handle requests; for each request, spawn a new
   * SocketHandler thread to take care of the request and send a response
   * back to the original requestor.
   * <p>
   *
   * @author John P. Wilson
   *
   * @version 01/06/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/20/2005  JPW	Created
   *
   */
  
  public void handleRequests() {
    ServerSocket server=null;
    
    try {
      // Create Server socket with a timeout of 1 second and backlog of 100
      server = new ServerSocket(serverSocket, 100);
      server.setSoTimeout(1000);
      System.err.println("\n\nServer socket listening on port " + serverSocket);
      System.err.println("Debug level = " + debug);
      if ( (syncChannel != null) && (!syncChannel.equals("")) ) {
	  System.err.println("Sync channel = " + syncChannel);
      }
      System.err.println(
         "Ultimate server = " + ultimateServerHost + ":" + ultimateServerPort);
      if (bPassThrough) {
	  System.err.println(
	      "TimeDrive is in Pass Through mode (TimeDrive fulfills data " +
	      "requests and sends results back to client)");
      } else if (bSecureRedirect) {
	  System.err.println(
	      "TimeDrive is in Redirect mode (use HTTPS in redirection URLs)");
      } else {
	  System.err.println(
	      "TimeDrive is in Redirect mode (use HTTP in redirection URLs)");
      }
      if (multiUserMode == MULTIUSER_OFF) {
	  System.err.println(
	      "TimeDrive is in single-user mode (all users share one set of " +
	      "time parameters)");
      } else if (multiUserMode == MULTIUSER_IP) {
	  System.err.println(
	      "TimeDrive is in multi-user mode, based on client IP address");
      } else if (multiUserMode == MULTIUSER_BASIC_AUTH) {
	  System.err.println(
	      "TimeDrive is in multi-user mode, based on username/password");
      } else if (multiUserMode == MULTIUSER_COMBO) {
	  System.err.println(
	      "TimeDrive is in multi-user mode, based on both " +
	      "username/password and client IP address");
      }
      System.err.println("\n\n");
      
      //handle connections by spawning threads
      while (!shutdown) {
        try {
          Socket newConnect=server.accept();
	  // JPW 09/05/2006: Add multiUserMode argument
	  // JPW 07/20/2006: Add bPassThrough and bSecureRedirect arguments
	  SocketHandler sh =
	      new SocketHandler(
	          this,
		  newConnect,
		  ultimateServerHost,
		  ultimateServerPort,
		  bPassThrough,
		  bSecureRedirect,
		  multiUserMode,
		  debug);
	  sh.start();
        } catch (InterruptedIOException iie) {
          if (iie.getMessage().indexOf("timed out") == -1) {
            throw iie;
          }
        } catch (SocketException se) {
          if ( (se.getMessage().indexOf("Interrupted") == -1) &&
	       (se.getMessage().indexOf("timed out") == -1))
	  {
	      throw se;
	  }
        }
      }
    } catch (Exception e) {
      System.err.println("TimeDrive exception:\n" + e.getMessage());
      e.printStackTrace();
    }
    
    try {
	if (server!=null) {
	    server.close();
	    System.err.println(
	        "TimeDrive: released server socket on port " + server);
	    System.gc(); //system server socket not released on close;
            	         // garbage collector appears to run finalize and do better
	}
    } catch (Exception e) {
	System.err.println(
	    "Exception closing server socket: " + e.getMessage());
	e.printStackTrace();
    }
  } //end handleRequests method
  
  /**************************************************************************
   * Signal to shutdown the server socket.
   * <p>
   *
   * @author John P. Wilson
   *
   * @version 01/06/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/20/2005  JPW	Created
   *
   */
  
  public void doShutdown() {
      shutdown=true;
  }
  
  /**************************************************************************
   * Get the server socket port number.
   * <p>
   *
   * @author John P. Wilson
   *
   * @version 01/06/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/20/2005  JPW	Created
   *
   */
  
  public int getServerSocket() {
      return serverSocket;
  }
  
  /**************************************************************************
   * Get the MungeParameters object associated with the given user ID.
   * <p>
   * If this user does not currently have a MungeParameters object, then create
   * one and add it to Hashtable multiUserMP
   *
   * @author John P. Wilson
   *
   * @param idStrI	String that identifies the user.
   *
   * @version 08/29/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 08/29/2006  JPW	Created
   *
   */
  
  private synchronized MungeParameters getMungeParameters(String idStrI) {
      
      // If the ID is null, use a global ID
      if ( (idStrI == null) || (idStrI.trim().equals("")) ) {
	  idStrI = "__GLOBAL__";
      }
      
      MungeParameters mungeParameters =
          (MungeParameters)multiUserMP.get(idStrI);
      if (mungeParameters == null) {
	  // This must be a new user; add new MungeParameters object
	  mungeParameters = new MungeParameters();
	  multiUserMP.put(idStrI,mungeParameters);
	  // System.err.println(
	  //     "Add new user; Current Users:\n" + multiUserMP);
      }
      
      return mungeParameters;
      
  }
  
  /**************************************************************************
   * Get the munge string to be appended to requests sent on to the RBNB.
   * <p>
   *
   * @author John P. Wilson
   *
   * @param idStrI		String that identifies the user.
   * @param tempDurationI	The duration to use for this request.
   *
   * @version 11/13/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/13/2006  JPW	Change bSetDurationToZeroI to tempDurationI;
   *			tempDurationI will be the duration to use for this
   *			request only (don't set it as a permanent value in
   *			the MungeParameters).
   * 08/29/2006  JPW	Add multi-user support using the new idStrI parameter.
   * 01/26/2006  JPW	Created
   *
   */
  
  public synchronized String getMungeString(
	String idStrI, double tempDurationI)
  {
      MungeParameters mp = getMungeParameters(idStrI);
      return mp.getTimeMunge(tempDurationI);
  }
  
  /**************************************************************************
   * Set new values and then return a copy of the MungeParameters object.
   * <p>
   *
   * @author John P. Wilson
   *
   * @param idStrI		String that identifies the user.
   * @param referenceI		Time reference (oldest, absolute, etc)
   * @param timeI		Request data at this time
   * @param durationI		Request length
   * @param playModeI		Play direction (forward, pause, etc)
   * @param rateI		Play rate
   *
   * @version 08/29/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 08/29/2006  JPW	Add multi-user support using the new idStrI parameter.
   * 05/31/2006  JPW    play mode can now be "forward", "backward",
   *			"live", or "pause"
   * 11/20/2005  JPW	Created
   *
   */
  
  public synchronized MungeParameters setTimeMunge(
      String idStrI,
      String referenceI,
      double timeI,
      double durationI,
      String playModeI,
      double rateI)
  {
      
      MungeParameters mungeParameters = getMungeParameters(idStrI);
      
      // Convert play mode from a String to a constant value indicating the
      // direction of play
      // If a valid new play mode hasn't come in, maintain the current mode
      int playMode = mungeParameters.getPlayMode();
      // JPW 05/31/06: Add "live" to the list of possible play modes
      if (playModeI != null) {
	  if (playModeI.equals("forward")) {
	      playMode = MungeParameters.FORWARD;
	  } else if (playModeI.equals("backward")) {
	      playMode = MungeParameters.BACKWARD;
	  } else if (playModeI.equals("pause")) {
	      playMode = MungeParameters.PAUSE;
	  } else if (playModeI.equals("live")) {
	      playMode = MungeParameters.LIVE;
	  }
      }
      
      // Bring play time up to date using the current (not the new) play mode and rate.
      // JPW 01/26/2006: bChangeFromPlayToPause will be true if updatePlayTime()
      //                 needed to make a change from play mode to pause mode
      //                 (this would occur if we were trying to play into
      //                 the future).
      boolean bChangeFromPlayToPause = mungeParameters.updatePlayTime();
      
      /*
       * JPW 05/31/2006: Since we have added playMode = LIVE, instead of just
       *		 checking if playMode != PAUSE, we need to check if
       *		 playMode == FORWARD || playMode == BACKWARD
       *
      if ( bChangeFromPlayToPause &&
	   (playMode != MungeParameters.PAUSE) &&
           (timeI == Double.MAX_VALUE) )
      {
      */
      if ( bChangeFromPlayToPause                              &&
	   ( (playMode == MungeParameters.FORWARD) ||
             (playMode == MungeParameters.BACKWARD) )      &&
           (timeI == Double.MAX_VALUE) )
      {
	  // What the caller wanted to do was be in play mode and use
	  // TimeDrive's time (as indicated by the condition
	  // "timeI == Double.MAX_VALUE").  However, we can't do that because
	  // the return value from updatePlayTime() indicates that the server
	  // had to switch from play mode to step mode because time was going
	  // into the future.
	  // Therefore, keep playMode as PAUSE
	  playMode = MungeParameters.PAUSE;
      }
      
      mungeParameters.setTimeMunge(
          referenceI, timeI, durationI, playMode, rateI);
      
      // Return a copy of mungeParameters
      return getCopyOfMungeParameters(idStrI);
      
  }
  
  /**************************************************************************
   * Get a copy of the munge parameters object.
   * <p>
   *
   * @author John P. Wilson
   *
   * @param idStrI	String that identifies the user.
   *
   * @version 08/29/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 08/29/2006  JPW	Add multi-user support using the new idStrI parameter.
   * 11/20/2005  JPW	Created
   *
   */
  
  public synchronized MungeParameters getCopyOfMungeParameters(String idStrI) {
      
      MungeParameters mungeParameters = getMungeParameters(idStrI);
      
      // Calculate the current time (if playing through data)
      mungeParameters.updatePlayTime();
      return
          new MungeParameters(
	      mungeParameters.getReference(),
	      mungeParameters.getTime(),
	      mungeParameters.getDuration(),
	      mungeParameters.getPlayMode(),
	      mungeParameters.getRate(),
	      debug);
  }
  
  /**************************************************************************
   * Set a new sync channel.
   * <p>
   * An absolute time is obtained from the sync channel and used as the current
   * time in TimeDrive.  The format of this string should be something like:
   *
   *    "localhost/RBNB/SourceA/chan1"
   *
   * That is, this string shouldn't have any leading slashes or "http".
   *
   * @author John P. Wilson
   *
   * @param idStrI		String that identifies the user.  NOT CURRENTLY USED.
   * @param newSyncChannelI	The name of the new sync channel
   *
   * @version 08/29/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/20/2005  JPW	Created
   *
   */
  
  public synchronized void setSyncChannel(
      String idStrI,
      String newSyncChannelI)
  {
      if (newSyncChannelI != null) {
	  syncChannel = newSyncChannelI;
      }
  }
  
  /**************************************************************************
   * Get the current sync channel.
   * <p>
   *
   * @author John P. Wilson
   *
   * @param idStrI	String that identifies the user.  NOT CURRENTLY USED.
   *
   * @version 08/29/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/20/2005  JPW	Created
   *
   */
  
  public synchronized String getSyncChannel(String idStrI) {
      return syncChannel;
  }
  
}

