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

/*
	PlugInTemplate.java

	2006/10/30  WHF  Created.
	2007/03/12  WHF  Supports frame based requests.
	2007/03/22  WHF  Changed the default for forwarding request data to 'true'.
			Improved handling of exceptions in the request slave.
	2007/04/03  WHF  Added getRequestOptions().
	2007/09/12  WHF  start() sends a notification in case someone needs it.
	2007/11/07  WHF  When VerifyConnection fails, close the connection before
		restarting it.
	2007/11/15  WHF  Added showHelper() and argHelper().
*/

package com.rbnb.plugins;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;

import com.rbnb.sapi.*;


/**
  * An abstract base class that simplifies PlugIn development.
  * <p>Most plug-in implementations will only need to overload the 
  * {@link #processRequest(ChannelMap, PlugInChannelMap ) } method in order to
  *  have a fully functional plug-in.
  * <p>Other overloadable functions:
  * <ul>
  *      <li>{@link #createForwardMap(PlugInChannelMap)} -- create the 
  *            ChannelMap used to forward requests for data to process, </li>
  *      <li>{@link #getForwardData(Sink, ChannelMap, PlugInChannelMap)}
  *            -- makes the forwarded request using the given sink and map.</li>
  *      <li>{@link #processRegistrationRequest(ChannelMap, PlugInChannelMap)}
  *            -- handles registration requests in case channel names or mime
  *               types need to be changed, or server XML meta-data needs to
  *               be generated.</li>
  * </ul>
  */
public abstract class PlugInTemplate
{
	/**
	  * Create a FilterPlugIn with default parameters.
	  */
	protected PlugInTemplate()
	{
		boolean temp=false;
		try {
			temp=Boolean.getBoolean("com.rbnb.plugins.PlugInTemplate.debug");
		} catch (SecurityException se) 
		{  } // not allowed, no debug.
		debugFlag=temp;
	}
	
	/**
	  * Create a FilterPlugIn specifying the RBNB host and the client name.
	  */
	protected PlugInTemplate(
			String host,
			String name
	)
	{
		this();
		
		this.host = host;
		this.name = name;
	}
	
	/**
	  * Starts the PlugIn in another thread, returning execution 
	  *  to the current thread.  Any threads waiting on <code>this</code>
	  *  are notified.
	  *
	  * @throws SAPIException  if a connection error occurs.
	  */
	public final void start() throws SAPIException
	{		
		if (slave.isRunning())
			throw new IllegalStateException("PlugIn already running.");
		
		pi.OpenRBNBConnection(host, name, user, pass);
		serverName = pi.GetServerName();

		/*if (channelToReg!=null)
		{
			ChannelMap cm=new ChannelMap();
			cm.Add(channelToReg);
			//cm.Add("info.txt");
			pi.Register(cm);
		} */
		if (registeredChannels.NumberOfChannels() != 0)
			pi.Register(registeredChannels);
		
		synchronized (this) {
			notifyAll();
		}
		
		new Thread(slave, getClass().getName()+" Slave").start();		
	}
	
	/**
	  * Stops the PlugIn thread.
	  * <p>Any threads waiting on this are notified.
	  */
	public final void stop()
	{
		slave.stop();
		synchronized (this) {
			notifyAll();
		}
	}
	
	/**
	  * Returns true if this PlugIn is currently answering requests.
	  */
	public final boolean isRunning() { return slave.isRunning(); }
	
//******************************  Accessors  ********************************//
	/**
	  * The RBNB hostname:port.  The default is "localhost:3333".
	  */
	public final String getHost() { return host; }
	public final void setHost(String host) { this.host = host; }

	/**
	  * The client name used for this PlugIn in the RBNB.  The default is
	  *  getClass().getName().
	  * @see Class#getName()
	  */
	public final String getName() { return name; }
	public final void setName(String name) { this.name = name; }

	/**
	  * Username used in connecting to the RBNB.  Default is null.
	  */
	public final String getUser() { return user; }
	public final void setUser(String user) { this.user = user; }

	/**
	  * Password used in connecting to the RBNB.  Default is null.
	  */
	public final String getPassword() { return pass; }
	public final void setPassword(String pass) { this.pass = pass; }

	/**
	  * Channel registered with the server.  By default this is null,
	  *  enabling dynamic registration.
	  */
	//public String getChannelToRegister() { return channelToReg; }
	//public void setChannelToRegister(String channelToReg) 
	//{ this.channelToReg = channelToReg; }
	
