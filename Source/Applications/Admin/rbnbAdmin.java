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


import com.rbnb.admin.Admin;

/******************************************************************************
 * Utility class for creating an instance of Admin.
 * <p>
 *
 * @author John P. Wilson
 *
 * @see <a href="{@docRoot}/../../Admin/rbnbadmin.html">DataTurbine Administrator Application</a>
 * @since V2.0
 * @version 02/20/2002
 */

/*
 * Copyright 2001,2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/20/2002  JPW	Change the format of the rbnbAdmin constructor;
 *			    now send the unparsed argument list to
 *			    the Admin constructor.
 * 05/01/2001  JPW	Created.
 *
 */

public class rbnbAdmin {

    /**************************************************************************
     * Launch the administrative application.
     * <p>
     * Command line arguments:
     *   -a     Host and port of the server to connect to
     *   -n     Name of the server to connect to
     *   -u     User ID
     * <p>
     *
     * @author John P. Wilson
     *
     * @param argsI  command line arguments
     * @since V2.0
     * @version 02/20/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/20/2002  JPW	Send the un-parsed argument list to the Admin
     *			    constructor.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public final static void main(String[] argsI) {
	
	// JPW 02/20/2002: Send the unparsed argument list to the Admin
	//                 constructor.  The Admin constructor will now
	//                 handle parsing the arguments.
	Admin admin = new Admin(argsI);
	
    }
}
