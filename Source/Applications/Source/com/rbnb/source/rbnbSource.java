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

package com.rbnb.source;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

import java.awt.*;
import java.awt.event.*;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
  * A dialog which presents the user with options for supplying various 
  *  waveforms as inputs to an RMap server.
  *
 * <p>
 *
 * @author WHF
 *
 * @since V2.0
 * @version 06/29/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/20/2005  EMF      Added option for local timeofday timestamps.
 * 11/05/2004  MJM      Changed Detach to CloseRBNBConnection.  Detach would create sequence of
 *                      rbnbSource_X sources with each change in channel list etc
 * 06/29/2004  INB	Replaced deprecated <code>Source.CloseRBNBConnection(boolean,boolean)</code> 
 *			method with <code>Source.CloseRBNBConnection</code> or
 *			<code>Source.Detach</code>.
 * 10/13/2003  INB	Changed close to ensure that the archive is kept.
 * 04/03/2003  WHF  The source is now reset when the number of channels 
 *						changes, due to a requirement that frames be identical.
 *					Fixed bug when cancel is hit on connect dialog.
 * 03/18/2003  WHF  Timestamps are no longer reset with changes in control 
 *						options.
 * 02/05/2003  WHF  Added -min command line option.
 * 08/14/2002  WHF  Broke down again and added multiple data type support.
 * 04/17/2002  WHF  Broke down and added command-line support, for batching.
 * 04/16/2002  WHF  Corrected bug where DC Offset field was not monitored.
 						Source name placed in title.
 * 04/15/2002  WHF  Added menu mnemonics.
 * 04/02/2002  WHF  Added DC offset, source naming cabilities.
 * 12/11/2001  WHF	Removed try block around CloseRBNBConnection in 
 *				onDisconnect() and Quit().
 * 09/20/2001  WHF	Corrected bug with abrupt termination of server
 *			while running.  Now uses SwingUtilities.invokeLater().
 * 08/28/2001  WHF	Corrected bug with freq slider, shrank dialog.
 * 08/20/2001  WHF	Fixed bug with Open.../Cancel.  Made channel selection
				a list box control.
 * 08/06/2001  WHF      Fixed bug in multiple channel implementation.
 * 07/05/2001  WHF      Added multiple channel option.
 * 06/28/2001  WHF	Added RBNB icon to Frame.
 * 06/15/2001  WHF	Used proper thread signalling techniques and join().
 * 06/11/2001  WHF  Changed to four slider representation of options.
 * 05/30/2001  INB  Changed the time stamping from automatic time-of-day
 * 05/22/2001  WHF 	Converted over to SAPI from beans.
 * 05/10/2001  WHF	Created.
 *
 */
public class rbnbSource extends JFrame implements ActionListener,
	FocusListener, ChangeListener
{
	private static final double MAXFREQ=1<<20, MINFREQ=1;
	private static final int MAXAMP=100, MAXFRAMES=4096, MIN_FRAMES=1;

//	private static final double logOf10=Math.log(10.0);
	private static final double logOf2=Math.log(2.0);

	// GUI Objects:
	private static final String 	
		// titlePrefix="rbnbSource: ", no longer static
					openMenuText="Open Server...",
					closeMenuText="Close Connection",
					archiveMenuText="Properties...",
					IMAGEPATH="images/",
					archiveStrings[]=
						{"None","Create","Append"},
					MIMETYPE="application/octet-stream",
					CLIENTNAMEDEFAULT="rbnbSource",
					dcUpCmd="UpButton",
					dcDownCmd="DownButton",
					NO_CON_MSG=": not connected";
					
	private static final int SLIDERPAD=8;
	private ButtonGroup
		waveFormButtons=new ButtonGroup(),
		dataTypeButtons=new ButtonGroup();
	private JButton startButton;
	private JSlider freqSlider, samplingSlider, framesSlider, ampSlider;
	private JMenuItem 	openMenu,
				archiveMenu;
	private JCheckBoxMenuItem delayControlCheckBox;
	private JComboBox channelBox;
	
	private final ArchiveDialog archiveDialog=new ArchiveDialog(this);

	private JTextField 	freqField,
				samplingRateField,
				amplitudeField,
				framesField,
				framesActualField,
				pointsPerFrameField,
				dcOffsetField;

	// Waveform data:
//	private double[] data=new double[20];	// Waveform proper.
//	private float[] data=new float[20];	// Waveform proper.
	// Multiple data types now handled here:
//	private DataHandler dataHandler=floatHandler;

	private String[] chanNames={"c0"},
		channelChoices={"1","2","5","10","20","50","100","200","500",
			"1000","2000","5000","10000"};

	private int amp=1, frameRate=1<<0, sampleRate=1<<7, freq=1<<3,
			numChan=1;
	private double dcOffset;

	// Thread objects:
	private DataThread dataThread;	
	private boolean doExit=true, doConnect=false, doStart=false, 
		startMin=false;

	// RBNB objects:
	private static int CACHEDEFAULT=100;
	private final Source sb=new Source();
	private final ChannelMap cm=new ChannelMap();
	private boolean connected=false,
		fixChannels=true; // If true, channel map needs updating
	private static final String defaultServer="localhost:3333";
	private String server=defaultServer;
        //EMF 9/20/05: local timeofday timestamps
	private boolean doTimeofday=false;

    /**
     * Default constructor.  Initializes dialog controls.
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  WHF	Created.
     *
     */
	public rbnbSource()
	{
		super();
		setTitle(archiveDialog.getClientName()+": not connected");
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we)
			{ Quit(); } } );

		setIconImage(new ImageIcon(getClass().getClassLoader()
			.getResource(IMAGEPATH+"RBNBIcon.GIF")).getImage());

		// Setup menu:
		JMenuBar bar=new JMenuBar();
		JMenu file=new JMenu("File");
		file.add(openMenu=new JMenuItem(openMenuText));
		file.setMnemonic('F');
		openMenu.setActionCommand("Open");
		openMenu.addActionListener(this);
		openMenu.setMnemonic(openMenuText.charAt(0));
		file.add(archiveMenu=new JMenuItem(archiveMenuText));
		archiveMenu.setActionCommand("Archive");
		archiveMenu.setMnemonic(archiveMenuText.charAt(0));
		archiveMenu.addActionListener(this);
		JMenuItem jmi;
		file.add(jmi=new JMenuItem("Exit"));
		jmi.setMnemonic('x');
		jmi.addActionListener(this);
		bar.add(file);
		/*JMenu options=new JMenu("Options");
		options.add(delayControlCheckBox=
			new JCheckBoxMenuItem("Delay Control",true));
		delayControlCheckBox.setToolTipText(
			"Modulates frame spacing in real time");
		bar.add(options); */
		JMenu types=new JMenu("Data Type");
		types.setMnemonic('D');
		JRadioButtonMenuItem jrbmi;
		types.add(jrbmi=new JRadioButtonMenuItem("Double"));
		jrbmi.setMnemonic('D');
		jrbmi.addActionListener(typeActionListener);
		dataTypeButtons.add(jrbmi);
		types.add(jrbmi=new JRadioButtonMenuItem("Float",true));
		jrbmi.setMnemonic('F');
		jrbmi.addActionListener(typeActionListener);
		dataTypeButtons.add(jrbmi);
		types.add(jrbmi=new JRadioButtonMenuItem("Long"));
		jrbmi.setMnemonic('L');
		jrbmi.addActionListener(typeActionListener);
		dataTypeButtons.add(jrbmi);
		types.add(jrbmi=new JRadioButtonMenuItem("Int"));
		jrbmi.setMnemonic('I');
		jrbmi.addActionListener(typeActionListener);
		dataTypeButtons.add(jrbmi);
		types.add(jrbmi=new JRadioButtonMenuItem("Short"));
		jrbmi.setMnemonic('S');
		jrbmi.addActionListener(typeActionListener);
		dataTypeButtons.add(jrbmi);
		types.add(jrbmi=new JRadioButtonMenuItem("Byte"));		
		jrbmi.setMnemonic('B');
		jrbmi.addActionListener(typeActionListener);
		dataTypeButtons.add(jrbmi);
		bar.add(types);
		setJMenuBar(bar);

		// Set up dialog controls:
		Container cp=getContentPane();
		Component c;
		GridBagLayout gbl,gbl2;
		GridBagConstraints gbc=new GridBagConstraints(), gbc2;
		JPanel p, p2;

		cp.setLayout(gbl=new GridBagLayout());

		gbc.gridwidth=1;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.fill=gbc.BOTH;
		gbc2=(GridBagConstraints) gbc.clone();
		gbc.insets.top=gbc.insets.bottom=3;
		gbc.insets.left=gbc.insets.right=4;

		// First col:
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridheight=4;
		gbc.fill=gbc.VERTICAL;
		p=new JPanel();
		p.setLayout(gbl2=new GridBagLayout());
		p.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"Freq. (Hz)"),
			BorderFactory.createEmptyBorder(2,SLIDERPAD,2,
				SLIDERPAD)));

		// Freq slider:
		gbc2.gridx=gbc2.gridy=0;
		gbc2.gridheight=1;
		gbc2.fill=gbc2.VERTICAL;

		Hashtable h=new Hashtable();
		for (int ii=0; ii<=20; ii+=5)
		{
			int val=1<<ii;
			h.put(new Integer(ii),new JLabel(String.valueOf(val)));
		}
		h.put(new Integer(20),new JLabel("1.05M"));
		h.put(new Integer(15),new JLabel("32.8K"));

		c=freqSlider=new JSlider(JSlider.VERTICAL,0,20,log2(freq));
		freqSlider.setMajorTickSpacing(5);
		freqSlider.setMinorTickSpacing(1);
		freqSlider.setPaintTicks(true);
		freqSlider.setPaintLabels(true);
		freqSlider.setSnapToTicks(true);
		freqSlider.setLabelTable(h);
		freqSlider.addChangeListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);

		// Freq field:
		gbc2.gridy+=gbc2.gridheight;
		gbc2.fill=gbc2.HORIZONTAL;
		c=freqField=new JTextField(String.valueOf(freq),5/*7*/);
		freqField.setActionCommand("srf");
		freqField.addActionListener(this);
		freqField.addFocusListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);

		gbl.setConstraints(p,gbc);
		cp.add(p);

		// Under first column:
		gbc.gridy+=gbc.gridheight;
		gbc.gridwidth=2;
		gbc.gridheight=2;
		gbc.fill=gbc.BOTH;
