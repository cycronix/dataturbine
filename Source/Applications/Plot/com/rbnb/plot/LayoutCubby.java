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
  ***   Name : LayoutCubby     ()                               ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : January 1997                                     ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : cubbyhole for passing layout state        ***
  ***                 between graphics and RBNB threads         ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***								***
  ***	Modification History :					***
  ***	04/19/2000	JPW	Added ExportToDT		***
  ***   04/22/2000      JPW     Changed Export to ExportToCB    ***
  ***                                                           ***
  *****************************************************************
*/

package com.rbnb.plot;

import java.util.Vector;

public class LayoutCubby {
   private Vector layout = new Vector();
   public static final int LoadConfig=1;
   public static final int SaveConfig=2;
   public static final int OpenRBNB=3;
   public static final int RefreshRBNB=4;
   public static final int CloseRBNB=5;
   public static final int PlotMode=6;
   public static final int TableMode=7;
   //EMF 9/8/99: added export to clipboard (eg - for paste into spreadsheet)
   // JPW 4/22/2000: Changed from "Export" to "ExportToCB"
   public static final int ExportToCB=8;
   // JPW 4/19/2000: Added "Export To DataTurbine" feature (export a
   //                data slice back to the DataTurbine as one
   //                or more new channels.
   public static final int ExportToDT=9;
   private Boolean status = null;

//get method - if has not been read, return new layout, else returns null
public synchronized Integer get() {
   if (layout.size()>0) {
	Integer value = (Integer)layout.elementAt(0);
	layout.removeElementAt(0);
      return value;
      }
   else return null;
   }

//set method - sets run mode to specified mode if legit, to stop 
//otherwise, and sets boolean indicating new mode available
public synchronized void set(int lo) {
   layout.addElement(new Integer(lo));
   }

//getStatus method - returns Boolean of status
public synchronized Boolean getStatus() {
	Boolean returnVal=status;
	status=null;
	return returnVal;
	}

//setStatus method - sets boolean indicating status
public synchronized void setStatus(boolean stat) {
	status=new Boolean(stat);
	}
} //end class LayoutCubby
