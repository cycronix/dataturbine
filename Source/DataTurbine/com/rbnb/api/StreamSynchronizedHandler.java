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
 * Streaming synchronized handler.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 07/31/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/23/2002  INB	Created.
 *
 */
final class StreamSynchronizedHandler
    extends com.rbnb.api.StreamListener
{

    /**
     * Class constructor to build a <code>StreamSynchronizedHandler</code> for
     * the specified <code>NBO</code> to work on the specified request
     * <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nboI	the <code>NBO</code>.
     * @param requestI  the request <code>DataRequest</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    StreamSynchronizedHandler(NBO nboI,DataRequest requestI)
	throws java.lang.InterruptedException
    {
	super();
	setParent(nboI);
	setRequest(requestI);
    }

    /**
     * Posts a response <code>Serializable</code> to the application.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rmapI  the <code>Serializable</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	((NBO) getParent()).asynchronousResponse(serializableI);
    }

    /**
     * Runs this <code>StreamSynchronizedHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #stop()
     * @since V2.0
     * @version 07/31/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    public final void run() {
	try {
	    // Initiate the request.
	    long count = 1;

	    while ((count != 0) && !getTerminateRequested()) {
		// Loop until we reach an end of stream condition or we are
		// terminated on by a request from the <code>NBO</code>.
		synchronized (this) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}

		if (!isAlive(false)) {
		    if (getEOS()) {
			break;
		    }
		}
	    }

	} catch (java.lang.InterruptedException e) {

	} catch (java.lang.Exception e) {
	    setEOS(true);
	    try {
		((NBO) getParent()).asynchronousException(e);
	    } catch (java.lang.Exception e1) {
	    }
	}

	// Notify any waiting thread.
	setTerminateRequested(false);

	// Kick our parent awake.
	if (getParent() != null) {
	    synchronized (getParent()) {
		getParent().notifyAll();
	    }
	}

	setThread(null);
    }

    /**
     * Starts this <code>StreamSynchronizedHandler</code> running.
     * <p>
     * 
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #run()
     * @see #stop()
     * @since V2.0
     * @version 01/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2002  INB	Created.
     *
     */
    final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
    }
}
