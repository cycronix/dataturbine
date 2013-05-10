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
  ***   Name :  Handler.java                                    ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August - September, 2000                        ***
  ***                                                           ***
  ***   Copyright 2000, 2002, 2003, 2004 Creare Inc.            ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This file contains the Handler class, which is a     ***
  ***   JMF data sink/RBNB data source that takes JMF data and  ***
  ***   sends it to the RBNB DataTurbine.                       ***
  ***                                                           ***
  ***   Modification History :                                  ***
  ***      2005/03/23 - WHF                                     ***
  ***           Now uses the new PutUserInfo ChannelMap method. ***
  ***      08/24/2004 - INB                                     ***
  ***           Separate the time information to per channel    ***
  ***           values.                                         ***
  ***      08/18/2004 - INB                                     ***
  ***           Separate channels into different maps.          ***
  ***      08/17/2004 - INB                                     ***
  ***           Check for empty channels when putting data.     ***
  ***      04/17/2003 - INB                                     ***
  ***           Changed the default timestamping mode to be     ***
  ***           system clock time rather than JMF time.         ***
  ***      02/12/2003 - INB                                     ***
  ***           Added handling of archive mode.                 ***
  ***      02/03/2003 - INB                                     ***
  ***           Added handling of frame rate. It appears that   ***
  ***           JMF may not always give the user their          ***
  ***           requested rate, so we try to adjust for it.     ***
  ***      08/13/2002 - 09/25/2002 INB ---> V2 <---             ***
  ***           Ported to V2 RBNB.                              ***
  ***                                                           ***
  ***      08/13/2002 - INB                                     ***
  ***           Allow for the input encoding type to determine  ***
  ***           the name of the channel.                        ***
  ***      04/01/2002 - INB                                     ***
  ***           Modified to use an absolute time stamp rather   ***
  ***           than a relative one.  The code saves the        ***
  ***           absolute start time in the static user data so  ***
  ***           that the display can substract it out.          ***
  ***                                                           ***
  *****************************************************************
*/
package com.rbnb.media.datasink.protocol;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;

import java.awt.Dimension;

import java.io.IOException;

import java.util.Vector;

import javax.media.Buffer;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.MediaLocator;

import javax.media.IncompatibleSourceException;

import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;

import javax.media.format.AudioFormat;
import javax.media.format.H261Format;
import javax.media.format.H263Format;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceStream;

