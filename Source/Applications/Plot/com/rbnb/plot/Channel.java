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

// Channel - replacement for COM.Creare.RBNB.API.Channel

package com.rbnb.plot;


// 04/19/2002  WHF  Added INT8 support.
// 05/08/2002  WHF  Kluged INT64 support.
// 10/18/2004  EMF  Added ByteArray support.
// 2004/12/23  WHF  Added mime field, for identification of byte arrays.
//
public class Channel {

public static final byte MSB=com.rbnb.api.DataBlock.ORDER_MSB;
public static final byte LSB=com.rbnb.api.DataBlock.ORDER_LSB;

public String channelName=null;
public short channelUserDataType=0;
public byte[] channelUserData=null;
public byte[] data=null;
private byte[][] dataByteArray=null;
private String[] dataString=null;
private String mimeType;
private byte[] dataInt8=null;
private short[] dataInt16=null;
private int[] dataInt32=null;
private long[] dataInt64=null;
private float[] dataFloat32=null;
private double[] dataFloat64=null;
public DataTimeStamps timeStamp=null;
public int numberOfPoints=0;
public short pointSize=0;
public DataTimeStamps frames=null;
public boolean isByteArray=false;
public boolean isString=false;
public boolean isInt8=false;
public boolean isInt16=false;
public boolean isInt32=false;
public boolean isInt64=false;
public boolean isFloat32=false;
public boolean isFloat64=false;
public short byteOrder=MSB;

public Channel() {
}

//EMF 8/8/05: add mime support
public Channel(String name, String userinfo, String mime) {
	this(name,userinfo);
	mimeType=mime;
}

//EMF 3/25/05: add userdata support back in
//  always assume type 1, since types are no longer supported
public Channel(String name, String userinfo) {
	this(name);
	if (userinfo!=null) {
		channelUserDataType=1;
		channelUserData=userinfo.getBytes();
	}
}

public Channel(String name) {
  // INB 11/05/2001 - eliminate leading slash.
  if ((name != null) &&
      (name.length() >= 1) &&
      (name.charAt(0) == '/')) {
    name = name.substring(1);
  }
  channelName=name;
}

public String toString() {
  return channelName;
}

public void clear() {
  numberOfPoints=0;
  pointSize=0;
  data=null;
  dataInt16=null;
  dataInt32=null;
  dataInt64=null;
  dataFloat32=null;
  dataFloat64=null;
  dataByteArray=null;
  dataString=null;
  isInt8=false;
  isInt16=false;
  isInt32=false;
  isInt64=false;
  isFloat32=false;
  isFloat64=false;
  isByteArray=false;
  isString=false;
}

public void setName(String name) {
  channelName=name;
}

public void setTimeStamp(DataTimeStamps ts) {
  timeStamp=ts;
}

public DataTimeStamps getTimeStamp() {
  return timeStamp;
}

public int getNumberOfPoints() {
  return numberOfPoints;
}

public byte getByteOrder() {
  return MSB;
}

public short getPointSize() {
  return pointSize;
}

public String getChannelName() {
  return channelName;
}

public boolean equals(Object o) {
  if (o instanceof Channel) {
    Channel c=(Channel)o;
    if (c.channelName.equals(channelName)) return true;
  }
  return false;
}

// 04/19/2002  WHF  Added.
public void setDataInt8(byte[] inData) {
  dataInt8=inData;
  numberOfPoints=inData.length;
  pointSize=1;
  isInt8=true;
  data=new byte[0];
}

// 04/19/2002  WHF  Added.
public byte[] getDataInt8() { return dataInt8; }

// 10/18/2004 EMF Added.
// 2004/12/23  WHF  Added mime parameter.
public void setDataByteArray(byte[][] inData, String mime) {
  dataByteArray=inData;
  numberOfPoints=inData.length;
  pointSize=-1;
  isByteArray=true;
  data=new byte[0];
  mimeType = mime;
}

// 10/18/2004 EMF Added.
public byte[][] getDataByteArray() {
  return dataByteArray;
}

// EMF 6/30/05: added support for string data
public void setDataString(String[] inData, String mime) {
	dataString=inData;
	numberOfPoints=inData.length;
	pointSize=1;
	isString=true;
	mimeType=mime;
	data=new byte[0];
}

public String[] getDataString() {
  return dataString;
}

// 2004/12/23  WHF  Added.
public String getMimeType() {
	return mimeType;
}

public void setDataInt16(short[] inData) {
  dataInt16=inData;
  numberOfPoints=inData.length;
  pointSize=2;
  isInt16=true;
  data=new byte[0];
}

public short[] getDataInt16() {
  return dataInt16;
}

public void setDataInt32(int[] inData) {
  dataInt32=inData;
  numberOfPoints=inData.length;
  pointSize=4;
  isInt32=true;
  data=new byte[0];
}

public int[] getDataInt32() {
  return dataInt32;
}

// 05/08/2002  WHF  Although this method exists, it is not supported
//	underneath.  As a quick-fix, we convert the data to doubles.
public void setDataInt64(long[] inData) {
/*	
  dataInt64=inData;
  numberOfPoints=inData.length;
  pointSize=8;
  isInt64=true;
  data=new byte[0];
*/
	double [] d=new double[inData.length];
	for (int ii=0; ii<inData.length; ++ii)
		d[ii]=(double) inData[ii];
	setDataFloat64(d);
}

public long[] getDataInt64() {
  return dataInt64;
}

public void setDataFloat32(float[] inData) {
//System.err.println("Channel.setDataFloat32: "+inData[0]+" "+inData[inData.length-1]);
  dataFloat32=inData;
  numberOfPoints=inData.length;
  pointSize=4;
  isFloat32=true;
  data=new byte[0];
}

public float[] getDataFloat32() {
  return dataFloat32;
}

public void setDataFloat64(double[] inData) {
  dataFloat64=inData;
  numberOfPoints=inData.length;
  pointSize=8;
  isFloat64=true;
  data=new byte[0];
}

public double[] getDataFloat64() {
  return dataFloat64;
}

}

