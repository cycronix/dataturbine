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

package com.rbnb.web;

import java.io.*;
import java.net.*;

/**
  * Utility to copy a resource from one URL to another.  Requires the 
  *   destination to support HTTP PUT.
  */
public class HttpCopy
{
	public static void copy(String source, String dest)
		throws IOException
	{
		copy(new URL(source), new URL(dest));
	}
	
	public static void copy(URL source, URL dest)
		throws IOException
	{
		HttpURLConnection srcCon = (HttpURLConnection) source.openConnection(),
				destCon = (HttpURLConnection) dest.openConnection();
				
		InputStream input = srcCon.getInputStream();
		destCon.setDoOutput(true); // necessary or getOutputStream() fails.
		destCon.setRequestMethod("PUT");
		OutputStream output = destCon.getOutputStream();
		
		try {
			byte[] ba = new byte[srcCon.getContentLength()];
			int bytesRead = 0;
			do {
				bytesRead += input.read(ba, bytesRead, ba.length-bytesRead);
			} while (bytesRead != ba.length);
			output.write(ba);
			output.flush();
		} finally {
			input.close();
			output.close();
		}
		int response = destCon.getResponseCode(); // also commits output
		// Input routines throw, output does not.
		if (response >= 200 && response < 300) // success
			System.out.println("Response: Successful ("
					+response+' '+destCon.getResponseMessage()+')');
		else
			throw new IOException("Error in PUT to "+dest+": "
					+response+' '+destCon.getResponseMessage());
	}
	
	public static void main(String[] args)
	{
		try {
			int interval;
			
			if (args.length < 3)
				interval = 60;
			else interval = Integer.parseInt(args[2]);
			if (args.length < 2) {
				System.err.println("Insufficient arguments.");
			} else {
				if (interval == 0)
					copy(args[0], args[1]);
				else while (true) {
					copy(args[0], args[1]);
					Thread.sleep(1000*interval);
				}
				return;				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(
				"HttpCopy source-url dest-url [interval-sec=60]\n"
				+"\tAn interval of zero only copies once.");
	}
}

