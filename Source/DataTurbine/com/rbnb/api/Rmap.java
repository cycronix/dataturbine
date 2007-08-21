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
 * Hierarchical description of data.
 * <p>
 * An <code>Rmap</code> consists of six major parts:
 * <p><ol>
 * <li>A name,</li>
 * <li>A time <code>TimeRange</code>,</li>
 * <li>A frame index <code>TimeRange</code>,</li>
 * <li>A data block,</li>
 * <li>A list of group member <code>Rmaps</code>, and</li>
 * <li>A list of child <code>Rmaps</code>.</li>
 * </ol><p>
 * The optional name is a string that provides part of the unique
 * identification for this <code>Rmap</code> within its parent
 * <code>Rmap's</code> group or child list. 
 * <p>
 * The optional time <code>TimeRange</code> provides the remainder of the
 * unique identification for this <code>Rmap</code> within its parent
 * <code>Rmap's</code> group or child list.
 * <p>
 * The optional frame index <code>TimeRange</code> provides the frame index(es)
 * represented by this <code>Rmap</code>. A "frame" is defined as a single
 * <code>Rmap</code> hierarchy sent by a source application to the server. The
 * server provides a monotonically increasing index for each frame.
 * <p>
 * A parent <code>Rmap</code> can contain only a single <code>Rmap</code> with
 * a particular combination of name and time (including no name and no
 * <code>TimeRange</code>). <code>Rmaps</code> are sorted in a list (group or
 * child) by name (nameless <code>Rmaps</code> come first) and then by time
 * (timeless <code>Rmaps</code> come first).
 * <p>
 * The optional <code>DataBlock</code> describes and optionally provides the
 * data belonging to this <code>Rmap</code> and its children.
 * <p>
 * The optional group list contains <code>Rmaps</code> that inherit the
 * children of this <code>Rmap</code> as children of themselves.
 * <p>
 * The optional children list contains <code>Rmaps</code> that belong to this
 * <code>Rmap</code> and its group members.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataBlock
 * @see com.rbnb.api.DataReference
 * @see com.rbnb.api.DataArray
 * @see com.rbnb.api.TimeRange
 * @since V2.0
 * @version 05/12/2005
 */

/*
 * Copyright 2000, 2001, 2002, 2003, 2004, 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/12/2005  JPW	In xmlRegistration(): To keep the code Java 1.1.4
 *			compatible, replaced the call to Vector.get(0) with
 *			Vector.elementAt(0).
 * 08/03/2004  INB	Added documentation to extractRmap.  Added in-line
 *			documentation.
 * 04/30/2004  INB	Use the index of the for loop rather than 0 in the
 *			search that checks each of the children in
 *			<code>beforeTimeRelative</code>.
 * 02/26/2004  INB	Ensure that the various time-relative routines handle
 *			name failures on children.
 * 02/17/2004  INB	Added synchronization to <code>addChild</code> and
 *			<code>addChildAt</code> to ensure that we can't have
 *			multiple adds happening simultaneously that each create
 *			a new <code>RmapVector</code>.
 * 12/11/2003  INB	Added <code>RequestOptions</code> to
 *			<code>TimeRelativeRequest</code> handling to allow the
 *			code to do the right thing for
 *			<code>extendStart</code>.
 * 12/09/2003  INB	Don't subtract out the <code>TimeRelativeRequest</code>
 *			duration.
 * 11/14/2003  INB	Added <code>lockRead</code> with a location.
 * 11/05/2003  INB	Added <code>afterTimeRelative</code>,
 *			<code>beforeTimeRelative</code>, and
 *			<code>findPointsTimeRelative</code>.
 * 11/03/2003  INB	Added <code>matchTimeRelative</code>.
 * 10/23/2003  INB	Clean out children before the <code>Rmap</code> in
 *			<code>nullify</code>.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/09/2003  INB	Added <code>extractFolders</code> method.
 * 05/06/2003  INB	Added <code>extractFoldersAndChannels</code> and
 *			supporting methods. Firewalled <code>extract</code>
 *			against folders.
 * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
 *			<code>RmapVector()</code>.
 * 04/04/2003  INB	Reorganized name matching logic in
 *			<code>matches</code>.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/14/2003  INB	Added <code>addChildAt</code> method.
 * 11/30/2000  INB	Created.
 *
 */

/*
   Note to programmers implementing extensions of this class: if your subclass
   needs to add new parameters, you need to override the following methods:

   defaultParameters(Rmap,boolean[])
   read(Rmap,InputStream,DataInputStream)
   write(Rmap,String[],int,InputStream,DataOutputStream)

   In addition, you need to add a new entry to the list of parameters here to
   allow your new class type to be read/written as a child of an
   <code>Rmap</code>.

   See the code in DataRequest.java for an example of these changes.
*/

