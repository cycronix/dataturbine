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
 * Implementation of the <code>java.util.Timer</code> class.
 * <p>
 * This class isolates any references to java.util.Timer from the rest of the
 * code.
 * <p>
 * The "as-is" form of this file extends java.util.Timer, and is used when
 * compiling with an up-to-date version of Java which supports java.util.Timer.
 * <p>
 * This file also contains some preprocessor directives which can be
 * manipulated using "sed" to create a version of this file appropriate for
 * compilation under J# (which only supports Java 1.1.4).  This form of the
 * file simply includes stubs for the Timer methods referenced in the code, but
 * does not reference java.util.Timer.  com.rbnb.api.Timer is set up to work
 * correctly when java.util.Timer is not supported.  When com.rbnb.api.Timer
 * attempts to create an instance of IndirectTimer, the constructor will throw
 * a NoClassDefFoundError; this flags com.rbnb.api.Timer to use the alternate
 * implementation for the needed java.util.Timer functionality.
 * <p>
 *
 * @author John Wilson
 *
 * @see Timer, java.util.Timer
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
 *			"sed".  Added stub cancel() method used when compiling
 *			for J#.
 * 10/01/2004  JPW	Created.
 *
 */
// JPW 10/01/2004: Only extend java.util.Timer when compiling under an
//                 up-to-date version of Java; not for compilation under J#.
//#open_java2_comment#
final class IndirectTimer extends java.util.Timer {
//#close_java2_comment#
//#java11_line# final class IndirectTimer {
    /**
     * Default class constructor when compiling under up-to-date version of
     * Java.
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.4
     * @version 10/01/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2004  JPW	Created.
     *
     */
    //#open_java2_comment#
    public IndirectTimer() {
        super();
    }
    //#close_java2_comment#

    /**
     * Creates a new <code>IndirectTimer</code> whose associated thread may be
     * specified to run as a daemon.  Used when compiling under up-to-date
     * version of Java.
     * <p>
     *
     * @author John Wilson
     *
     * @param isDaemonI true if associated thread should run as a daemon.
     * @since V2.4
     * @version 10/01/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2004  JPW	Created.
     *
     */
    //#open_java2_comment#
    public IndirectTimer(boolean isDaemonI) {
	super(isDaemonI);
    }
    //#close_java2_comment#
    
    /**
     * Default class constructor when compiling under Java 1.1.
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.4
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
     
    //#java11_line# public IndirectTimer() throws NoClassDefFoundError {
    //#java11_line#     throw new NoClassDefFoundError(
    //#java11_line#         "java.util.Timer not supported in Java 1.1");
    //#java11_line# }
     
    /**
     * Creates a new <code>IndirectTimer</code> whose associated thread may be
     * specified to run as a daemon.  Used when compiling under Java 1.1.
     * <p>
     *
     * @author John Wilson
     *
     * @param isDaemonI true if associated thread should run as a daemon.
     * @since V2.4
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
     
    //#java11_line# public IndirectTimer(boolean isDaemon)
    //#java11_line#     throws NoClassDefFoundError {
    //#java11_line#         throw new NoClassDefFoundError(
    //#java11_line#             "java.util.Timer not supported in Java 1.1");
    //#java11_line# }

    /**
     * Stub method used to sucessfully compile code under Java 1.1.  This method
     * will not actually get called; the alternate Timer functionality in
     * com.rbnb.api.Timer is executed in its place.
     * <p>
     *
     * @author John Wilson
     *
     * @param task - task to be scheduled.
     * @param delay - delay in milliseconds before task is to be executed.
     * @since V2.4
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
     
    //#java11_line# public void schedule(IndirectTimerTask task, long delay)
    //#java11_line# {
    //#java11_line# }

    /**
     * Stub method used to sucessfully compile code under Java 1.1.  This method
     * will not actually get called; the alternate Timer functionality in
     * com.rbnb.api.Timer is executed in its place.
     * <p>
     *
     * @author John Wilson
     *
     * @param task - task to be scheduled.
     * @param time - time at which task is to be executed.
     * @since V2.4
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
    
    //#java11_line# public void schedule(
    //#java11_line#     IndirectTimerTask task, java.util.Date time)
    //#java11_line# {
    //#java11_line# }

    /**
     * Stub method used to sucessfully compile code under Java 1.1.  This method
     * will not actually get called; the alternate Timer functionality in
     * com.rbnb.api.Timer is executed in its place.
     * <p>
     *
     * @author John Wilson
     *
     * @param task - task to be scheduled.
     * @param delay - delay in milliseconds before task is to be executed.
     * @param period - time in milliseconds between successive task executions.
     * @since V2.4
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
    
    //#java11_line# public void schedule(
    //#java11_line#     IndirectTimerTask task, long delay, long period)
    //#java11_line# {
    //#java11_line# }

    /**
     * Stub method used to sucessfully compile code under Java 1.1.  This method
     * will not actually get called; the alternate Timer functionality in
     * com.rbnb.api.Timer is executed in its place.
     * <p>
     *
     * @author John Wilson
     *
     * @param task - task to be scheduled.
     * @param firstTime - First time at which task is to be executed.
     * @param period - time in milliseconds between successive task executions.

     * @since V2.4
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
    
    //#java11_line# public void schedule(
    //#java11_line#     IndirectTimerTask task,
    //#java11_line#     java.util.Date firstTime,
    //#java11_line#     long period)
    //#java11_line# {
    //#java11_line# }

    /**
     * Stub method used to sucessfully compile code under Java 1.1.  This method
     * will not actually get called; the alternate Timer functionality in
     * com.rbnb.api.Timer is executed in its place.
     * <p>
     *
     * @author John Wilson
     *
     * @param task - task to be scheduled.
     * @param delay - delay in milliseconds before task is to be executed.
     * @param period - time in milliseconds between successive task executions.
     * @since V2.4
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
    
    //#java11_line# public void scheduleAtFixedRate(
    //#java11_line#     IndirectTimerTask task, long delay, long period)
    //#java11_line# {
    //#java11_line# }

    /**
     * Stub method used to sucessfully compile code under Java 1.1.  This method
     * will not actually get called; the alternate Timer functionality in
     * com.rbnb.api.Timer is executed in its place.
     * <p>
     *
     * @author John Wilson
     *
     * @param task - task to be scheduled.
     * @param firstTime - First time at which task is to be executed.
     * @param period - time in milliseconds between successive task executions.
     * @since V2.4
     * @version 10/04/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/09/2004  JPW	To compile under J#, changed method argument
     *			from "TimerTask task" to "IndirectTimerTask task".
     * 10/04/2004  JPW	Created.
     *
     */
    
    //#java11_line# public void scheduleAtFixedRate(
    //#java11_line#     IndirectTimerTask task,
    //#java11_line#     java.util.Date firstTime,
    //#java11_line#     long period)
    //#java11_line# {
    //#java11_line# }
    
    /**
     * Stub method when compiling for Java 1.1; this method will not actually
     * be called; the alternate Timer functionality built into
     * com.rbnb.api.Timer will be used in this case.  This method is here
     * only to avoid a compile error when compiling under J#.
     * 
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.5
     * @version 10/05/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/05/2004  JPW	Created.
     *
     */
    //#java11_line# public final void cancel()
    //#java11_line# {
    //#java11_line# }

}

