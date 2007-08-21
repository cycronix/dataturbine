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

/*
  *****************************************************************
  ***                                                           ***
  ***   Name : AppletFrame      ()                              ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date :                                                  ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : creates frame, initializes and starts     ***
  ***                 applet in it, handles window close events,***
  ***                 implements applet stub and context methods***
  ***                                                           ***
  ***   Input :  applet to run, preferred size, whether to show ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***                                                           ***
  ***	Modification History :					***
  ***	   04/07/2004 - INB					***
  ***		Added methods (get/set)Stream and		***
  ***		getStreamKeys (new in Java 1.4).		***
  ***								***
  *****************************************************************
*/

package com.rbnb.plot;

import com.rbnb.utility.RBNBProcess;
import java.applet.AudioClip;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Enumeration;

// idea from Core Java 1.1 - volume 1, chapter 10

   public class AppletFrame extends Frame
   implements AppletStub, AppletContext {

// constructor
public	AppletFrame(Applet a, int x, int y, boolean showFrame) {
		setTitle(a.getClass().getName());
		setSize(x,y);
		add("Center",a);
		a.setStub(this);
		a.init();
		if (showFrame) show();
		a.start();
      addWindowListener(new CloseClass());
		}

// anonymous inner classes do not work on all machines
// use named inner classes for now

// CloseClass class handles window events
   class CloseClass extends WindowAdapter {
      public void windowClosing(WindowEvent event) {
         dispose();
         RBNBProcess.exit(0);
      }
   }

// AppletStub methods
   public boolean isActive() { return true; }
   public URL getDocumentBase() { return null; }
   public URL getCodeBase() { return null; }
   public String getParameter(String name) { return ""; }
   public AppletContext getAppletContext() { return this; }
   public void appletResize(int width, int height) {}
   
// AppletContext methods
   public AudioClip getAudioClip(URL url) { return null; }
   public Image getImage(URL url) { return null; }
   public Applet getApplet(String name) { return null; }
   public Enumeration getApplets() { return null; }
   public java.io.InputStream getStream(String key) { return null; };
   public java.util.Iterator getStreamKeys() { return null; };
   public void setStream(String key,java.io.InputStream stream) {};
   public void showDocument(URL url) {}
   public void showDocument(URL url, String target) {}
   public void showStatus(String status) {}

   } // end class AppletFrame
