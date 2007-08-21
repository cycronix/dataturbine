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
 * Messaging host class.
 * <p>
 * This class provides the capability to host messages. A host has two
 * connections to the server:
 * <p><ol>
 *     <li>A plugin connection called <it>_Host</it>, and</li>
 *     <li>A source connection called <it>Host</it>.</li>
 * </ol><p>
 * Where Host is the name of the host connection.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.messaging.Message
 * @since V2.0
 * @version 08/27/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/27/2003  INB	Divide the time value by 1000 to convert from
 *			milliseconds to seconds since 1970.
 * 08/04/2003  INB	Eliminated special "chat" mode handling.  All messages
 *			are now stored using the original data type.
 * 07/31/2003  INB	Added the concept of command strings.  Implemented
 *			!DELETE.
 * 06/20/2003  INB	Created from <code>com.rbnb.chat.Host</code>.  Added
 *			code to allow chat or strict message data type
 *			handling.   Renamed chat rooms to groups and eliminated
 *			all references to chat except with regards to the
 *			message handling.
 * 05/28/2003  INB	<code>Sink.GetServerName</code> no longer throws
 *			exceptions.
 * 04/09/2003  INB	Allow chat users to specify their name by adding an
 *			extra level to the channel name.
 * 04/02/2003  INB	Turn off message number/sender's name in receipt code
 *			by default.
 * 03/28/2003  INB	Handle byte array data type.
 * 03/26/2003  INB	Allow the <code>Source</code> connection to be turned
 *			off.
 * 03/24/2003  INB	Added <code>FullUser<code> class to keep track of the
 *			number of messages received for a client.
 * 03/21/2003  INB	Put the sender's name into the receipt.
 * 03/13/2003  INB	Added blank marker byte to registration return.
 * 01/07/2003  INB	Created.
 *
 */
