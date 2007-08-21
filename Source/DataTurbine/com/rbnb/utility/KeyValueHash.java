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

/* KeyValueHash.java
 * Reads Creare type 1 UserData byte array, parses into key/value pairs,
 * and stores them in a hashtable.  The format of the UserData byte array is
 * key = value <,|\n|\r> sequences, where
 *   key is an ascii string, which may not include ,=\n
 *   = is the ascii character 'equals'
 *   value is an ascii string, as above
 * No trailing separater is expected after the last key/value pair
 * For example, the key/value pairs units/PSI and rate/slow would be the
 * byte sequence 'units=PSI,rate=slow'
 *
 * Modification History :
 * 09/15/2006 - JPW
 *	Add new constructor that can take a String argument and an array of
 *	user-specified terminator characters.
 * 08/18/2000 - INB
 *	Stole from RBNBPlot to use with the RBNB to JMF handling code. In
 *	this case, it handles Creare type 2 user data, which looks like type
 *	1, but is used for audio/video streams.
 */
package com.rbnb.utility;

import java.util.Hashtable;

public class KeyValueHash {
   Hashtable hash = new Hashtable();
   
   public KeyValueHash(byte[] userData) {
      
      // JPW 09/15/2006: These aren't used
      //ByteArrayInputStream byteArray = new ByteArrayInputStream(userData);
      //DataInputStream inStream = new DataInputStream(byteArray);
      
      // JPW 09/15/2006: Add new constructor
      this(new String(userData), null);
   }
   
   public KeyValueHash(String inString) {
       this(inString,null);
   }
   
   // JPW 09/15/2006: Add new constructor that takes a String and an optional
   //                 set of terminating characters
   public KeyValueHash(String inString, char[] terminatorCharsI) {
      
      // The default terminating characters
      char[] terminatorChars = {',','\n','\r'};
      if ( (terminatorCharsI != null) && (terminatorCharsI.length > 0) ) {
	  terminatorChars = terminatorCharsI;
      }
      
      int lt = -1; // location of the terminator from the previous iteration
      int nt=nextTerminator(inString,lt,terminatorChars);
      while (nt!=-1) {
	 int ne=inString.indexOf('=',lt+1);  // next equals sign
	 if (ne>lt+1 && ne<nt-1) { //have key,value isolated
	    hash.put(inString.substring(lt+1,ne),inString.substring(ne+1,nt));
	    //System.out.println(inString.substring(lt+1,ne)+"  "+inString.substring(ne+1,nt));
	    }
	 lt=nt;
	 while ((nt=nextTerminator(inString,lt,terminatorChars))==lt+1) //ignore multiple adjacent separators
	    lt=nt;
	 }
   } //end KeyValueHash constructor
   
   
   public String get(String key) {
      // JPW 09/15/2006: Add some checks for null
      if (key == null) {
	  return null;
      }
      Object value = hash.get(key);
      if (value == null) {
	  return null;
      }
      return (String)value;
   }
   
   public Hashtable getHash() {
      return hash;
   }
   
   private int nextTerminator(String s, int n, char[] t) {
      
      // JPW 09/15/2006: The terminator characters are provided as a parameter
      // char[] t = {',','\n','\r'}; //terminator characters
      
      int min=-1;
      boolean foundOne=false;
      
      if (n>=s.length()) return -1; //at end of string
      
      for (int i=0;i<t.length;i++) {
	 int j=s.indexOf(t[i],n+1);
	 if (j>=0) {
	    if (foundOne) {
	       if (j<min) min=j;
	       }
	    else {
	       foundOne=true;
	       min=j;
	       }
	    }
	 }
      if (foundOne) return min;
      else return s.length();
   }
   
} //end class KeyValueHash
