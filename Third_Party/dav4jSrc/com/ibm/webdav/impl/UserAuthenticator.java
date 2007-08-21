/*
 * (C) Copyright Simulacra Media Ltd, 2004.  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * Simulacra Media Ltd will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will Simulacra Media Ltd be liable for any
 * special, indirect or consequential damages or lost profits even if
 * Simulacra Media Ltd has been advised of the possibility of their occurrence. 
 * Simulacra Media Ltd will not be liable for any third party claims against you.
 * 
 */

package com.ibm.webdav.impl;

import com.ibm.webdav.WebDAVException;

/**
 * UserAuthenticator implements all WebDAV authentication methods that are
 * dependent on a specific repository manager interface. This manager is
 * used by ResourceImpl and its subclasses to interface with a particular
 * repository manager for authenticating the request principal. 
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *
 */
public interface UserAuthenticator {

    public boolean authenticate(String user,String pwd) throws WebDAVException ;

    public boolean isSuperUser(ResourceImpl resource) throws WebDAVException ;
}