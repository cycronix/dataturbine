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

package com.rbnb.api;

/**
 * <bold>RBNB</bold> build version information interface.
 * <p>
 * Classes implementing this interface provide information about what version
 * of the <bold>RBNB</bold> the code was built for, the date it was built, and
 * the license identification being used to run the code.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 01/10/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 12/20/2001  INB	Created.
 *
 */
public interface BuildInterface {

    /**
     * Gets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build date.
     * @see #setBuildDate(java.util.Date)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract java.util.Date getBuildDate();

    /**
     * Gets the <bold>RBNB</bold> build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the build version string.
     * @see #setBuildVersion(String)
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract String getBuildVersion();

    /**
     * Gets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the license string.
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    //    public abstract String getLicenseString();

    /**
     * Sets the date that the code was built.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildDateI the build date.
     * @see #getBuildDate()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract void setBuildDate(java.util.Date buildDateI);

    /**
     * Sets the <bold>RBNB</bold> build version.
     * <p>
     *
     * @author Ian Brown
     *
     * @param buildVersionI the build version string.
     * @see #getBuildVersion()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract void setBuildVersion(String buildVersionI);

    /**
     * Sets the <bold>RBNB</bold> license string.
     * <p>
     *
     * @author Ian Brown
     *
     * @param licenseStringI the license string.
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    //    public abstract void setLicenseString(String licenseStringI);
}

