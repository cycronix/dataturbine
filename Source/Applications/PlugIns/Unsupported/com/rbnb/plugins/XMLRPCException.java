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

package com.rbnb.plugins;

/**
  * Exception type specific to XML-RPC.  Optionally includes the fault code 
  *  number given in XML-RPC specification, although specific codes are 
  *  undefined.
  */
public class XMLRPCException extends RPCException
{
	public XMLRPCException(String message) { super(message); }
	
	public XMLRPCException(String message, Throwable t) { super(message, t); }
	
	public XMLRPCException(Throwable t) { super(t); }
	
	/**
	  *  Construct exception with XMLRPC fault code.
	  */
	public XMLRPCException(String message, int code) 
	{
		super(message);
		this.code=code;
	}
	
	/**
	  *  Construct exception with XMLRPC fault code.
	  */
	public XMLRPCException(int code, String message)
	{
		this(message, code);	
	}

	public int getCode() { return code; }
	
	private int code=0;
}


