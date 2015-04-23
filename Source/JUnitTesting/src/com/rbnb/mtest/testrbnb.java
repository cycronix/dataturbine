// wrapper to run test with SAPIException catch and PASS/FAIL print

package com.rbnb.mtest;

import com.rbnb.sapi.SAPIException;

public abstract class testrbnb {
	
	public boolean runtest(String clientName) {
		try {
			boolean status = test("localhost");		// can edit for non-localhost network test
			if(status) 	System.err.println("PASS:  "+clientName);
			else		System.err.println("FAIL:  "+clientName);
			return status;
		} catch(Exception e) {
			e.printStackTrace(System.err);			// uncomment this for verbose error traceback
			System.err.println("FAIL:  "+clientName+", Exception: "+e.getMessage());
			return false;
		}
	}
		
	public abstract boolean test(String host) throws SAPIException;	// must be implemented
	
}
