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

package com.rbnb.media;

/*
  *****************************************************************
  								
  	Name :	rbnbAVCP.java				
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000, 2001, 2004 Creare Inc.			
  	All Rights Reserved					
  								
  	Description :						
  	   This class provides a simple main program for	
  	viewing video from the DataTurbine. It uses the Video	
  	Retriever class of the API.				
  								
  	Modification History :					
  	   08/18/2004 - INB					
  		Ported to V2.4.4 RBNB.				
  	   12/12/2001 - INB					
  		Added NoClassDefFoundError handling.		
  								
  *****************************************************************
*/

import com.rbnb.media.capture.JPEGCapture;
import com.rbnb.media.protocol.VideoRetriever;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.SAPIException;

import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.RBNBExplorer;
import com.rbnb.utility.RBNBProcess;
import com.rbnb.utility.RBNBProcessInterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;

import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
  * Audio-Video Capture and Playback application for the RBNB network.
  * <p>
  * @since V2.5
  * @author WHF
  * @version 2005/03/21
  */
  
/* Modification History:
	2004/11/30  WHF  Created.
	2005/03/21  WHF  Added support for command-line parameters.
*/
public class rbnbAVCP
    extends JFrame
{
// **************************  Construction  ********************************//
    public rbnbAVCP()
    {
		jpegCapture = new JPEGCapture();
		
		setSize(480,360);
		setResizable(true);
		// this forces the menu popup to be a heavy weight, which 
		//  allows it to paint over the heavyweight canvas.
		javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);		
		setTitle(TITLE_PREFIX + ": not connected");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { doExit(); }
		    public void windowIconified(WindowEvent we) { retriever.pause(); }
		    public void windowOpened(WindowEvent we) { firstTimeOpen(); }
		});
		
		setIconImage(new ImageIcon(getClass().getClassLoader()
			.getResource(IMAGEPATH+"RBNBIcon.GIF")).getImage());

		createDisplayWindow();
		// This centers the window on-screen:
		setLocationRelativeTo(null);
    }
	
// **************************  Private Methods  *****************************//
	/**
	  * Starts video/audio capture.
	  *<p>
	  * @author WHF
	  * @version 2004/12/02
	  */
	private void doCapture()
	{
		// Modal, this blocks until done:
		captureOptionsDlg.setVisible(true);
		if (captureOptionsDlg.wasOkHit()) {
			startCapture();
		}
	}
	private void doStop()
	{
		try {
			jpegCapture.stop();
			//jcStop.invoke(jpegCapture, null);
			stopCaptureMenuItem.setEnabled(false);
			captureMenuItem.setEnabled(true);
			captureStatus = DEFAULT_STATUS;
			updateStatusbar();
		/*} catch (IllegalAccessException iae) {
			showException(
					this,
					iae,
					"Error stopping capture:",
					"The following error occurred while stopping the "
					+"capture process:"
			);
		} catch (java.lang.reflect.InvocationTargetException ite) {
			// This exception wraps an exception which occurred inside the
			//  invoked method:
			showException(
					this,
					ite.getCause(),
					"Error stopping capture:",
					"The following error occurred while stopping the "
					+"capture process:"
			); */
		} catch (Exception e) {
			showException(
					this,
					e,
					"Error stopping capture:",
					"The following error occurred while stopping the "
					+"capture process:"
			);
		}
	}
	
	/**
	  * Select channels for viewing.
	  */
	// New, dialog approach:
	private void doChannels()
	{
		viewOptionsDlg.setVisible(true);
		
		if (viewOptionsDlg.wasOkHit()) {
			updateChannels();
		}
	}	


/*
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
    } */

/*
  *****************************************************************
  								
  	Name :	doExit					
  	By   :	Eric Friets	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	December, 2000					
  								
  	Copyright 2000 Creare Inc.				
  	All Rights Reserved					
  								
  	Description :						
  	   This method quits the application.  Separated from   
  	   callback code so can be stopped when running as      
        PlugIn.                                              
     2004/11/24  WHF  Made private.  JFrame has methods to close.
     2004/12/02  WHF  Also stops capture.
  								
  *****************************************************************
*/
    private void doExit()
	{
		setVisible(false);
		doClose(); // close display
		doStop();  // stop capture
		RBNBProcess.exit(0, target);
    }

/*
  *****************************************************************
  								
  	Name :	closeConnection					
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000, 2004 Creare Inc.			
  	All Rights Reserved					
  								
  	Description :						
  	   This method closes any active connection.		
  								
  	Modification History :					
  	   08/18/2004 - INB					
  		Ported to V2.4.4 RBNB.				
  								
  *****************************************************************
*/
    private void doClose() 
	{
		// Stop the video retriever.
		stopRetriever();
	
		// Close the connection.
		// Now done under disconnect:
		//rbnbCon.CloseRBNBConnection();
		
		closeMenuItem.setEnabled(false);
		viewMenuItem.setEnabled(true);
		displayStatus = DEFAULT_STATUS;
		updateStatusbar();
	
		// Clear the current map.
		rbnbMap = null;
    }
	
	/**
	  * Called when the user presses File / Connect.
	  *<p>
	  * @author WHF
	  * @since V2.4.5
	  * @version 2004/12/02
	  */
	private void doConnect()
	{
		try {
			serverAddress = (String) JOptionPane.showInputDialog(
					this,
					"DataTurbine Server Address (host:port)",
					"Open Connection to DataTurbine Server",
					JOptionPane.QUESTION_MESSAGE,
					null,
					null,
					lastServer
			);
			if (serverAddress != null) {
				// Will stop display:
				openConnection();
			}
		} catch (Exception e) {
			serverAddress = lastServer;
			showException(
					this,
					e,
					"Open DataTurbine Server Error",
					"The following error occurred while connecting to the RBNB"
					+" server:"
			);
		}

	}
	
	private void doDisconnect()
	{
		doStop();
		doClose();		
		rbnbCon.CloseRBNBConnection();
		
		setTitle(TITLE_PREFIX + ": not connected");

		connectMenuItem.setEnabled(true);
		disconnectMenuItem.setEnabled(false);

		viewMenuItem.setEnabled(false);
		captureMenuItem.setEnabled(false);		
	}


/*
  *****************************************************************
  								
  	Name :	createDisplayWindow				
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000 Creare Inc.				
  	All Rights Reserved					
  								
  	Description :						
  	   This method builds the display window for the video	
  	retriever.						
  								
  *****************************************************************
*/
    private void createDisplayWindow() 
	{
		createMenuBar();
		// Create the status-bar:
		statusbar = new JTextField();
		statusbar.setEditable(false);
		final int HGAP = 0, VGAP = 10;
		getContentPane().setLayout(new BorderLayout(HGAP, VGAP));
		getContentPane().add(statusbar, BorderLayout.SOUTH);
		
		updateStatusbar();
    }
	
	/**
	  * Writes informative text in the area at the bottom of the control.
	  *
	  * @author WHF
	  * @since V2.4.5
	  */
	private void updateStatusbar()
	{
		statusbar.setText(
				"Capture: "+captureStatus
				+"      Display: "+displayStatus);			
	}

