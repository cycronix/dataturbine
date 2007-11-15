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

package com.rbnb.sapi;

import java.io.ByteArrayOutputStream;

/* These are 1.2 classes.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//  import java.util.LinkedList;
import java.util.List;
*/

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.util.StringTokenizer;

import com.rbnb.api.*;

/**
  * A class to encapsulate the concept of a list of channels, each of which
  *  contains data.
  * <p>
  * @author WHF
  * @version 2005/03/31
  * 
*/

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/25/2002  WHF	Created.
 * 01/28/2002  WHF	Added GetMime, PutMime methods, renamed several others.
 * 04/10/2002  WHF	Added PutTimeRef(), GetNodeList(), GetChannelList().
 * 05/03/2002  WHF  Bug fixes to support PutTimeRef().
 * 05/13/2002  WHF  Added check for trailing slash in Add().
 * 06/12/2002  WHF  Added GetServerList() method.
 * 08/07/2002  WHF  Added serialVersionUID member.
 * 09/23/2002  WHF  Returns concatenated byte array in GetData for 
 *						several byte arrays.
 * 09/24/2002  WHF  Removed 1.2 dependencies.
 * 10/24/2002  INB  Added server TOD time stamping.
 * 11/13/2002  WHF  Added produceRequest() method, removed dataReq member.
 * 01/23/2003  WHF  Added GetSinkList(), GetSourceList().
 * 01/30/2003  INB  Copy Mime type on PutDataRef.
 * 02/28/2003  WHF  Added shortcut type strings to TypeID.
 * 09/28/2004  JPW  To make the J# compiler happy, initialize
 *			variable dataString to null in Search()
 * 2005/03/22  WHF  Added GetUserInfo, PutUserInfo.
 * 2005/03/31  WHF  Added support to GetUserInfo for a data ChannelMaps.
 * 2005/08/25  WHF  Repaired serialization support.
 * 2005/09/01  EMF  Added serializaton methods, so just underlying RMap is saved.
 * 2007/04/03  WHF  Added AddPlugInOption().
 * 2007/11/15  WHF  Added GetDataAsArray().
 */

public class ChannelMap implements java.io.Serializable
{
//////////////////  Public Constants: //////////////////////
	// Byte order enumerations.  Note that we have to repeat each
	//  type declaration in order for JavaDoc to work properly!
	/**
	 * Least significant bytes are first in each point, ala Intel.
	 */
	public static final ByteOrderEnum
		LSB= new ByteOrderEnum(DataBlock.ORDER_LSB);
	/**
	 * Most significant bytes are first in each point, as is the case
	 *  with many UNIX machines, and Java data on export.
	 */
	public static final ByteOrderEnum
		MSB = new ByteOrderEnum(DataBlock.ORDER_MSB);
	/**
	 * The data is in whichever format is appropriate for this NATIVE
	 *  machine.  Please note that this is not appropriate for Java
	 *  generated data.
	 */
	public static final ByteOrderEnum
		LOCAL = new ByteOrderEnum(determineLocalByteOrder());

	public static final int
		TYPE_FLOAT64   = DataBlock.TYPE_FLOAT64,
		TYPE_FLOAT32   = DataBlock.TYPE_FLOAT32,
		TYPE_INT64     = DataBlock.TYPE_INT64,
		TYPE_INT32     = DataBlock.TYPE_INT32,
		TYPE_INT16     = DataBlock.TYPE_INT16,
		TYPE_INT8      = DataBlock.TYPE_INT8,
		TYPE_STRING    = DataBlock.TYPE_STRING,
		TYPE_UNKNOWN   = DataBlock.UNKNOWN,
		TYPE_BYTEARRAY = DataBlock.TYPE_BYTEARRAY,
		TYPE_USER      = DataBlock.TYPE_USER;

	/**
	  * Default constructor.
	  */
	  
	// 05/02/2003  WHF  Added debug capability.
	public ChannelMap() 
	{
		initVariables();
		boolean temp=false;
		try {
			temp=Boolean.getBoolean("com.rbnb.sapi.ChannelMap.debug");
		} catch (SecurityException se) 
		{  } // not allowed, no debug.
		debugFlag=temp;
		//debugFlag=true;
	}

	/**
	  * Adds a channel (or channels, if a wildcard is used) to the
	  *  acqusition list.  Parameter <code>channelName</code>
	  *  must be fully qualified name of the channel, including the
	  *  server name.
	  * <p> If the channel is already present, its current index is 
	  *     returned, and no other action is taken.
	  * <p> <strong>Note:</strong> When making requests of PlugIns, 
	  *   a double slash should be used when signalling the PlugIn to 
	  *   forward or filter an absolute channel.  For example, use:<br/>
	  *   <center>"PlugInTest//Server/rbnbSource/c0" </center><br/>
	  *   to filter or forward absolute channel "/Server/rbnbSource/c0".
	  *  If the slash is omitted, the extra portion of the name will be
	  *   interpreted as a relative name.
	  * <p>
     *
     * @author WHF
     *
     * @return The index which refers to this channel.
     * @exception SAPIException If there is a problem parsing the channel name.
	 * @exception NullPointerException If the channel name is null.
	 * @exception IllegalArgumentException If the channel name ends in a 
	 *    slash "/".
     * @see #Clear()
     * @see #PutData(int,byte[],int,ChannelMap.ByteOrderEnum)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 06/05/2001  WHF  Removed serverName omission capability.
     * 11/05/2001  INB  Use new Rmap.addChannel method.
     * 02/04/2002  WHF  Added data markers to sink hierarchy for use
     *		as registration map.
     * 02/05/2002  WHF  Removed datamarkers.  Now done in PlugIn.Register.
     * 04/05/2002  WHF  Changed documentation to reflect fact that index
     *	is returned.
	 * 05/13/2002  WHF  Added check for trailing slash name.
     * 05/02/2003  WHF  Added overrideable addIfFolder() method.
	 * 05/09/2003  WHF  Moved folder code to AddFolder().
     */
	public final int Add(String channelName) throws SAPIException
	{
/*		if (channelName.endsWith("/"))
			addIfFolder(channelName);
*/
		Channel ch=(Channel) channelMap.get(channelName);
		if (ch==null)
		{
			ch=new Channel(
				channelList.size(),
				channelName,
				null);
//			channelList.add(ch);
			channelList.addElement(ch);
			channelMap.put(channelName,ch);
			return channelList.size()-1;
		}
		return ch.index;
	}
	
	
	/**
	  * Add a folder to this channel map.  Useful primarily for registration.
	  * The name may end in a slash '/', but this is not required.
	  * <p>
	  * @exception SAPIException If there is a problem parsing the channel name.
	  * @exception NullPointerException If the channel name is null.
	  * @see Source#Register(ChannelMap)
	  * @author WHF
	  * @since V2.1
	  */
	public final void AddFolder(String channelName) throws SAPIException
	{
		addIfFolder(channelName);
	}

	/**
	  * Method to handle folders, which may be overridden by 
	  *  PlugInChannelMap.
	  */
	void addIfFolder(String channelName) throws SAPIException
	{
		try {
		if (channelName.charAt(channelName.length()-1) == Rmap.PATHDELIMITER)
			// remove trailing slash, or a "" rmap will be produced.
			channelName=channelName.substring(0,channelName.length()-1); 
		
		if (folderMappings.containsKey(channelName)) return;
		// add channel get the root of the new hierarchy:
//		folderRmaps=folderRmaps.addChannel(channelName).moveToTop();
		baseFrame.addChannel(channelName);
		folderMappings.put(channelName, channelName);
		
		} catch (Exception e) { throw new SAPIException(e); }
	}
	
	/**
	  * Used in request maps, adds an option to configure a PlugIn's response
	  *   to this request.
	  * <p>For example, if you have a ResamplePlugIn running with the name
	  *  <b>resample</b> and you want to get a maximum of 1200 points from
	  *  each channel in the request, you would use:
	  * <p><center><code>
	  *   channelMap.AddPlugInOption("resample", "maxSamples", "1200")
	  *  </code></center></p>
	  * <p>If you wanted to then display a plot of the data through 
	  * an instance of PNGPlugIn called png, with a width of 640 and 
	  *  a height of 480, you would instead use:
	  * <p><center><code>
	  *   channelMap.AddPlugInOption("png/resample", "maxSamples", "1200")
	  *  </code></center></p>
	  * <p><center><code>
	  *   channelMap.AddPlugInOption("png", "width", "640")
	  *  </code></center></p>	  
	  * <p><center><code>
	  *   channelMap.AddPlugInOption("png", "height", "480")
	  *  </code></center></p>	  
	  * <p>
	  * @param channel  The fully qualified channel representing the 
	  *   PlugIn to configure.
	  * @param key      The case-sensitive name of the option to set.
	  * @param value    The value, a string, of the option to set.
	  * <p>
	  * @author WHF
	  * @since V3.0B4
	  */
    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/03/2007  WHF	Created.
     */
	public final void AddPlugInOption(String channel, String key, String value)
		throws SAPIException
	{
		PutDataAsString(Add(channel+"/."), key+"="+value);
	}
	
