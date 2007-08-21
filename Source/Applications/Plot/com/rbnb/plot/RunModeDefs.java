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
  ***                                                           ***
  ***   Name : RunModeDefs          ()                          ***
  ***   By   : Eric Friets      (Creare Inc., Hanover, NH)      ***
  ***   For  : FlyScan                                          ***
  ***   Date : December 1997                                    ***
  ***                                                           ***
  ***   Copyright 1997 Creare Inc.                              ***
  ***                                                           ***
  ***   Description : constant definitions for run modes        ***
  ***                                                           ***
  ***   Input :                                                 ***
  ***                                                           ***
  ***   Input/Output :                                          ***
  ***                                                           ***
  ***   Output :                                                ***
  ***                                                           ***
  ***   Returns :                                               ***
  ***                                                           ***
  *****************************************************************
*/

package com.rbnb.plot;

public class RunModeDefs {
   public static final int bof=-3;
   public static final int revPlay=-2;
   public static final int revStep=-1;
   public static final int stop=0;
   public static final int fwdStep=1;
   public static final int fwdPlay=2;
   public static final int eof=3;
   public static final int realTime=4;
   public static final int quit=5;
   public static final int allData=6;
   public static final int current=7;

}
