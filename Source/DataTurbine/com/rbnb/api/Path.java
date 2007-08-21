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
 * A directed series of <bold>RBNB</bold> servers.
 * <p>
 * A <code>Path</code> represents a connected sequence of RBNB DataTurbine
 * servers that can be used to deliver a <code>RoutedMessage</code> from its
 * source on the server at the start of the <code>Path</code> to its target at
 * the end of the <code>Path</code>.
 * <p>
 * 
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RoutedMessage
 * @see com.rbnb.api.RemoteServer
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/05/2004  INB	Updated the documentation.
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/26/2003  INB	Added <code>BY_COST</code> sorting index. Use
 *			<code>Hashtable</code> to provide a sorted search for
 *			server names.
 * 04/25/2001  INB	Created.
 *
 */
class Path
    extends com.rbnb.api.Serializable
    implements com.rbnb.utility.SortCompareInterface
{
    /**
     * sort by cost and then by name.
     * <p>
     * As opposed to sorting by name and then cost.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    final static String BY_COST = "COST";

    /**
     * the ordered list of <bold>RBNB</bold> server names.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private java.util.Vector ordered = new java.util.Vector();

    /**
     * sorted list of the server names.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    private java.util.Hashtable sortedNames = new java.util.Hashtable();

    // Private constants:
    private final static int PAR_SRV = 0;

    private final static String[] PARAMETERS = {
			    "SRV"
			};

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
     * 04/27/2001  INB	Created.
     *
     */
    public Path() {
	super();
    }

    /**
     * Class constructor to read a <code>Path</code>.
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
     * 04/27/2001  INB	Created.
     *
     */
    Path(InputStream isI,DataInputStream disI)
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
     * Adds an <code>Rmap</code> hierarchy that corresponds to the hierarchy
     * ending with the input <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>Server</code>.
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
     * @see #getOrdered()
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 04/27/2001  INB	Created.
     *
     */
    public final void add(Server serverI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	add(serverI.getFullName());
    }

    /**
     * Adds a server name to the ordered list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the server name.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Add the names to <code>sortedNames</code>.
     * 11/28/2001  INB	Created.
     *
     */
    final void add(String nameI) {
	sortedNames.put(nameI,new Integer(getOrdered().size()));
	getOrdered().addElement(nameI);
    }

    /**
     * Compares this <code>Path</code> to the input one.
     * <p>
     * The elements of the <code>Path</code> are compared in reverse
     * order. This ensures that the sort is primarily by destination.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>Path</code>.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Path</code> compares less than the input,
     *	       <p> 0 if this <code>Path</code> compares equal to the input,
     *		   and
     *	       <p>>0 if this <code>Path</code> compares greater than the
     *		   input.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Call version with <code>costI</code> parameter.
     * 05/04/2001  INB	Created.
     *
     */
    public int compareTo(Path otherI) {
	return (compareTo(otherI,false));
    }

    /**
     * Compares this <code>Path</code> to the input one.
     * <p>
     * The elements of the <code>Path</code> are compared in reverse
     * order. This ensures that the sort is primarily by destination.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI  the other <code>Path</code>.
     * @param costI   is cost the primary sort?
     * @return the results of the comparison:
     *	       <p><0 if this <code>Path</code> compares less than the input,
     *	       <p> 0 if this <code>Path</code> compares equal to the input,
     *		   and
     *	       <p>>0 if this <code>Path</code> compares greater than the
     *		   input.
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Created.
     *
     */
    public int compareTo(Path otherI,boolean costI) {
	int idx,
	    idx1,
	    comparedR = 0;
	String mine,
	    theirs;

	for (idx = getOrdered().size() - 1,
		 idx1 = otherI.getOrdered().size() - 1;
	     (comparedR == 0) && (idx >= 0) && (idx1 >= 0);
	     --idx,
		 --idx1) {
	    mine = (String) getOrdered().elementAt(idx);
	    theirs = (String) otherI.getOrdered().elementAt(idx1);
	    comparedR = mine.compareTo(theirs);
	}

	if (comparedR == 0) {
	    if (idx >= 0) {
		comparedR = 1;
	    } else if (idx1 >= 0) {
		comparedR = -1;
	    }
	}

	return (comparedR);
    }

    /**
     * Compares the sorting value of this <code>Path</code> to the input
     * sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier. Legal values are:
     *		      <p><ul>
     *			 <li><code>null</code> - sort by name and then cost, or
     *			     </li>
     *			 <li><code>BY_COST</code> - sort by cost and then
     *			     name.</li>
     *		      </ul>
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Path</code> compares less than the input,
     *	       <p> 0 if this <code>Path</code> compares equal to the input,
     *		   and
     *	       <p>>0 if this <code>Path</code> compares greater than the
     *		   input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is not legal.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Added <code>BY_COST</code> capability.
     * 05/04/2001  INB	Created.
     *
     */
    public int compareTo
	(Object sidI,
	 Object otherI)
	throws com.rbnb.utility.SortException
    {
	int comparedR;

	if (otherI instanceof String) {
	    if ((sidI instanceof String) && ((String) sidI).equals(BY_COST)) {
		throw new com.rbnb.utility.SortException
		    ("Cannot search for a destination name in a list sorted " +
		     "by cost.");
	    }
	    comparedR = ((String)
			 getOrdered().lastElement()).compareTo
		((String) otherI);

	} else {
	    Path theirs = (Path) otherI;

	    comparedR = compareTo(theirs,
				  ((sidI instanceof String) &&
				   ((String) sidI).equals(BY_COST)));
	}

	return (comparedR);
    }

    /**
     * Looks for a <code>Server</code> in the <code>Path</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI the <code>Server</code> to find.
     * @return the matching index or -1.
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
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/26/2003  INB	Use <code>sortedNames</code> to do the search.
     * 04/27/2001  INB	Created.
     *
     */
    public final int find(Server serverI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	String theirs = ((Rmap) serverI).getFullName();
	Integer index = (Integer) sortedNames.get(theirs);
	int idxR = (index == null) ? -1 : index.intValue();

	return (idxR);
    }

    /**
     * Gets the ordered list of <bold>RBNB</bold> server names.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the ordered list of <bold>RBNB</bold> server names.
     * @see #add(com.rbnb.api.Server)
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 04/27/2001  INB	Created.
     *
     */
    public final java.util.Vector getOrdered() {
	return (ordered);
    }

    /**
     * Reads the <code>Path</code> from the specified input stream.
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
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the <code>Path</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_SRV:
		for (int nServers = isI.readInt();
		     nServers > 0;
		     --nServers) {
		    add(isI.readUTF());
		}
	    }
	}
    }

    /**
     * Sets the ordered list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param orderedI the new ordered list of server names.
     * @see #getOrdered()
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Update <code>sortedNames</code>.
     * 12/21/2001  INB	Created.
     *
     */
    final void setOrdered(java.util.Vector orderedI) {
	ordered = orderedI;
	sortedNames.clear();
	if (ordered != null) {
	    for (int idx = 0; idx < ordered.size(); ++idx) {
		sortedNames.put(ordered.elementAt(idx),
				new Integer(idx));
	    }
	}
    }

    /**
     * Gets the sorting value for this <code>Path</code>.
     * <p>
     * The sort identifier for <code>Paths</code> is the <code>Path</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI the sort type identifier. Legal values are:
     *		   <p><ul>
     *		      <li><code>null</code> - sort by name and then cost, or
     *			  </li>
     *		      <li><code>BY_COST</code> - sort by cost and then
     *			  name.</li>
     *		   </ul>
     * @return the sort value.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is not legal.
     * @see #compareTo(Object,Object)
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Added <code>BY_COST</code> capability.
     * 05/04/2001  INB	Created.
     *
     */
    public Object sortField(Object sidI)
	throws com.rbnb.utility.SortException
    {
	if ((sidI != null) &&
	    (!(sidI instanceof String) ||
	     !((String) sidI).equals(BY_COST))) {
	    throw new com.rbnb.utility.SortException
		("The sort identifier for Paths must be null or " +
		 BY_COST + ".");
	}

	return (this);
    }

    /**
     * Writes this <code>Path</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 11/28/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2001  INB	Created.
     *
     */
    final void write(String[] parametersI,
		     int parameterI,
		     OutputStream osI,
		     DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.writeParameter(parametersI,parameterI);
	Serialize.writeOpenBracket(osI);

	if (getOrdered().size() > 0) {
	    osI.writeParameter(PARAMETERS,PAR_SRV);
	    osI.writeInt(getOrdered().size());
	    for (int idx = 0; idx < getOrdered().size(); ++idx) {
		osI.writeUTF((String) getOrdered().elementAt(idx));
	    }
	}

	Serialize.writeCloseBracket(osI);
    }

    /**
     * Gets a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/27/2001  INB	Created.
     *
     */
    public String toString() {
	String stringR = "Path: " + getOrdered().toString();

	return (stringR);
    }
}