	/**
	  * Removes all channels from the acquisition list.  Also clears
	  *   any cached channel memory which may have been retained for
	  *   efficiency.
	  * <p>
     *
     * @author WHF
     *
     * @return None.
     * @see #Add(String)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 01/17/2002  WHF  Clears channel map as well as vector.
     *
     */
	public final void Clear()
	{
//		channelList.clear();
		channelList.removeAllElements();
		channelMap.clear();
		wasTimeout=false;
//		folderRmaps=new Rmap();
		folderArray=new String[0];
		folderMappings.clear();
		
		try {
		clearData();
//		dataReq=new DataRequest();
		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	  * Yields the times for each data point for the specified channel.
	  *  A reference to the double array stored in this map is returned,
	  *   which should not be modified by the end user.
	  * <p>The array returned is calculated only when this method is first
	  *   called on the channel index.  The same array is returned on 
	  *   successive calls.
	  * <p>A zero length array is returned in cases where no time is available
	  *  for this channel.
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return An array of doubles representing the times, or null if no 
     *	data is available.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
	 * 05/21/2002  WHF  Returns zero-length array instead of null.
     *
     */
	public double[] GetTimes(int index)
	{
		double[] times=((Channel) channelList.elementAt(index)).dArray.getTime();
		if (times==null) return new double[0];
		return times;
	}

	/**
	  * Yields the start time of this channel's data.  Should only be 
	  *   on the map resulting from a <code>Fetch()</code>.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The start time for this channel's data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @see #GetData(int)
     * @see #GetTimeDuration(int)
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  WHF	Created.
     *
     */
	public double GetTimeStart(int index)
	{
		return ((Channel) channelList.elementAt(index)).dArray
			.getStartTime();
	}

	/**
	  * Yields the duration of this channel's data.  Should only be 
	  *   on the map resulting from a <code>Fetch()</code>.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The start time for this channel's data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @see #GetData(int)
     * @see #GetTimeStart(int)
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  WHF	Created.
     *
     */
	public double GetTimeDuration(int index)
	{
		return ((Channel) channelList.elementAt(index)).dArray.getDuration();
	}

	/**
	  * Determines the type of the data in the array, and then converts it
	  *  to an array of bytes in local machine order.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data as raw bytes.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @see #GetTimes(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 09/23/2002  WHF  Concatenates byte arrays into single large byte array.
     * 12/17/2002  INB	Concatenates string arrays into a single large byte
     *			array.
     *
     */
	public byte[] GetData(int index) //throws Exception
	{
		Object data=getData(index);
		if (data == null) {
			return null;
		}

		Class cl=data.getClass();
		if (cl==double[].class)
			return double2Byte((double[]) data);
		else if (cl==float[].class)
			return float2Byte((float[]) data);
		else if (cl==long[].class)
			return long2Byte((long[]) data);
		else if (cl==int[].class)
			return int2Byte((int[]) data);
		else if (cl==short[].class)
			return short2Byte((short[]) data);
		else if (cl==String.class)
			return ((String) data).getBytes();
		else if (cl==String[].class)
		{
			String[] sdata = (String[]) data;
			int length = 0;
			byte[][] arrays = new byte[sdata.length][];
			for (int idx = 0; idx < sdata.length; ++idx) {
				arrays[idx] = sdata[idx].getBytes();
				length += arrays[idx].length;
			}
			byte[] arrayR = new byte[length];
			length = 0;
			for (int idx = 0; idx < sdata.length; ++idx) {
				System.arraycopy(arrays[idx],
						 0,
						 arrayR,
						 length,
						 arrays[idx].length);
				length += arrays[idx].length;
			}
			return (arrayR);
		}
		else if (cl==byte[][].class) // array of byte arrays
		{ // build a single byte array containing all data:
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			try {
			byte[][] bData=(byte[][]) data;
			for (int ii=0; ii<bData.length; ++ii)
				baos.write(bData[ii]);
			} catch (java.io.IOException ioe) // unlikely
			{ ioe.printStackTrace(); }
			return baos.toByteArray();
		}

		return (byte[]) data;
	}

	/**
	  * Returns the channel data as an array of doubles.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public double[] GetDataAsFloat64(int index) //throws Exception
	{ return (double[]) getData(index); }

	/**
	  * Returns the channel data as an array of floats.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public float[] GetDataAsFloat32(int index) //throws Exception
	{ return (float[]) getData(index); }

	/**
	  * Returns the channel data as an array of longs.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public long[] GetDataAsInt64(int index) //throws Exception
	{ return (long[]) getData(index); }

	/**
	  * Returns the channel data as an array of ints.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public int[] GetDataAsInt32(int index) //throws Exception
	{ return (int[]) getData(index); }

	/**
	  * Returns the channel data as an array of shorts.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public short[] GetDataAsInt16(int index) //throws Exception
	{ return (short[]) getData(index); }

	/**
	  * Returns the channel data as an array of bytes.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public byte[] GetDataAsInt8(int index) //throws Exception
	{ return (byte[]) getData(index); }

    /**
     * Returns the channel data as an array of Strings.
     * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
     * @since V2.0
     * @version 08/31/2001
     */

    /*
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     * 08/28/2001  WHF  Deprecated.
     * 08/31/2001  INB	Eliminated deprecation; routine now returns an array of
     *			strings, making it useful and compatible with the other
     *			methods.
     */
      public String[] GetDataAsString(int index)
      { return ((String[]) getData(index)); }

	/**
	  * Returns the channel data as an array of arrays of bytes.  This is most
	  *   useful when the data, instead of a time series of bytes (int8), is
	  *   in fact an object, such as a file or files, which exist all at one
	  *   particular time stamp.
	  * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return The data.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @exception ClassCastException Channel data is not the correct type.
     * @see #GetData(int)
	 * @see #GetDataAsInt8(int)
     * @since V2.0
     * @version 09/13/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
    public byte[][] GetDataAsByteArray(int index) 
    { return (byte[][]) getData(index); }
	
	/**
	  * Returns the channel data as an untyped Object.  The 
	  *   java.lang.reflect.Array class may then be used to extract doubles.
	  *  This is most useful when writing PlugIns which must deal with 
	  *  multiple input data types.
      * @author WHF
      *
      * @param index The channel index.
	  * @return The data.
	  * @exception ArrayIndexOutOfBoundsException If index out of bounds.
	  * @since V3.1
	  * @version 2007/11/15
	  */	
	public Object GetDataAsArray(int index)
	{ return getData(index); }

    /**
     * Get the fully qualified channel name from its index in constant time.
     * <p>
     *
     * @author WHF
     *
     * @param index The channel index.
     * @return Its corresponding name.
     * @exception ArrayIndexOutOfBoundsException If the index is out of bounds.
     * @see #GetIndex(String)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public String GetName(int index)
	{
		return ((Channel) channelList.elementAt(index)).name;
	}

	/**
	  * Returns the index of a channel which has been added to this map
	  *  if given its name.  This search takes approximately constant time.
	  *  Note that the value returned by this function will change with
	  *  each call to <code>Fetch()</code>, even if this map is reused,
	  *  and should not be stored.
	  * <p>
     *
     * @author WHF
     *
     * @param channelName The fully qualified channel name, including the server name.
     * @return The corresponding channel index, or -1 if not found.
     * @see #GetName(int)
     * @since V2.0
     * @version 10/05/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/09/2001  WHF  Verifies that retrieved index actually matches,
			which is still faster than deallocating all channel
			structures, or using a linear search.
     * 10/05/2001  WHF  Determines Fetched index from a given channel name.
     * 06/07/2001  WHF  Fixed bug that was result of caching technique.
     * 06/05/2001  WHF  Removed server name omission capability.
     * 05/17/2001  WHF	Created.
     *
     */
	public final int GetIndex(String channelName)
	{
		Channel ch=(Channel) channelMap.get(channelName);
		if (ch==null) return -1;
		else return ch.index;
	}

	/**
	  * Returns the type of the data returned by the specified channel.
	  * Note that this function never returns TYPE_USER.  The reason for this
	  *  is that a registration channel map which does contain user info will
	  *  also contain server meta-data, and thus is of the more general type
	  *  TYPE_STRING.  To test if user info is available, call GetUserInfo, 
	  *  which will return an empty string if there is none.
	  * <p>
     *
     * @author WHF
     *
     * @param index The desired channel index.
     * @return The corresponding data type ID.
     * @exception ArrayIndexOutOfBoundsException If index out of bounds.
     * @see <a href="#TYPE_FLOAT64"><code>TYPE_FLOAT64</code></a>
     * @since V2.0
     * @version 04/09/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/09/2001  WHF	Created.
     * 05/24/2001  WHF  Returns TYPE_UNKNOWN if channel contains no data.
	 * 2005/03/31  WHF  Added TYPE_USER check under byte array data.
     *
     */
	public final int GetType(int index)
	{
		try {
			Object data=getData(index);
	
			if (data==null) return TYPE_UNKNOWN;
	
			Class cl=data.getClass();
			if (cl==double[].class)
				return DataBlock.TYPE_FLOAT64;
			else if (cl==float[].class)
				return DataBlock.TYPE_FLOAT32;
			else if (cl==long[].class)
				return DataBlock.TYPE_INT64;
			else if (cl==int[].class)
				return DataBlock.TYPE_INT32;
			else if (cl==short[].class)
				return DataBlock.TYPE_INT16;
		// mjm/inb 5/25/01 changed "String" to "String[]"
			else if (cl==String[].class)
				return DataBlock.TYPE_STRING;
			else if (cl==byte[][].class) {
				return ((Channel) channelList.elementAt(index))
						.dArray.getDataType();
			}
		} catch (Exception e)
		{ e.printStackTrace(); }
		return DataBlock.TYPE_INT8;
	}

	/**
	  * Returns the MIME type identifier for this channel, if any.
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  WHF	Created.
     *
     */
	public final String GetMime(int index)
	{
		return ((Channel) channelList.elementAt(index)).dArray.getMIMEType();
	}


	/**
	  * Returns true if and only if this channel map was returned in
	  *  reponse to a <code>Fetch()</code> which timed-out.  
	  * <p>A call to {@link Sink#Fetch(long)} may return a <code>
	  *  ChannelMap</code> with no channels.  This method allows you to 
	  *  determine whether this map is empty because the request timed out,
	  *  or because the request made matches no data on the server.
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  WHF	Created.
     *
     */	
	public final boolean GetIfFetchTimedOut()
	{ return wasTimeout; }
	
	/**
	  * Returns user information for the specified channel, if any is available.
	  *  <p>User information corresponds to the string in server registration
	  *  data
	  * between the &lt;user&gt; and &lt;/user&gt; tags.  It is placed into the
	  *  server by registering a ChannelMap containing one or more channels to
	  *  which {@link #PutUserInfo(int, String)} has been applied.
	  * <p>
	  * @return The user info, or an empty string if the mime type is not
	  *  "text/xml" or the underlying data type is not String.
	  * <p>
	  * @author WHF
	  * @version 2005/03/31
	  * @since V2.5B4
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2005/03/22  WHF	Created.
	 * 2005/03/31  WHF  Added check for TYPE_USER data.
     *
     */	
	public final String GetUserInfo(int channel)
	{
	    
	    /////////////////////////////////////////////////////////////////
	    //
	    // JPW 03/13/2007
	    // The same tests as are done here for determining if there is
	    // USER data are also done in com.rbnb.api.MirrorController.setRegistration();
	    // therefore, if a change is made here, should also check that
	    // API method for making the same changes.  Alternately, make a
	    // Utility method which performs these tests and call the Utility
	    // method from both places.
	    //
	    ////////////////////////////////////////////////////////////////
	    
		// Check to see if we have USER data.  This is the result of a user
		//  calling PutUserInfo on a data channel map that was then flushed
		//  to the server.
		if (GetType(channel) == TYPE_USER) {
			StringBuffer sb = new StringBuffer();
			byte[][] dataArray = GetDataAsByteArray(channel);
			for (int ii = 0; ii < dataArray.length; ++ii) {
				// TODO: Should use a specific encoding:
				sb.append(new String(dataArray[ii]));
			}
			return sb.toString();		
		// Otherwise, check to be sure we have a server-meta data packet:
		} else if (!"text/xml".equals(GetMime(channel))
				|| GetType(channel) != TYPE_STRING) 
			return "";
			
		StringBuffer sb = new StringBuffer();
		String[] dataArray = GetDataAsString(channel);
		for (int ii = 0; ii < dataArray.length; ++ii) {
			String data = dataArray[ii];
			int index = data.indexOf("<user>");
			if (index >= 0) {
				int index2 = data.lastIndexOf("</user>");
				if (index2 >= 0)
					sb.append(data.substring(index+6, index2));
			}
		}
		return sb.toString();
	}
	
	/**
	  * Specifies the MIME (Multipurpose Internet Mail Extensions)
	  *  type of this source data.  Typical MIME types include 
	  *  "text/plain", "text/xml", for String data, and 
	  *  "application/octet-stream" for binary data.
	  * <p>A full list can be found at:
	  * <blockquote><a href="ftp://ftp.isi.edu/in-notes/iana/assignments/media-types/media-types">
	  *  ftp://ftp.isi.edu/in-notes/iana/assignments/media-types/media-types</a>
	  *  </blockquote>
	  * <p><strong>NOTE:</strong> The MIME type information will only be sent
	  *		to the server if one of the <code>PutData()</code> methods is
	  *     called on this channel.
	  * <p>
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  WHF	Created.
	 * 05/03/2002  WHF  PutMime on all nodes under the rmap.  Needed due to
	 *	PutTimeRef case.
     *
     */
	public void PutMime(int index, String mime)
	{
		Channel ch=((Channel) channelList.elementAt(index));
		if (ch.rmap==null) // case where putData not yet called
			ch.mime=mime;
		else try {
			Client.forEachNode(ch.rmap,new SetMimeAction(mime));
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) // shouldn't happen
		{ e.printStackTrace(); }
			
	}

	/**
	  * Inner utility class for PutMime().
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/03/2002  WHF	Created.
     *
     */
	private static class SetMimeAction implements Client.Action
	{
		private String mime;
		public SetMimeAction(String mime) { this.mime=mime; }

		public void doAction(Object o) throws Exception
		{
			Rmap r=(Rmap) o;
			if (r.getDblock()!=null)
				r.getDblock().setMIMEType(mime);
		}
	}

	/**
	  * Determines the numerical constant used as the type ID
	  *  for the specified data type.  This function is intended primarily
	  *  for use by the COM interface; Java users should use the 
	  *  appropriate <code>public static final int TYPE_*</code>
	  *  constants.
	  * <p>
	  * The String should be one of "int8", "int16", "int32", "int64"
	  *  "float32", "float64", "string", "bytearray", or "unknown".
	  *  Case is ignored.
	  * For example, calling this function with "int8" returns 
	  *  {@link #TYPE_INT8}.
	  * <p>
	  * @throws IllegalArgumentException if the type is not one of the ones
	  *  listed above;
	  * @throws NullPointerException if <code>type</code> is null.
	  */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  WHF	Created.
	 * 02/28/2003  WHF  Added shortcut type strings.
     */
	public int TypeID(String type)
	{
		type=type.toLowerCase();
		if ("int8".equals(type)||"i8".equals(type)) return TYPE_INT8;
		if ("int16".equals(type)||"i16".equals(type)) return TYPE_INT16;
		if ("int32".equals(type)||"i32".equals(type)) return TYPE_INT32;
		if ("int64".equals(type)||"i64".equals(type)) return TYPE_INT64;
		if ("float32".equals(type)||"f32".equals(type)) return TYPE_FLOAT32;
		if ("float64".equals(type)||"f64".equals(type)) return TYPE_FLOAT64;
		if ("string".equals(type)||"s".equals(type)) return TYPE_STRING;
		if ("user".equals(type)) return TYPE_USER;
		if ("unknown".equals(type)) return TYPE_UNKNOWN;
		if ("bytearray".equals(type)||"b".equals(type)||"ba".equals(type))
			return TYPE_BYTEARRAY;
		throw new IllegalArgumentException(
			"Type unrecognized.");
	}

	/**
	  * Returns a string type identifier which matches the given typeID.
	  * <p>
	  * @exception ArrayOutOfBoundsException If the typeID does not match
	  *  any of the allowed types.
	  */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/28/2002  WHF	Created.
     */
	public String TypeName(int typeID)
	{
		return types[typeID];
	}

	/**
	  * Adds the data given in rawBytes to the channel identified with
	  *  the specified channel index.  This assumes
	  *  that the byte ordering of the data is the same as the local 
	  *  system.  (Not Java byte ordering, which is always MSB!)
	  * <p>
     *
     * @author WHF
     *
     * @param channelIndex The channel index.
     * @param rawData An array of bytes representing the data.
     * @param typeID An integer representing the type of the data.
     * @return None.
     * @exception SAPIException If there is an error creating the channel locally.
     * @see Source#Flush(ChannelMap)
     * @see #PutData(int,byte[],int,ChannelMap.ByteOrderEnum)
     * @since V2.0
     * @version 08/16/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/16/2001  WHF	Created.
     *
     */
	public void PutData(int channelIndex, byte[] rawData, int typeID)
		throws SAPIException
	{ PutData(channelIndex,rawData,typeID,LOCAL); }


	/**
	  * Adds the data given in rawBytes to the channel identified with
	  *  the specified channel index.
	  * <p>
	  * Note that this version of PutData is not available in the 
	  *   JavaBean/ActiveX API.
	  *  
     *
     * @author WHF
     *
     * @param channelIndex The index of the channel, obtained via 
     *  {@link #Add(String)} or {@link #GetIndex(String)}.
     * @param rawData An array of bytes representing the data.
     * @param typeID An integer representing the type of the data.
     * @param byteOrder An enumerated type, one of <a href=#LOCAL>LOCAL</a>,
     *  <a href=#LSB>LSB</a>, or <a href=#MSB>MSB</a>.
     * @return None.
     * @exception SAPIException If there is an error creating the channel locally.
     * @see Source#Flush(ChannelMap)
     * @see #Add(String)
     * @see #GetIndex(String)
     * @since V2.0
     * @version 08/31/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/16/2001  WHF	Created.
     * 08/31/2001  INB	Added check for type string; strings are 1xN rather
     *			than Nx1.
     */
	public void PutData(int channelIndex, byte[] rawData, int typeID,
		ByteOrderEnum byteOrder)
		throws SAPIException
	{
		int size=
		    (typeID == TYPE_STRING || typeID == TYPE_BYTEARRAY 
					|| typeID == TYPE_USER) ?
		    rawData.length :
		    getSize(typeID);

		try {
		putData(channelIndex,rawData,rawData.length/size,size,
			(byte)typeID,byteOrder.getByte());
		} catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsFloat64(int channelIndex, double[] data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,data.length,8,
			DataBlock.TYPE_FLOAT64,DataBlock.ORDER_MSB);

		} catch (Exception e) { throw new SAPIException(e); }
	}


	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsFloat32(int channelIndex, float[] data)
		throws SAPIException
	{
		try {
		putData(channelIndex,data,data.length,4,
			DataBlock.TYPE_FLOAT32,DataBlock.ORDER_MSB);

		} catch (Exception e) { throw new SAPIException(e); }
	}


	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsInt64(int channelIndex, long[] data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,data.length,8,
			DataBlock.TYPE_INT64,DataBlock.ORDER_MSB);

		} catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsInt32(int channelIndex, int[] data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,data.length,4,
			DataBlock.TYPE_INT32,DataBlock.ORDER_MSB);

		} catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsInt16(int channelIndex, short[] data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,data.length,2,
			DataBlock.TYPE_INT16,DataBlock.ORDER_MSB);

		} catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsInt8(int channelIndex, byte[] data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,data.length,1,
			DataBlock.TYPE_INT8,DataBlock.ORDER_MSB);

		} catch (Exception e) { throw new SAPIException(e); }
	}

	/**
	  * Type safe version of PutData().
	  * @see #PutData(int,byte[],int)
	  *
	*/
	public void PutDataAsString(int channelIndex, String data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,1,data.length(),
			DataBlock.TYPE_STRING,DataBlock.ORDER_MSB);
		} catch (Exception e) { throw new SAPIException(e); }
	}


	/**
	  * Type safe version of PutData().  Places a block of bytes into the
	  *   ChannelMap as a contiguous object.
	  * @see #PutData(int,byte[],int)
	*/
	public void PutDataAsByteArray(int channelIndex, byte[] data)
		throws SAPIException
	{
		try { 
		putData(channelIndex,data,1,data.length,
			DataBlock.TYPE_BYTEARRAY,DataBlock.ORDER_MSB);
		} catch (Exception e) { throw new SAPIException(e); }
	}
	
	/**
	  * Specify user information for a given channel.  For use with server
	  *  registration, this method signals the server to place the provided
	  *  string inside the &lt;user&gt; and &lt;/user&gt; tags of the server
	  *  XML file.  This is prefered to placing data directly in the 
	  *  registration map, as that overrides the server registration data.
	  * <p>
	  * This method will work along with {@link #GetUserInfo(int)} with data
	  * (non registration) ChannelMaps.  However, {@link #PutDataAsString(
	  * int, String)}/{@link #GetDataAsString(int)} has more functionality in 
	  *  this case and is recommended.
	  * <p>
	  * @see Sink#RequestRegistration()
	  * @see Source#Register(ChannelMap)
	  * @author WHF
	  * @version 2005/03/22
	  * @since V2.5B4
	  */
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2005/03/22  WHF	Created.
     *
    */	
	public final void PutUserInfo(int channelIndex, String data)
			throws SAPIException
	{
		try {
		putData(channelIndex, data, 1, data.length(),
			DataBlock.TYPE_USER, DataBlock.ORDER_MSB);
		} catch (Exception e) { throw new SAPIException(e); }
	}

	/** 
	  * Transfers a reference of the data in the sourceMap to <code>this</code>
	  *  map. The primary application of this method is in "pass-through"
	  *  PlugIns, such as VSource, which do nothing with the data other than
	  *  transfer it from one channel to another.
	  * <p>
	  * @throws IndexOutOfRange If either <code>destChannel >= 
	  *  this.NumberOfChannels()</code>
	  *  or <code>sourceChannel >= sourceMap.NumberOfChannels()</code>.
	  * @throws SAPIException If there is a problem transferring the data.
	  * @author WHF
	  * @see #PutTimeRef(ChannelMap, int)
	  * @since V2.0B10
	 */
	public void PutDataRef(int destChannel, ChannelMap sourceMap, 
		int sourceChannel) throws SAPIException
	{
		// INB 01/30/2003
		// Ideally, if this routine is used in combination with
		// PutTimeRef, we probably should copy Rmaps from the source to
		// the destination rather than going through DataArray.
		Channel ch=((Channel) sourceMap.channelList.elementAt(sourceChannel));
		Object data=ch.dArray.getData();
		byte type=(byte) sourceMap.GetType(sourceChannel);
		int	size, pts = ch.dArray.getNumberOfPoints();

		// INB 01/30/2003
		// The following uglyness is because arrays of arrays are not
		// handled properly by the full API code. So we unroll the
		// data.
		if (pts != 1 && (type == TYPE_USER || type == TYPE_STRING ||
				type == TYPE_BYTEARRAY)) {
			unrollDataRef(destChannel,
				      data,
				      type,
				      pts,
				      sourceMap.GetMime(sourceChannel));

		} else {
			switch (type) {
				case TYPE_USER:
				case TYPE_STRING:
					data = ((String[]) data)[0];
					size=((String) data).length();
					pts=1;
					break;

				case TYPE_BYTEARRAY:
					data = ((byte[][]) data)[0];
					size=((byte[]) data).length;
					pts=1;
					break;
			
				default: 
					size=getSize(type);
					pts=ch.dArray.getNumberOfPoints();
			}

			try {
				putData(destChannel, data, pts, size, type,
					DataBlock.ORDER_MSB);
				// INB 01/30/2003
				PutMime(destChannel,
					sourceMap.GetMime(sourceChannel));
			} catch (Exception e) { throw new SAPIException(e); }
		}
	}

	/**
	 * This method unrolls a data reference containing either an array of
	 * byte arrays or strings into multiple PutTime/PutData calls.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @param destChannelI the destination channel index.
	 * @param dataI	       the data.
	 * @param typeI	       the data type.
	 * @param ptsI	       the number of points.
	 * @param mimeI	       the MIME type.
	 * @exception SAPIException
	 *	      If there is a problem transferring the data.
	 * @since V2.0
	 * @version 01/30/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/30/2003  INB	Created.
	 * 2007/03/23  WHF  Fixed problem when forwarding String/ByteArray data
	 *     without calling PutTimeRef.
	 *
	 */
	private final void unrollDataRef(int destChannelI,
					 Object dataI,
					 byte typeI,
					 int ptsI,
					 String mimeI)
	    throws SAPIException
	{
		// Save the time information so that we can restore it.
		int oldTimeMode = timeMode;
		DataArray oldTimeReference = timeReference;
		double oldStart = start,
		    oldDuration = duration;
		double[] oldPointTimes = pointTimes;
		double[] theTimes;
		double lDuration = 0.;

		if (timeMode == REFERENCE) {
			theTimes = oldTimeReference.getTime();
		} else {
			TimeRange tr = getTR();
			// 2007/03/23  WHF  Added next two lines:
			if (tr == null) theTimes = null;
			else {
				theTimes = new double[ptsI];
				tr.copyTimes(ptsI, theTimes, 0);
				if (tr.getDuration() != 0.) {
					lDuration = tr.getDuration()/ptsI;
				}
			}
		}

		for (int idx = 0; idx < ptsI; ++idx) {
			// 2007/03/23  WHF  Added next line:
			if (theTimes != null)
				PutTime(theTimes[idx], lDuration);
			if (dataI instanceof String[]) {
				PutDataAsString(destChannelI,
						((String[]) dataI)[idx]);
			} else if (dataI instanceof byte[][]) {
				PutDataAsByteArray(destChannelI,
						   ((byte[][]) dataI)[idx]);
			}
		}
		PutMime(destChannelI, mimeI);

		timeMode = oldTimeMode;
		timeReference = oldTimeReference;
		start = oldStart;
		duration = oldDuration;
		pointTimes = oldPointTimes;
	}


	/**
	  * Sets the time range for all data subsequently added to the channel
	  *  map.  This function may be called once per frame, once per 
	  *  channel, or once per point for a multi-point block, depending
	  *  on your application.
	  * <p>Clears the AutoTimeStamp setting, if any.
	  * <p>
     *
     * @author WHF
     *
     * @param start The absolute start time of the next frame.
     * @param duration The duration of the next frame.
     * @return None.
     * @see #PutTimeAuto(String)
     * @see #PutData(int,byte[],int)
     * @see Source#Flush(ChannelMap,boolean)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public void PutTime(double start, double duration)
	{
		timeMode=MANUAL;
		this.start=start;
		this.duration=duration;

		onTimeModeSet();
	}

	/**
	  * Sets the time vector for all data subsequently added to the channel
	  *  map.  This function may be called once per frame, once per 
	  *  channel, or once per point for a multi-point block, depending
	  *  on your application.  The array should have the same number of
	  *  points as the data subsequently added, or else an exception
	  *  will be thrown on the <code>PutData</code> call.
	  * <p>Clears the AutoTimeStamp setting, if any.
	  * <p>
     *
     * @author WHF
     *
     * @param times The array of time points that applies to the data set.
     * @return None.
     * @see #PutTimeAuto(String)
     * @see #PutData(int,byte[],int)
     * @see Source#Flush(ChannelMap,boolean)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public void PutTimes(double[] times)
	{
		if (times!=null&&times.length==0) times=null;
		pointTimes=times;
		timeMode=ARRAY;
		onTimeModeSet();
	}


	/**
	  * Copies the time reference for the specified channel on the
	  *  ChannelMap provided into the current time buffer for this
	  *  channel.  This is done in the most efficient way possible,
	  *  usually much better than:<blockquote>
	  *  <code>PutTimes(sourceMap.GetTimes(channelIndex))</code>
	  *  </blockquote>
	  * <p>This function may be called once per frame, once per 
	  *  channel, or once per point for a multi-point block, depending
	  *  on your application.  The time reference must be compatable 
	  *  with the data subsequently added, or else an exception
	  *  will be thrown on the <code>PutData</code> call.
	  * <p>Clears the PutTimeAuto setting, if any.
	  * <p>
     *
     * @author WHF
     *
     * @param sourceMap The map from which to copy the time reference.
     * @param channelIndex The index of the channel whose reference to copy.
     * @see #PutTimeAuto(String)
     * @see #PutTimes(double[])
     * @see #PutData(int,byte[],int)
     * @see Source#Flush(ChannelMap,boolean)
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2002  WHF	Created.
     * 04/15/2002  WHF  Implemented.
     *
     */
	public void PutTimeRef(ChannelMap sourceMap, int channelIndex)
	{
		timeReference=((Channel) sourceMap.channelList.elementAt(
			channelIndex)).dArray;
		timeMode=REFERENCE;

		onTimeModeSet();

		/*
		throw new UnsupportedOperationException(
			"Not yet implemented.");
		*/
	}

	/**
	  * Sets the mode for automatic time stamp generation that will be used
	  *  on the next call to Flush(). Case is not significant.
	  * <p>
	  * If the <code>timeMode</code> is "next", then the time-stamp
	  * increments starting from 1 each time <code>Source.Flush</code> is
	  * called.
	  * <p>
	  * If the <code>timeMode</code> is "timeofday", then the time-stamp is
	  * set equal to the client-side system clock time each time
	  * <code>PutData</code> is called.
	  * <p>
	  * If the <code>timeMode</code> is "server", then the time-stamp is
	  * set equal to the server-side system clock time at the point when
	  * the flushed frame is placed in the ring buffer.  This mode should
	  * not be used in conjunction with any other time-stamping style
	  * within a single frame, although it can be changed from flush to
	  * flush.
	  * <p>
	  * If the mode is not set, it defaults to "server".
	  * <p>
	  * The default units for auto timestamps are seconds since 1970,
	  * stored in a double precision number (System.currentTimeMillis()/1000.).
	  * Note that this results in approximately 1 micro-second time resolution.
	  * For higher resolution, use manual (non-auto) timestamps, e.g.
	  * use a relative (zero-based) start time instead of 1970.
	  * <p>
     *
     * @author WHF
     *
     * @param timeMode The mode setting, one of "next", "timeofday", or
     *		       "server".
     * @return None.
     * @exception IllegalArgumentException If the mode is not correct.
     * @see com.rbnb.sapi.Source#Flush(ChannelMap,boolean)
     * @since V2.0
     * @version 05/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/17/2001  WHF	Created.
     *
     */
	public void PutTimeAuto(String timeMode) 
		throws IllegalArgumentException
	{
		if (timeMode!=null)
		{
			timeMode=timeMode.toLowerCase();
			if (timeMode.equals("next"))
				this.timeMode=NEXT;
			else if (timeMode.equals("timeofday"))
				this.timeMode=TIMEOFDAY;
			else if (timeMode.equals("server"))
				this.timeMode=SERVERTOD;
			else throw new IllegalArgumentException(
				timeStampErrStr);
		}
		else throw new IllegalArgumentException(timeStampErrStr);

		onTimeModeSet();
	}

	/**
	  * Returns the number of channels available in the map.
	  * <p>
     *
     * @author WHF
     *
     * @see #Add(String)
     * @since V2.0
     * @version 01/25/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/25/2002  WHF	Created.
     *
     */
	public int NumberOfChannels()
	{ return channelList.size(); }


	/**
	  * Returns a descriptive <code>String</code> about the state of
	  *  this channel map.
	  * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 02/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/06/2002  WHF	Created.
     *
     */	
	public String toString()
	{
		StringBuffer response=new StringBuffer(this.getClass()
			.getName());
		response.append(" with ").append(channelList.size())
			.append(" channels.");
		for (int ii=0; ii<channelList.size(); ++ii)
			response.append("\n\t").append(channelList.elementAt(ii));

		return response.toString();
	}

	/**
	  * Returns a String array containing one string for every node
	  *  in the channel hierarchy of this map.  If all the channels in
	  *  the map were placed in one or more trees, with the first token
	  *  (using '/' to separate tokens) as the root, the second tokens as
	  *  its children, etc., each node of this tree would be returned 
	  *  by this function.  Each String is as qualified as possible.
	  * <p>For example, if a <code>ChannelMap</code> contains the channel
	  *	"/Server/Source/Channel", the return result would be "/Server",
	  *	"/Server/Source", and "/Server/Source/Channel".  However, if
	  *	the channel was "Source/Channel", only "Source" and
	  *	"Source/Channel" would be returned.
	  * <p>
     *
     * @author WHF
     *
     * @since V2.0
     * @version 04/10/2002
	 *
	 * @deprecated Please use the <code>ChannelTree</code> class for 
	 *  per-node heirarchy construction and identification.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2002  WHF	Created.
     *
     */	
	public final String[] GetNodeList()
	{
		try {
		Rmap r;
		if (response!=null) r=response;
		else 
		{
			if (channelList.size()==0) return new String[0];
			r=Rmap.createFromName(channelList.elementAt(0).toString());
			for (int ii=0; ii<channelList.size(); ++ii)
			    // INB 10/21/2002 - add channel now returns the
			    // Rmap corresponding to the new channel.
			    /*r=r.addChannel(channelList.elementAt(0).toString());
			     */
			    {
				r.addChannel
				    (channelList.elementAt(0).toString());
				r = r.moveToTop();
			    }
		}
//System.err.println("Rmap: "+r);
		Client.GetListAction getList=new Client.GetListAction();

		Client.forEachNode(r,getList);
		return getList.getNames();
	
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }
	}

	/**
	  * Convenience function to return the list of channels contained in
	  * this ChannelMap.  This could be implemented as:
	  * <p><code>ChannelMap map= ...<br>
	  *	String[] list=new String[map.NumberOfChannels()];<br>
	  *	for (int ii=0; ii&lt;list.length; ++ii)<br>
	  *	&nbsp;&nbsp;&nbsp;&nbsp;list[ii]=map.GetName(ii);</code></p>
	  * <p>However, this function is slightly more efficient.</p>
	  * <p>
     *
     * @author WHF
     *
     * @return The list of channels, or a zero length array if no channels
     *   are available.
     * @since V2.0
     * @version 04/10/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/10/2002  WHF	Created.
     *
     */	
	public final String[] GetChannelList()
	{
		String[] list=new String[channelList.size()];
		for (int ii=0; ii<list.length; ++ii)
			list[ii]=channelList.elementAt(ii).toString();
		return list;
	}
	
	/**
	  * Returns the list of <em>empty</em> folders contained within this 
	  *  ChannelMap.  If there are no such folders, an array of length zero
	  *  is returned.  In general there will only be folders in a ChannelMap
	  *  which is the result of a {@link Sink#RequestRegistration(ChannelMap)}.
	  * <p>
	  * @author WHF
	  * @see Sink#RequestRegistration(ChannelMap)
	  * @since V2.1
	  */
	public final String[] GetFolderList() { return folderArray; } 	
	
	/**
	  * Convenience function to return the list of child servers contained in
	  * this ChannelMap.  This is only useful for maps returned from
	  *  a registration request.
	  * <p>
     *
     * @author WHF
     *
     * @return The list of child servers, or a zero length array if no servers
     *   are available.
     * @since V2.0
     * @version 06/12/2002
	 *
	 * @deprecated Please use the <code>ChannelTree</code> class for 
	 *  per-node heirarchy construction and identification.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/12/2002  WHF	Created.
     *
     */	
	public final String[] GetServerList()
	{
		try {
//System.err.println(response);
		if (response==null) return new String[0];

//		Client.GetServersAction getServers=new Client.GetServersAction();
		Client.GetClientsAction getServers=new Client.GetClientsAction(
			com.rbnb.api.Server.class);

		// Search to depth of 2: Unnamed top level, First level current
		//  server, second level children.
//		Client.forEachNodeToDepth(response,getServers,2);
		Client.forEachNode(response,getServers);
		return getServers.getNames();
	
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }
	}

	/**
	  * Returns the list of {@link Sink}s contained in
	  * this ChannelMap.  This is only useful for maps returned from
	  *  a registration request.
	  * <p>
     *
     * @author WHF
     *
     * @return The list of Sinks, or a zero length array if no Sinks
     *   are present.
     * @since V2.0B10
     * @version 01/23/2003
	 *
	 * @deprecated Please use the <code>ChannelTree</code> class for 
	 *  per-node heirarchy construction and identification.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2003  WHF	Created.
     *
     */	
	public final String[] GetSinkList()
	{
		try {
//System.err.println(response);
		if (response==null) return new String[0];

		Client.GetClientsAction getSinks=new Client.GetClientsAction(
			com.rbnb.api.Sink.class);

		Client.forEachNode(response,getSinks);
		return getSinks.getNames();
	
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }
	}

	/**
	  * Returns the list of {@link Source}s contained in
	  * this ChannelMap.  This is only useful for maps returned from
	  *  a registration request.
	  * <p>
     *
     * @author WHF
     *
     * @return The list of Sources, or a zero length array if no Sources
     *   are present.
     * @since V2.0B10
     * @version 01/23/2003
	 *
	 * @deprecated Please use the <code>ChannelTree</code> class for 
	 *  per-node heirarchy construction and identification.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2003  WHF	Created.
     *
     */	
	public final String[] GetSourceList()
	{
		try {
//System.err.println(response);
		if (response==null) return new String[0];

		Client.GetClientsAction getSources=new Client.GetClientsAction(
			com.rbnb.api.Source.class, com.rbnb.api.Sink.class);

		// Search to depth of 2: Unnamed top level, First level current
		//  server, second level children.
//		Client.forEachNodeToDepth(response,getServers,2);
		Client.forEachNode(response,getSources);
		return getSources.getNames();
	
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }
	}


	/**
	  * Returns the list of {@link PlugIn}s contained in
	  * this ChannelMap.  This is only useful for maps returned from
	  *  a registration request.
	  * <p>
     *
     * @author WHF
     *
     * @return The list of PlugIns, or a zero length array if no PlugIns
     *   are present.
     * @since V2.0B10
     * @version 01/23/2003
	 *
	 * @deprecated Please use the <code>ChannelTree</code> class for 
	 *  per-node heirarchy construction and identification.
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2003  WHF	Created.
     *
     */	
	public final String[] GetPlugInList()
	{
		try {
//System.err.println(response);
		if (response==null) return new String[0];

		Client.GetClientsAction getPlugIns=new Client.GetClientsAction(
			com.rbnb.api.PlugIn.class);

		// Search to depth of 2: Unnamed top level, First level current
		//  server, second level children.
//		Client.forEachNodeToDepth(response,getServers,2);
		Client.forEachNode(response,getPlugIns);
		return getPlugIns.getNames();
	
		// Shouldn't happen:
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) 
		{ throw new RuntimeException(e.getMessage()); }
	}


	/**
	  * Convenience function to search this <code>ChannelMap</code>
	  * for the specified <code>keyword</code> and <code>mimeType</code>.
	  *  &nbsp;All channels of <b>fetched</b> data currently in the map are
	  *  iterrogated to see if their MIME type exactly matches the specified 
	  * <code>mimeType</code> parameter.  If they do, the data for that 
	  *  channel is searched to see if every key word in the input set exists.
	  *  If so, the channel is added to the array of channels returned.
	  * <p>Keywords should be separated by spaces.  If either parameter
	  *  is null, that constraint is ignored on the search.</p>
	  * <p>
     *
     * @author WHF
     *
     * @return An array of channel indices for channels which match the search
	 *	criteria.
     * @since V2.0
     * @version 09/28/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/28/2004  JPW	To make the J# compiler happy, initialize
     *			variable dataString to null
     * 05/20/2002  WHF	Created.
     *
     */	
	public int[] Search(String mimeType, String keywords)
	{
		if (mimeType!=null&&mimeType.length()==0) mimeType=null;
		if (keywords!=null&&keywords.length()==0) keywords=null;

		// Check to see if user wants everything (null search).
		//  If so, handle quickly:
		if (mimeType==null&&keywords==null)
		{
			int[] arr=new int[NumberOfChannels()];
			for (int ii=0; ii<arr.length; ++ii)
				arr[ii]=ii;
			return arr;
		}

		// Build array of search tokens:
		String [] searchWordArray;
		if (keywords!=null)
		{
			StringTokenizer st=new StringTokenizer(keywords);
			int tok=st.countTokens();
			
			searchWordArray=new String[tok];
			for (int ii=0; ii<tok; ++ii)
				searchWordArray[ii]=st.nextToken().toLowerCase();
		}
		else
			searchWordArray=new String[0];

//		final LinkedList hits=new LinkedList();
		final Vector hits=new Vector();
//		final HashMap map=new HashMap();
		final Hashtable map=new Hashtable();
		for (int ii=0; ii<NumberOfChannels(); ++ii) 
		{
			String mime=GetMime(ii);
			// JPW 09/28/2004: To make the J# compiler happy,
			//                 initialize dataString to null
			String dataString = null;
			int type=GetType(ii);
			boolean mimesEqual=mimeType==null
					||(mimeType!=null&&mimeType.equals(mime));

			// Check for just meta-search
			if (searchWordArray.length==0&&mimesEqual)
			{
				hits.addElement(new Integer(ii));
				continue;
			}
			if (mimesEqual||mimeType==null
					&&mime!=null&&mime.startsWith("text/"))
			{
				switch (type) {
				case ChannelMap.TYPE_STRING:
					dataString=GetDataAsString(ii)[0];
					break;
				case ChannelMap.TYPE_INT8:
					dataString=new String(GetDataAsInt8(ii));
					break;
				default:
					continue;
				}
			}// otherwise only read if string:
			else if (mimeType==null&&type==ChannelMap.TYPE_STRING)
				dataString=GetDataAsString(ii)[0];
			else continue;

			boolean isMatch=true;
			if (dataString!=null)
			{					
				// Build hashmap of tokens:
				StringTokenizer st=new StringTokenizer(
					dataString.toLowerCase(),
					" \t\n\r\f:;'\",.`()[]{}");
//				HashMap tokens=new HashMap();
				Hashtable tokens=new Hashtable();
				while (st.hasMoreTokens())
					tokens.put(st.nextToken(),null); // map to null
				for (int iii=0; iii<searchWordArray.length; ++iii)
				{
					if (!tokens.containsKey(searchWordArray[iii]))
					{
						isMatch=false;
						break;
					}
				}
			}
			else if (searchWordArray.length!=0) isMatch=false; 
			if (isMatch) 
			{
				hits.addElement(new Integer(ii));
			}
		}  // end for
		int [] matches=new int[hits.size()];
		{ /* sigh
		Iterator iter=hits.iterator();
		int ii=0;
		while (iter.hasNext())
			matches[ii++]=((Integer) iter.next()).intValue();
			*/
		for (int ii=0; ii<matches.length; ++ii)
			matches[ii]=((Integer) hits.elementAt(ii)).intValue();
		}
		return matches;
	} // end Search()

