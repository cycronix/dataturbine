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
  ***	Name :	U2TProcessor                            	***
  ***	By   :	U. Bergstrom   (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan						***
  ***	Date :	August, 2001					***
  ***								***
  ***	Copyright 2001 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***      This interface is used in association with UDPToTCP. ***
  ***   It permits the receipt of status updates and the        ***
  ***   processing of each UDP data packet before it is re-sent ***
  ***   out the TCP connection.                                 ***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

// NOTE: If an implementation of this interface calls UDPToTCP.start()
// or .close() from within the U2TProcessor methods, a
// U2TForbiddenError will be thrown.

public interface U2TProcessor {
    
    // Passes a U2TUpdate object, indicating a change in status.
    public void statusUpdated(U2TUpdate update);

    // Permits alteration of received data before re-transmition.
    public byte[] process(byte[] date);

}
