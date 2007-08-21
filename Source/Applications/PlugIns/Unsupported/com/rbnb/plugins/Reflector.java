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
  * PlugIn which reflects input data as the output.  If no input is provided,
  *  a string is returned to that effect.
  */
public class Reflector implements SimplePlugIn.PlugInCallback
{
	public void setOptions(java.util.Hashtable ht) { }

	public void processRequest(com.rbnb.sapi.PlugInChannelMap picm)
		throws com.rbnb.sapi.SAPIException
	{
		for (int ii=0; ii<picm.NumberOfChannels(); ++ii)
		{
			int type=picm.GetType(ii);
			byte[] res=picm.GetData(ii);
			String mime=picm.GetMime(ii);
			
System.err.println("Got request, type = "+picm.GetRequestReference()
	+", "+res.length+" bytes, type = "+picm.TypeName(picm.GetType(ii))+": \""
	+new String(res)+'\"');
			
			// Put it back:
			picm.PutTimeAuto("timeofday");
			picm.PutData(ii, res, type);
			picm.PutMime(ii,mime);
		}
	} 
	
	public boolean recycle() { return true; } // can recycle
}
