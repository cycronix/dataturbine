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
 * FIXME - Michael Bell DIDN'T GIVE ME A DESCRIPTION!!
 * @author Michael Bell
 * @version $Revision: 1.1 $
 * @since January 27, 2004
 */
public class SearchConditionTerm {

	protected String m_sOperator = null;
	protected String m_sValue = null;

	/**
	 * 
	 */
	public SearchConditionTerm(String sOperator,String sValue) {
		m_sOperator = sOperator;
		m_sValue = sValue;
	}

	public String getOperator() {
		return m_sOperator;
	}

	public String getValue() {
		return m_sValue;
	}
}
