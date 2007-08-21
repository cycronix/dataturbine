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

// WebDate.java - parses, compares, and otherwise deals with dates
//
// EMF
// 11/20/01
// Copyright 2001 Creare Incorporated

package com.rbnb.webCache;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WebDate {
  private static String format=new String("EEE, dd MMM yyyy HH:mm:ss zzz");
  private static SimpleDateFormat sdf=new SimpleDateFormat(format);
  private static boolean firstUse=true;

  private Date date=null;

  public WebDate() {
    if (firstUse) {
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      firstUse=false;
    }
    date=new Date();
  }

  public WebDate(String dateString) throws Exception {
    if (firstUse) {
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      firstUse=false;
    }
    try {
      date=sdf.parse(dateString);
    } catch (Exception e) {
      //try other formats here
      System.err.println("WebDate exception: "+e.getMessage());
      throw e;
    }
  }

  //getDateString method - returns date as string in appropriate format
  public String getDateString() {
    return sdf.format(date);
  }
  
  public String toString() {
      return getDateString();
  }

  //earlierThan method - compares input WebDate to this one
  public boolean laterThan(WebDate wd) {
    return date.after(wd.date);
  }

} //end class WebDate

