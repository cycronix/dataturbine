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
 * Determine if a feature is supported.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 07/30/2004
 */

/*
 * Copyright 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/30/2004  INB	FEATURE_ASK_NO_JAVA_SERIALIZE is not supported prior to
 *			V2.4.3.
 * 01/08/2004  INB	FEATURE_CLEAR_CACHE is not supported prior to V2.2.
 * 12/08/2003  INB	FEATURE_OPTION_EXTEND_START is not supported prior to
 *			V2.2.
 * 11/06/2003  INB	FEATURE_REQUEST_TIME_RELATIVE is not supported prior
 *			to V2.2.
 * 09/26/2003  INB	Ensure that the <code>Feature_Dates</code> variable
 *			has been fully created before checking for support.
 * 07/30/2003  INB	FEATURE_DELETE_CHANNELS is not supported prior to V2.2.
 * 06/11/2003  INB	FEATURE_REQUEST_OPTIONS is not supported priot to V2.2.
 * 03/04/2003  INB	FEATURE_PINGS_WITH_DATA is not supported prior to V2.1.
 * 02/25/2003  INB	Added FEATURE_PINGS_WITH_DATA.
 * 11/20/2002  INB	Created.
 *
 */
final class IsSupported {

    /**
     * use internal (rather than Java) serialization of regular Java objects
     * in <code>Ask</code> messages?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.4.3
     * @version 07/30/2004
     */
    public final static int FEATURE_ASK_NO_JAVA_SERIALIZE = 10;

    /**
     * are <code>ClearCache</code> commands supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 01/08/2004
     */
    public final static int FEATURE_CLEAR_CACHE = 9;

    /**
     * are <code>DeleteChannels</code> supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 07/30/2003
     */
    public final static int FEATURE_DELETE_CHANNELS = 6;

    /**
     * are <code>RequestOptions.extendStart</code> settings supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 12/08/2003
     */
    public final static int FEATURE_OPTION_EXTEND_START = 8;

    /**
     * are <code>Pings</code> with data supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 02/25/2003
     */
    public final static int FEATURE_PINGS_WITH_DATA = 4;

    /**
     * aligned requests allowed?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/13/2003
     */
    public final static int FEATURE_REQUEST_ALIGNED = 2;

    /**
     * leaf nodes in requests are marked?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/20/2002
     */
    public final static int FEATURE_REQUEST_LEAF_NODES = 1;

    /**
     * are <code>RequestOptions</code> supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 06/11/2003
     */
    public final static int FEATURE_REQUEST_OPTIONS = 5;

    /**
     * are time-relative requests supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/06/2003
     */
    public final static int FEATURE_REQUEST_TIME_RELATIVE = 7;

    /**
     * <code>TimeRanges</code> can be inclusive of end time?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/20/2002
     */
    public final static int FEATURE_TIME_RANGE_INCLUSIVE = 0;

    /**
     * <code>Usernames</code> supported?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/14/2003
     */
    public final static int FEATURE_USERNAMES = 3;

    /**
     * dates that the various features came into being.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 07/30/2004
     */
    private static java.util.Date[] Feature_Dates = new java.util.Date[11];

