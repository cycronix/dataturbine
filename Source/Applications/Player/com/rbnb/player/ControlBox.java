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
  ***	Name :	ControlBox			        	***
  ***	By   :	UCB/INB  	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class creates and maintains a Box of GUI        ***
  ***   components which allows the user to retrieve data from  ***
  ***   a DataTurbine, using the familiar VCR-control paradigm. ***
  ***      To respond to user actions, the developer must       ***
  ***   implement the ControlBoxListener interface.             ***
  ***								***
  ***	Modification History					***
  ***   07/22/2005	JPW	By default, don't display	***
  ***				the Duration or Increment	***
  ***				sliders.  Also, change the	***
  ***				default Duration from 1 second	***
  ***				to 1 point (0 seconds).		***
  ***	10/29/2004	JPW	Add get/setTimeFormat(); this	***
  ***				works along with the new "Time	***
  ***				Format" menu in RequestHandler	***
  ***				to allow the user to specify	***
  ***				how the time information should	***
  ***				be formatted for display on the	***
  ***				Position slider on the GUI.	***
  ***	10/14/2004	JPW	Changed durSiT values to include***
  ***				"1 point"; this will set	***
  ***				duration = 0			***
  ***	10/13/2004	JPW	No more mode buttons (only Time	***
  ***				mode in RBNB V2)		***
  ***	10/08/2004	JPW	Brought over from		***
  ***				COM.Creare.RBNB.Widgets		***
  ***				to be in the new RBNB V2 Player	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ControlBox extends Box 
    implements ActionListener {

    /*************** Public constants **************/
    public final static int	BEGINNINGOFDATA = 0,
				PLAYBACKWARD = 1,
				STEPBACKWARD = 2,
				PAUSE = 3,
				STEPFORWARD = 4,
				PLAYFORWARD = 5,
				ENDOFDATA = 6,
	                        REALTIME = 7;

    public final static int     TIME_MODE = 0,
	                        FRAMES_MODE = 1,
	                        //STREAM_MODE = 2,
	                        NUM_MODES = 2;

    public final static int MINPOS = 1,
	MAXPOS = 1000;

    public final static Font GUI_FONT = new Font("Helvetica", Font.PLAIN, 12);
    
    // JPW 10/29/2004: Add time format modes
    public final static int TIME_FORMAT_UNSPECIFIED  = 1;
    public final static int TIME_FORMAT_MILLISECONDS = 2;
    public final static int TIME_FORMAT_SECONDS      = 3;
    
    /*************** Private constants **************/
    private final static String[][] imageFiles = {
	{ "Video/bofIns.jpg",   "Video/bof.jpg",   "Video/bofOn.jpg"   },
	{ "Video/rplayIns.jpg", "Video/rplay.jpg", "Video/rplayOn.jpg" },
	{ "Video/rstepIns.jpg", "Video/rstep.jpg", "Video/rstepOn.jpg" },
	{ "Video/stopIns.jpg",  "Video/stop.jpg",  "Video/stopOn.jpg"  },
	{ "Video/stepIns.jpg",  "Video/step.jpg",  "Video/stepOn.jpg"  },
	{ "Video/playIns.jpg",  "Video/play.jpg",  "Video/playOn.jpg"  },
	{ "Video/eofIns.jpg",   "Video/eof.jpg",   "Video/eofOn.jpg"   },
	{ "Video/rtIns.jpg",    "Video/rt.jpg",    "Video/rtOn.jpg"    },
      };

    private final static String	actions[] = {
	                          "BOD",
				  "RPLAY",
				  "RSTEP",
				  "PAUSE",
				  "STEP",
				  "PLAY",
				  "EOD",
				  "RT"};

//      private final static String modes[] = {"Time", "Frames", "Stream"};
    private final static String modes[] = {"Time", "Frames"};

    // Note that the panels must be displayed (from top to bottom) in the
    // same order as they are indexed in sPan[].  (Indexed by POS, DUR,
    // INC and RATE)
    private final static int	DISABLED = 0,
				OFF = 1,
	                        ON = 2,
	                        POS = 0,
	                        DUR = 1,
	                        INC = 2,
	                        RATE = 3,
	                        NUMSLIDERS = 4,
	                        BUTTONS = 8;

    // SliderInfos
    private static final SliderInfo posSiT = new SliderInfo(MINPOS, MAXPOS, MAXPOS);
    private static final SliderInfo posSiF = new SliderInfo(MINPOS, MAXPOS, MAXPOS);

    // JPW 10/14/2004: Add case for duration = 0
    // JPW 07/22/2005: Change the default duration from 1 second (which is at
    //                 index=19) to 1 point/0 duration (which is at index=0)
    private static final SliderInfo durSiT =
    	new SliderInfo(
	    new String[] {	"1 point",
		    		"1 microsecond",
				"2 microseconds",
				"5 microseconds",
				"10 microseconds", 
				"20 microseconds", 
				"50 microseconds", 
				"100 microseconds", 
				"200 microseconds", 
				"500 microseconds",
				"1 millisecond", 
				"2 milliseconds", 
				"5 milliseconds", 
				"10 milliseconds", 
				"20 milliseconds", 
				"50 milliseconds", 
				"100 milliseconds",
				"200 milliseconds", 
				"500 milliseconds", 
				"1 second",
				"2 seconds",
				"5 seconds",
				"10 seconds",
				"30 seconds",
				"1 minute",
				"2 minutes",
				"5 minutes",
				"10 minutes",
				"30 minutes",
				"1 hour",
				"2 hours",
				"5 hours",
				"12 hours",
				"1 day",
				"2 days",
				"5 days",
	    			"10 days"},
	    new double[] {	0.0,
		    		0.000001,
		    		0.000002, 
				0.000005,
				0.00001, 
				0.00002,
				0.00005, 
				0.0001,
				0.0002,
				0.0005,
				0.001,
				0.002,
				0.005,
				0.01,
				0.02,
				0.05,
				0.1,
				0.2,
				0.5,
				1,
				2,
				5,
				10,
				30,
				60,
				120,
				300,
				600,
				1800,
				3600,
				7200,
				18000,
				43200,
				86400,
				172800,
				432000,
	    			864000},
	    0);
    private static final SliderInfo durSiF = new SliderInfo(new String[] {"1 frame", "2 frames", 
									  "5 frames", "10 frames", 
									  "20 frames", "50 frames",
									  "100 frames", "200 frames", 
									  "500 frames", "1000 frames", 
									  "2000 frames", "5000 frames",
									  "10,000 frames"},
							    new double[] {1, 2, 5, 10, 20, 50, 100, 
									  200, 500, 1000,
									  2000, 5000, 10000},
							    0);

    private static final SliderInfo incSiT = new SliderInfo(new String[] {"0.0001x duration", 
									  "0.0002x duration",
									  "0.0005x duration", 
									  "0.001x duration", 
									  "0.002x duration", 
									  "0.005x duration",
									  "0.01x duration", 
									  "0.02x duration", 
									  "0.05x duration", 
									  "0.1x duration", 
									  "0.2x duration", 
									  "0.5x duration",
									  "1x duration", 
									  "2x duration", 
									  "5x duration", 
									  "10x duration", 
									  "20x duration", 
									  "50x duration", 
									  "100x duration", 
									  "200x duration", 
									  "500x duration", 
									  "1,000x duration", 
									  "2,000x duration", 
									  "5,000x duration", 
									  "10,000x duration"},
							    new double[] {0.0001, 0.0002, 
									  0.0005, 0.001, 0.002, 
									  0.005, 0.01, 0.02, 0.05, 
									  0.1, 0.2, 0.5, 1, 2, 5, 
									  10, 20, 50, 100, 200, 500, 
									  1000, 2000, 5000, 10000},
							    12);
    private final static SliderInfo incSiF = new SliderInfo(new String[] {"1 frame", "2 frames", 
									  "5 frames", "10 frames", 
									  "20 frames", "50 frames",
									  "100 frames", "200 frames", 
									  "500 frames",
									  "1000 frames"},
							    new double[] {1, 2, 5, 10, 20, 50, 
									  100, 200, 500, 1000},
							    0);
    
    private static final SliderInfo ratSiT = new SliderInfo(new String[] {"0.001x", "0.002x", 
									  "0.005x", "0.01x",
									  "0.02x", "0.05x", "0.1x", 
									  "0.2x", "0.5x", "1x", 
									  "2x", "5x", "10x", "20x", 
									  "50x", "100x", "200x", 
									  "500x", "1000x"},
							    new double[] {0.001, 0.002, 0.005, 0.01, 
									  0.02, 0.05, 0.1, 0.2, 0.5, 
									  1, 2, 5, 10, 20, 50, 
									  100, 200, 500, 1000},
							    9);
    private static final SliderInfo ratSiF = new SliderInfo(new String[] {"0.001 frames/second", 
									  "0.002 frames/second", 
									  "0.005 frames/second", 
									  "0.01 frames/second",
									  "0.02 frames/second", 
									  "0.05 frames/second",
									  "0.1 frames/second", 
									  "0.2 frames/second",
									  "0.5 frames/second", 
									  "1 frame/second", 
									  "2 frames/second", 
									  "5 frames/second", 
									  "10 frames/second", 
									  "20 frames/second", 
									  "50 frames/second", 
									  "100 frames/second", 
									  "200 frames/second", 
									  "500 frames/second", 
									  "1000 frames/second"},
							    new double[] {0.001, 0.002, 0.005, 0.01, 
									  0.02, 0.05, 0.1, 0.2, 0.5, 
									  1, 2, 5, 10, 20, 50, 100, 
									  200, 500, 1000},
							    9);

    /*************** Private variables **************/
    private ControlBoxListener cbl = null;

    // components making up display
    private JRadioButton[] buttons = null;
    // JPW 10/13/2004: No more mode buttons (only Time mode in RBNB V2)
    // private JRadioButton[] modeButtons = null;
    private SliderPanel sPan[] = null;

    // images for main buttons
    private Image images[][] = null;

    // UCB 11/01/01: we must change to a variable which can
    // represent a mode with more than two states.
    //private boolean timeBaseInSec = true; // true: seconds; false: frames
    private int mode = TIME_MODE;
    
    // JPW 10/29/2004: This variable specifies how the user wants the time
    //                 value displayed on the Position slider.
    private int guiTimeFormat = TIME_FORMAT_UNSPECIFIED;

/*
  *****************************************************************
  ***								***
  ***	Name :	ControlBox 			        	***
  ***	By   :	UCB/INB  	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
    public ControlBox() 
	throws Exception {

	super(BoxLayout.Y_AXIS);
	setBackground(Color.lightGray);

	// Load the images
	loadImages();
	
	// Create the buttons.
	add(createControlButtons());

	// create sliders and time/framerate displays
	sPan = new SliderPanel[NUMSLIDERS];

	SliderInfo[] posModeInfos = new SliderInfo[NUM_MODES];
	posModeInfos[TIME_MODE] = posSiT;
	posModeInfos[FRAMES_MODE] = posSiF;
	//posModeInfos[STREAM_MODE] = posSiT;
	sPan[POS] = new SliderPanel(this, "Position", POS,
				  false, posModeInfos, false, false);

	SliderInfo[] durModeInfos = new SliderInfo[NUM_MODES];
	durModeInfos[TIME_MODE] = durSiT;
	durModeInfos[FRAMES_MODE] = durSiF;
	//durModeInfos[STREAM_MODE] = durSiF;
	sPan[DUR] = new SliderPanel(this, "Duration", DUR,
				  true, durModeInfos, true, false);

	SliderInfo[] incModeInfos = new SliderInfo[NUM_MODES];
	incModeInfos[TIME_MODE] = incSiT;
	incModeInfos[FRAMES_MODE] = incSiF;
	//incModeInfos[STREAM_MODE] = incSiF;
	sPan[INC] = new SliderPanel(this, "Increment", INC,
				    true, incModeInfos, true, false);

	SliderInfo[] ratModeInfos = new SliderInfo[NUM_MODES];
	ratModeInfos[TIME_MODE] = ratSiT;
	ratModeInfos[FRAMES_MODE] = ratSiF;
	//ratModeInfos[STREAM_MODE] = ratSiF;
	sPan[RATE] = new SliderPanel(this, "Rate", RATE,
				     true, ratModeInfos, true, true);

	for (int i = 0; i < NUMSLIDERS; i++) {
	    add(sPan[i].getPanel());
	}
	
	// JPW 07/22/2005: By default, don't display Duration and Increment
	//                 sliders.  Also, must disable the Increment slider
	//                 (since duration = 0)
	setDurationVisible(false);
	setIncrementVisible(false);
	sPan[INC].setEnabled(false);
	
	// create a dummy ControlBoxListener and assign it for now
	addControlBoxListener(new DummyCBL());
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	addControlBoxListener 			        ***
  ***	By   :	U. Bergstrom 	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Add a ControlBoxListener.                            ***
  ***								***
  *****************************************************************
*/
    public void addControlBoxListener(ControlBoxListener cblI) {
	cbl = cblI;
    }

    /**********************************************************/
    /********** Methods to create the GUI *********************/
    /**********************************************************/    
/*
  *****************************************************************
  ***								***
  ***	Name :	loadImages					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method loads the button images.			***
  ***								***
  ***	12/23/2004	JPW	Use WHF's method of loading	***
  ***				image files.			***
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
		  // JPW 12/23/2004: This is the same new approach that WHF used in
		  //                 Applications/AudioVideo/com/rbnb/media/protocol/VideoRetriever.java
		  //                 in the method loadImages()
		  images[idx][idx1] =
		      new ImageIcon(
		          this.getClass().getClassLoader().getResource(imageFiles[idx][idx1])).getImage();
		  if (images[idx][idx1] == null) {
		      throw new Exception
			  ("Unable to load image " + imageFiles[idx][idx1]);
		  }
	      }
	  }
      }
      
  /*
   * OLD METHOD
   *
      DataLoader dl = new DataLoader();
      byte[]   image = null;

      // If the images aren't already loaded, load them.
      if (images == null) {
	  images = new Image[BUTTONS][];
	  
	  for (int idx = 0; idx < BUTTONS; ++idx) {
	      images[idx] = new Image[3];
	      for (int idx1 = 0; idx1 < 3; ++idx1) {
		  image = dl.loadData(imageFiles[idx][idx1]);
		  if (image == null) {
		      throw new Exception
			  ("Unable to load image " + imageFiles[idx][idx1]);
		  }
		  images[idx][idx1] =
		      Toolkit.getDefaultToolkit().createImage(image);
	      }
	  }
      }
   *
   *
   */

  }

