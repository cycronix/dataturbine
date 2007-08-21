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
  ***	Name :	MulticastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This class extends UDPOutputStream.                 ***
  ***								***
  *****************************************************************
*/

package com.rbnb.utility;

import java.io.IOException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MulticastOutputStream extends UDPOutputStream {

    private int timeToLive = 8;

    /********************** constructors ********************/
/*
  *****************************************************************
  ***								***
  ***	Name :	MutlicastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	October, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Default constructor.                                ***
  ***								***
  *****************************************************************
*/
    public MulticastOutputStream() {}

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	October, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Constructor.  Sets size of buffer.                  ***
  ***								***
  *****************************************************************
*/
    public MulticastOutputStream(int buffSize) {
	setBufferSize(buffSize);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	October, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Constructor.  Sets the address and port of the      ***
  ***   Multicast group to write to.                            ***
  ***								***
  *****************************************************************
*/
    public MulticastOutputStream(String address, int portI) 
	throws UnknownHostException, SocketException, IOException {

	open(InetAddress.getByName(address), portI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	November, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Constructor.  Sets the address and port of the      ***
  ***   Multicast group to write to.                            ***
  ***								***
  *****************************************************************
*/
    public MulticastOutputStream(InetAddress address, int portI) 
	throws SocketException, IOException {

	open(address, portI);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	October, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Constructor.  Sets the address and port of the      ***
  ***   Multicast group to write to.  Sets the size of the      ***
  ***   buffer.                                                 ***
  ***								***
  *****************************************************************
*/
    public MulticastOutputStream(String address, 
				 int portI, 
				 int buffSize) 
	throws UnknownHostException, SocketException, IOException {

	open(InetAddress.getByName(address), portI);
	setBufferSize(buffSize);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastOutputStream                       	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	October, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       Constructor.  Sets the address and port of the      ***
  ***   Multicast group to write to.  Sets the size of the      ***
  ***   buffer.                                                 ***
  ***								***
  *****************************************************************
*/
    public MulticastOutputStream(InetAddress address, 
				 int portI, 
				 int buffSize) 
	throws SocketException, IOException {

	open(address, portI);
	setBufferSize(buffSize);
    }

    /************ Time To Live ************/
/*
  *****************************************************************
  ***								***
  ***	Name :	setTTL                                	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II      					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method sets the Time To Live.                  ***
  ***								***
  *****************************************************************
*/
    public void setTTL(int ttlI) throws IOException {
	timeToLive = ttlI;
	((MulticastSocket) dsock).setTimeToLive(timeToLive);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	getTTL                                	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II      					***
  ***	Date :	April, 2002					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This method gets the Time To Live.                  ***
  ***								***
  *****************************************************************
*/
    public int getTTL() {
	return timeToLive;
    }

    /************ opening and closing the stream ************/
/*
  *****************************************************************
  ***								***
  ***	Name :	open                                   	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	October, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       The user may use this method to set the address and ***
  ***   port of the Multicast group to write to.                ***
  ***								***
  *****************************************************************
*/
    public void open(InetAddress address, int portI)
	throws SocketException, IOException {

	iAdd = address;
	port = portI;

	dsock = new MulticastSocket(port);
	((MulticastSocket) dsock).joinGroup(iAdd);
    }
}
