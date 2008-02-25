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


import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.SimpleTimeZone;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.KeyValueHash;
// Alternative to System.exit, so don't bring down servlet engine
import com.rbnb.utility.RBNBProcess;
// String formatter
import com.rbnb.utility.ToString;
import com.rbnb.utility.Utility;

/*
//JPW 05/31/2007: To marshall KML objects
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.rbnb.kml.*;
*/

/******************************************************************************
 * Produce KML documents for Google Earth which contain track data.
 * <p>
 * The PlugInChannelMap fetched from the PlugIn connection should contain a
 * single "channel", which is actually the name of either a Source or sub-
 * Folder within a Source.  The left-most portion of this channel should be
 * the name of a TrackPlugIn.  A request is made to this TrackPlugIn, which in
 * turn will request all the low-level RIF channels for track data.  The
 * ChannelMap returned from TrackPlugIn will either:
 *     1. Directly contain 3 position channels: Alt, Lat, Lon
 *     2. Contain sub-Folders which, in turn, contain Alt, Lat, Lon channels
 * The type of KML document returned by this PlugIn will depend on which
 * category of information is in the given Source/Folder, as follows:
 *     1. If the specified Source/Folder directly contains Alt, Lat, Lon, then
 *        this PlugIn returns a KML with a LineString showing this one track.
 *     2. If the specified Source/Folder contains sub-Folders with Alt, Lat,
 *        and Lon channels then the PlugIn returns one of the following:
 *		a. KML document containing a separate NetworkLink for each
 *		   of the sub-Folders.  Google Earth will then make a separate
 *		   request for each of the NetworkLink URLs.
 *		b. "Consolidated" KML document containing data for each track.
 *	  "Consolidated" mode is turned on using the "-C" command line flag.
 *
 * @author John P. Wilson
 *
 * @version 07/23/2007
 */

/*
 * Copyright 2005 - 2007 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 2008/02/25  WHF      Added heading, and sign + bias correction dialog.
 * 02/22/2008  WHF      Added pitch/roll channels to KML.
 * 07/23/2007  JPW	Add "-u" command line options.  This allows a user to
 *			specify a base URL for fetching Placemark icons.  Thus,
 *			instead of bundling icons with the KML file into a
 *			KMZ which is returned to the client, only a KML is
 *			returned.  The Google Earth client will then separately
 *			fetch the icon files, as specified in the KML.
 * 06/04/2007  JPW	Add Icon Scale menu; scale icons (3D and non-3D)
 *			according to this scale.
 * 05/31/2007  JPW	Add support to display a single 3D airplane (from a
 *			".dae" model) as the track icon.
 * 12/01/2006  JPW	Clean up how colors are defined and handled:
 *			    - add Hashtable colors
 *			    - change getHexColorStr() to use Hashtable colors
 * 11/30/2006  JPW	Add custom Type and Classification color mapping
 *			support in the config file.
 * 11/27/2006  EMF	Add support for munge-settable display parameter
 *			options in a user request, using the following
 *			key/value options:
 *			Key		Value options
 *			--------------------------------------------------------------------------
 *			KmlLabel	ID Alt LatLon AltLatLon Type Class None <literal string>
 *			KmlSort		ID Type Class
 *			KmlIcon		Type Class Dot Square Airplane None
 *			KmlColor	Type Class Red Green Blue Yellow Purple Aqua <literal string bbggrr>
 *			KmlCurtain	true false
 * 11/15/2006  JPW	Add "-d" flag to indicate that the user would like
 *			debug info printed; otherwise, just print minimal
 *			debug output.
 * 10/26/2006  EMF      Catch exceptions from Sink.Request/Fetch calls,
 *                      create empty ChannelMaps.
 * 09/25/2006  JPW	Change name from KMLPlugIn to TrackKMLPlugIn
 * 09/15/2006  JPW	Add support for displaying pseudo-altitude; if the
 *			channel map returned from Sink request comes back
 *			with an extra channel, then this is the pseudo-alt
 *			channel that we will display.
 * 09/13/2006  JPW	Set menu options and variables correctly based on
 *			config file settings.
 *			Remove "track_name" option from config file (Label
 *			has both <Custom> and None options).
 * 08/23/2006  JPW	Migrate the following features from KMLTrackPlugIn:
 *			1. Display current data values with the track icon's
 *			   name; RBNB chan names are obtained from "label_chan"
 *			   field in the config file and stored in Hashtable
 *			   labelChans.
 *			2. Add "track_name" tag to the config file and a new
 *			   variable, trackNameFromConfigFile.  This variable is
 *			   used to specify the track name when labelByID = true
 * 07/11/2006  JPW	Break KMLTrackPlugIn into KMLPlugIn and TrackPlugIn;
 *			KMLPlugIn will focus on KML presentation of track data;
 *			TrackPlugIn will focus on low-level requests for needed
 *			data channels and also perform data filtering.
 * 04/11/2006  EMF      Class no longer extends Frame, which caused problems
 *                      when running remotely with -g option.
 * 04/07/2006  EMF      Added -f and -g arguments, changed config file name.
 * 03/07/2006  JPW	Take out "LookAt" objects that were just hard-wired
 *			to look at NASA Dryden.  The only place that still has
 *			"LookAt" is in writeSingleTrackKML().
 * 02/06/2006  JPW	Ignore alt/lat/lon chans in a "_Master" folder.
 * 01/11/2006  JPW	Change name to from KMLPlugIn to KMLTrackPlugIn
 * 11/20/2005  JPW	Created.
 *
 */

public class TrackKMLPlugIn implements ActionListener, ItemListener {
    
    // JPW 12/01/2006: Define colors; this is initialized in the constructor
    private static Hashtable colors = new Hashtable();
    
    // JPW 11/30/2006: Color by TYPE
    private String TYPE_AIR_COLOR;
    private String TYPE_SPECIAL_POINT_COLOR;
    private String TYPE_EMERGENCY_POINT_COLOR;
    private String TYPE_FORWARDED_COLOR;
    private String TYPE_GROUND_COLOR;
    private String TYPE_MUNITION_COLOR;
    private String TYPE_SPACE_COLOR;
    private String TYPE_REFERENCE_POINT_COLOR;
    private String TYPE_SURFACE_COLOR;
    private String TYPE_SUBSURFACE_COLOR;
    private String TYPE_ELECTRONIC_WARFARE_COLOR;
    private String TYPE_UNKNOWN_COLOR;
    private String TYPE_DEFAULT_COLOR;
    
    // JPW 11/30/2006: Color by CLASSIFICATION
    private String CLASS_ASSUMED_FRIEND_COLOR;
    private String CLASS_FRIEND_COLOR;
    private String CLASS_HOSTILE_COLOR;
    private String CLASS_NEUTRAL_COLOR;
    private String CLASS_PENDING_COLOR;
    private String CLASS_SUSPECT_COLOR;
    private String CLASS_UNKNOWN_COLOR;
    private String CLASS_DEFAULT_COLOR;
    
    // Channels names returned from TrackPlugIn
    private String altChanName = "Alt";
    private String headingChanName = "Heading";
    private String latChanName = "Lat";
    private String lonChanName = "Lon";
    private String speedChanName = "Speed";
    private String idChanName = "TrackID";
    private String typeChanName = "Type";
    private String classificationChanName = "Classification";
    private final static String pitchChanName = "Pitch";
    private final static String rollChanName = "Roll";    
    
    // RBNB connections
    private String address = "localhost:3333";
    private String rbnbServerName = null;
    private String sinkName = "TrackKMLSink";
    // WebDAV/Tomcat or TimeDrive server address; this is only used
    // in NetworkLink URLs
    private String webdavAddress = "localhost:80";
    private Sink sink = null;
    private String pluginName = "TrackKML";
    private PlugIn plugin = null;
    // Which web application server will be used, Tomcat 4 or 5?
    // (This effects the format of the URL specified in the NetworkLink)
    private boolean bTomcat4 = false;
    
    // NetworkLink periodic refresh interval;
    // can be set via the "-r" command line option
    // refreshInterval = 0.0 means don't do any refreshes
    private double refreshInterval = 2.0;
    
    // Fetch timeout; can be set via the "-t" command line option
    private long timeout=60000;
    
    // Variables to store data for one track
    // JPW 05/31/2007: Add array of times (taken from alt channel); this is
    //                 used if we want to add placemarks at each track point
    //                 which are taged with their start and end times.
    private double[] time = null;
    private float[] alt = null, heading, pitch, roll;
    private float[] pAlt = null;
    private double[] lat = null;
    private double[] lon = null;
    private double[] speed = null;
    private String trackID = null;
    private String type = null;
    private String classification = null;
    
    // Elevation offset (in meters) for the view from the current UAV position;
    // this can be specified by the "-e" command line option.
    private double viewpointElevationOffset = 5000.0;
    // No longer have a viewpoint text field
    // private TextField viewpointElevationOffsetTF = null;
    
    // For requests that have sub-tracks, give a "consolidated" response?
    // A consolidated response is a single KML document which contains track
    // data for all of the known tracks. If this is false, then in response to
    // a top-level request we will provide a KML which has a series of
    // NetworkLinks (one for each track).
    private boolean bConsolidatedResponse = false;
    
    private Vector iconsUsed=null;
    
    // Store the track names
    Vector tracks = new Vector();
    // Store the sorted track names
    Hashtable sortmap = new Hashtable();
    
    // Execution loop control variables
    private boolean bWhileLoopExited = false;
    private boolean bExitPlugIn = false;
    
    static final int ID             = 0;
    static final int TYPE           = 1;
    static final int CLASSIFICATION = 2;
    static final int CONSTANT       = 3;
    static final int NONE           = 4;
    static final int ALT            = 5;
    static final int LATLON         = 6;
    static final int ALTLATLON      = 7;
    
    // constantLabel, constantColor, iconName, etc are the values used for
    //     the current request; these values could have been parsed from the
    //     munge sent in with the request
    // constantLabelG, constantColorG, iconNameG, etc are the values currently
    //     specified in the GUI.  If the user's request doesn't override any
    //     values via a munge, then these are the values that will be used to
    //     fulfill the request.
    private String constantLabel = "FlightTrack";
    private String constantLabelG = "FlightTrack";
    private String constantColor;
    private String constantColorG;
    // This is the label string for the custom color menu item; note that this
    // string will be a valid 6-digit HEX number (bbggrr) but *will not*
    // include the prepended "ff" opacity indicator.
    private String constantColorMenuLabel;
    private String colorFromConfigFile = "";
    private int previousColorByIndex = 0; // previous menu selection, for undo's
    private int previousLabelByIndex = 0; // previous menu selection, for undo's
    private int previousScaleByIndex = 0; // previous menu selection, for undo's
    private String iconName = "Red Dot";
    private String iconNameG = "Red Dot";
    private int labelBy = ID;
    private int labelByG = ID;
    private int sortBy = ID;
    private int sortByG = ID;
    private int iconBy = TYPE;
    private int iconByG = TYPE;
    private int colorBy = CLASSIFICATION;
    private int colorByG = CLASSIFICATION;
    private boolean showCurtain = false;
    private boolean showCurtainG = false;
    
    // JPW 06/04/2007: Add icon scale, used for scaling 3D icon
    private double iconScale = 1.0;
    private double iconScaleG = 1.0;
    // This is the label string for the custom icon scale menu item
    private String iconScaleMenuLabel = "";
    
    private String configFile = "TrackResources/KMLConfig.txt";
    private String iconsDirectory = "TrackResources/icons/";
    
    // The frame won't be initialized if user has specified "-g" flag (no GUI)
    private boolean showGUI = true;
    private Frame frame = null;
    private Choice[] choice=null;
    private Checkbox checkbox=null;
    private boolean showTactical=false;
    
    // Extra data (such as a Google Earth bounding box) that came in with
    // the data request
    private String requestData = null;
    
    // JPW 08/23/2006: Display current data values with the track icon's name;
    //                 RBNB chan names are obtained from "label_chan" field
    //                 in the config file and stored in Hashtable labelChans.
    //                 The Hashtable "keys" are channel names to request; the
    //                 "values" are each a String array of length 2: the first
    //                 String is the channel nickname to display and the second
    //                 String is a String representation of the data to display
    private Hashtable labelChans = null;
    // Since Hashtables don't preserve order, this Vector will store the
    // keys in the user's desired order for display
    private Vector labelChansV = null;
    
    // JPW 09/15/2006: Options parsed from the request data String; to support
    //                 the display of pseudo-altitude.
    private double pseudoAltScale = 1.0;
    private double pseudoAltOffset = 0.0;
    private boolean bUsePseudoAltOffset = false;
    
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
     * 11/20/2005  JPW  Created.
     *
     */
    
    public static void main(String[] args) {
	(new TrackKMLPlugIn(args)).exec();
    }
    
