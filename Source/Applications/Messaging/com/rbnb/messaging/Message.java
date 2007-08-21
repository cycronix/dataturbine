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

package com.rbnb.messaging;

/**
 * Sends messages via the RBNB server to a messaging host.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.messaging.Host
 * @since V2.2
 * @version 06/20/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/20/2003  INB	Created.
 *
 */
public final class Message {
    /**
     * the SAPI sink to use to send the messages.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/20/2003
     */
    private com.rbnb.sapi.Sink sink = null;

    /**
     * Class constructor to build a <code>Message</code> for a
     * <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Created.
     *
     */
    public Message(com.rbnb.sapi.Sink sinkI) {
	super();
	sink = sinkI;
    }

    /**
     * Creates a <code>ChannelMap</code> for the specified messaging
     * destination.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hostPathI the path to the messaging host.
     * @param groupI    the message group to put the message in.
     * @param userI     the user to assign the message to.  This may be
     *			<code>null</code>, in which case the
     *			<code>Sink's</code> name will be used.
     * @return the <code>ChannelMap</code>.
     * @exceptions com.rbnb.sapi.SAPIException
     *		   if there is a problem building the <code>ChannelMap</code>.
     * @since V2.2
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Created.
     *
     */
    private final com.rbnb.sapi.ChannelMap createMap
	(String hostPathI,
	 String groupI,
	 String userI)
	throws com.rbnb.sapi.SAPIException
    {
	com.rbnb.sapi.ChannelMap outmapR = new com.rbnb.sapi.ChannelMap();

	int slash = hostPathI.lastIndexOf("/");
	String serverPath = ((slash == -1) ?
			     "" :
			     hostPathI.substring(0,slash + 1));
	String hostName = hostPathI.substring(slash + 1);
	outmapR.Add(serverPath +
		    "_" + hostName +
		    "/" + groupI +
		    ((userI == null) ? "" : ("/" + userI)));

	return (outmapR);
    }

    /**
     * Sends a string message to the messaging host.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hostPathI the path to the messaging host.
     * @param groupI    the message group to put the message in.
     * @param userI     the user to assign the message to.  This may be
     *			<code>null</code>, in which case the
     *			<code>Sink's</code> name will be used.
     * @param messageI  the message to send.
     * @return a <code>ChannelMap</code> containing the response from the
     *	       messaging host.<p>
     *	       <ul>
     *	          <li>An empty <code>ChannelMap</code> indicates that the host
     *		      was not reached,</li>
     *	          <li>Otherwise, there should be one channel with string data
     *		      that starts with one of the following two strings:<p>
     *		      <ol>
     *			 <li>Received - indicates that the message was
     *			     accepted, or</li>
     *			 <li>Rejected - indicates that the message was rejected
     *			     by the messaging host due to a user naming
     *			     conflict.</li>
     *		      </ol></li>
     *	       </ul><p>
     *	       Additional information may be provided, depending on whether the
     *	       messaging host has "fancy receipts" enabled.
     * @exceptions com.rbnb.sapi.SAPIException
     *		   if there is a problem sending the message.
     * @see #sendMessage(String hostPathI,String groupI,String userI,byte[] messageI)
     * @since V2.2
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Created.
     *
     */
    public final com.rbnb.sapi.ChannelMap sendMessage
	(String hostPathI,
	 String groupI,
	 String userI,
	 String messageI)
	throws com.rbnb.sapi.SAPIException
    {
	com.rbnb.sapi.ChannelMap outmap = createMap(hostPathI,groupI,userI);
	outmap.PutDataAsString(0,messageI);
	return (transmit(outmap));
    }

    /**
     * Sends a byte array message to the messaging host.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hostPathI the path to the messaging host.
     * @param groupI    the message group to put the message in.
     * @param userI     the user to assign the message to.  This may be
     *			<code>null</code>, in which case the
     *			<code>Sink's</code> name will be used.
     * @param messageI  the message to send.
     * @return a <code>ChannelMap</code> containing the response from the
     *	       messaging host.<p>
     *	       <ul>
     *	          <li>An empty <code>ChannelMap</code> indicates that the host
     *		      was not reached,</li>
     *	          <li>Otherwise, there should be one channel with string data
     *		      that starts with one of the following two strings:<p>
     *		      <ol>
     *			 <li>Received - indicates that the message was
     *			     accepted, or</li>
     *			 <li>Rejected - indicates that the message was rejected
     *			     by the messaging host due to a user naming
     *			     conflict.</li>
     *		      </ol></li>
     *	       </ul><p>
     *	       Additional information may be provided, depending on whether the
     *	       messaging host has "fancy receipts" enabled.
     * @exceptions com.rbnb.sapi.SAPIException
     *		   if there is a problem sending the message.
     * @see #sendMessage(String hostPathI,String groupI,String userI,String messageI)
     * @since V2.2
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Created.
     *
     */
    public final com.rbnb.sapi.ChannelMap sendMessage
	(String hostPathI,
	 String groupI,
	 String userI,
	 byte[] messageI)
	throws com.rbnb.sapi.SAPIException
    {
	com.rbnb.sapi.ChannelMap outmap = createMap(hostPathI,groupI,userI);
	outmap.PutDataAsByteArray(0,messageI);
	return (transmit(outmap));
    }

    /**
     * Transmits a message to the messaging host and returns its reply.
     * <p>
     *
     * @author Ian Brown
     *
     * @param outmapI the output <code>ChannelMap</code>.
     * @return a <code>ChannelMap</code> containing the response from the
     *	       messaging host.<p>
     *	       <ul>
     *	          <li>An empty <code>ChannelMap</code> indicates that the host
     *		      was not reached,</li>
     *	          <li>Otherwise, there should be one channel with string data
     *		      that starts with one of the following two strings:<p>
     *		      <ol>
     *			 <li>Received - indicates that the message was
     *			     accepted, or</li>
     *			 <li>Rejected - indicates that the message was rejected
     *			     by the messaging host due to a user naming
     *			     conflict.</li>
     *		      </ol></li>
     *	       </ul><p>
     *	       Additional information may be provided, depending on whether the
     *	       messaging host has "fancy receipts" enabled.
     * @exceptions com.rbnb.sapi.SAPIException
     *		   if there is a problem sending the message.
     * @since V2.2
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Created.
     *
     */
    private final com.rbnb.sapi.ChannelMap transmit
	(com.rbnb.sapi.ChannelMap outmapI)
	throws com.rbnb.sapi.SAPIException
    {
	sink.Request(outmapI,0,1,"newest");
	com.rbnb.sapi.ChannelMap outmapR = sink.Fetch(-1);
	return (outmapR);
    }
}
