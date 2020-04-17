package com.github.jochenw.afw.core.util;

import javax.xml.XMLConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.jochenw.afw.core.util.DomHelper.LocalizableException;

/**
 * Utility class for working with Dom nodes.
 */
public class Dom {
	private final DomHelper domHelper = new DomHelper();

	/** Returns the given nodes child elements.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @return The parent nodes child elements.
	 */
	public Iterable<Element> getChildren(Node pNode) {
		return domHelper.getChildren(pNode);
	}

	/** Returns the given nodes child elements with the given local name, and
	 * the {@link XMLConstants#NULL_NS_URI null namespace URI}.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @param pLocalName The local name of the requested child elements.
	 * @return The parent nodes child elements with the given local name,
	 *   and the {@link XMLConstants#NULL_NS_URI null namespace URI}.
	 */
	public Iterable<Element> getChildren(Node pNode, String pLocalName) {
		return domHelper.getChildren(pNode, pLocalName);
	}

	/** Returns the given nodes child elements with the given local name, and
	 * the given namespace URI.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @param pNamespaceUri The namespace URI of the requested child elements.
	 *   Use null, or {@link XMLConstants#NULL_NS_URI} for the default namespace.
	 * @param pLocalName The local name of the requested child elements.
	 * @return The parent nodes child elements with the given local name,
	 *   and the given namespace URI.
	 */
	public Iterable<Element> getChildrenNS(Node pNode, String pNamespaceUri, String pLocalName) {
		return domHelper.getChildrenNS(pNode, pNamespaceUri, pLocalName);
	}

	/**
	 * Returns, whether the given node is an element with the given namespace Uri, and the given
	 * local name.
	 * @param pNode The node, which is being tested.
	 * @param pNamespaceUri The expected namespace URI. Null, or
	 *   {@link XMLConstants#NULL_NS_URI} for the null namespace URI.
	 * @param pLocalName The expected local name.
	 * @return True, if the given node is an element with the given namespace URI, and the given
	 * local name.
	 */
	public boolean isElementNS(Node pNode, String pNamespaceUri, String pLocalName) {
		return domHelper.isElementNS(pNode, pNamespaceUri, pLocalName);
	}

	/**
	 * Asserts, that the given node is an element with the {@link XMLConstants#NULL_NS_URI
	 * null namespace URI}, and the given local name. If the assertion fails, throws a
	 * {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @param pLocalName The expected local name.
	 * @throws LocalizableException The assertion failed.
	 */
	public void assertElement(Node pNode, String pLocalName) throws LocalizableException {
		domHelper.assertElement(pNode, pLocalName);
	}

	/**
	 * Asserts, that the given node is an element with the given
	 * namespace URI, and the given local name. If the assertion
	 * fails, throws a {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @param pNamespaceURI The expected namespace URI. Null, or
	 *   {@link XMLConstants#NULL_NS_URI} for the null namespace URI.
	 * @param pLocalName The expected local name.
	 * @throws LocalizableException The assertion failed.
	 */
	public void assertElementNS(Node pNode, String pNamespaceURI, String pLocalName) throws LocalizableException {
		domHelper.assertElementNS(pNode, pNamespaceURI, pLocalName);
	}

	
}
