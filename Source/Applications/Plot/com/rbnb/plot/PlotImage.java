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
  ***   Name : PlotImage.java    ()                              ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : NeesScan                                          ***
  ***   Date : October 2004                                    ***
  ***                                                           ***
  ***   Copyright 2004 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : handles the graphics for one image,   ***
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

import java.awt.Color;
// import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
// import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JFrame;

// JPW 04/08/2005: Convert to Swing; replace Container with JComponent

// public class PlotImage extends Container {
public class PlotImage extends JComponent {
   private Dimension oldSize = new Dimension(0,0);
   private double start=0;
   private double duration=0;
   private boolean newDuration=false;
   private byte[][] data=null; //raw data in
   private boolean newData=false;
   private double[] time=null; //raw times in
   private Image[] im=null; //images to plot, may be decimated from data
   private double[] ti=null; //times of images to plot
   private Image imnew=null; //newest image
   private double tinew=0;
   //private Image imold=null; //oldest image
   //private double tiold=0;
   private Image bufferImage=null;
   private boolean firstData=true;
   private Color background=new Color(0,0,0);
   private Toolkit tk=null;
   private int w=0; //width to plot images
   private double tf=0; //first time for decimation lineup
   private double tl=0; //last time for decimation lineup
   private int jump=0; //decimation factor
   private int minWidth=100;
   private int maxNumImages=16;

/*
  *****************************************************************
  ***                                                           ***
  ***   Name : PlotImage         (constructor)                   ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : NeesScan                                          ***
  ***   Date :                                                  ***
  ***                                                           ***
  ***   Copyright 2004 Creare Inc.                              ***
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
   // public PlotImage(Container parent) {
   public PlotImage(JComponent parent) {
		setNoData();
		addMouseListener(new MouseTrack(this));
      }
	
	//addNotify - override, since 'this' is null until peer is created
	//EMF 8/25/05: eliminate method; change MouseTrack to find ancestor
	//             frame during first callback, after everything is created
	//             and construct MouseTrack in PlotImage constructor
	//public void addNotify() {
		//super.addNotify();
		//addMouseListener(new MouseTrack(this));
		//}
	
	public synchronized Hashtable getParameters() {
		Hashtable ht=new Hashtable();
		ht.put("minWidth",new Integer(minWidth));
		ht.put("maxNumImages",new Integer(maxNumImages));
		return ht;
	}
	
	public synchronized void setParameters(Hashtable ht) {
		if (ht.containsKey("minWidth")) minWidth=((Integer)ht.get("minWidth")).intValue();
		if (minWidth<1) minWidth=1;
		if (ht.containsKey("maxNumImages")) maxNumImages=((Integer)ht.get("maxNumImages")).intValue();
		if (maxNumImages<1) maxNumImages=1;
		newData=true;	//force relayout of images
		newDuration=true;
	}
	
   public synchronized void setDuration(double durationI) {
	   duration=durationI;
	   newDuration=true;
   }
   
   public synchronized void setNoData() {
	  data=null;
	  time=null;
	  newData=true;
      repaint();
   }

   public synchronized void setData(byte[][] in,double[] timeI,double startI) {
	   start=startI;
	   time=timeI;
	   data=in;
	   newData=true;
	   repaint();
   }
   
   public void update(Graphics g) {
      paint(g);
   }

	public void paint(Graphics g) {
		Dimension size=getSize();
		boolean newSize = false;
		if (size.width<=0 || size.height<=0) {
			super.paint(g);
			return;
		}
		if (size.width!=oldSize.width || size.height!=oldSize.height) {
			newSize=true;
			oldSize.width=size.width;
			oldSize.height=size.height;
		}
		
		synchronized(this) {
			if (newSize||newData||newDuration) {
//if (time!=null) System.err.println("PlotImage.paint: duration="+duration+", data extends "+(time[time.length-1]-time[0]));
//System.err.println("PlotImage.paint: size.width="+size.width);
				if (data==null || time==null) {
					bufferImage=createImage(size.width,size.height);
					Graphics bi=bufferImage.getGraphics();
					bi.setColor(background);
					bi.fillRect(0,0,size.width-1,size.height-1);
					bi.dispose();
					newData=false;
				} else {
					if (newSize || newDuration) { //allow image size to change
						w=size.width;
						tf=tl=0; //reset decimation lineup
						jump=1;
					}
					if (newSize) bufferImage = createImage(size.width,size.height);
					//get graphics panel, start painting
					Graphics bi = bufferImage.getGraphics();
					bi.setColor(background);
					bi.fillRect(0,0,size.width-1,size.height-1);
					//decimate (if needed) and convert images
					if (true) {
					//if (newData) {
						//find images within ploting range, decimate if too many
				/*		int idx1=0;
						int idx2=time.length-1;
						for (int i=0;i<time.length;i++) {
							if (time[i]>=start) {
								idx1=i;
								break;
							}
						}
						for (int i=time.length-1;i>=0;i--) {
							if (time[i]<=start+duration+1e-6) {
								idx2=i;
								break;
							}
						}
						//maintain minWidth image size; decimate if needed
						if (jump==1 && idx1!=idx2)
							while ((jump*size.width/(idx2-idx1) < minWidth)) jump*=2;
						//align with previous decimation if possible
						if (jump>1 && tf>0 && tl>0) {
							for (int i=idx1;i<=idx2;i++) {
								if (Math.abs(time[i]-tf)<1e-6 || Math.abs(time[i]-tl)<1e-6) {
									//found a match, find newest image even hop from there
									while (i <= idx2-jump) i+=jump;
									idx2=i;
									while (i >= idx1+jump) i-=jump; //also find oldest
									idx1=i;
									break;
								}
							}
						}
						int num=0;
						for (int i=idx1;i<=idx2;i+=jump) num++; //number of images
						if (num>maxNumImages) {  //alternate
							num=maxNumImages;
							idx1=idx2 - jump*(num-1);
						}
						boolean showNewest=false;
						if (idx2<time.length-1) { //add if newest image not included
							showNewest=true;;
							idx1+=jump;
						}
				*/
						int idx1=0;
						int idx2=time.length-1;
						boolean showNewest=false;
						//boolean showOldest=false;
						boolean showSome=false;
						int num=time.length;
						if (maxNumImages==1) {
							showNewest=true;
							//imold=null;
							im=null;
						//} else if (maxNumImages==2 && data.length>1) {
							//showNewest=true;
							//showOldest=true;
							//im=null;
						} else { 
							showSome=true;
							//imold=null;
							imnew=null;
							//reality check if data changed too much
							if (num/jump>maxNumImages || jump*size.width/num<minWidth) {
//System.err.println("forcing reset - data changed too much");
								jump=1;
								tf=0;
								tl=0;
							}
							if (jump==1) while (num/jump>maxNumImages ||
												jump*size.width/num<minWidth) jump++;
//System.err.println("PlotImage.paint: num="+num+", jump="+jump);
							//align with previous decimation if possible
							if (jump>1 && tf>0 && tl>0) {
								for (int i=idx1;i<=idx2;i++) {
									if (Math.abs(time[i]-tf)<1e-6 || Math.abs(time[i]-tl)<1e-6) {
										//found a match, find newest image even hop from there
										while (i <= idx2-jump) i+=jump;
										idx2=i;
										while (i >= idx1+jump) i-=jump; //also find oldest
										idx1=i;
										break;
									}
								}
							}
							num=0;
							for (int i=idx1;i<=idx2;i+=jump) num++;
//System.err.println("PlotImage.paint: planning "+num+" images");
						}
						if (Math.abs(time[time.length-1]-start-duration)<1e-6) showNewest=true;
						//EMF 9/15/05: handle 0 duration
						if (duration==0) showNewest=true;
						
