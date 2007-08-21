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


import com.rbnb.timedrive.TimeDrive;
import com.rbnb.utility.ArgHandler;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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
 *
 * @author John P. Wilson
 *
 * @version 09/05/2006
 */

/*
 * Copyright 2005, 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/05/2006  JPW	Add "-m" flag to specify single- or multi-user mode.
 * 07/20/2006  JPW	Add flags to support data request URL redirection,
 *			which will be the default mode: -P, -S.
 * 02/28/2006  JPW	Change default server port from 6677 to 4000.
 *			Default ultimate server set at localhost:80
 * 01/10/2006  JPW	Created
 *
 */

public class rbnbTimeDrive {
    
    /**************************************************************************
     * Main method. Parse command line arguments and create a TimeDrive object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param arg  argument list
     *
     * @version 09/05/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/05/2006  JPW  Add "-m" flag to specify single- or multi-user mode
     * 07/20/2006  JPW  Add -S and -P flags.  Also, the user cannot have
     *			a combination of both -S and -P; if they do, quit.
     * 02/28/2006  JPW	For working with the University students, MJM wanted
     *			the default ultimate server to point to localhost.
     * 01/30/2006  JPW	The default ultimateServerHost is null; therefore,
     *			if no "-u" command line option is used, then
     *			the ultimate server host:port must be specified
     *			in the request URL.
     * 01/27/2006  JPW	Fix bug parsing ultimate host name ("-u" flag)
     * 01/10/2006  JPW	Created
     *
     */
    
