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

// WebCache.java - a caching proxy for http
// Eric Friets
// August 31, 2001
// Copyright 2001 Creare Incorporated

// Listens on a server socket, spawning a thread to handle each connection.
// This first version just copies the requests and responses to a file,
// passing all requests on (no caching).

package com.rbnb.webCache;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

public class WebCache {
  private String proxyHost=null;
  private int proxyPort=80;
  private int socket=6677;;
  private String rbnbServer="localhost:3333";
  private int debug=0;
  private int numCacheFrames=1000;
  private int numArchiveFrames=-1;
  private boolean useArchive=false;
  private boolean newArchive=false;
  private CacheInterface ci=null;
  private boolean shutdown=false;

  //main method
  public static void main(String[] arg) {
    WebCache tc=new WebCache(arg);
    tc.exec();
  }

  //constructor
  public WebCache(String[] arg) {
    try {
      ArgHandler ah=new ArgHandler(arg);
      if (ah.checkFlag('h')) {
        System.err.println("WebCache");
        System.err.println("  -h                : print this usage info");
        System.err.println("  -s <serversocket> : specify socket to listen on");
        System.err.println("            default : 6677");
        System.err.println("  -p <proxy>        : web proxy as host:port");
        System.err.println("            default : no proxy");
        System.err.println("  -r <rbnbServer>   : rbnb server to cache to");
        System.err.println("            default : localhost:3333");
        System.err.println("  -c <cacheFrames>  : number of cache frames");
        System.err.println("            default : 1000");
        System.err.println("  -a <archiveFrames>: number of archive frames");
        System.err.println("            default : existing size or cacheFrames");
        System.err.println("  -n                : create a new archive");
        System.err.println("  -d <level>        : print debug info to console");
        System.err.println("            default : 0 (no debug)");
        RBNBProcess.exit(0);
      }
      if (ah.checkFlag('d')) {
        debug=Integer.parseInt(ah.getOption('d'));
        if (debug>0) System.err.println("debug level "+debug);
       }
       if (ah.checkFlag('s')) {
         String socketString=ah.getOption('s');
         if (socketString!=null) {
           socket=Integer.parseInt(ah.getOption('s'));
           if (debug>0) System.err.println("serverSocket "+socket);
         }
       }
       if (ah.checkFlag('r')) {
         rbnbServer=ah.getOption('r');
         if (debug>0) System.err.println("rbnbServer "+rbnbServer);
       }
       if (ah.checkFlag('c')) {
         numCacheFrames=Integer.parseInt(ah.getOption('c'));
         if (debug>0) System.err.println("numCacheFrames "+numCacheFrames);
         if (numCacheFrames<1) throw new Exception("numCacheFrames must be >0");
       }
       if (ah.checkFlag('a')) {
         String naf=ah.getOption('a');
         if (naf!=null) numArchiveFrames=Integer.parseInt(naf);
         if (numArchiveFrames>0) useArchive=true;
         else numArchiveFrames=-1;
         if (debug>0) System.err.println("numArchiveFrames "+numArchiveFrames);
       }
       if (ah.checkFlag('n')) {
         newArchive=true;
         if (debug>0) System.err.println("newArchive "+newArchive);
       }
       if (ah.checkFlag('p')) {
         String proxy=ah.getOption('p');
         if (proxy!=null) {
           int colon=proxy.indexOf(':');
           proxyHost=proxy.substring(0,colon);
           proxyPort=Integer.parseInt(proxy.substring(colon+1));
           if (debug>0) System.err.println("proxyHost "+proxyHost+", proxyPort "+proxyPort);
         }
       }
    } catch (Exception e) {
      System.err.println("Argument parsing exception: "+e.getMessage());
      e.printStackTrace();
      System.err.println("WebCache");
      System.err.println("  -h                : print this usage info");
      System.err.println("  -s <serversocket> : specify socket to listen on");
      System.err.println("            default : 6677");
      System.err.println("  -p <proxy>        : web proxy as host:port");
      System.err.println("            default : no proxy");
      System.err.println("  -r <rbnbServer>   : rbnb server to cache to");
      System.err.println("            default : localhost:3333");
      System.err.println("  -c <cacheFrames>  : number of cache frames");
      System.err.println("            default : 1000");
      System.err.println("  -a <archiveFrames>: number of archive frames");
      System.err.println("            default : existing size or cacheFrames");
      System.err.println("  -n                : create a new archive");
      System.err.println("  -d <level>        : print debug info to console");
      System.err.println("            default : 0 (no debug)");
      RBNBProcess.exit(0);
    }
  } //end constructor

  //exec method
  // starts the program
  public void exec() {
    int filenum=1;
    ServerSocket server=null;

    try {
      //start listening
      server=new ServerSocket(socket);
      server.setSoTimeout(1000); //one second timeout
      if (debug>0) System.err.println("Listening on port "+socket);

      //start CacheInterface
      ci=new CacheInterface(this,rbnbServer,useArchive,numCacheFrames,
                            numArchiveFrames,newArchive,debug);

      //handle connections by spawning threads
      while (!shutdown) {  //run forever
        try {
          Socket newConnect=server.accept();
          (new SocketHandler(newConnect,proxyHost,proxyPort,ci,debug)).start();
        } catch (InterruptedIOException iie) {
          if (iie.getMessage().indexOf("timed out") == -1) {
            throw iie;
          }
        } catch (SocketException se) {
          if ((se.getMessage().indexOf("Interrupted") == -1)&&(se.getMessage().indexOf("timed out") == -1)) throw se;
        }
      }
    } catch (Exception e) {
      System.err.println("Exception "+e.getMessage());
      e.printStackTrace();
    }
  try {
    if (server!=null) {
      server.close();
      System.err.println("WebCache: released server socket "+server);
      System.gc(); //system server socket not released on close;
                   // garbage collector appears to run finalize and do better
    }
  } catch (Exception e) {
    System.err.println("Exception closing server socket: "+e.getMessage());
    e.printStackTrace();
  }
  } //end exec method

  public void doShutdown() {
    shutdown=true;
  }

} //end WebCache class

