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
 * Interface describing objects for which metrics can be collected.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.MetricsCollector
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
interface MetricsInterface
    extends com.rbnb.api.MetricsCollectorInterface
{

    /**
     * timer task - retrieve the metrics.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/15/2002
     */
    public final static String TT_METRICS = "Metrics";

    /**
     * Calculate the metrics for this <code>MetricsInterface</code> that apply
     * for the specified time range.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lastTimeI the last time metrics were calculated; if
     *			<code>Long.MIN_VALUE</code>, then this is the first
     *			time they've been calculated.
     * @param nowI	the current time.
     * @return an <code>Rmap</code> containing the metrics.
     * @see com.rbnb.api.MetricsCollector#timerTask(com.rbnb.api.TimerTask)
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
    public abstract Rmap calculateMetrics(long lastTimeI,long nowI);
}
