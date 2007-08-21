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
  ***	Name :	rbnbCapture	(Capture from UDP/multicast)	***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998 - January, 1999			***
  ***								***
  ***	Copyright 1998, 1999, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class captures data from a UDP or multicast	***
  ***	socket and sends it to the RBNB.			***
  ***								***
  ***	Modification History:					***
  ***	07/23/2004	JPW	Use the new package name:	***
  ***				com.rbnb.capture.Capture	***
  ***								***
  *****************************************************************
*/
import com.rbnb.capture.Capture;

public class rbnbCapture {
/*
  *****************************************************************
  ***								***
  ***	Name :	main		(Application main)		***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	December, 1998					***
  ***								***
  ***	Copyright 1998 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This static routine provides the main program for	***
  ***	the rbnbCapture application. It accepts the command-	***
  ***	line arguments and creates an rbnbCapture object.	***
  ***								***
  ***	Input :							***
  ***	   argsI		The command line arguments.	***
  ***								***
  *****************************************************************
*/
  public final static void main(String argsI[]) {
    Capture.main(argsI);
  }
}