    public TrackKMLPlugIn(String[] args) {
	
	// Initialize the Hashtable of colors
	// Colors have a prepended "ff" indicating opaque presentation
	// The remaining 6-digits in the hex color are: bbggrr
	colors.put("black","ff000000");
	colors.put("white","ffffffff");
	colors.put("blue","ffff0000");
	colors.put("green","ff00ff00");
	colors.put("red","ff0000ff");
	colors.put("yellow","ff00ffff");
	colors.put("purple","ffff00ff");
	colors.put("orange","ff0099ff");
	colors.put("dark green","ff006600");
	colors.put("dark purple","ff990099");
	colors.put("dark orange","ff0066ff");
	colors.put("aqua","ffffff00");
	colors.put("blue aqua","ffffba00");
	colors.put("dark aqua","ff999900");
	colors.put("pink","ffb469ff");
	
	// Set Type colors
	TYPE_AIR_COLOR = getHexColorStr("blue");
	TYPE_SPECIAL_POINT_COLOR = getHexColorStr("green");
	TYPE_EMERGENCY_POINT_COLOR = getHexColorStr("red");
	TYPE_FORWARDED_COLOR = getHexColorStr("purple");
	TYPE_GROUND_COLOR = getHexColorStr("orange");
	TYPE_MUNITION_COLOR = getHexColorStr("dark green");
	TYPE_SPACE_COLOR = getHexColorStr("dark orange");
	TYPE_REFERENCE_POINT_COLOR = getHexColorStr("yellow");
	TYPE_SURFACE_COLOR = getHexColorStr("dark purple");
	TYPE_SUBSURFACE_COLOR = getHexColorStr("blue aqua");
	TYPE_ELECTRONIC_WARFARE_COLOR = getHexColorStr("dark aqua");
	TYPE_UNKNOWN_COLOR = getHexColorStr("white");
	TYPE_DEFAULT_COLOR = getHexColorStr("pink");
	
	// Set Classification colors
	CLASS_ASSUMED_FRIEND_COLOR = getHexColorStr("aqua");
	CLASS_FRIEND_COLOR = getHexColorStr("aqua");
	CLASS_HOSTILE_COLOR = getHexColorStr("red");
	CLASS_NEUTRAL_COLOR = getHexColorStr("green");
	CLASS_PENDING_COLOR = getHexColorStr("yellow");
	CLASS_SUSPECT_COLOR = getHexColorStr("red");
	CLASS_UNKNOWN_COLOR = getHexColorStr("yellow");
	CLASS_DEFAULT_COLOR = getHexColorStr("orange");
	
	// Set default constant color selection
	constantColor = getHexColorStr("red");
	constantColorG = getHexColorStr("red");
	// "constantColorMenuLabel" should only be the 6-digit HEX color;
	// initialize it by taking off the leading "ff" opacity specifier
	// from "constantColor"
	constantColorMenuLabel = constantColor.substring(2);
	
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
	    // 'C'
	    // This pertains to top-level requests only (requests that have
	    // subtracks); give a "consolidated" response, that is, a
	    // single KML document which contains track data for all of the
	    // known tracks.
	    //
	    if (ah.checkFlag('C')) {
		bConsolidatedResponse = true;
	    }
	    //
	    // 'd' Print debug
	    //
	    if (ah.checkFlag('d')) {
		bPrintDebug = true;
	    }
	    //
	    // 'e' viewpoint elevation offset
	    //
	    if (ah.checkFlag('e')) {
		try {
		    String tempOffsetStr = ah.getOption('e');
		    if (tempOffsetStr != null) {
			double tempOffset = Double.parseDouble(tempOffsetStr);
			viewpointElevationOffset = tempOffset;
		    } else {
			System.err.println(
			    "WARNING: Null argument to the \"-e\"" +
			    " command line option.");
		    }
		} catch (NumberFormatException nfe) {
		    System.err.println(
		        "ERROR: The elevation offset specified with the " +
			"\"-e\" flag is not a number; value ignored.");
		}
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
	    if (ah.checkFlag('g')) showGUI=false;
	    
	    //
	    // 'h' Help
	    //
	    if (ah.checkFlag('h')) {
		System.err.println("TrackKMLPlugIn command line options");
		System.err.println("   -a <RBNB address>");
		System.err.println("       default: localhost:3333");
		System.err.println("   -C");
		System.err.println("       NOTE: The presense of the '-C' command line option indicates");
		System.err.println("             that TrackKMLPlugIn returns a consolidated response");
		System.err.println("             (that is, don't use NetworkLinks).");
		System.err.println("   -d      Print debug to console.");
		System.err.println("   -e <viewpoint elevation offset>");
		System.err.println("       default: 5000");
		System.err.println("   -f <config file>");
		System.err.println("       default: TrackResources/KMLConfig.txt");
		System.err.println("   -g (do not display GUI)");
		System.err.println("   -h (display this help message)");
		System.err.println("   -n <PlugIn name>");
		System.err.println("       default: TrackKML");
		System.err.println("   -r <NetworkLink refresh interval, sec>");
		System.err.println("       default: 2.0 seconds");
		System.err.println("       NOTE: This refresh rate is only used in NetworkLinks.  This option");
		System.err.println("             isn't used if the user specifies consolidated responses (the");
		System.err.println("             '-C' command line option).");
		System.err.println("       NOTE: \"-r 0\" means no updates");
		System.err.println("   -s (show GUI options for tactical data sources)");
		System.err.println("   -t <Fetch timeout, in milliseconds>");
		System.err.println("       default: 60000 milliseconds");
		System.err.println("   -T <4 | 5>");
		System.err.println("       default: 5");
		System.err.println("       NOTE: This command line option is used to specify the version of");
		System.err.println("             the Tomcat server, either version 4 or version 5. This is");
		System.err.println("             only used in NetworkLinks.  This option isn't used if the user");
		System.err.println("             specifies consolidated responses (the '-C' command line option).");
		System.err.println("   -u <base URL for fetching icons>");
		System.err.println("       NOTE: The base URL must start with \"http://\" or \"https://\"");
		System.err.println("   -w <Tomcat or TimeDrive server address to use in NetworkLink URLs>");
		System.err.println("       default: localhost:80");
		System.err.println("       NOTE: This option isn't used if the user specifies consolidated");
		System.err.println("             responses (the '-C' command line option).");
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
	    // 's' show Label By in GUI
	    //
	    if (ah.checkFlag('s')) {
		showTactical=true;
	    }
	    //
	    // 'r' Refresh interval
	    //
	    if (ah.checkFlag('r')) {
		try {
		    String refreshStr = ah.getOption('r');
		    if (refreshStr != null) {
			double refresh = Double.parseDouble(refreshStr);
			if (refresh >= 0.0) {
			    refreshInterval = refresh;
			} else {
			    System.err.println(
			    	"WARNING: Specified refresh interval is " +
				"negative; value ignored.");
			}
		    } else {
			System.err.println(
			    "WARNING: Null argument to the \"-r\"" +
			    " command line option.");
		    }
		} catch (NumberFormatException nfe) {
		    System.err.println(
		        "ERROR: The refresh interval specified with the " +
			"\"-r\" flag is not a number; value ignored");
		}
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
	    //
	    // 'T' Tomcat version; must be either 4 or 5
	    //
	    if (ah.checkFlag('T')) {
		String tomcatStr = ah.getOption('T');
		if (tomcatStr != null) {
		    if (tomcatStr.equals("4")) {
			bTomcat4 = true;
		    } else if (tomcatStr.equals("5")) {
			bTomcat4 = false;
		    } else {
			System.err.println(
			    "WARNING: Unrecognized argument to the \"-T\"" +
			    "command line option.\nMust be either \"4\"" +
			    " or \"5\"; you specified " + tomcatStr);
		    }
		} else {
		    System.err.println(
			"WARNING: Null argument to the \"-T\"" +
			" command line option.");
		}
	    }
	    //
	    // JPW 07/23/2007: User can specify a URL for fetching icons
	    // 'u' Base URL for fetching icons
	    //
	    if (ah.checkFlag('u')) {
		String urlStr = ah.getOption('u');
		if (urlStr != null) {
		    // Make sure this starts with "http://" or "https://"
		    if ( !urlStr.startsWith("http://") &&
			 !urlStr.startsWith("https://") )
		    {
			System.err.println(
			"WARNING: Illegal \"-u\" argument: " +
			" didn't start with \"http://\" or \"https://\".");
		    }
		    else
		    {
			iconsDirectory = urlStr;
			// Make sure this ends with a '/'
			if (!iconsDirectory.endsWith("/")) {
			    iconsDirectory = iconsDirectory + "/";
			}
		    }
		} else {
		    System.err.println(
			"WARNING: Null argument to the \"-u\"" +
			" command line option.");
		}
	    }
	    //
	    // 'w' WebDAV/Tomcat server address
	    //
	    if (ah.checkFlag('w')) {
		String addrStr = ah.getOption('w');
		if (addrStr != null) {
		    webdavAddress = addrStr;
		} else {
		    System.err.println(
			"WARNING: Null argument to the \"-w\"" +
			" command line option.");
		}
	    }
	} catch (Exception e) {
	    System.err.println("TrackKMLPlugIn argument exception "+e.getMessage());
	    e.printStackTrace();
	    RBNBProcess.exit(0);
	}
	
	System.err.println("\nInput Options:");
	System.err.println("RBNB server address: " + address);
	System.err.println(
	    "Viewpoint elevation offset: " + viewpointElevationOffset);
	System.err.println("Desired PlugIn name: " + pluginName);
	if (bConsolidatedResponse) {
	    System.err.println(
	        "Tracks will be returned in a consolidated fashion " +
		"(in 1 KML file; no NetworkLinks)");
	} else {
	    // Options that are only used in NetworkLinks
	    System.err.println(
	        "GoogleEarth refresh interval: " + refreshInterval);
	    if (refreshInterval == 0.0) {
		System.err.println(
	            "   (NOTE: NO AUTOMATIC REFRESHES WILL BE DONE)");
	    }
	    if (bTomcat4) {
		System.err.println(
		    "Build NetworkLink URLs for Tomcat version 4");
	    } else {
		System.err.println(
		    "Build NetworkLink URLs for Tomcat version 5");
	    }
	    System.err.println(
	        "Tomcat/TimeDrive server address: " + webdavAddress);
	}
	System.err.println("RBNB Fetch timeout: " + timeout);
	if (iconsDirectory.startsWith("http")) {
	    System.err.println(
	    	"Base URL for fetching icon files: " + iconsDirectory);
	}
	
	// Read the configuration file
	try {
	    readConfigFile();
	} catch (Exception e) {
	    System.err.println(
	        "Exception reading configuration file:\n" +
		e +
		"\nUsing default display values.\n\n");
	}
	
	System.err.print("\n");
	
	//EMF 4/11/06: only perform GUI operations if showing it, otherwise
	//             XHOST exceptions when running remotely
	if (showGUI) {
	    frame = new Frame("TrackKMLPlugIn");
	    
	    // Add File menu
	    /*
	    Font font = new Font("Dialog", Font.PLAIN, 12);
	    MenuBar menuBar = new MenuBar();
	    menuBar.setFont(font);
	    Menu menu = new Menu("File");
	    menu.setFont(font);
	    MenuItem menuItem = new MenuItem("Exit");
	    menuItem.setFont(font);
	    menuItem.addActionListener(this);
	    menuItem.setEnabled(true);
	    menu.add(menuItem);
	    menuBar.add(menu);
	    frame.setMenuBar(menuBar);
	    */
	    
	    // Don't add the elevation offset field
	    /*
	    viewpointElevationOffsetTF =
		new TextField(Double.toString(viewpointElevationOffset), 20);
	    viewpointElevationOffsetTF.addActionListener(this);
	    Label tempLabel =
		new Label("Viewpoint elevation offset (m)",Label.LEFT);
	    gbc.insets = new Insets(15,15,15,5);
	    Utility.add(guiPanel,tempLabel,gbl,gbc,0,0,1,1);
	    gbc.insets = new Insets(15,0,15,15);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 100;
	    gbc.weighty = 100;
	    Utility.add(guiPanel,viewpointElevationOffsetTF,gbl,gbc,1,0,1,1);
	    */
	    
	    frame.setFont(new Font("Dialog", Font.PLAIN, 12));
	    GridBagLayout gbl = new GridBagLayout();
	    Panel guiPanel = new Panel(gbl);
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.CENTER;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 100;
	    gbc.weighty = 100;
	    
	    //make choice pulldowns
	    choice = new Choice[5];
	    
	    //label by
	    gbc.insets = new Insets(5,5,5,2);
	    Utility.add(guiPanel,new Label("Label",Label.RIGHT),gbl,gbc,0,0,1,1);
	    choice[0]=new Choice();
	    choice[0].add("by ID");
	    if (showTactical) choice[0].add("by Type");
	    if (showTactical) choice[0].add("by Class");
	    choice[0].add("by Alt");
	    choice[0].add("by Lat,Lon");
	    choice[0].add("by Alt,Lat,Lon");
	    choice[0].add("None");
	    choice[0].add("<Custom>");
	    gbc.insets = new Insets(5,0,5,5);
	    Utility.add(guiPanel,choice[0],gbl,gbc,1,0,1,1);
	    choice[0].addItemListener(this);
	    // Choose the right menu option based on user's selection in config file
	    if (labelByG == TYPE) {
		choice[0].select("by Type");
	    } else if (labelByG == CLASSIFICATION) {
		choice[0].select("by Class");
	    } else if (labelByG == ALT) {
		choice[0].select("by Alt");
	    } else if (labelByG == LATLON) {
		choice[0].select("by Lat,Lon");
	    } else if (labelByG == ALTLATLON) {
		choice[0].select("by Alt,Lat,Lon");
	    } else if (labelByG == NONE) {
		choice[0].select("None");
	    } else if (labelByG == CONSTANT) {
		choice[0].insert(constantLabelG,choice[0].getItemCount()-1);
		choice[0].select(choice[0].getItemCount()-2);
	    } else {
		// The default
		labelByG = ID;
		choice[0].select("by ID");
	    }
	    
	    //sort by
	    if (showTactical) {
		gbc.insets = new Insets(5,0,5,2);
		Utility.add(guiPanel,new Label("Sort",Label.RIGHT),gbl,gbc,2,0,1,1);
	    }
	    choice[1]=new Choice();
	    choice[1].add("by ID");
	    choice[1].add("by Type");
	    choice[1].add("by Class");
	    if (showTactical) {
		gbc.insets = new Insets(5,0,5,5);
		Utility.add(guiPanel,choice[1],gbl,gbc,3,0,1,1);
	    }
	    choice[1].addItemListener(this);
	    // Choose the right menu option based on user's selection in config file
	    if (sortByG == TYPE) {
		choice[1].select(1);
	    } else if (sortByG == CLASSIFICATION) {
		choice[1].select(2);
	    } else {
		// The default
		sortByG = ID;
		choice[1].select(0);
	    }
	    
	    //icon by
	    gbc.insets = new Insets(5,0,5,2);
	    Utility.add(guiPanel,new Label("Icon",Label.RIGHT),gbl,gbc,4,0,1,1);
	    choice[2]=new Choice();
	    if (showTactical) choice[2].add("by Type");
	    if (showTactical) choice[2].add("by Class");
	    choice[2].add("Red Dot");
	    choice[2].add("Red Square");
	    choice[2].add("Airplane");
	    choice[2].add("3D Airplane");
	    choice[2].add("None");
	    gbc.insets = new Insets(5,0,5,5);
	    Utility.add(guiPanel,choice[2],gbl,gbc,5,0,1,1);
	    choice[2].addItemListener(this);
	    // Choose the right menu option based on user's selection in config file
	    if (iconByG == CLASSIFICATION) {
		choice[2].select("by Class");
	    } else if ( (iconByG == CONSTANT) && (iconNameG.equals("Red Dot")) ) {
		choice[2].select("Red Dot");
	    } else if ( (iconByG == CONSTANT) && (iconNameG.equals("Red Square")) ) {
		choice[2].select("Red Square");
	    } else if ( (iconByG == CONSTANT) && (iconNameG.equals("Airplane")) ) {
		choice[2].select("Airplane");
	    } else if ( (iconByG == CONSTANT) && (iconNameG.equals("3D Airplane")) ) {
		choice[2].select("3D Airplane");
	    } else if (iconByG == NONE) {
		choice[2].select("None");
	    } else {
		// The default
		if (showTactical) {
		    iconByG = TYPE;
		    choice[2].select("by Type");
		} else {
		    iconByG = CONSTANT;
		    iconNameG = "Red Dot";
		    choice[2].select("Red Dot");
		}
	    }
	    
	    // JPW 06/04/2007: Add 3D icon scale
	    gbc.insets = new Insets(5,0,5,2);
	    Utility.add(guiPanel,new Label("Icon Scale",Label.RIGHT),gbl,gbc,6,0,1,1);
	    choice[3]=new Choice();
	    choice[3].add("0.1");
	    choice[3].add("0.2");
	    choice[3].add("0.5");
	    choice[3].add("1.0");
	    choice[3].add("2.0");
	    choice[3].add("5.0");
	    choice[3].add("10.0");
	    choice[3].add("20.0");
	    choice[3].add("50.0");
	    choice[3].add("100.0");
	    choice[3].add("200.0");
	    choice[3].add("500.0");
	    choice[3].add("1000.0");
	    choice[3].add("<Custom>");
	    gbc.insets = new Insets(5,0,5,5);
	    Utility.add(guiPanel,choice[3],gbl,gbc,7,0,1,1);
	    choice[3].addItemListener(this);
	    // Choose the right menu option based on user's selection in config file
	    if (iconScaleG == 0.1) {
		choice[3].select("0.1");
	    } else if (iconScaleG == 0.2) {
		choice[3].select("0.2");
	    } else if (iconScaleG == 0.5) {
		choice[3].select("0.5");
	    } else if (iconScaleG == 1.0) {
		choice[3].select("1.0");
	    } else if (iconScaleG == 2.0) {
		choice[3].select("2.0");
	    } else if (iconScaleG == 5.0) {
		choice[3].select("5.0");
	    } else if (iconScaleG == 10.0) {
		choice[3].select("10.0");
	    } else if (iconScaleG == 20.0) {
		choice[3].select("20.0");
	    } else if (iconScaleG == 50.0) {
		choice[3].select("50.0");
	    } else if (iconScaleG == 100.0) {
		choice[3].select("100.0");
	    } else if (iconScaleG == 200.0) {
		choice[3].select("200.0");
	    } else if (iconScaleG == 500.0) {
		choice[3].select("500.0");
	    } else if (iconScaleG == 1000.0) {
		choice[3].select("1000.0");
	    } else {
		// User must have entered a custom value; only set it if
		// value is greater than 0.0
		if (iconScaleG > 0.0) {
		    iconScaleMenuLabel = Double.toString(iconScaleG);
		    choice[3].insert(iconScaleMenuLabel,13);
		    choice[3].select(13);
		} else {
		    choice[3].select("1.0");
		    iconScaleG = 1.0;
		}
	    }
	    
	    
	    //color by
	    gbc.insets = new Insets(5,0,5,2);
	    Utility.add(guiPanel,new Label("Color",Label.RIGHT),gbl,gbc,8,0,1,1);
	    choice[4]=new Choice();
	    if (showTactical) choice[4].add("by Type");
	    if (showTactical) choice[4].add("by Class");
	    choice[4].add("Red");
	    choice[4].add("Green");
	    choice[4].add("Blue");
	    choice[4].add("Yellow");
	    choice[4].add("Purple");
	    choice[4].add("Aqua");
	    choice[4].add("<Custom>");
	    gbc.insets = new Insets(5,0,5,5);
	    Utility.add(guiPanel,choice[4],gbl,gbc,9,0,1,1);
	    choice[4].addItemListener(this);
	    // Choose the right menu option based on user's selection in config file
	    if ( (showTactical) && (colorByG == TYPE) ) {
		choice[4].select("by Type");
	    } else if ( (colorByG == CONSTANT) && (colorFromConfigFile.equals("red")) ) {
		choice[4].select("Red");
		constantColorG = getHexColorStr("red");
	    } else if ( (colorByG == CONSTANT) && (colorFromConfigFile.equals("green")) ) {
		choice[4].select("Green");
		constantColorG = getHexColorStr("green");
	    } else if ( (colorByG == CONSTANT) && (colorFromConfigFile.equals("blue")) ) {
		choice[4].select("Blue");
		constantColorG = getHexColorStr("blue");
	    } else if ( (colorByG == CONSTANT) && (colorFromConfigFile.equals("yellow")) ) {
		choice[4].select("Yellow");
		constantColorG = getHexColorStr("yellow");
	    } else if ( (colorByG == CONSTANT) && (colorFromConfigFile.equals("purple")) ) {
		choice[4].select("Purple");
		constantColorG = getHexColorStr("purple");
	    } else if ( (colorByG == CONSTANT) && (colorFromConfigFile.equals("aqua")) ) {
		choice[4].select("Aqua");
		constantColorG = getHexColorStr("aqua");
	    } else if (colorByG == CONSTANT) {
		// See if the user has entered a valid color
		String tempConstantColorG = getHexColorStr(colorFromConfigFile);
		if (tempConstantColorG != null) {
		    constantColorG = tempConstantColorG;
		    // Remove the leading "ff" opacity specification for the
		    // color string before showing it on the menu
		    constantColorMenuLabel = constantColorG.substring(2);
		    int menuInt = 6;
		    if (showTactical) {
			menuInt = 8;
		    }
		    choice[4].insert(constantColorMenuLabel,menuInt);
		    choice[4].select(menuInt);
		} else {
		    // User did not enter a valid color in config file;
		    // use the default
		    if (showTactical) {
			colorByG = CLASSIFICATION;
			choice[4].select(1);
			System.err.println(
			    "User made an unrecognizable color selection: " +
			    colorFromConfigFile +
			    "; using color by classification");
		    } else {
			colorByG = CONSTANT;
			choice[4].select("Red");
			constantColorG = getHexColorStr("red");
			System.err.println(
			    "User made an unrecognizable color selection: " +
			    colorFromConfigFile +
			    "; using Red");
		    }
		}
	    } else {
		// The default
		if (showTactical) {
		    colorByG = CLASSIFICATION;
		    choice[4].select("by Class");
		} else {
		    colorByG = CONSTANT;
		    choice[4].select("Red");
		    constantColorG = getHexColorStr("red");
		}
	    }
	    
	    // 2008/02/25  WHF  Angular correction dialog.
	    java.awt.Button b = new java.awt.Button("Angle Corrections");
	    b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    if (angleCorrectionDlg == null)
			angleCorrectionDlg = new AngleCorrectionDlg();
		    angleCorrectionDlg.setVisible(true);
		}
	    } );
	    gbc.insets.left = 15;
	    Utility.add(guiPanel, b, gbl, gbc, 10, 0, 1, 1);
	    
	    //show curtain
	    checkbox=new Checkbox("Show Curtain",showCurtainG);
	    gbc.insets = new Insets(5,15,5,5);
	    Utility.add(guiPanel,checkbox,gbl,gbc,11,0,1,1);
	    checkbox.addItemListener(this);
	    
	    frame.add(guiPanel);
	    frame.pack();
	    
	    frame.addWindowListener(
		new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			exitAction();
		    }
		});
	    
	    frame.setVisible(true);
	}
	
    }
    
    /**************************************************************************
     * Read the configuration file for GUI settings.
     * <p>
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
     * 11/23/2005  JPW  Created.
     * 03/17/2006  EMF  Added fields for additional channels and display options.
     * 04/06/2006  EMF  Added -f command line option to specify config file name.
     * 04/10/2006  JPW  Change name from readChanNames() to readConfigFile()
     * 07/11/2006  JPW	Only read GUI settings (not chan names) from the file
     * 08/23/2006  JPW  Migrate 2 features from KMLTrackPlugIn:
     *			Add "label_chan" tag: a comma-delimited list of
     *			absolute channel names, and a label "nickname" for each
     *			channel.  For displaying data on the icon label.
     *			Add "track_name" tag to specify the name displayed with
     *			the track icon; only used when labelByID is true.
     * 11/28/2006  JPW  Change String comparison method from equals() to
     *                  equalsIgnoreCase(); add alt/latlon/altlatlon support
     *                  to Label field.
     * 11/30/2006  JPW	Read Type and Classification color mapping from
     *			config file.
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
		    "\n\nConfiguration file, \"" + configFile + "\", could not be found.\n");
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

	String temp = (String)hashtable.get("labelBy");
	if ( (temp != null) &&
	     (!temp.trim().equals("")) )
	{
	    if (temp.trim().equalsIgnoreCase("id")) labelByG=ID;
	    else if (temp.trim().equalsIgnoreCase("type")) labelByG=TYPE;
	    else if (temp.trim().equalsIgnoreCase("class")) labelByG=CLASSIFICATION;
	    else if (temp.trim().equalsIgnoreCase("alt")) labelByG=ALT;
	    else if (temp.trim().equalsIgnoreCase("latlon")) labelByG=LATLON;
	    else if (temp.trim().equalsIgnoreCase("altlatlon")) labelByG=ALTLATLON;
	    else if (temp.trim().equalsIgnoreCase("none")) labelByG=NONE;
	    else {
		labelByG=CONSTANT;
		constantLabelG=temp.trim();
	    }
	}
	
	temp = (String)hashtable.get("sortBy");
	if ( (temp != null) &&
	     (!temp.trim().equals("")) )
	{
	    if (temp.trim().equalsIgnoreCase("id")) sortByG=ID;
	    else if (temp.trim().equalsIgnoreCase("type")) sortByG=TYPE;
	    else if (temp.trim().equalsIgnoreCase("class")) sortByG=CLASSIFICATION;
	    else {
		sortByG=ID;
		System.err.println(
		    "Unknown sortBy field: " +
		    temp.trim() +
		    "; setting sortBy ID");
	    }
	}
	
	temp = (String)hashtable.get("iconBy");
	if ( (temp != null) &&
	     (!temp.trim().equals("")) )
	{
	    if (temp.trim().equalsIgnoreCase("type")) iconByG=TYPE;
	    else if (temp.trim().equalsIgnoreCase("class")) iconByG=CLASSIFICATION;
	    else if (temp.trim().equalsIgnoreCase("dot")) {
		iconByG=CONSTANT;
		iconNameG = "Red Dot";
	    }
	    else if (temp.trim().equalsIgnoreCase("square")) {
		iconByG=CONSTANT;
		iconNameG = "Red Square";
	    }
	    else if (temp.trim().equalsIgnoreCase("airplane")) {
		iconByG=CONSTANT;
		iconNameG = "Airplane";
	    }
	    else if (temp.trim().equalsIgnoreCase("3d_airplane")) {
		iconByG=CONSTANT;
		iconNameG = "3D Airplane";
	    }
	    else if (temp.trim().equalsIgnoreCase("none")) iconByG=NONE;
	    else {
		iconByG=TYPE;
		System.err.println(
		    "Unknown iconBy field: " +
		    temp.trim() +
		    "; setting iconBy TYPE");
	    }
	}
	
	// JPW 06/04/2007: Add icon scale
	temp = (String)hashtable.get("iconScale");
	if ( (temp != null) &&
	     (!temp.trim().equals("")) )
	{
	    // See if the value parses to a floating point num greater than 0.0
	    try {
		iconScaleG = Double.parseDouble(temp.trim());
		if (iconScaleG <= 0.0) {
		    throw new NumberFormatException("Invalid value");
		}
	    } catch (NumberFormatException nfe) {
		iconScaleG = 1.0;
		System.err.println(
		    "Invalid iconScale field: " +
		    temp.trim() +
		    "; setting iconScale to " +
		    iconScaleG);
	    }
	}
	
	temp = (String)hashtable.get("colorBy");
	if ( (temp != null) &&
	     (!temp.trim().equals("")) )
	{
	    if (temp.trim().equalsIgnoreCase("type")) colorByG=TYPE;
	    else if (temp.trim().equalsIgnoreCase("class")) colorByG=CLASSIFICATION;
	    else {
		colorByG=CONSTANT;
		colorFromConfigFile = temp.trim();
	    }
	}
	
	// JPW 11/30/2006: Type color mapping parameters
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_AIR_COLOR"))) != null ) {
	    TYPE_AIR_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_SPECIAL_POINT_COLOR"))) != null ) {
	    TYPE_SPECIAL_POINT_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_EMERGENCY_POINT_COLOR"))) != null ) {
	    TYPE_EMERGENCY_POINT_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_FORWARDED_COLOR"))) != null ) {
	    TYPE_FORWARDED_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_GROUND_COLOR"))) != null ) {
	    TYPE_GROUND_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_MUNITION_COLOR"))) != null ) {
	    TYPE_MUNITION_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_SPACE_COLOR"))) != null ) {
	    TYPE_SPACE_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_REFERENCE_POINT_COLOR"))) != null ) {
	    TYPE_REFERENCE_POINT_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_SURFACE_COLOR"))) != null ) {
	    TYPE_SURFACE_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_SUBSURFACE_COLOR"))) != null ) {
	    TYPE_SUBSURFACE_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_ELECTRONIC_WARFARE_COLOR"))) != null ) {
	    TYPE_ELECTRONIC_WARFARE_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_UNKNOWN_COLOR"))) != null ) {
	    TYPE_UNKNOWN_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("TYPE_DEFAULT_COLOR"))) != null ) {
	    TYPE_DEFAULT_COLOR = temp;
	}
	
	// JPW 11/30/2006: Classification color mapping parameters
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_ASSUMED_FRIEND_COLOR"))) != null ) {
	    CLASS_ASSUMED_FRIEND_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_FRIEND_COLOR"))) != null ) {
	    CLASS_FRIEND_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_HOSTILE_COLOR"))) != null ) {
	    CLASS_HOSTILE_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_NEUTRAL_COLOR"))) != null ) {
	    CLASS_NEUTRAL_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_PENDING_COLOR"))) != null ) {
	    CLASS_PENDING_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_SUSPECT_COLOR"))) != null ) {
	    CLASS_SUSPECT_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_UNKNOWN_COLOR"))) != null ) {
	    CLASS_UNKNOWN_COLOR = temp;
	}
	if ( (temp = getHexColorStr((String)hashtable.get("CLASS_DEFAULT_COLOR"))) != null ) {
	    CLASS_DEFAULT_COLOR = temp;
	}
	
	// "label_chan"
	temp = (String)hashtable.get("label_chan");
	if ( (temp != null) &&
	     (!temp.trim().equals("")) )
	{
	    // The format should be:
	    // <chan 1>=<nickname 1>,<chan 2>=<nickname 2>,etc.
	    String[] keyValuePairsArray = temp.split(",");
	    for (int i = 0; i < keyValuePairsArray.length; ++i) {
		String[] keyValuePair = keyValuePairsArray[i].split("=");
		if ( (keyValuePair == null)              ||
		     (keyValuePair.length != 2)          ||
		     (keyValuePair[0] == null)           ||
		     (keyValuePair[0].trim().equals("")) ||
		     (keyValuePair[1] == null)           ||
		     (keyValuePair[1].trim().equals("")) )
		{
		    // We don't have both a key and a value
		    continue;
		}
		String chanName = keyValuePair[0].trim();
		String chanNickname = keyValuePair[1].trim();
		if (labelChans == null) {
		    labelChans = new Hashtable();
		    labelChansV = new Vector();
		}
		String[] strArray = new String[2];
		strArray[0] = chanNickname;
		strArray[1] = "N/A";
		labelChans.put(chanName,strArray);
		labelChansV.add(chanName);
	    }
	}
	
    }
    
    /**************************************************************************
     * Return the predefined HEX number for the given color string.
     * <p>
     * If the given string is not one of the predefined colors, then
     * see if it is a valid HEX color code.
     * <p>
     * Return null on any error.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 12/01/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2006  JPW  Created.
     *
     */
    
    private String getHexColorStr(String colorStrI) {
	
	// FIREWALL
	if ( (colorStrI == null) || (colorStrI.trim().equals("")) ) {
	    return null;
	}
	
	String colorStr = colorStrI.trim();
	
	// See if the given string is one of our predefined colors
	String hexStr = (String)colors.get(colorStr);
	if ( (hexStr != null) && (hexStr.length() == 8) ) {
	    return hexStr;
	}
	
	// The given string wasn't one of our predefined colors; see if it is a
	// valid HEX color code.  If it is, then prepend it with "ff" to
	// indicate opaque presentation in Google Earth.
	// The 6-digit HEX color number will be interpreted by Google Earth as
	// bbggrr.
	if ( (colorStr.length() == 6)    &&
	     (colorStr.charAt(0) != '-') &&
	     (colorStr.charAt(0) != '+') )
	{
	    try {
		int val = Integer.parseInt(colorStr,16);
		// The input string represents a 6-digit HEX number!
		// Prepend with "ff" to indicate opaque presentation.
		return new String("ff" + colorStr);
	    } catch (Exception e) {
		// Nothing to do
	    }
	}
	
	System.err.println(
	    "Warning: could not interpret color \"" + colorStr + "\"");
	
	return null;
	
    }
    
    /**************************************************************************
     * Respond to user actions.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/23/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/23/2005  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
	String label = event.getActionCommand();
	
	if ( (event.getActionCommand() != null) &&
	     (event.getActionCommand().equals("Exit")) )
	{
	    exitAction();
	}
    }
    
    /**************************************************************************
     * Respond to user actions.
     * <p>
     *
     * @author Eric M. Friets
     *
     * @version 02/13/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/13/2006  EMF  Created.
     *
     */
    
    public void itemStateChanged(ItemEvent event) {
		
	Object source=event.getSource();
	
	//change label by?
	if (source == choice[0]) {
	    if (choice[0].getSelectedItem().equals("by ID")) labelByG = ID;
	    else if (choice[0].getSelectedItem().equals("by Type")) labelByG = TYPE;
	    else if (choice[0].getSelectedItem().equals("by Class")) labelByG = CLASSIFICATION;
	    else if (choice[0].getSelectedItem().equals("by Alt")) labelByG = ALT;
	    else if (choice[0].getSelectedItem().equals("by Lat,Lon")) labelByG = LATLON;
	    else if (choice[0].getSelectedItem().equals("by Alt,Lat,Lon")) labelByG = ALTLATLON;
	    else if (choice[0].getSelectedItem().equals(constantLabelG)) labelByG=CONSTANT;
	    else if (choice[0].getSelectedItem().equals("<Custom>")) {
		// JPW 11/28/2006: Don't set this labelling mode until after
		//                 user has specified a custom label
		// labelByG = CONSTANT;
		JOptionPane jop=new JOptionPane();
		//following line works in matlab, unclear why not here...
		//String temp=jop.showInputDialog(frame,"Enter the track label","Specify Label",JOptionPane.QUESTION_MESSAGE,null,null,constantLabelG);
		String temp=jop.showInputDialog(frame,"Enter the track label.",constantLabelG);
		if (temp==null) { //user hit cancel; return to previous state
		    choice[0].select(previousLabelByIndex);
		} else {
		    constantLabelG = temp;
		    labelByG = CONSTANT;
		    int place=choice[0].getSelectedIndex();
		    if (!choice[0].getItem(place-1).equals("None")) {
			place--;
			choice[0].remove(place);
		    }
		    choice[0].insert(constantLabelG,place);
		    choice[0].select(place);
		}
	    }
	    else if (choice[0].getSelectedItem().equals("None")) labelByG = NONE;
	    previousLabelByIndex = choice[0].getSelectedIndex();
	}
	//change sort by?
	else if (source == choice[1]) {
	    if (choice[1].getSelectedItem().equals("by ID")) sortByG = ID;
	    else if (choice[1].getSelectedItem().equals("by Type")) sortByG = TYPE;
	    else if (choice[1].getSelectedItem().equals("by Class")) sortByG = CLASSIFICATION;
	}
	//change icon by?
	else if (source == choice[2]) {
	    if (choice[2].getSelectedItem().equals("by Type")) iconByG = TYPE;
	    else if (choice[2].getSelectedItem().equals("by Class")) iconByG = CLASSIFICATION;
	    else if (choice[2].getSelectedItem().equals("Red Dot")) {
		iconByG = CONSTANT;
		iconNameG = "Red Dot";
	    } else if (choice[2].getSelectedItem().equals("Red Square")) {
		iconByG = CONSTANT;
		iconNameG = "Red Square";
	    } else if (choice[2].getSelectedItem().equals("Airplane")) {
		iconByG = CONSTANT;
		iconNameG = "Airplane";
	    } else if (choice[2].getSelectedItem().equals("3D Airplane")) {
		iconByG = CONSTANT;
		iconNameG = "3D Airplane";
	    } else if (choice[2].getSelectedItem().equals("None")) iconByG = NONE;
	}
	// change iconScale?
	else if (source == choice[3]) {
	    if (choice[3].getSelectedItem().equals("0.1")) {
		iconScaleG = 0.1;
	    } else if (choice[3].getSelectedItem().equals("0.2")) {
		iconScaleG = 0.2;
	    } else if (choice[3].getSelectedItem().equals("0.5")) {
		iconScaleG = 0.5;
	    } else if (choice[3].getSelectedItem().equals("1.0")) {
		iconScaleG = 1.0;
	    } else if (choice[3].getSelectedItem().equals("2.0")) {
		iconScaleG = 2.0;
	    } else if (choice[3].getSelectedItem().equals("5.0")) {
		iconScaleG = 5.0;
	    } else if (choice[3].getSelectedItem().equals("10.0")) {
		iconScaleG = 10.0;
	    } else if (choice[3].getSelectedItem().equals("20.0")) {
		iconScaleG = 20.0;
	    } else if (choice[3].getSelectedItem().equals("50.0")) {
		iconScaleG = 50.0;
	    } else if (choice[3].getSelectedItem().equals("100.0")) {
		iconScaleG = 100.0;
	    } else if (choice[3].getSelectedItem().equals("200.0")) {
		iconScaleG = 200.0;
	    } else if (choice[3].getSelectedItem().equals("500.0")) {
		iconScaleG = 500.0;
	    } else if (choice[3].getSelectedItem().equals("1000.0")) {
		iconScaleG = 1000.0;
	    }  else if (choice[3].getSelectedItem().equals(iconScaleMenuLabel)) {
		try {
		    iconScaleG = Double.parseDouble(iconScaleMenuLabel);
		    if (iconScaleG <= 0.0) {
			throw new NumberFormatException("Invalid value");
		    }
		} catch (NumberFormatException nfe) {
		    choice[3].select(previousScaleByIndex);
		    System.err.println("Invalid icon scale menu label: unexpected error");
		}
	    } else if (choice[3].getSelectedItem().equals("<Custom>")) {
		JOptionPane jop=new JOptionPane();
		String temp =
		    jop.showInputDialog(
			frame,
			"Enter the icon scale (floating point value greater than 0.0).",
			iconScaleMenuLabel);
		if (temp==null) { //user hit cancel; return to previous state
		    choice[3].select(previousScaleByIndex);
		} else {
		    // See if the user has entered a valid scale
		    try {
			double tempIconScaleG = Double.parseDouble(temp);
			if (tempIconScaleG <= 0.0) {
			    throw new NumberFormatException("Invalid value");
			}
			iconScaleG = tempIconScaleG;
			iconScaleMenuLabel = Double.toString(iconScaleG);
			int place=choice[3].getSelectedIndex();
			if (!choice[3].getItem(place-1).equals("1000.0")) {
			    // There is an existing custom scale menu item;
			    // remove it before adding the new one
			    place--;
			    choice[3].remove(place);
			}
			choice[3].insert(iconScaleMenuLabel,place);
			choice[3].select(place);
		    } catch (NumberFormatException nfe) {
			// pop error dialog, return to previous state
			choice[3].select(previousScaleByIndex);
			jop.showMessageDialog(
			    frame,
			    "Scale must be a floating point value greater than 0.0.",
			    "Invalid scale specified",
			    JOptionPane.ERROR_MESSAGE);
		    }
		}
	    }
	    previousScaleByIndex = choice[3].getSelectedIndex();
	}
	//change color by?
	else if (source == choice[4]) {
	    if (choice[4].getSelectedItem().equals("by Type")) colorByG = TYPE;
	    else if (choice[4].getSelectedItem().equals("by Class")) colorByG = CLASSIFICATION;
	    else if (choice[4].getSelectedItem().equals("Red")) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr("red");
	    } else if (choice[4].getSelectedItem().equals("Green")) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr("green");
	    } else if (choice[4].getSelectedItem().equals("Blue")) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr("blue");
	    } else if (choice[4].getSelectedItem().equals("Yellow")) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr("yellow");
	    } else if (choice[4].getSelectedItem().equals("Purple")) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr("purple");
	    } else if (choice[4].getSelectedItem().equals("Aqua")) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr("aqua");
	    } else if (choice[4].getSelectedItem().equals(constantColorMenuLabel)) {
		colorByG = CONSTANT;
		constantColorG = getHexColorStr(constantColorMenuLabel);
	    } else if (choice[4].getSelectedItem().equals("<Custom>")) {
		JOptionPane jop=new JOptionPane();
		String temp =
		    jop.showInputDialog(
			frame,
			"Enter the color in hex as bbggrr.",
			constantColorMenuLabel);
		if (temp==null) { //user hit cancel; return to previous state
		    choice[4].select(previousColorByIndex);
		} else {
		    // See if the user has entered a valid color
		    String tempConstantColorG = getHexColorStr(temp);
		    if (tempConstantColorG != null) {
			constantColorG = tempConstantColorG;
			// Remove the leading "ff" opacity specification for
			// the color string before showing it on the menu
			constantColorMenuLabel = constantColorG.substring(2);
			int place=choice[4].getSelectedIndex();
			if (!choice[4].getItem(place-1).equals("Aqua")) {
			    // There is an existing custom color menu item;
			    // remove it before adding the new one
			    place--;
			    choice[4].remove(place);
			}
			choice[4].insert(constantColorMenuLabel,place);
			choice[4].select(place);
			colorByG = CONSTANT;
		    } else {
			// pop error dialog, return to previous state
			choice[4].select(previousColorByIndex);
			jop.showMessageDialog(
			    frame,
			    "Color must be 6 hex digits.",
			    "Invalid color specified",
			    JOptionPane.ERROR_MESSAGE);
		    }
		}
	    }
	    previousColorByIndex = choice[4].getSelectedIndex();
	}
	
	//change show curtain?
	else if (source == checkbox) {
	  showCurtainG=checkbox.getState();
	}
	
	else {
	    System.err.println("Unknown event source...");
	    System.err.println(source);
	}
    }
    
    /**************************************************************************
     * Exit the program.
     * <p>
     * Signal the execution loop to exit by setting bExitPlugIn to true.
     *
     * @author John P. Wilson
     *
     * @version 11/23/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/23/2005  JPW  Created.
     *
     */
    
    public void exitAction() {
	
	boolean bChangeCursor = false;
	if (frame.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
	    frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    bChangeCursor = true;
	}
	
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
	
	if (bChangeCursor) {
	    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	RBNBProcess.exit(0);
	
    }
    
    /**************************************************************************
     * Main PlugIn execution loop.
     * <p>
     * Create Sink and PlugIn connections and handle PlugIn registration and
     * data requests.
     *
     * @author John P. Wilson
     *
     * @version 11/27/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/27/2006  EMF	Add munge-settable display parameters using the
     *			following key/value options:
     *			Key		Value options
     *			--------------------------------------------------------------------------
     *			KmlLabel	ID Alt LatLon AltLatLon Type Class None <literal string>
     *			KmlSort		ID Type Class
     *			KmlIcon		Type Class Dot Square Airplane None
     *			KmlColor	Type Class Red Green Blue Yellow Purple Aqua <literal string bbggrr>
     *			KmlCurtain	true false
     * 11/20/2005  JPW  Created.
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
	if ( (showGUI) && (frame != null) ) {
	    frame.setTitle("TrackKML  \"" + pluginName + "\"");
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
	    
	    if (picm.GetRequestReference().equals("registration")) {
		picm.PutTime( (System.currentTimeMillis()/1000.0), 0.0);
		if ( (picm.GetName(0).equals("...")) ||
		     (picm.GetName(0).equals("*")) )
		{
		    // Just return the same picm
		    // Nothing to do
		}
		else
		{
		    String result=
		    	"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
			+"<!DOCTYPE rbnb>\n"
			+"<rbnb>\n"
			+"\t\t<size>"+1+"</size>\n"
			// For sending back KML
			// +"\t\t<mime>text/plain</mime>\n"
			// For sending back KMZ
			+"\t\t<mime>application/vnd.google-earth.kmz</mime>\n"
			+"</rbnb>\n";
		    picm.PutDataAsString(0,result);
		    picm.PutMime(0,"text/xml");
		}
		plugin.Flush(picm);
		System.err.println(
		    (new Date()).toString() +
		    "  Responded to registration request.");
		continue;
	    }
	    
	    String[] chanList = picm.GetChannelList();
	    String requestChanStr = chanList[0];
	    // Peel off final slash, if there is one
	    if (requestChanStr.endsWith("/")) {
		requestChanStr =
		    requestChanStr.substring(0, requestChanStr.length() - 1);
	    }
	    System.err.println(
		(new Date()).toString() +
		"  Source: " +
		requestChanStr);
	    
	    // Extra information may have been passed in the channel data;
	    // only consider the first String in the array
	    requestData = null;
	    pseudoAltScale = 1.0;
	    pseudoAltOffset = 0.0;
	    bUsePseudoAltOffset = false;
	    
	    //initialize this request's variables from the gui settings
	    labelBy=labelByG;
	    constantLabel=constantLabelG;
	    iconBy=iconByG;
	    iconScale=iconScaleG;
	    iconName=iconNameG;
	    colorBy=colorByG;
	    constantColor=constantColorG;
	    showCurtain=showCurtainG;
	    sortBy=sortByG;
	    
	    if (picm.GetType(0) == ChannelMap.TYPE_STRING) {
		String[] strArray = picm.GetDataAsString(0);
		if ( (strArray != null)    &&
		     (strArray.length > 0) &&
		     (strArray[0] != null)  &&
		     (!strArray[0].trim().equals("")) )
		{
		    requestData = strArray[0].trim();
		    if (bPrintDebug) {
			System.err.println(
			    "Options string: \"" + requestData + "\"");
		    }
		    char[] terminatorChars = {'&'};
		    KeyValueHash kvh =
		    	new KeyValueHash(requestData,terminatorChars);
		    String pseudoAltScaleStr  = kvh.get("KmlScale");
		    if (pseudoAltScaleStr != null) {
			try {
			    pseudoAltScale =
				Double.parseDouble(pseudoAltScaleStr);
			    if (bPrintDebug) {
				System.err.println(
				    "Pseudo-alt scale = " + pseudoAltScale);
			    }
			} catch (NumberFormatException nfe) {
			    // Nothing to do; must not have been a valid scale
			}
		    }
		    String pseudoAltOffsetStr = kvh.get("KmlOffset");
		    if (pseudoAltOffsetStr != null) {
			try {
			    pseudoAltOffset =
				Double.parseDouble(pseudoAltOffsetStr);
			    if (bPrintDebug) {
				System.err.println(
				    "Pseudo-alt offset = " + pseudoAltOffset);
			    }
			    // Since this value is present, we will use it
			    // (instead of altitude) to provide the offset
			    bUsePseudoAltOffset = true;
			} catch (NumberFormatException nfe) {
			    // Nothing to do; must not have been a valid offset
			}
		    }
		    String kmlLabel = kvh.get("KmlLabel");
		    if (kmlLabel!=null) {
			if (kmlLabel.equalsIgnoreCase("ID")) labelBy=ID;
			else if (kmlLabel.equalsIgnoreCase("Type")) labelBy=TYPE;
			else if (kmlLabel.equalsIgnoreCase("Class")) labelBy=CLASSIFICATION;
			else if (kmlLabel.equalsIgnoreCase("Alt")) labelBy=ALT;
			else if (kmlLabel.equalsIgnoreCase("LatLon")) labelBy=LATLON;
			else if (kmlLabel.equalsIgnoreCase("AltLatLon")) labelBy=ALTLATLON;
			else if (kmlLabel.equalsIgnoreCase("None")) labelBy=NONE;
			else if (kmlLabel.trim().length()>0) {
			    labelBy=CONSTANT;
			    constantLabel=kmlLabel.trim();
			}
		    }
		    String kmlSort = kvh.get("KmlSort");
		    if (kmlSort!=null) {
			if (kmlSort.equalsIgnoreCase("ID")) sortBy=ID;
			else if (kmlSort.equalsIgnoreCase("Type")) sortBy=TYPE;
			else if (kmlSort.equalsIgnoreCase("Class")) sortBy=CLASSIFICATION;
		    }
		    String kmlIcon = kvh.get("KmlIcon");
		    if (kmlIcon!=null) {
			if (kmlIcon.equalsIgnoreCase("Type")) iconBy=TYPE;
			else if (kmlIcon.equalsIgnoreCase("Class")) iconBy=CLASSIFICATION;
			else if (kmlIcon.equalsIgnoreCase("Dot")) {
			    iconBy=CONSTANT;
			    iconName="Red Dot";
			} else if (kmlIcon.equalsIgnoreCase("Square")) {
			    iconBy=CONSTANT;
			    iconName="Red Square";
			} else if (kmlIcon.equalsIgnoreCase("Airplane")) {
			    iconBy=CONSTANT;
			    iconName="Airplane";
			} else if (kmlIcon.equalsIgnoreCase("3D_Airplane")) {
			    iconBy=CONSTANT;
			    iconName="3D Airplane";
			} else if (kmlIcon.equalsIgnoreCase("None")) iconBy=NONE;
		    }
		    // JPW 06/04/2007: Add icon scale
		    String kmlIconScale = kvh.get("KmlIconScale");
		    if (kmlIconScale!=null) {
			// See if this string parses to be a floating point
			// value greater than zero
			try {
			    double tempIconScale = Double.parseDouble(kmlIconScale);
			    if (tempIconScale <= 0.0) {
				throw new NumberFormatException("Invalid value");
			    }
			    iconScale = tempIconScale;
			} catch (NumberFormatException nfe) {
			    // Not a valid value; just ignore it
			}
		    }
		    String kmlColor = kvh.get("KmlColor");
		    if (kmlColor!=null) {
			if (kmlColor.equalsIgnoreCase("Type")) colorBy=TYPE;
			else if (kmlColor.equalsIgnoreCase("Class")) colorBy=CLASSIFICATION;
			else if (kmlColor.equalsIgnoreCase("Red")) {
			    colorBy = CONSTANT;
			    constantColor = getHexColorStr("red");
			} else if (kmlColor.equalsIgnoreCase("Green")) {
			    colorBy = CONSTANT;
			    constantColor = getHexColorStr("green");
			} else if (kmlColor.equalsIgnoreCase("Blue")) {
			    colorBy = CONSTANT;
			    constantColor = getHexColorStr("blue");
			} else if (kmlColor.equalsIgnoreCase("Yellow")) {
			    colorBy = CONSTANT;
			    constantColor = getHexColorStr("yellow");
			} else if (kmlColor.equalsIgnoreCase("Purple")) {
			    colorBy = CONSTANT;
			    constantColor = getHexColorStr("purple");
			} else if (kmlColor.equalsIgnoreCase("Aqua")) {
			    colorBy = CONSTANT;
			    constantColor = getHexColorStr("aqua");
			} else {
			    // See if the user has specified a valid color
			    // First, decode the string (useful, for example,
			    // for converting "dark%20green" to "dark green")
			    try {
				kmlColor = URLDecoder.decode(kmlColor,"UTF-8");
			    } catch (UnsupportedEncodingException uee) {
				// Nothing to do
			    }
			    String tempConstantColor=getHexColorStr(kmlColor);
			    if (tempConstantColor != null) {
				colorBy = CONSTANT;
				constantColor = tempConstantColor;
			    }
			}
		    }
		    String kmlCurtain = kvh.get("KmlCurtain");
		    if (kmlCurtain!=null) {
			if (kmlCurtain.equalsIgnoreCase("true")) showCurtain=true;
			else if (kmlCurtain.equalsIgnoreCase("false")) showCurtain=false;
		    }
		}
	    }
	    
	    // Pass the request to the TrackDataPlugIn
	    // NOTE: The left-most part of requestChanStr *must* be
	    //       the name of TrackDataPlugIn.
	    ChannelMap cm=new ChannelMap();
	    int requestIdx = cm.Add(requestChanStr);
	    // If request data came in with the original request (such as a
	    // Google Earth bounding box) then add it as channel data
	    if (requestData != null) {
		cm.PutDataAsString(requestIdx,requestData);
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
	    
	    // A String containing the KML document to send back to requestor
	    String kmlStr = "";
	    iconsUsed=new Vector();
	    // clear tracks vector of track names
	    tracks.clear();
	    
	    if ( (dataMap == null) || (dataMap.NumberOfChannels() == 0) ) {
		System.err.println("\tNo data at requested time.");
		kmlStr = constructStubResponse("No tracks found", true);
	    } else {
		String[] chans = dataMap.GetChannelList();
		for (int i = 0; i < chans.length; ++i) {
		    String chan = chans[i];
		    // If this channel name ends in altChanName
		    // then this might be part of a track
		    if (chan.endsWith(altChanName)) {
			// See if this folder contains all the needed tactical
			// data channels; if so, this is a track
			String trackName =
			    chan.substring(
				0, chan.length() - altChanName.length() - 1);
			if ((dataMap.GetIndex(new String(trackName + "/" + latChanName)) != -1) &&
			    (dataMap.GetIndex(new String(trackName + "/" + lonChanName)) != -1))
			{
			    if (bPrintDebug) {
				System.err.println("Found a track: " + trackName);
			    }
			    if (!tracks.contains(trackName)) {
				tracks.add(trackName);
			    }
			}
		    }
		}
		if (tracks.isEmpty()) {
		    System.err.println("\tNo data at requested time.");
		    kmlStr = constructStubResponse("No tracks found", true);
		} else {
		    if (!bConsolidatedResponse) {
			// Create KML containing a NetworkLinks for each track
			kmlStr =
			    writeNetworkLinksKML(
				picm.GetRequestStart(),
				picm.GetRequestDuration(),
				picm.GetRequestReference());
		    } else {
			// Sort tracks as specified by the user
			String suffix=null;
			switch (sortBy) {
			    case ID:
				suffix=new String("/"+idChanName);
				break;
			    case TYPE:
				suffix=new String("/"+typeChanName);
				break;
			    case CLASSIFICATION:
				suffix=new String("/"+classificationChanName);
				break;
			}
			sortmap = new Hashtable();
			for (Enumeration e = tracks.elements(); e.hasMoreElements();) {
			    String trackname=(String)e.nextElement();
			    int idx = dataMap.GetIndex(trackname+suffix);
			    if (idx == -1) {
				// The appropriate sort channel for this track
				// doesn't exist; group all of the "unsorted"
				// tracks together
				if (sortmap.containsKey("tracks")) {
				    ((Vector)sortmap.get("tracks")).add(trackname);
				} else {
				    Vector vec = new Vector();
				    vec.add(trackname);
				    sortmap.put("tracks",vec);
				}
			    } else {
				String[] sortdat = dataMap.GetDataAsString(idx);
				if (sortdat!=null && sortdat.length>0) {
				    if (sortmap.containsKey(sortdat[0])) {
					((Vector)sortmap.get(sortdat[0])).add(trackname);
				    } else {
					Vector vec=new Vector();
					vec.add(trackname);
					sortmap.put(sortdat[0],vec);
				    }
				}
			    }
			}
			/*
			// Print debug info on sortmap
			System.err.println("sortmap:");
			for (Enumeration e=sortmap.keys();e.hasMoreElements();) {
			    String key=(String)e.nextElement();
			    System.err.println("  key "+key);
			    Vector vec=(Vector)sortmap.get(key);
			    for (Enumeration ev=vec.elements();ev.hasMoreElements();) {
				System.err.println("    "+(String)ev.nextElement());
			    }
			}
			*/
			//
			// Create KML containing all the data for each track
			//
			kmlStr =
			    writeTracksKML(
				dataMap,
				picm.GetRequestStart(),
				picm.GetRequestDuration(),
				picm.GetRequestReference());
		    }
		}
	    }
	    
	    // JPW 02/27/2006: Timestamp the data we send back
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
	    
	    if (iconsUsed.size()==0) {
		// Send off the KML document
		if (bPrintDebug) {
		    System.err.println("Respond to request: send back KML");
		}
		picm.PutDataAsString(0,kmlStr);
	    } else { 
		// Send off as KMZ with icons attached
		if (bPrintDebug) {
		    System.err.println("Respond to request: send back KMZ");
		}
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
		    ZipOutputStream zos = new ZipOutputStream(baos);
		    //add icons
		    for (Enumeration e=iconsUsed.elements();e.hasMoreElements();) {
			String icon = (String)e.nextElement();
			ZipEntry ze = new ZipEntry(icon);
			zos.setMethod(ZipOutputStream.DEFLATED);
			zos.setLevel(Deflater.DEFAULT_COMPRESSION);
			zos.putNextEntry(ze);
			//does this work for URLs instead of just local files?
			InputStream is = ClassLoader.getSystemResourceAsStream(icon);
			int bread;
			byte[] bin = new byte[4096]; //too small?
			while ( (bread=is.read(bin)) != -1) zos.write(bin,0,bread);
			zos.closeEntry();
		    }
		    //add KML document
		    ZipEntry ze = new ZipEntry("doc.kml");
		    zos.setMethod(ZipOutputStream.DEFLATED);
		    zos.setLevel(Deflater.DEFAULT_COMPRESSION);
		    zos.putNextEntry(ze);
		    byte[] kmlBytes = kmlStr.getBytes();
		    zos.write(kmlBytes,0,kmlBytes.length);
		    zos.close();
		} catch (Exception ex) {
		    System.err.println(
		        "Exception generating KMZ: " + ex.getMessage());
		    ex.printStackTrace();
		}
		picm.PutDataAsByteArray(0,baos.toByteArray());
		picm.PutMime(0,"application/vnd.google-earth.kmz");
	    }
	    plugin.Flush(picm);
	    long testTimeEnd = System.currentTimeMillis();
	    if (bPrintDebug) {
		System.err.println(
		    "Processing time = " +
		    (testTimeEnd - testTimeStart)/1000.0 +
		    " sec");
	    }
	    // Cleanup
	    kmlStr = null;
	    lat = null;
	    lon = null;
	    alt = null;
	    
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
     * Write KML which contains all the data for each discovered track.
     * <p>
     *
     * @param cmI		   ChannelMap containing track data.
     * @param startI		   Request start time
     * @param durationI		   Request duration
     * @param referenceI	   Request reference (newest, oldest, absolute)
     *
     * @author John P. Wilson
     *
     * @version 07/12/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/12/2006  JPW  Created.
     *
     */
    
    private String writeTracksKML(
	ChannelMap cmI,
	double startI,
	double durationI,
	String referenceI)
    {
	
	String kmlStr =
	    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	    "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" +
	    "<Document id=\"Track Manager\">\n" +
	    "<name>Track Manager</name>\n" +
	    "<visibility>1</visibility>\n" +
	    "<open>1</open>\n";
	StringBuffer kmlSB = new StringBuffer(kmlStr);
	if (sortmap.size() == 0) {
	    kmlSB.append(
	        constructStubResponse("No tracks at requested time", false));
	}
	
	// If the channel map contains exactly 7 channels, then we assume that
	// these channels are: Alt, Lat, Lon, Type, Class, ID, and a
	// Pseudo-Alt channel.
	// NOTE: We only do pseudo-alt if there is one and only one track!
	int pseudoAltChanIdx = -1;
	if (cmI.NumberOfChannels() == 7) {
	    // Determine the pseudo-alt channel
	    String[] chans = cmI.GetChannelList();
	    for (int i = 0; i < chans.length; ++i) {
		String chan = chans[i];
		if (!chan.endsWith("/" + altChanName)  &&
		    !chan.endsWith("/" + latChanName)  &&
		    !chan.endsWith("/" + lonChanName)  &&
		    !chan.endsWith("/" + idChanName)   &&
		    !chan.endsWith("/" + typeChanName) &&
		    !chan.endsWith("/" + classificationChanName) )
		{
		    // This is our pseudo-alt channel
		    if (bPrintDebug) {
			System.err.println("Found pseudo-alt channel: " + chan);
		    }
		    pseudoAltChanIdx = i;
		    break;
		}
	    }
	}
	
	for (Enumeration e = sortmap.keys();e.hasMoreElements();) {
	    String folder = (String)e.nextElement();
	    if ((sortBy!=ID)&&(!folder.equals("tracks"))) {
		kmlSB.append("<Folder>\n<name>"+folder+"</name>\n");
	    }
	    Vector vec=(Vector)sortmap.get(folder);
	    for (Enumeration ev=vec.elements();ev.hasMoreElements();) {
		kmlSB.append(
		    writeSingleTrackKML(
			cmI,
			(String)ev.nextElement(),
			startI,
			durationI,
			referenceI,
			pseudoAltChanIdx));
	    }
	    if ((sortBy!=ID)&&(!folder.equals("tracks"))) {
		kmlSB.append("</Folder>\n");
	    }
	}
	
	kmlSB.append("</Document>\n</kml>");
	kmlStr = kmlSB.toString();
	kmlSB = null;
	
	return kmlStr;
    }
    
    /**************************************************************************
     * Write KML which contains a seperate NetworkLink object for each track
     * that has been discovered.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param startI		Request start time
     * @param durationI		Request duration
     * @param referenceI	Request reference (newest, oldest, absolute)
     *
     * @version 12/13/2005
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/13/2005  JPW  Created.
     *
     */
    
    private String writeNetworkLinksKML(
	double startI,
	double durationI,
	String referenceI)
    {
	String kmlStr =
	    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	    "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" +
	    "<Document id=\"Track Manager\">\n" +
	    "<name>Track Manager</name>\n" +
	    "<visibility>1</visibility>\n" +
	    "<open>1</open>\n";
	StringBuffer kmlSB = new StringBuffer(kmlStr);
	for (Enumeration e = tracks.elements(); e.hasMoreElements();) {
	    String nextTrack = (String)e.nextElement();
	    String trackName = nextTrack;
	    int slashIndex = trackName.lastIndexOf('/');
	    if (slashIndex != -1) {
		trackName = nextTrack.substring(slashIndex + 1);
	    }
	    kmlSB.append(
		"<NetworkLink>\n" +
		"<name>" + trackName + "</name>\n" +
		"<visibility>1</visibility>\n" +
		"<open>0</open>\n" +
		"<Url>\n" +
		"<href>http://" +
		webdavAddress);
	    if (bTomcat4) {
		// Format for Tomcat 4 Servlet
		kmlSB.append("/rbnbNet/" + rbnbServerName + "/");
	    } else {
		// Format for Tomcat 5 Servlet
		kmlSB.append("/RBNB/");
	    }
	    // If PICM indicated the default munge (start time = 0,
	    // duration = 0, and reference = newest) the don't specifically
	    // add a munge to this request
	    if ( (startI == 0.0) &&
		 (durationI == 0.0) &&
		 (referenceI.equals("newest")) )
	    {
		kmlSB.append(
		    pluginName +
		    "/" +
		    nextTrack);
	    }
	    else
	    {
		kmlSB.append(
		    pluginName +
		    "/" +
		    nextTrack +
		    "@t=" +
		    startI +
		    "%26d=" +
		    durationI +
		    "%26r=" +
		    referenceI);
	    }
	    kmlSB.append("</href>\n");
	    // refreshInterval == 0 means no refreshes
	    if (refreshInterval == 0.0) {
		kmlSB.append("<refreshMode>once</refreshMode>\n");
	    } else {
		kmlSB.append(
		    "<refreshMode>onInterval</refreshMode>\n" +
		    "<refreshInterval>" +
		    refreshInterval +
		    "</refreshInterval>\n");
	    }
	    kmlSB.append(
		"</Url>\n" +
		"</NetworkLink>\n");
	}
	
	kmlSB.append("</Document>\n</kml>");
	kmlStr = kmlSB.toString();
	kmlSB = null;
	
	return kmlStr;
    }
    
    /**************************************************************************
     * Write KML code for a track to the given StringBuffer
     * <p>
     * NOTE: startI, durationI, and referenceI are only used if requesting
     *       current channel data for the track label.
     *
     * @author John P. Wilson
     *
     * @param cmI		   ChannelMap containing all track data
     * @param remoteSourceI	   Name of Source or sub-Folder for this track
     * @param startI		   Request start time
     * @param durationI		   Request duration
     * @param referenceI	   Request reference (newest, oldest, absolute)
     * @param pseudoAltChanIdxI    Index of the pseudo-alt channel (or -1 if there is none)
     *
     * @version 06/04/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2007  JPW  Support display of a single 3D icon at head of track.
     *			Scale 3D as well as non-3D icons using "iconScale".
     * 12/19/2006  JPW  For pseudo-alt, no longer use a hardwired heading=0
     *			or hardwired icon=red dot.
     * 10/11/2006  JPW	Add support for pseudo-alt - display a track for
     *			pAlt, and if using a constant offset then also include
     *			a track at constant elevation to show the offset.
     *			Remove bWriteFullDocumentI - it was always false.
     * 02/09/2006  EMF  Added label, icon, color switches.
     * 12/12/2005  JPW  Created.
     *
     */
    
    private String writeSingleTrackKML(
        ChannelMap cmI,
        String remoteSourceI,
	double startI,
	double durationI,
	String referenceI,
	int pseudoAltChanIdxI)
    {
	
	// Parse the track name from the name of the remote source
	String trackName = remoteSourceI;
	int slashIndex = trackName.lastIndexOf('/');
	if (slashIndex != -1) {
	    trackName = remoteSourceI.substring(slashIndex + 1);
	}
	
	String kmlStr = null;
	
	// JPW 08/23/2006: Get current data for the Placemark label
	if (labelChans != null) {
	    
	    // Remove TrackPlugIn name from the front of remoteSourceI
	    String remoteSourceNoTPI = remoteSourceI;
	    int firstSlashIndex = remoteSourceNoTPI.indexOf('/');
	    if (firstSlashIndex != -1) {
		remoteSourceNoTPI =
		    remoteSourceI.substring(firstSlashIndex + 1);
	    }
	    
	    // Reset the data in the Hashtable
	    for (Enumeration e=labelChans.elements(); e.hasMoreElements(); ) {
		String[] strArray = (String[])e.nextElement();
		strArray[1] = "N/A";
	    }
	    
	    try {
	    
	    ChannelMap labelCM = new ChannelMap();
	    if (bPrintDebug) {
		System.err.println("Fetch current values for the label:");
	    }
	    for (Enumeration e=labelChans.keys(); e.hasMoreElements(); ) {
		String key = (String)e.nextElement();
		String chanName = remoteSourceNoTPI + "/" + key;
		if (bPrintDebug) {
		    System.err.println("\t" + chanName);
		}
		labelCM.Add(chanName);
		// Reset the data for this key
		String[] strArray = (String[])labelChans.get(key);
		strArray[1] = "N/A";
	    }
	    double starttime = startI + durationI;
	    if (referenceI.equals("newest")) {
		// JPW 05/31/2007: Only subtract durationI from startI if durationI is negative
		//                 Remember: "newest" is a backward looking request.  We want the
		//                 "current" datapoint here - the most recent data point over the
		//                 requested duration.  Therefore, if durationI is positive (as it
		//                 will usually be) then the most recent point will be at startI.
		// starttime = startI - durationI;
		if (durationI >= 0.0) {
		    starttime = startI;
		} else {
		    starttime = startI - durationI;
		}
		if (starttime < 0.0) {
		    starttime = 0.0;
		}
	    }
	    try {
		sink.Request(
		    labelCM,
		    starttime,
		    0,
		    referenceI);
		labelCM = sink.Fetch(timeout);
	    } catch (Exception e) {
		e.printStackTrace();
		labelCM=new ChannelMap();
	    }
	    if (bPrintDebug) {
		System.err.println("Fetched the following label data:");
	    }
	    if ( (labelCM != null) && (labelCM.NumberOfChannels() > 0) ) {
		for (int i = 0; i < labelCM.NumberOfChannels(); ++i) {
		    String name = labelCM.GetName(i);
		    if (bPrintDebug) {
			System.err.println("Channel: " + name);
		    }
		    slashIndex = name.lastIndexOf('/');
		    String key = name.substring(slashIndex + 1);
		    String[] strArray = (String[])labelChans.get(key);
		    if (labelCM.GetType(i) == ChannelMap.TYPE_FLOAT32) {
			float dataPt = labelCM.GetDataAsFloat32(i)[0];
			String formatStr = "%.2f";
			if ( (dataPt >= 180) && (dataPt < 1000) ) {
			    formatStr = "%.1f";
			} else if (dataPt >= 1000) {
			    formatStr = "%.0f";
			}
			strArray[1] = ToString.toString(formatStr,dataPt);
		    } else if (labelCM.GetType(i) == ChannelMap.TYPE_FLOAT64) {
			double dataPt = labelCM.GetDataAsFloat64(i)[0];
			String formatStr = "%.2f";
			if ( (dataPt >= 180) && (dataPt < 1000) ) {
			    formatStr = "%.1f";
			} else if (dataPt >= 1000) {
			    formatStr = "%.0f";
			}
			strArray[1] = ToString.toString(formatStr,dataPt);
		    } else if (labelCM.GetType(i) == ChannelMap.TYPE_INT16) {
			strArray[1] =
			    Short.toString(labelCM.GetDataAsInt16(i)[0]);
		    } else if (labelCM.GetType(i) == ChannelMap.TYPE_INT32) {
			strArray[1] =
			    Integer.toString(labelCM.GetDataAsInt32(i)[0]);
		    } else if (labelCM.GetType(i) == ChannelMap.TYPE_INT64) {
			strArray[1] =
			    Long.toString(labelCM.GetDataAsInt64(i)[0]);
		    } else if (labelCM.GetType(i) == ChannelMap.TYPE_INT8) {
			strArray[1] =
			    Byte.toString(labelCM.GetDataAsInt8(i)[0]);
		    } else if (labelCM.GetType(i) == ChannelMap.TYPE_STRING) {
			strArray[1] = labelCM.GetDataAsString(i)[0];
		    }
		    if (bPrintDebug) {
			System.err.println("Data: " + strArray[1]);
		    }
		}
	    }
	    
	    } catch (Exception e) {
		System.err.println(
		    "Caught exception trying to fetch label data:\n" + e);
	    }
	    
	}
	
	if (!verifyChannelMap(cmI, remoteSourceI, pseudoAltChanIdxI)) {
	    // send back a bare bones KML
	    return constructStubResponse(trackName, false);
	}
	
	/////////////////////////////////////////////////
	// Send back KML containing the track coordinates
	/////////////////////////////////////////////////
	
	String lastLonStr = null;
	String lastLatStr = null;
	String lastAltStr = null;
	int numPts = alt.length;
	// Using java.text.DecimalFormat instead of com.rbnb.utility.ToString
	// may speed things up
	try {
	    lastLonStr = ToString.toString("%.6f",lon[numPts - 1]);
	    lastLatStr = ToString.toString("%.6f",lat[numPts - 1]);
	    lastAltStr = ToString.toString("%.6f",alt[numPts - 1]);
	} catch (Exception e) {
	    lastLonStr = Double.toString(lon[numPts - 1]);
	    lastLatStr = Double.toString(lat[numPts - 1]);
	    lastAltStr = Float.toString(alt[numPts - 1]);
	}
	
	StringBuffer kmlSB = new StringBuffer(1000);
	
	//EMF 2/9/06: get label, icon, color
	String trackLabel = getLabel(
		"Alt",
		alt[numPts - 1],
		lat[numPts - 1],
		lon[numPts - 1],
		false
	);
	String[] icon = getIcon();
	String color = getColor();
	String styleID = "trackStyle" + trackName + color;
	
	// JPW 06/04/2007: Placeholders for heading, pitch, and roll values
	String currentHeadingStr = "0";
	String currentPitchStr = "0";
	String currentRollStr = "0";
	/*try {
	    currentHeadingStr = ToString.toString("%.6f",heading);
	} catch (Exception e) {
	    currentHeadingStr = Double.toString(heading);
	} */
	
	// 2008/02/25  WHF  Use heading data if available, otherwise calc.
	double head;
	if (heading == null) {
	    try {
		head = calculateHeading();
	    } catch (Exception e) {
		head = 0.0;
	    }
	} else head = headingSwitchSign * heading[numPts-1] + headingBias;
	
	currentHeadingStr = Double.toString(head);

	// 2008/02/22  WHF  Pitch/roll support
	if (pitch != null) currentPitchStr = Double.toString(
	    	pitchSwitchSign * pitch[numPts-1] + pitchBias);
	if (roll != null) currentRollStr = Double.toString(
	    	rollSwitchSign * roll[numPts-1] + rollBias);
	
	///////////////////////////////////////////
	// The main alt/lat/lon placemark and track
	///////////////////////////////////////////
	
	// JPW 10/18/2006: Only display the main track if there is no
	//                 pAlt to display.
	if (pAlt == null) {
	    
	    kmlSB.append(
		"<Placemark id=\"" + trackName + "\">\n" +
		
		"<Style id=\"" + styleID + "\">\n" +
		"<LineStyle>\n" +
		"<color>" + color + "</color>\n" +
		"<width>2</width>\n" +
		"</LineStyle>\n" +
		// The following specifies the curtain color, used when extruding
		"<PolyStyle>\n" +
		"<color>7f00007f</color>\n" +
		"</PolyStyle>\n" +
		"<IconStyle>\n" +
		"<scale>" + Double.toString(iconScale) + "</scale>\n" +
		"<heading>" + currentHeadingStr + "</heading>\n" +
		"<Icon>\n");
	    
	    // JPW 06/05/2007: Only specify Icon details if user has not
	    //                 selected a .dae model
	    if ( (icon[0] != null) && (!icon[0].endsWith(".dae")) ) {
		kmlSB.append(
		    "<href>" + icon[0] + "</href>\n" +
		    "<x>" + icon[1] + "</x>\n" +
		    "<y>" + icon[2] + "</y>\n" +
		    "<w>32</w>\n" +
		    "<h>32</h>\n");
	    }
	    
	    kmlSB.append(
		"</Icon>\n" +
		"</IconStyle>\n" +
		"</Style>\n");
	    
	    // JPW 08/23/2006: There may be data to display with the name
	    // NOTE: code copied below in the pseudoAlt section...change must
	    //       be done in both places
	    if ( (labelChans == null) &&
		 (!trackLabel.equals("")) )
	    {
		kmlSB.append("<name>" + trackLabel + "</name>\n");
	    } else if (labelChans != null) {
		if (!trackLabel.equals("")) {
		    kmlSB.append("<name>" + trackLabel + " ");
		} else {
		    kmlSB.append("<name>");
		}
		// Vector labelChansV has the key names in the correct
		// user-desired order
		for (Enumeration e=labelChansV.elements(); e.hasMoreElements(); ) {
		    String key = (String)e.nextElement();
		    // Get the String array associated with this key
		    String[] strArray = (String[])labelChans.get(key);
		    kmlSB.append(strArray[0] + "=" + strArray[1]);
		    if (e.hasMoreElements()) {
			kmlSB.append(",");
		    }
		}
		kmlSB.append("</name>\n");
	    }
	    
	    kmlSB.append(
	    	"<Snippet maxLines=\"0\">\n" +
		"<![CDATA[&nbsp;]]>\n" +
		"</Snippet>\n" +
		"<description><![CDATA[Current track position<p>" +
		"Latitude = " +
		lastLatStr +
		"<br>Longitude = " +
		lastLonStr +
		"<br>Altitude = " +
		lastAltStr +
		// Display a screen capture from RBNBPlot
		// "<p><center><Img src=\"http://analysis.creare.com/rbnbNet/Analysis/PlotJPG/image.jpg@i=" +
		// System.currentTimeMillis() +
		// "\"></center>" +
		"<p>For more information, go to:\n" +
		"http://rbnb.creare.com" +
		// Can't display the png image; display jpg instead
		// "<center><Img src=\"http://analysis.creare.com/rbnbNet/Analysis/PNGPlugIn/NOAADEMO/LN-100G Altitude@d=1000\"></center>]]></description>\n" +
		// NOTE: Add URL "i=X" munge so the most recent picture is always returned (a cached copy isn't displayed instead)
		// "<center><Img src=\"http://rbnb.creare.com:8080/rbnbNet/Creare/sdp/ThumbNail/Video/WRBV.jpg@i=" +
		// System.currentTimeMillis() +
		// "\"></center>" +
		"]]>\n" +
		"</description>\n" +
		"<LookAt>\n" +
		"<longitude>" +
		lastLonStr +
		"</longitude>\n" +
		"<latitude>" +
		lastLatStr +
		"</latitude>\n" +
		/*
		 *
		 * JPW 03/07/2006
		 * According to the KML schemas that I can find
		 * (http://kml.tjworld.net/kml.xsd, http://kml.tjworld.net/kml.rng,
		 * http://kml.tjworld.net/kml.dtd) it appears that lat, lon, range,
		 * and heading are all required; only tilt is optional.  What we
		 * would like is for the user to be able to specify heading, tilt,
		 * and range, and we will specify lat and lon.  When I leave off
		 * range, tilt, and heading, then Google Earth keeps the user's
		 * range setting, but sets tilt and heading both to 0.  For now,
		 * MJM has asked that we don't specify range, tilt, or heading.
		 *
		"<range>" +
		(viewpointElevationOffset + alt[numPts - 1]) +
		"</range>\n" +
		"<tilt>0</tilt>\n" +
		"<heading>0</heading>\n" +
		*
		*/
		"</LookAt>\n" +
		"<visibility>1</visibility>\n" +
		"<MultiGeometry>\n" +
		"<styleUrl>#" + styleID + "</styleUrl>\n" +
		"<Point>\n");
	    
	    if (showCurtain) kmlSB.append("<extrude>1</extrude>\n");
	    
	    kmlSB.append(
	    	"<altitudeMode>absolute</altitudeMode>\n" +
		"<coordinates>\n" +
		// Write out the most current position
		lastLonStr +
		"," +
		lastLatStr +
		"," +
		lastAltStr +
		"\n" +
		"</coordinates>\n" +
		"</Point>\n");
	    
	    if ( (icon[0] != null) && (icon[0].endsWith(".dae")) ) {
		// User has specified to use a 3D model for the track icon
		kmlSB.append(
		    "<Model>\n" +
		    "<altitudeMode>absolute</altitudeMode>\n" +
		    "<Location>\n" +
		    "<longitude>" + lastLonStr + "</longitude>\n" +
		    "<latitude>" + lastLatStr + "</latitude>\n" +
		    "<altitude>" + lastAltStr + "</altitude>\n" +
		    "</Location>\n" +
		    "<Orientation>\n" +
		    "<heading>" + currentHeadingStr + "</heading>\n" +
		    "<tilt>" + currentPitchStr + "</tilt>\n" +
		    "<roll>" + currentRollStr + "</roll>\n" +
		    "</Orientation>\n" +
		    "<Scale>\n" +
		    "<x>" + Double.toString(iconScale) + "</x>\n" +
		    "<y>" + Double.toString(iconScale) + "</y>\n" +
		    "<z>" + Double.toString(iconScale) + "</z>\n" +
		    "</Scale>\n" +
		    "<Link>\n" +
		    "<href>" + icon[0] + "</href>\n" +
		    "</Link>\n" +
		    "</Model>\n");
	    }
	    
	    kmlSB.append(
		"<LineString id=\"Track\">\n");
	    
	    if (showCurtain) kmlSB.append("<extrude>1</extrude>\n");
	    
	    kmlSB.append(
		"<tessellate>0</tessellate>\n" +
		"<altitudeMode>absolute</altitudeMode>\n" +
		"<coordinates>\n");
	    
	    // Using java.text.DecimalFormat instead of com.rbnb.utility.ToString
	    // may speed things up
	    for (int i = 0; i < numPts; ++i) {
		try {
		    kmlSB.append(ToString.toString("%.6f",lon[i]));
		} catch (Exception e) {
		    System.err.println(
			"Caught exception trying to write out longitude point: " +
			"lon = " +
			lon[i] +
			"\n" +
			e);
		    kmlSB.append("0.0");
		}
		kmlSB.append(",");
		try {
		    kmlSB.append(ToString.toString("%.6f",lat[i]));
		} catch (Exception e) {
		    System.err.println(
			"Caught exception trying to write out latitude point: " +
			"lon = " +
			lon[i] +
			"\n" +
			e);
		    kmlSB.append("0.0");
		}
		kmlSB.append(",");
		try {
		    kmlSB.append(ToString.toString("%.1f",alt[i]));
		} catch (Exception e) {
		    System.err.println(
			"Caught exception trying to write out altitude point: " +
			"lon = " +
			lon[i] +
			"\n" +
			e);
		    kmlSB.append("0.0");
		}
		kmlSB.append("\n");
	    }
	    
	    kmlSB.append(
		"</coordinates>\n" +
		"</LineString>\n" +
		"</MultiGeometry>\n" +
		"</Placemark>\n");
	    
	    // Since pAlt is null (there is no pseudo-alt to display) we are done
	    kmlStr = kmlSB.toString();
	    kmlSB = null;
	    return kmlStr;
	    
	}
	
	////////////////////////////////////////////////////////////
	// NOTE: If we got this far, then we know there is pAlt data
	////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////
	// Save the last unscaled pAlt value for the Placemark icon label
	/////////////////////////////////////////////////////////////////
	float lastPAltDataPt = pAlt[numPts - 1];
	
	//////////////////////////////////////////////////////////////////
	// Calculate the scaled pAlt values.
	// NOTE: Pseudo-alt, like altitude, needs to be an array of floats
	//////////////////////////////////////////////////////////////////
	if (bUsePseudoAltOffset) {
	    if (bPrintDebug) {
		System.err.println("Calculate pseudo-alt using offset");
	    }
	    for (int i = 0; i < pAlt.length; ++i) {
		pAlt[i] =
		    (float)pseudoAltOffset +
		    ( (float)pseudoAltScale * pAlt[i] );
	    }
	} else {
	    if (bPrintDebug) {
		System.err.println("Calculate pseudo-alt using alt data as offset");
	    }
	    for (int i = 0; i < pAlt.length; ++i) {
		pAlt[i] =
		    alt[i] +
		    ( (float)pseudoAltScale * pAlt[i] );
	    }
	}
	
	//////////////////////////////////////////////////////////////
	// Save the last scaled pAlt value for displaying the position
	// of the Placemark icon
	//////////////////////////////////////////////////////////////
	String lastScaledPaltStr = null;
	// Using java.text.DecimalFormat instead of com.rbnb.utility.ToString
	// may speed things up
	try {
	    lastScaledPaltStr = ToString.toString("%.6f",pAlt[numPts - 1]);
	} catch (Exception e) {
	    lastScaledPaltStr = Float.toString(pAlt[numPts - 1]);
	}
	
	/////////////////////////////////
	// Pseudo-alt placemark and track
	/////////////////////////////////
	String pseudoAltTrackName = cmI.GetName(pseudoAltChanIdxI);
	slashIndex = pseudoAltTrackName.lastIndexOf('/');
	if (slashIndex != -1) {
	    pseudoAltTrackName = pseudoAltTrackName.substring(slashIndex + 1);
	}
	// Must send getLabel() the *unscaled* pAlt value for display
	trackLabel =
	    getLabel(
		pseudoAltTrackName,
		lastPAltDataPt,
		lat[numPts - 1],
		lon[numPts - 1],
		true);
	
	kmlSB.append(
	    "<Placemark id=\"pseudoalt\">\n" +
	    
	    "<Style id=\"pseudoaltstyle\">\n" +
	    "<LineStyle>\n" +
	    "<color>" + color + "</color>\n" +
	    "<width>2</width>\n" +
	    "</LineStyle>\n" +
	    // The following specifies the curtain color, used when extruding
	    "<PolyStyle>\n" +
	    "<color>7f00007f</color>\n" +
	    "</PolyStyle>\n" +
	    "<IconStyle>\n" +
	    "<scale>" + Double.toString(iconScale) + "</scale>\n" +
	    "<heading>" + heading + "</heading>\n" +
	    "<Icon>\n");
	
	// JPW 06/05/2007: Only specify Icon details if user has not
	//                 selected a .dae model
	if ( (icon[0] != null) && (!icon[0].endsWith(".dae")) ) {
	    kmlSB.append(
		"<href>" + icon[0] + "</href>\n" +
		"<x>" + icon[1] + "</x>\n" +
		"<y>" + icon[2] + "</y>\n" +
		"<w>32</w>\n" +
		"<h>32</h>\n");
	}
	
	kmlSB.append(
	    "</Icon>\n" +
	    "</IconStyle>\n" +
	    "</Style>\n");
	
	// JPW 08/23/2006: There may be data to display with the name
	// NOTE: code copied above in the alt section...change must
	//       be done in both places
	if ( (labelChans == null) &&
	     (!trackLabel.equals("")) )
	{
	    kmlSB.append("<name>" + trackLabel + "</name>\n");
	} else if (labelChans != null) {
	    if (!trackLabel.equals("")) {
		kmlSB.append("<name>" + trackLabel + " ");
	    } else {
		kmlSB.append("<name>");
	    }
	    // Vector labelChansV has the key names in the correct
	    // user-desired order
	    for (Enumeration e=labelChansV.elements(); e.hasMoreElements(); ) {
		String key = (String)e.nextElement();
		// Get the String array associated with this key
		String[] strArray = (String[])labelChans.get(key);
		kmlSB.append(strArray[0] + "=" + strArray[1]);
		if (e.hasMoreElements()) {
		    kmlSB.append(",");
		}
	    }
	    kmlSB.append("</name>\n");
	}
	
	kmlSB.append(
	    "<LookAt>\n" +
	    "<longitude>" +
	    lastLonStr +
	    "</longitude>\n" +
	    "<latitude>" +
	    lastLatStr +
	    "</latitude>\n" +
	    "</LookAt>\n" +
	    "<visibility>1</visibility>\n" +
	    "<MultiGeometry>\n" +
	    "<styleUrl>#pseudoaltstyle</styleUrl>\n" +
	    "<Point>\n");
	
	if (showCurtain) kmlSB.append("<extrude>1</extrude>\n");
	
	kmlSB.append(
	    "<altitudeMode>absolute</altitudeMode>\n" +
	    "<coordinates>\n" +
	    // Write out the most current position
	    lastLonStr +
	    "," +
	    lastLatStr +
	    "," +
	    lastScaledPaltStr +
	    "\n" +
	    "</coordinates>\n" +
	    "</Point>\n");
	
	if ( (icon[0] != null) && (icon[0].endsWith(".dae")) ) {
	    // User has specified to use a 3D model for the track icon
	    kmlSB.append(
		"<Model>\n" +
		"<altitudeMode>absolute</altitudeMode>\n" +
		"<Location>\n" +
		"<longitude>" + lastLonStr + "</longitude>\n" +
		"<latitude>" + lastLatStr + "</latitude>\n" +
		"<altitude>" + lastScaledPaltStr + "</altitude>\n" +
		"</Location>\n" +
		"<Orientation>\n" +
		"<heading>" + currentHeadingStr + "</heading>\n" +
		"<tilt>" + currentPitchStr + "</tilt>\n" +
		"<roll>" + currentRollStr + "</roll>\n" +
		"</Orientation>\n" +
		"<Scale>\n" +
		"<x>" + Double.toString(iconScale) + "</x>\n" +
		"<y>" + Double.toString(iconScale) + "</y>\n" +
		"<z>" + Double.toString(iconScale) + "</z>\n" +
		"</Scale>\n" +
		"<Link>\n" +
		"<href>" + icon[0] + "</href>\n" +
		"</Link>\n" +
		"</Model>\n");
	}
	
	kmlSB.append(
	    "<LineString id=\"pseudoaltlinestring\">\n");
	
	if (showCurtain) kmlSB.append("<extrude>1</extrude>\n");
	
	kmlSB.append(
	    "<tessellate>0</tessellate>\n" +
	    "<altitudeMode>absolute</altitudeMode>\n" +
	    "<coordinates>\n");
	
	// Using java.text.DecimalFormat instead of com.rbnb.utility.ToString
	// may speed things up
	for (int i = 0; i < numPts; ++i) {
	    try {
		kmlSB.append(ToString.toString("%.6f",lon[i]));
	    } catch (Exception e) {
		System.err.println(
		    "Caught exception trying to write out longitude point: " +
		    "lon = " +
		    lon[i] +
		    "\n" +
		    e);
		kmlSB.append("0.0");
	    }
	    kmlSB.append(",");
	    try {
		kmlSB.append(ToString.toString("%.6f",lat[i]));
	    } catch (Exception e) {
		System.err.println(
		    "Caught exception trying to write out latitude point: " +
		    "lon = " +
		    lon[i] +
		    "\n" +
		    e);
		kmlSB.append("0.0");
	    }
	    kmlSB.append(",");
	    try {
	        kmlSB.append(ToString.toString("%.1f",pAlt[i]));
	    } catch (Exception e) {
		System.err.println(
		    "Caught exception trying to write out pseudo-alt point: " +
		    "lon = " +
		    lon[i] +
		    "\n" +
		    e);
		kmlSB.append("0.0");
	    }
	    kmlSB.append("\n");
	}
	
	kmlSB.append(
	    "</coordinates>\n" +
	    "</LineString>\n" +
	    "</MultiGeometry>\n" +
	    "</Placemark>\n");
	
	kmlStr = kmlSB.toString();
	kmlSB = null;
	return kmlStr;
	
    }
    
    /**************************************************************************
     * No data available, send back stub response.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 02/01/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/01/2006  JPW  Created.
     *
     */
    
    private String constructStubResponse(
	String trackNameI, boolean bWriteFullDocumentI)
    {
	
	String kmlStr =
	    "<Placemark>\n" +
	    "<name>" + trackNameI + " (no data)</name>\n" +
	    // JPW 03/07/2006: Take out the hardwired LookAt
	    //                 This was a view of NASA Dryden
	    // "<LookAt>\n" +
	    // "<longitude>-117.6708712297157</longitude>\n" +
	    // "<latitude>34.57887256926223</latitude>\n" +
	    // "<range>23130.0</range>\n" +
	    // "<tilt>67.0</tilt>\n" +
	    // "<heading>0.0</heading>\n" +
	    // "</LookAt>\n" +
	    "<visibility>1</visibility>\n" +
	    "</Placemark>\n";
	if (bWriteFullDocumentI) {
	    kmlStr =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" +
		"<Document>\n" +
		"<name>Track data</name>\n" +
		"<open>1</open>\n" +
		"<visibility>1</visibility>\n" +
		// JPW 03/07/2006: Take out the hardwired LookAt
		//                 This was a view of NASA Dryden
		// "<LookAt>\n" +
		// "<longitude>-117.6708712297157</longitude>\n" +
		// "<latitude>34.57887256926223</latitude>\n" +
		// "<range>23130.0</range>\n" +
		// "<tilt>67.0</tilt>\n" +
		// "<heading>0.0</heading>\n" +
		// "</LookAt>\n" +
		
		kmlStr +
		
		"</Document>\n" +
		"</kml>";
	}
	
	return kmlStr;
	
    }
    
    /**************************************************************************
     * Verify the Alt, Lat, Lon, trackID, Type, and Classification data
     * chans contained in the given ChannelMap for the particular remoteSourceI
     * of interest.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param cmI		   ChannelMap containing all track data
     * @param remoteSourceI	   Name of Source or sub-Folder for this track
     * @param pseudoAltChanIdxI    Index of the pseudo-alt channel (or -1 if there is none)
     *
     * @version 10/11/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/22/2008  WHF  Added heading/pitch/roll.
     * 10/11/2006  JPW  Add support for pseudo-alt channel
     * 07/12/2006  JPW  Created.
     */
    
    private boolean verifyChannelMap(
	ChannelMap cmI,
	String remoteSourceI,
	int pseudoAltChanIdxI)
    {
	
	// JPW 06/04/2007: Add time array
	time = null;
	alt = heading = pitch = roll = null;
	pAlt = null;
	lat = null;
	lon = null;
	trackID = null;
	type = null;
	classification = null;
	
	if ( (cmI == null) || (cmI.NumberOfChannels() == 0) ) {
	    System.err.println("No channel data:");
	    System.err.println("cmI: "+cmI);
	    return false;
	}
	
	/////////////////////////////////////
	// Get data from the given ChannelMap
	/////////////////////////////////////
	for (int i = 0; i < cmI.NumberOfChannels(); ++i) {
	    // JPW 10/11/2006: See if this is the pseudo-alt channel
	    if (i == pseudoAltChanIdxI) {
		// pAlt should be an array of floats
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT32) {
		    pAlt = cmI.GetDataAsFloat32(i);
		} else {
		    System.err.println("Wrong type for pseudo-alt channel");
		    pAlt = null;
		    // Should I return false or just keep going and ignore the
		    // pAlt data?  Returning false indicates a catastrophic
		    // error and a stub KML will be returned.
		    return false;
		}
		continue;
	    }
	    // Handle the regular track channels (alt, lat, lon, etc.)
	    String chan = cmI.GetName(i);
	    if (!chan.startsWith(remoteSourceI)) {
		continue;
	    }
	    chan = chan.substring(remoteSourceI.length() + 1);
	    if (chan.equals(latChanName)) {
		// lat should be an array of doubles
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT64) {
		    lat = cmI.GetDataAsFloat64(i);
		} else {
		    System.err.println("Wrong type for Lat channel");
		    lat = null;
		    return false;
		}
	    } else if (chan.equals(lonChanName)) {
		// lon should be an array of doubles
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT64) {
		    lon = cmI.GetDataAsFloat64(i);
		} else {
		    System.err.println("Wrong type for Lon channel");
		    lon = null;
		    return false;
		}
	    } else if (chan.equals(altChanName)) {
		// alt should be an array of floats
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT32) {
		    alt = cmI.GetDataAsFloat32(i);
		} else {
		    System.err.println("Wrong type for Alt channel");
		    alt = null;
		    return false;
		}
		// JPW 06/04/2007: Get time array from alt channel
		time = cmI.GetTimes(i);
	    } else if (chan.equals(idChanName)) {
		// id should be string data; we only store one value
		if (cmI.GetType(i) == ChannelMap.TYPE_STRING) {
		    String[] temp = cmI.GetDataAsString(i);
		    trackID = temp[temp.length-1];
		} else {
		    System.err.println("Wrong type for TrackID channel");
		    trackID = null;
		    return false;
		}
	    } else if (chan.equals(typeChanName)) {
		// type should be string data; we only store one value
		if (cmI.GetType(i) == ChannelMap.TYPE_STRING) {
		    String[] temp = cmI.GetDataAsString(i);
		    type = temp[temp.length-1];
		} else {
		    System.err.println("Wrong type for Type channel");
		    type = null;
		    return false;
		}
	    } else if (chan.equals(classificationChanName)) {
		// classification should be string data; we only store one value
		if (cmI.GetType(i) == ChannelMap.TYPE_STRING) {
		    String[] temp = cmI.GetDataAsString(i);
		    classification = temp[temp.length-1];
		} else {
		    System.err.println("Wrong type for Classification channel");
		    classification = null;
		    return false;
		}
	    } else if (chan.equals(headingChanName)) {
		// pitch should be an array of floats
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT32) {
		    heading = cmI.GetDataAsFloat32(i);
		} else {
		    System.err.println("Wrong type for Heading channel");
		    return false;
		}
	    } else if (chan.equals(pitchChanName)) {
		// pitch should be an array of floats
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT32) {
		    pitch = cmI.GetDataAsFloat32(i);
		} else {
		    System.err.println("Wrong type for Pitch channel");
		    return false;
		}
	    } else if (chan.equals(rollChanName)) {
		// roll should be an array of floats
		if (cmI.GetType(i) == ChannelMap.TYPE_FLOAT32) {
		    roll = cmI.GetDataAsFloat32(i);
		} else {
		    System.err.println("Wrong type for Roll channel");
		    return false;
		}
	    } else {
		System.err.println("Unknown channel " + chan);
	    }
	}
	
	////////////////////////////////////
	// Check that we got all needed data
	////////////////////////////////////
	if (alt == null) {
	    System.err.println("Missing alt data channel");
	    return false;
	}
	if (lat == null) {
	    System.err.println("Missing lat data channel");
	    return false;
	}
	if (lon == null) {
	    System.err.println("Missing lon data channel");
	    return false;
	}
	if (trackID == null) {
	    System.err.println("Missing trackID data channel");
	    // Parse the track name from the name of the remote source
	    trackID = remoteSourceI;
	    int slashIndex = remoteSourceI.lastIndexOf('/');
	    if (slashIndex != -1) {
		trackID = remoteSourceI.substring(slashIndex + 1);
	    }
	    System.err.println("TrackID set to "+trackID);
	}
	if (type == null) {
	    System.err.println("Missing type data channel");
	    System.err.println("type set to Unknown");
	    type = "Unknown";
	}
	if (classification == null) {
	    System.err.println("Missing classification data channel");
	    System.err.println("classification set to Unknown");
	    classification = "Unknown";
	}
	
	// Check that all coordinate arrays are the same size
	int numPts = alt.length;
	if ( (numPts != lat.length) ||
	     (numPts != lon.length) ||
	     ( (pAlt != null) && (numPts != pAlt.length) ) )
	{
	    System.err.println("ERROR: coordinate arrays not equal size.");
	    return false;
	}
	
	return true;
	
    }
    
    /**************************************************************************
     * Calculate track heading (between 0 and 360 degrees). Ideally we
     * want to calculate heading using the current point and two points
     * back from the current point.
     * <p>
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
     * 03/22/2006  JPW  Previously I used the current and next to current
     *			point to calculate heading.  Now, look back farther
     *			to calculate heading.  We made this change because
     *			frequently the NASA data has repeated points - which
     *			will result in heading = 0.0 and the plane icon
     *			jerking around in Google Earth
     * 02/03/2006  JPW  Created.
     *
     */
    
    private double calculateHeading() throws Exception {
	
	// Check that latitude and longitude arrays are the same size
	if ( (lat == null) ||
	     (lon == null) ||
	     (lat.length != lon.length) ||
	     (lat.length == 0) )
	{
	    throw new Exception(
	    "ERROR: problem with lat/lon arrays; can't calculate heading.");
	}
	
	double deltaLon = 0.0;
	double deltaLat = 0.0;
	
	if (lat.length == 1)
	{
	    // Only a single data point, can't calculate heading
	    return (double)0.0;
	}
	else if (!validDataPoint(lat[lat.length-1],lon[lon.length-1]))
	{
	    // The most recent point is not valid; can't calculate heading
	    return (double)0.0;
	}
	else if (lat.length == 2)
	{
	    // We are forced to use the 2 most recent points to calc heading
	    if (!validDataPoint(lat[0],lon[0]))
	    {
		return (double)0.0;
	    }
	    else
	    {
		deltaLon = lon[1] - lon[0];
		deltaLat = lat[1] - lat[0];
	    }
	}
	else
	{
	    
	    // First preference is to use the current point and two points
	    // back.  If the point two back isn't valid, try the
	    // current point and one back.  If that isn't valid, just
	    // return 0.0
	    
	    if (validDataPoint(lat[lat.length-3],lon[lon.length-3])) {
		deltaLon = lon[lon.length-1] - lon[lon.length-3];
		deltaLat = lat[lat.length-1] - lat[lat.length-3];
	    }
	    else if (validDataPoint(lat[lat.length-2],lon[lon.length-2])) {
		deltaLon = lon[lon.length-1] - lon[lon.length-2];
		deltaLat = lat[lat.length-1] - lat[lat.length-2];
	    } else {
		return 0.0;
	    }
	    
	}
	
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
	
	return 0.0;
	
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
     * Return appropriate display label.
     * <p>
     * Display label is 0 to 3 attributes, separated by the / character:
     * ID/Type/Classification
     * Which attributes to use are set by booleans which are set by the GUI.
     *
     * @author Eric M. Friets
     *
     * @version 11/28/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/28/2006  JPW  Added bPAltI option, indicating whether this will be
     *			displaying a pseudo-alt value
     * 11/27/2006  EMF  Added alt, lat, lon options.
     * 02/09/2006  EMF  Created.
     *
     */    
    private String getLabel(String altname, float lalt, double llat, double llon, boolean bPAltI) {
	String label = null;
	switch (labelBy) {
	    case ID:
	    	label=trackID;
		break;
		
	    case TYPE:
	    	label=type;
		break;
		
	    case CLASSIFICATION:
	    	label=classification;
		break;
		
	    case CONSTANT:
	    	label=constantLabel;
		break;
		
	    case ALT:
	    	try {
		    String precStr = "%.0f";
		    if (bPAltI) {
			// We are displaying the value for a pseudo-alt;
			// use more precision
			precStr = "%.2f";
		    }
		    label=altname+"="+ToString.toString(precStr,lalt);
		} catch (Exception e) {
		    label=altname+"="+Float.toString(lalt);
		}
		break;
		
	    case LATLON:
	    	try {
		    label="Lat="+ToString.toString("%.2f",llat)+
			",Lon="+ToString.toString("%.2f",llon);
		} catch (Exception e) {
		    label="Lat="+Double.toString(llat)+",Lon="+Double.toString(llon);
		}
		break;
		
	    case ALTLATLON:
	    	try {
		    label=altname+"="+ToString.toString("%.0f",lalt)+
			",Lat="+ToString.toString("%.2f",llat)+
			",Lon="+ToString.toString("%.2f",llon);
		} catch (Exception e) {
		    label=altname+"="+Float.toString(lalt)+
		    	",Lat="+Double.toString(llat)+
			",Lon="+Double.toString(llon);
		}
		break;
		
	    case NONE:
	    default:
	    	label="";
		break;
	}
	
	return label;
    }
    
    /**************************************************************************
     * Return appropriate color for track.
     * <p>
     * Color is chosen by ID, Type, Classification, or Constant and includes
     * the "ff" prefix which denotes opaque presentation.
     * Which attribute controls color is set by int constants, defined above,
     * which are selected by the GUI.
     *
     * @author Eric M. Friets
     *
     * @version 12/01/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/01/2006  JPW  Add check before returning the color to make sure it
     *			isn't null.
     *			Don't prepend "ff" to constantColor here; this has
     *			already been done by the time we reach this method.
     * 02/09/2006  EMF  Created.
     *
     */
    private String getColor() {
	
	String color = new String();
	
	switch (colorBy) {
	    case ID:
	    	color = getHexColorStr("red");
	    	break;
		
	    case TYPE: //default values from JADE Types window (except Unknown and no match)
	        // JPW 11/30/2006: Use color mapping
	    	if (type.equals("Air")) color = TYPE_AIR_COLOR;
		else if (type.equals("Special Point")) color = TYPE_SPECIAL_POINT_COLOR;
		else if (type.equals("Emergency Point")) color = TYPE_EMERGENCY_POINT_COLOR;
		else if (type.equals("Forwarded")) color = TYPE_FORWARDED_COLOR;
		else if (type.equals("Ground")) color = TYPE_GROUND_COLOR;
		else if (type.equals("Munition")) color = TYPE_MUNITION_COLOR;
		else if (type.equals("Space")) color = TYPE_SPACE_COLOR;
		else if (type.equals("Reference Point")) color = TYPE_REFERENCE_POINT_COLOR;
		else if (type.equals("Surface")) color = TYPE_SURFACE_COLOR;
		else if (type.equals("Sub-surface")) color = TYPE_SUBSURFACE_COLOR;
		else if (type.equals("Electronic Warfare")) color = TYPE_ELECTRONIC_WARFARE_COLOR;
		else if (type.equals("Unknown")) color = TYPE_UNKNOWN_COLOR;
		else color = TYPE_DEFAULT_COLOR;
		break;
		
	    case CLASSIFICATION: //default values from JADE Classifications window (except no match)
	    	if (classification.equals("Assumed Friend")) color = CLASS_ASSUMED_FRIEND_COLOR;
	    	else if (classification.equals("Friend")) color = CLASS_FRIEND_COLOR;
	    	else if (classification.equals("Hostile")) color = CLASS_HOSTILE_COLOR;
	    	else if (classification.equals("Neutral")) color = CLASS_NEUTRAL_COLOR;
		else if (classification.equals("Pending")) color = CLASS_PENDING_COLOR;
	    	else if (classification.equals("Suspect")) color = CLASS_SUSPECT_COLOR;
	    	else if (classification.equals("Unknown")) color = CLASS_UNKNOWN_COLOR;
		else color = CLASS_DEFAULT_COLOR;
		break;
		
	    case CONSTANT:
	        // JPW 12/01/2006: Don't prepend "ff" to constantColor here;
		//                 this has already been done by the time we
		//                 reach this method.
	    	// color = "ff"+constantColor;
		color = constantColor;
		break;
	    	
	    default:
	    	color = getHexColorStr("red");
	}
	
	// JPW 12/01/2006: Add check if color is null
	if ( (color == null)           ||
	     (color.trim().equals("")) ||
	     (color.length() != 8) )
	{
	    System.err.println(
		"WARNING: color not found; using default color (red)");
	    // Set default color: red
	    color = getHexColorStr("red");
	}
	
	return color;
    }
    
    /**************************************************************************
     * Return appropriate icon for track.
     * <p>
     * Icon is chosen by Type, or by Classification, and includes the
     * Google Earth pallet, x offset, and y offset as strings.
     * Which icon is returned is controlled by the iconBy variable, which
     * is set to a constant by the GUI.
     *
     * @author Eric M. Friets
     *
     * @version 02/09/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2006  EMF  Created.
     *
     */
    private String[] getIcon() {
	
	// Content of the icon array:
	// icon[0] = pallete
	// icon[1] = x-offset
	// icon[2] = y-offset
	String[] icon = new String[3];
	
	switch (iconBy) {
	    case TYPE:
		if (type.equals("Air")) {
		    icon[0]="root://icons/palette-2.png"; //airplane
		    icon[1]="0";
		    icon[2]="0";
		} else if (type.equals("Space")) {
		    icon[0]=iconsDirectory + "Missile.png";
		    icon[1]="0";
		    icon[2]="0";
		} else if (type.equals("Ground")) {
		    icon[0]=iconsDirectory + "Truck.png";
		    icon[1]="0";
		    icon[2]="0";
		} else if (type.equals("Surface")) {
		    icon[0]=iconsDirectory + "Boat.png";
		    icon[1]="0";
		    icon[2]="0";
		} else if (type.equals("Sub-surface")) {
		    icon[0]=iconsDirectory + "Submarine.png";
		    icon[1]="0";
		    icon[2]="0";
		} else if (classification.equals("Unknown")) {
		    icon[0]="root://icons/palette-5.png"; // "U" for Unknown
		    icon[1]="128";
		    icon[2]="128";
		} else {
		    icon[0]="root://icons/palette-2.png"; //airplane
		    icon[1]="0";
		    icon[2]="0";
		}
		break;
		
	    case CLASSIFICATION:
	    	if (classification.equals("Friend")) {
		    icon[0]="root://icons/palette-4.png"; //gold star
		    icon[1]="224";
		    icon[2]="64";
		} else if (classification.equals("Assumed Friend")) {
		    icon[0]="root://icons/palette-4.png"; //gold star
		    icon[1]="224";
		    icon[2]="64";
		} else if (classification.equals("Neutral")) {
		    icon[0]="root://icons/palette-2.png"; //cross in diamond
		    icon[1]="224";
		    icon[2]="224";
		} else if (classification.equals("Pending")) {
		    icon[0]="root://icons/palette-2.png"; //cross in diamond
		    icon[1]="224";
		    icon[2]="224";
		} else if (classification.equals("Suspect")) {
		    icon[0]="root://icons/palette-3.png"; //bang in triangle
		    icon[1]="32";
		    icon[2]="64";
		} else if (classification.equals("Hostile")) {
		    icon[0]="root://icons/palette-3.png"; //radioactive
		    icon[1]="224";
		    icon[2]="64";
		} else if (classification.equals("Unknown")) {
		    icon[0]="root://icons/palette-5.png"; // "U" for Unknown
		    icon[1]="128";
		    icon[2]="128";
		} else {
		    icon[0]="root://icons/palette-2.png"; //airplane
		    icon[1]="0";
		    icon[2]="0";
		}
		break;
		
	    case CONSTANT:
		if (iconName.equals("Red Dot")) {
		    icon[0]="root://icons/palette-4.png";
		    icon[1]="32";
		    icon[2]="32";
		    break;
		} else if (iconName.equals("Red Square")) {
		    icon[0]="root://icons/palette-4.png";
		    icon[1]="0";
		    icon[2]="32";
		    break;
		} else if (iconName.equals("Airplane")) {
		    icon[0]="root://icons/palette-2.png";
		    icon[1]="0";
		    icon[2]="0";
		    break;
		} else if (iconName.equals("3D Airplane")) {
		    icon[0]=iconsDirectory + "aircraft.dae";
		    icon[1]="0";
		    icon[2]="0";
		    break;
		}
	    
	    case NONE:
	    	icon[0]=null;
		icon[1]=null;
		icon[2]=null;
		break;
	    
	    default:
		icon[0]="root://icons/palette-2.png"; //airplane
		icon[1]="0";
		icon[2]="0";
	}
	
	// JPW 07/23/2007: Add check on "http://" and "https://"
	if ( (icon[0] != null)                &&
	     (!iconsUsed.contains(icon[0]))   &&
	     (!icon[0].startsWith("root"))    &&
	     (!icon[0].startsWith("http://")) &&
	     (!icon[0].startsWith("https://")) )
	{
	    iconsUsed.add(icon[0]);
	}
	
	return icon;
	
    }
    
    /**
      * Angle correction variables.
      */
    private double
	headingBias,
	pitchBias,
	rollBias,
	headingSwitchSign = 1,
	pitchSwitchSign = 1,
	rollSwitchSign = 1;
	
    /**
      * This dialog is lazily initialized when needed.
      */
    private AngleCorrectionDlg angleCorrectionDlg = null;
    
    private class AngleCorrectionDlg extends javax.swing.JDialog
    {
	public AngleCorrectionDlg()
	{
	    super(frame, "Angle Corrections", true); // modal
	    
	    addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent we) {
		    doCancel();
		}
	    });
	    
	    java.awt.Container cp = getContentPane();
	    cp.setLayout(new java.awt.GridLayout(5, 3));
	    
	    cp.add(new JLabel("Angle", JLabel.CENTER));
	    cp.add(new JLabel("Switch Sign?", JLabel.CENTER));
	    cp.add(new JLabel("Bias", JLabel.CENTER));
	    
	    cp.add(new JLabel("Heading"));
	    cp.add(hsb = new javax.swing.JCheckBox());
	    cp.add(hBias = new javax.swing.JTextField(5));
	    
	    cp.add(new JLabel("Pitch"));
	    cp.add(psb = new javax.swing.JCheckBox());
	    cp.add(pBias = new javax.swing.JTextField(5));
	    
	    cp.add(new JLabel("Roll"));
	    cp.add(rsb = new javax.swing.JCheckBox());
	    cp.add(rBias = new javax.swing.JTextField(5));
	    
	    JButton b = new JButton("Ok");
	    b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    doOk();
		}
	    } );
	    cp.add(b);
	    cp.add(new JLabel("")); // take up center pos
	    b = new JButton("Cancel");
	    b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    doCancel();
		}
	    } );
	    cp.add(b);
	    
	    pack();
	    
	    writeGUI();
	}
	
	private void doOk()
	{
	    try {
		readGUI();
		setVisible(false);
	    } catch (NumberFormatException nfe) {
		JOptionPane.showMessageDialog(
			this,
			nfe.getMessage(),
			"Error",
			JOptionPane.ERROR_MESSAGE
		);
	    }
	}
	
	private void doCancel()
	{
	    // Reset the GUI with variables so ready for next use:
	    writeGUI();
	    setVisible(false);
	}
	
	/**
	  * Read the controls into variables.  Note that no variables are 
	  *  written until all are validated.
	  */
	private void readGUI()
	{
	    double hb, pb, rb;
	    String field = null;
	    
	    try {
		field = "Heading";
		hb = Double.parseDouble(hBias.getText());
		field = "Pitch";
		pb = Double.parseDouble(pBias.getText());
		field = "Roll";
		rb = Double.parseDouble(rBias.getText());		
	    } catch (Exception e) {
		throw new NumberFormatException(
			"Could not parse field \""+field+" Bias\".  Please"
			+" correct."
		);
	    }
	    
	    headingBias = hb;
	    pitchBias = pb;
	    rollBias = rb;
	 
	    // These won't throw.
	    headingSwitchSign = hsb.isSelected()?-1:1;
	    pitchSwitchSign = psb.isSelected()?-1:1;
	    rollSwitchSign = rsb.isSelected()?-1:1;
	}
	
	/**
	  * Write the current variable states into the controls.
	  */
	private void writeGUI()
	{
	    hBias.setText(Double.toString(headingBias));
	    pBias.setText(Double.toString(pitchBias));
	    rBias.setText(Double.toString(rollBias));
	    
	    hsb.setSelected(headingSwitchSign == -1);
	    hsb.setSelected(pitchSwitchSign == -1);
	    rsb.setSelected(rollSwitchSign == -1);	    
	}
	
	/**
	  * Sign correction check boxes.
	  */
	private final javax.swing.JCheckBox hsb, psb, rsb;
	/**
	  * Offset correction text fields.
	  */
	private final javax.swing.JTextField hBias, pBias, rBias;
    }
	
}

