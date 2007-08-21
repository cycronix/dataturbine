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

/******************************************************************************
 * A Panel subclass that draws a colored border and optional label around
 * itself and arranges its components inside its border.
 * <p>
 * Most of the code in this class comes from two different sources:
 * 1. The sample code provided on the Sun web site to accompany question
 *    Q5.8 of Jonni Kanerva's "The Java FAQ" published by Addison-Wesley.
 * 2. Symantec's "BorderPanel" class (symantec.itools.awt.BorderPanel)
 * <p>
 *
 * @author John P. Wilson
 *
 * @since V2.0
 * @version 06/07/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/07/2001  JPW	Created (Taken from V1.1 RBNB code)
 *
 */
abstract public class BorderedPanel extends Panel {
    
    /**
     * Title displayed in the border.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private String title;
    
    /**
     * Color of the border.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private Color borderColor;
    
    /**
     * Specifies how far from the left side of the container the border will
     * be drawn.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private int padleft;
    
    /**
     * Specifies how far from the right side of the container the border will
     * be drawn.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private int padright;
    
    /**
     * Specifies how far from the top of the container the border will
     * be drawn.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private int padtop;
    
    /**
     * Specifies how far from the bottom of the container the border will
     * be drawn.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    private int padbottom;
    
    /**************************************************************************
     * Create the BorderedPanel without any title.
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
    public BorderedPanel() {
        this(null);
    }
    
    /**************************************************************************
     * Create the BorderedPanel with the specified title.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param title  Title to use in the new BorderedPanel.
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
    public BorderedPanel(String title) {
        this(title,Color.black);
    }
    
    /**************************************************************************
     * Create the BorderedPanel with the specified title and border color.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param title  Title to use in the new BorderedPanel.
     * @param color  Border color.
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
    public BorderedPanel(String title, Color color) {
        
	// These padding values specify how far into the container the
	// border will be drawn. Ideally, should draw the border within the
	// container's margin region (as defined by the Insets values; see
	// getInsets() below).
	
	padleft     = 1;
	padright    = 1;
	padtop      = 1;
	padbottom   = 1;
	
        setTitle(title);
        setColor(color);
    }
    
    /**************************************************************************
     * Draws a rectangle around the panel and displays the title (if
     * present, inside the top right corner of the rectangle.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param g  Graphics context to use for drawing the border.
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
    public void paint(Graphics g) {
        
	Dimension s = getSize();
	FontMetrics fm = getFontMetrics(getFont());
	
	int x;
	int y;
	int w;
	int h;
        
	g.clipRect(0, 0, s.width, s.height);
        
        int delta = padtop;
        if ( (getTitle() != null) && (getTitle().length() != 0) ) {
            delta = (fm.getAscent()  + fm.getDescent() + padtop) / 2;
        }
        
	x = padleft;
	y = delta;
	w = s.width - padleft - padright - 1;
	h = s.height - 1 - delta - padbottom;
        
        // Draw the border
	g.setColor(getColor());
	g.drawRect(x, y, w, h);
	
	// Draw the label
	drawLabel(g, fm);
	
    }
    
    /**************************************************************************
     * Draws the border panel label.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param g  Graphics context to use for drawing the label.
     * @param fm  FontMetrics of the font to be used for writing the label.
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
    private void drawLabel(Graphics g, FontMetrics fm) {
	
	//Label horizontal coordinate padding constant, in pixels.
        int labelpadx = 4;
        // Label horizontal coordinate inset constant, in pixels.
        int labelipadx = 4;
	
	String label = getTitle();
	
	if ( (label == null) || (label.length() == 0) || (fm == null) ) {
	    return;
	}
	
        int fWidth;
	Dimension s;
	int stringWidth;
	int ascent;
	int descent;
	int x;
	int y;
	int h;
        
	fWidth = 10;
	s = getSize();
        
	if (getFont().getSize() > fWidth) {
	    fWidth = fWidth + getFont().getSize() / 2;
	}
        
	stringWidth = fm.stringWidth(label);
	ascent      = fm.getAscent();
	descent     = fm.getDescent();
        
        x = fWidth + (labelpadx + labelipadx) / 2;
        h = ascent + descent + padtop;
        y = (fWidth - h) / 2 + (padtop + ascent);
        
	g.setColor(getBackground());
	g.fillRect(x - labelipadx / 2, y - 1 - ascent - padtop/2,
	           stringWidth + labelipadx, h);
	g.setColor(getColor());
	g.drawString(label, x, y - 1);
	
    }
    
    /**************************************************************************
     * Overrides Container's Insets methods to return non-zero values.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return Insets to be applied to this panel.
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
    public Insets getInsets() {
        
        // Insets specify the container's top, left, bottom, and right
        // margins. Layout managers know to obey these inset values.
        // For example, say the top inset was 10 pixels. In this case,
        // the layout manager would know to position a component at least
        // 10 pixels from the top of the container.
        //
        //                 top     left    bottom  right
        return (new Insets(5,      5,      5,      5));
        
    }
    
    /**************************************************************************
     * Gets the title.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return Title used in this BorderedPanel, or null if no title is
     *         specified.
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
    public String getTitle() {
        
        if (title == null) {
            return null;
        }
        return (new String(title));
        
    }
    
    /**************************************************************************
     * Set the title.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param newValue  The new title to be used in the border.
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
    public void setTitle(String newValue) {
        
        if (newValue == null) {
            title = null;
        }
        else {
            title = new String(newValue);
        }
        
    }
    
    /**************************************************************************
     * Gets the border color.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return Border color.
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
    public Color getColor() {
        return borderColor;
    }
    
    /**************************************************************************
     * Set the border color.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param newValue  The new color to paint the border with.
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
    public void setColor(Color newValue) {
        borderColor = newValue;
    }
    
}
