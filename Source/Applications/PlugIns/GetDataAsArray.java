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
	GetDataAsArray.java
	
	2007/09/12  WHF  Created.
*/

import com.rbnb.sapi.ChannelMap;

/**
  * A simple helper to extract numerical data from a ChannelMap.  The 
  *   java.lang.reflect.Array class may then be used to extract doubles.
  */
class GetDataAsArray
{
	public static Object get(ChannelMap cm, int index)
	{
		Object data;
		
		switch (cm.GetType(index)) {
			case ChannelMap.TYPE_FLOAT32:
			data = cm.GetDataAsFloat32(index);
			break;
			
			case ChannelMap.TYPE_FLOAT64:
			data = cm.GetDataAsFloat64(index);
			break;
			
			case ChannelMap.TYPE_INT16:
			data = cm.GetDataAsInt16(index);
			break;

			case ChannelMap.TYPE_INT32:
			data = cm.GetDataAsInt32(index);
			break;

			case ChannelMap.TYPE_INT64:
			data = cm.GetDataAsInt64(index);
			break;

			case ChannelMap.TYPE_INT8:
			data = cm.GetDataAsInt8(index);
			break;

			default:
			data = null;
			break;
		}		
		
		return data;
	}		
}

