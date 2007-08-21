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
 * Stop an object.
 * <p>
 * The objects that can be stopped include:
 * <p><ul>
 * <li><code>ClientInterfaces</code> such as <code>Sources</code>,</li>
 * <li><code>ServerInterfaces</code>, and</li>
 * <li><code>RouteInterfaces</code>.</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Start
 * @since V2.0
 * @version 01/08/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/09/2001  INB	Created.
 *
 *
 */
final class Stop
    extends com.rbnb.api.Command
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    public Stop() {
	super();
    }

    /**
     * Class constructor to build a <code>Stop</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    Stop(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(isI,disI);
    }

    /**
     * Creates a <code>Stop</code> command for the specified
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>ClientInterface</code> to stop.
     * 
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/22/2001  INB	Created.
     *
     */
    Stop(com.rbnb.api.ClientInterface clientI) {
	super(clientI);
    }

    /**
     * Creates a <code>Stop</code> command for the specified
     * <code>ServerInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>ServerInterface</code> to stop.
     * 
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2001  INB	Created.
     *
     */
    Stop(com.rbnb.api.ServerInterface serverI) {
	super(serverI);
    }

    /**
     * Creates a <code>Stop</code> command for the specified
     * <code>ShortcutInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI  the <code>ShortcutInterface</code> to stop.
     * 
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    Stop(com.rbnb.api.ShortcutInterface shortcutI) {
	super(shortcutI);
    }
}
