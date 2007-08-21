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
  ***   Name : PlotArea.java    ()                              ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : December 1997                                    ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : handles the graphics for one plot area,   ***
  ***                 exclusive of axes.                        ***
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

import java.util.Hashtable;
import java.awt.Color;
//import java.awt.Component;
import java.awt.Cursor;
import java.awt.Container;
import java.awt.Dimension;
//import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

//EMF 5/18/01: use replacement Time
//import COM.Creare.RBNB.Time;

//JPW -4/08/2005: Convert to Swing

//public class PlotArea extends Container {
public class PlotArea extends JComponent {
	private PlotContainer pCont=null; //parent container
   private Dimension oldSize = new Dimension(0,0);
   //private float absMin=0;	// default data range
   //private float absMax=99;
   private float duration=(float)0.1;
   private float ordMin= -1;
   private float ordMax=1;
   private float absScale=1;	// default plot scaling factors
   private float absOffset=0;
   private float ordScale=1;
   private float ordOffset=0;
   private int numHorizGridLines=1;
   private boolean newScale=false;
   private double[] absDoubleIn=null; //data to be plotted
   private double start=0;
   private double[] ordDoubleIn=null;
   private float[] ordFloatIn=null;
   //EMF 9/12/00: add support for ints
   private int[] ordIntIn=null;
   private short[] ordShortIn=null;
   private boolean newData=false;   //
   private int ordMode=PlotContainer.FloatData;
   private int[] absData=null;
   private int[] ordData=null;
   private Image bufferImage=null;
   private Color background = new Color(0,0,75);
   private Color gridline = new Color(255,100,100);
   private Color dataline = new Color(255,255,255);
   //EMF 2/25/02: added support for moving gridlines
   //             (fixing to data, rather than to screen)
   private double firstGridLine=start;
   private boolean firstData=true;
   private boolean scrollGrid=false;
   //for zooming
	boolean isDragging=false;
	Point dragStart=null;
	Point dragEnd=null;

/*
  *****************************************************************
  ***                                                           ***
  ***   Name : PlotArea         (constructor)                   ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date :                                                  ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : creates a new plot area                   ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns : instance of PlotArea class                    ***
  ***                                                           ***
  *****************************************************************
*/
   public PlotArea(PlotContainer pc,boolean scrollGridI) {
		pCont=pc;
        scrollGrid=scrollGridI;
		setNoData();
		MouseTrack mt=new MouseTrack(pCont);
		addMouseListener(mt);
		//EMF 4/7/05: for dragging to zoom in on a region
		addMouseMotionListener(mt);
      }
	
	//addNotify - override, since 'this' is null until peer is created
	//EMF 8/22/05: eliminate method; change MouseTract to find ancestor
	//             frame during first callback, after everything is created,
	//             and construct MouseTrack in PlotArea constructor
	/*public void addNotify() {
		super.addNotify();
		MouseTrack mt=new MouseTrack(pCont);
System.err.println("PlotArea.addNotify(): "+pCont.plottitle+" created "+mt);
		addMouseListener(mt);
		//EMF 4/7/05: for dragging to zoom in on a region
		addMouseMotionListener(mt);
		} */

   public void setAbscissa(Time dur) {
      synchronized(this) {
			duration=dur.getFloatValue();
			newScale=true;
			}
      repaint();
      }

   public void setOrdinate(double min, double max, int numlines) {
      synchronized(this) {
	 if (min==max) {
	    System.out.println("PlotArea: zero range");
	    max=max+1;
	    }

	 // INB 08/17/2000
	 // Added these checks to eliminate potential overflow problems.
	 ordMin=(float)min;
	 if (Math.abs(min - max) < 1.0e-34) {
	   if (Math.abs(ordMin) > 1.) {
	     ordMax = ordMin + (float) 1.;
	   } else if (Math.abs(ordMin) > 1.e-6) {
	     ordMax = ordMin + (float) 1.e-6;
	   } else if (Math.abs(ordMin) > 1.e-13) {
	     ordMax = ordMin + (float) 1.e-13;
	   } else if (Math.abs(ordMin) > 1.e-20) {
	     ordMax = ordMin + (float) 1.e-20;
	   } else if (Math.abs(ordMin) > 1.e-27) {
	     ordMax = ordMin + (float) 1.e-27;
	   } else {
	     ordMax = ordMin + (float) 1.e-34;
	   }
	 } else {
	   ordMax=(float)max;
	 }
	 numHorizGridLines=numlines;
	 newScale=true;
	 }
      repaint();
      }
      
   public void setNoData() {
      synchronized(this) {
	 absDoubleIn=null;
	 ordShortIn=null;
         //EMF 9/12/00: add support for int data
         ordIntIn=null;
	 ordFloatIn=null;
	 ordDoubleIn=null;
	 absData=null;
	 ordData=null;
	 newData=true;
	 }
      repaint();
      }

