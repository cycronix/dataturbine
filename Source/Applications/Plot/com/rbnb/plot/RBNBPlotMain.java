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
  ***                                                           ***
  ***   Name : RBNBPlotMain     (application/applet)            ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan/M0tran                                   ***
  ***   Date : December 1997                                    ***
  ***                                                           ***
  ***   Copyright 1997 - 2005 Creare Inc.                       ***
  ***                                                           ***
  ***   Description : connects to a RBNB, plots data from       ***
  ***                 selected channels                         ***
  ***                                                           ***
  ***   Input : command line arguments                          ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :  plots to screen, optionally to printer        ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***								***
  ***	Modification History :					***
  ***	09/16/2005	JPW	Replace the existing Window	***
  ***				menu (Cascade button, Tile	***
  ***				button, and Auto toggle button)	***
  ***				with 3 radio buttons: Cascade,	***
  ***				Tile, and Manual		***
  ***   04/07/2005	JPW	Convert to Swing		***
  ***   04/05/2005	JPW	Remove View menu		***
  ***	04/19/2000	JPW	Added "Export To DataTurbine"	***
  ***				support.			***
  ***   11/8/00         EMF     Added features to allow plot    ***
  ***                           to run within rbnbManager's JVM ***
  ***                           or as a PlugIn.                 ***
  ***                                                           ***
  *****************************************************************
*/

package com.rbnb.plot;

//EMF 5/18/01: use replacement Time,Connection,Channel
//import COM.Creare.RBNB.Time;
//import COM.Creare.RBNB.API.Connection;
//import COM.Creare.RBNB.API.Channel;
import com.rbnb.utility.HostAndPortDialog;
// import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.JInfoDialog;
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.RBNBProcessInterface;
import java.applet.Applet;
// import java.awt.Button;
// import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Cursor;
// import java.awt.FileDialog;
//import java.awt.Font;
// import java.awt.Frame;
import java.awt.Graphics;
// import java.awt.Label;
// import java.awt.Menu;
// import java.awt.MenuItem;
// import java.awt.MenuBar;
import java.awt.Point;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;

