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

// Environment class - contains static variables reflecting the state
// of configuration and execution.

//EMF 11/8/00: To allow multiple instances of rbnbPlot to run within the
//             same JVM, as with rbnbManager or via a PlugIn wrapper,
//             many of these variables were changed from class variables to
//             instance variables.  The code was changed to have RBNBPlotMain
//             create an instance of this class, which is passed to those 
//             classes needing access.  Variables that can be shared between
//             instances, such as fonts, were left as class variables.
//             Note that to minimize the changes to other parts of the code,
//             the variable names have been left in all caps, though this
//             does not follow Java conventions.

//JPW 09/21/12: Add SCALE_AUTO and SCALE_DECREASE to the environment


package com.rbnb.plot;

//EMF 5/18/01: use new replacements
//import COM.Creare.RBNB.Time;
//import COM.Creare.RBNB.API.Connection;
import java.awt.Color;
import java.awt.Font;

public class Environment {

public String	HOST = null; //new String("localhost");
public int	PORT = 3333;
public String	EXPORT = null;	// export-to-database flag - mjm
//public boolean	STATICMODE = false;
public boolean	STATICMODE = true; //EMF 2/22/02: change default to staticmode
public boolean	KILLRBNB = false;
public int DISPLAYGROUPS = 10;
public int POSITION_X = -1; //if -1, window will be set to center of screen
public int POSITION_Y = -1;
public Time DURATION = null; // if null, estimate from data source
public Time MAXDURATION = null; //duration slider max position
public String TIME_LABEL = new String(" Sec");
// JPW 9/21/2012: by default, start plot containers in increasing autoscale mode
public boolean SCALE_AUTO = true;
public boolean SCALE_DECREASE = false;
public int SCALE_DIV = 5;
public double SCALE_MIN = 0.0;
public double SCALE_MAX = 1.0;
public int RTWAIT = 0; //wait time in milliseconds during RT mode
public static Color BGCOLOR = new Color(236,233,216);
//# ifdef DUMMYVERSION
public static final String VERSION = new String("Dev");
//# else
//+public static final String VERSION = SERIALVERSION;
//# endif
public static Font FONT10=new Font("dialog",Font.PLAIN,10);
public static Font FONT12=new Font("dialog",Font.PLAIN,12);
public static Font FONT12B=new Font("dialog",Font.BOLD,12);
public static Font FONT14=new Font("dialog",Font.PLAIN,14);
public static Font FONT18=new Font("dialog",Font.PLAIN,18);
public boolean SHOWALLCHANNELS=false;
public String REQUESTFILTER=Connection.StandardChannels;
//EMF 9/30/99: changed STREAMING default to false
public boolean STREAMING=false;
//EMF 10/27/00: added SLAVEMODE flag for using rbnbPlot with rbnbPlayer
public boolean SLAVEMODE=false;
//EMF 3/20/01: added FOURBYTEASINTEGER mode for forcing interpretation of 4 byte values as integers
public boolean FOURBYTEASINTEGER=false;
//EMF 3/27/02: added SCROLLGRIDLINES so PNGPlugIn won't use them by default
//             note PlotsContainer sets it to true; could add GUI control later
public boolean SCROLLGRIDLINES=false;
//EMF 10/10/02: added CHANSPERDG so user can control how many channels are
//              put into each display group when SHOWALLCHANNELS is set
public int CHANSPERDG=4;

//EMF 11/8/00: now need a constructor
//EMF 4/10/07: add custom font sizes via java debug flag
public Environment() {
  String fontsize=System.getProperty("plot.fontsize","10");
  if (!fontsize.equals("10")) {
    try {
      int size=Integer.parseInt(fontsize);
      FONT10=new Font("dialog",Font.PLAIN,size);
      FONT12=new Font("dialog",Font.PLAIN,size+2);
      FONT12B=new Font("dialog",Font.BOLD,size+2);
      FONT14=new Font("dialog",Font.PLAIN,size+4);
      FONT18=new Font("dialog",Font.PLAIN,size+8);
    } catch (Exception e) {
      System.err.println("Exception setting custom font size "+fontsize);
    }
  }
}

public void showState() {
   System.err.println("RBNB HOST:PORT "+HOST+":"+PORT);
   System.err.println("STATICMODE "+STATICMODE);
   System.err.println("KILLRBNB "+KILLRBNB);
   System.err.println("DISPLAYGROUPS "+DISPLAYGROUPS);
   System.err.println("POSITION_X "+POSITION_X);
   System.err.println("POSITION_Y "+POSITION_Y);
   System.err.println("SCALE_AUTO "+SCALE_AUTO);
   System.err.println("SCALE_DECREASE "+SCALE_DECREASE);
   System.err.println("SCALE_DIV "+SCALE_DIV);
   System.err.println("SCALE_MIN "+SCALE_MIN);
   System.err.println("SCALE_MAX "+SCALE_MAX);
   System.err.println("DURATION "+DURATION);
   System.err.println("MAXDURATION "+MAXDURATION);
   System.err.println("TIME_LABEL "+TIME_LABEL);
   System.err.println("BGCOLOR "+BGCOLOR);
   System.err.println("VERSION "+VERSION);
   System.err.println("FONT10 "+FONT10);
   System.err.println("FONT12 "+FONT12);
   System.err.println("FONT12B "+FONT12B);
   System.err.println("FONT14 "+FONT14);
   System.err.println("FONT18 "+FONT18);
   System.err.println("SHOWALLCHANNELS "+SHOWALLCHANNELS);
   System.err.println("CHANSPERDG "+CHANSPERDG);
   System.err.println("REQUESTFILTER "+REQUESTFILTER);
   System.err.println("STREAMING "+STREAMING);
   System.err.println("SLAVEMODE "+SLAVEMODE);
   System.err.println("FOURBYTEASINTEGER "+FOURBYTEASINTEGER);
   }
}





