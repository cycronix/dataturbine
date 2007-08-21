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
import java.util.*;


import java.text.ParseException;

/** A PropertyResponse describes the properties returned as the
 * result of a method send on a resource, e.g., getProperties(). A
 * <code>MultiStatus</code> contains a collection of Response instances,
 * one for each resource effected by the method sent.</p>
 * <p>
 * PropertyResponse also has convenience methods for all the DAV properties.
 * The method names correspond to the DAV property names suitably
 * modified to fit JavaBean conventions. If the property was not requested,
 * these convenience methods will return null or some other suitable value
 * to indicate the property value is unknown.</p>
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.MethodResponse
 * @see com.ibm.webdav.MultiStatus
 */
public class PropertyResponse extends Response implements java.io.Serializable {

   // a Dictionary whose key is a PropertyName and whose value is a PropertyValue
   private Hashtable pnproperties = new Hashtable();

   // a Dictionary matching a property name string into a PropertyName.
   // All PropertyNames in this dictionary have entries in the
   // pnproperites Dictionary.
   //
   // The property name string is the property's XML namespace concatenated with the
   // element local tag name (the name without the prefix and :). This is consistent
   // with the current WebDAV semantics for XML namespaces
   private Hashtable strxproperties = new Hashtable();

   //Table of property definitions
   private Hashtable propertydefs = new Hashtable();

/** Construct a Response from an XML DAV:response element
 *
 * @param document the document that will contain the Response
 *        when output as XML.
 * @param response the XML DAV:response element that is the source
 * @exception com.ibm.webdav.ServerException thrown if the XML for the response is incorrect
 * of this response
 */
public PropertyResponse(Document document, Element response) throws ServerException {
	super(document);
	try {
		// get the identity of the resource this is a response on
		Element href = (Element) response.getElementsByTagNameNS("DAV:","href").item(0);
		setResource(((Text) href.getFirstChild()).getData());

		// get the properties in the response, and their statuses
		NodeList propstats = response.getElementsByTagNameNS("DAV:","propstat");
		Element propstat = null;
		for (int i = 0; i < propstats.getLength(); i++) {
			propstat = (Element) propstats.item(i);
			// get the properties in the propstat. Note that there should be at most
			// one prop in the propstat, and it contains the actual properties
			Element prop = (Element) propstat.getElementsByTagNameNS("DAV:","prop").item(0);

			// this is the status for all properties in this propstat
			Element status = (Element) propstat.getElementsByTagNameNS("DAV:","status").item(0);
			String statusMessage = ((Text) status.getFirstChild()).getData();
			StringTokenizer statusFields = new StringTokenizer(statusMessage, " ");
			statusFields.nextToken(); // skip the HTTP version
			int statusCode = Integer.parseInt(statusFields.nextToken());
			NodeList properties = prop.getChildNodes();
			Node property = null;
			for (int j = 0; j < properties.getLength(); j++) {
				property = (Node) properties.item(j);
				// skip ignorable TXText elements
				if (property.getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element)property;
					//el.collectNamespaceAttributes();
					PropertyName propname = new PropertyName( el );
					addProperty(propname, (Element) property, statusCode);
				}
			}
		}
		Element responseDescription = (Element) response.getElementsByTagNameNS("DAV:","responsedescription").item(0);
		if (responseDescription != null) {
			setDescription(((Text) responseDescription.getFirstChild()).getData());
		}
	} catch (Exception exc) {
		exc.printStackTrace();
		throw new ServerException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Invalid PropertyResponse");
	}
}
/** Construct an empty Response for some resource.
 *
 * @param url the URL of the resource this is a response for
 *
 */
public PropertyResponse(String url) {
	super(url);
}
/** Add a property and its status to the collection of properties generated
 * as a result of sending a method to a resource.
 *
 * @param propertyName the name of the property to add.
 * @param propertyElement the value of the property
 * @param status its status
 * @exception com.ibm.webdav.ServerException thrown if the property is already in this response
 */
public void addProperty( PropertyName propertyName, Element propertyElement, int status) throws ServerException {
	if (pnproperties.contains(propertyName)) {
		throw new ServerException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "Duplicate property in a Response");
	} else {
		pnproperties.put(propertyName, new PropertyValue(propertyElement, status));
		strxproperties.put(propertyName.asExpandedString(), propertyName);
	}
}

