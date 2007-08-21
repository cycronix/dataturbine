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

// RegChannel.java - class holds registered channel attributes, and provide convenience methods
//                   for using them
// Eric Friets, September 1998
// Copyright 1998 Creare Inc.
//
// Revisions:
// 04/05/2005  JPW  This class now implements the Comparable interface; I added
//                  compareTo() and toString() methods.  This allows us to sort
//                  arrays of RegChannel objects, as I do in JChannelDialog.

package com.rbnb.plot;

import java.lang.Comparable;

//EMF 5/18/01: use replacement Channel
//import COM.Creare.RBNB.API.Channel;

public class RegChannel implements Comparable {

public String name = null;
public short staticUserDataType = 0;
public byte[] staticUserData = null;
public short channelUserDataType=0;
public byte[] channelUserData=null;
//EMF 8/8/05: add mime type
public String mime=null;

//default constructor
public RegChannel() {
	}

//constructor - name only
public RegChannel(String chanName) {
	name=chanName;
	}

// constructor from Channel
public RegChannel(Channel chI) {
	name=chI.channelName;
	staticUserDataType=chI.channelUserDataType;
	staticUserData=chI.channelUserData;
	setMime(chI.getMimeType());
	}
	
//EMF 8/8/05: add get/set mime methods
public void setMime(String mimeI) {
	mime=mimeI;
}

public String getMime() {
	return mime;
}

public String toString() {
	return name;
	}

public int compareTo(Object otherRegChannelI) {
	if (!(otherRegChannelI instanceof RegChannel)) {
	    System.err.println(
	        "RegChannel.compareTo(): ERROR: " +
		"other object is not a RegChannel");
	    // Just return something
	    return -1;
	    }
	return
	    this.toString().compareTo(
	        ((RegChannel)otherRegChannelI).toString());
	}

// isStaticUserData method - returns boolean indicating nonzero static userdata of specified type
public boolean isStaticUserData(short type) {
	if (type!=staticUserDataType || staticUserData.length==0) return false;
	else return true;
	}

// copy method - returns copy of RegChannel object
public RegChannel copy() {
	RegChannel rc = new RegChannel();
	rc.name=new String(name);
	rc.staticUserDataType=staticUserDataType;
	if (staticUserData!=null) {
		rc.staticUserData=new byte[staticUserData.length];
		System.arraycopy(staticUserData,0,rc.staticUserData,0,staticUserData.length);
		}
	return rc;
	}

// will probably want a method that parses the byte array and returns a hashtable

}
