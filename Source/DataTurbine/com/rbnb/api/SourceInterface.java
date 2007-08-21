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
 * Common representation of a source client application connection to the RBNB
 * server.
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
 * 09/28/2004  JPW	Declare this interface public; otherwise, J# has
 *			compile time errors.
 * 01/08/2004  INB	Added <code>clearCache</code> method.
 * 07/30/2003  INB	Added <code>deleteChannels</code> method.
 * 05/09/2001  INB	Created.
 *
 */
public interface SourceInterface
    extends com.rbnb.api.ClientInterface
{
    /**
     * do not access an archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ACCESS_APPEND
     * @see #ACCESS_CREATE
     * @see #ACCESS_LOAD
     * @since V2.0
     * @version 05/09/2001
     */
    public final static byte ACCESS_NONE = 0;

    /**
     * read an existing archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ACCESS_APPEND
     * @see #ACCESS_CREATE
     * @see #ACCESS_NONE
     * @since V2.0
     * @version 05/09/2001
     */
    public final static byte ACCESS_LOAD = 1;

    /**
     * create a new archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ACCESS_APPEND
     * @see #ACCESS_LOAD
     * @see #ACCESS_NONE
     * @since V2.0
     * @version 05/09/2001
     */
    public final static byte ACCESS_CREATE = 2;

    /**
     * append to an existing archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ACCESS_CREATE
     * @see #ACCESS_LOAD
     * @see #ACCESS_NONE
     * @since V2.0
     * @version 05/09/2001
     */
    public final static byte ACCESS_APPEND = 3;

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
    public abstract void clearCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     * Deletes one or more channels from the <code>SourceInterface</code>.
     * <p>
     * Channels are deleted by deleting the <code>RingBuffer</code> that
     * contains them.  If that <code>RingBuffer</code> contains more than one
     * channel, then the request must specify that all of the channels are to
     * be deleted or those that are specified will not be deleted.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelsI <code>Rmap</code> hierarchy specifying the channels
     *			to be deleted.
     * @return <code>Rmap</code> containing status information for each of
     *	       the channels.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
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
     * @since V2.2
     * @version 07/30/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    public abstract Rmap deleteChannels(Rmap channelsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the maximum number of frames allowed in the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive limit in frames.
     * @see #setAframes(long)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract long getAframes();

    /**
     * Gets the keep archive flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return keep the archive on disk?
     * @see #setAkeep(boolean)
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public abstract boolean getAkeep();

    /**
     * Gets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive access mode.
     * @see #ACCESS_APPEND
     * @see #ACCESS_CREATE
     * @see #ACCESS_LOAD
     * @see #ACCESS_NONE
     * @see #setAmode(byte)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public abstract byte getAmode();

    /**
     * Gets the maximum amount of memory usage allowed for the archive.
     * <p>
     * This method is not currently supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive size in bytes.
     * @see #setAsize(long)
     * @since V2.0
     * @version 09/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract long getAsize();

    /**
     * Gets the maximum number of frames allowed in the cache.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cache limit in frames.
     * @see #setCframes(long)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract long getCframes();

    /**
     * Gets the keep cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return keep the cache in the server on disconnect?
     * @see #setCkeep(boolean)
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public abstract boolean getCkeep();

    /**
     * Gets the maximum amount of memory usage allowed for the cache.
     * <p>
     * This method is not currently supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cache size in bytes.
     * @see #setCsize(long)
     * @since V2.0
     * @version 09/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract long getCsize();

    /**
     * Gets the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of <code>FrameSets</code>.
     * @see #setNfs(int)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public abstract int getNfs();

    /**
     * Updates the registration for this <code>Source</code>.
     * <p>
     * The input <code>Rmap</code> hierarchy is used to update the registration
     * for this <code>Source</code>.  The hierarchy may contain
     * <code>DataBlocks</code>, but not time information.  Those
     * <code>DataBlocks</code> are copied into the appropriate locations in the
     * registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI the registration <code>Rmap</code> hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 09/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    public abstract void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Reset this <code>Source's</code> ring buffer.
     * <p>
     * This method performs the functional equivalent of closing and re-opening
     * the <code>Source</code>.  A completely new ring buffer is created to
     * replace the existing one.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
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
    public abstract void reset()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the maximum number of frames allowed in the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a maximum number
     * of frames.
     * <p>
     * The following ratios should be positive integers:
     * <p><ul>
     * <li>cache frames/number of sets, and</li>
     * <li>archive frames/cache frames.</li>
     * </ul><p>
     * If these ratios are not positive integers, then the <bold>RBNB</bold>
     * server increases the numerator until the result is a positive integer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param archiveFramesI  the archive limit in frames.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the archive limit specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getAframes()
     * @see #setCframes(long)
     * @see #setNfs(int)
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract void setAframes(long archiveFramesI);

    /**
     * Sets the keep archive flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param keepI keep the archive on disk?
     * @see #getAkeep()
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public abstract void setAkeep(boolean keepI);

    /**
     * Sets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param modeI  the archive access mode.
     * @see #ACCESS_APPEND
     * @see #ACCESS_CREATE
     * @see #ACCESS_LOAD
     * @see #ACCESS_NONE
     * @see #getAmode()
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public abstract void setAmode(byte modeI);

    /**
     * Sets the maximum amount of memory usage allowed for the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a maximum amount
     * of memory.
     * <p>
     * This method is not currently supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @param archiveSizeI  the archive size in bytes.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the archive size specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getAsize()
     * @since V2.0
     * @version 09/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract void setAsize(long archiveSizeI);

    /**
     * Sets the maximum number of frames allowed in the cache.
     * <p>
     * A value of -1 means that the cache is not limited to a maximum number
     * of frames.
     * <p>
     * The following ratios should be positive integers:
     * <p><ul>
     * <li>cache frames/number of sets, and</li>
     * <li>archive frames/cache frames.</li>
     * </ul><p>
     * If these ratios are not positive integers, then the <bold>RBNB</bold>
     * server increases the numerator until the result is a positive integer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheFramesI  the cache limit in frames.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the cache limit specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #getCframes()
     * @see #setAframes(long)
     * @see #setNfs(int)
     * @since V2.0
     * @version 09/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract void setCframes(long cacheFramesI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the keep cache flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param keepI keep the cache in the server?
     * @see #getCkeep()
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/08/2002  INB	Created.
     *
     */
    public abstract void setCkeep(boolean keepI);

    /**
     * Sets the maximum amount of memory usage allowed for the cache.
     * <p>
     * A value of -1 means that the cache is not limited to a maximum amount of
     * memory.
     * <p>
     * This method is not currently supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cacheSizeI  the cache size in bytes.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the cache size specified is not a legal value:
     *		  <p><ul>
     *		  <li>negative values other than -1,</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getCsize()
     * @since V2.0
     * @version 09/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public abstract void setCsize(long cacheSizeI);

    /**
     * Sets the number of cache <code>FrameSets</code>.
     * <p>
     * The following ratios should be positive integers:
     * <p><ul>
     * <li>cache frames/number of sets, and</li>
     * <li>archive frames/cache frames.</li>
     * </ul><p>
     * If these ratios are not positive integers, then the <bold>RBNB</bold>
     * server increases the numerator until the result is a positive integer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameSetsI  the number of cache <code>FrameSets</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of cache <code>FrameSets</code>
     *		  specified is not a legal value:
     *		  <p><ul>
     *		  <li>values less than 0, or</li>
     *		  <li>larger than some server-imposed limit.</li>
     *		  </ul><p>
     * @see #getNfs()
     * @see #setAframes(long)
     * @see #setCframes(long)
     * @since V2.0
     * @version 09/26/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public abstract void setNfs(int frameSetsI);
}
