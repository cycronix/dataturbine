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

import java.io.IOException;
import java.io.File;
import java.io.FileReader;

import java.util.Hashtable;
import java.util.Stack;

import com.rbnb.sapi.*;

import com.rbnb.utility.ArgHandler;

/**
  * Simple call-back style PlugIn client.&nbsp; The SimplePlugIn class 
  *  simplifies the PlugIn programmers task by encapsulating the multi-threading
  *  required for a robust PlugIn implementation.
  * <p>A class which implements the 
  *  <code>com.rbnb.plugins.SimplePlugIn.PlugInCallback</code> interface must be
  *  provided to a running instance of the SimplePlugIn.&nbsp; When a request is
  *  received, the plugin instantiates a new copy of the provided class (or 
  *  reuses an existing copy), and calls the <code>handleRequest</code> method.
  *  
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
*/

public class SimplePlugIn implements Runnable
{
// ************************* Inner classes *******************************//
	/**
	  *  Interface which must be implemented by simple plug-in implementers. 
	  */	  
	public static interface PlugInCallback
	{
		/**
		  * Passes generic options to the plugin handler.
		  */
		public void setOptions(Hashtable options);
		
		/**
		  * Handles the request defined by the provided <code>PlugInChannelMap.
		  *  </code>
		  */
		public void processRequest(PlugInChannelMap picm) throws SAPIException;
		
		/**
		  * Prepares this object for reuse.  Return false if the object should
		  *  be discarded.  The object will also be discarded if this method
		  *  throws a run-time exception.
		  */
		public boolean recycle();
	}
	
	private class AnswerRequest implements Runnable
	{
		public void setRequestMap(PlugInChannelMap requestMap)
		{ this.requestMap=requestMap; }

		public void run()
		{
			try {
			if (requestHandler==null)
			{
				requestHandler=(PlugInCallback) handlerClass.newInstance();
				requestHandler.setOptions(handlerOptions); 
			}
			requestHandler.processRequest(requestMap);
			int ii=requestMap.GetIndex("index.txt");
			if (ii>=0) requestMap.PutDataAsString(ii, infoText);
			pi.Flush(requestMap, false);
			if (!requestHandler.recycle()) requestHandler=null;
			} catch (Exception e) 
			{
				requestHandler=null;
				e.printStackTrace(); 
			}
			// Request done, push us back onto the stack:
			threadStack.push(this);
		}
		
		///////// AnswerRequest Private Data /////////////
		private PlugInChannelMap requestMap;
		private PlugInCallback requestHandler;
	} // end class AnswerRequest
	
// ************************* Construction *******************************//
	public SimplePlugIn() {}	
	
// ********************** Runnable Interface ****************************//
	public final void run()
	{
		running=true;
		// Adds some options needed by many plugins for their sinks:
		handlerOptions.put("hostname", getRBNBAddress());
		handlerOptions.put("RBNB", getRBNBAddress());
		handlerOptions.put("sinkname", getPlugInName()+".sink");
		handlerOptions.put("user", "");
		handlerOptions.put("password", "");
		
		try {
		pi.OpenRBNBConnection(host,name);
		if (channelToReg!=null)
		{
			ChannelMap cm=new ChannelMap();
			cm.Add(channelToReg);
			cm.Add("info.txt");
			pi.Register(cm);
		}
		
		while (true)
		{
			PlugInChannelMap map=pi.Fetch(-1);
			if (!map.GetIfFetchTimedOut())
			{
				AnswerRequest a;
				if (threadStack.empty())
					a=new AnswerRequest();
				else
					a=(AnswerRequest) threadStack.pop();
				a.setRequestMap(map);
				new Thread(a).start();
				map=new PlugInChannelMap(); // use new map
			}
		}
		} catch (SAPIException se) { se.printStackTrace(); }
		finally { pi.CloseRBNBConnection(); running=false; }
	}

// ************************** Accessors *******************************//
	/**
	  * The address of the RBNB to which this PlugIn, and any Sinks necessary 
	  *  to satisfy requests on this PlugIn, should connect.
	  */
	public String getRBNBAddress() { return host; }
	
	/**
	  * Set the address used by the PlugIn and any local sinks.
	  *
	  * <p>
	  * @throws IllegalStateException Calling this method after 
	  *  <code>run()</code>.
	  */
	public void setRBNBAddress(String host) { this.host=host; }
		