   public void setShortData(double[] absIn, short[] ordIn,double st) {
      synchronized(this) {
	 absDoubleIn=absIn;
	 ordShortIn=ordIn;
	 start=st;
         if (firstData || !scrollGrid) {
           firstGridLine=start;
           firstData=false;
         }
	 newData=true;
	 ordMode=PlotContainer.ShortData;
	 }
      repaint();
      }

   //EMF 9/12/00: add support for integers
   public void setIntData(double[] absIn, int[] ordIn,double st) {
      synchronized(this) {
	 absDoubleIn=absIn;
	 ordIntIn=ordIn;
	 start=st;
         if (firstData || !scrollGrid) {
           firstGridLine=start;
           firstData=false;
         }
	 newData=true;
	 ordMode=PlotContainer.IntData;
	 }
      repaint();
      }

   public void setFloatData(double[] absIn, float[] ordIn,double st) {
      synchronized(this) {
	 absDoubleIn=absIn;
	 ordFloatIn=ordIn;
	 start=st;
         if (firstData || !scrollGrid) {
           firstGridLine=start;
           firstData=false;
         }
	 newData=true;
	 ordMode=PlotContainer.FloatData;
	 }
      repaint();
      }

   public void setDoubleData(double[] absIn,double[] ordIn,double st) {
      synchronized(this) {
	 absDoubleIn=absIn;
	 ordDoubleIn=ordIn;
	 start=st;
         if (firstData || !scrollGrid) {
           firstGridLine=start;
           firstData=false;
         }
	 newData=true;
	 ordMode=PlotContainer.DoubleData;
	 }
      repaint();
      }
   
   public void update(Graphics g) {
      System.out.println("PlotArea.update()");
      paint(g);
      }

   public void paint(Graphics g) {
      Dimension size=getSize();
      boolean newSize = false;
      if (size.width!=oldSize.width || size.height!=oldSize.height)
			newSize=true;
		if (size.width<=0 || size.height<=0) {
			super.paint(g);
			return;
			}
      synchronized(this) {
      if (newSize || newScale || newData || isDragging) {	// need to redraw
	 if (newSize) bufferImage = createImage(size.width,size.height);
         Graphics bi = bufferImage.getGraphics();
         bi.setColor(background);
         //bi.fillRect(1,1,size.width-3,size.height-3);
         bi.fillRect(0,0,size.width-1,size.height-1);
         bi.setColor(gridline);
         bi.drawRect(0,0,size.width-1,size.height-1);
         //EMF 2/26/02: scroll gridlines with data
         //for (int i=1;i<10;i++) {  // draw vertical gridlines
            //bi.drawLine(i*size.width/10,1,i*size.width/10,size.height-2);
	    //}
         int G=(int)((firstGridLine-start)*(size.width-1)/duration);
         G=G%(size.width/10);
         if (G<0) G+=size.width/10;
         firstGridLine=G*duration/(size.width-1) + start;
         for (int i=0;i<10;i++) {
           bi.drawLine(G+i*size.width/10,1,G+i*size.width/10,size.height-2);
         }
	 for (int i=1;i<numHorizGridLines;i++) {  // draw horizontal gridlines
            bi.drawLine(1,i*size.height/numHorizGridLines,
			size.width-2,i*size.height/numHorizGridLines);
	    }
         bi.setColor(dataline);
	 if (newSize || newScale) {
            //absScale=(1-size.width)/(absMin-absMax);
            //absOffset=absMin*(1-size.width)/(absMin-absMax);
            absScale=(size.width-1)/duration;
            //absOffset=(float)start*(size.width-1)/duration;
	    absOffset=0; //start subtracted below
	    ordScale=(size.height-1)/(ordMin-ordMax);
            ordOffset=ordMax*(1-size.height)/(ordMin-ordMax);
	    oldSize.height=size.height;
	    oldSize.width=size.width;
//System.out.println("PlotArea.paint: width="+size.width+"   start="+start+"   duration="
//   +duration+"   absScale="+absScale+"   absOffset="+absOffset);
	    }
		if (newData || newSize || newScale) {
	 if (ordMode==PlotContainer.DoubleData && ordDoubleIn!=null) {
	    if (ordData==null || ordData.length!=ordDoubleIn.length) {
	       absData=new int[ordDoubleIn.length];
	       ordData=new int[ordDoubleIn.length];
	       }
	    for (int i=0;i<ordDoubleIn.length;i++) {
	       absData[i]=(int)((absDoubleIn[i]-start)*absScale + absOffset);
	       ordData[i]=(int)(ordDoubleIn[i]*ordScale + ordOffset);
	       }
	    }
	 else if (ordMode==PlotContainer.FloatData && ordFloatIn!=null) {
	    if (ordData==null || ordData.length!=ordFloatIn.length) {
	       absData=new int[ordFloatIn.length];
	       ordData=new int[ordFloatIn.length];
	       }
	    for (int i=0;i<ordFloatIn.length;i++) {
	       absData[i]=(int)((absDoubleIn[i]-start)*absScale + absOffset);
	       ordData[i]=(int)(ordFloatIn[i]*ordScale + ordOffset);
	       }
	    }
         //EMF 9/12/00: add support for int data
	 else if (ordMode==PlotContainer.IntData && ordIntIn!=null) {
	    if (ordData==null || ordData.length!=ordIntIn.length) {
	       absData=new int[ordIntIn.length];
	       ordData=new int[ordIntIn.length];
	       }
	    for (int i=0;i<ordIntIn.length;i++) {
	       absData[i]=(int)((absDoubleIn[i]-start)*absScale + absOffset);
	       ordData[i]=(int)(ordIntIn[i]*ordScale + ordOffset);
	       }
	    }
	 else if (ordMode==PlotContainer.ShortData && ordShortIn!=null) {
	    if (ordData==null || ordData.length!=ordShortIn.length) {
	       absData=new int[ordShortIn.length];
	       ordData=new int[ordShortIn.length];
	       }
	    for (int i=0;i<ordShortIn.length;i++) {
	       absData[i]=(int)((absDoubleIn[i]-start)*absScale + absOffset);
	       ordData[i]=(int)(ordShortIn[i]*ordScale + ordOffset);
	       }
	    }
		}
         if (absData!=null) { //draw polyLine, or horizontal line if only one point
				//solaris/jdk1.1 only draws the first 64k points in the vector...

				if (absData.length>1) {
					//EMF 6/2/00: lockup problem
					//flyscan (NT4 running jdk1.2.2) with v1.1b7 occasionally never returns from polyline - tends to happen when starting many plots at once with the -r command line option
					bi.drawPolyline(absData,ordData,absData.length);
					}
				else bi.drawLine(0,ordData[0],size.width-1,ordData[0]);
				}
	 if (isDragging) { //paint vertical lines to show drag positions
		 bi.setColor(Color.YELLOW);
		 bi.drawLine(dragStart.x,0,dragStart.x,oldSize.height-1);
		 bi.drawLine(dragEnd.x,0,dragEnd.x,oldSize.height-1);
	 }
	 bi.dispose();
	 newScale=false;
	 newData=false;
	 }
	 } //end synchronized(this)
      g.drawImage(bufferImage,0,0,null);
      super.paint(g);
      }

