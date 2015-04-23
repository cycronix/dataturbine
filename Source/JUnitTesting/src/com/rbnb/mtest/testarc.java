// RBNB JUNIT Test 
// archive test

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class testarc extends testrbnb {		
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	@SuppressWarnings("deprecation")
	public boolean test(String host) throws SAPIException {

		// Source
		int ncache = 100;
		int narchive = 2*ncache;
		Source src = new Source(ncache,"create",narchive*2);
		src.OpenRBNBConnection(host,"mySource");

		ChannelMap cmap = new ChannelMap();
		cmap.Add("x");  
		for(int idx=0; idx<narchive; idx++) {
			//			      rbnb_put(src, 'x', i, i, 1);
			cmap.PutTime((double)idx, 1.);  
			cmap.PutDataAsFloat64(0, new double[]{ (double)idx } );
			src.Flush(cmap,true);
		}

		// Sink		  
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");

		cmap.Clear();  cmap.Add("mySource/x");
		snk.Request(cmap,  0., (double)narchive, "absolute");
		cmap = snk.Fetch(60000);
		if(cmap.NumberOfChannels() < 1) throw new SAPIException("Failed to get channel!");
		double[] gdat = cmap.GetDataAsFloat64(0);
		double[] gtim = cmap.GetTimes(0);
		String gnam = cmap.GetName(0);

		if(!gnam.equals("mySource/x")) {
			src.CloseRBNBConnection(false,false);
			snk.CloseRBNBConnection();
			throw new SAPIException("Channel name mismatch");
		}

		if(gdat.length < narchive) {
			src.CloseRBNBConnection(false,false);
			snk.CloseRBNBConnection();
			throw new SAPIException("Fetched wrong amount of data");
		}

		for(int i=0; i<narchive; i++) {
			if((gdat[i] - (double)i) != 0.) {
				src.CloseRBNBConnection();
				snk.CloseRBNBConnection();
				throw new SAPIException("Channel data mismatch");
			}
		}

		// try closing and re-loading archive
		src.CloseRBNBConnection(false,true);

		int narchive2 = 5*ncache;
		src = new Source(ncache,"append",narchive + narchive2);
		src.OpenRBNBConnection(host,"mySource");

		// append some more data
		cmap.Clear();
		cmap.Add("x");  
		for(int idx=0; idx<narchive2; idx++) {
			cmap.PutTime((double)(idx+narchive), 1.);  
			cmap.PutDataAsFloat64(0, new double[]{ (double)(idx+narchive) } );
			src.Flush(cmap,true);
		}

		//		  [gdat gtim gnam] = rbnb_get(snk, 'mySource/x', 1+narchive, narchive2);

		cmap.Clear();  cmap.Add("mySource/x");
		snk.Request(cmap,  narchive, (double)narchive2, "absolute");
		cmap = snk.Fetch(60000);
		if(cmap.NumberOfChannels() < 1) throw new SAPIException("Failed to get channel!");
		gdat = cmap.GetDataAsFloat64(0);
		gtim = cmap.GetTimes(0);
		gnam = cmap.GetName(0);

		if(!gnam.equals("mySource/x")) {
			src.CloseRBNBConnection();
			snk.CloseRBNBConnection();
			throw new SAPIException("Channel name mismatch (on append/reload)");
		}

		if(gdat.length < narchive2) {
			src.CloseRBNBConnection();
			snk.CloseRBNBConnection();
			throw new SAPIException("Fetched wrong amount of data (on append/reload)");
		}

		for(int i=0; i<narchive2; i++) {
			if((gtim[i] - (double)(narchive+i)) != 0.) {
				src.CloseRBNBConnection();
				snk.CloseRBNBConnection();
				throw new SAPIException("Channel time mismatch (on append/reload)");
			}
		}

		cmap.Clear();  cmap.Add("mySource/x");
		snk.Request(cmap,  1., (double)(narchive+narchive2), "absolute");
		cmap = snk.Fetch(60000);
		if(cmap.NumberOfChannels() < 1) throw new SAPIException("Failed to get channel!");
		gdat = cmap.GetDataAsFloat64(0);
		gtim = cmap.GetTimes(0);
		gnam = cmap.GetName(0);

		if(gdat.length < (narchive/2)+narchive2) {
			src.CloseRBNBConnection();
			snk.CloseRBNBConnection();
			throw new SAPIException("Failed to get all data (on append/reload");
		}

		// all done with RBNB access
		src.CloseRBNBConnection(false,false);  // delete archive 
		snk.CloseRBNBConnection();

		return true;			// fill in test logic here
	}
}
