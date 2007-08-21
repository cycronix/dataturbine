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
  ***								***
  ***	Name :	TwoWayServerDlg			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   The TwoWayServerDlg is a dialog in which the user may***
  ***   enter an input host and port and (optionally) an output ***
  ***   host and port.  This class is built around the class    ***
  ***   COM.Creare.Utility.HostAndPortDialog, as it stood on    ***
  ***   January 22, 2001.                                       ***
  ***								***
  ***	Modification History:                                   ***
  ***	10/07/2004	JPW	Upgrade from the package	***
  ***				COM.Creare.RBNB.Widgets to be	***
  ***				in the RBNB V2 Player.		***
  ***	10/13/2004	JPW	Cache size is be hardwired to 1;***
  ***				take the cache size text field	***
  ***				off of this dialog.  (The RBNB	***
  ***				Server alows for data moving	***
  ***				backward in time only when the	***
  ***				cache size is 1 and there is no	***
  ***				archive.)			***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.Utility;

public class TwoWayServerDlg extends Dialog
    implements ActionListener, ItemListener, WindowListener {
    
    private TextField inNameTF = null,
	              outNameTF = null,
	              inPortTF = null,
	              outPortTF = null;
    // JPW 10/13/2004: No more cache size text field
    // private TextField cacheSizeTF = null;
    private Checkbox  outEqInCB = null;
    private BorderedPanel outBPan = null;
    private Label outLabel = null;
    
    public String inMachine = "localhost",
	          outMachine = null;
    public int    inPort    = 3333,
	          outPort = -1,
	          cacheSize = 1;
    public int    state;
    public static final int OK = 1;
    public static final int CANCEL = 2;
    
    private InfoDialog infoDialog = null;
    private boolean validityChecks = true;
   
/*
  *****************************************************************
  ***								***
  ***	Name :	TwoWayServerDlg			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/  
    public TwoWayServerDlg (Frame parent,
			    String inMachineI,
			    int inPortI,
			    int cacheSizeI) {
	this(parent, inMachineI, inPortI, null, -1, true, cacheSizeI);
    }
 
/*
  *****************************************************************
  ***								***
  ***	Name :	TwoWayServerDlg			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/  
    public TwoWayServerDlg (Frame parent,
			    String inMachineI,
			    int inPortI,
			    String outMachineI,
			    int outPortI,
			    int cacheSizeI) {
	this(parent, inMachineI, inPortI, outMachineI, outPortI, false, cacheSizeI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	TwoWayServerDlg			        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  ***	UCB 05/24/01 - added new argument: boolean inEqualOut,  ***
  ***		       to indicate whether the input and output ***
  ***                  DataTurbines are the same.               ***
  ***								***
  ***	JPW 10/13/04 - Cache size is be hardwired to 1; take	***
  ***		       the cache size text field off of this	***
  ***		       dialog.  (The RBNB Server alows for data	***
  ***		       moving backward in time only when the	***
  ***		       cache size is 1 and there is no archive)	***
  ***								***
  *****************************************************************
*/
    public TwoWayServerDlg (Frame parent,
			    String inMachineI,
			    int inPortI,
			    String outMachineI,
			    int outPortI,
			    boolean inEqualOut,
			    int cacheSizeI)
    {
        super(parent, true);
	setTitle("Specify Servers");
	setFont(new Font("Dialog", Font.PLAIN, 12));
	setBackground(Color.lightGray);
	
	GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
        
        Label tempLabel = null;
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.anchor = GridBagConstraints.CENTER;
	
	// JPW 10/13/2004: Add row
	int row = 0;
	
        //////////////////////////////////////////
        // Construct Input Server Bordered Panel
        //////////////////////////////////////////
	GridBagLayout gbl2 = new GridBagLayout();
	Panel inP = new Panel(gbl2);
	
        tempLabel = new Label("Host:Port");
        Utility.add(inP, tempLabel, gbl2, gbc, 0, 0, 1, 1);
        inNameTF = new TextField(20);
        Utility.add(inP, inNameTF, gbl2, gbc, 1, 0, 1, 1);
        tempLabel = new Label(":");
        Utility.add(inP, tempLabel, gbl2, gbc, 2, 0, 1, 1);
        inPortTF = new TextField(7);
        Utility.add(inP, inPortTF, gbl2, gbc, 3, 0, 1, 1);
        
	gbc.anchor = GridBagConstraints.CENTER;
	Utility.add(this, new BorderedPanel(inP, "Input Server"),
		    gbl, gbc, 0, row, 1, 1);
	row++;

	//////////////////////////////////////////////
	// Construct Checkbox for setting output 
	// server equal to input server
	//////////////////////////////////////////////
	outEqInCB = new Checkbox("Set Output Server equal to Input Server");
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.insets = new Insets(5, 5, 5, 5);
	Utility.add(this, outEqInCB, gbl, gbc, 0, row, 1, 1);
	row++;

        //////////////////////////////////////////
        // Construct Output Server Bordered Panel
        //////////////////////////////////////////
	gbc.insets = new Insets(0, 0, 0, 0);
	gbl2 = new GridBagLayout();
	Panel outP = new Panel(gbl2);

        outLabel = new Label("Host:Port");
        Utility.add(outP, outLabel, gbl2, gbc, 0, 0, 1, 1);
        outNameTF = new TextField(20);
        Utility.add(outP, outNameTF, gbl2, gbc, 1, 0, 1, 1);
        tempLabel = new Label(":");
        Utility.add(outP, tempLabel, gbl2, gbc, 2, 0, 1, 1);
        outPortTF = new TextField(7);
        Utility.add(outP, outPortTF, gbl2, gbc, 3, 0, 1, 1);

	gbc.anchor = GridBagConstraints.CENTER;
	outBPan = new BorderedPanel(outP, "Output Server");
	Utility.add(this, outBPan, gbl, gbc, 0, row, 1, 1);
	row++;

	//////////////////////////////////////////
        // Construct Cache Size Panel
        //////////////////////////////////////////
	// JPW 10/13/2004: Cache size will be hardwired to 1; the RBNB Server
	//                 alows for data moving backward in time only when
	//                 the cache size is 1 and there is no archive.
	/*
	gbc.insets = new Insets(0,0,0,0);
	gbl2 = new GridBagLayout();
	Panel cacheP = new Panel(gbl2);

	tempLabel = new Label("Output Cache Size: ");
	tempLabel.setAlignment(Label.RIGHT);
	//gbc.insets = new Insets(5,5,5,5);
	Utility.add(cacheP,tempLabel,gbl2,gbc,0,0,1,1);
	
	cacheSizeTF = new TextField(String.valueOf(cacheSizeI), 8);
	//gbc.insets = new Insets(5,5,5,5);
	Utility.add(cacheP,cacheSizeTF,gbl2,gbc,1,0,1,1);
	
	tempLabel = new Label("frame(s)");
	tempLabel.setAlignment(Label.LEFT);
	//gbc.insets = new Insets(5,5,5,5);
	Utility.add(cacheP,tempLabel,gbl2,gbc,2,0,1,1);

	gbc.anchor = GridBagConstraints.CENTER;
	gbc.insets = new Insets(5,5,5,5);
	Utility.add(this, cacheP, gbl, gbc, 0, row, 1, 1);
	row++;
	*/

	//////////////////////////////////////////
        // Construct OK/Cancel Button Panel
        //////////////////////////////////////////
        Panel buttonPanel = new Panel(new GridLayout(1,2,15,5));
        Button OKbutton = new Button("OK");
        buttonPanel.add(OKbutton);
        Button Cancelbutton = new Button("Cancel");
        buttonPanel.add(Cancelbutton);

        gbc.anchor = GridBagConstraints.CENTER;
        Utility.add(this, buttonPanel, gbl, gbc, 0, row, 1, 1);
	row++;
        
        pack();
        setResizable(false);
        
        //////////////////////
        // ADD EVENT LISTENERS
        //////////////////////
        OKbutton.addActionListener(this);
        Cancelbutton.addActionListener(this);
        inNameTF.addActionListener(this);
        inPortTF.addActionListener(this);
	outEqInCB.addItemListener(this);
	outNameTF.addActionListener(this);
        outPortTF.addActionListener(this);
	// JPW 10/13/2004: No more cache size text field
	// cacheSizeTF.addActionListener(this);
        addWindowListener(this);
        
        ///////////////////////////////
        // Initialize the components
        ///////////////////////////////
        initDialog(inMachineI, inPortI, outMachineI, outPortI, inEqualOut);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	ActionPerformed 		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Respond to user selections.                          ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/ 
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals("OK")) {
            OKAction();

        } else if (evt.getActionCommand().equals("Cancel")) {
	    state = CANCEL;
	    setVisible(false);

        } else if (evt.getSource() instanceof TextField) {
            OKAction();
        }
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	itemStateChanged() 		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Respond to user selections.                          ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/ 
    public void itemStateChanged(ItemEvent ie) {
	if (ie.getSource() instanceof Checkbox) {
	    enableOutput(!outEqInCB.getState());
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	enableOutput() 		                	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   En/disables the output server fields.                ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/ 
    private void enableOutput(boolean en) {
	if (en) {
	    outBPan.setEnabled(true);
	    outBPan.setTitleGrey(false);
	    outLabel.setEnabled(true);
	    outNameTF.setForeground(Color.black);
	    outPortTF.setForeground(Color.black);
	} else {
	    outBPan.setEnabled(false);
	    outBPan.setTitleGrey(true);
	    outLabel.setEnabled(false);
	    outNameTF.setForeground(Color.lightGray);
	    outNameTF.setText(inMachine);
	    outPortTF.setForeground(Color.lightGray);
	    outPortTF.setText(Integer.toString(inPort));
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	OKAction()      		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Check the validity of the entered data, save, and    ***
  ***   go invisible.                                           ***
  ***								***
  ***	Modification History:                                   ***
  ***	10/13/2004	JPW	Took the cache size text field	***
  ***				off of the dialog; don't check	***
  ***				its value in this method anymore***
  ***								***
  *****************************************************************
*/
    private void OKAction() {
	if (!checkValidity(inNameTF, inPortTF)) {
	    return;
	}

	inMachine = inNameTF.getText().trim();
	try {
	    inPort = Integer.parseInt(inPortTF.getText().trim());
	} catch (NumberFormatException e) {
	    inPort = 3333;
	}
	
	if (outEqInCB.getState()) {
	    outMachine = inMachine;
	    outPort = inPort;

	} else {
	    if (!checkValidity(outNameTF, outPortTF)) {
		return;
	    }
	    outMachine = outNameTF.getText().trim();
	    try {
		outPort = Integer.parseInt(outPortTF.getText().trim());
	    } catch (NumberFormatException e) {
		outPort = 3333;
	    }
	}
        
	// JPW 10/13/2004: No more cache size text field
	/*
	if (cacheSizeTF.getText().trim().equals("")) {
	    cacheError();
	    return;
	}
	try {
	    cacheSize = Integer.parseInt(cacheSizeTF.getText().trim());
	} catch (NumberFormatException nfe) {
	    cacheError();
	    return;
	}
	if (cacheSize < 1) {
	    cacheError();
	    return;
	}
	*/
	
        state = OK;
        setVisible(false);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setValidityChecking     	              	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :		                                ***
  ***      Enable or disable validity checking of user input.   ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/       
    public void setValidityChecking (boolean state) {
	validityChecks = state;
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	checkValidity()          		        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Check the validity of the data in the given host and ***
  ***   port TextFields.                                        ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/
    private boolean checkValidity (TextField host,
				   TextField port) {
	if (!validityChecks) {
	    return true;
	}

	String hostStr = host.getText().trim();
	String portStr = port.getText().trim();
	int portInt;

	// check for failure to enter a host or port
	if (hostStr.equals("")) {
            String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a valid host name.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
			       true,
			       "Host Error",
			       strArray);
	    infoDialog.setVisible(true);
	    infoDialog.dispose();
	    return false;
        }
	
        if (portStr.equals("")) {
            String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a valid port number.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
			       true,
			       "Port Error",
			       strArray);
	    infoDialog.setVisible(true);
	    infoDialog.dispose();
            return false;
        }
        
        // make sure there is a valid number in the port field
        try {
            portInt = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port.requestFocus();
            port.selectAll();
            String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a valid port number.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
			       true,
			       "Port Error",
			       strArray);
	    infoDialog.setVisible(true);
	    infoDialog.dispose();
            return false;
        }
        
        if (portInt <= 0) {
            port.requestFocus();
            port.selectAll();
            String[] strArray = new String[1];
	    strArray[0] =
		new String("Must enter a valid port number.");
	    infoDialog =
		new InfoDialog((Frame)(this.getParent()),
			       true,
			       "Port Error",
			       strArray);
	    infoDialog.setVisible(true);
	    infoDialog.dispose();
            return false;
        }

	return true;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	cacheError()      				***
  ***	By   :	U. Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	March, 2002		        		***
  ***								***
  ***	Copyright 2002 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method displays a cache-error message.          ***
  ***								***
  *****************************************************************
*/
  private void cacheError() {
      String[] errStr = new String[]
	  {"Cache size must be a positive, non-zero integer."};

      InfoDialog infoDialog =
	  new InfoDialog((Frame) this.getParent(),
			 true,
			 new String("Cache Error!"),
			 errStr);
      
      // center the dialog inside the parent Frame
      infoDialog.setLocation(Utility.centerRect(infoDialog.getBounds(),
						((Frame) this.getParent()).getBounds()));
      infoDialog.setVisible(true);
      infoDialog.dispose();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	initDialog      		        	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Initialize all components.                           ***
  ***								***
  ***	Modification History:                                   ***
  ***								***
  *****************************************************************
*/ 
    public synchronized void initDialog(String inMachineI,
                                        int inPortI,
					String outMachineI,
					int outPortI,
					boolean inEqOut) {
        
        // Initialize class variables
	if ((inMachineI != null) && (!inMachineI.equals(""))) {
	    inMachine = inMachineI;
	    inPort = inPortI;
	}
        inNameTF.setText(inMachine);
	inPortTF.setText(Integer.toString(inPort));

	if ((outMachineI != null) && (!outMachineI.equals("")) &&
	    !inEqOut) {
	    outMachine = outMachineI;
	    outNameTF.setText(outMachine);
	    outPort = outPortI;
	    outPortTF.setText(Integer.toString(outPort));
	    outEqInCB.setState(false);
	    enableOutput(true);
	} else {
	    outEqInCB.setState(true);
	    enableOutput(false);
	}
        
        // set focus in the inNameTF TextField
        inNameTF.requestFocus();
        
        // center the dialog inside the parent Frame
        setLocation(Utility.centerRect(getBounds(), 
				       getParent().getBounds()));
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Methods to implement the WindowListener interface
    //
    /////////////////////////////////////////////////////////////////////////
    
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowClosing(WindowEvent event) {
        // User has clicked on the small "x" button in the upper right
        // corner of the screen.
	state = CANCEL;
	setVisible(false);
    }
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    
}