/////////////////// Package Utilities: ///////////////////////

	// For serialization:
	static final long serialVersionUID = 8274903653155563848L;

//  Sink:
	/**
	  * Allows Sinks to add data from Fetches.  Also used by PlugIn
	  *  to store requested channels, with null data arrays.
	  */

	// 05/22/2002  WHF  Also added to dataRequest.
	void addFetched(String name, DataArray dArray)
	{
/*		try {
			dataReq.addChannel(name);
		} catch (Exception e) { } // never happen. */
		Channel ch=new Channel(channelList.size(),name,null);
		ch.dArray=dArray;

		channelList.addElement(ch);
		channelMap.put(name,ch);
	}

	/**
	  * Stores this rmap in this channelmap.  If tokeep is true, 
	  *  also stores the rmap internally for forwarding.
	  */
	// 11/15/2002  WHF  Check for relative vs. absolute names, so that
	//   plugins get the correct output.
	// 11/19/2002  WHF  Add flag to remove the leading slash.  Removing seems
	//  to be necessary in cases of relative names in Plug-Ins, but shouldn't
	// be necessary for the default case.
	// 05/05/2003  WHF  Now returns a 'channel' for every endpoint, regardless
	//  of whether or not they contain data.  'Folders', or channels without
	//  data, have a trailing slash.
	//
	void processResult(
			Rmap result, 
			boolean tokeep, 
			boolean removeLeadingSlash) throws Exception
	{
		// For response forwarding:
//		cm.setResponse(result);
		if (tokeep) response=result;
if (debugFlag) System.err.println("Processing from fetch "+result);

		if (result!=null)
		{
			setIfFetchTimedOut(false);
			String [] names=result.extractNames();
//			String [] names=result.extractFoldersAndChannels();
/* caused problems with variations
			Client.GetNamedListAction gla=new Client.GetNamedListAction();
			Client.forEachEndpoint(result, gla);
			String [] names=gla.getNames();
*/
			for (int ii=0; ii<names.length; ++ii)
			{
				String name=names[ii];
				
				// 2007/03/23  WHF  Extraction fails for "." channels, because
				//   extractNames extracts them as nameless (length == 0).
				//   We briefly remap them back to "." to get any matching
				//   data.
				DataArray res;
				if (name.length() == 0)	res = result.extract(".");
				else res = result.extract(name);
	
				// Original code:
				// DataArray res=result.extract(name);

				// 2007/03/23  WHF  Added check for zero length name: 
				if (       name.length() > 0 
						&& removeLeadingSlash
						&& name.charAt(0) == Rmap.PATHDELIMITER)
					name=name.substring(1);

				addFetched(name, res);
			}
			
			folderArray=result.extractFolders();
		}
		else setIfFetchTimedOut(true);
	}


	// Also reset by Clear().
	private void setIfFetchTimedOut(boolean wasTimeout)
	{ this.wasTimeout=wasTimeout; }

	/**
	  * Creates a DataRequest object that contains any data added
	  *  so far to the Rmap.  Channels which have been added but do not
	  *  contain data will be merged in.
	  *
	  * @param bThrowOnMixChans  Throw exception if there is a mix of
	  *                          channels with and without data?
	  *
	  * 11/13/2002  WHF  Added this method.
	  * 2003/10/14  WHF  Throws meaningful exception when an empty request
	  *                  is made.
	  * 2007/02/28  JPW  Add new argument, bThrowOnMixChans
	  * 2007/03/23  WHF  Made both methods final.  Now that they are overloaded,
	  *    allowing them to be virtual risks confusion.
	  */
	
	// JPW 02/28/2007: Default form of the method
	final DataRequest produceRequest() throws SAPIException
	{
	    return produceRequest(true);
	}
	
	final DataRequest produceRequest(boolean bThrowOnMixChans) throws SAPIException
	{
// 2007/04/04  WHF  Experiment:
bThrowOnMixChans = false;
		try {
		DataRequest dataReq=new DataRequest();
		Rmap base=(Rmap) produceOutput();
/***  2007/04/04  WHF  Original ordering:
		boolean doThrow=false;
		while (base.getNchildren()>0)
		{
			Rmap tmp=base.getChildAt(0);
			base.removeChildAt(0);
			dataReq.addChild(tmp);
			doThrow=true;
		}
		// Add channels which do not contain data:
		for (int ii=0; ii<channelList.size(); ++ii)
		{
			Channel ch=(Channel) channelList.elementAt(ii);
			if (ch.rmap==null) {    // no data, add channel with data marker
			    
			    // mjm 2/8/05:  this won't let certain registrations to happen?
			    
			    // JPW 02/28/2007: Add bThrowOnMixChans; only throw exception if both
			    //                 doThrow and bThrowOnMixChans are true. For registration
			    //                 requests, bThrowOnMixChans will be false - it is OK
			    //                 if there is a mix of chans with and without User Info
			    //                 for registration.
			    if (doThrow && bThrowOnMixChans) {
				throw new IllegalArgumentException(
				    "Illegal mixed request of channels with and without data");
			    }
			    
				// 2007/04/04  WHF  Experiment:
			    //Client.addDataMarkerAction.doAction(dataReq.addChannel(ch.name));
				Rmap child = dataReq.addChannel(ch.name), grandChild = new Rmap();
				child.addChild(grandChild);
				Client.addDataMarkerAction.doAction(grandChild);
			}
		}
***/
// 2007/04/04  WHF  Experiment
		// Remove time values.  They will be replaced with the request 
		//  times.
		Client.forEachNode(base, Client.removeTimeAction);
		
		// Simplify the resulting Rmap:
		base.collapse();
		
		// Add the channels without data:
		for (int ii=0; ii<channelList.size(); ++ii)
		{
			Channel ch=(Channel) channelList.elementAt(ii);
			if (ch.rmap==null) {    // no data, add channel with data marker
			    Client.addDataMarkerAction.doAction(base.addChannel(ch.name));
			}
		}
		// Find the new root of the hierarchy:
		while (base.getParent() != null) base = base.getParent();
		
		// Add its children to the data request.
		while (base.getNchildren()>0)
		{
			Rmap tmp=base.getChildAt(0);
			base.removeChildAt(0);
			dataReq.addChild(tmp);
		} 

// 2007/04/04  WHF  Original code continues.
		clearData();
		
		if (dataReq.getNchildren() == 0) throw new IllegalArgumentException(
			"Cannot make empty request.");			
		
		for (	Rmap curr=dataReq.getChildAt(0); 
				curr.getName()==null; 
				 ) {
			if (curr.getTrange()!=null)
				curr.getChildAt(0).setTrange(curr.getTrange());
			Rmap parent=curr.getParent(), child=curr.getChildAt(0);
			parent.removeChildAt(0);
			curr.removeChildAt(0);			
			parent.addChild(child);
			curr=child;
		}
		
if (debugFlag) System.err.println("DataRequest: "+dataReq);
		return dataReq;
		} catch (RuntimeException re) { throw re; }
		catch (Exception e) { throw new SAPIException(e); }
	}

	// Stores Rmap response from Sink.Fetch for purposes of forwarding
	final void setResponse(Rmap response) { this.response = response; }

	/**
	  * Used by Source#Flush in the case of no channels out to pass along a
	  *  response.
	  */
	final Rmap getResponse() { return response; }

