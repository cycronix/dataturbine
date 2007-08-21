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
 * Extended <code>ACO</code> that communicates via a memory.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/15/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 04/15/2004  INB	The <code>send</code> method can now throw exceptions.
 * 05/23/2003  INB	Added timeout to <code>send</code>.
 * 05/09/2001  INB	Created.
 *
 */
final class RAMACO
    extends com.rbnb.api.ACO
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    RAMACO() {
	super();
    }

    /**
     * Class constructor to build a <code>RAMACO</code> for the specified
     * <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI the <code>Client</code..
     * @since V2.0
     * @version 05/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2001  INB	Created.
     *
     */
    RAMACO(Client clientI) {
	super(clientI);
    }

    /**
     * Connects the data communications channel.
     * <p>
     * There is no separate data communications channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is problem with I/O.
     * @see #disconnectData()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final void connectData()
	throws java.io.InterruptedIOException,
	       java.io.IOException
    {
    }

    /**
     * Converts this <code>ACO</code> to an <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rbnbI the <code>ServerHandler</code>.
     * @return the <code>RCO</code>.
     * @since V2.0
     * @version 02/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  INB	Created.
     *
     */
    final RCO convertToRCO(ServerHandler rbnbI) {
	RAMCommunications rc = (RAMCommunications) getClientSide();

	RAMRCO rcoR = new RAMRCO(rc.getOtherSide(),rbnbI);

	if (getClient() instanceof BuildInterface) {
	    BuildInterface bi = (BuildInterface) getClient();
	    rcoR.setBuildDate(bi.getBuildDate());
	    rcoR.setBuildVersion(bi.getBuildVersion());
	    //	rcoR.setLicenseString(bi.getLicenseString());
	} else {
	    BuildFile.loadBuildFile(rcoR);
	}

	return (rcoR);
    }

    /**
     * Disonnects the data communications channel.
     * <p>
     * There is no separate data communications channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is problem with I/O.
     * @see #disconnectData()
     * @since V2.0
     * @version 05/14/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/14/2001  INB	Created.
     *
     */
    final void disconnectData()
	throws java.io.InterruptedIOException,
	       java.io.IOException
    {
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
	RAMCommunications rc = (RAMCommunications) getClientSide();
	return (rc.isWaiting());
    }

    /**
     * Receives a message from the <code>RCO</code> in the
     * <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>= 0 means read anything that is already waiting,</li>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li><code>Client.FOREVER</code> means wait until a message shows
     *		  up or the connection is terminated.</li>
     *	      </ul>
     * @return the <code>Serializable</code> message received or null on a
     *	       timeout.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #send(com.rbnb.api.Serializable)
     * @see #send(com.rbnb.api.Serializable,long)
     * @since V2.0
     * @version 05/23/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/08/2001  INB	Created.
     *
     */
    final Serializable receive(long timeOutI)
	throws java.lang.InterruptedException
    {
	RAMCommunications rc = (RAMCommunications) getClientSide();
	Serializable serializableR = rc.read(timeOutI);
	return (serializableR);
    }

    /**
     * Sends a message to the <code>RCO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI the <code>Serializable</code> message.
     * @param timeOutI the timeout period:
     *	      <br><ul>
     *	      <li>>>0 means to wait up to the specified number of milliseconds
     *		  for a message to show up,</li>
     *	      <li>0 or <code>Client.FOREVER</code> means wait until a message
     *		  shows up or the connection is terminated.</li>
     *	      </ul>
     * @exception java.io.IOException
     *		  thrown if there is a problem sending the message.     
     * @see #receive(long)
     * @see #receive(java.lang.Class,boolean,long)
     * @since V2.0
     * @version 04/15/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2004  INB	Allow for <code>IOExceptions</code>.
     * 05/23/2003  INB	Added timeout to method. Not used by this
     *			implementation.
     * 05/08/2001  INB	Created.
     *
     */
    final void send(Serializable serializableI,long timeOutI)
	throws java.io.IOException
    {
	RAMCommunications rc = (RAMCommunications) getClientSide();
	rc.getOtherSide().write(serializableI);
    }
}
