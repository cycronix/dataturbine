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
  ***	Name :	SettingsReader		                        ***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II		      			***
  ***	Date :	April 2002              			***
  ***								***
  ***	Copyright 2002 Creare Inc.       			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class is used to retrieve application settings  ***
  ***   from a "flat" file (i.e., no hierarchy within the data).***
  ***								***
  ***	Modification History :					***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;

public class SettingsReader {
    
    private Hashtable match = null;

    public SettingsReader() {} // constructor

    public void setMatchHash(Hashtable matchI) {
	// Pass to SettingsReader a Hashtable containing the names
	// of settings to be matched from the files, and their
	// default values.
	match = matchI;
    }

    public void addMatch(String name, String defaultValue) {
	// Append a single settings-name/settings-default-value
	// pair to the Hashtable of settings to be matched.
	if (match == null) {
	    match = new Hashtable();
	}
	match.put(name, defaultValue);
    }

    public static boolean settingsExist(String docName) {
	// Client class can check for pre-existance of file.
	return SettingsWriter.settingsExist(docName);
    }

    public Hashtable getMatch() {
	return match;
    }

    public void readSettingsFrom(String docName) throws IOException {
	if (match == null) {
	    return;
	}

	FileReader fr = new FileReader(docName);
	int charRead;
	String key = null;
	String value = null;
	StringBuffer sb = new StringBuffer();
	boolean inErrorState = false;

	// The file should be in the format "key{value}key{value}...".
	// WHITESPACE IS NOT IGNORED.  NEWLINES ARE NOT IGNORED.
	// If an error is encountered during reading (null key), we
	// cease parsing and discard chars until the next '}' is encountered.

	READING: while ((charRead = fr.read()) != -1) {

	    switch ((char) charRead) {
		
	    case '{':
		if (sb.length() == 0) {
		    inErrorState = true; // null keys are illegal.
		    continue READING;
		}
		key = sb.toString();
		sb = new StringBuffer();
		break;

	    case '}':
		if (inErrorState || key == null) {
		    // reset from errorState to resume parsing.
		    inErrorState = false;
		    continue READING;
		}
		if (sb.length() == 0) {
		    value = "";  // null values are legal.
		} else {
		    value = sb.toString();
		    sb = new StringBuffer();
		}
		if (match.containsKey(key)) {
		    match.put(new String(key), new String(value));
		    key = null; // clear the old key.
		    value = null; // clear the old value.
		}
		break;

	    default:
		if (inErrorState) {
		    continue READING; // discard chars while inErrorState.
		}
		sb.append((char) charRead);
	    }
	}

	fr.close();
    }

}