	/**
	  * Add a channel to be registered with the server for this PlugIn.
	  * If never called, no channels are registered.  This allows for 
	  *  dynamic registration.
	  *
	  * @throws IllegalArgumentException if the channel is invalid.
	  */
	public final void addChannelToRegister(String channel)
	{
		try {
			registeredChannels.Add(channel);
		} catch (SAPIException sapie) {
			// Requires 1.5!
			// throw new IllegalArgumentException(sapie);
			throw new IllegalArgumentException(sapie.getMessage());
		}
	}

	/**
	  * Timeout (in ms) used in calls to Fetch().  The default is -1
	  *   (forever).
	  * @see PlugIn#Fetch(long)
	  * @see Sink#Fetch(long)
	  */
	public final long getTimeout() { return timeout; }
	public final void setTimeout(long timeout) { this.timeout = timeout; }

	/**
	  * If true, one sink is used to get all forwarding data.  Otherwise,
	  *  one sink is used per thread created to handle an incoming request.
	  * <p>The default is false.
	  */
	public final boolean getUseOneSink() { return oneSink; }
	public final void setUseOneSink(boolean oneSink) { this.oneSink = oneSink; }
	
	/** 
	  * If true, incoming requests are automatically forwarded to the sink
	  *  to get matching data for processing.  This is the typical usage for
	  *  filter plug-ins that process data already in the RBNB to produce
	  *  new data.
	  *
	  * <p>Other plug-ins generate their own data internally, or want to handle
	  *  forwarding themselves; they should set this field to false.
	  *
	  * <p>The default is true.
	  */
	public final boolean getForwardRequests() { return forwardRequests; }
	public final void setForwardRequests(boolean forwardRequests)
	{ this.forwardRequests = forwardRequests; }
	
	/** 
	  * If true, the data from incoming requests is forwarded to the sink
	  *   to get matching data for processing.
	  *  Otherwise, the forwarded request contains only the channel names.
	  * <p>The default is true.
	  */
	public final boolean getForwardRequestData() { return forwardRequestData; }
	public final void setForwardRequestData(boolean forwardRequestData)
	{ this.forwardRequestData = forwardRequestData; }
	
	/**
	  * Returns the class whose instances are created to help answer requests.
	  *  The default is null.
	  * @see #setUserRequestClass(Class)
	  */
	public final Class getUserRequestClass() { return userClass; }
	/**
	  * Set the class used to help answer requests. 
	  *  One instance of this class will be created each time a thread is 
	  *  created to handle an incoming PlugIn request.  This instance can
	  *  be recovered from inside these threads using 
	  *  {@link #getUserRequestObject()}.
	  * <p>The class should have either a single argument constructor which
	  *   takes an instance of PlugInTemplate, or failing that 
	  *   a no-argument constructor. 
	  * <p>This object is typically used to store references to objects
	  *  which are expensive to create and non-reentrant, 
	  *  to give a performance improvement
	  *  over creating them each time 
	  *  {@link #processRequest(ChannelMap, PlugInChannelMap)} is called.
	  */
	public final void setUserRequestClass(Class userClass) 
	{ this.userClass = userClass; }
	
	/**
	  * If the user request class has been set, and we are inside a callback
	  *  such as {@link #processRequest(ChannelMap, PlugInChannelMap)},
	  *  then this method returns an 
	  *  instance of the user request class.
	  * @return The user request object instance of the current thread, or null.
	  */  
	public final Object getUserRequestObject()
	{
		/* Note that thread local objects were not used because values
			must be preserved across multiple threads. */
		
		if (userClass != null) {
			AnswerRequest ar = (AnswerRequest) thread2RequestMap.get(
					Thread.currentThread());
			if (ar != null) return ar.getUserInstance();
		}
		return null;
	}
	
	/**
	  * For sinks where the default request forwarding approach is 
	  *  inappropriate, this method may be used to get a handle to a
	  *  Sink to make requests.  These calls must be made inside one of the
	  *  callback methods of this class.
	  * <p><strong>NOTE:</strong> If the useOneSink property is true, 
	  *  you should synchronize on the Sink object returned by this method.
	  * @return The Sink instance for the current thread, 
	  * @throws SAPIException if the sink is new and needs to be connected,
	  *  but failed.
	  */
	public final Sink getRequestSink() throws SAPIException
	{
		AnswerRequest ar = (AnswerRequest) thread2RequestMap.get(
				Thread.currentThread());
		if (ar != null) return ar.getSink();
		return null;
	}
	
