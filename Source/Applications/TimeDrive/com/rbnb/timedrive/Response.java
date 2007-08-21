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

// Response.java - represents an HTTP request issued by a browser (or created
//                by the cache
// John P. Wilson
// Taken from WebCache by Eric Friets
// Copyright 2005, 2006 Creare Incorporated

package com.rbnb.timedrive;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

public class Response {
  private static String contentLengthLabel=new String("Content-Length:");
  private static String contentTypeLabel=new String("Content-Type:");
  private static String textType=new String("text/");
  //private static String textHtmlType=new String("text/html");
  private static String lastModifiedLabel=new String("Last-Modified:");
  private static String dateLabel=new String("Date:");
  public static String eol=new String("\r\n");
  private static String notModifiedLine=new String("HTTP/1.1 304 Not Modified");

  private int debug=0;
  private boolean headerOnly=false;
  private Vector headerV=new Vector();
  private int numHeaderLines=0;
  private boolean isNull=false;
  private String[] headerA=null;
  private String firstLineNoHost=null;
  private boolean isText=false;
  private int contentLength=0;
  private byte[] content=null;
  private Vector bodyV=new Vector();

  private String statusCode=null;
  private String header=null;
  private byte[] responseBytes=null;
  
  public Response(
      String headerI,
      int debugI)
  throws Exception
  {
    this(new ByteArrayInputStream(headerI.getBytes()),null,debugI,true);
    header=headerI;
  }

  public Response(
      InputStream is,
      OutputStream os,
      int debugI)
  throws Exception
  {
    this(is,os,debugI,false);
  }
  
