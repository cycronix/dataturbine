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
 * Data information extracted from an <code>Rmap</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 2005/03/31
 */

/*
 * Copyright 2000, 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 2005/03/31  WHF  Added getDataType().
 * 11/12/2003  INB	Handle case where the number of points of data is not
 *			equal to the number of point times given a duration of
 *			zero in <code>toRmap</code>.
 * 11/30/2000  INB	Created.
 *
 */
public class DataArray
    implements java.lang.Cloneable,
	       java.io.Serializable
{
    /**
     * the data.
     * <p>
     * This field needs to be cast to the appropriate array type to be used.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private Object data = null;

    /**
     * the data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/17/2002
     */
    private byte dType = DataBlock.UNKNOWN;

    /**
     * the frame information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0;
     * @version 04/12/2002
     */
    java.util.Vector frameRanges = null;

    /**
     * the individual frame values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    private double[] frames = null;

    /**
     * the current number of points in the data array.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/12/2002
     */
    private int numberInArray = 0;

    /**
     * the total number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/12/2002
     */
    private int numberOfPoints = 0;

    /**
     * the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/24/2002
     */
    private String mimeType = null;

    /**
     * the number of points per <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/12/2002
     */
    java.util.Vector pointsPerRange = null;

    /**
     * the point size.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/17/2002
     */
    private int ptSize = 0;

    /**
     * the time information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0;
     * @version 04/12/2002
     */
    java.util.Vector timeRanges = null;

    /**
     * the individual times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    private double[] times = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public DataArray() {
	super();
    }

    /**
     * Adds data, along with optional associated time and frame information, to
     * this <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI the number of data points to add.
     * @param dBlockI  the optional <code>DataBlock</code> to add.
     * @param tRangeI  the optional <code>TimeRange</code>.
     * @param fRangeI  the optional <code>TimeRange</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if:
     *		  <p><ul>
     *		  <li>too many points are added,</li>
     *		  <li>if the optional fields provided are not the same fields
     *		      as in previous calls.</li>
     *		  </ul>
     * @see #getData()
     * @see #getFrame()
     * @see #getTime()
     * @see #setNumberOfPoints(int,int,byte)
     * @since V2.0
     * @version 09/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    final void add(int nPointsI,
		   DataBlock dBlockI,
		   TimeRange tRangeI,
		   TimeRange fRangeI)
    {
	if (numberInArray + nPointsI > getNumberOfPoints()) {
	    throw new java.lang.IllegalArgumentException
		("Cannot add " + nPointsI +
		 " additional points to data array.");

	} else if (tRangeI != null) {
	    if (numberInArray == 0) {
		timeRanges = new java.util.Vector();
		if (fRangeI != null) {
		    frameRanges = new java.util.Vector();
		}
		insertData(nPointsI,
			   dBlockI,
			   tRangeI,
			   fRangeI,
			   timeRanges,
			   frameRanges);
	    } else if (timeRanges == null) {
		addData(nPointsI,dBlockI);
	    } else {
		insertData(nPointsI,
			   dBlockI,
			   tRangeI,
			   fRangeI,
			   timeRanges,
			   frameRanges);
	    }

	} else if (fRangeI != null) {
	    if (numberInArray == 0) {
		frameRanges = new java.util.Vector();
		insertData(nPointsI,
			   dBlockI,
			   fRangeI,
			   null,
			   frameRanges,
			   timeRanges);
	    } else if (frameRanges == null) {
		addData(nPointsI,dBlockI);
	    } else {
		insertData(nPointsI,
			   dBlockI,
			   fRangeI,
			   null,
			   frameRanges,
			   timeRanges);
	    }

	} else if (dBlockI != null) {
	    addData(nPointsI,dBlockI);
	}
    }

    /**
     * Adds data without any time information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI the number of data points.
     * @param dBlockI  the <code>DataBlock</code>.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if previous calls to <code>add</code> had different
     *		  optional fields set.
     * @since V2.0
     * @version 11/26/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    private final void addData(int nPointsI,DataBlock dBlockI) {
	if ((frameRanges != null) || (timeRanges != null)) {
	    throw new java.lang.IllegalArgumentException
		("Cannot add data without time or frame information to a " +
		 "data array containing time or frame information.");
	}

	if ((dBlockI != null) || (data != null)) {
	    if (dBlockI.getDtype() == DataBlock.TYPE_BYTEARRAY) {
		byte[][] ba = (byte[][]) data;
		for (int idx = 0; idx < nPointsI; ++idx) {
		    ba[numberInArray + idx] = new byte[dBlockI.getPtsize()];
		}
	    }
	    dBlockI.getDataPoints(0,data,numberInArray,nPointsI);
	}

	numberInArray += nPointsI;
    }

    /**
     * Adds part of a <code>TimeRange</code> to a storage vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sIndexI	  the starting index to copy.
     * @param nPointsI	  the number of points to copy.
     * @param tPointsI	  the number of points in the <code>TimeRange</code>.
     * @param rangeI	  the <code>TimeRange</code>.
     * @param storeIndexI the starting index to copy into.
     * @param storeI	  the storage vector.
     * @since V2.0
     * @version 04/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    private final static void addRange(int sIndexI,
				       int nPointsI,
				       int tPointsI,
				       TimeRange rangeI,
				       int storeIndexI,
				       java.util.Vector storeI)
    {
	TimeRange range;

	if (nPointsI == tPointsI) {
	    range = rangeI;

	} else if (rangeI.getNptimes() == 1) {
	    range = new TimeRange(rangeI.getPointTime(sIndexI,tPointsI),
				(nPointsI*rangeI.getDuration()/
				 tPointsI));

	} else {
	    double[] times = new double[nPointsI];
	    System.arraycopy(rangeI.getPtimes(),
			     sIndexI,
			     times,
			     0,
			     nPointsI);
	    range = new TimeRange(times,rangeI.getDuration());
	}

	storeI.insertElementAt(range,storeIndexI);
    }

    /**
     * Clones this <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the clone.
     * @since V2.0
     * @version 09/18/2000
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    protected final Object clone() {
	try {
	    return (super.clone());
	} catch (CloneNotSupportedException e) {
	    return (null);
	}
    }

    /**
     * Extracts a <code>DataBlock</code> from this <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param startI   the starting point.
     * @param nPointsI the number of points.
     * @return the <code>DataBlock</code>.
     * @since V2.0
     * @version 10/29/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/17/2002  INB	Created.
     *
     */
    private final DataBlock extractDataBlock(int startI,int nPointsI) {
	DataBlock dataBlockR = null;

	if (nPointsI == numberOfPoints) {
	    if ((dType != DataBlock.TYPE_STRING) &&
		(dType != DataBlock.TYPE_BYTEARRAY)) {
		dataBlockR = new DataBlock(data,
					   nPointsI,
					   ptSize,
					   dType,
					   DataBlock.ORDER_MSB,
					   false,
					   0,
					   ptSize);
	    }

	} else {
	    Object nData = null;
	    switch (dType) {
	    case DataBlock.TYPE_BOOLEAN:
		boolean[] boolData = new boolean[nPointsI];
		System.arraycopy(getData(),startI,boolData,0,nPointsI);
		nData = boolData;
		break;

	    case DataBlock.TYPE_INT8:
		if (ptSize == 1) {
		    byte[] byteData = new byte[nPointsI];
		    System.arraycopy(getData(),startI,byteData,0,nPointsI);
		    nData = byteData;
		} else {
		    byte[][] baData = new byte[nPointsI][],
			obaData = (byte[][]) getData();
		    for (int idx = 0; idx < nPointsI; ++idx) {
			baData[idx] = new byte[ptSize];
			System.arraycopy(obaData[startI + idx],
					 0,
					 baData[idx],
					 0,
					 ptSize);
		    }
		    nData = baData;
		}
		break;

	    case DataBlock.TYPE_INT16:
		short[] shortData = new short[nPointsI];
		System.arraycopy(getData(),startI,shortData,0,nPointsI);
		nData = shortData;
		break;

	    case DataBlock.TYPE_INT32:
		int[] intData = new int[nPointsI];
		System.arraycopy(getData(),startI,intData,0,nPointsI);
		nData = intData;
		break;

	    case DataBlock.TYPE_INT64:
		long[] longData = new long[nPointsI];
		System.arraycopy(getData(),startI,longData,0,nPointsI);
		nData = longData;
		break;

	    case DataBlock.TYPE_FLOAT32:
		float[] floatData = new float[nPointsI];
		System.arraycopy(getData(),startI,floatData,0,nPointsI);
		nData = floatData;
		break;

	    case DataBlock.TYPE_FLOAT64:
		double[] doubleData = new double[nPointsI];
		System.arraycopy(getData(),startI,doubleData,0,nPointsI);
		nData = doubleData;
		break;
	    }

	    dataBlockR = new DataBlock(nData,
				       nPointsI,
				       ptSize,
				       dType,
				       DataBlock.ORDER_MSB,
				       false,
				       0,
				       ptSize);
	}

	dataBlockR.setMIMEType(getMIMEType());
	return (dataBlockR);
    }

    /**
     * Gets the duration of this <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the duration.
     * @see #getStartTime()
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  INB	Created.
     *
     */
    public final double getDuration() {
	double durationR = 0.;

	if (timeRanges != null) {
	    double startTime = getStartTime();
	    TimeRange lTrange = (TimeRange) timeRanges.lastElement();
	    double endTime = (lTrange.getPtimes()[lTrange.getNptimes() - 1] +
			      lTrange.getDuration());

	    durationR = endTime - startTime;
	}

	return (durationR);
    }

    /**
     * Finds out where to put data based on a reference time or frame.
     * <p>
     * If necessary this method will break up existing entries into two pieces
     * to make room for the new data.
     * <p>
     *
     * @author Ian Brown
     *
     * @param pIndexI	  the index of the point to locate.
     * @param nPointsI	  the number of points represented by the reference.
     * @param refI	  the reference range.
     * @param refStoreI	  the reference storage vector.
     * @param otherStoreI the other storage vector.
     * @return the index to store the new data at.
     * @since V2.0
     * @version 10/31/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    private final int findInsertionPoint(int pIndexI,
					 int nPointsI,
					 TimeRange refI,
					 java.util.Vector refStoreI,
					 java.util.Vector otherStoreI)
    {
	int low = 0,
	    high = refStoreI.size() - 1;
	double ptime = refI.getPointTime(pIndexI,nPointsI),
	       erange;

	for (int idx = (high + low)/2;
	     (high >= low);
	     idx = (high + low)/2) {
	    TimeRange tRange = (TimeRange) refStoreI.elementAt(idx);
	    boolean greater = (tRange.getDuration() == 0.);

	    if (ptime < tRange.getTime()) {
		high = idx - 1;

	    } else if ((ptime >
			(erange = (tRange.getPtimes()
				   [tRange.getNptimes() - 1] +
				   tRange.getDuration()))) ||
		       (!greater && (ptime == erange))) {
		low = idx + 1;

	    } else {
		int nPoints =
		    ((pointsPerRange != null) ?
		     ((Integer)	pointsPerRange.elementAt(idx)).intValue() :
		     tRange.getNptimes());
		if (nPoints == 1) {
		    low = idx + 1;
		    continue;
		}

		TimeRange other =
		    ((otherStoreI == null) ?
		     null :
		     (TimeRange) otherStoreI.elementAt(idx));
		int lPoint,
		    nPoints2;

		if (tRange.getNptimes() == 1) {
		    double value = ptime - tRange.getTime();
		    lPoint = (int) (value*nPoints/tRange.getDuration());
		} else {
		    int low2 = 0,
			high2 = tRange.getNptimes()  - 1;
		    for (int idx1 = (low2 + high2)/2;
			 low2 <= high2;
			 idx1 = (low2 + high2)/2) {
			double ttime = tRange.getPtimes()[idx1];

			if (ptime < ttime) {
			    high2 = idx1 - 1;
			} else {
			    low2 = idx1 + 1;
			}
		    }
		    lPoint = high2;
		}

		nPoints2 = lPoint + 1;
		if (nPoints2 < nPoints) {
		    numberInArray -= (nPoints - nPoints2);
		    TimeRange ref2,
			other2,
			ref3 = null,
			other3 = null;

		    if (tRange.getNptimes() == 1) {
			ref2 = new TimeRange
			    (tRange.getTime(),
			     nPoints2*tRange.getDuration()/nPoints);
			ref3 = new TimeRange
			    (tRange.getPointTime(nPoints2,nPoints),
			     tRange.getDuration()*
			     (nPoints - nPoints2)/nPoints);
		    } else {
			double[] times = new double[nPoints2];
			System.arraycopy(tRange.getPtimes(),
					 0,
					 times,
					 0,
					 nPoints2);
			ref2 = new TimeRange(times,tRange.getDuration());
			times = new double[nPoints - nPoints2];
			System.arraycopy(tRange.getPtimes(),
					 nPoints2,
					 times,
					 0,
					 times.length);
			ref3 = new TimeRange(times,tRange.getDuration());
		    }
		    refStoreI.setElementAt(ref2,idx);

		    if (other != null) {
			if (other.getNptimes() == 1) {
			    other2 = new TimeRange
				(other.getTime(),
				 nPoints2*other.getDuration()/nPoints);
			    other3 = new TimeRange
				(other.getPointTime(nPoints2,nPoints),
				 other.getDuration()*
				 (nPoints - nPoints2)/nPoints);
			} else {
			    double[] times = new double[nPoints2];
			    System.arraycopy(other.getPtimes(),
					     0,
					     times,
					     0,
					     nPoints2);
			    other2 = new TimeRange(times,
						   other.getDuration());
			    times = new double[nPoints - nPoints2];
			    System.arraycopy(other.getPtimes(),
					     nPoints2,
					     times,
					     0,
					     times.length);
			    other3 = new TimeRange(times,
						   other.getDuration());
			}
			otherStoreI.setElementAt(other2,idx);
		    }

		    if (pointsPerRange != null) {
			pointsPerRange.setElementAt(new Integer(nPoints2),idx);
		    }

		    if (nPoints2 < nPoints) {
			insertConsecutive(0,
					  nPoints - nPoints2,
					  nPoints - nPoints2,
					  null,
					  ref3,
					  other3,
					  idx + 1,
					  refStoreI,
					  otherStoreI);
		    }
		}

		low = idx + 1;
		break;
	    }
	}

	return (low);
    }

    /**
     * Gets the data array.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the data array.
     * @since V2.0
     * @version 09/18/20010
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final Object getData() {
	return (data);
    }
	
	/**
	  * Returns the RBNB data type of the underlying data.
	  */
	  
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2005/03/31  WHF	Created.
     *
     */
	public final int getDataType() { return dType; }
	
	/**
	  * Returns the point size of the underlying data.
	  */
	  
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2006/06/20  EMF	Created.
     *
     */
	public final int getPointSize() { return ptSize; }
	
	/**
	  * Returns the number of points currently in the DataArray
	  */
	  
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 2006/06/20  EMF	Created.
     *
     */
	public final int getNumInArray() { return numberInArray; }

    /**
     * Gets the frame values as an array of individual frame indexes.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the frame array.
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/18/2001  INB	Created.
     *
     */
    public final double[] getFrame() {
	if (frames == null) {
	    if (frameRanges != null) {
		frames = new double[getNumberOfPoints()];

		if (pointsPerRange != null) {
		    for (int idx = 0, idx1 = 0;
			 idx < frameRanges.size();
			 ++idx) {
			TimeRange fRange = (TimeRange)
			    frameRanges.elementAt(idx);
			int ppR = ((Integer)
				   pointsPerRange.elementAt(idx)).intValue();

			for (int idx2 = 0; idx2 < ppR; ++idx2, ++idx1) {
			    frames[idx1] = fRange.getPointTime(idx2,ppR);
			}
		    }

		} else if (frameRanges.size() == getNumberOfPoints()) {
		    for (int idx = 0; idx < frameRanges.size(); ++idx) {
			TimeRange fRange = (TimeRange)
			    frameRanges.elementAt(idx);

			frames[idx] = fRange.getTime();
		    }

		} else if (timeRanges != null) {
		    for (int idx = 0, idx1 = 0;
			 idx < timeRanges.size();
			 ++idx) {
			TimeRange tRange = (TimeRange)
			    timeRanges.elementAt(idx),
			    fRange = (TimeRange) frameRanges.elementAt(idx);
			for (int idx2 = 0;
			     idx2 < tRange.getNptimes();
			     ++idx2, ++idx1) {
			    frames[idx1] = fRange.getPointTime
				(idx2,
				 tRange.getNptimes());
			}
		    }

		} else {
		    for (int idx = 0, idx1 = 0;
			 idx < frameRanges.size();
			 ++idx) {
			TimeRange fRange = (TimeRange)
			    frameRanges.elementAt(idx);
			System.arraycopy(fRange.getPtimes(),
					 0,
					 frames,
					 idx1,
					 fRange.getNptimes());
			idx1 += fRange.getNptimes();
		    }
		}
	    }
	}

	return (frames);
    }

    /**
     * Gets the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the MIME type.
     * @since V2.0
     * @version 04/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    public final String getMIMEType() {
	return (mimeType);
    }

    /**
     * Gets the number of points represented by this <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the number of points.
     * @since V2.0
     * @version 04/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    public final int getNumberOfPoints() {
	return (numberOfPoints);
    }

    /**
     * Gets the start time of the <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the start time.
     * @see #getDuration()
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/15/2002  INB	Created.
     *
     */
    public final double getStartTime() {
	double startTimeR = -Double.MAX_VALUE;

	if (timeRanges != null) {
	    startTimeR = ((TimeRange) timeRanges.elementAt(0)).getTime();
	}

	return (startTimeR);
    }

    /**
     * Gets the time values as an array of individual times for each point.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the time array.
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/30/2000  INB	Created.
     *
     */
    public final double[] getTime() {
	if (times == null) {
	    if (timeRanges != null) {
		times = new double[getNumberOfPoints()];

		if (pointsPerRange != null) {
		    for (int idx = 0, idx1 = 0;
			 idx < timeRanges.size();
			 ++idx) {
			TimeRange tRange = (TimeRange)
			    timeRanges.elementAt(idx);
			int ppR = ((Integer)
				   pointsPerRange.elementAt(idx)).intValue();

			for (int idx2 = 0; idx2 < ppR; ++idx2, ++idx1) {
			    times[idx1] = tRange.getPointTime(idx2,ppR);
			}
		    }

		} else if (timeRanges.size() == getNumberOfPoints()) {
		    for (int idx = 0; idx < timeRanges.size(); ++idx) {
			TimeRange tRange =
			    (TimeRange) timeRanges.elementAt(idx);
			times[idx] = tRange.getTime();
		    }

		} else {
		    for (int idx = 0, idx1 = 0;
			 idx < timeRanges.size();
			 ++idx) {
			TimeRange tRange =
			    (TimeRange) timeRanges.elementAt(idx);
			System.arraycopy(tRange.getPtimes(),
					 0,
					 times,
					 idx1,
					 tRange.getNptimes());
			idx1 += tRange.getNptimes();
		    }
		}
	    }
	}

	return (times);
    }

    /**
     * Inserts consecutive data sorted by either times or frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sIndexI	  the starting index of the point to add.
     * @param nPointsI	  the number of points to add.
     * @param tPointsI	  the total number of points being added for the
     *			  <code>TimeRanges</code>.
     * @param dBlockI	  the <code>DataBlock</code> to add.
     * @param refI	  the time or frame <code>TimeRange</code> reference.
     * @param otherI      the optional other reference.
     * @param refIndexI   the starting index in the reference vector.
     * @param refStoreI	  the vector to hold the reference.
     * @param otherStoreI the vector to store the other reference.
     * @exception java.lang.Illegasize:lArgumentException
     *		  thrown if previous calls to <code>add</code> had different
     *		  optional fields set.
     * @since V2.0
     * @version 11/26/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    private final void insertConsecutive(int sIndexI,
					 int nPointsI,
					 int tPointsI,
					 DataBlock dBlockI,
					 TimeRange refI,
					 TimeRange otherI,
					 int refIndexI,
					 java.util.Vector refStoreI,
					 java.util.Vector otherStoreI)
    {
	if ((dBlockI != null) && (data != null)) {
	    int dIndex = numberInArray;

	    if (refIndexI < refStoreI.size()) {
		dIndex = 0;

		if (pointsPerRange == null) {
		    TimeRange tRange;
		    for (int idx = 0; idx < refIndexI; ++idx) {
			tRange = (TimeRange) refStoreI.elementAt(idx);
			dIndex += tRange.getNptimes();
		    }
		} else {
		    for (int idx = 0; idx < refIndexI; ++idx) {
			dIndex += ((Integer)
				   pointsPerRange.elementAt(idx)).intValue();
		    }
		}

		if (dBlockI.getDtype() == DataBlock.TYPE_BYTEARRAY) {
		    byte[][] ba = (byte[][]) data;
		    for (int idx = numberInArray - dIndex - 1;
			 idx >= 0;
			 --idx) {
			ba[dIndex + nPointsI + idx] = ba[dIndex + idx];
		    }
		    for (int idx = 0; idx < nPointsI; ++idx) {
			ba[dIndex + idx] = new byte[dBlockI.getPtsize()];
		    }

		} else {
		    if (dIndex < numberInArray) {
			System.arraycopy(data,
					 dIndex,
					 data,
					 dIndex + nPointsI,
					 numberInArray - dIndex);
		    }
		}
	    }

	    dBlockI.getDataPoints(sIndexI,
				  data,
				  dIndex,
				  nPointsI);
	}

	if ((nPointsI != refI.getNptimes()) || (pointsPerRange != null)) {
	    if (pointsPerRange == null) {
		pointsPerRange = new java.util.Vector();
		for (int idx = 0; idx < refStoreI.size(); ++idx) {
		    TimeRange tRange =
			(TimeRange) refStoreI.elementAt(idx);

		    pointsPerRange.addElement
			(new Integer(tRange.getNptimes()));
		}
	    }

	    pointsPerRange.insertElementAt(new Integer(nPointsI),
					   refIndexI);
	}

	addRange(sIndexI,nPointsI,tPointsI,refI,refIndexI,refStoreI);
	if ((otherI != null) && (otherStoreI != null)) {
	    addRange(sIndexI,nPointsI,tPointsI,otherI,refIndexI,otherStoreI);
	}

	numberInArray += nPointsI;
    }

    /**
     * Inserts data sorted by either times or frames.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI	  the number of points to add.
     * @param dBlockI	  the <code>DataBlock</code> to add.
     * @param refI	  the time or frame <code>TimeRange</code> reference.
     * @param otherI      the optional other reference.
     * @param refStoreI	  the vector to hold the reference.
     * @param otherStoreI the vector to store the other reference.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if previous calls to <code>add</code> had different
     *		  optional fields set.
     * @since V2.0
     * @version 09/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    private final void insertData(int nPointsI,
				  DataBlock dBlockI,
				  TimeRange refI,
				  TimeRange otherI,
				  java.util.Vector refStoreI,
				  java.util.Vector otherStoreI)
    {
	byte changing = refI.getChanging();

	if ((changing == TimeRange.UNKNOWN) ||
	    (changing == TimeRange.RANDOM) ||
	    (changing == TimeRange.DECREASING)) {
	    insertRandom(nPointsI,dBlockI,refI,otherI,refStoreI,otherStoreI);

	} else if (numberInArray == 0) {
	    insertConsecutive(0,
			      nPointsI,
			      nPointsI,
			      dBlockI,
			      refI,
			      otherI,
			      0,
			      refStoreI,
			      otherStoreI);

	} else {
	    TimeRange last = (TimeRange) refStoreI.lastElement();
	    if (refI.getTime() >= (last.getPtimes()[last.getNptimes() - 1] +
				   last.getDuration())) {
		insertConsecutive(0,
				  nPointsI,
				  nPointsI,
				  dBlockI,
				  refI,
				  otherI,
				  refStoreI.size(),
				  refStoreI,
				  otherStoreI);
	    } else {
		insertRandom(nPointsI,
			     dBlockI,
			     refI,
			     otherI,
			     refStoreI,
			     otherStoreI);
	    }
	}
    }

    /**
     * Inserts data sorted by either times or frames when the time information
     * is not monotonically increasing relative to the existing information.
     * <p>
     *
     * @author Ian Brown
     *
     * @param nPointsI	  the number of points to add.
     * @param dBlockI	  the <code>DataBlock</code> to add.
     * @param refI	  the time or frame <code>TimeRange</code> reference.
     * @param otherI      the optional other reference.
     * @param refStoreI	  the vector to hold the reference.
     * @param otherStoreI the vector to store the other reference.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if previous calls to <code>add</code> had different
     *		  optional fields set.
     * @since V2.0
     * @version 04/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    private final void insertRandom(int nPointsI,
				    DataBlock dBlockI,
				    TimeRange refI,
				    TimeRange otherI,
				    java.util.Vector refStoreI,
				    java.util.Vector otherStoreI)
    {
	int sIndex = 0,
	    refIndex = -1;
	for (int idx = 0; idx < nPointsI; ++idx) {
	    int nIndex = findInsertionPoint(idx,
					    nPointsI,
					    refI,
					    refStoreI,
					    otherStoreI);

	    if ((refIndex != -1) &&
		(nIndex != refIndex) &&
		(idx != sIndex)) {
		insertConsecutive(sIndex,
				  idx - sIndex,
				  nPointsI,
				  dBlockI,
				  refI,
				  otherI,
				  refIndex,
				  refStoreI,
				  otherStoreI);
		refIndex = -1;
		++nIndex;
	    }

	    if (refIndex == -1) {
		sIndex = idx;
	    }
	    refIndex = nIndex;
	}

	insertConsecutive(sIndex,
			  nPointsI - sIndex,
			  nPointsI,
			  dBlockI,
			  refI,
			  otherI,
			  refIndex,
			  refStoreI,
			  otherStoreI);
    }

    /**
     * Sets the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @param mimeTypeI the MIME type.
     * @see #getMIMEType()
     * @since V2.0
     * @version 04/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/24/2002  INB	Created.
     *
     */
    final void setMIMEType(String mimeTypeI) {
	mimeType = mimeTypeI;
    }

    /**
     * Sets the number of data points represented by this
     * <code>DataArray</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param numberOfPointsI the number of points.
     * @param ptSizeI	      the point size (0 means no data storage).
     * @param dTypeI	      the data type.
     * @see #getNumberOfPoints()
     * @since V2.0
     * @version 09/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/12/2002  INB	Created.
     *
     */
    final void setNumberOfPoints(int numberOfPointsI,
				 int ptSizeI,
				 byte dTypeI) {
	numberOfPoints = numberOfPointsI;
	dType = dTypeI;
	ptSize = ptSizeI;

	if (ptSizeI > 0) {
	    switch (dTypeI) {
	    case DataBlock.TYPE_BOOLEAN:
		data = new boolean[numberOfPointsI];
		break;

	    case DataBlock.TYPE_INT16:
		data = new short[numberOfPointsI];
		break;

	    case DataBlock.TYPE_INT32:
		data = new int[numberOfPointsI];
		break;

	    case DataBlock.TYPE_INT64:
		data = new long[numberOfPointsI];
		break;

	    case DataBlock.TYPE_FLOAT32:
		data = new float[numberOfPointsI];
		break;

	    case DataBlock.TYPE_FLOAT64:
		data = new double[numberOfPointsI];
		break;

	    case DataBlock.TYPE_STRING:
		data = new String[numberOfPointsI];
		break;

	    case DataBlock.TYPE_INT8:
	    case DataBlock.TYPE_BYTEARRAY:
	    case DataBlock.UNKNOWN:
	    default:
		// For unknown data types: use the byte primitive type.
		if (((dType == DataBlock.TYPE_INT8) ||
		     (dType == DataBlock.UNKNOWN)) &&
		    (ptSizeI == 1)) {
		    // With a single byte per point, return a byte array.
		    data = new byte[numberOfPointsI];

		} else {
		    // With multiple bytes per point, return an array of
		    // byte arrays.
		    byte[][] dataT = new byte[numberOfPointsI][];
		    if ((dType == DataBlock.TYPE_INT8) ||
			(dType == DataBlock.UNKNOWN)) {
			for (int idx = 0; idx < numberOfPointsI; ++idx) {
			    dataT[idx] = null;
			}
		    }
		    data = dataT;
		}
		break;
	    }
	}
    }

    /**
     * Converts this <code>DataArray</code> to an <code>Rmap</code>.
     * <p>
     * This method attempts to create the most efficient <code>Rmap</code>,
     * which means that it will collapse time information when possible.
     * <p>
     * This method goes through the Vector of TimeRange objects and compares
     * the current TimeRange (in the variable workRange) to the last TimeRange
     * we considered (in the variable ltRange).
     *
     * @author Ian Brown
     *
     * @return the <code>Rmap</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalArgumentException
     *		  thrown if there is a problem with the input name.
     * @exception java.lang.IllegalStateException
     *		  thrown if this <code>Rmap</code> already has a parent.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Handle case where the number of points of data is not
     *			equal to the number of point times given a duration of
     *			zero.
     * 09/17/2002  INB	Created.
     *
     */
    final Rmap toRmap()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	if ((timeRanges == null) ||
	    (dType == DataBlock.TYPE_STRING) ||
	    (dType == DataBlock.TYPE_BYTEARRAY) ||
	    (dType == DataBlock.UNKNOWN) ||
	    (ptSize == 0)) {
	    return (null);
	}
	
	// The new, compressed Rmap that we will produce
	Rmap rmapR = new Rmap();
	
	
	//////////////////////////////
	//
	// Examine the first TimeRange
	//
	//////////////////////////////
	
	// When looping through all the TimeRanges, ltRange is the previous
	// TimeRange we examined
	TimeRange
	    ltRange =
	        (TimeRange)((TimeRange) timeRanges.firstElement()).clone();
	TimeRange workRange;
	DataBlock ldBlock;
	// When looping through all the TimeRanges, lnPoints is the number of
	// points in the previous TimeRange we examined
	int lnPoints =
	    ((pointsPerRange == null) ?
	     ((timeRanges.size() > 1) ?
	      ((TimeRange) timeRanges.firstElement()).getPtimes().length :
	      numberOfPoints) :
	     ((Integer) pointsPerRange.firstElement()).intValue());
	int workPoints;
	int startPoint = 0;
	// What is called "duration" here is really the delta-T between
	// points in a compressed frame (that is, a frame which just has a
	// single start time and duration).
	// NOTE: timepoint frames made up of a series of point where each point
	// has its own start time and zero duration  will return 0.0 for
	// ltRange.getDuration()
	double duration = ltRange.getDuration()/lnPoints;
	// mjm
	/* 
	System.err.println("ltRange.duration: "+ltRange.getDuration());
	System.err.println("ltRange.Ptimes: "+ltRange.getPtimes().length);
	System.err.println("lnPoints: "+lnPoints);
	System.err.println("duration: "+duration);
	*/
	double[] pTimes;
	java.util.Vector times = null;
        //EMF 8/22/06: if repeated times, zero duration, leave as timerange
        //             instead of expanding, else archive hdr.rbn is very
        //             large due to repeated times
//        if (duration==0. && lnPoints>1) {
//            System.err.println("duration 0 && lnPoints "+lnPoints);
//        } 
	if (duration == 0.) {
	    // This TimeRange must be either for a frame with a single point
	    // and zero duration or else a timepoint frame made up of a
	    // series of points where each point has its own start time and
	    // zero duration
	    times = new java.util.Vector();
	    if (ltRange.getPtimes().length == lnPoints) {
		times.addElement(ltRange.getPtimes());
	    } else {
		// JPW ??? Not sure if we would ever reach this case ???
		// I think we only reach this case when dealing with a
		// compressed frame (described by a single start time and
		// duration).
		// If we did reach this case, instead of using
		// ltRange.getPtimes() I think we should use
		// ltRange.getPointTime(idx,lnPoints);
		for (int idx = 0; idx < lnPoints; ++idx) {
		    times.addElement(ltRange.getPtimes());
		}
	    }
	}
	
	//System.err.println("timeRanges.size: "+timeRanges.size()+", duration: "+duration);
	
	/*****  DEBUG PRINTOUTS *****
	System.err.println("DataArray.toRmap(): time range #0");
	System.err.println("    ltRange.duration: " + ltRange.getDuration());
	System.err.println("    lnPoints: " + lnPoints);
	System.err.println("    duration (delta-T): " + duration);
	// printouts of the arrays in the times Vector
	if (times == null) {
	    System.err.println("    times = null");
	} else {
	    System.err.print("    times: ");
	    for (int ii = 0; ii < times.size(); ++ii) {
		double[] entry = (double[]) times.elementAt(ii);
		for (int jj = 0; jj < entry.length; ++jj) {
		    System.err.print(entry[jj] + "  ");
		}
	    }
	}
	***********/
	
	////////////////////////////////////////////////////
	//
	// Examine each of the rest of the TimeRange objects
	//
	////////////////////////////////////////////////////
	
	for (int idx = 1; idx < timeRanges.size(); ++idx) {
	    workRange = (TimeRange) timeRanges.elementAt(idx);
	    if (pointsPerRange == null) {
		workPoints = workRange.getPtimes().length;
	    } else {
		workPoints = ((Integer)
			      pointsPerRange.elementAt(idx)).intValue();
	    }
	    
	    /*****  DEBUG PRINTOUTS *****
	    System.err.println("\n\nDataArray.toRmap(): time range #" + idx);
	    System.err.println("    workRange.duration: " + workRange.getDuration());
	    System.err.println("    workPoints: " + workPoints);
	    System.err.print("    workRange.getPtimes(): ");
	    double[] ptentry = (double[])workRange.getPtimes();
	    for (int jjj = 0; jjj < ptentry.length; ++jjj) {
		System.err.print(ptentry[jjj] + "  ");
	    }
	    System.err.println("    workRange.getDuration()/workPoints: " + workRange.getDuration()/workPoints);
	    System.err.println("    duration (delta-T): " + duration);
	    // printouts of the arrays in the times Vector
	    if (times == null) {
		System.err.println("    times = null");
	    } else {
		System.err.print("    times: ");
		for (int ii = 0; ii < times.size(); ++ii) {
		    double[] entry = (double[]) times.elementAt(ii);
		    for (int jj = 0; jj < entry.length; ++jj) {
			System.err.print(entry[jj] + "  ");
		    }
		}
		System.err.println("");
	    }
	    ***********/
	    
	    if (duration > 0.) {
		// If the last TimeRange was a compressed frame and if this
		// current work TimeRange is a compressed frame, see if the
		// two TimeRange objects can be combined into one big
		// compressed frame.
		if ((workRange.getTime() ==
		     ltRange.getTime() + ltRange.getDuration()) &&
		    (workRange.getDuration()/workPoints == duration))
		{
		    // The two TimeRanges can be compressed into a big
		    // compressed TimeRange!
		    ltRange.setDuration
			(ltRange.getDuration() + workRange.getDuration());
		    lnPoints += workPoints;

		}
		else
		{
		    // Can't combine the two compressed TimeRange objects;
		    // write out the previous times and data to the Rmap
		    ldBlock = extractDataBlock(startPoint,lnPoints);
		    startPoint += lnPoints;
		    if (times != null) {
			pTimes = new double[lnPoints];
			for (int idx1 = 0,
				 idx2 = 0;
			     idx1 < times.size();
			     ++idx1) {
			    double[] entry = (double[]) times.elementAt(idx1);
			    for (int idx3 = 0; idx3 < entry.length; ++idx3) {
				pTimes[idx2++] = entry[idx3];
			    }
			}
			ltRange = new TimeRange(pTimes,0.);
		    }
		    rmapR.addChild(new Rmap(null,ldBlock,ltRange));
		    ltRange = workRange;
		    lnPoints = workPoints;
		    duration = ltRange.getDuration()/lnPoints;
		    if (duration == 0.) {
			// This current TimeRange has individual timepoints;
			// write them as an array to the times Vector
			times = new java.util.Vector();
			times.addElement(workRange.getPtimes());
		    } else {
			times = null;
		    }
		}

	    } else if (ltRange.getDuration() == 0.) {
		// With ltRange.getDuration() == 0, I believe the last
		// TimeRange contained individual timepoints
		if (times != null) {
		    if (workRange.getPtimes().length == workPoints) {
			// The current TimeRange is a timepoint TimeRange
			// made up of individual timepoints.
			times.addElement(workRange.getPtimes());
		    } else {
			// The current TimeRange is a compressed TimeRange
			// represented by a single start time and duration
			// JPW 03/25/2006: I belive we should use
			//                 workRange.getPointTime() instead of
			//                 workRange.getPtimes(); for a
			//                 compressed TimeRange, getPtimes()
			//                 will just return a single start time
			// for (int idx1 = 0; idx1 < workPoints; ++idx1) {
			    // Orig
			    // times.addElement(workRange.getPtimes());
			// }
			double[] tempTimes = new double[workPoints];
			for (int idx1 = 0; idx1 < workPoints; ++idx1) {
			    tempTimes[idx1] = workRange.getPointTime(idx1,workPoints);
			}
			times.addElement(tempTimes);
			
		    }
		    lnPoints += workPoints;
		} else {
		    ldBlock = extractDataBlock(startPoint,lnPoints);
		    startPoint += lnPoints;
		    // JPW ?? I think we would only get to this point
		    //        in the code if times == null; therefore, the
		    //        following code won't ever execute
		    if (times != null) {
			pTimes = new double[lnPoints];
			for (int idx1 = 0,
				 idx2 = 0;
			     idx1 < times.size();
			     ++idx1) {
			    double[] entry = (double[]) times.elementAt(idx1);
			    for (int idx3 = 0; idx3 < entry.length; ++idx3) {
				pTimes[idx2++] = entry[idx3];
			    }
			}
			ltRange = new TimeRange(pTimes,0.);
		    }
		    rmapR.addChild(new Rmap(null,ldBlock,ltRange));
		    ltRange = workRange;
		    lnPoints = workPoints;
		    duration = ltRange.getDuration()/lnPoints;
		    if (duration == 0.) {
			times = new java.util.Vector();
			times.addElement(workRange.getPtimes());
		    } else {
			times = null;
		    }
		}

	    } else {
		// I think we'll only get here if duration < 0.0
		if ((duration != 0.) &&
		    (workRange.getTime() ==
		     (ltRange.getTime() + ltRange.getDuration())) &&
		    (workRange.getDuration()/workPoints ==
		     duration/lnPoints)) {
		    ltRange.setDuration(ltRange.getDuration() - duration);
		    lnPoints += workPoints;
		} else {
		    ldBlock = extractDataBlock(startPoint,lnPoints);
		    startPoint += lnPoints;
		    if (times != null) {
			pTimes = new double[lnPoints];
			for (int idx1 = 0,
				 idx2 = 0;
			     idx1 < times.size();
			     ++idx1) {
			    double[] entry = (double[]) times.elementAt(idx1);
			    for (int idx3 = 0; idx3 < entry.length; ++idx3) {
				pTimes[idx2++] = entry[idx3];
			    }
			}
			ltRange = new TimeRange(pTimes,0.);
		    }
		    rmapR.addChild(new Rmap(null,ldBlock,ltRange));
		    ltRange = workRange;
		    lnPoints = workPoints;
		    duration = ltRange.getDuration()/lnPoints;
		    if (duration == 0.) {
			times = new java.util.Vector();
			times.addElement(workRange.getPtimes());
		    } else {
			times = null;
		    }
		}
	    }
	}
	
	// Write out the last TimeRange to the Rmap
	ldBlock = extractDataBlock(startPoint,lnPoints);
	if (times != null) {
	    pTimes = new double[lnPoints];
	    for (int idx1 = 0,
		     idx2 = 0;
		 idx1 < times.size();
		 ++idx1) {

		double[] entry = (double[]) times.elementAt(idx1);
		for (int idx3 = 0; idx3 < entry.length; ++idx3) {
		    pTimes[idx2++] = entry[idx3];
		}
	    }
	    /*
	    // System.err.println("ltDuration: "+ltRange.getDuration()+", times.size: "+times.size());
	    // start mjm grope:
	    double maxDuration=0.;
	    if(maxDuration == 0.) {
		double pTimes2 = pTimes[0];
		double fDuration = pTimes[pTimes.length-1]-pTimes2;
		ltRange = new TimeRange(pTimes2, fDuration);
	    } else
	    // end mjm grope
	    */

	    ltRange = new TimeRange(pTimes,0.);
	    //System.err.println("ltRrange: "+ltRange);
	}
	if (rmapR.getNchildren() == 0) {
	    rmapR.setTrange(ltRange);
	    rmapR.setDblock(ldBlock);
	} else {
	    rmapR.addChild(new Rmap(null,ldBlock,ltRange));
	}
	
	// System.err.println("DataArray.toRmap(): returned Rmap:\n" + rmapR);
	
	return (rmapR);
    }
    
    /**
     * Returns a string representation of this object.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string representation.
     * @since V2.0
     * @version 04/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/30/2001  INB	Created.
     *
     */
    public final String toString() {
	String stringR = "DataArray: ";
	double[] frames = getFrame(),
		 times = getTime();

	for (int idx = 0; idx < getNumberOfPoints(); ++idx) {
	    stringR += (((times != null) ? ("" + times[idx]) : "") +
			((frames != null) ? (", F" + frames[idx]) : "") +
			((getMIMEType() != null) ?
			 (", " + getMIMEType()) : "") +
			((getData() != null) ?
			 (" = (" +
			  java.lang.reflect.Array.get(getData(),
						      idx) + ")") : "") +
			"\n");
	}

	return (stringR);
    }

    /*
    public final static void main(String[] argsI) {
      try {
	DataBlock dBlock;
	TimeRange tRange,
	    fRange;
	int[] data,
	    data2 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
	double[] times;
	DataArray dArray;
	int[] pointIdx = {  0,  1,  2,  4,  6,
				    3,  5,  7,  9, 11,
					    8, 10, 12, 13, 14 };
	Rmap rmap;

	dArray = new DataArray();
	dArray.setNumberOfPoints(15,4,DataBlock.TYPE_INT32);

	for (int idx = 0; idx < 3; ++idx) {
	    tRange = new TimeRange(idx,1);
	    fRange = new TimeRange(idx + 1,0);
	    data = new int[5];
	    for (int idx1 = 0; idx1 < 5; ++idx1) {
		data[idx1] = idx*5 + idx1;
	    }
	    dBlock = new DataBlock(data,
				   5,
				   4,
				   DataBlock.TYPE_INT32,
				   DataBlock.ORDER_MSB,
				   false,
				   0,
				   4);
	    dArray.add(5,dBlock,tRange,fRange);
	}

	System.err.println("Consecutive:");
	System.out.println(dArray);
	System.out.println(dArray.timeRanges);
	System.out.println(dArray.frameRanges);
	System.out.println(dArray.pointsPerRange);
	rmap = new Rmap("Test");
	rmap.addDataWithTimeReference(data2,
				      15,
				      4,
				      DataBlock.TYPE_INT32,
				      DataBlock.ORDER_MSB,
				      dArray);
	System.err.println(rmap);
	System.out.println("");

	dArray = new DataArray();
	dArray.setNumberOfPoints(15,4,DataBlock.TYPE_INT32);
	for (int idx = 0; idx < 3; ++idx) {
	    times = new double[5];
	    for (int idx1 = 0; idx1 < times.length; ++idx1) {
		times[idx1] = idx + idx1*1./times.length;
	    }
	    tRange = new TimeRange(times,1./times.length);
	    fRange = new TimeRange(idx + 1,0);
	    data = new int[5];
	    for (int idx1 = 0; idx1 < 5; ++idx1) {
		data[idx1] = idx*5 + idx1;
	    }
	    dBlock = new DataBlock(data,
				   5,
				   4,
				   DataBlock.TYPE_INT32,
				   DataBlock.ORDER_MSB,
				   false,
				   0,
				   4);
	    dArray.add(5,dBlock,tRange,fRange);
	}

	System.err.println("Consecutive point times:");
	System.out.println(dArray);
	System.out.println(dArray.timeRanges);
	System.out.println(dArray.frameRanges);
	System.out.println(dArray.pointsPerRange);
	rmap = new Rmap("Test");
	rmap.addDataWithTimeReference(data2,
				      15,
				      4,
				      DataBlock.TYPE_INT32,
				      DataBlock.ORDER_MSB,
				      dArray);
	System.err.println(rmap);
	System.out.println("");

	dArray = new DataArray();
	dArray.setNumberOfPoints(15,4,DataBlock.TYPE_INT32);
	for (int idx = 0; idx < 3; ++idx) {
	    times = new double[5];
	    for (int idx1 = 0; idx1 < times.length; ++idx1) {
		int value = (20*idx)/5 + idx1*10/times.length;
		times[idx1] = value/10.;
	    }
	    tRange = new TimeRange(times,0.);
	    fRange = new TimeRange(idx + 1,0);
	    data = new int[5];
	    for (int idx1 = 0; idx1 < 5; ++idx1) {
		data[idx1] = pointIdx[idx*5 + idx1];
	    }
	    dBlock = new DataBlock(data,
				   5,
				   4,
				   DataBlock.TYPE_INT32,
				   DataBlock.ORDER_MSB,
				   false,
				   0,
				   4);
	    dArray.add(5,dBlock,tRange,fRange);
	}

	System.err.println("Overlapping point times:");
	System.out.println(dArray);
	System.out.println(dArray.timeRanges);
	System.out.println(dArray.frameRanges);
	System.out.println(dArray.pointsPerRange);
	rmap = new Rmap("Test");
	rmap.addDataWithTimeReference(data2,
				      15,
				      4,
				      DataBlock.TYPE_INT32,
				      DataBlock.ORDER_MSB,
				      dArray);
	System.err.println(rmap);
	System.out.println("");

	dArray = new DataArray();
	dArray.setNumberOfPoints(15,4,DataBlock.TYPE_INT32);
	for (int idx = 0; idx < 3; ++idx) {
	    tRange = new TimeRange(((int) (20*idx)/5)/10.,1.);
	    fRange = new TimeRange(idx + 1,0);
	    data = new int[5];
	    for (int idx1 = 0; idx1 < 5; ++idx1) {
		data[idx1] = pointIdx[idx*5 + idx1];
	    }
	    dBlock = new DataBlock(data,
				   5,
				   4,
				   DataBlock.TYPE_INT32,
				   DataBlock.ORDER_MSB,
				   false,
				   0,
				   4);
	    dArray.add(5,dBlock,tRange,fRange);
	}

	System.err.println("Overlapping start and duration:");
	System.out.println(dArray);
	System.out.println(dArray.timeRanges);
	System.out.println(dArray.frameRanges);
	System.out.println(dArray.pointsPerRange);
	rmap = new Rmap("Test");
	rmap.addDataWithTimeReference(data2,
				      15,
				      4,
				      DataBlock.TYPE_INT32,
				      DataBlock.ORDER_MSB,
				      dArray);
	System.err.println(rmap);
	System.out.println("");
      } catch (java.lang.Exception e) {
	e.printStackTrace();
      }
    }
    */
}
