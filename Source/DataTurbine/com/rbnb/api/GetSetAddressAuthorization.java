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

/**
  * A Command to download the server's Access Control List.
  *
  * @author WHF
  * @since V3.0
  * @version 2007/07/23
  */
class GetAddressAuthorization extends Command
{
	GetAddressAuthorization(ServerInterface si)
	{ super(si); }
	
	GetAddressAuthorization(InputStream isI, DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
	{ super(isI, disI); }
}

/**
  * A Command to upload the server's Access Control List.
  *
  * @author WHF
  * @since V3.0
  * @version 2007/07/23
  */
class SetAddressAuthorization extends com.rbnb.api.Serializable
{
	private String[] PARAMETERS = { "OBJ", "AUT" };
	
	SetAddressAuthorization(ServerInterface si, String auth)
	{
		this.si = si;
		this.auth = auth;
	}
	
	SetAddressAuthorization(InputStream isI, DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
	{ 
		read(isI, disI);
	}
	
	public String getAuthorization() { return auth; }
	
//***************************  Serializable Overrides  **********************//
    void read(InputStream isI,DataInputStream disI)
		throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
		// Read the open bracket marking the command of the
		// <code>Command</code>. 
		Serialize.readOpenBracket(isI);
		int parameter;
		while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
			readStandardParameter(parameter,isI,disI);
		}
    }

	void write(String[] parametersI,
			   int parameterI,
			   OutputStream osI,
			   DataOutputStream dosI)
		throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
		osI.writeParameter(parametersI,parameterI);
		Serialize.writeOpenBracket(osI);
		writeStandardParameters(osI,dosI);
		Serialize.writeCloseBracket(osI);
    }
	
	private void readStandardParameter(
			int parameterI,
			InputStream isI,
			DataInputStream disI)
		throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
		switch (parameterI) {
			case 0:
			si = (ServerInterface) Language.read(null,isI,disI);
			break;
			
			case 1:
			auth = isI.readUTF();
			break;
		}
	}
	
    private void writeStandardParameters(OutputStream osI,
			DataOutputStream dosI)
		throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
		osI.writeParameter(PARAMETERS, 0);
	    Language.write((Serializable) si, null, osI, dosI);
		
		osI.writeParameter(PARAMETERS, 1);
		osI.writeUTF(auth);	
    }
	
	private ServerInterface si;
	private String auth;
}