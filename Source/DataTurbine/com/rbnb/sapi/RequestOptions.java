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
  * Encapsulates request settings.  The class is used both by 
  *  {@link Sink#Request(ChannelMap, double, double, String, RequestOptions)},
  *  to set the options used by the sink making a request, and by 
  *  {@link PlugInChannelMap#GetRequestOptions()}, to inform the PlugIn
  *  developer of the settings required for this PlugIn request.
  * <p>The MaxWait property should be set to the wait time in milliseconds,
  *  the time the server waits to completely fulfill the request.  If zero
  *  there is no wait.
  *  
  * <p>
  * @author WHF
  * @since V2.1B4
  */

/* Copyright 2004 Creare Inc.
   All Rights Reserved */
  
public final class RequestOptions extends com.rbnb.api.RequestOptions
{
	/**
	  * Initializes object with the default parameters.
	  */
	public RequestOptions() { }	
}


