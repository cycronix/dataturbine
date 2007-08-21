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


import com.rbnb.trim.Trim;

/******************************************************************************
 * Utility class for creating an instance of Trim.
 * <p>
 *
 * @author John P. Wilson
 *
 * @see <a href="{@docRoot}/../../Trim/rbnbtrim.html">DataTurbine Source Trim Application</a>
 * @since V2.5
 * @version 07/11/2005
 */

/*
 * Copyright 2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/11/2005  JPW	Created.
 *
 */

public class rbnbTrim {

    /**************************************************************************
     * Launch the Trim application.
     * <p>
     * Command line arguments:
     * Print help message:    <-h | -?>
     * RBNB address:          -a <host[:port]>
     * Start (begin) time:    -b <start time>
     * Stop (end) time:       -e <stop time>
     * Input source:          -i <input source name>
     * Output source:         -o <output source name>
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  command line arguments
     * @since V2.5
     * @version 07/11/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/11/2005  JPW  Created.
     *
     */
    
    public final static void main(String[] argsI) {
	
	new Trim(argsI);
	
    }
    
}

