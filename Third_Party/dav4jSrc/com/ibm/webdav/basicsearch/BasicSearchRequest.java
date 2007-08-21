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

package com.ibm.webdav.basicsearch;

import com.ibm.webdav.*;

import java.net.URLDecoder;
import java.util.*;

import org.w3c.dom.*;

/**
 * BasicSearchRequest implements the <code>SearchRequest</code>
 * interface for the DASL basic search format.
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *
 */
public class BasicSearchRequest implements SearchRequest {
	public static final String TAG_BASICSEARCH = "basicsearch";
	public static final String TAG_SELECT = "select";
	public static final String TAG_FROM = "from";
	public static final String TAG_WHERE = "where";
	public static final String TAG_ORDERBY = "orderby";
	public static final String TAG_ORDER = "order";
	public static final String TAG_LIMIT = "limit";
	public static final String TAG_PROP = "prop";
	public static final String TAG_LITERAL = "literal";
	public static final String TAG_ALLPROP = "allprop";
	public static final String TAG_HREF = "href";
	public static final String TAG_DEPTH = "depth";
	public static final String TAG_DESC = "descending";
	public static final String TAG_ASC = "ascending";
	public static final String TAG_NRESULTS = "nresults";
	private static final String DAV_NAMESPACE = "DAV:";
	public static Hashtable LOG_OPS = new Hashtable();
	public static Hashtable COMP_OPS = new Hashtable();
	public static Hashtable STRING_OPS = new Hashtable();
	public static Hashtable CONTENT_OPS = new Hashtable();
	public static Hashtable SPECIAL_OPS = new Hashtable();
	public static Hashtable ALL_OPS = new Hashtable();
	
	public static String OPERATOR_OR = "or";
	public static String OPERATOR_AND = "and";

	static {
		LOG_OPS.put(OPERATOR_AND, OPERATOR_AND);
		LOG_OPS.put(OPERATOR_OR, OPERATOR_OR);

		COMP_OPS.put("eq", "=");
		COMP_OPS.put("lt", "<");
		COMP_OPS.put("gt", ">");
		COMP_OPS.put("lte", "<=");
		COMP_OPS.put("gte", ">=");

		STRING_OPS.put("like", "like");
		CONTENT_OPS.put("contains", "contains");

		SPECIAL_OPS.put("isdefined", "isdefined");
		SPECIAL_OPS.put("is-collection", "is-collection");

		ALL_OPS.putAll(LOG_OPS);
		ALL_OPS.putAll(STRING_OPS);
		ALL_OPS.putAll(CONTENT_OPS);
		ALL_OPS.putAll(SPECIAL_OPS);
		ALL_OPS.putAll(COMP_OPS);
	}

	protected String m_scope_uri = null;
	protected int m_scope_depth = 0;
	protected boolean m_bSelectAllProps = false;
	protected Vector m_select_props = new Vector();
	protected SearchCondition m_condition = new SearchCondition();
	protected Vector m_orderbyProps = new Vector();
	protected Vector m_orderbyDirections = new Vector();
	protected boolean m_bIncludeDefinitions = true;
	protected int m_nLimit = -1;

	public BasicSearchRequest() {
	}