/*
  *****************************************************************
  ***								***
  ***	Name :	createControlButtons				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
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
  ***	Modification History					***
  ***	10/13/2004	JPW	No more mode buttons (only Time	***
  ***				mode in RBNB V2)		***
  ***								***
  *****************************************************************
*/
    private final Box createControlButtons() throws Exception {

	// Create the box and button group to contain the control buttons.
	Box controlBox = new Box(BoxLayout.X_AXIS);
	controlBox.setBackground(Color.lightGray);
	ButtonGroup bGroup = new ButtonGroup();
	
	// Create the buttons.
	if (buttons == null) {
	    buttons = new JRadioButton[BUTTONS];
	}
	
	for (int idx = 0; idx < BUTTONS; ++idx) {
	    buttons[idx] = new JRadioButton();
	    buttons[idx].setBackground(Color.lightGray);
	    buttons[idx].setActionCommand(actions[idx]);
	    buttons[idx].setDisabledIcon(new ImageIcon(images[idx][DISABLED]));
	    buttons[idx].setIcon(new ImageIcon(images[idx][OFF]));
	    buttons[idx].setSelectedIcon(new ImageIcon(images[idx][ON]));
	    buttons[idx].addActionListener(this);
	    buttons[idx].setEnabled(false);
	    controlBox.add(buttons[idx]);
	    bGroup.add(buttons[idx]);
	}
	
	// JPW 10/13/2004: Take off the Time/Frames mode buttons; in RBNB V2
	//                 we only work in Time mode.
	/*
	// create Mode radio buttons
	Color modeBack = new Color(169, 190, 218);
	Panel modeP = new Panel(new GridLayout(1, NUM_MODES, 2, 2));
	modeP.setBackground(modeBack);
	modeButtons = new JRadioButton[NUM_MODES];
	ButtonGroup bGroup2 = new ButtonGroup();

	for (int i = 0; i < NUM_MODES; i++) {
	    modeButtons[i] = new JRadioButton(modes[i], i == 0);
	    modeButtons[i].setFont(GUI_FONT);
	    modeButtons[i].setBackground(modeBack);
	    modeButtons[i].setActionCommand(modes[i]);
	    modeButtons[i].addActionListener(this);
	    modeButtons[i].setEnabled(false);
	    bGroup2.add(modeButtons[i]);
	    modeP.add(modeButtons[i]);
	}
	BorderedPanel borP = new BorderedPanel(modeP, "Mode");
	// put panel in box?  No, not for now.
	controlBox.add(borP);
	*/
	
	return (controlBox);
    }

    /**********************************************************/
    /*********** ActionListener and ChangeListener ************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	actionPerformed					***
  ***	By   :	INB/UCB       (Creare Inc., Hanover, NH)	***
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
	
	if (command.equals("BOD")) {
	    cbl.limitOfData(true, false);
	} else if (command.equals("RPLAY")) {
	    cbl.play(false);
	} else if (command.equals("RSTEP")) {
	    cbl.step(false);
	} else if (command.equals("PAUSE")) {
	    cbl.pause();
	} else if (command.equals("STEP")) {
	    cbl.step(true);
	} else if (command.equals("PLAY")) {
	    cbl.play(true);
	} else if (command.equals("EOD")) {
	    cbl.limitOfData(false, false);
	} else if (command.equals("RT")) {
	    cbl.realTime();

	}
	
	/*
	else if (command.equals("Time")) {
	    setMode(TIME_MODE);
	    cbl.setMode(mode);
	} else if (command.equals("Frames")) {
	    setMode(FRAMES_MODE);
	    cbl.setMode(mode);
	}
	*/
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	sliderChanged					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called by a SliderPanel when the user ***
  ***   has changed the slider.  Note that this method is called***
  ***   from within the Event Handling thread!                  ***
  ***								***
  ***	Modification History					***
  ***	10/14/2004	JPW	Special case when duration = 0;	***
  ***				the increment slider is disabled***
  ***								***
  *****************************************************************