// class extends applet, but works as application or applet
public class RBNBPlotMain extends Applet implements ActionListener,Runnable,Printable {
    public JFrame frame = null; // main plotting and control frame, drawn outside the browser
    boolean applicationRun=false;  //true if run as application
    private RunModeCubby rmc = null;
    private JButton startButton = null;
    private LayoutCubby loc = null;
    private RBNBInterface rbnbInterface = null;
    private PlotsContainer pc = null;
    private ConfigCubby cc = null;
    // JPW 04/08/2005: Change plot and table to be JRadioButtonMenuItem
    // private JCheckBoxMenuItem plot=null;
    // private JCheckBoxMenuItem table=null;
    private JRadioButtonMenuItem plot=null;
    private JRadioButtonMenuItem table=null;
    //EMF 1/26/01: add slave mode to menu
    //EMF 8/24/05: remove slave mode
    // private JCheckBoxMenuItem slave=null;
    //EMF 8/22/05: add cascade to window menu
    // JPW 09/16/2005: remove the auto menu;
    //                 add "Cascade", "Tile", and "Manual" radio button items
    // private JCheckBoxMenuItem auto=null;
    private JRadioButtonMenuItem rbCascade = null;
    private JRadioButtonMenuItem rbTile = null;
    private JRadioButtonMenuItem rbManual = null;
    // JPW 04/06/2005: Remove the View menu
    // private JCheckBoxMenuItem standard=null;
    // private JCheckBoxMenuItem metrics=null;
    // private JCheckBoxMenuItem all=null;
    private Cursor pointer = new Cursor(Cursor.DEFAULT_CURSOR);
    private Cursor hand = new Cursor(Cursor.HAND_CURSOR);
    private Cursor wait = new Cursor(Cursor.WAIT_CURSOR);
    private Thread runner;
    private UserControl uc=null;
    //EMF 11/8/00: added Environment instance to avoid state collisions
    //             between multiple instances of rbnbPlot running in same JVM
    private Environment environment=new Environment();
    //EMF 12/15/00: target object for passing to RBNBProcess.exit
    private RBNBProcessInterface target=null;
    
// default constructor method, no command line arguments
    public RBNBPlotMain() {
	applicationRun=false;
    }

// constructor method, with command line arguments and target for RBNBProcess.exit
    public RBNBPlotMain(String[] args) {
      this(args,null);
    }

// constructor method, with command line arguments for RBNB host and port
    public RBNBPlotMain(String[] args,RBNBProcessInterface targetI) {
        target=targetI;
	int idx,idx1,idx2,idx3,idx4;

	//EMF 1/13/04: set SCROLLGRIDLINES true
	// needs to be false in Environment, so PNG plugin behaves correctly
	// may be set false below if -g is set
	environment.SCROLLGRIDLINES=true;

	//handle the argument list
	for (idx=0;idx<args.length;idx++) {
	    if (args[idx].charAt(0) != '-') {
		System.err.println("Illegal command line argument:" + args[idx]);
		RBNBProcess.exit(-1,target);
	    }
	    
	    //deal with optional space after <dash><letter> before <value>
	    if (args[idx].length()==2) {
		idx1=idx+1;
		idx2=0;
	    }
	    else {
		idx1=idx;
		idx2=2;
	    }
	    
	    switch (args[idx].charAt(1)) {
		
		// -r [rbnbhost][:rbnbport]
	    case 'r':
		idx3=args[idx1].substring(idx2).indexOf(':');
		if (idx3==-1) { //no port given
		    environment.HOST=args[idx1].substring(idx2);
		}
		else {
		    if (idx2<idx3) {
                      environment.HOST=args[idx1].substring(idx2,idx2+idx3);
                    //EMF 3/5/01: correctly handle :port case
                    } else {
                      environment.HOST="localhost";
                    }
		    environment.PORT=(new Integer(args[idx1].substring(idx2+idx3+1))).intValue();
		}
		idx=idx1;
		break;
		
		// -S static mode (show all data, hide user controls
	    case 'S':
		environment.STATICMODE=false;
		break;
		
		// -k Kill RBNB on exit from plot
	    case 'k':
		environment.KILLRBNB=true;
		break;
		
		// -p specify position for main window (x,y)
	    case 'p':
		idx3=args[idx1].substring(idx2).indexOf(',');
		try {
		    environment.POSITION_X=Integer.parseInt(args[idx1].substring(idx2,idx2+idx3));
		    environment.POSITION_Y=Integer.parseInt(args[idx1].substring(idx2+idx3+1));
		}
		catch (NumberFormatException e) {}
		idx=idx1;
		if (environment.POSITION_X<0 || environment.POSITION_Y<0) {
		    System.err.println("-p format incorrect.  Use x,y.");
		    System.err.println("RBNBPlot aborting.");
		    RBNBProcess.exit(-3,target);
		}
		break;
		
		// -s specify manual scaling with min,max,div
	    case 's':
		idx3=args[idx1].substring(idx2).indexOf(',');
		idx4=args[idx1].substring(idx2).lastIndexOf(',');
		//use local variables, since Symantic compiler is too dumb to handle comparisons
		//with static variables correctly...
		double min=1,max=0;
		int div=0;
		try {
		    min=(new Double(args[idx1].substring(idx2,idx2+idx3))).doubleValue();
		    max=(new Double(args[idx1].substring(idx2+idx3+1,idx2+idx4))).doubleValue();
		    div=Integer.parseInt(args[idx1].substring(idx2+idx4+1));
		}
		catch (NumberFormatException e) {}
		if (div<0 || min>=max) {
		    System.err.println("-s format incorrect.  Use min,max,div.");
		    System.err.println("RBNBPlot aborting.");
		    RBNBProcess.exit(-3,target);
		}
		environment.SCALE_MIN=min;
		environment.SCALE_MAX=max;
		environment.SCALE_DIV=div;
		idx=idx1;
		break;
		
		// -d specify startup duration for plots
	    case 'd':
		environment.DURATION=new Time((new Double(args[idx1].substring(idx2))).doubleValue());
		idx=idx1;
		break;
		
		// -D <max duration>
                // EMF 12/30/03: limit duration for Pawscan project
		// max is set by reducing the number of steps in the duration
		// slider.  Calculation is done at initialization of UserControl
	    case 'D':
		environment.MAXDURATION=new Time((new Double(args[idx1].substring(idx2))).doubleValue());
		idx=idx1;
		break;

		// -g
		// EMF 1/13/04: no scrolling of gridlines
		// to get default to be true, see above where it is set
	    case 'g':
		environment.SCROLLGRIDLINES=false;
		break;

		// -u specify units label for time (default is Sec)
	    case 'u':
		environment.TIME_LABEL=" "+args[idx1].substring(idx2);
		idx=idx1;
		break;
		
		// -w specify minimum wait time in RT mode to limit 'spinning'
	    case 'w':
		environment.RTWAIT=Integer.parseInt(args[idx1].substring(idx2));
		idx=idx1;
		break;
		
		// -c show all channels, even those with unknown point sizes
	    case 'c':
		environment.SHOWALLCHANNELS=true;
                try {
                if (!args[idx1].substring(idx2).startsWith("-")) {
                  environment.CHANSPERDG=Integer.parseInt(args[idx1].substring(idx2));
                  idx=idx1;
                }
                } catch (Exception e) {}
		break;
		
		// -n no streaming, just request mode RBNB data requests
		// EMF 8/27/99: added option, default true
		// EMF 9/30/99: changed default to false
	    case 'n':
		environment.STREAMING=true;
		break;
		
		//EMF 10/28/00: added SLAVEMODE flag for running plot via player
	    case 'N':
		environment.STREAMING=true;
		environment.SLAVEMODE=true;
		break;
		
	    case 'e':		// mjm
		environment.EXPORT = "jdbc:odbc:rbnb";
		if(idx1 < args.length) {
		    String temp = args[idx1].substring(idx2); 
		    if( (temp.charAt(0) != '-') && (temp != null) ) {
			environment.EXPORT = temp;
			idx = idx1;
		    }
		}
		System.err.println("export to: " +environment.EXPORT);
		break;
            //EMF 3/20/01: added FOURBYTEASINTEGER flag to force display as integer rather than float for 4 byte data values
            case 'i':
                environment.FOURBYTEASINTEGER=true;
                break;

	    default:
		System.err.println("Unrecognized switch: "+args[idx]);
		RBNBProcess.exit(-3,target);
	    } //end switch
	} //end for idx
	
	applicationRun=true;
	//environment.showState();
    }

// init() method     
// reads RBNB server and port if applet, lays out button that spawns external
// frame and starts real work
    public void init() {
	if (applicationRun) createFrame();
	else {
	    String parameter=null;
	    //get RBNB server and port from HTML file
	    parameter=getParameter("host");
	    if (parameter!=null) environment.HOST=parameter;
	    parameter=getParameter("port");
	    if (parameter!=null) environment.PORT=Integer.parseInt(parameter);
	    parameter=getParameter("staticmode");
	    if (parameter!=null && parameter.equals("true")) environment.STATICMODE=true;
	    parameter=getParameter("killrbnb");
	    if (parameter!=null && parameter.equals("true")) environment.KILLRBNB=true;
	    parameter=getParameter("position");
	    if (parameter!=null) {
		int idx=parameter.indexOf(',');
		try {
		    environment.POSITION_X=Integer.parseInt(parameter.substring(0,idx));
		    environment.POSITION_Y=Integer.parseInt(parameter.substring(idx+1));
		}
		catch (NumberFormatException e) {}
		if (environment.POSITION_X<0 || environment.POSITION_Y<0) {
		    System.err.println("position format incorrect.  Use x,y.");
		    System.err.println("RBNBPlot aborting.");
		    RBNBProcess.exit(-3,target);
		}
	    }
	    parameter=getParameter("scaling");
	    if (parameter!=null) {
		int idx1=parameter.indexOf(',');
		int idx2=parameter.lastIndexOf(',');
		//use local variables, since Symantic compiler is too dumb to handle comparisons
		//with static variables correctly...
		double min=1,max=0;
		int div=0;
		try {
		    min=(new Double(parameter.substring(0,idx1))).doubleValue();
		    max=(new Double(parameter.substring(idx1+1,idx2))).doubleValue();
		    div=Integer.parseInt(parameter.substring(idx2+1));
		}
		catch (NumberFormatException e) {}
		if (div<0 || min>=max) {
		    System.err.println("-s format incorrect.  Use min,max,div.");
		    System.err.println("RBNBPlot aborting.");
		    RBNBProcess.exit(-3,target);
		}
		environment.SCALE_MIN=min;
		environment.SCALE_MAX=max;
		environment.SCALE_DIV=div;
	    }
	    parameter=getParameter("duration");
	    if (parameter!=null) environment.DURATION=new Time((new Double(parameter).doubleValue()));
	    parameter=getParameter("timelabel");
	    if (parameter!=null) environment.TIME_LABEL=" "+parameter;
	    parameter=getParameter("rtwait");
	    if (parameter!=null) environment.RTWAIT=Integer.parseInt(parameter);
	    parameter=getParameter("showallchannels");
	    if (parameter!=null && parameter.equals("true")) environment.SHOWALLCHANNELS=true;
	    parameter=getParameter("streaming");
	    if (parameter!=null && parameter.equals("false")) environment.STREAMING=false;
	    
	    setLayout(new BorderLayout());
	    startButton = new JButton("Start Plot");
	    startButton.addActionListener(this);
	    add(startButton,BorderLayout.CENTER);
	    setVisible(true);
	    //set the cursor to be a hand when over the button
	    setCursor(hand);
	}
    }
   
