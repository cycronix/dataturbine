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
  ***	Name :	U2TUpdate                        	        ***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***       This class is used to pass status updates from      ***
  ***   a UDPToTCP object to its U2TProcessor.                  ***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

public class U2TUpdate {
    public static final int CLIENT_CONNECTED = 0;
    public static final int CLIENT_DISCONNECTED = 1;
    public static final int TCP_SERVER_CLOSED = 2;
    public static final int UDP_SOCKET_CLOSED = 3;
    public static final int TCP_SERVER_EXCEPTION = 4;
    public static final int UDP_SOCKET_EXCEPTION = 5;
    public static final int CLIENT_EXCEPTION = 6;

    public int state;
    public String info = "";

    public U2TUpdate(int stateI, String text) {
	state = stateI;
	info = text;
    }
}
