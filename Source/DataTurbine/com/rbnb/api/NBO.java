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
 * RBNB Network Bus Object (NBO) class.
 * <p>
 * This class implements a client handler for sinks.  It is a specialization of
 * the RBNB Ring Buffer Object (RBO) class that stores requests for data rather
 * than actual data.  It extends the RBO capabilities by adding the ability to
 * retrieve the data matching the requests in its ring buffer.  This capability
 * is documented in <code>NBO.initiateRequestAt</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see #initiateRequestAt(int indexI)
 * @since V2.0
 * @version 08/05/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/05/2004  INB	Added documentation at top level.
 * 07/30/2004  INB	Added outline of how requests are processed to
 *			initiateRequestAt.
 * 04/07/2004  INB	The <code>unansweredRSVPs</code> list is a simple
 *			<code>Vector</code>, so the binary search doesn't
 *			make sense.  The list should never be very large,
 *			anyway.
 * 04/06/2004  INB	Ensure that the <code>RSVP</code> list is updated
 *			before sending the response in
 *			<code>asynchronousMessage</code>.
 *			The <code>acknowledge</code> method now longer waits
 *			to match the <code>RSVP</code>.
 * 02/11/2004  INB	Log exceptions at standard level.
 * 11/14/2003  INB	Added identification to the <code>Door</code> and
 *			locations to the <code>Locks</code>.
 * 07/08/2003  INB	Added the saving of <code>RSVP</code> multiple
 *			levels of identification when posting and the restoring
 *			of it when handling <code>Acknowledge</code> messages.
 * 06/11/2003  INB	Added <code>get/setRequestOptions</code>.
 * 05/27/2003  INB	If the <code>RemoteClient</code> retrieved for a
 *			target has no parent, toss it out.
 * 04/08/2003  INB	Added <code>stopOnIOException</code>.
 * 03/21/2003  INB	Hold on to <code>RemoteClients</code> and allow them to
 *			be reused. This initial implementation holds onto one.
 * 03/19/2003  INB	Added in <code>getRegistered(Rmap)</code>.
 * 03/14/2003  INB	Eliminated <code>updateRegistration</code>.
 * 05/10/2001  INB	Created.
 *
 */
