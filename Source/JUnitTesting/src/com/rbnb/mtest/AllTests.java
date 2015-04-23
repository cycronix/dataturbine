// run all JUNIT rbnb tests
/*
* 	Testrbnb JUNIT tests. They roughly follow the Matlab test suite.
*	Each test extends testrbnb wrapper that runs each test.  Add new classes to SuiteClasses below.
*	See test_template.java for outline for creating new test class.
*	For stand-alone test-runner, in Eclipse "Export" AllTests.java to runnable jar file.
*/

package com.rbnb.mtest;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	testopen.class, 
	testput.class, 
	testwildcard.class, 
	testchans.class, 
	testmulti.class, 
	testarc.class, 
	testarcsub.class, 
	testpw.class, 
	testuser.class,
	testreattach.class,
	teststream.class
	})

public class AllTests {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(AllTests.class);
//        for (Failure fail : result.getFailures()) System.out.println(fail.toString());

        if (result.wasSuccessful()) {
            System.err.println("Tests complete, all passed.");
        }
        else {
            System.err.println("Tests complete, some failed!");
        }
    }	
	
}
