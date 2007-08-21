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

 /** This is the superclass of all WebDAV exceptions. It contains a status
 * code that provides information, and a descriptive message.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class WebDAVException extends java.rmi.RemoteException
{
   private int statusCode = 0;
/** Construct a WebDAVException
* @param statusCode the HTTP/1.1 or WebDAV status code
* @param statusMessage a message describing the exception of status code
*/
public WebDAVException(int statusCode, String statusMessage) {
	super(statusMessage);
	this.statusCode = statusCode;
}
/** Get the status code that provides additional information about the
* exception. These status codes are defined by the HTTP/1.1 and WebDAV
* specifications.
* @return the HTTP/1.1 or WebDAV status code
* @see com.ibm.webdav.WebDAVStatus
*/
public int getStatusCode() {
	return statusCode;
}
/** Render this WebDAVException as a string including its status code.
* @return the string includes the status code and message
*/
public String toString() {
	return (new Integer(statusCode)).toString() + ": " + getMessage();
}
}
