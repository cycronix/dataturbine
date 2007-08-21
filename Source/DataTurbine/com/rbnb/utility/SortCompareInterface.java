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
  ***	Name :	SortCompareInterface.java			***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This interface provides for a method that provides	***
  ***	comparisons on the sort field for the SortedVector	***
  ***	class.							***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

public interface SortCompareInterface extends SortInterface {

/*
  *****************************************************************
  ***								***
  ***	Name :	compareTo					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	October, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method compares two sort objects according to	***
  ***	the specified sort field.				***
  ***								***
  ***	Input :							***
  ***	   identifierI		The sort field identifier. An	***
  ***				object that specifies the field	***
  ***				to sort on.			***
  ***	   otherI		The other sort value to compare	***
  ***				to.				***
  ***								***
  ***	Returns :						***
  ***	   compareTo		0 if the two compare equal,	***
  ***				<0 if this object comes before	***
  ***				the input object		***
  ***				>0 if this object comes after	***
  ***				the input object		***
  ***								***
  *****************************************************************
*/
  public int compareTo(Object identifierI,Object otherI)
  throws SortException;
}
