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
  ***	Name :	rbnbPlayer			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Entry point for RBNBPlayer-- visual playback control ***
  ***   for DataTurbine data.                                   ***
  ***								***
  ***	Modification History :					***
  ***    01/19/01 - UCB:  Added command line argument -r        ***
  ***	 05/01/01 - UCB:  Added boolean autoRun to              ***
  ***                     call to RequestHandler constructor.   ***
  ***								***
  ***	Syntax :						***
  ***	    java rbnbPlayer \					***
  ***		[-r [<RBNB host>][:<RBNB server port>]] \	***
  ***								***
  ***	Modification History					***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player.	***
  ***								***
  *****************************************************************
*/

import com.rbnb.player.RequestHandler;
import com.rbnb.utility.SettingsReader;

public class rbnbPlayer {
    
    public static void main(String[] args) {
	int		idx,
	                idx1,
	                idx2;
	int switchPos;
	String inputServer = null;
	String outputServer = null;
	boolean autoOpen = false;
	boolean useSettingsFile = false;
	
	for (idx = 0; idx < args.length; ++idx) {
	    // Command line options *should* begin with a '-'.
	    if (args[idx].charAt(0) != '-') {
		switchPos = 0;
	    } else {
		switchPos = 1;
	    }
	    
	    // Deal with optional space after [<dash>]<letter> before <value>
	    if (args[idx].length() <= 2){
		// There is a space between [<dash>]<letter> and <value>
		idx1 = idx + 1;
		idx2 = 0;
	    } else {
		// There isn't a space between <dash><letter> and <value>
		idx1 = idx;
		idx2 = 2;
	    }
	    
	    switch (args[idx].charAt(switchPos)) {
		
		// -r [rbnbhost][:rbnbport]
	    case 'r':
		inputServer = args[idx + 1].substring(idx2);
		autoOpen = true;
		idx = idx1;
		break;
		
	    case 'w':
		outputServer = args[idx + 1].substring(idx2);
		idx = idx1;
		break;

	    case 'f':
		if (SettingsReader.settingsExist(RequestHandler.SETTINGS_FILE))
		{
		    useSettingsFile = true;
		} else {
		    System.err.println(
		        "Unable to find file " + RequestHandler.SETTINGS_FILE);
		    System.err.println(
		        "Proceeding without the settings file.");
		}
		break;

	    default:
		System.err.println("Unrecognized switch: " + args[idx]);
		System.err.println("");
		System.err.println("Usage:");
		System.err.println(
		    "-r [<RBNB host>][:<RBNB port>] : " +
		    "DataTurbine for read connection");
		System.err.println(
		    "-w [<RBNB host>][:<RBNB port>] : " +
		    "DataTurbine for write connection");
		System.err.println(
		    "                                 " +
		    "(defaults to read DataTurbine)");
		System.err.println(
		    "-f  (use settings from " +
		    RequestHandler.SETTINGS_FILE +
		    ")");
		break;
	    }
	}
	
	if (inputServer == null) {
	    inputServer = "localhost:3333";
	}
	if (outputServer == null) {
	    outputServer = inputServer;
	}

	RequestHandler rh =
	    new RequestHandler(
	        inputServer, outputServer, autoOpen, useSettingsFile);
    }
    
}
