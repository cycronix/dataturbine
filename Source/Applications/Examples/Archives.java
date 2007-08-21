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

import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;

/**
 * Simple <bold>RBNB</bold> example program to create, write, close, load, and
 * read archives.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/17/2002
 */
public class Archives {

    /**
     * Main method.
     * <p>
     * This method performs the following steps:
     * <p><ol>
     * <li>Creates an archive in the <bold>RBNB</bold> server,<li>
     * <li>Writes a single frame to the archive,</li>
     * <li>Closes the archive,</li>
     * <li>Loads the archive back into the server, and<li>
     * <li>Connects to the <bold>RBNB</bold> as a sink and reads the
     *     archive./li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments (ignored).
     * @since V2.0
     * @version 05/17/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2002  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {

	    /* Create a source connection to the RBNB server with a 10 frame
	     * cache and a 100 frame archive called ArchiveSource.
	     */
	    Source source = new Source(10,"Create",100);
	    source.OpenRBNBConnection("localhost:3333","ArchiveSource");

	    /* Put a single data point into the archive with a value equal to
	     * the current time of day.
	     */
	    ChannelMap cMap = new ChannelMap();
	    cMap.Add("Data");
	    long[] cData = new long[1];
	    cData[0] = System.currentTimeMillis();
	    cMap.PutTime(0.,1.);
	    cMap.PutDataAsInt64(0,cData);
	    source.Flush(cMap);

	    /* Close the archive. */
	    source.CloseRBNBConnection();

	    /* Load the archive back into the server. */
	    source = new Source(10,"Load",0);
	    source.OpenRBNBConnection("localhost:3333","ArchiveSource");

	    /* Open a sink connection to read from the archive. */
	    Sink sink = new Sink();
	    sink.OpenRBNBConnection("localhost:3333","ArchiveSink");

	    /* Read the data from the archive. */
	    ChannelMap rMap = new ChannelMap();
	    rMap.Add("ArchiveSource/Data");
	    sink.Request(rMap,0.,1.,"absolute");
	    ChannelMap aMap = sink.Fetch(-1);

	    /* Close the connections to the server. */
	    source.CloseRBNBConnection();
	    sink.CloseRBNBConnection();

	    /* Check the data to be sure that it is what we thought it was. */
	    double[] rTime = aMap.GetTimes(0);
	    long[] rData = aMap.GetDataAsInt64(0);
	    if ((rData.length != 1) || (rData[0] != cData[0])) {
		System.err.println("Failed to retrieve data.");
	    } else {
		System.err.println("Success.");
	    }

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }
}

