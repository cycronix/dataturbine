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
// EMF
// 11/15/01
// Copyright 2001 Creare Incorporated

package com.rbnb.webCache;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
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
  private String source=null;
  private boolean headerOnly=false;
  private Vector headerV=new Vector();
  private int numHeaderLines=0;
  private boolean isNull=false;
  private String[] headerA=null;
  private String firstLineNoHost=null;
  private boolean isText=false;
  private int contentLength=0;
  private WebDate modifiedDate=null;
  private byte[] content=null;
  private Vector bodyV=new Vector();

  private String statusCode=null;
  private String header=null;
  private byte[] responseBytes=null;

  public Response(String headerI,int debugI,String sourceI) {
    this(new ByteArrayInputStream(headerI.getBytes()),null,debugI,sourceI,true);
    header=headerI;
  }

  public Response(InputStream is,OutputStream os,int debugI,String sourceI) {
    this(is,os,debugI,sourceI,false);
  }

  public Response(InputStream is,OutputStream os,int debugI,String sourceI,boolean headerOnlyI) {

    debug=debugI;
    source=sourceI;
    headerOnly=headerOnlyI;
    boolean done=false;
    int nAvail=0;
    int nRead=0;

    //BufferedReader br=new BufferedReader(new InputStreamReader(is));
    DataInputStream  br=new DataInputStream (is);
    try {
      //read header
      while (!done) {
        String nextLine=br.readLine();
if (debug>4) System.err.println("Response nextLine "+nextLine);
        if (nextLine==null) done=true;
        else {
          if (os!=null) {
            os.write(nextLine.getBytes());
            os.write(eol.getBytes());
          }
          if (nextLine.length()==0) done=true;
          headerV.add(nextLine);
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
          try {
            modifiedDate=new WebDate(headerA[i].substring(lastModifiedLabel.length()).trim());
          } catch (Exception e) {}
        }
      }
      if (!headerOnly) {
        //check for null response
        if (numHeaderLines==0 || headerV.elementAt(0)==null || ((String)(headerV.elementAt(0))).equals("Error")) {
          if (debug>1) System.err.println("Response null header");
          isNull=true;
        //check for binary content, length unspecified
        // isText reading fails for articles in dailynews.yahoo.com and 
        // other sites, so read as binary...
        } else if (/*!isText &&*/ contentLength==0 && getStatusCode().equals("200")) {
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
            if (os!=null) os.write(chunk);
            contentV.add(chunk);
            chunk=new byte[1024];
          }
          if (os!=null) {
            os.write(eol.getBytes());
          }
          content=new byte[contentLength];
          int j=0;
          for (int i=0;i<contentV.size();i++) {
            byte[] nextChunk=(byte[])contentV.elementAt(i);
            System.arraycopy(nextChunk,0,content,j,nextChunk.length);
            j+=nextChunk.length;
          }
        }
        else if (contentLength>0) {
          content=new byte[contentLength];
          int numRead=br.read(content,0,contentLength);
          if (numRead>=0) {
            while (numRead<contentLength) {
              numRead+=br.read(content,numRead,contentLength-numRead);
            }
            br.readLine(); //final eol
            if (os!=null) {
              os.write(content);
              os.write(eol.getBytes());
            }
          } else { //no content actually there
            contentLength=0;
            headerOnly=true;
          }
        }
        else if (isText) {
          String nextLine=null;
          while ((nextLine=br.readLine())!=null) {
            if (os!=null) {
              os.write(nextLine.getBytes());
              os.write(eol.getBytes());
            }
            bodyV.add(nextLine);
          }
          //strip trailing blank line
          if (bodyV.size()>0) bodyV.remove(bodyV.size()-1);
        }
      }
      if (os!=null) os.close();
    } catch (Exception e) {
      System.err.println("Response exception: "+e.getMessage());
      System.err.println("  os "+os);
      System.err.println("header "+getHeader());
      e.printStackTrace();
      isNull=true;
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

  //getModifiedDate method - returns modification date as WebDate
  public WebDate getModifiedDate() {
    return modifiedDate;
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
    headerV.add(1,"Date: "+(new WebDate()).getDateString());
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
      
} //end class Response

