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

package com.rbnb.api;

import java.lang.reflect.Method;

/**
  * Wrapper for the GSIAuthentication class.
  * @author WHF
  * @version 2005/01/07
  * @since V2.5
  */
  
class SecurityProviderGSIImpl implements SecurityProvider
{
	/**
	  * Attempts to create an instance of the GSIAuthentication class.
	  */
	SecurityProviderGSIImpl() 
			throws ClassNotFoundException, IllegalAccessException,
					InstantiationException, 
					NoSuchMethodException
	{
		Class gsiAuth = Class.forName("com.gridwise.gsi.GSIAuthentication");
		Class[] serverParameters = {
				java.io.InputStream.class, 
				java.io.OutputStream.class,
				Class.forName("org.ietf.jgss.GSSCredential")
		}, clientParameters = {
				java.io.InputStream.class, 
				java.io.OutputStream.class,
				Class.forName("org.ietf.jgss.GSSCredential"),
				String.class
		};
		serverSide = gsiAuth.getMethod("serverSide", serverParameters);
		clientSide = gsiAuth.getMethod("clientSide", clientParameters);
	}
	
	/**
	  * Authenticates incoming data on the server.
	  */
	public void serverSideAuthenticate(
			java.io.InputStream is,
			java.io.OutputStream os) throws Exception
	{
		Object[] parameters = {
				is,
				os,
				null
		};
		// Invoke a static method:
		serverSide.invoke(null, parameters); 
	}

	/**
	  * Authenticates incoming data on the client.
	  */
	public void clientSideAuthenticate(
			java.io.InputStream is,
			java.io.OutputStream os) throws Exception
	{
	  String expectedTargetName = System.getProperty("com.gridwise.gsi.target");
System.err.println("Target = " + expectedTargetName);

		org.ietf.jgss.GSSManager manager = (org.ietf.jgss.GSSManager)
				Class.forName("org.globus.gsi.gssapi.GlobusGSSManagerImpl")
				.newInstance();
		
		org.ietf.jgss.GSSCredential userCredential = manager.createCredential(
				null, //userName,
				org.ietf.jgss.GSSCredential.DEFAULT_LIFETIME,
				//org.globus.gsi.gssapi.GSSConstants.MECH_OID,
				(org.ietf.jgss.Oid) (Class.forName(
						"org.globus.gsi.gssapi.GSSConstants")
						.getField("MECH_OID").get(null)),
				org.ietf.jgss.GSSCredential.INITIATE_ONLY);
		
/*		com.gridwise.gsi.GSIAuthentication.clientSide(
				is,
				os,
				userCredential,
				expectedTargetName); */	
		
		Object[] parameters = {
				is,
				os,
				userCredential,
				expectedTargetName
		};
		clientSide.invoke(null, parameters);
	}
	
	
// **************************  Data Members  ********************************//
	// GSI Authentication methods.
	private final Method clientSide, serverSide;
}

