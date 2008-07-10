/*
	ResponseMon.java

	Copyright 2008 Creare Inc.

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at 
	
	http://www.apache.org/licenses/LICENSE-2.0 
	
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.

	2008/07/09  WHF  Created.
*/

package com.rbnb.web;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
//import java.util.List;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;


/**
  * An application to download resources from web sites at fixed intervals, and
  *  calculate the response times.  The results are stored the in an RBNB.
  */
public class ResponseMon
{
	ResponseMon(String confFile)
	{
		try {			
			new ConfigParser(confFile);
			if (resourceQueue.size() == 0) {
				System.err.println("ERROR: No source files specified.");
				return;
			}
			
			makeOutputSource();
			
			getAndTimeResources();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			outputSource.Detach();
		}
	}
	
//************************  Private Methods  ********************************//	
	private void makeOutputSource() throws SAPIException
	{
		outputSource.SetRingBuffer(1000, "append", 1000000);
		outputSource.OpenRBNBConnection(host, rbnbSource);
	}
	
	private void printHeader()
	{
		System.err.println(
				"Tag,Date,Header Response Time (sec),"
				+ "Total Response Time (sec),Response Code,File Size (bytes)"
		);
	}
		
	private void getAndTimeResources() throws SAPIException
	{		
		printHeader();
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
				"MM/dd/yyyy kk:mm:ss"
		);
		dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
		
		while (true) {
			
			long beginGetTime = System.currentTimeMillis();
			
			for (Iterator iter = resourceQueue.iterator(); iter.hasNext();) {
				Resource res = (Resource) iter.next();
				res.get();

				Date now = new Date();
				double nowSec = now.getTime() * 1e-3;
				
				String csvString = 
						  res.getTag() + ','
						+ dateFormat.format(now) + " GMT,"
						+ res.getHeaderTime() + ','
						+ res.getTotalTime() + ','
						+ res.getResponseCode() + ','
						+ res.getFileSize() + "\r\n";
						
				System.err.print(csvString);
				
				// Add results to the RBNB:
				cmap.Clear();
				cmap.PutTime(nowSec, 0.0);
				int idx = cmap.Add(res.getTag()+"/responseTime");
				cmap.PutDataAsFloat64(idx, new double[] { res.getHeaderTime() });
				idx = cmap.Add(res.getTag()+"/totalTime");
				cmap.PutDataAsFloat64(idx, new double[] { res.getTotalTime() });
				idx = cmap.Add(res.getTag()+"/responseCode");
				cmap.PutDataAsInt32(idx, new int[] { res.getResponseCode() });
				idx = cmap.Add(res.getTag()+"/_CSV");
				cmap.PutDataAsString(idx, csvString);
				cmap.PutMime(idx, "text/csv");
				
				outputSource.Flush(cmap);
			}
			
			long endGetTime = System.currentTimeMillis();
			
			long sleepTime = intervalMs - (endGetTime - beginGetTime);
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ie) {
					break;
				}
			}
		}		
	}
	
