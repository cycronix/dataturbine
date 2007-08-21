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

// DataTimeStamps - replacement for COM.Creare.RBNB.DataTimeStamps;

package com.rbnb.plot;

public class DataTimeStamps {

public final static int first=0;
public final static int last=-1;

private double[] times=null;
private int numberOfIntervals=1;
public int format=0;

public DataTimeStamps(double[] t) {
//System.err.println("DataTimeStamps: "+t.length+":  "+t[0]+" for "+(t[t.length-1]-t[0]));
  times=t;
  if (times[0]>9e8) format=Time.AbsoluteSeconds1970; //a crude assumption...
}

public int getNumberOfIntervals() {
  return numberOfIntervals;
}

public Time getStartOfInterval(int i) {
  if (times!=null) return new Time(times[0]);
  else return null;
}

public Time getDurationOfInterval(int i) {
  if (times!=null) return new Time(times[times.length-1]-times[0]);
  else return null;
}

public Time getEndOfInterval(int i) {
  if (times!=null) return new Time(times[times.length-1]);
  else return null;
}

public int getFormat() {
  return format;
}

public double[] getTimesDouble(int start, int count) {
  return times;
}

}

