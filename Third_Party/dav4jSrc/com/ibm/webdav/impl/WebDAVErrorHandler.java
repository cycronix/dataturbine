/*
 * (C) Copyright Simulacra Media Ltd, 2004.  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * Simulacra Media Ltd will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will Simulacra Media Ltd be liable for any
 * special, indirect or consequential damages or lost profits even if
 * Simulacra Media Ltd has been advised of the possibility of their occurrence. 
 * Simulacra Media Ltd will not be liable for any third party claims against you.
 * 
 */

package com.ibm.webdav.impl;

import org.xml.sax.SAXParseException;

public class WebDAVErrorHandler extends org.xml.sax.helpers.DefaultHandler {

	private String resourceName = null;
	private int errorCount = 0;
/** Create an error listener for handling XML parsing errors in entity request
* or response bodies, or properties files.
*
* @param resourceName the URL of the resource involved in the request or response
*/
public WebDAVErrorHandler(String resourceName) {
	super();
	this.resourceName = resourceName;
}
/**
 * This method is called by the XML Parser when some error occurs.
 * <p>
 * This implementation prints the error to standard error and counts the errors
 * detected. Applications can then access the number of errors in order to raise
 * an exception after parsing.
 * </p>
 * @see org.xml.sax.helpers.DefaultHandler
 */
public void error(SAXParseException e) throws SAXParseException {
	errorCount++;

	System.err.println(resourceName + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + ": " + e.getMessage());
}

public void warning(SAXParseException e) throws SAXParseException {
	errorCount++;

	System.err.println(resourceName + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + ": " + e.getMessage());
}

public void fatalError(SAXParseException e) throws SAXParseException {
	errorCount++;

	System.err.println(resourceName + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + ": " + e.getMessage());

        throw e;
}

/** Return the nuber of errors counted by this error listener.
 */
public int getErrorCount() {
	return errorCount;
}
}
