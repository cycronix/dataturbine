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
 * A ServerHandler DataTurbine source that logs <code>ServerHandler</code>
 * messages.
 * <p>
 * <code>Log</code> is an implementation of the <code>RBO</code> class that
 * provides message logging capabilities within a DataTurbine.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/23/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/07/2004  INB	Added <code>LogDisplay</code> to ensure that the
 *			server doesn't block on message output.
 * 04/04/2003  INB	Added <code>addError</code> method.
 * 04/02/2003  INB	Allow for a "non-runnable" log.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 03/21/2003  INB	Added <code>CLASS_RBNB_SHORTCUT</code>. Synchronized
 *			<code>addMessage</code>.
 * 02/18/2003  INB	Modified to deal with the multiple
 *			<code>RingBuffers</code> structure.
 * 01/11/2001  INB	Created.
 *
 *
 */
final class Log
    extends com.rbnb.api.RBO
{
    /**
     * classes to display.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    private long classes = NONE;

    /**
     * is this runnable?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/02/2003
     */
    private boolean isRunnable = true;

    /**
     * level to display.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
     private byte level = STANDARD;

    /**
     * the <code>LogDisplay</code> object to use to display messages.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/07/2004
     */
    private LogDisplay logDisplay = null;

    // Package constants:
    /**
     * all classes.
     * <p>
     * Messages belonging to all classes are shown.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #NONE
     * @since V2.0
     * @version 01/11/2002
     */
    final static long ALL = 0xffffffffffffffffL;

    /**
     * <code>Archive</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_ARCHIVE = 0x0000000000000001L;

    /**
     * <code>ChildServer</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_CHILD_SERVER = 0x0000000000000002L;

    /**
     * <code>ConnectedServer</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_CONNECTED_SERVER = 0x0000000000000004L;

    /**
     * <code>MirrorController</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_MIRROR_CONTROLLER = 0x0000000000000008L;

    /**
     * <code>NBO</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_NBO = 0x0000000000000010L;

    /**
     * <code>ParentServer</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_PARENT_SERVER = 0x0000000000000020L;

    /**
     * <code>PeerServer</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_PEER_SERVER = 0x0000000000000040L;

    /**
     * <code>RBNB</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RBNB = 0x0000000000000080L;

    /**
     * <code>RBNBClient</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RBNB_CLIENT = 0x0000000000000100L;

    /**
     * <code>RBNBController</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RBNB_CONTROLLER = 0x0000000000000200L;

    /**
     * <code>RBNBRouter</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RBNB_ROUTER = 0x0000000000000400L;

    /**
     * <code>RBNBRoutingMap</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RBNB_ROUTING_MAP = 0x0000000000000800L;

    /**
     * <code>RBO</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RBO = 0x0000000000001000L;

    /**
     * <code>RCO</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_RCO = 0x0000000000002000L;

    /**
     * <code>RemoteClientHandler/code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_REMOTE_CLIENT_HANDLER = 0x0000000000004000L;

    /**
     * <code>RemoteServer</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_REMOTE_SERVER = 0x0000000000008000L;

    /**
     * <code>RouterHandle</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static long CLASS_ROUTER_HANDLE = 0x0000000000010000L;

    /**
     * <code>RBNBPlugIn</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2002
     */
    final static long CLASS_RBNB_PLUGIN = 0x0000000000020000L;

    /**
     * <code>RBNBShortcut</code> class mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/21/2003
     */
    final static long CLASS_RBNB_SHORTCUT = 0x0000000000040000L;

    /**
     * no classes.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #ALL
     * @since V2.0
     * @version 01/11/2002
     */
    final static long NONE = 0x0000000000000000L;

    /**
     * standard level messages.
     * <p>
     * Messages at the <code>STANDARD</code> level are always displayed,
     * regardless of the class that they belong to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/11/2002
     */
    final static byte STANDARD = (byte) 0;

    // Private constants:
    private final static String LINESEPARATOR =
	System.getProperty("line.separator");


    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    Log()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super();
	setName("_Log");
    }

    /**
     * Adds an error to the log.
     * <p>
     * Errors are always logged.
     * <p>
     *
     * @author Ian Brown
     *
     * @param levelI the level of the message.
     *		     <p>
     *		     This is here for compatibility with the other
     *		     <code>addXXX</code> methods. It is ignored.
     *		     <p>
     * @param classI the class sending the message.
     * @param nameI  the name of <code>Rmap</code> hierarchy to contain
     *		     the message.
     * @param errorI the error to add.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the creation is interrupted.
     * @since V2.0
     * @version 04/29/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2003  INB	Created from <code>addException</code>.
     *
     */
    final void addError(int levelI,
			long classI,
			String nameI,
			java.lang.Error errorI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.io.ByteArrayOutputStream baos =
	    new java.io.ByteArrayOutputStream();
	java.io.PrintWriter pw = new java.io.PrintWriter(baos);

	// Write the error out to the print writer stream.
	errorI.printStackTrace(pw);
	pw.flush();

	// Convert the output to a string.
	String message = new String(baos.toByteArray());
	if (message.indexOf("\n") == message.lastIndexOf("\n")) {
	    pw.close();
	    java.lang.Exception exception =
		new java.lang.Exception
		    ("Traceback (may not reflect location of error)");
	    
	    baos = new java.io.ByteArrayOutputStream();
	    pw = new java.io.PrintWriter(baos);
	    exception.printStackTrace(pw);
	    pw.flush();
	    message += "\n" + new String(baos.toByteArray());
	}

	// Close things.
	pw.close();

	// Add the message to the log.
	addMessage(0,classI,nameI,message);
    }

    /**
     * Adds an exception to the log.
     * <p>
     * The exception is only added if:
     * <p><ul>
     * <li>it is at the <code>STANDARD</code> level, or</li>
     * <li>it is at or below the maximum level allowed and belongs to one of
     *	   the classes being shown.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param levelI	 the level of the message.
     * @param classI	 the class sending the message.
     * @param nameI	 the name of <code>Rmap</code> hierarchy to contain
     *			 the message.
     * @param exceptionI the exception to add.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the creation is interrupted.
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    final void addException(int levelI,
			    long classI,
			    String nameI,
			    java.lang.Exception exceptionI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	java.io.ByteArrayOutputStream baos =
	    new java.io.ByteArrayOutputStream();
	java.io.PrintWriter pw = new java.io.PrintWriter(baos);

	// Write the exception out to the print writer stream.
	exceptionI.printStackTrace(pw);
	pw.flush();

	// Convert the output to a string.
	String message = new String(baos.toByteArray());

	// Close things.
	pw.close();

	// Add the message to the log.
	addMessage(levelI,classI,nameI,message);
    }

    /**
     * Adds a message to the log.
     * <p>
     *
     * @author Ian Brown
     *
     * The message is only added if:
     * <p><ul>
     * <li>it is at the <code>STANDARD</code> level, or</li>
     * <li>it is at or below the maximum level allowed and belongs to one of
     *	   the classes being shown.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param levelI   the level of the message.
     * @param classI   the class sending the message.
     * @param nameI    the name of <code>Rmap</code> hierarchy to contain the
     *		       message.
     * @param messageI the message.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the creation is interrupted.
     * @since V2.0
     * @version 01/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2004  INB	Use the <code>LogDisplay</code> to display the message.
     * 04/02/2003  INB	If the log isn't runnable, then simply display the
     *			message.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 03/21/2003  INB	Made this method synchronized.
     * 02/18/2003  INB	Modified to deal with the multiple
     *			<code>RingBuffers</code> structure.
     * 01/11/2001  INB	Created.
     *
     */
    final synchronized void addMessage(int levelI,
			  long classesI,
			  String nameI,
			  String messageI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
//	if (!getRunnable()) return;		// MJM debug - try to stop memory leaks
	    
	if ((levelI <= getLevel()) &&
	    ((levelI == STANDARD) ||
	     ((getClasses() & classesI) == classesI))) {
	    // Create the frame <code>Rmap</code> based on the name.
	    Rmap lFrame = Rmap.createFromName(nameI);

	    // Now reparent one higher.
	    Rmap rmap = new Rmap(null,
				 null,
				 new TimeRange
				     (System.currentTimeMillis()/1000.,
				      0.));
	    rmap.addChild(lFrame);
	    lFrame = rmap;

	    // We'll add the messages to the <code>Rmap</code> at the bottom of
	    // the frame.
	    Rmap bottom = lFrame.moveToBottom();
	    String time = Time.since1970(lFrame.getTrange().getTime());
	    while (time.length() < 28) {	// could we be any less efficient?
		time += " ";
	    }
	    String message = "<" + time  + "> <" + nameI + ">\n" + messageI;
	    if (message.lastIndexOf(LINESEPARATOR) !=
		message.length() - LINESEPARATOR.length()) {
		message += LINESEPARATOR;
	    }

	    DataBlock dBlock = 
		(new DataBlock
		    (message,
		     1,
		     message.length(),
		     DataBlock.TYPE_STRING,
		     DataBlock.ORDER_MSB,
		     true,
		     0,
		     message.length()));
	    bottom.setDblock(dBlock);

	    if (!getRunnable()) {
		synchronized (this) {
		    if (logDisplay == null) {
			logDisplay = new LogDisplay();
			logDisplay.start();
		    }
		    logDisplay.addMessage(lFrame);
		    return;		// MJM 10-11/10 don't lumber on and add it anyways!!!!!
		}

	    } else {
		// Wait until the <code>Cache</code> exists.
		while ((cFrameSets == 0) && (cFrFrameSet == 0)) {
		    wait(TimerPeriod.NORMAL_WAIT);
		}

		// Add the frame.
		addChild(lFrame);
	    }
	}
    }

    /**
     * Displays a frame <code>Rmap</code> on <code>System.err</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameI  the frame <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception java.lang.InterruptedException
     *		  thrown if the display operation is interrupted.
     * @since V2.0
     * @version 01/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2004  INB	Eliminated synchronization as it isn't needed with
     *			the new <code>LogDisplay</code> capabililty.
     * 01/11/2001  INB	Created.
     *
     */
    private final void displayFrame(Rmap frameI)
	throws com.rbnb.api.AddressException,
	       java.lang.InterruptedException
    {
	try {
	    // Get a list of the data <code>Rmaps</code> in the frame.
	    String[] names = frameI.extractNames();

	    // Now, extract the time and data information for all of them.
	    DataArray[] td = new DataArray[names.length];
	    int indexes[] = new int[names.length];

	    for (int idx = 0; idx < names.length; ++idx) {
		indexes[idx] = 0;
		try {
		    td[idx] = frameI.extract(names[idx]);
		} catch (java.lang.IllegalStateException e) {
		    td[idx] = null;
		}
	    }

	    // Display the messages in order.
	    boolean done = false;
	    int last = -1;
	    String lastHeader = null;

	    do {
		int oldest = -1;

		for (int idx = 0; idx < names.length; ++idx) {
		    // Locate the oldest entry that still needs to be
		    // processed.
		    if ((td[idx] != null) &&
			(td[idx].getData() instanceof String[]) &&
			(indexes[idx] <
			 ((String[]) td[idx].getData()).length) &&
			((oldest == -1) ||
			 (td[idx].getTime()[indexes[idx]] <
			  td[oldest].getTime()[indexes[oldest]]))) {
			// If there is a message that is of a valid type
			// (String) and it is older than any other unprocessed
			// one we've seen so far, then it is the oldest.
			oldest = idx;
		    }
		}

		if (!(done = (oldest == -1))) {
		    // Now display the message itself.
		    String message =
			((String[]) td[oldest].getData())[indexes[oldest]];
		    message = message.substring
			(0,
			 message.length() - LINESEPARATOR.length());
		    java.util.StringTokenizer sTok =
			new java.util.StringTokenizer
			    (message,
			     LINESEPARATOR);
		    String token;
		    while (sTok.hasMoreTokens()) {
			token = sTok.nextToken();
			if (token.charAt(0) == '<') {
			    if ((lastHeader == null) ||
				!token.equals(lastHeader)) {
				System.err.println(token);
			    }
			    token = lastHeader;
			} else {
			    System.err.println("   " + token);
			}
		    }

		    // Move on to the next message for this name.
		    ++indexes[oldest];
		}
		
	    } while (!done);

	} catch (SerializeException e) {
	} catch (java.io.IOException e) {
	}
    }

    /**
     * Gets the current display class mask.
     * <p>
     * Messages for classes whose values are in this mask are displayed if they
     * are at a valid level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #ALL
     * @see #getLevel()
     * @see #NONE
     * @see #setClasses(long)
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
    final long getClasses() {
	return (classes);
    }

    /**
     * Gets the current display level for messages going to the log.
     * <p>
     * Messages at or below this level are displayed if they belong to a class
     * that can be displayed. <code>STANDARD</code> messages are always
     * displayed.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level.
     * @see #getClasses()
     * @see #setLevel(byte)
     * @see #STANDARD
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
    final byte getLevel() {
	return (level);
    }

    /**
     * Gets the runnable flag.
     * <p>
     * A <code>Log</code> is runnable if there is a cache or archive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this <code>Log</code> runnable?
     * @see #setRunnable(boolean)
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
    public final boolean getRunnable() {
	return (isRunnable);
    }

    /**
     * Handles a new frame <code>Rmap</code>.
     * <p>
     * This method is called whenever a frame <code>Rmap</code> is accepted by
     * the <code>RBO</code>. It displays the frame.
     * <p>
     *
     * @author Ian Brown
     *
     * @param theFrameI  the frame to handle.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception java.lang.InterruptedException
     *		  thrown if the display operation is interrupted.
     * @since V2.0
     * @version 01/23/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2004  INB	Use the <code>LogDisplay</code>.
     * 02/27/2001  INB	Created.
     *
     */
    final void handleNewFrame(Rmap theFrameI)
	throws com.rbnb.api.AddressException,
	       java.lang.InterruptedException
    {
	super.handleNewFrame(theFrameI);

	// Display the message via the <code>LogDisplay</code>.
	synchronized (this) {
	    if (logDisplay == null) {
		logDisplay = new LogDisplay();
		logDisplay.start();
	    }
	}
	if (logDisplay != null) {
	    logDisplay.addMessage(theFrameI);
	}
    }

    /**
     * Sets the current display class mask.
     * <p>
     * Messages for classes whose values are in this mask are displayed if they
     * are at a valid level.
     * <p>
     *
     * @author Ian Brown
     *
     * @param classesI the class mask.
     * @see #ALL
     * @see #getClasses()
     * @see #NONE
     * @see #setLevel(byte)
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
    final void setClasses(long classesI) {
	classes = classesI;
    }

    /**
     * Sets the current display level for messages going to the log.
     * <p>
     * Messages at or below this level are displayed if they belong to a class
     * that can be displayed. <code>STANDARD</code> messages are always
     * displayed.
     * <p>
     *
     * @author Ian Brown
     *
     * @param levelI the level.
     * @see #getLevel()
     * @see #setClasses(long)
     * @see #STANDARD
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
    final void setLevel(byte levelI) {
	level = levelI;
    }

    /**
     * Sets the runnable flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isRunnableI is the <code>Log</code> runnable?
     * @see #getRunnable()
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
    public final void setRunnable(boolean isRunnableI) {
	isRunnable = isRunnableI;
    }

    /**
     * Shutdown the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/07/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2004  INB	Created.
     *
     */
    final void shutdown() {
	synchronized (this) {
	    if (logDisplay != null) {
		logDisplay.interrupt();
		logDisplay = null;
	    }
	}
    }

    /**
     * Displays <code>Log</code> messages in an asynchronous fashion to ensure
     * that the server does not lock up if the output of messages is blocked.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/15/2004
     */

    /*
     * Copyright 2004 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/07/2004  INB	Created.
     *
     */
    private final class LogDisplay
	extends java.lang.Thread
    {
	/**
	 * the index of the last message added.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/07/2004
	 */
	private int lastMessage = -1;

	/**
	 * maximum number of messages.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/07/2004
	 */
	private final static int MAX_MESSAGES = 10;

	/**
	 * list of messages held for display.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/07/2004
	 */
	private Rmap[] messages = new Rmap[MAX_MESSAGES];

	/**
	 * the number of messages added.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/07/2004
	 */
	private int numMessages = 0;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/07/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/07/2004  INB	Created.
	 *
	 */
	LogDisplay() {
	    super("LogDisplay");
	    setDaemon(true);
	}

	/**
	 * Adds a message.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param messageI the message.
	 * @see #waitForMessage()
	 * @since V2.2
	 * @version 01/07/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/07/2004  INB	Created.
	 *
	 */
	public final void addMessage(Rmap messageI) {
	    synchronized (messages) {
		lastMessage = (lastMessage + 1) % MAX_MESSAGES;
		numMessages = Math.min(numMessages + 1,MAX_MESSAGES);
		messages[lastMessage] = messageI;
	    }
	}

	/**
	 * Runs the message display thread.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.2
	 * @version 01/07/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/07/2004  INB	Created.
	 *
	 */
	public final void run() {
	    try {
		Rmap message;

		while (true) {
		    message = waitForMessage();
		    try {
			displayFrame(message);
		    } catch (com.rbnb.api.AddressException e) {
		    }
		}

	    } catch (java.lang.InterruptedException e) {
	    }
	}

	/**
	 * Waits for a message to be displayed.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @return the message to display.
	 * @see #addMessage(com.rbnb.api.Rmap messageI)
	 * @throws java.lang.InterruptedException
	 *	   thrown if this method is interrupted.
	 * @since V2.2
	 * @version 01/07/2004
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/07/2004  INB	Created.
	 *
	 */
	public final Rmap waitForMessage()
	    throws java.lang.InterruptedException
	{
	    int messageToGet;
	    Rmap messageR;
	    synchronized (messages) {
		while (numMessages == 0) {
		    messages.wait(TimerPeriod.NORMAL_WAIT);
		}

		messageToGet = ((MAX_MESSAGES +
				 lastMessage -
				 (numMessages - 1)) %
				MAX_MESSAGES);
		messageR = messages[messageToGet];
		--numMessages;
	    }

	    return (messageR);
	}
    }
}
