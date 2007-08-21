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
  ***	Name :	SortedVector.java				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999 - January, 2000			***
  ***								***
  ***	Copyright 1999, 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class extends the java.util.Vector class to	***
  ***	provide sorting on a particular field of a standard	***
  ***	object class to be inserted.				***
  ***								***
  ***	Modification History :					***
  ***	   06/06/2000 - INB					***
  ***		Save the last index and try looking at that	***
  ***		point the next time around.			***
  ***	   06/29/2000 - INB					***
  ***		The remove method now returns a boolean to	***
  ***		be consistent with the V1.3 Vector.		***
  ***	   06/11/2001 - INB					***
  ***		The remove() method is no longer final.		***
  ***	   09/25/2001 - INB					***
  ***		Optimized some of the routines.			***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

import java.util.Vector;

public class SortedVector
extends Vector
implements Cloneable {

  // Private fields:
					// The sorting field identifier.
  private Object		sortIdentifier = null;
					// The last index found.
  private int			lastIndex = -1;

/*
  *****************************************************************
  ***								***
  ***	Name :	SortedVector					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor creates a new default SortedVector	***
  ***	using a null sort field.				***
  ***								***
  *****************************************************************
*/
  public SortedVector() {
    super();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SortedVector					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	  This constructor creates a new SortedVector using	***
  ***	the input sort field. A default vector is created.	***
  ***								***
  ***	Input :							***
  ***	   sortIdentifierI	The sort field identifier.	***
  ***								***
  *****************************************************************
*/
  public SortedVector(Object sortIdentifierI) {
    super();
    sortIdentifier = sortIdentifierI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SortedVector					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor creates a new SortedVector using	***
  ***	the specified initial capacity and a null sort field.	***
  ***								***
  ***	Input :							***
  ***	   capacityI		The initial capacity.		***
  ***								***
  *****************************************************************
*/
  public SortedVector(int capacityI) {
    super(capacityI);
  }


/*
  *****************************************************************
  ***								***
  ***	Name :	SortedVector					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor creates a new SortedVector using	***
  ***	the specified initial capacity and sort field.		***
  ***								***
  ***	Input :							***
  ***	   capacityI		The initial capacity.		***
  ***	   sortIdentifierI	The sort field identifier.	***
  ***								***
  *****************************************************************
*/
  public SortedVector(int capacityI,Object sortIdentifierI) {
    super(capacityI);
    sortIdentifier = sortIdentifierI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SortedVector					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor creates a new SortedVector using	***
  ***	the specified initial capacity, increment, and a null	***
  ***	sort field.						***
  ***								***
  ***	Input :							***
  ***	   capacityI		The initial capacity.		***
  ***	   incrementI		The capacity increment.		***
  ***								***
  *****************************************************************
*/
  public SortedVector(int capacityI,int incrementI) {
    super(capacityI,incrementI);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SortedVector					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor creates a new SortedVector using	***
  ***	the specified initial capacity, increment, and sort	***
  ***	field.							***
  ***								***
  ***	Input :							***
  ***	   capacityI		The initial capacity.		***
  ***	   incrementI		The capacity increment.		***
  ***	   sortIdentifierI	The sort field identifier.	***
  ***								***
  *****************************************************************
*/
  public SortedVector(int capacityI,int incrementI,Object sortIdentifierI) {
    super(capacityI,incrementI);
    sortIdentifier = sortIdentifierI;
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	getSortIdentifier				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the sort field identifier.	***
  ***								***
  ***	Returns :						***
  ***	   getSortIdentifier	The sort field identifier.	***
  ***								***
  *****************************************************************
*/
  public final Object getSortIdentifier() {
    return (sortIdentifier);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	add						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999 - January, 2000			***
  ***								***
  ***	Copyright 1999, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method adds an object to the sorted vector.	***
  ***								***
  ***	Input :							***
  ***	   objectI		The object to add.		***
  ***								***
  ***	Modification History :					***
  ***	   09/25/2001 - INB					***
  ***		Save the vector size and use the variable in	***
  ***		the loop.					***
  ***								***
  *****************************************************************
*/
  public final synchronized void add(SortInterface objectI)
  throws SortException {
    Object value = objectI.sortField(getSortIdentifier());
    int    idx = findInsertionPoint(value,true),
	   eIdx = size();

    // If the object is a new one, add it to the list.
    if (idx < 0) {
      idx = -(idx + 1);
      if (idx == eIdx) {
	addElement(objectI);
      } else {
	insertElementAt(objectI,idx);
      }

    // Otherwise, add the entry at the end of the matching entries.
    } else {
      for (++idx; idx < eIdx; ++idx) {
	SortInterface object = (SortInterface) elementAt(idx);

	if (compareEntryTo(object,value) != 0) {
	  break;
	}
      }

      if (idx == eIdx) {
	addElement(objectI);
      } else {
	insertElementAt(objectI,idx);
      }
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	findIndex					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method finds the index of the object with the	***
  ***	specified identifying value.				***
  ***								***
  ***	Input :							***
  ***	   sortValueI		The sort value to locate.	***
  ***								***
  ***	Returns :						***
  ***	   findIndex		The index of the existing	***
  ***				entry or the index to insert a	***
  ***				new entry as negative value.	***
  ***				Note that the actual index in	***
  ***				the latter case is equal to	***
  ***				-(return value + 1).		***
  ***								***
  ***	Modification History :					***
  ***	   10/08/2000 - INB					***
  ***		Call findInsertionPoint with the allowNewI	***
  ***		parameter set to true rather than false to	***
  ***		ensure that the above note for the returned	***
  ***		value is true.					***
  ***								***
  *****************************************************************
*/
  public final synchronized int findIndex(Object sortValueI)
  throws SortException {
    if (size() == 0) {
      return (-1);
    }

    int idxR = findInsertionPoint(sortValueI,true);

    return (idxR);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	find						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999, 2000 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method finds the object with the specified	***
  ***	identifying value.					***
  ***								***
  ***	Input :							***
  ***	   sortValueI		The sort value to locate.	***
  ***								***
  ***	Returns :						***
  ***	   find			The object with that sort value	***
  ***				or null if no object matches.	***
  ***								***
  ***	Modification History :					***
  ***	   06/07/2000 - INB					***
  ***		Increment the lastIndex value.			***
  ***								***
  *****************************************************************
*/
  public final synchronized SortInterface find(Object sortValueI)
  throws SortException {
    if (size() == 0) {
      return (null);
    }

    int idx = findInsertionPoint(sortValueI,false);

    if (lastIndex != -1) {
      ++lastIndex;
    }
    return ((idx == -1) ? null : (SortInterface) elementAt(idx));
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	remove						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999, 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method removes the entry that matches the	***
  ***	specified value.					***
  ***								***
  ***	Input :							***
  ***	   sortValueI		The sort value to remove.	***
  ***								***
  ***	Returns :						***
  ***	   remove		Success status.			***
  ***								***
  ***	Modification History :					***
  ***	   06/29/2000 - INB					***
  ***		Return boolean and no exception because the	***
  ***		V1.3 Vector remove method doesn't match.	***
  ***	   06/11/2001 - INB					***
  ***		This method is no longer final.			***
  ***								***
  *****************************************************************
*/
  public synchronized boolean remove(Object sortValueI) {
    SortInterface objectR = null;

    try {
      int		  idx = findInsertionPoint(sortValueI,false);

      // If we found an entry, remove it.
      if (idx != -1) {
	objectR = (SortInterface) elementAt(idx);
	removeElementAt(idx);
      }
    } catch (SortException e) {
      objectR = null;
    }

    return (objectR != null);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	findInsertionPoint				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September - November, 1999			***
  ***								***
  ***	Copyright 1999, 2000, 2001 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method finds the point at which an object	***
  ***	using the specified sort value should be inserted.	***
  ***								***
  ***	Input :							***
  ***	   sortValueI		The sort value to use to locate	***
  ***				the insertion point.		***
  ***	   allowNewI		If set, return the insertion	***
  ***				point for a new value as -1 -	***
  ***				the index or the location of an	***
  ***				existing entry.			***
  ***				If clear, just return the	***
  ***				location of an existing entry	***
  ***				or -1.				***
  ***								***
  ***	Returns :						***
  ***	   findInsertionPoint	The index of the existing	***
  ***				entry, the index to insert a	***
  ***				new entry, or -1.		***
  ***								***
  ***	Modification History :					***
  ***	   06/06/2000 - INB					***
  ***		Save the last index and try looking at that	***
  ***		point the next time around.			***
  ***	   09/25/2001 - INB					***
  ***		Save the vector size on input and use that	***
  ***		value where possible. Modified handling when	***
  ***		the last value was off the end of the list.	***
  ***								***
  *****************************************************************
*/
  public final int findInsertionPoint
    (Object sortValueI,
     boolean allowNewI)
  throws SortException {
    boolean isNew = false;
    int	    idxR = 0,
	    eIdx = size();

    // If there are no entries, we need to add a new entry.
    if (eIdx == 0) {
      isNew = true;
      lastIndex = eIdx;

    // If the input value is null, we insert it at the start of the list
    // as a new entry. We can never match nulls.
    } else if (sortValueI == null) {
      isNew = true;
      lastIndex = -1;

    // Perform a binary search for either the object or the point where we
    // should put the object.
    } else {
      int lo = 0,
	  hi = eIdx - 1;
      isNew = true;
      for (idxR =
	     ((lastIndex == -1) ?
	      (lo + hi)/2 :
	      ((lastIndex > hi) ?
	       hi :
	       lastIndex));
	   (lo <= hi);
	   idxR = (lo + hi)/2) {

	SortInterface object = (SortInterface) elementAt(idxR);
	int	      direction = compareEntryTo(object,sortValueI);

	// This entry matches.
	if (direction == 0) {

	  // Back up until we find an entry that doesn't match, in case there
	  // are multiple matches.
	  for (int idx = idxR - 1; idx >= lo; --idx) {
	    object = (SortInterface) elementAt(idx);
	    direction = compareEntryTo(object,sortValueI);

	    if (direction != 0) {
	      idxR = idx + 1;
	      break;
	    }
	  }

	  isNew = false;
	  break;

	// New entry comes before the existing entry.
	} else if (direction < 0) {
	  hi = idxR - 1;

	// New entry comes after the existing entry.
	} else {
	  lo = idxR + 1;
	}
      }

      // If we are inserting a new entry, then lo point is the correct
      // insertion point.
      if (isNew) {
	idxR = lo;
      }

      lastIndex = idxR;
    }

    // If we do not allow new entries and this is a new one, return -1.
    if (isNew && !allowNewI) {
      idxR = -1;

    // For new entries, make the return index equal to -1 - index.
    } else if (isNew) {
      idxR = -1 - idxR;
    }

    return (idxR);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	compareEntryTo					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	January, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This routine compares the input entry to the input	***
  ***	value.							***
  ***								***
  ***	Input :							***
  ***	   objectI		The entry object.		***
  ***	   valueI		The value to check.		***
  ***								***
  ***	Returns :						***
  ***	   compareEntryTo	The result of the compare:	***
  ***				-1 object comes first		***
  ***				 0 object equals value		***
  ***				 1 object comes second		***
  ***								***
  *****************************************************************
*/
  private final int compareEntryTo(SortInterface objectI,Object valueI)
  throws SortException {
    int directionR = 0;

    // If this object supports the compareTo interface, use that.
    if (objectI instanceof SortCompareInterface) {
      directionR =
	-((SortCompareInterface) objectI).compareTo
	  (getSortIdentifier(),valueI);

    // Otherwise, handle standard sort field types.
    } else {
      Object sortValue = objectI.sortField(getSortIdentifier());

      // Determine which directionR to move based on the comparison of
      // the object at the current search point and the input value.
      if (valueI instanceof String) {
	if (!(sortValue instanceof String)) {
	  throw new SortException
	    ("Input source value " +
	     valueI +
	     " does not match type of sort field for vector.");
	} else {
	  directionR = ((String) valueI).compareTo((String) sortValue);
	}
      } else if (valueI instanceof Byte) {
	if (!(sortValue instanceof Byte)) {
	  throw new SortException
	    ("Input source value " +
	     valueI +
	     " does not match type of sort field for vector.");
	} else {
	  byte sv = ((Byte) sortValue).byteValue(),
	       svI = ((Byte) valueI).byteValue();

	  directionR = (int) (svI - sv);
	}
      } else if (valueI instanceof Integer) {
	if (!(sortValue instanceof Integer)) {
	  throw new SortException
	    ("Input source value " +
	     valueI +
	     " does not match type of sort field for vector.");
	} else {
	  int sv = ((Integer) sortValue).intValue(),
	      svI = ((Integer) valueI).intValue();

	  directionR = svI - sv;
	}
      } else if (valueI instanceof Float) {
	if (!(sortValue instanceof Float)) {
	  throw new SortException
	    ("Input source value " +
	     valueI +
	     " does not match type of sort field for vector.");
	} else {
	  float sv = ((Float) sortValue).floatValue(),
		svI = ((Float) valueI).floatValue();

	  directionR = (int) (svI - sv);
	}
      } else if (valueI instanceof Double) {
	if (!(sortValue instanceof Double)) {
	  throw new SortException
	    ("Input source value " +
	     valueI +
	     " does not match type of sort field for vector.");
	} else {
	  double sv = ((Double) sortValue).doubleValue(),
		 svI = ((Double) valueI).doubleValue();

	  directionR = (int) (svI - sv);
	}
      } else {
	throw new SortException
	  ("Input source value " +
	   valueI +
	   " is not a legal sort object type.");
      }
    }

    return (directionR);
  }
}
