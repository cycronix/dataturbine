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
  ***	Name :	SortInterface.java				***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This interface provides for a method that selects	***
  ***	the sort field for an object in a sorted vector. It	***
  ***	allows for multiple sort fields if desired.		***
  ***								***
  *****************************************************************
*/
package com.rbnb.utility;

public interface SortInterface {

/*
  *****************************************************************
  ***								***
  ***	Name :	sortField					***
  ***	By   :	Ian Brown	(Creare Inc., Hanover, NH)	***
  ***	For  :	DataTurbine					***
  ***	Date :	September, 1999					***
  ***								***
  ***	Copyright 1999 Creare Inc.				***
  ***	All Rights Reserved					***
  ***								***
  ***	Description :						***
  ***	   This method returns the value of the sort field for	***
  ***	an object. The caller can select which of an optional	***
  ***	set of sort fields to use by passing in the sort field	***
  ***	identifier.						***
  ***								***
  ***	Input :							***
  ***	   identifierI		The sort field identifier. An	***
  ***				object that specifies the field	***
  ***				to sort on.			***
  ***								***
  ***	Returns :						***
  ***	    sortField		An object containing the value	***
  ***				of the sort field.		***
  ***								***
  *****************************************************************
*/
  public Object sortField(Object identifierI) throws SortException;
}
