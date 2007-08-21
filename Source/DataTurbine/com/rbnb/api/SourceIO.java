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
 * Object that represents a client application source connection to an RBNB
 * server.
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
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/25/2005  EMF      Added bytesTransferred method; does nothing.
 * 01/08/2004  INB	Added <code>clearCache</code> method.
 * 11/07/2003  INB	Eliminated <code>isImplemented</code> - it isn't
 *			necessary in this object.
 * 07/30/2003  INB	Added support for <code>deleteChannels<code>.
 * 04/17/2003  INB	Added <code>tryReconnect</code> method.
 * 05/10/2001  INB	Created.
 *
 */
class SourceIO
    extends com.rbnb.api.ClientIO
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

    // Package constants:
    final static byte PAR_AFM = 0,
		      PAR_ASZ = 1,
		      PAR_CFM = 2,
		      PAR_CSZ = 3,
		      PAR_MOD = 4,
		      PAR_SET = 5,
		      PAR_AKP = 6,
		      PAR_CKP = 7;

    final static String[] PARAMETERS = {
			    "AFM",
			    "ASZ",
			    "CFM",
			    "CSZ",
			    "MOD",
			    "SET",
			    "AKP",
			    "CKP"
			};

    // Private class fields:
    private static String[] ALL_PARAMETERS = null;

    private static int parametersStart = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    SourceIO() {
	super();
    }

    /**
     * Class constructor to build a <code>SourceIO</code> by reading it in.
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
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    SourceIO(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>SourceIO</code> by reading it in.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>SourceIO</code> as an <code>Rmap</code>.
     * @param isI    the <code>InputStream</code>.
     * @param disI   the <code>DataInputStream</code>.
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
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  INB	Created.
     *
     */
    SourceIO(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(otherI,isI,disI);
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
      return 0L;
    }

    /**
    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This static method provides a single method for handling
     * <code>SourceInterface</code> implementations. It adds:
     * <p><ul>
     * <li>the desired number of cache frames,</li>
     * <li>the desired maximum size of the cache in bytes,</li>
     * <li>the desired number of cache <code>FrameSets</code>,</li>
     * <li>the archive access mode,</li>
     * <li>the desired maximum number of archive frames, and</li>
     * <li>the desired maximum size of the archive in bytes.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param sourceI the <code>SourceInterface</code>.
     * @return the additional information.
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/25/2001  INB	Created.
     *
     */
    final static String additionalToString(SourceInterface sourceI) {
	String stringR = ClientIO.additionalToString
	    ((ClientInterface) sourceI);
	try {
	    stringR += (" Cache: Frames: " + sourceI.getCframes() +
			" Keep: " + sourceI.getCkeep() +
			" Bytes: " + sourceI.getCsize() +
			" Sets: " + sourceI.getNfs());

	    if (sourceI.getAmode() == ACCESS_LOAD) {
		stringR = " Load Archive";
	    } else if (sourceI.getAmode() != ACCESS_NONE) {
		stringR += (" Archive: Mode: " +
			    ((sourceI.getAmode() == ACCESS_APPEND) ?
			     "Append" :
			     "Create") +
			    " Keep: " + sourceI.getAkeep() +
			    " Frames: " + sourceI.getAframes() +
			    " Bytes: " + sourceI.getAsize());
	    }
	} catch (java.lang.Exception e) {
	}

	return (stringR);
    }

    /**
     * Adds the <code>SourceIO's</code> parameters to the full serialization
     * parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 12/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/01/2001  INB	Created.
     *
     */
    static synchronized String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = ClientIO.addToParameters(null);
	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}
	return (addToParameters(parametersR,PARAMETERS));
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
    public void clearCache()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
    }

    /**
     * Copies some standard fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Rmap</code> to copy.
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/18/2002  INB	Created.
     *
     */
    final void copyFields(Rmap clientI) {
	SourceInterface source = (SourceInterface) clientI;

	try {
	    setCframes(source.getCframes());
	    setCkeep(source.getCkeep());
	    setCsize(source.getCsize());
	    setNfs(source.getNfs());
	    setAmode(source.getAmode());
	    setAkeep(source.getAkeep());
	    setAframes(source.getAframes());
	    setAsize(source.getAsize());
	} catch (java.lang.Exception e) {
	    throw new java.lang.InternalError();
	}
	super.copyFields(clientI);
    }

    /**
     * Defaults for all parameters.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code> into
     * this one. It is designed to be overridden by higher level objects to
     * ensure that they handle all of their parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param seenI  the fields that we've seen already.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2002  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultSourceParameters(this,(SourceInterface) otherI,seenI);
    }

    /**
     * Defaults for source parameters.
     * <p>
     * This method copies unread fields from the other
     * <code>SourceInterface</code> into the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param ciIO   the <code>SourceInterface</code>.
     * @param otherI the other <code>SourceInterface</code>.
     * @param seenI  the fields that we've seen already.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    final static void defaultSourceParameters(SourceInterface siIO,
					      SourceInterface otherI,
					      boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (otherI != null) {
	    if ((seenI == null) || !seenI[parametersStart + PAR_AFM]) {
		siIO.setAframes(otherI.getAframes());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_ASZ]) {
		siIO.setAsize(otherI.getAsize());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_CFM]) {
		siIO.setCframes(otherI.getAframes());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_CSZ]) {
		siIO.setCsize(otherI.getCsize());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_MOD]) {
		siIO.setAmode(otherI.getAmode());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_SET]) {
		siIO.setNfs(otherI.getNfs());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_AKP]) {
		siIO.setAkeep(otherI.getAkeep());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_CKP]) {
		siIO.setCkeep(otherI.getCkeep());
	    }

	    defaultClientParameters((ClientInterface) siIO,
				    (ClientInterface) otherI,
				    seenI);
	}
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
    public Rmap deleteChannels(Rmap channelsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (channelsI);
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
    public boolean getAkeep() {
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
    public boolean getCkeep() {
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
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Reads the <code>SourceIO</code> from the specified input stream.
     * <p>
     * This method fills in missing fields from the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>SourceInterface</code> as an
     *		     <code>Rmap</code>.
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
     * @version 08/15/2001
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
	read(isI,disI,this,(SourceInterface) otherI);
    }

    /**
     * Reads the <code>SourceInterface</code> from the specified input stream.
     * <p>
     * This method fills in missing fields from the input
     * <code>Rmap/code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @param siIO   the <code>SourceInterface</code>.
     * @param otherI the other <code>SourceInterface</code>.
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
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream,com.rbnb.api.SourceInterface,SourceInterface)
     * @since V2.0
     * @version 10/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    static void read(InputStream isI,
		     DataInputStream disI,
		     SourceInterface siIO,
		     SourceInterface otherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Rmap</code>.
	Serialize.readOpenBracket(isI);

	// Initialize the full parameter list.
	initializeParameters();

	boolean[] seen = new boolean[ALL_PARAMETERS.length];
	int parameter;
	while ((parameter = Serialize.readParameter(ALL_PARAMETERS,
						    isI)) != -1) {
	    seen[parameter] = true;

	    // Read parameters until we see a closing bracket.
	    if (!readSourceParameter(parameter,isI,disI,siIO)) {
		if (!readClientParameter(parameter,
					 isI,
					 disI,
					 (ClientInterface) siIO)) {
		    ((Rmap) siIO).readStandardParameter((Rmap) otherI,
							parameter,
							isI,
							disI);
		}
	    }
	}

	defaultSourceParameters(siIO,otherI,seen);
    }

    /**
     * Reads <code>SourceInterface</code> parameters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI the parameter index.
     * @param isI	 the input stream.
     * @param disI	 the data input stream.
     * @param siIO	 the <code>SourceInterface</code>.
     * @return was the parameter recognized?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeSourceParameters(com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream,com.rbnb.api.SourceInterface,com.rbnb.api.SourceInterface)
     * @since V2.0
     * @version 10/08/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    static final boolean readSourceParameter(int parameterI,
					     InputStream isI,
					     DataInputStream disI,
					     SourceInterface siIO)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean successR = false;

	if (parameterI >= parametersStart) {
	    successR = true;
	    switch (parameterI - parametersStart) {
	    case PAR_AFM:
		siIO.setAframes(isI.readLong());
		break;

	    case PAR_ASZ:
		siIO.setAsize(isI.readLong());
		break;

	    case PAR_CFM:
		siIO.setCframes(isI.readLong());
		break;

	    case PAR_CSZ:
		siIO.setCsize(isI.readLong());
		break;

	    case PAR_MOD:
		siIO.setAmode(isI.readByte());
		break;

	    case PAR_SET:
		siIO.setNfs(isI.readInt());
		break;

	    case PAR_AKP:
		siIO.setAkeep(isI.readBoolean());
		break;

	    case PAR_CKP:
		siIO.setCkeep(isI.readBoolean());
		break;

	    default:
		successR = false;
		break;
	    }
	}

	return (successR);
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
    public void register(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
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
    public void reset()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
    }

    /**
     * Sets the desired number of frames allowed in the archive.
     * <p>
     * A value of -1 means that the archive is not limited to a desired number
     * of frames.
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
    public void setAframes(long archiveFramesI) {
	if (archiveFramesI < -1) {
	    throw new IllegalArgumentException
		("Negative archive frame limit of " + archiveFramesI);
	}

	archiveFrames = archiveFramesI;
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
    public void setAkeep(boolean keepI) {
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
    public void setAsize(long archiveSizeI) {
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
    public void setCkeep(boolean keepI) {
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
    public void setCsize(long cacheSizeI) {
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
     * Writes this <code>SourceIO</code> to the specified stream.
     * <p>
     * This method writes out differences between this <code>SourceIO</code>
     * and the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>SourceInterface</code> as an
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
	write(parametersI,parameterI,osI,dosI,this,(SourceInterface) otherI);
    }

    /**
     * Writes the <code>SourceInterface</code> to the specified stream.
     * <p>
     * This method writes out differences between the two
     * <code>SourceInterfaces</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI our parent's parameter list.
     * @param paramterI	  the parent parameter to use.
     * @param osI	  the output stream.
     * @param dosI	  the data output stream.
     * @param siI	  the <code>SourceInterface</code>.
     * @param otherI	  the other <code>SourceInterface</code>.
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
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream,com.rbnb.api.SourceInterface,com.rbnb.api.SourceInterface)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    static void write(String[] parametersI,
		      int parameterI,
		      OutputStream osI,
		      DataOutputStream dosI,
		      SourceInterface siI,
		      SourceInterface otherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Write out the object.
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged((Serializable) siI,parametersI,parameterI);

	((Rmap) siI).writeStandardParameters((Rmap) otherI,osI,dosI);
	ClientIO.writeClientParameters(osI,
				       dosI,
				       (ClientInterface) siI,
				       (ClientInterface) otherI);
	writeSourceParameters(osI,dosI,siI,otherI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes out <code>SourceInterface</code> parameters.
     * <p>
     * This method writes out differences between the two input
     * <code>SourceInterfaces</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI    the output stream.
     * @param dosI   the data output stream.
     * @param siI  the <code>SourceInterface</code>.
     * @param otherI the other <code>SourceInterface</code>.
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
     * @see #readSourceParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream,com.rbnb.api.SourceInterface)
     * @since V2.0
     * @version 10/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    static final void writeSourceParameters(OutputStream osI,
					    DataOutputStream dosI,
					    SourceInterface siI,
					    SourceInterface otherI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	if (((otherI == null) && (siI.getAframes() != -1)) ||
	    ((otherI != null) && (siI.getAframes() != otherI.getAframes()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_AFM);
	    osI.writeLong(siI.getAframes());
	}

	if (((otherI == null) && (siI.getAsize() != -1)) ||
	    ((otherI != null) && (siI.getAsize() != otherI.getAsize()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_ASZ);
	    osI.writeLong(siI.getAsize());
	}

	if (((otherI == null) && (siI.getCframes() != -1)) ||
	    ((otherI != null) && (siI.getCframes() != otherI.getCframes()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_CFM);
	    osI.writeLong(siI.getCframes());
	}

	if (((otherI == null) && (siI.getCsize() != -1)) ||
	    ((otherI != null) && (siI.getCsize() != otherI.getCsize()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_CSZ);
	    osI.writeLong(siI.getCsize());
	}

	if (((otherI == null) && (siI.getAmode() != Source.ACCESS_NONE)) ||
	    ((otherI != null) && (siI.getAmode() != otherI.getAmode()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_MOD);
	    osI.writeByte(siI.getAmode());
	}

	if (((otherI == null) && (siI.getNfs() != -1)) ||
	    ((otherI != null) && (siI.getNfs() != otherI.getNfs()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_SET);
	    osI.writeInt(siI.getNfs());
	}

	if (((otherI == null) && !siI.getAkeep()) ||
	    ((otherI != null) && (siI.getAkeep() != otherI.getAkeep()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_AKP);
	    osI.writeBoolean(siI.getAkeep());
	}

	if (((otherI == null) && siI.getCkeep()) ||
	    ((otherI != null) && (siI.getCkeep() != otherI.getCkeep()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_CKP);
	    osI.writeBoolean(siI.getCkeep());
	}
    }

    /**
     * Try to reconnect to an existing <code>SourceHandler</code>?
     * <p>
     * If this method returns <code>true</code>, then the server will look for
     * an existing <code>SourceHandler</code> when the client represented by
     * the <code>SourceIO</code> logs in. If a match is found, then the
     * server will try to reconnect to that existing handler rather than start
     * a new one.
     * <p>
     *
     * @author Ian Brown
     *
     * @return try to reconnect? <code>true</code> if an archive is to be
     *	       loaded or appended to.
     * @since V2.1
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2003  INB	Created.
     *
     */
    public final boolean tryReconnect() {
	return ((getAmode() == ACCESS_LOAD) ||
		(getAmode() == ACCESS_APPEND));
    }
}