final class NBO
    extends com.rbnb.api.RBO
    implements com.rbnb.api.SinkHandler,
	       com.rbnb.api.StreamParent
{
    /**
     * door to control asynchronous posting access.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/23/2001
     */
    private Door asynchronousDoor = null;

    /**
     * check stream?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private boolean checkStream = false;

    /**
     * the identification for the next <code>RSVP</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/08/2003
     */
    private static long nextRSVP = 1;

    /**
     * the number of requests processed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private long numberOfRequests = 0;

    /**
     * the list of <code>RemoteClients</code> to hold onto.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/21/2003
     */
    private java.util.Hashtable remoteClients = new java.util.Hashtable();

    /**
     * the <code>RequestOptions</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/11/2003
     */
    private RequestOptions ro = new RequestOptions();

    /**
     * streaming request handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private StreamRequestHandler srh = null;

    /**
     * the stream request index.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private long sri = -1;

    /**
     * list of unanswered <code>RSVPs</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/08/2003
     */
    private java.util.Vector unansweredRSVPs = new java.util.Vector();

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
     * @since V2.0
     * @version 05/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    NBO()
	throws com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	setCframes(1);
    }

    /**
     * Class constructor to build a <code>NBO</code> for a
     * particular <code>RCO</code> from a <code>SinkInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param rcoI  the <code>RCO</code>.
     * @param sinkI the <code>SourceInterface</code>.
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
     * @since V2.0
     * @version 05/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/15/2001  INB	Created.
     *
     */
    NBO(RCO rcoI,SinkInterface sinkI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(rcoI);
	setAsynchronousDoor(new Door(Door.STANDARD));
	setCframes(1);
	update(sinkI);
    }

    /**
     * Receives acknowledgement from the client.
     * <p>
     *
     * @author Ian Brown
     *
     * @param acknowledgeI the <code>Acknowledge</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.EndOfStreamException
     *		  thrown if there is no way for any data to be collected; for
     *		  example a request for oldest or newest existing data when no
     *		  data exists.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 04/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/07/2004  INB	The <code>unansweredRSVPs</code> list is a simple
     *			<code>Vector</code>, so the binary search doesn't
     *			make sense.  The list should never be very large,
     *			anyway.
     * 04/06/2004  INB	Don't wait for an entry in the
     *			<code>unansweredRSVPs</code> list.  It should already
     *			be here.
     * 07/08/2003  INB	Added handling of <code>unansweredRSVPs</code>.
     * 06/08/2001  INB	Created.
     *
     */
    public final void acknowledge(Acknowledge acknowledgeI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getSRH() != null) {
	    int idx;
	    RSVP target = null;

	    synchronized (unansweredRSVPs) {
		for (idx = 0;
		     (target == null) && (idx < unansweredRSVPs.size());
		     ++idx) {
		    RSVP entry = (RSVP) unansweredRSVPs.elementAt(idx);

		    if (entry.getIdentification() ==
			acknowledgeI.getIdentification()) {
			unansweredRSVPs.removeElementAt(idx);
			target = entry;
		    }
		}
	    }

	    if (target != null) {
		acknowledgeI.setIDVector(target.getIDVector());
		getSRH().acknowledge(acknowledgeI);
	    } else {
		try {
		    throw new RBNBException
			("Unable to match RVSP for " +
			 acknowledgeI +
			 "\nCurrent RSVP list: " +
			 unansweredRSVPs);
		} catch (com.rbnb.api.RBNBException e) {
		    try {
			getLog().addException
			    (Log.STANDARD,
			     getLogClass(),
			     getName(),
			     e);
		    } catch (java.lang.Exception e1) {
		    }
		    return;
		}
	    }
	}
    }

    /**
     * Adds a <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param listenerI  the <code>StreamListener</code>.
     * @see #removeListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/09/2001  INB	Created.
     *
     */
    public final void addListener(StreamListener listenerI) {
    }

    /**
     * Adds a <code>RemoteClient</code> to the list of held ones.
     * <p>
     * The <code>RemoteClient</code> must not be in use when it is handed to
     * this method.
     * <p>
     *
     * @author Ian Brown
     *
     * @param targetI the target of the <code>RemoteClient</code>.
     * @param rcI     the <code>RemoteClient</code>.
     * @see #getRemoteClient(String)
     * @since V2.1
     * @version 05/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/27/2003  INB	Don't hold onto <code>RemoteClients</code> that do
     *			not have a parent.
     * 03/21/2003  INB	Created.
     *
     */
    final void addRemoteClient(String targetI,RemoteClient rcI) {
	if ((rcI == null) || (rcI.getParent() == null)) {
	    return;
	}

	synchronized (remoteClients) {
	    if (!remoteClients.isEmpty()) {
		// For now, we only hold a single <code>RemoteClient</code>.
		java.util.Enumeration keys = remoteClients.keys();
		String key;
		RemoteClient rc;
		while (keys.hasMoreElements()) {
		    key = (String) keys.nextElement();
		    rc = (RemoteClient) remoteClients.get(key);
		    try {
			// Stop the <code>RemoteClient</code>.
			rc.stop();
		    } catch (java.lang.Exception e) {
		    }
		    try {
			rc.getParent().removeChild(rc);
		    } catch (java.lang.Exception e) {
		    }
		}
		remoteClients.clear();
	    }

	    remoteClients.put(targetI,rcI);
	}
    }

    /**
     * Sends an exception caused by an asynchronously running
     * <code>StreamRequestHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param exceptionI  the exception.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #asynchronousResponse(com.rbnb.api.Serializable)
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 03/26/2001  INB	Created.
     *
     */
    final void asynchronousException(java.lang.Exception exceptionI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    getLog().addException
		(Log.STANDARD,
		 getLogClass(),
		 getName(),
		 exceptionI);
	} catch (java.lang.Exception e) {
	}
	try {
	    getAsynchronousDoor().setIdentification(getFullName() + "_asynch");
	    getAsynchronousDoor().lock("NBO.asynchronousException");
	    if (getRCO() != null) {
		getRCO().send(Language.exception(exceptionI));
	    }
	} finally {
	    getAsynchronousDoor().unlock();
	}
    }

    /**
     * Sends a response <code>Serializable</code> that was retrieved by an
     * asynchronously running <code>StreamRequestHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param responseI  the response <code>Serializable</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #asynchronousException(java.lang.Exception)
     * @since V2.0
     * @version 04/06/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/06/2004  INB	Moved sending of response after updating of
     *			<code>RSVP</code> list.  In the original order, it was
     *			possible for the client to acknowledge the response
     *			before we actually had a chance to note that we're
     *			waiting for the acknowledgment.
     * 11/14/2003  INB	Added identification to the <code>Door</code> and
     *			locations to the <code>Locks</code>.
     * 07/08/2003  INB	Added <code>RSVP</code> handling.
     * 03/26/2001  INB	Created.
     *
     */
    final void asynchronousResponse(Serializable responseI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	try {
	    boolean clearSerializable = false;
	    RSVP rsvp = null;

	    getAsynchronousDoor().setIdentification(getFullName() + "_asynch");
	    getAsynchronousDoor().lock("NBO.asynchronousResponse");
	    if (getRCO() != null) {
		if (responseI instanceof RSVP) {
		    rsvp = (RSVP) responseI;
		    rsvp.setIdentification(nextRSVP++);
		    clearSerializable = true;
		    synchronized (unansweredRSVPs) {
			unansweredRSVPs.addElement(rsvp);
		    }
		}
		getRCO().send(responseI);
		if (clearSerializable) {
		    rsvp.setSerializable(null);
		}
	    }

	} finally {
	    getAsynchronousDoor().unlock();
	}
    }

    /**
     * Gets the <code>Door</code> that controls sequencing of asynchronous
     * responses.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @see #setAsynchronousDoor(com.rbnb.api.Door)
     * @since V2.0
     * @version 10/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    private final Door getAsynchronousDoor() {
	return (asynchronousDoor);
    }

    /**
     * Gets the check streaming request flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return check streaming request?
     * @see #setCstream(boolean)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    final boolean getCstream() {
	return (checkStream);
    }

    /**
     * Gets the log class mask for this <code>NBO</code>.
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
	return (super.getLogClass() | Log.CLASS_NBO);
    }

    /**
     * Gets the registration list for this <code>RBO</code> matching the input
     * hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param requestI  the request <code>Rmap</code>.
     * @return the matching registration information.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem during serialization.
     * @exception java.io.EOFException
     *		  thrown if an EOF is encountered while getting the response.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the get is interrupted.
     * @since V2.1
     * @version 03/19/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2003  INB	Created.
     *
     */
    public final Rmap getRegistered(Rmap requestI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getName().indexOf("_RC.") != -1) {
	    return (null);
	}

	if (compareNames(requestI) != 0) {
	    throw new java.lang.IllegalArgumentException
		(requestI + " does not have a matching name.");
	}

	SinkInterface sinkR = (SinkInterface) newInstance();
	sinkR.setName(getName());
        sinkR.setType(getType());
	sinkR.setRemoteID(getRemoteID());

	return ((Rmap) sinkR);
    }

    /**
     * Gets the <code>RemoteClient</code> for a particular target, if any.
     * <p>
     * If there is a <code>RemoteClient</code>, it is removed from the list.
     * <p>
     *
     * @author Ian Brown
     *
     * @param targetI the target of the <code>RemoteClient</code>.
     * @return the <code>RemoteClient</code> or null.
     * @see #addRemoteClient(String,com.rbnb.api.RemoteClient)
     * @since V2.1
     * @version 05/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/27/2003  INB	If the <code>RemoteClient</code> found has no parent,
     *			then throw it away.
     * 03/21/2003  INB	Created.
     *
     */
    final RemoteClient getRemoteClient(String targetI) {
	RemoteClient rcR = null;

	synchronized (remoteClients) {
	    rcR = (RemoteClient) remoteClients.remove(targetI);
	}

	if ((rcR != null) && (rcR.getParent() == null)) {
	    rcR = null;
	}
	return (rcR);
    }

    /**
     * Gets the <code>RequestOptions</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>RequestOptions</code>.
     * @see #setRequestOptions(RequestOptions)
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    public final RequestOptions getRequestOptions() {
	return (ro);
    }

    /**
     * Gets the <code>ServerHandler</code> (our parent).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>ServerHandler</code>.
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
    public final ServerHandler getServer() {
	return ((ServerHandler) getParent());
    }

    /**
     * Gets the <code>StreamRequestHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return  the <code>StreamRequestHandler</code>.
     * @see #setSRH(com.rbnb.api.StreamRequestHandler)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    final StreamRequestHandler getSRH() {
	return (srh);
    }

    /**
     * Gets the stream request index.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the stream request index.
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    final long getSRI() {
	return (sri);
    }

    /**
     * Initiates a request for the child at the specified index.
     * <p>
     * This method starts the process of matching data stored in the RBNB
     * server to a request for data after the RCO associated with this NBO
     * determines that it has received a command to initiate processing of a
     * request for data.  What follows is an overview of how data is retrieved
     * from the RBNB server.
     * <p>
     * A request for data consists of a list of the channels (with optional
     * wildcards) and the time range desired (which may optionally be the
     * oldest or newest data currently available).  There are two basic types
     * of  requests:
     * <p><ol>
     *    <li>A request for data given a specific time or frame (termed
     *	      "regular requests"), or</li>
     *    <li>A request for data whose time is related in some way to the time
     *	      specified in the request (before or after) (termed "time relative
     *	      requests").</li>
     * </ol><p>
     * Either type request can be for a specific duration (termed
     * "request/response"), or for a stream of data with a step size (termed
     * "streaming").
     * <p>
     * Requests are handled by matching the request Rmap hierarchy against the
     * Rmap hierarchy of the RBNB server.  The RBNB server hierarchy is
     * documented in <code>RBNBRouterMap</code>, the root node of the
     * hierarchy.
     * <p>
     * At the highest level, all requests are matched in the same way.  Only
     * when matching an RBO or a plugin does the difference become apparent.
     * The matching for the former is handled two different classes within the
     * RBNB server, while matching for plugins is done by the external plugin
     * code.
     * <p>
     * "Network bus objects" or NBOs, which are the server-side handlers for
     * sink applications, are glorified RBOs.  Instead of one or more ring
     * buffers containing frames of data, NBOs store a list of one or more
     * (currently forced to one) request for data in a single ring buffer.  New
     * requests replace the oldest request in the ring buffer.  Aside from the
     * limitations of a single request in a single ring buffer, NBOs use RBO
     * handling to store those requests as if they were frames.
     * <p>
     * Once a request has been stored in the ring buffer, it can be executed by
     * sending an initiate request message.  The handling of a request is
     * outlined below (some details have been omitted for clarity):
     * <p><ol>
     *    <li>The initiate request message is received by the thread running
     *	      the "RBNB control object" or RCO, which passes is off to its
     *	      associated NBO (<code>RCO.process</code> to
     *	      <code>NBO.initiateRequestAt</code>),</li>
     *    <li>The request is located in the ring buffer.  If it is an aligned
     *	      request for newest/oldest, then the time limits for the requested
     *	      channels are retrieved from the registration map and the times
     *	      for the request are set appropriately
     *	      (<code>RBNBRoutingMap.getRegistered</code> and
     *	      code>DataRequest.updateRequestLimits</code>),</li>
     *    <li>A StreamRequestHandler is created to match the request against
     *	      the routing map and a thread is started to perform the matching
     *	      operation (<code>StreamRequestHandler.start</code>).  Any
     *	      existing children of the routing map are matched against the
     *	      highest level name(s) in the request hierarchy
     *	      (<code>StreamRequestHandler.matchRequest</code), resulting in
     *        the creation of zero or more StreamRemoteListeners and
     *	      StreamRBNBListeners, as appropriate for the matched
     *	      children (<code>StreamRemoteListener.start</code> and
     *        <code>StreamRBNBListener.start),</li>
     *    <li>For streaming requests, the StreamRequestHandler will also
     *	      "listen" for the addition or removal of objects to the routing
     *	      map and will start new child listeners if matches are found
     *	      (<code>RBNBRoutingMap.post</code>,
     *	      <code>AwaitNotification.addEvent</code>,
     *	      <code>AwaitNotification.run</code>, and
     *	      <code>StreamRequestHandler.accept</code>),</li>
     *    <li>StreamRemoteListeners match the top of the remainder of the
     *	      request against the RemoteServer's (which could be a
     *	      ParentServer, a PeerServer, or a ChildServer) children.  Children
     *	      are handled in a manner similar to the way StreamRequestHandler
     *	      works on its children
     *	      (<code>StreamRemoteListener.accept</code>),</li>
     *    <li>If, however, no children match part or all of the request, then
     *	      those parts of the request are sent to the actual remote server
     *	      using a RemoteClient.  This is essentially a sink connection to
     *	      the remote server,</li>
     *    <li>StreamRBNBListeners match the top of the remainder of the request
     *	      against the RBNB's children.  Children are handled in a manner
     *	      similar to the way StreamRequestHandler works on its children
     *	      (<code>StreamPlugInListener</code>,
     *	      <code>StreamRBOListener</code>, and
     *	      <code>StreamTimeRelativeListener</code>).  If the matched child
     *	      is an RBO, then the code checks the type of request.  Regular
     *	      requests are handled by StreamRBOListeners, while time relative
     *	      requests are handled by StreamTimeRelativeListeners,</li>
     *    <li>StreamPlugInListeners handle requests by passing them to the
     *	      plugin application via its server-side RBNBPlugIn
     *	      (<code>StreamPlugInListener.run</code> and
     *	      <code>RBNBPlugIn.initiateRequestAt</code>).</li>
     * </ol><p>
     * The remainder of the handling is specific to StreamRBOListeners or
     * StreamTimeRelativeListeners, as appropriate.  The details for how these
     * classes work can be found in corresponding class documentation.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the child to retrieve.
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
     * @see com.rbnb.api.AwaitNotification
     * @see com.rbnb.api.DataRequest
     * @see com.rbnb.api.RBNBRoutingMap
     * @see com.rbnb.api.RBNBPlugIn
     * @see com.rbnb.api.RBO
     * @see com.rbnb.api.RCO
     * @see com.rbnb.api.StreamPlugInListener
     * @see com.rbnb.api.StreamRequestHandler
     * @see com.rbnb.api.StreamRBOListener
     * @see com.rbnb.api.StreamRemoteListener
     * @see com.rbnb.api.StreamTimeRelativeListener
     * @see #addChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 07/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2004  INB	Added overview of processing.
     * 04/02/2001  INB	Created.
     *
     */
    public final void initiateRequestAt(int indexI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getSRH() != null) {
	    // If there is an old request running, terminate it.
	    setCstream(false);
	    getSRH().stop();
	    setSRH(null);
	}

	Rmap child = locateChildAt(indexI);
	if (child == null) {
	    throw new java.lang.ArrayIndexOutOfBoundsException
		("No request " + indexI + " exists.");
	}

	// Mark leaf nodes.
	if (!getRCO().isSupported(IsSupported.FEATURE_REQUEST_LEAF_NODES)) {
	    child.markLeaf();
	}

	// Copy the request to eliminate unneeded info and to create a
	// <code>DataRequest</code>.
	DataRequest request;

	if (child instanceof DataRequest) {
	    request = (DataRequest) child.clone();
	    request.setName(null);
	    request.setTrange(null);
	    request.setFrange(null);

	} else {
	    request = new DataRequest();
	    if (child.getNchildren() > 0) {
		request.setChildren
		    ((RmapVector) child.getChildren().clone());
	    }
	    if (child.getNmembers() > 0) {
		request.setMembers
		    ((RmapVector) child.getMembers().clone());
	    }
	}

	if (request.getReference() == request.ALIGNED) {
	    // Synchronized requests for newest/oldest are handled by
	    // retrieving the registration list and finding the newest (oldest)
	    // data point for all of the channels.
	    Rmap rHandler = null;
	    for (rHandler = getParent();
		 !(rHandler instanceof RoutingMapHandler);
		 rHandler = rHandler.getParent()) {
	    }
	    Rmap rmap = ((RoutingMapHandler) rHandler).getRegistered(request);

	    TimeRange tLimits = new TimeRange(Double.MAX_VALUE),
		fLimits = new TimeRange(Double.MAX_VALUE);
	    tLimits.setDuration(-Double.MAX_VALUE);
	    fLimits.setDuration(-Double.MAX_VALUE);
	    rmap.findLimits(tLimits,fLimits);
	    if ((tLimits.getDuration() == -Double.MAX_VALUE) &&
		(fLimits.getDuration() == -Double.MAX_VALUE)) {
		asynchronousResponse(new EndOfStream(EndOfStream.REASON_EOD));
		return;
	    }

	    request.updateRequestLimits(tLimits,fLimits);
	}

	/*
	getLog().addMessage
	    (getLogLevel() + 10,
	     getLogClass(),
	     getName(),
	     "Initiating request.\n" + request);
	*/
	if ((getRequestOptions() != null) &&
	    getRequestOptions().getExtendStart()) {
	    request.setRelationship(DataRequest.LESS_EQUAL);
	}

	// Create a <code>StreamRequestHandler</code>.
	setSRH(new StreamRequestHandler(this,request));
	setSRI(indexI);
	getSRH().start();
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
     * 04/03/2001  INB	Created.
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
	if (true) {
	    return (Rmap.MATCH_NOINTERSECTION);
	}

	byte reasonR = Rmap.MATCH_UNKNOWN;
	if ((extractorI.getWorkRequest() instanceof DataRequest) &&
	    ((((DataRequest) extractorI.getWorkRequest()).getReference() !=
	      DataRequest.ABSOLUTE))) {
	    // If this is a special request (NEWEST/OLDEST/AFTER/MODIFIED), use
	    // the method in <code>RBO</code>.
	    reasonR = super.moveDownFrom(extractorI,unsatisfiedI,unsatisfiedO);

	} else {
	    // If this is normal request, we need to do a two-level
	    // match.

	    // First, use a match against the <code>NBO</code> to build a
	    // second level request.
	    Rmap second = new DataRequest(),
		child = extractNewRequest(unsatisfiedI.combinedRequest());

	    if (child != null) {
		second.addChild(child);

		// Second, use that to actually build matching information.
		Rmap result = getParent().extractRmap
		    (second,
		     extractorI.getExtractData());
		extractorI.addInformation(result);
	    }
	}

	return (reasonR);
    }

    /**
     * Creates a new instance of the same class as this <code>NBO</code> (or a
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
	return (SinkIO.newInstance(this));
    }

    /**
     * Removes a <code>StreamListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param listenerI  the <code>StreamListener</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #addListener(com.rbnb.api.StreamListener)
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/09/2001  INB	Created.
     *
     */
    public final void removeListener(StreamListener listenerI)
	throws java.lang.InterruptedException {
    }

    /**
     * Sets the <code>Door</code> that controls the sequencing of asynchronous
     * responses.
     * <p>
     *
     * @author Ian Brown
     *
     * @param asynchronousDoorI the <code>DoorI</code>.
     * @see #getAsynchronousDoor()
     * @since V2.0
     * @version 10/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/23/2001  INB	Created.
     *
     */
    private final void setAsynchronousDoor(Door asynchronousDoorI) {
	asynchronousDoor = asynchronousDoorI;
    }

    /**
     * Sets the check streaming request flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cStreamI  check streaming request?
     * @see #getCstream()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2001  INB	Created.
     *
     */
    private final void setCstream(boolean cStreamI) {
	checkStream = cStreamI;
    }

    /**
     * Sets the <code>RequestOptions</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param roI the <code>RequestOptions</code>.
     * @see #getRequestOptions()
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    public final void setRequestOptions(RequestOptions roI) {
	ro = roI;
    }

    /**
     * Sets the <code>StreamRequestHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param srhI  the <code>StreamRequestHandler</code>.
     * @see #getSRH()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    private final void setSRH(StreamRequestHandler srhI) {
	srh = srhI;
    }

    /**
     * Sets the stream request index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sriI  the stream request index.
     * @see #getSRI()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2001  INB	Created.
     *
     */
    private final void setSRI(long sriI) {
	sri = sriI;
    }

    /**
     * Shutdown the <code>NBO</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2003  INB	Made final. Clear <code>RemoteClients</code> list.
     * 06/11/2001  INB	Created.
     *
     */
    final void shutdown() {
	if (getSRH() != null) {
	    try {
		// Terminate any running request.
		setCstream(false);
		getSRH().stop();
		setSRH(null);
	    } catch (java.lang.Exception e) {
	    }
	}
	synchronized (remoteClients) {
	    if (!remoteClients.isEmpty()) {
		// Toss the <code>RemoteClients</code>.
		java.util.Enumeration keys = remoteClients.keys();
		String key;
		RemoteClient rc;
		while (keys.hasMoreElements()) {
		    key = (String) keys.nextElement();
		    rc = (RemoteClient) remoteClients.get(key);
		    try {
			// Stop the <code>RemoteClient</code>.
			rc.stop();
		    } catch (java.lang.Exception e) {
		    }
		    try {
			rc.getParent().removeChild(rc);
		    } catch (java.lang.Exception e) {
		    }
		}
		remoteClients.clear();
	    }
	}


	// Perform the <code>RBO</code> shutdown.
	super.shutdown();
    }

    /**
     * Stops the <code>SourceHandler</code> on an I/O exception in the
     * <code>RCO</code>.
     * <p>
     * For <code>NBOs</code>, this does nothing. The <code>NBO</code> will shut
     * down normally.
     * <p>
     *
     * @author Ian Brown
     *
     * @return did the stop actually occur?
     * @exception java.lang.InterruptedException
     *		  thrown if this method is interrupted.
     * @since V2.1
     * @version 04/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/08/2003  INB	Created.
     *
     */
    public final boolean stopOnIOException()
	throws java.lang.InterruptedException
    {
	return (false);
    }

    /**
     * Synchronizes with the client-side.
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
     * @exception java.lang.IllegalStateException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>the <code>NBO</code> has not been connected to a
     *		      server,</li>
     *		  <li>the <code>NBO</code> is not running.</li>
     *		  </ul><p>
     * @exception java.lang.InterruptedException
     *		  thrown if the synchronization is interrupted.
     * @see #acceptFrame(boolean)
     * @see #addChild(com.rbnb.api.Rmap)
     * @since V2.0
     * @version 06/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/30/2001  INB	Created.
     *
     */
    public final void synchronizeWclient()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (getSRH() != null) {
	    // Terminate any running request.
	    try {
		setCstream(false);
		getSRH().stop();
		setSRH(null);
	    } catch (java.lang.Exception e) {
	    }
	}

	// Now perform an <code>RBO</code> synchronizeWclient.
	super.synchronizeWclient();
    }

    /**
     * Updates this <code>NBO</code> using the input
     * <code>ClientInterface</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param clientI  the <code>ClientInterface</code> providing the update.
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
     *		  thrown if the <code>ClientInterface</code> is not a
     *		  <code>RBNBClient</code>.
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
     * 01/12/2001  INB	Created.
     *
     */
    public final void update(ClientInterface clientI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (!(clientI instanceof SinkInterface)) {
	    throw new java.lang.IllegalArgumentException
		(clientI + " cannot be used to update " + this);
	}

	// The only real fields that can be updated are in <code>RBO</code>.
	super.update(clientI);
    }
}
