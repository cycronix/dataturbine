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
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import com.ibm.webdav.impl.*;

/** A Resource implements a client proxy of the Resource interface locally where
 * possible, and by delegating the methods that must be implemented by the server
 * to another proxy stub as specified by the protocol given
 * in the resource URL. This allows WebDAV client applications to communicate
 * with a server through multiple RPC protocols, including no protocol at all for
 * local access.
 * @see com.ibm.webdav.CollectionP
 * @see com.ibm.webdav.Precondition#addStateTokenCondition
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class Resource implements Serializable {

   /** The version of XML used by WebDAV
	*/
   public static String XMLVersion = "1.0";
   public static String DAV4JVersion = "2.0.10";

	/** The default charset encoding for XML documents and for storing
	 * text/* MIME types on the server
	 */
	public static String defaultXMLEncoding = "UTF-8";
	public static String defaultCharEncoding = "UTF-8";

	static {
		try {
			// (for now, always use UTF-8, explore using the platform default later)
			/*OutputStreamWriter temp = new OutputStreamWriter(System.out);
			 String charEncoding = temp.getEncoding();
			 if (charEncoding.startsWith("ISO")) {
			 charEncoding = charEncoding.substring(3);
			 }
			 String XMLEncoding = MIME2Java.reverse(charEncoding);
			 if (XMLEncoding != null) {
			 defaultCharEncoding = charEncoding;
			 defaultXMLEncoding = XMLEncoding;
			 } else {
			 System.err.println("Java encoding "+charEncoding+" is not supported");
			 }*/
			URL.setURLStreamHandlerFactory(new com.ibm.webdav.protocol.URLStreamHandlerFactory());
		} catch (Error exc) {
		}
	}

	//------------------------------------------------------------------------------------

	protected URL url = null; // the resource URL, key, or identifier
	protected TargetSelector targetSelector;  // identifier for revision selector

	// contexts for communicating HTTP and WebDAV headers (method contol couples)
	protected ResourceContext context = new ResourceContext();
	protected IRResource impl = null; // the implementation to delegate to

	// cache the contents to avoid unnecessary trips to the server
	protected byte[] cachedContents = null;
/** The default constructor. Should be rarely used.
 */
public Resource() {
	this.url = null;
}
/** A copy constructor. This copies by reference so the resources share the
 * same URL and contexts. TODO: probably should clone.
 * @param resource the resource to copy
 * @exception com.ibm.webdav.WebDAVException
 */
public Resource(Resource resource) throws WebDAVException {
	this.url = resource.url;
	this.context = resource.context;
	this.targetSelector = resource.targetSelector;
	this.impl = ResourceFactory.createResource(url, null);
}
/** Construct a Resource with the given URL. This is the constructor most clients
 * will use to construct and access Resources using WebDAV. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(String url) throws WebDAVException {
   try {
	  initialize(new URL(url), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Resource with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @param targetSelector the revision target selector for this Collection
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(String url, TargetSelector targetSelector) throws WebDAVException {
   try {
	  initialize(new URL(url), targetSelector);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Create a Resource from the given URL components.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param port the TCP port to use. HTTP uses 80 by default.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(String protocol, String host, int port, String file) throws WebDAVException {
   try {
	  initialize(new URL(protocol, host, port, file), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Create a Resource from the given URL components. This constructor uses the default
 * HTTP port.
 * @param protocol the protocol to use, http:, rmi:, or iiop:
 * @param host the name or IP addres of the server host. Using the client host name,
 *    or 'localhost' without a port uses local access with no RPC or server required.
 * @param file the resource URL relative to the server including any query string, etc.
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(String protocol, String host, String file) throws WebDAVException {
   try {
	  initialize(new URL(protocol, host, file), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Resource with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(URL url) throws WebDAVException {
   try {
	  initialize(url, null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Resource with the given URL. The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param url the URL of the resource.
 * @param targetSelector the revision target selector for this Collection
 * @exception java.io.IOException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(URL url, TargetSelector targetSelector) throws WebDAVException {
   try {
	  initialize(url, targetSelector);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/** Construct a Resource with the given URL specification in the given context.  The resource having
 * the url may not exist as this constructor does not access the resource from
 * the server. Use exists() or attmept to get the contents of the resource to
 * see if it exists. Other constructors are provided using parameters for the
 * various parts of the URL. See java.net.URLConnection for details. A ResourceFactory
 * may also be used to construct instances of a Resource.
 *
 * @param context a URL giving the context in which the spec is evaluated
 * @param spec a URL whose missing parts are provided by the context
 * @exception com.ibm.webdav.WebDAVException
 * @see URLConnection
 * @see com.ibm.webdav.ResourceFactory
 */
public Resource(URL context, String spec) throws WebDAVException {
   try {
	  initialize(new URL(context, spec), null);
   } catch (java.io.IOException exc) {
	  throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Bad URL");
   }
}
/**
 * Add a label to this revision of a versioned resource. The
 * versioned resource must not already have the label on any
 * revision, and the label cannot be the same as any revision
 * id. The label must be removed from one revision before it
 * can be added to a different revision. The operation will
 * fail if the resource is not a versioned resource.
 * <p>
 * Labels are used to provide meaningful names that distinguish
 * revisions of versioned resources. They can be used in the revision
 * selection rule of the workspace to specify what revision should
 * be used in that workspace. A specific label may be used to override
 * the workspace to access revisions.
 * <p>
 * A revision does not need to be checked out to add a label.
 *
 * @param label the label to add to the labels used to identify
 * this revision
 * @exception com.ibm.webdav.WebDAVException
 */
