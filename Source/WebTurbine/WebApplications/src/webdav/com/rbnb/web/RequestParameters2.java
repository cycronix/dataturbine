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

import com.rbnb.sapi.ChannelMap;
//import com.rbnb.sapi.ChannelMap.ByteOrderEnum;
import java.util.Date;		// MJM 2/11
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
  * A structure for storing the parameters for a WebDAV based data request.
  *  Since the number of these parameters has become large, a class is 
  *  now used to pass them around.
  */

//
// 02/17/2003  WHF  Created.
// 2004/02/10  WHF  Added enhanced MIME guessing code.
// 2005/04/27  WHF  Added FETCH_INFO fetchtype.
// 2005/06/29  WHF  Added protect boolean.
// 2006/09/27  WHF  Removed support for the 'msg' parameter.  Entire query
//     string now always forwarded.
// 2007/04/20  WHF  Added plug-in-option parsing.
// 2011/02/15  MJM  Added ISO8601 time parsing
// 2013/09/03  MJM  Added FETCH_BOTH for fetching both time and data single request

public class RequestParameters2 implements Cloneable
{
	/** 
	  * Sets the parameters to their defaults.
	  */
	public RequestParameters2(java.util.Map defaultProps)
	{
		this.defaultProps = defaultProps;
		init();
	}
	
	/**
	  * Default properties as specified by owner.
	  */
	
	/**
	  * The full requested URI without any query string.  Does not end in a 
	  *  slash.
	  */
	public String path;
	
	/**
	  * Flag used in directory listings for a redirect.
	  */
	public boolean pathHadTrailingSlash;
	
	/**
	  * Channel name, default = <code>null</code>.  Does not include the 
	  *   name of the source.
	  */
	public String name;
	
	/**
	  * Source name, default = <code>null</code>.  No slashes.
	  */
	public String source;
	
	/**
	  * Name of the servlet used in the method call.
	  */
	public String servlet;
	
	/**
	  * Arbitrary string to be included in requests to the server of PlugIns.
	  *  The default = <code>null</code>.
	  */
	public String[] requestData;

	/**
	  * Start time of request; default = 0.
	  */
	public double start;
	
	/** 
	  * Duration of request; default = 0 (one point).
	  */
	public double duration;
	
	/**
	  * Request reference; default = "newest".
	  */
	public String reference;
		
	/**
	  * Byte order of the result or the input data.  Default = MSB.
	  */
	public ChannelMap.ByteOrderEnum byteorder=
		com.rbnb.sapi.ChannelMap.MSB;
	
	/**
	  * Primitive type of the underlying data.  Default = byte array.
	  */
	public int datatype=com.rbnb.sapi.ChannelMap.TYPE_BYTEARRAY;
	
	/**
	  * Number of channels for multiplexed data.
	  */
	public int mux;
	
	/**
	  * Number of words per block for multiplexed data.
	  */
	public int blockSize;
	
	/**
	  * Various fetchtype constants.
	  */
	public static final int 	FETCH_DATA=1,
								FETCH_TIMES=2,
								FETCH_SIZE=3,
								FETCH_DATATYPE=4,
								FETCH_MIME=5,
								FETCH_INFO = 6,
								FETCH_BOTH = 7;		// MJM 9/13:  fetch both data and time
						
	/**
	  * Now loaded from server properties.
	  */
	private int
								CACHE_DEFAULT = 100,
								ARCHIVE_DEFAULT = 10000;

	private final java.util.Map defaultProps;				
								
	/**
	  * Type of fetch performed; data, time, or size.
	  */
	public int fetchtype=FETCH_DATA;
	
	public Object clone() throws CloneNotSupportedException
	{ return super.clone(); }
	
