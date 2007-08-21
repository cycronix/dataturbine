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
 * Extended <code>Runnable</code> interface that allows for interrupts.
 * <p>
 * Classes implementing this interface can be "interrupted", as in a call to
 * <code>Thread.interrupt()</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/04/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/04/2001  INB	Created.
 *
 */
interface Interruptable
    extends java.lang.Runnable
{

    /**
     * Interrupts this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.SecurityException
     *		  thrown if the interrupt is not allowed by the security
     *		  system.
     * @see java.lang.Thread#interrupt()
     * @since V2.0
     * @version 04/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2001  INB	Created.
     *
     */
    public abstract void interrupt();
}

