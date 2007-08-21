/*
 * (C) Copyright IBM Corp. 2000  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 * 
 * Portions Copyright (C) Simulacra Media Ltd, 2004.
 */

package com.ibm.webdav;

import org.w3c.dom.*;


/**
 * Utility class for serialising DOM nodes.
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *
 */
public class XMLUtility {
    public XMLUtility() {
    }

    public static String printNode(Node node) {
        StringBuffer sBuffer = new StringBuffer();

        printNode(sBuffer, node);

        return sBuffer.toString();
    }

    /** Takes XML node and prints to String.
    *
    * @param node Element to print to String
    * @return XML node as String
    */
    private static void printNode(StringBuffer sBuffer, Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            sBuffer.append(encodeXMLText(node.getNodeValue().trim()));
        } else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
            sBuffer.append("<![CDATA[");
            sBuffer.append(node.getNodeValue());
            sBuffer.append("]]>");
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element el = (Element) node;

            sBuffer.append("<").append(el.getTagName());

            NamedNodeMap attribs = el.getAttributes();

            for (int i = 0; i < attribs.getLength(); i++) {
                Attr nextAtt = (Attr) attribs.item(i);
                sBuffer.append(" ").append(nextAtt.getName()).append("=\"")
                       .append(nextAtt.getValue()).append("\"");
            }

            NodeList nodes = node.getChildNodes();

            if (nodes.getLength() == 0) {
                sBuffer.append("/>");
            } else {
                sBuffer.append(">");

                for (int i = 0; i < nodes.getLength(); i++) {
                    printNode(sBuffer, nodes.item(i));
                }

                sBuffer.append("</").append(el.getTagName()).append(">");
            }
        }
    }
    
	/**
	 * Handles XML encoding of text, e.g. & to &amp;
	 * 
	 * @param sText Text to XML encode
	 * @return XML Encoded text
	 */
	public static String encodeXMLText(String sText) {
		StringBuffer sBuff2 = new StringBuffer(sText);
		StringBuffer sNewBuff = new StringBuffer();

		for (int i = 0; i < sBuff2.length(); i++) {
			char currChar = sBuff2.charAt(i);
			Character currCharObj = new Character(sBuff2.charAt(i));
			if (currChar == '&') {
				if ((sBuff2.length() - 1 - i) >= 4
					&& sBuff2.charAt(i + 1) == 'a'
					&& sBuff2.charAt(i + 2) == 'm'
					&& sBuff2.charAt(i + 3) == 'p'
					&& sBuff2.charAt(i + 4) == ';') {
					i = i + 4;
					sNewBuff.append("&amp;");
				} else {
					sNewBuff.append("&amp;");
				}
			} else if (currChar == '>') {
				sNewBuff.append("&gt;");
			} else if (currChar == '<') {
				sNewBuff.append("&lt;");
			} else {
				sNewBuff.append(currChar);
			}
		}

		return sNewBuff.toString();

	}
}