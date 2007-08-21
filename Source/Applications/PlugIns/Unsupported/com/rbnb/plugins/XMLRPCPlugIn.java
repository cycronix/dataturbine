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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import com.rbnb.sapi.*;

import com.rbnb.utility.ArgHandler;

import org.apache.xmlrpc.XmlRpcServer;

/**
  * Translates PlugIn requests into method calls to registered objects.
  *  &nbsp;This plugin listens for requests which contain XML-RPC method calls,
  *  as defined <a href="http://www.xmlrpc.com/">here</a>.&nbsp; The XML is 
  *  translated
  *  via a component from the Apache implementation, available 
  * <a href="http://xml.apache.org/xmlrpc/">here</a>.&nbsp; This XML-RPC method
  *  processor invokes the appropriate method on one of its registered objects,
  *  passing it the provided inputs.
  * <p>
  * @author WHF
  * @version 2.0B9
*/

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/17/2002  WHF	Created.
 * 01/08/2002  WHF  Added removeHandler.
*/
public class XMLRPCPlugIn extends SimplePlugIn
{
	public XMLRPCPlugIn()
	{
		setHandlerClass(XmlRpcHandler.class);
		Hashtable ht=new Hashtable();
		ht.put(XmlRpcHandler.XMLRPC_SERVER_INSTANCE, server);
		setHandlerOptions(ht);
	}
	
	/**
	  * Adds a handler to the XML-RPC method processor.
	  */
	public void addHandler(String handle, Object handler)
	{
		server.addHandler(handle, handler);		
	}
	
	/**
	  * Removes a handler from the XML-RPC method processor.
	  */
	public void removeHandler(String handle)
	{
		server.removeHandler(handle);	
	}
	
	/**
	  * Creates an instance of XMLRPCPlugIn, and registers classes per the
	  *  command-line interface.
	  */
	public static void main(String args[])
	{	
		ArgHandler ah=null;
		try {
		ah=new ArgHandler(args);
		} catch (Exception e) { showUsage(); return; }
		
		if (ah.checkFlag('?')||ah.checkFlag('h'))
		{
			showUsage();
			return;
		}
		
		XMLRPCPlugIn pi=new XMLRPCPlugIn();
		pi.setRBNBAddress(ah.getOption('a',pi.getRBNBAddress()));
		pi.setPlugInName(ah.getOption('n',XMLRPCPlugIn.class.getName()));
		try {
			String infoFile=ah.getOption('i', null);
			if (infoFile!=null) pi.setInfoFile(infoFile);
		} catch (java.io.IOException ie) {
			System.err.println("Error reading file \""+ah.getOption('i'));
			ie.printStackTrace();
			return;
		}
		
		String s=ah.getOption('c');
		if (s==null)
			pi.addHandler(String.class.getName(),"This is a test object.");
		else try {
			pi.addHandler(ah.getOption('i', s), Class.forName(s).newInstance());
		} catch (Exception e) { e.printStackTrace(); return; }
		
		pi.run();
	}
	
	private static void showUsage()
	{
		System.err.println(XMLRPCPlugIn.class.getName()
			+": Generic XML-RPC PlugIn.\nCopyright Creare, Inc. 2002"
			+"\nOptions:"
			+"\n\t-a host:port [localhost:3333]\t- RBNB server"
				+" to connect to"
			+"\n\t-n name ["+XMLRPCPlugIn.class.getName()
				+"]\t- client name for plugin"
			+"\n\t-i identifier [java.lang.String]\t- name for the object" 
			+" instance"
			+"\n\t-c class [java.lang.String]\t- arbitrary class with a "
			+"default constructor");
	}
	
	static class XmlRpcHandler implements SimplePlugIn.PlugInCallback
	{
		/**
		  * Passes generic options to the plugin handler.
		  */
		public void setOptions(Hashtable options)
		{
//System.err.println("XmlRpcHandler::setOptions()\n"+options);
			server=(XmlRpcServer) options.get(XMLRPC_SERVER_INSTANCE);				
		}
		
		/**
		  * Handles the request defined by the provided <code>PlugInChannelMap.
		  *  </code>
		  */
		public void processRequest(PlugInChannelMap picm) throws SAPIException
		{
//System.err.println("Got "+picm);
			if ("registration".equals(picm.GetRequestReference()))
			{
				picm.Clear();
			}
			else 
			{ // whatever the reference, execute the command:
				for (int ii=0; ii<picm.NumberOfChannels(); ++ii)
				{
//System.err.println(picm.GetName(ii));
					byte[] array=null;
					if (picm.GetType(ii)==picm.TYPE_BYTEARRAY) {
//System.err.println(new String(picm.GetDataAsByteArray(ii)[0]));	
						array=picm.GetDataAsByteArray(ii)[0];
					}
					else if (picm.GetType(ii)==picm.TYPE_STRING) {
						try {
							array=picm.GetDataAsString(ii)[0].getBytes("utf-8");
						} catch (java.io.UnsupportedEncodingException uee)
						{ uee.printStackTrace(); }
					}
					if (array!=null) {
						ByteArrayInputStream bais=
							new ByteArrayInputStream(array);
						array=server.execute(bais);
//System.err.println("Response: " + new String(array));
						picm.PutDataAsByteArray(ii, array); 
						picm.PutMime(ii,"text/xml");
					}						
				}
			}
		}
		
		/**
		  * Prepares this object for reuse.  Return false if the object should
		  *  be discarded.  The object will also be discarded if this method
		  *  throws a run-time exception.
		  */
		public boolean recycle()
		{
			return true;	
		}
		
		private XmlRpcServer server;
		private final static String XMLRPC_SERVER_INSTANCE=
			"com.rbnb.plugins.XMLRPCPlugIn.XMLRPCServer";
	} // end class XmlRpcHandler

	private final XmlRpcServer server=new XmlRpcServer();	
} // end XMLRPCPlugIn


