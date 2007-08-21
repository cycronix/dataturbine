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

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import javax.imageio.ImageIO;

import com.rbnb.utility.ParseURL;

/******************************************************************************
 * Handle one user request.
 * <p>
 * This class processes one user request.  Various requests are supported:
 *
 * 1. Request an image which contains the current TimeDrive time. The request
 *    would look something like:
 *    http://<TimeDrive host>:<TimeDrive port>/time.jpg
 *
 * 2. Request data from an RBNB.  The request would look something like:
 *    http://<TimeDrive host>:<TimeDrive port>/RBNB/<Source>/<Channel>
 *    This request will be transformed into a request to the appropriate
 *    ultimate server, with an added munge, as follows:
 *    http://<ultimateServerHost>:<ultimateServerPort>/RBNB/<Source>/<Channel>?<munge parameters string>
 *    If redirects are turned off, SocketHandler will send this request to
 *    the ultimate server, and then send the ultimate server's response back
 *    to the original requestor.  If redirects are turned on, TimeDrive
 *    responds to the client with an HTTP redirect, where the HTTP header
 *    contains a "Location" field which specifies the resolved, munged URL.
 *
 * 3. Request the TimeDrive Web interface page with one of the following
 *    types of requests:
 *    http://<TimeDrive host>:<TimeDrive port>/
 *    http://<TimeDrive host>:<TimeDrive port>/index.html
 *    http://<TimeDrive host>:<TimeDrive port>/timedrive.html
 *
 * 4. Request that the current time in TimeDrive be set to a time value
 *    obtained from a reference/sync channel.  Request would look like:
 *    http://<TimeDrive host>:<TimeDrive port>/updatetime/<full chan name>?r=<reference>&dt=string&f=t
 *    Note that there is an implied "t=0" in this request. The "reference" will
 *    either be "newest" or "oldest".  An example of this request is:
 *    http://<TimeDrive host>:<TimeDrive port>/updatetime/localhost/RBNB/Video/WRBV.jpg?r=oldest&dt=string&f=t
 *    When TimeDrive receives this request, it will in turn send out the
 *    following request to Tomcat in order to get the desired time:
 *    GET localhost/RBNB/Video/WRBV.jpg?r=oldest&dt=string&f=t
 *    The data (in this case, the oldest time on the WRBV.jpg channel) is
 *    received back from Tomcat and set as the current time in TimeDrive.
 *
 * 5. Request that TimeDrive update its time to be the current system time.
 *    Request would look like:
 *    http://<TimeDrive host>:<TimeDrive port>/updatetocurrenttime
 *
 * 6. Request that TimeDrive update its munge parameters.
 *    Values are parsed from the request string and set in MungeParameters.
 *    The request can have 2 forms:
 *    (a) http://<TimeDrive host>:<TimeDrive port>/?<new munge>
 *        We will return a small KML document from this type of request.
 *        This request may come from a Google Earth placemark and be used to
 *        set an initial time for looking at a track.
 *    (b) http://<TimeDrive host>:<TimeDrive port>/updatemunge?reference=<new reference>&time=<new time>&duration=<new duration>&play=<new play mode>
 *        This is the form of the request made from the TimeDrive web interface.
 *
 * @author John P. Wilson
 *
 * @version 11/13/2006
 */

/*
 * Copyright 2005, 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/13/2006  JPW	In handleDataRequest(): No longer save time request
 *			parameters from a request URL; these will only be used
 *			for the current request, not saved for further use in
 *			TimeDrive.
 * 09/29/2006  JPW	Made the following change in handleDataRequest():
 *			If the user has included the munge "z=f" then don't
 *			force duration to zero.  If they have included the
 *			munge "z=t" then force the duration to zero, whether or
 *			not there is a ".<ext>" or not at the end of the
 *			request channel.
 * 09/05/2006  JPW	Support two multi-user modes:
 *			1. Based on the client IP address
 *			2. Using HTTP Basic Authorization (username/password)
 * 08/29/2006  JPW	Add support for Basic Authorization
 *			Add multi-user support; this is implemented by
 *			sending all TimeDrive methods a copy of the user's
 *			ID string.
 *			TimeDrive will maintain a Hashtable of MungeParameter
 *			objects key'ed off this ID string.
 * 07/20/2006  JPW	Add "redirect" mode, which will be the default mode.
 *			Only used for data requests (handleDataRequest()).
 *			In this mode, TimeDrive responds to data requests with
 *			an HTTP 303 (See Other), which is a redirect.  The
 *			HTTP header contains a "Location" field with the
 *			munged redirection URL.
 * 06/02/2006  JPW	Replace parseMungeParameters(), breakupMungeOptions(),
 *			and the use of RawMungeParameters with ParseURL
 * 05/03/2006  JPW	Remove the restriction that a data request must
 *			include "RBNB" or "rbnbNet" in the request string.
 * 11/20/2005  JPW	Created from EMF's WebCache application
 *
 */

public class SocketHandler extends Thread {
  
  // WebTurbine server to which requests will be passed on
  private String ultimateServerHost=null;
  private int ultimateServerPort=80;
  
  // JPW 01/30/2006: If no ultimateServer host has been specified in the
  //                 constructor, then bUltimateServerInURL will be true and
  //                 we must find the ultimate server in the browser's
  //                 request URL
  // private boolean bUltimateServerInURL = false;

  // JPW 05/09/2006: bUltimateServerInURL replaced by bUltimateHostSpecified
  private boolean bUltimateHostSpecified = false;
  
  ////////////////////////////////////////////////////
  // The following objects are used to communicate with the browser:
  // read the request from the browser and write the response back
  // to the browser
  
  // We get input and output streams from this Socket in order
  // to communicate with the browser
  private Socket browser=null;
  
  // bpos and bpis are used to read the user's request from the browser socket
  private PipedOutputStream bpos=null;
  private PipedInputStream bpis=null;
  
  // browser socket reader; used to read the request from the browser Socket
  private SocketReader bsr=null;
  
  // browser output stream; send the response to the browser by writing to bos
  private OutputStream bos=null;
  
  //
  // End of browser communication objects
  ////////////////////////////////////////////////////
  
  
  
  ////////////////////////////////////////////////////
  // The following objects are used to communicate with the Tomcat/Web
  // server (to fulfill an RBNB data request).
  
  // We get input and output streams from this Socket in order
  // to communicate with the Tomcat/Web server
  private Socket server=null;
  
  // server output stream; we send the request to the Tomcat/Web server by
  // writing it to sos
  private OutputStream sos=null;
  
  // server socket reader; used to read the Tomcat/Web server's response
  // to our request
  private SocketReader ssr=null;
  
