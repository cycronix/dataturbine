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

/*************************
 * RBNBInterface - handles communication with RBNB, calls plotting routines
 */

// Modification History :
// 04/19/2000	JPW	Added "case LayoutCubby.ExportToDT" to the
//                      switch statement in changeLayout()
// 08/18/00     UCB     Added readily-visible comment blocks
//                      to highlight the head of each method.  Also
//                      regularized the bracing and indentation to
//                      current Emacs Java default.  Tried to
//                      shorten lines for printing.
// 10/25/00     EMF     modified duration updates to adjust start time
//                      if needed so position label in UserControl stays
//                      constant.  Note the state information regarding
//                      whether the displayed position is start or end time
//                      is held by UserControl.  Modification is to pass that
//                      state info with the duration update via PosDurCubby,
//                      and adjust the start time as appropriate.
// 3/21/01      EMF     Major changes to use RMAP Simple API.  A total hack job.
// 3/23/04      EMF     Make duration a function of display group, modify
//                      save/load configuration to use files not rbnb channels.
// 1/18/06      EMF     For duration=0, set 1/10 second as playback starting jump.

package com.rbnb.plot;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
//import COM.Creare.RBNB.API.Map;
//import COM.Creare.RBNB.API.Channel;
//import COM.Creare.RBNB.API.Connection;
//import COM.Creare.RBNB.DataRequest;
//import COM.Creare.RBNB.DataTimeStamps;
//import COM.Creare.RBNB.RegisteredChannel;
//import COM.Creare.RBNB.Time;
import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.ToString;

public class RBNBInterface implements Runnable {
    private int dg=0; //display group
    private PlotsContainer plotsContainer=null;
    private PlotContainer[][] pca=null; //plot containers
    private RegChannel[] cha=new RegChannel[0]; //available channels
    private Sink connection=null;
    private Map[] map=null;
    private Map cmap=null; //connection map with unsupported point size channels removed
    private Channel[] channel=new Channel[0];
    private boolean newData=false;
    private RBNBPlotMain rbnbPlotMain=null;
    private RunModeCubby runModeCubby=null;
    private LayoutCubby layoutCubby=null;
    private RBNBCubby rbnbCubby=null;
    private PosDurCubby posDurCubby=null;
    private ConfigCubby configCubby=null;
    private int runMode = RunModeDefs.stop;
    private Time start = new Time();
    private boolean firstRT=true;  //reset each time usercontrol makes 
                                       //changes - used to prevent
				       //replotting the same data
    private boolean rtTimeout=false; //if timeout once, don't try streaming again until RT is rehit
    private Time[] position = new Time[3]; //0=current, 1=min, 2=max
    //EMF 3/23/04: make duration a function of display group
    private Time[] duration = null;
    private static int flags = DataRequest.extendStart | DataRequest.extendEnd;
    private long speedMant = 2; //playrate mantissa
    private byte speedExp = -2; //playrate exponent
    private Time speed = new Time(speedMant,speedExp);
//# ifndef RTREQUEST
      private boolean rtReqMode = false;
//# endif
    private long lastUpdate=(new Date()).getTime();
    private double updatePeriod=0;
    private Time timeOutMin=new Time(1);
    //private Time timeOutMax=new Time(120);
    // EMF 9/30/99: reduce timeout from 120 to 2
    private Time timeOutMax=new Time(2);
    private int repaintContainer = -1;
    private int repaintContainerCount = 0;
    private int rtWaitTime = 20; //milliseconds to wait when RT gets no 
                                 //new data - reset on entering RT mode
    private int tlFrames = -1;
	// EMF 10/1/99: add boolean so don't update position when dragging slider
    private boolean updatePosition=true;
    //EMF 11/9/00: added local Environment instance so multiple versions of plot
    //             running in same JVM don't collide
    private Environment environment=null;
    private ExportData ed=null; //created on first data export
    //EMF 1/18/06: starting playback jump for duration==0
    //             note actual jump is this * 2e-2
    private double zerodurbase = 5.0;
    
    // JPW 11/29/2006: Dialog to handle exporting to RBNB
    private ExportToDT exportToDT = null;
    
    //EMF 9/4/07: Dialog to handle exporting to Matlab
    private ExportToMatlab exportToMatlab = null;
    
    /********************
     *
     *  RBNBInterface() (Constructor)
     *
     *******************/
   // constructor - makes initial connection, spawns thread
   // to do data check/get/plot loop
    public RBNBInterface(RBNBPlotMain rpm, PlotsContainer pc, RunModeCubby rmc,
			LayoutCubby loc, RBNBCubby rbc,PosDurCubby pdc,
			ConfigCubby cc, Environment e) {
	rbnbPlotMain = rpm;
	plotsContainer = pc;
	runModeCubby=rmc;
	layoutCubby=loc;
	rbnbCubby=rbc;
	posDurCubby=pdc;
	configCubby=cc;
        environment=e;
	//System.out.println("passed PlotsContainer: "+pc);
	//System.out.println("RBNBInterface: "+environment.HOST+":"+environment.PORT);
	// create empty map for each display group
	map=new Map[environment.DISPLAYGROUPS];
	duration=new Time[environment.DISPLAYGROUPS];
	for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	  map[i]=new Map();
	  duration[i]=new Time(0.1);
	}

	// connect to RBNB, get map and channels
	boolean stat=RBNBOpen(environment.HOST,environment.PORT);
	if (environment.HOST!=null) layoutCubby.setStatus(stat);
	if (!stat ||
	    !RBNBChannel()) {
	    start=new Time(0);
	    position[0]=start;
	    position[1]=position[0];
	    position[2]=start.addTime(new Time(1));
	    posDurCubby.setPosition(position,false);
	    posDurCubby.setTimeFormat(Time.Unspecified);
	    if (environment.DURATION!=null) {
		posDurCubby.setDuration(environment.DURATION,
					false);
	    }
	    else posDurCubby.setDuration(new Time(0.1),false);
	    cha=new RegChannel[0];
	    pca=new PlotContainer[environment.DISPLAYGROUPS][0];
	    rbnbCubby.setAvailableChannels(cha);
	    //System.out.println("no channels available in RBNB");
	    // start main data gathering/plotting thread
	    //Thread rbnbThread = new Thread(this,"rbnbThread");
	    //rbnbThread.start();
	    return;
	}
	if (!RBNBTimeLimits()) {
	    start=new Time(0);
	    position[0]=start;
	    position[1]=position[0];
	    position[2]=start.addTime(new Time(1));
	    posDurCubby.setPosition(position,false);
	    posDurCubby.setTimeFormat(Time.Unspecified);
	}
	//guestimate sane duration from last frame
	if (!RBNBDuration()) {
	    duration[dg]=new Time(0.1);
	    posDurCubby.setDuration(duration[dg],false);
	}
	
	// extract channel names, notify cubby, create arrays
	//cha = new String[channel.length];
	//pca = new PlotContainer[environment.DISPLAYGROUPS][channel.length];
	//for (int i=0;i<channel.length;i++) {
	//cha[i]=channel[i].channelName;
	//}
	//rbnbCubby.setAvailableChannels(cha);
	
	//timing bug - start from RBNBPlotMain after UserControl created
	// start main data gathering/plotting thread
	//Thread rbnbThread = new Thread(this,"rbnbThread");
	//rbnbThread.start();
	
    } // end RBNBInterface constructor
    
    /********************
     *
     *  RBNBOpen()
     *
     *******************/
    // RBNBOpen method - creates connection to rbnb
    // returns false if problems
    public boolean RBNBOpen(String serverHost,int serverPort) {
	
	/* INB 01/07/1999
	   For now rather than trying to recode this thing
	   to properly use the new streamSetMap/streamGetMap/
	   getMap calls, we just turn off the
	   deprecation warnings.
	*/
	//Connection.setErrorMask(~Connection.DeprecationWarnings);
	
	if (connection!=null) RBNBClose();
	if (serverHost==null || serverPort<=0) return false;
	try {
	    //connection=new Connection(serverHost+":"+serverPort,"r");
            connection=new Sink();
            connection.OpenRBNBConnection(serverHost+":"+serverPort,"rbnbPlot");
	    if (environment.STATICMODE) {
		//runMode=RunModeDefs.current;
		runMode=RunModeDefs.bof;
//# ifndef RTREQUEST
		if (rtReqMode) setRTReqMode(false);
//# endif
	    }
	    else {
		runMode=RunModeDefs.realTime;
//# ifndef RTREQUEST
		if (!rtReqMode) setRTReqMode(true);
//# endif
	    }
	    runModeCubby.set(runMode,false);
	}
	catch (Exception e) {
	    System.out.println("RBNBInterface.RBNBOpen: caught "+e);
	    e.printStackTrace();
	    runMode=RunModeDefs.stop;
	    runModeCubby.set(RunModeDefs.stop,false);
	    return false;
	}
	return true;
    }

    /********************
     *
     *  RBNBChannel()
     *
     *******************/
