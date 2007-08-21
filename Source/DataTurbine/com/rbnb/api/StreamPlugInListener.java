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
 * Extended <code>StreamDHListener</code> that listens for <code>PlugIn</code>
 * "events".
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.RBNBPlugIn
 * @since V2.0
 * @version 03/12/2007
 */

/*
 * Copyright 2002, 2003, 2004, 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/14/2007  WHF      Handle error caused by a sink Monitoring a PlugIn
 *                      which has disconnected and is then trying to reconnect.
 *                      Wait until it is ready before forwarding messages.
 * 03/12/2007  WHF      Subscription modes handled here as repeated requests.
 * 11/18/2005  JPW	For Monitor mode, no longer call post() directly from
 *			accept(); for Monitor, post() will be called from the
 *			thread executing in the run() method.  For Monitor,
 *			we wait for an RSVP reply from the client before
 *			sending out the next Rmap.  With post() called from
 *			run(), we can send out the newest Rmap as soon as we
 *			receive the RSVP (we won't have to wait until accept()
 *			is called again).
 * 08/05/2004  INB	Added link to com.rbnb.api.RBNBPlugIn.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.  Added identification to <code>Door</code>.
 * 06/16/2003  INB	Added handling of <code>RequestOptions</code>.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 01/18/2002  INB	Created.
 *
 */
