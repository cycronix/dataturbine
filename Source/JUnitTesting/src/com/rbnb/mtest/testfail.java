package com.rbnb.mtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class testfail extends testrbnb {
	String clientName = getClass().getSimpleName();

	@Test 
	public void testFail() {
		assertEquals(clientName, true, runtest(clientName));
	}
	
	public boolean test(String host) {
		return false;
	}
}
