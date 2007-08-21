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

import java.util.logging.*;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * 
 * @author Michael Bell
 * @version $Revision: 1.1 $
 *  
 */
public class SchemaResponse extends Response {
	private SearchSchema m_schema = null;

	/**
	 * Logger for this class
	 */
	private static final Logger m_logger = Logger
			.getLogger(SchemaResponse.class.getName());

	public SchemaResponse(Document document, SearchSchema schema,
			String resourceURL) throws ServerException {
		super(document);
		m_schema = schema;
		this.setResource(resourceURL);
	}

	public boolean isOK() {
		boolean isOk = true;

		return isOk;
	}

	public Element asXML() {
		Element response = document.createElementNS("DAV:", "D:response");

		Element href = document.createElementNS("DAV:", "D:href");

		href.appendChild(document.createTextNode(getResource()));
		response.appendChild(href);

		Element query_schema = document.createElementNS("DAV:",
				"D:query-schema");

		query_schema.appendChild(document.importNode(m_schema.asXML(), true));

		response.appendChild(query_schema);

		return response;
	}

	public PropertyResponse toPropertyResponse() {
		PropertyResponse response = new PropertyResponse(this.getResource());
		response.setDescription(getDescription());
		try {
			response.setDocument(DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument());
		} catch (Exception e) {
			m_logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		return response;
	}
}