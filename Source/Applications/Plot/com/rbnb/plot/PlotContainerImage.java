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


package com.rbnb.plot;

import java.awt.BorderLayout;
import java.awt.Color;
// import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
// import java.lang.Math;
import java.util.Hashtable;

import com.rbnb.utility.ToString;
//EMF 5/18/01: use replacement RegisteredChannel,Time,Channel
//import COM.Creare.RBNB.RegisteredChannel;
//import COM.Creare.RBNB.Time;
//import COM.Creare.RBNB.API.Channel;
import com.rbnb.utility.ByteConvert;
import com.rbnb.utility.KeyValueHash;

//EMF 10/15/04: handle images instead of numeric data
// extends PlotContainer so integrates easily into rbnbPlot,
// but overrides all the methods
public class PlotContainerImage extends PlotContainer {
   private boolean beenPainted=false;
   private Dimension size=null;
   private String plottitle=null;
   private PlotTitle pTitle=null;
   private PlotImage pImage=null;
   private double start=0;
   private double duration=0;
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
   private Environment environment=null;

   // constructor
	public PlotContainerImage(RegChannel rc,int dm,PosDurCubby pdc,Environment e) {
		super();
		plottitle=rc.name;
		displayMode=dm;
		environment=e;
		
		setLayout(new BorderLayout());
		add(pTitle=new PlotTitle(plottitle),BorderLayout.NORTH);
		//EMF 8/19/05: keep plottitle invisible, title now in JInternalFrame
		pTitle.setVisible(false);
		add(pImage=new PlotImage(this),BorderLayout.CENTER);
		
		// JPW 04/27/2005: Don't set the plot invisible in Table mode
		/*
		if (displayMode!=LayoutCubby.PlotMode) {
			pTitle.setVisible(false);
			pImage.setVisible(false);
		}
		*/
	} //end constructor
	
	public synchronized void getConfig(Hashtable ht, String prefix) {
		ht.put(prefix+"name",plottitle);
	}
	
	public synchronized void setConfig(Hashtable ht, String prefix) {
		if (ht.containsKey(prefix+"name")) {
			plottitle=(String)(ht.get(prefix+"name"));
			if (pTitle!=null) pTitle.setTitle(plottitle);
		}
	}
	
	public void setDisplayMode(int dm) {
		displayMode=dm;
		if (displayMode==LayoutCubby.TableMode) {
		        // JPW 04/12/2005: Continue to display the video
			//                 images instead of a blank window
			// pTitle.setVisible(false);
			// pImage.setVisible(false);
			pTitle.setVisible(false); //EMF 8/19/05: hide plottitle
			pImage.setVisible(true);
			//force layout in case size changed
			invalidate();
			validate();
		} else if (displayMode==LayoutCubby.PlotMode) {
			pTitle.setVisible(false); //EMF 8/19/05: hide plottitle
			pImage.setVisible(true);
			//force layout in case size changed
			invalidate();
			validate();
		} else {
			System.err.println("PlotContainerImage.setDisplayMode: unknown mode: "+displayMode);
			repaint();
		}
	}
	
	public Hashtable getParameters() {
		return new Hashtable();
	}
	
	public void setParameters(Hashtable ht) {
	}
	
	public void setAbscissa(Time durT) {
		duration=durT.getDoubleValue();
		pImage.setDuration(duration);
	}
	
	public void setOrdinate(double min, double max, int numLines) {
	}
	
	public void setChannelData(Channel ch, Time startT) {
		start=startT.getDoubleValue();
		beenPainted=false;
		if (ch.numberOfPoints<1 || !ch.isByteArray) {
			pImage.setNoData();
			return;
		}
		
		//just get last image
		byte[][] imdata=ch.getDataByteArray();
		DataTimeStamps dts=ch.getTimeStamp();
		double[] times=dts.getTimesDouble(0,ch.getNumberOfPoints());
		pImage.setData(imdata,times,start);
	}
	
	public void update(Graphics g) {
		paint(g);
	}
	
	public void paint(Graphics g) {
		if (displayMode==LayoutCubby.PlotMode) {
		} else if (displayMode==LayoutCubby.TableMode) {
			//add code here...
		}
		beenPainted=true;
		super.paint(g);
	}
	
	public synchronized boolean hasBeenPainted() {
		//see comment in PlotContainer
		if (displayMode==LayoutCubby.TableMode) repaint();
		return beenPainted;
	}
	
} //end class PlotContainerImage

