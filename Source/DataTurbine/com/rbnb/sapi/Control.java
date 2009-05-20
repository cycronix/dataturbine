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
	Control.java
	
	Copyright Creare Inc.  2006.  All rights reserved.
	
	
	*** Modification History ***
	2007/07/20  WHF  Created.
	2009/05/20  WHF  Implemented Terminate(String) method.  Renamed Terminate()
		method to TerminateServer().
*/

package com.rbnb.sapi;

import java.lang.reflect.Method;
import java.io.IOException;
import java.util.Vector;

import com.rbnb.api.Controller;
import com.rbnb.api.ControllerHandle;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Username;

/**
  * A client for controlling RBNB server behavior.
  * <p>
  * @author WHF
  * @since V3.1
  *
  */
public class Control extends Client
{
	public Control()
	{
		super(1, null, 0);
	}
	
//*****************************  Public Methods  ****************************//
	/**
	  * Creates a mirror to copy data from a source on the specified server 
	  *  address to the connected server.
	  */
	public Mirror CreateMirrorIn(
			String remoteServer,
			String remoteSource,
			String localName) throws SAPIException
	{
		try {
			controller.mirror(Server.createMirror());	
		} catch (Exception e) {
			throw new SAPIException(e);
		}
		
		return new Mirror();
	}
	
	/**
	  * Creates a mirror to copy data from a local source to the specified
	  *   remote server.
	  */
	public Mirror CreateMirrorOut(
			String localSource,
			String remoteServer,
			String remoteName) throws SAPIException
	{
		return new Mirror();
	}

