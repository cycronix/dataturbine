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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.rbnb.sapi.*;

import com.rbnb.utility.ByteConvert;

/**
  * Class to allow byte level streaming of an RBNB request.
  */
  
// 12/12/2002  WHF  Replaced sink element from constructor with RBNBDirContext.
// 2005/08/03  WHF  Added convertToString method, stolen from ToStringPlugin.
public class RBNBInputStream extends InputStream
{
//***************************  Class Constants  *****************************//
	public static final String STRING_ENCODING = "UTF-8";
	
	/////////////////////// Construction /////////////////////////
	/**
	  * Initializing constructor; the request parameters are cloned for 
	  *  multithreaded protection.
	  *
	  */
	public RBNBInputStream(
		RBNBDirContext context,
		RequestParameters request) throws SAPIException
	{
		RequestParameters temp=null;
		try {
		temp=(RequestParameters) request.clone();
		} catch (CloneNotSupportedException cnse) { } 
		
		this.request=temp;
		this.context=context;
		int idx=map.Add(this.request.name);
		if (this.request.requestData!=null)
		{
			for (int ii=0; ii<this.request.requestData.length; ++ii)
			{
				// We cannot use zero length strings:
				if (this.request.requestData[ii].length()>0)
					map.PutDataAsString(idx, this.request.requestData[ii]);
			}
		}				
	}

	/////////////////////// Input Stream Methods /////////////////////////
	public int available() throws IOException
	{
		if (bais!=null) return bais.available();
		return 0;
	}
	
	public void close() throws IOException { /*sink=null;*/ bais=null; }
	
	public void mark(int readlimit)
	{
		if (bais!=null) bais.mark(readlimit);
	}
	
	public boolean markSupported() { return true; }
	
	public int read() throws IOException
	{
		if (bais==null)
			fetchData();
		return bais.read();
	}
	
	public void reset() throws IOException { if (bais!=null) bais.reset(); }
	
	public long skip(long n) throws IOException 
	{
		if (bais!=null) return bais.skip(n);
		fetchData();
		return bais.skip(n);
	}
	
	// An extension when used in RBNBWebDAVServlet:
	public byte[] getByteArray() throws IOException
	{
		fetchData();
		return theArray;
	}

//	void setDebug(boolean debug) { this.debug=debug; }

	/////////////////////////// Private Methods  //////////////////////////////
	private void fetchData() throws IOException
	{
if (context.getDebug()) System.err.println("RBNBInputStream::fetchData("
+map+") *** "+Thread.currentThread());
		RBNBDirContext.SinkNode sn=null;
		try {
//		synchronized (sink) 
		{
			sn=context.checkSinkConnection();
//			sn.sink.Request(map, time, duration, reference);
			sn.sink.Request(
				map,
				request.start,
				request.duration,
				request.reference);

			sn.sink.Fetch(RBNBDirContext.TIMEOUT, map);
			if (map.GetIfFetchTimedOut()) {
			    // If the fetch timeod out, then close the
			    // connection and throw an exception.
			    sn.sink.CloseRBNBConnection();
			    sn = null;
			    throw new IOException
				("Timed out waiting for response.");
			}
		}
		if (map.NumberOfChannels()>0) {
			switch (request.fetchtype) {
				case RequestParameters.FETCH_DATA:
					if (request.datatype == ChannelMap.TYPE_STRING) {
						bais = new ByteArrayInputStream(
								theArray = convertToString(map));
					} else {
						switch (map.GetType(0)) {
						case ChannelMap.TYPE_FLOAT64:
							bais=new ByteArrayInputStream(
								theArray=ByteConvert.double2Byte(
									map.GetDataAsFloat64(0),
									ChannelMap.LSB.equals(request.byteorder)));
						break;

						case ChannelMap.TYPE_FLOAT32:
							bais=new ByteArrayInputStream(
								theArray=ByteConvert.float2Byte(
									map.GetDataAsFloat32(0),
									ChannelMap.LSB.equals(request.byteorder)));
						break;

						case ChannelMap.TYPE_INT64:
							bais=new ByteArrayInputStream(
								theArray=ByteConvert.long2Byte(
									map.GetDataAsInt64(0),
									ChannelMap.LSB.equals(request.byteorder)));
						break;

						case ChannelMap.TYPE_INT32:
							bais=new ByteArrayInputStream(
								theArray=ByteConvert.int2Byte(
									map.GetDataAsInt32(0),
									ChannelMap.LSB.equals(request.byteorder)));
						break;

						case ChannelMap.TYPE_INT16:
							bais=new ByteArrayInputStream(
								theArray=ByteConvert.short2Byte(
									map.GetDataAsInt16(0),
									ChannelMap.LSB.equals(request.byteorder)));
						break;

						case ChannelMap.TYPE_INT8:
							bais=new ByteArrayInputStream(
								theArray=map.GetDataAsInt8(0));
						break;
						
						// This case assumes that the byte order of the objects
						//  is not significant.  This may not be the case, 
						//  particularly with Unicode strings.  Is UTF-8
						//  byte order independent?
						default:
							bais=new ByteArrayInputStream(
								theArray=map.GetData(0));
						} // end switch (map.GetType(0))
					}
					break;
				
				case RequestParameters.FETCH_TIMES:
					if (request.datatype != ChannelMap.TYPE_STRING) {
						bais=new ByteArrayInputStream(
							theArray=ByteConvert.double2Byte(map.GetTimes(0),
									ChannelMap.LSB.equals(request.byteorder)));
					} else { // convert to string
						double[] timed = map.GetTimes(0);
						StringBuffer result = new StringBuffer();
						for (int j=0; j < timed.length; j++) {
							result.append(Double.toString(timed[j]));
							result.append('\n');
						}
						bais = new ByteArrayInputStream(theArray = 
								result.toString().getBytes(STRING_ENCODING));
					}
					break;
					
				case RequestParameters.FETCH_SIZE:
					long[] size= { map.GetData(0).length }; 
					bais=new ByteArrayInputStream(
						theArray=ByteConvert.long2Byte(size,
									ChannelMap.LSB.equals(request.byteorder)));
					break;
					
				case RequestParameters.FETCH_DATATYPE:
					bais=new ByteArrayInputStream(
						theArray=map.TypeName(map.GetType(0))
						.getBytes(STRING_ENCODING));
					break;
					
				case RequestParameters.FETCH_MIME:
					String mime=map.GetMime(0);
					if (mime==null) mime="null";
					bais=new ByteArrayInputStream(
							theArray=mime.getBytes(STRING_ENCODING));
					break;
					
				case RequestParameters.FETCH_INFO:
					String info = map.GetUserInfo(0);
					bais = new ByteArrayInputStream(
							theArray = info.getBytes(STRING_ENCODING));
					break;
					
				default:
					throw new Error("Bad case in RBNBInputStream.fetchData().");
			}					
		}
		else bais=new ByteArrayInputStream(theArray=new byte[0]);
		} catch (SAPIException se) 
		{ throw new java.io.EOFException("Error obtaining data from RBNB: "
			+se.getMessage()); }
			
		// Recycle the object no matter what:
		finally { if (sn!=null) context.recycleSinkNode(sn); }
	}	
	
