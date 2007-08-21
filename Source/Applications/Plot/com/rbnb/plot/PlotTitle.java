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

//EMF 5/18/01: use replacement RegisteredChannel
//import COM.Creare.RBNB.RegisteredChannel;
import java.awt.Color;
// import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

// 04/08/2005  JPW  Convert to Swing; replace Container with JComponent
// 05/15/2002  WHF  Replaced references to Color.black with system color.

// public class PlotTitle extends Container {
public class PlotTitle extends JComponent {
   private Dimension oldSize = new Dimension(0,0);
   private String plottitle=null;
   private String ordScale=new String("");
   private boolean newOrdScale=false;
   private String ordUnits=new String("");
   private boolean newOrdUnits=false;
   private int stringOffset;
   //EMF 4/10/07: use Environment fonts so size can be changed
   //private Font f = new Font("Dialog",Font.PLAIN,10);
   private Font f = Environment.FONT10;
   private FontMetrics fm = getFontMetrics(f);
   private Image bufferImage = null;

   public PlotTitle(String title) {
      // label only with RCO and channel name, not RBNB
      //plottitle=title.substring(title.lastIndexOf('/')+1);
		//EMF 5/17/99: added pathname to plot title
		//plottitle=RegisteredChannel.rboName(title)+"/"+RegisteredChannel.channel(title);
		//EMF 5/24/99: use full pathname for path title, unless too long (see paint())
		plottitle=title;
      }

  public void setTitle(String title) {
    synchronized(this) {
      plottitle=title;
      newOrdScale=true;
    }
    repaint();
  }

   public void setOrdScale(String eu) {
      synchronized(this) {
	 ordScale=new String(eu);
	 newOrdScale=true;
	 }
      repaint();
      }

   public void setOrdUnits(String un) {
      synchronized(this) {
	 ordUnits=new String(un);
	 newOrdUnits=true;
	 }
      repaint();
      }
      
   public void update(Graphics g) {
     //System.out.println("PlotTitle.update()");
      paint(g);
      }
   
   public void paint(Graphics g) {
      boolean newOrd;
      String ordLabel=null;
      boolean newSize=false;
      
      synchronized(this) {
			if (newOrdScale||newOrdUnits) newOrd=true;
			else newOrd=false;
			newOrdScale=false;
			newOrdUnits=false;
			ordLabel=new String(ordScale+ordUnits);
			}
	 
      Dimension size = getSize();
      if (size.width!=oldSize.width || size.height!=oldSize.height) newSize=true;
      if (newSize||newOrd) {
			if (newSize) bufferImage=createImage(size.width,size.height);
			//if (newSize) bufferImage=new java.awt.image.BufferedImage(size.width,size.height,java.awt.image.BufferedImage.TYPE_INT_RGB);
//System.err.println("PlotTitle.paint: bufferImage "+bufferImage);
			Graphics bi=bufferImage.getGraphics();
			if (!newSize) bi.clearRect(0,0,size.width-1,size.height-1);
			bi.setFont(f);
//			bi.setColor(Color.black);
			bi.setColor(java.awt.SystemColor.textText);
			bi.drawString(ordLabel,0,stringOffset);
			int ordPosition=fm.stringWidth(ordLabel);
			int chanPosition=size.width-fm.stringWidth(plottitle)-4;
			if (chanPosition>100) chanPosition=100;
			if (chanPosition>ordPosition+5) {
				bi.drawString(plottitle,chanPosition,stringOffset);
				}
			else {
				String shortTitle;
				try {
				    shortTitle = RegisteredChannel.rboName(plottitle)+"/"+RegisteredChannel.channel(plottitle);
				} catch (Exception e) {
				    throw new InternalError();
                                }
				chanPosition=size.width-fm.stringWidth(shortTitle)-4;
				if (chanPosition>ordPosition+5) {
					bi.drawString(shortTitle,chanPosition,stringOffset);
					}
				else {
				    try {
					shortTitle=RegisteredChannel.channel(plottitle);
				    } catch (Exception e) {
					throw new InternalError();
				    }
					chanPosition=size.width-fm.stringWidth(shortTitle)-4;
					bi.drawString(shortTitle,chanPosition,stringOffset);
					}
				}
			bi.drawLine(size.width-1,0,size.width-1,size.height-1);
			bi.setColor(Color.white);
			bi.drawLine(0,0,size.width-2,0);
			bi.drawLine(0,0,size.height,0);
			bi.dispose();
			oldSize.width=size.width;
			oldSize.height=size.height;
			}
      g.drawImage(bufferImage,0,0,null);
      super.paint(g);
      }

   public Dimension getMinimumSize() {
      stringOffset=fm.getAscent();
      return new Dimension(0,fm.getDescent()+stringOffset);
   }

   public Dimension getPreferredSize() {
      Dimension min = getMinimumSize();
      //System.out.println("PlotTitle min siz: "+min);
      min.setSize(min.width,min.height+4);
      stringOffset += 2;
      //System.out.println("PlotTitle pref siz: "+min);
      return min;
   }
}
