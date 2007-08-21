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

// ConfigCubby class - cubbyhole for passing channel names and hashtables between main frame (memus)
// and RBNBInterface classes
// unlike other cubbyholes, this one blocks on reading since communication needs to be synchronous
// note that notifyAll() and while must be used, to prevent deadlocks if more than one get is waiting

package com.rbnb.plot;

//EMF 5/18/01: use replacement Channel
//import COM.Creare.RBNB.API.Channel;
import java.util.Hashtable;

public class ConfigCubby {
	private Channel[] channels=null;
	private boolean newChannels=false;
	private Channel channel=null;
	private boolean newChannel=false;
	private Hashtable ht=null;
	private boolean newHash=false;

// default constructor method is sufficient
	
// setChannels method - sets list of channel names
public synchronized void setChannels(Channel[] ch) {
	while (newChannels) {
		try {
			wait();
			}
		catch (InterruptedException e) {
			System.out.println("ConfigCubby.setChannels: exception");
			e.printStackTrace();
			}
		}
	channels=ch;
	newChannels=true;
	notifyAll();
	}

// getChannels method - returns list of channel names
public synchronized Channel[] getChannels() {
	while (!newChannels) {
		try {
			wait();
			}
		catch (InterruptedException e) {
			System.out.println("ConfigCubby.getChannels: exception");
			e.printStackTrace();
			}
		}
	newChannels=false;
	notifyAll();
	return channels;
	}

// setChannel method - sets single channel name
public synchronized void setChannel(Channel ch) {
	while (newChannel) {
		try {
			wait();
			}
		catch (InterruptedException e) {
			System.out.println("ConfigCubby.setChannels: exception");
			e.printStackTrace();
			}
		}
	channel=ch;
	newChannel=true;
	notifyAll();
	}

// getChannel method - returns single channel name
public synchronized Channel getChannel() {
	while (!newChannel) {
		try {
			wait();
			}
		catch (InterruptedException e) {
			System.out.println("ConfigCubby.getChannels: exception");
			e.printStackTrace();
			}
		}
	newChannel=false;
	notifyAll();
	return channel;
	}

// setHash method - sets hashtable containing configuration information
public synchronized void setHash(Hashtable h) {
	while (newHash) {
		try {
			wait();
			}
		catch (InterruptedException e) {
			System.out.println("ConfigCubby.setHash: exception");
			e.printStackTrace();
			}
		}
	ht=h;
	newHash=true;
	notifyAll();
	}

// getHash method - returns hashtable containing configuration information
public synchronized Hashtable getHash() {
	while (!newHash) {
		try {
			wait();
			}
		catch (InterruptedException e) {
			System.out.println("ConfigCubby.getHash: exception");
			e.printStackTrace();
			}
		}
	newHash=false;
	notifyAll();
	return ht;
	}

} //end class ConfigCubby
