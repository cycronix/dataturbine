// RBNB JUNIT Test Template
// test source put/close/attach/close/reattach

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.rbnb.sapi.*;

public class testreattach extends testrbnb {		
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	@SuppressWarnings("deprecation")
	public boolean test(String host) throws SAPIException {

		// Source
		int ncache = 1;
		int narchive = 10000000;
		int nput = 10;

		Source src = new Source(ncache,"create",narchive);
		src.OpenRBNBConnection(host, "mySource"); 
		ChannelMap cmap = new ChannelMap();
		int ix = cmap.Add("x");

		for (int i=0; i<nput; i++) {
			cmap.PutTime((double)i, 0.);
			double d[] = { (double) i };
			cmap.PutDataAsFloat64(ix, d);
			//		      rbnb_put(src, "x", i, i, 0);
		}
		src.Flush(cmap, true);               	// send it over

		// Sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");
		cmap.Clear();
		cmap.Add("mySource/x");
		snk.Request(cmap,  0., (double)narchive, "absolute");
		cmap = snk.Fetch(60000);
		//		  [gdat gtim gnam] = rbnb_get(snk, "mySource/x", 1, narchive);
		double[] gdat = cmap.GetDataAsFloat64(0);
//		double[] gtim = cmap.GetTimes(0);
//		String gnam = cmap.GetName(0);
		if(gdat.length != nput) throw new SAPIException("Bad data after put"); 

		src.Detach();                // try detach  
		src = new Source(ncache,"append",narchive);
		src.OpenRBNBConnection(host, "mySource"); 

		// append some more data
		cmap.Clear();
		cmap.Add("x");
		for (int i=nput; i<(2*nput); i++) {
			cmap.PutTime((double)i, 0.);
			double d[] = { (double) i };
			cmap.PutDataAsFloat64(ix, d);
		}
		src.Flush(cmap, true);               	// send it over

		cmap.Clear();
		cmap.Add("mySource/x");
		snk.Request(cmap,  0., (double)narchive, "absolute");
		cmap = snk.Fetch(60000);
		//		  [gdat gtim gnam] = rbnb_get(snk, "mySource/x", 1, narchive);
		double[] gdat1 = cmap.GetDataAsFloat64(0);
//		gtim = cmap.GetTimes(0);
//		gnam = cmap.GetName(0);
		if(gdat1.length != (2*nput)) throw new SAPIException("Bad data after reattach"); 

		// close and re-open
		src.CloseRBNBConnection(false,true);
		src = new Source(ncache,"append",narchive);
		src.OpenRBNBConnection(host, "mySource"); 

		cmap.Clear();
		cmap.Add("mySource/x");
		snk.Request(cmap,  0., (double)narchive, "absolute");
		cmap = snk.Fetch(60000);
		//		  [gdat gtim gnam] = rbnb_get(snk, "mySource/x", 1, narchive);
		double[] gdat2 = cmap.GetDataAsFloat64(0);

		// all done with RBNB access
		src.CloseRBNBConnection(false,false);  
		snk.CloseRBNBConnection();

		if(gdat2.length != (2*nput)) throw new SAPIException("Bad data after reopen/reattach, got: "+gdat2.length+", expected: "+(2*nput)); 


		return true;			// fill in test logic here
	}
}