	private static byte[] convertToString(ChannelMap cm)
			throws java.io.UnsupportedEncodingException
	{
		StringBuffer result = new StringBuffer();
		
		switch (cm.GetType(0)) {
		case (ChannelMap.TYPE_STRING):
			//already string, just copy it
			result.append(cm.GetDataAsString(0));
			break;
		case (ChannelMap.TYPE_INT8):
			byte[] datab=cm.GetDataAsInt8(0);
			for (int j=0;j<datab.length;j++) {
				result.append(Byte.toString(datab[j]));
				result.append('\n');
			}
			break;
		case (ChannelMap.TYPE_INT16):
			short[] datas=cm.GetDataAsInt16(0);
			for (int j=0;j<datas.length;j++) {
				result.append(Short.toString(datas[j]));
				result.append('\n');
			}
			break;
		case (ChannelMap.TYPE_INT32):
			int[] datai=cm.GetDataAsInt32(0);
			for (int j=0;j<datai.length;j++) {
				result.append(Integer.toString(datai[j]));
				result.append('\n');
			}
			break;
		case (ChannelMap.TYPE_INT64):
			long[] datal=cm.GetDataAsInt64(0);
			for (int j=0;j<datal.length;j++) {
				result.append(Long.toString(datal[j]));
				result.append('\n');
			}
			break;
		case (ChannelMap.TYPE_FLOAT32):
			float[] dataf=cm.GetDataAsFloat32(0);
			for (int j=0;j<dataf.length;j++) {
				result.append(Float.toString(dataf[j]));
				result.append('\n');
			}
			break;
		case (ChannelMap.TYPE_FLOAT64):
			double[] datad=cm.GetDataAsFloat64(0);
			for (int j=0;j<datad.length;j++) {
				result.append(Double.toString(datad[j]));
				result.append('\n');
			}
			break;
		default:
			// Conversion is not done for this type:
			return cm.GetData(0);
		} //end switch(type)
		
		return result.toString().getBytes(STRING_ENCODING);
	}
	
	/////////////////////// Private Instance Data /////////////////////////
//	private Sink sink;
	private final RBNBDirContext context;
	private final ChannelMap map=new ChannelMap();
	private ByteArrayInputStream bais;
/*	private double time, duration;
	private String reference; */
	private final RequestParameters request;
	private byte[] theArray;
//	private boolean debug=false;	
}
