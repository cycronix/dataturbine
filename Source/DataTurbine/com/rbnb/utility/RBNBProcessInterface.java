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

// RBNBProcessInterface - interface through which RBNBProcess tells
//                        an instance of an object to terminate
// Eric Friets
// 4/20/01
// Copyright 2001 Creare Incorporated

package com.rbnb.utility;

public interface RBNBProcessInterface extends Runnable{

  public void stopProcess();

  public String getUsername();

  public String getPassword();

}