	// Helpers:
	/**
	  * Sets the fetchtype value based on the input string.
	  * <p>
	  * @throws IllegalArgumentException if the type is not one of the ones
	  *  listed above;
	  * @throws NullPointerException if <code>type</code> is null.
	  */
	public void setFetchType(String toFetch)
	{
		toFetch=toFetch.toLowerCase();
		if ("data".equals(toFetch)||"d".equals(toFetch))
			fetchtype=FETCH_DATA;
		else if ("time".equals(toFetch)||"t".equals(toFetch)
				||"times".equals(toFetch))
			fetchtype=FETCH_TIMES;
		else if ("size".equals(toFetch)||"s".equals(toFetch))
			fetchtype=FETCH_SIZE;
		else if ("datatype".equals(toFetch)||"dt".equals(toFetch))
			fetchtype=FETCH_DATATYPE;
		else if ("mime".equals(toFetch)||"m".equals(toFetch))
			fetchtype=FETCH_MIME;
		else if ("info".equals(toFetch)||"i".equals(toFetch))
			fetchtype = FETCH_INFO;
		else if ("both".equals(toFetch)||"b".equals(toFetch))			// MJM 9/13
			fetchtype=FETCH_BOTH;
		else throw new IllegalArgumentException("Fetch type \""+toFetch
			+"\" not recognized.");
	}
	
	public void setByteOrder(String bo)
	{
		bo=bo.toLowerCase();
		if ("msb".equals(bo))
			byteorder=com.rbnb.sapi.ChannelMap.MSB;
		else if ("lsb".equals(bo))
			byteorder=com.rbnb.sapi.ChannelMap.LSB;
		else if ("local".equals(bo))
			byteorder=com.rbnb.sapi.ChannelMap.LOCAL;
		else throw new IllegalArgumentException("Byte order \""+bo
			+"\" not recognized.");
	}
	
	public void setDataType(String dt)
	{
		datatype=cmap.TypeID(dt);
	}
	
	/**
	  * A utility function which calculates the word size based on the current
	  *  data type.  May throw IllegalArgumentException if the size is not
	  *  well-defined from the type (i.e. ByteArray, String).
	  */
	public int getWordSize()
	{
		int size=0;
		switch (datatype)
		{
			case ChannelMap.TYPE_FLOAT64:
				size=8;
				break;

			case ChannelMap.TYPE_FLOAT32:
				size=4;
				break;

			case ChannelMap.TYPE_INT64:
				size=8;
				break;

			case ChannelMap.TYPE_INT32:
				size=4;
				break;

			case ChannelMap.TYPE_INT16:
				size=2;
				break;

			case ChannelMap.TYPE_INT8:
				size=1;
				break;

			default:
				throw new IllegalArgumentException(
					"Unsupported type in PutData().");
		}
		return size;
	}
	
	public void setMux(String mux)
	{
		this.mux=1;
		try {
			this.mux=Integer.parseInt(mux);
		} catch (NumberFormatException nfe) { }
	}
	
	public void setBlockSize(String blockSize)
	{
		this.blockSize=1;
		try {
			this.blockSize=Integer.parseInt(blockSize);
		} catch (NumberFormatException nfe) { }
	}

	public boolean isTimeSet() { return timeSet; }

	/**
	  * Determines the mime type for this resource.  If not specified via
	  *  the RequestParameters, a guess is made based on the extension.
	  *
	  * @since 2004/02/10
	  */
	public String getMime(javax.servlet.ServletContext sc, String name) 
	{ 
		if (mime == null) {
			if (sc!=null && name != null) {
				mime = sc.getMimeType(name);
			}
		}
//System.err.println("The file \""+name+"\" has a mime type of \""+mime+"\".");
		// Mime could still be unset:
		return mime==null?"application/octet-stream":mime;
	}
	
	/**
	  * Type of archive used by server.
	  * <p>
	  * @return Specified mode or, if unspecified, 
	  *  the value of com.rbnb.web.defaultarchivemode in the web.xml file.
	  *
	  * @version 2005/10/11
	  */
	public String getArchiveMode() 
	{ return mime==null?modeDefault:mime; }

