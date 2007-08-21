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

import java.util.logging.*;

import org.w3c.dom.Element;

import com.ibm.webdav.basicsearch.BasicSearchRequest;


/**
 * @author Michael Bell
 * @version 1.0
 */
public class SearchRequestFactory {
	
	/**
	 * Logger for this class
	 */
	private static final Logger m_logger = Logger.getLogger(SearchRequestFactory.class.getName());
	
    public SearchRequestFactory() {
    }

    public static SearchRequest getSearchRequest(Element xmlElement)
                                          throws WebDAVException {
        SearchRequest request = null;

        try {
            if (xmlElement.getNamespaceURI().equals("DAV:") &&
                    xmlElement.getLocalName()
                              .equals(BasicSearchRequest.TAG_BASICSEARCH)) {
                request = new BasicSearchRequest();

                request.instantiateFromXML(xmlElement);
            }
        } catch (WebDAVException e) {
            throw e;
        } catch (Exception e) {
        	m_logger.log(Level.WARNING, e.getLocalizedMessage(), e);
            throw new WebDAVException(WebDAVStatus.SC_INTERNAL_SERVER_ERROR,
                                      e.getMessage());
        }

        return request;
    }
}