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

// RBNBProcess class - provides a safe exit method so applications
//                    can be run within rbnbManager's JVM, and
//                    provides counting of applications by type for
//                    rbnbManager
//
// Eric Friets
// November 2000
// Copyright 2000 Creare Incorporated

package com.rbnb.utility;

import java.util.Hashtable;

public class RBNBProcess {

//constants - used by rbnbManager to keep track of applications it has
//            running within its JVM

public static final int ADMIN=0;
public static final int CAPTURE=1;
public static final int CASTER=2;
public static final int CHAT=3;
public static final int PIL=4;
public static final int PIM=5;
public static final int PLAYER=6;
public static final int PLOT=7;
public static final int PLUGIN=8;
public static final int PROXY=9;
public static final int SERVER=10;
public static final int SOURCE=11;
public static final int VIDEO=12;
public static final int VIDEOCASTER=13;
public static final int UNKNOWN=14;
public static final int NUM=15; //number of types

// JPW 12/18/2000: Add string array containing each application name
//                 NOTE: This array should contain NUM elements and be
//                 in the correct order (as presented above).
public static final String[] APP_NAMES= {"Admin",
					 "Capture",
					 "Caster",
					 "Chat",
					 "PlugIn Launcher",
					 "PlugIn Manager",
					 "Player",
					 "Plot",
					 "PlugIn",
					 "Proxy",
					 "Server",
					 "Source",
					 "Video",
					 "VideoCaster",
					 "Unknown application"};

// JPW 12/18/2000: Add string array containing utility application class
//                 names.  These are used when Manager launches a local
//                 utility app.  Not used for loading a local PlugIn.
//                 NOTE: This array should contain NUM elements and be
//                 in the correct order (as presented above).
public static final String[] CLASS_NAMES= {"rbnbAdminPI",
					   "rbnbCapturePI",
					   "rbnbCasterPI",
					   "rbnbChatPI",
					   "PILPI",
					   "PIMPI",
					   "rbnbPlayerPI",
					   "rbnbPlotPI",
					   "*** PlugIn",
					   "rbnbProxyPI",
					   "rbnbServerPI",
					   "rbnbSourcePI",
					   "rbnbVidViewPI",
					   "rbnbVideoCasterPI",
					   "*** Unknown application"};

//state variables - used by this class to keep tally of currently running
//                  and number of started applications by type, and to 
//                  note whether a hardExit is appropriate

private static boolean hardExit=true; //default is perform hard exit
private static volatile int[] process=new int[NUM]; //currently running
                                                     //applications
private static volatile int[] count=new int[NUM]; //number of applications
                                                 //started, used to name
                                                 //threadgroups uniquely
private static String tgPrefix="rbnbProcess"; //threadgroup name prefix
private static Hashtable liveProcess=new Hashtable(); //associates Runnable
  //target with ThreadGroup it's running in, so can locate appropriate
  //ThreadGroup to kill on exit
private static Hashtable deadProcess=new Hashtable(); //former processes,
  //which are used to prevent erroneous error messages about unknown processes

private static ThreadGroup myTG=new ThreadGroup("rbnbProcess");

//setHardExit method - called with argument false when multiple applications 
//                     will be run within the same JVM
public static void setHardExit(boolean he) {
  hardExit=he;
}

//exit method - called by each application upon termination, instead of a
//              direct call to System.exit().  If multiple 
//              applications are running, does not call System.exit().
//              Queries threadgroups to determine the type of application
//              and updates the list of currently running applications.
//
//              Note that if the treads within each application handled
//              thread interrupts correctly, this method could ensure that
//              the application was completely cleaned up.  This would be
//              good to do, since without it residue from incompletely
//              terminated applications can build up.

// Stub method which accepts the handler object
public static void exit(int status) {
    exit(status,null);
}

public synchronized static void exit(int status, RBNBProcessInterface target) {
    
//System.err.println("RBNBProcess.exit called");
//try { throw new Exception("RBNBProcess.exit"); }
//catch (Exception e) { e.printStackTrace(); }
//showThreads();


  // JPW 4/6/2001: Some reorganization of the method;
  //               if target is a Handler object, then call
  //                 Handler.stopPlugIn()
  
  ThreadGroup tg=null;
  
  // If hardExit is true, not much work to do - just exit
  if (hardExit) {
    System.exit(status);
  }
  
  // If target is null, nothing to do
  if (target==null) {
    System.err.println("RBNBProcess.exit: ERROR: called with null target");
    return;
  }
  
  // EMF 4/20/01: use interface instead of casting to Handler or PIHandler
  target.stopProcess();
  
  tg=(ThreadGroup)liveProcess.remove(target);
  
  if (tg!=null) { //found threadgroup, add to dead list and update process list
      deadProcess.put(target,tg);
      int type=getProcessType(tg); //-1 means unknown
      try {
        process[type]--;
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        System.err.println("RBNBProcess.exit: undefined process type "+type);
      }
      
      //tg.interrupt();
      
//System.err.println(
//	  "RBNBProcess.exit: stopping all threads in group "+tg.getName());
//      tg.list();
      int active=0;
      int num=0;
      Thread[] tlist=null;
      do {
        active=tg.activeCount()+10;
        tlist=new Thread[active];
        num=tg.enumerate(tlist,true);
      } while (num>=active) ;
      Thread current=null;
      
      for (int i=0;i<num;i++) {
//System.err.println(i+": "+tlist[i]);
        if (tlist[i]!=null) {
          if ( tlist[i]==Thread.currentThread()) current=tlist[i];
          else tlist[i].stop();
        }
      }
      
      // JPW 4/13/2001: Don't kill the current thread (the user may need it
      //                to do something else outside of calling this method).
      // if (current!=null) {
      //     System.err.println(
      //         "Stopping current thread "+Thread.currentThread());
      //     current.stop();
      // }
  }
  else {
      tg=(ThreadGroup)deadProcess.get(target);
      if (tg==null) {
          System.err.println("RBNBProcess.exit: unknown target "+target);
      }
  }
  
}

//for testing only
public static void showThreads() {
  ThreadGroup tg=Thread.currentThread().getThreadGroup();
  System.err.println("RBNBPRocess.showThreads: ");
  tg.list();
  //int num=tg.activeCount();
  //Thread[] tlist=new Thread[num];
  //num=tg.enumerate(tlist,true);
  //for (int i=0;i<num;i++) {
  //  System.err.println(
}

//startProcess method - called by the managing application to start an
//                      application of the specified type in the specified
//                      target.  The thread is started in a new ThreadGroup,
//                      and the list of running processes is updated.
public static boolean startProcess(int type,RBNBProcessInterface target) {
  int num;
  try {
    num=count[type]++;
  } catch (ArrayIndexOutOfBoundsException aioobe) {
    num=count[UNKNOWN]++;
    type=UNKNOWN;
  }
  process[type]++;
  ThreadGroup tg=new ThreadGroup(myTG,tgPrefix+type+"."+num);
  //add to hashtable
  Object previous=liveProcess.put(target,tg);
  if (previous!=null) { //problem - duplicate key, previous contains old value
    System.err.println("RBNBProcess.startProcess: error starting process, duplicate Runnable target.  Process not started.");
    liveProcess.put(target,previous); //replace original
    return(false);
  }
  new Thread(tg,target).start();
  return(true);
}

//listProcess method - called by the managing application to retrieve a
//                     count of running processes by type
public static int[] listProcess() {
  return process;
}

//getProcessType method - called locally to determine the process type of
//                        the calling thread.  Traverses up the threadgroup
//                        tree looking for a known one.  If none is found,
//                        returns -1.
private static int getProcessType(ThreadGroup tg) {
  if (tg==null) {
    return -1;
  }
  String tgName=tg.getName();
  /*
  //determine threadgroup
  ThreadGroup tg=null;
  String tgName=null;
  //System.err.println(
      "RBNBProcess.getProcessType(): current thread = " +
      Thread.currentThread().getName());
  //System.err.println("RBNBProcess.getProcessType: thread stack");
  //Thread.dumpStack();
  do {
    if (tg==null) {
      tg=Thread.currentThread().getThreadGroup();
      // The following will list out all the threads in this group
      tg.list();
    } else {
      tg=tg.getParent();
      if (tg==null) {
        return(-1);
      }
    }
    tgName=tg.getName();
  } while (!tgName.startsWith(tgPrefix));
  */
  //extract process type
  tgName=tgName.substring(tgPrefix.length(),tgName.indexOf("."));
  return (new Integer(tgName)).intValue();
}

}
