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
import java.util.Hashtable;

public class WebDAVStatus 
{
   private int statusCode = 200;
   
   private static Hashtable messages = new Hashtable();

   /*
	* HTTP/1.1 status codes; see RFC 1945 and the WebDAV specification
	*/

   // Provisional response:

   /**
	* Status code (100) indicating the client may continue with
	* its request.  This interim response is used to inform the 
	* client that the initial part of the request has been
	* received and has not yet been rejected by the server.
	*/
   public static final int SC_CONTINUE = 100;

   /**
	* Status code (101) indicating the server is switching protocols 
	* according to Upgrade header.
	*/
   public static final int SC_SWITCHING_PROTOCOLS = 101;

   /**
	* Status code (102) indicating the server is still processing the request. 
	*/
   public static final int SC_PROCESSING = 102;



   // Request was successfully received, understood, and accepted.

   /**
	* Status code (200) indicating the request succeeded normally.
	*/
   public static final int SC_OK = 200;

   /**
	* Status code (201) indicating the request succeeded and created
	* a new resource on the server.
	*/
   public static final int SC_CREATED = 201;

   /**
	* Status code (202) indicating that a request was accepted for
	* processing, but was not completed.
	*/
   public static final int SC_ACCEPTED = 202;

   /**
	* Status code (203) indicating that the meta information presented 
	* by the client did not originate from the server. 
	*/
   public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;

   /**
	* Status code (204) indicating that the request succeeded but that
	* there was no new information to return.
	*/
   public static final int SC_NO_CONTENT = 204;

   /**
	* Status code (205) indicating that the agent SHOULD reset the document 
	* view which caused the request to be sent. 
	*/
   public static final int SC_RESET_CONTENT = 205;

   /**
	* Status code (206) indicating that the server has fulfilled the partial 
	* GET request for the resource.
	*/
   public static final int SC_PARTIAL_CONTENT = 206;

   /**
	* Status code (207) indicating that the response provides status for multiple
	* independent operations.
	*/
   public static final int SC_MULTI_STATUS = 207;



   // Redirection: indicates further action needs to be taken by the user.

   /** Status code (300) indicating that the requested resource corresponds to any one of
	* a set of representations, each with its own specific location
	*/
   public static final int SC_MULTIPLE_CHOICES = 300;

   /**
	* Status code (301) indicating that the resource has permanently
	* moved to a new location, and that future references should use a
	* new URI with their requests.
	*/
   public static final int SC_MOVED_PERMANENTLY = 301;

   /**
	* Status code (302) indicating that the resource has temporarily
	* moved to another location, but that future references should
	* still use the original URI to access the resource.
	*/
   public static final int SC_MOVED_TEMPORARILY = 302;

   /**
	* Status code (303) indicating that the response to the request can 
	* be found under a different URI.
	*/
   public static final int SC_SEE_OTHER = 303;

   /**
	* Status code (304) indicating that a conditional GET operation
	* found that the resource was available and not modified.
	*/
   public static final int SC_NOT_MODIFIED = 304;

   /**
	* Status code (305) indicating that the requested resource MUST be accessed 
	* through the proxy given by the Location field. 
	*/
   public static final int SC_USE_PROXY = 305;



   // Client error

   /**
	* Status code (400) indicating the request sent by the client was
	* syntactically incorrect.
	*/
   public static final int SC_BAD_REQUEST = 400;

   /**
	* Status code (401) indicating that the request requires HTTP
	* authentication.
	*/
   public static final int SC_UNAUTHORIZED = 401;

   /**
	* Status code (402) reserved for future use.
	*/
   public static final int SC_PAYMENT_REQUIRED = 402;

   /**
	* Status code (403) indicating the server understood the request
	* but refused to fulfill it.
	*/
   public static final int SC_FORBIDDEN = 403;

   /**
	* Status code (404) indicating that the requested resource is not
	* available.
	*/
   public static final int SC_NOT_FOUND = 404;

   /**
	* Status code (405) indicating the method specified is not
	* allowed for the resource.
	*/
   public static final int SC_METHOD_NOT_ALLOWED = 405;

   /**
	* Status code (406) indicating the resource identified by the 
	* request is only capable of generating response entities 
	* which have content characteristics not acceptable according 
	* to the accept headerssent in the request. 
	*/
   public static final int SC_NOT_ACCEPTABLE = 406;

   /**
	* Status code (407) indicating the client MUST first authenticate
	* itself with the proxy.
	*/
   public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
   
