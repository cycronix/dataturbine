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

/**
 * Regression test to check mirrors and routing.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 11/06/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 11/06/2002  INB	Created.
 *
 */
public final class MirrorRouting {
    public final static void main(String[] argsI) {
	MirrorRouting mr = new MirrorRouting();

	mr.start();
    }

    MirrorRouting() {
	super();
    }

    private final void start() {
	com.rbnb.api.Server root = null,
	    left = null,
	    right = null,
	    bottom = null;
	Source source = null;
	Mirror dLR = null,
	    iLR = null,
	    dRBiLR = null,
	    iRBdLR = null;
	Sink rSink = null,
	    bSink = null;

	try {
	    java.util.Timer timer = new java.util.Timer();

	    root = com.rbnb.api.Server.newServerHandle("Root",
						       "localhost:5000");
	    left = com.rbnb.api.Server.newServerHandle("Left",
						       "localhost:5002");
	    right = com.rbnb.api.Server.newServerHandle("Right",
							"localhost:5004");
	    bottom = com.rbnb.api.Server.newServerHandle("Bottom",
							 "localhost:5006");

	    root.start();
	    Thread.currentThread().sleep(1000);
	    root.addChild(left);
	    left.start();
	    Thread.currentThread().sleep(1000);
	    root.addChild(right);
	    right.start();
	    Thread.currentThread().sleep(1000);
	    left.addChild(bottom);
	    bottom.start();
	    Thread.currentThread().sleep(1000);

	    source = new Source(left,1,10,1,timer);
	    source.start();
	    Thread.currentThread().sleep(1000);

	    dLR = new Mirror(left,source.source,right,"direct");
	    dLR.start();
	    Thread.currentThread().sleep(1000);
	    iLR = new Mirror(left,source.source,right,"indirect");
	    iLR.start();
	    Thread.currentThread().sleep(1000);
	    iRBdLR = new Mirror(right,dLR.mirror,bottom,"indirect");
	    iRBdLR.start();
	    Thread.currentThread().sleep(1000);
	    dRBiLR = new Mirror(right,iLR.mirror,bottom,"direct");
	    dRBiLR.start();
	    Thread.currentThread().sleep(1000);

	    rSink = new Sink(right);
	    rSink.start();
	    Thread.currentThread().sleep(1000);
	    bSink = new Sink(bottom);
	    bSink.start();
	    Thread.currentThread().sleep(1000);

	    Thread.currentThread().sleep(600000);

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	} finally {
	    if (bSink != null) {
		bSink.stop();
	    }
	    if (rSink != null) {
		rSink.stop();
	    }
	    if (iRBdLR != null) {
		iRBdLR.stop();
	    }
	    if (dRBiLR != null) {
		dRBiLR.stop();
	    }
	    if (iLR != null) {
		iLR.stop();
	    }
	    if (dLR != null) {
		dLR.stop();
	    }
	    if (source != null) {
		source.stop();
	    }
	    if (bottom != null) {
		try {
		    bottom.stop();
		} catch (Exception e) {
		}
	    }
	    if (right != null) {
		try {
		    right.stop();
		} catch (Exception e) {
		}
	    }
	    if (left != null) {
		try {
		   left.stop();
		} catch (Exception e) {
		}
	    }
	    if (root != null) {
		try {
		    root.stop();
		} catch (Exception e) {
		}
	    }
	}
    }

