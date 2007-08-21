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
 * Object that represents a shortcut connection from one server to another.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/15/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/03/2002  INB	Created.
 *
 */
class ShortcutIO
    extends com.rbnb.api.Rmap
    implements com.rbnb.api.Shortcut
{
    /**
     * the active state value.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ACTIVE
     * @see #PASSIVE
     * @since V2.0
     * @version 01/10/2002
     */
    private byte active = ACTIVE;

    /**
     * the cost of using this <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2002
     */
    private double cost = 1.;

    /**
     * the destination address.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2002
     */
    private String destinationAddress = null;

    /**
     * the destination name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2002
     */
    private String destinationName = null;

    // Private constants:
    private final static byte PAR_ACT = 0;
    private final static byte PAR_CST = 1;
    private final static byte PAR_DAD = 2;
    private final static byte PAR_DAN = 3;

    private final static String[] PARAMETERS = {
	"ACT",
	"CST",
	"DAD",
	"DAN"
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
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    ShortcutIO() {
	super();
    }

    /**
     * Class constructor to build a <code>ShortcutIO</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name.
     * @since V2.0
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
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    ShortcutIO(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
    }

    /**
     * Class constructor to build a <code>ShortcutIO</code> by reading it in.
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
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    ShortcutIO(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this(null,isI,disI);
    }

    /**
     * Class constructor to build a <code>ShortcutIO</code> by reading it in.
     * <p>
     * This method copies unread fields from the input <code>Rmap</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>ShortcutIO</code> as an <code>Rmap</code>.
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
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    ShortcutIO(Rmap otherI,InputStream isI,DataInputStream disI)
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
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     * This method calls the static <code>ShortcutIO</code> method.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the additional information.
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    String additionalToString() {
	return (additionalToString(this));
    }

    /**
     * Adds additional, class-specific information to the <code>toString</code>
     * method's return value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutI the <code>ShortcutInterface</code>.
     * @return the additional information.
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    static String additionalToString(ShortcutInterface shortcutI) {
	String stringR = "->";

	if (shortcutI.getDestinationName() != null) {
	    stringR += " " + shortcutI.getDestinationName();
	}
	if (shortcutI.getDestinationAddress() != null) {
	    stringR += " (@" + shortcutI.getDestinationAddress() + ")";
	}
	stringR += " $" + shortcutI.getCost();
	stringR += (shortcutI.getActive() == ACTIVE) ? " active" : " passive";

	return (stringR);
    }

    /**
     * Adds the <code>ShortcutIO's</code> parameters to the full serialization
     * parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI the serialization parameters list so far.
     * @return the updated list of serialization parameters.
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    static synchronized String[] addToParameters
	(String[] parametersI)
    {
	String[] parametersR = Rmap.addToParameters(null);
	if (parametersR != null) {
	    parametersStart = parametersR.length;
	}
	return (addToParameters(parametersR,PARAMETERS));
    }

    /**
     * Compares the sorting value of this <code>Shortcut</code> to the input
     * sorting value according to the type sort specified by the sort
     * identifier.
     * <p>
     * If the identifier is null, then this method calls the super
     * class. Otherwise, the identifier determines which string to use to
     * compare.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI    the sort type identifier.
     * @param otherI  the other sorting value.
     * @return the results of the comparison:
     *	       <p><0 if this <code>Shortcut</code> compares less than the
     *		  input,
     *	       <p> 0 if this <code>Shortcut</code> compares equal to the input,
     *		   and 
     *	       <p>>0 if this <code>Shortcut</code> compares greater than the
     *		  input.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is not legal.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final int compareTo(Object sidI,Object otherI)
	throws com.rbnb.utility.SortException
    {
	int comparedR = 0;

	if (sidI == null) {
	    comparedR = super.compareTo(sidI,otherI);

	} else {
	    String value = (String) sortField(sidI);

	    comparedR = value.compareTo((String) otherI);
	}

	return (comparedR);
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
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    void defaultParameters(Rmap otherI,boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	defaultShortcutParameters(otherI,seenI);
	super.defaultParameters(otherI,seenI);
    }

    /**
     * Defaults for <code>Shortcut</code> parameters.
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
     * @version 01/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    final void defaultShortcutParameters(Rmap otherI,
					 boolean[] seenI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (otherI != null) {
	    Shortcut other = (Shortcut) otherI;

	    if ((seenI == null) || !seenI[parametersStart + PAR_ACT]) {
		setActive(other.getActive());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_CST]) {
		setCost(other.getCost());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_DAD]) {
		setDestinationAddress(other.getDestinationAddress());
	    }

	    if ((seenI == null) || !seenI[parametersStart + PAR_DAN]) {
		setDestinationName(other.getDestinationName());
	    }
	}
    }

    /**
     * Gets the active state.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the value.
     * @see #ACTIVE
     * @see #PASSIVE
     * @see #setActive(byte)
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    public final byte getActive() {
	return (active);
    }

    /**
     * Gets the cost of using this <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the cost.
     * @see #setCost(double)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public final double getCost() {
	return (cost);
    }

    /**
     * Gets the address of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the address.
     * @see #setDestinationAddress(String)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public final String getDestinationAddress() {
	return (destinationAddress);
    }

    /**
     * Gets the fully-qualified name of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setDestinationName(String)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public final String getDestinationName() {
	return (destinationName);
    }

    /**
     * Initializes the full serialization parameters list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    private final static synchronized void initializeParameters() {
	if (ALL_PARAMETERS == null) {
	    // If the parameters haven't been initialized, do so now.
	    ALL_PARAMETERS = addToParameters(null);
	}
    }

    /**
     * Creates a new instance of the same class as this <code>Shortcut</code>
     * (or a similar class).
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
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Shortcut scR = new ShortcutIO();

	scR.setName(getName());
	scR.setCost(getCost());
	scR.setDestinationAddress(getDestinationAddress());
	scR.setDestinationName(getDestinationName());
	scR.setActive(getActive());

	return ((Rmap) scR);
    }

    /**
     * Reads the <code>Shortcut</code> from the specified input stream.
     * <p>
     * This method uses the input <code>Rmap</code> to provide default values.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Shortcut</code> as an <code>Rmap</code>.
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
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    void read(Rmap otherI,InputStream isI,DataInputStream disI)
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
	    if (!readShortcutParameter(parameter,isI,disI)) {
		readStandardParameter(otherI,parameter,isI,disI);
	    }
	}

	defaultParameters(otherI,seen);
    }

    /**
     * Reads <code>Shortcut</code> parameters.
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
     * @see #writeShortcutParameters(com.rbnb.api.Shortcut,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    final boolean readShortcutParameter(int parameterI,
				      InputStream isI,
				      DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	boolean shortcutR = false;

	if (parameterI >= parametersStart) {
	    shortcutR = true;

	    switch (parameterI - parametersStart) {
	    case PAR_ACT:
		setActive(isI.readByte());
		break;

	    case PAR_CST:
		setCost(isI.readDouble());
		break;

	    case PAR_DAD:
		setDestinationAddress(isI.readUTF());
		break;

	    case PAR_DAN:
		setDestinationName(isI.readUTF());
		break;

	    default:
		shortcutR = false;
		break;
	    }
	}

	return (shortcutR);
    }

    /**
     * Sets the active state.
     * <p>
     *
     * @author Ian Brown
     *
     * @param activeI the new active state.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the <code>activeI</code> state value is not
     *		  <code>ACTIVE</code> or <code>PASSIVE</code>.
     * @see #ACTIVE
     * @see #PASSIVE
     * @see #getActive()
     * @since V2.0
     * @version 01/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2002  INB	Created.
     *
     */
    public final void setActive(byte activeI) {
	if ((activeI != ACTIVE) && (activeI != PASSIVE)) {
	    throw new java.lang.IllegalArgumentException
		(activeI + " is not a valid shortcut state.");
	}
	active = activeI;
    }

    /**
     * Sets the cost of using this <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param costI the cost.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the cost is less than 1.
     * @see #getCost()
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public final void setCost(double costI) {
	if (costI < 1.) {
	    throw new java.lang.IllegalArgumentException
		("Cost cannot be less than 1.");
	}

	cost = costI;
    }

    /**
     * Sets the address of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param destinationAddressI the destination address.
     * @see #getDestinationAddress()
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public final void setDestinationAddress(String destinationAddressI) {
	destinationAddress = destinationAddressI;
    }

    /**
     * Sets the name of the destination server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param destinationNameI the destination name.
     * @see #getDestinationName()
     * @since V2.0
     * @version 01/03/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    public final void setDestinationName(String destinationNameI) {
	destinationName = destinationNameI;
    }

    /**
     * Gets the sorting value for this <code>Shortcut</code>.
     * <p>
     * If the sort identified is null, then the super class is
     * called. Otherwise, the method returns the destination name of this
     * <code>Shortcut</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sidI  the sort type identifier.
     * @return the sort value.
     * @exception com.rbnb.utility.SortException
     *		  thrown if the input sort identifier is not legal.
     * @see #compareTo(Object,Object)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2002  INB	Created.
     *
     */
    public final Object sortField(Object sidI)
	throws com.rbnb.utility.SortException
    {
	Object valueR = null;

	if (sidI == null) {
	    valueR = super.sortField(sidI);
	} else {
	    valueR = getDestinationName();
	}

	return (valueR);
    }

    /**
     * Writes this <code>Shortcut</code> to the specified stream.
     * <p>
     * This method writes out the differences between this
     * <code>Shortcut</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI	   the other <code>Shortcut</code> as an
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
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
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
	// Write out the object.
	long before = osI.getWritten();
	int valid = osI.setStage(true,false);
	osI.addStaged(this,parametersI,parameterI);

	writeStandardParameters(otherI,osI,dosI);
	writeShortcutParameters((Shortcut) otherI,osI,dosI);

	if ((otherI == null) || (osI.getWritten() > before)) {
	    Serialize.writeCloseBracket(osI);
	} else if (valid >= 0) {
	    osI.removeStaged(valid);
	}
    }

    /** 
     * Writes out <code>Shortcut</code> parameters.
     * <p>
     * This method writes out the differences between this
     * <code>Shortcut</code> and the input one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherI the other <code>Shortcut</code>.
     * @param osI    the output stream.
     * @param dosI   the data output stream.
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
     * @see #readShortcutParameter(int,com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2002  INB	Created.
     *
     */
    final void writeShortcutParameters(Shortcut otherI,
				       OutputStream osI,
				       DataOutputStream dosI)
	throws com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	osI.setStage(false,false);

	// Initialize the full parameter list.
	initializeParameters();

	
	if ((otherI == null) || (getActive() != otherI.getActive())) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_ACT);
	    osI.writeByte(getActive());
	}
	
	if ((otherI == null) || (getCost() != otherI.getCost())) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_CST);
	    osI.writeDouble(getCost());
	}

	if ((getDestinationAddress() != null) &&
	    ((otherI == null) ||
	     (otherI.getDestinationAddress() == null) ||
	     !getDestinationAddress().equals
	     (otherI.getDestinationAddress()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_DAD);
	    osI.writeUTF(getDestinationAddress());
	}

	if ((getDestinationName() != null) &&
	    ((otherI == null) ||
	     (otherI.getDestinationName() == null) ||
	     !getDestinationName().equals
	     (otherI.getDestinationName()))) {
	    osI.writeParameter(ALL_PARAMETERS,parametersStart + PAR_DAN);
	    osI.writeUTF(getDestinationName());
	}
    }
}
