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

// RegisteredChannel - replacement for COM.Creare.RBNB.RegisteredChannel

package com.rbnb.plot;

public class RegisteredChannel {

public static String hostName(String name) {
  int idx=name.indexOf('/');
  if (idx==-1) return new String("");
  String hostport=name.substring(0,idx);
  idx=hostport.indexOf(':');
  if (idx==-1) return hostport;
  return hostport.substring(0,idx);
}

public static int port(String name) {
  //System.err.println("RC.port: name "+name);
  int idx=name.indexOf('/');
  //System.err.println("RC.port: / "+idx);
  if (idx==-1) return 0;
  String hostport=name.substring(0,idx);
  //System.err.println("RC.port: hostport "+hostport);
  idx=hostport.indexOf(':');
  //System.err.println("RC.port: : "+idx);
  if (idx==-1) return 0;
  return Integer.parseInt(hostport.substring(idx+1));
}

public static String rboName(String name) {
  int idx1=name.indexOf('/');
  int idx2=name.lastIndexOf('/');
  if (idx2==-1) return new String("");
  if (idx1==idx2) return name.substring(0,idx1-1);
  return name.substring(idx1+1,idx2);
}

public static String channel(String name) {
  int idx=name.lastIndexOf('/');
  if (idx==-1) return name;
  else return name.substring(idx+1);
}

}