	/**
	  * Stops the object with the given name.  It may be a server or any
	  *  sort of client.
	  * <p>Note: this method returns after the server has acknowledged receipt
	  *   of the command, but before the server executes the task.  Thus the
	  *  actual shutdown of the object will occur asynchronously.
	  */
	public void Terminate(String name) throws SAPIException
	{
		assertConnection();
		
		Rmap leaf;
		try {
			// Create an Rmap hierarchy to the target.  Note that we 
			//  use the fully qualified name unless a leading slash is present.
			//  This is because stop() fails with child servers using a 
			//  relative name (leading "." as the name).  Other clients work!
			String fullName = name.charAt(0) == '/' ? name 
					: getServer().getFullName() + '/' + name;
			Rmap toFind = getServer().createFromName(fullName);
				
			leaf = controller.getRegistered(toFind);
			// Find the endpoint of the Rmap chain:
			while (leaf.getNchildren() > 0)
				leaf = leaf.getChildAt(0);
		
			// The controller stop method takes one of Client, Server, 
			//  or Shortcut, none of which have a common base class.
			//  Thus we must handle each case separately.
			if (leaf instanceof com.rbnb.api.Client) {
				controller.stop((com.rbnb.api.Client) leaf);
			} else if (leaf instanceof Server) {
				controller.stop((Server) leaf);
			} else if (leaf instanceof com.rbnb.api.Shortcut) {
				controller.stop((com.rbnb.api.Shortcut) leaf);
			}

		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}
	
	/**
	  * Stops the server to which this Control is connected.
	  */
	public void TerminateServer() throws SAPIException
	{
		assertConnection();
		
		try {
			super.terminateLocalServer();
		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}
	
	/**
	  * Obtains a list of access control entries from the connected server.
	  */
	public Vector GetAccessControlList() throws SAPIException
	{
		Vector resp = new Vector();
		
		try {
			String auth = ((ControllerHandle) controller)
					.fetchAddressAuthorization(getServer()); 
			resp = CreateACLFromStream(new java.io.StringReader(auth));
		} catch (Exception e) {
			throw new SAPIException(e);
		}
		
		return resp;
	}
	
	/**
	  * Sets the access control list on the server.
	  * @throws IllegalArgumentException  if any element in the vector is
	  *   not an instance of {@link AccessControlEntry}.
	  */
	public void SetAccessControlList(Vector list) throws SAPIException
	{
		StringBuffer sb = new StringBuffer();

		for (int ii = 0; ii < list.size(); ++ii) {
			sb.append(list.elementAt(ii));
			sb.append('\n');
		}
		try {
			((ControllerHandle) controller).sendAddressAuthorization(
					getServer(),
					sb.toString()
			);
		} catch (Exception e) {
			throw new SAPIException(e);
		}
	}

//*****************************  Client Overrides  **************************//
	public long BytesTransferred()
	{
		if (controller != null)
			return ((ControllerHandle)controller).bytesTransferred(); 
		else return 0L;
	}
		
	void doOpen(
			Server server,
			String clientName,
			String userName,
			String password
	) throws Exception
	{
	    controller = server.createController(clientName);
		if (userName != null) {
			controller.setUsername(new Username(userName,password));
		}
	    controller.start();
	}
	
	com.rbnb.api.Client getClient() { return controller; }
	final void clearData()
	{
		controller = null;
	}	
	
//*****************************  Inner Classes  *****************************//
	/**
	  * An element in a list of internet hosts which define permitted access
	  *   to an RBNB server.
	  */
	public final static class AccessControlEntry
	{
		private AccessControlEntry(
				boolean isDeny,
				String address,
				String permissions
		) {
			this.isDeny = isDeny;
			this.address = address;
			this.permissions = permissions;
		}
		
		public boolean isDeny() { return isDeny; }
		public String getAddress() { return address; }
		public String getPermissions() { return permissions; }
	
		public String toString() 
		{
			return (isDeny?"DENY ":"ALLOW ")+address
					+(permissions!=null?"="+permissions:"");
		}
		
		private boolean isDeny;
		private String address, permissions;
	}
	
	/**
	  * A representation of a 'mirror', which copies data from one server
	  *   to another.
	  */
	public final static class Mirror
	{
		
		
	}
	
//*****************************  Data Members  ******************************//
	private Controller controller;
	
//*****************************  Statics  ***********************************//
	/**
	  * Create a new access control entry that allows access.  The 
	  * <code>permissions</code> object is optional.  If specified it should
	  *  be one or more of the following:
	  *	  <ul>
	  *		<li>R - read permission (sink connections),</li>
	  *		<li>W - write permission (source connections),</li>
	  *		<li>X - execute permission (control connections and functions),</li>
	  *		<li>P - plugin permission (plugin connections), and</li>
	  *		<li>T - routing permission (routing connections).</li>
	  *	  </ul>
	  * If not set it is as if all these options were specified.
	  * <p>
	  * See the <a href="http://rbnb.creare.com:8080/documentation/Server/rbnbServer.html">
	  *   RBNB Server documentation</a> for more details.
	  */
	public static AccessControlEntry CreateAllowEntry(
			String address,
			String permissions)
	{ return new AccessControlEntry(false, address, permissions); }

	/**
	  * Create a new access control entry that forbids access.
	  */
	public static AccessControlEntry CreateDenyEntry(String address)
	{ return new AccessControlEntry(true, address, null); }
	
	/**
	  * Create an access control list from a character stream.
	  */
    /*
     *
     *   Date      By 	Description
     * YYYY/MM/DD
     * ----------  ---  -----------
     * 2007/07/23  WHF  Created from com.rbnb.api.AddressAuthorization code. 
     *
     */
	  
	public static Vector CreateACLFromStream(java.io.Reader isRead)
			throws IOException
	{
		java.io.LineNumberReader lRead = new java.io.LineNumberReader(isRead);
		String line,
			   token;
		java.util.StringTokenizer sTok;
		int allowDeny = 0;
		boolean allowAdded = false;
		Vector result = new Vector();

		try {
			while ((line = lRead.readLine()) != null) {
				// Read each line of the authorization file.

				if ((line.length() == 0) || (line.charAt(0) == '#')) {
					// Skip over blank lines and lines starting with #.
					continue;
				}

				// Tokenize the line by breaking it at whitespace.
				sTok = new java.util.StringTokenizer(line," \t\n\r");
				while (sTok.hasMoreTokens()) {
					// Loop through the tokens.
					token = sTok.nextToken();

					if (token.equalsIgnoreCase("ALLOW")) {
						// The addresses to follow will be allowed.
						allowDeny = 1;

					} else if (token.equalsIgnoreCase("DENY")) {
						// The addresses to follow will be denied.
						allowDeny = -1;

					} else if (allowDeny == 0) {
						// The allow/deny state has not been set yet.
						throw new java.lang.IllegalStateException
							("Do not know whether to allow or deny " +
							 token +
							 ".");

					} else {
						// Parse out the equals:
						int eq = token.indexOf('=');
						String perm;
						
						if (eq == -1) // no equals
							perm = null;
						else {
							perm = token.substring(eq+1);
							token = token.substring(0, eq);
						}
						
						if (allowDeny == -1) {
							// Deny mode:
							if (perm != null) throw new IllegalStateException(
								"DENY elements do not accept permissions.");
							result.addElement(CreateDenyEntry(token));
						} else {
							// Allow mode:
							allowAdded = true;
							result.addElement(CreateAllowEntry(
									token,
									perm
							));
						}
					}
				}
			}

			if (!allowAdded) {
				// If nothing was specifically allowed, then create a default
				// entry to allow everything that isn't denied.
				result.addElement(CreateAllowEntry("*", null));
			}

		} catch (java.io.EOFException e) {
		}

		lRead.close();	
		isRead.close();
		
		return result;
	}	
		
	
/*
	// Accesss control list test.
	public static void main(String args[]) throws Exception
	{
		Control c = new Control();
		c.OpenRBNBConnection();
		
		System.err.println(c.GetAccessControlList());
		
		if (args.length > 0) {
			c.SetAccessControlList(CreateACLFromStream(
					new java.io.StringReader(args[0])));
					
			// Get back to confirm:
			System.err.println(c.GetAccessControlList());
		} else System.err.println("No set.");

		c.CloseRBNBConnection();
	}
*/
}