    public void actionPerformed(ActionEvent e) {
	
	// JPW 04/08/2005: Add support for the JRadioButtonMenuItems
	if (e.getSource() == plot) {
	    loc.set(LayoutCubby.PlotMode);
	    //pc.tile(false);
	    // JPW 09/16/2005: Make sure the "Cascade" and "Manual"
	    //                 menu items are enabled
	    rbCascade.setEnabled(true);
	    rbManual.setEnabled(true);
	    return;
	} else if (e.getSource() == table) {
	    loc.set(LayoutCubby.TableMode);
	    // pc.tile(true);
	    // JPW 09/16/2005: No more auto toggle
	    // auto.setState(true);
	    // JPW 09/16/2005: Select the "Tile" radio button; disable the
	    //                 "Cascade" and "Manual" radio buttons
	    rbCascade.setEnabled(false);
	    rbManual.setEnabled(false);
	    rbTile.setSelected(true);
	    return;
	} else if (e.getSource() == rbCascade) {
	    pc.setAuto(false);
	    pc.cascade();
	    return;
	} else if (e.getSource() == rbTile) {
	    pc.setAuto(true);
	    pc.tile(false);
	    return;
	} else if (e.getSource() == rbManual) {
	    pc.setAuto(false);
	    return;
	}
	
	if (e.getSource() instanceof JButton) {
	    //set the cursor to hourglass
	    setCursor(wait);
	    if (frame == null) createFrame();
	}
	else if (e.getSource() instanceof JMenuItem) {
	    JMenuItem mi = (JMenuItem)e.getSource();
	    // String arg=mi.getLabel();
	    String arg=mi.getText();
	    if (arg.equals("Exit")) {
		destroy();
		quitApp();
	    }
	    
	    /*
	    // JPW 09/16/2005: New "Window" menu; see above for hanlding
	    //                 "rbTile", "rbCascade", and "rbManual" menu items
	    //EMF 8/19/05: add support for cascade,tile
	    else if (arg.equals("Cascade")) {
		pc.cascade();
	    }
	    else if (arg.equals("Tile")) {
		pc.tile(false);
	    }
	    else if (arg.equals("Auto")) {
		if ( ((JCheckBoxMenuItem)mi).getState() ) {
		    pc.setAuto(true);
		} else {
		    
		}pc.setAuto(false);
	    }
	    */
	    
	    else if (arg.equals("Save Config")) {
		//use Properties not Hashtable so write is plain text
		Properties config=new Properties();
		pc.getConfig(config);
		loc.set(LayoutCubby.SaveConfig);
		cc.setHash(config);
		cc.getChannel(); //wait until RBNBInterface done
		config=(Properties)cc.getHash();
		//EMF 3/22/04: save to file, not RBNB channel
		try {
		    // JPW 04/07/2005: Convert to Swing
		    /*
		    FileDialog fd = new FileDialog(frame,"Save Configuration",FileDialog.SAVE);
		    fd.setDirectory(".");
		    fd.setFile("rbnbPlotConfig");
		    fd.show();
		    String fileName=fd.getDirectory()+fd.getFile();
		    */
		    JFileChooser chooser = new JFileChooser(".");
		    chooser.setSelectedFile( new File("rbnbPlotConfig") );
		    chooser.setDialogTitle("Save Configuration");
		    int returnVal = chooser.showSaveDialog(frame);
		    if (returnVal != JFileChooser.APPROVE_OPTION) {
			throw new Exception("File not selected");
		    }
		    String fileName = chooser.getSelectedFile().getAbsolutePath();
		    System.err.println("Save config to file " + fileName);
		    FileOutputStream fos = new FileOutputStream(fileName);
		    //ObjectOutputStream oos=new ObjectOutputStream(fos);
		    //oos.writeObject(config);
		    //oos.flush();
		    //oos.close();
		    config.store(fos,"rbnbPlot Configuration File");
		    fos.close();
		} catch (Exception fe) {
			System.err.println("Exception, configuration not saved.");
			fe.printStackTrace();
		}
                /*
		loc.set(LayoutCubby.SaveConfig);
		cc.setHash(config);
		//get list of already used channel names
		Channel[] chans=cc.getChannels();
		//pop dialog box
		ConfigDialog cd = new ConfigDialog(true,frame,chans);
		cd.show();
		cc.setChannel(cd.configName);
		cd.dispose();
                */
	    }
	    else if (arg.equals("Load Config")) {
		//EMF 3/22/04: load from file, not RBNB channel
		/*
		loc.set(LayoutCubby.LoadConfig);
		Channel[] chans=cc.getChannels();
		ConfigDialog cd = new ConfigDialog(false,frame,chans);
		cd.show();
		cc.setChannel(cd.configName);
		cd.dispose();
		Hashtable config=cc.getHash();
		*/
		Properties config=new Properties();
		try {
		    // JPW 04/07/2005: Convert to Swing
		    /*
		    FileDialog fd=new FileDialog(frame,"Load Configuration",FileDialog.LOAD);
		    fd.setDirectory(".");
		    fd.setFile("rbnbPlotConfig");
		    fd.show();
		    String fileName=fd.getDirectory()+fd.getFile();
		    */
		    JFileChooser chooser = new JFileChooser(".");
		    chooser.setSelectedFile( new File("rbnbPlotConfig") );
		    chooser.setDialogTitle("Load Configuration");
		    int returnVal = chooser.showOpenDialog(frame);
		    if (returnVal != JFileChooser.APPROVE_OPTION) {
			throw new Exception("File not selected");
		    }
		    File loadFile = chooser.getSelectedFile();
		    if (!loadFile.exists()) {
			throw new Exception("Specified config file does not exist.");
		    }
		    String fileName = loadFile.getAbsolutePath();
		    System.err.println("Load config from file " + fileName);
		    FileInputStream fis=new FileInputStream(fileName);
		    //ObjectInputStream ois=new ObjectInputStream(fis);
		    //config=(Hashtable)ois.readObject();
		    config.load(fis);
		} catch (Exception fe) {
			System.err.println("Exception, configuration not loaded.");
			fe.printStackTrace();
			return;
		}
		loc.set(LayoutCubby.LoadConfig);
		cc.setHash(config);
		cc.getChannel(); //waits for RBNBInterface to finish its wor
		//EMF 3/22/04: end new code
		if (config==null) {
		    String[] aboutInfo = new String[2];
		    aboutInfo[0]=new String("Error reading configuration file.");
		    aboutInfo[1]=new String("Load aborted.");
		    JInfoDialog id = new JInfoDialog(frame,true,"Error",aboutInfo);
		    id.show();
		    id.dispose();
		    //ErrorDialog ed=new ErrorDialog(frame,"Error reading configuration file.","Load aborted.");
		    //ed.show();
		}
		else {
		    if (config.containsKey("mode") && Integer.parseInt((String)config.get("mode"))==LayoutCubby.PlotMode) {
			plot.setSelected(true);
			// plot.setState(true);
			// table.setState(false);
		    }
		    else {
			table.setSelected(true);
			// plot.setState(false);
			// table.setState(true);
		    }
		    pc.setConfig(config);
		    pc.setDisplayMode(Integer.parseInt((String)config.get("mode")));
		    pc.setDisplayGroup(Integer.parseInt((String)config.get("dg.current")));
		}
	    }
	    // EMF 9/8/99: added Export
	    else if (arg.equals("Export to Clipboard")) {
		loc.set(LayoutCubby.ExportToCB);
	    }
	    // JPW 4/19/2000: added "Export to DataTurbine" feature
	    else if (arg.equals("Export to DataTurbine")) {
		loc.set(LayoutCubby.ExportToDT);
	    }
	    // EMF 9/4/07: added "Export to Matlab" feature
	    else if (arg.equals("Export to Matlab")) {
		loc.set(LayoutCubby.ExportToMatlab);
	    }
	    else if (arg.equals("Print")) {
		printScreen();
	    }
	    else if (arg.equals("Open RBNB")) {
		HostAndPortDialog hapd =
		    new HostAndPortDialog(
		        frame, true, "RBNB", "Specify RBNB Connection",
			environment.HOST,environment.PORT,applicationRun);
		hapd.show();
		if (hapd.state == HostAndPortDialog.OK) {
		    // JPW 04/05/2005: Remove View menu
		    // enableViewMenu();
		    environment.HOST=new String(hapd.machine);
		    environment.PORT=hapd.port;
		    loc.set(LayoutCubby.OpenRBNB);
		    frame.setCursor(wait);
		    frame.setTitle(
		        "rbnbPlot " +
			Environment.VERSION +
			" (connecting to " +
			environment.HOST+":" +
			environment.PORT+"...)");
		    //start thread to listen for response to open request
		    if (runner==null || !runner.isAlive()) {
			runner=new Thread(this);
			runner.start();
		    }
		}
		hapd.dispose();
	    }
	    else if (arg.equals("Refresh")) {
		loc.set(LayoutCubby.RefreshRBNB);
	    }
	    else if (arg.equals("Close RBNB")) {
		loc.set(LayoutCubby.CloseRBNB);
		frame.setTitle("rbnbPlot (no connection)");
	    }
	    else if (arg.equals("About")) {
		System.err.println("rbnbPlot by Creare, version "+Environment.VERSION);
		String[] aboutInfo=new String[3];
//# ifdef SERIALVERSION
//+			aboutInfo[0]=new String("rbnbPlot by Creare, " + SERIALVERSION);
//# else
		aboutInfo[0]=new String("rbnbPlot by Creare, Development Version");
//# endif
		aboutInfo[1]=new String("Copyright 1998-2005 Creare, Inc.");
		aboutInfo[2]=new String("All Rights Reserved");
		// This will create a modal dialog; no need to call
		// anything other than the constructor.
		JInfoDialog id = new JInfoDialog(frame, true, "About",aboutInfo);
	    }
	    else if (arg.equals("OnLine Documentation")) { //disabled for now...
		if (applicationRun) {
		    Runtime rt = Runtime.getRuntime();
		    try {
			Process p = rt.exec("C:\\u\\SDP\\Product\\RBNB\\V1.0\\browser.bat http://outlet.creare.com/rbnb");
			//Process p = rt.exec("C:\Program Files\Netscape\Communicator\Program\netscape.exe http://outlet.creare.com/rbnb");
		    }
		    catch (IOException ioe) {
			System.err.println("cannot create process!");
			ioe.printStackTrace();
		    }
		}
	    }
	}
    }
   