	public void setCache(String cache) 
	{
		this.cache=CACHE_DEFAULT;
		try {
			this.cache=Integer.parseInt(cache);
		} catch (NumberFormatException nfe) { }
	}
	public void setArchive(String archive) 
	{
		this.archive=ARCHIVE_DEFAULT;
		try {
			this.archive=Integer.parseInt(archive);
		} catch (NumberFormatException nfe) { }
	}
	public int getCacheSize() { return cache; }
	public int getArchiveSize() { return archive==0?ARCHIVE_DEFAULT:archive; }
	public void setProtect(String prot)
	{
		this.protect = false;
		if ("on".equals(prot) || "true".equals(prot))
			this.protect = true;
	}
	public boolean getProtect() { return protect; }
	
	public void setPlugInOptions(String[] pio) { plugInOptions = pio; }
	public String[] getPlugInOptions() { return plugInOptions; }
	
	private String queryCat(String a, String b)
	{
		if (b == null) return a;
		if (a == null) return b;
		return a + "&" + b;
	}
	
	/**
	  * Sets values to defaults before parsing new values from 
	  *  the string.  This allows this object to be reused.
	  * <p>
	  */
	public void parseQueryString(String uri, //java.util.Map requestParameters)
			String queryString,
			String postQuery)
	{
		init();
		
		// Some clients will encode the URL of certain characters.
		try {
			uri = java.net.URLDecoder.decode(uri, "UTF-8");
		} catch (Throwable t) {}
		
		path = uri;
		map.clear();
		
		int index = uri.indexOf('?');
		if (index==-1) 
			index = uri.indexOf('@'); // Check for @ as alternate syntax

		String paramString = null;		
		if (index!=-1) {
			path = uri.substring(0,index);
			paramString = uri.substring(index+1);
		}
		
		paramString = queryCat(queryCat(paramString, queryString), postQuery);

		if (paramString != null) {
			// 2006/09/27  WHF  What was once the msg field:
			requestData = new String[] { paramString };
			try {
				// Deprecated, but available:
				map.putAll(javax.servlet.http.HttpUtils.parseQueryString( // ignore warning
						paramString));
			} catch (IllegalArgumentException iae) { }
		}
	
		// Strip off trailing slash, if any
		if (path.endsWith("/")) {
			// Signal it:
			pathHadTrailingSlash = true;
			path = path.substring(0, path.length()-1);
		} else {
			pathHadTrailingSlash = false;
		}
		
		// Path looks like: /servlet/source/folder/channel
		// Microsoft client adds redundant slashes, particularly
		//   /servlet//source/folder/channel
		path = path.replaceAll("//+", "/");

		int srcIndex = path.indexOf('/', 1);
		if (srcIndex == -1) {
			servlet = path.substring(1);
			source = "";
			name = "";
		} else {
			servlet = path.substring(1, srcIndex);
			index = path.indexOf('/', srcIndex+1);
			if (index != -1) {
				source = path.substring(srcIndex+1, index);
				name = path.substring(index+1);
			} else {
				source = path.substring(srcIndex+1);
				name = "";
			}
		}
		// Merge in request parameters:
		/*if (requestParameters!=null)
			map.putAll(requestParameters); */
		if (map.size()==0)	
			return; // no parameters, accept defaults, we're done
		
		Object temp;
		temp=map.get("t");
		if (temp==null)
			temp=map.get("time");
		if (temp!=null) {	// MJM 2/15/10:  add ISO time format option
//			System.err.println("parsing time string: "+((String[]) temp)[0]);
			Date dp = ISOparse(((String[]) temp)[0]); 
			if(dp != null) {
				start = (double)dp.getTime() / 1000.;
				timeSet=true;
				reference="absolute";
			}
			else
			try { start=Double.parseDouble(((String[]) temp)[0]); 
				timeSet=true; 
				reference="absolute"; 
			} 
			catch (NumberFormatException nfe) {}
		}

		// MJM:  add 'end' option (implicitly sets duration)
		temp=map.get("e");
		if (temp==null)	temp=map.get("end");
		if (temp!=null) {	// MJM 2/15/10:  add ISO time format option
//			System.err.println("parsing time string: "+((String[]) temp)[0]);
			Date dp = ISOparse(((String[]) temp)[0]); 
			if(dp != null) {
				duration = ((double)dp.getTime() / 1000.) - start; // start needs to be parsed at this point
				timeSet=true;
			}
			else
			try { duration=Double.parseDouble(((String[]) temp)[0]) - start; 
				timeSet=true; 
			} 
			catch (NumberFormatException nfe) {}
		}
	
		temp=map.get("d");
		if (temp==null)
			temp=map.get("duration");
		if (temp!=null)
			try { duration=Double.parseDouble(((String[]) temp)[0]); 
				timeSet=true; } 
			catch (NumberFormatException nfe) {}
		temp=map.get("r");
		if (temp==null) temp=map.get("reference");
		// If one of the above worked, override default.
		if (temp!=null) reference=((String[]) temp)[0];
		
		temp=map.get("m");
		if (temp==null) temp=map.get("mime");
		if (temp==null) temp=map.get("mode");
		if (temp!=null) mime=((String[]) temp)[0];
		
		temp=map.get("f");
		if (temp==null) temp=map.get("fetch");
		if (temp!=null) setFetchType(((String[]) temp)[0]);
		
		temp=map.get("bo");
		if (temp==null) temp=map.get("byteorder");
		if (temp!=null) setByteOrder(((String[]) temp)[0]);
		
		temp=map.get("dt");
		if (temp==null) temp=map.get("datatype");
		if (temp!=null) setDataType(((String[]) temp)[0]);
		
		temp=map.get("a");
		if (temp==null) temp=map.get("archive");
		if (temp!=null) setArchive(((String[]) temp)[0]);
		
		temp=map.get("c");
		if (temp==null) temp=map.get("cache");
		if (temp!=null) setCache(((String[]) temp)[0]);
		
		temp = map.get("p");
		if (temp == null) temp = map.get("protect");
		if (temp != null) setProtect(((String[]) temp)[0]);
		
		// 2007/04/20  WHF  Added.
		temp = map.get("pio");
		if (temp != null) setPlugInOptions((String[]) temp);
		
		/* 2006/09/27  WHF  No longer available.  requestData set above.
		temp=map.get("msg");
		if (temp==null) temp=map.get("message");
// The following two are deprecated as of 04/02/2003:
if (temp==null) temp=map.get("rd");
if (temp==null) temp=map.get("requestdata");
		if (temp!=null) requestData=(String[]) temp; */
		
		temp=map.get("x");
		if (temp==null) temp=map.get("mux");
		if (temp!=null) {
			setMux(((String[]) temp)[0]);
			// Byte array doesn't make sense for MUX, so convert to int8:
			if (datatype==com.rbnb.sapi.ChannelMap.TYPE_BYTEARRAY)
				datatype=com.rbnb.sapi.ChannelMap.TYPE_INT8;
			temp=map.get("bs");
			if (temp==null) temp=map.get("blocksize");
			if (temp!=null) setBlockSize(((String[]) temp)[0]);
		}
	} // end parseQueryString()
	
