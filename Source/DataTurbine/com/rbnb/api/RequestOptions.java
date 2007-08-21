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
 * Data request options class.
 * <p>
 * Objects of this class modify the handling of requests by changing how the
 * server handles things.  For example, the server can be told to wait for
 * a specified period or until all of the data to arrive before responding.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.DataRequest
 * @since V2.2
 * @version 12/09/2003
 */

/*
 * Copyright 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/08/2003  INB	Added <code>extendStart</code> flag.
 * 06/11/2003  INB	Created.
 *
 */
public class RequestOptions
    extends com.rbnb.api.Serializable
{

    /**
     * extend the start time of a request rather than move it?
     * <p>
     * This option only works with requests that specify that the actual time
     * retrieved must be less than, or less than or equal to, the request
     * specified time.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/08/2003
     */
    private boolean extendStart = false;

    /**
     * the maximum time to wait for the data to appear in a request for
     * "existing" data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/11/2003
     */
    private long maxWait = 0L;

    private final static int PAR_MWT = 0;
    private final static int PAR_EXS = 1;

    private final static String[] PARAMETERS = {
			    "MWT",
			    "EXS"
			};

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    public RequestOptions() {
	super();
    }

    /**
     * Class constructor to build a <code>RequestOptions</code> object from the
     * specified input streams.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the control input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    RequestOptions(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	this();
	read(isI,disI);
    }

    /**
     * Copies the fields from the input <code>RequestOptions</code> into this
     * one.
     * <p>
     *
     * @author Ian Brown
     *
     * @param roI the <code>RequestOptions</code> to copy.
     * @since V2.2
     * @version 12/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Added <code>extendStart</code> handling.
     * 06/11/2003  INB	Created.
     *
     */
    final void copy(RequestOptions roI) {
	setExtendStart(roI.getExtendStart());
	setMaxWait(roI.getMaxWait());
    }

    /**
     * Gets the extend start flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return extend the start of the request?
     * @see #setExtendStart(boolean extendStartI)
     * @since V2.2
     * @version 12/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Created.
     *
     */
    public final boolean getExtendStart() {
	return (extendStart);
    }

    /**
     * Gets the maximum wait period.
     * <p>
     * This is the amount of time that the server will wait for "existing"
     * data to arrive.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum wait time in milliseconds.
     * @see #setMaxWait(long)
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    public final long getMaxWait() {
	return (maxWait);
    }

    /**
     * Reads the object from an input stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI   the input stream.
     * @param disI  the data input stream.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization. For
     *		  example, a missing bracket.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an error reading the input stream.
     * @exception java.lang.InterruptedException
     *		  thrown if the read is interrupted.
     * @see #write(String[],int,com.rbnb.api.OutputStream,com.rbnb.api.DataOutputStream)
     * @since V2.2
     * @version 12/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Added <code>extendStart</code> handling.
     * 06/11/2003  INB	Created.
     *
     */
    final void read(InputStream isI,DataInputStream disI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Read the open bracket marking the start of the
	// <code>RequestOptions</code>.
	Serialize.readOpenBracket(isI);

	int parameter;
	while ((parameter = Serialize.readParameter(PARAMETERS,isI)) != -1) {
	    switch (parameter) {
	    case PAR_MWT:
		setMaxWait(isI.readLong());
		break;
	    case PAR_EXS:
		setExtendStart(isI.readBoolean());
		break;
	    }
	}
    }

    /**
     * Sets the extend start flag.
     * <p>
     * If the flag is set and the request being worked on is for before, or
     * at or before, the request specified time, then the start of request is
     * extended from its specified time to the actual matching time.
     * Effectively, this actually extends the duration so that the end time of
     * the request is always equal to the request specified start time plus
     * the request specified duration.
     * <p>
     *
     * @author Ian Brown
     *
     * @param extendStartI extend the start time?
     * @see #getExtendStart()
     * @since V2.2
     * @version 12/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Created.
     *
     */
    public final void setExtendStart(boolean extendStartI) {
	extendStart = extendStartI;
    }

    /**
     * Sets the maximum wait period.
     * <p>
     * This is the amount of time that the server will wait for "existing"
     * data to arrive.
     * <p>
     * A value of zero (0) means don't wait, simply return whatever data is
     * available.
     * <p>
     * A positive value means wait for that amount of time or until all of the
     * data is available.  If the timeout period expires, return whatever data
     * is available at that time.
     * <p>
     * A negative value means wait until all of the data is available.
     * <p>
     *
     * @author Ian Brown
     *
     * @param maxWaitI the maximum wait period in milliseconds.
     * @see #getMaxWait()
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/11/2003  INB	Created.
     *
     */
    public final void setMaxWait(long maxWaitI) {
	maxWait = maxWaitI;
    }

    /**
     * Gets a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.2
     * @version 12/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Added <code>extendStart</code> handling.
     * 06/11/2003  INB	Created.
     *
     */
    public final String toString() {
	return ("RequestOptions:\n" +
		"   Extend Start: " + getExtendStart() + "\n" +
		"   Max Wait: " + getMaxWait() + " milliseconds.");
    }

    /**
     * Writes the object to an output stream.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parametersI  the object's parent's parameter list.
     * @param paramterI	   the parent parameter to use.
     * @param osI	   the output stream.
     * @param dosI	   the data output stream.
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
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #read(com.rbnb.api.InputStream,com.rbnb.api.DataInputStream)
     * @since V2.2
     * @version 12/08/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/08/2003  INB	Added <code>extendStart</code> handling.
     * 06/11/2003  INB	Created.
     *
     */
    final void write(String[] parametersI,
			int parameterI,
			OutputStream osI,
			DataOutputStream dosI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (IsSupported.isSupported(IsSupported.FEATURE_REQUEST_OPTIONS,
				    osI.getBuildVersion(),
				    osI.getBuildDate())) {
	    osI.writeParameter(parametersI,parameterI);
	    Serialize.writeOpenBracket(osI);

	    osI.writeParameter(PARAMETERS,PAR_MWT);
	    osI.writeLong(getMaxWait());

	    if (IsSupported.isSupported
		(IsSupported.FEATURE_OPTION_EXTEND_START,
		 osI.getBuildVersion(),
		 osI.getBuildDate())) {
		osI.writeParameter(PARAMETERS,PAR_EXS);
		osI.writeBoolean(getExtendStart());
	    }

	    Serialize.writeCloseBracket(osI);
	}
    }

    /**
     * Writes out staging information for this <code>Serializable</code>.
     * <p>
     * By staging <code>Serializable</code> objects, the I/O requirements are
     * reduced when writing collapsable objects such as <code>Rmaps</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param osI	  the output stream.
     * @param parametersI the parameters array.
     * @param parameterI  the index into the parameters array.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @since V2.2
     * @version 06/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/15/2001  INB	Created.
     *
     */
    final void writeStaged(OutputStream osI,
			   String[] parametersI,
			   int parameterI)
	throws java.io.IOException
    {
	if (IsSupported.isSupported(IsSupported.FEATURE_REQUEST_OPTIONS,
				    osI.getBuildVersion(),
				    osI.getBuildDate())) {
	    super.writeStaged(osI,parametersI,parameterI);
	}
    }
}
