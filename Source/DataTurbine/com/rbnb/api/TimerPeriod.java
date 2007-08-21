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
 * Periods for timer tasks.
 * <p>
 * This class defines all of the timer period constants used by the
 * <bold>RBNB</bold> system. All periods are in milliseconds.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.TimerTask
 * @since V2.0
 * @version 02/18/2003
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/18/2004  INB	Made the <code>STARTUP_WAIT</code> two minutes rather
 *			than five.
 * 10/22/2003  INB	Added <code>LOCK_WAIT</code>.
 * 05/22/2003  INB	Changed log status period to 0 (no logging).
 * 05/07/2003  INB	Added <code>STARTUP_WAIT</code> value.
 * 04/01/2003  INB	Added <code>PING_WAIT</code> values.
 * 03/26/2003  INB	Added <code>NORMAL_WAIT, LONG_WAIT</code> values.
 * 03/04/2003  INB	Routers don't wait as long as regular clients.
 * 02/28/2003  INB	Changed ping times to once a minute and shutdown to two
 *			minutes.
 * 12/05/2001  INB	Created.
 *
 */
final class TimerPeriod {

    /**
     * timeout for waiting for locks.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/22/2003
     */
    final static long LOCK_WAIT = 60000;

    /**
     * period for logging status.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.LogStatusInterface
     * @see com.rbnb.api.MetricsCollector
     * @since V2.0
     * @version 05/22/2003
     */
    final static long LOG_STATUS = 0;

    /**
     * a "long" wait period.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    final static long LONG_WAIT = 10000;

    /**
     * default period for collecting metrics.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.MetricsCollector
     * @see com.rbnb.api.MetricsInterface
     * @since V2.0
     * @version 11/20/2002
     */
    final static long METRICS = 1000;

    /**
     * a "normal" wait period.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    final static long NORMAL_WAIT = 1000;

    /**
     * period between server->server connectivity checks.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.RouterHandle
     * @since V2.0
     * @version 12/05/2001
     */
    final static long PING = 30000;
//    final static long PING = 5000;
//    final static long PING = 1000;

    /**
     * period to wait for server-server connectivity check responses.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.RouterHandle
     * @since V2.1
     * @version 04/01/2003
     */
    final static long PING_WAIT = 30000;
//    final static long PING_WAIT = 5000;

    /**
     * period to wait for a <code>PlugIn</code> to respond to a registration
     * request.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.RBNBPlugIn
     * @since V2.0
     * @version 02/22/2002
     */
    final static long PLUGINREGISTRATION = 60000;

    /**
     * period between parent server reconnection attempts.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.ParentServer
     * @since V2.0
     * @version 12/05/2001
     */
    final static long RECONNECT = 60000;
//    final static long RECONNECT = 1000;

    /**
     * period to wait for a clean shutdown of a client before interrupting it
     * and moving on without waiting.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/15/2002
     */
    final static long SHUTDOWN = 120000;

    /**
     * period to wait for a clean shutdown of a router before interrupting it
     * and moving on without waiting.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/04/2003
     */
    final static long SHUTDOWN_ROUTER = 5000;

    /**
     * period to wait for startup of remote handler when making a new
     * connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 05/07/2003
     */
    final static long STARTUP_WAIT = 120000;
//    final static long STARTUP_WAIT = 5000;
}

