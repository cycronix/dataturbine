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

import java.io.*;

/******************************************************************************
 * Store info about the location of a Source: its data path and server address.
 * <p>
 *
 * @author John P. Wilson
 * @author Ian A. Brown
 *
 * @since V2.0
 * @version 03/04/2002
 */

/*
 * Copyright 2001, 2002 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 03/04/2002  INB	Eliminated server name.
 * 06/08/2001  JPW	Add server name. Change hostPortStr to addressStr.
 * 06/07/2001  JPW	Created (Taken from V1.1 RBNB code)
 *
 */
public class SourceInfo extends Object {
    
    /**
     * Source's server address.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public String addressStr = "";
    
    /**
     * Source's data path.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 06/07/2001
     */
    public String dataPathStr = "";
    
    /**************************************************************************
     * Create the SourceInfo object.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @param hostPortStrI  Source's server address.
     * @param dataPathStrI  Data path to the source.
     * @since V2.0
     * @version 03/04/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/04/2002  INB	Eliminated server name.
     * 06/07/2001  JPW  Created. (Taken from V1.1 RBNB code)
     *
     */
    public SourceInfo(
	String addressStrI,
	String dataPathStrI)
    {
	addressStr = addressStrI;
	dataPathStr = dataPathStrI;
    }
    
}