  // spos and spis are used to read the Tomcat/Web server's response
  private PipedOutputStream spos=null;
  private PipedInputStream spis=null;
  
  //
  // End of Tomcat server communication objects
  ////////////////////////////////////////////////////
  
  // Debug level for info prints
  private int debug = 0;
  
  // The browser's request
  private Request request = null;
  
  // Response to send back to the browser
  private Response response = null;
  
  // TimeDrive instance; we get the MungeParameters via this object
  private TimeDrive timeDrive = null;
  
  // JPW 07/20/2006: Pass-through (proxy) data requests?
  //		     When this is true, data requests are made from
  //		     TimeDrive to the ultimate server and the data
  //		     response from the server is sent back to the
  //		     browser. This is the opposite of redirect mode.
  private boolean bPassThrough = false;
  
  // JPW 07/20/2006: Use "https" as the protocol in redirection URLs?
  private boolean bSecureRedirect = false;
  
  // For multi-user modes, the client ID string; this can either be an encoded
  // HTTP Basic Authorization username/password string or simply the client's
  // IP address
  private String idStr = "foo";
  
  // JPW 09/05/2006: Specify multi-user mode
  private int multiUserMode = TimeDrive.MULTIUSER_IP;
  
  /**************************************************************************
   * Constructor.  This class handles one request from a browser.
   * <p>
   *
   * @author John P. Wilson
   *
   * @param timeDriveI		  TimeDrive instance.
   * @param browserI		  Socket for reading from/writing to the browser.
   * @param ultimateServerHostI	  Tomcat server host.
   * @param ultimateServerPortI	  Tomcat server port.
   * @param bPassThroughI	  Pass-through (proxy) data requests?
   * @param bSecureRedirectI	  Use "https" as protocol in redirection URLs?
   * @param multiUserModeI	  Multi-user mode
   * @param debugI		  Debug level for info print statements.
   *
   * @version 09/05/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 09/05/2006  JPW    Add multiUserModeI
   * 07/20/2006  JPW	Add bPassThroughI and bSecureRedirectsI
   * 11/20/2005  JPW	Created
   *
   */
  
  public SocketHandler(
      TimeDrive timeDriveI,
      Socket browserI,
      String ultimateServerHostI,
      int ultimateServerPortI,
      boolean bPassThroughI,
      boolean bSecureRedirectI,
      int multiUserModeI,
      int debugI)
  {
    
    timeDrive = timeDriveI;
    browser=browserI;
    ultimateServerHost = null;
    bUltimateHostSpecified = false;
    if ( (ultimateServerHostI != null) &&
         (!ultimateServerHostI.trim().equals("")) )
    {
	ultimateServerHost = ultimateServerHostI;
	bUltimateHostSpecified = true;
    }
    ultimateServerPort = ultimateServerPortI;
    bPassThrough = bPassThroughI;
    bSecureRedirect = bSecureRedirectI;
    multiUserMode = multiUserModeI;
    debug = debugI;
    
  }
  
