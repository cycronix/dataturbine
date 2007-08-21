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

// MetricsHandler - accumulates and computes metrics
// Eric Friets
// October 2005
// for IOScan

package com.rbnb.utility;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;


public class MetricsHandler extends Thread {
	
	private String address=null;
	private String name=null;
	private int cache=0;
	private int archive=0;
	private int interval=0;
	
	private Source src=null;
	private ChannelMap cm=null;
	private long[] totalBytes={ 0 };
	private long[] rateBytes={ 0 };
	private boolean doRun=true;
	
	public MetricsHandler(String addressI, String nameI, int cacheI,
						  int archiveI, int intervalI) {
		address=addressI;
		name=nameI;
		cache=cacheI;
		archive=archiveI;
		interval=intervalI;
		try {
			src=new Source();
			if (archive>0) src.SetRingBuffer(cache,"create",archive);
			else src.SetRingBuffer(cache,"none",archive);
			src.OpenRBNBConnection(address,name);
			cm=new ChannelMap();
			cm.Add("DataBytes");
			cm.PutMime(0,"application/octet-stream");
			cm.Add("DataRate");
			cm.PutMime(1,"application/octet-stream");
			cm.PutTimeAuto("timeofday");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean update(long bytes) {
		totalBytes[0]+=bytes;
		rateBytes[0]+=bytes;
		return doRun;
	}
	
	public synchronized void updateInfo(String xmlInfo) {
		try {
			ChannelMap cminfo=new ChannelMap();
			cminfo.Add("Info");
			cminfo.PutMime(0,"text/xml");
			cminfo.PutTimeAuto("timeofday");
			cminfo.PutDataAsString(0,xmlInfo);
			src.Flush(cminfo);
		} catch (Exception e) {
			e.printStackTrace();
			src.CloseRBNBConnection();
			//doRun=false;
		}
	}
	
	public void close() {
		doRun=false;
	}
	
	public void run() {
		while (doRun) {
			synchronized (this) {
				try {
					if (!src.VerifyConnection()) {
						src.OpenRBNBConnection(address,name);
					}
					cm.PutDataAsInt64(0,totalBytes);
					rateBytes[0]/=interval;
					cm.PutDataAsInt64(1,rateBytes);
					src.Flush(cm);
					
				} catch (Exception e) {
					System.err.println("Exception writing metrics; will attempt reconnect.");
					e.printStackTrace();
					//doRun=false;
				}
				rateBytes[0]=0;
			}
			try { sleep(interval*1000); } catch (Exception e) {}
		}
		
		//src.CloseRBNBConnection();
	} //end method run
	
} //end class MetricsHandler
		
