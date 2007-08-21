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

/**
 * @author Michael Bell
 * @version 1.0
 */

public class SearchPropertyConditionTerm extends SearchConditionTerm {

  protected PropertyName m_propName = null;
  public SearchPropertyConditionTerm(PropertyName propName,String sOperator,String sValue) {
	super(sOperator,sValue);
    m_propName = propName;
  }


  public String getPropertyLocalName() {
    return m_propName.getLocal();
  }

  public PropertyName getPropertyName() {
    return m_propName;
  }

}