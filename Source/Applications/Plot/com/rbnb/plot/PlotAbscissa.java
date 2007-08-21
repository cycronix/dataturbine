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

/*
  *****************************************************************
  ***                                                           ***
  ***   Name : PlotAbscissa     ()                              ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : December 1997                                    ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : handles plotting of the abscissa of one   ***
  ***                 plot, using double buffering to eliminate ***
  ***                 flashing                                  ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***                                                           ***
  *****************************************************************
*/

package com.rbnb.plot;

import java.awt.Color;
// import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

//EMF 5/18/01: use replacement time
//import COM.Creare.RBNB.Time;
import com.rbnb.utility.ToString; /* INB 04/09/98 */

// 09/14/2005 EMF Add units/multiplier label here, since PlotTitle no longer displayed
// 04/08/2005  JPW  Convert to Swing; replace Container with JComponent

// 05/15/2002  WHF  Replaced references to Color.black with system color.


// public class PlotAbscissa extends Container {
public class PlotAbscissa extends JComponent {
	private PosDurCubby posDurCubby=null;
   private Dimension oldSize = new Dimension(0,0);
   //EMF 4/10/07: use Environment font so size can be changed
   //private Font f = new Font("Dialog",Font.PLAIN,10);
   private Font f = Environment.FONT10;
   private FontMetrics fm=null;
   private int stringOffset = 0;
	private int ordinateWidth = 0;
   private String axisLabel = null;
   private boolean newAxisLabel = false;
   private Time duration=new Time(0.1);
   //private Time start=null;
   //private String startLabel=new String("");
	private int numpoints=0;
   private String numLabel=// new String(Integer.toString(numpoints)+" pts");
   		new String(" : "+Integer.toString(numpoints)+" pts ");  // whf
   private Image bufferImage=null;
	private String timeLabel = null;
   //EMF 11/9/00
   private Environment environment=null;
   //EMF 9/14/05
   private String ordScale=new String("");
   private boolean newOrdScale=false;
   private String ordUnits=new String("");
   private boolean newOrdUnits=false;

// constructor
   public PlotAbscissa(String al,PosDurCubby pdc,Environment e) {
		posDurCubby=pdc;
      environment=e;
      timeLabel=new String(environment.TIME_LABEL);
      axisLabel = new String(al);
      newAxisLabel = true;
      }

//setAbscissa method - sets plotting range
   public void setAbscissa(Time dur) {
		synchronized(this) {
         duration=dur;
			 int timeFormat=posDurCubby.getTimeFormat();
			 int precision=posDurCubby.getPrecision();
			//	 axisLabel = new String(Double.toString(duration)+" seconds");
			/* INB 04/09/98 */
			try {
				if (timeFormat==Time.AbsoluteSeconds1970 || timeFormat==Time.RelativeSeconds) {
					axisLabel=duration.getFormattedString(Time.RelativeSeconds,Time.Full,precision);
					int firstColon=axisLabel.indexOf(':');
					if (firstColon==-1) axisLabel=axisLabel.trim()+" Sec";
					else {
						int lastColon=axisLabel.lastIndexOf(':');
						if (firstColon==lastColon) axisLabel=axisLabel.trim()+" Min:Sec";
						else axisLabel=axisLabel.trim()+" H:M:S";
						}
					// if string contains 'Day', truncate the rest - it must be zeros
					int day=axisLabel.indexOf("Day");
					if (day!=-1) axisLabel=axisLabel.substring(0,day+4).trim();
					}
				else {
					axisLabel=duration.getFormattedString(timeFormat,Time.Full,precision);
					//axisLabel=axisLabel.trim()+" Sec";
					axisLabel=axisLabel.trim()+timeLabel;
					}
			} catch (Exception e) {
			e.printStackTrace();
			}
			newAxisLabel = true;
			}
		repaint();
		}

//setStartNum method - sets start time and number of points
   public void setStartNum(Time s,int n) {
		//start=s;
      if (numpoints==n) return;
		numpoints=n;
      synchronized(this) {
	 /*try {
	   startLabel=" "+start.getFormattedString(posDurCubby.getTimeFormat(),Time.Full,posDurCubby.getPrecision());
	 } catch (Exception e) {} */
	 //startLabel=new String("testString");
	 numLabel=new String(" : "+Integer.toString(numpoints)+" pts ");
	 newAxisLabel=true;
	 }
      repaint();
      //System.out.println("PlotAbscissa.setStartNum end"); 
    }
	
