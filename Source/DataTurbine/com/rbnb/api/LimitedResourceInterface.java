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
 * Interface for objects representing limited resources.
 * <p>
 * Objects implementing this interface can be hold onto a resource even if they
 * do not currently have a need for it (such as open files).  They simply tell
 * the <code>LimitedResource</code> object controlling that resource of their
 * desire to hold onto the resource.  Should the <code>LimitedResource</code>
 * object need access to the resource, then it will tell the holding object
 * that it must release the resource.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.LimitedResource
 * @since V2.2
 * @version 10/23/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/23/2003  INB	Created.
 *
 */
interface LimitedResourceInterface {

    /**
     * Forces this object to release a limited resource.
     * <p>
     *
     * @author Ian Brown
     *
     * @param resourceI the resource to release (if this object has multiple
     *			resources).
     * @since V2.2
     * @version 10/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Created.
     *
     */
    public abstract void forcedRelease(Object resourceI);
}
