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

import com.rbnb.api.*;

import java.util.*;

public class SimpleTest {
    public final static void main(String[] argsI) {
	boolean started = false;
	Server server = null;
	byte aMode = Source.ACCESS_NONE;
	boolean doMirror = false,
		streamSink = false,
		fMode = false,
		timing = false,
		debugging = false;
	int sourcesSinks = 1,
	    numChans = 1,
	    numPoints = 10,
	    sleepTime = 1000;
	long numFrames = -1,
	     cacheFrames = -1,
	     archiveFrames = 0;
	int cacheFS = 2;
	String serverName = "Server",
	       serverAddress = "internal://";
	SimpleSource[] sources = null;
	SimpleSink[]  sinks = null;

	try {
	    for (int idx = 0; idx < argsI.length;) {
		if (argsI[idx].equals("-n")) {
		    serverName = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-a")) {
		    serverAddress = argsI[idx + 1];
		    idx += 2;

		} else if (argsI[idx].equals("-A")) {
		    archiveFrames = Long.parseLong(argsI[idx + 1]);
		    if (aMode == Source.ACCESS_NONE) {
			aMode = Source.ACCESS_CREATE;
		    }
		    idx += 2;

		} else if (argsI[idx].equals("-c")) {
		    numChans = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-C")) {
		    cacheFrames = Long.parseLong(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-D")) {
		    debugging = true;
		    ++idx;

		} else if (argsI[idx].equals("-f")) {
		    numFrames = Long.parseLong(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-F")) {
		    cacheFS = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-m")) {
		    aMode = (byte) Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-M")) {
		    fMode = true;
		    ++idx;

		} else if (argsI[idx].equals("-p")) {
		    numPoints = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-s")) {
		    sourcesSinks = Integer.parseInt(argsI[idx + 1]);
		    idx += 2;

		} else if (argsI[idx].equals("-t")) {
		    timing = true;
		    ++idx;

		} else if (argsI[idx].equals("-z")) {
		    doMirror = true;
		    ++idx;

		} else if (argsI[idx].equals("-Z")) {
		    streamSink = true;
		    ++idx;
		}
	    }

	    server = com.rbnb.api.Server.newServerHandle
		(serverName,
		 serverAddress);

	    if (!server.isRunning()) {
		started = true;
		server.start();
	    }

if (doMirror && (serverAddress.compareTo("internal://") != 0)) {
    Source oldMSource = server.createSource("Mirror1");
    Sink oldMSink = server.createSink("_Snk.Mirror1");
    Controller mCntrl = server.createController("_Ctl.Mirror1");
    mCntrl.start();
    try {
	mCntrl.stop(oldMSource);
    } catch (java.lang.Exception e) {
    }
    try {
	mCntrl.stop(oldMSink);
    } catch (java.lang.Exception e) {
    }
    mCntrl.stop();
}

	    SimpleController controller = new SimpleController
		("SimpleController",
		 server);
	    controller.start();

	    sources = new SimpleSource[sourcesSinks];
	    sinks = new SimpleSink[sourcesSinks];
	    Date last = new Date();
	    for (int idx = 0; idx < sourcesSinks; ++idx) {
		sources[idx] = new SimpleSource("Source" + (idx + 1),
						server);

		sources[idx].setAframes(archiveFrames);
		sources[idx].setAmode(aMode);
		sources[idx].setCframes(cacheFrames);
		sources[idx].setCfs(cacheFS);
		sources[idx].setNchans(numChans);
		sources[idx].setNpoints(numPoints);
		sources[idx].setNframes(numFrames);
		sources[idx].setStime(sleepTime);
		sources[idx].setTiming(timing);
		if (timing || (aMode == Source.ACCESS_LOAD)) {
		    sources[idx].setDisconnect(false);
		}

		sources[idx].start();
		Thread.currentThread().yield();
		sources[idx].isRunning();
		Thread.currentThread().yield();
	    }

if (doMirror && (serverAddress.compareTo("internal://") != 0)) {
    Mirror mirror = server.createMirror();
    DataRequest req = new DataRequest(null,
				      null,
				      null,
				      DataRequest.OLDEST,
				      DataRequest.ALL,
				      DataRequest.INFINITE,
				      1.,
				      false,
				      DataRequest.FRAMES),
		srv = new DataRequest(server.getName(),
				      null,
				      new TimeRange(0.,0.)),
		src = new DataRequest(sources[0].getSourceName()),
		chn = new DataRequest("...");
    src.addChild(chn);
    srv.addChild(src);
    req.addChild(srv);
    mirror.setRemote(server);
    mirror.setRequest(req);
    mirror.getSource().setName("Mirror1");
    mirror.getSource().setCframes(cacheFrames);
    mirror.getSource().setNfs(cacheFS);
    mirror.getSource().setAmode(aMode);
    mirror.getSource().setAframes(archiveFrames);
    Controller ctrl = server.createController("MirrorStarter");
    ctrl.start();
    try {
	ctrl.mirror(mirror);
    } catch (java.lang.Exception e) {
	e.printStackTrace();
    }
    ctrl.stop();
}

	    if (timing) {
		boolean done = false;
		do {
		    done = true;

		    for (int idx = 0; done && (idx < sourcesSinks); ++idx) {
			if (sources[idx].getMframes()) {
			    done = false;
			}
		    }
		    Thread.currentThread().sleep(10);
		} while (!done);
		Date now = new Date();

		System.err.println("Ran " + sourcesSinks + " sources in " +
				   (now.getTime() - last.getTime())/1000.);
	    }

	    for (int idx = 0; idx < sourcesSinks; ++idx) {
		sinks[idx] = new SimpleSink("Sink" + (idx + 1),
					    server,
					    sources[idx].getSource());

		sinks[idx].setNframes(numFrames);
		sinks[idx].setStime(sleepTime);
		sinks[idx].setStream(streamSink);
		sinks[idx].setFmode(fMode);
		sinks[idx].setTiming(timing);
		sinks[idx].setAmode(aMode);
		sinks[idx].start();
		Thread.currentThread().yield();
	    }

	    while (true) {
		Thread.currentThread().sleep(1000);

		boolean done = true;
		for (int idx = 0; done && (idx < sourcesSinks); ++idx) {
		    done = !sinks[idx].isRunning() &&
			(timing || (aMode == Source.ACCESS_LOAD) ||
			 !sources[idx].isRunning());
		}

		if (done) {
		    if (timing || (aMode == Source.ACCESS_LOAD)) {
			for (int idx = 0; idx < sourcesSinks; ++idx) {
			    sources[idx].disconnect();
			}
		    }

		    if (!debugging) {
			System.out.println
			    ("\nTest complete, press <CR> to exit:");
			new java.io.BufferedReader
			    (new java.io.InputStreamReader
				(System.in)).readLine();
		    }
		    if (started) {
			server.stop();
		    }
		    break;
		}
	    }
	    server = null;

	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    if ((server != null) && server.isRunning() && started) {
		server.stop();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
