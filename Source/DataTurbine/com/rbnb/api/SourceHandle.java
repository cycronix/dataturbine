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
 * Client-side object that represents a client source application connection to
 * an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/08/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/25/2005  EMF      Added bytesTransferred method.
 * 01/08/2004  INB	Added support for <code>clearCache</code>.
 * 07/30/2003  INB	Added support for <code>deleteChannels<code>.
 * 05/11/2001  INB	Created.
 *
 */
class SourceHandle
    extends com.rbnb.api.ClientHandle
    implements com.rbnb.api.Source
{
    /**
     * the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Source#ACCESS_APPEND
     * @see com.rbnb.api.Source#ACCESS_CREATE
     * @see com.rbnb.api.Source#ACCESS_LOAD
     * @see com.rbnb.api.Source#ACCESS_NONE
     * @since V2.0
     * @version 05/10/2001
     */
    private byte accessMode = Source.ACCESS_NONE;

    /**
     * the desired number of archive frames allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long archiveFrames = 0;

    /**
     * keep the archive?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/08/2002
     */
    private boolean archiveKeep = true;

    /**
     * the desired size of the archive in bytes.
     * <p>
     * A value of -1 means that the archive is not limited by memory use.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long archiveSize = 0;

    /**
     * the desired number of cache frames allowed.
     * <p>
     * A value of -1 means that the cache is not limited to a particular number
     * of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long cacheFrames = -1;

    /**
     * keep the cache?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/08/2002
     */
    private boolean cacheKeep = false;

    /**
     * the desired size of the cache in bytes.
     * <p>
     * A value of -1 means that the cache is not limited by memory use.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long cacheSize = -1;

    /**
     * the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private int frameSets = 10;

    /**
     * need to be synchronized?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/12/2002
     */
    private boolean needSynchronization = false;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    SourceHandle() {
	super();
    }

    /**
     * Class constructor to build a <code>SourceHandle</code> by reading it in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
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
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    SourceHandle(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Class constructor to build a <code>SourceHandle</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    SourceHandle(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
    }

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/25/2005  EMF	Created.
     *
     */
    public long bytesTransferred() {
      return getACO().bytesTransferred();
    }

    /**
    /**
     * Adds a child <code>Rmap</code> to this <code>SourceHandle</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the new child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2000  INB	Created.
     *
     */
    public synchronized void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getACO().addChild(childI);
	needSynchronization = true;
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 05/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/25/2001  INB	Created.
     *
     */
    String additionalToString() {
	return (SourceIO.additionalToString(this));
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
	if (!getACO().isSupported(IsSupported.FEATURE_CLEAR_CACHE)) {
	    throw new com.rbnb.api.SerializeException
		("Clearing the cache is not supported by this version of " +
		 "the server.");
	}

	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().clearCache();
    }

    /**
     * Deletes one or more channels from the <code>SourceHandler</code>.
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
    public final Rmap deleteChannels(Rmap channelsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!getACO().isSupported(IsSupported.FEATURE_DELETE_CHANNELS)) {
	    throw new com.rbnb.api.SerializeException
		("Deleting channels is not supported by this version of " +
		 "the server.");
	}
	getACO().send(new DeleteChannels(channelsI));
	return ((Rmap) getACO().receive(ACO.rmapClass,false,Sink.FOREVER));
    }

    /**
     * Gets the desired number of frames allowed in the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive limit in frames.
     * @see #setAframes(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getAframes() {
	return (archiveFrames);
    }

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
    public final boolean getAkeep() {
	return (archiveKeep);
    }

    /**
     * Gets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive access mode.
     * @see com.rbnb.api.Source#ACCESS_APPEND
     * @see com.rbnb.api.Source#ACCESS_CREATE
     * @see com.rbnb.api.Source#ACCESS_LOAD
     * @see com.rbnb.api.Source#ACCESS_NONE
     * @see #setAmode(byte)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public final byte getAmode() {
	return (accessMode);
    }

    /**
     * Gets the desired amount of memory usage allowed for the archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive size in bytes.
     * @see #setAsize(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getAsize() {
	return (archiveSize);
    }

    /**
     * Gets the desired number of frames allowed in the cache.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cache limit in frames.
     * @see #setCframes(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getCframes() {
	return (cacheFrames);
    }

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
    public final boolean getCkeep() {
	return (cacheKeep);
    }

    /**
     * Gets the desired amount of memory usage allowed for the cache.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cache size in bytes.
     * @see #setCsize(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public long getCsize() {
	return (cacheSize);
    }

    /**
     * Gets the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of <code>FrameSets</code>.
     * @see #setNfs(int)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2001  INB	Created.
     *
     */
    public final int getNfs() {
	return (frameSets);
    }

    /**
     * Is the specified type of operation implemented?
     * <p>
     * <code>SourceIOs</code> allow for children, but not extractions or
     * members.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI  the type of operation.
     * @return is the operation implemented?
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2001  INB	Created.
     *
     */
    public boolean isImplemented(byte typeI) {
	return (typeI == OPR_CHILDREN);
    }

    /**
     * Is this <code>ClientHandle</code> synchronized with the server?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the client synchronized?
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2002  INB	Created.
     *
     */
    public final boolean isSynchronized() {
	return (super.isSynchronized() && !needSynchronization);
    }

    /**
     * Creates a new instance of the same class as this
     * <code>SourceHandle</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (SourceIO.newInstance(this));
    }

    /**
     * Reads the <code>SourceIO</code> from the specified input stream.
     * <p>
     * Any missing fields are copied over from the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Source</code> as an <code>Rmap</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
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
     * @see #write(com.rbnb.api.Rmap,String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 07/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	SourceIO.read(isI,disI,this,otherI);
	if ((otherI.getDblock() != null) &&
	    (otherI.getDblock().getData() != null) &&
	    (getDblock().getNpts()*getDblock().getPtsize() > 0) &&
	    (getDblock().getData() == null)) {
	    getDblock().setData(new byte[getDblock().getNpts()*
					getDblock().getPtsize()]);
	    disI.read((byte[]) getDblock().getData().firstElement());
	}
    }

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
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2001  INB	Created.
     *
     */
    public synchronized void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().register(rmapI);
    }

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
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  INB	Created.
     *
     */
    public final void reset()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isSynchronized()) {
	    synchronizeWserver();
	}
	getACO().reset();
    }

    /**
     * Sets the desired number of frames allowed in the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a desired number
     * of frames.
     * <p>
     * If the archive mode is currently <code>ACCESS_NONE</code> or
     * <code>ACCESS_LOAD</code>, this method sets it to
     * <code>ACCESS_CREATE</code>. Subsequent calls to <code>setAmode</code>
     * override that.
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
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setAframes(long archiveFramesI) {
	if (archiveFramesI < -1) {
	    throw new IllegalArgumentException
		("Negative archive frame limit of " + archiveFramesI);
	}

	archiveFrames = archiveFramesI;
	if ((archiveFrames == 0) && (getAmode() == ACCESS_CREATE)) {
	    setAmode(ACCESS_NONE);
	} else if ((archiveFrames != 0) &&
		   (getAmode() == ACCESS_NONE)) {
	    setAmode(ACCESS_CREATE);
	}
    }

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
    public final void setAkeep(boolean keepI) {
	archiveKeep = keepI;
    }

    /**
     * Sets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param modeI  the archive access mode.
     * @see com.rbnb.api.Source#ACCESS_APPEND
     * @see com.rbnb.api.Source#ACCESS_CREATE
     * @see com.rbnb.api.Source#ACCESS_LOAD
     * @see com.rbnb.api.Source#ACCESS_NONE
     * @see #getAmode()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2001  INB	Created.
     *
     */
    public final void setAmode(byte modeI) {
	accessMode = modeI;
    }

    /**
     * Sets the desired amount of memory usage allowed for the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a desired amount
     * of memory.
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
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setAsize(long archiveSizeI) {
	if (true) {
	    throw new java.lang.IllegalStateException
		("Setting the archive size in bytes is not yet supported.");
	}

	if (archiveSizeI < -1) {
	    throw new IllegalArgumentException
		("Negative archive frame limit of " + archiveSizeI);
	}

	archiveSize = archiveSizeI;
    }

    /**
     * Sets the desired number of frames allowed in the cache.
     * <p>
     * A value of -1 means that the cache is not limited to a desired number of
     * frames.
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
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setCframes(long cacheFramesI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (cacheFramesI < -1) {
	    throw new IllegalArgumentException
		("Negative cache frame limit of " + cacheFramesI);
	}

	cacheFrames = cacheFramesI;
    }

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
    public final void setCkeep(boolean keepI) {
	cacheKeep = keepI;
    }

    /**
     * Sets the desired amount of memory usage allowed for the cache.
     * <p>
     * A value of -1 means that the cache is not limited to a desired amount of
     * memory.
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
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2000  INB	Created.
     *
     */
    public void setCsize(long cacheSizeI) {
	if (true) {
	    throw new java.lang.IllegalStateException
		("Setting the cache size in bytes is not yet supported.");
	}

	if (cacheSizeI < -1) {
	    throw new IllegalArgumentException
		("Negative cache frame limit of " + cacheSizeI);

	}

	cacheSize = cacheSizeI;
    }

    /**
     * Sets the number of cache <code>FrameSets</code>.
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
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2001  INB	Created.
     *
     */
    public final void setNfs(int frameSetsI) {
	if (frameSetsI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The number of cache framesets must be greater than 0.");
	}

	frameSets = frameSetsI;
    }

    /**
     * Synchronizes with the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the
     *		  <code>Serialization</code>.
     * @exception java.io.IOException
     *		  thrown if there is a I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>SourceHandle</code> has not been connected
     *		      to a server,</li>
     *		  <li>the <code>SourceHandle</code> is not running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the synchronization is interrupted.
     * @since V2.0
     * @version 11/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/22/2002  INB	Created.
     *
     */
    public synchronized void synchronizeWserver()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super.synchronizeWserver();
	needSynchronization = false;
    }

    /**
     * Writes this <code>SourceHandle</code> to the specified stream.
     * <p>
     * This method writes out differences between this <code>Source</code> and
     * the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>Source</code> as an
     *			   <code>Rmap</code>.
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.Rmap,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 07/30/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    void write(Rmap otherI,
	       String[] parametersI,
	       int parameterI,
	       OutputStream osI,
	       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	SourceIO.write(parametersI,
		       parameterI,
		       osI,
		       dosI,
		       this,
		       (SourceInterface) otherI);
    }
}
