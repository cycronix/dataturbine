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
 * Action interface for <code>ActionThreadQueue</code>.
 * <p>
 * Classes implementing this interface can be used in
 * <code>ActionThreadQueues</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.ActionThreadQueue
 * @since V2.1
 * @version 03/13/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/13/2003  INB	Created.
 *
 */
interface Action {

    /**
     * Performs this action.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public abstract void performAction();

    /**
     * Stops this action (removes it from the queue).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Created.
     *
     */
    public abstract void stopAction();
}
