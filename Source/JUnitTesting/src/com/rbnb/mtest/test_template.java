// RBNB JUNIT Test Template
// use this template for new JUNIT test; fill in the logic

package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.rbnb.sapi.*;

public class test_template extends testrbnb {		// replace "test_tempate" with name of your test
	String clientName = getClass().getSimpleName();

	@Test 
	public void thisTest() {
		assertEquals(clientName, true, runtest(clientName));
	}

	public boolean test(String host) throws SAPIException {
		return true;			// fill in test logic here
	}
}
