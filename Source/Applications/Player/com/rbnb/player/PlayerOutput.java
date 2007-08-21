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
  ***	Name :	PlayerOutput			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   PlayerOutput is an abstract base class whose         ***
  ***   subclasses are used by PlayerEngine to output frames of ***
  ***   data to a desired sink (DataTurbine, UDP port, etc.)    ***
  ***								***
  ***	Modification History					***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

// JPW 10/07/2004: Upgrade to RBNB V2
// import COM.Creare.RBNB.API.Map;
import com.rbnb.sapi.ChannelMap;

abstract class PlayerOutput {

    protected String name = "rbnbPlayer";

    public abstract void disconnect();
    
    public abstract void setOutputChannels(
        ChannelMap outMap, boolean useShortNames) throws Exception;

    // do not use on a null outputMap or an outputMap
    // with no data!  (Use removeNullFrames to prep
    // outputMap and check for an map with no data.)
    // JPW 10/07/2004: Upgrade to RBNB V2: use ChannelMap
    public abstract void outputData(ChannelMap outMap) throws Exception;

    public final String getName() {
	return name;
    }

}

