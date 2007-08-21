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
  ***	Name :	ControlBoxListener			        ***
  ***	By   :	UCB/INB  	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***      To respond to user actions in the ControlBox, the    ***
  ***   developer must implement the ControlBoxListener         ***
  ***   interface.                                              ***
  ***								***
  ***	Modification History					***
  ***	10/07/2004	JPW	Brought over from		***
  ***				COM.Creare.RBNB.Widgets to be	***
  ***				in the new RBNB V2 Player.	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

public interface ControlBoxListener {
    
    public void limitOfData(boolean beginning, boolean trackingPosition);

    public void play(boolean forwards);

    public void step(boolean forwards);

    public void pause();

    public void realTime();

    public void positionSliderAt(int sliderPos, boolean trackingPosition);

    public void durationAt(double dur, boolean trackingDuration);

    public void rateAt(double rate, boolean trackingRate);

    // UCB 11/02/01 - changed name of stepAt() to
    // incrementAt(), to be consistent.
    public void incrementAt(double value, boolean trackingInc);

    // UCB 11/01/01 - superceding setTimeBaseInSec, which permitted
    // only one of two modes to be set, with setMode, which allows
    // one of more than two modes to be set.
    //public void setTimeBaseInSec(boolean tbis);
    public void setMode(int mode);
}