/** Translate this Response into an XML response element.
 * @return a DAV:response XML element
 */
public Element asXML() {

	Element response = document.createElementNS("DAV:","D:response");

	Element href = document.createElementNS("DAV:","D:href");

	href.appendChild(document.createTextNode(getResource()));
	response.appendChild(href);

	// group the properties having the same status
	Hashtable byStatus = new Hashtable();
	Enumeration propertyNames = getPropertyNamesPN();
	while (propertyNames.hasMoreElements()) {
		PropertyName propertyName = (PropertyName) propertyNames.nextElement();
		PropertyValue propertyValue = getProperty(propertyName);
		Integer status = new Integer(propertyValue.status);

		// keep a Vector of property names having this status
		Vector props = null;
		if (byStatus.containsKey(status)) {
			props = (Vector) byStatus.get(status);
		} else {
			props = new Vector();
			byStatus.put(status, props);
		}
		props.addElement(propertyName);
	}
	// generate XML for the propstat
	Enumeration statuses = byStatus.keys();

	// if there are no properties, construct a propstat with a prop with no elements and
	// an SC_OK status,
	if (!statuses.hasMoreElements()) {
		Element propstat = document.createElementNS("DAV:","D:propstat");

                Element prop = document.createElementNS("DAV:","D:prop");

		propstat.appendChild(prop);
		Element statusElement = document.createElementNS("DAV:","D:status");

		String statusText = HTTPVersion + " " + WebDAVStatus.SC_OK + " " + WebDAVStatus.getStatusMessage(WebDAVStatus.SC_OK);
		statusElement.appendChild(document.createTextNode(statusText));
		propstat.appendChild(statusElement);
		response.appendChild(propstat);
	}

	// create a propstat for all those properties having the same status.
	while (statuses.hasMoreElements()) {
		Integer status = (Integer) statuses.nextElement();
		Enumeration propertiesHavingThisStatus = ((Vector) byStatus.get(status)).elements();
		Element propstat = document.createElementNS("DAV:","D:propstat");

		Element prop = document.createElementNS("DAV:","D:prop");


		// put all the properties having this status in the current prop element
		while (propertiesHavingThisStatus.hasMoreElements()) {
			PropertyName propertyName = (PropertyName) propertiesHavingThisStatus.nextElement();
			PropertyValue propertyValue = getProperty(propertyName);
			//Element property = document.createElement(pn2);

			try {
				prop.appendChild(document.importNode(propertyValue.value,true));
			} catch(NullPointerException e) {
				System.err.println("Null pointer for property - " + propertyName);
				throw e;
			}
			
		}
		propstat.appendChild(prop);
		String statusText = HTTPVersion + " " + status + " " + WebDAVStatus.getStatusMessage(status.intValue());
		Element statusElement = document.createElementNS("DAV:","D:status");

		statusElement.appendChild(document.createTextNode(statusText));
		propstat.appendChild(statusElement);
		response.appendChild(propstat);
	}

        Enumeration defKeys = propertydefs.keys();

        if(defKeys.hasMoreElements()) {
          Element propdefn = document.createElementNS("DAV:","D:propdefn");
          response.appendChild(propdefn);

          while(defKeys.hasMoreElements()) {
            Object key = defKeys.nextElement();
            PropertyDefinition propdef = (PropertyDefinition)propertydefs.get(key);

            propdefn.appendChild(document.importNode(propdef.asXML(),true));
          }

        }

	if (getDescription() != null) {
		Element description = document.createElementNS("DAV:","D:responsedescription");

		description.appendChild(document.createTextNode(getDescription()));
		response.appendChild(description);
	}
	return response;
}
/** Get the active locks in the property response if any.
 *
 * @return a Vector of ActiveLock objects containing information about locks
 * on the resource. The Vector will be empty if the lockdiscovery property
 * was not requested.
 * @exception com.ibm.webdav.WebDAVException
 */
