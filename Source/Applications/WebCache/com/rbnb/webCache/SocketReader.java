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

// SocketReader.java - Socket reader that uses a separate thread to provide
//                     a sane nonblocking read method, which InputStream
//                     doesn't  provide
// Eric Friets
// September 2001
// Copyright 2001 Creare Incorporated

package com.rbnb.webCache;

import java.io.InputStream;
import java.io.PipedOutputStream;
import java.io.EOFException;
import java.net.Socket;

public class SocketReader implements Runnable {

  private Socket socket=null;
  private PipedOutputStream pos=null;
  private InputStream is=null;
  private boolean atEOF=false;
  private Thread thisThread=null;
  private int debug=0;
  private String path;

  public SocketReader(Socket socketI,PipedOutputStream posI,int debugI,String pathI) {
    socket=socketI;
    pos=posI;
    debug=debugI;
    path=pathI;
    if (debug>4) System.err.println("new SocketReader on "+socket);
    //start thread to read
    thisThread=new Thread(this);
    thisThread.start();
  }

  public void close() {
    try {
      atEOF=true;
      pos.close();
      is.close();
      socket.close();
      //if (thisThread!=null) {
        //System.err.println("Calling interrupt on "+thisThread);
        //thisThread.interrupt();
      //}
    } catch (Exception e) {
      System.err.println("SocketReader.close exception "+e.getMessage());
    }
  }

  public void run() {
    int num=0;
    byte[] nextDat=new byte[1024]; //optimum size??

    //get input stream
    try {
      socket.setSoTimeout(1000);
      is=socket.getInputStream();
    } catch (Exception e) {
      System.err.println("SocketReader exception opening InputStream: "+e.getMessage());
      e.printStackTrace();
      atEOF=true;
      return;
    }

    try {
      while (!atEOF) {
        try {
          num=is.read(nextDat);
          if (debug>4) System.err.println("read "+num+" on "+path);
        } catch (Exception e) {
          if (e.getMessage().indexOf("timed out")==-1) {
            throw e;
          } else {
            num=0;
          }
        }
        if (num==-1) {
          atEOF=true;
        } else if (num>0) {
          pos.write(nextDat,0,num);
          pos.flush();
        }
      } //end while
      if (debug>4) System.err.println("SocketReader atEOF on: "+path);
    } catch (Exception e) {
      atEOF=true;
      if (debug>4) System.err.println("SocketReader atEOF (exception) on: "+path);
    }
    close();
  } //end run method
      
} //end class SocketReader

