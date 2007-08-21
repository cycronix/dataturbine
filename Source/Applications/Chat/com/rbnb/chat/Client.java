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

package com.rbnb.chat;

/**
 * Chat room client class.
 * <p>
 * This class provides the capability to chat with other people via a chat room
 * host. A client has two sink connections to the server:
 * <p><ol>
 *     <li>A connection called <it>_Chat</it> to receive messages, and</li>
 *     <li>A connection called <it>Chat</it> to send messages.</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.chat.Host
 * @since V2.0
 * @version 06/29/2004
 */

/*
 * Copyright 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/29/2004  INB	Replaced use of <code>ChannelMap.GetNodeList</code>
 *			with a <code>ChannelTree</code> and use of the
 *			<code>iterator</code> method.
 * 08/04/2003  INB	Added support for secure command responses.
 * 07/31/2003  INB	Added support for command responses.
 * 05/28/2003  INB	<code>Sink.GetServerName</code> no longer throws
 *			exceptions.
 * 04/04/2003  INB	Reset the message count when the room changes.
 * 04/02/2003  INB	Handle both fancy and non-fancy receipts.
 * 03/26/2003  INB	Added send/receive only modes.
 * 03/24/2003  INB	Added handling of return message count.
 * 03/21/2003  INB	Make sure that we've subscribed before indicating that
 *			we're running. Expect our name in the receipt.
 * 03/19/2003  INB	Added <code>abort</code> call.
 * 03/18/2003  INB	Changed the default room to "chat".
 * 01/08/2003  INB	Created.
 *
 */