  public Response(
      InputStream is,
      OutputStream os,
      int debugI,
      boolean headerOnlyI)
  throws Exception
  {

    debug=debugI;
    headerOnly=headerOnlyI;
    boolean done=false;
    int nAvail=0;
    int nRead=0;

    // JPW 11/30/2005: May want to switch over from DataInputStream to
    //                 BufferedReader in order to use the readLine() method;
    //                 only problem is that we also use read() to read
    //                 byte data, not character data.
    // BufferedReader br=new BufferedReader(new InputStreamReader(is));
    DataInputStream  br=new DataInputStream (is);
    try {
      //read header
      // JPW 08/25/2006: Only want to add 1 "Connection: close"
      boolean bAddedConnectionClose = false;
      while (!done) {
        String nextLine=br.readLine();
	if (debug>4) System.err.println("Response nextLine "+nextLine);
        if (nextLine==null) done=true;
        else {
          if (os!=null) {
	    // Before writing out the last line of the header,
	    // sneak in "Connection: close"
	    // JPW 08/25/2006: Add "Connection: close" only once
	    if ( (!bAddedConnectionClose) && (nextLine.length()==0) ) {
		// Tell the client that this socket connection will close and
		// no longer be available; this will tell the browser that
		// we are not keeping this socket alive
		String closeStr = "Connection: close";
		headerV.add(closeStr);
		os.write(closeStr.getBytes());
		os.write(eol.getBytes());
	    }
	    if (nextLine.equalsIgnoreCase("connection: close")) {
	      bAddedConnectionClose = true;
	    }
            os.write(nextLine.getBytes());
            os.write(eol.getBytes());
          }
          if (nextLine.length()==0) done=true;
          headerV.add(nextLine);
	  
	  /*
	   * This code can be used in place of the code above in order to get
	   * rid of the following HTTP header lines in the WebTurbine response:
	   *    Pragma: no-cache
	   *    Cache-Control: no-cache
	   *    Expires: -1
	   *
	   * Can also try adding an "ETag" in the response and see if the
	   * next request from the browser includes "If-None-Match" with the
	   * same ETag.  If trying out the ETag, don't include "Last-Modified"
	   * in the response.
	   *
	   *
	  if ( (os!=null) &&
	       (!nextLine.equalsIgnoreCase("Pragma: no-cache")) &&
	       (!nextLine.equalsIgnoreCase("Cache-Control: no-cache")) &&
	       (!nextLine.equalsIgnoreCase("Expires: -1")) )
	       // Don't pass through "Last-Modified" if injecting an ETag into the response
	       // && (!nextLine.startsWith("Last-Modified:")) )
	  {
	    // Before writing out the last line of the header,
	    // sneak in "Connection: close"
	    if (nextLine.length()==0) {
		// Tell the client that this socket connection will close and
		// no longer be available; this will tell the browser that
		// we are not keeping this socket alive
		String closeStr = "Connection: close";
		headerV.add(closeStr);
		os.write(closeStr.getBytes());
		os.write(eol.getBytes());
	    }
            os.write(nextLine.getBytes());
            os.write(eol.getBytes());
          }
	  // Optional: if we want to inject something in place of "Expires: -1"
	  // else if (nextLine.equalsIgnoreCase("Expires: -1")) {
	      
	      // Option 1: Set expiration at some point in the future
	      // nextLine = "Expires: Mon, 28 Aug 2006 18:00:00 GMT";
	      
	      // Option 2:
	      // nextLine = "Cache-Control: must-revalidate, max-age=0";
	      
	      // Option 3: Try an ETag; when using this option, don't send
	      //           "Last-Modified" in the response
	      // nextLine = "ETag: \"0192384-9023-1a929893\"";
	      
	      // Do the following if we use one of the options above
	      // System.err.println("Don't use \"Expires: -1\"; use: " + nextLine);
	      // os.write(nextLine.getBytes());
	      // os.write(eol.getBytes());
	  // }
	  else {
	      System.err.println("Don't send \"" + nextLine + "\" back to browser");
	  }
          if (nextLine.length()==0) done=true;
	  if ( (!nextLine.equalsIgnoreCase("Pragma: no-cache")) &&
	       (!nextLine.equalsIgnoreCase("Cache-Control: no-cache")) &&
	       (!nextLine.equalsIgnoreCase("Expires: -1")) )
	       // Don't pass through "Last-Modified" if injecting an ETag into the response
	       // && (!nextLine.startsWith("Last-Modified:")) )
	  {
	      headerV.add(nextLine);
	  }
	  *
	  */
	  
	  
	  
        }
      }
      //parse for content length, if any
      numHeaderLines=headerV.size();
      headerA=new String[numHeaderLines];
      headerV.copyInto(headerA);
      for (int i=numHeaderLines-1;i>=0;i--) {
        if (headerA[i].regionMatches(true,0,contentLengthLabel,0,contentLengthLabel.length())) {
          contentLength=Integer.parseInt(headerA[i].substring(contentLengthLabel.length()).trim());
        }
        else if (headerA[i].regionMatches(true,0,contentTypeLabel,0,contentTypeLabel.length())) {
          if (headerA[i].indexOf(textType)>0) isText=true;
        }
        else if (headerA[i].regionMatches(true,0,lastModifiedLabel,0,lastModifiedLabel.length())) {
	    // nothing to do
        }
      }
      
      if (!headerOnly) {
        //check for null response
        if ( numHeaderLines==0 ||
	     headerV.elementAt(0)==null ||
	     ((String)(headerV.elementAt(0))).equalsIgnoreCase("Error") )
	{
            if (debug>2) {
		System.err.println("Response null header");
	    }
            isNull=true;
        }
	else if ( contentLength==0 &&
	          getStatusCode().equals("200") )
	{
	    // NOTE: contentLength is unspecified; go until read() returns -1
	    Vector contentV=new Vector();
	    byte[] chunk=new byte[1024]; //optimal size??
	    int numRead=0;
	    while ((numRead=br.read(chunk))>-1) {
		contentLength+=numRead;
		if (chunk.length>numRead) {
		    byte[] temp=new byte[numRead];
		    System.arraycopy(chunk,0,temp,0,numRead);
		    chunk=temp;
		}
	        if (os!=null) {
		    // ??? As I do below in the code, maybe I should handle
		    //     exceptions raised from this write right here ???
		    os.write(chunk);
		}
		contentV.add(chunk);
		chunk=new byte[1024];
	    }
	    /*
	     * JPW 12/20/2005: Remove this call to os.write(); I don't think it
	     *                 is necessary, and it was causing an exception
	     *                 to be thrown when os is already closed
	    if (os!=null) {
		os.write(eol.getBytes());
	    }
	    */
	    content=new byte[contentLength];
	    int j=0;
	    for (int i=0;i<contentV.size();i++) {
		byte[] nextChunk=(byte[])contentV.elementAt(i);
		System.arraycopy(nextChunk,0,content,j,nextChunk.length);
		j+=nextChunk.length;
	    }
        }
        else if (contentLength>0)
	{
	    content=new byte[contentLength];
	    int numRead=br.read(content,0,contentLength);
	    if (numRead>=0) {
		while (numRead<contentLength) {
		    numRead+=br.read(content,numRead,contentLength-numRead);
		}
		// JPW 12/01/2005: Took out this readLine(); the application was
		//                 just hanging at this point waiting for a
		//                 timeout
		// br.readLine(); //final eol
		if (os!=null) {
		    // JPW 08/25/2006: Catch the exception from
		    //                 OutputStream.write(); assume the client
		    //                 has received the content and just
		    //                 closed the connection.
		    try {
			os.write(content);
		    } catch (Exception e) {
			if (debug > 2) {
			    System.err.println(
			        "Write response to client: ignore exception " +
				"(assume client has received content and " +
				"just closed the connection):");
			    e.printStackTrace();
			}
		    }
		    /*
		     * JPW 08/25/2006: Same as above, remove this call to
		     *                 os.write(); I don't think it is
		     *                 necessary, and it causes an exception
		     *                 to be thrown when os is already closed
		    os.write(eol.getBytes());
		    */
		}
	    } else { //no content actually there
		contentLength=0;
		headerOnly=true;
	    }
        }
        else if (isText)
	{
	    String nextLine=null;
	    while ((nextLine=br.readLine())!=null) {
		if (os!=null) {
		    os.write(nextLine.getBytes());
		    os.write(eol.getBytes());
		}
		bodyV.add(nextLine);
	    }
	    //strip trailing blank line
	    if (bodyV.size()>0) {
		bodyV.remove(bodyV.size()-1);
	    }
        }
      }
      // Close the OutputStream in the class that creates this Response object
      // if (os!=null) os.close();
    } catch (Exception e) {
	if (debug > 2) {
	    System.err.println("Response exception:");
	    e.printStackTrace();
	}
	isNull=true;
	throw e;
    }
    
  } //end constructor