	/**
	  * Gives a Properties object, which represents the options set
	  *   by the Sink for this PlugIn when the request was made.  Note that
	  *   calls to this function must be made inside one of the callback
	  *   methods of this class.
	  */
	public final Properties getRequestOptions()
	{
		AnswerRequest ar = (AnswerRequest) thread2RequestMap.get(
				Thread.currentThread());
		if (ar != null) return ar.getRequestOptions();
		return null;		
	}
	
//*****************************  Overrides  *********************************//
	/**
	  * Callback to create a ChannelMap to be used to request data from the 
	  *  server to match an incoming request.
	  * <p>Override this method to provide an alternative mapping between 
	  *    channel names. <strong>It must be reentrant.</strong>
	  *    The default simply copies the names over, with some special 
	  *    processing for registration requests.
	  *
	  * <p>Developers overriding this method are encouraged to 
	  *  respect the value of the forwardRequestData field.
	  *
	  * <p>Note this method will never be called if 
	  * {@link #getForwardRequests()} is false.
	  */
	protected ChannelMap createForwardMap(PlugInChannelMap picm)
		throws SAPIException 
	{
		if (debugFlag) System.err.println("Creating forward map from: "+picm);
		
		ChannelMap cm = new ChannelMap();
		
		if ("registration".equals(picm.GetRequestReference())) {
			for (int ii = 0; ii < picm.NumberOfChannels(); ++ii) {
				String name = picm.GetName(ii);
				if ("*".equals(name)) {
					// Support the case where PlugIn/* has been requested.
					// Ordinarily we would forward this to the server as *,
					// which as of 2006/11/08 doesn't work.  So we replace
					//  it with [servername]/*, which does.
					// serverName variable does not end in a slash:
					//cm.Add(serverName+"/*");
					
					// 2006/11/08  WHF  The above doesn't actually work.  
					//  There are two problems.  One, the Rmap returned
					// by Server/* contains no data, so produces no channels.
					// Second, all the channels are children of Server.
					//  You would have to build a channel tree and place
					//  the first layer of nodes.
					// It is easier to give too much information:
					cm.Add("...");
				} else {
				    addChannel(picm, ii, cm);
				}
			}
		} else {
			// Other kind of request:
			for (int ii = 0; ii < picm.NumberOfChannels(); ++ii) {
				if (picm.GetName(ii).length() > 0)
					addChannel(picm, ii, cm);
			}
		}
		return cm;
	}
	
	/**
	  * Override this callback to perform the plug-in processing.
	  * <p>
	  * @param fwdData  Matching data from the server, if forwardRequests
	  *    is true; otherwise an empty ChannelMap which may be used as desired.
	  * @param out  PlugIn output.
	  *
	  * @see #setForwardRequests(boolean)
	  * @see #getUserRequestClass()
	  * @see #getRequestSink()
	  */
	protected abstract void processRequest(
			ChannelMap fwdData,
			PlugInChannelMap out) throws SAPIException;
	
	/**
	  * Callback to handle requests for dynamic registration.
	  *  The default simply forwards the request from the filtered channel;
	  *  override to provide other functionality.
	  */
	protected void processRegistrationRequest(
			ChannelMap fwdReg,
			PlugInChannelMap out
	) throws SAPIException
	{
		out.Clear();
		for (int ii = 0; ii < fwdReg.NumberOfChannels(); ++ii) {
			out.Add(fwdReg.GetName(ii));
			out.PutTimeRef(fwdReg, ii);
			out.PutDataRef(ii, fwdReg, ii);
		}
	}
	
