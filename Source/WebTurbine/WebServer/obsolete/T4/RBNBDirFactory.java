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

package com.rbnb.web;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;

/**
  * Service Provider factory for RBNB Directory Contexts.  Instances of this
  *  class generate JNDI Directory Context wrappers to the RBNB.
  * @author Bill Finger
  *
  * @since V2.0
  * @version 09/03/2002
  */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/04/2002  WHF	Created.
 *
 */
 
public class RBNBDirFactory implements javax.naming.spi.ObjectFactory,
	javax.naming.spi.InitialContextFactory
{
	public RBNBDirFactory() { }

	// ObjectFactory interface:
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
		Hashtable environment) throws NamingException
	{
System.err.println("Got here!");
		RBNBDirContext rdc=new RBNBDirContext(environment);
		
		// Customize the context properties from our attributes
		if (obj instanceof Reference)
		{
			Reference ref = (Reference) obj;
			Enumeration addrs = ref.getAll();
			while (addrs.hasMoreElements()) 
			{
			  RefAddr addr = (RefAddr) addrs.nextElement();
			  rdc.addToEnvironment(addr.getType(),addr.getContent());
        	}
		}
		
		return rdc;
	}
	
	// InitialContextFactory
	public Context getInitialContext(Hashtable environment)
	{
		return new RBNBDirContext(environment);
	}
}

