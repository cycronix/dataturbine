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


package com.rbnb.admin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import javax.swing.JOptionPane;

import com.rbnb.api.Source;

import com.rbnb.utility.*;

/******************************************************************************
 * Dialog to allow the user to enter information in order to establish a
 * new data Source which will copy/mirror data from an existing Source.
 * <p>
 *
 * @author John P. Wilson
 * @author Ian A. Brown
 *
 * @since V2.0
 * @version 11/24/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/26/2010  JPW	Moved the definitions of OLDEST, CONTINUOUS, and NOW
 * 			from this class to com.rbnb.api.Mirror, becasue
 * 			these constants are also referenced in com.rbnb.sapi.Control.
 * 03/01/2002  INB	Eliminated the server name fields as they shouldn't be
 *			necessary and may be difficult for the user to figure
 *			out.
 * 06/12/2001  JPW	Disable the "Stop Now" radio button; this doesn't
 *			properly work with either Start time option.
 * 06/12/2001  JPW	Replace using InfoDialog with JOptionPane
 * 06/08/2001  JPW	Change names which include "Host" or "Port" to just
 *			"Address"; add source_Name and destination_Name.
 *			Add archive mode selection.
 * 06/07/2001  JPW	Created (Taken from V1.1 RBNB code)
 *
 */

