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
 * Implements a priority queue for the <code>Timer</code> class.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/04/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/04/2002  INB	Created.
 *
 */
final class PriorityQueue {

    /**
     * the heap for the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private TimerTask[] heap = new TimerTask[1];

    /**
     * the number of entries in the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private int size = 0;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    PriorityQueue() {
	super();
    }

    /**
     * Adds a new entry to the priority queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @param comparableI the <code>TimerTask</code> to add to the queue.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final synchronized void add(TimerTask comparableI) {
	if (size == heap.length) {
	    // Increase the size of the heap if necessary.
	    TimerTask[] newHeap = new TimerTask[size*2];
	    System.arraycopy(heap,0,newHeap,0,size);
	    heap = newHeap;
	}

	// Locate the insertion point for the new element.
	int parent,
	    child;
	for (child = size++,
		 parent = (child - 1)/2;
	     (child > 0) && (heap[parent].compareTo(comparableI) < 0);
	     heap[child] = heap[parent],
		 child = parent,
		 parent = (child - 1)/2) {
	}
	heap[child] = comparableI;

	synchronized (this) {
	    notify();
	}
    }

    /**
     * Clears out the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final synchronized void clear() {
	for (int idx = 0; idx < size; ++idx) {
	    heap[idx] = null;
	}
	size = 0;
    }

    /**
     * Returns the current entry without removing it.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the current entry.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final TimerTask current() {
	return (heap[0]);
    }

    /**
     * Is the queue empty?
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the queue empty?
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final boolean isEmpty() {
	return (size == 0);
    }

    /**
     * Removes an entry from the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the highest priority item in the queue.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final synchronized TimerTask remove() {
	TimerTask resultR = null;
	if (size > 0) {
	    resultR = heap[0];
	    TimerTask item = heap[--size];
	    int child,
		parent = 0;
	    while ((child = 2*parent + 1) < size) {
		if (((child + 1) < size) &&
		    (heap[child].compareTo(heap[child + 1]) < 0)) {
		    // If there are at least two children and the first one
		    // comes before the second, move down.
		    ++child;
		}

		if (item.compareTo(heap[child]) < 0) {
		    // If the current working item is less than the child under
		    // consideration, then push the child up.
		    heap[parent] = heap[child];
		    parent = child;

		} else {
		    // If the current working item is greater than or equal to
		    // the child, then we've found the insertion point.
		    break;
		}
	    }
	    heap[parent] = item;
	}

	return (resultR);
    }

    /**
     * Returns the size of the queue.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the size of the queue.
     * @since V2.0
     * @version 09/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/04/2002  INB	Created.
     *
     */
    public final int size() {
	return (size);
    }
}