public void addLabel(String label) throws WebDAVException {
}
/** Add properties to a resource.
 *
 * @param names an array of property names
 * @param value an array of property values
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus addProperties( PropertyName[] names, Element[] values) throws WebDAVException {
        // create a propertyupdate document to set the values
	Document document = null;
        try {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,"Parsing problem");
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element propertyUpdate = document.createElementNS("DAV:","D:propertyupdate");
	propertyUpdate.setAttribute("xmlns:D", "DAV:");
	document.appendChild(propertyUpdate);
	Element set = document.createElementNS("DAV:","D:set");
	propertyUpdate.appendChild(set);
	Element prop = document.createElementNS("DAV:","D:prop");
	set.appendChild(prop);
	for (int i = 0; i < names.length; i++) {
		prop.appendChild((Element) values[i]);
	}
	return setProperties(document);
}
/** Add a property to a resource
 *
 * @param name the property name
 * @param value the property value
 * @exception com.ibm.webdav.WebDAVException
 */
public void addProperty( PropertyName name, Element value) throws WebDAVException {
	PropertyName[] names = new PropertyName[1];
	names[0] = name;
	Element[] values = new Element[1];
	values[0] = value;
	MultiStatus result = addProperties(names, values);

	// raise any necessary exceptions
	if (result.getResponses().hasMoreElements()) {
		Response response = (Response) result.getResponses().nextElement();
		if (response instanceof MethodResponse) {
			int status = ((MethodResponse) response).getStatus();
			if (status != WebDAVStatus.SC_OK) {
				throw new WebDAVException(status, WebDAVStatus.getStatusMessage(status));
			}
		} else {
			PropertyValue propertyValue = (PropertyValue) ((PropertyResponse) response).getPropertiesByPropName().elements().nextElement();
			if ((propertyValue.status != WebDAVStatus.SC_OK) && (propertyValue.status != WebDAVStatus.SC_FAILED_DEPENDENCY)) {
				throw new WebDAVException(propertyValue.status, WebDAVStatus.getStatusMessage(propertyValue.status));
			}
		}
	}
}
/**
 * Cancel the checkout of this working resource, delete the
 * working resource, and remove any predecessor/successor
 * relationships created by checkout or merge. An exception
 * is raised if the resource is not currently checked out.
 *
 * @exception com.ibm.webdav.WebDAVException
 */
public void cancelCheckOut() throws WebDAVException {
}
/**
 * Checkin a resource creating a new immutable revision and
 * releasing the revision so other user agents may subsequently
 * check it out.
 *
 * @exception com.ibm.webdav.WebDAVException
 */
public void checkin() throws WebDAVException {
}
/**
 * Checkin a resource releasing it so other user agents may check
 * it out. If overwrite is false, create a new revision and set
 * the predecessor and successor relationships. If overwrite is
 * true, the revision is updated in place and the previous contents
 * are lost. Effectively, no new revision is created, and the revision
 * id now refers to the updated revision. Overwrite will fail if the
 * revision being overwritten is not mutable.
 * <p>
 * If makeCurrentTarget is true, this revision becomes the default target
 * for the versioned resource. Otherwise the current target is unchanged.
 *
 * @param activity the activity associted with the changes made in this revision
 * @param makeCurrentTarget true means the new revision becomes the
 * target for the versioned resource. Otherwise the target is unchanged.
 * @param overwrite ture means overwrite the existing revision,
 * false means create a new revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public void checkin(Activity activity, boolean makeCurrentTarget, boolean overwrite) throws WebDAVException {
}
/**
 * Checkin a resource releasing it so other user agents may check
 * it out. If overwrite is false, create a new revision and set
 * the predecessor and successor relationships. If overwrite is
 * true, the revision is updated in place and the previous contents
 * are lost. Effectively, no new revision is created, and the revision
 * id now refers to the updated revision. Overwrite will fail if the
 * revision being overwritten is not mutable.
 * <p>
 * If makeCurrentTarget is true, this revision becomes the default target
 * for the versioned resource. Otherwise the current target is unchanged.
 *
 * @param makeCurrentTarget true means the new revision becomes the
 * target for the versioned resource. Otherwise the target is unchanged.
 * @param overwrite ture means overwrite the existing revision,
 * false means create a new revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public void checkin(boolean makeCurrentTarget, boolean overwrite) throws WebDAVException {
}
/**
 * Check out a resource in order to create a new working resource.
 * A resource is checked out in the context of the workspace, and
 * can only be checked out once in a given activity. The workspace
 * to use can be set in the request context. Checkout control
 * on versioned may be managed by locking the versioned resource or a
 * revision before checking out a revision.
 * <p>
 * CheckOut fails is the resource is not a versioned resource, is
 * currently checked out in the current activity, or the versioned
 * <p>
 * resource or revision is locked by another user. CheckOut also
 * fails if the current activity or workspace is locked. If the
 * versioned resource or revision is locked, the request context
 * must include a precondition containing the lock token.
 * <p>
 * If workspace is null, the server will return a workspace that
 * can be subsequently used to access the checked out working
 * resource.
 *
 * @return the TargetSelector for this working resource
 * @exception com.ibm.webdav.WebDAVException
 */
public TargetSelector checkOut() throws WebDAVException {
	return checkOut(null);
}
/**
 * Check out a resource in order to create a new working resource.
 * A resource is checked out in the context of the workspace, and
 * can only be checked out once in a given activity. The workspace
 * to use can be set in the request context. Checkout control
 * on versioned may be managed by locking the versioned resource or a
 * revision before checking out a revision.
 * <p>
 * CheckOut fails is the resource is not a versioned resource, is
 * currently checked out in the current activity, or the versioned
 * <p>
 * resource or revision is locked by another user. CheckOut also
 * fails if the current activity or workspace is locked. If the
 * versioned resource or revision is locked, the request context
 * must include a precondition containing the lock token.
 * <p>
 * If workspace is null, the server will return a workspace that
 * can be subsequently used to access the checked out working
 * resource.
 *
 * @param workspace the Workspace in which the revision is checked out.
 * @return the TargetSelector for this working resource
 * @exception com.ibm.webdav.WebDAVException
 */