*/
  public final void sliderChanged(int which,
				  int index,
				  boolean trackingSlider) {
      if (which == POS) {
	  // Check whether we are at a limit!
	  if (index == MINPOS) {
	      cbl.limitOfData(true, trackingSlider);
	  } else if (index == MAXPOS) {
	      cbl.limitOfData(false, trackingSlider);
	  } else {
	      cbl.positionSliderAt(index, trackingSlider);
	  }

      } else if (which == DUR) {
	  double duration = sPan[DUR].getValue(index);
	  // JPW 10/14/2004: Special case when duration = 0; the
	  //                 increment slider is disabled
	  if (duration == 0.0) {
	      sPan[INC].setEnabled(false);
	  } else {
	      sPan[INC].setEnabled(true);
	  }
	  cbl.durationAt(duration, trackingSlider);

      } else if (which == RATE) {
	  double rate = sPan[RATE].getValue(index);
	  cbl.rateAt(rate, trackingSlider);

      } else if (which == INC) {
	  double inc = sPan[INC].getValue(index);
	  cbl.incrementAt(inc, trackingSlider);
      }
  }

    /**********************************************************/
    /****************** Control the buttons *******************/
    /**********************************************************/
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
  **	   buttonI		The button to toggle.		***
  ***								***
  *****************************************************************
