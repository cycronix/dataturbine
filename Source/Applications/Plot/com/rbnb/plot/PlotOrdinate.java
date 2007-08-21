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

import java.awt.Color;
// import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JComponent;

// JPW 04/08/2005 Convert to Swing; replace Container with JComponent

// WHF 05/15/2002  Replaced references to Color.black with system color.

// public class PlotOrdinate extends Container {
public class PlotOrdinate extends JComponent {
   private PlotAbscissa pTitle=null; //sibling component
   private Dimension oldSize = new Dimension(0,0);
   private String plottitle;
   private double ordMin=-1;
   private double ordMax=1;
   private int ordPow=0;
   private int numLines=1;
   private Image bufferImage=null;
   private boolean newLimits=true;
   //EMF 4/10/07: use Environment font so size can be changed
   //private Font f=new Font("Dialog",Font.PLAIN,10);
   private Font f=Environment.FONT10;
   private FontMetrics fm=null;
   private int fascent=5;
   private int fadvance=20;

   public PlotOrdinate(PlotAbscissa pt) {
      pTitle=pt;
		}
   
   public void setOrdinate(double min, double max, int num) {
      double minPow,maxPow,pow;
      String label=null;
      if (min==max) {
	 System.out.println("PlotOrdinate: zero range");
	 synchronized(this) {
	    numLines=0;
	    ordMin=min;
	    ordMax=max+1;
	    newLimits=true;
	    }
	 repaint();
	 return;
	 }
      if (min==0) pow=Math.log(Math.abs(max))/Math.log(10);
      else if (max==0) pow=Math.log(Math.abs(min))/Math.log(10);
      else {
	 minPow=Math.log(Math.abs(min))/Math.log(10);
	 maxPow=Math.log(Math.abs(max))/Math.log(10);
	 pow=Math.max(minPow,maxPow);
	 }
      //System.out.println("PlotOrdinate.setOrdinate: min "+min+
	 //"  max "+max+" pow "+pow);
      if (pow<-12) {
	 label=new String("E"+Math.ceil(pow)+" ");
	 min = min/Math.pow(10,Math.ceil(pow));
	 max = max/Math.pow(10,Math.ceil(pow));
	 }
      else if (pow<-9) {
	 label=new String("pico ");
	 min = min/1e-12;
	 max = max/1e-12;
	 }
      else if (pow<-6) {
	 label = new String("nano ");
	 min = min/1e-9;
	 max = max/1e-9;
	 }
      else if (pow<-3) {
	 label = new String("micro ");
	 min = min/1e-6;
	 max = max/1e-6;
	 }
      else if (pow<0) {
	 label = new String("milli ");
	 min = min/1e-3;
	 max = max/1e-3;
	 }
      else if (pow<3) {
	 label = new String(" ");
	 min = min;
	 max = max;
	 }
      else if (pow<6) {
	 label = new String("kilo ");
	 min = min/1e3;
	 max = max/1e3;
	 }
      else if (pow<9) {
	 label = new String("mega ");
	 min = min/1e6;
	 max = max/1e6;
	 }
      else if (pow<12) {
	 label = new String("giga ");
	 min = min/1e9;
	 max = max/1e9;
	 }
      else if (pow<15) {
	 label = new String("tera ");
	 min = min/1e12;
	 max = max/1e12;
	 }
      else {
	 label = new String("E"+Math.floor(pow)+" ");
	 min = min/Math.pow(10,Math.floor(pow));
	 max = max/Math.pow(10,Math.floor(pow));
	 }
      pTitle.setOrdScale(label);
      //System.out.println("PlotOrdinate:setOrdinate: label "+label+"  min:"+
	 //ordMin+"  max:"+ordMax);
      synchronized(this) {
	 numLines=num;
	 newLimits=true;
	 ordMin=min;
	 ordMax=max;
	 }
      repaint();
      }
   
   public void paint(Graphics g) {
      boolean newSize=false;
      boolean lLimits;
      double min,max;
      int lLines;
      
      synchronized(this) {
	 lLimits=newLimits;
	 lLines=numLines;
	 newLimits=false;
	 min=ordMin;
	 max=ordMax;
	 }
      Dimension size=getSize();
      if (size.width!=oldSize.width || size.height!=oldSize.height)
			newSize=true;
		if (size.width<=0 || size.height<=0) {
			super.paint(g);
			return;
			}
      if (newSize || lLimits) { //need to recalculate
	 if (newSize) {
	    bufferImage=createImage(size.width,size.height);
	    oldSize.width=size.width;
	    oldSize.height=size.height;
	    }
	 Graphics bi=bufferImage.getGraphics();
	 bi.setFont(f);
//	 bi.setColor(Color.black);
	bi.setColor(java.awt.SystemColor.textText);
	 bi.clearRect(0,0,size.width,size.height);
	 String s=Double.toString(Math.rint(min*1000)/1000);
	 if (min<0) s=s.substring(0,Math.min(s.length(),7));
	 else s=s.substring(0,Math.min(s.length(),6));
	 //System.out.print("PlotOrdinate.paint ordMin string: "+s);
	 int advance=fm.stringWidth(s);
	 bi.drawString(s,size.width-advance,size.height);
	 s=Double.toString(Math.rint(max*1000)/1000);
	 if (max<0) s=s.substring(0,Math.min(s.length(),7));
	 else s=s.substring(0,Math.min(s.length(),6));
	 //System.out.println("   ordMax string: "+s);
	 advance=fm.stringWidth(s);
	 bi.drawString(s,size.width-advance,fascent);
	 double increment=(max-min)/numLines;
	 for (int i=1;i<lLines;i++) {
	   s=Double.toString(Math.rint((min+i*increment)*1000)/1000);
	   if (min+i*increment<0) s=s.substring(0,Math.min(s.length(),7));
	   else s=s.substring(0,Math.min(s.length(),6));
	   advance=fm.stringWidth(s);
	   bi.drawString(s,size.width-advance,(numLines-i)*size.height/numLines + fascent/2);
	   }
	 bi.setColor(Color.white);
	 bi.drawLine(0,0,0,size.height-1);
	 bi.dispose();
	 //newSize=false;
	 }
      g.drawImage(bufferImage,0,0,null);
      //System.out.println("PlotOrdinate.paint(end) ordMin "+ordMin+"   ordMax "
      //   +ordMax);
      super.paint(g);
      }

   public Dimension getMinimumSize() {
      fm=getFontMetrics(f);
      fascent=fm.getAscent();
      fadvance=fm.stringWidth("-0.2345");
      return new Dimension(fadvance,0);
   }

   public Dimension getPreferredSize() {
      return getMinimumSize();
   }

} //end PlotOrdinate class
