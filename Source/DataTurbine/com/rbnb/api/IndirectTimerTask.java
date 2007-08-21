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
 * Implementation of the <code>java.util.TimerTask</code> class.
 * <p>
 * This class isolates any references to java.util.TimerTask from the rest of
 * the code.
 * <p>
 * The "as-is" form of this file extends java.util.TimerTask, and is used when
 * compiling with an up-to-date version of Java which supports java.util.Timer.
 * <p>
 * This file also contains some preprocessor directives which can be
 * manipulated using "sed" to create a version of this file appropriate for
 * compilation under J# (which only supports Java 1.1.4).  This form of the
 * file simply includes stubs for the TimerTask methods referenced in the code,
 * but does not reference java.util.TimerTask.  com.rbnb.api.TimerTask is set
 * up to work correctly when java.util.TimerTask is not supported.  When
 * com.rbnb.api.TimerTask attempts to create an instance of IndirectTimerTask,
 * the constructor will throw a NoClassDefFoundError; this flags
 * com.rbnb.api.TimerTask to use the alternate implementation for the needed
 * java.util.TimerTask functionality.
 * <p>
 * This class was originally an internal class in com.rbnb.api.TimerTask.  It
 * was taken out of that class in order to completely isolate
 * com.rbnb.api.TimerTask from java.util.TimerTask.
 *
 * @author Ian Brown, John Wilson
 *
 * @see TimerTask, java.util.TimerTask
 * @since V2.4
 * @version 10/05/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/05/2004  JPW	Add preprocessor comment lines which will be used by
 *			"sed".
 * 10/01/2004  JPW	Created.
 *
 */
// JPW 10/05/2004: Only extend java.util.TimerTask when compiling under an
//                 up-to-date version of Java; not for compilation under J#.
//#open_java2_comment#
final class IndirectTimerTask extends java.util.TimerTask {
//#close_java2_comment#
//#java11_line# final class IndirectTimerTask {

    /**
     * our parent <code>TimerTask</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private TimerTask timerTask = null;

    /**
     * Creates an <code>IndirectTimerTask</code> for the specified
     * <code>TimerTask</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timerTaskI our parent <code>TimerTask</code>.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public IndirectTimerTask(TimerTask timerTaskI) {
	
	// Code for an up-to-date Java compiler
        //#open_java2_comment#
	super();
        timerTask = timerTaskI;
	//#close_java2_comment#
	
	// Java 1.1 compatible code: Just throw NoClassDefFoundError
	//#java11_line# throw new NoClassDefFoundError(
	//#java11_line#     "java.util.Timer not supported in Java 1.1");
	
    }

    /**
     * Runs this <code>IndirectTimerTask</code> by calling our parent's
     * <code>run</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final void run() {
        if (timerTask != null) {
            timerTask.run();
	}
    }
    
    /**
     * Stub method when compiling for Java 1.1; this method will not actually
     * be called; the alternate TimerTask functionality built into
     * com.rbnb.api.TimerTask will be used in this case.  This method is here
     * only to avoid a compile error when compiling under J#.
     * 
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.5
     * @version 10/04/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/04/2004  JPW	Created.
     *
     */
    //#java11_line# public final void cancel()
    //#java11_line# {
    //#java11_line# }

}

