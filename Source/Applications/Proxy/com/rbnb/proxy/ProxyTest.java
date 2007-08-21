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

package com.rbnb.proxy;

//ProxyTest - write gobs of data to a port, as a test of Proxy
// EMF 4/1/03
// INB 11/21/2003 - moved in the V2 directory structure
// Copyright 2003 Creare Incorporated

import java.io.*;
import java.util.*;
import java.net.*;

public class ProxyTest {
	
  public final static void main(String args[]) {
    try {
		//build up data
		short numChan=24;
		ByteArrayOutputStream[] data=new ByteArrayOutputStream[numChan];
		for (short chan=0;chan<numChan;chan++) {
			data[chan]=new ByteArrayOutputStream();
			DataOutputStream dos=new DataOutputStream(data[chan]);
			for (short i=0;i<100;i++) dos.writeShort(chan+1);
			for (short i=0;i<100;i++) dos.writeShort(-chan-1);
		}
			
		//connect to Proxy
		Socket ss = new Socket("localhost",3000);
		OutputStream os = ss.getOutputStream();

		//loop forever, dumping data
		while (true) {
			for (short chan=0;chan<numChan;chan++) {
				data[chan].writeTo(os);
			}
			os.flush();
			//pace the effort
			try {
				Thread.currentThread().sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
  } //end method Main
  
} //end class ProxyTest
