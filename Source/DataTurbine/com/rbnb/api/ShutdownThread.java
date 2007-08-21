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
 * Server shutdown class.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RBNB
 * @since V2.0
 * @version 01/15/2004
 */

/*
 * Copyright 2001, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/15/2004  INB	Added name to thread.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 12/06/2001  INB	Created.
 *
 */
final class ShutdownThread
    extends com.rbnb.api.ThreadWithLocks
{
    /**
     * the <code>RBNB</code> object to shutdown.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/06/2001
     */
    private RBNB rbnb = null;

    /**
     * Class constructor to build a <code>ShutdownThread</code> for an
     * <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbI the RBNB.
     * @since V2.0
     * @version 01/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/15/2004  INB	Added name to thread.
     * 12/06/2001  INB	Created.
     *
     */
    ShutdownThread(RBNB rbnbI) {
	super(rbnbI.getName() + "_Shutdown");
	setRBNB(rbnbI);
    }

    /**
     * Gets the <code>RBNB</code> to shutdown.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RBNB</code>.
     * @see #setRBNB(com.rbnb.api.RBNB)
     * @since V2.0
     * @version 12/06/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2001  INB	Created.
     *
     */
    private final RBNB getRBNB() {
	return (rbnb);
    }

    /**
     * Runs this <code>ShutdownThread</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 12/06/2001  INB	Created.
     *
     */
    public final void run() {
	try {
	    getRBNB().stop(null);
	} catch (java.lang.Exception e) {
	}
	clearLocks();
    }

    /**
     * Sets the <code>RBNB</code> to shutdown.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbI the <code>RBNB</code>.
     * @see #getRBNB()
     * @since V2.0
     * @version 12/06/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2001  INB	Created.
     *
     */
    private final void setRBNB(RBNB rbnbI) {
	rbnb = rbnbI;
    }
}

