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
 * <code>Rmap</code> with metrics information.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 05/22/2003
 */

/*
 * Copyright 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 05/22/2003  INB	Made <code>getDataSize</code> final.
 * 11/19/2002  INB	Created.
 *
 */
class RmapWithMetrics
    extends com.rbnb.api.Rmap
{
    /**
     * data size of this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    private long dataSize = -1;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    RmapWithMetrics() {
	super();
    }

    /**
     * Gets the data size of this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data size.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.ArrayIndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #setDataSize(long dataSizeI)
     * @since V2.0
     * @version 05/22/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/22/2003  INB	Made final.
     * 11/19/2002  INB	Created.
     *
     */
    final long getDataSize()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	long dataSizeR = dataSize;

	if (dataSizeR == -1) {
	    dataSizeR = super.getDataSize();
	}

	return (dataSizeR);
    }

    /**
     * Sets the data size of this <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param dataSizeI the data size.
     * @see #getDataSize()
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    final void setDataSize(long dataSizeI) {
	dataSize = dataSizeI;
    }
}

