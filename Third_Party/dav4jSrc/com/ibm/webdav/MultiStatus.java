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

import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

/** A MultiStatus contains multiple Response instances resulting from the
 * invocation of a method on a resource. There will generally be one Response for
 * each resource effected by the method.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.MethodResponse
 * @see com.ibm.webdav.PropertyResponse
 */
public class MultiStatus extends Object implements Serializable {
	private Vector responses = new Vector();
	private String description = null;
   /** Construct an empty MultiStatus.
	*
	*/
   public MultiStatus() throws ServerException
   {
   }
/** Construct a MultiStatus from an XML DOM Document.
 *
 * @param document the XML Document containing a DAV:multistatus element
 *    used to construct a MultiStatus object
 *
 */
public MultiStatus(Document document) throws ServerException {
	init(document);
}
/** Add a Response to this MultiStatus
 *
 * @param response the Response to add
 */
public void addResponse(Response response) {
	responses.addElement(response);
}
/** Convert a MultiStatus into an XML DOM Document containing a
 * DAV:multistatus element.
 * @return an XML document containing a DAV:multistatus representing this MultiStatus
 */
public Document asXML() {

	Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          e.printStackTrace(System.err);
          throw new RuntimeException(e.getMessage());
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element multistatus = document.createElementNS("DAV:","D:multistatus");

	multistatus.setAttribute("xmlns:D", "DAV:");
	document.appendChild(multistatus);
	Enumeration responses = getResponses();
	while (responses.hasMoreElements()) {
		Response response = (Response) responses.nextElement();
		response.setDocument(document);
                
		multistatus.appendChild(document.importNode(response.asXML(),true));
	}
	if (getDescription() != null) {
		Element description = document.createElementNS("DAV:","D:responsedescription");

                description.appendChild(document.createTextNode(getDescription()));
		multistatus.appendChild(description);
	}

        //System.out.println(Response.printNode(multistatus));

	return document;
}
/** Get the active lock on this resource owned by the given principal if any.
 * This is a convenience method to get the active lock granted by a lock request
 * from the returned MultiStatus. A WebDAVException is raised if the MultiStatus
 * does not contain a PropertyResponse containing a DAV:lockdiscovery property.
 * <p>
 * NOTE: this method cannot be reliably implemented based on the version 10 of
 * the WebDAV spec as an activelock element in a lockdiscovery does not contain
 * the authorization credentials of the owner of the lock. For now, this method
 * relies on an additional principal element in the activelock that contains
 * the required id that is an IBM EXTENSION.
 *
 * @param principal the authorization id of the requesting principal
 *
 * @return the active lock owned by that principal or null if the resource is
 * not locked by that principal.
 * @exception com.ibm.webdav.WebDAVException raised if the MultiStatus doesn't contain a DAV:lockdiscovery property
 */
public ActiveLock getActiveLockFor(String principal) throws WebDAVException {
	if (!isOK()) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Can't access lock information lock method failed");
	}
	if (!(responses.elementAt(0) instanceof PropertyResponse)) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "MultiStatus doesn't contain a property response");
	}
	PropertyResponse response = (PropertyResponse) responses.elementAt(0);
	PropertyValue lockdiscovery = response.getProperty( PropertyName.pnLockdiscovery );
	if (lockdiscovery == null) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "MultiStatus doesn't contain a property response containing a lockdiscovery property");
	}

	// get the active locks from the lockdiscovery and see if one is owned
	// by this principal. This relies on the principal element added to the
	// activelock element to identify the owning principal
	NodeList locks = ((Element) lockdiscovery.value).getElementsByTagNameNS("DAV:","activelock");
	Element lockElement = null;
	ActiveLock ownedLock = null;
	for (int i = 0; i < locks.getLength(); i++) {
		lockElement = (Element) locks.item(i);
		ActiveLock lock = new ActiveLock(lockElement);
		if (lock.getPrincipal().equals(principal)) {
			ownedLock = lock;
		}
	}
	return ownedLock;
}
/** Get the overall summary description for this MultiStatus.
 *
 * @return a synopsis of the responses in the MultiStatus
 */
public String getDescription() {
	return description;
}
/** Get the responses in this MultiStatus
 *
 * @return Enumeration of Response instances
 * @see com.ibm.webdav.MethodResponse
 * @see com.ibm.webdav.PropertyResponse
 */
public Enumeration getResponses() {
	return responses.elements();
}
/** Initialize a MultiStatus from an XML DOM Document.
 *
 * @param document an XML Document containing a DAV:multistatus element
 * @exception com.ibm.webdav.ServerException
 */
