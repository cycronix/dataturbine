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
  ***	Name :	RBNBPlayerOutput		              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   RBNBPlayerOutput is a subclass of PlayerOutput, and  ***
  ***   used to output Maps to a DataTurbine.                   ***
  ***								***
  ***	07/23/01 - UCB: Altered constructor such that the first ***
  ***                   Player created will be named rbnbPlayer,***
  ***                   and the later Players will be named     ***
  ***                   rbnbPlayer.<unique number>.             ***
  ***	10/07/04 - JPW: Upgrade to RBNB V2 Player		***
  ***	10/13/04 - JPW:	To allow for backward going times in	***
  ***			Server, hardwire cacheSize = 1.  When	***
  ***			there is no archive and when cache size	***
  ***			equals 1, the RBNB allows backward	***
  ***			going times.				***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.utility.Utility;

import java.util.Date;

class RBNBPlayerOutput extends PlayerOutput {
    
    private Source source = null;
    
    private int cacheSize = 1000;  // was 1 mjm 2/16/05
    
    // private double lastStart = Double.MAX_VALUE;
    private double currEarliestStart;
    private double currLatestStart;
    
    private boolean useShortNames = true;
    
    // JPW 10/12/2004: For assigning times to data sent to RBNB.
    private int timeIndex = 0;

/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBPlayerOutput		              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires a hostport sting.             ***
  ***								***
  ***	07/23/01 - UCB: Altered constructor such that the first ***
  ***                   Player created will be named rbnbPlayer,***
  ***                   and the later Players will be named     ***
  ***                   rbnbPlayer.<unique number>.             ***
  ***	10/11/04 - JPW: Upgrade to RBNB V2			***
  ***	10/13/04 - JPW:	To allow for backward going times in	***
  ***			Server, hardwire cacheSize = 1.  When	***
  ***			there is no archive and when cache size	***
  ***			equals 1, the RBNB allows backward	***
  ***			going times.				***
  ***								***
  *****************************************************************
*/
    public RBNBPlayerOutput(String hostport, int cacheSizeI) throws Exception {
	
	// JPW 10/13/2004: Hardwire the cache size to 1 in order to allow for
	//		   backward going times.  Backward going times only
	//		   work in the RBNB Server when there is no archive and
	//		   when cache size = 1.
	cacheSize = cacheSizeI;
	if(cacheSize < 100) cacheSize = 1000;  // foo
	//	cacheSize = 1;  // mjm nope, allow cacheSize>1 for re-timestamping game
	// JPW 11/07/2005: Kludge for now to give Player a large output cache
	cacheSize = 200000;
	System.err.println("cacheSize: "+cacheSize);

	try {
	    source = new Source(cacheSize, "none", 0);
	    source.OpenRBNBConnection(hostport, name);
	} catch (Exception e) {
	    if (source != null) {
	        disconnect();
	    }
	    throw e;
	}
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect         			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Disconnects source from DataTurbine.                 ***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    public void disconnect() {
	if (source != null) {
	    source.CloseRBNBConnection();
	    source = null;
	}
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	setOutputChannels               		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :  May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Registers output channels.                           ***
  ***								***
  ***	Modification History					***
  ***	10/11/04	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    public void setOutputChannels(
	ChannelMap outMap,
	boolean useShortNamesI)
    throws Exception
    {
	useShortNames = useShortNamesI;
	// JPW 10/11/2004: renameChans() doesn't work anymore
	// renameChans(outMap);
	source.Register(outMap);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	outputData     			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method outputs a map.                           ***
  ***								***
  ***	UCB 05/25/01 - moved this method from PlayerEngine to   ***
  ***                  RBNBPlayerOutput.                        ***
  ***   UCB 03/18/02 - If the cacheSize is greater than one,    ***
  ***                  NBO is type 0 and cannot accept start-   ***
  ***                  times that run in reverse.  Check for    ***
  ***                  these are call deleteArchive() as needed.***
  ***								***
  *****************************************************************
*/
    public void outputData(ChannelMap outMap) throws Exception {
	
	// This method should not be called with outputMap == null or with an
	// outputMap with no data!  Use removeNullFrames() to prep outputMap
	// check outputMap for null/no data before calling outputData()
	
	// JPW 10/11/2004: renameChans() doesn't work anymore
	// renameChans(outMap);
	
	// JPW 10/13/2004: No longer create a new output map; with the
	//                 source's cache size hardwired to 1 we can just
	//                 pass outMap directly out to the Source.
	// ChannelMap newOutMap = createNewOutMap(outMap);
	// if ( (newOutMap == null) || (newOutMap.NumberOfChannels() == 0) ) {
	//     return;
	// }
	
	try {
	    // JPW 10/13/2004: No longer create a new output map; with the
	    //                 source's cache size hardwired to 1 we can just
	    //                 pass outMap directly out to the Source.
	    // source.Flush(newOutMap);
	    
	    //       	    source.Flush(outMap);  // mjm
	    
	    // JPW 02/07/2005: Handle multiple channels in the output map
	    
	    // MJM 2/10/05:  following are iterations to fix multi-channel play
	    if(true) {
		
		// JPW 11/11/2005: Pull this line out of the channel loop;
		//                 this way, all channels will have the same
		//                 timestamp
		double tstamp = (double)System.currentTimeMillis()/1000.;
		
		for (int i=0; i < outMap.NumberOfChannels(); ++i) {
		    
		    ChannelMap cm = new ChannelMap();
		    int chanIdx = cm.Add(outMap.GetName(i));
		    // NOTE: rbnbPlayer only outputs does zero duration
		    double   duration = 0.;
		    
		    // JPW 11/11/2005: Pull timestamp out of the channel loop;
		    //                 see above
		    // pull timestamps out of PutData loop - mjm 2/1605
		    // double tstamp = (double)System.currentTimeMillis()/1000.;
		    
		    cm.PutTime(tstamp,duration);
		    
		    // Old solution #1
		    // cm.PutTime((double)System.currentTimeMillis()/1000.,0.);
		    // cm.PutData(chanIdx, outMap.GetData(i), outMap.GetType(i));
		    
		    // Old solution #2
       	       	    // cm.PutTimeRef(outMap, i);
      		    // cm.PutDataRef(chanIdx, outMap, i);
		    // source.Flush(cm,true);  // sync helps?
		    
		    // chop multi-point frames into parts
		    double[] ta = outMap.GetTimes(i);
		    
		    switch(outMap.GetType(i)) {
			case ChannelMap.TYPE_STRING:
			    String[] das = outMap.GetDataAsString(i);
			    for(int ii=0; ii<ta.length; ii++) {
				cm.PutDataAsString(chanIdx, das[ii]);
			    }
			    break;
			case ChannelMap.TYPE_FLOAT64:
			    // JPW 11/07/2005: Added support for doubles
			    double[] dad = outMap.GetDataAsFloat64(i);
			    // Put entire data array into the channel map all
			    // at once (not point-by-point)
			    cm.PutDataAsFloat64(chanIdx, dad);
			    break;
			case ChannelMap.TYPE_FLOAT32:
			    float[] daf = outMap.GetDataAsFloat32(i);
			    // Put entire data array into the channel map all
			    // at once (not point-by-point)
			    cm.PutDataAsFloat32(chanIdx, daf);
			    break;
			default:
			    byte[][] dab = outMap.GetDataAsByteArray(i);
			    for(int ii=0; ii<ta.length; ii++) {
				cm.PutDataAsByteArray(chanIdx, dab[ii]);
			    }
			    break;
		    }
		    source.Flush(cm,true);
		}
		
	    }
	    
	} catch (Exception e) {
	    System.err.println(
	        "Exception encountered outputting to DataTurbine.");
	    throw e;
	}
	// Don't clear the map; Player is not a stateless system, so
	// the time info in this map is needed for future Requests
	// outMap.Clear();  // mjm
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	renameChans                        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method removes the server and datapath from     ***
  ***   the channel names (cannot output channels with          ***
  ***   servers or datapaths in their names).                   ***
  ***								***
  ***	JPW 10/12/04 - Disable the guts of this method for now;	***
  ***		       there is no way under RBNB V2 SAPI to	***
  ***		       individually rename channels in the	***
  ***		       ChannelMap.				***
  ***								***
  ***	UCB 06/15/01 - method used to add "_prime" to the       ***
  ***                  channel names.  No longer does so.       ***
  ***								***
  ***	UCB 05/25/01 - moved this method from PlayerEngine to   ***
  ***                  RBNBPlayerOutput.                        ***
  ***								***
  *****************************************************************
*/
    /*
    private void renameChans(ChannelMap inMap) {
	String[] chanNames = inMap.GetChannelList();
	
	String newName;
	
	for (int i = 0; i < inMap.NumberOfChannels(); ++i) {
	    String[] parts = Utility.unpack(chanNames[i], "/");
	System.err.println("renameChans(): Parts of the chan name:");
	for (int j = 0; j < parts.length; ++j) {
	    System.err.println("\t" + parts[j]);
	}
	    if (useShortNames || parts.length != 3) {
		newName = parts[2];
	    } else {
		newName = parts[2] + "." + parts[1];
	    }
	    
	    THERE IS NO EQUIVALENT CALL UNDER RBNB V2 - CAN'T SIMPLY
	    CHANGE THE NAME OF AN INDIVIDUAL CHANNEL IN A CHANNEL MAP
	    chans[i].setName(newName);
	}
    }
    */

/*
  *****************************************************************
  ***								***
  ***	Name :	getCurrTimes()    			        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2002					***
  ***								***
  ***	Copyright 2002, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the earliest and latest start  ***
  ***   Times from the input map, and stores them in            ***
  ***   currEarliestStart and currLatestStart.                  ***
  ***         Note: do not use with a null map!                 ***
  ***								***
  ***	Modification History					***
  ***	10/11/2004	JPW	Upgrade to RBNB V2		***
  ***								***
  *****************************************************************
*/
    private void getCurrTimes(ChannelMap map) {
	
	double startEarlT = Double.MAX_VALUE;
	double startLateT = Double.MIN_VALUE;
	
	for (int i = 0; i < map.NumberOfChannels(); ++i) {
	    double tempT = map.GetTimeStart(i);
	    if (tempT < startEarlT) {
		startEarlT = tempT;
	    }
	    if (tempT > startLateT) {
		startLateT = tempT;
	    }
	}
	
	currEarliestStart = startEarlT;
	currLatestStart = startLateT;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createNewOutMap()    			        ***
  ***	By   :	John Wilson (Creare Inc., Hanover, NH)		***
  ***	For  :	DataTurbine					***
  ***	Date :	October 2004					***
  ***								***
  ***	Copyright 2004 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Create a new ChannelMap from the given input map.	***
  ***	The channel names will be the same as in the input map,	***
  ***	and a data ref to the data in the input map will be set	***
  ***	in the output map.  However, times will be reset such	***
  ***	that time always moves forward.				***
  ***								***
  ***	Modification History					***
  ***	10/12/2004	JPW	Created				***
  ***								***
  *****************************************************************
*/
    private ChannelMap createNewOutMap(ChannelMap mapI) throws Exception {
	
	if ( (mapI == null) || (mapI.NumberOfChannels() == 0) ) {
	    throw new Exception("Empty ChannelMap to send to Source");
	}
	
	ChannelMap newMap = new ChannelMap();
	
	String[] chanNames = mapI.GetChannelList();
	for (int i = 0; i < chanNames.length; ++i) {
	    // First, check to see if this channel has any data
	    byte[] tempData = mapI.GetData(i);
	    if ( (tempData == null) || (tempData.length == 0) ) {
		continue;
	    }
	    int chanIdx = newMap.Add(chanNames[i]);
	    newMap.PutTimeRef(mapI, i);
	    newMap.PutDataRef(chanIdx, mapI, i);
	}
	
	return newMap;
	
    }

}

