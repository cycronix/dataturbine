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

// SocketHandler - handles one web connection
// Eric Friets
// August 31, 2001
// Copyright 2001 Creare Incorporated

// Passes data between web client and web server, while copying it to a file.
// Since the protocol is stateless, read from client, then read from server,
// with no need to read simultaneously.

package com.rbnb.webCache;

import com.rbnb.utility.ParseURL;

import java.net.Socket;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class SocketHandler extends Thread {
  private Socket browser=null;
  private String proxyHost=null;
  private int proxyPort=80;
  private CacheInterface ci=null;
  private SocketReader bsr=null; //browser socket reader
  private OutputStream bos=null; //broser output stream
  private PipedOutputStream bpos=null; //browser piped output stream
  private PipedInputStream bpis=null; //browser piped input stream
  private Socket server=null; //socket to web server
  private SocketReader ssr=null; //server socket reader
  private OutputStream sos=null; //server output stream
  private PipedOutputStream spos=null; //server piped output stream
  private PipedInputStream spis=null; //server piped input stream
  private int debug=0;
  private Request request=null;
  private WebDate requestModifiedDate=null;
  private Response response=null;
  private WebDate responseModifiedDate=null;
  private static int cacheCount=0;

  //constructor - background request
  public SocketHandler(Request requestI,String proxyHostI,int proxyPortI, CacheInterface ciI,int debugI) {
    request=requestI;
    proxyHost=proxyHostI;
    proxyPort=proxyPortI;
    ci=ciI;
    debug=debugI;
    System.err.println("background request");
  } //end constructor


  //constructor - request from browser
  public SocketHandler(Socket browserI,String proxyHostI,int proxyPortI, CacheInterface ciI,int debugI) {
    browser=browserI;
    proxyHost=proxyHostI;
    proxyPort=proxyPortI;
    ci=ciI;
    debug=debugI;
    System.err.println("request from browser");
  } //end constructor

  //run method
  public void run() {
    boolean done=false;
    int num=0;
    byte[] header=null;
    byte[] content=null;
    boolean toCache=false;
    boolean fromCache=false;
    boolean doWebFetch=false;

    //set up streams
    if (browser!=null) {
      try {
        bpos=new PipedOutputStream();
        bpis=new PipedInputStream(bpos);
        //bsr=new SocketReader(browser,bpos,debug,"browser");
        bsr=new SocketReader(browser,bpos,0,"browser");
        bos=browser.getOutputStream();
      } catch (Exception e) {
        System.err.println("Exception getting streams for client socket: "+e.getMessage());
        e.printStackTrace();
      }
    }
    

    try {
      //read request from browser
      try {
      if (browser!=null) {
        //build request
        //request=new Request(bpis,debug,"browser");
        request=new Request(bpis,0,"browser");
        requestModifiedDate=request.getModifiedDate();
        if (debug>3) System.err.println("request:\n"+request.getRequest());
      }
    } catch (Exception e) {
      if (!(e instanceof EOFException)) {
        System.err.println("Exception "+e.getMessage());
        e.printStackTrace();
      }
      throw e;
    }

    //11/9/01: if background request, do not check cache
    if (request.isNull() || request.doNullResponse()) {
      toCache=false;
      fromCache=false;
      doWebFetch=false;
    } else if (browser==null) {
      toCache=true;
      fromCache=false;
      doWebFetch=true;
    } else if (request.getCommand().equals("GET") && request.getProtocol().equals("http")) { //decide if appropriate for caching
        toCache=true;
        fromCache=true;
    } else {
      doWebFetch=true;
    }
    
    System.err.println("\n"+this+": toCache="+toCache+", fromCache="+fromCache+", doWebFetch="+doWebFetch+"\n");

    //more sophisticated analysis to come later
    
    //EMF 5/12/06: if request includes RBNB munge, just check from cache
    ParseURL purl=request.getPURL();
    rbnb_munge:
    if (fromCache && purl.isRBNBMunge()) {
	// NOTE: There could still be a non-RBNB munge in the request URL; should this be considered as well
	//       or do we only look in the cache if there is specifically an RBNB munge option (such as "t", "d", etc)?
	System.err.println("isRBNBMunge");
	//ignore if zero duration newest request
	if ((purl.getReference()!=null&&purl.getReference().startsWith("n"))
	    &&(purl.getDuration()==null||purl.getDuration().doubleValue()==0)) break rbnb_munge;
	System.err.println("not newest 0 dur");
	//temporary: ignore if time is close to current time (TimeDrive always sends
	//           absolute time, at least for now)
	double now=System.currentTimeMillis()/1000.0;
	if (purl.getTime()!=null && now-purl.getTime().doubleValue()<1.0) break rbnb_munge;
	System.err.println("more than one second ago");
	fromCache=false;
	doWebFetch=false;
	System.err.println("request had RBNB munge; requesting from cache");
	// NOTE: What if getNonRBNBMungeStr() returns null; will this be a problem?
	System.err.println("non-rbnb request string: "+purl.getRequest()+"?"+purl.getNonRBNBMungeStr());
	header=ci.getHeader(purl.getRequest()+"?"+purl.getNonRBNBMungeStr());
	System.err.println("header = "+header);
//needs modified date set...
	if (header==null) { //sent empty response
	    response=null;
	} else {
	    double start=(purl.getTime()==null) ? 0.0 : purl.getTime().doubleValue();
	    double duration=(purl.getDuration()==null) ? 0.0 : purl.getDuration().doubleValue();
	    String reference=(purl.getReference()==null) ? "newest" : purl.getReference();
	    // JPW 09/28/2006: "msg" munge no longer has special meaning to the RBNB servlet;
	    //                 therefore, replace getCompleteMessageMunge() (which creates the
	    //                 encoded content to be put in the "msg" munge) with
	    //                 getNonRBNBMungeStr()
	    // NOTE: What if getNonRBNBMungeStr() returns null; will this be a problem?
	    content=ci.getData(purl.getRequest()+"?"+purl.getNonRBNBMungeStr(),start,duration,reference);
	    //response=new Response(new ByteArrayInputStream(header),null,debug,request.getPath(),true);
	    response=new Response(new ByteArrayInputStream(header),null,0,request.getPath(),true);
	    if (content==null) response.setNotModified();
	    else response.setContent(content);
	    bos.write(response.getResponse());
	}
	try {
	    if (response!=null) bos.write(response.getResponse());
	    bos.close();
	} catch (Exception e) {
	    System.err.println("Exception sending cached data to browser: "+e.getMessage());
	    e.printStackTrace();
	    throw new Exception();
	}
    }

    //check cache for copy
    if (fromCache) {
      boolean needContent=true;
      header=ci.getHeader(request.getPath());
      if (header==null) {
        if (debug>0) System.err.println("miss:     "+request.getPath());
        doWebFetch=true;
      } else {
        if (debug>0) System.err.println("found:    "+request.getPath());
        //response=new Response(new ByteArrayInputStream(header),null,debug,request.getPath(),true);
        response=new Response(new ByteArrayInputStream(header),null,0,request.getPath(),true);
        responseModifiedDate=response.getModifiedDate();
        //see if modified
System.err.println("requestModifiedDate "+requestModifiedDate);//.getDateString());
System.err.println("responseModifiedDate "+responseModifiedDate);//.getDateString());
        if (responseModifiedDate!=null) {
          if (requestModifiedDate==null) request.setModifiedDate(responseModifiedDate);
          else {
            if (/*!*/requestModifiedDate.laterThan(responseModifiedDate)) {
              response.setNotModified();
              request.setModifiedDate(responseModifiedDate);
              needContent=false;
              if (debug>1) System.err.println("not modified: "+request.getPath());
            }
          }
        }
        if (needContent) {
          content=ci.getData(request.getPath());
          response.setContent(content);
        }
        try {
          bos.write(response.getResponse());
          bos.close();
          //11/9/01: start background request to update cache
          if (debug>1) System.err.println("starting background update for "+request.getPath());
          (new SocketHandler(request,proxyHost,proxyPort,ci,debug)).start();
        } catch (Exception e) {
          System.err.println("Exception sending cached data to browser: "+e.getMessage());
          e.printStackTrace();
          throw new Exception();
        }
      }
    }

    //send request on to web server, pass response on to client
    // build copy for caching if appropriate
    if (doWebFetch) {

      //create server socket streams
      try {
        if (proxyHost!=null) {
          server=new Socket(proxyHost,proxyPort);
        } else {
          server=new Socket(request.getHost(),request.getPort());
        }
        spos=new PipedOutputStream();
        spis=new PipedInputStream(spos);
        ssr=new SocketReader(server,spos,debug,request.getPath());
        sos=server.getOutputStream();
      } catch (Exception e) {
        if (proxyHost!=null) {
          System.err.println("Exception getting streams for server "+proxyHost+":"+proxyPort+" "+e.getMessage());
        } else {
          System.err.println("Exception getting streams for server "+request.getHost()+":"+request.getPort()+" "+e.getMessage());
        }
        throw e;
      }

      try {
        if (debug>1) System.err.println("requesting from web: "+request.getPath());
        if (debug>2) System.err.println("request is\n"+request.getRequest());
        if (proxyHost!=null) {
          sos.write(request.getRequest().getBytes());
        } else {
          sos.write(request.getRequestNoHost().getBytes());
        }
        //build response
        response=new Response(spis,bos,debug,request.getPath());
     
        //put copy into cache
        if (request.getCommand().equals("GET") && !response.isNull() && response.getStatusCode().equals("200")) {
          if (debug>0) {
            if (browser==null) System.err.println(this+" add("+(++cacheCount)+"): "+request.getPath());
            else System.err.println("new("+(++cacheCount)+"): "+request.getPath());
          }
          ci.putData(request.getPath(),response.getHeader().getBytes(),response.getContent());
        } else {
          if (debug>0) System.err.println("not mod:  "+request.getPath());
        }

        //parse off response header, print out
        if (debug>3) System.err.println(response.getHeader());

      } catch (Exception e) {
        System.err.println("Exception reading data from web: "+e.getMessage());
        System.err.println("path "+request.getPath());
        System.err.println("response header "+response.getHeader());
        System.err.println("fromCache "+fromCache+", doWebFetch "+doWebFetch);
        e.printStackTrace();
        throw new Exception();
      } 
    } //end if (doWebFetch)


    // all done, by error or completion of work, so close all streams
    if (bos!=null) bos.close();
    if (bsr!=null) bsr.close();
    if (sos!=null) sos.close();
    if (ssr!=null) ssr.close();

    } catch (Exception e) {
      e.printStackTrace();
      try {
        if (debug>1) System.err.println("Closing all sockets on "+this);
        if (bos!=null) bos.close();
        if (bsr!=null) bsr.close();
        if (sos!=null) sos.close();
        if (ssr!=null) ssr.close();
      } catch (Exception ee) {
        System.err.println("Excpetion closing sockets: "+ee.getMessage());
        ee.printStackTrace();
      }
    }
      
  } //end run method

} //end SocketHandler class

