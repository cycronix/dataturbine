package com.ibm.webdav.impl;

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
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.*;


/** CachedPropertiesManager implements the Properties interface by loading
 * all properties on an initial request, and then accessing from the
 * cache to satisfy more specific requests. This implementation is
 * appropriate in those situations where reading all the properties
 * is just as efficient as reading one. CachedProperties is abstract
 * because it relies on specific subclasses to support the actual
 * reading and writing of the properties.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public abstract class CachedPropertiesManager implements PropertiesManager {
    private static final int set = 0;
    private static final int remove = 1;
    protected ResourceImpl resource = null;

    /** The default constructor.
    */
    public CachedPropertiesManager() {
    }

    /** Create a properties manager for the given resource and its
    * namespace manager.
    * @param resource the resource whose properties are to be managed
    * @param namespaceManager its namespace manager
    */
    public CachedPropertiesManager(ResourceImpl resource,
                                   com.ibm.webdav.impl.NamespaceManager namespaceManager) {
        initialize(resource, namespaceManager);
    }

    /** Delete all properties for the managed resource
    * @exception IOException
    * @exception com.ibm.webdav.WebDAVException
    */
    public abstract void deleteProperties() throws WebDAVException;

    /** Get all the properties of this resource.
    * This implementation stores properties in an XML document containing a properties
    * root element. The properties file name is derived from the URI by adding the extension
    * PropertiesManager.propertiesSuffix. This applies to collections as well as other resources.
    * <p>
    * Since the properties are stored in a file, and all methods that query and
    * update the properties must read the XML file into memory and parse it,
    * many of the other property methods are implemented by calling this method.
    * Subclasses of ResourceImpl may want to use other techniques depending on
    * how the properties are stored.
    * </p>
    * @return a MultiStatus containing response elements with prop elements
    * containing the properties and their statuses.
    * @exception com.ibm.webdav.WebDAVException
    */
    public MultiStatus getProperties()
                              throws WebDAVException {
        Document propertiesDocument = resource.loadProperties();

        // create a MultiStatus to hold the results
        MultiStatus results = new MultiStatus();

        // create a response element to hold the properties for this resource
        PropertyResponse response = null;
        String urlstring;

        if (false) {
            // I consider this to be the more correct way and the
            //    way used in the examples in the spec... but it is
            //    redundant and creates the possibility of the two
            //    redundant parts being out of synch.
            urlstring = resource.getURL().toString();
        } else {
            // this is the way that mod_dav and a few others do it. This
            //    way also makes it easier to debug clients even if
            //    redirecting through a dedicated proxy.  Without this
            //    it's inconvenient to debug IE5.  It gets confused if
            //    the host:port (if provided) don't match who it thinks
            //    it's connecting to.
            urlstring = resource.getURL().getFile();
        }

        response = new PropertyResponse(urlstring);

        // add the properties to the response
        NodeList properties = propertiesDocument.getDocumentElement()
                                                .getChildNodes();
        Node temp = null;

        for (int i = 0; i < properties.getLength(); i++) {
            temp = properties.item(i);

            // Skip ignorable TXText elements
            if (!(temp.getNodeType() == Node.ELEMENT_NODE)) {
                continue;
            }

            Element property = (Element) temp;
            PropertyName pn = new PropertyName(property);
            response.addProperty(pn, property, WebDAVStatus.SC_OK);
        }

        results.addResponse(response);

        return results;
    }

    /** Get the named properties for this resource and (potentially) its children.
    *
    * @param names an arrary of property names to retrieve
    * @return a MultiStatus of PropertyResponses
    * @exception com.ibm.webdav.WebDAVException
    */
    public MultiStatus getProperties(PropertyName[] names)
                              throws WebDAVException {
        MultiStatus multiStatus = resource.getProperties(resource.getContext());
        MultiStatus newMultiStatus = new MultiStatus();

        Enumeration responses = multiStatus.getResponses();

        while (responses.hasMoreElements()) {
            PropertyResponse response = (PropertyResponse) responses.nextElement();
            PropertyResponse newResponse = new PropertyResponse(
                                                   response.getResource());
            newResponse.setDescription(response.getDescription());
            newMultiStatus.addResponse(newResponse);

            Hashtable properties = (Hashtable) response.getPropertiesByPropName();

            //Hashtable newProperties = (Hashtable) newResponse.getProperties();
            for (int i = 0; i < names.length; i++) {
                if (properties.containsKey(names[i])) {
                    PropertyValue srcval = response.getProperty(names[i]);
                    newResponse.setProperty(names[i], srcval);

                    //newProperties.put(names[i], properties.get(names[i]));
                } else {
                    Document factory = null;

                    try {
                        factory = DocumentBuilderFactory.newInstance()
                                                        .newDocumentBuilder()
                                                        .newDocument();
                    } catch (Exception e) {
                        throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
                                                  e.getMessage());
                    }

                    // we'll create an xml element with no value because that's
                    //    what webdav will need to return for most methods... even if the
                    //    property doesn't exist.   That's because the
                    //    distinction between a propertyname and propertyvalue
                    //    is fuzzy in WebDAV xml. A property name is
                    //    essentially an empty property value because
                    //    all property values have their property
                    //    name stuck on.
                    // if we decide to set reviewStatus to null instead (as
                    //    we did previously, then the code for MultiStatus.asXML()
                    //    needs to be updated to expect null values.
                    // (jlc 990520)
                    Element elTm = factory.createElementNS("X",
                                                           "X:" +
                                                           names[i].getLocal());

                    elTm.setAttribute("xmlns:X", names[i].getNamespace());

                    newResponse.addProperty(names[i], elTm,
                                            WebDAVStatus.SC_NOT_FOUND);
                }
            }
        }

        return newMultiStatus;
    }

    /** Get the names of all properties for this resource. This implementation
    * reads all the properties and then extracts their names.
    *
    * @return a MultiStatus of PropertyResponses
    * (PropertyValue.value is always null, PropertyValue.status contains the status)
    * @exception com.ibm.webdav.WebDAVException
    */
    public MultiStatus getPropertyNames() throws WebDAVException {
        MultiStatus multiStatus = resource.getProperties(resource.getContext());
        Enumeration responses = multiStatus.getResponses();

        // we have the result, but all of the properties in our structure contain
        //    values.  We don't want to include values.  Just names.  The following
        //    code strips out the content of these elements.
        while (responses.hasMoreElements()) {
            PropertyResponse response = (PropertyResponse) responses.nextElement();
            Dictionary properties = response.getPropertiesByPropName();
            Enumeration keys = properties.keys();

            while (keys.hasMoreElements()) {
                PropertyName key = (PropertyName) keys.nextElement();
                Element value = (Element) response.getProperty(key).getValue();
                response.setProperty(key,
                                     new PropertyValue(
                                             (Element) value.cloneNode(false),
                                             WebDAVStatus.SC_OK));
            }
        }

        return multiStatus;
    }

    /** Initialize this properties manager.
    * @param resource the resource whose properties are to be managed
    * @param namespaceManager its namespace manager
    */
    public void initialize(ResourceImpl resource,
                           com.ibm.webdav.impl.NamespaceManager namespaceManager) {
        this.resource = resource;
    }

    /** Is a property live, i.e., has semantics supported by the server?
    * @param propertyName the name of the property to check
    * @return true if the named property is live on this server
    */
    public boolean isLive(String propertyName) {
		return propertyName.equals("creationdate") ||
				   propertyName.equals("getcontentlength") ||
				   propertyName.equals("getlastmodified") ||
				   propertyName.equals("lockdiscovery") ||
				   propertyName.equals("getcontenttype") ||
				   propertyName.equals("resourcetype") ||
				   propertyName.equals("supportedlock") ||
        		 propertyName.equals("DAV:creationdate") ||
               propertyName.equals("DAV:getcontentlength") ||
               propertyName.equals("DAV:getlastmodified") ||
               propertyName.equals("DAV:lockdiscovery") ||
               propertyName.equals("DAV:getcontenttype") ||
               propertyName.equals("DAV:resourcetype") ||
               propertyName.equals("DAV:supportedlock");
    }

    /** Load properties from their persistent store.
    * @return an XML document containing a properties element.
    * @exception com.ibm.webdav.WebDAVException
    */
    public abstract Document loadProperties() throws WebDAVException;


    /** Remove the live DAV properties from the properties document that
    * do not need to be saved. There is no reason to save them as long
    * as they are recalculated each time the properties are loaded. This
    * method removes the ones that are repository specific.
    * @param propertiesDocument the XML document containing the properties. Elements
    *    are removed from this document that don't need to be saved.
    */
    public abstract void removeLiveProperties(Document propertiesDocument);

    /** Save the properties to the persistent store.
    * @param propertiesDocument an XML document containing a properties element.
    * @exception com.ibm.webdav.WebDAVException
    */
    public abstract void saveProperties(Document propertiesDocument)
                                 throws WebDAVException;

    /** Edit the properties of a resource. The updates must refer to a Document containing a WebDAV
    * propertyupdates element as the document root.
    *
    * @param updates an XML Document containing propertyupdate elements
    * @return the result of making the updates
    * describing the edits to be made.
    * @exception com.ibm.webdav.WebDAVException
    */
    public MultiStatus setProperties(Document propertyUpdates)
                              throws WebDAVException {
        // create a MultiStatus to hold the results. It will hold a MethodResponse
        // for each update, and one for the method as a whole
        MultiStatus multiStatus = new MultiStatus();
        boolean errorsOccurred = false;

        // first, load the properties so they can be edited
        Document propertiesDocument = resource.loadProperties();
        Element properties = (Element) propertiesDocument.getDocumentElement();

        // be sure the updates have at least one update
        Element propertyupdate = (Element) propertyUpdates.getDocumentElement();
        String tagName = propertyupdate.getNamespaceURI() +
                         propertyupdate.getLocalName();

        if (!tagName.equals("DAV:propertyupdate")) {
            throw new WebDAVException(WebDAVStatus.SC_UNPROCESSABLE_ENTITY,
                                      "missing propertyupdate element");
        }

        NodeList updates = propertyupdate.getChildNodes();

        if (updates.getLength() == 0) {
            throw new WebDAVException(WebDAVStatus.SC_UNPROCESSABLE_ENTITY,
                                      "no updates in request");
        }

        Vector propsGood = new Vector(); // a list of properties that

        // were patched correctly or would have been if another
        // property hadn't gone bad.
        // apply the updates
        Node temp = null;

        for (int i = 0; i < updates.getLength(); i++) {
            temp = updates.item(i);

            // skip any ignorable TXText elements
            if (!(temp.getNodeType() == Node.ELEMENT_NODE)) {
                continue;
            }

            Element update = (Element) temp;
            int updateCommand = -1;
            tagName = update.getNamespaceURI() + update.getLocalName();

            if (tagName.equals("DAV:set")) {
                updateCommand = set;
            } else if (tagName.equals("DAV:remove")) {
                updateCommand = remove;
            } else {
                throw new WebDAVException(WebDAVStatus.SC_UNPROCESSABLE_ENTITY,
                                          update.getTagName() +
                                          " is not a valid property update request");
            }

            // iterate through the props in the set or remove element and update the
            // properties as directed
            Element prop = (Element) update.getElementsByTagNameNS("DAV:",
                                                                   "prop")
                                           .item(0);

            if (prop == null) {
                throw new WebDAVException(WebDAVStatus.SC_UNPROCESSABLE_ENTITY,
                                          "no propeprties in update request");
            }

            NodeList propsToUpdate = prop.getChildNodes();

            for (int j = 0; j < propsToUpdate.getLength(); j++) {
                temp = propsToUpdate.item(j);

                // skip any TXText elements??
                if (!(temp.getNodeType() == Node.ELEMENT_NODE)) {
                    continue;
                }

                Element propToUpdate = (Element) temp;

                // find the property in the properties element
                Element property = null;
                PropertyName propertyName = new PropertyName(propToUpdate);

                if (((Element) propToUpdate).getNamespaceURI() != null) {
                    property = (Element) properties.getElementsByTagNameNS(
                                                 propToUpdate.getNamespaceURI(),
                                                 propToUpdate.getLocalName())
                                                   .item(0);
                } else {
                    property = (Element) properties.getElementsByTagName(
                                                 propToUpdate.getTagName())
                                                   .item(0);
                }

                boolean liveone = isLive(propertyName.asExpandedString());

                if (liveone) {
                    errorsOccurred = true;

                    PropertyResponse response = new PropertyResponse(
                                                        resource.getURL()
                                                                .toString());
                    response.addProperty(propertyName, propToUpdate,
                                         WebDAVStatus.SC_FORBIDDEN);
                    multiStatus.addResponse(response);
                }

                // do the update
                if (updateCommand == set) {
                    if (property != null) {
                        try {
                            properties.removeChild(property);
                        } catch (DOMException exc) {
                        }
                    }

                    if (!liveone) {
                        // I don't think we're allowed to update live properties
                        //    here.  Doing so effects the cache.  A case in
                        //    point is the lockdiscoveryproperty.  properties
                        //    is actually the properites cache "document" of this
                        //    resource.  Even though we don't "save" the request
                        //    if it includes live properties, we don't remove
                        //    it from the cache after we'd set it here, so it
                        //    can affect other queries. (jlc 991002)
                        properties.appendChild(propertiesDocument.importNode(
                                                       propToUpdate, true));

                        propsGood.addElement(propToUpdate);
                    }
                } else if (updateCommand == remove) {
                    try {
                        if (property != null) {
                            properties.removeChild(property);
                            propsGood.addElement(propToUpdate);
                        }
                    } catch (DOMException exc) {
                    }
                }
            }
        }

        {
            Enumeration els = propsGood.elements();

            for (; els.hasMoreElements();) {
                Object ob1 = els.nextElement();
                Element elProp = (Element) ob1;
                PropertyName pn = new PropertyName(elProp);
                PropertyResponse response = new PropertyResponse(
                                                    resource.getURL()
                                                            .toString());
                response.addProperty(pn, (Element) elProp.cloneNode(false),
                                     (errorsOccurred
                                      ? WebDAVStatus.SC_FAILED_DEPENDENCY
                                      : WebDAVStatus.SC_OK));


                // todo: add code for responsedescription
                multiStatus.addResponse(response);
            }
        }

        // write out the properties
        if (!errorsOccurred) {
            resource.saveProperties(propertiesDocument);
        } 

        return multiStatus;
    }

    /** Set a property of a resource to a value.
    *
    * @param name the property name
    * @param value the property value
    * @exception com.ibm.webdav.WebDAVException
    */
    public void setProperty(String name, Element value)
                     throws WebDAVException {
        // load the properties
        Document propertiesDocument = resource.loadProperties();
        Element properties = propertiesDocument.getDocumentElement();
        String ns = value.getNamespaceURI();
        
        Element property = null;
        if(ns == null) {
			property = (Element) ((Element) properties).getElementsByTagName(
														 value.getTagName()).item(0);
        } else {
        	property = (Element)properties.getElementsByTagNameNS(ns, value.getLocalName()).item(0);
        }
        

        if (property != null) {
            try {
                properties.removeChild(property);
            } catch (DOMException exc) {
            }
        }

        properties.appendChild(propertiesDocument.importNode(value, true));


        // write out the properties
        resource.saveProperties(propertiesDocument);
    }

    /** Update the live properties that are unique to the
    * repository implementation.
    * @param document the XML document containing the properties
    * @exception com.ibm.webdav.WebDAVException
    */
    public abstract void updateLiveProperties(Document document)
                                       throws WebDAVException;
}