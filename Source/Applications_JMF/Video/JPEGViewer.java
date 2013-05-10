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
import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import com.rbnb.sapi.*;
import com.rbnb.simpleplayer.*;
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.ArgHandler;

/**
 * M-JPEG video viewer.
 * <p>
 * This <bold>RBNB</bold> V2 application provides a simple video viewer from
 * one or more streams of M-JPEG data.
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
 * 09/19/2002  INB	Created.
 *
 */
public final class JPEGViewer
    extends Frame
    implements ActionListener,
	       AdjustmentListener,
	       ItemListener,
	       PlayerTimeListener,
	       Runnable,
	       WindowListener
{
    /**
     * the address of the <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/20/2002
     */
    private String address = "localhost:3333";

    /**
     * are we performing an update?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/25/2002n
     */
    private boolean inUpdate = false;

    /**
     * the monitor button.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */
    private Checkbox monitorButton = null;

    /**
     * go to a position.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */
    private int gotoPosition = -1;

    /**
     * the wall clock time of the last post.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/19/2002
     */
    private double lastClock = -Double.MAX_VALUE;

    /**
     * the time of the last posted data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/19/2002
     */
    private double lastTime = -Double.MAX_VALUE;

    /**
     * monitor mode status.
     * <p>
     * <ol start=0>
     *     <li>is no change,</li>
     *     <li>is start monitoring, or</li>
     *     <li>is end monitoring.</li>
     * </ol><p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */
    private int monitor = 100000;

    /**
     * the position scrollbar.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/24/2002
     */
    private Scrollbar position = null;

    /**
     * quit button pressed?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */
    private boolean quitPressed = false;

    /**
     * the current display time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/23/2002
     */
    private TextField currentDisplayTime = null;

    /**
     * the list of <code>JPEGStreams</code> for the channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/19/2002
     */
    private Vector streams = new Vector();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/19/2002  INB	Created.
     *
     */
    JPEGViewer() {
	super();
	addWindowListener(this);
    }

    /**
     * Builds a <code>JPEGViewer</code> from the command line arguments.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @exception java.lang.Exception if an error occurs in parsing.
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
    public JPEGViewer(String[] argsI)
	throws java.lang.Exception
    {
	this();
	ArgHandler ah = new ArgHandler(argsI);
	String value;
	if ((value = ah.getOption('d')) != null) {
	    StringTokenizer st = new StringTokenizer(value,"x");
	    int width = Integer.parseInt(st.nextToken()),
		height = Integer.parseInt(st.nextToken());
	    setSize(width,height);
	} else {
	    setSize(220,250);
	}
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(gbl);

	if ((value = ah.getOption('a')) != null) {
	    address = value;
	}

	Panel panel = new Panel();
	Button quit = new Button("Quit");
	quit.addActionListener(this);
	panel.add(quit);
	monitorButton = new Checkbox("Monitor",false);
	monitorButton.addItemListener(this);
	panel.add(monitorButton);
	setGBC(gbl,
	       gbc,
	       panel,
	       0,0,
	       gbc.REMAINDER,1,
	       1.,0.01,
	       gbc.CENTER,
	       gbc.BOTH);
	add(panel);

	panel = new Panel();
	GridBagLayout gbl2 = new GridBagLayout();
	GridBagConstraints gbc2 = new GridBagConstraints();
	panel.setLayout(gbl2);
	position = new Scrollbar(Scrollbar.HORIZONTAL,
				 100000,
				 10,
				 0,
				 100000);
	position.addAdjustmentListener(this);
	setGBC(gbl2,
	       gbc2,
	       position,
	       0,0,
	       gbc.REMAINDER,gbc.REMAINDER,
	       1.,1.,
	       gbc2.CENTER,
	       gbc2.BOTH);
	panel.add(position);
	setGBC(gbl,
	       gbc,
	       panel,
	       0,1,
	       gbc.REMAINDER,1,
	       1.,.01,
	       gbc.CENTER,
	       gbc.BOTH);
	add(panel);

	SimpleDateFormat sdf = new SimpleDateFormat
	    ("MM/dd/yyyy HH:mm:ss.SSS");
	currentDisplayTime = new TextField(sdf.format(new Date()));
	currentDisplayTime.setEditable(false);
	setGBC(gbl,
	       gbc,
	       currentDisplayTime,
	       0,2,
	       gbc.REMAINDER,1,
	       1.,.025,
	       gbc.CENTER,
	       gbc.BOTH);
	add(currentDisplayTime);	

	if ((value = ah.getOption('c')) == null) {
	    value = "rbnbVideo/Video.jpg";
	}
	String channelName;
	JPEGStream stream;
	StringTokenizer st = new StringTokenizer(value,",");

	while (st.hasMoreTokens()) {
	    channelName = st.nextToken();
	    stream = new JPEGStream(this,channelName);
	    streams.addElement(stream);
	}
	panel = new Panel();
	int rows = (int) Math.sqrt(streams.size()),
	    columns = ((streams.size()/rows) +
		       ((((streams.size() % rows) != 0)) ? 1 : 0));
	gbl2 = new GridBagLayout();
	gbc2 = new GridBagConstraints();
	panel.setLayout(gbl2);
	int row = 0,
	    column = 0;
	for (int idx = 0; idx < streams.size(); ++idx) {
	    setGBC(gbl2,
		   gbc2,
		   (JPEGStream) streams.elementAt(idx),
		   column,row,
		   1,1,
		   1./columns,1./rows,
		   gbc2.CENTER,
		   gbc2.BOTH);
	    if (++column == columns) {
		++row;
		column = 0;
	    }
	    panel.add((JPEGStream) streams.elementAt(idx));
	}
	setGBC(gbl,
	       gbc,
	       panel,
	       0,3,
	       gbc.REMAINDER,gbc.REMAINDER,
	       1.,1.,
	       gbc.CENTER,
	       gbc.BOTH);
	add(panel);
    }

    /**
     * Performs a "quit" action.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event that caused this action.
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
    public final void actionPerformed(ActionEvent eventI) {
	monitorButton.setState(false);
	quitPressed = true;
	synchronized (this) {
	    notify();
	}
    }

    /**
     * Performs a goto action.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void adjustmentValueChanged(AdjustmentEvent eventI) {
	monitorButton.setState(false);
	gotoPosition = eventI.getValue();
	synchronized (this) {
	    notify();
	}
    }

    /**
     * Performs a monitor operation.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the adjustment event.
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
    public final void itemStateChanged(ItemEvent eventI) {
	if (eventI.getStateChange() == eventI.SELECTED) {
	    monitor = 1;
	} else if (eventI.getStateChange() == eventI.DESELECTED) {
	    monitor = 2;
	}
	synchronized (this) {
	    notify();
	}
    }

    /**
     * Main method for the <code>JPEGViewer</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI the command line arguments.
     * @since V2.0
     * @version 09/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/19/2002  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	try {
	    JPEGViewer viewer = new JPEGViewer(argsI);
	    (new Thread(viewer)).start();
	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	    RBNBProcess.exit(-1);
	}
    }

    /**
     * Paints this <code>JPEGViewer</code> to the graphics context.
     * <p>
     *
     * @author Ian Brown
     *
     * @param gI the graphics context.
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/19/2002  INB	Created.
     *
     */
    public final void paint(Graphics gI) {
	synchronized (this) {
	    if (inUpdate) {
		return;
	    }
	    inUpdate = true;
	}

	JPEGStream stream;
	for (int idx = 0; idx < streams.size(); ++idx) {
	    stream = (JPEGStream) streams.elementAt(idx);
	    stream.paint(gI);
	}
	super.paint(gI);

	inUpdate = false;
    }

    /**
     * Posts time information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param cmapI the <code>ChannelMap</code>.
     * @since V2.0
     * @version 09/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/24/2002  INB	Created.
     *
     */
    public final void postTime(ChannelMap cmapI) {
	double currentTime = Double.MAX_VALUE,
	    sTime;

	for (int idx = 0; idx < cmapI.NumberOfChannels(); ++idx) {
	    sTime = cmapI.GetTimeStart(idx);
	    currentTime = Math.min(currentTime,sTime);
	}

	if (currentTime == Double.MAX_VALUE) {
	    currentDisplayTime.setText("No data");
	} else {
	    SimpleDateFormat sdf = new SimpleDateFormat
		("MM/dd/yyyy HH:mm:ss.SSS");
	    long longTime = (long) currentTime;
	    double value = (currentTime - longTime)*1000.;
	    longTime = longTime*1000 + (long) value;
	    currentDisplayTime.setText(sdf.format(new Date(longTime)));
	}
    }

    /**
     * Runs this <code>JPEGViewer</code>.
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
     * 09/19/2002  INB	Created.
     *
     */
    public final void run() {
	try {
	    setVisible(true);
	    Thread.currentThread().yield();

	    Player player = new Player(address);
	    player.addTimeListener(this);
	    player.start();

	    JPEGStream stream;
	    for (int idx = 0; idx < streams.size(); ++idx) {
		stream = (JPEGStream) streams.elementAt(idx);
		player.add(stream.channelName,stream);
	    }

	    player.connect();
	    player.gotoPosition(100000,0,100000);
	    synchronized (this) {
		while (!quitPressed) {
		    wait(1000);

		    if (!player.isAlive()) {
			break;
		    }
		    if (gotoPosition >= 0) {
			player.gotoPosition(gotoPosition,0,100000);
			monitor = 0;
			gotoPosition = -1;
		    } else if (monitor == 1) {
			position.setValue(100000);
			player.monitor();
			monitor = 0;
		    } else if (monitor == 2) {
			player.pause();
			monitor = 0;
		    }
		}
	    }
	    player.terminate();
	    player.join();

	} catch (Exception e) {
	    e.printStackTrace();
	    RBNBProcess.exit(-1);
	}

	RBNBProcess.exit(0);
    }

    /**
     * Sets up the <code>GridBagConstraints</code> for an object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param gblI	 the <code>GridBagLayout</code> manager.
     * @param gbcI	 the <code>GridBagConstraints</code>.
     * @param componentI the <code>Component</code>.
     * @param xI	 the X coordinate.
     * @param yI	 the Y coordinate.
     * @param widthI	 the width.
     * @param heightI	 the height.
     * @param weightxI	 the X weight.
     * @param weightyI	 the Y weight.
     * @param anchorI	 the anchor.
     * @param fillI	 the fill.
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
    private final static void setGBC(GridBagLayout gblI,
				     GridBagConstraints gbcI,
				     Component componentI,
				     int xI,
				     int yI,
				     int widthI,
				     int heightI,
				     double weightxI,
				     double weightyI,
				     int anchorI,
				     int fillI)
    {
	gbcI.gridx = xI;
	gbcI.gridy = yI;
	gbcI.gridwidth = widthI;
	gbcI.gridheight = heightI;
	gbcI.weightx = weightxI;
	gbcI.weighty = weightyI;
	gbcI.anchor = anchorI;
	gbcI.fill = fillI;
	gbcI.insets = new Insets(0,0,0,0);
	gblI.setConstraints(componentI,gbcI);
    }

    /**
     * Handles a window activated event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowActivated(WindowEvent eventI) {
    }

    /**
     * Handles a window closed event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowClosed(WindowEvent eventI) {
	RBNBProcess.exit(0);
    }

    /**
     * Handles a window closing event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowClosing(WindowEvent eventI) {
	RBNBProcess.exit(0);
    }

    /**
     * Handles a window deactivated event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowDeactivated(WindowEvent eventI) {
    }

    /**
     * Handles a window deiconified event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowDeiconified(WindowEvent eventI) {
    }

    /**
     * Handles a window iconified event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowIconified(WindowEvent eventI) {
    }

    /**
     * Handles a window opened event.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the event.
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
    public final void windowOpened(WindowEvent eventI) {
    }

    /**
     * Internal class to handle a single channel of <bold>RBNB</bold> video
     * data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/19/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/19/2002  INB	Created.
     *
     */
    private final class JPEGStream
	extends Panel
	implements PlayerChannelListener
    {
	/**
	 * the channel name.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/19/2002
	 */
	String channelName = null;

	/**
	 * the frame at the top.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/20/2002
	 */
	private Frame frame = null;

	/**
	 * the image.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/19/2002
	 */
	private Image image = null;

	/**
	 * the height of the previous image.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/19/2002
	 */
	private int oldH = -1;

	/**
	 * the width of the previous image.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/19/2002
	 */
	private int oldW = -1;

	/**
	 * change in the size of the image.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/19/2002
	 */
	private boolean sizeChanged = false;

	/**
	 * Class constructor.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 09/19/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2002  INB	Created.
	 *
	 */
	JPEGStream() {
	    super();
	}

	/**
	 * Builds a <code>JPEGStream</code> for a particular channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param frameI       the frame at the top.
	 * @param channelNameI the channel name.
	 * @since V2.0
	 * @version 09/19/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2002  INB	Created.
	 *
	 */
	JPEGStream(Frame frameI,String channelNameI) {
	    this();
	    frame = frameI;
	    channelName = channelNameI;
	}

	/**
	 * Paints the current image into the <code>JPEGStream's</code> display.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param gI the graphics context.
	 * @since V2.0
	 * @version 09/25/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2002  INB	Created.
	 *
	 */
	public final void paint(Graphics gI) {
	    synchronized (this) {
		if (image != null) {
		    Dimension size = getSize();
		    if (sizeChanged) {
			gI.clearRect(0,0,size.width,size.height);
			sizeChanged = false;
		    }

		    int newW = image.getWidth(null),
			newH = image.getHeight(null);
		    while ((getToolkit().checkImage(image,
						    newW,
						    newH,
						    this) &
			    ImageObserver.ALLBITS) == 0) {
			try {
			    Thread.currentThread().sleep(1);
			} catch (java.lang.InterruptedException e) {
			    return;
			}
		    }

		    gI.drawImage(image,
				 (size.width - image.getWidth(null))/2,
				 (size.height - image.getHeight(null))/2,
				 this);
		}
	    }
	    super.paint(gI);
	}

	/**
	 * Posts data for the channel.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param cmapI the <code>ChannelMap</code> containing the data.
	 * @param cidxI the channel index.
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
	public final void post(ChannelMap cmapI,int cidxI) {
	    byte[][] data = cmapI.GetDataAsByteArray(cidxI);

	    update(data[data.length - 1]);
	}

	/**
	 * Updates the graphics display for this <code>JPEGStream</code>.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param gI the graphics context.
	 * @since V2.0
	 * @version 09/19/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2002  INB	Created.
	 *
	 */
	public final void update(Graphics gI) {
	    paint(gI);
	}

	/**
	 * Updates the JPEG frame being displayed.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param dataI the JPEG data.
	 * @since V2.0
	 * @version 09/19/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2002  INB	Created.
	 *
	 */
	final void update(byte[] dataI) {
	    synchronized (this) {
		Image oldImage = image;
		image = getToolkit().createImage(dataI);

		int newH = -1,
		    newW = -1;
		while (((newW = image.getWidth(null)) == -1) ||
		       ((newH = image.getHeight(null)) == -1)) {
		    try {
			Thread.currentThread().sleep(1);
		    } catch (java.lang.InterruptedException e) {
			return;
		    }
		}

		if (oldImage != null) {
		    if ((oldW != newW) || (oldH != newH)) {
			sizeChanged = true;
			oldW = newW;
			oldH = newH;
		    }
		    
		    oldImage.flush();
		}
	    }

	    frame.repaint();
	}
    }
}