/*
  *****************************************************************
  								
  	Name :	createMenuBar					
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000 Creare Inc.				
  	All Rights Reserved					
  								
  	Description :						
  	   This routine creates the menu bar for the video	
  	viewer. The menu bar has a File pulldown.		
  								
  *****************************************************************
*/
    private  void createMenuBar() 
	{
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
	
		// Create the file menu.
		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');
		menuBar.add(menu);
	
		// Create the action buttons.
		connectMenuItem = menu.add(createMenuItem("Connect...", 'o',
				new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doConnect(); }
		}));
		(disconnectMenuItem = menu.add(createMenuItem("Disconnect...", 'D',
				new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doDisconnect(); }
		}))).setEnabled(false);
		menu.addSeparator();
		// The other menu items are disabled until we are connected to a
		//  server:
		(viewMenuItem = menu.add(createMenuItem("View...", 'V',
				new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doChannels(); }
		}))).setEnabled(false);
		(closeMenuItem = menu.add(createMenuItem("Close", 'C', 
				new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doClose(); }
		}))).setEnabled(false);
		menu.addSeparator();
		(captureMenuItem = menu.add(createMenuItem("Capture...", 'a',
				new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doCapture(); }
		}))).setEnabled(false);
		(stopCaptureMenuItem = menu.add(createMenuItem("Stop Capture", 'S',
				new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doStop(); }
		}))).setEnabled(false);
		menu.addSeparator();
		menu.add(createMenuItem("Exit", 'x', new ActionListener() {
			public void actionPerformed(ActionEvent ae) { doExit(); }
		}));
	
		setJMenuBar(menuBar);
    }
	
	/**
	  * Support method for createMenuBar().
	  * @version 2004/11/24
	  */
	private JMenuItem createMenuItem(String label, char mnemonic, 
			ActionListener al)
	{
		JMenuItem jmi = new JMenuItem(label);
		jmi.setMnemonic(mnemonic);
		jmi.addActionListener(al);
		
		return jmi;
	}
/*
  *****************************************************************
  								
  	Name :	openConnection					
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000, 2004 Creare Inc.			
  	All Rights Reserved					
  								
  	Description :						
  	   This method opens a connection to the specified	
  	DataTurbine address.					
  								
  	Input :							
  	   addressI		The DataTurbine address.	
  								
  	Modification History :					
  	   08/18/2004 - INB  Ported to V2.4.4 RBNB.
	   2005/03/22 - WHF  Removed argument for a more property-style approach.
  								
  *****************************************************************
*/
    private void openConnection() throws SAPIException
	{
		// Close any existing connection.
		doClose();
		// Stop capture:
		doStop();
	
		// Open a new connection to the server.
		rbnbCon.OpenRBNBConnection(serverAddress, this.getClass().getName());
		lastServer = serverAddress;
		
		setTitle(TITLE_PREFIX + ": connected to "+serverAddress);
	
		retriever.serverAddress = serverAddress;
		retriever.setConnection(rbnbCon);
				
		connectMenuItem.setEnabled(false);
		disconnectMenuItem.setEnabled(true);		
    }

/*
  *****************************************************************
  								
  	Name :	parseCommandLine				
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	2005/03/21					
  								
  	Copyright 2005 Creare Inc.				
  	All Rights Reserved					
  								  								
  	Input :							
  	   argsI		The command line arguments.	
  								
  	Modification History :					
		08/18/2004 - INB	 Ported to V2.4.4 RBNB.				
		2004/11/24  WHF  Made public so could be called by external
				applications.
  								
  *****************************************************************
*/
	/**
	  * Sets properties for this rbnbAVCP object based on command line
	  *  strings.  The properties supported are:
	  * <dl>
	  * <dt>-a [address]</dt><dd>RBNB to connect to.  Connects if specified.  If -view or -capture is specified, and -a is not, tries localhost:3333.</dd> 
      * <dt>-capture [sourcename]</dt><dd>Will start capture under the specified source name using default parameters, unless overridden with other flags.</dd>
	  * <dt>-cache [size]</dt><dd>Cache size in frames.</dd>
      * <dt>-archive [size]</dt><dd>Archive size in frames.</dd>
      * <dt>-device [number]</dt><dd>Capture video under the specified zero based device number.</dd>
      * <dt>-format [string]</dt><dd>Specify video capture format.</dd>
      * <dt>-resolution [widthxheight]</dt><dd>Specify image resolution.</dd>
      * <dt>-frate [Hz]</dt><dd>Frames per second video.</dd>
      * <dt>-audio &lt;number&gt;</dt><dd>Capture audio.  Optional device number (default 0).</dd>
      * <dt>-srate [Hz]</dt><dd>Sample rate for audio capture.</dd>
      * <dt>-bits [number]</dt><dd>Bits per sample for audio.</dd>
      * <dt>-stereo</dt><dd>Specifies stereo capture. </dd>
      * <dt>-view [video chan] &lt;audio chan&gt;</dt><dd>View a video channel and optional audio channel.</dd>
	  * </dl>
*/

    public void parseCommandLine(String[] argsI) throws SAPIException
	{
		for (int ii = 0; ii < argsI.length; ii+=2) {
			String arg = argsI[ii];
			if (arg.charAt(0) == '-') { // argument
				String value;
				if (ii+1 < argsI.length && argsI[ii+1].charAt(0) != '-')
					value = argsI[ii+1];
				else value = null;
				if ("-a".equals(arg)) {
					setHostname(value);
				} else if ("-capture".equals(arg)) {
					setCaptureSource(value);
				} else if ("-cache".equals(arg)) {
					setCache(Integer.parseInt(value));
				} else if ("-archive".equals(arg)) {
					setArchive(Integer.parseInt(value));
				} else if ("-device".equals(arg)) {
					setVideoDevice(Integer.parseInt(value));
				} else if ("-format".equals(arg)) {
					setVideoFormat(value);
				} else if ("-resolution".equals(arg)) {
					int xi = value.indexOf('x');
					setVideoResolution(new Dimension(Integer.parseInt(
							value.substring(0, xi)),
							Integer.parseInt(value.substring(xi+1))
					));
				} else if ("-frate".equals(arg)) {
					setVideoFrameRate(Float.parseFloat(value));
				} else if ("-audio".equals(arg)) {
					setAudioCapture(true);
					if (value != null && value.charAt(0) != '-')
						setAudioDevice(Integer.parseInt(value));
					else --ii; // didn't use value, decrement ii
				} else if ("-srate".equals(arg)) {
					setAudioSampleRate(Double.parseDouble(value));
				} else if ("-bits".equals(arg)) {
					setAudioBits(Integer.parseInt(value));
				} else if ("-stereo".equals(arg)) {
					setStereo(true);
					--ii; // didn't use value, decrement ii
				} else if ("-view".equals(arg)) {
					setViewVideo(value);
					if (ii+2 < argsI.length && argsI[ii+2].charAt(0) != '-') {
						setViewAudio(argsI[ii+2]);
						++ii; // additional argument used
					} else setViewAudio(""); // no audio display
				} else if ("-?".equals(arg) || "-help".equals(arg)) {
					showHelp();
				}else {
					showHelp();
					throw new IllegalArgumentException(
						"Argument not recognized: \""+arg+'"');
				}
			}
		}		
    }
	
	/**
	  * Displays command line argument options
	  */
	public void showHelp()
	{
		System.err.println(
				"-a [address]             RBNB to connect to.  Connects if specified.  If -view or -capture is specified, and -a is not, tries localhost:3333.\n"
				 
				+"\n-capture [sourcename]    Will start capture under the specified source name using default parameters, unless overridden with other flags.\n"
				 
				+"-cache [size]            Cache size in frames.\n"
				+"-archive [size]          Archive size in frames.\n"
				+"-device [number]         Capture video under the specified zero based device number.\n"
				+"-format [string]         Specify video capture format.\n"
				+"-resolution [widthxheight] Specify image resolution.\n"
				+"-frate [Hz]              Frames per second video.\n"
				 
				+"\n-audio <number>          Capture audio.  Optional device number (default 0).\n"
				+"-srate [Hz]              Sample rate for audio capture.\n"
				+"-bits [number]           Bits per sample for audio.\n"
				+"-stereo                  Specifies stereo capture.\n"
				 
				+"\n-view [video chan] <audio chan> View a video channel and optional audio channel.\n"
		); 
	}

	/**
	  * Starts the capture based on set properties.
	  * @author WHF
	  * @since 2005/03/22
	  */
	private void startCapture()
	{	
		// 2005/01/12  WHF  This never seems to work.  May be due to 
		//  presence of non-swing child (retriever)?
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		synchronized (jpegCapture) { // we synch here for firstTimeOpen 
		try {
			jpegCapture.setup(
					serverAddress,
					captureOptionsDlg.getSourceName(),
					captureOptionsDlg.getCache(),
					captureOptionsDlg.getArchive(),
					captureOptionsDlg.getVideoDevice(),
					captureOptionsDlg.getVideoFormat(),
					captureOptionsDlg.getAudioDevice(),
					captureOptionsDlg.getAudioFormat()
			);
			new Thread(new Runnable() {
				public void run() 
				{ 						
					try {
						//jcGo.invoke(jpegCapture, null);
						jpegCapture.go();
						captureStatus = "" + captureOptionsDlg
							.getVideoFormat().getFrameRate()+" fps to "+
							captureOptionsDlg.getSourceName();
						updateStatusbar();
					} catch (Exception e) {
						exceptionToShow = e;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								doStop();
								showException(
										rbnbAVCP.this,
										exceptionToShow,
										"Error during capture:",
										"The following error was detected"
										+" during capture:"
								);
							}
						});
					}
				}
			}, "CaptureThread").start();
			stopCaptureMenuItem.setEnabled(true);
			captureMenuItem.setEnabled(false);
		} catch (Exception e) {
			showException(
					this,
					e,
					"Error starting capture:",
					"The following error occurred while starting the "
					+"capture process:"
			);
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
		} // end synch
	}
	