public TargetSelector checkOut(Workspace workspace) throws WebDAVException {
	return null;
}
/**
 * Create a new revision of this resource, but keep it checked
 * out. If overwrite is false, create a new revision and set
 * the predecessor and successor relationships. If overwrite is
 * true, the revision is updated in place and the previous contents
 * are lost. Effectively, no new revision is created, and the revision
 * id now refers to the updated revision. Overwrite will fail if the
 * revision being overwritten is not mutable.
 * <p>
 * If makeCurrentTarget is true, this revision becomes the default target
 * for the versioned resource. Otherwise the current target is unchanged.
 *
 * @param makeCurrentTarget true means the new revision becomes the
 * target for the versioned resource. Otherwise the target is unchanged.
 * @param overwrite ture means overwrite the existing revision,
 * false means create a new revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public void checkPoint(boolean makeCurrentTarget, boolean overwrite) throws WebDAVException {
}
/** This method must be called after the client has completed writing to the contents
 * output stream that was obtained from <code>getContentsOutputStream()</code>.
 * @exception com.ibm.webdav.WebDAVException
 */
public void closeContentsOutputStream() throws WebDAVException {
	this.closeContentsOutputStream(null);
}
/** Copy this resource to the destination URL. The destination resource must not already exist.
 * Partial results are possible, check the returned status for details.
 *
 * @param destinationURL the destination
 *
 * @return the status of the copy operation for each resource copied
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus copy(String destinationURL) throws WebDAVException {
	return copy(destinationURL, true, null);
}
/** Copy this resource to the destination URL.
 * Partial results are possible, check the returned status for details.
 *
 * @param destinationURL the destination
 * @param overwrite true implies overrite the destination if it exists
 * @param propertiesToCopy a collection of properties that must be copied or
 * the method will fail. propertiesToCopy may have one of the following values:
 * <ul>
 *    <li>null - ignore properties that cannot be copied</li>
 *    <li>empty collection - all properties must be copied or the method will fail</li>
 *    <li>a collection of URIs - a list of the properties that must be copied
 *        or the method will fail</li>
 * </ul>
 *
 * @return the status of the copy operation for each resource copied
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus copy(String destinationURL, boolean overwrite, Vector propertiesToCopy) throws WebDAVException {
	flushCaches();
	return impl.copy(context, destinationURL, overwrite, propertiesToCopy);
}
/** Delete this resouce from the server. The actual effect of the delete operation is
 * determined by the underlying repository manager. The visible effect to WebDAV
 * is that the resource is no longer available.
 *
 * @return a MultiStatus containing the status of the delete method on each
 *         effected resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus delete() throws WebDAVException {
	flushCaches();
	return impl.delete(context);
}
/**
 * Return an XML document describing the differences between two
 * revisions, both contents and properties.
 *
 * @return an XML document describing the differences between
 * the given resource and this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public Document differencesWith(Resource resource) throws WebDAVException {
	return null;
}
/** Two Resources are equal if they have the same URL. In this case, port
 * number -1 and port number 80 are considered the same port for equality
 * purposes.
 * @return true if the resources have URLs indicating the same server resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean equals(Resource resource) throws WebDAVException {
	URL resourceURL = resource.getURL();

	int thisPort = url.getPort() == -1 ? 80 : url.getPort();
	int resourcePort = resourceURL.getPort() == -1 ? 80 : resourceURL.getPort();
	return url.getProtocol().equals(resourceURL.getProtocol()) && thisPort == resourcePort && url.getFile().equals(resourceURL.getFile());
}
/** See if the contents of this resource exists. A resource exists
 * if it has contents or state maintained by a server.
 *
 * @return true if the contents exists, false otherwise
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean exists() throws WebDAVException {
	boolean exists = true;
	InputStream is = null;
	try {
		is = getContentsInputStream();
	} catch (WebDAVException exc) {
                if (exc.getStatusCode() == WebDAVStatus.SC_NOT_FOUND) {
			exists = false;
		}
	}

        return exists;
}
/** Flush any caches so that subsequent methods obtain fresh data from the server.
 * @exception com.ibm.webdav.WebDAVException
 */
public void flushCaches() throws WebDAVException {
	cachedContents = null;
}
/** Get the active lock on this resource owned by the given principal if any.
 * NOTE: this method cannot be reliably implemented based on version 10 of
 * the WebDAV spec as an activelock element in a lockdiscovery does not contain
 * the authorization credentials of the owner of the lock. For now, this method
 * relies on an additional principal element in the activelock that contains
 * the required id. This is an IBM EXTENSTION. When WebDAV ACLs are introduced,
 * the principal will likely be added to the activelock element.
 *
 * @param principal the authorization id of the requesting principal
 *
 * @return the active lock owned by that principal or null if the resource is
 * not locked by that principal.
 * @exception com.ibm.webdav.WebDAVException
 */
public ActiveLock getActiveLockFor(String principal) throws WebDAVException {
	Enumeration locks = getLocks().elements();
	ActiveLock ownedLock = null;
	while (ownedLock == null && locks.hasMoreElements()) {
		ActiveLock lock = (ActiveLock) locks.nextElement();
		if (lock.getPrincipal().equals(principal)) {
			ownedLock = lock;
		}
	}
	return ownedLock;
}
/**
 * Get the activity this revision was created in. Returns null
 * for un-versioned resources or revisions that weren't
 * updated in an activity.
 *
 * @return the Activity used to create this revision if any
 * @exception com.ibm.webdav.WebDAVException
 */
public Activity getActivity() throws WebDAVException {
	return null;
}
/** Get the contents of this resource. This method does not decode text contents. The
 * caller should convert the result to a String using a character set based on the
 * contentType.
 *
 * @return the contents as a byte array
 * @exception com.ibm.webdav.WebDAVException
 */
