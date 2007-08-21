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

import com.rbnb.sapi.*;

/**
 * Regression test to check out monitor and subscribe modes.
 * <p>
 * This class tests monitor and subscribe modes using a Put/Fetch loop. One
 * frame is sent to the server and then the code attempts to fetch that frame
 * back again.
 * <p>
 * In subscribe mode, each frame sent out should come back immediately.
 * <p>
 * In monitor mode, we may miss frames due to timing issues. Since monitor mode
 * allows for missed frames, this is OK, but we need to account for it. What
 * the code does is use a short non-blocking read loop. If no frame can be
 * retrieved within a certain amount of time, the code notes that fact and
 * returns to the Put call. When a frame is finally retrieved, the code allows
 * for it to be any frame between the expected one and the first one missed
 * since the code last retrieved one.
 * <p>
 * Syntax: java MonitorSubscribe [<server address>] [M or S] [<# repetitions>]
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/06/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/21/2001  INB	Created.
 *
 */
public class MonitorSubscribe {
    public static void main(String[] argsI) {
	try {
	    int nPts = 10,
	        repetitions = 1;
	    String address = "localhost",
		   mode = "S";

	    for (int aIdx = 0; aIdx < argsI.length; ++aIdx) {
		if (argsI[aIdx].length() == 1) {
		    mode = argsI[aIdx];
		} else {
		    int idx;
		    for (idx = 0; idx < argsI[aIdx].length(); ++idx) {
			if (!Character.isDigit(argsI[aIdx].charAt(idx))) {
			    break;
			}
		    }
		    if (idx < argsI[aIdx].length()) {
			address = argsI[aIdx];
		    } else {
			repetitions = Integer.parseInt(argsI[aIdx]);
		    }
		}
	    }


	    for (int count = 0; count < repetitions; ++count) {
		Sink snk = new Sink();
		try {
		    snk.OpenRBNBConnection(address,"mySink");
		} catch (com.rbnb.sapi.SAPIException e) {
		    continue;
		}

		ChannelMap map = new ChannelMap();
		map.Add("mySource/C0");
		if (mode.equalsIgnoreCase("S")) {
		    snk.Subscribe(map);
		} else {
		    snk.Monitor(map,1);
		}

		// Give the sink a moment to actually get settled in.
		Thread.sleep(100);

		Source src = new Source(10,"none",0);
		try {
		    src.OpenRBNBConnection(address,"mySource");
		} catch (com.rbnb.sapi.SAPIException e) {
		    snk.CloseRBNBConnection();
		    continue;
		}

		ChannelMap sMap = new ChannelMap(),
		    rMap = null;
		sMap.Add("C0");
		int offBy = 0,
		    last = -1;
		for (int idx = 0; idx < nPts; ++idx) {
		    System.err.println("Frame: " + idx);
		    double[] data = { idx*1. };
		    sMap.PutTime(idx,1.);
		    sMap.PutDataAsFloat64(0,data);
		    src.Flush(sMap,true);

		    int nChan = 0;
		    if (mode.equalsIgnoreCase("S")) {
			rMap = snk.Fetch(-1);
		    } else {
			for (int tries = 0;
			     (nChan == 0) && (tries < 5);
			     ++tries) {
			    rMap = snk.Fetch(0);
			    if ((nChan = rMap.NumberOfChannels()) == 0) {
				Thread.sleep(25);
			    }
			}
			if (nChan == 0) {
			    ++offBy;
			}
		    }
		    for (int cIdx = 0; cIdx < nChan; ++cIdx) {
			double[] time = rMap.GetTimes(cIdx);
			data = rMap.GetDataAsFloat64(cIdx);
		    
			if (time.length != 1) {
			    throw new Exception
				("Only got " + time.length + " points.");
			}
			for (int tIdx = 0; tIdx < time.length; ++tIdx) {
			    System.err.println(rMap.GetName(cIdx) +
					       " got " + time[tIdx] +
					       ", " + data[tIdx]);
			    if ((time[tIdx] < idx - offBy) ||
				(time[tIdx] > idx)) {
				throw new Exception(time[tIdx] + " vs. " + idx);
			    } else if ((data[tIdx] < idx - offBy) ||
				       (data[tIdx] > idx)) {
				throw new Exception(data[tIdx] + " vs. " + idx);
			    }
			    if (time[tIdx] != idx) {
				System.err.println
				    ("Off by: " + (idx - time[tIdx]) +
				     " " + time[tIdx] + " vs. " + idx);
			    }
			    offBy = (idx - (int) time[tIdx]);
			    if ((int) time[tIdx] <= last) {
				throw new Exception("Duplicate data!");
			    }
			    last = idx - offBy;
			}
		    }
		}
	    

		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
