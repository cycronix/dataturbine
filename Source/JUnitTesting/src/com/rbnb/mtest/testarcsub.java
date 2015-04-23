// RBNB JUNIT Test 
// subscribe from archive test

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class testarcsub extends testrbnb {	
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	@SuppressWarnings("deprecation")
	public boolean test(String host) throws SAPIException {

		int nframes = 100;
		int fpoints = 10;
		double nduration = 5.;

		// Source
		int ncache = 100;
		Source src = new Source(ncache,"create",nframes);
		src.OpenRBNBConnection(host,"mySource");

		ChannelMap cmapsrc = new ChannelMap();
		cmapsrc.Add("c1");

		//    for idx=1:nframes
		double[] times = new double[fpoints];
		double[] points = new double[fpoints];

		for(int idx = 1, pt=1; idx<=nframes; idx++) {
			for(int j=0; j<fpoints; j++) {
				times[j] = pt++;
				points[j] = -times[j];
			}
			cmapsrc.PutTimes(times);
			cmapsrc.PutDataAsFloat64(0,points);
			src.Flush(cmapsrc,true);
		}

		src.CloseRBNBConnection(false,true);
		src = new Source(ncache,"append",nframes);			// reload
		src.OpenRBNBConnection(host,"mySource");
		//    src = rbnb_source(host,'mySource',100,'append',nframes);	

		// Sink		  
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");

		ChannelMap cmapsnk = new ChannelMap();
		cmapsnk.Add(src.GetClientName() + "/c1");
		snk.Subscribe(cmapsnk,0., nduration, "oldest");		// subscribe in time chunks
		ChannelMap resmap = snk.Fetch(10000);

		int lpoint = 0;
		int pt = 1;
		while (!resmap.GetIfFetchTimedOut()) {
			times = resmap.GetTimes(0);
			points = resmap.GetDataAsFloat64(0);

			for(int i=0; i<times.length; i++) {
//				System.err.println("pt: "+pt+", points["+i+"]: "+points[i]+", times.length: "+times.length);
				if(points[i] != -pt || times[i] != pt) {
					src.CloseRBNBConnection(false,false);
					snk.CloseRBNBConnection();
					throw new SAPIException("FAIL: Fetched times that do not match the expected times");
				}
				pt++;
			}
			lpoint += times.length;    
			if (lpoint == nframes*fpoints) break;
			resmap = snk.Fetch(10000);

		}

		if (lpoint != nframes*fpoints) {
			src.CloseRBNBConnection(false,false);
			snk.CloseRBNBConnection();
			throw new SAPIException("FAIL: Did not retrieve the expected amount of data");
		}

		src.CloseRBNBConnection(false,false);
		snk.CloseRBNBConnection();

		return true;			// fill in test logic here
	}
}
