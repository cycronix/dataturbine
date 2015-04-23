// RBNB JUNIT Test Template
// test streaming data source with subscribe

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class teststream extends testrbnb {		
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	public boolean test(String host) throws SAPIException {
		int modeflag = 1;

		// Sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");
		ChannelMap cmapsnk = new ChannelMap();
		cmapsnk.Add("mySource/*");

		if(modeflag == 0) snk.Monitor(cmapsnk, 1);
		else              snk.Subscribe(cmapsnk);

		// Source
		int maxwait = 10;
		int ncache = 20;
		int ngot = 0;
		int nloop = ncache;

		Source src = new Source(ncache,"none",0);
		src.OpenRBNBConnection(host, "mySource"); 

		ChannelMap cmap = new ChannelMap();
		int ix = cmap.Add("x");

		for(int i=0; i<nloop; i++) {
			cmap.PutTime((double)i, 1.);
			double[] d = { (double)i };
			cmap.PutDataAsFloat64(ix, d);
			src.Flush(cmap);
			
			int j=0;
			for (; j<maxwait; j++) {          // avoid forever loop
				cmapsnk = snk.Fetch(500);
//				System.err.println("cmapsnk: "+cmapsnk);
				if(cmapsnk.NumberOfChannels() > 0) break;
			}
			if(j >= maxwait) throw new SAPIException("Gave up waiting to fetch data"); 
			else  			 ngot = ngot + 1;

			double[] gdat = cmapsnk.GetDataAsFloat64(0);
			double[] gtim = cmapsnk.GetTimes(0);
			String gnam = cmapsnk.GetName(0);

			if(!gnam.equals("mySource/x")) throw new SAPIException("Channel name mismatch");
			if(gdat.length < 1) throw new SAPIException("Fetched wrong amount of data");

			if(gdat[0] != gtim[0]) throw new SAPIException("Channel data mismatch");   	// we put in matching data=time
			if(gdat[0] != i) throw new SAPIException("Channel data mismatch");   		// we put in matching data=i
		}

		// all done with RBNB access
		src.CloseRBNBConnection();
		snk.CloseRBNBConnection();

		// fprintf('ngot: %g, nloop: %g, fraction: %g\n',ngot,nloop,ngot/nloop);
		if(ngot < nloop) {
			String wtxt = "Lost "+(nloop-ngot)+"/"+nloop+" data frame(s)";
			if((double)ngot/(double)nloop < .5) throw new SAPIException(wtxt);
			else                				System.err.println(wtxt);
		}

		return true;			// fill in test logic here
	}
}
