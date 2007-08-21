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
  ***   Name : RunModeCubby    ()                               ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : December 1997                                    ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : cubbyhole for passing run mode state      ***
  ***                 between graphics and RBNB threads         ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***                                                           ***
  *****************************************************************
*/

package com.rbnb.plot;

public class RunModeCubby {
   private int runMode = RunModeDefs.stop;
   private boolean newMode = true;
   private boolean setByGUI = true;
   private boolean streaming = false;
   private boolean newStreaming = false;

// constructor - checks RBNBPlot mode and sets runMode accordingly
public RunModeCubby(boolean staticMode) {
   if (staticMode) runMode=RunModeDefs.current;
   else runMode=RunModeDefs.realTime;
	}
   
//get method - returns current run mode, and boolean indicating if
//mode has been reset since last read   
public synchronized Integer get(boolean reqByGUI) {
   if (!newMode) return null;
   if (reqByGUI==setByGUI) return null;
   newMode=false;
   return new Integer(runMode);
   }

//set method - sets run mode to specified mode if legit, to stop 
//otherwise, and sets boolean indicating new mode available
public synchronized void set(int mode,boolean sbg) {
   // System.err.println("RunModeCubby.set mode="+mode+"   sbg="+sbg);
   if (mode==RunModeDefs.bof) runMode=mode;
   else if (mode==RunModeDefs.revPlay) runMode=mode;
   else if (mode==RunModeDefs.revStep) runMode=mode;
   else if (mode==RunModeDefs.stop) runMode=mode;
   else if (mode==RunModeDefs.fwdStep) runMode=mode;
   else if (mode==RunModeDefs.fwdPlay) runMode=mode;
   else if (mode==RunModeDefs.eof) runMode=mode;
   else if (mode==RunModeDefs.realTime) runMode=mode;
   else if (mode==RunModeDefs.quit) runMode=mode;
   else if (mode==RunModeDefs.current) runMode=mode;
   else runMode=RunModeDefs.stop;
   newMode=true;
   setByGUI=sbg;
   }

//setStreaming method - sets Boolean indicating whether rbnb connection is streaming
public synchronized void setStreaming(boolean s) {
	newStreaming=true;
	streaming=s;
	}

public synchronized Boolean getStreaming() {
	if (newStreaming) {
		newStreaming=false;
		return new Boolean(streaming);
		}
	else return null;
	}

} //end class RunModeCubby
