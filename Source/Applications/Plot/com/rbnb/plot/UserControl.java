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

// UserControl.java - playback buttons and sliders for controlling rbnbPlot
// Eric Friets
// Copyright 1997,1998,1999,2000 Creare Incorporated
// All Rights Reserved
//
// Revisions:
//   04/07/05 JPW - Convert to Swing
//   04/05/05 JPW - Remove use of ChannelDialog; add use of JChannelDialog
//   10/25/00 EMF - pass showStartTime state to RBNBInterface so duration
//                  changes don't change displayed position

package com.rbnb.plot;

// import java.awt.Adjustable;
import java.awt.BorderLayout;
// import java.awt.Button;
// import java.awt.Component;
// import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
// import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
// import java.awt.Label;
// import java.awt.List;
// import java.awt.Scrollbar;
// import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
// import java.awt.event.ItemEvent;
// import java.awt.event.ItemListener;
// import java.lang.String;
// import java.lang.Thread;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

//EMF 5/18/01: use replacement RegisteredChannel,Time
//import COM.Creare.RBNB.RegisteredChannel;
//import COM.Creare.RBNB.Time;
import com.rbnb.utility.ToString; /* INB 04/09/98 */

// public class UserControl extends Container
public class UserControl extends JComponent
                         implements Runnable, ActionListener
{
   
   private Dimension size=null;
   private LWContainer buttons = null;
   private JButton bof=null;
   private JButton revPlay=null;
   private JButton revStep=null;
   private JButton stop=null;
   private JButton fwdStep=null;
   private JButton fwdPlay=null;
   private JButton eof=null;
   private JButton realTime=null;
   private JButton channelButton=null;
   Time[] position=new Time[3];
   private JScrollBar posSlider=null;
   int psval=0;
   private int psblock=100,psunit=10,psmin=0,psmax=1000; //position slider
   FWLabel posText=null; //>Position or <Position text label
   //EMF 3/2/05: change to FWField, so can type in new position
   //FWLabel posLabel=null; //label for current position
   FWField posLabel=null; //label for current position
   FWLabel updateLabel=null; //label for update rate
   //EMF 5/3/01: added raw time label
   FWLabel rawTimeLabel=null; //label for raw time (seconds, usually)
   Time duration=null;
   private JScrollBar durSlider=null;
   int dsval=20;
   private int dsblock=1,dsunit=1,dsmin=0,dsmax=40; //duration slider
   //EMF 4/21/05: change to FWField so can type in values
   //FWLabel durLabel=null; //label for current duration
   FWField durLabel=null; //label for current duration
   //private List channelList = null;
   private RegChannel[] availableChans = null;
   private RunModeCubby runModeCubby = null;
   private LayoutCubby layoutCubby = null;
   private RBNBCubby rbnbCubby = null;
   PosDurCubby posDurCubby = null;
	DurationListener durListener = null;
	private String timeLabel = null;
	private boolean showStartTime = false;
	private int oldTimeFormat = Time.Unspecified;
        // JPW 04/05/2005: Replace ChannelDialog with JChannelDialog
        // private ChannelDialog cd=null;
        // private boolean cdShown=false;
        private JChannelDialog jcd=null;
	private boolean jcdShown=false;
	private static NumberFormat numberformat=NumberFormat.getInstance();
	
   public UserControl(JFrame f,RunModeCubby rmc,LayoutCubby loc,
      RBNBCubby rbc,PosDurCubby pdc,Environment env) {
      
      //EMF 4/10/07: use Environment font so size can be changed
      // JPW 04/12/2005: set the font
      //setFont(new Font("Dialog", Font.PLAIN, 12));
      setFont(Environment.FONT12);
      
      // save cubbyholes
      JFrame frame=f;
      runModeCubby = rmc;
      layoutCubby = loc;
      rbnbCubby = rbc;
      posDurCubby = pdc;
      timeLabel=new String(env.TIME_LABEL); //use local copy since visual cafe
					  //compiler is braindead
      while ((duration = pdc.getDuration(true))==null) {
	   try { Thread.sleep(50); }
	   catch (InterruptedException e) {}
	   }
	//System.out.println("UserControl: duration="+duration.getFloatValue());

	 // create run mode buttons and callback class instances
	 buttons=new LWContainer();
	 bof=new JButton("| <");
	 bof.setFont(Environment.FONT10);
	 bof.addActionListener(new ButtonListener(buttons,bof,runModeCubby,RunModeDefs.bof));
	 bof.setName(Integer.toString(RunModeDefs.bof));
	 revPlay=new JButton("<");
	 revPlay.setFont(Environment.FONT10);
	 revPlay.addActionListener(new ButtonListener(buttons,revPlay,runModeCubby,RunModeDefs.revPlay));
	 revPlay.setName(Integer.toString(RunModeDefs.revPlay));
	 revStep=new JButton("< |");
	 revStep.setFont(Environment.FONT10);
	 revStep.addActionListener(new ButtonListener(buttons,revStep,runModeCubby,RunModeDefs.revStep));
	 revStep.setName(Integer.toString(RunModeDefs.revStep));
	 stop=new JButton("||");
	 stop.setFont(Environment.FONT10);
	 stop.addActionListener(new ButtonListener(buttons,stop,runModeCubby,RunModeDefs.stop));
	 stop.setName(Integer.toString(RunModeDefs.stop));
	 fwdStep=new JButton("| >");
	 fwdStep.setFont(Environment.FONT10);
	 fwdStep.addActionListener(new ButtonListener(buttons,fwdStep,runModeCubby,RunModeDefs.fwdStep));
	 fwdStep.setName(Integer.toString(RunModeDefs.fwdStep));
	 fwdPlay=new JButton(">");
	 fwdPlay.setFont(Environment.FONT10);
	 fwdPlay.addActionListener(new ButtonListener(buttons,fwdPlay,runModeCubby,RunModeDefs.fwdPlay));
	 fwdPlay.setName(Integer.toString(RunModeDefs.fwdPlay));
	 eof=new JButton("> |");
	 eof.setFont(Environment.FONT10);
	 eof.addActionListener(new ButtonListener(buttons,eof,runModeCubby,RunModeDefs.eof));
	 eof.setName(Integer.toString(RunModeDefs.eof));
	 realTime=new JButton("RT");
	 realTime.setFont(Environment.FONT10);
	 realTime.addActionListener(new ButtonListener(buttons,realTime,runModeCubby,RunModeDefs.realTime));
	 realTime.setName(Integer.toString(RunModeDefs.realTime));
	 posSlider=new JScrollBar(JScrollBar.HORIZONTAL,psval,psblock,psmin,psmax);
	 posSlider.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
	 posSlider.addAdjustmentListener(new PositionListener());
 
         //EMF 12/30/03: check environment, adjust dsmax if needed
	 try {
           if (env.MAXDURATION!=null) {
             double maxdur=env.MAXDURATION.getDoubleValue();
             double startdur=duration.getDoubleValue();
             int dsdelta=(int)(3*(Math.log(maxdur)-Math.log(startdur))/Math.log(10));
             if (dsdelta>0) dsmax=dsval+dsdelta+1;
           }
         } catch (Exception e) {
           //e.printStackTrace();
         }

	 durSlider=new JScrollBar(JScrollBar.HORIZONTAL,dsval,dsblock,dsmin,dsmax);
	 //durSlider.setBackground(java.awt.Color.blue);
	 //durSlider.setForeground(java.awt.Color.red);
	 durSlider.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
	 durSlider.setUnitIncrement(dsunit);
	 durListener = new DurationListener();
	 durSlider.addAdjustmentListener(durListener);
	
	 //make channel button
	 channelButton=new JButton("Channels");
	 channelButton.setFont(Environment.FONT10);
	 channelButton.addActionListener(this);
		// create channel list, request channels
      //channelList = new List(6,true);
      //channelList.setFont(new Font("Dialog",Font.PLAIN,10));
      //channelList.add("All");
      //channelList.add("None");
		//size is ignored, at least in 1.1 ... how can its size be changed??
      //channelList.setSize(new Dimension(50,50));
      //ChanListener channelListener = new ChanListener(channelList,rbnbCubby);
      //channelList.addItemListener(this);
      // get available channels, add to list
      while ((availableChans = rbc.getAvailableChannels())==null) {
			//Thread.yield();
         try { Thread.sleep(50); }
         catch (InterruptedException e) {}
			}
		//make channel dialog, but don't show
                // JPW 04/05/2005: Replace ChannelDialog with JChannelDialog
                // cd=new ChannelDialog(frame,rbnbCubby);
                // cd.setAvailableChannels(availableChans);
                jcd=new JChannelDialog(frame,layoutCubby,rbnbCubby);
// System.err.println("UserControl.init: calling setAvailableChannels "+availableChans);
                jcd.setAvailableChannels(availableChans);
	//for (int i=0;i<availableChans.length;i++) {
	 // display data path and channel name, not RBNB
	 //channelList.add(RegisteredChannel.rboName(availableChans[i].name)+"/"+
						 // RegisteredChannel.channel(availableChans[i].name));
	 //channelList.add(availableChans[i].substring(availableChans[i].lastIndexOf('/')+1));
	 //if (i<4) channelList.select(i+2); //autoselect only the first 4 channels
	 //}
      // notify cubby of selected channels
      if (availableChans.length<=4) rbnbCubby.setSelectedChannels(availableChans,true);
      else {
	 RegChannel[] selectedChans=new RegChannel[4];
	 for (int i=0;i<4;i++) selectedChans[i]=availableChans[i];
	 rbnbCubby.setSelectedChannels(selectedChans,true);
	 }
      // lay out User Controls
	//setLayout(new GridLayout(1,0));
	setLayout(new BorderLayout()); //chan list east, buttons/sliders center
	buttons.setLayout(new GridLayout(1,0)); //buttons in single row
	buttons.add(bof);
	buttons.add(revPlay);
	buttons.add(revStep);
	buttons.add(stop);
	buttons.add(fwdStep);
	buttons.add(fwdPlay);
	buttons.add(eof);
	buttons.add(realTime);
	LWContainer sliders = new LWContainer();
	sliders.setLayout(new GridLayout(0,1)); //sliders in single column
	LWContainer posCont = new LWContainer();
	posCont.setLayout(new BorderLayout());
	//posLabel=new FWLabel("","Duration",Label.LEFT);
	//posCont.add(new FWLabel("Position","Duration",Label.LEFT),"West");
	/* INB 04/09/98 */
	try {
	  posLabel=new FWField("",
	    duration.getFormattedString(Time.AbsoluteSeconds1970,Time.Full,-1),
	    SwingConstants.LEFT);
		PositionTextListener ptl=new PositionTextListener();
	    posLabel.addActionListener(ptl);
		posLabel.addFocusListener(ptl);
	    //(ToString.toString("%*8g",0.),"MMMMMMMMM",Label.LEFT);
	} catch (Exception e) {}
        //EMF 5/3/01
	try {
	  rawTimeLabel=new FWLabel("",
	    //duration.getFormattedString(Time.Unspecified,Time.Full,-1),
	    "1234567890  Updates/Sec ",
	    SwingConstants.RIGHT);
	    //(ToString.toString("%*8g",0.),"MMMMMMMMM",Label.LEFT);
	} catch (Exception e) {}
	//posCont.add(new FWLabel("Position","Duration",Label.LEFT),"West");
	updateLabel=new FWLabel("","1234567890 Updates/Sec ",SwingConstants.RIGHT);
	posText=new FWLabel(">Position:",">Duration: ",SwingConstants.LEFT);
	posCont.add(posText,BorderLayout.WEST);
	posCont.add(posLabel,BorderLayout.CENTER);
        posCont.add(rawTimeLabel,BorderLayout.EAST);
	//posCont.add(posSlider,"Center");
	sliders.add(posCont);
	sliders.add(posSlider);
	LWContainer durCont = new LWContainer();
	durCont.setLayout(new BorderLayout());
	//durLabel=new FWLabel("0.1","Duration",Label.LEFT);
	//durCont.add(new FWLabel("Duration","Duration",Label.LEFT),"West");
	/* INB 04/09/98 */
	try {
	  durLabel=new FWField
	    (makeDurLabel(duration),
	    duration.getFormattedString(Time.AbsoluteSeconds1970,Time.Full,-1),
	    SwingConstants.LEFT);
	    //(ToString.toString("%*8g",0.1),"MMMMMMMMM",Label.LEFT);
		DurationTextListener dtl=new DurationTextListener();
		durLabel.addActionListener(dtl);
		durLabel.addFocusListener(dtl);
	} catch (Exception e) {}
	//durCont.add(new FWLabel("Duration","Duration",Label.LEFT),"West");
	durCont.add(new FWLabel("Duration:",">Duration: ",SwingConstants.LEFT),BorderLayout.WEST);
	durCont.add(durLabel,BorderLayout.CENTER);
        //EMF 5/2/01: move updateLabel to durCont from posCont
	durCont.add(updateLabel,BorderLayout.EAST);
	//durCont.add(durSlider,"Center");
	sliders.add(durCont);
	sliders.add(durSlider);
	posSlider.setValues(psval,psblock,psmin,psmax);
	posSlider.setBlockIncrement(psblock);
	posSlider.setUnitIncrement(psunit);
	durSlider.setValues(dsval,dsblock,dsmin,dsmax);
	durSlider.setBlockIncrement(dsblock);
	durSlider.setUnitIncrement(dsunit);
	LWContainer butslid = new LWContainer();
	butslid.setLayout(new BorderLayout());
	butslid.add(sliders,BorderLayout.SOUTH); //sliders south
	butslid.add(buttons,BorderLayout.CENTER); //buttons center
	//add(channelList,"East");
	add(channelButton,BorderLayout.EAST);
	add(butslid,BorderLayout.CENTER);
	//add(butslid);
	//add(channelList);
	// set startup time format
	oldTimeFormat=posDurCubby.getTimeFormat();
	// start thread to check for position/range changes
	Thread ucThread = new Thread(this,"ucThread");
	ucThread.start();
      }
	
	//makeDurLabel method - creates string for duration label based on Time type and duration
	private String makeDurLabel(Time duration) {
		String labelString=null;
		int timeFormat=posDurCubby.getTimeFormat();
		int precision=posDurCubby.getPrecision();
		try {
			if (timeFormat==Time.AbsoluteSeconds1970 || timeFormat==Time.RelativeSeconds) {
				labelString=duration.getFormattedString(Time.RelativeSeconds,Time.Full,precision);
				int firstColon=labelString.indexOf(':');
				if (firstColon==-1) labelString=labelString.trim()+" Sec";
				else {
					int lastColon=labelString.lastIndexOf(':');
					if (firstColon==lastColon) labelString=labelString.trim()+" Min:Sec";
					else labelString=labelString.trim()+" H:M:S";
					}
				// if string contains 'Day', truncate the rest - it must be zeros
				int day=labelString.indexOf("Day");
				if (day!=-1) labelString=labelString.substring(0,day+4).trim();
				}
			else {
				//labelString=duration.getFormattedString(timeFormat,Time.Fine,3).trim()+" Sec";
				labelString=duration.getFormattedString(timeFormat,Time.Fine,3).trim()+timeLabel;
				} 
			} catch (Exception e1) {}
		return labelString;
		}
	
	//makePosTextlabel method - adjusts Position: label as appropriate
	private void makePosTextLabel(int runMode) {
		if (runMode==RunModeDefs.realTime || runMode==RunModeDefs.eof) {
			 showStartTime=false;
			 }
		 else if (runMode==RunModeDefs.bof || runMode==RunModeDefs.revPlay ||
					 runMode==RunModeDefs.revStep || runMode==RunModeDefs.fwdStep ||
					 runMode==RunModeDefs.fwdPlay || runMode==RunModeDefs.allData ) {
			 showStartTime=true;
			 }
		 if (showStartTime) posText.setText("<Position: ");
		 else posText.setText(">Position: ");
  //EMF 10/25/00: pass showStartTime state to RBNBInterface
  posDurCubby.setPositionAtStart(showStartTime);
		 }
		 
    public void clearChannelDialog() {
	// JPW 04/05/2005: Replace ChannelDialog with JChannelDialog
	/*
	if (cd!=null) {
	  //	    cd.hide();
	    cd.setVisible(false);
            //EMF 5/4/01: dispose sometimes never returns; don't do
	    //cd.dispose();
	    cd=null;
	}
	*/
	if (jcd != null) {
	    jcd.setVisible(false);
	    jcd = null;
	}
    }

   public void run() {
      Time[] newPosition=new Time[3];
      Time dur=null;
		String newUpdateRate=null;
	RegChannel[] newChans=null;
	Integer runModeInt = null;
	Boolean streaming=null;
	Time[] zoom=null;
      
      while(true) {
	 try {
	    Thread.sleep(25);  //one more further down - changed if/elseif to just if so updates are more even
	    }
	 catch (InterruptedException e) {
	    System.out.println("ucThread sleep error: "+e);
	    }
	 // start time changes
	 if ((newPosition=posDurCubby.getPosition(true)) != null) {
		 changePosition(newPosition);
	    }
	 if ((dur=posDurCubby.getDuration(true)) != null) {
	    synchronized(this) {
	       duration=dur;
	       durListener.setDurTimeValues();
	       durLabel.setText(makeDurLabel(duration));
	       }
	    }
	 if ((newUpdateRate = posDurCubby.getUpdateRate())!=null) {
		updateLabel.setText(newUpdateRate);
		}
	 //EMF 4/19/05: add check for zoom by dragging region in plot
	 if ((zoom=posDurCubby.getZoom())!=null) {
		 posLabel.requestFocusInWindow();
		 //gross hack to aid timing
		 //try { Thread.currentThread().sleep(20); } catch (Exception e) {}
		 durLabel.setText(zoom[1].toString());
		 durLabel.postActionEvent();
		 posLabel.setText(zoom[0].getFormattedString(
							posDurCubby.getTimeFormat(),Time.Full,posDurCubby.getPrecision()));
		 posLabel.postActionEvent();
	 }
	 try {
		Thread.sleep(25);
		}
	 catch (InterruptedException e) {
	    System.out.println("ucThread sleep error: "+e);
	    }
	 if ((newChans = rbnbCubby.getAvailableChannels())!=null) {
	        // JPW 04/05/2005: Replace ChannelDialog with JChannelDialog
	        // if (cd!=null) cd.setAvailableChannels(newChans);
		if (jcd!=null) {
// System.err.println("UserControl.run: calling setAvailableChannels "+newChans);
			jcd.setAvailableChannels(newChans);
// System.err.println("UserControl.run: returned from setAvailableChannels ");
		}
	   /*synchronized(this) {
	    for (int i=availableChans.length-1;i>=0;i--) channelList.remove(i+2);
	    for (int i=0;i<newChans.length;i++) {
			channelList.add(RegisteredChannel.channel(newChans[i].name),i+2);
			channelList.deselect(i+2);
			}
	    availableChans=newChans;
	    //changeChannels();
	    }*/
	   } //end else if getAvailbleChannels
	 if ((newChans = rbnbCubby.getSelectedChannels(true))!=null) {
	        // JPW 04/05/2005: Replace ChannelDialog with JChannelDialog
	        // cd.setSelectedChannels(newChans);
                jcd.setSelectedChannels(newChans);
	    /*synchronized(this) {
		 for (int i=0;i<channelList.getItemCount();i++)
		    channelList.deselect(i);
		 for (int i=0;i<availableChans.length;i++) {
		    for (int j=0;j<newChans.length;j++) {
			 if (availableChans[i].name.equals(newChans[j].name)) {
			    channelList.select(i+2);
			    channelList.makeVisible(i+2);
			    j=newChans.length;
			    }
			 }
		    }
		 //changeChannels();
		 } */
	    } //end else if getSelectedChannels
	 else if ((runModeInt = runModeCubby.get(true))!=null) {
		 makePosTextLabel(runModeInt.intValue());
		 for (int i=0;i<buttons.getComponentCount();i++) {
			buttons.getComponent(i).setBackground(Environment.BGCOLOR);
			if (buttons.getComponent(i).getName().equals(runModeInt.toString()))
				 buttons.getComponent(i).setBackground(Environment.BGCOLOR.darker());
			}
	    } //end if new runmode
	 else if ((streaming = runModeCubby.getStreaming())!=null) {
		 if (streaming.booleanValue()) realTime.setText("RT*");
		 else realTime.setText("RT");
		 }
	 } //end while(true)      
   } //end run method
   
   //EMF 4/20/05: pull code from run to here, so can be called by PositionListener as well
   private synchronized void changePosition(Time[] newPosition) {
	   position=newPosition;
	   if (position[0].compareTo(position[1])==-1) psval=0;
	   else if (position[0].compareTo(position[2])==1) psval=1000;
	   else psval =
	   (int)Math.round(
			   1000*
		   position[0].subtractTime(position[1]).getDoubleValue()/
			   position[2].subtractTime(position[1]).getDoubleValue());
	   //
	   // JPW 04/08/2005: Don't fire events when updating the value,
	   //                 visible amount, or block increment
	   //
	   //                 NOTE: I can't figure out any other way to
	   //                       set these parameters w/o an
	   //                       AdjustmentEvent being fired
	   //
	   AdjustmentListener[] listeners =
		   posSlider.getAdjustmentListeners();
	   for (int adjIdx = 0; adjIdx < listeners.length; ++adjIdx) {
	   posSlider.removeAdjustmentListener(listeners[adjIdx]);
	   }
	   posSlider.setValue(psval);
	   psblock =
		   (int)(1000*duration.getDoubleValue()/
		   (position[2].subtractTime(position[1]).getDoubleValue()) + 0.5);
	   if (psblock<1) psblock=1;
	   if (psblock>1000) psblock=1000;
	   posSlider.setVisibleAmount(psblock);
	   posSlider.setBlockIncrement(psblock);
	   //
	   // JPW 04/08/2005: Add back the listeners
	   //
	   for (int adjIdx = 0; adjIdx < listeners.length; ++adjIdx) {
	   posSlider.addAdjustmentListener(listeners[adjIdx]);
	   }
	   int timeFormat=posDurCubby.getTimeFormat();
	   if (oldTimeFormat!=timeFormat) {
		   oldTimeFormat=timeFormat;
	   durListener.updateTimeFormat(timeFormat);
	   //if (runMode==RunModeDefs.stop) runModeCubby.set
	   }
	   try {
			 if (showStartTime) posLabel.setText(position[0].getFormattedString(
									 timeFormat,Time.Full,posDurCubby.getPrecision()));
			 else posLabel.setText(position[0].addTime(duration).getFormattedString(
									 timeFormat,Time.Full,posDurCubby.getPrecision()));
							 if (timeFormat!=Time.Unspecified) {
							   if (showStartTime) rawTimeLabel.setText(position[0].getFormattedString(Time.Unspecified,Time.Full,posDurCubby.getPrecision())+ " Sec");
							   else rawTimeLabel.setText(position[0].addTime(duration).getFormattedString(Time.Unspecified,Time.Full,posDurCubby.getPrecision())+" Sec");
							 } else {
							   rawTimeLabel.setText("");
							 }
			 }
		 catch (Exception e) {}
   }

   public void paint(Graphics g) {
      size=getSize();
      g.drawRect(0,0,size.width-1,size.height-1);
      super.paint(g);
   }
   
   // ActionListener implementation
   // EMF 9/7/05: now called by RBNBPlotMain upon successful Open
   public void actionPerformed(ActionEvent e) {
       
       // JPW 09/14/2005: Remove refresh; the JChannelDialog has its own
       //                 refresh button which the user can use if desired
       //EMF 2/5/01: add call to refresh channel list
       // layoutCubby.set(LayoutCubby.RefreshRBNB);
       
       // JPW 04/05/2005: Replace ChannelDialog with JChannelDialog
       /*
       if (cd==null) {
	    System.err.println("ChannelDialog null!");
       }
       else {
	    if (!cdShown) {
		cd.firstShow();
		cdShown=true;
	    }
	    cd.show();
	    cd.toFront();
	    cd.requestFocus();
       }
       */
       if (jcd==null) {
	    System.err.println("JChannelDialog null!");
       }
       else {
	    // JPW 09/14/2005: Always call jcd.firstShow();
	    jcd.firstShow();
	    /*
	    if (!jcdShown) {
		jcd.firstShow();
		jcdShown=true;
	    }
	    jcd.show();
	    jcd.toFront();
	    jcd.requestFocus();
	    */
       }
   }
   
   // ItemListener implementation
   /*public void itemStateChanged(ItemEvent e) {
	synchronized (this) {
	   changeChannels();
	   }
	} */
   
  /* private void changeChannels() {
	  RegChannel[] selectedChans=null;
	  int[] selectedIndices = null;
	  
     if (channelList.isIndexSelected(1)) { // None selected
			channelList.deselect(0);
			channelList.deselect(1);
			selectedIndices=channelList.getSelectedIndexes();
			for (int i=0;i<selectedIndices.length;i++)
				channelList.deselect(selectedIndices[i]);
			selectedChans=new RegChannel[0];
			}
		else if (channelList.isIndexSelected(0)) { // All selected
			channelList.deselect(0);
			channelList.deselect(1);
			int itemCount=channelList.getItemCount();
			for (int i=2;i<itemCount;i++)
				if (!channelList.isIndexSelected(i)) channelList.select(i);
			itemCount=itemCount-2;
			selectedChans=new RegChannel[itemCount];
			for (int i=0;i<itemCount;i++) {
				selectedChans[i]=availableChans[i];
				}
			}
		else {
			selectedIndices=channelList.getSelectedIndexes();
			selectedChans = new RegChannel[selectedIndices.length];
			int j=0;
			for (int i=0;i<selectedIndices.length;i++) {
				selectedChans[j++]=availableChans[selectedIndices[i]-2];
				}
			}
	 rbnbCubby.setSelectedChannels(selectedChans,true);
	} */

// ButtonListener class - handles button clicks, sets appropriate runmode
class ButtonListener implements ActionListener {
   LWContainer lwCont=null;
   JButton btn=null;
   private RunModeCubby runModeCubby = null;
   int runMode = RunModeDefs.stop;

   public ButtonListener(LWContainer lwc,JButton b,RunModeCubby rmc, int rm) {
	lwCont=lwc;
	btn=b;
      runModeCubby = rmc;
      runMode = rm;
      }

   public void actionPerformed(ActionEvent e) {
	for (int i=0;i<lwCont.getComponentCount();i++)
	   lwCont.getComponent(i).setBackground(Environment.BGCOLOR);
	btn.setBackground(Environment.BGCOLOR.darker());
	runModeCubby.set(runMode,true);
	makePosTextLabel(runMode);
   }
}

class PositionListener implements AdjustmentListener {
   public void adjustmentValueChanged(AdjustmentEvent e) {
      synchronized(this) {
			showStartTime=true; //runMode will switch to stop, ensure <Position labelling
  			//EMF 10/25/00: pass showStartTime state to RBNBInterface
  			posDurCubby.setPositionAtStart(showStartTime);
			psval=posSlider.getValue();
			// JPW 04/08/2005: Not needed
			//                 If we ever add this back, must make
			//                 sure to remove adjustment listeners
			//                 before calling setValue() and add
			//                 them back after calling setValue()
			// posSlider.setValue(psval); //need to do this so older JDKs update correctly
			if (psval==0) position[0]=position[1];
			else if (psval==1000) position[0]=position[2].subtractTime(duration);
			else position[0]=new Time((psval*
				(position[2].getDoubleValue()-position[1].getDoubleValue())/1000)+
				position[1].getDoubleValue());
			posDurCubby.setPosition(position,true);
			/* INB 04/09/98 */
			try {
				posLabel.setText(position[0].getFormattedString(posDurCubby.getTimeFormat(),Time.Full,posDurCubby.getPrecision()));
                                if (posDurCubby.getTimeFormat()!=Time.Unspecified) {
                                  rawTimeLabel.setText(position[0].getFormattedString(Time.Unspecified,Time.Full,posDurCubby.getPrecision())+" Sec");
                                } else {
                                  rawTimeLabel.setText("");
                                }
				}
			catch (Exception e1) {}
			}
      }
   }//end class PositionListener

class DurationListener implements AdjustmentListener {
   Time[] durTime=null;
	Time[] secTime=new Time[41];
	Time[] hmsTime=new Time[41];
	private long day=24*60*60;
	private long[] hms={30,60,120,300,600,1800,3600,7200,18000,43200};
	private int Day=3;
	private int HourMin=2;
	private int Sec=1;
	Time dur=null; //set by text box
	boolean setByText=false;
	int lastValue=20;
	
   public DurationListener() {
		setDurTimeValues();
		}
	
	public void setFromText(Time durI) {
		dur=durI;
		setByText=true;
		for (int t=durTime.length-1;t>=0;t--) {
			if (dur.compareTo(durTime[t])>=0) {
				durSlider.setValue(t);
				lastValue=t;
				break;
			}
		}		
	}
	
	public void setDurTimeValues() {
	long mant,m;  //reference duration=mant*10^exp
	byte exp,e;
	int timeFormat=posDurCubby.getTimeFormat();
		double dur=duration.getDoubleValue();
		exp=(byte)Math.floor(Math.log(dur)/Math.log(10));
		mant=(long)Math.floor(dur*Math.pow(10,-1*exp));
		if (mant>=5) mant=5;
		else if (mant>=2) mant=2;
		else if (mant>=1) mant=1;
		else {
			mant=5;
			exp-=1;
			}
		m=mant;
		e=exp;
		//fill in secTime array
		secTime[20]=new Time(mant,exp);
		for (int i=19;i>=0;i--) {
			if (mant==5) mant=2;
			else if (mant==2) mant=1;
			else {
				mant=5;
				exp-=1;
				}
			secTime[i]=new Time(mant,exp);
			}
		//EMF 9/16/05: set 0 for shortest duration
		secTime[0]=new Time(0.0);
		mant=m;
		exp=e;
		for (int i=21;i<=40;i++) {
			if (mant==1) mant=2;
			else if (mant==2) mant=5;
			else {
				mant=1;
				exp+=1;
				}
			secTime[i]=new Time(mant,exp);
			}
		//fill in hmsTime array
		mant=m;
		exp=e;
		int startRegime=0;
		int regime=0;
		int startIdx=0;
		int idx=0;
		if (dur>=hms[0] && dur<=hms[9]) { //find starting point from hms array
			while (dur>hms[startIdx]) startIdx++;
			startRegime=HourMin;
			}
		else if (dur>hms[9]) {
			startRegime=Day;
			double days=Math.rint(dur/day);
			exp=(byte)Math.floor(Math.log(days)/Math.log(10));
			mant=(long)Math.floor(days*Math.pow(10,-1*exp));
			if (mant>=5) mant=5;
			else if (mant>=2) mant=2;
			else if (mant>=1) mant=1;
			else {
				mant=5;
				exp-=1;
				}
			m=mant;
			e=exp;
			}
		else startRegime=Sec;
		regime=startRegime;
		idx=startIdx;
		//fill from middle point down
		if (regime==Sec) { //simplest case - just 521 rule
			hmsTime[20]=new Time(mant,exp);
			for (int i=19;i>=0;i--) {
				if (mant==5) mant=2;
				else if (mant==2) mant=1;
				else {
					mant=5;
					exp-=1;
					}
				hmsTime[i]=new Time(mant,exp);
				}
			}
		else if (regime==HourMin) { //start in array, then 521 rule
			hmsTime[20]=new Time(hms[idx],(byte)0);
			for (int i=19;i>=0;i--) {
				if (--idx==-1) {
					regime=Sec;
					mant=2;
					exp=1;
					}
				if (regime==HourMin) hmsTime[i]=new Time(hms[idx],(byte)0);
				else {
					if (mant==5) mant=2;
					else if (mant==2) mant=1;
					else {
						mant=5;
						exp-=1;
						}
					hmsTime[i]=new Time(mant,exp);
					}
				}
			}
		else { //regime==Day, start 521day, then array, then 521seconds
			hmsTime[20]=new Time(day*mant,exp);
			for (int i=19;i>=0;i--) {
				idx--;
				if (mant==5) mant=2;
				else if (mant==2) mant=1;
				else {
					mant=5;
					exp-=1;
					}
				if (regime==Day && mant*Math.pow(10,exp)<1) {
					regime=HourMin;
					idx=9;
					}
				else if (regime==HourMin && idx<0) {
					regime=Sec;
					mant=1;
					exp=1;
					}
				if (regime==Day) hmsTime[i]=new Time(day*mant,exp);
				else if (regime==HourMin) hmsTime[i]=new Time(hms[idx],(byte)0);
				else hmsTime[i]=new Time(mant,exp);
				}
			}
		//EMF 9/16/05: set 0 for shortest duration
		hmsTime[0]=new Time(0.0);
		//now, fill in upper part of hmsTime array
		regime=startRegime;
		idx=startIdx;
		mant=m;
		exp=e;
		if (regime==Day) { //simplest case
			for (int i=21;i<=40;i++) {
				if (mant==1) mant=2;
				else if (mant==2) mant=5;
				else {
					mant=1;
					exp++;
					}
				hmsTime[i]=new Time(day*mant,exp);
				}
			}
		else if (regime==HourMin) { //start in hms array, then day
			for (int i=21;i<=40;i++) {
				idx++;
				if (mant==1) mant=2;
				else if (mant==2) mant=5;
				else {
					mant=1;
					exp++;
					}
				if (regime==HourMin && idx>9) {
					regime=Day;
					mant=1;
					exp=0;
					}
				if (regime==HourMin) hmsTime[i]=new Time(hms[idx],(byte)0);
				else hmsTime[i]=new Time(day*mant,exp);
				}
			}
		else { //start in sec, then hms array, then day
			for (int i=21;i<=40;i++) {
				idx++;
				if (mant==1) mant=2;
				else if (mant==2) mant=5;
				else {
					mant=1;
					exp++;
					}
				if (regime==Sec && mant*Math.pow(10,exp)>10) {
					regime=HourMin;
					idx=0;
					}
				else if (regime==HourMin && idx>9) {
					regime=Day;
					mant=1;
					exp=0;
					}
				if (regime==Sec) hmsTime[i]=new Time(mant,exp);
				else if (regime==HourMin) hmsTime[i]=new Time(hms[idx],(byte)0);
				else hmsTime[i]=new Time(day*mant,exp);
				}
			}
		if (timeFormat==Time.Unspecified) durTime=secTime;
		else durTime=hmsTime;
		duration=durTime[20];
		posDurCubby.setDuration(duration,true);
	//for (int i=0;i<21;i++)
	//   System.out.println("UserControl.DurationListener "+i+": "+durTime[i].getFloatValue());
	durSlider.setValue(20); //middle point in slider
	}
	
	public void updateTimeFormat(int timeFormat) {
		if (timeFormat==Time.AbsoluteSeconds1970 || timeFormat==Time.RelativeSeconds) durTime=hmsTime;
		else durTime=secTime;
		updateSlider();
		}
	
   public void adjustmentValueChanged(AdjustmentEvent e) {
	   //JScrollBar seems to generate events for mousepressed, 
	   //mousereleased, and mouseclicked, so ignore event if thumb
	   //hasn't moved
	   if (durSlider.getValue()==lastValue) {
	   } else {
		   updateSlider();
	   }
	   lastValue=durSlider.getValue();
	}
	
	private synchronized void updateSlider() {
	   dsval=durSlider.getValue();
	   // JPW 04/12/2005: Don't fire another event
	   // durSlider.setValue(dsval); //need to do this so older JDKs update correctly
	   duration=durTime[dsval];
	   //EMF 4/25/05: handle between-value durations set by text box
	   if (setByText) {
		   setByText=false;
		   if (duration.compareTo(dur)<0) {
			   durSlider.setValue(dsval+1);
			   duration=durTime[dsval+1];
		   }
	   }
	   posDurCubby.setDuration(duration,true);
		double power=Math.log(duration.getDoubleValue()/100)/Math.log(10);
		if (power>=0) posDurCubby.setPrecision(0);
		else posDurCubby.setPrecision((int)Math.ceil(Math.abs(power)));
		durLabel.setText(makeDurLabel(duration));
		}
   }//end class DurationListener
   
// cannot create a Container with new, so extend - need multiple containers
// to do nested layout of user controls
// class LWContainer extends Container {
class LWContainer extends JComponent {

   public LWContainer() {
   }
   
   public void paint(Graphics g) {
      super.paint(g);
   }
}

//EMF 3/2/05: replace FWLabel with a TextField so data can be entered
//            as well as just displayed
class FWField extends JTextField {
        //EMF 4/10/07: use Environment font so size can be changed
	//private Font f = new Font("Dialog",Font.PLAIN,10);
	private Font f = Environment.FONT10;
	
	public FWField(String text,String refWidth,int align) {
		super(text,refWidth.length());
		setFont(f);
	}
}

//EMF 3/2/05: listener class for FWField - position
class PositionTextListener implements ActionListener, FocusListener {
	
	public void actionPerformed(ActionEvent ae) {
		Time[] newpos=new Time[3];
		try {
			String pos=((JTextField)ae.getSource()).getText().trim();
			if (pos.indexOf(":")==-1) {
				newpos[0]=new Time(numberformat.parse(pos).doubleValue());
			} else {
				newpos[0]=Time.fromFormattedString(pos);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		newpos[1]=position[1];
		newpos[2]=position[2];
		changePosition(newpos);
		posDurCubby.setPosition(newpos,true);
	}
	
	public void focusGained(FocusEvent fe) {
		showStartTime=true; //runMode will switch to stop, ensure <Position labelling
  		posDurCubby.setPositionAtStart(showStartTime);
		//changePosition(position);
		runModeCubby.set(RunModeDefs.current,true);
		makePosTextLabel(RunModeDefs.current);
		for (int i=0;i<buttons.getComponentCount();i++) {
			buttons.getComponent(i).setBackground(Environment.BGCOLOR);
			if (buttons.getComponent(i).getName().equals(Integer.toString(RunModeDefs.stop)))
				 buttons.getComponent(i).setBackground(Environment.BGCOLOR.darker());
			}
	}
	
	public void focusLost(FocusEvent fe) {
	}
}

//EMF 4/21/05: listener class for FWField - duration
class DurationTextListener implements ActionListener, FocusListener {
	
	public void actionPerformed(ActionEvent ae) {
		Time newdur=null;
		try {
			String dur=((JTextField)ae.getSource()).getText().trim();
			newdur=new Time(numberformat.parse(dur).doubleValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		duration=newdur;
		posDurCubby.setDuration(duration,true);
		//change scrollbar value, but don't issue event
		AdjustmentListener[] listener=durSlider.getAdjustmentListeners();
		for (int i=0;i<listener.length;i++) {
			durSlider.removeAdjustmentListener(listener[i]);
		}
		for (int i=0;i<listener.length;i++) {
			if (listener[i] instanceof DurationListener) {
				((DurationListener)listener[i]).setFromText(duration);
			}
			durSlider.addAdjustmentListener(listener[i]);
		}
	}
	
	public void focusGained(FocusEvent fe) {
		showStartTime=true; //runMode will switch to stop, ensure <Position labelling
  		posDurCubby.setPositionAtStart(showStartTime);
		changePosition(position);
		runModeCubby.set(RunModeDefs.current,true);
		makePosTextLabel(RunModeDefs.current);
		for (int i=0;i<buttons.getComponentCount();i++) {
			buttons.getComponent(i).setBackground(Environment.BGCOLOR);
			if (buttons.getComponent(i).getName().equals(Integer.toString(RunModeDefs.stop)))
				 buttons.getComponent(i).setBackground(Environment.BGCOLOR.darker());
			}
	}
	
	public void focusLost(FocusEvent fe) {
	}
}

// Fixed Width Label - for slider labels
// need to use double buffering to eliminate flashing
// class FWLabel extends Container {
class FWLabel extends JComponent {
   //EMF 4/10/07: use Environment font so size can be changed
   //private Font f = new Font("Dialog",Font.PLAIN,10);
   private Font f = Environment.FONT10;
   private FontMetrics fm = getFontMetrics(f);
   private String label = null;
   private int alignment = SwingConstants.LEFT;
   private boolean newLabel=false;
   private int width=0;
   private int height=0;
   private int length=0;
   private int stringOffset=0;
   private Dimension oldSize=new Dimension(0,0);
   private Image bufferImage=null;

   public FWLabel(String text,String refWidth,int align) {
      label = text;
      alignment = align;
      width=fm.stringWidth(refWidth);
      height=fm.getAscent() + fm.getDescent();
      stringOffset=fm.getAscent();
      }
      
   public void setText(String text) {
      synchronized(this) {
		label=text;
		length=fm.stringWidth(label);
		newLabel=true;
	 }
      repaint();
      }
      
   public Dimension getMinimumSize() {
      return new Dimension(width,height+2);
      }
      
   public Dimension getPreferredSize() {
      return new Dimension(width,height+2);
      }
    
   public void update(Graphics g) {
      paint(g);
      }
        
   public void paint(Graphics g) {
      boolean newSize=false;
      synchronized(this) {
	 Dimension size=getSize();
	 if (size.width!=oldSize.width || size.height!=oldSize.height)
	    newSize=true;
	 if (newSize || newLabel) {
	    if (newSize) bufferImage=createImage(size.width,size.height);
	    Graphics bi=bufferImage.getGraphics();
	    bi.clearRect(0,0,size.width-1,size.height-1);
	    bi.setFont(f);
	    if (alignment==SwingConstants.LEFT) bi.drawString(label,0,stringOffset);
	    else if (alignment==SwingConstants.RIGHT) bi.drawString(label,size.width-length,stringOffset);
	    bi.dispose();
	    oldSize.width=size.width;
	    oldSize.height=size.height;
	    newLabel=false;
	    }
	 g.drawImage(bufferImage,0,0,null);
	 }
      super.paint(g);
      }
   }

} // end class UserControl
