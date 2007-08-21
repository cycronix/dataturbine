/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.plugins;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;

import com.rbnb.sapi.*;

import org.apache.xmlrpc.Base64;
//import org.apache.xmlrpc.XmlRpcException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


//import uk.co.wilson.xml.MinML;


/**
  * RBNB XML-RPC Client.
  * <p>
  * @author WHF
  * @since V2.0B9
  */
  
/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/17/2002  WHF	Created main function.
 * 12/18/2002  WHF  Made into full fledged client class.
 * 12/23/2002  WHF  Now uses local XML-RPC exception instead of depending
 *  on open source version.
 * 02/05/2003  WHF  Added close method.
 * 05/27/2003  WHF  Dumped MimML parser, which did not meet specifications.
 * 06/02/2003  WHF  Added PlugInName property, which specifies the fully 
 *  qualified name of the target PlugIn.
 * 2003/12/02  WHF  Reduced debug output.
 * 2003/12/15  WHF  Added debug flag to XMLRPCClient class, implemented way
 *   to set debug flag in XMLRPCResponseProcessor. 
*/
public class XMLRPCClient
{
// ************************* Construction *******************************//
	/**
	  * Sets the address field to "localhost:3333" and the client name to
	  *  "XMLRPCClient".
	  */
	public XMLRPCClient()
	{ this("localhost:3333", "XMLRPCClient"); }
	
	/**
	  * Preset the address and name fields.
	  */
	public XMLRPCClient(String address, String name)
	{
		this(new Sink());
		this.host=address;
		this.name=name;
	}
	
	/**
	  * Uses a pre-existing sink connection.
	  */
	public XMLRPCClient(Sink sink)
	{ 	
		this.sink=sink;
		
		// Exception safe initialization:
		XmlWriter temp=null;
		XMLRPCResponseProcessor tempProcessor=null;
		try {
		tempProcessor=new XMLRPCResponseProcessor(); 	
		temp=new XmlWriter(baos);
		} catch (Exception e) { e.printStackTrace(); } // never
		writer=temp;
		processor=tempProcessor;
		
		debug = Boolean.getBoolean("com.rbnb.plugins.XMLRPCClient.debug");
	}
	
// ************************* Accessors *******************************//
	/**
	  * The address of the RBNB to which this Client should connect.
	  */
	public String getRBNBAddress() { return host; }
	
	/**
	  * Set the address used by the sink.
	  */
	public void setRBNBAddress(String host) { this.host=host; }
		
	public String getClientName() { return name; }
	public void setClientName(String name) { this.name=name; }
	
	/** 
	  * Get the fully qualified name of the XMLRPC PlugIn to communicate with.
	  */
	public String getPlugInName() { return pluginName; }

	/** 
	  * Set the fully qualified name of the XMLRPC PlugIn to communicate with.
	  */
	public void setPlugInName(String pluginName) { this.pluginName=pluginName; }
	
	public String getPlugInChannel() { return pluginChannel; }
	public void setPlugInChannel(String pluginChannel) 
	{ this.pluginChannel=pluginChannel; }
	
// ************************* Public Methods *******************************//
	/**
	  * Call a method on a remote object through XML-RPC.
	  *
	  * @param method The object and method to invoke on the server, in the form
	  *  "object.method".&nbsp; If the server supports a default object, the
	  *  object portion may be omitted.
	  * @param params The arguments for the function; the data types supported
	  *  can be found at <a href="http://xml.apache.org/xmlrpc/types.html">
	  * http://xml.apache.org/xmlrpc/types.html</a> or
	  *  <a href="http://www.xmlrpc.com/spec">http://www.xmlrpc.com/spec</a>.
	  * @throws SAPIException If there is an RBNB communication error.
	  * @throws IOException If there is a low level IO error.
	  * @throws XmlRpcException If the XML-RPC server has an error.
	  * @throws SAXException If there is an error processing the returned XML.
	  */
	public Object invoke(String method, Vector params) throws SAPIException,
		IOException, XMLRPCException, SAXException
	{
		checkSinkConnection();
		baos.reset();
		map.Clear();

		writeRequest(writer, method, params);
		writer.flush();
		
		int index=map.Add(pluginName+"/"+pluginChannel);
		map.PutDataAsByteArray(index, baos.toByteArray());
		map.PutMime(index,"text/xml");
//System.err.println(map);
		sink.Request(map,0,0,"newest");
		sink.Fetch(-1, map);
		if (debug)
			for (int ii=0; ii<map.NumberOfChannels(); ++ii)
				System.err.println(new String(map.GetDataAsByteArray(ii)[0]));

		Object res=processor.process(
			new ByteArrayInputStream(map.GetDataAsByteArray(0)[0]));
//System.err.println(res.getClass().getName()+" = "+res);
		return res;
	}
	
