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
import java.util.logging.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

/**
 * ActiveLock contains information about locks on this resourece. The
 * information can be used to provide details when obtaining locks, or for
 * holding results from lock queries. It combines the activelock DAV element
 * with the principal, and the lock expiration date.
 * 
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class ActiveLock extends Object {

	/**
	 * Only one exclusive lock can be granted at any time on a resource.
	 *  
	 */
	public static final String exclusive = "exclusive";

	/**
	 * A resource may have many concurrent shared locks which indicate an
	 * intention to change the resource in some way. It is the responsibilty of
	 * the shared lock owners to coordinate their updates appropriately through
	 * other means.
	 */
	public static final String shared = "shared";

	/**
	 * Write locks allow a resource to be updated or deleted.
	 *  
	 */
	public static final String writeLock = "write";

	//---------------------------------------------------------------------------------

	// headers
	private String timeout = "Infinite";

	private String depth = Collection.deep;

	// from the request entity
	private String scope = ActiveLock.exclusive;

	private String lockType = ActiveLock.writeLock;

	private Element owner = null;

	private String sOwner = null;

	// from the response entity
	private String lockToken = null;

	// useful extensions:
	private Date expiration = null; // when the lock will expire

	private String principal = null; // the userid of the lock owner
	
	/**
	 * Logger for this class
	 */
	private static final Logger m_logger = Logger.getLogger(ActiveLock.class.getName());

	/**
	 * The default constructor.
	 *  
	 */
	public ActiveLock() {
	}

	/**
	 * Convert an activelock Element into a ActiveLock instance for convenient
	 * access to the lock information. This method uses two IBM extensions to
	 * the WebDAV activelock element, one for the principal, and onother for the
	 * lock expiration time.
	 * 
	 * @param an
	 *            activelock Element containing information about the lock
	 * @exception com.ibm.webdav.WebDAVException
	 */
	public ActiveLock(Element activeLock) throws WebDAVException {

		Element lockScope = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"lockscope").item(0);
		setScope(((Element) lockScope.getFirstChild()).getLocalName());

		Element lockType = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"locktype").item(0);
		setLockType(((Element) lockType.getFirstChild()).getLocalName());

		Element depth = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"depth").item(0);
		setDepth(((Text) depth.getFirstChild()).getData());

		Element owner = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"owner").item(0);
		if (owner != null) {
			setOwner(owner);
		}
		Element timeout = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"timeout").item(0);
		if (timeout != null) {
			this.timeout = ((Text) timeout.getFirstChild()).getData();
		}

		// TODO: There may be many locktokens all identifying the same lock
		Element lockToken = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"locktoken").item(0);
		if (lockToken != null) {
			Element href = (Element) lockToken.getElementsByTagNameNS("DAV:",
					"href").item(0);
			if (href != null) {
				this.lockToken = ((Text) href.getFirstChild()).getData();
			}
		}

		// IBM extensions:

		Element principal = (Element) activeLock.getElementsByTagNameNS("DAV:",
				"principal").item(0);
		if (principal != null) {
			setPrincipal(((Text) principal.getFirstChild()).getData());
		}

		//	Element principal =
		// (Element)activeLock.getElementsByTagName("principal").item(0);
		//		if (principal != null) {
		//			setPrincipal(((Text) principal.getFirstChild()).getData());
		//		}

		//	Element expiration =
		// (Element)activeLock.getElementsByTagName("expiration").item(0);
		//	if (expiration != null) {
		//		Date d = null;
		//		try {
		//			DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		//			d = df.parse(((Text) expiration.getFirstChild()).getData());
		//		} catch (ParseException exc) {
		//			System.err.print("Unable to parse lock expiration date: " + exc);
		//		}
		//		setExpiration(d);
		//	}
	}

	/**
	 * Translate this ActiveLock instance into an activelock XML element. The
	 * activelock element will include two IBM extensions, one for the principal
	 * owning the lock (the authorization id), and another containing the
	 * expiration date for the lock.
	 * 
	 * @return the DOM representation of an activelock XML element
	 */
	public Element asXML() {
		String sPrefix = "D";
		// the document is only used create elements
		Document document = null;

		try {
			document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (Exception e) {
			m_logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		//document.setVersion(Resource.XMLVersion);
		//document.setEncoding(Resource.defaultXMLEncoding);

		Element activelock = document.createElementNS("DAV:", "D:activelock");

		activelock.setAttribute("xmlns:D", "DAV:");

		Element lockscope = document.createElementNS("DAV:", "D:lockscope");

		Element scopeEl = document.createElementNS("DAV:", "D:" + this.scope);

		lockscope.appendChild(scopeEl);
		activelock.appendChild(lockscope);

		Element locktype = document.createElementNS("DAV:", "D:locktype");

		Element locktypeEl = document.createElementNS("DAV:", "D:"
				+ this.lockType);

		locktype.appendChild(locktypeEl);
		activelock.appendChild(locktype);

		Element depth = document.createElementNS("DAV:", "D:depth");

		depth.appendChild(document.createTextNode(this.depth));
		activelock.appendChild(depth);

		if (this.owner != null) {
			if (owner.getPrefix() == null
					|| owner.getPrefix().equals("D") == false) {

				Element ownerEl = document.createElementNS("DAV:", "D:owner");
				Element hrefEl = document.createElementNS("DAV:", "D:href");

				String sOwnerVal = owner.getChildNodes().item(0)
						.getChildNodes().item(0).getNodeValue();

				Text txtOwnerVal = document.createTextNode(sOwnerVal);

				hrefEl.appendChild(txtOwnerVal);

				ownerEl.appendChild(hrefEl);
				activelock.appendChild(ownerEl);
				owner = ownerEl;
			} else {
				activelock.appendChild(document.importNode(owner, true));
			}
		} else if (sOwner != null) {
			Element ownerEl = document.createElementNS("DAV:", "D:owner");
			Element hrefEl = document.createElementNS("DAV:", "D:href");

			hrefEl.appendChild(document.createTextNode(sOwner));
			ownerEl.appendChild(hrefEl);
			activelock.appendChild(ownerEl);
		}

		Element timeout = document.createElementNS("DAV:", "D:timeout");

		timeout.appendChild(document.createTextNode(this.timeout));
		activelock.appendChild(timeout);

		if (this.lockToken != null) {
			Element locktoken = document.createElementNS("DAV:", "D:locktoken");

			Element href = document.createElementNS("DAV:", "D:href");

			href.appendChild(document.createTextNode(this.lockToken));
			locktoken.appendChild(href);
			activelock.appendChild(locktoken);
		}

		if (getPrincipal() != null) {
			Element principal = (Element) document.createElementNS("DAV:",
					"D:principal");
			principal.appendChild(document.createTextNode(getPrincipal()));
			activelock.appendChild(principal);
		}

		//	if (getPrincipal() != null) {
		//			Element principal = (Element) document.createElement("principal");
		//			principal.appendChild(document.createTextNode(getPrincipal()));
		//			activelock.appendChild(principal);
		//		}

		//		if (getExpiration() != null) {
		//			Element expiration = (Element) document.createElement("expiration");
		//			expiration.appendChild(document.createTextNode(getExpiration().toString()));
		//			activelock.appendChild(expiration);
		//		}

		return activelock;
	}

	/**
	 * Get the depth of the lock.
	 * <ul>
	 * <li>shallow: only this resource is locked or will be locked</li>
	 * <li>deep: this resource and recursively, all its internal members are
	 * locked</li>
	 * </ul>
	 * 
	 * @return shallow or deep
	 */
	public String getDepth() {
		return depth;
	}

	/**
	 * Get the date and time when the lock will timeout. Null means it will
	 * never timeout. This is an IBM EXTENSION.
	 * 
	 * @return the expiration date for the lock
	 */
	public Date getExpiration() {
		return expiration;
	}

	/**
	 * Get the lock token. A lock token represents the lock, and is used to
	 * unlock the resource or for any access that might change the resource
	 * state. There may be many (shared) locks on a resource, each with its own
	 * lock token. Each lock will have its own ActiveLock instance describing
	 * the lock and providing access to the lock token. See the lockdiscovery
	 * DAV property.
	 * 
	 * @return the lock token identifying a lock on this resource.
	 */
	public String getLockToken() {
		return lockToken;
	}

	/**
	 * Get the type of the lock.
	 * 
	 * @return write (other lock types may be supported in the future)
	 */
	public String getLockType() {
		return lockType;
	}

	/**
	 * Get the owner of the lock. The method provides information about the
	 * principal taking out the lock, but not necessarily the principal's
	 * authorization ID.
	 * 
	 * @return any information that might identify the principal taking out the
	 *         lock.
	 */
	public Element getOwner() {
		return owner;
	}

	/**
	 * Get the user authorization id of the owner of the lock. This is an IBM
	 * EXTENSION to the WebDAV activelock.
	 * 
	 * @return the principal owning this lock as given in the Authorization
	 *         context on lock
	 */
	public String getPrincipal() {
		return principal;
	}

	/**
	 * Get the scope of the lock.
	 * 
	 * @return exclusive or shared
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Get the lock timeout.
	 * 
	 * @return the lock timeout as either Second-n or "Infinite" for no timeout.
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * Get the time remaining before the lock times out
	 * 
	 * @return the number of seconds that must elapse before the lock times out.
	 *         0 means the lock has timed out.
	 */
	public long getTimeRemaining() {
		long now = new Date().getTime();
		long t = expiration.getTime() - now;
		if (t < 0) {
			t = 0;
		}
		return t;
	}

	/**
	 * Set the depth of the lock.
	 * <ul>
	 * <li>shallow: only this resource is locked or will be locked</li>
	 * <li>deep: this resource and recursively, all its internal members are
	 * locked</li>
	 * </ul>
	 * 
	 * @param depth
	 *            shallow or deep
	 * @exception com.ibm.webdav.ClientException
	 *                thrown if the depth is incorrect
	 */
	public void setDepth(String depth) throws ClientException {
		if (!(depth.equals(Collection.shallow) || depth.equals(Collection.deep))) {
			throw new ClientException(400, "invalid lock depth");
		}
		this.depth = depth;
	}

	/**
	 * Set the date and time when the lock will timeout. Null means it will
	 * never timeout. This is an IBM EXTENSION.
	 */
	public void setExpiration(Date value) {
		expiration = value;
	}

	/**
	 * Set the lock token.
	 * 
	 * @param lockToken
	 *            the lock token corresponding to a lock on a resource.
	 */
	public void setLockToken(String lockToken) {
		this.lockToken = lockToken;
	}

	/**
	 * Set the type of the lock.
	 * 
	 * @param lockType
	 *            write (other lock types may be supported in the future)
	 * @exception com.ibm.webdav.ClientException
	 *                thrown if the lockType is incorrect. Currently, only write
	 *                locks are supported.
	 */
	public void setLockType(String lockType) throws ClientException {
		if (!(lockType.equals(writeLock))) {
			throw new ClientException(400, "invalid lock type: " + lockType);
		}
		this.lockType = lockType;
	}

	/**
	 * Set the owner of the lock. The method sets the information about the
	 * principal taking out the lock. This is not necessarily the authorization
	 * id of the principal owning the lock, and therefore cannot be relied upon
	 * for authentication.
	 * 
	 * @param owner
	 *            any information that might identify the principal taking out
	 *            the lock.
	 */
	public void setOwner(Element owner) {
		this.owner = owner;
	}

	public void setOwner(String sOwner) {
		this.sOwner = sOwner;
	}

	/**
	 * Set the user authorization id of the owner of the lock. This is and IBM
	 * EXTENSION.
	 */
	public void setPrincipal(String value) {
		principal = value;
	}

	/**
	 * Set the scope of the lock.
	 * 
	 * @param scope
	 *            exclusive or shared
	 * @exception com.ibm.webdav.ClientException
	 *                thrown if the scope is invalid
	 */
	public void setScope(String scope) throws ClientException {
		if (!(scope.equals(exclusive) || scope.equals(shared))) {
			throw new ClientException(400, "invalid lock scope: " + scope);
		}
		this.scope = scope;
	}

	/**
	 * Set the lock timeout.
	 * 
	 * @param timeout
	 *            the lock timeout in seconds. -1 means infinite timeout.
	 */
	public void setTimeout(int timeout) {
		if (timeout < 0) {
			setTimeout("Infinite");
		} else {
			setTimeout("Second-" + new Integer(timeout).toString());
		}
	}

	/**
	 * Set the lock timeout.
	 * 
	 * @param timeout
	 *            the lock timeout as either Second-n or "Infinite" for no
	 *            timeout. Any other syntax is ignored.
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
		if (timeout.equals("Infinite")) {
			expiration = null;
		} else {
			if (timeout.startsWith("Second-")) {
				long t = new Long(timeout.substring(7)).longValue();
				expiration = new Date(new Date().getTime() + t * 1000);
			}
		}
	}

	/**
	 * Convert this ActiveLock to a String representation (an activelock XML
	 * element).
	 * 
	 * @return a String representation of the ActiveLock as an activelock
	 *         element
	 *  
	 */
	public String toString() {
		/*
		 * ByteArrayOutputStream os = new ByteArrayOutputStream(); PrintWriter
		 * pout = new PrintWriter(os); Element activelock = (Element) asXML();
		 * try { activelock.print(pout); } catch (Exception exc) { }
		 * pout.close(); return os.toString();
		 */
		return XMLUtility.printNode(asXML());
	}
}