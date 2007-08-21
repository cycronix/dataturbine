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
  *********************************************************************
  ***								    ***
  ***	Name :	ChanList.java			        	    ***
  ***	By   :	U. Bersgtrom    (Creare Inc., Hanover, NH)          ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	January 2002            			    ***
  ***								    ***
  ***	Copyright 2002, 2004 Creare Inc.       			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This class extends java.awt.List, to create a List which ***
  ***   supports the use of the shift key with selecting, to select ***
  ***   a range of values.                                          ***
  ***								    ***
  ***   Modifications:                                              ***
  ***	12/01/2000	UCB	ChanList modified to hi-lite	    ***
  ***				currently selected channels in list.***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player.	    ***
  ***								    ***
  *********************************************************************
*/

package com.rbnb.player;

import java.awt.Font;
import java.awt.List;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Vector;

class ChanList extends List implements ItemListener {

    /* Public fields: */
    public boolean haveChans = false;

    /* Private fields: */

    private SmartChanName[] smartChans = null;

    private boolean amSelRange = false;  // are we selecting a range of values?

    private int lastClicked = -1;  // index of last [de]selected item.
    
    private ChannelDlg parent = null;

    private boolean useShortNames = false;

/*
  *********************************************************************
  ***								    ***
  ***	Name :	ChanList         				    ***
  ***	By   :	U. Bersgtrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	January, December 2002             		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.       			    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   Constructor.                                             ***
  ***	Input :							    ***
  ***	   channelNames     Names of available chans.               ***
  ***	   selectedChans    Names of currently selected chans.      ***
  ***      permitMultiple   Whether to permit the selection of      ***
  ***                       multiple channels from the ChanList.    ***
  ***								    ***
  *********************************************************************
*/
  ChanList(ChannelDlg parentI, 
	   String[] channelNames, 
	   String[] selectedChans,
	   boolean permitMultiple,
	   boolean useShortNamesI) {
      super(10, permitMultiple);
      parent = parentI;
      setFont(new Font("Dialog",Font.PLAIN,12));

      if (channelNames == null || channelNames.length == 0 ||
	  (channelNames.length == 1 && channelNames[0].equals(""))) {

	  setEnabled(false);

      } else {
	  smartChans = new SmartChanName[channelNames.length];
	  
	  String[] localSelChans = new String[selectedChans.length];
	  System.arraycopy(selectedChans,
			   0,
			   localSelChans,
			   0,
			   selectedChans.length);
	  Arrays.sort(localSelChans);
	  
	  for (int i = 0; i < channelNames.length; i++) {
	      smartChans[i] = new SmartChanName(i, channelNames[i]);
	      
	      add(channelNames[i]);
	      
	      // UCB 05/03/02 - go ahead and select the matching channels 
	      // for now.  Afterwards, we must do any necessary
	      // checks for dupes.
	      if (Arrays.binarySearch(localSelChans, channelNames[i]) >= 0) {
		  smartChans[i].isSelected = true;
		  super.select(i);
	      }
	  }

	  findChanDupes();

	  setUseShortNames(useShortNamesI, false);
	  
	  addItemListener(this);

	  haveChans = true;
      }
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	deselect                                   	    ***
  ***	By   :	U. C. Bergstrom     (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method overrides List.deselect(int).                ***
  ***								    ***
  *********************************************************************
*/
    public void deselect(int indexToDeselect) {
	smartChans[indexToDeselect].isSelected = false;
	if (isIndexSelected(indexToDeselect)) {
	    super.deselect(indexToDeselect);
	}
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	setSelectingRange   				    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	March 2002               	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method sets whether or not ChanList is selecting a  ***
  ***   range of values.                                            ***
  ***								    ***
  ***	Input :							    ***
  ***	   amSelRange  is ChanList now selecting a range of values? ***
  ***								    ***
  *********************************************************************
*/
    public void setSelectingRange(boolean selectingRangeI) {
	amSelRange = selectingRangeI;
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	getSelectingRange   				    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	March 2002               	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method returns true if ChanList is selecting a      ***
  ***   range of values.                                            ***
  ***								    ***
  *********************************************************************
*/
    public boolean getSelectingRange() {
	return amSelRange;
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	selectRange					    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	January 2002            	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method handles the selection of a range of channels ***
  ***   from the list.                                              ***
  ***								    ***
  ***	Input :							    ***
  ***	   startIndex  starting index for the range.                ***
  ***      rangeLen    length of the range.                         ***
  ***								    ***
  *********************************************************************
*/
  public void selectRange(int startIndex, int rangeLen) {
      int limit = startIndex + rangeLen;
      boolean sawDupes = false;
      for (int i = startIndex; i < limit; i++) {
	  if (selectChan(i, false) && !sawDupes) {
	      sawDupes = true;
	  }
      }

      if (sawDupes) {
	  parent.displayMsg(new String[] {
	      "Duplicate short names are not permitted.",
	      "Selections with duplicate names have been deselected."},
			    "Information");
      }
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	deselectRange					    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	January 2002            	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method handles the deselection of a range of        ***
  ***   channels from the list.                                     ***
  ***								    ***
  ***	Input :							    ***
  ***	   startIndex  starting index for the range.                ***
  ***      rangeLen    length of the range.                         ***
  ***								    ***
  *********************************************************************
*/
  public void deselectRange(int startIndex, int rangeLen) {
      int limit = startIndex + rangeLen;
      for (int i = startIndex; i < limit; i++) {
	  deselect(i);
      }
  }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	itemStatChanged     (ItemListener interface)	    ***
  ***	By   :	U. Bergstrom        (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	January 2002            	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method is called when the user selects or deselects ***
  ***   an item from the ChanList.                                  ***
  ***								    ***
  ***	Input :							    ***
  ***	   itemEvent                                                ***
  ***								    ***
  *********************************************************************
*/
    public synchronized void itemStateChanged(ItemEvent itemEvent) {
	int index = ((Integer) itemEvent.getItem()).intValue();

	if (getSelectingRange()) {
	    if (lastClicked == -1) {
		lastClicked = index;
	    } else {
		if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
		    if (lastClicked >= index) {
			selectRange(index, lastClicked - index + 1);
		    } else {
			selectRange(lastClicked, index - lastClicked + 1);
		    }
		} else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
		    if (lastClicked >= index) {
			deselectRange(index, lastClicked - index + 1);
		    } else {
			deselectRange(lastClicked, index - lastClicked + 1);
		    }
		}
		lastClicked = -1;
	    }
	} else {
	    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
		selectChan(index, true);
	    } else {
		deselect(index);
	    }
	}
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	setUseShortNames                           	    ***
  ***	By   :	U. C. Bergstrom     (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method sets the value of useShortNames, checking    ***
  ***   for and removing dupes as needed.                           ***
  ***								    ***
  *********************************************************************
*/
    public void setUseShortNames(boolean useShortNamesI, boolean reportDupes) {
	useShortNames = useShortNamesI;
	if (useShortNames) {
	    boolean dupesFound = false;
	    int[] selected = getSelectedIndexes();
	    for (int i = selected.length - 1; i >= 0; --i) {
		if (checkForDupes(selected[i])) {
		    dupesFound = true;
		    deselect(selected[i]);
		}
	    }

	    if (dupesFound && reportDupes) {
		parent.displayMsg(new String[] {
		    "Duplicate short names are not permitted.",
		    "Selections with duplicate names have been deselected."},
				  "Information");
				  
	    }
	}
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	findChanDupes                           	    ***
  ***	By   :	U. C. Bergstrom     (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method sorts smartChans by channel name, finds and  ***
  ***   records duplicate names, and then resorts smartChans by     ***
  ***   index.                                                      ***
  ***								    ***
  *********************************************************************
*/
    private void findChanDupes() {
	// Sort the smartChans by their endNames-- the "chan" part of
	// the full channel name <server>/<datapath>/<chan>.
	SmartNameComparator snComp = new SmartNameComparator();
	Arrays.sort(smartChans, snComp);
	
	String name = smartChans[0].endName;
	Vector dupeVec = new Vector();	int startOfRun = 0;
	int endOfRun = 0;
	for (int i = 1; i < smartChans.length; ++i) {

	    // 1) find any run of duplicate endNames.
	    while (i < smartChans.length && name.equals(smartChans[i].endName)) {
		endOfRun = i;
		// Note that smartChans[startOfRun].index is not
		// added to the dupeVec during this while loop.
		// it is only added below, in step two, as needed.
		// Otherwise, it would have to be added to and removed 
		// from dupeVec for every single element in smartChans.
		dupeVec.addElement(new Integer(smartChans[i].index));

		++i;
	    }

	    // 2) if startOfRun isn't equal to endOfRun,
	    // a run of dupes were found.  They must be recorded
	    // for all SmartChanNames in the run.
	    // Note that the run is from startOfRun to endOfRun *inclusive*.
	    if (startOfRun != endOfRun) {
		dupeVec.addElement(new Integer(smartChans[startOfRun].index));
		for (int j = startOfRun; j <= endOfRun; ++j) {
		    smartChans[j].setDupeVector(dupeVec);
		}
	    }

	    // 3) re-init for the next pass, if we aren't done
	    if (i >= smartChans.length) {
		break;
	    }
	    startOfRun = endOfRun = i;
	    name = smartChans[i].endName;
	    dupeVec.removeAllElements();
	}

	// Resort the array of smartChans by its own natural
	// ordering: index.
	Arrays.sort(smartChans);
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	checkForDupes                              	    ***
  ***	By   :	U. C. Bergstrom     (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method returns true if any of the duplicates of     ***
  ***   this smartChan are already selected.  Otherwise, it returns ***
  ***   false.                                                      ***
  ***								    ***
  *********************************************************************
*/
    private boolean checkForDupes(int chanIndex) {
	Vector dupeVec = smartChans[chanIndex].getDupeVector();
	int dupeIndex;

	for (int i = 0; i < dupeVec.size(); ++i) {
	    dupeIndex = ((Integer) dupeVec.elementAt(i)).intValue();
	    if (smartChans[dupeIndex].isSelected) {
		return true;
	    }
	}

	return false;
    }

/*
  *********************************************************************
  ***								    ***
  ***	Name :	selectChan                              	    ***
  ***	By   :	U. C. Bergstrom     (Creare Inc., Hanover, NH)      ***
  ***	For  :  DataTurbine					    ***
  ***	Date :	May 2002                	      		    ***
  ***								    ***
  ***	Copyright 2002 Creare Inc.	               		    ***
  ***	All Rights Reserved					    ***
  ***								    ***
  ***	Description :						    ***
  ***	   This method selects a channel by index.  If useShortNames***
  ***   is true, it will check for duplicate selections, and        ***
  ***   disallow them.  If reportDupes is also true, this method    ***
  ***   will display an error box to the user.                      ***
  ***								    ***
  *********************************************************************
*/
    private boolean selectChan(int chanIndex, boolean reportDupes) {
	if (!useShortNames || !checkForDupes(chanIndex)) {
	    smartChans[chanIndex].isSelected = true;
	    if (!isIndexSelected(chanIndex)) {
		super.select(chanIndex);
	    }
	    return false;  // no dupes, or we don't care
	} else {
	    if (isIndexSelected(chanIndex)) {
		super.deselect(chanIndex);
	    }

	    if (reportDupes) {
		// display an error box
		String[] errStr = 
		    new String[] {"No more than one channel with the name " +
				  smartChans[chanIndex].endName,
				  "may be selected if Player is not outputting " +
				  "long channel names"};
		parent.displayMsg(errStr, "Error!");
	    }
	}
	return true;  // dupes found
    }

}