  //EMF 9/14/05 - copied from PlotTitle.java
   public void setOrdScale(String eu) {
      synchronized(this) {
	 ordScale=new String(eu);
	 newOrdScale=true;
	 }
      repaint();
      }

   //EMF 9/14/05 - copied from PlotTitle.java
   public void setOrdUnits(String un) {
      synchronized(this) {
	 ordUnits=new String(un);
	 newOrdUnits=true;
	 }
      repaint();
      }
// paint method - draws graphics to buffer, blits to screen   
   public void paint(Graphics g) {
      boolean newLabel=false;
      //String sLabel=null,mLabel=null;
		String mLabel=null;
      boolean newSize=false;
	  //EMF 9/14/05: added units/scale
	  boolean newOrd=false;
	  String ordLabel=null;
	synchronized(this) {
	if (newOrdScale||newOrdUnits) newOrd=true;
	else newOrd=false;
	newOrdScale=false;
	newOrdUnits=false;
	ordLabel=new String("  "+ordScale+ordUnits);
	}
		synchronized (this) {
	 newLabel=newAxisLabel;
	 newAxisLabel=false;
	 //sLabel=new String(startLabel);
	 mLabel=new String(axisLabel+numLabel);  
	 //}
	 
      Dimension size=getSize();
      if (size.width!=oldSize.width || size.height!=oldSize.height) newSize=true;
      if (newSize || newLabel || newOrd) {
	 if (newSize) bufferImage=createImage(size.width,size.height);
	 Graphics bi=bufferImage.getGraphics();
	 bi.clearRect(0,0,size.width,size.height);
	 bi.setFont(f);
//	 bi.setColor(Color.black); // whf
	bi.setColor(java.awt.SystemColor.textText);
	//EMF 9/14/05: draw label
	bi.drawString(ordLabel,0,stringOffset);
	 //bi.drawString(sLabel,0,stringOffset);
	 int sWidth=fm.stringWidth(mLabel);
	 //bi.drawString(mLabel,size.width-sWidth,stringOffset);
	 if (size.width-ordinateWidth < sWidth) bi.drawString(mLabel,size.width/2-sWidth/2,stringOffset);
	 else bi.drawString(mLabel,size.width/2-sWidth/2+ordinateWidth/2,stringOffset);
	 bi.drawLine(0,size.height-1,size.width-1,size.height-1);
	 bi.drawLine(size.width-1,0,size.width-1,size.height-1);
	 bi.setColor(Color.white);
	 bi.drawLine(0,0,0,size.height-1);
	 bi.dispose();
	 oldSize.width=size.width;
	 oldSize.height=size.height;
	 }
      g.drawImage(bufferImage,0,0,null);
      } //end synchronize
      super.paint(g);
      }

//getMinimumSize method - returns minimum size in pixels
   public Dimension getMinimumSize() {
      fm = getFontMetrics(f);
      stringOffset = fm.getAscent();
		ordinateWidth = fm.stringWidth("-0.234");
      return new Dimension(0,fm.getDescent()+stringOffset); // 0 indicates don't care
   }

//getPreferredSize method - returns preferred size in pixels
   public Dimension getPreferredSize() {
      Dimension min = getMinimumSize();
      min.setSize(min.width,min.height+4);
      stringOffset += 2;
      return min;
   }

}
