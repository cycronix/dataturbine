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

// BufferedRoutePI - implements a buffered routing plugin
// Eric Friets
// January 2002
// Copyright 2002 Creare Incorporated

package com.rbnb.bufferedRoute;

import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.ChannelMap;
import java.util.StringTokenizer;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

public class BufferedRoutePI {
  private int debug=0;
  private String serverAddress=new String("tcp://localhost:3333");
  private String piName=new String("BufferedRoute");
  private String cacheName=null;
  private String[] inputs=new String[0];
  private int numCacheFrames=1000;
  private int numArchiveFrames=-1;
  private boolean useArchive=false;
  private boolean newArchive=false;

  //main method
  public static void main(String[] arg) {
    BufferedRoutePI brpi=new BufferedRoutePI(arg);
    brpi.exec();
  }

  //constructor
  public BufferedRoutePI(String[] arg) {
    //parse command line arguments
    try{
      ArgHandler ah=new ArgHandler(arg);
      if (ah.checkFlag('u')) {
        System.err.println("BufferedRoutePI");
        System.err.println("  -u                  : print this usage info");
        System.err.println("  -a <server address> : address of server to connect to");
        System.err.println("              default : localhost:3333");
        System.err.println("  -n <name>           : name of BufferedRoute PlugIn");
        System.err.println("              default : BufferedRoute");
        System.err.println("  -c <num>            : cache (memory) frames");
        System.err.println("                      : default 1000");
        System.err.println("  -k <num>            : archive (disk) frames");
        System.err.println("                      : default 0");
        System.err.println("  -w                  : create a new archive");
        System.err.println("  -d <level>          : log debug to console");
        System.err.println("              default : 0 (no debug)");
        RBNBProcess.exit(0);
      }
      if (ah.checkFlag('d')) {
        String dlevel=ah.getOption('d');
        if (dlevel!=null) {
          debug=Integer.parseInt(dlevel);
          if (debug>0) System.err.println("debug level "+debug);
        }
      }
      if (ah.checkFlag('a')) {
        String serverAddressL=ah.getOption('a');
        if (serverAddressL!=null) serverAddress=serverAddressL;
        if (debug>0) System.err.println("server address "+serverAddress);
      }
      if (ah.checkFlag('n')) {
        String piNameL=ah.getOption('n');
        if (piNameL!=null) piName=piNameL;
        if (debug>0) System.err.println("buffered route name "+piName);
      }
      if (ah.checkFlag('c')) {
        numCacheFrames=Integer.parseInt(ah.getOption('c'));
        if (debug>0) System.err.println("numCacheFrames "+numCacheFrames);
      }
      if (ah.checkFlag('k')) {
        String naf=ah.getOption('k');
        if (naf!=null) numArchiveFrames=Integer.parseInt(naf);
        if (numArchiveFrames>0) useArchive=true;
        else numArchiveFrames=-1;
        if (debug>0) System.err.println("numArchiveFrames "+numArchiveFrames);
      }
      if (ah.checkFlag('w')) {
        newArchive=true;
        if (debug>0) System.err.println("newArchive "+newArchive);
      }
/*
      if (ah.checkFlag('i')) {
        String input=ah.getOption('i');
        if (input!=null) {
          if (debug>0) System.err.println("inputs "+input);
          StringTokenizer st=new StringTokenizer(input,",");
          int num=st.countTokens();
          inputs=new String[num];
          for (int i=0;i<num;i++) {
            inputs[i]=st.nextToken();
          }
        }
      }
*/
        
    } catch (Exception e) {
      System.err.println("Argument parsing exception: "+e.getMessage());
      e.printStackTrace();
      System.err.println("BufferedRoutePI");
      System.err.println("  -u                  : print this usage info");
      System.err.println("  -a <server address> : address of server to connect to");
      System.err.println("              default : localhost:3333");
      System.err.println("  -n <name>           : name of BufferedRoute PlugIn");
      System.err.println("              default : BufferedRoute");
      System.err.println("  -c <num>            : cache (memory) frames");
      System.err.println("                      : default 1000");
      System.err.println("  -k <num>            : archive (disk) frames");
      System.err.println("                      : default 0");
      System.err.println("  -w                  : create a new archive");
      System.err.println("  -d <level>          : log debug to console");
      System.err.println("              default : 0 (no debug)");
      RBNBProcess.exit(0);
    }
  } //end constructor