// ************************  Property Methods  ******************************//
	public void setHostname(String hostname)
	{
		if (hostname == null)
			throw new IllegalArgumentException("Hostname cannot be null.");
		serverAddress = hostname;
		connectOnStart = true;
	}
	public void setCaptureSource(String sourcename)
	{
		if (sourcename == null)
			throw new IllegalArgumentException(
					"Capture requires a non-null source name.");
		captureOptionsDlg.setSourceName(sourcename);
		connectOnStart = true;
		captureOnStart = true;
	}
	public void setArchive(int archive)
	{
		captureOptionsDlg.setArchive(archive);
	}
	public void setCache(int cache)
	{
		captureOptionsDlg.setCache(cache);
	}		
	public void setVideoDevice(int dev)
	{
		captureOptionsDlg.setVideoDeviceByNumber(dev);
	}
	public void setVideoFormat(String format)
	{
		captureOptionsDlg.setVideoFormat(format);
	}
	public void setVideoResolution(Dimension res)
	{
		captureOptionsDlg.setVideoResolution(res);
	}
	public void setVideoFrameRate(float frate)
	{
		captureOptionsDlg.setVideoFrameRate(frate);
	}
	public void setAudioCapture(boolean toCapture)
	{
		captureOptionsDlg.setAudioEnabled(toCapture);
	}
	public void setAudioDevice(int dev)
	{
		captureOptionsDlg.setAudioDeviceByNumber(dev);
	}
	public void setAudioSampleRate(double srate)
	{
		captureOptionsDlg.setAudioSampleRate(srate);
	}
	public void setAudioBits(int bits)
	{
		captureOptionsDlg.setAudioBits(bits);
	}	
	public void setStereo(boolean forStereo)
	{
		captureOptionsDlg.setStereo(forStereo);
	}
	public void setViewVideo(String channel)
	{
		connectOnStart = true;
		viewOnStart = true;
		viewOptionsDlg.setVideoChannel(channel);
	}
	/**
	  * Note that {@link #setViewVideo(String)} must be called for the viewer
	  *  to actually run at startup.
	  */
	public void setViewAudio(String channel)
	{
		viewOptionsDlg.setAudioChannel(channel);
	}