	public void instantiateFromXML(Element xmlElement) throws Exception {
		if (xmlElement
			.getLocalName()
			.equals(BasicSearchRequest.TAG_BASICSEARCH)
			== false) {
			throw new Exception("Invalid Tag");
		}

		NodeList nodes = xmlElement.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element tempEl = (Element) nodes.item(i);
			String sTagName = tempEl.getLocalName();

			if (sTagName.equals(BasicSearchRequest.TAG_SELECT)) {
				NodeList props =
					tempEl.getElementsByTagNameNS(
						DAV_NAMESPACE,
						BasicSearchRequest.TAG_PROP);

				if (props.getLength() > 0) {
					for (int j = 0; j < props.getLength(); j++) {
						Element propEl = (Element) props.item(j);

						NodeList nl = propEl.getElementsByTagName("*");

						if (nl.getLength() > 0) {
							Element indpropEl = (Element) nl.item(0);
							PropertyName pname = new PropertyName(indpropEl);
							

							this.m_select_props.add(pname);
						}
					}
				} else {
					this.m_bSelectAllProps = true;
				}
			} else if (sTagName.equals(BasicSearchRequest.TAG_FROM)) {
				Element depthEl =
					(Element) tempEl
						.getElementsByTagNameNS(
							DAV_NAMESPACE,
							BasicSearchRequest.TAG_DEPTH)
						.item(0);
				String sdepth = ((Text) depthEl.getFirstChild()).getNodeValue();

				try {
					this.m_scope_depth = Integer.parseInt(sdepth);
				} catch (NumberFormatException e) {
					m_scope_depth = -1;
				}

				Element hrefEl =
					(Element) tempEl
						.getElementsByTagNameNS(
							DAV_NAMESPACE,
							BasicSearchRequest.TAG_HREF)
						.item(0);

				this.m_scope_uri =
					((Text) hrefEl.getFirstChild()).getNodeValue();

				m_scope_uri = URLDecoder.decode(m_scope_uri, "UTF-8");

			} else if (sTagName.equals(BasicSearchRequest.TAG_WHERE)) {
				Element condEl =
					(Element) tempEl.getElementsByTagName("*").item(0);

				m_condition = this.createCondition(condEl);
			} else if (sTagName.equals(BasicSearchRequest.TAG_ORDERBY)) {
				NodeList orderNodes =
					tempEl.getElementsByTagNameNS(
						DAV_NAMESPACE,
						BasicSearchRequest.TAG_ORDER);

				for (int j = 0; j < orderNodes.getLength(); j++) {
					Element orderEl = (Element) orderNodes.item(j);
					Element propEl =
						(Element) orderEl
							.getElementsByTagNameNS(
								DAV_NAMESPACE,
								BasicSearchRequest.TAG_PROP)
							.item(0);
					this.m_orderbyProps.add(
						new PropertyName(
							(Element) propEl.getElementsByTagName("*").item(
								0)));

					if (orderEl
						.getElementsByTagNameNS(
							DAV_NAMESPACE,
							BasicSearchRequest.TAG_DESC)
						.getLength()
						> 0) {
						m_orderbyDirections.add(SearchRequest.ORDER_DESC);
					} else {
						m_orderbyDirections.add(SearchRequest.ORDER_ASC);
					}
				}
			} else if (sTagName.equals(BasicSearchRequest.TAG_LIMIT)) {
				Element numresults =
					(Element) tempEl
						.getElementsByTagNameNS(
							DAV_NAMESPACE,
							BasicSearchRequest.TAG_NRESULTS)
						.item(0);
				Text txt = (Text) numresults.getFirstChild();

				this.m_nLimit = Integer.parseInt(txt.getNodeValue());
			}
		}
	}

	public int getResultLimit() {
		return m_nLimit;
	}

	public String getScopeURI() {
		return m_scope_uri;
	}

	public int getScopeDepth() {
		return m_scope_depth;
	}

	public SearchSchema getSearchSchema() throws Exception {
		return new BasicSearchSchema();
	}

	public SearchCondition getCondition() {
		return this.m_condition;
	}

	public Vector getSelectProperties() {
		return this.m_select_props;
	}

	public boolean isAllSelectProperties() {
		return this.m_bSelectAllProps;
	}

	public boolean isIncludePropertyDefinitions() {
		return m_bIncludeDefinitions;
	}

	public Vector getOrderByProperties() {
		return this.m_orderbyProps;
	}

	public String getOrderByDirection(PropertyName propName) {
		return (String) this.m_orderbyDirections.elementAt(
			this.m_orderbyProps.indexOf(propName));
	}

	private SearchCondition createCondition(Element condEl) throws Exception {
		SearchCondition searchCond = new SearchCondition();
		String sTagname = condEl.getLocalName();

		if (BasicSearchRequest.ALL_OPS.containsKey(sTagname) == false) {
			throw new Exception("Invalid operator");
		}

		if (BasicSearchRequest.LOG_OPS.containsKey(sTagname)) {
			searchCond.setOperator(sTagname);

			NodeList nodes = condEl.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				Element tempEl = (Element) nodes.item(i);

				if (LOG_OPS.containsKey(tempEl.getLocalName())) {
					SearchCondition cond = createCondition(tempEl);
					if (cond != null) {
						searchCond.addCondition(cond);
					}

				} else {
					SearchConditionTerm term = createConditionTerm(tempEl);
					if (term != null) {
						searchCond.addCondition(term);
					}
				}
			}
		} else {
			SearchConditionTerm term = createConditionTerm(condEl);

			if (term != null) {
				searchCond.addCondition(term);
			}
		}

		return searchCond;
	}

	private SearchConditionTerm createConditionTerm(Element condEl)
		throws Exception {
		SearchConditionTerm condTerm = null;
		String sOperator = condEl.getLocalName();

		if (COMP_OPS.containsKey(sOperator)
			|| BasicSearchRequest.STRING_OPS.containsKey(sOperator)
			|| BasicSearchRequest.CONTENT_OPS.containsKey(sOperator)
			|| BasicSearchRequest.SPECIAL_OPS.containsKey(sOperator)) {
			Element propEl =
				(Element) condEl
					.getElementsByTagNameNS(
						DAV_NAMESPACE,
						BasicSearchRequest.TAG_PROP)
					.item(0);

			if (propEl != null) {

				NodeList nodes = propEl.getElementsByTagName("*");

				if (nodes.getLength() > 0) {

					Element propPropEl = (Element) nodes.item(0);

					String sValue = null;

					if (BasicSearchRequest.SPECIAL_OPS.containsKey(sOperator)
						== false) {
						Element literalEl =
							(Element) condEl
								.getElementsByTagNameNS(
									DAV_NAMESPACE,
									BasicSearchRequest.TAG_LITERAL)
								.item(0);
						if(literalEl.getChildNodes().getLength() > 0) {
							sValue =
								((Text) literalEl.getFirstChild()).getNodeValue();
						}
						
					}
					
					if(sValue != null) {
						condTerm =
							new SearchPropertyConditionTerm(
								new PropertyName(propPropEl),
								(String) BasicSearchRequest.ALL_OPS.get(
									sOperator),
								sValue);
					}
					
				}
			} else {
				String sVal = condEl.getFirstChild().getNodeValue();
				
				condTerm = new SearchConditionTerm(sOperator,sVal);
				
			}
		} else {
			throw new Exception("InvalidOperator - " + sOperator);
		}

		return condTerm;
	}

	private void addOrder(Element orderEl) {
		String sTagname = orderEl.getLocalName();

		if (sTagname.equals(BasicSearchRequest.TAG_ORDER)) {
			Element propEl =
				(Element) orderEl
					.getElementsByTagNameNS(
						DAV_NAMESPACE,
						BasicSearchRequest.TAG_PROP)
					.item(0);

			Element propPropEl =
				(Element) propEl.getElementsByTagName("*").item(0);
			this.m_orderbyProps.add(new PropertyName(propPropEl));

			if (orderEl
				.getElementsByTagNameNS(
					DAV_NAMESPACE,
					BasicSearchRequest.TAG_DESC)
				.getLength()
				> 0) {
				this.m_orderbyDirections.add(ORDER_DESC);
			} else {
				this.m_orderbyDirections.add(ORDER_ASC);
			}
		}
	}
}