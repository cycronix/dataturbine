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
  ***	Name :	SliderPanel                                     ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class is used to maintain and display a slider/ ***
  ***   textfield pair.                                         ***
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

import com.rbnb.utility.Utility;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderPanel implements ChangeListener {

    // background color
    // UCB 04/28/02 - changing background color to white.
    //public static final Color BACK_COLOR = new Color(255, 237, 223);
    public static final Color BACK_COLOR = Color.white;

    // graphical components
    private BorderedPanel pan = null;
    private JSlider slider = null;
    private JTextField feedback = null;
    private JTextField extraFeedback = null;  // UCB 12/05/01

    // ControlBox's way of identifying this SliderPanel
    private int IDNumber = -1;

    private ControlBox parent = null;

    // UCB 11/01/02: an array holds the SliderInfos for each mode;
    // the array is indexed by mode.  SliderInfos hold info about
    // values and appearances
    private SliderInfo[] modeInfos = null;

    // UCB 11/02/02 - replacing two-value usingTimeMode
    // with multi-value currMode
    //private boolean usingTimeMode = true;
    private int currMode = ControlBox.TIME_MODE;

    // is the user moving the slider, or is the app?
    private boolean movingSlider = false;  // synchronize accesses!

    // does this sliderpanel have an extra feedback TextField?
    private boolean hasExtraFeedback = false;
    
    /**********************************************************/
    /********************* Construction ***********************/
    /**********************************************************/    
/*
  *****************************************************************
  ***								***
  ***	Name :	SliderPanel                                     ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.                                         ***
  ***								***
  *****************************************************************
*/
    public SliderPanel(ControlBox parentI, 
		       String title,
		       int id,
		       boolean tickAll,
		       SliderInfo[] modeInfosI,
		       boolean snapToTicks,
		       boolean createExtraFeedback) {

	parent = parentI;
	IDNumber = id;
	modeInfos = modeInfosI;
	
	int minSp = 1,
	    maxSp = 10000;
	if (!tickAll) {
	    minSp = 10;
	    maxSp = 100;
	}

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	Panel panel = new Panel(gbl);
	panel.setBackground(BACK_COLOR);

	gbc.weightx = 0;
	gbc.weighty = 0;

	// create the textfield used to display the current
	// slider value
	feedback = new JTextField(21);
	feedback.setFont(ControlBox.GUI_FONT);
	gbc.anchor = GridBagConstraints.WEST;
	feedback.setBackground(BACK_COLOR);
	//  feedback.setEditable(false);
	Utility.add(panel, feedback,
		    gbl, gbc, 0, 0, 1, 1);

	hasExtraFeedback = createExtraFeedback;
	if (hasExtraFeedback) {
	    // UCB 12/05/01: create extra feedback textfield, if desired.
	    extraFeedback = new JTextField(23);
	    Font italFont = ControlBox.GUI_FONT.deriveFont(Font.ITALIC);
	    extraFeedback.setFont(italFont);
	    extraFeedback.setHorizontalAlignment(JTextField.RIGHT);
	    extraFeedback.setBackground(BACK_COLOR);
	    //  extraFeedback.setEditable(false);
	    Utility.add(panel, extraFeedback,
			gbl, gbc, 1, 0, 1, 1);
	} else {
	    // a blank label used to pad slider to correct length
	    Utility.add(panel, new JLabel("                         "),
			gbl, gbc, 1, 0, 1, 1);
	}

	// Create the slider.
	slider = createSlider(modeInfos[currMode].getMinIndex(),
			      modeInfos[currMode].getMaxIndex(),
			      minSp, maxSp, 
			      modeInfos[currMode].getDefaultIndex(),
			      snapToTicks);
	slider.setBackground(BACK_COLOR);
	// 03/01/02 UCB - make enabled by default.
	slider.setEnabled(true);
	// slider.setEnabled(false);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1;
	gbc.weighty = 1;
	Utility.add(panel, slider,
		    gbl, gbc, 0, 1, 2, 1);

	gotoDefault(currMode);

	pan = new BorderedPanel(panel, title, 5);

	movingSlider = false;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	createSlider    				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method creates the position slider for the	***
  ***	the video retreiver.					***
  ***								***
  ***	Returns :						***
  ***	   createSlider	The slider.		         	***
  ***								***
  *****************************************************************
*/
    private final JSlider createSlider(int minVal, int maxVal,
				       int minSp, int majSp,
				       int startValue,
				       boolean snapToTicks) {
	
	// Create the slider.
	JSlider slider = new JSlider(minVal, maxVal, minVal);
	slider.setSnapToTicks(snapToTicks);
	slider.setValue(startValue);

	slider.setPaintLabels(false);
	slider.setMajorTickSpacing(majSp);
	slider.setMinorTickSpacing(minSp);
	slider.setPaintTicks(true);
	slider.setPaintTrack(true);
	slider.setOrientation(JSlider.HORIZONTAL);
	slider.addChangeListener(this);
	
	return (slider);
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	getPanel		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the BorderedPanel, for display.  ***
  ***								***
  *****************************************************************
*/
    public final synchronized BorderedPanel getPanel() {
	return pan;
    }

    /**********************************************************/
    /******************* Event handling ***********************/
    /**********************************************************/    
/*
  *****************************************************************
  ***								***
  ***	Name :	stateChanged					***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method is called whenever the state of the	***
  ***	slider has changed.                                     ***
  ***								***
  *****************************************************************
*/
  public final void stateChanged(ChangeEvent eventI) {

      // Check source of event
      if (((JSlider) eventI.getSource()) != slider) {
	  //System.err.println("This can't happen.");
	  return;
      }

      int index = slider.getValue();
      String toDisp = modeInfos[currMode].getDisplay(index);
      if (!toDisp.equals("")) {
	  // JPW 10/28/2004: Call the setText() method
	  // feedback.setText(toDisp);
	  setText(toDisp);
      }

      // ignore application-driven events
      if (getMovingSlider()) {
	  setMovingSlider(false);
	  return;
      }
      
      // Is the the slider moving or has it stopped?
      boolean trackingSlider = slider.getValueIsAdjusting();

      // now we send info along to ControlBox...
      parent.sliderChanged(IDNumber, index, trackingSlider);
  }

    /**********************************************************/
    /***************** movingSlider accessor ******************/
    /**********************************************************/    
/*
  *****************************************************************
  ***								***
  ***	Name :	getMovingSlider		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the value of the moving slider   ***
  ***   flag.        						***
  ***								***
  *****************************************************************
*/
  private final synchronized boolean getMovingSlider() {
      return movingSlider;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setMovingSlider		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the value of the moving slider flag.***
  ***								***
  *****************************************************************
*/
  public final synchronized void setMovingSlider(boolean isMoving) {
      movingSlider = isMoving;
  }

    /**********************************************************/
    /************** value and index accessors *****************/
    /**********************************************************/ 
/*
  *****************************************************************
  ***								***
  ***	Name :	getValue		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method gets the value (interpretation of the    ***
  ***   index) of the slider.                                   ***
  ***								***
  *****************************************************************
*/
    public final synchronized double getValue() {
	return modeInfos[currMode].getValue(slider.getValue());
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getValue		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method gets the value (interpretation of the    ***
  ***   index) of the slider.                                   ***
  ***								***
  *****************************************************************
*/
    public final synchronized double getValue(int index) {
	int idx = index;
	if (!modeInfos[currMode].isValidIndex(idx)) {
	    idx = modeInfos[currMode].getDefaultIndex();
	}

	return modeInfos[currMode].getValue(idx);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setIndex		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the value (index) of the slider.    ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setIndex(int index,
					    int mode,
					    boolean updateDisplay) {
	currMode = mode;

	int idx = index;
	if (!modeInfos[mode].isValidIndex(idx)) {
	    idx = modeInfos[mode].getDefaultIndex();
	}

	setMovingSlider(true);
	slider.setValue(idx);
	if (updateDisplay) {
	    // JPW 10/28/2004: Call the setText() method
	    // feedback.setText(modeInfos[mode].getDisplay(idx));
	    setText(modeInfos[mode].getDisplay(idx));
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	gotoDefault		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the value of the slider to its      ***
  ***   default.                                                ***
  ***								***
  *****************************************************************
*/
    public final synchronized void gotoDefault(int mode) {
	currMode = mode;
	resetSlider();
	int idx = modeInfos[currMode].getDefaultIndex();
	setMovingSlider(true);
	slider.setValue(idx);
	// JPW 10/28/2004: Call the setText() method
	// feedback.setText(modeInfos[currMode].getDisplay(idx));
	setText(modeInfos[currMode].getDisplay(idx));
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setEnabled		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method enables/disables the slider.             ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setEnabled(boolean enable) {
	slider.setEnabled(enable);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	resetTimeBase		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000; November, 2001        		***
  ***								***
  ***	Copyright 2000, 2001 Creare Inc.		      	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method resets the min and max of the slider to  ***
  ***   reflect the change from one mode to another.            ***
  ***								***
  *****************************************************************
*/
    private final synchronized void resetSlider() {
	setMovingSlider(true);
	slider.setMinimum(modeInfos[currMode].getMinIndex());
	slider.setMaximum(modeInfos[currMode].getMaxIndex());
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setText  		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the text in the feedback TextField. ***
  ***								***
  ***	Modification History					***
  ***								***
  *****************************************************************
*/
    public final synchronized void setText(String textI) {
	feedback.setText(textI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	setExtraText  		        		***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method sets the text in the extra feedback      ***
  ***   TextField.                                              ***
  ***								***
  *****************************************************************
*/
    public final synchronized void setExtraText(String text) {
	if (hasExtraFeedback) {
	    extraFeedback.setText(text);
	}
    }

}