/*
  *****************************************************************
  								
  	Name :	startRetriever					
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000, 2004 Creare Inc.			
  	All Rights Reserved					
  								
  	Description :						
  	   This method starts a video retriever. If necessary,	
  	it creates the retriever for the first time.		
  								
  	Modification History :					
  	   08/18/2004 - INB					
  		Ported to V2.4.4 RBNB.				
  								
  *****************************************************************
*/
    private void startRetriever() throws Exception
	{
		// If things are not fully set up at this point, do nothing.
		if ((rbnbMap == null)) {
			return;
		}

		// Ensure that we have a retriever in the base state.
//		if (retriever != null) {
			retriever.terminate();
//		} else {
//			retriever = new VideoRetriever();
//		}

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
  								
  	Name :	stopRetriever					
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000 Creare Inc.				
  	All Rights Reserved					
  								
  	Description :						
  	   This method stops the video retriever.		
  								
  *****************************************************************
*/
    private  void stopRetriever() 
	{
		// Terminate the retriever thread if it exists.
//		if (retriever != null) {
			retriever.terminate();
			retriever.setConnection(null);
//		}
    }

/*
  *****************************************************************
  								
  	Name :	updateChannels					
  	By   :	Ian Brown	(Creare Inc., Hanover, NH)	
  	For  :	DataTurbine					
  	Date :	September, 2000					
  								
  	Copyright 2000 Creare Inc.				
  	All Rights Reserved					
  								
  	Description :						
  	   This method updates the channel list. If there is an	
  	active retriever, it stops that retriever. If there is	
  	an active connection, it uses that connection to get	
  	a real list of channels for the channel path(s) that	
  	are specified. It then starts the retriever on those	
  	channels.						
  								
  	Modification History :					
		08/18/2004 - INB	 Ported to V2.4.4 RBNB.
		2004/12/03  WHF  Made lastVideo, lastAudio input arguments.
		2005/02/08  WHF  Check type of registration information before
							extracting strings.
		2005/03/23  WHF  Removed lastVideo, lastAudio altogether.  No longer 
				throws.
  								
  *****************************************************************
*/
    private void updateChannels()
	{
		try {
			ChannelMap channelPaths = new ChannelMap();
			
			String videoPath = viewOptionsDlg.getVideoChannel();
			if ((videoPath != null) && !videoPath.equals("")) {
				channelPaths.Add(videoPath);
			}
			String audioPath = viewOptionsDlg.getAudioChannel();
			if ((audioPath != null) && !audioPath.equals("")) {
				channelPaths.Add(audioPath);
			}
			
			// Stop any active retriever.
			stopRetriever();
			
			// Given a connection, request a list of channels matching the current
			// path(s).
			if ((channelPaths != null)) {
				if (/*(retriever != null) &&*/ retriever.streaming) {
					rbnbCon.CloseRBNBConnection();
					rbnbCon.OpenRBNBConnection(serverAddress,"rbnbVidView");
				}
			
				// Get the registration for the requested channels.
				rbnbCon.RequestRegistration(channelPaths);
				rbnbMap = rbnbCon.Fetch(-1);
			
	System.err.println(channelPaths);
			
				// If the map is null, throw an exception.
				if (rbnbMap == null) {
					throw new Exception(
							"Unable to find any channels matching the specified " +
							"channel path.");
				} else {
				// Ensure that we have the right number and type of channels.
				boolean	    audioMatched = (audioPath == null),
						videoMatched = (videoPath == null);
			
				for (int idx = 0; idx < rbnbMap.NumberOfChannels(); ++idx) {
					String content;
					if (rbnbMap.GetType(idx) == ChannelMap.TYPE_STRING) {
						KeyValueHash kvh = new KeyValueHash(
								//rbnbMap.GetDataAsString(idx)[0].getBytes());
								rbnbMap.GetUserInfo(idx).getBytes());
						content = kvh.get("content");
			
						//System.err.println(rbnbMap.GetDataAsString(idx)[0]);
						//System.err.println("User info: "+rbnbMap.GetUserInfo(idx));
						//System.err.println(rbnbMap.GetMime(idx));
					} else content = null;
			
					try {
					if (audioPath.equals(rbnbMap.GetName(idx))) {
						if (audioMatched) {
						throw new Exception
							("Multiple channels match audio " +
							 "specification " + audioPath);
						}
						// 2004/12/30  WHF  Other methods of matching:
						audioMatched = "Audio".equalsIgnoreCase(content)
								|| "audio/basic".equals(rbnbMap.GetMime(idx));
					}
					} catch (Exception e) { //ugh!
					}
			
					try {
					if (videoPath.equals(rbnbMap.GetName(idx))) {
						if (videoMatched) {
						throw new Exception
							("Multiple channels match video "+
							 "specification " + videoPath);
						}
						// 2004/12/30  WHF  Other methods of matching:
						int dotI = videoPath.lastIndexOf('.');
						videoMatched = "Video".equalsIgnoreCase(content)
								|| "image/jpeg".equals(rbnbMap.GetMime(idx))
								|| dotI != -1 
								 && (videoPath.substring(dotI).equalsIgnoreCase(
										".jpg") || videoPath.substring(dotI)
										.equalsIgnoreCase(".jpeg"));
					}
					} catch (Exception e) {
					}
				}
			
				String unmatched = null;
				if (!audioMatched) {
					//		    unmatched = "Audio channel " + audioPath + " not found.";
					audioPath=null;  // mjm just ignore audio and continue
				}
				if (!videoMatched) {
					String vunmatched =
					"Video channel " + videoPath + " not found.";
			
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
		
			closeMenuItem.setEnabled(true);
			viewMenuItem.setEnabled(false);
			displayStatus = viewOptionsDlg.getAudioChannel() != null?
					"Video/Audio playback"
					:"Video from "+viewOptionsDlg.getVideoChannel();
			updateStatusbar();
		} catch (Exception e) {
			showException(
					this,
					e,
					"Select Audio/Video Channels Error",
					"An error occured while processing the channel selection:"
			);
		}
    }

	/**
	  * Note this function should not throw or it ruins the window
	  *  functionality.
	  */
	private void firstTimeOpen()
	{
		if (connectOnStart) {
			// If there is a server, connect to it.
			if (serverAddress == null) serverAddress = "localhost:3333";
			try {
				openConnection();
			} catch (Exception e) {
				showException(
						this,
						e,
						"Open DataTurbine Server Error",
						"Could not connect to the specified server:"
				);
			}
		}
		if (captureOnStart) {
			if (captureOptionsDlg.getVideoDevice() == null)
				setVideoDevice(0);
			if (captureOptionsDlg.getAudioEnabled()) {
				if (captureOptionsDlg.getAudioDevice() == null)
					setAudioDevice(0);
				if (captureOptionsDlg.getAudioFormat() == null)
					setStereo(false);
			}
			startCapture();
			// 2005/03/23  WHF  This gives the capture routines a chance to 
			//   start, in case we're viewing ourselves.  
			//  Really, there should be some kind of lock, but the objects that
			//  this depends on are buried very deeply.
			if (viewOnStart) try { Thread.sleep(5000); } catch (Exception e) {}
		}
		if (viewOnStart)
			updateChannels();
    }

    public static void main(String[] argsI) 
	{
		rbnbAVCP avcp = null;
		try {
			avcp = new rbnbAVCP();			
			avcp.parseCommandLine(argsI);
			avcp.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			if (avcp != null)
				avcp.dispose();
		}
    }
	
	static void showException(Component parent, 
			Throwable e, String title, String msg)
	{
		// Display on console:
		e.printStackTrace();

		try {
			com.rbnb.utility.ExceptionDialog.show(
					parent,
					e,
					title,
					msg
			);
		} catch (Throwable t) { // above didn't work, do backup:
			// Create dialog message by spewing stack trace into a string:
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println(msg);
			e.printStackTrace(pw);
			pw.close();
			
			JOptionPane.showMessageDialog(
					parent,
					sw,
					title,
					JOptionPane.ERROR_MESSAGE
			);
		}
	}

// *************************  Static Constants  *****************************//
	static final int DEFAULT_ARCHIVE = 10000;
	static final int DEFAULT_CACHE = 100;
	/**
	  * The frame rate field in VideoFormat is a float.
	  */
	static final float DEFAULT_FRAME_RATE = 10.0f;
	static final String DEFAULT_SOURCE = "rbnbVideo",
			DEFAULT_VIDEO = "Video.jpg",
			DEFAULT_AUDIO = "Audio";
	private static final String DEFAULT_STATUS = "none",
			TITLE_PREFIX = "RBNB Audio-Video Capture and Display",
			IMAGEPATH="Video/";

// **********************  Private Data  ************************************//
    private boolean live,
			connectOnStart,
			captureOnStart,
			viewOnStart;
    private final Sink rbnbCon = new Sink();
    private int playerVisible = 0;
    private ChannelMap
			rbnbMap = null;
			//channelPaths = null;
    private String
			lastServer = "localhost:3333",
			serverAddress = null,
			captureStatus = DEFAULT_STATUS,
			displayStatus = DEFAULT_STATUS;

	private JTextField statusbar;		
    private final VideoRetriever retriever = new VideoRetriever();
	
    //EMF 12/19/00: target for RBNBProcess.exit
    private RBNBProcessInterface target = null;
	
	private final CaptureOptionsDlg captureOptionsDlg 
			= new CaptureOptionsDlg(this);
			
	private final ViewOptionsDlg viewOptionsDlg = new ViewOptionsDlg(
			this, rbnbCon);
			
	/**
	  * Declared as Object because it is in the unnamed package and therefore
	  *  cannot be compiled against!
	  */
	//private final Object jpegCapture; //  = new JPEGCapture();
	//private final Method jcGo, jcStop, jcSetup;
	private final JPEGCapture jpegCapture;
	private JMenuItem captureMenuItem, closeMenuItem, connectMenuItem,
			disconnectMenuItem, stopCaptureMenuItem,
			viewMenuItem;
	/**
	  * Stores an exception for later display in the main GUI thread.
	  */
	private Exception exceptionToShow;

} // end class rbnbAVCP

class CaptureOptionsDlg extends JDialog
{
// ***********************  Construction  ***********************************//
	CaptureOptionsDlg(Frame parent)	
	{
		super(parent, "Capture Options", true);
		
		// 2004/12/29  WHF  Note that windowOpened is only called when the 
		//  window is first created.  We want every time it's made visible. 
		//addWindowListener(new WindowAdapter() {
		//	public void windowOpened(WindowEvent we)
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce)
			{ okHit = false; fillDevices(); }
		});
		
		final Container cp = getContentPane();
		final GridBagConstraints gbc = new GridBagConstraints();
		JPanel p;
		JComboBox jcb;
		
		cp.setLayout(new GridBagLayout());
		
		// Set common constraints:
		gbc.gridwidth=1;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.fill=gbc.BOTH;
		
		// Create the Source panel:
		p = createInnerPanel("Output");
		// First col: labels
		gbc.gridx=0;
		gbc.gridy=0;
		addComponent(p, new JLabel("RBNB Source Name:"), gbc);
		addComponent(p, new JLabel("Cache (frames):"), gbc);
		addComponent(p, new JLabel("Archive (frames):"), gbc);
		
		// Second col: controls
		gbc.gridx=1;
		gbc.gridy=0;
		addComponent(p, sourceName = new JTextField(
				rbnbAVCP.DEFAULT_SOURCE, 10), gbc);
		addComponent(p, cache = new JTextField(
				String.valueOf(rbnbAVCP.DEFAULT_CACHE), 10), gbc);
		addComponent(p, archive = new JTextField(
				String.valueOf(rbnbAVCP.DEFAULT_ARCHIVE), 10), gbc);
				
		// Source is the first panel:
		gbc.gridx=0;
		gbc.gridy=0;
		addComponent(cp, p, gbc);
		
		// Create the Video panel:
		p = createInnerPanel("Video");		
		// First col: labels
		gbc.gridx=0;
		gbc.gridy=0;
		addComponent(p, new JLabel("Device:"), gbc);
		addComponent(p, new JLabel("Format:"), gbc);
		addComponent(p, new JLabel("Resolution:"), gbc);
		addComponent(p, new JLabel("Frame Rate:"), gbc);
//		addComponent(p, new JLabel("Channel:"), gbc);
		// Second col: controls
		gbc.gridx=1;
		gbc.gridy=0;
		addComponent(p, videoDevices = new JComboBox(), gbc);
		videoDevices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ fillVideoFormats(); }
		});
		addComponent(p, videoFormats = new JComboBox(), gbc);
		videoFormats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ if (!videoFormatsChanging) fillVideoResolutions(); }
		});
		addComponent(p, videoResolutions = new JComboBox(), gbc);
		addComponent(p, videoFrameRate = new JTextField(
				String.valueOf(rbnbAVCP.DEFAULT_FRAME_RATE), 4), gbc);
