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

package com.rbnb.api;

/**
 * Keeps a list of sorted strings.
 * <p>
 * This class exists because the <code>TreeSet</code> class is not
 * available in JDK 1.1.8.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 03/26/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/26/2003  INB	Added <code>elementAt(int)</code>.
 * 08/30/2002  INB	Created.
 *
 */
final class SortedStrings {

    /**
     * vector containing the sorted strings.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/30/2002
     */
    private java.util.Vector strings = new java.util.Vector();

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    SortedStrings() {
	super();
    }

    /**
     * Adds a new string.
     * <p>
     * This method performs an insert-sort.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stringI the string to add.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    public final void add(String stringI) {
	int length = strings.size();

	if (length == 0) {
	    strings.addElement(stringI);

	} else {
	    int insertAt = indexOf(stringI);

	    if (insertAt < 0) {
		strings.insertElementAt(stringI,(-insertAt) - 1);
	    }
	}
    }

    /**
     * Is the string an element of this list?
     * <p>
     *
     * @author Ian Brown
     *
     * @param stringI the string to locate.
     * @return is it an element?
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    public final boolean contains(String stringI) {
	return (indexOf(stringI) >= 0);
    }

    /**
     * Returns the element at the specified index.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index.
     * @return the element.
     * @exception java.lang.ArrayIndexOutOfRangeException
     *		  if the index is not in the range 0 to <code>size()</code> -
     *		  1.
     * @since V2.1
     * @version 03/26/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/26/2003  INB	Created.
     *
     */
    public final String elementAt(int indexI) {
	return ((String) strings.elementAt(indexI));
    }

    /**
     * Returns an array of strings containing the elements of this list.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the array of strings.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    public final String[] elements() {
	String[] stringsR = new String[strings.size()];

	for (int idx = 0; idx < strings.size(); ++idx) {
	    stringsR[idx] = (String) strings.elementAt(idx);
	}

	return (stringsR);
    }

    /**
     * Locates the specified string.
     * <p>
     * If the string cannot be found, then the method returns -(insert point +
     * 1).
     * <p>
     *
     * @author Ian Brown
     *
     * @param stringI the string to locate.
     * @return the index or the insert point.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    private final int indexOf(String stringI) {
	int lo = 0,
	    hi = strings.size() - 1,
	    idx;
	String entry;

	for (idx = (hi + lo)/2; lo <= hi; idx = (hi + lo)/2) {
	    entry = (String) strings.elementAt(idx);
	    int cmpr = entry.compareTo(stringI);

	    if (cmpr == 0) {
		return (idx);
	    } else if (cmpr < 0) {
		lo = idx + 1;
	    } else {
		hi = idx - 1;
	    }
	}

	return (-(lo + 1));
    }

    /**
     * Removes the specified string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param stringI the string to remove.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    public final void remove(String stringI) {
	int idx = indexOf(stringI);

	if (idx >= 0) {
	    strings.removeElementAt(idx);
	}
    }

    /**
     * Returns the size of the list.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the size.
     * @since V2.0
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2002  INB	Created.
     *
     */
    public final int size() {
	return (strings.size());
    }
}
