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


package com.rbnb.admin;

/******************************************************************************
 * Class which stores data associated with a server shortcut.
 * <p>
 *
 * @author John P. Wilson
 *
 * @since V2.0
 * @version 02/12/2002
 */

/*
 * Copyright 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/12/2002  JPW	Created.
 *
 */

public class ShortcutData {
    
    /**
     * 
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/12/2002
     */
    public String destinationAddress;
    
    /**
     * 
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/12/2002
     */
    public String name;
    
    /**
     * 
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/12/2002
     */
    public double cost;
    
    /**
     * 
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/12/2002
     */
    public static final double MAX_COST_NOT_INCLUSIVE =
        Double.MAX_VALUE/1000000;
    
    /**************************************************************************
     * Create object which will store data associated with a server shortcut.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param destinationAddressI  address of the destination server
     * @param nameI  name of the shortcut
     * @param costI  network cost associated with moving data via this shortcut
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  JPW  Created.
     *
     */
    
    public ShortcutData(String destinationAddressI,
                        String nameI,
                        double costI)
    {
	
	destinationAddress = destinationAddressI;
	name = nameI;
	cost = costI;
	
    }
    
    /**************************************************************************
     * Create object which will store data associated with a server shortcut.
     * <p>
     * Use default values to initialize fields.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  JPW  Created.
     *
     */
    
    public ShortcutData() {
	
	destinationAddress = "localhost:3333";
	name = "Shortcut";
	cost = 1.0;
	
    }
    
    /**************************************************************************
     * Return a String containing the data in this ShortcutData object.
     * <p>
     *
     * @author John P. Wilson
     *
     * @return  a String containing the data in this ShortcutData object
     * @since V2.0
     * @version 02/12/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/12/2002  JPW  Created.
     *
     */
    
    public String toString() {
	
	return(
	    new String(
	        "Shortcut data: destination server address = " +
	        destinationAddress +
	        ", shortcut name = " +
	        name +
	        ", cost = " +
	        cost));
	
    }
    
}
