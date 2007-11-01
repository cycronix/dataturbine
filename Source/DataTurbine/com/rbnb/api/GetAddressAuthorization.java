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