	/**
	  * Restores all fields to their default values.
	  */
	public void init()
	{
		start=0; duration=0; reference="newest";
		timeSet=false;
		name = source = mime = servlet = path = null;
		requestData=null;
		byteorder=com.rbnb.sapi.ChannelMap.MSB;
		datatype=com.rbnb.sapi.ChannelMap.TYPE_BYTEARRAY;
		fetchtype=FETCH_DATA;
		
		try {
		ARCHIVE_DEFAULT = ((Integer) defaultProps.get("archivesize")).intValue();

		modeDefault = (String) defaultProps.get("archivemode");
		CACHE_DEFAULT = ((Integer) defaultProps.get("cachesize")).intValue();
		} catch (Throwable t) { } // ignore, use previous defaults

		cache = CACHE_DEFAULT;
		archive = ARCHIVE_DEFAULT;
		mux=1; blockSize=1;
		protect = false;
		
		plugInOptions = null;
	}

	// MJM 1/15/11: 
	// ISO 8601 date parsing utility.
	// The supported formats are as follows. Exactly the components shown here must be
	// present, with exactly this punctuation. Note that the "T" appears literally
	// in the string, to indicate the beginning of the time element, as specified in ISO 8601.

	    //    Complete date plus hours, minutes and seconds (presumed local TZ):
	    //       YYYY-MM-DDThh:mm:ss
    	//    Complete date plus hours, minutes and seconds plus 'Z' for GMT:
    	//       YYYY-MM-DDThh:mm:ssZ
	    //    Complete date plus hours, minutes, seconds plus timezone:
	    //       YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30EDT)
	
