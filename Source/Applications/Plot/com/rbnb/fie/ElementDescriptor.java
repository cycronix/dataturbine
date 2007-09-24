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
	ElementDescriptor.java
	
	2007/08/22  WHF  Created.
*/

package com.rbnb.fie;

/**
  * Specifies the type of data output by a Serializable data stream.
  */
public final class ElementDescriptor
{
	private ElementDescriptor(int _code, int _size)
	{
		code = _code;
		size = _size;
	}
	
	int getCode() { return code; }
	int getSize() { return size; }
	
	private final int code, size;

	private static final int
			STRUCT_CODE = 0x2,
			CHAR_CODE   = 0x4,
			DOUBLE_CODE = 0x6,
			FLOAT_CODE  = 0x7,
			INT8_CODE   = 0x8,
			UINT8_CODE  = 0x9,
			INT16_CODE  = 0xA,
			UINT16_CODE = 0xB,
			INT32_CODE  = 0xC,
			UINT32_CODE = 0xD,
			INT64_CODE  = 0xE,
			UINT64_CODE = 0xF;

	public static ElementDescriptor
		// Note that although MATLAB often leaves the size zero, we should
		//  use it to help size calculations in the code.
		DOUBLE_DESCRIPTOR = new ElementDescriptor(DOUBLE_CODE, 0x8),
		FLOAT_DESCRIPTOR  = new ElementDescriptor(FLOAT_CODE, 0x4),
		INT64_DESCRIPTOR  = new ElementDescriptor(INT64_CODE, 0x8),
		UINT64_DESCRIPTOR = new ElementDescriptor(UINT64_CODE, 0x8),
		CHAR_DESCRIPTOR   = new ElementDescriptor(CHAR_CODE, 0x2),
		INT32_DESCRIPTOR  = new ElementDescriptor(INT32_CODE, 0x4),
		UINT32_DESCRIPTOR = new ElementDescriptor(UINT32_CODE, 0x4),
		INT16_DESCRIPTOR  = new ElementDescriptor(INT16_CODE, 0x2),
		UINT16_DESCRIPTOR = new ElementDescriptor(UINT16_CODE, 0x2),
		INT8_DESCRIPTOR   = new ElementDescriptor(INT8_CODE, 0x1),
		UINT8_DESCRIPTOR  = new ElementDescriptor(UINT8_CODE, 0x1);
}