  //get methods - parse info first time requested

  //isNull method - returns true if Response is null
  public boolean isNull() {
    return isNull;
  }
  
  //getCommand method - returns command of request
  public String getStatusCode() {
    if (isNull) return null;
    if (statusCode==null) parseFirstLine();
    return statusCode;
  }

  //parseFirstLine method - parses command,protocol,path,language
  private void parseFirstLine() {
    if (isNull) return;
    try {
	StringTokenizer st=new StringTokenizer(headerA[0]," :");
	st.nextToken(); // HTTP
	statusCode=st.nextToken(); // status code
    } catch (Exception e) {
	System.err.println("Response.parseFirstLine exception "+e.getMessage());
	System.err.println("  headerV:\n"+headerV.toString());
	e.printStackTrace();
    }
  }

  //getHeader method - returns header portion of response
  public String getHeader() {
    if (isNull) return null;
    if (header==null) {
      StringBuffer sb=new StringBuffer();
      for (int i=0;i<numHeaderLines;i++) {
        sb=sb.append(headerA[i]).append(eol);
      }
      header=sb.toString();
    }
    return header;
  }

  //getContent method - returns content portion of response, without trailing eol
  public byte[] getContent() {
    if (isNull) return null;
    if (headerOnly) return null;
    if (contentLength>0) return content;
    else if (isText) {
      StringBuffer sb=new StringBuffer();
      Enumeration e=bodyV.elements();
      while (e.hasMoreElements()) sb=sb.append((String)e.nextElement()).append(eol);
      return sb.toString().getBytes();
    } else {
      return null;
    }
  }

  //getResponse method - returns entire request as byte array
  public byte[] getResponse() {
    if (isNull) return null;
    if (headerOnly) return getHeader().getBytes();
    if (responseBytes==null) {
      byte[] headerBytes=getHeader().getBytes();
      byte[] bodyBytes=null;
      if (contentLength>0) bodyBytes=content;
      else if (isText) {
        StringBuffer sb=new StringBuffer();
        Enumeration e=bodyV.elements();
        while (e.hasMoreElements()) sb=sb.append((String)e.nextElement()).append(eol);
        bodyBytes=sb.toString().getBytes();
      }
      responseBytes=new byte[headerBytes.length+bodyBytes.length+eol.getBytes().length];
      System.arraycopy(headerBytes,0,responseBytes,0,headerBytes.length);
      System.arraycopy(bodyBytes,0,responseBytes,headerBytes.length,bodyBytes.length);
      System.arraycopy(eol.getBytes(),0,responseBytes,headerBytes.length+bodyBytes.length,eol.getBytes().length);
    }
    return responseBytes;
  }

  //setNotModified method - sets response code to Not Modified and clears content
  public void setNotModified() {
    if (isNull) return;
    headerV.remove(headerA[0]);
    headerV.add(0,notModifiedLine);
    //remove content length and last modified lines
    for (int i=1;i<numHeaderLines;i++) {
      if (headerA[i].regionMatches(true,0,contentLengthLabel,0,contentLengthLabel.length())) headerV.remove(headerA[i]);
      else if (headerA[i].regionMatches(true,0,lastModifiedLabel,0,lastModifiedLabel.length())) headerV.remove(headerA[i]);
      else if (headerA[i].regionMatches(true,0,dateLabel,0,dateLabel.length())) headerV.remove(headerA[i]);
    }
    //add current date
    // (These next lines were originally from the WebDate class)
    String format = new String("EEE, dd MMM yyyy HH:mm:ss zzz");
    SimpleDateFormat sdf=new SimpleDateFormat(format);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date date = new Date();
    headerV.add(1,"Date: "+ sdf.format(date));
    //make new headerA
    numHeaderLines=headerV.size();
    headerA=new String[numHeaderLines];
    headerV.copyInto(headerA);

    statusCode=null;
    responseBytes=null;
    isText=false;
    header=null;
    headerOnly=true;
    contentLength=0;
    content=null;
  }

  //setContent method - adds content as byte array
  public void setContent(byte[] contentI) {
    if (isNull) return;
    content=contentI;
    contentLength=content.length;
    headerOnly=false;
    isText=false;
  }
  
  public int getContentLength() {
      return contentLength;
  }
      
} //end class Response

