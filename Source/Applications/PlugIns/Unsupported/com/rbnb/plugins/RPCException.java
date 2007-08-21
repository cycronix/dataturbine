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
  * General exception class for errors that occur during remote-procedure 
  *  calls.
  */
public class RPCException extends Exception
{
	public RPCException(String message) { this(message,null); }

	public RPCException(Throwable cause) 
	{ this("Nesting "+cause.getClass().getName(), cause); }
	
	public RPCException(String message, Throwable cause)
	{
		super(message);
		this.cause=cause;
	}		
	
	public Throwable getRootCause() { return cause; }
	
	/**
	  * Overridden so as to print nested exception also.
	  */
	public void printStackTrace()
	{
		printStackTrace(System.err);
	}

	/**
	  * Overridden so as to print nested exception also.
	  */
	public void printStackTrace(java.io.PrintStream ps)
	{
		super.printStackTrace(ps);
		if (cause!=null)
		{
			ps.println("\nNested exception:");
			cause.printStackTrace(ps);
		}
	}

	/**
	  * Overridden so as to print nested exception also.
	  */
	public void printStackTrace(java.io.PrintWriter pw)
	{
		super.printStackTrace(pw);
		if (cause!=null)
		{
			pw.println("\nNested exception:");
			cause.printStackTrace(pw);
		}
	}
	

	private final Throwable cause;		
}


