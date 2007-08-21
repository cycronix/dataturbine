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

import com.rbnb.api.DataBlock;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;
import com.rbnb.api.Source;
import com.rbnb.api.TimeRange;

import java.util.Date;

/**
 * Simple source application using the RBNB API.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 04/13/2001
 */

/*
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 01/03/2001  INB	Created.
 *
 */
public class SimpleSource extends Thread {
    /**
     * disconnect after run?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private boolean disconnectAfterRun = true;

    /**
     * insert an extra level of hierarchy?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/03/2001
     */
    private boolean extraLevel = true;

    /**
     * more frames to send out?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private boolean moreFrames = true;

    /**
     * offset channels in time?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/03/2001
     */
    private boolean offsetChannels = true;

    /**
     * are we running?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    private boolean running = false;

    /**
     * run in timing mode?
     * <p>
     * When a <code>SimpleSource</code> is run in timing mode, it times how
     * long various operations take, including the total of the addChild()
     * calls. The number of frames is always limited (default is 1000) and
     * there is no sleep time between frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private boolean timing = false;

    /**
     * archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/27/2001
     */
    private byte aMode = Source.ACCESS_NONE;

    /**
     * the data values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2001
     */
    private float[][][] data = null;

    /**
     * the number of cache <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/23/2001
     */
    private int cacheFrameSets = 2;

    /**
     * the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private int numChans = 1;

    /**
     * the number of points per channel per frame.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private int numPoints = 10;

    /**
     * time to sleep between frames in milliseconds.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private int sleepTime = 1000;

    /**
     * the number of archive frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/23/2001
     */
    private long archiveFrames = 0;

    /**
     * the number of cache frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/23/2001
     */
    private long cacheFrames = -1;

    /**
     * the number of frames to produce.
     * <p>
     * A value of -1 is treated as infinite.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/17/2001
     */
    private long numFrames = -1;

    /**
     * the <code>Server</code> that this source application is attached to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2001
     */
    private Server server = null;

    /**
     * the <code>Source</code> object that this source application uses to
     * communicate with the actual <code>Source</code> object in the RBNB
     * DataTurbine <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2001
     */
    private Source source = null;

    /**
     * the name of the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/03/2001
     */
    private String name = null;