public class MirrorDialog
    extends Dialog
    implements ActionListener, ItemListener, WindowListener
{
    
    /**
     * Radio button group for setting the start time.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private CheckboxGroup startGroup;
    
    /**
     * Radio button to specify start mirroring from the oldest available data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Checkbox start_OldestCB;
    
    /**
     * Radio button to specify start mirroring from the current time.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Checkbox start_NowCB;
    
    /**
     * Radio button group for setting the stop time.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private CheckboxGroup stopGroup;
    
    /**
     * Radio button to specify stop mirroring at the current time.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Checkbox stop_NowCB;
    
    /**
     * Radio button to specify to continuously mirror data.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Checkbox stop_ContinuousCB;
    
    /**
     * Specify the source Server address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private TextField source_Address;
    
    /**
     * Specify the source data path.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private TextField source_DataPath;
    
    /**
     * Specify the destination Server address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private TextField destination_Address;
    
    /**
     * Specify the destination data path.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private TextField destination_DataPath;
    
    /**
     * Checkbox to specify to match the source's archive and cache sizes.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Checkbox bufferFrames_MatchCB;
    
    /**
     * Label for the cache TextField.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Label bufferFrames_CacheLabel;
    
    /**
     * TextField for specifying number of cache frames in the new source.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private TextField bufferFrames_Cache;
    
    /**
     * Label for the archive TextField.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Label bufferFrames_ArchiveLabel;
    
    /**
     * TextField for specifying number of archive frames in the new source.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private TextField bufferFrames_Archive;
    
    /**
     * Label for the archive mode.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private Label archiveModeLabel;
    
    /**
     * Radio button group for setting the archive mode.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private CheckboxGroup archiveModeGroup;
    
    /**
     * Radio button to specify no archiving.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private Checkbox archiveNoneCB;
    
    /**
     * Radio button to specify the creation of a new archive.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private Checkbox archiveCreateCB;
    
    /**
     * Radio button to specify appending to an existing archive.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    private Checkbox archiveAppendCB;
    
    /**
     * OK button; specifies the user wishes to create the mirror.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Button okButton;
    
    /**
     * Cancel button; specifies the user wishes to cancel the mirror operation.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Button cancelButton;
    
    /**
     * Allow multiple "From" sources?
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private boolean bMultipleSources = false;
    
    /**
     * ScrollPane used when specifying multiple "From" sources.
     * <p>
     * Only used when bMultipleSources is true.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private ScrollPane sourceScrollPane;
    
    /**
     * SelectionPanel used when specifying multiple "From" sources.
     * <p>
     * Only used when bMultipleSources is true.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private SelectionPanel sourceSelectionPanel;
    
    /**
     * Add a new "From" source.
     * <p>
     * Only used when bMultipleSources is true.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Button addButton;
    
    /**
     * Remove the currently selected "From" source.
     * <p>
     * Only used when bMultipleSources is true.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Button removeButton;

    /**
     * Specifies the user's start choice: OLDEST or NOW.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public int startChoice = com.rbnb.api.Mirror.OLDEST;
    
    /**
     * Stores the user's stop choice: NOW or CONTINUOUS.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public int stopChoice = com.rbnb.api.Mirror.CONTINUOUS;
    
    /**
     * Stores the source server address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    public String sourceAddressStr = null;
    
    /**
     * Stores the source data path.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public String sourceDataPathStr = null;
    
    /**
     * Stores the destination server address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    public String destinationAddressStr = null;
    
    /**
     * Stores the destination data path.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public String destinationDataPathStr = null;
    
    /**
     * Stores whether the user wishes to have the new source's archive and
     * cache sizes match the original source's frame sizes.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public boolean bMatchSource = true;
    
    /**
     * Number of cache frames the new source should have.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public long numCacheFrames = -1;
    
    /**
     * Number of archive frames the new source should have.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public long numArchiveFrames = -1;
    
    /**
     * Archive mode.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/08/2001
     */
    public byte archiveMode = Source.ACCESS_NONE;
    
    /**
     * Constant representing that the user hit the OK button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public static final int OK = 1;
    
    /**
     * Constant representing that the user hit the Cancel button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public static final int CANCEL = 2;
    
    /**
     * Stores the user's button hit selection: OK or CANCEL.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public int state = CANCEL;
    
    /**
     * Vector to store information about the "From" source(s) specified by
     * the user.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public Vector multipleSourceInfo = new Vector();
    
    /**************************************************************************
     * Create the Mirror dialog.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @param parent  This dialog's parent.
     * @param modal  Make the dialog modal?
     * @param sourceAddressStrI  Initial value for source_Address.
     * @param sourceDataPathStrI  Initial value for source_DataPath.
     * @param destinationAddressStrI  Initial value for destination_Address.
     * @param destinationDataPathStrI  Initial value for destination_DataPath.
     * @param sourceAddressEnabledI  Enable source_Address?
     * @param sourceDataPathEnabledI  Enable source_DataPath?
     * @param destinationAddressEnabledI  Enable destination_Address?
     * @param destinationDataPathEnabledI  Enable destination_DataPath?
     * @param bMultipleSourcesI  Allow multiple sources?
     * @since V2.0
     * @version 03/01/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/2002  INB	Eliminated server names.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public MirrorDialog(Frame parent,
                        boolean modal,
                        String sourceAddressStrI,
                        String sourceDataPathStrI,
                        String destinationAddressStrI,
                        String destinationDataPathStrI,
		        boolean sourceAddressEnabledI,
		        boolean sourceDataPathEnabledI,
		        boolean destinationAddressEnabledI,
		        boolean destinationDataPathEnabledI,
			long numCacheFramesI,
			long numArchiveFramesI,
			byte archiveModeI,
		        boolean bMultipleSourcesI)
    {
        
        super(parent, modal);
        
	// JPW 7/11/2000: Add bMultipleSources
	bMultipleSources = bMultipleSourcesI;
	
        Label tempLabel;
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
	
	setBackground(Color.lightGray);
	
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        
        int row = 0;
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 100;
        gbc.weighty = 100;
        
	// Create all the dialog controls
	startGroup = new CheckboxGroup();
	start_OldestCB =
	    new Checkbox("Oldest", startGroup, false);
	start_NowCB =
	    new Checkbox("Now", startGroup, true);
	
	stopGroup = new CheckboxGroup();
	stop_NowCB =
	    new Checkbox("Now", stopGroup, false);
	stop_NowCB.setEnabled(false);
	stop_ContinuousCB =
	    new Checkbox("Continuous", stopGroup, true);
	
	// Used when using a "single source only" interface
	source_Address = new TextField(20);
	source_DataPath = new TextField(20);
	
	// JPW 7/11/2000: Add capability to allow the user to specify multiple
	//                "from" data sources.
	sourceScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
	sourceSelectionPanel = new SelectionPanel(new GridLayout(0,1,1,1));
	sourceScrollPane.add(sourceSelectionPanel);
	addButton = new Button("Add");
	removeButton = new Button("Remove");
	
	destination_Address = new TextField(20);
	destination_DataPath = new TextField(20);
	
	bufferFrames_MatchCB = new Checkbox("Match Source",true);
	bufferFrames_CacheLabel = new Label("Cache Frames",Label.LEFT);
	bufferFrames_CacheLabel.setEnabled(false);
	bufferFrames_Cache = new TextField("100",10);
	bufferFrames_Cache.setEnabled(false);
	bufferFrames_ArchiveLabel = new Label("Archive Frames",Label.LEFT);
	bufferFrames_ArchiveLabel.setEnabled(false);
	bufferFrames_Archive = new TextField("0",10);
	bufferFrames_Archive.setEnabled(false);
	archiveModeLabel = new Label("Archive Mode",Label.LEFT);
	archiveModeLabel.setEnabled(false);
	archiveModeGroup = new CheckboxGroup();
	archiveNoneCB = new Checkbox("None", archiveModeGroup, true);
	archiveNoneCB.setEnabled(false);
	archiveCreateCB = new Checkbox("Create", archiveModeGroup, false);
	archiveCreateCB.setEnabled(false);
	archiveAppendCB = new Checkbox("Append", archiveModeGroup, false);
	archiveAppendCB.setEnabled(false);
	
	okButton = new Button("OK");
	cancelButton = new Button("Cancel");
	
	// Row 1
	tempLabel = new Label("To:",Label.LEFT);
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,tempLabel,gbl,gbc,1,row,1,1);
	gbc.insets = new Insets(5,0,0,5);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        Utility.add(this,destination_Address,gbl,gbc,2,row,1,1);
	gbc.fill = GridBagConstraints.NONE;
        row++;
	
	// Row 2
	tempLabel = new Label("Data Path",Label.LEFT);
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,tempLabel,gbl,gbc,1,row,1,1);
	gbc.insets = new Insets(5,0,0,5);
	gbc.fill = GridBagConstraints.HORIZONTAL;
        Utility.add(this,destination_DataPath,gbl,gbc,2,row,1,1);
	gbc.fill = GridBagConstraints.NONE;
        row++;
	
	// JPW 7/11/2000: Rows 3/4: DEPEND ON WHETHER MULTIPLE SOURCES
	//                          ARE ALLOWED OR NOT
	if (bMultipleSources) {
	    tempLabel = new Label("From:",Label.LEFT);
            gbc.insets = new Insets(5,5,0,5);
            Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
	    // sourceScrollPane
	    gbc.insets = new Insets(5,0,0,5);
	    gbc.fill = GridBagConstraints.BOTH;
            Utility.add(this,sourceScrollPane,gbl,gbc,1,row,2,1);
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 100;
            gbc.weighty = 100;
	    gbc.ipadx = 0;
	    row++;
	    // Add/Remove buttons
	    Panel buttonPanel = new Panel(new GridLayout(1,2,2,2));
	    buttonPanel.add(addButton);
            buttonPanel.add(removeButton);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(5,0,0,5);
            Utility.add(this,buttonPanel,gbl,gbc,1,row,2,1);
	    gbc.anchor = GridBagConstraints.WEST;
	    row++;
	}
	else {
	    // Row 4
	    tempLabel = new Label("From:",Label.LEFT);
            gbc.insets = new Insets(5,0,0,5);
            Utility.add(this,tempLabel,gbl,gbc,1,row,1,1);
	    gbc.insets = new Insets(5,0,0,5);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
            Utility.add(this,source_Address,gbl,gbc,2,row,1,1);
	    gbc.fill = GridBagConstraints.NONE;
            row++;
	    
	    // Row 5
	    tempLabel = new Label("Data Path",Label.LEFT);
            gbc.insets = new Insets(5,0,0,5);
            Utility.add(this,tempLabel,gbl,gbc,1,row,1,1);
	    gbc.insets = new Insets(5,0,0,5);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
            Utility.add(this,source_DataPath,gbl,gbc,2,row,1,1);
	    gbc.fill = GridBagConstraints.NONE;
            row++;
	}
	
	// Row 6
	tempLabel = new Label("Start:",Label.LEFT);
        gbc.insets = new Insets(5,5,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,start_NowCB,gbl,gbc,1,row,1,1);
        row++;
	
	// Row 7
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,start_OldestCB,gbl,gbc,1,row,1,1);
        row++;
	
	// Row 8
	tempLabel = new Label("Stop:",Label.LEFT);
        gbc.insets = new Insets(5,5,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,stop_NowCB,gbl,gbc,1,row,1,1);
        row++;
	
	// Row 9
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,stop_ContinuousCB,gbl,gbc,1,row,1,1);
        row++;
	
	// Row 10
	tempLabel = new Label("Buffer Size:",Label.LEFT);
        gbc.insets = new Insets(5,5,0,5);
        Utility.add(this,tempLabel,gbl,gbc,0,row,1,1);
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,bufferFrames_MatchCB,gbl,gbc,1,row,1,1);
        row++;
	
	// Row 11
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,bufferFrames_CacheLabel,gbl,gbc,1,row,1,1);
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,bufferFrames_Cache,gbl,gbc,2,row,1,1);
        row++;
	
	// Row 12
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,bufferFrames_ArchiveLabel,gbl,gbc,1,row,1,1);
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,bufferFrames_Archive,gbl,gbc,2,row,1,1);
        row++;
	
	// Row 13
        gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,archiveModeLabel,gbl,gbc,1,row,1,1);
        row++;
	
	// Row 14
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,archiveNoneCB,gbl,gbc,1,row,1,1);
	row++;
	
	// Row 15
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,archiveCreateCB,gbl,gbc,1,row,1,1);
	row++;
	
	// Row 16
	gbc.insets = new Insets(5,0,0,5);
        Utility.add(this,archiveAppendCB,gbl,gbc,1,row,1,1);
	row++;
	
	// Row 17: OK/Cancel buttons
        // Want to get the 2 bottom buttons the same size:
        // put both buttons in a panel which uses GridLayout (to force all
        // components to the same size) and then add the panel to the dialog
        Panel buttonPanel = new Panel(new GridLayout(1,2,5,5));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5,5,5,5);
        Utility.add(this,buttonPanel,gbl,gbc,0,row,3,1);
        
        setTitle("Mirror Data");
        
        pack();
        
        setResizable(true);
        
        //////////////////////
        // ADD EVENT LISTENERS
        //////////////////////
	
	addButton.addActionListener(this);
	removeButton.addActionListener(this);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
	bufferFrames_MatchCB.addItemListener(this);
        addWindowListener(this);
        
        ////////////////////////
        // INITIALIZE THE DIALOG
        ////////////////////////
        
	if (bMultipleSources) {
	    // Add an initial SourceBorderedPanel to the dialog
	    addSourcePanel();
	}
	
	if (sourceAddressStrI != null) {
	    source_Address.setText(sourceAddressStrI);
	}
	if (sourceDataPathStrI != null) {
	    source_DataPath.setText(sourceDataPathStrI);
	}
	if (destinationAddressStrI != null) {
	    destination_Address.setText(destinationAddressStrI);
	}
	if (destinationDataPathStrI != null) {
	    destination_DataPath.setText(destinationDataPathStrI);
	}
	
	// Set archive mode
	if (archiveModeI == Source.ACCESS_NONE) {
	    archiveModeGroup.setSelectedCheckbox(archiveNoneCB);
	}
	else if (archiveModeI == Source.ACCESS_CREATE) {
	    archiveModeGroup.setSelectedCheckbox(archiveCreateCB);
	}
	else {
	    archiveModeGroup.setSelectedCheckbox(archiveAppendCB);
	}
	
	source_Address.setEnabled(sourceAddressEnabledI);
	source_DataPath.setEnabled(sourceDataPathEnabledI);
	destination_Address.setEnabled(destinationAddressEnabledI);
	destination_DataPath.setEnabled(destinationDataPathEnabledI);
	
        // center the dialog inside the parent Frame
        setLocation(
	    Utility.centerRect(
		getBounds(), getParent().getBounds()));
        
    }
    
    /**************************************************************************
     * Callback for the Checkboxes/Radio buttons.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param event  The ItemEvent that has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void itemStateChanged(ItemEvent event) {
	
	Checkbox item = (Checkbox)event.getSource();
	
	if (item == bufferFrames_MatchCB) {
	    if (item.getState()) {
		bufferFrames_CacheLabel.setEnabled(false);
		bufferFrames_Cache.setEnabled(false);
		bufferFrames_ArchiveLabel.setEnabled(false);
		bufferFrames_Archive.setEnabled(false);
		archiveModeLabel.setEnabled(false);
		archiveNoneCB.setEnabled(false);
		archiveCreateCB.setEnabled(false);
		archiveAppendCB.setEnabled(false);
	    }
	    else {
		bufferFrames_CacheLabel.setEnabled(true);
		bufferFrames_Cache.setEnabled(true);
		bufferFrames_ArchiveLabel.setEnabled(true);
		bufferFrames_Archive.setEnabled(true);
		bufferFrames_Cache.requestFocus();
		archiveModeLabel.setEnabled(true);
		archiveNoneCB.setEnabled(true);
		archiveCreateCB.setEnabled(true);
		archiveAppendCB.setEnabled(true);
	    }
	}
	
    }
    
    /**************************************************************************
     * Handle button events.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param event  The ActionEvent that has occurred.
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void actionPerformed(ActionEvent evt) {
        
	if (evt.getSource() == addButton) {
	    addSourcePanel();
	}
	else if (evt.getSource() == removeButton) {
	    removeSourcePanel();
	}
        else if (evt.getSource() == okButton) {
            okAction();
        }
        else if (evt.getSource() == cancelButton) {
            cancelAction();
        }
        
    }
    
    /**************************************************************************
     * User has hit the "Add" button. Add a new SourceBorderedPanel object
     * to the SelectionPanel.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @since V2.0
     * @version 03/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/04/2002  INB	Eliminated server name.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    private void addSourcePanel() {
	
	SourceInfo sourceInfo = new SourceInfo("","");
	SourceBorderedPanel sbp = new SourceBorderedPanel(sourceInfo,this);
	sourceSelectionPanel.add(sbp);
	
	// Make this new panel the selected one
	sbp.getAddressTextField().requestFocus();
	
	// Redo layout of the scrolling window
	// NEED TO DO THIS BEFORE SETTING THE SCROLL POSITION
	sourceScrollPane.validate();
	sourceScrollPane.doLayout();
	
	// Sleep for a bit to try to get things sync'ed up under Linux
	//  - doesn't seem to help any
	// try { Thread.sleep(500); } catch (Exception e) {}
	
	// If the vertical scrollbar is visible, set its position to 0 so that
	// the user can see the newly added SourceBorderedPanel
	Adjustable hAdj = sourceScrollPane.getHAdjustable();
	Adjustable vAdj = sourceScrollPane.getVAdjustable();
	int hPos,vPos;
	if (hAdj == null) {
	    hPos = 0;
	}
	else {
	    hPos = hAdj.getMinimum();
	    hAdj.setUnitIncrement(50);
	}
	if (vAdj == null) {
	    vPos = 0;
	}
	else {
	    vPos = vAdj.getMaximum() - vAdj.getVisibleAmount();
	    vAdj.setUnitIncrement(50);
	}
	sourceScrollPane.setScrollPosition(hPos,vPos);
	
	// Problems adjusting the scroll position under Linux; even when I
	// specify a legal scroll position, I get Warnings in the console
	// window.
	/*
	System.out.println(
	    "Horizontal: min = " + Integer.toString(hAdj.getMinimum()) +
	    ", max = " + Integer.toString(hAdj.getMaximum()));
	System.out.println(
	    "Vertical: min = " + Integer.toString(vAdj.getMinimum()) +
	    ", max = " + Integer.toString(vAdj.getMaximum()));
	*/
	
    }
    
    /**************************************************************************
     * User has hit the "Remove" button. Remove the selected
     * SourceBorderedPanel object from the SelectionPanel (if there is one).
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    private void removeSourcePanel() {
	
	SourceBorderedPanel selectedPanel =
	    (SourceBorderedPanel)(sourceSelectionPanel.getSelectedPanel());
	
	if (selectedPanel == null) {
	    return;
	}
	
	sourceSelectionPanel.remove(selectedPanel);
	
	// Redo layout of the scrolling window
	sourceScrollPane.validate();
        sourceScrollPane.doLayout();
    }
    
    /**************************************************************************
     * User has hit the "OK" button. Check and store user entered data.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @since V2.0
     * @version 03/01/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/2002  INB	Eliminated server names.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    private void okAction() {
	
	///////////////////////////////////////////////////////////////
	// Check host and data path info for the source and destination
	///////////////////////////////////////////////////////////////
	
	// Save destination information
	destinationAddressStr = destination_Address.getText().trim();
	destinationDataPathStr = destination_DataPath.getText().trim();
	
	// Save source information
	if (!bMultipleSources) {
	    sourceAddressStr = source_Address.getText().trim();
	    sourceDataPathStr = source_DataPath.getText().trim();
	}
	else {
	    multipleSourceInfo = new Vector();
            for (int i=0; i < sourceSelectionPanel.getComponentCount(); ++i) {
                SourceBorderedPanel panel =
                   (SourceBorderedPanel)(sourceSelectionPanel.getComponent(i));
		SourceInfo si = panel.getUpdatedSourceInfo();
		// Weed out cases where address, and data path are blank
		if ( !si.addressStr.equals("") &&
		     !si.dataPathStr.equals("") )
		{
		    multipleSourceInfo.addElement(si);
		}
	    }
	    // Make sure there is as least one entry
	    if (multipleSourceInfo.size() == 0) {
		JOptionPane.showMessageDialog(
		    this,
		    "Must specify at least one \"From\" Source.",
		    "Mirror Error",
		    JOptionPane.ERROR_MESSAGE);
		/*
		String[] strArray = new String[1];
	        strArray[0] =
		    new String("Must specify at least one \"From\" Source.");
	        infoDialog =
		    new InfoDialog((Frame)getParent(),
		                   true,
		                   "Data Error",
		                   strArray);
	        infoDialog.show();
                infoDialog.dispose();
		*/
	        return;
	    }
	}
	
	// Now check the entered data
	if (!bMultipleSources) {
	    int returnVal =
		checkUserAddressDataPath(
		    destinationAddressStr,
		    destinationDataPathStr,
		    sourceAddressStr,
		    sourceDataPathStr);
	    if (returnVal == -1) {
		return;
	    }
	}
	else {
	    for (int i=0; i < multipleSourceInfo.size(); ++i) {
		SourceInfo si = (SourceInfo)(multipleSourceInfo.elementAt(i));
		int returnVal =
		    checkUserAddressDataPath(
			destinationAddressStr,
			destinationDataPathStr,
			si.addressStr,
			si.dataPathStr);
		if (returnVal == -1) {
		    return;
	        }
	    }
	}
	
	////////////////////////////////
	// Save user-entered information
	////////////////////////////////
	
	// Start time information
	Checkbox selection = startGroup.getSelectedCheckbox();
	if (selection == start_OldestCB) {
	    startChoice = com.rbnb.api.Mirror.OLDEST;
	}
	else if (selection == start_NowCB) {
	    startChoice = com.rbnb.api.Mirror.NOW;
	}
	
	// Stop time information
	selection = stopGroup.getSelectedCheckbox();
	if (selection == stop_NowCB) {
	    stopChoice = com.rbnb.api.Mirror.NOW;
	}
	else if (selection == stop_ContinuousCB) {
	    stopChoice = com.rbnb.api.Mirror.CONTINUOUS;
	}
	
	// Buffer frames information
	bMatchSource = bufferFrames_MatchCB.getState();
	if (bMatchSource == false) {
	    
	    try {
            	numCacheFrames =
		    Long.parseLong( bufferFrames_Cache.getText() );
		if (numCacheFrames <= 0) {
		    throw new NumberFormatException("");
		}
            }
            catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(
		    this,
		    "\"Cache Frames\" must be a positive integer" +
                        " greater than zero.",
		    "Mirror Error",
		    JOptionPane.ERROR_MESSAGE);
		/*
            	String[] strArray = new String[1];
		strArray[0] =
		    new String("\"Cache Frames\" must be a positive integer" +
                               " greater than zero.");
		infoDialog =
		    new InfoDialog((Frame)(this.getParent()),
		                   true,
		                   "Data Error",
		                   strArray);
		infoDialog.show();
                infoDialog.dispose();
		*/
		return;
            }
	    
	    try {
		if (bufferFrames_Archive.getText().trim().equals("")) {
		    numArchiveFrames = 0;
		}
		else {
            	    numArchiveFrames =
		        Long.parseLong( bufferFrames_Archive.getText() );
		    if (numArchiveFrames < 0) {
		        throw new NumberFormatException("");
		    }
		}
            }
            catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(
		    this,
		    "\"Archive Frames\" must be a positive integer.",
		    "Mirror Error",
		    JOptionPane.ERROR_MESSAGE);
		/*
            	String[] strArray = new String[1];
		strArray[0] =
		  new String("\"Archive Frames\" must be a positive integer.");
		infoDialog =
		    new InfoDialog((Frame)(this.getParent()),
		                   true,
		                   "Data Error",
		                   strArray);
		infoDialog.show();
                infoDialog.dispose();
		*/
		return;
            }
	    
	    // Save the desired archive mode
	    if (numArchiveFrames == 0) {
		archiveMode = Source.ACCESS_NONE;
	    }
	    else {
		Checkbox archiveSel = archiveModeGroup.getSelectedCheckbox();
		if (archiveSel == archiveNoneCB) {
		    archiveMode = Source.ACCESS_NONE;
		}
		else if (archiveSel == archiveCreateCB) {
		    archiveMode = Source.ACCESS_CREATE;
		}
		else {
		    archiveMode = Source.ACCESS_APPEND;
		}
	    }
	    
	}
	
        state = OK;
        setVisible(false);
	
    }
    
    /**************************************************************************
     * Check the user entered address and data path information for a
     * variety of errors.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @return Integer indicating the validity of the user-entered data.
     *         Return -1 if there is any error; 1 if no error.
     * @since V2.0
     * @version 03/01/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/2002  INB	Eliminated server names.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    private int checkUserAddressDataPath(
	String destinationAddressStrI,
	String destinationDataPathStrI,
	String sourceAddressStrI,
	String sourceDataPathStrI)
    {
	
	if ( (destinationAddressStrI == null) ||
	     (destinationAddressStrI.equals("")) ||
	     (destinationDataPathStrI == null) ||
	     (destinationDataPathStrI.equals("")) ||
	     (sourceAddressStrI == null) ||
	     (sourceAddressStrI.equals("")) ||
	     (sourceDataPathStrI == null) ||
	     (sourceDataPathStrI.equals("")) )
	{
	    JOptionPane.showMessageDialog(
		this,
		"ERROR: null or empty address or data path string.",
		"Mirror Error",
		JOptionPane.ERROR_MESSAGE);
	    /*
	    String[] strArray = new String[1];
	    strArray[0] =
		new String("ERROR: null address or data path string.");
	    infoDialog =
		new InfoDialog((Frame)getParent(),
		               true,
		               "Data Error",
		               strArray);
	    infoDialog.show();
            infoDialog.dispose();
	    */
	    return -1;
	}
	
	if ( destinationAddressStrI.equals(sourceAddressStrI) &&
	     destinationDataPathStrI.equals(sourceDataPathStrI) )
	{
	    JOptionPane.showMessageDialog(
		this,
		"\"From\" and \"To\" Data Paths must be different.",
		"Mirror Error",
		JOptionPane.ERROR_MESSAGE);
	    /*
	    String[] strArray = new String[1];
	    strArray[0] =
	       new String("\"From\" and \"To\" Data Paths must be different.");
	    infoDialog = new InfoDialog((Frame)getParent(),
		                        true,
		                        "Data Error",
		                        strArray);
	    infoDialog.show();
            infoDialog.dispose();
	    */
	    return -1;
	}
	
	// Everything checked out OK!
	return 1;
	
    }
    
    /**************************************************************************
     * User has hit the Cancel button.
     * <p>
     * Set state to CANCEL and make the dialog invisible.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    private void cancelAction() {
	
        state = CANCEL;
        setVisible(false);
	
    }
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  No action is
     * necessary in this case.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowActivated(WindowEvent e) {}
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  No action is
     * necessary in this case.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowClosed(WindowEvent e) {}
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  User has clicked
     * on the small "x" button in the upper right corner of the dialog;
     * interpret this the same as hitting the Cancel button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowClosing(WindowEvent event) {
        cancelAction();
    }
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  No action is
     * necessary in this case.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowDeactivated(WindowEvent e) {}
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  No action is
     * necessary in this case.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowDeiconified(WindowEvent e) {}
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  No action is
     * necessary in this case.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowIconified(WindowEvent e) {}
    
    /**************************************************************************
     * Method defined as part of implementing WindowListener.  No action is
     * necessary in this case.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public void windowOpened(WindowEvent e) {}
    
}
