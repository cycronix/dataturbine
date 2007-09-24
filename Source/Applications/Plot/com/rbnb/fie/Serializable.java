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
	Serializable.java
	
	An alternate Serializability interface for writing data to a buffer.
	
	2007/08/22  WHF  Created.
*/

package com.rbnb.fie;

/**
  * An alternate Serializable interface that writes data to a buffer.
  * <p>A simple example for doubles:
  * <p><code><pre>
  *    public class DoubleSerial implements Serializable
  *    {
  *        public ElementDescriptor getElementDescriptor()
  *        {
  *            return ElementDescriptor.DOUBLE_DESCRIPTOR;
  *        }
  *
  *        public void writeBinary(java.nio.ByteBuffer bb)
  *        {
  *            bb.asDoubleBuffer().put(array);
  *        }
  *        private double[] array;
  *    }
  *  </pre></code>
  */
public interface Serializable
{
	/**
	  * Returns a descriptor which specifies the sort of data this instance
	  *  writes out.
	  */
	public ElementDescriptor getElementDescriptor();
	
	/**
	  * Writes the binary data to disk.  Note that the type of data written 
	  *  out should correspond in final form to that in the element descriptor,
	  *  or else it will be interpretted incorrectly by MATLAB when loaded.
	  */
	public void writeBinary(java.nio.ByteBuffer bb);
}

class DoubleSerial implements Serializable
{
	public DoubleSerial(double[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.DOUBLE_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
		// Note: Does not update bb's position, but is fast so we use it.
        bb.asDoubleBuffer().put(array);
		
		bb.position(
				bb.position() + array.length * getElementDescriptor().getSize()
		);		
    }
    private double[] array;
}

class FloatSerial implements Serializable
{
	public FloatSerial(float[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.FLOAT_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
		// Note: Does not update bb's position, but is fast so we use it.
        bb.asFloatBuffer().put(array);
		
		bb.position(
				bb.position() + array.length * getElementDescriptor().getSize()
		);		
    }
    private float[] array;
}

class LongSerial implements Serializable
{
	public LongSerial(long[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.INT64_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
		// Note: Does not update bb's position, but is fast so we use it.
        bb.asLongBuffer().put(array);
		
		bb.position(
				bb.position() + array.length * getElementDescriptor().getSize()
		);		
    }
    private long[] array;
}

class IntSerial implements Serializable
{
	public IntSerial(int[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.INT32_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
		// Note: Does not update bb's position, but is fast so we use it.
        bb.asIntBuffer().put(array);
		
		bb.position(
				bb.position() + array.length * getElementDescriptor().getSize()
		);		
    }
    private int[] array;
}

class ShortSerial implements Serializable
{
	public ShortSerial(short[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.INT16_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
		// Note: Does not update bb's position, but is fast so we use it.
        bb.asShortBuffer().put(array);
		
		bb.position(
				bb.position() + array.length * getElementDescriptor().getSize()
		);		
    }
    private short[] array;
}

class CharSerial implements Serializable
{
	public CharSerial(char[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.CHAR_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
		// Note: Does not update bb's position, but is fast so we use it.
        bb.asCharBuffer().put(array);
		
		bb.position(
				bb.position() + array.length * getElementDescriptor().getSize()
		);		
    }
    private char[] array;
}

class ByteSerial implements Serializable
{
	public ByteSerial(byte[] array) { this.array = array; }
	
    public ElementDescriptor getElementDescriptor()
    {
        return ElementDescriptor.INT8_DESCRIPTOR;
    }

    public void writeBinary(java.nio.ByteBuffer bb)
    {
        bb.put(array);
    }
    private byte[] array;
}