	public String getPlugInName() { return name; }
	public void setPlugInName(String name) { this.name=name; }
	/**
	  * Sets the information text property from a source file.
	  *  Only the first 10k of the file are read.
	  * @see #setInfoText
	  */
	public void setInfoFile(String infoFile) throws IOException
	{
		File file=new File(infoFile);
		FileReader fr=new FileReader(file);
		char[] data=new char[(int) Math.min(10240, file.length())];
		fr.read(data);
		setInfoText(new String(data));
	}
	
	/** 
	  * Sets the information String.
	  *  This data is placed in the "info.txt" channel, which will only be
	  *  registered with the server 
	  *  if {@link #setRegisteredChannel(String)} is called.
	  */
	public void setInfoText(String infoText) { this.infoText=infoText; }
	/** 
	  * Returns the information String.
	  *  This data is available in the "info.txt" channel, which will only be
	  *  registered with the server 
	  *  if {@link #setRegisteredChannel(String)} is called.
	  */
	public String getInfoText() { return infoText; }
	
	/**
	  * Returns the class used to handle incoming requests.
	  */
	public Class getHandlerClass() { return handlerClass; }

	/**
	  * Sets the class, instances of which will handle incoming requests.
	  *  The class must be an instance of <code>PlugInCallback</code>, or
	  *  a ClassCastException will be thrown.
	  * <p>
	  * @throws ClassCastException If the <code>handlerClass</code> argument
	  *   is not an instance of <code>PlugInCallback</code>.
	  */
	public void setHandlerClass(Class handlerClass) 
	{ 
		this.handlerClass=handlerClass; 
		if (!PlugInCallback.class.isAssignableFrom(handlerClass))
		{
			this.handlerClass=defaultCallback.getClass();
			throw new ClassCastException("The specified class, \""+
				handlerClass.getName()+"\", is not an instance of \""
				+PlugInCallback.class.getName()+"\".");
		}
	}
	
	/**
	  * Gets the channel registered with the server.
	  */
	public final String getRegisteredChannel()
	{ return channelToReg; }
	
	/** 
	  * Sets the channel which is registered in the server.
	  */
	public final void setRegisteredChannel(String channelToReg)
	{
		this.channelToReg=channelToReg;
	}
	
	/**
	  * Returns the options passed to new instances of the handler class.
	  */
	public final Hashtable getHandlerOptions() { return handlerOptions; }
	/**
	  * Passes generic options to the plugin handler.  Calling this after
	  *  run() is undefined.
	  */
	public final void setHandlerOptions(Hashtable handlerOptions)
	{ this.handlerOptions.putAll(handlerOptions); }
	 
// *********************** Static Methods *******************************//
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

		SimplePlugIn pi=new SimplePlugIn();
		pi.setRBNBAddress(ah.getOption('a',pi.host));
		pi.setPlugInName(ah.getOption('n',pi.name));
		pi.setRegisteredChannel(ah.getOption('r',null));
		String s=ah.getOption('c');
		try {
		if (s!=null)
			pi.setHandlerClass(Class.forName(s));

		pi.run();
		} catch (ClassNotFoundException cnfe) 
		{ 
			System.err.println("The specified handler class, \""+s
				+"\", cannot be found in the classpath."); 
		}
		catch (ClassCastException cce)
		{
			cce.printStackTrace();
		}
	}
	
	private static void showUsage()
	{
		System.err.println(SimplePlugIn.class.getName()
			+": Simple callback plug-in mechanism.\nCopyright Creare, Inc. 2002"
			+"\nOptions:"
			+"\n\t-a host:port [localhost:3333]\t- RBNB server"
				+" to connect to"
			+"\n\t-n name ["+SimplePlugIn.class.getName()
				+"]\t- client name for plugin"
			+"\n\t-c class [a null handler]\t- class, which must implement"
			+" com.rbnb.plugins.SimplePlugIn.PlugInCallback"
			+"\n\t-r channel [none]\t- channel to register with the server");
	}
	
// ********************** Private Singleton Data ***************************//
	private final static PlugInCallback defaultCallback=new PlugInCallback() {
		public void setOptions(Hashtable ht) { }
		public void processRequest(PlugInChannelMap picm) throws SAPIException
		{ picm.Clear(); } // just hand the request back, sans data.
		
		public boolean recycle() { return true; } // can recycle
	};
		
		
// ************************ Private Instance Data ***************************//
	private Class handlerClass=defaultCallback.getClass();
	private String host="localhost:3333", name=this.getClass().getName(),
		channelToReg=null, infoText="SimplePlugIn implementation.";
	private final PlugIn pi=new PlugIn();
	private final Stack threadStack=new Stack();
	private final Hashtable handlerOptions=new Hashtable();
	private volatile boolean running=false;
} // end class SimplePlugIn




