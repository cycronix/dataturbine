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
  ***								***
  ***	Name :	ChanInfo                                 	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class stores a byte array and two doubles: the  ***
  ***   data, start and duration times describing a Channel.    ***
  ***								***
  ***	Modification History:					***
  ***	07/23/04 JPW - Brought this class into the new		***
  ***		       RBNB V2.4 compliant Capture package.	***
  ***	05/04/05 JPW - No longer store duration; duration will	***
  ***		       always be set to 0.0			***
  ***								***
  *****************************************************************
*/
package com.rbnb.capture;

public class ChanInfo {

    // NOTE: make sure that this byte array "belongs" to ChanInfo,
    // and will not be overwritten by code in a client class!
    public byte[] data = null;
    // JPW 05/06/2005: Initialize start time to 0
    public long start = 0;

    public ChanInfo() {}

    public ChanInfo(byte[] dataI, long startI) {
	data = dataI;
	start = startI;
    }
}