// RBNBChannel method - obtains channel list & forwards to UserControl
// returns false if problems
   public boolean RBNBChannel() {
       boolean returnVal=true;
       boolean newChans=false;
       RegChannel[] oldcha=null;
       PlotContainer[][] oldpca=null;
       Map imap=null;
       Channel[] chanInfo=null;
       
       if (connection==null) return false;
       try {
//# ifndef RTREQUEST
	   if (rtReqMode) setRTReqMode(false);
//# endif
//System.err.println("RBNBInterface.RBNBChannel: REQUESTFILTER "+environment.REQUESTFILTER);
	   try {
	       //cmap = connection.getChannels(environment.REQUESTFILTER);
               String[][] chanList=connection.getChannelList("*");
               cmap=new Map(chanList);
	   }
	   catch (Exception e) { //assume talking to old RBNB, retry
             System.err.println("RBNBInterface.RBNBChannel: exception");
             e.printStackTrace();
             return false;
	       //System.err.print("Connected to previous version RBNB, ");
	       //System.err.println("requesting ALL channels.");
	       //environment.REQUESTFILTER=Connection.AllChannels;
	       //cmap = connection.getChannels(environment.REQUESTFILTER);
	       //rbnbPlotMain.disableViewMenu();
	   }
	   channel = cmap.channelList();
           // EMF 5/30/01: filter out channels with _Log in name
           for (int i=0;i<channel.length;i++) {
             if (channel[i].getChannelName().indexOf("_Log")!=-1) {
               // System.err.println(
	       //     "Channel " + channel[i].getChannelName() + " removed.");
	       cmap.removeChannel(channel[i]);
             }
           }
           channel=cmap.channelList();
	   // EMF 3/26/99: filter out channels with unknown pointsizes, 
	   // including rbnbPlotConfig channels
	   /*if (!environment.SHOWALLCHANNELS && channel.length>0) {
	       imap = connection.getChannels(environment.REQUESTFILTER);
	       chanInfo=(connection.getInformation(imap)).channelList();
	       for (int i=0;i<chanInfo.length;i++) {
		   if (!(chanInfo[i].pointSize==2 ||
			 chanInfo[i].pointSize==4 || 
			 chanInfo[i].pointSize==8)) {
		       cmap.removeChannel(cmap.findChannel(
							   chanInfo[i].channelName));
		       System.err.println("Channel " + 
					  chanInfo[i].channelName + 
					  " has unsupported point size of " +
					  chanInfo[i].pointSize + 
					  ", ignored.");
		   }
	       }
	   }*/
	   // remove rbnbPlotConfig channels (which have 
	   //rbnbPlotConfig.##### as middle of channel name)
	   //for (int i=0;i<channel.length;i++) {
	     //if (channel[i].channelName.startsWith("rbnbPlotConfig",
	       //channel[i].channelName.indexOf("/")+1)) {
	        //if (RegisteredChannel.rboName(channel[i].channelName).startsWith("rbnbPlotConfig")) {
	            //lmap.removeChannel(channel[i]);
		//}
	     //}
	   //channel = cmap.channelList();
	   //if (channel.length>0) {  //taken out since static userdat now contains displaygroup info
		//connection.getInformation(lmap);
		//channel = lmap.channelList();
	   //}
//# ifndef RTREQUEST
	   if (runMode==RunModeDefs.realTime) setRTReqMode(true);
//# endif
	   // if (channel.length==0) returnVal=false;
	   if (cha.length>0) {
	       oldcha=cha;
	       oldpca=pca;
	   }
	   else newChans=true; //seed channels into appropriate display groups
	   cha=new RegChannel[channel.length];
	   pca=new PlotContainer[environment.DISPLAYGROUPS][channel.length];
	   for (int i=0;i<channel.length;i++) {
	       cha[i]=new RegChannel(channel[i]);
	       for (int j=0;j<environment.DISPLAYGROUPS;j++) pca[j][i]=null;
	   }
	   if (oldcha!=null) { //save existing plotcontainers as needed, delete others
	       boolean[] kept=new boolean[oldcha.length];
	       for (int i=0;i<oldcha.length;i++) kept[i]=false;
	       for (int i=0;i<cha.length;i++) {
		   for (int j=0;j<oldcha.length;j++) {
		       if (cha[i].name.equals(oldcha[j].name)) {
			   for (int k=0;k<environment.DISPLAYGROUPS;k++) {
			       pca[k][i]=oldpca[k][j];
			   }
			   kept[j]=true;
		       }
		   }
	       }
	       for (int i=0;i<oldcha.length;i++) if (!kept[i]) {
		   for (int j=0;j<environment.DISPLAYGROUPS;j++) {
		       if (oldpca[j][i]!=null) {
			   plotsContainer.removePlot(oldpca[j][i],j);
			   map[j].removeChannel(map[j].findChannel(oldcha[i].name));
		       }
		   }
	       }
	   }
	   rbnbCubby.setAvailableChannels(cha);
	   
	   if (newChans) { //select channels into display groups
				// get userdata for channels, check for preferred display grouping
	       String[] dgName=new String[channel.length];
	       String[] dgLabel=new String[environment.DISPLAYGROUPS];
	       for (int i=0;i<channel.length;i++) {
		   if (channel[i].channelUserDataType==1) {
		       KeyValueHash kvh=new KeyValueHash(channel[i].channelUserData);
		       dgName[i]=(String)kvh.get("group");
		       //System.out.println("RBNBInterface.RBNBChannel: dgName["+i+"]="+dgName[i]);
		   }
		   else dgName[i]=null;
	       }
				// group commonly identified channels into display groups
	       int j=0; //display group
	       for (int i=0;i<channel.length;i++) {
		   if (dgName[i]!=null) {
		       boolean found=false;
		       for (int k=0;k<j;k++) {
			   if (dgName[i].equals(dgLabel[k])) { //add to existing group
			       pca[k][i]=plotsContainer.addPlot(cha[i],k);
			       pca[k][i].setAbscissa(duration[dg]);
			       map[k].addChannel(cmap.findChannel(cha[i].name));
			       found=true;
			       k=j;
			   }
		       }
		       if (!found) { //create new group
			   dgLabel[j]=dgName[i];
			   pca[j][i]=plotsContainer.addPlot(cha[i],j);
			   pca[j][i].setAbscissa(duration[dg]);
			   map[j].addChannel(cmap.findChannel(cha[i].name));
			   j++;
		       }
		   }
	       }
				// put rest of channels into following display groups, max of 4 per group
	       int num=0;
//EMF 10/09/02: do put into display groups, if SHOWALLCHANNELS is true
// (change for m0tran port to RBNBv2 at INB request)
//EMF 2/22/02: do not put channels into display groups
             if (environment.SHOWALLCHANNELS) {
	       for (int i=0;i<channel.length;i++) {
		   if (j>=environment.DISPLAYGROUPS) break;
		   if (dgName[i]==null) {
		       pca[j][i]=plotsContainer.addPlot(cha[i],j);
		       pca[j][i].setAbscissa(duration[dg]);
		       map[j].addChannel(cmap.findChannel(cha[i].name));
		       if (++num%environment.CHANSPERDG == 0) j++;
		   }
	       }
             }
				//set display group button labels
	       plotsContainer.labelDisplayGroups(dgLabel);
	   } //end if newChans
	   // set selected channels for current display group
	   int j=0;
	   for (int i=0;i<cha.length;i++) if (pca[dg][i]!=null) j++;
	   RegChannel[] sca=new RegChannel[j];
	   j=0;
	   for (int i=0;i<cha.length;i++) if (pca[dg][i]!=null) sca[j++]=cha[i];
	   rbnbCubby.setSelectedChannels(sca,false); 
	   // force repaint of plotcontainers
	   plotsContainer.invalidate();
	   plotsContainer.validate();
	   plotsContainer.repaint();
       }
       catch (Exception e) {
	   System.out.println("RBNBInterface.RBNBChannel: caught "+e);
	   e.printStackTrace();
	   runMode=RunModeDefs.stop;
//# ifndef RTREQUEST
	   if (rtReqMode) setRTReqMode(false);
//# endif
	   runModeCubby.set(RunModeDefs.stop,false);
	   returnVal=false;
       }
       return returnVal;
   } //end RBNBChannel method

    /********************
     *
     *  RBNBTimeLimits()
     *
     *******************/
