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
 * Timed task handling interface.
 * <p>
 * Classes implementing this interface can schedule tasks to be performed on a
 * timer.
 * <p>
 * Tasks using this interface are scheduled by creating a
 * <code>TimerTask</code> using an object implementing this interface and a
 * code string. The <code>TimerTask</code> is then passed to a
 * <code>Timer</code> for execution. Tasks can be scheduled as a one-time thing
 * or to execute on an interval timer.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.TimerTask
 * @since V2.0
 * @version 12/05/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/05/2001  INB	Created.
 *
 */
interface TimerTaskInterface {

    /**
     * Executes a task on a timer.
     * <p>
     * The input code string specifies which of an optional number of specific
     * tasks is to be performed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param codeI the task-specific code string.
     * @since V2.0
     * @version 12/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/05/2001  INB	Created.
     *
     */
    public abstract void timerTask(TimerTask ttI);
}
