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
  ***	Name :	SmartNameComparator.java			    ***
  ***	By   :	U. C. Bersgtrom    (Creare Inc., Hanover, NH)       ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                			    ***
  ***								    ***
  ***	Copyright 2002, 2004 Creare Inc.       			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class is a utility for the Player.ChanList class.   ***
  ***   It is used to sort the SmartChanName objects by name.       ***
  ***								    ***
  ***	Modification History					    ***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player	    ***
  ***								    ***
  *********************************************************************
*/

package com.rbnb.player;

import java.util.Comparator;

public class SmartNameComparator implements Comparator {

    // constructor
    public SmartNameComparator() {}

    // Comparator interface: compare()
    public int compare(Object obj1, Object obj2) {
	SmartChanName scn1 = (SmartChanName) obj1;
	SmartChanName scn2 = (SmartChanName) obj2;

	return scn1.endName.compareTo(scn2.endName);
    }

}

