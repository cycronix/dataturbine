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
	ToHexPlugIn.java
	
	RBNB Plug-In to convert raw bytes to a hexadecimal representation.
	
	***  History  ***
	2007/07/12  WHF  Created.
*/

import com.rbnb.sapi.*;

public class ToHexPlugIn extends com.rbnb.plugins.PlugInTemplate
{
//*******************************  Constants  *******************************//
	public static final int N_BYTE_PER_LINE = 16;
	
//*****************************  Construction  ******************************//
	public ToHexPlugIn() {}
	
//*************************  PlugInTemplate Overrides  **********************//
	protected void processRequest(
			ChannelMap fwdData,
			PlugInChannelMap picm) throws SAPIException
	{
		StringBuffer sb = new StringBuffer(),
				hex = new StringBuffer(),
				ascii = new StringBuffer();
		
		for (int ii = 0;
				ii < fwdData.NumberOfChannels() && ii < picm.NumberOfChannels();
				++ii) {
			byte[] raw = fwdData.GetData(ii);
			
			// Convert to hex string:
			int lines = raw.length / N_BYTE_PER_LINE;
			writeHeader(sb);
			
			for (int iii = 0; iii < lines; ++iii)
				writeLine(
						raw,
						iii*N_BYTE_PER_LINE,
						(iii+1)*N_BYTE_PER_LINE,
						sb,
						hex,
						ascii
				);
				
			if (lines*N_BYTE_PER_LINE != raw.length)
				writeLine(
						raw,
						lines*N_BYTE_PER_LINE,
						raw.length,
						sb,
						hex,
						ascii
				);
			writeFooter(sb);
			
			// Put into channel:
			picm.PutMime(ii, "text/html");
			picm.PutTime(
					fwdData.GetTimeStart(ii), 
					fwdData.GetTimeDuration(ii)
			);
			picm.PutDataAsString(ii, sb.toString());
		}
	}
	
//*****************************  Static Methods  ****************************//
	private static void writeLine(
			byte[] raw,
			int start,
			int end,
			StringBuffer result,
			StringBuffer hex,
			StringBuffer ascii)
	{
		hex.setLength(0);
		ascii.setLength(0);
		
		for (int ii = start; ii < end; ++ii) {
			byte b = raw[ii];
			hex.append(hexValues[b&0xFF]); // includes trailing space
			if (b == '<') ascii.append("&lt;");
			else if (b == '&') ascii.append("&amp;");
			else if (b == '>') ascii.append("&gt;");
			else if (b >= ' ' && b <= '~')
				ascii.append((char) b);
			else ascii.append(' ');
		}
		
		for (int ii = end; ii < start + N_BYTE_PER_LINE; ++ii)
			hex.append("   ");
		
		result.append("<b>");
		String offsetStr = Integer.toHexString(start);
		for (int ii = offsetStr.length(); ii < 8; ++ii)
			result.append('0');
		result.append(offsetStr);
		result.append("</b> ");
		result.append(hex);
		result.append("<span style=\"background-color: #AFAFAF;\">");
		result.append(ascii);
		result.append("</span>");
		result.append("\r\n");
	}
	
	private static void writeHeader(StringBuffer sb)
	{
		sb.setLength(0);		
		sb.append("<html><body><code><pre>");
		sb.append("         "
				+"<b>00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F</b>\r\n"); 
	}
	
	private static void writeFooter(StringBuffer sb)
	{
		sb.append("</pre></code></body></html>");
	}
	
	public static void showHelp()
	{
		System.err.println("ToHexPlugIn [options]"
			+"\nwhere options are zero or more of:"
			+"\n\t-a host:port      RBNB host to connect to  [localhost:3333]"
			+"\n\t-n name           plug-in client name      [ToHexPlugIn]"
			+"\n\t-h or -?          show this help"
		);
	}
	
	public static void main(String[] args) throws Exception
	{
		ToHexPlugIn thpi = new ToHexPlugIn();
		
		// Check command-line arguments:
		boolean help = false;
		try {
			for (int ii = 0; ii < args.length; ++ii) {
				if (args[ii].charAt(0) != '-') { help = true; break; }
				switch (args[ii].charAt(1)) {
					case 'a': thpi.setHost(args[++ii]); break;
					case 'n': thpi.setName(args[++ii]); break;
					case '?':
					case 'h': help = true; ii = args.length; break;
				}
			}
		} catch (Exception e) { e.printStackTrace(); help = true; }
		if (help) {
			showHelp();
			return;
		}			
		
		thpi.start();
		System.in.read();
		thpi.stop();
	}
	
	private static final String[] hexValues = new String[256];
	static {
		for (int ii = 0; ii <= 0xFF; ++ii) {
			hexValues[ii] = Integer.toHexString(ii) + ' ';
			if (hexValues[ii].length() == 2)
				hexValues[ii] = "0" + hexValues[ii];
		}
	}
}

