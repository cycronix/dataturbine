package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.rbnb.sapi.*;
import java.util.Vector;

public class testwildcard extends testrbnb {
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	public boolean test(String host) throws SAPIException {
		int nloop = 16;
		Vector <Source>vsrc = new Vector<Source>();		// store vector so GC doesn't blow them away
		ChannelMap cmap = new ChannelMap();
		String mysrc = "mysrc";

		for(int i=0; i<nloop; i++) {
			// source
			vsrc.add(new Source());
			String srcname = mysrc + i;
			vsrc.get(i).OpenRBNBConnection(host,srcname);
			//				System.err.println("open new source: "+srcname);
			cmap = new ChannelMap();
			cmap.Add("chan");
			cmap.PutDataAsString(0, "hi there"+i);
			vsrc.get(i).Flush(cmap);
			//			try{ Thread.sleep(2000); } catch(Exception e){};
		}
		try{ Thread.sleep(1000); } catch(Exception e){};

		// sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host, "mysink1");
		cmap.Clear();		
		//		cmap.Add(srcname+"/...");
		cmap.Add("*/chan");
		snk.Request(cmap,0., 100., "newest");	
		cmap = snk.Fetch(10000);
		snk.CloseRBNBConnection();
		//			System.err.println("got: "+cmap);
		if(cmap.NumberOfChannels() != nloop) {
			throw(new SAPIException("wrong number of wildcard sources!"));
		}

		return true;
	}
}
