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

//test code to synchronize on an object for 1 minute
//EMF 8/11/04

//prevents the object from entering synchronized methods, in an effort to
//induce a routing bug for testing purposes

package com.rbnb.api;

public class TestSync extends Thread {
  Object obj=null;

  public TestSync(Object objI) {
    obj=objI;
  }

  public void run() {
    synchronized(obj) {
      System.err.println("TestSync on "+obj);
      try {
        Thread.currentThread().sleep(120000); //sleep for 2 minutes
      } catch (Exception e) {}
    }
    System.err.println("TestSync off "+obj);
  }

} //end class TestSync

