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
	PngMonitor.java
	
	As HttpMonitor, but if the file is a PNG, its file time is extracted.
	
	***  History  ***
	2007/06/25  WHF  Created.
	2007/07/19  WHF  Defaults to HttpMonitor if the PNG time cannot be
			extracted.
*/

package com.rbnb.web;

import java.io.IOException;

import javax.imageio.ImageReader;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class PngMonitor extends HttpMonitor
{
	public static void main(String[] args)
	{
		HttpMonitor.resourceClass = PngResource.class;
		
		HttpMonitor.main(args);
	}	
	
	protected static class PngResource extends HttpMonitor.Resource
	{
		public PngResource(URL src, URL dest)
		{
			super(src, dest);
		
			if (src.getFile().toLowerCase().endsWith(".png")) doPng = true;
			else doPng = false;
		}
		public URL getDestination()
		{
			if (!doPng) return super.getDestination();
			
			String time;
			byte[] data = getLastRead();
			
			ImageReader imr = (ImageReader)
					javax.imageio.ImageIO.getImageReadersByMIMEType(
					"image/png").next();

			try {					
				imr.setInput(
						new javax.imageio.stream.MemoryCacheImageInputStream(
						new java.io.ByteArrayInputStream(getLastRead()))
				);
			
				time = extractTime(imr);
			} catch (IOException ioe) {
				System.err.println("Error extracting content time:");
				ioe.printStackTrace();
				return super.getDestination();
			}
			URL dest = super.getDestination();
			if (time == null) return dest;
			String newQuery = "?t=" + time;
			String dQuery = dest.getQuery();
			if (dQuery != null) newQuery += "&" + dQuery;
			
			try {
				return new URL(dest, dest.getFile() + newQuery);
			} catch (MalformedURLException male) {
				System.err.println("Could not form new URL:");
				male.printStackTrace();
			}
			
			return null;
		}
		
		private String extractTime(ImageReader imr) throws IOException
		{
			String result;			
			javax.imageio.metadata.IIOMetadata meta = imr.getImageMetadata(0);			
			Node root = meta.getAsTree(meta.getMetadataFormatNames()[0]);			
			Node text = findChild(findChild(root, "tEXt"), "tEXtEntry");
			if (text == null) {
//System.err.println("No tEXtEntry found.");				
				return null;
			}
			// The text is located in the node's attributes, one labeled
			//  keyword, with a value of ContentTime(Unix), and one labeled
			//  value, with a value of time in seconds as a string.
			org.w3c.dom.NamedNodeMap attr = text.getAttributes();
			if ("ContentTime(Unix)".equals(
					attr.getNamedItem("keyword").getNodeValue())) {
//System.err.print("Found time: ");
				result = attr.getNamedItem("value").getNodeValue();
//System.err.println(result);				
			} else {
//System.err.println("No time found.");
				result = null;
			}				
			
			return result;
		}
		
		private Node findChild(Node parent, String name)
		{
			if (parent == null) return null;
			NodeList nl = parent.getChildNodes();
			for (int ii = 0; ii < nl.getLength(); ++ii) {
				Node child = nl.item(ii);
				if (name.equals(child.getNodeName()))
					return child;
			}
			
			return null;
		}
		
		private final boolean doPng;
	}
}

