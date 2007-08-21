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
  *****************************************************************
  ***                                                           ***
  ***   Name : ByteConvert      ()                              ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan,DoeScan                                  ***
  ***   Date : December 1997,July 2000                          ***
  ***                                                           ***
  ***   Copyright 1997-2000 Creare Inc.                         ***
  ***                                                           ***
  ***   Description : extracts doubles, floats, short ints from ***
  ***                 byte arrays without using input streams,  ***
  ***                 and vice-versa                            ***
  ***                                                           ***
  ***   Input : primitive array                                 ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns : primitive array                               ***
  ***                                                           ***
  *****************************************************************
*/

// all methods are static

// using ByteArrayInputStream, DataInputStream, ByteArrayOutputStream,
// and DataOutputStream are too slow; code for direct reads/writes
// based on routines in the JVM ObjectInputStream and ObjectOutputStream
// source files

package com.rbnb.utility;

/**
  * Utility class to convert low level data types to and from byte arrays.
  */
  
// 
// 02/17/2003  WHF  Added optional byte swapping on x2Byte methods.
//
public class ByteConvert {

// byte2Double method - extracts doubles from byte array
public static final double[] byte2Double(byte[] inData,boolean byteSwap) {
   int j=0,upper,lower;
	int length=inData.length/8;
   double[] outData=new double[length];
	if (!byteSwap) for (int i=0;i<length;i++) {
		j=i*8;
		upper=( ((inData[j]   & 0xff) << 24) +
				  ((inData[j+1] & 0xff) << 16) +
				  ((inData[j+2] & 0xff) << 8) +
				  ((inData[j+3] & 0xff) << 0) );
		lower=( ((inData[j+4] & 0xff) << 24) +
				  ((inData[j+5] & 0xff) << 16) +
				  ((inData[j+6] & 0xff) << 8) +
				  ((inData[j+7] & 0xff) << 0) );
		outData[i]=Double.longBitsToDouble( (((long)upper) << 32) +
						    (lower & 0xffffffffl) );
		}
	else for (int i=0;i<length;i++) {
		j=i*8;
		upper=( ((inData[j+7] & 0xff) << 24) +
				  ((inData[j+6] & 0xff) << 16) +
				  ((inData[j+5] & 0xff) << 8) +
				  ((inData[j+4] & 0xff) << 0) );
		lower=( ((inData[j+3] & 0xff) << 24) +
				  ((inData[j+2] & 0xff) << 16) +
				  ((inData[j+1] & 0xff) << 8) +
				  ((inData[j]   & 0xff) << 0) );
		outData[i]=Double.longBitsToDouble( (((long)upper) << 32) +
						    (lower & 0xffffffffl) );
		}
		
	return outData;
   }

// byte2Float method - extracts floats from byte array
public static final float[] byte2Float(byte[] inData,boolean byteSwap) {
	int j=0,value;
	int length=inData.length/4;
   float[] outData=new float[length];
	if (!byteSwap) for (int i=0;i<length;i++) {
		j=i*4;
		value=( ((inData[j]   & 0xff) << 24) +
				  ((inData[j+1] & 0xff) << 16) +
				  ((inData[j+2] & 0xff) << 8) +
				  ((inData[j+3] & 0xff) << 0) );
		outData[i]=Float.intBitsToFloat(value);
		}
	else for (int i=0;i<length;i++) {
		j=i*4;
		value=( ((inData[j+3] & 0xff) << 24) +
				  ((inData[j+2] & 0xff) << 16) +
				  ((inData[j+1] & 0xff) << 8) +
				  ((inData[j]   & 0xff) << 0) );
		outData[i]=Float.intBitsToFloat(value);
		}

   return outData;
    }

// EMF 9/12/00
// byte2Int method - extracts ints from byte array
public static final int[] byte2Int(byte[] inData,boolean byteSwap) {
	int j=0,value;
	int length=inData.length/4;
   int[] outData=new int[length];
	if (!byteSwap) for (int i=0;i<length;i++) {
		j=i*4;
		outData[i]=( ((inData[j]   & 0xff) << 24) +
				  ((inData[j+1] & 0xff) << 16) +
				  ((inData[j+2] & 0xff) << 8) +
				  ((inData[j+3] & 0xff) << 0) );
		}
	else for (int i=0;i<length;i++) {
		j=i*4;
		outData[i]=( ((inData[j+3] & 0xff) << 24) +
				  ((inData[j+2] & 0xff) << 16) +
				  ((inData[j+1] & 0xff) << 8) +
				  ((inData[j]   & 0xff) << 0) );
		}

   return outData;
    }

// INB 11/7/00
// byte2Long method - extracts longs from byte array
public static final long[] byte2Long(byte[] inData,boolean byteSwap) {
	int j=0;
	long value;
	int length=inData.length/8;
	long ff = 0xff;
   long[] outData=new long[length];
	if (!byteSwap) for (int i=0;i<length;i++) {
		j=i*8;
		outData[i]=( ((inData[j]   & ff) << 56) +
				  ((inData[j+1] & ff) << 48) +
				  ((inData[j+2] & ff) << 40) +
				  ((inData[j+3] & ff) << 32) +
				  ((inData[j+4] & ff) << 24) +
				  ((inData[j+5] & ff) << 16) +
				  ((inData[j+6] & ff) << 8) +
				  ((inData[j+7] & ff) << 0) );

		// UCB 06/21/01-- finding a bug!
//  		System.err.println("   Converting: ");
//  		long result = 0;
//  		long accume = 0;
//  		for (int k = j+7, m = 0; k > -1; k--, m++) {
//  		    System.err.println("   inData[" + k + "]: " + inData[k]);
//  		    System.err.println("   inData & (long) ff: " + (inData[k] & ff));
//  		    result = (inData[k] & ff) << (m*8);
//  		    System.err.println("   (inData & (long) ff) << " + (m*8) + ": " +
//  				       result);
//  		    accume += result;
//  		    System.err.println("   So far: " + accume);
//  		}

		}
	else for (int i=0;i<length;i++) {
		j=i*8;
		outData[i]=( ((inData[j+7]   & 0xff) << 56) +
				  ((inData[j+6] & 0xff) << 48) +
				  ((inData[j+5] & 0xff) << 40) +
				  ((inData[j+4] & 0xff) << 32) +
				  ((inData[j+3] & 0xff) << 24) +
				  ((inData[j+2] & 0xff) << 16) +
				  ((inData[j+1] & 0xff) << 8) +
				  ((inData[j]   & 0xff) << 0) );
		}

   return outData;
    }

// byte2Short method - extracts short ints from byte array    
public static final short[] byte2Short(byte[] inData,boolean byteSwap) {
	//int j=0;
	int length=inData.length/2;
   short[] outData=new short[length];
	if (!byteSwap) for (int i=0,j=0;i<length;i++,j+=2) {
		//j=i*2;
		//outData[i]=(short)( ((inData[j] & 0xff) << 8) + ((inData[j+1] & 0xff) << 0 ) );
		outData[i]=(short)( (inData[j] << 8) + (inData[j+1] & 0xff) );
		}
	else for (int i=0;i<length;i++) {
		int j=i*2;
		outData[i]=(short)( ((inData[j+1] & 0xff) << 8) + ((inData[j] & 0xff) << 0) );
		}
	
   return outData;
   }

// double2Byte method - writes doubles to byte array
public static final byte[] double2Byte(double[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*8];
  for (int i=0;i<length;i++) {
    long data=Double.doubleToLongBits(inData[i]);
    outData[j++]=(byte)(data>>>56);
    outData[j++]=(byte)(data>>>48);
    outData[j++]=(byte)(data>>>40);
    outData[j++]=(byte)(data>>>32);
    outData[j++]=(byte)(data>>>24);
    outData[j++]=(byte)(data>>>16);
    outData[j++]=(byte)(data>>>8);
    outData[j++]=(byte)(data>>>0);
  }
  return outData;
}

//float2Byte method - writes floats to byte array
public static final byte[] float2Byte(float[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*4];
  for (int i=0;i<length;i++) {
    int data=Float.floatToIntBits(inData[i]);
    outData[j++]=(byte)(data>>>24);
    outData[j++]=(byte)(data>>>16);
    outData[j++]=(byte)(data>>>8);
    outData[j++]=(byte)(data>>>0);
  }
  return outData;
}

//EMF 9/13/00: added support for integers
//int2Byte method - writes ints to byte array
public static final byte[] int2Byte(int[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*4];
  for (int i=0;i<length;i++) {
    outData[j++]=(byte)(inData[i]>>>24);
    outData[j++]=(byte)(inData[i]>>>16);
    outData[j++]=(byte)(inData[i]>>>8);
    outData[j++]=(byte)(inData[i]>>>0);
  }
  return outData;
}

//UCB 7/19/01: added support for longs
//long2Byte method - writes longs to byte array
public static final byte[] long2Byte(long[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*8];
  for (int i=0;i<length;i++) {
    outData[j++]=(byte)(inData[i]>>>56);
    outData[j++]=(byte)(inData[i]>>>48);
    outData[j++]=(byte)(inData[i]>>>40);
    outData[j++]=(byte)(inData[i]>>>32);
    outData[j++]=(byte)(inData[i]>>>24);
    outData[j++]=(byte)(inData[i]>>>16);
    outData[j++]=(byte)(inData[i]>>>8);
    outData[j++]=(byte)(inData[i]>>>0);
  }
  return outData;
}

// short2Byte method - writes short ints to byte array
public static final byte[] short2Byte(short[] inData) {
  int j=0;
  int length=inData.length;
  byte[] outData=new byte[length*2];
  for (int i=0;i<length;i++) {
    outData[j++]=(byte)(inData[i]>>>8);
    outData[j++]=(byte)(inData[i]>>>0);
  }
  return outData;
}


/**
  * Writes doubles to byte array, with optional byte swapping.
  *
  * <p>
  * @author WHF
  * @since V2.0B10
  */
public static final byte[] double2Byte(double[] inData, boolean makeLSB) 
{
	if (!makeLSB) return double2Byte(inData);
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

/**
  * Writes floats to byte array, with optional byte swapping.
  *
  * <p>
  * @author WHF
  * @since V2.0B10
  */
public static final byte[] float2Byte(float[] inData, boolean makeLSB) {
	if (!makeLSB) return float2Byte(inData);
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

/**
  * Writes ints to byte array, with optional byte swapping.
  *
  * <p>
  * @author WHF
  * @since V2.0B10
  */
public static final byte[] int2Byte(int[] inData, boolean makeLSB) {
	if (!makeLSB) return int2Byte(inData);
	int j=0;
	int length=inData.length;
	byte[] outData=new byte[length*4];
	for (int i=0;i<length;i++) {
		int data=inData[i];
		outData[j++]=(byte)(data>>>0);
		outData[j++]=(byte)(data>>>8);
		outData[j++]=(byte)(data>>>16);
		outData[j++]=(byte)(data>>>24);
	}
	return outData;
}

/**
  * Writes longs to byte array, with optional byte swapping.
  *
  * <p>
  * @author WHF
  * @since V2.0B10
  */
public static final byte[] long2Byte(long[] inData, boolean makeLSB) {
	if (!makeLSB) return long2Byte(inData);
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

/**
  * Writes shorts to byte array, with optional byte swapping.
  *
  * <p>
  * @author WHF
  * @since V2.0B10
  */
public static final byte[] short2Byte(short[] inData, boolean makeLSB) {
	if (!makeLSB) return short2Byte(inData);
	int j=0;
	int length=inData.length;
	byte[] outData=new byte[length*2];
	for (int i=0;i<length;i++) {
		short data=inData[i];
		outData[j++]=(byte)(data>>>0);
		outData[j++]=(byte)(data>>>8);
	}
	return outData;
}
}
