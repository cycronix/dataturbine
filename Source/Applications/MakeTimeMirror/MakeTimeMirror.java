/*
Copyright 2011 Erigo Technologies LLC

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

import com.rbnb.api.Client;
import com.rbnb.api.Controller;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Source;
import com.rbnb.sapi.Control;

/*****************************************************************************
 *
 * MakeTimeMirror
 *
 * Create a time-based PUSH mirror, starting NOW and going on forever.  The
 * output/downstream source's is set as follows: cache = 1, archive = 10000,
 * archive mode = append.
 *
 * Before making the mirror, we check to make sure we can connect to the
 * downstream source.  If there is already an existing output Source with the
 * desired output Source name, we terminate that Source before setting up the
 * new Mirror.
 *
 * If the upstream source's frames each contain multiple points (let's say
 * N points per frame) then the effective size of the downstream source is not
 * the same as the upstream source (since data is fed to the downstream source
 * on a point-by-point basis).  The total storage space of the downstream
 * source will only be 1/N that of the upstream source.  For example, if the
 * upstream source contains 100 frames total, and each frame contains 10
 * points, then the total number of points that can be stored in the upstream
 * source is 1000.  The downstream source, however, will only be able to hold
 * 100 points total (1/10th the amount of the upstream source) because data is
 * fed to the downstream source on a point-by-point basis.
 *
 * Copyright 2010 Erigo Technologies
 *
 * Version: 0.2
 *
 * Modification History
 * --------------------
 * 10/28/2010  JPW  Created.
 * 10/29/2010  MJM  Change to "append" archive mode; don't match upstream source.
 * 11/04/2010  JPW  Try indefinitely to connect to the downstream server (we
 *                  used to have a specific time limit, but now we just keep
 *                  trying indefinitely).
 * 12/23/2010  JPW  Add stopOutputSource() - terminate an existing output
 *                  Source (if one already exists).
 * 01/06/2011  JPW  Add version number (start at 0.2)
 *
 */

public class MakeTimeMirror {
    
    public static void main(String[] argsI) throws Exception {
    	
	if (argsI.length != 4) {
	    System.err.println("Usage: java MakeTimeMirror <from server address> <from source name> <to server address> <to source name>");
	    System.exit(0);
	}
    	
    	// Get the arguments
    	String fromServerAddr = argsI[0];
    	String fromSourceName = argsI[1];
    	String toServerAddr = argsI[2];
    	String toSourceName = argsI[3];
	
    	// Make sure we can connect to the downstream RBNB server before proceeding
	while (true) {
	    try {
		Server tempServer = Server.newServerHandle("DTServer",toServerAddr);
		Controller tempController = tempServer.createController("tempMirrorConnection");
		tempController.start();
		try {
		    stopOutputSource(tempController,toSourceName);
		} catch (Exception me) {
		    System.err.println("Caught exception trying to stop existing downstream Mirror:\n" + me);
		}
		tempController.stop();
		break;
	    } catch (Exception e) {
		// Must not have been able to make the connection; try again
		// after sleeping for a bit
		System.err.println("Waiting for downstream server to be network accessible...");
		try {Thread.sleep(20000);} catch (Exception e2) {}
	    }
	}
    	
    	System.err.println(
	    "\nCreate a time-based push mirror:\n\t" +
	    fromServerAddr + "/" + fromSourceName +
	    " --> " +
	    toServerAddr + "/" + toSourceName);
    	
	// Frame-based mirror; automatically match the originating source:
	// Control cont = new Control();
	// cont.OpenRBNBConnection(fromServerName,"tempConnection");
	// cont.CreateMirrorOut(fromSourceName,toServerName,toSourceName);
	// cont.CloseRBNBConnection();
	
	Server server = Server.newServerHandle("DTServer",fromServerAddr);
	Controller controller = server.createController("tempMirrorConnection");
	controller.start();
	
	// Setup the time mirror
	Control.createTimeMirror(
	    controller,
	    server,
	    null,
	    server.getAddress(),
	    null,
	    fromSourceName,
	    null,
	    toServerAddr,
	    toSourceName,
	    com.rbnb.api.Mirror.NOW,
	    com.rbnb.api.Mirror.CONTINUOUS,
	    1,
	    10000,
	    com.rbnb.api.SourceInterface.ACCESS_APPEND,
	    false,  // DON'T MATCH SOURCE; we want to force append mode
	    0.);
	
	controller.stop();
    }
    
    /**
     * If there is already an existing output Source (possibly from an earlier
     * Mirror that went away and left the output Source) then we need to
     * terminate this existing output Source first before establishing the
     * new Mirror.  Otherwise, when the new Mirror tries to make the new output
     * Source, an IllegalStateException will be thrown (“Cannot reconnect to
     * existing client handler”).
     * <p>
     * This method is largely based on com.rbnb.api.MirrorController.stopOutputSource()
     * This method uses the same logic as rbnbAdmin for terminating a Source.
     * <p>
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/06/2007  JPW	Created.
     *
     */
    private static void stopOutputSource(Controller controllerI, String sourceNameI) throws Exception {
	
	Rmap tempRmap =
	    Rmap.createFromName(
		sourceNameI + Rmap.PATHDELIMITER + "...");
	tempRmap.markLeaf();
	Rmap rmap = controllerI.getRegistered(tempRmap);
	if (rmap == null) {
	    // No existing downstream source - just return
	    return;
	}
	// Get rid of all the unnamed stuff in the Rmap hierarchy
	rmap = rmap.toNameHierarchy();
	if (rmap == null) {
	    // No existing downstream source - just return
	    return;
	}
	// System.err.println(
	//     "\nMakeTimeMirror.stopOutputSource(): Full Rmap =\n" +
	//     rmap +
	//     "\n");
	Rmap startingRmap = rmap.findDescendant(sourceNameI,false);
	if (startingRmap == null) {
	    // No existing downstream source - just return
	    return;
	}
	// System.err.println(
	//     "\nMirrorController.stopOutputSource(): Starting Rmap =\n" +
	//     startingRmap +
	//     "\n");
	
	// If the client is a Source, clear the keep cache flag.  This will
	// ensure that the RBO will actually go away.
	if (startingRmap instanceof Source) {
	    ((Source) startingRmap).setCkeep(false);
	}
	// Stop the downstream source
	System.err.println("Stopping the existing downstream source before starting the new Mirror.");
	controllerI.stop((Client)startingRmap);
	
    }
    
}
