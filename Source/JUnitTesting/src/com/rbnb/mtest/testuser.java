// RBNB JUNIT Test 
// test "user data" as sub-channels (vs "User Info" in registration)

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class testuser extends testrbnb {		
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	@SuppressWarnings("deprecation")
	public boolean test(String host) throws SAPIException {

		// Source  
		Source src = new Source(10,"create",100);
		src.OpenRBNBConnection(host,"mySource");
		ChannelMap cmap = new ChannelMap();
		int ix  = cmap.Add("x");
		int is  = cmap.Add("x/scale");
		int iu  = cmap.Add("x/units");

		cmap.PutTime(0.,1.);                // initial time

		double[] x1 = { 1, 2, 3, 4, 5 };
		cmap.PutDataAsFloat64(ix, x1);
		double[] xs1 = { 11, 12, 13, 14, 15 };
		cmap.PutDataAsFloat64(is, xs1);
		String xu = "bananas";
		cmap.PutDataAsString(iu, xu);

		cmap.PutTime(1.,1.);                 // another time step
		double[] x2 = { 2, 4, 6, 8, 10 };
		cmap.PutDataAsFloat64(ix, x2);
		double[] xs2 = { 22, 24, 26, 28, 30 };
		cmap.PutDataAsFloat64(is, xs2);

		src.Flush(cmap, true);               	// send it over

		// Sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");

		snk.GetChannelList("...");                // see what's there

		cmap.Clear();                       	// re-use cmap
		cmap.Add("mySource/x");
		cmap.Add("mySource/x/scale");
		cmap.Add("mySource/x/units");

		snk.Request(cmap, 0., 2., "oldest");
		cmap = snk.Fetch(1000);           	// re-use cmap

		src.CloseRBNBConnection();

		int ngot = cmap.NumberOfChannels();
		if(ngot != 3) throw new SAPIException("Got wrong number of channels"); 

		for (int i=0; i<ngot; i++) {           // spot check results  
			String n = cmap.GetName(i);
			double[] t = cmap.GetTimes(i);

			if(n.equals("mySource/x")) {
				double[] d = cmap.GetDataAsFloat64(i);

				int k = 0;
				for(int j=0; j<x1.length; j++, k++) {
					if(Math.abs(d[k] - x1[j]) > 0.) throw new SAPIException("FAIL: testuser/data values dont check"); 
				}
				for(int j=0; j<x2.length; j++, k++) {
					if(Math.abs(d[k] - x2[j]) > 0.) throw new SAPIException("FAIL: testuser/data values dont check"); 
				}
			}

			if(n.equals("mySource/x/scale")) {
				double[] d = cmap.GetDataAsFloat64(i);

				int k = 0;
				for(int j=0; j<xs1.length; j++, k++) {
					if(Math.abs(d[k] - xs1[j]) > 0.) throw new SAPIException("FAIL: testuser/scale/data values dont check"); 
				}
				for(int j=0; j<xs2.length; j++, k++) {
					if(Math.abs(d[k] - xs2[j]) > 0.) throw new SAPIException("FAIL: testuser/scale/data values dont check"); 
				}
			}

			if(n.equals("mySource/x/units")) {
				if(t[0] != 0.) throw new SAPIException("chanu time error");
				if(!cmap.GetDataAsString(i)[0].equals("bananas")) throw new SAPIException("chanu data mismatch");

			}
		}

		snk.CloseRBNBConnection();
		return true;
	}
}