// RBNBTimeLimits - determines start/end time of data, sets position
    public boolean RBNBTimeLimits() {
	
	if (connection==null) return false;
	try {
//# ifndef RTREQUEST
	    if (rtReqMode) setRTReqMode(false);
//# endif
	    //EMF 3/5/01: get time limits only for displayed channels
	    //channel = cmap.channelList();
	    channel = map[dg].channelList();
	    if (channel.length==0) return false;
	    
	    //cmap=connection.getTimeLimits(cmap);
	    map[dg]=connection.getTimeLimits(map[dg]);
//# ifndef RTREQUEST
	    if (runMode==RunModeDefs.realTime) setRTReqMode(true);
//# endif
            //channel = cmap.channelList();
            channel = map[dg].channelList();
	    
	    tlFrames=-1;
	    Time begin=null;
	    Time end=null;
	    Time newpt=null;
	    for (int i=0;i<channel.length;i++) {
		if (channel[i].timeStamp!=null) {
		    newpt=channel[i].timeStamp.getStartOfInterval(DataTimeStamps.first);
		    if (begin==null) begin=newpt;
		    else if (newpt.compareTo(begin)==-1) begin=newpt;
		    newpt=newpt.addTime(channel[i].timeStamp.getDurationOfInterval(DataTimeStamps.first));
		    if (end==null) end=newpt;
		    else if (newpt.compareTo(end)==1) end=newpt;
		}
		// get number of frames in case RBNBDuration can't estimate sane duration
		if (channel[i].frames!=null) {
		    int numFrames = channel[i].frames.getDurationOfInterval(0).getIntValue();
		    if (numFrames>tlFrames) tlFrames=numFrames;
		}
	    }
		if (begin==null) begin=new Time(0);
	    if (end==null) end=begin.addTime(new Time(1));
	    position[1]=begin;
	    position[2]=end;
		if (position[0]==null) position[0]=begin;
		if (position[0].compareTo(position[1])<0) position[1]=position[0];
		if (position[0].compareTo(position[2])>0) position[2]=position[0];
	    posDurCubby.setPosition(position,false);
	    for (int i=0;i<channel.length;i++) {
		if (channel[i].timeStamp!=null) {
		    posDurCubby.setTimeFormat(channel[i].timeStamp.format);
		    i=channel.length;
		}
	    }
	}
	catch (Exception e) {
	    System.out.println("RBNBInterface.RBNBTimeLimits: caught "+e);
	    e.printStackTrace();
	    return false;
	}
	return true;
    } //end RBNBTimeLimits

    /********************
     *
     *  RBNBDuration()
     *
     *******************/
// RBNBDuration method - sets sane duration based on most recent frame of data
    public boolean RBNBDuration() {
//System.err.println("RBNBInterface.RBNBDuration called");
	Time newDuration=null;
	if (connection==null) return false;
	//if set via command line option, use it here
	if (environment.DURATION!=null) {
	    for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	      duration[i]=new Time(environment.DURATION);
	    }
	    posDurCubby.setDuration(duration[dg],false);
	    return true;
	}
	try {
//# ifndef RTREQUEST
	    if (rtReqMode) setRTReqMode(false);
//# endif
	    //cmap=connection.getChannelList();
	    if (cmap.channelList().length==0) {
		//System.out.println("RBNBInterface.RBNBDuration: no channels in map");
		return false;
	    }
	    connection.getInformation(cmap);
//# ifndef RTREQUEST
	    if (runMode==RunModeDefs.realTime) setRTReqMode(true);
//# endif
	    channel=cmap.channelList();
	    Time newdur=null;
	    int npts=0;
	    for (int i=0;i<channel.length;i++) {
		if (channel[i].timeStamp!=null && channel[i].numberOfPoints>0) {
		    newdur=channel[i].timeStamp.getEndOfInterval(DataTimeStamps.last).
			subtractTime(channel[i].timeStamp.getStartOfInterval(DataTimeStamps.first));
		    if (channel[i].numberOfPoints<200 && 
			   newdur.compareTo(new Time(0))==1) {
			newdur=newdur.multiplyBy(200).divideBy(channel[i].numberOfPoints);
		    }
		    if (newdur.compareTo(new Time(0))==1) {
			if (newDuration==null) newDuration=newdur;
			else if (newdur.compareTo(newDuration)==-1) newDuration=newdur;  //choose shortest frame
		    }
		}
	    }
	    //EMF 4/27/99: don't use this backup code, just set duration=0.1.  If the channels
	    // have disjoint time ranges, this will guess a really long duration.  The correct
	    // fix is to save total duration and number of frames for each channel.
	    
	    //if needed, try to use number of frames to make sane estimate
	    //if (newDuration==null || newDuration.compareTo(new Time(0))==0)
	    //	if (tlFrames>0) {
	    //		newDuration=position[2].subtractTime(position[0]).multiplyBy(200).divideBy(tlFrames);
	    //		}
	    if (newDuration==null || newDuration.compareTo(new Time(0))==0) newDuration=new Time(0.1);
	    duration[dg]=newDuration;
	    posDurCubby.setDuration(duration[dg],false);
	}
	catch (Exception e) {
	    System.out.println("RBNBInterface.RBNBDuration: caught "+e);
	    e.printStackTrace();
	    return false;
	}
	return true;
    }
    
    /********************
     *
     *  RBNBClose()
     *
     *******************/
// RBNBClose method - closes current RBNB connection, sets to null, cleans up
    public void RBNBClose() {
	if (connection!=null) {
//# ifndef RTREQUEST
	    if (rtReqMode) setRTReqMode(false);
//# endif
	    if (environment.KILLRBNB)
		try {
		    connection.terminateRBNB();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    else connection.disconnect(true,true);
	    connection=null;
	    cmap=null; //free map of suitable channels
	    //clear channel list (usercontrol will send back zero length
	    // selected channel list, thereby clearing plotcontainers
	    cha=new RegChannel[0];
	    rbnbCubby.setAvailableChannels(cha);
	    for (int i=0;i<pca.length;i++)
		for (int j=0;j<pca[i].length;j++)
		    if (pca[i][j]!=null) plotsContainer.removePlot(pca[i][j],i);
	    pca=new PlotContainer[environment.DISPLAYGROUPS][0];
	    for (int i=0;i<environment.DISPLAYGROUPS;i++) map[i].clear();
	    //EMF 8/30/99: reset display group button labels to numbers
	    String dgLabel[]=new String[environment.DISPLAYGROUPS];
	    for (int i=0;i<environment.DISPLAYGROUPS;i++) dgLabel[i]=String.valueOf(i+1);
	    plotsContainer.labelDisplayGroups(dgLabel);
	}
	// JPW 11/29/2006: If the ExportToDT dialog is up, pop it down
	if (exportToDT != null) {
	    exportToDT.setVisible(false);
	    exportToDT = null;
	}
    }
    
    /********************
     *
     *  RBNBSaveConfig()
     *
     *******************/
// RBNBSaveConfig method - writes config information to rbnb on a new connection
    private void RBNBSaveConfig(Channel lchan,byte[] configByte) {
      System.err.println("RBNBInterface: SaveConfig not implemented");
      /*
	try {
	    Connection lconnection=new Connection(environment.HOST+":"+environment.PORT,"w");
	    Map lmap = new Map();
	    lchan.setUser((short)1,configByte);
	    lmap.addChannel(lchan);
	    String sourceName=lconnection.setRingBuffer(null,"None",0,1,1);
	    sourceName="rbnbPlotConfig"+sourceName.substring(sourceName.lastIndexOf("."));
	    lconnection.setRingBuffer(sourceName,"None",0,1,1);
	    lconnection.registerChannels(lmap);
	    lconnection.disconnect(false,true);
	}
	catch (Exception e) {
	    System.out.println("RBNBInterface.RBNBSaveConfig: exception ");
	    e.printStackTrace();
	}
      */
    }

    /********************
     *
     *  RBNBConfigChannels()
     *
     *******************/
// RBNBConfigChannels method - returns list of available rbnbPlot configuration channels
    private Channel[] RBNBConfigChannels() {
      System.err.println("RBNBInterface: ConfigChannels not implemented");
	Channel[] chName=null;
      /*
	try {
	    //get channel list
	    Map lmap=connection.getChannelList();
	    Channel[] ch=lmap.channelList();
	    //keep only rbnbPlot config channels
	    for (int i=0;i<ch.length;i++) {
				//if (!ch[i].channelName.startsWith("rbnbPlotConfig",ch[i].channelName.indexOf("/")+1))
		if (!RegisteredChannel.rboName(ch[i].channelName).startsWith("rbnbPlotConfig"))
		    lmap.removeChannel(ch[i]);
	    }
	    //make list for user to choose from (include only the middle part of the full channel name)
	    chName=lmap.channelList();
	}
	catch (Exception e) {
	    System.out.println("RBNBInterface.RBNBConfigChannels: caught exception");
	    e.printStackTrace();
	}
      */
	return chName;
    }
    
    /********************
     *
     *  RBNBApplyConfig()
     *
     *******************/
// RBNBApplyConfig method - clears all existing channels, add new ones from hashtable that are
// available in current channel list
    public void RBNBApplyConfig(Hashtable ht) {
      //System.err.println("RBNBInterface: ApplyConfig not implemented");
	//remove existing channels
	for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	    for (int j=0;j<pca[i].length;j++) {
		if (pca[i][j]!=null) {
		    plotsContainer.removePlot(pca[i][j],i);
		    pca[i][j]=null;
		}
		map[i].clear();
	    }
	}
	// loop through channels in config, compare against available list, add if matches
	for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	    int numChan = Integer.parseInt((String)ht.get("dg["+i+"].chans"));
	    for (int j=0;j<numChan;j++) {
		String chan = (String)ht.get("dg["+i+"]["+j+"].name");
		for (int k=0;k<cha.length;k++) {
		    if (chan.equals(cha[k].name)) { //got match, add
			pca[i][k]=plotsContainer.addPlot(new RegChannel(chan),i);
			pca[i][k].setAbscissa(duration[dg]);
			Channel ch=new Channel();
			ch.setName(chan);
			map[i].addChannel(ch);
			break;
		    }
		}
	    }
	}
	//EMF 3/23/04: set durations
	for (int i=0;i<environment.DISPLAYGROUPS;i++) {
	    String durString=(String)ht.get("duration["+i+"]");
	    if (durString!=null) duration[i]=new Time(Double.parseDouble(durString));
	}
    } //end RBNBApplyConfig method
	  
    /********************
     *
     *  run()
     *
     *******************/ 		
