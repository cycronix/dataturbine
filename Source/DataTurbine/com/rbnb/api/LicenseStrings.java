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
 * License related strings.
 * <p>
 * These strings are used to read and write the license file. They are
 * separated from the license reader in order to help protect the license
 * code.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V1.0
 * @version 10/24/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/24/2001  INB	Converted for use with V2.
 * 11/01/1998  INB	Created.
 *
 */
class LicenseStrings {

    /**
     * index of the string specifying the name of the license file.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int LICENSE_FILE = 0;

    /**
     * index of the string identifying the version.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int VERSION = 1;

    /**
     * index of the string identifying the serial number.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int SERIAL = 2;

    /**
     * index of the string identifying the number of clients.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int NCLIENTS = 3;

    /**
     * index of the string specifying an unlimited number of clients.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int UNLIMITED = 4;

    /**
     * index of the string identifying the security flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int SECURITY = 5;

    /**
     * index of the string identifying the routing flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int ROUTING = 6;

    /**
     * index of the string identifying the mirroring flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int MIRRORS = 7;

    /**
     * index of the string identifying the archiving flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int ARCHIVES = 8;

    /**
     * index of the string identifying the remote connections flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int REMOTE = 9;

    /**
     * index of the string identifying the check serial number flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int CHECK = 10;

    /**
     * index of the string identifying the broadcast serial number flag.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int BROADCAST = 11;

    /**
     * index of the string identifying the support ends date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int SUPPORT = 12;

    /**
     * index of the string identifying the expiration date.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int EXPIRES = 13;

    /**
     * index of the string identifying the signature.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int SIGNATURE = 14;

    /**
     * index of the string specifying the base file name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int BASE_FILE = 15;

    /**
     * index of the no file found string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int NO_FILE = 16;

    /**
     * index of the bad signature found string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int BAD_SIGNATURE = 17;

    /**
     * index of the bad license file string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int BAD_LICENSE_FILE = 18;

    /**
     * index of the bad support date string.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    final static int BAD_SUPPORT_DATE = 19;

    /**
     * the license strings.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V1.0
     * @version 10/24/2001
     */
    private final static String strings[] = {
	"rvsnusrmarcbsesrNBBB",
	"beecneoirehruxiboaaa",
	"nrrllcurcmeoppgn ddd",
	"bsiiiutrhocapinbl   ",
	"LiaemrioitkdoraBisls",
	"iolniinrve cretaciiu",
	"cn tttgse  atsusegcp",
	"e  sey  s  s  rennep",
	"n   d      t  e.sano",
	"s              tetsr",
	"e              x uet",
	".              tfr  ",
	"t               iefe",
	"x               l in",
	"t               eild",
	"                 nes",
	"                w . ",
	"                al d",
	"                si a",
	"                 c t",
	"                fe e",
	"                on .",
	"                us  ",
        "                ne  ",
        "                d   ",
        "                .f  ",
	"                 i  ",
	"                 l  ",
	"                 e  ",
	"                 .  "
    };

    /**
     * Return a string for the license handling code.
     * <p>
     * The strings are stored in a vertical, rather than horizontal,
     * fashion. This makes searching for them difficult, reducing the
     * possibility of them being found by someone wishing to break the
     * licensing protection.
     * <p>
     *
     * @author Ian Brown
     *
     * @param indexI the index of the string.
     * @return the string.
     * @since V1.0
     * @version 10/24/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/24/2001  INB	Converted for use with V2.
     * 11/01/1998  INB	Created.
     *
     */
    final static String getString(int indexI) {
	String stringR = null;

	/* Build the string. */
	int lastIdx;
	for (lastIdx = strings.length - 1;
	     lastIdx > 0;
	     --lastIdx) {
	    if ((strings[lastIdx].length() > indexI) &&
		(strings[lastIdx].charAt(indexI) != ' ')) {
		break;
	    }
	}
	++lastIdx;
	stringR = new Character(strings[0].charAt(indexI)).toString();
	for (int idx = 1; idx < lastIdx; ++idx) {
	    stringR += strings[idx].charAt(indexI);
	}

	return (stringR);
    }
}
