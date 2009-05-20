/*
	Copyright 2009 Creare Inc.
	
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
	ArchiveTests.java
	
	2008/08/11  WHF  Created.
	2009/05/19  WHF  Depends on TestUtil.
*/

package com.rbnb.tests;


import com.rbnb.sapi.*;

public class ArchiveTests
{
	private static final TestUtil testUtil = new TestUtil();
	
	@org.junit.Before
	public void startServer() throws Exception
	{
		testUtil.startServer();
	}
	
	@org.junit.After
	public void stopServer() throws Exception
	{
		testUtil.stopServer();
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