// run method - separate thread that checks cubbyholes for
// requests, performs data requests and plotting calls
    public void run() {
	int layout = 0;
	Integer newVal = null;
	RegChannel[] newRC = null;
	Time newDur = null;
	Time[] newPosition=new Time[3];
	
	//System.out.println("run: rbnb thread started");
	// main loop, checks for and responds to changes in
	// cubbyhole values
	// implemented in else-if form to allow other threads
	// time to run between changes and data requests
//try{Thread.sleep(500);}catch(Exception e){}

		while(true) {
//System.err.println("top of run() while");
	    try {
		Thread.sleep(10); // let other threads run
	    }
	    catch (InterruptedException e) {
		System.out.println("thread sleep error: "+e);
	    }
	    // rbnb cubby - changes in selected channels and display group
	    if ((newRC = rbnbCubby.getSelectedChannels(false))!=null) {
		firstRT=true;
		changeChannels(newRC);
		//update plots if needed
		if (runMode==RunModeDefs.stop) runMode=RunModeDefs.current;
	    }
	    else if ((newVal = rbnbCubby.getGroup())!=null) {
		firstRT=true;
//# ifndef RTREQUEST
		if (rtReqMode) setRTReqMode(false);
//# endif
		dg=newVal.intValue();
		posDurCubby.setDuration(duration[dg],false);
//# ifndef RTREQUEST
		if (runMode==RunModeDefs.realTime) setRTReqMode(true);
//# endif
		//update start/duration in plotcontainers
		for (int i=0;i<cha.length;i++) if (pca[dg][i]!=null) {
		    pca[dg][i].setAbscissa(duration[dg]);
		}
		//set selected channels from non-null pca[dg] array
		int j=0;
		for (int i=0;i<cha.length;i++) if (pca[dg][i]!=null) j++;
		RegChannel[] sca=new RegChannel[j];
		j=0;
		for (int i=0;i<cha.length;i++) if (pca[dg][i]!=null) sca[j++]=cha[i];
		rbnbCubby.setSelectedChannels(sca,false);
		if (runMode==RunModeDefs.stop) runMode=RunModeDefs.current;
	    }
	    
	    
	    // layout cubby - changes in layout and config
	    else if ((newVal = layoutCubby.get())!=null) {
		firstRT=true;
		changeLayout(newVal.intValue());
	    }
	    
	    // posDur cubby - changes in position or duration
	    else if ((newDur = posDurCubby.getDuration(false))!=null) {
		firstRT=true;
                //EMF 10/25/00: check anchor for position label, adjust start
                //              time if appropriate
                if (posDurCubby.getPositionAtStart()) {
		  duration[dg]=newDur;
                } else { //reduce start time so start+duration is constant
                  start=start.addTime(duration[dg]).subtractTime(newDur);
                  duration[dg]=newDur;
                }
//System.err.println("calling setAbscissa");
		for (int i=0;i<pca[dg].length;i++) {
		    if (pca[dg][i]!=null) {
			pca[dg][i].setAbscissa(duration[dg]);
		    }
		}
//System.err.println("calling resetPosition");
		resetPosition(false);
		if (runMode==RunModeDefs.stop) {
		    runMode=RunModeDefs.current;
		}
//System.err.println("end change duration");
	    }
	    else if ((newPosition = posDurCubby.getPosition(false))!=null) {
		firstRT=true;
		updatePosition=false;
		// EMF 10/1/99: if at known endpoints, recheck them
		if ((newPosition[0].compareTo(position[1])<=0) ||
		    (newPosition[0].addTime(duration[dg]).compareTo(position[2])>=0)) {
		    resetPosition(false);
		    position[0]=newPosition[0]; //keep current location
		    updatePosition=true;
		    //System.err.println("updating endpoints"+position[0].getDoubleValue());
		}
		else position=newPosition;
		start=position[0];
		runMode=RunModeDefs.current;
//# ifndef RTREQUEST
		if (rtReqMode) setRTReqMode(false);
//# endif
		//if (runMode==RunModeDefs.stop) runMode=RunModeDefs.current;
	    }
	    
	    // run mode cubby - changes in run mode
	    // could do blocking mode wait if runmode=stop, would use less CPU
	    else if ((newVal = runModeCubby.get(false))!=null) {
		firstRT=true;
		rtTimeout=false;
		int newRunMode=newVal.intValue();
		if (newRunMode==RunModeDefs.fwdPlay || newRunMode==RunModeDefs.revPlay)
		    if (newRunMode==runMode) incrementSpeed(false); //increase play speed
		    else incrementSpeed(true); //reset play speed
		runMode=newRunMode;
//# ifndef RTREQUEST
		if (runMode==RunModeDefs.realTime && rtReqMode==false) setRTReqMode(true);
		else if (runMode!=RunModeDefs.realTime && rtReqMode==true) setRTReqMode(false);
//# endif
		if (runMode==RunModeDefs.quit) { // clean up and exit run() so thread dies
		    RBNBClose();
		    break;
		}
		//if realTime, reset rtWait
		if (runMode==RunModeDefs.realTime) rtWaitTime=20;
	    }
	    // get and plot data if appropriate
	    else {
		oneDataStep();
	    }
	}
	// EMF 9/8/99: commented out Thread.stop() line - method has been deprecated in Java1.2,
	//             and it should not be needed here since this ends the run method anyway...
	//Thread.currentThread().stop(); // explicit stop, rather than exit run()
    } //end run() method
    
    /********************
     *
     *  setRTReqMode()
     *
     *******************/
//# ifndef RTREQUEST
 //setRTReqMode method - sets up or takes down real-time streaming mode
 //to simplify things for now, it fetches one batch of data so don't need
 //to bookkeep on if first fetch or not
    private void setRTReqMode(boolean intoRT) {
	boolean quitRT=false;

        //EMF 3/5/01: firewall null connection
        if (connection==null) return;
	
        //EMF 2/5/01: ensure in r/r mode, in case user turned off slave mode
        //            while system was in rt streaming
	if (environment.STREAMING==false) {
          try {
            connection.setSinkMode("Request");
          } catch (Exception e) {
            e.printStackTrace();
          }
          rtReqMode=false;
          runModeCubby.setStreaming(false);
          return; //don't even try to stream...
        }
	
	if (intoRT && !rtTimeout) {
	    rtReqMode=true;
	    try {
		//connection.synchronizeSink();
		if (map[dg].channelList().length==0 || duration[dg].mantissa==0) {
		    //runMode=RunModeDefs.stop;
		    //runModeCubby.set(runMode,false);
		    rtReqMode=false;
		}
		else {
		    connection.setSinkMode("Stream");
		    Time timeOut = duration[dg].multiplyBy(2);
		    if (timeOut.compareTo(timeOutMin)==-1) timeOut=timeOutMin;
		    else if (timeOut.compareTo(timeOutMax)==1) timeOut=timeOutMax;
		    connection.setReadTimeOut(timeOut);
		    connection.streamSetMap(map[dg],null,duration[dg],
					    flags | DataRequest.newest | DataRequest.aligned | DataRequest.realTime);
                    //EMF 11/30/00: if in slavemode, don't check that returned
                    //              data is sane
                    if (!environment.SLAVEMODE) {
		    // INB 08/30/1999
		    // Save the current map so that we don't lose it when we get no data.
		    Map oldMap = map[dg];
		    map[dg]=connection.streamGetMap();

		     //check that got enough data; if not, switch to request/response mode
		     //System.out.println("setRTReqMode: got data at "+(new Date()).getTime());
		    Channel[] lch=map[dg].channelList();
		    if (lch==null || lch.length<1 || 
			  lch[0].numberOfPoints==0 || 
			  lch[0].timeStamp==null) {
			// INB 08/30/1999
			// Restore the old map.
			map[dg] = oldMap;
			quitRT=true;
			rtTimeout=true;
		    } else {
			Time returnedDuration = lch[0].timeStamp.getEndOfInterval(DataTimeStamps.last).
			    subtractTime(lch[0].timeStamp.getStartOfInterval(DataTimeStamps.first));
			/* INB 08/14/1998 
			   If you divide by numberOfPoints - 1
			   and numberOfPoints is 1, you get a
			   divide by zero.
			*/
			if (lch[0].numberOfPoints > 1) {
			    returnedDuration = returnedDuration.addTime(
				     returnedDuration.divideBy(lch[0].numberOfPoints-1).multiplyBy(2));
			}
			if (returnedDuration.compareTo(duration[dg])==-1) quitRT=true;
		    }
                    } //EMF 11/30/00
		    if (quitRT) {
			rtReqMode=false;
			//EMF 8/31/99: sync is redundant
			//connection.synchronizeSink();
			connection.setSinkMode("Request");
		    } else {
			runModeCubby.setStreaming(true);
                        runModeCubby.set(RunModeDefs.realTime,false);
		    }
		}
	    }
	    catch (Exception e) {
		System.out.println("RBNBInterface.setRTReqMode: exception");
		e.printStackTrace();
	    }
	}
	else if (!intoRT) {
	    rtReqMode=false;
	    runModeCubby.setStreaming(false);
	    try {
		connection.synchronizeSink();
		connection.setSinkMode("Request");
	    }
	    catch (Exception e) {
		System.out.println("RBNBInterface.setRTReqMode: exception");
		e.printStackTrace();
	    }
	}
    }
