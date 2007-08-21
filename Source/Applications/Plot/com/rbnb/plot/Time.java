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

// Time - replacement for COM.Creare.RBNB.Time

package com.rbnb.plot;

import java.text.NumberFormat;

public class Time {

public final static int Full=0;
public final static int Fine=1;
public final static int Coarse=2;
public final static int Unspecified=0;
public final static int AbsoluteSeconds1970=1;
public final static int RelativeSeconds=2;

public long mantissa=0;
private double time;

//EMF 8/5/03: reduce insignificant fractional digits in time
private static NumberFormat nf=null;

public Time() {
  time=System.currentTimeMillis()/1000.0;
}

public Time(double t) {
  time=t;
  mantissa=(long)time;
}

public Time(Time t) {
  time=t.getDoubleValue();
  mantissa=(long)time;
}

public Time(long man, byte exp) {
  time=man*Math.pow(10,exp);
  mantissa=(long)time;
}

public String getFormattedString(int format, int resolution, int precision) {
  String timeString=null;
  if (format==AbsoluteSeconds1970) {
    timeString = com.rbnb.api.Time.since1970(time);
  } else {
    if (nf==null) {
      nf=NumberFormat.getInstance();
      nf.setMaximumFractionDigits(6);
    }
    //timeString = Double.toString(time);
    timeString = nf.format(time);
  }
  return timeString;
}

public static Time fromFormattedString(String timeString) throws Exception {
	return new Time(com.rbnb.api.Time.fromFormattedString(timeString));
}

public float getFloatValue() {
  return (float)time;
}

public double getDoubleValue() {
  return time;
}

public int compareTo(Time other) {
  if (time<other.time) return -1;
  else if (time==other.time) return 0;
  else return 1;
}

public Time addTime(Time other) {
  return new Time(time+other.time);
}

public Time subtractTime(Time other) {
  return new Time(time-other.time);
}

public Time multiplyTime(Time other) {
  return new Time(time*other.time);
}

public Time divideTime(Time other) {
  return new Time(time/other.time);
}

public Time multiplyBy(int tint) {
  return new Time(time*tint);
}

public Time divideBy(int tint) {
  return new Time(time/tint);
}

public int getIntValue() {
  return (int)time;
}

public String toString() {
	return Double.toString(time);
}

}

