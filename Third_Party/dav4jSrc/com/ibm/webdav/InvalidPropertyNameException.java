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

 /** When thrown, this class signals that the property name
 * string provided was not legal based on the definition
 * provided in the documentation of the PropertyName(String)
 * constructor.
 * @author Jason Crawford &lt;ccjason@us.ibm.com&gt;
 */
public class InvalidPropertyNameException extends Exception {
/**
 * Construct an InvalidPropertyNameException object.
 */
public InvalidPropertyNameException() {
	super( "InvalidPropertyName" );
}
/**
 * InvalidPropertyNameException constructor comment.
 * @param statusMessage a message describing the exception of status code
 */
public InvalidPropertyNameException(String s) {
	super(s);
}
}