//		gbc.ipadx=20;
		p=new JPanel();
		((FlowLayout) p.getLayout()).setVgap(0);
		p.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"Channels"));		
		p.add(channelBox=new JComboBox(channelChoices));
		channelBox.setEditable(true);
		channelBox.setActionCommand("Channels");
		channelBox.addActionListener(this);
		gbl.setConstraints(p,gbc);
		cp.add(p);

		// Actual freq field:
		gbc.gridy+=gbc.gridheight;
		gbc.gridheight=1;
		p=new JPanel();
		((FlowLayout) p.getLayout()).setVgap(0);
		p.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),
			"Actual Frame Rate"));
		c=framesActualField=new JTextField("",5);
		c.setEnabled(false);
		p.add(c);
		gbl.setConstraints(p,gbc);
		cp.add(p);	

		// Points per frame field:
		gbc.gridy+=gbc.gridheight;
		gbc.gridheight=1;
		p=new JPanel();
		((FlowLayout) p.getLayout()).setVgap(0);
		p.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"Points per Frame"));
		c=pointsPerFrameField=new JTextField(String.valueOf(sampleRate
			/frameRate),5);
		c.setEnabled(false);
		p.add(c);
		gbl.setConstraints(p,gbc);
		cp.add(p);	
		

		// Second column:
		gbc.gridx=1;
		gbc.gridy=0;
		gbc.gridheight=4;
		gbc.gridwidth=1;
		gbc.fill=gbc.VERTICAL;
		
		p=new JPanel();
		p.setLayout(gbl2=new GridBagLayout());
/*
		Font f=UIManager.getLookAndFeel().getDefaults().getFont(
			//"TitledBorder.font");
			"Label.font");
		f=new Font(f.getName(), f.getStyle(), 10);
		UIManager.getDefaults().put("Label.font",f);  */
		p.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Sample Rate" /*,
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				f*/),
			BorderFactory.createEmptyBorder(2,SLIDERPAD,2,SLIDERPAD)));
		// Sample Rate slider:
		gbc2.gridx=gbc2.gridy=0;
		gbc2.gridheight=1;
		gbc2.fill=gbc2.VERTICAL;
		h=new Hashtable();
		for (int ii=0; ii<=24; ii+=6)
		{
			int val=1<<ii;
			h.put(new Integer(ii),new JLabel(String.valueOf(val)));
		}
		// Replace a few select values:
		h.put(new Integer(24),new JLabel("16.8M"));
		h.put(new Integer(18),new JLabel("262K"));
		c=samplingSlider=new JSlider(JSlider.VERTICAL,0,24,
			log2(sampleRate));
		samplingSlider.setMajorTickSpacing(6);
		samplingSlider.setMinorTickSpacing(1);
		samplingSlider.setPaintTicks(true);
		samplingSlider.setPaintLabels(true);
		samplingSlider.setSnapToTicks(true);
		samplingSlider.setLabelTable(h);
		samplingSlider.addChangeListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);

		// Sampling field:
		gbc2.gridy+=gbc2.gridheight;
		gbc2.fill=gbc2.HORIZONTAL;
		c=samplingRateField=new JTextField(
			String.valueOf(sampleRate),5);

		samplingRateField.setActionCommand("srf");
		samplingRateField.addActionListener(this);
		samplingRateField.addFocusListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);

		gbl.setConstraints(p,gbc);
		cp.add(p);

		// Third column:
		gbc.gridx=2;
		gbc.gridy=0;
		gbc.gridheight=4;
		gbc.gridwidth=1;
		gbc.fill=gbc.VERTICAL;
		gbc.ipadx=0;

		p=new JPanel();
		p.setLayout(gbl2=new GridBagLayout());
		p.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"Frame Rate"),
			BorderFactory.createEmptyBorder(2,SLIDERPAD,
				2,SLIDERPAD)));
		// Frames slider:
		gbc2.gridx=gbc2.gridy=0;
		gbc2.gridheight=1;
		gbc2.fill=gbc2.VERTICAL;
		h=new Hashtable();
		for (int ii=0; ii<=12; ii+=3)
		{
			int val=1<<ii;
			h.put(new Integer(ii),new JLabel(String.valueOf(val)));
		}
		c=framesSlider=new JSlider(JSlider.VERTICAL,0,12,
			log2(frameRate));
		framesSlider.setMajorTickSpacing(3);
		framesSlider.setMinorTickSpacing(1);
		framesSlider.setPaintTicks(true);
		framesSlider.setPaintLabels(true);
		framesSlider.setSnapToTicks(true);
		framesSlider.setLabelTable(h);
		framesSlider.addChangeListener(this);

		gbl2.setConstraints(c,gbc2);
		p.add(c);

		// Frames field:
		gbc2.gridy+=gbc2.gridheight;
		gbc2.fill=gbc2.HORIZONTAL;
		c=framesField=new JTextField(String.valueOf(frameRate),4);
		framesField.setActionCommand("srf");
		framesField.addActionListener(this);
		framesField.addFocusListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);

		gbl.setConstraints(p,gbc);
		cp.add(p);
				
		// Under third column:		
		// DC Offset control:
		gbc.gridx=2;
		gbc.gridwidth=2;
		gbc.gridy+=gbc.gridheight;
		gbc.gridheight=1;
		gbc.fill=gbc.BOTH;
		
		p=new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"DC Offset"));