    private void createFrame() {
	loc = new LayoutCubby();
	RBNBCubby rbc = new RBNBCubby();
	PosDurCubby pdc = new PosDurCubby();
	rmc = new RunModeCubby(environment.STATICMODE);
	cc = new ConfigCubby();
	
        //EMF 4/10/07: use Environment fonts so size can be changed
	//setFont(new Font("Dialog", Font.PLAIN, 12));
	setFont(Environment.FONT12);
	
	// JPW 04/12/2005: Set the look and feel
	try {
	   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    // Do nothing
	}
	
	JMenuBar mb = new JMenuBar();
        //EMF 4/10/07: use Environment fonts so size can be changed
	//mb.setFont(new Font("Dialog",Font.PLAIN,12));
	mb.setFont(Environment.FONT12);
	JMenu file = new JMenu("File");
	file.setFont(Environment.FONT12);
	JMenuItem open = new JMenuItem("Open RBNB");
	open.setFont(Environment.FONT12);
	open.addActionListener(this);
	file.add(open);
	JMenuItem close = new JMenuItem("Close RBNB");
	close.setFont(Environment.FONT12);
	close.addActionListener(this);
	file.add(close);
	file.addSeparator();
	JMenuItem load = new JMenuItem("Load Config");
	load.setFont(Environment.FONT12);
        //EMF 5/18/01: disable
	//EMF 3/22/04: enable, but load config from file
	load.setEnabled(true); //load config not implemented
	load.addActionListener(this);
	file.add(load);
	JMenuItem save = new JMenuItem("Save Config");
	save.setFont(Environment.FONT12);
        //EMF 5/18/01: disable
	//EMF 3/22/04: enable again, but save config to file
	save.setEnabled(true); //save config not implemented
	save.addActionListener(this);
	file.add(save);
	file.addSeparator();
	// EMF 9/8/99: added Export
	JMenuItem export = new JMenuItem("Export to Clipboard");
	export.setFont(Environment.FONT12);
        //EMF 5/18/01: disable
        export.setEnabled(false);
	export.addActionListener(this);
	file.add(export);
	// JPW 4/19/2000: added "Export to DataTurbine"
	JMenuItem copyToDT = new JMenuItem("Export to DataTurbine");
	copyToDT.setFont(Environment.FONT12);
        //EMF 5/18/01: disable
		//EMF 2/2/05: re-enable
        copyToDT.setEnabled(true);
	copyToDT.addActionListener(this);
	file.add(copyToDT);
	// EMF 9/8/99: added Export
	JMenuItem exportMatlab = new JMenuItem("Export to Matlab");
	exportMatlab.setFont(Environment.FONT12);
        //EMF 5/18/01: disable
        exportMatlab.setEnabled(true);
	exportMatlab.addActionListener(this);
	file.add(exportMatlab);
	JMenuItem print = new JMenuItem("Print");
	print.setFont(Environment.FONT12);
	// EMF 4/28/99 //try 1.2 printing 2/27/02
	print.setEnabled(true); //printing broken until JDK1.1.7 is released
	print.addActionListener(this);
	file.add(print);
	file.addSeparator();
	JMenuItem exit = new JMenuItem("Exit");
	exit.setFont(Environment.FONT12);
	exit.addActionListener(this);
	file.add(exit);
	JMenu mode = new JMenu("Mode");
	mode.setFont(Environment.FONT12);
	/*
	 * JPW 04/08/2005: Use JRadioButtonMenuItem instead of check boxes
	 *
	ItemListener mil = new ModeItemListener(mode,loc);
	plot = new JCheckBoxMenuItem("Plot",true);
	plot.setFont(Environment.FONT12);
	plot.addItemListener(mil);
	mode.add(plot);
	table = new JCheckBoxMenuItem("Table",false);
	table.setFont(Environment.FONT12);
	table.addItemListener(mil);
	mode.add(table);
	*
	*/
	plot = new JRadioButtonMenuItem("Plot");
	plot.setFont(Environment.FONT12);
	plot.setSelected(true);
	table = new JRadioButtonMenuItem("Table");
	table.setFont(Environment.FONT12);
	// Add radio buttons to a group
	ButtonGroup group = new ButtonGroup();
	group.add(plot);
	group.add(table);
	// Add radio buttons to the menu
	mode.add(plot);
	mode.add(table);
	// Add action listener
	plot.addActionListener(this);
	table.addActionListener(this);
	
        //EMF 1/26/01: added slave mode to menu
		//EMF 8/24/05: remove
        //ItemListener sil=new SlaveItemListener(mode,environment);
        //slave = new JCheckBoxMenuItem("Slave",false);
        //slave.addItemListener(sil);
        //mode.addSeparator();
        //mode.add(slave);
	
	// JPW 09/16/2005: New Window menu: "Cascade", "Tile", and "Manual"
	JMenu window=new JMenu("Window");
	window.setFont(Environment.FONT12);
	/*
	//EMF 8/19/05: add Window menu, to control layout of plots
	JMenuItem cascade=new JMenuItem("Cascade");
	cascade.setFont(Environment.FONT12);
	cascade.addActionListener(this);
	JMenuItem tile=new JMenuItem("Tile");
	tile.setFont(Environment.FONT12);
	tile.addActionListener(this);
	window.add(cascade);
	window.add(tile);
	window.addSeparator();
	auto=new JCheckBoxMenuItem("Auto");
	auto.setFont(Environment.FONT12);
	auto.setState(true);
	auto.addActionListener(this);
	window.add(auto);
	*/
	rbCascade = new JRadioButtonMenuItem("Cascade");
	rbCascade.setFont(Environment.FONT12);
	rbTile = new JRadioButtonMenuItem("Tile", true);
	rbTile.setFont(Environment.FONT12);
	rbManual = new JRadioButtonMenuItem("Manual");
	rbManual.setFont(Environment.FONT12);
	// Add radio buttons to a group
	ButtonGroup windowgroup = new ButtonGroup();
	windowgroup.add(rbCascade);
	windowgroup.add(rbTile);
	windowgroup.add(rbManual);
	// Add radio buttons to the menu
	window.add(rbCascade);
	window.add(rbTile);
	window.add(rbManual);
	// Add action listener
	rbCascade.addActionListener(this);
	rbTile.addActionListener(this);
	rbManual.addActionListener(this);
	
	/*
	 * JPW 04/05/2005: Remove View menu
	 *
	// EMF 5/21/1999 : added channel menu with standard and metrics channels
	Menu view=new Menu("View");
	ItemListener vil = new ViewItemListener(view,loc);
	if (environment.REQUESTFILTER.equals(Connection.StandardChannels)) {
	    standard = new CheckboxMenuItem("Standard",true);
	}
	else standard = new CheckboxMenuItem("Standard",false);
	standard.addItemListener(vil);
	view.add(standard);
	if (environment.REQUESTFILTER.equals(Connection.MetricsChannels)) {
	    metrics = new CheckboxMenuItem("Metrics",true);
	}
	else metrics = new CheckboxMenuItem("Metrics",false);
	metrics.addItemListener(vil);
	view.add(metrics);
	if (environment.REQUESTFILTER.equals(Connection.AllChannels)) {
	    all = new CheckboxMenuItem("All",true);
	}
	else all = new CheckboxMenuItem("All",false);
	all.addItemListener(vil);
	view.add(all);
	// EMF 9/8/99: moved Refresh from File to View menu
	view.addSeparator();
	MenuItem refresh = new MenuItem("Refresh");
	refresh.addActionListener(this);
	view.add(refresh);
	*
	*
	*/
	//
	JMenu help = new JMenu("Help");
	help.setFont(Environment.FONT12);
	JMenuItem about = new JMenuItem("About");
	about.setFont(Environment.FONT12);
	about.addActionListener(this);
	help.add(about);
	//MenuItem onlineHelp = new MenuItem("OnLine Documentation");
	//onlineHelp.addActionListener(this);
	//help.add(onlineHelp);
	mb.add(file);
	mb.add(mode);
	//EMF 8/19/05: add Window
	mb.add(window);
	// JPW 04/05/2005: Remove View menu
	// mb.add(view);
	mb.add(help);
	// JPW 04/07/2005: setHelpMenu is not yet implemented in Swing
	// mb.setHelpMenu(help);
	
	frame = new LWFrame("rbnbPlot");
	if (environment.HOST==null) frame.setTitle("rbnbPlot (no connection)");
	else {
	    frame.setTitle("rbnbPlot (connecting to "+environment.HOST+":"+
			   environment.PORT+"...)");
	    //start thread to listen for response to open request
	    if (runner==null || !runner.isAlive()) {
		runner=new Thread(this);
		runner.start();
	    }
	}
	frame.addNotify();
	frame.setLocation(getFrameLocation());
	frame.setSize(getFrameSize());
	
	// Handle the close operation in the windowClosing() method of the
	// registered WindowListener object.  This will get around
	// JFrame's default behavior of automatically hiding the window when
	// the user clicks on the '[x]' button.
	frame.setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	
	frame.addWindowListener(new CloseClass());
	frame.setJMenuBar(mb);
	frame.getContentPane().setLayout(new BorderLayout());
	
	pc = new PlotsContainer(rbc,pdc,environment);
	rbnbInterface=new RBNBInterface(this,pc,rmc,loc,rbc,pdc,cc,environment);
	uc = new UserControl(frame,rmc,loc,rbc,pdc,environment);
	Thread rbnbThread = new Thread(rbnbInterface,"rbnbThread");
	rbnbThread.start();
	
	frame.getContentPane().add(uc,BorderLayout.NORTH);
	frame.getContentPane().add(pc,BorderLayout.CENTER);
	frame.validate();
	frame.show();
    }
	
// disableViewMenu method - called by RBNBInterface when connected to older RBNB
    // JPW 04/05/2005: Remove View menu
    /*
    public void disableViewMenu() {
	synchronized(this) {
	    standard.setState(false);
	    standard.setEnabled(false);
	    metrics.setState(false);
	    metrics.setEnabled(false);
	    all.setState(true);
	    environment.REQUESTFILTER=Connection.AllChannels;
	}
    }
    */

// enableViewMenu method - called when opening new RBNB connection
    // JPW 04/05/2005: Remove View menu
    /*
    public void enableViewMenu() {
	synchronized(this) {
	    standard.setEnabled(true);
	    metrics.setEnabled(true);
	}
    }
    */
    
// destroy method - called by browser on shutdown, by this on windowclose
    public void destroy() {

	if (rmc != null) {
	    rmc.set(RunModeDefs.quit,true);
	    rmc = null;
	}
	if (uc!=null) {
	    uc.clearChannelDialog();
	    uc=null;
	}
	if (frame != null) {
	    frame.setVisible(false);
	    // JPW/EMF 5/4/2001:
	    // This call hangs up for some reason; take it out
	    // frame.dispose();
	    frame = null;
	}

    }
    