//****************************  Inner Classes  ******************************//
	private final class Resource
	{
		Resource(URL source, String tag)
		{
			this.source = source;
			this.tag = tag.replace(',', ';')
					.replace('/', '\\').replace('.', '_');
		}
		
		public boolean get()
		{
			boolean result = false;
			
			lastHeaderTime = lastTotalTime = -1.0;
			fileSize = 0;
			
			try {
				long start, end;
				
				start = System.currentTimeMillis();

				HttpURLConnection srcCon = (HttpURLConnection) 
						source.openConnection();
				
				// Set the read timeout, if the Java version supports it.
				if (urlConnectionReadTimeoutMethod != null) {
					try {
						Object[] args = { new Integer(resourceReadTimeout) }; 
						urlConnectionReadTimeoutMethod.invoke(
								srcCon,
								args
						);
					} catch (Throwable t) {}
				}
				
				// If authorization is available, set it:
				if (auth != null) {
					srcCon.setRequestProperty("Authorization", auth);
				}
				
					
				// This method connects to the server and obtains the response
				//  code.  The file is not downloaded.  A response >= 400 will
				//  throw an exception; others do not.
				lastResponseCode = srcCon.getResponseCode();
				
				end = System.currentTimeMillis();
				lastHeaderTime = (end - start) * 1e-3;

				// To get a realistic time value, download the file:				
				InputStream input = srcCon.getInputStream();				
				try {					
					while (true) {
						int wasRead = input.read(
								ba,
								0,
								ba.length
						);
						
						if (System.currentTimeMillis() - start 
								> resourceReadTimeout) 
							throw new java.net.SocketTimeoutException(); 
						
						if (wasRead <= 0) break;
						
						fileSize += wasRead; 
					}
				} finally {
					input.close();
				}
				
				end = System.currentTimeMillis();
				lastTotalTime = (end - start) * 1e-3;
				
				result = true;
			} catch (java.net.SocketTimeoutException ste) {
				lastResponseCode = 601;		
			} catch (java.net.UnknownHostException uhe) {
				lastResponseCode = 602;
			} catch (Exception e) {
				lastResponseCode = 600;
				if (debug) e.printStackTrace();
			}
				
			return result;
		}
		
		/**
		  * Valid if get() returned true.
		  */
		public int getResponseCode() { return lastResponseCode; }
		
		public String getTag() { return tag; }
		
		public double getHeaderTime() { return lastHeaderTime; }
		public double getTotalTime() { return lastTotalTime; }
		public int getFileSize() { return fileSize; }

		public void setAuthorization(String name, String pwd)
		{
			String authentication =
					com.rbnb.utility.Base64Encode
							.encode((name + ":"+ pwd).getBytes());
			auth = "Basic "+ authentication;
		}		

		private final URL source;
		private final String tag;
		private String auth;
		
		private int lastResponseCode, fileSize;
		private final byte[] ba = new byte[1024];
		private double lastHeaderTime, lastTotalTime;
	}

	private class ConfigParser
		extends org.xml.sax.helpers.DefaultHandler
	{
		ConfigParser(String fname) throws IOException, SAXException
		{
			parse(fname);
		}
		
		/**
		  * Parse the XML in the provided filename.
		  */
		private void parse(String fname) throws java.io.IOException,
				SAXException
		{
			InputStream stream;
			File file = new File(fname);
			
			if (!file.exists()) {
				// Try as a URL:
				URL url = new URL(fname);
				stream = url.openStream();
			} else
				stream = new java.io.FileInputStream(file); 
			
			parse(new org.xml.sax.InputSource(stream));
		}
		
		protected void parse(org.xml.sax.InputSource input)
			throws java.io.IOException, SAXException
		{
			if (xmlReader == null) {
				try {
					// Try to instantiate the system default parser:
					xmlReader
						= XMLReaderFactory.createXMLReader();
				} catch (Throwable t) {	
					// Failed, use the one we packaged:
					try {
						xmlReader = XMLReaderFactory.createXMLReader(
								"com.bluecast.xml.Piccolo"
						);
					} catch (Throwable t2) {
						// Try one we used to package:
						xmlReader = XMLReaderFactory.createXMLReader(
								"org.apache.xerces.parsers.SAXParser"
						);
					}
				}						
System.err.println("Using SAX parser: "+xmlReader.getClass().getName());
			}
			
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
		
			xmlReader.parse(input);
		}
		
		private void clear()
		{
			srcUrl = null;
			tag = null;
		}
		
		/**
		  * Create a resource object and add it to the queue.
		  */
		private void makeResource()
		{
			Resource res = new Resource(srcUrl, tag);
			if (user != null && password != null)
				res.setAuthorization(user, password);
			
			resourceQueue.add(res);
		}
			
		
//*********************  ContentHandler interface  ***************************//
		public void startDocument() throws SAXException
		{
			inConfig = inResource = false;	
		}
		
		public void startElement(String uri, String localName,
				String qName, Attributes attributes)
			throws SAXException
		{
			sbuffer.setLength(0);
			
			if ("responseMon".equals(qName)) {
				String temp;
				
				inConfig = true;
				
				// Process attributes:
				if ((temp = attributes.getValue("intervalMs")) != null) {
					try { intervalMs = Long.parseLong(temp); }
					catch (NumberFormatException nfe) {
						System.err.println(
							"WARNING: intervalMs attribute incorrect.");
					}
				}
				if ((temp = attributes.getValue("timeoutMs")) != null) {
					try { resourceReadTimeout = Integer.parseInt(temp); }
					catch (NumberFormatException nfe) {
						System.err.println(
							"WARNING: timeoutMs attribute incorrect.");
					}
				}
				if ((temp = attributes.getValue("rbnbSource")) != null)
					rbnbSource = temp;
				
				if ((temp = attributes.getValue("host")) != null)
					host = temp;
				debug = "true".equals(attributes.getValue("debug"));
			} else {
				if (!inConfig) throw new SAXException(
						"Root tag is not \"responseMon\";"
						+" not a ResponseMon config file."
					);
					
				if (!inResource) {
					if ("resource".equals(qName)) {
						clear();
						inResource = true;
					} else if ("url".equals(qName) || "tag".equals(qName)
							|| "user".equals(qName) 
							|| "password".equals(qName))
						throw new SAXException(
								qName + " must be a child of resource."
						);
				} else if ("resource".equals(qName)) {
					throw new SAXException(
							"resource can not be a child of itself."
					);
				}
			}
		}
		
		public void characters(char[] ch, int start, int length)
		{
			sbuffer.append(ch, start, length);
		}
		
		public void endElement(String uri, String localName, String qName)
			throws SAXException
		{
			if (inResource) {
				if ("url".equals(qName)) {
					try {
						srcUrl = new URL(sbuffer.toString());
					} catch (MalformedURLException mue) {
						System.err.println("WARNING: "+mue);
					}
				} else if ("tag".equals(qName)) {
					tag = sbuffer.toString();
				} else if ("user".equals(qName))
					user = sbuffer.toString();
				else if ("password".equals(qName))
					password = sbuffer.toString();
				else if ("resource".equals(qName)) {
					inResource = false;
					if (srcUrl == null || tag == null) {
						throw new SAXException(
								"ERROR: resource requires url & tag subtags.");
					}
					makeResource();
				}
			}
		}
		
		public void endDocument() throws SAXException
		{
		}			
		
		public void error(SAXParseException e)
		{ System.err.println("Parse error: "); e.printStackTrace(); }
		
		public void warning(SAXParseException e)
		{ System.err.println("Parse warning: "); e.printStackTrace(); }
		
		
	//***********************  ConfigParser Member Data  *********************//
		private URL srcUrl;
		private String tag, user, password;
		private boolean inConfig = false, inResource = false;
		private XMLReader xmlReader = null;
		private final StringBuffer sbuffer = new StringBuffer();		
	}

	
//***************************  Member Data  *********************************//	
	private final static ArrayList resourceQueue = new ArrayList();
	private final Source outputSource = new Source();
	private final ChannelMap cmap = new ChannelMap();
	private String host = "localhost:3333", rbnbSource = "ResponseMon";
	private long intervalMs = 10000;
	private int resourceReadTimeout = 10000;
	private boolean debug = false;
	

//*****************************  Statics  ***********************************//
	private static final java.lang.reflect.Method urlConnectionReadTimeoutMethod;
	
	static {
		java.lang.reflect.Method temp = null;
		try {
			Class[] args = { int.class }; 
			temp = URLConnection.class.getDeclaredMethod(
					"setReadTimeout",
					args
			);
		} catch (Throwable t) {
			System.err.println("Read Timeout not available.");
		}
		urlConnectionReadTimeoutMethod = temp;
	}	

	public static void main(String args[])
	{
		if (args.length != 1) {
			System.err.println("ResponseMon config-file-or-url");
			return;
		}
		
		new ResponseMon(args[0]);		
	}
}

