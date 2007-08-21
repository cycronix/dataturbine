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
import com.rbnb.sapi.Source;

/**
 * Load an archive, and detach leaving it available for other clients.
 *
 * @author Matt Miller
 *
 * @since V2.5
 * @version 12/20/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/20/2004  MJM	Created.
 *
 */
public final class LoadArchive {
    public final static void main(String[] argsI) {
	try {
	    ArgHandler ah = new ArgHandler(argsI);
	    String address = "localhost:3333";
	    String archive = "mySource";
	    String value;

	    if ((value = ah.getOption('a')) != null) {
		// Set the address of the server to check.
		address = value;
	    }

	    if ((value = ah.getOption('s')) != null) {
		// Specify archive (source) to load.
		archive = value;
	    }

	    // Check the server by creating a source, connecting it to the
	    // server, loading archive and then disconnecting. 
	    Source source = new Source(1,"load",1);
            System.err.println("Loading archive '"+archive+"'...");
	    source.OpenRBNBConnection(address,archive);
	    source.Detach();
	    source.CloseRBNBConnection();

	    // Success.
	    System.exit(0);

	} catch (java.lang.Exception e) {
	    // On any exception, return a bad status.
	    System.err.println("Error on LoadArchive");
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
}