    public void quitApp() {
	
        destroy();
	
	if (applicationRun) {
	    //delay to be sure RBNB has disconnected
	    try {
		Thread.sleep(2000);
	    }
	    catch (InterruptedException e) {
		System.err.println("thread sleep error: "+e);
	    }
	    RBNBProcess.exit(0,target);
	}
	else { //set cursor back to hand
	    setCursor(hand);
	}
    }
    
//printScreen method - creates new graphic, redraws everything, and dumps to printer
    public  void printScreen() {
/*
	Toolkit tk=Toolkit.getDefaultToolkit();
	String pjTitle = new String("rbnbPlot print job");
	PrintJob pj=tk.getPrintJob(frame,pjTitle,new Properties());
	if (pj!=null) {
	    Graphics pg=pj.getGraphics();
	    if (pg!=null) {
		frame.printAll(pg);
		pg.dispose();
	    }
	    else System.err.println("RBNBPlot.printScreen: print graphics is null!");
	    pj.end();
	}
	else System.err.println("RBNBPlot.printScreen: print job is null");
*/
        PrinterJob pj=PrinterJob.getPrinterJob();
        //PageFormat pf=pj.defaultPage();
        //pj.setPrintable(this,pf);
        pj.setPrintable(this);
        if (pj.printDialog()) {
          try {
            pj.print();
          } catch (Exception e) {
            System.err.println("Printing exception "+e.getMessage());
            e.printStackTrace();
          }
        }
    }

//EMF 2/27/01: print method for Printable interface
public int print(Graphics g, PageFormat format, int pageNumber) {
  if (pageNumber!=0) return Printable.NO_SUCH_PAGE;
//System.err.println("format: "+format.getImageableX()+" "+format.getImageableY());
//System.err.println("        "+format.getImageableWidth()+" "+format.getImageableHeight());
//System.err.println("        "+format.getWidth()+" "+format.getHeight());
  g.translate((int)format.getImageableX(),(int)format.getImageableY());
  Dimension frameSize=frame.getSize();
  //preserve aspect ratio
  double frameAspect=frameSize.getWidth()/frameSize.getHeight();
  double pageAspect=format.getImageableWidth()/format.getImageableHeight();
  if (frameAspect>pageAspect) {
    frame.setSize((int)format.getImageableWidth(),(int)(format.getImageableWidth()/frameAspect));
  } else {
    frame.setSize((int)(format.getImageableHeight()*frameAspect),(int)format.getImageableHeight());
  }
    
  //frame.setSize((int)format.getImageableWidth(),(int)format.getImageableHeight());
  frame.invalidate();
  frame.validate();
  frame.repaint();
  frame.printAll(g);
  frame.setSize(frameSize);
  frame.invalidate();
  frame.validate();
  frame.repaint();
  return Printable.PAGE_EXISTS;
}
    
//run method - started when waiting for a new connection to be established, dies
//when connection succeeds or fails
    public void run() {
	int count=0;
	Boolean status = null;
	while ((status=loc.getStatus())==null) {
	    if (count++ > 600) {
		status=new Boolean(false);
		break;
	    }
	    try {
		Thread.sleep(200);		// slow down, let channel list get refreshed at start
	    }
	    catch (InterruptedException ie) {}
	}
	if (status.booleanValue()==true) {
	    frame.setTitle("rbnbPlot (connected to "+environment.HOST+":"+
			   environment.PORT+")");
	    //EMF 9/7/05: pop channel dialog
		if (uc==null) try { Thread.currentThread().sleep(1000); } catch (Exception e) {}
	    uc.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"Channels"));
	}
	else {
	    String[] aboutInfo=new String[2];
	    aboutInfo[0]=new String("Failed to connect to");
	    aboutInfo[1]=new String(environment.HOST+":"+environment.PORT);
	    JInfoDialog id=new JInfoDialog(frame,true,"Error",aboutInfo);
	    //id.show();
	    //id.dispose();
	    frame.setTitle("rbnbPlot (no connection)");
	}
	frame.setCursor(pointer);
    }

