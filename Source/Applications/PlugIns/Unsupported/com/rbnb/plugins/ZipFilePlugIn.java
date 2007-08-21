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

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
  * A PlugIn to create zip files from raw data.  One instance of PlugInCallback
  *  will be created for every request, and kept in a pool by SimplePlugIn.
  */
  
//
// 2003/10/22  WHF  Created.
// 
public class ZipFilePlugIn implements SimplePlugIn.PlugInCallback
{
// *************************** PlugInCallback Interface *********************//	
	public void setOptions(Hashtable options)
	{
		myOptions = options;
	}
	
	public void processRequest(PlugInChannelMap picm) throws SAPIException
	{
		boolean registrationRequest="registration".equals(
			picm.GetRequestReference());
		if (!(registrationRequest && picm.NumberOfChannels()==0
			||picm.NumberOfChannels()==1  // old style reg request
				&&picm.GetName(0).equals("...")))
		{ // not an empty registration request: fill in map:
			
			verifySink();

			// Get original data:
			sinkMap.Clear();
			for (int ii=0; ii<picm.NumberOfChannels(); ++ii)
				sinkMap.Add(picm.GetName(ii));
			
			sink.Request(sinkMap,
				picm.GetRequestStart(),
				picm.GetRequestDuration(),
				picm.GetRequestReference()
				// ,picm.IsRequestFrames() whf 05/22/2002
				);
			sink.Fetch(60000,sinkMap); // bump timeout 10->60? mjm

			if (sinkMap.GetIfFetchTimedOut())
				System.err.println("++ ZipFilePlugIn Time out on"
					+picm.GetName(0));
			
			else if (registrationRequest) {
				for (int ii=0; ii<sinkMap.NumberOfChannels(); ++ii)	{
					int rIndex=picm.GetIndex(
						sinkMap.GetName(ii));					
	
					if (rIndex==-1) // channel does not exist 
					// in request, add it
						rIndex=picm.Add(sinkMap.GetName(ii));
	
						picm.PutTimeRef(sinkMap,ii);
						if ("text/xml".equals(sinkMap.GetMime(ii)))
						{  // probably server meta-data, override
							String result=
								"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
								+"<!DOCTYPE rbnb>\n"
								+"<rbnb>\n"
								+"\t\t<size>"+1+"</size>\n"
								+"\t\t<mime>"+ZIP_MIME+"</mime>\n"
								+"</rbnb>\n";
							picm.PutDataAsString(rIndex, result);						
						}
						else
							picm.PutData(rIndex,sinkMap.GetData(ii),
								sinkMap.GetType(ii));
						picm.PutMime(rIndex,sinkMap.GetMime(ii));
				} // end for
			} else { // write the zip file
				picm.Clear();
				int index = picm.Add("output.zip");
				picm.PutMime(index, ZIP_MIME);
				picm.PutDataAsByteArray(index, zip());
			}
		} else
			picm.Clear();  // for "..." registration, return nothing		
	}
	
	public boolean recycle()
	{
		sink.CloseRBNBConnection();
		return true;
	}
	
// **************************** Static Methods ***********************//	
	/**
	  * The main only prints instructions on how to use the service with
	  * the XMLRPCPlugIn.  It then exits.
	  */
	public static void main(String[] args)
	{
		if (args.length == 0)
			showUsage();
		else {
			String[] args2 = new String[args.length+2];
			System.arraycopy(args, 0, args2, 0, args.length);
			args2[args.length]="-c";
			args2[args.length+1]=ZipFilePlugIn.class.getName();
			SimplePlugIn.main(args2);
		}
			
	}
	
	private static void showUsage()
	{
		System.err.println(ZipFilePlugIn.class.getName()
			+": Data compressor.\nCopyright Creare, Inc. 2003"
			+"\nOptions:"
			+"\n\t-a host:port [localhost:3333]\t- RBNB server"
				+" to connect to"
			+"\n\t-n name ["+ZipFilePlugIn.class.getName()
				+"]\t- client name for plugin"
		);
	}
	
	
	private static final String ZIP_MIME = "application/x-zip-compressed";
	
// *************************** Private Methods ******************************//
	/**
	  * Verifies that a good sink connection exists; if not, reconnects once.
	  */
	private void verifySink() throws SAPIException
	{
		String hostname = myOptions.get("RBNB").toString(),
			sinkname = "ZipFilePlugIn.sink",
			user = myOptions.get("user").toString(),
			pword = myOptions.get("password").toString();
			
		boolean reconnect = true;
		try {
			if (hostname.equals(sink.GetServerName()))
				reconnect = false;
		} catch (Throwable t) { }
		if (reconnect)
			sink.OpenRBNBConnection(hostname, sinkname, user, pword);
	}
	
	/**
	  * Performs zip function.
	  */
	private byte[] zip()
	{
		try {
			baos.reset();
			ZipOutputStream zos = new ZipOutputStream(baos);
			
			for (int ii=0; ii<sinkMap.NumberOfChannels(); ++ii) {
				byte[] data = sinkMap.GetData(ii);
				ZipEntry ze = new ZipEntry(sinkMap.GetName(ii));
				
				zos.putNextEntry(ze);  // will deflate by default
				zos.write(data);
			}
			zos.finish();
	
			return baos.toByteArray();
		} catch (IOException ie) { ie.printStackTrace(); }
		return null;
	}

	
// ************************** Private Instance Data *************************//
	private final Sink sink = new Sink();
	private final ChannelMap sinkMap = new ChannelMap();
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private Hashtable myOptions;
}