public final class Client
    implements java.lang.Runnable
{

    /**
     * the address.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    private String address = "localhost:3333";

    /**
     * object to pass messages to when they are received.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    private ClientReceiverInterface cri = null;

    /**
     * the name of the chat room.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/18/2003
     */
    private String chatRoom = "chat";

    /**
     * the name of the <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    private String hostName = "host-chat";

    /**
     * the client connection name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    private String name = "chat-client";

    /**
     * the password.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private String password = null;

    /**
     * sink connection for receiving messages from the chat room
     * <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    private com.rbnb.sapi.Sink receive = null;

    /**
     * the last returned message count.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/24/2003
     */
    private long lastReturned = 0;

    /**
     * is this <code>Client</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/10/2003
     */
    private boolean running = false;

    /**
     * sink connection for sending messages to the chat room <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    private com.rbnb.sapi.Sink send = null;

    /**
     * the <code>ChannelMap</code> for sending messages.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    com.rbnb.sapi.ChannelMap sendMap = new com.rbnb.sapi.ChannelMap();

    /**
     * is this <code>Client</code> supposed to terminate?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */
    public boolean stopped = true;

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
     * use a receiver?
     * <p>
     * If this is set, then the <code>Client</code> can receive messages from
     * the <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    private boolean useReceiver = true;

    /**
     * the username.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    private String username = null;

    /**
     * use a sender?
     * <p>
     * If this is set, then the <code>Client</code> can send messages to the
     * <code>Host</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/26/2003
     */
    private boolean useSender = true;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public Client() {
	super();
    }

    /**
     * Builds a <code>Client</code> for a server address, host name, chat room,
     * client name, and receiver object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI  the server address.
     * @param hostNameI the host name (fully qualified).
     * @param chatRoomI the chat room name.
     * @param nameI     the client connection name (also the username).
     * @param passwordI the password.
     * @param criI      the receiver object.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
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
     * 03/26/2003  INB	Use the version that takes the
     *			<code>useReceiverI</code> and <code>useSenderI</code>
     *			parameters.
     * 01/08/2003  INB	Created.
     *
     */
    public Client(String addressI,
		  String hostNameI,
		  String chatRoomI,
		  String nameI,
		  String passwordI,
		  ClientReceiverInterface criI)
	throws com.rbnb.sapi.SAPIException
    {
	this(addressI,hostNameI,chatRoomI,nameI,passwordI,true,true,criI);
    }

    /**
     * Builds a <code>Client</code> for a server address, host name, chat room,
     * client name, receiver object, and whether or not a sender and/or
     * receiver connection is to be used.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI	   the server address.
     * @param hostNameI	   the host name (fully qualified).
     * @param chatRoomI	   the chat room name.
     * @param nameI	   the client connection name (also the username).
     * @param passwordI	   the password.
     * @param useSenderI   use a sender connection?
     * @param useReceiverI use a receiver connection?
     * @param criI      the receiver object.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
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
     * 03/26/2003  INB	Created from the original version w/o the
     *			<code>useSenderI</code> and <code>useReceiverI</code>
     *			parameters.
     *
     */
    public Client(String addressI,
		  String hostNameI,
		  String chatRoomI,
		  String nameI,
		  String passwordI,
		  boolean useSenderI,
		  boolean useReceiverI,
		  ClientReceiverInterface criI)
	throws com.rbnb.sapi.SAPIException
    {
	this();
	setUseSender(useSenderI);
	setUseReceiver(useReceiverI);
	setCRI(criI);
	setHostName(hostNameI);
	setChatRoom(chatRoomI);
	open(addressI,nameI,passwordI);
    }

    /**
     * Closes this <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #open(String addressI,String nameI,String passwordI)
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public final void close() {
	stop();
	if (send != null) {
	    send.CloseRBNBConnection();
	    send = null;
	}
	closeReceive();
    }

    /**
     * Closes the receive connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #openReceive()
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    private final void closeReceive() {
	if (receive != null) {
	    receive.CloseRBNBConnection();
	    receive = null;
	}
    }

    /**
     * Creates a command line receiver object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the command line receiver object.
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    private final CLReceiver createCLReceiver() {
	return (new CLReceiver());
    }

    /**
     * Gets the chat room.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the chat room.
     * @see #setChatRoom(String chatRoomI)
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public final String getChatRoom() {
	return (chatRoom);
    }

    /**
     * Gets the receiver object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the receiver object.
     * @see #setCRI(com.rbnb.chat.ClientReceiverInterface criI)
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public final ClientReceiverInterface getCRI() {
	return (cri);
    }

    /**
     * Gets the host name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the host name.
     * @see #setHostName(String hostNameI)
     * @since V2.0
     * @version 01/16/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public final String getHostName() {
	if ((hostName.charAt(0) != '/') && (getServerName() != null)) {
	    hostName = getServerName() + "/" + hostName;
	}
	return (hostName);
    }

    /**
     * Gets the connection name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the connection name.
     * @since V2.0
     * @version 01/09/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/09/2003  INB	Created.
     *
     */
    public final String getName() {
	return (name);
    }

    /**
     * Gets the connected server's name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the server's name.
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
    public final String getServerName() {
	return ((send != null) ? send.GetServerName() : null);
    }

    /**
     * Gets the flag indicating whether a receiver connection is to be made.
     * <p>
     *
     * @author Ian Brown
     *
     * @return use a receiver?
     * @see #setUseReceiver(boolean)
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created.
     *
     */
    public final boolean getUseReceiver() {
	return (useReceiver);
    }

    /**
     * Gets the flag indicating whether a sender connection is to be made.
     * <p>
     *
     * @author Ian Brown
     *
     * @return use a sender?
     * @see #setUseSender(boolean)
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created.
     *
     */
    public final boolean getUseSender() {
	return (useSender);
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
     *		       <li>-h <it>host name</it></li>
     *		       <li>-n <it>client connection name</it></li>
     *		       <li>-r <it>client room name</it></li>
     *		       <li>-R</li>
     *		       <li>-S</li>
     *		    </ul>
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Added -R and -S flags.
     * 03/18/2003  INB	Changed the default room to "chat".
     * 01/08/2003  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	Client client = null;

	try {
	    String address = "localhost:3333",
		hostName = "host-chat",
		clientName = "chat-client",
		chatRoom = "chat",
		value;
	    boolean luseReceiver = true,
		luseSender = true;
	    com.rbnb.utility.ArgHandler argH = new
		com.rbnb.utility.ArgHandler(argsI);

	    if ((value = argH.getOption('a')) != null) {
		address = value;
	    }
	    if ((value = argH.getOption('h')) != null) {
		hostName = value;
	    }
	    if ((value = argH.getOption('n')) != null) {
		clientName = value;
	    }
	    if ((value = argH.getOption('r')) != null) {
		chatRoom = value;
	    }
	    luseReceiver = !argH.checkFlag('R');
	    luseSender = !argH.checkFlag('S');

	    client = new Client();
	    client.setHostName(hostName);
	    client.setChatRoom(chatRoom);
	    client.setUseSender(luseSender);
	    client.setUseReceiver(luseReceiver);
	    if (luseReceiver) {
		client.setCRI(client.createCLReceiver());
	    }
	    client.open(address,clientName,null);
	    java.io.BufferedReader br = new java.io.BufferedReader
		(new java.io.InputStreamReader(System.in));

	    client.start();
	    if (luseSender) {
		String message;
		System.out.print("> ");
		double duration;
		while (((message = br.readLine()) != null) &&
		       message.length() > 0) {
		    if (message.startsWith("[ChatRoom=",0)) {
			chatRoom = message.substring("[ChatRoom=".length(),
						     message.lastIndexOf("]"));
			client.setChatRoom(chatRoom);

		    } else if (message.startsWith("[Refresh=",0)) {
			duration = Double.parseDouble
			    (message.substring("[Refresh=".length(),
					       message.lastIndexOf("]")));
			client.refresh(duration);

		    } else {
			client.send(message);
		    }
		    System.out.print("> ");
		}
		client.stop();
	    }

	} catch (java.lang.Exception e) {
	    if (client != null) {
		client.stop();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * Opens this <code>Client</code> on the specified server with the
     * specified connection name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the server address.
     * @param nameI     the client connection name (also the username).
     * @param passwordI the password.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
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
     * 03/26/2003  INB	Only create the desired connection(s).
     * 01/08/2003  INB	Created.
     *
     */
    public final void open(String addressI,String nameI,String passwordI)
	throws com.rbnb.sapi.SAPIException
    {
	if ((send != null) || (receive != null)) {
	    throw new java.lang.IllegalStateException
		("Client is already connected to a server.");
	}
	address = addressI;
	username = nameI;
	password = passwordI;
	if (getUseSender()) {
	    send = new com.rbnb.sapi.Sink();
	    send.OpenRBNBConnection(addressI,nameI,nameI,passwordI);
	    name = send.GetClientName();
	}
	openReceive();
    }

    /**
     * Opens the receive connection.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Only open the connection if desired.
     * 01/08/2003  INB	Created.
     *
     */
    public final void openReceive()
	throws com.rbnb.sapi.SAPIException
    {
	if (getUseReceiver()) {
	    receive = new com.rbnb.sapi.Sink();
	    receive.OpenRBNBConnection(address,"_" + name,username,password);
	}
    }

    /**
     * Refresh for a certain duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param durationI the duration of the refresh operation.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	This is a NOP if there is no sender connection or there
     *			is no <code>ClientReceiverInterface</code> object.
     * 01/08/2003  INB	Created.
     *
     */
    public final void refresh(double durationI)
	throws com.rbnb.sapi.SAPIException
    {
	if (getUseSender() && (getCRI() != null)) {
	    com.rbnb.sapi.ChannelMap refMap = new com.rbnb.sapi.ChannelMap();
	    refMap.Add(getHostName() + "/" + getChatRoom() + "/...");

	    if (durationI == Double.MAX_VALUE) {
		send.Request(refMap,-durationI/2.,durationI,"absolute");
	    } else {
		send.Request(refMap,0,durationI,"aligned");
	    }

	    refMap = send.Fetch(-1);
	    getCRI().refresh(this,refMap);
	}
    }

    /**
     * Runs this <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @see #stop()
     * @since V2.0
     * @version 03/21/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/21/2003  INB	Set the running flag after we start the subscription.
     * 03/19/2003  INB	Added <code>abort</code> call.
     * 01/08/2003  INB	Created.
     *
     */
    public final void run() {
	try {
	    thread = Thread.currentThread();

	    String lastRoom;
	    com.rbnb.sapi.ChannelMap subMap = new com.rbnb.sapi.ChannelMap();

	    while (!stopped) {
		synchronized (this) {
		    lastRoom = getChatRoom();
		    subMap.Clear();
		    subMap.Add(getHostName() + "/" + lastRoom + "/...");
		}
		receive.Subscribe(subMap);
		try {
		    Thread.currentThread().sleep(1000);
		} catch (java.lang.InterruptedException e) {
		    stopped = true;
		    synchronized (this) {
			running = true;
			notifyAll();
		    }
		    break;
		}

		if (!running) {
		    synchronized (this) {
			running = true;
			notifyAll();
		    }
		}

		com.rbnb.sapi.ChannelMap message = null;
		while (!stopped &&
		       (getChatRoom() == lastRoom) &&
		       ((message = receive.Fetch(100,message)) != null)) {
		    if (!message.GetIfFetchTimedOut()) {
			getCRI().receive(this,message);
		    }
		}

		if (!stopped) {
		    closeReceive();
		    openReceive();
		}
	    }

	} catch (com.rbnb.sapi.SAPIException e) {
	    e.printStackTrace();
	    getCRI().abort();

	} finally {
	    close();

	    synchronized (this) {
		running = false;
		notifyAll();
	    }
	}
    }

    /**
     * Receives a list of chat rooms available via the current host.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of rooms available.
     * @exception com.rbnb.sapi.SAPIException
     *		  if an error occurs while talking to the RBNB.
     * @see #receiveUsers()
     * @since V2.0
     * @version 06/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/29/2004  INB	Replaced use of <code>ChannelMap.GetNodeList</code>
     *			with a <code>ChannelTree</code> and use of the
     *			<code>iterator</code> method.
     * 03/26/2003  INB	This method returns null if there is no sender
     *			connection.
     * 01/09/2003  INB	Created.
     *
     */ 
    public final String[] receiveRooms()
	throws com.rbnb.sapi.SAPIException
    {
	String[] roomsR = null;

	if (getUseSender()) {
	    com.rbnb.sapi.ChannelMap roomMap = new com.rbnb.sapi.ChannelMap();
	    roomMap.Add(getHostName() + "/*");

	    send.RequestRegistration(roomMap);
	    roomMap = send.Fetch(-1);

	    com.rbnb.sapi.ChannelTree roomTree =
		com.rbnb.sapi.ChannelTree.createFromChannelMap(roomMap);
	    com.rbnb.sapi.ChannelTree.Node node;
	    java.util.Vector rooms = new java.util.Vector();
	    String name;
	    for (java.util.Iterator roomItr = roomTree.iterator();
		 roomItr.hasNext();
		 ) {
		node = (com.rbnb.sapi.ChannelTree.Node) roomItr.next();
		name = node.getFullName();
		if (name.length() > getHostName().length()) {
		    rooms.add(name.substring(getHostName().length() + 1));
		}
	    }

	    roomsR = new String[rooms.size()];
	    int idx = 0;
	    for (java.util.Iterator roomItr = rooms.iterator();
		 roomItr.hasNext();
		 ) {
		roomsR[idx++] = (String) roomItr.next();
	    }
	}

	return (roomsR);
    }

   /**
     * Receives a list of users in the current room.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the list of users available.
     * @exception com.rbnb.sapi.SAPIException
     *		  if an error occurs while talking to the RBNB.
     * @see #receiveRooms()
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	This method returns null if there is no sender
     *			connection.
     * 01/09/2003  INB	Created.
     *
     */
    public final String[] receiveUsers()
	throws com.rbnb.sapi.SAPIException
    {
	String[] usersR = null;

	if (getUseSender()) {
	    com.rbnb.sapi.ChannelMap userMap = new com.rbnb.sapi.ChannelMap();
	    userMap.Add(getHostName() + "/" + getChatRoom() + "/...");

	    send.RequestRegistration(userMap);
	    userMap = send.Fetch(-1);

	    usersR = userMap.GetChannelList();
	    for (int idx = 0; idx < usersR.length; ++idx) {
		usersR[idx] =
		    usersR[idx].substring(getHostName().length() + 1 +
					  getChatRoom().length() + 1);
	    }
	}

	return (usersR);
    }

    /**
     * Sends a message to the chat room.
     * <p>
     *
     * @author Ian Brown
     *
     * @param messageI the message to send.
     * @return secure connection?
     * @exception java.lang.IllegalStateException
     *		  if there is no server to talk to.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @since V2.0
     * @version 08/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/08/2005  EMF  Set MIME type of message to assist sinks.
     * 08/04/2003  INB	Allow for (secure) !DELETE responses.
     * 07/31/2003  INB	Allow for !DELETE.
     * 04/02/2003  INB	Handle the non-fancy receipts.
     * 03/26/2003  INB	If there is no sender connection, throw an exception.
     * 03/24/2003  INB	Expect a message count in the receipt. Check it against
     *			the last returned message count.
     * 03/21/2003  INB	Expect our name in the receipt.
     * 01/08/2003  INB	Created.
     *
     */
    public final boolean send(String messageI)
	throws com.rbnb.sapi.SAPIException
    {
	if (!getUseSender() || (send == null)) {
	    throw new java.lang.IllegalStateException
		("There is no sender connection for this client.");
	}

	sendMap.PutDataAsString(0,messageI);
        //EMF 8/8/05: set mime type to help plot and other sinks
        sendMap.PutMime(0,"text/plain");
	send.Request(sendMap,0,1,"newest");
	com.rbnb.sapi.ChannelMap receipt = send.Fetch(-1);
	String[] data;

	if (receipt.NumberOfChannels() != 1) {
	    throw new java.lang.IllegalStateException
		("Unable to send message to " + getHostName() + "/" +
		 getChatRoom() + ", no such host.");

	} else {
	    data = receipt.GetDataAsString(0);

	    if (data[0].equals("Received.") ||
		data[0].equals("Received (secure).")) {
		++lastReturned;

	    } else if (data[0].startsWith("Received command") ||
		       data[0].startsWith("Received (secure) command")) {

	    } else if ((!data[0].startsWith("Received #") &&
		 !data[0].startsWith("Received (secure) #")) ||
		   !data[0].endsWith("for " + name + ".")) {
		throw new java.lang.IllegalStateException
		    ("Unable to send message to " + getHostName() + "/" +
		     getChatRoom() +
		     ", there is another user by the same name!");

	    } else {
		int sharp = data[0].indexOf("#") + 1,
		    space = data[0].indexOf(" ",sharp);
		String value = data[0].substring(sharp,space);
		long currentReturned = (new Long(value)).longValue();
		if ((lastReturned != 0) &&
		    (currentReturned != (lastReturned + 1))) {
		    throw new java.lang.IllegalStateException
			("Unable to send message to " + getHostName() + "/" +
			 getChatRoom() +
			 ", returned message count out of order!");
		}
		lastReturned = currentReturned;
	    }
	}

	return (data[0].indexOf("(secure)") != -1);
    }

    /**
     * Sets the chat room.
     * <p>
     *
     * @author Ian Brown
     *
     * @param chatRoomI the name of the chat room host (fully qualified).
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an problem communicating with the RBNB.
     * @see #getHostName()
     * @since V2.0
     * @version 04/04/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2003  INB	Reset the message count.
     * 01/08/2003  INB	Created.
     *
     */
    public final void setChatRoom(String chatRoomI)
	throws com.rbnb.sapi.SAPIException
    {
	if (!chatRoom.equals(chatRoomI)) {
	    lastReturned = 0;
	}
	chatRoom = chatRoomI;
	sendMap.Clear();
	String hostPI = getHostName();
	int idx = hostPI.lastIndexOf("/") + 1;
	hostPI =
	    hostPI.substring(0,idx) +
	    "_" +
	    hostPI.substring(idx);
	sendMap.Add(hostPI + "/" + getChatRoom());
    }

    /**
     * Sets the receiver object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param criI the receiver object.
     * @see #getCRI()
     * @since V2.0
     * @version 01/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public final void setCRI(ClientReceiverInterface criI) {
	cri = criI;
    }

    /**
     * Sets the host name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param hostNameI the name of the chat room host (fully qualified).
     * @see #getHostName()
     * @since V2.0
     * @version 01/15/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2003  INB	Created.
     *
     */
    public final void setHostName(String hostNameI) {
	hostName = hostNameI;
    }

    /**
     * Sets the flag indicating whether a receiver connection is to be made.
     * <p>
     *
     * @author Ian Brown
     *
     * @param useReceiverI use a receiver?
     * @see #getUseReceiver()
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created.
     *
     */
    public final void setUseReceiver(boolean useReceiverI) {
	useReceiver = useReceiverI;
    }

    /**
     * Sets the flag indicating whether a sender connection is to be made.
     * <p>
     *
     * @author Ian Brown
     *
     * @param useSenderI use a sender?
     * @see #getUseSender()
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created.
     *
     */
    public final void setUseSender(boolean useSenderI) {
	useSender = useSenderI;
    }

    /**
     * Starts this <code>Client</code> running in its own thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #run()
     * @see #stop()
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	This method is a NOP if there is no receiver.
     * 01/08/2003  INB	Created.
     *
     */
    public final void start() {
	stopped = false;

	if (getUseReceiver()) {
	    (new Thread(this)).start();
	    waitForStart();
	}
    }

    /**
     * Stops this <code>Client</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #run()
     * @see #start()
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	This method is a NOP if there is no receiver.
     * 01/08/2003  INB	Created.
     *
     */
    public final void stop() {
	stopped = true;

	if (getUseReceiver()) {
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
    }

    /**
     * Waits for this <code>Client</code> to start running.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	This method is a NOP if there is no receiver.
     * 01/10/2003  INB	Created.
     *
     */
    public final void waitForStart() {
	if (getUseReceiver()) {
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
    }

    /**
     * Internal client receiver class for a simple command line chat client.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/19/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/19/2003  INB	Added <code>abort</code> method.
     * 01/08/2003  INB	Created.
     *
     */
    private final class CLReceiver
	implements com.rbnb.chat.ClientReceiverInterface
    {

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/08/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/08/2003  INB	Created.
	 *
	 */
	CLReceiver() {
	    super();
	}

	/**
	 * Receives notification of an abort by the host side.
	 * <p>
	 * This method is called whenever a chat <code>Client</code> detects a
	 * fatal error when trying to receive messages from the server.
	 * <p>
	 *
	 * @author Ian Brown
	 *
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
	public final void abort() {
	}

	/**
	 * Paints a message onto the display.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param messageI the message in question.
	 * @param refreshI is this a refreshed message?a
	 * @since V2.0
	 * @version 01/09/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/09/2003  INB	Created.
	 *
	 */
	private final void paint(com.rbnb.sapi.ChannelMap messageI,
				 boolean refreshI)
	{
	    String sender;
	    String message;
	    double[][] times = new double[messageI.NumberOfChannels()][];
	    int[] index = new int[messageI.NumberOfChannels()];
	    for (int idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
		times[idx] = messageI.GetTimes(idx);
		index[idx] = 0;
	    }

	    boolean done = false;
	    double time,
		ctime;
	    int cIdx;
	    while (!done) {
		done = true;
		cIdx = -1;
		time = Double.MAX_VALUE;
		for (int idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
		    if (index[idx] < times[idx].length) {
			done = false;
			ctime = times[idx][index[idx]];
			if ((cIdx == -1) || (ctime < time)) {
			    cIdx = idx;
			    time = ctime;
			}
		    }
		}

		if (!done && (cIdx >= 0)) {
		    sender = messageI.GetName(cIdx);
		    sender = sender.substring(getHostName().length() + 1 +
					      getChatRoom().length());
		    message = messageI.GetDataAsString(cIdx)[index[cIdx]];

		    System.out.println("<" +
				       com.rbnb.api.Time.since1970(time) +
				       "> <" + sender + ">");
		    System.out.println(message);
		    ++index[cIdx];
		}
	    }
	}

	/**
	 * Receives a chat message.
	 * <p>
	 * This method is called whenever a chat <code>Client</code> receives a
	 * message from the chat room <code>Host</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param clientI  the <code>Client</code>.
	 * @param messageI the message <code>ChannelMap</code>.
	 * @since V2.0
	 * @version 01/17/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/08/2003  INB	Created.
	 *
	 */
	public final void receive(Client clientI,
				  com.rbnb.sapi.ChannelMap messageI) {
	    paint(messageI,false);
	}

	/**
	 * Receives a refreshed chat message.
	 * <p>
	 * This method is called whenever a chat <code>Client</code> receives a
	 * message from the chat room <code>Host</code> when refreshing.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param clientI  the <code>Client</code>.
	 * @param messageI the message <code>ChannelMap</code>.
	 * @since V2.0
	 * @version 01/09/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/09/2003  INB	Created.
	 *
	 */
	public final void refresh(Client clientI,
				  com.rbnb.sapi.ChannelMap messageI) {
	    paint(messageI,true);
	}
    }
}
