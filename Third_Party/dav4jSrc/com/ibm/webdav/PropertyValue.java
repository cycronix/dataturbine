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
import java.io.*;
import javax.xml.parsers.*;

/** A PropertyValue represents the value of a property of a resource and
 * it's status.
 *
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class PropertyValue extends Object implements java.io.Serializable
{
   /** the value of the property as an XML element
	*/
   public Element value = null;

   /** the status of the property as returned from a Resource.getProperty()
	* method.
	*/
   public int status = WebDAVStatus.SC_OK;
/** Create a PropertyValue
 *
 * @param value an XML DOM Element representing the value of the property
 * @param status WebDAV the status code of the property as returned from one of the many
 * Resource.getProperty() methods.
 */
public PropertyValue(Element value, int status) {
	this.value = value;
	this.status = status;
}
/** The status of the property as returned from a Resource.getProperty()
 * method.
 * @return the HTTP or WebDAV status code for a property
 */
public int getStatus()
{
	return status;
}
/** The value of the property as an XML element
 * @return the value of a property
 */
public Element getValue() {
	return value;
}
/** Format a PropertyValue as a string of the form: value(status)
 * @return a string representation of a PropertyValue
 */
static public String nodeToString( Element el ) {

	return XMLUtility.printNode(el);
}
/** De-serialize a PropertyValue from a stream.
 * @param in the stream to read from
 * @exception IOException
 * @exception ClassNotFoundException
 */
private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	status = in.readInt();
	int size = in.readInt();
	byte[] buffer = new byte[size];
	in.readFully(buffer);
	ByteArrayInputStream is = new ByteArrayInputStream(buffer);
	Document contents = null;

        try {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setNamespaceAware(true);
          DocumentBuilder docbuilder = factory.newDocumentBuilder();
          contents = docbuilder.parse(new org.xml.sax.InputSource(is));
	} catch(Exception e) {
          throw new IOException(e.getMessage());
        }

        value = contents.getDocumentElement();
}
/** Format a PropertyValue as a string.  This
 * usually returns the content of the protocol tag.
 * @return a string representation of a PropertyValue
 */
public String toContentString() {
	Element el = (Element)value;
        NodeList children = el.getChildNodes();
	//java.util.Enumeration children = el.elements();
	String retval = "";

	for(int i=0;i<children.getLength();i++) {
		Node child = (Node)children.item(i);
		retval += XMLUtility.printNode( child );
	}
	return retval;
}
/** Format a PropertyValue as a string of the form: value(status)
 * @return a string representation of a PropertyValue
 */
public String toString() {
	StringWriter s = new StringWriter();
	PrintWriter pout = new PrintWriter(s);
	try {
                pout.print(XMLUtility.printNode(value));
		//((Element) value).print(pout);
		pout.flush();
		s.write(" (" + status + ")");
		s.close();
	} catch (IOException exc) {
	}
	return s.toString();
}
/** Serialize a PropertyValue to a stream.
 * @param in the stream to write to
 * @exception IOException
 */
private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch(Exception e) {
          throw new IOException(e.getMessage());
        }
	//document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);
	document.appendChild(value);
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	PrintWriter pw = new PrintWriter(os, false);
	//document.print(pw);
        pw.print(XMLUtility.printNode(document.getDocumentElement()));
	out.writeInt(status);
	out.writeInt(os.size());
	out.write(os.toByteArray());
}
}
