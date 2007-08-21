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


// JPW 04/12/2005: Convert to Swing

// tweak of OrdinateDialot to control image display instead
// of strip chart display
// EMF 11/3//04

// ImageDialog - a dialog box for setting image viewer properties

package com.rbnb.plot;

import java.util.Hashtable;
// import java.awt.Button;
// import java.awt.Checkbox;
// import java.awt.CheckboxGroup;
// import java.awt.Choice;
import java.awt.Container;
// import java.awt.Dialog;
import java.awt.Dimension;
// import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
// import java.awt.Label;
// import java.awt.Panel;
import java.awt.Point;
// import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.rbnb.utility.Utility;

public class ImageDialog extends JDialog implements ActionListener {
    
   public boolean proceed=false;
   //stores values for fetching by calling method
   private Hashtable ht=null;
   private JTextField minField=null;
   private JTextField maxField=null;
   
   public ImageDialog(JFrame parent, Hashtable h, PlotImage pi) {
	
	super(parent,"Image Display Configuration",true);
	
	ht = h;
	
 	//EMF 4/10/07: use Environment font so size can be changed
	setFont(Environment.FONT12);
	GridBagLayout gbl = new GridBagLayout();
	getContentPane().setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill=GridBagConstraints.NONE;
	gbc.anchor=GridBagConstraints.WEST;
	gbc.weightx=100;
	gbc.weighty=100;
	
	Integer minWidth=(Integer)ht.get("minWidth");
	Integer maxNumImages=(Integer)ht.get("maxNumImages");
	
	JLabel minLabel =
	    new JLabel("Min Image Width (pixels) ", SwingConstants.RIGHT);
	JLabel maxLabel =
	    new JLabel("Max Number of Images ", SwingConstants.RIGHT);
	minField=new JTextField(minWidth.toString(), 8);
	maxField=new JTextField(maxNumImages.toString(), 8);
	
	// ROW 1
	gbc.insets = new Insets(15,15,0,5);
	gbc.anchor = GridBagConstraints.WEST;
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0;
        gbc.weighty = 0;
	Utility.add(getContentPane(),minLabel,gbl,gbc,0,0,1,1);
	gbc.insets = new Insets(15,0,0,15);
	Utility.add(getContentPane(),minField,gbl,gbc,1,0,1,1);
	
	// ROW 2
	gbc.insets = new Insets(15,15,0,5);
	gbc.anchor = GridBagConstraints.WEST;
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0;
        gbc.weighty = 0;
	Utility.add(getContentPane(),maxLabel,gbl,gbc,0,1,1,1);
	gbc.insets = new Insets(15,0,0,15);
	Utility.add(getContentPane(),maxField,gbl,gbc,1,1,1,1);
	
	// ROW 3: OK and Cancel buttons
	JPanel lowerPanel=new JPanel();
	lowerPanel.setLayout(new GridLayout(1,2,15,5));
	JButton ok=new JButton("Ok");
	ok.addActionListener(this);
	lowerPanel.add(ok);
	JButton cancel=new JButton("Cancel");
	cancel.addActionListener(this);
	lowerPanel.add(cancel);
	gbc.insets = new Insets(15,15,15,15);
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0;
        gbc.weighty = 0;
	Utility.add(getContentPane(),lowerPanel,gbl,gbc,0,2,2,1);
	
	// Handle the close operation in the windowClosing() method of the
	// registered WindowListener object.  This will get around
	// JFrame's default behavior of automatically hiding the window when
	// the user clicks on the '[x]' button.
	setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseClass());
	
	pack();
	
	//EMF 8/24/05: locate dialog over matching image window
	Point p=pi.getLocation();
	Container c=(Container)pi.getParent();
	while ((c=c.getParent())!=null) {
		Point p2=c.getLocation();
		p.x+=p2.x;
		p.y+=p2.y;
	}
	//Point p=parent.getLocation();
	//p.x+=50;
	//p.y+=100;
	setLocation(p);
    }
   
   public void quitCancel() {
	proceed=false;
	setVisible(false);
	}
   
   public void actionPerformed(ActionEvent e) {
	if (e.getSource() instanceof JButton) {
	   JButton b=(JButton)e.getSource();
	   if (b.getText().equals("Ok")) {
		ht.put("minWidth",new Integer(minField.getText()));
		ht.put("maxNumImages",new Integer(maxField.getText()));
		proceed=true;
		setVisible(false);
		}
	   else if (b.getText().equals("Cancel")) {
		quitCancel();
		}
	   }
	}
   
   public Hashtable getParameters() {
	return ht;
	}
	
   class CloseClass extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	   quitCancel();
	   }
	}
   
   }