	/**
	  * Callback to make the request to the server for matching data.  Override
	  *  if you wish to alter the way the request is made.
	  * <p>Note that this method is not called if the 
	  *  {@link #getForwardRequests() } property is false.
	  * @return either the mappedChannels map, or a new map; filled with data.
	  */
	protected ChannelMap getForwardData(
			Sink sink,
			ChannelMap mappedChannels,
			PlugInChannelMap picm)
		throws SAPIException
	{
		if (debugFlag)
			System.err.println("Forwarding "+mappedChannels
					+(picm.IsRequestFrames()?"\n(frames)":""));		
		
		if (picm.IsRequestFrames()) {
			sink.RequestFrame(mappedChannels);
		} else {
			sink.Request(
					mappedChannels,
					picm.GetRequestStart(),
					picm.GetRequestDuration(),
					picm.GetRequestReference(),
					picm.GetRequestOptions()
			);
		}
		sink.Fetch(timeout, mappedChannels);
		
		return mappedChannels;
	}	
	
//***************************  Private  Methods  ****************************//
	private void addChannel(ChannelMap src, int srcIndex, ChannelMap dest)
		throws SAPIException
	{
		int destIndex = dest.Add(src.GetName(srcIndex));
		if (forwardRequestData) {
			// 2007/03/23  WHF  Copying the time breaks server requests.
			//dest.PutTimeRef(src, destIndex);
			dest.PutDataRef(destIndex, src, srcIndex);
		}
	}
	
//*******************************  Inner Classes  ***************************//
	/**
	  * Waits for incoming requests and parsels them off to AnswerRequest 
	  *  objects.
	  */
	private class Slave implements Runnable
	{
		public final void run()
		{
			running = true;
			myThread = Thread.currentThread();
			
			try {
				PlugInChannelMap map = new PlugInChannelMap();
				
				while (running) {
					pi.Fetch(timeout, map);
					if (!map.GetIfFetchTimedOut()) {
						AnswerRequest a;
						if (threadStack.empty())
							a = new AnswerRequest();
						else
							a = (AnswerRequest) threadStack.pop();
						a.answerRequest(map);
						map=new PlugInChannelMap(); // use new map
					} else map.Clear();
				}
			} catch (SAPIException se) { se.printStackTrace(); }
			finally { 
				while (!threadStack.empty())
					((AnswerRequest) threadStack.pop()).close();
				pi.CloseRBNBConnection();
				running = false;
				myThread = null;
			}
		}
		
		public void stop() 
		{
			Thread toJoin = myThread;
			if (running) {
				running = false;
				try {
					toJoin.join();
				} catch (InterruptedException ie) {}
			}
		}
		
		public boolean isRunning() { return running; }
		
		private volatile boolean running;
		private Thread myThread;
	}
		
