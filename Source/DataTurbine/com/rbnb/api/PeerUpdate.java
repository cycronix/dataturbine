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
 * A <code>PeerServer</code> update message.
 * <p>
 * This class describes an update from a <code>PeerServer</code>. These updates
 * are used to keep track of who each of a <bold>RBNB</bold> server's peers is
 * connected to.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 08/30/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/20/2001  INB	Created.
 *
 */
final class PeerUpdate
    extends com.rbnb.api.Serializable
{
    /**
     * the address of the <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private String peerAddress = null;

    /**
     * the fully-qualified name of the <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private String peerName = null;

    /**
     * peer update counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */
    private long peerUpdateCounter = 0;

    /**
     * the list of <code>Shortcuts</code> from the <code>PeerServer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2002
     */
    private com.rbnb.utility.SortedVector shortcuts = null;

    // Private constants:
    private final static byte PAR_ADR = 0;
    private final static byte PAR_NAM = 1;
    private final static byte PAR_UPC = 2;
    private final static byte PAR_SHC = 3;
    private final static byte SHC_SHC = 0;

    private final static String[] PARAMETERS = {
				    "ADR",
				    "NAM",
				    "UPC",
				    "SHC"
				};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    PeerUpdate() {
	super();
    }

    /**
     * Class constructor to build a <code>PeerUpdate</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the control input stream.
     * @param disI the data input stream.
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    PeerUpdate(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>PeerUpdate</code> object from a
     * <code>PeerServer</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerI the <code>PeerServer</code>.
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
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    PeerUpdate(PeerServer peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	setPeerName(peerI.getFullName());
	setPeerAddress(peerI.getAddress());
	setPeerUpdateCounter(peerI.getUpdateCounter());
	setShortcuts
	    ((com.rbnb.utility.SortedVector) peerI.getShortcuts().clone());
    }

    /**
     * Gets the address of the peer.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the peer's address.
     * @see #setPeerAddress(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    final String getPeerAddress() {
	return (peerAddress);
    }

    /**
     * Gets the name of the peer.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the peer's name.
     * @see #setPeerName(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    final String getPeerName() {
	return (peerName);
    }

    /**
     * Gets the peer's update counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the peer's update counter.
     * @see #setPeerUpdateCounter(long)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    final long getPeerUpdateCounter() {
	return (peerUpdateCounter);
    }

    /**
     * Gets the list of <code>Shortcuts</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of <code>Shortcuts</code>.
     * @see #setShortcuts(com.rbnb.utility.SortedVector)
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
    final com.rbnb.utility.SortedVector getShortcuts() {
	return (shortcuts);
    }

    /**
     * Reads the <code>PeerUpdate</code> from the specified input stream.
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
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the command of the
	// <code>Command</code>. 
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_ADR:
		setPeerAddress(isI.readUTF());
		break;

	    case PAR_NAM:
		setPeerName(isI.readUTF());
		break;

	    case PAR_UPC:
		setPeerUpdateCounter(isI.readLong());
		break;

	    case PAR_SHC:
		if (getShortcuts() == null) {
		    setShortcuts(new com.rbnb.utility.SortedVector("SCN"));
		}
		Shortcut shortcut = new ShortcutIO(isI,disI);
		getShortcuts().addElement(shortcut);
		break;
	    }
	}
    }

    /**
     * Sets the address of the peer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerAddressI the address of the peer.
     * @see #getPeerAddress()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    private final void setPeerAddress(String peerAddressI) {
	peerAddress = peerAddressI;
    }

    /**
     * Sets the name of the peer.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerNameI the name of the peer.
     * @see #getPeerName()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    private final void setPeerName(String peerNameI) {
	peerName = peerNameI;
    }

    /**
     * Sets the peer's update counter.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateCounterI the peer's update counter.
     * @see #getPeerUpdateCounter()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    private final void setPeerUpdateCounter(long peerUpdateCounterI) {
	peerUpdateCounter = peerUpdateCounterI;
    }

    /**
     * Sets the list of <code>Shortcuts</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param shortcutsI the list.
     * @see #getShortcuts()
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
    final void setShortcuts(com.rbnb.utility.SortedVector shortcutsI) {
	shortcuts = shortcutsI;
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    public final String toString() {
	String stringR =
	    "Update: " + getPeerName() +
	    " @ " + getPeerAddress() +
	    " #" + getPeerUpdateCounter() +
	    ":";

	if ((getShortcuts() == null) || (getShortcuts().size() == 0)) {
	    stringR += " no shortcuts.";
	} else {
	    stringR += "\n" + getShortcuts();
	}

	return (stringR);
    }

    /**
     * Writes this <code>PeerUpdate</code> to the specified stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  our parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing to the stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.0
     * @version 01/08/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
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

	osI.writeParameter(PARAMETERS,PAR_ADR);
	osI.writeUTF(getPeerAddress());

	if (getShortcuts() != null) {
	    for (int idx = 0; idx < getShortcuts().size(); ++idx) {
		Shortcut shortcut = (Shortcut) getShortcuts().elementAt(idx);
		((ShortcutIO) shortcut).write
		    ((Rmap) null,
		     PARAMETERS,
		     PAR_SHC,
		     osI,
		     dosI);
	    }
	}

	osI.writeParameter(PARAMETERS,PAR_NAM);
	osI.writeUTF(getPeerName());

	osI.writeParameter(PARAMETERS,PAR_UPC);
	osI.writeLong(getPeerUpdateCounter());

	Serialize.writeCloseBracket(osI);
    }
}