//# endif

    /********************
     *
     *  changeChannels()
     *
     *******************/
   // changeChannels method - creates/removes plotcontainers
   // as needed to match requested channels
   // looping logic assumes channel names are in same order
   // as in cha, the available channels list
    private void changeChannels(RegChannel[] sca) {
//for (int i=0;i<sca.length;i++) System.out.println("RBNBInterface.changeChannels: "+sca[i]);
//# ifndef RTREQUEST
	if (rtReqMode) setRTReqMode(false);
//# endif
	newData=false;
	if (sca.length==0) {
	    for (int j=0;j<pca[dg].length;j++) {
		if (pca[dg][j]!=null) {
		    plotsContainer.removePlot(pca[dg][j],dg);
//System.out.println("RBNBInterface.changeChannels: removed pca["+dg+"]["+j+"] "+pca[dg][j]);
		    pca[dg][j]=null;
		    map[dg].removeChannel(map[dg].findChannel(cha[j].name));
		}
	    }
	    runMode=RunModeDefs.stop; //stop looping if no channels selected
//# ifndef RTREQUEST
	    if (rtReqMode) setRTReqMode(false);
//# endif
	    runModeCubby.set(RunModeDefs.stop,false);
	}
	else {
	    int j=0;
	    for (int i=0;i<sca.length;i++) {
		while (sca[i].name.equals(cha[j].name)==false) {
		    // channel not wanted, be sure not in map and no plotcontainer
		    if (pca[dg][j]!=null) {
			plotsContainer.removePlot(pca[dg][j],dg);
//System.out.println("RBNBInterface.changeChannels: removed pca["+dg+"]["+j+"] "+pca[dg][j]);
			pca[dg][j]=null;
			map[dg].removeChannel(map[dg].findChannel(cha[j].name));
		    }
		    j++;
		}  //end while
		// channel wanted, be sure in map and has plotcontainer
		if (pca[dg][j]==null) {
		    pca[dg][j]=plotsContainer.addPlot(cha[j],dg);
//System.out.println("RBNBInterface.changeChannels: added pca["+dg+"]["+j+"] "+pca[dg][j]);
		    pca[dg][j].setAbscissa(duration[dg]);
		    Channel ch = new Channel();
		    ch.setName(cha[j].name);
		    map[dg].addChannel(ch);
		}
		j++;
	    } //end for
	    for (int i=j;i<cha.length;i++) { // remove rest of channels
		if (pca[dg][i]!=null) {
		    plotsContainer.removePlot(pca[dg][i],dg);
//System.out.println("RBNBInterface.changeChannels: removed pca["+dg+"]["+j+"] "+pca[dg][j]);
		    pca[dg][i]=null;
		    map[dg].removeChannel(map[dg].findChannel(cha[i].name));
		}
	    }
	} //end else
//# ifndef RTREQUEST
	if (runMode==RunModeDefs.realTime) setRTReqMode(true);
//# endif
	plotsContainer.invalidate(); //force layout of plots container
	plotsContainer.validate();
	plotsContainer.repaint();
    } //end changeChannel method

    /********************
     *
     *  changeLayout()
     *
     *******************/
    // changeLayout method - handles configuration save/load, plot/table mode,
    // and rbnb open/refresh/close tasks
    private void changeLayout(int layout) {
		
	switch(layout) {
	case LayoutCubby.LoadConfig:
	    //EMF 3/23/04: config loaded from file, grab from configCubby
	    Hashtable ht=configCubby.getHash(); //blocks until ht is available
	    if (ht!=null) RBNBApplyConfig(ht);
	    configCubby.setChannel(new Channel("foo")); //tell RBNBPlotMain to continue
	    /*
	    configCubby.setChannels(RBNBConfigChannels());
	    Channel configChan = configCubby.getChannel();
	    if (configChan==null) {
		configCubby.setHash(null);
		break;
	    }
	    byte[] ud=configChan.channelUserData;
	    Hashtable ht=null;
	    if (ud!=null) {
		ht=(new KeyValueHash(ud)).getHash();
		// clear existing channels, then load new ones from config info
		RBNBApplyConfig(ht);
	    }
	    // lastly, send config back to RBNBPlot to make other changes
	    configCubby.setHash(ht);
	    */
	    break;
	    
	case LayoutCubby.SaveConfig:
	    Hashtable configHash=configCubby.getHash();
	    //EMF 3/23/04: add durations to hashtable
	    for (int i=0;i<environment.DISPLAYGROUPS;i++) {
		configHash.put("duration["+i+"]",Double.toString(duration[i].getDoubleValue()));
	    }
	    configCubby.setHash(configHash);
	    configCubby.setChannel(new Channel("foo")); //tell RBNBPlotMain done
	    /*
	    configCubby.setChannels(RBNBConfigChannels());
	    Channel chan = configCubby.getChannel();
	    if (chan==null) break;
	    //create byte array from hashtable
	    Enumeration keys=configHash.keys();
	    Enumeration elements=configHash.elements();
	    String userdata=new String();
	    while (keys.hasMoreElements())
		userdata=userdata.concat((String)keys.nextElement()+"="+(String)elements.nextElement()+",");
	    byte[] configBytes=userdata.getBytes();
	    RBNBSaveConfig(chan,configBytes);
	    */
	    break;
		
	case LayoutCubby.OpenRBNB:
	    boolean stat=false;
	    RBNBClose();
	    stat=RBNBOpen(environment.HOST,environment.PORT);
	    layoutCubby.setStatus(stat);
		//EMF 9/7/05: continue only if open succeeded
	    if (stat) {
			RBNBChannel();
			RBNBTimeLimits();
			RBNBDuration();
		}
	    break;
	    
	case LayoutCubby.RefreshRBNB:
	    //System.out.println("RBNBInterface.changeLayout: RefreshRBNB");
	    RBNBChannel();
	    RBNBTimeLimits();
	    //if (!RBNBChannel() || !RBNBTimeLimits())
	    //System.out.println("RBNBInterface.changeLayout: RBNB refresh failed.");
	    break;
	    
	case LayoutCubby.CloseRBNB:
	    //System.out.println("RBNBInterface.changeLayout: CloseRBNB");
	    RBNBClose();
	    break;
	    
	case LayoutCubby.PlotMode:
	    //System.out.println("RBNBInterface.changeLayout: PlotMode");
	    plotsContainer.setDisplayMode(LayoutCubby.PlotMode);
	    newData=false;
	    break;
	    
	case LayoutCubby.TableMode:
	    //System.out.println("RBNBInterface.changeLayout: TableMode not implemented");
	    plotsContainer.setDisplayMode(LayoutCubby.TableMode);
	    newData=false;
	    break;
	    
	    // EMF 9/8/99: added Export capability - see ExportData.java for details
		// EMF 2/7/05: disable, note new constructor for ExportData so
		//             changes will be needed to re-enable
	case LayoutCubby.ExportToCB:
	    System.err.println("rbnbPlot: Export to Clipboard is disabled");
	    // JPW 4/19/2000: Figure out which mode to use
	    /*
	     *
	    int mode = ExportData.EXPORT_TO_CLIPBOARD;
	    if(environment.EXPORT != null) {
		mode = ExportData.EXPORT_TO_DATABASE;
	    }
	    //ed = new ExportData(rbnbPlotMain.frame,map[dg],mode,cha,environment);
	    d.show();
	    ed.dispose();
	    *
	    */
	    break;
	
	// JPW 4/19/2000: added "Export to DataTurbine" capability - see
	//                ExportData.java for details
	// EMF 2/7/05: new constructor for ExportData, which takes connection
	//             so ExportData can work with V2 data structures instead
	//             of converting back from V1
	// JPW 11/29/2006: Use ExportToDT to export data
	case LayoutCubby.ExportToDT:
	    /*
	     * Here is the previous way we used to export; Source name was static
	     *
	    if (ed==null) ed = new ExportData(rbnbPlotMain.frame);
	    ed.export(
		connection,
		ExportData.EXPORT_TO_DATATURBINE,
		environment);
	    */
	    // JPW 11/29/2006: Use ExportToDT to export data
	    if (connection != null) {
		if (exportToDT == null) {
		    // Create non-modal ExportToDT dialog to handle exporting data to the RBNB
		    exportToDT =
			new ExportToDT(
			    rbnbPlotMain.frame,
			    false,
			    connection,
			    environment);
		}
		exportToDT.setVisible(true);
	    }
	    break;
	//EMF 9/4/07: added Export to Matlab
	case LayoutCubby.ExportToMatlab:
	    // JPW 11/29/2006: Use ExportToDT to export data
	    if (connection != null) {
		//if (exportToMatlab == null) {
		    // Create non-modal ExportToDT dialog to handle exporting data to the RBNB
		    exportToMatlab =
			new ExportToMatlab(
			    rbnbPlotMain.frame,
			    false,
			    connection,
			    environment);
		//}
		exportToMatlab.setVisible(true);
	    } 
	    break;	    
	default:
	    System.out.println("RBNBInterface.changeLayout: unknown layout "+layout);
	    break;
	}
    }//end changeLayout method

    /********************
     *
     *  oneDataStep()
     *
     *******************/
   // oneDataStep method - acquires and plots one data chunk,
   // based on runMode
    private void oneDataStep() {
	int retData = 0;
	Time oldStart = null;
	Time newStart = null;
	//System.out.println("RBNBInterface.oneDataStep: start - newData="+newData+"   runMode="+runMode);
	//if (!newData) System.out.println("RBNBInterface.oneDataStep top: waiting for newData");
	if (!newData) {
	    switch(runMode) {
		
	    case RunModeDefs.bof:
		newStart=getExtremeData(false);
		if (newStart!=null) {
		    start=newStart;
		    position[0]=start;
		    position[1]=position[0];
		    if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
			position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		break;
		
	    case RunModeDefs.revPlay:
		oldStart=start;
		// EMF 9/30/99: recheck endpoints if go past current knowledge
		if (start.compareTo(position[1])<=0 && start.addTime(duration[dg]).compareTo(position[2])>=0) {
		    resetPosition(false);
		    if (start.compareTo(position[1])<=0 && start.addTime(duration[dg]).compareTo(position[2])>=0) {
			runMode=RunModeDefs.stop;
			runModeCubby.set(RunModeDefs.stop,false);
		    }
		    else if (start.compareTo(position[1])<=0) {
			start=position[1];
			runMode=RunModeDefs.stop;
			runModeCubby.set(RunModeDefs.stop,false);
		    }
		}
		else { //move start time and check again
		    //start=start.subtractTime(duration[dg].multiplyTime(speed));
			double localdur=duration[dg].getDoubleValue();
			if (localdur==0) localdur=zerodurbase;
		    start=start.subtractTime(new Time(localdur*speed.getDoubleValue()));
		    if (start.compareTo(position[1])==-1) {
			resetPosition(false);
			//if still before bof, jump there and stop
			if (start.compareTo(position[1])==-1) {
			    start=new Time(position[1]);
			    runMode=RunModeDefs.stop;
			    runModeCubby.set(RunModeDefs.stop,false);
			}
		    }
		}
		retData=getData(false);
		/* EMF 9/29/99: let play run forever
		   while (speedExp>=0 && retData==0) {
		   decrementSpeed();
		   start=oldStart.subtractTime(new Time(duration[dg].getDoubleValue()*speed.getDoubleValue()));
		   retData=getData(false);
		   }
		   if (retData!=0)   //got some data, update sliders
		*/ if (true) {
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
			position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		else {  //got no data, back up to last position and stop
		    //start=start.addTime(duration[dg].multiplyTime(speed));
		    start=start.addTime(new Time(duration[dg].getDoubleValue()*speed.getDoubleValue()));
		    runMode=RunModeDefs.stop;
		    runModeCubby.set(RunModeDefs.stop,false);
		}
		if (retData==-1) { //start time was adjusted, so at gap or bof, so stop
		    /* EMF 9/29/99 - let play run forever
		       runMode=RunModeDefs.stop;
		       runModeCubby.set(RunModeDefs.stop,false);
		    */
		}
		break;
		
	    case RunModeDefs.revStep:
		start=start.subtractTime(duration[dg]);
		retData=getData(false);
		if (retData!=0) { //got some data, update sliders
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
			position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		/* EMF 9/29/99 - don't adjust endpoints
		   else start=start.addTime(duration[dg]); //got no data, back up to last position
		*/
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		break;
		
	    case RunModeDefs.stop:
		break;
		
	    case RunModeDefs.fwdStep:
		start=start.addTime(duration[dg]);
		retData=getData(true);
		if (retData!=0) { //got data, update sliders
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
			position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		/* EMF 9/29/99 - don't adjust endpoints
		   else start=start.subtractTime(duration[dg]); //got no data, back up
		*/
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		break;
		
	    case RunModeDefs.fwdPlay:
		oldStart=start;
		// EMF 10/1/99: recheck endpoints if go past current knowledge
		if (start.compareTo(position[1])<=0 && start.addTime(duration[dg]).compareTo(position[2])>=0) {
		    resetPosition(false);
		    if (start.compareTo(position[1])<=0 && start.addTime(duration[dg]).compareTo(position[2])>=0) {
			runMode=RunModeDefs.stop;
			runModeCubby.set(RunModeDefs.stop,false);
		    }
		    else if (start.addTime(duration[dg]).compareTo(position[2])>=0) {
			start=position[2].subtractTime(duration[dg]);
			runMode=RunModeDefs.stop;
			runModeCubby.set(RunModeDefs.stop,false);
		    }
		}
		else { //move start time and check again
		    //start=start.subtractTime(duration[dg].multiplyTime(speed));
			double localdur=duration[dg].getDoubleValue();
			if (localdur==0) localdur=zerodurbase;
		    start=start.addTime(new Time(localdur*speed.getDoubleValue()));
		    if (start.addTime(duration[dg]).compareTo(position[2])==1) {
			resetPosition(false);
			//if still after eof, jump there and stop
			if (start.addTime(duration[dg]).compareTo(position[2])==1) {
			    start=position[2].subtractTime(duration[dg]);
			    runMode=RunModeDefs.stop;
			    runModeCubby.set(RunModeDefs.stop,false);
			}
		    }
		}
		retData=getData(true);
		/* EMF 9/29/99: let play run forever
		   while (speedExp>=0 && retData==0) {
		   decrementSpeed();
		   //start=oldStart.addTime(duration[dg].multiplyTime(speed));
		   start=oldStart.addTime(new Time(duration[dg].getDoubleValue()*speed.getDoubleValue()));
		   retData=getData(true);
		   }
		   if (retData!=0)  //got data, update sliders
		*/ if (true) {
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
			position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		else { //got no data, back up and set runMode to stop
		    //start=start.subtractTime(duration[dg].multiplyTime(speed));
		    start=start.subtractTime(new Time(duration[dg].getDoubleValue()*speed.getDoubleValue()));
		    runMode=RunModeDefs.stop;
		    runModeCubby.set(RunModeDefs.stop,false);
		}
		if (retData==-1) { //start adjusted, so at gap or eof, so stop
		    /* EMF 9/29/99: let play run forever
		       runMode=RunModeDefs.stop;
		       runModeCubby.set(RunModeDefs.stop,false);
		    */
		}
		break;
		
	    case RunModeDefs.eof:
		newStart=getExtremeData(true);
		if (newStart!=null) {
		    start=newStart;
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		break;
		
	    case RunModeDefs.realTime:
//System.err.println("RBNBInterface.oneDataStep: rtWaittime "+rtWaitTime);
		newStart=getExtremeData(true);
//System.err.println("RBNBInterface.oneDataStep: newStart "+newStart.getDoubleValue());
		/* INB 08/14/1998
		   Need to check for a null pointer here. */
		if ((newStart != null) && (!firstRT) &&
		    (newStart.compareTo(start)==0)) { //repeat data; hang out and do not replot or include in update rate
		    rtWaitTime *= 2;
		    try {
			Thread.sleep(rtWaitTime);
		    }
		    catch (InterruptedException e) {
			System.out.println("thread sleep error: "+e);
		    }
		}
		else if (newStart!=null) {
		    start=newStart;
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		    rtWaitTime /= 2;
		}
		else { //newStart==null
		    try {
			Thread.sleep(75); // no data received, so sleep a bit to limit CPU usage
		    }
		    catch (InterruptedException e) {
			System.out.println("thread sleep error: "+e);
		    }
		}
		firstRT=false;
		// keep rtWaitTime withing sane bounds - note actual wait will be twice this
		if (rtWaitTime < 10) rtWaitTime = 10;
		else if (rtWaitTime > 640) rtWaitTime = 640;
//System.err.println("RBNBInterface: rtWaitTime "+(2*rtWaitTime));
		// if set on command line, do extra sleep now
		if (environment.RTWAIT>0) {
		    try { Thread.sleep(environment.RTWAIT); }
		    catch (InterruptedException e) {
			System.err.println("thread sleep error: "+e);
		    }
		}
		break;
		
	    case RunModeDefs.allData:
		getAllData();  //this method sets new position/duration values in cubby
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		newData=false;
		setUpdateRate();
		break;
		
	    case RunModeDefs.current:
		retData=getData(true);
		//if (retData==-1)  //start adjusted, update sliders
		if (retData!=0) { //got some data, update sliders
		    position[0]=start;
		    if (position[0].compareTo(position[1])==-1) position[1]=position[0];
		    if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
			position[2]=position[0].addTime(duration[dg]);
		    newData=true;
		    setUpdateRate();
		    //posDurCubby.setPosition(position,false);
		}
		newData=true; //want to clear screen if no data in current mode
		//EMF 10/14/99: check if user has requested new run mode.  Set runmode to
		// the new request or stop if no new request
		Integer newRunMode=runModeCubby.get(false);
		if (newRunMode!=null) {
		runMode=newRunMode.intValue();
		} else {
		    runMode=RunModeDefs.stop;
		}
		runModeCubby.set(runMode,false);
		break;
		
	    default:
		System.out.println("runMode not implemented: "+runMode);
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		break;
		
	    } // end switch(runMode)
	} // end if (!newData)
	//System.out.println("RBNBInterface.oneDataStep: end of case - newData="+newData);  
	//try { Thread.sleep(2000); } catch(InterruptedException e) { System.out.println("BURP interrupted!"); }    
	// send data to plotcontainers if all repaints are done
      if (newData) {
	  PlotContainer[] pc=new PlotContainer[channel.length];
	  for (int i=0;i<channel.length;i++) {
	      pc[i]=findPC(channel[i].channelName);
				//pc[i]=pca[dg][i];
/* EMF 2/18/02: seems to loop on MJM machine - disable for now
	      if (pc[i]!=null && !pc[i].hasBeenPainted()) {
		  //sleazy workaround for a bug - the waiting never ends unless a repaint is called from here.  I
		  //wait for 5 times, then force a repaint.  It's unclear why the plotcontainer gets in a state
		  //of having not been painted even though data has been sent...
		  System.out.println("RBNBInterface.oneDataStep: waiting for repaint on "+i+" - newData="+newData);
		  if (i==repaintContainer) {
		      if (repaintContainerCount>5) {
			  pc[i].repaint();
			  repaintContainerCount=0;
			  //System.out.println("RBNBInterface.oneDataStep: called repaint on "+i);
		      }
		      else repaintContainerCount++;
		  }
		  else {
		      repaintContainer=i;
		      repaintContainerCount=1;
		  }
		  return;
	      }
*/
	  }
	  repaintContainer=-1; //reset
	  //System.out.println("RBNBInterface.oneDateStep: not waiting for repaint - newData="+newData);
	  if (updatePosition) posDurCubby.setPosition(position,false);
	  else updatePosition=true;
	  for (int i=0;i<channel.length;i++)
	      if (pc[i]!=null) pc[i].setChannelData(channel[i],position[0]);
	  newData=false;
      }
    } //end oneDataStep method
    
    /********************
     *
     *  setUpdateRate()
     *
     *******************/
    //setUpdateRate method - computes update rate by simple heuristic (uses exponential averaging
    // if rate is > 1, just most recent sample otherwise - and sends appropriate string to
    // cubbyhole for UserControl to read and display
    private void setUpdateRate() {
	long thisUpdate=(new Date()).getTime();
	
	if ((thisUpdate-lastUpdate)>1000 || updatePeriod==0 || updatePeriod>1000) { // don't do any averaging
	    updatePeriod = thisUpdate-lastUpdate;
	}
	else { //apply exponential average
	    updatePeriod = ( (thisUpdate-lastUpdate) + 7*updatePeriod)/8;
	}
	lastUpdate=thisUpdate;
	try {
	    posDurCubby.setUpdateRate(ToString.toString("%.2g",1000.0/updatePeriod)+" Updates/Sec");
	    //System.out.println("RBNBInterface.setUpdateRate: "+
	    //ToString.toString("%.2g",updateRate)+" Updates/Sec");
	}
	catch (Exception e) {
	    System.out.println("RBNBInterface.setUpdateRate exception");
	    e.printStackTrace();
	}
    }
    
    /********************
     *
     *  getExtremeData()
     *
     *******************/
    public Time getExtremeData(boolean atEOF) {
//System.err.println("getExtremeData top: connection "+connection+", map "+map[dg]+", chans "+map[dg].channelList().length);
	Time newStart=null;
	if (map[dg].channelList().length>0 && connection!=null) {
	    try {
//# ifndef RTREQUEST
		if (rtReqMode) {
		    //EMF 8/30/99: allow for null return without losing channel info...
		    Map lmap=connection.streamGetMap();
		    if (lmap==null) {
			rtTimeout=true;
			setRTReqMode(false);
		    }
		    else {
			//EMF 3/28/00: don't copy lmap into map[dg] unless 
			// contains reasonable stuff - on timeout, the map is empty
			Channel[] ch=lmap.channelList();
			if (ch==null || ch.length<1 || 
			      ch[0].numberOfPoints==0 || 
			      ch[0].timeStamp==null) {
                            if (environment.SLAVEMODE) {
                              rtTimeout=false;
                              setRTReqMode(true);
                              return null;
                            } else {
			      rtTimeout=true;
			      setRTReqMode(false);
                            }
			}
			else {
			    map[dg]=lmap;
			}
		    }
		}
		if (!rtReqMode) {
//# endif
		    if (atEOF) {
			connection.getMap(map[dg],null,duration[dg],
					  flags | DataRequest.newest | DataRequest.aligned);
		    }
		    else connection.getMap(map[dg],null,duration[dg],
					   flags | DataRequest.oldest | DataRequest.aligned);
//# ifndef RTREQUEST
		}
//# endif
		
		channel=map[dg].channelList();
		// set timeStamp format
		for (int i=0;i<channel.length;i++) {
		    if (channel[i].timeStamp!=null) {
			posDurCubby.setTimeFormat(channel[i].timeStamp.format);
			i=channel.length;
		    }
		}
		//if EOF, reduce start time so data appears on right of plot
		if (atEOF) {
		    Time last=null;
		    Time newlast=null;
		    for (int i=0;i<channel.length;i++) {
			if (channel[i].numberOfPoints>0) {
			    newlast=channel[i].timeStamp.getEndOfInterval(DataTimeStamps.last);
			    if (last==null) last=newlast;
			    else if (newlast.compareTo(last)==1) last=newlast;
			    //EMF 2/28/00: changed to pick latest of the endpoints
			    //else if (newlast.compareTo(last)==-1) last=newlast;
			}
		    }
		    if (last==null) newStart=last;
		    else newStart=last.subtractTime(duration[dg]);
		}
		else { //if BOF, determine real start time so data appears on left of plot
		    Time first=null;
		    Time newfirst=null;
		    for (int i=0;i<channel.length;i++) {
			if (channel[i].numberOfPoints>0) {
			    newfirst=channel[i].timeStamp.getStartOfInterval(0);
			    if (first==null) first=newfirst;
			    else if (newfirst.compareTo(first)==-1) first=newfirst;
			    //EMF 2/28/00: changed to pick earliest of the startpoints
			    //else if (newfirst.compareTo(first)==1) first=newfirst;
			}
		    }
		    newStart=first;
		}
	    }
	    catch (Exception e) {
		System.out.println("RBNB data exception: "+e);
		e.printStackTrace();
		runMode=RunModeDefs.stop;
//# ifndef RTREQUEST
		if (rtReqMode) setRTReqMode(false);
//# endif
		runModeCubby.set(RunModeDefs.stop,false);
		return null;
	    }
	 /*for (int i=0;i<channel.length;i++) {
	   //System.out.println(channel[i].channelName+"; 
	   //numberOfPoints "+channel[i].numberOfPoints+"; 
	   //pointSize  "+channel[i].pointSize);
	   PlotContainer pc=findPC(channel[i].channelName);
	   if (pc!=null) pc.setChannelData(channel[i],newStart);
	   } */
	}
	else { // no channels selected or no connection, set run mode to stop
	    //runMode=RunModeDefs.stop;
	    //System.err.println("no channels in map; no data requested");
	    //System.err.println("mode set to pause");
	}
	return newStart;
    }  // end getExtremeData method

    /********************
     *
     *  getData()
     *
     *******************/
   // getData method - gets data from RBNB, sends to plotcontainers
   // returns 0 if no data, -1 if data with time adjustment, 1 if
   // data with no time adjustment
   // modifies start time if needed to get full duration's worth of data
    public int getData(boolean fwdDirection) {
	boolean gotData=false,adjusted=false;
	
	if (map[dg].channelList().length>0 && connection!=null) {
	    try {
		connection.getData(map[dg],start,duration[dg],flags);
//System.out.println("RBNBInterface.getData: start="+start+"   duration="+duration[dg]);
		channel=map[dg].channelList();
		for (int i=0;i<channel.length;i++) {
		    if (channel[i].numberOfPoints>0) {
			posDurCubby.setTimeFormat(channel[i].timeStamp.format);
			i=channel.length;
		    }
		}
		Time first=null,last=null,time=null;
		for (int i=0;i<channel.length;i++) {
		    if (channel[i].numberOfPoints>0) {
			time=channel[i].timeStamp.getStartOfInterval(DataTimeStamps.first);
			if (!gotData) first=time;
			else if(time.compareTo(first)==-1) first=time;
			time=channel[i].timeStamp.getEndOfInterval(DataTimeStamps.last);
			if (!gotData) {
			    gotData=true;
			    last=time;
			}
			else if (time.compareTo(last)==1) last=time;
		    }
		}
		if (!gotData) return 0; //no data
		/* EMF 9/29/99: let play run forever
		   if (fwdDirection) { //make sure data ends near start+duration
		     if (start.addTime(duration[dg]).subtractTime(last).compareTo(duration[dg].divideBy(20))==1) {
		       start=last.subtractTime(duration[dg]);
		       connection.getData(map[dg],start,duration[dg],flags);
		       channel=map[dg].channelList();
		       for (int i=0;i<channel.length;i++) {
		         if (channel[i].timeStamp!=null) {
		           posDurCubby.setTimeFormat(channel[i].timeStamp.format);
		           break;
		         }
		       }
		      adjusted=true;
		      gotData=false;
		     }
		   }
		   else { //make sure data begins near start
		     if (first.subtractTime(start).compareTo(duration[dg].divideBy(20))==1) {
		       start=first;
		       connection.getData(map[dg],start,duration[dg],flags);
		       channel=map[dg].channelList();
		       for (int i=0;i<channel.length;i++) {
		         if (channel[i].timeStamp!=null) {
		           posDurCubby.setTimeFormat(channel[i].timeStamp.format);
		           break;
		         }
		       }
		       adjusted=true;
		       gotData=false;
		     }
		   }
		*/
	    }
	    catch (Exception e) {
		System.out.println("RBNB data exception: "+e);
		e.printStackTrace();
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		return 0;
	    }
	    if (!gotData) for (int i=0;i<channel.length;i++) {
		if (!gotData) if (channel[i].numberOfPoints>0) gotData=true;
	    }
	    if (!gotData) return 0;
	    /*for (int i=0;i<channel.length;i++) {
	      PlotContainer pc=findPC(channel[i].channelName);
	      if (pc!=null) pc.setChannelData(channel[i],start);
	      } */
	}
	else { // no channels selected or no connection
	    return 0;
	}
	if (adjusted) return -1;
	else return 1;
    }  // end getData method
   
    /********************
     *
     *  getAllData()
     *
     *******************/
   // getAllData method - retrieves full range of data for all channels
    private void getAllData() {
	Time dur=null; //duration of longest interval
	if (map[dg].channelList().length>0 && connection!=null) {
	    try {
		connection.getTimeLimits(map[dg]);
		channel=map[dg].channelList();
		Time first=null;
		Time last=null;
		Time newtime=null;
		for (int i=0;i<channel.length;i++) {
		    if (channel[i].timeStamp!=null) {
			newtime=channel[i].timeStamp.getStartOfInterval(DataTimeStamps.first);
			if (first==null) first=newtime;
			else if (newtime.compareTo(first)==-1) first=newtime;
			newtime=channel[i].timeStamp.getEndOfInterval(DataTimeStamps.last);
			if (last==null) last=newtime;
			else if (newtime.compareTo(last)==1) last=newtime;
		    }
		}
		if (first==null || last==null) { //no data
		    System.out.println("RBNBInterface.getAllData: no data available!");
		    runMode=RunModeDefs.stop;
		    runModeCubby.set(RunModeDefs.stop,false);
		    return;
		}
		dur=last.subtractTime(first);
		connection.getData(map[dg],first,dur,flags);
		channel=map[dg].channelList();
		for (int i=0;i<channel.length;i++) {
		    if (channel[i].numberOfPoints>0) {
			posDurCubby.setTimeFormat(channel[i].timeStamp.format);
			i=channel.length;
		    }
		}
		//set new start and duration
		start=first;
		duration[dg]=dur;
		position[0]=start;
		position[1]=position[0];
		position[2]=last;
		posDurCubby.setPosition(position,false);
		posDurCubby.setDuration(duration[dg],false);
	    }
	    catch (Exception e) {
		System.out.println("RBNB data exception: "+e);
		e.printStackTrace();
		runMode=RunModeDefs.stop;
		runModeCubby.set(RunModeDefs.stop,false);
		return;
	    }
	    for (int i=0;i<channel.length;i++) {
		//System.out.println(channel[i].channelName+"; 
		//numberOfPoints "+channel[i].numberOfPoints+"; pointSize  "+channel[i].pointSize);
		PlotContainer pc=findPC(channel[i].channelName);
		if (pc!=null) {
		    //System.out.println("pc "+pc+"  set Abscissa "+dur+"   set ChanData");
		    pc.setAbscissa(duration[dg]);
		    pc.setChannelData(channel[i],start);
		}
	    }
	}
	else { // no channels selected or no connection, set run mode to stop
	    //runMode=RunModeDefs.stop;
	    //System.out.println("no channels in map; no data requested");
	    //System.out.println("mode set to pause");
	}
    } //end getAllData method  
   
    /********************
     *
     *  resetPosition()
     *
     *******************/
   // resetPosition method - finds endpoints of data, adjusts position and start
    public void resetPosition(boolean adjustStart) {
		Time bof=null,eof=null;
	if (map[dg].channelList().length>0 && connection!=null) {
	    try {
//# ifndef RTREQUEST
		if (rtReqMode) setRTReqMode(false);
//# endif
		connection.getTimeLimits(map[dg]);
//# ifndef RTREQUEST
		if (runMode==RunModeDefs.realTime) setRTReqMode(true);
//# endif
		channel=map[dg].channelList();
		Time newtime=null;
		for (int i=0;i<channel.length;i++) {
		    if (channel[i].timeStamp!=null) {
			newtime=channel[i].timeStamp.getStartOfInterval(DataTimeStamps.first);
			if (bof==null) bof=newtime;
			else if (newtime.compareTo(bof)==-1) bof=newtime;
			newtime=channel[i].timeStamp.getEndOfInterval(DataTimeStamps.last);
			if (eof==null) eof=newtime;
			else if (newtime.compareTo(eof)==1) eof=newtime;
		    }
		}
		if (bof==null || eof==null) return;
		position[1]=bof;
		position[2]=eof;
		if (position[0].compareTo(position[1])==-1) position[0]=position[1];
		if (position[0].addTime(duration[dg]).compareTo(position[2])==1)
		    position[0]=position[2].subtractTime(duration[dg]);
		if (adjustStart) start=position[0];
		posDurCubby.setPosition(position,false);
	    }
	    catch (Exception e) {
		System.out.println("RBNBInterface.init: caught "+e);
		e.printStackTrace();
	    }
	}
	else { //no channels in map or no connection - leave position alone
	    //System.out.println("RBNBInterface.resetPosition: no channels!");
	}
    } //end resetPosition method
    
    // findPC method - returns plotcontainer matching supplied name
    private PlotContainer findPC(String chanName) {
	for (int i=0;i<cha.length;i++) {
	    if (chanName.equals(cha[i].name)) return pca[dg][i];
	}
	// channel not found
	System.out.println("RBNBInterface.findPC: channel not found: "+chanName);
	return(null);
    }
       
    /********************
     *
     *  incrementSpeed()
     *
     *******************/
    private void incrementSpeed(boolean reset) {
	if (reset) {
	    speedMant=2;
	    speedExp=-2;
	}
	else {
	    if (speedMant==1) speedMant=2;
	    else if (speedMant==2) speedMant=5;
	    else {
		speedMant=1;
		speedExp+=1;
	    }
	}
	speed=new Time(speedMant,speedExp);
    }
    
    /********************
     *
     *  decrementSpeed()
     *
     *******************/
    private void decrementSpeed() {
	if (speedMant==5) speedMant=2;
	else if (speedMant==2) speedMant=1;
	else {
	    speedMant=5;
	    speedExp-=1;
	}
	speed=new Time(speedMant,speedExp);
    }
    
} //end class RBNBInterface
