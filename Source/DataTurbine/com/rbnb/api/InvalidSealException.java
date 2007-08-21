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
 * Invalid seal exception.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 06/04/2001
 */

/*
 * Copyright 2001 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 06/04/2001  INB	Created.
 *
 */
class InvalidSealException
    extends com.rbnb.api.RBNBException
{
    /**
     * the minimum valid time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #before
     * @since V2.0
     * @version 06/04/2001
     */
    private long after = Long.MIN_VALUE;

    /**
     * the maximum valid time.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #after
     * @since V2.0
     * @version 06/04/2001
     */
    private long before = Long.MAX_VALUE;

    /**
     * the invalid <code>Seal</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 06/04/2001
     */
    private Seal invalid = null;

    /**
     * Class constructor to build an <code>InvalidSealException</code> for an
     * invlaid <code>Seal</code> and minimum and maximum valid times.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sealI   the invalid <code>Seal</code>.
     * @param afterI  the minimum valid time.
     * @param beforeI the maximum valid time.
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    InvalidSealException(Seal sealI,long afterI,long beforeI) {
	super();
	setInvalid(sealI);
	setAfter(afterI);
	setBefore(beforeI);
    }

    /**
     * Gets the minimum valid time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the minimum valid time.
     * @see #setAfter(long)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final long getAfter() {
	return (after);
    }

    /**
     * Gets the maximum valid time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the maximum valid time.
     * @see #setBefore(long)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final long getBefore() {
	return (before);
    }

    /**
     * Gets the invalid <code>Seal</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the invalid <code>Seal</code>.
     * @see #setInvalid(com.rbnb.api.Seal)
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final Seal getInvalid() {
	return (invalid);
    }

    /**
     * Sets the minimum valid time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param afterI the minimum valid time.
     * @see #getAfter()
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final void setAfter(long afterI) {
	after = afterI;
    }

    /**
     * Sets the maximum valid time.
     * <p>
     *
     * @author Ian Brown
     *
     * @param beforeI the maximum valid time.
     * @see #getBefore()
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final void setBefore(long beforeI) {
	before = beforeI;
    }

    /**
     * Sets the invalid <code>Seal</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sealI the invalid <code>Seal</code>.
     * @see #getInvalid()
     * @since V2.0
     * @version 06/04/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 06/04/2001  INB	Created.
     *
     */
    final void setInvalid(Seal sealI) {
	invalid = sealI;
    }
}