						if (tk==null) tk=getToolkit();
						MediaTracker mt=new MediaTracker(this);
						if (showSome) {
							im=new Image[num];
							ti=new double[num];
							for (int i=idx1,j=0;i<=idx2;i+=jump,j++) {
								im[j]=tk.createImage(data[i]);
								mt.addImage(im[j],0);
								ti[j]=time[i];
							}
							tf=ti[0];
							tl=ti[num-1];
//							System.err.println("showSome, num: "+num+", ti[0]: "+ti[0]);
						}
						//if (showOldest) {
							//imold=tk.createImage(data[0]);
							//mt.addImage(imold,0);
							//tiold=time[0];
						//}
						if (showNewest) { //add newest image if needed
							imnew=tk.createImage(data[time.length-1]);
							mt.addImage(imnew,0);
							tinew=time[time.length-1];
						}
						//wait for all images to be created
						try {
							mt.waitForID(0);
						} catch (Exception e) {
							System.err.println("PlotImage.paint: image wait exception");
							e.printStackTrace();
						}
					} //end if newData
					//plot images, spread across available space
					//identify pixel locations of images
					int hi=0; //height and width of images in pixels
					int wi=0;
					int h=size.height;
					if (im!=null) {
						int[] tpix=new int[ti.length];
						for (int i=0;i<ti.length;i++) {
							tpix[i]=(int)((ti[i]-start)*size.width/duration);
	//System.err.println("tpix["+i+"] = "+tpix[i]+", start: "+start);
							if(tpix[i] < 0) tpix[i] = 0;  // mjm 11/5/07:  keep in range, causes infinite loop in drawImage if negative
						}
	//System.err.println("width = "+size.width+", im.length: "+im.length);
						if (w>size.width/im.length) w=size.width/im.length;
						for (int i=0;i<im.length;i++) {
							//preserve aspect ratio
							//int w=(int)(1.0*im[i].getWidth(this)*h/im[i].getHeight(this));
							//if images were evenly spaced, have room for them with no overlap
							hi=(int)(1.0*im[i].getHeight(this)*w/im[i].getWidth(this));
							wi=w;
							if (h<hi) { //image too tall, make it narrower
								hi=h;
								wi=(int)(1.0*im[i].getWidth(this)*hi/im[i].getHeight(this));
							}
//							System.err.println("<drawImage, i: "+i+", tpix[i]:"+tpix[i]+", wi: "+wi+",h: "+h+", hi: "+hi);
							bi.drawImage(im[i],tpix[i]-wi,(h-hi)/2,wi,hi,null);
//							System.err.println(">drawImage");
						}
					}
					//if (imold!=null) {
						//hi=(int)(1.0*imold.getHeight(this)*w/imold.getWidth(this));
						//if (w>size.width/2) w=size.width/2;
						//wi=w;
						//if (h<hi) { //image too tall, make it narrower
							//hi=h;
							//wi=(int)(1.0*imold.getWidth(this)*hi/imold.getHeight(this));
						//}
						//bi.drawImage(imold,0,(h-hi)/2,wi,hi,null);
					//}
					if (imnew!=null) {
						hi=(int)(1.0*imnew.getHeight(this)*w/imnew.getWidth(this));
						if (w>size.width) w=size.width;
						wi=w;
						if (h<hi) { //image too tall, make it narrower
							hi=h;
							wi=(int)(1.0*imnew.getWidth(this)*hi/imnew.getHeight(this));
						}
						bi.drawImage(imnew,size.width-wi,(h-hi)/2,wi,hi,null);
					}
					//bi.drawImage(im[i],(iw+4)*j,0,iw,h,null);
					bi.dispose();
					newData=false;
					newSize=false;
					newDuration=false;
				}
			} //end if new
		} //end synchronized(this)
		if (bufferImage!=null) g.drawImage(bufferImage,0,0,null);
		super.paint(g);
      } //end paint

	//MouseTrack class - tracks mouse location, changes cursor when over plot area as hint
	//to user that clicking here will do something
	public class MouseTrack extends MouseAdapter {
		PlotImage pImage=null;
		JFrame frame=null;
		Cursor pointer=new Cursor(Cursor.DEFAULT_CURSOR);
		Cursor hand=new Cursor(Cursor.HAND_CURSOR);
		boolean isRunning=false;
		
		public MouseTrack(PlotImage pi) {
			pImage=pi;
			//Container c = (Container)pi;
			
			//while ((c=c.getParent())!=null)
				//if (c instanceof JFrame) frame=(JFrame)c;
		}
		
		public void mouseEntered(MouseEvent me) {
			setCursor(hand);
		}
		
		public void mouseExited(MouseEvent me) {
			setCursor(pointer);
		}
		
		public void mouseReleased(MouseEvent me) {
			if (isRunning) {
				return;
			} else {
				if (frame==null) {
					Container c=(Container)pImage;
					while ((c=c.getParent())!=null)
						if (c instanceof JFrame) frame=(JFrame)c;
				}
				isRunning=true;
				Hashtable ht=pImage.getParameters();
				ImageDialog id=new ImageDialog(frame,ht,pImage);
				// id.show();
				// id.dispose();
				id.setVisible(true);
				if (id.proceed) pImage.setParameters(ht);
				isRunning=false;
				}
		}
	} //end class MouseTrack

} //end class PlotImage