public class Handler
    implements DataSink,
               BufferTransferHandler
{

  // Private fields:
  private long                  absoluteStartTime = Long.MIN_VALUE;
  private int                   audioChannels = 0;
  private Vector                buffersWaiting[] = null;
  private String[]              channels = null;
  private Format[]              formats = null;
  private long                  framesSoFar = 0;
  private int[]                 lastKeyFrame = null;
  /* 2004/12/29  WHF  Cleanup of time system.
  private double[]              lastJMTime = null;
  private double[]              lastStartTime = null;
  */
  private double[] lastFrameTime;
  private double lastFrameTimeStamp, greatestFrameTimeStamp;
  private Vector                listeners = new Vector(1);
  private MediaLocator          locator = null;
  private PullBufferStream[]    pulling = null;
  private PullThread[]          pullThreads = null;
  private PushBufferStream[]    pushing = null;
  private Source                rbnbSource = null;
  private Buffer                readBuffer[] = null;
  private boolean               registered = false;
  private double                requestedRate = 0.;
  /* 2004/12/29  WHF  Cleanup of time system.
  private double                rolloverGuess = 4294.967296;
  private double                rolloverTime = 0.;
  */
  private DataSource            source = null;
  private Integer               synchLock = new Integer(0);
  private SourceStream[]        unfinished = null;
  private boolean               useEncoding = false;
  private String[]              userData = null;
  //private boolean             useWallClock = true;
  private int                   videoChannels = 0;
  private long                  wallClockBegan = 0;
  private ChannelMap            channelMap = null;

    private boolean gotAudio=false;
    private boolean gotVideo=false;
    private boolean doneFlush=true;   // start as if just did flush


/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  addDataSinkListener                             ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method adds a data sink listener to this        ***
  ***   data sink.                                              ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      listenerI            The new listener.               ***
  ***                                                           ***
  *****************************************************************
*/
  public final void addDataSinkListener(DataSinkListener listenerI) {
    if (listenerI != null) {
      listeners.addElement(listenerI);
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  close                                           ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method closes this data sink. It stops the      ***
  ***   source, kills the pull threads (if any), and detaches   ***
  ***   from the DataTurbine server.                            ***
  ***                                                           ***
  ***   Modification History :                                  ***
  ***      08/13/2002 - INB ---> V2 <---                        ***
  ***           Ported to V2 RBNB.                              ***
  ***                                                           ***
  *****************************************************************
*/
  public final void close() {
    try {

      // Stop any active processing.
      stop();

      // Terminate the pulling threads.
      if (pullThreads != null) {
        for (int idx = 0; idx < pullThreads.length; ++idx) {
          pullThreads[idx].terminateProcessing();
        }
      }

      // Detach from the DataTurbine.
      if (rbnbSource != null) {
	// 2006/01/05  WHF  Detach, maintaining the archive for playback. 
        //rbnbSource.CloseRBNBConnection();
	rbnbSource.Detach();
        rbnbSource = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getContentType                                  ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method returns the source content type, if any. ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      getContentType       The source content type.        ***
  ***                                                           ***
  *****************************************************************
*/
  public final String getContentType() {
    return ((source == null) ? null : source.getContentType());
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getControl                                      ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method returns the control object specified     ***
  ***   by the control type - always null.                      ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      controlTypeI         The control type desired.       ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      getControl           Always null (not supported).    ***
  ***                                                           ***
  *****************************************************************
*/
  public final Object getControl(String controlTypeI) {
    return (null);
  }
/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getControls                                     ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method returns an array of the control objects  ***
  ***   for this data sink. There are none.                     ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      getControls          Always a 0 length array.        ***
  ***                                                           ***
  *****************************************************************
*/
  public final Object[] getControls() {
    return (new Object[0]);
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getOutputLocator                                ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method returns the output media locator for     ***
  ***   this data sink.                                         ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      getOutputLocator     The output media locator.       ***
  ***                                                           ***
  *****************************************************************
*/
  public final MediaLocator getOutputLocator() {
    return (locator);
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getUseEncoding                                  ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2002                                    ***
  ***                                                           ***
  ***   Copyright 2002 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method returns the use encoding flag.           ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      getUseEncoding       Use the encoding type to set    ***
  ***                           the output channel name?        ***
  ***                                                           ***
  *****************************************************************
*/
  public final boolean getUseEncoding() {
    return (useEncoding);
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getUseWallClock                                 ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  April, 2003                                     ***
  ***                                                           ***
  ***   Copyright 2003 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method returns the use wall clock flag.         ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      getUseWallClock      Use the wall clock rather than  ***
  ***                           JMF clock?                      ***
  ***                                                           ***
  *****************************************************************
*/
//  public final boolean getUseWallClock() {
//    return (useWallClock);
//  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  getRequestedRate                                ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  February, 2003                                  ***
  ***                                                           ***
  ***   Copyright 2003 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      Gets the requested frame rate.                       ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      The requested frame rate.                            ***
  ***                                                           ***
  *****************************************************************
*/
  public final double getRequestedRate() {
    return (requestedRate);
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  open                                            ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000, 2002, 2003, 2004 Creare Inc.            ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method opens the data sink by connecting to     ***
  ***   the DataTurbine.                                        ***
  ***                                                           ***
  ***   Modification History :                                  ***
  ***      08/24/2004 - INB                                     ***
  ***           Set the initial time information.               ***
  ***      02/12/2003 - INB                                     ***
  ***           Added handling of archive mode.                 ***
  ***      08/13/2002 - INB ---> V2 <---                        ***
  ***           Ported to V2 RBNB.                              ***
  ***                                                           ***
  *****************************************************************
*/
  public final void open() throws IOException,SecurityException {

    // Figure out the pieces of the locator.
    String remainder = locator.getRemainder(),
           serverAddress = null,
           dataPath = null,
           archiveMode = "none";
    int    cache = 100,
           archive = 0;

    // The server address is everything up to the first slash.
    int idx;

    for (idx = 0; remainder.charAt(idx) == '/'; ++idx) {}
    remainder = remainder.substring(idx);

    int slash = remainder.indexOf("/");

    // If there is no slash, then the data path is defaulted to whatever
    // the server chooses and the cache and archive use the defaults set
    // above.
    if (slash == -1) {
      serverAddress = remainder;

    } else {
      serverAddress = remainder.substring(0,slash);

      // The cache and archive fields are optional and appear in parenthesis
      // at the end of the data path.
      int endDP = remainder.length();

      if (remainder.charAt(remainder.length() - 1) == ')') {
        endDP = remainder.indexOf("(");

        String ringbuffer = remainder.substring
          (endDP + 1,
           remainder.length() - 1);

        // The cache and archive sizes are separated by a comma. The cache
        // is before the comma and the archive comes after. If the archive
        // is to default, the archive value and the comma can be omitted.
        // The cache can be defaulted by omitting it - i.e., start with a
        // comma.
        // The archive mode follows the archive size and can be omitted if
        // the archive size is omitted.
        int comma = ringbuffer.indexOf(","),
            secondComma;

        if (comma == -1) {
          cache = Integer.parseInt(ringbuffer);
        } else {
          if (comma > 0) {
            cache = Integer.parseInt(ringbuffer.substring(0,comma));
          }
          if (comma < ringbuffer.length() - 1) {
            secondComma = ringbuffer.indexOf(",",comma + 1);
            if (secondComma == -1) {
              secondComma = ringbuffer.length();
            }
            if (comma < secondComma) {
              archive = Integer.parseInt(ringbuffer.substring
                                         (comma + 1,
                                          secondComma));
            }
            if (secondComma < ringbuffer.length() - 1) {
              archiveMode = ringbuffer.substring(secondComma + 1);
            }
          }
        }
      }

      // The data path is everything between the slash and either the end of
      // the string or the open parenthesis.
      if (endDP > slash + 1) {
        dataPath = remainder.substring(slash + 1,endDP);
      }
    }

    try {
      // Connect to the server.
        rbnbSource = new Source(cache,
                                archiveMode,
                                archive);
        rbnbSource.OpenRBNBConnection(serverAddress,dataPath);

      // Allocate the channels array.
      channels = new String[unfinished.length];
          // 2004/12/29  WHF  Time system cleanup:
      //lastJMTime = new double[unfinished.length];
      //lastStartTime = new double[unfinished.length];
          lastFrameTime = new double[unfinished.length];
          lastFrameTimeStamp = greatestFrameTimeStamp = Double.NEGATIVE_INFINITY;
      for (idx = 0; idx < channels.length; ++idx) {
                channels[idx] = null;
        //lastJMTime[idx] = lastStartTime[idx] = 0.;
      }
      buffersWaiting = new Vector[unfinished.length];
//    } catch (Exception e) {
//      throw new SecurityException(e.getMessage());
//    }
	} catch (com.rbnb.sapi.SAPIException se) { throw new RuntimeException(se); }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  removeDataSinkListener                          ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method removes a data sink listener from this   ***
  ***   data sink.                                              ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      listenerI            The listener to remove.         ***
  ***                                                           ***
  *****************************************************************
*/
  public final void removeDataSinkListener(DataSinkListener listenerI) {
    if (listenerI != null) {
      listeners.removeElement(listenerI);
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  setOutputLocator                                ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sets the output media locator for this   ***
  ***   data sink. The locator should look like:                ***
  ***                                                           ***
  ***   rbnb://<server host:port>/datapath[(cache[,archive])]   ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      locatorI             The output locator.             ***
  ***                                                           ***
  *****************************************************************
*/
  public final void setOutputLocator(MediaLocator locatorI) {
    locator = locatorI;
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  setSource                                       ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sets the media source that this data     ***
  ***   sink should use to obtain its content.                  ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      sourceI              The new source.                 ***
  ***                                                           ***
  *****************************************************************
*/
  public final void setSource(DataSource sourceI)
  throws IncompatibleSourceException {

    // Handle the source based on the type of source.
    if (sourceI instanceof PushBufferDataSource) {
      pushBufferDataSource((PushBufferDataSource) sourceI);
    } else if (sourceI instanceof PullBufferDataSource) {
      pullBufferDataSource((PullBufferDataSource) sourceI);
    } else {
      throw new IncompatibleSourceException();
    }

    // Save the source.
    source = sourceI;
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  setUseEncoding                                  ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2002                                    ***
  ***                                                           ***
  ***   Copyright 2002 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sets the use encoding flag.              ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      useEncodingI         Use the encoding type to set    ***
  ***                           the output channel name?        ***
  ***                                                           ***
  *****************************************************************
*/
  public final void setUseEncoding(boolean useEncodingI) {
      useEncoding = useEncodingI;
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  setUseWallClock                                 ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  April, 2003                                     ***
  ***                                                           ***
  ***   Copyright 2003 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sets the use wall clock flag.            ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      useWallClockI        Use wall clock rather than      ***
  ***                           JMF time?                       ***
  ***                                                           ***
  *****************************************************************
*/
//  public final void setUseWallClock(boolean useWallClockI) {
//      useWallClock = useWallClockI;
//  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  setRequestedRate                                ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  February, 2003                                  ***
  ***                                                           ***
  ***   Copyright 2003 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      Sets the requested frame rate.                       ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      requestedRateI       The requested frame rate.       ***
  ***                                                           ***
  *****************************************************************
*/
  public final void setRequestedRate(double requestedRateI) {
    requestedRate = requestedRateI;
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  pushBufferDataSource                            ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August - September, 2000                        ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sets up to handle a push buffer data     ***
  ***   source. It sets up this object as the buffer transfer   ***
  ***   handler for each of the streams of the source.          ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      sourceI              The push buffer data source.    ***
  ***                                                           ***
  *****************************************************************
*/
  private final void pushBufferDataSource(PushBufferDataSource sourceI) {
    pushing = sourceI.getStreams();
    unfinished = new SourceStream[pushing.length];
    readBuffer = new Buffer[pushing.length];
    formats = new Format[pushing.length];
    userData = new String[pushing.length];

    for (int idx = 0; idx < pushing.length; ++idx) {
      pushing[idx].setTransferHandler(this);
      unfinished[idx] = pushing[idx];
      readBuffer[idx] = new Buffer();
      formats[idx] = null;
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  pullBufferDataSource                            ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sets up to handle a pull buffer data     ***
  ***   source. It sets up a thread to stream data.             ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      sourceI              The pull buffer data source.    ***
  ***                                                           ***
  *****************************************************************
*/
  private final void pullBufferDataSource(PullBufferDataSource sourceI) {
    pulling = sourceI.getStreams();
    unfinished = new SourceStream[pulling.length];
    pullThreads = new PullThread[pulling.length];
    readBuffer = new Buffer[pulling.length];
    formats = new Format[pulling.length];
    userData = new String[pulling.length];

    for (int idx = 0; idx < pulling.length; ++idx) {
      pullThreads[idx] = new PullThread
          (pulling[idx],
           this);
      unfinished[idx] = pulling[idx];
      readBuffer[idx] = new Buffer();
      formats[idx] = null;
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  start                                           ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method starts the source running. If we have    ***
  ***   pull threads, start them as well.                       ***
  ***                                                           ***
  *****************************************************************
*/
  public final void start() {

    // Start the source.
    try {
      source.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Start or restart the pulling threads.
    if (pullThreads != null) {
      for (int idx = 0; idx < pullThreads.length; ++idx) {
        pullThreads[idx].startProcessing();
      }
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  stop                                            ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method stops the source. It also stops the      ***
  ***   processing of the pulling threads.                      ***
  ***                                                           ***
  *****************************************************************
*/
  public final void stop() {

    // Stop the source.
    try {
      source.stop();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Stop the pulling threads.
    if (pullThreads != null) {
      for (int idx = 0; idx < pullThreads.length; ++idx) {
        pullThreads[idx].stopProcessing();
      }
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  transferData                                    ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method is called when there's data pushed from  ***
  ***   a push buffer data source.                              ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      streamI              The stream that generated the   ***
  ***                           data.                           ***
  ***                                                           ***
  *****************************************************************
*/
  public final void transferData(PushBufferStream streamI) {

    // Locate the stream.
    int     idx;

    for (idx = 0; idx < unfinished.length; ++idx) {
      if (streamI == unfinished[idx]) {
        break;
      }
    }

    // Read the data from the stream.
    try {

      // Read the data.
      streamI.read(readBuffer[idx]);

      // Send it to the DataTurbine.
      sendToDataTurbine(streamI,idx,readBuffer[idx]);
    } catch (Exception e) {
      e.printStackTrace();
      sendEvent(new DataSinkErrorEvent(this,e.getMessage()));
      return;
    }

    // Determine if we're done.
    if (readBuffer[idx].isEOM() && doneWithStream(streamI)) {
      sendEvent(new EndOfStreamEvent(this));
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  readPullBuffer                                  ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method is called whenever a pull thread has     ***
  ***   data to send to us.                                     ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      streamI              The stream that generated the   ***
  ***                           data.                           ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      readPullBuffer       Are we done with this stream?   ***
  ***                                                           ***
  *****************************************************************
*/
  public final boolean readPullBuffer(PullBufferStream streamI) {

    // Locate the stream.
    int     idx;

    for (idx = 0; idx < unfinished.length; ++idx) {
      if (streamI == unfinished[idx]) {
        break;
      }
    }

    // Read the data from the stream.
    try {

      // Read the data.
      streamI.read(readBuffer[idx]);

      // Send it to the DataTurbine.
      sendToDataTurbine(streamI,idx,readBuffer[idx]);
    } catch (Exception e) {
      e.printStackTrace();
      return (true);
    }

    // Determine if we're done.
    if (readBuffer[idx].isEOM()) {
      if (doneWithStream(streamI)) {
        close();
      }
      return (true);
    }

    return (false);
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  sendToDataTurbine                               ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August - September, 2000                        ***
  ***                                                           ***
  ***   Copyright 2000, 2002, 2004 Creare Inc.                  ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sends the input buffer to the            ***
  ***   DataTurbine for the specified stream. Actually, since   ***
  ***   the streams may be out of synchronization with each     ***
  ***   other, the method does several things:                  ***
  ***                                                           ***
  ***   1) Locate the channel corresponding to the stream.      ***
  ***           a) If the channel doesn't exist, create it. If  ***
  ***              at that point, all of the channels exist,    ***
  ***              register the channels with the DT.           ***
  ***           b) If the channel does exist, grab it.          ***
  ***                                                           ***
  ***   2) Add the buffer to the channel's list of buffers.     ***
  ***                                                           ***
  ***   3) If there are buffers for all of the unfinished       ***
  ***      channels, create a map consisting of the channels    ***
  ***      that line up at the earliest start time and send     ***
  ***      it to the DataTurbine.                               ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      streamI              The stream that generated the   ***
  ***                           data.                           ***
  ***      idxI                 The channel index.              ***
  ***      bufferI              The buffer.                     ***
  ***                                                           ***
  ***   Modification History :                                  ***
  ***      08/18/2004 - INB                                     ***
  ***           Always register the channels whenever we get a  ***
  ***           new one.                                        ***
  ***      08/13/2002 - 09/25/2002 - INB ---> V2 <---           ***
  ***           Ported to V2 RBNB.                              ***
  ***                                                           ***
  ***      08/13/2002 - INB                                     ***
  ***           Allow for the input encoding type to determine  ***
  ***           the name of the channel.                        ***
  ***      04/01/2002 - INB                                     ***
  ***           Create and save an absolute start time.         ***
  ***                                                           ***
  *****************************************************************
*/
  private final void sendToDataTurbine
    (SourceStream streamI,
     int idxI,
     Buffer bufferI)
  throws Exception {

    synchronized (synchLock) {

      // Skip buffers that do not contain anything of interest.
      if (bufferI.isDiscard()) {
        return;
      }

      Format format = bufferI.getFormat();

      // We require byte arrays for data.
      if (format.getDataType() != Format.byteArray) {
        return;

      } else if ((formats[idxI] != null) && !format.equals(formats[idxI])) {
        throw new Exception
          ("Unsupported format change on stream " + idxI +
           " from " + formats[idxI] + " to " + format);
      }

                if (absoluteStartTime == Long.MIN_VALUE) {
                        absoluteStartTime = System.currentTimeMillis();
                        // 2005/01/13  WHF  Init frame time:
                        for (int ii = 0; ii < lastFrameTime.length; ++ii)
                                lastFrameTime[ii] = absoluteStartTime * 1e-3;
                }

      //System.err.println("sendToDT, idx: "+idxI+",channels[i]: "+channels[idxI]);
      // If the channel exists, we'll use it. If not, create one and see if
      // that makes all of the channels.
      if (channels[idxI] == null) {
        buffersWaiting[idxI] = new Vector(1);
        formats[idxI] = format;
        userData[idxI] = "encoding=" + format.getEncoding();

        if (format instanceof AudioFormat) {
          AudioFormat aFormat = (AudioFormat) format;

          if (++audioChannels == 1) {
            channels[idxI] = "Audio";
          } else {
            channels[idxI] = "Audio" + audioChannels;
          }

          userData[idxI] +=
            ",content=audio" +
            ",channels=" + aFormat.getChannels() +
            ",framerate=" + aFormat.getFrameRate() +
            ",framesize=" + aFormat.getFrameSizeInBits() +
            ",samplerate=" + aFormat.getSampleRate() +
            ",samplesize=" + aFormat.getSampleSizeInBits() +
            ",endian=" + aFormat.getEndian() +
            ",signed=" + aFormat.getSigned() +
            ",startAt=" + absoluteStartTime;

        } else {
          VideoFormat vFormat = (VideoFormat) format;

          String cName;
          if (++videoChannels == 1) {
            cName = "Video";
          } else {
            cName = "Video" + videoChannels;
          }
          if (getUseEncoding()) {
            if (format.getEncoding().equalsIgnoreCase("jpeg")) {
              cName += ".jpg";
            }
          }
          channels[idxI] = cName;

          Dimension   size = vFormat.getSize();

          userData[idxI] +=
            ",content=video" +
            ",framerate=" + vFormat.getFrameRate() +
            ",maxlength=" + vFormat.getMaxDataLength() +
            ",height=" + ((int) size.getHeight()) +
            ",width=" + ((int) size.getWidth());

          if (vFormat instanceof H261Format) {
            H261Format h261Format = (H261Format) vFormat;

            userData[idxI] +=
              ",stillimage=" + h261Format.getStillImageTransmission();

          } else if (vFormat instanceof H263Format) {
            H263Format h263Format = (H263Format) vFormat;

            userData[idxI] +=
              ",advancedprediction=" + h263Format.getAdvancedPrediction() +
              ",arithmeticcoding=" + h263Format.getArithmeticCoding() +
              ",errorcompensation=" + h263Format.getErrorCompensation() +
              ",hrdb=" + h263Format.getHrDB() +
              ",pbframes=" + h263Format.getPBFrames() +
              ",unrestrictedvector=" + h263Format.getUnrestrictedVector();

          } else if (vFormat instanceof JPEGFormat) {
            JPEGFormat jpegFormat = (JPEGFormat) vFormat;

            userData[idxI] +=
              ",decimation=" + jpegFormat.getDecimation() +
              ",qfactor=" + jpegFormat.getQFactor();

          } else if (vFormat instanceof RGBFormat) {
            RGBFormat rgbFormat = (RGBFormat) vFormat;

            userData[idxI] +=
              ",bpp=" + rgbFormat.getBitsPerPixel() +
              ",blue=" + rgbFormat.getBlueMask() +
              ",endian=" + rgbFormat.getEndian() +
              ",flipped=" + rgbFormat.getFlipped() +
              ",green=" + rgbFormat.getGreenMask() +
              ",line=" + rgbFormat.getLineStride() +
              ",pixel=" + rgbFormat.getPixelStride() +
              ",red=" + rgbFormat.getRedMask();
          }

          userData[idxI] += ",startAt=" + absoluteStartTime;
        }

        // If all of the channels now exist, register them.
        int count = 0;
        for (int idx1 = 0; idx1 < channels.length; ++idx1) {
          count += (channels[idx1] != null) ? 1 : 0;
        }
        if (count == channels.length) {

            channelMap = new ChannelMap();

            channelMap.PutTime(0.,0.);
            for (int idx1 = 0; idx1 < channels.length; ++idx1) {
                System.err.println("IDX: "+idx1);
                if (channels[idx1] != null) {
                    
                    System.err.println("REGISTER: "+channels[idx1] + " " 
							+ userData[idx1]);
                    
                    int cIndex = channelMap.Add(channels[idx1]);
					// 2005/03/23  WHF  Switching to new user-meta-data:
                    //channelMap.PutDataAsString(cIndex, userData[idx1]);
					channelMap.PutUserInfo(cIndex, userData[idx1]);
					channelMap.PutMime(cIndex, "text/plain");
                }
            }

            rbnbSource.Register(channelMap);
        }

        // Create a list of key frame indexes.
        lastKeyFrame = new int[channels.length];
      }

      // Add the buffer to the channel's list.
      if (bufferI.getLength() > 0) {
        Buffer waitBuffer = new Buffer();

        waitBuffer.copy(bufferI);
        byte[] original = (byte[]) waitBuffer.getData(),
               copied = new byte[original.length];

        System.arraycopy
          (original,
           0,
           copied,
           0,
           original.length);
        waitBuffer.setData(copied);
        buffersWaiting[idxI].addElement(waitBuffer);

        // Determine if we should send anything and what we should send.
        sendReadyToDataTurbine();
      }
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  sendReadyToDataTurbine                          ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000, 2002, 2003, 2004 Creare Inc.            ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method determines what channels are ready to    ***
  ***   send to the DataTurbine and what they have ready. It    ***
  ***   sends those channels to the DataTurbine.                ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      sendReadyToDataTurbine                               ***
  ***                           True if something was sent.     ***
  ***                                                           ***
  ***   Modification History :                                  ***
  ***      08/24/2004 - INB                                     ***
  ***           Separate the time information per channel.      ***
  ***      08/18/2004 - INB                                     ***
  ***           Separate channels into different maps.  Also,   ***
  ***           loop until everything is sent.                  ***
  ***      08/17/2004 - INB                                     ***
  ***           Check for empty channels when putting data.     ***
  ***      04/17/2003 - INB                                     ***
  ***           Added support for wall clock time.              ***
  ***      02/03/2003 - INB                                     ***
  ***           Added handling of frame rate. It appears that   ***
  ***           JMF may not always give the user their          ***
  ***           requested rate, so we try to adjust for it.     ***
  ***      08/13/2002 - 09/25/2002 - INB ---> V2 <---           ***
  ***           Ported to V2 RBNB.                              ***
  ***      04/01/2002 - INB                                     ***
  ***           Use the absoluteStartTime field to set the      ***
  ***           actual time sent to the server.                 ***
  ***                                                           ***
  *****************************************************************
  */

	private final boolean sendReadyToDataTurbine()
	throws Exception {
		final double SKOOTCH_INC = 1e-9;
		
		boolean readyToSend = true;
		boolean sentSomething = false;
		
		synchronized (synchLock) {
			boolean ready[] = new boolean[channels.length];
			Buffer  readyBuffer = null;
			
			while (readyToSend) {
				// Loop until we've sent everything that is currently ready.
				
				// Look for channels with data waiting to send and determine
				// which buffer(s) should be sent now.
				readyToSend = false;
				for (int idx = 0; idx < channels.length; ++idx) {
					
					// With no channel yet, there cannot be any data.
					if (channels[idx] == null) {
						ready[idx] = false;
						continue;
						
					} else if (((buffersWaiting[idx] == null) ||
					buffersWaiting[idx].isEmpty()) &&
					(unfinished[idx] != null)) {
						// If there is nothing waiting and the stream is not
						// finished, then we need to wait for something on this
						// stream.
						ready[idx] = false;
						continue;
						
					} else if (!buffersWaiting[idx].isEmpty()) {
						// If there is something waiting, compare it to what we
						// think we are going to send.
						readyToSend = true;
						Buffer buffer = (Buffer)
						buffersWaiting[idx].firstElement();
						
						// If we had nothing so far, assume this buffer should be
						// sent.
						if (readyBuffer == null) {
							ready[idx] = true;
							readyBuffer = buffer;
							
						} else if (buffer.getTimeStamp() ==
						readyBuffer.getTimeStamp()) {
							// If this buffer starts at the same time as the
							// current ready buffer, add this one to the list to
							// send.
							ready[idx] = true;
							
						} else if (buffer.getTimeStamp() <
						readyBuffer.getTimeStamp()) {
							// If this buffer starts before the current ready
							// buffer, make this the ready buffer in place of
							// that one.
							for (int idx1 = 0; idx1 < idx; ++idx1) {
								ready[idx1] = false;
							}
							ready[idx] = true;
							readyBuffer = buffer;
						}
					}
				}
				
				// If there is no ready buffer, then we're done.
				readyToSend = readyToSend && (readyBuffer != null);
				
				// If we should send something, build up the map and send it.
				// For V2, we need to ensure that groups of channels stay
				// together each time they are flushed.  For this reason, it is
				// simplest to put each channel in its own map.
				
				// mjm 11-12-04:	alternate approach.	 
				// flush audio+video together, wait until some of each to flush
				if (readyToSend) {
					ChannelMap[] map = new ChannelMap[channels.length];
					byte[][] data = new byte[channels.length][];
					
					// Find the ready buffers.
					// 2004/12/29	 WHF  Cleanup.
					//double jmTime = -Double.MAX_VALUE;
					
					for (int idx = 0; idx < channels.length; ++idx) {
						if (ready[idx]) {
							Buffer buffer =
							(Buffer) buffersWaiting[idx].firstElement();
							// 2006/01/05	 WHF  Buffer already copied:
							data[idx] = //new byte[buffer.getLength()];
							(byte[]) buffer.getData();
							
							// Determine how far back the last key frame is. For
							// audio channels, every frame is a key frame.
							if (buffer.getFormat() instanceof AudioFormat) {
								lastKeyFrame[idx] = 0;
								
							} else if ((buffer.getFlags() &
							Buffer.FLAG_KEY_FRAME) == 0) {
								// For video frames, check to see if this is a
								// key frame. If not add one frame to the count
								// back to the previous key frame.
								++lastKeyFrame[idx];
								
							} else {
								// If this is a key frame, then set the count to
								// 0.
								lastKeyFrame[idx] = 0;
							}
							
							/* 2005/01/13	 WHF  Older JMF timestamps.
							// Grab the time for this channel. Convert from JMF
							// time (nanoseconds since start) to DataTurbine
							// relative seconds (seconds since start).
							// 2004/12/23 WHF	 Why pow?
							double jmTime = buffer.getTimeStamp()*1e-9; // Math.pow(10.,-9);
							double currentTime = jmTime + absoluteStartTime*1e-3;
							if (lastFrameTime[idx] - currentTime > 100) {
								System.err.println("RESET!  "+(lastFrameTime[idx] - currentTime));
								// Rollover bug, so reset reference
								absoluteStartTime 
								= ((long) ((lastFrameTime[idx] - jmTime)*1000));
								currentTime = //now/1000.0;
								lastFrameTime[idx] + SKOOTCH_INC;												  
							}
							*/					  
							// Timestamp by counting audio samples or just system clock.
							double currentTime;					  
							if (channels.length == 1) { // video only
								currentTime = System.currentTimeMillis() * 1e-3;
							} else { // We assume video on 0 and audio on 1 here.							  
								if (idx == 0)
									currentTime = lastFrameTime[1]; // last audio buffer
								else {
									// 2006/01/05  WHF  Buffer.getLength() is 
									// ill-defined.  Samples, or array length?
									AudioFormat af = ((AudioFormat) formats[idx]);
									currentTime = lastFrameTime[1] + 1e-9*af.computeDuration(data[idx].length);
//System.err.println("L: "+buffer.getLength()+"  S: "+data[idx].length+"  D: "+af.computeDuration(buffer.getLength()));										
								}
							}
							
							// This occurs if the time is somehow less than that of the last
							//  frame sent, which may occur because of the independent nature of
							//  the audio/video capture.	Correct with a 'skootch'.
							if (currentTime < lastFrameTimeStamp) {
								System.err.println("SKOOTCH!	"+(lastFrameTimeStamp - currentTime)+'s');
								currentTime = lastFrameTimeStamp + SKOOTCH_INC;
							}						  
							
							lastFrameTime[idx] = currentTime;
							// Remove the buffer from the channel's list.
							buffersWaiting[idx].removeElementAt(0);
							
							//System.err.println("COPY "+idx+"  "+currentTime);
							
							// Copy the data from the buffer into the channel.
							/* 2006/01/05	 WHF  Already copied.
							System.arraycopy
							(buffer.getData(),
							buffer.getOffset(),
							data[idx],
							0,
							buffer.getLength()); */
						}
					}
					
					long now = System.currentTimeMillis();
					//double startTime;
					//double currentTime = 0.;
					for (int idx = 0; idx < channels.length; ++idx) {
						/* 2004/12/29	 WHF  Cleanup.	Time code done above.
						// Calculate the start time for this
						// frame. Unfortunately, JMF appears to have a rollover
						// problem at 2^32 microseconds (which implies that
						// someone is using a 32-bit integer somewhere). The
						// following code attempts to correct for that rollover
						// by adjusting the time.
						startTime = jmTime + rolloverTime;
						
						// If the calculated start time is less than the
						// previously calculated time, adjust the increment.
						while (startTime < lastStartTime[idx]) {
							// If the last JMF time is greater than our guess
							// (originally 2^32 microseconds), then change the
							// guess to the last JMF time.
							if (lastJMTime[idx] > rolloverGuess) {
								rolloverGuess = lastJMTime[idx];
							}
							
							// Adjust the increment by adding the current
							// rollover guess. 
							rolloverTime += rolloverGuess;
							
							// Calculate a new start time.
							startTime = jmTime + rolloverTime;
						}
						lastJMTime[idx] = jmTime;
						*/
						
						if ((data[idx] != null) && 
							( (wallClockBegan == 0) ||
						((userData[idx] != null) && formats[idx] instanceof AudioFormat) || 
						(getRequestedRate() == 0.) || 
						((now - wallClockBegan)/1000.*getRequestedRate() > framesSoFar))) {
							
							if (wallClockBegan == 0) {
								wallClockBegan = now;
							} else if (formats[idx] instanceof VideoFormat) {
								// The intent of this counter is to allow us to
								// drop frames if they are coming faster than we
								// can actually send them out.
								++framesSoFar;
							}
							/* 2004/12/29	 WHF  Cleanup.
							if (useWallClock) {
								currentTime = now/1000.;
							} else {
								currentTime =
								(absoluteStartTime/1000. +
								lastStartTime[idx]);
							}
							*/
							/*
							if (data[idx] != null) {
								map[idx] = new ChannelMap();
								map[idx].PutTime(currentTime,0.);
								map[idx].Add(channels[idx]);
								map[idx].PutDataAsByteArray(0,data[idx]);
								
								// Send the data to the DataTurbine.
								rbnbSource.Flush(map[idx]);
								sentSomething = true;
							}
							*/
							sentSomething = flushToTurbine(
							idx, 
							//									  currentTime,
							//absoluteStartTime/1000.0 + whfTimes[idx],
							lastFrameTime[idx],
							data[idx]);  // mjm 11/04
						} // end elaborate error-prone if
						
						//lastStartTime[idx] = startTime;
					}	 // for each chan
				}
			} // end while (readyToSend)
		}
		
		return (sentSomething);
	}

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  flushToTurbine                                  ***
  ***   By   :  Matt Miller     (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  Nov, 2004                                       ***
  ***                                                           ***
  ***   Copyright 2004 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***   This method queues up audio+video data and flushes      ***
  ***   consistent (audio+video) channel maps to RBNB server    ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      idx                  channels[idx]                   ***
  ***      tstamp               time stamp                      ***
  ***      data                 data bufer                      ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      doneWithFlush        True if flushed this call.      ***
  
                2004/12/23  WHF  Added mime type information.
  
  ***                                                           ***
  *****************************************************************
*/
        private boolean flushToTurbine(int idx, double tstamp, byte[] data)
                throws Exception 
        {
                boolean doFlush=false;
                
                if (tstamp > greatestFrameTimeStamp) greatestFrameTimeStamp = tstamp;
                
                if(channelMap == null) return(false);  // wait until all chans available
                
//System.err.println("PUT  "+idx+"  "+tstamp);
                
                channelMap.PutTime(tstamp,0.);
                channelMap.PutDataAsByteArray(idx,data);
        
                if (formats[idx] instanceof VideoFormat) {
                        gotVideo = true;
                        channelMap.PutMime(idx, "image/jpeg");
                } else { 
                        gotAudio = true;
                        // audio/basic refers specifically to PCM 8-bit 8000Hz.
                        AudioFormat af = (AudioFormat) formats[idx]; 
                        if (af.getSampleRate() == 8000 && af.getSampleSizeInBits() == 8)
                                channelMap.PutMime(idx, "audio/basic");
                        else
                                channelMap.PutMime(idx, "audio/L" + af.getSampleSizeInBits());
                }
        
                // got to flush every video frame?
                if((channels.length==1) || (gotAudio && gotVideo)) doFlush = true;
                if(doFlush) {
                        //System.err.println("flushToTurbine"+channelMap);  
//System.err.println("FLUSH");
                        lastFrameTimeStamp = greatestFrameTimeStamp;
                        rbnbSource.Flush(channelMap);
                        gotAudio = gotVideo = false;
                        doneFlush = true;
                }
                else doneFlush = false;
                
                return(doFlush);
        }


/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  doneWithStream                                  ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method is called whenever a stream finishes.    ***
  ***   It removes the stream from the unfinished list and      ***
  ***   determines if there are any other streams still         ***
  ***   running.                                                ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      streamI              The stream that is done.        ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***      doneWithStream       True if all streams are done.   ***
  ***                                                           ***
  *****************************************************************
*/
  private final boolean doneWithStream(SourceStream streamI) {
    boolean allDoneR = true;

    synchronized (synchLock) {
      for (int idx = 0; idx < unfinished.length; ++idx) {
        if (unfinished[idx] == streamI) {
          unfinished[idx] = null;
        } else if (unfinished[idx] != null) {
          allDoneR = false;
        }
      }

      // If everything is done, flush the data out.
      if (allDoneR) {
        try {
          while (sendReadyToDataTurbine()) {}
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return (allDoneR);
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  sendEvent                                       ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method sends the input event to the list of     ***
  ***   listeners, if there are any.                            ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      eventI               The event to send.              ***
  ***                                                           ***
  *****************************************************************
*/
  protected final void sendEvent(DataSinkEvent eventI) {
    if (!listeners.isEmpty()) {
      synchronized (listeners) {
        for (int idx = 0; idx < listeners.size(); ++idx) {
          DataSinkListener listener =
            (DataSinkListener) listeners.elementAt(idx);

          listener.dataSinkUpdate(eventI);
        }
      }
    }
  }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  PullThread                                      ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This class provides a processing loop for pulling    ***
  ***   data.                                                   ***
  ***                                                           ***
  *****************************************************************
*/
  private class PullThread extends Thread {

    // Private fields:
    private Handler             parent = null;

    private PullBufferStream    stream = null;

    private boolean             stop = true,
                                terminate = false;

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  PullThread                                      ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This constructor builds a pull thread for the        ***
  ***   specified stream and Handler object.                    ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***      streamI              The stream.                     ***
  ***      parentI              Our parent Handler.             ***
  ***                                                           ***
  *****************************************************************
*/
    PullThread(PullBufferStream streamI,Handler parentI) {
      stream = streamI;
      parent = parentI;
      this.start();
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  run                                             ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This is the processing loop for the pull thread.     ***
  ***   It can be active (stop = false) or inactive (stop =     ***
  ***   true).                                                  ***
  ***      While it is inactive, it just waits to be started    ***
  ***   or terminated.                                          ***
  ***      While it is active, it calls our parent's read       ***
  ***   PullBuffer method and then checks for termination.      ***
  ***                                                           ***
  *****************************************************************
*/
    public final void run() {

      // Loop until we are terminated.
      while (!terminate) {

        // Wait for something to happen.
        try {
          while (stop && !terminate) {
            wait();
          }
        } catch (InterruptedException e) {
        }

        // If we haven't been terminated, get some data.
        if (!terminate) {
          if (parent.readPullBuffer(stream)) {
            stopProcessing();
          }
        }
      }
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  startProcessing                                 ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method is called to start the processing loop.  ***
  ***                                                           ***
  *****************************************************************
*/
    final synchronized void startProcessing() {
      stop = false;
      notify();
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  stopProcessing                                  ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method is called to stop the processing loop.   ***
  ***                                                           ***
  *****************************************************************
*/
    final synchronized void stopProcessing() {
      stop = true;
      notify();
    }

/*
  *****************************************************************
  ***                                                           ***
  ***   Name :  terminateProcessing                             ***
  ***   By   :  Ian Brown       (Creare Inc., Hanover, NH)      ***
  ***   For  :  DataTurbine                                     ***
  ***   Date :  August, 2000                                    ***
  ***                                                           ***
  ***   Copyright 2000 Creare Inc.                              ***
  ***   All Rights Reserved                                     ***
  ***                                                           ***
  ***   Description :                                           ***
  ***      This method is called to terminate the processing    ***
  ***   loop.                                                   ***
  ***                                                           ***
  *****************************************************************
*/
    final synchronized void terminateProcessing() {
      terminate = true;
      notify();
    }
  }
}
