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

package com.rbnb.simpleplayer;

import com.rbnb.sapi.*;

/**
 * Listener interface for the <bold>RBNB</bold> V2 player class.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.simpleplayer.Player
 * @since V2.0
 * @version 09/24/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/24/2002  INB	Created.
 *
 */
public interface PlayerTimeListener {

    /**
     * Posts time information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cmapI the <code>ChannelMap</code>.
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/24/2002  INB	Created.
     *
     */
    public abstract void postTime(ChannelMap cmapI);
}