final class StreamPlugInListener
    extends com.rbnb.api.StreamDHListener
{
    
    /**
     * For Monitor mode, this is the next event to post to the client
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.0
     * @version 11/18/2005
     */
    private Serializable newestEvent = null;
    
    /**
     * Class constructor to build a <code>StreamPlugInListener</code> for the
     * specified code>StreamParent</code>, request <code>Rmap</code>, and
     * <code>NotificationFrom</code> source.
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
     * @version 08/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    StreamPlugInListener(StreamParent parentI,
			 Rmap requestI,
			 NotificationFrom sourceI)
	throws java.lang.InterruptedException
    {
	super(parentI,requestI,sourceI);
	if (requestI instanceof DataRequest) {
	    DataRequest dr = (DataRequest) requestI;
	    if (dr.getNrepetitions() != 1) {
		throw new java.lang.IllegalStateException
		    ("PlugIns do not yet support requests with repetition " +
		     "(such as for monitor or subscription requests).");
	    }
	}
    }

    /**
     * Class constructor to build a <code>StreamPlugInListener</code> for the
     * specified code>StreamParent</code>, <code>DataRequest</code>, and
     * <code>PlugInHandler</code> source.
     * <p>
     *
     * @author Ian Brown
     *
     * @param parentI     our parent.
     * @param requestI    the <code>DataRequest</code
     * @param plugInI     the <code>PlugInHandler</code source.
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
     * 01/18/2002  INB	Created.
     *
     */
    StreamPlugInListener(StreamParent parentI,
			 DataRequest requestI,
			 PlugInHandler plugInI)
	throws java.lang.InterruptedException
    {
	this(parentI,requestI,(NotificationFrom) plugInI);
    }

//EMF 9/19/05
public String toString() {
return new String("StreamPlugInListener:"+super.toString());
}

    /**
     * Accepts a notification event <code>Serializable</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param eventI the "event" <code>Serializable</code>.
     * @param matchI the matched request <code>Rmap</code>.
     * @exception java.lang.InterruptedException
     *		  thrown if the post operation is interrupted.
     * @since V2.0
     * @version 11/18/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/18/2005  JPW	For Monitor mode, no longer call post() directly from
     *                  accept(); for Monitor, post() will be called from the
     *                  thread executing in the run() method.  For Monitor,
     *			we wait for an RSVP reply from the client before
     *			sending out the next Rmap.  With post() called from
     *			run(), we can send out the newest Rmap as soon as we
     *			receive the RSVP (we won't have to wait until accept()
     *			is called again).
     * 01/18/2002  INB	Created.
     *
     */
    public final synchronized void accept(Serializable eventI,Rmap matchI)
        throws java.lang.InterruptedException
    {
	
	if (eventI == null) {
	    return;
	}
	
	// JPW 11/18/2005: If we are in Monitor mode, save eventI and wait to
	//                 call post() asynchronously from run()
	
//	if (isMonitorMode()) {
    	// 2006/11/07  WHF  Always handle logic in run() loop.
	if (true) {
//Thread.dumpStack();	    
	    // NOTE: Since this method is synchronized, we don't need to
	    //       synchronize when we store this object
	    newestEvent = eventI;
	    // Tickle the thread executing in run() that a new event is ready
	    notifyAll();
	} else {
	    try {
		post(eventI);
	    } catch (com.rbnb.api.AddressException e) {
		stop();
	    } catch (com.rbnb.api.SerializeException e) {
		stop();
	    } catch (java.io.IOException e) {
		stop();
	    }
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
     * @version 11/07/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/18/2002  INB	Created.
     *
     */
    final void createWorking()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// The working request needs to have a name equal to our
	// <code>AwaitNotification</code> object's name. We'll just go ahead
	// and use the "original" request, as we don't actually need anything
	// else.
	if ((getAnotification() != null) &&
	    (getAnotification().size() == 1)) {
	    getOriginal().setName
		(((AwaitNotification)
		  getAnotification().firstElement()).getName());
	    Rmap me = getOriginal().getChildAt(0);
	    getOriginal().removeChildAt(0);
	    while (me.getNchildren() > 0) {
		Rmap child = me.getChildAt(0);
		me.removeChildAt(0);
		if (me.getTrange() != null) {
		    if (child.getTrange() == null) {
			child.setTrange(me.getTrange());
		    } else {
			child.setTrange(me.getTrange().add(child.getTrange()));
		    }
		}
		if (child.getFrange() == null) {
		    child.setFrange(me.getFrange());
		}
		getOriginal().addChild(child);
	    }

	} else {
	    throw new java.lang.IllegalStateException
		("Software error:\n" + this + " is in a bad state.");
	}
    }
    
    /**
     * Are we in Monitor mode?
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 11/18/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/18/2005  JPW	Created.
     *
     */
    
    private boolean isMonitorMode() {
	
	// NOTE: In StreamRBOListener we detect Monitor mode with the
	//       following test:
	// if ( (subWorking instanceof DataRequest)      &&
	//      (((DataRequest)subWorking).getDomain() ==
	//                            DataRequest.FUTURE) &&
	//      (((DataRequest)subWorking).getMode() ==
	//                            DataRequest.FRAMES) &&
	//      (((DataRequest)subWorking).getReference() ==
	//                            DataRequest.NEWEST) )
	
	DataRequest dr=((getBaseRequest() instanceof DataRequest) ?
                       ((DataRequest)getBaseRequest()) :
                       null);
	if ( (dr!=null) && (dr.getGapControl()) ) {
	    return true;
	}
	
	return false;
    }
    
    /**
     * Posts a response <code>Rmap</code> to the application.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serializableI  the <code>Serializable</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a serialization problem.
     * @exception java.lang.InterruptedIOException
     *		  thrown if this operation is interrupted during I/O.
     * @exception java.io.IOException
     *		  thrown if there is an I/O problem.
     * @exception java.lang.InterruptedException
     *		  thrown if this operation is interrupted.
     * @since V2.0
     * @version 02/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/16/2002  INB	Created.
     *
     */
    public final void post(Serializable serializableI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.InterruptedIOException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Serializable serializable;
	Rmap eos = null,
	    me = ((Rmap) getSource()).newInstance(),
	    highest,
	    child;
	me.setParent(null);

	if (serializableI instanceof EndOfStream) {
	    eos = (Rmap) serializableI;
	    highest = new Rmap();
	    if (eos.compareNames(".") != 0) {
		highest.setName(eos.getName());
	    }
	    eos.setName(null);
	    highest.setTrange(eos.getTrange());
	    eos.setTrange(null);
	    highest.setFrange(eos.getFrange());
	    eos.setFrange(null);
	    while (eos.getNchildren() > 0) {
	        child = eos.getChildAt(0);
		eos.removeChildAt(0);
		highest.addChild(child);
	    }
	} else {
	    highest = (Rmap) serializableI;
	}

	if (highest != null) {
	    me.addChild(highest);
	}
	if (eos != null) {
	    eos.addChild(me);
	    serializable = eos;
	} else {
	    serializable = me;
	}
	
        //EMF 11/1/05: add RSVP if monitoring, so don't fall behind
        //             code adapted from StreamRBOListener.post
	// JPW 11/18/2005: Add call to isMonitorMode()
	if (isMonitorMode()) {
	    setWaiting(true);
	    serializable = new RSVP(0,serializable);
        }
	
	super.post(serializable);
    }
    
    private void processEvent() throws
    	AddressException,
    	java.io.InterruptedIOException,
	InterruptedException,
	java.io.IOException,
	SerializeException
    {
	// 2006/10/31  WHF  Experimentation with PlugIn Monitor
	//		polling:
	/*  original code
	((PlugInHandler) getSource()).initiateRequest
	    (getOriginal(),
	     getNBO().getRequestOptions());
	while (!getTerminateRequested()) {
	*/
	
	// 2006/11/08  WHF  Here I define streaming as a request which expects
	//   more than one answer, i.e. Monitor or Subscribe.
	DataRequest original = (DataRequest) getOriginal();
	boolean isStreaming = original.getNrepetitions() > 1;
	boolean doRequest = true;
	TimeRange originalTR;
	if (original.getNchildren() > 0)
	    originalTR = original.getChildAt(0).getTrange();
	else
	    originalTR = new TimeRange(0.0, 0.0);
	
	DataRequest request;	
	
	if (isStreaming) {
	    request = (DataRequest) (original.clone());
	    // Set repetition count to 1, with optional frame skipping:
	    request.setRepetitions(1, original.getGapControl() ? 1.0 : 0.0); 
	    if (request.getMode() == DataRequest.FRAMES) {
		// Either Frame based subscription or Monitor:
		/* 2007/03/12  WHF  We can now just forward, after
		    setting the repetition count to one.
		request.setMode(DataRequest.CONSOLIDATED);
		request.setReference(DataRequest.NEWEST);
		request.getChildAt(0).setTrange(new TimeRange(0, 0.0));
		request.getChildAt(0).setFrange(null);
		*/
	    } else {
		// Time based subscription.
	    }
	} else request = original;
	
	while (true) {
	    if (doRequest) {
		PlugInHandler pih = (PlugInHandler) getSource(); 
		// 2007/03/14  WHF  Try to handle Monitor reconnects:
		for (int ii = 0; pih.getIsInStartup() && ii < 100; ++ii) {
		    Thread.sleep(100);
		}
//System.err.println("Requesting: "+request);
		pih.initiateRequest(
			request,
			getNBO().getRequestOptions()
		);
		doRequest = false;
	    }
	    
	    if (getTerminateRequested()) {
//System.err.println("\nREQUEST TERMINATED\n");			
		break;
	    }
       
	    synchronized (this) {
		// JPW 11/18/2005: If we are in Monitor mode, and
		//                 we aren't waiting, and there is a
		//                 new event send out, then call post()
		if ( //(isMonitorMode()) &&
			 (!getWaiting()) &&
			 (newestEvent != null) ) {
		    if (isStreaming) {	   
			if (newestEvent instanceof EndOfStream) {
//System.err.println(newestEvent);
			    if (((EndOfStream) newestEvent).getReason() != 
				    EndOfStream.REASON_END 
				    || ((Rmap) newestEvent).getNchildren() == 0) {
			        // Actual error, no data.  Consume?
				newestEvent = null;
				continue;
			    } else {
				newestEvent = ((Rmap) newestEvent).getChildAt(0);
				((Rmap) newestEvent).getParent().removeChildAt(0);
			    }
			}			    
			
			TimeRange tr = ((Rmap) newestEvent).summarize().getTrange();
    //System.err.println(tr);
			/* 2007/03/12  WHF  Original frame-based to time-based
					translation:
			if (tr != null) {
			    // NOTE: We are in a synchronized block, so don't
			    //       have to add additional synchronization
			    //       to protect access to newestEvent
			    post(newestEvent);

			    // Change request for next time:
			    request = (DataRequest) request.duplicate();
			    request.setRepetitions(1, 0.0); // no repeats!			
			    request.setMode(DataRequest.CONSOLIDATED);
			    double magicOffset;
			    if (original.getGapControl()) {
				// Monitor mode:
				request.setReference(DataRequest.AFTER);
			        // This magic offset makes AFTER work as of 2006/11/06.
				//magicOffset = 2e-7;
				// 2007/02/23  WHF  Changed magic offset to 
				//    4e-3 because the old value 'magically'
				//    no longer works.
				//magicOffset = 4e-3;
				// 2007/02/23  WHF  Holy smoke!  After recompiling,
				//    we need this magic number!!!
				magicOffset = .5;
			    } else {
				// Subscribe, set up 'next' or 'prev' request:
				request.setReference(DataRequest.ABSOLUTE);
				request.setRelationship(
					original.getIncrement() > 0?
					DataRequest.GREATER :
					DataRequest.LESS
				);
				magicOffset = 0; // Next seems to work without
			    }
			    // The time range for the request is stored in the first child:
			    request.getChildAt(0).setTrange(new TimeRange(
				    tr.getTime() + magicOffset, 0.0));
			    request.getChildAt(0).setFrange(null);
			} else {
			    // No data received, resend request after timeout:
			    // (I did not use a wait() here, because I want a delay before requesting
			    //   that cannot be altered by the action of other threads.
			    Thread.sleep(100);
			} // end if (tr != null) */
			
			// 2007/03/12  WHF  Frame based requests:
			if (tr != null) {
			    // NOTE: We are in a synchronized block, so don't
			    //       have to add additional synchronization
			    //       to protect access to newestEvent
			    post(newestEvent);

			    if (original.getMode() != DataRequest.FRAMES) {
				// Time based subscription.
				// Change request for next time:
				request = (DataRequest) request.duplicate();
						    
				TimeRange tRange, fRange;
				//if (original.getGapControl()) {
				// Subscribe, time based,
				//  set up 'next' or 'prev' request:
				request.setRepetitions(1, 0.0); // no repeats!
				request.setMode(DataRequest.CONSOLIDATED);
				request.setReference(DataRequest.ABSOLUTE);
				request.setRelationship(
					original.getIncrement() > 0?
					DataRequest.GREATER :
					DataRequest.LESS
				);
				tRange = new TimeRange(
					tr.getTime() + tr.getDuration(), 
					originalTR.getDuration());
				fRange = null;
			    
				// The time range for the request is stored in the first child:
				// 2007/03/12  WHF  It might be necessary to set the time
				//     for all children for a request which spans
				//     multiple channels.
				for (int idx = 0; idx < request.getNchildren(); ++idx) {
				    request.getChildAt(idx).setFrange(fRange);
				    request.getChildAt(idx).setTrange(tRange);
				}
			    } // otherwise, we can reuse the original request.
			} else {
			    // No data received, resend request after timeout:
			    // (I did not use a wait() here, because I want a delay before requesting
			    //   that cannot be altered by the action of other threads.
			    Thread.sleep(100);
			}
			
			doRequest = true;
		    } else {
			// Not streaming, just post the event:
			post(newestEvent);
		    } //end if (isStreaming)

		    // Now that we've sent the event off, set our
		    // class member to null (so we don't end up
		    // posting it again!)
		    newestEvent = null;
		    // Pop back up to check if a terminate has been
		    // requested without sleeping; for Monitor mode,
		    // we'll only call wait() if there was no data to
		    // post
		    continue;
		}
		wait(TimerPeriod.NORMAL_WAIT);
	    }
	}
    }	
	
    /**
     * Runs this <code>StreamPlugInListener</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.  Added identification to <code>Door</code>.
     * 06/16/2003  INB	Added handling of <code>RequestOptions</code>.
     * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
     * 01/18/2002  INB	Created.
     *
     */
    public final void run() {
	getDoor().setIdentification(toString());

	try {
	    if ((getSource() instanceof ClientHandler) &&
		    !((ClientHandler) getSource()).allowAccess(getNBO())) {
		post(new EndOfStream(EndOfStream.REASON_EOD));
		setEOS(true);
		return;

	    } else {
		processEvent();
	    } // end if ClientHandler

	} catch (java.io.InterruptedIOException e) {
	} catch (java.lang.InterruptedException e) {
	} catch (java.lang.Exception e) {
	    try {
		getNBO().asynchronousException(e);
	    } catch (java.lang.Exception e1) {
	    }
	    setEOS(true);

	} finally {
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
	    }
	}

	if (!getTerminateRequested()) {
	    // If this thread was not terminated on a request from another
	    // thread, then go through the termination work here.
	    try {
		stop();
	    } catch (java.lang.Exception e) {
	    }
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).clearLocks();
	    }
	}

	// Notify any waiting thread.
	setTerminateRequested(false);
	setThread(null);
    }
}