//  Source:
	// Returns the frame used as the base of the source hierarchy,
	//  used primarily by PlugInChannelMap.produceOutput():
	Rmap getBaseFrame() { return baseFrame; }

	/**
	  * Combines the data received from the last Fetch(), as well as 
	  *  data Put() to this map, into one Rmap.
	  * <p>As of 05/19/2003, always uses null parent.
	  */
	Rmap produceOutput() throws Exception
	{
/*		Rmap toReturn;
		if (response==null)
			toReturn=baseFrame;
		else {
			Rmap output=new Rmap(null);
			output.addChild(baseFrame);
			output.addChild(response);
			toReturn=output;
		}
*/
		if (outputRmap==null) outputRmap=new Rmap(null);
		else while (outputRmap.getNchildren()>0) 
			outputRmap.removeChildAt(outputRmap.getNchildren()-1);
		
		if (baseFrame.getName()!=null || baseFrame.getNchildren()>0)
			outputRmap.addChild(baseFrame);
		if (response!=null)
			outputRmap.addChild(response);		
		// 05/06/2003  Only add folders if there are any (otherwise null
		//  empty Rmap confuses things).
//		if (folderRmaps.getName()!=null || folderRmaps.getNchildren()>0)
//			outputRmap.addChild(folderRmaps);

if (debugFlag) System.err.println("produceOutput: "+outputRmap);
		return outputRmap;
	}

	/**
	  * Clears out source data, without deleting channels.
	  *  Called by Clear(), Sink, Source.
	  */
	void clearData() throws Exception
	{
		baseFrame=new Rmap();
		outputRmap=null;
		response=null;
		for (int ii=channelList.size()-1; ii>=0; ii--)
		{
			Channel ch=((Channel) channelList.elementAt(ii));
			ch.rmap=null;
		}
		channelsPut=0;
		timePerChannel=false;
	}
	void incrementNext() { if (timeMode==NEXT) ++start; }
	int getChannelsPut() { return channelsPut; }
	
	final boolean debugFlag;

