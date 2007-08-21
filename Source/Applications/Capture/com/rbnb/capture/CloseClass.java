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


package com.rbnb.capture;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
  *****************************************************************
  ***								***
  ***	Name :	CloseClass	(Close down the frame)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	January, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class handles window closing events on the	***
  ***	frame used for the display.				***
  ***								***
  ***	Modification History:					***
  ***	07/23/04 JPW - Brought this class into the new		***
  ***		       RBNB V2.4 compliant Capture package.	***
  ***								***
  *****************************************************************
*/

class CloseClass extends WindowAdapter {

  // 07/23/2004  JPW  Not currently supporting Caster
  // private Caster		caster = null;
  
  private Capture		capture = null;

/*
  *****************************************************************
  ***								***
  ***	Name :	CloseClass	(Constructor: from caster)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	January, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a CloseClass object from the	***
  ***	input Caster object.					***
  ***								***
  ***	Input :							***
  ***	   casterI		The caster object.		***
  ***								***
  *****************************************************************
*/
/*

  07/23/2004  JPW  Not currently supporting Caster

  CloseClass(Caster casterI) {
    caster = casterI;
  }
*/

/*
  *****************************************************************
  ***								***
  ***	Name :	CloseClass	(Constructor: from capture)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	January, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a CloseClass object from the	***
  ***	input Capture object.					***
  ***								***
  ***	Input :							***
  ***	   captureI		The capture object.		***
  ***								***
  *****************************************************************
*/
  CloseClass(Capture captureI) {
    capture = captureI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowClosing	(Close down frame)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	January, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method closes down the application.		***
  ***								***
  ***	Input :							***
  ***	   eventI		The window closing event.	***
  ***								***
  *****************************************************************
*/
  public void windowClosing(WindowEvent eventI) {
    
    // 07/23/2004  JPW  Not currently supporting Caster
    /*
    if (caster != null) {
	caster.exitAction();
    }
    */
    
    if (capture != null) {
      capture.exitAction();
    }
    
  }
}
