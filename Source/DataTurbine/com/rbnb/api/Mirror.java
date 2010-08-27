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
 * Client-side representation of a mirror of data from one RBNB server to
 * another.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/26/2010  JPW	Moved the definitions of OLDEST, CONTINUOUS, and NOW
 * 			from com.rbnb.admin.MirrorDialog to this class, becasue
 * 			these constants are also be referenced in com.rbnb.sapi.Control.
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 05/10/2001  INB	Created.
 *
 */
public interface Mirror
    extends com.rbnb.api.MirrorInterface
{
    
    /**
     * Flag for setting start time to request oldest data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    public final static int OLDEST = 1;
    
    /**
     * Flag for setting mirror to never stop (no stop time).
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    public final static int CONTINUOUS = 2;
    
    /**
     * Flag for setting start or stop time to the current time.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    public final static int NOW = 3;
    
    /**
     * Clones this object.
     * <p>
     * This same abstract declaration is also included in MirrorInterface.java,
     * but for some unknown reason J# gives a compiler error if it is not also
     * included here.
     *
     * @author John Wilson
     *
     * @return the clone.
     * @see java.lang.Cloneable
     * @since V2.5
     * @version 09/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/29/2004  JPW	Created.
     *
     */
    public abstract Object clone();
}
