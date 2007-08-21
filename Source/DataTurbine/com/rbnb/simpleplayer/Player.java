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

package com.rbnb.simpleplayer;

import com.rbnb.sapi.*;
import java.util.*;
import com.rbnb.utility.SortException;
import com.rbnb.utility.SortedVector;
import com.rbnb.utility.SortCompareInterface;

/**
 * General purpose class for playing out data from a V2 <bold>RBNB</bold>
 * server.
 * <p>
 * This class provides some simple controls for playing out one or more
 * channels of <bold>RBNB</bold> data in a synchronized, "real-time" fashion.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.simpleplayer.PlayerChannelListener
 * @see com.rbnb.simpleplayer.PlayerTimeListener
 * @since V2.0
 * @version 09/30/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/20/2002  INB	Created.
 *
 */
public final class Player
    extends Thread
{
    /**
     * the address of the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private String address = null;

    /**
     * the current channel map.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private ChannelMap channelMap = null;

    /**
     * the list of channels to be played.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private SortedVector channels = new SortedVector();

    /**
     * the current display duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private double duration = 0.0;

    /**
     * last display message added.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/24/2002
     */
    private Message lastDisplayMessage = null;

    /**
     * the limits for determining the position information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private double[] limits = null;

    /**
     * the messages to be processed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private Vector messages = new Vector();

    /**
     * the current reference.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private String reference = "newest";

    /**
     * the connection to the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private Sink sink = null;

    /**
     * the current start time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private double start = 0.;

    /**
     * current activity state.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private byte state = NONE;

    /**
     * the time listener.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/24/2002
     */
    private Vector timeListeners = new Vector();

    /**
     * add a channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #REMOVE
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte ADD = 1;

    /**
     * connect to the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #DISCONNECT
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte CONNECT = 2;

    /**
     * disconnect from the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #CONNECT
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte DISCONNECT = 3;

    /**
     * goto to a specified position within a range.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #MOVE
     * @since V2.0
     * @version 09/23/2002
     */
    final static byte GOTO = 4;

    /**
     * monitors the channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte MONITOR = 5;

    /**
     * moves to a specified time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #GOTO
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte MOVE = 6;

    /**
     * no command issued.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte NONE = 0;

    /**
     * pauses the player.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */
    final static byte PAUSE = 7;

    /**
     * remove a channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ADD
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte REMOVE = 8;

    /**
     * terminates the player.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    final static byte TERMINATE = 9;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    Player() {
	super();
    }

    /**
     * Builds a <code>Player</code> for the specified server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @param addressI the server address.
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public Player(String addressI) {
	this();
	address = addressI;
    }

    /**
     * Adds a channel and its listener.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI     the channel to be added.
     * @param listenerI the listener for the channel.
     * @see #remove(String nameI)
     * @see #remove(String nameI,PlayerChannelListener listenerI)
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void add(String nameI,PlayerChannelListener listenerI) {
	Channel channel = new Channel(nameI,listenerI);
	messages.addElement(new Message(ADD,channel));
	synchronized (messages) {
	    messages.notify();
	}

	Thread.currentThread().yield();
    }

    /**
     * Adds a <code>PlayerTimeListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeListenerI the <code>PlayerTimeListener</code>.
     * @see #removeTimeListener(PlayerTimeListener timeListenerI)
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/24/2002  INB	Created.
     *
     */
    public final void addTimeListener(PlayerTimeListener timeListenerI) {
	timeListeners.addElement(timeListenerI);
    }

    /**
     * Connects to the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  if the operation is interrupted.
     * @see #disconnect()
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void connect()
	throws InterruptedException
    {
	messages.addElement(new Message(CONNECT));
	synchronized (messages) {
	    messages.notify();
	}

	synchronized (this) {
	    while (sink == null) {
		wait();
	    }
	}
    }

    /**
     * Disconnects from the server.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  if the operation is interrupted.
     * @see #connect()
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void disconnect()
	throws InterruptedException
    {
	messages.addElement(new Message(DISCONNECT));
	synchronized (messages) {
	    messages.notify();
	}

	synchronized (this) {
	    while (sink != null) {
		wait();
	    }
	}
    }

    /**
     * Finds the starting time that represents the specified position within
     * the range.
     * <p>
     *
     * @author Ian Brown
     *
     * @param positionI the position.
     * @param minimumI  the minimum range value.
     * @param maximumI  the maximum range value.
     * @return the starting time.
     * @exception com.rbnb.sapi.SAPIException if an RBNB error occurs.
     * @exception java.lang.IllegalStateException if no time can be found.
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/23/2002  INB	Created.
     *
     */
    private final double findStart(int positionI,int minimumI,int maximumI)
	throws com.rbnb.sapi.SAPIException
    {
	sink.RequestRegistration(channelMap);
	ChannelMap rmap = sink.Fetch(-1);

	if ((rmap == null) || (rmap.NumberOfChannels() == 0)) {
	    throw new java.lang.IllegalStateException
		("Cannot find a time for position " + positionI);
	}

	if (limits == null) {
	    limits = new double[2];
	    limits[0] = Double.MAX_VALUE;
	    limits[1] = -Double.MAX_VALUE;
	    double sTime,
		eTime;
	    for (int idx = 0; idx < rmap.NumberOfChannels(); ++idx) {
		sTime = rmap.GetTimeStart(idx);
		eTime = sTime + rmap.GetTimeDuration(idx);
		limits[0] = Math.min(limits[0],sTime);
		limits[1] = Math.max(limits[1],eTime);
	    }
	}

	double startR = (limits[0] +
			 (((limits[1] - limits[0])*positionI)/
			  (maximumI - minimumI + 1)));
	return (startR);
    }

    /**
     * Goes to a specified position.
     * <p>
     *
     * @author Ian Brown
     *
     * @param positionI the position.
     * @param minimumI  the minimum of the range.
     * @param maximumI  the maximum of the range.
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/23/2002  INB	Created.
     *
     */
    public final void gotoPosition(int positionI,int minimumI,int maximumI) {
	synchronized (messages) {
	    if (lastDisplayMessage != null) {
		messages.removeElement(lastDisplayMessage);
	    }
	    messages.addElement
		(lastDisplayMessage = new Message(GOTO,
						  positionI,
						  minimumI,
						  maximumI));
	    messages.notify();
	}
    }

    /**
     * Starts monitoring the current channel list.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void monitor() {
	synchronized (messages) {
	    limits = null;
	    if (lastDisplayMessage != null) {
		messages.removeElement(lastDisplayMessage);
	    }
	    messages.addElement(lastDisplayMessage = new Message(MONITOR));
	    messages.notify();
	}

	Thread.currentThread().yield();
    }

    /**
     * Moves to the specified location.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startI     the starting time.
     * @param durationI  the duration to retrieve (-1. means the current).
     * @param referenceI the reference.
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void move(double startI,double durationI,String referenceI) {
	synchronized (messages) {
	    if (lastDisplayMessage != null) {
		messages.removeElement(lastDisplayMessage);
	    }
	    messages.addElement
		(lastDisplayMessage = new Message(MOVE,
						  startI,
						  durationI,
						  referenceI));
	    messages.notify();
	}

	Thread.currentThread().yield();
    }

    /**
     * Pauses the player.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/23/2002  INB	Created.
     *
     */
    public final void pause() {
	messages.addElement(new Message(PAUSE));
	synchronized (messages) {
	    messages.notify();
	}
    }

    /**
     * Performs the current activity.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem locating a channel.
     * @exception com.rbnb.sapi.SAPIException
     *		  if an <bold>RBNB</bold> error occurs.
     * @see #startActivity()
     * @see #stopActivity()
     * @since V2.0
     * @version 09/27/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    private final void performActivity()
	throws SAPIException,
	       SortException
    {
	if ((state == MONITOR) || (state == MOVE)) {
	    ChannelMap rmap = sink.Fetch(250);

	    if ((rmap != null) &&
		((rmap.NumberOfChannels() > 0) ||
		 !rmap.GetIfFetchTimedOut())) {
		post(rmap);
		if (state == MOVE) {
		    state = NONE;
		}
	    }

	} else if (state != TERMINATE) {
	    state = NONE;
	}
    }

    /**
     * Posts data to the listeners.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cmapI the channel map to post.
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem finding a channel.
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    private final void post(ChannelMap cmapI)
	throws SortException
    {
	Channel channel;

	for (int idx = 0; idx < timeListeners.size(); ++idx) {
	    ((PlayerTimeListener) timeListeners.elementAt(idx)).postTime
		(cmapI);
	}
	int idx1,
	    idx2;
	for (int idx = 0; idx < cmapI.NumberOfChannels(); ++idx) {
	    idx1 = channels.findIndex(cmapI.GetName(idx));

	    if (idx1 >= 0) {
		for (idx2 = idx1; idx2 > 0; --idx2) {
		    channel = (Channel) channels.elementAt(idx2 - 1);
		    if (!channel.name.equals(cmapI.GetName(idx))) {
			break;
		    }
		}
		for (; idx2 < channels.size(); ++idx2) {
		    channel = (Channel) channels.elementAt(idx2);
		    if (channel.name.equals(cmapI.GetName(idx))) {
			channel.listener.post(cmapI,idx);
		    } else {
			break;
		    }
		}
	    }
	}
    }

    /**
     * Removes a channel and its listener.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the channel.
     * @see #add(String nameI,PlayerChannelListener listenerI)
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void remove(String nameI) {
	remove(nameI,null);
    }

    /**
     * Removes a channel and its listener.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name of the channel.
     * @param listenerI the listener for the channel.
     * @see #add(String nameI,PlayerChannelListener listenerI)
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void remove(String nameI,PlayerChannelListener listenerI) {
	Channel channel = new Channel(nameI,listenerI);
	messages.addElement(new Message(REMOVE,channel));
	synchronized (messages) {
	    messages.notify();
	}

	Thread.currentThread().yield();
    }

    /**
     * Removes a <code>PlayerTimeListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeListenerI the <code>PlayerTimeListener</code>.
     * @see #addTimeListener(PlayerTimeListener timeListenerI)
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/24/2002  INB	Created.
     *
     */
    public final void removeTimeListener(PlayerTimeListener timeListenerI) {
	timeListeners.removeElement(timeListenerI);
    }

    /**
     * Runs this <code>Player</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void run() {
	try {
	    Message message;

	    while (state != TERMINATE) {
		synchronized (messages) {
		    while ((state == NONE) && (messages.size() == 0)) {
			messages.wait(1000);
		    }

		    if (messages.size() > 0) {
			message = (Message) messages.firstElement();
			if ((state != MOVE) ||
			    ((message.command == DISCONNECT) ||
			     (message.command == TERMINATE))) {
			    messages.removeElementAt(0);
			    if (message == lastDisplayMessage) {
				lastDisplayMessage = null;
			    }
			} else {
			    message = null;
			}
		    } else {
			message = null;
		    }
		}

		if (message != null) {
		    stopActivity();

		    switch (message.command) {
		    case ADD:
			limits = null;
			channels.add(message.channel);
			state = ADD;
			break;

		    case CONNECT:
			if (sink == null) {
			    sink = new Sink();
			    sink.OpenRBNBConnection(address,"player");
			    synchronized (this) {
				notify();
			    }
			}
			break;

		    case DISCONNECT:
		    case TERMINATE:
			limits = null;
			if (sink != null) {
			    sink.CloseRBNBConnection();
			    sink = null;
			    synchronized (this) {
				notify();
			    }
			}
			state = message.command;
			break;

		    case GOTO:
			start = findStart(message.position,
					  message.minimum,
					  message.maximum);
			reference = "absolute";
			state = MOVE;
			break;

		    case MONITOR:
			state = MONITOR;
			break;

		    case MOVE:
			start = message.start;
			if (message.duration != -1.) {
			    duration = message.duration;
			}
			reference = message.reference;
			state = MOVE;
			break;

		    case PAUSE:
			state = NONE;
			break;

		    case REMOVE:
			limits = null;
			if (message.channel.listener != null) {
			    channels.remove(message.channel);
			} else {
			    Channel channel;
			    while ((channel = (Channel)
				    channels.find(message.channel)) != null) {
				channels.remove(channel);
			    }
			}
			state = REMOVE;
			break;
		    }

		    startActivity();
		}

		performActivity();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    if (sink != null) {
		sink.CloseRBNBConnection();
		sink = null;
	    }
	    state = NONE;
	}
    }

    /**
     * Starts the current activity.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.utility.SortException
     *		  if there is a problem adding a channel.
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an <bold>RBNB</bold> exception.
     * @see #performActivity()
     * @see #stopActivity()
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    private final void startActivity()
	throws SAPIException,
	       SortException
    {
	Channel channel;

	switch (state) {
	case ADD:
	case REMOVE:
	    state = NONE;
	    break;

	case MONITOR:
	    if ((channels.size() == 0) ||
		(sink == null)) {
		state = NONE;
	    } else {
		channelMap = new ChannelMap();
		for (int idx = 0; idx < channels.size(); ++idx) {
		    channel = (Channel) channels.elementAt(idx);
		    channelMap.Add(channel.name);
		}
		sink.Monitor(channelMap,1);
	    }
	    break;

	case MOVE:
	    if ((channels.size() == 0) ||
		(sink == null)) {
		state = NONE;
	    } else {
		channelMap = new ChannelMap();
		for (int idx = 0; idx < channels.size(); ++idx) {
		    channel = (Channel) channels.elementAt(idx);
		    channelMap.Add(channel.name);
		}
		sink.Request(channelMap,start,duration,reference);
	    }
	    break;
	}
    }

    /**
     * Stops the current activity.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.sapi.SAPIException
     *		  if there is an <bold>RBNB</bold> exception.
     * @see #performActivity
     * @see #startActivity()
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    private final void stopActivity()
	throws SAPIException
    {
	if (state == MONITOR) {
	    sink.CloseRBNBConnection();
	    sink = null;
	    sink = new Sink();
	    sink.OpenRBNBConnection(address,"player");
	    state = NONE;
	}

	state = NONE;
    }

    /**
     * Terminates this player.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.InterruptedException
     *		  if the operation is interrupted.
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    public final void terminate()
	throws InterruptedException
    {
	synchronized (messages) {
	    messages.removeAllElements();
	    messages.addElement(new Message(TERMINATE));
	    messages.notify();
	}
    }

    /**
     * Internal channel class representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/24/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    private final class Channel
	implements SortCompareInterface
    {
	/**
	 * the listener for the channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	PlayerChannelListener listener = null;

	/**
	 * the name of the channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	String name = null;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	Channel() {
	    super();
	}

	/**
	 * Builds a <code>Channel</code> for the specified name and listener.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param nameI     the name of the channel.
	 * @param listenerI the listener for the channel.
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	Channel(String nameI,PlayerChannelListener listenerI) {
	    this();
	    name = nameI;
	    listener = listenerI;
	}

	/**
	 * Compares the input sort value to this one.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param identifierI the sort field identifier.
	 * @param otherI      the other object to compare to.
	 * @return the difference between the two objects, 0 is equal.
	 * @since V2.0
	 * @version 09/24/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	public final int compareTo(Object identifierI,Object otherI) {
	    String otherName;
	    int hashDiff = 0;

	    if (otherI instanceof String) {
		otherName = (String) otherI;
	    } else {
		Channel other = (Channel) otherI;
		otherName = other.name;

		if (other.listener != null) {
		    hashDiff = listener.hashCode() - other.listener.hashCode();
		}
	    }

	    int diffR = name.compareTo(otherName);
	    if (diffR == 0) {
		diffR = hashDiff;
	    }
	    return (diffR);
	}

	/**
	 * Returns the value of the sort field for this <code>Channel</code>.
	 * <p>
	 * The sort field is the name.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param identifierI the sort field identifier - ignored.
	 * @return the sort field.
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	public final Object sortField(Object identifierI) {
	    return (name);
	}
    }

    /**
     * Message class for player.
     * <p>
     * This class represents messages to the player.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2002  INB	Created.
     *
     */
    private final class Message {
	/**
	 * a changed channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	Channel channel = null;

	/**
	 * the command.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	byte command = NONE;

	/**
	 * the new duration (< 0 means leave unchanged).
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	double duration = -1.;

	/**
	 * the maximum range value.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/23/2002
	 */
	int maximum = -1;

	/**
	 * the minimum range value.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/23/2002
	 */
	int minimum = -1;

	/**
	 * the position.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/23/2002
	 */
	int position = -1;

	/**
	 * the reference.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	String reference = null;

	/**
	 * the new start time.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	double start = 0.;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	Message() {
	    super();
	}

	/**
	 * Builds a <code>Message</code> for a command.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param commandI the command.
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	Message(byte commandI) {
	    this();
	    command = commandI;
	}

	/**
	 * Builds a <code>Message</code> for a changed channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param commandI the command - must be <code>ADD</code> or
	 *		   <code>REMOVE</code>.
	 * @param channelI the <code>Channel</code>.
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	Message(byte commandI,Channel channelI) {
	    this();
	    command = commandI;
	    channel = channelI;
	}

	/**
	 * Builds a <code>Message</code> to move to a specific location.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param commandI   the command (should be MOVE).
	 * @param startI     the start time.
	 * @param durationI  the duration of the request (-1 means use previous
	 *		     duration).
	 * @param referenceI the request reference ("absolute", "newest", or
	 *		     "oldest").
	 * @since V2.0
	 * @version 09/20/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	Message(byte commandI,
		double startI,
		double durationI,
		String referenceI)
	{
	    this();
	    command = commandI;
	    start = startI;
	    duration = durationI;
	    reference = referenceI;
	}

	/**
	 * Builds a <code>Message</code> to go to the specified position within
	 * a relative range.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param commandI  the command (should be GOTO).
	 * @param positionI the position.
	 * @param minimumI  the minimum of the range.
	 * @param maximumI  the maximum of the range.
	 * @since V2.0
	 * @version 09/23/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/23/2002  INB	Created.
	 *
	 */
	Message(byte commandI,
		int positionI,
		int minimumI,
		int maximumI)
	{
	    this();
	    command = commandI;
	    position = positionI;
	    minimum = minimumI;
	    maximum = maximumI;
	}

	/**
	 * Converts this <code>Message</code> to a string.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the string representation.
	 * @since V2.0
	 * @version 09/23/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/20/2002  INB	Created.
	 *
	 */
	public final String toString() {
	    String stringR = "";

	    switch (command) {
	    case ADD:
		stringR = "Add " + channel.name;
		break;

	    case CONNECT:
		stringR = "Connect";
		break;

	    case DISCONNECT:
		stringR = "Disconnect";
		break;

	    case GOTO:
		stringR = ("Goto " + position +
			   " (" + minimum +
			   "-" + maximum + ")");
		break;

	    case MONITOR:
		stringR = "Monitor";
		break;

	    case MOVE:
		stringR = ("Move to " + start +
			   "+" + duration +
			   " @" + reference);
		break;

	    case REMOVE:
		stringR = "Remove " + channel.name;
		break;

	    case TERMINATE:
		stringR = "Terminate";
		break;
	    }

	    return (stringR);
	}
    }
}
