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

/** Represents exceptions that can happen on the server as the
 * result of a server error.
 * <p>
 * Status codes:
 * <ul>
 *    <li>500 Internal Server Error</li>
 *    <li>501 Not Implemented</li>
 *    <li>502 Bad Gateway</li>
 *    <li>503 Service Unavailable</li>
 *    <li>504 Gateway Timeout</li>
 *    <li>505 HTTP Version Not Supported</li>
 * </ul>
 * </p>
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.ClientException
 * @see com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.WebDAVStatus
 */
public class ServerException extends WebDAVException
{
/** Construct a ServerException with a status code and simple message.
 * @param statusCode the WebDAV status code corresponding to the exception
 * @param statusMessage a message describing the status code in the context of the exception
 * @see com.ibm.webdav.WebDAVStatus
 */
public ServerException(int statusCode, String statusMessage) {
	super(statusCode, statusMessage);
}
}
