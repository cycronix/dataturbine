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

/** Represents exceptions that can happen on the Client as the
 * result of a client error.
 * <p>
 * Status codes:
 * <ul>
 *    <li>400 Bad Request</li>
 *    <li>401 Unauthorized</li>
 *    <li>402 Payment Required</li>
 *    <li>403 Forbidden</li>
 *    <li>404 Not Found</li>
 *    <li>405 Mothod Not Allowed</li>
 *    <li>406 Not Acceptable</li>
 *    <li>407 Proxy Authentication Required</li>
 *    <li>408 Request Timeout</li>
 *    <li>409 Conflict</li>
 *    <li>410 Gone</li>
 *    <li>411 Length Required</li>
 *    <li>412 Precondition Failed</li>
 *    <li>413 Request Entity Too Large</li>
 *    <li>414 Request-URI Too Long</li>
 *    <li>415 Unsupported Media Type</li>
 *    <li>422 Unprocessable Entity</li>
 *    <li>423 Locked</li>
 *    <li>424 Method Failure</li>
 *    <li>425 Insufficient Space on Resource</li>
 * </ul>
 * </p>
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see com.ibm.webdav.ServerException
 * @see com.ibm.webdav.WebDAVException
 * @see com.ibm.webdav.WebDAVStatus
 */
public class ClientException extends WebDAVException
{
/** Construct a ClientException with a status code and simple message.
 * @param statusCode the WebDAV status code corresponding to the exception
 * @param statusMessage a message describing the status code in the context of the exception
 * @see com.ibm.webdav.WebDAVStatus
 */
public ClientException(int statusCode, String statusMessage) {
	super(statusCode, statusMessage);
}
}