public class Rmap
    extends com.rbnb.api.Serializable
    implements com.rbnb.utility.SortCompareInterface
{
    // Public constants:
    /**
     * the delimiter between names in a fully specified channel name path.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    public static final char	PATHDELIMITER = '/';

    // Package constants:
    static final byte	OPR_CHILDREN = 0,
			OPR_EXTRACTION = 1,
			OPR_MEMBERS = 2,
			MATCH_UNKNOWN = -5,
			MATCH_ILLEGAL = -4,
			MATCH_BEFORE = -3,
			MATCH_BEFORENAME = -2,
			MATCH_NOINTERSECTION = -1,
			MATCH_EQUAL = 0,
			MATCH_SUBSET = 1,
			MATCH_SUPERSET = 2,
			MATCH_INTERSECTION = 3,
			MATCH_AFTERNAME = 4,
			MATCH_AFTER = 5;

    // Private constants:
    private final static byte	PAR_CHL = 0,
			        PAR_DBK = 1,
				PAR_FRG = 2,
				PAR_GRP = 3,
				PAR_NAM = 4,
				PAR_TRG = 5;

    private final static String[] PARAMETERS = {
				    "CHL",
				    "DBK",
				    "FRG",
				    "GRP",
				    "NAM",
				    "TRG"
				};

    // Private fields:
    /**
     * data for this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private DataBlock dataBlock = null;

    /**
     * parent <code>Rmap</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Rmap parent = null;

    /**
     * <code>Rmap</code> children of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private RmapVector children = null;

    /**
     * <code>Rmap</code> group children of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private RmapVector groupList = null;

    /**
     * name of the <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private String name = null;

    /**
     * frame index span of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private TimeRange frameRange = null;

    /**
     * name is not to be significant?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/21/2002
     */
    boolean nameNSD = false;

    /**
     * time span of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private TimeRange timeRange = null;

    /**
     * Marker <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/20/2002
     */
    static DataBlock MarkerBlock = new DataBlock(new byte[1],1,1);

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #Rmap(String)
     * @see #Rmap(String,DataBlock,TimeRange)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public Rmap() {
	super();
    }

    /**
     * Class constructor to build an <code>Rmap</code> by reading it from an
     * input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #Rmap()
     * @see #Rmap(String)
     * @see #Rmap(String,DataBlock,TimeRange)
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    Rmap(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build an <code>Rmap</code> by reading it from an
     * input stream.
     * <p>
     * This constructor fills in unread fields from the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #Rmap()
     * @see #Rmap(String)
     * @see #Rmap(String,DataBlock,TimeRange)
     * @since V2.0
     * @version 07/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2001  INB	Created.
     *
     */
    Rmap(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(otherI,isI,disI);
    }

    /**
     * Class constructor to build an <code>Rmap</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the name of the <code>Rmap</code>.
     * @see #Rmap()
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/24/2000  INB	Created.
     *
     */
    public Rmap(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	setName(nameI);
    }

    /**
     * Class constructor to build an <code>Rmap</code> from a name, data block,
     * and <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the name of the <code>Rmap</code>.
     * @param dblockI  the <code>DataBlock</code>.
     * @param trangeI  the <code>TimeRange</code>.
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
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public Rmap(String nameI,DataBlock dblockI,TimeRange trangeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(nameI);
	setDblock(dblockI);
	setTrange(trangeI);
    }

    /**
     * Adds a new <code>Rmap</code> hierarchy (channel name) to an existing
     * hierarchy.
     * <p>
     * This method creates the minimum number of additional <code>Rmaps</code>
     * needed to add the new hierarchy by matching names in the new hierarchy
     * to those in the existing hierarchy.
     * <p>
     * If necessary, this method creates a common parent for the existing
     * hierarchy and the new one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param channelNameI the name to add.
     * @return the new <code>Rmap</code> representing the channel.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if there is a problem with the input name.
     * @exception java.lang.IllegalStateException
     *		  thrown if this <code>Rmap</code> already has a parent.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 08/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2004  INB	Added in-line documentation.
     * 11/05/2001  INB	Created.
     *
     */
    public final Rmap addChannel(String channelNameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getParent() != null) {
	    throw new java.lang.IllegalStateException
		("Cannot add a channel Rmap hierarchy to an Rmap that has a " +
		 "parent.");
	}

	Rmap topR = this,
	     where = this,
	     child;
	int curIdx = -1,
	    nxtIdx = channelNameI.indexOf(PATHDELIMITER);
	String name;
	boolean added = false;

	if (nxtIdx != 0) {
	    // If the input name does not start with a path delimiter, then it
	    // must be a relative name.  Create a "." Rmap at the top of its
	    // hierarchy.
	    name = ".";
	    nxtIdx = 0;

	} else {
	    // If the input name does start with a delimiter, then determine
	    // whether it is a single name or a qualified name.
	    nxtIdx = channelNameI.indexOf(PATHDELIMITER,1);
	    if (nxtIdx == -1) {
		name = channelNameI.substring(1);
	    } else {
		name = channelNameI.substring(1,nxtIdx);
	    }
	    curIdx = nxtIdx;
	}

	if (where.getName() == null) {
	    // If the top of the current working hierarchy has no name, then
	    // find the child Rmap matching first part of the new channel
	    // name and move down to that child.  As necessary, create a new
	    // entry.
	    Rmap tChild = new Rmap(name);
	    if ((child = where.findChild(tChild)) != null) {
		where = child;
	    } else {
		where.addChild(tChild);
		where = tChild;
		added = true;
	    }

	} else if (where.compareNames(name) != 0) {
	    // If the name at the top of the current working hierarchy does not
	    // match the first part of the new channel name, then we need to
	    // create a new root Rmap at the top that is the parent of both the
	    // current working hierarchy and a new Rmap for the new channel
	    // name.
	    topR = new Rmap();
	    topR.addChild(this);
	    where = new Rmap(name);
	    topR.addChild(where);
	    added = true;
	}

	while (nxtIdx != -1) {
	    // Loop until we find the end of the new channel name.  Parse off
	    // parts of the channel name at each path delimiter.
	    nxtIdx = channelNameI.indexOf(PATHDELIMITER,curIdx + 1);
	    if (nxtIdx == -1) {
		name = channelNameI.substring(curIdx + 1);
	    } else {
		name = channelNameI.substring(curIdx + 1,nxtIdx);
	    }
	    curIdx = nxtIdx;

	    Rmap tChild = new Rmap(name);
	    if (added) {
		where.addChild(tChild);
		where = tChild;

	    } else if ((child = where.findChild(tChild)) != null) {
		where = child;

	    } else {
		if (!added && (where.getNchildren() == 0)) {
		    Rmap t2Child = new Rmap();
		    if (where.getTrange() != null) {
			t2Child.setTrange(where.getTrange());
			where.setTrange(null);
		    }
		    if (where.getFrange() != null) {
			t2Child.setFrange(where.getFrange());
			where.setFrange(null);
		    }
		    if (where.getDblock() != null) {
			t2Child.setDblock(where.getDblock());
			where.setDblock(null);
		    }
		    where.addChild(t2Child);
		}
		where.addChild(tChild);
		where = tChild;
		added = true;
	    }
	}
	if (!added) {
	    Rmap t2Child = new Rmap();
	    if (where.getTrange() != null) {
		t2Child.setTrange(where.getTrange());
		where.setTrange(null);
	    }
	    if (where.getFrange() != null) {
		t2Child.setFrange(where.getFrange());
		where.setFrange(null);
	    }
	    if (where.getDblock() != null) {
		t2Child.setDblock(where.getDblock());
		where.setDblock(null);
	    }
	    where.addChild(t2Child);
	}

	return (where);
    }

    /**
     * Adds a child <code>Rmap</code> to this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the new child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a
     *		  child of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 02/17/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2004  INB	Added synchronization to ensure that only one
     *			<code>RmapVector</code> is created.
     * 11/30/2000  INB	Created.
     *
     */
    public void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (childI == null) {
	    // Cannot have null children.
	    throw new IllegalArgumentException
		("Cannot add a null child to " + getName() + ".");

	} else if (!isImplemented(OPR_CHILDREN)) {
	    throw new java.lang.IllegalArgumentException
		("Children are not supported.");
	}

	childI.setParent(this);
	synchronized (this) {
	    setChildren(RmapVector.addToVector(getChildren(),childI));
	}
    }

    /**
     * Adds a child <code>Rmap</code> to this <code>Rmap</code> at the
     * specified index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the new child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a
     *		  child of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 02/17/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/17/2004  INB	Added synchronization to ensure that only one
     *			<code>RmapVector</code> is created.  Also, use the
     *			<code>RmapVector</code> method <code>insertAt</code>.
     * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
     *			<code>RmapVector()</code>.
     * 03/14/2003  INB	Created from <code>addChild<code>.
     *
     */
    public void addChildAt(Rmap childI,int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (childI == null) {
	    // Cannot have null children.
	    throw new IllegalArgumentException
		("Cannot add a null child to " + getName() + ".");

	} else if (!isImplemented(OPR_CHILDREN)) {
	    throw new java.lang.IllegalArgumentException
		("Children are not supported.");
	}

	childI.setParent(this);
	synchronized (this) {
	    if (getChildren() == null) {
		setChildren(new RmapVector(1));
	    }
	    getChildren().insertAt(childI,indexI);
	}
    }

    /**
     * Adds data to this <code>Rmap</code> hierarchy with a
     * <code>DataArray</code> supplying the <code>TimeRanges</code>.
     * <p>
     * The method adds one or more unnamed <code>Rmaps</code> to this
     * <code>Rmap</code>.
     * <p>
     * This method is intended to allow plugins to efficiently copy time values
     * from their input to their output without having to go through extracting
     * the individual time values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataI	 the data object.
     * @param ndataI	 the number of data points in the input.
     * @param ptsizeI	 the size of a data point in bytes.
     * @param dtypeI	 the type of data.
     * @param worderI	 the word order.
     * @param referenceI the <code>DataArray</code> reference.
     *			 <p>
     *			 The number of points in the reference must be equal to
     *			 the number of points in the data object.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a
     *		  child of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 01/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  INB	Created.
     *
     */
    public final void addDataWithTimeReference(Object dataI,
					       int ndataI,
					       int ptsizeI,
					       byte dtypeI,
					       byte worderI,
					       DataArray referenceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Object iData = dataI;

	if (referenceI.getNumberOfPoints() != ndataI) {
	    throw new java.lang.IllegalArgumentException
		("The number of points (" + ndataI + ") of data does not " +
		 "match the reference (" + referenceI.getNumberOfPoints() +
		 ").");
	}

	DataBlock ldblock = getDblock();
	TimeRange ltrange = getTrange(),
	    lfrange = getFrange();

	if ((ldblock != null) || (ltrange != null) || (lfrange != null)) {
	    Rmap lparent = getParent();
	    setDblock(null);
	    setTrange(null);
	    setFrange(null);
	    if ((ltrange != null) || (lfrange != null)) {
		lparent.removeChild(this);
		lparent.addChild(this);
	    }

	    Rmap existingInfo = new Rmap();
	    existingInfo.setDblock(ldblock);
	    existingInfo.setTrange(ltrange);
	    existingInfo.setFrange(lfrange);
	    addChild(existingInfo);
	}

	ldblock = new DataBlock(iData,
				ndataI,
				ptsizeI,
				dtypeI,
				worderI,
				false,
				0,
				ptsizeI);

	if (referenceI.timeRanges == null) {
	    if ((getNchildren() == 0) ||
		(getChildAt(0).getName() != null)) {
		setDblock(ldblock);
	    } else {
		addChild(new Rmap(null,ldblock,null));
	    }

	} else {
	    if (referenceI.timeRanges.size() == 1) {
		if ((getNchildren() == 0) ||
		    (getChildAt(0).getName() != null)) {
		    setTrange((TimeRange)
			      referenceI.timeRanges.firstElement());
		    setDblock(ldblock);
		} else {
		    addChild
			(new Rmap
			    (null,
			     ldblock,
			     (TimeRange)
			     referenceI.timeRanges.firstElement()));
		}
			      
	    } else {
		DataBlock extractRef = new DataBlock();
		extractRef.setPtsize(ptsizeI);
		extractRef.setDtype(dtypeI);
		extractRef.setNpts(1);
		extractRef.setOffset(0);
		extractRef.setStride(ptsizeI);
		Rmap newRmap;

		for (int idx = 0,
			 sPoint = 0,
			 endIdx = referenceI.timeRanges.size();
		     idx < endIdx;
		     ++idx) {
		    ltrange = (TimeRange) referenceI.timeRanges.elementAt(idx);
		    int nPoints = ((referenceI.pointsPerRange == null) ?
				   ltrange.getNptimes() :
				   ((Integer)
				    referenceI.pointsPerRange.elementAt
				    (idx)).intValue());

		    ldblock.setNpts(nPoints);
		    ldblock.setOffset(sPoint*ptsizeI);
		    newRmap = new Rmap(null,
				       new DataBlock(ldblock.extractData
						     (extractRef),
						     nPoints,
						     ptsizeI,
						     dtypeI,
						     worderI,
						     false,
						     0,
						     ptsizeI),
				       ltrange);
		    addChild(newRmap);
		    sPoint += nPoints;
		}
	    }
	}
    }

    /**
     * Adds names of <code>Rmaps</code> in this <code>Rmap</code> hierarchy
     * that have no children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param chainListI  the list of <code>NameChains</code> that are being
     *			  worked on.
     * @param nameListIO  the list of names generated.
     * @return the list of <code>NameChains</code> still needing work.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.1
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created from <code>addNamesWithData</code>.
     *
     */
    private final java.util.Vector addFolderNames
	(java.util.Vector chainListI,
	 SortedStrings nameListIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Create the internal list of <code>RmapChains</code>. These are used
	// to process the members and children of this <code>Rmap</code>.
	java.util.Vector chainListR = new java.util.Vector();

	if (chainListI == null) {
	    // If there is no input list, we start with just this
	    // <code>Rmap</code> in the <code>FolderChain</code>.
	    FolderChain fChain = new FolderChain(this);
	    chainListR.addElement(fChain);

	    if (fChain.getAtName()) {
		// If the <code>FolderChain</code> is at a naming point, add a
		// name to the list.
		nameListIO.add(fChain.getName());
	    }

	} else {
	    // If there is an input list, we create a new list consisting
	    // clones of the members of the input list with the addition of
	    // this <code>Rmap</code> to each cloned <code>FolderChain</code>.

	    for (int idx = 0,
		     endIdx = chainListI.size();
		 idx < endIdx;
		 ++idx) {
		// Create a clone of each entry in turn.
		FolderChain fChain = (FolderChain)
		    ((FolderChain) chainListI.elementAt(idx)).clone();

		// Add this <code>Rmap</code> to the end of the clone.
		fChain.addRmap(this);

		// Add the new <code>FolderChain</code> to the internal list.
		chainListR.addElement(fChain);

		if (fChain.getAtName()) {
		    // If the <code>FolderChain</code> is at a naming point,
		    // add a name to the list.
		    nameListIO.add(fChain.getName());
		}
	    }
	}

	int eIdx;
	if ((eIdx = getNmembers()) > 0) {
	    // If this <code>Rmap</code> has group members, build
	    // <code>FolderChains</code> for all of them.
	    java.util.Vector origChains = chainListR;
	    chainListR = new java.util.Vector();

	    for (int idx = 0; idx < eIdx; ++idx) {
		// Add the names for each of the members to the list of names.
		Rmap member = (Rmap) getMemberAt(idx);
		java.util.Vector mChains =
		    member.addFolderNames(origChains,nameListIO);

		// Add the entries from the return list to the chain list being
		// built for processing the children.
		for (int idx1 = 0, eIdx1 = mChains.size();
		     idx1 < eIdx1;
		     ++idx1) {
		    chainListR.addElement(mChains.elementAt(idx1));
		}
	    }
	}

	if ((eIdx = getNchildren()) > 0) {
	    // If this <code>Rmap</code> has children, build
	    // <code>FolderChains</code> for all of them.
	    java.util.Vector origChains = chainListR;
	    chainListR = new java.util.Vector();

	    for (int idx = 0; idx < eIdx; ++idx) {
		// Add the names for each of the children to the list of names.
		Rmap child = (Rmap) getChildAt(idx);
		java.util.Vector cChains =
		    child.addFolderNames(origChains,nameListIO);

		// Add the entries from the return list to the return chain
		// list.
		for (int idx1 = 0,
			 endIdx1 = cChains.size();
		     idx1 < endIdx1;
		     ++idx1) {
		    chainListR.addElement(cChains.elementAt(idx1));
		}
	    }
	}

	return (chainListR);
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This implementation returns "".
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    String additionalToString() {
	return ("");
    }

    /**
     * Adds a member <code>Rmap</code> to this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI  the new member <code>Rmap</code>.
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
     * @see #getMembers()
     * @see #getParent()
     * @see #removeMember(com.rbnb.api.Rmap)
     * @see #removeMemberAt(int)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public void addMember(Rmap memberI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (memberI == null) {
	    // Cannot have null members.
	    throw new java.lang.IllegalArgumentException
		("Cannot add a null member to " + getName() + ".");

	} else if (!isImplemented(OPR_MEMBERS)) {
	    throw new java.lang.IllegalArgumentException
		("Members are not supported.");
	}

	memberI.setParent(this);
	setMembers(RmapVector.addToVector(getMembers(),memberI));
    }

    /**
     * Adds names of <code>Rmaps</code> in this <code>Rmap</code> hierarchy
     * that have data associated with them.
     * <p>
     *
     * @author Ian Brown
     *
     * @param chainListI  the list of <code>NameChains</code> that are being
     *			  worked on.
     * @param nameListIO  the list of names generated.
     * @return the list of <code>NameChains</code> still needing work.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    private final java.util.Vector addNamesWithData
	(java.util.Vector chainListI,
	 SortedStrings nameListIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Create the internal list of <code>RmapChains</code>. These are used
	// to process the members and children of this <code>Rmap</code>.
	java.util.Vector chainListR = new java.util.Vector();

	if (chainListI == null) {
	    // If there is no input list, we start with just this
	    // <code>Rmap</code> in the <code>NameChain</code>.
	    NameChain nChain = new NameChain(this);
	    chainListR.addElement(nChain);

	    if (nChain.getAtName()) {
		// If the <code>NameChain</code> is at a naming point, add a
		// name to the list.
		nameListIO.add(nChain.getName());
	    }

	} else {
	    // If there is an input list, we create a new list consisting
	    // clones of the members of the input list with the addition of
	    // this <code>Rmap</code> to each cloned <code>NameChain</code>.

	    for (int idx = 0,
		     endIdx = chainListI.size();
		 idx < endIdx;
		 ++idx) {
		// Create a clone of each entry in turn.
		NameChain nChain = (NameChain)
		    ((NameChain) chainListI.elementAt(idx)).clone();

		// Add this <code>Rmap</code> to the end of the clone.
		nChain.addRmap(this);

		// Add the new <code>NameChain</code> to the internal list.
		chainListR.addElement(nChain);

		if (nChain.getAtName()) {
		    // If the <code>NameChain</code> is at a naming point, add
		    // a name to the list.
		    nameListIO.add(nChain.getName());
		}
	    }
	}

	int eIdx;
	if ((eIdx = getNmembers()) > 0) {
	    // If this <code>Rmap</code> has group members, build
	    // <code>NameChains</code> for all of them.
	    java.util.Vector origChains = chainListR;
	    chainListR = new java.util.Vector();

	    for (int idx = 0; idx < eIdx; ++idx) {
		// Add the names for each of the members to the list of names.
		Rmap member = (Rmap) getMemberAt(idx);
		java.util.Vector mChains =
		    member.addNamesWithData(origChains,
					    nameListIO);

		// Add the entries from the return list to the chain list being
		// built for processing the children.
		for (int idx1 = 0, eIdx1 = mChains.size();
		     idx1 < eIdx1;
		     ++idx1) {
		    chainListR.addElement(mChains.elementAt(idx1));
		}
	    }
	}

	if ((eIdx = getNchildren()) > 0) {
	    // If this <code>Rmap</code> has children, build
	    // <code>NameChains</code> for all of them.
	    java.util.Vector origChains = chainListR;
	    chainListR = new java.util.Vector();

	    for (int idx = 0; idx < eIdx; ++idx) {
		// Add the names for each of the children to the list of names.
		Rmap child = (Rmap) getChildAt(idx);
		java.util.Vector cChains =
		    child.addNamesWithData(origChains,
					   nameListIO);

		// Add the entries from the return list to the return chain
		// list.
		for (int idx1 = 0,
			 endIdx1 = cChains.size();
		     idx1 < endIdx1;
		     ++idx1) {
		    chainListR.addElement(cChains.elementAt(idx1));
		}
	    }
	}

	return (chainListR);
    }

    /**
     * Add an increment to the start time of an <code>Rmap</code> hierarchy.
     * <p>
     * The "start time" can actually be a starting frame, if the request has
     * frame rather than time information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param incrementI  the amount to add to the start time.
     * @return  a flag indicating whether or not any times were updated.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if groups of this <code>Rmap</code> hierarchy are set
     *		  up in an inconsistent fashion such that some members have and
     *		  some members do not have <code>TimeRanges</code> to be
     *		  updated.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    final boolean addToStart(double incrementI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean updatedR = false;
	TimeRange tr = (getTrange() == null) ? getFrange() : getTrange();

	if ((tr != null) && (tr.getNptimes() > 0)) {
	    // If there is <code>TimeRange</code> in this <code>Rmap</code> and
	    // it has time values, then add to its times.
	    tr.addToTimes(incrementI);
	    updatedR = true;

	} else {
	    // Otherwise, we need to work down from here.
	    for (int idx = 0, eIdx = getNmembers(); idx < eIdx; ++idx) {
		// Work on the group members.
		Rmap member = getMemberAt(idx);
		boolean updated = member.addToStart(incrementI);

		if (idx == 0) {
		    updatedR = updated;

		} else if (updated != updatedR) {
		    throw new java.lang.IllegalStateException
			("Cannot add increment to start times of some, but " +
			 "not all members of\n" + this);
		}
	    }

	    if (!updatedR) {
		// If the members did not produce an update, then we need to
		// work down the children too.

		for (int idx = 0, eIdx = getNchildren();
		     idx < eIdx;
		     ++idx) {
		    Rmap child = getChildAt(idx);
		    boolean updated = child.addToStart(incrementI);

		    if (idx == 0) {
			updatedR = updated;

		    } else if (updated != updatedR) {
			throw new java.lang.IllegalStateException
			    ("Cannot add increment to start times of some, " +
			     "but not all children of\n" + this);
		    }
		}
	    }
	}

	return (updatedR);
    }

    /**
     * Adds the <code>Rmap's</code> parameters to the full serialization
     * parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 06/01/2001  INB	Created.
     *
     */
    static String[] addToParameters
	(String[] parametersI)
    {
	return (addToParameters(parametersI,PARAMETERS));
    }

    /**
     * Adds the input parameters to the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param allParametersI the serialization parameters list so far.
     * @param parametersI    the list of parameters to add.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 06/01/2001  INB	Created.
     *
     */
    final static String[] addToParameters
	(String[] allParametersI,
	 String[] parametersI) {
	int startAt = 0;
	String[] parametersR;

	if (allParametersI == null) {
	    parametersR = new String[parametersI.length];
	} else {
	    parametersR = new String[allParametersI.length +
				    parametersI.length];

	    System.arraycopy
		(allParametersI,
		 0,
		 parametersR,
		 0,
		 allParametersI.length);
	    startAt = allParametersI.length;
	}

	System.arraycopy(parametersI,
			 0,
			 parametersR,
			 startAt,
			 parametersI.length);

	return (parametersR);
    }

    /**
     * Builds a request for data for the requested channels starting at the
     * beginning of this <code>Rmap</code> (which is after the time
     * reference).
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
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
     * @see #beforeTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions roI)
     * @since V2.2
     * @version 02/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2004  INB	If we get a name failures, then move to the next
     *			child.
     * 12/11/2003  INB	Added <code>RequestOptions</code> to
     *			<code>TimeRelativeRequest</code> handling to allow the
     *			code to do the right thing for
     *			<code>extendStart</code>.
     * 11/04/2003  INB	Created.
     *
     */
    TimeRelativeResponse afterTimeRelative(TimeRelativeRequest requestI,
					   RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException

    {
	/*
	System.err.println("Rmap.afterTimeRelative: " + getFullName() + " " +
			   getTrange() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = null;
	TimeRelativeRequest request = requestI;
	if (getName() != null) {
	    // With a name, we need to ensure that we've got a good one.
	    TimeRelativeChannel trc = (TimeRelativeChannel)
		request.getByChannel().firstElement();
	    String requestName = trc.nextNameLevel(request.getNameOffset());
	    int direction = compareNames(requestName);

	    if (direction > 0) {
		responseR = new TimeRelativeResponse();
		responseR.setStatus(-3);
		return (responseR);
	    } else if (direction < 0) {
		responseR = new TimeRelativeResponse();
		responseR.setStatus(3);
		return (responseR);
	    }
	    request = new TimeRelativeRequest();
	    request.setNameOffset
		(requestI.getNameOffset() +
		 requestName.length() +
		 1);
	    request.setRelationship
		(requestI.getRelationship());
	    request.setTimeRange(requestI.getTimeRange());
	    request.setByChannel(requestI.getByChannel());
	}

	if (getTrange() != null) {
	    // If the <code>TimeRange</code> is here, then use it.
	    responseR = new TimeRelativeResponse();
	    responseR.setStatus(0);
	    responseR.setTime(getTrange().getTime());
	    responseR.setInvert(false);

	} else {
	    // Otherwise, find the one for the entry that matches the name.
	    for (int idx = 0,
		     endIdx = getNchildren();
		 idx < endIdx;
		 ++idx) {
		responseR = getChildAt(idx).afterTimeRelative(request,roI);
		if (responseR.getStatus() != 3) {
		    // This child is not before the name, so it is as good as
		    // anything.
		    break;
		}
	    }
	}

	/*
	System.err.println("Rmap.afterTimeRelative: " + getFullName() + " " +
			   getTrange() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Appends a child <code>Rmap</code> to this <code>Rmap</code>.
     * <p>
     * This method adds the input <code>Rmap</code> as a child of this
     * <code>Rmap</code>.  It always adds the input entry to the end of the
     * children list.  Since it is unchecked, the caller is required to ensure
     * that children are added in sorted order (for example, when they are
     * being read from an input stream).
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the new child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a
     *		  child of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
     *			<code>RmapVector()</code>.
     * 09/25/2000  INB	Created.
     *
     */
    final void appendChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	childI.setParent(this);
	if (getChildren() == null) {
	    setChildren(new RmapVector(1));
	}
	getChildren().addElement(childI);
    }

    /**
     * Appends a member <code>Rmap</code> to this <code>Rmap</code>.
     * <p>
     * This method adds the input <code>Rmap</code> as a member of this
     * <code>Rmap</code>.  It always adds the input entry to the end of the
     * members list.  Since it is unchecked, the caller is required to ensure
     * that members are added in sorted order (for example, when they are
     * being read from an input stream).
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI  the new member <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a
     *		  member of another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>RmapVector(1)</code> rather than
     *			<code>RmapVector()</code>.
     * 09/25/2000  INB	Created.
     *
     */
    final void appendMember(Rmap memberI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	memberI.setParent(this);
	if (getMembers() == null) {
	    setMembers(new RmapVector(1));
	}
	getMembers().addElement(memberI);
    }

    /**
     * Builds a request for data for the requested channels ending at the
     * end of this <code>Rmap</code> (which is before the time reference).
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
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
     * @see #afterTimeRelative(com.rbnb.api.TimeRelativeRequest requestI,com.rbnb.api.RequestOptions roI)
     * @since V2.2
     * @version 04/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/30/2004  INB	Use the index of the for loop rather than 0 in the
     *			search that checks each of the children.
     * 02/26/2004  INB	If we get a name failures, then move to the previous
     *			child and we want to do the search backwards (we're
     *			looking for the last information, not the first).
     * 12/11/2003  INB	Added <code>RequestOptions</code> to
     *			<code>TimeRelativeRequest</code> handling to allow the
     *			code to do the right thing for
     *			<code>extendStart</code>.
     * 12/09/2003  INB	Don't subtract out the <code>TimeRelativeRequest</code>
     *			duration.
     * 11/04/2003  INB	Created.
     *
     */
    TimeRelativeResponse beforeTimeRelative(TimeRelativeRequest requestI,
					    RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("Rmap.beforeTimeRelative: " + getFullName() + " " +
			   getTrange() +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	TimeRelativeResponse responseR = null;
	TimeRelativeRequest request = requestI;
	if (getName() != null) {
	    // With a name, we need to ensure that we've got a good one.
	    TimeRelativeChannel trc = (TimeRelativeChannel)
		request.getByChannel().firstElement();
	    String requestName = trc.nextNameLevel(request.getNameOffset());
	    int direction = compareNames(requestName);

	    if (direction > 0) {
		responseR = new TimeRelativeResponse();
		responseR.setStatus(-3);
		return (responseR);
	    } else if (direction < 0) {
		responseR = new TimeRelativeResponse();
		responseR.setStatus(3);
		return (responseR);
	    }
	    request = new TimeRelativeRequest();
	    request.setNameOffset
		(requestI.getNameOffset() +
		 requestName.length() +
		 1);
	    request.setRelationship
		(requestI.getRelationship());
	    request.setTimeRange(requestI.getTimeRange());
	    request.setByChannel(requestI.getByChannel());
	}

	// Locate the <code>TimeRange</code>.  This will be the last
	// <code>TimeRange</code> going down the last child path (unless we
	// get name problems).  We also want to locate the corresponding
	// <code>DataBlock</code> when working on <code>extendStart</code>
	// requests.
	if (getTrange() == null) {
	    for (int idx = getNchildren() - 1,
		     endIdx = -1;
		 idx > endIdx;
		 --idx) {
		responseR = getChildAt(idx).beforeTimeRelative(request,roI);
		if (responseR.getStatus() != -3) {
		    // This child is not after the name, so it is as good as
		    // anything.
		    break;
		}
	    }

	} else if ((roI != null) && roI.getExtendStart()) {
	    // For <code>extendStart</code>, we need to find the start time of
	    // the last point.
	    int nPoints = 0;

	    responseR = new TimeRelativeResponse();
	    responseR.setStatus(0);
	    if ((getTrange().getNptimes() > 1) ||
		(getTrange().getDuration() == 0.)) {
		// If the <code>TimeRange</code> represents either a single
		// time or has multiple times, then we've got enough
		// informnation to get a meaningful result.
		nPoints = getTrange().getNptimes();

	    } else {
		// Otherwise, we need to find out how many data points this
		// represents.  To do that, we need to determine how many
		// data points each channel has.

		// If the children of this <code>Rmap</code> have names, then
		// we need to split the request into subsets by the next part
		// of the channel name.  This ensures that we can perform
		// binary searches for names as well as times from here on
		// down.

		// The <code>Rmap</code> sort is such that named
		// <code>Rmaps</code> definitely follow all unnamed ones, so we
		// only need to check the last one.
		java.util.Vector requests;

		if ((getNchildren() == 0) ||
		    (getChildAt(getNchildren() - 1).getName() == null)) {
		    // No names at the next level, so we can simply make a list
		    // consisting of the input request.
		    requests = new java.util.Vector();
		    requests.addElement(requestI);

		} else {
		    // With names at the next level, we need to split the
		    // request.
		    requests = requestI.splitByNameLevel();
		}

		java.util.Hashtable pointsPerChannel =
		    new java.util.Hashtable();
		int rnPoints;
		request = (TimeRelativeRequest) requests.firstElement();
		nPoints = findPointsTimeRelative(request,pointsPerChannel);
		for (int idx = 1;
		     (nPoints != -2) && (idx < requests.size());
		     ++idx) {
		    request = (TimeRelativeRequest) requests.elementAt(idx);
		    rnPoints = findPointsTimeRelative(request,
						      pointsPerChannel);
		    if (rnPoints != nPoints) {
			nPoints = -2;
		    }
		}

		if (nPoints == -2) {
		    // If we cannot find a consistent number of points,
		    // then force a split of the channels.
		    responseR.setStatus(-2);
		    
		} else if (nPoints == -1) {
		    // If we cannot find any information, then we need to
		    // move on.
		    responseR.setStatus(1);
		}
	    }

	    if (responseR.getStatus() == 0) {
		responseR = getTrange().beforeTimeRelative
		    (request,
		     roI,
		     nPoints);
	    }

	} else {
	    responseR = new TimeRelativeResponse();
	    responseR.setStatus(0);
	    responseR.setTime
		(getTrange().getPtimes()[getTrange().getNptimes() - 1] +
		 getTrange().getDuration());
	    responseR.setInvert(true);
	}

	/*
	System.err.println("Rmap.beforeTimeRelative: " + getFullName() + " " +
			   getTrange() +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Returns the children of this <code>Rmap</code> in a string
     * representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indentI the indentation string to use.
T     * @return the string representation.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/25/2001  INB	Created.
     *
     */
    String childrenToString(String indentI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String stringR = "";

	for (int idx = 0, eIdx = getNchildren();
	     idx < eIdx;
	     ++idx) {
	    stringR +=
		"\n" + getChildAt(idx).toString(false,indentI + "..");
	}

	return (stringR);
    }

    /**
     * Clears the <code>TimeRanges</code> for this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timesI   clear times?
     * @param framesI  clear frames?
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
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    final void clearTranges(boolean timesI,boolean framesI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (timesI) {
	    setTrange(null);
	}
	if (framesI) {
	    setFrange(null);
	}
	for (int idx = 0, eIdx = getNchildren();
	     idx < eIdx;
	     ++idx) {
	    getChildAt(idx).clearTranges(timesI,framesI);
	}
	for (int idx = 0, eIdx = getNmembers();
	     idx < eIdx;
	     ++idx) {
	    getMemberAt(idx).clearTranges(timesI,framesI);
	}
    }

    /**
     * Clones this <code>Rmap</code>.
     * <p>
     * This method clones the member and child <code>Rmaps</code>, the frame
     * and time <code>TimeRanges</code>, and the <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public Object clone() {
	Rmap clonedR = (Rmap) super.clone();

	try {
	    if (clonedR != null) {
		clonedR.setParent(null);
		if (getMembers() != null) {
		    clonedR.setMembers((RmapVector) getMembers().clone());
		    for (int idx = 0, eIdx = clonedR.getNmembers();
			 idx < eIdx;
			 ++idx) {
			Rmap member = clonedR.getMemberAt(idx);
			member.setParent(clonedR);
		    }
		}
		if (getChildren() != null) {
		    clonedR.setChildren((RmapVector) getChildren().clone());
		    for (int idx = 0, eIdx = clonedR.getNchildren();
			 idx < eIdx;
			 ++idx) {
			Rmap child = clonedR.getChildAt(idx);
			child.setParent(clonedR);
		    }
		}

		if (getDblock() != null) {
		    clonedR.setDblock((DataBlock) getDblock().clone());
		}

		if (getTrange() != null) {
		    clonedR.setTrange((TimeRange) getTrange().clone());
		}

		if (getFrange() != null) {
		    clonedR.setFrange((TimeRange) getFrange().clone());
		}
	    }
	} catch (com.rbnb.api.AddressException e) {
	    clonedR = null;
	} catch (com.rbnb.api.SerializeException e) {
	    clonedR = null;
	} catch (java.io.IOException e) {
	    clonedR = null;
	} catch (java.lang.InterruptedException e) {
	    clonedR = null;
	}

	return (clonedR);
    }

    /**
     * Collapses this <code>Rmap</code> hierarchy.
     * <p>
     * This method collapses and reorganizes the <code>Rmap</code> hierarchy
     * rooted at the input <code>Rmap</code> to produce the most efficient
     * structure possible, given reasonable time constraints.
     * <p>
     * The method recursively performs the following operations:
     * <p><ol>
     * <li>Collapses its member and child hierarchies,</li>
     * <li>Eliminates itself if it contains no useful information,</li>
     * <li>If there is a single child and no members, and that child contains
     *     only information not found in this <code>Rmap</code>, collapses out
     *     the child <code>Rmap</code>,</li>
     * <li>Eliminates redundant <code>TimeRanges</code> from its children
     *     children by moving the <code>TimeRange</code> to this
     *     <code>Rmap</code> if that won't change the meaning of the
     *     hierarchy, and</li>
     * <li>Eliminates redudant <code>Rmap</code> hierarchies if it is possible
     *     to move a single copy of their children to the current level and
     *     create groups using data pools.</li>
     * </ol><p>
     * Note that this method replaces elements of this <code>Rmap</code>
     * hierarchy in place. It does not copy information unless absolutely
     * necessary.
     * <p>
     * Note that the data pool creation logic is not completed. Look into this
     * further. It is turned off for now.
     * <p>
     *
     * @author Ian Brown
     *
     * @return keep this <code>Rmap</code>?
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2001  INB	Created.
     *
     */
    public final boolean collapse()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Collapse any group members.
	collapseMembers();

	// Collapse any children.
	collapseChildren();

	int nMem = getNmembers(),
	    nChl = getNchildren();
	if ((nMem == 0) && (nChl == 0) && (getDblock() == null)) {
	    // If there is no useful information in this <code>Rmap</code>,
	    // then eliminate it from its parent's list, unless it is an
	    // <code>EndOfStream</code>.
	    return (this instanceof EndOfStream);
	}

	if (nMem == 0) {
	    if (nChl == 1) {
		// With a single child, we may be able to collapse out a level
		// of <code>Rmap</code> hierarchy.
		Rmap child = getChildAt(0);

		if ((getParent() != null) &&
		    ((getName() == null) ||
		     ((getName() == null) !=
		      (child.getName() == null))) &&
		    ((getTrange() == null) ||
		     ((getTrange() == null) !=
		      (child.getTrange() == null))) &&
		    ((getFrange() == null) ||
		     ((getFrange() == null) !=
		      (child.getFrange() == null))) &&
		    ((getDblock() == null) ||
		     ((getDblock() == null) !=
		      (child.getDblock() == null)))) {
		    // If the single child contains only information missing
		    // from this <code>Rmap</code>, then we can collapse a
		    // level out.
		    removeChildAt(0);
		    if (child.getName() != null) {
			setName(child.getName());
		    }
		    if (child.getTrange() != null) {
			setTrange(child.getTrange());
		    }
		    if (child.getDblock() != null) {
			setDblock(child.getDblock());
		    }
		    Rmap entry;
		    int cLen = child.getNmembers();
		    for (int idx = 0; idx < cLen; ++idx) {
			entry = child.getMemberAt(0);
			child.removeMemberAt(0);
			addMember(entry);
		    }
		    cLen = child.getNchildren();
		    for (int idx = 0; idx < cLen; ++idx) {
			entry = child.getChildAt(0);
			child.removeChildAt(0);
			addChild(entry);
		    }
		}

	    } else if (nChl > 0) {
		Rmap child = getChildAt(0);

		if ((getTrange() == null) &&
		    (child.getTrange() != null)) {
		    // If there is some chance that we could inherit time
		    // information from our children, then try to do so.

		    if (child.getName() != null) {
			// If we have at least one named child, then we can
			// check for common times across the children. Each
			// child must be named and have the same
			// <code>TimeRange</code>.
			TimeRange ltRange = child.getTrange();
			String cName = child.getName();
			for (int idx = 1; idx < nChl; ++idx) {
			    child = getChildAt(idx);
			    if ((child.getName() == null) ||
				(child.compareNames(cName) == 0)) {
				ltRange = null;
				break;
			    }
			    TimeRange ntRange = child.getTrange();
			
			    if ((ntRange == null) ||
				(ntRange.compareTo(ltRange) != 0)) {
				ltRange = null;
				break;
			    }
			}

			if (ltRange != null) {
			    java.util.Vector oChildren = getChildren();
			    setChildren(null);

			    int offset = 0;
			    for (int idx = oChildren.size() - 1;
				 idx >= 0;
				 --idx) {
				child = (Rmap) oChildren.elementAt(idx);
				child.setTrange(null);
				child.setParent(null);
				addChild(child);
			    }
			    setTrange(ltRange);
			}
		    }
		}
	    }
	}

	return (true);
    }

    /**
     * Collapses the children <code>Rmaps</code> of this <code>Rmap</code>.
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
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/08/2001  INB	Created.
     *
     */
    private final void collapseChildren()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getNchildren() > 0) {
	    java.util.Vector oChildren = getChildren();
	    setChildren(null);

	    Rmap entry;
	    int nChl = 0;
	    for (int idx = 0,
		     endIdx = oChildren.size();
		 idx < endIdx;
		 ++idx) {
		entry = (Rmap) oChildren.elementAt(idx);
		if (entry.collapse()) {
		    if (getNchildren() == nChl) {
			addChild(entry);
		    }
		    ++nChl;
		}
	    }
	}
    }

    /**
     * Collapses the group member <code>Rmaps</code> of this <code>Rmap</code>.
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
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/08/2001  INB	Created.
     *
     */
    private final void collapseMembers()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getNmembers() > 0) {
	    java.util.Vector oMembers = getMembers();
	    setMembers(null);

	    Rmap entry;
	    int nMem = 0;
	    for (int idx = 0,
		     endIdx = oMembers.size();
		 idx < endIdx;
		 ++idx) {
		entry = (Rmap) oMembers.elementAt(idx);
		if (entry.collapse()) {
		    if (getNmembers() == nMem) {
			addMember(entry);
		    }
		    ++nMem;
		}
	    }
	}
    }

    /**
     * Combines reasons for not matching data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param firstI   the first reason for no match.
     * @param secondI  the second reason for no match.
     * @return the combined reason.
     * @since V2.0
     * @version 02/03/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/25/2001  INB	Created.
     *
     */
    final static byte combineReasons(byte firstI,byte secondI) {
	byte reasonR = MATCH_UNKNOWN;

	if (firstI == MATCH_UNKNOWN) {
	    reasonR = secondI;
	} else if (secondI == MATCH_UNKNOWN) {
	    reasonR = firstI;
	} else if (firstI == MATCH_ILLEGAL) {
	    reasonR = secondI;
	} else if (secondI == MATCH_ILLEGAL) {
	    reasonR = firstI;
	} else if (((firstI == MATCH_AFTER) &&
		    (secondI == MATCH_AFTERNAME)) ||
		   ((firstI == MATCH_AFTERNAME) &&
		    (secondI == MATCH_AFTER))) {
	    reasonR = MATCH_AFTER;
	} else if (((firstI == MATCH_BEFORE) &&
		    (secondI == MATCH_BEFORENAME)) ||
		   ((firstI == MATCH_BEFORENAME) &&
		    (secondI == MATCH_BEFORE))) {
	    reasonR = MATCH_BEFORE;
	} else if (firstI != secondI) {
	    reasonR = MATCH_NOINTERSECTION;
	} else {
	    reasonR = firstI;
	}

	return (reasonR);
    }

    /**
     * Compares the name of this <code>Rmap</code> to the name of the input
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>Rmap</code>.
     * @return the results of the comparison:
     *	       <br><ul>
     *	       <li><0 if this <code>Rmap</code> compares less than the
     *		   input,</li>
     *	       <li> 0 if this <code>Rmap</code> compares equal to the input,
     *		   and</li>
     *	       <li>>0 if this <code>Rmap</code> compares greater than the
     *		   input.</li>
     *	       </ul>
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    public final int compareNames(Rmap otherI) {
	return (compareNames(otherI.getName()));
    }

    /**
     * Compares the name of this <code>Rmap</code> to the input name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name to compare to.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Rmap</code> compares less than the input,
     *	       <p> 0 if this <code>Rmap</code> compares equal to the input, and
     *	       <p>>0 if this <code>Rmap</code> compares greater than the input.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    public final int compareNames(String nameI) {
	return (compareNames(getName(),nameI));
    }

    /**
     * Compares the input names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the first name.
     * @param name2I the second name.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Rmap</code> compares less than the input,
     *	       <p> 0 if this <code>Rmap</code> compares equal to the input, and
     *	       <p>>0 if this <code>Rmap</code> compares greater than the input.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    public final static int compareNames(String nameI,String name2I) {
	return ((nameI == null) ?
		((name2I == null) ? 0 : -1) :
		((name2I == null) ? 1 : nameI.compareTo(name2I)));
    }

    /**
     * Compares the sorting value of this <code>Rmap</code> to the input
     * sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * The sorting value for an <code>Rmap</code> is always itself. The
     * comparison is first by name, then by frame <code>TimeRange</code>, and
     * finally by <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier -- must be null.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Rmap</code> compares less than the input,
     *	       <p> 0 if this <code>Rmap</code> compares equal to the input, and
     *	       <p>>0 if this <code>Rmap</code> compares greater than the input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @exception java.lang.IllegalStateException
     *		  thrown if both the this <code>Rmap</code> and the input
     *		  <code>Rmap</code> are nameless and timeless.
     * @see #compareTo(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	if (sidI != null) {
	    // Only the null sort identifier is supported.
	    throw new com.rbnb.utility.SortException
		("The sort identifier for Rmaps must be null.");
	}

	if (this == otherI) {
	    // A <code>Rmap</code> is always equal to itself.
	    return (0);
	}

	// The input sorting value is just the other <code>Rmap</code>.
	Rmap other = (Rmap) otherI;
	int comparedR = compareNames(other);

	if (comparedR == 0) {
	    if (getFrange() == null) {
		comparedR = (other.getFrange() == null) ? 0 : -1;
	    } else if (other.getFrange() == null) {
		comparedR = 1;
	    } else {
		comparedR = getFrange().compareTo(other.getFrange());
	    }
	}
	if (comparedR == 0) {
	    if (getTrange() == null) {
		comparedR = (other.getTrange() == null) ? 0 : -1;
	    } else if (other.getTrange() == null) {
		comparedR = 1;
	    } else {
		comparedR = getTrange().compareTo(other.getTrange());
	    }
	}

	return (comparedR);
    }

    /**
     * Compares the input <code>Rmap</code> to this one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code> to compare.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Rmap</code> compares less than the input,
     *	       <p> 0 if this <code>Rmap</code> compares equal to the input, and
     *	       <p>>0 if this <code>Rmap</code> compares greater than the input.
     * @see #compareTo(Object,Object)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/22/2000  INB	Created.
     *
     */
    public int compareTo(Rmap rmapI) {
	int comparedR;

	if ((getName() != null) ||
	    (getTrange() != null) ||
	    (getFrange() != null)) {
	    // If this <code>Rmap</code> has a name, a frame index, or a time,
	    // we can use the <code>compareTo(Object,Object)</code> method.
	    try {
		comparedR = compareTo(null,rmapI);
	    } catch (com.rbnb.utility.SortException e) {
		throw new java.lang.InternalError();
	    }

	} else {
	    // Else, this <code>Rmap</code> comes first unless the input is
	    // also nameless and timeless.

	    if (rmapI.isNamelessTimeless()) {
		comparedR = 0;

	    } else {
		comparedR = -1;
	    }
	}

	return (comparedR);
    }

    /**
     * Creates an <code>Rmap</code> hierarchy from the input name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name.
     * @return the top of the <code>Rmap</code> hierarchy.
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
     *		  thrown if the creation is interrupted.
     * @since V2.0
     * @version 09/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    public final static Rmap createFromName(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (createFromName(nameI,null));
    }

    /**
     * Creates an <code>Rmap</code> hierarchy from the input name.
     * <p>
     * This method takes an input <code>Rmap</code> object and creates objects
     * of the same class. By default, it creates <code>Rmaps</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI   the name.
     * @param objectI the base object.
     * @return the top of the <code>Rmap</code> hierarchy.
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
     *		  thrown if the creation is interrupted.
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    final static Rmap createFromName(String nameI,Rmap objectI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;
	Class myClass = (objectI == null) ? null : objectI.getClass();

	try {
	    if (nameI == null) {
		// If there is no name, just create a nameless
		// <code>Rmap</code>.
		rmapR = ((objectI == null) ?
			 new Rmap() :
			 (Rmap) myClass.newInstance());

	    } else {
		// If there is a name, create an <code>Rmap</code> for each
		// separated piece of the name.
		Rmap lastRmap = null;
		boolean skipped = false;
		for (int last = 0,
			 next = nameI.indexOf(PATHDELIMITER);
		     last != nameI.length() + 1;
		     last = next + 1,
			 next = ((last == nameI.length()) ?
				 -1 :
				 nameI.indexOf(PATHDELIMITER,last))) {
		    if (next == -1) {
			next = nameI.length();
		    }

		    if (next == last) {
			skipped = true;
			continue;
		    }

		    // Grab the next piece of the name.
		    String name = nameI.substring(last,next);
		   if (!skipped &&
		       (rmapR == null) &&
		       !name.equals(".")) {
			Rmap child = ((objectI == null) ?
				      new Rmap() :
				      (Rmap) myClass.newInstance());
			child.setName(".");
			rmapR = child;
			lastRmap = rmapR;
		    }

		    // Create an <code>Rmap</code> using that piece.
		    Rmap child = ((objectI == null) ?
				  new Rmap() :
				  (Rmap) myClass.newInstance());
		    child.setName(name);

		    if (rmapR == null) {
			// If there is no return <code>Rmap</code>, use the
			// latest <code>Rmap</code> created.
			lastRmap =
			    rmapR = child;

		    } else {
			// If there is a return <code>Rmap</code>, add the
			// latest one to the end of the hierarchy.
			lastRmap.appendChild(child);
			lastRmap = child;
		    }
		}
	    }

	} catch (java.lang.InstantiationException e) {
	} catch (java.lang.IllegalAccessException e) {
	}

	return (rmapR);
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
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultStandardParameters(otherI,seenI);
    }

    /**
     * Defaults for standard parameters.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code> into
     * this one.
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
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2001  INB	Created.
     *
     */
    final void defaultStandardParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (otherI != null) {
	    int eIdx;
	    if (((seenI == null) || !seenI[PAR_CHL]) &&
		((eIdx = otherI.getNchildren()) > 0)) {
		for (int idx = 0; idx < eIdx; ++idx) {
		    Rmap oChild = otherI.getChildAt(idx),
			child = oChild.newInstance();
		    child.defaultParameters(oChild,null);
		    appendChild(child);
		}
	    }

	    if (((seenI == null) || !seenI[PAR_FRG]) &&
		(otherI.getFrange() != null)) {
		setFrange(otherI.getFrange().duplicate());
	    }

	    if (((seenI == null) || !seenI[PAR_GRP]) &&
		((eIdx = otherI.getNmembers()) > 0)) {
		for (int idx = 0; idx < eIdx; ++idx) {
		    try {
			Rmap oMember = otherI.getMemberAt(idx),
			     member = (Rmap) oMember.getClass().newInstance();
			member.defaultParameters(oMember,null);
			appendMember(member);
		    } catch (java.lang.IllegalAccessException e) {
			throw new java.lang.InternalError();
		    } catch (java.lang.InstantiationException e) {
			throw new java.lang.InternalError();
		    }
		}
	    }

	    if ((seenI == null) || !seenI[PAR_NAM]) {
		setName(otherI.getName());
	    }

	    if (((seenI == null) || !seenI[PAR_TRG]) &&
		(otherI.getTrange() != null)) {
		setTrange(otherI.getTrange().duplicate());
	    }

	    if (((seenI == null) || !seenI[PAR_DBK]) &&
		(otherI.getDblock() != null)) {
		setDblock(new DataBlock());
		getDblock().defaultParameters(otherI.getDblock(),null);
	    }
	}
    }

    /**
     * Copies this <code>Rmap</code> without copying the data.
     * <p>
     * This method copies the member and child <code>Rmaps</code>, the frame
     * and time <code>TimeRanges</code>, and the <code>DataBlock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2001  INB	Created.
     *
     */
    Rmap duplicate() {
	Rmap clonedR = (Rmap) super.clone();

	try {
	    if (clonedR != null) {
		clonedR.setParent(null);
		if (getMembers() != null) {
		    clonedR.setMembers(getMembers().duplicate());
		    for (int idx = 0, eIdx = clonedR.getNmembers();
			 idx < eIdx;
			 ++idx) {
			Rmap member = clonedR.getMemberAt(idx);
			member.setParent(clonedR);
		    }
		}
		if (getChildren() != null) {
		    clonedR.setChildren(getChildren().duplicate());
		    for (int idx = 0, eIdx = clonedR.getNchildren();
			 idx < eIdx;
			 ++idx) {
			Rmap child = clonedR.getChildAt(idx);
			child.setParent(clonedR);
		    }
		}

		if (getDblock() != null) {
		    clonedR.setDblock(getDblock().duplicate());
		}

		if (getTrange() != null) {
		    clonedR.setTrange(getTrange().duplicate());
		}

		if (getFrange() != null) {
		    clonedR.setFrange(getFrange().duplicate());
		}
	    }
	} catch (com.rbnb.api.AddressException e) {
	    clonedR = null;
	} catch (com.rbnb.api.SerializeException e) {
	    clonedR = null;
	} catch (java.io.IOException e) {
	    clonedR = null;
	} catch (java.lang.InterruptedException e) {
	    clonedR = null;
	}

	return (clonedR);
    }

    /**
     * Extracts the time and data for a particular channel.
     * <p>
     * This method requires that all of the data for the channel be of a
     * single data type. The returned result is an array of the data type,
     * returned as a single object.
     * <p>
     * If there is no time or data information available for the specified
     * channel, an empty <code>DataArray</code> object is returned, with null
     * time and data fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @param chanNameI  the fully qualified name of the channel.
     * @return a <code>DataArray</code> object containing the extracted times
     *	       and data.
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
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final DataArray extract(String chanNameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (extract(chanNameI,false,true,true));
    }

    /**
     * Extracts the frame indexes, time, and/or data for a particular channel.
     * <p>
     * This method requires that all of the data for the channel be of a
     * single data type. The returned result is an array of the data type,
     * returned as a single object.
     * <p>
     * If there is no frame, time, or data information available for the
     * specified channel, an empty <code>DataArray</code> object is returned,
     * with null frame, time, and data fields.
     * <p>
     *
     * @author Ian Brown
     *
     * @param chanNameI  the fully qualified name of the channel.
     * @param frameFlagI return the extracted frame indexes?
     * @param timeFlagI	 return the extacted times?
     * @param dataFlagI	 return the extraced data?
     * @return a <code>DataArray</code> object containing the extracted times
     *	       and data.
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
     * @since V2.0
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Firewalled against folders (names ending in '/').
     * 11/30/2000  INB	Created.
     *
     */
    public final DataArray extract(String chanNameI,
				   boolean frameFlagI,
				   boolean timeFlagI,
				   boolean dataFlagI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isImplemented(OPR_EXTRACTION)) {
	    throw new java.lang.IllegalArgumentException
		("Extraction is not supported by " + this);
	}

	DataArray arrayR = null;
	if (chanNameI.endsWith("/")) {
	    // For folders, we always return an empty <code>DataArray</code>.
	    arrayR = new DataArray();

	} else {
	    // Build an <code>Rmap</code> hierarchy for the input channel name.
	    Rmap request = createFromName(chanNameI);

	    // Build an <code>RmapExtractor</code> to extract the information.
	    RmapExtractor extractor = new RmapExtractor(request,
							false,
							frameFlagI,
							timeFlagI,
							dataFlagI);
	    arrayR = (DataArray) extractor.extract(this);
	}

	return (arrayR);
    }

    /**
     * Extracts the names of folders from this <code>Rmap</code> hierarchy.
     * <p>
     * Folders are defined as named <code>Rmaps</code> with no children.
     * <p>
     *
     * @author Ian Brown
     *
     * @return array of the names found.
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
     * @since V2.1
     * @version 05/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2003  INB	Created from <code>extractFoldersAndChannels</code>.
     *
     */
    public final String[] extractFolders()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isImplemented(OPR_EXTRACTION)) {
	    throw new java.lang.IllegalArgumentException
		("Extraction is not supported by " + this);
	}

	// Vector to hold the names.
	SortedStrings nameList = new SortedStrings();

	// Add the names of the <code>Folders</code>.
	addFolderNames(null,nameList);

	// Convert the names to an array.
	String[] namesR = nameList.elements();
	return (namesR);
    }

    /**
     * Extracts the names of channels and folders from this <code>Rmap</code>
     * hierarchy.
     * <p>
     * Channels are defined as names with data (equivalent to the results of
     * <code>extractNames</code>). Folders are defined as named
     * <code>Rmaps</code> with no children.
     * <p>
     *
     * @author Ian Brown
     *
     * @return array of the names found.
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
     * @since V2.1
     * @version 05/06/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/06/2003  INB	Created from <code>extractNames</code>.
     *
     */
    public final String[] extractFoldersAndChannels()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isImplemented(OPR_EXTRACTION)) {
	    throw new java.lang.IllegalArgumentException
		("Extraction is not supported by " + this);
	}

	// Vector to hold the names.
	SortedStrings nameList = new SortedStrings();

	// Add the names of all of the <code>Rmaps</code> in this hierarchy
	// that have data associated with them.
	addNamesWithData(null,nameList);

	// Add the names of the <code>Folders</code>.
	addFolderNames(null,nameList);

	// Convert the names to an array.
	String[] namesR = nameList.elements();
	return (namesR);
    }

    /**
     * Extracts a data <code>Rmap</code> for this level of the
     * <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	  the request <code>Rmap</code>.
     * @param fRequestI	  the request frame <code>TimeRange</code>.
     * @param fDataI	  the data frame <code>TimeRange</code>.
     * @param keepFramesI keep the frame <code>TimeRange</code> from this
     *			  <code>Rmap</code>?
     * @param tRequestI   the request <code>TimeRange</code>.
     * @param tDataI	  the data <code>TimeRange</code>.
     * @param keepTimesI  keep the <code>TimeRange</code> from this
     *			  <code>Rmap</code>?
     * @param needDataI   do we need the data?
     * @return the resulting <code>Rmap</code>.
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
     *		  thrown if this code cannot perform the extraction.
     * @since V2.0
     * @version 10/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/19/2000 INB	Created.
     *
     */
    final Rmap extractData
	(Rmap requestI,
	 TimeRange fRequestI,
	 TimeRange fDataI,
	 boolean keepFramesI,
	 TimeRange tRequestI,
	 TimeRange tDataI,
	 boolean keepTimesI,
	 boolean needDataI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = newInstance();

	if ((fRequestI == null) && (tRequestI == null)) {
	    if (keepFramesI && (getFrange() != null)) {
		rmapR.setFrange((TimeRange) getFrange().clone());
	    }
	    if (keepTimesI && (getTrange() != null)) {
		rmapR.setTrange((TimeRange) getTrange().clone());
	    }
	    if (needDataI && (getDblock() != null)) {
		rmapR.setDblock((DataBlock) getDblock().clone());
	    }

	} else {
	    TimeRange request = null,
		      from1 = null,
		      from2 = null,
		      to1 = null,
		      to2 = null;

	    if (fRequestI != null) {
		// If the request is for frames, then extract the appropriate
		// ones.
		request = fRequestI;
		from1 = fDataI;
		from2 = keepTimesI ? getTrange() : null;
		rmapR.setFrange(to1 = new TimeRange());
		if (keepTimesI && (from2 != null)) {
		    rmapR.setTrange(to2 = new TimeRange());
		}

	    } else if (tRequestI != null) {
		// If the request is for times, then extract the appropriate
		// ones.
		request = tRequestI;
		from1 = tDataI;
		from2 = keepFramesI ? getFrange() : null;
		rmapR.setTrange(to1 = new TimeRange());
		if (keepFramesI && (from2 != null)) {
		    rmapR.setFrange(to2 = new TimeRange());
		}
	    }

	    if (!needDataI || (getDblock() == null)) {
		if (!from1.extractRequest(request,from2,to1,to2)) {
		    rmapR = null;
		}
	    } else {
		rmapR.setDblock(new DataBlock());
		if (!from1.extractRequestWithData(request,
						  from2,
						  getDblock(),
						  to1,
						  to2,
						  rmapR.getDblock())) {
		    rmapR = null;
		}
	    }
	}

	if ((rmapR != null) && (rmapR.getFrange() != null)) {
	    // We never keep the frame indexes.  No one needs them downstream
	    // of this, so don't hold onto them.
	    rmapR.setFrange(null);
	}

	/*
	System.err.println("\nExtract:\n" +
			   requestI +
			   "\nFrames Request: " + fRequestI +
			   "\nFrames Data: " + fDataI +
			   "\nKeep frames: " + keepFramesI +
			   "\nTime Request: " + tRequestI +
			   "\nTime Data: " + tDataI +
			   "\nKeep Times: " + keepTimesI +
			   "\nNeed Data: " + needDataI +
			   "\nFrom:\n" + this +
			   "\nEquals:\n" + rmapR);
	*/

	return (rmapR);
    }

    /**
     * Extracts a data <code>Rmap</code> for this level and the underlying
     * levels of the <code>Rmap</code> hierarchy.
     * <p>
     * This method is called for the <code>Rmap</code> that contains the data
     * payload.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	  the request <code>Rmap</code>.
     * @param fRequestI	  the request frame <code>TimeRange</code>.
     * @param fDataI	  the data frame <code>TimeRange</code>.
     * @param keepFramesI keep the frame <code>TimeRange</code> from this
     *			  <code>Rmap</code>?
     * @param tRequestI   the request <code>TimeRange</code>.
     * @param tDataI	  the data <code>TimeRange</code>.
     * @param keepTimesI  keep the <code>TimeRange</code> from this
     *			  <code>Rmap</code>?
     * @param nextIdxI	  the next index into the <code>Rmap</code> chains.
     * @param requestsI   the list of requests.
     * @param dRmapsI     the list of data <code>Rmaps</code>.
     * @return the resulting <code>Rmap</code>.
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
     *		  thrown if this code cannot perform the extraction.
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001 INB	Created.
     *
     */
    final Rmap extractMultiData
	(Rmap requestI,
	 TimeRange fRequestI,
	 TimeRange fDataI,
	 boolean keepFramesI,
	 TimeRange tRequestI,
	 TimeRange tDataI,
	 boolean keepTimesI,
	 int nextIdxI,
	 java.util.Vector requestsI,
	 java.util.Vector dRmapsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = extractData(requestI,
				 fRequestI,
				 fDataI,
				 keepFramesI,
				 tRequestI,
				 tDataI,
				 keepTimesI,
				 true);

	if (rmapR != null) {
	    rmapR.extractSubData(fRequestI,
				 false,
				 tRequestI,
				 false,
				 nextIdxI,
				 requestsI,
				 dRmapsI);
	}
	    
	return (rmapR);
    }

    /**
     * Extracts a data <code>Rmap</code> for this level and the underlying
     * levels of the <code>Rmap</code> hierarchy.
     * <p>
     * This method is called for <code>Rmaps</code> that have been
     * extracted. It works on the next level of the <code>Rmap</code> chain.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fRequestI	  the request frame <code>TimeRange</code>.
     * @param keepFramesI keep the frame <code>TimeRange</code> from this
     *			  <code>Rmap</code>?
     * @param tRequestI   the request <code>TimeRange</code>.
     * @param keepTimesI  keep the <code>TimeRange</code> from this
     *			  <code>Rmap</code>?
     * @param nextIdxI	  the next index into the <code>Rmap</code> chains.
     * @param requestsI   the list of requests.
     * @param dRmapsI     the list of data <code>Rmaps</code>.
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
     *		  thrown if this code cannot perform the extraction.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001 INB	Created.
     *
     */
    final void extractSubData
	(TimeRange fRequestI,
	 boolean keepFramesI,
	 TimeRange tRequestI,
	 boolean keepTimesI,
	 int nextIdxI,
	 java.util.Vector requestsI,
	 java.util.Vector dRmapsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (nextIdxI < requestsI.size()) {
	    Rmap lRequest = (Rmap) requestsI.elementAt(nextIdxI),
		 dRmap = (Rmap) dRmapsI.elementAt(nextIdxI);

	    if (dRmap.getDblock() == null) {
		throw new java.lang.IllegalStateException
		    ("Cannot extract from a data pool for an Rmap that does " +
		     "not have a data reference.\n" + dRmap);
	    }

	    DataBlock wdBlock = getDblock();
	    setDblock(null);
	    int wdPts = wdBlock.getNpts();

	    DataBlock ptDblock = new DataBlock();
	    ptDblock.setData(wdBlock.getData());
	    ptDblock.setNpts(dRmap.getDblock().getNpts());
	    ptDblock.setPtsize(dRmap.getDblock().getPtsize());
	    ptDblock.setDtype(dRmap.getDblock().getDtype());
	    ptDblock.setMIMEType(dRmap.getDblock().getMIMEType());
	    ptDblock.setWorder((dRmap.getDblock().getWorder() == 0) ?
			       wdBlock.getWorder() :
			       dRmap.getDblock().getWorder());
	    ptDblock.setIndivFlg(dRmap.getDblock().getIndivFlg());
	    ptDblock.setStride(dRmap.getDblock().getStride());

	    int onPts = dRmap.getDblock().getNpts();
	    int offset = wdBlock.getOffset() + dRmap.getDblock().getOffset();

	    TimeRange fromRequest,
		      fromThis,
		      fromOther,
		      fromdRmap,
		      fromdRmapOther;
	    boolean keepOther;
	    if (fRequestI != null) {
		fromRequest = fRequestI;
		fromThis = getFrange();
		fromOther = getTrange();
		fromdRmap = dRmap.getFrange();
		fromdRmapOther = dRmap.getTrange();
		keepOther = keepTimesI && (getTrange() != null);
	    } else {
		fromRequest = tRequestI;
		fromThis = getTrange();
		fromOther = getFrange();
		fromdRmap = dRmap.getTrange();
		fromdRmapOther = dRmap.getFrange();
		keepOther = keepFramesI && (getFrange() != null);
	    }
	    double startRequest = fromRequest.getTime(),
		   endRequest =	startRequest + fromRequest.getDuration(),
		   baseTime = (fromdRmap == null) ? fromThis.getTime() : 0.,
		   baseTimeOther = (((fromOther == null) ||
				     (fromdRmapOther == null)) ?
				    0 :
				    fromOther.getTime());


	    TimeRange request = null,
		      from1 = null,
		      from2 = null;
	    for (int idx = 0; idx < wdPts; ++idx) {
		TimeRange requestT = null,
			  fromT = null,
			  fromO = null;

		double startTime = fromThis.getPointTime(idx,wdPts),
		       endTime;
		if (fromdRmap == null) {
		    if (fromThis.getNptimes() == 1) {
			endTime = startTime + fromThis.getDuration()/wdPts;
		    } else {
			endTime = startTime + fromThis.getDuration();
		    }

		} else {
		    startTime += fromdRmap.getTime();
		    if (fromdRmap.getNptimes() == 1) {
			endTime = startTime + fromdRmap.getDuration();
		    } else {
			endTime =
			    startTime +
			    fromdRmap.getPtimes()[fromdRmap.getNptimes() - 1] +
			    fromdRmap.getDuration();
		    }
		}

		fromT = new TimeRange(startTime - baseTime,
				      endTime - startTime);
		if ((startRequest <= startTime) && (endRequest >= endTime)) {
		    requestT = (TimeRange) fromT.clone();
		} else {
		    double startV = Math.max(startTime,startRequest);
		    requestT = new TimeRange
			(startV - baseTime,
			 Math.min(endTime,endRequest) - startV);
		}

		if (fromOther != null) {
		    startTime = fromOther.getPointTime(idx,wdPts);
		    if (fromdRmapOther == null) {
			if (fromOther.getNptimes() == 1) {
			    endTime = (startTime +
				       fromOther.getDuration()/wdPts);
			} else {
			    endTime = startTime + fromOther.getDuration();
			}
		    } else {
			startTime += fromdRmapOther.getTime();
			if (fromdRmapOther.getNptimes() == 1) {
			    endTime = startTime + fromdRmapOther.getDuration();
			} else {
			    endTime =
				startTime +
				fromdRmapOther.getPtimes()
				[fromdRmapOther.getNptimes() - 1] +
				fromdRmapOther.getDuration();
			}
		    }
		    fromO = new TimeRange(startTime - baseTime,
					  endTime - startTime);
		}

		if ((from1 != null) && (requestT.compareTo(fromT) != 0)) {
		    Rmap child = dRmap.extractSubRmap(request,
						      from1,
						      from2,
						      ptDblock,
						      (fRequestI != null),
						      keepOther);

		    if (child != null) {
			addChild(child);
		    }
		    from1 = null;
		}

		if (from1 == null) {
		    request = requestT;
		    from1 = fromT;
		    from2 = fromO;
		    ptDblock.setNpts(onPts);
		    ptDblock.setOffset(offset);
		} else {
		    request.setDuration(request.getDuration() +
					requestT.getDuration());
		    from1.setDuration(from1.getDuration() +
				      fromT.getDuration());
		    if (from2 != null) {
			from2.setDuration(from2.getDuration() +
					  fromO.getDuration());
		    }
		    ptDblock.setNpts(ptDblock.getNpts() + onPts);
		}

		if ((idx == wdPts - 1) || (requestT.compareTo(fromT) != 0)) {
		    Rmap child = dRmap.extractSubRmap(request,
						      from1,
						      from2,
						      ptDblock,
						      (fRequestI != null),
						      keepOther);

		    if (child != null) {
			addChild(child);
			child.extractSubData
			    ((fRequestI == null) ? null : request,
			     keepFramesI,
			     (tRequestI == null) ? null : request,
			     keepTimesI,
			     nextIdxI + 1,
			     requestsI,
			     dRmapsI);
			from1 = null;
		    }
		}


		offset = wdBlock.getStride();
	    }
	}

	return;
    }

    /**
     * Extracts information from a <code>DataBlock</code> based on a
     * data reference into a data pool.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	 the request <code>TimeRange</code>.
     * @param fromI	 the <code>TimeRange</code> from which to perform the
     *			 extraction.
     * @param fromOtherI the other <code>TimeRange</code> from which to get
     *			 information.
     * @param fromdBI    the <code>DataBlock</code> to extract the data from.
     * @param framesI    is the primary extraction frames rather than time??
     * @param keepOtherI keep the other information?
     * @return the extracted child <code>Rmap</code>.
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
     *		  thrown if this code cannot perform the extraction.
     * @since V2.0
     * @version 10/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/03/2001  INB	Created.
     *
     */
    private final Rmap extractSubRmap(TimeRange requestI,
				      TimeRange fromI,
				      TimeRange fromOtherI,
				      DataBlock fromdBI,
				      boolean framesI,
				      boolean keepOtherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap childR = newInstance();
	TimeRange to1 = null,
		  to2 = null;
	if (framesI) {
	    childR.setFrange(to1 = new TimeRange());
	    if (keepOtherI) {
		childR.setTrange(to2 = new TimeRange());
	    }
	} else {
	    childR.setTrange(to1 = new TimeRange());
	    if (keepOtherI) {
		childR.setFrange(to2 = new TimeRange());
	    }
	}
	childR.setDblock(new DataBlock());

	if (!fromI.extractRequestWithData(requestI,
					  fromOtherI,
					  fromdBI,
					  to1,
					  to2,
					  childR.getDblock())) {
	    childR = null;
	}

	if ((childR != null) && (childR.getFrange() != null)) {
	    // We never keep the frame indexes.  No one needs them downstream
	    // of this, so don't hold onto them.
	    childR.setFrange(null);
	}

	return (childR);
    }

    /**
     * Extracts the names from this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return array of the names found.
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
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    public final String[] extractNames()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!isImplemented(OPR_EXTRACTION)) {
	    throw new java.lang.IllegalArgumentException
		("Extraction is not supported by " + this);
	}

	// Vector to hold the names.
	SortedStrings nameList = new SortedStrings();

	// Add the names of all of the <code>Rmaps</code> in this hierarchy
	// that have data associated with them.
	addNamesWithData(null,nameList);

	// Convert the names to an array.
	String[] namesR = nameList.elements();
	return (namesR);
    }

    /**
     * Extracts <code>Rmaps</code> from this <code>Rmap</code> that match the
     * input request <code>Rmap</code>.
     * <p>
     * This method assumes that the result will be another request and does not
     * require that data be seen in the this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI	 the request <code>Rmap</code>.
     * @return the matched <code>Rmap</code> hierarchy.
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
     *		  thrown if the information matching the request cannot be
     *		  extracted for any reason.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2001  INB	Created.
     *
     */
    Rmap extractNewRequest(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Build an <code>RmapExtractor</code> to extract the information.
	RmapExtractor extractor = new RmapExtractor(requestI,
						    true,
						    true,
						    false,
						    false);

	Rmap extractedR = ((Rmap) extractor.extract(this));

	return (extractedR);
    }

    /**
     * Extracts a new <code>DataRequest</code> for this level of the
     * <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the request <code>Rmap</code>.
     * @param referenceI  the reference <code>DataRequest</code>.
     * @param lTrangeI    the last <code>TimeRange</code> seen.
     * @param lFrangeI    the last frame <code>TimeRange</code> seen.
     * @return the resulting <code>Rmap</code>.
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
     *		  thrown if this code cannot perform the extraction.
     * @since V2.0
     * @version 10/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/19/2000 INB	Created.
     *
     */
    final Rmap extractRequest
	(Rmap requestI,
	 DataRequest referenceI,
	 TimeRange lTrangeI,
	 TimeRange lFrangeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap workWith = this;
	DataRequest rmapR,
	    	    dr;

	dr =
	    rmapR = new DataRequest();
	rmapR.setName(getName());
	TimeRange pTrange = lTrangeI,
		  pFrange = lFrangeI;

	// Determine where to get the time and frame information.
	if (getTrange() != null) {
	    pTrange = getTrange();
	}
	if (getFrange() != null) {
	    pFrange = getFrange();
	}

	if (this instanceof SourceHandler) {
	    RBO rbo = (RBO) this;
	    workWith = new Registration();
	    workWith.setTrange(rbo.getRegistered().getTrange());
	    workWith.setFrange(rbo.getRegistered().getFrange());
	    rmapR = new DataRequest();
	    rmapR.addChild(dr);
	    if (workWith.getTrange() != null) {
		pTrange = workWith.getTrange();
	    }
	    if (workWith.getFrange() != null) {
		pFrange = workWith.getFrange();
	    }
	}

	boolean direction =
	    ((referenceI.getReference() == DataRequest.AFTER) ||
	     (referenceI.getReference() == DataRequest.MODIFIED) ||
	     (referenceI.getReference() == DataRequest.NEWEST));
	if ((requestI.getTrange() != null) && (pTrange != null)) {
	    // If both the source and the request have
	    // <code>TimeRanges</code>, then combine the two.
	    double start = 0.,
		offset = 0.,
		duration = requestI.getTrange().getDuration();
	    double[] times;
	    int nTimes = requestI.getTrange().getNptimes();

	    if (referenceI.getReference() == DataRequest.AFTER) {
		// An "AFTER" request matches newest data that is strictly
		// after the specified time.
		times = new double[1];
		times[0] = Math.max((pTrange.getTime() +
				     pTrange.getDuration() -
				     duration),
				    requestI.getTrange().getTime());

	    } else if (referenceI.getReference() == DataRequest.MODIFIED) {
		// A "MODIFIED" request matches newest data where at least some
		// is after the specified time.
		times = new double[1];
		double endTime = pTrange.getTime() + pTrange.getDuration();
		if (endTime <= requestI.getTrange().getTime()) {
		    times[0] = Double.MAX_VALUE;
		} else {
		    times[0] = endTime - duration;
		}

	    } else {
		if (referenceI.getReference() == DataRequest.OLDEST) {
		    start = pTrange.getTime();
		    if (requestI.getTrange().getPtimes() !=
			TimeRange.INHERIT_TIMES) {
			offset = requestI.getTrange().getTime();
		    }
		} else {
		    start = pTrange.getTime() + pTrange.getDuration();
		    if (requestI.getTrange().getPtimes() !=
			TimeRange.INHERIT_TIMES) {
			offset = -(requestI.getTrange().getTime() +
				   duration);
		    }
		}
		
		if (nTimes == 0) {
		    times = new double[1];
		    times[0] = start;
		} else {
		    times = new double[nTimes];
		    
		    for (int idx = 0; idx < nTimes; ++idx) {
			times[idx] = start + offset;
		    }
		}
	    }

	    dr.setTrange(new TimeRange(times,duration));
	    dr.getTrange().setDirection(direction);

	} else if ((requestI.getFrange() != null) && (pFrange != null)) {
	    // If both the source and the request have frame
	    // <code>TimeRanges</code>, then combine the two.
	    double start = 0.,
		offset = 0.,
		duration = requestI.getFrange().getDuration();
	    double[] frames;
	    int nFrames = requestI.getFrange().getNptimes();

	    if (referenceI.getReference() == DataRequest.AFTER) {
		// An "AFTER" request matches newest data that is strictly
		// after the specified time.
		frames = new double[1];
		frames[0] = (pFrange.getTime() +
			     pFrange.getDuration() -
			     duration);
		frames[0] = Math.max(frames[0],requestI.getFrange().getTime());

	    } else if (referenceI.getReference() == DataRequest.MODIFIED) {
		// A "MODIFIED" request matches newest data where at least some
		// is after the specified time.
		frames = new double[1];
		double endFrame = pFrange.getTime() + pFrange.getDuration();
		if (endFrame <= requestI.getFrange().getTime()) {
		    frames[0] = Double.MAX_VALUE;
		} else {
		    frames[0] = endFrame - duration;
		}

	    } else {
		if ((requestI.getFrange() != null) &&
		    (requestI.getFrange().getPtimes() !=
		     TimeRange.INHERIT_TIMES)) {
		    offset = requestI.getFrange().getTime();
		}
		if (referenceI.getReference() == DataRequest.OLDEST) {
		    start = pFrange.getTime();
		    if ((requestI.getFrange() != null) &&
			(requestI.getFrange().getPtimes() !=
			 TimeRange.INHERIT_TIMES)) {
			offset = requestI.getFrange().getTime();
		    }
		} else {
		    start = pFrange.getTime() + pFrange.getDuration();
		    if ((requestI.getFrange() != null) &&
			(requestI.getFrange().getPtimes() !=
			 TimeRange.INHERIT_TIMES)) {
			offset = -(requestI.getFrange().getTime() + duration);
		    }
		}

		if (nFrames == 0) {
		    frames = new double[1];
		    frames[0] = start;
		} else {
		    frames = new double[nFrames];

		    for (int idx = 0; idx < nFrames; ++idx) {
			frames[idx] = start + offset;
		    }
		}
	    }

	    dr.setFrange(new TimeRange(frames,duration));
	    dr.getFrange().setDirection(direction);

	} else {
	    // If the request doesn't have a <code>TimeRange</code>, then
	    // use the range from the source.
	    dr.setTrange(workWith.getTrange());
	    dr.setFrange(workWith.getFrange());
	}

	dr.setDomain(referenceI.getDomain());

	return (rmapR);
    }

    /**
     * Extracts <code>Rmaps</code> from this <code>Rmap</code> that match the
     * input request <code>Rmap</code>.
     * <p>
     * This is a complicated process that does what appears to be a relatively
     * simple task, namely finding the subset of the Rmap hiearchy headed by
     * this Rmap that matches the input request.  It is complicated by the fact
     * that Rmaps are a recursive structure that allows for inheritance of
     * information from the top down to the bottom.  In addition, the Rmap
     * design at the time that this code was written was such that no
     * assumptions could be made about the structure of any particular,
     * non-specialized Rmap.
     * <p>
     * The processing involves a number of different objects:
     * <p><ol>
     *    <li>Rmap and its subclasses (such as DataRequest, RBO, RingBuffer,
     *	      etc.),</li>
     *    <li>TimeRange,</li>
     *    <li>RmapExctractor, and</li>
     *    <li>RmapChain and its subclass ExtractedChain.</li>
     * </ol><p>
     * The process starts by creating an RmapExtractor, which acts as the
     * central dispatch and depository.  An ExtractedChain is created to start
     * the process of matching the source Rmap hierarchy against the request.
     * <p>
     * From there, the process proceeds to compare information in the source
     * Rmap hierarchy against the request, moving down the two hierarchies,
     * until a point in the source Rmap hierarchy is reached such that its
     * name, time, and data match a portion of the request hierarchy.  This is
     * a recursive search sequence that goes through the following:
     * <p><ol>
     *    <li><code>ExtractedChain.matchList</code> - matches the list of Rmaps
     *	      at the next level down in the source hierarchy against the
     *	      current level of the request hierarchy,</li>
     *    <li>One of:
     *        <p><ol type=a>
     *           <li><code>ExtractedChain.moveDown</code> - moves down past a
     *		      nameless/timeless entry in the request hierarchy.  If
     *		      there are multiple children in the request hierarchy, a
     *		      separate ExtractedChain is started for each one.  This
     *		      loops back to the first step,</li>
     *           <li><code>ExtractedChain.matchListCurrent</code> - matches the
     *		     source list against the current level in the request
     *		     hierarchy with no inherited information from higher up the
     *		     request hierarchy,</li>
     *		 <li><code>ExtractedChain.matchListInherited</code> - matches
     *		     the source list against just the inherited information
     *		     from higher up in the request hierarchy, or</li>
     *		 <li><code>ExtractedChain.matchListCurrentAndInherited</code> -
     *		     matches the source list against a request consisting of
     *		     the combination of the current request and the inherited
     *		     information from higher up,</li>
     *        </ol></li><p>
     *    <li><code>ExtractedChain.matchListAgainst</code> - performs the
     *	      actual matching of the source list against the request
     *	      (inherited, current, current+inherited),</li>
     *	  <li><code>RmapVector.findMatches</code> - performs a modified
     *	      binary search for the request name and time within the
     *	      source list,</li>
     *    <li>If no matches are found at the current level, but there was
     *	      enough information available from higher levels to qualify as a
     *	      match, then extract that information using
     *	      <code>RmapExtractor.extractInformation</code>,</li>
     *    <li>If no matches are found at the current level and there isn't
     *	      enough information available from higher levels to qualify as a
     *	      match, then note that we were unable to find a match,</li>
     *    <li>If a list of matches was found, then loop through and process
     *	      each match individually using
     *	      <code>ExtractedChain.matchedList</code>,</li>
     *    <li><code>ExtractedChain.matchRmap</code> builds one or more new
     *	      ExtractedChains containing the matched source Rmap, the request
     *	      that matched it (inherited, current, or current+inherited),
     *	      inherited information consisting of the inherited information
     *	      from above plus any unmatched name and time range from the
     *	      matching request (<code>ExtractedChain.updateInherited</code>),
     *	      and one child from the next level of request hierarchy as the
     *	      ExtractedChain's request,</li>
     *    <li>If we've reached the bottom of the request hierarchy and there is
     *	      enough information from the source hierarchy (name, time, data)
     *	      to declare a match, then the match is extracted
     *	      (<code>RmapExtractor.extractInformation</code>),</li>
     *    <li>If there is more work to do to match the request, then
     *	      <code>ExtractedChain.moveDownFrom</code> starts moving down a
     *	      level in the source hierarchy,</li>
     *    <li><code>Rmap.moveDownFrom</code> (and the moveDownFrom methods from
     *	      Rmap's specialized subclasses) moves down one level in the source
     *	      hierarchy and starts the process over again.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @param requestI	 the request <code>Rmap</code>.
     * @param dataFlagI  extract the data payloads?
     * @return the matched <code>Rmap</code> hierarchy.
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
     *		  thrown if the information matching the request cannot be
     *		  extracted for any reason.
     * @since V2.0
     * @version 08/03/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/03/2004  INB	Added documentation.
     * 12/15/2000  INB	Created.
     *
     */
    public Rmap extractRmap(Rmap requestI,boolean dataFlagI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//long startTime=System.currentTimeMillis();
	if (!isImplemented(OPR_EXTRACTION)) {
	    throw new java.lang.IllegalArgumentException
		("Extraction is not supported by " + this);
	}

	// Build an <code>RmapExtractor</code> to extract the information.
	RmapExtractor extractor = new RmapExtractor(requestI,
						    true,
						    true,
						    true,
						    dataFlagI);

	Rmap extractedR = ((Rmap) extractor.extract(this));
//System.err.println("Rmap.extractRmap complete  at "+(System.currentTimeMillis()-startTime));
//System.err.println();

	return (extractedR);
    }

    /**
     * Finds a matching child <code>Rmap</code>.
     * <p>
     * If there are multiple matches, the one returned is not defined.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the child <code>Rmap</code> to match.
     * @return the matching child <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2001  INB	Created.
     *
     */
    public final Rmap findChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap matchR = null;

	if (getNchildren() > 0) {
	    matchR = getChildren().findMatch(childI);
	}

	return (matchR);
    }

    /**
     * Finds a descendant child <code>Rmap</code> by name.
     * <p>
     * This method can optionally create the hierarchy leading to the desired
     * descendant.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the fully qualified descendant's name.
     * @param addI  add the descendant if it doesn't exist?
     * @return the descendant.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if a group is discovered.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description

     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/07/2001  INB	Created.
     *
     */
    private final Rmap findChildDescendant(String nameI,boolean addI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = null;

	int eIdx;
	if ((eIdx = getNchildren()) > 0) {
	    // If there are children, then match the first part of the input
	    // name.
	    int startAt = 0,
		idx = nameI.indexOf(PATHDELIMITER);
	    if (idx == 0) {
		startAt = 1;
		idx = nameI.indexOf(PATHDELIMITER,startAt);
	    }
	    String lName,
		   nameT;
	    if (idx == -1) {
		lName = nameI.substring(startAt);
		nameT = null;
	    } else {
		lName = nameI.substring(startAt,idx);
		nameT = nameI.substring(idx + 1);
	    }

	    java.util.Vector matches = getChildren().findName(lName);

	    if (matches.size() == 0) {
		// If there are no matching names, try working down any of the
		// unnamed children of this <code>Rmap</code>.
		for (idx = 0; (rmapR == null) && (idx < eIdx); ++idx) {
		    Rmap entry = getChildAt(idx);
		    if (entry.getName() != null) {
			break;
		    } else {
			rmapR = entry.findDescendant(nameI,false);
		    }
		}

	    } else {
		// If there are matching names, then see any of them has the
		// fully qualified child we're interested in.

		if (nameT == null) {
		    // If there is no more to the name, take the first entry.
		    rmapR = (Rmap) matches.elementAt(0);

		} else {
		    for (idx = 0, eIdx = matches.size();
			 (rmapR == null) && (idx < eIdx);
			 ++idx) {
			Rmap entry = (Rmap) matches.elementAt(idx);

			rmapR = entry.findDescendant
			    (nameI,
			     addI && (idx == matches.size() - 1));
		    }
		}
	    }
	}

	return (rmapR);
    }

    /**
     * Finds a descendant <code>Rmap</code> by name.
     * <p>
     * Due to the complications introduced by group membership, this method can
     * only be used properly when there are no groups. If a group is discovered
     * during the search, an exception is thrown.
     * <p>
     * This method can optionally create the hierarchy leading to the desired
     * descendant.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the fully qualified descendant's name.
     * @param addI  add the descendant if it doesn't exist?
     * @return the descendant.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if a group is discovered.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/07/2001  INB	Created.
     *
     */
    public final Rmap findDescendant(String nameI,boolean addI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean OK = true;
	Rmap rmapR = null;
	String nameT = nameI;
	int idx = nameI.indexOf(PATHDELIMITER);

	// If this <code>Rmap</code> has a name, then it must match th
	// beginning of the name hierarchy.
	if (getName() != null) {
	    String lName;
	    if (idx == 0) {
		idx = nameI.indexOf(PATHDELIMITER,1);
		if (idx == -1) {
		    lName = nameI.substring(1);
		    nameT = null;
		} else {
		    lName = nameI.substring(1,idx);
		    nameT = nameI.substring(idx);
		}
	    } else if ((nameI.charAt(0) != '.') ||
		       ((nameI.length() > 1) && (idx > 1))) {
		lName = ".";
		nameT = PATHDELIMITER + nameI;
	    } else if (idx != -1) {
		lName = ".";
		nameT = nameI.substring(idx);
	    } else {
		lName = ".";
		nameT = null;
	    }

	    OK = (compareNames(lName) == 0);

	} else if ((idx == -1) ||
		   (idx > 1) ||
		   ((idx == 1) && (nameI.charAt(0) != '.'))) {
	    nameT = "." + PATHDELIMITER + nameI;

	} else {
	    nameT = nameI;
	}

	if (OK) {
	    // If we've got at least a partial match, then try to find the rest
	    // of the name.

	    if (nameT == null) {
		// If there is nothing more to match, then we've found what we
		// want.
		rmapR = this;

	    } else if (getNmembers() > 0) {
		// Groups introduce too many complications.
		throw new java.lang.IllegalStateException
		    ("Cannot find a descendant of an Rmap that has a " +
		     "group.\n" + this);

	    } else if ((rmapR = findChildDescendant(nameT,addI)) == null) {
		// If there is no descendant child matching the name, then
		// create a new one and add it in here.
		if (addI) {
		    rmapR = createFromName(nameT);
		    addChild(rmapR);
		    while (rmapR.getNchildren() == 1) {
			rmapR = rmapR.getChildAt(0);
		    }
		}
	    }
	}

	return (rmapR);
    }

    /**
     * Finds the time and frame limits of this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param tLimitsIO the time limits.
     * @param fLimitsIO the frame limits.
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
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    final void findLimits(TimeRange tLimitsIO,TimeRange fLimitsIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	double[] limits;

	if (getTrange() != null) {
	    limits = getTrange().getLimits();
	    if ((tLimitsIO.getTime() == Double.MAX_VALUE) &&
		(tLimitsIO.getDuration() == -Double.MAX_VALUE)) {
		tLimitsIO.set(limits[0],limits[1] - limits[0]);
	    } else {
		tLimitsIO.addLimits(limits);
	    }
	}

	if (getFrange() != null) {
	    limits = getFrange().getLimits();
	    if ((fLimitsIO.getTime() == Double.MAX_VALUE) &&
		(fLimitsIO.getDuration() == -Double.MAX_VALUE)) {
		fLimitsIO.set(limits[0],limits[1] - limits[0]);
	    } else {
		fLimitsIO.addLimits(limits);
	    }
	}

	for (int idx = 0; idx < getNchildren(); ++idx) {
	    getChildAt(idx).findLimits(tLimitsIO,fLimitsIO);
	}
    }

    /**
     * Finds a matching member <code>Rmap</code>.
     * <p>
     * If there are multiple matches, the one returned is not defined.
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI  the member <code>Rmap</code> to match.
     * @return the matching member <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2001  INB	Created.
     *
     */
    public final Rmap findMember(Rmap memberI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap matchR = null;

	if (getNmembers() > 0) {
	    matchR = getMembers().findMatch(memberI);
	}

	return (matchR);
    }

    /**
     * Finds the number of points for a time relative request.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI		 the request in question.
     * @param pointsPerChannelIO the number of points per channel found so far.
     * @return the number of points found.
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
     * @version 11/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/05/2003  INB	Created.
     *
     */
    final int findPointsTimeRelative
	(TimeRelativeRequest requestI,
	 java.util.Hashtable pointsPerChannelIO)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	/*
	System.err.println("Rmap.findPointsTimeRelative: " + getFullName() +
			   " " + getTrange() + " " + getDblock());
	*/

	TimeRelativeResponse response = new TimeRelativeResponse();
	int nPointsR =
	    (pointsPerChannelIO.isEmpty() ?
	     -1 :
	     ((Integer)
	      pointsPerChannelIO.elements().nextElement()).intValue());
	int nPoints;
	TimeRelativeRequest request;
	TimeRelativeChannel trc;
	java.util.Vector requests;

	// If the children of this <code>Rmap</code> have names, then we need
	// to split the request into subsets by the next part of the channel
	// name.  This ensures that we can perform binary searches for names as
	// well as times from here on down.
	// The <code>Rmap</code> sort is such that named <code>Rmaps</code>
	// definitely follow all unnamed ones, so we only need to check the
	// last one.
 	if ((getNchildren() == 0) ||
	    (getChildAt(getNchildren() - 1).getName() == null)) {
	    // No names at the next level, so we can simply make a list
	    // consisting of the input request.
	    requests = new java.util.Vector();
	    requests.addElement(requestI);

	} else {
	    // With names at the next level, we need to split the request.
	    requests = requestI.splitByNameLevel();
	}

	if (getDblock() != null) {
	    // If there is a <code>DataBlock</code> here, then see if this
	    // channel matches any of the names.  For it to do so, the name
	    // must be fully matched - it will be the first name in the input
	    // channel list.
	    trc = (TimeRelativeChannel) requestI.getByChannel().firstElement();
	    if (trc.nextNameLevel(requestI.getNameOffset()) == null) {
		// We've found a matching channel, so grab the number of points
		// and check it out.
		nPoints = getDblock().getNpts();

		if ((nPointsR == -1) || (nPoints == nPointsR)) {
		    // Found a reasonable number of points.
		    nPointsR = nPoints;
		    pointsPerChannelIO.put(trc.getChannelName(),
					   new Integer(nPoints));
		} else {
		    // The number of points has changed, we need to split the
		    // channels.
		    nPointsR = -2;
		}
	    }
	}

	if ((nPointsR != -2) && (getNchildren() > 0)) {
	    // OK, the number of points is still good, so perform a binary
	    // search of our children for each of the requests still needing to
	    // be worked on (there may not be any at this point).
	    int lo;
	    int hi;
	    int direction;
	    String requestName;
	    Rmap rmap;

	    for (int ridx = 0;
		 (nPointsR != -2) && (ridx < requests.size());
		 ++ridx) {
		request = (TimeRelativeRequest) requests.elementAt(ridx);
		lo = 0;
		hi = getNchildren() - 1;

		// First work on any nameless entries.
		for (int idx = lo;
		     (nPointsR != -2) && (idx <= hi);
		     ++idx) {
		    rmap = getChildAt(idx);
		    if (rmap.getName() != null) {
			break;
		    }
		    ++lo;
		    nPoints = rmap.findPointsTimeRelative
			(request,
			 pointsPerChannelIO);
		    if ((nPoints == -2) || (nPoints == nPointsR)) {
			nPointsR = nPoints;
			lo = 0;
			hi = -1;
		    } else if (nPoints != -1) {
			if (nPointsR == -1) {
			    nPointsR = nPoints;
			} else {
			    nPointsR = -2;
			}
			lo = 0;
			hi = -1;
		    }
		}

		// And then on any with names.
		for (int idx = (lo + hi)/2;
		     ((nPointsR != -2) && (lo <= hi));
		     idx = (lo + hi)/2) {
		    rmap = getChildAt(idx);
		    trc = (TimeRelativeChannel)
			request.getByChannel().firstElement();
		    requestName = trc.nextNameLevel(request.getNameOffset());
		    direction = rmap.compareNames(requestName);

		    if (direction > 0) {
			// The <code>Rmap</code> name comes after that
			// of the request.
			hi = idx - 1;
			continue;

		    } else if (direction < 0) {
			// The <code>Rmap</code> name comes before that
			// of the request.
			lo = idx + 1;
			continue;
		    }

		    // The channel name matches the request.
		    TimeRelativeRequest oRequest = request;
		    request = new TimeRelativeRequest();
		    request.setNameOffset
			(oRequest.getNameOffset() +
			 requestName.length() +
			 1);
		    request.setRelationship
			(oRequest.getRelationship());
		    request.setTimeRange(oRequest.getTimeRange());
		    request.setByChannel(oRequest.getByChannel());

		    nPoints = rmap.findPointsTimeRelative
			(request,
			 pointsPerChannelIO);
		    if ((nPoints == -2) || (nPoints == nPointsR)) {
			nPointsR = nPoints;
			lo = 0;
			hi = -1;
		    } else if (nPoints != -1) {
			if (nPointsR == -1) {
			    nPointsR = nPoints;
			} else {
			   nPointsR = -2;
			}
			lo = 0;
			hi = -1;
		    }
		}
	    }
	}

	/*
	System.err.println("Rmap.findPointsTimeRelative: " + nPointsR);
	*/

	return (nPointsR);
    }

    /**
     * Gets the child at the specified index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the child to retrieve.
     * @return the child <code>Rmap</code>.
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
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #getNchildren()
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public Rmap getChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap childR = null;

	if (getChildren() == null) {
	    try {
		throw new ArrayIndexOutOfBoundsException
		    ("Cannot get child at " + indexI +
		     "; there are no children.");
	    } catch (ArrayIndexOutOfBoundsException ee) {
		ee.printStackTrace();
		throw ee;
	    }

	} else {
	    childR = (Rmap) getChildren().elementAt(indexI);
	}

	return (childR);
    }

    /**
     * Gets the list of children of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the children.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    public final RmapVector getChildren()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (children);
    }

    /**
     * Gets the data size of this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data size.
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
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    long getDataSize()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long dataSizeR = 0;

	if ((getDblock() != null) && (getDblock().getData() != null)) {
	    dataSizeR += getDblock().getNpts()*getDblock().getPtsize();
	}

	for (int idx = 0; idx < getNchildren(); ++idx) {
	    dataSizeR += getChildAt(idx).getDataSize();
	}

	return (dataSizeR);
    }

    /**
     * Gets the <code>DataBlock</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>DataBlock</code>.
     * @see #setDblock(DataBlock)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final DataBlock getDblock() {
	return (dataBlock);
    }

    /**
     * Gets the frame index <code>TimeRange</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the frame index <code>TimeRange</code>.
     * @see #setFrange(TimeRange)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    public final TimeRange getFrange() {
	return (frameRange);
    }

    /**
     * Gets the fully qualified name of this <code>Rmap</code>.
     * <p>
     * If the <code>Rmap</code> is a descendent of an <code>Rmap</code> with
     * group members, the fully qualified name is actually ambiguous. The code
     * fills in a special _<member> name into the fully qualified name at the
     * appropriate point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the fully qualified name.
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
     * @since V2.0
     * @version 02/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/27/2001  INB	Created.
     *
     */
    public final String getFullName()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String stringR = null;

	if (getParent() != null) {
	    // If this <code>Rmap</code> has a parent, prepend the fully
	    // qualified name of the parent.
	    stringR = getParent().getFullName();

	    if ((getParent().getNmembers() > 0) && getParent().isChild(this)) {
		// If the parent has members and this entry is a child, then
		// prepend "_<member>".
		if (stringR == null) {
		    stringR = PATHDELIMITER + "_<member>";
		} else {
		    stringR += PATHDELIMITER + "_<member>";
		}
	    }
	}

	if ((getName() != null) && (compareNames(".") != 0)) {
	    // If this <code>Rmap</code> has a name, add it to the end of the
	    // name built so far.
	    if (stringR != null) {
		stringR += PATHDELIMITER + getName();
	    } else if (getParent() == null) {
		stringR = PATHDELIMITER + getName();
	    } else {
		String delimiter = "" + PATHDELIMITER;
		for (Rmap ancestor = getParent();
		     ancestor != null;
		     ancestor = ancestor.getParent()) {
		    if (ancestor.compareNames(".") == 0) {
			delimiter = "";
			break;
		    }
		}
		stringR = delimiter + getName();
	    }
	}

	return (stringR);
    }

    /**
     * Gets the member at the specified index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the member to retrieve.
     * @return the member <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no members or the index is not in the
     *		  range 0 to # of members - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addMember(com.rbnb.api.Rmap)
     * @see #getNmembers()
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public Rmap getMemberAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap memberR = null;

	if (getMembers() == null) {
	    throw new ArrayIndexOutOfBoundsException
		("Cannot get member at " + indexI + "; there are no members.");
	} else {
	    memberR = (Rmap) getMembers().elementAt(indexI);
	}

	return (memberR);
    }

    /**
     * Gets the group members of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the group members.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #addMember(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/07/2000  INB	Created.
     *
     */
    public final RmapVector getMembers()
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (groupList);
    }

    /**
     * Gets the name of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setName(String)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final String getName() {
	return (name);
    }

    /**
     * Gets the number of children.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of children.
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
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #getChildAt(int)
     * @see #removeChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getNchildren()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.util.Vector lChildren = getChildren();
	int nChildrenR = (lChildren == null) ? 0 : lChildren.size();

	return (nChildrenR);
    }

    /**
     * Gets the number of members.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of members.
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
     * @see #addMember(com.rbnb.api.Rmap)
     * @see #getMemberAt(int)
     * @see #removeMember(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final int getNmembers()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.util.Vector lMembers = getMembers();
	return ((lMembers == null) ? 0 : lMembers.size());
    }

    /**
     * Gets the parent <code>Rmap</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the parent <code>Rmap</code>.
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final Rmap getParent() {
	return (parent);
    }

    /**
     * Gets the <code>TimeRange</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>TimeRange</code>.
     * @see #setTrange(TimeRange)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final TimeRange getTrange() {
	return (timeRange);
    }

    /**
     * Inherits information from the input <code>Rmap</code> to produce an
     * updated version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI    the <code>Rmap</code> to inherit from.
     * @param rmapInI  the <code>RmapInheritor</code> to use.
     * @return was anything inherited?
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
     * @exception java.lang.IllegalStateException
     *		  thrown if the input <code>Rmap</code> is missing information
     *		  that cannot be retrieved from the <code>Rmaps</code> in this
     *		  <code>RmapInheritor's</code> list.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem inheriting information.
     * @see com.rbnb.api.RmapInheritor#addRmap(com.rbnb.api.Rmap)
     * @see com.rbnb.api.RmapInheritor#apply(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2000  INB	Created.
     *
     */
    final boolean inheritFrom(Rmap rmapI,RmapInheritor rmapInI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean inheritedR;

	if (inheritedR = rmapInI.addRmap(rmapI)) {
	    // If the input <code>Rmap</code> can provide enough information to
	    // fill in for this <code>Rmap</code>, then inherit that
	    // information.
	    rmapInI.apply(this);
	}

	return (inheritedR);
    }

    /**
     * Is the input <code>Rmap</code> a child of this one?
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI  the suspected child.
     * @return is the entry a child?
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
     * @see #isMember(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/11/2001  INB	Created.
     *
     */
    final boolean isChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean isR = false;

	if ((childI.getParent() == this) && (getNchildren() > 0)) {
	    isR = (getChildren().indexOf(childI) != -1);
	}

	return (isR);
    }

    /**
     * Is this <code>Rmap</code> identifiable (by name, time, frame, or other
     * information)?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this identifiable?
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2001  INB	Created.
     *
     */
    boolean isIdentifiable() {
	return (!isNamelessTimeless());
    }

    /**
     * Is the specified type of operation implemented?
     * <p>
     * <code>Rmaps</code> implement everything in <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeI  the type of operation.
     * @return is the operation implemented?
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
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
    public boolean isImplemented(byte typeI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (true);
    }

    /**
     * Is the input <code>Rmap</code> a member of this one?
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI  the suspected member.
     * @return is the entry a member?
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
     * @see #isMember(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/11/2001  INB	Created.
     *
     */
    final boolean isMember(Rmap memberI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean isR = false;

	if ((memberI.getParent() == this) && (getNmembers() > 0)) {
	    isR = (getMembers().indexOf(memberI) != -1);
	}

	return (isR);
    }

    /**
     * Is this <code>Rmap</code> nameless and timeless?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this nameless and timeless?
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2001  INB	Created.
     *
     */
    boolean isNamelessTimeless() {
	return ((getName() == null) &&
		(getTrange() == null) &&
		(getFrange() == null));
    }

    /**
     * Is this <code>Rmap</code> significantly different than the input one?
     * <p>
     * This method is used to determine whether an <code>Rmap</code> hierarchy
     * should be transmitted via an update or a complete transmission of a new
     * object. It tries to do a minimum amount of work to determine whether or
     * not it is going to be expensive to transmit a completely new object or
     * not.
     * <p>
     * This method is recursive; under some circumstances the children of the
     * object are checked individually.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @return are the two significantly different?
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
     * @version 10/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2001  INB	Created.
     *
     */
    final boolean isSignificantlyDifferent(Rmap otherI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean differentR =
	    ((otherI == null) ||
	     !getClass().equals(otherI.getClass()) ||
	     (!otherI.nameNSD && (compareNames(otherI) != 0)) ||
	     ((getTrange() == null) != (otherI.getTrange() == null)) ||
	     ((getFrange() == null) != (otherI.getFrange() == null)) ||
	     ((getDblock() == null) != (otherI.getDblock() == null)));

	if (!differentR) {
	    int eIdx;
	    if ((eIdx = getNchildren()) != otherI.getNchildren()) {
		differentR = true;

	    } else {
		for (int idx = 0;
		     !differentR && (idx < eIdx);
		     ++idx) {
		    differentR = getChildAt(idx).isSignificantlyDifferent
			(otherI.getChildAt(idx));
		}
	    }
	}

	if (!differentR) {
	    int eIdx;
	    if ((eIdx = getNmembers()) != otherI.getNmembers()) {
		differentR = true;

	    } else {
		for (int idx = 0;
		     !differentR && (idx < eIdx);
		     ++idx) {
		    differentR = getMemberAt(idx).isSignificantlyDifferent
			(otherI.getMemberAt(idx));
		}
	    }
	}

	return (differentR);
    }

    /**
     * Limits this <code>Rmap</code> hierarchy to a 'valid" list of names.
     * <p>
     * The <code>Rmap</code> hierarchy returned is the subset of this
     * <code>Rmap</code> hierarchy that matches the input <code>Rmap</code>
     * hierarchy's chains of names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param validI the valid <code>Rmap</code> hierarchy to compare to.
     * @return the limited hierarchy.
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
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    final Rmap limitToValid(Rmap validI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap validatedR = null,
	     valid = validI;

	if (getName() == null) {
	    validatedR = newInstance();
	    validatedR.setTrange(getTrange());
	    validatedR.setFrange(getFrange());
	    validatedR.setDblock(getDblock());

	} else if ((compareNames("...") != 0) &&
		   (compareNames("*") != 0)) {
	    String name =
		PATHDELIMITER +
		((validI.getName() != null) ?
		 (validI.getName() + PATHDELIMITER + getName()) :
		 getName());
	    if ((valid = validI.findDescendant(name,false)) != null) {
		validatedR = newInstance();
		validatedR.setTrange(getTrange());
		validatedR.setFrange(getFrange());
		validatedR.setDblock(getDblock());
	    }
	}

	if (validatedR != null) {
	    if (getNmembers() > 0) {
		throw new java.lang.IllegalStateException
		    ("Cannot validate Rmap hierarchy containing members.");
	    }
	    Rmap child,
		vChild;
	    for (int idx = 0,
		     endIdx = getNchildren();
		 idx < endIdx;
		 ++idx) {
		child = getChildAt(idx);
		vChild = child.limitToValid(valid);

		if (vChild != null) {
		    validatedR.addChild(vChild);
		}
	    }
	    if ((getNchildren() > 0) &&
		(validatedR.getNchildren() == 0)) {
		validatedR = null;
	    }

	} else if (compareNames("...") == 0) {
	    validatedR = newInstance();
	    validatedR.setName(null);
	    validatedR.setTrange(getTrange());
	    validatedR.setFrange(getFrange());
	    validatedR.setDblock(getDblock());

	    String[] names;
	    int idx,
		idx1,
		endIdx;
	    for (idx = 0,
		     endIdx = validI.getNchildren();
		 idx < endIdx;
		 ++idx) {
		names = validI.getChildAt(idx).extractNames();

		if (names.length > 0) {
		    for (idx1 = 0; idx1 < names.length; ++idx1) {
			validatedR.addChannel(names[idx1]);
		    }
		}
	    }

	} else if (compareNames("*") == 0) {
	    validatedR = newInstance();
	    validatedR.setName(null);
	    validatedR.setTrange(getTrange());
	    validatedR.setFrange(getFrange());
	    validatedR.setDblock(getDblock());

	    int idx,
		idx1,
		endIdx,
		endIdx1;
	    Rmap child,
		vChild1,
		vChild2;
	    for (idx = 0,
		     endIdx = validI.getNchildren();
		 idx < endIdx;
		 ++idx) {
		child = validI.getChildAt(idx);
		vChild1 = new Rmap(child.getName());
		validatedR.addChild(vChild1);
		for (idx1 = 0,
			 endIdx1 = getNchildren();
		     idx1 < endIdx1;
		     ++idx1) {
		    vChild2 = getChildAt(idx1).limitToValid(child);
		    vChild1.addChild(vChild2);
		}
	    }
	}

	return (validatedR);
    }

    /**
     * Locks this <code>Rmap</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
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
     * 11/14/2003  INB	Call version with a location.
     * 06/14/2001  INB	Created.
     *
     */
    void lockRead()
	throws java.lang.InterruptedException
    {
	lockRead("Rmap.lockRead");
    }

    /**
     * Locks this <code>Rmap</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locationI the location.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.2
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Created.
     *
     */
    void lockRead(String locationI)
	throws java.lang.InterruptedException
    {
    }

    /**
     * Marks leaf nodes of an <code>Rmap</code> hierarchy by putting in
     * single byte <code>DataBlocks</code>.
     * <p>
     * Leaf nodes with <code>DataBlocks</code> are not modified.
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
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2002  INB	Created.
     *
     */
    public final void markLeaf()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getNchildren() == 0) {
	    if (getDblock() == null) {
		setDblock(MarkerBlock);
	    }
	} else {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		getChildAt(idx).markLeaf();
	    }
	}
    }

    /**
     * Matches the input request <code>Rmap</code> against this one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return the results of the match:
     *	       <p><ul>
     *	       <li><code>MATCH_EQUAL</code> - the two compare equal,</li>
     *	       <li><code>MATCH_SUBSET</code> - this <code>Rmap</code> is
     *		   contained in the request,</li>
     *	       <li><code>MATCH_SUPERSET</code> - this <code>Rmap</code>
     *		   contains the request,</li>
     *	       <li><code>MATCH_INTERSECTION</code> - this <code>Rmap</code> and
     *		   the request contain common regions,</li>
     *	       <li><code>MATCH_BEFORENAME</code> - this <code>Rmap</code> is
     *		   before the request based solely on name,</li>
     *	       <li><code>MATCH_BEFORE</code> - this <code>Rmap</code> is before
     *		   the request,</li>
     *	       <li><code>MATCH_AFTERNAME</code> - this <code>Rmap</code> is
     *		   after the request based solely on name, or</li>
     *	       <li><code>MATCH_AFTER</code> - this <code>Rmap</code> is after
     *		   the request, or</li>
     *	       <li><code>MATCH_NOINTERSECTION</code> - this <code>Rmap</code>
     *		   and the request have the same name and have overlapping
     *		   <code>TimeRanges</code>, but do not have any common
     *		   regions.</li>
     *	       </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 09/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/06/2000  INB	Created.
     *
     */
    final byte matches(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte matchesR = matches(requestI,
				((requestI instanceof DataRequest) ?
				 ((DataRequest) requestI) :
				 new DataRequest()));

	return (matchesR);
    }

    /**
     * Matches the input request <code>Rmap</code> against this one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI    the request <code>Rmap</code>.
     * @param referenceI  the reference request.
     * @return the results of the match:
     *	       <p><ul>
     *	       <li><code>MATCH_EQUAL</code> - the two compare equal,</li>
     *	       <li><code>MATCH_SUBSET</code> - this <code>Rmap</code> is
     *		   contained in the request,</li>
     *	       <li><code>MATCH_SUPERSET</code> - this <code>Rmap</code>
     *		   contains the request,</li>
     *	       <li><code>MATCH_INTERSECTION</code> - this <code>Rmap</code> and
     *		   the request contain common regions,</li>
     *	       <li><code>MATCH_BEFORENAME</code> - this <code>Rmap</code> is
     *		   before the request based solely on name,</li>
     *	       <li><code>MATCH_BEFORE</code> - this <code>Rmap</code> is before
     *		   the request,</li>
     *	       <li><code>MATCH_AFTERNAME</code> - this <code>Rmap</code> is
     *		   after the request based solely on name, or</li>
     *	       <li><code>MATCH_AFTER</code> - this <code>Rmap</code> is after
     *		   the request, or</li>
     *	       <li><code>MATCH_NOINTERSECTION</code> - this <code>Rmap</code>
     *		   and the request have the same name and have overlapping
     *		   <code>TimeRanges</code>, but do not have any common
     *		   regions.</li>
     *	       </ul>
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2003  INB	Reorganized name checks for efficiency.
     * 01/26/2001  INB	Created.
     *
     */
    byte matches(Rmap requestI,DataRequest referenceI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte matchesR = MATCH_EQUAL;

	if (requestI.getName() == null) {
	    if (getName() != null) {
		matchesR = MATCH_AFTERNAME;
	    }

	} else {
	    // The request has a name to match.
	    boolean requestWild = (requestI.getName().equals("...") ||
				   requestI.getName().equals("*"));

	    if (!requestWild) {
		// If the request is not a wildcard, then it's name may not
		// match.

		if (getName() == null) {
		    // If there is no name for this <code>Rmap</code>, then it
		    // comes before the name of the request.
		    matchesR = MATCH_BEFORENAME;

		} else {
		    // Now match against the name of the <code>Rmap</code>
		    boolean myWild = (getName().equals("...") ||
				      getName().equals("*"));

		    if (!myWild) {
			// If neither the request nor this <code>Rmap</code>
			// is a wildcard, then we need to compare the names.
			int direction = compareNames(requestI);
			if (direction < 0) {
			    // If this <code>Rmap's</code> name is less than
			    // that of the request, then this <code>Rmap</code>
			    // comes before the request.
			    matchesR = MATCH_BEFORENAME;

			} else if (direction > 0) {
			    // If this <code>Rmap's</code> name is greater than
			    // that of the request, then this <code>Rmap</code>
			    // comes after the request.
			    matchesR = MATCH_AFTERNAME;
			}
		    }
		}
	    }
	}

	if (matchesR == MATCH_EQUAL) {
	    // If the name matched, try the <code>TimeRanges</code>.

	    if ((referenceI.getReference() != DataRequest.ABSOLUTE) ||
		((requestI.getTrange() == null) &&
		 (requestI.getFrange() == null))) {
		// If the request does not have a <code>TimeRange</code> or if
		// it is a special request, then the <code>Rmap</code> either
		// equals the request or is a subset of the request.

		if ((getTrange() != null) || (getFrange() != null)) {
		    // If the <code>Rmap</code> has a <code>TimeRange</code>,
		    // then it is a subset of the request.
		    matchesR = MATCH_SUBSET;
		}

	    } else {
		// If the request does have a <code>TimeRange</code>, then
		// either the <code>Rmap</code> is a superset of the request or
		// we have to compare the <code>TimeRanges</code>.
		TimeRange mine = null,
			  requests = null;

		if ((requests = requestI.getTrange()) != null) {
		    // If the request is for times, compare them.
		    mine = getTrange();

		} else if ((requests = requestI.getFrange()) != null) {
		    // If the request is for frames, compare them.
		    mine = getFrange();
		}

		if (mine == null) {
		    // If the <code>Rmap</code> does not have a
		    // <code>TimeRange</code>, then it is a superset of the
		    // request.
		    matchesR = MATCH_SUPERSET;

		} else {
		    // If the <code>Rmap</code> has a <code>TimeRange</code>,
		    // then compare the <code>TimeRanges</code>.
		    matchesR = mine.matches(requests);
		}
	    }
	}

	return (matchesR);
    }

    /**
     * Matches the contents of this <code>Rmap</code> against a
     * <code>TimeRelativeRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI the <code>TimeRelativeRequest</code>.
     * @param roI	the <code>RequestOptions</code>.
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
     * @version 02/26/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/26/2004  INB	If we get name match failures back from children, use
     *			them to set the <code>nameLo</code> and
     *			<code>nameHi</code> values.
     * 12/11/2003  INB	Added <code>RequestOptions</code> to
     *			<code>TimeRelativeRequest</code> handling to allow the
     *			code to do the right thing for
     *			<code>extendStart</code>.
     * 10/10/2003  INB	Created.
     *
     */
    TimeRelativeResponse matchTimeRelative(TimeRelativeRequest requestI,
					   RequestOptions roI)
	throws com.rbnb.utility.SortException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRelativeResponse responseR = new TimeRelativeResponse();
	TimeRelativeRequest request;
	TimeRelativeChannel trc;
	java.util.Vector requests;

	/*
	System.err.println("Rmap.matchTimeRelative: " + getFullName() +
			   " " + getTrange() + " " +
			   "\nAgainst: " + requestI + " " + roI);
	*/

	// If the children of this <code>Rmap</code> have names, then we need
	// to split the request into subsets by the next part of the channel
	// name.  This ensures that we can perform binary searches for names as
	// well as times from here on down.
	// The <code>Rmap</code> sort is such that named <code>Rmaps</code>
	// definitely follow all unnamed ones, so we only need to check the
	// last one.
 	if ((getNchildren() == 0) ||
	    (getChildAt(getNchildren() - 1).getName() == null)) {
	    // No names at the next level, so we can simply make a list
	    // consisting of the input request.
	    requests = new java.util.Vector();
	    requests.addElement(requestI);

	} else {
	    // With names at the next level, we need to split the request.
	    requests = requestI.splitByNameLevel();
	}

	if (getTrange() != null) {
	    // If there is a <code>TimeRange</code> here, then get our answer
	    // from it.
	    responseR.setStatus(requestI.compareToTimeRange(getTrange()));

	    if (responseR.getStatus() == 0) {
		// If the desired time reference is in the
		// <code>TimeRange</code>, then find the specific time that
		// matches what we are interested in.  Note that we may still
		// decide to find something before or after this.
		int nPoints;
		if ((getTrange().getNptimes() > 1) ||
		    (getTrange().getDuration() == 0.)) {
		    // If the <code>TimeRange</code> represents either a single
		    // time or has multiple times, then we've got enough
		    // informnation to get a meaningful result.
		    nPoints = getTrange().getNptimes();

		} else {
		    // Otherwise, we need to find out how many data points this
		    // represents.  To do that, we need to determine how many
		    // data points each channel has.
		    java.util.Hashtable pointsPerChannel =
			new java.util.Hashtable();
		    int rnPoints;
		    request = (TimeRelativeRequest) requests.firstElement();
		    nPoints = findPointsTimeRelative(request,pointsPerChannel);
		    for (int idx = 1;
			 (nPoints != -2) && (idx < requests.size());
			 ++idx) {
			request = (TimeRelativeRequest)
			    requests.elementAt(idx);
			rnPoints = findPointsTimeRelative(request,
							  pointsPerChannel);
			if (rnPoints != nPoints) {
			    nPoints = -2;
			}
		    }

		    if (nPoints == -2) {
			// If we cannot find a consistent number of points,
			// then force a split of the channels.
			responseR.setStatus(-2);

		    } else if (nPoints == -1) {
			// If we cannot find any information, then we need to
			// move on.
			responseR.setStatus(1);
		    }
		}

		if (responseR.getStatus() == 0) {
		    responseR = getTrange().matchTimeRelative
			(requestI,
			 roI,
			 nPoints);
		}
	    }
	    
	} else {
	    // With no <code>TimeRange</code> here, then we need to do a binary
	    // search of the children for the match.
	    TimeRelativeResponse response;
	    Rmap rmap;
	    int lo;
	    int hi;
	    int idx;
	    int lastIdx = 0;
	    int direction;
	    int nameLo;
	    int nameHi;
	    String requestName = null;
	    int lastGoodStatus = Integer.MIN_VALUE;

	    for (int ridx = 0;
		 (responseR.getStatus() != -2) && (ridx < requests.size());
		 ++ridx) {
		response = new TimeRelativeResponse();
		response.setStatus(-1);
		request = (TimeRelativeRequest) requests.elementAt(ridx);
		nameLo =
		    lo = 0;
		nameHi =
		    hi = getNchildren() - 1;
		trc = (TimeRelativeChannel)
		    request.getByChannel().firstElement();
		requestName = trc.nextNameLevel(request.getNameOffset());

		lastIdx = 0;
		lastGoodStatus = Integer.MIN_VALUE;
		for (idx = (lo + hi)/2;
		     (responseR.getStatus() != -2) &&
			 (response.getStatus() != 0) &&
			 (lo <= hi);
		     idx = (lo + hi)/2) {
		    // Search each <code>Rmap</code> for the data.
		    response.setStatus(0);
		    lastIdx = idx;
		    rmap = getChildAt(idx);
		    if (rmap.getName() != null) {
			// If the <code>Rmap</code> has a name, check it
			// against the request's next name part (which is
			// consistent for a channels in this request).
			direction = rmap.compareNames(requestName);

			if (direction > 0) {
			    // The channel name comes after the request.
			    response.setStatus(-3);
			    hi = idx - 1;
			    nameHi = hi;
			} else if (direction < 0) {
			    // The channel name comes before the request.
			    response.setStatus(3);
			    lo = idx + 1;
			    nameLo = lo;
			} else {
			    // The channel name matches the request.
			    TimeRelativeRequest oRequest = request;
			    request = new TimeRelativeRequest();
			    request.setNameOffset
				(oRequest.getNameOffset() +
				 requestName.length() +
				 1);
			    request.setRelationship
				(oRequest.getRelationship());
			    request.setTimeRange(oRequest.getTimeRange());
			    request.setByChannel(oRequest.getByChannel());
			}
		    }

		    if (response.getStatus() == 0) {
			// So far, the entry matches, so check it in detail.
			response = rmap.matchTimeRelative(request,roI);
			if ((response.getStatus() != -3) &&
			    (response.getStatus() != 3)) {
			    lastGoodStatus = response.getStatus();
			}

			switch (response.getStatus()) {
			case -2:
			    // Different channels have different time bases.
			    // We'll need to split this requesst into per
			    // channel requests.
			    responseR.setStatus(-2);
			    break;

			case -1:
			case -3:
			    // The request is for data before the
			    // <code>Rmap</code>.
			    hi = idx - 1;
			    if (response.getStatus() == -3) {
				nameHi = hi;
			    }
			    break;

			case 0:
			    // The request is for data within the
			    // <code>Rmap</code> - we're done.
			    break;

			case 1:
			case 3:
			    // The request is for data after the
			    // <code>Rmap</code>.
			    lo = idx + 1;
			    if (response.getStatus() == 3) {
				nameLo = lo;
			    }
			    break;
			}
		    }
		}

		if (((response.getStatus() == -1) && (lastIdx > nameLo)) ||
		    ((response.getStatus() == 1) && (lastIdx < nameHi))) {
		    if (response.getStatus() == 1) {
			// Always set the index to the second
			// <code>Rmap</code>.
			++lastIdx;
		    }

		    /*
		    System.err.println("Rmap.matchTimeRelative: " +
				       getFullName() +
				       " " + getTrange() + " " +
				       "\nAgainst: " + requestI + " " + roI +
				       "\nWorking: " + response + " " +
				       lastIdx + "\n" +
				       getChildAt(lastIdx));
		    */

		    // If we discover that the request is between two
		    // <code>Rmap</code>, then we can figure out the
		    // time from one of the two.
		    switch (request.getRelationship()) {
		    case TimeRelativeRequest.BEFORE:
		    case TimeRelativeRequest.AT_OR_BEFORE:
			// For requests before the time reference, use the
			// end of the previous <code>Rmap</code>.
			rmap = getChildAt(lastIdx - 1);
			response = rmap.beforeTimeRelative(request,roI);
			if ((response.getStatus() == -3) ||
			    (response.getStatus() == 3)) {
			    response.setStatus(-1);
			}
			break;

		    case TimeRelativeRequest.AT_OR_AFTER:
		    case TimeRelativeRequest.AFTER:
			// For requests before the time reference, use the
			// start of the current <code>Rmap</code>.
			rmap = getChildAt(lastIdx);
			response = rmap.afterTimeRelative(request,roI);
			if ((response.getStatus() == -3) ||
			    (response.getStatus() == 3)) {
			    response.setStatus(1);
			}
			break;
		    }

		} else if ((response.getStatus() == -3) ||
			   (response.getStatus() == 3)) {
		    if (lastGoodStatus != Integer.MIN_VALUE) {
			response.setStatus(lastGoodStatus);
		    }
		}

		if ((ridx == 0) || (response.getStatus() == -2)) {
		    // Use the current response as the overall one.
		    responseR = response;

		} else if (response.getStatus() != responseR.getStatus()) {
		    // If the status has changed, then we need to split the
		    // channels.
		    responseR.setStatus(-2);
		}
	    }
	}

	/*
	System.err.println("Rmap.matchTimeRelative: " + getFullName() +
			   " " + getTrange() + " " +
			   "\nAgainst: " + requestI + " " + roI +
			   "\ngot: " + responseR);
	*/

	return (responseR);
    }

    /**
     * Returns the members of this <code>Rmap</code> in a string
     * representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indentI the indentation string to use.
     * @return the string representation.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/25/2001  INB	Created.
     *
     */
    String membersToString(String indentI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String stringR = "";

	for (int idx = 0, eIdx = getNmembers();
	     idx < eIdx;
	     ++idx) {
	    stringR +=
		"\n" + getMemberAt(idx).toString(true,indentI + "--");
	}

	return (stringR);
    }

    /**
     * Merges the frame ranges of the input <code>Rmap</code> hierarchy into
     * a single entry.
     * <p>
     * All frame ranges are removed from the hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param referenceI the reference type, which determines how the merge is
     *			 done.
     * @param fRangeI    the merged range so far or null.
     * @return the merged frame range.
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
     * @see com.rbnb.api.DataRequest
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    final TimeRange mergeFrange(byte referenceI,TimeRange fRangeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	TimeRange fRangeR = fRangeI;
	if (getFrange() != null) {
	    if (fRangeR == null) {
		fRangeR = getFrange();
	    } else {
		double[] myLimits = getFrange().getLimits(),
		    rLimits = fRangeR.getLimits();
		switch (referenceI) {
		case DataRequest.AFTER:
		case DataRequest.MODIFIED:
		case DataRequest.NEWEST:
		    if (myLimits[1] > rLimits[1]) {
			fRangeR = getFrange();
		    }
		    break;

		case DataRequest.OLDEST:
		    if (myLimits[0] < rLimits[0]) {
			fRangeR = getFrange();
		    }
		    break;
		}
	    }
	    setFrange(null);
	}

	for (int idx = 0; idx < getNchildren(); ++idx) {
	    fRangeR = getChildAt(idx).mergeFrange(referenceI,fRangeR);
	}

	return (fRangeR);
    }

    /**
     * Merges the input <code>Rmap</code> hierarchy with this one.
     * <p>
     * The merge is done by working through first the group members of each of
     * the two <code>Rmaps</code> and then the children. Matching members and
     * children are merged using a recursive call. New members and children in
     * the input <code>Rmap</code> are added to this <code>Rmap</code>.
     * <p>
     * If the input <code>Rmap</code> doesn't match this one, then the method
     * tries three things:
     * <p><ol>
     * <li>if this <code>Rmap</code> is nameless and timeless and the input
     *     <code>Rmap</code> is not, but is a root <code>Rmap</code>, the
     *     method compares the input <code>Rmap</code> to the children of this
     *     <code>Rmap</code> and merges with any match or inserts the input
     *	   <code>Rmap</code> hierarchy as a new child, or</li>
     * <li>if the two <code>Rmaps</code> are the roots of their hierarchies and
     *	   this <code>Rmap</code> is not nameless and timeless, the method
     *     creates a new nameless and timeless root <code>Rmap</code> and adds 
     *     both of the original <code>Rmaps</code> as children,</li>
     * <li>throws an exception.</li>
     * <ol><p>
     * The status input <code>Rmap</code> after this method is called depends
     * on what exactly is done, but in general, it shouldn't be used
     * independently.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Rmap</code> to merge.
     * @return the merged <code>Rmap</code> (may be this <code>Rmap</code>).
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
     * @version 01/31/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/22/2000  INB	Created.
     *
     */
    public final Rmap mergeWith(Rmap rmapI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR = this,
	    copy;

	try {
	    if (compareTo(rmapI) != 0) {
		// If the two <code>Rmaps</code> do not compare equal, then we
		// need to perform the special checks.

		if ((getName() != null) ||
		    (getTrange() != null) ||
		    (getFrange() != null)) {
		    // If the two don't match because they should be peers,
		    // insert the new one into our parent.
		    copy = rmapI.duplicate();
		    copy.setParent(null);
		    getParent().addChild(copy);

		} else {
		    // Make the input <code>Rmap</code> a child of this one.
		    Rmap match = null;

		    if (getNchildren() > 0) {
			if ((rmapI.getName() == null) &&
			    (rmapI.getTrange() == null)) {
			    Rmap mine = getChildAt(0);

			    if ((mine.getName() == null) &&
				(mine.getTrange() == null)) {
				match = mine;
			    }
			} else {
			    match = (Rmap) getChildren().find(rmapI);
			}
		    }

		    if (match != null) {
			// If we can find a matching child, merge the two.
			match.mergeWith(rmapI);
		    } else {
			// With no match, just add the input <code>Rmap</code>
			// to this one.
			addChild(rmapI);
		    }
		}

	    } else {
		// If the two <code>Rmaps</code> match, then merge them.

		if ((getNmembers() != 0) || (rmapI.getNmembers() != 0)) {
		    // Right now, we don't allow for members.
		    throw new java.lang.IllegalStateException
			("Merging Rmaps with group members is not supported " +
			 "at this time.");
		}

		for (int idx = 0,
			 eIdx = rmapI.getNchildren();
		     idx < eIdx;
		     ++idx) {
		    Rmap theirs = rmapI.getChildAt(idx),
			mine = findChild(theirs);

		    if (mine == null) {
			copy = theirs.duplicate();
			addChild(copy);
		    } else {
			// If the two children compare equal, then merge them
			// and move on to the next pair.

			if ((mine.getDblock() == null) &&
			    (theirs.getDblock() == null)) {
			    // Without data, the two entries can be merged.
			    mine.mergeWith(theirs);

			} else {
			    // If either of the children has a data block, then
			    // we cannot actually merge them (nor do we really
			    // want to). Instead, we want them as peers.
			    copy = theirs.duplicate();
			    addChild(copy);
			}
		    }
		}
	    }

	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.InternalError();
	}

	return (rmapR);
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
     * @param unsatisfiedO  the new list of unsatisfied
     *			    <code>ExtractedChains</code>.
     * @return the reason for a failed match.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if there is a problem matching the request to the
     *		  source <code>Rmap</code> hierarchy or in extracting the
     *		  desired information.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/11/2000  INB	Created.
     *
     */
    byte moveDownFrom(RmapExtractor extractorI,
		      ExtractedChain unsatisfiedI,
		      java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = moveDownFrom((getNmembers() > 0) ? getMembers() : null,
				    getChildren(),
				    extractorI,
				    unsatisfiedI,
				    unsatisfiedO);
	return (reasonR);
    }

    /**
     * Moves down a level in the <code>Rmap</code> hierarchy by working on the
     * input <code>membersI</code> and <code>childrenI</code> lists.
     * <p>
     *
     * @author Ian Brown
     *
     * @param membersI      the members to work on.
     * @param childrenI     the children to work on.
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
     * @version 05/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/02/2003  INB	Call <code>Vector(1)</code> rather than
     *			<code>Vector()</code>.
     * 03/13/2001  INB	Created.
     *
     */
    final static byte moveDownFrom(RmapVector membersI,
				   RmapVector childrenI,
				   RmapExtractor extractorI,
				   ExtractedChain unsatisfiedI,
				   java.util.Vector unsatisfiedO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	byte reasonR = Rmap.MATCH_UNKNOWN;

	// Match the members and then the children.
	java.util.Vector unsatisfied = null;
	if ((membersI == null) || (membersI.size() == 0)) {
	    // If there are no members, we'll match the children against just
	    // the input <code>ExtractedChain</code>.
	    unsatisfied = new java.util.Vector(1);
	    unsatisfied.addElement(unsatisfiedI);

	} else {
	    // If there are members, then we need to work on them first. We'll
	    // build a new list of unsatisfied <code>ExtractedChains</code>.
	    unsatisfied = new java.util.Vector();

	    // Match unsatisified <code>ExtractedChain</code> against the
	    // members. This adds extracted information to the matched list and
	    // <code>ExtractedChains</code>to the new unsatsified list.
	    reasonR = unsatisfiedI.matchList(!extractorI.getExtractRmaps(),
					     membersI,
					     unsatisfied);
	}

	if ((unsatisfied == null) ||
	    (childrenI == null) ||
	    (childrenI.size() == 0) ||
	    (unsatisfied.size() == 0)) {
	    // If there are no children or unsatisfied
	    // <code>ExtractedChains</code>, copy any unsatisfied entries.
	    if ((unsatisfiedO != null) && (unsatisfied != null)) {
		for (int idx = 0,
			 endIdx = unsatisfied.size();
		     idx < endIdx;
		     ++idx) {
		    unsatisfiedO.addElement(unsatisfied.elementAt(idx));
		}
	    }

	} else {
	    // If there are children and there are still unsatisified
	    // <code>ExtractedChains</code>, then match the children against
	    // the unsatisified list.
	    for (int idx = 0,
		     endIdx = unsatisfied.size();
		 idx < endIdx;
		 ++idx) {
		// Match each of the unsatisified extracted chains in sequence.
		ExtractedChain eChain = (ExtractedChain)
		    unsatisfied.elementAt(idx);

		// Match the extracted chain against the children. This adds
		// extracted information to the matched list and
		// <code>ExtractedChains</code>to the return unsatsified list.
		byte reason = eChain.matchList(!extractorI.getExtractRmaps(),
					       childrenI,
					       unsatisfiedO);

		reasonR = Rmap.combineReasons(reasonR,reason);
	    }
	}

	return (reasonR);
    }

    /**
     * Moves to the bottom of this <code>Rmap</code> hierarchy.
     * <p>
     * This method is intended for use in conjunction with
     * <code>createFromName</code>. At each point in the hierarchy, it gets the
     * first child and moves down until it reaches an <code>Rmap</code> with no
     * children.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the bottom <code>Rmap</code>.
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
     * @see #createFromName(String)
     * @see #moveToTop()
     * @since V2.0
     * @version 01/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  INB	Created.
     *
     */
    public final Rmap moveToBottom()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap bottomR;

	for (bottomR = this;
	     bottomR.getNchildren() > 0;
	     bottomR = bottomR.getChildAt(0)) {
	}

	return (bottomR);
    }

    /**
     * Moves to the top of this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the top <code>Rmap</code>.
     * @see #moveToBottom()
     * @since V2.0
     * @version 01/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  INB	Created.
     *
     */
    public final Rmap moveToTop() {
	Rmap topR;

	for (topR = this;
	     topR.getParent() != null;
	     topR = topR.getParent()) {
	}

	return (topR);
    }

    /**
     * Creates a new instance of the same class as this <code>Rmap</code> (or a
     * similar class).
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
	Rmap rmapR = null;

	try {
	    rmapR = (Rmap) getClass().newInstance();
	    rmapR.setName(getName());
	} catch (java.lang.IllegalAccessException e) {
	    throw new java.lang.InternalError();
	} catch (java.lang.InstantiationException e) {
	    throw new java.lang.InternalError();
	} catch (java.lang.SecurityException e) {
	    throw new java.lang.InternalError();
	}

	return (rmapR);
    }

    /**
     * Nullifies this <code>Rmap</code>.
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
     * 10/23/2003  INB	Clean out our children first.
     * 07/30/2003  INB	Created.
     *
     */
    public void nullify() {
	try {
	    Rmap child;
	    boolean isMine;
	    while (getNchildren() > 0) {
		child = (Rmap) getChildAt(0);
		isMine = (child.getParent() == this);
		removeChildAt(0);
		if (isMine) {
		    child.nullify();
		}
	    }
	    setChildren(null);

	    Rmap member;
	    while (getNmembers() > 0) {
		member = (Rmap) getMemberAt(0);
		removeMemberAt(0);
		member.nullify();
	    }
	    groupList = null;

	    setParent(null);
	    setName(null);
	    setDblock(null);
	    setFrange(null);
	    setTrange(null);

	} catch (java.lang.Throwable e) {
	}
    }

    /**
     * Pulls out a hierarchy of named <code>Rmaps</code> by eliminating the
     * matching names.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pullHierarchyI the hierarchy of <code>Rmaps</code> with the names
     *			     to be pulled out.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    final void pullOut(Rmap pullHierarchyI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap pullHierarchy = pullHierarchyI;

	if ((getName() == null) ||
	    (compareNames(pullHierarchy.getName()) == 0)) {
	    if (getName() != null) {
		pullHierarchy =
		    ((pullHierarchy.getNchildren() == 0) ?
		     null :
		     pullHierarchy.getChildAt(0));
	    }
	    setName(null);
	    if (pullHierarchy != null) {
		for (int idx = 0,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    getChildAt(idx).pullOut(pullHierarchy);
		}
	    }
	}
    }

    /**
     * Reads the <code>Rmap</code> from the specified input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
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
    void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	read(null,isI,disI);
    }

    /**
     * Reads the <code>Rmap</code> from the specified input stream.
     * <p>
     * The input <code>Rmap</code> is used to fill in any fields that are not
     * seen.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param isI    the input stream.
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
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
     * 07/30/2001  INB	Created.
     *
     */
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean seen[] = new boolean[PARAMETERS.length];

	// Read the open bracket marking the start of the <code>Rmap</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    seen[parameter] = true;

	    // Read parameters until we see a closing bracket.
	    readStandardParameter(otherI,parameter,isI,disI);
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads the data for this <code>Rmap</code> and its children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param disI   the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #writeData(com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2001  INB	Created.
     *
     */
    void readData(DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getDblock() != null) {
	    getDblock().readData(disI);
	}

	for (int idx = 0, eIdx = getNchildren(); idx < eIdx; ++idx) {
	    getChildAt(idx).readData(disI);
	}

	for (int idx = 0, eIdx = getNmembers(); idx < eIdx; ++idx) {
	    getMemberAt(idx).readData(disI);
	}
    }

    /**
     * Reads standard parameters.
     * <p>
     * This method handles parameters that may be defaulted by copying missing
     * information from the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parameterI  the parameter index.
     * @param isI	  the input stream.
     * @param disI	  the data input stream.
     * @return was the parameter recognized?
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
     * @see #writeStandardParameters(com.rbnb.api.Rmap,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 10/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    final boolean readStandardParameter(com.rbnb.api.Rmap otherI,
					int parameterI,
					InputStream isI,
					DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean standardR = true;

	try {
	    switch (parameterI) {
	    case PAR_CHL:
	    case PAR_GRP:
		// Read the open bracket marking the start of the children or
		// members.
		Serialize.readOpenBracket(isI);

		if (otherI == null) {
		    Rmap entry,
			previous = null;

		    while ((entry = (Rmap)
			    Language.read(previous,isI,disI)) != null) {
			if (parameterI == PAR_CHL) {
			    appendChild(entry);
			} else {
			    appendMember(entry);
			}
			previous = entry;
		    }

		} else {
		    java.util.Vector vector;
		    int oldIdx = 0,
			oldEIdx = ((otherI == null) ? 0 :
				   ((parameterI == PAR_CHL) ?
				    otherI.getNchildren() :
				    otherI.getNmembers())),
			offset = 0,
			number = 0;

		    while ((vector = Language.readRmapUpdate
			    (otherI,
			     offset,
			     (parameterI == PAR_CHL),
			     isI,
			     disI)) != null) {
			int command =
			    ((Integer) vector.firstElement()).intValue(),
			    index;
			Serializable descendant = null;

			switch (command) {
			case Language.ADD:
			    descendant = (Serializable) vector.lastElement();
			    if (descendant instanceof ExceptionMessage) {
				Language.throwException
				    ((ExceptionMessage) descendant);
			    } else if (!(descendant instanceof Rmap)) {
				throw new com.rbnb.api.SerializeException
				    ("Unexpected object " + descendant +
				     " when reading Rmap.");
			    }
			    if (parameterI == PAR_CHL) {
				appendChild((Rmap) descendant);
			    } else {
				appendMember((Rmap) descendant);
			    }
			    ++number;
			    --offset;
			    break;

			case Language.REMOVE:
			    index = ((Integer)
				     vector.lastElement()).intValue();
			    if (parameterI == PAR_CHL) {
				for (; index > number; ++number) {
				    Rmap oldEntry =
					otherI.getChildAt(oldIdx++),
					newEntry = (Rmap)
					oldEntry.getClass().newInstance();
				    newEntry.defaultParameters(oldEntry,null);
				    appendChild(newEntry);
				}
			    } else {
				for (; index > number; ++number) {
				    Rmap oldEntry =
					otherI.getMemberAt(oldIdx++),
					newEntry = (Rmap)
					oldEntry.getClass().newInstance();
				    newEntry.defaultParameters(oldEntry,null);
				    appendMember(newEntry);
				}
			    }
			    ++oldIdx;
			    ++offset;
			    break;

			case Language.COPY:
			    throw new com.rbnb.api.SerializeException
				("Unexpected copy Rmap update for " + vector);

			case Language.EDIT:
			    index = ((Integer) vector.elementAt(1)).intValue();
			    descendant = (Serializable) vector.lastElement();
			    if (parameterI == PAR_CHL) {
				for (; index > number; ++number) {
				    Rmap oldEntry =
					otherI.getChildAt(oldIdx++),
					newEntry = (Rmap)
					oldEntry.getClass().newInstance();
				    newEntry.defaultParameters(oldEntry,null);
				    appendChild(newEntry);
				}
			    } else {
				for (; index > number; ++number) {
				    Rmap oldEntry =
					otherI.getMemberAt(oldIdx++),
					newEntry = (Rmap)
					oldEntry.getClass().newInstance();
				    newEntry.defaultParameters(oldEntry,null);
				    appendMember(newEntry);
				}
			    }
			    if (parameterI == PAR_CHL) {
				appendChild((Rmap) descendant);
			    } else {
				appendMember((Rmap) descendant);
			    }
			    ++number;
			    ++oldIdx;
			}
		    }

		    if (otherI != null) {
			if (parameterI == PAR_CHL) {
			    for (; oldIdx < oldEIdx; ++oldIdx) {
				Rmap oldEntry = otherI.getChildAt(oldIdx),
				    newEntry = (Rmap)
				    oldEntry.getClass().newInstance();
				newEntry.defaultParameters(oldEntry,null);
				appendChild(newEntry);
				++number;
			    }
			} else {
			    for (; oldIdx < oldEIdx; ++oldIdx) {
				Rmap oldEntry = otherI.getMemberAt(oldIdx),
				    newEntry = (Rmap)
				    oldEntry.getClass().newInstance();
				newEntry.defaultParameters(oldEntry,null);
				appendMember(newEntry);
				++number;
			    }
			}
		    }
		}
		break;

	    case PAR_DBK:
		setDblock(new DataBlock(((otherI == null) ?
					 null :
					 otherI.getDblock()),
					isI,
					disI));
		break;

	    case PAR_FRG:
		setFrange(new TimeRange(((otherI == null) ?
					 null :
					 otherI.getFrange()),
					isI,
					disI));
		break;

	    case PAR_NAM:
		setName(isI.readUTF());
		break;

	    case PAR_TRG:
		setTrange(new TimeRange(((otherI == null) ?
					 null :
					 otherI.getTrange()),
					isI,
					disI));
		break;

	    default:
		standardR = false;
		break;
	    }

	} catch (java.lang.IllegalAccessException e) {
	    throw new java.lang.InternalError();

	} catch (java.lang.InstantiationException e) {
	    throw new java.lang.InternalError();
	}

	return (standardR);
    }

    /**
     * Removes a child <code>Rmap</code> from this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param childI the child to remove.
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
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #getParent()
     * @see #removeChildAt(int)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public void removeChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((getNchildren() > 0) &&
	    (childI.getParent() == this) &&
	    (getChildren().indexOf(childI) >= 0)) {
	    // If there are children and the child <code>Rmap</code> is one of
	    // them, remove that child.
	    getChildren().remove(childI);
	    childI.setParent(null);
	}
    }

    /**
     * Removes the child <code>Rmap</code> at a particular index from this
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of child to remove.
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
     * @see #addChild(com.rbnb.api.Rmap)
     * @see #getParent()
     * @see #removeChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public void removeChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getNchildren() > indexI) {
	    // If there are children, try to remove the entry.
	    Rmap child = (Rmap) children.elementAt(indexI);
	    getChildren().removeEntryAt(indexI);
	    child.setParent(null);
	}
    }

    /**
     * Removes a member <code>Rmap</code> from this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI the member to remove.
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
     * @see #addMember(com.rbnb.api.Rmap)
     * @see #getParent()
     * @see #removeMemberAt(int)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public void removeMember(Rmap memberI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((getNmembers() > 0) &&
	    (memberI.getParent() == this) &&
	    (getMembers().indexOf(memberI) >= 0)) {
	    // If there are members and the member <code>Rmap</code> is one of
	    // them, remove that member.
	    getMembers().remove(memberI);
	    memberI.setParent(null);
	}
    }

    /**
     * Removes the member <code>Rmap</code> at a particular index from this
     * <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of member to remove.
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
     * @see #addMember(com.rbnb.api.Rmap)
     * @see #getParent()
     * @see #removeMember(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public void removeMemberAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getNmembers() > indexI) {
	    // If there are members, try to remove the entry.
	    Rmap member = (Rmap) getMembers().elementAt(indexI);

	    getMembers().removeEntryAt(indexI);
	    member.setParent(null);
	}
    }

    /**
     * Sets the children of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param chilrenI  the new children list.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #getChildren()
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    final void setChildren(RmapVector childrenI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	children = childrenI;
    }

    /**
     * Sets the <code>DataBlock</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dblockI  the <code>DataBlock</code>.
     * @see #getDblock()
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setDblock(DataBlock dblockI) {
	dataBlock = dblockI;
    }

    /**
     * Sets the frame index <code>TimeRange</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frangeI  the frame <code>TimeRange</code>.
     * @see #getFrange()
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    public final void setFrange(TimeRange frangeI) {
	frameRange = frangeI;
    }

    /**
     * Sets the group members of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param membersI  the new members list.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #getMembers()
     * @since V2.0
     * @version 08/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/15/2000  INB	Created.
     *
     */
    final void setMembers(RmapVector membersI)
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	groupList = membersI;
    }

    /**
     * Sets the name of this <code>Rmap</code>.
     * <p>
     * If the <code>Rmap</code> has a parent <code>Rmap</code>, then changing
     * the name of this <code>Rmap</code> may effect the sort order of its
     * parent's list. To ensure that this change is made properly, this
     * method temporarily removes this <code>Rmap</code> from its parent,
     * updates the name, and adds this <code>Rmap</code> back in to its
     * parent.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name of the <code>Rmap</code>.
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
     * @see #getName()
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 11/30/2000  INB	Created.
     *
     */
    public final void setName(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((name == nameI) ||
	    ((name != null) &&
	     (nameI != null) &&
	     name.equals(nameI))) {
	    return;
	}
	Rmap myParent = getParent();
	boolean isChild = true;

	if (myParent != null) {
	    // If this object is a child or member of another
	    // <code>Rmap</code>, determine which it is and the remove it.
	    boolean doRemove = true;

	    if ((myParent.getNchildren() == 0) ||
		(myParent.getChildren().indexOf(this) == -1)) {
		if ((myParent.getNmembers() == 0) ||
		    (myParent.getMembers().indexOf(this) == -1)) {
		    doRemove = false;
		} else {
		    isChild = false;
		}
	    }

	    if (doRemove) {
		if (isChild) {
		    myParent.removeChild(this);
		} else {
		    myParent.removeMember(this);
		}
	    }
	}

	// Update the name.
	name = nameI;

	if (myParent != null) {
	    // If this <code>Rmap</code> belonged to another<code>Rmap</code>,
	    // then rebuild the relationship.
	    if (isChild) {
		myParent.addChild(this);
	    } else {
		myParent.addMember(this);
	    }
	}
    }

    /**
     * Sets the parent <code>Rmap</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI  the parent <code>Rmap</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if this <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> and the input is non-null.
     * @see #getParent()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    void setParent(Rmap parentI) {
	if ((parentI != null) &&
	    (getParent() != null) &&
	    (parentI != getParent())) {
	    // Cannot have two parents.
	    throw new java.lang.IllegalArgumentException
		(getClass() + " " + getName() + " is already a child of " +
		 getParent().getClass() + " " + getParent().getName() +
		 " and cannot be a child of " +
		 parentI.getClass() + " " + parentI.getName() +
		 ".");
	}

	parent = parentI;
    }

    /**
     * Sets the <code>TimeRange</code> of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param trangeI  the <code>TimeRange</code>.
     * @see #getTrange()
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final void setTrange(TimeRange trangeI) {
	timeRange = trangeI;
    }

    /**
     * Gets the sorting value for this <code>Rmap</code>.
     * <p>
     * The sort identifier for <code>Rmaps</code> is the <code>Rmap</code>
     * itself.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI  the sort type identifier -- must be null.
     * @return the sort value.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is non-null.
     * @see #compareTo(Object,Object)
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public Object sortField(Object sidI)
	throws com.rbnb.utility.SortException
    {
	if (sidI != null) {
	    // Only the null sort identifier is supported.
	    throw new com.rbnb.utility.SortException
		("The sort identifier for Rmaps must be null.");
	}

	return (this);
    }

    /**
     * Strips dot (".") names out of the leading part of the <code>Rmap</code>
     * hierarchy.
     * <p>
     * This method is used by <code>RBOs</code> to ensure that "."
     * <code>Rmaps</code> do not show up in the registration or data maps.
     * <p>
     *
     * @author Ian Brown
     *
     * @return name found or stripped a dot?
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
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/05/2002  INB	Created.
     *
     */
    final boolean stripDot()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean strippedR = true;

	if (compareNames(".") == 0) {
	    setName(null);
	} else if (getName() == null) {
	    strippedR = false;
	    for (int idx = 0,
		     endIdx = getNmembers();
		 idx < endIdx;
		 ++idx) {
		Rmap member = getMemberAt(idx);

		strippedR = member.stripDot() || strippedR;
	    }
	    if (!strippedR) {
		for (int idx = 0,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    Rmap child = getChildAt(idx);

		    strippedR = child.stripDot() || strippedR;
		}
	    }
	}

	return (strippedR);
    }

    /**
     * Summarizes this <code>Rmap</code>.
     * <p>
     * The summary produces a <code>Rmap</code> that contains a
     * <code>TimeRange</code> and a frame <code>TimeRange</code> that span all
     * of the time and frame <code>TimeRanges</code> in this hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the summary <code>Rmap</code>.
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
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/08/2001  INB	Created.
     *
     */
    final Rmap summarize()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] need = new boolean[2];
	need[0] =
	    need[1] = true;
	double[][] limits = new double[2][];
	boolean[] inclusive = new boolean[2];
	inclusive[0] = false;
	inclusive[1] = true;

	// Produce the summary using a recursive sequence.
	summarizeRanges(need,limits,inclusive);

	Rmap summaryR = newInstance();
	summaryR.setName(getName());
	TimeRange rrange;
	if (limits[0] != null) {
	    summaryR.setTrange
		(rrange = new TimeRange(limits[0][0],
					limits[0][1] - limits[0][0]));
	    rrange.setInclusive(inclusive[0]);
	}
	if (limits[1] != null) {
	    summaryR.setFrange
		(rrange = new TimeRange(limits[1][0],
					limits[1][1] - limits[1][0]));
	    rrange.setInclusive(true);
	}

	return (summaryR);
    }

    /**
     * Summarizes the <code>TimeRange</code> and frame <code>TimeRange</code>
     * for this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param needI       what limits do we need to find?
     * @param limitsIO    the calculated limits.
     * @param inclusiveIO are the ranges inclusive of both ends?
     * @return what limits are still needed?
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
     * @since V2.0
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/08/2001  INB	Created.
     *
     */
    private final boolean[] summarizeRanges(boolean[] needI,
					    double[][] limitsIO,
					    boolean[] inclusiveIO)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean[] needR = new boolean[2];
	System.arraycopy(needI,0,needR,0,2);

	if (needI[0] && getTrange() != null) {
	    // If we need the time information and there is a
	    // <code>TimeRange</code>, then update the output
	    // <code>TimeRange</code>.
	    double[] oLimits = getTrange().getLimits();
	    if (limitsIO[0] == null) {
		limitsIO[0] = oLimits;
		inclusiveIO[0] = getTrange().isInclusive();
	    } else {
		double startAt = Math.min(limitsIO[0][0],oLimits[0]),
		    endAt = Math.max(limitsIO[0][1],oLimits[1]);

		limitsIO[0][0] = startAt;
		limitsIO[0][1] = endAt;
		if (endAt == oLimits[1]) {
		    inclusiveIO[0] = getTrange().isInclusive();
		}
	    }
	    needR[0] = false;
	}

	if (needI[1] && getFrange() != null) {
	    // If we need the frame information and there is a
	    // frame <code>TimeRange</code>, then update the output frame
	    // <code>TimeRange</code>.
	    double[] oLimits = getFrange().getLimits();
	    if (limitsIO[1] == null) {
		limitsIO[1] = oLimits;
	    } else {
		double startAt = Math.min(limitsIO[1][0],oLimits[0]),
		    endAt = Math.max(limitsIO[1][1],oLimits[1]);

		limitsIO[1][0] = startAt;
		limitsIO[1][1] = endAt;
	    }
	    inclusiveIO[1] = true;
	    needR[1] = false;
	}

	if (needR[0] || needR[1]) {
	    // If we still need something, try working on the members and then
	    // the children.
	    boolean[] startWith = needR;

	    if (getNmembers() > 0) {
		// If there are members, see what we can learn from them.
		needR = new boolean[2];
		needR[0] =
		    needR[1] = false;

		for (int idx = 0,
			 endIdx = getNmembers();
		     idx < endIdx;
		     ++idx) {
		    Rmap member = (Rmap) getMemberAt(idx);
		    boolean[] result = member.summarizeRanges(startWith,
							      limitsIO,
							      inclusiveIO);

		    needR[0] |= result[0];
		    needR[1] |= result[1];
		}
	    }

	    if ((needR[0] || needR[1]) && (getNchildren() > 0)) {
		// If we still need something and there are children, see what
		// we can learn from them.
		startWith = needR;
		needR = new boolean[2];
		needR[0] =
		    needR[1] = false;

		for (int idx = 0,
			 endIdx = getNchildren();
		     idx < endIdx;
		     ++idx) {
		    Rmap child = (Rmap) getChildAt(idx);
		    boolean[] result = child.summarizeRanges(startWith,
							     limitsIO,
							     inclusiveIO);

		    needR[0] |= result[0];
		    needR[1] |= result[1];
		}
	    }
	}

	return (needR);
    }

    /**
     * Creates an <code>Rmap</code> hierarchy that can be used to make requests
     * for the names.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name <code>Rmap</code> hierarchy.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/29/2001  INB	Created.
     *
     */
    public Rmap toNameHierarchy()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap rmapR;
	rmapR = newInstance();
	if (rmapR instanceof ClientInterface) {
	    ClientInterface mi = (ClientInterface) this,
			    ci = (ClientInterface) rmapR;

	    ci.setType(mi.getType());
	    ci.setRemoteID(mi.getRemoteID());
	}

	for (int idx = 0, eIdx = getNmembers(); idx < eIdx; ++idx) {
	    // Copy the name hierarchy for each member.
	    Rmap member = getMemberAt(idx).toNameHierarchy(),
		 matches = rmapR.findMember(member);

	    if (matches == null) {
		// If we haven't seen a previous entry with this name, just add
		// the new one in.
		rmapR.addMember(member);

	    } else {
		// If we have seen a previous entry, merge the two.
		matches.mergeWith(member);
	    }
	}

	for (int idx = 0, eIdx = getNchildren();
	     idx < eIdx;
	     ++idx) {
	    // Copy the name hierarchy for each child.
	    Rmap child = getChildAt(idx).toNameHierarchy(),
		 entry = child,
		 matches;
	    boolean done = false;

	    while (!done) {
		// Nameless children are removed from the middle of the list as
		// they serve little purpose.

		if ((child.getName() != null) ||
		    (child.getNmembers() > 0)) {
		    // If the child has a name or members, add it in.
		    done = true;

		} else if (child.getNchildren() > 0) {
		    // If the child has no name or members, but has children,
		    // add those children.
		    entry = child.getChildAt(0);
		    child.removeChildAt(0);
		    done = (child.getNchildren() == 0);

		} else {
		    // If the child has nothing of interest, skip it.
		    break;
		}
			
		matches = rmapR.findChild(entry);
		if (matches == null) {
		    // If we haven't seen a previous entry with this name, just
		    // add the new one in.
		    rmapR.addChild(entry);

		} else {
		    // If we have seen a previous entry, merge the two.
		    matches.mergeWith(entry);
		}
	    }
	}

	return (rmapR);
    }

    /**
     * Gets a displayable string representation of this <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public String toString() {
	try {
	    return (toString(false,""));
	} catch (InterruptedException e) {
	    return (null);
	}
    }

    /**
     * Gets a displayable string representation of this <code>Rmap</code>.
     * <p>
     * This method is the workhorse that actually does the work under the
     * the standard <code>toString()</code> method. It builds a representation
     * that shows the entire structure of the <code>Rmap</code>, including the
     * group members and the children.
     * <p>
     * The input parameters specify whether this <code>Rmap</code> is a group
     * member of its parent (or a child) and the identation string to prepend
     * to put this <code>Rmap</code> under its parent.
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI is this <code>Rmap</code> a group member?
     * @param indentI the indentation string to use.
     * @return the string representation.
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
     * 11/30/2000  INB	Created.
     *
     */
    public String toString(boolean memberI,String indentI)
	throws java.lang.InterruptedException
    {
	try {
	    String className = getClass().toString();
	    className = className.substring
		(className.lastIndexOf(".") + 1);

	    String stringR =
		indentI +
		className + " " +
		((getName() == null) ? "?" : getName()) +
		((getTrange() == null) ? "" : (" " + getTrange())) +
		((getFrange() == null) ? "" : (" F" + getFrange())) +
		((getDblock() == null) ? "" : (" " + getDblock()));

	    stringR += additionalToString();
	    stringR += membersToString(indentI);
	    stringR += childrenToString(indentI);
	    return (stringR);

	} catch (com.rbnb.api.AddressException e) {
	    return (null);

	} catch (com.rbnb.api.SerializeException e) {
	    return (null);

	} catch (java.io.EOFException e) {
	    return (null);

	} catch (java.io.IOException e) {
	    return (null);
	}
    }

    /**
     * Unlocks this <code>Rmap</code> hierarchy for read access.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 06/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/14/2001  INB	Created.
     *
     */
    void unlockRead()
	throws java.lang.InterruptedException
    {
    }

    /**
     * Updates the limits of a request based on the input limits.
     * <p>
     *
     * @author Ian Brown
     *
     * @param endI     update the end? If false, update the beginning.
     * @param tLimitsI the time limits.
     * @param fLimitsI the frame limits.
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
     * @version 01/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/13/2003  INB	Created.
     *
     */
    final void updateLimits(boolean endI,TimeRange tLimitsI,TimeRange fLimitsI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getTrange() != null) {
	    if (endI) {
		getTrange().set((-getTrange().getTime() +
				 tLimitsI.getTime() +
				 tLimitsI.getDuration() -
				 getTrange().getDuration()),
				getTrange().getDuration());
	    } else {
		getTrange().set(getTrange().getTime() + tLimitsI.getTime(),
				getTrange().getDuration());
	    }

	} else if (getFrange() != null) {
	    if (endI) {
		getFrange().set((-getFrange().getTime() +
				 fLimitsI.getTime() +
				 fLimitsI.getDuration() -
				 getFrange().getDuration()),
				getFrange().getDuration());
	    } else {
		getFrange().set(getFrange().getTime() + fLimitsI.getTime(),
				getFrange().getDuration());
	    }

	} else {
	    for (int idx = 0; idx < getNchildren(); ++idx) {
		getChildAt(idx).updateLimits(endI,tLimitsI,fLimitsI);
	    }
	}
    }

    /**
     * Writes this <code>Rmap</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  our parent's parameter list.
     * @param parameterI   the parent parameter to use.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
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
    final void write(String[] parametersI,
	       int parameterI,
	       OutputStream osI,
	       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	write(null,parametersI,parameterI,osI,dosI);
    }

    /**
     * Writes this <code>Rmap</code> to the specified stream.
     * <p>
     * This version compares this object to the input one and writes out only
     * the differences.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>Rmap</code>.
     * @param parametersI  our parent's parameter list.
     * @param parameterI   the parent parameter to use.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 08/15/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/27/2001  INB	Created.
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
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged(this,parametersI,parameterI);

	writeStandardParameters(otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /**
     * Writes the data for this <code>Rmap</code> and its children.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dosI the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.InterruptedIOException
     *		  thrown if the operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #readData(com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 09/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2001  INB	Created.
     *
     */
    void writeData(DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getDblock() != null) {
	    getDblock().writeData(dosI);
	}

	for (int idx = 0, eIdx = getNchildren(); idx < eIdx; ++idx) {
	    getChildAt(idx).writeData(dosI);
	}

	for (int idx = 0, eIdx = getNmembers(); idx < eIdx; ++idx) {
	    getMemberAt(idx).writeData(dosI);
	}
    }

    /**
     * Writes out standard parameters.
     * <p>
     * This version writes out differences between this <code>Rmap</code> and
     * the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Rmap</code>.
     * @param osI    the output stream.
     * @param dosI   the data output stream.
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
     * @see #readStandardParameter(com.rbnb.api.Rmap,int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 10/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    final void writeStandardParameters(Rmap otherI,
				       OutputStream osI,
				       DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	if ((getName() != null) &&
	    ((otherI == null) ||
	     ((otherI != null) &&
	      (otherI.nameNSD || (compareNames(otherI) != 0))))) {
	    osI.writeParameter(PARAMETERS,PAR_NAM);
	    osI.writeUTF(getName());
	}

	if (getTrange() != null) {
	    long before = osI.getWritten();
	    getTrange().write(((otherI == null) ? null : otherI.getTrange()),
			      PARAMETERS,
			      PAR_TRG,
			      osI,
			      dosI);
	}

	if (getFrange() != null) {
	    long before = osI.getWritten();
	    getFrange().write((otherI == null) ? null : otherI.getFrange(),
			      PARAMETERS,
			      PAR_FRG,
			      osI,
			      dosI);
	}

	int eIdx;

	if (otherI == null) {
	    if ((eIdx = getNmembers()) > 0) {
		osI.writeParameter(PARAMETERS,PAR_GRP);
		Serialize.writeOpenBracket(osI);
		Language.write(getMemberAt(0),null,osI,dosI);
		Rmap previous = getMemberAt(0);
		for (int idx = 1; idx < eIdx; ++idx) {
		    Rmap member = getMemberAt(idx);

		    if (previous != null) {
			previous.nameNSD = true;
		    }
		    Language.write(member,
				   previous,
				   osI,
				   dosI);
		    if (previous != null) {
			previous.nameNSD = false;
		    }
		    previous = member;
		}
		Serialize.writeCloseBracket(osI);
	    }

	    if ((eIdx = getNchildren()) > 0) {
		osI.writeParameter(PARAMETERS,PAR_CHL);
		Serialize.writeOpenBracket(osI);
		Language.write(getChildAt(0),null,osI,dosI);
		Rmap previous = getChildAt(0);
		for (int idx = 1; idx < eIdx; ++idx) {
		    Rmap child = getChildAt(idx);

		    if (previous != null) {
			previous.nameNSD = true;
		    }

		    Language.write(child,
				   previous,
				   osI,
				   dosI);
		    if (previous != null) {
			previous.nameNSD = false;
		    }
		    previous = child;
		}
		Serialize.writeCloseBracket(osI);
	    }

	} else {
	    eIdx = getNmembers();
	    int eIdx1 = otherI.getNmembers();

	    if ((eIdx > 0) || (eIdx1 > 0)) {
		long before = osI.getWritten();
		int valid = osI.setStage(true,false);
		osI.addStaged(PARAMETERS,PAR_GRP,true);

		int idx = 0,
		    idx1 = 0;
		Rmap newE = ((idx < eIdx) ?
			     getMemberAt(idx) :
			     null),
		    oldE = ((idx1 < eIdx1) ?
			    otherI.getMemberAt(idx1) :
			    null);
		while ((idx < eIdx) || (idx1 < eIdx1)) {
		    int operation = Language.writeRmapUpdate(idx,
							     newE,
							     oldE,
							     osI,
							     dosI);

		    switch (operation) {
		    case Language.ADD:
		    case Language.COPY:
			++idx;
			newE = ((idx < eIdx) ? getMemberAt(idx) : null);
			break;

		    case Language.EDIT:
			++idx;
			newE = ((idx < eIdx) ? getMemberAt(idx) : null);
			++idx1;
			oldE = ((idx1 < eIdx1) ?
				  otherI.getMemberAt(idx1) :
				  null);
			break;

		    case Language.REMOVE:
			++idx1;
			oldE = ((idx1 < eIdx1) ?
				  otherI.getMemberAt(idx) :
				  null);
			break;
		    }
		}

		if (osI.getWritten() > before) {
		    Serialize.writeCloseBracket(osI);
		} else if (valid >= 0) {
		    osI.removeStaged(valid);
		}
	    }

	    eIdx = getNchildren();
	    eIdx1 = otherI.getNchildren();
	    if ((eIdx > 0) || (eIdx1 > 0)) {
		long before = osI.getWritten();
		int valid = osI.setStage(true,false);
		osI.addStaged(PARAMETERS,PAR_CHL,true);

		int idx = 0,
		    idx1 = 0;
		Rmap newE = ((idx < eIdx) ?
			     getChildAt(idx) :
			     null),
		    oldE = ((idx1 < eIdx1) ?
			    otherI.getChildAt(idx1) :
			    null);
		while ((idx < eIdx) || (idx1 < eIdx1)) {
		    int operation = Language.writeRmapUpdate(idx,
							     newE,
							     oldE,
							     osI,
							     dosI);

		    switch (operation) {
		    case Language.ADD:
		    case Language.COPY:
			++idx;
			newE = ((idx < eIdx) ? getChildAt(idx) : null);
			break;

		    case Language.EDIT:
			++idx;
			newE = ((idx < eIdx) ? getChildAt(idx) : null);
			++idx1;
			oldE = ((idx1 < eIdx1) ?
				  otherI.getChildAt(idx1) :
				  null);
			break;

		    case Language.REMOVE:
			++idx1;
			oldE = ((idx1 < eIdx1) ?
				  otherI.getChildAt(idx1) :
				  null);
			break;
		    }
		}

		if (osI.getWritten() > before) {
		    Serialize.writeCloseBracket(osI);
		} else if (valid >= 0) {
		    osI.removeStaged(valid);
		}
	    }
	}

	if (getDblock() != null) {
	    long before = osI.getWritten();
	    getDblock().write((otherI == null) ? null : otherI.getDblock(),
			      PARAMETERS,
			      PAR_DBK,
			      osI,
			      dosI);
	}
    }

    /**
     * Produces an XML description of this <code>Rmap</code> for the
     * registration map.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the XML description.
     * @since V2.0
     * @version 2005/05/12
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/09/2002  INB	Created.
     * 2005/03/22  WHF  Includes TYPE_USER data, if any.
     * 2005/05/12  JPW  To keep the code Java 1.1.4 compatible, I replaced
     *                  the call to Vector.get(0) with Vector.elementAt(0)
     *
     */
    final String xmlRegistration() {
	return (xmlRegistration(getDblock().getPtsize(),
				getDblock().getMIMEType(),
				getDblock().getDtype() == DataBlock.TYPE_USER?
					"<user>"+new String((byte[])
					getDblock().getData().elementAt(0))
					+"</user>":null));
    }

    /**
     * Produces an XML description of the specified length for the registration
     * map.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lengthI   the length.
     * @param mimeTypeI the MIME type of the data.
     * @return the XML description.
     * @since V2.0
     * @version 01/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2002  INB	Created.
     *
     */
    public final static String xmlRegistration(long lengthI,String mimeTypeI) {
	return (xmlRegistration(lengthI,mimeTypeI,null));
    }

    /**
     * Produces an XML description of the specified length and the MIME type
     * for the registration map. Additional XML is added if desired.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lengthI	the length.
     * @param mimeTypeI the MIME type of the data.
     * @param xmlI	the additional XML to add, if any.
     * @return the XML description.
     * @since V2.0
     * @version 01/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/17/2002  INB	Created.
     *
     */
    public final static String xmlRegistration(long lengthI,
					       String mimeTypeI,
					       String xmlI)
    {
	String xmlR = ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
		       "<!DOCTYPE rbnb>\n" +
		       "<rbnb>\n" +
		       "  <size>" + lengthI + "</size>\n" +
		       ((mimeTypeI != null) ?
			"  <mime>" + mimeTypeI + "</mime>\n" :
			"") +
		       ((xmlI == null) ? "" : "  "+xmlI+"\n") +
		       "</rbnb>\n");
	return (xmlR);
    }
}
