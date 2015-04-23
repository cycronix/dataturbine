// RBNB JUNIT Test
// rbnb archive multiple channels test.

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class testchans extends testrbnb {		
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	@SuppressWarnings("deprecation")
	public boolean test(String host) throws SAPIException {
		int nchans = 100;
		Source src = new Source(10,"create",100);
		src.OpenRBNBConnection(host,"mySource");

		ChannelMap cmap = new ChannelMap();
		for(int idx=0; idx<nchans; idx++) {
			cmap.Clear();
			cmap.Add("c"+idx);
			cmap.PutTime((double)idx, 1.);
			cmap.PutDataAsFloat64(0, new double[]{ (double)idx } );
			src.Flush(cmap, true);
			//			rbnb_put(src,sprintf('c%d',idx),idx,idx,1);
		}

		src.CloseRBNBConnection(false,true);
		src = new Source(10,"append",100);		// reload source
		src.OpenRBNBConnection(host,"mySource");
//		try{ Thread.sleep(10000); } catch(Exception e){};
		
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");

		for (int idx=0; idx<nchans; idx++) {	// fetch them one at a time
			cmap.Clear();
			cmap.Add("mySource/c"+idx);
			snk.Request(cmap, (double)idx,  1., "absolute");
			cmap = snk.Fetch(60000);
			if(cmap.NumberOfChannels() != 1) throw new SAPIException("Failed to get channel: "+idx);
			double[] gdat = cmap.GetDataAsFloat64(0);
//			double[] gtim = cmap.GetTimes(0);
			String gnam = cmap.GetName(0);

			if(!gnam.equals("mySource/c"+idx)) {
				src.CloseRBNBConnection(false,false);
				snk.CloseRBNBConnection();
				throw new SAPIException("Channel name mismatch");
			}

			if (gdat.length < 1) {
				src.CloseRBNBConnection(false,false);
				snk.CloseRBNBConnection();
				throw new SAPIException("Channel data size mismatch");
			}

			if (gdat[0] != (double)idx) {
				src.CloseRBNBConnection(false,false);
				snk.CloseRBNBConnection();
				throw new SAPIException("Channel data value mismatch");
			}
		}

		src.CloseRBNBConnection(false,false);
		snk.CloseRBNBConnection();

		//		fprintf('PASS: many channels test\n');
		return true;			// fill in test logic here
	}
}