	/**
	  * Close RBNB connections, free resources.
	  *
	  * @author WHF
	  */
	public void close()
	{
		sink.CloseRBNBConnection();	
	}
	
// ************************* Private methods *******************************//
	private void checkSinkConnection() throws SAPIException
	{
		try {
			sink.GetClientName();
		} catch (IllegalStateException ise)
		{ sink.OpenRBNBConnection(host,name);  }		
	}		
	
// ************************* Private data *******************************//
	private final Sink sink;
	private final ChannelMap map=new ChannelMap();
	private final ByteArrayOutputStream baos=new ByteArrayOutputStream();
	private final XmlWriter writer;
	private final XMLRPCResponseProcessor processor;
	private String host, name,  // identify the RBNB 
		pluginName=XMLRPCPlugIn.class.getName(),  // identify the plugin
		pluginChannel="request";
	private final boolean debug;
	
// ************************* Static methods *******************************//
	
	public static void main(String args[])
	{
		try {
		int ii;
		String host="localhost:3333", piName=null, piChannel=null;
		Vector v=new Vector();
		for (ii=0; ii<args.length; ++ii) {
			if ("-a".equals(args[ii])) host=args[++ii];
			else if ("-n".equals(args[ii])) piName=args[++ii];
			else if ("-c".equals(args[ii])) piChannel=args[++ii];
			else break;
		}

		for (int iii=ii+1; iii<args.length; ++iii) {
			v.add(parseArguments(args[iii], null));
		}
			
		XMLRPCClient client=new XMLRPCClient(host, "XMLRPCClient");
		if (piName!=null) client.setPlugInName(piName);
		if (piChannel!=null) client.setPlugInChannel(piChannel);
		Object result=client.invoke(args[ii], v);
		System.out.println("Result: Type="+result.getClass().getName()
			+" Value="+result);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	  * Parses an argument.  Leaves it as a String unless it begins with
	  * '{', in which case it becomes a Hashtable.  It is then processed
	  *  recursively.
	  */
	private static Object parseArguments(
			String arg,
			StringCharacterIterator sci)
	{
		if (sci == null) sci=new StringCharacterIterator(arg);
		if (sci.current() == '[' || sci.current() == '{') {
			Hashtable ht = null;
			Vector vt = null;
			if (sci.current() == '{') 
				ht = new Hashtable();
			if (sci.current() == '[')
				vt = new Vector();
			
			int startField = -1, startName = sci.getIndex()+1;
			while (true) {
				char c=sci.next();
				switch (c) {
					case '}': 
					case ',':
					case ']':
						if (ht != null) {
							if (startName != -1)
								if (startField == -1) 
									throw new IllegalArgumentException(
											"Blank entry illegal.");
								else {
									ht.put(	arg.substring(startName,
													startField-1),
											arg.substring(startField,
													sci.getIndex()));
								}
							startName=sci.getIndex()+1;
							startField=-1;
							if (c == '}') {
								//System.err.println(ht);					
								return ht;
							}
						}
						else {
							vt.add(arg.substring(startName, sci.getIndex()));
							startName = sci.getIndex()+1;
							startField=-1;
							if (c == ']') {
								return vt;
							}
						}
						break;
					
					case '=': startField=sci.getIndex()+1; break;
					
					case '{':
					case '[':
						ht.put(	arg.substring(startName, startField-1),
								parseArguments(arg, sci));
						startName=startField=-1;
						break;
						
						
					case StringCharacterIterator.DONE: 
						throw new IllegalArgumentException(
								"Unterminated brace.");
				}
			}
		} else return arg.substring(sci.getIndex());
	}
	
	/**
	  * Generate an XML-RPC request from a method name and a parameter vector.
	  */
	private static void writeRequest(XmlWriter writer, String method,
			Vector params) throws IOException
	{
		writer.startElement("methodCall");
		writer.startElement("methodName");
		writer.write(method);
		writer.endElement("methodName");
		writer.startElement("params");
		int l = params.size();
		for (int i = 0; i < l; i++)
		{
			writer.startElement("param");
			writer.writeObject(params.elementAt(i));
			writer.endElement("param");
		}
		writer.endElement("params");
		writer.endElement("methodCall");
	}
}

/**
 * A quick and dirty XML writer.  If you feed it a
 * <code>ByteArrayInputStream</code>, it may be necessary to call
 * <code>writer.flush()</code> before calling
 * <code>buffer.toByteArray()</code> to get the data written to
 * your byte buffer.
 *
 * <p>Borrowed from org.apache.xmlrpc.XmlRpc.java, where it was package private.
 */
class XmlWriter
	extends OutputStreamWriter
{
	protected static final String PROLOG_START =
		"<?xml version=\"1.0\" encoding=\"";
	protected static final String PROLOG_END = "\"?>";
	protected static final String CLOSING_TAG_START = "</";
	protected static final String SINGLE_TAG_END = "/>";
	protected static final String LESS_THAN_ENTITY = "&lt;";
	protected static final String GREATER_THAN_ENTITY = "&gt;";
	protected static final String AMPERSAND_ENTITY = "&amp;";

	public XmlWriter (OutputStream out)
		throws UnsupportedEncodingException, IOException
	{
		// The default encoding used for XML-RPC is ISO-8859-1.
		this (out, encoding);
	}

	public XmlWriter (OutputStream out, String enc)
		throws UnsupportedEncodingException, IOException
	{
		super(out, enc);

		// Add the XML prolog (which includes the encoding)
		write(PROLOG_START);
		write(encodings.getProperty(enc, enc));
		write(PROLOG_END);
	}

	/**
	 * Writes the XML representation of a supported Java object
	 * type.
	 *
	 * @param obj The <code>Object</code> to write.
	 */
	public void writeObject (Object obj)
		throws IOException
	{
		startElement ("value");
		if (obj == null)
		{
			throw new RuntimeException("null value not supported by XML-RPC");
		}
		else if (obj instanceof String)
		{
			chardata(obj.toString());
		}
		else if (obj instanceof Integer)
		{
			startElement("int");
			write(obj.toString());
			endElement("int");
		}
		else if (obj instanceof Boolean)
		{
			startElement("boolean");
			write(((Boolean) obj).booleanValue() ? "1" : "0");
			endElement("boolean");
		}
		else if (obj instanceof Double || obj instanceof Float)
		{
			startElement("double");
			write(obj.toString());
			endElement("double");
		}
		else if (obj instanceof Date)
		{
			startElement("dateTime.iso8601");
			Date d = (Date) obj;
			write(dateformat.format(d));
			endElement("dateTime.iso8601");
		}
		else if (obj instanceof byte[])
		{
			startElement("base64");
			// FIXME: Yucky! Find a better way!
			write(new String(Base64.encode((byte[]) obj)).toCharArray());
			endElement("base64");
		}
		else if (obj instanceof Vector)
		{
			startElement("array");
			startElement("data");
			Vector array = (Vector) obj;
			int size = array.size();
			for (int i = 0; i < size; i++)
			{
				writeObject(array.elementAt(i));
			}
			endElement("data");
			endElement("array");
		}
		else if (obj instanceof Hashtable)
		{
			startElement("struct");
			Hashtable struct = (Hashtable) obj;
			for (Enumeration e = struct.keys(); e.hasMoreElements(); )
			{
				String nextkey = (String) e.nextElement();
				Object nextval = struct.get(nextkey);
				startElement("member");
				startElement("name");
				write(nextkey);
				endElement("name");
				writeObject(nextval);
				endElement("member");
			}
			endElement("struct");
		}
		else
		{
			throw new RuntimeException("unsupported Java type: " +
										obj.getClass());
		}
		endElement("value");
	}

	protected void startElement (String elem)
		throws IOException
	{
		write('<');
		write(elem);
		write('>');
	}

	protected void endElement (String elem)
		throws IOException
	{
		write(CLOSING_TAG_START);
		write(elem);
		write('>');
	}

	protected void emptyElement (String elem)
		throws IOException
	{
		write('<');
		write(elem);
		write(SINGLE_TAG_END);
	}

	protected void chardata (String text)
		throws IOException
	{
		int l = text.length ();
		for (int i = 0; i < l; i++)
		{
			char c = text.charAt (i);
			switch (c)
			{
			case '<':
				write(LESS_THAN_ENTITY);
				break;
			case '>':
				write(GREATER_THAN_ENTITY);
				break;
			case '&':
				write(AMPERSAND_ENTITY);
				break;
			default:
				write(c);
			}
		}
	}
	
    /**
     * Java's name for the encoding we're using.
     */
    static String encoding = "ISO8859_1";

    /**
     * Mapping between Java encoding names and "real" names used in
     * XML prolog.
     */
    static Properties encodings = new Properties ();
    static
    {
        encodings.put ("UTF8", "UTF-8");
        encodings.put ("ISO8859_1", "ISO-8859-1");
    }

    /**
     * Thread-safe wrapper for the <code>DateFormat</code> object used
     * to format and parse date/time values.
     */
    static Formatter dateformat = new Formatter ();	
}

/**
 * Wraps a <code>DateFormat</code> instance to provide thread safety.
 *  Also borrowed from org.apache.xmlrpc.XmlRpc.java.
 */
class Formatter
{
    private DateFormat f;

    /**
     * Uses the <code>DateFormat</code> string
     * <code>yyyyMMdd'T'HH:mm:ss</code>.
     */
    public Formatter ()
    {
        f = new SimpleDateFormat ("yyyyMMdd'T'HH:mm:ss");
    }

    public synchronized String format (Date d)
    {
        return f.format (d);
    }

    public synchronized Date parse (String s)
        throws ParseException
    {
        return f.parse (s);
    }
}

/**
  * Parses the return response from the server.  Borrowed heavily from 
  *  org.apache.xmlrpc.XmlRpc.java, which was completely unusable because 
  *  it was package protected and married to HTTP specific code.
  */
class XMLRPCResponseProcessor
{
	/**
	  * Constructor.
	  *
	  * @throws Various exceptions if there is a problem locating the sax
	  *  driver.
	  */
	public XMLRPCResponseProcessor() throws Exception
	{
/*        if (parserClass == null)
        {
            // try to get the name of the SAX driver from the System properties
            String driver;
            try
            {
                driver = System.getProperty("sax.driver", DEFAULT_PARSER);
            }
            catch (SecurityException e)
            {
                // An unsigned applet may not access system properties.
                driver = DEFAULT_PARSER;
            }
            setDriver(driver);
        }
*/
		javax.xml.parsers.SAXParser sxp=
			javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();		
        XMLReader temp = null;
        try
        {
//            temp = (Parser) parserClass.newInstance ();
			temp=sxp.getXMLReader();
        }
        catch (NoSuchMethodError nsm)
        {
            throw new Exception ("Can't create Parser: " + parserClass);
        }
		parser=temp;
//        parser.setDocumentHandler (handler);
		parser.setContentHandler(handler);
        parser.setErrorHandler (handler);
		
		// Set debug flag:
		debug = Boolean.getBoolean(
				"com.rbnb.plugins.XMLRPCResponseProcessor.debug");		
	}		
	
	/**
	  * Process the response returned from a method call.
	  * <p>
	  * @return An Object, the type of which depends upon the method invoked.
	  * @throws ClassNotFoundException If the SAX driver cannot be found.
	  * @throws SAXException If there is an error processing the XML.
	  */
	public Object process(InputStream response) throws SAXException, 
		XMLRPCException, IOException
	{
        // reset values (XmlRpc objects are reusable)
		fault=false;
        errorLevel = NONE;
        errorMsg = null;
        values.removeAllElements();
		cdata.setLength (0);
        readCdata = false;
        currentValue = null;

        long now = System.currentTimeMillis ();

        if (debug)
            System.err.println("Beginning parsing XML input stream");
        parser.parse (new InputSource (response));
        if (debug)
            System.err.println ("Spent "+
                    (System.currentTimeMillis () - now) + " millis parsing");

		if (fault)
		{
			// generate an XMLRPCException
			XMLRPCException exception = null;
			Hashtable f =(Hashtable) result;
			String faultString =(String) f.get("faultString");
			int faultCode = 0;
			try {
			faultCode=Integer.parseInt(
					f.get("faultCode").toString());
			} catch (Exception e) { }
			throw new XMLRPCException(faultCode,
					faultString.trim());
		}					
					
		return result; 
	}
	private Object result;
	private final XMLReader parser;
	private void objectParsed(Object o) { result=o; }
	
    /**
     * Set the SAX Parser to be used. The argument can either be the
     * full class name or a user friendly shortcut if the parser is
     * known to this class. The parsers that can currently be set by
     * shortcut are listed in the main documentation page. If you are
     * using another parser please send me the name of the SAX driver
     * and I'll include it in a future release.  If setDriver() is
     * never called then the System property "sax.driver" is
     * consulted. If that is not defined the driver defaults to
     * OpenXML.
     */
    public static void setDriver (String driver)
            throws ClassNotFoundException
    {
        String parserClassName = null;
        try
        {
            parserClassName = (String) saxDrivers.get (driver);
            if (parserClassName == null)
            {
                // Identifier lookup failed, assuming we were provided
                // with the fully qualified class name.
                parserClassName = driver;
            }
            parserClass = Class.forName (parserClassName);
        }
        catch (ClassNotFoundException x)
        {
            throw new ClassNotFoundException ("SAX driver not found: "+
                    parserClassName);
        }		
    }
	
	
	
	private class SAXHandler //extends HandlerBase
		extends DefaultHandler
	{
		////////////////////////////////////////////////////////////////
		// methods called by XML parser
	
		/**
		 * Method called by SAX driver.
		 */
		public void characters (char ch[], int start, int length)
			throws SAXException
		{
			if (readCdata)
			{
				cdata.append (ch, start, length);
			}
		}
	
		/**
		  * Method called by SAX driver.
		 */
//		public void endElement (String name) throws SAXException
		public void endElement(
			String namespaceURI,
			String localName,
			String qName)
		{
			String name=qName;
			if (debug)
				System.err.println ("endElement: "+name);
	
			// finalize character data, if appropriate
			if (currentValue != null && readCdata)
			{
				currentValue.characterData (cdata.toString ());
				cdata.setLength (0);
				readCdata = false;
			}
	
			if ("value".equals (name))
			{
				// Only handle top level objects or objects contained in
				// arrays here.  For objects contained in structs, wait
				// for </member> (see code below).
				int depth = values.size ();
				if (depth < 2 || values.elementAt(depth - 2).hashCode() != STRUCT)
				{
					Value v = currentValue;
					values.pop ();
					if (depth < 2)
					{
						// This is a top-level object
						objectParsed (v.value);
						currentValue = null;
					}
					else
					{
						// Add object to sub-array; if current container
						// is a struct, add later (at </member>).
						currentValue = (Value) values.peek ();
						currentValue.endElement (v);
					}
				}
			}
	
			// Handle objects contained in structs.
			if ("member".equals (name))
			{
				Value v = currentValue;
				values.pop ();
				currentValue = (Value) values.peek ();
				currentValue.endElement (v);
			}
	
			else if ("methodName".equals (name))
			{
				methodName = cdata.toString ();
				cdata.setLength (0);
				readCdata = false;
			}
		}
	
	
		/**
		  * Method called by SAX driver.
		  */
//		public void startElement (String name,	AttributeList atts) throws SAXException
		public void startElement(
			String uri,
			String localName,
			String qName,
			Attributes attributes)
		{
			String name=qName;
			if (debug)
				System.err.println ("startElement: "+name);
	
            if ("fault".equals(name))
            {
                fault = true;
            }
            else if ("value".equals (name))
			{
				// System.err.println ("starting value");
				Value v = new Value ();
				values.push (v);
				currentValue = v;
				// cdata object is reused
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("methodName".equals (name))
			{
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("name".equals (name))
			{
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("string".equals (name))
			{
				// currentValue.setType (STRING);
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("i4".equals (name) || "int".equals (name))
			{
				currentValue.setType (INTEGER);
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("boolean".equals (name))
			{
				currentValue.setType (BOOLEAN);
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("double".equals (name))
			{
				currentValue.setType (DOUBLE);
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("dateTime.iso8601".equals (name))
			{
				currentValue.setType (DATE);
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("base64".equals (name))
			{
				currentValue.setType (BASE64);
				cdata.setLength(0);
				readCdata = true;
			}
			else if ("struct".equals (name))
			{
				currentValue.setType (STRUCT);
			}
			else if ("array".equals (name))
			{
				currentValue.setType (ARRAY);
			}
		}
	
		public void error (SAXParseException e) throws SAXException
		{
			System.err.println ("Error parsing XML: "+e);
			errorLevel = RECOVERABLE;
			errorMsg = e.toString ();
		}
	
		public void fatalError(SAXParseException e) throws SAXException
		{
			System.err.println ("Fatal error parsing XML: "+e);
			errorLevel = FATAL;
			errorMsg = e.toString ();
		}
	}

    /**
     * This represents a XML-RPC value parsed from the request.  Borrowed from 
	 *  XmlRpc.java.
     */
    class Value
    {
        int type;
        Object value;
        // the name to use for the next member of struct values
        String nextMemberName;

        Hashtable struct;
        Vector array;

        /**
         * Constructor.
         */
        public Value ()
        {
            this.type = STRING;
        }

        /**
          * Notification that a new child element has been parsed.
          */
        public void endElement (Value child)
        {
            switch (type)
            {
            case ARRAY:
                array.addElement (child.value);
                break;
            case STRUCT:
                struct.put (nextMemberName, child.value);
            }
        }

        /**
         * Set the type of this value. If it's a container, create the
         * corresponding java container.
         */
        public void setType (int type)
        {
            // System.err.println ("setting type to "+types[type]);
            this.type = type;
            switch (type)
            {
            case ARRAY:
                value = array = new Vector ();
                break;
            case STRUCT:
                value = struct = new Hashtable ();
                break;
            }
        }

        /**
         * Set the character data for the element and interpret it
         * according to the element type.
         */
        public void characterData (String cdata)
        {
            switch (type)
            {
                case INTEGER:
                    value = new Integer (cdata.trim ());
                    break;
                case BOOLEAN:
                    value = ("1".equals (cdata.trim ()) ?
                             Boolean.TRUE : Boolean.FALSE);
                    break;
                case DOUBLE:
                    value = new Double (cdata.trim ());
                    break;
                case DATE:
                    try
                    {
                        value = dateformat.parse (cdata.trim ());
                    }
                    catch (ParseException p)
                    {
                        throw new RuntimeException (p.getMessage ());
                    }
                    break;
                case BASE64:
                    value = Base64.decode (cdata.getBytes());
                    break;
                case STRING:
                    value = cdata;
                    break;
                case STRUCT:
                    // this is the name to use for the next member of this struct
                    nextMemberName = cdata;
                    break;
            }
        }

        /**
         * This is a performance hack to get the type of a value
         * without casting the Object.  It breaks the contract of
         * method hashCode, but it doesn't matter since Value objects
         * are never used as keys in Hashtables.
         */
        public int hashCode ()
        {
            return type;
        }

        public String toString ()
        {
            return (types[type] + " element " + value);
        }
    } // end class Value
	
	
	private final SAXHandler handler=new SAXHandler();
	
	/**
     * Used to collect character data (<code>CDATA</code>) of
     * parameter values.
     */
    final StringBuffer cdata = new StringBuffer (128);
    boolean readCdata;
    String methodName;
    
	// the stack we're parsing our values into.
    final Stack values=new Stack();
    Value currentValue;

    // Error level + message
    int errorLevel;
    String errorMsg;
	boolean debug=false;
	boolean fault=false;

	
    static final Formatter dateformat = new Formatter ();

    /**
     * The list of valid XML elements used for RPC.
     */
    final static String types[] =
    {
        "String",
        "Integer",
        "Boolean",
        "Double",
        "Date",
        "Base64",
        "Struct",
        "Array"
    };
	
    /**
     * The class name of SAX parser to use.
     */
    private static Class parserClass;
    private static Hashtable saxDrivers = new Hashtable (8);
    static
    {
        // A mapping of short identifiers to the fully qualified class
        // names of common SAX parsers.  If more mappings are added
        // here, increase the size of the saxDrivers Map used to store
        // them.
        saxDrivers.put ("xerces", "org.apache.xerces.parsers.SAXParser");
        saxDrivers.put ("xp", "com.jclark.xml.sax.Driver");
        saxDrivers.put ("ibm1", "com.ibm.xml.parser.SAXDriver");
        saxDrivers.put ("ibm2", "com.ibm.xml.parsers.SAXParser");
        saxDrivers.put ("aelfred", "com.microstar.xml.SAXDriver");
        saxDrivers.put ("oracle1", "oracle.xml.parser.XMLParser");
        saxDrivers.put ("oracle2", "oracle.xml.parser.v2.SAXParser");
        saxDrivers.put ("openxml", "org.openxml.parser.XMLSAXParser");
    }
	
	
    /**
     * The default parser to use (MinML).
     */
//    private static final String DEFAULT_PARSER = MinML.class.getName();
	

    // XML RPC parameter types used for dataMode
    static final int STRING = 0;
    static final int INTEGER = 1;
    static final int BOOLEAN = 2;
    static final int DOUBLE = 3;
    static final int DATE = 4;
    static final int BASE64 = 5;
    static final int STRUCT = 6;
    static final int ARRAY = 7;

    static final int NONE = 0;
    static final int RECOVERABLE = 1;
    static final int FATAL = 2;
	
} // end class XMLRPCResponseProcessor


	

