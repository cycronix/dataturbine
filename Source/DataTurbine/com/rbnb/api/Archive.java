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
 * Provides long-term (disk file) storage for an <code>SourceHandler</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 10/02/2006
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/18/2010  MJm		Explicit buffer size in BufferedReader
 * 11/16/2006  EMF      Fixed archive read and recover.
 * 10/11/2006  EMF      Added trim by time arguments to constructor.
 * 12/21/2004  MJM      Added null pointer check in archive recovery logic
 * 04/14/2004  INB	Added <code>moveDownFrom</code>.
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 10/31/2003  INB	Build a recovery message buffers rather than report to
 *			the log.
 * 10/27/2003  INB	Ensure that we close all files.
 * 10/23/2003  INB	Don't close the <code>FileSet</code> files in
 *			<code>writeToArchive</code>.
 * 09/10/2003  INB	Handle <code>null</code> frame range when recovering
 *			<code>FileSets</code>.
 * 09/09/2003  INB	Reordered the archive writes so that data is always
 *			written before the headers.  Allow recovery of archive
 *			from the basic data file pairs.
 * 07/30/2003  INB	Added <code>deleteArchive</code> method to allow just
 *			this archive to be deleted.  Added
 *			<code>nullify</code>.
 * 05/19/2003  INB	Use the largest rather than last sizes.
 * 04/30/2003  INB	Seal filesets if necesary when recovering them.
 * 04/24/2003  INB	Added <code>markOutOfDate</code>.
 * 03/10/2003  INB	Moved <code>listFiles</code> to separate
 *			<code>Directory</code> class.
 * 02/18/2003  INB	Modified to fit into the new multiple
 * 			<code>RingBUffers</code> per <code>RBO</code>
 *			structure. Each <code>RingBuffer</code> has a separate
 *			<code>Archive</code>.
 * 02/09/2001  INB	Created.
 *
 */