//		p.setLayout(new GridLayout(1,3)); 
		javax.swing.plaf.basic.BasicArrowButton bab
			=new javax.swing.plaf.basic.BasicArrowButton(JButton.WEST);
		bab.addActionListener(this);
		bab.setActionCommand(dcDownCmd);
		p.add(bab);
		p.add(dcOffsetField=new JTextField("0",7));
		dcOffsetField.setActionCommand("amp");
		dcOffsetField.addActionListener(this);
		dcOffsetField.addFocusListener(this);
		p.add(bab=new javax.swing.plaf.basic.BasicArrowButton(JButton.EAST));
		bab.addActionListener(this);
		bab.setActionCommand(dcUpCmd);
		gbl.setConstraints(p,gbc);
		cp.add(p);

		gbc.gridy+=gbc.gridheight;
		gbc.gridwidth=1;
		gbc.gridheight=gbc.REMAINDER;
		gbc.fill=gbc.NONE;
		// Waveform control:
		p=new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"Waveform"));
		p.setLayout(new GridLayout(3,1));
		p2=new JPanel();
		JRadioButton jr;
		JLabel jl;
		p2.add(jr=new JRadioButton("",true));
		jr.setActionCommand("Sine");
		jr.addActionListener(this);
		waveFormButtons.add(jr);

		// Note that the name must match exactly (case sensitive)!!
		p2.add(jl=new JLabel(new ImageIcon(getClass().getClassLoader()
			.getResource(IMAGEPATH+jr.getActionCommand()+".GIF"))));
		jl.addMouseListener(new MouseHandler(jr));
		p.add(p2);
		p2=new JPanel();
		p2.add(jr=new JRadioButton("",false));
		jr.setActionCommand("Square");
		jr.addActionListener(this);
		waveFormButtons.add(jr);
		p2.add(jl=new JLabel(new ImageIcon(getClass().getClassLoader()
			.getResource(IMAGEPATH+jr.getActionCommand()+".GIF"))));
		jl.addMouseListener(new MouseHandler(jr));
		p.add(p2);
		p2=new JPanel();
		p2.add(jr=new JRadioButton("",false));
		jr.setActionCommand("Sawtooth");
		jr.addActionListener(this);
		waveFormButtons.add(jr);
		p2.add(jl=new JLabel(new ImageIcon(getClass().getClassLoader()
			.getResource(IMAGEPATH+jr.getActionCommand()+".GIF"))));
		jl.addMouseListener(new MouseHandler(jr));
		p.add(p2);
		gbl.setConstraints(p,gbc);
		cp.add(p);

		// Start button:
		gbc.gridx=3;
		gbc.gridheight=gbc.REMAINDER;
		gbc.fill=gbc.NONE;
		c=startButton=new JButton("Start");
		startButton.addActionListener(this);
		startButton.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createEmptyBorder(2,5,2,5)));
		startButton.setEnabled(false);
		gbl.setConstraints(c,gbc);
		cp.add(c);
		
		// Fourth column:
		gbc.gridx=3;
		gbc.gridy=0;
		gbc.gridheight=4;
		gbc.gridwidth=1;
		gbc.fill=gbc.VERTICAL;
		p=new JPanel();
		p.setLayout(gbl2=new GridBagLayout());
		p.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),"Amplitude"),
			BorderFactory.createEmptyBorder(2,SLIDERPAD,2,
				SLIDERPAD)));
		// Amp slider:
		gbc2.gridx=gbc2.gridy=0;
		gbc2.gridheight=1;
		gbc2.fill=gbc2.VERTICAL;
		c=ampSlider=new JSlider(JSlider.VERTICAL,0,MAXAMP,1);
		ampSlider.setMajorTickSpacing(MAXAMP/10);
		ampSlider.setMinorTickSpacing(MAXAMP/100);
		ampSlider.setPaintTicks(true);
		ampSlider.setPaintLabels(true);
		ampSlider.setSnapToTicks(true);
		ampSlider.addChangeListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);

		// Amp field:
		gbc2.gridy+=gbc2.gridheight;
		gbc2.fill=gbc2.HORIZONTAL;
		c=amplitudeField=new JTextField("1",3);
		amplitudeField.setActionCommand("amp");
		amplitudeField.addActionListener(this);
		amplitudeField.addFocusListener(this);
		gbl2.setConstraints(c,gbc2);
		p.add(c);
		p.doLayout();
		p.setMinimumSize(p.getPreferredSize());

		gbl.setConstraints(p,gbc);
		cp.add(p);

//		setSize(420,480);
		pack();  // Sets the size equal to the minimum to have all controls 
		//  show up properly.

		// Set up threads:
//		timer=new Timer(1,timerListener);
		dataThread=new DataThread(1<<6);

		// Set up SrcBean:
		// INB 05/30/2001 - This doesn't produce timestamps that make
		// much sense for something that is supposed to represent
		// continuous data.
		// sb.AutoTimeStamp("timeofday");
				
		// Prepare first round of data:
		generateWaveForm();

	} // end default constructor

    /**
     * Closes the application.  Is called by <code>File - Exit</code> 
     *  and the system <code>Close</code>  request.
     * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 06/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/29/2004  INB	Replaced deprecated <code>Source.CloseRBNBConnection(boolean,boolean)</code> 
     *			method with
     *			<code>Source.CloseRBNBConnection</code> or
     *			<code>Source.Detach</code>.
     * 10/13/2003  INB	Ensure that the close keeps the archive.
     * 04/04/2003  WHF  Stop threads before exit.
     * 05/10/2001  WHF	Created.
     */
	public void Quit()
	{
		if (dataThread.isRunning()) dataThread.stop();
		sb.CloseRBNBConnection();  // mjm 11/5/04:  detach orphans old connections?!
		//sb.Detach();
		dispose();
		if (doExit) System.exit(0);
	}
	
	void setDoExit(boolean doExit) { this.doExit=doExit; }
	
	private static final double ooLOG10=1.0/Math.log(10);

	private double getOMag(double x,boolean isInc)
	{
		if (x==0) return 1.0;
		if (x<0) isInc=!isInc;
		double y=Math.log(Math.abs(x))/Math.log(10), 
			fy=Math.floor(y),
			cy=Math.ceil(y);
		if (Math.abs(y-fy)<1e-3) // x is a power of ten.
			return Math.max(1,Math.pow(10,fy-(isInc?0:1)));
		if (Math.abs(y-cy)<1e-3)
			return Math.max(1,Math.pow(10,cy-(isInc?0:1)));
		return Math.max(1,Math.pow(10,fy));
	}

	public final void actionPerformed(ActionEvent ae)
	{
//System.err.println(getSize());
		String s=ae.getActionCommand();
		if (s==dcUpCmd)
		{
			dcOffset+=getOMag(dcOffset,true);
			dcOffsetField.setText(String.valueOf(dcOffset));
			ampChange();
		}
		else if (s==dcDownCmd)
		{
			dcOffset-=getOMag(dcOffset,false);
			dcOffsetField.setText(String.valueOf(dcOffset));
			ampChange();
		}
		else if ("Open".equals(s))
			onOpen();
		else if ("Archive".equals(s))
		{
			archiveDialog.show();
			if (archiveDialog.okHit())
			{
/*				sb=new Source(CACHEDEFAULT,
					archiveDialog.getArchiveMode(),
					archiveDialog.getArchiveSize()); */
				try {
				sb.SetRingBuffer(CACHEDEFAULT,
					archiveDialog.getArchiveMode(),
					archiveDialog.getArchiveSize());				
				fixChannels=true;
				generateWaveForm();
				} catch (SAPIException se) {
					se.printStackTrace();
					JOptionPane.showMessageDialog(
						this,
						"Cannot set archive options as specified.",
						"Error:",
						JOptionPane.ERROR_MESSAGE);
				}
				setTitle(archiveDialog.getClientName()+NO_CON_MSG);
			}
		}
		else if ("Start".equals(s))
			onStart();
		else if ("Exit".equals(s))
			Quit();
		else if ("Stop".equals(s))
		{
			onStop();
		}
		else if ("srf".equals(s))
		{
			freqChange();
		}
		else if ("amp".equals(s))
		{
			ampChange();
		}
		else if ("Channels".equals(s))
		{
			channelChange();
		}
		else // waveform or channel number selection
		{
			generateWaveForm();
		}
	}

	public void focusGained(FocusEvent fe) {}

	public void focusLost(FocusEvent fe)
	{
		if (fe.getSource()==amplitudeField || fe.getSource()==dcOffsetField)
			ampChange();
		else freqChange();
	}

	private long sliderEventTime;

	// milliseconds between processed events:
	private static long EVENT_DELTA_T = 50;

	public void stateChanged(ChangeEvent e)
	{
		JSlider source = (JSlider)e.getSource();

		long currT=System.currentTimeMillis();
//		  if (!source.getValueIsAdjusting())
		if (currT-sliderEventTime>=EVENT_DELTA_T)
		{
			sliderEventTime=currT;
			if (source==freqSlider)
			{
				freqField.setText(
					String.valueOf(1<<freqSlider.getValue()));
				freqChange();
			}
			else if (source==samplingSlider)
			{
				samplingRateField.setText(
					String.valueOf(1<<samplingSlider.getValue()));
				freqChange();
			}
			else if (source==framesSlider)
			{
				framesField.setText(
					String.valueOf(1<<framesSlider.getValue()));
				freqChange();
			}
			else
			{
				amplitudeField.setText(
					String.valueOf(ampSlider.getValue()));
				ampChange();
			}
		  }
	}
	
	public void show()
	{
		super.show();
		if (doConnect) onOpen();
		if (connected&&doStart) onStart();
		setState(startMin?ICONIFIED:NORMAL);
	}

    /**
     * Starts the application.  Creates an rbnbSource object and makes it visible.
     * <p>
     *
     * @author WHF
     *
     * @param args The command line options are ignored.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/10/2001  WHF	Created.
     * 02/19/2002  WHF  Added "-noexit" parameter, which specifies that the
     *   application should not exit when closed.  Useful when run as a child.
     * 04/17/2002  WHF  Added full command-line support.
     * 09/20/2005  EMF  Added timeofday timestamp arg.  Not listed in help,
     *                  since for latency testing only.
     */
	public static void main(String args[])
	{  
		rbnbSource rs=new rbnbSource();
		
		// Process command line arguments:
		try {
		for (int ii=0; ii<args.length; ++ii)
		{
			if ("-?".equals(args[ii])||"-h".equals(args[ii]))
				throw new Exception();
			else if ("-noexit".equals(args[ii]))
				rs.setDoExit(false);
			else if ("-connect".equals(args[ii]))
				rs.setDoConnect(true);
			else if ("-start".equals(args[ii]))
			{
				rs.setDoStart(true);
				rs.setDoConnect(true);
			}
			else if ("-min".equals(args[ii]))
				rs.setStartMinimized(true);
			else if ("-a".equals(args[ii]))
				rs.setHostname(args[++ii]);
			else if ("-name".equals(args[ii]))
				rs.setSourceName(args[++ii]);
			else if ("-freq".equals(args[ii]))
				rs.setFreq(Integer.parseInt(args[++ii]));
			else if ("-sampleRate".equals(args[ii]))
				rs.setSampleRate(Integer.parseInt(args[++ii]));
			else if ("-frameRate".equals(args[ii]))
				rs.setFrameRate(Integer.parseInt(args[++ii]));
			else if ("-amplitude".equals(args[ii]))
				rs.setAmplitude(Integer.parseInt(args[++ii]));
			else if ("-dcOffset".equals(args[ii]))
				rs.setDCOffset(Double.parseDouble(args[++ii]));
			else if ("-channels".equals(args[ii]))
				rs.setChannels(Integer.parseInt(args[++ii]));
			else if ("-waveform".equals(args[ii]))
				rs.setWaveForm(Integer.parseInt(args[++ii]));
			else if ("-type".equals(args[ii]))
			{
				String type=args[++ii].toLowerCase();
				Enumeration butts=rs.dataTypeButtons.getElements();
				boolean found=false;
				while (!found&&butts.hasMoreElements())
				{
					AbstractButton ab=(AbstractButton) butts.nextElement();
					if (type.equals(ab.getText().toLowerCase()))
					{
						found=true;
						ab.doClick();
					}
				}
				if (!found) throw new Exception();						
			}
			else if ("-timeofday".equals(args[ii]))
				rs.setTimeofday();
			else throw new Exception();
		}
		} catch (Exception e) { e.printStackTrace(); showHelp(); System.exit(1); }
		rs.setVisible(true);
	}
	
	private static void showHelp()
	{
		System.err.println("Usage for "+rbnbSource.class.getName()+":"
			+"\n\t-noexit          Does not call System.exit() when closed"
			+"\n\t-a [hostname]    Provide hostname to connect to"
			+"\n\t-name [name]     Provide name for this source"
			+"\n\t-connect         Connect to the server automatically"
			+"\n\t-start           Start the source after connecting.  "
				+"Implies -connect"
			+"\n\t-min             Window is initially minimized"
			+"\n\t-freq [f]        Specify the initial signal frequency, Hz"
			+"\n\t-sampleRate [sr] Specify the initial sample rate"
			+"\n\t-frameRate [fr]  Specify the initial frame rate"
			+"\n\t-amplitude [a]   Specify the initial amplitude"
			+"\n\t-dcOffset [dc]   Specify the initial DC offset"
			+"\n\t-channels [ch]   Specify the initial number of channels"
			+"\n\t-waveform [wf]   Specify the initial signal waveform, one of:"
				+"\n\t\t0 - sinusoidal\n\t\t1 - square\n\t\t2 - triangular"
			+"\n\t-type [type]     Specify the data type sent, one of:"
				+"\n\t\t\"double\", \"float\", \"long\", \"int\", \"short\", "
				+"or \"byte\"."
			+"\n");
	}			

	// Produces the log base 2 of x:
	private static int log2(int x)
	{
		int ii=0;
		while (x!=1)
		{
			x>>=1;
			ii++;
		}
		return ii;
	}

	private void onDisconnect()
	{
		onStop();
		connected=false;
		openMenu.setText(openMenuText);
		//sb.Detach();  // mjm
		sb.CloseRBNBConnection();
		setTitle(archiveDialog.getClientName()+NO_CON_MSG);
		startButton.setEnabled(false);
		archiveMenu.setEnabled(true);
	}
	
	private void onOpen()
	{
		if (connected) // close
		{
			onDisconnect();
		}
		else
		{
			setTitle(archiveDialog.getClientName()+": connecting...");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			if (!doConnect)
			{ // if true, we want to skip this step once.
				Object o=JOptionPane.showInputDialog(
					this,
					"Input server name with which to connect:",
					"Enter server name",
					JOptionPane.QUESTION_MESSAGE,
					null,
					null,
					server);
				if (o==null) {
					setTitle(archiveDialog.getClientName()+NO_CON_MSG);
					setCursor(Cursor.getDefaultCursor());
					return;
				}
				server = o.toString();
			}
			else doConnect=false;
			try {
			sb.OpenRBNBConnection(server,archiveDialog.getClientName());
			} catch (SAPIException e)
			{
				e.printStackTrace();
				setCursor(Cursor.getDefaultCursor());
				JOptionPane.showMessageDialog(
					this,
					"Cannot connect to server "+server+".",
					"Error:",
					JOptionPane.ERROR_MESSAGE);
				setTitle(archiveDialog.getClientName()
					+": not connected");
				return;
			}
			openMenu.setText(closeMenuText);
			setTitle(archiveDialog.getClientName()+" on "+server);
			archiveMenu.setEnabled(false);					
			connected=true;
		}
		setCursor(Cursor.getDefaultCursor());
		startButton.setEnabled(connected);
	}
	

	private void onStart()
	{
		startButton.setText("Stop");
		dataThread.resetClock();
		dataThread.start();
	}

	private void onStop()
	{
		dataThread.stop();
		startButton.setText("Start");
	}

	private void freqChange()
	{
		freq=1<<10;

		try {
		freq=Integer.parseInt(freqField.getText());
		} catch (NumberFormatException nfe) { }
		if (freq>sampleRate>>2)
			freq=sampleRate>>2;
		if (freq<frameRate)
			freq=frameRate;
		freqField.setText(String.valueOf(freq));

		sampleRate=1<<12;
		try {
		sampleRate=Integer.parseInt(samplingRateField.getText());
		} catch (NumberFormatException nfe)
		{  }
		if (sampleRate<freq*4)
			sampleRate=freq*4;
		samplingRateField.setText(String.valueOf(sampleRate));

		try {
		frameRate=Integer.parseInt(framesField.getText());
		} catch (NumberFormatException nfe)
		{  }
		if (frameRate>freq)
			frameRate=freq;
		framesField.setText(String.valueOf(frameRate));

		pointsPerFrameField.setText(String.valueOf(sampleRate/frameRate));
		freqSlider.setValue(log2(freq));
		samplingSlider.setValue(log2(sampleRate));
		framesSlider.setValue(log2(frameRate));
		generateWaveForm();
	}

	private void ampChange()
	{
		amp=1;
		try { amp=Integer.parseInt(amplitudeField.getText());			
		} catch (NumberFormatException nfe)
		{ amplitudeField.setText(String.valueOf(amp)); }
		ampSlider.setValue(amp);
		
		dcOffset=0.0;
		try { dcOffset=Double.parseDouble(dcOffsetField.getText());
		} catch (NumberFormatException nfe)
		{ dcOffsetField.setText(String.valueOf(dcOffset)); }

		generateWaveForm();
	}
	
	private void channelChange()
	{
		numChan=Integer.parseInt(//s.substring(1));
			channelBox.getSelectedItem().toString());
		fixChannels=true;
		generateWaveForm();
	}

	//
	// 10/13/2003  INB  Ensure that the close doesn't destroy the archive.
	// 04/03/2003  WHF  Resets source when number of channels changes.
	//
	private void generateWaveForm()
	{
		boolean running=dataThread.isRunning();

//System.err.print("Timer.stop()...");
		dataThread.stop();
//System.err.println("done.");

		dataThread.setRate(frameRate,
//			delayControlCheckBox.getState());
			true);  // Closed loop always used.
			
		dataHandler.generateWaveForm();

		// Create & define channel names:
		if (fixChannels)
		{
			try {
			//if (connected) sb.Detach(); // mjm
			if (connected) sb.CloseRBNBConnection();
			cm.Clear();
			//if (doTimeofday) cm.PutTimeAuto("timeofday");
			chanNames=new String[numChan];
			for (int ii=0; ii<numChan; ii++)
			{
				chanNames[ii]="c"+ii;
				try {
				cm.Add(chanNames[ii]);
				} catch (Exception e)
				{ e.printStackTrace(); } // never happen
			}
			fixChannels=false;
			if (connected) 
				sb.OpenRBNBConnection(server,archiveDialog.getClientName());
			} catch (SAPIException se)
			{
				se.printStackTrace();
				onDisconnect();
				setCursor(Cursor.getDefaultCursor());
				JOptionPane.showMessageDialog(
					this,
					"Cannot reconnect to server "+server+".",
					"Error:",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		if (running)
		{
			dataThread.start();
		}
	}
	
	// Setting functions, for command-line:
	public void setStartMinimized(boolean startMin) { this.startMin=startMin; }
	public void setDoConnect(boolean doConnect) { this.doConnect=doConnect; }
	public void setHostname(String host) { this.server=host; }
	public void setSourceName(String source) 
	{ 
		archiveDialog.setClientName(source); 
		setTitle(archiveDialog.getClientName()+": not connected");
	}
	public void setDoStart(boolean doStart) { this.doStart=doStart; }
	public void setFreq(int freq) 
	{
		freqField.setText(String.valueOf(freq));
		freqChange();
	}
	public void setSampleRate(int rate) 
	{
		samplingRateField.setText(String.valueOf(rate));
		freqChange();
	}
	public void setFrameRate(int rate) 
	{
		framesField.setText(String.valueOf(rate));
		freqChange();
	}
	public void setAmplitude(int amplitude)
	{
		amplitudeField.setText(String.valueOf(amplitude));
		ampChange();
	}
	
	/**
	  * Change the signal's wave form type.
	  * <ol start="0"><li>Sinudoidal</li><li>Square</li><li>Triangular</li>
	  * </ol>
	  */
	public void setWaveForm(int waveForm)
	{
		try {
		Enumeration e=waveFormButtons.getElements();
		Object o=null;
		for (int ii=0; ii<=waveForm; ++ii)
			o=e.nextElement();
			
		waveFormButtons.setSelected(((AbstractButton) o).getModel(), true);
		generateWaveForm();
	} catch (Exception e) { e.printStackTrace(); }			
	}

	/**
	 * sets auto timestamping
	 */
	public void setTimeofday() {
		doTimeofday=true;
System.err.println("WARNING: using local timeofday timestamps, data will not be continuous.");
	}
		
	public void setChannels(int numChan)
	{
		if (numChan==0) return;
		for (int ii=1; ii<channelBox.getItemCount(); ++ii)
		{
			int val=Integer.parseInt(channelBox.getItemAt(ii).toString());
			if (val==numChan) 
			{
				channelBox.setSelectedIndex(ii);
				channelChange();
				break;
			}
			if (val>numChan)
			{
				channelBox.insertItemAt(String.valueOf(numChan),ii);
				channelBox.setSelectedIndex(ii);
				channelChange();
				return;
			}
		}

		channelBox.addItem(String.valueOf(numChan));			
		channelBox.setSelectedIndex(channelBox.getItemCount()-1);
		channelChange();
	}
	
	public void setDCOffset(double dc)
	{
		dcOffsetField.setText(String.valueOf(dc));
		ampChange();
	}
	
	public void setDataHandler(DataHandler dh)
	{
		dataHandler=dh;
		generateWaveForm();
	}
	
	ActionListener typeActionListener=new ActionListener()
	{
		public void actionPerformed(ActionEvent ae)
		{
			String type=ae.getActionCommand().toLowerCase();
			if ("double".equals(type))
				setDataHandler(doubleHandler);
			else if ("float".equals(type))
				setDataHandler(floatHandler);
			else if ("long".equals(type))
				setDataHandler(longHandler);
			else if ("int".equals(type))
				setDataHandler(intHandler);
			else if ("short".equals(type))
				setDataHandler(shortHandler);
			else if ("byte".equals(type))
				setDataHandler(byteHandler);
			else throw new RuntimeException();
		}
	};
	
	private static abstract class DataHandler
	{
		abstract void generateWaveForm();
		abstract void putData(ChannelMap cm, int index) throws SAPIException;
	}
	
	private class DoubleHandler extends DataHandler
	{
		double[] output=new double[0];
		
		void generateWaveForm()
		{
			int npts=sampleRate/freq;
			double [] data=new double[npts];

			String s=waveFormButtons.getSelection().getActionCommand();
			if ("Sine".equals(s))
			{
				for (int ii=0; ii<npts; ii++)
					data[ii]=(double) (amp*Math.sin(
						2*Math.PI*ii/npts)+dcOffset);
			}
			else if ("Square".equals(s))
			{
				int ii;
				for (ii=0; ii<npts/2; ii++)
					data[ii]=(double) (amp+dcOffset);
				for (ii=npts/2; ii<npts; ii++)
					data[ii]=(double) (-amp+dcOffset);
			}
			else if ("Sawtooth".equals(s))
			{
				int ii, qtr=npts/4;
				double step=((double)amp)/qtr, dc=(double) dcOffset;
				
				for (ii=0; ii<qtr; ii++)
					data[ii]=step*ii+dc;
				for (ii=qtr; ii<qtr*3; ii++)
					data[ii]=amp-step*(ii-qtr)+dc;
				for (ii=qtr*3; ii<npts; ii++)
					data[ii]=-amp+step*(ii-qtr*3)+dc;
			}
			else
			{
				System.err.println("Unrecognized waveform!");
				System.exit(1);
			}
	
			// Copy the cycles:
			int cyclesPerFrame=freq/frameRate;
			if (output.length!=npts*cyclesPerFrame)
				output=new double[npts*cyclesPerFrame];
			for (int ii=0; ii<cyclesPerFrame; ii++)
				System.arraycopy(data,0,output,ii*npts,npts);
		}
		
		void putData(ChannelMap cm, int index) throws SAPIException
		{
			cm.PutDataAsFloat64(index,output);
		}
	}
	private DoubleHandler doubleHandler=new DoubleHandler();
	
	private class FloatHandler extends DataHandler
	{
		float[] output=new float[0];
		
		void generateWaveForm()
		{
			int npts=sampleRate/freq;
			float [] data=new float[npts];

			String s=waveFormButtons.getSelection().getActionCommand();
			if ("Sine".equals(s))
			{
				for (int ii=0; ii<npts; ii++)
					data[ii]=(float) (amp*Math.sin(
						2*Math.PI*ii/npts)+dcOffset);
			}
			else if ("Square".equals(s))
			{
				int ii;
				for (ii=0; ii<npts/2; ii++)
					data[ii]=(float) (amp+dcOffset);
				for (ii=npts/2; ii<npts; ii++)
					data[ii]=(float) (-amp+dcOffset);
			}
			else if ("Sawtooth".equals(s))
			{
				int ii, qtr=npts/4;
				float step=((float)amp)/qtr, dc=(float) dcOffset;
				
				for (ii=0; ii<qtr; ii++)
					data[ii]=step*ii+dc;
				for (ii=qtr; ii<qtr*3; ii++)
					data[ii]=amp-step*(ii-qtr)+dc;
				for (ii=qtr*3; ii<npts; ii++)
					data[ii]=-amp+step*(ii-qtr*3)+dc;
			}
			else
			{
				System.err.println("Unrecognized waveform!");
				System.exit(1);
			}
	
			// Copy the cycles:
			int cyclesPerFrame=freq/frameRate;
			if (output.length!=npts*cyclesPerFrame)
				output=new float[npts*cyclesPerFrame];
			for (int ii=0; ii<cyclesPerFrame; ii++)
				System.arraycopy(data,0,output,ii*npts,npts);
		}
		
		void putData(ChannelMap cm, int index) throws SAPIException
		{
			cm.PutDataAsFloat32(index,output);
		}
	}
	private FloatHandler floatHandler=new FloatHandler();
	private DataHandler dataHandler=floatHandler;
	
	private class LongHandler extends DataHandler
	{
		long[] output=new long[0];
		
		void generateWaveForm()
		{
			int npts=sampleRate/freq;
			long [] data=new long[npts];

			String s=waveFormButtons.getSelection().getActionCommand();
			if ("Sine".equals(s))
			{
				for (int ii=0; ii<npts; ii++)
					data[ii]=(long) (amp*Math.sin(
						2*Math.PI*ii/npts)+dcOffset);
			}
			else if ("Square".equals(s))
			{
				int ii;
				long mag=(long) (amp+dcOffset);
				for (ii=0; ii<npts/2; ii++)
					data[ii]=mag;
				for (ii=npts/2; ii<npts; ii++)
					data[ii]=-mag;
			}
			else if ("Sawtooth".equals(s))
			{
				int ii, qtr=npts/4;
				double step=((double)amp)/qtr, dc=(double) dcOffset;
				
				for (ii=0; ii<qtr; ii++)
					data[ii]=(long) (step*ii+dc);
				for (ii=qtr; ii<qtr*3; ii++)
					data[ii]=(long) (amp-step*(ii-qtr)+dc);
				for (ii=qtr*3; ii<npts; ii++)
					data[ii]=(long) (-amp+step*(ii-qtr*3)+dc);
			}
			else
			{
				System.err.println("Unrecognized waveform!");
				System.exit(1);
			}
	
			// Copy the cycles:
			int cyclesPerFrame=freq/frameRate;
			if (output.length!=npts*cyclesPerFrame)
				output=new long[npts*cyclesPerFrame];
			for (int ii=0; ii<cyclesPerFrame; ii++)
				System.arraycopy(data,0,output,ii*npts,npts);
		}
		
		void putData(ChannelMap cm, int index) throws SAPIException
		{
			cm.PutDataAsInt64(index,output);
		}
	}
	private LongHandler longHandler=new LongHandler();

	private class IntHandler extends DataHandler
	{
		int[] output=new int[0];
		
		void generateWaveForm()
		{
			int npts=sampleRate/freq;
			int [] data=new int[npts];

			String s=waveFormButtons.getSelection().getActionCommand();
			if ("Sine".equals(s))
			{
				for (int ii=0; ii<npts; ii++)
					data[ii]=(int) (amp*Math.sin(
						2*Math.PI*ii/npts)+dcOffset);
			}
			else if ("Square".equals(s))
			{
				int ii;
				int mag=(int) (amp+dcOffset);
				for (ii=0; ii<npts/2; ii++)
					data[ii]=mag;
				for (ii=npts/2; ii<npts; ii++)
					data[ii]=-mag;
			}
			else if ("Sawtooth".equals(s))
			{
				int ii, qtr=npts/4;
				double step=((double)amp)/qtr, dc=(double) dcOffset;
				
				for (ii=0; ii<qtr; ii++)
					data[ii]=(int) (step*ii+dc);
				for (ii=qtr; ii<qtr*3; ii++)
					data[ii]=(int) (amp-step*(ii-qtr)+dc);
				for (ii=qtr*3; ii<npts; ii++)
					data[ii]=(int) (-amp+step*(ii-qtr*3)+dc);
			}
			else
			{
				System.err.println("Unrecognized waveform!");
				System.exit(1);
			}
	
			// Copy the cycles:
			int cyclesPerFrame=freq/frameRate;
			if (output.length!=npts*cyclesPerFrame)
				output=new int[npts*cyclesPerFrame];
			for (int ii=0; ii<cyclesPerFrame; ii++)
				System.arraycopy(data,0,output,ii*npts,npts);
		}
		
		void putData(ChannelMap cm, int index) throws SAPIException
		{
			cm.PutDataAsInt32(index,output);
		}
	}
	private IntHandler intHandler=new IntHandler();

	private class ShortHandler extends DataHandler
	{
		short[] output=new short[0];
		
		void generateWaveForm()
		{
			int npts=sampleRate/freq;
			short [] data=new short[npts];

			String s=waveFormButtons.getSelection().getActionCommand();
			if ("Sine".equals(s))
			{
				for (int ii=0; ii<npts; ii++)
					data[ii]=(short) (amp*Math.sin(
						2*Math.PI*ii/npts)+dcOffset);
			}
			else if ("Square".equals(s))
			{
				int ii;
				short mag=(short) (amp+dcOffset),
					nmag=(short) -mag; // necessary because negation expands 
									//  type to int for some reason.  Note that
									// -int doesn't become long...
				for (ii=0; ii<npts/2; ii++)
					data[ii]=mag;
				for (ii=npts/2; ii<npts; ii++)
					data[ii]=nmag;
			}
			else if ("Sawtooth".equals(s))
			{
				int ii, qtr=npts/4;
				double step=((double)amp)/qtr, dc=(double) dcOffset;
				
				for (ii=0; ii<qtr; ii++)
					data[ii]=(short) (step*ii+dc);
				for (ii=qtr; ii<qtr*3; ii++)
					data[ii]=(short) (amp-step*(ii-qtr)+dc);
				for (ii=qtr*3; ii<npts; ii++)
					data[ii]=(short) (-amp+step*(ii-qtr*3)+dc);
			}
			else
			{
				System.err.println("Unrecognized waveform!");
				System.exit(1);
			}
	
			// Copy the cycles:
			int cyclesPerFrame=freq/frameRate;
			if (output.length!=npts*cyclesPerFrame)
				output=new short[npts*cyclesPerFrame];
			for (int ii=0; ii<cyclesPerFrame; ii++)
				System.arraycopy(data,0,output,ii*npts,npts);
		}
		
		void putData(ChannelMap cm, int index) throws SAPIException
		{
			cm.PutDataAsInt16(index,output);
		}
	}
	private ShortHandler shortHandler=new ShortHandler();

	private class ByteHandler extends DataHandler
	{
		byte[] output=new byte[0];
		
		void generateWaveForm()
		{
			int npts=sampleRate/freq;
			byte [] data=new byte[npts];

			String s=waveFormButtons.getSelection().getActionCommand();
			if ("Sine".equals(s))
			{
				for (int ii=0; ii<npts; ii++)
					data[ii]=(byte) (amp*Math.sin(
						2*Math.PI*ii/npts)+dcOffset);
			}
			else if ("Square".equals(s))
			{
				int ii;
				byte mag=(byte) (amp+dcOffset),
					nmag=(byte) -mag;  // see short for why
				
				for (ii=0; ii<npts/2; ii++)
					data[ii]=mag;
				for (ii=npts/2; ii<npts; ii++)
					data[ii]=nmag;
			}
			else if ("Sawtooth".equals(s))
			{
				int ii, qtr=npts/4;
				double step=((double)amp)/qtr, dc=(double) dcOffset;
				
				for (ii=0; ii<qtr; ii++)
					data[ii]=(byte) (step*ii+dc);
				for (ii=qtr; ii<qtr*3; ii++)
					data[ii]=(byte) (amp-step*(ii-qtr)+dc);
				for (ii=qtr*3; ii<npts; ii++)
					data[ii]=(byte) (-amp+step*(ii-qtr*3)+dc);
			}
			else
			{
				System.err.println("Unrecognized waveform!");
				System.exit(1);
			}
	
			// Copy the cycles:
			int cyclesPerFrame=freq/frameRate;
			if (output.length!=npts*cyclesPerFrame)
				output=new byte[npts*cyclesPerFrame];
			for (int ii=0; ii<cyclesPerFrame; ii++)
				System.arraycopy(data,0,output,ii*npts,npts);
		}
		
		void putData(ChannelMap cm, int index) throws SAPIException
		{
			cm.PutDataAsInt8(index,output);
		}
	}
	private ByteHandler byteHandler=new ByteHandler();
	

	private class DataThread implements Runnable
	{
		// INB 05/30/2001 - added these for time stamping.
		private double currentTime = 0.,
			       duration = 0.;

		private volatile boolean running=false,
					 stop=false,
					 doResetClock=true;

		// Closed loop timing parameters.
		private boolean closedLoop=true;
		private static final int nSamples=10;
		private static final double	K=1e-6,
						Td=0.0,
						Ti=0.01,
						alpha=0.9;
		private double c1, c2, d, q1, q2;

		private volatile int
			millis,		// delay of work loop, ms
			nanos,
			counter,	// counts of work loop
			slowCount,	// counts of tracking (1 s. update) loop
			monitorCount;	// counts of monitor loop

		// Current delay of work loop, used to calc millis/nanos:
		private double fastDelay;

		private int periodM, periodN;

		private MonitorThread monitorThread=new MonitorThread();
		private Timer timeCount;
		private CountListener countListener = new CountListener();

		DataThread(double rate)
		{
			setRate(rate,true);
			timeCount=new Timer(1000,countListener);
		}

		void setRate(double rate, boolean _closedLoop)
		{
			closedLoop=_closedLoop;
//System.err.println("Rate: "+rate);
			// Set up period for MonitorThread:
			double delay=nSamples/rate;
			double 	ms=delay*1000.0,
				ns=Math.floor(Math.IEEEremainder(ms,1)*1e6);
			periodM=(int) Math.floor(ms);
			if (ns<0) ns=1e6+ns;
			periodN=(int) ns;
//System.err.println("Monitor: "+periodM+"ms + "+periodN+"ns");

			fastDelay = duration = 1.0/rate;
			setDelay();

			double T=delay; // sample time
			c1=K*(Td*(alpha-1)+T/Ti);
			c2=K*(Td*(1-alpha)-alpha*T/Ti);
			d=K*(1+Td);
			// Preset integrator state:
			q1=fastDelay/c1;  q2=0.0;
		}

		private Thread mT, dT;

		void start() { if (!running) (dT=new Thread(this,
			"rbnbSource.dataThread")).start(); }

		void stop()
		{
			if (running)
			{
				// Order of the next two lines is important.
				//  If stop is set before the interrupt, the
				//  dT could conceivably be interrupted during
				//  mT.join().
				dT.interrupt();
				stop=true;
				try { dT.join(); } 
				catch (InterruptedException ie)
				{ ie.printStackTrace(); }
				stop=false;
			}
		}

		boolean isRunning() { return running; }
		
		void resetClock() { doResetClock=true; }

		// 03/18/2003  WHF  Changed 'currentTime' so that only initialized 
		//						when Start/Stop button selected on GUI.
		public void run()
		{
			timeCount.start();

			if (doResetClock)
			{
				currentTime=System.currentTimeMillis()/1e3;
				doResetClock=false;
			}

			if (closedLoop) (mT=new Thread(monitorThread)).start();

			running=true;
			java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("HH:mm:ss.SSS");
			java.util.Date date=new java.util.Date();
			while (true)
			{
				try {
				Thread.sleep(millis, nanos);
				} catch (InterruptedException ie)
				{
					break;
				}

				// Do task:
				counter++;
				slowCount++;

				try {
				// WHF Although this will place datapoints into the future, 
				//  it ensures causality in the face of frequent restarts 
				//  with large frame sizes.
				if (!doTimeofday) cm.PutTime(currentTime,duration);
				else {
					long time=System.currentTimeMillis();
					date.setTime(time);
					System.err.println("timestamp "+sdf.format(date));
					cm.PutTime(time/1000.0,0.);
				}
				currentTime+=duration;

				for (int ii=0; ii<chanNames.length; ii++)
				{
					//cm.PutDataAsFloat32(ii,data);
					dataHandler.putData(cm,ii);
					cm.PutMime(ii,MIMETYPE);
				} // end for
				sb.Flush(cm);
				} // end try
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(
						rbnbSource.this,
							"Error placing data in server.",
							"Error:",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace(System.err);
					// Because this makes calls into some
					// Swing components, we have to place
					// it in the event thread:
					SwingUtilities.invokeLater(new Runnable() 
						{ public void run() { onDisconnect(); } });
					break;
				}
//				if (stop) break;  // not needed; use thread
//					signalling (interrupted)
			}
			if (mT!=null)
			{
				mT.interrupt();
				try { mT.join(); } 
				catch (InterruptedException ie)
				{ ie.printStackTrace(); } // if displayed, error
				mT=null;
			}
			timeCount.stop();
			running=false;
		}

		// Called internalally by monitorthread, and by setRate:
		private void setDelay()
		{
			// Bound delay:
			if (fastDelay<0) fastDelay=0.0;
			else if (fastDelay>duration) fastDelay=duration;
			double 	ms=fastDelay*1000.0,
			ns=Math.floor(Math.IEEEremainder(ms,1)*1e6);
			millis=(int) Math.floor(ms);
			if (ns<0) ns=1e6+ns;
			nanos=(int) ns;
		}


		private class MonitorThread implements Runnable
		{
			public void run()
			{
				while (true)
				{
					try {
					Thread.sleep(periodM, periodN);
					} catch (InterruptedException ie) { break; }

					// Here is our control law:
					//fastDelay+=(counter-nSamples)*K;
					double	e=(counter-nSamples),
						q1new=(1+alpha)*q1-alpha*q2+e;

					q2=q1;	q1=q1new;
					fastDelay=c1*q1+c2*q2+d*e;

					setDelay();
					counter=0;
					monitorCount++;
					if (stop) break;
				}
			}
		} // end RmapSource.DataThread.MonitorThread

		private class CountListener implements ActionListener
		{
			public void actionPerformed(ActionEvent ae)
			{
				framesActualField.setText(String.valueOf(slowCount));
				slowCount=0;
//System.err.println(monitorCount+"Delay: "+millis+"ms + "+nanos+"ns");
				monitorCount=0;
			}
		} // end RmapSource.DataThread.CountListener

	} // end RmapSource.DataThread

	private class MouseHandler extends MouseAdapter
	{
		private AbstractButton button;
		public MouseHandler(AbstractButton button) { this.button=button; }
		public void mouseClicked(MouseEvent me)
		{
			button.doClick();
		}
	}
	
	private class ArchiveDialog extends JDialog implements ActionListener
	{
		private JComboBox archiveMode= new JComboBox(archiveStrings);
		private int theMode=0;
		private JTextField archiveFrames=new JTextField("0",7),
			clientNameField=new JTextField(CLIENTNAMEDEFAULT,7);
		private int theFrames=0;		
		private boolean okhit=false;
		private String clientName=CLIENTNAMEDEFAULT;
		ArchiveDialog(Frame parent)
		{
			super(parent,"Set source options:");
			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent we)
				{ okhit=true; setVisible(false); }});
			Container cp=getContentPane(); 
			JLabel jl;
			KeyAdapter ka=(new KeyAdapter() {
				public void keyPressed(KeyEvent ke)
				{ System.err.println(ke); } });
			setModal(true);
			GridBagLayout gbl=new GridBagLayout();
			cp.setLayout(gbl);
			GridBagConstraints gbc=new GridBagConstraints();
			gbc.gridwidth=1;
			gbc.gridheight=1;
			gbc.insets.left=gbc.insets.right=5;

			gbc.insets.top=10;
			gbc.gridx=0;  gbc.gridy+=gbc.gridheight;
			gbc.anchor=gbc.EAST;
			jl=new JLabel("Source Name:");
			gbl.setConstraints(jl,gbc);
			cp.add(jl);
			gbc.gridx=1;
			gbc.anchor=gbc.CENTER;
			gbl.setConstraints(clientNameField,gbc);
			cp.add(clientNameField);

			gbc.insets.top=5;
			gbc.gridx=0;  gbc.gridy+=gbc.gridheight;
			gbc.weightx=gbc.weighty=1.0;
			gbc.anchor=gbc.EAST;
			jl=new JLabel("Mode:");
			gbl.setConstraints(jl,gbc);
			cp.add(jl);
			gbc.gridx=1;
			gbc.anchor=gbc.CENTER;
//			archiveMode.addKeyListener(ka);
			gbl.setConstraints(archiveMode,gbc);
			cp.add(archiveMode);

			gbc.gridx=0;  gbc.gridy+=gbc.gridheight;
			gbc.anchor=gbc.EAST;
			jl=new JLabel("Frames:");
			gbl.setConstraints(jl,gbc);
			cp.add(jl);
			gbc.gridx=1;
			gbc.anchor=gbc.CENTER;
			gbl.setConstraints(archiveFrames,gbc);
			cp.add(archiveFrames);

			gbc.insets.top=gbc.insets.bottom=10;
			gbc.gridx=0;  gbc.gridy+=gbc.gridheight;
			JButton b=new JButton("Ok");
			b.addActionListener(this);
			getRootPane().setDefaultButton(b);
			gbl.setConstraints(b,gbc);
			cp.add(b);
			gbc.gridx=1;
			b=new JButton("Cancel");
			b.addActionListener(this);
			b.setMnemonic(KeyEvent.VK_ESCAPE);
			gbl.setConstraints(b,gbc);
			cp.add(b);
			
//			setBounds(300,200,200,150);
			pack();
			setLocation(300,200);
			getRootPane().addKeyListener(ka);
		}
		String getArchiveMode() 
		{ return archiveStrings[theMode]; }
		int getArchiveSize()
		{ 
			return theFrames;
		}
		boolean okHit()
		{ return okhit; }
		String getClientName() { return clientName; }
		void setClientName(String clientName) 
		{
			this.clientName=clientName;
			clientNameField.setText(clientName);
		} 
		public void actionPerformed(ActionEvent ae) 
		{ 
			if ("Ok".equals(ae.getActionCommand()))
			{
				theMode=archiveMode.getSelectedIndex();
				clientName=clientNameField.getText();
				try {
				theFrames=Integer.parseInt(archiveFrames
					.getText());
				archiveFrames.setText(String
					.valueOf(theFrames));
				} catch ( NumberFormatException nfe)
				{
					theFrames=0; 
					JOptionPane.showMessageDialog(
						this,
						"Invalid frame size.",
						"Error:",
						JOptionPane.ERROR_MESSAGE);
					archiveFrames.setText("0");
					return;
				}
				okhit=true;
			}
			else // Cancel
			{
				archiveMode.setSelectedIndex(theMode);
				archiveFrames.setText(String
					.valueOf(theFrames));
				okhit=false;
			}
					
		setVisible(false); }
	}
			
			
} // end RmapSource

