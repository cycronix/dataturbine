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

import java.util.*;
import java.util.logging.*;
import java.util.logging.Level;

import javax.xml.parsers.*;

import org.w3c.dom.*;


/**
 * BasicSearchSchema implements <code>SearchSchema</code> for the
 * DASL basic search format.
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *
 */
public class BasicSearchSchema implements SearchSchema {
    public static final String TAG_SCHEMA = "D:basicsearchschema";
    public static final String TAG_PROPDESC = "D:propdesc";
    public static final String TAG_ANY_OTHER_PROP = "D:any-other-property";
    public static final String TAG_PROP = "D:prop";
    public static final String TAG_DATATYPE = "D:datatype";
    public static final String TAG_SEARCHABLE = "D:searchable";
    public static final String TAG_SELECTABLE = "D:selectable";
    public static final String TAG_SORTABLE = "D:sortable";
    public static final String TAG_OPDESC = "D:opdesc";
    public static final String TAG_OPERAND_PROP = "D:operand-property";
    public static final String TAG_OPERAND_LIT = "D:operand-literal";
    public static final String TAG_PROPERTIES = "D:properties";
    public static final String TAG_OPERATORS = "D:operators";
    private static final String DAV_NAMESPACE = "DAV:";
    private Hashtable m_propdescs = new Hashtable();
    private Hashtable m_operators = new Hashtable();
    protected Document m_document = null;
    
    private static final Logger m_logger = Logger.getLogger(BasicSearchSchema.class.getName());

    public BasicSearchSchema() {
        try {
            m_document = DocumentBuilderFactory.newInstance()
                                               .newDocumentBuilder()
                                               .newDocument();
        } catch (Exception e) {
        	m_logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void addAnyOtherPropertyDescription(boolean bSearchable,
                                               boolean bSelectable,
                                               boolean bSortable)
                                        throws Exception {
        Element el = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,BasicSearchSchema.TAG_ANY_OTHER_PROP);
        addPropertyDescription(el,null,bSearchable,bSelectable,bSortable);
    }

    public void addPropertyDescription(Element propertyEl,
                                       Element datatypeDef, boolean bSearchable,
                                       boolean bSelectable, boolean bSortable)
                                throws Exception {
        String sPropname = propertyEl.getTagName();

        Element propdescEl = m_document.createElementNS(DAV_NAMESPACE,
                                                        BasicSearchSchema.TAG_PROPDESC);
        Element propEl = m_document.createElementNS(DAV_NAMESPACE,
                                                    BasicSearchSchema.TAG_PROP);

        propEl.appendChild(m_document.importNode(propertyEl,true));
        propdescEl.appendChild(propEl);

        if (datatypeDef != null) {
            Element datatypeEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                            BasicSearchSchema.TAG_DATATYPE);

            datatypeEl.appendChild(m_document.importNode(datatypeDef, true));
            propdescEl.appendChild(datatypeEl);
        }

        if (bSearchable == true) {
            Element searchableEl = m_document.createElementNS(
                                           BasicSearchSchema.DAV_NAMESPACE,
                                           BasicSearchSchema.TAG_SEARCHABLE);
            propdescEl.appendChild(searchableEl);
        }

        if (bSelectable == true) {
            Element selectableEl = m_document.createElementNS(
                                           BasicSearchSchema.DAV_NAMESPACE,
                                           BasicSearchSchema.TAG_SELECTABLE);
            propdescEl.appendChild(selectableEl);
        }

        if (bSortable == true) {
            Element sortableEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                            BasicSearchSchema.TAG_SORTABLE);
            propdescEl.appendChild(sortableEl);
        }

        m_propdescs.put(sPropname, propdescEl);
    }

    public void addOperator(Element OpEl, boolean bIncludeLiteral)
                     throws Exception {
        String sOpName = OpEl.getTagName();
        Element opdescEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                      BasicSearchSchema.TAG_OPDESC);
        opdescEl.appendChild(m_document.importNode(OpEl, true));

        Element operandPropEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                           BasicSearchSchema.TAG_OPERAND_PROP);
        opdescEl.appendChild(operandPropEl);

        if (bIncludeLiteral == true) {
            Element operandLitEl = m_document.createElementNS(
                                           BasicSearchSchema.DAV_NAMESPACE,
                                           BasicSearchSchema.TAG_OPERAND_LIT);
            opdescEl.appendChild(operandLitEl);
        }

        m_operators.put(sOpName, opdescEl);
    }

    public Element asXML() {
        Element schemaEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                      TAG_SCHEMA);
        Element propertiesEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                          BasicSearchSchema.TAG_PROPERTIES);

        for (Iterator i = m_propdescs.values().iterator(); i.hasNext();) {
            Element tempEl = (Element)i.next();
            //if(tempEl.getOwnerDocument().equals(this.m_document)
            propertiesEl.appendChild(tempEl);
        }

        schemaEl.appendChild(propertiesEl);

        Element operatorsEl = m_document.createElementNS(BasicSearchSchema.DAV_NAMESPACE,
                                                         BasicSearchSchema.TAG_OPERATORS);

        for (Iterator i = m_operators.values().iterator(); i.hasNext();) {
            operatorsEl.appendChild((Element) i.next());
        }

        schemaEl.appendChild(operatorsEl);

        return schemaEl;
    }
}