  /**************************************************************************
   * Read and handle the request from the browser.
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
  
  public void run() {
    boolean done=false;
    int num=0;
    byte[] header=null;
    byte[] content=null;
    boolean toCache=false;
    boolean fromCache=false;
    boolean bUpdateMungeParameters=false;

    //set up streams
    if (browser!=null) {
      try {
        bpos=new PipedOutputStream();
        bpis=new PipedInputStream(bpos);
	// Read from the browser Socket and fill up bpos;
	// we then read from bpis
        bsr = new SocketReader(browser,bpos,bpis,debug,"browser");
        bos=browser.getOutputStream();
      } catch (Exception e) {
        System.err.println(
	    "Exception getting streams for client socket: "+e.getMessage());
        e.printStackTrace();
      }
    }
    
    try {
      //read request from browser
      try {
      if (browser!=null) {
        // Read bpis to get the browser Request
        request=new Request(bpis,debug,"browser",bUltimateHostSpecified);
	if (debug == 3) {
	    System.err.println("request: " + request.getPath());
	} else if (debug > 3) {
	    System.err.println("request:\n"+request.getRequest());
	}
      }
      } catch (Exception e) {
        if (!(e instanceof EOFException)) {
          System.err.println("Exception reading request:\n" + e.getMessage());
          e.printStackTrace();
	}
        throw e;
      }
      
    // Figure out what kind of request we have been given
    if ( (browser != null) &&
	 (!request.doNullResponse()) &&
         (request.getCommand().equals("GET")) &&
	 (request.getProtocol().equals("http")) )
    {
	idStr =
	    request.getUserID(
		multiUserMode, browser.getInetAddress().getHostAddress());
	if ( ( (multiUserMode == TimeDrive.MULTIUSER_BASIC_AUTH) ||
	       (multiUserMode == TimeDrive.MULTIUSER_COMBO) )        &&
	     (idStr == null) )
	{
	    // Send back HTTP 401; client will request
	    // user to enter username/password
	    try {
		requestBasicAuthorization();
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
			"Exception requesting authorization:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if (request.getPath().startsWith("/time.jpg"))
	{
	    // This is a request for JPEG image containing current server time
	    try {
		handleTimeRequest("jpg");
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception fulfilling time image request:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if (request.getPath().startsWith("/time.png"))
	{
	    // This is a request for PNG image containing current server time
	    try {
		handleTimeRequest("png");
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception fulfilling time image request:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if (request.getPath().startsWith("/time.gif"))
	{
	    // This is a request for GIF image containing current server time
	    try {
		handleTimeRequest("gif");
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception fulfilling time image request:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	// JPW 01/27/2006: If there is an empty request string, or a request
	//                 for index.html, or a request for timedrive.html:
	//                 in all cases return time drive web interface page
	else if ( request.getPath().equals("/") ||
	    	  request.getPath().startsWith("/index.html") ||
	          request.getPath().startsWith("/timedrive.html") )
	{
	    // User is requesting the TimeDrive Web interface HTML page
	    try {
		handleHTMLRequest();
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println("Exception sending HTML page:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if (request.getPath().startsWith("/updatetime/"))
	{
	    // User is requesting that the current server time be set to some
	    // time value which will be returned from an RBNB request to a
	    // a reference channel
	    String absTimeRequestString = "/updatetime/";
	    try {
		String timeRequestURL =
		    "http://" +
		    request.getPath().substring(
		    absTimeRequestString.length());
		handleUpdateAbsoluteTimeRequest(timeRequestURL);
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception updating time using reference channel:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if (request.getPath().startsWith("/updatetocurrenttime"))
	{
	    // User is requesting that the current server time be set to the
	    // server's current system clock time
	    try {
		handleUpdateToCurrentTime();
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception updating to current system time:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if (request.getPath().startsWith("/updatemunge"))
	{
	    // Parse new munge parameters out of URL
	    try {
		handleUpdateMungeRequest(false);
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception updating munge parameters:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	else if ( (request.getPath().startsWith("/?")) ||
	          (request.getPath().startsWith("/@")) )
	{
	    // This request is a special key code indicating that there are
	    // new munge parameters to parse and process;
	    // we send back the TimeDrive web interface HTML page.
	    try {
		handleUpdateMungeRequest(true);
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println(
		        "Exception updating munge parameters:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	// JPW 05/03/2006: Remove the restriction that a data request must
	//		   include "RBNB" or "rbnbNet" in the request string.
	// else if ( (request.getPath().indexOf("rbnbNet") > -1) ||
	//           (request.getPath().indexOf("RBNB") > -1) )
	else
	{
	    // This is a request for data from the RBNB; we will add a munge
	    // to this request, send the request on to the RBNB, and then
	    // send the response back to the original requestor.
	    try {
		handleDataRequest();
	    } catch (Exception e) {
		if ( (!e.getMessage().equals("Socket closed")) ||
		     (debug > 2) )
		{
		    System.err.println("Exception fulfilling data request:");
		    e.printStackTrace();
		}
		throw e;
	    }
	}
	// JPW 05/03/2006: Remove the stub response; as you can see above,
	//                 the default case in this "if...else if...else"
	//                 is to treat the request as a data request.
	// else {
	//   try {
	// 	// Unknown request; just send a stub response back to browser
	// 	sendStubResponse();
	//   } catch (Exception e) {
	// 	if ( (!e.getMessage().equals("Socket closed")) ||
	// 	     (debug > 2) )
	// 	{
	// 	    System.err.println(
	// 	        "Exception sending stub response back to browser:");
	// 	    e.printStackTrace();
	// 	}
	// 	throw e;
	//   }
	// }
    } else {
	bUpdateMungeParameters = false;
	if (debug > 0) {
	    System.err.println(
	        "Not able to handle request:\n" + request.getRequest());
	}
    }
    
    // all done, by error or completion of work, so close all streams
    //if (bos != null) bos.close();
    //if (bsr != null) bsr.close();
    //if (sos != null) sos.close();
    //if (ssr != null) ssr.close();
    // JPW 12/05/2005: just close client Sockets
    if (browser != null) {
	// System.err.println("CLOSE BROWSER SOCKET");
	browser.close();
    }
    if (server != null) {
	// System.err.println("CLOSE SERVER SOCKET");
	server.close();
    }
    
    } catch (Exception e) {
	
	if (debug > 2) {
	    e.printStackTrace();
	}
	try {
	    // JPW 12/05/2005: just close client Sockets
	    if ( (browser != null) && (!browser.isClosed()) ) {
		// System.err.println("CLOSE BROWSER SOCKET");
		browser.close();
	    }
	    if ( (server != null) && (!server.isClosed()) ) {
		// System.err.println("CLOSE SERVER SOCKET");
		server.close();
	    }
	} catch (Exception ee) {
	    if (debug > 2) {
		System.err.println(
		    "Excpetion closing sockets: "+ee.getMessage());
		ee.printStackTrace();
	    }
	}
    }
  }
  
  /**************************************************************************
   * Request Basic Authorization from the client
   * <p>
   *
   * @author John P. Wilson
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
  
  private void requestBasicAuthorization() throws Exception {
	
	String htmlCode =
	    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">\n" +
	    "<HTML>\n" +
	    "<HEAD>\n" +
	    "<TITLE>TimeDrive username/password</TITLE>\n" +
	    "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=ISO-8859-1\">\n" +
	    "</HEAD>\n" +
	    "<BODY>\n" +
	    "<H1>TimeDrive username and optional password.</H1><br>\n" +
	    "Join a TimeDrive session by entering a username (your choice) " +
	    "and an optional password.<br><br>" +
	    "To support multiple users independently TimeDriving, we " +
	    "require you to enter your choice of username (and optional " +
	    "password). This is not a user account.  Each time you start " +
	    "a new TimeDrive session, you may select a different " +
	    "username and password combination. NOTE: The username and " +
	    "password are transmitted as plain text.\n" +
	    "</BODY>\n" +
	    "</HTML>";
	
	byte[] htmlBytes = htmlCode.getBytes();
	
	if (debug > 0) {
	    System.err.println(
		"Request username/password ID information from the user");
	}
	
	// Current time
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	Date date = new Date();
	String dateString = sdf.format(date);
	// Put together a response to send to browser
	String headerStr =
	    "HTTP/1.0 401 Unauthorised\n" +
	    "Server: TimeDrive\n" +
	    "Date: " +  dateString + "\n" +
	    "WWW-Authenticate: Basic realm=\"Unique TimeDrive identity\"\n" +
	    // "WWW-Authenticate: Basic realm=\"Join a TimeDrive session by entering a username (your choice).  Password is optional.\"\n" +
	    "Content-Type: text/html\n" +
	    "Content-Length: " + htmlBytes.length + "\n" +
	    // Have the last line of the header blank
	    "\n";
        String responseStr = headerStr + htmlCode;
        // Write response back to browser
        response =
	    new Response(
	        new ByteArrayInputStream(responseStr.getBytes()), bos, debug);
	
	if (debug > 3) System.err.println(response.getHeader());
	
  }
  
  /**************************************************************************
   * The browser is requesting an image which contains the current TimeDrive
   * time.
   * <p>
   * The request would look something like:
   * http://<TimeDrive host>:<TimeDrive port>/time.jpg
   * <p>
   *
   * @author John P. Wilson
   *
   * @version 09/28/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 09/28/2006  JPW    Parse updated munge parameters from URL request path
   * 11/20/2005  JPW	Created
   *
   */
  
