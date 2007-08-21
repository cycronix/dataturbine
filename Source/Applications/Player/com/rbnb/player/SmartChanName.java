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
  *********************************************************************
  ***								    ***
  ***	Name :	SmartChanName.java			            ***
  ***	By   :	U. C. Bersgtrom    (Creare Inc., Hanover, NH)       ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                			    ***
  ***								    ***
  ***	Copyright 2002, 2004 Creare Inc.       			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class is a utility for the Player.ChanList class.   ***
  ***   It is used to maintain information on each of the channel   ***
  ***   names provided by ChanList.                                 ***
  ***								    ***
  ***	Modification History					    ***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player	    ***
  ***								    ***
  *********************************************************************
*/

package com.rbnb.player;

import java.util.Vector;

public class SmartChanName implements Comparable {

    public int index = -1;
    public String fullName = null;
    public String endName = null;
    public boolean isSelected = false;

    private Vector dupes = new Vector();

    // Constructor
    public SmartChanName(int indexI, String fullNameI) {
	index = indexI;
	fullName = fullNameI;

	int idx = fullName.lastIndexOf("/");
	if (idx == -1) {
	    endName = fullName;
	} else {
	    endName = fullName.substring(idx + 1);
	}
    }

    // Comparable interface
    public int compareTo(Object obj) {
	SmartChanName scn = (SmartChanName) obj;
	return (index - scn.index);
    }

    // implement equals
    public boolean equals(SmartChanName scn) {
	if (index == scn.index) {
	    return true;
	}
	return false;
    }

    // setDupeVector
    public void setDupeVector(Vector dupesI) {
	dupes = new Vector();
	Integer myIndex = new Integer(index);
	for (int i = 0; i < dupesI.size(); ++i) {
	    if (!myIndex.equals((Integer) dupesI.elementAt(i))) {
		dupes.addElement(dupesI.elementAt(i));
	    }
	}
    }

    // getDupeVector
    public Vector getDupeVector() {
	return dupes;
    }

}

