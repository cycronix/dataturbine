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
  ***   Name : RBNBCubby       ()                               ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : January 1998                                     ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : cubbyhole for passing RBNB channels       ***
  ***                 between graphics and RBNB threads         ***
  ***                 and for signalling request for new list   ***
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

// arrays are passed by reference, so copies are made on input/output to
// prevent corruption
public class RBNBCubby {
   private RegChannel[] availableChannels = null;
   private RegChannel[] selectedChannels = null;
   private boolean newAvailable = false;
   private boolean newSelected = false;
   private boolean selectedByGUI = false;
   private boolean wantNewAvailable = false;
   private int group=0;
   private boolean newGroup=false;

//requestAC method - signals that a new channel list is wanted
public synchronized void requestAC() {
   wantNewAvailable = true;
   }

//acknowledgeRequestAC method - signals that request is being processed
public synchronized boolean acknowledgeRequestAC() {
   if (wantNewAvailable == true) {
      wantNewAvailable = false;
      return true;
      }
   else return false;
   }

//getAvailableChannels method - returns available channels if changed since
// last get, or null otherwise
public synchronized RegChannel[] getAvailableChannels() {
   if (newAvailable == true) {
      newAvailable=false;
      RegChannel[] ac = new RegChannel[availableChannels.length];
      for (int i=0;i<availableChannels.length;i++) {
	  ac[i]=availableChannels[i].copy();
          }
      return ac;
      }
   else return null;
   }

//getSelectedChannels method - returns selected channels if changed since
// last get, or null otherwise
public synchronized RegChannel[] getSelectedChannels(boolean reqByGUI) {
   if (!newSelected) return null;
   if (reqByGUI==selectedByGUI) return null;
   newSelected=false;
   RegChannel[] sc = new RegChannel[selectedChannels.length];
   for (int i=0;i<selectedChannels.length;i++) {
       sc[i]=selectedChannels[i].copy();
       }
   return sc;
   }

//setAvailableChannels method - sets available channels
public synchronized void setAvailableChannels(RegChannel[] chans) {
   availableChannels = new RegChannel[chans.length];
   for (int i=0;i<availableChannels.length;i++) {
       availableChannels[i]=chans[i].copy();
       }
   newAvailable = true;
   }

//setSelectedChannels method - sets selected channels
//change due to group change has priority, so ignore changes until !setByGUI has
//been read
public synchronized void setSelectedChannels(RegChannel[] chans, boolean setByGUI) {
   //System.out.println("RBNBCubby.setSelectedChannels: nchans="+chans.length+" setByGUI="+setByGUI);
   if (newSelected && !selectedByGUI && setByGUI) return;
   selectedChannels = new RegChannel[chans.length];
   for (int i=0;i<selectedChannels.length;i++) {
       selectedChannels[i]=chans[i].copy();
       }
   newSelected = true;
   selectedByGUI=setByGUI;
   }

//getGroup method - returns new display group if changed 
// since last getGroup, or null otherwise
public synchronized Integer getGroup() {
   if (newGroup==true) {
	newGroup=false;
	return new Integer(group);
	}
   else return null;
   }

//setGroup method - set new display group
public synchronized void setGroup(int g) {
   group=g;
   newGroup=true;
   }
   
} //end class RBNBCubby
