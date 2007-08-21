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
 * Server-side representation of a source client application connection to the
 * RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 01/08/2004  INB	Added <code>get/setPerformClearCache</code> methods.
 * 04/07/2003  INB	Added <code>stopOnIOException</code>.
 * 02/17/2003  INB	Modified for multiple <code>RingBuffers</code> per
 *			<code>SourceHandler</code>. This adds some archive
 *			handling to the <code>RBO</code> itself.
 * 05/09/2001  INB	Created.
 *
 */
interface SourceHandler
    extends com.rbnb.api.ClientHandler,
	    com.rbnb.api.DataSizeMetricsInterface,
	    com.rbnb.api.NotificationFrom,
	    com.rbnb.api.SourceInterface
{

    /**
     * Clones this object.
     * <p>
     * This same abstract declaration is also included in RmapInterface.java,
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

    /**
     * Gets the archive directory.
     * <p>
     * This method works up the Rmap chain until it sees the
     * <code>Server</code> object and prepends names.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive directory.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public abstract String getArchiveDirectory();

    /**
     * Gets the explicitly registered <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the explicitly registered <code>Rmap</code> hierarchy.
     * @since V2.0
     * @version 04/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2002  INB	Created.
     *
     */
    public abstract Rmap getExplicitRegistration();

    /**
     * Gets the next <code>Rmap</code> frame index to assign.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the next <code>Rmap</code> frame index.
     * @see #setNfindex(long)
     * @since V2.0
     * @version 12/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/12/2001  INB	Created.
     *
     */
    public abstract long getNfindex();

    /**
     * Gets the next <code>RingBuffer</code> frame index to assign.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the next <code>RingBuffer</code> frame index.
     * @see #setNrbindex(long)
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created.
     *
     */
    public abstract long getNrbindex();

    /**
     * Gets the perform clear cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return perform a reset?
     * @see #setPerformClearCache(boolean)
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public abstract boolean getPerformClearCache();

    /**
     * Gets the perform reset flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return perform a reset?
     * @see #setPerformReset(boolean)
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public abstract boolean getPerformReset();

    /**
     * Sets the next <code>Rmap</code> frame index to be assigned.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nFindexI  the next <code>Rmap</code> frame index.
     * @see #getNfindex()
     * @since V2.0
     * @version 12/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/12/2001  INB	Created.
     *
     */
    public abstract void setNfindex(long nFindexI);

    /**
     * Sets the next <code>RingBuffer</code> index to be assigned.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nRBindexI  the next <code>RingBuffer</code> index.
     * @see #getNrbindex()
     * @since V2.1
     * @version 02/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2003  INB	Created.
     *
     */
    public abstract void setNrbindex(long nRBindexI);

    /**
     * Sets the perform clear cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param performClearCacheI perform a clear cache?
     * @see #getPerformClearCache()
     * @since V2.2
     * @version 01/08/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2004  INB	Created.
     *
     */
    public abstract void setPerformClearCache(boolean performClearCacheI);

    /**
     * Sets the perform reset flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param performResetI perform a reset?
     * @see #getPerformReset()
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public abstract void setPerformReset(boolean performResetI);

    /**
     * Stops the <code>SourceHandler</code> on an I/O exception in the
     * <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return did the stop actually occur?
     * @exception java.lang.InterruptedException
     *		  thrown if this method is interrupted.
     * @since V2.1
     * @version 04/07/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2003  INB	Created.
     *
     */
    public abstract boolean stopOnIOException()
	throws java.lang.InterruptedException;

    /**
     * Updates the <code>Registration</code> for the
     * <code>SourceHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2001  INB	Created.
     *
     */
    public abstract void updateRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;
}