    public static void main(String[] arg) {
	
	// The "utlimate server" host and port: this is the WebTurbine to which
	// RBNB requests will be sent off.  This is set with the "-u" command
	// line option.
	// If the HTTP client is using a proxy, then the type of request
	// TimeDrive receives will be like:
	//     http://<server host>:<server port>/<request>
	// If ultimateServerHost is null, then SocketHandler will use the
	// server host and port from this request as the ultimate server host
	// and port.
	// String ultimateServerHost = null;
	// JPW 02/28/2006: For working with the University students, MJM
	//                 wanted the default ultimate server to point
	//                 to localhost.
	String ultimateServerHost = "localhost";
	int ultimateServerPort = 80;
	
	// Socket port to listen for client requests on
	int serverSocket = 4000;
	
	// Debug level for info printouts
	int debug = 0;
	
	// Name of the time synchronization channel (used to fetch absolute
	// start and end time from the RBNB); the name of this channel is sent
	// back with the updated HTML code to the TimeDrive web interface
	// browser
	String syncChannel = "";
	
	// JPW 07/20/2006: Pass-through (proxy) data requests?
	//		   When this is true, data requests are made from
	//		   TimeDrive to the ultimate server and the data
	//		   response from the server is sent back to the
	//		   browser. This is the opposite of redirect mode.
	boolean bPassThrough = false;
	
	// JPW 07/20/2006: Use "https" as the protocol in redirection URLs?
	boolean bSecureRedirect = false;
	
	// JPW 08/30/2006: Specify the multi-user mode; this can be specified
	//		   by the "-m" flag
	int multiUserMode = TimeDrive.MULTIUSER_COMBO;
	
	try {
	    ArgHandler ah=new ArgHandler(arg);
	    
	    // JPW 07/20/2006: Make sure that both -P and -S are not specified
	    if ( (ah.checkFlag('P')) && (ah.checkFlag('S')) ) {
		throw new Exception(
		    "The \"-S\" flag specifies secure (https) redirection " +
		    "requests.\nIt cannot be used with the \"-P\" flag, " +
		    "which specifies pass through mode (the opposite of " +
		    "redirection mode).");
	    }
	    
	    // Debug level
	    if (ah.checkFlag('d')) {
		debug=Integer.parseInt(ah.getOption('d'));
	    }
	    // Print help message
	    if (ah.checkFlag('h')) {
		throw new Exception("help");
	    }
	    // JPW 09/05/2006: Specify the multi-user mode
	    if (ah.checkFlag('m')) {
		int tempInt = Integer.parseInt(ah.getOption('m'));
		if ( (tempInt == TimeDrive.MULTIUSER_OFF)        ||
		     (tempInt == TimeDrive.MULTIUSER_IP)         ||
		     (tempInt == TimeDrive.MULTIUSER_BASIC_AUTH) ||
		     (tempInt == TimeDrive.MULTIUSER_COMBO) )
		{
		    multiUserMode = tempInt;
		} else {
		    System.err.println(
			"ERROR: multiuser mode (-m " +
			tempInt +
			") out of range; using default (" +
			multiUserMode + ")");
		}
	    }
	    // JPW 07/20/2006: Instead of sending the client a redirection,
	    //     have TimeDrive pass-through and handle the data requests
	    if (ah.checkFlag('P')) {
		bPassThrough = true;
	    }
	    // Reference/sync channel
	    if (ah.checkFlag('r')) {
		syncChannel = ah.getOption('r');
		if (syncChannel == null) {
		    syncChannel = "";
		}
	    }
	    // Server socket
	    if (ah.checkFlag('s')) {
		String socketString=ah.getOption('s');
		if (socketString!=null) {
		    serverSocket=Integer.parseInt(ah.getOption('s'));
		}
	    }
	    // JPW 07/20/2006: Specify https for redirection URLs
	    if (ah.checkFlag('S')) {
		bSecureRedirect = true;
	    }
	    // Ultimate server host:port (address of the Tomcat/RBNB server)
	    if (ah.checkFlag('u')) {
		String ultimateServer=ah.getOption('u');
		if ( (ultimateServer != null) && (!ultimateServer.equals("")) )
		{
		    int colonIdx = ultimateServer.indexOf(':');
		    if (colonIdx < 0) {
			// No port given, use 80
			ultimateServerHost = ultimateServer;
			ultimateServerPort = 80;
		    } else if (colonIdx == 0) {
			// No hostname given, use "localhost"
			ultimateServerHost = "localhost";
		        ultimateServerPort =
			    Integer.parseInt(
			        ultimateServer.substring(colonIdx + 1));
		    } else if (colonIdx >= 0) {
		        ultimateServerHost =
			    ultimateServer.substring(0,colonIdx);
		        ultimateServerPort =
			    Integer.parseInt(
			        ultimateServer.substring(colonIdx+1));
		    }
		}
	    }
	} catch (Exception e) {
	    if ( (e.getMessage() != null) && (!e.getMessage().equals("help")) ) {
		System.err.println("Argument parsing exception: "+e.getMessage());
		e.printStackTrace();
	    }
	    System.err.println("TimeDrive");
	    System.err.println("  -d <level>                      : print debug info to console");
	    System.err.println("                          default : 0 (no debug)");
	    System.err.println("  -h                              : print this usage info");
	    System.err.println("  -m < 1 | 2 | 3 | 4 >            : Specify the multi-user mode:");
	    System.err.println("                                  : 1 = OFF; single-user mode; all users share one set of time parameters");
	    System.err.println("                                  : 2 = IP ONLY: multi-user based on the client IP address");
	    System.err.println("                                  : 3 = USERNAME/PASSWORD ONLY: multi-user based on username/password (HTTP basic authorization)");
	    System.err.println("                                  : 4 = COMBINATION: multi-user based on combination of IP address and username/password");
	    System.err.println("                          default : 4 (COMBINATION of username/password and IP address)");
	    System.err.println("  -P                              : Pass-through (proxy) data requests to the ultimate server (the opposite of \"redirection\" mode).");
	    System.err.println("  -r <reference channel>          : RBNB reference/sync channel; for example \"-r localhost/RBNB/SourceA/chan1\"");
	    System.err.println("  -s <server socket>              : specify the socket TimeDrive will listen on");
	    System.err.println("                          default : 4000");
	    System.err.println("  -S                              : Use https in redirection URLs. (NOTE: Cannot be combined with the \"-P\" flag.)");
	    System.err.println("  -u <host><:port>                : WebTurbine server to which requests will be passed on");
	    // JPW 02/28/2006: For working with the University students, MJM
	    //                 wanted the default ultimate server to point
	    //                 to localhost.
	    System.err.println("                          default : localhost:80");
	    // System.err.println("                          default : The ultimate server host:port must be specified in the request URL");
	    
	    System.exit(0);
	}
	
	// Create TimeDrive object
	new TimeDrive(
	    debug,
	    syncChannel,
	    serverSocket,
	    ultimateServerHost,
	    ultimateServerPort,
	    bPassThrough,
	    bSecureRedirect,
	    multiUserMode);
	
    }
    
}

