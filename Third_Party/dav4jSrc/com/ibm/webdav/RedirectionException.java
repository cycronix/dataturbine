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
 */

/** Represents exceptions that require further action by the user agent
 * in order to fulfill the request.
 * <p>
 * Status codes:
 * <ul>
 *    <li>300 Multiple Choices</li>
 *    <li>301 Moved Permanently</li>
 *    <li>302 Moved Temporarily</li>
 *    <li>303 See Other</li>
 *    <li>304 Not Modified</li>
 *    <li>305 Use Proxy</li>
 * </ul>
 * </p>
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.ServerException
 * @see com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.WebDAVStatus
 */
public class RedirectionException extends WebDAVException {
/** Construct a RedirectionException with a status code and simple message.
 * @param statusCode the WebDAV status code corresponding to the exception
 * @param statusMessage a message describing the status code in the context of the exception
 * @see com.ibm.webdav.WebDAVStatus
 */
public RedirectionException(int statusCode, String statusMessage) {
	super(statusCode, statusMessage);
}
}