final class Archive
    extends com.rbnb.api.StorageManager
{
    /**
     * cached <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/01/2001
     */
    private FrameSet cachedSet = null;

    /**
     * oldest <code>FileSet</code> still in the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #newestFS
     * @since V2.0
     * @version 05/10/2001
     */
    private long newestFS = -1;

    /**
     * oldest <code>FileSet</code> still in the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #newestFS
     * @since V2.0
     * @version 05/10/2001
     */
    private long oldestFS = -1;

    /**
     * is <code>Archive</code> known to be out-of-date?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */
    private boolean outOfDate = true;

    // Package constants:
    final static String SEPARATOR =
	System.getProperty("file.separator");

    final static boolean BINARYARCHIVE = true;
    final static boolean TEXTARCHIVE = !BINARYARCHIVE;

    final static String[] ARCHIVE_PARAMETERS = {
				    "REG",
				    "EXP"
				};

    final static int ARC_REG = 0;
    final static int ARC_EXP = 1;

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
     * 02/23/2001  INB	Created.
     *
     */
    Archive(float flush, float trim)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(flush,trim); //set flush,trim intervals in seconds
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
    }

    /**
     * Deletes the <code>Archive</code> files in their entirety.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem performing the delete.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 07/30/2003  INB	Re-created from the <code>RBO.deleteArchive</code>
     *			method.
     *
     */
    final void deleteArchive()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	// Lock the door.
	try {
	    getDoor().lock("Archive.deleteArchive");

	    // Create a <code>File</code> object for the archive directory.
	    java.io.File aDir = new java.io.File(getArchiveDirectory());
	    Directory asDirectory = new Directory(aDir);

	    // Delete the files in the archive.
	    java.io.File[] files = asDirectory.listFiles(),
		nFiles = null;
	    
	    // This code performs a recursive removal without using a recursive
	    // method.
	    java.util.Vector levels = new java.util.Vector();
	    levels.addElement(files);
	    levels.addElement(new Integer(0));
	    int filesIdx = 0,
		indexIdx = 1;

	    while (levels.size() > 0) {
		files = (java.io.File []) levels.elementAt(filesIdx);
		int idx = ((Integer) levels.elementAt(indexIdx)).intValue();

		for (; (files != null) && (idx < files.length); ++idx) {
		    Directory fAsDirectory = new Directory(files[idx]);
		    nFiles = fAsDirectory.listFiles();
		    if ((nFiles != null) && (nFiles.length != 0)) {
			// Ensure that directories are empty before we get rid
			// of them.
			break;
		    }

		    files[idx].delete();
		    if (files[idx].exists()) {
		    	throw new java.io.IOException
		    	    ("Failed to delete " + files[idx] +
		    	     " from old archive.");
		    }
		}

		if ((files != null) && (idx < files.length)) {
		    // If we broke out of the loop, then we need to move down a
		    // level.
		    levels.setElementAt(new Integer(idx),indexIdx);
		    levels.addElement(nFiles);
		    levels.addElement(new Integer(0));
		    filesIdx += 2;
		    indexIdx += 2;

		} else {
		    // If we reached the end of the loop, then we need to move
		    // up a level.
		    levels.removeElementAt(indexIdx);
		    levels.removeElementAt(filesIdx);
		    indexIdx -= 2;
		    filesIdx -= 2;
		}
	    }

	    // Delete the directory.
	    aDir.delete();
	    if (aDir.exists()) {
		throw new java.io.IOException
		    ("Failed to delete directory " + aDir +
		     " from old archive.");
	    }

	} finally {
	    // OK, we can unlock the door now.
	    getDoor().unlock();
	}
    }

    /**
     * Gets the <code>Archive</code> directory.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Archive</code> directory.
     * @since V2.1
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Created.
     *
     */
    final String getArchiveDirectory() {
	return (((RingBuffer) getParent()).getArchiveDirectory());
    }

    /**
     * Gets the cached <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cached <code>FrameSet</code>.
     * @see #setCachedSet(com.rbnb.api.FrameSet)
     * @since V2.0
     * @version 10/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2001  INB	Created.
     *
     */
    final FrameSet getCachedSet() {
	return (cachedSet);
    }

    /**
     * Gets the log class mask for this <code>Archive</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final long getLogClass() {
	return (Log.CLASS_ARCHIVE);
    }

    /**
     * Gets the base log level for this <code>Archive</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final byte getLogLevel() {
	return (Log.STANDARD);
    }

    /**
     * Gets the index of the newest <code>FileSet</code> still in the
     * <code.Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the newest <code>FileSet</code>.
     * @see #setNewest(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    private final long getNewest() {
	return (newestFS);
    }

    /**
     * Gets the index of the oldest <code>FileSet</code> still in the
     * <code.Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the oldest <code>FileSet</code>.
     * @see #setOldest(long)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    private final long getOldest() {
	return (oldestFS);
    }

    /**
     * Marks this <code>Archive</code> and its parent <code>RingBuffer</code>
     * as out-of-date.
     * <p>
     * Deletes the <code>Archive</code> summary and <code>Seal</code> files.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2003  INB	Created.
     *
     */
    final synchronized void markOutOfDate() {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	if (!outOfDate) {
	    outOfDate = true;

	    ((RingBuffer) getParent()).markOutOfDate();

	    String directory = getArchiveDirectory() + SEPARATOR;
	    java.io.File file = new java.io.File(directory + "summary.rbn");
	    if (file.exists()) {
		file.delete();
	    }
	    file = new java.io.File(directory + "seal.rbn");
	    if (file.exists()) {
		file.delete();
	    }
	}
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy in response to a
     * request <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI    the <code>RmapExtractor</code> performing the
     *			    extraction.
     * @param unsatisfiedI  the unsatisfied <code>ExtractedChain</code>.
     * @param unsatsifiedO  the new list of unsatisfied
     *			    <code>ExtractedChains</code>.
     * @param the reason for a failed match.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 12/08/2006
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2006  JPW  For a Subscription when we are reframing, this method
     *                  was returning MATCH_AFTER, which will kill the
     *                  Subscription.  Return MATCH_UNKNOWN instead.
     * 04/14/2004  INB	Created.
     *
     */
    final byte moveDownFrom(RmapExtractor extractorI,
			    ExtractedChain unsatisfiedI,
			    java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	byte reasonR = MATCH_UNKNOWN;

	// mjm 1/12/06 null ptr checks
	if ((System.getProperty("noreframe") == null)                     &&
	    (unsatisfiedI!=null) && (unsatisfiedI.getInherited() != null) &&
	    (unsatisfiedI.getInherited().getFrange() != null)) {
	    // If we're compressed (reframing) <code>FrameSets</code>, then
	    // frame based requests do not work.
	    // EMF, JPW 12/8/2006
	    //     Returning MATCH_AFTER will kill the Subscription.
	    //     Return MATCH_UNKNOWN instead.
	    // reasonR = MATCH_AFTER;
	    reasonR = MATCH_UNKNOWN;
	} else {
	    // Otherwise, try and use our superclass method.
	    reasonR = super.moveDownFrom(extractorI,unsatisfiedI,unsatisfiedO);
	}
	
	// System.err.println("Archive.moveDownFrom(): returning " + reasonR);
	
	return (reasonR);
    }

    /**
     * Nullifies this <code>Archive</code>.
     * <p>
     * This method ensures that all pointers in this <code>DataBlock</code>
     * are cleared, reducing the effort needed by the garbage collector to
     * clean it up.
     * <p>
     *
     * @author Ian Brown
     *
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
    public final void nullify() {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	super.nullify();

	if (getCachedSet() != null) {
	    getCachedSet().nullify();
	    setCachedSet(null);
	}
    }

    /**
     * Loads the parent <code>RingBuffer</code> (<code>Cache</code> and this
     * <code>Archive</code>) with information from the archive files.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with addressing.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #writeToArchive()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 04/24/2003  INB	Mark <code>Archive</code> as not out-of-date.
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure. This is no longer the top of the heirarchy,
     *			and we no longer validate here.
     * 03/14/2001  INB	Created.
     *
     */
    final void readFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the door.
	    getDoor().setIdentification(getFullName() + "/" + getClass());
	    getDoor().lock("Archive.readFromArchive");

	    // Read the <code>Archive</code> from the files, starting with the
	    // basic information.
	    readOffsetsFromArchive();

	    // Read a skeleton of the data from the archive.
	    readSkeletonFromArchive();

	    // Load the <code>FileSets</code> from the archive.
	    FileSet lastFS = null;
	    for (long idx = getOldest(),
		     endIdx = getNewest();
		 idx <= endIdx;
		 ++idx) {
		java.io.File fileSetDirectory = new java.io.File
		    (getArchiveDirectory() +
		     Archive.SEPARATOR +
		     "FS" + idx);
		if (fileSetDirectory.exists()) {
		    FileSet fs = new FileSet(idx);
		    addChild(fs);
		    fs.readFromArchive();
		    lastFS = fs;
		}
	    }

	    // Update the next frame, frameset, and fileset indexes.
	    if (lastFS != null) {
		setNextIndex(lastFS.getIndex() + 1);
	    }

	    // Mark as not out-of-date.
	    outOfDate = false;

	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }

    /**
     * Reads the basic information describing what is in the archive files.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #writeToArchive()
     * @since V2.0
     * @version 04/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure. Our parent is now a
     *			<code>RingBuffer</code>.
     * 03/14/2001  INB	Created.
     *
     */
    final void readOffsetsFromArchive()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	String directory = getArchiveDirectory() + SEPARATOR;
	java.io.FileInputStream fis = new java.io.FileInputStream
	    (directory + "summary.rbn");
	java.io.BufferedReader br = new java.io.BufferedReader
	    (new java.io.InputStreamReader(fis),8192);	// MJM explicit size

	String line;
	while ((line = br.readLine()) != null) {
	    if (line.indexOf("Date: ") == 0) {
		// Skip the date.
	    } else if (line.indexOf("Oldest FileSet: ") == 0) {
		// Grab the oldest <code>FileSet</code> index.
		setOldest(Long.parseLong(line.substring(16)));
	    } else if (line.indexOf("Newest FileSet: ") == 0) {
		// Grab the newest <code>FileSet</code> index.
		setNewest(Long.parseLong(line.substring(16)));
	    } else {
		StorageManager entry;
		int idx;

		if (line.indexOf("Cache ") == 0) {
		    entry = ((RingBuffer) getParent()).getCache();
		    idx = 6;
		} else if (line.indexOf("Archive ") == 0) {
		    entry = this;
		    idx = 8;
		} else {
		    throw new com.rbnb.api.SerializeException
			("Unrecognized line read from archive summary: " +
			 line);
		}

		if (line.indexOf("Sets: ",idx) == idx) {
		    idx += 6;
		    entry.setMs(Integer.parseInt(line.substring(idx)));
		} else if (line.indexOf("Elements/Set: ",idx) == idx) {
		    idx += 14;
		    entry.setMeps(Integer.parseInt(line.substring(idx)));
		} else if (line.indexOf("Memory/Set: ",idx) == idx) {
		    idx += 12;
		    entry.setMmps(Integer.parseInt(line.substring(idx)));
		} else {
		    throw new com.rbnb.api.SerializeException
			("Unrecognized line read from archive summary: " +
			 line);
		}
	    }
	}

	br.close();
    }

    /**
     * Reads a skeleton of the <code>Archive</code> from the archive files.
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
     * @see #readSkeletonFromArchive()
     * @see #writeToArchive()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure.
     * 03/14/2001  INB	Created.
     *
     */
    final void readSkeletonFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	try {
	    // Lock the door.
	    getDoor().lock("Archive.readSkeletonFromArchive");

	    // Open the <code>Registration</code> files for the
	    // <code>Archive</code> as a whole.
	    String directory = getArchiveDirectory() + SEPARATOR;
	    java.io.FileInputStream hfis =
		new java.io.FileInputStream(directory + "reghdr.rbn"),
				    dfis =
		new java.io.FileInputStream(directory + "regdat.rbn");
	    InputStream his = new InputStream(hfis,BINARYARCHIVE,0);
	    DataInputStream dis = new DataInputStream(dfis,0);

	    // Read the <code>Registration</code>.
	    if (his.readParameter(ARCHIVE_PARAMETERS) == ARC_REG) {
		setRegistered(new Registration(his,dis));
		getRegistered().readData(dis);
	    }
	    try {
		if (his.readParameter(ARCHIVE_PARAMETERS) == ARC_EXP) {
		    Rmap rmap = new Rmap(his,dis);
		    rmap.readData(dis);
		    ((SourceHandler) getParent().getParent()).register(rmap);
		}
	    } catch (java.io.EOFException e) {
	    }
	    setLastRegistration(System.currentTimeMillis());

	    // Close the registration files; they won't be needed again.
	    his.close();
	    hfis.close();
	    dis.close();
	    dfis.close();
	    
	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }

    /**
     * Rebuild an invalid <code>Archive</code> from the last good sequence of
     * <code>Archive</code> <code>FileSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param validSealsI   the valid <code>FileSet Seals</code>.
     * @param invalidSealsI the invalid <code>FileSet Seals</code>.
     * @param goodMessageO  the message buffer for good messages.
     * @param notMessageO   the message buffer for not recovered messages.
     * @param unMessageO    the message buffer for unrecoverable messages.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is just no way to recover anything from the
     *		  <code>Archive</code>.
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/31/2003  INB	Build a recovery message buffers rather than report to
     *			the log.
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure. Renamed <code>recoverFromArchive</code>.
     * 06/04/2001  INB	Created.
     *
     */
    final void recoverFromArchive(java.util.Vector validSealsI,
				  java.util.Vector invalidSealsI,
				  StringBuffer goodMessageO,
				  StringBuffer notMessageO,
				  StringBuffer unMessageO)
	throws java.lang.IllegalStateException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	if ((validSealsI.size() == 0) && (invalidSealsI.size() == 0)) {
	    throw new java.lang.IllegalStateException
		("No filesets were found in the archive.");
	}

	// Attempt to read whatever <code>FileSets</code> we can get.
	try {
	    // Start by trying to recover the offset information, which
	    // includes the sizes of things.
	    readOffsetsFromArchive();
	} catch (java.lang.Exception e) {
	    ((RingBuffer) getParent()).getCache().setMs(0);
	    ((RingBuffer) getParent()).getCache().setMeps(0);
	    setMs(0);
	    setMeps(0);
	}
	setOldest(-1);
	setNewest(-1);
	recoverFileSets(validSealsI,
			invalidSealsI,
			goodMessageO,
			notMessageO,
			unMessageO);

	// Update the registration from the result.
	setRegistered(new Registration());
	setLastRegistration(Long.MIN_VALUE);
	setAddedSets(0);
    }

    /**
     * Recovers any <code>FileSets</code> that can be made valid in some way or
     * another.
     * <p>
     *
     * @author Ian Brown
     *
     * @param validSealsI   the list of definitely valid <code>Seals</code>.
     * @param invalidSealsI the list of <code>Seals</code> with problems.
     * @param goodMessageO  the message buffer for good messages.
     * @param notMessageO   the message buffer for not recovered messages.
     * @param unMessageO    the message buffer for unrecoverable messages.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is just no way to recover anything from the
     *		  <code>Archive</code>.
     * @since V2.0
     * @version 09/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/16/2006  EMF  Fixed routine, so all FileSets recovered.
     *                  Force deep recovery if seal is invalid.
     * 10/31/2003  INB	Build a recovery message buffers rather than report to
     *			the log.
     * 09/10/2003  INB	Allow for a <code>null</code> frame range in the
     *			the <code>FrameSets</code>.
     * 09/09/2003  INB	Allow for recovery of a <code>FileSet</code> from its
     *			data file pair.
     * 05/19/2003  INB	Use the largest rather than last sizes.
     * 04/30/2003  INB	Seal filesets as needed.
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure.
     * 06/04/2001  INB	Created.
     *
     */
    private final void recoverFileSets(java.util.Vector validSealsI,
				       java.util.Vector invalidSealsI,
				       StringBuffer goodMessageO,
				       StringBuffer notMessageO,
				       StringBuffer unMessageO)
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	boolean useValid = false,
		notrecovered = false,
	        unrecoverable = false;
	int lmeps = 0,
	    ceps = 0;
	java.io.File lastFile = null,
		     validFile = null,
		     invalidFile = null,
		     unrecoverableDirectory = new java.io.File
			 (getArchiveDirectory() +
			  Archive.SEPARATOR +
			  "UNRECOVERABLE"),
		     notrecoveredDirectory = new java.io.File
			 (getArchiveDirectory() +
			  Archive.SEPARATOR +
			  "NOTRECOVERED");
	long validFS = Long.MAX_VALUE,
	    invalidFS = Long.MAX_VALUE,
	    oldestFS = Long.MIN_VALUE,
	    newestFS = Long.MIN_VALUE;
	Seal lastSeal = null,
	     validSeal = null,
	     invalidSeal = null;

	goodMessageO.append("\n\t\tFileSets recovered:");
	FileSet previousFS = null,
		lastFS = null;
        //EMF 11/16/06: Seal dates must be chronological, but a recovered
        //              FileSet gets a new Seal with current date; hence
        //              once recover a FileSet, must recover all the later
        //              ones.  Note that there may be a bug if FileSet 
        //              numbers are changed to be in a different order than
        //              the timestamps
        boolean forceDeep=false;
        //EMF 11/16/06: added check for nonnull Files, so won't skip last one
	for (int idx = 0,
		 endIdx = validSealsI.size(),
		 idx1 = 0,
		 endIdx1 = invalidSealsI.size();
	     (idx < endIdx) || (idx1 < endIdx1) || validFile!=null || invalidFile!=null;
	     ) {
	    // Read through the <code>Seals</code> to produce a sequence of
	    // valid <code>FileSets</code>.
	    java.io.File fileSetDirectory;
	    Seal theSeal;
	    long fsIndex;

	    // Get the next <code>FileSet</code> and its <code>Seal</code>.
	    if ((validFile == null) && (idx < endIdx)) {
		validFile = (java.io.File) validSealsI.elementAt(idx++);
		validSeal = (Seal) validSealsI.elementAt(idx++);
		String name = validFile.getName();
		validFS = Long.parseLong(name.substring(2));
	    }
	    if ((invalidFile == null) && (idx1 < endIdx1)) {
		invalidFile = (java.io.File) invalidSealsI.elementAt(idx1++);
		invalidSeal =
		    ((com.rbnb.api.InvalidSealException)
		     invalidSealsI.elementAt(idx1++)).getInvalid();
		String name = invalidFile.getName();
		invalidFS = Long.parseLong(name.substring(2));
	    }

	    if (validFS < invalidFS) {
		// If the <code>FileSet</code> from the valid list comes before
		// the one from the invalid list, then it is the "next" one.
		fileSetDirectory = validFile;
		theSeal = validSeal;
		fsIndex = validFS;
		validFile = null;
		validFS = Long.MAX_VALUE;
	    } else {
		// If the <code>FileSet</code> from the invalid list comes
		// before the one from the valid list, then it is the "next"
		// one.
		fileSetDirectory = invalidFile;
		theSeal = invalidSeal;
		fsIndex = invalidFS;
		invalidFile = null;
		invalidFS = Long.MAX_VALUE;
	    }

	    FileSet fs = null;
	    boolean added = false;
	    try {
		// Determine if this is a usable <code>FileSet</code>.

		// Attempt to read the <code>FileSet</code> into memory.
		fs = new FileSet(fsIndex);
		addChild(fs);
		previousFS = lastFS;
		lastFS = fs;
		added = true;
		try {
                  //EMF 11/14/06: do deep recovery if seal bad, or already
                  //              done a deep recovery
                  if (forceDeep || theSeal==null || theSeal.equals(invalidSeal)) {
                    //System.err.println("invalidSeal, force recovery: "+theSeal);
                    throw new Exception();
                  } else {
                    //System.err.println("validSeal, normal read: "+theSeal);
		    fs.accessFiles(true);
		    fs.readFromArchive();
		    fs.releaseFiles();
                  }

		} catch (java.lang.Exception e) {
//EMF 7/26/07: temporary test - do not latch; recover only bad filesets not
//             all subsequent ones
                  //forceDeep=true;
                  try {
                    String dir=fs.getArchiveDirectory()+Archive.SEPARATOR;
                    System.err.println("\nRecovering "+dir);
                    //EMF 11/16/06: remove summary files, since recovery
                    //              code appends to them
                    java.io.File foo=new java.io.File(dir+"regdat.rbn");
                    if (foo.exists()) foo.delete();
                    foo=new java.io.File(dir+"reghdr.rbn");
                    if (foo.exists()) foo.delete();
                    foo=new java.io.File(dir+"offsets.rbn");
                    if (foo.exists()) foo.delete();
                    } catch (Exception ee) {
                      ee.printStackTrace();
                    }
		    // If we get here, then the only chance of recovery is to
		    // read  from the data file pair.
		    theSeal = fs.recoverFromDataFiles();
		}

		if (theSeal == null) {
		    // We were able to read the <code>Fileset</code> files, but
		    // there is no <code>Seal</code>. We can probably go ahead
		    // and make one here.
		    Seal.seal(fs.getArchiveDirectory());
		    theSeal = Seal.validate
			(fs.getArchiveDirectory(),
			 ((lastSeal == null) ?
			  Long.MIN_VALUE :
			  lastSeal.getAsOf()),
			  Long.MAX_VALUE);
		    if (theSeal == null) {
			throw new com.rbnb.api.InvalidSealException
			    (theSeal,
			     ((lastSeal == null) ?
			      Long.MIN_VALUE :
			      lastSeal.getAsOf()),
			      Long.MAX_VALUE);
		    }
		}

		if (lastFile == null) {
		    // If this is the first good <code>FileSet</code>, then
		    // grab it.
		    lastFile = fileSetDirectory;
		    lastSeal = theSeal;
		    oldestFS =
			newestFS = fsIndex;
		    goodMessageO.append("\n\t\t\t").append
			(fileSetDirectory.getName());

		} else {
		    // Otherwise, we need to ensure that it comes after the
		    // last good one.
		    try {
			theSeal.validate(lastSeal.getAsOf(),Long.MAX_VALUE);
			goodMessageO.append("\n\t\t\t").append
			    (fileSetDirectory.getName());
			newestFS = fsIndex;

		    } catch (com.rbnb.api.InvalidSealException e) {
			// If the <code>Seal</code> is invalid, then this
			// <code>FileSet</code> cannot be recovered. Attempt to
			// move it into a list of not recovered
			// <code>FileSets</code>.
			added = false;
			removeChild(fs);
			lastFS = previousFS;
			try {
			    if (!notrecovered) {
				notrecovered = true;
				notMessageO.append
				    ("\n\t\tFilesets not recovered " +
				     "(in NOTRECOVERED):");
				notrecoveredDirectory.mkdirs();
			    }
			    java.io.File newName = new java.io.File
				(notrecoveredDirectory.getAbsolutePath() +
				 Archive.SEPARATOR +
				 fileSetDirectory.getName());
			    if (fileSetDirectory.renameTo(newName)) {
				notMessageO.append
				    ("\n\t\t\t").append
				    (fileSetDirectory.getName());
			    } else {
				notMessageO.append
				    ("\n\t\t\t").append
				    (fileSetDirectory.getName()).append
				    (" rename failed.");
			    }
			} catch (java.lang.Exception e1) {
			}
		    }
		}

		if (added) {
		    // If the <code>FileSet</code> was good, then we need to
		    // figure out how many <code>FrameSets</code> per
		    // <code>FileSet</code>.
		    lmeps = Math.max(lmeps,fs.getNchildren());
		    for (int idx2 = 0; idx2 < fs.getNchildren(); ++idx2) {
			Rmap rm = fs.getChildAt(idx2);

			if (rm instanceof FrameSet) {
			    FrameSet fsFS = (FrameSet) rm;
			    //			    if (fsFS.getSummary().getFrange() == null) 
			    // 			     else 
			    // mjm null pointer exception check 12/21/04
			    if ((fsFS.getSummary()!=null) && (fsFS.getSummary().getFrange() != null)) {
				ceps = Math.max
				    (ceps,
				     (int) fsFS.getSummary
				     ().getFrange().getDuration());
			    }
			}
		    }
		}
		if (ceps == 0) {
		    ceps = 10;
		}

	    } catch (java.lang.Exception e) {
		System.err.println("Archive recovery failed... ");
		// __e.printStackTrace();
		// If we get here, then the <code>FileSet</code> is really
		// no good. Try to move it to a list of unrecoverable
		// <code>FileSets</code..
		try {
		    if (added) {
			removeChild(fs);
			lastFS = previousFS;
		    }
		    if (!unrecoverable) {
			unrecoverable = true;
			unMessageO.append
			    ("\n\t\tUnrecoverable filesets ").append
			    ("(in UNRECOVERABLE):");
			unrecoverableDirectory.mkdirs();
		    }
		    java.io.File newName = new java.io.File
			(unrecoverableDirectory.getAbsolutePath() +
			 Archive.SEPARATOR +
			 fileSetDirectory.getName());
		    if (fileSetDirectory.renameTo(newName)) {
			unMessageO.append("\n\t\t\t").append
			    (fileSetDirectory.getName());
		    } else {
			unMessageO.append
			    ("\n\t\t\t").append
			    (fileSetDirectory.getName()).append
			    (" rename failed.");
		    }
		} catch (java.lang.Exception e1) {
		}
	    }
	}

	try {
	    if (oldestFS != Long.MIN_VALUE) {
		setOldest(oldestFS);
		setNewest(newestFS);
	    } else {
		throw new java.lang.IllegalStateException
		    ("No filesets could be recovered from archive.");
	    }
	    if (getMeps() < lmeps) {
		setMeps(lmeps);
	    }
	    if (getMs() < getNchildren() + 1) {
		setMs(getNchildren() + 1);
	    }
	    Cache cache = ((RingBuffer) getParent()).getCache();
	    if (cache.getMeps() < ceps) {
		cache.setMeps(ceps);
	    }
	    if (cache.getMs() < lmeps) {
		cache.setMs(lmeps);
	    }
	    if (lastFS != null) {
		setNextIndex(lastFS.getIndex() + 1);
	    }

	} catch (java.lang.IllegalStateException e) {
	    throw e;

	} catch (java.lang.Exception e) {
	    throw new java.lang.IllegalStateException
		("Archive recovery failed.\n\t" + e.getMessage());
	}
    }

    /**
     * Sets the cached <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param setI the <code>FrameSet</code> to cache.
     * @see #getCachedSet()
     * @since V2.0
     * @version 10/01/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2001  INB	Created.
     *
     */
    final void setCachedSet(FrameSet setI) {
	cachedSet = setI;
    }

    /**
     * Sets the index of the newest <code>FileSet</code> in the
     * <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param newestFSI  the newest <code>FileSet</code> index.
     * @see #getNewest()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    private final void setNewest(long newestFSI) {
	newestFS = newestFSI;
    }

    /**
     * Sets the index of the oldest <code>FileSet</code> in the
     * <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param oldestFSI  the oldest <code>FileSet</code> index.
     * @see #getOldest()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    final void setOldest(long oldestFSI) {
	oldestFS = oldestFSI;
    }

    /**
     * Sets the current set.
     * <p>
     *
     * @author Ian Brown
     *
     * @param setI  the new set.
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
     * @see #getSet()
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2001  INB	Created.
     *
     */
    final void setSet(FrameManager setI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
// new Exception("WHF TESTING").printStackTrace();
//System.err.println("Archive::setSet(): "+setI);
	// Add the set to the <code>Archive</code>.
	super.setSet(setI);

	if (setI != null) {
	    // If there is an input <code>FrameManager</code> set, update the
	    // newest <code>FileSet</code> index for the
	    // <code>Archive</code>.
	    setNewest(setI.getIndex());

	    if (getOldest() == -1) {
		// If the oldest <code>FileSet</code> hasn't been set, then do
		// so now.
		setOldest(setI.getIndex());
	    }
	}
    }

    /**
     * Validates this <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param afterI	    the <code>Seal</code> must be after this.
     * @param beforeI	    the <code>Seal</code> must be before this.
     * @param validSealsO   the list of valid <code>FileSet Seals</code>.
     * @param invalidSealsO the list of invalid <code>FileSet Seals</code>.
     * @return was the <code>Archive</code> validated?
     * @exception com.rbnb.api.InvalidSealException
     *		  thrown if the <code>Seal</code> is invalid.
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>. This is
     *			no longer the top of the archive hierarchy, so it needs
     *			to handle the specified parameters.
     * 06/04/2001  INB	Created.
     *
     */
    final boolean validate(long afterI,
			   long beforeI,
			   java.util.Vector validSealsO,
			   java.util.Vector invalidSealsO)
	throws com.rbnb.api.InvalidSealException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	long after = afterI,
	     before = beforeI;

	// Read and validate the <code>Seals</code> for the
	// <code>FileSets</code>. If all of the <code>Seals</code> are valid,
	// then the <code>Archive</code> can be read without any further
	// trouble.
	boolean validR = validateFileSets(after,
					  before,
					  validSealsO,
					  invalidSealsO);

	return (validR);
    }

    /**
     * Validates the <code>FileSets</code> in the <code>Archive</code>.
     * <p>
     * This method reads all of the <code>FileSets</code> from the
     * <code>Archive's</code> directory, whether or not they are considered to
     * be part of the <code>Archive</code>. It ensures that they are consistent
     * with respect to each other and then checks them against the
     * <code>Archive</code> as a whole.
     * <p>
     *
     * @author Ian Brown
     *
     * @param afterI	    the minimum valid time.
     * @param beforeI	    the maximum valid time.
     * @param validSealsO   the list of valid <code>FileSets</code> and their
     *			    <code>Seals</code>.
     * @param invalidSealsO the list of invalid <code>FileSets</code> and their
     *			    <code>Seals</code>.
     * @return is the <code>Archive</code> valid?
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Archive</code> is in such a state that it
     *		  cannot possibly be validated.
     * @since V2.0
     * @version 02/18/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2003  INB	Use new <code>Directory</code> class.
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure.
     * 06/04/2001  INB	Created.
     *
     */
    private final boolean validateFileSets(long afterI,
					   long beforeI,
					   java.util.Vector validSealsO,
					   java.util.Vector invalidSealsO)
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	boolean validR = true;

	long after = afterI,
	     before = beforeI;
	String directory = getArchiveDirectory();
	java.io.File archive = new java.io.File(directory);
	Directory asDirectory = new Directory(archive);
	java.io.File[] files;

	// Ensure that the <code>Archive</code> directory is valid. If not,
	// we've got a serious problem.
	try {
	    if (!archive.exists()) {
		throw new java.lang.IllegalStateException
		    ("Cannot find archive directory " + directory);
	    } else if ((files = asDirectory.listFiles()) == null) {
		throw new java.lang.IllegalStateException
		    ("Archive location " + directory + " is not a directory.");
	    }
	} catch (java.io.IOException e) {
	    throw new java.lang.IllegalStateException
		("Archive in " + directory + " is in a bad state.\n" +
		 e.getMessage());
	}

	// Find the summary file and create a sorted list of the
	// <code>FileSet</code> directories.
	boolean summary = false;
	java.util.Vector fileSets = new java.util.Vector();
	for (int idx = 0; idx < files.length; ++idx) {
	    try {
		String name = files[idx].getName();
		Directory fAsDirectory = new Directory(files[idx]);

		if (name.equalsIgnoreCase("summary.rbn")) {
		    summary = true;

		} else if (name.substring(0,2).equalsIgnoreCase("FS") &&
			   (fAsDirectory.listFiles() != null)) {
		    // If this is a potential <code>FileSet</code>, attempt to
		    // add it to the list to check. We put these
		    // <code>FileSets</code> into numerical order, which,
		    // unfortunately, is not the natural sort order, so we have
		    // to do it by hand.
		    long fsIndex = 0;

		    try {
			fsIndex = Long.parseLong(name.substring(2));
		    } catch (java.lang.NumberFormatException e) {
			continue;
		    }
		    int lo,
			hi,
			idx1;
		    for (lo = 0,
			     hi = fileSets.size() - 1,
			     idx1 = (lo + hi)/2;
			 (lo <= hi);
			 idx1 = (lo + hi)/2) {
			java.io.File other =
			    (java.io.File) fileSets.elementAt(idx1);
			long oIndex = Long.parseLong
			    (other.getName().substring(2));

			if (fsIndex < oIndex) {
			    hi = idx1 - 1;
			} else if (fsIndex > oIndex) {
			    lo = idx1 + 1;
			} else {
			    break;
			}
		    }
		    fileSets.insertElementAt(files[idx],lo);
		}
	    } catch (java.io.IOException e) {
	    }
	}

	// At this point, we supposedly have a sorted list of
	// <code>FileSet</code> directories. Validate them in order.
	for (int idx = 0,
		 endIdx = fileSets.size();
	     idx < endIdx;
	     ++idx) {
	    java.io.File fileSetFile = (java.io.File) fileSets.elementAt(idx);

	    try {
		// Try to validate the <code>Seal</code> for this
		// <code>FileSet</code>. If it succeeds, then add the entry to
		// the valid list.
		Seal theSeal = Seal.validate(fileSetFile.getAbsolutePath(),
					     after,
					     before);
		if (theSeal == null) {
		    throw new com.rbnb.api.InvalidSealException
			(theSeal,
			 after,
			 before);
		} else {
		    validSealsO.addElement(fileSetFile);
		    validSealsO.addElement(theSeal);
		    after = theSeal.getAsOf();
		}

	    } catch (com.rbnb.api.InvalidSealException e) {
		// If the <code>Seal</code> wasn't valid, add it to the invalid
		// list.
		invalidSealsO.addElement(fileSetFile);
		invalidSealsO.addElement(e);
		validR = false;
	    }
	}

	if (!summary) {
	    // If we lack a summary, then the <code>Archive</code> is not
	    // valid.
	    validR = false;
	}

	return (validR);
    }

    /**
     * Writes the <code>Archive</code> to the archive files.
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
     * @see #readFromArchive()
     * @see #readOffsetsFromArchive()
     * @see #readSkeletonFromArchive()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/27/2003  INB	Ensure that we close all files.
     * 10/23/2003  INB	Don't close the <code>FileSet</code> files.
     * 09/09/2003  INB	Reordered the archive writes so that data is always
     *			written before the headers.
     * 02/18/2003  INB	Modified to work with the multiple
     *			<code>RingBuffers</code> per <code>RBO</code>
     *			structure. This is no longer the top of the heirarchy.
     * 03/14/2001  INB	Created.
     *
     */
    final void writeToArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