public Vector getActiveLocks() throws WebDAVException {
	Vector allLocks = new Vector();
	PropertyValue prop = getProp("DAV:lockdiscovery");
	if (prop != null) {
		NodeList activeLocks = ((Element) prop.value).getElementsByTagNameNS("DAV:","activelock");
		Element activeLock = null;
		for (int i = 0; i < activeLocks.getLength(); i++) {
			activeLock = (Element) activeLocks.item(i);
			allLocks.addElement(new ActiveLock(activeLock));
		}
	}
	return allLocks;
}
/** The author of this resource. That is, the principal id of the user agent that
 * initially created the resource.
 * @return the DAV:author property or null if the property was not requested.
 */
public String getAuthor() {
	String author = null;
	PropertyValue prop = getProp("DAV:author");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			author = pcdata.getData();
		}
	}
	return author;
}
/** The date the resource revision was checked in.
 * @return the DAV:checkin-date property or null if the property was not requested
 * or the resource is not versioned or checked in.
 */
public Date getCheckinDate() {
	Date date = null;
	PropertyValue prop = getProp("DAV:checkin-date");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			String dateString = pcdata.getData();
			try {
				date = (new SimpleISO8601DateFormat()).parse(dateString);
			} catch (ParseException exc) {
				System.err.println("Invalid date format for creationdate in " + getResource());
			}
		}
	}
	return date;
}
/** The comment associated this resource.
 * @return the DAV:comment property or null if the property was not requested.
 */
public String getComment() {
	String comment = null;
	PropertyValue prop = getProp("DAV:comment");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			comment = pcdata.getData();
		}
	}
	return comment;
}
/** The language the content is written in.
 * @return the value of the DAV:getcontentlanguage property of null if the property was not requested
 */
public String getContentLanguage() {
	String contentLanguage = null;
	PropertyValue prop = getProp("DAV:getcontentlanguage");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			contentLanguage = pcdata.getData();
		}
	}
	return contentLanguage;
}
/** The length of the content of the resource or -1 if content length is not applicable.
 * @return the DAV:getcontentlength property or -1 if the property was not requested.
 */
public int getContentLength() {
	int length = -1;
	PropertyValue prop = getProp("DAV:getcontentlength");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			try {
				length = Integer.parseInt(pcdata.getData());
			} catch (NumberFormatException exc) {
				System.err.println("Bad contentlength property in " + getResource());
			}
		}
	}
	return length;
}
/** The MIME content type of the resource.
 * @return the DAV:getcontenttype property or null if the property was not requested.
 */
public String getContentType() {
	String contentType = null;
	PropertyValue prop = getProp("DAV:getcontenttype");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			contentType = pcdata.getData();
		}
	}
	return contentType;
}
/** The date the resource was created.
 * @return the DAV:creationdate property or null if the property was not requested.
 */
public Date getCreationDate() {
	Date date = null;
	PropertyValue prop = getProp("DAV:creationdate");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			String dateString = pcdata.getData();
			try {
				date = (new SimpleISO8601DateFormat()).parse(dateString);
			} catch (ParseException exc) {
				System.err.println("Invalid date format for creationdate in " + getResource());
			}
		}
	}
	return date;
}
/** A name for this resource suitable for display by client applications.
 * @return the DAV:displayname property or null if the property was not requested.
 */
public String getDisplayName() {
	String displayName = null;
	PropertyValue prop = getProp("DAV:displayname");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			displayName = pcdata.getData();
		}
	}
	return displayName;
}
/** The resource entity tag, useful for verifying the state of a cached resource.
 * @return The DAV:getetag property or null if the property was not requested.
 */
public String getETag() {
	String eTag = null;
	PropertyValue prop = getProp("DAV:getetag");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			eTag = pcdata.getData();
		}
	}
	return eTag;
}
/** The date on which the resource was last modified.
 * @return the DAV:getlastmodified property or null if the property was not requested.
 */
public Date getLastModifiedDate() {
	Date date = null;
	PropertyValue prop = getProp("DAV:getlastmodified");
	if (prop != null) {
		Text pcdata = (Text) prop.value.getFirstChild();
		if (pcdata != null) {
			String dateString = pcdata.getData();
			try {
				date = (new SimpleRFC1123DateFormat()).parse(dateString);
			} catch (ParseException exc) {
				System.err.println("Invalid date format for getlastmodified in " + getResource());
			}
		}
	}
	return date;
}
/** Internal helper method that returns the value
 *    of a property given its name.
 * <p>
 * If an invalid property name string (see
 *  PropertyName(String) constructor for definition)
 *  is provided, this routine will throw a runtime
 *  exception. This can be convenient if you know
 *  the property name string is valid and dont want
 *  to clean up exceptions that will never be thrown.
 *  (Runtime exceptions don't need to be declared.)
 *  If you need to find the value of a property and
 *  have a property name that you can't be sure is
 *  valid, use getProperty instead of this method.
 *
 * @return a PropertyValue for a given property name... or
 *    null if that property was not found.
 *
 * @exception RuntimeException
 */