    /**
     * Determines if the specified feature is supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @param featureI	    the feature code.
     * @param buildVersionI the version to check.
     * @param buildDateI    the date to check.
     * @since V2.0
     * @version 07/30/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2004  INB	FEATURE_ASK_NO_JAVA_SERIALIZE is not supported prior to
     *			V2.4.3.
     * 01/08/2004  INB	FEATURE_CLEAR_CACHE is not supported prior to V2.2.
     * 12/08/2003  INB	FEATURE_OPTION_EXTEND_START is not supported prior to
     *			V2.2.
     * 11/06/2003  INB	FEATURE_REQUEST_TIME_RELATIVE is not supported prior to
     *			V2.2
     * 09/26/2003  INB	The <code>Feature_Dates</code> object is created at
     *			initialization of this class.  We can now synchronize
     *			on it at startup to ensure that all of the dates have
     *			been filled in before we check for support.
     * 07/30/2003  INB	FEATURE_DELETE_CHANNELS is not supported prior to V2.2.
     * 06/11/2003  INB	FEATURE_REQUEST_OPTIONS is not supported priot to V2.2.
     * 03/04/2003  INB	FEATURE_PINGS_WITH_DATA is not supported prior to V2.1.
     * 02/25/2003  INB	Added FEATURE_PINGS_WITH_DATA.
     * 11/20/2002  IMB	Created.
     *
     */
    final static boolean isSupported(int featureI,
				     String buildVersionI,
				     java.util.Date buildDateI)
    {
	boolean isSupported = true;

	synchronized (Feature_Dates) {
	    if (Feature_Dates[0] == null) {
		try {
		    Feature_Dates[FEATURE_ASK_NO_JAVA_SERIALIZE] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Jul 30 2004"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_CLEAR_CACHE] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Jan 08 2004"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_DELETE_CHANNELS] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Jul 30 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_OPTION_EXTEND_START] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Dec 08 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_PINGS_WITH_DATA] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Feb 25 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_REQUEST_ALIGNED] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Jan 13 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_REQUEST_LEAF_NODES] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Nov 20 2002"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_REQUEST_OPTIONS] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Jun 11 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_REQUEST_TIME_RELATIVE] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Nov 6 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_TIME_RANGE_INCLUSIVE] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Nov 06 2002"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
		try {
		    Feature_Dates[FEATURE_USERNAMES] =
			((new java.text.SimpleDateFormat
			    ("MMM dd yyyy",
			     java.util.Locale.US)).parse
			 ("Jan 14 2003"));
		} catch (java.text.ParseException e) {
		    throw new java.lang.Error();
		}
	    }
	}

	if ((buildVersionI == null) || (buildDateI == null)) {
	    isSupported = false;

	} else {
	    long builtAt = buildDateI.getTime(),
		featureAt = 0;
	    switch (featureI) {
		// Entries in this switch should be added at the top and should
		// fall through.

	    case FEATURE_ASK_NO_JAVA_SERIALIZE:
		if (buildVersionI.startsWith("V2.3") ||
		    buildVersionI.equals("V2.4") ||
		    buildVersionI.equals("V2.4.1") ||
		    buildVersionI.equals("V2.4.2")) {
		    isSupported = false;
		    break;
		}
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_ASK_NO_JAVA_SERIALIZE].getTime());

	    case FEATURE_CLEAR_CACHE:
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_CLEAR_CACHE].getTime());

	    case FEATURE_OPTION_EXTEND_START:
		if (buildVersionI.equals("V2.2B5")) {
		    isSupported = false;
		    break;
		}
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_OPTION_EXTEND_START].getTime());

	    case FEATURE_REQUEST_TIME_RELATIVE:
		if (buildVersionI.equals("V2.2B4") ||
		    buildVersionI.equals("V2.2B3") ||
		    buildVersionI.equals("V2.2B2") ||
		    buildVersionI.equals("V2.2B1")) {
		    isSupported = false;
		    break;
		}
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_REQUEST_TIME_RELATIVE].getTime());

	    case FEATURE_DELETE_CHANNELS:
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_DELETE_CHANNELS].getTime());

	    case FEATURE_REQUEST_OPTIONS:
		if (buildVersionI.substring(0,4).equals("V2.1")) {
		    isSupported = false;
		    break;
		}
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_REQUEST_OPTIONS].getTime());

	    case FEATURE_PINGS_WITH_DATA:
		if (buildVersionI.substring(0,2).equals("V1") ||
		    buildVersionI.substring(0,4).equals("V2.0")) {
		    isSupported = false;
		    break;
		}
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_PINGS_WITH_DATA].getTime());

	    case FEATURE_USERNAMES:
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_USERNAMES].getTime());
		
	    case FEATURE_REQUEST_ALIGNED:
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_REQUEST_ALIGNED].getTime());

	    case FEATURE_REQUEST_LEAF_NODES:
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_REQUEST_LEAF_NODES].getTime());

	    case FEATURE_TIME_RANGE_INCLUSIVE:
		featureAt = Math.max
		    (featureAt,
		     Feature_Dates[FEATURE_TIME_RANGE_INCLUSIVE].getTime());
		if ((builtAt < featureAt) ||
		    buildVersionI.equals("V2.0B1") ||
		    buildVersionI.equals("V2.0B2") ||
		    buildVersionI.equals("V2.0B3") ||
		    buildVersionI.equals("V2.0B4") ||
		    buildVersionI.equals("V2.0B5") ||
		    buildVersionI.equals("V2.0B6") ||
		    buildVersionI.equals("V2.0B7") ||
		    buildVersionI.equals("V2.0B8")) {
		    isSupported = false;
		}
		break;
	    }
	}

	return (isSupported);
    }
}
