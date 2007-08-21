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
  ***	Name :	SliderInfo                                      ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This is a convenience class, used to store           ***
  ***   information pertaining to the values and display of the ***
  ***   slider/textfield pairs used in ControlBox.java.         ***
  ***								***
  ***	Modification History					***
  ***	10/07/2004	JPW	Brought over from		***
  ***				COM.Creare.RBNB.Widgets to be	***
  ***				in the new RBNB V2 Player	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

public class SliderInfo {
    
    private boolean useArrays = false;

    // use when useArrays is true
    private String displayStrings[] = null;
    private double values[] = null;
    private int defaultIndex = 0;

    // for use when useArrays is false
    private int minIndex = 0;
    private int maxIndex = 0;

/*
  *****************************************************************
  ***								***
  ***	Name :	SliderInfo                                      ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  This constructor causes the SliderInfo ***
  ***   to refer to arrays of values.                           ***
  ***								***
  *****************************************************************
*/    
    public SliderInfo(String disps[], double vals[], int indx) {
	if (disps.length != vals.length) {
	    useArrays = false;
	    minIndex = 0;
	    maxIndex = vals.length - 1;
	    defaultIndex = 0;
	} else {
	    useArrays = true;
	    displayStrings = disps;
	    values = vals;
	    if (indx >= values.length) {
		defaultIndex = values.length - 1;
	    } else if (indx < 0) {
		defaultIndex = 0;
	    } else {
		defaultIndex = indx;
	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	SliderInfo                                      ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  This constructor causes the SliderInfo ***
  ***   to refer to min and max values.                         ***
  ***								***
  *****************************************************************
*/
    public SliderInfo(int min, int max, int indx) {
	useArrays = false;
	minIndex = min;
	maxIndex = max;
	if (indx > max) {
	    defaultIndex = max - 1;
	} else if (indx < min) {
	    defaultIndex = min;
	} else {
	    defaultIndex = indx;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getValue                                        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the value of the input slider    ***
  ***   index.                                                  ***
  ***								***
  *****************************************************************
*/
    public double getValue(int index) {
	if (useArrays) {
	    return values[index];
	} else {
	    return (double) index;
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getDisplay                                      ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns a String to display.             ***
  ***								***
  *****************************************************************
*/
    public String getDisplay(int index) {
	if (useArrays) {
	    return displayStrings[index];
	} else {
	    return "";
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getDefaultIndex                                 ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the default index.               ***
  ***								***
  *****************************************************************
*/
    public int getDefaultIndex() {
	return defaultIndex;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMinIndex                                     ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the minimum index.               ***
  ***								***
  *****************************************************************
*/
    public int getMinIndex() {
	return useArrays ? 0 : minIndex;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getMaxIndex                                     ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the maximum index.               ***
  ***								***
  *****************************************************************
*/
    public int getMaxIndex() {
	return useArrays ? (values.length - 1) : maxIndex;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isValidIndex                                    ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method tests whether the input index is valid.  ***
  ***								***
  *****************************************************************
*/
    public boolean isValidIndex(int index) {
	if (useArrays) {
	    if ((index < values.length) && (index >= 0)) {
		return true;
	    }
	} else {
	    if ((index <= maxIndex) && (index >= minIndex)) {
		return true;
	    }
	}
	return false;
    }

}