  private void handleTimeRequest(String formatI) throws Exception {
	
      	// JPW 09/28/2006: Parse updated munge parameters from the path
	ParseURL parseURL = new ParseURL(request.getPath(), false);
	Hashtable nonRBNBMunge = parseURL.getNonRBNBMunge();
	String playMode = null;
	double rate = Double.MAX_VALUE;
	if (nonRBNBMunge != null) {
	    playMode = (String)nonRBNBMunge.get("play");
	    String rateStr = (String)nonRBNBMunge.get("rate");
	    if ( (rateStr != null) && (!rateStr.trim().equals("")) ) {
		try {
		    rate = Double.parseDouble(rateStr);
		} catch (NumberFormatException nfe) {
		    // Nothing to do; other garbage was in the "rate" field
		}
	    }
	}
	MungeParameters mp =
	    timeDrive.setTimeMunge(
	        idStr,
		(parseURL.getReference() != null) ? parseURL.getReference() : "absolute",
		(parseURL.getTime() != null) ? parseURL.getTime().doubleValue() : Double.MAX_VALUE,
		(parseURL.getDuration() != null) ? parseURL.getDuration().doubleValue() : Double.MAX_VALUE,
		playMode,
		rate);
      	
	/////////////////////////////////////////////////
      	// Send back an image containing the current time
	/////////////////////////////////////////////////
	
	// time, in seconds
	double time = mp.getTime();
	
	// JPW 09/28/2006: We used to use "t=0.0" as a special case to
	//                 indicate live mode; now live mode is indicated
	//                 directly by the value of playMode
	// if (time == 0.0) {
	//     time = ((double)System.currentTimeMillis()) / 1000.0;
	// }
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	// Date needs time in msec since 1970
	Date date = new Date( (long)(time * 1000.0) );
	String dateString = sdf.format(date);
	
      	int width = 250;
        int height = 35;
	
        // Create a buffered image in which to draw
        BufferedImage bufferedImage =
	    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
	
        // Write the time into the graphics context
	Font font = new Font("SansSerif", Font.BOLD, 14);
	g2d.setFont(font);
	FontMetrics fontMetrics = g2d.getFontMetrics(font);
	int xOffset = (int)((width - fontMetrics.stringWidth(dateString)) / 2);
	int verticalSpace = height - fontMetrics.getAscent();
	int yOffset = height - (int)(verticalSpace / 2);
        // g2d.setColor(Color.black);
        // g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.yellow);
        g2d.drawString(dateString, xOffset, yOffset);
	
        // Graphics context no longer needed so dispose it
        g2d.dispose();
	
	// Write the image data to a byte array
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// Check to see if the writer supports the desired format
	String[] formatNames = ImageIO.getWriterFormatNames();
	boolean bFormatSupported = false;
	for (int i = 0; i < formatNames.length; ++i) {
	    if (formatNames[i].toLowerCase().equals(formatI)) {
		bFormatSupported = true;
		break;
	    }
	}
	if (bFormatSupported) {
	    ImageIO.write(bufferedImage, formatI, baos);
	} else {
	    String newFormat = formatNames[0];
	    System.err.println(
	        "Image format \"" +
		formatI +
		"\" is not supported.\n" +
		"Using format \"" +
		newFormat +
		"\" instead.");
	    ImageIO.write(bufferedImage, newFormat, baos);
	}
	byte[] imageBytes = baos.toByteArray();
	
	if (debug > 0) {
	    System.err.println(
	        "Time image request: " +
		request.getPath() +
		"  Date: " +
		dateString +
		"  Image size: " +
		imageBytes.length);
	}
	
	// Current time
	date = new Date();
	dateString = sdf.format(date);
	
	// HTTP Header
	String headerStr =
	    "HTTP/1.1 200 OK\n" +
	    "Server: TimeDrive\n" +
	    "Pragma: no-cache\n" +
	    "Cache-Control: no-cache\n" +
	    "Expires: -1\n" +
	    "Last-Modified: " + Long.toString(date.getTime()) + "\n" +
	    "Duration: 0.0\n" +
	    "Content-Length: " + imageBytes.length + "\n" +
	    "Date: " +  dateString + "\n" +
	    // Have the last line of the header blank
	    "\n";
        byte[] headerBytes = headerStr.getBytes();
	
	byte[] responseBytes =
	    new byte[ headerBytes.length + imageBytes.length ];
	System.arraycopy(
	    headerBytes,0,responseBytes,0,headerBytes.length);
	System.arraycopy(
	    imageBytes,0,responseBytes,headerBytes.length,imageBytes.length);
	
        // Write response back to browser
        response =
	    new Response(new ByteArrayInputStream(responseBytes), bos, debug);
	
	if (debug > 3) System.err.println(response.getHeader());
	
  }
  
  /**************************************************************************
   * The browser is requesting data from an RBNB.
   * <p>
   * The request would look something like:
   * http://<TimeDrive host>:<TimeDrive port>/RBNB/<Source>/<Channel>
   * This request will be transformed into a request to the appropriate
   * ultimate server, with an added munge, as follows:
   * http://<ultimateServerHost>:<ultimateServerPort>/RBNB/<Source>/<Channel>@<munge parameters string>
   * <p>
   * If redirects are turned off, SocketHandler will send this request to
   * the ultimate server, and then send the ultimate server's response back
   * to the original requestor.  If redirects are turned on, TimeDrive
   * responds to the client with an HTTP redirect, where the HTTP header
   * contains a "Location" field which specifies the resolved, munged URL.
   * <p>
   *
   * @author John P. Wilson
   *
   * @version 11/13/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 11/13/2006  JPW	No longer save time request parameters from a request;
   *			these will only be used for the current request, not
   *			saved for further use in TimeDrive.
   * 09/29/2006  JPW	If the user has included the munge "z=f" then don't
   *			force duration to zero.  If they have included the
   *			munge "z=t" then force the duration to zero, whether or
   *			not there is a ".<ext>" or not at the end of the
   *			request channel.
   * 07/20/2006  JPW	Add support for sending back a redirect to the client.
   * 11/20/2005  JPW	Created
   *
   */
  