/*		addComponent(p, videoChannel = new JTextField(
				"rbnbVideo/Video.jpg", 20), gbc); */
		
		// Video is the second panel:
		gbc.gridx=0;
		gbc.gridy=1;
		addComponent(cp, p, gbc);

		// Next create the Audio panel:
		p = createInnerPanel("Audio");		

		// First col: labels
		gbc.gridx=0;
		gbc.gridy=0;
		addComponent(p, new JLabel("Capture Audio:"), gbc);
		addComponent(p, new JLabel("Device:"), gbc);
		addComponent(p, new JLabel("Format:"), gbc);
		addComponent(p, new JLabel("Sample Rate:"), gbc);
		addComponent(p, new JLabel("Bits per Sample:"), gbc);
		addComponent(p, new JLabel("Mono/Stereo:"), gbc);
//		addComponent(p, new JLabel("Channel:"), gbc);		

		// Second col: controls
		gbc.gridx=1;
		gbc.gridy=0;
		addComponent(p, audioEnabled = new JCheckBox(), gbc);
		audioEnabled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) 
			{ setAudioEnabled(((JCheckBox)ae.getSource()).isSelected()); }
		});
		audioEnabledList.add(addComponent(p, 
				audioDevices = new JComboBox(), gbc));
		audioDevices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ fillAudioFormats(); }
		});
		audioEnabledList.add(addComponent(p, 
				audioFormats = new JComboBox(), gbc)); 
		audioFormats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ if (!audioFormatsChanging) fillAudioSampleRates(); }
		});
		audioEnabledList.add(addComponent(p, 
				audioSampleRates = new JComboBox(), gbc)); 
		audioSampleRates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ if (!audioFormatsChanging) fillAudioBits(); }
		});
		audioEnabledList.add(addComponent(p, 
				audioBits = new JComboBox(), gbc)); 
		audioEnabledList.add(addComponent(p, 
				audioMonoStereo = new JComboBox(), gbc));
		audioMonoStereo.addItem("Mono");
		audioMonoStereo.addItem("Stereo");
//		audioEnabledList.add(addComponent(p, 
//				audioChannel = new JTextField("rbnbVideo/Audio", 20), gbc));

		// Set the audio options off by default:
		setAudioEnabled(false);

		// Audio is the third panel:
		gbc.gridx=0;
		gbc.gridy=2;
		addComponent(cp, p, gbc);
		
		// Add ok, cancel buttons as the fourth and last panel:
		p = new JPanel();
		p.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = gbc.NONE;
		gbc.anchor = gbc.EAST;
		addComponent(cp, p, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		{ JButton jb;
			getRootPane().setDefaultButton((JButton) addComponent(
					p, jb = new JButton("Ok"), gbc));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{ if (prepareFormats()) { setVisible(false); okHit = true; } }
			});
		}

		gbc.gridx = 1;
		gbc.gridy = 0;
		((JButton) addComponent(
				p, new JButton("Cancel"), gbc)).addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent ae) 
					{ setVisible(false); }
				});
		makeDialogCloseOnEscape(this);
		
		// Size the dialog to its most economical size:
		pack();
		setLocationRelativeTo(parent);
	}
	
