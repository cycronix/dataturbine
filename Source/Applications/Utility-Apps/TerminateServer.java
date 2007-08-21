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

import com.rbnb.utility.ArgHandler;
import com.rbnb.api.Controller;
import com.rbnb.api.Server;

/**
 * Terminates an RBNB server.
 * <p>
 * This class uses the so-called "full API".
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.3.2
 * @version 06/22/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/22/2004  INB	Created.
 *
 */
public final class TerminateServer {

    /**
     * Performs the server termination.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments:
     *		    <br><ul>
     *                  <li>-a [server address]</li>
     *		    </ul>
     * @since V2.3.2
     * @version 06/22/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/22/2004  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {
	    ArgHandler ah = new ArgHandler(argsI);
	    String address = "localhost:3333";
	    String value;

	    if ((value = ah.getOption('a')) != null) {
		// Set the address of the server to check.
		address = value;
	    }

	    // Terminate the server.
	    Server server = Server.newServerHandle(null,address);
	    server.stop();

	    // Success.
	    System.exit(0);

	} catch (java.lang.Exception e) {
	    // On any exception, return a bad status.
	    System.exit(-1);
	}
    }
}
