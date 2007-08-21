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
 * Extended abstract <code>StreamListener</code> that Listens for data handler
 * (such as <code>RBO</code> or <code>NBO</code>) "events".
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/14/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/11/2003  INB	Ensure that we don't lose the relationship in
 *			requests.
 * 11/07/2003  INB	Account for <code>StreamTimeRelativeListeners</code>
 *			when starting.
 * 11/06/2003  INB	Handle request with relationships.
 * 06/16/2003  INB	Handle <code>RequestOptions.maxWait</code>.
 * 03/27/2001  INB	Created.
 *
 */
abstract class StreamDHListener
    extends com.rbnb.api.StreamListener
{

    /**
     * Class constructor to build a <code>StreamDHListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>Rmap</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI   our parent.
     * @param requestI  the request <code>Rmap</code
     * @param sourceI   the <code>NotificationFrom</code> source.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 01/16/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/04/2001  INB	Created.
     *
     */
    StreamDHListener(StreamParent parentI,
		     Rmap requestI,
		     NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
    }

//EMF 9/19/05
public String toString() {
return new String("StreamDHListener:"+super.toString());
}

    /**
     * Creates the original request.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 11/11/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/11/2003  INB	Ensure that we don't lose the relationship in
     *			requests.
     * 11/06/2003  INB	Handle request with relationships.
     * 03/27/2001  INB	Created.
     *
     */
    final void createOriginal()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Our ultimate ancestor has the original request <code>Rmap</code> to
	// be matched. That should provide us with information about what we're
	// doing.
	DataRequest base =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	// The working request is our input request plus any time information
	// from our parentage, plus any <code>DataRequest</code> information
	// off of the base request.
	Rmap wOriginal = (Rmap) getRequest().clone();

	StreamParent ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof StreamListener) {
		StreamListener aListener = (StreamListener) ancestor;
		Rmap aRequest;
		if ((aRequest = aListener.getRequest()) != null) {
		    TimeRange aRange;
		    if ((aRange = aRequest.getTrange()) != null) {
			if (wOriginal.getTrange() == null) {
			    wOriginal.setTrange(aRange);
			} else {
			    wOriginal.setTrange
				(aRange.add(wOriginal.getTrange()));
			}

		    } else if (wOriginal.getFrange() == null) {
			wOriginal.setFrange(aRequest.getFrange());
		    }
		}
		ancestor = ((StreamListener) ancestor).getParent();
	    } else {
		break;
	    }
	}

	if (base == null) {
	    setOriginal(new DataRequest());
	    getOriginal().addChild(wOriginal);
	} else {
	    setOriginal(new DataRequest(null,
				       null,
				       null,
				       base.getReference(),
				       base.getRelationship(),
				       base.getDomain(),
				       base.getNrepetitions(),
				       base.getIncrement(),
				       base.getSynchronized(),
				       base.getMode(),
				       base.getGapControl()));
	    getOriginal().addChild(wOriginal);
	}
    }

    /**
     * Creates the working <code>DataRequest</code> from the original
     * <code>DataRequest</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 10/25/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/05/2001  INB	Created.
     *
     */
    abstract void createWorking()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Starts this <code>StreamDHListener</code> running.
     * <p>
     * 
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @see #run()
     * @see #stop()
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 11/07/2003  INB	Account for <code>StreamTimeRelativeListeners</code>.
     * 06/16/2003  INB	Handle <code>RequestOptions.maxWait</code>.
     * 03/27/2001  INB	Created.
     *
     */
    final void start()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	DataRequest base =
	    ((getBaseRequest() instanceof DataRequest) ?
	     ((DataRequest) getBaseRequest()) :
	     null);

	if (((base != null) &&
	     (base.getMode() != DataRequest.CONSOLIDATED)) ||
	    ((getNBO().getRequestOptions() != null) &&
	     (getNBO().getRequestOptions().getMaxWait() != 0))) {
	    (new AwaitNotification(null,getSource(),this)).start();
	} else if (this instanceof StreamPlugInListener) {
	    (new AwaitNotification(null,getSource(),this)).start();
	} else if (this instanceof StreamTimeRelativeListener) {
	    (new AwaitNotification(null,getSource(),this)).start();
	}
	try {
	    createOriginal();
	    createWorking();
	    setThread
		(new ThreadWithLocks
		    (this,
		     ("_SDL." +
		      getNBO().getName() +
		      Rmap.PATHDELIMITER +
		      ((Rmap)
		       ((StreamListener)
			getParent()).getSource()).getName() +
		      Rmap.PATHDELIMITER +
		      ((Rmap) getSource()).getName())));
	    getThread().start();

	} catch (com.rbnb.api.AddressException e) {
	    getNBO().asynchronousException(e);
	    throw e;

	} catch (com.rbnb.api.SerializeException e) {
	    getNBO().asynchronousException(e);
	    throw e;

	} catch (java.io.IOException e) {
	    getNBO().asynchronousException(e);
	    throw e;

	} catch (java.lang.InterruptedException e) {
	    getNBO().asynchronousException(e);
	    throw e;

	} catch (java.lang.RuntimeException e) {
	    getNBO().asynchronousException(e);
	    throw e;
	}
    }
}
