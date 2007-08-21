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
// EMF
// 11/15/01
// Copyright 2001 Creare Incorporated

package com.rbnb.webCache;

import com.rbnb.utility.ParseURL; //EMF 5/11/06: add munge handling; use this new class

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PipedInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

public class Request {
  private static String contentLabel=new String("Content-Length: ");
  private static String modifiedLabel=new String("If-Modified-Since: ");
  private static String eol=new String("\r\n");
  private static String optionsCommand=new String("OPTIONS");

  private PipedInputStream pis=null;
  private int debug=0;
  private String source=null;
  private boolean isNull=false;
  private boolean doNullResponse=false;
  private int numHeaderLines=0;
  private String[] headerA=null;
  private String firstLineNoHost=null;
  private int contentLength=0;
  private char[] content=null;
  private WebDate modifiedDate=null;

  private String command=null;
  private String protocol=null;
  private String path=null;
  private String language=null;
  private String host=null;
  private int port=80;
  private String requestString=null;
  private String requestStringNoHost=null;
  
  private ParseURL purl = null;

  public Request(PipedInputStream pisI,int debugI,String source) {
    pis=pisI;
    debug=debugI;
    source=source;
    boolean done=false;
    Vector headerV=new Vector();
    int nAvail=0;
    int nRead=0;

    BufferedReader br=new BufferedReader(new InputStreamReader(pis));
    try {
      //read header
      while (!done) {
        String nextLine=br.readLine();
if (debug>4) System.err.println("Request nextLine "+nextLine);
        if (nextLine==null) done=true;
        else {
          if (nextLine.length()==0) done=true;
          headerV.add(nextLine);
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
          try {
            modifiedDate=new WebDate(modifiedString);
          } catch (Exception e) {
            System.err.println("Exception parsing date: "+modifiedString);
          }
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

  //get methods - parse info first time requested

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
  
  //EMF 5/12/06: return purl for upstream usage
  public ParseURL getPURL() {
      return purl;
  }

  //parseFirstLine method - parses command,protocol,path,language
  private void parseFirstLine() {
    if (isNull) return;
    if (headerA==null || headerA.length<1) {
      isNull=true;
      return;
    }
    //EMF 5/11/06: use ParseURL class
    //StringTokenizer st=new StringTokenizer(headerA[0]," :");
    StringTokenizer st=new StringTokenizer(headerA[0]," ");
    command=st.nextToken();
    String url=st.nextToken();
    purl=new ParseURL(url,true);
    protocol=purl.getProtocol();
    if (protocol==null) protocol="http"; //kludge, may be bad assumption
    if (command.equals(optionsCommand)) {
      path=protocol;
      doNullResponse=true;
      return;
    } else {
      //path=st.nextToken().substring(2); //string leading //
      path=purl.getRequest();
    }
    language=st.nextToken();
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

  //getModifiedDate method - returns If-Modified-Since as WebDate
  //                         (returns null if not set)
  public WebDate getModifiedDate() {
    if (isNull) return null;
    return modifiedDate;
  }

  //setModifiedDate method - sets if-mod-since line to specified date
  public void setModifiedDate(WebDate modifiedDateI) {
    if (isNull) return;
    if (modifiedDate==null) {
      modifiedDate=modifiedDateI;
      String[] newHeaderA=new String[numHeaderLines+1];
      newHeaderA[0]=headerA[0];
      newHeaderA[1]=modifiedLabel+modifiedDate.getDateString();
      System.arraycopy(headerA,1,newHeaderA,2,numHeaderLines-1);
      /*//put modified line third from end
      for (int i=0;i<numHeaderLines-3;i++) {
        newHeaderA[i]=headerA[i];
      }
      newHeaderA[numHeaderLines-3]=modifiedLabel+modifiedDate.getDateString();
      for (int i=numHeaderLines-2;i<numHeaderLines+1;i++) {
        newHeaderA[i]=headerA[i-1];
      }
      */
      numHeaderLines++;
      headerA=newHeaderA;
    } else {
    modifiedDate=modifiedDateI;
      for (int i=0;i<numHeaderLines;i++) {
        if (headerA[i].regionMatches(true,0,modifiedLabel,0,modifiedLabel.length())) {
          headerA[i]=modifiedLabel+modifiedDate.getDateString();
          i=numHeaderLines;
        }
      }
    }
    requestString=null;
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

