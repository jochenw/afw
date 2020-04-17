package com.github.jochenw.afw.core.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.jochenw.afw.core.util.DomHelper.LocalizableException;

/**
 * Utility class for working with Dom nodes.
 */
public class Dom {
	private static final DomHelper domHelper = new DomHelper();

	/** Returns the given nodes child elements.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @return The parent nodes child elements.
	 */
	public static @Nonnull Iterable<Element> getChildren(@Nonnull Node pNode) {
		return domHelper.getChildren(pNode);
	}

	/** Returns the given nodes child elements with the given local name, and
	 * the {@link XMLConstants#NULL_NS_URI null namespace URI}.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @param pLocalName The local name of the requested child elements.
	 * @return The parent nodes child elements with the given local name,
	 *   and the {@link XMLConstants#NULL_NS_URI null namespace URI}.
	 */
	public static @Nonnull Iterable<Element> getChildren(@Nonnull Node pNode, @Nonnull String pLocalName) {
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
	public static @Nonnull Iterable<Element> getChildrenNS(@Nonnull Node pNode, @Nullable String pNamespaceUri, @Nonnull String pLocalName) {
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
	public static boolean isElementNS(@Nonnull Node pNode, @Nullable String pNamespaceUri, @Nonnull String pLocalName) {
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
	public static void assertElement(@Nonnull Node pNode, @Nonnull String pLocalName) throws LocalizableException {
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
	public static void assertElementNS(@Nonnull Node pNode, @Nullable String pNamespaceURI, @Nonnull String pLocalName) throws LocalizableException {
		domHelper.assertElementNS(pNode, pNamespaceURI, pLocalName);
	}

	/**
	 * Returns, whether the given node is an element with the
	 * {@link XMLConstants#NULL_NS_URI null namespace URI}, and the given local name.
	 * @param pNode The node, which is being tested.
	 * @param pLocalName The expected local name.
	 * @return True, if the given node is an element with the
	 * {@link XMLConstants#NULL_NS_URI null namespace URI},
	 * and the given local name.
	 */
	public static boolean isElement(@Nonnull Node pNode, @Nonnull String pLocalName) {
		return domHelper.isElement(pNode, pLocalName);
	}

	/** Returns the first child element of the given node, which has the
	 * {@link XMLConstants#NULL_NS_URI null namespace URI}, and the
	 * given local name. If no such child element is found, returns null.
	 * @param pNode The requested child's parent node.
	 * @param pLocalName The requested child's local name.
	 * @return The first child element of the given node, which has the
	 * {@link XMLConstants#NULL_NS_URI null namespace URI}, and the
	 * given local name. Null, if no such child is present.
	 */
	public static @Nullable Element getFirstChild(@Nonnull Node pNode, String pLocalName) {
		return domHelper.getFirstChild(pNode, pLocalName);
	}

	/** Returns the first child element of the given node, which has the given
	 * namespace URI, and the given local name. If no such child element is
	 * found, returns null.
	 * @param pNode The requested child's parent node.
	 * @param pNamespaceURI The requested child's namespace URI.
	 * @param pLocalName The requested child's local name.
	 * @return The first child element of the given node, which has the given
	 * namespace URI, and the given local name. Null, if no such child is present.
	 */
	public static @Nullable Element getFirstChildNS(@Nonnull Node pNode, String pNamespaceURI, String pLocalName) {
		return domHelper.getFirstChildNS(pNode, pNamespaceURI, pLocalName);
	}

	/** Returns the first child element of the given node, which has the
	 * {@link XMLConstants#NULL_NS_URI null namespace URI}, and the
	 * given local name. If no such child element is found, returns null.
	 * @param pNode The requested child's parent node.
	 * @param pLocalName The requested child's local name.
	 * @return The first child element of the given node, which has the
	 * {@link XMLConstants#NULL_NS_URI null namespace URI}, and the
	 * given local name. If no such child element is
	 * found, throws a {@link LocalizableException}.
	 * @throws LocalizableException No matching child is present.
	 */
	public static @Nullable Element requireFirstChild(@Nonnull Node pNode, String pLocalName) throws LocalizableException {
		return domHelper.requireFirstChild(pNode, pLocalName);
	}

	/** Returns the first child element of the given node, which has the given
	 * namespace URI, and the given local name. If no such child element is
	 * found, throws a {@link LocalizableException}.
	 * @param pNode The requested child's parent node.
	 * @param pNamespaceURI The requested child's namespace URI.
	 * @param pLocalName The requested child's local name.
	 * @return The first child element of the given node, which has the given
	 * namespace URI, and the given local name. If no such child element is
	 * found, throws a {@link LocalizableException}.
	 * @throws LocalizableException No matching child is present.
	 */
	public static @Nonnull Element requireFirstChildNS(@Nonnull Node pNode, String pNamespaceURI, String pLocalName)
	    throws LocalizableException {
		return domHelper.requireFirstChildNS(pNode, pNamespaceURI, pLocalName);
	}
}
