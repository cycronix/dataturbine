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

// OrdinateDialog - a dialog box for setting ordinate range and scaling

package com.rbnb.plot;

import java.util.Hashtable;
// import java.awt.Button;
// import java.awt.Checkbox;
// import java.awt.CheckboxGroup;
// import java.awt.Choice;
// import java.awt.Dialog;
import java.awt.Container;
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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class OrdinateDialog extends JDialog
                            implements ActionListener, ItemListener
{
   public boolean proceed=false;
   private Hashtable ht=null;	   //stores values for fetching by calling method
   private JLabel divLabel=null;
   private JComboBox divChoice=null;
   private JLabel minLabel=null;
   private JTextField minField=null;
   private JLabel maxLabel=null;
   private JTextField maxField=null;
   private JRadioButton autoUD_CB=null;
   private JRadioButton autoU_CB=null;
   private JRadioButton manual_CB=null;
   
   public OrdinateDialog(JFrame parent, Hashtable h, PlotContainer pc) {
	
	super(parent,"Y Axis Configuration",true);
	
	ht=h;
	
	//EMF 4/10/07: use Environment font so size can be changed
	setFont(Environment.FONT12);
	GridBagLayout gbl = new GridBagLayout();
	getContentPane().setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill=GridBagConstraints.NONE;
	gbc.anchor=GridBagConstraints.WEST;
	gbc.weightx=100;
	gbc.weighty=100;
	
	boolean autoScale=((Boolean)ht.get("autoscale")).booleanValue();
	boolean autoDecrease=((Boolean)ht.get("autodecrease")).booleanValue();
	
	// Radio buttons
	ButtonGroup group = new ButtonGroup();
	
	autoUD_CB =
	    new JRadioButton(
	        "AutoScale (increasing/decreasing)", autoScale && autoDecrease);
        autoUD_CB.setFont(Environment.FONT10);
	group.add(autoUD_CB);
	autoUD_CB.addItemListener(this);
	gbc.gridx=0;
	gbc.gridy=0;
	gbc.gridwidth=2;
	gbc.gridheight=1;
	getContentPane().add(autoUD_CB,gbc);
	
	autoU_CB =
	    new JRadioButton(
	        "AutoScale (increasing only)", autoScale && !autoDecrease);
        autoU_CB.setFont(Environment.FONT10);
	group.add(autoU_CB);
	autoU_CB.addItemListener(this);
	gbc.gridy=1;
	getContentPane().add(autoU_CB,gbc);
	
	manual_CB=new JRadioButton("Manual Scaling",!autoScale);
        manual_CB.setFont(Environment.FONT10);
	group.add(manual_CB);
	manual_CB.addItemListener(this);
	gbc.gridy=2;
	getContentPane().add(manual_CB,gbc);
	
	divLabel=new JLabel("Number of Divisions ", SwingConstants.RIGHT);
        divLabel.setFont(Environment.FONT10);
	// divLabel.setAlignment(Label.RIGHT);
	gbc.gridy=3;
	gbc.gridwidth=1;
	gbc.anchor=GridBagConstraints.EAST;
	getContentPane().add(divLabel,gbc);
	
	divChoice=new JComboBox();
        divChoice.setFont(Environment.FONT10);
	for (int i = 1; i <= 10; ++i) {
	    divChoice.addItem(Integer.toString(i));
	}
	divChoice.setSelectedItem(((Integer)ht.get("divisions")).toString());
	gbc.gridx=1;
	gbc.anchor=GridBagConstraints.WEST;
	getContentPane().add(divChoice,gbc);
	
	minLabel=new JLabel("Minimum ", SwingConstants.RIGHT);
        minLabel.setFont(Environment.FONT10);
	// minLabel.setAlignment(Label.RIGHT);
	gbc.gridx=0;
	gbc.gridy=4;
	gbc.anchor=GridBagConstraints.EAST;
	getContentPane().add(minLabel,gbc);
	//paramPanel.add(minLabel);
	minField=new JTextField(((Double)ht.get("min")).toString(),16);
        minField.setFont(Environment.FONT10);
	gbc.gridx=1;
	getContentPane().add(minField,gbc);
	//paramPanel.add(minField);
	
	maxLabel=new JLabel("Maximum ", SwingConstants.RIGHT);
        maxLabel.setFont(Environment.FONT10);
	// maxLabel.setAlignment(Label.RIGHT);
	//paramPanel.add(maxLabel);
	gbc.gridx=0;
	gbc.gridy=5;
	getContentPane().add(maxLabel,gbc);
	maxField=new JTextField(((Double)ht.get("max")).toString(),16);
        maxField.setFont(Environment.FONT10);
	gbc.gridx=1;
	getContentPane().add(maxField,gbc);
	//paramPanel.add(maxField);
	
	//upper panel for auto and param inner panels
	//Panel upperPanel=new Panel();
	//upperPanel.setLayout(new GridLayout(2,1));
	//upperPanel.add(autoPanel);
	//upperPanel.add(paramPanel);
	
	//lower panel for buttons
	JPanel lowerPanel = new JPanel();
	lowerPanel.setLayout(new GridLayout(1,2,15,5));
	JButton ok = new JButton("Ok");
        ok.setFont(Environment.FONT10);
	ok.addActionListener(this);
	lowerPanel.add(ok);
	JButton cancel = new JButton("Cancel");
        cancel.setFont(Environment.FONT10);
	cancel.addActionListener(this);
	lowerPanel.add(cancel);
	gbc.gridx=0;
	gbc.gridy=6;
	gbc.gridwidth=2;
	gbc.anchor=GridBagConstraints.CENTER;
	getContentPane().add(lowerPanel,gbc);
	
	// Handle the close operation in the windowClosing() method of the
	// registered WindowListener object.  This will get around
	// JFrame's default behavior of automatically hiding the window when
	// the user clicks on the '[x]' button.
	setDefaultCloseOperation(
	    javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	addWindowListener(new CloseClass());
	
	pack();
	
	//EMF 8/23/05: locate dialog over matching plot window
	Point p=pc.getLocation();
	Container c=(Container)pc.getParent();
	while ((c=c.getParent())!=null) {
		Point p2=c.getLocation();
		p.x+=p2.x;
		p.y+=p2.y;
	}
	setLocation(p);
	
	if (autoScale && autoDecrease) {
	   divLabel.setEnabled(false);
	   divChoice.setEnabled(false);
	   minLabel.setEnabled(false);
	   minField.setEnabled(false);
	   maxLabel.setEnabled(false);
	   maxField.setEnabled(false);
	}
   }
   
   public void quitCancel() {
	proceed=false;
	setVisible(false);
	}
   
   public void itemStateChanged(ItemEvent e) {
	/*if (e.getSource() instanceof Choice) {
	   if (e.getStateChange() == ItemEvent.SELECTED) {
		Integer divInt=new Integer((String)e.getItem());
		System.out.println("OrdinateDialog.itemStateChanged: Choice "+divInt);
		}
	   }*/
	if (e.getSource() instanceof JRadioButton) {
	   boolean enabled=true;
	   if ((JRadioButton)e.getSource() == autoUD_CB) enabled=false;
	   divLabel.setEnabled(enabled);
	   divChoice.setEnabled(enabled);
	   minLabel.setEnabled(enabled);
	   minField.setEnabled(enabled);
	   maxLabel.setEnabled(enabled);
	   maxField.setEnabled(enabled);
	   }
	}
   
   public void actionPerformed(ActionEvent e) {
	if (e.getSource() instanceof JButton) {
	   JButton b=(JButton)e.getSource();
	   if (b.getText().equals("Ok")) {
		//System.out.println("OrdinateDialog.actionPerformed: Ok");
		ht.put("autoscale",new Boolean(autoUD_CB.isSelected() || autoU_CB.isSelected()));
		ht.put("autodecrease",new Boolean(!autoU_CB.isSelected()));
		ht.put("divisions",new Integer( (String)divChoice.getSelectedItem() ));
		//add code to check for correctly formatted double
		ht.put("min",new Double(minField.getText()));
		ht.put("max",new Double(maxField.getText()));
		proceed=true;
		setVisible(false);
		}
	   else if (b.getText().equals("Cancel")) {
		//System.out.println("OrdinateDialog.actionPerformed: Cancel");
		quitCancel();
		}
	   }
	}
   
   public Hashtable getParameters() {
	return ht;
	}
	
	/*public Dimension getMinimumSize() {
		return new Dimension(500,300);
		}
	
	public Dimension getPreferredSize() {
		return new Dimension(500,300);
		} */
	
   class CloseClass extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	   quitCancel();
	   }
	}
   
   }
	