  private void handleDataRequest() throws Exception {
	
	//////////////////////////////
	//create server socket streams
	//////////////////////////////
	/*
	 JPW 07/20/2006: Only connect to the ultimate server if bPassThrough
	                 is true.
	 */
	if (bPassThrough) {
	    try {
		if (ultimateServerHost!=null) {
		    server=new Socket(ultimateServerHost,ultimateServerPort);
		} else {
		    server=new Socket(request.getHost(),request.getPort());
		}
		spos=new PipedOutputStream();
		spis=new PipedInputStream(spos);
		// Read response from the Tomcat server and write it to spos;
		// read this response (at the other end of the pipe) from spis
		ssr=new SocketReader(server,spos,spis,debug,request.getPath());
		// To write to the Tomcat server
		sos=server.getOutputStream();
	    } catch (Exception e) {
		if (ultimateServerHost!=null) {
		    System.err.println(
			"Exception getting streams for server " +
			ultimateServerHost +
			":" +
			ultimateServerPort +
			" " +
			e.getMessage());
		} else {
		    System.err.println(
			"Exception getting streams for server " +
			request.getHost() +
			":" +
			request.getPort() +
			" " +
			e.getMessage());
		}
		throw e;
	    }
	}
	
	////////////////////
	// Deal with munging
	////////////////////
	
	// Full path: made up of the request + munge
	String pathStr = request.getPath();
	// The channel request string from the request URL
	String requestStr = null;
	// The munge string from the request URL
	String browserMungeStr = null;
	// The final munge string
	String mungeStr = null;
	
	// Google Earth (and possibly some other browsers) preprocess the
	// URL by replacing the following characters with their corresponding
	// HEX codes:
	//	GE REPLACES	WITH
	//	   '&'		%26
	//	   '@'		%40
	//	   '='		%3D
	// Translate these HEX codes back to their appropriate characters
	
	//////////////////////////////////////////////////////////////////////
	// NOTE: This will cause a problem if there is an encoded string which
	//       is the value for an RBNB "msg" munge in the input request.
	//       To get around this, at some point I could add the following:
	//       1. Peel out "msg=<encoded str>" from the path
	//       2. Do a standard decode on path
	//       3. Put "msg=<encoded str>" back into the path
	//////////////////////////////////////////////////////////////////////
	
	String newPathStr = pathStr.replaceAll("%26","&");
	newPathStr = newPathStr.replaceAll("%40","@");
	newPathStr = newPathStr.replaceAll("%3D","=");
	newPathStr = newPathStr.replaceAll("%3d","=");
	if (!newPathStr.equals(pathStr)) {
	    // The path string has changed; must reprocess it in the
	    // Request object
	    request.changePath(newPathStr);
	    pathStr = request.getPath();
	}
	
	ParseURL parseURL = new ParseURL(pathStr,false);
	requestStr = parseURL.getRequest();
	if (requestStr == null) {
	    throw new Exception(
		"ERROR: Request string is all munge, no RBNB request.");
	}
	
	// Make sure requestStr has the appropriate leading characters,
	// just as pathStr does
	if ( (pathStr.charAt(0) == '/') && (requestStr.charAt(0) != '/') )
	{
	    // ParseURL removed the leading slash from the stored request.
	    requestStr = "/" + requestStr;
	}
	else if ( pathStr.startsWith("http://") &&
	          !requestStr.startsWith("http://") )
	{
	    requestStr = "http://" + requestStr;
	}
	
	//////////////////////////////////////////////////
	// Save time and duration from the request's munge
	//////////////////////////////////////////////////
	double tempDuration =
	    (parseURL.getDuration() != null) ?
	    parseURL.getDuration().doubleValue() :
	    Double.MAX_VALUE;
	if ( (tempDuration < 0.0) || (tempDuration >= Double.MAX_VALUE) ) {
	    tempDuration = Double.MAX_VALUE;
	}
	double tempTime =
	    (parseURL.getTime() != null) ?
	    parseURL.getTime().doubleValue() :
	    Double.MAX_VALUE;
	String tempReference = parseURL.getReference();
	
	/*
	 * JPW 11/13/2006: No longer save time request parameters from a
	 *                 request; these will only be used for the current
	 *                 request, not saved for further use in TimeDrive.
	 *
	// Save time only if "r=absolute" (explicit or implied)
	if ( ((tempReference == null) || (tempReference.equals("absolute"))) &&
	     (tempTime < Double.MAX_VALUE) )
	{
	    // Time reference is "absolute"; save time value
	    // Go into "pause" mode
	    timeDrive.setTimeMunge(
	        idStr,
		"absolute",
		tempTime,
		tempDuration,
		"pause",
		Double.MAX_VALUE);
	}
	else {
	    // Don't save time value; only save duration
	    timeDrive.setTimeMunge(
	        idStr,
		null,
		Double.MAX_VALUE,
		tempDuration,
		null,
		Double.MAX_VALUE);
	}
	*
	*/
	
	// JPW 09/29/2006: If the user has included the munge "z=f" then
	//                 don't force duration to zero.  If they have included
	//                 the munge "z=t" then force the duration to zero,
	//                 whether or not there is a ".<ext>" or not at the end
	//                 of the request channel.
	// JPW 08/25/2006: After the final slash in the request, if the
	//                 request ends in ".<something>" then set
	//                 duration to 0.  We used to only do this with
	//                 image requests, but now do it for all file
	//                 types where the request ends in some "." + any
	//                 extension.
	Hashtable nonRBNBMunge = parseURL.getNonRBNBMunge();
	String zeroFlag = null;
	if (nonRBNBMunge != null) {
	    zeroFlag = (String)nonRBNBMunge.get("z");
	    // zeroFlag must be one of "t" or "f"
	    if ( (zeroFlag != null)      &&
	         (!zeroFlag.equals("t")) &&
	         (!zeroFlag.equals("f")) )
	    {
		// This must not be a "z" flag intended for us
		// Just reset the String to null
		zeroFlag = null;
	    }
	}
	boolean bSetDurationToZero = false;
	int finalSlashIdx = requestStr.lastIndexOf('/');
	int finalDotIdx = requestStr.lastIndexOf('.');
	if ( (zeroFlag != null) && (zeroFlag.equals("f")) )
	{
	    bSetDurationToZero = false;
	}
	else if ( ((zeroFlag != null) && (zeroFlag.equals("t"))) ||
	          (finalDotIdx > finalSlashIdx) )
	{
	    bSetDurationToZero = true;
	}
	browserMungeStr =
	    parseURL.createNewRBNBMunge(
		false,
		bSetDurationToZero,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false);
	if ( (browserMungeStr == null) || (browserMungeStr.length() == 0) ) {
	    // No munge string from the request; use our standard munge string
	    mungeStr =
	        timeDrive.getMungeString(
		    idStr,
		    bSetDurationToZero ? 0.0 : tempDuration);
	} else if (parseURL.getTime() == null) {
	    // The request did include a munge string, but there was no
	    // "t=X" setting in it.
	    // Append our standard munge string onto the request munge string
	    // JPW 11/13/2006: Send getMungeString() the temporary value of
	    //                 duration to use in this request.
	    mungeStr =
		new String(
		    browserMungeStr +
		    "&" +
		    timeDrive.getMungeString(
		        idStr,
			bSetDurationToZero ? 0.0 : tempDuration));
	} else {
	    // The request did include a munge string with a "t=X" setting.
	    // Use browserMungeStr for the munge string.
	    mungeStr = browserMungeStr;
	    // JPW 11/13/2006: If no duration munge is in the request, add it.
	    if (parseURL.getDuration() == null) {
		// No "duration" munge in the request URL;
		// add duration to mungeStr
		MungeParameters mp = timeDrive.getCopyOfMungeParameters(idStr);
		mungeStr = mungeStr + "&d=" + Double.toString(mp.getDuration());
	    }
	    
	}
	
	//////////////////////////////////////////////////////////////////
	// Create munged URL and either:
	// a) Send a redirect back to the original requestor
	//        (this is the default option)
	// b) Write out request to and read reply from the ultimate server
	//////////////////////////////////////////////////////////////////
	
	String origRequest = request.getPath();
	
	request.changePath(requestStr + "?" + mungeStr);
	
	String mungedRequest = null;
	if (ultimateServerHost != null) {
	    mungedRequest = request.getRequest();
        } else {
            mungedRequest = request.getRequestNoHost();
        }
	
	if (debug>2) {
	    System.err.println(
		"Updated RBNB data request:\n"+request.getRequest());
	}
	
	/*
	 JPW 07/20/2006: Only connect to the ultimate server if bPassThrough
	                 is true.
	*/
	String redirectURI = null;
	if (bPassThrough) {
	    // Write out the request
	    sos.write(mungedRequest.getBytes());
	    // Read the response from the Server (by reading from spis);
	    // write this back to the browser by writing to bos
	    response = new Response(spis,bos,debug);
	} else if (request.isProxyRequest()) {
	    // The request begins with "http://", which means that a proxy
	    // directed the client request to TimeDrive automatically. In this
	    // case we can't just send the client back a redirect - because
	    // then the client will try the redirect URL, but the proxy will
	    // direct this to TimeDrive again, etc.  Therefore, just send back
	    // a stub response.
	    sendStubResponse(
	        "TimeDrive is in redirect (not pass-through) mode.\n" +
		"To prevent infinite request loops, can't process a request " +
		"sent to TimeDrive by a proxy.");
	} else {
	    // Send the client a redirection response
	    if (debug>2) {
		System.err.println(
		"Send an HTTP 303 (See Other) redirect back to the client.");
	    }
	    String protocolStr = "http://";
	    if (bSecureRedirect) {
		protocolStr = "https://";
	    }
	    redirectURI =
		new String(
		    protocolStr +
		    ultimateServerHost +
		    ":" +
		    ultimateServerPort + 
		    request.getPath());
	    String htmlCode =
		new String(
		    "<html>\n" +
		    "<head>\n" +
		    "<title>RBNB TimeDrive Redirection</title>\n" +
		    "</head>\n" +
		    "<body>\n" +
		    "To fulfill the data request, " +
		    "you are being redirected to:\n" +
		    "<a href=\"" +
		    redirectURI +
		    "\">" +
		    redirectURI +
		    "</a>\n" +
		    "</body>\n" +
		    "</html>");
	    byte[] byteContent = htmlCode.getBytes();
	    int numBytes = byteContent.length;
	    // Use current time
	    String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	    SimpleDateFormat sdf=new SimpleDateFormat(format);
	    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	    Date date = new Date();
	    String dateString = sdf.format(date);
	    // Put together a response to send to browser
	    String headerStr =
		"HTTP/1.1 303 See Other\n" +
		"Server: TimeDrive\n" +
		"Pragma: no-cache\n" +
		"Cache-Control: no-cache\n" +
		"Expires: -1\n" +
		"Last-Modified: " + Long.toString(date.getTime()) + "\n" +
		"Duration: 0.0\n" +
		"Content-Length: " + numBytes + "\n" +
		"Date: " +  dateString + "\n" +
		"Location: " + redirectURI + "\n" +
		"Connection: close\n" +
		// Have the last line of the header blank
		"\n";
	    String responseStr = headerStr + htmlCode;
	    // Write response back to browser
	    response =
		new Response(
		    new ByteArrayInputStream(responseStr.getBytes()),
		    bos,
		    debug);
	}
	
	// Debug print
	if ( (debug > 0) && (debug <= 3) ) {
	    if ( (parseURL.getMunge() == null) ||
	         (parseURL.getMunge().length() == 0) )
	    {
                System.err.println(
                    "Data request:  " +
                    request.getPath() +
                    "  Length:" +
                    response.getContentLength());
            }
	    else
	    {
                // The request came in with a munge string; show both the
                // original and final request
                System.err.println(
                    "Data request  Orig:" +
                    origRequest +
                    "  Final:" +
                    request.getPath() +
                    "  Length:" +
                    response.getContentLength());
            }
	    if (redirectURI != null) {
		System.err.println("\tRedirected to:  " + redirectURI);
	    }
	} else if (debug > 3) {
	    System.err.println(response.getHeader());
	}
	
  }
  
