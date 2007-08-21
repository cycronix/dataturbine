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
 * Provides a single unit for storage within the <code>Archive</code>.
 * <p>>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Archive
 * @since V2.0
 * @version 01/12/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/16/2006  EMF      Fixed recoverFromDataFiles(), reduced memory usage.
 * 01/12/2004  INB	Ensure that we don't try to write out a
 *			<code>FrameSet</code> that doesn't point to this
 *			<code>FileSet</code>.
 * 01/06/2004  INB	Ensure that we only read <code>FrameSets</code> when
 *			really necessary in <code>buildRegistration</code>.
 * 12/11/2003  INB	Added <code>RequestOptions</code> to
 *			<code>TimeRelativeRequest</code> handling to allow the
 *			code to do the right thing for
 *			<code>extendStart</code>.
 * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
 *			it.
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 10/23/2003  INB	Modified code to hold onto the files unless someone
 *			else needs to be able to open other files via the
 *			<code>LimitedResourceInterface</code>.
 * 10/22/2003  INB	Force read/write access when writing.  Reposition
 *			the archive files to the end when opened.
 * 10/17/2003  INB	Reworked file locking to only be on when it is really
 *			needed.
 * 09/09/2003  INB	Reordered the archive writes so that data is always
 *			written before the headers.  Give up on
 *			<code>FileNotFoundExceptions</code> in
 *			<code>accessFiles</code>.  Created
 *			<code>accessFiles</code> method with mode override and
 *			added the flag to the <code>openFiles</code> method.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/06/2003  INB	Added method <code>reduceToSkeleton</code>.
 * 04/30/2003  INB	Made <code>getArchiveDirectory</code> accessible
 *			outside of the class.
 * 04/24/2003  INB	Added <code>markOutOfDate</code>.
 * 03/13/2003  INB	Reduced the amount of time the <code>FileSet</code> has
 *			the files locked.
 * 03/10/2003  INB	Use new <code>Directory</code> class for getting file
 *			lists.
 * 02/19/2003  INB	Modified to handle multiple <code>RingBuffers</code>
 *			per <code>RBO</code>.
 * 03/08/2001  INB	Created.
 *
 */
