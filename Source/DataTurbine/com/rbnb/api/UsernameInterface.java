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
 * Username interface.
 * <p>
 * Classes implementing this interface have <code>Usernames</code> associated
 * with them.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Username
 * @since V2.0
 * @version 09/28/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/28/2004  JPW	Declare the interface public; otherwise, J# has
 *			compile-time errors.
 * 01/14/2003  INB	Created.
 *
 */
public interface UsernameInterface {

    /**
     * Gets the <code>Username</code> associated with this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Username</code>.
     * @see #setUsername(com.rbnb.api.Username usernameI)
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public abstract Username getUsername();

    /**
     * Sets the <code>Username</code> for this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param usernameI the <code>Username</code>.
     * @see #getUsername()
     * @since V2.0
     * @version 01/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/14/2003  INB	Created.
     *
     */
    public abstract void setUsername(Username usernameI);
}
