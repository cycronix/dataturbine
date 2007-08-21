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

package com.rbnb.api;

/**
 * <bold>RBNB</bold> class to load a source archive into the server.
 * <p>
 * This class is used to automatically load archives at launch time.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.1
 * @version 02/11/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/11/2004  INB	Log exceptions at standard level.
 * 12/24/2003  INB	Report the exception when a folder fails to load.
 *			Use RAM sources.
 * 10/03/2003  INB	Retry the start on socket exceptions, unless the
 *			server is gone.
 * 04/03/2003  INB	Created.
 *
 */
final class LoadSource
    implements com.rbnb.api.Action
{
    /**
     * the name of the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/03/2003
     */
    private String archiveName = null;

    /**
     * the client-side object representing the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/03/2003
     */
    private Server server = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2003  INB	Created.
     *
     */
    LoadSource() {
	super();
    }

    /**
     * Class constructor to build a <code>LoadSource</code> object for a
     * particular <code>Server</code> and archive name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI      the <code>Server</code>.
     * @param archiveNameI the name of the archive.
     * @since V2.1
     * @version 04/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2003  INB	Created.
     *
     */
    LoadSource(Server serverI,String archiveNameI) {
	this();
	server = serverI;
	archiveName = archiveNameI;

	/*
	System.err.println("LoadSource: " + archiveNameI);
	*/
    }

    /**
     * Performs this action.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 12/24/2003  INB	Report the exception when a folder fails to load.
     *			Change to RAM source rather than TCP source.
     * 10/03/2003  INB	Retry the start on socket exceptions, unless the
     *			server is gone.
     * 04/03/2003  INB	Created.
     *
     */
    public final void performAction() {
	/*
	System.err.println("LoadSource performAction: " + archiveName);
	*/
	ServerHandler sh = server.getServerSide();
	RBNB rbnb = (RBNB) sh;

	try {
	    Source source = server.createRAMSource(archiveName);

	    source.setCframes(0);
	    source.setAframes(0);
	    source.setAmode(source.ACCESS_LOAD);
	    source.setCkeep(true);
	    source.setAkeep(true);

	    boolean started = false,
		sleepABit = false;
	    do {
		try {
		    if (sleepABit) {
			Thread.currentThread().sleep(TimerPeriod.NORMAL_WAIT);
		    }
		    source.start();
		    started = true;
		} catch (java.net.SocketException e) {
		    sleepABit = true;
		}
	    } while (!started &&
		     !sh.getTerminateRequested() &&
		     ((sh.getThread() != null) && sh.getThread().isAlive()));

	    source.stop();
	} catch (java.lang.Exception e) {
	    try {
		rbnb.getLog().addException
		    (Log.STANDARD,
		     sh.getLogClass(),
		     archiveName,
		     e);
		rbnb.getLog().addMessage
		    (Log.STANDARD,
		     sh.getLogClass(),
		     archiveName,
		     "Folder could not be loaded as an archive.");
	    } catch (java.lang.Exception e1) {
	    }
	}
    }

    /**
     * Stops this action (removes it from the queue).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/03/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2003  INB	Created.
     *
     */
    public final void stopAction() {
	ServerHandler sh = server.getServerSide();
	server = null;
	archiveName = null;
	sh.getActivityQueue().removeEvent(this);
    }
}
