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

package com.ibm.webdav;

import java.io.IOException;

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
import java.util.Properties;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/** A HTTPHeaders represents additional information about the resource
 * that may effect how a method is executed, or reflect information that
 * is returned from a method execution. These methods correspond to the
 * HTTP/1.1 and WebDAV request and response Headers. Method names correspond
 * to the Header names after suitable translation to Java syntax. Not all
 * HTTP/1.1 headers are included, but all headers are supported through
 * the <code>get</code> and <code>put</code> methods. The method descriptions
 * are brief. For further details, see the HTTP/1.1 specification, section 14,
 * and the WebDAV specification, section 9.
 * <p>
 * Values for the request context are set before calling a method of
 * Resource or Collection, and provide additional parameters for
 * the method or context in which it is executed. After the method returns,
 * additional information about the method execution is available in the
 * context.</p>
 * <p>
 * Many of these headers are only used internally by the ResourceHTTPImpl
 * class to marshall arguments between the client and server using the HTTP
 * protocol. Most of this information can be more conveniently obtained through
 * the parameters and return values of class Resource.</p>
 * <p>
 * Since header names are case insensitive, all headers are
 * converted to lower case before being used to search for context information.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 */
public class HTTPHeaders extends Properties {
    public static HTTPHeaders defaults = new HTTPHeaders();

    static {
        defaults.put("connection", "close");

        defaults.put("accept", "text/xml");

        //	  defaults.put("depth", Collection.allMembers);
        //	  defaults.put("overwrite", "F");
    }

    // used for authorization headers
    static BASE64Encoder base64encoder = new BASE64Encoder();
    static BASE64Decoder base64decoder = new BASE64Decoder();

    /** The default constructor for a ResourceContext.
     */
    public HTTPHeaders() {
        super();
    }

    /** Construct a HTTPHeaders instance with a set of default values.
     * @param defaults default values for the new resource context
     */
    public HTTPHeaders(HTTPHeaders defaults) {
        super(defaults);
    }

    /** Get what media types are acceptable for a response.
    */
    public String accept() {
        return getProperty("accept");
    }

    /** Set what media types are acceptable for a response.
    */
    public void accept(String value) {
        put("accept", value);
    }

    /** Get what character sets are acceptable for a response.
    */
    public String acceptCharset() {
        return getProperty("accept-charset");
    }

    /** Set what character sets are acceptable for a response.
    */
    public void acceptCharset(String value) {
        put("accept-charset", value);
    }

    /** Get what content-encoding values are acceptable for a response.
    */
    public String acceptEncoding() {
        return getProperty("accept-encoding");
    }

    /** Get what content-encoding values are acceptable for a response.
    */
    public void acceptEncoding(String value) {
        put("accept-encoding", value);
    }

    /** Get what natural languages are acceptable for a response.
    */
    public String acceptLanguage() {
        return getProperty("accept-language");
    }

    /** Set what natural languages are acceptable for a response.
    */
    public void acceptLanguage(String value) {
        put("accept-language", value);
    }

    /** Get the range requests acceptable to a server.
    */
    public String acceptRanges() {
        return getProperty("accept-ranges");
    }

    /** Set the range requests acceptable to a server.
    */
    public void acceptRanges(String value) {
        put("accept-ranges", value);
    }

    /** Get the sender's estimate of the time since the response was generated.
    */
    public String age() {
        return getProperty("age");
    }

    /** Set the sender's estimate of the time since the response was generated.
    */
    public void age(String value) {
        put("age", value);
    }

    /** Get methods allowed on a resource
    */
    public String allow() {
        return getProperty("allow");
    }

    /** Set methods allowed on a resource
    */
    public void allow(String value) {
        put("allow", value);
    }

    /** Get the user's credentials for the realm of the resource being requested.
    */
    public String authorization() {
        return getProperty("authorization");
    }

    /** Set the user's credentials for the realm of the resource being requested.
    * @see com.ibm.webdav.ResourceContext#setBasicAuthorization
    */
    public void authorization(String value) {
        if (value != null) {
            put("authorization", value);
        } else {
            remove("authorization");
        }
    }

    /** Get the cache control directives that must be obeyed.
    */
    public String cacheControl() {
        return getProperty("cache-control");
    }

    /** Set the cache control directives that must be obeyed.
    */
    public void cacheControl(String value) {
        put("cache-control", value);
    }

    /** Get sender connection options.
    */
    public String connection() {
        return getProperty("connection");
    }

    /** Set sender connection options.
    */
    public void connection(String value) {
        put("connection", value);
    }

    /** See if the resource context contains a value for a given key. Keys are case insensitive.
    * @param keyi the name of the value to check for
    * @return true if the context contains a value for the key
    * @exception IllegalArgumentException thrown if keyi is not a String
    */
    public boolean containsKey(Object keyi) throws IllegalArgumentException {
        String key = null;

        if (keyi instanceof String) {
            key = ((String) keyi).toLowerCase();
        } else {
            throw new IllegalArgumentException(
                    "internal programming error: header must be string: " +
                    keyi);
        }

        return super.containsKey(key);
    }