/*
 *
 * JPW 04/08/2005: No longer use ItemListener; I changed the menu items from
 *                 JCheckBoxMenuItem to JRadioButtonMenuItem and I use
 *                 and ActionListener.
 *
 *
// ModeItemListener class catches Mode Menu events, implements mutually
// exclusive selection of mode options
    class ModeItemListener implements ItemListener {
	JMenu mode = null;
	LayoutCubby loc = null;
	
	ModeItemListener(JMenu m, LayoutCubby l) {
	    mode=m;
	    loc=l;
	}
	
	public void itemStateChanged(ItemEvent e) {
	    if (e.getSource() instanceof JCheckBoxMenuItem) {
		JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
		for (int i=0;i<2;i++) {
		    JCheckBoxMenuItem item=(JCheckBoxMenuItem)mode.getItem(i);
		    if (cbmi==item) {
			item.setState(true);
			// if (item.getLabel().equals("Plot")) {
			if (item.getText().equals("Plot")) {
			    loc.set(LayoutCubby.PlotMode);
			}
			// else if (item.getLabel().equals("Table")) {
			else if (item.getText().equals("Table")) {
			    loc.set(LayoutCubby.TableMode);
			}
		    }
		    else item.setState(false);
		}
	    }
	}
    }
*
*
*/

