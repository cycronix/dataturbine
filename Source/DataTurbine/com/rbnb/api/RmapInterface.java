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
 * <code>Rmap</code> methods interface.
 * <p>
 * This interface provides a description of an <code>Rmap</code> that provides
 * the minimum number of methods necessary to access generally useful
 * capabilities of an <code>Rmap</code> through other interfaces.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/28/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/28/2004  JPW	Declare the interface public; otherwise, J# has
 *			compile-time errors.
 * 05/10/2001  INB	Created.
 *
 */
public interface RmapInterface
    extends java.lang.Cloneable,
	    java.io.Serializable

{

    /**
     * Adds a child <code>Rmap</code> to this <code>RmapInterface</code>.
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
     *		  child of another <code>Rmap</code> or if the input
     *		  is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #removeChild(com.rbnb.api.Rmap)
     * @see #removeChildAt(int)
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public abstract void addChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Clones this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @see java.lang.Cloneable
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
    public abstract Object clone();

    /**
     * Compares the name of this <code>RmapInterface</code> to the name of the
     * input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>Rmap</code>.
     * @return the results of the comparison:
     *	       <br><ul>
     *	       <li><0 if this <code>RmapInterface</code> compares less than the
     *		   input,</li>
     *	       <li> 0 if this <code>RmapInterface</code> compares equal to the
     *		   input, and,</li>
     *	       <li>0 if this <code>RmapInterface</code> compares greater than
     *		   the input.</li>
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
    public abstract int compareNames(Rmap otherI);

    /**
     * Compares the name of this <code>RmapInterface</code> to the input name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name to compare to.
     * @return the results of the comparison:
     *	       <br><ul>
     *	       <li><0 if this <code>RmapInterface</code> compares less than the
     *		   input,</li>
     *	       <li> 0 if this <code>RmapInterface</code> compares equal to the
     *		   input, and,</li>
     *	       <li>0 if this <code>RmapInterface</code> compares greater than
     *		   the input.</li>
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
    public abstract int compareNames(String nameI);

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
    public abstract int compareTo(Rmap rmapI);

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
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/02/2001  INB	Created.
     *
     */
    public abstract Rmap findChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    public abstract Rmap findDescendant(String nameI,boolean addI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

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
     * @version 12/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2001  INB	Created.
     *
     */
    public abstract String getFullName()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the name of this <code>RmapInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setName(String)
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
    public abstract String getName();

    /**
     * Gets the parent <code>Rmap</code> of this <code>RmapInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the parent <code>Rmap</code>.
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
    public abstract Rmap getParent();

    /**
     * Removes a child <code>Rmap</code> from this <code>RmapInterface</code>.
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
     * @see #removeChildAt(int)
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    public abstract void removeChild(Rmap childI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Removes the child <code>Rmap</code> at a particular index from this
     * <code>RmapInterface</code>.
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
     * @see #removeChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 01/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    public abstract void removeChildAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Sets the name of this <code>RmapInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name of the <code>RmapInterface</code>.
     * @see #getName()
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
    public abstract void setName(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets a displayable string representation of this
     * <code>RmapInterface</code>.
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
    public abstract String toString();

    /**
     * Gets a displayable string representation of this
     * <code>RmapInterface</code>.
     * <p>
     * This method is the workhorse that actually does the work under the
     * the standard <code>toString()</code> method. It builds a representation
     * that shows the entire structure of the <code>RmapInterface</code>,
     * including the group members and the children.
     * <p>
     * The input parameters specify whether this <code>RmapInterface</code> is
     * a group member of its parent (or a child) and the identation string to
     * prepend to put this <code>RmapInterface</code> under its parent.
     * <p>
     *
     * @author Ian Brown
     *
     * @param memberI is this <code>RmapInterface</code> a group member?
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
    public abstract String toString(boolean memberI,String indentI)
	throws java.lang.InterruptedException;
}
