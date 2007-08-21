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
  ***	Name :	MulticastPlayerOutput		                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001, 2004 Creare Inc.			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Multicast PlayerOutput is a subclass of              ***
  ***   UDPPlayerOutput, used to output Maps to a mulitcast UDP ***
  ***   socket.                                                 ***
  ***								***
  ***	Modification History					***
  ***	10/07/2004	JPW	Upgrade to RBNB V2 Player	***
  ***								***
  *****************************************************************
*/

package com.rbnb.player;

import com.rbnb.utility.MulticastOutputStream;

import java.net.MulticastSocket;
import java.net.InetAddress;

class MulticastPlayerOutput extends UDPPlayerOutput {

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastPlayerOutput		                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires a destination InetAddress and ***
  ***   port.                                                   ***
  ***								***
  *****************************************************************
*/
    public MulticastPlayerOutput() {
	
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	MulticastPlayerOutput		                ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Constructor.  Requires a destination InetAddress and ***
  ***   port.                                                   ***
  ***								***
  *****************************************************************
*/
    public MulticastPlayerOutput(InetAddress iAddI, int port)
        throws Exception
    {
	connect(iAddI, port);
    }

/*
  *****************************************************************
  ***								***
  ***	Name :	connect  		                 	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	May, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   Connects to UDPOutputStream.                         ***
  ***								***
  *****************************************************************
*/
    public void connect(InetAddress iAdd, int port) throws Exception {
	udpOut = new MulticastOutputStream(iAdd, port);
    }
}

