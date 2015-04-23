// RBNB JUNIT Test
// test password

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class testpw extends testrbnb {		// replace "test_tempate" with name of your test
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	@SuppressWarnings("deprecation")
	public boolean test(String host) throws SAPIException {		  
		  String usr = "myUsr";
		  String pw  = "myPW";
		  String srcname = "mySource";
		  
		  // SOURCE
			Source src = new Source(100,"create",1000);
		  src.OpenRBNBConnection(host, srcname, usr, pw);
		  
		  srcname = src.GetClientName();  // may not get what ask for
		  
		  ChannelMap cmapsrc = new ChannelMap();
		  cmapsrc.PutTimeAuto("timeofday");
		  
		  double[] x = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		  int idx = cmapsrc.Add("x");
		  cmapsrc.PutDataAsFloat64(idx,x);
		 
		  src.Flush(cmapsrc, true);

		  src.CloseRBNBConnection();
		  src = new Source(1, "load", 0);
		  src.OpenRBNBConnection(host, srcname, "jj", pw);
		  
		  // SINK
		  Sink snk = new Sink();
		  snk.OpenRBNBConnection(host, "mySink", "mm", pw);	// user name doesn't matter, only pw
		  snk.GetChannelList();                            	// can get list even with bad pw
		  
		  ChannelMap cmapreq = new ChannelMap();
		  String snkchan = srcname + "/x";
		  cmapreq.Add(snkchan);
		  snk.Request(cmapreq, 0., 1., "oldest");
		  
		  ChannelMap cmapget = snk.Fetch(1000);
		 
		  if(cmapget.NumberOfChannels() < 1) 
		    throw new SAPIException("FAIL: testpw snk.Fetch failed");

		  if(cmapget.GetType(0) != ChannelMap.TYPE_FLOAT64)
		    throw new SAPIException("FAIL: testpw got wrong datatype");
		    
		  double[] y = cmapget.GetDataAsFloat64(0);

		  src.CloseRBNBConnection(false,false);
		  snk.CloseRBNBConnection();
		  
		  for(int i=0; i<x.length; i++) {
			  if(y[i] != x[i]) throw new SAPIException("FAIL: testpw/data values dont check");
		  }

		return true;			// fill in test logic here
	}
}
