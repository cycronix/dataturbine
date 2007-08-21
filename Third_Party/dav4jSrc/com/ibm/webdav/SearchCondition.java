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
 */

package com.ibm.webdav;

import java.util.*;

/**
 * @author Michael Bell
 * @version $Revision: 1.1 $
 */

public class SearchCondition {

  protected Vector m_conditions = new Vector();
  protected String m_sOperator = "and";

  public SearchCondition() {
  }

  public void addCondition(SearchConditionTerm term) {
    m_conditions.add(term);
  }

  public void addCondition(SearchCondition cond) {
    m_conditions.add(cond);
  }

  public void setOperator(String sOp) {
    m_sOperator = sOp;
  }

  public String getOperator() {
    return m_sOperator;
  }

  public int size() {
    return this.m_conditions.size();
  }

  public Object getCondition(int index) {
    return m_conditions.elementAt(index);
  }

  public boolean isConditionLeaf(Object obj) {
    return (obj instanceof SearchConditionTerm);
  }

  public boolean isConditionLeaf(int index) {
    return isConditionLeaf(this.m_conditions.elementAt(index));
  }

}