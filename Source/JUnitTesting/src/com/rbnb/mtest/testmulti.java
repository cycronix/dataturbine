// RBNB JUNIT Test
// test multiple channel Flush/Fetch

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rbnb.sapi.*;

public class testmulti extends testrbnb {		
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	public boolean test(String host) throws SAPIException {
		
		Source src = new Source(10,"create",100);
		src.OpenRBNBConnection(host,"mySource");
		Sink snk = new Sink();
		snk.OpenRBNBConnection(host,"mySink");
		
		  // Source
		  double[] c1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		  double[] c2 = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
		  double[] c3 = { 3, 6, 9, 12, 15, 18, 21, 24, 27, 30 };
		  double[] c4 = { 4, 8, 12, 16, 20, 24, 28, 32, 36, 40 };

		  ChannelMap cm = new ChannelMap();
		  int i1=cm.Add("c1");
		  int i2=cm.Add("c2");
		  int i3=cm.Add("c3");
		  int i4=cm.Add("c1/c4");
		  
		  cm.PutTime(1.,10.);  	// this applies to all putdata until next time-set
		  
		  cm.PutDataAsFloat64(i1,c1);
		  cm.PutDataAsFloat64(i2,c2);
		  cm.PutDataAsFloat64(i3,c3);
		  cm.PutDataAsFloat64(i4,c4);
		  
		  src.Flush(cm,true);
		  
		  // Sink
		  cm = new ChannelMap();
		  i1=cm.Add("mySource/c1");     // would be nice if could re-use put cmap,
		  i2=cm.Add("mySource/c2");     // but need Source/chan on fetch (!)
		  i3=cm.Add("mySource/c3");
		  i4=cm.Add("mySource/c1/c4");
		  
		  snk.Request(cm, 0, 10, "oldest");
		  ChannelMap cmg = snk.Fetch(1000);

		  src.CloseRBNBConnection();
		  snk.CloseRBNBConnection();
		  
		  // Check Results
		  
		  double[] c1g = cmg.GetDataAsFloat64(i1);
		  double[] t = cmg.GetTimes(i1);
		  for(int i=0; i<c1.length; i++) {
			  if(Math.abs(c1g[i] - c1[i]) > 0.) throw new SAPIException("FAIL: testmulti/data values dont check"); 
			  if(Math.abs(t[i] - (double)(i+1)) > 0.) throw new SAPIException("FAIL: testmulti/time values dont check"); 
		  }
		  		  	
		  return true;	
	}
}