public final class Host
    implements java.lang.Runnable
{
    /**
     * fancy receipts?
     * <p>
     * Fancy receipts include the sender's name and the message count in the
     * receipt.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/02/2003
     */
    private boolean fancyReceipts = false;

    /**
     * have password?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/15/2003
     */
    private boolean havePassword = false;

    /**
     * the plugin connection used to receive messages and send receipts.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/07/2003
     */
    private com.rbnb.sapi.PlugIn plugin = null;

    /**
     * the source connection used to store messages.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/07/2003
     */
    private com.rbnb.sapi.Source source = null;

    /**
     * is this <code>Host</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2003
     */
    private boolean running = false;

    /**
     * is this <code>Host</code> supposed to terminate?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/07/2003
     */
    private boolean stopped = true;

    /**
     * the running thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2003
     */
    private Thread thread = null;

    /**
     * user to full name mapping.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/16/2003
     */
    private java.util.Hashtable userMapping = new java.util.Hashtable();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/07/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2003  INB	Created.
     *
     */
    public Host() {
	super();
    }

    /**
     * Builds a <code>Host</code> from a server address and a host connection
     * name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the server address.
     * @param nameI     the connection name (also the username).
     * @param passwordI the password.
     * @param cacheI    the size of the cache in frames.
     * @param aModeI    the archive mode:
     *		        <p><ul>
     *			   <li>None - no archive,</li>
     *			   <li>Create - create a new archive,</li>
     *			   <li>Append - append to an existing archive.</li>
     *			   </ul>
     * @param archiveI  the size of the archive in frames. This must be non-
     *		        zero for <code>aModeI = Create</li>.  For <code>aModeI
     *			= Append</li>, the size can be 0 (use existing size) or
     *		        non-zero (set new size).
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @exception java.lang.IllegalArgumentException
     *		  if the archive mode and size are not consistent.
     * @exception java.lang.IllegalStateException
     *		  if this object is already connected.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use the version that takes the <code>useSourceI</code>
     *			parameter.
     * 01/07/2003  INB	Created.
     *
     */
    public Host(String addressI,
		String nameI,
		String passwordI,
		long cacheI,
		String aModeI,
		long archiveI)
	throws com.rbnb.sapi.SAPIException
    {
	this(addressI,nameI,passwordI,true,cacheI,aModeI,archiveI);
    }

    /**
     * Builds a <code>Host</code> from a server address and a host connection
     * name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI   the server address.
     * @param nameI      the connection name (also the username).
     * @param passwordI  the password.
     * @param useSourceI use a <code>Source</code> connection.
     * @param cacheI	 the size of the cache in frames.
     * @param aModeI	 the archive mode:
     *			 <p><ul>
     *			    <li>None - no archive,</li>
     *			    <li>Create - create a new archive,</li>
     *			    <li>Append - append to an existing archive.</li>
     *			 </ul>
     * @param archiveI  the size of the archive in frames. This must be non-
     *		        zero for <code>aModeI = Create</li>.  For <code>aModeI
     *			= Append</li>, the size can be 0 (use existing size) or
     *		        non-zero (set new size).
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @exception java.lang.IllegalArgumentException
     *		  if the archive mode and size are not consistent.
     * @exception java.lang.IllegalStateException
     *		  if this object is already connected.
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created from the version that doesn't have a
     *			<code>useSourceI</code> parameter.
     *
     */
    public Host(String addressI,
		String nameI,
		String passwordI,
		boolean useSourceI,
		long cacheI,
		String aModeI,
		long archiveI)
	throws com.rbnb.sapi.SAPIException
    {
	this();
	open(addressI,nameI,passwordI,useSourceI,cacheI,aModeI,archiveI);
    }

    /**
     * Adds the specified sender to the user mappings for the group.
     * <p>
     *
     * @author Ian Brown
     *
     * @param groupI	    the message group.
     * @param senderI	    the name of the sender.
     * @param fullSenderI   the full name of the sender.
     * @param messageCountI the message count.
     * @see #removeSender(String roomI,String senderI,String fullSenderI)
     * @since V2.0
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Replaced room with group.
     * 03/24/2003  INB	Added the message count.
     * 01/16/2003  INB	Created.
     *
     */
    private final void addSender(String groupI,
				 String senderI,
				 String fullSenderI,
				 long messageCountI)
    {
	java.util.Hashtable table =
	    (java.util.Hashtable) userMapping.get(groupI);
	FullUser fullUser = new FullUser(fullSenderI,messageCountI);

	if (table == null) {
	    table = new java.util.Hashtable();
	    userMapping.put(groupI,table);
	}

	table.put(senderI,fullUser);
    }

    /**
     * Checks a sender against the list of valid senders for the specified
     * group.
     * <p>
     *
     * @author Ian Brown
     *
     * @param groupI	  the message group.
     * @param senderI	  the name of the sender.
     * @param fullSenderI the full name of the sender.
     * @return the sender's message count (0 if not allowed).
     * @since V2.0
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Replaced room with group.
     * 04/09/2003  INB	A <code>null fullSenderI</code> value indicates that
     *			this is a special client who gave his or her own
     *			name.
     * 03/24/2003  INB	Added handling of the message count.
     * 01/16/2003  INB	Created.
     *
     */
    private final long checkSender(String groupI,
				   String senderI,
				   String fullSenderI)
    {
	java.util.Hashtable table =
	    (java.util.Hashtable) userMapping.get(groupI);
	long messageCount = 1;
	FullUser fullUser = null;

	if (table == null) {
	    addSender(groupI,senderI,fullSenderI,messageCount);
	} else {
	    fullUser = (FullUser) table.get(senderI);

	    if (fullUser == null) {
		addSender(groupI,senderI,fullSenderI,messageCount);
	    } else if ((fullSenderI == null) &&
		       (fullUser.fullUser != null)) {
		messageCount = 0;
	    } else if ((fullSenderI != null) &&
		       (fullUser.fullUser == null)) {
		messageCount = 0;
	    } else if ((fullSenderI != null) &&
		       !fullUser.fullUser.equals(fullSenderI)) {
		messageCount = 0;
	    } else {
		messageCount = ++fullUser.messageCount;
	    }
	}

	return (messageCount);
    }

    /**
     * Closes this <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #open(String addressI,String nameI,String passwordI,long cacheI,String aModeI,long archiveI)
     * @see #open(String addressI,String nameI,String passwordI,boolean useSourceI,long cacheI,String aModeI,long archiveI)
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Added documentation of <code>open</code> with the
     *			<code>useSourceI</code> parameter.
     * 01/07/2003  INB	Created.
     *
     */
    public final void close() {
	stop();
	if (plugin != null) {
	    plugin.CloseRBNBConnection();
	    plugin = null;
	}
	if (source != null) {
	    source.CloseRBNBConnection();
	    source = null;
	}
    }

    /**
     * Gets the fancy receipts flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return provide fancy receipts?
     * @see #setFancyReceipts(boolean fancyReceiptsI)
     * @since V2.1
     * @version 04/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/02/2003  INB	Created.
     *
     */
    public final boolean getFancyReceipts() {
	return (fancyReceipts);
    }

    /**
     * Gets the fully qualified name of this host.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the fully qualified name.
     * @since V2.0
     * @version 05/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/29/2003  INB	Don't need to catch exceptions any more.
     * 01/09/2003  INB	Created.
     *
     */
    public final String getHostName() {
	return ((source == null) ?
		null :
		source.GetServerName() + "/" + source.GetClientName());
    }

    /**
     * Simple command-line main method for testing.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments:
     *		    <p><ul>
     *		       <li>-a <it>server address</it></li>
     *		       <li>-c <it>cache frames</it></li>
     *		       <li>-C</li>
     *		       <li>-f <it>archive frames</it></li>
     *		       <li>-F <it>archive mode</it></li>
     *		       <li>-n <it>host connection name</it></li>
     *		       <li>-R</li>
     *		       <li>-S</li>
     *		    </ul>
     * @since V2.0
     * @version 08/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/04/2003  INB	Eliminated -C flag.
     * 06/20/2003  INB	Added the -C flag.
     * 04/02/2003  INB	Added the -R flag.
     * 03/26/2003  INB	Added the -S flag.
     * 01/08/2003  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	Host host = null;

	try {
	    String address = "localhost:3333",
		hostName = "host-chat",
		aMode = "None",
		value;
	    boolean useSource = true;
	    boolean fancy = false;
	    long archive = 0;
	    long cache = 1000;
	    com.rbnb.utility.ArgHandler argH = new
		com.rbnb.utility.ArgHandler(argsI);

	    if ((value = argH.getOption('a')) != null) {
		address = value;
	    }
	    if ((value = argH.getOption('c')) != null) {
		cache = Long.parseLong(value);
	    }
	    if ((value = argH.getOption('f')) != null) {
		archive = Long.parseLong(value);
		if (archive == 0) {
		    aMode = "None";
		} else if (archive > 0) {
		    aMode = "Create";
		}
	    }
	    if ((value = argH.getOption('F')) != null) {
		aMode = value;
	    }
	    if ((value = argH.getOption('n')) != null) {
		hostName = value;
	    }
	    fancy = argH.checkFlag('R');
	    useSource = !argH.checkFlag('S');

	    host = new Host(address,
			    hostName,
			    null,
			    useSource,
			    cache,
			    aMode,
			    archive);
	    host.setFancyReceipts(fancy);
	    host.start();

	} catch (java.lang.Exception e) {
	    if (host != null) {
		host.stop();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * Opens this <code>Host</code> on the specified server with the specified
     * connection name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the server address.
     * @param nameI     the connection name (also the username).
     * @param passwordI the password.
     * @param cacheI    the size of the cache in frames.
     * @param aModeI    the archive mode:
     *		        <p><ul>
     *			   <li>None - no archive,</li>
     *			   <li>Create - create a new archive,</li>
     *			   <li>Append - append to an existing archive.</li>
     *			   </ul>
     * @param archiveI  the size of the archive in frames. This must be non-
     *		        zero for <code>aModeI = Create</li>.  For <code>aModeI
     *			= Append</li>, the size can be 0 (use existing size) or
     *		        non-zero (set new size).
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @exception java.lang.IllegalArgumentException
     *		  if the archive mode and size are not consistent.
     * @exception java.lang.IllegalStateException
     *		  if this object is already connected.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Use the version that takes the <code>useSourceI</code>
     *			parameter.
     * 01/07/2003  INB	Created.
     *
     */
    public final void open(String addressI,
			   String nameI,
			   String passwordI,
			   long cacheI,
			   String aModeI,
			   long archiveI)
	throws com.rbnb.sapi.SAPIException
    {
	open(addressI,nameI,passwordI,true,cacheI,aModeI,archiveI);
    }

    /**
     * Opens this <code>Host</code> on the specified server with the specified
     * connection name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the server address.
     * @param nameI     the connection name (also the username).
     * @param passwordI the password.
     * @param useSourceI use a <code>Source</code> connection.
     * @param cacheI    the size of the cache in frames.
     * @param aModeI    the archive mode:
     *		        <p><ul>
     *			   <li>None - no archive,</li>
     *			   <li>Create - create a new archive,</li>
     *			   <li>Append - append to an existing archive.</li>
     *			   </ul>
     * @param archiveI  the size of the archive in frames. This must be non-
     *		        zero for <code>aModeI = Create</li>.  For <code>aModeI
     *			= Append</li>, the size can be 0 (use existing size) or
     *		        non-zero (set new size).
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @exception java.lang.IllegalArgumentException
     *		  if the archive mode and size are not consistent.
     * @exception java.lang.IllegalStateException
     *		  if this object is already connected.
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created from the version that doesn't have a
     *			<code>useSourceI</code> parameter.
     *
     */
    public final void open(String addressI,
			   String nameI,
			   String passwordI,
			   boolean useSourceI,
			   long cacheI,
			   String aModeI,
			   long archiveI)
	throws com.rbnb.sapi.SAPIException
    {
	if ((aModeI.equalsIgnoreCase("None") && (archiveI > 0)) ||
	    (aModeI.equalsIgnoreCase("Create") && (archiveI == 0))) {
	    throw new java.lang.IllegalArgumentException
		("Archive mode (" + aModeI + ") is not consistent with " +
		 "specified archive size (" + archiveI + ").");
	}
	if ((plugin != null) || (source != null)) {
	    throw new java.lang.IllegalStateException
		("Host is already connected to a server.");
	}
	String name = nameI;
	havePassword = ((passwordI != null) && (passwordI.length() != 0));
	if (useSourceI) {
	    source = new com.rbnb.sapi.Source((int) cacheI,
					      aModeI,
					      (int) archiveI);
	    source.OpenRBNBConnection(addressI,nameI,nameI,passwordI);
	    name = source.GetClientName();
	}
	plugin = new com.rbnb.sapi.PlugIn();
	plugin.OpenRBNBConnection(addressI,
				  "_" + name,
				  nameI,
				  (((passwordI != null) &&
				   (passwordI.length() > 0)) ?
				   passwordI :
				   null));
    }

    /**
     * Processes a message.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the message received.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @since V2.0
     * @version 08/27/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/27/2003  INB	Divide the time value by 1000 to convert from
     *			milliseconds to seconds since 1970.
     * 08/04/2003  INB	Eliminated "chat" mode handling.  Replaced with code
     *			that uses PutDataRef.  Use the time that this method
     *			starts as the time stamp for all of the channels,
     *			rather than timeofday.
     * 07/31/2003  INB	Added the concept of command strings.  Implemented
     *			!DELETE.
     * 06/20/2003  INB	Changed chat room to message group.  Added chat or
     *			regular message handling code.
     * 04/09/2003  INB	Allow chat users to specify their name by adding an
     *			extra level to the channel name.
     * 04/02/2003  INB	Fancy receipt handling.
     * 03/28/2003  INB	Handle byte array data type.
     * 03/26/2003  INB	Handle the lack of a <code>Source</code>.
     * 03/24/2003  INB	Added handling of message count.
     * 03/21/2003  INB	Put the sender's name into the receipt.
     * 01/07/2003  INB	Created.
     *
     */
    private final void process(com.rbnb.sapi.PlugInChannelMap messageI)
	throws com.rbnb.sapi.SAPIException
    {
	String sender = messageI.GetRequestId();
	String messageGroup;
	String[] message = null;
	com.rbnb.sapi.ChannelMap cmap = new com.rbnb.sapi.ChannelMap(),
	    cmap2;
	sender = sender.substring(sender.lastIndexOf(".") + 2);
	long messageCount,
	    timeOfDay = System.currentTimeMillis();

	int lIdx = -1,
	    lIO = sender.lastIndexOf("_"),
	    idx = sender.indexOf("_");
	do {
	    if (idx == lIO) {
		int idx1;
		for (idx1 = idx + 1; idx1 < sender.length(); ++idx1) {
		    if (!Character.isDigit(sender.charAt(idx1))) {
			break;
		    }
		}
		if (idx1 == sender.length()) {
		    break;
		}
	    }
	    sender = sender.substring(0,idx) + "/" + sender.substring(idx + 1);
	    lIdx = idx;
	} while ((idx = sender.indexOf("_",lIdx)) != -1);
	String fullSender = sender;
	sender = sender.substring(sender.lastIndexOf("/") + 1);

	String rSender,
	    rFullSender;
	int slash;
	for (idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
	    messageGroup = messageI.GetName(idx);
	    slash = messageGroup.indexOf("/");

	    if (slash == -1) {
		rSender = sender;
		rFullSender = fullSender;
	    } else {
		rSender = messageGroup.substring(slash + 1);
		rFullSender = null;
		messageGroup = messageGroup.substring(0,slash);
	    }

	    if ((messageCount = checkSender(messageGroup,
					    rSender,
					    rFullSender)) == 0) {
		messageI.PutDataAsString(idx,
					 (getFancyReceipts() ?
					  "Rejected for " + rSender + "." :
					  "Rejected."));

	    } else {
		message = null;
		if (messageI.GetType(idx) ==
		    com.rbnb.sapi.ChannelMap.TYPE_STRING) {
		    message = messageI.GetDataAsString(idx);
		}

		if ((message != null) && (message[0].charAt(0) == '!')) {
		    if (!message[0].startsWith("!DELETE")) {
			messageI.PutDataAsString
			    (idx,
			     "Rejected unknown command " + message[0]);
		    } else {
			cmap.Clear();
			cmap.Add(messageGroup + "/" + rSender);
			cmap2 = source.Delete(cmap);
			messageI.PutDataAsString
			    (idx,
			     "Received" +
			     (havePassword ? " (secure)" : "") +
			     " command !DELETE - server responds " +
			     cmap2.GetDataAsString(0)[0]);
		    }

		} else {
		    cmap.Clear();
		    cmap.Add(messageGroup + "/" + rSender);
		    cmap.PutTime(timeOfDay/1000.,0.);
		    if (message != null) {
			for (int idx1 = 0; idx1 < message.length; ++idx1) {
			    if (message[idx1].equals
				(rSender +
				 " has left this group.")) {
				removeSender(messageGroup,rSender,rFullSender);
			    }
			}
		    }

		    cmap.PutDataRef(0,messageI,idx);
		    if (source != null) {
			source.Flush(cmap,false);
		    }

		    messageI.PutDataAsString
			(idx,
			 (getFancyReceipts() ?
			  "Received" + (havePassword ? " (secure) #" : " #") +
			  messageCount + " for " + rSender + "." :
			  "Received" + (havePassword ? " (secure)." : ".")));
		}
	    }
	}
    }

    /**
     * Removes the specified sender from the user mappings for the group.
     * <p>
     *
     * @author Ian Brown
     *
     * @param groupI	  the message group.
     * @param senderI	  the name of the sender.
     * @param fullSenderI the full name of the sender.
     * @see #addSender(String groupI,String senderI,String fullSenderI)
     * @since V2.0
     * @version 06/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/20/2003  INB	Renamed chat room to message group.
     * 01/16/2003  INB	Created.
     *
     */
    private final void removeSender(String groupI,
				    String senderI,
				    String fullSenderI)
    {
	java.util.Hashtable table =
	    (java.util.Hashtable) userMapping.get(groupI);

	if (table != null) {
	    table.remove(senderI);
	}

    }

    /**
     * Runs this <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.0
     * @version 03/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/13/2003  INB	Added blank marker byte array to registration.
     * 01/07/2003  INB	Created.
     *
     */
    public final void run() {
	try {
	    thread = Thread.currentThread();
	    synchronized (this) {
		Thread.currentThread().sleep(1000);
		running = true;
		notifyAll();
	    }

	    com.rbnb.sapi.PlugInChannelMap picmap = null;

	    while (!stopped &&
		   ((picmap = plugin.Fetch(1000,picmap)) != null)) {
		if (picmap.GetIfFetchTimedOut()) {
		    continue;
		} else if (picmap.GetRequestReference().equalsIgnoreCase
			   ("registration")) {
		    for (int idx = 0; idx < picmap.NumberOfChannels(); ++idx) {
			picmap.PutDataAsInt8(idx,new byte[1]);
		    }
		    plugin.Flush(picmap);
		} else {
		    process(picmap);
		    plugin.Flush(picmap);
		}
	    }

	} catch (com.rbnb.sapi.SAPIException e) {
	    e.printStackTrace();

	} catch (java.lang.InterruptedException e) {
	    e.printStackTrace();

	} finally {
	    close();

	    synchronized (this) {
		running = false;
		notifyAll();
	    }
	}
    }

    /**
     * Sets the fancy receipts flag.
     * <p>
     * Fancy receipts include the sender's name and the message number.
     * <p>
     *
     * @author Ian Brown
     *
     * @param fancyReceiptsI provide fancy receipts?
     * @see #getFancyReceipts()
     * @since V2.1
     * @version 04/02/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/02/2003  INB	Created.
     *
     */
    public final void setFancyReceipts(boolean fancyReceiptsI) {
	fancyReceipts = fancyReceiptsI;
    }

    /**
     * Starts this <code>Host</code> running in its own thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #run()
     * @see #stop()
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2003  INB	Created.
     *
     */
    public final void start() {
	stopped = false;
	(new Thread(this)).start();

	waitForStart();
    }

    /**
     * Stops this <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #run()
     * @see #start()
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2003  INB	Created.
     *
     */
    public final void stop() {
	stopped = true;
	if ((thread != null) && (thread != Thread.currentThread())) {
	    synchronized (this) {
		while (running) {
		    try {
			wait();
		    } catch (java.lang.InterruptedException e) {
			break;
		    }
		}
	    }
	}
    }

    /**
     * Waits for this <code>Host</code> to start running.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/10/2003  INB	Created.
     *
     */
    public final void waitForStart() {
	synchronized (this) {
	    while (!running) {
		try {
		    wait();
		} catch (java.lang.InterruptedException e) {
		    break;
		}
	    }
	}
    }

    /**
     * Stores the full user name for a client along with the message count.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/24/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2003  INB	Created.
     *
     */
    private final class FullUser {

	/**
	 * the full username.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 03/24/2003
	 */
	String fullUser = null;

	/**
	 * the message count.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 03/24/2003
	 */
	private long messageCount = 0;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.1
	 * @version 03/24/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 03/24/2003  INB	Created.
	 *
	 */
	FullUser() {
	    super();
	}

	/**
	 * Class constructor to build a <code>FullUser</code> for a name and a
	 * count.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param fullUserI the full username.
	 * @param countI    the message count.
	 * @since V2.1
	 * @version 03/24/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 03/24/2003  INB	Created.
	 *
	 */
	FullUser(String fullUserI,long messageCountI) {
	    this();
	    fullUser = fullUserI;
	    messageCount = messageCountI;
	}
    }
}
