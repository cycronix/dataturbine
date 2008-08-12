/*
	ArchiveTests.java
	
	2008/08/11  WHF  Created.
*/

package com.rbnb.tests;

import com.rbnb.api.Server;
import com.rbnb.sapi.*;

public class ArchiveTests
{
	private static Server server;
	
	@org.junit.Before
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
	
	@org.junit.After
	public void stopServer() throws Exception
	{
		if (server != null)
			server.stop();
	}
	
	@org.junit.Test
	public void testArchiveFailSafe() throws Exception
	{
		// We go through this loop twice.  The first time we create a new
		//  archive, then close it, and damage it by deleting a key file.
		//  The second time we try to load the file.  It should fail, move the
		//  old archive to a new name, and then make a new archive.
		for (int ii = 0; ii < 2; ++ii) {
			Source src = new Source(100, "append", 10000);
			src.OpenRBNBConnection("localhost", "testArchiveFailSafe");
			ChannelMap cmap = new ChannelMap();
			cmap.PutDataAsString(cmap.Add("string"), "Testing");
			src.Flush(cmap);
			src.CloseRBNBConnection();
			
			// Damage the archive:
			new java.io.File(System.getProperty("com.rbnb.tests.rbnbArchiveDir")
					+"/testArchiveFailSafe/reghdr.rbn").delete();
		}
		
		org.junit.Assert.assertTrue(true);
	}	
}