  /**************************************************************************
   * Send the given response back to the client.
   * <p>
   *
   * @author John P. Wilson
   *
   * @version 07/20/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 07/20/2006  JPW	Add htmlCodeI
   * 11/20/2005  JPW	Created
   *
   */
  
  private void sendStubResponse(String htmlCodeI) throws Exception {
	
	String htmlCode =
	    "The request, " +
	    request.getPath() +
	    "could not be processed.\n" +
	    "Request denied.\n";
	if ( (htmlCodeI != null) && (!htmlCodeI.trim().equals("")) ) {
	    htmlCode = htmlCodeI;
	}
	
	// EMF: Test out cookies with Google Earth
	/* htmlCode =
	    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	    "<kml xmlns=\"http://earth.google.com/kml/2.0\">" +
	    "<NetworkLinkControl><cookie>ID=46</cookie></NetworkLinkControl>" +
	    "<Document>" +
	    "<name>Farmhouse.kml</name>" +
	    "<GroundOverlay id=\"khGroundOverlay707\">" +
            "<name>Creare Farmhouse</name>" +
	    "<LookAt id=\"khLookAt708\">" +
            "<longitude>-72.2336291</longitude>" +
            "<latitude>43.6839681</latitude>" +
            "<altitude>0</altitude>" +
            "<range>461</range>" +
            "<tilt>0</tilt>" +
            "<heading>0</heading>" +
            "</LookAt>" +
            "<Icon>" +
            "<href>camera.jpg</href>" +
            "<refreshMode>onInterval</refreshMode>" +
            "<refreshInterval>1</refreshInterval>" +
            "</Icon>" +
            "<LatLonBox id=\"khLatLonBox710\">" +
            "<north>43.6845</north>" +
            "<south>43.6835</south>" +
            "<east>-72.23283900456192</east>" +
            "<west>-72.23453994731886</west>" +
            "</LatLonBox>" +
	    "</GroundOverlay>" +
	    "</Document>" +
	    "</kml>";
	*/
	
	byte[] htmlBytes = htmlCode.getBytes();
	
	if (debug > 0) {
	    System.err.println(htmlCode);
	}
	
	// Current time
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	Date date = new Date();
	String dateString = sdf.format(date);
	// Put together a response to send to browser
	String headerStr =
	    "HTTP/1.1 200 OK\n" +
	    "Server: TimeDrive\n" +
	    "Pragma: no-cache\n" +
	    "Cache-Control: no-cache\n" +
	    "Expires: -1\n" +
	    "Last-Modified: " + Long.toString(date.getTime()) + "\n" +
	    "Duration: 0.0\n" +
	    "Content-Length: " + htmlBytes.length + "\n" +
	    "Date: " +  dateString + "\n" +
	    // Have the last line of the header blank
	    "\n";
        String responseStr = headerStr + htmlCode;
        // Write response back to browser
        response =
	    new Response(
	        new ByteArrayInputStream(responseStr.getBytes()), bos, debug);
	
	if (debug > 3) System.err.println(response.getHeader());
	
  }
  
