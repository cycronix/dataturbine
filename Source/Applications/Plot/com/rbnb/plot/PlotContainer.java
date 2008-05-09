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


// JPW 04/07/2005: Convert to Swing

package com.rbnb.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
//import java.awt.Container;
import java.awt.Dimension;
//import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
//import java.lang.Math;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.rbnb.utility.ToString;
//EMF 5/18/01: use replacement RegisteredChannel,Time,Channel
//import COM.Creare.RBNB.RegisteredChannel;
//import COM.Creare.RBNB.Time;
//import COM.Creare.RBNB.API.Channel;
import com.rbnb.utility.ByteConvert;
import com.rbnb.utility.KeyValueHash;

//public class PlotContainer extends Container {
    public class PlotContainer extends JComponent {
	public static final int ShortData=1;
   public static final int FloatData=2;
   public static final int DoubleData=3;
   //EMF 9/12/00: support for 4 byte integers
   public static final int IntData=4;
   private boolean fourByteIsFloat=true;
   private boolean beenPainted=false;
   private Dimension size=null;
   private String plottitle=null;
   private PlotTitle pTitle=null;
   private PlotAbscissa pAbscissa=null;
   private String absLabel = new String("");
   private PlotOrdinate pOrdinate=null;
   private PlotArea pArea=null;
   private int ordMode=FloatData;
   private boolean beenScaled=false;
   private double scaleMin=0.0,scaleMax=1.0;
   private int scaleDiv=5;
   private boolean scaleAuto=true;
   private boolean scaleDecrease=false;
	private int displayMode;
	private String tableUnits=null;
	private double tableFirst=0;
	private double tableLast=0;
	private double tableMin=0;
	private double tableMax=0;
	private double tableAve=0;
	private double tableStdDev=0;
	private Dimension oldSize=new Dimension(0,0);
	private boolean newData=true;
	private Image bufferImage=null;
	private FontMetrics fm = getFontMetrics(Environment.FONT12);
	private int lastUnknownPointSize=0;	//used to keep from repeatedly printing error message
	private byte[] oldUserData = null;
	private float oldScale=1;
	private float oldOffset=0;
	private float preScale=1;
	private float preOffset=0;
	private boolean preScaleOffset = false;
   //EMF 11/9/00 
   private Environment environment=null;
   //EMF 4/19/05
   private PosDurCubby posDurCubby=null;
   private Dimension prefSize=new Dimension(100,100);
   //EMF 10/12/06
   private boolean showTitle=false;

   // constructor
	public PlotContainer(RegChannel rc,int dm,PosDurCubby pdc,Environment e) {
          this(rc,dm,pdc,e,false);
 }

   // constructor
	public PlotContainer(RegChannel rc,int dm,PosDurCubby pdc,Environment e,boolean showTitleI) {
                showTitle=showTitleI;
		plottitle=rc.name;
		displayMode=dm;
        environment=e;
		posDurCubby=pdc;
      //EMF set display to integer if appropriate
      if (environment.FOURBYTEASINTEGER) fourByteIsFloat=false;
//      size=getSize();
//      System.out.println("plotcontainer " + plottitle + "size: " + size);
      setLayout(new BorderLayout());
      //add(pTitle = new PlotTitle(plottitle),"North");
      //add(pAbscissa = new PlotAbscissa(absLabel,pdc,environment),"South");
      //add(pOrdinate = new PlotOrdinate(pTitle),"West");
      //add(pArea = new PlotArea(this,e.SCROLLGRIDLINES),"Center");
      //EMF 8/19/05: make plottitle invisible, since JInternalFrame has channel name
	  add(pTitle = new PlotTitle(plottitle),BorderLayout.NORTH);
	  pTitle.setVisible(showTitle);
      add(pAbscissa = new PlotAbscissa(absLabel,pdc,environment),BorderLayout.SOUTH);
      add(pOrdinate = new PlotOrdinate(pAbscissa),BorderLayout.WEST);
      add(pArea = new PlotArea(this,e.SCROLLGRIDLINES),BorderLayout.CENTER);
      if (displayMode!=LayoutCubby.PlotMode) {
			pTitle.setVisible(false);
			pAbscissa.setVisible(false);
			pOrdinate.setVisible(false);
			pArea.setVisible(false);
			}
		//set scaling if necessary
		if (environment.SCALE_DIV!=-1) {
			scaleAuto=false;
			beenScaled=true;
			scaleMin=environment.SCALE_MIN;
			scaleMax=environment.SCALE_MAX;
			scaleDiv=environment.SCALE_DIV;
			}
		setOrdinate(scaleMin,scaleMax,scaleDiv);
		//check static userdata for scale,offset,units
		if (rc.isStaticUserData((short)1)) {
			KeyValueHash kvpairs=new KeyValueHash(rc.staticUserData);
			String value = (String)kvpairs.get("scale");
			if (value!=null) {
				//System.out.println("scale "+value);
				preScale=(new Float(value)).floatValue();
				preScaleOffset=true;
				}
			value = (String)kvpairs.get("offset");
			if (value!=null) {
				//System.out.println("offset "+value);
				preOffset=(new Float(value)).floatValue();
				preScaleOffset=true;
				}
			value = (String)kvpairs.get("units");
			if (value!=null) {
				pAbscissa.setOrdUnits(value);
				}
			tableUnits=value;
			}
                //EMF 9/12/00: add support for 4 byte integers
                else if (rc.isStaticUserData((short)2)) {
                  fourByteIsFloat=false;
                  }
		}
		
	//EMF 10/18/04: no argument constructor for PlotContainerImage to call
	public PlotContainer() {}
	
	// getConfig method - adds relevent key/value pairs to hashtable, using specified prefix
	public synchronized void getConfig(Hashtable ht, String prefix) {
		ht.put(prefix+"name",plottitle);
		if (scaleAuto && scaleDecrease) ht.put(prefix+"scaling",new String("auto_inc_dec"));
		else if (scaleAuto) ht.put(prefix+"scaling",new String("auto_inc"));
		else ht.put(prefix+"scaling",new String("manual"));
		ht.put(prefix+"divisions",Integer.toString(scaleDiv));
		ht.put(prefix+"min",Double.toString(scaleMin));
		ht.put(prefix+"max",Double.toString(scaleMax));
		}
	
	// setConfig method - sets relevent values from hashtable, using specified prefix
	public synchronized void setConfig(Hashtable ht, String prefix) {
		if (ht.containsKey(prefix+"name")) {
                  plottitle=(String)ht.get(prefix+"name");
                  if (pTitle!=null) pTitle.setTitle(plottitle);
                }
		if (ht.containsKey(prefix+"scaling")) {
			String scaleString=(String)ht.get(prefix+"scaling");
			if (scaleString.equals("auto_inc_dec")) {
				scaleAuto=true;
				scaleDecrease=true;
				}
			else if (scaleString.equals("auto_inc")) {
				scaleAuto=true;
				scaleDecrease=false;
				}
			else scaleAuto=false;
			}
		if (ht.containsKey(prefix+"divisions"))
			scaleDiv=Integer.parseInt((String)ht.get(prefix+"divisions"));
		if (ht.containsKey(prefix+"min"))
			scaleMin=(new Double((String)ht.get(prefix+"min"))).doubleValue();
		if (ht.containsKey(prefix+"max"))
			scaleMax=(new Double((String)ht.get(prefix+"max"))).doubleValue();
		beenScaled=true;
		// push new values to children components
		setOrdinate(scaleMin,scaleMax,scaleDiv);
		//if (ht.containsKey(prefix+"rangelow")&&ht.containsKey(prefix+"rangenominal")&&ht.containsKey(prefix+"rangehigh")) {
		//    float rangelow=Float.parseFloat((String)ht.get(prefix+"rangelow"));
		//    float rangenominal=Float.parseFloat((String)ht.get(prefix+"rangenominal"));
		//    float rangehigh=Float.parseFloat((String)ht.get(prefix+"rangehigh"));
		//    pArea.setRangeLines(rangelow,rangenominal,rangehigh);
		//} else pArea.clearRangeLines();
	}
	
	public void setDisplayMode(int dm) {
		displayMode=dm;
		if (displayMode==LayoutCubby.TableMode) {
			pTitle.setVisible(false);
			pAbscissa.setVisible(false);
			pOrdinate.setVisible(false);
			pArea.setVisible(false);
			}
		else if (displayMode==LayoutCubby.PlotMode) {
			//pTitle.setVisible(true);
			//pTitle.setVisible(false); //EMF 8/19/05: keep invisible
                        pTitle.setVisible(showTitle); //EMF 10/12/06
			pAbscissa.setVisible(true);
			pOrdinate.setVisible(true);
			pArea.setVisible(true);
			//force layout in case size has changed
			invalidate();
			validate();
			}
		else System.out.println("PlotContainer.setDisplayMode: unknown mode: "+displayMode);
		repaint();
		}
		
   public Hashtable getParameters() {
	Hashtable ht=new Hashtable();
	ht.put("divisions",new Integer(scaleDiv));
	ht.put("min",new Double(scaleMin));
	ht.put("max",new Double(scaleMax));
	ht.put("autoscale",new Boolean(scaleAuto));
	ht.put("autodecrease",new Boolean(scaleDecrease));
	return ht;
	}
   
   public void setParameters(Hashtable ht) {
	scaleAuto=((Boolean)ht.get("autoscale")).booleanValue();
	scaleDecrease=((Boolean)ht.get("autodecrease")).booleanValue();
	scaleDiv=((Integer)ht.get("divisions")).intValue();
	scaleMin=((Double)ht.get("min")).doubleValue();
	scaleMax=((Double)ht.get("max")).doubleValue();
	beenScaled=true;
	setOrdinate(scaleMin,scaleMax,scaleDiv);
	}
	
	public void setZoom(double start, double duration) {
		posDurCubby.setZoom(start,duration);
	}

   public void setAbscissa(Time duration) {
      synchronized(this) {
	   beenPainted=false;
	   }
      pAbscissa.setAbscissa(duration);
      pArea.setAbscissa(duration);
      //System.out.println("setAbscissa: "+min+"   "+max);
      }

   public void setOrdinate(double min, double max, int numLines) {
      synchronized(this) {
	   beenPainted=false;
	   }
      pOrdinate.setOrdinate(min,max,numLines);
      pArea.setOrdinate(min,max,numLines);
      //System.out.println("setOrdinate: "+min+"   "+max);
      }
	
	// byteArrayCompare method - returns true if arrays are identical, false otherwise
	private boolean byteArrayCompare(byte[] a,byte[] b) {
		if (a==null || b==null) return false;
		if (a.length!=b.length) return false;
		for (int i=0;i<a.length;i++) if (a[i]!=b[i]) return false;
		return true;
		}

   public void setChannelData(Channel ch,Time start) {
      KeyValueHash kvpairs = null;
      boolean scaleOffset = false;
      float scale=1,offset=0;

      synchronized(this) {
	   beenPainted=false;
		}
      //check for earlier defined scale/offset
		if (preScaleOffset) {
			scale=preScale;
			offset=preOffset;
			scaleOffset=true;
			}
			
      //apply key/value pairs if appropriate
      if (ch.channelUserDataType==1) {
			if (byteArrayCompare(oldUserData,ch.channelUserData)) {
				scaleOffset=true;
				scale=oldScale;
				offset=oldOffset;
				}
			else { 
				kvpairs=new KeyValueHash(ch.channelUserData);
				String value = (String)kvpairs.get("scale");
				if (value!=null) {
					//System.out.println("scale "+value);
					scale=(new Float(value)).floatValue();
					scaleOffset=true;
					}
				value = (String)kvpairs.get("offset");
				if (value!=null) {
					//System.out.println("offset "+value);
					offset=(new Float(value)).floatValue();
					scaleOffset=true;
					}
				value = (String)kvpairs.get("units");
				if (value!=null) {
					pAbscissa.setOrdUnits(value);
					}
				tableUnits=value;
				}
				oldUserData=ch.channelUserData;
				oldScale=scale;
				oldOffset=offset;
			}
      //EMF 9/12/00
      //interpret 4 byte values as ints if userdatatype=2
      else if (ch.channelUserDataType==2) {
	fourByteIsFloat=false;
      }
		
      double[] absData = null;
      //channel contains no data - clear screen and return
      if (ch.numberOfPoints < 1) {
			pArea.setNoData();
			pAbscissa.setStartNum(start,0);
			return;
			}
      try {
			// interval is indivisible, so discrete times do not exist
			// clear screen and return
                        // EMF 9/14/00: API now returns times for indivisible intervals,
                        // leaving the user to make sense of the data, so remove
                        // the check here - no exception will be thrown
			/*for (int i=0; i<ch.timeStamp.numberOfIntervals; i++) {
				if (ch.timeStamp.getIndivisibilityOfInterval(i)) {
					System.out.println("TimeStamp interval is indivisible - cannot plot");
					pArea.setNoData();
					pAbscissa.setStartNum(start,0);
					return;
					}
				}*/
			// convert time stamps to doubles
			if (displayMode!=LayoutCubby.TableMode) {
                           absData = ch.timeStamp.getTimesDouble(0,ch.numberOfPoints);
                        }
			}
      catch (Exception e) {
			System.out.println("timesDouble exception:");
			e.printStackTrace();
			}
       /*if (displayMode!=LayoutCubby.TableMode) for (int i=1;i<absData.length;i++) {
			if ((absData[i]-absData[i-1])<0) {
				System.out.println("Time decreases: "+i+": "+absData[i-1]+"   "+absData[i]);
				}
			}*/
      //get ordinate data and scale/offset if needed
      short[] ordShortData=null;
      //EMF 9/12/00: support for 4 byte integers
      int[] ordIntData=null;
      float[] ordFloatData=null;
      double[] ordDoubleData=null;
//EMF 5/22/01: changes to get data directly, remove 4 point ambiguity
      if (ch.isInt16) {   //assume short format
			boolean byteSwap=false;
			if (ch.byteOrder==Channel.LSB) byteSwap=true;
			//ordShortData=ByteConvert.byte2Short(ch.data,byteSwap);
			ordShortData=ch.getDataInt16();
			if (scaleOffset) {
				ordFloatData=new float[ordShortData.length];
				for (int i=0;i<ordFloatData.length;i++) {
					ordFloatData[i] = scale*ordShortData[i] + offset;
					}
				ordMode=FloatData;
				}
			else {
				ordMode=ShortData;
				newData=true;
				}
			}
      else if (ch.isFloat32) {	//assume float format
			boolean byteSwap=false;
			if (ch.byteOrder==Channel.LSB) byteSwap=true;
			//ordFloatData=ByteConvert.byte2Float(ch.data,byteSwap);
			ordFloatData=ch.getDataFloat32();
			ordMode=FloatData;
			if (scaleOffset) for (int i=0;i<ordFloatData.length;i++) {
				ordFloatData[i] = scale*ordFloatData[i] + offset;
				}
       }
     else if (ch.isInt32) { //convert to ints
			boolean byteSwap=false;
			if (ch.byteOrder==Channel.LSB) byteSwap=true;
			//ordIntData=ByteConvert.byte2Int(ch.data,byteSwap);
			ordIntData=ch.getDataInt32();
			ordMode=IntData;
			if (scaleOffset) {
                          ordFloatData=new float[ordIntData.length];
                          for (int i=0;i<ordFloatData.length;i++) {
				ordFloatData[i] = scale*ordIntData[i] + offset;
				}
                                ordMode=FloatData;
			}
          }
		 // 04/19/2002  WHF  Added:
     else if (ch.isInt8) { //convert to shorts or floats
		 	byte[] temp=ch.getDataInt8();
			if (scaleOffset) {
				ordFloatData=new float[temp.length];
				for (int i=0;i<ordFloatData.length;i++) {
					ordFloatData[i] = scale*temp[i] + offset;
					}
				ordMode=FloatData;
				}
			else {
				ordShortData=new short[temp.length];
				for (int i=0; i<temp.length; ++i)
					ordShortData[i]=(short) temp[i];
				ordMode=ShortData;
				newData=true;
				}
			}
      else if (ch.isFloat64) {	//assume double format
			boolean byteSwap=false;
			if (ch.byteOrder==Channel.LSB) byteSwap=true;
			//ordDoubleData=ByteConvert.byte2Double(ch.data,byteSwap);
			ordDoubleData=ch.getDataFloat64();
			ordMode=DoubleData;
			if (scaleOffset) for (int i=0;i<ordDoubleData.length;i++) {
				ordDoubleData[i] = scale*ordDoubleData[i] + offset;
				}
			}
	  // 2004/12/23  WHF  Possibly handle byte array case:
		else if (ch.isByteArray && "audio/basic".equals(ch.getMimeType())) { 
			// Audio is actually 8 bit unsigned, 8000 Hz.
			// A note about unsigned: It's just like signed, except that instead
			// of a datum of zero, with two's complement, the datum is 128 
			// (0x80) and it's straight binary.
			
			int length = 0;
			byte[][] dba = ch.getDataByteArray();
			for (int ii = 0; ii < dba.length; ++ii)
			length += dba[ii].length;
			if (scaleOffset) {
				ordFloatData = new float[length];
				int c = 0;
				for (int ii = 0; ii < dba.length; ++ii)
				for (int iii = 0; iii < dba[ii].length; ++iii)
				ordFloatData[c++] = scale*(dba[ii][iii]&0xff) + offset;
				ordMode = FloatData;
			} else {
				ordShortData = new short[length];
				int c = 0;
				for (int ii = 0; ii < dba.length; ++ii)
				for (int iii = 0; iii < dba[ii].length; ++iii)
				ordShortData[c++]=(short) (dba[ii][iii]&0xff);
				ordMode = ShortData;
				newData = true;
			}
			// Correct time stamps:
			double [] times = new double[length];
			int c = 0;
			for (int ii = 0; ii < dba.length-1; ++ii) {
				for (int iii = 0; iii < dba[ii].length; ++iii) {
					times[c] = absData[ii] 
							+ (absData[ii+1] - absData[ii])*iii/dba[ii].length;
					++c;
				}
			}
			// For the last one guess 8kHz:
			for (int iii = 0; iii < dba[dba.length-1].length; ++iii) {
				times[c] = absData[absData.length-1]+iii/8000.0;
				++c;
			}
			absData = times;
		} else {  //unimplemented data format - bail out
			if (ch.pointSize!=lastUnknownPointSize) {
				lastUnknownPointSize=ch.pointSize;
				System.err.println("PlotContainer.setChannelData: unknown type in channel "+ch.channelName);
				}
			pArea.setNoData();
			pAbscissa.setStartNum(start,0);
			return;
			}
      
      // compute min/max, autoscale if appropriate, send data to PlotArea
		double min=0,max=0,sum=0,sumsq=0;
      if (ordMode==DoubleData) {
			if (scaleAuto || displayMode==LayoutCubby.TableMode) {
				min=ordDoubleData[0];
				max=min;
				if (displayMode==LayoutCubby.TableMode) {
					sum=min;
					sumsq=min*min;
					}
				for (int i=1;i<ordDoubleData.length;i++) {
					if (ordDoubleData[i]<min) min=ordDoubleData[i];
					else if (ordDoubleData[i]>max) max=ordDoubleData[i];
					if (displayMode==LayoutCubby.TableMode) {
						sum += ordDoubleData[i];
						sumsq += ordDoubleData[i]*ordDoubleData[i];
						}
					}
				if (displayMode!=LayoutCubby.TableMode) {
					if (scaleDecrease || !beenScaled) autoScale(min,max);
					else if ((min<scaleMin) || (max>scaleMax)) autoScale(min,max);
					}
				}
			if (displayMode==LayoutCubby.TableMode) {
				tableFirst=ordDoubleData[0];
				tableLast=ordDoubleData[ordDoubleData.length-1];
				tableMin=min;
				tableMax=max;
				tableAve=sum/ordDoubleData.length;
				tableStdDev=Math.sqrt((ordDoubleData.length*sumsq-sum*sum)/(ordDoubleData.length*ordDoubleData.length));
				}
			else if (displayMode==LayoutCubby.PlotMode) {
				pArea.setDoubleData(absData,ordDoubleData,start.getDoubleValue());
				pAbscissa.setStartNum(start,ordDoubleData.length);
				}
			}
      else if (ordMode==FloatData) { //floating point data
			if (scaleAuto || displayMode==LayoutCubby.TableMode) {
				float minF=ordFloatData[0];
				float maxF=minF;
				if (displayMode==LayoutCubby.TableMode) {
					sum=minF;
					sumsq=minF*minF;
					}
				for (int i=1;i<ordFloatData.length;i++) {
					if (ordFloatData[i]<minF) minF=ordFloatData[i];
					else if (ordFloatData[i]>maxF) maxF=ordFloatData[i];
					if (displayMode==LayoutCubby.TableMode) {
						sum += ordFloatData[i];
						sumsq += ordFloatData[i]*ordFloatData[i];
						}
					}
				//autocasting float to double leaves residue - use string intermediary
				try {
					min=(new Double(Float.toString(minF))).doubleValue();
					max=(new Double(Float.toString(maxF))).doubleValue();
					}
				catch (NumberFormatException e) {
					min=minF;
					max=maxF;
					}
				if (displayMode!=LayoutCubby.TableMode) {
					if (scaleDecrease || !beenScaled) autoScale(min,max);
					else if ((min<scaleMin) || (max>scaleMax)) autoScale(min,max);
					}
				}
			if (displayMode==LayoutCubby.TableMode) {
				tableFirst=ordFloatData[0];
				tableLast=ordFloatData[ordFloatData.length-1];
				tableMin=min;
				tableMax=max;
				tableAve=sum/ordFloatData.length;
				tableStdDev=Math.sqrt((ordFloatData.length*sumsq-sum*sum)/(ordFloatData.length*ordFloatData.length));
				}
			else if (displayMode==LayoutCubby.PlotMode) {
				pArea.setFloatData(absData,ordFloatData,start.getDoubleValue());
				pAbscissa.setStartNum(start,ordFloatData.length);
				}
			}
      //EMF 9/12/00: add support for 4 byte integers
      else if (ordMode==IntData) { //integer data
			if (scaleAuto || displayMode==LayoutCubby.TableMode) {
				min=ordIntData[0];
				max=min;
				if (displayMode==LayoutCubby.TableMode) {
					sum=min;
					sumsq=min*min;
					}
				for (int i=1;i<ordIntData.length;i++) {
					if (ordIntData[i]<min) min=ordIntData[i];
					else if (ordIntData[i]>max) max=ordIntData[i];
					if (displayMode==LayoutCubby.TableMode) {
						sum += ordIntData[i];
						sumsq += ordIntData[i]*ordIntData[i];
						}
					}
				if (displayMode!=LayoutCubby.TableMode) {
					if (scaleDecrease || !beenScaled) autoScale(min,max);
					else if ((min<scaleMin) || (max>scaleMax)) autoScale(min,max);
					}
				}
			if (displayMode==LayoutCubby.TableMode) {
				tableFirst=ordIntData[0];
				tableLast=ordIntData[ordIntData.length-1];
				tableMin=min;
				tableMax=max;
				tableAve=sum/ordIntData.length;
				tableStdDev=Math.sqrt((ordIntData.length*sumsq-sum*sum)/(ordIntData.length*ordIntData.length));
				}
			else if (displayMode==LayoutCubby.PlotMode) {
				pArea.setIntData(absData,ordIntData,start.getDoubleValue());
				pAbscissa.setStartNum(start,ordIntData.length);
				}
			}
      else if (ordMode==ShortData) { //short data
			if (scaleAuto  || displayMode==LayoutCubby.TableMode) {
				min=ordShortData[0];
				max=min;
				if (displayMode==LayoutCubby.TableMode) {
					sum=min;
					sumsq=min*min;
					}
				for (int i=1;i<ordShortData.length;i++) {
					if (ordShortData[i]<min) min=ordShortData[i];
					if (ordShortData[i]>max) max=ordShortData[i];
					if (displayMode==LayoutCubby.TableMode) {
						sum += ordShortData[i];
						sumsq += ordShortData[i]*ordShortData[i];
						}
					}
				if (displayMode!=LayoutCubby.TableMode) {
					if (scaleDecrease || !beenScaled) autoScale(min,max);
					else if ((min<scaleMin) || (max>scaleMax)) autoScale(min,max);
					}
				}
			if (displayMode==LayoutCubby.TableMode) {
				tableFirst=ordShortData[0];
				tableLast=ordShortData[ordShortData.length-1];
				tableMin=min;
				tableMax=max;
				tableAve=sum/ordShortData.length;
				tableStdDev=Math.sqrt((ordShortData.length*sumsq-sum*sum)/(ordShortData.length*ordShortData.length));
				}
			else if (displayMode==LayoutCubby.PlotMode) {
				pArea.setShortData(absData,ordShortData,start.getDoubleValue());
				pAbscissa.setStartNum(start,ordShortData.length);
				}
			}
      else System.out.println("PlotContainer.setChannelData: unknown ordMode "+ordMode);
		newData=true;
		//if (displayMode==LayoutCubby.TableMode) repaint();
		repaint();
      } //end setChannelData method
   
   // autoScale determines axis limits based on the current data extremes
   // and the 125 rule - the limits have a mantissa of 1,2, or 5, enclose
   // all the data points, and share the same power of 10
   private void autoScale(double min,double max) {
//System.err.println("PlotContainer.autoScale: min "+min);
//System.err.println("                         max "+max);
      double powmin=0,powmax=0,pow=0,lower=0,upper=0;
      int num=0; //number of gridlines
      double realmin=min; //save copy of actual values
      double realmax=max;
      double thisScaleMin=scaleMin;
      double thisScaleMax=scaleMax;
      double offset=0;
      boolean rescale=true;
      int rescaleCount=0;

    //EMF 3/26/02: repeat autoscale if data fills < 10% of range
    while (rescale) {
      double thisMin=min;
      double thisMax=max;
      //determine power of 10 (scale)
      if (min==0&&max==0&&offset==0) {
			//System.out.println("min and max both 0...autoscaling from -1 to 1");
			setOrdinate(0.,1.,5);
			return;
			}
      else if (min==0&&max==0) {
        pow=0;
        min=-1e-2;
        max=1e-2;
      }
      else if (min==0) pow=Math.floor(Math.log(Math.abs(max))/Math.log(10));
      else if (max==0) pow=Math.floor(Math.log(Math.abs(min))/Math.log(10));
      else {
			powmin=Math.floor(Math.log(Math.abs(min))/Math.log(10));
			if (beenScaled && !scaleDecrease) powmin=Math.max(powmin,
			   Math.floor(Math.log(Math.abs(thisScaleMin))/Math.log(10)));
			 powmax=Math.floor(Math.log(Math.abs(max))/Math.log(10));
			if (beenScaled && !scaleDecrease) powmax=Math.max(powmax,
				Math.floor(Math.log(Math.abs(thisScaleMax))/Math.log(10)));
			pow=Math.max(powmin,powmax);
			}
      if (min==max) {  //keep flat line away from edge of plot
			min -= Math.abs(min/10);
			max += Math.abs(max/10);
			}
      
      // scale min & max to power of 10
      min=min/Math.pow(10.0,pow);
      max=max/Math.pow(10.0,pow);
      //System.out.println("PlotContainer.autoScale: scaled min="+min+
	 //"   max="+max+"   pow="+pow);
	 
      //tweak values to protect against roundoff error
      if (min!=0) min-=min*1e-4;
      if (max!=0) max-=max*1e-4;
      //System.out.println("PlotContainer.autoScale: tweaked min="+min+
	 //"   max="+max+"   pow="+pow);
	 
      //set lower bound
      if (min>=0) {
	 if      (min<1) lower=0.;
	 else if (min<2) lower=1.*Math.pow(10.0,pow);
	 else if (min<5) lower=2.*Math.pow(10.0,pow);
	 else            lower=5.*Math.pow(10.0,pow);
	 }
      else {
	 if      (min>=-1) lower=-1.*Math.pow(10.0,pow);
	 else if (min>=-2) lower=-2.*Math.pow(10.0,pow);
	 else if (min>=-5) lower=-5.*Math.pow(10.0,pow);
	 //else              lower=-10.*Math.pow(10.0,pow);
	 else              lower=-1.*Math.pow(10.0,pow+1);
	 }
      //user older range min if smaller
      if (beenScaled && !scaleDecrease) lower=Math.min(lower,thisScaleMin);
      
      //set upper bound
      if (max>0) {
	 //if      (max>5) upper=10.*Math.pow(10.0,pow);
	 if      (max>5) upper=Math.pow(10.0,pow+1);
	 else if (max>2) upper=5.*Math.pow(10.0,pow);
	 else if (max>1) upper=2.*Math.pow(10.0,pow);
	 else             upper=Math.pow(10.0,pow);
	 }
      else {
	 if      (max>-1) upper=0.;
	 else if (max>-2) upper=-1.*Math.pow(10.0,pow);
	 else if (max>-5) upper=-2.*Math.pow(10.0,pow);
	 else             upper=-5.*Math.pow(10.0,pow);
	 }
      //use older range max if larger
      if (beenScaled && !scaleDecrease) upper=Math.max(upper,thisScaleMax);
	 
      //System.out.println("PlotContainer:autoScale lower="+lower+"   upper="+upper+"   pow="+pow);
      //System.out.println();
      num=(int)Math.round((upper-lower)/Math.pow(10.0,pow));
      if (num<3) num*=4;
      else if (num<5) num*=2;
      else if (num>10 && num%2==0) num/=2;
      else if (num==15) num=6;

//System.err.println("    lower "+lower);
//System.err.println("    upper "+upper);
//System.err.println("    num "+num);
      //EMF 3/26/02: check if data covers at least 10% of range; if not,
      //             subtract offset and zoom in closer
      // note increasing autoscale should not zoom
      boolean doZoom=true;
//System.err.println("beenScaled "+beenScaled+", scaleDecrease "+scaleDecrease);
      if (beenScaled && !scaleDecrease) doZoom=false;
      if (rescaleCount++ >= 2) doZoom=false;
      if ((upper-lower)/(realmax-realmin) < 10) doZoom=false;
      if (doZoom) {
        //find gridline nearest mean
        double ave=(thisMax+thisMin)/2;
        double[] gridline=new double[num+1];
        double gridSpace=(upper-lower)/num;
        double delta=Double.MAX_VALUE; //distance to gridline
        int gridnum=0;
        for (int i=0;i<num+1;i++) {
          gridline[i]=lower+i*gridSpace;
          double thisDelta=Math.abs(ave-gridline[i]);
//System.err.println("gridline "+gridline[i]);
//System.err.println("thisDelta "+thisDelta);
//System.err.println();
          if (thisDelta<delta) {
            delta=thisDelta;
            gridnum=i;
          }
        }
        min=thisMin-gridline[gridnum];
        max=thisMax-gridline[gridnum];
        thisScaleMin-=gridline[gridnum];
        thisScaleMax-=gridline[gridnum];
        offset+=gridline[gridnum];
        if (rescaleCount>2) rescale=false;
//System.err.println("    rescaleCount "+rescaleCount);
//System.err.println("    delta "+delta);
//System.err.println("    offset "+offset);
//System.err.println("    min "+min);
//System.err.println("    max "+max);
//System.err.println("    rescale "+rescale);
      } else { //do not rescale
        rescale=false;
      }

  } //end while (rescale)
 
     //add offset back into range
     lower+=offset;
     upper+=offset;
        
        
      
      //save state and inform children
      beenScaled=true;
      scaleMin=lower;
      scaleMax=upper;
      scaleDiv=num;
      setOrdinate(scaleMin,scaleMax,scaleDiv);
      //System.out.println("PlotContainer.autoScale: "+scaleMin+"  "+scaleMax+"  "+scaleDiv);
      } //end autoScale()
      
   public void update(Graphics g) {
      System.out.println("PlotContainer.update()");
      paint(g);
      }
      
   //EMF 8/22/05: drop name column, since now in JInteralFrame
   public void paint(Graphics g) {
      if (displayMode==LayoutCubby.PlotMode) {
			}
		else if (displayMode==LayoutCubby.TableMode) {
			boolean newSize=false;
			Dimension size=getSize();
			if (size.width!=oldSize.width || size.height!=oldSize.height) {
				newSize=true;
				oldSize.width=size.width;
				oldSize.height=size.height;
				}
			if (newSize||newData) {
				if (newSize) bufferImage=createImage(size.width,size.height);
//bufferImage=new java.awt.image.BufferedImage(320,200,java.awt.image.BufferedImage.TYPE_INT_RGB);
				Graphics bi=bufferImage.getGraphics();
				if (!newSize) bi.clearRect(0,0,size.width-1,size.height-1);
				try {
					int block=size.width/7; //8;
					int fh = fm.getAscent(); //font ascent in pixels
					int dh = fh; //draw height in pixels
					int cw = fm.charWidth('0'); //number width in pixels
					int fw = block/cw - 1; //precision to fill available space, no exponent
					if (fw>12) fw=12; //maximum precision
					String number=null;
					//make alternate columns white
					bi.setColor(Color.white);
					for (int i=1;i<7;i+=2) {
						bi.fillRect(i*block,0,block,size.height);
						}
					//make alternate columns light gray
					bi.setColor(Color.lightGray);
					for (int i=0;i<7;i+=2) {
						bi.fillRect(i*block,0,block,size.height);
						}
					bi.setColor(Color.black);
					bi.setFont(Environment.FONT12);
					//bi.drawString(plottitle.substring(plottitle.lastIndexOf('/')+1),0,dh);
					//bi.drawString(RegisteredChannel.channel(plottitle),0,dh);
					if (tableUnits!=null) bi.drawString(tableUnits,0,dh);
					number=ToString.toString("%-*"+fw+"g",tableFirst);
					bi.drawString(number,block,dh);
					number=ToString.toString("%-*"+fw+"g",tableLast);
					bi.drawString(number,2*block,dh);
					number=ToString.toString("%-*"+fw+"g",tableMin);
					bi.drawString(number,3*block,dh);
					number=ToString.toString("%-*"+fw+"g",tableMax);
					bi.drawString(number,4*block,dh);
					number=ToString.toString("%-*"+fw+"g",tableAve);
					//System.err.println("tableAve string: "+number);
					bi.drawString(number,5*block,dh);
					number=ToString.toString("%-*"+fw+"g",tableStdDev);
					//System.err.println("tableStdDev string: "+number);
					bi.drawString(number,6*block,dh);
					}
				catch (Exception e) {
					System.out.println("PlotContainer.paint: exception "+e);
					e.printStackTrace();
					}
				newData=false;
				}
			g.drawImage(bufferImage,0,0,null);
			}
      super.paint(g);
		synchronized (this) {
			beenPainted=true;
			}
		}

   public synchronized boolean hasBeenPainted() {
//repaint is sleazy fix for table mode not updating when switch display groups - needs a better fix
		if (displayMode==LayoutCubby.TableMode) repaint();
		return beenPainted;
      }
      
} // end class PlotContainer
