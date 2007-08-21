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
  ***	Name :	SortException.java				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This class provides a special type of exception for	***
  ***	the SortedVector and SortInterface classes.		***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

public class SortException extends Exception {

/*
  *****************************************************************
  ***								***
  ***	Name :	SortException					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This is the default constructor for the exception.	***
  ***								***
  *****************************************************************
*/
  public SortException() {
    super();
  }

/*
  *****************************************************************
  ***								***
  ***	Name :	SortException					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This constructor builds a SortException from the	***
  ***	detail message.						***
  ***								***
  ***	Input :							***
  ***	   messageI		The detail message.		***
  ***								***
  *****************************************************************
*/
  public SortException(String messageI) {
    super(messageI);
  }
}
