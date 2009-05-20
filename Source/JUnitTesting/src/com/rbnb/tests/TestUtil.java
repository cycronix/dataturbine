/*
	TestUtil.java
	
	Utilities to support DataTurbine JUnit testing.
	
	2009/05/19  WHF  Created.
*/

package com.rbnb.tests;

import com.rbnb.api.Server;


class TestUtil
{
	public void startServer() throws Exception
	{
		if (server == null) {
			String args[] = {
				"-H", // set the 'archive home directory'
				System.getProperty("com.rbnb.tests.rbnbArchiveDir")
			};
			
			// Note the server is created in the started state:
			server = Server.launchNewServer(args);
		} 
	}
	
	public void startChildServer() throws Exception
	{
		if (childServer == null) {
			String args[] = {
				"-a",
				"localhost:4444",
				"-p",
				"localhost:3333",
				"-n",
				"ChildServer"
			};
			childServer = Server.launchNewServer(args);
		}
	}
	
	public void stopServer() throws Exception
	{
		if (server != null) {
			server.stop();
			server = null;
		}
	}
	
	public void stopChildServer() throws Exception
	{
		if (childServer != null) {
			childServer.stop();
			childServer = null;
		}
	}
	
	
	public Server getServer() { return server; }
	public Server getChildServer() { return childServer; }

	
	private Server server, childServer;
}
