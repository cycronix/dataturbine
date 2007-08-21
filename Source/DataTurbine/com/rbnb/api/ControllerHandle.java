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
 * Client-side object that represents a client controller application
 * connection to an RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/19/2002
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/23/2007  WHF  Made public so that methods specific to this implemenation
 *     of Controller could be accessed without adding them to the Controller
 *     interface.
 * 05/11/2001  INB	Created.
 *
 */
public class ControllerHandle
    extends com.rbnb.api.ClientHandle
    implements com.rbnb.api.Controller,
	       com.rbnb.api.IOMetricsInterface
{

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    ControllerHandle() {
	super();
    }

    /**
     * Class constructor to build a <code>ControllerHandle</code> by reading it
     * in.
     * <p>
     *
     * @author Ian Brown
     *
     * @param isI  the <code>InputStream</code>.
     * @param disI the <code>DataInputStream</code>.
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
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    ControllerHandle(InputStream isI,DataInputStream disI)
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
     * Class constructor to build a <code>ControllerHandle</code> from a name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI the name.
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
     * @exception java.lang.IllegalArgumentException
     *		  thrown if <code>repetitionsI</code> is negative or zero.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/11/2001  INB	Created.
     *
     */
    ControllerHandle(String nameI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	super(nameI);
    }

    /**
     * Calculates the total number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of bytes transferred.
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
    public final long bytesTransferred() {
	long bytesR = ((getACO() == null) ?
		       metricsBytes :
		       getACO().bytesTransferred());

	return (bytesR);
    }

    /**
     * Mirrors data from or to a remote RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param mirrorI  the <code>Mirror</code> command.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the check is interrupted.
     * @since V2.0
     * @version 12/18/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/17/2001  INB	Created.
     *
     */
    public final void mirror(Mirror mirrorI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getACO().send((Serializable) mirrorI);
	getACO().receive(ACO.okClass,false,Client.FOREVER);
    }

    /**
     * Creates a new instance of the same class as this
     * <code>ControllerHandle</code> (or a similar class).
     * <p>
     *
     * @author Ian Brown
     *
     * @return the new instance.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 08/02/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/02/2001  INB	Created.
     *
     */
    Rmap newInstance()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	return (ControllerIO.newInstance(this));
    }

    /**
     * Creates a routing connection from the remote side to this side from this
     * <code>Controller</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param reverseI  the <code>ReverseRoute</code>.
     * @param sHandlerI the <code>ServerHandler</code> for this side.
     * @return the <code>RouterHandler</code> that this <code>Controller</code>
     *	       has been converted to.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem serializing an object.
     * @exception java.io.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is a problem during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 03/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2002  INB	Created.
     *
     */
    final RouterHandler reverseRoute(ReverseRoute reverseI,
				     ServerHandler sHandlerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	RouterInterface rI = new RouterIO();
	rI.setName(getName());
	getACO().send(reverseI);
	getACO().metricsBytes += metricsBytes;
	metricsBytes = 0;
	RCO rco = getACO().convertToRCO(sHandlerI);
	setACO(null);
	setStarted(false);
	RBNBRouter routerR = new RBNBRouter(rco,rI);
	rco.start();

	return (routerR);
    }
    
    public String fetchAddressAuthorization(ServerInterface si)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getACO().send(new GetAddressAuthorization(si));
	
	DataBlock response = (DataBlock) getACO().receive(
		DataBlock.class,
		false,
		true,
		1000
	);
	
	if (response == null) return null;
	return ((String) response.getData().get(0));
    }
    
    public void sendAddressAuthorization(ServerInterface si, String auth)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
   {
       getACO().send(new SetAddressAuthorization(si, auth));
   }
}
