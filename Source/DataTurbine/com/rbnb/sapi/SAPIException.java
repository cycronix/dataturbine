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

package com.rbnb.sapi;

/**
 * The typed exception thrown by the SAPI package.
 * <p>
 *
 * @author WHF
 *
 * @since V2.0
 * @version 05/17/2001
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/17/2001  WHF	Created.
 * 07/30/2001  WHF      Modified rethrow constructor to provide
 *                         class information.
 *
 */

public class SAPIException extends java.lang.Exception
{
	/**
	  * Constructor with no message.
	  */
	public SAPIException() { }

	/**
	  * Constructor with the specified message.
	  */
	public SAPIException(String s) { super(s); }

	/**
	  * Constructor which copies its message from another exception.
	  */
	public SAPIException(Exception e)
	{
		super("Nesting "+e.getClass().getName());
		nestedException=e;
	}

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
		if (nestedException!=null)
		{
			ps.println("\nNested exception:");
			nestedException.printStackTrace(ps);
		}
	}

	/**
	  * Overridden so as to print nested exception also.
	  */
	public void printStackTrace(java.io.PrintWriter pw)
	{
		super.printStackTrace(pw);
		if (nestedException!=null)
		{
			pw.println("\nNested exception:");
			nestedException.printStackTrace(pw);
		}
	}

	private Exception nestedException=null;
}