// ***********************  Accessors  **************************************//
	/**
	  * @return <code>null</code> if no audio capture is desired.
	  */
	int getArchive() { return theArchive; }
	void setArchive(int arch) { theArchive = arch; }
	
	AudioFormat getAudioFormat() { return theAudioFormat; }
	
	CaptureDeviceInfo getAudioDevice() { return theAudioDevice; }
	void setAudioDeviceByNumber(int devnum)
	{
		java.util.Vector devices = CaptureDeviceManager.getDeviceList(
				new AudioFormat(null));  // Thusly are only audio formats found
		theAudioDevice = (CaptureDeviceInfo) devices.get(devnum);
	}
	void setAudioSampleRate(double srate)
	{
		if (theAudioFormat == null) 
			theAudioFormat = new AudioFormat(
					null,
					Format.NOT_SPECIFIED,
					Format.NOT_SPECIFIED,
					1
			);
		else theAudioFormat = new AudioFormat(
					theAudioFormat.getEncoding(),
					srate,
					theAudioFormat.getSampleSizeInBits(),
					theAudioFormat.getChannels()
			);
	}
	void setAudioBits(int bits)
	{
		if (theAudioFormat == null) 
			theAudioFormat = new AudioFormat(
					null,
					Format.NOT_SPECIFIED,
					bits,
					1
			);
		else theAudioFormat = new AudioFormat(
					theAudioFormat.getEncoding(),
					theAudioFormat.getSampleRate(),
					bits,
					theAudioFormat.getChannels()
			);
	}
	void setStereo(boolean stereo)
	{
		int nchan = stereo?2:1;
		if (theAudioFormat == null) 
			theAudioFormat = new AudioFormat(
					null,
					Format.NOT_SPECIFIED,
					Format.NOT_SPECIFIED,
					nchan
			);
		else theAudioFormat = new AudioFormat(
					theAudioFormat.getEncoding(),
					theAudioFormat.getSampleRate(),
					theAudioFormat.getSampleSizeInBits(),
					nchan
			);
	}		
				
	
	int getCache() { return theCache; }
	void setCache(int cache) { theCache = cache; }
	
	String getSourceName() { return theSourceName; }
	void setSourceName(String sn) { theSourceName = sn; }
	
	CaptureDeviceInfo getVideoDevice() { return theVideoDevice; }
	/**
	  * @throws ArrayIndexOutOfBoundsException if devnum > number of devices
	  */
	void setVideoDeviceByNumber(int devnum)
	{
		java.util.Vector devices = CaptureDeviceManager.getDeviceList(
				new VideoFormat(null));  // Thusly are only video formats found
		theVideoDevice = (CaptureDeviceInfo) devices.get(devnum);
		if (theVideoFormat == null) {
			theVideoFormat = (VideoFormat) theVideoDevice.getFormats()[0];
			if (theVideoFormat.getFrameRate() == Format.NOT_SPECIFIED)
				setVideoFrameRate(rbnbAVCP.DEFAULT_FRAME_RATE);
		}
	}

	VideoFormat getVideoFormat() { return theVideoFormat; }
	void setVideoFormat(String format)
	{
		if (theVideoFormat == null) theVideoFormat = new VideoFormat(format);
		else theVideoFormat = new VideoFormat(
				format,
				theVideoFormat.getSize(),
				theVideoFormat.getMaxDataLength(),
				theVideoFormat.getDataType(),
				theVideoFormat.getFrameRate()
		);
	}
	void setVideoResolution(Dimension res)
	{
		if (theVideoFormat == null) 
			theVideoFormat = new VideoFormat(
				null,
				res,
				Format.NOT_SPECIFIED,
				null,
				rbnbAVCP.DEFAULT_FRAME_RATE
		);
		else theVideoFormat = new VideoFormat(
				theVideoFormat.getEncoding(),
				res,
				theVideoFormat.getMaxDataLength(),
				theVideoFormat.getDataType(),
				theVideoFormat.getFrameRate()
		);
	}
	void setVideoFrameRate(float frate)
	{
		if (theVideoFormat == null) 
			theVideoFormat = new VideoFormat(
				null,
				null,
				Format.NOT_SPECIFIED,
				null,
				frate
		);
		else theVideoFormat = new VideoFormat(
				theVideoFormat.getEncoding(),
				theVideoFormat.getSize(),
				theVideoFormat.getMaxDataLength(),
				theVideoFormat.getDataType(),
				frate
		);
	}
	
	boolean wasOkHit() { return okHit; }
	
