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

package com.rbnb.utility;

import java.io.*;

/**
  * Decodes InputStreams which contain binary data in base64 form.
  */
  
//
// 2004/11/10  WHF  Created.
// 2008/07/10  WHF  Added padding character.
//
public abstract class Base64Encode
{
	public static final char BASE64_PAD = '=';
	
	/**
	  * Encodes the provided bytes into a base64 string.
	  */
	public static String encode(byte[] x)	
	{
		StringBuffer sb = new StringBuffer();
		int c;
		for (c = 0; c + 2 < x.length; c += 3) {
			// The & 0xff is necessary to prevent the sign expansion of negative
			//  bytes to negative words.
			int work = ((x[c] & 0xff) << 16) + ((x[c+1] & 0xff) << 8) 
					+ (x[c+2] & 0xff);
			// unrolled loop for speed
			sb.append(Base64Decode.BASE64CHARS.charAt(work >> 18));
			sb.append(Base64Decode.BASE64CHARS.charAt((work >> 12) & 0x3f));
			sb.append(Base64Decode.BASE64CHARS.charAt((work >> 6) & 0x3f));
			sb.append(Base64Decode.BASE64CHARS.charAt(work & 0x3f));
		}
		if (c + 2 == x.length) { // two bytes left, requires 3 * 6 bits
			int work = ((x[c] & 0xff) << 8) + (x[c+1] & 0xff);
			sb.append(Base64Decode.BASE64CHARS.charAt(work >> 10));
			sb.append(Base64Decode.BASE64CHARS.charAt((work >> 4) & 0x3f));
			// pack left
			sb.append(Base64Decode.BASE64CHARS.charAt((work & 0x0f) << 2));
			sb.append(BASE64_PAD);
		} else if (c + 1 == x.length) { // one byte left, requires 2 * 6 bits
			// The unsigned shift is necessary here because x[c] is still a 
			//  byte.
			sb.append(Base64Decode.BASE64CHARS.charAt((x[c] & 0xff) >> 2));
			sb.append(Base64Decode.BASE64CHARS.charAt((x[c] & 0x03) << 4));
			sb.append(BASE64_PAD);
			sb.append(BASE64_PAD);
		}
		return sb.toString();
	}
	
	public static void main(String args[]) throws Exception
	{
		/*
		byte[] buff = new byte[128];
		while (true) {
			int bytesRead = System.in.read(buff);
			if (bytesRead < 2 || bytesRead == 2 && buff[0] == '\r') return;
			byte[] buff2 = new byte[bytesRead - 2]; // \r\n, not true for linux
			System.arraycopy(buff, 0, buff2, 0, bytesRead - 2); 
			String out = encode(buff2);
			System.out.println(out);
			System.out.println(new String(Base64Decode.decode(
					new ByteArrayInputStream(out.getBytes()))));
		} // */ 
		byte[] buff = new byte[256];
		for (int ii = 0; ii < 256; ++ii) buff[ii] = (byte) ii;
		String out = encode(buff);
		System.out.println(out);
		byte[] resp = Base64Decode.decode(
				new ByteArrayInputStream(out.getBytes()));
		for (int ii = 0; ii < 256; ++ii) System.out.println(resp[ii]&0xff); //*/

		/* // Test to compare Our ver with SUN's:		
		System.err.println("SUN:  "+
				(new sun.misc.BASE64Encoder())
				//com.rbnb.utility.Base64Encode
						.encode(args[0].getBytes()));
		System.err.println("RBNB: "+
				//(new sun.misc.BASE64Encoder())
				com.rbnb.utility.Base64Encode
						.encode(args[0].getBytes()));
		*/
	}
}