/////////////////// Private Data: ///////////////////////

//  Sink:
//	private DataRequest dataReq=new DataRequest();
	private boolean wasTimeout=false;

	// Used when this ChannelMap is pushed into a Sink to store the
	//  response for forwarding.
	private Rmap response;

	// Instances of this class are stored in chanVect:
	private class Channel implements java.io.Serializable
	{
		String name;
		int index;

		// Sink:
		DataArray dArray;

		// Source:
		Rmap rmap;
		String mime;	// Sigh.  Needed in case PutMime is called 
						// before PutData.
		int	lastConsistentChild,
			lastSetTimeCount;

		Channel(int index, String name, Rmap rmap)
		{ 
			this.index=index;
			this.name=name;
			this.rmap=rmap;
			lastSetTimeCount=setTimeCounter;
		}

		// Will work with either a String or a Channel:
		public boolean equals(Object comp)
		{
			return name.equals(comp.toString());
		}

		public String toString()
		{
			return name;
		}
	}

//  Source:
	private int timeMode=SERVERTOD;
	private double start=0.0, duration=1.0;
	private double[] pointTimes;
	private DataArray timeReference;

	// Keeps track of the number of channels which have data:
	private int channelsPut=0;

	// If false, the time set applies to the entire frame.  Otherwise,
	//  the current time range applies to each new channel added.
	//  Reset with clearData().
	private boolean timePerChannel=false;

	// Counter for calls to SetTime.  If timePerChannel is
	//  true, each channel must check its local counter against
	//  this one for consistency.
	private int setTimeCounter=0;

	// Collection used to archive channels:
