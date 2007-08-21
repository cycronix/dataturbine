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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rbnb.utility.Utility;

/******************************************************************************
 * Enter Server shortcut configuration information: destination address,
 * shortcut name, and cost.
 * <p>
 *
 * @author John P. Wilson
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 02/12/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/11/2002  JPW	Created.
 *
 */

public class ShortcutDialog extends JDialog
                            implements ActionListener,
				       WindowListener
{
    
    /**
     * TextField component which contains the address of this shortcut's
     * destination server.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private JTextField destinationAddress = null;
    
    /**
     * The registered name of this shortcut.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private JTextField name = null;
    
    /**
     * The cost associated with moving data via this shortcut.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private JTextField cost = null;
    
    /**
     * OK button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private JButton okButton = null;
    
    /**
     * Cancel button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private JButton cancelButton = null;
    
    /**
     * Constant indicating that the user has hit the OK button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    public static final int OK = 1;
    
    /**
     * Constant indicating that the user has hit the Cancel button.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    public static final int CANCEL = 2;
    
    /**
     * Saves which button the user has pressed: OK or Cancel.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    public int state = CANCEL;
    
    /**
     * Object to store the shortcut data: the destination
     * address, the shortcut name, and the cost.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/12/2002
     */
    public ShortcutData shortcutData = null;
    
    /**************************************************************************
     * Create a dialog which allows a user to configure a server shortcut.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param parentI  this dialog's parent frame
     * @param modalI  should this dialog be modal?
     * @param destinationAddressI  address of shortcut's destination server
     * @param nameI registered name of the shortcut
     * @param costI cost associated with moving data via this shortcut
     * @since V2.0
     * @version 02/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2002  JPW  Created.
     *
     */
    
    public ShortcutDialog(Frame parentI,
                          boolean modalI,
			  String destinationAddressI,
			  String nameI,
			  double costI)
    {
        
        super(parentI, "Server Shortcut", modalI);
        
        Label tempLabel;
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setBackground(Color.lightGray);
        
        GridBagLayout gbl = new GridBagLayout();
        getContentPane().setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 100;
        gbc.weighty = 100;
        
        //////////////////////
        // Destination Address
        //////////////////////
        
        tempLabel = new Label("Destination Address",Label.LEFT);
        gbc.insets = new Insets(15,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,0,1,1);
        
        destinationAddress = new JTextField(20);
	if (destinationAddressI != null) {
	  destinationAddress.setText(destinationAddressI);
	}
        gbc.insets = new Insets(15,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),destinationAddress,gbl,gbc,1,0,1,1);
        
        ////////////////
        // Shortcut Name
        ////////////////
        
        tempLabel = new Label("Shortcut Name",Label.LEFT);
        gbc.insets = new Insets(15,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,1,1,1);
        
        name = new JTextField(20);
	if (nameI != null) {
	  name.setText(nameI);
	}
        gbc.insets = new Insets(15,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),name,gbl,gbc,1,1,1,1);
        
        ////////////////
        // Shortcut Name
        ////////////////
        
        tempLabel = new Label("Cost",Label.LEFT);
        gbc.insets = new Insets(15,15,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),tempLabel,gbl,gbc,0,2,1,1);
        
        cost = new JTextField(Double.toString(costI),10);
        gbc.insets = new Insets(15,0,0,15);
        gbc.anchor = GridBagConstraints.WEST;
        Utility.add(getContentPane(),cost,gbl,gbc,1,2,1,1);
        
        //////////
        // Buttons
        //////////
        
        // Put the buttons in a JPanel so they are all the same size
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,15,0));
        okButton = new JButton("OK");
        buttonPanel.add(okButton);
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        // Don't have the buttons resize if the dialog is resized
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipadx = 20;
        gbc.insets = new Insets(15,25,15,25);
        gbc.anchor = GridBagConstraints.CENTER;
        Utility.add(getContentPane(),buttonPanel,gbl,gbc,0,3,2,1);
        
        pack();
        
        // Add event listeners
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        addWindowListener(this);
        
        // Initialize the location of the dialog box
        setLocation(
	    Utility.centerRect(
		getBounds(),
		getParent().getBounds()));
	
    }
    
    /**************************************************************************
     * The action callback method for the ShortcutDialog object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param event  Describes the action that has occurred.
     * @since V2.0
     * @version 02/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2002  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
        
        if (event.getSource() == cancelButton) {
            state = CANCEL;
            shortcutData = null;
            setVisible(false);
        }
        
        else if (event.getSource() == okButton) {
            shortcutData = null;
            // Check destination address
            String destinationAddressStr =
                destinationAddress.getText().trim();
	    if (destinationAddressStr.equals("")) {
	        JOptionPane.showMessageDialog(
		    this,
		    "Destination address field must not be blank.",
		    "Shortcut Error",
		    JOptionPane.ERROR_MESSAGE);
	        return;
	    }
	    // Check shortcut name
	    String nameStr = name.getText().trim();
	    if (nameStr.equals("")) {
	        JOptionPane.showMessageDialog(
		    this,
		    "Name field must not be blank.",
		    "Shortcut Error",
		    JOptionPane.ERROR_MESSAGE);
	        return;
	    }
	    // Check cost
	    String costStr = cost.getText().trim();
	    double costVal = 0.0;
	    if (costStr.equals("")) {
	        JOptionPane.showMessageDialog(
		    this,
		    new String(
		        "Cost must be in the range 1.0 <= X < " +
		        ShortcutData.MAX_COST_NOT_INCLUSIVE),
		    "Shortcut Error",
		    JOptionPane.ERROR_MESSAGE);
	        return;
	    }
	    // Check that the value entered in the cost field is a value
	    try {
		costVal = Double.parseDouble(costStr);
	    } catch (NumberFormatException e) {
		JOptionPane.showMessageDialog(
		    this,
		    new String(
		        "Cost must be in the range 1.0 <= X < " +
		        ShortcutData.MAX_COST_NOT_INCLUSIVE),
		    "Shortcut Error",
		    JOptionPane.ERROR_MESSAGE);
	        return;
	    }
	    if ( (costVal < 1.0) ||
	         (costVal >= ShortcutData.MAX_COST_NOT_INCLUSIVE) )
	    {
		JOptionPane.showMessageDialog(
		    this,
		    new String(
		        "Cost must be in the range 1.0 <= X < " +
		        ShortcutData.MAX_COST_NOT_INCLUSIVE),
		    "Shortcut Error",
		    JOptionPane.ERROR_MESSAGE);
	        return;
	    }
            
            shortcutData =
                new ShortcutData(
                    destinationAddressStr,
                    nameStr,
                    costVal);
            
            state = OK;
            setVisible(false);
        }
        
        return;
        
    }
    
    /**************************************************************************
     * Respond to the event that occurs when the user clicks in the small
     * "[x]" button on the right side of the title bar.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param event  Describes the WindowEvent that has occurred.
     * @since V2.0
     * @version 02/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2002  JPW  Created.
     *
     */
    
    public void windowClosing(WindowEvent event) {
        state = CANCEL;
        setVisible(false);
    }
    
    public void windowOpened(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    
}
