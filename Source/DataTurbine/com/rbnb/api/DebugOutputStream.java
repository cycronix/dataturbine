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

package com.rbnb.api;

/**
 * RBNB serialization output stream.
 * <p>
 * This class provides serialization of RBNB objects using either binary or
 * text output modes.
 * <p>
 * This version provides debugging output.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.InputStream
 * @since V2.0
 * @version 03/28/2003
 */

/*
 * Copyright 1997, 1998, 1999, 2000, 2001, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/28/2003  INB	Eliminated unnecessary synchronization.
 * 03/01/1997  INB	Created.
 * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
 *
 */
final class DebugOutputStream
    extends com.rbnb.api.OutputStream
{
    /**
     * the debug output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 08/20/2001
     */
    private OutputStream debug = null;

    /**
     * Class constructor to build an <code>DebugOutputStream</code> from a
     * <code>java.io.OutputStream</code>, a binary mode flag, and a buffer
     * size.
     * <p>
     * If <code>sizeI</code> is equal to zero, the putput stream is used
     * directly. Otherwise, a <code>java.io.BufferedOutputStream</code> is
     * put in place.
     *
     * @author Ian Brown
     *
     * @param oStreamI  the <code>java.io.OutputStream</code> to write to.
     * @param binaryI   binary mode?
     * @param sizeI	the size of the buffer.
     * @exception java.io.IOException
     *		  thrown if there is a problem with the output stream.
     * @since V1.0
     * @version 03/07/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    DebugOutputStream(java.io.OutputStream oStreamI,boolean binaryI,int sizeI)
	throws java.io.IOException
    {
	super(oStreamI,binaryI,sizeI);
	debug = new OutputStream(System.err,false,0);
    }

    /**
     * Closes this stream.
     * <p>
     * This method leaves the underlying stream open.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/12/2001  INB	Created.
     *
     */
    public final void close() {
	super.close();
	debug.close();
	debug = null;
    }

    /**
     * Flushes the <code>OutputStream</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception java.io.Exception
     *		  thrown if there is a problem flushing the stream.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void flush()
	throws java.io.IOException
    {
	super.flush();
	debug.flush();
    }

    /**
     * Gets the binary mode flag.
     * <p>
     *

    /**
     * Writes a boolean value to the <code>OutputStream</code>.
     * <p>
     * This method uses the <code>writeElement()</code> method and the constant
     * array <code>BOOLEANS</code> to write out the boolean value.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the boolean value.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing out the boolean value.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void writeBoolean(boolean vI)
	throws java.io.IOException
    {
	super.writeBoolean(vI);
	if (!getStage()) {
	    debug.writeBoolean(vI);
	}
    }

    /**
     * Writes a single byte value.
     * <p>
     * In binary mode, the method uses <code>write()</code> to write the byte.
     * <p>
     * In text mode, the method converts the byte to a string and writes that
     * out.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the byte value.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the byte value.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void writeByte(int vI)
	throws java.io.IOException
    {
	super.writeByte(vI);
	if (!getStage()) {
	    debug.writeByte(vI);
	}
    }

    /**
     * Writes a string as a series of bytes, discarding the high eight bits of
     * the characters of the string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the string.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void writeBytes(String vI)
	throws java.io.IOException
    {
	super.writeBytes(vI);
	if (!getStage()) {
	    debug.writeBytes(vI);
	}
    }

    /**
     * Writes out a character to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the character as a pair of bytes.
     * <p>
     * In text mode, the method writes the character as a single byte.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the character.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the character.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void writeChar(int vI)
	throws java.io.IOException
    {
	super.writeChar(vI);
	if (!getStage()) {
	    debug.writeChar(vI);
	}
    }

    /**
     * Writes a string to the <code>OutputStream</code> as an array of
     * characters.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the characters.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void writeChars(String vI)
	throws java.io.IOException
    {
	super.writeChars(vI);
	if (!getStage()) {
	    debug.writeChars(vI);
	}
    }

    /**
     * Writes a double to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method converts the double to a long using
     * doubleToLongBits and writes that.
     * <p>
     * In text mode, the method converts the double to a string and writes
     * that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the double.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the double.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 03/01/1997  INB	Created.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     *
     */
    public final void writeDouble(double vI)
	throws java.io.IOException
    {
	super.writeDouble(vI);
	if (!getStage()) {
	    debug.writeDouble(vI);
	}
    }

    /**
     * Writes a float to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method converts the float to an int using
     * floatToIntBits and writes that.
     * <p>
     * In text mode, the method converts the float to a string and writes that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the float.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the float.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeFloat(float vI)
	throws java.io.IOException
    {
	super.writeFloat(vI);
	if (!getStage()) {
	    debug.writeFloat(vI);
	}
    }

    /**
     * Writes an int to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the integer as four bytes.
     * <p>
     * In text mode, the method converts the integer to a string and writes
     * that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the int.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the int.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeInt(int vI)
	throws java.io.IOException
    {
	super.writeInt(vI);
	if (!getStage()) {
	    debug.writeInt(vI);
	}
    }

    /**
     * Writes a long to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the long as eight bytes.
     * <p>
     * In text mode, the method converts the long to a string and writes that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the long.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the long.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeLong(long vI)
	throws java.io.IOException
    {
	super.writeLong(vI);
	if (!getStage()) {
	    debug.writeLong(vI);
	}
    }

    /**
     * Writes a parameter to the <code>OutputStream</code>.
     * <p>
     * This method uses <code>writeElement()</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the list of parameters.
     * @param indexI	   the index of the parameter.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the parameter.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeParameter(String[] parametersI,int parameterI)
	throws java.io.IOException
    {
	super.writeParameter(parametersI,parameterI);
	if (!getStage()) {
	    debug.writeParameter(parametersI,parameterI);
	}
    }

    /**
     * Writes a short to the <code>OutputStream</code>.
     * <p>
     * In binary mode, the method writes the short as two bytes.
     * <p>
     * In text mode, the method converts the short to a string and writes that.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the short.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the short.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeShort(int vI)
	throws java.io.IOException
    {
	super.writeShort(vI);
	if (!getStage()) {
	    debug.writeShort(vI);
	}
    }

    /**
     * Writes a string to the <code>OutputStream</code>.
     * <p>
     * In binary mode, this method uses <code>writeUTF()</code>.
     * <p>
     * In text mode, this method uses <code>writeBytes()</code> and adds a
     * space character after the string, unless the string is a <CR><LF>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param vI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the string.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeString(String vI)
	throws java.io.IOException
    {
	super.writeString(vI);
	if (!getStage()) {
	    debug.writeString(vI);
	}
    }

    /**
     * Writes a string to the <code>OutputStream</code> as a UTF string.
     * <p>
     * The string is converted to UTF format (1, 2, or 3 bytes per
     * character). The length of the resulting byte array is written to the
     * <code>OutputStream</code> as an unsigned short value, followed by the
     * byte array.
     * <p>
     *
     * @author Ian Brown
     *
     * @param strI  the string.
     * @exception java.io.IOException
     *		  thrown if there is a problem writing the string.
     * @since V1.0
     * @version 03/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/28/2003  INB	Eliminated unnecessary synchronization.
     * 02/02/2001  INB	Modified for V2.0 (Rmap) RBNB.
     * 03/01/1997  INB	Created.
     *
     */
    public final void writeUTF(String strI)
	throws java.io.IOException
    {
	super.writeUTF(strI);
	if (!getStage()) {
	    debug.writeUTF(strI);
	}
    }
}