//	private ArrayList channelList=new ArrayList();
//	private HashMap channelMap=new HashMap();
	private Vector channelList;
	private Hashtable channelMap,
		folderMappings;

	// Rmap hierarchy to store output data
	private	Rmap 
		baseFrame,
//		folderRmaps=new Rmap(),
		outputRmap=null;

	// Stores result of Rmap.extractFolders():
	private String[] folderArray;
	
/////////////////// Private Utilities: ///////////////////////

	// EMF 9/1/05: serialization methods
	private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException {
		try {
			response.collapse();
		} catch (Exception e) {
			e.printStackTrace();
		}
//System.err.println("ChannelMap.writeObject called; writing "+response);
		out.writeObject(response);
	}
	
	// EMF 9/1/05: serialization methods
	 private void readObject(java.io.ObjectInputStream in)
	 throws java.io.IOException, ClassNotFoundException {
		 try {
			 initVariables();
Rmap rmin=(Rmap)in.readObject();
//System.err.println("ChannelMap.readObject called: read "+rmin);
			 processResult(rmin,true,false);
			 //processResult((Rmap)in.readObject(),true,false);
			 response.collapse();
		 } catch (Exception e) {
			 e.printStackTrace();
			 throw new java.io.IOException(e.getMessage());
		 }
	 }
	 
	 //EMF 9/14/05: initialize class variables
	 // collected here, since need for both new ChannelMap() and readObject
	 private final void initVariables() {
		 channelList=new Vector();
		 channelMap=new Hashtable();
		 folderMappings=new Hashtable();
		 baseFrame=new Rmap();
		 folderArray=new String[0];
	 }
		 
	 //  Sink:
	private Object getData(int index)
	{
		return ((Channel) channelList.elementAt(index)).dArray.getData();
	}