final class FileSet
    extends com.rbnb.api.FrameManager
    implements com.rbnb.api.LimitedResourceInterface
{
    /**
     * file access count.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/04/2001
     */
    private long accessCount = 0;

    /**
     * has the file been deleted?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/26/2002
     */
    boolean deleted = false;

    /**
     * the data input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private DataInputStream[] dis = null;

    /**
     * the data output streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private DataOutputStream[] dos = null;

    /**
     * are the files in a usable state?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/23/2003
     */
    private boolean filesUsable = false;

    /**
     * the door for the archive file.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/04/2001
     */
    private Door fileDoor = null;

    /**
     * the <code>FileSet</code> archive <code>RandomAccessFiles</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private RandomAccessFile[] files = null;

    /**
     * the identification index of the first <code>FrameSet</code> in this
     * <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private long firstFrameSet = -1;

    /*
     * the header input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private InputStream[] his = null;

    /**
     * the header output streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private OutputStream[] hos = null;

    /**
     * the index of the last child registered.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/18/2001
     */
    private int lastRegistrationIndex = -1;

    /**
     * the <code>LimitedResource</code> object controlling open
     * <code>FileSet</code> files.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/23/2003
     */
    LimitedResource openFileSets = null;

    /**
     * has this <code>FileSet</code> been opened previously?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/04/2001
     */
    private boolean previousOpen = false;

    /**
     * the file offset of the <code>Registration</code> data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #regHeaderOffset
     * @since V2.0
     * @version 05/10/2001
     */
    private long regDataOffset = -1;

    /**
     * the file offset of the <code>Registration</code> header.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #regDataOffset
     * @since V2.0
     * @version 05/10/2001
     */
    private long regHeaderOffset = -1;

    // Private constants:
    private final static String[] ARCHIVE_PARAMETERS = {
				    "DSZ",
				    "REG",
				};

    private final static int	ARC_DSZ = 0;
    private final static int	ARC_REG = 1;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #FileSet(long)
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    FileSet()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	setFileDoor(new Door(Door.STANDARD));
    }

    /**
     * Class constructor to build a <code>FileSet</code> from a fileset
     * index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param filesetIndexI  the <code>FileSet</code> index.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #FileSet()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2001  INB	Created.
     *
     */
    FileSet(long filesetIndexI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(filesetIndexI);
	setFileDoor(new Door(Door.STANDARD));
    }

    /**
     * Register a need to access the files of the <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #releaseFiles()
     * @since V2.0
     * @version 09/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/09/2003  INB	Call version that takes an argument.
     * 10/04/2001  INB	Created.
     *
     */
    final void accessFiles()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	accessFiles(false);
    }

    /**
     * Register a need to access the files of the <code>FileSet</code>.
     * <p>
     * This version lets the caller force read/write mode.
     * </p>
     *
     * @author Ian Brown
     *
     * @param forceReadWriteI force read/write mode on files?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #forcedRelease(Object)
     * @see #releaseFiles()
     * @since V2.2
     * @version 10/24/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Modified code to hold onto the files unless someone
     *			else needs to be able to open other files via the
     *			<code>LimitedResourceInterface</code>.
     *			Set the <code>openFileSets</code> object.
     * 10/17/2003  INB	Eliminated file locking.
     * 09/09/2003  INB	Created.
     *			than a couple of seconds, give up.
     * 10/04/2001  INB	Created.
     *
     */
    final void accessFiles(boolean forceReadWriteI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long startAt = System.currentTimeMillis(),
	    now;
	boolean retry = false;
	long count;

	do {
	    retry = false;
	    synchronized (this) {
		if ((count = ++accessCount) > 1) {
		    long lastAt = System.currentTimeMillis();
		    long nowAt;
		    while (!filesUsable) {
			wait(TimerPeriod.NORMAL_WAIT);
			if (((nowAt = System.currentTimeMillis()) - lastAt) >=
			    TimerPeriod.LOCK_WAIT) {
			    try {
				throw new Exception
				    (this + " waiting for file open.");
			    } catch (Exception e) {
				e.printStackTrace();
				lastAt = nowAt;
			    }
			}
		    }
		}
	    }
	    

	    if (count == 1) {
		if (files != null) {
		    if (openFileSets == null) {
			Rmap above = getParent();
			while (!(above instanceof ServerHandler) &&
			       !(above instanceof RoutingMapHandler)) {
			    above = above.getParent();
			}
			ServerHandler sh;
			if (above instanceof RoutingMapHandler) {
			    sh = ((RoutingMapHandler)
				  above).getLocalServerHandler();
			} else {
			    sh = (ServerHandler) above;
			}
			openFileSets = sh.getOpenFileSets();
		    }

		    if (openFileSets.grabResource(this,files)) {
			synchronized (this) {
			    filesUsable = true;
			    notifyAll();
			}
			break;
		    }
		}

		while (files == null) {
		    try {
			openFiles(forceReadWriteI);
			synchronized (this) {
			    filesUsable = true;
			    notifyAll();
			}
			
		    } catch (com.rbnb.api.AddressException e) {
			synchronized (this) {
			    --accessCount;
			}
			throw e;
		    } catch (com.rbnb.api.SerializeException e) {
			synchronized (this) {
			    --accessCount;
			}
			throw e;
		    } catch (java.io.FileNotFoundException e) {
			synchronized (this) {
			    --accessCount;
			}
			now = System.currentTimeMillis();
			if (forceReadWriteI ||
			    (now - startAt > TimerPeriod.LONG_WAIT)) {
			    throw e;
			}
			Thread.sleep(100);
			retry = true;
			break;

		    } catch (java.io.IOException e) {
			synchronized (this) {
			    --accessCount;
			}
			throw e;

		    } catch (java.lang.InterruptedException e) {
			synchronized (this) {
			    --accessCount;
			}
			throw e;

		    } catch (java.lang.RuntimeException e) {
			synchronized (this) {
			    --accessCount;
			}
			throw e;
		    }
		}
	    }
	} while (retry);
    }

    /**
     * Builds the registration for this <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return were any changes made?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 01/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/06/2004  INB	Only read <code>FrameSets</code> from the archive
     *			if we have reason to believe that we need to in order
     *			to actually update the <code>FileSet</code>
     *			registration.
     * 09/18/2001  INB	Created.
     *
     */
    final boolean buildRegistration()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean changedR = false;

	for (int idx = 0, endIdx = getNchildren(); idx < endIdx; ++idx) {
	    FrameSet fs = (FrameSet) getChildAt(idx);
	    boolean updated = false;
	    if (fs.getRegistered() != null) {
		updated = fs.updateRegistration();
	    }
	    if (updated || (idx > lastRegistrationIndex)) {
		if (fs.getRegistered() == null) {
		    fs = (FrameSet) fs.clone();
		    fs.setFileSet(this);
		    fs.setParent(this);
		    fs.readFromArchive();
		    fs.setUpToDate(false);
		    fs.updateRegistration();
		}
		changedR =
		    getRegistered().updateRegistration(fs.getRegistered(),
						       false,
						       false) ||
		    changedR;
		lastRegistrationIndex = Math.max(lastRegistrationIndex,idx);
	    }
	}

	return (changedR);
    }

    /**
     * Clears this <code>FileSet's</code> contents.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/23/2003  INB	Modified code to hold onto the files unless someone
     *			else needs to be able to open other files via the
     *			<code>LimitedResourceInterface</code>.
     * 10/17/2003  INB	Eliminated the file locking.
     * 06/05/2001  INB	Created.
     *
     */
    final void clear()
	throws com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    getDoor().lock("FileSet.clear");

	    deleted = true;
	    boolean decrementCount = false;
	    if (openFileSets != null) {
		if (files != null) {
		    if (!(decrementCount = (accessCount > 0))) {
			decrementCount = openFileSets.grabResource(this,
								   files);
		    }
		}
		synchronized (this) {
		    filesUsable = false;
		    accessCount = 0;
		}
		closeFiles();

		// Release the open fileset count.
		if (decrementCount) {
		    openFileSets.removeUser();
		}
	    }

	    deleteFromArchive();
	} finally {
	    getDoor().unlock();
	}
    }

    /**
     * Closes this <code>FileSet</code> to further additions.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 10/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/22/2003  INB	Force read/write access to the files.
     * 10/17/2003  INB	Eliminated the file lock.
     * 05/06/2003  INB	Call <code>reduceToSkeleton</code>.
     * 06/05/2001  INB	Created.
     *
     */
    final void close()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    accessFiles(true);
	    writeToArchive();
	    reduceToSkeleton();
	} finally {
	    releaseFiles();
	}
    }

    /**
     * Closes the files associated with this <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #openFiles(boolean forceReadWriteI)
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/17/2003  INB	Lock the door right off.
     * 09/09/2003  INB	Changed <code>openFiles</code> reference.
     * 03/13/2003  INB	Open files is now a limited resource.
     * 03/12/2001  INB	Created.
     *
     */
    final void closeFiles()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    getFileDoor().lock("FileSet.closeFiles");

	    if (files != null) {
		// Close the streams.
		int nFiles = 0;
		if (his != null) {
		    nFiles = Math.max(nFiles,his.length);
		}
		if (hos != null) {
		    nFiles = Math.max(nFiles,hos.length);
		}
		if (dis != null) {
		    nFiles = Math.max(nFiles,dis.length);
		}
		if (dos != null) {
		    nFiles = Math.max(nFiles,dos.length);
		}
		for (int idx = 0; idx < nFiles; ++idx) {
		    if ((his != null) &&
			(idx < his.length) &&
			(his[idx] != null)) {
			his[idx].close();
		    }
		    if ((hos != null) &&
			(idx < hos.length) &&
			(hos[idx] != null)) {
			hos[idx].close();
		    }
		    if ((dis != null) &&
			(idx < dis.length) &&
			(dis[idx] != null)) {
			dis[idx].close();
		    }
		    if ((dos != null) &&
			(idx < dos.length) &&
			(dos[idx] != null)) {
			dos[idx].close();
		    }
		}
		his = null;
		hos = null;
		dis = null;
		dos = null;

		// Close the files.
		if (files != null) {
		    for (int idx = 0; idx < files.length; ++idx) {
			if (files[idx] != null) {
			    files[idx].close();
			}
		    }
		}
		files = null;
	    }

	} finally {
	    getFileDoor().unlock();
	}
    }

    /**
     * Deletes the <code>FileSet</code> archive files.
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
     * @see #readFromArchive()
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
     * 03/10/2003  INB	Use new <code>Directory</code> class for getting file
     *			lists.
     * 02/23/2001  INB	Created.
     *
     */
    final void deleteFromArchive()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    // Lock the door.
	    getFileDoor().lock("FileSet.deleteFromArchive");

	    // Create a <code>File</code> object for the archive directory.
	    java.io.File aDir = new java.io.File(getArchiveDirectory());
	    Directory asDirectory = new Directory(aDir);

	    // Delete the files in the archive for the <code>FileSet</code>.
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
			    ("Unable to delete " + files[idx] +
			     " from fileset " + getIndex());
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

	} finally {
	    // OK, we can unlock the door now.
	    getFileDoor().unlock();
	}
    }

    /**
     * Forces this object to release a limited resource.
     * <p>
     *
     * @author Ian Brown
     *
     * @param resourceI the resource to release (if this object has multiple
     *			resources).
     * @since V2.2
     * @version 10/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Created.
     *
     */
    public final void forcedRelease(Object resourceI) {
	if (resourceI == files) {
	    try {
		closeFiles();
	    } catch (java.lang.Exception e) {
	    }
	}
    }

    /**
     * Gets the archive directory path for this <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive directory path.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/30/2003  INB	Made this accessible outside of the class.
     * 02/19/2003  INB	Our parent <code>Archive</code> now has a method
     *			<code>getArchiveDirectory</code>.
     * 03/09/2001  INB	Created.
     *
     */
    final String getArchiveDirectory()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (((Archive) getParent()).getArchiveDirectory() +
		Archive.SEPARATOR +
		"FS" + getIndex());
    }

    /**
     * Gets the <code>Door</code> for the archive file of this
     * <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive file <code>Door</code>.
     * @see #setFileDoor(com.rbnb.api.Door)
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/04/2001  INB	Created.
     *
     */
    final Door getFileDoor() {
	return (fileDoor);
    }

    /**
     * Gets the archive data file input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive data file input stream.
     * @see #getHIS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final DataInputStream getDIS() {
	return ((dis == null) ? null : dis[0]);
    }

    /**
     * Gets the archive data file output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive data file output stream.
     * @see #getHOS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final DataOutputStream getDOS() {
	return ((dos == null) ? null : dos[0]);
    }

    /**
     * Gets the archive header file input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive header file input stream.
     * @see #getDIS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final InputStream getHIS() {
	return ((his == null) ? null : his[0]);
    }

    /**
     * Gets the archive header file output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive header file output stream.
     * @see #getDOS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final OutputStream getHOS() {
	return ((hos == null) ? null : hos[0]);
    }

    /**
     * Gets the archive offset data file input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive offset data file input stream.
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final DataInputStream getODIS() {
	return ((dis == null) ? null : dis[2]);
    }

    /**
     * Gets the archive offset data file output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive offset data file output stream.
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final DataOutputStream getODOS() {
	return ((dos == null) ? null : dos[2]);
    }

    /**
     * Gets the archive registration data file input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive registration data file input stream.
     * @see #getRHIS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final DataInputStream getRDIS() {
	return ((dis == null) ? null : dis[1]);
    }

    /**
     * Gets the byte offset to the registration data for this
     * <code>FileSet</code> within the <code>FileSet</code> archive
     * registration data file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the registration data offset.
     * @see #setRdoffset(long)
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
    private final long getRdoffset() {
	return (regDataOffset);
    }

    /**
     * Gets the archive registration data file output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive registration data file output stream.
     * @see #getRHOS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final DataOutputStream getRDOS() {
	return ((dos == null) ? null : dos[1]);
    }

    /**
     * Gets the archive registration header file input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive registration header file input stream.
     * @see #getRDIS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final InputStream getRHIS() {
	return ((his == null) ? null : his[1]);
    }

    /**
     * Gets the byte offset to the registration header for this
     * <code>FileSet</code> within the <code>FileSet</code> archive
     * registration header file.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the registration header offset.
     * @see #setRhoffset(long)
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
    private final long getRhoffset() {
	return (regHeaderOffset);
    }

    /**
     * Gets the archive registration header file output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the archive registration header file output stream.
     * @see #getRDOS()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/12/2001  INB	Created.
     *
     */
    final OutputStream getRHOS() {
	return ((hos == null) ? null : hos[1]);
    }

    /**
     * Marks the <code>FileSet</code> (and the <code>Archive</code> above it)
     * as out-of-date.
     * <p>
     * This method ensures that a future attempt to read the
     * <code>Archive</code> will not miss the updated information, but will
     * instead attempt to recover it if the <code>Archive</code> is not
     * subsequently closed properly.
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
    final void markOutOfDate() {
	// We do not currently reopen <code>FileSets</code> to add data, so
	// they can't actually be out-of-date. However, adding a new
	// <code>FileSet</code> to the <code>Archive</code> above us will
	// invalidate its summary.
	((Archive) getParent()).markOutOfDate();
    }

    /**
     * Matches the contents of this <code>FileSet</code> against a
     * <code>TimeRelativeRequest</code>.
     * <p>
     * This method performs the following steps:
     * <p><ol>
     *    <li>Compare the time reference for each of the channels in the
     *	      request to the limits of the <code>FileSet</code>,<li>
     *    <li>If there comparison produces different results for different
     *	      channels, then return a status of -2,</li>
     *    <li>If time references falls outside of the limits for all of the
     *	      channels, then return a status code indicating which way to move
     *	      next,</li>
     *    <li>If the time reference falls inside the limits, then start a
     *	      binary search of the <code>FrameSets</code> contained herein.
     *        Return the status code built from that search.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param requestI	    the <code>TimeRelativeRequest</code>.
     * @param roI	    the <code>RequestOptions</code>.
     * @return the <code>TimeRelativeResponse</code>.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a reference.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Rmap</code> hierarchy contains
     *		  information that cannot be processed by this code.
     * @since V2.2
     * @version 12/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2003  INB	Added <code>RequestOptions</code> to
     *			<code>TimeRelativeRequest</code> handling to allow the
     *			code to do the right thing for
     *			<code>extendStart</code>.
     * 11/04/2003  INB	Created.
     *
     */
    final TimeRelativeResponse matchTimeRelative(TimeRelativeRequest requestI,
						 RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("FileSet.matchTimeRelative: " +
			   getFullName() + "/" + getIndex() +
			   "\nRegistration: " + getRegistered() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = new TimeRelativeResponse();
	responseR.setStatus(1);
	boolean locked = false;

	try {
	    lockRead("FileSet.matchTimeRelative");
	    locked = true;

	    TimeRelativeChannel trc;
	    com.rbnb.utility.SortedVector toMatch = requestI.getByChannel();
	    DataArray limits;
	    int direction;
	    int idx;

	    for (idx = 0; idx < toMatch.size(); ++idx) {
		// Check the registered limits for each of the channels against
		// the time reference.
		trc = (TimeRelativeChannel) toMatch.elementAt(idx);
		limits = getRegistered().extract
		    (trc.getChannelName().substring(requestI.getNameOffset()));
		direction = requestI.compareToLimits(limits);

		if (idx == 0) {
		    // If this is the first channel, save its direction.
		    responseR.setStatus(direction);

		} else if (direction != responseR.getStatus()) {
		    // If the direction has changed, then we need to split the
		    // channel list.
		    responseR.setStatus(-2);
		}
	    }

	    switch (responseR.getStatus()) {
	    case -2:
		// Splitting the channels - this will go back to the top.
		break;

	    case -1:
	    case 1:
		// Reference is before or after the limits, pass it up one
		// level so that we can move on to another
		// <code>FileSet</code>.
		break;

	    case 2:
		// No data was found.  This shouldn't be possible.
		throw new java.lang.IllegalStateException
		    (requestI + " did not match any data in a fileset.");

	    case 0:
		// Looks like the data is in this <code>FileSet</code>.
		// Perform a binary search of the <code>FrameSets</code> found
		// herein.
		responseR = super.matchTimeRelative(requestI,roI);
		break;
	    }

	} finally {
	    if (locked) {
		unlockRead();
	    }
	}

	/*
	System.err.println("FileSet.matchTimeRelative: " + 
			   getFullName() + "/" + getIndex() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy in response to a
     * request <code>Rmap</code> hierarchy.
     * <p>
     * <code>FileSets</code> contain <code>FrameSets</code> that could also be
     * in the <code>Cache</code>. This method eliminates those
     * <code>FrameSets</code> that are in the <code>Cache</code> from
     * consideration here.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extractorI    the <code>RmapExtractor</code> performing the
     *			    extraction.
     * @param unsatisfiedI  the unsatisfied <code>ExtractedChain</code>.
     * @param unsatisfiedO  the new list of unsatisfied
     *			    <code>ExtractedChains</code>.
     * @return the reason for a failed match.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to the
     *		  source <code>Rmap</code> hierarchy or in extracting the
     *		  desired information.
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that a <code>Lock</code> is set before clearing
     *			it.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 02/19/2003  INB	Our grandparent is now a <code>RingBuffer</code>.
     * 05'25/2001  INB	Created.
     *
     */
    byte moveDownFrom(RmapExtractor extractorI,
		      ExtractedChain unsatisfiedI,
		      java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	/*
	String id;
	if ((getParent() instanceof Cache) ||
	    (getParent() instanceof Archive)) {
	    id = getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) getParent().getParent()).getIndex() + "/" +
		getParent().getClass() + "/" +
		getClass() + "#" + getIndex();
	} else {
	    id = getParent().getParent().getParent().getParent().getName() + "/RB" +
		((RingBuffer) getParent().getParent().getParent()).getIndex() + "/" +
		getParent().getParent().getClass() + "/" +
		getParent().getClass() + "#" +
		((FrameManager) getParent()).getIndex() +
		getClass() + "#" + getIndex();
	}
	*/

	boolean locked = false;
	try {
	    // Lock the door.
	    /*
	    System.err.println(Thread.currentThread() + " entering " +
			       id +
			       " moveDownFrom read lock.");
	    */

	    getDoor().lockRead("FileSet.moveDownFrom");
	    locked = true;

	    if (getParent() == null) {
		// If this <code>FileSet</code> is no longer in the
		// <code>Archive</code>, then the request must be too soon.
		reasonR = Rmap.MATCH_AFTER;

	    } else {
		// Create a list of the unique <code>FrameSets</code>.
		RmapVector workChildren = null;

		for (int idx = 0,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    FrameSet set = (FrameSet) getChildAt(idx);
		    if (set.getParent() == this) {
			// <code>FrameSets</code> that have this
			// <code>FileSet</code> as their parent are just in the
			// <code>Archive</code>.
			workChildren =
			    RmapVector.addToVector(workChildren,set);
		    }
		}

		if (workChildren != null) {
		    // If we found any, move down them.
		    reasonR = Rmap.moveDownFrom(null,
						workChildren,
						extractorI,
						unsatisfiedI,
						unsatisfiedO);
		}
	    }

	} finally {
	    // Unlock the door.
	    if (locked) {
		getDoor().unlockRead();
	    }

	    /*
	    System.err.println(Thread.currentThread() + " exited " +
			       id +
			       " moveDownFrom read lock.");
	    */
	}

	return (reasonR);
    }

    /**
     * Nullifies this <code>FileSet</code>.
     * <p>
     * This method ensures that all pointers in this <code>DataBlock</code>
     * are cleared, reducing the effort needed by the garbage collector to
     * clean it up.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 10/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Modified code to hold onto the files unless someone
     *			else needs to be able to open other files via the
     *			<code>LimitedResourceInterface</code>.
     * 07/30/2003  INB	Created.
     *
     */
    public final void nullify() {
	boolean decrementCount = false;

	if (openFileSets != null) {
	    try {
		if (files != null) {
		    if (!(decrementCount = (accessCount > 0))) {
			decrementCount = openFileSets.grabResource(this,
								   files);
		    }
		    accessCount = 0;
		}
		closeFiles();
	    } catch (java.lang.Exception e) {
	    }

	    // Release the open fileset count.
	    if (decrementCount) {
		openFileSets.removeUser();
	    }

	    openFileSets = null;
	}

	super.nullify();

	if (files != null) {
	    try {
		closeFiles();
	    } catch (java.lang.Exception e) {
	    }
	}
	files = null;
	dis = null;
	dos = null;
	his = null;
	hos = null;

	if (getFileDoor() != null) {
	    getFileDoor().nullify();
	    setFileDoor(null);
	}
    }

    /**
     * Opens the files associated with this <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param forceReadWriteI force read/write access?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #closeFiles()
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
     * 10/23/2003  INB	Set the <code>openFileSets</code> object.
     * 10/22/2003  INB	Ensure that the files are positioned to the end for
     *			writing.
     * 10/17/2003  INB	Lock the door after we are sure we can open the
     *			files.
     * 09/09/2003  INB	Allow for forced read/write.
     * 03/13/2003  INB	Open files is now a limited resource.
     * 02/19/2003  INB	The <code>RBO</code> is now our greatgrandparent.
     * 03/12/2001  INB	Created.
     *
     */
    private final void openFiles(boolean forceReadWriteI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean locked = false;
	try {
	    if (files == null) {
		// If the files are not already open, open them now.

		// Wait until we can actually open them.
		if (openFileSets == null) {
		    Rmap above = getParent();
		    while (!(above instanceof ServerHandler) &&
			   !(above instanceof RoutingMapHandler)) {
			above = above.getParent();
		    }
		    ServerHandler sh;
		    if (above instanceof RoutingMapHandler) {
			sh = ((RoutingMapHandler)
			      above).getLocalServerHandler();
		    } else {
			sh = (ServerHandler) above;
		    }
		    openFileSets = sh.getOpenFileSets();
		}
		openFileSets.addUser();

		// Determine what the open modes should be.
		String rafMode = "rw";
		boolean os = true,
			afDelete = !previousOpen;
		SourceHandler rbo =
		    (SourceHandler) getParent().getParent().getParent();

		if (rbo.getAmode() == Source.ACCESS_APPEND) {
		    afDelete = false;

		} else if ((rbo.getAmode() == Source.ACCESS_LOAD) &&
			   !forceReadWriteI) {
		    os = false;
		    rafMode = "r";
		    afDelete = false;

		} else if (forceReadWriteI) {
		    afDelete = false;
		}

		// Clean up any files that should be created from scratch.
		if (afDelete) {
		    deleteFromArchive();
		}

		// Lock the files.
		getFileDoor().setIdentification(getFullName() + "/" +
						getClass() + "_" +
						getIndex() + "_FileDoor");
		getFileDoor().lock("FileSet.openFiles");
		locked = true;

		// Create the directories.
		String directory = getArchiveDirectory() + Archive.SEPARATOR;
		java.io.File dir = new java.io.File(directory);
		dir.mkdirs();
		
		// Open the files.
		files = new RandomAccessFile[5];
		files[0] = new RandomAccessFile(directory + "hdr.rbn",rafMode);
		files[1] = new RandomAccessFile(directory + "dat.rbn",
						rafMode);
		files[2] = new RandomAccessFile(directory + "reghdr.rbn",
						rafMode);
		files[3] = new RandomAccessFile(directory + "regdat.rbn",
						rafMode);
		files[4] = new RandomAccessFile(directory + "offsets.rbn",
						rafMode);

		// Connect the streams.
		his = new InputStream[2];
		hos = new OutputStream[2];
		dis = new DataInputStream[3];
		dos = new DataOutputStream[3];
		his[0] = new InputStream(files[0].getRAIS(),
					 Archive.BINARYARCHIVE,
					 0);
		dis[0] = new DataInputStream(files[1].getRAIS(),0);
		his[1] = new InputStream(files[2].getRAIS(),
					 Archive.BINARYARCHIVE,
					 0);
		dis[1] = new DataInputStream(files[3].getRAIS(),0);
		dis[2] = new DataInputStream(files[4].getRAIS(),0);
		if (os) {
		    for (int idx = 0; idx < files.length; ++idx) {
			files[idx].getRAOS().setFilePointer
			    (files[idx].length());
		    }

		    hos[0] = new OutputStream(files[0].getRAOS(),
					      Archive.BINARYARCHIVE,
					      0);

		    /*
		    hos[0] = new DebugOutputStream(files[0].getRAOS(),
						   Archive.BINARYARCHIVE,
						   0);
		    */
		    hos[0].setWritten(files[0].length());
		    BuildFile.loadBuildFile(hos[0]);

		    dos[0] = new DataOutputStream(files[1].getRAOS(),0);
		    dos[0].setWritten(files[1].length());

		    hos[1] = new OutputStream(files[2].getRAOS(),
					      Archive.BINARYARCHIVE,
					      0);
		    hos[1].setWritten(files[2].length());
		    BuildFile.loadBuildFile(hos[1]);

		    dos[1] = new DataOutputStream(files[3].getRAOS(),0);
		    dos[1].setWritten(files[3].length());

		    dos[2] = new DataOutputStream(files[4].getRAOS(),0);
		    dos[2].setWritten(files[4].length());
		}
		previousOpen = true;
	    }

	} catch (java.lang.RuntimeException e) {
	    closeFiles();
	    openFileSets.removeUser();
	    throw e;

	} catch (com.rbnb.api.SerializeException e) {
	    closeFiles();
	    openFileSets.removeUser();
	    throw e;

	} catch (java.io.IOException e) {
	    closeFiles();
	    openFileSets.removeUser();
	    throw e;

	} catch (java.lang.InterruptedException e) {
	    closeFiles();
	    openFileSets.removeUser();
	    throw e;

	} finally {
	    if (locked) {
		getFileDoor().unlock();
	    }
	}
    }

    /**
     * Reads a <code>FileSet</code> from an archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return did the read succeed?
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
     * 10/17/2003  INB	Do the file lock inside the other lock.  Access the
     *			files outside locks.
     * 03/13/2003  INB	Reduce the time that the files are marked accessed.
     * 02/21/2001  INB	Created.
     *
     */
    final boolean readFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean successR = false;
	boolean[] locks = new boolean[3];

	try {
	    if (!deleted) {
		// Lock the door.
		accessFiles();
		locks[2] = true;
		getDoor().lock("FileSet.readFromArchive");
		locks[1] = true;
		getFileDoor().lock("FileSet.readFromArchive");
		locks[0] = true;

		// Read in the basic structure of the <code>FileSet</code>.
		readOffsetsFromArchive();

		// Switch to the <code>Registration</code> files.
		InputStream his = getRHIS();
		DataInputStream dis = getRDIS();

		// Reposition them.
		his.seek(getRhoffset());
		dis.seek(getRdoffset());

		// Read in the <code>Registration</code>.
		int parameter;
		
		if (his.readParameter(ARCHIVE_PARAMETERS) == ARC_REG) {
		    setRegistered(new Registration(his,dis));
		}

		// Read in the skeleton of the <code>FileSet</code>.
		readSkeletonFromArchive();

		// Read in the skeletons for the <code>FrameSets</code>.
		for (int idx = 0,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    FrameSet fs = (FrameSet) getChildAt(idx);

		    fs.readSkeletonFromArchive();
		}

		successR = true;
		getDoor().setIdentification(getFullName() + "/" +
					    getClass() + "_" +
					    getIndex());
		getFileDoor().setIdentification(getFullName() + "/" +
						getClass() + "_" +
						getIndex() + "_FileDoor");
	    }

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		releaseFiles();
	    }
	}

	return (successR);
    }

    /**
     * Recover the <code>FileSet</code> from its component data file pair
     * (header and data).
     * <p>
     * This method is a fallback
     *
     * @author Ian Brown
     *
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
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/16/2006  EMF  Fixed numerous bugs, so routine actually works.
     *                  Reduced memory requirements by orders of magnitude.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/17/2003  INB	Access the files outside the locks.
     * 09/09/2003  INB	Created.
     *
     */
    final Seal recoverFromDataFiles()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Seal theSealR = null;
	boolean[] locks = new boolean[2];

	try {
	    // Access the files, force read/write mode because we're going to
	    // write everything except the header and data.
	    accessFiles(true);
	    locks[0] = true;

	    getFileDoor().lock("FileSet.recoverFromDataFiles");
	    locks[1] = true;

	    // Read from the header and data files to get the frame sets.
	    long hOffset = 0;
	    long dOffset = 0;
	    FrameSet fs = null;
	    boolean success = true;
	    setRegistered(new Registration());

            //EMF 11/14/06: streams for writing recreated summary files
            OutputStream rhos = getRHOS();
            DataOutputStream rdos = getRDOS();
            DataOutputStream odos = getODOS();

int i=0;
	    while (success && (hOffset < files[0].length())) {
		try {
		    //fs = new FrameSet();
		    fs = new FrameSet(i++);
		    fs.setParent(this);
		    fs.setHoffset(hOffset);
		    fs.setDoffset(dOffset);
                    //EMF 11/8/06: don't read data, just header
		    //success = fs.readFromArchive();
		    success = fs.readFromArchive(false);
		    hOffset = files[0].getRAIS().getFilePointer();
		    dOffset = files[1].getRAIS().getFilePointer();
		    fs.setRegistered(new Registration());
		    fs.setUpToDate(false);
		    fs.setLastRegistration(Long.MIN_VALUE);
                    //EMF 11/14/06: create and write summary information
                    fs.updateRegistration();
//duplicate code to try; if works, make method in FrameSet to be called here 
// and from FrameSet.writeToArchive
                    fs.setRhoffset(rhos.getFilePointer());
                    fs.setRdoffset(rdos.getFilePointer());
                    Registration summary=fs.getSummary();
                    summary.writeData(rdos);
                    rdos.flush();
                    summary.write(FrameSet.ARCHIVE_PARAMETERS,FrameSet.ARC_REG,rhos,rdos);
                    rhos.flush();
                    odos.writeByte(1);
                    odos.writeLong(fs.getIndex());
                    odos.writeLong(fs.getHoffset());
                    odos.writeLong(fs.getDoffset());
                    odos.writeLong(fs.getRhoffset());
                    odos.writeLong(fs.getRdoffset());
                    odos.flush();

                    //11/16/06: remove frames to reduce memory usage
                    for (int j=fs.getNchildren()-1;j>=0;j--) fs.removeChildAt(j);
		} catch (java.lang.Exception e) {
		    e.printStackTrace();
		    success = false;
		}

		if (success) {
		    fs.setParent(null);
		    addChild(fs);
		}
	    }

	    if (getNchildren() == 0) {
		throw new java.io.EOFException
		    ("No framesets could be read from the fileset.");
	    }
	    writeToArchive();
	    try {
		theSealR = Seal.validate
		    (getArchiveDirectory(),
		     Long.MIN_VALUE,
		     Long.MAX_VALUE);
	    } catch (com.rbnb.api.InvalidSealException e) {
		throw new java.io.IOException("Failed to seal fileset.");
	    }

	} finally {
	    if (locks[1]) {
		getFileDoor().unlock();
	    }
	    if (locks[0]) {
		releaseFiles();
	    }
	}

	return (theSealR);
    }

    /**
     * Reads the file offsets for this <code>FileSet</code> from the archive.
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
     * 10/17/2003  INB	Do the file access outside of other locks.  Lock the
     *			file door last.
     * 03/13/2003  INB	Reduce the time that the files are marked accessed.
     * 03/12/2001  INB	Created.
     *
     */
    final void readOffsetsFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	int count = 0;
	boolean[] locks = new boolean[3];

	try {
	    accessFiles();
	    locks[2] = true;

	    // Lock the <code>Door</code>.
	    getDoor().lock("FileSet.readOffsetsFromArchive");
	    locks[1] = true;

	    getFileDoor().lock("FileSet.readOffsetsFromArchive");
	    locks[0] = true;

	    DataInputStream dis = getODIS();

	    // Read in the <code>FrameSets</code>.
	    int value;
	    dis.seek(0);

	    while ((value = dis.readByte()) == 1) {
		FrameSet fs = new FrameSet();
		fs.setParent(this);
		fs.readOffsetsFromArchive();
		addChild(fs);
	    }

	    // If the byte isn't right, then throw an exception.
	    if (value != 0) {
		throw new com.rbnb.api.SerializeException
		    ("Failed to find fileset offsets in archive. Expected " +
		     "value 0, got: " + value + ".");
	    }

	    // Read in the <code>FileSets</code>.
	    setRhoffset(dis.readLong());
	    setRdoffset(dis.readLong());

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		releaseFiles();
	    }
	}
    }

    /**
     * Reads a skeleton of the <code>FileSet</code> from an archive.
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
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #readFromArchive()
     * @see #readOffsetsFromArchive()
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
     * 10/17/2003  INB	Do the file lock inside the other lock.
     * 03/13/2003  INB	Eliminated the accessFiles call.
     * 02/21/2001  INB	Created.
     *
     */
    final void readSkeletonFromArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] locks = new boolean[2];

	try {
	    // Lock the <code>Door</code>.
	    getDoor().lock("FileSet.readSkeletonFromArchive");
	    locks[1] = true;
	    getFileDoor().lock("FileSet.readSkeletonFromArchive");
	    locks[0] = true;

	    // Open the summary <code>Registration</code> files for the
	    // <code>FileSet</code> as a whole.
	    String directory = getArchiveDirectory() + Archive.SEPARATOR;
	    java.io.FileInputStream hfis =
		new java.io.FileInputStream(directory + "fsreghdr.rbn");
	    InputStream his = new InputStream(hfis,
					      Archive.BINARYARCHIVE,
					      0);

	    // Read the summary <code>Registration</code>.
	    int parameter;
	    while ((his.available() > 0) &&
		   ((parameter = his.readParameter
		     (ARCHIVE_PARAMETERS)) >= 0)) {
		switch (parameter) {
		case ARC_REG:
		    setSummary(new Registration(his,null));
		    break;

		case ARC_DSZ:
		    setDataSize(his.readLong());
		    break;
		}
	    }

	    // Close the summary files; they won't be needed again.
	    his.close();
	    hfis.close();
	    
	    // Note that this <code>FileSet</code> is up-to-date.
	    setUpToDate(true);

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	}
    }

    /**
     * Reduces this <code>FrameSet</code> to a skeleton.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem with I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.1
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created.
     *
     */
    final void reduceToSkeleton()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	FrameSet fs;
	for (int idx = 0; idx < getNchildren(); ++idx) {
	    fs = (FrameSet) getChildAt(idx);
	    fs.reduceToSkeleton();
	}
    }

    /**
     * Releases the <code>FileSet's</code> archive files.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #accessFiles()
     * @since V2.0
     * @version 10/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2003  INB	Modified code to hold onto the files unless someone
     *			else needs to be able to open other files via the
     *			<code>LimitedResourceInterface</code>.
     * 10/17/2003  INB	Eliminated file locking.
     * 10/04/2001  INB	Created.
     *
     */
    final void releaseFiles()
	throws java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	synchronized (this) {
	    if ((--accessCount == 0) & (openFileSets != null)) {
		if (files != null) {
		    openFileSets.holdResource(this,files);
		}
		filesUsable = false;
	    }
	}
    }

    /**
     * Sets the archive file <code>Door</code> for this <code>FileSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param doorI the archive file <code>Door</code>.
     * @see #getFileDoor()
     * @since V2.0
     * @version 10/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/04/2001  INB	Created.
     *
     */
    private final void setFileDoor(Door doorI) {
	fileDoor = doorI;
    }

    /**
     * Sets the byte offset to the registration data for this
     * <code>FileSet</code> within the <code>FileSet</code> archive
     * registration data file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the registration data offset.
     * @see #getRdoffset()
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
    private final void setRdoffset(long offsetI) {
	regDataOffset = offsetI;
    }

    /**
     * Sets the byte offset to the registration header for this
     * <code>FileSet</code> within the <code>FileSet</code> archive
     * registration header file.
     * <p>
     *
     * @author Ian Brown
     *
     * @param offsetI  the registration header offset.
     * @see #getRhoffset()
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
    private final void setRhoffset(long offsetI) {
	regHeaderOffset = offsetI;
    }

    /**
     * Stores an <code>Rmap</code> to this <code>FileSet</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param elementI  the <code>Rmap</code>.
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
     * @since V2.0
     * @version 01/12/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/12/2004  INB	Ensure that the <code>FrameSet</code> points to us.
     * 11/14/2003  INB	Added location to the <code>Lock</code>.
     * 10/17/2003  INB	Eliminated the file lock.
     * 02/09/2001  INB	Created.
     *
     */
    final void storeElement(Rmap elementI)
 	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	FrameSet fs = (FrameSet) elementI;

	try {
	    fs.getDoor().lock("FileSet.storeElement");

	    // Add the <code>FrameSet</code>.
	    addChild(elementI);

	    // Write the <code>FrameSet</code>
	    fs.setFileSet(this);
	    fs.writeToArchive();

	} finally {
	    fs.getDoor().unlock();
	}
    }

    /**
     * Writes this <code>FileSet</code> to the archive.
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
     * 10/22/2003  INB	Force read/write access.
     * 10/17/2003  INB	Access the files outside of the locks and put the
     *			file lock to the inside.
     * 09/09/2003  INB	Reordered the archive writes so that data is always
     *			written before the headers.
     * 03/13/2003  INB	Reduce the amount of time that the files are accessed.
     * 02/21/2001  INB	Created.
     *
     */
    final void writeToArchive()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] locks = new boolean[3];

	try {
	    // By the time we get here, the only thing left to do is to write
	    // the registration for the entire <code>FileSet</code>.
	    updateRegistration();

	    // Access the files.
	    accessFiles(true);
	    locks[2] = true;

	    // Lock the door.
	    getDoor().lock("FileSet.writeToArchive");
	    locks[1] = true;
	    getFileDoor().lock("FileSet.writeToArchive");
	    locks[0] = true;

	    OutputStream hos = getRHOS();
	    DataOutputStream dos = getRDOS();

	    // Write the registration for the <code>FileSet</code>.
	    setRdoffset(dos.getFilePointer());
	    getRegistered().writeData(dos);
	    setRhoffset(hos.getFilePointer());
	    getRegistered().write(ARCHIVE_PARAMETERS,ARC_REG,hos,dos);

	    // Write out the offsets.
	    dos = getODOS();
	    dos.writeByte(0);
	    dos.writeLong(getRhoffset());
	    dos.writeLong(getRdoffset());

	    // Open the summary <code>Registration</code> files for the
	    // <code>FileSet</code> as a whole.
	    String directory = getArchiveDirectory() + Archive.SEPARATOR;
	    java.io.FileOutputStream hfos =
		new java.io.FileOutputStream(directory + "fsreghdr.rbn");
	    hos = new OutputStream(hfos,
				   Archive.BINARYARCHIVE,
				   0);
	    BuildFile.loadBuildFile(hos);

	    // Write out the data size.
	    hos.writeParameter(ARCHIVE_PARAMETERS,ARC_DSZ);
	    hos.writeLong(getDataSize());

	    // Write the summary <code>Registration</code>.
	    getSummary().write(ARCHIVE_PARAMETERS,ARC_REG,hos,null);

	    // Close the files.
	    hos.close();
	    hfos.close();

	    // Release the files.
	    releaseFiles();
	    locks[2] = false;

	    // Seal the <code>FileSet</code>.
	    Seal.seal(getArchiveDirectory());

	} finally {
	    // Unlock the door.
	    if (locks[0]) {
		getFileDoor().unlock();
	    }
	    if (locks[1]) {
		getDoor().unlock();
	    }
	    if (locks[2]) {
		releaseFiles();
	    }
	}
    }
}