  /**************************************************************************
   * The browser is requesting that the current time in TimeDrive be set to a
   * time value obtained from a reference/sync channel.
   * <p>
   * Request would look like:
   * http://<TimeDrive host>:<TimeDrive port>/updatetime/<full chan name>@r=<reference>&dt=string&f=t
   * Note that there is an implied "t=0" in this request. The "reference" will
   * either be "newest" or "oldest".  An example of this request is:
   * http://<TimeDrive host>:<TimeDrive port>/updatetime/localhost/RBNB/Video/WRBV.jpg@r=oldest&dt=string&f=t
   * <p>
   * When TimeDrive receives this request, it will in turn send out the
   * following request to Tomcat in order to get the desired time:
   * GET localhost/RBNB/Video/WRBV.jpg@r=oldest&dt=string&f=t
   * The data (in this case, the oldest time on the WRBV.jpg channel) is
   * received back from Tomcat and set as the current time in TimeDrive.
   * <p>
   *
   * @author John P. Wilson
   *
   * @param timeRequestURLI	The URL to make the time request of the
   *				Tomcat/RBNB server.
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
  
  private void handleUpdateAbsoluteTimeRequest(String timeRequestURLI)
      throws Exception
  {
	// Save the name of the new sync channel; this channel name gets
	// sent back to the browser in the HTML code
	int index = timeRequestURLI.indexOf('@');
      	String syncChannel = "";
	if (index > -1) {
	    // Peel off "http://" from front and everything at and after "@"
	    syncChannel = timeRequestURLI.substring(7,index);
	    timeDrive.setSyncChannel(idStr, syncChannel);
	}
	// Modify the request
	request.changePath(timeRequestURLI);
	//create server socket streams
	try {
	    server=new Socket(request.getHost(),request.getPort());
	    spos=new PipedOutputStream();
	    spis=new PipedInputStream(spos);
	    // Read from server and write to spos (we can then read this from spis)
	    ssr=new SocketReader(server,spos,spis,debug,request.getPath());
	    // To write to the server
	    sos=server.getOutputStream();
	} catch (Exception e) {
	    if (ultimateServerHost != null) {
		System.err.println(
		    "Exception getting streams for server " +
		    ultimateServerHost +
		    ":" +
		    ultimateServerPort +
		    " " +
		    e.getMessage());
	    } else {
		System.err.println(
		    "Exception getting streams for server " +
		    request.getHost() +
		    ":" +
		    request.getPort() +
		    " " +
		    e.getMessage());
	    }
	    throw e;
	}
	
        if (debug > 2) System.err.println("request is: " + request.getRequest());
	
	// Write out the request to the Server
	sos.write(request.getRequestNoHost().getBytes());
	
        // Read the response from the Server (by reading from spis)
	// don't need to write to an OutputStream; set this to null
        response = new Response(spis,null,debug);
	
        // The content should contain the requested time value
	String absTimeStr = new String(response.getContent());
	
	// See if this contains a double value
	double newTime = Double.MAX_VALUE;
	try {
	    newTime = Double.parseDouble(absTimeStr);
	    if (debug > 1) {
		System.err.println(
		    "Update time from reference chan;  Request: " +
		    request.getPath() +
		    "  Set time to: " +
		    newTime);
	    }
	    
	} catch (NumberFormatException nfe) {
	    System.err.println(
	        "Error setting TimeDrive time; new time not set.\n" +
		"Request was: " +
		request.getPath());
	}
	// Update MungeParameters; note: only set new time value
	MungeParameters copyOfMungeParams =
          timeDrive.setTimeMunge(
	      idStr,
	      null,
	      newTime,
	      Double.MAX_VALUE,
	      null,
	      Double.MAX_VALUE);
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	// Current time
	Date date = new Date();
	String dateString = sdf.format(date);
	// Put together a response to send to browser
	String bodyStr =
	    copyOfMungeParams.toString(timeDrive.getSyncChannel(idStr));
	String headerStr =
	    "HTTP/1.1 200 OK\n" +
	    "Server: TimeDrive\n" +
	    "Pragma: no-cache\n" +
	    "Cache-Control: no-cache\n" +
	    "Expires: -1\n" +
	    "Last-Modified: " + Long.toString(date.getTime()) + "\n" +
	    "Duration: 0.0\n" +
	    "Content-Length: " + Integer.toString(bodyStr.getBytes().length) + "\n" +
	    "Date: " +  dateString + "\n" +
	    // Have the last line of the header blank
	    "\n";
	String responseStr = headerStr + bodyStr;
        // Write response back to browser
        response =
	    new Response(
	        new ByteArrayInputStream(responseStr.getBytes()), bos, debug);
	
        if (debug > 3) System.err.println(response.getHeader());
	
  }
  
  /**************************************************************************
   * The browser is requesting that TimeDrive update its time to be the
   * current system time.
   * <p>
   * Request would look like:
   * http://<TimeDrive host>:<TimeDrive port>/updatetocurrenttime
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
  
  private void handleUpdateToCurrentTime() throws Exception {
        
	Date date = new Date();
	double newTime = ((double)date.getTime()) / 1000.0;
	
	// Update MungeParameters; note: only set new time value
	MungeParameters copyOfMungeParams =
          timeDrive.setTimeMunge(
	      idStr,
	      null,
	      newTime,
	      Double.MAX_VALUE,
	      null,
	      Double.MAX_VALUE);
	
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String dateString = sdf.format(date);
	if (debug > 1) {
	    System.err.println("Set time to current time: " + dateString);
	}
	
	// Put together a response to send to browser
	String bodyStr =
	    copyOfMungeParams.toString(
		timeDrive.getSyncChannel(idStr));
	String headerStr =
	    "HTTP/1.1 200 OK\n" +
	    "Server: TimeDrive\n" +
	    "Pragma: no-cache\n" +
	    "Cache-Control: no-cache\n" +
	    "Expires: -1\n" +
	    "Last-Modified: " + Long.toString(date.getTime()) + "\n" +
	    "Duration: 0.0\n" +
	    "Content-Length: " + Integer.toString(bodyStr.getBytes().length) + "\n" +
	    "Date: " +  dateString + "\n" +
	    // Have the last line of the header blank
	    "\n";
	String responseStr = headerStr + bodyStr;
        // Write response back to browser
        response =
	    new Response(
	        new ByteArrayInputStream(responseStr.getBytes()), bos, debug);
	
	if (debug > 3) System.err.println(response.getHeader());
	
  }
  
  /**************************************************************************
   * The browser is requesting the TimeDrive Web interface page.
   * <p>
   * Request would look like:
   * http://<TimeDrive host>:<TimeDrive port>/timedrive.html
   * <p>
   * Read the html code from a file and send it back to the browser.
   *
   * @author John P. Wilson
   *
   * @version 01/10/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 01/10/2006  JPW	Read "timedrive.html" from the JAR file.
   *			In timedrive.html, replace the string "<WebServer>"
   *			with the name of the actual Tomcat server host from
   *			which the client will access timedrive.kml.
   * 11/20/2005  JPW	Created
   *
   */
  
