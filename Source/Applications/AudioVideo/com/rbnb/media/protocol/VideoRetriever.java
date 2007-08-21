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
  ***	Name :	VideoRetriever.java				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001, 2002, 2004 Creare Inc.		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This file contains the VideoRetriever class for the	***
  ***	DataTurbine API. It provides a simple set of standard	***
  ***	controls to use to retrieve video data from the		***
  ***	DataTurbine and passes the results on to a JMF		***
  ***	player for display purposes.				***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***	   04/01/2002 - INB					***
  ***		The times may be absolute seconds since 1970	***
  ***		rather than relative; adjust for that.		***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
package com.rbnb.media.protocol;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.SAPIException;

import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.SortCompareInterface;
import com.rbnb.utility.SortedVector;
import com.rbnb.utility.SortException;
import com.rbnb.utility.Utility;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.media.Buffer;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.StartEvent;
import javax.media.StopEvent;

import javax.media.bean.playerbean.MediaPlayer;

import javax.media.format.AudioFormat;
import javax.media.format.H261Format;
import javax.media.format.H263Format;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VideoRetriever
    implements ControllerListener,
	       Runnable
{
    public boolean 		streaming = false;
    public String		serverAddress = null;

    // Private fields:
    private boolean		ready = false,
				movingSlider = false,
				trackingSlider = false;

    private Box			mediaBox = null;

    private Sink		rbnbCon = null;
    Sink			rbnbAddCon = null;

    private int			state = NOREQUEST;

    private JPanel		mainPanel = null;

    private Image		images[][] = null;

    private JSlider		positionSlider = null;

    private JRadioButton	buttons[] = null;

    private ChannelMap		rbnbMap = null,
				primaryMap = null;

    private MediaPlayer		mediaPlayer = null;

    DataSource			dataSource = null;

    private JTextField		frameRateText = null,
	timeText = null;

    private Thread		myThread = null;

    private double		minimumFrame = -Double.MAX_VALUE,
				maximumFrame = -Double.MAX_VALUE,
				currentFrame = -Double.MAX_VALUE,
				lastFrameRate = 0.;

    volatile VideoRequest	currentRequest = null,
				nextRequest = null;

    // Public constants:
    public final static int	BEGINNINGOFDATA = 0,
				PLAYBACKWARD = 1,
				STEPBACKWARD = 2,
				PAUSE = 3,
				STEPFORWARD = 4,
				PLAYFORWARD = 5,
				ENDOFDATA = 6,
				REALTIME = 7;

    // Package constants:
    protected final static String	actions[] = {
	"BOD",
	"RPLAY",
	"RSTEP",
	"PAUSE",
	"STEP",
	"PLAY",
	"EOD",
	"RT"
    };

    protected final static int	NOREQUEST = 0,
				REALIZING = 1,
				PREFETCHING = 2,
				STARTING = 3,
				RUNNING = 4,
				BUTTONS = 8,
				DISABLED = 0,
				OFF = 1,
				ON = 2;

    // Private constants:
    private final static String[][]
				imageFiles = {
	    { "Video/bofIns.jpg",   "Video/bof.jpg",   "Video/bofOn.jpg" },
	    { "Video/rplayIns.jpg", "Video/rplay.jpg", "Video/rplayOn.jpg" },
	    { "Video/rstepIns.jpg", "Video/rstep.jpg", "Video/rstepOn.jpg" },
	    { "Video/stopIns.jpg",  "Video/stop.jpg",  "Video/stopOn.jpg" },
	    { "Video/stepIns.jpg",  "Video/step.jpg",  "Video/stepOn.jpg" },
	    { "Video/playIns.jpg",  "Video/play.jpg",  "Video/playOn.jpg" },
	    { "Video/eofIns.jpg",   "Video/eof.jpg",   "Video/eofOn.jpg" },
	    { "Video/rtIns.jpg",    "Video/rt.jpg",    "Video/rtOn.jpg" }
/*
	    { "/Video/bofIns.jpg",   "/Video/bof.jpg",   "/Video/bofOn.jpg" },
	    { "/Video/rplayIns.jpg", "/Video/rplay.jpg", "/Video/rplayOn.jpg" },
	    { "/Video/rstepIns.jpg", "/Video/rstep.jpg", "/Video/rstepOn.jpg" },
	    { "/Video/stopIns.jpg",  "/Video/stop.jpg",  "/Video/stopOn.jpg" },
	    { "/Video/stepIns.jpg",  "/Video/step.jpg",  "/Video/stepOn.jpg" },
	    { "/Video/playIns.jpg",  "/Video/play.jpg",  "/Video/playOn.jpg" },
	    { "/Video/eofIns.jpg",   "/Video/eof.jpg",   "/Video/eofOn.jpg" },
	    { "/Video/rtIns.jpg",    "/Video/rt.jpg",    "/Video/rtOn.jpg" }
*/

	};

    
/*
  *****************************************************************
  ***								***
  ***	Name :	VideoRetriever					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This is the default constructor for a video		***
  ***	retriever.						***
  ***								***
  *****************************************************************
*/
    public VideoRetriever() {}

/*
  *****************************************************************
  ***								***
  ***	Name :	beginningOfData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method moves to the start of the data.		***
  ***								***
  ***	Input :							***
  ***	   trackingSliderI	Is this tracking the slider?	***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void beginningOfData(boolean trackingSliderI) {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.ONEFRAME,
	     VideoRequest.BEGINNINGOFDATA);
	lRequest.setTrackingSlider(trackingSliderI);
	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	controllerUpdate				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called when the controller (media	***
  ***	player) produces an event. It may perform some		***
  ***	specific processing as result of the event.		***
  ***								***
  ***	Input :							***
  ***	   eventI		The event to handle.		***
  ***								***
  *****************************************************************
*/
    public final void controllerUpdate(ControllerEvent eventI) {
	// On a realize complete event, set up the media player.
	if (eventI instanceof RealizeCompleteEvent) {
	    setupMediaPlayer();

	    // On a prefetch complete event, start the media player.
	} else if (eventI instanceof PrefetchCompleteEvent) {
	    startMediaPlayer();

	    // On a start event, we're running.
	} else if (eventI instanceof StartEvent) {
	    synchronized (this) {
		state = RUNNING;
		notifyAll();
	    }

	    // On a stop event, we're done.
	} else if (eventI instanceof StopEvent) {
	    synchronized (this) {
		state = NOREQUEST;
		notifyAll();
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createControlButtons				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates a box and a button group		***
  ***	containing the various control buttons.			***
  ***								***
  ***	Returns :						***
  ***	   createControlButtons	The box containing the control	***
  ***				buttons.			***
  ***								***
  *****************************************************************
*/
    private final Box createControlButtons() throws Exception {

	// Create the box and button group to contain the control buttons.
	Box		controlBox = new Box(BoxLayout.X_AXIS);
	ButtonGroup	bGroup = new ButtonGroup();

	// Load the images.
	loadImages();

	// Create the buttons.
	if (buttons == null) {
	    buttons = new JRadioButton[BUTTONS];
	}

	RadioListener rListener = new RadioListener(this);
	for (int idx = 0; idx < BUTTONS; ++idx) {
	    buttons[idx] = new JRadioButton();
	    buttons[idx].setActionCommand(actions[idx]);
	    buttons[idx].setDisabledIcon(new ImageIcon(images[idx][DISABLED]));
	    buttons[idx].setIcon(new ImageIcon(images[idx][OFF]));
	    buttons[idx].setSelectedIcon(new ImageIcon(images[idx][ON]));
	    buttons[idx].addActionListener(rListener);
	    buttons[idx].setEnabled(false);
	    controlBox.add(buttons[idx]);
	    bGroup.add(buttons[idx]);
	}

	return (controlBox);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createControls					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates the controls used to control the	***
  ***	video retrieval. The controls consist of a radio button	***
  ***	box and a position slider.				***
  ***								***
  ***	Returns :						***
  ***	   createControls	The controls panel.		***
  ***								***
  *****************************************************************
*/
    private final JPanel createControls() throws Exception {

	// Create the controls panel.
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	JPanel controlsPanelR = new JPanel(gbl);

	// Create the buttons.
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx =
	    gbc.weighty = 0;
	Utility.add
	    (controlsPanelR,
	     createControlButtons(),
	     gbl,
	     gbc,
	     0,0,
	     1,1);

	// Create the time controls (slider/time display).
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 1;
	Utility.add
	    (controlsPanelR,
	     createTimeControls(),
	     gbl,
	     gbc,
	     0,1,
	     1,1);

	return (controlsPanelR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createPositionSlider				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates the position slider for the	***
  ***	the video retreiver.					***
  ***								***
  ***	Returns :						***
  ***	   createPositionSlider	The slider.			***
  ***								***
  *****************************************************************
*/
    private final JSlider createPositionSlider() {

	// Create the slider.
	JSlider slider = new JSlider(0,10000,0);

	slider.setPaintLabels(false);
	slider.setPaintTicks(true);
	slider.setMajorTickSpacing(100);
	slider.setMinorTickSpacing(10);
	slider.setPaintTrack(true);
	slider.setOrientation(JSlider.HORIZONTAL);
	slider.addChangeListener(new SliderListener(this));

	return (slider);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createTimeControls				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates a box containing a slider and	***
  ***	a text field to show the current time on the right.	***
  ***								***
  ***	Returns :						***
  ***	   createTimeControls	The time panel.			***
  ***								***
  *****************************************************************
*/
    private final JPanel createTimeControls() {

	// Create the time panel to hold the slider and text.
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	JPanel timePanelR = new JPanel(gbl);

	// Create the position slider.
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1;
	gbc.weighty = 0;
	Utility.add
	    (timePanelR,
	     positionSlider = createPositionSlider(),
	     gbl,
	     gbc,
	     0,0,
	     1,1);

	// Create an area to hold the time and frame rate fields.
	JPanel timeRatePanel = new JPanel();

	gbc.anchor = GridBagConstraints.NORTHEAST;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1;
	Utility.add
	    (timePanelR,
	     timeRatePanel,
	     gbl,
	     gbc,
	     0,1,
	     1,1);

	// Create a new gridbag layout for the time/rate panel.
	timeRatePanel.setLayout(gbl = new GridBagLayout());

	// Create the time label and text.
	JLabel label = new JLabel("Time");
	gbc.anchor = GridBagConstraints.WEST;
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx =
	    gbc.weighty = 0;
	Utility.add
	    (timeRatePanel,
	     label,
	     gbl,
	     gbc,
	     0,0,
	     1,1);

	timeText = new JTextField(19);
	timeText.setEditable(false);
	Utility.add
	    (timeRatePanel,
	     timeText,
	     gbl,
	     gbc,
	     1,0,
	     1,1);


	// Create the frame rate label and text.
	label = new JLabel("Rate");
	gbc.anchor = GridBagConstraints.EAST;
	Utility.add
	    (timeRatePanel,
	     label,
	     gbl,
	     gbc,
	     3,0,
	     1,1);

	frameRateText = new JTextField(6);
	frameRateText.setEditable(false);
	Utility.add
	    (timeRatePanel,
	     frameRateText,
	     gbl,
	     gbc,
	     4,0,
	     1,1);

	// Create a filler label.
	label = new JLabel("");
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1;
	Utility.add
	    (timeRatePanel,
	     label,
	     gbl,
	     gbc,
	     2,0,
	     1,1);

	return (timePanelR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	destroyDataSource				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method destroys the data source object for this	***
  ***	video retriever.					***
  ***								***
  *****************************************************************
*/
    private final void destroyDataSource() {

	// Destroy the media player.
	destroyMediaPlayer();

	// Destroy the source.
	if (dataSource != null) {
	    dataSource.disconnect();
	    dataSource = null;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	destroyMediaPlayer				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method destroys the media player for this	***
  ***	video retriever.					***
  ***								***
  *****************************************************************
*/
    private final void destroyMediaPlayer() {

	// If there is a media player, shut it down.
	if (mediaPlayer != null) {
	    mediaBox.remove(mediaPlayer);
	    mainPanel.validate();
	    mediaPlayer.stopAndDeallocate();
	    mediaPlayer = null;
	}
	
	// Disable the buttons.
	if (buttons != null) {
	    for (int idx = 0; idx < BUTTONS; ++idx) {
		buttons[idx].setEnabled(false);
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disable						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method disables the display, desensitizing	***
  ***	the buttons and pausing the output. It waits for the	***
  ***	display to actually pause.				***
  ***								***
  *****************************************************************
*/
    public final void disable() {

	// If we have a display.
	if (mainPanel != null) {

	    // Desensitize the buttons and turn off the slider.
	    for (int idx = 0; idx < buttons.length; ++idx) {
		buttons[idx].setEnabled(false);
	    }
	    //      positionSlider.setEnabled(false);

	    // If there is a thread running, pause it.
	    if ((myThread != null) && myThread.isAlive()) {

		// Request a pause.
		pause();

		// Wait for the display to actually pause.
		synchronized (this) {
		    while (state != NOREQUEST) {
			try {
			    wait();
			} catch (InterruptedException e) {
			    e.printStackTrace();
			    return;
			}
		    }
		}
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	enable						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method enables the display by making the	***
  ***	buttons and slider sensitive again.			***
  ***								***
  *****************************************************************
*/
    public final void enable() {

	// If we have a display.
	if (mainPanel != null) {

	    // Sensitize the buttons and turn off the slider.
	    for (int idx = 0; idx < buttons.length; ++idx) {
		buttons[idx].setEnabled(true);
	    }
	    //      positionSlider.setEnabled(true);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	endOfData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method moves to the end of the data.		***
  ***								***
  ***	Input :							***
  ***	   trackingSliderI	Is this tracking the slider?	***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void endOfData(boolean trackingSliderI) {

	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.ONEFRAME,
	     VideoRequest.ENDOFDATA);
	lRequest.setTrackingSlider(trackingSliderI);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getConnection					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the current connection to the	***
  ***	DataTurbine.						***
  ***								***
  ***	Returns :						***
  ***	   getConnection	The connection in use.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		For V2, the connection is a sink.		***
  ***								***
  *****************************************************************
*/
    public final Sink getConnection() {
	return (rbnbCon);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getCurrentFrame					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the current frame index as a	***
  ***	time object.						***
  ***								***
  ***	Returns :						***
  ***	   getCurrentFrame	The current frame index.	***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final double getCurrentFrame() {
	return (currentFrame);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getDataSource					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the JMF data source for this	***
  ***	video retriever. If necessary, it creates one.		***
  ***								***
  ***	Returns :						***
  ***	   getDataSource	The data source.		***
  ***								***
  *****************************************************************
*/
    final DataSource getDataSource() {

	// If there is no existing data source, create one if possible.
	if ((dataSource == null) &&
	    (getConnection() != null) &&
	    (getMap() != null)) {
	    dataSource = new DataSource();

	    // Initialize the data source and connect it up.
	    dataSource.setParent(this);
	    dataSource.setRegisteredMap(getMap());
	    dataSource.connect();
	}

	return (dataSource);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMap						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the current map.			***
  ***								***
  ***	Returns :						***
  ***	   getMap		The map in use.			***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		For V2, the map is a channel map.		***
  ***								***
  *****************************************************************
*/
    public final ChannelMap getMap() {
	return (rbnbMap);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMaximumFrame					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the maximum frame that the	***
  ***	retriever knows about.					***
  ***								***
  ***	Returns :						***
  ***	   getMaximumFrame	The maximum frame index.	***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final double getMaximumFrame() {
	return (maximumFrame);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMediaPlayer					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the media player for this	***
  ***	video retriever. If necessary, it creates one.		***
  ***								***
  ***	Returns :						***
  ***	   getMediaPlayer	The media player.		***
  ***								***
  *****************************************************************
*/
    final MediaPlayer getMediaPlayer() {
	// If there is no existing media player, create one if possible.
	if (mediaPlayer == null) {
	    getDataSource();
	    if (dataSource != null) {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(dataSource);
		mediaPlayer.addControllerListener(this);
		if (mediaPlayer.getPlayer() == null) {
		    mediaPlayer = null;
		} else {
		    mediaPlayer.setPlaybackLoop(false);
		    mediaPlayer.setFixedAspectRatio(true);
		    mediaPlayer.setPopupActive(false);
		    mediaPlayer.setControlPanelVisible(false);
		    synchronized (this) {
			state = REALIZING;
			notifyAll();
		    }
		    mediaPlayer.setVolumeLevel
			(currentRequest.getVolumeLevel());
		    mediaPlayer.realize();
		}
	    }
	  
	} else {
	    // Otherwise, start the media player.
	    mediaPlayer.setVolumeLevel(currentRequest.getVolumeLevel());
	    startMediaPlayer();
	}
      
	return (mediaPlayer);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMinimumFrame					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the minimum frame that the	***
  ***	retriever knows about.					***
  ***								***
  ***	Returns :						***
  ***	   getMinimumFrame	The minimum frame index.	***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final double getMinimumFrame() {
	return (minimumFrame);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMovingSlider					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the value of the moving slider	***
  ***	flag.							***
  ***								***
  ***	Returns :						***
  ***	   getMovingSlider	Is the slider being moved?	***
  ***								***
  *****************************************************************
*/
    final synchronized boolean getMovingSlider() {
	return (movingSlider);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getPrimaryMap					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the "primary" map. The primary	***
  ***	map contains a single channel, either the first video	***
  ***	channel (if any) or the first audio channel (if any and	***
  ***	no video).						***
  ***								***
  ***	Returns :						***
  ***	   getPrimaryMap	The primary map (may be set up	***
  ***				by this routine).		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		For V2, the map is a channel map.		***
  ***								***
  *****************************************************************
*/
    final ChannelMap getPrimaryMap() throws SAPIException {

	// If there is no primary map, create it by locating the first video
	// or audio channel in the DataSource's list of channels.
	if (primaryMap == null) {
	    String[] videoChannels = dataSource.getVideoChannels();
	    primaryMap = new ChannelMap();

	    if ((videoChannels != null) && (videoChannels.length > 0)) {
		primaryMap.Add(videoChannels[0]);
	    } else {
		primaryMap.Add(dataSource.getAudioChannels()[0]);
	    }
	}

	return (primaryMap);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getRequest					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the current request.		***
  ***								***
  ***	Returns :						***
  ***	   getRequest		The request in question.	***
  ***								***
  *****************************************************************
*/
   final VideoRequest getRequest() {
	return (currentRequest);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	loadImages					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method loads the button images.			***
  ***								***
  ***	Modification History :					***
  ***	   10/12/2000 - INB					***
  ***		If a JAR file doesn't have the images, see if	***
  ***		its directory does.				***
  ***      2004/12/17 - WHF  Replaced most of the code with one line, which
  ***		actually works.
  ***								***
  *****************************************************************
*/
    private final void loadImages() throws Exception {

	// If the images aren't already loaded, load them.
	if (images == null) {
	    images = new Image[BUTTONS][];

	    for (int idx = 0; idx < BUTTONS; ++idx) {
		images[idx] = new Image[3];
		for (int idx1 = 0; idx1 < 3; ++idx1) {
			// 2004/12/17  WHF  New approach.
			images[idx][idx1] = new ImageIcon(this.getClass().getClassLoader()
					.getResource(imageFiles[idx][idx1])).getImage();			
			
/*  old method
		    byte[]   image = null;
		    String   classPath = System.getProperty("java.class.path"),
			homePath = System.getProperty("user.home"),
			currentPath = System.getProperty("user.dir"),
			pathSeparator = System.getProperty("path.separator"),
			fileSeparator = System.getProperty("file.separator");

		    // Locate the file. We search the following:
		    //   1) The local directory,
		    //   2) The user's home directory,
		    //   3) The classpath. If a JAR file is found in the class
		    //   path, then it is searched.
		    for (int idx2 = 0; (image == null) && (idx2 < 3); ++idx2) {
			String path = null;

			switch (idx2) {
			case 0: path = currentPath; break;
			case 1: path = homePath; break;
			case 2: path = classPath; break;
			}

			for (int last = 0,
				 current = path.indexOf(pathSeparator);
			     (image == null) && (last < path.length());
			     last = current + 1,
				 current = path.indexOf(pathSeparator,last)) {
			    boolean gotIt = false;

			    if (current == -1) {
				current = path.length();
			    }
			    String trial = path.substring(last,current);
			    File   directory = new File(trial);

			    if (directory.isFile()) {
				try {
				    JarInputStream jfi =
					new JarInputStream
					    (new FileInputStream(directory));
				    JarEntry je;

				    while ((je = jfi.getNextJarEntry()) !=
					   null) {
					String   name = je.getName();

					if (name.equals
					    (imageFiles[idx][idx1])) {
					    image =
						new byte[(int) je.getSize()];
					    jfi.read(image,0,image.length);
					    gotIt = true;
					    break;
					}
				    }
				    jfi.close();
				} catch (Exception e) {
				}

				if (trial.lastIndexOf(fileSeparator) != -1) {
				    trial = trial.substring
					(0,
					 trial.lastIndexOf(fileSeparator));
				} else {
				    trial = "./";
				}
			    }

			    // If we didn't have a jar file or the jar file
			    // failed (and trial has been reset to the
			    // directory of the jar file), then try looking in
			    // the directory.
			    if (!gotIt) {
				trial += fileSeparator + imageFiles[idx][idx1];
				File imageFile = new File(trial);
				if (imageFile.exists()) {
				    try {
					FileInputStream fis =
					    new FileInputStream(imageFile);
					image =
					    new byte[(int) imageFile.length()];
					fis.read(image);
					fis.close();
					break;
				    } catch (IOException e) {
					image = null;
				    }
				}
			    }
			}
		    }
		    if (image == null) {
			throw new Exception
			    ("Unable to load image " + imageFiles[idx][idx1]);
		    }
		    images[idx][idx1] =
			Toolkit.getDefaultToolkit().createImage(image);
			*/
		}
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	moveSlider					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  This method moves the slider to the specified		***
  ***	position.						***
  ***								***
  ***	Input :							***
  ***	   frameI		The frame index.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    private final void moveSlider(double frameI) {

	// If the input is valid and we're not tracking the slider, move the
	// slider.
	if (!trackingSlider && (frameI != -1.)) {

	    // Calculate where to move the slider.
	    double percentage =
		(frameI - minimumFrame)/(maximumFrame- minimumFrame);

	    synchronized (this) {
		setMovingSlider(true);

		if (percentage <= 0.) {
		    if (positionSlider.getValue() !=
			positionSlider.getMinimum()) {
			positionSlider.setValue(positionSlider.getMinimum());
		    }
		} else if (percentage >= 1.) {
		    if (positionSlider.getValue() !=
			positionSlider.getMaximum()) {
			positionSlider.setValue(positionSlider.getMaximum());
		    }
		} else {
		    int position = positionSlider.getMinimum() +
			(int) (percentage*(positionSlider.getMaximum() -
					   positionSlider.getMinimum()));

		    if (positionSlider.getValue() != position) {
			positionSlider.setValue(position);
		    }
		}
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	pause						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method pauses the display.			***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void pause() {
		buttons[PAUSE].setSelected(true);
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.CONTINUOUS,
	     VideoRequest.PAUSE);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	realTime					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method switches the display into real time	***
  ***	mode.							***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void realTime() {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.CONTINUOUS,
	     VideoRequest.REALTIME);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	run						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method actually runs the requests.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are channel maps.			***
  ***								***
  *****************************************************************
*/
    public final void run() {
	ChannelMap frame = null;
	double waitTime = 0.,
	    lastFrameAt = System.currentTimeMillis()/1000.;
	double[] times = null;

	// Let our parent know we're ready.
	synchronized (this) {
	    ready = true;
	    notifyAll();
	}

	// Loop until we are terminated by an outside agency.

    RUNLOOP:
	while (true) {
	    boolean waited = false;

	    // If there is a current request, handle the current frame
	    // retrieved.
	    if (currentRequest != null) {

		// If there is a wait time, wait it out.
		if (waitTime > 0.) {
		    synchronized (this) {
			try {
			    double nanos =
				waitTime - Math.floor(waitTime*1000.)/1000.;
			    int inanos = (int) (nanos*1000000000.);
			    wait((long) waitTime,inanos);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			    break RUNLOOP;
			}
		    }
		}

		// If the state is RUNNING or there is a frame, work on the
		// current request.
		if ((state == RUNNING) || (frame != null)) {

		    // Display the frame.
		    dataSource.putMap(frame);

		    if (frame == null) {
			// If there was no real frame, turn off the current
			// request.

			// Ensure that we're in the expected state.
			synchronized (this) {
			    while (state != NOREQUEST) {
				try {
				    wait();
				} catch (InterruptedException e) {
				    e.printStackTrace();
				    break RUNLOOP;
				}
			    }
			}

			// If we're not tracking the slider, update the limits.
			boolean trackingSlider =
			    currentRequest.getTrackingSlider() ||
			    (currentRequest.flags ==
			     currentRequest.BEGINNINGOFDATA) ||
			    (currentRequest.flags ==
			     currentRequest.ENDOFDATA);
			currentRequest = null;
			if (!trackingSlider) {
			    try {
				updateLimits();
			    } catch (Exception e) {
				e.printStackTrace();
				
				// 2004/12/03  WHF  We don't really want to do this 
				//   in this thread, delegate:
				// 2005/02/09  WHF  Still hung.  Print to stderr.
				/*
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
				JOptionPane.showMessageDialog
				    (mainPanel,
				     //e.getMessage(),
					 "Data source disconnected.",
				     "Update Display Limits Error",
				     JOptionPane.ERROR_MESSAGE,
				     null);
					}
				}); */
				break RUNLOOP;
			    }
			}

			// With no request to handle, change the button and
			// move the slider, unless we were tracking the slider.
			if (nextRequest == null) {
			    buttons[PAUSE].setSelected(true);
			    if (!trackingSlider) {
				moveSlider(currentFrame);
			    }
			}
		    }
		}

		// Clear the frame information.
		waitTime = 0.;
		frame = null;
	    }
		

	    // Wait for a request to process.
	    synchronized (this) {
		while (((currentRequest == null) ||
			currentRequest.getEndOfData()) &&
		       (nextRequest == null)) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			e.printStackTrace();
			break RUNLOOP;
		    }
		}
	    }

	    // If there is a next request, handle it.
	    if (nextRequest != null) {
		VideoRequest myRequest;

		// Grab the request.
		synchronized (nextRequest) {
		    myRequest = nextRequest;
		    nextRequest = null;
		}

		// Terminate any existing request.
		if (currentRequest != null) {
		    try {
			currentRequest.stop();
			dataSource.putMap(null);
		    } catch (Exception e) {
			e.printStackTrace();
			break RUNLOOP;
		    }
		}
		currentRequest = null;

		// Ensure that we're in the expected state.
		synchronized (this) {
		    while (state != NOREQUEST) {
			try {
			    wait();
			} catch (InterruptedException e) {
			    e.printStackTrace();
			    break RUNLOOP;
			}
		    }
		}

		// Update the frame limits by asking the DataTurbine server for
		// the limits of the data. We can only do this when we're
		// between requests because that is the only time we can be
		// sure that data is not flowing. However, we don't want to do
		// this if the slider is tracking.
		if (!myRequest.getTrackingSlider() && (dataSource != null)) {
		    try {
			updateLimits();
		    } catch (Exception e) {
			e.printStackTrace(); 
			/* 2005/03/23  WHF  Showing this dialog locks up the application:
			JOptionPane.showMessageDialog
			    (mainPanel,
			     e.getMessage(),
			     "Update Display Limits Error",
			     JOptionPane.ERROR_MESSAGE,
			     null); */
			break RUNLOOP;
		    }
		}

		// Set up the new current request for processing.
		currentRequest = myRequest;
		setTrackingSlider(currentRequest.getTrackingSlider());

		// If the request is tracking the slider, ensure that the pause
		// button is lit.
		if (currentRequest.getTrackingSlider()) {
		    buttons[PAUSE].setSelected(true);
		}

		// If it is a terminate, do so.
		if (currentRequest.isTerminate()) {
		    break RUNLOOP;

		} else if (currentRequest.isPause()) {
		    // If it is a pause, clear it 'cause we're done.
		    currentRequest = null;

		} else {
		    // Otherwise, update the start time and start things going.
		    if (currentRequest.flags != currentRequest.SETFRAMEINDEX) {
			currentRequest.setStartTime(times);
		    }

		    getMediaPlayer();
		}
	    }

	    // If there is a current request, grab the next frame and time.
	    if (currentRequest != null) {
		try {
		    synchronized (this) {
			while ((state == PREFETCHING) ||
			       ((state != NOREQUEST) &&
				!currentRequest.isActive())) {
			    try {
				wait(1000);
			    } catch (InterruptedException e) {
				e.printStackTrace();
				break RUNLOOP;
			    }
			}
		    }

		    // If the request has terminated for some reason, clear it
		    // out.
		    if (state == NOREQUEST) {
			currentRequest = null;

		    } else {
			// Otherwise fetch the next frame of data from the
			// DataTurbine. This routine also sets the time range
			// for the frame, the amount of time we should wait to
			// make the frame play at the correct speed, and
			// determines if the frame rate channel is in the
			// frame.
			frame = currentRequest.getNextFrame(); 

			// If we actually got a frame, update the rate and time
			// displays.
			//if (frame != null && frame.GetName(0).equals(primaryMap.GetName(0))) {  // mjm added make sure video frame
			if (frame != null && hasPrimaryChan(frame)) { 

			    // If the frame contains the channel that we're
			    // using for the rate calculations, update the rate
			    // display. The channel is usually the video
			    // channel, unless there is only an audio channel).
			    if (currentRequest.getRateTime() !=
				Double.MAX_VALUE) {
				double now = System.currentTimeMillis()/1000.,
				    rate;

				// If the current time is the same as the last
				// time, we would get a division by zero when
				// calculating rate. Instead, we  use either
				// the last frame rate (if it has been set) or
				// a rate of 1 as a guess.

				if (now == lastFrameAt)  rate = 0.;   // mjm
				else {
				    // If the current time is different than
				    // the last time, calculate the rate
				    rate = 1./(now - lastFrameAt);
				}

				// For non-zero rate, use a lowpass filter to update the rate
  				if (rate != 0.) { // was .1, .9 - mjm 1/11/05
       				    rate = 0.5*rate + 0.5*lastFrameRate;
       				}

				// Update the frame rate display.
				frameRateText.setText
				    (Double.toString
				     (Math.round(rate*100.)/100.));

				// Save the frame rate and time for the next
				// pass.
				lastFrameRate = rate;
				lastFrameAt = now;
			    }

			    // Grab the time range for the frame. The time
			    // values are:
			    //	0) starting frame index
			    //	1) frame start time
			    //	2) ending frame index (should match start)
			    //	3) frame end time
			    times = currentRequest.getTimes();

			    // Update the slider limits (minimum and maximum
			    // frame times).
			    if ((minimumFrame == -Double.MAX_VALUE) ||
				(times[0] < minimumFrame)) {
				minimumFrame = times[0];
			    }
			    if ((maximumFrame == -Double.MAX_VALUE) ||
				(times[2] > maximumFrame)) {
				maximumFrame = times[2];
			    }

			    // Move the slider to match the current frame
			    // within the known limits.
			    moveSlider(times[0]);

			    // Update the time display (use the frame start
			    // time).
			    timeText.setText
				(com.rbnb.api.Time.since1970(times[1]));
			}

			// Grab the amount of time to wait to play at the
			// correct speed. This value is only set when handling
			// play backward and play forward requests. Otherwise,
			// it is -Double.MAX_VALUE and the display is updated
			// immediately.
			waitTime = currentRequest.getWaitTime();
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    JOptionPane.showMessageDialog
			(mainPanel,
			 e.getMessage(),
			 "Update Display Error",
			 JOptionPane.ERROR_MESSAGE,
			 null);
		    break RUNLOOP;
		}
	    }
	}

	// Shut things down.
	currentRequest =
	    nextRequest = null;
	destroyMediaPlayer();
	if (dataSource != null) {
	    dataSource.disconnect();
	}
	disable();
    }

    /*-------------------------------------------------------------------------------*/
    // utility function to decide if rbnb channelMap has primary (video) chane
    private boolean hasPrimaryChan(ChannelMap frame) throws Exception {
	int i;

	for(i=0; i<frame.NumberOfChannels(); ++i) {
	    //System.err.println("frame("+i+"): "+frame.GetName(i)+", pmap: "+primaryMap.GetName(0));
	    if(frame.GetName(i).equals(primaryMap.GetName(0))) return(true);
	}

	return(false);
    }


/*
  *****************************************************************
  ***								***
  ***	Name :	runBackward					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method runs backward from whereever we		***
  ***	currently are. The default is to run from the		***
  ***	end.							***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void runBackward() {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.CONTINUOUS,
	     VideoRequest.RUNBACKWARD);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	runForward					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method runs forward from whereever we currently	***
  ***	are. The default is to run from the beginning.		***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void runForward() {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.CONTINUOUS,
	     VideoRequest.RUNFORWARD);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setConnection					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the current connection to the	***
  ***	specified value. It terminates any active display.	***
  ***								***
  ***	Input :							***
  ***	   rbnbConI		The new connection.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		The connection is now a sink.			***
  ***								***
  *****************************************************************
*/
    public final void setConnection(Sink rbnbConI) {

	// Terminate any active display.
	if ((mainPanel != null) && (dataSource != null)) {
	    terminate();
	}

	// Destroy the data source.
	destroyDataSource();

	// Update the connection.
	rbnbCon = rbnbConI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setContainer					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the parent container for this video	***
  ***	retriever to the specified input value.			***
  ***								***
  ***	Input :							***
  ***	   containerI		The container to display in.	***
  ***								***
  *****************************************************************
*/
    public final synchronized void setContainer(Container containerI)
	throws Exception
    {

	// Terminate any existing display.
	if (mainPanel != null) {
	    terminate();
	}

	// Destroy the media player.
	destroyMediaPlayer();

	// If the container hasn't actually changed, do nothing.
	if ((mainPanel != null) && (mainPanel.getParent() == containerI)) {
	    return;
	}

	// Create the top-level panel with a grid bag layout.
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	mainPanel = new JPanel(gbl);
	containerI.add(mainPanel);

	// Create the controls.
	gbc.anchor = GridBagConstraints.NORTHEAST;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1;
	gbc.weighty = 0;
	Utility.add
	    (mainPanel,
	     createControls(),
	     gbl,
	     gbc,
	     0,0,
	     1,1);

	// Create a box to hold the media player.
	mediaBox = new Box(BoxLayout.X_AXIS);

	// Add the box to the frame.
	gbc.anchor = GridBagConstraints.NORTHEAST;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx =
	    gbc.weighty = 1;
	Utility.add
	    (mainPanel,
	     mediaBox,
	     (GridBagLayout) mainPanel.getLayout(),
	     gbc,
	     0,1,
	     1,1);


	containerI.validate();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setFrameIndex					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the frame index for the data source	***
  ***	to the specified value.					***
  ***								***
  ***	Input :							***
  ***	   desiredFrameI	The frame to select.		***
  ***	   trackingSliderI	Is this tracking the slider?	***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void setFrameIndex(double desiredFrameI,
				    boolean trackingSliderI)
    {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.ONEFRAME,
	     VideoRequest.SETFRAMEINDEX);
	lRequest.setStartTime(desiredFrameI);
	lRequest.setTrackingSlider(trackingSliderI);

	// Always search for the frame in the reverse direction.
	lRequest.setDirection(VideoRequest.BACKWARD);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setMap						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the retrieval map to the specified	***
  ***	value. It terminates any active display.		***
  ***								***
  ***	Input :							***
  ***	   rbnbMapI		The new map.			***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		The map is now a channel map.			***
  ***								***
  *****************************************************************
*/
    public final void setMap(ChannelMap rbnbMapI) throws Exception {

	// Destroy the data source.
	destroyDataSource();

	// Update the map and destroy the primary map (we'll regenerate it
	// later).
	rbnbMap = rbnbMapI;
	primaryMap = null;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setMovingSlider					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the moving slider flag.		***
  ***								***
  ***	Input :							***
  ***	   movingSliderI	Are we moving the slider?	***
  ***								***
  *****************************************************************
*/
    final synchronized void setMovingSlider(boolean movingSliderI) {
	movingSlider = movingSliderI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setupMediaPlayer				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Set up the media player. In particular, make things	***
  ***	the right size and visible.				***
  ***								***
  *****************************************************************
*/
    private final void setupMediaPlayer() {

	// Make the player visible.
	mediaPlayer.setVisible(true);
      
	// Put the media player in the media box.
	mediaBox.add(mediaPlayer);
    
	// Validate the display.
	mainPanel.validate();
    
	// Update the state.
	synchronized (this) {
	    state = PREFETCHING;
	    notifyAll();
	}

	// Request a prefetch.
	mediaPlayer.prefetch();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setTrackingSlider				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the tracking slider flag.		***
  ***								***
  ***	Input :							***
  ***	   trackingSliderI	Are we tracking the slider?	***
  ***								***
  *****************************************************************
*/
    final synchronized void setTrackingSlider(boolean trackingSliderI) {
	trackingSlider = trackingSliderI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setVisible					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method makes the media player (in)visible.	***
  ***								***
  ***	Input :							***
  ***	   visibleI		Make the player visible?	***
  ***								***
  *****************************************************************
*/
    public final void setVisible(boolean visibleI) {
	if (mediaPlayer != null) {
	    if (visibleI) {
		enable();
	    } else {
		disable();
	    }
	    mediaPlayer.setVisible(visibleI);

	    if (mainPanel != null) {
		mainPanel.validate();
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	start						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts up the video retriever thread.	***
  ***								***
  *****************************************************************
*/
    public final void start() {

	// Start a thread running on this object.
	(myThread = new Thread(this)).start();

	// Wait for it to be ready for us.
	synchronized (this) {
	    while (!ready) {
		try {
		    wait(1000);
		} catch (InterruptedException e) {
		    return;
		}
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	startMediaPlayer				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts the media player.			***
  ***								***
  *****************************************************************
*/
    private final void startMediaPlayer() {

	// Update the state.
	synchronized (this) {
	    state = STARTING;
	    notifyAll();
	}

	// Start the media player.
	mediaPlayer.start();

	// Enable the buttons.
	for (int idx = 0; idx < BUTTONS; ++idx) {
	    buttons[idx].setEnabled(true);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stepBackward					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method steps back one frame.			***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void stepBackward() {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.ONEFRAME,
	     VideoRequest.STEPBACKWARD);

	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stepForward					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method steps back one frame.			***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void stepForward() {
	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.ONEFRAME,
	     VideoRequest.STEPFORWARD);
	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	terminate					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method terminates the display.			***
  ***								***
  ***	Modification History :					***
  ***	   05/08/2001 - INB					***
  ***		All methods that create a new nextRequest first	***
  ***		create the request as a local, set it up, and	***
  ***		then set nextRequest. This eliminates some	***
  ***		potential synchronization race conditions.	***
  ***		Also moved stopStream calls out of sync blocks.	***
  ***								***
  *****************************************************************
*/
    public final void terminate() {
	if ((myThread == null) || !myThread.isAlive()) {
	    return;
	}

	VideoRequest lRequest = currentRequest;
	if (lRequest != null) {
	    lRequest.stopStream();
	}
	lRequest = new VideoRequest
	    (this,
	     VideoRequest.CONTINUOUS,
	     VideoRequest.TERMINATE);
	synchronized (this) {
	    nextRequest = lRequest;
	    notifyAll();
	}

	// If there is a thread, wait for it to terminate.
	if (myThread != null) {
	    try {
		myThread.join();
	    } catch (InterruptedException e) {
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	toggleButton					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method toggles the specified button on.		***
  ***								***
  ***	Input :							***
  ***	   buttonI		The button to toggle.		***
  ***								***
  *****************************************************************
*/
    public final void toggleButton(int buttonI) {
	buttons[buttonI].setEnabled(true);
	buttons[buttonI].doClick();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	updateLimits					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method updates the limits (minimum and maximum)	***
  ***	of the frame index range.				***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Updated for V2.  Maps become channel maps and	***
  ***		times are doubles.  There are no actual frame	***
  ***		indexes, so we use the time values.		***
  ***		Note that we cannot use the registration map	***
  ***		to find out the time information because it	***
  ***		gets overwritten by the video/audio user	***
  ***		information.  We should find a way to combine	***
  ***		it with that information when returning it from	***
  ***		the server,  but that isn't currently possible.	***
  ***								***
  *****************************************************************
*/
    final void updateLimits() throws Exception {
	// Retrieve the limits for the map.
	ChannelMap oldestMap;
	ChannelMap newestMap;

       	rbnbCon.Request(getPrimaryMap(),0.,0.,"oldest");
	oldestMap = rbnbCon.Fetch(-1);
	rbnbCon.Request(getPrimaryMap(),0.,0.,"newest");
	newestMap = rbnbCon.Fetch(-1);

	// If there are no limits to be found, there must be no data. We should
	// stop displaying anything.

	if ((oldestMap.NumberOfChannels() == 0) ||
	    (newestMap.NumberOfChannels() == 0)) {
	    throw new Exception("No data is available.");
	}

	minimumFrame = Double.MAX_VALUE;
	maximumFrame = -Double.MAX_VALUE;
	for (int idx = 0; idx < newestMap.NumberOfChannels(); ++idx) {
	    minimumFrame = Math.min(minimumFrame,
				    oldestMap.GetTimeStart(idx));
	    maximumFrame = Math.max(maximumFrame,
				    newestMap.GetTimeStart(idx) +
				    newestMap.GetTimeDuration(idx));
	}

	// If there are no limits to be found, there must be no data. We should
	// stop displaying anything.
	if (minimumFrame == Double.MAX_VALUE) {
	    throw new Exception("No data is available.");
	}
    }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	VideoRequest					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class is used to handle requests made of the	***
  ***	video retriever.					***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Ported to V2.4.4 RBNB.				***
  ***								***
  *****************************************************************
*/
class VideoRequest implements Runnable {

    // Private fields:
    private boolean		active = false,
				endOfData = true,
				streamHasData = false,
				trackingSlider = false;

    private String     	        rateChannel = null;

    private Exception		runException = null;

    int				flags = PAUSE;

    private int			direction = FORWARD;

    private ChannelMap		streamFrame = null;

    double			startTime = -Double.MAX_VALUE,
				endTime = -Double.MAX_VALUE,
				duration = CONTINUOUS,
				rateTime = -Double.MAX_VALUE,
				startedAt[] = null,
				times[] = null,
				waitTime = -Double.MAX_VALUE;

    private VideoRetriever	retriever = null;

    // Package constants:
    final static int		BEGINNINGOFDATA = Integer.MIN_VALUE + 1,
				ENDOFDATA = Integer.MAX_VALUE - 1,
				PAUSE = Integer.MIN_VALUE,
				REALTIME = Integer.MAX_VALUE,
				RUNBACKWARD = -2,
				RUNFORWARD = 2,
				SETFRAMEINDEX = 3,
				STEPBACKWARD = -1,
				STEPFORWARD = 1,
				TERMINATE = -3,
				BACKWARD = -1,
				FREEZE = 0,
				FORWARD = 1;

    final static String		OFF = "0",
				ON = "5",
				REQUESTRESPONSE = "Request/Response",
				STREAM = "Stream";

    final static double		ONEFRAME = 1.,
				ONEFRAMEBACK = -0.01,
				CONTINUOUS = Double.MAX_VALUE;

/*
  *****************************************************************
  ***								***
  ***	Name :	VideoRequest					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a new request from the	***
  ***	specified values.					***
  ***								***
  ***	Input :							***
  ***	   retrieverI		The video retriever.		***
  ***	   durationI		The request duration.		***
  ***	   flagsI		The request flags.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    VideoRequest(VideoRetriever retrieverI,double durationI,int flagsI) {
	retriever = retrieverI;
	duration = durationI;
	flags = flagsI;
	if ((flags == BEGINNINGOFDATA) ||
	    (flags == STEPFORWARD) ||
	    (flags == RUNFORWARD) ||
	    (flags == REALTIME)) {
	    setDirection(FORWARD);
	} else if ((flags == ENDOFDATA) ||
		   (flags == STEPBACKWARD) ||
		   (flags == RUNBACKWARD)) {
	    setDirection(BACKWARD);
	} else {
	    setDirection(FREEZE);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getEndOfData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the end of data flag.		***
  ***								***
  ***	Returns :						***
  ***	   getEndOfData		At end of the data?		***
  ***								***
  *****************************************************************
*/
    final boolean getEndOfData() {
	return (endOfData);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getNextFrame					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the next frame of data from	***
  ***	the DataTurbine.					***
  ***								***
  ***	Returns :						***
  ***	   getNextFrame		The next frame (Map) of data.	***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Maps are now channel maps.			***
  ***								***
  *****************************************************************
*/
    final ChannelMap getNextFrame() throws Exception {

	// If we've already reached the end of the data, return nothing.
	if (endOfData) {
	    return (null);
	}

	ChannelMap frameR = null;

	// If we are running forwards or in real-time, the data arrives in a
	// stream.
	if ((flags == REALTIME) || (flags == RUNFORWARD)) {
	    frameR = nextStreamFrame();

	} else {
	    // Otherwise, we are using request/response mode.
	    frameR = nextRequestFrame();
	}

	// Update the local time information.
	setTimes(frameR);

	// Are we at the end of the data?
	if (endOfData) {
	    frameR = null;
	}

	return (frameR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getRateTime					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the time (video unless there is	***
  ***	none, audio otherwise) of the current frame for rate	***
  ***	calculating purposes.					***
  ***								***
  ***	Returns :						***
  ***	   getRateTime		The time or null if none.	***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final double getRateTime() {
	return (rateTime);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getTimes					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the times array for the current	***
  ***	frame.							***
  ***								***
  ***	Returns :						***
  ***	   getTimes		The times array.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final double[] getTimes() {
	return (times);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getTrackingSlider				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method retrieves the value of the tracking	***
  ***	slider flag. The flag tells us whether or not this	***
  ***	event is tracking the slider (and therefor the slider	***
  ***	should not be moved).					***
  ***								***
  ***	Returns :						***
  ***	   getTrackingSlider	Are we tracking the slider?	***
  ***								***
  *****************************************************************
*/
    final boolean getTrackingSlider() {
	return (trackingSlider);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getVolumeLevel					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns a volume level for the audio	***
  ***	track(s). For anything but RUNFORWARD and REALTIME, the	***
  ***	volume is OFF.						***
  ***								***
  ***	Returns :						***
  ***	   getVolumeLevel	The volume for the audio.	***
  ***								***
  *****************************************************************
*/
    final String getVolumeLevel() {
	return (((flags == RUNFORWARD) || (flags == REALTIME)) ? ON : OFF);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getWaitTime					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the wait time for the current	***
  ***	frame.							***
  ***								***
  ***	Returns :						***
  ***	   getWaitTime		The time to wait.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final double getWaitTime() {
	return (waitTime);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isActive					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the active flag.			***
  ***								***
  ***	Returns :						***
  ***	   isActive		True if this request is active.	***
  ***								***
  *****************************************************************
  */
    final boolean isActive() {
	return (active);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isPause						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method determines if this request is a pause.	***
  ***								***
  ***	Returns :						***
  ***	   isPause		Request is a pause?		***
  ***								***
  *****************************************************************
  */
    final boolean isPause() {
	return (flags == PAUSE);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isTerminate					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method determines if this request is a		***
  ***	terminate.						***
  ***								***
  ***	Returns :						***
  ***	   isTerminate		Request is a terminate?		***
  ***								***
  *****************************************************************
*/
    final boolean isTerminate() {
	return (flags == TERMINATE);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	nextRequestFrame				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the next frame of data from a	***
  ***	request/response run. It actually has to work to find	***
  ***	the next frame, including determining where the first	***
  ***	frame is and where the next frame is.			***
  ***								***
  ***	Returns :						***
  ***	   nextRequestFrame	The next request/response frame.***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.		***
  ***								***
  *****************************************************************
*/
    private final ChannelMap nextRequestFrame() throws Exception {
	String[] audio = retriever.getDataSource().getAudioChannels(),
	    video = retriever.getDataSource().getVideoChannels();
	boolean needKeyFrame = false;
	/*
	    ((flags == BEGINNINGOFDATA) ||
	     (flags == ENDOFDATA) ||
	     (flags == STEPBACKWARD) ||
	     (flags == RUNBACKWARD) ||
	     (flags == SETFRAMEINDEX));
	*/
	String reference =
	    ((flags == BEGINNINGOFDATA) ? "oldest" :
	     (flags == ENDOFDATA) ? "newest" :
	     ((flags == STEPBACKWARD) || (flags == RUNBACKWARD)) ? "previous" :
	     ((flags == STEPFORWARD) || (flags == RUNFORWARD)) ? "next" :
	     "absolute");
	boolean needVideoFrame =
	    ((video != null) && (video.length > 0)) &&
	    (needKeyFrame ||
	     (flags == STEPFORWARD));
	ChannelMap requestMap = retriever.getPrimaryMap();
	ChannelMap  frameR = null;

	double frameStartTime = -Double.MAX_VALUE;
	double frameDuration = 0.;

	// If we have no time information, then this is the first frame. The
	// start time to get is the request's start time plus or minus a frame
	// depending on the direction we're running.
	if ((times == null) || (times[0] == -Double.MAX_VALUE)) {
	    if (startTime != -Double.MAX_VALUE) {	
		if ((flags == STEPBACKWARD) || (flags == RUNBACKWARD)) {
		    frameStartTime = startTime;
		} else if ((flags == STEPFORWARD) || (flags == RUNFORWARD)) {
		    frameStartTime = endTime;
		} else {
		    frameStartTime = startTime;
		}
	    }

	} else if (duration != CONTINUOUS) {
	    // If the request is for a single frame, then we're done.
	    return (null);

	} else if (flags == RUNBACKWARD) {
	    // If the request is running backward, back up by one frame.
	    frameStartTime = times[0];

	} else if (flags == RUNFORWARD) {
	    // If the request is running forward, move forward by one frame.
	    frameStartTime = times[2];
	}

	// Find the next frame of interest.
	boolean gotData = false,
	    performingReverseSearch = false;
	do {
	    double stepSize =
		((direction >= FORWARD) ? ONEFRAME :
		 (direction <= BACKWARD) ? ONEFRAMEBACK :
		 -Double.MAX_VALUE);
	    double rStart =
		((!reference.equals("absolute") &&
		  !reference.equals("next") &&
		  !reference.equals("previous")) ||
		 (frameStartTime == -Double.MAX_VALUE)) ? 0. : frameStartTime;

	    retriever.getConnection().Request
		(requestMap,
		 rStart,
		 frameDuration,
		 reference);
	    frameR = retriever.getConnection().Fetch(-1);

	    /* With the way the capture and the code works in V2, we'll pretty
	     * much always get an answer if there is data to be had.  Always
	     * flag it.
	     */
	    gotData = true;

	    /* Turn off all of this code for now.
	    // Determine if we got any data. If we require a key frame, then
	    // the code looks for a key frame.
	    if ((frameR != null) && (frameR.NumberOfChannels() > 0)) {
		boolean foundKey = false;

		// If this was the real reverse search, then we're done.
		if (performingReverseSearch && (flags == BEGINNINGOFDATA)) {
		    gotData = true;
		    break;
		}

		for (int idx = 0;
		     idx < frameR.NumberOfChannels();
		     ++idx) {
		    // If we need to get video frames, skip audio data.
		    if (needVideoFrame && (audio.length > 0)) {
			RBNBJMFSourceStream[] streams =
			    (RBNBJMFSourceStream[])
			    retriever.getDataSource().getStreams();
	    
			if (streams[idx].isAudioTrack()) {
			    continue;
			}
		    }
	  
		    if (!needKeyFrame) {
			gotData = true;
			break;

		    } else {
			/*
			 * There is no frame level user data for channels at
			 * this time and no keyframe indicators.  Fortunately,
			 * the only video capture application that we need to
			 * deal with is the JPEGCapture program, for which all
			 * frames are key frames.
			 *//*
			gotData = true;
			break;

		    } // end if (!needKeyFrame) / else block
		} // end for loop in idx over channels.length
	
	    } else if (performingReverseSearch) {

		// Not getting a frame in reverse search mode is a special case.
		// If we were looking for the oldest data, then there is no
		// data to be had.
		if (reference.equalsIgnoreCase("oldest")) {
		    break;

		} else {
		    // Otherwise, force a search for the oldest available data.
		    frameStartTime = -Double.MAX_VALUE;
		    reference = "oldest";
		    continue;
		}
	    }
	    */

	    // If we didn't, first we need to determine whether or not there is
	    // any more data. Retrieve the limits and see if we are inside
	    // them.
	    if (!gotData) {
		// Retrieve the limits for the map.
		ChannelMap oldestMap;
		ChannelMap newestMap;

		retriever.getConnection().Request(requestMap,0.,0.,"oldest");
		oldestMap = retriever.getConnection().Fetch(-1);
		retriever.getConnection().Request(requestMap,0.,0.,"newest");
		newestMap = retriever.getConnection().Fetch(-1);

		// With no limits, we're done.
		if ((oldestMap.NumberOfChannels() == 0) ||
		    (newestMap.NumberOfChannels() == 0)) {
		    break;
		}

		// With no step size, break out and return a blank frame.
		if (stepSize == -Double.MAX_VALUE) {
		    frameR = null;
		    break;
		}

		// If we get here and the frame start time is
		// -Double.MAX_VALUE, then the data source is probably
		// gone. Return a null frame.
		if (frameStartTime == -Double.MAX_VALUE) {
		    frameR = null;
		    break;
		}

		// Move the start frame by the step size.
		frameStartTime += stepSize;

		// Figure out the earliest and latest frame.
		double startFrame = Double.MAX_VALUE,
		    endFrame = -Double.MAX_VALUE;
		for (int idx = 0; idx < oldestMap.NumberOfChannels(); ++idx) {
		    double cStartFrame = oldestMap.GetTimeStart(idx);
		    double cEndFrame =
			newestMap.GetTimeStart(idx) +
			newestMap.GetTimeDuration(idx);

		    if (startFrame > cStartFrame) {
			startFrame = cStartFrame;
		    }
		    if (endFrame < cEndFrame) {
			endFrame = cEndFrame;
		    }
		}

		// If we're outside the limits, we're done.
		if ((frameStartTime < startFrame) ||
		    (frameStartTime > endFrame)) {
		    frameR = null;
		    break;
		}
	    }

	    // If we are not doing a step forward, then we are doing a reverse
	    // search.
	    performingReverseSearch = ((flags != STEPFORWARD) &&
				       (flags != RUNFORWARD));

	} while (!gotData);

	return (frameR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	nextStreamFrame					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the next frame of data from a	***
  ***	streaming run.						***
  ***								***
  ***	Returns :						***
  ***	   nextStreamFrame	The next stream frame.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				***
  ***								***
  *****************************************************************
*/
    private final ChannelMap nextStreamFrame() throws Exception {
	ChannelMap mapR = null;

	// Wait for a frame or an exception.
	synchronized (this) {
	    while (!streamHasData) {
		try {
		    wait(1000);
		} catch (InterruptedException e) {
		    setEndOfData(true);
		    return (null);
		}
	    }

	    // If we got an exception, throw it.
	    if (runException != null) {
		throw runException;
	    }

	    // Grab the frame and notify the other thread that we've got it.
	    mapR = streamFrame;
	    streamFrame = null;
	    streamHasData = false;
	    notifyAll();
	}

	// Release the CPU.
	Thread.currentThread().yield();

	// If the map is empty, switch off the stream.
	if (mapR == null) {
	    retriever.streaming = false;
	    String name = retriever.getConnection().GetClientName();
	    retriever.getConnection().CloseRBNBConnection();
	    retriever.getConnection().OpenRBNBConnection
		(retriever.serverAddress,name);
	    if (retriever.rbnbAddCon != null) {
		retriever.rbnbAddCon.CloseRBNBConnection();
		retriever.rbnbAddCon = null;
	    }
	}

	return (mapR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	run						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is used when streaming (RUNFORWARD or	***
  ***	REALTIME) to actually read the data. Basically, this	***
  ***	method provides a double buffering capability so that	***
  ***	we can prefetch frames of data. The intent is to reduce	***
  ***	the chances of causing choppy audio by trying to read	***
  ***	ahead a frame.						***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				***
  ***								***
  *****************************************************************
*/
    public final void run() {
	// These are used to handle synchronization of two streams.
	double[] lastTime = new double[2];
	double[] curTime = new double[2];
	double delta;
	ChannelMap localFrame = null;
	ChannelMap[] frame = new ChannelMap[2];
	for (int idx = 0; idx < 2; ++idx) {
	    lastTime[idx] =
		curTime[idx] = -Double.MAX_VALUE;
	    frame[idx] = null;
	}

	// Catch exceptions and pass them to the primary thread through the use
	// of the runException field.
	try {

	    // Elevate our priority to give us a greater chance of getting CPU
	    // time to grab the frame.
		// 2006/01/05  WHF  Probably not a good idea. 
		/*
	    Thread.currentThread().setPriority
			(Thread.currentThread().getPriority() + 1);
		*/

	    // Loop until the end of data is seen.
	    while (!endOfData) {

		// Wait until the last data frame has been processed.
		synchronized (this) {
		    while (streamHasData && !endOfData) {
			try {
			    wait(1000);
			} catch (InterruptedException e) {
			    return;
			}
		    }

		    localFrame = null;
		    if (retriever.rbnbAddCon == null) {
			// When there is only a single connection, then we just
			// use that one.
			while (!endOfData &&
			       (((localFrame =
				  retriever.getConnection().Fetch(0)) != null) &&
				localFrame.GetIfFetchTimedOut())) {
			    try {
				wait(1,0);
			    } catch (InterruptedException e) {
				setEndOfData(true);
			    }
			}

		    } else {
			// With two connections, the code tries to synchronize
			// the two by waiting for the connection that is
			// behind.  When we don't actually have a frame for one
			// of the two, we use the delta time between the last
			// two as a predictor of the next time.

			while (!endOfData &&
			       (frame[0] == null) &&
			       (frame[1] == null)) {
			    // With no frames at all, we'll wait for a frame to
			    // show up on one of the channels.
			    try {
				wait(1,0);
			    } catch (InterruptedException e) {
				setEndOfData(true);
			    }
			    frame[0] = retriever.getConnection().Fetch(0);
			    if (frame[0].GetIfFetchTimedOut()) {
				frame[0] = null;
			    } else {
				curTime[0] = frame[0].GetTimeStart(0);
			    }
			    frame[1] = retriever.rbnbAddCon.Fetch(0);
			    if (frame[1].GetIfFetchTimedOut()) {
				frame[1] = null;
			    } else {
				curTime[1] = frame[1].GetTimeStart(0);
			    }
			}

			if (frame[0] != null) {
			    // If we have a frame on the first connection, then
			    // we need to read the second until its time is
			    // past the first.  We'll always perform at least
			    // one read check.
			    do {
				frame[1] = retriever.rbnbAddCon.Fetch(0);
				if (frame[1].GetIfFetchTimedOut()) {
				    frame[1] = null;
				} else {
				    curTime[1] = frame[1].GetTimeStart(0);
				}
			    } while (!endOfData &&
				     (frame[1] == null) &&
				     (curTime[1] < curTime[0]));
			    
			} else if (frame[1] != null) {
			    // If we have a frame on the second connection,
			    // then we need to read the first until its time
			    // is past the second.  We'll always perform at
			    // least one read check.
			    do {
				frame[0] = retriever.getConnection().Fetch(0);
				if (frame[0].GetIfFetchTimedOut()) {
				    frame[0] = null;
				} else {
				    curTime[0] = frame[0].GetTimeStart(0);
				}
			    } while (!endOfData &&
				     (frame[0] == null) &&
				     (curTime[0] < curTime[1]));
			}

			if (!endOfData) {
			    // If we get here, we've got something to put out.

			    if (frame[0] == null) {
				// With no first frame, then we must have a
				// second frame.
				localFrame = frame[1];
				frame[1] = null;
				if (lastTime[1] != -Double.MAX_VALUE) {
				    // If there was a last time for the second
				    // channel, then use it to predict the
				    // arrival of the next frame.
				    delta = curTime[1] - lastTime[1];
				    lastTime[1] = curTime[1];
				    curTime[1] += delta;
				} else {
				    lastTime[1] = curTime[1];
				}

			    } else if (frame[1] == null) {
				// With no second frame, then we must have a
				// first frame.
				localFrame = frame[0];
				frame[0] = null;
				if (lastTime[0] != -Double.MAX_VALUE) {
				    // If there was a last time for the first
				    // channel, then use it to predict the
				    // arrival of the next frame.
				    delta = curTime[0] - lastTime[0];
				    lastTime[0] = curTime[0];
				    curTime[0] += delta;
				} else {
				    lastTime[0] = curTime[0];
				}

			    } else {
				// With both, we need to take the one that
				// came first.
				int idx;
				if (curTime[0] <= curTime[1]) {
				    idx = 0;
				} else {
				    idx = 1;
				}
				localFrame = frame[idx];
				frame[idx] = null;
				if (lastTime[idx] != -Double.MAX_VALUE) {
				    // If there was a last time for the chosen
				    // channel, then use it to predict the
				    // arrival of the next frame.
				    delta = curTime[idx] - lastTime[idx];
				    lastTime[idx] = curTime[idx];
				    curTime[idx] += delta;
				} else {
				    lastTime[idx] = curTime[idx];
				}
			    }
			}
		    }

		    streamFrame = (endOfData ? null : localFrame);
		    streamHasData = true;
		    notifyAll();
		}
	    }

	} catch (Exception e) {
	    synchronized (this) {
		streamHasData = true;
		streamFrame = null;
		runException = e;
		notifyAll();
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setDirection					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the direction of the move when the	***
  ***	command doesn't indicate it (SETFRAMEINDEX).		***
  ***								***
  ***	Input :							***
  ***	   directionI		The direction to move.		***
  ***								***
  *****************************************************************
*/
    final void setDirection(int directionI) {
	direction = directionI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setEndOfData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the end of data flag to the input	***
  ***	value.							***
  ***								***
  ***	Input :							***
  ***	   endOfDataI		At end of data?			***
  ***								***
  *****************************************************************
*/
    final synchronized void setEndOfData(boolean endOfDataI) {
	endOfData = endOfDataI;
	notifyAll();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setStartTime					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the start time to the specified	***
  ***	value.							***
  ***								***
  ***	Input :							***
  ***	   startTimeI		The new start time.		***
  ***								***
  ***	Modification History :					***
  ***	   08/17/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.						***
  ***								***
  *****************************************************************
*/
    final void setStartTime(double startTimeI) {
	startTime = startTimeI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setStartTime					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the start time based on the input	***
  ***	values.							***
  ***								***
  ***	Input :							***
  ***	   timesI		The times to use to set the	***
  ***				start time.			***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    final void setStartTime(double[] timesI) {

	// If there are times, then we may want to set the time.
	if ((timesI != null) && (timesI[0] != -Double.MAX_VALUE)) {
	    startTime = timesI[0];
	    endTime = timesI[2];
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setTimes					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2002, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the times for this video thread	***
  ***	based on the input map. It also ensures that the right	***
  ***	amount of time passes.					***
  ***								***
  ***	Input :							***
  ***	   mapI			The map to work from.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    final synchronized void setTimes(ChannelMap mapI) throws Exception {
	ChannelMap primaryMap = retriever.getPrimaryMap();

	// Determine which channel should be used to find the frame rate.
	if (rateChannel == null) {
	    rateChannel = primaryMap.GetName(0);
	}

	// If we get an empty map, we're done.
	if (mapI == null) {
	    setEndOfData(true);
	    return;
	}

	// Get the list of channels.
	if (mapI.NumberOfChannels() == 0) {
	    setEndOfData(true);
	    return;
	}

	// Determine if we need a wait before the next frame is sent out.
	// NOTE: we're trying to allow the audio to pace things, which we
	// believe it will do if it has a higher frame rate than the video.  If
	// the opposite is true, we may want to be doing sleeps.
	boolean gotTimes = false,
	    needWait =
	    (flags == RUNBACKWARD) || (flags == RUNFORWARD);
	    /*
	    ((flags == RUNFORWARD) &&
	     (retriever.dataSource.getAudioChannels() != null));
	    */

	// Clear the times.
	times = new double[4];
	for (int idx = 0; idx < times.length; ++idx) {
	    times[idx] = -Double.MAX_VALUE;
	}
	rateTime = -Double.MAX_VALUE;

	// Find the limits of the map in terms of times and frames.
	for (int idx = 0; idx < mapI.NumberOfChannels(); ++idx) {
	    double firstFrame = mapI.GetTimeStart(idx);
	    double lastFrame =
		mapI.GetTimeStart(idx) + mapI.GetTimeDuration(idx);
	    gotTimes = true;

	    if ((times[0] == -Double.MAX_VALUE) || (firstFrame < times[0])) {
		times[1] =
		    times[0] = firstFrame;
	    }
	    if ((times[2] == -Double.MAX_VALUE) || (lastFrame > times[2])) {
		times[3] =
		    times[2] = lastFrame;
	    }

	    // Determine if this is the rate time channel. If so, save the
	    // start time.
	    if (mapI.GetName(idx).equals(rateChannel)) {
		rateTime = startTime;
	    }
	}

	// If no times were found, we're done.
	if (!gotTimes) {
	    setEndOfData(true);
	    return;
	}

	// If the startedAt fields have not been set, set them now. The values
	// are the start or end time of the current frame and the current wall
	// clock time. We do not do this for single frame or real time
	// requests.
	waitTime = -Double.MAX_VALUE;
	if (startedAt == null) {
	    startedAt = new double[2];
	    startedAt[0] = System.currentTimeMillis()/1000.;

	    // If we're going backwards, we want the end time of the frame.
	    if ((flags == RUNBACKWARD) || (flags == STEPBACKWARD)) {
		startedAt[1] = times[3];
	    } else {
		// If we are running forwards, we want the start time of the
		// frame.
		startedAt[1] = times[1];
	    }

	} else if (needWait) {
	    // Otherwise, we want to sleep until the right duration has passed.
	    double elapsedFrame;

	    // Determinate the "elapsed" time between this frame and the first
	    // one.
	    if (direction <= BACKWARD) {
		elapsedFrame = startedAt[1] - times[3];
	    } else {
		elapsedFrame = times[1] - startedAt[1];
	    }

	    // Determine the wall clock elapsed time.
	    double elapsedReal =
		System.currentTimeMillis()/1000. - startedAt[0];

	    // If the real time elapsed is less than the frame elapsed time,
	    // sleep for the difference.
	    double difference = elapsedFrame - elapsedReal;

	    if (difference > 0.) {
		waitTime = difference*1000.;
	    }
	}

	/*
	if (flags != oldFlags) {
	    System.err.println("\n\n---");
	    oldFlags = flags;
	}
	System.err.println("Flags: " + flags);
	System.err.println(mapI);
	System.err.println(times[0] + " - " + times[2]);
	*/
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setTrackingSlider				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the tracking slider flag to the	***
  ***	input value.						***
  ***								***
  ***	Input :							***
  ***	   trackingSliderI	Are we tracking the slider?	***
  ***								***
  *****************************************************************
*/
    final void setTrackingSlider(boolean trackingSliderI) {
	trackingSlider = trackingSliderI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	start						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts the request.			***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				***
  ***								***
  *****************************************************************
*/
    final void start() throws Exception {

	// We're not at the end of the data yet.
	setEndOfData(false);
	startedAt = null;

	// We should switch into stream mode for RUNFORWARD and REALTIME modes.
	if ((flags == RUNFORWARD) || (flags == REALTIME)) {
	    retriever.streaming = true;
	    if (flags == RUNFORWARD) {
		ChannelMap lMap = new ChannelMap();

		// For RUNFORWARD, we need to separate and synchronize the
		// video and audio channels.  We cannot do this with more than
		// two channels.  In that case, we'll simply run them out of
		// sync.
		if (retriever.getMap().NumberOfChannels() != 2) {
		    for (int idx = 0;
			 idx < retriever.getMap().NumberOfChannels();
			 ++idx) {
			lMap.Add(retriever.getMap().GetName(idx));
		    }
		    retriever.getConnection().Subscribe
			(lMap,
			 startTime,
			 0.,
			 "next");
		} else if (retriever.getMap().NumberOfChannels() == 2) {
		    // playback via subscribe mode just works better with dual sinks
		    retriever.rbnbAddCon = new Sink();
		    retriever.rbnbAddCon.OpenRBNBConnection
			(retriever.serverAddress,
			 retriever.getConnection().GetClientName());

		    lMap.Add(retriever.getMap().GetName(0));
		    retriever.getConnection().Subscribe
			(lMap,
			 startTime,
			 0.,
			 "next");

		    //lMap = new ChannelMap();
		    lMap.Clear();
		    lMap.Add(retriever.getMap().GetName(1));
		    retriever.rbnbAddCon.Subscribe
			(lMap,
			 startTime,
			 0.,
			 "next");
		}

	    } else {
		/*
		 * There is no time-based monitor mode, so we'll do this using
		 * the standard monitor mode.
		 */
		ChannelMap lMap = new ChannelMap();
		for (int idx = 0;
		     idx < retriever.getMap().NumberOfChannels();
		     ++idx) {
		    lMap.Add(retriever.getMap().GetName(idx));
		}
		retriever.getConnection().Monitor(lMap,1);
	    }
	    (new Thread(this)).start();

	} else {
	    // Otherwise, we're running request/response mode. In this case, we
	    // may want to keep the limits and information about the last frame
	    // retrieved.
	    times = new double[4];
	    for (int idx = 0; idx < times.length; ++idx) {
		times[idx] = -Double.MAX_VALUE;
	    }

	    if (retriever.streaming) {
		// If we were streaming, we need to reconnect because streams
		// are not currently interruptable in a reliable fashion.  This
		// "fact" should be confirmed at some point.
		retriever.streaming = false;
		String name = retriever.getConnection().GetClientName();
		retriever.getConnection().CloseRBNBConnection();
		retriever.getConnection().OpenRBNBConnection
		    (retriever.serverAddress,name);
	    }
	}
	active = true;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stop						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called to stop the run. It terminates	***
  ***	any running stream.					***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    final void stop() throws Exception {
	setEndOfData(true);
	active = false;

	if (retriever.streaming) {
	    // If we were streaming, then terminate the stream.
	    retriever.streaming = false;
	    String name = retriever.getConnection().GetClientName();
	    retriever.getConnection().CloseRBNBConnection();
	    retriever.getConnection().OpenRBNBConnection
		(retriever.serverAddress,name);
	    if (retriever.rbnbAddCon != null) {
		retriever.rbnbAddCon.CloseRBNBConnection();
		retriever.rbnbAddCon = null;
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stopStream					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called to stop a stream mode run.	***
  ***	It is necessary because a real-time stream can lock if	***
  ***	the DataTurbine source application stops sending data,	***
  ***	but doesn't detach or disconnect.			***
  ***								***
  *****************************************************************
*/
    final void stopStream() {
	if ((flags == REALTIME) || (flags == RUNFORWARD)) {
	    setEndOfData(true);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	toString					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Return a string representation of this request.	***
  ***								***
  ***	Returns :						***
  ***	   toString		The string representation.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    public final String toString() {
	String stringR = "VideoRequest: [" + retriever.getMap().toString();
	stringR += startTime + " - " + duration + " ";
	stringR +=
	    ((flags == BEGINNINGOFDATA) ? "bod" :
	     (flags == ENDOFDATA) ? "eod" :
	     (flags == PAUSE) ? "pause" :
	     (flags == REALTIME) ? "rt" :
	     (flags == RUNBACKWARD) ? "rplay" :
	     (flags == RUNFORWARD) ? "play" :
	     (flags == SETFRAMEINDEX) ? "sfi" :
	     (flags == STEPBACKWARD) ? "back" :
	     (flags == STEPFORWARD) ? "fwd" :
	     (flags == TERMINATE) ? "terminate" :
	     "?") + " (" + flags + ")";

	return (stringR);
    }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	RadioListener					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides an action listener to handle	***
  ***	the radio buttons.					***
  ***								***
  *****************************************************************
*/
class RadioListener implements ActionListener {

    // Private fields:
    private VideoRetriever	retriever = null;

/*
  *****************************************************************
  ***								***
  ***	Name :	RadioListener					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a radio listener for the	***
  ***	specified video retriever.				***
  ***								***
  ***	Input :							***
  ***	   retrieverI		The video retriever.		***
  ***								***
  *****************************************************************
*/
    RadioListener(VideoRetriever retrieverI) {
	retriever = retrieverI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	actionPerformed					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method performs an action whenever the user	***
  ***	presses one of the buttons.				***
  ***								***
  ***	Input :							***
  ***	   eventI		The action event.		***
  ***								***
  *****************************************************************
*/
    public final void actionPerformed(ActionEvent eventI) {
	String command = eventI.getActionCommand();

	if (command.equals(retriever.actions
	    [VideoRetriever.BEGINNINGOFDATA])) {
	    retriever.beginningOfData(false);
	} else if (command.equals
		   (retriever.actions[VideoRetriever.PLAYBACKWARD])) {
	    retriever.runBackward();
	} else if (command.equals
		   (retriever.actions[VideoRetriever.STEPBACKWARD])) {
	    retriever.stepBackward();
	} else if (command.equals(retriever.actions[VideoRetriever.PAUSE])) {
	    retriever.pause();
	} else if (command.equals
		   (retriever.actions[VideoRetriever.STEPFORWARD])) {
	    retriever.stepForward();
	} else if (command.equals
		   (retriever.actions[VideoRetriever.PLAYFORWARD])) {
	    retriever.runForward();
	} else if (command.equals
		   (retriever.actions[VideoRetriever.ENDOFDATA])) {
	    retriever.endOfData(false);
	} else if (command.equals
		   (retriever.actions[VideoRetriever.REALTIME])) {
	    retriever.realTime();
	}
    }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	SliderListener					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides a listener for the slider that	***
  ***	can be used to control the position of the display in	***
  ***	the video retriever.					***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
class SliderListener implements ChangeListener {
    private boolean		wasTrackingSlider = false;
    private VideoRetriever	retriever = null;

/*
  *****************************************************************
  ***								***
  ***	Name :	SliderListener					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a slider listener for the	***
  ***	specified video retriever.				***
  ***								***
  ***	Input :							***
  ***	   retrieverI		The video retriever.		***
  ***								***
  *****************************************************************
*/
    SliderListener(VideoRetriever retrieverI) {
	retriever = retrieverI;
    }
    

/*
  *****************************************************************
  ***								***
  ***	Name :	stateChanged					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called whenever the state of the	***
  ***	slider has changed. It repositions the video retriever	***
  ***	to the appropriate frame.				***
  ***								***
  ***	Input :							***
  ***	   eventI		The change event.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    public final void stateChanged(ChangeEvent eventI) {

	// If the retriever is just moving the slider, skip the event.
	if (retriever.getMovingSlider()) {
	    retriever.setMovingSlider(false);
	    return;
	}

	// Grab the source of the event.
	JSlider slider = (JSlider) eventI.getSource();

	// Let the video retriever know if the slider is moving or has stopped.
	boolean trackingSlider = slider.getValueIsAdjusting();

	// If the slider is at the minimum position, go to the beginning of
	// data.
	if (slider.getValue() == slider.getMinimum()) {
	    retriever.beginningOfData(trackingSlider || wasTrackingSlider);

	} else if (slider.getValue() == slider.getMaximum()) {
	    // If the slider is at the maximum position, go to the end of the
	    // data.
	    retriever.endOfData(trackingSlider || wasTrackingSlider);

	} else {
	    // Calculate the position of the slider in terms of frames.
	    double percentage =
		((double) slider.getValue() - slider.getMinimum())/
		(slider.getMaximum() - slider.getMinimum()),
		minimum = retriever.getMinimumFrame(),
		maximum = retriever.getMaximumFrame();
	    long   position = (long)
		(Math.round(minimum + percentage*(maximum - minimum)));

	    // Reposition the retriever.
	    retriever.setFrameIndex
		(position,
		 trackingSlider || wasTrackingSlider);
	}

	// Save the tracking slider value for next time. The reason we do this
	// is because when the button is released on the slider, one more event
	// happens with the current tracking slider value being false. This
	// event is really still part of the tracking slider events, so we
	// don't want/ to generate a moveSlider action.
	wasTrackingSlider = trackingSlider;
    }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	DataSource					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides a JMF data source that gets	***
  ***	data in the form of maps from a DataTurbine. The source	***
  ***	is provided data from the outside.			***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
class DataSource extends PushBufferDataSource {

    // Private fields:
    private int			state = DISCONNECTED;

    private ChannelMap		registeredMap = null;

    private Object[]		controls = new Object[0];

    private RBNBJMFSourceStream[]
				streams = null;

    private SortedVector	streamChannels = null;

    private String		contentType = "raw";

    private String[]		videoChannels = null,
				audioChannels = null;

    private javax.media.Time	duration = DURATION_UNKNOWN;

    private VideoRetriever	parent = null;

    // Private constants:
    private final static int	DISCONNECTED = 0,
				CONNECTED = 1,
				MAKESTREAMS = 2,
				STREAMS = 3,
				STARTED = 4;

/*
  *****************************************************************
  ***								***
  ***	Name :	DataSource					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This is the default constructor for RBNB to JMF	***
  ***	data source objects.					***
  ***								***
  *****************************************************************
*/
    DataSource() {
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	connect						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method connects this data source. It just marks	***
  ***	it as connected.					***
  ***								***
  *****************************************************************
*/
    public final void connect() {
	synchronized (this) {
	    if (state == DISCONNECTED) {
		state = CONNECTED;
		notifyAll();
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	destroyStreams					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method destroys the push buffer streams that	***
  ***	are used to pass data from the DataTurbine to JMF.	***
  ***								***
  *****************************************************************
*/
    private synchronized final void destroyStreams() {

	// If the streams are running, stop them.
	if (state == STARTED) {
	    try {
		stop();
	    } catch (IOException e) {
		e.printStackTrace();
		return;
	    }
	}

	// If there are streams, destroy them.
	if (state == STREAMS) {

	    // Clear out the stream objects.
	    streams = null;
	    streamChannels = null;

	    // We are back to just a connected state.
	    state = CONNECTED;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	disconnect					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method disconnects this data source.		***
  ***								***
  *****************************************************************
*/
    public final void disconnect() {
	synchronized (this) {

	    // Destroy the streams.
	    destroyStreams();

	    // Disconnect.
	    state = DISCONNECTED;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getAudioChannels				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the list of audio channels.	***
  ***								***
  ***	Returns :						***
  ***	   getAudioChannels	The list of audio channels.	***
  ***								***
  *****************************************************************
*/
    public final String[] getAudioChannels() {
	return (audioChannels);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getContentType					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the content type for this	***
  ***	source.							***
  ***								***
  ***	Returns :						***
  ***	   getContentType	The source content type.	***
  ***								***
  *****************************************************************
*/
    public final String getContentType() {
	return (contentType);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getControl					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the control object for the	***
  ***	specified control type.					***
  ***								***
  ***	Input :							***
  ***	   controlTypeI		The desired control type.	***
  ***								***
  ***	Returns :						***
  ***	   getControl		The control object or null.	***
  ***								***
  *****************************************************************
*/
    public final Object getControl(String controlTypeI) {
	Object controlR = null;

	try {

	    // Find the class associated with the control type.
	    Class	controlClass = Class.forName(controlTypeI);

	    // Search our controls for an instance of that class.
	    for (int idx = 0; idx < controls.length; ++idx) {
		if (controlClass.isInstance(controls[idx])) {
		    controlR = controls[idx];
		}
	    }

	    // Return null if we cannot find the specified control type.
	} catch (Exception e) {
	}

	return (controlR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getControls					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the controls associated with	***
  ***	this DataTurbine data source.				***
  ***								***
  ***	Returns :						***
  ***	   getControls		The list of controls.		***
  ***								***
  *****************************************************************
*/
    public final Object[] getControls() {
	return (controls);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getDuration					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the duration of the data.	***
  ***								***
  ***	Returns :						***
  ***	   getDuration		The duration of the data.	***
  ***								***
  *****************************************************************
*/
    public final javax.media.Time getDuration() {
	return (duration);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getVideoChannels				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the list of video channels.	***
  ***								***
  ***	Returns :						***
  ***	   getVideoChannels	The list of video channels.	***
  ***								***
  *****************************************************************
*/
    public final String[] getVideoChannels() {
	return (videoChannels);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getStreams					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the streams for this data	***
  ***	source. The DataTurbine connection must be established	***
  ***	for this to work.					***
  ***								***
  ***	Returns :						***
  ***	   getStreams		The push buffer streams.	***
  ***								***
  *****************************************************************
*/
    public final PushBufferStream[] getStreams() {
	try {

	    // If necessary make the streams.
	    makeStreams();
	    return (streams);

	    // On an exception, return null.
	} catch (Exception e) {
	    e.printStackTrace();
	    return (null);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	makeStreams					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method makes push buffer streams to pass data	***
  ***	received from the DataTurbine to JMF.			***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    private synchronized final void makeStreams() throws Exception {
	Vector audio = new Vector(),
	       video = new Vector();

	// If we are not connected, throw an exception.
	if (state == DISCONNECTED) {
	    notifyAll();
	    throw new IOException
		("Cannot make streams when not connected to the DataTurbine.");

	} else if (state == MAKESTREAMS) {
	    // If we are already making the streams, wait.
	    while (state == MAKESTREAMS) {
		try {
		    wait(1000);
		} catch (InterruptedException e) {
		}
	    }

	} else if (state == CONNECTED) {
	    // If the streams don't exist, create them.
	    state = MAKESTREAMS;

	    // Get the list of channels from the registered map.

	    // Create a stream for each channel in the map.
	    streams = new RBNBJMFSourceStream
		[registeredMap.NumberOfChannels()];
	    streamChannels =
		new SortedVector(registeredMap.NumberOfChannels());
	    for (int idx = 0; idx < registeredMap.NumberOfChannels(); ++idx) {
		StreamChannel sChannel =
		    new StreamChannel(parent.getConnection(), registeredMap,idx);

		streamChannels.add(sChannel);
		streams[idx] = sChannel.getStream();
		if (streams[idx].isAudioTrack()) {
		    audio.addElement(registeredMap.GetName(idx));
		} else {
		    video.addElement(registeredMap.GetName(idx));
		}
	    }

	    audioChannels = new String[audio.size()];
	    audio.copyInto(audioChannels);
	    videoChannels = new String[video.size()];
	    video.copyInto(videoChannels);

	    // We now have streams.
	    state = STREAMS;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	putMap						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method processes the input map by determining	***
  ***	what is in it:						***
  ***								***
  ***	o - an empty map marks the end of the data, force all	***
  ***	    of the streams to stop once they have delivered any	***
  ***	    data in them.					***
  ***								***
  ***	o - a map with channels is processed by retrieving each	***
  ***	    channel, matching it to a channel name in the	***
  ***	    stream channels list, and passing the channel to	***
  ***	    the corresponding stream.				***
  ***								***
  ***	Input :							***
  ***	   mapI			The map to process.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    final void putMap(ChannelMap mapI) {

	synchronized (this) {
	    try {
		// If we aren't running, ignore the frame.
		if (state != STARTED) {
		    return;
		}

		// If the map is empty, we're done.
		if ((mapI == null) || (mapI.NumberOfChannels() == 0)) {
		    for (int idx = 0;
			 idx < streamChannels.size();
			 ++idx) {
			StreamChannel sChannel = (StreamChannel)
			    streamChannels.elementAt(idx);

			// Mark the channel as done.
			sChannel.done();
		    }

		    // We're now done getting data.
		    state = STREAMS;
		    notifyAll();

		} else {
		    // Otherwise, pass each channel to the appropriate stream.

		    for (int idx = 0;
			 idx < mapI.NumberOfChannels();
			 ++idx) {
			StreamChannel sChannel = (StreamChannel)
			    streamChannels.find(mapI.GetName(idx));

			sChannel.stream.startedAt =
			    parent.currentRequest.startedAt[1];
			sChannel.passData(mapI,idx);
		    }
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setParent					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets our parent video retriever to the	***
  ***	input value.						***
  ***								***
  ***	Input :							***
  ***	   parentI		Our parent.			***
  ***								***
  *****************************************************************
*/
    final void setParent(VideoRetriever parentI) {
	parent = parentI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setRegisteredMap				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the registered map of channels to	***
  ***	the input value.					***
  ***								***
  ***	Input :							***
  ***	   registeredMapI	The new registered map.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    void setRegisteredMap(ChannelMap registeredMapI) {
	registeredMap = registeredMapI;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	start						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method starts getting data from the DataTurbine	***
  ***	based on the current channel path.			***
  ***								***
  *****************************************************************
*/
    public final void start() throws IOException {
	synchronized (this) {

	    // It is an error to be in a disconnected state at this point.
	    if (state == DISCONNECTED) {
		throw new Error
		    ("DataSource must be connected before it can be started.");

	    } else if (state != STARTED) {
		// If we are already started, we need do nothing more.
		// If the streams do not yet exist, determine what they should
		// be.
		try {
		    makeStreams();
		} catch (Exception e) {
		    throw new IOException(e.getMessage());
		}

		// Note that we are now started.
		state = STARTED;
	    }

	    // Start our parent's request.
	    try {
		if ((parent.getRequest() != null) &&
		    !parent.getRequest().isActive()) {
		    parent.getRequest().start();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	stop						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method stops the streams that are passing data	***
  ***	from the DataTurbine to JMF.				***
  ***								***
  *****************************************************************
*/
    public final void stop() throws IOException {
	synchronized (this) {
	    try {

		// If the data has started flowing, terminate it.
		if (state == STARTED) {
		    // We still have streams, but no data is flowing.
		    state = STREAMS;
		    notifyAll();
		}

		// Stop our parent's request.
		if (parent.getRequest() != null) {
		    parent.getRequest().stop();
		}

	    } catch (Exception e) {
		e.printStackTrace();
		disconnect();
		throw new IOException(e.getMessage());
	    }
	}
    }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	StreamChannel					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class ties channel names to push buffer streams	***
  ***	using a sortable interface.				***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
class StreamChannel implements Runnable,SortCompareInterface {

    // Private fields:
    private boolean		haveData = false;

    private String		cName = null;

    private ChannelMap		map = null;
    private int			channelToPass = -1;

    final RBNBJMFSourceStream stream;

    private Thread		thread = null;

    // Private constants:

    // The stream channel code can have an extra thread to separate accepting
    // new data from passing it on to the stream belonging to this channel.
    // Using the extra thread appears to introduce synchronization problems,
    // so it is turned off at this time.
    private final static boolean	useThread = false;
  
/*
  *****************************************************************
  ***								***
  ***	Name :	StreamChannel					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a stream channel from the	***
  ***	input DataTurbine API channel.				***
  ***								***
  ***	Input :							***
  ***	   mapI			The channel map.		***
  ***	   channelI		The DataTurbine channel.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				***\
  		2004/12/30  WHF  Added a connection parameter, for use in dynamic
				JPEG type identification.
  ***								***
  *****************************************************************
*/
    StreamChannel(Sink con, ChannelMap mapI,int channelI) throws Exception
	{
		// Get the channel name.
		cName = mapI.GetName(channelI);
	
		// Create the stream.
		stream = new RBNBJMFSourceStream(con, mapI,channelI);
    }

/*
  *****************************************************************
  ***								***
  ***   Name :	compareTo					***
  ***   By   :	Ian Brown	(Creare Inc., Hanover, NH)      ***
  ***   For  :	DataTurbine					***
  ***   Date :	September, 2000					***
  ***								***
  ***   Copyright 2000, 2004 Creare Inc.			***
  ***   All Rights Reserved					***
  ***								***
  ***   Description :						***
  ***	   This method performs a comparison between this	***
  ***	object and the input value.				***
  ***								***
  ***   Input :							***
  ***	   identifierI		The sort field identifier.	***
  ***	   otherI		The other object.		***
  ***								***
  ***   Returns :						***
  ***	   compareTo		The result:			***
  ***				    <0 this object is first.	***
  ***				     0  objects are equal.	***
  ***				    >0 this object is last.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Channel names are strings.			***
  ***								***
  *****************************************************************
*/
    public final int compareTo(Object identifierI,Object otherI)
	throws SortException
    {
	String mySort = (String) sortField(identifierI),
	    theirSort = (String) otherI;

	return (mySort.compareTo(theirSort));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	done						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method marks the stream as done.		***
  ***								***
  *****************************************************************
*/
    final void done() throws Exception {
	passData(null,-1);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getStream					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the stream associated with this	***
  ***	stream channel.						***
  ***								***
  ***	Returns :						***
  ***	   getStream		The push buffer stream.		***
  ***								***
  *****************************************************************
*/
    final RBNBJMFSourceStream getStream() {
	return (stream);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	passData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method passes a channel of data to the stream	***
  ***	associated with this stream channel.			***
  ***								***
  ***	Input :							***
  ***	   mapI			The channel map.		***
  ***	   channelI		The DataTurbine channel.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    final synchronized void passData(ChannelMap mapI,int channelI)
	throws Exception
    {

	// If we are not using the extra thread, pass the data directly to the
	// stream.
	if (!useThread) {
	    stream.passData(mapI,channelI);

	} else {
	    // Otherwise, synchronize with the other thread and pass the
	    // channel when the other thread is ready to accept it.

	    // If there is no thread running, then start one.
	    if (thread == null) {
		thread = new Thread(this);
		thread.start();
	    }

	    // Wait for there to be a place to put the data.
	    while (haveData) {
		try {
		    wait();
		} catch (InterruptedException e) {
		    return;
		}
	    }

	    // Pass the data to the running thread.
	    map = mapI;
	    channelToPass = channelI;
	    haveData = true;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	run						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method actually transfers data to the JMF	***
  ***	player for viewing.					***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    public final void run() {

	// Loop until we get an empty frame.
	ChannelMap lMap = null;
	int channelPassed = -1;

	do {

	    // Wait for some data.
	    synchronized (this) {
		while (!haveData) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			thread = null;
			return;
		    }
		}

		// Grab the channel frame.
		lMap = map;
		channelPassed = channelToPass;
		map = null;
		channelToPass = -1;
		haveData = false;
		notifyAll();
	    }

	    // Pass the frame to the stream.
	    stream.passData(lMap,channelPassed);
	} while (channelPassed != -1);

	// Note that we're no longer running.
	thread = null;
    }

/*
  *****************************************************************
  ***								***
  ***   Name :	sortField					***
  ***   By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***   For  :	DataTurbine					***
  ***   Date :	September, 2000					***
  ***								***
  ***   Copyright 2000 Creare Inc.				***
  ***   All Rights Reserved					***
  ***								***
  ***   Description :						***
  ***	   This field returns the sort field for this channel	***
  ***	based on the identifier.				***
  ***								***
  ***   Input :							***
  ***	   identifierI		The sort field identifier. This ***
  ***				should be null.			***
  ***								***
  ***   Returns :						***
  ***	   sortField		The sort field value - this is  ***
  ***				the cName object.		***
  ***								***
  *****************************************************************
*/
    public final Object sortField(Object identifierI)
	throws SortException {

	// We don't allow more than one identifier.
	if (identifierI != null) {
	    throw new SortException
		("Stream channel objects do not allow for multiple sort " +
		 "identifiers.");
	}

	return (cName);
    }
}

/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBJMFSourceStream				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August - September, 2000			***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This file contains the JMF push buffer stream class	***
  ***	for handling RBNB DataTurbine data.			***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
class RBNBJMFSourceStream implements PushBufferStream {

    // Private fields:
    private boolean		markDone = false,
				isAudio = false;

    private BufferTransferHandler
				transferHandler = null;

    private ChannelMap		map = null;
    private int		        channelToRead = -1;

    private ContentDescriptor	cd = new ContentDescriptor
	(ContentDescriptor.RAW);

    private Format		format = null;

    private int			maxDataLength,
				sequenceNumber = 0;

    private Object[]		controls = new Object[0];

    private String		channelName = null;

    double			startedAt = -Double.MAX_VALUE;
	
	private final Sink con;

/*
  *****************************************************************
  ***								***
  ***	Name :	RBNBJMFSourceStream				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a source stream to handle	***
  ***	the specified channel. The channel should have user	***
  ***	data explaining what it is. If not, the constructor	***
  ***	throws an exception because it doesn't know what to do	***
  ***	with the data.						***
  ***								***
  ***	Input :							***
  ***	   mapI			The channel map.		***
  ***	   channelI		The channel to work on.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    public RBNBJMFSourceStream(Sink conI, ChannelMap mapI,int channelI)
	throws Exception
    {		
		con = conI;
		
		// Save the channel name.
		channelName = mapI.GetName(channelI);
		
		// See if there is user data of the correct type.
		// 2005/03/23  WHF  Now use 'User Info':
		/*
		String[] uData = mapI.GetDataAsString(channelI);
		if (uData == null) {
			throw new Exception
			("No user data found for " + channelName +
			 ", cannot identify channel.");
		} else if (uData.length != 1) {
			throw new Exception
			("Bad user data found for " + channelName +
			 ", cannot identify channel.");
		}
		
		// Process the user data to build a format.
		// 2004/12/30  WHF  Also include MIME type.
		identifyUserData(uData[0], mapI.GetMime(channelI));
		*/
		String uInfo = mapI.GetUserInfo(channelI);
System.err.println("User info!! "+uInfo);
		identifyUserData(uInfo, mapI.GetMime(channelI));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	identifyUserData				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method parses out the user data into a format	***
  ***	to use for JMF. The user data should contain an		***
  ***	encoding type and then encoding specific information	***
  ***	for that.						***
  ***								***
  ***	Input :							***
  ***	   uDataI		The user data to identify.	***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB	 The user data is a string.
       	   2004/12/30  WHF  added MIME type as an added input.
           2005/02/09  WHF  Also uses extension to determine action.
  ***								***
  *****************************************************************
*/
	private final void identifyUserData(String uDataI, String mime) 
	throws Exception
	{
		// Hash the user data.
		KeyValueHash kvh = new KeyValueHash(uDataI.getBytes());
		
		// Find the content.
		String content = kvh.get("content");
		
		// If the content, that is an error.
		if (content == null && mime == null) {
			throw new Exception
			("No content found in user data for channel " +
			 channelName +
			 ".");
		}
		if ("text/xml".equals(mime)) { // server registration data, may contain
			// mime field.
			int mimeI = uDataI.indexOf("<mime>");
			if (mimeI != -1) {
				mime = uDataI.substring(mimeI+"<mime>".length());
				mimeI = mime.indexOf("</mime>");
				if (mimeI != -1) {
					mime = mime.substring(0, mimeI).trim();
				}
			}
		}

		// Handle this based on the content.
		if ("Audio".equalsIgnoreCase(content)) {
			audioUserData(kvh);
		} else if ("audio/basic".equals(mime)) {
			audioBasicData();
		} else if ("Video".equalsIgnoreCase(content)) {
			videoUserData(kvh);
		} else if ("image/jpg".equals(mime) || "image/jpeg".equals(mime)) {
			videoJpgData();
		} else {
			String lcn = channelName.toLowerCase();
			if (lcn.endsWith(".jpg") || lcn.endsWith(".jpeg")) {
				videoJpgData();
			} else {
				throw new Exception(
						"Cannot handle content \"" + content +
						 "\" or MIME type \""+ mime + "\" for channel " 
						 + channelName + ".");
			}
		}
	}

/*
  *****************************************************************
  ***								***
  ***	Name :	audioUserData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August - September, 2000			***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method handles audio user data by parsing it	***
  ***	into an audio format field.				***
  ***								***
  ***	Input :							***
  ***	   kvhI			The key value hash of the	***
  ***				audio user data.		***
  ***								***
  *****************************************************************
  */
    private final void audioUserData(KeyValueHash kvhI) throws Exception {
	int fsb;

	// Get the audio format.
	isAudio = true;
	format = new AudioFormat
	    (kvhI.get("encoding"),
	     Double.parseDouble(kvhI.get("samplerate")),
	     Integer.parseInt(kvhI.get("samplesize")),
	     Integer.parseInt(kvhI.get("channels")),
	     Integer.parseInt(kvhI.get("endian")),
	     Integer.parseInt(kvhI.get("signed")),
	     fsb = Integer.parseInt(kvhI.get("framesize")),
	     Double.parseDouble(kvhI.get("framerate")),
	     Format.byteArray);
System.err.println("-- Extracted audio format: " + format); 
	// Calculate the maximum data length from the frame size.
	maxDataLength = fsb/8;
    }
	
	/**
	  * Calculates format for the "audio/basic" MIME type.
	  * @author WHF
	  * @since V2.6
	  * @version 2004/12/30
	  */
	// 2004/12/30  WHF  Created.
	private void audioBasicData()
	{
		isAudio = true;
		format = new AudioFormat(
				AudioFormat.LINEAR,
				8000, // kHz
				8, // bit
				1, // mono
				AudioFormat.BIG_ENDIAN, // whatever, its 8-bit
				AudioFormat.UNSIGNED
		);
				
		maxDataLength = -1; // doesn't appear to be used.
	}

/*
  *****************************************************************
  ***								***
  ***	Name :	videoUserData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August - September, 2000			***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method handles vidio user data by parsing it	***
  ***	into an video format field.				***
  ***								***
  ***	Input :							***
  ***	   kvhI			The key value hash of the	***
  ***				video user data.		***
  ***								***
  *****************************************************************
*/
    private final void videoUserData(KeyValueHash kvhI) throws Exception 
	{
		// Handle this based on the encoding.
		String encoding = kvhI.get("encoding");
		isAudio = false;
		
		if (encoding.equalsIgnoreCase(VideoFormat.H261) &&
			(kvhI.get("stillimage") != null)) {
			format = new H261Format
			(new java.awt.Dimension
				(Integer.parseInt(kvhI.get("width")),
				 Integer.parseInt(kvhI.get("height"))),
			 maxDataLength = Integer.parseInt(kvhI.get("maxlength")),
			 Format.byteArray,
			 Float.parseFloat(kvhI.get("framerate")),
			 Integer.parseInt(kvhI.get("stillimage")));
		} else if (encoding.equalsIgnoreCase(VideoFormat.H263) &&
			   (kvhI.get("advancedprediction") != null)) {
			format = new H263Format
			(new java.awt.Dimension
				(Integer.parseInt(kvhI.get("width")),
				 Integer.parseInt(kvhI.get("height"))),
			 maxDataLength = Integer.parseInt(kvhI.get("maxlength")),
			 Format.byteArray,
			 Float.parseFloat(kvhI.get("framerate")),
			 Integer.parseInt(kvhI.get("advancedprediction")),
			 Integer.parseInt(kvhI.get("arithmeticcoding")),
			 Integer.parseInt(kvhI.get("errorcompenstation")),
			 Integer.parseInt(kvhI.get("hrdb")),
			 Integer.parseInt(kvhI.get("pbframes")),
			 Integer.parseInt(kvhI.get("unrestrictedvector")));
		} else if (encoding.equalsIgnoreCase(VideoFormat.JPEG) &&
			   (kvhI.get("qfactor") != null)) {
			format = new JPEGFormat
			(new java.awt.Dimension
				(Integer.parseInt(kvhI.get("width")),
				 Integer.parseInt(kvhI.get("height"))),
			 maxDataLength = Integer.parseInt(kvhI.get("maxlength")),
			 Format.byteArray,
			 Float.parseFloat(kvhI.get("framerate")),
			 Integer.parseInt(kvhI.get("qfactor")),
			 Integer.parseInt(kvhI.get("decimation")));
		} else if (encoding.equalsIgnoreCase(VideoFormat.RGB)) {
			format = new RGBFormat
			(new java.awt.Dimension
				(Integer.parseInt(kvhI.get("width")),
				 Integer.parseInt(kvhI.get("height"))),
			 maxDataLength = Integer.parseInt(kvhI.get("maxlength")),
			 Format.byteArray,
			 Float.parseFloat(kvhI.get("framerate")),
			 Integer.parseInt(kvhI.get("bpp")),
			 Integer.parseInt(kvhI.get("red")),
			 Integer.parseInt(kvhI.get("green")),
			 Integer.parseInt(kvhI.get("blue")),
			 Integer.parseInt(kvhI.get("pixel")),
			 Integer.parseInt(kvhI.get("line")),
			 Integer.parseInt(kvhI.get("flipped")),
			 Integer.parseInt(kvhI.get("endian")));
		} else {
			format = new VideoFormat
			(encoding,
			 new java.awt.Dimension
				 (Integer.parseInt(kvhI.get("width")),
				  Integer.parseInt(kvhI.get("height"))),
			 maxDataLength = Integer.parseInt(kvhI.get("maxlength")),
			 Format.byteArray,
			 Float.parseFloat(kvhI.get("framerate")));
		}
    }
	
	/**
	  * Calculates the format of a JPG video channel.
	  * @author WHF
	  * @since V2.6
	  * @version 2004/12/30
	  */
	// 2004/12/30  WHF  Created.	  
	private final void videoJpgData()
	{
		isAudio = false;
		maxDataLength = -1; // appears to be unused.
		format = null;
		// We need to sneak a copy of one of the JPEGs to get it's image 
		//  properties:
		try {
			ChannelMap cmap = new ChannelMap();
			cmap.Add(channelName);
			con.Request(cmap, 0, 0, "newest");
			con.Fetch(60000, cmap);
			if (cmap.NumberOfChannels() > 0) {
				java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(
						new java.io.ByteArrayInputStream(
								cmap.GetDataAsByteArray(0)[0]));
				int w = bi.getWidth(), h = bi.getHeight();
				format = new JPEGFormat(
						new java.awt.Dimension(w, h),
						w*h*3,  // max data length (bytes).  Worst case based
								//  on 24 bit pixels.
						Format.byteArray,
						1000000, // fps
						100, // quality
						JPEGFormat.DEC_411
				);
			}
		} catch (Throwable t) {} // use a sensible default:
		if (format == null) {
			System.err.println("Failed to load image instance.  "
					+"Using defaults.");
			format = new JPEGFormat(
					new java.awt.Dimension(640, 480),
					100000,  // max data length (bytes)
					Format.byteArray,
					1, // fps
					100, // quality
					JPEGFormat.DEC_411  // sampling decimation
			);
		}
	}

/*
  *****************************************************************
  ***								***
  ***	Name :	endOfStream					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method determines if the end of the stream	***
  ***	has been reached.					***
  ***								***
  ***	Returns :						***
  ***	   endOfStream		Are we at the end?		***
  ***								***
  *****************************************************************
*/
    public final boolean endOfStream() {
	return (markDone);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getContentDescriptor				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the content descriptor for the	***
  ***	data contained in this stream. It is always raw.	***
  ***								***
  ***	Returns :						***
  ***	   getContentDescriptor	The stream content.		***
  ***								***
  *****************************************************************
*/
    public final ContentDescriptor getContentDescriptor() {
	return (cd);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getContentLength				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the length of this stream. If	***
  ***	we knew the source was done, we could calculate this	***
  ***	value. Unfortunately, there is no way to determine for	***
  ***	sure that the source is done.				***
  ***								***
  ***	Returns :						***
  ***	   getContentLength	The length of this stream.	***
  ***								***
  *****************************************************************
*/
    public final long getContentLength() {
	return (LENGTH_UNKNOWN);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getControls					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the controls associated with	***
  ***	this DataTurbine data source.				***
  ***								***
  ***	Returns :						***
  ***	   getControls		The list of controls.		***
  ***								***
  *****************************************************************
*/
    public final Object[] getControls() {
	return (controls);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getControl					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the control object for the	***
  ***	specified control type.					***
  ***								***
  ***	Input :							***
  ***	   controlTypeI		The desired control type.	***
  ***								***
  ***	Returns :						***
  ***	   getControl		The control object or null.	***
  ***								***
  *****************************************************************
*/
    public final Object getControl(String controlTypeI) {
	Object controlR = null;

	try {

	    // Find the class associated with the control type.
	    Class	controlClass = Class.forName(controlTypeI);

	    // Search our controls for an instance of that class.
	    for (int idx = 0; idx < controls.length; ++idx) {
		if (controlClass.isInstance(controls[idx])) {
		    controlR = controls[idx];
		}
	    }

	    // Return null if we cannot find the specified control type.
	} catch (Exception e) {
	}

	return (controlR);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getFormat					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the format of this stream.	***
  ***								***
  ***	Returns :						***
  ***	   getFormat		The format of the stream.	***
  ***								***
  *****************************************************************
*/
    public final Format getFormat() {
	return (format);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isAudioTrack					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the value of the is audio flag.	***
  ***								***
  ***	Returns :						***
  ***	   isAudioTrack		Is this an audio track?		***
  ***								***
  *****************************************************************
*/
    public final boolean isAudioTrack() {
	return (isAudio);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isVideoTrack					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the inverse of the value of the	***
  ***	is audio flag.						***
  ***								***
  ***	Returns :						***
  ***	   isVideoTrack		Is this an video track?		***
  ***								***
  *****************************************************************
*/
    public final boolean isVideoTrack() {
	return (!isAudioTrack());
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	passData					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called to pass data from the Data	***
  ***	Turbine into this stream. It adds the data to the	***
  ***	data vector and notifies the transfer handler.		***
  ***								***
  ***	Input :							***
  ***	   mapI			The channel map.		***
  ***	   channelI		The channel of data.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    final void passData(ChannelMap mapI,int channelI) {

	// Update the channel to read.
	synchronized (this) {

	    // Add the frame or mark the stream as done.
	    if (channelI != -1) {
		map = mapI;
		channelToRead = channelI;
	    } else {
		markDone = true;
	    }
	    notifyAll();
	}

	transferHandler.transferData(this);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	read						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method reads a buffer (frame) of data from	***
  ***	this stream. Data comes to this class as a series of	***
  ***	COM.Creare.RBNB.API.Channel objects. It is the job of	***
  ***	this method to translate that into a buffer.		***
  ***								***
  ***	Input :							***
  ***	   bufferI		The buffer to fill.		***
  ***								***
  ***	Modification History :					***
  ***	   08/18/2004 - INB					***
  ***		Time is a double in V2.  V2 doesn't support	***
  ***		actual frame based access, but it does allow	***
  ***		for the retrieval of a single point at any	***
  ***		time.  Maps are now channel maps.  The		***
  ***		connection is a sink.				*** 
  ***								***
  *****************************************************************
*/
    public final void read(Buffer bufferI) {
	ChannelMap lMap = null;
	int channel = -1;

	// Set up the standard fields.
	bufferI.setFormat(format);
	bufferI.setHeader(null);
	bufferI.setSequenceNumber(sequenceNumber++);

	synchronized (this) {
	    lMap = map;
	    channel = channelToRead;
	}

	// If there is no more data, return a garbage buffer.
	if (channel == -1) {
	    bufferI.setDiscard(true);
	    synchronized (this) {
		if (markDone) {
		    bufferI.setEOM(true);
		    markDone = false;
		}
	    }

	} else {
	    // Copy the data into the buffer.
	    //byte[][] data = lMap.GetDataAsByteArray(channel);
	    byte[] data;
	    if(isAudioTrack()) data = lMap.GetData(channel);  // mjm: glom audio data together 
	    else	       data = lMap.GetDataAsByteArray(channel)[0];

	    // Build the output buffer space if necessary.
	    Object outdata = bufferI.getData();

	    if ((outdata == null) ||
		(outdata.getClass() != Format.byteArray) ||
		(((byte[]) outdata).length < data.length)) {  // mjm was data[0]
		outdata = new byte[data.length];              // mjm was data[0]
		bufferI.setData(outdata);
	    }

	    bufferI.setFlags(0);
	    bufferI.setLength(data.length);  // mjm was data[0]

	    System.arraycopy
		(data,   // mjm was data[0]
		 0,
		 outdata,
		 0,
		 data.length);  // mjm was data[0]

	    double start = lMap.GetTimeStart(channel);
	    if (startedAt == -Double.MAX_VALUE) {
		startedAt = start;
		start = 0.;
	    } else {
		start -= startedAt;
	    }
	    double duration = lMap.GetTimeDuration(channel);
	    /*
	    bufferI.setTimeStamp((long) (start*1000000000.));
	    bufferI.setDuration((long) (duration*1000000000.));
	    */
	}

	// Clear the channel.
	synchronized (this) {
	    map = null;
	    channelToRead = -1;
	    notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setTransferHandler				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the transfer handler for this	***
  ***	stream to the input value.				***
  ***								***
  ***	Input :							***
  ***	   transferHandlerI	The transfer handler.		***
  ***								***
  *****************************************************************
*/
    public void setTransferHandler(BufferTransferHandler transferHandlerI) {
	synchronized (this) {
	    transferHandler = transferHandlerI;
	    notifyAll();
	}
    }
}