  //exec method
  // starts the program
  public void exec() {
    BRPlugIn plugin=null;
    BRCache cache=null;
    BRSink sink=null;

    PlugInChannelMap request=null;
    ChannelMap cacheResponse=null;
    ChannelMap srcResponse=null;
    ChannelMap response=null;

    boolean checkCache=false;
    boolean checkSink=false;

    try {
      if (debug>0) System.err.println("Starting BufferedRoute...");
      //create source
      plugin=new BRPlugIn(serverAddress,piName,inputs,debug);
      //create cache
      cache=new BRCache(serverAddress,useArchive,numCacheFrames,numArchiveFrames,newArchive,debug);
      cacheName=cache.getName();
	  if (cacheName==null) {
		  throw new Exception("Error starting cache for BufferedRoute...exiting.");
	  }
      //create sink
      sink=new BRSink(serverAddress,cacheName,debug);
	  cache.setSink(sink); //give sink reference to cache

      //loop handling requests - singlethreaded for now
      while (true) {
        request=plugin.getRequest(); //blocks until request comes in
		if (request==null) { //problem - clean up and exit
			throw new Exception("rbnbCache: Error receiving request from server.  Exiting.");
		}
        if (debug>3) System.err.println("request is "+request);
        String reference=request.GetRequestReference();

        //registration request
        if (reference.equalsIgnoreCase("registration")) {
          //pass request on
          // if only channel is ... or * checkSources will return empty map
          srcResponse=sink.checkSources(request,request.GetRequestStart(),request.GetRequestDuration(),"registration");
          if (false) /* (srcResponse.NumberOfChannels()>0||srcResponse.GetFolderList().length>0)*/ {
            //cache.register(srcResponse);
            answerRequest(request,srcResponse);
          } else {
            cacheResponse=sink.checkCache(request,request.GetRequestStart(),request.GetRequestDuration(),"registration");
            answerRequest(request,srcResponse,cacheResponse);
			//answerRequest(request,cacheResponse);
          }
        }
        
        //absolute request
        // note does not make inverse request - if cache has any data, that 
        // is the response
        else if (reference.equalsIgnoreCase("absolute")) {
          //check cache
          cacheResponse=sink.checkCache(request,request.GetRequestStart(),request.GetRequestDuration(),"absolute");
          //if data, return it
          if (cacheResponse!=null && cacheResponse.NumberOfChannels()>0) {
            answerRequest(request,cacheResponse);
          //else check sources
          } else { //no data found, check sources
            srcResponse=sink.checkSources(request,request.GetRequestStart(),request.GetRequestDuration(),"absolute");
            answerRequest(request,srcResponse);
            cache.put(srcResponse);
          }
        }

        //oldest request
        // note always checks sources, never checks cache
        // note does not update cache
        else if (reference.equalsIgnoreCase("oldest")) {
          //check sources
          srcResponse=sink.checkSources(request,request.GetRequestStart(),request.GetRequestDuration(),"oldest");
          answerRequest(request,srcResponse);
          cache.put(srcResponse);
        }

        //newest request, start!=0
        // note if start!=0, cannot break into modified/after request
        //      so always checks sources
        else if (reference.equalsIgnoreCase("newest") && request.GetRequestStart()!=0) {
          //check sources
          srcResponse=sink.checkSources(request,request.GetRequestStart(),request.GetRequestDuration(),"newest");
          answerRequest(request,srcResponse);
          cache.put(srcResponse);
        }

        //newest request, start==0
        else if (reference.equalsIgnoreCase("newest")) {
          double rd=request.GetRequestDuration();
          //get end times from cache
          Double[] endTimes=sink.getEndTimes(request);
          //for now, keep channels together - should be separated if endtimes differ
          double end=-1*Double.MAX_VALUE;
          for (int i=0;i<request.NumberOfChannels();i++) {
            if (endTimes[i]!=null && endTimes[i].doubleValue()>end) {
              end=endTimes[i].doubleValue();
            }
          }
          if (debug>4) System.err.println("latest end time "+end);
          //make after request of sources
          srcResponse=sink.checkSources(request,end,rd,"after");
          if (debug>1) System.err.println("srcResponse "+srcResponse);
          //if response empty, just request from cache
          if (srcResponse==null || srcResponse.NumberOfChannels()==0) {
            if (debug>3) System.err.println("sources had nothing");
            cacheResponse=sink.checkCache(request,0,rd,"newest");
            answerRequest(request,cacheResponse);
          }
          //if response nonempty, see if need more from cache
          else {
            //find duration of srcResponse
            double sd=-1*Double.MAX_VALUE;
            for (int i=0;i<srcResponse.NumberOfChannels();i++) {
              if (srcResponse.GetTimeDuration(i)>sd) sd=srcResponse.GetTimeDuration(i);
            }
            if (debug>4) System.err.println("source duration "+sd);
            if ((rd-sd)>(0.01*rd)) {
              if (debug>1) System.err.println("making cache request");
              cacheResponse=sink.checkCache(request,end-rd+sd,rd-sd,"absolute");
              answerRequest(request,cacheResponse,srcResponse);
            } else {
              if (debug>4) System.err.println("no cache request");
              answerRequest(request,srcResponse);
            }
            cache.put(srcResponse);
          }
        }

        //after request
        // not implemented - just pass on, put response into cache
        else if (reference.equalsIgnoreCase("after")) {
          srcResponse=sink.checkSources(request,request.GetRequestStart(),request.GetRequestDuration(),"after");
          if (srcResponse.NumberOfChannels()>0) {
            answerRequest(request,srcResponse);
            cache.put(srcResponse);
          } else {
            cacheResponse=sink.checkCache(request,request.GetRequestStart(),request.GetRequestDuration(),"after");
            answerRequest(request,cacheResponse);
          }
        }

        //modified request
        // not implemented - just pass on, put response into cache
        else if (reference.equalsIgnoreCase("modified")) {
          srcResponse=sink.checkSources(request,request.GetRequestStart(),request.GetRequestDuration(),"modified");
          if (srcResponse.NumberOfChannels()>0) {
            answerRequest(request,srcResponse);
            cache.put(srcResponse);
          } else {
            cacheResponse=sink.checkCache(request,request.GetRequestStart(),request.GetRequestDuration(),"modified");
            answerRequest(request,cacheResponse);
          }
        }
 
        //unknown request type
        else {
          System.err.println("unknown request type: "+reference);
        }

        //send back answer
        if (debug>3) System.err.println("response is "+request);
        plugin.sendResponse(request);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      if (plugin!=null) plugin.close();
      if (cache!=null) cache.close();
      if (sink!=null) sink.close();
      RBNBProcess.exit(0);
    }
  }//end exec method

  //answerRequest method - copies channels and data into request from channelmap
  public void answerRequest(PlugInChannelMap request, ChannelMap response) {
    try {
	  //EMF 5/2003: add folder handling
	  String[] folder=response.GetFolderList();
	  for (int i=0;i<folder.length;i++) {
		  if (folder[i].startsWith(cacheName)) folder[i]=folder[i].substring(cacheName.length());
		  if (folder[i].startsWith("/")) folder[i]=folder[i].substring(1);
		  if (folder[i].length()>0) request.AddFolder(folder[i]);
	  }
      for (int i=0;i<response.NumberOfChannels();i++) {
        String chan=response.GetName(i);
        if (chan.startsWith(cacheName)) chan=chan.substring(cacheName.length());
        if (chan.startsWith("/")) chan=chan.substring(1);
        int idx=request.GetIndex(chan);
        if (idx==-1) {
          idx=request.Add(chan);
        }
        request.PutTimeRef(response,i);
		request.PutDataRef(idx,response,i);
        //request.PutData(idx,response.GetData(i),response.GetType(i));
        //request.PutMime(idx,response.GetMime(i));
      }
    } catch (Exception e) {
      System.err.println("Exception generating response: "+e.getMessage());
      e.printStackTrace();
    }
  } //end answerRequest method

  public void answerRequest(PlugInChannelMap request, ChannelMap response1, ChannelMap response2) {
    try {
	  //EMF 5/2003: add folder handling
	  String[] folder=response1.GetFolderList();
	  for (int i=0;i<folder.length;i++) {
		  if (folder[i].startsWith(cacheName)) folder[i]=folder[i].substring(cacheName.length());
		  if (folder[i].startsWith("/")) folder[i]=folder[i].substring(1);
		  if (folder[i].length()>0) request.AddFolder(folder[i]);
	  }
	  folder=response2.GetFolderList();
	  for (int i=0;i<folder.length;i++) {
		  if (folder[i].startsWith(cacheName)) folder[i]=folder[i].substring(cacheName.length());
		  if (folder[i].startsWith("/")) folder[i]=folder[i].substring(1);
		  if (folder[i].length()>0) request.AddFolder(folder[i]);
	  }

      for (int i=0;i<response1.NumberOfChannels();i++) {
        String chan=response1.GetName(i);
        if (chan.startsWith(cacheName)) chan=chan.substring(cacheName.length());
        if (chan.startsWith("/")) chan=chan.substring(1);
        int idx=request.GetIndex(chan);
        if (idx==-1) {
          idx=request.Add(chan);
        }
        request.PutTimeRef(response1,i);
		request.PutDataRef(idx,response1,i);
        //request.PutData(idx,response1.GetData(i),response1.GetType(i));
        //request.PutMime(idx,response1.GetMime(i));
      }
      for (int i=0;i<response2.NumberOfChannels();i++) {
        String chan=response2.GetName(i);
        if (chan.startsWith(cacheName)) chan=chan.substring(cacheName.length());
        if (chan.startsWith("/")) chan=chan.substring(1);
        int idx=request.GetIndex(chan);
        if (idx==-1) {
          idx=request.Add(chan);
        }
        request.PutTimeRef(response2,i);
		request.PutDataRef(idx,response2,i);
        //request.PutData(idx,response2.GetData(i),response2.GetType(i));
        //request.PutMime(idx,response2.GetMime(i));
      }
    } catch (Exception e) {
      System.err.println("Exception generating response: "+e.getMessage());
      e.printStackTrace();
    }
  } //end answerRequest method
    

}//end BufferedRoutePI class

