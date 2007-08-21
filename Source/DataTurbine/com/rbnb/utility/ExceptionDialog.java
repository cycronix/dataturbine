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

package com.rbnb.utility;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.*;

/**
  * Displays an exception message, optionally also listing the stack trace, 
  *  in a swing dialog.
  * <p>
  * @author WHF
  * @since V2.4.5
  * @version 2004/12/10
  */
public abstract class ExceptionDialog
{
	/** 
	  * Shows the exception dialog.
	  * @throws NullPointerException If parent is null or has no dialog or frame
	  *   among it and its ancestors.
	  */
	public static void show(
			Component parent,
			Throwable t,
			String title,
			String msg)
	{
		final JDialog dlg;
		while (true) {
			if (parent instanceof Dialog) {
				dlg = new JDialog((Dialog) parent, title, true);
				break;
			} else if (parent instanceof Frame) {
				dlg = new JDialog((Frame) parent, title, true);
				break;
			}
			parent = parent.getParent();
		}
		
		final Container cp = dlg.getContentPane();
		final GridBagConstraints gbc = new GridBagConstraints();
		
		final JTextArea errorText = new JTextArea();
		
		cp.setLayout(new GridBagLayout());
		
		// Set common constraints:
		gbc.gridwidth=1;
		gbc.gridheight = 1;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.fill=gbc.BOTH;
		gbc.ipadx = gbc.ipady = 10;

		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridheight = 2;
		// Get the icon which is normally used with the JOptionPane:
		Icon errorIcon = (Icon) UIManager.getLookAndFeel()
				.getDefaults().get("OptionPane.errorIcon");
		Utility.addComponent(cp, new JLabel(errorIcon), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		Utility.addComponent(cp, new JLabel(msg), gbc);
		Utility.addComponent(cp, new JLabel(t.getMessage()), gbc);
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		JPanel p = new JPanel();
		JButton jb;
		p.add(jb = new JButton("Ok"));
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ dlg.dispose(); }
		});
		dlg.getRootPane().setDefaultButton(jb);
		
		p.add(jb = new JButton("Details..."));
		jb.setMnemonic('D');
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ errorText.setVisible(!errorText.isVisible()); dlg.pack(); }
		});
		
		Utility.addComponent(cp, p, gbc);
		
		// Produce error text.
		// Create dialog message by spewing stack trace into a string:
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		errorText.setText(sw.toString());
		errorText.setVisible(false);
		Utility.addComponent(cp, errorText, gbc);
		
		dlg.pack();
		try {
			// This was added in a fairly recent version of Java, so may throw:
			dlg.setLocationRelativeTo(parent);
		} catch (Exception e) { } // just ignore
		dlg.setVisible(true);		
	}
	
	/**
	  * Test method, uncomment to use, but leave commented because depends on
	  *   features which may not be in every version of Java.
	  */
	/*
	public static void main(String[] args)
	{
		try {
			throw new Exception("Exception!");
		} catch (Exception e) {
			show(new Frame(), e, "Bogus Exception:", 
				"A bogus exception was caught.");
			try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());	
			show(new Frame(), e, "Cross Platfrom (Metal) L&F:", 
				"A bogus exception was caught.");
			UIManager.setLookAndFeel(
					UIManager.getCrossPlatformLookAndFeelClassName());	
			show(new Frame(), e, "Cross Platfrom (Metal) L&F:", 
				"A bogus exception was caught.");
			UIManager.setLookAndFeel(
					"com.sun.java.swing.plaf.motif.MotifLookAndFeel");	
			show(new Frame(), e, "Motif L&F:", 
				"A bogus exception was caught.");
			} catch (Exception e2) {
				show(new Frame(), e2, "Error loading look and feel:",
						"An error occured during the loading of the requested"
						+ " Look and Feel.");
			}
			System.exit(0);
		}
	} */
}


