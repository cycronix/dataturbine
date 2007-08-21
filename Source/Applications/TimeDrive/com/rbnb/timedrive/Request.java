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

// Request.java - represents an HTTP request issued by a browser (or created
//                by the cache
// John P. Wilson
// Taken from WebCache by Eric Friets
// Copyright 2005, 2006 Creare Incorporated

package com.rbnb.timedrive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class Request {
  
  private static String authorizationLabel=new String("Authorization: Basic");
  private static String contentLabel=new String("Content-Length: ");
  private static String sourceIPLabel=new String("-source-ip: ");
  private static String modifiedLabel=new String("If-Modified-Since: ");
  private static String optionLabel=new String("Opt: \"http://rbnb.net/ext\"; ns=");
  
  private static String eol=new String("\r\n");
  
  private PipedInputStream pis=null;
  private int debug=0;
  private String source=null;
  // JPW 05/09/2006: Add bUltimateHostSpecified
  private boolean bUltimateHostSpecified = false;
  private boolean isNull=false;
  private boolean doNullResponse=false;
  private int numHeaderLines=0;
  private String[] headerA=null;
  private String firstLineNoHost=null;
  private int contentLength=0;
  private char[] content=null;

  private String command=null;
  private String protocol=null;
  private String path=null;
  private String language=null;
  private String host=null;
  private int port=80;
  private String requestString=null;
  private String requestStringNoHost=null;
  
  // JPW 08/29/2006: Encoded username/password when using Basic Authorization
  private String authorizationString = null;
  
  // JPW 09/05/2006: Namespace code pulled from an "Opt" header
  private String namespaceCode = null;
  
  // JPW 09/05/2006: IP address string pulled from a header line of the form
  //                 "xx-source-ip", where "xx" is the namespace code
  private String ipAddress = null;
  
  // JPW 06/20/2006: Did a proxy redirect this client request to TimeDrive?
  //                 If the client request starts with "http://" then this
  //                 request was redirected to TimeDrive by a proxy.
  private boolean bProxyRequest = false;
  
  // JPW 05/09/2006: Add constructor argument, bUltimateHostSpecifiedI;
  //                 used in parseFirstLine()
  public Request(
	PipedInputStream pisI,
	int debugI,
	String source,
	boolean bUltimateHostSpecifiedI)
  {
    pis=pisI;
    debug=debugI;
    source=source;
    bUltimateHostSpecified = bUltimateHostSpecifiedI;
    boolean done=false;
    Vector headerV=new Vector();
    int nAvail=0;
    int nRead=0;

    BufferedReader br=new BufferedReader(new InputStreamReader(pis));
    try {
      //read header
      while (!done) {
        String nextLine=br.readLine();
	if (debug > 4) {
	    System.err.println("Request nextLine "+nextLine);
	}
        if (nextLine==null) {
	    done=true;
	} else {
	    if (nextLine.length()==0)
	    {
		// JPW 12/20/2005: Before this empty line gets added to the
		//                 header, add "Connection: close"; this tells
		//                 the server that we don't want to keep
		//                 this socket connection open
		headerV.add("Connection: close");
		headerV.add(nextLine);
		done=true;
	    }
	    // JPW 08/25/2006: Do a case-insensitive comparison
	    else if ( (!nextLine.toLowerCase().startsWith("keep-alive")) &&
		      (!nextLine.toLowerCase().startsWith("connection")) )
	    {
		// JPW 08/29/2006
		// Add support for authorization; however, don't pass the
		// "Authorization" header on to the ultimate server (that is,
		// don't add this line to headerV)
		if (nextLine.regionMatches(true,0,authorizationLabel,0,authorizationLabel.length())) {
		    authorizationString =
		      nextLine.substring(authorizationLabel.length()).trim();
		    if ( (authorizationString != null) &&
		         (authorizationString.trim().equals("")) )
		    {
			authorizationString = null;
		    }
		    authorizationString = authorizationString.trim();
		}
		// JPW 09/05/2006
		// Pull the 2 digit namespace number out; don't pass this
		// "Opt" line on to the ultimate server (that is, don't add
		// this line to headerV)
		else if (nextLine.regionMatches(true,0,optionLabel,0,optionLabel.length())) {
		    namespaceCode =
		      nextLine.substring(optionLabel.length()).trim();
		    if ( (namespaceCode != null) &&
		         (namespaceCode.trim().equals("")) )
		    {
			namespaceCode = null;
		    }
		    namespaceCode = namespaceCode.trim();
		}
		
		// JPW 09/05/2006
		// Pull out the IP address of the original client; don't pass
		// this line on to the ultimate server (that is, don't add
		// this line to headerV)
		// NOTE: In these comparisons we assume the fact that the namespace
		//       code is 2 digits long
		// Make sure the namespace code at the beginning of nextLine
		// matches the previously saved namespaceCode stirng.
		else if ( nextLine.regionMatches(true,2,sourceIPLabel,0,sourceIPLabel.length()) &&
		          (namespaceCode != null)                                               &&
		          (nextLine.regionMatches(true,0,namespaceCode,0,namespaceCode.length())) )
		{
		    ipAddress =
			nextLine.substring(sourceIPLabel.length() + 2).trim();
		    if ( (ipAddress != null) &&
		         (ipAddress.trim().equals("")) )
		    {
			ipAddress = null;
		    }
		    ipAddress = ipAddress.trim();
		}
		
		else {
		    headerV.add(nextLine);
		}
	    }
        }
      }
      //parse for content length, if any
      numHeaderLines=headerV.size();
      headerA=new String[numHeaderLines];
      headerV.copyInto(headerA);
      for (int i=numHeaderLines-1;i>=0;i--) {
        if (headerA[i].regionMatches(true,0,contentLabel,0,contentLabel.length())) {
            contentLength=Integer.parseInt(headerA[i].substring(contentLabel.length()));
        } else if (headerA[i].regionMatches(true,0,modifiedLabel,0,modifiedLabel.length())) {
            String modifiedString=headerA[i].substring(modifiedLabel.length());
        }
      }
      if (contentLength>0) {
        content=new char[contentLength];
        int numRead=br.read(content,0,contentLength);
        while (numRead<contentLength) {
          numRead+=br.read(content,numRead,contentLength-numRead);
        }
      }
    } catch (Exception e) {
      System.err.println("Request exception: "+e.getMessage());
      isNull=true;
    }
    
  } //end constructor
  
  // JPW 07/20/2006: Did a proxy redirect this client request to TimeDrive?
  //                 If the client request starts with "http://" then this
  //                 request was redirected to TimeDrive by a proxy.
  //                 "bProxyRequest" is set in parseFirstLine().
  public boolean isProxyRequest() {
      if (isNull) return false;
      if (path==null) parseFirstLine();
      return bProxyRequest;
  }
  
  //isNull method - returns true if request is null
  public boolean isNull() {
    return isNull;
  }

  public boolean doNullResponse() {
    if (isNull) return true;
    if (path==null) parseFirstLine();
    return doNullResponse;
  }
  
  //getCommand method - returns command of request
  public String getCommand() {
    if (isNull) return null;
    if (command==null) parseFirstLine();
    return command;
  }

  //getProtocol method - returns protocol of request
  public String getProtocol() {
    if (isNull) return null;
    if (protocol==null) parseFirstLine();
    return protocol;
  }

  //getPath method - returns path of request
  public String getPath() {
    if (isNull) return null;
    if (path==null) parseFirstLine();
    return path;
  }

  //getLanguage method - returns language of request
  public String getLanguage() {
    if (isNull) return null;
    if (language==null) parseFirstLine();
    return language;
  }
  
  // JPW 09/14/2006: Add MULTIUSER_COMBO mode
  // Return the user ID, where the type of ID returned is based on the
  // multi-user mode we are in
  public String getUserID(int multiUserModeI, String hostAddressFromSocketI) {
    
    if ( (isNull) || (multiUserModeI == TimeDrive.MULTIUSER_OFF) )
    {
	return null;
    }
    
    else if (multiUserModeI == TimeDrive.MULTIUSER_IP)
    {
	// Give preference to the IP address provided in the HTTP header
	if (ipAddress != null) {
	    return ipAddress;
	} else {
	    return hostAddressFromSocketI;
	}
    }
    
    else if (multiUserModeI == TimeDrive.MULTIUSER_BASIC_AUTH)
    {
	return authorizationString;
    }
    
    else if (multiUserModeI == TimeDrive.MULTIUSER_COMBO)
    {
	// Must have a non-null authorization and non-null IP;
	// Give preference to the IP address provided in the HTTP header
	if (authorizationString == null) {
	    return null;
	} else {
	    if (ipAddress != null) {
	       return new String(ipAddress + authorizationString);
	    } else {
	       return new String(hostAddressFromSocketI + authorizationString);
	    }
	}
    }
    
    return null;
    
  }
  
  // JPW 09/05/2006
  // Return authorizationString
  public String getAuthorizationString() {
      if (isNull) return null;
      return authorizationString;
  }
  
  //parseFirstLine method - parses command,protocol,path,language
  private void parseFirstLine() {
    if (isNull) return;
    if (headerA==null || headerA.length<1) {
      isNull=true;
      return;
    }
    int httpIndex = headerA[0].indexOf("http://");
    if (httpIndex == -1) {
	// This is for parsing a first line that looks something like:
	// "GET /RBNB/rbnbSource/c0 HTTP/1.1"
	// JPW 07/20/2006: Add bProxyRequest; setting this false means that
	//                 the client sent this request directly to TimeDrive
	//                 (a proxy didn't redirect the client request
	//                 to TimeDrive).
	bProxyRequest = false;
	StringTokenizer st=new StringTokenizer(headerA[0]," ");
	command=st.nextToken();
	path = st.nextToken();
	// Hardwire the protocol
	protocol = "http";
	language=st.nextToken();
    } else {
	// This is for parsing a first line that looks something like:
	// "GET http://jpw.creare.com/RBNB/rbnbSource/c0 HTTP/1.1"
	// We would get this format if a proxy was directing
	// the client request to TimeDrive
	
	// JPW 07/20/2006
	// NOTE: Since a proxy directed this client request to TimeDrive,
	//       sending back a redirect to the client won't work (because the
	//       client will send the request off again, and it will be
	//       redirected to TimeDrive by the proxy again, etc.).  Therefore,
	//       this case is only valid if we are *not* in redirect mode,
	//       that is, if bPassThrough is true.  Otherwise, this is an
	//       error and we should just send a stub response back to the
	//       client (don't send a redirect back to the client).
	// JPW 07/20/2006: Add bProxyRequest; setting this true means that
	//                 a proxy redirected the client request to TimeDrive.
	bProxyRequest = true;
	
	StringTokenizer st=new StringTokenizer(headerA[0]," :");
	command=st.nextToken();
	protocol=st.nextToken();
	if (bUltimateHostSpecified) {
	    // JPW 05/09/2006: A proxy is being used and the user has
	    //                 specified an ultimate host - must keep
	    //                 the protocol on the path
	    path = protocol + ":" + st.nextToken();
	} else {
	    // No ultimate host has been specified by the user - in this
	    // case, the ultimate host:port will be parsed from the
	    // path itself and TimeDrive will make a connection to that
	    // host:port and send it the request.
	    path=st.nextToken().substring(2); // strip the leading "//"
	}
	language=st.nextToken();
    }
    
  }

  //getHost method - returns host of path
  public String getHost() {
    if (isNull) return null;
    if (host==null) {
      StringTokenizer st=new StringTokenizer(headerA[0]," /",true);
      StringBuffer sb=new StringBuffer();
      sb=sb.append(st.nextToken()); // command
      sb=sb.append(st.nextToken()); // space
      st.nextToken(); // http:
      st.nextToken(); // slash
      st.nextToken(); // slash
      host=st.nextToken(); // host
      //parse off port, if there
      int idx=host.indexOf(':');
      if (idx>0) {
        port=Integer.parseInt(host.substring(idx+1));
        host=host.substring(0,idx);
      }
      while (st.hasMoreTokens()) sb=sb.append(st.nextToken()); //rest of line
      firstLineNoHost=sb.toString();
    }
    return host;
  }

  //getPort method - returns port of path (if not specified, default of 80)
  public int getPort() {
    if (isNull) return 0;
    if (host==null) getHost();
    return port;
  }
  
  // Change the path to be the given string; return the new request string
  public String changePath(String newPathI) {
      if (isNull) {
	  return null;
      }
      if (debug > 2) {
          System.err.println("Change request path to: " + newPathI);
      }
      // Change headerA[0] to include the new path
      if (!bUltimateHostSpecified) {
	  headerA[0] =
              new String(
		  command +
		  " http://" +
		  newPathI +
		  " " +
		  language);
      } else {
	  headerA[0] =
              new String(
		  command +
		  " " +
		  newPathI +
		  " " +
		  language);
      }
      // Reset variables and reparse
      path = null;
      command = null;
      language = null;
      protocol = null;
      host = null;
      requestString = null;
      requestStringNoHost = null;
      firstLineNoHost = null;
      requestString = getRequest();
      parseFirstLine();
      getHost();
      return requestString;
  }
  
  //getRequest method - returns entire request as single string
  public String getRequest() {
    if (isNull) return null;
    if (requestString==null) {
      StringBuffer sb=new StringBuffer();
      for (int i=0;i<numHeaderLines;i++) {
        sb=sb.append(headerA[i]).append(eol);
      }
      if (contentLength>0) {
        sb=sb.append(content).append(eol);
      }
      requestString=sb.toString();
    }
    return requestString;
  }
  
  //getRequestNoHost method - returns entire request as a single string,
  //                          with host removed
  public String getRequestNoHost() {
    if (isNull) return null;
    if (requestStringNoHost==null) {
      if (firstLineNoHost==null) getHost();
      StringBuffer sb=new StringBuffer();
      sb=sb.append(firstLineNoHost).append(eol);
      for (int i=1;i<numHeaderLines;i++) {
        sb=sb.append(headerA[i]).append(eol);
      }
      if (contentLength>0) {
        sb=sb.append(content).append(eol);
      }
      requestStringNoHost=sb.toString();
    }
    return requestStringNoHost;
  }
  
} //end class Request