	    private static Date ISOparse( String dateString ) {
	    	
	    	String[] formats = new String[] {// order these most complex to least?
	    			"yyyy-MM-dd'T'HH:mm:ssz",
//	    			"yyyy-MM-dd'T'HH:mm:ss'Z'",
	    			"yyyy-MM-dd'T'HH:mm:ss",
	    			 };
	    	
	    	// Note: fractional seconds don't parse well with SimpleDateFormat: 
	    	//				.2 -> 002 msec, .200 -> 200 msec
	    	
	    	/*
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	        SimpleDateFormat formatz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
	        SimpleDateFormat formatZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	        SimpleDateFormat formatZz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'z");
	        */
	    	
            // Parse the date
        	Date date=null;
//			for (String format : formats) {	// requires Java 1.5
        	for (int i=0; i<formats.length; i++) {
	        	   String format = formats[i];
				   if(dateString.endsWith("Z")) 
					   dateString = dateString.substring(0,dateString.length()-2) + "GMT";
	  			   SimpleDateFormat sdf = new SimpleDateFormat(format);
	  			   try { date = sdf.parse(dateString); } catch (Exception e) { date =  null; };
	  			   if(date != null) break;
  			}
 /*       	
            if(date == null) try { date = formatZ.parse(dateString); 
            	if(date != null) date = formatZz.parse(dateString+"GMT"); 
            } 	catch (Exception e3){};
            if(date == null) try { date = formatz.parse(dateString); } 	catch (Exception e2){}
            if(date == null) try { date = format.parse(dateString); } 	catch (Exception e1){}; 
            
            if(date !=  null) {
                System.out.println("Original string: " + dateString);
                System.out.println("Parsed date    : " +date.toString());
            }
            else {
                System.out.println("ERROR: could not parse date in string \"" +dateString + "\"");
            }
*/
            return(date);
	    }	
	
	/**
	  * Used to make calls to ChannelMap.TypeID(), which is unfortunately not
	  *   static.
	  */ 
	private final com.rbnb.sapi.ChannelMap cmap=new com.rbnb.sapi.ChannelMap();

	/**
	  * Used to parse query strings.
	  */
	private final java.util.HashMap map=new java.util.HashMap();
	
	private boolean timeSet;
	private int cache;
	private int archive;

	/**
	  * Mime type, used on PUTs.  Default = "application/octet-stream".
	  *  Also used to store archive mode.
	  */
	private String mime;
	
	/**
	  * Loaded from defaultProps.
	  */
	private String modeDefault;
	
	/**
	  * If true, data is not deleted, only hidden.
	  */
	private boolean protect;
	
	/**
	  * If non-null, one or more plug-in options have been set.
	  */
	private String[] plugInOptions;
}




