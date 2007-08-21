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
 * Provides short-term (in-memory) storage for an <code>SourceHandler</code>.
 * <p>
 * The <code>Cache</code> stores the frames in <code>FrameSets</code>. Frames
 * are added to the newest <code>FrameSet</code> until it is full (the number
 * of frames reaches a maximum number of frames or the amount of memory it is
 * using exceeds a limit).
 * <p>
 * When the <code>FrameSet</code> becomes full it is closed. A new
 * <code>FrameSet</code> is opened and future frames are added to it.
 * <p>
 * When the number of closed <code>FrameSets</code> reaches a limit or when the
 * amount of memory used by the closed <code>FrameSets</code> exceeds a limit,
 * the oldest framesets are removed from the <code>Cache</code> until the
 * limit is no longer exceeded.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.SourceHandler
 * @since V2.0
 * @version 01/08/2004
 */

/*
 * Copyright 2000, 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/11/2006  EMF      Modified constructor to take trim by time arguments.
 * 01/08/2004  INB	Added <code>clearCache</code> method.
 * 02/17/2003  INB	Modified to handle multiple <code>RingBuffers</code>
 *			per <code>RBO</code>.
 * 12/21/2000  INB	Created.
 *
 */
final class Cache
    extends com.rbnb.api.StorageManager
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
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
     * 10/11/2006  EMF  Added trim by time arguments.
     * 12/22/2000  INB	Created.
     *
     */
    Cache(float flush, float trim)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(flush,trim); //set flush,trim intervals in seconds
    }

    /**
     * Clears the contents of the <code>Cache</code>.
     * <p>
     * If there is an <code>Archive</code>, all data will be flushed to it
     * before the <code>Cache</code> is cleared to ensure that the data is not
     * lost.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
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
    public final void clearCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the door.
	    getDoor().setIdentification(getFullName() + "/" + getClass());
	    getDoor().lock("Cache.clearCache");

	    // Flush things out.
	    flush();

	    FrameManager set;
	    while (getNchildren() > 0) {
		set = (FrameManager) getChildAt(0);
		removeChildAt(0);
		if (set.getParent() == null) {
		    set.nullify();
		} else {
		    set.clear();
		}
	    }
	    setRegistered(new Registration());
	    setRemovedSets(false);
	    setAddedSets(-1);

	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }

    /**
     * Flushes the current <code>FrameSet</code> to the archive.
     * <p>
     * The <code>FrameSet</code> is closed and a new <code>FrameSet</code> is
     * opened.
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
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    final void flush()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getSet() != null) {
	    // If there is a current <code>FrameSet</code>, close it.
	    getSet().close();
	    setSet(null);
	}
    }
}
