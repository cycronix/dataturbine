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


package com.rbnb.plot;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.utility.Utility;

/******************************************************************************
 * Dialog to handle exporting data to DataTurbine. Allow the user to specify
 * the Source name and whether they wish to archive.
 * <p>
 *
 * @author John P. Wilson
 *
 * @version 11/29/2006
 */

/*
 * Copyright 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/29/2006  JPW	Created.
 * 12/21/2007  MJM	Tweeked output "frames" for trimByTime mode, changed "Cancel" to "Exit" on popup dialog
 *
 */

public class ExportToDT extends JDialog
                        implements ActionListener, WindowListener
{
    
    private Environment environment = null;
    private Sink sink = null;
    private Source src = null;
    private String exportSourceName = null;
    private boolean bArchive = false;
    
    // GUI fields
    private JTextField sourceNameTF = null;
    private JCheckBox archiveCB = null;
    private JLabel statusLabel = null;
    private JButton exportButton = null;
    private JButton cancelButton = null;
    
    /**************************************************************************
     * Constructor.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/29/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/29/2006  JPW  Created.
     *
     */
    
    public ExportToDT(
    	Frame parentI,
	boolean modal,
	Sink sinkI,
	Environment environmentI)
    {
	super(parentI, "Export to DataTurbine", modal);
	
	environment = environmentI;
	sink = sinkI;
	
	JLabel tempLabel;
        int row = 0;
        
        //EMF 4/10/07: use Environment font so size can be changed
        setFont(Environment.FONT12);
        setBackground(Color.lightGray);
        
        GridBagLayout gbl = new GridBagLayout();
        getContentPane().setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 100;
        gbc.weighty = 100;
        
	/////////////////////
	// ROW 1: Source name
	/////////////////////
	
        tempLabel = new JLabel("Source name",javax.swing.SwingConstants.LEFT);
        gbc.insets = new Insets(10,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,row,1,1);
        
        sourceNameTF = new JTextField(35);
        gbc.insets = new Insets(10,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),sourceNameTF,gbl,gbc,1,row,1,1);
        row++;
        
	//////////////////////////
	// ROW 2: Archive checkbox
	//////////////////////////
	
	archiveCB = new JCheckBox("Archive?",bArchive);
        gbc.insets = new Insets(10,15,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),archiveCB,gbl,gbc,0,row,2,1);
	row++;
	
	////////////////////
	// ROW 3: Status msg
	////////////////////
	
	statusLabel = new JLabel("Status:",javax.swing.SwingConstants.LEFT);
        gbc.insets = new Insets(10,15,0,15);
        gbc.anchor = GridBagConstraints.WEST;
	gbc.fill = GridBagConstraints.HORIZONTAL;
        Utility.add(getContentPane(),statusLabel,gbl,gbc,0,row,2,1);
	row++;
	gbc.fill = GridBagConstraints.NONE;
	
        ///////////////////////////////////
	// ROW 4: Export and Cancel buttons
	///////////////////////////////////
	
        // Put the buttons in a JPanel so they are all the same size
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,10,0));
        exportButton = new JButton("Export");
        buttonPanel.add(exportButton);
        cancelButton = new JButton("Exit");			// "Cancel" confusing means to exit after successful export
        buttonPanel.add(cancelButton);
        // Don't have the buttons resize if the dialog is resized
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipadx = 20;
        gbc.insets = new Insets(10,25,10,25);
        gbc.anchor = GridBagConstraints.CENTER;
        Utility.add(getContentPane(),buttonPanel,gbl,gbc,0,row,2,1);
        
        pack();
        
        // Add event listeners
        exportButton.addActionListener(this);
        cancelButton.addActionListener(this);
        addWindowListener(this);
        
    }
    
    /**************************************************************************
     * Show or hide the dialog based on the value of the given boolean.
     * <p>
     * If we are showing the dialog, center it within the parent frame.
     *
     * @author John P. Wilson
     *
     * @version 11/29/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/29/2006  JPW  Created.
     *
     */
    
    public void setVisible(boolean b) {
	if (b) {
	    // Initialize the location of the dialog box
	    setLocation(
	    	Utility.centerRect(
		    getBounds(),
		    getParent().getBounds()));
	}
	super.setVisible(b);
    }
    
    /**************************************************************************
     * Action event handler.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/29/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/29/2006  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
        if (event.getSource() == cancelButton) {
            setVisible(false);
        }
        
        else if (event.getSource() == exportButton) {
		    // Make sure user has entered a source name
		    exportSourceName = sourceNameTF.getText().trim();
		    if (exportSourceName.equals("")) {
		    	statusLabel.setText("Status: Must enter a source name.");
		    	return;
		    }
		    bArchive = archiveCB.isSelected();
            exportData();
        }
        
        return;
        
    }
    
    /**************************************************************************
     * Export data to the RBNB
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/29/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/29/2006  JPW  Created.
     *
     */
    
private void exportData() {
	
	String srcName = null;
	
	try {
	    ChannelMap inMap=sink.getLastMap();
	    if ( (inMap == null) || (inMap.NumberOfChannels() < 1) ) {
	    	statusLabel.setText("Status: No channels to export");
	    	return;
	    }
	    // if (src!=null) src.CloseRBNBConnection();
	    if (bArchive) {
	    	src = new Source(1000,"create",1000);	// more "frames" for trimbytime mode mjm 12/07
	    				// this shouldnt hurt frame mode as only 1 is put
	    				// this seems to reduce/eliminate duplicate frames being put in trimbytime mode
	    				// still a mystery why it would happen... MJM							
	    } else {
	    	src = new Source(1,"none",0);
	    }
	    src.OpenRBNBConnection(
	    		environment.HOST + ":" + environment.PORT,
	    		exportSourceName);

	    srcName = src.GetClientName();
	    statusLabel.setText("Status: Exporting to " + srcName);
	    System.err.println("\nExport data to " + srcName);
	    ChannelMap outMap=new ChannelMap();

	    for (int i=0;i<inMap.NumberOfChannels();i++) {
	    	String name=inMap.GetName(i);
	    	System.err.print("\tchannel " + name + " exported as ");
	    	name=name.substring(name.lastIndexOf('/')+1);
	    	if (outMap.GetIndex(name) > -1) {
	    		System.err.print("...duplicate name, modifying...");
	    		int j=1;
	    		while (outMap.GetIndex(name+"_"+j)>-1) j++;
	    		name=name+"_"+j;
	    	}
			int j=outMap.Add(name);
			System.err.println(name);
			if (j==i) { //new channel, fill in time and data
//				System.out.println("PutDataRef("+j+","+i+")");
			    outMap.PutTimeRef(inMap,i);
			    outMap.PutDataRef(j,inMap,i);
			}
	    }
	    ChannelMap cmReg=new ChannelMap();
	    for (int i=0;i<outMap.NumberOfChannels();i++) {
	    	cmReg.Add(outMap.GetName(i));
	    }

	    src.Register(cmReg);
	    src.Flush(outMap,true);
	    System.err.println("Exported " + outMap);
	    src.Detach();
	    statusLabel.setText("Status: Finished exporting to " + srcName);
	    
	} catch (Exception e) {
		e.printStackTrace();
		statusLabel.setText("Status: Error exporting to " + srcName);
	}
}
    
    /**************************************************************************
     * Methods to implement WindowListener.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 11/29/2006
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/29/2006  JPW  Created.
     *
     */
    
    // Respond to the event that occurs when the user clicks in the small
    // "[x]" button on the right side of the title bar.
    public void windowClosing(WindowEvent event) {
        setVisible(false);
    }
    public void windowOpened(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    
}