public byte[] getContents() throws WebDAVException {
	try {
		if (cachedContents == null) {
			InputStream is = getContentsInputStream();
			int length = (int) getResponseContext().contentLength();
			if (length != -1) {
				int rcvd = 0;
				int size = 0;
				cachedContents = new byte[length];
				do {
					size += rcvd;
					rcvd = is.read(cachedContents, size, length - size);
				} while (size < length && rcvd != -1);
				if (rcvd == -1)

					// premature EOF
					cachedContents = resizeArray(cachedContents, size);
			} else {
				cachedContents = new byte[0];
				int inc = 8192;
				int off = cachedContents.length;
				int rcvd = 0;
				do {
					off += rcvd;
					cachedContents = resizeArray(cachedContents, off + inc);
					rcvd = is.read(cachedContents, off, inc);
				} while (rcvd != -1);
				cachedContents = resizeArray(cachedContents, off);
			}
			is.close();
		}
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	return cachedContents;
}
/** Get an InputStream for accessing the contents of this resource. This method may provide
 * more efficient access for resources that have large contents. Clients may want to create
 * a Reader to perform appropriate character conversions on this stream.
 *
 * @return an InputStream on the contents
 * @exception com.ibm.webdav.WebDAVException
 */
public InputStream getContentsInputStream() throws WebDAVException {
	InputStream is = null;
	if (cachedContents != null) {
		is = new ByteArrayInputStream(cachedContents);
	} else {
		is = impl.getContentsInputStream(context);
	}
        return is;
}
/** Get an OutputStream for setting the contents of this resource. This method may provide
 * more efficient access for resources that have large contents. Remember to call
 * closeContentsOutputStream() when all the data has been written.
 *
 * @return an OutputStream to set the contents
 * @exception com.ibm.webdav.WebDAVException
 */
public OutputStream getContentsOutputStream() throws WebDAVException {
	flushCaches();
	return impl.getContentsOutputStream(context);
}
/**
 * Return all the labels on this revision, not including its revision id.
 *
 * @return an Enumeration of revision labels that identify this revision
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getLabels() throws WebDAVException {
	return null;
}
/** Get the locks that exist on this resource.
 *
 * @return a Vector of ActiveLock objects
 * @exception com.ibm.webdav.WebDAVException
 */
public Vector getLocks() throws WebDAVException {
	PropertyValue p = getProperty( PropertyName.createPropertyNameQuietly("DAV:lockdiscovery") );
	Element lockdiscovery = null;
	if (p != null) {
		lockdiscovery = p.value;
	}
	Vector allLocks = new Vector();
	if (lockdiscovery != null) {
		NodeList activeLocks = ((Element) lockdiscovery).getElementsByTagNameNS("DAV:", "activelock");
		Element activeLock = null;
		for (int i = 0; i < activeLocks.getLength(); i++) {
			activeLock = (Element) activeLocks.item(i);
			allLocks.addElement(new ActiveLock(activeLock));
		}
	}
	return allLocks;
}
/**
 * Return a list of activities on different lines of descent
 * for this revision that are candidates for merging. Returns
 * null if the resource is not a versioned resource.
 *
 * @return an Enumeration of Activities that specify revisions
 * on different lines of descent.
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMergeCandidates() throws WebDAVException {
	return null;
}
/**
 * Get the predecessors of this revision that were established
 * by merging changes from another activity. The list may be empty.
 *
 * @return an Enumeration of Resources that are the merge
 * predecessors of this revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMergePredecessors() throws WebDAVException {
	return null;
}

/** This method can be used for obtaining meta-information about this resource without
 * actually reading the resource contents. This meta-information is maintained by the server
 * in addition to the resource properties.</p>
 * <p>
 * After this call, the resource context has been updated and
 * <code>getStatusCode()</code>, <code>getStatusMessage()</code>, and <code>getResponseContext()</code>
 * as well as all the ResourceContext methods return updated values based on the current
 * state of the resource.</p>
 * <p>This methods corresponds to the HTTP HEAD method.</p>
 *
 * @exception com.ibm.webdav.WebDAVException
 */
public void getMetaInformation() throws WebDAVException {
	if (cachedContents != null) {
		return; // already have them
	}
	impl.getMetaInformation(context);
}
/**
 * A resource may have a number of mutable properties. These are
 * properties that may change even when the resource is checked in.
 * Changes to these properties does not require a new revision.
 *
 * @return an Enumeration of the mutable properties of this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getMutableProperties() throws WebDAVException {
	return null;
}

/**
 * Get the options for this resource. Versioning options
 * are established by the server and include:
 * <ul>
 *   <li>Mutable/immutable revisions</li>
 *   <li>Supports multiple activities </li>
 *   <li>Is automatically versioned</li>
 * </ul>
 *
 * @return an XML Element containing the options for
 * this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public Element getOptions() throws WebDAVException {
	return null;
}

/** Get the collection containing this resource.
 *
 * @return the parent collection
 * @exception com.ibm.webdav.WebDAVException
 */
public Collection getParentCollection() throws WebDAVException {
	String parentURL = getURL().toString();

	int delimiterPosition = 0;
	if (parentURL.endsWith("/")) {
		delimiterPosition = parentURL.substring(0, parentURL.length() - 1).lastIndexOf("/");
	} else {
		delimiterPosition = parentURL.lastIndexOf("/");
	}
	parentURL = parentURL.substring(0, delimiterPosition + 1);
	Collection parent = null;
	try {
		parent = new Collection(parentURL);
	} catch (WebDAVException exc) {
		throw exc;
	} 
	return parent;
}
/**
 * Get the predecessor of this revision. That is, get the
 * revision from which this revision was checked out.
 * Returns null if the Resource has no predecessor.
 *
 * @return the predecessor of this revision or null if the revision
 * has no successor.
 * @exception com.ibm.webdav.WebDAVException
 */
public Resource getPredecessor() throws WebDAVException {
	return null;
}

/** Get all the properties of this resource.
 *
 * @return a MultiStatus of PropertyResponses. It should contain only one
 * response element.
 * @see com.ibm.webdav.MultiStatus
 * @see com.ibm.webdav.PropertyResponse
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus getProperties() throws WebDAVException {
	return impl.getProperties(context);
}
/** Get the named properties of this resource.
 *
 * @param names an arrary of property names to retrieve
 *
 * @return a MultiStatus of PropertyResponses
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.PropertyResponse
 */
public MultiStatus getProperties( PropertyName names[]) throws WebDAVException {
	return impl.getProperties(context, names);
}
/** Get the value of the given property for this resource.
 *
 * @param name the name of the property to retrieve
 *
 * @return PropertyValue or null if the resource does not have the requested property
 * @exception com.ibm.webdav.WebDAVException
 */
public PropertyValue getProperty( PropertyName name) throws WebDAVException {
	PropertyName[] names = new PropertyName[1];
	names[0] = name;
	Enumeration responses = getProperties(names).getResponses();
	PropertyResponse response = (PropertyResponse) responses.nextElement();
	Dictionary properties = response.getPropertiesByPropName();
	return (PropertyValue) properties.get(name);
}
/** Get the names of all properties for this resource. The result is similar to
 * getProperties(), but the properties have no values.
 *
 * @return a MultiStatus of PropertyResponses
 * (PropertyValue.value is always null, PropertyValue.status contains the status)
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.PropertyResponse
 */
public MultiStatus getPropertyNames() throws WebDAVException {
	return impl.getPropertyNames(context);
}
/** Get the request context for this resource. The context contains information
 * used by methods on a resource when the method is called.
 *
 * @return the ResourceContext providing information that controls
 * method execution.
 * @exception com.ibm.webdav.WebDAVException
 */
public HTTPHeaders getRequestContext() throws WebDAVException {
	return context.getRequestContext();
}
/** Get the response context for this resource. The context contains information
 * returned from invocations of methods on a resource.
 *
 * @return the ResourceContext providing information that
 * is returned by method execution.
 * @exception com.ibm.webdav.WebDAVException
 */
public HTTPHeaders getResponseContext() throws WebDAVException {
	return context.getResponseContext();
}
public ResourceContext getContext() {
  return context;
}

/**
 * Get the revision history for a versioned resource. The revision
 * history lists the revisions of a resource and their predecessors
 * and successors. The format of the document is given in section
 * Revision History. The document will not contain any revisions
 * if the resource is not versioned.
 *
 * @return an XML document containing the revision history of the
 * associated versioned resource.
 * @exception com.ibm.webdav.WebDAVException
 */
public Document getRevisionHistory() throws WebDAVException {
	return null;
}

/**
 * Get the system-assigned revision id for this revision. This
 * revision name cannot be changed, and cannot be reused if
 * this revision is deleted. Returns NULL if the resource is
 * not versioned.
 * <p>
 * The revision id must be unique for the revision across all
 * time. Servers may choose to use an opaque identifier consisting
 * of a time stamp similar to UUIDs for lock tokens.
 *
 * @return the revision id of this revision of a versioned resource
 * @exception com.ibm.webdav.WebDAVException
 */
public String getRevisionId() throws WebDAVException {
	return null;
}

/** Get the status code corresponding to the last method execution.
 *
 * @return the status code as defined by HTTP/1.1 and the WebDAV extensions.
 * @exception com.ibm.webdav.WebDAVException
 */
public int getStatusCode() throws WebDAVException {
	return context.getStatusCode().getStatusCode();
}
/** Get the status message corresponding to the last method execution.
 *
 * @return the status message as defined by HTTP/1.1 and the WebDAV extensions.
 * @exception com.ibm.webdav.WebDAVException
 */
public String getStatusMessage() throws WebDAVException {
	return context.getStatusCode().getStatusMessage();
}
/**
 * Get the immediate successors of this revision. That is, get the revisions
 * that were created by checking out this revision.
 * The list may be empty.
 *
 * @return an Enumeration of Resources that are
 * successors of this revision.
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getSuccessors() throws WebDAVException {
	return null;
}

/** Get the TargetSelector that identifies this resource revision.
 *
 * @return the TargetSelector for this revision
 * @exception com.ibm.webdav.WebDAVException
 */
public TargetSelector getTargetSelector() throws WebDAVException {
	return targetSelector;
}
/** Get the name that identifies this resource.
 *
 * @return the URL for this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public URL getURL() throws WebDAVException {
	return url;
}
/**
 * Get the system-assigned working resource id for this revision.
 * Returns NULL if the resource is not versioned or is not checked out.
 * <p>
 * The working resource id must be unique for all working resources
 * of this revision. Servers may choose to use an opaque identifier consisting
 * of a time stamp similar to UUIDs for lock tokens.
 *
 * @return the working resource id of this working resource of a revision of
 * a versioned resource
 * @exception com.ibm.webdav.WebDAVException
 */
public String getWorkingResourceId() throws WebDAVException {
	return null;
}

/**
 * Get the current working resources of this revision. Returns an
 * empty Enumeration if this revision has no current working resources.
 * Returns null if this resource is not a revision.
 *
 * @return An Enumeration of current working resources of this VersionedResource
 * @exception com.ibm.webdav.WebDAVException
 */
public Enumeration getWorkingResources() throws WebDAVException {
	return null;
}
/** Initialize this collection instance. Make sure the URL ends in a '/'.
*/
protected void initialize(URL url, TargetSelector targetSelector) throws WebDAVException {
	try {
		this.url = url;
		this.targetSelector = targetSelector;
		impl = ResourceFactory.createResource(url, targetSelector);
                
	} catch (Exception exc) {
		throw new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Malformed URL");
	}
}
/**
 * A resource can be automatically versioned on each method
 * that updates its state (content or properties). Non-versioning
 * aware clients use automatic versioning to support updates. If
 * a resource is not automatically versioned, attempts to update
 * the revision without explicitly checking it out first will fail.
 *
 * @return true if this resource is automatically versioned,
 * false if not.
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isAutomaticallyVersioned() throws WebDAVException {
	return false;
}

/**
 * Return true if this revision is checked out in the given activity.
 * The activity may be null to see if the revision was checked out
 * without using an activity.
 *
 * @param activity the Activity to check for
 * @return boolean return true if this revision is checked out in the
 * given activity
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isCheckedOut(Activity activity) throws WebDAVException {
	return false;
}
/** Returns true if this Resource is a collection. Returns false otherwise.
 *
 * @return true if this Resource is a collection.
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isCollection() throws WebDAVException {
	boolean isCollection = false;
	PropertyValue pv = getProperty(PropertyName.pnResourcetype);
	if (pv != null) {
		Element resourcetype = (Element) pv.value;
		if (resourcetype.getElementsByTagNameNS("DAV:", "collection") != null) {
			isCollection = true;
		}
	}
	return isCollection;
}
/**
 * Return true if any revision of this versioned resource is
 * labeled with the given label
 *
 * @param label the label to check
 * @return true if this revision is labeled with the given label
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isLabeledWith(String label) throws WebDAVException {
	return false;
}
/** See if this resource is locked.
 *
 * @return true if this resource is locked, false otherwise.
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isLocked() throws WebDAVException {
	// see if there are any active locks
	return !getLocks().isEmpty();
}
/** Is this resource locked by the current authorized user? That is, does the
 * current user have sufficient locking access to modify this resource. The
 * method, like all methods that do modify the resource, must have a precondition
 * set in the context containing the lock token of the resource owned by this
 * user. The user is set in the request context using the authorization method.
 * @return true if this resource is locked by the principal in the context
 *    sufficient to modify the resource.
 * @exception com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.ResourceContext#authorization
 */
public boolean isLockedByMe() throws WebDAVException {
	String principal = getRequestContext().getAuthorizationId();
	Precondition precondition = getRequestContext().precondition();
	if (precondition == null) {
		return false;  // it is not locked by me.
		//raise(new WebDAVException(WebDAVStatus.SC_BAD_REQUEST, "Missing If header containing lock token"));
	}

	// get the locks on this resource
	Enumeration locks = getLocks().elements();
	boolean isLockedByMe = false;
	// look for a matching lock
	while (locks.hasMoreElements()) {
		ActiveLock activeLock = (ActiveLock) locks.nextElement();
		Condition condition = new Condition(getURL().getFile());
		ConditionTerm term = new ConditionTerm();
		StateToken stateToken = new StateToken(activeLock.getLockToken());
		term.addConditionFactor(stateToken);
		condition.addConditionTerm(term);
		if (precondition.matches(condition) && activeLock.getPrincipal().equals(principal) && activeLock.getLockType().equals(ActiveLock.writeLock)) {
			isLockedByMe = true;
			break;
		}
	}
	return isLockedByMe;
}
/**
 * Return true if this revision is mutable. That is, it was checked
 * in as a mutable revision. Mutable revisions may be checked in
 * overwriting the contents of the revision with the contents of
 * the checked out working resource. This allows users to make
 * pdates that do not require a new revision.
 * <p>
 * An immutable revision can never be made mutable, but a new revision
 * can be. A mutable revision can be made immutable by checking it out
 * in place and checking it back is as immutable.
 *
 * @return an XML document describing the differences between
 * the given resource and this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public boolean isMutable() throws WebDAVException {
	return false;
}
/**
 * Return true if this resource is a versioned resource. A versioned
 * resource has multiple revisions and a revision history.
 *
 * @return true if this resource is a versioned resource, false otherwise
 * @exception com.ibm.webdav.WebDAVException
 */
boolean isVersioned() throws WebDAVException {
	return false;
}
/** Exclusively write Lock this resource for all time.
 *
 * @return detailed information about the lock status of this resource. A MultiStatus
 * containing lockdiscovery properties.
 * An ActiveLock may be constructed by accessing the lockdiscovery element(s) of the
 * returned MultiStatus in order to obtain information about the lock.
 *
 * @return a MultiStatus containing a lockdiscovery property indicating
 * the results of the lock operation.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus lock(Document document) throws WebDAVException {
	String sPrefix = "D";
        String userName = System.getProperties().getProperty("user.name");
	Element owner = document.createElementNS("DAV:","D:owner");
	owner.setAttribute("xmlns:D", "DAV:");
	owner.appendChild(document.createTextNode(userName));
	return lock(ActiveLock.exclusive, ActiveLock.writeLock, -1, owner);
}
/** Lock this resource based on the given parameters. This allows control of
 * the lock scope (exclusive or shared) the lock type (write), owner information, etc.
 *
 * @param scope the scope of the lock, exclusive or shared
 * @param type the type of the lock, currently only write
 * @param timeout the number of seconds before the lock times out or
 *     -1 for infinite timeout.
 * @param owner an XML element containing useful information that can be
 *     used to identify the owner of the lock. An href to a home page, an
 *     email address, phone number, etc. Can be null if no owner information
 *     is provided.
 *
 * @return a MultiStatus containing a lockdiscovery property indicating
 * the results of the lock operation.
 * @exception com.ibm.webdav.WebDAVException
*/
public MultiStatus lock(String scope, String type, int timeout, Element owner) throws WebDAVException {
	// remove any lock tokens in the context so we don't try to
	// do a refresh by mistake.
	getRequestContext().precondition((String) null);
	return impl.lock(context, scope, type, timeout, owner);
}
/** Move this resource to the destination URL.
 * The destination resource must not already exist.
 * Partial results are possible, check the returned status for details
 *
 * @param destinationURL the destination
 *
 * @return the status of the move operation for each resource moved
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus move(String destinationURL) throws WebDAVException {
	return move(destinationURL, true, null);
}
/** Move this resource to the destination URL.
 * Partial results are possible, check the returned status for details
 *
 * @param destinationURL the destination
 * @param overwrite true implies overrite the destination if it exists
 * @param propertiesToMove a collection of properties that must be moved or
 * the method will fail. propertiesToMove may have one of the following values:
 * <ul>
 *    <li>null - ignore properties that cannot be moved</li>
 *    <li>empty collection - all properties must be moved or the method will fail</li>
 *    <li>a collection of URIs - a list of the properties that must be moved
 *        or the method will fail</li>
 * </ul>
 *
 * @return the status of the move operation for each resource moved
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus move(String destinationURL, boolean overwrite, Vector propertiesToMove) throws WebDAVException {
	flushCaches();
	return impl.move(context, destinationURL, overwrite, propertiesToMove);
}
/** This method treats this resource as a method or service, and sends its parameter to
 * this resource where it is handled in a resource-specific way. For example,
 * sending data from an HTML form to a URL representing a Servlet or CGI script that processes
 * the form data to produce some result.
 *
 * @param args a string representing the arguments to the method represented by this URL. The
 * arguments are in the form ?parameterName1=value1&amp;parameterName2=value2... as specified
 * for URL queries.
 *
 * @return the results of sending the arguments to the URL
 * @exception com.ibm.webdav.WebDAVException
 */
public byte[] performWith(String args) throws WebDAVException {
	flushCaches(); // can't cache the results of a POST
	return impl.performWith(context, args);
}
/** Refresh the lock on this resource by resetting the lock timeout.
 * The context must contain the proper authorization for the requesting
 * principal.
 *
 * @param lockToken the lock token identifying the lock.
 * @param timeout the new timeout in seconds. -1 means infinite timeout.
 *
 * @return updated information about the lock status of this resource
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus refreshLock(String lockToken, int timeout) throws WebDAVException {
	return impl.refreshLock(context, lockToken, timeout);
}
/**
 * Remove a label from a revision. An exception is raised
 * if the revision does not have this label.
 * <p>
 * A revision does not need to be checked out to add a label.
 *
 * @param label the label to add to the labels used to identify
 * this revision
 * @exception com.ibm.webdav.WebDAVException
 */
public void removeLabel(String label) throws WebDAVException {
}
/** Remove properties from a resource.
 *
 * @param names an array of property names
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus removeProperties( PropertyName[] names) throws WebDAVException {
	String sPrefix = "D";
        Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element propertyUpdate = document.createElementNS("DAV:","D:propertyupdate");

	propertyUpdate.setAttribute("xmlns:D", "DAV:");
	document.appendChild(propertyUpdate);
	Element remove = document.createElementNS("DAV:","D:remove");

	propertyUpdate.appendChild(remove);
	Element prop = document.createElementNS("DAV:","D:prop");

	remove.appendChild(prop);
	for (int i = 0; i < names.length; i++) {
		// we don't care about the property value, only its name. But sending the whole
		// value element would work too because it still has the property name.
		PropertyName name = names[i];
		String prefix = "E";
		if (name.ns.equals("DAV:")) {
			prefix = "D";
		}
		Element newel = (Element) document.createElementNS(name.ns,prefix + ":" + name.local );
                
		if (prefix.equals("E")) {
			newel.setAttribute( "xmlns:E", name.ns );
		}
		prop.appendChild( newel );
	}
	return setProperties(document);
}
/** Remove a property from a resource.
 *
 * @param name the property name
 * @exception com.ibm.webdav.WebDAVException
 */
public void removeProperty( PropertyName name) throws WebDAVException {
	PropertyName[] names = new PropertyName[1];
	names[0] = name;
	MultiStatus result = removeProperties(names);
	setStatusCode(WebDAVStatus.SC_OK);
	if (result.getResponses().hasMoreElements()) {
		Response response = (Response) result.getResponses().nextElement();
		// raise any necessary exceptions
		if (response instanceof MethodResponse) {
			int status = ((MethodResponse) response).getStatus();
			if (status != WebDAVStatus.SC_OK) {
				throw new WebDAVException(status, WebDAVStatus.getStatusMessage(status));
			}
		} else {
			PropertyValue propertyValue = (PropertyValue) ((PropertyResponse) response).getPropertiesByPropName().elements().nextElement();
			if ((propertyValue.status != WebDAVStatus.SC_OK) && (propertyValue.status != WebDAVStatus.SC_FAILED_DEPENDENCY)) {
				throw new WebDAVException(propertyValue.status, WebDAVStatus.getStatusMessage(propertyValue.status));
			}
		}
	}
}
/** A utility to resize a byte array and copy its current contents.
 * @param src the source array
 * @param new_size the new size to make the array
 * @param the newly sized array (may be smaller than src)
 */
private final static byte[] resizeArray(byte[] src, int new_size) {
	byte tmp[] = new byte[new_size];
	System.arraycopy(src, 0, tmp, 0, (src.length < new_size ? src.length : new_size));
	return tmp;
}
/** Set the contents of this resource. This may create a new resource on the server,
 * or update the contents of an existing resource. Sufficient authorization is required
 * and administered by the target web server. For text/* MIME types, the caller should
 * be sure to convert Strings to byte codes using an acceptable charset, and to set
 * that charset in the request context so the server knows how to decode the byte
 * stream.
 * <p><B>deprecated</B>: Use the setContents method that takes content type as a parameter.
 *
 * @param value the new contents for the resource
 * @exception com.ibm.webdav.WebDAVException
 */
public void setContents(byte[] value) throws WebDAVException {
	setContents( value, "text/plain" );
}
/** Set the contents of this resource. This may create a new resource on the server,
 * or update the contents of an existing resource. Sufficient authorization is required
 * and administered by the target web server. For text/* MIME types, the caller should
 * be sure to convert Strings to byte codes using an acceptable charset, and to set
 * that charset in the request context so the server knows how to decode the byte
 * stream.
 *
 * @param value the new contents for the resource
 * @param mimetype the mimetype of the new contents
 * @exception com.ibm.webdav.WebDAVException
 */
public void setContents(byte[] value, String mimetype) throws WebDAVException {
	context.getRequestContext().contentType(mimetype);
	OutputStream os = getContentsOutputStream();
	try {
		os.write(value, 0, value.length);
	} catch (WebDAVException exc) {
		throw exc;
	} catch (java.io.IOException exc) {
		throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, "IO Error");
	}
	closeContentsOutputStream();
}
/** Set properties of a resource.
 *
 * @param names an array of property names
 * @param values an array of property value
 * @return a MultiStatus indicating the result of the update
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus setProperties( PropertyName[] names, Element[] values) throws WebDAVException {

        Document document = null;

        try {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	} catch(Exception e) {
          throw new WebDAVException(WebDAVStatus.SC_PROCESSING,e.getMessage());
        }
        //document.setVersion(Resource.XMLVersion);
	//document.setEncoding(Resource.defaultXMLEncoding);

	Element propertyUpdate = document.createElementNS("DAV:","D:propertyupdate");

	propertyUpdate.setAttribute("xmlns:D", "DAV:");
	document.appendChild(propertyUpdate);
	Element set = document.createElementNS("DAV:","D:set");

	propertyUpdate.appendChild(set);
	Element prop = document.createElementNS("DAV:","D:prop");

	set.appendChild(prop);
	for (int i = 0; i < names.length; i++) {
		prop.appendChild((Element) values[i]);
	}
	return setProperties(document);
}
/** Edit the properties of a resource. The updates must refer to a Document containing a WebDAV
 * DAV:propertyupdates element as the document root.
 *
 * @param updates an XML Document containing DAV:propertyupdate elements
 * describing the edits to be made
 * @return a MultiStatus indicating the status of the updates
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus setProperties(Document updates) throws WebDAVException {
	return impl.setProperties(context, updates);
}
/** Set a property of a resource to a value.
 *
 * @param name the property name
 * @param value the property value
 * @exception com.ibm.webdav.WebDAVException
 */