    /**
     * Internal class that acts as a mirror of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    private final class Mirror {
	com.rbnb.api.Controller controller = null;

	private com.rbnb.api.Server from = null;

	com.rbnb.api.Mirror mirror = null;

	private String mode = null;

	Object source = null;

	private com.rbnb.api.Server to = null;

	Mirror() {
	    super();
	}

	Mirror(com.rbnb.api.Server fromI,
	       Object sourceI,
	       com.rbnb.api.Server toI,
	       String modeI)
	{
	    this();
	    from = fromI;
	    source = sourceI;
	    to = toI;
	    mode = modeI;
	}

	final void start()
	    throws java.lang.Exception
	{
	    controller = to.createController(null);
	    controller.start();

	    mirror = to.createMirror();
	    com.rbnb.api.DataRequest request = new com.rbnb.api.DataRequest();
	    request.setReference(request.NEWEST);
	    request.setDomain(request.FUTURE);
	    request.setRepetitions(request.INFINITE,1.);
	    request.setSynchronized(false);
	    request.setMode(request.FRAMES);
	    request.setGapControl(true);

	    com.rbnb.api.Source theSource =
		((source instanceof com.rbnb.api.Source) ?
		 (com.rbnb.api.Source) source :
		 ((com.rbnb.api.Mirror) source).getSource());

	    mirror.setDirection(mirror.PULL);
	    if (mode.equalsIgnoreCase("direct")) {
		mirror.setRemote(from);
		request.addChild(com.rbnb.api.Rmap.createFromName
				 (theSource.getName() + "/..."));
	    } else {
		mirror.setRemote(to);
		request.addChild(com.rbnb.api.Rmap.createFromName
				 (from.getFullName() + "/" +
				  theSource.getName() + "/..."));
	    }
	    request.getChildAt(0).setFrange(new com.rbnb.api.TimeRange(0.,0.));
	    mirror.setRequest(request);
	    mirror.getSource().setName(mode + "." +
				       from.getName() + "." +
				       to.getName());
	    mirror.getSource().setCframes(theSource.getCframes());
	    mirror.getSource().setAmode(theSource.getAmode());
	    mirror.getSource().setAframes(theSource.getAframes());

	    controller.mirror(mirror);
	}

	final void stop() {
	    if (controller != null) {
		if (mirror != null) {
		    try {
			controller.stop(mirror.getSource());
		    } catch (Exception e) {
		    }
		    mirror = null;
		}
		try {
		    controller.stop();
		} catch (Exception e) {
		}
	    }
	}
    }

    /**
     * Internal class that acts as a sink of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    private final class Sink
	implements java.lang.Runnable
    {
	private com.rbnb.api.Server server = null;

	private Thread thread = null;

	private boolean stopped = false;

	Sink() {
	    super();
	}

	Sink(com.rbnb.api.Server serverI) {
	    this();
	    server = serverI;
	}

	public final void run() {
	    com.rbnb.api.Sink sink = null;
	    com.rbnb.api.DataRequest request = null;
	    com.rbnb.api.Rmap response;
	    com.rbnb.api.DataArray da;
	    String[] names = null,
		names1;
	    int count;

	    try {
		for (int state = 0;
		     !thread.interrupted() &&
			 !stopped;
		     ) {
		    sink = server.createSink(null);
		    sink.start();

		    switch (state) {
		    case 0:
		    case 3:
			com.rbnb.api.Rmap reg = sink.getRegistered
			    (com.rbnb.api.Rmap.createFromName("..."));
			names = reg.extractNames();
			boolean found = false;

			for (int idx = 0;
			     !found && (idx < names.length);
			     ++idx) {
			    if (names[idx].indexOf("/c0") != -1) {
				found = true;
			    }
			}

			if (found) {
			    state = (state == 0) ? 1 : 2;
			} else {
			    Thread.currentThread().sleep(1000);
			}
			break;

		    case 1:
			request = new com.rbnb.api.DataRequest();
			for (int idx = 0; idx < names.length; ++idx) {
			    int idx1;
			    if ((idx1 = names[idx].indexOf("/c")) != -1) {
				boolean good = true;
				for (idx1 += 2;
				     good && (idx1 < names[idx].length());
				     ++idx1) {
				    good = Character.isDigit
					(names[idx].charAt(idx1));
				}

				if (good) {
				    request.addChannel(names[idx]);
				}
			    }
			}
			if (request.getNchildren() == 0) {
			    sink.stop();
			    sink = null;
			    state = 0;
			    break;
			}
			for (int idx = 0;
			     idx < request.getNchildren();
			     ++idx) {
			    request.getChildAt(idx).setTrange
				(new com.rbnb.api.TimeRange(0.,1.));
			}
			request.setReference(request.NEWEST);

			sink.addChild(request);
			sink.initiateRequestAt(0);
			response = sink.fetch(sink.FOREVER);
			names = response.extractNames();
			double startAt = Double.MAX_VALUE;
			for (int idx = 0; idx < names.length; ++idx) {
			    da = response.extract(names[idx]);
			    startAt = Math.min(startAt,da.getStartTime());
			}

			count = 0;
			do {
			    ++count;
			    startAt -= 1.;
			    for (int idx = 0;
				 idx < request.getNchildren();
				 ++idx) {
				request.getChildAt(idx).setTrange
				    (new com.rbnb.api.TimeRange(startAt,1.));
			    }
			    request.setReference(request.ABSOLUTE);

			    sink.addChild(request);
			    sink.initiateRequestAt(0);
			    response = sink.fetch(sink.FOREVER);
			    names1 = response.extractNames();
			} while ((names1 != null) &&
				 (names1.length == names.length) &&
				 !stopped);

			System.out.println(sink.getFullName() + " backwards " +
					   count + " seconds of data for " +
					   names.length + " channels.");

			state = 3;
			sink.stop();
			sink = null;
			break;

		    case 2:
			request = new com.rbnb.api.DataRequest();
			for (int idx = 0; idx < names.length; ++idx) {
			    int idx1;
			    if ((idx1 = names[idx].indexOf("/c")) != -1) {
				boolean good = true;
				for (idx1 += 2;
				     good && (idx1 < names[idx].length());
				     ++idx1) {
				    good = Character.isDigit
					(names[idx].charAt(idx1));
				}

				if (good) {
				    request.addChannel(names[idx]);
				}
			    }
			}
			if (request.getNchildren() == 0) {
			    state = 3;
			    sink.stop();
			    sink = null;
			    break;
			}
			for (int idx = 0;
			     idx < request.getNchildren();
			     ++idx) {
			    request.getChildAt(idx).setTrange
				(new com.rbnb.api.TimeRange(0.,1.));
			}
			request.setReference(request.NEWEST);
			
			sink.addChild(request);
			sink.initiateRequestAt(0);
			response = sink.fetch(sink.FOREVER);
			names = response.extractNames();
			double[] last = new double[names.length];
			for (int idx = 0; idx < names.length; ++idx) {
			    da = response.extract(names[idx]);
			    last[idx] = da.getStartTime() + da.getDuration();
			}

			for (count = 0; !stopped && (count < 10); ++count) {
			    thread.sleep(1000);
			    sink.initiateRequestAt(0);
			    response = sink.fetch(sink.FOREVER);
			    names1 = response.extractNames();
			    if ((names1 == null) ||
				(names1.length != names.length)) {
				break;
			    }
			    int idx;
			    for (idx = 0; idx < names.length; ++idx) {
				da = response.extract(names[idx]);
				double value =
				    da.getStartTime() + da.getDuration();

				if (value == last[idx]) {
				    break;
				}
				last[idx] = value;
			    }

			    if (idx != names.length) {
				break;
			    }
			}

			System.out.println(sink.getFullName() + " forwards " +
					   count + " seconds of data for " +
					   names.length + " channels.");

			state = 0;
			sink.stop();
			sink = null;
			break;
		    }
		}
	    } catch (java.lang.Exception e) {
		if (sink != null) {
		    try {
			sink.stop();
		    } catch (Exception e1) {
		    }
		}
	    }
	}

	final void start() {
	    thread = new Thread(this);
	    thread.start();
	}

	final void stop() {
	    if (thread != null) {
		stopped = true;
		thread.interrupt();
		try {
		    thread.join();
		} catch (java.lang.InterruptedException e) {
		}
		thread = null;
	    }
	}
    }

    /**
     * Internal class that acts as a source of data.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/06/2002  INB	Created.
     *
     */
    private final class Source
	extends java.util.TimerTask
    {
	private int nChans = 1;

	private int nPoints = 10;

	private long frame = 0;

	private double fRate = 1.;

	private com.rbnb.api.Rmap rmap = null;
	
	private com.rbnb.api.Server server = null;

	com.rbnb.api.Source source = null;

	private java.util.Timer timer = null;

	Source() {
	    super();
	}

	Source(com.rbnb.api.Server serverI,
	       int nChansI,
	       int nPointsI,
	       double fRateI,
	       java.util.Timer timerI) {
	    this();
	    server = serverI;
	    nChans = nChansI;
	    nPoints = nPointsI;
	    fRate = fRateI;
	    timer = timerI;
	}

	private final com.rbnb.api.Source connect()
	    throws java.lang.Exception
	{
	    com.rbnb.api.Source sourceR = server.createSource("source");

	    sourceR.setCframes(100);
	    sourceR.setAmode(sourceR.ACCESS_CREATE);
	    sourceR.setAframes(10000);
	    sourceR.start();

	    return (sourceR);
	}

	private final double[][] createData() {
	    double[][] dataR = new double[nChans][];

	    for (int idx = 0; idx < nChans; ++idx) {
		dataR[idx] = new double[nPoints];

		for (int idx1 = 0; idx1 < nPoints; ++idx1) {
		    dataR[idx][idx1] = idx*nChans + idx1;
		}
	    }

	    return (dataR);
	}

	private final com.rbnb.api.Rmap createMap(double[][] dataI)
	    throws java.lang.Exception
	{
	    com.rbnb.api.Rmap rmapR = new com.rbnb.api.Rmap();
	    for (int idx = 0; idx < nChans; ++idx) {
		com.rbnb.api.DataBlock dBlock = new com.rbnb.api.DataBlock
		    (dataI[idx],
		     nPoints,
		     8,
		     com.rbnb.api.DataBlock.TYPE_FLOAT64,
		     com.rbnb.api.DataBlock.ORDER_MSB,
		     false,
		     0,
		     8);
		com.rbnb.api.Rmap cmap =
		    new com.rbnb.api.Rmap("c" + idx,dBlock,null);
		rmapR.addChild(cmap);
	    }

	    return (rmapR);
	}

	public synchronized final void run() {
	    try {
		if (source != null) {
		    updateMap(rmap,++frame);
		    source.addChild(rmap);
		}
	    } catch (java.lang.Exception e) {
		stop();
	    }
	}

	public synchronized  final void start() {
	    try {
		source = connect();
		rmap = createMap(createData());
		timer.scheduleAtFixedRate(this,0,(long) (1000./fRate));

	    } catch (java.lang.Exception e) {
		e.printStackTrace();
		if (source != null) {
		    try {
			source.stop();
		    } catch (Exception e1) {
		    }
		}
	    }
	}

	private synchronized final void stop() {
	    cancel();
	    Thread.currentThread().yield();
	    if (source != null) {
		try {
		    source.stop();
		} catch (Exception e) {
		}
		source = null;
	    }
	}

	private final void updateMap(com.rbnb.api.Rmap rmapI,long frameI)
	    throws java.lang.Exception
	{
	    rmap.setTrange(new com.rbnb.api.TimeRange(frameI/fRate,1./fRate));
	    source.addChild(rmap);
	}
    }
}