// ***********************  Private Methods  ********************************//

	private void fillAudioFormats()
	{
		audioFormatsChanging = true;
		audioFormats.removeAllItems();
		
		if (audioDevices.getSelectedItem() == null) return;
		Format[] formats = //CaptureDeviceManager.getDevice(
				//audioDevices.getSelectedItem().toString()).getFormats();
				((CDIWrapper) audioDevices.getSelectedItem())
					.getCaptureDeviceInfo().getFormats();
		ArrayList putFormats = new ArrayList();
		for (int ii = 0; ii < formats.length; ++ii) {
			AudioFormat vf = (AudioFormat) formats[ii];
			if (!putFormats.contains(vf.getEncoding())) {
				audioFormats.addItem(vf.getEncoding());
				putFormats.add(vf.getEncoding());
			}
		}
		audioFormatsChanging = false;
		fillAudioSampleRates();
	}
	
	private void fillAudioSampleRates()
	{
		audioFormatsChanging = true;
		audioSampleRates.removeAllItems();
		
		Format[] formats = //CaptureDeviceManager.getDevice(
				//audioDevices.getSelectedItem().toString()).getFormats();
				((CDIWrapper) audioDevices.getSelectedItem())
					.getCaptureDeviceInfo().getFormats();
		String encoding = audioFormats.getSelectedItem().toString();
		// We use Vector here only because of the antiquated interface in
		//  DefaultComboBoxModel.
		Vector putRates = new Vector();
		for (int ii = 0; ii < formats.length; ++ii) {
			AudioFormat af = (AudioFormat) formats[ii];
			Double sr = new Double(af.getSampleRate());
			if (af.getEncoding().equals(encoding) && !putRates.contains(sr)) {
				//audioSampleRates.addItem(sr);
				putRates.add(sr);
			}
		}		
		Collections.sort(putRates);
		audioSampleRates.setModel(new DefaultComboBoxModel(putRates));

		audioFormatsChanging = false;
		
		fillAudioBits();
	}
	
	private void fillAudioBits()
	{
		audioBits.removeAllItems();
		Format[] formats = //CaptureDeviceManager.getDevice(
				//audioDevices.getSelectedItem().toString()).getFormats();
				((CDIWrapper) audioDevices.getSelectedItem())
					.getCaptureDeviceInfo().getFormats();
		String encoding = audioFormats.getSelectedItem().toString();
		double SR = ((Double) audioSampleRates.getSelectedItem()).doubleValue();
		Vector putBits = new Vector();
		for (int ii = 0; ii < formats.length; ++ii) {
			AudioFormat af = (AudioFormat) formats[ii];
			Integer ssib = new Integer(af.getSampleSizeInBits());
			if (af.getEncoding().equals(encoding) && af.getSampleRate() == SR
					&& !putBits.contains(ssib)) {
				//audioBits.addItem(ssib);
				putBits.add(ssib);
			}
		}
		Collections.sort(putBits);
		audioBits.setModel(new DefaultComboBoxModel(putBits));
	}		
	
	private void fillDevices()
	{
		java.util.Vector devices;
		
		// Find video devices:
		videoDevices.removeAllItems();		
		devices = CaptureDeviceManager.getDeviceList(
				new VideoFormat(null));  // Thusly are only video formats found
		if (devices.isEmpty()) {
			JOptionPane.showMessageDialog(
					this,
					"No video capture devices found.",
					"Error finding capture devices:",
					JOptionPane.ERROR_MESSAGE);
			setVisible(false);
			return;
		}
		for (Iterator iter = devices.iterator(); iter.hasNext(); ) {
			videoDevices.addItem(//((CaptureDeviceInfo) iter.next()).getName());
					new CDIWrapper((CaptureDeviceInfo) iter.next()));
		}
		
		fillVideoFormats();
		
		// Find audio devices:
		audioDevices.removeAllItems();
		devices = CaptureDeviceManager.getDeviceList(
				// And lo, they came unto the audio devices
				new AudioFormat(null));  
		for (Iterator iter = devices.iterator(); iter.hasNext(); ) {
			audioDevices.addItem(//((CaptureDeviceInfo) iter.next()).getName());
					new CDIWrapper((CaptureDeviceInfo) iter.next()));
		}
		
		fillAudioFormats();
				
		pack(); // may change size of some controls, so readjust
	}
	
	private void fillVideoFormats()
	{
		videoFormatsChanging = true;
		videoFormats.removeAllItems();

		if (videoDevices.getSelectedItem() == null) return;
		Format[] formats = //CaptureDeviceManager.getDevice(
				//videoDevices.getSelectedItem().toString()).getFormats();
				((CDIWrapper) videoDevices.getSelectedItem())
					.getCaptureDeviceInfo().getFormats();
		ArrayList putFormats = new ArrayList();
		for (int ii = 0; ii < formats.length; ++ii) {
			VideoFormat vf = (VideoFormat) formats[ii];
			if (!putFormats.contains(vf.getEncoding())) {
				videoFormats.addItem(vf.getEncoding());
				putFormats.add(vf.getEncoding());
			}
		}
		videoFormatsChanging = false;
		fillVideoResolutions();
	}
	
	private void fillVideoResolutions()
	{
		videoResolutions.removeAllItems();
		Format[] formats = //CaptureDeviceManager.getDevice(
				//videoDevices.getSelectedItem().toString()).getFormats();
				((CDIWrapper) videoDevices.getSelectedItem())
					.getCaptureDeviceInfo().getFormats();
		String encoding = videoFormats.getSelectedItem().toString();
		for (int ii = 0; ii < formats.length; ++ii) {
			VideoFormat vf = (VideoFormat) formats[ii];
			if (vf.getEncoding().equals(encoding))
				videoResolutions.addItem(new DimensionWrapper(vf.getSize()));
		}
	}
	
	/**
	  * Return true if inputs valid (as far as this code knows).
	  */
	private boolean prepareFormats()
	{
		theSourceName = sourceName.getText();
		try {
			theCache = Integer.parseInt(cache.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(
					this,
					"Cache should be a number greater than zero.",
					"Error processing cache option:",
					JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
		try {
			theArchive = Integer.parseInt(archive.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(
					this,
					"Archive should be a number equal to zero OR greater than"
					+" the cache size.",
					"Error processing archive option:",
					JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
		
		float frameRate;
		try {
			frameRate = Float.parseFloat(videoFrameRate.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(
					this,
					"Frame rate is not a valid value.",
					"Error parsing frame rate:",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		theVideoDevice = ((CDIWrapper) 
				videoDevices.getSelectedItem()).getCaptureDeviceInfo();
		theVideoFormat = new VideoFormat(
				videoFormats.getSelectedItem().toString(),
				((DimensionWrapper) videoResolutions.getSelectedItem())
						.getDimension(),
				Format.NOT_SPECIFIED,
				null,
				frameRate
		);
		if (audioFormats.isEnabled()) {
			theAudioDevice = ((CDIWrapper) audioDevices.getSelectedItem())
					.getCaptureDeviceInfo();
			theAudioFormat = new AudioFormat(
					audioFormats.getSelectedItem().toString(),
					((Double) audioSampleRates.getSelectedItem()).doubleValue(),
					((Integer) audioBits.getSelectedItem()).intValue(),
					audioMonoStereo.getSelectedIndex()+1
			);
		} else {
			theAudioFormat = null;
			theAudioDevice = null;
		}
		
		return true;
	}
	
	// 2005/03/22  WHF  Made package protected so can be called by rbnbAVCP.
	void setAudioEnabled(boolean onoff)
	{
		for (Iterator iter = audioEnabledList.iterator(); iter.hasNext();) {
			Component c = (Component) iter.next();
			c.setEnabled(onoff);
		}
	}
	boolean getAudioEnabled()
	{
		return ((Component) audioEnabledList.get(0)).isEnabled();
	}
	
// ************************  Inner Classes  *********************************//
	/** 
	  * Wraps a CaptureDeviceInfo object so that it appears attractively.
	  */
	private static class CDIWrapper
	{
		CDIWrapper(CaptureDeviceInfo cdi) { this.cdi = cdi; }
		CaptureDeviceInfo getCaptureDeviceInfo() { return cdi; }
		public String toString() { return cdi.getName(); }
		
		private final CaptureDeviceInfo cdi;
	}
	
	/**
	  * Wraps a Dimension object so that it appears attractively.
	  */
	private static class DimensionWrapper
	{
		DimensionWrapper(Dimension d) { this.d = d; }
		Dimension getDimension() { return d; }
		public String toString() 
		{ return String.valueOf(d.width)+'x'+d.height; }
		
		private final Dimension d;
	}
	
	private static class CloseAction extends AbstractAction
	{
		public CloseAction(JDialog _toClose) 
		{
			putValue(NAME, "CloseAction");
			toClose = _toClose;
		}
		public void actionPerformed(ActionEvent ae)
		{ toClose.setVisible(false); }
		
		private final JDialog toClose;
	}


// ******************  Static Utility Methods  **********************//
	static void makeDialogCloseOnEscape(JDialog jd)
	{
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = jd.getRootPane();
		Action action = new CloseAction(jd);
		rootPane.getActionMap().put(action.getValue(Action.NAME), action);
		rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				stroke,
				action.getValue(Action.NAME)
		);
	}
	
	/**
	  * Adds a component to a container using the specified constraints.
	  * @param con The container, which must have a GridBagLayout.
	  * @return the component.
	  * @throws A ClassCastException if the container's layout is not a 
	  *   GridBagLayout.
	  */
	static Component addComponent(
			Container con,
			Component c, 
			GridBagConstraints gbc)
	{
		((GridBagLayout) con.getLayout()).setConstraints(c, gbc);
		con.add(c);
		++gbc.gridy;
		return c;
	}

	static JPanel createInnerPanel(String title)
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.setBorder(
			BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(), title			
		));
		
		return p;
	}
	
// **************************  Private Member Data  *************************//
	private final ArrayList audioEnabledList = new ArrayList();
	private final JCheckBox audioEnabled;
	private final JComboBox videoDevices, videoFormats, videoResolutions,
			audioDevices, audioFormats, audioSampleRates, audioBits,
			audioMonoStereo;

	private boolean audioFormatsChanging;
	private boolean videoFormatsChanging;
	//private final JTextField videoChannel, audioChannel;
	private final JTextField sourceName, cache, archive, videoFrameRate;
	
	// Dialog 'outputs':
	private boolean okHit;
	private String theSourceName = "";
	private CaptureDeviceInfo theVideoDevice, theAudioDevice;
	private VideoFormat theVideoFormat;
	private AudioFormat theAudioFormat;
	private int theCache = rbnbAVCP.DEFAULT_CACHE,
			theArchive = rbnbAVCP.DEFAULT_ARCHIVE;
	
} // end CaptureOptionsDlg

class ViewOptionsDlg extends JDialog
{
// ***************************  Construction  *******************************//
	ViewOptionsDlg(Frame parent, Sink rbnbCon)
	{
		super(parent, "Display Options", true);
		this.rbnbCon = rbnbCon;
		
		// 2004/12/29  WHF  Note that windowOpened is only called when the 
		//  window is first created.  We want every time it's made visible. 
		//addWindowListener(new WindowAdapter() {
		//	public void windowOpened(WindowEvent we)
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce)
			{ 
				okHit = false;
				audioName.setText(theAudioChannel);
				videoName.setText(theVideoChannel);
				refreshTree();
			}
		});
		
		// Set up explorer control:
		rbnbExplorer.addDisplayFilter(ChannelTree.SINK);
		
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		// Set common constraints:
		gbc.weightx=1.0;
		gbc.gridx=0;
		gbc.gridy=0;

/*
		// Top is the explorer view:
		gbc.gridwidth = 2;
		gbc.weighty = 1.0;
		gbc.fill=gbc.BOTH;
		gbc.insets.left = gbc.insets.right = gbc.insets.top 
				= gbc.insets.bottom = 10;
		CaptureOptionsDlg.addComponent(cp, rbnbExplorer, gbc);
		rbnbExplorer.getTree().addTreeSelectionListener(
				new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent tse)
					{ updateSelection(); }
				});
*/
		// First col: labels
		gbc.gridwidth=1;
		gbc.weighty = 0.0;
		gbc.fill = gbc.NONE;
		gbc.insets.left = 10;
		gbc.insets.right = 0;
		gbc.insets.top = 0; 
		gbc.insets.bottom = 5;
//		CaptureOptionsDlg.addComponent(cp, new JLabel("Input Source:"), gbc);
		CaptureOptionsDlg.addComponent(cp, new JLabel("Video Channel:"), gbc);
//		CaptureOptionsDlg.addComponent(cp, new JLabel("Play Audio:"), gbc);
		CaptureOptionsDlg.addComponent(cp, new JLabel("Audio Channel:"), gbc);

		// Second col: controls
		gbc.gridx=1;
		gbc.gridy=0;
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets.left = 0;
		gbc.insets.right = 0;
		gbc.insets.top = 0; 
		gbc.insets.bottom = 5;
/*
		CaptureOptionsDlg.addComponent(cp, 
				sourceName = new JTextField(rbnbAVCP.DEFAULT_SOURCE, 10), gbc);
		CaptureOptionsDlg.addComponent(cp, 
				videoName = new JTextField(rbnbAVCP.DEFAULT_VIDEO, 10), gbc);
		CaptureOptionsDlg.addComponent(cp, playAudio = new JCheckBox(), gbc);
		CaptureOptionsDlg.addComponent(cp, 
				audioName = new JTextField(rbnbAVCP.DEFAULT_AUDIO, 10), gbc);
*/
		CaptureOptionsDlg.addComponent(cp, 
				videoName = new JTextField(10), gbc);
		CaptureOptionsDlg.addComponent(cp, 
				audioName = new JTextField(10), gbc);
		audioName.setToolTipText("Leave empty to disable audio playback.");
		
		// Third col: browsing
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets.right = 10;
		// Video browsing:
		((JButton) CaptureOptionsDlg.addComponent(
				cp, new JButton("Browse..."), gbc))
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae)
					{ browseVideo(); }
		});
		// Audio browsing:
		((JButton) CaptureOptionsDlg.addComponent(
				cp, new JButton("Browse..."), gbc))
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae)
					{ browseAudio(); }
		});

		// Add ok, cancel buttons:
		JPanel p = new JPanel();
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.fill = gbc.NONE;
		gbc.anchor = gbc.EAST;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.top = 0; 
		gbc.insets.bottom = 10;
		CaptureOptionsDlg.addComponent(cp, p, gbc);
		
		p.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets.left = gbc.insets.right = gbc.insets.top 
				= gbc.insets.bottom = 0;
		
		{ JButton jb;
			getRootPane().setDefaultButton(
					(JButton) CaptureOptionsDlg.addComponent(
					p, jb = new JButton("Ok"), gbc));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{ setVisible(false); okHit = true; prepareChannels(); }
			});
		}

		gbc.gridx = 1;
		gbc.gridy = 0;
		((JButton) CaptureOptionsDlg.addComponent(
				p, new JButton("Cancel"), gbc)).addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent ae) 
					{ setVisible(false); }
				});
		CaptureOptionsDlg.makeDialogCloseOnEscape(this);
		
		// Size the dialog to its most economical size:
		pack();
		setLocationRelativeTo(parent);
	}
	