	/**
	  * Instances of this class are created to handle incoming PlugIn requests.
	  */
	private class AnswerRequest implements Runnable
	{
		public AnswerRequest() 
		{
//System.err.println("new answer request");
			Object temporary = null;
			if (userClass != null) {
				try {
					Object[] args = { PlugInTemplate.this };
					temporary = userClass.getConstructor(
							userConstructorParameterTypes).newInstance(args);
					// Single argument failed, try no-argument:
				} catch (Exception e) {
					try {
						temporary = userClass.newInstance();
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
			}
			userInstance = temporary;
		}
		
		final void answerRequest(PlugInChannelMap requestMap)
		{ 
			this.requestMap=requestMap;
			parseOptions(requestMap);
			(thread = new Thread(
					this,
					PlugInTemplate.this.getClass().getName()
							+" AnswerRequest"
			)).start();
		}
		
		public final void run()
		{
			try {
				try {
					ChannelMap map;
					
					thread2RequestMap.put(thread, this);
					
					if (forwardRequests) {
						map = createForwardMap(requestMap);
					
						if (oneSink) {
							synchronized (globalSink) {
								map = getForwardData(
										globalSink,
										map,
										requestMap
								);
							}
						} else {
							Sink sink = connect();
							map = getForwardData(sink, map, requestMap);
						}
					} else map = new ChannelMap();
			
					if (!map.GetIfFetchTimedOut()) {
						if ("registration".equals(
								requestMap.GetRequestReference()))
							processRegistrationRequest(map, requestMap);
						else processRequest(map, requestMap);				
					}	
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					// 2007/03/22  WHF  Moved Flush() into a separate try block.
					//  Even if the above code throws, we want to give
					//  something back to the server so the client doesn't 
					//  hang.
					pi.Flush(requestMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				thread2RequestMap.remove(thread);
			}
			// Request done, push us back onto the stack:
			threadStack.push(this);
		}
		
		/**
		  * Lazily initializes the sink object, and ensures it is connected.
		  *  Note that this method is called only if:
		  *  <ol><li>The useOneSink property is false, and </li>
		  *      <li>either forwardRequests is true or getRequestSink() is
		  *            called.</li>
		  *  </ol>
		  */
		private Sink connect() throws SAPIException
		{
			if (perRequestSink == null) {
//System.err.println("new sink");							
				perRequestSink = new Sink();
			}
			if (!perRequestSink.VerifyConnection()) {
//System.err.println("verify failed");
				// 2007/11/07  WHF  If connection is bad, terminate before
				//   restarting:
				perRequestSink.CloseRBNBConnection();
				perRequestSink.OpenRBNBConnection(
						host,
						name+"Sink",
						user,
						pass
				);
			}
				
			return perRequestSink;
		}
		
		private void parseOptions(PlugInChannelMap picm)
		{
			requestOptions.clear();
			int index = picm.GetIndex(""); // appears as nameless
			if (index == -1) return; // no parameters
			String[] data = picm.GetDataAsString(index);
			for (int ii = 0; ii < data.length; ++ii) {
				try {
					requestOptions.load(new java.io.ByteArrayInputStream(
							data[ii].getBytes()));
				} catch (Throwable t) {
					System.err.print("WARNING: Option could not be parsed: \"");
					System.err.print(data[ii]);
					System.err.println("\"");
					t.printStackTrace();
				}
			}
			if (debugFlag) {
				System.err.println(PlugInTemplate.this.getClass().getName()
						+" request options: ");
				System.err.println(requestOptions);
			}
		}
		final void close()
		{ if (perRequestSink != null) perRequestSink.CloseRBNBConnection(); } 
		
		final Object getUserInstance() { return userInstance; }
		/**
		  * @return Either the globalSink, if one sink is used, or a
		  *   connected perRequestSink.
		  * @throws SAPIException if the sink is new and needs to be connected,
		  *  but failed.
		  */
		final Sink getSink() throws SAPIException
		{ return oneSink ? globalSink : connect(); }
		
		final Properties getRequestOptions() { return requestOptions; }
		
		///////// AnswerRequest Private Data /////////////
		private PlugInChannelMap requestMap;
		/**
		  * Lazily initialized when needed.
		  */
		private Sink perRequestSink;
		private Thread thread;
		private final Object userInstance;
		private final Properties requestOptions = new Properties();
	} // end class AnswerRequest
	
//**************************  Static Methods  *******************************//
	/**
	  * A helper routine which displays the options supported by 
	  *  argHelper.  The format is a tab, followed by the option and its
	  *  format, padded to 24 characters; followed by a description, then 
	  *  newline.
	  */
	protected static void showHelper()
	{
		System.err.println(
				 "\t-a address:port         RBNB address (localhost:3333)\n"
				+"\t-n name                 Client name (class name)\n"
		);
	}
	
	/**
	  * Helper function to parse PlugInTemplate specific command-line options.
	  *
	  * @return the remaining arguments.
	  * @throws IllegalArgumentException  if the arguments supported do not
	  *   follow the conventions specified in showHelper().
	  */
	protected static String[] argHelper(String[] args, PlugInTemplate pit)
	{
		boolean[] used = new boolean[args.length];
		int usedCount = 0;
		
		for (int ii = 0; ii < args.length; ++ii) {
			if ("-a".equals(args[ii])) {
				used[ii] = true;
				pit.setHost(args[++ii]);
				used[ii] = true;
				usedCount += 2;
			} else if ("-n".equals(args[ii])) {
				used[ii] = true;
				pit.setName(args[++ii]);
				used[ii] = true;
				usedCount += 2;
			}
		}
		
		String[] res = new String[args.length - usedCount];
		int iii = 0;
		for (int ii = 0; ii < args.length; ++ii) {
			if (!used[ii]) res[iii++] = args[ii];
		}
		return res;
	}
	
//******************************  Data  *************************************//	
	private static final Class[] userConstructorParameterTypes = {
		PlugInTemplate.class
	};

	private String host = "localhost:3333",
			name = getClass().getName(),
			user, pass, channelToReg,
			serverName;
	private long timeout = -1;
	private boolean oneSink = false, forwardRequests = true, 
			forwardRequestData = true;
	private Class userClass;
	
	private final boolean debugFlag;
	
	private final PlugIn pi = new PlugIn();
	private final Slave slave = new Slave();
	private final Sink globalSink = new Sink();
	private final ChannelMap registeredChannels = new ChannelMap();
	/**
	  * Stores a mapping between answer request threads and user data objects.
	  *  A Hashtable is used because the methods are synchronized.
	  */
	private final Hashtable thread2RequestMap = new Hashtable();
	
	/**
	  * Has synchronized methods.
	  */
	private final Stack threadStack = new Stack();	
}

