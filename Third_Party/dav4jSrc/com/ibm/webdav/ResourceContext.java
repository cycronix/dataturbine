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

/** A ResourceContext represents additional information about the resource
 * that may effect how a method is executed, or reflect information that
 * is returned from a method execution. It contains two HTTPHeaders object
 * representing the method request and response Headers. A ResourceContext
 * also includs a status code which indicates the success or failure of the
 * method request.
 * <p>
 * Values for the request context are set before calling a method of
 * Resource or Collection, and provide additional parameters for
 * the method or context in which it is executed. After the method returns,
 * additional information about the method execution is available in the
 * context.</p>
 * <p>
 * Many of these headers are only used internally by the ResourceHTTPStub
 * class to marshall arguments between the client and server using the HTTP
 * protocol. Some of this information can be more conveniently obtained through
 * the parameters and return values of class Resource.</p>
 * <p>
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class ResourceContext {
	protected HTTPHeaders requestContext = new HTTPHeaders();
	protected HTTPHeaders responseContext = new HTTPHeaders();
	protected WebDAVStatus statusCode = new WebDAVStatus();
        protected String methodName = "unknown";
/**
 * Insert the method's description here.
 * Creation date: (4/14/2000 3:04:10 PM)
 * @return com.ibm.webdav.HTTPHeaders
 */
public HTTPHeaders getRequestContext() {
	return requestContext;
}
/**
 * Insert the method's description here.
 * Creation date: (4/14/2000 3:04:10 PM)
 * @return com.ibm.webdav.HTTPHeaders
 */
public HTTPHeaders getResponseContext() {
	return responseContext;
}
/**
 * Insert the method's description here.
 * Creation date: (4/14/2000 3:04:10 PM)
 * @return com.ibm.webdav.WebDAVStatus
 */
public WebDAVStatus getStatusCode() {
	return statusCode;
}

public String getMethodName() {
  return methodName;
}

/**
 * Insert the method's description here.
 * Creation date: (4/14/2000 3:04:10 PM)
 * @param newRequestContext com.ibm.webdav.HTTPHeaders
 */
public void setRequestContext(HTTPHeaders newRequestContext) {
	requestContext = newRequestContext;
}
/**
 * Insert the method's description here.
 * Creation date: (4/14/2000 3:04:10 PM)
 * @param newResponseContext com.ibm.webdav.HTTPHeaders
 */
public void setResponseContext(HTTPHeaders newResponseContext) {
	responseContext = newResponseContext;
}
/**
 * Insert the method's description here.
 * Creation date: (4/14/2000 3:04:10 PM)
 * @param newStatusCode com.ibm.webdav.WebDAVStatus
 */
public void setStatusCode(WebDAVStatus newStatusCode) {
	statusCode = newStatusCode;
}

public void setMethodName(String newMethodName) {
  methodName = newMethodName;
}
}
