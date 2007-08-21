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
  ***	Name :	SettingsWriter		                        ***
  ***	By   :	U. C. Bergstrom    (Creare Inc., Hanover, NH)	***
  ***	For  :	E-Scan II		      			***
  ***	Date :	April 2002              			***
  ***								***
  ***	Copyright 2002 Creare Inc.       			***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class is used to store application settings in  ***
  ***   a "flat" file (i.e., no hierarchy within the data).     ***
  ***								***
  ***	Modification History :					***
  ***	09/28/2004	JPW	In writeSettingsTo(): In order	***
  ***				to compile under J#, change	***
  ***				variable name from enum to eEnum***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;

public class SettingsWriter {
    
    private Hashtable settings = null;

    public SettingsWriter() {}  // constructor.

    public void setSettingsHash(Hashtable settingsI) {
	// Pass to SettingsReader a Hashtable containing the names
	// of settings to be written the files, and their values.
	settings = settingsI;
    }

    public void addSetting(String name, String value) {
	// Append a single settings-name/value pair to the Hashatable
	// of settings.
	if (settings == null) {
	    settings = new Hashtable();
	}
	settings.put(name, value);
    }

    public static boolean settingsExist(String docName) {
	// Client class can check for pre-existance of file.
	// Pre-existance is likely to be a pre-req for
	// most all clients of SettingsWriter.
	File file = new File(docName);
	if (!file.exists() || !file.isFile()) {
	    return false;
	}
	return true;
    }

    public void writeSettingsTo(String docName) throws IOException {
	if (settings == null) {
	    return;
	}

	FileWriter fw = new FileWriter(docName);

	// JPW 09/28/04: In order to compile under J#, change variable name
	//               from enum to eEnum
	Enumeration eEnum = settings.keys();
	String key;
	String val;
	while(eEnum.hasMoreElements()) {
	    key = (String) (eEnum.nextElement());
	    val = (String) (settings.get(key));

	    if (val != null) {
		fw.write(key + "{" + val + "}");
	    }
	}

	fw.flush();
	fw.close();
    }

}
