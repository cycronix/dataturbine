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
 * <code>Serializable</code> class to represent a newly created object.
 * <p>
 * The real object is stored as a field of this object.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/16/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/23/2001  INB	Created.
 *
 */
final class NewObjectEvent
    extends com.rbnb.api.Command
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    NewObjectEvent() {
	super();
    }

    /**
     * Class constructor to build a <code>NewObjectEvent</code> for the input
     * <code>Serializable</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the <code>Serializable</code> object.
     * @since V2.0
     * @version 10/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    NewObjectEvent(Serializable objectI) {
	super();
	setObject(objectI);
    }
}
