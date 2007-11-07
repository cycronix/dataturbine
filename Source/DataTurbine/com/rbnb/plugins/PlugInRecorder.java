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
	PlugInRecorder.java

	2007/11/06  WHF  Created.
*/

package com.rbnb.plugins;

import com.rbnb.sapi.*;

/**
  * Makes requests of a plug-in at a fixed interval.  Records the results
  *  in a channel on the server.  The requests are spaced so as to be gap 
  *  free, without overlap.
  */
public class PlugInRecorder
{
	public PlugInRecorder() {}
	
	/**
	  * A convenience constructor which sets the input and output channels.
	  *
	  * @see #setInputChannel(String)
	  * @see #setOutputChannel(String)
	  */
	public PlugInRecorder(String in, String out)
	{
		setInputChannel(in);
		setOutputChannel(out);
	}

//********************************  Accessors  *******************************//
	/**
	  * Returns the length of the archive to use for recording.
	  */
	public int getArchiveSize() { return archiveSize; }
	/**
	  * Set the length of the archive to use for recording.
	  *  Note the archive is always in "append" mode.
	  *  The default is 100,000 frames.
	  */
	public void setArchiveSize(int archiveSize)
	{ this.archiveSize = archiveSize; }
	
	/**
	  * The RBNB hostname:port.  The default is "localhost:3333".
	  */
	public final String getHost() { return host; }
	public final void setHost(String host) { this.host = host; }

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
	  * Returns the interval between requests, in milliseconds.  This also 
	  *  becomes the minimum duration.
	  */
	public int getInterval() { return interval_ms; }
	/**
	  * Set the interval between requests, in milliseconds.  The default is
	  *   10000 ms.
	  */
	public void setInterval(int interval_ms)
	{ this.interval_ms = interval_ms; }

	/**
	  * Returns the channel to record.
	  */
	public String getInputChannel() { return inputChannel; }
	/**
	  * Set the channel to record.
	  */
	public final void setInputChannel(String inputChannel) 
	{ this.inputChannel = inputChannel; }

	/**
	  * Get the recording destination channel.
	  */
	public String getOutputChannel() { return outputChannel; }
	/**
	  * Set the destination of recording.  It should be of the form:
	  *  <p><center>source/folder_1/folder_2/.../folder_n/file</center>
	  * <p> where the folders are optional.
	  * @throws IllegalArgumentException If the channel is not the correct form.
	  */
	public final void setOutputChannel(String outputChannel) 
	{
		if (outputChannel.indexOf('/') == -1)
			throw new IllegalArgumentException("Output channel should be of the"
		+" form \"source/folder_1/folder_2/.../folder_n/file\".");
		this.outputChannel = outputChannel;
	}
	
	/**
	  * Returns the starting point for recording.  One of "newest" or "oldest".
	  */
	public String getRequestStart() { return requestStart; }
	/**
	  * Sets the start of the recording.  One of:
	  * <ul><li>newest: record new output as it becomes available</li>
	  *     <li>oldest: record starting with the oldest available data</li>
	  * </ul>
	  * <p>The default is newest.
	  */
	public void setRequestStart(String requestStart)
	{ this.requestStart = requestStart; }
	
	/**
	  * Returns true if the recorder is running.
	  */
	public final boolean isRunning() { return isRunning; }
	
	/**
	  * If the recording thread stops because of an exception, it can be
	  *  retrieved using this method.  Otherwise returns null.
	  */
	public Throwable getRecorderException() { return recorderException; }
	
//******************************  Public Methods  ***************************//
	/**
	  * Begins recording.
	  */
	public void start()
	{
		if (isRunning) throw new IllegalStateException("Already running.");
		if (inputChannel == null)
			throw new IllegalStateException("Input channel not set.");
		if (outputChannel == null)
			throw new IllegalStateException("Output channel not set.");
		
		recorderException = null;
		stopThread = false;
		recordingThread = new Thread(recorderSlave);
		recordingThread.start();
	}
	
	/**
	  * Ends recording.
	  */
	public void stop()
	{
		if (!isRunning) throw new IllegalStateException("Not running.");
		stopThread = true;
		while (isRunning) {
			try {
				recordingThread.join();
			} catch (InterruptedException ie) {}
		}
	}
	
//****************************  Inner Classes  ******************************//
	private final Runnable recorderSlave = new Runnable() {
		public void run()
		{
			isRunning = true;
			try {
				initialRequest();
				
				while (!stopThread)
					fetchAndFlush();
				
			} catch (Throwable t) {
				t.printStackTrace();
				recorderException = t;
			} finally {
				shutdown();
				isRunning = false;
			}
		}
	};
	
//*****************************  Private Methods  ***************************//
	private void initialRequest() throws SAPIException
	{
		String name = "PlugInRecorder_"+inputChannel.replace('/','.');
		sink.OpenRBNBConnection(
				host,
				name,
				user,
				pass
		);
		
		outMap.Clear();
		int index = outputChannel.indexOf('/');
		name = outputChannel.substring(0, index);
		source.SetRingBuffer(1000, "append", archiveSize);
		source.OpenRBNBConnection(
				host,
				name,
				user,
				pass
		);
		outMap.Add(outputChannel.substring(index+1));
		source.Register(outMap);
		
		inMap.Clear();
		inMap.Add(inputChannel);
		// Time based subscription:
		sink.Subscribe(
				inMap,
				0.0,
				interval_ms * 1e-3,
				requestStart
		);
	}
	
	private void fetchAndFlush() throws SAPIException
	{
		sink.Fetch(timeout_ms, inMap);
		if (inMap.NumberOfChannels() != 0) {
			outMap.PutTimeRef(inMap, 0);
			outMap.PutDataRef(0, inMap, 0);
			source.Flush(outMap);
		}		
	}
	
	/**
	  * Does not throw.
	  */
	private void shutdown()
	{
		try {
			sink.CloseRBNBConnection();
			source.Detach();
		} catch (Throwable t) {}
	}

//*****************************  Data Members  ******************************//
	private String
		inputChannel,
		outputChannel,
		requestStart = "newest",
		host = "localhost:3333",
		user,
		pass;
	
	/**
	  * The number of milliseconds to wait between successive requests.
	  */
	private int interval_ms = 10000;
	private int archiveSize = 100000, timeout_ms = 2000;
	
	private volatile boolean stopThread = false, isRunning = false;
	private Thread recordingThread;
	private final Sink sink = new Sink();
	private final Source source = new Source();
	private final ChannelMap
		inMap = new ChannelMap(),
		outMap = new ChannelMap();
	private Throwable recorderException;
}

