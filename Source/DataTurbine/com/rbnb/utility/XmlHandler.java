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

// XmlHandler - simple xml handler
// Eric Friets
// October 2005

// For now, assumes nodeNames are not repeated.  This is
// a convenience class, and is not fully featured.  One
// could say it is only minimally featured.  It does, however,
// neatly encapsulate the somewhat complex and wordy Java DOM coding.

package com.rbnb.utility;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHandler {
	Document doc=null;
	
	//create with xml string as input
	public XmlHandler(String xmlString) {
		try {
			//create DOM object representation
			ByteArrayInputStream bais=new ByteArrayInputStream(xmlString.getBytes());
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			doc=db.parse(bais);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//return value of specified node
	// returns null if no such node, or node has no associated value
	public String getValueOf(String nodeName) {
		if (doc==null) return null;
		NodeList nl=doc.getElementsByTagName(nodeName);
		if (nl.getLength()==0) return null;
		Node n=nl.item(0);
		if (n.hasChildNodes()) return n.getFirstChild().getNodeValue();
		return null;
	}
	
} //end class XmlHandler
