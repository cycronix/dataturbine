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

package utility;

/**
  * Various utility methods for use in web-pages.
  */

// Mods:
// 04/09/2002  WHF  Created
public final class WebUtil
{
	/**
	  * Utility to replace a single character with a substring.
	  * @exception NullPointerException if either String parameter is null.
	  */
	public static String replace(String s1, char toReplace, String replacement)
	{
		StringBuffer sb=new StringBuffer(s1);
		for (int ii=0; ii<sb.length(); )
		{
			if (sb.charAt(ii)==' ')
			{
				sb.replace(ii,ii+1,replacement);
				ii+=replacement.length();
			}
			else ++ii;
		}
		return sb.toString();
	}
}
