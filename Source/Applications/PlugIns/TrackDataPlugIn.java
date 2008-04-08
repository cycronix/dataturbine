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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;
// Coordinate compression algorithm
// JPW 03/16/2006: Moved from com.rbnb.utility to PlugIns directory
// import com.rbnb.utility.LineSimp;
import com.rbnb.utility.KeyValueHash;
// Alternative to System.exit, so don't bring down servlet engine
import com.rbnb.utility.RBNBProcess;
// String formatter
import com.rbnb.utility.ToString;
import com.rbnb.utility.Utility;

/******************************************************************************
 * Respond to requests for track data by performing the needed low-level
 * RBNB requests to RIF channels and returning this data as a series of
 * channels with normalized names (for instance, return "Alt" instead of
 * "ADC Altitude-m").
 * <p>
 *
 * @author John P. Wilson
 *
 * @version 04/08/2008
 */

/*
 * Copyright 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 2008/04/08  WHF      Corrected heading calculation.
 * 2008/03/24  JPW	In addTrackChans(), for pseudo-alt, when defining the
 * 			source channel name, don't prepend  remoteSourceI onto
 * 			pAltChanName; just use pAltChanName on its own.
 * 2008/02/28  WHF      PseudoAlt now written to its own virtual channel.
 * 2008/02/27  WHF      Renamed bTacticalRequest to fullDurationFlag to convey
 *                      its new meaning.
 * 02/22/2008  WHF      Refactoring, added pitch/roll channels.
 * 11/15/2006  JPW	Add "-d" flag to indicate that the user would like
 *			debug info printed; otherwise, just print minimal
 *			debug output.
 * 11/08/2006  JPW	If the "tactical=1" key/value pair is included in the
 *			data String with the request, then request Speed and
 *			and Heading channels along with the other ancillary
 *			channels.  Also, use the requested duration rather than
 *			hardwiring to 0-duration.  The ancillary chans are used
 *			for creating FormatX and CMF packets.  Add new flag,
 *			bTacticalRequest, to indicate if the client wants to
 *			do this.
 * 10/26/2006  EMF      Catch exceptions from Sink.Request/Fetch calls,
 *                      create empty ChannelMaps.
 * 10/10/2006  JPW	If there is a top-level track and the request channel
 *			data contains an "append" munge, then request the
 *			specified channel (what we call the pseudo-alt chan).
 * 09/25/2006  JPW	Change name from TrackPlugIn to TrackDataPlugIn
 * 08/24/2006  JPW	Add geo-filter
 * 07/11/2006  JPW	Break KMLTrackPlugIn into KMLPlugIn and TrackPlugIn;
 *			KMLPlugIn will focus on KML presentation of track data;
 *			TrackPlugIn will focus on low-level requests for needed
 *			data channels and also perform data filtering.
 *
 */

public class TrackDataPlugIn implements ActionListener, ItemListener {
    
    // Static, "virtual" channel names sent back to the requestor
    private final static String vAltChanName = "Alt";
    private final static String vLatChanName = "Lat";
    private final static String vLonChanName = "Lon";
    private final static String vIdChanName = "TrackID";
    private final static String vTypeChanName = "Type";
    private final static String vClassificationChanName = "Classification";
    private final static String vSpeedChanName = "Speed";
    private final static String vHeadingChanName = "Heading";
    private final static String vPitchChanName = "Pitch";
    private final static String vRollChanName = "Roll";    
    private final static String vPseudoAltChanName = "PseudoAlt";
                                                        
    // Channels names for the actual, remote track channels
    private String altChanName = vAltChanName;
    private String pAltChanName = null;
    private String latChanName = vLatChanName;
    private String lonChanName = vLonChanName;
    private String idChanName = vIdChanName;
    private String typeChanName = vTypeChanName;
    private String classificationChanName = vClassificationChanName;
    private String speedChanName = vSpeedChanName;
    private String headingChanName = vHeadingChanName;
//    private String yawChanName = vYawChanName;  heading
    private String pitchChanName = vPitchChanName;
    private String rollChanName = vRollChanName;    
    
    // RBNB connections
    private String address = "localhost:3333";
    private String rbnbServerName = null;
    private String sinkName = "TrackDataSink";
    private Sink sink = null;
    private String pluginName = "TrackData";
    private PlugIn plugin = null;
    
    // Fetch timeout; can be set via the "-t" command line option
    private long timeout=60000;
    
    private double[] times = null;
    private float[] alt = null;
    private float[] pAlt = null; // Pseudo-alt channel data
    private double[] lat = null;
    private double[] lon = null;
    // Ancillary channels
    // JPW 11/009/2006: Change from single points to arrays;
    //                  if bTacticalRequest is false, these arrays will
    //                  only contain single points; otherwise, there should be
    //                  the same number of points in these arrays as there are
    //                  in the alt/lat/lon arrays.
    private String[] trackID = null;
    private String[] type = null;
    private String[] classification = null;
    private float[] speed, heading, pitch, roll;
    
    // Number of contiguous data points to write at the track's head and tail
    private int numContiguousPts = 10;
    
    // There are 2 methods we can use to compress the coordinate data:
    // 1. Use a skip factor (only write 1 coordinate point every X points)
    // 2. Use a compression algorithm (the one we use was developed by WHF
    //    and is located in com.rbnb.utility.LineSimp; it is the
    //    Douglas-Peucker algorithm)
    // Both methods write out "numContiguousPts" of data at the tail and head
    private boolean bUseCoordSkip = false;
    
    // If using coordinate skip factor: the approx number of points we want to
    // target; this can be specified by using the "-p" command line option.
    private int targetNumPts = 1000;
    
    // If using coordinate compression algorithm: this is the tolerance factor
    // given to the algorithm; this can be specified using the "-c" command
    // line option.
    private double tolerance = 0.001;
    
    // Execution loop control variables
    private boolean bWhileLoopExited = false;
    private boolean bExitPlugIn = false;
    
    private String configFile = "TrackResources/TrackConfig.txt";
    
    // Don't initialize the GUI if "-g" flag is set
    private boolean bShowGUI = true;
    private JFrame frame = null;
    
    // Perform geo-filtering?
    private boolean bGeoFilter = false;
    
    // BoundingBox for filtering output
    private BoundingBox boundingBox = null;
    
    // GUI components
    private JCheckBox useGeoFilterCB = null;
    private JLabel geoFilterLabel = null;
    
    // JPW 11/08/2006: Does the user want to request additional ancillary
    //                 tactical data channels (Speed and Heading) and also
    //                 use the full duration (rather than force 0-duration)?
    //private boolean bTacticalRequest = false;
    
    /**
      * If true, make a full duration request for the 'ancillary' data.
      *   Otherwise zero duration requests are made.
      *  @since 2008/02/27 
      */
    private boolean fullDurationFlag;
    
    // JPW 11/15/2006: Print debug info?
    private boolean bPrintDebug = false;
    
    /**************************************************************************
     * Constructor.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  argument list
     * @version 11/15/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2006  JPW  Add "-d" flag (print debug).
     * 07/11/2006  JPW  Created.
     *
     */
    
    public static void main(String[] args) {
	(new TrackDataPlugIn(args)).exec();
    }
    