  private void handleHTMLRequest() throws Exception {
	
	// FileInputStream fis = new FileInputStream("timedrive.html");
	
	// JPW 01/10/2006: Read the HTML file from timedrive.jar
	BufferedInputStream fis =
	    new BufferedInputStream(
	        getClass().getClassLoader().getResourceAsStream(
		    "timedrive.html"));
	
	int numBytes = fis.available();
	byte[] byteContent = new byte[numBytes];
	fis.read(byteContent, 0, numBytes);
	fis.close();
	String htmlCode = new String(byteContent);
	
	byteContent = htmlCode.getBytes();
	numBytes = byteContent.length;
	
	// Current time
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	Date date = new Date();
	String dateString = sdf.format(date);
	// Put together a response to send to browser
	String headerStr =
	    "HTTP/1.1 200 OK\n" +
	    "Server: TimeDrive\n" +
	    "Pragma: no-cache\n" +
	    "Cache-Control: no-cache\n" +
	    "Expires: -1\n" +
	    "Last-Modified: " + Long.toString(date.getTime()) + "\n" +
	    "Duration: 0.0\n" +
	    "Content-Length: " + numBytes + "\n" +
	    "Date: " +  dateString + "\n" +
	    // Have the last line of the header blank
	    "\n";
	String responseStr = headerStr + htmlCode;
        // Write response back to browser
        response =
	    new Response(
	        new ByteArrayInputStream(responseStr.getBytes()), bos, debug);
	
	if (debug > 1) System.err.println("Returned TimeDrive HTML");
	
        if (debug > 3) System.err.println(response.getHeader());
	
  }
  
  /**************************************************************************
   * The browser is requesting that TimeDrive update its munge parameters.
   * <p>
   * Requests come in one of two forms:
   * http://<TimeDrive host>:<TimeDrive port>/updatemunge@reference=<new reference>&time=<new time>&duration=<new duration>&play=<new play mode>&rate=<new play rate or step>
   * OR
   * http://<TimeDrive host>:<TimeDrive port>/?reference=<new reference>&time=<new time>&duration=<new duration>&play=<new play mode>&rate=<new play rate or step>
   * <p>
   * Values are parsed from the browser's request string and set in the
   * MungeParameters class stored in TimeDrive.
   *
   * @author John P. Wilson
   *
   * @param bHTMLResponseI	Send back the TimeDrive HTML page in response?
   *
   * @version 06/02/2006
   */
   
  /*
   *
   *   Date      By	Description
   * MM/DD/YYYY
   * ----------  --	-----------
   * 06/02/2006  JPW	Replace parseMungeParameters() and the use of
   *			    RawMungeParameters with ParseURL
   * 03/01/2006  JPW	If bHTMLResponseI is true, then respond with the HTML
   *			for the TimeDrive interface.
   * 11/20/2005  JPW	Created
   *
   */
  
  private void handleUpdateMungeRequest(boolean bHTMLResponseI)
      throws Exception
  {
        // Parse updated munge parameters from the path
	ParseURL parseURL = new ParseURL(request.getPath(), false);
	Hashtable nonRBNBMunge = parseURL.getNonRBNBMunge();
	String playMode = null;
	double rate = Double.MAX_VALUE;
	if (nonRBNBMunge != null) {
	    playMode = (String)nonRBNBMunge.get("play");
	    String rateStr = (String)nonRBNBMunge.get("rate");
	    if ( (rateStr != null) && (!rateStr.trim().equals("")) ) {
		try {
		    rate = Double.parseDouble(rateStr);
		} catch (NumberFormatException nfe) {
		    // Nothing to do; other garbage was in the "rate" field
		}
	    }
	}
	MungeParameters copyOfMungeParams =
	    timeDrive.setTimeMunge(
	        idStr,
		(parseURL.getReference() != null) ? parseURL.getReference() : "absolute",
		(parseURL.getTime() != null) ? parseURL.getTime().doubleValue() : Double.MAX_VALUE,
		(parseURL.getDuration() != null) ? parseURL.getDuration().doubleValue() : Double.MAX_VALUE,
		playMode,
		rate);
	String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
	SimpleDateFormat sdf=new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	// Current time
	Date date = new Date();
	String dateString = sdf.format(date);
	
	if (debug > 1) {
	    System.err.println(
		"Updated munge parameters: " +
		copyOfMungeParams.toString(timeDrive.getSyncChannel(idStr)));
	}
	
	// Put together a response to send to browser
	if (bHTMLResponseI) {
	    handleHTMLRequest();
	} else {
	    String bodyStr =
	        copyOfMungeParams.toString(timeDrive.getSyncChannel(idStr));
	    String headerStr =
		"HTTP/1.1 200 OK\n" +
		"Server: TimeDrive\n" +
		"Pragma: no-cache\n" +
		"Cache-Control: no-cache\n" +
		"Expires: -1\n" +
		"Last-Modified: " + Long.toString(date.getTime()) + "\n" +
		"Duration: 0.0\n" +
		"Content-Length: " + Integer.toString(bodyStr.getBytes().length) + "\n" +
		"Date: " +  dateString + "\n" +
		// Have the last line of the header blank
		"\n";
	    String responseStr = headerStr + bodyStr;
	    // Write response back to browser
	    response =
	    	new Response(
		    new ByteArrayInputStream(responseStr.getBytes()),
		    bos,
		    debug);
	    if (debug > 3) System.err.println(response.getHeader());
	}
  }
  
}