    // Private constants:
    private final static float PREGEN[] = {
	0f, 1.22461e-16f, 0f, 0.983871f, 1.20485e-16f,
	-0.983871f, 0f, 0.838089f, 0.838089f, 1.1851e-16f,
	-0.838089f, -0.838089f, 0f, 0.672892f, 0.951613f,
	0.672892f, 1.16535e-16f, -0.672892f, -0.951613f, -0.672892f,
	0f, 0.549864f, 0.889698f, 0.889698f, 0.549864f,
	1.1456e-16f, -0.549864f, -0.889698f, -0.889698f, -0.549864f,
	0f, 0.459677f, 0.796185f, 0.919355f, 0.796185f,
	0.459677f, 1.12585e-16f, -0.459677f, -0.796185f, -0.919355f,
	-0.796185f, -0.459677f, 0f, 0.391895f, 0.70617f,
	0.88058f, 0.88058f, 0.70617f, 0.391895f, 1.1061e-16f,
	-0.391895f, -0.70617f, -0.88058f, -0.88058f, -0.70617f,
	-0.391895f, 0f, 0.339477f, 0.627272f, 0.819571f,
	0.887097f, 0.819571f, 0.627272f, 0.339477f, 1.08634e-16f,
	-0.339477f, -0.627272f, -0.819571f, -0.887097f, -0.819571f,
	-0.627272f, -0.339477f, 0f, 0.297889f, 0.559847f,
	0.75428f, 0.857736f, 0.857736f, 0.75428f, 0.559847f,
	0.297889f, 1.06659e-16f, -0.297889f, -0.559847f, -0.75428f,
	-0.857736f, -0.857736f, -0.75428f, -0.559847f, -0.297889f,
	0f, 0.26416f, 0.502462f, 0.691579f, 0.813f,
	0.854839f, 0.813f, 0.691579f, 0.502462f, 0.26416f,
	1.04684e-16f, -0.26416f, -0.502462f, -0.691579f, -0.813f,
	-0.854839f, -0.813f, -0.691579f, -0.502462f, -0.26416f,
	0f, 0.236292f, 0.453441f, 0.633854f, 0.762917f,
	0.830173f, 0.830173f, 0.762917f, 0.633854f, 0.453441f,
	0.236292f, 1.02709e-16f, -0.236292f, -0.453441f, -0.633854f,
	-0.762917f, -0.830173f, -0.830173f, -0.762917f, -0.633854f,
	-0.453441f, -0.236292f, 0f, 0.2129f, 0.41129f,
	0.581652f, 0.712376f, 0.794552f, 0.822581f, 0.794552f,
	0.712376f, 0.581652f, 0.41129f, 0.2129f, 1.00734e-16f,
	-0.2129f, -0.41129f, -0.581652f, -0.712376f, -0.794552f,
	-0.822581f, -0.794552f, -0.712376f, -0.581652f, -0.41129f,
	-0.2129f, 0f, 0.192997f, 0.374777f, 0.534776f,
	0.663697f, 0.754045f, 0.800572f, 0.800572f, 0.754045f,
	0.663697f, 0.534776f, 0.374777f, 0.192997f, 9.87586e-17f,
	-0.192997f, -0.374777f, -0.534776f, -0.663697f, -0.754045f,
	-0.800572f, -0.800572f, -0.754045f, -0.663697f, -0.534776f,
	-0.374777f, -0.192997f, 0f, 0.175863f, 0.342908f,
	0.492758f, 0.617899f, 0.712056f, 0.770508f, 0.790323f,
	0.770508f, 0.712056f, 0.617899f, 0.492758f, 0.342908f,
	0.175863f, 9.67834e-17f, -0.175863f, -0.342908f, -0.492758f,
	-0.617899f, -0.712056f, -0.770508f, -0.790323f, -0.770508f,
	-0.712056f, -0.617899f, -0.492758f, -0.342908f, -0.175863f,
	0f, 0.160964f, 0.314893f, 0.45506f, 0.575338f,
	0.670471f, 0.736302f, 0.769952f, 0.769952f, 0.736302f,
	0.670471f, 0.575338f, 0.45506f, 0.314893f, 0.160964f,
	9.48082e-17f, -0.160964f, -0.314893f, -0.45506f, -0.575338f,
	-0.670471f, -0.736302f, -0.769952f, -0.769952f, -0.736302f,
	-0.670471f, -0.575338f, -0.45506f, -0.314893f, -0.160964f,
	0f, 0.147891f, 0.290099f, 0.421158f, 0.536033f,
	0.630308f, 0.70036f, 0.743499f, 0.758065f, 0.743499f,
	0.70036f, 0.630308f, 0.536033f, 0.421158f, 0.290099f,
	0.147891f, 9.28331e-17f, -0.147891f, -0.290099f, -0.421158f,
	-0.536033f, -0.630308f, -0.70036f, -0.743499f, -0.758065f,
	-0.743499f, -0.70036f, -0.630308f, -0.536033f, -0.421158f,
	-0.290099f, -0.147891f, 0f, 0.13633f, 0.268018f,
	0.390579f, 0.499839f, 0.592077f, 0.664153f, 0.713613f,
	0.738771f, 0.738771f, 0.713613f, 0.664153f, 0.592077f,
	0.499839f, 0.390579f, 0.268018f, 0.13633f, 9.08579e-17f,
	-0.13633f, -0.268018f, -0.390579f, -0.499839f, -0.592077f,
	-0.664153f, -0.713613f, -0.738771f, -0.738771f, -0.713613f,
	-0.664153f, -0.592077f, -0.499839f, -0.390579f, -0.268018f,
	-0.13633f, 0f, 0.126035f, 0.24824f, 0.362903f,
	0.466539f, 0.556f, 0.628567f, 0.682035f, 0.71478f,
	0.725806f, 0.71478f, 0.682035f, 0.628567f, 0.556f,
	0.466539f, 0.362903f, 0.24824f, 0.126035f, 8.88827e-17f,
	-0.126035f, -0.24824f, -0.362903f, -0.466539f, -0.556f,
	-0.628567f, -0.682035f, -0.71478f, -0.725806f, -0.71478f,
	-0.682035f, -0.628567f, -0.556f, -0.466539f, -0.362903f,
	-0.24824f, -0.126035f, 0f, 0.116809f, 0.230432f,
	0.337769f, 0.435893f, 0.522127f, 0.594118f, 0.649904f,
	0.687961f, 0.707254f, 0.707254f, 0.687961f, 0.649904f,
	0.594118f, 0.522127f, 0.435893f, 0.337769f, 0.230432f,
	0.116809f, 8.69075e-17f, -0.116809f, -0.230432f, -0.337769f,
	-0.435893f, -0.522127f, -0.594118f, -0.649904f, -0.687961f,
	-0.707254f, -0.707254f, -0.687961f, -0.649904f, -0.594118f,
	-0.522127f, -0.435893f, -0.337769f, -0.230432f, -0.116809f,
	0f, 0.108495f, 0.214318f, 0.314864f, 0.407658f,
	0.490413f, 0.561092f, 0.617956f, 0.659604f, 0.68501f,
	0.693548f, 0.68501f, 0.659604f, 0.617956f, 0.561092f,
	0.490413f, 0.407658f, 0.314864f, 0.214318f, 0.108495f,
	8.49324e-17f, -0.108495f, -0.214318f, -0.314864f, -0.407658f,
	-0.490413f, -0.561092f, -0.617956f, -0.659604f, -0.68501f,
	-0.693548f, -0.68501f, -0.659604f, -0.617956f, -0.561092f,
	-0.490413f, -0.407658f, -0.314864f, -0.214318f, -0.108495f,
	0f, 0.100964f, 0.199673f, 0.293921f, 0.381604f,
	0.460762f, 0.529628f, 0.586662f, 0.630592f, 0.660435f,
	0.675525f, 0.675525f, 0.660435f, 0.630592f, 0.586662f,
	0.529628f, 0.460762f, 0.381604f, 0.293921f, 0.199673f,
	0.100964f, 8.29572e-17f, -0.100964f, -0.199673f, -0.293921f,
	-0.381604f, -0.460762f, -0.529628f, -0.586662f, -0.630592f,
	-0.660435f, -0.675525f, -0.675525f, -0.660435f, -0.630592f,
	-0.586662f, -0.529628f, -0.460762f, -0.381604f, -0.293921f,
	-0.199673f, -0.100964f, 0f, 0.0941114f, 0.186307f,
	0.27471f, 0.357521f, 0.433053f, 0.49977f, 0.556313f,
	0.601531f, 0.634503f, 0.654559f, 0.66129f, 0.654559f,
	0.634503f, 0.601531f, 0.556313f, 0.49977f, 0.433053f,
	0.357521f, 0.27471f, 0.186307f, 0.0941114f, 8.0982e-17f,
	-0.0941114f, -0.186307f, -0.27471f, -0.357521f, -0.433053f,
	-0.49977f, -0.556313f, -0.601531f, -0.634503f, -0.654559f,
	-0.66129f, -0.654559f, -0.634503f, -0.601531f, -0.556313f,
	-0.49977f, -0.433053f, -0.357521f, -0.27471f, -0.186307f,
	-0.0941114f, 0f, 0.0878495f, 0.174062f, 0.257033f,
	0.335215f, 0.407154f, 0.471507f, 0.527077f, 0.572829f,
	0.60791f, 0.631667f, 0.643657f, 0.643657f, 0.631667f,
	0.60791f, 0.572829f, 0.527077f, 0.471507f, 0.407154f,
	0.335215f, 0.257033f, 0.174062f, 0.0878495f, 7.90069e-17f,
	-0.0878495f, -0.174062f, -0.257033f, -0.335215f, -0.407154f,
	-0.471507f, -0.527077f, -0.572829f, -0.60791f, -0.631667f,
	-0.643657f, -0.643657f, -0.631667f, -0.60791f, -0.572829f,
	-0.527077f, -0.471507f, -0.407154f, -0.335215f, -0.257033f,
	-0.174062f, -0.0878495f, 0f, 0.0821052f, 0.162806f,
	0.24072f, 0.314516f, 0.382931f, 0.444793f, 0.499045f,
	0.544758f, 0.58115f, 0.607599f, 0.623651f, 0.629032f,
	0.623651f, 0.607599f, 0.58115f, 0.544758f, 0.499045f,
	0.444793f, 0.382931f, 0.314516f, 0.24072f, 0.162806f,
	0.0821052f, 7.70317e-17f, -0.0821052f, -0.162806f, -0.24072f,
	-0.314516f, -0.382931f, -0.444793f, -0.499045f, -0.544758f,
	-0.58115f, -0.607599f, -0.623651f, -0.629032f, -0.623651f,
	-0.607599f, -0.58115f, -0.544758f, -0.499045f, -0.444793f,
	-0.382931f, -0.314516f, -0.24072f, -0.162806f, -0.0821052f,
	0f, 0.0768171f, 0.152423f, 0.225625f, 0.295268f,
	0.360255f, 0.419561f, 0.47225f, 0.517491f, 0.554571f,
	0.582906f, 0.602047f, 0.611694f, 0.611694f, 0.602047f,
	0.582906f, 0.554571f, 0.517491f, 0.47225f, 0.419561f,
	0.360255f, 0.295268f, 0.225625f, 0.152423f, 0.0768171f,
	7.50565e-17f, -0.0768171f, -0.152423f, -0.225625f, -0.295268f,
	-0.360255f, -0.419561f, -0.47225f, -0.517491f, -0.554571f,
	-0.582906f, -0.602047f, -0.611694f, -0.611694f, -0.602047f,
	-0.582906f, -0.554571f, -0.517491f, -0.47225f, -0.419561f,
	-0.360255f, -0.295268f, -0.225625f, -0.152423f, -0.0768171f,
	0f, 0.0719332f, 0.142817f, 0.211619f, 0.277335f,
	0.339006f, 0.395734f, 0.446692f, 0.491136f, 0.528417f,
	0.557994f, 0.579433f, 0.592423f, 0.596774f, 0.592423f,
	0.579433f, 0.557994f, 0.528417f, 0.491136f, 0.446692f,
	0.395734f, 0.339006f, 0.277335f, 0.211619f, 0.142817f,
	0.0719332f, 7.30813e-17f, -0.0719332f, -0.142817f, -0.211619f,
	-0.277335f, -0.339006f, -0.395734f, -0.446692f, -0.491136f,
	-0.528417f, -0.557994f, -0.579433f, -0.592423f, -0.596774f,
	-0.592423f, -0.579433f, -0.557994f, -0.528417f, -0.491136f,
	-0.446692f, -0.395734f, -0.339006f, -0.277335f, -0.211619f,
	-0.142817f, -0.0719332f, 0f, 0.0674088f, 0.133906f,
	0.198592f, 0.260593f, 0.31907f, 0.373232f, 0.422346f,
	0.465749f, 0.502853f, 0.533158f, 0.556252f, 0.571824f,
	0.579663f, 0.579663f, 0.571824f, 0.556252f, 0.533158f,
	0.502853f, 0.465749f, 0.422346f, 0.373232f, 0.31907f,
	0.260593f, 0.198592f, 0.133906f, 0.0674088f, 7.11062e-17f,
	-0.0674088f, -0.133906f, -0.198592f, -0.260593f, -0.31907f,
	-0.373232f, -0.422346f, -0.465749f, -0.502853f, -0.533158f,
	-0.556252f, -0.571824f, -0.579663f, -0.579663f, -0.571824f,
	-0.556252f, -0.533158f, -0.502853f, -0.465749f, -0.422346f,
	-0.373232f, -0.31907f, -0.260593f, -0.198592f, -0.133906f,
	-0.0674088f, 0f, 0.0632058f, 0.125617f, 0.186448f,
	0.244934f, 0.300341f, 0.35197f, 0.399173f, 0.441356f,
	0.477989f, 0.508611f, 0.532837f, 0.550363f, 0.560967f,
	0.564516f, 0.560967f, 0.550363f, 0.532837f, 0.508611f,
	0.477989f, 0.441356f, 0.399173f, 0.35197f, 0.300341f,
	0.244934f, 0.186448f, 0.125617f, 0.0632058f, 6.9131e-17f,
	-0.0632058f, -0.125617f, -0.186448f, -0.244934f, -0.300341f,
	-0.35197f, -0.399173f, -0.441356f, -0.477989f, -0.508611f,
	-0.532837f, -0.550363f, -0.560967f, -0.564516f, -0.560967f,
	-0.550363f, -0.532837f, -0.508611f, -0.477989f, -0.441356f,
	-0.399173f, -0.35197f, -0.300341f, -0.244934f, -0.186448f,
	-0.125617f, -0.0632058f, 0f, 0.0592911f, 0.117887f,
	0.175101f, 0.230262f, 0.282723f, 0.33187f, 0.377126f,
	0.41796f, 0.453894f, 0.484507f, 0.509439f, 0.528398f,
	0.541163f, 0.547583f, 0.547583f, 0.541163f, 0.528398f,
	0.509439f, 0.484507f, 0.453894f, 0.41796f, 0.377126f,
	0.33187f, 0.282723f, 0.230262f, 0.175101f, 0.117887f,
	0.0592911f, 6.71558e-17f, -0.0592911f, -0.117887f, -0.175101f,
	-0.230262f, -0.282723f, -0.33187f, -0.377126f, -0.41796f,
	-0.453894f, -0.484507f, -0.509439f, -0.528398f, -0.541163f,
	-0.547583f, -0.547583f, -0.541163f, -0.528398f, -0.509439f,
	-0.484507f, -0.453894f, -0.41796f, -0.377126f, -0.33187f,
	-0.282723f, -0.230262f, -0.175101f, -0.117887f, -0.0592911f,
	0f, 0.0556361f, 0.110663f, 0.164477f, 0.216489f,
	0.266129f, 0.312853f, 0.35615f, 0.395545f, 0.430606f,
	0.460949f, 0.486242f, 0.506208f, 0.520627f, 0.529342f,
	0.532258f, 0.529342f, 0.520627f, 0.506208f, 0.486242f,
	0.460949f, 0.430606f, 0.395545f, 0.35615f, 0.312853f,
	0.266129f, 0.216489f, 0.164477f, 0.110663f, 0.0556361f,
	6.51807e-17f, -0.0556361f, -0.110663f, -0.164477f, -0.216489f,
	-0.266129f, -0.312853f, -0.35615f, -0.395545f, -0.430606f,
	-0.460949f, -0.486242f, -0.506208f, -0.520627f, -0.529342f,
	-0.532258f, -0.529342f, -0.520627f, -0.506208f, -0.486242f,
	-0.460949f, -0.430606f, -0.395545f, -0.35615f, -0.312853f,
	-0.266129f, -0.216489f, -0.164477f, -0.110663f, -0.0556361f,
	0f, 0.0522159f, 0.103896f, 0.15451f, 0.203539f,
	0.250478f, 0.294848f, 0.336192f, 0.374087f, 0.408142f,
	0.43801f, 0.463383f, 0.484001f, 0.499653f, 0.510177f,
	0.515467f, 0.515467f, 0.510177f, 0.499653f, 0.484001f,
	0.463383f, 0.43801f, 0.408142f, 0.374087f, 0.336192f,
	0.294848f, 0.250478f, 0.203539f, 0.15451f, 0.103896f,
	0.0522159f, 6.32055e-17f, -0.0522159f, -0.103896f, -0.15451f,
	-0.203539f, -0.250478f, -0.294848f, -0.336192f, -0.374087f,
	-0.408142f, -0.43801f, -0.463383f, -0.484001f, -0.499653f,
	-0.510177f, -0.515467f, -0.515467f, -0.510177f, -0.499653f,
	-0.484001f, -0.463383f, -0.43801f, -0.408142f, -0.374087f,
	-0.336192f, -0.294848f, -0.250478f, -0.203539f, -0.15451f,
	-0.103896f, -0.0522159f, 0f, 0f, 0f,
	0f, 0f, 0f, 0f, 0f
    };

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #SimpleSource(com.rbnb.api.Server,String)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public SimpleSource() {
	super();
    }

    /**
     * Class constructor to build a <code>SimpleSource</code> attached to the
     * specified <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI    the name of the <code>Source</code>.
     * @param serverI  the <code>Server</code>.
     * @see #SimpleSource()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public SimpleSource(String nameI,Server serverI) {
	this();
	setSourceName(nameI);
	setServer(serverI);
    }

    /**
     * Creates the first frame of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the frame <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the input <code>Rmap</code> is already a child of
     *		  another <code>Rmap</code> or if the input is null.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    private final synchronized Rmap createFirstFrame()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {

	// Create the frame <code>Rmap</code> with a <code>TimeRange</code>
	// that will span the channel <code>TimeRanges</code>.
	Rmap frameR = new Rmap(null,null,new TimeRange(0.,1.));
	//	Rmap frameR = new Rmap("baseFrame",null,new TimeRange(0.,1.));

	// Create the data arrays for the channels.
	createDataArrays();

	Rmap[] extraLevels = null;
	if (extraLevel) {
	    extraLevels = new Rmap[2];
	    extraLevels[0] = new Rmap("Even");
	    frameR.addChild(extraLevels[0]);
	    if (getNchans() >= 2) {
		extraLevels[1] = new Rmap("Odd");
		frameR.addChild(extraLevels[1]);
	    }
	}

	for (int cIdx = 0; cIdx < getNchans(); ++cIdx) {
	    // Loop through the channels and create the channel
	    // <code>Rmaps</code>.

	    // First, create the <code>DataBlock</code> for the channel from
	    // the appropriate entry in the data array.
	    DataBlock dBlock = new DataBlock
		(data[cIdx][0],
		 data[cIdx][0].length,
		 4,
		 DataBlock.TYPE_FLOAT32,
		 DataBlock.ORDER_MSB,
		 false,
		 0,
		 4);

	    // Second, create the <code>TimeRange</code>. For this, we do two
	    // things in order to push the capabilities of the
	    // <code>Rmap</code> system:
	    // <p><ol>
	    // <li>The channels have a time offset relative to each other,</li>
	    // <li>Half the channels use a single start and duration, and</li>
	    // <li>The other half have individual point times.</li>
	    TimeRange tRange;
	    double interval = 1./getNpoints();

	    if ((cIdx % 2) == 0) {
		// The even channels use a single start and duration.
		tRange = new TimeRange((offsetChannels ?
					((double) cIdx)/getNchans()*interval :
					0.),
				       1.);
	    } else {
		// The odd channels use separate start times for each point,
		// with a zero duration.
		double[] times = new double[getNpoints()];

		times[0] = (offsetChannels ?
			    ((double) cIdx)/getNchans()*interval :
			    0.);
		for (int pIdx = 1; pIdx < getNpoints(); ++pIdx) {
		    times[pIdx] = times[pIdx - 1] + interval;
		}

		tRange = new TimeRange(times,0.);
	    }

	    // Third, create the channel <code>Rmap</code> from the
	    // <code>DataBlock</code> and <code>TimeRange</code>.
	    Rmap channel = new Rmap("C" + cIdx,dBlock,tRange);

	    // Fourth, add the channel to the frame.
	    if (extraLevel) {
		extraLevels[cIdx % 2].addChild(channel);
	    } else {
		frameR.addChild(channel);
	    }
	}

	return (frameR);
    }

    /**
     * Creates the data arrays.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    private final synchronized void createDataArrays() {
	// Create the data arrays for each of the frames (up to the first N,
	// where N depends on the number of points per frame).
	data = new float[getNchans()][][];

	for (int cIdx = 0; cIdx < getNchans(); ++cIdx) {
	    // Loop through the channels and create the frame data arrays.
	    int nFrames;

	    if (getNpoints() < PREGEN.length) {
		nFrames = PREGEN.length/getNpoints();
	    } else {
		nFrames = getNpoints()/PREGEN.length;
	    }

	    data[cIdx] = new float[nFrames][];

	    for (int fIdx = 0, sPoint = 0;
		 fIdx < nFrames;
		 ++fIdx,
		     sPoint = ((sPoint + getNpoints()) % PREGEN.length)) {

		data[cIdx][fIdx] = new float[getNpoints()];

		for (int curP = sPoint,
			 ePoint = sPoint + getNpoints(),
			 nPts = getNpoints();
		     curP < ePoint;
		     ) {
		    int strP = (curP + cIdx) % PREGEN.length,
			remP = PREGEN.length - strP,
			lstP = strP + nPts,
			numP = nPts;

		    if (lstP >= PREGEN.length) {
			numP = remP;
			lstP = strP + numP;
		    }

		    System.arraycopy(PREGEN,
				     strP,
				     data[cIdx][fIdx],
				     curP % getNpoints(),
				     numP);

		    curP += numP;
		    nPts -= numP;
		}
	    }
	}
    }

    /**
     * Disconnect from the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not running.
     * @exception java.lang.InterruptedException
     *		  thrown if the terminate is interrupted.
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void disconnect()
	throws java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException,
	       com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException
    {
	if ((getSource() != null) && getDisconnect()) {
	    if (getSource().isRunning()) {
		getSource().stop();
		setSource(null);
	    }
	}
    }

    /**
     * Gets the nominal size of the archive in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of archive frames.
     * @see #setAframes(long)
     * @since V2.0
     * @version 02/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public final synchronized long getAframes() {
	return (archiveFrames);
    }

    /**
     * Gets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the archive access mode.
     * @see #setAmode(byte)
     * @since V2.0
     * @version 02/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2001  INB	Created.
     *
     */
    public final synchronized byte getAmode() {
	return (aMode);
    }

    /**
     * Gets the nominal size of the cache in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of cache frames.
     * @see #setCframes(long)
     * @since V2.0
     * @version 02/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public final synchronized long getCframes() {
	return (cacheFrames);
    }

    /**
     * Gets the nominal size of the cache in <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of cache <code>FrameSets</code>.
     * @see #setCfs(int)
     * @since V2.0
     * @version 02/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public final synchronized int getCfs() {
	return (cacheFrameSets);
    }

    /**
     * Gets the disconnect flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return disconnect after run?
     * @see #setDisconnect(boolean)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized boolean getDisconnect() {
	return (disconnectAfterRun);
    }

    /**
     * Gets the more frames flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are there more frames to send?
     * @see #setMframes(boolean)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized boolean getMframes() {
	return (moreFrames);
    }

    /**
     * Gets the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of channels.
     * @see #setNchans(int)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized int getNchans() {
	return (numChans);
    }

    /**
     * Gets the number of frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of frames.
     * @see #setNframes(long)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized long getNframes() {
	return (numFrames);
    }

    /**
     * Gets the number of points per frame per channel.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of points.
     * @see #setNpoints(int)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized int getNpoints() {
	return (numPoints);
    }

    /**
     * Gets the running flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we running?
     * @see #setRunning(boolean)
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    private final synchronized boolean getRunning() {
	return (running);
    }

    /**
     * Gets the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Server</code>.
     * @see #setServer(com.rbnb.api.Server)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public final synchronized Server getServer() {
	return (server);
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
     * @version 01/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public final synchronized Source getSource() {
	return (source);
    }

    /**
     * Gets the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the name.
     * @see #setName(String)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public final synchronized String getSourceName() {
	return (name);
    }

    /**
     * Gets the sleep time between frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time between frames in milliseconds.
     * @see #setStime(int)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized int getStime() {
	return (sleepTime);
    }

    /**
     * Gets the timing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is this run being timed?
     * @see #setTiming(boolean)
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized boolean getTiming() {
	return (timing);
    }

    /**
     * Is this <code>SimpleSource</code> running?
     * <p>
     *
     * @author Ian Brown
     *
     * @return are we running?
     * @exception java.lang.InterruptedException
     *		  thrown if the check is interrupted.
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    public final synchronized boolean isRunning()
	throws java.lang.InterruptedException
    {
	while (isAlive() && !getRunning()) {
	    wait(1000);
	}

	return (isAlive() && getRunning());
    }

    /**
     * Runs the source.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/12/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public final void run() {
	boolean error = false;

	try {
	    Date last = new Date(),
		 now;

	    setSource(getServer().createSource(getSourceName()));
	    //	    setSource(getServer().createRAMSource(getSourceName()));
	    getSource().setCframes((getCframes() == -1) ?
				   Math.min(getNframes(),10000) :
				   getCframes());
	    getSource().setNfs(getCfs());
	    getSource().setAmode(getAmode());
	    getSource().setAframes(getAframes());

	    if (getTiming()) {
		now = new Date();
		System.err.println(getSourceName() +
				   " created in " +
				   (now.getTime() - last.getTime())/1000.);
		last = now;
	    }

	    // Start the <code>Source</code>.
	    getSource().start();

	    if ((getAmode() == Source.ACCESS_APPEND) ||
		(getAmode() == Source.ACCESS_LOAD)) {
		// If an archive is being loaded, wait for the load to
		// complete.
		getSource().synchronizeWserver();
	    }

	    // We're now running.
	    setRunning(true);

	    if (getTiming()) {
		now = new Date();
		System.err.println(getSourceName() +
				   " started in " +
				   (now.getTime() - last.getTime())/1000.);
		last = now;
	    }

	    if (getAmode() != Source.ACCESS_LOAD) {
		// If we are not loading the archive, then build the data map.

		// Create the first frame of data.
		Rmap frame = createFirstFrame();
		if (getTiming()) {
		    now = new Date();
		    System.err.println(getSourceName() +
				       " created first frame of " +
				       getNchans() + " channels of " +
				       getNpoints() + " points in " +
				       (now.getTime() - last.getTime())/1000.);
		    last = now;
		}

		for (long idx = 0; getMframes(); ++idx) {
		    // Loop through the frames. Update the frame for the
		    // current time step.
		    updateFrame(frame,idx);

		    // Add the frame to the source.
		    getSource().addChild(frame);

		    // Wait a while before sending another frame.
		    if (!getTiming()) {
			sleep(getStime());
		    }

		    if (idx == getNframes() - 1) {
			setMframes(false);
			if (!getTiming()) {
			    Thread.currentThread().sleep(5000);
			}
		    }
		}
	    }

	    getSource().synchronizeWserver();
	    if (getTiming()) {
		now = new Date();
		if (getAmode() != Source.ACCESS_LOAD) {
		    System.err.println(getSourceName() +
				       " sent " +
				       getNframes() + " frames of " +
				       getNchans() + " channels of " +
				       getNpoints() + " points in " +
				       (now.getTime() - last.getTime())/1000. +
				       " seconds.");
		} else {
		    System.err.println(getSourceName() + " loaded archive.");
		}
		last = now;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    error = true;
	}

	try {
	    if (error || getDisconnect()) {
		// Disconnect if there was an error or if we're supposed to.
		disconnect();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// We're no longer running.
	setRunning(false);
    }

    /**
     * Sets the nominal size of the archive in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param framesI  the number of archive frames.
     * @see #getAframes()
     * @since V2.0
     * @version 02/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public final synchronized void setAframes(long framesI) {
	archiveFrames = framesI;
    }

    /**
     * Sets the archive access mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @param aModeI  the archive access mode.
     * @see #getAmode()
     * @since V2.0
     * @version 02/27/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/27/2001  INB	Created.
     *
     */
    public final synchronized void setAmode(byte aModeI) {
	aMode = aModeI;
    }

    /**
     * Sets the nominal size of the cache in frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param framesI  the number of cache frames.
     * @see #getCframes()
     * @since V2.0
     * @version 02/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public final synchronized void setCframes(long framesI) {
	cacheFrames = framesI;
    }

    /**
     * Sets the nominal size of the cache in <code>FrameSets</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param setsI  the number of cache <code>FrameSets</code>.
     * @see #getCfs(int)
     * @since V2.0
     * @version 02/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/23/2001  INB	Created.
     *
     */
    public final synchronized void setCfs(int setsI) {
	cacheFrameSets = setsI;
    }

    /**
     * Sets the disconnect flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param disconnectAfterRunI  disconnect after running?
     * @see #getDisconnect()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setDisconnect(boolean disconnectAfterRunI) {
	disconnectAfterRun = disconnectAfterRunI;
    }

    /**
     * Sets the more frames flags.
     * <p>
     *
     * @author Ian Brown
     *
     * @param moreFramesI  are there more frames to send?
     * @see #getMframes()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setMframes(boolean moreFramesI) {
	moreFrames = moreFramesI;
    }

    /**
     * Sets the number of channels.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numChansI  the number of channels.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of channels <= 0.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getNchans()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setNchans(int numChansI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the number of channels once the source is " +
		 "running.");
	} else if (numChansI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The number of channels must be positive.");
	}

	numChans = numChansI;
    }

    /**
     * Sets the number of frames.
     * <p>
     * A value of -1 means that the number of frames is not limited.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numFramesI  the number of frames.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of frames <= 0 and is not -1.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getNframes()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setNframes(long numFramesI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the number of frames once the source is " +
		 "running.");
	} else if ((!getTiming() && (numFramesI != -1)) &&
		   (numFramesI <= 0)) {
	    throw new java.lang.IllegalArgumentException
		("The number of frames must be positive.");
	}

	numFrames = numFramesI;
    }

    /**
     * Sets the number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numPointsI  the number of points.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the number of points <= 0.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getNpoints()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setNpoints(int numPointsI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the number of points once the source is " +
		 "running.");
	} else if (numPointsI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The number of points must be positive.");
	}

	numPoints = numPointsI;
    }

    /**
     * Sets the running flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param runningI  are we running?
     * @see #getRunning()
     * @see #isRunning()
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/23/2001  INB	Created.
     *
     */
    private final synchronized void setRunning(boolean runningI) {
	running = runningI;
	notifyAll();
    }

    /**
     * Sets the <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param serverI  the <code>Server</code>.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getServer()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    private final synchronized void setServer(Server serverI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the server once the source is " +
		 "running.");
	}

	server = serverI;
    }

    /**
     * Sets the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sourceI  the <code>Source</code>.
     * @see #getSource()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    protected final synchronized void setSource(Source sourceI) {
	source = sourceI;
    }

    /**
     * Sets the name.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nameI  the name.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getSourceName()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/03/2001  INB	Created.
     *
     */
    public final synchronized void setSourceName(String nameI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the source name once the source is " +
		 "running.");
	}

	name = nameI;
    }

    /**
     * Sets the sleep time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sleepTimeI  the sleep time in milliseconds.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if the sleep time <= 0.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getStime()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setStime(int sleepTimeI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the sleep time once the source is " +
		 "running.");
	} else if (sleepTimeI <= 0) {
	    throw new java.lang.IllegalArgumentException
		("The sleep time must be positive.");
	}

	sleepTime = sleepTimeI;
    }

    /**
     * Sets the timing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timingI  should the run be timed?
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>SimpleSource</code> is running.
     * @see #getTiming()
     * @since V2.0
     * @version 01/17/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/17/2001  INB	Created.
     *
     */
    public final synchronized void setTiming(boolean timingI) {
	if (isAlive()) {
	    throw new java.lang.IllegalStateException
		("Cannot change the timing flag once the source is " +
		 "running.");
	}

	timing = timingI;
	if (getNframes() == -1) {
	    setNframes(1000);
	}
    }

    /**
     * Updates the frame for the next time step.
     * <p>
     *
     * @author Ian Brown
     *
     * @param frameIO  the frame <code>Rmap</code>.
     * @param tStepI   the time step.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IndexOutOfBoundsException
     *		  thrown if there are no children or the index is not in the
     *		  range 0 to # of children - 1.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 04/03/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/04/2001  INB	Created.
     *
     */
    private final synchronized void updateFrame(Rmap frameIO,long tStepI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if (tStepI > 0) {
	    // If this is not the first time step, we update the frame by
	    // increasing the time value for the frame as a whole by one and
	    // shift the data to the appropriate entry in the data array using
	    // (tStepI % [num frames of pregenerated data]).

	    // First, update the frame <code>TimeRange</code>.
	    frameIO.getTrange().set((double) tStepI,1.);

	    for (int cIdx = 0; cIdx < getNchans(); ++cIdx) {
		// Loop through the channels and update the
		// <code>DataBlocks</code>.
		int tIdx = (int) tStepI % data[cIdx].length;

		if (extraLevel) {
		    frameIO.getChildAt(cIdx % 2).getChildAt
			(cIdx/2).getDblock().setData
			(data[cIdx][tIdx]);
		} else {
		    frameIO.getChildAt(cIdx).getDblock().setData
			(data[cIdx][tIdx]);
		}
	    }
	}
    }

    /**
     * Main method for running a <code>SimpleSource</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param argsI  the command line arguments:
     *		     <p><ul>
     *		     <li>-a serverAddress</li>
     *		     <li>-A archiveSizeInFrames</li>
     *		     <li>-c numberOfChannels</li>
     *		     <li>-C cacheSizeInFrames</li>
     *		     <li>-f numberOfFrames</li>
     *		     <li>-F cacheSizeInFrameSets</li>
     *		     <li>-m archiveMode</li>
     *		     <li>-n serverName</li>
     *		     <li>-p numberOfPoints</li>
     *		     <li>-t runInTimingMode</li>
     *		     </ul>
     * @since V2.0
     * @version 04/13/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/05/2001  INB	Created.
     *
     */
    public final static void main(String[] argsI) {
	Server server = null;
	byte aMode = Source.ACCESS_NONE;
	boolean started = false,
	        timing = false;
	int sourcesSinks = 1,
	    numChans = 1,
	    numPoints = 10,
	    sleepTime = 10;
	long numFrames = -1,
	     cacheFrames = -1,
	     archiveFrames = 0;
	int cacheFS = 10;
	String serverName = "Server",
	       serverAddress = "internal://";
	SimpleSource source = null;

	try {
	    for (int idx = 0; idx < argsI.length;) {
		if (argsI[idx].equals("-n")) {
		    serverName = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-a")) {
		    serverAddress = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-A")) {
		    archiveFrames = Long.parseLong(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-c")) {
		    numChans = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-C")) {
		    cacheFrames = Long.parseLong(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-f")) {
		    numFrames = Long.parseLong(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-F")) {
		    cacheFS = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-m")) {
		    aMode = (byte) Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-p")) {
		    numPoints = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-t")) {
		    timing = true;
		    ++idx;
		}
	    }

	    server = com.rbnb.api.Server.newServerHandle
		(serverName,
		 serverAddress);

	    if (!server.isRunning()) {
		throw new com.rbnb.api.AddressException("Unable to connect to " + server);
	    }

	    source = new SimpleSource("TestSource",server);
	    source.setAframes(archiveFrames);
	    source.setAmode(aMode);
	    source.setCframes(cacheFrames);
	    source.setCfs(cacheFS);
	    source.setNchans(numChans);
	    source.setNpoints(numPoints);
	    source.setNframes(numFrames);
	    source.setStime(sleepTime);
	    source.setTiming(timing);
	    source.start();
	    if (aMode == Source.ACCESS_LOAD) {
		source.setDisconnect(false);
	    }
	    Thread.currentThread().yield();
	    source.isRunning();
	    Thread.currentThread().yield();

	    boolean done;
	    do {
		done = true;

		done = !source.getMframes();
		Thread.currentThread().sleep(10);
	    } while (!done);

	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    if (started) {
		if ((server != null) && server.isRunning()) {
		    server.stop();
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
