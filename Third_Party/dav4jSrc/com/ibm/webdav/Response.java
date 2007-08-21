package com.ibm.webdav;

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

import org.w3c.dom.*;

/** A Response describes the effect of a method on a resource and/or
 * its properties. See concrete subclasses <code>MethodResponse</code>
 * and <code>PropertyResponse</code> for additional details. A <code>
 * MultiStatus</code> contains a collection of Response instances, one
 * for each resource effected by the method sent.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.MethodResponse
 * @see com.ibm.webdav.PropertyResponse
 * @see com.ibm.webdav.MultiStatus
 */
public abstract class Response extends Object implements java.io.Serializable
{

   public static String HTTPVersion = "HTTP/1.1";


   protected Document document = null;
   private String resource = null; // the URL of the resource this is the response for
   private String description = null;
/** A Response should be constructed with a Document Element
 */
public Response() {
}
/** Construct a Response from an XML response element
 *
 * @param document the document that will contain the Response
 *        when output as XML.
 * @exception com.ibm.webdav.ServerException thrown if the XML for the response is incorrect
 */
public Response(Document document) throws ServerException {
	this.document = document;
}
/** Construct an empty Response for some resource.
 *
 * @param url the URL of the resource this is a response for
 *
 */
public Response(String url) {
	resource = url;
}
/** Translate this Response into an XML response element.
 *
 */
public abstract Element asXML();
/** Get the description of this response
 *
 * @return the description
 */
public String getDescription() {
	return description;
}
/** Get the URL of the resource this response describes
 * @return the URL of the resource associated with this response
 */
public String getResource() {
	return resource;
}
/** Check to see if this response contains any errors.
 *
 * @return true if all response does not contain an error, false otherwise.
 */
public abstract boolean isOK();
/** Set the description of this response
 *
 * @param value the new description
 */
public void setDescription(String value) {
	description = value;
}
/** Set the document the response element is placed in when translated to XML
 *
 * @param document usually a MultiStatus document
 */
public void setDocument(Document document) {
	this.document = document;
}
/** Set the URL of the resource this response describes
 */
protected void setResource(String url) {
	resource = url;
}
/** Convert this Response to a PropertyResponse.
 * This method is used to convert MethodResponses to PropertyResponses
 * when an error occurred accessing the properties of some member.
 *
 */
public abstract PropertyResponse toPropertyResponse();
/** Convert this Response to a String representation (an XML document).
 * The String is formatted, and will therefore contain ignorable whitespace.
 * Use response.asXML().print(pout) if ignorable whitespace is not desired.
 * @return a string representation of the Response as an XML document
 *
 */
public String toString() {
	/*ByteArrayOutputStream os = new ByteArrayOutputStream();
	PrintWriter pout = new PrintWriter(os);
	TXDocument document = (TXDocument) asXML();
	try {
		document.printWithFormat(pout);
	} catch (Exception exc) {
	}
	return os.toString();*/
        return printNode(asXML());
}

 /** Takes XML node and prints to String.
   *
   * @param node Element to print to String
   * @return XML node as String
   */
  public static String printNode( Node node ) {
    StringBuffer sBuffer = new StringBuffer();

    if( node.getNodeType() == Node.TEXT_NODE ) {
        sBuffer.append( node.getNodeValue() ) ;
    }
    else if( node.getNodeType() == Node.CDATA_SECTION_NODE ) {
        sBuffer.append("<![CDATA[");
        sBuffer.append( node.getNodeValue() ) ;
        sBuffer.append("]]>\n");
    }
    else if( node.getNodeType() == Node.ELEMENT_NODE ) {
        Element el = (Element)node ;

        sBuffer.append( "<" + el.getTagName() ) ;

        NamedNodeMap attribs = el.getAttributes() ;
        for( int i=0 ; i < attribs.getLength() ; i++ ) {
           Attr nextAtt = (Attr)attribs.item(i) ;
           sBuffer.append( " " + nextAtt.getName() + "=\"" + nextAtt.getValue() + "\"" ) ;
        }

        NodeList nodes = node.getChildNodes() ;

        if( nodes.getLength() == 0 ) {
            sBuffer.append("/>\n") ;
        }
        else {
            sBuffer.append(">\n") ;

            for( int i = 0 ; i < nodes.getLength() ; i++ ) {
                sBuffer.append(printNode(nodes.item(i) )) ;
            }
            sBuffer.append( "</" + el.getTagName() + ">\n" ) ;
        }
    }

      return(sBuffer.toString());
  }
}