*/
  public final void toggleButton(int buttonI) {
      buttons[buttonI].setEnabled(true);
      buttons[buttonI].setSelected(true);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setMode   					***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the mode.  Permissible values are:  ***
  ***   TIME, FRAMES, STREAM.  The method returns the new value ***
  ***   of the mode.                                            ***
  ***                                                           ***
  ***   11/01/01 UCB: This method used to be called toggleTBase,***
  ***                 and it was used to toggle between the     ***
  ***                 two original modes (called time-bases):   ***
  ***                 Time and Frames.                          ***
  ***								***
  ***	10/13/04 JPW: No more mode buttons (only Time more in	***
  ***		      RBNB V2).					***
  ***								***
  *****************************************************************
*/
    public final int setMode(int modeI) {
	mode = modeI;
	
	// JPW 10/13/2004: No more mode buttons (only Time mode in RBNB V2)
	// modeButtons[mode].setSelected(true);
	
	// reset sliders' ranges and values
	for (int i = 0; i < NUMSLIDERS; i++) {
	    // be careful not to call this on position slider;
	    // it doesn't really make sense for it here...
	    if (i != POS) {
		sPan[i].gotoDefault(mode);
	    }
	}

	return mode;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setModeEnabled 		         		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method dis/enables the Modes selction.     	***
  ***								***
  ***	Modification History					***
  ***	10/13/2004	JPW	No more mode buttons (only Time	***
  ***				mode in RBNB V2)		***
  ***								***
  *****************************************************************
*/
  public final void setModeEnabled(boolean enable) {
      for (int i = 0; i < NUM_MODES; i++) {
	  // JPW 10/13/2004: No more mode buttons (only Time mode in RBNB V2)
	  // modeButtons[i].setEnabled(enable);
      }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setButtonsEnabled				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method enables all buttons.     		***
  ***								***
  *****************************************************************
*/
  public final void setButtonsEnabled(boolean enable) {
      for (int i = 0; i < BUTTONS; i++) {
	  buttons[i].setEnabled(enable);
      }

      setModeEnabled(enable);
  }

    /**********************************************************/
    /**************** Enable/disable sliders ******************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	setSlidersEnabled				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method enables/disables the sliders.            ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setSlidersEnabled(boolean enable) {
	for (int i = 0; i < NUMSLIDERS; i++) {
	    sPan[i].setEnabled(enable);
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setPositionEnabled		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method enables/disables the rate slider.        ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setPositionEnabled(boolean enable) {
	sPan[POS].setEnabled(enable);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setRateEnabled		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method enables/disables the rate slider.        ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setRateEnabled(boolean enable) {
	sPan[RATE].setEnabled(enable);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setPositionVisible				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method causes the Position slider panel to be   ***
  ***   either visible or invisible.                            ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setPositionVisible(boolean vis) {
	setSliderPanVisible(POS, vis);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setDurationVisible				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method causes the Duration slider panel to be   ***
  ***   either visible or invisible.                            ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setDurationVisible(boolean vis) {
	setSliderPanVisible(DUR, vis);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setIncrementVisible				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method causes the Increment slider panel to be  ***
  ***   either visible or invisible.                            ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setIncrementVisible(boolean vis) {
	setSliderPanVisible(INC, vis);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setRateVisible		         		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method causes the Rate slider panel to be       ***
  ***   either visible or invisible.                            ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setRateVisible(boolean vis) {
	setSliderPanVisible(RATE, vis);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setSliderPanVisible				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method causes the specified slider panel to be  ***
  ***   either visible or invisible.                            ***
  ***								***
  *****************************************************************
*/
    private final synchronized void setSliderPanVisible(int which,
							boolean vis) {
	BorderedPanel p = sPan[which].getPanel();
	p.setVisible(vis);
	
	// UCB 1/29/01: attempted a fix here, unsuccessfully.
	// This code is currently compiled under Java 1.3, and when
	// run under JRE 1.2.2, setting a slider panel invisible
	// makes it invisible OK, but does not set its size to (0,0),
	// as when run under JRE 1.3.
//  	if (vis) {
//  	    p.setSize(p.getPreferredSize());
//  	} else {
//  	    p.setSize(0,0);
//  	}
//  	validate();
//  	doLayout();
    }

    /**********************************************************/
    /******************* slider accessors *********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	getDuration					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method gets the value of the duration slider.   ***
  ***								***
  *****************************************************************
*/
    public final synchronized double getDuration() {
	return sPan[DUR].getValue();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getRate 					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method gets the value of the rate slider.       ***
  ***								***
  *****************************************************************
*/
    public final synchronized double getRate() {
	return sPan[RATE].getValue();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setRateToDefault 				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the rate slider to its default      ***
  ***   value.                                                  ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setRateToDefault() {
	sPan[RATE].gotoDefault(mode);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getIncrement 					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method gets the value of the increment slider.  ***
  ***								***
  *****************************************************************
*/
    public final synchronized double getIncrement() {
	return sPan[INC].getValue();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setPosition					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the value of the position slider.   ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setPosition(int pos) {
	sPan[POS].setIndex(pos, mode, false);
    }

    /**********************************************************/
    /************* Set Position and Rate displays *************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	setPositionDisplay				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the position feedback display.      ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setPositionDisplay(String disp) {
	sPan[POS].setText(disp);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setRateFeedback  				***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2002					***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the rate extra feedback display.    ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setRateFeedback(String disp) {
	sPan[RATE].setExtraText(disp);
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	setTimeFormat  					***
  ***	By   :	John P. Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2004					***
  ***								***
  ***	Copyright 2004 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Set the time format mode.  This mode is used to	***
  ***	specify how the user wants the time value to be		***
  ***	displayed on the Position slider on the GUI.		***
  ***								***
  ***	Modification History					***
  ***	10/29/2004	JPW	Created.			***
  ***								***
  *****************************************************************
*/
    public void setTimeFormat(int formatI) {
	guiTimeFormat = formatI;
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	getTimeFormat  					***
  ***	By   :	John P. Wilson   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 2004					***
  ***								***
  ***	Copyright 2004 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Get the time format mode.  This mode is used to	***
  ***	specify how the user wants the time value to be		***
  ***	displayed on the Position slider on the GUI.		***
  ***								***
  ***	Modification History					***
  ***	10/29/2004	JPW	Created.			***
  ***								***
  *****************************************************************
*/
    public int getTimeFormat() {
	return (guiTimeFormat);
    }
    
    /**********************************************************/
    /********************** Inner Classes *********************/
    /**********************************************************/
/*
  *****************************************************************
  ***								***
  ***	Name :	DummyCBL         		        	***
  ***	By   :	U. Bersgtrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class implements ControlBoxListener and is used ***
  ***   to give each ControlBox a listener (avoids NullPointers)***
  ***   Note that each ControlBox is also able to have just one ***
  ***   ControlBoxListener; thus this system falls outside the  ***
  ***   true Java paradigm of event notification.  But it will  ***
  ***   do!                                                     ***
  ***								***
  *****************************************************************
*/
    class DummyCBL implements ControlBoxListener {
	DummyCBL() {}

	public void limitOfData(boolean beginning,
				boolean trackingPosition) {}
	
	public void play(boolean forwards) {}
	
	public void step(boolean forwards) {}
	
	public void pause() {}
	
	public void realTime() {}
	
	public void positionSliderAt(int sliderPos, 
				     boolean trackingPosition) {}
	
	public void durationAt(double dur,
			       boolean trackingDuration) {}
	
	public void rateAt(double rate,
			   boolean trackingDuration) {}

	public void incrementAt(double value,
			   boolean trackingInc) {}
	
	public void setMode(int mode) {}
	
    }

}