public void setProperty( PropertyName name, Element value) throws WebDAVException {
	PropertyName[] names = new PropertyName[1];
	names[0] = name;
	Element[] values = new Element[1];
	values[0] = value;
	int responseCode = 0;

	MultiStatus result = setProperties(names, values);
	setStatusCode(WebDAVStatus.SC_OK);
	if (result.getResponses().hasMoreElements()) {
		Response response = (Response) result.getResponses().nextElement();
		// raise any necessary exceptions
		if (response instanceof MethodResponse) {
			responseCode = ((MethodResponse) response).getStatus();
			if (responseCode != WebDAVStatus.SC_OK) {
				throw new WebDAVException(getStatusCode(), getStatusMessage());
			}
		} else {
			PropertyValue propertyValue = (PropertyValue) ((PropertyResponse) response).getPropertiesByPropName().elements().nextElement();
			responseCode = propertyValue.status;
			if ((responseCode != WebDAVStatus.SC_OK) && (responseCode != WebDAVStatus.SC_FAILED_DEPENDENCY)) {
				throw new WebDAVException(propertyValue.status, getStatusMessage());
			}
		}
	}
}
/** Set the request context for this resource. The context contains information
 * used by methods on a resource. This method is provided
 * for implementation reasons and would generally not be used by client
 * applications.
 *
 * @value the ResourceContext providing information that controls
 * method execution.
 * @exception com.ibm.webdav.WebDAVException
 */
