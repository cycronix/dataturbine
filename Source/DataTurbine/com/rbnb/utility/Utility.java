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


// copied from RBNBAdmin to Utility, 9/8/98 EMF
package com.rbnb.utility;

import java.io.*;
import java.awt.*;
import java.util.*;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

/*
  *****************************************************************
  ***                                                           ***
  ***	Name :	Utility                                         ***
  ***	By   :	John P. Wilson	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	October, 1997				        ***
  ***								***
  ***	Copyright 1997 Creare Inc, Hanover, N.H.                ***
  ***   All rights reserved.                                    ***
  ***							        ***
  ***	Description :					        ***
  ***	    This class contains a variety of static utility     ***
  ***   methods.                                                ***
  ***								***
  ***	Modification History :				        ***
  ***	05/09/2005	JPW	Add addChanData() and		***
  ***				parseExceptionMsg().		***
  ***								***
  *****************************************************************
*/

public class Utility {

/*
  *****************************************************************
  ***								***
  ***	Name :	Utility.add		        	        ***
  ***	By   :	John P. Wilson	(Creare Inc., Hanover, NH)	***
  ***	For  :	FlyScan						***
  ***	Date :	September, 1997					***
  ***								***
  ***	Copyright 1997, 2000 Creare Inc, Hanover, N.H.		***
  ***	All rights reserved.					***
  ***								***
  ***	Description :						***
  ***	    Add a component to the frame. This frame uses a	***
  ***	gridbag layout manager.					***
  ***								***
  ***	Input :							***
  ***		container   Container to add the component to.  ***
  ***	    c		The component to add.			***
  ***	    gbl		The instance of GridBagLayout used by	***
  ***				this frame.			***
  ***	    gbc		GridBagConstraints used to add the	***
  ***				component.		        ***
  ***	    x,y		Desired row and column position of the	***
  ***				upper left corner of the	***
  ***				component.			***
  ***	    w,h		Number of columns (w) and rows (h) that	***
  ***				this component should occupy.	***
  ***								***
  ***	Modification History :					***
  ***	   09/22/2000 - INB					***
  ***		There is no need to cast the container to	***
  ***		its specific type; the add() method called is	***
  ***		always the correct one.				***
  ***								***
  *****************************************************************
*/

  public static void add(Container container, Component c,
			 GridBagLayout gbl, GridBagConstraints gbc,
			 int x, int y, int w, int h)
  {
    
	gbc.gridx = x;
	gbc.gridy = y;
	gbc.gridwidth = w;
	gbc.gridheight = h;
	gbl.setConstraints(c,gbc);
	container.add(c);
	/*
	if (container instanceof Frame) {
		((Frame)container).add(c);
	}
	else if (container instanceof Panel) {
		((Panel)container).add(c);
	}
	else if (container instanceof Dialog) {
		((Dialog)container).add(c);
	}
	else {
		System.out.println(
			"Utility.add: error in the type of container used.");
	}
	*/
  }

    /**
      * Places a component in a column in a container with a GridBagLayout.
      *  The gridy member of gbc is advanced one after the constraints are set.
      *
	  * @return the input component for chaining purposes.
      * @author WHF
      * @version 2005/01/21
      * @since V2.5
      */
    public static Component addComponent(
		    Container con,
		    Component c, 
		    GridBagConstraints gbc)
    {
	((GridBagLayout) con.getLayout()).setConstraints(c, gbc);
	con.add(c);
	++gbc.gridy;
	return c;
    }

  
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     Parser.makeChoiceBoxSelection()
    //  Date:       April 1, 1997
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1997 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Select the item at (zero-based) index "selectionIndex" from the
    //  Choice box "component". If selectionIndex is less than 0 then just
    //  choose the first item in the list (at index 0).
    //
    //  Modification History:
    //	May 13,1997			JPW			
    //               Moved from Class FlowSolverStepPanel
    //			       to Class Parser
    //
    /////////////////////////////////////////////////////////////////////////
    
