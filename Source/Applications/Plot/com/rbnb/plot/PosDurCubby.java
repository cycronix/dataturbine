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
  ***   Name : PosDurCubby      ()                              ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : March 1998                                       ***
  ***                                                           ***
  ***   Copyright 1998 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : cubbyhole for passing position (current,  ***
  ***                 min, max) and duration between graphics   ***
  ***                                  and RBNB threads         ***
  ***                                                           ***
  ***   Revisions						***
  ***      10/25/00 EMF: added cubby indicating whether current ***
  ***                    position label is start or end time,   ***
  ***                    which allows changes in duration to    ***
  ***                    leave position label alone by changing ***
  ***                    start time when appropriate            ***
  ***                                                           ***
  *****************************************************************
*/

package com.rbnb.plot;

import java.util.Date;
//EMF 5/18/01: use replacement Time
//import COM.Creare.RBNB.Time;

public class PosDurCubby {
   private Time[] position = new Time[3]; //0=current,1=min,2=max
   private Time duration;
   private int timeFormat = Time.Unspecified;
    private int precision = 3;
   private boolean newPosition=false;
   private boolean newDuration=false;
   private boolean positionByGUI=false;
   private boolean durByGUI=false;
	//private long lastUpdate=(new Date()).getTime();
	//private long firstUpdate=lastUpdate;
	//private int updateCount=0;
	//private long min=0;
	//private long max=0;
	private String updateRate = null;
  //EMF 10/25/00
  boolean positionAtStart=true;
  //EMF 4/19/05
  boolean newZoom=false;
  Time[] zoom = new Time[2];

//getUpdateRate method - returns updateRate and sets it to null
public synchronized String getUpdateRate() {
	if (updateRate==null) return null;
	String returnVal=new String(updateRate);
	updateRate=null;
	return returnVal;
	}

//setUpdateRate method - sets updateRate to specified string
public synchronized void setUpdateRate(String udr) {
	updateRate=udr;
	}
		
//getPostion method - if unread and set by other party, returns start.
// otherwise returns null
public synchronized Time[] getPosition(boolean reqByGUI) {
   if (!newPosition) return null;
   if (reqByGUI==positionByGUI) return null;
   newPosition=false;
   Time[] pos=new Time[3];
   pos[0]=new Time(position[0]);
   pos[1]=new Time(position[1]);
   pos[2]=new Time(position[2]);
   return pos;
   }

//setPosition method - sets start and startByGUI
public synchronized void setPosition(Time[] pos,boolean setByGUI) {
   //if set by GUI and not yet read, ignore new position if being set by rbnb
   if (newPosition && positionByGUI && !setByGUI) return;
   if (pos.length!=3) {
      System.err.println("PosDurCubby: position array length not 3!");
      return;
      }
   position[0]=new Time(pos[0]);
   position[1]=new Time(pos[1]);
   position[2]=new Time(pos[2]);
   newPosition=true;
   if (setByGUI) positionByGUI=true;
   else positionByGUI=false;
   }
   
//getDuration method - if unread and set by other party, returns duration.
// otherwise returns null
public synchronized Time getDuration(boolean reqByGUI) {
//System.err.println("PosDurCubby.getDuration: "+reqByGUI);
   if (!newDuration) return null;
   if (reqByGUI==durByGUI) return null;
   newDuration=false;
   return (new Time(duration));
   }

//setDuration method - sets duration and durByGUI
public synchronized void setDuration(Time dur,boolean setByGUI) {
//System.err.println("PosDurCubby.setDuration: "+dur.getDoubleValue()+", "+setByGUI);
   // EMF 10/7/99: timing bug if time format and duration change together
   //ignore if pending duration set by RBNBInterface
   if (newDuration && !durByGUI) return;
   // end 10/7/99 change
   duration=new Time(dur);
   newDuration=true;
   if (setByGUI) durByGUI=true;
   else durByGUI=false;
   }

//setTimeFormat method - sets timeFormat
public synchronized void setTimeFormat(int tf) {
   timeFormat=tf;
   }

//getTimeFormat method - return timeFormat
public synchronized int getTimeFormat() {
   return timeFormat;
   }

//setPrecision method - set precision for printing position/start time strings
public synchronized void setPrecision(int p) {
	precision=p;
	}

//getPrecision method - gets precision
public synchronized int getPrecision() {
	return precision;
	}

//EMF 10/25/00
//setPositionAtStart method - sets indication of position label anchor
public synchronized void setPositionAtStart(boolean pas) {
  positionAtStart=pas;
}

//EMF 10/25/00
//getPositionAtStart method - returns indication of position label anchor
public synchronized boolean getPositionAtStart() {
  return positionAtStart;
}

//EMF 4/19/05: support for zooming by dragging in plotarea
public synchronized void setZoom(double start, double duration) {
	zoom[0]=new Time(start);
	zoom[1]=new Time(duration);
	newZoom=true;
}

public synchronized Time[] getZoom() {
	if (newZoom) {
		newZoom=false;
		return zoom;
	} else {
		return null;
	}
}

} //end class StartDurCubby