   /**
	* Status code (408) indicating the client did not produce a request within 
	* the time that the server was prepared to wait. 
	*/
   public static final int SC_REQUEST_TIMEOUT = 408;
   
   /**
	* Status code (409) indicating that the request could not be
	* completed due to a conflict with the current state of the
	* resource.
	*/
   public static final int SC_CONFLICT = 409;

   /**
	* Status code (410) indicating the server did not receive a timely 
	* response from the upstream server while acting as a gateway or proxy. 
	*/
   public static final int SC_GONE = 410;

   /**
	* Status code (411) indicating the request cannot be handled 
	* without a defined Content-Length. 
	*/
   public static final int SC_LENGTH_REQUIRED = 411;

   /**
	* Status code (412) indicating the precondition given in one
	* or more of the request-header fields evaluated to false
	* when it was tested on the server.
	*/
   public static final int SC_PRECONDITION_FAILED = 412;

   /**
	* Status code (413) indicating the server is refusing to
	* process a request because the request entity is larger
	* than the server is willing or able to process.
	*/
   public static final int SC_REQUEST_TOO_LONG = 413;

   /**
	* Status code (414) indicating the server is refusing to 
	* service the request because the Request-URI is longer 
	* than the server is willing to interpret. 
	*/
   public static final int SC_REQUEST_URI_TOO_LONG = 414;

   /**
	* Status code (415) indicating the server is refusing to service
	* the request because the entity of the request is in a format
	* not supported by the requested resource for the requested
	* method.
	*/
   public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

   /**
	* Status code (422) indicating the server understands the content type of the
	* request entity, but was unable to process the contained instructions.
	*/
   public static final int SC_UNPROCESSABLE_ENTITY = 422;

   /**
	* Status code (423) indicating the source or destination resource of a
	* method is locked.
	*/
   public static final int SC_LOCKED = 423;

   /**
	* Status code (424) indicating the method was not executed on
	* a particular resource within its scope because some part of
	* the method's execution failed causing the entire method to be
	* aborted.  (AKA Failed Dependency)
	*/
   public static final int SC_FAILED_DEPENDENCY = 424;
   
   /**
	* Status code (425) indicating that the resource does not have sufficient
	* space to record the state of the resource after the executino of the method.
	*/
   public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 425;



   /**
	* Status code (426) indicating that the resource does not have 
	* the specified target revision
	*/
   public static final int SC_NO_SUCH_TARGET = 426;



   // Server errors

   /**
	* Status code (500) indicating an error inside the HTTP service
	* which prevented it from fulfilling the request.
	*/
   public static final int SC_INTERNAL_SERVER_ERROR = 500;

   /**
	* Status code (501) indicating the HTTP service does not support
	* the functionality needed to fulfill the request.
	*/
   public static final int SC_NOT_IMPLEMENTED = 501;

   /**
	* Status code (502) indicating that the HTTP server received an
	* invalid response from a server it consulted when acting as a
	* proxy or gateway.
	*/
   public static final int SC_BAD_GATEWAY = 502;

   /**
	* Status code (503) indicating that the HTTP service is
	* temporarily overloaded, and unable to handle the request.
	*/
   public static final int SC_SERVICE_UNAVAILABLE = 503;

   /**
	* Status code (504) indicating the server did not receive a 
	* timely response from the upstream server while acting as a
	* gateway or proxy. 
	*/
   public static final int SC_GATEWAY_TIMEOUT = 504;

   /**
	* Status code (505) indicating the server does not support or 
	* refuses to support the HTTP protocol version that was used 
	* in the request message.
	*/
   public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

   //----------------------------------------------------------------
   