private void init(Document document) throws ServerException {
	responses = new Vector();
	Element multistatus = (Element) document.getDocumentElement();
	NodeList responses = multistatus.getElementsByTagNameNS("DAV:","response");
	Element response = null;
	for (int i = 0; i < responses.getLength(); i++) {
		response = (Element) responses.item(i);
		Response aResponse = null;
		if (response.getElementsByTagNameNS("DAV:","propstat").item(0) != null) {
			aResponse = new PropertyResponse((Document) document, response);
		} else {
			aResponse = new MethodResponse((Document) document, response);
		}
		this.responses.addElement(aResponse);
	}
	Element responseDescription = (Element) multistatus.getElementsByTagNameNS("DAV:","responsedescription");
	if (responseDescription != null) {
		setDescription(((Text) responseDescription.getFirstChild()).getData());
	}
}
/** Check to see if all responses in this MultiStatus were successful.
 *
 * @return true if all method response status codes are successful, false otherwise.
 */
public boolean isOK() {
	boolean isok = true;
	Enumeration responses = this.getResponses();
	while (isok && responses.hasMoreElements()) {
		Response response = (Response) responses.nextElement();
		if (response instanceof MethodResponse) {
			isok = isok && ((MethodResponse) response).isOK();
		}
	}
	return isok;
}
/** Merge the responses in childMultiStatus into this MultiStatus.
 *
 * @param childMultiStatus contains the responses to merge
 */
public void mergeWith(MultiStatus childMultiStatus) {
	Enumeration responses = childMultiStatus.getResponses();
	while (responses.hasMoreElements()) {
		Response response = (Response) responses.nextElement();
		addResponse(response);
	}
}
/** De-serialize a MultiStatus as an XML document.
 * @param in the stream to read from
 */
private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	int size = in.readInt();
	byte[] buffer = new byte[size];
	in.readFully(buffer);
	ByteArrayInputStream is = new ByteArrayInputStream(buffer);

	// TODO: the parser closes the stream with it is done reading. This closes
	// the rmi socket!
        Document contents = null;

	try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                contents = docbuilder.parse(new org.xml.sax.InputSource(is));
		init(contents);
	} catch (Exception exc) {
		System.err.println(exc);
		throw new IOException(exc.getMessage());
	}
}
/** Remove responses that are OK from this MultiStatus. This method is used
 * for those situations where the MultiStatus may contain responses that are
 * not necessary to return to a client.
 */
public void removeOKResponses() {
	for (int i = responses.size() - 1; i >= 0; i--) {
		Response response = (Response) responses.elementAt(i);
		if (response.isOK()) {
			responses.removeElementAt(i);
		}
	}
}
/** Remove a Response from this MultiStatus. Ignores errors if the response is
 * not in the MultiStatus.
 *
 * @param response the Response to remove
 */
public void removeResponse(Response response) {
	try {
		responses.removeElement(response);
	} catch (Exception exc) {
	}
}
/** Set the overall summary description for this MultiStatus.
*
* @param value a synopsis of the responses in the MultiStatus
*/
public void setDescription(String value) {
	description = value;
}
/** Convert this MultiStatus to a String representation (an XML document).
* The String is formatted, and will therefore contain ignorable whitespace.
* Use multiStatus.asXML().print(pout) if ignorable whitespace is not desired.
* @return a string representation of a MultiStatus as an XML document
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
	pout.close();
	return os.toString();*/

        Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch(Exception e) {
          e.printStackTrace(System.err);
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element multistatus = document.createElementNS("DAV:","D:multistatus");

	multistatus.setAttribute("xmlns:D", "DAV:");
	document.appendChild(multistatus);
	Enumeration responses = getResponses();
	while (responses.hasMoreElements()) {
		Response response = (Response) responses.nextElement();
		response.setDocument(document);
                //System.out.println(response.toString());
		multistatus.appendChild(response.asXML());
	}
	if (getDescription() != null) {
		Element description = document.createElementNS("DAV:","D:responsedescription");

		description.appendChild(document.createTextNode(getDescription()));
		multistatus.appendChild(description);
	}

        return Response.printNode(multistatus);

}
/** Serialize a MultiStatus as an XML document.
 * @param out the stream to write to
 */
private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	Document document = (Document) asXML();
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	PrintWriter pw = new PrintWriter(os, false);
	pw.print(XMLUtility.printNode(document.getDocumentElement()));
        //document.print(pw);
	out.writeInt(os.size());
	out.write(os.toByteArray());
}
}
