/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

/*
  *****************************************************************
  ***								***
  ***	Name :	SafeVariables.java				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This file contains the SafeVariables class, which	***
  ***	is designed to help convert programs that were designed	***
  ***	to run from the command line into code that can be run	***
  ***	under a thread started by another program. If the	***
  ***	program could be run multiple times simultaneously by	***
  ***	the parent program, there is the potential for problems	***
  ***	with "static" variables that may want to be static only	***
  ***	for the particular instance of the program, rather than	***
  ***	all instances.						***
  ***	   This class addreses that problem by providing a	***
  ***	means by which one or more variables can be set up to	***
  ***	belong to a particular thread group, shared by all of	***
  ***	the threads in that group, but not accessible to any	***
  ***	thread outside that group.				***
  ***								***
  ***	Modification History :					***
  ***	   11/21/2000 - 11/22/2000 - INB			***
  ***		For performance reasons, keep the last thread	***
  ***		group entry handy.				***
  ***		Also, the variable "names" are now indexes.	***
  ***	   12/20/2000 - INB					***
  ***		Ensure that the lastSVGroup field is set if	***
  ***		the group is added.				***
  ***		Synchronize access to the last field variables	***
  ***		and the individual SV group.			***
  ***		Use the myHasher vector as the synchronization	***
  ***		object and create it right off the bat.		***
  ***		Re-wrote the comparison routine to do a compare	***
  ***		rather than a subtraction so that it produces	***
  ***		the correct results.				***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

import java.util.Vector;

public final class SafeVariables implements SortCompareInterface {

  // Private fields:
					// The variables for this group.
  private Object[]		variables = new Object[0];

					// The owning thread group.
  private ThreadGroup		group = null;

  // Private static fields:
					// The thread groups seen.
  private static SortedVector	groups = new SortedVector(1,16);
					// Hashing vector when hashes do not
					// work.
  private static Vector		myHasher = new Vector();

  private static Thread		lastThread = null;

  private static ThreadGroup	lastTGroup = null;

  private static SafeVariables	lastSVGroup = null;

/*
  *****************************************************************
  ***								***
  ***	Name :	SafeVariables					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a safe variables object for	***
  ***	the current thread group.				***
  ***								***
  *****************************************************************
*/
  private SafeVariables() {
    group = Thread.currentThread().getThreadGroup();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	setVariable					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This static method sets the variable with the input	***
  ***	name to the specified value for the current thread	***
  ***	group.							***
  ***								***
  ***	Input :							***
  ***	   nameI		The name of the variable.	***
  ***	   valueI		The new value of the variable.	***
  ***								***
  ***	Modification History :					***
  ***	   11/21/2000 - 11/22/2000 - INB			***
  ***		Use the last information if possible.		***
  ***		The "name" is now an index.			***
  ***	   12/20/2000 - 12/21/2000 - INB			***
  ***		Ensure that the lastSVGroup field is set if	***
  ***		the group is added.				***
  ***		Synchronize access to the last field variables	***
  ***		and the individual SV group.			***
  ***								***
  *****************************************************************
*/
  public final static void setVariable(int nameI,Object valueI)
  throws SortException {
    SafeVariables theGroup = null,
		  localLSV;
    Thread	  thread = Thread.currentThread(),
		  localLThread;
    ThreadGroup	  tGroup,
		  localLTGroup;

    // If the thread group hasn't changed, then we can use the safe variables
    // list from last time.
    synchronized (myHasher) {
      if ((thread == lastThread) ||
	  ((tGroup = thread.getThreadGroup()) == lastTGroup)) {
	theGroup = lastSVGroup;

	// If the group is not in the list, create a new entry.
	if (theGroup == null) {
	  lastSVGroup =
	    theGroup = new SafeVariables();
	  groups.add(theGroup);
	}
	lastThread = thread;

      // Locate the safe variables for the current group. If necessary,
      // create a new group.
      } else {
	lastThread = thread;
	lastTGroup = tGroup;

	// First see if the group is in the existing list.
	theGroup = (SafeVariables) groups.find(tGroup);

	// If the group is not in the list, create a new entry.
	if (theGroup == null) {
	  theGroup = new SafeVariables();
	  groups.add(theGroup);
	}
      }

      lastSVGroup = theGroup;
    }

    // Locate the safe variable that has the specified name. If necessary,
    // create a new variable.
    synchronized (theGroup) {
      if (nameI >= theGroup.variables.length) {
	Object[] old = theGroup.variables;

	theGroup.variables = new Object[nameI + 1];

	if (old.length > 0) {
	  System.arraycopy
	    (old,
	     0,
	     theGroup.variables,
	     0,
	     old.length);
	}
      }
      theGroup.variables[nameI] = valueI;
    }
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	getVariable					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This static method gets the variable with the input	***
  ***	name and returns its value for the current thread	***
  ***	group.							***
  ***								***
  ***	Input :							***
  ***	   nameI		The name of the variable.	***
  ***								***
  ***	Returns :						***
  ***	   getVariable		The value of the variable.	***
  ***								***
  ***	Modification History :					***
  ***	   11/21/2000 - 11/22/2000 - INB			***
  ***		Use the last information if possible.		***
  ***		Return null rather than throw an exception if	***
  ***		the variable cannot be found.			***
  ***		The "name" is now an index.			***
  ***	   12/20/2000 - 12/21/2000 - INB			***
  ***		Ensure that we get the thread group.		***
  ***		Synchronize access to the last field variables	***
  ***		and the individual SV group.			***
  ***								***
  *****************************************************************
*/
  public final static Object getVariable(int nameI)
  throws IllegalArgumentException,
	 SortException {
    Object	  valueR = null;
    SafeVariables theGroup = null;
    Thread	  thread = Thread.currentThread();
    ThreadGroup	  tGroup = null;

    // If the thread group hasn't changed, then we can use the safe variables
    // list from last time.
    synchronized (myHasher) {
      if ((thread == lastThread) ||
	  ((tGroup = thread.getThreadGroup()) == lastTGroup)) {
	if (lastSVGroup == null) {
	  return (null);
	}
	theGroup = lastSVGroup;
	lastThread = thread;

      // Locate the safe variables for the current group. If the group isn't
      // there, throw an illegal argument exception.
      } else {
	lastThread = thread;
	if ((lastTGroup = tGroup) == null) {
	  lastTGroup =
	    tGroup = thread.getThreadGroup();
	}

	// First see if the group is in the existing list.
	lastSVGroup =
	  theGroup = (SafeVariables) groups.find(tGroup);

	// If the group is not in the list, return null.
	if (theGroup == null) {
	  return (null);
	}
      }
    }

    synchronized (theGroup) {
      if (nameI < theGroup.variables.length) {
	valueR = theGroup.variables[nameI];
      }
    }

    return (valueR);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	sortField					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method provides the sort field for this safe	***
  ***	variables object. The sort field is always the thread	***
  ***	group.							***
  ***								***
  ***	Input :							***
  ***	   sortSelectorI	The sort field selector - must	***
  ***				be null.			***
  ***								***
  ***	Returns :						***
  ***	   sortField		The sort field.			***
  ***								***
  *****************************************************************
*/
  public final Object sortField(Object sortSelectorI) throws SortException {

    // No selector is allowed.
    if (sortSelectorI != null) {
      throw new SortException
	("The SafeVariables class does not support sort selectors.");
    }

    return (group);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	compareTo					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method compares this safe variables object to	***
  ***	the input value. The value must be a thread group.	***
  ***								***
  ***	Input :							***
  ***	   sortSelectorI	The sort field selector - must	***
  ***				be null.			***
  ***	   sortValueI		The sort field value to check.	***
  ***								***
  ***								***
  ***	Returns :						***
  ***	   compareTo		The results of the comparison:	***
  ***				   <0 this object comes before	***
  ***				   =0 equal			***
  ***				   >0 this object comes after	***
  ***								***
  ***	Modification History :					***
  ***	   12/21/2000 - INB					***
  ***		Compare, don't subtract!			***
  ***								***
  *****************************************************************
*/
  public final int compareTo(Object sortSelectorI,Object sortValueI)
  throws SortException {
    ThreadGroup	mine = (ThreadGroup) sortField(sortSelectorI),
		theirs = (ThreadGroup) sortValueI;
    int		myHash = mine.hashCode(),
		theirHash = theirs.hashCode(),
		diff = ((myHash < theirHash) ? -1 :
			(myHash > theirHash) ? 1 : 0);

    // Hopefully, the hash code produces different values for the different
    // thread groups. If not, we're going to have to generate our own values.
    if (mine == theirs) {
	if (diff != 0) {
	    diff = 0;
	    System.err.println("Hashing weirdity in SafeVariables!");
	}
    } else {
	if (diff == 0) {
	    diff = hashem(mine,theirs);
	}
    }

    return (diff);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	hashem						***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	August, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This static method returns the difference in the	***
  ***	local hashing values for the two input values. The	***
  ***	local hashing values are the index of the input objects	***
  ***	in the myHasher vector.					***
  ***								***
  ***	Input :							***
  ***	   firstI		The first object to look at.	***
  ***	   secondI		The second object to look at.	***
  ***								***
  ***	Returns :						***
  ***	   hashem		The results of the comparison:	***
  ***				   <0 first object comes before	***
  ***				   =0 equal			***
  ***				   >0 first object comes after	***
  ***								***
  ***	Modification History :					***
  ***	   Synchronize on the myHasher vector.			***
  ***								***
  *****************************************************************
*/
  private final static int hashem
    (ThreadGroup firstI,
     ThreadGroup secondI) {
    int firstIdx = -1,
	secondIdx = -1;

    // Get the index of the first object in the "hash" vector. If it isn't in
    // the vector, put it in at the end and get its new index.
    synchronized (myHasher) {
      if ((firstIdx = myHasher.indexOf(firstI)) == -1) {

	// If the hasher hasn't been built yet, build it from the group list.
	if (myHasher.size() == 0) {
	  for (int idx = 0; idx < groups.size(); ++idx) {
	    ThreadGroup group = (ThreadGroup) groups.elementAt(idx);

	    myHasher.addElement(group);
	  }

	  firstIdx = groups.size();
	  myHasher.addElement(firstI);
	}
      }

      // Get the index of the second object in the "hash" vector. If it isn't
      // in the vector, put it in at the end and get its new index.
      if ((secondIdx = myHasher.indexOf(secondI)) == -1) {
	secondIdx = groups.size();
	myHasher.addElement(secondI);
      }
    }

    return (firstIdx - secondIdx);
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	toString					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	December, 2000					***
  ***								***
  ***	Copyright 2000 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns a string representation of this	***
  ***	SafeVariables object.					***
  ***								***
  ***	Returns :						***
  ***	   toString		The string representation.	***
  ***								***
  *****************************************************************
*/
  public String toString() {
    return ("SV: " + group);
  }
}
