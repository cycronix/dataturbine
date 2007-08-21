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
  ***	Name :	ChanInfoBuffer                              	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class queues an n-deep set of channel data,     ***
  ***   start and end times, and is used by Capture.            ***
  ***      This class was based on the pre-existing class       ***
  ***   ChannelBuffer.                                          ***
  ***		                                                ***
  ***	NOTE THAT THE CLIENT CLASS NEED NOT (AND SHOULD NOT)    ***
  ***   PLACE CALLS TO FETCHNEXT(), INTERRUPTFETCH() AND/OR     ***
  ***   ADDCHANNEL() WITHIN A BLOCK SYNCHRONIZED ON AN          ***
  ***   INSTANCE OF THE CHANNELBUFFER CLASS.                    ***
  ***								***
  ***	Modification History :					***
  ***	07/23/04 JPW - Brought this class into the new		***
  ***		       RBNB V2.4 compliant Capture package.	***
  ***								***
  *****************************************************************
*/
package com.rbnb.capture;

public class ChanInfoBuffer {

    private int length;
    private volatile ChanInfo[] buffer = null;

    private volatile int rear = 0;
    private volatile int front = 0;

    private Object syncObj = new Object();
    private volatile boolean quitRequest = false;

    public static final int DEFAULT_BUFFER_SIZE = 12;

    /***************** CONSTRUCTORS *******************/
/*
  *****************************************************************
  ***								***
  ***	Name :	ChanInfoBuffer                              	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Creates buffer with default size.      ***
  ***								***
  *****************************************************************
*/
    public ChanInfoBuffer() {
	this(DEFAULT_BUFFER_SIZE);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	ChanInfoBuffer                              	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires the desired buffer size.      ***
  ***								***
  *****************************************************************
*/
    public ChanInfoBuffer(int size) {
	// Note that one buffer slot is always left open under this
	// implementation, and so the actual buffer must be 
	// one larger than the requested length.
	length = (size > 0) ? size + 1 : 2;
	buffer = new ChanInfo[length];
    }

    /**************** PUBLIC INTERFACE *****************/
/*
  *****************************************************************
  ***								***
  ***	Name :	fetchNext                                       ***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method allows a client class of ChanInfoBuffer  ***
  ***   to fetch the next available ChanInfo from the front of  ***
  ***   the buffer.  It will block if the buffer is empty.  The ***
  ***   client class may cause it to un-block by calling        ***
  ***   interruptFetch().                                       ***
  ***   THE RETURNED CHANNEL REFERENCE MAY BE NULL.             ***
  ***								***
  *****************************************************************
*/
    public ChanInfo fetchNext() {
	synchronized (syncObj) {
	    while (isEmpty() && !quitRequest) {
		try {
		    syncObj.wait();
		} catch (InterruptedException e) {
		    System.err.println("interrupted");
		    quitRequest = false;
		    return null;
		}
	    }

	    if (quitRequest) {
		quitRequest = false;
		return null;
	    }

	    front = incrementOf(front);
	    return buffer[front];
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	interruptFetch                                  ***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method allows a client class of ChanInfoBuffer   ***
  ***   to interrupt a fetchNext() call which is blocked due to ***
  ***   the buffer being empty.                                 ***
  ***								***
  *****************************************************************
*/
    public void interruptFetch() {
	synchronized (syncObj) {
	    quitRequest = true;
	    syncObj.notifyAll();
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	addChanInfo                                     ***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method allows a client class of ChanInfoBuffer  ***
  ***   to add a ChanInfo to the rear of the buffer.  If the    ***
  ***   buffer is full, the ChanInfo will not be added.         ***
  ***								***
  *****************************************************************
*/
    public void addChanInfo(ChanInfo chanInf) {
	synchronized (syncObj) {
	    if (!isFull()) {
		rear = incrementOf(rear);
		buffer[rear] = chanInf;
		syncObj.notifyAll();
	    } 
//  	    else {
//  		// 11/12/01 UCB DEBUG
//  		System.err.println("ChanInfoBuffer is full; discarding data.");
//  	    }
	}
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	addChanInfo                                     ***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method allows a client class of ChanInfoBuffer  ***
  ***   to add ChanInfo specifications to the rear of the       ***
  ***   buffer.  If the buffer is full, the ChanInfo will not   ***
  ***   be created or added.                                    ***
  ***								***
  ***	05/04/2005 - JPW					***
  ***		Remove duration					***
  ***								***
  *****************************************************************
*/
    public void addChanInfo(byte[] data, long start) {
	synchronized (syncObj) {
	    if (!isFull()) {
		rear = incrementOf(rear);
		buffer[rear] = new ChanInfo(data, start);
		syncObj.notifyAll();
	    } else {
		// 11/12/01 UCB DEBUG
		System.err.println("ChanInfoBuffer is full; discarding data.");
	    }
	}
    }

    /**************** PRIVATE METHODS *****************/
/*
  *****************************************************************
  ***								***
  ***	Name :	incrementOf                              	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   A method providing the next index for the pointers   ***
  ***   into the buffer, where the pointers "wrap" around the   ***
  ***   buffer.                                                 ***
  ***								***
  *****************************************************************
*/
    private int incrementOf(int val) {
	return (val + 1) % length;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isEmpty                                 	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method determines whether the buffer is empty.  ***
  ***   ONLY CALL THIS METHOD WITHIN A BLOCK SYNC'ED ON SYNCOBJ!***
  ***								***
  *****************************************************************
*/
    private boolean isEmpty() {
	return front == rear;
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	isFull                                   	***
  ***	By   :	U. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	November, 2001                                  ***
  ***								***
  ***	Copyright 2001 Creare Inc.      			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method determines whether the buffer is empty.  ***
  ***   ONLY CALL THIS METHOD WITHIN A BLOCK SYNC'ED ON SYNCOBJ!***
  ***								***
  *****************************************************************
*/
    private boolean isFull() {
	return incrementOf(rear) == front;
    }
}