    public static void makeChoiceBoxSelection(Choice component,
                                              int selectionIndex)
    {
        try {
            if (selectionIndex >= 0)
                component.select(selectionIndex);
            else
                component.select(0);
        }
        catch (IllegalArgumentException e) {
            // just choose the first item in the list
            component.select(0);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     Parser.makeChoiceBoxSelection()
    //  Date:       April 1, 1997
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1997 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Select the item with the text specified by "selection" from the
    //  Choice box "component". If there is any error in making this
    //  selection, just choose the first item in the Choice box.
    //
    //  Modification History:
    //	May 13,1997			JPW			
    //                   Moved from Class FlowSolverStepPanel
    //				  to Class Parser
    //
    /////////////////////////////////////////////////////////////////////////
    
    public static void makeChoiceBoxSelection(Choice component,
                                              String selection)
    {
        try {
            component.select(selection);
        }
        catch (IllegalArgumentException e) {
            // just choose the first item in the list
            component.select(0);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     Parser.getChoiceBoxSelectionInt()
    //  Date:       August 10, 1997
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1997 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Return the index of the selected item in the given Choice component.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public static int getChoiceBoxSelectionInt(Choice component) {
    	return (component.getSelectedIndex());
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //  Method:     Parser.getChoiceBoxSelectionString()
    //  Date:       August 10, 1997
    //  Programmer: John P. Wilson
    //  For:        Flyscan
    //
    //  Copyright 1997 Creare Inc, Hanover, N.H.
    //  All rights reserved.
    //
    //  Return the selected item in the given Choice component.
    //
    //  Modification History:
    //
    /////////////////////////////////////////////////////////////////////////
    
    public static String getChoiceBoxSelectionString(Choice component) {
    	return (new String(component.getSelectedItem()));
    	
    }
    
/*
  *****************************************************************
  ***								***
  ***	Name :	centerRect	        	        	***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	April, 2000  				        ***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method centers one rectangle over another,      ***
  ***   returning a new location for the rectangle.  An attempt ***
  ***   is made to ensure that the new location will not cause  ***
  ***   the rectangle to display off-screen.                    ***
  ***								***
  *****************************************************************
*/
  public static Point centerRect(Rectangle toCenter,
				 Rectangle anchor) {
    Point locPt = new Point(anchor.x + (anchor.width - toCenter.width)/2,
			    anchor.y + (anchor.height - toCenter.height)/2);

    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension screenSize = tk.getScreenSize();

    if ( (locPt.x + toCenter.width) > screenSize.width) {
      locPt.x = screenSize.width - toCenter.width;
    }
    if ( (locPt.y + toCenter.height) > screenSize.height) {
      locPt.y = screenSize.height - toCenter.height;
    }

    if (locPt.x < 0) {
      locPt.x = 0;
    }
    if (locPt.y < 0) {
      locPt.y = 0;
    }

    return locPt;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	unpack	       (unpack encoded array)	        ***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	February/March, 2000                     	***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method unpacks a string array from a single     ***
  ***   packed string with specified delimiters within it.      ***
  ***   That is, the format of the input string is:             ***
  ***              (<term><delimitor>)*                         ***
  ***								***
  ***	Modification:                                           ***
  ***	   11/17/00 - UCB: Method used to place a copy of the   ***
  ***                      delimitor at the end of the packed   ***
  ***                      string and unpack from the head      ***
  ***                      forwards.  Could cause errors-- for  ***
  ***                      example, unpack("titforta", "tat")   ***
  ***                      would return "titfor" instead of     ***
  ***                      "titforta".  Corrected this.         ***
  ***								***
  *****************************************************************
*/
    public static String[] unpack(String packed, String delimitor) {
	String str[] = null;
	
	// test for null input
	if ((packed == null) || (packed.equals(""))) {
	    str = new String[1];
	    str[0] = new String("");
	    return str;
	}
	
	// remove any final delimitor(s)
	int delimLength = delimitor.length();
	while (packed.endsWith(delimitor)) {
	    if (packed.length() > delimLength) {
		packed = packed.substring(0, packed.length() - delimLength);
	    } else {
		// degenerate case-- packed string is nothing
		// but delimitors!
		str = new String[1];
		str[0] = new String("");
		return str;
	    }
	}
	
	// find all instances of delimitor
	int startIndex = 0;
	int arrayIndex = 1; // will yield size of output array
	int index = 0;
	while (true) {
	    index = packed.indexOf(delimitor, startIndex);
	    if (index == -1) {
		break;
	    }
	    if (index == startIndex) {
		// deals with repeated delimitors,
		// or delimitor at start of packed string
		startIndex = index + delimLength;
		continue;
	    }
	    startIndex = index + delimLength;
	    arrayIndex++;
	}
	
	// construct array and unpack packed string, removing delimitors.
	str = new String[arrayIndex];
	startIndex = 0;
	arrayIndex = 0;
	boolean reachedLast = false;
	while (!reachedLast) {
	    index = packed.indexOf(delimitor, startIndex);
	    if (index == startIndex) {
		// ignore repeat terms or delimitor at
		// head of string
		startIndex = index + delimLength;
		continue;
	    }
	    if (index == -1) {
		// have reached end of string
		index = packed.length();
		reachedLast = true;
	    }
	    str[arrayIndex] = new String(packed.substring(startIndex, index));
	    arrayIndex++;
	    startIndex = index + delimitor.length();
	}
	return str;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	convertToSeconds                 	        ***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	June 2000                     			***
  ***								***
  ***	Copyright 2000 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method takes a time input as a string with the  ***
  ***   format:     [[hours:]minutes:]seconds[.decimal seconds] ***
  ***   and returns the time as a number of seconds.            ***
  ***   Note: will only work accurately for amounts of up to    ***
  ***   about 68 years (about 596,000 hours)                    ***
  ***								***
  *****************************************************************
*/
  public static float convertToSeconds(String timeStr) {
    String[] partsStr = unpack(timeStr, ":");

    float[] partsFl = new float[partsStr.length];
    for (int i = 0; i < partsFl.length; i++) {
      try {
	partsFl[i] = Float.valueOf(partsStr[i]).floatValue();
      } catch (NumberFormatException e) {
	partsFl[i] = 0;
      }
    }

    float totalSeconds = partsFl[partsFl.length - 1];
    if (partsFl.length > 1) {
      totalSeconds += 60 * (partsFl[partsFl.length - 2]);
    }
    if (partsFl.length > 2) {
      totalSeconds += 3600 * (partsFl[partsFl.length - 3]);
    }

    return totalSeconds;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	resizeStringArray                 	        ***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	April 2002                     			***
  ***								***
  ***	Copyright 2002 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method takes an input String array and a target ***
  ***   size.  It creates a String array of the desired size,   ***
  ***   copies the contents of the input String array into it,  ***
  ***   and fills any remaining empty slots in the new String   ***
  ***   array with the last entry of the input String array.    ***
  ***      The method returns the new String array.             ***
  ***								***
  *****************************************************************
*/
    public static String[] resizeStringArray(String[] origArray,
					     int targetSize) {
	String[] newArray = new String[targetSize];
	
	if (origArray == null || origArray.length == 0) {
	    return newArray;
	}

	System.arraycopy
	    (origArray,
	     0,
	     newArray,
	     0,
	     (targetSize > origArray.length ? origArray.length : targetSize));

	if (targetSize > origArray.length) {
	    for (int i = origArray.length; i < targetSize; ++i) {
		newArray[i] = new String(newArray[i - 1]);
	    }
	}

	return newArray;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	resizeBooleanArray                 	        ***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	April 2002                     			***
  ***								***
  ***	Copyright 2002 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method takes an input boolean array and a target***
  ***   size.  It creates a boolean array of the desired size,  ***
  ***   copies the contents of the input boolean array into it, ***
  ***   and fills any remaining empty slots in the new boolean  ***
  ***   array with the last entry of the input boolean array.   ***
  ***      The method returns the new boolean array.            ***
  ***								***
  *****************************************************************
*/
    public static boolean[] resizeBooleanArray(boolean[] origArray,
					       int targetSize) {
	boolean[] newArray = new boolean[targetSize];

	if (origArray == null || origArray.length == 0) {
	    return newArray;
	}

	System.arraycopy
	    (origArray,
	     0,
	     newArray,
	     0,
	     (targetSize > origArray.length ? origArray.length : targetSize));

	if (targetSize > origArray.length) {
	    for (int i = origArray.length; i < targetSize; ++i) {
		newArray[i] = newArray[i - 1];
	    }
	}

	return newArray;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	resizeIntArray                  	        ***
  ***	By   :	Ursula Bergstrom  (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II					***
  ***	Date :	April 2002                     			***
  ***								***
  ***	Copyright 2002 Creare Inc.		        	***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method takes an input int array and a target    ***
  ***   size.  It creates a int array of the desired size,      ***
  ***   copies the contents of the input int array into it,     ***
  ***   and fills any remaining empty slots in the new int      ***
  ***   array with the last entry of the input int array.       ***
  ***      The method returns the new int array.                ***
  ***								***
  *****************************************************************
*/
    public static int[] resizeIntArray(int[] origArray,
				       int targetSize) {
	int[] newArray = new int[targetSize];

	if (origArray == null || origArray.length == 0) {
	    return newArray;
	}

	System.arraycopy
	    (origArray,
	     0,
	     newArray,
	     0,
	     (targetSize > origArray.length ? origArray.length : targetSize));

	if (targetSize > origArray.length) {
	    for (int i = origArray.length; i < targetSize; ++i) {
		newArray[i] = newArray[i - 1];
	    }
	}

	return newArray;
    }
    
    /**************************************************************************
     * Add a float value to a channel in a ChannelMap.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param cmI  the ChannelMap to add the channel and data to
     * @param chanNameI  name of the channel to add to the ChannelMap
     * @param dataI  the float data to put in the channel
     * @version 05/09/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2005  JPW  Created.
     *
     */
    
    public static void addChanData(
        ChannelMap cmI, String chanNameI, float dataI)
    throws SAPIException
    {
	int chanIdx = cmI.Add(chanNameI);
	float[] dataArray = new float[1];
	dataArray[0] = dataI;
	cmI.PutDataAsFloat32(chanIdx, dataArray);
    }
    
    /**************************************************************************
     * Add a double value to a channel in a ChannelMap.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param cmI  the ChannelMap to add the channel and data to
     * @param chanNameI  name of the channel to add to the ChannelMap
     * @param dataI  the double data to put in the channel
     * @version 05/09/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2005  JPW  Created.
     *
     */
    
    public static void addChanData(
        ChannelMap cmI, String chanNameI, double dataI)
    throws SAPIException
    {
	int chanIdx = cmI.Add(chanNameI);
	double[] dataArray = new double[1];
	dataArray[0] = dataI;
	cmI.PutDataAsFloat64(chanIdx, dataArray);
    }
    
    /**************************************************************************
     * Add a String to a channel in a ChannelMap.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param cmI  the ChannelMap to add the channel and data to
     * @param chanNameI  name of the channel to add to the ChannelMap
     * @param dataI  the String data to put in the channel
     * @version 05/09/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/09/2006  JPW	Set the MIME type
     * 05/09/2005  JPW  Created.
     *
     */
    
    public static void addChanData(
        ChannelMap cmI, String chanNameI, String strI)
    throws SAPIException
    {
	int chanIdx = cmI.Add(chanNameI);
	cmI.PutDataAsString(chanIdx, strI);
	// JPW 02/09/2006: Set the MIME type
	cmI.PutMime(chanIdx, "text/plain");
    }
    
    /**************************************************************************
     * Add byte array data to a channel in a ChannelMap.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param cmI  the ChannelMap to add the channel and data to
     * @param chanNameI  name of the channel to add to the ChannelMap
     * @param byteArrayI  the byte array data to put in the channel
     * @version 05/09/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2005  JPW  Created.
     *
     */
    
    public static void addChanData(
        ChannelMap cmI, String chanNameI, byte[] byteArrayI)
    throws SAPIException
    {
	int chanIdx = cmI.Add(chanNameI);
	cmI.PutDataAsByteArray(chanIdx, byteArrayI);
    }
    
    /**************************************************************************
     * Parse an intelligible exception message from the given exception.
     * <p>
     * This method is intended to parse nested exceptions that are returned
     * from the RBNB.  The message that is part of the nested exception is
     * returned.
     *
     * @author John P. Wilson
     *
     * @param exceptionI  the Exception to parse
     * @return a <code>String</code> containing the exception message
     * @version 05/09/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/09/2005  JPW  Created.
     *
     */
    
    public static String parseExceptionMsg(Exception exceptionI) {
	
	if (exceptionI == null) {
	    return null;
	}
	
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw, true);
	exceptionI.printStackTrace(pw);
	StringReader sr = new StringReader(sw.toString());
	BufferedReader br = new BufferedReader(sr);
	
	// See if there was a nested exception thrown by the Server
	String exceptionMsg = null;
	String msg = null;
	try {
	    while ( (msg = br.readLine()) != null ) {
		if (msg.startsWith("Nested exception:")) {
		    // Read the next line; this will contain the
		    // Server exception message we want
		    exceptionMsg = br.readLine();
		    break;
		}
	    }
	} catch (Exception readException) {
	    exceptionMsg = null;
	}
	
	if ( (exceptionMsg == null) || (exceptionMsg.trim().equals("")) ) {
	    exceptionMsg = exceptionI.getMessage();
	}
	
	return exceptionMsg;
	
    }

}