//  Source: 
	private void onTimeModeSet()
	{
		if (!timePerChannel&&channelsPut>0) // user has already set
			// the time:
			inheritTimes();

		++setTimeCounter;
	}

	private TimeRange getTR()
	{
		++channelsPut;
		TimeRange tr=null;
		switch (timeMode)
		{
			case MANUAL:
				tr=new TimeRange(start,duration);
			break;

			case ARRAY:
				tr=new TimeRange(pointTimes,0.0);
			break;

			case NEXT:
				tr=new TimeRange(start+1.0,0.0);
			break;

			case TIMEOFDAY:
				tr=new TimeRange(
			  (System.currentTimeMillis()/1000.0),0.0);
			break;

			case SERVERTOD:
				tr = TimeRange.SERVER_TOD;
			break;

			case REFERENCE:
				throw new IllegalStateException(
					"Cannot get Reference time range.");
		}
		if (!timePerChannel)
		{
			baseFrame.setTrange(tr);
			return null;  // set null for channel data time ranges
		}
		return tr;
	}

	// Moves time stamp from the base frame to all its root nodes.
	private void inheritTimes()
	{
		// Should only be set true here.  Set false in flush.
		timePerChannel=true; 

		try {  // doing basically nothing throws tons of exceptions...
		TimeRange tr=baseFrame.getTrange();
		if (tr==null)
		 // this happens when PutTimeRef has been called on a clean
			// channelmap.  In this case there is no global time range,
			//  since the reference is local to the next block added only.
			return;

		baseFrame.setTrange(null);

		findEndNodes(baseFrame,tr);

		} catch (Exception e) { e.printStackTrace(); } // should never throw
	}

	private void putData(	int channelIndex,
				Object data,
				int data_length,
				int data_pointsize,
				byte data_type,
				byte data_order)
		throws Exception
	{
		// INB 08/31/2001 - strings always need separate data blocks.
		Rmap descendant;
		Channel ch=(Channel) channelList.elementAt(channelIndex);
		if (ch.rmap==null) {
		    // INB 02/06/2002 - want to use addChannel rather than
		    // findDescendant to do the actual add work.  This isn't as
		    // efficient, but addChannel does the right thing and
		    // findDescendant doesn't.
		    /*
			ch.rmap=descendant=baseFrame
				.findDescendant(ch.name,true);
		    */
		    // INB 10/21/2002 - addChannel now does everything we want,
		    // including returning the needed pointer.
		    /*
		    if ((ch.rmap = baseFrame.findDescendant
			 (ch.name,false)) == null) {
			baseFrame.addChannel(ch.name);
			ch.rmap = baseFrame.findDescendant(ch.name,false);
		    }
		    */
//System.err.println("prev BF: "+baseFrame);
		    ch.rmap = baseFrame.addChannel(ch.name);
//System.err.println("putData(\""+ch.name+"\"): ch.rmap: "
//+ch.rmap+"\nbaseFrame: "+baseFrame);
		}
		descendant=ch.rmap;

		if (timeMode==REFERENCE)
		{
			++channelsPut; // incremented in getTR(), which is
				// not now called.
			ch.rmap.addDataWithTimeReference(data,
				data_length,data_pointsize,
				data_type,data_order,timeReference);
			return; // done
		}

 		TimeRange newTR=getTR();  // will be null if set on baseFrame
		if (descendant.getNchildren()==0) // no children
			if (descendant.getDblock()==null) // no data neither
			{
				descendant.setDblock(new DataBlock(
					data,data_length,
					data_pointsize,
					data_type,
					ch.mime,
					data_order,
					false,
					0,
					data_pointsize));
				descendant.setTrange(newTR);
//				ch.checkConsistency=false; // consistent now
				ch.lastSetTimeCount=setTimeCounter;
			}
			else if (data_type != TYPE_STRING && data_type != TYPE_USER && 
			     /* INB 10/23/2002 check byte array point size. */
			     ((data_type != TYPE_BYTEARRAY) ||
			      (data_pointsize ==
			       descendant.getDblock().getPtsize())) &&
				   (ch.lastSetTimeCount==setTimeCounter)) { 
			// there has been no increment
				descendant.getDblock().addData(data,
					data_length);
			}
			else {
				ch.lastSetTimeCount=setTimeCounter;
				if (data_type == TYPE_STRING || data_type == TYPE_USER ||
			     /* INB 10/23/2002 check byte array point size. */
				    ((data_type == TYPE_BYTEARRAY) &&
				     (data_pointsize !=
				      descendant.getDblock().getPtsize())) ||
				    !checkConsistency(descendant,true,
					newTR,data,data_length))
				{ // data inconsistent, move DB&TR down a level
					// and add new DB&TR:
					Rmap child=new Rmap(
						null,
						descendant.getDblock(),
						descendant.getTrange());
					descendant.setDblock(null); 
					descendant.setTrange(null);
					descendant.addChild(child);
					descendant.addChild(new Rmap(
						null,
						new DataBlock(
							data,
							data_length,
							data_pointsize,
							data_type,
							ch.mime,
							data_order,
							false,
							0,
							data_pointsize),
						newTR));
					ch.lastConsistentChild=1;
				} // else checkConsistency added data, done
				else {
				    return;
				}
			}
		else if (data_type == TYPE_STRING || data_type == TYPE_USER ||
			 /* INB 10/23/2002 - check byte array. */
			 (data_type == TYPE_BYTEARRAY) ||
			 (ch.lastSetTimeCount!=setTimeCounter)) // was inc
		{ // check children
//			ch.checkConsistency=false;
			ch.lastSetTimeCount=setTimeCounter;
			if (data_type != TYPE_STRING && data_type != TYPE_USER) {
				for (int ii=descendant.getNchildren()-1; ii>=0;
				     ii--)
				    /* INB 10/23/2002 check byte array size. */
					if (((data_type != TYPE_BYTEARRAY) ||
					     (data_pointsize ==
					      descendant.getChildAt
					      (ii).getDblock().getPtsize())) &&
					    checkConsistency(descendant.getChildAt(ii),false,
							     newTR,data,data_length))
                              					{
						ch.lastConsistentChild=ii;
						return;
					}
			}
			// If here need to add new child:
			descendant.addChild(new Rmap(null,new DataBlock(
				data,data_length,data_pointsize,
				data_type,ch.mime,data_order,
				false,0,data_pointsize),
				newTR));
			for (ch.lastConsistentChild=descendant.getNchildren()-1;
			     (ch.lastConsistentChild >= 0) &&
			       (descendant.getChildAt(ch.lastConsistentChild).getName() != null);
			     --ch.lastConsistentChild) {}
			if (ch.lastConsistentChild == -1) {
			}
		}
		else if ((ch.lastConsistentChild == -1) ||
			 descendant.getChildAt(ch.lastConsistentChild).getName() != null)
		{ // need an unnamed entry.
			descendant.addChild(new Rmap(null,new DataBlock(
				data,data_length,data_pointsize,
				data_type,ch.mime,data_order,
				false,0,data_pointsize),
				newTR));
			for (ch.lastConsistentChild=descendant.getNchildren()-1;
			     (ch.lastConsistentChild >= 0) &&
			       (descendant.getChildAt(ch.lastConsistentChild).getName() != null);
			     --ch.lastConsistentChild) {}
			if (ch.lastConsistentChild == -1) {
			}
		}
		else // consistent child
		{
			descendant.getChildAt(ch.lastConsistentChild)
				.getDblock().addData(data,data_length);
		}
	}

	private boolean checkConsistency(Rmap res, boolean expectName, TimeRange newTR, Object data, int data_length) throws Exception
	{
		TimeRange oldTR=res.getTrange();
		DataBlock oldDB=res.getDblock();
		if (expectName == (res.getName() != null))
		{
			if (oldTR!=null)
			{
				if (oldTR.extend(oldDB.getNpts(),newTR,data_length))
				{
					oldDB.addData(data,data_length);
					return true;
				}
			}
			else if (newTR==null) // both TRs are null, must be frame set TR
			{ // just add data:
				oldDB.addData(data,data_length);
				return true;
			}
		}

		return false; // data inconsistent
	}

