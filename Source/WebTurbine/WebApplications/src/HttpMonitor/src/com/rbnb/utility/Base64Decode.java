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
// 2003/07/10  WHF  Created.
// 2004/11/10  WHF  Made BASE64CHARS package protected so that it might be 
//  shared with Base64Encode.
//
public class Base64Decode
{
	private final static byte[] REV_BASE64=new byte['z'+1];
	
	static final  String BASE64CHARS = 
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		
	static {
		for (int ii=0; ii<BASE64CHARS.length(); ++ii)
			REV_BASE64[BASE64CHARS.charAt(ii)]=(byte) ii;
	}
	
	/**
	  * Returns a binary array which is the result of the base64 decoding.
	  */
	public static byte[] decode(InputStream enc) throws IOException
	{	
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		
		byte[] buf=new byte[4];
		int c;
		while ((c=enc.read(buf))==4) { // unrolled loop for speed
			int work=
				(REV_BASE64[buf[0]]<<18)
				+(REV_BASE64[buf[1]]<<12)
				+(REV_BASE64[buf[2]]<<6)
				+(REV_BASE64[buf[3]]);

			buf[0]=(byte) ((work>>16)&0xff);
			buf[1]=(byte) ((work>>8)&0xff);
			buf[2]=(byte) (work&0xff);
			baos.write(buf,0,3);
		}
		if (c==3) {
			int work=
				(REV_BASE64[buf[0]]<<12)
				+(REV_BASE64[buf[1]]<<6)
				+(REV_BASE64[buf[2]]);
			buf[0]=(byte) ((work>>10)&0xff);
			buf[1]=(byte) ((work>>2)&0xff); // note lose last two bits, 
										//  should be zero if properly encoded
if ((work&0x3)!=0) System.err.println("WARNING: May be error in logic (3).");
			baos.write(buf,0,2);
		} else if (c==2) {
			int work=
				(REV_BASE64[buf[0]]<<6)
				+(REV_BASE64[buf[1]]);
			buf[0]=(byte) ((work>>4)&0xff); // note lose last four bits, 
										//  should be zero if properly encoded
if ((work&0xf)!=0) System.err.println("WARNING: May be error in logic (2).");
			baos.write(buf[0]);
		} else if (c==1) {
			// Since one 6 bit word does not encode an 8 bit byte properly,
			//  this case should never occur.
			System.err.println("WARNING: May be error in logic (1).");
		}
		return baos.toByteArray();
	}
	
	/**
	  * A test method of questionable utility.
	  */
	public static void main(String args[])
	{
		try {
		FileOutputStream fos=new FileOutputStream("out.dat");
	
		fos.write(decode(System.in));
		fos.close();
		} catch (IOException ie) { ie.printStackTrace(); }
	}	
}
