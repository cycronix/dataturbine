package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.rbnb.sapi.*;

public class testopen extends testrbnb {
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	public boolean test(String host) throws SAPIException {
	// source
		Source src = new Source();
		src.OpenRBNBConnection(host,clientName);
		if(!src.GetClientName().equals(clientName)) {
			throw new SAPIException("unexpected client name");
		}
		src.GetServerName();    			// just for fun
		src.CloseRBNBConnection();
	// sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host, clientName);
		ChannelMap cmap = new ChannelMap();
		cmap.Add("_Log/...");
		snk.RequestRegistration(cmap);		// request just log channels
		snk.Fetch(10000);
		snk.RequestRegistration();			// request all chans
		snk.Fetch(10000);
		snk.CloseRBNBConnection();
		return true;
	}
}