private PropertyValue getProp( String pnstr ) {

	PropertyName pn;
	try {
		pn = new PropertyName( pnstr );
	} catch (InvalidPropertyNameException excc) {
		throw new RuntimeException( "internal error: bad property: "+pnstr );
	}
	PropertyValue prop = getProperty( pn );
	return prop;
}
/** Get all the properties and their statuses in this Response.
 *
 * @return a Dictionary whose keys are PropertyNames, and whose values are
 * PropertyValues.
 */
public Dictionary getPropertiesByPropName() {
	return pnproperties;
}

public Dictionary getPropertyDefinitionsByPropName() {
  return this.propertydefs;
}
/** Get the value of a property contained in a PropertyResponse. The
 * values were set when a method was invoked that returned a MultiStatus
 * containing one or more PropertyResponses.
 *
 * @param name the name of the property to get,
 * @return the property value for the named property
 */
public PropertyValue getProperty( PropertyName name) {
	return (PropertyValue) pnproperties.get(name);
}
/** Get the names of the properties contained in a PropertyResponse. The
 * values were set when a method was invoked that returned a MultiStatus
 * containing one or more PropertyResponses.  The values here are instances
 * of the class PropertyName... not String.
 */
public Enumeration getPropertyNamesPN() {
	return pnproperties.keys();
}
/** Get the resource type for the resource associated with this PropertyResponse.
 * This is the tagName of the first child of the DAV:resourcetype property.
 * @return The DAV:resourcetype DAV property or null if the property was not requested.
 */
public String getResourceType() {
	String resourceType = null;
	PropertyValue prop = getProp("DAV:resourcetype");
	if (prop != null) {
		Element type = (Element) prop.value.getFirstChild();
		if (type != null) {
			resourceType = type.getTagName();
		}
	}
	return resourceType;
}
/** Check to see if this response does not contain an error.
 *
 * @return true if all property statuses are less than 300.
 */
public boolean isOK() {
	boolean isOk = true;
	Enumeration propertyValues = getPropertiesByPropName().elements();
	while (isOk && propertyValues.hasMoreElements()) {
		PropertyValue propertyValue = (PropertyValue) propertyValues.nextElement();
		isOk = isOk && propertyValue.status < 300;
	}
	return isOk;
}
/** See if this property response is on a collection. That is, see
 * if it contains a resourcetype property having a collection element.
 *
 * @return true if this PropertyResponse is on a collection resource
 */
public boolean isOnACollection() {
	PropertyValue resourcetype = getProp("DAV:resourcetype");
	boolean isACollection = false;
	if (resourcetype != null) {
		Element value = (Element) resourcetype.value;
		isACollection = value.getElementsByTagNameNS("DAV:","collection").getLength()>0;
	}
	return isACollection;
}
/** Remove a property from the collection of properties generated
 * as a result of sending a method to a resource.
 *
 * @param propertyName the property to remove.
 * @exception com.ibm.webdav.ServerException
 */
public void removeProperty( PropertyName propertyName) throws ServerException {
	if (pnproperties.contains(propertyName)) {
		strxproperties.remove( propertyName.asExpandedString() );
		pnproperties.remove( propertyName );
	}
}
/** Set the value of a property.
 *
 * @param name the name of the property to set.
 * @param value the property value for the named property
 */
public void setProperty( PropertyName name, PropertyValue value) {
	strxproperties.put( name.asExpandedString(), name);
	pnproperties.put(name, value);
}
/** Convert this Response to a PropertyResponse.
 * This method is used to convert MethodResponses to PropertyResponses
 * when an error occurred accessing the properties of some member.
 *
 */
public PropertyResponse toPropertyResponse() {
	return this;
}
}