public void setRequestContext(HTTPHeaders value) throws WebDAVException {
	context.setRequestContext(value);
}
/** Set the response context for this resource. The context contains information
 * returned from methods on a resource. This method is provided
 * for implementation reasons and would generally not be used by client
 * applications.
 *
 * @value the ResourceContext providing information resulting from
 * method execution.
 * @exception com.ibm.webdav.WebDAVException
 */
public void setResponseContext(HTTPHeaders value) throws WebDAVException {
	context.setResponseContext(value);
}
/** Set the status code corresponding to the last method execution.
 *
 * @value the status code as defined by HTTP/1.1 and the WebDAV extensions.
 * @exception com.ibm.webdav.WebDAVException
 */
public void setStatusCode(int value) throws WebDAVException {
	context.getStatusCode().setStatusCode(value);
}
/** Get a String representation of this resource.
 *
 * @return the URL of this Resource
 */
public String toString() {
	String value = null;
	try {
		value = getURL().toString();
	} catch (Exception exc) {
	}
	return value;
}
/** Unlock the lock identified by the lockToken on this resource. The request context
 * must contain the proper authorization.
 *
 * @param lockToken the lock token obtained from the ActiveLock of a previous <code>lock() </code>
 * or <code>getLocks()</code>.
 *
 * @return a MultiStatus containing any responses on resources that could not
 *     be unlocked.
 * @exception com.ibm.webdav.WebDAVException
 */
public MultiStatus unlock(String lockToken) throws WebDAVException {
	return impl.unlock(context, lockToken);
}
/**
 * @param sContentType
 */
public void closeContentsOutputStream(String sContentType) throws WebDAVException {
	if(sContentType == null) {
		impl.closeContentsOutputStream(context);
	} else {
		impl.closeContentsOutputStream(context,sContentType);
	}
	
}
}
