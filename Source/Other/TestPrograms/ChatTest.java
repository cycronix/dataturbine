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

/**
 * Chat test application.
 * <p>
 * This program tests the Chat> code by seeing what kind of performance can be
 * had from multiple <code>Client</code> connections.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/20/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/20/2003  INB	Created.
 *
 */
public final class ChatTest
    extends java.lang.Thread
{

    /**
     * the address of the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private String address = "localhost:3333";

    /**
     * the name of the chat host.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private String chatHost = "test-chat";

    /**
     * the name of the chat room.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private String chatRoom = "test";

    /**
     * the basic chat name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private String name = "test-client";

    /**
     * the number of clients.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private int numberOfClients = 1;

    /**
     * the number of groups.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private int numberOfGroups = 1;

    /**
     * the number of rounds to run.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private int numberOfRounds = 1;

    /**
     * the odds of a particular client sending a message.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private double odds = .1;

    /**
     * the password.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private String password = null;

    /**
     * start the host?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */
    private boolean startHost = true;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/20/2003  INB	Created.
     *
     */
    ChatTest() {
	super();
    }

    /**
     * Main method.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments:
     *		    <p><ul>
     *			<li>-a <it>address</it></li>
     *			<li>-c <it>number of clients</it></li>
     *			<li>-g <it>number of groups</it></li>
     *			<li>-h <it>host name</it></li>
     *			<li>-H</li>
     *			<li>-i <it>number of iterations</it></li>
     *			<li>-n <it>client name</it></li>
     *			<li>-o <it>odds of sending a message</it></li>
     *			<li>-p <it>password</it></li>
     *			<li>-r <it>chat room</it></li>
     *		    </ul>
     * @since V2.0
     * @version 01/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/20/2003  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {
	    ChatTest cTest = new ChatTest();
	    com.rbnb.utility.ArgHandler ah =
		new com.rbnb.utility.ArgHandler(argsI);
	    String value;

	    if ((value = ah.getOption('a')) != null) {
		cTest.address = value;
	    }
	    if ((value = ah.getOption('c')) != null) {
		cTest.numberOfClients = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('g')) != null) {
		cTest.numberOfGroups = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('h')) != null) {
		cTest.chatHost = value;
	    }
	    cTest.startHost = ah.checkFlag('H');
	    if ((value = ah.getOption('i')) != null) {
		cTest.numberOfRounds = Integer.parseInt(value);
	    }
	    if ((value = ah.getOption('n')) != null) {
		cTest.name = value;
	    }
	    if ((value = ah.getOption('o')) != null) {
		cTest.odds = Double.parseDouble(value);
	    }
	    if ((value = ah.getOption('p')) != null) {
		cTest.password = value;
	    }
	    if ((value = ah.getOption('r')) != null) {
		cTest.chatRoom = value;
	    }
	    cTest.start();
	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Runs this test.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/20/2003  INB	Created.
     *
     */
    public final void run() {
	com.rbnb.chat.Host host = null;
	ChatTestClient[] clients = new ChatTestClient[numberOfClients];

	try {
	    if (startHost) {
		host = new com.rbnb.chat.Host(address,
					      chatHost,
					      password,
					      numberOfClients*numberOfRounds,
					      "None",
					      0);
		host.start();
	    }
	    java.util.Random random = new java.util.Random
		(System.currentTimeMillis());
	    for (int idx = 0; idx < numberOfClients; ++idx) {
		clients[idx] = new ChatTestClient
		    (address,
		     chatHost,
		     (chatRoom +
		      ((numberOfGroups == 1) ?
		       "" :
		       ("" + (idx % numberOfGroups)))),
		     name + (idx + 1),
		     password);
		clients[idx].start();
	    }

	    long totalElapsed  = 0,
		totalSent = 0;
	    long[] perGroup = new long[numberOfGroups];
	    for (int idx = 0; idx < numberOfRounds; ++idx) {
		int nSent = 0,
		    sent;
		long startAt = System.currentTimeMillis();
		for (int idx1 = 0; idx1 < numberOfClients; ++idx1) {
		    sent = (clients[idx1].send(odds,random) ? 1 : 0);
		    /*
		    if (sent == 1) {
			System.err.print(idx1 + " ");
		    }
		    */
		    nSent += sent;
		    perGroup[idx1 % numberOfGroups] += sent;
		}
		/*
		if (nSent != 0) {
		    System.err.println("");
		}
		*/
		totalSent += nSent;
		for (int idx1 = 0; idx1 < numberOfClients; ++idx1) {
		    clients[idx1].waitUntil(perGroup[idx1 % numberOfGroups]);
		}
		long endAt = System.currentTimeMillis(),
		    elapsed = endAt - startAt;
		totalElapsed += elapsed;
		/*
		if (nSent > 0) {
		    System.err.println(nSent +" messages sent in " +
				       (elapsed/1000.) + " sec.");
		}
		*/
	    }
	    System.err.println("Total " + totalSent + " messages sent in " +
			       (totalElapsed/1000.) + " sec.");
	    System.err.println("Rate: " + (totalSent*1000./totalElapsed) +
			       " messages/sec.");
		
	    for (int idx = 0; idx < numberOfClients; ++idx) {
		clients[idx].stop();
	    }

	} catch (java.lang.Exception e) {
	    e.printStackTrace();

	} finally {
	    if (host != null) {
		try {
		    host.stop();
		} catch (java.lang.Exception e1) {
		}
	    }
	    for (int idx = 0; idx < numberOfClients; ++idx) {
		try {
		    clients[idx].stop();
		} catch (java.lang.Exception e1) {
		}
	    }
	}

	System.exit(0);
    }

    /**
     * Chat test client handler.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/20/2003
     */

    /*
     * Copyright 2003 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/20/2003  INB	Created.
     *
     */
    private final class ChatTestClient
	implements com.rbnb.chat.ClientReceiverInterface
    {

	/**
	 * Chat <code>Client</code>
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/20/2003
	 */
	private com.rbnb.chat.Client client = null;

	/**
	 * the number of messages received.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/20/2003
	 */
	private long messagesReceived = 0;

	/**
	 * the number of messages sent by this client.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/20/2003
	 */
	private long messagesSent = 0;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param addressI  the address of the server.
	 * @param chatHostI the name of the chat host.
	 * @param chatRoomI the name of the chat room.
	 * @param nameI     the name of this client.
	 * @param passwordI the password for this client.
	 * @exception java.lang.Exception if an error occurs.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	ChatTestClient(String addressI,
		       String hostNameI,
		       String chatRoomI,
		       String nameI,
		       String passwordI)
	    throws java.lang.Exception
	{
	    super();
	    client = new com.rbnb.chat.Client(addressI,
					      hostNameI,
					      chatRoomI,
					      nameI,
					      passwordI,
					      this);
	}

	/**
	 * Gets the number of messages received.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the number of messages received.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	final long getReceived() {
	    return (messagesReceived);
	}

	/**
	 * Gets the number of messages sent.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the number of messages sent.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	final long getSent() {
	    return (messagesSent);
	}

	/**
	 * Receives a message from the host.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param clientI  the <code>Client</code> connection.
	 * @param messageI the message <code>ChannelMap</code>.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	public final synchronized void receive
	    (com.rbnb.chat.Client clientI,
	     com.rbnb.sapi.ChannelMap messageI)
	{
	    ++messagesReceived;
	    notifyAll();
	}

	/**
	 * Refreshesa message from the host.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param clientI  the <code>Client</code> connection.
	 * @param messageI the message <code>ChannelMap</code>.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	public final void refresh
	    (com.rbnb.chat.Client clientI,
	     com.rbnb.sapi.ChannelMap messageI)
	{
	}

	/**
	 * Send a message to the host (maybe).
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param oddI    the odds that the message will be sent.
	 * @param randomI the random number generator.
	 * @return was a message actually sent?
	 * @exception java.lang.Exception if an error occurs.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	final boolean send(double oddsI,java.util.Random randomI)
	    throws java.lang.Exception
	{
	    boolean sentR = false;

	    if (randomI.nextDouble() <= oddsI) {
		client.send(client.getName() +
			    " message #" + (++messagesSent));
		sentR = true;
	    }

	    return (sentR);
	}

	/**
	 * Starts this client.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @see #stop()
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	final void start() {
	    client.start();
	}

	/**
	 * Stops this client.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @see #start()
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	final void stop() {
	    client.stop();
	}

	/**
	 * Waits until the specified number of messages have been received.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param untilI the number of messages expected.
	 * @exception java.lang.Exception if an error occurs.
	 * @since V2.0
	 * @version 01/20/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/20/2003  INB	Created.
	 *
	 */
	final synchronized void waitUntil(long untilI)
	    throws java.lang.Exception
	{
	    while (getReceived() < untilI) {
		wait();
	    }
	}
    }
}
