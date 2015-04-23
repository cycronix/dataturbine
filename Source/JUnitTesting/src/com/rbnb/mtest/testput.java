package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class testput extends testrbnb {
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}
	
	public boolean test(String host) throws SAPIException {
		String testdata = " ";			// empty string will fail
		
		ChannelMap cmap = new ChannelMap();

	// source
		Source src = new Source();
		src.OpenRBNBConnection(host,"mySource");
		cmap.Add("test.txt");
		cmap.PutDataAsString(0,testdata);
		src.Flush(cmap);
	// sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host, clientName);
		cmap.Add("mySource/test.txt");
		snk.Request(cmap, 0, 0, "newest");
		cmap = snk.Fetch(10000);
		boolean status;
		if(cmap.GetDataAsString(0)[0].equals(testdata)) status = true;
		else											status = false;
//		System.err.println("sink got: "+cmap);
		src.CloseRBNBConnection();
		snk.CloseRBNBConnection();
		return status;
	}
}