// EMF 1/26/01 added callback
// SlaveItemListener class catches Slave Mode menu events, toggles state
  class SlaveItemListener implements ItemListener {
    JMenu mode=null;
    Environment env=null;
    boolean streamingStart=false;

    SlaveItemListener(JMenu m, Environment e) {
      mode=m;
      env=e;
      streamingStart=env.STREAMING;
    }

    public void itemStateChanged(ItemEvent e) {
      JCheckBoxMenuItem cbmi= (JCheckBoxMenuItem)e.getSource();
      env.SLAVEMODE=cbmi.getState();
      if (env.SLAVEMODE) {
        env.STREAMING=true;
        rmc.set(RunModeDefs.realTime,true);
      }
      else env.STREAMING=streamingStart;
    }
  }
    
// ViewItemListener class catches View Menu events, sets appropriate
//  channel filter constant in environment class, then forces a RBNB Refresh
    /*
     * JPW 04/05/2005: Remove View menu
     *
    class ViewItemListener implements ItemListener {
	Menu view = null;
	LayoutCubby layoutCubby=null;
	
	ViewItemListener(Menu v,LayoutCubby loc) {
	    view=v;
	    layoutCubby=loc;
	}
	
	public void itemStateChanged(ItemEvent e) {		
	    if (e.getSource() instanceof CheckboxMenuItem) {
		CheckboxMenuItem cbmi = (CheckboxMenuItem)e.getSource();
		int count=view.getItemCount();
		for (int i=0;i<count;i++) {
		    if (view.getItem(i) instanceof CheckboxMenuItem) {
			CheckboxMenuItem item=(CheckboxMenuItem)view.getItem(i);
			if (item==cbmi) {
			    if (item.getState()==false) { //ignore if user turned off item that was on
				item.setState(true);
				return;
			    }
			    else if (item.getLabel().equals("Standard")) {
				environment.REQUESTFILTER=Connection.StandardChannels;
			    }
			    else if (item.getLabel().equals("Metrics")) {
				environment.REQUESTFILTER=Connection.MetricsChannels;
			    }
			    else if (item.getLabel().equals("All")) {
				environment.REQUESTFILTER=Connection.AllChannels;
			    }
			}
			else item.setState(false);
		    }
		}
		loc.set(LayoutCubby.RefreshRBNB);	//force update of channels
	    }
	}
    }
    *
    *
    */
		
			
