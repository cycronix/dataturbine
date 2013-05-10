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

import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.datasink.*;
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.ArgHandler;

/**
 * M-JPEG video capture plugin.
 * <p>
 * This <bold>RBNB</bold> V2 plugin provides a simple video capture stream
 * consisting of an M-JPEG stream.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 02/12/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/15/2002  INB	Created.
 *
 */
public class JPEGCapture
    implements ControllerListener,
	       DataSinkListener
{
    /**
     * size of the archive in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private int archive = ARCHIVE;

    /**
     * the arachive mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/12/2003
     */
    private String archiveMode = "None";

    /**
     * size of the cache in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private int cache = CACHE;

    /**
     * the capture encoding format.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private String captureEncoding = CAPTUREENCODING;

    /**
     * the <bold>JMF</bold> video capture source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private DataSource captureSource = null;

    /**
     * the selected capture device.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private CaptureDeviceInfo cdi = null;

    /**
     * the device selected.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private int device = DEVICE;

    /**
     * the user selected dimensions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private String dimensions = SMALL;

    /**
     * the capture frame rate in frames per second.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private float frameRate = FRAMERATE;

    /**
     * the video capture height in pixels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private int height = HEIGHT;

    /**
     * the <bold>JMF</bold> processed video JPEG stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private DataSource jpegSource = null;

    /**
     * list information about the available devices or formats?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private boolean listInformation = false;

    /**
     * name of the <bold>RBNB</bold> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private String name = NAME;

    /**
     * the <bold>JMF</bold> video processor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private Processor processor = null;

    /**
     * desired JPEG quality.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private int quality = QUALITY;

    /**
     * the <bold>JMF</bold> video to <bold>RBNB</bold> data sink.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/26/2002
     */
    private DataSink rbnbSink = null;

    /**
     * the address of the <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private String serverAddress = ADDRESS;

    /**
     * the capture size.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private int size = SIZE;

    /**
     * the state change failed?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private boolean stateFailed = false;

    /**
     * state locking object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private Object stateLock = new Object();

    /**
     * the JMF video format to use to perform the conversion.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private VideoFormat useFormat = null;

    /**
     * the video capture width in pixels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private int width = WIDTH;

    /**
     * the default <bold>RBNB</bold> server address.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static String ADDRESS = "localhost:3333";

    /**
     * the default archive size in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static int ARCHIVE = 0;

    /**
     * the default cache size in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static int CACHE = 100;

    /**
     * the default video capture encoding format.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static String CAPTUREENCODING = null;

    /**
     * the default device.
     * <p>
     * A value of zero (0) means the first device supporting a valid format.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private final static int DEVICE = 0;

    /**
     * the default video capture frame rate.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static float FRAMERATE = 1.0f;

    /**
     * the default video capture height in pixels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static int HEIGHT = 240;

    /**
     * large capture size.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static String LARGE = "LARGE";

    /**
     * medium capture size.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static String MEDIUM = "MEDIUM";

    /**
     * default name for the <bold>RBNB</bold> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static String NAME = "rbnbVideo";

    /**
     * the default JPEG conversion quality.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static int QUALITY = 50;

    /**
     * the default size (1 is smallest, N is largest).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */
    private final static int SIZE = 1;

    /**
     * small capture dimensions.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private static final String SMALL = "SMALL";

    /**
     * the default video capture width in pixels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */
    private final static int WIDTH = 320;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/27/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    public JPEGCapture() {
	super();
    }

    /**
     * Class constructor to build a <code>JPEGCapture</code> from command line
     * arguments.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments:
     *		    <p><ul>
     *		    <li>-a <bold>RBNB</bold> server addresss</li>
     *		    <li>-A archive size in frames</li>
     *		    <li>-C cache size in frames</li>
     *		    <li>-d capture dimensions = SMALL, MEDIUM, LARGE, #</li>
     *		    <li>-D device = 1 to N</li>
     *		    <li>-I</li>
     *		    <li>-M <archive mode>
     *		    <li>-n name of the <bold>RBNB</bold> data path</li>
     *		    <li>-q JPEG quality = 0 to 100</li>
     *		    <li>-R capture frame rate</li>
     *		    </ul>
     * @since V2.0
     * @version 02/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/18/2002  INB	Created.
     *
     */
    public JPEGCapture(String[] argsI) {
	try {
	    ArgHandler ah = new ArgHandler(argsI);
	    String value;

	    if ((value = ah.getOption('a')) != null) {
		serverAddress = value;
	    }
	    if ((value = ah.getOption('A')) != null) {
		archive = Integer.parseInt(value);
		if (archive < 0) {
		    throw new java.lang.IllegalArgumentException
			(value +
			 " is not a legal archive size. " +
			 "Legal sizes are 0 for\n" +
			 "no archive, or a value > 0 for an archive.");
		} else if (archive == 0) {
		    archiveMode = "None";
		} else {
		    archiveMode = "Create";
		}
	    }
	    if ((value = ah.getOption('C')) != null) {
		cache = Integer.parseInt(value);
		if ((cache < 1) ||
		    ((archive > 0) && (cache > archive))) {
		    throw new java.lang.IllegalArgumentException
			(value + " is not a legal cache size.");
		}
	    }
		
	    if ((value = ah.getOption('d')) != null) {
		if (value.equalsIgnoreCase(SMALL) ||
		    value.equalsIgnoreCase(MEDIUM) ||
		    value.equalsIgnoreCase(LARGE)) {
		    dimensions = value;
		} else if (value.indexOf("x") != -1) {
		    dimensions = value;
		} else {
		    size = Integer.parseInt(value);
		    dimensions = "";
		}
	    }
	    if ((value = ah.getOption('D')) != null) {
		device = Integer.parseInt(value);
	    }
	    listInformation = ah.checkFlag('I');
	    if ((value = ah.getOption('M')) != null) {
		archiveMode = value;
		if (archiveMode.equalsIgnoreCase("none") && (archive > 0)) {
		    throw new java.lang.IllegalArgumentException
			("An archive size was specified, but archiving has " +
			 "been turned off.");
		} else if (archiveMode.equalsIgnoreCase("create") &&
			   (archive == 0)) {
		    throw new java.lang.IllegalArgumentException
			("Cannot create a zero-length archive.");
		}
	    }
	    if ((value = ah.getOption('n')) != null) {
		name = value;
	    }
	    if ((value = ah.getOption('q')) != null) {
		quality = Integer.parseInt(value);
		if ((quality < 0) || (quality > 100)) {
		    throw new java.lang.IllegalArgumentException
			(value +
			 " is not a legal quality. The range is 0 to 100.");
		}
	    }
	    if ((value = ah.getOption('R')) != null) {
		frameRate = Float.parseFloat(value);
	    }
	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	    RBNBProcess.exit(-1);
	}
    }

    /**
     * Lists information about the available devices or a specific device.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/25/2002  INB	Created.
     *
     */
    private final void displayInformation() {
	Vector devices = CaptureDeviceManager.getDeviceList(null);
	CaptureDeviceInfo cdiL;
	boolean shown;
	Format[] formats = null;

	for (int idx = 0, devCount = 0; idx < devices.size(); ++idx) {
	    cdiL = (CaptureDeviceInfo) devices.elementAt(idx);
	    formats = cdiL.getFormats();
	    shown = false;

	    for (int idx1 = 0,
		     count = 0;
		 idx1 < formats.length;
		 ++idx1) {


		if (formats[idx1] instanceof VideoFormat) {
		    VideoFormat vf = (VideoFormat) formats[idx1];
		    /*
		    if ((vf instanceof RGBFormat) &&
			(((RGBFormat) vf).getFlipped() == Format.TRUE)) {
			continue;
		    }
		    */
		    if ((captureEncoding == null) ||
			vf.getEncoding().equalsIgnoreCase
			(captureEncoding)) {
			if (count == 0) {
			    ++devCount;
			    if ((device != 0) &&
				(devCount != device)) {
				break;
			    }
			}
			++count;
			if (!shown) {
			    System.out.println("\n" + cdiL.getName());
			    shown = true;
			}
			System.out.println(count + ": " + vf);
		    }

/*
		} else if (formats[idx1] instanceof AudioFormat) {
		    AudioFormat af = (AudioFormat) formats[idx1];

		    System.out.println(cdiL.getName());
		    System.out.println("A: " + af);
*/

		}
	    }
	}
    }

    /**
     * Execute the video capture.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/28/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/28/2002  INB	Created.
     *
     */
    public final void exec() {
	try {
	    Runtime.getRuntime().addShutdownHook(new ShutdownThread());
	} catch (java.lang.NoSuchMethodError e) {
	}
	if (listInformation) {
	    displayInformation();
	} else {
	    setup();
	    go();
	}
    }

    /**
     * Starts the JPEG capture process.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/29/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private void go() {
	try {
	    rbnbSink.start();
	    processor.start();
	} catch (java.lang.Exception e) {
	    try {
		rbnbSink.stop();
	    } catch (java.lang.Exception e1) {
	    }
	    rbnbSink.close();
	    rbnbSink = null;
	    processor.stop();
	    processor.close();
	    processor = null;
	}	    
    }

    /**
     * Sets up the video processing sequence.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/29/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private final void setup() {
	try {
	    VideoFormat vformat = createCaptureFormat();
	    createCaptureDS(vformat);
	    createProcessor();

	    if (!waitForState(processor,Processor.Realized)) {
		throw new Exception("Processor could not be realized.");
	    }
	    jpegSource = processor.getDataOutput();
	    createRBNBSink();

	} catch (Exception e) {
	    e.printStackTrace();
	    if (captureSource != null) {
		captureSource.disconnect();
	    }
	    if (rbnbSink != null) {
		try {
		    rbnbSink.stop();
		} catch (java.lang.Exception e1) {
		}
		rbnbSink.close();
		rbnbSink = null;
	    }
	    if (processor != null) {
		processor.stop();
		processor.close();
		processor = null;
	    }
	    RBNBProcess.exit(-1);
	}
    }

    /**
     * Creates the capture format object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the format.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/27/2002  INB	Created.
     *
     */
    private final VideoFormat createCaptureFormat()
	throws java.lang.Exception
    {
	Vector devices = CaptureDeviceManager.getDeviceList(null);
	Format[] formats = null;

	if (dimensions.indexOf("x") != -1) {
	    int xat = dimensions.indexOf("x");
	    width = Integer.parseInt(dimensions.substring(0,xat));
	    height = Integer.parseInt(dimensions.substring(xat + 1));
	    Format format = new VideoFormat
		(captureEncoding,
		 new Dimension(width,height),
		 Format.NOT_SPECIFIED,
		 null,
		 frameRate);
	    boolean found = false;
	    for (int idx = 0,
		     devCount = 0;
		 idx < devices.size();
		 ++idx) {
		cdi = (CaptureDeviceInfo) devices.elementAt(idx);
		formats = cdi.getFormats();

		for (int idx1 = 0, count = 0; idx1 < formats.length; ++idx1) {
		    if (formats[idx1] instanceof VideoFormat) {
			VideoFormat vf = (VideoFormat) formats[idx1];
			/*
			if ((vf instanceof RGBFormat) &&
			    (((RGBFormat) vf).getFlipped() == Format.TRUE)) {
			    continue;
			}
			*/
			if ((captureEncoding == null) ||
			    vf.getEncoding().equalsIgnoreCase
			    (captureEncoding)) {
			    if (count == 0) {
				++devCount;
				if ((device != 0) &&
				    (devCount != device)) {
				    break;
				}
			    }
			    ++count;
			    if (vf.matches(format)) {
				found = true;
				break;
			    }
			}
		    }
		}
	    }
	    if (!found) {
		throw new java.lang.Exception
		    ("Cannot use capture dimensions " +
		     width + "x" + height + ".");
	    }

	} else {
	    Dimension minimum = null,
		maximum = null;
	    long totalHeight = 0,
		totalWidth = 0,
		count = 0;
	    for (int idx = 0,
		     devCount = 0;
		 idx < devices.size();
		 ++idx) {
		cdi = (CaptureDeviceInfo) devices.elementAt(idx);
		formats = cdi.getFormats();

		for (int idx1 = 0, fCount = 0; idx1 < formats.length; ++idx1) {
		    if (formats[idx1] instanceof VideoFormat) {
			VideoFormat vf = (VideoFormat) formats[idx1];
			/*
			if ((vf instanceof RGBFormat) &&
			    (((RGBFormat) vf).getFlipped() == Format.TRUE)) {
			    continue;
			}
			*/
			if ((captureEncoding == null) ||
			    vf.getEncoding().equalsIgnoreCase
			    (captureEncoding)) {
			    if (fCount == 0) {
				++devCount;
				if ((device != 0) &&
				    (devCount != device)) {
				    break;
				}
			    }
			    ++fCount;
			    ++count;
			    if (dimensions.equals("")) {
				if (fCount == size) {
				    minimum = vf.getSize();
				    break;
				} else {
				    continue;
				}
			    }
			    totalHeight += vf.getSize().getHeight();
			    totalWidth += vf.getSize().getWidth();

			    if (minimum == null) {
				minimum = vf.getSize();
			    } else if ((vf.getSize().getHeight()*
					((long) vf.getSize().getWidth())) <
				       (minimum.getHeight()*
					((long) minimum.getWidth()))) {
				minimum = vf.getSize();
			    }

			    if (maximum == null) {
				maximum = vf.getSize();
			    } else if ((vf.getSize().getHeight()*
					((long) vf.getSize().getWidth())) >
				       (maximum.getHeight()*
					((long) maximum.getWidth()))) {
				maximum = vf.getSize();
			    }
			}
		    }
		}

		if (minimum != null) {
		    break;
		}
	    }

	    if (minimum == null) {
		throw new java.lang.IllegalStateException
		    ("Cannot find a valid video capture device.");
	    }
	    if (dimensions.equals("") || dimensions.equalsIgnoreCase(SMALL)) {
		width = (int) minimum.getWidth();
		height = (int) minimum.getHeight();
	    } else if (dimensions.equalsIgnoreCase(LARGE)) {
		width = (int) maximum.getWidth();
		height = (int) maximum.getHeight();
	    } else {
		width = (int) (totalWidth/count);
		height = (int) (totalHeight/count);
		Dimension medium = null;
		for (int idx = 0; idx < formats.length; ++idx) {
		    if (formats[idx] instanceof VideoFormat) {
			VideoFormat vf = (VideoFormat) formats[idx];
			/*
			if ((vf instanceof RGBFormat) &&
			    (((RGBFormat) vf).getFlipped() == Format.TRUE)) {
			    continue;
			}
			*/
			if ((captureEncoding == null) ||
			    vf.getEncoding().equalsIgnoreCase
			    (captureEncoding)) {
			    if (medium == null) {
				medium = vf.getSize();
			    } else if
				((Math.abs(vf.getSize().getHeight() - height) +
				  Math.abs(vf.getSize().getWidth() - width)) <
				 Math.abs(medium.getHeight() - height) +
				 Math.abs(medium.getWidth() - width)) {
				medium = vf.getSize();
			    }
			}
		    }
		}
		width = (int) medium.getWidth();
		height = (int) medium.getHeight();
	    }
	}
	
	return (new VideoFormat
	    (captureEncoding,
	     new Dimension(width,height),
	     Format.NOT_SPECIFIED,
	     null,
	     frameRate));
    }

    /**
     * Creates the video capture data source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vformatI the video format to use.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private final void createCaptureDS(VideoFormat vformatI)
	throws java.lang.Exception
    {
	MediaLocator ml = cdi.getLocator();
	captureSource = Manager.createDataSource(ml);
	captureSource.connect();
	setCaptureFormat(vformatI);
    }

    /**
     * Sets the capture video format.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vformatI the video format to use.
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 08/27/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private final void setCaptureFormat(VideoFormat vformatI)
	throws java.lang.Exception
    {
	CaptureDevice device = (CaptureDevice) captureSource;
	FormatControl[] fcs = device.getFormatControls();
	if (fcs.length == 0) {
	    throw new Exception("No format controls found.");
	}
	FormatControl fc = fcs[0];
	Format[] formats = fc.getSupportedFormats();

	for (int idx = 0; idx < formats.length; ++idx) {
	    if (formats[idx].matches(vformatI)) {
		/*
		if ((formats[idx] instanceof RGBFormat) &&
		    (((RGBFormat) formats[idx]).getFlipped() == Format.TRUE)) {
		    continue;
		}
		*/
		useFormat = (VideoFormat) formats[idx].intersects(vformatI);
		fc.setFormat(useFormat);
		break;
	    }
	}
    }

    /**
     * Creates the video processor to capture the video and convert it to
     * JPEGs.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 08/27/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private final void createProcessor()
	throws java.lang.Exception
    {
	if (captureSource != null) {
	    processor = Manager.createProcessor(captureSource);
	    processor.addControllerListener(this);

	    if (!waitForState(processor,Processor.Configured)) {
		throw new Exception("Processor could not be configured.");
	    }
	    processor.setContentDescriptor
		(new ContentDescriptor(captureSource.getContentType()));
	    Format format = new JPEGFormat
		(useFormat.getSize(),
		 Format.NOT_SPECIFIED,
		 null,
		 -1f,
		 quality,
		 Format.NOT_SPECIFIED);
	    TrackControl[] trackControls = processor.getTrackControls();
	    trackControls[0].setFormat(format);
	}
    }

    /**
     * Creates the JMF data sink to send the JPEGs to the <bold>RBNB</bold>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.lang.Exception if an error occurs.
     * @since V2.0
     * @version 02/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private final void createRBNBSink()
	throws java.lang.Exception
    {
	MediaLocator ml = new MediaLocator
	    ("rbnb://" + serverAddress +
	     "/" + name + "(" + cache +
	     "," + archive +
	     "," + archiveMode +
	     ")");
	/*
	rbnbSink = new COM.Creare.RBNB.media.datasink.protocol.Handler();
	((COM.Creare.RBNB.media.datasink.protocol.Handler)
	 rbnbSink).setUseEncoding(true);
	*/
	rbnbSink = new com.rbnb.media.datasink.protocol.Handler();
	((com.rbnb.media.datasink.protocol.Handler)
	 rbnbSink).setUseEncoding(true);
	((com.rbnb.media.datasink.protocol.Handler)
	 rbnbSink).setRequestedRate((double) frameRate);
	rbnbSink.addDataSinkListener(this);
	rbnbSink.setSource(jpegSource);
	rbnbSink.setOutputLocator(ml);
	rbnbSink.open();
    }

    /**
     * The main application method.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments:
     *		    <p><ul>
     *		    <li>-a <bold>RBNB</bold> server addresss</li>
     *		    <li>-A archive size in frames</li>
     *		    <li>-C cache size in frames</li>
     *		    <li>-d capture dimensions = SMALL, MEDIUM, LARGE, #</li>
     *		    <li>-D device = 1 to N</li>
     *		    <li>-I</li>
     *		    <li>-M <archive mode>
     *		    <li>-n name of the <bold>RBNB</bold> data path</li>
     *		    <li>-q JPEG quality = 0 to 100</li>
     *		    <li>-R capture frame rate</li>
     *		    </ul>
     * @since V2.0
     * @version 02/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	JPEGCapture jcap = new JPEGCapture(argsI);
	jcap.exec();
    }

    /**
     * Waits for a video processor state change.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pI     the video processor.
     * @param stateI the desired state.
     * @return was the state reached?
     * @since V2.0
     * @version 08/27/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private synchronized boolean waitForState(Processor pI,int stateI) {
	pI.addControllerListener(new StateListener());
	stateFailed = false;
	
	if (stateI == Processor.Configured) {
	    pI.configure();
	} else if (stateI == Processor.Realized) {
	    pI.realize();
	}

	while ((pI.getState() < stateI) && !stateFailed) {
	    synchronized (stateLock) {
		try {
		    stateLock.wait();
		} catch (InterruptedException ie) {
		    break;
		}
	    }
	}

	return (!stateFailed);
    }

    /**
     * Handles a video controller event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the video controller event.
     * @since V2.0
     * @version 08/29/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    public void controllerUpdate(ControllerEvent eventI) {
        if (eventI instanceof ControllerErrorEvent) {
	    if (rbnbSink != null) {
		try {
		    rbnbSink.stop();
		} catch (java.lang.Exception e) {
		}
		rbnbSink.close();
		rbnbSink = null;
	    }
	    if (processor != null) {
		processor.stop();
		processor.close();
		processor = null;
	    }
	    RBNBProcess.exit(-1);
        } else if (eventI instanceof EndOfMediaEvent) {
	    if (rbnbSink != null) {
		try {
		    rbnbSink.stop();
		} catch (java.lang.Exception e) {
		}
		rbnbSink.close();
		rbnbSink = null;
	    }
	    if (processor != null) {
		processor.stop();
		processor.close();
		processor = null;
	    }
	    RBNBProcess.exit(0);
	}
    }

    /**
     * Handles a data sink event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the data sink event.
     * @since V2.0
     * @version 08/29/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/28/2002  INB	Created.
     *
     */
    public void dataSinkUpdate(DataSinkEvent eventI) {
        if (eventI instanceof DataSinkErrorEvent) {
	    try {
		rbnbSink.stop();
	    } catch (java.lang.Exception e) {
	    }
	    rbnbSink.close();
	    processor.stop();
	    processor.close();
	    RBNBProcess.exit(-1);
        } else if (eventI instanceof EndOfStreamEvent) {
	    try {
		rbnbSink.stop();
	    } catch (java.lang.Exception e) {
	    }
	    rbnbSink.close();
	    processor.stop();
	    processor.close();
	    RBNBProcess.exit(0);
	}
    }

    /**
     * Internal class to handle video processor state changes.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2002  INB	Created.
     *
     */
    private class StateListener implements ControllerListener {

	/**
	 * Class constructor.
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
	 * 09/25/2002  INB	Created.
	 *
	 */
	StateListener() {
	    super();
	}

	/**
	 * Handles a controller update event.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param ceI the controller event.
	 * @since V2.0
	 * @version 08/27/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 08/15/2002  INB	Created.
	 *
	 */
	public void controllerUpdate(ControllerEvent ceI) {
	    if (ceI instanceof ControllerClosedEvent) {
		stateFailed = true;

	    }

	    synchronized (stateLock) {
		stateLock.notifyAll();
	    }
	}
    }

    /**
     * Shutdown task to clean up.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/29/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/29/2002  INB	Created.
     *
     */
    private final class ShutdownThread
	extends java.lang.Thread
    {

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 08/29/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 08/29/2002  INB	Created.
	 *
	 */
	ShutdownThread() {
	    super();
	}

	/**
	 * Shuts down the video capture.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 08/29/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 08/29/2002  INB	Created.
	 *
	 */
	public final void run() {
	    if (rbnbSink != null) {
		try {
		    rbnbSink.stop();
		} catch (java.lang.Exception e) {
		}
		rbnbSink.close();
		rbnbSink = null;
	    }
	    if (processor != null) {
		processor.stop();
		processor.close();
		processor = null;
	    }
	}
    }
}
