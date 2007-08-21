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
 * Collects metrics information for an object implementing
 * <code>MetricsCollectorInterface</code> into a ring buffer.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.MetricsCollectorInterface
 * @since V2.0
 * @version 11/20/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/15/2002  INB	Created.
 *
 */
final class MetricsCollector
    implements com.rbnb.api.TimerTaskInterface
{
    /**
     * the object for which metrics are to be collected.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/15/2002
     */
    private MetricsCollectorInterface object = null;

    /**
     * the source connection to the server into which to place the metrics
     * information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/15/2002
     */
    private Source source = null;

    /**
     * last time that metrics were taken.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/15/2002
     */
    private long lastTime = Long.MIN_VALUE;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2002  INB	Created.
     *
     */
    MetricsCollector() {
	super();
    }

    /**
     * Gets the <code>MetricsCollectorInterface</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>MetricsCollectorInterface</code> object.
     * @see #setObject(com.rbnb.api.MetricsCollectorInterface)
     * @since V2.0
     * @version 11/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2002  INB	Created.
     *
     */
    final MetricsCollectorInterface getObject() {
	return (object);
    }

    /**
     * Gets the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Source</code>.
     * @see #setSource(com.rbnb.api.Source)
     * @since V2.0
     * @version 11/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2002  INB	Created.
     *
     */
    final Source getSource() {
	return (source);
    }

    /**
     * Sets the <code>MetricsCollectorInterface</code> object.
     * <p>
     *
     * @author Ian Brown
     *
     * @param objectI the <code>MetricsCollectorInterface</code> object.
     * @see #getObject()
     * @since V2.0
     * @version 11/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2002  INB	Created.
     *
     */
    final void setObject(MetricsCollectorInterface objectI) {
	object = objectI;
    }

    /**
     * Sets the <code>Source</code> object to which to send the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceI the <code>Source</code>.
     * @see #getSource()
     * @since V2.0
     * @version 11/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2002  INB	Created.
     *
     */
    final void setSource(Source sourceI) {
	source = sourceI;
    }

    /**
     * Executes a task on a timer.
     * <p>
     * The input code string specifies which of an optional number of specific
     * tasks is to be performed.  The tasks are:
     * <p><ul>
     * <li><code>MetricsInterface.TT_METRICS</code> - calculates and stores a
     *	   frame of metrics information,</li>
     * <li><code>LogStatusInterface.TT_LOG_STATUS</code> - logs the status
     *	   (existance, channel list, etc.) of the object.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param codeI the task-specific code string.
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/15/2002  INB	Created.
     *
     */
    public final void timerTask(TimerTask ttI) {
	try {
	    if (ttI.getCode().equals(MetricsInterface.TT_METRICS)) {
		long now = System.currentTimeMillis();
		Rmap frame =
		    ((MetricsInterface) getObject()).calculateMetrics
		    (lastTime,
		     now);
		if (frame != null) {
		    getSource().addChild(frame);
		}
		lastTime = now;

	    } else if (ttI.getCode().equals
		       (LogStatusInterface.TT_LOG_STATUS)) {
		((LogStatusInterface) getObject()).logStatus("Is running");
	    }

	} catch (java.lang.Exception e) {
	}
    }

    /**
     * String representation of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 11/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/20/2002  INB	Created.
     *
     */
    public final String toString() {
	String stringR = "MetricsCollector: ";

	if (getObject() instanceof Rmap) {
	    Rmap rmap = (Rmap) getObject();

	    try {
		stringR += rmap.getFullName();
	    } catch (java.lang.Exception e) {
		stringR += rmap;
	    }

	} else {
	   stringR += getObject();
	}

	return (stringR);
    }
}
