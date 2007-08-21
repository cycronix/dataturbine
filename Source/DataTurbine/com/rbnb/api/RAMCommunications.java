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

package com.rbnb.api;/**
 * RAM communications object.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/15/2004
 */

/*
 * Copyright 2001, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/15/2004  INB	Don't lock up if the other side is <code>null</code> on
 *			a read or write.
 * 02/19/2003  INB	Modified to ensure that we can shutdown when performing
 *			a read.
 * 05/11/2001  INB	Created.
 *
 */
final class RAMCommunications {

    /**
     * abort any active reads?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/19/2003
     */
    private boolean abort = false;

    /**
     * the <code>ACO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #rco
     * @since V2.0
     * @version 05/14/2001
     */
    private ACO aco = null;

    /**
     * the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #aco
     * @since V2.0
     * @version 05/14/2001
     */
    private RCO rco = null;

    /**
     * the other side of the communications channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private RAMCommunications otherSide = null;

    /**
     * list of objects waiting to be read.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private java.util.Vector waiting = new java.util.Vector();

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
    RAMCommunications() {
	super();
    }

    /**
     * Class constructor to build a <code>RAMCommunications</code> object for
     * the specified <code>ACO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acoI the <code>ACO</code>.
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    RAMCommunications(ACO acoI) {
	this();
	setACO(acoI);
    }

    /**
     * Aborts an active read.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #read(long)
     * @since V2.1
     * @version 02/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/19/2003  INB	Created.
     *
     */
    final void abortRead() {
	abort = true;
	synchronized (waiting) {
	    waiting.notifyAll();
	}
    }

    /**
     * Disconnects from the RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.RAMServerCommunications#connect(java.lang.Object)
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    final void disconnect() {
	if (getACO() != null) {
	    getOtherSide().setOtherSide(null);
	}
	setOtherSide(null);
    }

    /**
     * Gets the <code>ACO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ACO</code>.
     * @see #setACO(com.rbnb.api.ACO)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final ACO getACO() {
	return (aco);
    }

    /**
     * Gets the other side of the connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RAMCommunications</code> object on the other side.
     * @see #setOtherSide(com.rbnb.api.RAMCommunications)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    final RAMCommunications getOtherSide() {
	return (otherSide);
    }

    /**
     * Gets the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RCO</code>.
     * @see #setACO(com.rbnb.api.ACO)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final RCO getRCO() {
	return (rco);
    }

    /**
     * Is anything waiting to be read?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is anything waiting to be read?
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final boolean isWaiting()
	throws java.lang.InterruptedException
    {
	return (waiting.size() > 0);
    }

    /**
     * Reads an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI  timeout in milliseconds.
     *			<br><ul>
     *			<li>0 means return a response only if one is
     *			    ready,</li>
     *			<li><code>Client.FOREVER</code> means wait for a
     *			    response to show up, or</li>
     *			<li>anything else means wait for a response to show up
     *			    or for the timeout period to elapse.</li>
     *			</ul>
     * @return the object read.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 04/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2004  INB	If the other side is gone and there is nothing to be
     *			read, then simply return immediately.
     * 02/19/2003  INB	Modified to allow this method to be aborted.
     * 05/11/2001  INB	Created.
     *
     */
    final Serializable read(long timeOutI)
	throws java.lang.InterruptedException
    {
	Serializable serializableR = null;

	if (!abort) {
	    synchronized (waiting) {
		if ((waiting.size() == 0) && (getOtherSide() != null)) {
		    if (timeOutI == Client.FOREVER) {
			waiting.wait();
		    } else if (timeOutI > 0) {
			waiting.wait(timeOutI);
		    }
		}

		if (waiting.size() > 0) {
		    serializableR = (Serializable) waiting.firstElement();
		    waiting.removeElementAt(0);
		}
	    }
	}

	return (serializableR);
    }

    /**
     * Sets the <code>ACO</code>.
     * <p>
     * A <code>RAMCommunications</code> object performs on behalf of an
     * <code>ACO</code> or an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acoI the <code>ACO</code>.
     * @see #getACO()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final void setACO(ACO acoI) {
	aco = acoI;
    }

    /**
     * Sets the other side of the connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param otherSideI the <code>RAMCommunications</code> object on the other
     *			 side.
     * @see #getOtherSide()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    final synchronized void setOtherSide(RAMCommunications otherSideI) {
	otherSide = otherSideI;
	notifyAll();
    }

    /**
     * Sets the <code>RCO</code>.
     * <p>
     * A <code>RAMCommunications</code> object performs on behalf of an
     * <code>ACO</code> or an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI the <code>RCO</code>.
     * @see #getRCO()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final void setRCO(RCO rcoI) {
	rco = rcoI;
    }

    /**
     * Writes an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> object.
     * @exception java.io.EOFException
     *		  thrown if the other side is <code>null</code>.
     * @see #read(long)
     * @since V2.0
     * @version 04/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2004  INB	If the other side is gone, then throw an exception.
     * 05/11/2001  INB	Created.
     *
     */
    final void write(Serializable serializableI)
	throws java.io.EOFException
    {
	if (getOtherSide() == null) {
	    throw new java.io.EOFException
		("The other side of the connection has been shut down.");
	}
	synchronized (waiting) {
	    waiting.addElement(serializableI.clone());
	    waiting.notifyAll();
	}
    }
}
