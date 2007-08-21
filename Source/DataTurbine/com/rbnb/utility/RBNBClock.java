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

package com.rbnb.utility;

/**
 * <bold>RBNB</bold> clock class.
 * <p>
 * This class provides a way to synchronize the time-stamps between two or more
 * <bold>RBNB</bold> applications running under a single JVM when one of the
 * applications wishes to provide a master clock for the others.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V1.3
 * @version 04/05/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/05/2002  INB	Created.
 *
 */
public final class RBNBClock {

    /**
     * has the clock been set?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.3
     * @version 04/05/2002
     */
    private static boolean clockSet = false;

    /**
     * the clock time (milliseconds since 1970).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.3
     * @version 04/05/2002
     */
    private static long clockTime = 0L;

    /**
     * Returns the current clock time.
     * <p>
     * If the clock has been set, then this method returns the set time.  If
     * the clock is free running, then this method calls
     * <code>java.lang.System.currentTimeMillis</code>.
     *
     * @author Ian Brown
     *
     * @return the clock time.
     * @see #freeRun()
     * @see #set(long clockTimeI)
     * @since V1.3
     * @version 04/05/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2002  INB	Created.
     *
     */
    public final static synchronized long currentTimeMillis() {
	long cTimeR;

	if (clockSet) {
	    cTimeR = clockTime;
	} else {
	    cTimeR = System.currentTimeMillis();
	}

	return (cTimeR);
    }

    /**
     * Releases the clock to run free.
     * <p>
     * In this mode, <code>currentTimeMillis()</code> calls
     * <code>java.lang.System.currentTimeMillis()</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #currentTimeMillis()
     * @see #set(long clockTimeI)
     * @since V1.3
     * @version 04/05/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2002  INB	Created.
     *
     */
    public final static synchronized void freeRun() {
	clockSet = false;
    }

    /**
     * Sets the clock to the input value.
     * <p>
     * Once the clock has been set, <code>currentTimeMillis</code> returns the
     * set value until the clock is set to a new value or is set to run free.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clockTimeI the new clock time (milliseconds since 1970).
     * @see #currentTimeMillis()
     * @see #freeRun()
     * @since V1.3
     * @version 04/05/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2002  INB	Created.
     *
     */
    public final static synchronized void set(long clockTimeI) {
	clockTime = clockTimeI;
	clockSet = true;
    }
}
