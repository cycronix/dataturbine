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
package com.ibm.webdav;

import org.w3c.dom.*;

import java.util.*;

/**
 * SearchRequest provides a standard interface to an underlying 
 * search implementation which can then be used in the DAV4J
 * to support the DASL spec. 
 * 
 * Note: Although the DASL spec describes a basic search XML format it
 * does not exclude the use of other formats.
 * 
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *
 */
public interface SearchRequest {
    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";

    public static final int ALL_MEMBERS = -1;
    public static final int THIS_RESOURCE = 0;
    public static final int IMMEDIATE_MEMBERS = 1;

    public void instantiateFromXML(Element xmlElement)
                            throws Exception;

    public SearchSchema getSearchSchema() throws Exception;

    public int getResultLimit();

    public String getScopeURI();

    public int getScopeDepth();

    public Vector getOrderByProperties();

    public String getOrderByDirection(PropertyName propName);

    public SearchCondition getCondition();

    public Vector getSelectProperties();

    public boolean isAllSelectProperties();

    public boolean isIncludePropertyDefinitions();
}