    /** Get what additional content encodings have been applied to the entity body.
    */
    public String contentEncoding() {
        return getProperty("content-encoding");
    }

    /** Set what additional content encodings have been applied to the entity body.
    */
    public void contentEncoding(String value) {
        put("content-encoding", value);
    }

    /** Get the natural language of the intended audience for the entity body.
    */
    public String contentLanguage() {
        return getProperty("content-language");
    }

    /** Set the natural language of the intended audience for the entity body.
    */
    public void contentLanguage(String value) {
        put("content-language", value);
    }

    /** Get the content length in bytes of the entity body.
    */
    public long contentLength() {
        return Long.parseLong(getProperty("content-length", "-1"));
    }

    /** Set the content length in bytes of the entity body.
    */
    public void contentLength(long value) {
        put("content-length", (new Long(value)).toString());
    }

    /** Get the resource location for the response.
    */
    public String contentLocation() {
        return getProperty("content-location");
    }

    /** Set the resource location for the response.
    */
    public void contentLocation(String value) {
        put("content-location", value);
    }

    /** Get the MIME type for the response contents.
     * @return the Content-Type header
     */
    public String contentType() {
        return getProperty("content-type");
    }

    /** Set the MIME type for the response contents.
     * @param value the MIME Content-Type
     */
    public void contentType(String value) {
        put("content-type", value);
    }

    /** The date the request was made.
     * @return the Date header
     */
    public String date() {
        return getProperty("date");
    }

    /** Set the date the request was made.
    */
    public void date(String value) {
        put("date", value);
    }

    /** Get the DAV level supported by the server.
    */
    public String DAV() {
        return getProperty("dav");
    }

    /** Set the DAV level supported by the server.
    */
    public void DAV(String value) {
        put("dav", value);
    }

    /** Get the depth to apply to resource collections.
    */
    public String depth() {
        return getProperty("depth");
    }

    /** Set the depth to be applied to resource collections.
    */
    public void depth(String value) {
        put("depth", value);
    }

    /** Get the destination URL for a copy or move operation.
    */
    public String destination() {
        return getProperty("destination");
    }

    /** Set the destination URL for a copy or move operation.
    */
    public void destination(String value) {
        put("destination", value);
    }

    /** Get the entity tag for the associated entity.
    */
    public String etag() {
        return getProperty("etag");
    }

    /** Set the entity tag for the associated entity.
    */
    public void etag(String value) {
        put("etag", value);
    }

    /** Get the date/time after which the response should be considered stale.
    */
    public String expires() {
        return getProperty("expires");
    }

    /** Set the date/time after which the response should be considered stale.
    */
    public void expires(String value) {
        put("expires", value);
    }

    /** Get the value of an entry in the resource context. Keys are case insensitive.
    * @param keyi the name of the value to get
    * @exception IllegalArgumentException thrown if keyi is not a String
    */
    public Object get(Object keyi) {
        String key = null;

        if (keyi instanceof String) {
            key = ((String) keyi).toLowerCase();
        } else {
            throw new IllegalArgumentException(
                    "internal programming error: header must be string: " +
                    keyi);
        }

        return super.get(key);
    }

    /** Get the user agent or principal's identifier from the authorication context.
    * @return the user agent's identifier
    */
    public String getAuthorizationId() {
        if (authorization() == null) {
            return null;
        }

        String authorization = authorization().trim();
        String id = null;

        if (authorization.startsWith("Basic") ||
                authorization.startsWith("BASIC")) {
            String basicCookie = authorization.substring(6);

            try {
                id = new String(base64decoder.decodeBuffer(basicCookie));
            } catch (IOException exc) {
            }

            int colon = id.indexOf(':');

            if (colon > 0) {
                id = id.substring(0, colon);
            }
        }

        return id;
    }

    /** Get the user agent or principal's password from the authorication context.
    * @return the user agent's password
    */
    public String getPassword() {
        if (authorization() == null) {
            return null;
        }

        String authorization = authorization().trim();
        String pw = null;

        if (authorization.startsWith("Basic") ||
                authorization.startsWith("BASIC")) {
            String basicCookie = authorization.substring(6);

            try {
                pw = new String(base64decoder.decodeBuffer(basicCookie));
            } catch (IOException exc) {
            }

            int colon = pw.indexOf(':');

            if (colon > 0) {
                pw = pw.substring(colon + 1);
            }
        }

        return pw;
    }

    /** Get the lock timeout value. The lock is a candidate for timeing out
     * after this nunber of seconds has elapsed.
     *
     * @return the lock timeout value in seconds. -1 means infinite timeout.
     */
    public int getTimeout() {
        String t = timeout();
        int timeout = -1;

        if ((t == null) || t.equals("Infinity")) {
            timeout = -1;
        } else if (t.startsWith("Second-")) {
            timeout = new Integer(t.substring(7)).intValue();
        }

        // ignore all other cases, and use inifite timeout
        return timeout;
    }

    /** Get the Internet host and port of the resource being requested.
    */
    public String host() {
        return getProperty("host");
    }

    /** Set the Internet host and port of the resource being requested.
    */
    public void host(String value) {
        put("host", value);
    }