// CloseClass class catches window events, allows user to close window and
// terminate applet/application
    class CloseClass extends WindowAdapter {
	public void windowClosing(WindowEvent evt) {
	    //System.err.println("shutting down RBNBPlot");
	    destroy();
	    quitApp();
	}
    }
    
// LWFrame class provides lightweight frame for graphical output
    class LWFrame extends JFrame {
	public LWFrame(String title) {
	    super(title);
		//EMF 8/23/05: add icon
		java.net.URL url=this.getClass().getResource("/images/whirligig.gif");
		if (url!=null) setIconImage(new javax.swing.ImageIcon(url).getImage());
	}
	// update method overridden to avoid screen flashing,
	// requires use of double buffering
	public void update(Graphics g) {
	    paint(g);
	}
    }
    
// start method overridden - want to continue running as user continues
// browsing
    public void start() {
    }

// main method - creates frame, then runs as applet
    public static void main(String[] args) {
	new AppletFrame(new RBNBPlotMain(args), 300, 300, false);
    }

// getFrameSize method - returns desired main frame size in
// screen coordinates
    private Dimension getFrameSize() {
	Toolkit tk = Toolkit.getDefaultToolkit();
	Dimension sz = tk.getScreenSize();
	//System.err.println("RBNBPlot.getFrameSize: Screensize "+sz.width+"   "
	//   +sz.height);
	sz.width /= 2;
	sz.height = sz.height*3/4;
	return(sz);
    }

// getFrameLocation method - returns desired main frame location
// in screen coordinates
    private Point getFrameLocation() {
	if (environment.POSITION_X==-1 || environment.POSITION_Y==-1) {
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    Dimension sz = tk.getScreenSize();
	    return(new Point(sz.width/4,sz.height/8));
	}
	else return(new Point(environment.POSITION_X,environment.POSITION_Y));
    }
    
} // end class RBNBPlot