// ***************************  Accessors  **********************************//
	boolean wasOkHit() { return okHit; }
	String getAudioChannel() { return theAudioChannel; }
	void setAudioChannel(String chan) { theAudioChannel = chan; }
	String getVideoChannel() { return theVideoChannel; }
	void setVideoChannel(String chan) { theVideoChannel = chan; }

// *************************  Private Methods  ******************************//
	private void browseAudio()
	{
		String res = channelSelect();
		if (res != null) audioName.setText(res);
	}
	private void browseVideo()
	{
		String res = channelSelect();
		if (res != null) videoName.setText(res);
	}
	
	private String channelSelect()
	{
		int result = JOptionPane.showConfirmDialog(
				this,
				rbnbExplorer,
				"Select channel to display:",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
		);
		
		if (result == JOptionPane.OK_OPTION) { // ok was selected
			ChannelTree.Node node = rbnbExplorer.getSelectedNode();
			if (node != null) {
				if (ChannelTree.CHANNEL.equals(node.getType()))
					return node.getFullName();
				else JOptionPane.showMessageDialog(
						this,
						"Only channels may be selected for display.",
						"Selection error:",
						JOptionPane.ERROR_MESSAGE
				);
			} else JOptionPane.showMessageDialog(
					this,
					"No channel was selected.",
					"Selection error:",
					JOptionPane.ERROR_MESSAGE
			);
		}
		return null;
	}

	private void prepareChannels()
	{
		/*
		String theSource = sourceName.getText();
		if (!theSource.endsWith("/")) theSource = theSource + '/';
		if (playAudio.isSelected())
			theAudioChannel	= theSource + audioName.getText();
		else theAudioChannel = null;
		theVideoChannel = theSource + videoName.getText();
		*/
		theAudioChannel = audioName.getText();
		theVideoChannel = videoName.getText();
		if (theAudioChannel.length() == 0) theAudioChannel = null;
	}
	
	private void refreshTree()
	{
		try {
			ChannelMap cm = new ChannelMap();
			cm.Add("/...");
			rbnbCon.RequestRegistration(cm);
			cm = rbnbCon.Fetch(-1);
			rbnbExplorer.display(cm);
		} catch (SAPIException se) {
			rbnbAVCP.showException(
					this,
					se,
					"Error browsing RBNB hierarchy:",
					"An error occurred while reading the RBNB hierarchy."
			);
		}
	}
	
	/**
	  * Called when a new node is selected in the tree-view.
	  */
/*	private void updateSelection()
	{
		ChannelTree.Node node = rbnbExplorer.getSelectedNode();
		if (node != null)
			sourceName.setText(node.getFullName());		
	} */	

// *************************  Instance Data  ********************************//
	private boolean okHit;
	private String theAudioChannel = rbnbAVCP.DEFAULT_SOURCE + '/'
				+ rbnbAVCP.DEFAULT_AUDIO, 
			theVideoChannel = rbnbAVCP.DEFAULT_SOURCE + '/'
				+ rbnbAVCP.DEFAULT_VIDEO;
	
	//private final JCheckBox playAudio;
	private final JTextField audioName, /*sourceName,*/ videoName;
	private final RBNBExplorer rbnbExplorer = new RBNBExplorer();
	private final Sink rbnbCon;
}