   static {
	  messages.put(new Integer(SC_CONTINUE), "Continue");
	  messages.put(new Integer(SC_SWITCHING_PROTOCOLS), "Switching Protocols");
	  messages.put(new Integer(SC_PROCESSING), "Processing");
	  messages.put(new Integer(SC_OK), "OK");
	  messages.put(new Integer(SC_CREATED), "Created");
	  messages.put(new Integer(SC_ACCEPTED), "Accepted");
	  messages.put(new Integer(SC_NON_AUTHORITATIVE_INFORMATION), "Non-Authoritative Information");
	  messages.put(new Integer(SC_NO_CONTENT), "No Content");
	  messages.put(new Integer(SC_RESET_CONTENT), "Reset Content");
	  messages.put(new Integer(SC_PARTIAL_CONTENT), "Partial Content");
	  messages.put(new Integer(SC_MULTI_STATUS), "Multi-Status");
	  messages.put(new Integer(SC_MULTIPLE_CHOICES), "Multiple Choices");
	  messages.put(new Integer(SC_MOVED_PERMANENTLY), "Moved Permanently");
	  messages.put(new Integer(SC_MOVED_TEMPORARILY), "Moved Temporarily");
	  messages.put(new Integer(SC_SEE_OTHER), "See Other");
	  messages.put(new Integer(SC_NOT_MODIFIED), "Not Modified");
	  messages.put(new Integer(SC_USE_PROXY), "Use Proxy");
	  messages.put(new Integer(SC_BAD_REQUEST), "Bad Request");
	  messages.put(new Integer(SC_UNAUTHORIZED), "Unauthorized");
	  messages.put(new Integer(SC_PAYMENT_REQUIRED), "Payment Required");
	  messages.put(new Integer(SC_FORBIDDEN), "Forbidden");
	  messages.put(new Integer(SC_NOT_FOUND), "Not Found");
	  messages.put(new Integer(SC_METHOD_NOT_ALLOWED), "Method Not Allowed");
	  messages.put(new Integer(SC_NOT_ACCEPTABLE), "Not Acceptable");
	  messages.put(new Integer(SC_PROXY_AUTHENTICATION_REQUIRED), "Proxy Authentication Required");
	  messages.put(new Integer(SC_REQUEST_TIMEOUT), "Request Time-out");
	  messages.put(new Integer(SC_CONFLICT), "Conflict");
	  messages.put(new Integer(SC_GONE), "Gone");
	  messages.put(new Integer(SC_LENGTH_REQUIRED), "Length Required");
	  messages.put(new Integer(SC_PRECONDITION_FAILED), "Precondition Failed");
	  messages.put(new Integer(SC_REQUEST_TOO_LONG), "Request Entity Too Large");
	  messages.put(new Integer(SC_REQUEST_URI_TOO_LONG), "Request-URI Too Large");
	  messages.put(new Integer(SC_UNSUPPORTED_MEDIA_TYPE), "Unsupported Media Type");
	  messages.put(new Integer(SC_UNPROCESSABLE_ENTITY), "Unprocessable Entity");
	  messages.put(new Integer(SC_LOCKED), "Locked");
	  messages.put(new Integer(SC_FAILED_DEPENDENCY), "Failed Dependency");
	  messages.put(new Integer(SC_INSUFFICIENT_SPACE_ON_RESOURCE), "Inusfficient Space On Resource");
	  messages.put(new Integer(SC_NO_SUCH_TARGET), "No Such Target");
	  messages.put(new Integer(SC_INTERNAL_SERVER_ERROR), "Internal Server Error");
	  messages.put(new Integer(SC_NOT_IMPLEMENTED), "Not Implemented");
	  messages.put(new Integer(SC_BAD_GATEWAY), "Bad Gateway");
	  messages.put(new Integer(SC_SERVICE_UNAVAILABLE), "Service Unavailable");
	  messages.put(new Integer(SC_GATEWAY_TIMEOUT), "Gateway Time-out");
	  messages.put(new Integer(SC_HTTP_VERSION_NOT_SUPPORTED), "HTTP Version not supported");
   }
   

/**
 * Get the value of this WebDAVStatus
 * @return int
 */
public int getStatusCode() {
	return statusCode;
}
/** Get a message describing the status code of this WebDAVStatus
* @return the corresponding status message or "Unknown" if there is no message for
*    the current status code
*/
public String getStatusMessage() {
	return getStatusMessage(statusCode);
}
/** Get a message describing the status code.
* @param statusCode an HTTP/1.1 or WebDAV status code
* @return the corresponding status message or "Unknown" if there is no message for
*    the given status code
*/
public static String getStatusMessage(int statusCode) {
	String message = "Unknown";
	if (statusCode > 0) {
		message = (String) messages.get(new Integer(statusCode));
	}
	return message;
}
/**
 * Set the value of this WebDAVStatus
 * @param newStatusCode int
 */
public void setStatusCode(int newStatusCode) {
	statusCode = newStatusCode;
}
/**
 * Convert a WebDAVStatus to a String for printing, etc.
 * @param newStatusCode int
 */
public String toString() {
	return new String(getStatusMessage()+'('+statusCode+')');
}
}