    public TrackDataPlugIn(String[] args) {
	
	// Not need since no longer Frame extension
	//super("Track PlugIn");
	
	System.err.println("\n");
	
	//parse args
	try {
	    ArgHandler ah=new ArgHandler(args);
	    //
	    // 'a' RBNB server address
	    //
	    if (ah.checkFlag('a')) {
		String addressL = ah.getOption('a');
		if (addressL != null) {
		    address=addressL;
		} else {
		    System.err.println(
			"WARNING: Null argument to the \"-a\"" +
			" command line option.");
		}
	    }
	    //
	    // 'c' tolerance for the Douglas-Peucker coordinate compression algorithm
	    //
	    if (ah.checkFlag('c')) {
		try {
		    String tempTolStr = ah.getOption('c');
		    if (tempTolStr != null) {
			double tempTol = Double.parseDouble(tempTolStr);
			tolerance = tempTol;
		    } else {
			System.err.println(
			    "WARNING: Null argument to the \"-c\"" +
			    " command line option.");
		    }
		} catch (NumberFormatException nfe) {
		    System.err.println(
		        "ERROR: The tolerance specified with the " +
			"\"-c\" flag is not a number; value ignored.");
		}
	    }
	    //
	    // 'd' Print debug
	    //
	    if (ah.checkFlag('d')) {
		bPrintDebug = true;
	    }
	    //
	    // 'f' config file
	    //
	    if (ah.checkFlag('f')) {
		try {
		    String tempFile=ah.getOption('f');
		    if (tempFile!=null) {
			configFile=tempFile;
		    } else {
			System.err.println(
			    "WARNING: Null argument to \"-f\"; " +
			    "use default config file: " +
			    configFile);
		    }
		} catch (Exception e) {
		    System.err.println("ERROR parsing -f command line option:");
		    e.printStackTrace();
		}
	    }
	    
	    //
	    // 'g' - do not display gui
	    //
	    if (ah.checkFlag('g')) bShowGUI=false;
	    
	    //
	    // 'h' Help
	    //
	    if (ah.checkFlag('h')) {
		System.err.println("TrackDataPlugIn command line options");
		System.err.println("   -a <RBNB address>");
		System.err.println("       default: localhost:3333");
		System.err.println("   -c <tolerance (only used in  Douglas-Peucker compression)>");
		System.err.println("       default: 0.001");
		System.err.println("   -d      Print debug to console.");
		System.err.println("   -f <config file>");
		System.err.println("       default: TrackResources/TrackConfig.txt");
		System.err.println("   -g (do not display GUI)");
		System.err.println("   -h (display this help message)");
		System.err.println("   -n <PlugIn name>");
		System.err.println("       default: TrackData");
		System.err.println("   -p <target number of points (only used in skip compression)>");
		System.err.println("       default: 1000");
		System.err.println("   -P");
		System.err.println("       NOTE: The presense of the '-P' command line option indicates");
		System.err.println("             that coordinate compression will be performed by the");
		System.err.println("             skip factor algorithm.  Absence of this command line");
		System.err.println("             option indicates that coordinate compression will be");
		System.err.println("             performed by the Douglas-Peucker compression algorithm.");
		System.err.println("   -t <Fetch timeout, in milliseconds>");
		System.err.println("       default: 60000 milliseconds");
		RBNBProcess.exit(0);
	    }
	    //
	    // 'n' PlugIn name
	    //
	    if (ah.checkFlag('n')) {
		String name = ah.getOption('n');
		if (name != null) {
		    pluginName = name;
		} else {
		    System.err.println(
			"WARNING: Null argument to the \"-n\"" +
			" command line option.");
		}
	    }
	    //
	    // 'p' target number of points for the skip compression algorithm
	    //
	    if (ah.checkFlag('p')) {
		try {
		    String tempNumPtsStr = ah.getOption('p');
		    if (tempNumPtsStr != null) {
			int tempNumPts = Integer.parseInt(tempNumPtsStr);
			if (tempNumPts > 0) {
			    targetNumPts = tempNumPts;
			}
		    } else {
			System.err.println(
			    "WARNING: Null argument to the \"-p\"" +
			    " command line option.");
		    }
		} catch (NumberFormatException nfe) {
		    System.err.println(
		        "ERROR: The point skip factor specified with the " +
			"\"-p\" flag is not a number; value ignored");
		}
	    }
	    //
	    // 'P' use the point skip compression algorithm
	    //
	    if (ah.checkFlag('P')) {
		bUseCoordSkip = true;
	    }
	    //
	    // 't' Fetch timeout
	    //
	    if (ah.checkFlag('t')) {
		try {
		    String toStr = ah.getOption('t');
		    if (toStr != null) {
			long to=Long.parseLong(toStr);
			if (to >= -1) {
			    timeout = to;
			} else {
			    System.err.println(
			    	"WARNING: Specified fetch timeout is " +
				"less than -1; value ignored.");
			}
		    } else {
			System.err.println(
			    "WARNING: Null argument to the \"-t\"" +
			    " command line option.");
		    }
		} catch (NumberFormatException nfe) {
		    System.err.println(
		        "ERROR: The fetch timeout specified with the " +
			"\"-t\" flag is not a number; value ignored");
		}
	    }
	} catch (Exception e) {
	    System.err.println(
		"TrackDataPlugIn argument exception " +
		e.getMessage());
	    e.printStackTrace();
	    RBNBProcess.exit(0);
	}
	
	System.err.println("\nInput Options:");
	System.err.println("RBNB server address: " + address);
	System.err.print("Method of output coordinate compression: ");
	if (bUseCoordSkip) {
	    System.err.println(
	    	"coordinate skip factor; target num output points: " +
		targetNumPts);
	} else {
	    System.err.println(
	    	"compression algorithm (Douglas-Peucker); tolerance = " +
		tolerance);
	}
	System.err.println("Desired PlugIn name: " + pluginName);
	System.err.println("RBNB Fetch timeout: " + timeout);
	
	// Read the configuration file
	try {
	    readConfigFile();
	} catch (Exception e) {
	    System.err.println(
	      "Exception caught trying to read file containing chan names:\n" +
	      e);
	    System.err.println("Using default channel names and display values.\n\n");
	}
	System.err.println("Name of the alt channel: " + altChanName);
	System.err.println("Name of the lat channel: " + latChanName);
	System.err.println("Name of the lon channel: " + lonChanName);
	System.err.println("Name of the id channel: " + idChanName);
	System.err.println("Name of the type channel: " + typeChanName);
	System.err.println("Name of the class channel: " + classificationChanName);
	System.err.println("Name of the speed channel: " + speedChanName);
	System.err.println("Name of the heading channel: " + headingChanName);
	System.err.println("Name of the pitch channel: " + pitchChanName);
	System.err.println("Name of the roll channel: " + rollChanName);
	
	System.err.print("\n");
	
	//EMF 4/11/06: only perform GUI operations if showing it, otherwise
	//             XHOST exceptions when running remotely
	if (bShowGUI) {
	    frame = new JFrame("TrackDataPlugIn");
	    
	    // Add File menu
	    Font font = new Font("Dialog", Font.PLAIN, 12);
	    JMenuBar menuBar = new JMenuBar();
	    menuBar.setFont(font);
	    JMenu menu = new JMenu("File");
	    menu.setFont(font);
	    JMenuItem menuItem = new JMenuItem("Exit");
	    menuItem.setFont(font);
	    menuItem.addActionListener(this);
	    menuItem.setEnabled(true);
	    menu.add(menuItem);
	    menuBar.add(menu);
	    frame.setJMenuBar(menuBar);
	    
	    frame.setFont(new Font("Dialog", Font.PLAIN, 12));
	    GridBagLayout gbl = new GridBagLayout();
	    
	    JPanel guiPanel = new JPanel(gbl);
	    
	    int row = 0;
	    
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 0;
	    gbc.weighty = 0;
	    
	    // Add geo-filter display
	    useGeoFilterCB = new JCheckBox("Use geo-filter?",bGeoFilter);
	    useGeoFilterCB.addItemListener(this);
	    geoFilterLabel = new JLabel(getBoundingBoxString());
	    gbc.insets = new Insets(5,5,5,15);
	    Utility.add(guiPanel,useGeoFilterCB,gbl,gbc,0,0,1,1);
	    gbc.insets = new Insets(5,0,5,5);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 100;
	    gbc.weighty = 100;
	    Utility.add(guiPanel,geoFilterLabel,gbl,gbc,1,0,1,1);
	    
	    // Add the panel to the content pane of the JFrame
	    gbl = new GridBagLayout();
	    frame.getContentPane().setLayout(gbl);
	    gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.CENTER;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 100;
	    gbc.weighty = 100;
	    gbc.insets = new Insets(0,0,0,0);
	    Utility.add(frame.getContentPane(),guiPanel,gbl,gbc,0,0,1,1);
	    
	    frame.pack();
	    
	    // Handle the close operation in the windowClosing() method of the
	    // registered WindowListener object.  This will get around
	    // JFrame's default behavior of automatically hiding the window when
	    // the user clicks on the '[x]' button.
	    frame.setDefaultCloseOperation(
		javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	    
	    frame.addWindowListener(
		new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			exitAction();
		    }
		});
	    
	    frame.setVisible(true);
	    
	}
	
    }
    
    /**
      * Extract a value from the configuration hash table.
      *
      * @author WHF
      * @version 2008/02/20
      */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Created.
     */
    private String extract(Hashtable hashtable, String name, String defVal)
    {
	String temp = (String)hashtable.get(name);
	String result = defVal;
	
	if (temp != null) {
	    temp = temp.trim();
	    if (!temp.equals(""))
		result = temp;
	}

	return result;	
    }
      
    
    /**************************************************************************
     * Read the configuration file for channel names.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/08/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/08/2006  JPW	Add support for reading Speed and Heading chan names
     * 07/12/2006  JPW  Created.
     */
    
    private void readConfigFile() throws Exception {
	
	BufferedReader bufferedReader = null;
	
	File file = new File(configFile);
	
	if (file.exists()) {
	    FileReader fileReader = new FileReader(file);
	    bufferedReader = new BufferedReader(fileReader);
	} else {
	    // Try to get the file as a resource
	    InputStream inStream =
	        getClass().getClassLoader().getResourceAsStream(configFile);
	    if (inStream == null) {
		throw new Exception(
		    "Channel name file could not be found.");
	    }
	    bufferedReader =
	        new BufferedReader( new InputStreamReader(inStream) );
	}
	
	Hashtable hashtable = new Hashtable();
	String line = bufferedReader.readLine();
	while (line != null) {
	    line = line.trim();
	    // Skip over empty lines
	    if (line.equals("")) {
		line = bufferedReader.readLine();
		continue;
	    }
	    // Skip over comment lines
	    if (line.charAt(0) == '#') {
		line = bufferedReader.readLine();
		continue;
	    }
	    // Split on any whitespace characters
	    String[] tokens = line.split("\\s");
	    if (tokens.length < 2) {
		line = bufferedReader.readLine();
		continue;
	    }
	    // Out of this line, parse the key (a code indicating what kind of
	    // channel the value refers to) and a value (containing the channel
	    // name).
	    String key = tokens[0].trim();
	    // Store the rest of the line as the value string
	    String value = line.substring(key.length());
	    if (value != null) {
		value = value.trim();
		if (value.equals("")) {
		    value = null;
		}
	    }
	    if ( (key != null) && (value != null) ) {
		hashtable.put(key,value);
	    }
	    line = bufferedReader.readLine();
	}
	
	bufferedReader.close();
	
	// Now look through the hashtable for channel names
	// 2008/02/20  WHF  Refactored.
	altChanName = extract(hashtable, "alt", altChanName);
	latChanName = extract(hashtable, "lat", latChanName);
	lonChanName = extract(hashtable, "lon", lonChanName);
	idChanName = extract(hashtable, "id", idChanName);
	typeChanName = extract(hashtable, "type", typeChanName);
	classificationChanName = extract(hashtable, "class",
			classificationChanName);
	speedChanName = extract(hashtable, "speed", speedChanName);
	headingChanName = extract(hashtable, "heading", headingChanName);
	pitchChanName = extract(hashtable, "pitch", pitchChanName);
	rollChanName = extract(hashtable, "roll", rollChanName);
    }
    
    /**************************************************************************
     * Respond to user actions.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 08/24/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/12/2006  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	if (label == null) {
	    return;
	}
	
	if (label.equals("Exit")) {
	    exitAction();
	}
	
    }
    
    /**************************************************************************
     * Respond to item state changes.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 08/24/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/24/2006  JPW  Created.
     *
     */
    
    public void itemStateChanged(ItemEvent event) {
	
	Object source = event.getItemSelectable();
	if (source == null) {
	    return;
	}
	int state = event.getStateChange();
	
	if (source == useGeoFilterCB) {
	    if (state == ItemEvent.DESELECTED) {
		bGeoFilter = false;
	    } else {
		bGeoFilter = true;
	    }
	}
	
    }
    
    /**************************************************************************
     * Exit the program.
     * <p>
     * Signal the execution loop to exit by setting bExitPlugIn to true.
     *
     * @author John P. Wilson
     *
     * @version 07/11/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/11/2006  JPW  Created.
     *
     */
    
    public void exitAction() {
	
	// Signal the execution loop to finish
	bExitPlugIn = true;
	
	// Check to see if the while loop has exited by checking
	// the value of bWhileLoopExited
	for (int i = 0; i < 9; ++i) {
	    if (bWhileLoopExited) {
		break;
	    }
	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
		// Nothing to do.
	    }
	}
	
	RBNBProcess.exit(0);
	
    }
    
    /**
      * Handle registration plugin requests.
      *
      * @author WHF
      * @version 2008/02/20
      */      
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Broken out from exec().
     * 02/22/2008  WHF  Do more dynamic registration.
    */    
    private void handleRegistration(
    		PlugIn plugin,
		Sink sink,
		PlugInChannelMap picm)
    	throws SAPIException
    {
	picm.PutTime( (System.currentTimeMillis()/1000.0), 0.0);
	if ( (picm.GetName(0).equals("...")) ||
	     (picm.GetName(0).equals("*")) )
	{
	    // Do nothing.
	}
	else
	{
	    String result=
		"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
		+"<!DOCTYPE rbnb>\n"
		+"<rbnb>\n"
		+"\t\t<size>"+1+"</size>\n"
		+"\t\t<mime>application/octet-stream</mime>\n"
		+"</rbnb>\n";
	    picm.PutDataAsString(0,result);
	    picm.PutMime(0,"text/xml");
	}
	plugin.Flush(picm);
	System.err.println(
	    (new Date()).toString() +
	    "  Responded to registration request.");
    }	
      
    /**
    * Parse the message string sent with the request.  May include
    * the following fields:
    * 1. Bounding Box (key: "BBOX")
    * 2. append channel (key: "append")
    * 3. Tactical flag, indicating that this PlugIn should also
    *    request Speed and Heading as two additional ancillary chans
    * NOTE: We only consider the first String in the String data array
    */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Broken out from exec().    
    */
    private void parseRequestMessage(PlugInChannelMap picm)
    {
	// Reset the pseudo-alt chan name
	pAltChanName = null;
	// Reset tactical flag; if user sends a "tactical=1" flag then
	// set bTacticalRequest true.  In this case, we will request
	// Speed and Heading, in addition to the other ancillary channels,
	// at the full duration (don't force 0-duration).
	//bTacticalRequest = false;
	fullDurationFlag = false;
	if (picm.GetType(0) == ChannelMap.TYPE_STRING) {
	    String[] strArray = picm.GetDataAsString(0);
	    if ( (strArray != null)    &&
		 (strArray.length > 0) &&
		 (strArray[0] != null)  &&
		 (!strArray[0].trim().equals("")) )
	    {
		String requestData = strArray[0].trim();
		char[] terminatorChars = {'&'};
		KeyValueHash kvh =
		    new KeyValueHash(requestData,terminatorChars);
		if (kvh.get("BBOX") != null) {
		    String bboxStr = kvh.get("BBOX");
		    if (bPrintDebug) {
			System.err.println(
			    "Possible BoundingBox: \"" +
			    bboxStr +
			    "\"");
		    }
		    try {
			BoundingBox tempBB = new BoundingBox(bboxStr);
			boundingBox = tempBB;
			if (bShowGUI) {
			    geoFilterLabel.setText(getBoundingBoxString());
			}
			if (bPrintDebug) {
			    System.err.println(getBoundingBoxString());
			}
		    } catch (Exception e) {
			// Must not have been a valid bounding box
		    }
		}
		if (kvh.get("append") != null) {
		    pAltChanName = kvh.get("append").trim();
		    if (pAltChanName.equals("")) {
			pAltChanName = null;
		    }
		}
		if (kvh.get("tactical") != null) {
		    if (kvh.get("tactical").trim().equals("1")) {
			//bTacticalRequest = true;
			fullDurationFlag = true;
		    }
		}
	    }
	}
    }
    
    /**
      * Adds ancillary channels if and only if they are present in the 
      *  registration map.
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Broken out from exec().    
     * 02/27/2008  WHF  Removed doTactical input, as the inputs are always
     *                  present.
    */    
    private void addAncillaryChannelsConditionally(
		ChannelMap regMap,
		String remoteSource,
		ChannelMap cm) throws SAPIException
    {
    
	if (regMap.GetIndex(
		remoteSource + classificationChanName) != -1)
	    cm.Add(remoteSource + classificationChanName);
	if (regMap.GetIndex(
		remoteSource + typeChanName) != -1)
	    cm.Add(remoteSource + typeChanName);
	if (regMap.GetIndex(
		remoteSource + idChanName) != -1)
	    cm.Add(remoteSource + idChanName);

	if (regMap.GetIndex(
		remoteSource + speedChanName) != -1)
	    cm.Add(remoteSource + speedChanName);
	if (regMap.GetIndex(
		remoteSource + headingChanName) != -1)
	    cm.Add(remoteSource + headingChanName);
	if (regMap.GetIndex(
		remoteSource + pitchChanName) != -1)
	    cm.Add(remoteSource + pitchChanName);
	if (regMap.GetIndex(
		remoteSource + rollChanName) != -1)
	    cm.Add(remoteSource + rollChanName);
    }
    
    /**
      * Retrieve from the RBNB server that track data which matches
      *  the client request.
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Broken out from exec().    
    */
    private void getMatchingTrackData(
    		String remoteSource,
		ChannelMap topLevelRegMap,
		PlugInChannelMap picm,
		Sink sink)
	throws SAPIException
    {    
	ChannelMap cm=new ChannelMap();
	cm.Add(remoteSource + altChanName);
	cm.Add(remoteSource + latChanName);
	cm.Add(remoteSource + lonChanName);
	if ( (pAltChanName != null) &&
	     (topLevelRegMap.GetIndex(pAltChanName) != -1) )
	{
	    cm.Add(pAltChanName);
	}
	else {
	    // There is no pseudo-alt channel
	    pAltChanName = null;
	}
	ChannelMap dataMap=null;
	try {
	    sink.Request(
		cm,
		picm.GetRequestStart(),
		picm.GetRequestDuration(),
		picm.GetRequestReference());
	    dataMap = sink.Fetch(timeout);
	} catch (Exception e) {
	    e.printStackTrace();
	    dataMap=new ChannelMap();
	}
	if ( (dataMap.GetIndex(remoteSource + altChanName) != -1) &&
	     (dataMap.GetIndex(remoteSource + latChanName) != -1) &&
	     (dataMap.GetIndex(remoteSource + lonChanName) != -1) )
	{
	    // We have top-level data!
	    // Now, request ancillary channel data. Unless
	    // fullDurationFlag is true, this will be a 0-dur request.
	    //cm=new ChannelMap();
	    cm.Clear();
	    addAncillaryChannelsConditionally(
	    		topLevelRegMap,
			remoteSource,
			cm			
	    );
	    ChannelMap ancillaryDataMap = new ChannelMap();
	    if (cm.NumberOfChannels() > 0) {
		// JPW 11/22/2006
		// To most reliably get synchronized data, perform an
		// absolute time fetch, where the time range requested
		// is based on the times received on the alt channel.
		// This will avoid mismatch problems that may occur
		// when we are fetching data from a live data source.

		int altChanIdx =
		    dataMap.GetIndex(remoteSource + altChanName);
		double[] altTimes = dataMap.GetTimes(altChanIdx);
		if (altTimes != null && altTimes.length > 0) {
		    double starttime = altTimes[altTimes.length - 1];
		    double duration = 0.0;
		    if (fullDurationFlag && altTimes.length > 1) {
			// Request the full duration of data
			starttime = altTimes[0];
			duration =
			   altTimes[altTimes.length - 1] - altTimes[0];
		    }
		    String requestRefStr = "absolute";
		    try {
			sink.Request(
			    cm,
			    starttime,
			    duration,
			    requestRefStr);
			ancillaryDataMap = sink.Fetch(timeout);
		    } catch (Exception e) {
			e.printStackTrace();
			ancillaryDataMap=new ChannelMap();
		    }
		}
	    }
	    addTrackChans(remoteSource, dataMap, ancillaryDataMap, picm);
	}
	else
	{
	    System.err.println("\tNo data at requested time.");
	}
    }
    
    /**
      * Retrieve RBNB server sub-track data that matches the client request.
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Broken out from exec().    
    */
    private void getMatchingSubTracks(
    		String remoteSource,
		PlugInChannelMap picm,
		Sink sink)
	throws SAPIException
    {    
	/////////////////////////////////////////////////////////
	// There were no top-level track channels; see if we have
	// any sub-tracks.
	/////////////////////////////////////////////////////////
	if (bPrintDebug) {
	    System.err.println(
		"No top level track; see if there are any sub-tracks");
	}
	// Store the names of sub-tracks (these are tracks that
	// at least have Alt, Lat, Lon channels)
	Vector tracks = new Vector();
	// Store the names of those sub-tracks which also have a
	// Classification, Type, ID, Speed or Heading channel.
	Vector ancillaryTracks = new Vector();
	// NOTE: Due to a bug in RBNB Server, we can't do data absolute
	//       requests on channels with wildcards.  However, we can
	//       do registration requests.  Therefore, we first do a
	//       registration request, then we determine all the sub-
	//       track names, then we do the data request using
	//       resolved channel names (no wildcards).
	ChannelMap cm = new ChannelMap();
	cm.Add(remoteSource + "*/" + altChanName);
	cm.Add(remoteSource + "*/" + latChanName);
	cm.Add(remoteSource + "*/" + lonChanName);
	cm.Add(remoteSource + "*/" + classificationChanName);
	cm.Add(remoteSource + "*/" + typeChanName);
	cm.Add(remoteSource + "*/" + idChanName);
	cm.Add(remoteSource + "*/" + speedChanName);
	cm.Add(remoteSource + "*/" + headingChanName);
	cm.Add(remoteSource + "*/" + pitchChanName);
	cm.Add(remoteSource + "*/" + rollChanName);
	sink.RequestRegistration(cm);
	ChannelMap regMap = sink.Fetch(timeout);
	String[] chans = regMap.GetChannelList();
	for (int i = 0; i < chans.length; ++i) {
	    String chan = chans[i];
	    // If this channel ends in altChanName then this might be
	    // data from a sub-track
	    // NOTE: The tactical data demux puts the concatenation of
	    //       data from all tracks into a folder called
	    //       "_Master".  We don't want to read data from the
	    //       chans in this folder.
	    if ( (chan.endsWith(altChanName)) &&
		 (!chan.endsWith("_Master/" + altChanName)) )
	    {
		// See if this folder contains all the needed tactical
		// data channels; if so, this is a sub-track
		String trackName =
		    chan.substring(
			0, chan.length() - altChanName.length());
		if ( (regMap.GetIndex(trackName+latChanName) != -1) &&
		     (regMap.GetIndex(trackName+lonChanName) != -1) )
		{
		    // We found a sub-track in the registration info!
		    // NOTE: This doesn't mean that this sub-track has
		    //       data of interest in the specific requested
		    //       time window - that is why we call this a
		    //       "Possible" sub-track
		    if (bPrintDebug) {
			System.err.println(
			    "Possible sub-track: " + trackName);
		    }
		    tracks.add(trackName);
		    // See if one of more of the ancillary channels
		    // exist for this sub-track (Type, ID, etc)
		    if ( (regMap.GetIndex(
				trackName+classificationChanName) != -1)     ||
			 (regMap.GetIndex(trackName+typeChanName) != -1)     ||
			 (regMap.GetIndex(trackName+idChanName) != -1)       ||
			 (regMap.GetIndex(trackName+speedChanName) != -1)    ||
			 (regMap.GetIndex(trackName+headingChanName) != -1)  ||
			 (regMap.GetIndex(trackName+pitchChanName) != -1)    ||
			 (regMap.GetIndex(trackName+rollChanName) != -1) )
		    {
			ancillaryTracks.add(trackName);
		    }
		}
	    }
	}
	if (tracks.isEmpty()) {
	    System.err.println("\tNo data at requested time.");
	} else {
	    //////////////////////////////////////////////////////
	    // One or more potential sub-tracks exist; perform the
	    // data request
	    //////////////////////////////////////////////////////
	    //cm=new ChannelMap();
	    cm.Clear();
	    for (Enumeration e = tracks.elements();
		 e.hasMoreElements();)
	    {
		String trackName = (String)e.nextElement();
		cm.Add(trackName + altChanName);
		cm.Add(trackName + latChanName);
		cm.Add(trackName + lonChanName);
	    }
	    ChannelMap dataMap=null;
	    try {
	    sink.Request(
		cm,
		picm.GetRequestStart(),
		picm.GetRequestDuration(),
		picm.GetRequestReference());
	    dataMap = sink.Fetch(timeout);
	    } catch (Exception e) {
		e.printStackTrace();
		dataMap=new ChannelMap();
	    }
	    ////////////////////////////////////////////////////////
	    // If there were any sub-tracks with ancillary channels,
	    // request ancillary data from these tracks now
	    ////////////////////////////////////////////////////////
	    ChannelMap ancillaryDataMap = new ChannelMap();
	    if (!ancillaryTracks.isEmpty()) {
		cm = new ChannelMap();
		for (Enumeration e = ancillaryTracks.elements();
		     e.hasMoreElements();)
		{
		    String trackName = (String)e.nextElement();
		    // If Alt/Lat/Lon chans exists in dataMap for this
		    // track, then we will also request the ancillary
		    // chans from this track.
		    if ( (dataMap.GetIndex(trackName+altChanName) != -1) &&
			 (dataMap.GetIndex(trackName+latChanName) != -1) &&
			 (dataMap.GetIndex(trackName+lonChanName) != -1) )
		    {
			// Make sure the ancillary channel exists
			// in regMap before requesting it
			addAncillaryChannelsConditionally(
				regMap,
				trackName,
				cm
			);
		    }
		}
		if (cm.NumberOfChannels() > 0) {
		    // JPW 11/22/2006
		    // Up above, where we were fetching data for the
		    // top-level channels, in order to most reliably
		    // get synchronized data, we perform an absolute
		    // time fetch, where the time range requested is
		    // based on the times received on the alt channel.
		    // This is done to avoid mismatch problems that may
		    // occur when we are fetching data from a live data
		    // source.  If we were to do that here, we would
		    // need to perform a separate fetch for each Track,
		    // using the particular times on that Track's alt
		    // channel.  At some point in the future, we
		    // may want to consider doing this.  For now, we
		    // will continue to do it the way we have.  If we
		    // start fetching sub-track data for FormatX or
		    // CMF creation, we will probably want to do this.
		    double starttime =
			picm.GetRequestStart() +
			picm.GetRequestDuration();
		    if (picm.GetRequestReference().equals("newest")) {
			// For a "newest" request, need to calculate
			// the starttime differently ("newest" is a
			// backward-looking request)
			starttime =
			    picm.GetRequestStart() -
			    picm.GetRequestDuration();
			if (starttime < 0.0) {
			    starttime = 0.0;
			}
		    }
		    double duration = 0.0;
		    if (fullDurationFlag) {
			// Use the full duration for this request
			starttime = picm.GetRequestStart();
			duration = picm.GetRequestDuration();
		    }
		    try {
			sink.Request(
			    cm,
			    starttime,
			    duration,
			    picm.GetRequestReference());
			ancillaryDataMap = sink.Fetch(timeout);
		    } catch (Exception e) {
			e.printStackTrace();
			ancillaryDataMap=new ChannelMap();
		    }
		}
	    }
	    ///////////////////////////////////////////////////////
	    // Go through our list of known sub-tracks and pick out
	    // the tracks that have data.
	    ///////////////////////////////////////////////////////
	    for (Enumeration e = tracks.elements();
		 e.hasMoreElements();)
	    {
		String trackName = (String)e.nextElement();
		if ( (dataMap.GetIndex(trackName+altChanName) != -1) &&
		     (dataMap.GetIndex(trackName+latChanName) != -1) &&
		     (dataMap.GetIndex(trackName+lonChanName) != -1) )
		{
		    if (bPrintDebug) {
			System.err.println(
			    "Data exists for sub-track: " + trackName);
		    }
		    addTrackChans(
			trackName, dataMap, ancillaryDataMap, picm);
		}
	    }
	}
    }
    
    /**************************************************************************
     * Main PlugIn execution loop.
     * <p>
     * Create Sink and PlugIn connections and handle PlugIn registration and
     * data requests.
     *
     * @author John P. Wilson
     *
     * @version 11/22/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2008  WHF  Refactored.
     * 11/22/2006  JPW	For fetching ancillary channel data for the top-level
     *			track: To most reliably get synchronized data, we now
     *			perform an absolute time fetch, where the time range
     *			requested is based on the times received on the alt
     *			channel.  This avoids mismatch problems that may occur
     *			when we are fetching data from a live data source.
     *			NOTE: I don't do this for the ancillary data fetch for
     *			sub-tracks, because I would have to make a separate
     *			data fetch (each with its own particular times) for
     *			each track.  If we start using sub-track data for
     *			creating CMF or FormatX, then we will probably want
     *			to start doing this.
     * 11/08/2006  JPW	Add support for reading speed and heading channels
     *			with the other ancillary channels in a 0-duration
     *			request.  This is only done if "tactical=1" is
     *			included in the message string sent with the request.
     * 07/12/2006  JPW  Created.
     *
     */
    
    public void exec() {
	
	// Not in the while loop yet, so in case there is an error and
	// we need to exit, set this true initially
	bWhileLoopExited = true;
	
	// Make Sink and PlugIn connections
	System.err.print(
	    "Open PlugIn and Sink connections to " + address + "...");
	try {
	    sink = new Sink();
	    sink.OpenRBNBConnection(address,sinkName);
	    rbnbServerName = sink.GetServerName();
	    if (rbnbServerName.charAt(0) == '/') {
		rbnbServerName = rbnbServerName.substring(1);
	    }
	    plugin=new PlugIn();
	    plugin.OpenRBNBConnection(address,pluginName);
	} catch (SAPIException e) {
	    System.err.println(
	        "Error opening Sink or PlugIn connections:\n" + e);
	    exitAction();
	}
	System.err.println("Connections are open");
	if (!plugin.GetClientName().equals(pluginName)) {
	    pluginName = plugin.GetClientName();
	    System.err.println(
	        "WARNING: The actual PlugIn name is " +
		pluginName);
	}
	// JPW 06/19/2006: Display the PlugIn name in the title bar
	if ( (bShowGUI) && (frame != null) ) {
	    frame.setTitle("TrackData  \"" + pluginName + "\"");
	}
	
	//loop handling requests - note multithreaded would be better
	//                       - does not support monitor/subscribe yet
	bWhileLoopExited = false;
	try {
	while (!bExitPlugIn) {
	    PlugInChannelMap picm = plugin.Fetch(3000);
	    long testTimeStart = System.currentTimeMillis();
	    if ( (picm.GetIfFetchTimedOut()) ||
		 (picm == null) ||
		 (picm.NumberOfChannels() == 0) )
	    {
		continue;
	    }
	    
	    if (bPrintDebug) {
		System.err.println("\n\nReceived new PICM:\n" + picm);
	    }
	    
	    // Check if this is a registration request
	    if (picm.GetRequestReference().equals("registration")) {
		handleRegistration(plugin, sink, picm);
		continue;
	    }
	    
	    String[] chanList = picm.GetChannelList();
	    String remoteSource = chanList[0];
	    // Make sure remoteSource ends in a slash
	    if (!remoteSource.endsWith("/")) {
		remoteSource = remoteSource + "/";
	    }
	    System.err.println(
		(new Date()).toString() +
		"  Source: " +
		remoteSource);
	    
	    parseRequestMessage(picm);
	    
	    // NOTE: Unfortunately, the RBNB returns an empty channel map when
	    //       making a registration request using the following type of
	    //       ChannelMap:
	    //
	    //       cm.Add(remoteSource + altChanName);
	    //       cm.Add(remoteSource + "*/" + altChanName);
	    //
	    //       Therefore, first we check to see if there are top-level
	    //       channels.  If not, then we see if there are any
	    //       sub-tracks.
	    
	    //////////////////////////////////////////////////////////////
	    // If there are top-level track chans then just use these and don't
	    // bother checking if there are sub-track channels
	    //////////////////////////////////////////////////////////////
	    // NOTE: There appears to be an RBNB problem requesting data
	    //       from channels that don't exist (we noticed this with
	    //       the NASA ER-2 mission) - if the channels don't exist,
	    //       the fetch wasn't always just returning; it would
	    //       occasionally take the full timeout time.  Therefore,
	    //       do a registration request first, and then a data
	    //       request only when we know the channels exist.
	    ChannelMap cm=new ChannelMap();
	    cm.Add(remoteSource + altChanName);
	    cm.Add(remoteSource + latChanName);
	    cm.Add(remoteSource + lonChanName);
	    cm.Add(remoteSource + classificationChanName);
	    cm.Add(remoteSource + typeChanName);
	    cm.Add(remoteSource + idChanName);
	    cm.Add(remoteSource + speedChanName);
	    cm.Add(remoteSource + headingChanName);
	    cm.Add(remoteSource + pitchChanName);
	    cm.Add(remoteSource + rollChanName);
	    if (pAltChanName != null) {
		cm.Add(pAltChanName);
	    }
	    sink.RequestRegistration(cm);
	    ChannelMap topLevelRegMap = sink.Fetch(timeout);
	    if ( (topLevelRegMap.GetIndex(remoteSource + altChanName) != -1) &&
		 (topLevelRegMap.GetIndex(remoteSource + latChanName) != -1) &&
	     	 (topLevelRegMap.GetIndex(remoteSource + lonChanName) != -1) )
	    {
		// We have a top-level track; now see if there is any track
		// data available at the client's request time.
		getMatchingTrackData(remoteSource, topLevelRegMap, picm, sink);
	    } else {
		getMatchingSubTracks(remoteSource, picm, sink);
	    }
	    
	    // Timestamp the data we send back
	    if (picm.GetRequestReference().equals("absolute")) {
	        picm.PutTime(
		    picm.GetRequestStart(),
		    picm.GetRequestDuration());
	    } else {
		// NOTE: This is a bit of a kludge to use current system time;
		//       might be better to keep track of our overall start and
		//       end time as we are going though the data and use
		//       these for the start time and duration
	        picm.PutTime(
		    System.currentTimeMillis()/1000.0,
		    picm.GetRequestDuration());
	    }
	    
	    plugin.Flush(picm);
	    long testTimeEnd = System.currentTimeMillis();
	    if (bPrintDebug) {
		System.err.println(
	            "Processing time = " +
		    (testTimeEnd - testTimeStart)/1000.0 +
		    " sec");
	    }
	} // end while loop
	} catch (SAPIException e) {
	    System.err.println("Error in execution loop: " + e);
	    bWhileLoopExited = true;
	    exitAction();
	}
	
	System.err.println("Execution loop has exited");
	System.err.print("Close RBNB connections...");
	if (sink != null) {
	    sink.CloseRBNBConnection();
	}
	if (plugin != null) {
	    plugin.CloseRBNBConnection();
	}
	System.err.println("connections closed.");
	
	bWhileLoopExited = true;
	
    } //end exec method
    
    /**************************************************************************
     * Copy channel data for a specific track from the given source ChannelMaps
     * to the given destination ChannelMap.
     * <p>
     * @param remoteSourceI        RBNB Source folder containing this track's data
     * @param sourceMapI           ChannelMap containing the core track data channels: Alt, Lat, Lon
     * @param ancillarySourceMapI  ChannelMap containing the ancillary track data channels: Classification, Type, ID (and possibly Speed and Heading)
     * @param destinationMapO      Destination ChannelMap to which the track data is copied.
     *
     * @author John P. Wilson
     *
     * @version 03/24/2008
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/24/2008  JPW  For pseudo-alt, when defining the source channel name,
     *                  don't prepend  remoteSourceI onto pAltChanName; just use
     *                  pAltChanName on its own.
     * 11/09/2006  JPW  Ancillary data is now stored in arrays
     * 11/08/2006  JPW	Add support for new ancillary chans: speed and heading
     * 10/11/2006  JPW  Add support for the pseudo-alt channel (pAlt)
     * 07/17/2006  JPW  Add ancillarySourceMapI
     * 07/12/2006  JPW  Created.
     */
    
    private void addTrackChans(
	String remoteSourceI,
	ChannelMap sourceMapI,
	ChannelMap ancillarySourceMapI,
	ChannelMap destinationMapO)
    {
	
	if (!processTrackData(remoteSourceI, sourceMapI, ancillarySourceMapI))
	{
	    System.err.println(
		"Error processing data for track " + remoteSourceI);
	    return;
	}
	
	if (alt.length == 0) {
	    // No points to add to destinationMapO
	    return;
	}
	
	try {
	    
	    // NOTE: By the time this method is called, we KNOW that there
	    //       is at least Alt, Lat, Lon channels.  The ancillary
	    //       channels (Type, TrackID, Classification, Speed and
	    //       Heading) are optional.
	    
	    // Alt
	    String srcChanName = remoteSourceI + altChanName;
	    String destChanName = remoteSourceI + vAltChanName;
	    int srcChanIdx = sourceMapI.GetIndex(srcChanName);
	    int destChanIdx = destinationMapO.Add(destChanName);
	    String mimeType = sourceMapI.GetMime(srcChanIdx);
	    if ( (mimeType != null) && (!mimeType.equals("")) ) {
		destinationMapO.PutMime(destChanIdx,mimeType);
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat32(destChanIdx, alt);
	    
	    // JPW 10/11/2006: Pseudo-Alt
	    if (pAlt != null) {
		/* 2008/02/28  WHF  Pseudo-Alt now has its own virtual channel:
		String chanName = pAltChanName;
		srcChanIdx = sourceMapI.GetIndex(chanName);
		destChanIdx = destinationMapO.Add(chanName); */
		// JPW 03/24/2008: Use pAltChanName on its own - don't prepend remoteSourceI
		srcChanName = pAltChanName;
		destChanName = remoteSourceI + vPseudoAltChanName;
		srcChanIdx = sourceMapI.GetIndex(srcChanName);
		destChanIdx = destinationMapO.Add(destChanName);
		mimeType = sourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
		destinationMapO.PutTimes(times);
		destinationMapO.PutDataAsFloat32(destChanIdx, pAlt);
	    }
	    
	    // Lat
	    srcChanName = remoteSourceI + latChanName;
	    destChanName = remoteSourceI + vLatChanName;
	    srcChanIdx = sourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    mimeType = sourceMapI.GetMime(srcChanIdx);
	    if ( (mimeType != null) && (!mimeType.equals("")) ) {
		destinationMapO.PutMime(destChanIdx,mimeType);
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat64(destChanIdx, lat);
	    
	    // Lon
	    srcChanName = remoteSourceI + lonChanName;
	    destChanName = remoteSourceI + vLonChanName;
	    srcChanIdx = sourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    mimeType = sourceMapI.GetMime(srcChanIdx);
	    if ( (mimeType != null) && (!mimeType.equals("")) ) {
		destinationMapO.PutMime(destChanIdx,mimeType);
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat64(destChanIdx, lon);
	    
	    // Track ID
	    srcChanName = remoteSourceI + idChanName;
	    destChanName = remoteSourceI + vIdChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    // If fullDurationFlag is false, only put a single value
	    //    corresponding to the last timestamp.  Otherwise, use the
	    //    full array of data.
	    if (!fullDurationFlag) {
		double[] timeArray = new double[1];
		timeArray[0] = times[ times.length - 1 ];
		destinationMapO.PutTimes(timeArray);
		// There should only be 1 element in the data array
		if (trackID.length != 1) {
		    throw new Exception("ERROR: length of trackID should be 1; it is " + trackID.length);
		}
		destinationMapO.PutDataAsString(destChanIdx, trackID[0]);
	    } else {
		for (int i = 0; i < times.length; ++i) {
		    destinationMapO.PutTime(times[i], 0.0);
		    destinationMapO.PutDataAsString(destChanIdx, trackID[i]);
		}
	    }
	    
	    // Type
	    srcChanName = remoteSourceI + typeChanName;
	    destChanName = remoteSourceI + vTypeChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    // If fullDurationFlag is false, only put a single value
	    //    corresponding to the last timestamp.  Otherwise, use the
	    //    full array of data.
	    if (!fullDurationFlag) {
		double[] timeArray = new double[1];
		timeArray[0] = times[ times.length - 1 ];
		destinationMapO.PutTimes(timeArray);
		// There should only be 1 element in the data array
		if (type.length != 1) {
		    throw new Exception("ERROR: length of type should be 1; it is " + type.length);
		}
		destinationMapO.PutDataAsString(destChanIdx, type[0]);
	    } else {
		for (int i = 0; i < times.length; ++i) {
		    destinationMapO.PutTime(times[i], 0.0);
		    destinationMapO.PutDataAsString(destChanIdx, type[i]);
		}
	    }
	    
	    // Classification
	    srcChanName = remoteSourceI + classificationChanName;
	    destChanName = remoteSourceI + vClassificationChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    // If bTacticalRequest is false, only put a single value
	    //    corresponding to the last timestamp.  Otherwise, use the
	    //    full array of data.
	    if (!fullDurationFlag) {
		double[] timeArray = new double[1];
		timeArray[0] = times[ times.length - 1 ];
		destinationMapO.PutTimes(timeArray);
		// There should only be 1 element in the data array
		if (classification.length != 1) {
		    throw new Exception("ERROR: length of classification should be 1; it is " + classification.length);
		}
		destinationMapO.PutDataAsString(destChanIdx, classification[0]);
	    } else {
		for (int i = 0; i < times.length; ++i) {
		    destinationMapO.PutTime(times[i], 0.0);
		    destinationMapO.PutDataAsString(destChanIdx, classification[i]);
		}
	    }
	    
	    // Speed - only add it to destination map if client has requested it
	    srcChanName = remoteSourceI + speedChanName;
	    destChanName = remoteSourceI + vSpeedChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat32(destChanIdx, speed);
	    
	    // Heading - only add it to destination map if client has requested it
	    srcChanName = remoteSourceI + headingChanName;
	    destChanName = remoteSourceI + vHeadingChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat32(destChanIdx, heading);
	    
	    // Pitch, roll
	    srcChanName = remoteSourceI + pitchChanName;
	    destChanName = remoteSourceI + vPitchChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat32(destChanIdx, pitch);

	    srcChanName = remoteSourceI + rollChanName;
	    destChanName = remoteSourceI + vRollChanName;
	    srcChanIdx = ancillarySourceMapI.GetIndex(srcChanName);
	    destChanIdx = destinationMapO.Add(destChanName);
	    if (srcChanIdx > -1) {
		mimeType = ancillarySourceMapI.GetMime(srcChanIdx);
		if ( (mimeType != null) && (!mimeType.equals("")) ) {
		    destinationMapO.PutMime(destChanIdx,mimeType);
		}
	    }
	    destinationMapO.PutTimes(times);
	    destinationMapO.PutDataAsFloat32(destChanIdx, roll);
	    
	} catch (Exception e) {
	    System.err.println(
		    "Caught exception trying to add channels for track " +
		    remoteSourceI
	    );
	    e.printStackTrace();
	    System.err.println();
	    return;
	}	
    }
        
    /**
      * Extracts a channel from the provided ChannelMap.
      *
      * @param createEmptyArray  if true, an array of zeros is created
      *    if no data is found.
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2008  WHF  Broken out from exec().
     * 2008/03/10  WHF  Added createEmptyArray argument.
    */      
    private float[] getAncillaryTrackData(
    		ChannelMap ancillarycmI,
		String fullChanName,
		boolean createEmptyArray
    ) { 
	float[] result;
	int chanIdx = ancillarycmI.GetIndex(fullChanName);
	
	if (chanIdx > -1) {
	    // We want the result to be an array of single-precision floats
	    if (ancillarycmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT32) {
		result = ancillarycmI.GetDataAsFloat32(chanIdx);
	    } else if (ancillarycmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT64) {
		double[] resultD = ancillarycmI.GetDataAsFloat64(chanIdx);
		// convert to array of floats
		result = new float[resultD.length];
		for (int i=0; i<resultD.length; ++i) {
		    result[i] = (float)resultD[i];
		}
		resultD = null;
	    } else {
		// We will use the default array created below
		result = null;
	    }
	} else result = null;
	
	if (result == null && createEmptyArray) {
	    // Need to specify a default array which is the same
	    // length as time array
	    result = new float[ times.length ];
	    Arrays.fill(result, -Float.MAX_VALUE);
	}
	
	return result;
    }
    
    /**
      * Extracts a channel from the provided ChannelMap.  Delegates to 
      * {@link #getAncillaryTrackData(ChannelMap, String, boolean) with 
      *   createEmptyArray equal to <b>true</b>.
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2008/03/10  WHF  Created.  
    */      
    private float[] getAncillaryTrackData(
    		ChannelMap ancillarycmI,
		String fullChanName
    ) { 
	return getAncillaryTrackData(ancillarycmI, fullChanName, true);
    }
    

    /**
      * Bounds a data array to length numPts.
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2008  WHF  Broken out from exec().    
    */          
    private float[] boundData(float[] in, int numPts)
    {   
	float[] out;
	
    	if ( in.length != numPts) {
	    if (bPrintDebug) {
		System.err.println(
		    "\tWARNING: Channel has different number of points: " +
		    in.length);
	    }
	    // Chop off extra points from the end of the data
	    out = new float[numPts];
	    System.arraycopy(in, 0, out, 0, numPts);
	} else out = in;
	
	return out;
    }
    
        /**************************************************************************
     * Is the given data point valid?  It will be valid if neither the x nor
     * the y are NaN or positive or negative MAX_VALUE or NEGATIVE_INFINITY or
     * POSITIVE_INFINITY.
     *
     * @author John P. Wilson
     *
     * @version 03/22/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/10/2008  WHF  Moved from TrackKMLPlugIn.
     * 03/22/2006  JPW  Created.
     *
     */
    
    private static boolean validDataPoint(double x, double y) {
	
	if ( (!Double.isNaN(x)) &&
	     (!Double.isNaN(y)) &&
	     (!Double.isInfinite(x)) &&
	     (!Double.isInfinite(y)) &&
	     (x != Double.MAX_VALUE) &&
	     (x != -Double.MAX_VALUE) &&
	     (y != Double.MAX_VALUE) &&
	     (y != -Double.MAX_VALUE) )
	{
	    return true;
	}
	
	return false;
	
    }
    
    /**************************************************************************
     * Calculate track heading (between 0 and 360 degrees). Ideally we
     * want to calculate heading using the current point and two points
     * back from the current point.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 04/08/2008
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
	 * 2008/04/08  WHF  Corrected heading calculation.
     * 2008/03/10  WHF  Moved here from TrackKMLPlugIn.  Removed throws clause.
     * 03/22/2006  JPW  Previously I used the current and next to current
     *			point to calculate heading.  Now, look back farther
     *			to calculate heading.  We made this change because
     *			frequently the NASA data has repeated points - which
     *			will result in heading = 0.0 and the plane icon
     *			jerking around in Google Earth
     * 02/03/2006  JPW  Created.
     *
     */
    
    private double calculateHeading() {
	
	// Check that latitude and longitude arrays are the same size
	if ( (lat == null) ||
	     (lon == null) ||
	     (lat.length != lon.length) ||
	     (lat.length == 0) )
	{
	    System.err.println(
	    "ERROR: problem with lat/lon arrays; can't calculate heading.");
	    return 0.0;
	}
	
	double lat1, lat2, lon1, lon2;
		
	if (lat.length == 1)
	{
	    // Only a single data point, can't calculate heading
	    return 0.0;
	}
	else if (!validDataPoint(lat[lat.length-1],lon[lon.length-1]))
	{
	    // The most recent point is not valid; can't calculate heading
	    return 0.0;
	}
	else if (lat.length == 2)
	{
	    // We are forced to use the 2 most recent points to calc heading
	    if (!validDataPoint(lat[0],lon[0]))
	    {
		return 0.0;
	    }
	    else
	    {
		//deltaLon = lon[1] - lon[0];
		//deltaLat = lat[1] - lat[0];
		lon2 = lon[1]; lon1 = lon[0];
		lat2 = lat[1]; lat1 = lat[0];
	    }
	}
	else
	{
	    
	    // First preference is to use the current point and two points
	    // back.  If the point two back isn't valid, try the
	    // current point and one back.  If that isn't valid, just
	    // return 0.0
	    
	    if (validDataPoint(lat[lat.length-3],lon[lon.length-3])) {
		//deltaLon = lon[lon.length-1] - lon[lon.length-3];
		//deltaLat = lat[lat.length-1] - lat[lat.length-3];
		lon2 = lon[lon.length-1]; lon1 = lon[lon.length-3];
		lat2 = lat[lat.length-1]; lat1 = lat[lat.length-3];
	    }
	    else if (validDataPoint(lat[lat.length-2],lon[lon.length-2])) {
		//deltaLon = lon[lon.length-1] - lon[lon.length-2];
		//deltaLat = lat[lat.length-1] - lat[lat.length-2];
		lon2 = lon[lon.length-1]; lon1 = lon[lon.length-2];
		lat2 = lat[lat.length-1]; lat1 = lat[lat.length-2];
	    } else {
		return 0.0;
	    }	    
	}
	
	// Convert degrees to radians:
	final double D2R = Math.PI / 180.0;
	lat1 *= D2R; lat2 *= D2R; lon1 *= D2R; lon2 *= D2R;
		
	// 2008/04/08  WHF  New approach, see:
	//  http://mathforum.org/library/drmath/view/55417.html
	// NOTE: This returns a result between -180 and 180.  TrackKML
	//  contains code to normalize this value to between 0 and 360, so it
	//  is not repeated here.
	return Math.atan2(
		Math.sin(lon2-lon1)*Math.cos(lat2),
		Math.cos(lat1)*Math.sin(lat2)
		    - Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)
	   ) / Math.PI*180.0;
	
	 /* old approach
	double deltaLon = lon2 - lon1;
	double deltaLat = lat2 - lat1;
	 
	/////////////////////////////////////////
	// Take care of the zero conditions first
	/////////////////////////////////////////
	
	if ( (deltaLat == 0.0) && (deltaLon == 0.0) ) {
	    // Moving nowhere
	    return 0.0;
	} else if ( (deltaLat == 0.0) && (deltaLon > 0.0) ) {
	    // Moving east
	    return 90.0;
	} else if ( (deltaLat == 0.0) && (deltaLon < 0.0) ) {
	    // Moving west
	    return 270.0;
	} else if ( (deltaLat > 0.0) && (deltaLon == 0.0) ) {
	    // Moving north
	    return 0.0;
	} else if ( (deltaLat < 0.0) && (deltaLon == 0.0) ) {
	    // Moving south
	    return 180.0;
	}
	
	////////////////////////////////////////
	// Calculate angle in one of 4 quadrants
	////////////////////////////////////////
	
	// Positive deltaLat, positive deltaLon
	else if ( (deltaLat > 0.0) && (deltaLon > 0.0) ) {
	    // Heading between 0 and 90 degrees
	    return Math.toDegrees( Math.atan(deltaLon/deltaLat) );
	}
	
	// Positive deltaLat, negative deltaLon
	else if ( (deltaLat > 0.0) && (deltaLon < 0.0) ) {
	    // Heading between 270 and 360 degrees
	    return
	        360.0 -
		Math.toDegrees(
		    Math.atan( Math.abs(deltaLon)/Math.abs(deltaLat) ) );
	}
	
	// Negative deltaLat, positive deltaLon
	else if ( (deltaLat < 0.0) && (deltaLon > 0.0) ) {
	    // Heading between 90 and 180 degrees
	    return
	        180.0 -
		Math.toDegrees(
		    Math.atan( Math.abs(deltaLon)/Math.abs(deltaLat) ) );
	}
	
	// Negative deltaLat, negative deltaLon
	else if ( (deltaLat < 0.0) && (deltaLon < 0.0) ) {
	    // Heading between 180 and 270 degrees
	    return
	        180.0 +
		Math.toDegrees(
		    Math.atan( Math.abs(deltaLon)/Math.abs(deltaLat) ) );
	}
	
	return 0.0; */
    }  
    
    /**************************************************************************
     * Extract, verify, and process data for a particular track from the
     * given ChannelMap.
     * <p>
     * The following items are done in this method:
     *
     * 1. Extract track channels from the given ChannelMap which correspond
     *    to the given remoteSourceI.  Also, verify the data type of each
     *    channel.  We want lat and lon to be arrays of doubles and alt to be
     *    an array of floats.  If the obtained array is floating point but just
     *    of the wrong type (for instance, we have the Lat array is float)
     *    then we convert the data to the appropriate type.  If the given array
     *    for Alt, Lat, Lon is not floating point (not floats or doubles) then
     *    this is an error - return false.
     *
     * 2. Make sure that all the necessary channels have been extracted,
     *    namely: Alt, Lat, Lon (other chans are optional).
     *
     * 3. If no data is given for Classification, Type, and ID, provide
     *    default data.
     *
     * 4. If the user has requested the extra tactical chans, Speed and
     *    Heading, then set default values (if no data is given for these
     *    chans in the ancillary channel map).
     *
     * 5. Reconcile any mismatches in the lengths of the track channels.
     *
     * 6. Compress the coordinate data based on the user's specifications
     *    (either use skip or compression).
     *
     * 7. If there is a BoundingBox, then geo-filter the lat/lon points.
     * <p>
     *
     * @param remoteSourceI  RBNB Source folder containing this track's data
     * @param cmI            ChannelMap containing the core track data channels: Alt, Lat, Lon
     * @param ancillarycmI   ChannelMap containing the ancillary track data channels: Classification, Type, ID, and possibly Speed and Heading.
     *
     * @author John P. Wilson
     *
     * @version 11/09/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2008  WHF  Refactored; added pitch and roll.
     * 11/09/2006  JPW  Ancillary data is now stored in arrays
     * 11/08/2006  JPW	Add support for new ancillary chans: speed and heading
     * 10/11/2006  JPW  Add support for the pseudo-alt channel (pAlt)
     * 08/24/2006  JPW  Add geo-filter using BoundingBox
     * 07/17/2006  JPW  Add ancillarycmI
     * 07/12/2006  JPW  Created.
     */
    
    private boolean processTrackData(
	String remoteSourceI,
	ChannelMap cmI,
	ChannelMap ancillarycmI)
    {
	
	// Reset variables
	times = null;
	alt = null;
	pAlt = null;
	lat = null;
	lon = null;
	trackID = null;
	type = null;
	classification = null;
	speed = null;
	heading = null;
	pitch = roll = null;
	
	if (cmI == null) {
	    System.err.println("No channel data:");
	    System.err.println("cmI: "+cmI);
	    return false;
	}
	
	//////
	// Alt
	//////
	int chanIdx = cmI.GetIndex(remoteSourceI + altChanName);
	if (chanIdx == -1) {
	    System.err.println(
		"Missing Alt data channel from track " + remoteSourceI);
	    return false;
	}
	// We want Alt to be an array of floats
	if (cmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT32) {
	    alt = cmI.GetDataAsFloat32(chanIdx);
	} else if (cmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT64) {
	    double[] altD = cmI.GetDataAsFloat64(chanIdx);
	    // convert to array of floats
	    alt = new float[altD.length];
	    for (int i=0; i<altD.length; ++i) {
		alt[i] = (float)altD[i];
	    }
	    altD = null;
	} else {
	    System.err.println("Wrong type for Alt channel");
	    return false;
	}
	
	/////////////
	// Pseudo-alt
	/////////////
	if (pAltChanName != null) {
	    int pAltChanIdx = cmI.GetIndex(pAltChanName);
	    if (pAltChanIdx > -1) {
		// We want Pseudo-Alt to be an array of floats
		if (cmI.GetType(pAltChanIdx) == ChannelMap.TYPE_FLOAT32)
		{
		    pAlt = cmI.GetDataAsFloat32(pAltChanIdx);
		    if (bPrintDebug) {
			System.err.println(
			    "Adding data for pseudo-alt channel: " +
			    pAltChanName);
		    }
		}
		else if (cmI.GetType(pAltChanIdx) == ChannelMap.TYPE_FLOAT64)
		{
		    double[] pAltD = cmI.GetDataAsFloat64(pAltChanIdx);
		    // convert to array of floats
		    pAlt = new float[pAltD.length];
		    for (int i=0; i<pAltD.length; ++i) {
			pAlt[i] = (float)pAltD[i];
		    }
		    pAltD = null;
		    if (bPrintDebug) {
			System.err.println(
			    "Adding data for pseudo-alt channel: " +
			    pAltChanName +
			    " (converted doubles to floats)");
		    }
		}
		else
		{
		    System.err.println(
		        "Wrong type for Pseudo-Alt channel " + pAltChanName);
		    pAlt = null;
		}
	    }
	}
	
	//////////////////////////////////////
	// Times - pulled from the Alt channel
	//////////////////////////////////////
	times = cmI.GetTimes(chanIdx);
	
	//////
	// Lat
	//////
	chanIdx = cmI.GetIndex(remoteSourceI + latChanName);
	if (chanIdx == -1) {
	    System.err.println(
		"Missing Lat data channel from track " + remoteSourceI);
	    return false;
	} else {
	    // We want lat to be an array of doubles
	    if (cmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT64) {
		lat = cmI.GetDataAsFloat64(chanIdx);
	    } else if (cmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT32) {
		float[] latF = cmI.GetDataAsFloat32(chanIdx);
		// convert to array of doubles
		lat = new double[latF.length];
		for (int i=0; i<latF.length; ++i) {
		    lat[i] = (double)latF[i];
		}
		latF = null;
	    } else {
		System.err.println("Wrong type for Lat channel");
		return false;
	    }
	}
	
	//////
	// Lon
	//////
	chanIdx = cmI.GetIndex(remoteSourceI + lonChanName);
	if (chanIdx == -1) {
	    System.err.println(
		"Missing Lon data channel from track " + remoteSourceI);
	    return false;
	} else {
	    // We want lon to be an array of doubles
	    if (cmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT64) {
		lon = cmI.GetDataAsFloat64(chanIdx);
	    } else if (cmI.GetType(chanIdx) == ChannelMap.TYPE_FLOAT32) {
		float[] lonF = cmI.GetDataAsFloat32(chanIdx);
		// convert to array of doubles
		lon = new double[lonF.length];
		for (int i=0; i<lonF.length; ++i) {
		    lon[i] = (double)lonF[i];
		}
		lonF = null;
	    } else {
		System.err.println("Wrong type for Lon channel");
		return false;
	    }
	}
	
	/////
	// ID
	/////
	chanIdx = ancillarycmI.GetIndex(remoteSourceI + idChanName);
	if ( (chanIdx > -1) &&
	     (ancillarycmI.GetType(chanIdx) == ChannelMap.TYPE_STRING) )
	{
	    trackID = ancillarycmI.GetDataAsString(chanIdx);
	}
	else
	{
	    if (bPrintDebug) {
		System.err.println(
		    "Missing Track ID data channel (or ID is wrong data " +
		    "type) for track " +
		    remoteSourceI);
	    }
	    // Parse the track name from the name of the remote source
	    // First, remove the final slash from remoteSourceI
	    String tempTrackID = remoteSourceI.substring(0, remoteSourceI.length() - 1);
	    int slashIndex = tempTrackID.lastIndexOf('/');
	    if (slashIndex != -1) {
		tempTrackID = tempTrackID.substring(slashIndex + 1);
	    }
	    if (bPrintDebug) {
		System.err.println("Track ID set to " + tempTrackID);
	    }
	    if (!fullDurationFlag) {
		// Just need an array of length 1
		trackID = new String[1];
		trackID[0] = tempTrackID;
	    } else {
		// Need an array the same length as time array
		trackID = new String[ times.length ];
		for (int i = 0; i < trackID.length; ++i) {
		    trackID[i] = tempTrackID;
		}
	    }
	}
	
	///////
	// Type
	///////
	chanIdx = ancillarycmI.GetIndex(remoteSourceI + typeChanName);
	if ( (chanIdx > -1) &&
	     (ancillarycmI.GetType(chanIdx) == ChannelMap.TYPE_STRING) )
	{
	    type = ancillarycmI.GetDataAsString(chanIdx);
	}
	else
	{
	    if (bPrintDebug) {
		System.err.println(
		    "Missing Type data channel (or Type is wrong data type) " +
		    "for track " +
		    remoteSourceI);
		System.err.println("Type set to Unknown");
	    }
	    if (!fullDurationFlag) {
		// Just need an array of length 1
		type = new String[1];
		type[0] = "Unknown";
	    } else {
		// Need an array the same length as time array
		type = new String[ times.length ];
		for (int i = 0; i < type.length; ++i) {
		    type[i] = "Unknown";
		}
	    }
	}
	
	/////////////////
	// Classification
	/////////////////
	chanIdx = ancillarycmI.GetIndex(remoteSourceI + classificationChanName);
	if ( (chanIdx > -1) &&
	     (ancillarycmI.GetType(chanIdx) == ChannelMap.TYPE_STRING) )
	{
	    classification = ancillarycmI.GetDataAsString(chanIdx);
	}
	else
	{
	    if (bPrintDebug) {
		System.err.println(
		    "Missing Classification data channel " +
		    "(or Classification is wrong data type) for track " +
		    remoteSourceI);
		System.err.println("Classification set to Unknown");
	    }
	    if (!fullDurationFlag) {
		// Just need an array of length 1
		classification = new String[1];
		classification[0] = "Unknown";
	    } else {
		// Need an array the same length as time array
		classification = new String[ times.length ];
		for (int i = 0; i < classification.length; ++i) {
		    classification[i] = "Unknown";
		}
	    }
	}
	
	///////////////////////////////
	// Speed, Heading, Pitch, Roll
	///////////////////////////////
	speed = getAncillaryTrackData(
		ancillarycmI,
		remoteSourceI + speedChanName
	);
	
	heading = getAncillaryTrackData(
		ancillarycmI,
		remoteSourceI + headingChanName,
		false       // do not create empty array, calc heading instead
	);
	
	if (heading == null) {
	    if (!fullDurationFlag)
		heading = new float[] { (float) calculateHeading() };
	    else // fill a whole array with calculated value
		java.util.Arrays.fill(
	    		heading = new float[times.length],
			(float) calculateHeading()
		);
	}

	pitch = getAncillaryTrackData(
		ancillarycmI,
		remoteSourceI + pitchChanName
	);

	roll = getAncillaryTrackData(
		ancillarycmI,
		remoteSourceI + rollChanName
	);
	
	///////////////////////////////////////////
	// Handle mismatch in length of data arrays
	///////////////////////////////////////////
	
	// A mismatch in the number of points can happen when:
	// (1) Timestamps across channels are not synchronized.  This
	//     shouldn't be the case here - if it is, then how could we
	//     possibly match up an alt/lat/lon data point?
	// (2) Data is coming from a live source and each channel is in
	//     its own RingBuffer (each channel is individually flushed)
	//     and TrackDataPlugIn makes a request for live data before all the
	//     desired channels have been flushed out for the current
	//     timestamp.  This can happen with live/realtime flights.
	//
	// If there is a mismatch in the number of points, we used to
	// take the most recent N points and chop the mismatch off the
	// oldest data.  However, in the case of the live NASA flights
	// this causes a time misalignment between the channels.  For the
	// NASA data, if data came in consistantly at 1 second intervals
	// without any gaps, then misalignment is OK.  However, if there
	// are time gaps in the data then this can cause serious mismatch
	// problems, particularly evident when putting together lat and lon
	// which are not nearly time aligned.
	//
	// Therefore, to try to keep better time alignment for the live
	// flights, we will drop off extra most-recent data points.
	//
	// An even BETTER way to do this would be to get the timepoint arrays
	// for Alt, Lat, and Lon and see where the mismatch has occurred.
	// I'll leave this as a TO-DO for now.
	
	int numAlt = alt.length;
	int numPalt = -1;
	if (pAlt != null) {
	    numPalt = pAlt.length;
	}
	int numLat = lat.length;
	int numLon = lon.length;
	// If this is a tactical request (and therefore we will have ancillary
	// data arrays containing about the same size as all other data arrays)
	// then we need to handle possible mismatches in these data arrays
	// here as well.
	int numTrackID = -1;
	int numType = -1;
	int numClassification = -1;
	int numSpeed = -1;
	int numHeading = -1;
	if (fullDurationFlag) {
	    numTrackID = trackID.length;
	    numType = type.length;
	    numClassification = classification.length;
	    numSpeed = speed.length;
	    numHeading = heading.length;
	}
	int numTimes = times.length;
	int numPts = numAlt;
	if ( (numPalt != -1) && (numPalt < numPts) ) {
	    numPts = numPalt;
	}
	if (numLat < numPts) {
	    numPts = numLat;
	}
	if (numLon < numPts) {
	    numPts = numLon;
	}
	if ( (numTrackID != -1) && (numTrackID < numPts) ) {
	    numPts = numTrackID;
	}
	if ( (numType != -1) && (numType < numPts) ) {
	    numPts = numType;
	}
	if ( (numClassification != -1) && (numClassification < numPts) ) {
	    numPts = numClassification;
	}
	if ( (numSpeed != -1) && (numSpeed < numPts) ) {
	    numPts = numSpeed;
	}
	if ( (numHeading != -1) && (numHeading < numPts) ) {
	    numPts = numHeading;
	}
	if (numTimes < numPts) {
	    numPts = numTimes;
	}
	if (bPrintDebug) {
	    System.err.println(
		"\tnumber of points (pre-filtering): " + numPts);
	}
	if (numAlt != numPts) {
	    if (bPrintDebug) {
		System.err.println(
		   "\tWARNING: Alt has different number of points: " + numAlt);
	    }
	    // Chop off extra points from the end of the data
	    float[] newalt = new float[numPts];
	    System.arraycopy(alt,0,newalt,0,numPts);
	    alt = newalt;
	}
	if ( (numPalt != -1) && (numPalt != numPts) ) {
	    if (bPrintDebug) {
		System.err.println(
		    "\tWARNING: Pseudo-alt has different number of points: " +
		    numPalt);
	    }
	    // Chop off extra points from the end of the data
	    float[] newpalt = new float[numPts];
	    System.arraycopy(pAlt,0,newpalt,0,numPts);
	    pAlt = newpalt;
	}
	if (numLat != numPts) {
	    if (bPrintDebug) {
		System.err.println(
		   "\tWARNING: Lat has different number of points: " + numLat);
	    }
	    // Chop off extra points from the end of the data
	    double[] newlat = new double[numPts];
	    System.arraycopy(lat,0,newlat,0,numPts);
	    lat = newlat;
	}
	if (numLon != numPts) {
	    if (bPrintDebug) {
		System.err.println(
		   "\tWARNING: Lon has different number of points: " + numLon);
	    }
	    // Chop off extra points from the end of the data
	    double[] newlon = new double[numPts];
	    System.arraycopy(lon,0,newlon,0,numPts);
	    lon = newlon;
	}
	if ( (numTrackID != -1) && (numTrackID != numPts) ) {
	    if (bPrintDebug) {
		System.err.println(
		    "\tWARNING: TrackID has different number of points: " +
		    numTrackID);
	    }
	    // Chop off extra points from the end of the data
	    String[] newTrackID = new String[numPts];
	    System.arraycopy(trackID,0,newTrackID,0,numPts);
	    trackID = newTrackID;
	}
	if ( (numType != -1) && (numType != numPts) ) {
	    if (bPrintDebug) {
		System.err.println(
		    "\tWARNING: Type has different number of points: " +
		    numType);
	    }
	    // Chop off extra points from the end of the data
	    String[] newType = new String[numPts];
	    System.arraycopy(type,0,newType,0,numPts);
	    type = newType;
	}
	if ( (numClassification != -1) && (numClassification != numPts) ) {
	    if (bPrintDebug) {
		System.err.println(
		 "\tWARNING: Classification has different number of points: " +
		 numClassification);
	    }
	    // Chop off extra points from the end of the data
	    String[] newClassification = new String[numPts];
	    System.arraycopy(classification,0,newClassification,0,numPts);
	    classification = newClassification;
	}
	
	if (fullDurationFlag) {
	    speed = boundData(speed, numPts);
	    heading = boundData(heading, numPts);
	    pitch = boundData(pitch, numPts);
	    roll = boundData(roll, numPts);
	}
	
	if (numTimes != numPts) {
	    // Don't need to warn the user about this - the times vector
	    // is from the Alt data channel, so if we needed to adjust Alt
	    // then we will also need to adjust times.
	    // Chop off extra points from the end of the data
	    double[] newtimes = new double[numPts];
	    System.arraycopy(times,0,newtimes,0,numPts);
	    times = newtimes;
	}
	
	/////////////////////////////
	// Geo-filter the coordinates
	/////////////////////////////
	if (bGeoFilter) {
	    geoFilterCoordinates();
	}
	
	///////////////////////
	// Compress coordinates
	///////////////////////
	compressCoordinates();
	
	numPts = alt.length;
	if (bPrintDebug) {
	    System.err.println(
		"\tnumber of alt/lat/lon points (post-filtering): " + numPts);
	}
	
	return true;
	
    }
    
    /**
      * Returns true if array lengths are logical.
      * @since 02/27/2008
      */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/09/2006  WHF  Created.  
     */      
    private boolean checkArrayLengths()
    {
	int numPts = alt.length;
	
	if ( numPts != lat.length                    ||
	     numPts != lon.length                    ||
	     pAlt != null && numPts != pAlt.length   ||
	     fullDurationFlag && (
		     numPts != trackID.length        ||
		     numPts != type.length           ||
		     numPts != classification.length ||
		     numPts != speed.length          ||
		     numPts != heading.length        ||
		     numPts != pitch.length          ||
		     numPts != roll.length ) )
	    return false; // lengths not logical
	return true;
    }
    
    /**************************************************************************
     * Geo-filter lat/lon data
     * <p>
     *
     * @author JPW
     *
     * @version 11/09/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/09/2006  JPW  Ancillary data is now stored in arrays; it will need
     *			to be filtered if bTacticalRequest is true.
     * 10/11/2006  JPW  Add support for the pseudo-alt channel (pAlt)
     * 08/24/2006  JPW  A modified version of what was in TrackFilterPlugIn
     *
     */
    
    private void geoFilterCoordinates() {
	
	int numPts = alt.length;
	// Check that all coordinate arrays are the same size
	if (!checkArrayLengths()) {
	    System.err.println(
		"ERROR: coordinate arrays not equal size; cannot geo-filter.");
	    return;
	}
	
	// Check that there is a bounding box
	if (boundingBox == null) {
	    if (bPrintDebug) {
		System.err.println("No bounding box to geo-filter the data.");
	    }
	    return;
	}
	
	// Go through each data point and see if it passes the geo filter.
	// Returned points should include end points just outside the view.
	// For example, if points 5 through 10 are in the view, PlugIn should
	// return points 4 through 11 so the displayed track doesn't appear to
	// begin/end within the bounding box.
	Vector timeV = new Vector();
	Vector altV = new Vector();
	Vector pAltV = new Vector();
	Vector latV = new Vector();
	Vector lonV = new Vector();
	Vector trackIDV = new Vector();
	Vector typeV = new Vector();
	Vector classificationV = new Vector();
	Vector speedV = new Vector();
	Vector headingV = new Vector();
	for (int i = 0; i < times.length; ++i) {
	    if (boundingBox.checkPoint(lat[i],lon[i]))
	    {
		// This point passed the filter
		timeV.add(new Double(times[i]));
		altV.add(new Float(alt[i]));
		if (pAlt != null) {
		    pAltV.add(new Float(pAlt[i]));
		}
		latV.add(new Double(lat[i]));
		lonV.add(new Double(lon[i]));
		if (fullDurationFlag) {
		    trackIDV.add(new String(trackID[i]));
		    typeV.add(new String(type[i]));
		    classificationV.add(new String(classification[i]));
		    speedV.add(new Float(speed[i]));
		    headingV.add(new Float(heading[i]));
		}
	    }
	    // JPW 11/17/2006: This is a bit of a kludge, but works for now.
	    //                 If bTacticalRequest is true, then we are getting
	    //                 data for creating CMF or FormatX objects.
	    //                 Up to this point, we've only requested data
	    //                 for creating these objects from the "_Master"
	    //                 Source, which contains data for *all* tracks.
	    //                 This being the case, we can't base our decision
	    //                 as to whether we are going to include a given
	    //                 lat/lon point on what comes before or after this
	    //                 point, because what comes before or after this
	    //                 point will typically not be from the same track.
	    else if (!fullDurationFlag)
	    {
		// This point did not pass the filter. However, there are two
		// cases where we will still add the current point:
		// 1. Add current point if the *next* point passes the filter
		if ( (i < times.length-1) &&
		     (boundingBox.checkPoint(lat[i+1],lon[i+1])) )
		{
		    timeV.add(new Double(times[i]));
		    altV.add(new Float(alt[i]));
		    if (pAlt != null) {
			pAltV.add(new Float(pAlt[i]));
		    }
		    latV.add(new Double(lat[i]));
		    lonV.add(new Double(lon[i]));
		    // JPW 11/17/2006: Because we have added the condition
		    //                 if (!bTacticalRequest) above, we will
		    //                 never go into the following if statement
		    if (fullDurationFlag) {
			trackIDV.add(new String(trackID[i]));
			typeV.add(new String(type[i]));
			classificationV.add(new String(classification[i]));
			speedV.add(new Float(speed[i]));
			headingV.add(new Float(heading[i]));
		    }
		}
		// 2. Add current point if the *previous* point passed filter
		if ( (i > 0) &&
		     (boundingBox.checkPoint(lat[i-1],lon[i-1])) )
		{
		    timeV.add(new Double(times[i]));
		    altV.add(new Float(alt[i]));
		    if (pAlt != null) {
			pAltV.add(new Float(pAlt[i]));
		    }
		    latV.add(new Double(lat[i]));
		    lonV.add(new Double(lon[i]));
		    // JPW 11/17/2006: Because we have added the condition
		    //                 if (!bTacticalRequest) above, we will
		    //                 never go into the following if statement
		    if (fullDurationFlag) {
			trackIDV.add(new String(trackID[i]));
			typeV.add(new String(type[i]));
			classificationV.add(new String(classification[i]));
			speedV.add(new Float(speed[i]));
			headingV.add(new Float(heading[i]));
		    }
		}
	    }
	}
	
	times = new double[timeV.size()];
	alt = new float[timeV.size()];
	if (pAlt != null) {
	    pAlt = new float[timeV.size()];
	}
	lat = new double[timeV.size()];
	lon = new double[timeV.size()];
	if (fullDurationFlag) {
	    trackID = new String[timeV.size()];
	    type = new String[timeV.size()];
	    classification = new String[timeV.size()];
	    speed = new float[timeV.size()];
	    heading = new float[timeV.size()];
	}
	for (int i = 0; i < timeV.size(); ++i) {
	    times[i] = ((Double)timeV.get(i)).doubleValue();
	    alt[i] = ((Float)altV.get(i)).floatValue();
	    if (pAlt != null) {
		pAlt[i] = ((Float)pAltV.get(i)).floatValue();
	    }
	    lat[i] = ((Double)latV.get(i)).doubleValue();
	    lon[i] = ((Double)lonV.get(i)).doubleValue();
	    if (fullDurationFlag) {
		trackID[i] = (String)trackIDV.get(i);
		type[i] = (String)typeV.get(i);
		classification[i] = (String)classificationV.get(i);
		speed[i] = ((Float)speedV.get(i)).floatValue();
		heading[i] = ((Float)headingV.get(i)).floatValue();
	    }
	}
	
    }
    
    /**************************************************************************
     * Compress track coordinate data.
     * <p>
     * Use one of two compression methods:
     * 1. Use a skip factor (only use 1 out of every X coordinate points)
     * 2. Use a compression algorithm. The algorithm used here is a 2D Polyline
     *    simplification via Douglas-Peucker, which removes "redundant" points
     *    from a high-resolution line based on a given tolerance.
     * <p>
     * Both methods use "numContiguousPts" of data at the tail and head.
     *
     * @author John P. Wilson
     *
     * @version 10/11/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/11/2006  JPW  Add support for the pseudo-alt channel (pAlt)
     * 07/12/2006  JPW  Created.
     *
     */
    
    private void compressCoordinates() {
	int numPts = alt.length;
	
	// Check that all coordinate arrays are the same size
	if (!checkArrayLengths()) {
	    System.err.println(
		"ERROR: coordinate arrays not equal size; cannot compress.");
	    return;
	}
	
	if ( ((bUseCoordSkip) && (numPts < targetNumPts)) ||
	     ((!bUseCoordSkip) && (numPts < 3 * numContiguousPts)) )
	{
	    // We don't have to do anything:
	    // data arrays are already fine the way they are!
	    return;
	}
	
	// Save the indeces of the elements we want in the final arrays
	Vector indexVector = new Vector();
	
	int dataPt = 0;
	
	// Write out contiguous data at the tail (oldest data)
	for (; dataPt < numContiguousPts; ++dataPt) {
	    indexVector.add(new Integer(dataPt));
	}
	
	if (bUseCoordSkip) {
	    // Write out decimated data in the middle
	    int numPointsToDecimate = numPts - (2 * numContiguousPts);
	    int skipFactor = numPointsToDecimate / targetNumPts;
	    if (skipFactor < 1) {
		skipFactor = 1;
	    }
	    for (; dataPt < (numPts - numContiguousPts);
	           dataPt = dataPt + skipFactor)
	    {
		indexVector.add(new Integer(dataPt));
	    }
	} else {
	    // Use compression algorithm
	    boolean[] bPointsToUse = LineSimp.simplify(lat, lon, tolerance);
	    for (; dataPt < (numPts - numContiguousPts); ++dataPt) {
		if (bPointsToUse[dataPt]) {
		    indexVector.add(new Integer(dataPt));
		}
	    }
	}
	
	// Write out contiguous data at the head (newest data)
	for (; dataPt < numPts; ++dataPt) {
	    indexVector.add(new Integer(dataPt));
	}
	
	//////////////////////////
	// Create the final arrays
	//////////////////////////
	
	double[] newTimes = new double[indexVector.size()];
	float[] newAlt = new float[indexVector.size()];
	float[] newPalt = new float[indexVector.size()];
	double[] newLat = new double[indexVector.size()];
	double[] newLon = new double[indexVector.size()];
	String[] newTrackID = new String[indexVector.size()];
	String[] newType = new String[indexVector.size()];
	String[] newClassification = new String[indexVector.size()];
	float[] newSpeed = new float[indexVector.size()];
	float[] newHeading = new float[indexVector.size()];
	float[] newPitch = new float[indexVector.size()];
	float[] newRoll = new float[indexVector.size()];
	
	int i = 0;
	for (Enumeration e = indexVector.elements(); e.hasMoreElements();) {
	    int idx = ((Integer)e.nextElement()).intValue();
	    newTimes[i] = times[idx];
	    newAlt[i] = alt[idx];
	    if (pAlt != null) {
		newPalt[i] = pAlt[idx];
	    }
	    newLat[i] = lat[idx];
	    newLon[i] = lon[idx];
	    if (fullDurationFlag) {
		newTrackID[i] = trackID[idx];
		newType[i] = type[idx];
		newClassification[i] = classification[idx];
		newSpeed[i] = speed[idx];
		newHeading[i] = heading[idx];
		newPitch[i] = pitch[idx];
		newRoll[i] = roll[idx];
	    }
	    ++i;
	}
	
	times = newTimes;
	alt = newAlt;
	if (pAlt != null) {
	    pAlt = newPalt;
	}
	lat = newLat;
	lon = newLon;
	if (fullDurationFlag) {
	    trackID = newTrackID;
	    type = newType;
	    classification = newClassification;
	    speed = newSpeed;
	    heading = newHeading;
	    pitch = newPitch;
	    roll = newRoll;
	}	
    }
    
    /**************************************************************************
     * Return a string representation of the current BoundingBox
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 08/24/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/24/2006  JPW  Created.
     *
     */
    
    private String getBoundingBoxString() {
	if (boundingBox == null) {
	    return
		new String(
		    "Bounding box:   Left: -180.00   Right: 180.00   " +
		    "Top: 90.00   Bottom: -90.00");
	}
	DecimalFormat df = new DecimalFormat("0.##");
	return
	    new String(
		"Bounding box:   Left: " + df.format(boundingBox.getLonLeft()) +
		"   Right: " + df.format(boundingBox.getLonRight()) +
		"   Top: " + df.format(boundingBox.getLatTop()) +
		"   Bottom: " + df.format(boundingBox.getLatBottom()));
    }
    
    /**************************************************************************
     * Parse a Google Earth bounding box message into its latitude and
     * longitude components.  Provide a method to determine whether a given
     * point is within the bounding box.
     * 
     * <p>
     * The bounding box message has the general form:
     *
     * BBOX=L,B,R,T
     *
     * where L = longitude of the box's left edge (west edge)
     *       B = latitude of the box's bottom edge (south edge)
     *       R = longitude of the box's right edge (east edge)
     *       T = latitude of the box's top edge (north edge)
     *
     * All values are in floating point degrees.
     * 
     * Notes from EMF regarding Google Earth's bounding box:
     *
     * B and T are latitude in degrees, with a range -90 to 90.  The south pole
     * is -90, equator is 0, north pole is 90.  T is always greater then B.
     *
     * L and R are longitude in degrees, with a range of -180 to 180.
     * Greenwich, England is 0, the branch cut -180/180 is in the Pacific
     * Ocean.  R is always greater than L.  If the range straddles the branch
     * cut, the value for R will exceed 180 so that R is greater than L.  In
     * Google Earth, the maximum R is approximately 320, when the view is far
     * from the surface and includes significant curvature from a pole.
     *
     * Positive longitude is to the east of Greenwich, negative is to the west.
     *
     * Examples: Just to the west of the branch cut, we can have a bounding box
     *           L/R which is 169/179. Straddling the branch cut, we can have
     *           a bounding box which is 175/184.  Just to the east of the
     *           branch cut, we can have a box which is -178/-169.  Note in
     *           all cases that L < R
     *
     * @author JPW
     *
     * @version 08/24/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/24/2006  JPW  Copied from TrackFilterPlugIn to TrackPlugIn
     *
     */
    
    public class BoundingBox {
	
	private double latBottom = 0.0;
	private double latTop = 0.0;
	private double lonLeft = 0.0;
	private double lonRight = 0.0;
	
	//
	// Constructor
	//
	public BoundingBox(String latLonStrI) throws Exception {
	    // Parse the bounding box members out of the given string
	    String[] latLonArray = latLonStrI.split(",");
	    if (latLonArray.length != 4) {
		throw new Exception(
		    "Invalid bounding box: \"" + latLonStrI + "\"");
	    }
	    try {
		lonLeft = Double.parseDouble(latLonArray[0]);
		latBottom = Double.parseDouble(latLonArray[1]);
		lonRight = Double.parseDouble(latLonArray[2]);
		latTop = Double.parseDouble(latLonArray[3]);
	    } catch (Exception e) {
		throw new Exception(
		    "Invalid bounding box: \"" + latLonStrI + "\"");
	    }
	}
	
	//
	// Check if the given point passes this geo filter
	// Note: The somewhat convoluted logic to determine if the given
	//       point passes the filter is due to the "branch cut" (that is,
	//       the 180/-180 longitude discontinuity in the Pacific).  In
	//       order to keep lonRight greater than lonLeft, if the
	//       bounding box streches across the branch cut then lonRight
	//       will be *greater* than 180
	//
	public boolean checkPoint(double latI, double lonI) {
	    if ( (latBottom <= latI) && (latI <= latTop) ) {
		if (lonRight <= 180) {
		    if ( (lonLeft <= lonI) && (lonI <= lonRight) ) {
			// The point passed the filter!
			return true;
		    }
		} else {
		    // This geo filter is across the branch cut
		    // NOTE: By definition, (-180 <= lonI <= 180) will be true
		    if ( (lonLeft <= lonI) || ((lonI+360.0) <= lonRight) ) {
			// The point passed the filter!
			return true;
		    }
		}
	    }
	    // Point failed the filter
	    return false;
	}
	
	//
	// Get the bounding box's bottom (southern-most) latitude
	//
	public double getLatBottom() {
	    return latBottom;
	}
	
	//
	// Get the bounding box's top (northern-most) latitude
	//
	public double getLatTop() {
	    return latTop;
	}
	
	//
	// Get the bounding box's left (western-most) longitude
	//
	public double getLonLeft() {
	    return lonLeft;
	}
	
	//
	// Get the bounding box's right (eastern-most) longitude
	//
	public double getLonRight() {
	    return lonRight;
	}
	
    }
    
}