// Static internal constants:
	private final static String	
		timeStampErrStr=
		"TimeRef must be one of \"Next\" or \"TimeOfDay\" "
			+"\nfor AutoTimeStamp()";

	private static final String[] types= {
		"Unknown",
		"",
		"",
		"Int8",
		"Int16",
		"Int32",
		"Int64",
		"Float32",
		"Float64",
		"String",
		"ByteArray" };
	
	private final static int MANUAL=0,
				 NEXT=1,
				 TIMEOFDAY=2,
				 ARRAY=3,
				 REFERENCE=4,
				 SERVERTOD = 5;

// Static inner classes:
	/**
	 * Byte order enumerated type.  One of LOCAL, LSB, or MSB.
	 */
	public static class ByteOrderEnum
	{
		private byte b;
		byte getByte() { return b; }
		ByteOrderEnum(byte _b) { b=_b; }
		
		/**
		  * Used to see if LOCAL equals LSB or MSB.  Since the objects are
		  *  singletons, use the == operator to differentiate between all three.
		  */
		public boolean equals(Object o)
		{
			try {
			return ((ByteOrderEnum) o).getByte()==b;
			} catch (ClassCastException cce) { return false; }
		}
	}

// Static utility functions:
	private static byte determineLocalByteOrder()
	{
		// Try to use facilities in Java 1.4:
		try {
		Class byteOrderClass=Class.forName("java.nio.ByteOrder");
		if (byteOrderClass.getField("BIG_ENDIAN").equals(
				byteOrderClass.getDeclaredMethod("nativeOrder", null).invoke(
					null,null)))
			return DataBlock.ORDER_MSB;
		return DataBlock.ORDER_LSB;
		} catch (Throwable t) { // if anything wrong, use below instead:
		
		try {  // mjm, catch getProperties exception and default for applets
		java.util.Enumeration en = System.getProperties()
			.propertyNames();
		while (en.hasMoreElements())
		{
			String prop=(String) en.nextElement(),
				p2=prop.toLowerCase();
			if (p2.endsWith("endian"))
				if (System.getProperty(prop)
					.equalsIgnoreCase("little"))
					return DataBlock.ORDER_LSB;
				else return DataBlock.ORDER_MSB;
			if (p2.endsWith("unicode.encoding"))
				if (System.getProperty(prop).equalsIgnoreCase(
					"unicodelittle"))
					return DataBlock.ORDER_LSB;
				else return DataBlock.ORDER_MSB;
		}
		} catch (Throwable t2) {};  // mjm fall thru

		// If we get here we must assume that we're MSB:
		return DataBlock.ORDER_MSB;
		}
	}

	// Recursive utility to set the time range of all end nodes:
	private static void findEndNodes(Rmap target, TimeRange tr) 
		throws Exception
	{
		int N=target.getNchildren();
		if (N==0) target.setTrange((TimeRange) tr.clone());
		for (int ii=N-1; ii>=0; ii--)
			findEndNodes(target.getChildAt(ii),tr);		
	}
	private static int getSize(int typecode)
	{
		int size=0;
		switch (typecode)
		{
			case DataBlock.TYPE_FLOAT64:
				size=8;
				break;

			case DataBlock.TYPE_FLOAT32:
				size=4;
				break;

			case DataBlock.TYPE_INT64:
				size=8;
				break;

			case DataBlock.TYPE_INT32:
				size=4;
				break;

			case DataBlock.TYPE_INT16:
				size=2;
				break;

			case DataBlock.TYPE_INT8:
			case DataBlock.TYPE_STRING:
			case DataBlock.TYPE_USER:
				size=1;
				break;

			default:
				throw new IllegalArgumentException(
					"Unsupported type in PutData().");
		}
		return size;
	}

// double2Byte method - writes doubles to byte array
private static final byte[] double2Byte(double[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*8];
  for (int i=0;i<length;i++) {
    long data=Double.doubleToLongBits(inData[i]);
    outData[j++]=(byte)(data>>>0);
    outData[j++]=(byte)(data>>>8);
    outData[j++]=(byte)(data>>>16);
    outData[j++]=(byte)(data>>>24);
    outData[j++]=(byte)(data>>>32);
    outData[j++]=(byte)(data>>>40);
    outData[j++]=(byte)(data>>>48);
    outData[j++]=(byte)(data>>>56);
  }
  return outData;
}

//float2Byte method - writes floats to byte array
private static final byte[] float2Byte(float[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*4];
  for (int i=0;i<length;i++) {
    int data=Float.floatToIntBits(inData[i]);
    outData[j++]=(byte)(data>>>0);
    outData[j++]=(byte)(data>>>8);
    outData[j++]=(byte)(data>>>16);
    outData[j++]=(byte)(data>>>24);
 }
  return outData;
}


// double2Byte method - writes doubles to byte array
private static final byte[] long2Byte(long[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*8];
  for (int i=0;i<length;i++) {
    long data=inData[i];
    outData[j++]=(byte)(data>>>0);
    outData[j++]=(byte)(data>>>8);
    outData[j++]=(byte)(data>>>16);
    outData[j++]=(byte)(data>>>24);
    outData[j++]=(byte)(data>>>32);
    outData[j++]=(byte)(data>>>40);
    outData[j++]=(byte)(data>>>48);
    outData[j++]=(byte)(data>>>56);
  }
  return outData;
}


//EMF 9/13/00: added support for integers
//int2Byte method - writes ints to byte array
private static final byte[] int2Byte(int[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*4];
  for (int i=0;i<length;i++) {
    outData[j++]=(byte)(inData[i]>>>0);
    outData[j++]=(byte)(inData[i]>>>8);
    outData[j++]=(byte)(inData[i]>>>16);
    outData[j++]=(byte)(inData[i]>>>24);
  }
  return outData;
}

// short2Byte method - writes short ints to byte array
private static final byte[] short2Byte(short[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*2];
  for (int i=0;i<length;i++) {
    outData[j++]=(byte)(inData[i]>>>0);
    outData[j++]=(byte)(inData[i]>>>8);
  }
  return outData;
}


} // end class ChannelMap


