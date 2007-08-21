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

/*
  *****************************************************************
  ***								***
  ***	Name :	rbnbVidView.java				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides a simple main program for	***
  ***	viewing video from the DataTurbine. It uses the Video	***
  ***	Retriever class of the API.				***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***	   12/12/2001 - INB					***
  ***		Added NoClassDefFoundError handling.		***
  ***								***
  *****************************************************************
*/
import com.rbnb.media.protocol.VideoRetriever;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.KeyValueHash;

//EMF 12/19/00: switch to RBNBProcess.exit from System.exit
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.RBNBProcessInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class rbnbVidView
    extends JFrame
    implements ActionListener,
	       MenuListener,
	       WindowListener {

    // Private fields:
    private boolean		live = false;

    private Sink		rbnbCon = null;

    private int			playerVisible = 0;

    private ChannelMap		rbnbMap = null,
	channelPaths = null;

    private String		lastAudio = null,
				lastVideo = null,
				lastServer = "localhost:3333",
				serverAddress = null;

    private VideoRetriever	retriever = null;

    // Private constants:
    private final static int	OPEN = 0,
	CHANNELS = 1,
	CLOSE = 2,
	EXIT = 3;

    private final static String[]	actions = {
	"Open...",
	"Channels...",
	"Close",
	"Exit"
    };

    //EMF 12/19/00: target for calling RBNBProcess.exit with
    private RBNBProcessInterface target = null;

    //EMF 12/19/00: added additional argument to constructor, this
    //              one for compatibility when running standalone
    private rbnbVidView(String[] argsI) throws Exception {
	this(argsI,null);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	rbnbVidView					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a video viewer display from	***
  ***	the input arguments.					***
  ***								***
  ***	Input :							***
  ***	   argsI		Command line arguments.		***
  ***								***
  *****************************************************************
  */
    public rbnbVidView(String[] argsI,
		       RBNBProcessInterface targetI)
	throws Exception
    {
	target = targetI;
	setSize(480,360);
	setResizable(true);
	setTitle("rbnbVidView");
	addWindowListener(this);
	createDisplayWindow();
	parseCommandLine(argsI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	actionPerformed					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called whenever the user performs an	***
  ***	action.							***
  ***								***
  ***	Input :							***
  ***	   eventI		The action event generated.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***	   12/12/2001 - INB					***
  ***		Catch the NoClassDefFoundError. This probably	***
  ***		means that JMF is not in the CLASSPATH.		***
  ***								***
  *****************************************************************
*/
    public final void actionPerformed(ActionEvent eventI) {
	try {
	    String  command = eventI.getActionCommand(),
		vmVersion = System.getProperty("java.vm.version");
	    boolean cancelled = false;

	    // Open... action: get the new DataTurbine server.
	    if (command.equals(actions[OPEN])) {
		if (retriever != null) {
		    if (--playerVisible == 0) {
			retriever.setVisible(false);
		    }
		}
		if (vmVersion.substring(0,3).equals("1.0") ||
		    vmVersion.substring(0,3).equals("1.1") ||
		    vmVersion.substring(0,3).equals("1.2")) {
		    serverAddress = (String)
			JOptionPane.showInputDialog
			(getJMenuBar(),
			 "DataTurbine Server Address (host:port)",
			 "Open Connection to DataTurbine Server",
			 JOptionPane.QUESTION_MESSAGE,
			 null,
			 null,
			 lastServer);
		    Thread.currentThread().yield();
		} else {
		    serverAddress = (String)
			JOptionPane.showInternalInputDialog
			(getJMenuBar(),
			 "DataTurbine Server Address (host:port)",
			 "Open Connection to DataTurbine Server",
			 JOptionPane.QUESTION_MESSAGE,
			 null,
			 null,
			 lastServer);
		    Thread.currentThread().yield();
		}
		try {
		    if (serverAddress != null) {
			openConnection(serverAddress);
		    } else {
			cancelled = true;
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    serverAddress = lastServer;
		    JOptionPane.showMessageDialog
			(getJMenuBar(),
			 e.getMessage(),
			 "Open DataTurbine Server Error",
			 JOptionPane.ERROR_MESSAGE,
			 null);
		}
		if (retriever != null) {
		    if ((playerVisible <= 0) && (++playerVisible == 1)) {
			retriever.setVisible(true);
		    }
		}
	    }
	 
	    // Open.../Channels... action: update the channels list.
	    if ((command.equals(actions[OPEN]) && !cancelled) ||
		command.equals(actions[CHANNELS])) {
		if (retriever != null) {
		    if (--playerVisible == 0) {
			retriever.setVisible(false);
		    }
		}
		String videoPath = null,
		    audioPath = null;
		if (vmVersion.substring(0,3).equals("1.0") ||
		    vmVersion.substring(0,3).equals("1.1") ||
		    vmVersion.substring(0,3).equals("1.2")) {
		    if ((videoPath = (String)
			 JOptionPane.showInputDialog
			 (getJMenuBar(),
			  "Video Channel Specification",
			  "Enter Channel Specification",
			  JOptionPane.QUESTION_MESSAGE,
			  null,
			  null,
			  lastVideo)) != null) {
			Thread.currentThread().yield();
			audioPath = (String)
			    JOptionPane.showInputDialog
			    (getJMenuBar(),
			     "Audio Channel Specification",
			     "Enter Channel Specification",
			     JOptionPane.QUESTION_MESSAGE,
			     null,
			     null,
			     lastAudio);
			Thread.currentThread().yield();
		    }
		} else {
		    if ((videoPath = (String)
			 JOptionPane.showInternalInputDialog
			 (getJMenuBar(),
			  "Video Channel Specification",
			  "Enter Channel Specification",
			  JOptionPane.QUESTION_MESSAGE,
			  null,
			  null,
			  lastVideo)) != null) {
			Thread.currentThread().yield();
			audioPath = (String)
			    JOptionPane.showInternalInputDialog
			    (getJMenuBar(),
			     "Audio Channel Specification",
			     "Enter Channel Specification",
			     JOptionPane.QUESTION_MESSAGE,
			     null,
			     null,
			     lastAudio);
			Thread.currentThread().yield();
		    }
		}

		try {
		    if (((videoPath != null) && !videoPath.equals("")) ||
			((audioPath != null) && !audioPath.equals(""))) {
			lastVideo = videoPath;
			lastAudio = audioPath;
			channelPaths = new ChannelMap();
			if ((videoPath != null) && !videoPath.equals("")) {
			    channelPaths.Add(videoPath);
			}
			if ((audioPath != null) && !audioPath.equals("")) {
			    channelPaths.Add(audioPath);
			}
			updateChannels();
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    JOptionPane.showMessageDialog
			(getJMenuBar(),
			 e.getMessage(),
			 "Select Audio/Video Channels Error",
			 JOptionPane.ERROR_MESSAGE,
			 null);
		}
		if (retriever != null) {
		    if ((playerVisible <= 0) && (++playerVisible == 1)) {
			retriever.setVisible(true);
		    }
		}

	    } else if (command.equals(actions[CLOSE])) {
		// Close action: close the existing connection.
		closeConnection();

	    } else if (command.equals(actions[EXIT])) {
		// Exit action: close the existing connection and exit.
		//EMF 12/19/00: use exitAction
		exitAction();
		//closeConnection();
		//System.exit(0);
	    }

	} catch (java.lang.NoClassDefFoundError e) {
	    e.printStackTrace();
	    JOptionPane.showMessageDialog
		(getJMenuBar(),
		 "A NoClassDefFoundError has been caught.\n\n" +
		 "This probably indicates that the Java Media Framework " +
		 "(JMF)\n" +
		 "library is not in the CLASSPATH. To fix this problem, " +
		 "make sure\n" +
		 "that the JMF API is installed and add the following file " +
		 "to your\n" +
		 "CLASSPATH:\n\n" +
		 "   <JMF installation directory>/lib/jmf.jar\n\n" +
		 "The JMF API can be downloaded from http://java.sun.com.",
		 "Class Definition Not Found Problem",
		 JOptionPane.ERROR_MESSAGE,
		 null);
	    exitAction();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	exitAction					***
  ***	By   :	Eric Friets	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method quits the application.  Separated from   ***
  ***	   callback code so can be stopped when running as      ***
  ***      PlugIn.                                              ***
  ***								***
  *****************************************************************
*/
    public final void exitAction() {
	setVisible(false);
	closeConnection();
	RBNBProcess.exit(0,target);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	closeConnection					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method closes any active connection.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***								***
  *****************************************************************
*/
    private final void closeConnection() {

	// Stop the video retriever.
	stopRetriever();

	// Close the connection.
	if (rbnbCon != null) {
	    rbnbCon.CloseRBNBConnection();
	    rbnbCon = null;
	}

	// Clear the current map.
	rbnbMap = null;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createDisplayWindow				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method builds the display window for the video	***
  ***	retriever.						***
  ***								***
  *****************************************************************
*/
    private final void createDisplayWindow() {
	createMenuBar();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createMenuBar					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This routine creates the menu bar for the video	***
  ***	viewer. The menu bar has a File pulldown.		***
  ***								***
  *****************************************************************
*/
    private final void createMenuBar() {

	// Create the menu bar.
	JMenuBar menuBar = new JMenuBar();

	// Create the file menu.
	JMenu menu = new JMenu("File");
	menuBar.add(menu);
	menu.addMenuListener(this);

	// Create the action buttons.
	for (int idx = 0; idx < actions.length; ++idx) {
	    JMenuItem mItem = new JMenuItem(actions[idx]);
	    menu.add(mItem);
	    mItem.addActionListener(this);
	    mItem.setActionCommand(actions[idx]);
	}

	setJMenuBar(menuBar);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	menuCanceled					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called whenever a menu is canceled.	***
  ***	It makes the video retriever visible again.		***
  ***								***
  ***	Input :							***
  ***	   eventI		The menu event.			***
  ***								***
  *****************************************************************
*/
    public final void menuCanceled(MenuEvent eventI) {
	if ((retriever != null) && (playerVisible <= 0)) {
	    if (++playerVisible == 1) {
		retriever.setVisible(true);
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	menuDeselected					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called whenever a menu is deselected.	***
  ***	It makes the video retriever visible again.		***
  ***								***
  ***	Input :							***
  ***	   eventI		The menu event.			***
  ***								***
  *****************************************************************
*/
    public final void menuDeselected(MenuEvent eventI) {
	if ((retriever != null) && (playerVisible <= 0)) {
	    if (++playerVisible == 1) {
		retriever.setVisible(true);
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	menuSelected					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called whenever a menu is selected.	***
  ***	It makes the video retriever invisible.			***
  ***								***
  ***	Input :							***
  ***	   eventI		The menu event.			***
  ***								***
  *****************************************************************
*/
    public final void menuSelected(MenuEvent eventI) {
	if (retriever != null) {
	    if (--playerVisible == 0) {
		retriever.setVisible(false);
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	openConnection					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method opens a connection to the specified	***
  ***	DataTurbine address.					***
  ***								***
  ***	Input :							***
  ***	   addressI		The DataTurbine address.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***								***
  *****************************************************************
*/
    private final void openConnection(String addressI) throws Exception {

	// Close any existing connection.
	closeConnection();

	// Open a new connection to the server.
	rbnbCon = new Sink();
	rbnbCon.OpenRBNBConnection(addressI,"rbnbVidView");
	lastServer = addressI;

	// If there is a retriever, update the connection.
	if (retriever != null) {
	    retriever.serverAddress = addressI;
	    retriever.setConnection(rbnbCon);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	parseCommandLine				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method parses the command line arguments. The	***
  ***	supported arguments are:				***
  ***								***
  ***		-a <audio path>					***
  ***		-r <dataturbine address>			***
  ***		-v <video path>					***
  ***								***
  ***	   The -r switch specifies which DataTurbine server to	***
  ***	connect to. Only one server can be specified.		***
  ***	   The -a and -v switches specify the audio and video	***
  ***	channel paths, respectively. These should each match	***
  ***	one channel of the appropriate type.			***
  ***								***
  ***	Input :							***
  ***	   argsI		The command line arguments.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***								***
  *****************************************************************
*/
    private final void parseCommandLine(String[] argsI) throws Exception {

	// Loop through the command line arguments.
	int idx1 = 0,
	    idx2;
	for (int idx = 0; idx < argsI.length; idx = idx1 + 1) {
	    if ((argsI[idx].charAt(0) != '-') &&
		(argsI[idx].charAt(0) != '/')) {
		throw new IllegalArgumentException
		    ("Unrecognized option: " + argsI[idx] + ".");
	    }

	    // Determine where the argument value is.
	    if (argsI[idx].length() >= 3) {
		idx1 = idx;
		idx2 = 2;
	    } else {
		idx1 = idx + 1;
		idx2 = 0;
		if (idx1 == argsI.length) {
		    throw new IllegalArgumentException
			("Need value for " + argsI[idx] + ".");
		}
	    }

	    // Parse the argument.
	    String argument = argsI[idx].substring(0,2),
		value = argsI[idx1].substring(idx2);

	    // DataTurbine server specification. Open the connection.
	    if (argument.equals("-r")) {
		serverAddress = value;

	    } else if (argument.equals("-a")) {
		// Audio channel.
		lastAudio = value;

	    } else if (argument.equals("-v")) {
		// Video channel.
		lastVideo = value;
	    }
	}

	// Update the channel list.
	if ((lastVideo != null) || (lastAudio != null)) {
	    channelPaths = new ChannelMap();
	    if (lastVideo != null){
		channelPaths.Add(lastVideo);
	    }
	    if (lastAudio != null){
		channelPaths.Add(lastAudio);
	    }

	} else {
	    lastVideo = "rbnbVideo/Video.jpg";
	    lastAudio = "rbnbVideo/Audio";
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	startRetriever					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts a video retriever. If necessary,	***
  ***	it creates the retriever for the first time.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***								***
  *****************************************************************
*/
    private final void startRetriever() throws Exception {

	// If things are not fully set up at this point, do nothing.
	if ((rbnbCon == null) || (rbnbMap == null)) {
	    return;
	}

	// Ensure that we have a retriever in the base state.
	if (retriever != null) {
	    retriever.terminate();
	} else {
	    retriever = new VideoRetriever();
	}

	// Set the connection and map for the retriever and start the data
	// source.
	retriever.serverAddress = serverAddress;
	retriever.setConnection(rbnbCon);
	retriever.setMap(rbnbMap);
	retriever.setContainer(getContentPane());
	playerVisible = 1;

	// Start the retriever running and force it to go to the beginning of
	// the data or real-time stream.
	retriever.start();
	if (live) {
	    //	    retriever.toggleButton(VideoRetriever.REALTIME);
	    // mjm 9/9/04:  startup in paused mode at current
	    retriever.toggleButton(VideoRetriever.ENDOFDATA);
	} else {
	    retriever.toggleButton(VideoRetriever.BEGINNINGOFDATA);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stopRetriever					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method stops the video retriever.		***
  ***								***
  *****************************************************************
*/
    private final void stopRetriever() {

	// Terminate the retriever thread if it exists.
	if (retriever != null) {
	    retriever.terminate();
	    retriever.setConnection(null);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	updateChannels					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method updates the channel list. If there is an	***
  ***	active retriever, it stops that retriever. If there is	***
  ***	an active connection, it uses that connection to get	***
  ***	a real list of channels for the channel path(s) that	***
  ***	are specified. It then starts the retriever on those	***
  ***	channels.						***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***								***
  *****************************************************************
*/
    private final void updateChannels() throws Exception {

	// Stop any active retriever.
	stopRetriever();

	// Given a connection, request a list of channels matching the current
	// path(s).
	if ((rbnbCon != null) && (channelPaths != null)) {
	    if ((retriever != null) && retriever.streaming) {
		rbnbCon.CloseRBNBConnection();
		rbnbCon.OpenRBNBConnection(serverAddress,"rbnbVidView");
	    }

	    // Get the registration for the requested channels.
	    rbnbCon.RequestRegistration(channelPaths);
	    rbnbMap = rbnbCon.Fetch(-1);

System.err.println(channelPaths);

	    // If the map is null, throw an exception.
	    if (rbnbMap == null) {
		throw new Exception
		    ("Unable to find any channels matching the specified " +
		     "channel path.");

	    } else {
		// Ensure that we have the right number and type of channels.
		boolean	    audioMatched = (lastAudio == null),
			    videoMatched = (lastVideo == null);

		for (int idx = 0; idx < rbnbMap.NumberOfChannels(); ++idx) {
		    KeyValueHash kvh = new KeyValueHash
			(rbnbMap.GetDataAsString(idx)[0].getBytes());
		    String       content = kvh.get("content");

System.err.println(rbnbMap.GetDataAsString(idx)[0]);

		    try {
			if (lastAudio.equals(rbnbMap.GetName(idx))) {
			    if (audioMatched) {
				throw new Exception
				    ("Multiple channels match audio " +
				     "specification " + lastAudio);
			    }
			    audioMatched = content.equalsIgnoreCase("Audio");
			}
		    } catch (Exception e) {
		    }

		    try {
			if (lastVideo.equals(rbnbMap.GetName(idx))) {
			    if (videoMatched) {
				throw new Exception
				    ("Multiple channels match video "+
				     "specification " + lastVideo);
			    }
			    videoMatched = content.equalsIgnoreCase("Video");
			}
		    } catch (Exception e) {
		    }
		}

		String unmatched = null;
		if (!audioMatched) {
		    //		    unmatched = "Audio channel " + lastAudio + " not found.";
		    lastAudio=null;  // mjm just ignore audio and continue
		}
		if (!videoMatched) {
		    String vunmatched =
			"Video channel " + lastVideo + " not found.";

		    if (unmatched == null) {
			unmatched = vunmatched;
		    } else {
			unmatched += "\n" + vunmatched;
		    }
		}
		if (unmatched != null) {
		    throw new Exception(unmatched);
		}
	    }

	    // If we have channels, determine whether or not there is live data
	    // and start the retriever.  For now, we simply assume live data.
	    live = true;
	    startRetriever();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowActivated					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame becomes the	***
  ***	active window. We don't really care.			***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowActivated(WindowEvent eventI) {
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowClosed					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame is disposed.	***
  ***	Ensure that the connection is closed.			***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowClosed(WindowEvent eventI) {
	//EMF 12/19/00: use exitAction()
	exitAction();
	//closeConnection();
	//System.exit(0);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowClosing					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame is closed by	***
  ***	the user. We don't really care.				***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowClosing(WindowEvent eventI) {
	//EMF 12/19/00: added code here to kill app instead of ignore event
	exitAction();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowDeactivated				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame is no longer	***
  ***	the active window. We don't really care.		***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowDeactivated(WindowEvent eventI) {
	//    dispose();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowDeiconified				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame is popped back	***
  ***	up from its icon. We don't really care.			***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowDeiconified(WindowEvent eventI) {
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowIconified					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame is iconified.	***
  ***	Force the retriever (if any) to pause since there is	***
  ***	no good reason to have it work.				***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowIconified(WindowEvent eventI) {
	retriever.pause();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	windowOpened					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the frame is opened. See	***
  ***	if there is an initial server and channels.		***
  ***								***
  ***	Input :							***
  ***	   eventI		The event.			***
  ***								***
  *****************************************************************
*/
    public final void windowOpened(WindowEvent eventI) {

	// If there is a server, connect to it.
	if (serverAddress != null) {
	    try {
		if (serverAddress != null) {
		    openConnection(serverAddress);
		}
		if (channelPaths != null) {
		    updateChannels();
		}
	    } catch (Exception e) {
		e.printStackTrace();
		if ((serverAddress != null) && (rbnbCon == null)) {
		    serverAddress = null;
		    JOptionPane.showMessageDialog
			(getJMenuBar(),
			 e.getMessage(),
			 "Open DataTurbine Server Error",
			 JOptionPane.ERROR_MESSAGE,
			 null);
		} else if ((channelPaths != null) && (rbnbMap == null)) {
		    channelPaths = null;
		    JOptionPane.showMessageDialog
			(getJMenuBar(),
			 e.getMessage(),
			 "Select Audio/Video Channels Error",
			 JOptionPane.ERROR_MESSAGE,
			 null);
		}
	    }
	}
    }

/*
  ****************************************************************
  ***								***
  ***	Name :	main						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This is the main method for the rbnbVidView class.	***
  ***	It starts an rbnbVidView object running to provide the	***
  ***	basic display.						***
  ***								***
  ***	Input :							***
  ***	   argsI		The command line arguments.	***
  ***								***
  *****************************************************************
*/
    public final static void main(String[] argsI) {
	try {
	    rbnbVidView viewer = new rbnbVidView(argsI);

	    viewer.show();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}