    /** Get when the resource was last modified.
    */
    public String lastModified() {
        return getProperty("last-modified");
    }

    /** Set when the resource was last modified.
    */
    public void lastModified(String value) {
        put("last-modified", value);
    }

    /** Get the redirect location.
    */
    public String location() {
        return getProperty("location");
    }

    /** Set the redirect location.
    */
    public void location(String value) {
        put("location", value);
    }

    /** Get the lock token for the resource.
    * @return the lock token (not the coded URL returned in the HTTP Lock-Token header)
    */
    public String lockToken() {
        String lockToken = getProperty("lock-token");

        if (lockToken.charAt(0) == '<') {
            lockToken = lockToken.substring(1, lockToken.length() - 1);
        }

        return lockToken;
    }

    /** Set the lock token for the resource. This context item must be set
    * before invoking any method on a locked resource that changes the state
    * of the resource.
    * @param value the locktoken (not the coded URL in the HTTP Lock-Token header)
    */
    public void lockToken(String value) {
        // convert the lock token to an HTTP Lock-Token header
        put("lock-token", "<" + value + ">");
    }

    /** Get if copy or move should overwrite an existing destination.
    * @return "T" if overwrite is true
    */
    public String overwrite() {
        return getProperty("overwrite");
    }

    /** Set if copy or move should overwrite an existing destination.
    * @param value "T" if overwrite is true, "F" for false
    */
    public void overwrite(String value) {
        put("overwrite", value);
    }

    /** Get any precondition that must be true in order for method
    * execution to be successful. A precondition corresponds to the
    * WebDAV "If" header.
    */
    public Precondition precondition() throws WebDAVException {
        String ifHeader = getProperty("if");
        Precondition precondition = null;

        if (ifHeader != null) {
            precondition = new Precondition(ifHeader);
        }

        return precondition;
    }

    /** Set any precondition that must be true in order for method
    * execution to be successful. A precondition corresponds to the
    * WebDAV "If" header.
    */
    public void precondition(Precondition value) {
        if (value != null) {
            put("if", value.toString());
        } else {
            remove("if");
        }
    }

    /** Set any precondition that must be true in order for method
    * execution to be successful. A precondition corresponds to the
    * WebDAV "If" header.
    */
    public void precondition(String value) {
        if (value != null) {
            put("if", value);
        } else {
            remove("if");
        }
    }

    /** Set the value of a resource context. Keys are case insensitive.
    * @param keyi the name of the value
    * @param value the value to set
    */
    public Object put(Object keyi, Object value) {
        String key = null;

        if (keyi instanceof String) {
            key = ((String) keyi).toLowerCase();
        } else {
            throw new IllegalArgumentException(
                    "internal programming error: header must be string: " +
                    keyi);
        }

        return super.put(key, value);
    }

    /** Get the URI of the resource from which the request was obtained.
    */
    public String referer() {
        return getProperty("referer");
    }

    /** Set the URI of the resource from which the request was obtained.
    */
    public void referer(String value) {
        put("referer", value);
    }

    /** Remove an entry from the resource context. Keys are case insensitive.
    * @param keyi the name of the entry to remove
    * @return the object removed
    */
    public Object remove(Object keyi) {
        String key = null;

        if (keyi instanceof String) {
            key = ((String) keyi).toLowerCase();
        } else {
            throw new IllegalArgumentException(
                    "internal programming error: header must be string: " +
                    keyi);
        }

        return super.remove(key);
    }

    /** Get information about the software used by the origin server
    * to handle the request.
    */
    public String server() {
        return getProperty("server");
    }

    /** Set information about the software used by the origin server
    * to handle the request.
    */
    public void server(String value) {
        put("server", value);
    }

    /** Set the authorization context using the Basic authentication scheme.
    * @param userid the authorization id of the user agent or principal
    * @param password the user agent's password
    */
    public void setBasicAuthorization(String userid, String password) {
        String authString = base64encoder.encode(
                                    (userid + ":" + password).getBytes());
        authorization("Basic " + authString);
    }

    /** Set the lock timeout value. The lock is a candidate for timeing out
     * after this nunber of seconds has elapsed.
     *
     * @param value the lock timeout value in seconds. -1 means infinite timeout.
     */
    public void setTimeout(int value) {
        if (value == -1) {
            timeout("Infinity");
        } else {
            timeout("Second-" + value);
        }
    }

    /** Get the URI of the resource whose method is in process.
    */
    public String statusURI() {
        return getProperty("status-uri");
    }

    /** Set the URI of the resource whose method is in process.
    */
    public void statusURI(String value) {
        put("status-uri", value);
    }

    /** Get the lock timeout value.
    */
    public String timeout() {
        return getProperty("timeout");
    }

    /** Set the lock timeout value.
    */
    public void timeout(String value) {
        put("timeout", value);
    }

    /** Get information about the user agent originating the request.
    */
    public String userAgent() {
        return getProperty("user-agent");
    }

    /** Set information about the user agent originating the request.
    */
    public void userAgent(String value) {
        put("user-agent", value);
    }
}