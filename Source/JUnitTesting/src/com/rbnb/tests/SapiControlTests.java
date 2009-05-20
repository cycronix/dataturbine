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
	SapiControlTests.java
	
	Unit to test the Control class of the Simple API. 
	
	2009/05/19  WHF  Created.
*/

package com.rbnb.tests;


import com.rbnb.sapi.*;

public class SapiControlTests
{
	private static final TestUtil testUtil = new TestUtil();
	private boolean serverTerminated = false;
	
	@org.junit.Before
	public void startServer() throws Exception
	{
		testUtil.startServer();
		testUtil.startChildServer();
	}
	
	@org.junit.After
	public void stopServer() throws Exception
	{
		if (!serverTerminated) {
			testUtil.stopServer();
			testUtil.stopChildServer();
		}
	}

	@org.junit.Test
	public void testTermination() throws Exception
	{
		boolean success = false;
		Control con = new Control();
		Source srcToTerm = new Source();
		ChannelMap testMap = new ChannelMap();
		
		con.OpenRBNBConnection();
		srcToTerm.OpenRBNBConnection("localhost", "SrcToTerm");

		testMap.Add("testChan");
		testMap.PutDataAsString(0, "Hello!");
		
		srcToTerm.Flush(testMap);
		
		con.Terminate("SrcToTerm");

		try {
			// The first flush will apparently succeed due to buffering.
			for (int ii = 0; ii < 100; ++ii) {
				testMap.PutDataAsString(0, "Goodbye!");
				// Flushing should fail, as the source has been terminated.
				srcToTerm.Flush(testMap);
				Thread.sleep(100);
			}
		} catch (SAPIException sapiE) {
System.err.println("%%%Terminate: "+testUtil.getChildServer().getName());			
			con.Terminate(testUtil.getChildServer().getName());
			srcToTerm.CloseRBNBConnection();
			try {
				// Should fail, as this child has been terminated:
				// --- NOTE: Does not succeed as of 2009/05/20.  Child server
				//  is only disconnected, not terminated.
				srcToTerm.OpenRBNBConnection(
						testUtil.getChildServer().getAddress(),
						"SrcToTerm"
				);			
			} catch (SAPIException sapiE2) {
				con.TerminateServer(); // kill the connected server
				try {
					// Should fail, since local server dead:
					srcToTerm.OpenRBNBConnection("localhost", "FailToCon");
				} catch (SAPIException sapiE3) {
					serverTerminated = success = true;
				}
			}
		}			
		org.junit.Assert.assertTrue(success);
	}
}