	//MouseTrack class - tracks mouse location, changes cursor when over plot area as hint
	//to user that clicking here will do something
	//EMF 4/7/05: added mousemotion stuff to handle dragging to zoom
	public class MouseTrack extends MouseAdapter implements MouseMotionListener {
		PlotContainer pCont=null;
		JFrame frame=null;
		Cursor pointer=new Cursor(Cursor.DEFAULT_CURSOR);
		Cursor hand=new Cursor(Cursor.HAND_CURSOR);
		boolean isRunning=false;
		
		public MouseTrack(PlotContainer pc) {
			pCont=pc;
			}
			
		public void mouseEntered(MouseEvent me) {
			setCursor(hand);
			}
		
		public void mouseExited(MouseEvent me) {
			setCursor(pointer);
			}
		
		//EMF 4/7/05: change from mouseRelease to mouseClick
		// originally was mouseRelease because bugs on some platforms
		public void mouseClicked(MouseEvent me) {
			if (isRunning) {
				return;
				}
			else {
				if (frame==null) {
					Container c=(Container)pCont;
					while ((c=c.getParent())!=null)
						if (c instanceof JFrame) frame=(JFrame)c;
				}
				isRunning=true;
				Hashtable ht=pCont.getParameters();
				OrdinateDialog od=new OrdinateDialog(frame,ht,pCont);
				//od.show();
				//od.dispose();
				od.setVisible(true);
				if (od.proceed) pCont.setParameters(ht);
				isRunning=false;
				}
			}
			
			public void mousePressed(MouseEvent me) {
				dragStart=me.getPoint();
			}
			
			public void mouseReleased(MouseEvent me) {
				if (isDragging) {
					isDragging=false;
					dragEnd=me.getPoint();
					//convert to times
					int pixStart=0;
					int pixEnd=0;
					if (dragStart.x == dragEnd.x) return;
					if (dragStart.x < dragEnd.x) {
						pixStart=dragStart.x;
						pixEnd=dragEnd.x;
					} else {
						pixStart=dragEnd.x;
						pixEnd=dragStart.x;
					}
					double zstart=(pixStart-absOffset)/absScale;
					double zdur=(pixEnd-absOffset)/absScale - zstart;
					zstart += start;
					pCont.setZoom(zstart,zdur);
					repaint();
				}
			}
			
			public void mouseMoved(MouseEvent me) {}
			
			public void mouseDragged(MouseEvent me) {
				dragEnd=me.getPoint();
				isDragging=true;
				repaint();
			}
		}
		
}