// Exception __e = new Exception("WHF TESTING");
// __e.printStackTrace();
	try {
	    // Lock the door.
	    getDoor().lock("writeToArchive");

	    // Update the <code>Registration</code>.
	    updateRegistration();

	    if (getSet() != null) {
		// If there is a current set, then we need to make sure that
		// it is either saved or is tossed, depending on whether or not
		// it has children.

		if (getSet().getNchildren() == 0) {
		    // If the set has no children, then toss it out.
		    setSet(null);

		} else {
		    // With any children, close it.
		    getSet().close();
		}
	    }

	    int endIdx;
	    if ((endIdx = getNchildren()) > 0) {

		// Open the <code>Registration</code> files for the
		// <code>Archive</code> as a whole.
		String directory = getArchiveDirectory() + SEPARATOR;
		java.io.FileOutputStream hfos =
		    new java.io.FileOutputStream(directory + "reghdr.rbn"),
					 dfos =
		    new java.io.FileOutputStream(directory + "regdat.rbn");
		OutputStream hos = new OutputStream(hfos,BINARYARCHIVE,0);
		BuildFile.loadBuildFile(hos);
		DataOutputStream dos = new DataOutputStream(dfos,0);

		// Write the <code>Registration</code> out.
		getRegistered().writeData(dos);
		getRegistered().write(ARCHIVE_PARAMETERS,ARC_REG,hos,dos);

		// Close the files.
		dos.close();
		dfos.close();
		hos.close();
		hfos.close();

		// Open the summary file for the <code>Archive</code> as a
		// whole.
		hfos = new java.io.FileOutputStream
		    (directory + "summary.rbn");
		java.io.PrintStream pos = new java.io.PrintStream(hfos);

		// Write the summary.
		Cache cache = ((RingBuffer) getParent()).getCache();
		pos.print("Date: ");
		pos.println((new java.util.Date()).toString());
		pos.print("Oldest FileSet: ");
		pos.println(getOldest());
		pos.print("Newest FileSet: ");
		pos.println(getNewest());
		for (int idx = 0; idx < 2; ++idx) {
		    StorageManager entry = null;
		    String header = null;

		    switch (idx) {
		    case 0:
			header = "Cache ";
			entry = cache;
			break;
		    case 1:
			header = "Archive ";
			entry = this;
			break;
		    }
		    pos.print(header + "Sets: ");
		    pos.println(entry.getMs());
		    pos.print(header + "Elements/Set: ");
		    pos.println(entry.getMeps());
		    pos.print(header + "Memory/Set: ");
		    pos.println(entry.getMmps());
		}

		// Close the summary file.
		pos.close();
		hfos.close();

		// Seal the <code>Archive</code>.
		Seal.seal(getArchiveDirectory());
	    }

	} finally {
	    // Unlock the door.
	    getDoor().unlock();
	}
    }
}
