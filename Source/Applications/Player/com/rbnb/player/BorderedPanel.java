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
  ***	Name :	BorderedPanel	                                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	February 2000      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class extends Panel to create a panel with an   ***
  ***   etched border and a title.                              ***
  ***								***
  ***	Modification History					***
  ***	10/08/2004	JPW	Upgrade to RBNB V2 Player;	***
  ***				brought this class from the	***
  ***				COM.Creare.RBNB.Widgets package	***
  ***				to com.rbnb.player.		***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;

public class BorderedPanel extends Panel {

  private Label titleL = null;

  private int thickness = 2;

  private Rectangle bounds = null;

  private Panel panel = null;

  private Color borderColor = Color.gray;

/*
  *****************************************************************
  ***								***
  ***	Name :	BorderedPanel	(constructor)                   ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	February 2000      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor for BorderedPanel class.                 ***
  ***								***
  *****************************************************************
*/
  public BorderedPanel(Panel borderThis, String title, int insetWidth, 
		       Color borderI, int borderWidth) {
      super();
      panel = borderThis;
      thickness = borderWidth;
      borderColor = borderI;
      
      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      setLayout(gbl);

      if (title != null && !title.equals("")) {
	  titleL = new Label(title, Label.CENTER);
	  Font f = new Font("Helvetica", Font.PLAIN, 12);
	  titleL.setFont(f);
	  gbc.gridwidth = GridBagConstraints.REMAINDER;
	  gbc.anchor = GridBagConstraints.NORTH;
	  gbl.setConstraints(titleL, gbc);
	  add(titleL);
      }
      
      gbc.insets = new Insets((titleL == null ? insetWidth : 0),
			       insetWidth, insetWidth, insetWidth);
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gbc.fill = GridBagConstraints.BOTH;
      gbl.setConstraints(borderThis, gbc);
      add(borderThis);
      
      bounds = new Rectangle();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	BorderedPanel	(constructor)                   ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	February 2000      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor for BorderedPanel class.                 ***
  ***								***
  *****************************************************************
*/
  public BorderedPanel(Panel borderThis, String title) {
      this(borderThis, title, 10, Color.gray, 2);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	BorderedPanel	(constructor)                   ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	December 2000      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor for BorderedPanel class.                 ***
  ***								***
  *****************************************************************
*/
  public BorderedPanel(Panel borderThis, String title,
		       int insetWidth) {
      this(borderThis, title, insetWidth, Color.gray, 2);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setTitleGrey                                    ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	January 2001      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   En/disables title label.                             ***
  ***								***
  *****************************************************************
*/
    public void setTitleGrey(boolean grey) {
	if (titleL != null) {
	    titleL.setEnabled(!grey);
	}
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	changeBorder                                    ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	September 2001      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Change the border color and width.  If the input     ***
  ***   Color is null, the color will not be changed.  If the   ***
  ***   input border width int is negative, it will not be      ***
  ***   changed.                                                ***
  ***								***
  *****************************************************************
*/
    public void changeBorder(Color newColor, int newThickness) {
	if (newColor != null) {
	    borderColor = newColor;
	}
	if (newThickness >= 0) {
	    thickness = newThickness;
	}

	repaint();
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	paint   	(paint)                         ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)       ***
  ***	For  :	DataTurbine					***
  ***	Date :	February 2000      	                 	***
  ***								***
  ***	Copyright 2000 Creare Inc.	        		***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Overridden paint method.                             ***
  ***								***
  *****************************************************************
*/
  public void paint(Graphics g) {
    Dimension size = getSize();

    int top;
    if (titleL != null) {
	FontMetrics fm = titleL.getFontMetrics(titleL.getFont());
	top = getInsets().top + fm.getAscent();
    } else {
	top = getInsets().top;
    }

    bounds.setBounds(0, top, size.width - 1, size.height - top - 1);
    int w = bounds.width - thickness;
    int h = bounds.height - thickness;

    g.setColor(borderColor);
    for (int i = 0; i < thickness/2; i++) {
      g.drawRect(bounds.x+i, bounds.y+i, w, h);
    }

    Color bottom = borderColor.brighter().brighter().brighter().brighter();
    g.setColor(bottom);
    for (int i = 0; i < thickness/2; i++) {
      g.drawRect(bounds.x + (thickness/2) + i,
		 bounds.y + (thickness/2) + i,
		 w, h);
    }
  }

}
